/*
 * Copyright (©) 2011 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid.receiver;

import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidsms.java.core.model.Form;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Starts the CsvOutputService that outputs csv files for the associated form
 * 
 * @author POKUAM1
 * @created Mar 24, 2011
 */
public class CsvReceiver extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		if (!intent.getAction().equals("org.rapidandroid.intents.SMS_REPLY_CSV_GO")) {
			throw new RuntimeException();
		}
		
		Form form = ModelTranslator.getFormById(intent.getExtras().getInt("formId"));
		Intent service = new Intent("org.rapidandroid.service.CsvOutputService");
		service.putExtra("formId", form.getFormId());
		service.putExtra("formName", form.getFormName());
		service.putExtra("formPrefix", form.getPrefix());
		
		context.startService(service);
		
	}
	
}
