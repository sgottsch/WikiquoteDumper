package mwdumper.articleprocessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import de.l3s.cleopatra.quotekg.data.DataStorage;
import de.l3s.cleopatra.quotekg.data.WikiquoteToJSONWriter;
import de.l3s.cleopatra.quotekg.model.Footnote;
import de.l3s.cleopatra.quotekg.model.Language;
import de.l3s.cleopatra.quotekg.util.StopTitlesLoader;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.Reference;
import mwdumper.model.ExternalLink;
import mwdumper.model.Line;
import mwdumper.model.Link;
import mwdumper.model.Section;
import mwdumper.model.Template;

public class TextExtractor {

	private Article article;

	private boolean printExampleEntity = false;
	private String exampleEntity = null;
	private Set<String> stopTitles;

	private int numberOfThreads;

	public static void main(String[] args) throws IOException {

		String entity = args[0]; // e.g., "Andy Warhol";
		Language language = Language.getLanguage(args[1]); // e.g., "da" for Danish
		String dataFolder = args[2]; // path to the input files
		String examplesFolder = args[3]; // path to the input files
		String outputFolder = args[4]; // path to the output files

		String fileName = entity.toLowerCase().replace(" ", "_") + "_" + language.getLanguage();

		Article article = new Article(entity, 123, 456); // dummy IDs
		article.setText(FileUtils.readFileToString(new File(examplesFolder + fileName + ".txt"), "UTF-8"));
		TextExtractor te = new TextExtractor(article, 1, StopTitlesLoader.getStopTitles(language));
		te.setExample(entity);
		te.extractQuotes();

		DataStorage dataStorage = new DataStorage();
		dataStorage.getArticles().add(article);

		WikiquoteToJSONWriter.writeJSONs(dataStorage, outputFolder + fileName + ".json", dataFolder + "types.ttl",
				dataFolder + "/" + language.getLanguage() + "/ids.ttl", language);
	}

	private void setExample(String entity) {
		this.exampleEntity = entity;
		this.printExampleEntity = true;
	}

	public TextExtractor(Article article, int numberOfThreads, Set<String> stopTitles) {
		article.setText(StringEscapeUtils.unescapeHtml4(article.getText()));
		this.article = article;
		this.numberOfThreads = numberOfThreads;
		this.stopTitles = stopTitles;
	}

	public void extractQuotes() {

		List<Line> lines = new ArrayList<Line>();
		for (String line : article.getText().split("\n"))
			lines.add(new Line(line));

		identifySections(lines);

	}

