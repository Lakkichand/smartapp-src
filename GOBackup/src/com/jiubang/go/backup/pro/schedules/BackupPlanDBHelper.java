package com.jiubang.go.backup.pro.schedules;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 定时备份数据库
 *
 * @author maiyongshen
 */
public class BackupPlanDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "schedules.db";
	// v1.06的数据库版本号
	private static final int DATABASE_VERSION_V106 = 1;

	private static final String PLAN_TABLE_NAME = "plans";
	private static final String CREATE_PLAN_TABLE = "CREATE TABLE " + PLAN_TABLE_NAME + "("
			+ BackupPlan.Columns._ID + " INTEGER PRIMARY KEY," + BackupPlan.Columns.BACKUP_TYPE
			+ " INTEGER, " + BackupPlan.Columns.HOUR + " INTEGER, " + BackupPlan.Columns.MINUTES
			+ " INTEGER, " + BackupPlan.Columns.START_TIME + " LONG, "
			+ BackupPlan.Columns.REPEAT_TYPE + " INTEGER, " + BackupPlan.Columns.DAY_OF_WEEK
			+ " INTEGER, " + BackupPlan.Columns.DAY_OF_MONTH + " INTEGER, "
			+ BackupPlan.Columns.REMINDER + " INTEGER, " + BackupPlan.Columns.ENABLED
			+ " INTERGER, " + BackupPlan.Columns.RUN_TIMES + " INTEGER" + ");";

	private Context mContext;

	public BackupPlanDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION_V106);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_PLAN_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + PLAN_TABLE_NAME);
		onCreate(db);
	}

	public long insertPlan(BackupPlan plan) {
		if (plan == null) {
			return -1;
		}
		ContentValues values = new ContentValues();
		values.put(BackupPlan.Columns.BACKUP_TYPE, /* plan.type.ordinal() */
				plan.type.getBackupType());
		values.put(BackupPlan.Columns.HOUR, plan.hour);
		values.put(BackupPlan.Columns.MINUTES, plan.minutes);
		values.put(BackupPlan.Columns.START_TIME, plan.startTime);
		values.put(BackupPlan.Columns.REPEAT_TYPE, plan.repeatType.ordinal());
		values.put(BackupPlan.Columns.DAY_OF_WEEK, plan.dayOfWeek);
		values.put(BackupPlan.Columns.DAY_OF_MONTH, plan.dayOfMonth);
		values.put(BackupPlan.Columns.REMINDER, plan.reminder);
		values.put(BackupPlan.Columns.ENABLED, plan.enabled ? 1 : 0);
		values.put(BackupPlan.Columns.RUN_TIMES, plan.runTimes);

		SQLiteDatabase db = null;
		long index = -1;
		try {
			db = getWritableDatabase();
			try {
				index = db.insert(PLAN_TABLE_NAME, null, values);
			} finally {
				db.close();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return index;
	}

	public boolean deletePlan(int planId) {
		if (planId < 0) {
			return false;
		}
		SQLiteDatabase db = null;
		boolean ret = false;
		try {
			db = getWritableDatabase();
			try {
				ret = db.delete(PLAN_TABLE_NAME, BackupPlan.Columns._ID + "=" + planId, null) > 0;
			} finally {
				db.close();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public boolean updatePlan(int id, ContentValues newValues) {
		if (id < 0 || newValues == null) {
			return false;
		}
		if (newValues.size() == 0) {
			return true;
		}
		SQLiteDatabase db = null;
		boolean ret = false;
		try {
			db = getWritableDatabase();
			try {
				ret = db.update(PLAN_TABLE_NAME, newValues, BackupPlan.Columns._ID + "=" + id, null) > 0;
			} finally {
				db.close();
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public Cursor getAllScheduledPlansCursor() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getWritableDatabase();
			cursor = db.query(PLAN_TABLE_NAME, BackupPlan.Columns.ALL_COLUMNS, null, null, null,
					null, BackupPlan.Columns.DEFAULT_SORT_ORDER);
		} catch (SQLiteException e) {
			e.printStackTrace();
			cursor = null;
		}
		return cursor;
	}

	public Cursor getEnabledPlansCursor() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getWritableDatabase();
			cursor = db.query(PLAN_TABLE_NAME, BackupPlan.Columns.ALL_COLUMNS,
					BackupPlan.Columns.WHERE_ENABLED, null, null, null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			cursor = null;
		}
		return cursor;
	}

}
