package com.zhidian.wifibox.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zhidian.wifibox.db.table.AppActivateCountTable;
import com.zhidian.wifibox.db.table.AppDownloadCountTable;
import com.zhidian.wifibox.db.table.AppInstallCountTable;
import com.zhidian.wifibox.db.table.AppPackgeTable;
import com.zhidian.wifibox.db.table.InstallSpkTable;

/**
 * 数据库管理工具类
 * 
 * @author zhaoyl
 * 
 */
public class DBOpenHelper extends SQLiteOpenHelper {

	private static final String TAG = DBOpenHelper.class.getSimpleName();
	private static final String DBNAME = "wifibox.db"; // 数据库名
	private static final int VERSION = 1; // 版本

	public DBOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "create database");
		createAllTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		Log.i(TAG, "update database");
		dropAllTables(db);
		onCreate(db);

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

	}
}
