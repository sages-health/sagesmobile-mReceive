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

package org.rapidandroid.activity.chart;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rapidandroid.data.SmsDbHelper;
import org.rapidsms.java.core.Constants;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.webkit.WebView;

/**
 * Interface for simple chart display.
 * 
 * The implementers of this interface will need access to database methods to
 * prepare data and output to the graphing system. * This class is the exposed
 * Java object that the WebView will need to call, specifically the method
 * jsLoadGraph().
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 29, 2009
 * 
 */
public abstract class ChartBroker {

	private static final String CHART_FILE = "file:///android_asset/flot/html/basechart.html";
	private static final String JAVASCRIPT_PROPERTYNAME = "graphdata";
//	private static final String EMPTY_FILE = "file:///android_asset/flot/html/empty.html";

	/**
	 * Enumeration for display types (date) for level of bucketization
	 * 
	 * @author Cory Zue
	 * 
	 */
	public enum DateDisplayTypes {
		Hourly, Daily, Weekly, Monthly, Yearly
	}

	protected boolean isLoading;
	protected Date mStartDate = Constants.NULLDATE;
	protected Date mEndDate = Constants.NULLDATE;
	// protected Calendar mStartCal = Constants.NULLCALENDAR; //we may need to
	// switch to using calendar's due to the sheer number of conversions we're
	// doing with them back and forth.
	// protected Calendar mEndCal = Constants.NULLCALENDAR;

	protected SmsDbHelper rawDB;
	protected WebView mAppView;

//	private int traceCount = 0;

	protected String[] mVariableStrings;
	protected int mChosenVariable = 0;
	protected Activity mParentActivity;
	protected boolean isShowing = false;
	private String mGraphTitle = "";

	protected JSONArray mGraphData;
	protected JSONObject mGraphOptions;

	protected final Handler mTitleHandler = new Handler();
	protected final Runnable mUpdateActivityTitle = new Runnable() {
		public void run() {
			mParentActivity.setTitle(mGraphTitle);
		}
	};

	protected final Handler mDialogHandler = new Handler();
	protected final Runnable mStartThinker = new Runnable() {
		public void run() {
			mParentActivity.showDialog(160);
		}
	};
	protected final Runnable mStopThinker = new Runnable() {
		public void run() {
			mParentActivity.dismissDialog(160);
		}
	};

