package com.jiubang.go.backup.pro.data;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.sms.SmsRestore;
import com.jiubang.go.backup.pro.sms.SmsRestore.SmsRestoreArgs;
import com.jiubang.go.backup.pro.sms.SmsRestore.SmsRestoreMsg;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 短信恢复分为两个阶段：恢复短信内容到sms数据库，更新threads表（更新会话）
 * 插入短信到sms表不会引发更新会话表，需要用增加一条temp短信，然后删除这条短信的方式，去触发会话表更新（从go短信拿过来的技术）
 */

/**
 * 短信恢复
 *
 * @author kevin
 */
public class SmsRestoreEntry extends BaseRestoreEntry {
	private static final String MMS_PACKAGE_NAME = "com.android.mms";
	public static final String SMS_RESTORE_FILE_NAME = "sms.dat";
	private final Context mContext;
	private IAsyncTaskListener mListener = null;
	private HandlerThread mThread;
	private RestoreSmsThreadHandler mHandler;
	private final String mSmsRestoreThreadName = "smsRestoreThreadName";
	private boolean mIsCancel = false;
	private SmsRestore mSmsRestore;
	private String mRecordDir;

	/**
	 * 恢复短信线程handler
	 *
	 * @author wencan
	 */
	private class RestoreSmsThreadHandler extends Handler {

		public RestoreSmsThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			final int m5 = 5;
			final float m07f = 0.7f;
			final float m03f = 0.3f;
			switch (msg.what) {
				case SmsRestoreMsg.MSG_RESTORE_START :
					break;

				case SmsRestoreMsg.MSG_RESTORE_PROCEEDING :
					if (mListener != null
							&& (msg.arg1 % m5 == 0 || msg.arg1 == 1 || msg.arg1 == msg.arg2)) {
						String tips = mContext != null ? mContext.getString(
								R.string.progress_detail, msg.arg1, msg.arg2) : "";
						mListener.onProceeding(m07f * ((float) (msg.arg1 - 1) / (float) msg.arg2),
								SmsRestoreEntry.this, tips, null);
					}
					break;

				case SmsRestoreMsg.MSG_RESTORE_END :
					finish(true);
					break;

				case SmsRestoreMsg.MSG_RESTORE_FILE_NOT_EXIT :
					finish(false);
					break;

				case SmsRestoreMsg.MSG_RESTORE_SMS_COUNT_ZERO :
					finish(true);
					break;

				case SmsRestoreMsg.MSG_RESTORE_USER_CANCEL :
					mIsCancel = true;
					break;

				case SmsRestoreMsg.MSG_RESTORE_START_UPDATE_CONVERSATION :
					break;

				case SmsRestoreMsg.MSG_RESTORE_UPDATING_CONVERSATION :
					if (mListener != null
							&& (msg.arg1 % m5 == 0 || msg.arg1 == 1 || msg.arg1 == msg.arg2)) {
						String tips = mContext != null ? mContext.getString(
								R.string.msg_updating_sms_conversation, msg.arg1, msg.arg2) : "";
						mListener.onProceeding(m07f + m03f
								* ((float) (msg.arg1) / (float) (msg.arg2)), SmsRestoreEntry.this,
								tips, null);
					}
					break;

				case SmsRestoreMsg.MSG_RESTORE_UPDAGE_CONVERSATION_FINISH :
					break;

				default :
					break;
			}
		}
	}

	public SmsRestoreEntry(Context ctx, String recordDir) {
		super();
		mContext = ctx;
		mRecordDir = recordDir;

		if (getOrginalFile(mRecordDir) != null) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	public static File getOrginalFile(String recordDir) {
		if (recordDir == null) {
			return null;
		}

		File smsFile = new File(recordDir, SmsRestoreEntry.SMS_RESTORE_FILE_NAME);
		if (smsFile.exists()) {
			return smsFile;
		}
		return null;
	}

	private void finish(boolean success) {
		if (mIsCancel) {
			setState(RestoreState.RESTORE_CANCELED);
		} else {
			RestoreState state = success
					? RestoreState.RESTORE_SUCCESSFUL
					: RestoreState.RESTORE_ERROR_OCCURRED;
			setState(state);
		}

		if (mListener != null) {
			if (mIsCancel) {
				success = false;
			}
			mListener.onEnd(success, SmsRestoreEntry.this, null);
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
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		if (context == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof RestoreArgs)) {
			return false;
		}

		if (mThread == null) {
			mThread = new HandlerThread(mSmsRestoreThreadName);
			mThread.start();
			mHandler = new RestoreSmsThreadHandler(mThread.getLooper());
		}
		setState(RestoreState.RESTORING);
		mListener = listener;
		mListener.onStart(SmsRestoreEntry.this, null);

		mSmsRestore = new SmsRestore();
		SmsRestoreArgs args = new SmsRestoreArgs();
		args.mRestoreFilePath = Util.ensureFileSeparator(((RestoreArgs) data).mRestorePath)
				+ SMS_RESTORE_FILE_NAME;
		args.mNeedDecrypte = true;
		args.mDecryptPassword = Constant.getPassword();
		args.mHandler = mHandler;
		mSmsRestore.restoreSms(mContext, args);
		return true;
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_SMS;
	}

	@Override
	public long getSpaceUsage() {
		return 0;

	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.sms) : "";
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = Util.loadIconFromPackageName(context, MMS_PACKAGE_NAME);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			} else {
				ret = false;
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
	public void stopRestore() {
		if (mSmsRestore != null) {
			mSmsRestore.stopRestoreSms();
			mIsCancel = true;
		}
	}

	public static int mGetSmsItemCount(String parentDir) {
		if (parentDir == null) {
			return 0;
		}
		int count = 0;
		String dir = Util.ensureFileSeparator(parentDir);
		File srcFile = new File(dir, SMS_RESTORE_FILE_NAME);
		File tempFile = new File(dir, "sms.temp");
		if (Util.decryptFile(srcFile, tempFile, Constant.getPassword())) {
			srcFile = tempFile;
		} else {
			// 如果失败，又可能是v1.0.2版本以前没有加密导致解密失败，不用处理，使用源文件解析
		}

		count = getSmsItemCountInternal(srcFile);
		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
		}
		return count;
	}

	private static int getSmsItemCountInternal(File srcFile) {
		FileInputStream trace = null;
		DataInputStream datainput = null;
		int count = 0;
		try {
			trace = new FileInputStream(srcFile);
			datainput = new DataInputStream(trace);
			count = datainput.readInt();
		} catch (Exception e) {
			e.printStackTrace();
			count = 0;
		} finally {
			try {
				if (trace != null) {
					trace.close();
					trace = null;
				}
				if (datainput != null) {
					datainput.close();
					datainput = null;
				}
			} catch (Exception e) {
			}
		}
		return count;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		final DrawableKey key = DrawableProvider.buildDrawableKey(MMS_PACKAGE_NAME);
		final Drawable defaultDrawable = mContext.getResources().getDrawable(R.drawable.icon_sms);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_SMS, null);
		File smsFile = new File(recordRootPah, SmsBackupEntry.SMS_BACKUP_FILE_NAME);
		if (smsFile.exists()) {
			smsFile.delete();
		}
	}

}
