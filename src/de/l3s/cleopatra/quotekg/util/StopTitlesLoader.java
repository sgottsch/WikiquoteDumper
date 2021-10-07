package de.l3s.cleopatra.quotekg.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import de.l3s.cleopatra.quotekg.model.Language;

public class StopTitlesLoader {

	public static Set<String> getStopTitles(Language language) {
		Set<String> stopTitles = new HashSet<String>();

		try {
			for (String line : getResourceFileAsString("stop_titles.txt").split("\n")) {
				String[] parts = line.split("\t");
				if (parts.length > 1) {
					if (language == Language.getLanguage(parts[0].toUpperCase())) {
						for (int i = 1; i < parts.length; i++)
							stopTitles.add(parts[i]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return stopTitles;
	}

	static String getResourceFileAsString(String fileName) throws IOException {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		try (InputStream is = classLoader.getResourceAsStream(fileName)) {
			if (is == null)
				return null;
			try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
				return reader.lines().collect(Collectors.joining("\n"));
			}
		}
	}
}
