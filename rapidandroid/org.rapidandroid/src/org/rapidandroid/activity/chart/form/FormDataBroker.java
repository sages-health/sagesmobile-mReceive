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

package org.rapidandroid.activity.chart.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rapidandroid.activity.chart.ChartBroker;
import org.rapidandroid.activity.chart.JSONGraphData;
import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.controller.ParsedDataReporter;
import org.rapidsms.java.core.Constants;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.Message;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.webkit.WebView;

public class FormDataBroker extends ChartBroker {
	public static final int PLOT_ALL_MESSAGES_FOR_FORM = 0;
	public static final int PLOT_NUMERIC_FIELD_VALUE = 1;
	public static final int PLOT_NUMERIC_FIELD_ADDITIVE = 2;
	public static final int PLOT_WORD_HISTOGRAM = 3;
	public static final int PLOT_NUMERIC_FIELD_COUNT_HISTOGRAM = 4;

	private Form mForm;
	private Field fieldToPlot;

	public FormDataBroker(Activity parentActivity, WebView appView, Form form, Date startDate, Date endDate) {
		super(parentActivity, appView, startDate, endDate);
		mForm = form;
		mVariableStrings = new String[mForm.getFields().length + 1];
		mVariableStrings[0] = "Messages over time";
		for (int i = 1; i < mVariableStrings.length; i++) {
			Field f = mForm.getFields()[i - 1];
			mVariableStrings[i] = f.getName() + "  [" + f.getFieldType().getParsedDataType() + "]";
		}
	}

	@Override
	public void doLoadGraph() {
		// mProgress = ProgressDialog.show(mAppView.getContext(),
		// "Rendering Graph...", "Please Wait",true,false);
		JSONGraphData allData = null;

		if (fieldToPlot == null) {
			// we're going to do all messages over timereturn;
			allData = loadMessageOverTimeHistogram();
		} else if (fieldToPlot.getFieldType().getParsedDataType().toLowerCase(Locale.getDefault()).equals("word")) {
			allData = loadHistogramFromField();
		} else if (fieldToPlot.getFieldType().getParsedDataType().toLowerCase(Locale.getDefault()).equals("boolean")
				|| fieldToPlot.getFieldType().getParsedDataType().toLowerCase(Locale.getDefault()).equals("yes/no")) {
			allData = loadBooleanPlot();
		} else {
			allData = loadNumericLine();
			// data.put(loadNumericLine());
		}
		if (allData != null) {
			mGraphData = allData.getData();
			mGraphOptions = allData.getOptions();
		}
		Log.d("FormDataBroker", mGraphData.toString());
		Log.d("FormDataBroker", mGraphOptions.toString());
	}

