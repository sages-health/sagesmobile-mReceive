/*
 * Copyright (©) 2012 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * @author pokuam1
 * @created Apr 25, 2012
 */
public class SystemHealthTracking {

	
	private static boolean mExternalStorageAvailable = false;
	private static boolean mExternalStorageWriteable = false;
	private static File sdcard = Environment.getExternalStorageDirectory();
	private static String state = Environment.getExternalStorageState();
	private static String logName = "sages_sys_health_track.log";
	private static File sdLogFile;
	
	public static void initDataStore(Context context) throws FileNotFoundException{
		File sdFile = new File(sdcard.getAbsolutePath() + "/rapidandroid/logs/"+ logName);
		
		sdLogFile = sdFile;
		boolean sdFileExists = sdFile.exists();
		if (!sdFileExists){
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			    
			    try {
			    	File fileDir = new File(sdcard.getAbsolutePath() + "/rapidandroid/logs/");
			    	fileDir.mkdirs();
			    	sdFile = new File(fileDir.getAbsolutePath(), logName);
			    	sdFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
		}
	};
	
	/**
	 * 
	 * @param context
	 * @param date
	 * @param eventType
	 * @param message
	 * @param severityLevel
	 */
	synchronized public static void logEvent(Context context, Date date, SagesEventType eventType, String message, int severityLevel){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(sdLogFile, true);
		try {
			StringBuilder logmsg = new StringBuilder(date.toString() + "," + eventType.toString() + "," + message);
			switch (severityLevel) 
			{
			case Log.DEBUG:
				logmsg.append(",DEBUG");
				break;
			case Log.ERROR:
				logmsg.append(",ERROR");
				break;
			case Log.WARN:
				logmsg.append(",WARN");
				break;
			case Log.INFO:
				logmsg.append(",INFO");
				break;
			default:
			}
				logmsg.append("\n");
				fos.write(logmsg.toString().getBytes());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos!= null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public enum SagesEventType {
		SMS_RECEIVED, SMS_SENT, DASHBOARD_OPENED, CSV_OUTPUT_AUTO, STARTUP, SMS_PARSE_FAIL, MULTIPART_SMS, SMS_PARSE_SUCCESS, MULTIPART_SMS_PARSE_SUCCESS, MULTIPART_SMS_PARSE_FAIL, MULTIPART_SMS_SAVED
	} 
	
}
