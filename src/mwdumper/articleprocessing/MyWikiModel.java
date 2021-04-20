package mwdumper.articleprocessing;

import static info.bliki.wiki.model.IConfiguration.Casing.FirstLetter;
import static info.bliki.wiki.tags.WPATag.ANCHOR;
import static info.bliki.wiki.tags.WPATag.CLASS;
import static info.bliki.wiki.tags.WPATag.HREF;
import static info.bliki.wiki.tags.WPATag.TITLE;
import static info.bliki.wiki.tags.WPATag.WIKILINK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import info.bliki.Messages;
import info.bliki.htmlcleaner.ContentToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.TagToken;
import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.Encoder;
import info.bliki.wiki.filter.HTMLConverter;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.ParsedPageName;
import info.bliki.wiki.filter.TemplateParser;
import info.bliki.wiki.filter.WikipediaParser;
import info.bliki.wiki.filter.WikipediaPreTagParser;
import info.bliki.wiki.model.AbstractWikiModel;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.Counter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.model.SemanticAttribute;
import info.bliki.wiki.model.SemanticRelation;
import info.bliki.wiki.model.WikiModelContentException;
import info.bliki.wiki.namespaces.INamespace;
import info.bliki.wiki.namespaces.INamespace.NamespaceCode;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.PTag;
import info.bliki.wiki.tags.WPATag;
import mwdumper.model.ExternalLink;
import mwdumper.model.Link;
import mwdumper.model.Template;

/**
 * Standard model implementation.
 */
public class MyWikiModel extends AbstractWikiModel {
	/**
	 * A map for categories and their associated sort keys
	 */
	protected Map<String, String> categories;
//	protected Set<String> templates = new HashSet<String>();
	protected Set<String> includes;
	protected List<SemanticRelation> semanticRelations;
	protected List<SemanticAttribute> semanticAttributes;

	private String fExternalImageBaseURL;
	private String fExternalWikiBaseURL;
	private Set<String> links; // unused
	private List<Link> orderedLinks = new ArrayList<Link>();
	private List<ExternalLink> orderedExternalLinks = new ArrayList<ExternalLink>();

	private List<Template> templates = new ArrayList<Template>();

	private int templateId = 0;
	private Map<String, Template> templateMap = new HashMap<String, Template>();

	/**
	 * @param imageBaseURL a url string which must contains a &quot;${image}&quot;
	 *                     variable which will be replaced by the image name, to
	 *                     create links to images.
	 * @param linkBaseURL  a url string which must contains a &quot;${title}&quot;
	 *                     variable which will be replaced by the topic title, to
	 *                     create links to other wiki topics.
	 */
	public MyWikiModel(String imageBaseURL, String linkBaseURL) {
		this(new Configuration(), imageBaseURL, linkBaseURL);
	}

	public MyWikiModel(Configuration configuration, String imageBaseURL, String linkBaseURL) {
		super(configuration);
		fExternalImageBaseURL = imageBaseURL;
		fExternalWikiBaseURL = linkBaseURL;
	}

	public MyWikiModel(Configuration configuration, Locale locale, String imageBaseURL, String linkBaseURL) {
		super(configuration, locale);
		fExternalImageBaseURL = imageBaseURL;
		fExternalWikiBaseURL = linkBaseURL;
	}

	public MyWikiModel(Configuration configuration, Locale locale, INamespace namespace, String imageBaseURL,
			String linkBaseURL) {
		super(configuration, locale, namespace);
		fExternalImageBaseURL = imageBaseURL;
		fExternalWikiBaseURL = linkBaseURL;
	}

	@Override
	public void addCategory(String categoryName, String sortKey) {
		categories.put(categoryName, sortKey);
	}

	@Override
	public void addLink(String topicName) {
		links.add(topicName);
	}

	@Override
	public boolean addSemanticAttribute(String attribute, String attributeValue) {
		if (semanticAttributes == null) {
			semanticAttributes = new ArrayList<>();
		}
		semanticAttributes.add(new SemanticAttribute(attribute, attributeValue));
		return true;
	}

