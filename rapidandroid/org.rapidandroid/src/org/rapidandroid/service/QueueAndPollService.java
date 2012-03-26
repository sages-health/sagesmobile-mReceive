/*
 * Copyright (©) 2012 The Johns Hopkins University Applied Physics Laboratory.
 * All Rights Reserved.  
 */
package org.rapidandroid.service;

import java.sql.RowId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.rapidandroid.ApplicationGlobals;
import org.rapidandroid.activity.CsvOutputScheduler;
import org.rapidandroid.content.translation.MessageTranslator;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.content.translation.ParsedDataTranslator;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.controller.WorktableDataLayer;
import org.rapidandroid.receiver.SmsParseReceiver;
import org.rapidandroid.receiver.SmsReplyReceiver;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.Monitor;
import org.rapidsms.java.core.parser.IParseResult;
import org.rapidsms.java.core.parser.service.ParsingService;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import sun.misc.HexDumpEncoder;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Service to queue pieces of multi-part sms and poll until all received prior to the 
 * configurable time threshold. After the time threshold is reached for any multi-part sms, then
 * incomplete pieces are deleted. A NACK is sent to the sender.
 *   
 * @author POKUAM1
 * @created Feb 7, 2012
 */
public class QueueAndPollService extends IntentService {

	private static String[] prefixes = null;
	private static Form[] forms = null;
	private static SmsManager smsManager = null;
	/**
	 * @param name
	 */
	public QueueAndPollService() {
		super("QueueAndPollService");
	}
	
