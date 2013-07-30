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

package org.rapidandroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.rapidsms.java.core.model.Form;

import android.content.Context;
import android.util.Log;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Feb 10, 2009
 * Summary:
 */
public class ApplicationGlobals {


	private static boolean globalsLoaded = false;
	
	private static boolean mActiveLogging = false; 
	private static boolean mActive = false; 
	private static boolean mReplyParse = false;
	private static boolean mReplyInProgress = false;
	private static boolean mReplyFail = false;
	
	private static String mReplyParseText = "";
	private static String mReplyParseInProgressText = "";
	private static String mReplyFailText = "";
	
	/**
	 * If global settings have not been loaded, loads them from the global settings file.
	 * @param context
	 */
	public static void initGlobals(Context context) {
		if(!globalsLoaded) {
			JSONObject globals = ApplicationGlobals.loadSettingsFromFile(context);
			try {
				
				if(globals.has(KEY_ACTIVE_ALL)) {
					mActive = globals.getBoolean(KEY_ACTIVE_ALL);
					
				} else {
					mActive = false;
				}
				
				if(globals.has(KEY_ACTIVE_LOGGING)) {
					mActiveLogging = globals.getBoolean(KEY_ACTIVE_LOGGING);
					
				} else {
					mActiveLogging = false;
				}
				
				mReplyParse = globals.getBoolean(KEY_PARSE_REPLY);
				mReplyInProgress = globals.getBoolean(KEY_INPROGRESS_REPLY);
				mReplyFail = globals.getBoolean(KEY_FAILED_REPLY);
				mReplyParseText = globals.getString(KEY_PARSE_REPLY_TEXT);
				mReplyParseInProgressText = globals.getString(KEY_PARSE_INPROGRESS_TEXT);
				mReplyFailText = globals.getString(KEY_FAILED_REPLY_TEXT);
				globalsLoaded = true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static boolean doLog(){
		return mActiveLogging;
	}
	
	public static boolean doReplyOnFail() {
		if(mActive) {
			return mReplyFail;
		} else {
			return false;
		}
	}
	
	public static boolean doReplyOnParse() {
		if(mActive) {
			return mReplyParse;
		} else {
			return false;
		}
	}
	
	
	public static boolean doReplyOnParseInProgress() {
		if(mActive) {
			return mReplyInProgress;
		} else {
			return false;
		}
	}
	
	public static String getParseSuccessText() {
		return mReplyParseText;
	}
	
	public static String getParseInProgressText() {
		return mReplyParseInProgressText;
	}
	
	public static String getParseFailText() {
		return mReplyFailText;
	}
	
	public static String getParseFailText(Form form) {
		// TODO: SAGES/pokuam1: reconcile this - not internationalization friendly
		/*		
		StringBuilder failReply = new StringBuilder(form.getPrefix()).append(" "); 
		for (Field field : form.getFields()){
			failReply.append(field.getName()).append("_").append(field.getFieldType().getReadableName()).append(" ");
		}
		return mReplyFailText + ": " + failReply.toString();
		*/
		return mReplyFailText;
	}
	
	/**
	 * Creates a global settings file if does not exist. For default values, all interactive features disabled.
	 * @param context
	 */
	public static void checkGlobals(Context context) {		
		File f = context.getFileStreamPath(SETTINGS_FILE);
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			saveGlobalSettings(context, false, false, false, "Message parsing in progress", false, "Message parsed successfully, thank you", false, "Unable to understand your message, please try again");
			
		}
	}
	
	/**
	 * 
	 */
	public static final String KEY_ACTIVE_LOGGING = "ActivateLogging";
	
	/**
	 * 
	 */
	public static final String KEY_ACTIVE_ALL = "ActivateAll";
	
	
	/**
	 * 
	 */
	public static final String KEY_FAILED_REPLY_TEXT = "FailedReplyText";
	/**
	 * 
	 */
	public static final String KEY_FAILED_REPLY = "FailedReply";
	/**
	 * 
	 */
	public static final String KEY_PARSE_REPLY_TEXT = "ParseReplyText";
	/**
	 * 
	 */
	public static final String KEY_PARSE_REPLY = "ParseReply";
	/**
	 * 
	 */
	public static final String KEY_PARSE_INPROGRESS_TEXT = "ParseInProgressReplyText";
	/**
	 * 
	 */
	public static final String KEY_INPROGRESS_REPLY = "ParseInProgressReply";
	/**
	 * 
	 */
	public static final String SETTINGS_FILE = "GlobalSettings.json";
	
	
	public static final String LOG_DEBUG_KEY = "ApplicationGlobals";
	/**
	 * 
	 */
	public static JSONObject loadSettingsFromFile(Context context) {
		FileInputStream fin = null;
		InputStreamReader irdr = null;
		JSONObject readobject = null;
		try {			

			fin = context.openFileInput(SETTINGS_FILE);

			irdr = new InputStreamReader(fin); // promote

			int size = (int) fin.getChannel().size();
			char[] data = new char[size]; // allocate char array of right
			// size
			irdr.read(data, 0, size); // read into char array
			irdr.close();

			String contents = new String(data);
			readobject = new JSONObject(contents);
			
			if(!readobject.has(KEY_ACTIVE_ALL)) {
				//dmyung hack to keep compatability with new version
				readobject.put(KEY_ACTIVE_ALL, false);
			}
			
			if(!readobject.has(KEY_ACTIVE_LOGGING)) {
				//dmyung hack to keep compatability with new version
				readobject.put(KEY_ACTIVE_LOGGING, false);
			}
			
//			mParseCheckbox.setChecked(readobject.getBoolean(KEY_PARSE_REPLY));
//			mParseReplyText.setText(readobject.getString(KEY_PARSE_REPLY_TEXT));
//			mNoparseCheckBox.setChecked(readobject.getBoolean(KEY_FAILED_REPLY));
//			mNoparseReplyText.setText(readobject.getString(KEY_FAILED_REPLY_TEXT));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (irdr != null) {
					irdr.close();
				}
				if (fin != null) {
					fin.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return readobject;
	}
	

	/**
	 * 
	 * @param context
	 * @param activateLogging
	 * @param activateAll
	 * @param inProgressReply
	 * @param inProgressReplyText
	 * @param parseReply
	 * @param parseReplyText
	 * @param failedReply
	 * @param failedReplyText
	 */
	public static void saveGlobalSettings(Context context,boolean activateLogging, boolean activateAll, boolean inProgressReply, String inProgressReplyText, boolean parseReply, String parseReplyText, boolean failedReply, String failedReplyText) {		
		JSONObject settingsObj = new JSONObject();
		FileOutputStream fos = null;
		try {
			settingsObj.put(KEY_ACTIVE_LOGGING, activateLogging);
			settingsObj.put(KEY_ACTIVE_ALL, activateAll);
			settingsObj.put(KEY_INPROGRESS_REPLY, inProgressReply);
			settingsObj.put(KEY_PARSE_INPROGRESS_TEXT, inProgressReplyText);
			settingsObj.put(KEY_FAILED_REPLY, failedReply);
			settingsObj.put(KEY_PARSE_REPLY, parseReply);
			settingsObj.put(KEY_PARSE_REPLY_TEXT, parseReplyText);
			settingsObj.put(KEY_FAILED_REPLY, failedReply);
			settingsObj.put(KEY_FAILED_REPLY_TEXT, failedReplyText);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		try {
			fos = context.openFileOutput(SETTINGS_FILE, Context.MODE_PRIVATE);
			fos.write(settingsObj.toString().getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_DEBUG_KEY, e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_DEBUG_KEY, e.getMessage());
		}
		finally {
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			globalsLoaded = false;
			ApplicationGlobals.initGlobals(context);
		}
	}
}
