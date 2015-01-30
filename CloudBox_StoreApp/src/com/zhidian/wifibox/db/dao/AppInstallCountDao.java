package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.AppInstallCountTable;

/**
 * app安装量、卸载量统计表，数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class AppInstallCountDao {

	private DBOpenHelper dbOpenHelper;

	public AppInstallCountDao(Context context) {
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

	/************************
	 * 保存App安装量、卸载量统计日志记录
	 ************************/
	public void saveAppInstallInfo(AppInstallBean bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.insert(AppInstallCountTable.TABLE_NAME, null,
						appInstallToContentValues(bean));
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

	private ContentValues appInstallToContentValues(AppInstallBean bean) {
		final ContentValues cv = new ContentValues();

		cv.put(AppInstallCountTable.FIELD_BOXNUM, bean.boxNum);
		cv.put(AppInstallCountTable.FIELD_UUID, bean.uuId);
		cv.put(AppInstallCountTable.FIELD_DOWNLOADSOURCE, bean.downloadSource);
		cv.put(AppInstallCountTable.FIELD_APPID, bean.appId);
		cv.put(AppInstallCountTable.FIELD_PACKAGENAME, bean.packageName);
		cv.put(AppInstallCountTable.FIELD_VERSION, bean.version);
		cv.put(AppInstallCountTable.FIELD_DOWNLOADMODEL, bean.downloadModel);
		cv.put(AppInstallCountTable.FIELD_NETWORKWAY, bean.networkWay);
		cv.put(AppInstallCountTable.FIELD_INSTALLTIME, bean.installTime);
		cv.put(AppInstallCountTable.FIELD_INSTALLTYPE, bean.installType);
		cv.put(AppInstallCountTable.FIELD_STATUS, bean.status);
		return cv;
	}

	/**
	 * 根据包名获取数据
	 */
	public AppInstallBean queryData(String packageName) {
		AppInstallBean bean = new AppInstallBean();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(false);
				cursor = db.rawQuery("select "
						+ AppInstallCountTable.FIELD_APPID + ","
						+ AppInstallCountTable.FIELD_VERSION + ","
						+ AppInstallCountTable.FIELD_DOWNLOADSOURCE + ","
						+ AppInstallCountTable.FIELD_INSTALLTIME + ","
						+ AppInstallCountTable.FIELD_DOWNLOADMODEL + " from "
						+ AppInstallCountTable.TABLE_NAME
						+ " where packageName = ?",
						new String[] { packageName });
				while (cursor.moveToNext()) {
					bean.appId = cursor.getString(0);
					bean.version = cursor.getString(1);
					bean.downloadSource = cursor.getString(2);
					bean.installTime = cursor.getString(3);
					bean.downloadModel = cursor.getString(4);
					bean.packageName = packageName;
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
		return bean;
	}

	/************************
	 * 获取所有数据
	 ************************/
	public List<AppInstallBean> getData() {
		List<AppInstallBean> result = new ArrayList<AppInstallBean>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ AppInstallCountTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					AppInstallBean bean = new AppInstallBean();
					bean.boxNum = cursor.getString(1);
					bean.uuId = cursor.getString(2);
					bean.downloadSource = cursor.getString(3);
					bean.appId = cursor.getString(4);
					bean.packageName = cursor.getString(5);
					bean.downloadModel = cursor.getString(6);
					bean.version = cursor.getString(7);
					bean.installType = cursor.getString(8);
					bean.status = cursor.getString(9);
					bean.networkWay = cursor.getString(10);
					bean.installTime = cursor.getString(11);

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
	 * 删除全部记录
	 ************************/
	public void deleteData() {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.execSQL("delete from " + AppInstallCountTable.TABLE_NAME
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
	 * 删除一条记录
	 ************************/
	public void deleteData(String packageName, String installType) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.execSQL("delete from " + AppInstallCountTable.TABLE_NAME
						+ " where packageName=? and installType=?",
						new String[] { packageName, installType });
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
