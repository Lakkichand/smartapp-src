package com.jiubang.go.backup.pro.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.UserDictionary;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.UserDictionaryRestoreEntry.WordStruct;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.EncryptDecrypt;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 用户字典备份项
 * 
 * @author wencan
 */
public class UserDictionaryBackupEntry extends BaseBackupEntry {
	public static final String USER_DICTIONARY_FILE_NAME = "UserDictionary.encrypt";
	private final Context mContext;
	private int mCount;
	private File mDestFile;

	public UserDictionaryBackupEntry(Context context) {
		super();
		mContext = context.getApplicationContext();
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
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.user_dictionary) : "";
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
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_user_dictionary);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	@Override
	public boolean backup(Context context, Object data, IAsyncTaskListener listener) {
		Context appContext = context != null ? context.getApplicationContext() : mContext != null
				? mContext.getApplicationContext()
				: null;
		if (appContext == null || !(data instanceof BackupArgs)) {
			return false;
		}
		setState(BackupState.BACKUPING);
		BackupArgs args = (BackupArgs) data;
		if (listener != null) {
			listener.onStart(UserDictionaryBackupEntry.this, null);
		}
		boolean result = backupInternal(appContext, args);
		if (result) {
			result = updateBackupDb(args.mDbHelper);
		}
		if (listener != null) {
			listener.onEnd(result, UserDictionaryBackupEntry.this,
					getUserDictionaryBackupFiles(args.mBackupPath));
		}
		return true;
	}

	private String[] getUserDictionaryBackupFiles(String rootDir) {
		if (rootDir == null) {
			return null;
		}
		if (mDestFile == null) {
			mDestFile = new File(rootDir, USER_DICTIONARY_FILE_NAME);
		}
		if (!mDestFile.exists()) {
			return null;
		}
		return new String[] { mDestFile.getAbsolutePath() };
	}

	private boolean backupInternal(Context context, BackupArgs args) {
		if (context == null || args == null || TextUtils.isEmpty(args.mBackupPath)) {
			return false;
		}

		File tempFile = writeToFile(getAllWords(context), args.mBackupPath);
		if (tempFile == null || !tempFile.exists()) {
			return false;
		}
		mDestFile = new File(args.mBackupPath, USER_DICTIONARY_FILE_NAME);
		boolean result = false;
		try {
			if (mDestFile.exists()) {
				mDestFile.delete();
			}
			mDestFile.createNewFile();
			EncryptDecrypt ed = new EncryptDecrypt();
			result = ed.encrypt(tempFile, mDestFile, Constant.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (tempFile != null) {
				tempFile.delete();
			}
			if (result) {
				setState(BackupState.BACKUP_SUCCESSFUL);
				/*
				 * if (args.mConfig != null) {
				 * args.mConfig.put(BackupPropertiesConfig
				 * .P_BACKUP_DICTIONAY_WORD_COUNT, Integer.toString(mCount)); }
				 */
			} else {
				mDestFile.delete();
				setState(BackupState.BACKUP_ERROR_OCCURRED);
			}
		}
		return result;
	}

	private boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY);
		cv.put(DataTable.DATA1, mDestFile.getName());
		cv.put(DataTable.DATA2, mCount);
		cv.put(DataTable.DATA14, new Date().getTime());
		return dbHelper.reflashDatatable(cv);
	}

	private File writeToFile(Cursor cursor, String dir) {
		if (cursor == null || cursor.getCount() <= 0) {
			return null;
		}
		File tempFile = new File(dir, "user_dictionary.temp");
		DataOutputStream dos = null;
		try {
			if (tempFile.exists()) {
				tempFile.delete();
			}
			tempFile.createNewFile();
			dos = new DataOutputStream(new FileOutputStream(tempFile));
			// 行数
			mCount = cursor.getCount();
			dos.writeInt(mCount);
			if (!cursor.moveToFirst()) {
				throw new Exception();
			}
			// 列数
			final int columnCount = cursor.getColumnCount();
			dos.writeInt(columnCount);
			do {
				for (int i = 0; i < columnCount; i++) {
					final String columnName = cursor.getColumnName(i);
					String columnValue = cursor.getString(i);
					if (columnValue == null) {
						columnValue = "";
					}
					dos.writeUTF(columnName);
					dos.writeUTF(columnValue);
				}
			} while (cursor.moveToNext());

		} catch (IOException e) {
			e.printStackTrace();
			tempFile = null;
		} catch (Exception e) {
			e.printStackTrace();
			tempFile = null;
		} finally {
			if (dos != null) {
				try {
					dos.close();
					dos = null;
				} catch (IOException e) {

				}
			}
			cursor.close();
		}
		return tempFile;
	}

	public static File saveWordsToFile(Set<WordStruct> words, File destDir) {
		if (Util.isCollectionEmpty(words) || destDir == null || !destDir.isDirectory()
				|| !destDir.exists()) {
			return null;
		}
		DataOutputStream dos = null;
		boolean result = false;
		File tempFile = new File(destDir, "user_dictionary.temp");
		try {
			dos = new DataOutputStream(new FileOutputStream(tempFile));
			dos.writeInt(words.size());
			dos.writeInt(WordStruct.COLUMN_COUNT);
			for (WordStruct word : words) {
				dos.writeUTF(UserDictionary.Words.WORD);
				dos.writeUTF(word.word);
				dos.writeUTF(UserDictionary.Words.FREQUENCY);
				dos.writeUTF(word.frequency);
				dos.writeUTF(UserDictionary.Words.LOCALE);
				dos.writeUTF(word.locale);
				dos.writeUTF(UserDictionary.Words.APP_ID);
				dos.writeUTF(word.appid);
			}
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (Exception e) {
				}
			}
		}
		File destFile = null;
		if (result) {
			destFile = new File(destDir, USER_DICTIONARY_FILE_NAME);
			EncryptDecrypt ed = new EncryptDecrypt();
			result = ed.encrypt(tempFile, destFile, Constant.getPassword());
		}
		if (tempFile.exists()) {
			tempFile.delete();
		}
		if (!result && destFile != null && destFile.exists()) {
			destFile.delete();
		}
		return destFile;
	}

	public static Cursor getAllWords(Context context) {
		if (context == null) {
			return null;
		}
		String[] projection = { UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY,
				UserDictionary.Words.LOCALE, UserDictionary.Words.APP_ID };
		Cursor cursor = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI,
				projection, null, null, UserDictionary.Words._ID + " ASC");
		return cursor;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_user_dictionary);
	}
}
