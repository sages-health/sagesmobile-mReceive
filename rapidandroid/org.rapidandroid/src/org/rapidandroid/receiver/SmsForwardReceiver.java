/*
 * Copyright (©) 2011 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;

/**
 * @author Adjoa Poku adjoa.poku@jhuapl.edu
 * @created May 11, 2011
 */
public class SmsForwardReceiver extends SmsReplyReceiver {

    /*
     * (non-Javadoc)
     * @see org.rapidandroid.receiver.SmsReplyReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("org.rapidandroid.intents.SMS_FORWARD")) {
            throw new RuntimeException();
        }
        SmsManager smgr = SmsManager.getDefault();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(this.getClass().getName(), "SMS forwarding in progress");
            String mesg = extras.getString("msg");
            String[] fwdnums = extras.getStringArray("forwardNums");
            for (String destinationAddr : fwdnums) {
                // String destinationAddr = extras.getString(KEY_DESTINATION_PHONE);
                // String mesg = extras.getString(KEY_MESSAGE);
                smgr.sendTextMessage(destinationAddr, null, mesg, null, null);
            }
            Log.d(this.getClass().getName(), "SMS forwarding complete");
        }

    }
}
