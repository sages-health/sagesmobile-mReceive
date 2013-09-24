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

package org.rapidandroid.receiver;

import java.util.Locale;
import java.util.Vector;

import org.rapidandroid.ApplicationGlobals;
import org.rapidandroid.SystemHealthTracking;
import org.rapidandroid.SystemHealthTracking.SagesEventType;
import org.rapidandroid.activity.CsvOutputScheduler;
import org.rapidandroid.content.translation.MessageTranslator;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.content.translation.ParsedDataTranslator;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.Monitor;
import org.rapidsms.java.core.parser.IParseResult;
import org.rapidsms.java.core.parser.service.ParsingService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Second level broadcast receiver. The idea is upon a successful SMS message
 * save, a separate receiver will be triggered to handle the actual parsing and
 * processing of the message.
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 12, 2009
 * 
 */
public class SmsParseReceiver extends BroadcastReceiver {
	private static SystemHealthTracking healthTracker = new SystemHealthTracking(SmsParseReceiver.class);

	private static String[] prefixes = null;
	private static Form[] forms = null;
	
	
	

	// private Context mContext = null;

	public synchronized static void initFormCache() {
		forms = ModelTranslator.getAllForms();
		prefixes = new String[forms.length];
		for (int i = 0; i < forms.length; i++) {
			prefixes[i] = forms[i].getPrefix();
		}
	}
	
	public synchronized static void initFormCache(Context context) {
		forms = ModelTranslator.getAllForms(context);
		prefixes = new String[forms.length];
		for (int i = 0; i < forms.length; i++) {
			prefixes[i] = forms[i].getPrefix();
		}
	}

	public static String[] getPrefixes() {
		return prefixes;
	}

	public static Form[] getForms() {
		return forms;
	}

	public static Form determineForm(String message) {
		int len = prefixes.length;
		for (int i = 0; i < len; i++) {
			String prefix = prefixes[i];
			if (message.toLowerCase(Locale.getDefault()).trim().startsWith(prefix + " ")) {
				return forms[i];
			}
		}
		return null;
	}

	/**
	 * Upon message receipt, determine the form in question, then call the
	 * corresponding parsing logic.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		ApplicationGlobals.initGlobals(context);
	
		if (prefixes == null) {
			initFormCache(); // profiler shows us that this is being called
								// frequently on new messages.
		}
		// TODO Auto-generated method stub
		String body = intent.getStringExtra("body");

		if (body.startsWith("notifications@dimagi.com /  / ")) {
			body = body.replace("notifications@dimagi.com /  / ", "");
			Log.d("SmsParseReceiver", "Debug, snipping out the email address");
		}

		int msgid = intent.getIntExtra("msgid", 0);

		Form form = determineForm(body);
		if (form == null) {
			healthTracker.logInfo(SagesEventType.SMS_PARSE_FAIL, "No form matched");
			if (ApplicationGlobals.doReplyOnFail()) {
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseFailText());
				context.sendBroadcast(broadcast);
			}
			return;
		} else {
			@SuppressWarnings("unused")
			Monitor mon = MessageTranslator.GetMonitorAndInsertIfNew(context, intent.getStringExtra("from"));
			// if(mon.getReplyPreference()) {
			if (ApplicationGlobals.doReplyOnParseInProgress()) {
				// for debug purposes, we'll just ack every time.
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseInProgressText());
				context.sendBroadcast(broadcast);
			}
			
			// TODO POKU "body" is the sms that we want to pass along
			SharedPreferences pref = context.getSharedPreferences(CsvOutputScheduler.sharedPreferenceFilename, Context.MODE_PRIVATE);
			Vector<IParseResult> results = ParsingService.ParseMessage(form, body);
			
			if (results != null){
				ParsedDataTranslator.InsertFormData(context, form, msgid, results);
			}
			
			// parse success reply
			if (ApplicationGlobals.doReplyOnParse() && results != null) {
				// for debug purposes, we'll just ack every time.
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseSuccessText());
				context.sendBroadcast(broadcast);
				
				healthTracker.logInfo(SagesEventType.SMS_PARSE_SUCCESS, "Success messaged parsed for " + form.getPrefix());

			}
			
			// parse failure reply
			// results would be null after the result of a StrictParser
			if (results == null) {
				healthTracker.logInfo(SagesEventType.SMS_PARSE_FAIL, "Failure, message not parsed properly for " + form.getPrefix());

				
				if (ApplicationGlobals.doReplyOnFail()){
					Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
					broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
					broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseFailText(form));
					context.sendBroadcast(broadcast);
				}
				return;
			}
			

			// SAGES/pokuam1: broadcast for the automatic csv output service
			Intent broadcastStartCsvOutput = new Intent("org.rapidandroid.intents.SMS_REPLY_CSV_GO");
			broadcastStartCsvOutput.putExtra("formId", form.getFormId());
			broadcastStartCsvOutput.putExtra("formName", form.getFormName());
			broadcastStartCsvOutput.putExtra("formPrefix", form.getPrefix());
			context.sendBroadcast(broadcastStartCsvOutput);
			
			// SAGES/pokuam1: broadcast for the SMS Forwarding Activity
			final int formId = form.getFormId();
			boolean isFwdOn = pref.getBoolean(formId + CsvOutputScheduler.FORWARDING_VAR, false);
			if (isFwdOn){
				Intent broadcastForwardSMS= new Intent("org.rapidandroid.intents.SMS_FORWARD");
				broadcastForwardSMS.putExtra("msg", body);
				broadcastForwardSMS.putExtra("forwardNums", pref.getString(form.getFormId() + CsvOutputScheduler.FORWARDING_NUMS, "").split(",")); 
				context.sendBroadcast(broadcastForwardSMS);
			}
		}
	}
}
