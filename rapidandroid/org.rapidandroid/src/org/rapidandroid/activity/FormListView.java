package org.rapidandroid.activity;

import org.rapidandroid.R;
import org.rapidandroid.content.translation.ModelTranslator;
import org.rapidsms.java.core.model.Form;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FormListView extends Activity implements OnItemClickListener //, ItemLongClickListener 
{
	private FormListAdapter adapter;
//	private boolean fromLongClick;
	private Form[] forms;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		forms = ModelTranslator.getAllForms();
		
		adapter = new FormListAdapter(this);

		ListView lv = new ListView(this);
//		Color c = new Color();
//		lv.setBackgroundColor(c.parseColor("#f3f3f3"));
		lv.setAdapter(adapter);
//		lv.setOnItemLongClickListener(this);
		lv.setOnItemClickListener(this);

		this.setTitle(R.string.formlistview_title);

		setContentView(lv);
	}

	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// reload reports in case something changed
//		loadFiles();
	}



	public class FormListAdapter extends BaseAdapter
	{
		private Context context;
		private LayoutInflater inflater;

		public FormListAdapter(Context c)
		{
			context = c;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount()
		{
			if (forms.length == 0)
				return 1;
			else
				return forms.length;
		}

		public Object getItem(int position)
		{
			return forms[position];
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{

			if (position == 0 && forms.length == 0)
			{
				convertView = new TextView(context);
				TextView blank = (TextView) convertView;
				blank.setTextColor(Color.BLACK);
				blank.setText("No forms have been made.");
				blank.setTextSize(18);
				return blank;
			}

			String formName = forms[position].getFormName();

			if (formName != null)
			{
				if (convertView == null)
					convertView = inflater.inflate(R.layout.listview, null);

				TextView tv = (TextView) convertView.findViewById(R.id.listViewItemText);
				tv.setText(formName);

				return convertView;
			}
			else
			{
				TextView blank = new TextView(context);
				blank.setText("");
				return blank;
			}
		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long ids)
	{
		Intent i = new Intent();
		i.putExtra("selected_form", forms[position]);
		setResult(RESULT_OK,i);
		finish();
	}
}
