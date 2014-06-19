package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.bookmark.RestoreVersionFour;
import com.jiubang.go.backup.pro.bookmark.RestoreVersionTwo;
import com.jiubang.go.backup.pro.bookmark.RestoreVersionTwo.BookMarkRestoreArgs;
import com.jiubang.go.backup.pro.bookmark.RestoreVersionTwo.BookMarkRestoreMsg;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * <br>类描述:书签恢复entry
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-16]
 */
public class BookMarkRestoreEntry extends BaseRestoreEntry {
	public static final String BOOKMARK_RESTORE_FILE_NAME = "bookmark.local";
	private final String mBookMarkBackupThreadName = "BookMarkBackupThreadName";
	private HandlerThread mThread;
	private RestoreBookMarkThreadHandler mHandler;
	private Context mContext = null;
	private IAsyncTaskListener mListener;
	private String mRecordDir;
	private boolean mIsCancel = false;

	public BookMarkRestoreEntry(Context ctx, String recordDir) {
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
		File bookMarkFile = new File(recordDir, BOOKMARK_RESTORE_FILE_NAME);
		if (bookMarkFile.exists()) {
			return bookMarkFile;
		}
		return null;
	}

	public static File getOrginalFile(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return null;
		}
		File srcFile = new File(dir, BookMarkBackupEntry.BOOKMARK_BACKUP_FILE_NAME);
		if (!srcFile.exists()) {
			return null;
		}
		return srcFile;
	}

	/**
	 * <br>类描述:恢复短信线程handler
	 * <br>功能详细描述:
	 *
	 * @author  jiangpeihe
	 * @date  [2012-10-16]
	 */
	private class RestoreBookMarkThreadHandler extends Handler {

		public RestoreBookMarkThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			final int m5 = 5;
			switch (msg.what) {
				case BookMarkRestoreMsg.BOOKMARK_RESTORE_START :
					break;

				case BookMarkRestoreMsg.BOOKMARK_RESTORE_PROCEEDING :
					if (mListener != null
							&& (msg.arg1 == 0 || msg.arg1 == msg.arg2 || msg.arg1 % m5 == 0)) {
						String tips = mContext != null ? mContext.getString(
								R.string.progress_detail, msg.arg1, msg.arg2) : "";
						mListener.onProceeding((float) (msg.arg1) / (float) (msg.arg2),
								BookMarkRestoreEntry.this, tips, null);
					}
					break;

				case BookMarkRestoreMsg.BOOKMARK_RESTORE_END :
					finish(true);
					break;

				case BookMarkRestoreMsg.BOOKMARK_RESTORE_FILE_NOT_EXIT :
					finish(false);
					break;

				case BookMarkRestoreMsg.BOOKMARK_RESTORE_SMS_COUNT_ZERO :
					finish(false);
					break;

				case BookMarkRestoreMsg.BOOKMARK_RESTORE_USER_CANCEL :
					mIsCancel = true;
					break;

				default :
					break;
			}
		}
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
			mListener.onEnd(success, BookMarkRestoreEntry.this, null);
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
		boolean booleanRet = true;
		if (mThread == null) {
			mThread = new HandlerThread(mBookMarkBackupThreadName);
			mThread.start();
			mHandler = new RestoreBookMarkThreadHandler(mThread.getLooper());
		}
		setState(RestoreState.RESTORING);
		mListener = listener;
		mListener.onStart(BookMarkRestoreEntry.this, null);
		BookMarkRestoreArgs args = new BookMarkRestoreArgs();
		args.mRestoreFilePath = Util.ensureFileSeparator(((RestoreArgs) data).mRestorePath)
				+ BOOKMARK_RESTORE_FILE_NAME;
		args.mHandler = mHandler;
		if (Util.getAndroidSystemVersion() < Build.VERSION_CODES.HONEYCOMB) {
			//对于系统2.x的恢复
			booleanRet = new RestoreVersionTwo().restoreBookMark(context, args);
		} else {
			booleanRet = new RestoreVersionFour().restoreBookMark(context, args);
		}
		return booleanRet;
	}

	@Override
	public void stopRestore() {

	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_BOOKMARK;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.bookmark) : "";
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
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_bookmark);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_bookmark);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_BOOKMARK, null);
		File bookMarkFile = new File(recordRootPah, BookMarkBackupEntry.BOOKMARK_BACKUP_FILE_NAME);
		if (bookMarkFile.exists()) {
			bookMarkFile.delete();
		}
	}

}
