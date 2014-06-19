package com.jiubang.go.backup.pro.schedules;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupManager.BackupType;

/**
 * 备份计划
 * 
 * @author maiyongshen
 */
public class BackupPlan implements Parcelable {

	/*
	 * public enum BackupType { BACKUP_SYSTEM_DATA, BACKUP_ALL_APPS,
	 * BACKUP_SYSTEM_DATA_AND_APPS, };
	 */

	/**
	 * 报告类型
	 * 
	 * @author GoBackup Dev Team
	 */
	public enum RepeatType {
		ONE_OFF, DAILY, WEEKLY, MONTHLY
	}

	public int id;
	public BackupType type;
	public int hour;
	public int minutes;
	/**
	 * startTime 一次性的活动，表示计划任务的启动时间 重复性的活动，表示计划任务的起始时间
	 */
	public long startTime;
	public int dayOfWeek;
	public int dayOfMonth;
	public RepeatType repeatType;
	public int reminder; // 单位是毫秒
	public boolean enabled;
	public int runTimes;

	public static final Parcelable.Creator<BackupPlan> CREATOR = new Parcelable.Creator<BackupPlan>() {
		@Override
		public BackupPlan createFromParcel(Parcel source) {
			return new BackupPlan(source);
		}

		@Override
		public BackupPlan[] newArray(int size) {
			return new BackupPlan[size];
		}
	};

	public BackupPlan() {
		id = -1;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		hour = c.get(Calendar.HOUR_OF_DAY);
		minutes = c.get(Calendar.MINUTE);
		repeatType = RepeatType.ONE_OFF;
		enabled = false;
		reminder = 0;
		runTimes = 0;
	}

	public BackupPlan(Cursor cursor) {
		id = cursor.getInt(Columns.COLUMN_INDEX_ID);
		// 兼容V2.0以前版本
		int typeFromCursor = cursor.getInt(Columns.COLUMN_INDEX_BACKUP_TYPE);
		if (typeFromCursor == 0) {
			// V2.0以前的系统数据
			typeFromCursor = BackupType.BACKUP_TYPE_USER_DATA;
		} else if (typeFromCursor == 1) {
			// V2.0以前的应用程序
			typeFromCursor = BackupType.BACKUP_TYPE_USER_APP;
		} else if (typeFromCursor == 2) {
			// V2.0以前的系统程序+应用程序
			typeFromCursor = BackupType.BACKUP_TYPE_USER_DATA | BackupType.BACKUP_TYPE_USER_APP;
		}
		type = /*
				 * BackupType.values()[cursor.getInt(Columns.
				 * COLUMN_INDEX_BACKUP_TYPE)];
				 */new BackupType(typeFromCursor);
		hour = cursor.getInt(Columns.COLUMN_INDEX_HOUR);
		minutes = cursor.getInt(Columns.COLUMN_INDEX_MINUTES);
		startTime = cursor.getLong(Columns.COLUMN_INDEX_START_TIME);
		dayOfWeek = cursor.getInt(Columns.COLUMN_INDEX_DAY_OF_WEEK);
		dayOfMonth = cursor.getInt(Columns.COLUMN_INDEX_DAY_OF_MONTH);
		repeatType = RepeatType.values()[cursor.getInt(Columns.COLUMN_INDEX_REPEAT_TYPE)];
		reminder = cursor.getInt(Columns.COLUMN_INDEX_REMINDER);
		enabled = cursor.getInt(Columns.COLUMN_INDEX_ENABLED) == 1;
		runTimes = cursor.getInt(Columns.COLUMN_INDEX_RUN_TIMES);

	}

	public BackupPlan(Parcel parcel) {
		id = parcel.readInt();
		type = /* BackupType.values()[parcel.readInt()]; */new BackupType(parcel.readInt());
		hour = parcel.readInt();
		minutes = parcel.readInt();
		startTime = parcel.readLong();
		dayOfWeek = parcel.readInt();
		dayOfMonth = parcel.readInt();
		repeatType = RepeatType.values()[parcel.readInt()];
		reminder = parcel.readInt();
		enabled = parcel.readInt() == 1;
		runTimes = parcel.readInt();
	}

	public static String getBackupContentText(Context context, BackupType type) {
		if (type == null) {
			return null;
		}

		boolean backupUserData = type.isBackupUserData();
		boolean backupUserApp = type.isBackupUserApp();
		boolean backupSystemData = type.isBackupSystemData();

		StringBuilder str = new StringBuilder();
		if (backupUserData) {
			str.append(context.getString(R.string.backup_type_user_data));
		}
		if (backupUserApp) {
			if (backupUserData) {
				str.append(",");
			}
			str.append(context.getString(R.string.backup_type_user_app));
		}
		if (backupSystemData) {
			if (backupUserData || backupUserApp) {
				str.append(",");
			}
			str.append(context.getString(R.string.backup_type_system_data));
		}
		String result = context.getString(R.string.backup_type_content, str.substring(0));
		return result;
	}

	/**
	 * 数据库字段
	 * 
	 * @author GoBackup Dev Team
	 */
	public static class Columns implements BaseColumns {
		public static final String BACKUP_TYPE = "type";
		public static final String HOUR = "hour";
		public static final String MINUTES = "minutes";
		public static final String START_TIME = "start_time";
		public static final String REPEAT_TYPE = "repeat";
		public static final String DAY_OF_WEEK = "day_of_week";
		public static final String DAY_OF_MONTH = "day_of_month";
		public static final String REMINDER = "reminder";
		public static final String ENABLED = "enabled";
		public static final String RUN_TIMES = "run_times";

		public static final int COLUMN_INDEX_ID = 0;
		public static final int COLUMN_INDEX_BACKUP_TYPE = 1;
		public static final int COLUMN_INDEX_HOUR = 2;
		public static final int COLUMN_INDEX_MINUTES = 3;
		public static final int COLUMN_INDEX_START_TIME = 4;
		public static final int COLUMN_INDEX_REPEAT_TYPE = 5;
		public static final int COLUMN_INDEX_DAY_OF_WEEK = 6;
		public static final int COLUMN_INDEX_DAY_OF_MONTH = 7;
		public static final int COLUMN_INDEX_REMINDER = 8;
		public static final int COLUMN_INDEX_ENABLED = 9;
		public static final int COLUMN_INDEX_RUN_TIMES = 10;
		public static final int COLUMN_INDEX_FIRST_START_TIME = 11;

		public static final String DEFAULT_SORT_ORDER = HOUR + ", " + MINUTES + " ASC";
		public static final String WHERE_ENABLED = ENABLED + "=1";
		public static final String[] ALL_COLUMNS = { _ID, BACKUP_TYPE, HOUR, MINUTES, START_TIME,
				REPEAT_TYPE, DAY_OF_WEEK, DAY_OF_MONTH, REMINDER, ENABLED, RUN_TIMES };
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(/* type.ordinal() */type.getBackupType());
		dest.writeInt(hour);
		dest.writeInt(minutes);
		dest.writeLong(startTime);
		dest.writeInt(dayOfWeek);
		dest.writeInt(dayOfMonth);
		dest.writeInt(repeatType.ordinal());
		dest.writeInt(reminder);
		dest.writeInt(enabled ? 1 : 0);
		dest.writeInt(runTimes);
	}
}
