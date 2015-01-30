package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.data.AppctivateCount;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.AppActivateCountTable;

/**
 * app激活量统计，数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class AppActivateCountDao {

	private DBOpenHelper dbOpenHelper;

	public AppActivateCountDao(Context context) {
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

	/****************************
	 * 新增App激活量统计日志记录
	 ****************************/
	public synchronized void saveAppActivateInfo(AppInstallBean bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.insert(AppActivateCountTable.TABLE_NAME, null,
						appActivateToContentValues(bean));
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

	private ContentValues appActivateToContentValues(AppInstallBean bean) {
		final ContentValues cv = new ContentValues();

		cv.put(AppActivateCountTable.FIELD_BOXNUM, bean.boxNum);
		cv.put(AppActivateCountTable.FIELD_UUID, bean.uuId);
		cv.put(AppActivateCountTable.FIELD_DOWNLOADSOURCE, bean.downloadSource);
		cv.put(AppActivateCountTable.FIELD_APPID, bean.appId);
		cv.put(AppActivateCountTable.FIELD_PACKAGENAME, bean.packageName);
		cv.put(AppActivateCountTable.FIELD_VERSION, bean.version);
		cv.put(AppActivateCountTable.FIELD_DOWNLOADMODEL, bean.downloadModel);
		cv.put(AppActivateCountTable.FIELD_NETWORKWAY, bean.networkWay);
		cv.put(AppActivateCountTable.FIELD_INSTALLTIME, bean.installTime);
		cv.put(AppActivateCountTable.FIELD_ACTIVATETIME, bean.activateTime);
		cv.put(AppActivateCountTable.FIELD_ISNETWORK, bean.isNetWork);
		cv.put(AppActivateCountTable.FIELD_ISINSERTSD, bean.isInsertSD);
		return cv;
	}

	/************************
	 * 获取所有App激活量统计日志记录
	 ************************/
	public List<AppctivateCount> getSpkData() {
		List<AppctivateCount> result = new ArrayList<AppctivateCount>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ AppActivateCountTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					AppctivateCount bean = new AppctivateCount();
					bean.boxNum = cursor.getString(1);
					bean.uuId = cursor.getString(2);
					bean.downloadSource = cursor.getString(3);
					bean.appId = cursor.getString(4);
					bean.packageName = cursor.getString(5);
					bean.downloadModel = cursor.getString(6);
					bean.version = cursor.getString(7);
					bean.activateTime = cursor.getString(8);
					bean.isNetwork = cursor.getString(9);
					bean.networkWay = cursor.getString(10);
					bean.isInsertSD = cursor.getString(11);
					bean.installTime = cursor.getString(12);
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
	 * 删除全部App激活量统计日志记录
	 ************************/
	public void deleteData() {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.execSQL("delete from " + AppActivateCountTable.TABLE_NAME
						+ " where 1=1", new Object[] {});
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (db != null) {
					try {
						db.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/************************
	 * 删除一条App激活量统计日志记录
	 ************************/
	public void deleteData(String packageName) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.execSQL("delete from " + AppActivateCountTable.TABLE_NAME
						+ " where packageName=?", new String[] { packageName });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (db != null) {
					try {
						db.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
