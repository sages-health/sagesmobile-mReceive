/**
 * 
 */
package org.rapidandroid.tests;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.controller.WorktableDataLayer;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;
import edu.jhuapl.sages.mobile.lib.rapidandroid.Demodulator;

/**
 * @author POKUAM1
 * @created Feb 13, 2012
 */
public class SagesMultiSmsTest extends AndroidTestCase {

//	private Uri currUri;
	
	private void rollCalDate(Calendar cal, int day, int hour, int min, int sec){
		cal.roll(Calendar.DATE, day);
		cal.roll(Calendar.HOUR, hour);
		cal.roll(Calendar.MINUTE, min);
		cal.roll(Calendar.SECOND, sec);
	}
	
	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		@SuppressWarnings("unused") // for debug
		int val = getContext().getContentResolver().delete(RapidSmsDBConstants.MultiSmsWorktable.CONTENT_URI, "tx_id >= ?", new String[] {"100"});
		Calendar c = new GregorianCalendar();
		final Calendar cRef = c;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		//c.roll(Calendar.MINUTE, 10);
		//c.roll(Calendar.SECOND, 20);
//		String dateStr = sdf.format(c.getTime()); 
//		
//
//		
//		rollCalDate(c, 10, 10, 10, 10);
//		String dateStr1 = sdf.format(c.getTime()); 
//		c = cRef;
//
//		rollCalDate(c, 20, 20, 20, 20);
//		String dateStr2 = sdf.format(c.getTime()); 
//		c = cRef;
//		
//		rollCalDate(c, 30, 30, 30, 30);
//		String dateStr3 = sdf.format(c.getTime()); 
//		c = cRef;
		
		/** LIVE - complete set of 5 messages, received in order sequentially, within 4 minutes of "now" **/
		//insertMultiSmsPart(31,1,5,100, "2012-02-09 15:17:28", "1of5_txid_100");
		rollCalDate(c, 0, 0, 0, 0);
		insertMultiSmsPart(/*31,*/1,5,100, sdf.format(c.getTime()), "1of5_txid_100");
		c = (Calendar) cRef.clone();
		
		//insertMultiSmsPart(32,2,5,100, "2012-02-09 15:18:28", "2of5_txid_100");
		rollCalDate(c, 0, 0, -1, 0);
		insertMultiSmsPart(/*32,*/2,5,100, sdf.format(c.getTime()), "2of5_txid_100");
		c = (Calendar) cRef.clone();

		//insertMultiSmsPart(33,3,5,100, "2012-02-09 15:19:28", "3of5_txid_100");
		rollCalDate(c, 0, 0, -2, 0);
		insertMultiSmsPart(/*33,*/3,5,100, sdf.format(c.getTime()), "3of5_txid_100");
		c = (Calendar) cRef.clone();
		
		//insertMultiSmsPart(34,4,5,100, "2012-02-09 15:20:28", "4of5_txid_100");
		rollCalDate(c, 0, 0, -3, 0);
		insertMultiSmsPart(/*34,*/4,5,100, sdf.format(c.getTime()), "4of5_txid_100");
		c = (Calendar) cRef.clone();

		//insertMultiSmsPart(35,5,5,100, "2012-02-09 15:21:28", "5of5_txid_100");
		rollCalDate(c, 0, 0, -4, 0);
		insertMultiSmsPart(/*35,*/5,5,100, sdf.format(c.getTime()), "5of5_txid_100");
		c = (Calendar) cRef.clone();

		/** LIVE - complete set of 3 messages, received out of order, within 2 days 5 minutes of "now" **/
		//insertMultiSmsPart(36,1,3,111, "2012-02-09 15:21:28", "1of3_txid_111");
		rollCalDate(c, 0, 0, -4, 0);
		insertMultiSmsPart(/*36,*/1,3,111, sdf.format(c.getTime()), "1of3_txid_111");
		c = (Calendar) cRef.clone();

