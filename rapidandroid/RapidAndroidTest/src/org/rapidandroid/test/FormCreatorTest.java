package org.rapidandroid.test;

import org.rapidandroid.R;
import org.rapidandroid.activity.AddField;
import org.rapidandroid.activity.FormCreator;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Spinner;

public class FormCreatorTest extends
		ActivityInstrumentationTestCase2<FormCreator> {
	final String NAME = "T E S T";
	final String PREFIX = "F A C E";
	private static final int MENU_SAVE = Menu.FIRST;
	private static final int MENU_ADD_FIELD = 2;
	private static EditText mFormName;
	private static EditText mPrefix;
	private static EditText mDescription;
	private static Spinner mSpinnerParserType;
	AddField mAddFieldActivity;

	public FormCreatorTest() {
		super("org.rapidandroid.activity", FormCreator.class);
	}

	FormCreator mActivity;
	Instrumentation instrumentation;
	Intent mStartIntent;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
		mFormName = (EditText) mActivity
				.findViewById(org.rapidandroid.R.id.etx_formname);
		mPrefix = (EditText) mActivity
				.findViewById(org.rapidandroid.R.id.etx_formprefix);
		mDescription = (EditText) mActivity
				.findViewById(org.rapidandroid.R.id.etx_description);
		mSpinnerParserType = (Spinner) mActivity
				.findViewById(R.id.spinner_formparser);
		mStartIntent = new Intent(Intent.ACTION_MAIN);

	}

	/**
	 * 
	 * @param menuInstance
	 * @param instruments
	 * @param menuItemId
	 */
	public void testFieldsShouldStartEmpty() {
		final String empty = "";
		assertEquals(empty, mFormName.getText().toString());
		assertEquals(empty, mPrefix.getText().toString());
		assertEquals(empty, mDescription.getText().toString());
	}
@UiThreadTest	
public void testOnMenuOptions(){
	mFormName.setText("Living");
	mPrefix.setText("Live");
	mDescription.setText("This is a living test");
		
		try{
		
			ActivityMonitor am = getInstrumentation().addMonitor(AddField.class.getName(), null, false);

		// Click the menu option
		getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
		getInstrumentation().getContext();
		getInstrumentation().invokeMenuActionSync(mActivity, 3, 0);

		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
		}catch(Exception e){Log.e("testOnMenuOptions, Activity start:", e.getMessage().toString());}
		
	}

	public void testParseSpinnerdefaultValue(){
		final String defaultParseValue="Parse Mode:simpleregex";
		assertEquals(defaultParseValue,mSpinnerParserType.getSelectedItem().toString());
	}
	// code for Actionbar
	/**
	 * The name "preconditions' is a convention to signal that if this test
	 * doesn't pass, the test case was not set up properly and it might explain
	 * any and all failures in otehr tests this is not guaranteed to run before
	 * other tests, as junit uses reflection to find the tests
	 */
	
}
