/**
 * 
 */
package org.rapidsms.java.core.parser;

import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.parser.service.ParsingService.ParserType;

/**
 * @author Dan  Myung, Adjoa Poku
 * @created Apr 14, 2011
 */
public class ParsingUtils {

	/**
	 * ok, for this iteration, we're going to greedily determine if this is
	   a message we can fracking parse.
	 * @param f
	 * @param input
	 * @return
	 */
	public static String parsePrefix(Form f, String input){
		String prefix = f.getPrefix();
		// System.out.println("what's the fracking form prefix: " + prefix);
		input = input.toLowerCase().trim();
		if (input.startsWith(prefix + " ")) {
			input = input.substring(prefix.length()).trim();
			return input;
		} else {
			return null;
		}
	}
	
	public static void test(){
		ParserType.SIMPLEREGEX.ordinal();
	}
}