	private JSONGraphData loadBooleanPlot() {

		Date startDateToUse = getStartDate();
		DateDisplayTypes displayType = this.getDisplayType(startDateToUse, mEndDate);

		String selectionArg = getSelectionString(displayType);

		StringBuilder rawQuery = new StringBuilder();

		String fieldcol = RapidSmsDBConstants.FormData.COLUMN_PREFIX + fieldToPlot.getName();

		rawQuery.append("select time, " + fieldcol + ", count(*) from  ");
		rawQuery.append(RapidSmsDBConstants.FormData.TABLE_PREFIX + mForm.getPrefix());

		rawQuery.append(" join rapidandroid_message on (");
		rawQuery.append(RapidSmsDBConstants.FormData.TABLE_PREFIX + mForm.getPrefix());
		rawQuery.append(".message_id = rapidandroid_message._id");
		rawQuery.append(") ");
		if (startDateToUse.compareTo(Constants.NULLDATE) != 0 && mEndDate.compareTo(Constants.NULLDATE) != 0) {
			rawQuery.append(" WHERE rapidandroid_message.time > '" + Message.SQLDateFormatter.format(startDateToUse)
					+ "' AND rapidandroid_message.time < '" + Message.SQLDateFormatter.format(mEndDate) + "' ");
		}

		rawQuery.append(" group by ").append(selectionArg).append(", " + fieldcol);
		rawQuery.append(" order by ").append("time").append(" ASC");

		SQLiteDatabase db = rawDB.getReadableDatabase();
		// the string value is column 0
		// the magnitude is column 1
		Log.d("query", rawQuery.toString());
		Cursor cr = db.rawQuery(rawQuery.toString(), null);
		// TODO Auto-generated method stub
		int barCount = cr.getCount();
		Date[] allDates = new Date[barCount];
		if (barCount == 0) {
			db.close();
			cr.close();
		} else {
			List<Date> xValsTrue = new ArrayList<Date>();
			// Date[] xValsTrue = new Date[barCount];
			List<Integer> yValsTrue = new ArrayList<Integer>();
			List<Date> xValsFalse = new ArrayList<Date>();
			// Date[] xValsTrue = new Date[barCount];
			List<Integer> yValsFalse = new ArrayList<Integer>();
			cr.moveToFirst();
			int i = 0;
			do {
				String trueFalse = cr.getString(1);
				// int trueFalse2 = cr.getInt(fieldcol);
				// String trueFalseStr = cr.getString(1);

				Date thisDate = getDate(displayType, cr.getString(0));
				Log.d("FormDataBroker: ", cr.getString(0) + ", " + trueFalse + " , " + cr.getInt(2));

				if (trueFalse.equals("true")) {
					xValsFalse.add(thisDate);
					yValsFalse.add(new Integer(cr.getInt(2)));
				} else {
					xValsTrue.add(thisDate);
					yValsTrue.add(new Integer(cr.getInt(2)));
				}
				allDates[i] = thisDate;
				i++;
			} while (cr.moveToNext());

			try {
//				String legend = this.getLegendString(displayType);
				int[] yVals = getIntsFromList(yValsTrue);
				JSONArray trueArray = getJSONArrayForValues(displayType, xValsTrue.toArray(new Date[0]), yVals);
				yVals = getIntsFromList(yValsFalse);
				JSONArray falseArray = getJSONArrayForValues(displayType, xValsFalse.toArray(new Date[0]), yVals);
				JSONArray finalValues = new JSONArray();
				JSONObject trueElem = new JSONObject();
				trueElem.put("data", trueArray);
				trueElem.put("label", "Yes");
				trueElem.put("lines", getShowTrue());
				finalValues.put(trueElem);
				JSONObject falseElem = new JSONObject();
				falseElem.put("data", falseArray);
				falseElem.put("label", "No");
				falseElem.put("lines", getShowTrue());
				finalValues.put(falseElem);
				return new JSONGraphData(finalValues, loadOptionsForDateGraph(allDates, true, displayType));

			} catch (Exception ex) {

			} finally {
				if (!cr.isClosed()) {

					cr.close();
				}
				if (db.isOpen()) {
					db.close();
				}
			}
		}
		// either there was no data or something bad happened
		return new JSONGraphData(getEmptyData(), new JSONObject());
	}

	private int[] getIntsFromList(List<Integer> values) {
		int[] toReturn = new int[values.size()];
		for (int i = 0; i < values.size(); i++) {
			toReturn[i] = values.get(i);
		}
		return toReturn;
	}

