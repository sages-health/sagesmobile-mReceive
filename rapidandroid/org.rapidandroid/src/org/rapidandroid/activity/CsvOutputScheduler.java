/**
 * 
 */
package org.rapidandroid.activity;
import org.rapidandroid.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Scheduling activity for automatic csv output whenever an sms is received matching the prefix
 * for the registered form(s)
 * 
 * @author Adjoa Poku adjoa.poku@jhuapl.edu
 * @created Mar 23, 2011
 */
public class CsvOutputScheduler extends Activity {
	    private SharedPreferences preferences;
	    public static final String TOGGLE_VAR = "_isAutoCsvOn";
	    public static final String FREQUENCY_VAR = "_autoCsvFrequency";
	    public static final String sharedPreferenceFilename = "RapidAndroidSettings";
	    
//TODO IF FORM IS DELETED, WE WOULD NEED TO BLOW THESE VALUES OUT OF THE SharedPreferences file.
	    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("RapidAndroid :: Schedule CSV Output");
        setContentView(R.layout.schedule_csv_output);
        Bundle extras = getIntent().getExtras();
        
        // load frequency value from SharedPrefernces
        preferences = getSharedPreferences(sharedPreferenceFilename, MODE_PRIVATE);
        final int formId = extras.getInt(FormReviewer.CallParams.REVIEW_FORM);
        boolean isAutoCsvOn = preferences.getBoolean(formId + TOGGLE_VAR, false);
        int autoCsvFrequency = preferences.getInt(formId + FREQUENCY_VAR, 1);
        
        
        final EditText frequencyTextField = (EditText)findViewById(R.id.etx_outputFreq);
        frequencyTextField.setText(String.valueOf(autoCsvFrequency));
        frequencyTextField.addTextChangedListener(new TextWatcher() {

        	final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle);
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
        
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle);
        toggle.setChecked(isAutoCsvOn);
        toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// toggle on/off if the csv output should be automatic 
				System.out.println("on checked changed for toggle.");
				System.out.println("arg: " + arg1);
				toggle.setChecked(arg1);
				Editor editor = preferences.edit();
				editor.putBoolean(formId + TOGGLE_VAR, arg1);
				editor.commit();
			}
		});
    
        
        Button updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// set frequency value into UserSettings
				Log.d("Button.CsvOutputScheduler","saving frequency to user settings.");
				Log.d("Button.CsvOutputScheduler", "arg: " + view);
				EditText frequencyVal = (EditText)findViewById(R.id.etx_outputFreq);
				Editor editor = preferences.edit();
				editor.putInt(formId + FREQUENCY_VAR, Integer.parseInt(frequencyVal.getText().toString()));
				editor.commit();
				
				Toast.makeText(getApplicationContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
			}
		});
    }
}
