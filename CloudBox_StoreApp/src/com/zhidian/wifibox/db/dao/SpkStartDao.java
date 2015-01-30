package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhidian.wifibox.data.SpkStart;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.SpkStartTable;

/**
 * 市场启动统计，数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class SpkStartDao {

	private DBOpenHelper dbOpenHelper;

	public SpkStartDao(Context context) {
		dbOpenHelper = DBOpenHelper.getInstance(context);
	}

	private SQLiteDatabase getDb(boolean writeable) {
		synchronized (DBOpenHelper.sObj) {
			if (writeable) {
				return dbOpenHelper.getWritableDatabase();
			} else {
				return dbOpenHelper.getReadableDatabase();
			}
		}
	}

	/***************************
	 * 新增一条数据
	 ***************************/
	public void save(SpkStart bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.insert(SpkStartTable.TABLE_NAME, null, values(bean));
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (db != null) {
					try {
						db.endTransaction();
						db.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private ContentValues values(SpkStart bean) {
		final ContentValues cv = new ContentValues();

		cv.put(SpkStartTable.FIELD_BOXNUM, bean.boxNum);
		cv.put(SpkStartTable.FIELD_UUID, bean.uuId);
		cv.put(SpkStartTable.FIELD_STARTTIME, bean.startTime);
		cv.put(SpkStartTable.FIELD_MAC, bean.mac);
		return cv;
	}

	/************************
	 * 获取所有数据
	 ************************/
	public List<SpkStart> getAllData() {
		List<SpkStart> result = new ArrayList<SpkStart>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ SpkStartTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					SpkStart bean = new SpkStart();
					bean.boxNum = cursor.getString(1);
					bean.uuId = cursor.getString(2);
					bean.startTime = cursor.getString(3);
					bean.mac = cursor.getString(4);
					result.add(bean);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (cursor != null) {
						cursor.close();
					}
					if (db != null) {
						db.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/************************
	 * 删除一条数据
	 ************************/
	public void deleteData(String startTime) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.execSQL("delete from " + SpkStartTable.TABLE_NAME
						+ " where startTime=?", new String[] { startTime });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					db.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