	private JSONGraphData loadNumericLine() {
		Date startDateToUse = getStartDate();

		SQLiteDatabase db = rawDB.getReadableDatabase();

		String fieldcol = RapidSmsDBConstants.FormData.COLUMN_PREFIX + fieldToPlot.getName();
		StringBuilder rawQuery = new StringBuilder();
		rawQuery.append("select rapidandroid_message.time, " + fieldcol);
		rawQuery.append(" from ");
		rawQuery.append(RapidSmsDBConstants.FormData.TABLE_PREFIX + mForm.getPrefix());

		rawQuery.append(" join rapidandroid_message on (");
		rawQuery.append(RapidSmsDBConstants.FormData.TABLE_PREFIX + mForm.getPrefix());
		rawQuery.append(".message_id = rapidandroid_message._id");
		rawQuery.append(") ");

		if (startDateToUse.compareTo(Constants.NULLDATE) != 0 && mEndDate.compareTo(Constants.NULLDATE) != 0) {
			rawQuery.append(" WHERE rapidandroid_message.time > '" + Message.SQLDateFormatter.format(startDateToUse)
					+ "' AND rapidandroid_message.time < '" + Message.SQLDateFormatter.format(mEndDate) + "' ");
		}

		rawQuery.append(" order by rapidandroid_message.time ASC");

		// the string value is column 0
		// the magnitude is column 1

		Cursor cr = db.rawQuery(rawQuery.toString(), null);
		int barCount = cr.getCount();

		if (barCount == 0) {
			cr.close();
		} else {
			Date[] xVals = new Date[barCount];
			int[] yVals = new int[barCount];
			cr.moveToFirst();
			int i = 0;
			do {
				try {
					xVals[i] = Message.SQLDateFormatter.parse(cr.getString(0));
					yVals[i] = cr.getInt(1);
				} catch (Exception ex) {

				}
				i++;
			} while (cr.moveToNext());

			// xaxis: { ticks: [0, [Math.PI/2, "\u03c0/2"], [Math.PI, "\u03c0"],
			// [Math.PI * 3/2, "3\u03c0/2"], [Math.PI * 2, "2\u03c0"]]},

			try {
				return new JSONGraphData(prepareDateData(xVals, yVals), loadOptionsForDateGraph(xVals, false,
																								DateDisplayTypes.Daily));
			} catch (Exception ex) {

			} finally {
				if (!cr.isClosed()) {
					cr.close();
				}
			}

		}
		// either there was no data or something bad happened
		return new JSONGraphData(getEmptyData(), new JSONObject());
	}

	private JSONArray prepareDateData(Date[] xvals, int[] yvals) {
		JSONArray outerArray = new JSONArray();
		JSONArray innerArray = new JSONArray();
		int datalen = xvals.length;
		for (int i = 0; i < datalen; i++) {
			JSONArray elem = new JSONArray();
			elem.put(xvals[i].getTime());
			elem.put(yvals[i]);
			innerArray.put(elem);
		}
		outerArray.put(innerArray);
		return outerArray;
	}

	private JSONGraphData loadMessageOverTimeHistogram() {
		Date startDateToUse = getStartDate();
		DateDisplayTypes displayType = this.getDisplayType(startDateToUse, mEndDate);

		String selectionArg = getSelectionString(displayType);

		StringBuilder rawQuery = new StringBuilder();

		rawQuery.append("select time, count(*) from  ");
		rawQuery.append(RapidSmsDBConstants.FormData.TABLE_PREFIX + mForm.getPrefix());

		rawQuery.append(" join rapidandroid_message on (");
		rawQuery.append(RapidSmsDBConstants.FormData.TABLE_PREFIX + mForm.getPrefix());
		rawQuery.append(".message_id = rapidandroid_message._id");
		rawQuery.append(") ");
		if (startDateToUse.compareTo(Constants.NULLDATE) != 0 && mEndDate.compareTo(Constants.NULLDATE) != 0) {
			rawQuery.append(" WHERE rapidandroid_message.time > '" + Message.SQLDateFormatter.format(startDateToUse)
					+ "' AND rapidandroid_message.time < '" + Message.SQLDateFormatter.format(mEndDate) + "' ");
		}

		rawQuery.append(" group by ").append(selectionArg);
		rawQuery.append("order by ").append(selectionArg).append(" ASC");

		// the X date value is column 0
		// the y value magnitude is column 1
		SQLiteDatabase db = rawDB.getReadableDatabase();
		Cursor cr = db.rawQuery(rawQuery.toString(), null);
		return getDateQuery(displayType, cr, db);

	}

	private Date getStartDate() {
		// TODO Auto-generated method stub
		Date firstDateFromForm = ParsedDataReporter.getOldestMessageDate(rawDB, mForm);
		if (firstDateFromForm.after(mStartDate)) {
			// first date in the form is more recent than the start date, so
			// just go with that.
			return firstDateFromForm;
		} else {
			return mStartDate;
		}
	}

