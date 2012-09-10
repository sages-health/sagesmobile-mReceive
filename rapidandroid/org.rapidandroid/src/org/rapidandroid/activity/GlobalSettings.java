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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.rapidandroid.ApplicationGlobals;

import org.rapidandroid.receiver.SmsParseReceiver;
import org.rapidandroid.R;
import android.app.Activity;
import android.content.Context;
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
	private static String ACTIVITY_TITLE_STRING = "Glb title";
	private static String GLOBAL_INPROGRESS_ACK="in progress ack";
	private static String GLOBAL_PARSE_ACK="parse ack";
	private static String GLOBAL_NO_PARSE_ACK=" no parse ack ";
	private static String GLOBAL_CHECK_PARSE_TEXT="check parse text";
	private static String GLOBAL_CHECK_IN_PROGRESS_TEXT="check in-progress text";
	
	/**
	 * 
	 */
	public static final String LOG_DEBUG_KEY = "GlobalSettings";
	
	private static final int MENU_DONE = Menu.FIRST;
	
	private CheckBox mActiveSwitch;
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
		
		//assign string resource value 
		ACTIVITY_TITLE_STRING=getText(R.string.glbsettings_inprogress_ack).toString();
		 GLOBAL_INPROGRESS_ACK=getText(R.string.glbsettings_parse_ack).toString();
		 GLOBAL_PARSE_ACK=getText(R.string.glbsettings_noparse_ack).toString();
		 GLOBAL_NO_PARSE_ACK=getText(R.string.glb_chk_noparse_text).toString();
		 GLOBAL_CHECK_PARSE_TEXT=getText(R.string.glb_chk_parse_text).toString();
		 GLOBAL_CHECK_IN_PROGRESS_TEXT=getText(R.string.glb_chk_inprogress_text).toString();
		super.onCreate(savedInstanceState);
		
		
		
		setTitle(ACTIVITY_TITLE_STRING);
		setContentView(R.layout.global_settings);

		
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
//Resource Get methods for Application Globals
	public static String getGLOBAL_INPROGRESS_ACK() {
		return GLOBAL_INPROGRESS_ACK;
	}



	public static String getGLOBAL_PARSE_ACK() {
		return GLOBAL_PARSE_ACK;
	}

	
	public static String getGLOBAL_NO_PARSE_ACK() {
		return GLOBAL_NO_PARSE_ACK;
	}

	

	public static String getGLOBAL_CHECK_PARSE_TEXT() {
		return GLOBAL_CHECK_PARSE_TEXT;
	}

	

	public static String getGLOBAL_CHECK_IN_PROGRESS_TEXT() {
		return GLOBAL_CHECK_IN_PROGRESS_TEXT;
	}

	

	/**
	 * 
	 */
	private void loadSettingsFromGlobals() {
		// TODO Auto-generated method stub
		JSONObject globals = ApplicationGlobals.loadSettingsFromFile(this);
		try {
			mActiveSwitch.setChecked(globals.getBoolean(ApplicationGlobals.KEY_ACTIVE_ALL));			
			mParseCheckbox.setChecked(globals.getBoolean(ApplicationGlobals.KEY_PARSE_REPLY));
			mParseReplyText.setText( GLOBAL_CHECK_PARSE_TEXT);
			mNoparseCheckBox.setChecked(globals.getBoolean(ApplicationGlobals.KEY_FAILED_REPLY));
			mNoparseReplyText.setText(GLOBAL_NO_PARSE_ACK);
			mParseInProgressCheckbox.setChecked(globals.getBoolean(ApplicationGlobals.KEY_INPROGRESS_REPLY));
			mParseInProgressReplyText.setText(GLOBAL_CHECK_IN_PROGRESS_TEXT);
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
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		ApplicationGlobals.saveGlobalSettings(this,mActiveSwitch.isChecked(),
											  mParseInProgressCheckbox.isChecked(),
											  mParseInProgressReplyText.getText().toString(),
											  mParseCheckbox.isChecked(), 
											  mParseReplyText.getText().toString(), 
		                                      mNoparseCheckBox.isChecked(), 
		                                      mNoparseReplyText.getText().toString());
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
