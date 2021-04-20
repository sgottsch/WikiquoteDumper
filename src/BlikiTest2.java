import java.io.IOException;

import info.bliki.wiki.model.WikiModel;
import mwdumper.articleprocessing.MyWikiModel;
import mwdumper.model.Link;
import mwdumper.model.Template;

public class BlikiTest2 {

	public static final String TEST = "This is a [[Hello World]] '''example'''";

	public static void main(String[] args) throws IOException {

		String wikiText = "This is a [[Hello World]] '''example'''";

//		wikiText = "[[File:Angela Merkel, Juli 2010.jpg|thumb|300px|[[w:Angela Merkel|Angela Merkel]]]]\n"
//				+ "== [[w:Angela Merkel|Angela Merkel]] (*1954) ==\n" + "''deutsche Politikerin ([[CDU]])'' \n" + "\n"
//				+ "== Zitate mit Quellenangabe ==\n" + "<small>nach Jahren geordnet</small>\n" + "\n" + "===2017===\n"
//				+ "* \"Das Volk ist jeder, der in diesem Land lebt.\" - ''[https://www.bild.de/politik/inland/angela-merkel/aufregung-um-merkel-zitat-volk-ist-wer-hier-lebt-50606620.bild.html Bild:] Merkels Rede zu Ihrer Kanditatur als Spitzenkandidatin der CDU am 26. Februar 2017''\n"
//				+ "* \"Die Zeiten, in denen wir uns auf andere völlig verlassen konnten, die sind ein Stück vorbei, das habe ich in den letzten Tagen erlebt. Und deshalb kann ich nur sagen: Wir Europäer müssen unser Schicksal wirklich in unsere eigene Hand nehmen. [...] wir müssen selber für unsere Zukunft kämpfen, als Europäer, für unser Schicksal.\" - ''[http://www.spiegel.de/politik/deutschland/angela-merkel-das-bedeutet-ihre-bierzelt-rede-ueber-donald-trump-a-1149649.html Spiegel:] Merkel nach dem G7-Gipfel am 28. Mai 2017''";

		wikiText = "{{Tilvitnun|Ég hef stundum verið sakaður um að svara ekki, þótt ég telji mig hafa svarað, af því að fyrirspyrjandinn vildi fá annað svar en ég gaf.|útskýring = {{vefheimild|url=http://bjorn.is/dagbok/ | Dagbók Björns Bjarnasonar á vefnum 4. júlí 2007 | 20. júlí|2007}} }}";

		WikiModel wikiModel = new WikiModel("${image}", "${title}");

		String plainStr = wikiModel.render(new BlikiConverter(false), wikiText);
		System.out.println(plainStr);

		System.out.println(wikiModel.getLinks());
		System.out.println(wikiModel.getReferences());
		System.out.println(wikiModel.getTemplates());
		
		for(String t: wikiModel.getTemplates())
			System.out.println(t);
		

//        WikipediaParser.parse(plainStr, wikiModel, true, null);
//        System.out.println(wikiModel.stackSize());

	}

}
