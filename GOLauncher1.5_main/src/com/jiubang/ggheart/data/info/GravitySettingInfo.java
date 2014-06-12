package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.database.Cursor;

import com.jiubang.ggheart.data.tables.GravityTable;

public class GravitySettingInfo {
	public boolean mEnable;
	public boolean mLandscape;
	public int mOrientationType;

	public GravitySettingInfo() {
		// mEnable = true;
		// mLandscape=false;
		mOrientationType = 0;
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
		// values.put(GravityTable.ENABLE, ConvertUtils.boolean2int(mEnable));
		// values.put(GravityTable.LANDSCAPE,
		// ConvertUtils.boolean2int(mLandscape));
		values.put(GravityTable.ORIENTATIONTYPE, Integer.valueOf(mOrientationType));

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
			// int enableIndex = cursor.getColumnIndex(GravityTable.ENABLE);
			//
			// if (-1 == enableIndex)
			// {
			// return false;
			// }
			//
			// mEnable = ConvertUtils.int2boolean(cursor.getInt(enableIndex));
			// int landscapeIndex =
			// cursor.getColumnIndex(GravityTable.LANDSCAPE);
			//
			// if (-1 == landscapeIndex)
			// {
			// return false;
			// }
			int orientationIndex = cursor.getColumnIndex(GravityTable.ORIENTATIONTYPE);

			if (-1 == orientationIndex) {
				return false;
			}

			mOrientationType = cursor.getInt(orientationIndex);
		}
		return bData;
	}
}
