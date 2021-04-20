package de.l3s.cleopatra.quotekg.model;

import java.util.Locale;

public enum Language {

	ALL("all", "All"), EN("en", "English"), DE("de", "German"), RU("ru", "Russian"), IT("it", "Italian"),
	FR("fr", "French"), PT("pt", "Portuguese"), NL("nl", "Dutch"), SV("sv", "Slovenian"),

	BG("bg", "Bulgarian"), CS("cs", "Czech"), HU("hu", "Hungarian"), PL("pl", "Polish"), RO("ro", "Romanian"),
	SK("sk", "Slovak"), SL("sl", "Slovene"), ES("es", "Spanish"), SE("sv", "Swedish"), FI("fi", "Finnish"),
	DA("da", "Danish"), AR("ar", "Arabian"), HR("hr", "Croatian"), NO("no", "Norwegian (Bokmål)"),
	NN("nn", "Norwegian (Nynorsk)"), SR("sr", "Serbian"), BS("bs", "Bosnian"), AF("af", "Afrikaans"),
	FO("fo", "Faroese"), FA("fa", "Persian"), HI("hi", "Hindi"), ZH("zh", "Mandarin Chinese"), TR("tr", "Turkish"),
	EL("el", "Greek"), UR("ur", "Urdu"), JA("ja", "Japanese"), TK("tk", "Turkmen"),

	DEFAULT(null, null);

	private String lang;
	private String adjective;
	private Locale locale;

	// Constructor
	Language(String l, String adjective) {
		this.lang = l;
		this.adjective = adjective;
		if (lang != null)
			this.locale = Locale.forLanguageTag(lang);
	}

	// Constructor
	Language(String l, String adjective, Locale locale) {
		this.lang = l;
		this.adjective = adjective;
		this.locale = locale;
	}

	// Overloaded constructor
	Language() {
		lang = null;
	}

	public String getLanguage() {
		return this.lang;
	}

	public String toString() {
		return this.lang;
	}

	public static Language getLanguage(String langStr) {
		try {
			return Language.valueOf(langStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			Language language = Language.DEFAULT;
			language.setAdjective(langStr);
			language.setLang(langStr);
			return language;
		}
	}

	public static Language getLanguageOrNull(String langStr) {
		try {
			return Language.valueOf(langStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public String getLanguageAdjective() {
		return this.adjective;
	}

	public static Language getLanguageByAdjective(String adjective) {

		for (Language language : Language.values()) {
			if (language.getLanguageAdjective().equals(adjective)) {
				return language;
			}
		}

		return null;
	}

	public static String getLanguageAdjective(Language language) {
		return language.getLanguageAdjective();
	}

	public String getWiki() {
		return getLanguageLowerCase() + "wiki";
	}

	public String getLanguageLowerCase() {
		return this.getLanguage().toLowerCase();
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setAdjective(String adjective) {
		this.adjective = adjective;
	}

}
