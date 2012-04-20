/*
 * Copyright (©) 2012 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid.data.controller;

import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.SmsDbHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Adds more control to forms and makes calls to the db layer. (There was a requirement to delete form via the UI)
 * 
 * @author POKUAM1
 * @created Nov 15, 2011
 */
public class FormController {

	private static final String t = FormController.class.getCanonicalName();
	private static SmsDbHelper mDbHelper;
	private static SQLiteDatabase mDb;
	
	/**
	 * Not fully implemented, see {@link FormController}.deleteFormByPrefix()
	 * @param context
	 * @param id
	 */
	private static void deleteFormById(Context context, int id){
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
		mDb = mDbHelper.getWritableDatabase();
		StringBuilder query = new StringBuilder();
	}
	
	public static void deleteFormByPrefix(Context context, String formPrefix){
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
		mDb = mDbHelper.getWritableDatabase();
		mDb.beginTransaction();
		
		try{
		// TODO: confirm the formdata_prefix table is empty

		
		// get formId of the form
		StringBuilder queryGetFormId = new StringBuilder();
		queryGetFormId.append("select " + RapidSmsDBConstants.Form._ID + " from ");
		queryGetFormId.append(RapidSmsDBConstants.Form.TABLE + " where ");
		queryGetFormId.append(RapidSmsDBConstants.Form.PREFIX + "=?");
		Log.d(t, queryGetFormId.toString());
		
		Cursor crFormId = mDb.rawQuery(queryGetFormId.toString(), new String[]{formPrefix});
		int formId = -1;
		if (crFormId.moveToFirst()){
			formId = crFormId.getInt(0);
		}
		
		// THIS DOESN'T USE THE FORM'S ID. RATHER THE MESSAGE'S ID
		// delete from message where _id in (select _id from formdata_prefix)
		//delete from rapidandroid_message where _id in (select _id from formdata_bednets);
		StringBuilder queryDeleteFromMessage = new StringBuilder();
		queryDeleteFromMessage.append("delete from " + RapidSmsDBConstants.Message.TABLE + " where ");
		queryDeleteFromMessage.append(RapidSmsDBConstants.Message._ID);
		queryDeleteFromMessage.append(" in (select " + RapidSmsDBConstants.FormData._ID + " from " + RapidSmsDBConstants.FormData.TABLE_PREFIX + formPrefix +")");
		Log.d(t, queryDeleteFromMessage.toString());
		mDb.execSQL(queryDeleteFromMessage.toString());
		
		//delete from rapidandroid_field where form_id = formId
		StringBuilder queryDeleteFields = new StringBuilder();
		queryDeleteFields.append("delete from " + RapidSmsDBConstants.Field.TABLE + " where ");
		queryDeleteFields.append(RapidSmsDBConstants.Field.FORM + "=" + formId);
		Log.d(t, queryDeleteFields.toString());
		mDb.execSQL(queryDeleteFields.toString());
		
		//delete from rapidandroid_form where _id = formId;
		StringBuilder queryDeleteForm = new StringBuilder();
		queryDeleteForm.append("delete from " + RapidSmsDBConstants.Form.TABLE + " where ");
		queryDeleteForm.append(RapidSmsDBConstants.Form._ID + "=" + formId);
		Log.d(t, queryDeleteForm.toString());
		mDb.execSQL(queryDeleteForm.toString());
		
		//drop table formdata_formPrefix
		StringBuilder queryDropFormData = new StringBuilder();
		queryDropFormData.append("drop table " + RapidSmsDBConstants.FormData.TABLE_PREFIX + formPrefix);
		Log.d(t, queryDropFormData.toString());
		mDb.execSQL(queryDropFormData.toString());
		mDb.setTransactionSuccessful();
		} finally{
			mDb.endTransaction();
			mDb.close();
			mDbHelper.close();			
		}

	}
}
