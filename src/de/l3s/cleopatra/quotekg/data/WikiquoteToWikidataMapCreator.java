package de.l3s.cleopatra.quotekg.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import de.l3s.cleopatra.quotekg.links.WikidataMapping;
import de.l3s.cleopatra.quotekg.model.Language;

public class WikiquoteToWikidataMapCreator {

	public static void main(String[] args) {

		String dataFolder = args[0];
		if (!dataFolder.endsWith("/"))
			dataFolder += "/";
		String outputFileName = args[1];

		File file = new File(dataFolder);
		String[] directories = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputFileName);
			for (String folderName : directories) {
				createFile(Language.getLanguage(folderName), dataFolder, writer);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}

	private static void createFile(Language language, String dataFolder, PrintWriter writer) {

		WikidataMapping wikipediaWikidataIDMap = new WikidataMapping(language);
		wikipediaWikidataIDMap.loadWikidataMapping(dataFolder);

		Map<Integer, String> wikiquoteTitles = loadWikiquoteTitles(dataFolder, language);

		Map<Integer, Integer> map = wikipediaWikidataIDMap.getWikiquotePageIdToWikidataId();

		for (Integer wikiquoteId : map.keySet()) {
			writer.println(language + "\t" + wikiquoteId + "\t" + wikiquoteTitles.get(wikiquoteId) + "\t" + "Q"
					+ map.get(wikiquoteId));
		}
	}

	private static Map<Integer, String> loadWikiquoteTitles(String dataFolder, Language language) {

		String titlesFileName = dataFolder + language.getLanguage().toLowerCase() + "/wikiquote_titles.tsv";

		Map<Integer, String> wikiquoteTitles = new HashMap<Integer, String>();

		LineIterator it = null;

		try {
			it = FileUtils.lineIterator(new File(titlesFileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split("\t");
				wikiquoteTitles.put(Integer.valueOf(parts[1]), parts[0]);
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

		return wikiquoteTitles;
	}

}
