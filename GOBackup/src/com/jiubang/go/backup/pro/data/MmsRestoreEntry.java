package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.mms.MmsRestore;
import com.jiubang.go.backup.pro.mms.MmsRestore.MmsRestoreArgs;
import com.jiubang.go.backup.pro.mms.MmsRestore.MmsRestoreMsg;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 彩信恢复
 * 
 * @author ReyZhang
 */
public class MmsRestoreEntry extends BaseRestoreEntry {

	private Context mContext;
	private IAsyncTaskListener mListener = null;
	private HandlerThread mThread;
	private RestoreMmsThreadHandler mHandler;
	private String mMmsRestoreThreadName = "mmsRestoreThreadName";
	private boolean mIsCancel = false;
	private MmsRestore mMmsRestore;
	private String mParentDir;

	public MmsRestoreEntry(Context context, String dirPath) {
		super();
		mContext = context;
		mParentDir = dirPath;
		if (hasRestorableMmsFiles(new File(mParentDir))) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	public static boolean hasRestorableMmsFiles(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return false;
		}
		File mmsFile = new File(dir, MmsBackupEntry.MMS_DIR_NAME);
		File[] subFiles = mmsFile.listFiles();
		if (!mmsFile.exists() || subFiles == null || subFiles.length < 1) {
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
			mThread = new HandlerThread(mMmsRestoreThreadName);
			mThread.start();
			mHandler = new RestoreMmsThreadHandler(mThread.getLooper());
		}
		setState(RestoreState.RESTORING);
		mListener = listener;
		mListener.onStart(MmsRestoreEntry.this, null);

		mMmsRestore = new MmsRestore();
		MmsRestoreArgs args = new MmsRestoreArgs();
		args.mRestoreFilePath = Util.ensureFileSeparator(((RestoreArgs) data).mRestorePath);
		args.mHandler = mHandler;
		mMmsRestore.restoreMms(mContext, args);
		return result;
	}

	@Override
	public void stopRestore() {
		if (mMmsRestore != null) {
			mMmsRestore.stopRestoreMms();
			mIsCancel = true;
		}
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_MMS;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.mms) : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_mms);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	/**
	 * 彩信恢复Handler
	 * 
	 * @author ReyZhang
	 */
	public class RestoreMmsThreadHandler extends Handler {
		private final int mFive = 5;
		private final int mOne = 1;
		private final float mPointSeven = 0.7f;
		private final float mPointThree = 0.3f;

		public RestoreMmsThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case MmsRestoreMsg.MMS_RESTORE_START :
					break;

				case MmsRestoreMsg.MMS_RESTORE_PROCEEDING :
					if (mListener != null
							&& (msg.arg1 % mFive == 0 || msg.arg1 == mOne || msg.arg1 == msg.arg2)) {
						String tips = mContext != null ? mContext.getString(
								R.string.progress_detail, msg.arg1, msg.arg2) : "";
						mListener.onProceeding(mPointSeven * (msg.arg1 - mOne) / (msg.arg2),
								MmsRestoreEntry.this, tips, null);
					}
					break;

				case MmsRestoreMsg.MMS_RESTORE_END :
					finish(true);
					break;

				case MmsRestoreMsg.MMS_RESTORE_FILE_NOT_EXIT :
					finish(false);
					break;

				case MmsRestoreMsg.MMS_RESTORE_SMS_COUNT_ZERO :
					finish(true);
					break;

				case MmsRestoreMsg.MMS_RESTORE_USER_CANCEL :
					mIsCancel = true;
					break;

				case MmsRestoreMsg.MMS_RESTORE_START_UPDATE_CONVERSATION :
					break;

				case MmsRestoreMsg.MMS_RESTORE_UPDATING_CONVERSATION :
					if (mListener != null
							&& (msg.arg1 % mFive == 0 || msg.arg1 == mOne || msg.arg1 == msg.arg2)) {
						String tips = mContext != null
								? mContext.getString(R.string.msg_mms_updating_msm_conversation,
										msg.arg1, msg.arg2) : "";
						mListener.onProceeding(mPointSeven + mPointThree
								* ((float) (msg.arg1) / (float) (msg.arg2)), MmsRestoreEntry.this,
								tips, null);
					}
					break;

				case MmsRestoreMsg.MMS_RESTORE_UPDAGE_CONVERSATION_FINISH :
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
			mListener.onEnd(result, MmsRestoreEntry.this, null);
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
		return context.getResources().getDrawable(R.drawable.icon_mms);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_MMS, null);
		File mmsFile = new File(recordRootPah, MmsBackupEntry.MMS_DIR_NAME);
		if (mmsFile.exists()) {
			File[] subFiles = mmsFile.listFiles();
			if (subFiles != null && subFiles.length > 0) {
				for (File subFile : subFiles) {
					if (subFile.exists()) {
						subFile.delete();
					}
				}
			}
			mmsFile.delete();
		}
	}
}
