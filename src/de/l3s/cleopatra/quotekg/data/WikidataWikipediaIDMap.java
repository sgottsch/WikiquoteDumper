package de.l3s.cleopatra.quotekg.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class WikidataWikipediaIDMap {

	private Map<String, Integer> wikidataIdsByWikipediaId = new HashMap<String, Integer>();

	public void load(DataStorage dataStorage, String fileName) {

		LineIterator it;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");

			try {
				while (it.hasNext()) {
					String line = it.nextLine();
					// System.out.println(line);
					if(line.isEmpty()||line.isBlank())
						continue;

					// Example line: 100000024 de SchuleXYZ

					String[] parts = line.split(" ");

					int wikidataId = Integer.valueOf(parts[0]);
					String wikipediaId = parts[1];

					wikidataIdsByWikipediaId.put(wikipediaId.replace("_", " "), wikidataId);
				}
			} finally {
				it.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Integer getWikidataIdByWikipediaId(String wikipediaId) {
		return this.wikidataIdsByWikipediaId.get(wikipediaId.replace("_", " "));
	}

}