	@Override
	public boolean addSemanticRelation(String relation, String relationValue) {
		if (semanticRelations == null) {
			semanticRelations = new ArrayList<>();
		}
		semanticRelations.add(new SemanticRelation(relation, relationValue));
		return true;
	}

//	@Override
//	public void addTemplate(String template) {
//		templates.add(template);
//	}

	@Override
	public void addInclude(String pageName) {
		includes.add(pageName);
	}

	@Override
	public void appendInternalLink(String topic, String hashSection, String topicDescription, String cssClass,
			boolean parseRecursive) {
		appendInternalLink(topic, hashSection, topicDescription, cssClass, parseRecursive, true);
	}

	protected void appendInternalLink(final String topic, final String hashSection, final String topicDescription,
			String cssClass, boolean parseRecursive, boolean topicExists) {

		String hrefLink;
		String description = topicDescription.trim();
		WPATag aTagNode = new WPATag();

		if (topic.length() > 0) {
			String title = Encoder.normaliseTitle(topic, true, ' ', casing() == FirstLetter);
			if (hashSection == null) {
				String pageName = Encoder.normaliseTitle(fPageTitle, true, ' ', true);
				// self link?
				if (title.equals(pageName)) {
					HTMLTag selfLink = new HTMLTag("strong");
					selfLink.addAttribute("class", "selflink", false);
					pushNode(selfLink);
					selfLink.addChild(new ContentToken(description));
					popNode();
					return;
				}
			}

			String encodedTopic = encodeTitleToUrl(topic, casing() == FirstLetter);
			if (replaceColon()) {
				encodedTopic = encodedTopic.replace(':', '/');
			}
			hrefLink = getWikiBaseURL().replace("${title}", encodedTopic);
			if (!topicExists) {
				if (cssClass == null) {
					cssClass = "new";
				}
				if (hrefLink.indexOf('?') != -1) {
					hrefLink += "&";
				} else {
					hrefLink += "?";
				}
				hrefLink += "action=edit&redlink=1";
				String redlinkString = Messages.getString(getResourceBundle(), Messages.WIKI_TAGS_RED_LINK,
						"${title} (page does not exist)");
				title = redlinkString.replace("${title}", title);
			}
			aTagNode.addAttribute(TITLE, title, true);
		} else {
			// assume, the own topic exists
			if (hashSection != null) {
				hrefLink = "";
				if (description.length() == 0) {
					description = "&#35;" + hashSection; // #....
				}
			} else {
				hrefLink = getWikiBaseURL().replace("${title}", "");
			}
		}

		String href = hrefLink;
		if (topicExists && hashSection != null) {
			aTagNode.addObjectAttribute(ANCHOR, hashSection);
			href = href + '#' + encodeTitleDotUrl(hashSection, false);
		}
		aTagNode.addAttribute(HREF, href, true);
		if (cssClass != null) {
			aTagNode.addAttribute(CLASS, cssClass, true);
		}
		aTagNode.addObjectAttribute(WIKILINK, topic);

		pushNode(aTagNode);
		if (parseRecursive) {
			WikipediaPreTagParser.parseRecursive(description, this, false, true);
		} else {
			aTagNode.addChild(new ContentToken(description));
		}
		popNode();

		this.orderedLinks.add(new Link(null, topicDescription, topic));
	}

	// @Override
	public Map<String, String> getCategories() {
		return categories;
	}

	@Override
	public Set<String> getLinks() {
		return links;
	}

	public List<Link> getOrderedLinks() {
		return orderedLinks;
	}

	@Override
	public List<SemanticAttribute> getSemanticAttributes() {
		return semanticAttributes;
	}

	@Override
	public List<SemanticRelation> getSemanticRelations() {
		return semanticRelations;
	}

//	/**
//	 * Gets the names of all included pages in the template namespace.
//	 *
//	 * @return page names without the template namespace prefix
//	 */
//	public Set<String> getTemplates() {
//		return templates;
//	}

	/**
	 * Gets the names of all included pages outside the template namespace.
	 *
	 * @return page names including their namespace prefix
	 */
	public Set<String> getIncludes() {
		return includes;
	}

