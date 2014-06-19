package com.jiubang.go.backup.pro.data;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.UserDictionary;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 用户字典恢复项
 * 
 * @author wencan
 */
public class UserDictionaryRestoreEntry extends BaseRestoreEntry {
	private Context mContext;
	private Object mLock = new byte[0];
	private boolean mStopRestoreFlag = false;
	private String mParentDir;
	private File mBackupFile;

	public UserDictionaryRestoreEntry(Context context, String parentDir) {
		super();
		mContext = context;
		mParentDir = parentDir;
		File encrypedBackupFile = getOrginalFile(new File(mParentDir));
		if (encrypedBackupFile != null && encrypedBackupFile.exists()) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	@Override
	public void stopRestore() {
		synchronized (mLock) {
			mStopRestoreFlag = true;
		}
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_DICTIONARY;
	}

	@Override
	public long getSpaceUsage() {
		File backupFile = getBackupFile();
		return backupFile != null && backupFile.exists() ? backupFile.length() : 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.user_dictionary) : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	private void reset() {
		mStopRestoreFlag = false;
	}

	public File getBackupFile() {
		if (mBackupFile == null || !mBackupFile.exists()) {
			mBackupFile = getDecryptedBackupFile(mParentDir);
		}
		return mBackupFile;
	}

	public static File getOrginalFile(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return null;
		}
		File srcFile = new File(dir, UserDictionaryBackupEntry.USER_DICTIONARY_FILE_NAME);
		if (!srcFile.exists()) {
			return null;
		}
		return srcFile;
	}

	public static File getDecryptedBackupFile(String dir) {
		File srcFile = getOrginalFile(new File(dir));
		if (srcFile == null || !srcFile.exists()) {
			return null;
		}
		File tempFile = new File(dir, "userDictionary.temp");
		if (Util.decryptFile(srcFile, tempFile, Constant.getPassword())) {
			return tempFile;
		} else if (tempFile.exists()) {
			tempFile.delete();
		}
		return null;
	}

