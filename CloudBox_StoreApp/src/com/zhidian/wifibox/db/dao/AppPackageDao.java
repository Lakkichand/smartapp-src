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
 * 记录市场已下载app信息表，下载成功插入记录，卸载应用删除记录
 * 
 * @author zhaoyl
 * 
 */
public class AppPackageDao {
	private DBOpenHelper dbOpenHelper;

	public AppPackageDao(Context context) {
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

	/**
	 * 保存
	 */
	public void savePackageName(AppDownloadBean bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.insert(AppPackgeTable.TABLE_NAME, null,
						dataToContentValues(bean));
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
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				String[] args = { packageName };
				db.delete(AppPackgeTable.TABLE_NAME, "packageName=?", args);
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

	/**
	 * 加入安装时间
	 */
	public void addInstallTime(String packageName, String installTime) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
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

	/**
	 * 修改激活状态
	 */
	public void updateActivit(String packageName) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
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

	/**
	 * 获取所有已下载应用的信息
	 */
	public List<AppInstallBean> getAllDownloadPackageName() {
		List<AppInstallBean> pagList = new ArrayList<AppInstallBean>();
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			Cursor cursor = null;
			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ AppPackgeTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					AppInstallBean bean = new AppInstallBean();
					bean.appId = cursor.getString(1);
					bean.downloadSource = cursor.getString(2);
					bean.installTime = cursor.getString(3);
					bean.downloadModel = cursor.getString(4);
					bean.version = cursor.getString(5);
					bean.packageName = cursor.getString(7);
					pagList.add(bean);
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
		return pagList;
	}

	/**
	 * 获取所有已下载且并未激活的包名
	 */
	public List<String> queryPagName() {
		List<String> pagList = new ArrayList<String>();
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			Cursor cursor = null;
			try {
				db = getDb(false);
				cursor = db.rawQuery("select "
						+ AppPackgeTable.FIELD_PACKAGENAME + " from "
						+ AppPackgeTable.TABLE_NAME + " where activit=0", null);
				while (cursor.moveToNext()) {
					pagList.add(cursor.getString(0));
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
		return pagList;
	}

	/**
	 * 查询
	 * 
	 * @return
	 */
	public AppInstallBean queryPackage(String packageName) {
		AppInstallBean bean = new AppInstallBean();
		synchronized (DBOpenHelper.sObj) {
			Cursor cursor = null;
			SQLiteDatabase db = null;
			try {
				db = getDb(false);
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

	/**
	 * 查询(安装、卸载时用)
	 * 
	 * @return
	 */
	public AppDownloadBean queryPackage2(String packageName) {
		synchronized (DBOpenHelper.sObj) {
			AppDownloadBean bean = new AppDownloadBean();
			SQLiteDatabase db = null;
			Cursor cursor = null;
			try {
				db = getDb(false);
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
		return null;
	}
}
