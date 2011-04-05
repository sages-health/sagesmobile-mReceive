package org.rapidandroid.service;

import java.io.File;
import java.io.FilenameFilter;

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
 * @author Adjoa Poku
 * @created March 22, 2011
 * 
 * 		Configurable Service that outputs CSV files without doing so through the FormViewer 
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
		Log.d("CsvOutputService","Poku's service is being started");
		//TODO create the "in use indicator file"
		preferences = getSharedPreferences("RapidAndroidSettings", MODE_PRIVATE);
		if (intent != null){
			Log.d("CsvOutputService","Poku's service is being started: " + intent.getAction());
		}
		int formId = intent.getExtras().getInt("formId");
		String formPrefix = intent.getExtras().getString("formPrefix");
		
		if (preferences.getBoolean(formId + "_isAutoCsvOn", false)) {
			
			// delete the old csv files for this form
			File sdcard = Environment.getExternalStorageDirectory();
			File destinationdir = new File(sdcard, "rapidandroid/exports");
			File[] files = destinationdir.listFiles(new FormPrefixFilter(formPrefix));
			for (File file: files) {
				file.delete();
			}
			super.onStart(intent, startId);
		} else {
			stopSelf();
		}
	}
	
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
			// TODO POKU Auto-generated method stub
			return filename.startsWith(formPrefix);
		}}
	
	
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
		
		Log.d("CsvOutputService.onHandleIntent", form.getFormId() + "_isAutoCsvOn: " + isAutoCsvOn);
		Log.d("CsvOutputService.onHandleIntent", form.getFormId() + "_autoCsvFrequency: " + autoCsvFrequency);
		
		// service runs for a minute once triggered
		long endTime = System.currentTimeMillis() + 60*1000;
		while(System.currentTimeMillis() < endTime) {
			synchronized (this) {
				try {
					FormReviewer reviewer = new FormReviewer();
					reviewer.outputCSV(form.getFormId());
					wait(autoCsvFrequency * 1000);
				} catch (Exception e){
					
				}
			}
		}
		//TODO POKU remove the "in use indicator file"
		Log.d("CsvOutputService","completed running and ran at intervals of " + autoCsvFrequency + " seconds.");
	}
}
