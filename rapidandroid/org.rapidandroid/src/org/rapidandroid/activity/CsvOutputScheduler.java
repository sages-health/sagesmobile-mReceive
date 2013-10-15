/*
 * Copyright (©) 2011 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid.activity;
import org.rapidandroid.R;
import org.rapidandroid.RapidAndroidApplication;
import org.rapidandroid.SystemHealthTracking;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Scheduling activity for automatic csv output whenever an sms is received matching the prefix
 * for the registered form(s)
 * 
 * NOTE: If this runs on schedule REALISE THAT IT WILL: delete the old csv files for this form - don't want them to accumulate  on sdcard
 * 
 * @author Adjoa Poku adjoa.poku@jhuapl.edu
 * @created Mar 23, 2011
 */
public class CsvOutputScheduler extends Activity {
		private static SystemHealthTracking healthTracker = new SystemHealthTracking(CsvOutputScheduler.class);
	    
		private SharedPreferences preferences;
	    public static final String TOGGLE_VAR = "_isAutoCsvOn";
	    public static final String FREQUENCY_VAR = "_autoCsvFrequency";
	    public static final String DELETE_VAR = "_isDeleteOn";
	    public static final String FORWARDING_VAR = "_isForwardingOn";
	    public static final String FORWARDING_NUMS = "_forwardingNums";
	    public static final String sharedPreferenceFilename = "RapidAndroidSettings";
	    
		public static final String t = CsvOutputScheduler.class.getSimpleName();

		private static final int MENU_DONE = Menu.FIRST;
//TODO POKUAM1 - IF FORM IS DELETED, WE WOULD NEED TO BLOW THESE VALUES OUT OF THE SharedPreferences file.
	    
    @SuppressLint("CutPasteId")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + ":: CSV Scheduler & Forwarding");
        setContentView(R.layout.schedule_csv_output);
        Bundle extras = getIntent().getExtras();
        
        // load frequency value from SharedPrefernces
//        preferences = getSharedPreferences(sharedPreferenceFilename, MODE_PRIVATE);
        preferences = RapidAndroidApplication.loadPreferences(this);
        final int formId = extras.getInt(FormReviewer.CallParams.REVIEW_FORM);
        boolean isAutoCsvOn = preferences.getBoolean(formId + TOGGLE_VAR, false);
        int autoCsvFrequency = preferences.getInt(formId + FREQUENCY_VAR, 55);
        //boolean isDeleteOn = preferences.getBoolean(formId + DELETE_VAR, false);
        boolean isForwardingOn = preferences.getBoolean(formId + FORWARDING_VAR, false);
        String forwardingNums = preferences.getString(formId + FORWARDING_NUMS, "");
        
        final EditText forwardingNumsTextField = (EditText)findViewById(R.id.etx_forwardingNums);
        forwardingNumsTextField.setText(forwardingNums);
        
        CheckBox forwardingCheck = (CheckBox) findViewById(R.id.chk_forwardingNums);
        forwardingCheck.setChecked(isForwardingOn);
        forwardingCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				forwardingNumsTextField.setEnabled(isChecked);
				Editor editor = preferences.edit();
				editor.putBoolean(formId + FORWARDING_VAR, isChecked);
				editor.commit();
				healthTracker.logDebug("SMS Forwarding enabled: " + isChecked);
			}
		});
        final EditText frequencyTextField = (EditText)findViewById(R.id.etx_outputFreq);
        frequencyTextField.setText(String.valueOf(autoCsvFrequency));
        frequencyTextField.addTextChangedListener(new TextWatcher() {

        	final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle_csv_output);
        	final Button update = (Button) findViewById(R.id.updateButton);
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// nothing to do
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// nothing to do
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// validate that value is 1 to 55
				String strvalue = arg0.toString();
				Integer intvalue;
				boolean exceptionOccured = false;
				try {
					intvalue = Integer.valueOf(strvalue);
					if (intvalue > 55 || intvalue < 1){
						toggle.setEnabled(false);
						update.setEnabled(false);
					} else {
						toggle.setEnabled(true);
						update.setEnabled(true);
					}
				} catch (NumberFormatException e) {
					exceptionOccured = true;
				}
				if (exceptionOccured) {
					toggle.setEnabled(false);
					update.setEnabled(false);
				}
			}
		});
        
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle_csv_output);
        toggle.setChecked(isAutoCsvOn);
        toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				// toggle on/off if the csv output should be automatic 
				System.out.println("on checked changed for toggle.");
				System.out.println("arg: " + isChecked);
				toggle.setChecked(isChecked);
				Editor editor = preferences.edit();
				editor.putBoolean(formId + TOGGLE_VAR, isChecked);
				editor.commit();
				healthTracker.logDebug("Automated CSV output enabled: " + isChecked);
			}
		});
 
        Button updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// set frequency value into UserSettings
				Log.d("Button."+t,"saving frequency to user settings.");
				Log.d("Button."+t, "arg: " + view);
				EditText frequencyVal = (EditText)findViewById(R.id.etx_outputFreq);
//		        EditText forwardingNumsTextField = (EditText)findViewById(R.id.etx_forwardingNums);
				Editor editor = preferences.edit();
				editor.putInt(formId + FREQUENCY_VAR, Integer.parseInt(frequencyVal.getText().toString()));
				editor.putString(formId + FORWARDING_NUMS, forwardingNumsTextField.getText().toString());
				editor.commit();
				
				Toast.makeText(getApplicationContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
			}
		});
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_DONE, 0, R.string.formreview_menu_done).setIcon(android.R.drawable.ic_menu_close_clear_cancel);		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case MENU_DONE:
				finish();
				healthTracker.logInfo("Settings saved. Leaving " + t);
				break;
		}
		return true;
	}
}
