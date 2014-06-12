package com.jiubang.ggheart.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jiubang.ggheart.data.tables.AppClassifyTable;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;


/**
 * 只提供只读信息数据库
 * 
 * @author wuziyi
 * 
 */
public class AppClassifyDatabaseHelper extends SQLiteOpenHelper {

	private final static int DB_VERSION_MAX = 2;
	private final static String DATABASE_NAME = "appclassify.db";
	private final static String ASSETS_NAME = "cities.db";

	private final Context mContext;

	// 数据库文件目标存放路径为系统默认位置，com.gau.go.launcherex 是你的包名
	private String mDbPath;
	/*
	 * //如果你想把数据库文件存放在SD卡的话 private static String DB_PATH =
	 * android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
	 * "/arthurcn/drivertest/packfiles/";
	 */

	/**
	 * 如果数据库文件较大，使用FileSplit分割为小于1M的小文件 此例中分割为 hello.db.101 hello.db.102
	 * hello.db.103
	 */
	// 第一个文件名后缀
	private static final int ASSETS_SUFFIX_BEGIN = 101;
	// 最后一个文件名后缀
	private static final int ASSETS_SUFFIX_END = 103;

	/**
	 * 在SQLiteOpenHelper的子类当中，必须有该构造函数
	 * 
	 * @param context
	 *            上下文对象
	 * @param name
	 *            数据库名称
	 * @param factory
	 *            一般都是null
	 * @param version
	 *            当前数据库的版本，值必须是整数并且是递增的状态
	 */
	public AppClassifyDatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		// 必须通过super调用父类当中的构造函数
		super(context, name, null, version);
		this.mContext = context;
		mDbPath = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
		try {
			createDataBase();
		} catch (Throwable e) {
			e.printStackTrace();
			// 如果出问题导致复制失败，检查下文件是否还在，存在的话删除，下次再尝试导入
			File dir = new File(mDbPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File dbf = new File(mDbPath + DATABASE_NAME);
			if (dbf.exists()) {
				dbf.delete();
			}
//			throw new Error("Copy cities.db from ASSETS to appclassify.db failed");
		}
	}

	public AppClassifyDatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public AppClassifyDatabaseHelper(Context context, String name) {
		this(context, name, DB_VERSION_MAX);
	}

	public AppClassifyDatabaseHelper(Context context) {
		this(context, DATABASE_NAME);
	}

	public void createDataBase() throws Exception {
		SQLiteDatabase dbExist = checkDataBase();
		if (dbExist != null) {
			int oldDBVer = dbExist.getVersion();
			boolean isExistTable = isExistTable(AppClassifyTable.TABLE_NAME, dbExist);
			dbExist.close();
			if (oldDBVer < DB_VERSION_MAX || !isExistTable) {
				// 创建数据库
				coverDBFromAssets();
			}
			// 数据库已存在,并且不用升级，do nothing.
		} else {
			coverDBFromAssets();
		}
	}

