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

package org.rapidandroid.content.translation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.SmsDbHelper;
import org.rapidandroid.receiver.SmsParseReceiver;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.SimpleFieldType;
import org.rapidsms.java.core.parser.service.ParsingService.ParserType;
import org.rapidsms.java.core.parser.token.ITokenParser;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * 
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 19, 2009
 * 
 *          Static methods to convert that wraps the ContentProvider calls and
 *          returns the actual coremodel objects.
 */

@SuppressLint("UseSparseArrays")
public class ModelTranslator {

	private static Context mContext;

	private static HashMap<String, Integer> formColumnNamesToIndex;
	private static HashMap<String, Integer> fieldColumnNamesToIndex;
	private static HashMap<String, Integer> typeColumnNamesToIndex;

	private static HashMap<Integer, Form> formIdCache = new HashMap<Integer, Form>();
//	private static HashMap<Integer, Vector<Field>> fieldToFormHash = new HashMap<Integer, Vector<Field>>();
	private static HashMap<Integer, SimpleFieldType> fieldTypeHash = new HashMap<Integer, SimpleFieldType>();

	private static SmsDbHelper mDbHelper;

	/*
	 * public ModelTranslator(Context context){ mContext = context; }
	 * 
	 * public ModelTranslator(IntentService context){ mContext =
	 * context.getApplicationContext(); }
	 */

	/**
	 * Pre save check to see if a form with given criteria is already in
	 * existence. This is to prevent dupe formnames and prefixes from existing
	 * in the DB.
	 * 
	 * @param context
	 * @param prefixCandidate
	 * @param nameCandidate
	 * @return
	 */
	public static boolean doesFormExist(String prefixCandidate,
			String nameCandidate) {
		// next let's see if this form is unique
		Uri formExistUri = RapidSmsDBConstants.Form.CONTENT_URI;
		StringBuilder whereclause = new StringBuilder();
		whereclause.append(RapidSmsDBConstants.Form.PREFIX + "='"
				+ prefixCandidate + "'");
		whereclause.append(" OR ");
		whereclause.append(RapidSmsDBConstants.Form.FORMNAME + "='"
				+ nameCandidate + "'");
		Cursor existsCursor = mContext.getContentResolver().query(formExistUri,
				null, whereclause.toString(), null, null);

		if (existsCursor.getCount() == 0) {
			existsCursor.close();
			return false;
		} else {
			existsCursor.close();
			return true;
		}
	}

	public static void editFormToDatabase(Form f) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		try
		{
			Cursor c = db.rawQuery("select * from formdata_" + f.getPrefix() + ";",	null);
			if (c.getCount() > 0) {
				// there was data collected with previous form/table so we need
				// rename that table
				// to a different prefix, update the prefix name in the forms table,
				// and generate a new formID and field equivalents for the
				// updated table
	
				// rename table
				SimpleDateFormat sdf = new SimpleDateFormat("HHmmss_ddMMyyyy", Locale.getDefault());
				String newPrefix = f.getPrefix() + "_"
						+ sdf.format(new Date(System.currentTimeMillis()));
				db.execSQL("alter table formdata_" + f.getPrefix()	+ " rename to formdata_" + newPrefix);
	
				// update name and prefix in forms table
				db.execSQL("update rapidandroid_form set prefix='" + newPrefix
						+ "', " + "formname='" + newPrefix + "' where _id="
						+ f.getFormId());
	
				Log.e("INFO",
						"Renaming table due to duplicate prefix since there is data in table.");
				f.setFormId(-1); // must set to -1 or it might try to reuse an id
				addFormToDatabase(f);
				return;
			} else {
	
				// there is no data in previous form so we can drop the table
				// associated with it,
				// keep the form ID and simply update the fields associated with it
				// in the field table
	
				db.execSQL("drop table formdata_" + f.getPrefix());
				Log.e("INFO",
						"Dropped table due to duplicate prefix since no data in table.");
			}
		}
		catch(Exception ex)
		{
			Log.e("INFO",ex.getMessage(),ex);
		}
		
