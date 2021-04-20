package de.l3s.cleopatra.quotekg.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TextUtil {

	// Source:
	// https://stackoverflow.com/questions/23445126/split-string-which-contains-escaped-delimiters

	static final char ESCAPING_CHAR = '\\';

	public static List<String> parseString(final String str, final char delimiter, final boolean removeEmpty)
			throws IOException {
		final Reader input = new StringReader(str);
		final StringBuilder part = new StringBuilder();
		final List<String> result = new ArrayList<String>();

		int c;
		do {
			c = input.read(); // get the next character

			if (c != delimiter) { // so long as it isn't a delimiter...
				if (c == ESCAPING_CHAR) // if it's an escape
					c = input.read(); // use the following character instead

				if (c >= 0) { // only if NOT at end of string...
					part.append((char) c); // append to current part
					continue; // move on to next character
				}
			}

			/* we're at either a real delimiter, or end of string => part complete */

			if (part.length() > 0 || !removeEmpty) { // keep this part?
				result.add(part.toString()); // add current part to result
				part.setLength(0); // reset for next part
			}
		} while (c >= 0); // repeat until end of string found

		return result;
	}

}
