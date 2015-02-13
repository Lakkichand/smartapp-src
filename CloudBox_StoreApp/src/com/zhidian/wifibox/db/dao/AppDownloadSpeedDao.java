package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.zhidian.wifibox.data.DownloadSpeed;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.AppDownloadSpeedTable;

/**
 * app下载速度统计，数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class AppDownloadSpeedDao {

	private DBOpenHelper dbOpenHelper;

	public AppDownloadSpeedDao(Context context) {
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

	/**************************
	 * 新增数据
	 *************************/
	public void saveData(DownloadSpeed speed) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.insert(AppDownloadSpeedTable.TABLE_NAME, null,
						downloadToContentValues(speed));
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

	private ContentValues downloadToContentValues(DownloadSpeed speed) {
		ContentValues cv = new ContentValues();
		cv.put(AppDownloadSpeedTable.FIELD_BOXNUM, speed.boxNum);
		cv.put(AppDownloadSpeedTable.FIELD_UUID, speed.uuId);
		cv.put(AppDownloadSpeedTable.FIELD_APPID, speed.appId);
		cv.put(AppDownloadSpeedTable.FIELD_APPNAME, speed.appName);
		cv.put(AppDownloadSpeedTable.FIELD_DOWNLOADTIME, speed.time);
		cv.put(AppDownloadSpeedTable.FIELD_DOWNLOAD_SPEED, speed.speed);
		cv.put(AppDownloadSpeedTable.FIELD_DOWNLOADSOURCE, speed.downloadSource);
		cv.put(AppDownloadSpeedTable.FIELD_DOWNLOADMODEL, speed.downloadModel);
		cv.put(AppDownloadSpeedTable.FIELD_VERSION, speed.version);
		cv.put(AppDownloadSpeedTable.FIELD_PACKAGENAME, speed.packageName);
		cv.put(AppDownloadSpeedTable.FIELD_NETWORKWAY, speed.networkWay);
		cv.put(AppDownloadSpeedTable.FIELD_UNIQUE, speed.unique);
		cv.put(AppDownloadSpeedTable.FIELD_CURRENTSIZE, speed.currentSize);
		cv.put(AppDownloadSpeedTable.FIELD_TOTALSIZE, speed.totalSize);
		return cv;
	}

	/**************************
	 * 查询所有数据
	 *************************/
	public List<DownloadSpeed> getAllData() {
		List<DownloadSpeed> result = new ArrayList<DownloadSpeed>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(false);
				db.beginTransaction();
				cursor = db.rawQuery("select * from "
						+ AppDownloadSpeedTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					DownloadSpeed bean = new DownloadSpeed();
					bean.unique = cursor.getString(1);
					bean.boxNum = cursor.getString(2);
					bean.uuId = cursor.getString(3);
					bean.appId = cursor.getString(4);
					bean.time = cursor.getString(5);
					bean.downloadSource = cursor.getString(6);
					bean.downloadModel = cursor.getString(7);
					bean.version = cursor.getString(8);
					bean.packageName = cursor.getString(9);
					bean.networkWay = cursor.getString(10);
					bean.speed = cursor.getString(11);
					bean.currentSize = cursor.getString(12);
					bean.totalSize = cursor.getString(13);
					bean.appName = cursor.getString(14);
					result.add(bean);
				}
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (db != null) {
						db.endTransaction();
					}
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
	 * 删除所有数据
	 ************************/
	public void deleteAllData() {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.execSQL("delete from " + AppDownloadSpeedTable.TABLE_NAME
						+ " where 1=1", new Object[] {});
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

	/************************
	 * 删除一条数据
	 ************************/
	public void deleteData(String appId, String downloadTime) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.execSQL("delete from " + AppDownloadSpeedTable.TABLE_NAME
						+ " where appId=? and downloadTime=?", new String[] {
						appId, downloadTime });
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
}
