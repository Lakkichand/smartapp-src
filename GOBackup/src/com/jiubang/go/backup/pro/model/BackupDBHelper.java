package com.jiubang.go.backup.pro.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppRestoreEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.CallLogBackupEntry;
import com.jiubang.go.backup.pro.data.ContactsBackupEntry;
import com.jiubang.go.backup.pro.data.LauncherDataBackupEntry;
import com.jiubang.go.backup.pro.data.LauncherDataRestoreEntry;
import com.jiubang.go.backup.pro.data.LauncherDataRestoreEntry.LauncherDataExtraInfo;
import com.jiubang.go.backup.pro.data.MmsBackupEntry;
import com.jiubang.go.backup.pro.data.SmsRestoreEntry;
import com.jiubang.go.backup.pro.data.UserDictionaryBackupEntry;
import com.jiubang.go.backup.pro.data.WifiBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageBean;
import com.jiubang.go.backup.pro.image.util.OneImageRestoreEntry;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份数据库 Version1 为表AppTable Version2 新增MimetypeTable表，新增DataTable表 Version3
 * MimetypeTable表增加mimetype类型， Apptable和DataTable表扩充字段 Version4
 * MimetypeTable表新增壁纸和铃声类型 version 5 MimetypeTable新增日历和书签类型
 *
 * @author wencan
 */

public class BackupDBHelper extends SQLiteOpenHelper {
	private static final String LOG_TAG = BackupDBHelper.class.getSimpleName();

	private static final String DB_NAME = "backup_config.db";
	private static final int DB_VERSION = 6;

	private String mCurDbName;

	private Context mContext;

