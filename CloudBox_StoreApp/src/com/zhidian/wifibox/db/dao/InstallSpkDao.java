package com.zhidian.wifibox.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhidian.wifibox.data.SpkInstallBean;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.db.table.InstallSpkTable;

/**
 * 市场spk安装，数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class InstallSpkDao {

	private DBOpenHelper dbOpenHelper;

	public InstallSpkDao(Context context) {
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

	/************************
	 * 新增市场spk安装日志记录
	 ************************/
	public void saveSpkInstallInfo(SpkInstallBean bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			db.beginTransaction();
			try {
				db.insert(InstallSpkTable.TABLE_NAME, null,
						spkInstallToContentValues(bean));
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
				db.close();
			}
		}

	}

	private ContentValues spkInstallToContentValues(SpkInstallBean bean) {
		final ContentValues cv = new ContentValues();

		cv.put(InstallSpkTable.FIELD_BOXNUM, bean.boxNum);
		cv.put(InstallSpkTable.FIELD_UUID, bean.uuId);
		cv.put(InstallSpkTable.FIELD_INSTALLTIME, bean.installTime);
		cv.put(InstallSpkTable.FIELD_INSTALLPACKAGENAME,
				bean.installPackageName);
		cv.put(InstallSpkTable.FIELD_MANUFACTURER, bean.manufacturer);
		cv.put(InstallSpkTable.FIELD_MODEL, bean.model);
		cv.put(InstallSpkTable.FIELD_VERSION, bean.version);
		cv.put(InstallSpkTable.FIELD_SIMOPERATORNAME, bean.simOperatorName);
		cv.put(InstallSpkTable.FIELD_NETWORKCOUNTRYISO, bean.networkCountryIso);
		cv.put(InstallSpkTable.FIELD_MAC, bean.mac);
		cv.put(InstallSpkTable.FIELD_IMEI, bean.imei);
		cv.put(InstallSpkTable.FIELD_IMSI, bean.imsi);
		return cv;
	}

	/************************
	 * 获取市场spk安装日志记录
	 ************************/
	public synchronized List<SpkInstallBean> getSpkData() {
		List<SpkInstallBean> result = new ArrayList<SpkInstallBean>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {

			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ InstallSpkTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					SpkInstallBean bean = new SpkInstallBean();
					bean.boxNum = cursor.getString(0);
					bean.uuId = cursor.getString(1);
					bean.installTime = cursor.getString(2);
					bean.installPackageName = cursor.getString(3);
					bean.manufacturer = cursor.getString(4);
					bean.model = cursor.getString(5);
					bean.version = cursor.getString(6);
					bean.simOperatorName = cursor.getString(7);
					bean.networkCountryIso = cursor.getString(8);
					bean.mac = cursor.getString(9);
					bean.imei = cursor.getString(10);
					bean.imsi = cursor.getString(11);
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
	 * 删除市场spk安装日志记录
	 ************************/

	public void deleteSpkData() {

		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			try {
				db.execSQL("delete from " + InstallSpkTable.TABLE_NAME
						+ " where 1=1", new Object[] {});
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
				db.close();
			}
		}

	}
}
