package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.database.Cursor;

public interface IDatabaseObject {
	public void writeObject(ContentValues values, String table);

	public void readObject(Cursor cursor, String table);
}