		updateFormData(f);
	}
	
	
	private static void updateFormData(Form f)
	{
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		ContentValues typecv = new ContentValues();
		if (f.getFormId() != -1) {
			typecv.put(BaseColumns._ID, f.getFormId());
		}
		typecv.put(RapidSmsDBConstants.Form.FORMNAME, f.getFormName());
		typecv.put(RapidSmsDBConstants.Form.PARSEMETHOD, f.getParserType()
				.getJsonConfigVal());

		typecv.put(RapidSmsDBConstants.Form.PREFIX, f.getPrefix());
		typecv.put(RapidSmsDBConstants.Form.DESCRIPTION, f.getDescription());

		db.update("rapidandroid_form", typecv, "_id=?",	new String[] { f.getFormId() + "" });

		Field[] fields = f.getFields();

		// first remove existing fields with reference to form, and then
		// re-add the updated ones
		db.delete("rapidandroid_field", "form_id=?",new String[] { f.getFormId() + "" });

		Log.d("dimagi", "****** Begin fields loop: " + fields.length);
		for (int j = 0; j < fields.length; j++) {
			Field thefield = fields[j];
			Log.d("dimagi", "******** Iterating through fields: "
					+ thefield.getName());
			Uri fieldUri = RapidSmsDBConstants.Field.CONTENT_URI;
			StringBuilder where = new StringBuilder();
			where.append("name='" + thefield.getName() + "' AND ");
			where.append("form_id=" + f.getFormId());
			Cursor crfield = mContext.getContentResolver().query(fieldUri,
					null, where.toString(), null, null);

			// if this field isn't in DB, add it
			if (crfield.getCount() == 0) {
				ContentValues fieldcv = new ContentValues();

				if (thefield.getFieldId() != -1) {
					fieldcv.put(BaseColumns._ID, thefield.getFieldId());
				}
				fieldcv.put(RapidSmsDBConstants.Field.NAME,
						thefield.getName());
				fieldcv.put(RapidSmsDBConstants.Field.FORM, f.getFormId());
				fieldcv.put(RapidSmsDBConstants.Field.PROMPT,
						thefield.getDescription());
				fieldcv.put(RapidSmsDBConstants.Field.SEQUENCE,
						thefield.getSequenceId());

				fieldcv.put(RapidSmsDBConstants.Field.FIELDTYPE,
						((SimpleFieldType) (thefield.getFieldType()))
								.getId());

				Uri insertedFieldUri = mContext.getContentResolver()
						.insert(RapidSmsDBConstants.Field.CONTENT_URI,
								fieldcv);
				Log.d("dimagi", "********** Inserted Field into db: "
						+ insertedFieldUri);
			} else {

			}
			crfield.close();
		}

		generateFormTable(f);

		SmsParseReceiver.initFormCache();
	}
	

	/**
	 * Add a form to to the rapidandroid_form table, inserting new fields as
	 * well. <br>
	 * <br>
	 * Upon form insert, the formdata_[prefix] table will be generated.
	 * 
	 * @param f
	 * @param fields
	 * @param crform
	 */
	public static void addFormToDatabase(Form f) {
		ContentValues typecv = new ContentValues();

		if (f.getFormId() != -1) {
			typecv.put(BaseColumns._ID, f.getFormId());
		}

		typecv.put(RapidSmsDBConstants.Form.FORMNAME, f.getFormName());
		typecv.put(RapidSmsDBConstants.Form.PARSEMETHOD, f.getParserType()
				.getJsonConfigVal());

		typecv.put(RapidSmsDBConstants.Form.PREFIX, f.getPrefix());
		typecv.put(RapidSmsDBConstants.Form.DESCRIPTION, f.getDescription());

		Uri insertedFormUri = mContext.getContentResolver().insert(
				RapidSmsDBConstants.Form.CONTENT_URI, typecv);
		Log.d("dimagi", "****** Inserted form into db: " + insertedFormUri);

		int newFormId = Integer.valueOf(
				insertedFormUri.getPathSegments().get(1)).intValue();
		f.setFormId(newFormId);

		Field[] fields = f.getFields();
		Log.d("dimagi", "****** Begin fields loop: " + fields.length);
		for (int j = 0; j < fields.length; j++) {
			Field thefield = fields[j];
			Log.d("dimagi",
					"******** Iterating through fields: " + thefield.getName());
			Uri fieldUri = RapidSmsDBConstants.Field.CONTENT_URI;
			StringBuilder where = new StringBuilder();
			where.append("name='" + thefield.getName() + "' AND ");
			where.append("form_id=" + newFormId);
			Cursor crfield = mContext.getContentResolver().query(fieldUri,
					null, where.toString(), null, null);

			// if this field isn't in DB, add it
			if (crfield.getCount() == 0) {
				ContentValues fieldcv = new ContentValues();

				if (thefield.getFieldId() != -1) {
					fieldcv.put(BaseColumns._ID, thefield.getFieldId());
				}
				fieldcv.put(RapidSmsDBConstants.Field.NAME, thefield.getName());
				fieldcv.put(RapidSmsDBConstants.Field.FORM, f.getFormId());
				fieldcv.put(RapidSmsDBConstants.Field.PROMPT,
						thefield.getDescription());
				fieldcv.put(RapidSmsDBConstants.Field.SEQUENCE,
						thefield.getSequenceId());

				fieldcv.put(RapidSmsDBConstants.Field.FIELDTYPE,
						((SimpleFieldType) (thefield.getFieldType())).getId());

				Uri insertedFieldUri = mContext.getContentResolver().insert(
						RapidSmsDBConstants.Field.CONTENT_URI, fieldcv);
				Log.d("dimagi", "********** Inserted Field into db: "
						+ insertedFieldUri);
			} else {

			}
			crfield.close();
		}

		// ok, so form and fields have been inserted. Now we need to generate
		// the form table if it doesn't exist yet.
		generateFormTable(f);

		SmsParseReceiver.initFormCache();
	}

	/**
	 * Startup procedure to give this class access to the main DBHelper.
	 * 
	 * @param helper
	 * @param context
	 */
	public static void setDbHelper(SmsDbHelper helper, Context context) {
		mDbHelper = helper;
		mContext = context;
	}

