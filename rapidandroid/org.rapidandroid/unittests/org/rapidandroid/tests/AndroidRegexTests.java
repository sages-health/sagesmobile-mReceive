/*
 * Copyright (C) 2009 Dimagi Inc., UNICEF
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.rapidandroid.tests;

import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import android.util.Log;

/**
 * @author dmyung
 * @created Jan 16, 2009
 */
public class AndroidRegexTests extends TestCase {
/*
	// ((\d*[a-zA-Z]+\d*)(\s))
	// ([\D\d]+)(\s) - worked
	// \d*[a-zA-Z]+\d*\s worked best
	// (\d*[a-zA-Z]+\d*)+(\s) try this
	 
	String[] goodMessages = { "bednets nyc 100 30 80", "bednets lax 1,500, 750 1000",
			"nutrition 12345 20 100 0.6 5 y n", "nutrition 55446 20kg 100cm 60% 5cm yes no",
			"nutrition 55446 21 kg 100cm 60% 5cm yes no", "nutrition 55446 2 kg 100 m 3/4 5cm yes no" };
	
	String[] problematic = { "bednets nyc 100 30", "bednets lax 1,500", "bednets", "bednets 100 200 300",
			"bednets 100 200 300 400", "bednets BOS 100 200 300 12321", "bednets 100 BOS 100 200 120",
			"bednets 100 BOS 100 200 120 51231", "bednetsBOS 100 200 120 51231",
			
			"nutrition asdfsadf 12345 20 100 0.6 5 y n", "nutrition 55446 20kg 100cm 60% 5cm yes no",
			"nutrition 55446 20kg 60% 5cm yes no", "nutrition 55446 21 100cm 60% 5cm yes no",
			"nutrition 55446 2 kg 100 m 5cm yes no"};
*/
	String[] goodMessages = { "55555 nyc12 23", "nyc12 23", "1a0b0c 66", "AABB30 56 56", "words4yah 66", "00099d 34 bed", "PO", "11FFad" };

	String[] problematic = { "bednets nyc 100 30", "bednets lax 1,500", "bednets", "bednets 100 200 300",
			"bednets 100 200 300 400", "bednets BOS 100 200 300 12321", "bednets 100 BOS 100 200 120",
			"bednets 100 BOS 100 200 120 51231", "bednetsBOS 100 200 120 51231",

			"nutrition asdfsadf 12345 20 100 0.6 5 y n", "nutrition 55446 20kg 100cm 60% 5cm yes no",
			"nutrition 55446 20kg 60% 5cm yes no", "nutrition 55446 21 100cm 60% 5cm yes no",
			"nutrition 55446 2 kg 100 m 5cm yes no"

	};

	String[] badMessages = { "bednwafasd this.is.free.text 12983 How.Is>E$#$# !.ajds" };
	String [] freetextMessages = {"ft this here is a free text message", "ft wow.i.never.new.about.free.texting"};
	String[] custom1Messages = {"diah.2 head.44 tong.65", "car.12 house.44", "single.1"};
	public void testCustom1() {
		Vector<String> regexes = new Vector<String>();
		HashMap<String, String> hackRegexHash = new HashMap<String, String>();
		System.out.println("Begin test:  Custom 1");
		// ^((([A-Za-z\d]+)\.\d+\s?){1,2}|(\.))($|\s)
		regexes.add("(([A-Za-z\\d]+\\.\\d+\\s?){1,2}|(\\.))($|\\s)");
		// ^((([A-Za-z\\d]+)\\.\\d+(\\s|$)){1,3}|(\\.))($|\\s) < -- was in custom but need to test new one.
		
		
		//doParse("goodMessages", goodMessages, regexes);
		//doParse("problematic", problematic, regexes);
		//doParse("badMessages", badMessages, regexes);
		doParse("custom1Messages", custom1Messages, regexes);
		
	}
	