	/**
	 * Append the internal wiki image link to this model.
	 *
	 * <br/>
	 * <br/>
	 * <b>Note</b>: the pipe symbol (i.e. &quot;|&quot;) splits the
	 * <code>rawImageLink</code> into different segments. The first segment is used
	 * as the <code>&lt;image-name&gt;</code> and typically ends with extensions
	 * like <code>.png</code>, <code>.gif</code>, <code>.jpg</code> or
	 * <code>.jpeg</code>.
	 *
	 * <br/>
	 * <br/>
	 * <b>Note</b>: if the image link contains a "width" attribute, the filename is
	 * constructed as <code>&lt;size&gt;px-&lt;image-name&gt;</code>, otherwise it's
	 * only the <code>&lt;image-name&gt;</code>.
	 *
	 * <br/>
	 * <br/>
	 * See <a href="https://en.wikipedia.org/wiki/Image_markup">Image markup</a> and
	 * see <a href="https://www.mediawiki.org/wiki/Help:Images">Help:Images</a>
	 *
	 * @param imageNamespace the image namespace
	 * @param rawImageLink   the raw image link text without the surrounding
	 *                       <code>[[...]]</code>
	 */
	@Override
	public void parseInternalImageLink(String imageNamespace, String rawImageLink) {
		String imageSrc = getImageBaseURL();
		if (imageSrc != null) {
			String imageHref = getWikiBaseURL();
			ImageFormat imageFormat = ImageFormat.getImageFormat(rawImageLink, imageNamespace);

			String imageName = createImageName(imageFormat);
			String link = imageFormat.getLink();
			if (link != null) {
				if (link.length() == 0) {
					imageHref = "";
				} else {
					String encodedTitle = encodeTitleToUrl(link, true);
					imageHref = imageHref.replace("${title}", encodedTitle);
				}

			} else {
				if (replaceColon()) {
					imageHref = imageHref.replace("${title}", imageNamespace + '/' + imageName);
				} else {
					imageHref = imageHref.replace("${title}", imageNamespace + ':' + imageName);
				}
			}
			imageSrc = imageSrc.replace("${image}", imageName);
			String type = imageFormat.getType();
			TagToken tag = null;
			if ("thumb".equals(type) || "frame".equals(type)) {
				if (fTagStack.size() > 0) {
					tag = peekNode();
				}
				reduceTokenStack(Configuration.HTML_DIV_OPEN);

			}
			appendInternalImageLink(imageHref, imageSrc, imageFormat);
			if (tag instanceof PTag) {
				pushNode(new PTag());
			}
		}
	}

	protected String createImageName(ImageFormat imageFormat) {
		String imageName = imageFormat.getFilename();
		String sizeStr = imageFormat.getWidthStr();
		if (sizeStr != null) {
			imageName = sizeStr + '-' + imageName;
		}
		if (imageName.endsWith(".svg")) {
			imageName += ".png";
		}
		imageName = Encoder.encodeUrl(imageName);
		if (replaceColon()) {
			imageName = imageName.replace(':', '/');
		}
		return imageName;
	}

	@Override
	public boolean replaceColon() {
		return false;
	}

	@Override
	public void setUp() {
		super.setUp();
		categories = new HashMap<>();
		links = new HashSet<>();
		templates = new ArrayList<>();
		includes = new HashSet<>();
		semanticRelations = null;
		semanticAttributes = null;
	}

	/**
	 *
	 */
	@Override
	public INamespace getNamespace() {
		return fNamespace;
	}

	/**
	 * Convert a given text in wiki notation into another format.
	 *
	 * @param model          a wiki model
	 * @param converter      a text converter. <b>Note</b> the converter may be
	 *                       <code>null</code>, if you only would like to analyze
	 *                       the raw wiki text and don't need to convert. This
	 *                       speeds up the parsing process.
	 * @param rawWikiText    a raw wiki text
	 * @param resultBuffer   the buffer to which to append the resulting HTML code.
	 * @param templateTopic  if <code>true</code>, render the wiki text as if a
	 *                       template topic will be displayed directly, otherwise
	 *                       render the text as if a common wiki topic will be
	 *                       displayed.
	 * @param parseTemplates parses the template expansion step (parses include,
	 *                       onlyinclude, includeonly etc)
	 * @throws IOException
	 */
	public static void toText(IWikiModel model, ITextConverter converter, String rawWikiText, Appendable resultBuffer,
			boolean templateTopic, boolean parseTemplates) throws IOException {
		model.render(converter, rawWikiText, resultBuffer, templateTopic, parseTemplates);
	}

