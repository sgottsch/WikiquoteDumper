package mwdumper.model;

public class ExternalLink {

	private String anchorText;
	private String url;

	public ExternalLink(String anchorText, String url) {
		super();
		this.anchorText = anchorText;
		this.url = url;
	}

	public String getAnchorText() {
		return anchorText;
	}

	public void setAnchorText(String anchorText) {
		this.anchorText = anchorText;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
