/*
 * Copyright (©) 2012 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Demodulates a concatenated message (a blob). Takes the complete concatenated message that adheres to the SAGES multisms syntax scheme, and 
 * breaks the message around the various qualifiers indicating where new forms data begins.
 * 
 * @author POKUAM1
 * @created Feb 16, 2012
 */
public class Demodulator {

	/**
	 * Multiple forms are indicated by a # syntax
	 */
	// parentheses causes issues. won't allow unit tests to run unless escaped, and then it doesn't
	// match in the demodulator as an escaped string. too much hassle for paren.  \(
	// Test run failed: Instrumentation run failed due to 'java.util.regex.PatternSyntaxException'
	private static String splitQualifier = "#"; 
	
	
	/**
	 * Demodulates all blobs in a map and places individual complete form values into a map corresponding
	 * to the original SMS submission txId
	 * @param blobMap
	 * @return Map of SMS submission txId and all form values it contained
	 */
	public static Map<Long, String[]> deModulateBlobMap(Map<Long, String> blobMap){
		Map<Long, String[]> demodMap = new HashMap<Long, String[]>();

		for (Entry<Long, String> e : blobMap.entrySet()){
			Long tx_id = e.getKey();
			String blob = e.getValue();
			demodMap.put(tx_id, demod(blob));
		}
		
		return demodMap;
	} 
	
	/**
	 * Demodulates a blob (concatenated message adhering to the SAGES multisms syntax)
	 * @param blob
	 * @return Array of the individual form values that were in the blob
	 */
	public static String[] demod(String blob){
		blob = blob.startsWith(splitQualifier) ? blob.substring(1) : blob;
		String[] demodedBlob = blob.split(splitQualifier);
		return demodedBlob;
	}
}
