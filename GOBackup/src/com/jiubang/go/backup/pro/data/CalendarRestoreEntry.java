package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.calendar.CalendarOperator;
import com.jiubang.go.backup.pro.calendar.CalendarOperator.OnCalendarOperateListener;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 日历恢复Entry
 *
 * @author wencan
 *
 */
public class CalendarRestoreEntry extends BaseRestoreEntry implements OnCalendarOperateListener {
	private Context mContext;
	private String mRecordDir;
	private IAsyncTaskListener mListener;
	private CalendarOperator mCalendarOperator;
	private boolean mIsCancel;

	public CalendarRestoreEntry(Context context, String rootDir) {
		mContext = context;
		mRecordDir = rootDir;
		mIsCancel = false;

		if (getOrginalFile(mRecordDir) != null) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	public static File getOrginalFile(String recordDir) {
		if (recordDir == null) {
			return null;
		}

		File calendarFile = new File(recordDir, CalendarBackupEntry.CALENDAR_FILE_NAME);
		if (calendarFile.exists()) {
			return calendarFile;
		}
		return null;
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		if (context == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof RestoreArgs)) {
			return false;
		}
		mListener = listener;

		mIsCancel = false;
		setState(RestoreState.RESTORING);
		mListener.onStart(CalendarRestoreEntry.this, null);
		boolean ret = restoreCalendarInternal(context, (RestoreArgs) data, listener);
		RestoreState state = mIsCancel ? RestoreState.RESTORE_CANCELED : ret
				? RestoreState.RESTORE_SUCCESSFUL
				: RestoreState.RESTORE_ERROR_OCCURRED;
		setState(state);
		ret = mIsCancel ? false : ret;
		mListener.onEnd(ret, CalendarRestoreEntry.this, null);
		return true;
	}

	private boolean restoreCalendarInternal(Context context, RestoreArgs args,
			IAsyncTaskListener listener) {
		if (context == null || args == null || listener == null) {
			return false;
		}

		boolean ret = true;
		// 解密文件
		File tempFile = new File(args.mRestorePath, "calendar.temp");
		File srcFile = new File(args.mRestorePath, CalendarBackupEntry.CALENDAR_FILE_NAME);
		ret = Util.decryptFile(srcFile, tempFile, Constant.getPassword());
		if (!ret) {
			Log.d("GOBackup", "CalendarRestoreEntry : descryptFile faild");
			return false;
		}

		// 解析文件
		mCalendarOperator = new CalendarOperator(mContext, tempFile);
		mCalendarOperator.registerOnCalendarOperateListener(this);
		if (mCalendarOperator.getEventCount() == 0) {
			// 解析文件出错
			mCalendarOperator.release();
			mCalendarOperator = null;
			Log.d("GOBackup", "CalendarRestoreEntry : parserCalendar faild");
			if (tempFile.exists()) {
				tempFile.delete();
			}
			ret = false;
			return ret;
		}

		// 恢复日历
		ret = mCalendarOperator.restoreCalendar(context);

		// 删除缓存文件
		if (tempFile.exists()) {
			tempFile.delete();
		}
		mCalendarOperator.release();
		mCalendarOperator.unregisterOnCalendarOperateListener(this);
		mCalendarOperator = null;
		return ret;
	}

	@Override
	public void stopRestore() {
		if (mCalendarOperator != null) {
			mCalendarOperator.stopRestoreCalendar();
			mIsCancel = true;
		}
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
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.calendar) : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	@Override
	public void onCalendarOperateStart(CalendarOperator co) {
	}

	@Override
	public void onCalendarOperateProcess(CalendarOperator co, int totalEvent, int eventIndex) {
		if (mListener != null) {
			String tips = mContext != null ? mContext.getString(R.string.progress_detail,
					eventIndex, totalEvent) : "";
			mListener.onProceeding((float) eventIndex / (float) totalEvent,
					CalendarRestoreEntry.this, tips, null);
		}
	}

	@Override
	public void onCalendarOperateEnd(CalendarOperator co, boolean success, int totalEventCount,
			int successOperateCount) {
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_calendar);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_CALENDAR, null);
		File calendarFile = new File(recordRootPah, CalendarBackupEntry.CALENDAR_FILE_NAME);
		if (calendarFile.exists()) {
			calendarFile.delete();
		}
	}
}
