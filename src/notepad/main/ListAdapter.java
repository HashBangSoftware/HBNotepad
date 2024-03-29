/*
 * ListAdapter.java
 * 
 * This is a custom adapter that will build a row that contains a CheckBox and a EditText. Their information will be populated
 * based on a the ArrayList of values found in the ListRow objects. 
 */

package notepad.main;

import java.util.ArrayList;

import model.ListRow;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter
{
	private ArrayList<ListRow> todoItems;
	private LayoutInflater inflater;
	private Context context;

	public ListAdapter(Context context, int textViewResourceId,
			ArrayList<ListRow> dataItems)
	{
		this.context = context;
		this.todoItems = dataItems;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ListRow row = this.todoItems.get(position);
		EditText rowDesc;
		CheckBox bCheck;
		View v = convertView;
		if (v == null)
		{
			v = inflater.inflate(R.layout.list_item, null);
			rowDesc = (EditText) v.findViewById(R.id.row_description);
			v.setFocusableInTouchMode(true);
			bCheck = (CheckBox) v.findViewById(R.id.checked);
			v.setTag(new RowViewHolder(rowDesc, bCheck));
			rowDesc.setText(row.getRowDescription());

			bCheck.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					CheckBox cb = (CheckBox) v;
					ListRow row = (ListRow) cb.getTag();
					row.setIsChecked((cb.isChecked()) ? 1 : 0);
				}
			});

			rowDesc.setOnFocusChangeListener(new View.OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus)
				{
					EditText et = (EditText) v;
					ListRow row = (ListRow) et.getTag();
					row.setRowDescription(et.getText().toString());
				}
			});

		} else
		{
			RowViewHolder viewHolder = (RowViewHolder) convertView.getTag();
			bCheck = viewHolder.getCheckBox();
			rowDesc = viewHolder.getEditText();

		}

		bCheck.setTag(row);
		rowDesc.setTag(row);

		bCheck.setChecked((row.getIsChecked() == 1) ? true : false);
		rowDesc.setText(row.getRowDescription().toString());

		return (v);
	}

	public int getCount()
	{
		return todoItems.size();
	}

	public Object getItem(int position)
	{
		return todoItems.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	private static class RowViewHolder
	{
		private CheckBox checkBox;
		private EditText editText;

		public RowViewHolder(EditText editText, CheckBox checkBox)
		{
			this.checkBox = checkBox;
			this.editText = editText;
		}

		public CheckBox getCheckBox()
		{
			return checkBox;
		}

		@SuppressWarnings("unused")
		public void setCheckBox(CheckBox checkBox)
		{
			this.checkBox = checkBox;
		}

		public EditText getEditText()
		{
			return editText;
		}

		@SuppressWarnings("unused")
		public void setEditText(TextView textView)
		{
			this.editText = editText;
		}

	}
}
