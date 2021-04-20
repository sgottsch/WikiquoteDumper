package de.l3s.cleopatra.quotekg.analysis;

import org.json.JSONArray;
import org.json.JSONObject;

import mwdumper.model.Line;
import mwdumper.model.Link;
import mwdumper.model.Section;

public class JSONLoader {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void loadFromJSON(JSONObject json) {

		JSONObject mainSectionJSON = json.getJSONArray("sections").getJSONObject(0);

		Section mainSection = parseSectionJSON(mainSectionJSON);

	}

	private Section parseSectionJSON(JSONObject sectionJSON) {
		Section section = new Section();

		if (sectionJSON.has("title"))
			section.setTitle(parseLine(sectionJSON.getJSONObject("title")));

		if (sectionJSON.has("sections")) {
			JSONArray sectionArr = sectionJSON.getJSONArray("sections");
			for (int i = 0; i < sectionArr.length(); i++) {
				section.addSubSection(parseSectionJSON(sectionArr.getJSONObject(i)));
			}
		}

		return section;
	}

	private Line parseLine(JSONObject lineJSON) {

		Line line = new Line();

		if (lineJSON.has("text"))
			line.setCleanText(lineJSON.getString("text"));

		if (lineJSON.has("links")) {
			JSONArray linksArr = lineJSON.getJSONArray("links");
			for (int i = 0; i < linksArr.length(); i++) {
				line.addLink(parseLink(linksArr.getJSONObject(i)));
			}
		}

		return line;
	}

	private Link parseLink(JSONObject linkJSON) {

		Link link = new Link();

		if (linkJSON.has("text"))
			link.setText(linkJSON.getString("text"));
		if (linkJSON.has("prefix"))
			link.setPrefix(linkJSON.getString("prefix"));

		return link;
	}

}