	protected final Runnable mEmptyData = new Runnable() {
		public void run() {
			mParentActivity.showDialog(170);
			isShowing = true;
		}
	};
	// private boolean mChartPageLoaded;
	// private boolean mAlreadyLoading;
	private final DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());

	protected ChartBroker(Activity activity, WebView appView, Date startDate, Date endDate) {
		mParentActivity = activity;
		mAppView = appView;
		rawDB = new SmsDbHelper(appView.getContext());
		// mVariableStrings = new String[] { "Trends by day",
		// "Receipt time of day" };
		mStartDate = startDate;
		mEndDate = endDate;
	}

	public String getGraphData() {
		if (mGraphData != null) {
			return mGraphData.toString();
		} else {
			return null;
		}
	}

	public synchronized void jsPrintDebug(String debugstring) {
		Log.d("ChartBroker", "JavaScript Debug Printout: " + debugstring);
	}
	
	public synchronized void setGraphData(String jsonarr) {

		try {
			this.mGraphData = new JSONArray(jsonarr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void setGraphOptions(String jsonobj) {

		try {
			this.mGraphOptions = new JSONObject(jsonobj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized String getGraphOptions() {
		if (mGraphOptions != null) {
			return mGraphOptions.toString();
		} else {
			return null;
		}
	}

	protected abstract void doLoadGraph();

	/**
	 * This is the primary method that the JavaScript in our HTML form will need
	 * access to in order to display graph data.
	 * 
	 * This SHOULD NOT be called from java, javascript should call it.
	 */
	public synchronized final void jsLoadGraph() {		
		// Debug.startMethodTracing("graphing_" + this.getClass().getName() +
		// traceCount++);
		// trying to get this to work, but it's quite annoying
		// if (!mAlreadyLoading) {
		mDialogHandler.post(mStartThinker);
		if (mGraphData == null && mGraphOptions == null) {
			doLoadGraph();
		}
		loadGraphFinish();
		// } else {
		// mAlreadyLoading = false;
		// }
	}

	protected void getPrettyTitleString() {
		StringBuilder tl = new StringBuilder();
		tl.append(mVariableStrings[mChosenVariable]).append(" :: ");
		DateFormat df = null;
		// yyyy-MM-dd HH:mm:ss
		DateDisplayTypes dtype = getDisplayType(mStartDate, mEndDate);
		switch (dtype) {
			case Hourly:
				df = new SimpleDateFormat("MM/dd HH:mm",Locale.getDefault());
				break;
			case Daily:
				df = new SimpleDateFormat("MM/dd",Locale.getDefault());
				break;
			case Weekly:
				df = new SimpleDateFormat("MM/dd",Locale.getDefault());
				break;
			case Monthly:
				df = new SimpleDateFormat("MM/yyyy",Locale.getDefault());
				break;
			case Yearly:
				df = new SimpleDateFormat("yyyy",Locale.getDefault());
				break;
		}
		tl.append(df.format(mStartDate)).append(" - ");
		tl.append(df.format(mEndDate));
		mGraphTitle = tl.toString();
		Log.d("ChartBroker", "end getPrettyTitleString");

	}

	protected void loadGraphFinish() {		
		Display display = this.mParentActivity.getWindowManager().getDefaultDisplay();
		//Get the screen orientation
			
		int width = display.getWidth();
		int height = display.getHeight();	
		
		Log.d("ChartBroker", "getWidth: " + width);
		Log.d("ChartBroker", "getHeight: " + height);
				
		height = height - 50;
		mAppView.loadUrl("javascript:SetGraph(\"" + width + "px\", \"" + height + "px\")");
		Log.d("ChartBroker", "javascript:SetGraph(\"" + width + "px\", \"" + height + "px\")");
		
		mAppView.loadUrl("javascript:GotGraph(" + mGraphData.toString() + "," + mGraphOptions.toString() + ")");
		Log.d("ChartBroker", "javascript:GotGraph(" + mGraphData.toString() + "," + mGraphOptions.toString() + ")");
	}

	private boolean hasData() {
		if (mGraphData == null || this.getEmptyData().toString().equals(mGraphData.toString())) {
			return false;
		}
		return true;
	}	

	/**
	 * Register this ChartBroker to the WebView as a javascript interface.
	 */
	public void bindChartToHTML() {
		mAppView.addJavascriptInterface(this, JAVASCRIPT_PROPERTYNAME);
		Log.d("ChartBroker", "addJavascriptInterface: " + JAVASCRIPT_PROPERTYNAME);
		mAppView.loadUrl(CHART_FILE);
		Log.d("ChartBroker", "loadUrl: " + CHART_FILE);
	}

	/**
	 * Gets the display type for this, based on the start and end dates
	 * 
	 * @return
	 */
	protected DateDisplayTypes getDisplayType(Date startDate, Date endDate) {
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);

		Calendar tempCal = Calendar.getInstance();
		tempCal.setTime(startDate);
		tempCal.add(Calendar.DATE, 3);
		if (endCal.before(tempCal)) {
			// within 3 days, we do it by hour. with day shading
			return DateDisplayTypes.Hourly;
		}

		tempCal.setTime(startDate);
		tempCal.add(Calendar.MONTH, 3);

		if (endCal.before(tempCal)) {
			// within 3 months, we break it down by day with week & month
			// shading?
			return DateDisplayTypes.Daily;
		}
		tempCal.setTime(startDate);
		tempCal.add(Calendar.YEAR, 2);
		if (endCal.before(tempCal)) {
			// within 2 years, we break it down by week with month shading
			return DateDisplayTypes.Weekly;
		}
		tempCal.setTime(startDate);
		tempCal.add(Calendar.YEAR, 4);

		if (endCal.before(tempCal)) {
			// 2-4 years break it down by month with year shading
			return DateDisplayTypes.Monthly;
		} else { // if(endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
					// >= 4) {
			// we need to break it down by year. with year shading
			return DateDisplayTypes.Yearly;
		}
	}

	protected String getSelectionString(DateDisplayTypes displayType) {
		switch (displayType) {
			case Hourly:
				return "  strftime('%Y-%m-%d %H',time) ";
			case Daily:
				return " strftime('%Y-%m-%d', time) ";
			case Weekly:
				return " strftime('%Y-%W', time) ";
			case Monthly:
				return " strftime('%Y-%m',time) ";
			case Yearly:
				return " strftime('%Y',time) ";
			default:
				return "";

		}
	}

	protected String getLegendString(DateDisplayTypes displayType) {
		switch (displayType) {
			case Hourly:
				return "Hourly count";
			case Daily:
				return "Daily count";
			case Weekly:
				return "Weekly count";
			case Monthly:
				return "Monthly count";
			case Yearly:
				return "Annual count";
			default:
				return "";
		}
	}

	private String getFormatString(DateDisplayTypes displayType) {
		switch (displayType) {
			case Hourly:
				return "%m/%d %H:%M";
			case Daily:
			case Weekly:
				return "%m/%d/%y";
			case Monthly:
				return "%m/%y";
			case Yearly:
				return "%y";
			default:
				return "%m/%d/%y";
		}
	}

	protected Date getNextValue(DateDisplayTypes displayType, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		switch (displayType) {
			case Hourly:
				cal.add(Calendar.HOUR, 1);
				break;
			case Daily:
				cal.add(Calendar.DATE, 1);
				break;
			case Weekly:
				cal.add(Calendar.WEEK_OF_YEAR, 1);
				break;
			case Monthly:
				cal.add(Calendar.MONTH, 1);
				break;
			case Yearly:
				cal.add(Calendar.YEAR, 1);
				break;
			default:
				throw new IllegalArgumentException("Bad display type: " + displayType);
		}
		return cal.getTime();
	}

	protected boolean isBefore(DateDisplayTypes displayType, Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		if (cal2.before(cal1)) {
			return false;
		}
		// i really feel like there should be a cleaner way to do this but it
		// escapes me
		if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) {
			return true;
		} else if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR) || displayType == DateDisplayTypes.Yearly) {
			return false;
		}
		// we know the years are the same and we're comparing less than years
		if (cal1.get(Calendar.MONTH) < cal2.get(Calendar.MONTH)) {
			return true;
		} else if (cal1.get(Calendar.MONTH) > cal2.get(Calendar.MONTH) || displayType == DateDisplayTypes.Monthly) {
			return false;
		}
		// we know months and years are the same and we're comparing less than
		// months
		if (cal1.get(Calendar.WEEK_OF_YEAR) < cal2.get(Calendar.WEEK_OF_YEAR)) {
			return true;
		} else if (cal1.get(Calendar.WEEK_OF_YEAR) > cal2.get(Calendar.WEEK_OF_YEAR)
				|| displayType == DateDisplayTypes.Weekly) {
			return false;
		}
		// we know months, years, and weeks are the same and we're comparing
		// less than weeks
		if (cal1.get(Calendar.DATE) < cal2.get(Calendar.DATE)) {
			return true;
		} else if (cal1.get(Calendar.DATE) > cal2.get(Calendar.DATE) || displayType == DateDisplayTypes.Daily) {
			return false;
		}
		// we know months, years,weeks, and days are the same and we're
		// comparing less than days
		if (cal1.get(Calendar.HOUR) < cal2.get(Calendar.HOUR)) {
			return true;
		}
		// anything else is not before
		return false;
	}

	protected Date getDate(DateDisplayTypes displayType, String string) {

		Date rawDate;
		try {
			rawDate = sqlDateFormat.parse(string);
		} catch (ParseException e) {
			Log.d("ChartBroker", "unparseable date: " + string);
			// this is actually a hard failure. Just not sure what to do
			return Constants.NULLDATE;
		}
		Calendar rawCal = Calendar.getInstance();
		rawCal.setTime(rawDate);
//		Calendar calToReturn = Calendar.getInstance();
		rawCal.set(Calendar.MINUTE, 0);
		rawCal.set(Calendar.SECOND, 0);
		switch (displayType) {
			case Hourly:
				break;
			case Daily:
				rawCal.set(Calendar.HOUR, 0);
				break;
			case Weekly:
				rawCal.set(Calendar.HOUR, 0);
				rawCal.set(Calendar.DAY_OF_WEEK, 1);
				break;
			case Monthly:
				rawCal.set(Calendar.HOUR, 0);
				rawCal.set(Calendar.DAY_OF_MONTH, 1);
				break;
			case Yearly:
				rawCal.set(Calendar.HOUR, 0);
				rawCal.set(Calendar.DAY_OF_MONTH, 1);
				rawCal.set(Calendar.MONTH, 1);
				break;
			default:
				return rawCal.getTime();
		}
//		Date toReturn = calToReturn.getTime();
		Date reallyToReturn = rawCal.getTime();
		return reallyToReturn;

	}

	protected JSONGraphData getDateQuery(DateDisplayTypes displayType, Cursor cr, SQLiteDatabase db) {
		// TODO Auto-generated method stub
		int barCount = cr.getCount();

		if (barCount == 0) {
			db.close();
			cr.close();
		} else {
			Date[] xVals = new Date[barCount];
			int[] yVals = new int[barCount];
			cr.moveToFirst();
			int i = 0;
			do {
				xVals[i] = getDate(displayType, cr.getString(0));
				yVals[i] = cr.getInt(1);
				i++;
			} while (cr.moveToNext());

			try {
				// result.put("label", fieldToPlot.getName());
				// result.put("data", prepareData(xVals, yVals));
				// result.put("bars", getShowTrue());
				// result.put("xaxis", getXaxisOptions(xVals));
				// todo
				String legend = this.getLegendString(displayType);
				return new JSONGraphData(prepareDateHistogramData(displayType, xVals, yVals, legend),
											loadOptionsForDateGraph(xVals, true, displayType));

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

	protected JSONObject loadOptionsForDateGraph(Date[] vals, boolean displayLegend, DateDisplayTypes displayType)
			throws JSONException {

		JSONObject toReturn = new JSONObject();
		// bars: { show: true }, points: { show: false }, xaxis: { mode: "time",
		// timeformat:"%y/%m/%d" }
		toReturn.put("bars", getShowFalse());
		toReturn.put("lines", getShowTrue());
		// if just a couple points, show them
		if (vals.length < 10) {
			toReturn.put("points", getShowTrue());
		} else {
			toReturn.put("points", getShowFalse());
		}

		toReturn.put("xaxis", getXaxisOptionsForDate(displayType));
		if (displayLegend) {
			toReturn.put("legend", getShowTrue());
		}
		toReturn.put("grid", getJSONObject("clickable", false));
		return toReturn;
	}

	protected static JSONObject getShowTrue() {
		JSONObject ret = new JSONObject();
		try {
			ret.put("show", true);
		} catch (Exception ex) {

		}
		return ret;
	}

	protected JSONObject getShowFalse() {
		JSONObject ret = new JSONObject();
		try {
			ret.put("show", false);
		} catch (Exception ex) {

		}
		return ret;
	}

	protected JSONObject getXaxisOptionsForDate(DateDisplayTypes displayType) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("mode", "time");
		toReturn.put("timeformat", getFormatString(displayType));
		return toReturn;
	}

	protected JSONObject loadOptionsForHistogram(String[] labels) throws JSONException {

		JSONObject toReturn = new JSONObject();
		toReturn.put("xaxis", this.getXaxisOptions(labels));
		toReturn.put("grid", getJSONObject("clickable", true));
		return toReturn;
	}

	protected JSONObject getXaxisOptions(String[] tickvalues) {
		JSONObject rootxaxis = new JSONObject();
		JSONArray arr = new JSONArray();
		int ticklen = tickvalues.length;

		for (int i = 0; i < ticklen; i++) {
			JSONArray elem = new JSONArray();
			elem.put(i);
			elem.put(tickvalues[i]);
			arr.put(elem);
		}

		try {
			rootxaxis.put("min", 0);
			rootxaxis.put("max", tickvalues.length + tickvalues.length / 5 + 1);
			rootxaxis.put("ticks", arr);
			rootxaxis.put("tickFormatter", "string");
		} catch (Exception ex) {

		}
		return rootxaxis;
	}

	protected JSONObject getJSONObject(String string, Object o) {
		JSONObject toReturn = new JSONObject();
		try {
			toReturn.put(string, o);
		} catch (Exception ex) {
		}
		return toReturn;
	}

	private JSONArray prepareDateHistogramData(DateDisplayTypes displayType, Date[] xvals, int[] yvals, String legend)
			throws JSONException {
		JSONArray outerArray = new JSONArray();
		JSONArray innerArray = getJSONArrayForValues(displayType, xvals, yvals);
		JSONObject finalObj = new JSONObject();
		finalObj.put("data", innerArray);
		finalObj.put("label", legend);
		outerArray.put(finalObj);
		return outerArray;
	}

	protected JSONArray getJSONArrayForValues(DateDisplayTypes displayType, Date[] xvals, int[] yvals) {
		JSONArray toReturn = new JSONArray();
		int datalen = xvals.length;
		Date prevVal = null;
		for (int i = 0; i < datalen; i++) {
			Date thisVal = xvals[i];
			if (prevVal != null) {
				// add logic to fill in zeros
				Date nextInSeries = getNextValue(displayType, prevVal);
				while (isBefore(displayType, nextInSeries, thisVal)) {
					JSONArray elem = new JSONArray();
					elem.put(nextInSeries.getTime());
					elem.put(0);
					toReturn.put(elem);
					nextInSeries = getNextValue(displayType, nextInSeries);
				}
			}
			JSONArray elem = new JSONArray();
			elem.put(xvals[i].getTime());
			elem.put(yvals[i]);
			toReturn.put(elem);
			prevVal = thisVal;
		}
		return toReturn;
	}

	protected JSONArray getEmptyData() {
		JSONArray toReturn = new JSONArray();
		JSONArray innerArray = new JSONArray();
		innerArray.put(0);
		innerArray.put(0);
		toReturn.put(innerArray);
		return toReturn;
	}

	/**
	 * This gets called by the javascript file after the graph is done plotting
	 */
	public void jsFinishGraph() {
		Log.d("ChartBroker", "begin finishGraph");
		getPrettyTitleString();
		mDialogHandler.post(mStopThinker);
		Log.d("ChartBroker", "stopped thinker");
		mTitleHandler.post(mUpdateActivityTitle);
		if (!hasData()) {
			mDialogHandler.post(mEmptyData);
		}
		Log.d("ChartBroker", "end finishGraph");
		this.isShowing = true;
		this.mAppView.setVisibility(0);
		
	}

	public abstract String getGraphTitle();

	public synchronized void setVariable(int id) {
		mChosenVariable = id;
		mGraphData = null;
		mGraphOptions = null;
	}

	public synchronized void setRange(Date startTime, Date endTime) {
		mStartDate = startTime;
		mEndDate = endTime;

		mGraphData = null;
		mGraphOptions = null;
	}

	public String[] getVariables() {
		return mVariableStrings;
	}

	public abstract String getName();
}
