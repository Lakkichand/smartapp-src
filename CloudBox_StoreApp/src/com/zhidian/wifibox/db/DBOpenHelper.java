package com.zhidian.wifibox.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zhidian.wifibox.db.table.AppActivateCountTable;
import com.zhidian.wifibox.db.table.AppDownloadCountTable;
import com.zhidian.wifibox.db.table.AppDownloadSpeedTable;
import com.zhidian.wifibox.db.table.AppDownloadTable;
import com.zhidian.wifibox.db.table.AppInstallCountTable;
import com.zhidian.wifibox.db.table.AppPackgeTable;
import com.zhidian.wifibox.db.table.InstallSpkTable;
import com.zhidian.wifibox.db.table.SpkFirstTable;
import com.zhidian.wifibox.db.table.SpkStartTable;
import com.zhidian.wifibox.util.FileUtil;

/**
 * 数据库管理工具类
 * 
 * @author zhaoyl
 * 
 */
public class DBOpenHelper extends SQLiteOpenHelper {

	public static final Object sObj = new Object();
	private static final String TAG = DBOpenHelper.class.getSimpleName();
	private static final int VERSION = 4; // 当前数据库版本
	
	private Context mContext;
	private static DBOpenHelper mInstance;

	/**
	 * 当手机有SD卡的时候，把数据库创建在SD卡上 如没有，则创建到缓存中。
	 * 
	 * @param context
	 */
	public DBOpenHelper(Context context) {
		super(context, FileUtil.getDbName(), null, VERSION);

	}
	
	public static DBOpenHelper getInstance(Context context) {
		synchronized (sObj) {
			if (mInstance == null) {
				mInstance = new DBOpenHelper(context);
			}
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.e(TAG, "create database");
		createAllTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.e(TAG, "update database");
		for (int j = oldVersion + 1; j <= newVersion; j++) {

			switch (j) {
			case 2:
				Log.i(TAG, "update database 2");
				db.execSQL(SpkStartTable.getCreateSQL());
				db.execSQL(AppDownloadTable.getCreateSQL());

				break;

			case 3:
				Log.i(TAG, "update database 3");
				db.execSQL(SpkFirstTable.getCreateSQL());

				break;

			case 4:
				Log.i(TAG, "update database 4");
				db.execSQL(AppDownloadSpeedTable.getCreateSQL());
				break;

			default:
				break;
			}
		}

	}

	/*************************
	 * 新建所有表
	 *************************/
	private static void createAllTables(SQLiteDatabase db) {
		db.execSQL(AppActivateCountTable.getCreateSQL());
		db.execSQL(AppDownloadCountTable.getCreateSQL());
		db.execSQL(AppInstallCountTable.getCreateSQL());
		db.execSQL(InstallSpkTable.getCreateSQL());
		db.execSQL(AppPackgeTable.getCreateSQL());
		db.execSQL(SpkStartTable.getCreateSQL());
		db.execSQL(AppDownloadTable.getCreateSQL());
		db.execSQL(SpkFirstTable.getCreateSQL());
		db.execSQL(AppDownloadSpeedTable.getCreateSQL());
	}

	/*************************
	 * 删除所有表
	 *************************/
	private static void dropAllTables(SQLiteDatabase db) {
		db.execSQL(AppActivateCountTable.getDropSQL());
		db.execSQL(AppDownloadCountTable.getDropSQL());
		db.execSQL(AppInstallCountTable.getDropSQL());
		db.execSQL(InstallSpkTable.getDropSQL());
		db.execSQL(AppPackgeTable.getDropSQL());
		db.execSQL(SpkStartTable.getDropSQL());
		db.execSQL(AppDownloadTable.getDropSQL());
		db.execSQL(SpkFirstTable.getDropSQL());
		db.execSQL(AppDownloadSpeedTable.getDropSQL());

	}
}
