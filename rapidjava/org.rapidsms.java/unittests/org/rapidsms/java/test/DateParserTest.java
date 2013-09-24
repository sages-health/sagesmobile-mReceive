package org.rapidsms.java.test;

import org.rapidsms.java.core.model.SimpleFieldType;
import org.rapidsms.java.core.parser.IParseResult;

import junit.framework.TestCase;

public class DateParserTest extends TestCase {

	SimpleFieldType sft1;
	protected void setUp() throws Exception {
		super.setUp();
		
		SimpleFieldType sft = new SimpleFieldType(1, "date224", "((\\d{2,2}\\.{1,1}){2,2}\\d{4,4}|(\\d{2,2}\\/{1,1}){2,2}\\d{4,4}|(\\d{2,2}\\-{1,1}){2,2}\\d{4,4}|\\d{4,4}(\\.\\d{2,2}){2,2}|\\d{4,4}(\\/\\d{2,2}){2,2}|\\d{4,4}(\\/\\d{2,2}){2,2}|\\d{4,4}(\\-\\d{2,2}){2,2})$",
//		SimpleFieldType sft = new SimpleFieldType(1, "date224", "((\\d{2,2}\\.{1,1}){2,2}\\d{4,4})|((\\d{2,2}\\/{1,1}){2,2}\\d{4,4})|((\\d{2,2}\\-{1,1}){2,2}\\d{4,4})|(\\d{4,4}(\\.\\d{2,2}){2,2})|(\\d{4,4}(\\/\\d{2,2}){2,2})|(\\d{4,4}(\\-\\d{2,2}){2,2})",
//		SimpleFieldType sft = new SimpleFieldType(1, "date224", "((\\d{2,2}\\.{1,1}){2,2}\\d{4,4})|((\\d{2,2}\\/{1,1}){2,2}\\d{4,4})|((\\d{2,2}\\-{1,1}){2,2}\\d{4,4})|(\\d{4,4}(\\.\\d{2,2}){2,2})|(\\d{4,4}(\\/\\d{2,2}){2,2})|(\\d{4,4}(\\-\\d{2,2}){2,2})$",
//				SimpleFieldType sft = new SimpleFieldType(1, "date224", "((\\d{2,2}\\.{1,1}){2,2}\\d{4,4})|((\\d{2,2}\\/{1,1}){2,2}\\d{4,4})|((\\d{2,2}\\-{1,1}){2,2}\\d{4,4})|(\\d{4,4}(\\.\\d{2,2}){2,2})|(\\d{4,4}(\\/\\d{2,2}){2,2})|(\\d{4,4}(\\-\\d{2,2}){2,2})\\.*",
//		SimpleFieldType sft = new SimpleFieldType(1, "date224", "^(\\d{4,4}(\\.\\d{2,2}){2,2})|(\\d{4,4}(\\/\\d{2,2}){2,2})|(\\d{4,4}(\\-\\d{2,2}){2,2})(\\s|$)",
				"date224_test");
//		sft.setDataType("date224");
//		sft.setRegex("^(\\d{4,4}(\\.\\d{2,2}){2,2})|(\\d{4,4}(\\/\\d{2,2}){2,2})|(\\d{4,4}(\\-\\d{2,2}){2,2})(\\s|$)");
		sft1 = sft;
		
	}
	
	public void test1_422(){
		
		IParseResult result = sft1.Parse("2013.22.21");
		assertNotNull(result);
		assertEquals("2013.22.21", result.getParsedToken());

		result = sft1.Parse("2013/22/21");
		assertNotNull(result);
		assertEquals("2013/22/21", result.getParsedToken());

		result = sft1.Parse("2013-22-21");
		assertNotNull(result);
		assertEquals("2013-22-21", result.getParsedToken());
	}

	public void test1_224(){
		
		IParseResult result = sft1.Parse("22.21.2013");
		assertNotNull(result);
		assertEquals("22.21.2013", result.getParsedToken());
		
		result = sft1.Parse("22/21/2013");
		assertNotNull(result);
		assertEquals("22/21/2013", result.getParsedToken());
		
		result = sft1.Parse("22-21-2013");
		assertNotNull(result);
		assertEquals("22-21-2013", result.getParsedToken());
	}

	public void test2(){
		
		IParseResult result = sft1.Parse("523.22.21kjsdsflkjdsf");
		assertNotNull(result);
		assertEquals("2013.22.21", result.getParsedToken());
	}
	
	public void test3(){
		
		IParseResult result = sft1.Parse("22.21.3023.kjsdsflkjdsf");
		assertNotNull(result);
		assertEquals("22.21.3023", result.getParsedToken());
	}

}
