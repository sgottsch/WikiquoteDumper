package de.l3s.cleopatra.quotekg.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import mwdumper.articleprocessing.Article;

public class DataStorage {

	private List<Article> articles = new ArrayList<Article>();

	private Map<String, Article> articlesByWikipediaId = new HashMap<String, Article>();
	private Map<String, Article> articlesByWikiquoteId = new HashMap<String, Article>();
	private Map<Integer, Article> articlesByWikidataId = new HashMap<Integer, Article>();

	private Set<Integer> personWikidataIDs;

	public List<Article> getArticles() {
		return articles;
	}

	public void setArticles(List<Article> articles) {
		this.articles = articles;
	}

	public Map<String, Article> getArticlesByWikipediaId() {
		return articlesByWikipediaId;
	}

	public Map<String, Article> getArticlesByWikiquoteId() {
		return articlesByWikiquoteId;
	}

	public Map<Integer, Article> getArticlesByWikidataId() {
		return articlesByWikidataId;
	}

	public Set<Integer> getPersonWikidataIDs() {
		return personWikidataIDs;
	}

	public void setPersonWikidataIDs(Set<Integer> personWikidataIDs) {
		this.personWikidataIDs = personWikidataIDs;
	}

	public void loadPersonWikidataIDs(String personsFileName) {
		this.personWikidataIDs = new HashSet<Integer>();
		try {
			for (String line : FileUtils.readLines(new File(personsFileName), "UTF-8")) {
				this.personWikidataIDs.add(Integer.valueOf(line));
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
