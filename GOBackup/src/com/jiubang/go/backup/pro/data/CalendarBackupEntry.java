package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.calendar.CalendarOperator;
import com.jiubang.go.backup.pro.calendar.OnCalendarPersistListener;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 日历备份Entity，实现日历的备份
 *
 * @author wencan
 *
 */
public class CalendarBackupEntry extends BaseBackupEntry implements OnCalendarPersistListener {
	public static final String DEFAULT_CALENDAR_PACKAGE = "com.android.calendar";
	public static final String DEFAULT_HTC_CALENDAR_PACKAGE = "com.htc.calendar";
	public static final String CALENDAR_FILE_NAME = "calendar.encrypt";
	private Context mContext;
	private BackupArgs mBackupArgs;
	private IAsyncTaskListener mListener;

	public CalendarBackupEntry(Context context) {
		super();
		mContext = context;
	}

	//	public static BaseBackupEntry buildCalendarBackupEntry(Context context, Map<String, PackageInfo> installedPackages) {
	//		PackageInfo pi = null;
	//		if (installedPackages != null && installedPackages.size() > 0) {
	//			pi = installedPackages.get(DEFAULT_CALENDAR_PACKAGE);
	//		}
	//		if (pi == null) {
	//			pi = installedPackages.get(DEFAULT_HTC_CALENDAR_PACKAGE);
	//		}
	//		if (pi == null) {
	//			PackageManager pm = context.getPackageManager();
	//			try {
	//				pi = pm.getPackageInfo(DEFAULT_CALENDAR_PACKAGE, 0);
	//			} catch (NameNotFoundException e) {
	//				e.printStackTrace();
	//				pi = null;
	//			}
	//		}
	//		if (pi == null) {
	//			PackageManager pm = context.getPackageManager();
	//			try {
	//				pi = pm.getPackageInfo(DEFAULT_HTC_CALENDAR_PACKAGE, 0);
	//			} catch (NameNotFoundException e) {
	//				e.printStackTrace();
	//				pi = null;
	//			}
	//		}
	//		if (pi == null) {
	//			return null;
	//		}
	//		return new CalendarBackupEntry(context);
	//	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof BackupArgs)) {
			return false;
		}
		mBackupArgs = (BackupArgs) data;
		mListener = listener;
		setState(BackupState.BACKUPING);
		mListener.onStart(null, null);
		boolean ret = backupCalendarInternal(ctx, (BackupArgs) data);

		BackupState state = ret ? BackupState.BACKUP_SUCCESSFUL : BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);

		mListener.onEnd(ret, this, getCalendarBackupFiles(mBackupArgs.mBackupPath));
		return true;
	}

	private String[] getCalendarBackupFiles(String rootDir) {
		if (rootDir == null) {
			return null;
		}
		rootDir = Util.ensureFileSeparator(rootDir);
		return new String[] { rootDir + CALENDAR_FILE_NAME };
	}

	private boolean backupCalendarInternal(Context context, BackupArgs args) {
		if (context == null || args == null || args.mDbHelper == null) {
			return false;
		}

		boolean ret = false;
		CalendarOperator co = new CalendarOperator(context);
		if (co.getEventCount() == 0) {
			// 没有日历 TODO
			co.release();
			ret = false;
			return ret;
		}

		File tempFile = new File(args.mBackupPath, "calendar.temp");
		ret = co.persistCalendar(mContext, tempFile, this);

		if (!ret) {
			// 备份日历失败
			co.release();
			if (tempFile.exists()) {
				tempFile.delete();
			}
			return ret;
		}

		// 加密
		File calendarFile = new File(args.mBackupPath, CALENDAR_FILE_NAME);
		ret = Util.encryFile(tempFile, calendarFile, Constant.getPassword());
		if (!ret) {
			// 加密失败
			if (tempFile.exists()) {
				tempFile.delete();
			}
			if (calendarFile.exists()) {
				calendarFile.delete();
			}
			co.release();
			return ret;
		}

		// 更新数据库
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CALENDAR);
		cv.put(DataTable.DATA1, CALENDAR_FILE_NAME);
		cv.put(DataTable.DATA2, co.getEventCount());
		cv.put(DataTable.DATA3, co.getCalendarCount());
		cv.put(DataTable.DATA14, new Date().getTime());
		ret = args.mDbHelper.reflashDatatable(cv);

		// 删除临时文件
		if (tempFile.exists()) {
			tempFile.delete();
		}

		co.release();
		return ret;
	}

	public static int getLocalCalendarEventCount(Context context) {
		return CalendarOperator.getLocalEventCount(context);
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_CALENDAR;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.calendar) : "";
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_calendar);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	@Override
	public void onPersistStart(int totalCalendarCount, int totalEventCount) {
	}

	@Override
	public void onPersistProgress(int curCalendar, int totalCalendar, int curEvent, int totalEvent) {
		if (mListener != null) {
			String tips = mContext != null ? mContext.getString(R.string.progress_detail, curEvent,
					totalEvent) : "";
			mListener.onProceeding((float) curEvent / (float) totalEvent, CalendarBackupEntry.this,
					tips, null);
		}
	}

	@Override
	public void onPersistEnd(boolean success, int successCalendarCount, int totalCalendar,
			int successEventCount, int totalEvent) {
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_calendar);
	}
}
