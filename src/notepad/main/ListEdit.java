/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package notepad.main;

import java.util.ArrayList;
import java.util.Date;

import model.ListRow;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ListEdit extends ListActivity
{
	private static final int ADD_ROW_ID = Menu.FIRST;
	private static final int SAVE_ID = Menu.FIRST + 1;
	private static final int REMOVE_ROW_ID = Menu.FIRST + 2;
	private static final int SEND_SMS = Menu.FIRST + 3;

	private final String SAVED_DATA = "list_data";
	private final String DEFAULT_TITLE = "Untitled";

	private Long mListId;
	private ArrayList<Long> mRowRemovalIds;
	private String listTitle;
	private String mCreateDate;
	private NotesDbAdapter mDbHelper;
	private ArrayList<ListRow> toDoData;
	private ListAdapter customToDoAdapter;
	private ListActivity contextView;

	private Button addButton;
	private Button removeButton;
	private TextView titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Hide Title Bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Setup the database adapter
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		// Get the layout content
		setContentView(R.layout.list_edit);

		// Get layout components
		addButton = (Button) findViewById(R.id.add_button);
		removeButton = (Button) findViewById(R.id.remove_button);
		titleView = (TextView) findViewById(R.id.list_title);

		// Add button listners
		addButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				addRow();
			}
		});

		removeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				removeRow();
			}
		});

		// Provide context reference
		contextView = this;
		setTitle(DEFAULT_TITLE);
		toDoData = new ArrayList<ListRow>();
		mRowRemovalIds = new ArrayList<Long>();
		customToDoAdapter = new ListAdapter(this, R.layout.list_item, toDoData);
		registerForContextMenu(getListView());

		setListAdapter(customToDoAdapter);

		mListId = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(NotesDbAdapter.KEY_ROWID);
		if (mListId == null)
		{
			final Bundle extras = getIntent().getExtras();
			mListId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
			final Date currentDate = new Date();
			mCreateDate = currentDate.toString().substring(0, 16);
		} else
		{
		}

		populateList(savedInstanceState);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		saveState();
		outState.putString("title", listTitle);
		outState.putParcelableArrayList(SAVED_DATA, toDoData);
		outState.putSerializable(NotesDbAdapter.KEY_ROWID, mListId);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	private void saveState()
	{
		this.getListView().requestFocus(); // Must steal focus in order to kick
											// off the OnFocusChange event in
											// the ListAdapter
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, ADD_ROW_ID, 0, R.string.add_row).setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(0, REMOVE_ROW_ID, 0, R.string.remove_row).setIcon(
				android.R.drawable.ic_menu_delete);
		menu.add(1, SAVE_ID, 0, R.string.save_list).setIcon(
				android.R.drawable.ic_menu_save);
		menu.add(1, SEND_SMS, 0, R.string.send_sms).setIcon(
				android.R.drawable.ic_menu_send);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
		case ADD_ROW_ID:
			addRow();
			return true;
		case SAVE_ID:
			saveList();
			setResult(RESULT_OK);
			finish();
			return true;
		case REMOVE_ROW_ID:
			removeRow();
			return true;
		case SEND_SMS:
			sendToSMS();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void sendToSMS()
	{
		String messageBody = "";
		messageBody += contextView.getTitle().toString() + "\n";
		for (int i = 0; i < toDoData.size(); i++)
		{
			final ListRow tempRow = toDoData.get(i);
			if (tempRow.getIsChecked() == 1)
			{
				messageBody += "\u2713" + " " + tempRow.getRowDescription()
						+ "\n";
			} else
			{
				messageBody += " X " + tempRow.getRowDescription() + "\n";
			}
		}

		final Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body", messageBody);
		sendIntent.setType("vnd.android-dir/mms-sms");
		startActivity(sendIntent);
	}

	private void addRow()
	{
		toDoData.add(new ListRow(0, 0, ""));
		customToDoAdapter.notifyDataSetChanged();
	}

	private void removeRow()
	{
		if (toDoData.size() > 0)
		{
			if (mListId == null) // List has not been saved yet (no data
									// persisted)
			{
				toDoData.remove(toDoData.size() - 1);
				customToDoAdapter.notifyDataSetChanged();
			} else
			// List exists in
			{
				final ListRow removeRow = toDoData.get(toDoData.size() - 1);
				if (removeRow.getId() != -1)
				{
					mRowRemovalIds.add(removeRow.getId());
				}
				toDoData.remove(toDoData.size() - 1);
				customToDoAdapter.notifyDataSetChanged();
			}
		}
	}

	private void saveList()
	{
		this.getListView().requestFocus(); // Must steal focus in order to kick
											// off the OnFocusChange event in
											// the ListAdapter
		final String title = titleView.getText().toString();
		if (mListId == null)
		{
			final long id = mDbHelper.createList(title, mCreateDate);
			if (id > 0)
			{
				mListId = id;
				for (int i = 0; i < toDoData.size(); i++)
				{
					final ListRow insertRow = toDoData.get(i);
					final long rowId = mDbHelper.createListRowData(mListId,
							insertRow.getRowDescription(),
							insertRow.getIsChecked());
					insertRow.setId(rowId);
					toDoData.set(i, insertRow);
				}
			}
		} else
		{
			for (int i = 0; i < toDoData.size(); i++)
			{
				final ListRow insertRow = toDoData.get(i);
				if (insertRow.getId() == -1) // -1 = ID not set
				{
					final long rowId = mDbHelper.createListRowData(mListId,
							insertRow.getRowDescription(),
							insertRow.getIsChecked());
					insertRow.setId(rowId);
					toDoData.set(i, insertRow);
				} else
				{
					mDbHelper.updateListData(insertRow.getId(),
							insertRow.getRowDescription(),
							insertRow.getIsChecked());
				}
			}
			// Row removal database clean up clean up
			for (int i = 0; i < mRowRemovalIds.size(); i++)
			{
				mDbHelper.deleteListRow(mRowRemovalIds.get(i));
			}
		}

		Toast.makeText(contextView, "List Saved", Toast.LENGTH_SHORT).show();

	}

	private void populateList(Bundle savedInstanceState)
	{
		if (mListId != null)
		{
			if (savedInstanceState == null)
			{
				final Cursor lists = mDbHelper.fetchList(mListId);
				startManagingCursor(lists);
				listTitle = lists.getString(lists
						.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
				titleView.setText(listTitle);
				final Cursor listDataRaw = mDbHelper.fetchListData(mListId);

				if (listDataRaw.getCount() > 0)
				{
					do
					{
						final ListRow row = new ListRow(
								listDataRaw.getLong(listDataRaw
										.getColumnIndex("_id")),
								listDataRaw.getLong(listDataRaw
										.getColumnIndex("list_id")),
								listDataRaw.getString(listDataRaw
										.getColumnIndex("item_data")),
								listDataRaw.getInt(listDataRaw
										.getColumnIndex("checked")));
						toDoData.add(row);
					} while (listDataRaw.moveToNext());
					customToDoAdapter.notifyDataSetChanged();
				} else
				{
				}
			} else
			{
				reloadSavedState(savedInstanceState);
			}
		} else
		{
			if (savedInstanceState == null)
			{
				final AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Please enter a title");

				// Set an EditText view to get user input
				final EditText titleInput = new EditText(this);
				titleInput.setText(DEFAULT_TITLE);
				titleInput.setSelectAllOnFocus(true);
				alert.setView(titleInput);

				alert.setPositiveButton("OK",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,
									int whichButton)
							{
								final String value = titleInput.getText()
										.toString();
								if (!value.equals(null))
								{
									listTitle = value;
								} else
								{
									listTitle = DEFAULT_TITLE;
								}
								titleView.setText(listTitle);
							}
						});
				alert.show();
			} else
			{
				reloadSavedState(savedInstanceState);
			}
		}
	}

	private void reloadSavedState(Bundle savedState)
	{
		listTitle = savedState.getString("title");
		titleView.setText(listTitle);
		final ArrayList<Parcelable> savedRows = savedState
				.getParcelableArrayList(SAVED_DATA);
		for (int i = 0; i < savedRows.size(); i++)
		{
			toDoData.add((ListRow) savedRows.get(i));
		}
		customToDoAdapter.notifyDataSetChanged();
	}
}