	private void coverDBFromAssets() throws IOException {
		SQLiteDatabase db = null;
		try {
			File dir = new File(mDbPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File dbf = new File(mDbPath + DATABASE_NAME);
			if (dbf.exists()) {
				dbf.delete();
			}
			db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
			if (db != null) {
				db.close();
				db = null;
			}
			// 复制asseets中的db文件到DB_PATH下
			copyDataBase();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	// 检查数据库是否有效
	private SQLiteDatabase checkDataBase() {
		SQLiteDatabase checkDB = null;
		String dbFilePath = mDbPath + DATABASE_NAME;
		try {
			checkDB = SQLiteDatabase.openDatabase(dbFilePath, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does't exist yet.
		} 
//		return checkDB != null ? true : false;
		return checkDB;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			// Open your local db as the input stream
			input = mContext.getAssets().open(ASSETS_NAME);
			// Path to the just created empty db
			String outFileName = mDbPath + DATABASE_NAME;
			// Open the empty db as the output stream
			output = new FileOutputStream(outFileName);
			// transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
		} finally {
			// Close the streams
			if (output != null) {
				output.flush();
				output.close();
			}
			if (input != null) {
				input.close();
			}
		}
	}

	// 复制assets下的大数据库文件时用这个
	private void copyBigDataBase() throws IOException {
		InputStream input;
		String outFileName = mDbPath + DATABASE_NAME;
		OutputStream output = new FileOutputStream(outFileName);
		try {
			for (int i = ASSETS_SUFFIX_BEGIN; i < ASSETS_SUFFIX_END + 1; i++) {
				input = mContext.getAssets().open(ASSETS_NAME + "." + i);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
				output.flush();
				input.close();
			}
		} finally {
			output.close();
		}
	}

	@Override
	public synchronized void close() {
		super.close();
	}

	/**
	 * 该函数是在第一次创建的时候执行， 实际上是第一次得到SQLiteDatabase对象的时候才会调用这个方法
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	/**
	 * 数据库表结构有变化时采用
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

//	public AppClassifyDatabaseHelper(Context context) {
//		super(context, DATABASE_NAME, null, DB_VERSION_MAX);
//
//		mContext = context;
//		mContext.getPackageName();
//		SQLiteDatabase db = null;
//		try {
//			db = getWritableDatabase();
//			if (!mUpdateResult) {
//				// 更新失败，则删除数据库，再行创建。
//				if (db != null) {
//					db.close();
//				}
//				mContext.deleteDatabase(DATABASE_NAME);
//				getWritableDatabase();
//			}
//		} catch (SQLiteException ex) {
//			// add begin 2011-06-30 rongjinsong
//			PackageManager pm = context.getPackageManager();
//			pm.clearPackagePreferredActivities(context.getPackageName());
//			mContext.deleteDatabase(DATABASE_NAME);
//		}
//	}

	public static String getDBName() {
		return DATABASE_NAME;
	}

	public static int getCurrentDbVersion() {
		return DB_VERSION_MAX;
	}

	/**
	 * 用于单表查询
	 */
	public Cursor query(String tableName, String[] projection,
			String selection, String[] selectionArgs, String groupBy,
			String having, String sortOrder) {
		Cursor result = null;
		try {
			SQLiteDatabase db = getReadableDatabase();
			result = db.query(tableName, projection, selection, selectionArgs,
					groupBy, having, sortOrder);
		} catch (SQLException e) {
			// e.printStackTrace();
			Log.i("data", "SQLException when query in " + tableName + ", "
					+ selection);
		} catch (IllegalStateException e) {
			// e.printStackTrace();
			Log.i("data", "IllegalStateException when query in " + tableName
					+ ", " + selection);
		}
		return result;
	}

	/**
	 * 
	 * @author huyong
	 * @param sql
	 * @throws DatabaseException
	 */
	public void exec(String sql) throws DatabaseException {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.execSQL(sql);
		} catch (SQLException e) {
			Log.i("data", "Exception when exec " + sql);
			throw new DatabaseException(e);
		}
	}

	/**
	 * 检查指定的表是否存在
	 * 
	 * @author liuxinyang
	 * @param tableName
	 *            要检查是否在的表的表名
	 * @return
	 */
	public boolean isExistTable(String tableName, SQLiteDatabase db) {
		boolean result = false;
		Cursor cursor = null;
		String where = "type='table' and name='" + tableName + "'";
		try {
			cursor = db.query("sqlite_master", null, where, null, null, null,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				result = true;
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 升级
	 */
	abstract class UpgradeDB {
		abstract boolean onUpgradeDB(SQLiteDatabase db);
	}

	/**
	 * 检查表中是否存在该字段
	 * 
	 * @author huyong
	 * @param db
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	private boolean isExistColumnInTable(SQLiteDatabase db, String tableName,
			String columnName) {
		boolean result = false;
		Cursor cursor = null;
		try {
			// 查询列数
			String columns[] = { columnName };
			cursor = db.query(tableName, columns, null, null, null, null, null);
			if (cursor != null && cursor.getColumnIndex(columnName) >= 0) {
				result = true;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			Log.i("AppClassifyDatabaseHelper",
					"isExistColumnInTable has exception");
			result = false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

}
