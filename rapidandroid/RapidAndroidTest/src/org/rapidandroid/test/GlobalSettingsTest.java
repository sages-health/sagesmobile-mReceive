/**
 * 
 */
package org.rapidandroid.test;

import org.rapidandroid.activity.GlobalSettings;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * @author powelnv1
 * 
 */
public class GlobalSettingsTest extends
		ActivityInstrumentationTestCase2<GlobalSettings> {
	private CheckBox mActiveSwitch;
	private CheckBox mParseInProgressCheckbox;
	private EditText mParseInProgressReplyText;
	private CheckBox mParseCheckbox;
	private EditText mParseReplyText;
	private CheckBox mNoparseCheckBox;
	private EditText mNoparseReplyText;
	private Button mCacheRefreshButton;
	private GlobalSettings mActivity;

	/**
	 * @param name
	 */

	public GlobalSettingsTest() {
		super("org.rapidandroid.activity", GlobalSettings.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
		// Initialize EditText instances
		mParseInProgressReplyText = (EditText) mActivity
				.findViewById(org.rapidandroid.R.id.glb_etx_inprogress);
		mParseReplyText = (EditText) mActivity
				.findViewById(org.rapidandroid.R.id.glb_etx_success);
		mNoparseReplyText = (EditText) mActivity
				.findViewById(org.rapidandroid.R.id.glb_etx_failed);
		// Initialize checkbox instances
		mActiveSwitch = (CheckBox) mActivity
				.findViewById(org.rapidandroid.R.id.glb_chk_activeall);
		mParseInProgressCheckbox = (CheckBox) mActivity
				.findViewById(org.rapidandroid.R.id.glb_chk_inprogress);
		mParseCheckbox = (CheckBox) mActivity
				.findViewById(org.rapidandroid.R.id.glb_chk_parse);
		mNoparseCheckBox = (CheckBox) mActivity
				.findViewById(org.rapidandroid.R.id.glb_chk_noparse);
		// Initalize Button
		mCacheRefreshButton = (Button) mActivity
				.findViewById(org.rapidandroid.R.id.glbsettings_bttn_cache);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link org.rapidandroid.activity.GlobalSettings#onCreateOptionsMenu(android.view.Menu)}
	 * .
	 */


	/**
	 * Test method for
	 * {@link org.rapidandroid.activity.GlobalSettings#onOptionsItemSelected(android.view.MenuItem)}
	 * .
	 */
	
//Depends on Default values
//	public final void testEditTextFieldsDefaultValues() {
//
//		final String etxParseInProgress = "Message parsing in progress";
//		final String etxParseSuccess = "Message parsed successfully, thank you";
//		final String etxParseFail = "Try again";
//		assertEquals(etxParseInProgress, mParseInProgressReplyText.getText()
//				.toString());
//		assertEquals(etxParseSuccess, mParseReplyText.getText().toString());
//		assertEquals(etxParseFail, mNoparseReplyText.getText().toString());
//	}

	@UiThreadTest
	public final void testCheckBoxDefaultValues() {
		if (mActiveSwitch.isChecked())
			mActiveSwitch.setChecked(false);
		if (mParseInProgressCheckbox.isChecked())
			mParseInProgressCheckbox.setChecked(false);
		if (mParseCheckbox.isChecked())
			mParseCheckbox.setChecked(false);
		if (mNoparseCheckBox.isChecked())
			mNoparseCheckBox.setChecked(false);

		assertTrue("Active Switch is Checked!!", !mActiveSwitch.isChecked());
		assertTrue("Parse In Progress is Checked!!",
				!mParseInProgressCheckbox.isChecked());
		assertTrue("Successful Parse message is Checked!!",
				!mParseCheckbox.isChecked());
		assertTrue("No Success Parse message is Checked!!",
				!mNoparseCheckBox.isChecked());

	}

	public final void testFromTopToBottomToActualizerButton() {
		sendRepeatedKeys(7, KeyEvent.KEYCODE_DPAD_DOWN);
		assertTrue("Actualizer Button not focused",
				mCacheRefreshButton.isFocused());
	}

	public final void testAFieldsOnScreen() {
		assertTrue("Activate all SMS CheckBox not first view",
				mActiveSwitch.isFocused());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);

		assertTrue("Reply on Progress not second view",
				mParseInProgressCheckbox.isFocused());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);

		assertTrue("EditText message parsing not third view",
				mParseInProgressReplyText.isFocused());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);

		assertTrue("Reply on Successful Parses not fourth view",
				mParseCheckbox.isFocused());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);

		assertTrue("Reply on Successful Parses text not fifth view",
				mParseReplyText.isFocused());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);

		assertTrue("Reply on No Successful Parse not sixth view",
				mNoparseCheckBox.isFocused());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);

		assertTrue("Reply on No Successful Parse text not seventh view",
				mNoparseReplyText.isFocused());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);

		assertTrue("Refresh Cache Button not last view",
				mCacheRefreshButton.isFocused());
	}

	public final void testChangeCheckBox() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mActiveSwitch.setChecked(true);
				mParseInProgressCheckbox.setChecked(true);

			}
		});

	}

	@UiThreadTest
	public final void testChangeGlobalSettings() {
		final String etxParseInProgress = "Message parsing in progress";
		final String etxParseSuccess = "Message parsed successfully, thank you";
		final String etxParseFail = "Try again";

		mParseInProgressReplyText.setText("Parsing in progress!!");
		mParseReplyText.setText("Successful Parse");
		mNoparseReplyText.setText("Parsing Failed");

		mCacheRefreshButton.performClick();

		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			Log.e("GlobalSettingsTest: thread sleep error", e.getMessage()
					.toString());
		}
		assertTrue(
				"inProgressParsing has not been changed",
				mParseInProgressReplyText.getText().toString() != etxParseInProgress);
		assertTrue(
				"Successful parsing has not been changed",
				mParseReplyText.getText().toString() != etxParseInProgress);
		assertTrue(
				"Failed parsing has not been changed",
				mNoparseReplyText.getText().toString() != etxParseInProgress);
		
	}

	public final void testPreconditions() {
		assertNotNull(mActivity);
		assertNotNull(mActiveSwitch);
		assertNotNull(mParseInProgressCheckbox);
		assertNotNull(mParseInProgressReplyText);
		assertNotNull(mParseCheckbox);
		assertNotNull(mParseReplyText);
		assertNotNull(mNoparseCheckBox);
		assertNotNull(mNoparseReplyText);
		assertNotNull(mCacheRefreshButton);

	}

}
