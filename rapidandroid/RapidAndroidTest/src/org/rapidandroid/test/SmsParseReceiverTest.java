/**
 * 
 */
package org.rapidandroid.test;

import org.rapidandroid.receiver.SmsParseReceiver;

import android.content.Intent;
import android.test.AndroidTestCase;

/**
 * @author powelnv1
 *
 */
public class SmsParseReceiverTest extends AndroidTestCase {
SmsParseReceiver mReceiver;
Intent intent;
	public SmsParseReceiverTest(String name){
		setName(name);
	}
	
	public void setUp() throws Exception{
		super.setUp();
		mReceiver = new SmsParseReceiver();
	}
	
	public void testSmsParseReceiver(){
		//FakeSms mFakeSms = new FakeSms("5554","bednets test300 23 14 15");
		Intent intent = new Intent(Intent.EXTRA_TEXT);
		intent.putExtra("body", "bednets test2000 23 45 68");
		intent.putExtra("from", "5554");
		mReceiver.onReceive(getContext(), intent);
		
	}
}
