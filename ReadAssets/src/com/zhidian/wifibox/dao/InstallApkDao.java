package com.zhidian.wifibox.dao;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.zhidian.bean.InstallBean;
import com.zhidian.wifibox.db.DBOpenHelper;
import com.zhidian.wifibox.table.InstallApkTable;

/**
 * 数据库操作类
 * 
 * @author zhaoyl
 * 
 */
public class InstallApkDao {

	private DBOpenHelper dbOpenHelper;

	public InstallApkDao(Context context) {
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

	/****************************
	 * 新增一条日志记录
	 ****************************/
	public synchronized void saveInfo(InstallBean bean) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			db.beginTransaction();
			try {
				db.insert(InstallApkTable.TABLE_NAME, null,
						installApkToContentValues(bean));
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
				db.close();
			}
		}

	}

	private ContentValues installApkToContentValues(InstallBean bean) {
		final ContentValues cv = new ContentValues();

		cv.put(InstallApkTable.FIELD_BOXNUM, bean.boxNum);
		cv.put(InstallApkTable.FIELD_CODE, bean.code);
		cv.put(InstallApkTable.FIELD_DOWNLOADURL, bean.downloadUrl);
		cv.put(InstallApkTable.FIELD_STATUS, bean.status);
		cv.put(InstallApkTable.FIELD_VERSION, bean.versionCode);
		cv.put(InstallApkTable.FIELD_TIME, bean.installTime);
		cv.put(InstallApkTable.FIELD_MSG, bean.msg);
		return cv;
	}
	
	/**
	 * 更新一条数据
	 * @param downloadUrl 下载链接
	 * @param installTime 安装时间
	 * @param unloadStatus 上传状态 0：表示上传成功；1：表示上传失败。
	 */
	
	public void updateData(String downloadUrl, String installTime, String unloadStatus){
		SQLiteDatabase db = null;
		synchronized (DBOpenHelper.sObj) {
			try {
				db = getDb(true);
				db.beginTransaction();
				ContentValues cv = new ContentValues();
				cv.put(InstallApkTable.FIELD_UNLOAD_STATUS, unloadStatus);
				db.update(InstallApkTable.TABLE_NAME, cv, InstallApkTable.FIELD_DOWNLOADURL + "= ? and " +
						InstallApkTable.FIELD_TIME + "= ?" , new String[]{downloadUrl, installTime});
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
				
			}finally{
				db.endTransaction();
				db.close();
			}
			
			
		}
	}
	
	/************************
	 * 获取所有App激活量统计日志记录
	 ************************/
	public List<InstallBean> getSpkData() {
		List<InstallBean> result = new ArrayList<InstallBean>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (DBOpenHelper.sObj) {

			try {
				db = getDb(false);
				cursor = db.rawQuery("select * from "
						+ InstallApkTable.TABLE_NAME, null);
				while (cursor.moveToNext()) {
					InstallBean bean = new InstallBean();
					bean.boxNum = cursor.getString(1);
					bean.code = cursor.getString(2);
					bean.downloadUrl = cursor.getString(3);
					bean.versionCode = cursor.getString(4);
					bean.status = cursor.getString(5);
					bean.installTime = cursor.getString(6);
					bean.msg = cursor.getString(7);
					bean.unloadStatus = cursor.getString(8);					
					
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
	 * 删除全部App激活量统计日志记录
	 ************************/

	public void deleteData() {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			try {
				db.execSQL("delete from " + InstallApkTable.TABLE_NAME
						+ " where 1=1", new Object[] {});
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
				db.close();
			}
		}

	}

	/************************
	 * 删除一条日志记录
	 ************************/

	public void deleteData(String installTime) {
		synchronized (DBOpenHelper.sObj) {
			SQLiteDatabase db = getDb(true);
			try {
				db.execSQL("delete from " + InstallApkTable.TABLE_NAME
						+ " where installTime=?", new String[] { installTime });
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
				db.close();
			}
		}

	}
}
