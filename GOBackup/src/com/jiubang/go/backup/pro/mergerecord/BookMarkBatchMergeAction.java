package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.bookmark.BookMark;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;
import com.jiubang.go.backup.pro.data.BookMarkRestoreEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * <br>
 * 类描述:浏览器书签批量整合
 * 
 * @author jiangpeihe
 * @date [2012-10-15]
 */
public class BookMarkBatchMergeAction extends BatchMergeAction {
	private File mDescFile = null;
	private File mDestBookmarkFile;
	private Map<String, BookMark> mBookMarkMap;
	private BackupDBHelper mDbHelper;

	public BookMarkBatchMergeAction(RestorableRecord beMergedRecord,
			BackupDBHelper dbHelper, IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		if (mBeMergedRecord != null) {
			mDescFile = new File(mBeMergedRecord.getRecordRootDir(),
					BookMarkRestoreEntry.BOOKMARK_RESTORE_FILE_NAME);
			mDbHelper = dbHelper;
			mBookMarkMap = new LinkedHashMap<String, BookMark>();
			mDestBookmarkFile = new File(mBeMergedRecord.getRecordRootDir(),
					BookMarkBackupEntry.BOOKMARK_BACKUP_FILE_NAME);
		}

	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {

		return new BookMarkMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {

		String thisBackupFileMd5Code = MD5Util
				.getFileMd5Code(getOrginalBookMarkBackupFile(mBeMergedRecord));
		String otherBackupFileMd5Code = MD5Util
				.getFileMd5Code(getOrginalBookMarkBackupFile(toMergeRecord));
		// 与被合并的记录文件内容相同，不必要进行合并
		if (TextUtils.equals(thisBackupFileMd5Code, otherBackupFileMd5Code)) {
			return false;
		}
		for (MergeAction action : mMergeActions) {
			final String code = MD5Util
					.getFileMd5Code(getOrginalBookMarkBackupFile(action.mToMergeRecord));
			if (TextUtils.equals(code, otherBackupFileMd5Code)) {
				return false;
			}
		}
		return true;
	}

	private File getOrginalBookMarkBackupFile(RestorableRecord record) {
		if (record == null) {
			return null;
		}
		return BookMarkRestoreEntry.getOrginalFile(new File(record
				.getRecordRootDir()));
	}

	@Override
	protected void onPreprocessing() {
		List<BaseRestoreEntry> beBookMarkEntrys = mBeMergedRecord
				.getEntriesByType(EntryType.TYPE_USER_BOOKMARK);
		if (Util.isCollectionEmpty(beBookMarkEntrys)) {
			return;
		}

		mBookMarkMap = loadBookMarkFromDatFile(mBeMergedRecord
				.getRecordRootDir()
				+ BookMarkRestoreEntry.BOOKMARK_RESTORE_FILE_NAME);
	}

	@Override
	protected boolean onPostprocessing(boolean[] subActionResults) {
		if (mDbHelper == null) {
			return false;
		}

		boolean changed = false;
		if (subActionResults != null) {
			final int len = subActionResults.length;
			for (int i = 0; i < len; i++) {
				if (subActionResults[i]) {
					changed = true;
					break;
				}
			}
		}
		boolean result = changed ? saveBookmarkDatFile(mBookMarkMap, mDescFile)
				: true;
		// TODO 结果写入属性文件
		/*
		 * if(mBeMergedRecord != null &&
		 * mBeMergedRecord.getBackupPropertiesConfig() != null){
		 * BackupPropertiesConfig bpc =
		 * mBeMergedRecord.getBackupPropertiesConfig();
		 * bpc.put(BackupPropertiesConfig.P_BACKUP_SMS_COUNT,
		 * String.valueOf(mSmsStructs.size())); }
		 */

		int bookmarkCount = getBookmarkCount(mBookMarkMap);

		result = true;
		String where = DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_BOOKMARK;
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_BOOKMARK);
		cv.put(DataTable.DATA1, mDescFile.getName());
		cv.put(DataTable.DATA2, bookmarkCount);
		result = mDbHelper.reflashDatatable(cv);
		// if(mDbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0){
		// result = mDbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		release();
		return result;
	}

	private int getBookmarkCount(Map<String, BookMark> bookMarkMap) {
		int bookmarkcount = 0;
		for (Iterator iter = bookMarkMap.keySet().iterator(); iter.hasNext();) {
			Object key = iter.next();
			BookMark bookmark = bookMarkMap.get(key);
			if (bookmark.isValidBookmark()) {
				bookmarkcount++;
			}
		}
		return bookmarkcount;
	}

	private void release() {
		if (mBookMarkMap != null) {
			mBookMarkMap.clear();
		}
	}

	/**
	 * <br>
	 * 类描述:浏览器书签整合
	 * 
	 * @author jiangpeihe
	 * @date [2012-10-15]
	 */
	private class BookMarkMergeAction extends MergeAction {

		public BookMarkMergeAction(RestorableRecord beMergedRecord,
				RestorableRecord toMergeRecord, IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);

		}

		@Override
		public boolean execute() {
			if (mBeMergedRecord == null) {
				return false;
			}
			List<BaseRestoreEntry> beBookmarkEntrys = mToMergeRecord
					.getEntriesByType(EntryType.TYPE_USER_BOOKMARK);
			if (Util.isCollectionEmpty(beBookmarkEntrys)) {
				return false;
			}
			Map<String, BookMark> tempBookMarkMap = loadBookMarkFromDatFile(mToMergeRecord
					.getRecordRootDir()
					+ BookMarkRestoreEntry.BOOKMARK_RESTORE_FILE_NAME);
			if (tempBookMarkMap == null || tempBookMarkMap.size() <= 0) {
				return false;
			}

			if (mBookMarkMap == null || mBookMarkMap.size() <= 0) {
				mBookMarkMap = tempBookMarkMap;
				return true;
			}
			boolean change = false;
			for (Iterator iter = tempBookMarkMap.keySet().iterator(); iter
					.hasNext();) {
				String bookKey = (String) iter.next();
				if (!mBookMarkMap.containsKey(bookKey)) {
					mBookMarkMap.put(bookKey, tempBookMarkMap.get(bookKey));
					change = true;
				}
			}
			tempBookMarkMap.clear();
			return change;

		}

		@Override
		public int getProgressWeight() {
			return 5;
		}

	}