	private void identifySections(List<Line> lines) {

		if (printExampleEntity && exampleEntity != null && !exampleEntity.equals(this.article.getWikiquoteId()))
			return;

		Map<Integer, Section> currentSectionPerLevel = new HashMap<Integer, Section>();
		currentSectionPerLevel.put(0,
				new Section(0, new Line(this.article.getWikiquoteId(), this.article.getWikiquoteId())));
		Section currentSection = currentSectionPerLevel.get(0);
		this.article.setMainSection(currentSection);

		Line currentLine = null;

		Template currentTemplate = null;

		boolean hasQuotes = false;

		List<Line> linesToParse = new ArrayList<Line>();

		Integer lastSectionLevel = null;
		for (Line line : lines) {

			String rawTextTitle = line.getRawText().strip();
			if (rawTextTitle.startsWith("==") && rawTextTitle.endsWith("==")) {
				rawTextTitle = StringUtils.strip(rawTextTitle, "=");
				if (stopTitles.contains(rawTextTitle)) {
					break;
				}
			}

			// Italian Wikiquote has an attribute to assign chronological quotes
			if (currentSection != null && line.getRawText().equals("{{cronologico}}"))
				currentSection.setIsChronological(true);
			if (currentSection != null && line.getRawText().equals("<small>chronologisch</small>"))
				currentSection.setIsChronological(true);

			// one-line templates
			if (currentTemplate == null) {
				if (line.getRawText().startsWith("{{") && line.getRawText().endsWith("}}")) {
					currentTemplate = new Template();
					currentTemplate.addText(line.getRawText());
					currentSection.addTemplate(currentTemplate);
					boolean successful = parseTemplate(currentTemplate);
					if (!successful) {
						currentSection.getTemplates().remove(currentTemplate);
					} else
						hasQuotes = true;
					currentTemplate = null;
					continue;
				}
			}

			if (currentTemplate != null) {
				currentTemplate.addText(line.getRawText());

				if (line.getRawText().endsWith("}}")) {
					boolean successful = parseTemplate(currentTemplate);
					if (!successful) {
						currentSection.getTemplates().remove(currentTemplate);
					} else
						hasQuotes = true;
					currentTemplate = null;
				}
				continue;
			}

			if (line.getRawText().startsWith("=") || line.getRawText().startsWith(";")
					|| (line.getRawText().startsWith("'''") && lastSectionLevel != null)) {
				int level = 0;

				if (line.getRawText().startsWith("'''") || line.getRawText().startsWith("#")) {
					// ''' never starts a new sub section
					level = lastSectionLevel + 1;
				} else if (line.getRawText().startsWith("=")) {
					for (int i = 0; i <= 5; i++) {
						if (line.getRawText().charAt(i) != '=')
							break;
						level = i;
					}
					lastSectionLevel = level;
					// currentSectionPerLevel.put(level, section);
				} else {
					level = currentSection.getLevel() + 1;
				}

				if (numberOfThreads > 1)
					linesToParse.add(line);
				else
					parseWikiText(line);

				Section section = new Section(level, line);

				for (int i = level - 1; i >= 0; i--) {
					Section parentSection = currentSectionPerLevel.get(i);
					if (parentSection != null) {
						parentSection.addSubSection(section);
						break;
					}
				}

				currentSection = section;
				currentSectionPerLevel.put(level, section);

			} else if (line.getRawText().startsWith("{{")) {
				currentTemplate = new Template();
				currentTemplate.addText(line.getRawText());
				currentSection.addTemplate(currentTemplate);
			} else {
				hasQuotes = true;
				if (line.getRawText().startsWith("**") || line.getRawText().startsWith(":*")
						|| line.getRawText().startsWith(":")) {

					if (numberOfThreads > 1)
						linesToParse.add(line);
					else
						parseWikiText(line);

					if (currentLine == null)
						continue;

					currentLine.addSubLine(line);

					if (line.getRawText().startsWith("**"))
						line.setPrefix("**");
					else if (line.getRawText().startsWith(":*"))
						line.setPrefix(":*");
					else if (line.getRawText().startsWith(":"))
						line.setPrefix(":");

				} else if (line.getRawText().startsWith("*") || line.getRawText().startsWith("#")) {

					if (numberOfThreads > 1)
						linesToParse.add(line);
					else
						parseWikiText(line);

					String lineWithoutPrefix = line.getRawText().replaceAll("^\\*", "").replaceAll("^#", "").trim();

					if (lineWithoutPrefix.startsWith("'''"))
						line.setFormatting("bold");
					else if (lineWithoutPrefix.startsWith("''"))
						line.setFormatting("italic");

					if (line.getRawText().startsWith("*"))
						line.setPrefix(":");
					else if (line.getRawText().startsWith("#"))
						line.setPrefix("#");

					currentSection.addLine(line);
					currentLine = line;
				}
			}
		}

		if (numberOfThreads > 1) {
			List<CompletableFuture<Boolean>> cfs = new ArrayList<CompletableFuture<Boolean>>();
			for (Line line : linesToParse) {
				CompletableFuture<Boolean> cf = CompletableFuture.supplyAsync(() -> parseWikiText(line));
				cfs.add(cf);
			}

			// Iterable<List<CompletableFuture<Boolean>>> subSets = ListUtils.partition(cfs,
			// numberOfThreads);
			// for (List<CompletableFuture<Boolean>> subset : subSets) {
			for (CompletableFuture<Boolean> cf : cfs) {
				try {
					cf.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			// }
		}

		this.article.setHasQuotes(hasQuotes);

		if (printExampleEntity) {
			printSection(currentSectionPerLevel.get(0));
			// System.exit(0);
		}

		// if the article is not connected to a Wikidata ID, find the Wikipedia ID
		// linked in the title
		if (hasQuotes && this.article.getWikidataId() == null
				&& !currentSectionPerLevel.get(0).getSubSections().isEmpty()) {
			Section wikiSection = currentSectionPerLevel.get(0).getSubSections().get(0);
			if (wikiSection != null) {
				if (!wikiSection.getTitle().getLinks().isEmpty()) {
					String wikiId = wikiSection.getTitle().getLinks().get(0).getText();
					this.article.setWikipediaId(wikiId);
				}
			}
		}
	}

	private void printSection(Section section) {
		System.out.println(" ".repeat(section.getLevel()) + "Section: " + section.getTitle().getCleanText());

		for (Line line : section.getLines()) {
			System.out.println(
					" ".repeat(section.getLevel() + 1) + "Line: " + line.getCleanText() + " - " + line.getBoldParts());
			for (Link link : line.getLinks())
				System.out.println(
						" ".repeat(section.getLevel() + 2) + "Link: " + link.getText() + " (" + link.getPrefix() + ")");

			if (line.getReferences() != null)
				for (ExternalLink reference : line.getReferences())
					System.out.println(" ".repeat(section.getLevel() + 2) + "Ref: " + reference.getUrl());

			for (Line subLine : line.getSubLines()) {
				System.out.println(" ".repeat(section.getLevel() + 2) + "Sub Line (" + subLine.getPrefix() + "): "
						+ subLine.getCleanText());
			}

			for (Template template : line.getTemplates()) {
				System.out.println(" ".repeat(section.getLevel() + 2) + "Template (" + template.getType() + "): "
						+ template.getValues());
			}

			for (Footnote r : line.getFootnotes()) {
				System.out.println(" ".repeat(section.getLevel() + 2) + "Footnote (" + r.getText() + ")");
			}

		}

		for (Template template : section.getTemplates()) {
			System.out.println(" ".repeat(section.getLevel() + 1) + "Template (" + template.getType() + "): "
					+ template.getValues());
			for (String key : template.getTemplateValues().keySet()) {
				Template template2 = template.getTemplateValues().get(key);
				System.out.println(" ".repeat(section.getLevel() + 2) + key + " -> Template (" + template2.getType()
						+ "): " + template2.getValues());
			}
		}

		for (Section subSection : section.getSubSections()) {
//			System.out.println(
//					"---sub sec of " + section.getTitle().getCleanText() + ": " + subSection.getTitle().getCleanText());
			printSection(subSection);
		}
	}

	public boolean parseWikiText(Line line) {

		line.setRawText(line.getRawText().replace("<br>", "\n"));
		line.setRawText(line.getRawText().replace("<br />", "\n"));
		line.setRawText(line.getRawText().replace("<br/>", "\n"));

		// for efficiency: if the text has no Wiki text, do not use the parser
		if (!line.getRawText().contains("|") && !line.getRawText().contains("[") && !line.getRawText().contains("'")) {
			String cleanText = line.getRawText();
			cleanText = cleanText.replaceAll("^:*", "");
			cleanText = cleanText.replaceAll("^\\**", "");
			cleanText = cleanText.replaceAll("^=*", "");
			cleanText = cleanText.replaceAll("=*$", "");
			cleanText = cleanText.trim();
			cleanText = cleanText.replace("\n\n", "\n");
			line.setCleanText(cleanText);
			return true;
		}

		line.setRawText(line.getRawText().replace("'''", "@@@"));
		line.setRawText(line.getRawText().replace("''", "§§§"));

		Configuration conf = new Configuration();
		conf.getTokenMap().put("ref", new MyRefTag());

		MyWikiModel wikiModel = new MyWikiModel(conf, "${image}", "${title}");

		String plainStr;
		try {

			// System.out.println("parseWikiText: "+line.getRawText().trim());

			plainStr = wikiModel.render(new PlainTextConverter(true), line.getRawText()).trim();
			plainStr = plainStr.replace("\n\n", "\n");
//			System.out.println("Plain String: "+plainStr);
//			for(Template t: wikiModel.getTemplates())
//				System.out.println("tt: "+t.getType()+", "+t.getValues());

			line.setCleanText(plainStr);
			line.setLinks(wikiModel.getOrderedLinks());
			this.article.addLinks(line.getLinks());
			line.setReferences(wikiModel.getOrderedExternalLinks());
			line.setTemplates(wikiModel.getTemplates());

			for (Template t : line.getTemplates()) {
				processTemplate(t);
				line.setCleanText(line.getCleanText().replace(" " + t.getId(), ""));
				line.setCleanText(line.getCleanText().replace(t.getId(), ""));
			}

			if (wikiModel.getReferences() != null)
				for (Reference r : wikiModel.getReferences())
					if (r.getRefString() != null && !r.getRefString().isEmpty())
						line.addFootnote(new Footnote(r.getRefString().trim()));

			addFormatting(line);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private void addFormatting(Line line) {
		String restLine = line.getCleanText();
		String cleanText = line.getCleanText().replace("§§§", "").replace("@@@", "");
		line.setCleanText(cleanText);

		List<Integer> indexes = new ArrayList<Integer>();
		List<Boolean> formatTypes = new ArrayList<Boolean>();
		int offset = 0;
		while (restLine.contains("@@@") || restLine.contains("§§§")) {
			int idxBold = restLine.indexOf("@@@");
			int idxItalic = restLine.indexOf("§§§");

			boolean bold = true;
			if (idxBold == -1)
				bold = false;
			else if (idxItalic != -1 && idxItalic < idxBold)
				bold = false;

			int idx = idxBold;
			if (!bold)
				idx = idxItalic;

			restLine = restLine.substring(idx + 3);

			indexes.add(idx + offset);
			formatTypes.add(bold);

			offset += 3 + idx;
		}
		if (indexes.size() % 2 == 1)
			return;

		List<Integer> indexesClean = new ArrayList<Integer>();

		for (int i = 0; i < indexes.size(); i++) {
			indexesClean.add(indexes.get(i) - 3 * i);
		}

		List<Integer> indexesBold = new ArrayList<Integer>();
		List<Integer> indexesItalic = new ArrayList<Integer>();

		for (int i = 0; i < indexes.size(); i++) {
			if (formatTypes.get(i))
				indexesBold.add(indexesClean.get(i));
			else
				indexesItalic.add(indexesClean.get(i));
		}

		if (indexesBold.size() % 2 == 0)
			for (int i = 0; i < indexesBold.size(); i += 2) {
				line.addBoldPart(new int[] { indexesBold.get(i), indexesBold.get(i + 1) });
			}

		if (indexesItalic.size() % 2 == 0)
			for (int i = 0; i < indexesItalic.size(); i += 2) {
				line.addItalicPart(new int[] { indexesItalic.get(i), indexesItalic.get(i + 1) });
			}
	}

	public boolean parseTemplate(Template template) {

		template.setText(template.getText().replace("{{:", "{{"));

		String templateString = template.getText();

		MyWikiModel wikiModel = new MyWikiModel("${image}", "${title}");
		StringBuilder buf = new StringBuilder(templateString.length() + templateString.length() / 10);

		try {

			MyTemplateParser.parseRecursive(templateString, wikiModel, buf, false, true, null);
			// wikiModel.parseTemplates(templateString);

			if (wikiModel.getTemplate() == null)
				return false;

			// template = wikiModel.getTemplate();
			template.setType(wikiModel.getTemplate().getType());
			for (String key : wikiModel.getTemplate().getValues().keySet())
				template.addValues(key, wikiModel.getTemplate().getValues().get(key));

			processTemplate(template);

			for (Template t : wikiModel.getTemplates())
				processTemplate(t);

			for (String key : template.getValues().keySet()) {
				String value = template.getValues().get(key);

				if (wikiModel.getTemplateMap2().containsKey(value)) {
					template.getValues().remove(key);
					template.getTemplateValues().put(key, wikiModel.getTemplateMap2().get(value));
					continue;
				}
			}

		} catch (Exception e) {
			System.err.println("Error during template parsing: " + e.getMessage() + ". Skip.");
			return false;
		}

		return true;
	}

	private void processTemplate(Template template) {
		for (String key : template.getValues().keySet()) {
			String value = template.getValues().get(key);

			if (!value.isEmpty()) {
				Line line = new Line(value);
				parseWikiText(line);
				template.addParsedValue(key, line);
			} else {
				template.addParsedValue(key, null);
			}
		}

	}

}