	/* (non-Javadoc)
	 * @see android.app.IntentService#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("Q&P", "Service creating.");
		SmsParseReceiver.initFormCache(super.getApplication());
		prefixes = SmsParseReceiver.getPrefixes();
		forms = SmsParseReceiver.getForms();
		smsManager = SmsManager.getDefault();
	}
	/* (non-Javadoc)
	 * @see android.app.Service#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.e("Q&P", "low memory point occurred. fyi.");
	}
	
	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		
		Form[] forms = (Form[]) intent.getSerializableExtra("org.rapidandroid.AllForms");
		String[] prefixes = intent.getStringArrayExtra("formPrefixes");
		int monitor_msg_id = intent.getIntExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, 0);
		String sender_phone = intent.getStringExtra(RapidSmsDBConstants.Message.PHONE);
		
		Log.d("QueueAndPollService", "queue & poll service being handled");
		Map<String, List<Long>> statusMap = null;
		Map<String, List<Long>> ttlMap = null;
		List<Long> completed = null;
		List<Long> incompleted = null;
		// get status Map(complete, incomplete, bad)
		//     and THEN
		// get ttl Map(stale incomplete, live incomplete)
		try {
			// process completed ("complete & live", "complete & stale")
			statusMap = WorktableDataLayer.categorizeCompleteVsIncomplete(this);
			completed = statusMap.get(WorktableDataLayer.label_complete);
			incompleted = statusMap.get(WorktableDataLayer.label_incomplete);
			
			// delete remaining stale records ("incomplete & stale", 
			ttlMap = WorktableDataLayer.categorizeStaleVsLive(this, incompleted);
			
		} catch (Exception e) {
			Log.e("QueueAndPollService", "unable to retrieve message and ttl statuses");
			e.printStackTrace();
			smsManager.sendTextMessage(sender_phone, null, "server troubles finding stale ttls", null, null);
		}
		// delete & NACK for: incomplete and stale
		Log.d("Queue&PollService", "NACK for the incomplete txIds.");
		String debugTTLStale = StringUtils.join(ttlMap.get(WorktableDataLayer.label_ttlStale), ",");
		String debugTTLLive = StringUtils.join(ttlMap.get(WorktableDataLayer.label_ttlLive), ",");
		
		Log.d("Q&P service", "Stale ids: " + debugTTLStale);
		Log.d("Q&P service", "Live ids: " + debugTTLLive);
		
		WorktableDataLayer.beginTransaction(this);
		try {
			// DELETE: incomplete and stale
			if (!ttlMap.get(WorktableDataLayer.label_ttlStale).isEmpty()) {
				WorktableDataLayer.deleteStaleIncompleteTxIds(this, ttlMap.get(WorktableDataLayer.label_ttlStale));
		
				// NACK: incomplete and stale TODO
				// lookupNumbersForStaleTxIds
				
				int i = 1;
				//TODO: why is the 1st one not being sent???
				for (Long txId: ttlMap.get(WorktableDataLayer.label_ttlStale)){
					smsManager.sendTextMessage(sender_phone, null, i + "_NACK--txid=" + txId + " was stale", null, null);
					i++;
				}
			}
			Log.d("Queue&PollService", "SERVICE(concat->DeMod->Parse&Val)");
			// startSERVICE(concat->DeMod->Parse&Val)
			if (completed.isEmpty()){
				WorktableDataLayer.setTransactionSuccessful();
				//WorktableDataLayer.endTransaction(); //is still called in the finally branch
				Log.d("Q&P", "no complete records, exiting.");
				return;
			}
			Map<Long, String> concatMap = WorktableDataLayer.getConcatenatedMessagesForTxIds(this, statusMap.get(WorktableDataLayer.label_complete));
			final Map<Long, String[]> demodedBlobs = Demodulator.deModulateBlobMap(concatMap);
//			concatMap=null; //TODO trying to conserve mem?
			
			//pass of demodedBlobs to the Parsing stuff
			Intent parseOutcome = null;
			Intent broadcast = new Intent("android.provider.Telephony.SMS_RECEIVED");
			Set<Form> outputCsvSet = new HashSet<Form>();

			//TODO setup a try catch finally??
			for (Entry<Long, String[]> allBlobs : demodedBlobs.entrySet()){
				
				String successfulFormsTxId = "";
				Long txId = allBlobs.getKey();
				String[] parseableBlobs = allBlobs.getValue();
				
				WorktableDataLayer.beginTransaction(this);
				boolean badData = false;
				
				for (String body: parseableBlobs){
					badData = false;
					
					broadcast.putExtra("body", body);
					broadcast.putExtra("from", sender_phone);
					broadcast.putExtra("txId", txId);
					broadcast.putExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, monitor_msg_id);
					
					//this.sendBroadcast(broadcast);
					parseOutcome = SmsParseUtility.parseOnCall(this, broadcast, this.forms, this.prefixes);
					long rowOutcome = parseOutcome.getLongExtra("rowid", -1);
					Form formToCsvOutput = (Form)parseOutcome.getSerializableExtra("form");
//					if (parseOutcome.getLongExtra("rowid", -1) == -1){
					if (rowOutcome == -1){
						badData = true;
//						
//						// throw ERROR SEND NACK MESSAGE FOR SURE!
//						if (ApplicationGlobals.doReplyOnFail()){
//							smsManager.sendTextMessage(sender_phone, null, "NACK--txid=" + txId + " error in parsing", null, null);
//						}
//
//						// delete problematic ones? OR JUST LET IT GET STALE???
						break;
					} else {
						if (formToCsvOutput != null) outputCsvSet.add(formToCsvOutput);
					} 
				}
				
				if (badData) {
					WorktableDataLayer.endTransaction();
				} else {
					// delete processed messages from worktable if not empty
					if (!statusMap.get(WorktableDataLayer.label_complete).isEmpty()){
						WorktableDataLayer.deleteTxIds(this, statusMap.get(WorktableDataLayer.label_complete));
					}	
					
					//setSuccessful
					WorktableDataLayer.setTransactionSuccessful();
					WorktableDataLayer.endTransaction();

					// fire off csv out put for formIds in stack
					String successfulForms = "";
					for (Form form: outputCsvSet){
						successfulForms += form.getPrefix() + " ";
						int formId = form.getFormId();
						String formPrefix = form.getPrefix();
						Intent csvIntent = new Intent(this, CsvOutputService.class);
						csvIntent.putExtra("formId", formId);
						csvIntent.putExtra("formPrefix", formPrefix);
						startService(csvIntent);
					}
					
					successfulFormsTxId = successfulForms;
				}
					
				if (ApplicationGlobals.doReplyOnParse()){
					smsManager.sendTextMessage(sender_phone, null, txId + " was completely successful for: " + successfulFormsTxId, null, null);
				}
			}
			
			WorktableDataLayer.setTransactionSuccessful();
		} catch (Exception e){
			e.printStackTrace();
			Log.e("Q&Pservice ERROR",e.getMessage());
		}finally {
			WorktableDataLayer.endTransaction();
		}
		Toast.makeText(this, "queue & poll service", Toast.LENGTH_SHORT);
	}
	
	

//public static class SmsParseReceiver extends BroadcastReceiver {
public static class SmsParseUtility {

	private static String[] prefixes = null;
	private static Form[] forms = null;
	
	
	

	// private Context mContext = null;

	public synchronized static void initFormCache(Form[] forms, String[] prefixes) {
		setFormsAndPrefixes(forms, prefixes);
//		forms = ModelTranslator.getAllForms();
//		prefixes = new String[forms.length];
//		for (int i = 0; i < forms.length; i++) {
//			prefixes[i] = forms[i].getPrefix();
//		}
	}

	public static Form determineForm(String message) {
		int len = prefixes.length;
		for (int i = 0; i < len; i++) {
			String prefix = prefixes[i];
			if (message.toLowerCase().trim().startsWith(prefix + " ")) {
				return forms[i];
			}
		}
		return null;
	}

	public static void setFormsAndPrefixes(Form[] tmpForms, String[] tmpPrefixes){
		prefixes = tmpPrefixes;
		forms = tmpForms;
	}
	/**
	 * Upon message receipt, determine the form in question, then call the
	 * corresponding parsing logic.
	 */
//	@Override
//	public void onReceive(Context context, Intent intent) {
	public static Intent parseOnCall(Context context, Intent intent, Form[] tmpForms, String[] tmpPrefixes) {
		ApplicationGlobals.initGlobals(context);
		Intent outcome = new Intent();
		long rowid = -1;
		outcome.putExtra("rowid", rowid);
		
		Long txId = intent.getLongExtra("txId", 0);
		if (prefixes == null) {
			initFormCache(tmpForms, tmpPrefixes); // profiler shows us that this is being called
								// frequently on new messages.
		}
		// TODO Auto-generated method stub
		String body = intent.getStringExtra("body");

		if (body.startsWith("notifications@dimagi.com /  / ")) {
			body = body.replace("notifications@dimagi.com /  / ", "");
			Log.d("SmsParseReceiver", "Debug, snipping out the email address");
		}

//		int msgid = intent.getIntExtra("msgid", 0);
		int msgid = intent.getIntExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, 0);

