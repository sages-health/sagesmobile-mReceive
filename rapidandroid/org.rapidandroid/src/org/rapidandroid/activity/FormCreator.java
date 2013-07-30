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

/**
 * 
 */
package org.rapidandroid.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.rapidandroid.R;
import org.rapidandroid.activity.AddField.ResultConstants;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidandroid.view.adapter.FieldViewAdapter;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.model.SimpleFieldType;
import org.rapidsms.java.core.parser.IMessageParser;
import org.rapidsms.java.core.parser.SimpleRegexParser;
import org.rapidsms.java.core.parser.StrictRegexParser;
import org.rapidsms.java.core.parser.service.ParsingService.ParserType;
import org.rapidsms.java.core.parser.token.ITokenParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Activity window for creating a new form.
 * 
 * Goal is to create all this form in memory for the user to interact with.
 * 
 * Creation of org.rapidsms.core.model.Form object for insertion into database
 * is the goal.
 * 
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 12, 2009
 * 
 */

public class FormCreator extends Activity {
	private static final int MENU_SAVE = Menu.FIRST;
	private static final int MENU_EDIT_FORMS = Menu.FIRST + 1;
	private static final int MENU_ADD_FIELD = Menu.FIRST + 2;
	private static final int MENU_CANCEL = Menu.FIRST + 3;

	public static final int ACTIVITY_ADDFIELD_CANCEL = 0;
	public static final int ACTIVITY_ADDFIELD_ADDED = 1;
	public static final int PREVIOUS_FORM_SELECTED = 2;

	private static final int CONTEXT_MOVE_UP = Menu.FIRST;
	private static final int CONTEXT_MOVE_DOWN = Menu.FIRST + 1;
	private static final int CONTEXT_REMOVE = Menu.FIRST + 2;
	// private static final int CONTEXT_EDIT = ContextMenu.FIRST + 3;

	private static final int DIALOG_FORM_SAVEABLE = -1;
	private static final int DIALOG_FORM_INVALID_NOFORMNAME = 0;
	private static final int DIALOG_FORM_INVALID_NOPREFIX = 1;
	private static final int DIALOG_FORM_INVALID_NOTUNIQUE = 2;
	private static final int DIALOG_FORM_INVALID_NOFIELDS = 3;
	private static final int DIALOG_CONFIRM_CLOSURE = 4;
	private static final int DIALOG_FORM_CREATE_FAIL = 5;

//	private static final int DIALOGRESULT_CLOSE_INFORMATIONAL = 0;
//	private static final int DIALOGRESULT_OK_DONT_SAVE = 1;
//	private static final int DIALOGRESULT_CANCEL_KEEP_WORKING = 2;

	private static final String STATE_FORMNAME = "formname";
	private static final String STATE_PREFIX = "prefix";
	private static final String STATE_DESC = "desc";
	private static final String STATE_PARSER = "parser";

	private ArrayList<Field> mCurrentFields;
//	private String[] fieldStrings;

	private IMessageParser[] mAllParsers = { new SimpleRegexParser(),
			new StrictRegexParser() };
	private IMessageParser mChosenParser;
	private boolean mClosing = false;

	private Form selectedForm;

	private int selectedFieldPosition = -1;