		//insertMultiSmsPart(37,3,3,111, "2012-02-11 15:21:28", "3of3_txid_111");
		rollCalDate(c, -2, 0, -4, 0);
		insertMultiSmsPart(/*37,*/3,3,111, sdf.format(c.getTime()), "3of3_txid_111");
		c = (Calendar) cRef.clone();

		//insertMultiSmsPart(38,2,3,111, "2012-02-11 15:22:28", "2of3_txid_111");
		rollCalDate(c, -2, 0, -5, 0);
		insertMultiSmsPart(/*38,*/2,3,111, sdf.format(c.getTime()), "2of3_txid_111");
		c = (Calendar) cRef.clone();

		/** STALE - incomplete set 2 of 3 messages, received out of order, within 3 days 14 minutes of "now" **/
		//insertMultiSmsPart(39,1,3,333, "2012-02-13 15:30:28", "1of3_txid_333");
		rollCalDate(c, -3, 0, -13, 0);
		insertMultiSmsPart(/*39,*/1,3,333, sdf.format(c.getTime()), "1of3_txid_333");
		c = (Calendar) cRef.clone();

		//insertMultiSmsPart(40,3,3,333, "2012-02-13 15:31:28", "3of3_txid_333");
		rollCalDate(c, -3, 0, -14, 0);
		insertMultiSmsPart(/*40,*/3,3,333, sdf.format(c.getTime()), "3of3_txid_333");
		c = (Calendar) cRef.clone();
		
		/** LIVE - complete set of 3 messages, received out of order, within seconds of "now" **/
		rollCalDate(c, 0, 0, 0, -1);
		insertMultiSmsPart(/*36,*/1,3,4000, sdf.format(c.getTime()), "1of3_txid_4000");
		c = (Calendar) cRef.clone();

		rollCalDate(c, 0, 0, 0, -2);
		insertMultiSmsPart(/*37,*/3,3,4000, sdf.format(c.getTime()), "3of3_txid_4000");
		c = (Calendar) cRef.clone();

		rollCalDate(c, 0, 0, 0, -3);
		insertMultiSmsPart(/*38,*/2,3,4000, sdf.format(c.getTime()), "2of3_txid_4000");
		c = (Calendar) cRef.clone();

		/** LIVE - complete set of 3 messages, received out of order, within seconds of "now" **/
		rollCalDate(c, 0, 0, 0, -1);
		insertMultiSmsPart(/*36,*/3,3,4001, sdf.format(c.getTime()), "3of3_txid_4001");
		c = (Calendar) cRef.clone();
		
		rollCalDate(c, 0, 0, 0, -2);
		insertMultiSmsPart(/*37,*/2,3,4001, sdf.format(c.getTime()), "2of3_txid_4001");
		c = (Calendar) cRef.clone();
		
