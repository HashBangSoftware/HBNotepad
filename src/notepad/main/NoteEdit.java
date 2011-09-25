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

import java.util.Calendar;
import java.util.Date;

import model.Note;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import component.NotepadEditText;

public class NoteEdit extends Activity
{

    private EditText mTitleText;
    private EditText mEditDate;
    private NotepadEditText mBodyText;
    private Long mRowId;
    private String mCreateDate;
    private NotesDbAdapter mDbHelper;
    private int mYear;
    private int mMonth;
    private int mDay;
    private Context noteContext;
    private Note currentNote; 

    private static final int SAVE_ID = Menu.FIRST;
    private static final int SEND_SMS = Menu.FIRST+1;

    static final int DATE_DIALOG_ID = 1;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        noteContext = this;
        
        mTitleText = (EditText) findViewById(R.id.title);
        mEditDate = (EditText) findViewById(R.id.date);
        mBodyText = (NotepadEditText) findViewById(R.id.body);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null)
        {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                                    : null;
            Date currentDate = new Date();
            mCreateDate = currentDate.toString().substring(0, 16);
        }

        populateFields(savedInstanceState);
        
        mEditDate.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
            		showDialog(DATE_DIALOG_ID);
            
                }
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay();
        mDateSetListener =
                new DatePickerDialog.OnDateSetListener()
        		{

                    public void onDateSet(DatePicker view, int year, 
                                          int monthOfYear, int dayOfMonth)
                    {
                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;
                        updateDisplay();
                    }
                };  
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SAVE_ID, 0, R.string.save_note).setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, SEND_SMS, 0, R.string.send_sms).setIcon(android.R.drawable.ic_menu_send);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) 
    {
        switch(item.getItemId())
        {
            case SAVE_ID:
                saveNote();
                return true;
            case SEND_SMS:
                sendToSMS();
                return true;
        }


        return super.onMenuItemSelected(featureId, item);
    }


	private void populateFields(Bundle savedState)
    {
        if (mRowId != null)
        {
        	if(savedState == null)
        	{
	            Cursor note = mDbHelper.fetchNote(mRowId);
	            startManagingCursor(note);
	            mTitleText.setText(note.getString(
	                        note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
	            mEditDate.setText(note.getString(
	            			note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE)));
	            mBodyText.setText(note.getString(
	                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));      
	        }
        	else
        	{
        		Note tempNote = savedState.getParcelable("note_data");
        		mTitleText.setText(tempNote.getTitle());
	            mEditDate.setText(tempNote.getDue_date());
	            mBodyText.setText(tempNote.getBody());      
        	}
    	}
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        currentNote = new Note(mTitleText.getText().toString(),mEditDate.getText().toString(),mBodyText.getText().toString());
       	outState.putParcelable("note_data", currentNote);
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);        
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
    }
    
    private void saveNote()
    {
       	saveState();
       	setResult(RESULT_OK);
       	Toast.makeText(noteContext,"Note Saved", Toast.LENGTH_SHORT).show();
       	finish();
    }
    
    private void sendToSMS()
    {
    	Intent sendIntent = new Intent(Intent.ACTION_VIEW);
    	sendIntent.putExtra("sms_body", mTitleText.getText().toString()+
    						"\n" + mBodyText.getText().toString()); 
    	sendIntent.setType("vnd.android-dir/mms-sms");
    	startActivity(sendIntent);  
    }
    
    private void saveState()
    {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        String date = mEditDate.getText().toString();
     
        if (mRowId == null)
        {
            long id = mDbHelper.createNote(title, date, body, mCreateDate);
            if (id > 0)
            {
                mRowId = id;
            }
        } 
        else
        {
            mDbHelper.updateNote(mRowId, title, date, body);
        }
    }
    
    private void updateDisplay()
    {
        String month = "XXX";
    	switch(mMonth)
        {
        	case 0: 
        		month = "JAN";
        	break;
        	case 1: 
        		month = "FEB";
        	break;
        	case 2: 
        		month = "MAR";
        	break;
        	case 3: 
        		month = "APR";
        	break;
        	case 4: 
        		month = "MAY";
        	break;
        	case 5: 
        		month = "JUN";
        	break;
        	case 6: 
        		month = "JUL";
        	break;
        	case 7: 
        		month = "AUG";
        	break;
        	case 8: 
        		month = "SEP";
        	break;
        	case 9: 
        		month = "OCT";
        	break;
        	case 10: 
        		month = "NOV";
        	break;
        	case 11: 
        		month = "DEC";
        	break;
        
        }
    	mEditDate.setText(
            new StringBuilder()
                    // Month is 0 based so add 1
                    .append(mDay).append("-")
                    .append(month).append("-")
                    .append(mYear).append(" "),TextView.BufferType.EDITABLE);
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }
}