	public void testFreeTexts() {
		Vector<String> regexes = new Vector<String>();
		HashMap<String, String> hackRegexHash = new HashMap<String, String>();
		System.out.println("Begin test:  FreeTexts");
		
		regexes.add("^(([A-Za-z\\d\\s\\.]+)|(\\.))($|\\s)");

		
		doParse("goodMessages", goodMessages, regexes);
		doParse("freetextMessages", freetextMessages, regexes);
		//doParse("problematic", problematic, regexes);
		//doParse("badMessages", badMessages, regexes);
		
	}
/*	
	public void testAlphaNumerics() {
		Vector<String> regexes = new Vector<String>();
		HashMap<String, String> hackRegexHash = new HashMap<String, String>();
		System.out.println("Begin test:  AlphaNumerics");
		regexes.add("^(\\d*[a-zA-Z]+\\d*)+($|\\s)");
		// (\d*[a-zA-Z]+\d*)+(\s)
		doParse("goodMessages", goodMessages, regexes);
		doParse("problematic", problematic, regexes);
		doParse("badMessages", badMessages, regexes);
		
	}

	public void testBools() {
		Vector<String> regexes = new Vector<String>();
		HashMap<String, String> hackRegexHash = new HashMap<String, String>();
		System.out.println("Begin test:  Booleans");

		regexes.add("(^|\\s)(t|f|true|false|y|no|yes|n|n0)(\\s|$)");
		// regexes.add("(\\d+)(\\s*(cm|mm|m|meter|meters))");
		// regexes.add("(\\d+\\:\\d+)|(\\d+\\/\\d+)|(\\d+\\s*%)|(\\d+\\s*pct)");
		// regexes.add("(\\d+)(\\s*(cm|m))");
		// regexes.add("(\\d+)(\\s*(kg|kilo|kilos))");
		// regexes.add("(\\d+)");
		// regexes.add("(\\w+)");

		doParse("goodMessages", goodMessages, regexes);
		doParse("problematic", problematic, regexes);
		doParse("badMessages", badMessages, regexes);

	}

	public void testNumeric() {
		Vector<String> regexes = new Vector<String>();
		HashMap<String, String> hackRegexHash = new HashMap<String, String>();
		Log.d("AndroidRegexTests",
				"\n\n#########################Begin test:  Numeric########################################");

		// regexes.add("(\\d+)(\\s*(cm|mm|m|meter|meters))");
		// regexes.add("(\\d+\\:\\d+)|(\\d+\\/\\d+)|(\\d+\\s*%)|(\\d+\\s*pct)");
		// regexes.add("(\\d+)(\\s*(cm|m))");
		// regexes.add("(\\d+)(\\s*(kg|kilo|kilos))");
		regexes.add("(^|\\s)(\\d+)(\\s|$)");
		// regexes.add("(\\w+)");

		doParse("goodMessages", goodMessages, regexes);
		doParse("problematic", problematic, regexes);
		doParse("badMessages", badMessages, regexes);
	}
*/
	private void doParse(String testname, String[] messages, Vector<String> regexes) {
		Log.d("AndroidRegexTests", "###############  Do Parse: " + testname + " ###################\n\n");

		for (int i = 0; i < messages.length; i++) {
			Log.d("AndroidRegexTests", "Matching message: " + messages[i]);
			for (int j = 0; j < regexes.size(); j++) {
				Pattern mPattern;
				mPattern = Pattern.compile(regexes.get(j));
				Matcher matcher = mPattern.matcher(messages[i]);
				Log.d("AndroidRegexTests", "\tMatching regex: " + regexes.get(j));
				boolean isMatched = matcher.find();
				if (isMatched) {
					while (isMatched) {

						Log.d("AndroidRegexTests", "\t\t**********************");

						for (int q = 0; q < matcher.groupCount(); q++) {
							Log.d("AndroidRegexTests", "\t\tRegion:" + matcher.start(q) + "-" + matcher.end(q));
							Log.d("AndroidRegexTests", "\t\t\tgroup: " + q + "  ##" + matcher.group(q) + "##");
						}
						Log.d("AndroidRegexTests", "\t\t**********************");
						isMatched = matcher.find();
					}
				} else {
					Log.d("AndroidRegexTests", "\t\tNo match!");
				}

			}

		}
	}

}
