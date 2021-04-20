package de.l3s.cleopatra.quotekg.links;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import de.l3s.cleopatra.quotekg.model.Language;

public class WikidataMapping {

	private Map<Integer, Integer> wikiquotePageIdToWikidataId = new HashMap<Integer, Integer>();
	private Language language;

	public WikidataMapping(Language language) {
		super();
		this.language = language;
	}

	public void loadWikidataMapping(String basePath) {

		LineIterator it = null;
		
		try {
			it = FileUtils.lineIterator(new File(basePath + language.getLanguage() + "/wikidata.sql"), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				if (line.startsWith("INSERT INTO")) {
					String[] parts = line.split("\\),\\(");
					for (String part : parts) {
						part = part.replaceAll("^\\(", "");
						part = part.replaceAll("\\)$", "");
						part = part.replaceAll("\\);$", "");
						String[] partParts = part.split(",");
						String wikidataId = StringUtils.strip(partParts[1], "'");
						this.wikiquotePageIdToWikidataId.put(Integer.valueOf(partParts[3]),
								Integer.valueOf(wikidataId.substring(1)));
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				it.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public Integer getWikidataId(int pageId) {
		return this.wikiquotePageIdToWikidataId.get(pageId);
	}

}
