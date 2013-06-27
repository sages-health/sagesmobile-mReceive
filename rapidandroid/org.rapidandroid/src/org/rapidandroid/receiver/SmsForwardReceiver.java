/********************************************************************************
* Copyright (c) 2013 The Johns Hopkins University/Applied Physics Laboratory
*                              All rights reserved.
*                    
* This material may be used, modified, or reproduced by or for the U.S. 
* Government pursuant to the rights granted under the clauses at             
* DFARS 252.227-7013/7014 or FAR 52.227-14.
*                     
* Licensed under the Apache License, Version 2.0 (the "License");            
* you may not use this file except in compliance with the License.           
* You may obtain a copy of the License at                                    
*                                                                            
*     http://www.apache.org/licenses/LICENSE-2.0                             
*                                                                            
* NO WARRANTY.   THIS MATERIAL IS PROVIDED "AS IS."  JHU/APL DISCLAIMS ALL
* WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT
* LIMITED TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE,
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF
* INTELLECTUAL PROPERTY RIGHTS. ANY USER OF THE MATERIAL ASSUMES THE ENTIRE
* RISK AND LIABILITY FOR USING THE MATERIAL.  IN NO EVENT SHALL JHU/APL BE
* LIABLE TO ANY USER OF THE MATERIAL FOR ANY ACTUAL, INDIRECT,     
* CONSEQUENTIAL, SPECIAL OR OTHER DAMAGES ARISING FROM THE USE OF, OR    
* INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT LIMITED TO, ANY DAMAGES
* FOR LOST PROFITS.
********************************************************************************/
package org.rapidandroid.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;

/**
 * @author sages
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
