package mwdumper.articleprocessing;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.Page;
import org.mediawiki.importer.Revision;
import org.mediawiki.importer.Siteinfo;
import org.mediawiki.importer.Wikiinfo;

import de.l3s.cleopatra.quotekg.data.DataStorage;
import de.l3s.cleopatra.quotekg.links.WikidataMapping;

public class WikiquoteExtractor implements DumpWriter {

	String pageTitle = "";
	List<Integer> _targetPageIds = null; //Arrays.asList(10585, 34965);
	int _pageId;
	boolean debug = false;
	String _page = "";
	boolean empty = true;
	String path;
	boolean pageIsMainArticle = false;
	int numberOfThreads;

	private WikidataMapping wikidataMapping;
	private DataStorage dataStorage;

	protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.S");

	public void close() throws IOException {
	}

	public WikiquoteExtractor(WikidataMapping wikidataMapping, DataStorage dataStorage, int numberOfThreads) {
		this.wikidataMapping = wikidataMapping;
		this.dataStorage = dataStorage;
		this.numberOfThreads = numberOfThreads;
	}

	public void writeStartWiki(Wikiinfo info) throws IOException {
	}

	public void writeEndWiki() throws IOException {
	}

	public void writeSiteinfo(Siteinfo info) throws IOException {
	}

	public void writeStartPage(Page page) throws IOException {
		this.empty = true;
		this._pageId = page.Id;
		this.pageTitle = page.Title.Text;
		if (page.Ns == 0 && !page.isRedirect) {
			this.pageIsMainArticle = true;
		}
	}

	public void writeEndPage() throws IOException {
		this.pageIsMainArticle = false;
	}

	public void writeRevision(Revision revision) throws IOException {

		if (this.pageIsMainArticle) {

//			if (dataStorage.getArticles().size() > 3)
//				return;

			Integer wikidataId = this.wikidataMapping.getWikidataId(this._pageId);
			if (wikidataId == null)
				return;
			if (!dataStorage.getPersonWikidataIDs().contains(wikidataId))
				return;

			if (this.pageTitle.startsWith("Babel:User"))
				return;

			if (this._targetPageIds != null && !this._targetPageIds.contains(this._pageId))
				return;
			else
				System.out.println("EXAMPLE FOUND");

			System.out.println(this.pageTitle + " -> " + wikidataId + " (" + this._pageId + ")");

			Article article = new Article(this.pageTitle, this._pageId, wikidataId);
			article.setText(revision.Text);
			this.dataStorage.getArticles().add(article);

			if (wikidataId != null)
				this.dataStorage.getArticlesByWikidataId().put(wikidataId, article);

			TextExtractor extractor = new TextExtractor(article, numberOfThreads);

			try {
				extractor.extractQuotes();
				if (!article.hasQuotes())
					return;

			} catch (Exception e) {
				System.err.println("Error (a) with " + this._pageId + ": " + this.pageTitle);
				System.err.println(e.getMessage() + "\n" + e.getStackTrace());
				e.printStackTrace();
			}

		}
	}

	public DataStorage getDataStorage() {
		return dataStorage;
	}

}
