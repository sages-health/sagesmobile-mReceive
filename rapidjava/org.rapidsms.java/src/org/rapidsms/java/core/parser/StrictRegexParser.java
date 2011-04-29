/**
 * 
 */
package org.rapidsms.java.core.parser;


/**
 * @author Adjoa Poku adjoa.poku@jhuapl.edu
 * @created Apr 12, 2011
 * 
 * 
 *          This is the second instance of a message parser for RapidAndroid
 * 
 *          The objective for this parser is to have a strict, greedy order
 *          dependent parse of a message
 * 
 *          for a given message MSG and a form F with fields [a,b,c,d,e]
 * 
 *          where the fields have regexes for each "token" they want to parse
 *          out (height measurement or a string for example)
 * 
 *          This parser will iterate through each field in order, greedily try
 *          to find an *exact* match it can find from its regex
 *          Slice out the substring of the first match from the original message
 *          MSG, and continue onto the next field again.
 *          
 *          Upon the first non-match, this parser returns with the results vector null
 */
public class StrictRegexParser extends SimpleRegexParser implements IMessageParser {

	public StrictRegexParser() {
	
	}

	/**
	 * @param res the IparseResult just parsed
	 * @return true if the result is null. this signals we should stop parsing
	 */
	protected boolean exitOnNullResult(IParseResult res){
		return (true && res == null);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapidsms.java.core.parser.IMessageParser#getName()
	 */
	public String getName() {
		return "strictregex";
	}

}
