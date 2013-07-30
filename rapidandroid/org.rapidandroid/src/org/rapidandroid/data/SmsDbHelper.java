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

package org.rapidandroid.data;

import java.io.File;

import org.rapidandroid.content.translation.ModelTranslator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * This class helps open, create, and upgrade the database file. <br>
 * By default it's hard coded to store the DB on the SD card. Thread safety and
 * closure safety are pulled straight from the parent class for db management
 * for getReadable() and getWriteable()
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 12, 2009
 * 
 * 
 * 
 */

public class SmsDbHelper extends SQLiteOpenHelper {
	private static final String TAG = "SmsDbHelper";

	private static final String DATABASE_NAME = "rapidandroid.db";
	private static final String DATABASE_PATH_EXTERNAL = "/sdcard/rapidandroid/rapidandroid.db";
	private static final String DATABASE_PATH_LOCAL = "rapidandroid.db";

	private boolean useLocal = false;
	private String dbPathToUse = DATABASE_PATH_EXTERNAL;

	// private static final int DATABASE_VERSION_1 = 1; //version 1: initial
	// version 1/22/2009
//	private static final int DATABASE_VERSION_2 = 2; // 2/6/2007, add receive_time
//													// column to message table
//	private static final int DATABASE_VERSION_3 = 3;  // 1/31/2012, create work_table for multipart sms
	private static final int DATABASE_VERSION = 3;  // 1/31/2012, create work_table for multipart sms
	
	// Sections lifted from the originating class SqliteOpenHelper.java
	private SQLiteDatabase mDatabase = null;
	private boolean mIsInitializing = false;

	public SmsDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		// super(context, null, null, 0)
		// For eventual sd card storage:
		// SQLiteDatabase.openDatabase("/sdcard/my.db", null,
		// SQLiteDatabase.CREATE_IF_NECESSARY);

		File sdcard = Environment.getExternalStorageDirectory();

		File destination = new File(sdcard, "rapidandroid");
		if (destination.mkdir()) {
			Log.d("SmsDbHelper", "Application data directory created");
		}
		if (destination.exists()) {
			useLocal = false;
			dbPathToUse = DATABASE_PATH_EXTERNAL;
		} else {
			useLocal = true;
			dbPathToUse = DATABASE_PATH_LOCAL;
		}

		// SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_NAME, null,
		// SQLiteDatabase.CREATE_IF_NECESSARY);
		// onCreate(db);
		ModelTranslator.setDbHelper(this, context);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		String mCreateTable_Message = "CREATE TABLE \"rapidandroid_message\" ("
				+ "\"_id\" integer NOT NULL PRIMARY KEY,"
				// +
				// "\"transaction_id\" integer NULL REFERENCES \"rapidandroid_transaction\" (\"id\"),"
				+ "\"phone\" varchar(30) NULL,"
				+ "\"monitor_id\" integer NULL REFERENCES \"rapidandroid_monitor\" (\"id\"),"
				+ "\"time\" datetime NOT NULL," + "\"message\" varchar(160) NOT NULL,"
				+ "\"is_outgoing\" bool NOT NULL," + "\"is_virtual\" bool NOT NULL,"
				+ "\"receive_time\" datetime NULL);";

		String mCreateTable_Monitor = "CREATE TABLE \"rapidandroid_monitor\" ("
				+ "\"_id\" integer NOT NULL PRIMARY KEY," + "\"first_name\" varchar(50) NOT NULL,"
				+ "\"last_name\" varchar(50) NOT NULL," + "\"alias\" varchar(16) NOT NULL UNIQUE,"
				+ "\"phone\" varchar(30) NOT NULL," + "\"email\" varchar(75) NOT NULL,"
				+ "\"incoming_messages\" integer unsigned NOT NULL," + "\"receive_reply\" bool DEFAULT '0' NOT NULL);";

		String mCreateTable_Form = "CREATE TABLE \"rapidandroid_form\" (" + "\"_id\" integer NOT NULL PRIMARY KEY,"
				+ "\"formname\" varchar(32) NOT NULL UNIQUE," + "\"prefix\" varchar(16) NOT NULL UNIQUE,"
				+ "\"description\" varchar(512) NOT NULL," + "\"parsemethod\" varchar(128) NOT NULL);";

