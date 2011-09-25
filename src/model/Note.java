package model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class Note implements Parcelable
{
	private String title;
	private String due_date;
	private String body;
	
	public Note(String title, String due_date, String body)
	{
		this.title = title;
		this.due_date = due_date;
		this.body = body;
	}
	
	public Note(Parcel source)
	{
		this.title = source.readString();
		this.due_date = source.readString();
		this.body = source.readString();
	}
	

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDue_date()
	{
		return due_date;
	}

	public void setDue_date(String due_date)
	{
		this.due_date = due_date;
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}



	@Override
	public int describeContents()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(this.title);
		dest.writeString(this.due_date);
		dest.writeString(this.body);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
	{
		public Note createFromParcel(Parcel in)
		{
			return new Note(in);
		}

		public Note[] newArray(int size)
		{
			return new Note[size];
		}
	};

}
