package model;

import android.os.Parcel;
import android.os.Parcelable;

public class ListRow implements Parcelable
{
	private long _id;
	private long _listId;
	private int _isChecked;
	private String _description;

	public ListRow(long listId, int isChecked, String description)
	{
		_id = -1;
		_listId = listId;
		_isChecked = isChecked;
		_description = description;
	}

	public ListRow(long id, long listId, String description, int isChecked)
	{
		_id = id;
		_listId = listId;
		_isChecked = isChecked;
		_description = description;
	}

	public ListRow(Parcel source)
	{
		_id = source.readLong();
		_listId = source.readLong();
		_isChecked = source.readInt();
		_description = source.readString();
	}

	public long getListId()
	{
		return _listId;
	}

	public int getIsChecked()
	{
		return _isChecked;
	}

	public String getRowDescription()
	{
		return _description;
	}

	public void setRowDescription(String desc)
	{
		_description = desc;
	}

	public void setListId(long listId)
	{
		_listId = listId;
	}

	public void setIsChecked(int checked)
	{
		_isChecked = checked;
	}

	public void setId(long id)
	{
		_id = id;
	}

	public long getId()
	{
		return _id;
	}

	@Override
	public int describeContents()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1)
	{
		arg0.writeLong(_id);
		arg0.writeLong(_listId);
		arg0.writeInt(_isChecked);
		arg0.writeString(_description);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
	{
		public ListRow createFromParcel(Parcel in)
		{
			return new ListRow(in);
		}

		public ListRow[] newArray(int size)
		{
			return new ListRow[size];
		}
	};

}
