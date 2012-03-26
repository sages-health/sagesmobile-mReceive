/**
 * 
 */
package org.rapidandroid.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author POKUAM1
 * @created Feb 16, 2012
 */
public class Demodulator {

	private static String splitQualifier = "#"; 
	// parentheses causes issues. won't allow unit tests to run unless escaped, and then it doesn't
	// match in the demodulator as an escaped string. too much hassle for paren.  \(
	// Test run failed: Instrumentation run failed due to 'java.util.regex.PatternSyntaxException'
	
	public static Map<Long, String[]> deModulateBlobMap(Map<Long, String> blobMap){
		Map<Long, String[]> demodMap = new HashMap<Long, String[]>();

		for (Entry<Long, String> e : blobMap.entrySet()){
			Long tx_id = e.getKey();
			String blob = e.getValue();
			demodMap.put(tx_id, demod(blob));
		}
		
		return demodMap;
	} 
	
	public static String[] demod(String blob){
		blob = blob.startsWith(splitQualifier) ? blob.substring(1) : blob;
		String[] demodedBlob = blob.split(splitQualifier);
		return demodedBlob;
	}
}
