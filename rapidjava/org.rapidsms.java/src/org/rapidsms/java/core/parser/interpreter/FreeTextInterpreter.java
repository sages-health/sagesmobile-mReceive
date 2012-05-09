package org.rapidsms.java.core.parser.interpreter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Adjoa Poku adjoa.poku@jhuapl.edu
 * @created June 2011 Summary:
 */
public class FreeTextInterpreter implements IParseInterpreter {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rapidsms.java.core.parser.interpreter.IParseInterpreter#interpretValue
	 * (java.lang.String)
	 */

	Pattern mPattern;

	public FreeTextInterpreter() {
		// MUST HAVE ZERO GROUPS AND OTHER TOKENIZING JUNK
		mPattern = Pattern.compile("'[A-Za-z\\d\\s\\.]*'\\.*");
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
