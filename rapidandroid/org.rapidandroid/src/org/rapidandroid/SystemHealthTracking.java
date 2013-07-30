/*
 * Copyright (©) 2012 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.Environment;

/**
 * Handles logging various event types for troubleshooting/debugging/monitoring system health
 * 
 * @author pokuam1
 * @created Apr 25, 2012
 */
public class SystemHealthTracking {

	private Logger logger;
	private static boolean loggingEnabled;
	
	public enum SagesEventType {
		SMS_RECEIVED, SMS_SENT, DASHBOARD_OPENED, CSV_OUTPUT_AUTO, STARTUP, SMS_PARSE_FAIL, MULTIPART_SMS, SMS_PARSE_SUCCESS, MULTIPART_SMS_PARSE_SUCCESS, MULTIPART_SMS_PARSE_FAIL, MULTIPART_SMS_SAVED
	} 

	
	public static boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public static void setLoggingEnabled(boolean loggingEnabled) {
		SystemHealthTracking.loggingEnabled = loggingEnabled;
	}

	public SystemHealthTracking(Class<?> clazz){
		this.logger = Logger.getLogger(clazz);
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public void logInfo(Object msg){
		if (loggingEnabled) {
			logger.info(msg);
		}
	};
	public void logWarn(Object msg){
		if (loggingEnabled) {
			logger.warn(msg);
		}
	};
	public void logDebug(Object msg){
		if (loggingEnabled) {
			logger.debug(msg);
		}
	};
	public void logError(Object msg){
		if (loggingEnabled) {
			logger.error(msg);
		}
	};
	public void logInfo(SagesEventType evt, Object msg){
		if (loggingEnabled) {
			logger.info("[" + evt + "]" + msg);
		}
	};
	public void logWarn(SagesEventType evt, Object msg){
		if (loggingEnabled) {
			logger.warn("[" + evt + "]" + msg);
		}
	};
	public void logDebug(SagesEventType evt, Object msg){
		if (loggingEnabled) {
			logger.debug("[" + evt + "]" + msg);
		}
	};
	public void logError(SagesEventType evt, Object msg){
		if (loggingEnabled) {
			logger.error("[" + evt + "]" + msg);
		}
	};

//	private static boolean mExternalStorageAvailable = false;
//	private static boolean mExternalStorageWriteable = false;
	private static File sdcard = Environment.getExternalStorageDirectory();
	private static String state = Environment.getExternalStorageState();
	public static String logName = "sages_system_health.log";
//	private static File sdLogFile;
	
	public static void initDataStore(Context context) throws FileNotFoundException{
		File sdFile = new File(sdcard.getAbsolutePath() + "/rapidandroid/logs/"+ logName);
		
//		sdLogFile = sdFile;
		boolean sdFileExists = sdFile.exists();
		if (!sdFileExists){
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
//			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			    
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
//			    mExternalStorageAvailable = true;
//			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
//			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
		}
	};
	
/*	*//**
	 * 
	 * @param context
	 * @param date
	 * @param eventType
	 * @param message
	 * @param severityLevel
	 *//*
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
	}*/
	
	
}
