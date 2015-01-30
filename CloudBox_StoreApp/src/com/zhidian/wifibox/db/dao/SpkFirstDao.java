package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhidian.wifibox.data.SpkInstallBean;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.SpkFirstTable;

/**
 * 市场第一次启动上传数据到插件数据，数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class SpkFirstDao {

	private DBOpenHelper dbOpenHelper;

	public SpkFirstDao(Context context) {
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
	 * 新增市场市场第一次启动日志记录
	 ************************/
	public void saveSpkInstallInfo(SpkInstallBean bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = getDb(true);
				db.beginTransaction();
				db.insert(SpkFirstTable.TABLE_NAME, null,
						spkInstallToContentValues(bean));
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

	private ContentValues spkInstallToContentValues(SpkInstallBean bean) {
		final ContentValues cv = new ContentValues();

		cv.put(SpkFirstTable.FIELD_BOXNUM, bean.boxNum);
		cv.put(SpkFirstTable.FIELD_UUID, bean.uuId);
		cv.put(SpkFirstTable.FIELD_INSTALLTIME, bean.installTime);
		cv.put(SpkFirstTable.FIELD_INSTALLPACKAGENAME, bean.installPackageName);
		return cv;
	}

	/************************
	 * 获取市场spk安装日志记录
	 ************************/
	public List<SpkInstallBean> getSpkData() {
		List<SpkInstallBean> result = new ArrayList<SpkInstallBean>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ SpkFirstTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					SpkInstallBean bean = new SpkInstallBean();
					bean.boxNum = cursor.getString(1);
					bean.uuId = cursor.getString(2);
					bean.installTime = cursor.getString(3);
					bean.installPackageName = cursor.getString(4);
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
	 * 删除一条市场spk安装日志记录
	 ************************/
	public void deleteOneData(String installTime) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = dbOpenHelper.getWritableDatabase();
				db.execSQL("delete from " + SpkFirstTable.TABLE_NAME
						+ " where installTime=?", new String[] { installTime });
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (db != null) {
						db.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/************************
	 * 删除市场spk安装日志记录
	 ************************/
	public void deleteSpkData() {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = null;
			try {
				db = dbOpenHelper.getWritableDatabase();
				db.execSQL("delete from " + SpkFirstTable.TABLE_NAME
						+ " where 1=1", new Object[] {});
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (db != null) {
						db.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
