package de.l3s.cleopatra.quotekg.data;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import de.l3s.cleopatra.quotekg.model.Footnote;
import de.l3s.cleopatra.quotekg.model.Language;
import mwdumper.articleprocessing.Article;
import mwdumper.model.ExternalLink;
import mwdumper.model.Line;
import mwdumper.model.Link;
import mwdumper.model.Section;
import mwdumper.model.Template;

public class WikiquoteToJSONWriter {

	public static void writeJSONs(DataStorage dataStorage, String outputFileName, String typesFileName,
			String idsFileName, Language language) {

		WikidataWikipediaIDMap wikipediaWikidataIDMap = new WikidataWikipediaIDMap();
		wikipediaWikidataIDMap.load(dataStorage, idsFileName);

		for (Article article : dataStorage.getArticles()) {
			updateArticleIds(article, wikipediaWikidataIDMap);

			if (article.getWikidataId() != null)
				dataStorage.getArticlesByWikidataId().put(article.getWikidataId(), article);
			if (article.getWikipediaId() != null)
				dataStorage.getArticlesByWikipediaId().put(article.getWikipediaId(), article);
			if (article.getWikiquoteId() != null)
				dataStorage.getArticlesByWikiquoteId().put(article.getWikiquoteId(), article);
		}

		for (Article article : dataStorage.getArticles()) {
			updateArticleLinkIds(article, dataStorage, language, wikipediaWikidataIDMap);
		}

		TypesAdder.addTypesToArticles(dataStorage, typesFileName);

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputFileName);
			for (Article article : dataStorage.getArticles()) {
				// ignore Wikiquote main page (e.g., https://hr.wikiquote.org)
				if (article.getWikidataId() != null && article.getWikidataId() == 5296)
					continue;

				if (article.hasQuotes()) {
					JSONObject articleJSON = createArticleJSON(article);
					writer.println(articleJSON);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}

	private static void updateArticleLinkIds(Article article, DataStorage dataStorage, Language language,
			WikidataWikipediaIDMap wikipediaWikidataIDMap) {
		System.out.println("updateArticleLinkIds");

		for (Link link : article.getLinks()) {
			
			link.setArticle(dataStorage.getArticlesByWikiquoteId().get(link.getText()));

			if (link.getArticle() == null && link.getPrefix() != null && link.getPrefix().equals("w")) {
				Article linkArticle = dataStorage.getArticlesByWikipediaId().get(link.getText());
				if (linkArticle == null) {
					Integer wikidataId = wikipediaWikidataIDMap.getWikidataIdByWikipediaId(link.getText());
					if (wikidataId == null)
						continue;
					linkArticle = dataStorage.getArticlesByWikidataId().get(wikidataId);
					if (linkArticle == null) {
						linkArticle = new Article(null, null, wikidataId);
						dataStorage.getArticlesByWikidataId().put(wikidataId, linkArticle);
						dataStorage.getArticlesByWikipediaId().put(link.getText(), linkArticle);
					}
				}
				link.setArticle(linkArticle);
			}
		}
	}

	private static void updateArticleIds(Article article, WikidataWikipediaIDMap wikipediaWikidataIDMap) {

		if (article.getWikidataId() == null && article.getWikipediaId() != null) {
			article.setWikidataId(wikipediaWikidataIDMap.getWikidataIdByWikipediaId(article.getWikipediaId()));
		}

	}

	private static JSONObject createArticleJSON(Article article) {

		JSONObject articleJSON = new JSONObject();

		articleJSON.put("wikiquoteId", article.getWikiquoteId());
		articleJSON.put("wikiquotePageId", article.getWikiquotePageId());

		if (article.getWikidataId() != null)
			articleJSON.put("wikidataId", article.getWikidataIdQ());
		if (article.getWikipediaId() != null)
			articleJSON.put("wikipediaId", article.getWikipediaId());

		if (!article.getTypes().isEmpty()) {
			JSONArray typesJSON = new JSONArray();
			for (String type : article.getTypes())
				typesJSON.put(type);
			articleJSON.put("types", typesJSON);
		}

		if (article.getMainSection() != null) {
			articleJSON.put("sections", new JSONArray());
			addSectionJSON(articleJSON, article.getMainSection());
		}

		return articleJSON;
	}

	private static void addSectionJSON(JSONObject parentJSON, Section section) {

		JSONObject sectionJSON = new JSONObject();
		parentJSON.getJSONArray("sections").put(sectionJSON);

		if (section.getTitle() != null)
			sectionJSON.put("title", createLineJONObject(section.getTitle()));

		if (section.getIsChronological() != null && section.getIsChronological() == true) {
			sectionJSON.put("chronological", true);
		}

		if (!section.getLines().isEmpty()) {
			JSONArray lines = new JSONArray();
			sectionJSON.put("lines", lines);
			for (Line line : section.getLines()) {
				JSONObject lineJSON = createLineJONObject(line);
				lines.put(lineJSON);
			}
		}

		if (!section.getTemplates().isEmpty()) {
			JSONArray templates = new JSONArray();
			sectionJSON.put("templates", templates);
			for (Template template : section.getTemplates()) {
				JSONObject templateJSON = createTemplateJSONObject(template);
				templates.put(templateJSON);
			}
		}

		if (!section.getSubSections().isEmpty()) {
			sectionJSON.put("sections", new JSONArray());
			for (Section subSection : section.getSubSections()) {
				addSectionJSON(sectionJSON, subSection);
			}
		}

	}

	private static JSONObject createTemplateJSONObject(Template template) {

		JSONObject templateJSON = new JSONObject();
		templateJSON.put("type", template.getType());

		JSONArray emptyTemplateValuesJSON = new JSONArray();
		JSONObject templateValuesJSON = new JSONObject();
		JSONObject templateTemplateValuesJSON = new JSONObject();

		for (String key : template.getParsedValues().keySet()) {
			Line value = template.getParsedValues().get(key);
			if (value == null)
				emptyTemplateValuesJSON.put(key);
			else
				templateValuesJSON.put(key, createLineJONObject(value));
		}

		for (String key : template.getTemplateValues().keySet()) {
			templateTemplateValuesJSON.put(key, createTemplateJSONObject(template.getTemplateValues().get(key)));
		}

		if (emptyTemplateValuesJSON.length() != 0)
			templateJSON.put("emptyValues", emptyTemplateValuesJSON);
		if (!templateTemplateValuesJSON.keySet().isEmpty())
			templateJSON.put("templateValues", templateTemplateValuesJSON);
		if (!templateValuesJSON.keySet().isEmpty())
			templateJSON.put("values", templateValuesJSON);

		return templateJSON;
	}

	private static JSONObject createLineJONObject(Line line) {
		JSONObject lineJSON = new JSONObject();
		if (line.getCleanText() != null)
			lineJSON.put("text", line.getCleanText());
//		if (line.getRawText() != null)
//			lineJSON.put("wikiText", line.getRawText());
		if (line.getPrefix() != null)
			lineJSON.put("prefix", line.getPrefix());
		if (line.getFormatting() != null)
			lineJSON.put("format", line.getFormatting());
		JSONArray subLinesArray = new JSONArray();

		if (!line.getLinks().isEmpty()) {
			JSONArray linksJSONArray = new JSONArray();
			for (Link link : line.getLinks()) {
				linksJSONArray.put(createLinkJSONObject(link));
			}
			lineJSON.put("links", linksJSONArray);
		}

		if (!line.getReferences().isEmpty()) {
			JSONArray externalLinksJSONArray = new JSONArray();
			for (ExternalLink link : line.getReferences()) {
				externalLinksJSONArray.put(createExternalLinkJSONObject(link));
			}
			lineJSON.put("externalLinks", externalLinksJSONArray);
		}

		if (!line.getFootnotes().isEmpty()) {
			JSONArray footnotesJSONArray = new JSONArray();
			for (Footnote footnote : line.getFootnotes()) {
				footnotesJSONArray.put(createFootnoteJSONObject(footnote));
			}
			lineJSON.put("footnotes", footnotesJSONArray);
		}

		if (!line.getTemplates().isEmpty()) {
			JSONArray templatesJSONArray = new JSONArray();
			for (Template template : line.getTemplates()) {
				templatesJSONArray.put(createTemplateJSONObject(template));
			}
			lineJSON.put("templates", templatesJSONArray);
		}

		if (!line.getSubLines().isEmpty()) {
			lineJSON.put("subLines", subLinesArray);
			for (Line subLine : line.getSubLines()) {
				subLinesArray.put(createLineJONObject(subLine));
			}
		}

		if (!line.getItalicParts().isEmpty()) {
			JSONArray italicParts = new JSONArray();
			lineJSON.put("italic", italicParts);
			for (int[] italicPart : line.getItalicParts()) {
				JSONArray italicPartJSON = new JSONArray();
				italicPartJSON.put(italicPart[0]);
				italicPartJSON.put(italicPart[1]);
				italicParts.put(italicPartJSON);
			}
		}

		if (!line.getBoldParts().isEmpty()) {
			JSONArray boldParts = new JSONArray();
			lineJSON.put("bold", boldParts);
			for (int[] boldPart : line.getBoldParts()) {
				JSONArray boldPartJSON = new JSONArray();
				boldPartJSON.put(boldPart[0]);
				boldPartJSON.put(boldPart[1]);
				boldParts.put(boldPartJSON);
			}
		}

		return lineJSON;
	}

	private static JSONObject createLinkJSONObject(Link link) {
		JSONObject jsonLink = new JSONObject();
		jsonLink.put("wikiquoteId", link.getText());
		jsonLink.put("text", link.getAnchorText());
		if (link.getPrefix() != null && !link.getPrefix().isEmpty())
			jsonLink.put("prefix", link.getPrefix());

		Article article = link.getArticle();

		if (article != null) {
			if (article.getWikidataId() != null)
				jsonLink.put("wikidataId", article.getWikidataIdQ());
			if (article.getWikipediaId() != null)
				jsonLink.put("wikipediaId", article.getWikipediaId());

			if (!article.getTypes().isEmpty()) {
				JSONArray typesJSON = new JSONArray();
				for (String type : article.getTypes())
					typesJSON.put(type);
				jsonLink.put("types", typesJSON);
			}
		}

		return jsonLink;
	}

	private static JSONObject createExternalLinkJSONObject(ExternalLink link) {
		JSONObject jsonLink = new JSONObject();
		jsonLink.put("link", link.getUrl());

		if (link.getAnchorText() != null)
			jsonLink.put("text", link.getAnchorText());

		return jsonLink;
	}

	private static JSONObject createFootnoteJSONObject(Footnote footnote) {
		JSONObject jsonFootnote = new JSONObject();
		jsonFootnote.put("text", footnote.getText());
		return jsonFootnote;
	}

}
