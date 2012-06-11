package org.rapidsms.java.core.parser.interpreter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


/**
 * @author Adjoa Poku adjoa.poku@jhuapl.edu
 * @created June 2011 Summary:
 */
public class AlphaNumericInterpreter implements IParseInterpreter {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rapidsms.java.core.parser.interpreter.IParseInterpreter#interpretValue
	 * (java.lang.String)
	 */

	Pattern mPattern;

	private static String[] unicodeBlocks = {"Basic Latin",
		"Latin-1 Supplement",
		"Latin Extended-A",
		"Latin Extended-B",
//		"IPA Extensions",
//		"Spacing Modifier Letters",
//		"Combining Diacritical Marks",
//		"Greek",
//		"Cyrillic",
//		"Khmer",
//		"Latin Extended Additional",
//		"Greek Extended",
//		"General Punctuation",
//		"Superscripts and Subscripts",
//		"Currency Symbols",
//		"Combining Marks for Symbols",
//		"Letterlike Symbols",
//		"Number Forms",
//		"Arrows",
//		"Mathematical Operators",
//		"Miscellaneous Technical",
//		"Optical Character Recognition",
//		"Enclosed Alphanumerics",
		"Hangul Jamo",
		"Hangul Compatibility Jamo",
		"Box Drawing",
		"Block Elements",
		"Geometric Shapes",
		"Miscellaneous Symbols",
		"Dingbats"	};
	
	public AlphaNumericInterpreter() {
		String unicodeBlockPullin = "\\p{In" + StringUtils.join(unicodeBlocks, "}\\p{In") + "}";
		System.out.println("ALPHANUMERIC INTERPRETER: FOOBAR>> " + unicodeBlockPullin);
		// MUST HAVE ZERO GROUPS AND OTHER TOKENIZING JUNK
		mPattern = Pattern.compile("([A-Za-z\\d" + unicodeBlockPullin + "]+)\\.*");
//		mPattern = Pattern.compile("[A-Za-z\\d]+\\.*");
	}

	public Object interpretValue(String token) {
		Matcher m = mPattern.matcher(token);
		if (m.find()) {
			try {
				return token;
			} catch (Exception ex) {
				return null;
			}
		}
		return null;
	}
}
