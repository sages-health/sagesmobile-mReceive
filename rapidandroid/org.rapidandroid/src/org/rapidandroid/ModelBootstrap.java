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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rapidandroid.content.translation.MessageTranslator;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.RapidSmsDBConstants.FieldType;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.SimpleFieldType;
import org.rapidsms.java.core.parser.service.ParsingService.ParserType;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 27, 2009 Summary:
 */
public class ModelBootstrap {
	private static SystemHealthTracking healthTracker = new SystemHealthTracking(ModelBootstrap.class);

	private static Context mContext;

	private static HashMap<Integer, Form> formIdCache = new HashMap<Integer, Form>();
	private static HashMap<Integer, Vector<Field>> fieldToFormHash = new HashMap<Integer, Vector<Field>>();
	private static HashMap<Integer, SimpleFieldType> fieldTypeHash = new HashMap<Integer, SimpleFieldType>();
	
	
	public static void InitApplicationDatabase(Context context) {
		mContext = context;
		// SAGES/pokuam1: force check existence of tables and forms
		if (true) {
		//if (isFieldTypeTableEmpty()) {
			healthTracker.logInfo("Bootstrapping fieldtypes, fields, and forms.");
			applicationInitialFormFieldTypesBootstrap();
		}
		MessageTranslator.updateMonitorHash(context);
	}

	private static boolean isFieldTypeTableEmpty() {
		Uri fieldtypeUri = RapidSmsDBConstants.FieldType.CONTENT_URI;
		Cursor fieldtypecheck = mContext.getContentResolver().query(fieldtypeUri, null, null, null, null);
		if (fieldtypecheck.getCount() == 0) {
			fieldtypecheck.close();
			return true;
		} else {
			// not empty!
			fieldtypecheck.close();
			return false;
		}
	}

	private static String loadAssetFile(String filename) {
		try {
			InputStream is = mContext.getAssets().open(filename);

			int size = is.available();

			// Read the entire asset into a local byte buffer.
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			// Convert the buffer into a Java string.
			String text = new String(buffer);

			return text;

		} catch (IOException e) {
			// Should never happen!
			throw new RuntimeException(e);
		}
	}
	
