package com.zhidian.wifibox.db;

import com.zhidian.wifibox.table.InstallApkTable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库管理工具类
 * 
 * @author zhaoyl
 * 
 */
public class DBOpenHelper extends SQLiteOpenHelper {

	public static final Object sObj=new Object();
	private static final String TAG = DBOpenHelper.class.getSimpleName();
//	public static final String WIFIBOX_DIR = Environment
//			.getExternalStorageDirectory() + "/MIBAO/";
	private static String DBNAME = "zhiapk.db"; // 数据库名
	private static final int VERSION = 4; // 版本

	
	/**
	 * @param context
	 */
	public DBOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);		
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.e(TAG, "create database");
		createAllTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.e(TAG, "update database");
//		for (int j = oldVersion + 1; j <= newVersion; j++) {
//			
//			switch (j) {
//			case 2:
//				Log.i(TAG, "update database 2");
//				db.execSQL(SpkStartTable.getCreateSQL());
//				db.execSQL(AppDownloadTable.getCreateSQL());
//				
//				break;
//				
//			case 3:
//				Log.i(TAG, "update database 3");
//				db.execSQL(SpkFirstTable.getCreateSQL());
//				
//				break;
//				
//			case 4:
//				Log.i(TAG, "update database 4");
//				db.execSQL(AppDownloadSpeedTable.getCreateSQL());
//				break;
//
//			default:
//				break;
//			}
//		}
		
//		dropAllTables(db);
//		onCreate(db);

	}

	/*************************
	 * 新建所有表
	 *************************/
	private static void createAllTables(SQLiteDatabase db) {
		db.execSQL(InstallApkTable.getCreateSQL());
	}

	/*************************
	 * 删除所有表
	 *************************/
	private static void dropAllTables(SQLiteDatabase db) {
		db.execSQL(InstallApkTable.getDropSQL());

	}
}
