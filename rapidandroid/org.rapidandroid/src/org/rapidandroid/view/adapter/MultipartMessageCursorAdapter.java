/**
 * 
 */
package org.rapidandroid.view.adapter;

import java.util.Date;

import org.rapidandroid.view.adapter.MessageCursorAdapter.SimpleMessageView;
import org.rapidsms.java.core.model.Message;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author pokuam1
 * @created May 3, 2012
 */
public class MultipartMessageCursorAdapter extends MessageCursorAdapter {

	/**
	 * @param context
	 * @param c
	 */
	public MultipartMessageCursorAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.CursorAdapter#bindView(android.view.View,
	 * android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view != null) {
			//{tx_id=3, payload=5, tx_timestamp=4, monitor_msg_id=6, 
			//total_segments=2, _id=0, segment_number=1}
//			int MonitorID = cursor.getInt(2);
//			String timestamp = cursor.getString(3);
//			String message = cursor.getString(5);
//			boolean isoutgoing = Boolean.parseBoolean(cursor.getString(5));

			//	[MON_ID, alias, _id, segment_number, total_segments, tx_id, tx_timestamp, payload, monitor_msg_id, _id, phone, monitor_id, time, message, is_outgoing, is_virtual, receive_time, _id, first_name, last_name, alias, phone, email, incoming_messages, receive_reply]
			int MonitorID = cursor.getInt(0);//0
			String timestamp = cursor.getString(6);//6
			String message = cursor.getString(13);//13
			boolean isoutgoing = Boolean.parseBoolean(cursor.getString(13));//13
			Date hackDate = new Date();
			
			boolean success = false;
			try {
				hackDate = Message.SQLDateFormatter.parse(timestamp);
				success = true;
			} catch (Exception ex) {
				success = false;
			}

			SimpleMessageView srv = (SimpleMessageView) view;
			srv.setData(message, hackDate, MonitorID, isoutgoing);
		}

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.CursorAdapter#newView(android.content.Context,
	 * android.database.Cursor, android.view.ViewGroup)
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
//		int MonitorID = cursor.getInt(2);//0
//		String timestamp = cursor.getString(3);//6
//		String message = cursor.getString(5);//13
//		boolean isoutgoing = Boolean.parseBoolean(cursor.getString(5));//13
		int MonitorID = cursor.getInt(0);//0
		String timestamp = cursor.getString(6);//6
		String message = cursor.getString(13);//13
		boolean isoutgoing = Boolean.parseBoolean(cursor.getString(13));//13
		Date hackDate = new Date();
//		[MON_ID, alias, _id, segment_number, total_segments, tx_id, tx_timestamp, payload, monitor_msg_id, _id, phone, monitor_id, time, message, is_outgoing, is_virtual, receive_time, _id, first_name, last_name, alias, phone, email, incoming_messages, receive_reply]
		SimpleMessageView srv = new SimpleMessageView(context, message, hackDate, MonitorID, isoutgoing);
		return srv;
	}
}
