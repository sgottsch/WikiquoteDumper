package mwdumper.model;

import java.util.ArrayList;
import java.util.List;

import de.l3s.cleopatra.quotekg.model.Footnote;

public class Line {

	private String rawText;
	private String cleanText;

	private List<Line> subLines = new ArrayList<Line>();

	private String prefix;
	private String formatting;

	private List<Link> links = new ArrayList<Link>();
	private List<ExternalLink> references = new ArrayList<ExternalLink>();
	private List<Template> templates = new ArrayList<Template>();
	private List<Footnote> footnotes = new ArrayList<Footnote>();

	private List<int[]> boldParts = new ArrayList<int[]>();
	private List<int[]> italicParts = new ArrayList<int[]>();

	public Line(String rawText) {
		super();
		this.rawText = rawText.trim();
	}

	public Line() {

	}

	public Line(String rawText, String cleanText) {
		super();
		this.rawText = rawText.trim();
		this.cleanText = cleanText;

	}

	public String getRawText() {
		return rawText;
	}

	public List<Line> getSubLines() {
		return subLines;
	}

	public void addSubLine(Line subLine) {
		this.subLines.add(subLine);
	}

	public String getCleanText() {
		return cleanText;
	}

	public void setCleanText(String cleanText) {
		this.cleanText = cleanText;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	public void addLink(Link link) {
		this.links.add(link);
	}

	public List<ExternalLink> getReferences() {
		return references;
	}

	public void setReferences(List<ExternalLink> references) {
		this.references = references;
	}

	public void addReference(ExternalLink reference) {
		this.references.add(reference);
	}

	public List<Template> getTemplates() {
		return templates;
	}

	public void setTemplates(List<Template> templates) {
		this.templates = templates;
	}

	public List<Footnote> getFootnotes() {
		return footnotes;
	}

	public void setFootnotes(List<Footnote> footnotes) {
		this.footnotes = footnotes;
	}

	public void addFootnote(Footnote footnote) {
		this.footnotes.add(footnote);
	}

	public String getFormatting() {
		return formatting;
	}

	public void setFormatting(String formatting) {
		this.formatting = formatting;
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

	public List<int[]> getBoldParts() {
		return boldParts;
	}

	public void setBoldParts(List<int[]> boldParts) {
		this.boldParts = boldParts;
	}

	public List<int[]> getItalicParts() {
		return italicParts;
	}

	public void setItalicParts(List<int[]> italicParts) {
		this.italicParts = italicParts;
	}

	public void addItalicPart(int[] italicPart) {
		this.italicParts.add(italicPart);
	}

	public void addBoldPart(int[] boldPart) {
		this.boldParts.add(boldPart);
	}

}
