package org.rapidandroid.test;

import java.util.ArrayList;

import org.rapidandroid.view.adapter.FieldViewAdapter;
import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.parser.IMessageParser;
import org.rapidsms.java.core.parser.SimpleRegexParser;
import org.rapidsms.java.core.parser.StrictRegexParser;

import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;

public class TestConstants {
	protected Menu testMenu;
	protected MenuItem testMenuItem;
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

	private static final int DIALOG_FORM_DUPLICATE_FORMNAME = 6;
	private static final int DIALOG_FORM_DUPLICATE_PREFIX = 7;
	
	private static final int DIALOG_FORM_INVALID_FORMNAME_WHITESPACE = 8;
	private static final int DIALOG_FORM_INVALID_PREFIX_WHITESPACE = 9;

	private static final int DIALOGRESULT_CLOSE_INFORMATIONAL = 0;
	private static final int DIALOGRESULT_OK_DONT_SAVE = 1;
	private static final int DIALOGRESULT_CANCEL_KEEP_WORKING = 2;

	private static final String STATE_FORMNAME = "formname";
	private static final String STATE_PREFIX = "prefix";
	private static final String STATE_DESC = "desc";
	private static final String STATE_PARSER = "parser";

	private ArrayList<Field> mCurrentFields;
	private String[] fieldStrings;

	private IMessageParser[] mAllParsers = { new SimpleRegexParser(),
			new StrictRegexParser() };
	private IMessageParser mChosenParser;
	private boolean mClosing = false;

	private Form selectedForm;

	private int selectedFieldPosition = -1;

	private FieldViewAdapter fieldViewAdapter;
	private Cursor cursor;
	
	//Hold values of inserted prefix and formname
	private String prefixCandidate=null;
	private String nameCandidate=null;
}