		String mCreateTable_FieldType = "CREATE TABLE \"rapidandroid_fieldtype\" ("
				+ "\"_id\" integer NOT NULL PRIMARY KEY," + "\"name\" varchar(32) NOT NULL UNIQUE,"
				+ "\"datatype\" varchar(32) NOT NULL," + "\"regex\" varchar(1024) NOT NULL);";

		String mCreateTable_Field = "CREATE TABLE \"rapidandroid_field\" (" + "\"_id\" integer NOT NULL PRIMARY KEY,"
				+ "\"form_id\" integer NOT NULL REFERENCES \"rapidandroid_form\" (\"id\"),"
				+ "\"sequence\" integer unsigned NOT NULL," 
				+ "\"name\" varchar(32) NOT NULL,"
				+ "\"prompt\" varchar(64) NOT NULL,"
				+ "\"fieldtype_id\" integer NOT NULL REFERENCES \"rapidandroid_fieldtype\" (\"id\"));";
		
		// SAGES:pokuam1: table for multipart sms support
		String mCreateTable_MultiSmsWorkTable = "CREATE TABLE \"sages_multisms_worktable\" (\"_id\" INTEGER PRIMARY KEY "
				+ "AUTOINCREMENT  NOT NULL , \"segment_number\" INTEGER NOT NULL , \"total_segments\" INTEGER NOT NULL , "
				+ "\"tx_id\" DATETIME NOT NULL , \"tx_timestamp\" DATETIME NOT NULL , \"payload\" VARCHAR NOT NULL , \"monitor_msg_id\" VARCHAR NOT NULL , "
				+ "UNIQUE( \"segment_number\" , \"total_segments\" , \"tx_id\"))";
	
		// String mCreateTable_Transaction =
		// "CREATE TABLE \"rapidandroid_transaction\" ("
		// + "\"_id\" integer NOT NULL PRIMARY KEY,"
		// + "\"identity\" integer unsigned NULL,"
		// + "\"phone\" varchar(30) NULL,"
		// +
		// "\"monitor_id\" integer NULL REFERENCES \"rapidandroid_monitor\" (\"id\"));";

		db.execSQL(mCreateTable_Message);
		db.execSQL(mCreateTable_Monitor);
		db.execSQL(mCreateTable_Form);
		db.execSQL(mCreateTable_FieldType);
		db.execSQL(mCreateTable_Field);
		db.execSQL(mCreateTable_MultiSmsWorkTable);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#getReadableDatabase()
	 */
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		if (useLocal) {
			return super.getReadableDatabase();
		}

		if (mDatabase != null && mDatabase.isOpen()) {
			return mDatabase; // The database is already open for business
		}

		if (mIsInitializing) {
			throw new IllegalStateException("getReadableDatabase called recursively");
		}

		try {
			return getWritableDatabase();
		} catch (SQLiteException e) {
			Log.e(TAG, "Couldn't open " + DATABASE_NAME + " for writing (will try read-only):", e);
		}

		SQLiteDatabase db = null;
		try {
			mIsInitializing = true;
			db = SQLiteDatabase.openDatabase(dbPathToUse, null, SQLiteDatabase.OPEN_READONLY);
			if (db.getVersion() != DATABASE_VERSION) {
				throw new SQLiteException("Can't upgrade read-only database from version " + db.getVersion() + " to "
						+ DATABASE_VERSION + ": " + dbPathToUse);
			}

			onOpen(db);
			Log.w(TAG, "Opened " + DATABASE_NAME + " in read-only mode");
			mDatabase = db;
			return mDatabase;
		} finally {
			mIsInitializing = false;
			if (db != null && db != mDatabase)
				db.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#getWritableDatabase()
	 */
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		if (useLocal) {
			return super.getWritableDatabase();
		}
		if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
			return mDatabase; // The database is already open for business
		}

