package de.l3s.cleopatra.quotekg.data;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import mwdumper.articleprocessing.Article;

public class TypesAdder {

	public static void addTypesToArticles(DataStorage dataStorage, String fileName) {

		LineIterator it;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");

			try {
				while (it.hasNext()) {
					String line = it.nextLine();

					// Example line: 100000024 School

					String[] parts = line.split(" ");

					int wikidataId = Integer.valueOf(parts[0]);
					Article article = dataStorage.getArticlesByWikidataId().get(wikidataId);

					if (article == null)
						continue;

					String type = parts[1];

					article.addType(type);
				}
			} finally {
				it.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
