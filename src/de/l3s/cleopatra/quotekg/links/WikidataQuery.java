package de.l3s.cleopatra.quotekg.links;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.cleopatra.quotekg.model.Language;

public class WikidataQuery {

	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(getWikidataIdByWikipediaId(Language.HR, "Kraš"));
	}

	public static Integer getWikidataIdByWikipediaId(Language language, String wikipediaId) {

		try {
			String query = "SELECT ?id WHERE { <https://" + language.getLanguage() + ".wikipedia.org/wiki/"
					+ URLEncoder.encode(wikipediaId.replace(" ", "_"), "UTF-8") + "> schema:about ?id . }";

			String url = "https://query.wikidata.org/sparql?query=" + URLEncoder.encode(query, "UTF-8")
					+ "&format=json";

			JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));

			JSONArray results = json.getJSONObject("results").getJSONArray("bindings");
			if (results.length() == 0) {
				System.out.println("Wikidata ID: " + wikipediaId + " -> /");
				return null;
			}
			String id = results.getJSONObject(0).getJSONObject("id").getString("value");
			int wikidataId = Integer.valueOf(id.substring(id.lastIndexOf("/") + 2));

			System.out.println("Wikidata ID: " + wikipediaId + " -> " + wikidataId);
			return wikidataId;
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
