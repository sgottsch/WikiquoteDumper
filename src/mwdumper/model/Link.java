package mwdumper.model;

import mwdumper.articleprocessing.Article;

public class Link {

	private String prefix;
	private String anchorText;
	private String text;

	private Article article;

	public Link() {

	}

	public Link(String prefix, String anchorText, String text) {
		super();
		this.prefix = prefix;
		this.anchorText = anchorText;
		this.text = text;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getAnchorText() {
		return anchorText;
	}

	public void setAnchorText(String anchorText) {
		this.anchorText = anchorText;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

}
