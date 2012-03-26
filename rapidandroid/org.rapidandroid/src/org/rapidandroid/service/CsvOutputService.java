/*
 * Copyright (©) 2011 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid.service;

import java.io.File;
import java.io.FilenameFilter;

import org.rapidandroid.activity.CsvOutputScheduler;
import org.rapidandroid.activity.Dashboard;
import org.rapidandroid.activity.FormReviewer;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.data.SmsDbHelper;
import org.rapidsms.java.core.model.Form;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author Adjoa Poku
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
			File destinationdir = new File(sdcard, "rapidandroid/exports");
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
		//TODO POKU remove the "in use indicator file"
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