	//TODO: make less code repeat
	private static String loadSdCardFile(String filename) {
		// InputStream is = mContext.getAssets().open(filename);
		File file = new File(filename);
		if (!file.exists()){
			return null;
		}
		
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
			int size = is.available();

			// Read the entire asset into a local byte buffer.
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			// Convert the buffer into a Java string.
			String text = new String(buffer);

			return text;

		} catch (IOException e) {
			// Should never happen!
			throw new RuntimeException(e);
		}
	}

	/**
	 * Initial app startup, ONLY SHOULD BE RUN ONCE!!! called when the existence
	 * of some data in the fieldtypes table is missing.
	 * 
	 * external-custom-fieldtypes: place them here: /sdcard/rapidandroid/externalcustomfieldtypes.json
	 */
	private static void applicationInitialFormFieldTypesBootstrap() {
		healthTracker.logInfo("Loading field types and forms from assets.");
		loadFieldTypesFromAssets();
		insertFieldTypesIntoDBIfNecessary();

		loadInitialFormsFromAssets();
		checkIfFormTablesExistCreateIfNecessary();
	}

	private static void insertFieldTypesIntoDBIfNecessary() {

		Iterator<?> it = fieldTypeHash.entrySet().iterator();

		// for(int i = 0; i < forms.size(); i++) {
		while (it.hasNext()) {
			Map.Entry<Integer, SimpleFieldType> pairs = (Map.Entry<Integer, SimpleFieldType>) it.next();
			SimpleFieldType thetype = pairs.getValue();
			// make the URI and insert for the Fieldtype
			Uri fieldtypeUri = Uri.parse(RapidSmsDBConstants.FieldType.CONTENT_URI_STRING + thetype.getId());
			Cursor typeCursor = mContext.getContentResolver().query(fieldtypeUri, null, null, null, null);
			
			if (typeCursor.getCount() == 0) {
				ContentValues typecv = new ContentValues();

				typecv.put(BaseColumns._ID, thetype.getId());
				typecv.put(RapidSmsDBConstants.FieldType.DATATYPE, thetype.getDataType());
				typecv.put(RapidSmsDBConstants.FieldType.NAME, thetype.getReadableName());
				typecv.put(RapidSmsDBConstants.FieldType.REGEX, thetype.getRegex());

				Log.d("dimagi", "InsertFieldType: " + thetype.getId());
				Log.d("dimagi", "InsertFieldType: " + thetype.getDataType());
				Log.d("dimagi", "InsertFieldType: " + thetype.getReadableName());
				Log.d("dimagi", "InsertFieldType: " + thetype.getRegex());
				
				Uri insertedTypeUri = mContext.getContentResolver().insert(RapidSmsDBConstants.FieldType.CONTENT_URI, typecv);
				Log.d("dimagi", "********** Inserted SimpleFieldType into db: " + insertedTypeUri);

			} else if (typeCursor.getCount() == 1 && typeCursor.moveToFirst()) {
				// SAGES: update the fieldtype in the database -- the Name and/or Regex has changed
				int nameColIndx = typeCursor.getColumnIndex(FieldType.NAME);
				int regexColIndx = typeCursor.getColumnIndex(FieldType.REGEX);
				
				boolean isUpdatedFieldType = (!typeCursor.getString(nameColIndx).equals(thetype.getReadableName()));
				boolean isUpdatedRegex = (!typeCursor.getString(regexColIndx).equals(thetype.getRegex()));
				
				if (isUpdatedFieldType || isUpdatedRegex) {
					ContentValues typecv = new ContentValues();
					
					//typecv.put(BaseColumns._ID, thetype.getId());
					typecv.put(RapidSmsDBConstants.FieldType.DATATYPE, thetype.getDataType());
					typecv.put(RapidSmsDBConstants.FieldType.NAME, thetype.getReadableName());
					typecv.put(RapidSmsDBConstants.FieldType.REGEX, thetype.getRegex());
					
					Log.d("sages", "UpdateFieldType: " + thetype.getId());
					Log.d("sages", "UpdateFieldType: " + thetype.getDataType());
					Log.d("sages", "UpdateFieldType: " + thetype.getReadableName());
					Log.d("sages", "UpdateFieldType: " + thetype.getRegex());
					
					String whereClause = BaseColumns._ID + "= ?";
					String[] whereClauseArgs = {String.valueOf(thetype.getId())};
					int numUpdatedType = mContext.getContentResolver().update(RapidSmsDBConstants.FieldType.CONTENT_URI, typecv, whereClause, whereClauseArgs);
					Log.d("sages", "********** Updated SimpleFieldType into db: " + numUpdatedType);
				}
			}
			typeCursor.close();
		}
	}

	private static void loadFieldTypesFromAssets() {
		String types = loadAssetFile("definitions/fieldtypes.json");
		String customtypes = loadAssetFile("definitions/customfieldtypes.json");
		// SAGES: loading the custom fieldtype files 
		String externalcustomtypes = loadSdCardFile("/sdcard/rapidandroid/externalcustomfieldtypes.json");
		
		try {
			JSONArray typesarray = new JSONArray(types);

			int arrlength = typesarray.length();
			for (int i = 0; i < arrlength; i++) {
				try {
					JSONObject obj = typesarray.getJSONObject(i);
					Log.d("dimagi", "type loop: " + i + " model: " + obj.getString("model"));
					if (!obj.getString("model").equals("rapidandroid.fieldtype")) {
						Log.d("dimagi", "###" + obj.getString("model") + "###");
						throw new IllegalArgumentException("Error in parsing fieldtypes.json");
					}

					int pk = obj.getInt("pk");
					JSONObject jsonfields = obj.getJSONObject("fields");
					Log.d("dimagi", "#### Regex from file: " + jsonfields.getString("name") + " ["
							+ jsonfields.getString("regex") + "]");
					SimpleFieldType newtype = new SimpleFieldType(pk, jsonfields.getString("datatype"),
																	jsonfields.getString("regex"),
																	jsonfields.getString("name"));
					fieldTypeHash.put(new Integer(pk), newtype);
				} catch (JSONException e) {
				}
			}
		} catch (JSONException e) {
		}
		try {
			// SAGES/pokuam1: load custom fieldtypes from sdcard but always append
			JSONArray customtypesarray = new JSONArray(customtypes);
			
			int arrlength = customtypesarray.length();
			for (int i = 0; i < arrlength; i++) {
				try {
					JSONObject obj = customtypesarray.getJSONObject(i);
					Log.d("sages", "type loop: " + i + " model: " + obj.getString("model"));
					if (!obj.getString("model").equals("rapidandroid.fieldtype")) {
						Log.d("sages", "###" + obj.getString("model") + "###");
						throw new IllegalArgumentException("Error in parsing fieldtypes.json");
					}
					
					int pk = obj.getInt("pk");
					JSONObject jsonfields = obj.getJSONObject("fields");
					Log.d("sages", "#### Regex from file: " + jsonfields.getString("name") + " ["
							+ jsonfields.getString("regex") + "]");
					SimpleFieldType newtype = new SimpleFieldType(pk, jsonfields.getString("datatype"),
							jsonfields.getString("regex"),
							jsonfields.getString("name"));
					fieldTypeHash.put(new Integer(pk), newtype);
				} catch (JSONException e) {
				}
			}
		} catch (JSONException e) {
		}
		// SAGES/pokuam1: load EXTERNAL (on the sdcard) custom fieldtypes from sdcard but always append
		if (externalcustomtypes != null) {
			try {
				JSONArray externalcustomtypesarray = new JSONArray(externalcustomtypes);

				int arrlength = externalcustomtypesarray.length();
				for (int i = 0; i < arrlength; i++) {
					try {
						JSONObject obj = externalcustomtypesarray.getJSONObject(i);
						Log.d("sages", "type loop: " + i + " model: " + obj.getString("model"));
						if (!obj.getString("model").equals("rapidandroid.fieldtype")) {
							Log.d("sages", "###" + obj.getString("model") + "###");
							throw new IllegalArgumentException("Error in parsing fieldtypes.json");
						}

						int pk = obj.getInt("pk");
						JSONObject jsonfields = obj.getJSONObject("fields");
						Log.d("sages", "#### Regex from file: " + jsonfields.getString("name") + " ["
										+ jsonfields.getString("regex") + "]");
						SimpleFieldType newtype = new SimpleFieldType(pk, jsonfields.getString("datatype"),
								jsonfields.getString("regex"), jsonfields.getString("name"));
						fieldTypeHash.put(new Integer(pk), newtype);
					} catch (JSONException e) {
					}
				}
			} catch (JSONException e) {
			}
		}
	}

	private static void loadInitialFormsFromAssets() {
		parseFieldsFromAssets();
		parseFormsFromAssets();
		
		parseFieldsFromLoadableAssets();
		parseFormsFromLoadableAssets();
	}

	/**
	 * Loads externally defined field types from /sdcard/rapidandroid/loadablefields.json file
	 * @return void
	 */
	// TODO pokuam1 - refactor to reuse original method, see parseFieldsFromAssets()
	private static void parseFieldsFromLoadableAssets() {
		String sdcardFields = loadSdCardFile("/sdcard/rapidandroid/loadablefields.json");

		if (sdcardFields != null){
		try {
			JSONArray fieldsarray = new JSONArray(sdcardFields);
			int arrlength = fieldsarray.length();
			for (int i = 0; i < arrlength; i++) {
				try {
					JSONObject obj = fieldsarray.getJSONObject(i);
					
					if (!obj.getString("model").equals("rapidandroid.field")) {
						
					}
					
					int pk = obj.getInt("pk");
					
					JSONObject jsonfields = obj.getJSONObject("fields");
					int form_id = jsonfields.getInt("form");
					Field newfield = new Field(pk, jsonfields.getInt("sequence"), jsonfields.getString("name"),
							jsonfields.getString("prompt"),
							fieldTypeHash.get(new Integer(jsonfields.getInt("fieldtype"))));
					
					Integer formInt = Integer.valueOf(form_id);
					if (!fieldToFormHash.containsKey(formInt)) {
						fieldToFormHash.put(formInt, new Vector<Field>());
						Log.d("sages", "### adding a key again?!" + formInt);
					}
					fieldToFormHash.get(formInt).add(newfield);
					Log.d("sages", "#### Parsed field: " + newfield.getFieldId() + " [" + newfield.getName()
							+ "] newlength: " + fieldToFormHash.get(formInt).size());
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.d("sages", e.getMessage());
					
				}
			}
		} catch (JSONException e) {
		}
		}
	}
	private static void parseFieldsFromAssets() {
		String fields = loadAssetFile("definitions/fields.json");
		try {
			JSONArray fieldsarray = new JSONArray(fields);
			int arrlength = fieldsarray.length();
			for (int i = 0; i < arrlength; i++) {
				try {
					JSONObject obj = fieldsarray.getJSONObject(i);

					if (!obj.getString("model").equals("rapidandroid.field")) {

					}

					int pk = obj.getInt("pk");

					JSONObject jsonfields = obj.getJSONObject("fields");
					int form_id = jsonfields.getInt("form");
					Field newfield = new Field(pk, jsonfields.getInt("sequence"), jsonfields.getString("name"),
												jsonfields.getString("prompt"),
												fieldTypeHash.get(new Integer(jsonfields.getInt("fieldtype"))));

					Integer formInt = Integer.valueOf(form_id);
					if (!fieldToFormHash.containsKey(formInt)) {
						fieldToFormHash.put(formInt, new Vector<Field>());
						Log.d("dimagi", "### adding a key again?!" + formInt);
					}
					fieldToFormHash.get(formInt).add(newfield);
					Log.d("dimagi", "#### Parsed field: " + newfield.getFieldId() + " [" + newfield.getName()
							+ "] newlength: " + fieldToFormHash.get(formInt).size());

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.d("dimagi", e.getMessage());

				}
			}
		} catch (JSONException e) {
		}
	}

	private static void parseFormsFromAssets() {
		String forms = loadAssetFile("definitions/forms.json");

		try {
			JSONArray formarray = new JSONArray(forms);
			int arrlength = formarray.length();
			for (int i = 0; i < arrlength; i++) {
				try {
					JSONObject obj = formarray.getJSONObject(i);

					if (!obj.getString("model").equals("rapidandroid.form")) {
					}

					int pk = obj.getInt("pk");
					Integer pkInt = new Integer(pk);
					JSONObject jsonfields = obj.getJSONObject("fields");

					Field[] fieldarr = new Field[fieldToFormHash.get(pkInt).size()];
					for (int q = 0; q < fieldarr.length; q++) {
						fieldarr[q] = fieldToFormHash.get(pkInt).get(q);
					}
					Form newform = new Form(pk, jsonfields.getString("formname"), jsonfields.getString("prefix"),
											jsonfields.getString("description"), fieldarr, 
											ParserType.getTypeFromConfig(jsonfields.getString("parsemethod")));
					formIdCache.put(pkInt, newform);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.d("dimagi", e.getMessage());
				}
			}
		} catch (JSONException e) {
		}
	}

	/**
	 * Loads externally defined forms from /sdcard/rapidandroid/loadableforms.json file
	 * @return void
	 * 
	 */
	// TODO pokuam1 - refactor to reuse original method, see parseFormsFromAssets()
	private static void parseFormsFromLoadableAssets() {
		// String forms = loadAssetFile("definitions/forms.json");
		String sdcardForms = loadSdCardFile("/sdcard/rapidandroid/loadableforms.json");

		if (sdcardForms != null) {
			try {
				JSONArray formarray = new JSONArray(sdcardForms);
				int arrlength = formarray.length();
				Integer curPkInt = null;
				for (int i = 0; i < arrlength; i++) {
					try {
						JSONObject obj = formarray.getJSONObject(i);

						if (!obj.getString("model").equals("rapidandroid.form")) {
						}

						int pk = obj.getInt("pk");
						Integer pkInt = new Integer(pk);
						curPkInt = pkInt;
						JSONObject jsonfields = obj.getJSONObject("fields");

						Field[] fieldarr = new Field[fieldToFormHash.get(pkInt)
								.size()];
						for (int q = 0; q < fieldarr.length; q++) {
							fieldarr[q] = fieldToFormHash.get(pkInt).get(q);
						}
						Form newform = new Form(pk,
								jsonfields.getString("formname"),
								jsonfields.getString("prefix"),
								jsonfields.getString("description"), fieldarr,
								ParserType.getTypeFromConfig(jsonfields
										.getString("parsemethod")));
						formIdCache.put(pkInt, newform);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						Log.d("sages", e.getMessage());
					} catch (NullPointerException npe) {
						Log.e("sages", "pkInt: " + curPkInt
								+ "may have caused exception.");
						throw npe;
					}
				}
			} catch (JSONException e) {
			}
		}
	}
	
	private static void checkIfFormTablesExistCreateIfNecessary() {
		// so, todo:
		// check if tables exist
		// else

		Iterator<?> it = formIdCache.entrySet().iterator();

		// for(int i = 0; i < forms.size(); i++) {
		while (it.hasNext()) {
			Map.Entry<Integer, Form> pairs = (Map.Entry<Integer, Form>) it.next();
			Form f = pairs.getValue();

			Log.d("dimagi", "**** inserting form " + f.getFormName());

			// insert the form first
			Uri formUri = Uri.parse(RapidSmsDBConstants.Form.CONTENT_URI_STRING + f.getFormId());
			Cursor crform = mContext.getContentResolver().query(formUri, null, null, null, null);
			boolean newFormInserted = false;
			if (crform.getCount() == 0) {
				ModelTranslator.addFormToDatabase(f);
			}
			crform.close();

		}
	}
}
