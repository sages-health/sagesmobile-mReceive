package org.rapidandroid.test;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.NoSuchPaddingException;

import edu.jhuapl.sages.mobile.lib.SharedObjects;
import edu.jhuapl.sages.mobile.lib.crypto.engines.CryptoEngine;
import edu.jhuapl.sages.mobile.lib.odk.SagesOdkMessage;
import android.telephony.SmsManager;
import android.test.AndroidTestCase;

public class SmsStressTest extends AndroidTestCase {

	private SagesOdkMessage som;
	private SharedObjects so;
	private SmsManager mgr;
	
	private String kdf_plain = "kdf . 0 PLAINTEXTnnmmmmmmmmmmmmmmmmMmmmnnnnnnnmmmmmmmmmmmmmmmmMmmmnnnnnnnmmmmmmmmmmmmmmmmMmmmnnnnnnnmmmmmmmmmmmmmmmmMmmmnnnnnnnmmmmmmmmmmmmmmmm 2013-05-20 1 0 1 2 99 58.0 58 36 88 45.0 ' 1 2 3 4 5 6 7 8 ' 1 ' 1 2 3 4 5 6 7 8 9 10 11 12 13 '";
	private String kdf_encrypted = "kdf . 0 CIPHERTEXTnmmmmmmmmmmmmmmmmMmmmnnnnnnnmmmmmmmmmmmmmmmmMmmmnnnnnnnmmmmmmmmmmmmmmmmMmmmnnnnnnnmmmmmmmmmmmmmmmmMmmmnnnnnnnmmmmmmmmmmmmmmmm 2013-05-20 1 0 1 2 99 58.0 58 36 88 45.0 ' 1 2 3 4 5 6 7 8 ' 1 ' 1 2 3 4 5 6 7 8 9 10 11 12 13 '";
	
	private int globalStress;
	
	public SmsStressTest() {
		globalStress = 300;
		som = new SagesOdkMessage("");
		mgr = SmsManager.getDefault();
		
		try {
			so = new SharedObjects();
//			SharedObjects.test_updateCryptoEngine("1234567890123456");
			SharedObjects.test_updateCryptoEngine("PASSWORD12345678");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void notestStressPlain(){
		String payload = kdf_plain;
		int stress = globalStress;
		som.configure(false);
		
		HashMap<Integer, String> batch = buildBatch(payload, stress);
		//smsSendBatch(batch,"+12404638748");
	}

	
	public void testStressEncrypted(){
		
		String payload = kdf_encrypted;
		int stress = globalStress;
		som.configure(true);

		HashMap<Integer, String> batch = buildBatch(payload, stress);
		
		/*
		int a = 1;
		
		HashMap<Integer, ArrayList<String>> processedBatch = new HashMap<Integer, ArrayList<String>>();
		while (a <= batch.size()){
			som.testHook(batch.get(a));
			ArrayList<String> blobs = som.getDividedBlob();
			processedBatch.put(a, blobs);
			a++;
		}*/
		
		smsSendBatch(batch,"+12404638748");
	}
	
	/**
	 * @param batch
	 */
	protected void smsSendBatch(HashMap<Integer, String> batch, String phoneNumber) {
		for (int j=0; j<batch.size(); j++){
			som.testHook(batch.get(j+1));
			ArrayList<String> blobs = som.getDividedBlob();
			
			
			for (int k=0; k<blobs.size(); k++){
				String sms = blobs.get(k);
				mgr.sendTextMessage(phoneNumber, null, sms, null, null);
			}
		}
	}

	/**
	 * @param payload
	 * @param stress
	 * @return
	 */
	protected HashMap<Integer, String> buildBatch(String payload, int stress) {
		HashMap<Integer, String> batch = new HashMap<Integer, String>();
		
		for (int i = 0; i < stress; i++){
			payload = payload.replaceFirst(i + "", i + 1 + "");
			batch.put(Integer.valueOf(i+1), payload);
		}
		return batch;
	}
}