		if (mIsInitializing) {
			throw new IllegalStateException("getWritableDatabase called recursively");
		}

		// If we have a read-only database open, someone could be using it
		// (though they shouldn't), which would cause a lock to be held on
		// the file, and our attempts to open the database read-write would
		// fail waiting for the file lock. To prevent that, we acquire the
		// lock on the read-only database, which shuts out other users.

		boolean success = false;
		SQLiteDatabase db = null;
		// if (mDatabase != null) mDatabase.lock(); //can't call the locks for
		// some reason. beginTransaction does lock it though
		try {
			mIsInitializing = true;
			db = SQLiteDatabase.openOrCreateDatabase(dbPathToUse, null);
			//db.execSQL("PRAGMA journal_size_limit = 0"); // SAGES/pokuam1: didn't do anything useful, see wal_autocheckpoint
			//db.rawQuery("PRAGMA journal_mode = DELETE", null); // SAGES/pokuam1: didn't do anything useful, wal_autocheckpoint

			// SAGES/pokuam1: with Android 2.3+ native sqlite v 3.7 uses WAL instead of journaling. use this to force commit WAL to main db file.
			// https://groups.google.com/group/android-developers/browse_thread/thread/f9dca550c085221c?pli=1
			Cursor cursor = db.rawQuery("PRAGMA wal_autocheckpoint = 1", null);
			cursor.close();
			int version = db.getVersion();
			if (version != DATABASE_VERSION) {
				db.beginTransaction();
				try {
					if (version == 0) {
						onCreate(db);
					} else {
						onUpgrade(db, version, DATABASE_VERSION);
					}
					db.setVersion(DATABASE_VERSION);
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}

			onOpen(db);
			success = true;
			return db;
		} finally {
			mIsInitializing = false;
			if (success) {
				if (mDatabase != null) {
					try {
						mDatabase.close();
					} catch (Exception e) {
					}
					// mDatabase.unlock();
				}
				mDatabase = db;
			} else {
				// if (mDatabase != null) mDatabase.unlock();
				if (db != null)
					db.close();
			}
		}

	}

	public static void getReadableSQLiteDatabase(Context context, SmsDbHelper mDbHelper, SQLiteDatabase mDb) {
		if (mDb != null) {
			if (mDb.isOpen()) {
				mDb.close();
			}
			mDb = null;
		}
		
		if (mDbHelper != null) {
			mDbHelper.close();
			mDbHelper = null;
		}
		
		mDbHelper = new SmsDbHelper(context);
		mDb = mDbHelper.getReadableDatabase();
	}
	
	//TODO: SAGES/pokuam: This needs work. Needs to increment by 1 because logic doesn't hit all version combinations
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Log.w(TAG, "Upgrading database from version " + oldVersion + " to " +
		// newVersion
		// + ", which will destroy all old data");
		// db.execSQL("DROP TABLE IF EXISTS notes");
		// onCreate(db);

		if (oldVersion == 1 && newVersion >= 2) {
			// version 1 to 2 introduced the receive_time for the message
			String messageAlterSql = "alter table rapidandroid_message add column receive_time datetime NULL";
			db.execSQL(messageAlterSql);
		} else if (oldVersion == 2 && newVersion >= 3){
			// SAGES/pokuam1: version 2 to 3 introduced the work_table for processing multi part SMS messages
			String mCreateTable_MultiSmsWorkTable = "CREATE TABLE \"sages_multisms_worktable\" (\"_id\" INTEGER PRIMARY KEY "
					+ "AUTOINCREMENT  NOT NULL , \"segment_number\" INTEGER NOT NULL , \"total_segments\" INTEGER NOT NULL , "
					+ "\"tx_id\" DATETIME NOT NULL , \"tx_timestamp\" DATETIME NOT NULL , \"payload\" VARCHAR NOT NULL , \"monitor_msg_id\" VARCHAR NOT NULL ,"
					+ "UNIQUE( \"segment_number\" , \"total_segments\" , \"tx_id\"))";;
			db.execSQL(mCreateTable_MultiSmsWorkTable);
		} 
	}
}