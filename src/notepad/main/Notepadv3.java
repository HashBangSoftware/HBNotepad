package notepad.main;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;

public class Notepadv3 extends TabActivity
{
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	private static final int ACTIVITY_CREATE_LIST = 2;
	private static final int ACTIVITY_EDIT_LIST = 3;
	private static final int ABOUT_ID = 4;

	private static final int INSERT_NOTE_ID = 5;
	private static final int DELETE_ID = 6;
	private static final int INSERT_LIST_ID = 7;
	private static final int EDIT_TITLE_ID = 8;
	private static final String DEFAULT_TITLE = "Untitled";

	private NotesDbAdapter mDbHelper;
	private ListView notesView;
	private ListView listsView;
	private Context homeContext;

	private SimpleCursorAdapter lists;
	private SimpleCursorAdapter notes;
	private Resources res;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		res = getResources();

		setContentView(R.layout.notes_list);
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();

		homeContext = this;

		notesView = (ListView) findViewById(R.id.notes);
		listsView = (ListView) findViewById(R.id.lists);
		TabHost.TabSpec spec = getTabHost().newTabSpec("tag1");

		spec.setContent(R.id.notes);
		spec.setIndicator("Notes",
				res.getDrawable(android.R.drawable.ic_menu_upload));
		getTabHost().addTab(spec);

		spec = getTabHost().newTabSpec("tag2");
		spec.setContent(R.id.lists);
		spec.setIndicator("Lists",
				res.getDrawable(android.R.drawable.ic_menu_agenda));
		getTabHost().addTab(spec);

		getTabHost().setCurrentTab(0);
		notesView.setOnItemClickListener(onNoteClick);
		listsView.setOnItemClickListener(onListClick);
		fillData();
		registerForContextMenu(notesView);
		registerForContextMenu(listsView);
	}

	private void fillData()
	{
		// Get all of the rows from the database and create the item list
		Cursor notesCursor = mDbHelper.fetchAllNotes();
		startManagingCursor(notesCursor);

		Cursor listsCursor = mDbHelper.fetchAllLists();
		startManagingCursor(listsCursor);

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] { NotesDbAdapter.KEY_TITLE };
		String[] listFrom = new String[] { NotesDbAdapter.KEY_TITLE };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { R.id.text1 };
		int[] listTo = new int[] { R.id.list_title };

		// Now create a simple cursor adapter and set it to display
		notes = new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor,
				from, to);
		notesView.setAdapter(notes);

		lists = new SimpleCursorAdapter(this, R.layout.list_display,
				listsCursor, listFrom, listTo);
		listsView.setAdapter(lists);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_NOTE_ID, 0, R.string.menu_insert).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, INSERT_LIST_ID, 0, R.string.list_insert).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, ABOUT_ID, 0, R.string.about).setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
		case INSERT_NOTE_ID:
			createNote();
			return true;
		case INSERT_LIST_ID:
			createList();
			return true;
		case ABOUT_ID:
			showAbout();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void showAbout()
	{
		 AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		 
         // set the message to display
		 alertbox.setIcon(android.R.drawable.ic_menu_info_details);
		 alertbox.setTitle("About");
		 TextView msg = new TextView(this);
		 msg.setTextSize(15);
		 msg.setPadding(2,2,2,2);
		 msg.setText(res.getString(R.string.app_name) +"\n" +
         		"Version: "+ res.getString(R.string.version) +"\n" +
         		"Developed by \n" +
         		"HashBang Software\n" +
         		"Feedback/Contact: \n" +
         		"hashbangsoftware@gmail.com");
		 msg.setGravity(Gravity.CENTER_HORIZONTAL);
		 alertbox.setView(msg);

         // add a neutral button to the alert box and assign a click listener
         alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener()
         {
             // click listener on the alert box
             public void onClick(DialogInterface arg0, int arg){}
         });
         alertbox.show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		super.onCreateContextMenu(menu, v, menuInfo);
		if (getTabHost().getCurrentTab() == 0) // Notes
		{
			Cursor mCursor = mDbHelper.getNoteCreateDate(info.id);
			startManagingCursor(mCursor);
			menu.setHeaderTitle(getResources().getString(R.string.create_date)
					+ " "
					+ mCursor.getString(mCursor
							.getColumnIndexOrThrow(NotesDbAdapter.KEY_CREATE_DATE)));
			menu.add(0, DELETE_ID, 0, R.string.menu_delete);
		} else
		// List
		{
			Cursor mCursor = mDbHelper.getListCreateDate(info.id);
			startManagingCursor(mCursor);
			menu.setHeaderTitle(getResources().getString(R.string.create_date)
					+ " "
					+ mCursor.getString(mCursor
							.getColumnIndexOrThrow(NotesDbAdapter.KEY_CREATE_DATE)));
			menu.add(0, EDIT_TITLE_ID, 0, R.string.edit_title);
			menu.add(0, DELETE_ID, 0, R.string.menu_delete);

		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case DELETE_ID:
			{
				AdapterContextMenuInfo deleteInfo = (AdapterContextMenuInfo) item
						.getMenuInfo();
				if (getTabHost().getCurrentTab() == 0) // Note
				{
					mDbHelper.deleteNote(deleteInfo.id);
				} else if (getTabHost().getCurrentTab() == 1) // List
				{
					mDbHelper.deleteList(deleteInfo.id);
				}
				fillData();
				return true;
			}
			case EDIT_TITLE_ID:
			{
				AdapterContextMenuInfo editInfo = (AdapterContextMenuInfo) item
						.getMenuInfo();
				updateListTitle(editInfo.id);
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	private void updateListTitle(final long id)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Please enter a title");

		// Set an EditText view to get user input
		final EditText titleInput = new EditText(this);
		titleInput.setText(DEFAULT_TITLE);
		titleInput.setSelectAllOnFocus(true);
		alert.setView(titleInput);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				String title = titleInput.getText().toString();
				if (!title.equals(null))
				{
					mDbHelper.updateListTitle(id, title);
				} else
				{
					mDbHelper.updateListTitle(id, DEFAULT_TITLE);
				}
				lists.getCursor().requery();
			}
		});
		alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.cancel();
			}
		});
		alert.show();
	}

	private void createNote()
	{
		Intent i = new Intent(this, NoteEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	private void createList()
	{
		Intent i = new Intent(this, ListEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE_LIST);
	}

	private final AdapterView.OnItemClickListener onNoteClick = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> l, View view, int position,
				long id)
		{
			Intent i = new Intent(homeContext, NoteEdit.class);
			i.putExtra(NotesDbAdapter.KEY_ROWID, id);
			startActivityForResult(i, ACTIVITY_EDIT);

		}
	};

	private final AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> l, View view, int position,
				long id)
		{
			Intent i = new Intent(homeContext, ListEdit.class);
			i.putExtra(NotesDbAdapter.KEY_ROWID, id);
			startActivityForResult(i, ACTIVITY_EDIT_LIST);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}
}
