package mwdumper.articleprocessing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mwdumper.model.Link;
import mwdumper.model.Section;

public class Article {

	private String wikiquoteId;
	private Integer wikiquotePageId;

	private String text;

	private Integer wikidataId;
	private String wikipediaId;

	private Section mainSection;

	private boolean hasQuotes;

	private Set<String> types = new HashSet<String>();
	private Set<Link> links = new HashSet<Link>(); // all links in all sections of this article

	public Article(String wikiquoteId, Integer wikiquotePageId, Integer wikidataId) {
		super();
		this.wikiquoteId = wikiquoteId;
		this.wikiquotePageId = wikiquotePageId;
		this.wikidataId = wikidataId;
	}

	public String getWikiquoteId() {
		return wikiquoteId;
	}

	public void setWikiquoteId(String wikiquoteId) {
		this.wikiquoteId = wikiquoteId;
	}

	public int getWikiquotePageId() {
		return wikiquotePageId;
	}

	public void setWikiquotePageId(int wikiquotePageId) {
		this.wikiquotePageId = wikiquotePageId;
	}

	public String getWikidataIdQ() {
		if (wikidataId == null)
			return null;
		return "Q" + wikidataId;
	}

	public Integer getWikidataId() {
		return wikidataId;
	}

	public void setWikidataId(Integer wikidataId) {
		this.wikidataId = wikidataId;
	}

	public String getWikipediaId() {
		return wikipediaId;
	}

	public void setWikipediaId(String wikipediaId) {
		this.wikipediaId = wikipediaId;
	}

	public Section getMainSection() {
		return mainSection;
	}

	public void setMainSection(Section mainSection) {
		this.mainSection = mainSection;
	}

	public boolean hasQuotes() {
		return hasQuotes;
	}

	public void setHasQuotes(boolean hasQuotes) {
		this.hasQuotes = hasQuotes;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Set<String> getTypes() {
		return types;
	}

	public void setTypes(Set<String> types) {
		this.types = types;
	}

	public void addType(String type) {
		this.types.add(type);
	}

	public Set<Link> getLinks() {
		return links;
	}

	public void setLinks(Set<Link> links) {
		this.links = links;
	}

	public void addLinks(List<Link> links) {
		this.links.addAll(links);
	}

}