	private FieldViewAdapter fieldViewAdapter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCurrentFields = new ArrayList<Field>();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.form_create);

		// Required onCreate setup
		// add some events to the listview
		ListView lsv = (ListView) findViewById(R.id.lsv_createfields);

		fieldViewAdapter = new FieldViewAdapter(this, mCurrentFields);
		lsv.setAdapter(fieldViewAdapter);

		lsv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selectedFieldPosition = arg2;
				return false;
			}

		});

		TextView noFields = new TextView(this);
		noFields.setText("No fields");
		lsv.setEmptyView(noFields);
		registerForContextMenu(lsv);

		initParserSpinner();
		// Set the event listeners for the spinner and the listview
		Spinner spin_forms = (Spinner) findViewById(R.id.spinner_formparser);
		spin_forms
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View theview, int position, long rowid) {
						spinnerItemSelected(position);
					}

					public void onNothingSelected(AdapterView<?> parent) {
						// blow away the listview's items
						mChosenParser = null;
						// resetCursor = true;
						// loadListViewWithFormData();
					}
				});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		menu.setHeaderTitle("Current Field");
		menu.add(0, CONTEXT_MOVE_UP, 0, "Move Up");
		menu.add(0, CONTEXT_MOVE_DOWN, 0, "Move Down");
		// menu.add(0, CONTEXT_EDIT, 0, "Edit");
		menu.add(0, CONTEXT_REMOVE, 0, "Remove").setIcon(
				android.R.drawable.ic_menu_delete);

	}

	@Override
	protected synchronized void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (!mClosing) {
			saveState();
		} else {
			File f = this.getFileStreamPath("FormCreatorSavedState");
			if (f.exists()) {
				f.delete();
			}
		}
	}



	private void saveState() {
		// TODO Auto-generated method stub
		// this.d
		JSONObject savedstate = new JSONObject();
		EditText etxFormName = (EditText) findViewById(R.id.etx_formname);
		EditText etxFormPrefix = (EditText) findViewById(R.id.etx_formprefix);
		EditText etxDescription = (EditText) findViewById(R.id.etx_description);
//		ListView lsv = (ListView) findViewById(R.id.lsv_createfields);

		try {
			savedstate.put(STATE_FORMNAME, etxFormName.getText().toString());
			savedstate.put(STATE_PREFIX, etxFormPrefix.getText().toString());
			savedstate.put(STATE_DESC, etxDescription.getText().toString());
			savedstate.put(STATE_PARSER, mChosenParser.getName());

			if (mCurrentFields != null) {
				int numFields = this.mCurrentFields.size();
				for (int i = 0; i < numFields; i++) {
					Field f = mCurrentFields.get(i);

					JSONObject fieldobj = new JSONObject();

					fieldobj.put(AddField.ResultConstants.RESULT_KEY_FIELDNAME,	f.getName());
					fieldobj.put(AddField.ResultConstants.RESULT_KEY_DESCRIPTION, f.getDescription());
					fieldobj.put(AddField.ResultConstants.RESULT_KEY_FIELDTYPE_ID, ((SimpleFieldType) f.getFieldType()).getId());
					savedstate.put("Field" + i, fieldobj);
				}
			}

			getIntent().putExtra("current_form", savedstate.toString());

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.d("FormCreator", e1.getMessage());
		}
	}

	@Override
	protected synchronized void onResume() {
		super.onResume();

		Intent intent = getIntent();
		String currentForm = intent.getStringExtra("current_form");
		if (currentForm != null) {
			try {
				String contents = new String(currentForm);
				JSONObject readobject = new JSONObject(contents);

				EditText etxFormName = (EditText) findViewById(R.id.etx_formname);
				EditText etxFormPrefix = (EditText) findViewById(R.id.etx_formprefix);
				EditText etxDescription = (EditText) findViewById(R.id.etx_description);

				etxFormName.setText(readobject.getString(STATE_FORMNAME));
				etxFormPrefix.setText(readobject.getString(STATE_PREFIX));
				etxDescription.setText(readobject.getString(STATE_DESC));
				boolean checkForFields = true;
				int i = 0;
				do {
					checkForFields = readobject.has("Field" + i);
					if (checkForFields) {
						JSONObject fieldBundle = (JSONObject) readobject.get("Field" + i);
						restoreFieldFromState(fieldBundle);
						i++;
					}
				} while (checkForFields);
			} catch (Exception e) {
				Log.e("FOOBAR", e.getMessage(), e);
			}
		}
	}


	/**
	 * Gets called from onActivityResult
	 * 
	 * @param f
	 */
	private void restoreForm(Form f) {
		EditText etxFormName = (EditText) findViewById(R.id.etx_formname);
		etxFormName.setText(f.getFormName());

		EditText etxFormPrefix = (EditText) findViewById(R.id.etx_formprefix);
		etxFormPrefix.setText(f.getPrefix());

		EditText etxDescription = (EditText) findViewById(R.id.etx_description);
		etxDescription.setText(f.getDescription());

		Spinner spin_forms = (Spinner) findViewById(R.id.spinner_formparser);
		int cnt = spin_forms.getCount();
		for(int i=0;i<cnt;i++)
		{
			if(spin_forms.getItemAtPosition(i).toString().toLowerCase(Locale.getDefault()).contains(f.getParserType().name().toLowerCase(Locale.getDefault())))
				spin_forms.setSelection(i);
		}
		
		
		// add fields
		Field[] fields = f.getFields();
		Arrays.sort(fields, new Comparator<Field>() {
			public int compare(Field f1, Field f2) {
				if (f1.getSequenceId() < f2.getSequenceId())
					return -1;
				else if (f1.getSequenceId() > f2.getSequenceId())
					return 1;
				return 0;
			}
		});

		for (Field fi : fields) {
			Bundle b = new Bundle();
			b.putString(ResultConstants.RESULT_KEY_FIELDNAME, fi.getName());
			b.putString(ResultConstants.RESULT_KEY_DESCRIPTION, fi.getDescription());
			b.putInt(ResultConstants.RESULT_KEY_FIELDTYPE_ID, ((SimpleFieldType)fi.getFieldType()).getId());

			addNewFieldFromActivity(b);
		}

		// save state so that the "current_form" is in the intent
		// and will therefore be loaded in the onResume call
		saveState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		Bundle extras = null;
		if (intent != null) {
			extras = intent.getExtras();
		}

		switch (requestCode) {
		case ACTIVITY_ADDFIELD_ADDED:
			if (extras != null) {
				addNewFieldFromActivity(extras);
			}
			break;
		case ACTIVITY_ADDFIELD_CANCEL:
			// do nothing
			break;
		case PREVIOUS_FORM_SELECTED:
			if (extras != null) {
				selectedForm = (Form) extras.getSerializable("selected_form");
				if (selectedForm != null) {
					restoreForm(selectedForm);
				}
			}
			break;
		}
	}

	private void addNewFieldFromActivity(Bundle extras) {
		Field newField = new Field();
		newField.setFieldId(-1);
		newField.setName(extras.getString(ResultConstants.RESULT_KEY_FIELDNAME)
				.trim());

		newField.setDescription(extras
				.getString(ResultConstants.RESULT_KEY_DESCRIPTION));
		int fieldTypeID = extras.getInt(ResultConstants.RESULT_KEY_FIELDTYPE_ID);
		
		ITokenParser fieldtype = ModelTranslator.getFieldType(fieldTypeID);
		newField.setFieldType(fieldtype);

		int seqId = mCurrentFields.size();
		if (seqId > 0) {
			seqId = seqId - 1;
		}
		newField.setSequenceId(seqId);
		mCurrentFields.add(newField);

		fieldViewAdapter.notifyDataSetChanged();
	}

	private void restoreFieldFromState(JSONObject fieldjson) {
		if (mCurrentFields == null) {
			mCurrentFields = new ArrayList<Field>();
		}
		try {
			Field newField = new Field();
			newField.setFieldId(-1);
			newField.setName(fieldjson.getString(ResultConstants.RESULT_KEY_FIELDNAME));
			newField.setDescription(fieldjson.getString(ResultConstants.RESULT_KEY_DESCRIPTION));
			int fieldTypeID = fieldjson.getInt(ResultConstants.RESULT_KEY_FIELDTYPE_ID);
			ITokenParser fieldtype = ModelTranslator.getFieldType(fieldTypeID);
			newField.setFieldType(fieldtype);

			int listSize = mCurrentFields.size();

			for (int i = 0; i < listSize; i++) {
				Field existingField = mCurrentFields.get(i);
				if (existingField.getName().equals(newField.getName())) {
					return;
				}
			}
			if (listSize > 0) {
				listSize = listSize - 1;
			}
			newField.setSequenceId(listSize);

			mCurrentFields.add(newField);

			fieldViewAdapter.notifyDataSetChanged();
			// updateFieldList();
		} catch (Exception ex) {
			Log.d("FormCreator", ex.getMessage());
		}
	}

	/**
	 * 
	 */
	// private void updateFieldList() {
	// ListView lsv = (ListView) findViewById(R.id.lsv_createfields);
	// if (mCurrentFields == null) {
	// mCurrentFields = new Vector<Field>();
	// }
	//
	// Field[] fieldArray = this.mCurrentFields
	// .toArray(new Field[mCurrentFields.size()]);
	// lsv.setAdapter(new FieldViewAdapter(this, fieldArray));
	// }

	// This is a call to the DB to get all the forms that this form can support.
	private void initParserSpinner() {
		// The steps:
		// get the spinner control from the layouts
		Spinner spin_forms = (Spinner) findViewById(R.id.spinner_formparser);
		// Get an array of forms from the DB
		// in the current iteration, it's mForms
		// this.mAllParsers = ModelTranslator.getAllForms();

		String[] monitors = new String[mAllParsers.length];

		for (int i = 0; i < mAllParsers.length; i++) {
			monitors[i] = "Parse Mode: " + mAllParsers[i].getName();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, monitors);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// apply it to the spinner
		spin_forms.setAdapter(adapter);
	}

	private void spinnerItemSelected(int position) {
		mChosenParser = mAllParsers[position];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_SAVE, 0, R.string.formeditor_menu_save).setIcon(
				android.R.drawable.ic_menu_save);
		menu.add(0, MENU_EDIT_FORMS, 0, R.string.formeditor_menu_edit_forms)
				.setIcon(android.R.drawable.ic_menu_view);
		menu.add(0, MENU_ADD_FIELD, 0, R.string.formeditor_menu_add_field)
				.setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_CANCEL, 0, R.string.formeditor_menu_cancel).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case MENU_SAVE:
			int formStatus = checkFormIsSaveable();
			if (formStatus == FormCreator.DIALOG_FORM_SAVEABLE) {
				doSave();
				setResult(0);
				File f = this.getFileStreamPath("FormCreatorSavedState");
				if (f.exists()) {
					f.delete();
				}
				mClosing = true;
				finish();
			} else {
				this.showDialog(formStatus);
			}
			return true;
		case MENU_EDIT_FORMS:
			Intent in = new Intent(this, FormListView.class);
			startActivityForResult(in, PREVIOUS_FORM_SELECTED);
			return true;
		case MENU_ADD_FIELD:
			Intent intent = new Intent(this, AddField.class);
			// mUpdatedFromActivity = true;
			if (mCurrentFields != null) {
				int len = this.mCurrentFields.size();
				for (int i = 0; i < len; i++) {
					intent.putExtra(mCurrentFields.get(i).getName(), 0);
				}
			}
			startActivityForResult(intent, ACTIVITY_ADDFIELD_ADDED);
			return true;
		case MENU_CANCEL:
			mClosing = true;
			finish();
			return true;
		}

		return true;
	}

	private int checkFormIsSaveable() {
		EditText etxFormName = (EditText) findViewById(R.id.etx_formname);
		EditText etxFormPrefix = (EditText) findViewById(R.id.etx_formprefix);
		// EditText etxDescription =
		// (EditText)findViewById(R.id.etx_description);
//		ListView lsvFields = (ListView) findViewById(R.id.lsv_createfields);

		if (etxFormName.getText().length() == 0) {
			return FormCreator.DIALOG_FORM_INVALID_NOFORMNAME;
		}
		if (etxFormPrefix.getText().length() == 0) {
			return FormCreator.DIALOG_FORM_INVALID_NOPREFIX;
		}

		if (this.mCurrentFields.size() == 0) {
			return FormCreator.DIALOG_FORM_INVALID_NOFIELDS;
		}

		// String prefixCandidate = etxFormPrefix.getText().toString();
		// String nameCandidate = etxFormName.getText().toString();
		//
		// if (ModelTranslator.doesFormExist(prefixCandidate, nameCandidate)) {
		// return FormCreator.DIALOG_FORM_INVALID_NOTUNIQUE;
		// } else {
		// return FormCreator.DIALOG_FORM_SAVEABLE;
		// }
		return FormCreator.DIALOG_FORM_SAVEABLE;
	}

	/**
	 * @param prefixCandidate
	 * @param nameCandidate
	 * @return
	 */

	private void doSave() {
		EditText etxFormName = (EditText) findViewById(R.id.etx_formname);
		EditText etxFormPrefix = (EditText) findViewById(R.id.etx_formprefix);
		EditText etxDescription = (EditText) findViewById(R.id.etx_description);
		Spinner spinnerParserType = (Spinner) findViewById(R.id.spinner_formparser);
		int parserPosition = spinnerParserType.getSelectedItemPosition();

		ParserType parserType = ParserType.valueOf(mAllParsers[parserPosition].getName().toUpperCase(Locale.getDefault()));

		Form formToSave = new Form();
		formToSave.setFormName(etxFormName.getText().toString().trim());
		formToSave.setPrefix(etxFormPrefix.getText().toString().trim());
		formToSave.setDescription(etxDescription.getText().toString().trim());
		formToSave.setParserType(parserType);
		// (Message[])parsedMessages.keySet().toArray(new
		// Message[parsedMessages.keySet().size()]);
		Field[] fieldArray = this.mCurrentFields.toArray(new Field[mCurrentFields.size()]);
		formToSave.setFields(fieldArray);

		try {
			if (selectedForm != null) {
				formToSave.setFormId(selectedForm.getFormId());
				ModelTranslator.editFormToDatabase(formToSave);
			} else {
				ModelTranslator.addFormToDatabase(formToSave);
			}
		} catch (Exception ex) {
			Log.e("FOOBAR", ex.getMessage(), ex);
			showDialog(DIALOG_FORM_CREATE_FAIL);
		}
	}

	@Override
	// http://www.anddev.org/tinytutcontextmenu_for_listview-t4019.html
	// UGH, things changed from .9 to 1.0
	public boolean onContextItemSelected(MenuItem item) {

		// some sanity checks:
		ListView lsvFields = (ListView) findViewById(R.id.lsv_createfields);
		if (lsvFields.getCount() == 0 || this.mCurrentFields == null || this.mCurrentFields.size() == 0) {
			return true;
		}

		if (selectedFieldPosition == -1) {
			return true;
		}

//		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		// TODO: IMPLEMENT CONTEXT MENU
		case CONTEXT_MOVE_UP:

			moveFieldUp(selectedFieldPosition);

			break;
		case CONTEXT_MOVE_DOWN:

			moveFieldDown(selectedFieldPosition);

			break;
		case CONTEXT_REMOVE:
			removeField(selectedFieldPosition);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	/**
	 * @param position
	 */
	private void removeField(int position) {
		mCurrentFields.remove(position);
		resetFieldSequences();
	}

	/**
	 * 
	 */
	private void moveFieldDown(int position) {
		if (position < mCurrentFields.size() - 1) {
			Field fieldToMove = mCurrentFields.get(position);
			mCurrentFields.remove(position);
			int newposition = position + 1;
			if (newposition >= mCurrentFields.size()) {
				mCurrentFields.add(fieldToMove);
			} else {
				mCurrentFields.add(newposition, fieldToMove);
			}
			resetFieldSequences();
		}
	}

	/**
	 * @param position
	 */
	private void moveFieldUp(int position) {
		// TODO Auto-generated method stub
		if (position > 0) {
			Field fieldToMove = mCurrentFields.get(position);
			mCurrentFields.remove(position);
			int newposition = position - 1;
			if (newposition <= 0) {
				mCurrentFields.add(0, fieldToMove);
			} else {
				mCurrentFields.add(newposition, fieldToMove);
			}
			resetFieldSequences();
		}
	}

	private void resetFieldSequences() {
		int len = mCurrentFields.size();
		for (int i = 0; i < len; i++) {
			Field f = mCurrentFields.get(i);
			f.setSequenceId(i);
		}
		fieldViewAdapter.notifyDataSetChanged();// updateFieldList();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		String title = "";
		String message = "";

		switch (id) {
		case DIALOG_FORM_INVALID_NOFIELDS:
			title = "Invalid form";
			message = "You must have at least one field for this form to save";
			break;
		case DIALOG_FORM_INVALID_NOFORMNAME:
			title = "Invalid form";
			message = "You must enter a formname";
			break;
		case DIALOG_FORM_INVALID_NOPREFIX:
			title = "Invalid form";
			message = "You must enter a prefix";
			break;
		case DIALOG_FORM_INVALID_NOTUNIQUE:
			title = "Invalid form";
			message = "The form of this name and prefix already exists";
			break;
		case DIALOG_FORM_CREATE_FAIL:
			title = "Form creation failed";
			message = "Unable to create the form and its support tables.  Check the logs.";
			return new AlertDialog.Builder(FormCreator.this).setTitle(title)
					.setMessage(message).setPositiveButton("Ok", null).create();
		case DIALOG_CONFIRM_CLOSURE:
			// for confirm closure, we actually just return the dialog as we
			// want it here.
			title = "Confirm Closure";
			message = "Are you sure you want to close without saving changes?";
			return new AlertDialog.Builder(FormCreator.this)
					.setTitle(title)
					.setMessage(message)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
							}).setNegativeButton("No, keep working", null)
					.create();
		default:
			return null;

		}
		return new AlertDialog.Builder(FormCreator.this).setTitle(title)
				.setMessage(message).setPositiveButton("OK", null).create();

	}

	@Override
	public void onBackPressed() {
		AlertDialog dialog = new AlertDialog.Builder(this).create();

		dialog.setMessage("Save Changes First?");
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int which) {
						doSave();
						finish();
					}
				});

		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int which) {
						finish();
					}
				});

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int which) {
					}
				});
		dialog.show();
	}

}