		Form form = determineForm(body);
		if (form == null) {			
			if (ApplicationGlobals.doReplyOnFail()) {
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseFailText() + ", form is null from txId("+ txId +").");
				context.sendBroadcast(broadcast);
			}
			outcome.putExtra("rowid", -1);
			return outcome;
		} else {
			//TODO -- need to pull this out. it's a CRUD operation and will break things. wonder if can pass in db con?
			if (false){
				Monitor mon = MessageTranslator.GetMonitorAndInsertIfNew(context, intent.getStringExtra("from"));
			}
			// if(mon.getReplyPreference()) {
			if (ApplicationGlobals.doReplyOnParseInProgress()) {
				// for debug purposes, we'll just ack every time.
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseInProgressText());
				//context.sendBroadcast(broadcast);
				outcome.putExtra("strategy_ReplyOnParseInProgress", broadcast);
			}
			
			// TODO POKU "body" is the sms that we want to pass along
			SharedPreferences pref = context.getSharedPreferences(CsvOutputScheduler.sharedPreferenceFilename, Context.MODE_PRIVATE);
			Vector<IParseResult> results = ParsingService.ParseMessage(form, body);

			// parse success reply
			if (ApplicationGlobals.doReplyOnParse() && results != null) {
				// for debug purposes, we'll just ack every time.
				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseSuccessText());
				if (false){context.sendBroadcast(broadcast);} // TODO figure strategy for this. 
			}
			
			// parse failure reply
			// results would be null after the result of a StrictParser
			if (results == null) {
				if (ApplicationGlobals.doReplyOnFail()){
					Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
					broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
					broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseFailText(form) + ", null results from txId("+ txId +").");
					context.sendBroadcast(broadcast);
				}
				return outcome;
			}
			
			if (false){ //TODO delete when fixed
				ParsedDataTranslator.InsertFormData(context, form, msgid, results);
			}
			 rowid = WorktableDataLayer.InsertFormData(context, form, msgid, results);
			 outcome.putExtra("rowid", rowid);
//			 outcome.putExtra("formId", form.getFormId());
//			 outcome.putExtra("formPrefix", form.getFormId());
			 outcome.putExtra("form", form);
			 
			 Log.d("Q&P.parseOnCall()", "success insert into form data");
			// broadcast for the automatic csv output service
			Intent broadcastStartCsvOutput = new Intent("org.rapidandroid.intents.SMS_REPLY_CSV_GO");
			broadcastStartCsvOutput.putExtra("formId", form.getFormId());
			broadcastStartCsvOutput.putExtra("formName", form.getFormName());
			broadcastStartCsvOutput.putExtra("formPrefix", form.getPrefix());
			boolean skip = true;
			if (!skip){
				context.sendBroadcast(broadcastStartCsvOutput);
			}
			// broadcast for the SMS Forwarding Activity
			final int formId = form.getFormId();
			boolean isFwdOn = pref.getBoolean(formId + CsvOutputScheduler.FORWARDING_VAR, false);
			if (isFwdOn){
				Intent broadcastForwardSMS= new Intent("org.rapidandroid.intents.SMS_FORWARD");
				//broadcastForwardSMS.putExtra("formId", formId);
				//broadcastForwardSMS.putExtra("formName", form.getFormName());
				//broadcastForwardSMS.putExtra("formPrefix", form.getPrefix());
				broadcastForwardSMS.putExtra("msg", body);
				broadcastForwardSMS.putExtra("forwardNums", pref.getString(form.getFormId() + CsvOutputScheduler.FORWARDING_NUMS, "").split(",")); 
				context.sendBroadcast(broadcastForwardSMS);
			}
		}
		return outcome;
	}
}
	
}

/* 
 * 
 * 
 
 * 
 * 
 * */


//		// get cursor of: incomplete and stale
//		Cursor cursorIncompletes = WorktableDataLayer.getIncompleteTxIds(this);
//		
//		int colIndx = cursorIncompletes.getColumnIndex("tx_id");
//		List<Long> incompleteTxIds = new ArrayList<Long>();
//		while (cursorIncompletes.moveToNext()){
//			long txId = cursorIncompletes.getLong(colIndx);
//			incompleteTxIds.add(txId);
//		}



//		// get cursor of complete:
//		Cursor cursorCompletes = WorktableDataLayer.getCompleteTxIds(this);
//		List<Long> completeTxIds = new ArrayList<Long>();
//		while (cursorCompletes.moveToNext()){
//			long txId = cursorCompletes.getLong(colIndx);
//			completeTxIds.add(txId);
//		}