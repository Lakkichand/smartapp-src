package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.ContactsBackupEntry;
import com.jiubang.go.backup.pro.data.ContactsRestoreEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.lib.contacts.ContactStruct;
import com.jiubang.go.backup.pro.lib.contacts.ContactsOperator;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 联系人批量整合
 *
 * @author maiyongshen
 */
public class ContactsBatchMergeAction extends BatchMergeAction {
	private Set<ContactStruct> mContactsSet;
	private File mDestContactsFile;
	private Context mContext;
	private BackupDBHelper mDbHelper;
	private Date mLatestMergeRecordDate;

	public ContactsBatchMergeAction(Context context, RestorableRecord beMergedRecord,
			BackupDBHelper dbHelper, IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		mContext = context;
		mDbHelper = dbHelper;
		if (mBeMergedRecord != null) {
			mDestContactsFile = new File(mBeMergedRecord.getRecordRootDir(),
					ContactsBackupEntry.BACKUP_CONTACTS_FILE_NAME_ENCRYPT);
		}
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		String thisBackupFileMd5Code = MD5Util
				.getFileMd5Code(getOrginalContactsBackupFile(mBeMergedRecord));
		String otherBackupFileMd5Code = MD5Util
				.getFileMd5Code(getOrginalContactsBackupFile(toMergeRecord));
		// 与被合并的记录文件内容相同，不必要进行合并
		if (TextUtils.equals(thisBackupFileMd5Code, otherBackupFileMd5Code)) {
			return false;
		}
		for (MergeAction action : mMergeActions) {
			final String code = MD5Util
					.getFileMd5Code(getOrginalContactsBackupFile(action.mToMergeRecord));
			if (TextUtils.equals(code, otherBackupFileMd5Code)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		return new ContactsMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected void onPreprocessing() {
		mContactsSet = getContactsDataFromRecrod(mBeMergedRecord);
		if (mContactsSet == null) {
			mContactsSet = new HashSet<ContactStruct>();
		}

		for (MergeAction action : mMergeActions) {
			RestorableRecord toMergeRecord = action.mToMergeRecord;
			if (toMergeRecord != null) {
				Date date = toMergeRecord.getDate();
				if (mLatestMergeRecordDate == null) {
					mLatestMergeRecordDate = date;
					continue;
				}
				if (date != null && date.compareTo(mLatestMergeRecordDate) > 0) {
					mLatestMergeRecordDate = date;
				}
			}
		}
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
		boolean result = changed ? saveContactsToFile() : !isStopped();
		if (!result) {
			mDestContactsFile.delete();
		} else {
			/*
			 * BackupPropertiesConfig config =
			 * mBeMergedRecord.getBackupPropertiesConfig(); if (config != null
			 * && mContactsSet.size() > 0) {
			 * config.put(BackupPropertiesConfig.P_BACKUP_CONTACTS_COUNT,
			 * String.valueOf(mContactsSet.size()));
			 * config.saveProper(mContext); }
			 */

			// V2.0
			String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_CONTACT;
			ContentValues cv = new ContentValues();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CONTACT);
			cv.put(DataTable.DATA1, mDestContactsFile.getName());
			cv.put(DataTable.DATA2, mContactsSet.size());
			// 判断是否存在头像文件
			File displayPhotoDir = new File(mBeMergedRecord.getRecordRootDir(),
					ContactsBackupEntry.DISPLAY_PHOTO_DIR);
			if (displayPhotoDir.exists() && displayPhotoDir.isDirectory()) {
				File[] subFiles = displayPhotoDir.listFiles();
				if (subFiles != null && subFiles.length > 0) {
					// 在数据库中保存文件夹的名称
					cv.put(DataTable.DATA3, displayPhotoDir.getName());
				}
			}
			// if(mDbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0){
			// result = mDbHelper.insert(DataTable.TABLE_NAME, cv);
			// }
			result = mDbHelper.reflashDatatable(cv);
		}
		release();
		return result;
	}

	private File getOrginalContactsBackupFile(RestorableRecord record) {
		if (record == null) {
			return null;
		}
		return ContactsRestoreEntry.getOrginalFile(new File(record.getRecordRootDir()));
	}

	private File getDecryptedContactsBackupFile(RestorableRecord record) {
		if (record == null) {
			return null;
		}
		return ContactsRestoreEntry.getDecryptedBackupFile(record.getRecordRootDir());
	}

	private Set<ContactStruct> getContactsDataFromRecrod(RestorableRecord record) {
		return ContactsOperator.loadContactsFromVCard(getDecryptedContactsBackupFile(record));
	}

	private boolean saveContactsToFile() {
		boolean result = true;
		if (!Util.isCollectionEmpty(mContactsSet)) {
			File tempFile = new File(mBeMergedRecord.getRecordRootDir(), "contacts.temp");
			ContactsOperator.saveContactsToVCardFile(mContactsSet, tempFile);
			result = tempFile.exists()
					&& Util.encryFile(tempFile, mDestContactsFile, Constant.getPassword());
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}
		return result;
	}

	private void release() {
		if (mContactsSet != null) {
			mContactsSet.clear();
		}
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.contacts) : super
				.getDescription(context);
	}

	/**
	 * 联系人整合
	 *
	 * @author maiyongshen
	 */
	private class ContactsMergeAction extends MergeAction {
		private Date mToMergeRecordDate;
		private Set<ContactStruct> mToMergeContacts;
		private ExecutorService mThreadPool;
		private byte[] mLock = new byte[0];

		public ContactsMergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
				IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
			if (toMergeRecord != null) {
				mToMergeRecordDate = toMergeRecord.getDate();
			}
			mThreadPool = Executors.newFixedThreadPool(1);
		}

		private void loadContactsData() {
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					mToMergeContacts = getContactsDataFromRecrod(mToMergeRecord);
					if (!isStopped()) {
						synchronized (mLock) {
							mLock.notify();
						}
					}
				}
			});

			synchronized (mLock) {
				try {
					mLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		/**
		 * 返回值表示联系人数据的集合是否有发生变化
		 */
		@Override
		public boolean execute() {
			Log.d("GoBackup", "contacts merge action execute");
			if (mToMergeRecord == null) {
				return false;
			}
			long t = System.currentTimeMillis();
			//			Set<ContactStruct> toMergeContacts = getContactsDataFromRecrod(mToMergeRecord);
			loadContactsData();
			t = System.currentTimeMillis() - t;
			Log.d("GoBackup", "load contacts time = " + t);
			Log.d("GoBackup", "load contacts end");
			if (Util.isCollectionEmpty(mToMergeContacts)) {
				return false;
			}
			if (isStopped()) {
				return false;
			}
			if (Util.isCollectionEmpty(mContactsSet)) {
				mContactsSet = mToMergeContacts;
				for (ContactStruct contact : mToMergeContacts) {
					mergeDisplayPhoto(mToMergeRecord, contact);
				}
				return true;
			}
			Log.d("GoBackup", "contacts merging");
			boolean changed = false;
			for (ContactStruct cs : mToMergeContacts) {
				if (isStopped()) {
					break;
				}
				// Set中只存储联系人信息，不包含头像的比较
				if (!mContactsSet.contains(cs)) {
					mContactsSet.add(cs);
					mergeDisplayPhoto(mToMergeRecord, cs);
					changed = true;
				} else if (mToMergeRecordDate != null) {
					// 联系人信息相同，但头像信息可能不同，取最新日期的记录的头像文件
					if (mToMergeRecordDate.compareTo(mLatestMergeRecordDate) == 0) {
						mergeDisplayPhoto(mToMergeRecord, cs);
						if (cs.associatedGroups != null || cs.starred) {
							mergeLatestContactGroupAndStarred(cs);
						}
					} else {
						mergeContactGroupAndStarred(cs);
					}
				}
			}
			mToMergeContacts.clear();
			Log.d("GoBackup", "contacts merge action end");
			return changed;
		}

		//合并最新的
		private void mergeLatestContactGroupAndStarred(ContactStruct contact) {
			for (Iterator iter = mContactsSet.iterator(); iter.hasNext();) {
				ContactStruct contactInSet = (ContactStruct) iter.next();
				if (contactInSet.equals(contact)) {
					if (contact.associatedGroups != null) {
						contactInSet.associatedGroups = contact.associatedGroups;
					}
					if (contact.starred) {
						contactInSet.starred = true;
					}
					break;
				}
			}
		}
		//合并不是最新的
		private void mergeContactGroupAndStarred(ContactStruct contact) {
			for (Iterator iter = mContactsSet.iterator(); iter.hasNext();) {
				ContactStruct contactInSet = (ContactStruct) iter.next();
				if (contactInSet.equals(contact)) {
					if (contact.starred) {
						contactInSet.starred = true;
					}
					if (contactInSet.associatedGroups == null && contact.associatedGroups != null) {
						contactInSet.associatedGroups = contact.associatedGroups;
					}
					break;
				}
			}
		}

		private void mergeDisplayPhoto(RestorableRecord toMergeRecord, ContactStruct contact) {
			File displayPhotoDir = new File(toMergeRecord.getRecordRootDir(),
					ContactsBackupEntry.DISPLAY_PHOTO_DIR);
			if (!displayPhotoDir.exists() || !displayPhotoDir.isDirectory()) {
				return;
			}
			if (!TextUtils.isEmpty(contact.photoFileName)) {
				File photo = new File(displayPhotoDir, contact.photoFileName);
				if (!photo.exists()) {
					return;
				}
				File destPhotoDir = new File(mBeMergedRecord.getRecordRootDir(),
						ContactsBackupEntry.DISPLAY_PHOTO_DIR);
				if (!destPhotoDir.exists()) {
					destPhotoDir.mkdirs();
				}
				Util.copyFile(photo.getAbsolutePath(),
						new File(destPhotoDir, photo.getName()).getAbsolutePath());
			}
		}

		@Override
		public int getProgressWeight() {
			return 10;
		}

		@Override
		public void forceToStop() {
			super.forceToStop();
			if (mThreadPool != null) {
				Log.d("GoBackup", "shut down thread pool");
				mThreadPool.shutdownNow();
				synchronized (mLock) {
					mLock.notify();
				}
			}
		}
	}
}
