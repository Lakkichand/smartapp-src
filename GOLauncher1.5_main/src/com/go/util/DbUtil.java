package com.go.util;

import android.database.Cursor;
import android.database.SQLException;

public class DbUtil {
	public static int getInt(Cursor cursor, String key) {
		if (null == cursor || null == key || key.length() <= 0) {
			throw new SQLException("params err");
		}

		int column = cursor.getColumnIndex(key);
		if (column < 0) {
			throw new SQLException("key not exist");
		}

		return cursor.getInt(column);
	}

	public static String getString(Cursor cursor, String key) {
		if (null == cursor || null == key || key.length() <= 0) {
			throw new SQLException("params err");
		}

		int column = cursor.getColumnIndex(key);
		if (column < 0) {
			throw new SQLException("key not exist");
		}

		return cursor.getString(column);
	}

	public static float getFloat(Cursor cursor, String key) {
		if (null == cursor || null == key || key.length() <= 0) {
			throw new SQLException("params err");
		}

		int column = cursor.getColumnIndex(key);
		if (column < 0) {
			throw new SQLException("key not exist");
		}

		return cursor.getFloat(column);
	}

	public static long getLong(Cursor cursor, String key) {
		if (null == cursor || null == key || key.length() <= 0) {
			throw new SQLException("params err");
		}

		int column = cursor.getColumnIndex(key);
		if (column < 0) {
			throw new SQLException("key not exist");
		}

		return cursor.getLong(column);
	}

	public static short getShort(Cursor cursor, String key) {
		if (null == cursor || null == key || key.length() <= 0) {
			throw new SQLException("params err");
		}

		int column = cursor.getColumnIndex(key);
		if (column < 0) {
			throw new SQLException("key not exist");
		}

		return cursor.getShort(column);
	}
}
