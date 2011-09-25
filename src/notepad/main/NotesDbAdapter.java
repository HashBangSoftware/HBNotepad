/*
 * Copyright (C) 2008 Google Inc.
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
 */

package notepad.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_DATE = "due_date";
    public static final String KEY_BODY = "body";
    public static final String KEY_CREATE_DATE = "create_date";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String CREATE_NOTE_TABLE =
        "CREATE TABLE notes (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "title TEXT NOT NULL," +
        "body TEXT NOT NULL," +
        "due_date TEXT NOT NULL," +
        "create_date TEXT NOT NULL)";
    private static final String CREATE_LISTS_TABLE = 
        "CREATE TABLE lists (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "title TEXT NOT NULL," +
        "create_date TEXT NOT NULL)";
    private static final String CREATE_LIST_DATA_TABLE = 
        "CREATE TABLE list_data (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "list_id INTEGER NOT NULL," +
        "item_data TEXT NOT NULL," +
        "checked INTEGER NOT NULL)";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 4;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper 
    {

        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_NOTE_TABLE);
            db.execSQL(CREATE_LISTS_TABLE);
            db.execSQL(CREATE_LIST_DATA_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            db.execSQL("DROP TABLE IF EXISTS lists");
            db.execSQL("DROP TABLE IF EXISTS list_data");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx)
    {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException
    {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String date, String body, String create_date)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_CREATE_DATE, create_date);
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    public long createList(String title, String create_date)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_CREATE_DATE, create_date);
        
        return mDb.insert("lists", null, initialValues);
    }
    
    public long createListRowData(long listId, String data, int checked)
    {
    	ContentValues initialValues = new ContentValues();
        initialValues.put("list_id", listId);
        initialValues.put("item_data", data);
        initialValues.put("checked", checked);
        
        return mDb.insert("list_data", null, initialValues);
    }
    
    
    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId)
    {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
    public boolean deleteList(long rowId)
    {
        if((mDb.delete("lists", KEY_ROWID + "=" + rowId, null)>0)) //Delete List Reference
        {
        	 mDb.delete("list_data", "list_id" + "=" + rowId, null); //Delete Associated Data
        	 return true;
        }
        else
        {
        	return false;
        }
    }

    
    public boolean deleteListRow(long rowId)
    {
        return mDb.delete("list_data", KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes()
    {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_DATE, KEY_BODY, KEY_CREATE_DATE}, null, null, null, null, null);
    }
    
    public Cursor fetchAllLists()
    {

        return mDb.query("lists", new String[] {KEY_ROWID, KEY_TITLE}, null, null, null, null, null);
    }


    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException
    {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_DATE, KEY_BODY, KEY_CREATE_DATE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchList(long rowId) throws SQLException
    {

        Cursor mCursor =
            mDb.query(true, "lists", new String[] {KEY_ROWID,
                    "title", "create_date"}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor fetchListData(long listId) throws SQLException
    {

        Cursor mCursor =
            mDb.query(true, "list_data", new String[] {KEY_ROWID,"list_id",
                    "item_data", "checked"}, "list_id" + "=" + listId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String date, String body)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_DATE, date);
        args.put(KEY_BODY, body);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateList(long rowId, String title)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);

        return mDb.update("lists", args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateListData(long rowId, String description, int isChecked)
    {
        ContentValues args = new ContentValues();
        args.put("item_data", description);
        args.put("checked", isChecked);
        return mDb.update("list_data", args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
    public Cursor getNoteCreateDate(long rowId)
    {
    	 Cursor mCursor =
    			 mDb.query(true, DATABASE_TABLE, new String[] {KEY_CREATE_DATE}, KEY_ROWID + "=" + rowId, null,
    	                    null, null, null, null);
    	 if (mCursor != null) {
             mCursor.moveToFirst();
         }
    	 return mCursor;
    }
    public Cursor getListCreateDate(long rowId)
    {
    	 Cursor mCursor =
    			 mDb.query(true, "lists", new String[] {KEY_CREATE_DATE}, KEY_ROWID + "=" + rowId, null,
    	                    null, null, null, null);
    	 if (mCursor != null) {
             mCursor.moveToFirst();
         }
    	 return mCursor;
    }

	public boolean updateListTitle(long rowId, String newTitle)
	{
		ContentValues args = new ContentValues();
        args.put(KEY_TITLE, newTitle);
        return mDb.update("lists", args, KEY_ROWID + "=" + rowId, null) > 0;
		
	}
}
