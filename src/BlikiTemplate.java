import java.io.IOException;

import mwdumper.articleprocessing.MyTemplateParser;
import mwdumper.articleprocessing.MyWikiModel;

public class BlikiTemplate {

	public static void main(String[] args) throws IOException {

		String wikiText = "{{Vertaald citaat\n"
				+ "| tekst = Ich muss ganz ehrlich sagen, wenn wir jetzt anfangen, uns noch entschuldigen zu müssen dafür, dass wir in Notsituationen ein freundliches Gesicht zeigen, dann ist das nicht mein Land.\n"
				+ "| bron = Tijdens een persconferentie op 15 september 2015 met haar Oostenrijkse collega Werner Faymann, aangehaald in {{aut|Andreas Rinke}}, [http://de.reuters.com/article/domesticNews/idDEKCN0RF1S920150915 ''\"Dann ist das nicht mein Land\" - Merkels Vertrauensfrage''], Reuters Deutschland, 15 september 2015.\n"
				+ "| taal = Duits\n"
				+ "| vertaling= Ik moet eerlijk zeggen; als we nu beginnen dat we ons ook nog moeten verontschuldigen voor het feit dat wij in noodsituaties een vriendelijk gezicht tonen, dan is dat niet mijn land.\n"
				+ "| aangehaald = [http://duitslandinstituut.nl/artikel/13001/Merkel-als-boegbeeld-van-Willkommenskultur ''Merkel als boegbeeld van ‘Willkommenskultur’''], Duitsland Instituut, 15 september 2015\n"
				+ "| opmerking = Reactie op de groeiende weerstand in Merkels eigen land, Duitsland, tegen de grote toestroom vluchtelingen uit onder meer het Midden-Oosten.\n"
				+ "| opmerking2 =\n" + "}}";

		MyWikiModel wikiModel = new MyWikiModel("${image}", "${title}");
		StringBuilder buf = new StringBuilder(wikiText.length() + wikiText.length() / 10);

		MyTemplateParser.parseRecursive(wikiText, wikiModel, buf, false, true, null);

		wikiModel.parseTemplates(wikiText);
		
		System.out.println("TYPE:"+wikiModel.getTemplate().getType());
		for(String key: wikiModel.getTemplate().getValues().keySet()) {
			System.out.println(key+" -> "+wikiModel.getTemplate().getValues().get(key));
		}
		
//		String plainStr = wikiModel.render(new PlainTextConverter(false), wikiText).trim();
//		System.out.println(plainStr);
	}

}
