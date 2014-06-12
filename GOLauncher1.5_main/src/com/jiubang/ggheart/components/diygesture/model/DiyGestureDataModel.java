package com.jiubang.ggheart.components.diygesture.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jiubang.ggheart.data.model.DataModel;

public class DiyGestureDataModel extends DataModel {

	public DiyGestureDataModel(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 读取数据库，获取当前所有手势
	 * 
	 * @return
	 */
	public ArrayList<DiyGestureInfo> getAllGestureInfos() {
		ArrayList<DiyGestureInfo> list = new ArrayList<DiyGestureInfo>();

		Cursor cursor = null;
		try {
			cursor = mDataProvider.queryDiyGestures();
			if (null != cursor && cursor.moveToFirst()) {
				do {
					DiyGestureInfo diyGestureInfo = new DiyGestureInfo();
					diyGestureInfo.readObject(cursor, null);
					list.add(diyGestureInfo);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}

		return list;
	}

	/**
	 * DB插入新手势记录
	 * 
	 * @param values
	 */
	public boolean insertGesture(ContentValues values) {
		return mDataProvider.addRecord(DiyGestureTable.TABLENAME, values);
	}

	/**
	 * DB删除手势记录
	 * 
	 * @param id
	 */
	public boolean delGesture(long id) {
		String selection = DiyGestureTable.ID + " = " + id;
		return mDataProvider.delRecord(DiyGestureTable.TABLENAME, selection);
	}

	/**
	 * DB更新手势记录
	 * 
	 * @param id
	 * @param values
	 */
	public boolean updateGesture(long id, ContentValues values) {
		String selection = DiyGestureTable.ID + " = " + id;
		return mDataProvider.updateRecord(DiyGestureTable.TABLENAME, values, selection);
	}
}
