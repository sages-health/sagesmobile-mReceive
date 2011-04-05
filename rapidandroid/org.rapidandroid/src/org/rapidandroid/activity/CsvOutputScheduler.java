/**
 * 
 */
package org.rapidandroid.activity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.rapidandroid.R;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidsms.java.core.model.Form;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

/**
 * @author POKUAM1
 * @created Mar 23, 2011
 */
public class CsvOutputScheduler extends Activity {
	    private SharedPreferences preferences;
	    public static final String TOGGLE_VAR = "_isAutoCsvOn";
	    public static final String FREQUENCY_VAR = "_autoCsvFrequency";
	    public static final String sharedPreferenceFilename = "RapidAndroidSettings";
	    
//TODO IF FORM IS DELETED, WE WOULD NEED TO BLOW THESE VALUES OUT OF THE SCHEDULER.
	    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The activity is being created.
        setTitle("RapidAndroid :: Schedule CSV Output");
        setContentView(R.layout.schedule_csv_output);
        Bundle extras = getIntent().getExtras();
        
        // load frequency value from SharedPrefernces
        preferences = getSharedPreferences(sharedPreferenceFilename, MODE_PRIVATE);
        final int formId = extras.getInt(FormReviewer.CallParams.REVIEW_FORM);
        boolean isAutoCsvOn = preferences.getBoolean(formId + TOGGLE_VAR, false);
        int autoCsvFrequency = preferences.getInt(formId + FREQUENCY_VAR, 1);
        
        
        final EditText frequencyTextField = (EditText)findViewById(R.id.editText1);
        frequencyTextField.setText(String.valueOf(autoCsvFrequency));
        
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
        toggle.setChecked(isAutoCsvOn);
        toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// toggle on/off if the output should be automatic 
				System.out.println("on checked changed for toggle.");
				System.out.println("arg: " + arg1);
				toggle.setChecked(arg1);
				Editor editor = preferences.edit();
				editor.putBoolean(formId + TOGGLE_VAR, arg1);
				editor.commit();
			}
		});
    
        
        Button updateButton = (Button) findViewById(R.id.button1);
        updateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// set frequency value into UserSettings
				System.out.println("saving frequency to user settings.");
				Log.d("Button.CsvOutputScheduler", "arg: " + view);
				EditText frequencyVal = (EditText)findViewById(R.id.editText1);
				Editor editor = preferences.edit();
				editor.putInt(formId + FREQUENCY_VAR, Integer.parseInt(frequencyVal.getText().toString()));
				editor.commit();
			}
		});
    }
    
    @Override
    //TODO  
    protected void onPause() {
    	super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
        System.out.println("this activity is about to be 'paused'");
        
    }
}