		rollCalDate(c, 0, 0, 0, -3);
		insertMultiSmsPart(/*38,*/1,3,4001, sdf.format(c.getTime()), "1of3_txid_4001");
		c = (Calendar) cRef.clone();
	}

	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		@SuppressWarnings("unused") // for debug
		int val = getContext().getContentResolver().delete(RapidSmsDBConstants.MultiSmsWorktable.CONTENT_URI, "tx_id >= ?", new String[] {"100"});
	}

	private void insertMultiSmsPart(/*int _id,*/ int segNum, int totSegs, long tx_id, String tx_ts, String payload) {
		ContentValues initialValues = new ContentValues();
		//initialValues.put(RapidSmsDBConstants.MultiSmsWorktable._ID, _id);
		initialValues.put(RapidSmsDBConstants.MultiSmsWorktable.SEGMENT_NUMBER, segNum);
		initialValues.put(RapidSmsDBConstants.MultiSmsWorktable.TOTAL_SEGMENTS, totSegs);
		initialValues.put(RapidSmsDBConstants.MultiSmsWorktable.TX_ID, tx_id);
		initialValues.put(RapidSmsDBConstants.MultiSmsWorktable.TX_TIMESTAMP, tx_ts);
		initialValues.put(RapidSmsDBConstants.MultiSmsWorktable.PAYLOAD, payload);
		getContext().getContentResolver().insert(RapidSmsDBConstants.MultiSmsWorktable.CONTENT_URI, initialValues);
	}

	public void testCategorizeCompleteVsIncomplete(){
		try {
			WorktableDataLayer.setTimerThreshold(300);
			Map<String, List<Long>> statusMap = WorktableDataLayer.categorizeCompleteVsIncomplete(getContext());
			assertNotNull(statusMap);
			List<Long> ids = Arrays.asList(new Long[]{100L, 111L, 4000L, 4001L});
			assertTrue("Did not contain expected tx_ids",statusMap.get(WorktableDataLayer.label_complete).containsAll(ids));
			assertEquals("Incorrect categorization of completes", ids, statusMap.get(WorktableDataLayer.label_complete));
			
			ids = Arrays.asList(new Long[]{333L});
			assertTrue(statusMap.get(WorktableDataLayer.label_incomplete).containsAll(ids));
			assertEquals("Incorrect categorization of incompletes", ids, statusMap.get(WorktableDataLayer.label_incomplete));
			
			assertTrue(statusMap.get(WorktableDataLayer.label_bad).isEmpty());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}
	
	public void testGetStaleVsLiveTxIds(){
		Cursor cursor = WorktableDataLayer.getStaleVsLiveTxIds(getContext(), null);
		while (cursor.moveToNext()){
			System.out.println(cursor.getString(0)); //ttl_timer
			System.out.println(cursor.getString(1)); //now local
			System.out.println(cursor.getString(2)); //DIFF
			System.out.println(cursor.getString(3)); //_id
			System.out.println(cursor.getString(4)); //seg num
			System.out.println(cursor.getString(5)); //tot segs
			System.out.println(cursor.getString(6)); //tx_id
			System.out.println(cursor.getString(7)); //tx_timestamp
			System.out.println(cursor.getString(8)); //payload
		}
		
	}
	public void testCategorizedStaleVsLive(){
		try {
			WorktableDataLayer.setTimerThreshold(300);
			Map<String, List<Long>> ttlMap = WorktableDataLayer.categorizeStaleVsLive(getContext(), null);
			assertNotNull(ttlMap);
			
			List<Long> ids = Arrays.asList(new Long[]{100L,111L,4000L,4001L});
			assertEquals("Wrong number of live data",ids, ttlMap.get(WorktableDataLayer.label_ttlLive));
			
			ids = Arrays.asList(new Long[]{333L});
			assertEquals("Wrong number of stale data", ids,ttlMap.get(WorktableDataLayer.label_ttlStale));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	/**
	 * Tests whether deletion nested in a transaction functions as expected
	 */
	public void testTransactionalDeleteStaleIncompletes(){
		Map<String, List<Long>> ttlMap;
		try {
			
			// known stale txIds
			ttlMap = WorktableDataLayer.categorizeStaleVsLive(getContext(), null);
			List<Long> staleTxIds = ttlMap.get(WorktableDataLayer.label_ttlStale);
			
			assertEquals("Wrong stale txIds categorized", Arrays.asList(new Long[]{333L}), staleTxIds);
			/** the transactional delete is rolled back **/

			// begin sql transaction
			WorktableDataLayer.beginTransaction(getContext());
			
			// delete records with stale txIds and verify gone
			WorktableDataLayer.deleteTxIds(getContext(), staleTxIds);
			Cursor cursor = WorktableDataLayer.getAvailableMessagesForTxId(getContext(), staleTxIds);
			assertTrue(cursor.moveToFirst());
			assertEquals(0, cursor.getInt(0));

			//end transaction without setting successful -- triggers a rollback.
			WorktableDataLayer.endTransaction();
			
			// verify that the deleted records were rolled back
			cursor = WorktableDataLayer.getAvailableMessagesForTxId(getContext(), staleTxIds);
			assertTrue(cursor.moveToFirst());
			assertEquals(2, cursor.getInt(0));
			cursor.close();
			
			/** the transactional delete is committed **/
			
			// begin sql transaction
			WorktableDataLayer.beginTransaction(getContext());
			
			// delete records with stale txIds and verify gone
			WorktableDataLayer.deleteTxIds(getContext(), staleTxIds);
			cursor = WorktableDataLayer.getAvailableMessagesForTxId(getContext(), staleTxIds);
			assertTrue(cursor.moveToFirst());
			assertEquals(0, cursor.getInt(0));

			//set transaction successful and end -- triggers a commit.
			WorktableDataLayer.setTransactionSuccessful();
			WorktableDataLayer.endTransaction();
			
			// verify that the deleted records were rolled back
			cursor = WorktableDataLayer.getAvailableMessagesForTxId(getContext(), staleTxIds);
			assertTrue(cursor.moveToFirst());
			assertEquals(0, cursor.getInt(0));
			cursor.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testConcatCompleteMessags(){
		Map<String, List<Long>> statusMap;
		try {
			statusMap = WorktableDataLayer.categorizeCompleteVsIncomplete(getContext());
			List<Long> txIds =  statusMap.get(WorktableDataLayer.label_complete);
			WorktableDataLayer.getConcatenatedMessagesForTxIds(getContext(), txIds);
			
			Map<Long, String> localBlobMap = WorktableDataLayer.getConcatenatedMessagesForTxIds(getContext(), txIds);
			
			assertEquals(4, localBlobMap.size());
			assertEquals("1of3_txid_1112of3_txid_1113of3_txid_111", localBlobMap.get(111L));
			assertEquals("1of5_txid_1002of5_txid_1003of5_txid_1004of5_txid_1005of5_txid_100", localBlobMap.get(100L));
			assertEquals("1of3_txid_40012of3_txid_40013of3_txid_4001", localBlobMap.get(4001L));
			assertEquals("1of3_txid_40002of3_txid_40003of3_txid_4000", localBlobMap.get(4000L));
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}

	/** mocked blobs and transaction ids**/
	@SuppressWarnings("serial")
	private static final Map<Long, String> blobMap = new HashMap<Long, String>(){{
		put(1L,"#form1 val1a val2a val3a#form1 val1b val2b val3b#form1 val1c val2c val3c");
		put(2L,"#formA a1 a2 a3#formB b1 b2 b3#formC c1 c2 c3");
		put(3L,"#form4 val4a val4a val4a#form5 val5b val5b val5b#form6 val6c val6c val6c#formX x1 x2 x3#formY y1 y2 y3#formZ z1 z2 z3");
	}};
	
	/** mocked demoded blobs in String array to transaction ids **/
	@SuppressWarnings("unused")
	private static final Map<Long, String[]> demodedBlobs = Demodulator.deModulateBlobMap(blobMap);
	String[] demodedBlobs1 = {"form1 val1a val2a val3a","form1 val1b val2b val3b","form1 val1c val2c val3c"};
	String[] demodedBlobs2 = {"formA a1 a2 a3","formB b1 b2 b3","formC c1 c2 c3"};
	String[] demodedBlobs3 = {"form4 val4a val4a val4a","form5 val5b val5b val5b","form6 val6c val6c val6c","formX x1 x2 x3","formY y1 y2 y3","formZ z1 z2 z3"};
	
	public void testDemodulateBlob(){
		Map<Long, String[]> demoded = Demodulator.deModulateBlobMap(blobMap);
		assertEquals("Demodulation was wrong.",StringUtils.join(demodedBlobs1), StringUtils.join(demoded.get(1L)));
		assertEquals("Demodulation was wrong.",StringUtils.join(demodedBlobs2), StringUtils.join(demoded.get(2L)));
		assertEquals("Demodulation was wrong.",StringUtils.join(demodedBlobs3), StringUtils.join(demoded.get(3L)));
	}
}
