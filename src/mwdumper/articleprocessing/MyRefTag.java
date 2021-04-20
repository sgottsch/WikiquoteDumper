package mwdumper.articleprocessing;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.filter.Encoder;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.PlainTextConvertable;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.IBodyTag;

public class MyRefTag extends HTMLTag implements IBodyTag {

	public MyRefTag() {
		super("ref");
	}

	@Override
	public void renderHTML(ITextConverter converter, Appendable writer, IWikiModel model) throws IOException {

		TagNode node = this;
		List<Object> children = node.getChildren();
		int len = children.size();
		len = len == 0 ? 16 : len * 64;
		StringBuilder buf = new StringBuilder(len);
		renderHTMLWithoutTag(converter, buf, model);
		Map<String, String> map = getAttributes();
		String value = map.get("name");
		String reference = buf.toString();
		String[] offset = model.addToReferences(reference, value);
		if (null == value) {
			value = offset[0];
		}
		String ref = (null == offset[1]) ? offset[0] : offset[1];

		writer.append("<sup id=\"_ref-");
		writer.append(Encoder.encodeDotUrl(ref));
		writer.append("\" class=\"reference\"><a href=\"#_note-");
		writer.append(Encoder.encodeDotUrl(value));
		writer.append("\" title=\"\">[");
		writer.append(offset[0]);
		writer.append("]</a></sup>");
	}

	@Override
	public boolean isReduceTokenStack() {
		return false;
	}

	@Override
	public void renderPlainText(ITextConverter converter, Appendable buf, IWikiModel wikiModel) throws IOException {

		StringBuffer sbuf = new StringBuffer();
		List<Object> children = getChildren();
		for (Object child : children) {
			if (child instanceof PlainTextConvertable) {
				((PlainTextConvertable) child).renderPlainText(converter, sbuf, wikiModel);
			}
		}

		wikiModel.addToReferences(sbuf.toString().trim(), null);
	}

}
