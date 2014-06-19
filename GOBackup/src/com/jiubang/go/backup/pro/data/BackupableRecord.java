package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.content.Context;

import com.jiubang.go.backup.pro.data.AppBackupEntry.AppBackupType;
import com.jiubang.go.backup.pro.data.IBackupable.BackupArgs;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author kevin 备份记录，描述每一次备份的详细情况
 */
public class BackupableRecord extends BaseRecord {
	/**
	 * 恢复参数
	 *
	 * @author wencan
	 */
	public static class RecordBackupArgs extends BackupArgs {
		public AppBackupType mAppBackupType = AppBackupType.APK_DATA;
	}

	private final static String LOG_TAG = "BackupableRecord";
	public static final String BACKUP_DIR_NAME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	private Date mDate;
	private String mDescription = null;

	private String mFullBackupPath;

	public BackupableRecord(Context context, String rootDir) {
		super(context);
		mDate = new Date();
		mFullBackupPath = rootDir;
	}

	public BackupableRecord(Context context) {
		super(context);
		mDate = new Date();
		String rootPath = Util.ensureFileSeparator(Util.getDefalutValidBackupRootPath(context));
		mFullBackupPath = rootPath + Constant.BACKUP_RES_ROOT_DIR + File.separator
				+ new SimpleDateFormat(BACKUP_DIR_NAME_FORMAT).format(getDate());
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	@Override
	public Date getDate() {
		if (mDate == null) {
			mDate = new Date();
		}
		return mDate;
	}

	@Override
	public long getSpaceUsage() {
		long totalSize = 0;
		for (Iterator<List<BaseEntry>> recordIterator = getRecordDataIterator(); recordIterator
				.hasNext();) {
			List<BaseEntry> entries = recordIterator.next();
			for (Iterator<BaseEntry> it = entries.iterator(); it.hasNext();) {
				BaseEntry entry = it.next();
				totalSize += entry.getSpaceUsage();
			}
		}
		return totalSize;
	}

	@Override
	public String getDescription() {
		return mDescription;
	}

	public String getBackupPath() {
		return mFullBackupPath;
	}

	public void loadAppinfo(Context ctx) {
		if (ctx == null) {
			return;
		}
		new AppExtraInfoLoader(ctx, (List<BaseEntry>) getGroup(GROUP_USER_APP))
				.load(AppExtraInfoLoader.LOAD_SIZE);
	}

	public boolean areAllAppEntriesSizeInited() {
		List<AppBackupEntry> userAppEntries = (List<AppBackupEntry>) getGroup(GROUP_USER_APP);
		List<AppBackupEntry> systemAppEntries = (List<AppBackupEntry>) getGroup(GROUP_SYSTEM_APP);
		if (Util.isCollectionEmpty(userAppEntries) && Util.isCollectionEmpty(systemAppEntries)) {
			return true;
		}

		for (AppBackupEntry entry : userAppEntries) {
			if (!entry.hasInitSizeFinish()) {
				return false;
			}
		}
		for (AppBackupEntry entry : systemAppEntries) {
			if (!entry.hasInitSizeFinish()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean shouldBeAdded(BaseEntry entry) {
		return entry instanceof BaseBackupEntry;
	}
}
