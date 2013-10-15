/*
 * Copyright (C) 2009 Dimagi Inc., UNICEF
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.rapidandroid.activity;

import org.json.JSONException;
import org.json.JSONObject;
import org.rapidandroid.ApplicationGlobals;
import org.rapidandroid.R;
import org.rapidandroid.SystemHealthTracking;
import org.rapidandroid.receiver.SmsParseReceiver;

import edu.jhuapl.sages.mobile.lib.RapidAndroidSecuritySetupActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Feb 10, 2009 Summary:
 * 
 * ----------------------------------------------------------------------
 * @author SAGES/pokuam1 - Modifications for parse in progress settings
 */
public class GlobalSettings extends Activity {


	/**
	 * 
	 */
	private static final String ACTIVITY_TITLE_STRING = " :: Global Settings";
	
	/**
	 * 
	 */
	public static final String LOG_DEBUG_KEY = "GlobalSettings";
	
	private static final int MENU_DONE = Menu.FIRST;
	private static final int MENU_SECURITY = 2;
	
	private CheckBox mActiveSwitch;
	private CheckBox mActiveLoggingSwitch;
	private CheckBox mParseInProgressCheckbox;
	private EditText mParseInProgressReplyText;
	private CheckBox mParseCheckbox;
	private EditText mParseReplyText;
	private CheckBox mNoparseCheckBox;
	private EditText mNoparseReplyText;
	private Button mCacheRefreshButton;
	
	
	private OnClickListener mCheckChangeListener = new OnClickListener() {

		public void onClick(View v) {
			if(v.equals(mActiveSwitch)) {
				mParseInProgressCheckbox.setEnabled(mActiveSwitch.isChecked());
				mParseInProgressReplyText.setEnabled(mActiveSwitch.isChecked());
				
				mParseReplyText.setEnabled(mActiveSwitch.isChecked());
				mNoparseReplyText.setEnabled(mActiveSwitch.isChecked());
				
				mParseCheckbox.setEnabled(mActiveSwitch.isChecked());
				mNoparseCheckBox.setEnabled(mActiveSwitch.isChecked());
			}
			
			if (mActiveSwitch.isChecked()) {
				if (v.equals(mParseCheckbox)) {
					mParseReplyText.setEnabled(mParseCheckbox.isChecked());
				} else if (v.equals(mNoparseCheckBox)) {
					mNoparseReplyText.setEnabled(mNoparseCheckBox.isChecked());
				} else if (v.equals(mParseInProgressCheckbox)) {
					mParseInProgressReplyText.setEnabled(mParseInProgressCheckbox.isChecked());
				}
			} 
		}
	};
	
	private OnClickListener mCacheRefreshButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v.equals(mCacheRefreshButton)) {
				SmsParseReceiver.initFormCache();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setTitle(getString(R.string.app_name) + ACTIVITY_TITLE_STRING);
		setContentView(R.layout.global_settings);

		
		mActiveLoggingSwitch = (CheckBox) findViewById(R.id.glb_chk_logging);

		mActiveSwitch = (CheckBox) findViewById(R.id.glb_chk_activeall);
		mActiveSwitch.setOnClickListener(mCheckChangeListener);
		
		mParseInProgressCheckbox = (CheckBox) findViewById(R.id.glb_chk_inprogress);
		mParseInProgressCheckbox.setOnClickListener(mCheckChangeListener);
		this.mParseInProgressReplyText = (EditText) findViewById(R.id.glb_etx_inprogress);
		
		mParseCheckbox = (CheckBox) findViewById(R.id.glb_chk_parse);
		mParseCheckbox.setOnClickListener(mCheckChangeListener);
		this.mParseReplyText = (EditText) findViewById(R.id.glb_etx_success);

		mNoparseCheckBox = (CheckBox) findViewById(R.id.glb_chk_noparse);
		mNoparseCheckBox.setOnClickListener(mCheckChangeListener);
		this.mNoparseReplyText = (EditText) findViewById(R.id.glb_etx_failed);
		
		loadSettingsFromGlobals();
		
		mParseReplyText.setEnabled(mActiveSwitch.isChecked());
		mNoparseReplyText.setEnabled(mActiveSwitch.isChecked());
		mParseInProgressReplyText.setEnabled(mActiveSwitch.isChecked());
		
		mParseCheckbox.setEnabled(mActiveSwitch.isChecked());
		mNoparseCheckBox.setEnabled(mActiveSwitch.isChecked());
		mParseInProgressCheckbox.setEnabled(mActiveSwitch.isChecked());
		
		
		mCacheRefreshButton = (Button) findViewById(R.id.glbsettings_bttn_cache);
		mCacheRefreshButton.setOnClickListener(mCacheRefreshButtonListener);
	}

	/**
	 * 
	 */
	private void loadSettingsFromGlobals() {
		// TODO Auto-generated method stub
		JSONObject globals = ApplicationGlobals.loadSettingsFromFile(this);
		try {
			mActiveLoggingSwitch.setChecked(globals.getBoolean(ApplicationGlobals.KEY_ACTIVE_LOGGING));			
			mActiveSwitch.setChecked(globals.getBoolean(ApplicationGlobals.KEY_ACTIVE_ALL));			
			mParseCheckbox.setChecked(globals.getBoolean(ApplicationGlobals.KEY_PARSE_REPLY));
			mParseReplyText.setText(globals.getString(ApplicationGlobals.KEY_PARSE_REPLY_TEXT));
			mNoparseCheckBox.setChecked(globals.getBoolean(ApplicationGlobals.KEY_FAILED_REPLY));
			mNoparseReplyText.setText(globals.getString(ApplicationGlobals.KEY_FAILED_REPLY_TEXT));
			mParseInProgressCheckbox.setChecked(globals.getBoolean(ApplicationGlobals.KEY_INPROGRESS_REPLY));
			mParseInProgressReplyText.setText(globals.getString(ApplicationGlobals.KEY_PARSE_INPROGRESS_TEXT));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		menu.add(1, MENU_SECURITY, 0, "Security Setup").setIcon(android.R.drawable.ic_partial_secure);		
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
				break;
			case MENU_SECURITY:
				startActivitySecuritySetup();
				break;
		}
		return true;
	}

	private void startActivitySecuritySetup() {
		Intent i;
		i = new Intent(this, RapidAndroidSecuritySetupActivity.class);
		startActivity(i);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		ApplicationGlobals.saveGlobalSettings(this,mActiveLoggingSwitch.isChecked(), 
											  mActiveSwitch.isChecked(),
											  mParseInProgressCheckbox.isChecked(),
											  mParseInProgressReplyText.getText().toString(),
											  mParseCheckbox.isChecked(), 
											  mParseReplyText.getText().toString(), 
		                                      mNoparseCheckBox.isChecked(), 
		                                      mNoparseReplyText.getText().toString());
		SystemHealthTracking.setLoggingEnabled(mActiveLoggingSwitch.isChecked());
		Log.d("GlobalSettings", "logging toggled");

	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {		
		super.onResume();
		loadSettingsFromGlobals();
	}

}
