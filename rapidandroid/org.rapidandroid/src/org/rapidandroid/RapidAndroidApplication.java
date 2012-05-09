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
import java.util.Date;

import org.rapidandroid.SystemHealthTracking.SagesEventType;
import org.rapidandroid.activity.CsvOutputScheduler;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 27, 2009 Summary:
 */
public class RapidAndroidApplication extends Application {

	static String prefsFileName = CsvOutputScheduler.sharedPreferenceFilename;
	public static boolean logSystemHealth = true;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//Debug.startMethodTracing("rapidandroid_application");
		try {
			SystemHealthTracking.initDataStore(this);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("RapidAndroidApplication", "RAPIDANDROID IS STARTING UP!");
		if (logSystemHealth) SystemHealthTracking.logEvent(this, new Date(), SagesEventType.STARTUP, "RapidAndroid is starting up!", Log.INFO);
		ApplicationGlobals.checkGlobals(this.getApplicationContext());
		ModelBootstrap.InitApplicationDatabase(this.getApplicationContext());
		
	}
	
	/**
	 * Copies source file contents into target file
	 * 
	 * @param context
	 * @param fromFile source file to copy
	 * @param toFile target file to be overwritten
	 */
	public static void copyFileAtoB(Context context, File fromFile, File toFile){
		FileInputStream fin = null;
		InputStreamReader irdr = null;
		FileOutputStream fout = null;
		
		try {
			//fin = context.openFileInput(fromFile.getAbsolutePath());
			fin = new FileInputStream(fromFile.getAbsolutePath());
			irdr = new InputStreamReader(fin);
			fout = new FileOutputStream(toFile.getAbsolutePath());
			int i = 0;
			while ((i = irdr.read()) != -1) {
				fout.write(i);
			}
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
		}finally {
			try {
		      if (fin != null) {
	                fin.close();
	            }
	            if (irdr != null) {
	            	irdr.close();
	            }
	            if (fout != null) {
	            	fout.close();
	            }
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads saved off preferences from internal /data/data/org.rapidandroid/shared_prefs/$prefs.xml 
	 * or /sdcard/rapidandroid/$prefsConfig.xml
	 * 
	 * @param context
	 * @return SharedPreferences either preferences loaded form /data or /sdcard
	 */
	public static SharedPreferences loadPreferences(Context context){
		
		File privateFile = new File("/data/data/org.rapidandroid/shared_prefs/"+prefsFileName +".xml");
		boolean privateFileExists = privateFile.exists();
		
		File sdFile = new File("/sdcard/rapidandroid/"+prefsFileName + "Config.xml");
		boolean sdFileExists = sdFile.exists();
		
		if (privateFileExists && sdFileExists){
			SharedPreferences prefs = context.getSharedPreferences(prefsFileName, MODE_PRIVATE);
			return prefs;
		} else if (!privateFileExists && !sdFileExists){
			SharedPreferences prefs = context.getSharedPreferences(prefsFileName, MODE_PRIVATE);
			return prefs;
		} else if (!privateFileExists && sdFileExists){
			//copy file from SDCARD to private shared prefs (needed on a new install or upgrade) 
			copyFileAtoB(context, sdFile, privateFile);
			return context.getSharedPreferences(prefsFileName, MODE_PRIVATE);
		} else if (privateFileExists && !sdFileExists){
			copyFileAtoB(context, privateFile, sdFile);
			return context.getSharedPreferences(prefsFileName, MODE_PRIVATE);
		}
		else return context.getSharedPreferences(prefsFileName, MODE_PRIVATE);
	}
}