	/**
	 * Convert a given text in wiki notation into HTML text.
	 *
	 * @param rawWikiText  a raw wiki text
	 * @param resultBuffer the buffer to which to append the resulting HTML code.
	 * @param imageBaseURL a url string which must contains a &quot;${image}&quot;
	 *                     variable which will be replaced by the image name, to
	 *                     create links to images.
	 * @param linkBaseURL  a url string which must contains a &quot;${title}&quot;
	 *                     variable which will be replaced by the topic title, to
	 *                     create links to other wiki topics.
	 * @throws IOException
	 */
	public static void toHtml(String rawWikiText, Appendable resultBuffer, String imageBaseURL, String linkBaseURL)
			throws IOException {
		toText(new MyWikiModel(imageBaseURL, linkBaseURL), new HTMLConverter(), rawWikiText, resultBuffer, false,
				false);
	}

	/**
	 * Convert a given text in wiki notation into HTML text.
	 *
	 * @param rawWikiText  a raw wiki text
	 * @param resultBuffer the buffer to which to append the resulting HTML code.
	 * @throws IOException
	 */
	public static void toHtml(String rawWikiText, Appendable resultBuffer) throws IOException {
		toText(new MyWikiModel("/${image}", "/${title}"), new HTMLConverter(), rawWikiText, resultBuffer, false, false);
	}

	/**
	 * Convert a given text in wiki notation into HTML text.
	 *
	 * @param rawWikiText a raw wiki text
	 * @return the resulting HTML text; nay returns <code>null</code>, if an
	 *         <code>IOException</code> occured.
	 */
	public static String toHtml(String rawWikiText) {
		try {
			StringBuilder resultBuffer = new StringBuilder(rawWikiText.length() + rawWikiText.length() / 10);
			toText(new MyWikiModel("/${image}", "/${title}"), new HTMLConverter(), rawWikiText, resultBuffer, false,
					false);
			return resultBuffer.toString();
		} catch (IOException ignored) {
		}
		return null;
	}

	@Override
	public String getImageBaseURL() {
		return fExternalImageBaseURL;
	}

	@Override
	public String getWikiBaseURL() {
		return fExternalWikiBaseURL;
	}

	/**
	 * Append an external link (starting with http, https, ftp,...) as described in
	 * <a href="http://en.wikipedia.org/wiki/Help:Link#External_links">Help
	 * Links</a>
	 *
	 * @param uriSchemeName         the top level URI (Uniform Resource Identifier)
	 *                              scheme name (without the following colon
	 *                              character ":"). Example "ftp", "http", "https".
	 *                              See <a href=
	 *                              "http://en.wikipedia.org/wiki/URI_scheme">URI
	 *                              scheme</a>
	 * @param link                  the external link with
	 *                              <code>http://, https:// or ftp://</code> prefix
	 * @param linkName              the link name which is separated from the URL by
	 *                              a space
	 * @param withoutSquareBrackets if <code>true</code> a link with no square
	 *                              brackets around the link was pFsarsed
	 */
	@Override
	public void appendExternalLink(String uriSchemeName, String link, String linkName, boolean withoutSquareBrackets) {
		link = Utils.escapeXml(link, true, false, false);
		TagNode aTagNode = new TagNode("a");
		aTagNode.addAttribute("href", link, true);
		aTagNode.addAttribute("rel", "nofollow", true);
		if (withoutSquareBrackets) {
			aTagNode.addAttribute("class", "external free", true);
			append(aTagNode);
			aTagNode.addChild(new ContentToken(linkName));
		} else {
			String trimmedText = linkName.trim();
			if (trimmedText.length() > 0) {
				pushNode(aTagNode);
				if (linkName.equals(link)
						// protocol-relative URLs also get auto-numbered if there is no
						// real
						// alias
						|| (link.length() >= 2 && link.charAt(0) == '/' && link.charAt(1) == '/'
								&& link.substring(2).equals(linkName))) {
					aTagNode.addAttribute("class", "external autonumber", true);
					aTagNode.addChild(new ContentToken("[" + (++fExternalLinksCounter) + "]"));
				} else {
					aTagNode.addAttribute("class", "external text", true);
					WikipediaParser.parseRecursive(trimmedText, this, false, true);
				}
				popNode();
			}
		}

		this.orderedExternalLinks.add(new ExternalLink(linkName, link));
	}

