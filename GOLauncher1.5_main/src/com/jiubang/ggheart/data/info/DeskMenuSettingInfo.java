package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.tables.DeskMenuTable;

public class DeskMenuSettingInfo {
	public boolean mEnable;

	public DeskMenuSettingInfo() {
		mEnable = true;
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
		values.put(DeskMenuTable.ENABLE, ConvertUtils.boolean2int(mEnable));
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
			int enableIndex = cursor.getColumnIndex(DeskMenuTable.ENABLE);

			if (-1 == enableIndex) {
				return false;
			}

			mEnable = ConvertUtils.int2boolean(cursor.getInt(enableIndex));
		}

		return bData;
	}
}