//	private static void setContext(Context context) {
//		mContext = context.getApplicationContext();
//		// if (mContext == null){
//		// }
//	}

	/**
	 * Query all the model tables and generate the fully fleshed out Form
	 * objects. <br>
	 * This call will return ALL forms in the system.
	 * 
	 * @return
	 */
	public static Form[] getAllForms(Context context) {
		mContext = context;
		Uri getFormsUri = RapidSmsDBConstants.Form.CONTENT_URI;

		while (getFormsUri == null) {
			Log.e("sages_ModelTranslator", "form uri is null.");
			getFormsUri = Uri.parse("content://"
					+ RapidSmsDBConstants.AUTHORITY + "/"
					+ RapidSmsDBConstants.Form.URI_PART);
		}
		if (mContext == null) {
			Log.e("sages", "CONTEXT IS NULL WTH!?");
		}
//		Context dummy = null;
		// ContentResolver resolver = dummy.getContentResolver();
		ContentResolver resolver = context.getContentResolver();
		Cursor allformsCursor = resolver.query(getFormsUri, null, null, null,
				null);
		// Cursor allformsCursor =
		// mContext.getContentResolver().query(getFormsUri, null, null, null,
		// null); // real
		// way
		// Cursor allformsCursor =
		// provider.query(getFormsUri,null,null,null,null); //hack way

		if (formColumnNamesToIndex == null) {
			formColumnNamesToIndex = new HashMap<String, Integer>();
			String[] colnames = allformsCursor.getColumnNames();
			int colcount = colnames.length;
			for (int i = 0; i < colcount; i++) {
				formColumnNamesToIndex.put(colnames[i], 
						allformsCursor.getColumnIndex(colnames[i]));
			}
		}
		int formcount = allformsCursor.getCount();

		Form[] ret = new Form[formcount];
		allformsCursor.moveToFirst();
		for (int i = 0; i < formcount; i++) {

			int id = allformsCursor.getInt(formColumnNamesToIndex.get(
					BaseColumns._ID).intValue());
			Integer idInt = Integer.valueOf(id);

			if (formIdCache.containsKey(idInt)) {
				ret[i] = formIdCache.get(idInt);
			}

			String name = allformsCursor.getString(formColumnNamesToIndex.get(
					RapidSmsDBConstants.Form.FORMNAME).intValue());
			String prefix = allformsCursor.getString(formColumnNamesToIndex
					.get(RapidSmsDBConstants.Form.PREFIX).intValue());
			String description = allformsCursor
					.getString(formColumnNamesToIndex.get(
							RapidSmsDBConstants.Form.DESCRIPTION).intValue());
			String parsemethod = allformsCursor
					.getString(formColumnNamesToIndex.get(
							RapidSmsDBConstants.Form.PARSEMETHOD).intValue());
			// Field[] fields = getFieldsForForm(provider, id); // hack way
			Field[] fields = getFieldsForForm(id); // real way

			Form theForm = new Form(id, name, prefix, description, fields,
					ParserType.getTypeFromConfig(parsemethod));

			formIdCache.put(idInt, theForm);
			ret[i] = theForm;
			allformsCursor.moveToNext();
		}
		allformsCursor.close();
		return ret;
	}

	/**
	 * Query all the model tables and generate the fully fleshed out Form
	 * objects. <br>
	 * This call will return ALL forms in the system.
	 * 
	 * @return
	 */
	public static Form[] getAllForms() {
		Uri getFormsUri = RapidSmsDBConstants.Form.CONTENT_URI;

		while (getFormsUri == null) {
			Log.e("sages_ModelTranslator", "form uri is null.");
			getFormsUri = Uri.parse("content://"
					+ RapidSmsDBConstants.AUTHORITY + "/"
					+ RapidSmsDBConstants.Form.URI_PART);
		}
		if (mContext == null) {
			Log.e("sages", "CONTEXT IS NULL WTH!?");
		}
//		Context dummy = null;
		// ContentResolver resolver = dummy.getContentResolver();
		ContentResolver resolver = mContext.getContentResolver();
		Cursor allformsCursor = resolver.query(getFormsUri, null, null, null,
				null);
		// Cursor allformsCursor =
		// mContext.getContentResolver().query(getFormsUri, null, null, null,
		// null); // real
		// way
		// Cursor allformsCursor =
		// provider.query(getFormsUri,null,null,null,null); //hack way

		if (formColumnNamesToIndex == null) {
			formColumnNamesToIndex = new HashMap<String, Integer>();
			String[] colnames = allformsCursor.getColumnNames();
			int colcount = colnames.length;
			for (int i = 0; i < colcount; i++) {
				formColumnNamesToIndex.put(colnames[i], 
						allformsCursor.getColumnIndex(colnames[i]));
			}
		}
		int formcount = allformsCursor.getCount();

		Form[] ret = new Form[formcount];
		allformsCursor.moveToFirst();
		for (int i = 0; i < formcount; i++) {

			int id = allformsCursor.getInt(formColumnNamesToIndex.get(
					BaseColumns._ID).intValue());
			Integer idInt = Integer.valueOf(id);

			if (formIdCache.containsKey(idInt)) {
				ret[i] = formIdCache.get(idInt);
			}

			String name = allformsCursor.getString(formColumnNamesToIndex.get(
					RapidSmsDBConstants.Form.FORMNAME).intValue());
			String prefix = allformsCursor.getString(formColumnNamesToIndex
					.get(RapidSmsDBConstants.Form.PREFIX).intValue());
			String description = allformsCursor
					.getString(formColumnNamesToIndex.get(
							RapidSmsDBConstants.Form.DESCRIPTION).intValue());
			String parsemethod = allformsCursor
					.getString(formColumnNamesToIndex.get(
							RapidSmsDBConstants.Form.PARSEMETHOD).intValue());
			// Field[] fields = getFieldsForForm(provider, id); // hack way
			Field[] fields = getFieldsForForm(id); // real way

			Form theForm = new Form(id, name, prefix, description, fields,
					ParserType.getTypeFromConfig(parsemethod));

			formIdCache.put(idInt, theForm);
			ret[i] = theForm;
			allformsCursor.moveToNext();
		}
		allformsCursor.close();
		return ret;
	}

	/**
	 * Get a fully defined form object by the integer id (rapidandroid_form._id)
	 * 
	 * @param id
	 * @return
	 */
	public static Form getFormById(int id) {
		return getFormFromUri(Uri
				.parse(RapidSmsDBConstants.Form.CONTENT_URI_STRING + id));
	}

	/**
	 * Get a fully defined form object by the integer id (rapidandroid_form._id)
	 * as part of a URI string
	 * 
	 * @param id
	 * @return
	 */
	public static Form getFormFromUri(Uri formUri) {

		Integer formid = Integer.valueOf(formUri.getPathSegments().get(1));
		if (formIdCache.containsKey(formid)) {
			return formIdCache.get(formid);
		}

		Cursor formCursor = mContext.getContentResolver().query(formUri, null,
				null, null, null); // real
									// way
		if (formCursor.getCount() != 1) {
			throw new IllegalArgumentException(formUri
					+ " returned a bad result.");
		}

		if (formColumnNamesToIndex == null) {
			formColumnNamesToIndex = new HashMap<String, Integer>();
			String[] colnames = formCursor.getColumnNames();
			int colcount = colnames.length;
			for (int i = 0; i < colcount; i++) {
				formColumnNamesToIndex.put(colnames[i],
						formCursor.getColumnIndex(colnames[i]));
			}
		}

		formCursor.moveToFirst();
		int id = formCursor.getInt(formColumnNamesToIndex.get(BaseColumns._ID)
				.intValue());
		String name = formCursor.getString(formColumnNamesToIndex.get(
				RapidSmsDBConstants.Form.FORMNAME).intValue());
		String prefix = formCursor.getString(formColumnNamesToIndex.get(
				RapidSmsDBConstants.Form.PREFIX).intValue());
		String description = formCursor.getString(formColumnNamesToIndex.get(
				RapidSmsDBConstants.Form.DESCRIPTION).intValue());
		String parsemethod = formCursor.getString(formColumnNamesToIndex.get(
				RapidSmsDBConstants.Form.PARSEMETHOD).intValue());

		// Field[] fields = getFieldsForForm(provider, id); // hack way
		Field[] fields = getFieldsForForm(id); // real way

		Form ret = new Form(formCursor.getInt(0), name, prefix, description,
				fields, ParserType.getTypeFromConfig(parsemethod));
		formIdCache.put(Integer.valueOf(id), ret);
		formCursor.close();
		return ret;
	}

	/**
	 * 
	 * @param formId
	 * @return
	 */
	public static Field[] getFieldsForForm(int formId) {
		// //real way
		// public static Field[] getFieldsForForm(ContentProvider provider, int
		// formId) { // hack
		// way

//		Context c;

		Uri fieldsUri = RapidSmsDBConstants.Field.CONTENT_URI;
		Cursor fieldsCursor = mContext.getContentResolver().query(fieldsUri,
				null, RapidSmsDBConstants.Field.FORM + "=" + formId, null,
				"sequence ASC"); // real
									// way

		// Cursor fieldsCursor = provider.query(fieldsUri, null,
		// RapidSmsDBConstants.Field.FORM + "=" + formId, null,
		// "sequence ASC");// hack way

		if (fieldColumnNamesToIndex == null) {
			fieldColumnNamesToIndex = new HashMap<String, Integer>();
			String[] colnames = fieldsCursor.getColumnNames();
			int colcount = colnames.length;
			for (int i = 0; i < colcount; i++) {
				fieldColumnNamesToIndex.put(colnames[i], 
						fieldsCursor.getColumnIndex(colnames[i]));
			}
		}
		Field[] newfields = new Field[fieldsCursor.getCount()];
		fieldsCursor.moveToFirst();
		int fieldcount = 0;
		do {
			int id = fieldsCursor.getInt(fieldColumnNamesToIndex.get(
					BaseColumns._ID).intValue());
			String name = fieldsCursor.getString(fieldColumnNamesToIndex.get(
					RapidSmsDBConstants.Field.NAME).intValue());
			String prompt = fieldsCursor.getString(fieldColumnNamesToIndex.get(
					RapidSmsDBConstants.Field.PROMPT).intValue());
			int sequence = fieldsCursor.getInt(fieldColumnNamesToIndex.get(
					RapidSmsDBConstants.Field.SEQUENCE).intValue());
			int fieldtype = fieldsCursor.getInt(fieldColumnNamesToIndex.get(
					RapidSmsDBConstants.Field.FIELDTYPE).intValue());

			Field newField = new Field(id, sequence, name, prompt,
					getFieldType(fieldtype));
			// //real way
			// Field newField = new Field(id, sequence, name,
			// prompt,getFieldType(provider, fieldtype));// hack way
			newfields[fieldcount++] = newField;

		} while (fieldsCursor.moveToNext());

		fieldsCursor.close();
		return newfields;
	}

	/**
	 * This is more a helper class to get all known field types in the system.
	 * Right now these are statically defined in the database and will need to
	 * be added via an exteranl process.
	 * 
	 * @return
	 */

	public static ITokenParser[] getFieldTypes() {
		Uri typesUri = RapidSmsDBConstants.FieldType.CONTENT_URI;
		Cursor typeCursor = mContext.getContentResolver().query(typesUri, null,
				null, null, null);

		ITokenParser[] ret = new ITokenParser[typeCursor.getCount()];

		typeCursor.moveToFirst();

		int typecounter = 0;
		do {
			int id = typeCursor.getInt(typeColumnNamesToIndex.get(
					BaseColumns._ID).intValue());

			ITokenParser newType = getFieldType(id);
			ret[typecounter++] = newType;

		} while (typeCursor.moveToNext());
		typeCursor.close();
		return ret;
	}

	/**
	 * NOTE: This method will always return 1 due to every 
	 * value in the hashtable being of SimpleFieldType class.
	 * Do not use this method to get the actual ID of the FieldType.
	 * Instead, cast the ITokenParser to a SimpleFieldType and then call
	 * getId();
	 * @param p
	 * @return
	 */
	public static int getFieldTypeId(ITokenParser p) {
		for (Integer key : fieldTypeHash.keySet()) {
			ITokenParser ip = fieldTypeHash.get(key);
			
//			Log.e("MARK 1",p.getReadableName());
//			Log.e("MARK 2",ip.getClass().getName());
//			Log.e("MARK 3",p.getClass().getName());
			
			if (ip.getClass().getName().equals(p.getClass().getName()))
				return key.intValue();
		}
		return -1;
	}

	public static ITokenParser getFieldType(int type_id) {
//		Log.e("FOOBAR","Field type id: " + type_id);
		
		// //real way
		// public static SimpleFieldType getFieldType(ContentProvider provider,
		// int type_id) { // hack
		// way

		Integer typeInt = type_id;
		if (fieldTypeHash.containsKey(typeInt)) {
			return fieldTypeHash.get(typeInt);
		}
		Uri typeUri = Uri
				.parse(RapidSmsDBConstants.FieldType.CONTENT_URI_STRING
						+ type_id);
		Cursor typeCursor = mContext.getContentResolver().query(typeUri, null,
				null, null, null); // real
									// way
		// Cursor typeCursor = provider.query(typeUri, null, null, null, null);
		// // hack
		// way

		if (typeColumnNamesToIndex == null) {
			typeColumnNamesToIndex = new HashMap<String, Integer>();
			String[] colnames = typeCursor.getColumnNames();
			int colcount = colnames.length;
			for (int i = 0; i < colcount; i++) {
				typeColumnNamesToIndex.put(colnames[i],
						typeCursor.getColumnIndex(colnames[i]));
			}
		}
		if (typeCursor.getCount() != 1) {
			throw new IllegalArgumentException(typeUri
					+ " returned a bad result.");
		}

		typeCursor.moveToFirst();

		int id = typeCursor.getInt(typeColumnNamesToIndex.get(BaseColumns._ID)
				.intValue());
		String dataType = typeCursor.getString(typeColumnNamesToIndex.get(
				RapidSmsDBConstants.FieldType.DATATYPE).intValue());
		String name = typeCursor.getString(typeColumnNamesToIndex.get(
				RapidSmsDBConstants.FieldType.NAME).intValue());
		String regex = typeCursor.getString(typeColumnNamesToIndex.get(
				RapidSmsDBConstants.FieldType.REGEX).intValue());

		// SimpleFieldType ftype) {
		SimpleFieldType newType = new SimpleFieldType(id, dataType, regex, name);
		fieldTypeHash.put(typeInt, newType);
		typeCursor.close();
		
//		Log.e("FOOBAR","Field type returned: " + newType.getReadableName());

		
		return newType;

	}

	private static void getFieldDeclaration(Field field, StringBuilder sb,
			boolean last) {

		sb.append(" \"");
		sb.append("col_" + field.getName());
		sb.append("\"");
		if (field.getFieldType().getParsedDataType().equals("integer")) {
			sb.append(" integer NULL");
		} else if (field.getFieldType().getParsedDataType().equals("number")) {
			sb.append(" float NULL");
		} else if (field.getFieldType().getParsedDataType().equals("boolean")) {
			sb.append(" bool NULL");
		} else if (field.getFieldType().getParsedDataType().equals("word")) {
			sb.append(" varchar(36) NULL");
		} else if (field.getFieldType().getParsedDataType().equals("ratio")) {
			sb.append(" float NULL");
		} else if (field.getFieldType().getParsedDataType().equals("datetime")) {
			sb.append(" datetime NULL");
		}
		if (!last) {
			sb.append(", ");
		}
	}

	/**
	 * Debug/bootstrap testing method to blow away all data in the core model
	 * tables <br>
	 * <br>
	 * rapidandroid_form <br>
	 * rapidandroid_field <br>
	 * rapidandroid_fieldtype
	 * 
	 */
	public static void ClearFormTables() {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		db.execSQL("delete from " + RapidSmsDBConstants.FieldType.TABLE);
		db.execSQL("delete from " + RapidSmsDBConstants.Field.TABLE);
		db.execSQL("delete from " + RapidSmsDBConstants.Form.TABLE);

		Log.v("dimagi",
				"wiped the form/field/fieldtype/formdata table for debug purposes");
	}

	/**
	 * Generate the fully typed out table that parsed data will be inserted into
	 * when SMS messages come in.
	 * 
	 * @param form
	 */
	public static void generateFormTable(Form form) {
		// dmyung: 1/19/2009
		// For the intial run through this is a bit hacky.

		// for each form, create a new sql table create table script
		// do do that get the form prefix and get a foriegn key back to the
		// message table
		// after that, create all the columns
		// do do this we make a switch statement and we will support the SQLite
		// datatypes.

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		try {
			Cursor formdatacursor = db.rawQuery("select * from formdata_"
					+ form.getPrefix() + ";", null);
			if (formdatacursor.getCount() > 0) {
				return;
			} else {
				db.execSQL("drop table formdata_" + form.getPrefix());
			}
		} catch (SQLException ex) {
			// table likely doesn't exist, we're good to go!

		}

		StringBuilder sb = new StringBuilder();
		sb.append("create table formdata_");
		sb.append(form.getPrefix());
		sb.append(" (");
		sb.append(" \"_id\" integer not null PRIMARY KEY, ");
		sb.append(" \"message_id\" integer not null references \"message\", ");

		org.rapidsms.java.core.model.Field[] fields = form.getFields();
		int fieldcount = fields.length;

		boolean last = false;
		for (int i = 0; i < fieldcount; i++) {
			if (i == fieldcount - 1) {
				last = true;
			}
			getFieldDeclaration(fields[i], sb, last);
		}

		sb.append(" );");

		// Log.e("FOOBAR", sb.toString());
		db.execSQL(sb.toString());
	}

}
