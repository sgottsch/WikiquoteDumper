package de.l3s.cleopatra.quotekg.mwdumper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mediawiki.dumper.ProgressFilter;
import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.MultiWriter;
import org.mediawiki.importer.XmlDumpReader;

import de.l3s.cleopatra.quotekg.data.DataStorage;
import de.l3s.cleopatra.quotekg.data.WikiquoteToJSONWriter;
import de.l3s.cleopatra.quotekg.links.WikidataMapping;
import de.l3s.cleopatra.quotekg.model.Language;
import de.l3s.cleopatra.quotekg.util.StopTitlesLoader;
import mwdumper.articleprocessing.WikiquoteExtractor;

class WikiquoteDumper {

	private static final int IN_BUF_SZ = 1024 * 1024;

	public static void main(String[] args) throws IOException, ParseException {

		// Arguments:
		// language
		// data folder
		// output folder

		if (args.length != 4) {
			System.out.println("Not enough arguments.");
			return;
		}

		Language language = Language.getLanguage(args[0]);

		String dataFolder = args[1];
		if (!dataFolder.endsWith("/"))
			dataFolder += "/";

		String outputFolder = args[2];
		if (!outputFolder.endsWith("/"))
			outputFolder += "/";

		int numberOfThreads = Integer.valueOf(args[3]);

		System.out.println("Language: " + language);
		System.out.println("dataFolder: " + dataFolder);
		System.out.println("outputFolder: " + outputFolder);
		System.out.println("numberOfThreads: " + numberOfThreads);

		MultiWriter writers = new MultiWriter();

		String fileName = dataFolder + language.getLanguage() + "/pages.xml";

		WikidataMapping wikidataMapping = new WikidataMapping(language);
		wikidataMapping.loadWikidataMapping(dataFolder);

		List<Language> languages = new ArrayList<Language>();
		languages.add(language);

		DataStorage dataStorage = new DataStorage();
		dataStorage.loadPersonWikidataIDs(dataFolder + "persons.csv");

		Set<String> stopTitles = StopTitlesLoader.getStopTitles(language);

		WikiquoteExtractor sink = getWikiquoteExtractor(wikidataMapping, dataStorage, numberOfThreads, stopTitles);
		InputStream input = new FileInputStream(new File(fileName));
		// openStandardInput();

		writers.add(sink);

		int progressInterval = 1000;
		DumpWriter outputSink = (progressInterval > 0) ? (DumpWriter) new ProgressFilter(writers, progressInterval)
				: (DumpWriter) writers;

		XmlDumpReader reader = new XmlDumpReader(input, outputSink);
		reader.readDump();

		writeOutput(sink, dataFolder + "types.ttl", dataFolder + language.getLanguageLowerCase() + "/ids.ttl",
				outputFolder + "results_" + language.getLanguageLowerCase() + ".json", language);
	}

	private static void writeOutput(WikiquoteExtractor sink, String typesFileName, String idsFileName,
			String outputFileName, Language language) {
		System.out.println("Write output. Number of articles: " + sink.getDataStorage().getArticles().size());
		WikiquoteToJSONWriter.writeJSONs(sink.getDataStorage(), outputFileName, typesFileName, idsFileName, language);
	}

	static InputStream openStandardInput() throws IOException {
		return new BufferedInputStream(System.in, IN_BUF_SZ);
	}

	static class OutputWrapper {
		private OutputStream fileStream = null;
		private Connection sqlConnection = null;

		OutputWrapper(OutputStream aFileStream) {
			fileStream = aFileStream;
		}

		OutputWrapper(Connection anSqlConnection) {
			sqlConnection = anSqlConnection;
		}

		OutputStream getFileStream() {
			if (fileStream != null)
				return fileStream;
			if (sqlConnection != null)
				throw new IllegalArgumentException("Expected file stream, got SQL connection?");
			throw new IllegalArgumentException("Have neither file nor SQL connection. Very confused!");
		}

	}

	private static WikiquoteExtractor getWikiquoteExtractor(WikidataMapping wikidataMapping, DataStorage dataStorage,
			int numberOfThreads, Set<String> stopTitles) {
		return new WikiquoteExtractor(wikidataMapping, dataStorage, numberOfThreads, stopTitles);
	}

}