	public static int getWordCount(String parentDir) {
		File decryptedFile = getDecryptedBackupFile(parentDir);
		if (decryptedFile == null || !decryptedFile.exists()) {
			return 0;
		}
		int count = 0;
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(decryptedFile));
			count = dis.readInt();
		} catch (IOException e) {
			e.printStackTrace();
			count = 0;
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (Exception e) {
				}
			}
		}
		if (decryptedFile.exists()) {
			decryptedFile.delete();
		}
		return count;
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		if (context == null || !(data instanceof RestoreArgs)) {
			return false;
		}
		setState(RestoreState.RESTORING);
		if (listener != null) {
			listener.onStart(UserDictionaryRestoreEntry.this, null);
		}
		reset();
		boolean result = restoreInternal(context, (RestoreArgs) data, listener);
		if (listener != null) {
			listener.onEnd(result, UserDictionaryRestoreEntry.this, null);
		}
		return true;
	}

	private boolean restoreInternal(Context context, RestoreArgs args, IAsyncTaskListener listener) {
		boolean result = false;
		final File backupFile = getBackupFile();
		if (backupFile != null && backupFile.exists()) {
			result = writeDataToDbFromFile(context, backupFile, listener);
		}
		if (backupFile != null && backupFile.exists()) {
			backupFile.delete();
		}
		if (result) {
			setState(RestoreState.RESTORE_SUCCESSFUL);
		} else if (mStopRestoreFlag) {
			setState(RestoreState.RESTORE_CANCELED);
		} else {
			setState(RestoreState.RESTORE_ERROR_OCCURRED);
		}
		return result;
	}

	private Set<String> getAllWordsFromDb(Context context) {
		if (context == null) {
			return null;
		}
		Cursor cursor = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI, null,
				null, null, UserDictionary.Words._ID + " ASC");
		Set<String> wordSet = null;
		if (cursor != null && cursor.getCount() > 0) {
			wordSet = new HashSet<String>();
			final int wordColumnIndex = cursor.getColumnIndex(UserDictionary.Words.WORD);
			if (cursor.moveToFirst()) {
				do {
					wordSet.add(cursor.getString(wordColumnIndex));
				} while (cursor.moveToNext());
			}
		}
		return wordSet;
	}

	private boolean writeDataToDbFromFile(Context context, File backupFile,
			IAsyncTaskListener listener) {
		if (context == null || backupFile == null || !backupFile.exists()) {
			return false;
		}
		Set<WordStruct> wordsFromFile = getAllWordsFromBackupFile(backupFile);
		if (wordsFromFile == null || wordsFromFile.size() < 1) {
			return false;
		}
		Set<String> wordsExisted = getAllWordsFromDb(context);
		boolean result = false;
		final int count = wordsFromFile.size();
		// 屏蔽批量添加的代码，在部分2.2以下的机型上无法正常工作，可添加进数据库，但是无法展示
		final int mMAXCONTACTSPERTIME = 30;
		final int m10 = 10;
		final int m50 = 50;
		int addCountPerTime = count / m10;
		if (addCountPerTime <= 0) {
			addCountPerTime = count;
		} else if (addCountPerTime > mMAXCONTACTSPERTIME) {
			addCountPerTime = mMAXCONTACTSPERTIME;
		}
		int index = 0;
		try {
			for (WordStruct wordStruct : wordsFromFile) {
				if (mStopRestoreFlag) {
					throw new IllegalStateException();
				}
				if (wordsExisted != null && wordsExisted.contains(wordStruct.word)) {
					continue;
				}
				// 使用标准接口进行逐条添加
				UserDictionary.Words.addWord(context, wordStruct.word,
						Integer.parseInt(wordStruct.frequency),
						UserDictionary.Words.LOCALE_TYPE_ALL);

				if (listener != null && index % addCountPerTime == 0) {
					String tip = context != null ? context.getString(R.string.progress_detail,
							index, count) : "";
					listener.onProceeding((float) index / (float) count,
							UserDictionaryRestoreEntry.this, tip, null);
					try {
						Thread.sleep(m50);
					} catch (InterruptedException e) {
					}
				}
				index++;
			}
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			if (wordsExisted != null) {
				wordsExisted.clear();
			}
			wordsFromFile.clear();
		}
		return result;
	}

	private static WordStruct buildWordStruct(DataInputStream dis, int columnCount) {
		WordStruct wordStruct = new WordStruct();
		try {
			for (int i = 0; i < columnCount; i++) {
				String columnName = dis.readUTF();
				String columnValue = dis.readUTF();
				if (columnName.equals(UserDictionary.Words.WORD)) {
					wordStruct.word = columnValue;
				} else if (columnName.equals(UserDictionary.Words.FREQUENCY)) {
					wordStruct.frequency = columnValue;
				} else if (columnName.equals(UserDictionary.Words.LOCALE)) {
					wordStruct.locale = columnValue;
				} else if (columnName.equals(UserDictionary.Words.APP_ID)) {
					wordStruct.appid = columnValue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return wordStruct;
	}

	/**
	 * WordStruct
	 * 
	 * @author wencan
	 */
	public static class WordStruct {
		public static final int COLUMN_COUNT = 4;
		public String word;
		public String frequency;
		public String locale;
		public String appid;

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof WordStruct)) {
				return false;
			}
			return TextUtils.equals(word, ((WordStruct) o).word);
		}

		@Override
		public int hashCode() {
			return word != null ? word.hashCode() : 0;
		}
	}

	// 批量添加在2.2以下的机型无法正常添加单词到用户词典，数据库正常写入，但是无法展示
	/*
	 * private boolean batchAddWords(Context context, List<WordStruct> words) {
	 * if (context == null) { return false; } if (words == null || words.size()
	 * <= 0) { return true; } ArrayList<ContentProviderOperation> ops = new
	 * ArrayList<ContentProviderOperation>(); final int wordCount =
	 * words.size(); for (int i = 0; i < wordCount; i++) { final WordStruct
	 * wordStruct = words.get(i);
	 * ops.add(ContentProviderOperation.newInsert(UserDictionary
	 * .Words.CONTENT_URI) .withValue(UserDictionary.Words.WORD,
	 * wordStruct.word) .withValue(UserDictionary.Words.FREQUENCY,
	 * wordStruct.frequency) .withValue(UserDictionary.Words.LOCALE,
	 * wordStruct.locale) .withValue(UserDictionary.Words.APP_ID,
	 * wordStruct.appid) .build()); } boolean result = false; try {
	 * ContentProviderResult[] results =
	 * context.getContentResolver().applyBatch(UserDictionary.AUTHORITY, ops);
	 * result = true; } catch (Exception e) { e.printStackTrace(); result =
	 * false; } finally { ops.clear(); ops = null; } return result; }
	 */

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_user_dictionary);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	public static Set<WordStruct> getAllWordsFromBackupFile(File backupFile) {
		if (backupFile == null || !backupFile.exists()) {
			return null;
		}
		DataInputStream dis = null;
		Set<WordStruct> result = new HashSet<WordStruct>();
		try {
			dis = new DataInputStream(new FileInputStream(backupFile));
			final int count = dis.readInt();
			final int columnCount = dis.readInt();
			for (int i = 0; i < count; i++) {
				WordStruct word = buildWordStruct(dis, columnCount);
				//				if (word != null) {
				result.add(word);
				//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.clear();
			result = null;
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (Exception e) {

				}
			}
		}
		return result;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_user_dictionary);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY, null);
		File userDictionaryFile = new File(recordRootPah,
				UserDictionaryBackupEntry.USER_DICTIONARY_FILE_NAME);
		if (userDictionaryFile.exists()) {
			userDictionaryFile.delete();
		}
	}
}
