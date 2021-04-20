import java.io.IOException;

import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.ParserConfig;
import org.sweble.wikitext.parser.WikitextPreprocessor;
import org.sweble.wikitext.parser.WtEntityMap;
import org.sweble.wikitext.parser.WtEntityMapImpl;
import org.sweble.wikitext.parser.encval.ValidatedWikitext;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtPreproWikitextPage;
import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtTemplateArguments;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtValue;

import xtc.parser.ParseException;

public class SwebleTest {

	public static void main(String[] args) throws IOException, ParseException {

		WikiConfig config = DefaultConfigEnWp.generate();
		// WtEngineImpl engine = new WtEngineImpl(config);

		ParserConfig parserConfig = config.getParserConfig();

		WtEntityMap em = new WtEntityMapImpl();

		String wikiText = "{{Vertaald citaat\n"
				+ "| tekst = Ich muss ganz ehrlich sagen, wenn wir jetzt anfangen, uns noch entschuldigen zu müssen dafür, dass wir in Notsituationen ein freundliches Gesicht zeigen, dann ist das nicht mein Land.\n"
				+ "| bron = Tijdens een persconferentie op 15 september 2015 met haar Oostenrijkse collega Werner Faymann, aangehaald in {{aut|Andreas Rinke}}, [http://de.reuters.com/article/domesticNews/idDEKCN0RF1S920150915 ''\"Dann ist das nicht mein Land\" - Merkels Vertrauensfrage''], Reuters Deutschland, 15 september 2015.\n"
				+ "| taal = Duits\n"
				+ "| vertaling= Ik moet eerlijk zeggen; als we nu beginnen dat we ons ook nog moeten verontschuldigen voor het feit dat wij in noodsituaties een vriendelijk gezicht tonen, dan is dat niet mijn land.\n"
				+ "| aangehaald = [http://duitslandinstituut.nl/artikel/13001/Merkel-als-boegbeeld-van-Willkommenskultur ''Merkel als boegbeeld van ‘Willkommenskultur’''], Duitsland Instituut, 15 september 2015\n"
				+ "| opmerking = Reactie op de groeiende weerstand in Merkels eigen land, Duitsland, tegen de grote toestroom vluchtelingen uit onder meer het Midden-Oosten.\n"
				+ "| opmerking2 =\n" + "}}";

		ValidatedWikitext text = new ValidatedWikitext(wikiText, em, false);
		WikitextPreprocessor preprocessor = new WikitextPreprocessor(parserConfig);
		WtPreproWikitextPage preprocessedAst = (WtPreproWikitextPage) preprocessor.parseArticle(text, "Title", false);

		System.out.println(preprocessedAst);

		WtText text2 = (WtText) preprocessedAst.get(0).get(0).get(0);
		System.out.println(text2.getContent());

		WtTemplateArguments templateArguments = (WtTemplateArguments) preprocessedAst.get(0).get(1);
		for (WtNode node : templateArguments) {
			WtTemplateArgument arg = (WtTemplateArgument) node;
			String key = ((WtText) arg.get(0).get(0)).getContent();
			System.out.println("Key: " + key);
			WtValue val = (WtValue) arg.get(1);

			for (int i = 0; i < val.size(); i++) {
				System.out.println(val.get(i));
				System.out.println(val.get(i).getNodeName());
				if (val.get(i).getNodeName().equals("WtTemplate")) {
					WtTemplate tem = (WtTemplate) val.get(i);
					System.out.println(tem.getArgs());
				}
			}

		}

	}

}
