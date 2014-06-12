package com.jiubang.ggheart.data.statistics;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.statistics.tables.GUIThemeTable;
import com.jiubang.ggheart.data.statistics.tables.GoStoreAppTable;
import com.jiubang.ggheart.data.statistics.tables.MonitorAppsTable;

/**
 * 统计数据库
 * 
 * @author huyong
 * 
 */
public class StatisticsDataBaseHelper extends SQLiteOpenHelper {
	private final static int DB_VERSION_ONE = 1;

	private final static int DB_VERSION_MAX = 5;
	private final static String DATABASE_NAME = "launchers.db";

	private final Context mContext;
	private boolean mUpdateResult = true; // 更新数据库结果，默认是成功的。

	private final static String TYPE_NUMERIC = "numeric";
	private final static String TYPE_TEXT = "text";

	public StatisticsDataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION_MAX);

		mContext = context.getApplicationContext();
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (!mUpdateResult) {
				// 更新失败，则删除数据库，再行创建。
				if (db != null) {
					db.close();
				}
				mContext.deleteDatabase(DATABASE_NAME);
				getWritableDatabase();
			}
		} catch (SQLiteException ex) {
			// add begin 2011-06-30 rongjinsong
			PackageManager pm = context.getPackageManager();
			pm.clearPackagePreferredActivities(context.getPackageName());
			// int icon = android.R.drawable.stat_notify_error;
			// Uri packageUri = Uri.parse("package:" +
			// context.getPackageName());
			// CharSequence tickerText = mContext
			// .getText(R.string.db_dataException);
			// CharSequence contentTitle = mContext
			// .getText(R.string.crash_notif_title);
			// CharSequence contentText = mContext
			// .getText(R.string.crash_notif_uninstall_text);
			// Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
			// AppUtils.sendNotification(mContext, intent, icon, tickerText,
			// contentTitle, contentText,
			// INotificationId.GOTO_DB_EXCEPTION);
			mContext.deleteDatabase(DATABASE_NAME);
			// add end 2011-06-30
		}
	}

	public static String getDBName() {
		return DATABASE_NAME;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {

			// 初始建表
			db.execSQL(MonitorAppsTable.CREATE_TEABLE_SQL);
			db.execSQL(GUIThemeTable.CREATE_TABLE_SQL);
			db.execSQL(GoStoreAppTable.CREATE_TABLE_SQL);

			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		if (oldVersion < DB_VERSION_ONE || oldVersion > newVersion || newVersion > DB_VERSION_MAX) {
			return;
		}
		ArrayList<UpgradeDB> upgradeDBFuncS = new ArrayList<StatisticsDataBaseHelper.UpgradeDB>();
		upgradeDBFuncS.add(new UpgradeDBOneToTwo());
		upgradeDBFuncS.add(new UpgradeDBTwoToThree());
		upgradeDBFuncS.add(new UpgradeDBThreeToFour());
		upgradeDBFuncS.add(new UpgradeDBFourToFive());

		for (int i = oldVersion - 1; i < newVersion - 1; i++) {
			mUpdateResult = upgradeDBFuncS.get(i).onUpgradeDB(db);
			if (!mUpdateResult) {
				// 中间有任何一次升级失败，则直接返回
				break;
			}
		}
		upgradeDBFuncS.clear();
	}

	/**
	 * 用于单表查询
	 */
	public Cursor query(String tableName, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having, String sortOrder) {
		Cursor result = null;
		try {
			SQLiteDatabase db = getReadableDatabase();
			result = db.query(tableName, projection, selection, selectionArgs, groupBy, having,
					sortOrder);
		} catch (SQLException e) {
			// e.printStackTrace();
			Log.i("data", "SQLException when query in " + tableName + ", " + selection);
		} catch (IllegalStateException e) {
			// e.printStackTrace();
			Log.i("data", "IllegalStateException when query in " + tableName + ", " + selection);
		}
		return result;
	}

	public long insert(String tableName, ContentValues initialValues) throws DatabaseException {
		SQLiteDatabase db = getWritableDatabase();
		long rowId = 0;
		try {
			rowId = db.insert(tableName, null, initialValues);
		} catch (Exception e) {
			Log.i("data", "Exception when insert in " + tableName);
			throw new DatabaseException(e);
		}
		return rowId;
	}

	public int delete(String tableName, String selection, String[] selectionArgs)
			throws DatabaseException {
		SQLiteDatabase db = getWritableDatabase();
		int count = 0;
		try {
			count = db.delete(tableName, selection, selectionArgs);
		} catch (Exception e) {
			Log.i("data", "Exception when delete in " + tableName + ", " + selection);
			throw new DatabaseException(e);
		}
		return count;
	}

	public int update(String tableName, ContentValues values, String selection,
			String[] selectionArgs) throws DatabaseException {
		SQLiteDatabase db = getWritableDatabase();
		int count = 0;
		try {
			count = db.update(tableName, values, selection, selectionArgs);
		} catch (Exception e) {
			Log.i("data", "Exception when update in " + tableName + ", " + selection);
			throw new DatabaseException(e);
		}
		return count;
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
	 * @author zhouxuewen
	 * @param tableName
	 *            要检查是否在的表的表名
	 * @return
	 */
	public boolean isExistTable(String tableName) {
		SQLiteDatabase db = getWritableDatabase();
		boolean result = false;
		Cursor cursor = null;
		String where = "type='table' and name='" + tableName + "'";
		try {
			cursor = db.query("sqlite_master", null, where, null, null, null, null);
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
	 * 创建表
	 * 
	 * @author zhouxuewen
	 * @param tabName
	 *            要创建的表的表名
	 * @return
	 */
	public void createTab(String tabName) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			// 创建表
			db.execSQL(tabName);

			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

	}

	/**
	 * 
	 * @author zhouxuewen
	 *
	 */
	abstract class UpgradeDB {
		abstract boolean onUpgradeDB(SQLiteDatabase db);
	}

	/**
	 * 
	 * @author zhouxuewen
	 *
	 */
	class UpgradeDBOneToTwo extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return onUpgrade1To2(db);
		}
	}

	/**
	 * 
	 * @author zhouxuewen
	 *
	 */
	class UpgradeDBTwoToThree extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return onUpgrade2To3(db);
		}
	}

	/**
	 * 
	 * @author zhouxuewen
	 *
	 */
	class UpgradeDBThreeToFour extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return onUpgrade3To4(db);
		}
	}
	
	/**
	 * 
	 * @author zhouxuewen
	 *
	 */
	class UpgradeDBFourToFive extends UpgradeDB {
		@Override
		boolean onUpgradeDB(SQLiteDatabase db) {
			return onUpgrade4To5(db);
		}
	}

	/**
	 * 一版升级到二版
	 * 
	 * @author zhouxuewem
	 * @param db
	 */
	private boolean onUpgrade1To2(SQLiteDatabase db) {
		boolean result = false;
		db.beginTransaction();
		try {
			String table = GoStoreAppTable.TABLENAME;
			String column = GoStoreAppTable.CLASSIFY;
			addColumnToTable(db, table, column, TYPE_TEXT, String.valueOf(0));
			db.setTransactionSuccessful();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			db.endTransaction();
		}
		return result;
	}

	/**
	 * 二版升级到三版
	 * 
	 * @author zhouxuewem
	 * @param db
	 */
	private boolean onUpgrade2To3(SQLiteDatabase db) {
		boolean result = false;
		db.beginTransaction();
		try {
			String table = GUIThemeTable.TABLENAME;
			String column = GUIThemeTable.PKG_TYPE;
			addColumnToTable(db, table, column, TYPE_TEXT, String.valueOf(0));
			db.setTransactionSuccessful();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			db.endTransaction();
		}
		return result;
	}

	/**
	 * 三版升级到四版
	 * 
	 * @author zhouxuewem
	 * @param db
	 */
	private boolean onUpgrade3To4(SQLiteDatabase db) {
		boolean result = false;
		db.beginTransaction();
		try {
			String table = GoStoreAppTable.TABLENAME;
			String column = GoStoreAppTable.CLICK_TIME;
			addColumnToTable(db, table, column, TYPE_TEXT, String.valueOf(0));
			db.setTransactionSuccessful();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			db.endTransaction();
		}
		return result;
	}
	
	/**
	 * 四版升级到五版
	 * 
	 * @author zhouxuewem
	 * @param db
	 */
	private boolean onUpgrade4To5(SQLiteDatabase db) {
		boolean result = false;
		db.beginTransaction();
		try {
			String table = GUIThemeTable.TABLENAME;
			
			String column = GUIThemeTable.DETAIL_CLICK;
			addColumnToTable(db, table, column, TYPE_NUMERIC, String.valueOf(0));
			
			column = GUIThemeTable.DETAIL_GET_CLICK;
			addColumnToTable(db, table, column, TYPE_NUMERIC, String.valueOf(0));
			
			db.setTransactionSuccessful();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			db.endTransaction();
		}
		return result;
	}

	/**
	 * 新添加字段到表中
	 * 
	 * @author huyong
	 * @param db
	 * @param tableName
	 *            : 修改表名
	 * @param columnName
	 *            ：新增字段名
	 * @param columnType
	 *            ：新增字段类型
	 * @param defaultValue
	 *            ：新增字段默认值。为null，则不提供默认值
	 */
	private void addColumnToTable(SQLiteDatabase db, String tableName, String columnName,
			String columnType, String defaultValue) {
		if (!isExistColumnInTable(db, tableName, columnName)) {
			db.beginTransaction();
			try {
				// 增加字段
				String updateSql = "ALTER TABLE " + tableName + " ADD " + columnName + " "
						+ columnType;
				db.execSQL(updateSql);

				// 提供默认值
				if (defaultValue != null) {
					if (columnType.equals(TYPE_TEXT)) {
						// 如果是字符串类型，则需加单引号
						defaultValue = "'" + defaultValue + "'";
					}

					updateSql = "update " + tableName + " set " + columnName + " = " + defaultValue;
					db.execSQL(updateSql);
				}

				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
			}
		}
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
	private boolean isExistColumnInTable(SQLiteDatabase db, String tableName, String columnName) {
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
			Log.i("StatisticsDataBaseHelper", "isExistColumnInTable has exception");
			result = false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return result;
	}
}
