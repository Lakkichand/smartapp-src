package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.AppDownloadCountTable;

/**
 * app下载成功量统计，数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class AppDownloadCountDao {

	private DBOpenHelper dbOpenHelper;

	public AppDownloadCountDao(Context context) {
		dbOpenHelper = new DBOpenHelper(context);
	}

	public SQLiteDatabase getDb(boolean writeable) {
		synchronized (DBOpenHelper.sObj) {
			if (writeable) {
				return dbOpenHelper.getWritableDatabase();
			} else {
				return dbOpenHelper.getReadableDatabase();
			}
		}

	}

	/***************************
	 * 新增app下载量统计日志记录begin
	 ***************************/
	public void saveAppDownloadInfo(AppDownloadCount bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			db.beginTransaction();
			try {
				db.insert(AppDownloadCountTable.TABLE_NAME, null,
						downloadToContentValues(bean));
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
				db.close();
			}
		}
		

	}

	private ContentValues downloadToContentValues(AppDownloadCount bean) {
		final ContentValues cv = new ContentValues();

		cv.put(AppDownloadCountTable.FIELD_BOXNUM, bean.boxNum);
		cv.put(AppDownloadCountTable.FIELD_UUID, bean.uuId);
		cv.put(AppDownloadCountTable.FIELD_DOWNLOADSOURCE, bean.downloadSource);
		cv.put(AppDownloadCountTable.FIELD_APPID, bean.appId);
		cv.put(AppDownloadCountTable.FIELD_PACKAGENAME, bean.packageName);
		cv.put(AppDownloadCountTable.FIELD_VERSION, bean.version);
		cv.put(AppDownloadCountTable.FIELD_DOWNLOADMODEL, bean.downloadModel);
		cv.put(AppDownloadCountTable.FIELD_NETWORKWAY, bean.networkWay);
		cv.put(AppDownloadCountTable.FIELD_DOWNLOADTIME, bean.downloadTime);
		return cv;
	}

	/************************
	 * 获取所有App下载量统计日志记录
	 ************************/
	public List<AppDownloadCount> getSpkData() {
		List<AppDownloadCount> result = new ArrayList<AppDownloadCount>();
		SQLiteDatabase db = null;
		Cursor cursor = null;

		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ AppDownloadCountTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					AppDownloadCount bean = new AppDownloadCount();
					bean.boxNum = cursor.getString(1);
					bean.uuId = cursor.getString(2);
					bean.downloadSource = cursor.getString(3);
					bean.appId = cursor.getString(4);
					bean.packageName = cursor.getString(5);
					bean.downloadModel = cursor.getString(6);
					bean.version = cursor.getString(7);
					bean.networkWay = cursor.getString(8);
					bean.downloadTime = cursor.getString(9);
					result.add(bean);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null)
					cursor.close();
				if (db != null)
					db.close();
			}
		}

		return result;
	}

	/************************
	 * 删除所有App下载量统计日志记录
	 ************************/

	public void deleteData() {

		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			try {
				db.execSQL("delete from " + AppDownloadCountTable.TABLE_NAME
						+ " where 1=1", new Object[] {});
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
				db.close();
			}
		}

	}

	/************************
	 * 删除一条App下载量统计日志记录
	 ************************/

	public void deleteData(String packageName) {

		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			try {
				db.execSQL("delete from " + AppDownloadCountTable.TABLE_NAME
						+ " where packageName=?", new String[] { packageName });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
				db.close();
			}
		}

	}

}
