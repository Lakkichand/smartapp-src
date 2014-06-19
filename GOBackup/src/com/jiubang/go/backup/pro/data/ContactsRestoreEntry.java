package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.lib.contacts.ContactsOperator;
import com.jiubang.go.backup.pro.lib.contacts.ContactsOperator.RestoreArg;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 联系人恢复项
 *
 * @author maiyongshen
 */
public class ContactsRestoreEntry extends BaseRestoreEntry {
	private static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
	private static final String TEMP_CONTACTS_BACKUP_FILE = "contact.temp";
	private Context mContext;
	private String mParentDir;
	private File mBackupFile;

	public ContactsRestoreEntry(Context context, String dirPath) {
		super();
		mContext = context;
		mParentDir = dirPath;
		File encrypedBackupFile = getOrginalFile(new File(mParentDir));
		if (encrypedBackupFile != null && encrypedBackupFile.exists()) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_CONTACTS;
	}

	@Override
	public long getSpaceUsage() {
		File backupFile = getBackupFile();
		return backupFile != null && backupFile.exists() ? backupFile.length() : 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.contacts) : "";
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = Util.loadIconFromPackageName(context, CONTACTS_PACKAGE_NAME);
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

	/**
	 * 返回值 如果参数不合法，返回false，上层需对此返回值作相应处理， 除此情况外，全部返回true，操作结束后需回调onEnd接口
	 */
	@Override
	public boolean restore(final Context context, Object data, final IAsyncTaskListener listener) {
		Context appContext = (context == null) ? (mContext == null ? null : mContext
				.getApplicationContext()) : context.getApplicationContext();
		if (appContext == null || !(data instanceof RestoreArgs)) {
			return false;
		}

		final File backupFile = getBackupFile();
		if (backupFile == null) {
			return false;
		}
		RestoreArg arg = new RestoreArg();
		arg.backupFile = backupFile;
		arg.discardDuplicateContacts = PreferenceManager.getInstance().getBoolean(context,
				PreferenceManager.KEY_DISCARD_DUPLICATE_CONTACTS, true);
		arg.displayPhotoDir = new File(mParentDir, ContactsBackupEntry.DISPLAY_PHOTO_DIR);
		ContactsOperator.getInstance().restoreContacts(appContext, arg, new IAsyncTaskListener() {
			@Override
			public void onStart(Object arg1, Object arg2) {
				if (listener != null) {
					listener.onStart(ContactsRestoreEntry.this, null);
				}
				setState(RestoreState.RESTORING);
			}

			@Override
			public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
				String tip = null;
				if (arg2 instanceof Integer && arg3 instanceof Integer) {
					tip = mContext != null ? mContext.getString(R.string.progress_detail,
							((Integer) arg2).intValue(), ((Integer) arg3).intValue()) : "";
				}
				if (listener != null) {
					listener.onProceeding(progress, ContactsRestoreEntry.this, tip, null);
				}
			}

			@Override
			public void onEnd(boolean success, Object arg1, Object arg2) {
				if (success) {
					setState(RestoreState.RESTORE_SUCCESSFUL);
					if (arg2 instanceof Integer[] && context instanceof Activity) {
						Integer[] result = (Integer[]) arg2;
						final String formatString = String.format(
								context.getString(R.string.contacts_restore_result), result[0],
								result[1]);
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(context, formatString, Toast.LENGTH_LONG).show();
							}
						});
					}
				} else {
					if (arg1 instanceof Boolean && (Boolean) arg1) {
						setState(RestoreState.RESTORE_CANCELED);
					} else {
						setState(RestoreState.RESTORE_ERROR_OCCURRED);
					}
				}

				// 删除解密出来的临时文件
				if (backupFile != null && backupFile.exists()) {
					backupFile.delete();
				}
				if (listener != null) {
					listener.onEnd(success, ContactsRestoreEntry.this, null);
				}
			}
		});

		return true;
	}

	@Override
	public void stopRestore() {
		ContactsOperator.getInstance().stopRestoreContacts();
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
		File srcFile = new File(dir, ContactsBackupEntry.BACKUP_CONTACTS_FILE_NAME_ENCRYPT);
		if (!srcFile.exists()) {
			// 不存在加密联系人文件，说明备份那文件为1.03版本以前备份的数据
			srcFile = new File(dir, ContactsBackupEntry.BACKUP_CONTACTS_FILE_NAME);
			if (!srcFile.exists()) {
				return null;
			}
		}
		return srcFile;
	}

	public static File getDecryptedBackupFile(String dir) {
		File srcFile = getOrginalFile(new File(dir));
		if (srcFile == null || !srcFile.exists()) {
			return null;
		}
		File tempFile = new File(dir, TEMP_CONTACTS_BACKUP_FILE);
		if (Util.decryptFile(srcFile, tempFile, Constant.getPassword())) {
			return tempFile;
		} else if (tempFile.exists()) {
			tempFile.delete();
		}
		return null;
	}

	public static int getContactsCount(String parentDir) {
		File decryptedFile = getDecryptedBackupFile(parentDir);
		if (decryptedFile == null || !decryptedFile.exists()) {
			return 0;
		}
		int count = ContactsOperator.scanContactsNumInVcard(decryptedFile);
		return count;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		final DrawableKey key = DrawableProvider.buildDrawableKey(CONTACTS_PACKAGE_NAME);
		final Drawable defaultDrawable = mContext.getResources().getDrawable(
				R.drawable.icon_contacts);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_CONTACT, null);
		File contactsFile = new File(recordRootPah,
				ContactsBackupEntry.BACKUP_CONTACTS_FILE_NAME_ENCRYPT);
		if (contactsFile.exists()) {
			contactsFile.delete();
		}
	}
}
