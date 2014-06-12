package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.database.Cursor;

import com.jiubang.ggheart.data.tables.GestureTable;

public class GestureSettingInfo {
	/**
	 * Home手势ID
	 */
	public static final int GESTURE_HOME_ID = 1;
	/**
	 * Up手势ID
	 */
	public static final int GESTURE_UP_ID = 2;
	/**
	 * Down手势ID
	 */
	public static final int GESTURE_DOWN_ID = 3;
	/**
	 * 双击空白处手势
	 */
	public static final int GESTURE_DOUBLLE_CLICK_ID = 4;

	public int mGestureId;
	public int mGestureAction;
	public int mGoShortCut = -1;
	public String mGestrueName;
	public String mAction;

	public GestureSettingInfo() {

	}

	/**
	 * 加入键值对
	 * 
	 * @param values
	 *            键值对
	 */
	public void contentValues(ContentValues values) {
		if (null == values) {
			return;
		}
		values.put(GestureTable.GESTUREID, mGestureId);
		values.put(GestureTable.GESTURENAME, mGestrueName);
		values.put(GestureTable.GESTURACTION, mGestureAction);
		values.put(GestureTable.ACTION, mAction);
		values.put(GestureTable.GOSHORTCUTITEM, mGoShortCut);
	}

	/**
	 * 解析数据
	 * 
	 * @param cursor
	 *            数据集
	 */
	public boolean parseFromCursor(Cursor cursor) {
		if (null == cursor) {
			return false;
		}

		boolean bData = cursor.moveToFirst();
		if (bData) {
			int idIndex = cursor.getColumnIndex(GestureTable.GESTUREID);
			int nameIndex = cursor.getColumnIndex(GestureTable.GESTURENAME);
			int gestureactionIndex = cursor.getColumnIndex(GestureTable.GESTURACTION);
			int actionIndex = cursor.getColumnIndex(GestureTable.ACTION);
			int goshortcutIndex = cursor.getColumnIndex(GestureTable.GOSHORTCUTITEM);
			if (-1 == idIndex || -1 == nameIndex || -1 == gestureactionIndex || -1 == actionIndex
					|| -1 == goshortcutIndex) {
				return false;
			}

			mGestureId = cursor.getInt(idIndex);
			mGestrueName = cursor.getString(nameIndex);
			mGestureAction = cursor.getInt(gestureactionIndex);
			mAction = cursor.getString(actionIndex);
			mGoShortCut = cursor.getInt(goshortcutIndex);
		}
		return bData;
	}
}