	/**
	 * Should return a two element array - the first element is the data, the
	 * second are the options
	 * 
	 * @return
	 */
	private JSONGraphData loadHistogramFromField() {
		// JSONObject result = new JSONObject();
		SQLiteDatabase db = rawDB.getReadableDatabase();

		String fieldcol = RapidSmsDBConstants.FormData.COLUMN_PREFIX + fieldToPlot.getName();
		StringBuilder rawQuery = new StringBuilder();
		rawQuery.append("select " + fieldcol);
		rawQuery.append(", count(*) from ");
		rawQuery.append(RapidSmsDBConstants.FormData.TABLE_PREFIX + mForm.getPrefix());

		rawQuery.append(" join rapidandroid_message on (");
		rawQuery.append(RapidSmsDBConstants.FormData.TABLE_PREFIX + mForm.getPrefix());
		rawQuery.append(".message_id = rapidandroid_message._id");
		rawQuery.append(") ");

		if (mStartDate.compareTo(Constants.NULLDATE) != 0 && mEndDate.compareTo(Constants.NULLDATE) != 0) {
			rawQuery.append(" WHERE rapidandroid_message.time > '" + Message.SQLDateFormatter.format(mStartDate)
					+ "' AND rapidandroid_message.time < '" + Message.SQLDateFormatter.format(mEndDate) + "' ");
		}

		rawQuery.append(" group by " + fieldcol);
		rawQuery.append(" order by " + fieldcol);

		// the string value is column 0
		// the magnitude is column 1

		Cursor cr = db.rawQuery(rawQuery.toString(), null);
		int barCount = cr.getCount();

		if (barCount != 0) {
			String[] xVals = new String[barCount];
			int[] yVals = new int[barCount];
			cr.moveToFirst();
			int i = 0;
			do {
				xVals[i] = cr.getString(0);
				yVals[i] = cr.getInt(1);
				i++;
			} while (cr.moveToNext());

			// xaxis: { ticks: [0, [Math.PI/2, "\u03c0/2"], [Math.PI, "\u03c0"],
			// [Math.PI * 3/2, "3\u03c0/2"], [Math.PI * 2, "2\u03c0"]]},

			try {
				// result.put("label", fieldToPlot.getName());
				// result.put("data", prepareData(xVals, yVals));
				// result.put("bars", getShowTrue());
				// result.put("xaxis", getXaxisOptions(xVals));
				return new JSONGraphData(prepareHistogramData(xVals, yVals), loadOptionsForHistogram(xVals));
			} catch (Exception ex) {

			} finally {
				if (!cr.isClosed()) {
					cr.close();
				}
				if (db.isOpen()) {
					db.close();
				}
			}
		}
		// either there was no data or something bad happened
		return new JSONGraphData(getEmptyData(), new JSONObject());
	}

	private JSONArray prepareHistogramData(String[] names, int[] counts) throws JSONException {
		// TODO Auto-generated method stub
		JSONArray arr = new JSONArray();
		int datalen = names.length;
		for (int i = 0; i < datalen; i++) {

			JSONObject elem = new JSONObject();
			// values will just be an array of length 1 with a single value
			JSONArray values = new JSONArray();
			JSONArray value = new JSONArray();
			value.put(i);
			value.put(counts[i]);
			values.put(value);
			elem.put("data", values);
			elem.put("bars", getShowTrue());
			elem.put("label", names[i]);
			arr.put(elem);
		}
		return arr;
	}

	@Override
	public String getGraphTitle() {
		return "Form Data";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapidandroid.activity.chart.ChartBroker#setVariable(int)
	 */

	@Override
	public synchronized void setVariable(int id) {
		// TODO Auto-generated method stub
		if (id == 0) {
			this.fieldToPlot = null;
		} else {
			this.fieldToPlot = mForm.getFields()[id - 1];
		}
		mChosenVariable = id;
		this.mGraphData = null;
		this.mGraphOptions = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapidandroid.activity.chart.ChartBroker#finishGraph()
	 */

	@Override
	public String getName() {
		return "graph_form";
	}
}