	@Override
	public void appendInterWikiLink(String namespace, String title, String linkText) {
		String hrefLink = "#";

		this.orderedLinks.add(new Link(namespace, linkText, title));

		TagNode aTagNode = new TagNode("a");
		aTagNode.addAttribute("href", hrefLink, true);
		pushNode(aTagNode);
		WikipediaParser.parseRecursive(linkText.trim(), this, false, true);
		popNode();
	}

	public List<ExternalLink> getOrderedExternalLinks() {
		return orderedExternalLinks;
	}

//	@Override
//	public void substituteTemplateCall(String templateName, Map<String, String> parameterMap, Appendable writer)
//			throws IOException {
//		Template template = new Template();
//
//		template.setType(templateName);
//		template.setValues(parameterMap);
//		this.templates.add(template);
//		 super.substituteTemplateCall(templateName, parameterMap, writer);
//	}

	/**
	 * Substitute the template name by the template content and parameters and
	 * append the new content to the writer.
	 *
	 * @param templateName the name of the template
	 * @param parameterMap the templates parameter <code>java.util.SortedMap</code>
	 * @param writer       the buffer to append the substituted template content
	 * @throws IOException
	 */
	@Override
	public void substituteTemplateCall(String templateName, Map<String, String> parameterMap, Appendable writer)
			throws IOException {

		Template template = new Template();

		Counter val = null;
		try {
			ParsedPageName parsedPagename = ParsedPageName.parsePageName(this, templateName, fNamespace.getTemplate(),
					true, true);
			if (!parsedPagename.valid) {
				writer.append("{{");
				writer.append(templateName);
				writer.append("}}");
				return;
			}
			String fullTemplateStr = parsedPagename.fullPagename();

			val = fTemplates.get(fullTemplateStr);
			if (val == null) {
				val = new Counter(0);
				fTemplates.put(fullTemplateStr, val);
			}
			if (val.inc() > 1) {
				writer.append("<span class=\"error\">Template loop detected: <strong class=\"selflink\">")
						.append(fullTemplateStr).append("</strong></span>");
				return;
			}

			if (parsedPagename.namespace.isType(NamespaceCode.TEMPLATE_NAMESPACE_KEY)) {
				if (isParameterParsingMode() && templateName.equals("!") && parameterMap.isEmpty()) {
					writer.append("{{").append(templateName).append("}}");
					return;
				}
				addTemplate(parsedPagename.pagename);
			} else {
				addInclude(fullTemplateStr);
				// invalidate cache:
			}

			String plainContent;
			try {
				plainContent = getRawWikiContent(parsedPagename, parameterMap);
			} catch (WikiModelContentException wme) {
				writer.append(wme.getMessage());
				return;
			}
			if (plainContent == null) {
				this.templateId += 1;
				// content of this transclusion is missing => render as link:
				plainContent = "[[:" + fullTemplateStr;
				plainContent += "]]" + this.templateId;

				// create unique identifier respresenting the template
				template.setId(fullTemplateStr + this.templateId);
				templateMap.put(plainContent, template);
			}

			StringBuilder templateBuffer = new StringBuilder(plainContent.length());
			TemplateParser.parseRecursive(plainContent.trim(), this, templateBuffer, false, false, parameterMap);

			writer.append(templateBuffer);
		} finally {
			if (val != null) {
				val.dec();
			}
		}

		template.setType(templateName);
		template.setValues(parameterMap);
		this.templates.add(template);

	}

	public List<Template> getTemplates() {
		return templates;
	}

	public Template getTemplate() {
		if (templates.size() == 0)
			return null;
		return templates.get(templates.size() - 1);
	}

	public Map<String, Template> getTemplateMap2() {
		return templateMap;
	}

}
