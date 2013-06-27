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
package org.rapidandroid.service;

import java.io.File;
import java.io.FilenameFilter;

import org.rapidandroid.RapidAndroidApplication;
import org.rapidandroid.activity.CsvOutputScheduler;
import org.rapidandroid.activity.FormReviewer;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidsms.java.core.model.Form;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author sages
 * @created March 22, 2011
 * 
 * Configurable Service that automatically outputs CSV files upon successful parsing of an SMS into rapidandroid. 
 */
public class CsvOutputService extends IntentService {

	private SharedPreferences preferences;
	
	/**
	 * @param name
	 */
	public CsvOutputService() {
		super("CsvOutputService");
	}
	
	@Override
	public void onStart(Intent intent, int startId){
		Log.d("CsvOutputService","service is being started");
		//TODO create the "in use indicator file"
		preferences = getSharedPreferences("RapidAndroidSettings", MODE_PRIVATE);

		int formId = intent.getExtras().getInt("formId");
		String formPrefix = intent.getExtras().getString("formPrefix");
		
		if (preferences.getBoolean(formId + "_isAutoCsvOn", false)) {
			
			// delete the old csv files for this form - don't want them to accumulate  on sdcard
			File sdcard = Environment.getExternalStorageDirectory();
			String state = Environment.getExternalStorageState();
			File destinationdir = new File(sdcard, RapidAndroidApplication.DIR_RAPIDANDROID_EXPORTS + "/"+ formPrefix + "_exports");
			File[] files = destinationdir.listFiles(new FormPrefixFilter(formPrefix));
			
			if (files != null) {
				for (File file: files) {
					file.delete();
				}
			}
			super.onStart(intent, startId);
		} else {
			stopSelf();
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("CsvOutputService","Now being handled: " + intent.getAction());
		Form form = ModelTranslator.getFormById(intent.getExtras().getInt("formId"));

		preferences = getSharedPreferences(CsvOutputScheduler.sharedPreferenceFilename, MODE_PRIVATE);
		boolean isAutoCsvOn = preferences.getBoolean(form.getFormId() + CsvOutputScheduler.TOGGLE_VAR, false);
		int autoCsvFrequency = preferences.getInt(form.getFormId() + CsvOutputScheduler.FREQUENCY_VAR, 1);
		//boolean isDeleteOn = preferences.getBoolean(form.getFormId() + CsvOutputScheduler.DELETE_VAR, false);
		
		Log.d("CsvOutputService.onHandleIntent", form.getFormId() + "_isAutoCsvOn: " + isAutoCsvOn);
		Log.d("CsvOutputService.onHandleIntent", form.getFormId() + "_autoCsvFrequency: " + autoCsvFrequency);
		
		// service runs for a minute once triggered
		long endTime = System.currentTimeMillis() + 60*1000;
		while(System.currentTimeMillis() < endTime) {
			synchronized (this) {
				try {
					FormReviewer reviewer = new FormReviewer();
					reviewer.outputCSV(form.getFormId());

/*					
 * 			seems like a bad idea...needs more thought
 * 						if (isDeleteOn){
						SmsDbHelper dbHelper = new SmsDbHelper(this);
						final SQLiteDatabase db = dbHelper.getWritableDatabase();
						final String table = "formdata_" + form.getPrefix();
						final String whereClause = null;
						final String[] whereArgs = null;
						
						int rows = db.delete(table, whereClause, whereArgs);
						Log.d("CsvOutputService", "deleted " + rows + " records from the form: " + form.getPrefix());
					}*/
					
					wait(autoCsvFrequency * 1000);
				} catch (Exception e){
					
				}
			}
		}
		Log.d("CsvOutputService","completed running and ran at intervals of " + autoCsvFrequency + " seconds.");
	}
	
	/**
	 * Filters files based on file names. Recall file names are based on the form prefix
	 * 
	 * @author Adjoa Poku adjoa.poku@jhuapl.edu
	 * @created Apr 1, 2011
	 */
	public class FormPrefixFilter implements FilenameFilter {

		String formPrefix;
		public FormPrefixFilter(String formPrefix){
			this.formPrefix = "formdata_" + formPrefix;
		}
		
		/* (non-Javadoc)
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		@Override
		public boolean accept(File dir, String filename) {
			return filename.startsWith(formPrefix);
		}}
}