	public static Map<String, BookMark> loadBookMarkFromDatFile(String path) {
		Map<String, BookMark> bookMarkMap = new LinkedHashMap<String, BookMark>();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			try {
				ObjectInputStream ois = new ObjectInputStream(fis);
				try {
					Object obj = null;
					while ((obj = ois.readObject()) != null) {
						BookMark bookmark = (BookMark) obj;
						String bookmarkKey = bookmark.getUrl()
								+ bookmark.getTitle();
						if (bookmark.getUrl() == null
								|| bookmark.getUrl().equals("")
								|| !bookmark.getBookmark().equals("1")) {
							bookmarkKey = bookmarkKey + bookmark.getCreated();
						}
						if (!bookMarkMap.containsKey(bookmarkKey)) {
							bookMarkMap.put(bookmarkKey, bookmark);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					// 文件不存在
				} catch (StreamCorruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					ois.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bookMarkMap;
	}

	public static boolean saveBookmarkDatFile(
			Map<String, BookMark> mBookMarkMap, File targetFile) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		boolean ret = true;
		try {
			fos = new FileOutputStream(targetFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ret = false;
		}
		try {
			oos = new ObjectOutputStream(fos);
			for (Iterator iter = mBookMarkMap.keySet().iterator(); iter
					.hasNext();) {
				Object key = iter.next();
				BookMark bookmark = mBookMarkMap.get(key);
				oos.writeObject(bookmark);
			}
			oos.writeObject(null);
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.bookmark) : super
				.getDescription(context);
	}

}
