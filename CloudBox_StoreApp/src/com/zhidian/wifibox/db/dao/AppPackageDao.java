package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.zhidian.wifibox.data.AppDownloadBean;
import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.AppPackgeTable;

/**
 * app下载统计
 * 
 * @author zhaoyl
 * 
 */
public class AppPackageDao {
	private DBOpenHelper dbOpenHelper;

	public AppPackageDao(Context context) {
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

	/**
	 * 保存
	 */
	public void savePackageName(AppDownloadBean bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			db.beginTransaction();
			try {
				db.insert(AppPackgeTable.TABLE_NAME, null,
						dataToContentValues(bean));
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
				db.close();
			}
		}

	}

	private ContentValues dataToContentValues(AppDownloadBean bean) {
		final ContentValues cv = new ContentValues();

		cv.put(AppPackgeTable.FIELD_PACKAGENAME, bean.packageName);
		cv.put(AppPackgeTable.FIELD_APPID, bean.appId);
		cv.put(AppPackgeTable.FIELD_INSTALLTIME, bean.installTime);
		cv.put(AppPackgeTable.FIELD_DOWNLOADSOURCE, bean.downloadSource);
		cv.put(AppPackgeTable.FIELD_DOWNLOADMODEL, bean.downloadModel);
		cv.put(AppPackgeTable.FIELD_VERSION, bean.version);
		cv.put(AppPackgeTable.FIELD_ACTIVIT, bean.activit);
		return cv;
	}

	/**
	 * 删除一条记录
	 */
	public void deletePackageName(String packageName) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			db.beginTransaction();
			try {
				String[] args = { packageName };
				db.delete(AppPackgeTable.TABLE_NAME, "packageName=?", args);
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
				db.close();
			}
		}

	}

	/**
	 * 加入安装时间
	 */
	public void addInstallTime(String packageName, String installTime) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			try {
				db.beginTransaction();
				ContentValues cv = new ContentValues();
				cv.put(AppPackgeTable.FIELD_INSTALLTIME, installTime);
				db.update(AppPackgeTable.TABLE_NAME, cv,
						AppPackgeTable.FIELD_PACKAGENAME + " = ?",
						new String[] { packageName });
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
				db.close();
			}
		}

	}

	/**
	 * 修改激活状态
	 */
	public void updateActivit(String packageName) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			try {
				db.beginTransaction();
				ContentValues cv = new ContentValues();
				cv.put(AppPackgeTable.FIELD_ACTIVIT, "1");
				db.update(AppPackgeTable.TABLE_NAME, cv,
						AppPackgeTable.FIELD_PACKAGENAME + " = ?",
						new String[] { packageName });
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
				db.close();
			}
		}

	}

	/**
	 * 获取所有已下载且并未激活的包名
	 */
	public List<String> queryPagName() {
		List<String> pagList = new ArrayList<String>();
		synchronized (DBOpenHelper.sObj) {

			SQLiteDatabase db = getDb(false);
			Cursor cursor = null;
			try {
				cursor = db.rawQuery("select "
						+ AppPackgeTable.FIELD_PACKAGENAME + " from "
						+ AppPackgeTable.TABLE_NAME + " where activit=0", null);
				while (cursor.moveToNext()) {
					pagList.add(cursor.getString(0));
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

		return pagList;

	}

	/**
	 * 查询
	 * 
	 * @return
	 */
	public AppInstallBean queryPackage(String packageName) {

		AppInstallBean bean = new AppInstallBean();
		Cursor cursor = null;

		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(false);
			try {
				cursor = db.rawQuery("select " + AppPackgeTable.FIELD_APPID
						+ "," + AppPackgeTable.FIELD_DOWNLOADSOURCE + ","
						+ AppPackgeTable.FIELD_INSTALLTIME + ","
						+ AppPackgeTable.FIELD_DOWNLOADMODEL + ","
						+ AppPackgeTable.FIELD_VERSION + " from "
						+ AppPackgeTable.TABLE_NAME
						+ " where activit = 0 and packageName = ?",
						new String[] { packageName });
				while (cursor.moveToNext()) {
					bean.appId = cursor.getString(0);
					bean.downloadSource = cursor.getString(1);
					bean.installTime = cursor.getString(2);
					bean.downloadModel = cursor.getString(3);
					bean.version = cursor.getString(4);
					bean.packageName = packageName;
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

		return bean;
	}

	/**
	 * 查询(安装、卸载时用)
	 * 
	 * @return
	 */
	public AppDownloadBean queryPackage2(String packageName) {

		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(false);
			AppDownloadBean bean = new AppDownloadBean();
			Cursor cursor = null;
			try {
				cursor = db.rawQuery("select " + AppPackgeTable.FIELD_APPID
						+ "," + AppPackgeTable.FIELD_DOWNLOADSOURCE + ","
						+ AppPackgeTable.FIELD_INSTALLTIME + ","
						+ AppPackgeTable.FIELD_DOWNLOADMODEL + ","
						+ AppPackgeTable.FIELD_VERSION + " from "
						+ AppPackgeTable.TABLE_NAME + " where packageName = ?",
						new String[] { packageName });
				while (cursor.moveToNext()) {
					bean.appId = cursor.getString(0);
					bean.downloadSource = cursor.getString(1);
					bean.installTime = cursor.getString(2);
					bean.downloadModel = cursor.getString(3);
					bean.version = cursor.getString(4);
					bean.packageName = packageName;
					return bean;
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

		return null;
	}

}
