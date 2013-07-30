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

import java.util.HashMap;
import java.util.Vector;

import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.Message;
import org.rapidsms.java.core.parser.IParseResult;
import org.rapidsms.java.core.parser.SimpleParseResult;
import org.rapidsms.java.core.parser.token.ITokenParser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Helper class to simplify the insertion and querying of parsed form data from
 * the content provider
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 21, 2009
 * 
 */

public class ParsedDataTranslator {

	/**
	 * Called after a message is parsed. For a given form, the results are put
	 * into a ParseResult for each field, typed out according to the fieldtype
	 * 
	 * @param context
	 * @param f
	 * @param message_id
	 * @param results
	 * @return
	 */
	public static boolean InsertFormData(Context context, Form f, int message_id, Vector<IParseResult> results) {

		ContentValues cv = new ContentValues();
		cv.put(RapidSmsDBConstants.FormData.MESSAGE, message_id);
		Field[] fields = f.getFields();
		int len = fields.length;
//		Random r = new Random();

		for (int i = 0; i < len; i++) {
			Field field = fields[i];
			IParseResult res = results.get(i);
			if (res != null) {
				cv.put(RapidSmsDBConstants.FormData.COLUMN_PREFIX + field.getName(), res.getValue().toString());
			} else {
				cv.put(RapidSmsDBConstants.FormData.COLUMN_PREFIX + field.getName(), "");
			}
		}
		@SuppressWarnings("unused")
		Uri inserted = context.getContentResolver().insert(
															Uri.parse(RapidSmsDBConstants.FormData.CONTENT_URI_PREFIX
																	+ f.getFormId()), cv);
		return true;
	}

	/**
	 * An expensive call to get all parsed messages for a given Form. Loads it
	 * all into memory.
	 * 
	 * @deprecated
	 * @param context
	 * @param f
	 * @return
	 */
	@Deprecated
	public static HashMap<Message, IParseResult[]> getParsedMessagesForForm(Context context, Form f) {

		HashMap<Message, IParseResult[]> ret = new HashMap<Message, IParseResult[]>();
		int formid = f.getFormId();
		Field[] formFields = f.getFields();
		int fieldsLen = formFields.length;

		Cursor cursor = context.getContentResolver().query(
															Uri.parse(RapidSmsDBConstants.FormData.CONTENT_URI_PREFIX
																	+ formid), null, null, null, null);
		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		cursor.moveToFirst();
		do {
			int message_id = cursor.getInt(Message.COL_PARSED_MESSAGE_ID);
			Message msg = MessageTranslator.GetMessage(context, message_id);

			IParseResult[] singleParsed = new SimpleParseResult[cursor.getColumnCount() - 2];

			int fieldcount = 0;
			for (int i = 0; i < fieldsLen; i++) {
				ITokenParser sfieldType = formFields[i].getFieldType();
				Object parsedValue = null;
				String type = sfieldType.getParsedDataType();

				if (type.equals("boolean")) {
					parsedValue = cursor.getInt(i + Message.COL_PARSED_FIELDS_OFFSET) != 0;
				} else if (type.equals("number")) {
					parsedValue = cursor.getFloat(i + Message.COL_PARSED_FIELDS_OFFSET);
				} else if (type.equals("word")) {
					parsedValue = cursor.getString(i + Message.COL_PARSED_FIELDS_OFFSET);
				} else if (type.equals("float")) {
					parsedValue = cursor.getFloat(i + Message.COL_PARSED_FIELDS_OFFSET);
				} else if (type.equals("integer")) {
					parsedValue = cursor.getInt(i + Message.COL_PARSED_FIELDS_OFFSET);
				} else if (type.equals("datemdy")) {
					parsedValue = cursor.getLong(i + Message.COL_PARSED_FIELDS_OFFSET);
				}
				
				SimpleParseResult res = new SimpleParseResult(formFields[i].getFieldType(), null, parsedValue);
				singleParsed[fieldcount++] = res;
			}
			ret.put(msg, singleParsed);

		} while (cursor.moveToNext());
		cursor.close();

		return ret;
	}

}
