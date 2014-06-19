package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.ringtone.RingtoneBackup;
import com.jiubang.go.backup.pro.ringtone.RingtoneRestore;
import com.jiubang.go.backup.pro.ringtone.RingtoneRestore.RingtoneRestoreArgs;
import com.jiubang.go.backup.pro.ringtone.RingtoneRestore.RingtoneRestoreMsg;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 铃声恢复项
 * 
 * @author chenchangming
 */
public class RingtoneRestoreEntry extends BaseRestoreEntry {

	private Context mContext;
	private IAsyncTaskListener mListener = null;
	private HandlerThread mThread;
	private RestoreRingtoneThreadHandler mHandler;
	private String mRingtoneRestoreThreadName = "mmsRestoreThreadName";
	private boolean mIsCancel = false;
	private RingtoneRestore mRingtoneRestore;
	private String mParentDir;

	public RingtoneRestoreEntry(Context context, String dirPath) {
		super();
		mContext = context;
		mParentDir = dirPath;
		if (hasRestorableRingtoneFiles(mParentDir)) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	public static boolean hasRestorableRingtoneFiles(String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		File file = new File(path, RingtoneBackup.MANAGER_DATA);
		if (!file.exists()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		boolean result = true;
		if (context == null || data == null || listener == null) {
			result = false;
			return result;
		}
		if (!(data instanceof RestoreArgs)) {
			result = false;
			return result;
		}

		if (mThread == null) {
			mThread = new HandlerThread(mRingtoneRestoreThreadName);
			mThread.start();
			mHandler = new RestoreRingtoneThreadHandler(mThread.getLooper());
		}

		mListener = listener;
		mListener.onStart(RingtoneRestoreEntry.this, null);

		mRingtoneRestore = new RingtoneRestore();
		RingtoneRestoreArgs args = new RingtoneRestoreArgs();
		args.mRestoreFilePath = Util.ensureFileSeparator(((RestoreArgs) data).mRestorePath);
		args.mHandler = mHandler;
		mRingtoneRestore.restoreRingtone(mContext, args);
		return result;
	}

	@Override
	public void stopRestore() {
		if (mRingtoneRestore != null) {
			mRingtoneRestore.stopRestoreRingtone();
			mIsCancel = true;
		}

	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_SYSTEM_RINGTONE;
	}

	@Override
	public long getSpaceUsage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return mContext != null ? mContext.getString(R.string.ringtone) : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_ringtone);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	/**
	 * 铃声恢复线程handler
	 * 
	 * @author chenchangming
	 */
	public class RestoreRingtoneThreadHandler extends Handler {
		public RestoreRingtoneThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {

			final float m025f = 0.25f;
			switch (msg.what) {

				case RingtoneRestoreMsg.RINGTONE_RESTORE_START :
					break;

				case RingtoneRestoreMsg.RINGTONE_RESTORE_PROCEEDING :
					if (mListener != null) {
						mListener
								.onProceeding(m025f, RingtoneRestoreEntry.this, msg.arg1, msg.arg2);
					}
					break;
				case RingtoneRestoreMsg.RINGTONE_RESTORE_END :
					finish(true);
					break;
				case RingtoneRestoreMsg.RINGTONE_RESTORE_FILE_NOT_EXIT :
					finish(false);
					break;
				case RingtoneRestoreMsg.RINGTONE_RESTORE_USER_CANCEL :
					mIsCancel = true;
					break;
				case RingtoneRestoreMsg.RINGTONE_RESTORE_ERROR_OCCUR :
					finish(false);
					break;
				default :
					break;
			}
		}
	}

	private void finish(boolean result) {
		if (mIsCancel) {
			setState(RestoreState.RESTORE_CANCELED);
		} else {
			RestoreState state = result
					? RestoreState.RESTORE_SUCCESSFUL
					: RestoreState.RESTORE_ERROR_OCCURRED;
			setState(state);
		}

		if (mListener != null) {
			if (mIsCancel) {
				result = false;
			}
			mListener.onEnd(result, RingtoneRestoreEntry.this, null);
		}
		mIsCancel = false;
		quitRestore();
	}

	private void quitRestore() {
		if (mThread != null && mThread.getLooper() != null) {
			mThread.getLooper().quit();
			mThread = null;
		}
	}

	@Override
	public boolean isNeedReboot() {
		return false;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_ringtone);
	}
	
	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		Cursor cursor = null;
		try {
			cursor = backupDBHelper.query(DataTable.TABLE_NAME, new String[] {
					DataTable.DATA3, DataTable.DATA4, DataTable.DATA5, DataTable.DATA6 },
					DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_RINGTONE,
					null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String alarmFileName = cursor.getString(0);
				String ringAllTypeFileName = cursor.getString(1);
				String notificationFileName = cursor.getString(2);
				String ringFileName = cursor.getString(3);
				if (!TextUtils.isEmpty(alarmFileName)) {
					File alarmFile = new File(recordRootPah, alarmFileName);
					if (alarmFile.exists()) {
						alarmFile.delete();
					}
				}
				if (!TextUtils.isEmpty(ringAllTypeFileName)) {
					File ringAllTypeFile = new File(recordRootPah, ringAllTypeFileName);
					if (ringAllTypeFile.exists()) {
						ringAllTypeFile.delete();
					}
				}
				if (!TextUtils.isEmpty(notificationFileName)) {
					File notificationFile = new File(recordRootPah, notificationFileName);
					if (notificationFile.exists()) {
						notificationFile.delete();
					}
				}

				if (!TextUtils.isEmpty(ringFileName)) {
					File ringFile = new File(recordRootPah, ringFileName);
					if (ringFile.exists()) {
						ringFile.delete();
					}
				}
			}
		} catch (Exception e) {
			Log.i("TEST", "Exception" + e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_RINGTONE, null);
		File ringtoneManagerFile = new File(recordRootPah, RingtoneBackup.MANAGER_DATA);
		if (ringtoneManagerFile.exists()) {
			ringtoneManagerFile.delete();
		}
	}
}
