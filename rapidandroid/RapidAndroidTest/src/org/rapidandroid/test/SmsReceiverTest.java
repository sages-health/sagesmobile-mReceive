/**
 * 
 */
package org.rapidandroid.test;

import org.rapidandroid.receiver.SmsParseReceiver;
import org.rapidandroid.receiver.SmsReceiver;

import android.content.Intent;
import android.test.AndroidTestCase;

/**
 * @author powelnv1
 * 
 */
public class SmsReceiverTest extends AndroidTestCase {
	public static Intent intent;
	SmsReceiver mReceiver;

	/**
	 * @param name
	 */
	public SmsReceiverTest(String name) {
		setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.AndroidTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		mReceiver = new SmsReceiver();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.AndroidTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testSmsReceiverOnReceive() {
		// Send fake sms message with pdu object
FakeSms mSmsMessage= new FakeSms("5554", "bednets test20 20 20 1");
		// call SmsReceiver onReceive method to test receiver
		mReceiver.onReceive(getContext(), mSmsMessage.getIntent());
	}

	


}
