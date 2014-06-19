package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.lib.contacts.ContactsOperator;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.schedules.ContactCheckerSchedule;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 联系人备份项
 *
 * @author maiyongshen
 */
public class ContactsBackupEntry extends BaseBackupEntry {
	public static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
	public static final String BACKUP_CONTACTS_FILE_NAME = "contacts_exported.vcf";
	public static final String BACKUP_CONTACTS_FILE_NAME_ENCRYPT = "contact.encrypt";
	public static final String DISPLAY_PHOTO_DIR = "ContactsPhoto";
	private static final String TEMP_FILE_NAME = "contact.temp";

	private final Context mContext;
	private BackupArgs mBackupArgs = null;
	private int mContactsCount;

	public ContactsBackupEntry(Context context) {
		super();
		mContext = context;
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
		return 0;
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
	public boolean backup(Context context, final Object data, final IAsyncTaskListener listener) {
		Context appContext = (context == null) ? (mContext == null ? null : mContext
				.getApplicationContext()) : context.getApplicationContext();
		if (appContext == null || !(data instanceof BackupArgs) || listener == null) {
			return false;
		}

		mBackupArgs = (BackupArgs) data;

		String dir = ((BackupArgs) data).mBackupPath;
		if (dir == null || dir.equals("")) {
			return false;
		}
		dir = Util.ensureFileSeparator(dir);
		PreferenceManager pm = PreferenceManager.getInstance();
		ContactsOperator.BackupArg arg = new ContactsOperator.BackupArg();
		// 是否只备份有电话号码的联系人
		arg.ignoreContactsWithoutNumber = pm.getBoolean(appContext,
				PreferenceManager.KEY_ONLAY_BACKUP_CONTACT_HAS_NUMBER, true);
		// 是否备份联系人头像
		arg.backupContactsAvatar = pm.getBoolean(appContext,
				PreferenceManager.KEY_BACKUP_CONTACTS_PHOTO, true);
		arg.parentDir = dir;
		arg.backupFileName = TEMP_FILE_NAME;
		arg.displayPhotoDir = DISPLAY_PHOTO_DIR;
		ContactsOperator.getInstance().backupContacts(context, arg, new IAsyncTaskListener() {
			@Override
			public void onStart(Object arg1, Object arg2) {
				listener.onStart(null, null);
				setState(BackupState.BACKUPING);
			}

			@Override
			public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
				String tip = null;
				if (arg2 instanceof Integer && arg3 instanceof Integer) {
					tip = mContext != null ? mContext.getString(R.string.progress_detail,
							((Integer) arg2).intValue(), ((Integer) arg3).intValue()) : "";
				}
				listener.onProceeding(progress, ContactsBackupEntry.this, tip, null);
			}

			@Override
			public void onEnd(boolean success, Object arg1, Object arg2) {
				if (success) {
					// 备份成功，加密vcf文件
					if (!encryptVcfFile()) {
						setState(BackupState.BACKUP_ERROR_OCCURRED);
					} else {
						if (arg2 instanceof Integer) {
							// 保存联系人备份个数
							mContactsCount = (Integer) arg2;
							success = updateBackupDb(((BackupArgs) data).mDbHelper);
						}
						setState(success
								? BackupState.BACKUP_SUCCESSFUL
								: BackupState.BACKUP_ERROR_OCCURRED);
					}
				} else {
					if (arg1 instanceof Boolean) {
						boolean hasCanceled = (Boolean) arg1;
						if (hasCanceled) {
							setState(BackupState.BACKUP_CANCELED);
						} else {
							setState(BackupState.BACKUP_ERROR_OCCURRED);
						}
					}
				}

				//更新Preference缓存，用于匹配联系人是否改变
				if (success) {
					ContactCheckerSchedule schedule = ContactCheckerSchedule.getInstance(mContext
							.getApplicationContext());
					schedule.reflashContactToPreference();
				}

				listener.onEnd(success, ContactsBackupEntry.this,
						getContactsBackupFiles(mBackupArgs.mBackupPath));
			}
		});

		return true;
	}

	private String[] getContactsBackupFiles(String dir) {
		if (dir == null) {
			return null;
		}
		List<String> paths = new ArrayList<String>();
		File contactsFile = new File(dir, BACKUP_CONTACTS_FILE_NAME_ENCRYPT);
		if (contactsFile.exists()) {
			paths.add(contactsFile.getAbsolutePath());
		}

		File displayPhotoDir = new File(dir, DISPLAY_PHOTO_DIR);
		if (displayPhotoDir.exists() && displayPhotoDir.isDirectory()) {
			paths.add(displayPhotoDir.getAbsolutePath());
		}

		if (!Util.isCollectionEmpty(paths)) {
			return paths.toArray(new String[paths.size()]);
		}

		return null;
	}

	private boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CONTACT);
		cv.put(DataTable.DATA1, BACKUP_CONTACTS_FILE_NAME_ENCRYPT);
		cv.put(DataTable.DATA2, mContactsCount);
		cv.put(DataTable.DATA14, new Date().getTime());

		File displayPhotoDir = new File(mBackupArgs.mBackupPath,
				ContactsBackupEntry.DISPLAY_PHOTO_DIR);
		if (displayPhotoDir.exists() && displayPhotoDir.isDirectory()) {
			File[] subFiles = displayPhotoDir.listFiles();
			if (subFiles != null && subFiles.length > 0) {
				// 在数据库中保存头像文件夹的名称
				cv.put(DataTable.DATA3, displayPhotoDir.getName());
			}
		}

		return dbHelper.reflashDatatable(cv);
		// if (dbHelper.update(DataTable.TABLE_NAME, cv, DataTable.MIME_TYPE +
		// "=" + MimetypeTable.MIMETYPE_VALUE_CONTACT, null) == 0) {
		// return dbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		// return true;
	}

	private boolean encryptVcfFile() {
		if (mBackupArgs == null) {
			return false;
		}
		String dir = Util.ensureFileSeparator(mBackupArgs.mBackupPath);
		if (dir == null) {
			return false;
		}
		try {
			File srcFile = new File(dir, TEMP_FILE_NAME);
			File descFile = new File(dir, BACKUP_CONTACTS_FILE_NAME_ENCRYPT);
			if (!Util.encryFile(srcFile, descFile, Constant.getPassword())) {
				// 加密失败,删除文件
				srcFile.delete();
				descFile.delete();
				return false;
			} else {
				// 删除源文件
				srcFile.delete();
			}
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		Drawable contactDrawable = null;
		final DrawableKey key = DrawableProvider.buildDrawableKey(CONTACTS_PACKAGE_NAME);
		final Drawable defaultDrawable = mContext.getResources().getDrawable(R.drawable.icon_contacts);
		contactDrawable = DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable,
				listener);
		return contactDrawable;
	}
}