	public BackupDBHelper(Context ctx, String dbName, int dbVersion) {
		super(ctx, dbName, null, dbVersion);
		File dbFile = new File(dbName);
		mCurDbName = dbFile.getName();
		mContext = ctx.getApplicationContext();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("GOBackup", "BackupDBHelper : onCreate()");
		try {
			db.beginTransaction();
			db.execSQL(AppTable.CREATE_TABLE);
			db.execSQL(DataTable.CREATE_TABLE);
			db.execSQL(MimetypeTable.CREATE_TABLE);
			insertMimetypeTableDefaultValue(db);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.d(LOG_TAG, "onCreate : execSQL error :" + e.getMessage());
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	private void insertMimetypeTableDefaultValue(SQLiteDatabase db) {
		if (db == null) {
			return;
		}
		SQLiteStatement ss = null;
		try {
			ss = db.compileStatement("INSERT INTO " + MimetypeTable.TABLENAME + "("
					+ MimetypeTable.MIMETYPE + "," + MimetypeTable.MIMETYPE_VALUE
					+ ") VALUES (?,?)");

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_LAUCHER_DATA,
					MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_SMS, MimetypeTable.MIMETYPE_VALUE_SMS);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_MMS, MimetypeTable.MIMETYPE_VALUE_MMS);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_CONTACT,
					MimetypeTable.MIMETYPE_VALUE_CONTACT);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_CALLLOG,
					MimetypeTable.MIMETYPE_VALUE_CALLLOG);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_WIFI,
					MimetypeTable.MIMETYPE_VALUE_WIFI);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_GOLAUNCHER_SETTING,
					MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_APP, MimetypeTable.MIMETYPE_VALUE_APP);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_CONFIG,
					MimetypeTable.MIMETYPE_VALUE_CONFIG);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_USER_DICTIONARY,
					MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_WALLPAPER,
					MimetypeTable.MIMETYPE_VALUE_WALLPAPER);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_RINGTONE,
					MimetypeTable.MIMETYPE_VALUE_RINGTONE);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_CALENDAR,
					MimetypeTable.MIMETYPE_VALUE_CALENDAR);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_BOOKMARK,
					MimetypeTable.MIMETYPE_VALUE_BOOKMARK);
			ss.executeInsert();

			buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_IMAGE,
					MimetypeTable.MIMETYPE_VALUE_IMAGE);
			ss.executeInsert();
		} finally {
			if (ss != null) {
				ss.close();
			}
		}

	}

	private SQLiteStatement buildMimetypeStatement(SQLiteStatement ss, String mimetype,
			int mimetypeValue) {
		ss.clearBindings();
		ss.bindString(1, mimetype);
		ss.bindLong(2, mimetypeValue);
		return ss;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("GOBackup", "BackupDBHelper : onUpgrade() : oldVersion = " + oldVersion
				+ ", newVersion = " + newVersion);
		// TODO:根据版本号，更新数据库表结构
		Log.d(LOG_TAG, "onUpgrade : oldVersion = " + oldVersion + ", newVersion = " + newVersion);
		try {
			db.beginTransaction();
			onUpgrade2(db, oldVersion, newVersion);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	private void onUpgrade2(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 数据库版本升级从这里添加
		ArrayList<UpgradeDB> upgradeDBFuncS = new ArrayList<BackupDBHelper.UpgradeDB>();
		upgradeDBFuncS.add(new UpgradeDBOneToTwo());
		upgradeDBFuncS.add(new UpgradeDBTwoToThree());
		upgradeDBFuncS.add(new UpgradeDBThreeToFour());
		upgradeDBFuncS.add(new UpgradeDBFourToFive());
		upgradeDBFuncS.add(new UpgradeDBFiveToSix());

		for (int i = oldVersion - 1; i < newVersion - 1; i++) {
			upgradeDBFuncS.get(i).onUpgradeDB(db);
		}
		upgradeDBFuncS.clear();
	}

	/**
	 * 获取当前打开的数据库的文件名
	 *
	 * @return
	 */
	public String getCurDbName() {
		return mCurDbName;
	}

	static public int getDBVersion() {
		return DB_VERSION;
	}

	/**
	 * 获取数据库默认的名字
	 *
	 * @return
	 */
	static public String getDBName() {
		return DB_NAME;
	}

	public boolean updateRecordFromV11ToV20(BackupPropertiesConfig bpc, String recordDir) {
		if (bpc == null || recordDir == null) {
			return false;
		}

		boolean ret = true;
		String value = null;
		// 更新sms
		ContentValues cv = new ContentValues();
		if (!TextUtils.isEmpty(value = getConfigValue(bpc,
				BackupPropertiesConfig.P_BACKUP_SMS_COUNT))) {
			delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_SMS, null);
			cv.clear();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_SMS);
			cv.put(DataTable.DATA1, SmsRestoreEntry.SMS_RESTORE_FILE_NAME);
			cv.put(DataTable.DATA2, value);
			ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, cv) == -1 ? false : true;
			if (!ret) {
				return ret;
			}
		}

		// 更新calllog
		if (!TextUtils.isEmpty(value = getConfigValue(bpc,
				BackupPropertiesConfig.P_BACKUP_CALLLOG_COUNT))) {
			delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_CALLLOG, null);
			cv.clear();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CALLLOG);
			cv.put(DataTable.DATA1, CallLogBackupEntry.CALLLOG_BACKUP_FILE_NAME);
			cv.put(DataTable.DATA2, value);
			ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, cv) == -1 ? false : true;
			if (!ret) {
				return ret;
			}
		}

		// 更新contact
		if (!TextUtils.isEmpty(value = getConfigValue(bpc,
				BackupPropertiesConfig.P_BACKUP_CONTACTS_COUNT))) {
			delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_CONTACT, null);
			cv.clear();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CONTACT);
			cv.put(DataTable.DATA1, ContactsBackupEntry.BACKUP_CONTACTS_FILE_NAME_ENCRYPT);
			cv.put(DataTable.DATA2, value);
			ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, cv) == -1 ? false : true;
			if (!ret) {
				return ret;
			}
		}

		// 更新应用
		ret = updateAppTableFromV11ToV20();
		if (!ret) {
			return ret;
		}

		// 更新系统桌面数据
		ret = updateLauncherDataFromV11ToV20(recordDir);
		if (!ret) {
			return ret;
		}

		// 更新mms
		// TODO
		if (!TextUtils.isEmpty(getConfigValue(bpc, BackupPropertiesConfig.P_BACKUP_MMS_COUNT))) {
			delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_MMS, null);
			cv.clear();
			File mmsDir = new File(Util.ensureFileSeparator(recordDir)
					+ MmsBackupEntry.MMS_DIR_NAME);
			if (mmsDir.isDirectory()) {
				File[] fileList = mmsDir.listFiles();
				int count = fileList == null ? 0 : fileList.length;
				for (int i = 0; i < count; i++) {
					cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_MMS);
					cv.put(DataTable.DATA1, fileList[i].getName());
					ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, cv) == -1
							? false
							: true;
					if (!ret) {
						return ret;
					}
				}
				// 完成以后，差一条数据库彩信的条数信息，用于获取彩信总体条数
				ContentValues cvPDUCount = new ContentValues();
				cvPDUCount.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_MMS);
				cvPDUCount.put(DataTable.DATA2, count);
				ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, cv) == -1 ? false : true;
				if (!ret) {
					return ret;
				}
			}
		}

		// 更新用户字典
		if (!TextUtils.isEmpty(value = getConfigValue(bpc,
				BackupPropertiesConfig.P_BACKUP_DICTIONAY_WORD_COUNT))) {
			cv.clear();
			delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY, null);
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY);
			cv.put(DataTable.DATA1, UserDictionaryBackupEntry.USER_DICTIONARY_FILE_NAME);
			cv.put(DataTable.DATA2, value);
			ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, cv) == -1 ? false : true;
			if (!ret) {
				return ret;
			}
		}

		// 更新wifi
		if (!TextUtils.isEmpty(value = getConfigValue(bpc,
				BackupPropertiesConfig.P_BACKUP_WIFI_PATH))) {
			delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_WIFI, null);
			cv.clear();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_WIFI);
			cv.put(DataTable.DATA1, WifiBackupEntry.WIFI_BACKUP_NAME);
			cv.put(DataTable.DATA2, value);
			ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, cv) == -1 ? false : true;
			if (!ret) {
				return ret;
			}
		}

		// 更新配置信息
		delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_CONFIG, null);
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CONFIG);
		cv.put(DataTable.DATA1, getConfigValue(bpc, BackupPropertiesConfig.P_SOFTWARE_VERSION_CODE));
		cv.put(DataTable.DATA2, getConfigValue(bpc, BackupPropertiesConfig.P_SOFTWARE_VERSION_NAME));
		cv.put(DataTable.DATA3, getConfigValue(bpc, BackupPropertiesConfig.P_OS_VERSION));
		cv.put(DataTable.DATA4, getConfigValue(bpc, BackupPropertiesConfig.P_DATABASE_VERSION));
		cv.put(DataTable.DATA5, getConfigValue(bpc, BackupPropertiesConfig.P_BACKUP_TIME));
		cv.put(DataTable.DATA6, getConfigValue(bpc, BackupPropertiesConfig.P_ISROOT));
		cv.put(DataTable.DATA7, getConfigValue(bpc, BackupPropertiesConfig.P_BACKUP_APP_ITEM_COUNT));
		cv.put(DataTable.DATA8,
				getConfigValue(bpc, BackupPropertiesConfig.P_BACKUP_SYSTEM_DATA_ITEM_COUNT));
		cv.put(DataTable.DATA9, getConfigValue(bpc, BackupPropertiesConfig.P_BACKUP_SIZE));
		ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, cv) == -1 ? false : true;
		return ret;
	}

	private boolean updateLauncherDataFromV11ToV20(String recordDir) {
		if (recordDir == null) {
			return false;
		}

		Cursor cursor = query(DataTable.TABLE_NAME, null, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA, null, null);
		if (cursor == null) {
			return true;
		}
		String packageNmae = null;
		try {
			if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				return true;
			}
			packageNmae = cursor.getString(cursor.getColumnIndex(DataTable.DATA1));
		} catch (Exception e) {
		} finally {
			cursor.close();
		}

		if (packageNmae == null) {
			return false;
		}

		String packageNameInFile = LauncherDataRestoreEntry.getLauncherDataPackageName(recordDir);
		if (packageNameInFile == null || !packageNameInFile.equals(packageNmae)) {
			return false;
		}

		File launcherDataFile = LauncherDataRestoreEntry.getLauncherDataBackupFile(recordDir);
		if (launcherDataFile == null) {
			return false;
		}

		ContentValues cv = new ContentValues();
		String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA;
		// launcher文件名
		cv.put(DataTable.DATA10, launcherDataFile.getName());

		if (new File(recordDir, LauncherDataBackupEntry.APP_WIDGET_FILE_NAME).exists()) {
			cv.put(DataTable.DATA11, LauncherDataBackupEntry.APP_WIDGET_FILE_NAME);
		}
		if (new File(recordDir, LauncherDataBackupEntry.WALLPAPER_FILE_NAME).exists()) {
			cv.put(DataTable.DATA12, LauncherDataBackupEntry.WALLPAPER_FILE_NAME);
		}
		update(DataTable.TABLE_NAME, cv, where, null);
		cv.clear();
		return true;
	}

	private boolean updateAppTableFromV11ToV20() {
		Cursor cursor = query(AppTable.TABLE_NAME, AppTable.DEFUALT_COLUMNS, null, null, null);
		if (cursor == null) {
			return true;
		}

		List<String> packageNames = new ArrayList<String>();
		try {
			if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				return true;
			}
			do {
				try {
					String packageNmae = cursor.getString(cursor
							.getColumnIndex(AppTable.APP_PACKAGE));
					if (packageNmae != null) {
						packageNames.add(packageNmae);
					}
				} catch (Exception e) {
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		ContentValues cv = new ContentValues();
		for (String packageName : packageNames) {
			String where = AppTable.APP_PACKAGE + "='" + packageName + "'";
			cv.put(AppTable.APP_APK_FILENAME, packageName + ".apk");
			cv.put(AppTable.APP_DATA_FILENAME, packageName + ".tar.gz");
			update(AppTable.TABLE_NAME, cv, where, null);
			cv.clear();
		}
		return true;
	}

	private String getConfigValue(BackupPropertiesConfig bcp, String key) {
		Object value = bcp.get(key);
		if (value != null) {
			return value.toString();
		}
		return "";
	}

	//	public long insertAppEntry(BaseEntry entry) {
	//		if (entry == null) {
	//			return -1;
	//		}
	//
	//		ContentValues cv = null;
	//		if (entry instanceof AppBackupEntry) {
	//			AppBackupEntry appEntry = (AppBackupEntry) entry;
	//			cv = appEntry.buildDbContentValues();
	//		} else if (entry instanceof AppRestoreEntry) {
	//			AppRestoreEntry appEntry = (AppRestoreEntry) entry;
	//			cv = appEntry.buildDbContentValues();
	//		}
	//
	//		if (cv == null) {
	//			return -1;
	//		}
	//		return insert(AppTable.TABLE_NAME, AppTable.APP_PACKAGE, cv);
	//	}
	//
	//	public int updateAppEntry(BaseEntry entry) {
	//		if (entry == null) {
	//			return 0;
	//		}
	//
	//		String packageName = null;
	//		ContentValues cv = null;
	//		if (entry instanceof AppBackupEntry) {
	//			AppBackupEntry appEntry = (AppBackupEntry) entry;
	//			packageName = appEntry.getAppInfo().packageName;
	//			cv = appEntry.buildDbContentValues();
	//		} else if (entry instanceof AppRestoreEntry) {
	//			AppRestoreEntry appEntry = (AppRestoreEntry) entry;
	//			packageName = appEntry.getAppInfo().packageName;
	//			cv = appEntry.buildDbContentValues();
	//		}
	//		if (packageName == null || cv == null) {
	//			return 0;
	//		}
	//		return update(AppTable.TABLE_NAME, cv, AppTable.APP_PACKAGE + "='" + packageName + "'",
	//				null);
	//	}

	public boolean reflashAppTable(ContentValues values) {
		if (values == null) {
			return false;
		}

		if (!values.containsKey(AppTable.APP_PACKAGE)) {
			return false;
		}

		String packageName = values.getAsString(AppTable.APP_PACKAGE);
		if (TextUtils.isEmpty(packageName)) {
			return false;
		}

		if (update(AppTable.TABLE_NAME, values, AppTable.APP_PACKAGE + "=?",
				new String[] { packageName }) == 0) {
			return insert(AppTable.TABLE_NAME, AppTable.APP_PACKAGE, values) > 0;
		}
		return true;
	}

	public boolean reflashDatatable(ContentValues values) {
		if (values == null) {
			return false;
		}

		if (!values.containsKey(DataTable.MIME_TYPE)) {
			return false;
		}

		boolean ret = true;
		final int mimetype = values.getAsInteger(DataTable.MIME_TYPE);
		if (mimetype == MimetypeTable.MIMETYPE_VALUE_MMS) {
			return reflashMms(values);
		}

		if (mimetype == MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA) {
			return reflashLauncherData(values);
		}

		if (mimetype == MimetypeTable.MIMETYPE_VALUE_IMAGE) {
			return reflashImage(values);
		}

		String where = DataTable.MIME_TYPE + "=?";
		if (update(DataTable.TABLE_NAME, values, where, new String[] { String.valueOf(mimetype) }) == 0) {
			ret = insert(DataTable.TABLE_NAME, DataTable.MIME_TYPE, values) > 0;
		}
		return ret;
	}

	private boolean reflashImage(ContentValues values) {
		if (values == null) {
			return false;
		}

		if (!values.containsKey(DataTable.MIME_TYPE)) {
			return false;
		}

		boolean ret = false;

		// 更新相片个数字段
		if (!values.containsKey(DataTable.DATA1)) {
			return false;
		}
		final String data7 = DataTable.DATA7;
		int count = delete(DataTable.TABLE_NAME, DataTable.DATA7 + "=?",
				new String[] { values.getAsString(data7) });
		ret = insert(DataTable.TABLE_NAME, null, values) > 0;

		return ret;
	}
	private boolean reflashLauncherData(ContentValues values) {
		if (values == null) {
			return false;
		}

		if (!values.containsKey(DataTable.MIME_TYPE)) {
			return false;
		}

		boolean ret = false;
		String where = DataTable.MIME_TYPE + "=?";
		List<String> whereArgs = new ArrayList<String>();
		whereArgs.add(String.valueOf(MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA));
		if (values.containsKey(DataTable.DATA1)) {
			ret = true;
			where += " AND " + DataTable.DATA1 + "=?";
			whereArgs.add(values.getAsString(DataTable.DATA1));
			if (update(DataTable.TABLE_NAME, values, where,
					whereArgs.toArray(new String[whereArgs.size()])) == 0) {
				ret = insert(DataTable.TABLE_NAME, DataTable.DATA1, values) > 0;
			}
			return ret;
		}

		return ret;
	}

	private boolean reflashMms(ContentValues values) {
		if (values == null) {
			return false;
		}

		if (!values.containsKey(DataTable.MIME_TYPE)) {
			return false;
		}

		boolean ret = false;
		boolean reflashPdu = values.containsKey(DataTable.DATA1);
		if (reflashPdu) {
			// 更新pdu字段
			ret = insert(DataTable.TABLE_NAME, DataTable.DATA1, values) > 0;
			return ret;
		}

		// 更新彩信个数字段
		if (!values.containsKey(DataTable.DATA2)) {
			return false;
		}

		ret = insert(DataTable.TABLE_NAME, null, values) > 0;
		return ret;
	}

	private synchronized long insert(String table, String nullColumnHack, ContentValues values) {
		if (table == null || values == null) {
			return -1;
		}
		long row = -1;
		try {
			SQLiteDatabase db = getWritableDatabase();
			try {
				row = db.insert(table, nullColumnHack, values);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return row;
	}

	private synchronized int update(String tableName, ContentValues values, String whereClause,
			String[] whereArgs) {
		if (tableName == null) {
			return 0;
		}
		int row = 0;
		try {
			SQLiteDatabase db = null;
			db = getWritableDatabase();
			try {
				row = db.update(tableName, values, whereClause, whereArgs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return row;
	}

	public synchronized int delete(String tableName, String whereClause, String[] whereArgs) {
		if (tableName == null) {
			return 0;
		}

		int row = 0;
		try {
			SQLiteDatabase db = getWritableDatabase();
			try {
				row = db.delete(tableName, whereClause, whereArgs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}

		return row;
	}

	public Cursor query(String table, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (table == null) {
			return null;
		}

		Cursor cursor = null;
		try {
			SQLiteDatabase db = getWritableDatabase();
			try {
				cursor = db.query(table, projection, selection, selectionArgs, null, null,
						sortOrder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public void cleanAllData() {
		/*
		 * SQLiteDatabase db = null; try { db = getWritableDatabase(); if(db !=
		 * null){ db.delete(AppTable.TABLE_NAME, "1", null);
		 * db.delete(DataTable.TABLE_NAME, "1", null); } } catch (Exception e) {
		 * e.printStackTrace(); return; }
		 */
		delete(AppTable.TABLE_NAME, null, null);
		delete(DataTable.TABLE_NAME, null, null);
	}

	/**
	 * 获取某些类型的项的个数 例如短信、联系人等返回具体条数；应用程序返回个数
	 *
	 * @param type
	 * @return
	 */
	public int getEntryCount(BaseEntry.EntryType type) {
		if (type == null) {
			return 0;
		}

		final int mimetype = entryTypeToMimetype(type);
		if (mimetype == 0) {
			return 0;
		}

		Cursor cursor = null;
		if (mimetype == MimetypeTable.MIMETYPE_VALUE_APP) {
			cursor = query(AppTable.TABLE_NAME, new String[] { AppTable._ID }, null, null, null);
		} else {
			cursor = query(DataTable.TABLE_NAME, new String[] { DataTable.DATA2 },
					DataTable.MIME_TYPE + "=" + mimetype, null, null);
		}

		if (cursor == null) {
			return 0;
		}
		int result = 0;
		try {
			if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				return 0;
			}
			try {
				if (mimetype == MimetypeTable.MIMETYPE_VALUE_APP) {
					result = cursor.getCount();
				} else if (mimetype == MimetypeTable.MIMETYPE_VALUE_SMS
						|| mimetype == MimetypeTable.MIMETYPE_VALUE_CONTACT
						|| mimetype == MimetypeTable.MIMETYPE_VALUE_CALLLOG
						|| mimetype == MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY) {
					result = cursor.getInt(0);
				} else if (mimetype == MimetypeTable.MIMETYPE_VALUE_MMS) {
					do {
						result = cursor.getInt(0);
						if (result != 0) {
							break;
						}
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
			}
		} finally {
			cursor.close();
		}
		return result;
	}

	public int getAppEntriesCount() {
		Cursor cursor = query(AppTable.TABLE_NAME, new String[] { AppTable._ID }, null, null, null);
		if (cursor == null) {
			return 0;
		}
		try {
			return cursor.getCount();
		} finally {
			cursor.close();
		}
	}

	public int getSystemEntriesCount() {
		Cursor cursor = query(DataTable.TABLE_NAME, new String[] { DataTable._ID }, null, null,
				null);
		if (cursor == null) {
			return 0;
		}
		try {
			return cursor.getCount();
		} finally {
			cursor.close();
		}
	}

	/**
	 * 判断是否存在某种类型的项，如果是应用程序则需对包名进行判断
	 *
	 * @param entry
	 * @return
	 */
	public boolean hasEntryExisted(BaseEntry entry) {
		if (entry == null) {
			return false;
		}

		boolean ret = false;
		final int mimetype = entryTypeToMimetype(entry.getType());
		if (mimetype == 0) {
			return false;
		}
		Cursor cursor = null;
		if (mimetype == MimetypeTable.MIMETYPE_VALUE_APP && entry instanceof AppRestoreEntry) {
			AppRestoreEntry appEntry = (AppRestoreEntry) entry;
			String packageName = appEntry.getAppInfo().packageName;
			cursor = query(AppTable.TABLE_NAME, new String[] { AppTable._ID }, AppTable.APP_PACKAGE
					+ "='" + packageName + "'", null, null);
		} else {
			cursor = query(DataTable.TABLE_NAME, new String[] { DataTable._ID },
					DataTable.MIME_TYPE + "=" + mimetype, null, null);
		}
		if (cursor == null) {
			return false;
		}
		try {
			ret = cursor.getCount() != 0;
		} finally {
			cursor.close();
		}
		return ret;
	}

	public EntryType convertMimeTypeToEntryType(int mimeType) {
		switch (mimeType) {
			case MimetypeTable.MIMETYPE_VALUE_CALLLOG :
				return EntryType.TYPE_USER_CALL_HISTORY;

			case MimetypeTable.MIMETYPE_VALUE_CONTACT :
				return EntryType.TYPE_USER_CONTACTS;

			case MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING :
				return EntryType.TYPE_USER_GOLAUNCHER_SETTING;

			case MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA :
				return EntryType.TYPE_SYSTEM_LAUNCHER_DATA;

			case MimetypeTable.MIMETYPE_VALUE_MMS :
				return EntryType.TYPE_USER_MMS;

			case MimetypeTable.MIMETYPE_VALUE_SMS :
				return EntryType.TYPE_USER_SMS;

			case MimetypeTable.MIMETYPE_VALUE_RINGTONE :
				return EntryType.TYPE_SYSTEM_RINGTONE;

			case MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY :
				return EntryType.TYPE_USER_DICTIONARY;

			case MimetypeTable.MIMETYPE_VALUE_WALLPAPER :
				return EntryType.TYPE_SYSTEM_WALLPAPER;

			case MimetypeTable.MIMETYPE_VALUE_WIFI :
				return EntryType.TYPE_SYSTEM_WIFI;

			case MimetypeTable.MIMETYPE_VALUE_CALENDAR :
				return EntryType.TYPE_USER_CALENDAR;

			case MimetypeTable.MIMETYPE_VALUE_BOOKMARK :
				return EntryType.TYPE_USER_BOOKMARK;

			case MimetypeTable.MIMETYPE_VALUE_IMAGE :
				return EntryType.TYPE_USER_IMAGE;
			default :
				throw new IllegalArgumentException("invalid mimetype");
		}
	}

	/**
	 * 从数据库中查出所有的备份项信息
	 *
	 * @return
	 */
	public List<BaseBackupEntryInfo> getAllBackupEntriesInfo() {
		List<BaseBackupEntryInfo> allEntryInfo = new ArrayList<BaseBackupEntryInfo>();
		List<BaseBackupEntryInfo> allSystemDataEntryInfo = getAllSystemDataEntriesInfo();
		if (!Util.isCollectionEmpty(allSystemDataEntryInfo)) {
			allEntryInfo.addAll(allSystemDataEntryInfo);
		}
		List<BaseBackupEntryInfo> allAppEntryInfo = getAllAppEntriesInfo();
		if (!Util.isCollectionEmpty(allAppEntryInfo)) {
			allEntryInfo.addAll(allAppEntryInfo);
		}

		List<BaseBackupEntryInfo> allImageEntryInfo = getAllImageEntriesInfo();
		if (!Util.isCollectionEmpty(allImageEntryInfo)) {
			allEntryInfo.addAll(allImageEntryInfo);
		}
		return allEntryInfo;
	}

	public BaseBackupEntryInfo getSystemDataEntryInfo(EntryType type) {
		if (type == null) {
			return null;
		}

		if (type == EntryType.TYPE_SYSTEM_APP || type == EntryType.TYPE_USER_APP) {
			return null;
		}

		int mimetype = entryTypeToMimetype(type);
		Cursor cursor = null;
		String where = DataTable.MIME_TYPE + "=" + mimetype;
		if (type == EntryType.TYPE_USER_MMS) {
			where += " AND " + DataTable.DATA2 + " IS NOT NULL";
		}
		cursor = query(DataTable.TABLE_NAME, null, where, null, null);
		if (cursor == null) {
			return null;
		}
		BaseBackupEntryInfo entryInfo = null;
		try {
			if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				return null;
			}
			entryInfo = buildSystemDataEntryInfoFromCursor(cursor);
		} finally {
			cursor.close();
		}
		return entryInfo;
	}

	public BaseBackupEntryInfo getImageEntryInfo(BaseEntry entry) {
		if (entry == null) {
			return null;
		}
		BaseBackupEntryInfo imageEntryInfo = null;
		try {
			imageEntryInfo = new ImageBackupEntryInfo(((OneImageRestoreEntry) entry).getImage());
			imageEntryInfo.type = EntryType.TYPE_USER_IMAGE;
			imageEntryInfo.backupFileName = new String[1];
//			String rootPath = Util.getDefalutValidSdPath(mContext);
			String imagePath = ((ImageBackupEntryInfo) imageEntryInfo).mImage.mImagePath;
//			String path = ImageBackupEntry.IMAGE_DIR_NAME + imagePath;
			imageEntryInfo.backupFileName[0] = ImageBackupEntry.IMAGE_DIR_NAME + imagePath;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return imageEntryInfo;
	}

	public BaseBackupEntryInfo getAppEntryInfo(String packageName) {
		if (packageName == null) {
			return null;
		}

		Cursor cursor = query(AppTable.TABLE_NAME, null, AppTable.APP_PACKAGE + "='" + packageName
				+ "'", null, null);
		if (cursor == null) {
			return null;
		}
		BaseBackupEntryInfo appEntryInfo = null;
		try {
			if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return null;
			}
			appEntryInfo = buildAppEntryInfoFromCursor(cursor);
		} finally {
			cursor.close();
		}
		return appEntryInfo;
	}

	private BaseBackupEntryInfo buildAppEntryInfoFromCursor(Cursor cursor) {
		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0) {
			return null;
		}

		BaseBackupEntryInfo appEntryInfo = null;

		try {
			AppInfo appInfo = new AppInfo(cursor);
			appEntryInfo = new AppBackupEntryInfo(appInfo);
			appEntryInfo.type = EntryType.TYPE_USER_APP;

			String apkFileName = cursor.getString(cursor.getColumnIndex(AppTable.APP_APK_FILENAME));
			String dataFileName = cursor.getString(cursor
					.getColumnIndex(AppTable.APP_DATA_FILENAME));

			List<String> pathList = new ArrayList<String>();
			if (!TextUtils.isEmpty(apkFileName)) {
				pathList.add(apkFileName);
			}
			if (!TextUtils.isEmpty(dataFileName)) {
				pathList.add(dataFileName);
			}
			if (pathList.size() > 0) {
				appEntryInfo.backupFileName = pathList.toArray(new String[pathList.size()]);
			}
			appEntryInfo.count = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appEntryInfo;
	}

	private BaseBackupEntryInfo buildImageEntryInfoFromCursor(Cursor cursor) {
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() == 0) {
			return null;
		}
		BaseBackupEntryInfo imageEntryInfo = null;
		try {
			int mimeType = cursor.getInt(cursor.getColumnIndex(DataTable.MIME_TYPE));
			if (mimeType == MimetypeTable.MIMETYPE_VALUE_CONFIG) {
				return null;
			}
			imageEntryInfo = new BaseBackupEntryInfo();
			imageEntryInfo.type = EntryType.TYPE_USER_IMAGE;
			imageEntryInfo.backupFileName = new String[1];
			imageEntryInfo.backupFileName[0] = ImageBackupEntry.IMAGE_DIR_NAME;
			imageEntryInfo.count = cursor.getCount();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return imageEntryInfo;

	}

	private BaseBackupEntryInfo buildSystemDataEntryInfoFromCursor(Cursor cursor) {
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() == 0) {
			return null;
		}

		BaseBackupEntryInfo dataEntryInfo = null;
		try {
			int mimeType = cursor.getInt(cursor.getColumnIndex(DataTable.MIME_TYPE));
			if (mimeType == MimetypeTable.MIMETYPE_VALUE_CONFIG) {
				return null;
			}

			if (mimeType == MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA) {
				dataEntryInfo = new LauncherDataEntryInfo();
			} else {
				dataEntryInfo = new BaseBackupEntryInfo();
			}

			dataEntryInfo.type = convertMimeTypeToEntryType(mimeType);
			long dateInMills = cursor.getLong(cursor.getColumnIndex(DataTable.DATA14));
			dataEntryInfo.backupDate = new Date(dateInMills);

			if (mimeType == MimetypeTable.MIMETYPE_VALUE_CALLLOG
					|| mimeType == MimetypeTable.MIMETYPE_VALUE_SMS
					|| mimeType == MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY
					|| mimeType == MimetypeTable.MIMETYPE_VALUE_CALENDAR
					|| mimeType == MimetypeTable.MIMETYPE_VALUE_BOOKMARK) {
				dataEntryInfo.backupFileName = new String[1];
				dataEntryInfo.backupFileName[0] = cursor.getString(cursor
						.getColumnIndex(DataTable.DATA1));
				dataEntryInfo.count = cursor.getInt(cursor.getColumnIndex(DataTable.DATA2));
			} else if (mimeType == MimetypeTable.MIMETYPE_VALUE_MMS) {
				// TODO 彩信的数据库表结构设计有问题，需特殊处理
				if (cursor.isNull(cursor.getColumnIndex(DataTable.DATA2))) {
					return null;
				}
				dataEntryInfo.backupFileName = new String[1];
				dataEntryInfo.backupFileName[0] = MmsBackupEntry.MMS_DIR_NAME;
				dataEntryInfo.count = cursor.getInt(cursor.getColumnIndex(DataTable.DATA2));
			} else if (mimeType == MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING) {
				String propFileName = cursor.getString(cursor.getColumnIndex(DataTable.DATA1));
				String dbFileName = cursor.getString(cursor.getColumnIndex(DataTable.DATA2));
				dataEntryInfo.backupFileName = new String[2];
				dataEntryInfo.backupFileName[0] = propFileName;
				dataEntryInfo.backupFileName[1] = dbFileName;
				dataEntryInfo.count = 1;
			} else if (mimeType == MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA) {
				String launcherExtraFileName = cursor.getString(cursor
						.getColumnIndex(DataTable.DATA10));
				String appWidgetsFileName = cursor.getString(cursor
						.getColumnIndex(DataTable.DATA11));
				String dataFileName = cursor.getString(cursor.getColumnIndex(DataTable.DATA13));
				dataEntryInfo.backupFileName = new String[] { launcherExtraFileName,
						appWidgetsFileName, dataFileName };
				dataEntryInfo.count = 1;
				LauncherDataExtraInfo launcherDataExtraInfo = new LauncherDataExtraInfo(cursor);
				((LauncherDataEntryInfo) dataEntryInfo).launcherDataExtraInfo = launcherDataExtraInfo;
			} else if (mimeType == MimetypeTable.MIMETYPE_VALUE_RINGTONE) {
				List<String> allFileNames = new ArrayList<String>();
				int firstFileName = cursor.getColumnIndex(DataTable.DATA2);
				int lastFileName = cursor.getColumnIndex(DataTable.DATA6);
				for (int i = firstFileName; i <= lastFileName; i++) {
					String fileName = cursor.getString(i);
					if (!TextUtils.isEmpty(fileName) && !allFileNames.contains(fileName)) {
						allFileNames.add(fileName);
					}
				}
				final int count = allFileNames.size();
				dataEntryInfo.backupFileName = allFileNames.toArray(new String[count]);
				dataEntryInfo.count = count;
			} else if (mimeType == MimetypeTable.MIMETYPE_VALUE_WALLPAPER
					|| mimeType == MimetypeTable.MIMETYPE_VALUE_WIFI) {
				dataEntryInfo.backupFileName = new String[1];
				dataEntryInfo.backupFileName[0] = cursor.getString(cursor
						.getColumnIndex(DataTable.DATA1));
				dataEntryInfo.count = 1;
			} else if (mimeType == MimetypeTable.MIMETYPE_VALUE_CONTACT) {
				List<String> backupFileList = new ArrayList<String>();
				String dataFile = cursor.getString(cursor.getColumnIndex(DataTable.DATA1));
				String photoDir = cursor.getString(cursor.getColumnIndex(DataTable.DATA3));
				if (!TextUtils.isEmpty(dataFile)) {
					backupFileList.add(dataFile);
				}
				if (!TextUtils.isEmpty(photoDir)) {
					backupFileList.add(photoDir);
				}
				if (backupFileList.size() > 0) {
					dataEntryInfo.backupFileName = backupFileList.toArray(new String[backupFileList
							.size()]);
				}
				dataEntryInfo.count = cursor.getInt(cursor.getColumnIndex(DataTable.DATA2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataEntryInfo;
	}

	/**
	 * 获取所有系统数据备份项的基本信息
	 *
	 * @return
	 */
	public List<BaseBackupEntryInfo> getAllSystemDataEntriesInfo() {
		Cursor cursor = null;
		cursor = query(DataTable.TABLE_NAME, null, DataTable.MIME_TYPE + "!="
				+ MimetypeTable.MIMETYPE_VALUE_CONFIG, null, null);
		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		List<BaseBackupEntryInfo> allSystemDataEntryInfo = new ArrayList<BaseBackupEntryInfo>();
		try {
			do {
				BaseBackupEntryInfo entryInfo = buildSystemDataEntryInfoFromCursor(cursor);
				if (entryInfo != null && !allSystemDataEntryInfo.contains(entryInfo)) {
					allSystemDataEntryInfo.add(entryInfo);
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		return allSystemDataEntryInfo;
	}

	public List<BaseBackupEntryInfo> getAllImageEntriesInfo() {
		Cursor cursor = null;
		cursor = query(DataTable.TABLE_NAME, null, DataTable.MIME_TYPE + "= '"
				+ MimetypeTable.MIMETYPE_IMAGE + "'", null, null);
		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		List<BaseBackupEntryInfo> allImageEntryInfo = new ArrayList<BaseBackupEntryInfo>();
		try {

			BaseBackupEntryInfo entryInfo = buildImageEntryInfoFromCursor(cursor);
			if (entryInfo != null) {
				allImageEntryInfo.add(entryInfo);
			}

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return allImageEntryInfo;
	}

	public List<BaseBackupEntryInfo> getAllAppEntriesInfo() {
		Cursor cursor = query(AppTable.TABLE_NAME, null, null, null, null);
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		List<BaseBackupEntryInfo> allAppEntryInfo = new ArrayList<BaseBackupEntryInfo>();
		try {
			do {
				BaseBackupEntryInfo appEntryInfo = buildAppEntryInfoFromCursor(cursor);
				if (appEntryInfo != null && !allAppEntryInfo.contains(appEntryInfo)) {
					allAppEntryInfo.add(appEntryInfo);
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		return allAppEntryInfo;
	}

	/**
	 * 获取备份项的备份时间
	 *
	 * @param entry
	 * @return
	 */
	public Date getEntryBackupDate(BaseEntry entry) {
		if (entry == null) {
			return null;
		}

		// 目前app不支持获取备份时间
		if (entry.getType() == EntryType.TYPE_SYSTEM_APP
				|| entry.getType() == EntryType.TYPE_USER_APP) {
			return null;
		}

		final int mimetype = entryTypeToMimetype(entry.getType());
		Cursor cursor = query(DataTable.TABLE_NAME, null, DataTable.MIME_TYPE + "=" + mimetype
				+ " AND " + DataTable.DATA14 + " IS NOT NULL", null, null);
		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		Date date = null;
		try {
			long time = cursor.getLong(cursor.getColumnIndex(DataTable.DATA14));
			if (time > 0) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(time);
				date = calendar.getTime();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}

		return date;
	}

	public byte[] getAppIconRawData(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		Cursor cursor = query(AppTable.TABLE_NAME, new String[] { AppTable.APP_ICON },
				AppTable.APP_PACKAGE + "='" + packageName + "'", null, null);
		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		try {
			byte[] data = cursor.getBlob(0);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return null;
	}

	private int entryTypeToMimetype(EntryType type) {
		int mimetype = 0;
		switch (type) {
			case TYPE_SYSTEM_APP :
			case TYPE_USER_APP :
				mimetype = MimetypeTable.MIMETYPE_VALUE_APP;
				break;

			case TYPE_SYSTEM_LAUNCHER_DATA :
				mimetype = MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA;
				break;

			case TYPE_SYSTEM_WIFI :
				mimetype = MimetypeTable.MIMETYPE_VALUE_WIFI;
				break;

			case TYPE_USER_CALL_HISTORY :
				mimetype = MimetypeTable.MIMETYPE_VALUE_CALLLOG;
				break;

			case TYPE_USER_CONTACTS :
				mimetype = MimetypeTable.MIMETYPE_VALUE_CONTACT;
				break;

			case TYPE_USER_DICTIONARY :
				mimetype = MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY;
				break;

			case TYPE_USER_GOLAUNCHER_SETTING :
				mimetype = MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING;
				break;

			case TYPE_USER_MMS :
				mimetype = MimetypeTable.MIMETYPE_VALUE_MMS;
				break;

			case TYPE_USER_SMS :
				mimetype = MimetypeTable.MIMETYPE_VALUE_SMS;
				break;

			case TYPE_SYSTEM_WALLPAPER :
				mimetype = MimetypeTable.MIMETYPE_VALUE_WALLPAPER;
				break;

			case TYPE_SYSTEM_RINGTONE :
				mimetype = MimetypeTable.MIMETYPE_VALUE_RINGTONE;
				break;

			case TYPE_USER_CALENDAR :
				mimetype = MimetypeTable.MIMETYPE_VALUE_CALENDAR;
				break;

			case TYPE_USER_BOOKMARK :
				mimetype = MimetypeTable.MIMETYPE_VALUE_BOOKMARK;
				break;

			case TYPE_USER_IMAGE :
				mimetype = MimetypeTable.MIMETYPE_VALUE_IMAGE;
				break;

			default :
				mimetype = 0;
		}
		return mimetype;
	}

	public boolean isClosed() {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return !db.isOpen();
	}

	/**
	 * @author maiyongshen
	 */
	abstract class UpgradeDB {
		abstract boolean onUpgradeDB(SQLiteDatabase db);
	}

	/**
	 * @author maiyongshen
	 */
	class UpgradeDBOneToTwo extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return onUpgrade1To2(db);
		}

		private boolean onUpgrade1To2(SQLiteDatabase db) {
			updateMimetypeTable(db);
			updateDataTable(db);
			return true;
		}

		private void updateMimetypeTable(SQLiteDatabase db) throws SQLException {
			String createSql = "CREATE TABLE " + MimetypeTable.TABLENAME + " (" + MimetypeTable._ID
					+ " INTEGER PRIMARY KEY," + MimetypeTable.MIMETYPE + " TEXT NOT NULL,"
					+ MimetypeTable.MIMETYPE_VALUE + " INTEGER NOT NULL" + ")";
			db.execSQL(createSql);

			String insertSql = "INSERT INTO " + MimetypeTable.TABLENAME + " ( "
					+ MimetypeTable.MIMETYPE + ", " + MimetypeTable.MIMETYPE_VALUE + " ) "
					+ "VALUES ( '" + MimetypeTable.MIMETYPE_LAUCHER_DATA + "', "
					+ MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA + " ) ";
			db.execSQL(insertSql);
		}

		private void updateDataTable(SQLiteDatabase db) throws SQLException {
			String createSql = "CREATE TABLE " + DataTable.TABLE_NAME + " (" + DataTable._ID
					+ " INTEGER PRIMARY KEY," + DataTable.MIME_TYPE + " TEXT," + DataTable.DATA1
					+ " TEXT," + DataTable.DATA2 + " TEXT," + DataTable.DATA3 + " TEXT,"
					+ DataTable.DATA4 + " TEXT," + DataTable.DATA5 + " TEXT," + DataTable.DATA6
					+ " TEXT," + DataTable.DATA7 + " TEXT," + DataTable.DATA8 + " TEXT,"
					+ DataTable.DATA9 + " TEXT" + ") ";
			db.execSQL(createSql);
		}
	}

	/**
	 * @author maiyongshen
	 */
	class UpgradeDBTwoToThree extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return onUpdate2To3(db);
		}

		private boolean onUpdate2To3(SQLiteDatabase db) {
			//			try {
			//				db.beginTransaction();
			// 更新MimetypeTable表
			updateMimetypeTable2To3(db);

			// 修改DataTable表
			updateDataTable2To3(db);

			// 修改AppTable表
			updateAppTable2To3(db);
			//				db.setTransactionSuccessful();
			//			} catch (SQLException e) {
			//				e.printStackTrace();
			//				return false;
			//			} finally {
			//				db.endTransaction();
			//			}
			return true;
		}

		private void updateMimetypeTable2To3(SQLiteDatabase db) {
			SQLiteStatement ss = null;
			try {
				ss = db.compileStatement("INSERT INTO " + MimetypeTable.TABLENAME + "("
						+ MimetypeTable.MIMETYPE + "," + MimetypeTable.MIMETYPE_VALUE
						+ ") VALUES (?,?)");
				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_SMS,
						MimetypeTable.MIMETYPE_VALUE_SMS);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_MMS,
						MimetypeTable.MIMETYPE_VALUE_MMS);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_RINGTONE,
						MimetypeTable.MIMETYPE_VALUE_RINGTONE);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_CONTACT,
						MimetypeTable.MIMETYPE_VALUE_CONTACT);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_CALLLOG,
						MimetypeTable.MIMETYPE_VALUE_CALLLOG);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_WIFI,
						MimetypeTable.MIMETYPE_VALUE_WIFI);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_GOLAUNCHER_SETTING,
						MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_APP,
						MimetypeTable.MIMETYPE_VALUE_APP);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_CONFIG,
						MimetypeTable.MIMETYPE_VALUE_CONFIG);
				ss.executeInsert();

				buildMimetypeStatement(ss, MimetypeTable.MIMETYPE_USER_DICTIONARY,
						MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY);
				ss.executeInsert();
			} finally {
				if (ss != null) {
					ss.close();
				}
			}
		}

		private void updateDataTable2To3(SQLiteDatabase db) {
			String sql = "ALTER TABLE " + DataTable.TABLE_NAME + " ADD " + DataTable.DATA10
					+ " TEXT";
			db.execSQL(sql);

			sql = "ALTER TABLE " + DataTable.TABLE_NAME + " ADD " + DataTable.DATA11 + " TEXT";
			db.execSQL(sql);

			sql = "ALTER TABLE " + DataTable.TABLE_NAME + " ADD " + DataTable.DATA12 + " TEXT";
			db.execSQL(sql);

			sql = "ALTER TABLE " + DataTable.TABLE_NAME + " ADD " + DataTable.DATA13 + " TEXT";
			db.execSQL(sql);

			sql = "ALTER TABLE " + DataTable.TABLE_NAME + " ADD " + DataTable.DATA14 + " TEXT";
			db.execSQL(sql);

			sql = "ALTER TABLE " + DataTable.TABLE_NAME + " ADD " + DataTable.DATA15 + " TEXT";
			db.execSQL(sql);
		}

		private void updateAppTable2To3(SQLiteDatabase db) {
			String sql = "ALTER TABLE " + AppTable.TABLE_NAME + " ADD " + AppTable.APP_APK_FILENAME
					+ " TEXT";
			db.execSQL(sql);

			sql = "ALTER TABLE " + AppTable.TABLE_NAME + " ADD " + AppTable.APP_DATA_FILENAME
					+ " TEXT";
			db.execSQL(sql);
		}
	}

	/**
	 * @author maiyongshen
	 */
	class UpgradeDBThreeToFour extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return updateMimetypeTable3To4(db);
		}

		private boolean updateMimetypeTable3To4(SQLiteDatabase db) {
			SQLiteStatement ss = null;
			try {
				ss = db.compileStatement("INSERT INTO " + MimetypeTable.TABLENAME + "("
						+ MimetypeTable.MIMETYPE + "," + MimetypeTable.MIMETYPE_VALUE
						+ ") VALUES (?,?)");

				ss.bindString(1, MimetypeTable.MIMETYPE_WALLPAPER);
				ss.bindLong(2, MimetypeTable.MIMETYPE_VALUE_WALLPAPER);
				ss.executeInsert();

				ss.clearBindings();
				ss.bindString(1, MimetypeTable.MIMETYPE_RINGTONE);
				ss.bindLong(2, MimetypeTable.MIMETYPE_VALUE_RINGTONE);
				ss.executeInsert();
			} finally {
				if (ss != null) {
					ss.close();
				}
			}
			return true;
		}
	}

	/**
	 * @author wencan
	 */
	class UpgradeDBFourToFive extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return updateMimetypeTable4To5(db);
		}

		private boolean updateMimetypeTable4To5(SQLiteDatabase db) {
			SQLiteStatement ss = null;
			try {
				ss = db.compileStatement("INSERT INTO " + MimetypeTable.TABLENAME + "("
						+ MimetypeTable.MIMETYPE + "," + MimetypeTable.MIMETYPE_VALUE
						+ ") VALUES (?,?)");
				ss.bindString(1, MimetypeTable.MIMETYPE_CALENDAR);
				ss.bindLong(2, MimetypeTable.MIMETYPE_VALUE_CALENDAR);
				ss.executeInsert();

				ss.clearBindings();
				ss.bindString(1, MimetypeTable.MIMETYPE_BOOKMARK);
				ss.bindLong(2, MimetypeTable.MIMETYPE_VALUE_BOOKMARK);
				ss.executeInsert();
			} finally {
				if (ss != null) {
					ss.close();
				}
			}
			return true;
		}
	}

	/**
	 * @author wencan
	 */
	class UpgradeDBFiveToSix extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return updateMimetypeTable5To6(db);
		}

		private boolean updateMimetypeTable5To6(SQLiteDatabase db) {
			SQLiteStatement ss = null;
			try {
				ss = db.compileStatement("ALTER TABLE " + AppTable.TABLE_NAME + " ADD "
						+ AppTable.APK_MD5 + " TEXT");
				ss.execute();
			} finally {
				if (ss != null) {
					ss.close();
				}
			}
			return true;
		}
	}

	/**
	 * @author maiyongshen
	 */
	public interface AppTable extends BaseColumns {
		/**
		 * APP表名
		 */
		public static String TABLE_NAME = "app_table";

		/**
		 * APP名字
		 */
		public static String APP_NAME = "app_name";
		/**
		 * APP包名
		 */
		public static String APP_PACKAGE = "app_package";
		/**
		 * APP安装包路径
		 */
		public static String APP_PATH = "app_path";
		/**
		 * APP Data路径
		 */
		public static String APP_DATA_PATH = "app_data_path";
		/**
		 * APP图标
		 */
		public static String APP_ICON = "app_icon";
		/**
		 * APP版本号
		 */
		public static String APP_VERSION_CODE = "app_version_code";
		/**
		 * APP版本名
		 */
		public static String APP_VERSION_NAME = "app_version_name";
		/**
		 * APP安装包大小
		 */
		public static String APP_CODE_SIZE = "app_code_size";
		/**
		 * APP缓存大小
		 */
		public static String APP_CACHE_SIZE = "app_cache_size";
		/**
		 * APP数据大小
		 */
		public static String APP_DATA_SIZE = "app_data_size";
		/**
		 * APP类型
		 */
		public static String APP_TYPE = "app_type";

		/**
		 * APP APK文件名 (version 3 新增字段)
		 */
		public static String APP_APK_FILENAME = "app_apk_file";
		/**
		 * APP 数据文件名 (version 3 新增字段)
		 */
		public static String APP_DATA_FILENAME = "app_data_file";

		/**
		 * APP MD5（version6 新增字段）
		 */
		public static String APK_MD5 = "apk_md5";

		/**
		 * 创建表语句
		 */
		public static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID
				+ " INTEGER PRIMARY KEY," + APP_NAME + " TEXT," + APP_PACKAGE + " TEXT," + APP_PATH
				+ " TEXT," + APP_DATA_PATH + " TEST," + APP_ICON + " BLOB," + APP_VERSION_CODE
				+ " INTEGER," + APP_VERSION_NAME + " TEXT," + APP_CODE_SIZE + " LONG,"
				+ APP_CACHE_SIZE + " LONG," + APP_DATA_SIZE + " LONG," + APP_TYPE + " INTEGER,"
				+ APP_APK_FILENAME + " TEXT," + APP_DATA_FILENAME + " TEXT," + APK_MD5 + " TEXT"
				+ ")";

		// 不包含图标数据
		public static String[] DEFUALT_COLUMNS = new String[] { _ID, APP_NAME, APP_PACKAGE,
				APP_PATH, APP_DATA_PATH, APP_VERSION_CODE, APP_VERSION_NAME, APP_CODE_SIZE,
				APP_CACHE_SIZE, APP_DATA_SIZE, APP_TYPE, APP_APK_FILENAME, APP_DATA_FILENAME,
				APK_MD5 };
	}

	/**
	 * data表 （version 2新增表）
	 */
	public interface DataTable extends BaseColumns {
		public static String TABLE_NAME = "data";
		public static String MIME_TYPE = "mimetype";
		public static String DATA1 = "date1";
		public static String DATA2 = "date2";
		public static String DATA3 = "date3";
		public static String DATA4 = "data4";
		public static String DATA5 = "date5";
		public static String DATA6 = "date6";
		public static String DATA7 = "date7";
		public static String DATA8 = "date8";
		public static String DATA9 = "date9";
		public static String DATA10 = "date10"; // (version 3 新增字段)
		public static String DATA11 = "date11"; // (version 3 新增字段)
		public static String DATA12 = "date12"; // (version 3 新增字段)
		public static String DATA13 = "date13"; // (version 3 新增字段)
		public static String DATA14 = "date14"; // (version 3 新增字段)
		public static String DATA15 = "date15"; // (version 3 新增字段)

		public static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID
				+ " INTEGER PRIMARY KEY," + MIME_TYPE + " INTEGER," + DATA1 + " TEXT," + DATA2
				+ " TEXT," + DATA3 + " TEXT," + DATA4 + " TEXT," + DATA5 + " TEXT," + DATA6
				+ " TEXT," + DATA7 + " TEXT," + DATA8 + " TEXT," + DATA9 + " TEXT," + DATA10
				+ " TEXT," + DATA11 + " TEXT," + DATA12 + " TEXT," + DATA13 + " TEXT," + DATA14
				+ " TEXT," + DATA15 + " TEXT" + ") ";
	}

	/**
	 * MIMETYPE表 (version 2 新增表)
	 */
	public interface MimetypeTable extends BaseColumns {
		/**
		 * 表名
		 */
		public static final String TABLENAME = "mimetypes";

		/**
		 * 表字段：MIMETYPE
		 */
		public static final String MIMETYPE = "mimetype";

		/**
		 * 表字段：MIMETYPE_VALUE
		 */
		public static final String MIMETYPE_VALUE = "value";

		public static final String MIMETYPE_BASE = "com.jiubang.go.backup.pro.item/";
		public static final String MIMETYPE_LAUCHER_DATA = MIMETYPE_BASE + "launcher";
		public static final String MIMETYPE_SMS = MIMETYPE_BASE + "sms"; // (version
																			// 3
																			// 新增字段)
		public static final String MIMETYPE_MMS = MIMETYPE_BASE + "mms"; // (version
																			// 3
																			// 新增字段)
		public static final String MIMETYPE_CALLLOG = MIMETYPE_BASE + "calllog"; // (version
																					// 3
																					// 新增字段)
		public static final String MIMETYPE_WIFI = MIMETYPE_BASE + "wifi"; // (version
																			// 3
																			// 新增字段)
		public static final String MIMETYPE_CONTACT = MIMETYPE_BASE + "contact"; // (version
																					// 3
																					// 新增字段)
		public static final String MIMETYPE_GOLAUNCHER_SETTING = MIMETYPE_BASE + "golauncher"; // (version
		// 3 新增字段)
		public static final String MIMETYPE_APP = MIMETYPE_BASE + "app"; // (version
																			// 3
																			// 新增字段)
		public static final String MIMETYPE_USER_DICTIONARY = MIMETYPE_BASE + "userDictionary"; // (version
		// 3 新增字段)
		public static final String MIMETYPE_CONFIG = MIMETYPE_BASE + "config"; // (version
																				// 3
																				// 新增字段)
		public static final String MIMETYPE_WALLPAPER = MIMETYPE_BASE + "wallpaper"; // (version
		// 4新增字段）
		public static final String MIMETYPE_RINGTONE = MIMETYPE_BASE + "ringtone"; // (version
		// 4新增字段)

		public static final String MIMETYPE_CALENDAR = MIMETYPE_BASE + "calendar"; // version 5 新增字段
		public static final String MIMETYPE_BOOKMARK = MIMETYPE_BASE + "bookmark"; // version 5新增字段
		public static final String MIMETYPE_IMAGE = MIMETYPE_BASE + "image"; // version 5新增字段

		public static final int MIMETYPE_VALUE_LAUCHER_DATA = 1;
		public static final int MIMETYPE_VALUE_SMS = 2; // (version 3 新增字段)
		public static final int MIMETYPE_VALUE_MMS = 3; // (version 3 新增字段)
		public static final int MIMETYPE_VALUE_CONTACT = 4; // (version 3 新增字段)
		public static final int MIMETYPE_VALUE_CALLLOG = 5; // (version 3 新增字段)
		public static final int MIMETYPE_VALUE_WIFI = 6; // (version 3 新增字段)
		public static final int MIMETYPE_VALUE_GOLAUNCHER_SETTING = 7; // (version
																		// 3
																		// 新增字段)
		public static final int MIMETYPE_VALUE_APP = 8; // (version 3 新增字段)
		public static final int MIMETYPE_VALUE_CONFIG = 9; // (version 3 新增字段)
		public static final int MIMETYPE_VALUE_USER_DICTIONARY = 10; // (version
																		// 3
																		// 新增字段)
		public static final int MIMETYPE_VALUE_WALLPAPER = 11; // (version
																// 4新增字段)
		public static final int MIMETYPE_VALUE_RINGTONE = 12; // (version 4新增字段）
		public static final int MIMETYPE_VALUE_CALENDAR = 13; // version 5新增字段
		public static final int MIMETYPE_VALUE_BOOKMARK = 14; // version 5新增字段
		public static final int MIMETYPE_VALUE_IMAGE = 15;
		public static final String CREATE_TABLE = "CREATE TABLE " + TABLENAME + " (" + _ID
				+ " INTEGER PRIMARY KEY," + MIMETYPE + " TEXT NOT NULL," + MIMETYPE_VALUE
				+ " INTEGER NOT NULL" + ")";
	}

	/**
	 * 数据库内备份项的公共基础信息，一些特殊的备份项信息可从这里继承扩展
	 *
	 * @author maiyongshen
	 */
	public static class BaseBackupEntryInfo {
		public EntryType type;
		public String[] backupFileName;
		public int count;
		public Date backupDate;

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof BaseBackupEntryInfo)) {
				return false;
			}

			return type == ((BaseBackupEntryInfo) o).type;
		}
	}

	/**
	 * @author maiyongshen
	 */
	public static class LauncherDataEntryInfo extends BaseBackupEntryInfo {
		public LauncherDataExtraInfo launcherDataExtraInfo;

		public LauncherDataEntryInfo() {

		}

		public LauncherDataEntryInfo(LauncherDataExtraInfo launcherDataExtraInfo) {
			this.launcherDataExtraInfo = launcherDataExtraInfo;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof LauncherDataEntryInfo)) {
				return false;
			}
			boolean isEqual = super.equals(o);
			if (isEqual) {
				isEqual = launcherDataExtraInfo.packageName
						.equals(((LauncherDataEntryInfo) o).launcherDataExtraInfo.packageName);
			}
			return isEqual;
		}
	}

	/**
	 * @author maiyongshen
	 */
	public static class AppBackupEntryInfo extends BaseBackupEntryInfo {
		public AppInfo appInfo;

		public AppBackupEntryInfo(AppInfo appInfo) {
			this.appInfo = appInfo;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof AppBackupEntryInfo)) {
				return false;
			}
			boolean isEqual = super.equals(o);
			if (isEqual) {
				isEqual = appInfo.packageName.equals(((AppBackupEntryInfo) o).appInfo.packageName);
			}
			return isEqual;
		}
	}

	/**
	 * @author jiangpeihe
	 */
	public static class ImageBackupEntryInfo extends BaseBackupEntryInfo {
		public ImageBean mImage;

		public ImageBackupEntryInfo(ImageBean image) {
			mImage = image;
		}
	}
}
