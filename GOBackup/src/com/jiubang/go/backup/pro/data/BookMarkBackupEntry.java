package com.jiubang.go.backup.pro.data;

import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Browser;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.bookmark.BackupFour;
import com.jiubang.go.backup.pro.bookmark.BackupTwo;
import com.jiubang.go.backup.pro.bookmark.BackupTwo.BookMarkBackupArgs;
import com.jiubang.go.backup.pro.bookmark.BackupTwo.BookMarkBackupMsg;
import com.jiubang.go.backup.pro.bookmark.BookMark;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * <br>类描述:书签备份entry
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-16]
 */
public class BookMarkBackupEntry extends BaseBackupEntry {
	public static final String BOOKMARK_BACKUP_FILE_NAME = "bookmark.local";
	private final String mBookMarkBackupThreadName = "BookMarkBackupThreadName";

	private static final String THUMBNAIL = "thumbnail";
	private static final String TOUCH_ICON = "touch_icon";
	public static final String[] STARNDARD_BOOKMARK_COLUMNS = new String[] {
			Browser.BookmarkColumns._ID, Browser.BookmarkColumns.CREATED,
			Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL,
			Browser.BookmarkColumns.FAVICON, Browser.BookmarkColumns.BOOKMARK,
			Browser.BookmarkColumns.VISITS, Browser.BookmarkColumns.DATE, THUMBNAIL, TOUCH_ICON };
	public static final String[] BOOKMARK_COLUMNS_WITH_FOLDER = new String[] {
			Browser.BookmarkColumns._ID, Browser.BookmarkColumns.CREATED,
			Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL,
			Browser.BookmarkColumns.FAVICON, Browser.BookmarkColumns.BOOKMARK,
			Browser.BookmarkColumns.VISITS, Browser.BookmarkColumns.DATE, THUMBNAIL, TOUCH_ICON,
			"folder" };

	private HandlerThread mThread;
	private BackupBookMarkThreadHandler mHandler;
	private Context mContext = null;
	private BackupArgs mBackupArgs;
	private IAsyncTaskListener mListener;
	private int mTotalBookmarkCount = 0;

	public BookMarkBackupEntry(Context context) {
		super();
		mContext = context;
	}

	/**
	 * <br>类描述:书签备份消息handler
	 * <br>功能详细描述:
	 *
	 * @author  jiangpeihe
	 * @date  [2012-10-11]
	 */
	private class BackupBookMarkThreadHandler extends Handler {

		public BackupBookMarkThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			final int m5 = 5;
			switch (msg.what) {
				case BookMarkBackupMsg.BOOKMARK_BACKUP_START :
					break;

				case BookMarkBackupMsg.BOOKMARK_BACKUP_PROCEEDING :
					if (mListener != null
							&& (msg.arg1 == 0 || msg.arg1 == msg.arg2 || msg.arg1 % m5 == 0)) {
						String tips = mContext != null ? mContext.getString(
								R.string.progress_detail, msg.arg1, msg.arg2) : "";
						mListener.onProceeding((float) (msg.arg1) / (float) (msg.arg2),
								BookMarkBackupEntry.this, tips, null);
					}
					mTotalBookmarkCount = msg.arg2;
					break;

				case BookMarkBackupMsg.BOOKMARK_BACKUP_END :
					finish(true);
					break;

				case BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR :
					finish(false);
					break;

				case BookMarkBackupMsg.BOOKMARK_COUNT_ZERO :
					finish(false);
					break;

				default :
					break;
			}
		}
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (mThread == null) {
			mThread = new HandlerThread(mBookMarkBackupThreadName);
			mThread.start();
			mHandler = new BackupBookMarkThreadHandler(mThread.getLooper());
		}
		if (!(data instanceof BackupArgs)) {
			return false;
		}
		mBackupArgs = (BackupArgs) data;
		mListener = listener;
		setState(BackupState.BACKUPING);
		mListener.onStart(null, null);
		BookMarkBackupArgs args = new BookMarkBackupArgs();
		args.mBackupFilePath = Util.ensureFileSeparator(mBackupArgs.mBackupPath)
				+ BOOKMARK_BACKUP_FILE_NAME;
		args.mHandler = mHandler;

		//系统版本的区别，而用不同的备份方法
		if (Util.getAndroidSystemVersion() < Build.VERSION_CODES.HONEYCOMB) {
			//对于系统2.x的备份
			new BackupTwo().backupBookMark(ctx, args);
		} else {
			new BackupFour().backupBookMark(ctx, args);
		}

		return true;
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

	private void finish(boolean success) {
		BackupState state = success
				? BackupState.BACKUP_SUCCESSFUL
				: BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);

		if (success && mBackupArgs != null) {
			/*
			 * if(mBackupArgs.mConfig != null){
			 * mBackupArgs.mConfig.put(BackupPropertiesConfig
			 * .P_BACKUP_BOOKMARK_COUNT, String.valueOf(mTotalBOOKMARKCount)); }
			 */
			success = updateBackupDb(mBackupArgs.mDbHelper);

		}

		if (mListener != null) {
			mListener.onEnd(success, this, getBookMarkBackupFiles());
		}
		quitBackup();
	}

	private boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_BOOKMARK);
		cv.put(DataTable.DATA1, BOOKMARK_BACKUP_FILE_NAME);
		cv.put(DataTable.DATA2, mTotalBookmarkCount);
		cv.put(DataTable.DATA14, new Date().getTime());
		return dbHelper.reflashDatatable(cv);
		// String where = DataTable.MIME_TYPE + "=" +
		// MimetypeTable.MIMETYPE_VALUE_SMS;
		// if(dbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0){
		// return dbHelper.insert(DataTable.TABLE_NAME, cv);
		//
		// }
		// return true;
	}

	private String[] getBookMarkBackupFiles() {
		String dir = Util.ensureFileSeparator(mBackupArgs.mBackupPath);
		return new String[] { dir + BOOKMARK_BACKUP_FILE_NAME };
	}

	private void quitBackup() {
		if (mThread != null && mThread.getLooper() != null) {
			mThread.getLooper().quit();
			mThread = null;
			mHandler = null;
		}
	}
	/**
	 * 获取本地书签个数
	 *
	 * @param context
	 * @return
	 */
	public static int getLocalBookMarkCount(Context context) {
		int count = 0;
		Cursor cursor = null;
		if (context == null) {
			return 0;
		}
		String selection = Browser.BookmarkColumns.BOOKMARK + "<>0 AND "
				+ Browser.BookmarkColumns.URL + " IS NOT NULL";

		try {
			cursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
					new String[] { Browser.BookmarkColumns.TITLE }, selection, null, null);
			count = cursor.getCount();

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}
	public static int getLocalFolderCount(Context context) {
		int count = 0;
		Cursor cursor = null;
		if (context == null) {
			return 0;
		}
		try {
			String selection = Browser.BookmarkColumns.BOOKMARK + "<>0 AND "
					+ Browser.BookmarkColumns.URL + " IS NULL";
			cursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
					new String[] { Browser.BookmarkColumns.TITLE }, selection, null, null);
			count = cursor.getCount();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return count;
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

	public static int calcValidBookmarkCount(List<BookMark> bookmarkList) {
		int count = 0;
		if (Util.isCollectionEmpty(bookmarkList)) {
			return 0;
		}
		for (BookMark bookmark : bookmarkList) {
			if (bookmark.isValidBookmark() && bookmark.getUrl() != null
					&& !bookmark.getUrl().equals("")) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_bookmark);
	}

}
