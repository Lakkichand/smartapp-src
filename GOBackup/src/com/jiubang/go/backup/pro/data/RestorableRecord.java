package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppRestoreEntry.AppRestoreType;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry.RestoreState;
import com.jiubang.go.backup.pro.data.IRestorable.RestoreArgs;
import com.jiubang.go.backup.pro.image.util.ImageRestoreEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.AppTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.BackupPropertiesConfig;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.sms.SmsRestore;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 恢复记录
 *
 * @author maiyongshen
 */
public class RestorableRecord extends BaseRecord {
	private Date mDate;
	private long mSpaceUsed = -1;
	// 当前备份文件的根目录
	private String mRecordRootDir;
	// 备份时的描述文件
	// private Properties mProp;
	// private BackupPropertiesConfig mConfig = null;
	// 数据是否可用，record的创建初始化分为两部分，第一步创建对象，
	// 第二步loaddata实际初始化数据，只有loaddata后，mDataAvailable才为true
	private boolean mDataAvailable = false;
	// 数据是否损坏
	private boolean mDataDamage = true;

	// private BackupDBHelper mDbHelper;
	private RecordDescribe mRecordDescribe;

	/**
	 * 恢复记录参数
	 *
	 * @author maiyongshen
	 */
	public static class RecordRestoreArgs extends RestoreArgs {
		public AppRestoreType mAppRestoreType = AppRestoreType.APP;
		public boolean mSilentRestoreApp;
	}

	public RestorableRecord(Context context, String restoreFilePath) {
		super(context);
		if (restoreFilePath != null) {
			mRecordRootDir = Util.ensureFileSeparator(restoreFilePath);
		}
		init();
	}

	private void init() {
		if (mRecordRootDir == null) {
			return;
		}

		/*
		 * //V1.11版本使用配置文件保存记录的一些额外信息，新版本使用数据库保存，做兼容 String backupPropName =
		 * mRecordRootDir + BackupPropertiesConfig.BACKUP_PROPERTIES_FILE_NAME;
		 * File backupPropFile = new File(backupPropName); if
		 * (backupPropFile.exists() && backupPropFile.length() > 0) { mConfig =
		 * new BackupPropertiesConfig(getContext(), backupPropName); mDataDamage
		 * = mConfig == null ? true : false; } if(mDataDamage){
		 * //V2.0版本后通过数据库保存属性信息 initRecordDecribe(); }
		 */

		// TODO 判断数据是否有效
		// mDataDamage = !new File(getRecordDbPath()).exists();
		// getBackupDBHelper(getContext());
		// //V2.0版本后通过数据库保存属性信息
		// initRecordDecribe();
		// releaseDBHelper();
		initRecordDecribe();
	}

	private void initRecordDecribe() {
		// 优先兼容旧版本，如果配置文件存在，从配置文件初始化
		if (initRecordDecribeFromConfig()) {
			mDataDamage = false;
		} else {
			BackupDBHelper dbHelper = getBackupDBHelper(getContext());
			mDataDamage = !initRecordDescribeFromDb(dbHelper);
			releaseDBHelper(dbHelper);
		}

		// 获取日期
		updateDate();
	}

	private boolean initRecordDescribeFromDb(BackupDBHelper dbHelper) {
		if (!new File(getRecordDbPath()).exists()) {
			return false;
		}
		if (dbHelper == null) {
			return false;
		}

		Cursor cursor = null;
		try {
			String where = DataTable.MIME_TYPE + "=?";
			cursor = dbHelper.query(DataTable.TABLE_NAME, null, where,
					new String[] { String.valueOf(MimetypeTable.MIMETYPE_VALUE_CONFIG) }, null);
			if (cursor == null || cursor.getCount() < 0) {
				return false;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		try {
			cursor = dbHelper.query(DataTable.TABLE_NAME, null, null, null, null);
			if (cursor == null || !cursor.moveToFirst()) {
				return false;
			}
			initRecordDescribe(cursor);
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		if (mRecordDescribe.mBackupDate == 0) {
			return false;
		}
		return true;
	}

	private void initRecordDescribe(Cursor cursor) {
		mRecordDescribe = new RecordDescribe();
		do {
			try {
				int mimeType = cursor.getInt(cursor.getColumnIndex(DataTable.MIME_TYPE));
				switch (mimeType) {
					case MimetypeTable.MIMETYPE_VALUE_CONFIG :
						mRecordDescribe.mSoftwareVersion = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA1));
						mRecordDescribe.mSoftwareVersionName = cursor.getString(cursor
								.getColumnIndex(DataTable.DATA2));
						mRecordDescribe.mOsVersion = cursor.getString(cursor
								.getColumnIndex(DataTable.DATA3));
						mRecordDescribe.mDatabaseVersion = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA4));
						mRecordDescribe.mBackupDate = cursor.getLong(cursor
								.getColumnIndex(DataTable.DATA5));
						mRecordDescribe.mIsRoot = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA6)) == 1 ? true : false;
						mRecordDescribe.mAppCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA7));
						mRecordDescribe.mSystemItemCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA8));
						mRecordDescribe.mSpaceUsed = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA9));
						break;

					case MimetypeTable.MIMETYPE_VALUE_CALLLOG :
						mRecordDescribe.mCallLogCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA2));
						break;

					case MimetypeTable.MIMETYPE_VALUE_CONTACT :
						mRecordDescribe.mContactCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA2));
						break;

					case MimetypeTable.MIMETYPE_VALUE_MMS :
						mRecordDescribe.mMmsCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA2));
						break;

					case MimetypeTable.MIMETYPE_VALUE_SMS :
						mRecordDescribe.mSmsCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA2));
						break;

					case MimetypeTable.MIMETYPE_VALUE_BOOKMARK :
						mRecordDescribe.mBookMarkCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA2));
						break;

					case MimetypeTable.MIMETYPE_VALUE_WIFI :
						mRecordDescribe.mWifiPath = cursor.getString(cursor
								.getColumnIndex(DataTable.DATA2));
						break;

					case MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY :
						mRecordDescribe.mUserDictionaryWordCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA2));
						break;

					case MimetypeTable.MIMETYPE_VALUE_RINGTONE :
						mRecordDescribe.mRingtoneCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA1));
						break;

					case MimetypeTable.MIMETYPE_VALUE_CALENDAR :
						mRecordDescribe.mCalendarEventCount = cursor.getInt(cursor
								.getColumnIndex(DataTable.DATA2));
						break;
					case MimetypeTable.MIMETYPE_VALUE_IMAGE :
						mRecordDescribe.mImageCount++;
						break;

					case MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING :
					case MimetypeTable.MIMETYPE_VALUE_APP :
					case MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA :
						break;
					default :
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (cursor.moveToNext());
	}

	private boolean initRecordDecribeFromConfig() {
		String backupPropName = mRecordRootDir + BackupPropertiesConfig.BACKUP_PROPERTIES_FILE_NAME;
		File backupPropFile = new File(backupPropName);
		if (!backupPropFile.exists() || backupPropFile.length() == 0) {
			return false;
		}

		BackupPropertiesConfig config = new BackupPropertiesConfig(getContext(),
				backupPropFile.getAbsolutePath());

		String value = null;
		mRecordDescribe = new RecordDescribe();
		mRecordDescribe.mAppCount = (value = config
				.get(BackupPropertiesConfig.P_BACKUP_APP_ITEM_COUNT)) == null ? 0 : Integer
				.valueOf(value);
		mRecordDescribe.mBackupDate = (value = config.get(BackupPropertiesConfig.P_BACKUP_TIME)) == null
				? 0
				: Long.valueOf(value);
		mRecordDescribe.mCallLogCount = (value = config
				.get(BackupPropertiesConfig.P_BACKUP_CALLLOG_COUNT)) == null ? 0 : Integer
				.valueOf(value);
		mRecordDescribe.mContactCount = (value = config
				.get(BackupPropertiesConfig.P_BACKUP_CONTACTS_COUNT)) == null ? 0 : Integer
				.valueOf(value);
		mRecordDescribe.mDatabaseVersion = (value = config
				.get(BackupPropertiesConfig.P_DATABASE_VERSION)) == null ? 0 : Integer
				.valueOf(value);
		mRecordDescribe.mIsRoot = Boolean.valueOf(config.get(BackupPropertiesConfig.P_ISROOT));
		mRecordDescribe.mMmsCount = (value = config.get(BackupPropertiesConfig.P_BACKUP_MMS_COUNT)) == null
				? 0
				: Integer.valueOf(value);
		mRecordDescribe.mOsVersion = config.get(BackupPropertiesConfig.P_OS_VERSION);
		mRecordDescribe.mSmsCount = (value = config.get(BackupPropertiesConfig.P_BACKUP_SMS_COUNT)) == null
				? 0
				: Integer.valueOf(value);
		mRecordDescribe.mSoftwareVersion = (value = config
				.get(BackupPropertiesConfig.P_SOFTWARE_VERSION_CODE)) == null ? 0 : Integer
				.valueOf(value);
		mRecordDescribe.mSoftwareVersionName = config
				.get(BackupPropertiesConfig.P_SOFTWARE_VERSION_NAME);
		mRecordDescribe.mSpaceUsed = (value = config.get(BackupPropertiesConfig.P_BACKUP_SIZE)) == null
				? 0
				: Integer.valueOf(value);
		mRecordDescribe.mSystemItemCount = (value = config
				.get(BackupPropertiesConfig.P_BACKUP_SYSTEM_DATA_ITEM_COUNT)) == null ? 0 : Integer
				.valueOf(value);
		mRecordDescribe.mUserDictionaryWordCount = (value = config
				.get(BackupPropertiesConfig.P_BACKUP_DICTIONAY_WORD_COUNT)) == null ? 0 : Integer
				.valueOf(value);
		mRecordDescribe.mWifiPath = config.get(BackupPropertiesConfig.P_BACKUP_WIFI_PATH);
		//备份图片个数
		mRecordDescribe.mImageCount = (value = config
				.get(BackupPropertiesConfig.P_BACKUP_IMAGE_COUNT)) == null ? 0 : Integer
				.valueOf(value);
		if (mRecordDescribe.mBackupDate == 0) {
			return false;
		}
		return true;
	}

	public boolean needUpdate() {
		// 存在旧的propertie文件表示需要进行升级
		File backupPropFile = new File(mRecordRootDir
				+ BackupPropertiesConfig.BACKUP_PROPERTIES_FILE_NAME);
		return backupPropFile.exists();
	}

	public boolean updateRecordFromV11ToV20() {
		File backupPropFile = new File(mRecordRootDir
				+ BackupPropertiesConfig.BACKUP_PROPERTIES_FILE_NAME);
		if (!backupPropFile.exists()) {
			return false;
		}

		BackupDBHelper dbHelper = getBackupDBHelper(getContext());
		if (dbHelper == null) {
			return false;
		}
		boolean ret = true;
		BackupPropertiesConfig bpc = new BackupPropertiesConfig(getContext(),
				backupPropFile.getAbsolutePath());
		ret = dbHelper.updateRecordFromV11ToV20(bpc, mRecordRootDir);
		dbHelper.close();
		Util.copyFile(getContext().getDatabasePath(dbHelper.getCurDbName()).getAbsolutePath(),
				new File(getRecordRootDir(), BackupDBHelper.getDBName()).getAbsolutePath());
		getContext().deleteDatabase(dbHelper.getCurDbName());

		if (ret) {
			// 成功后删除原来旧的配置文件
			Util.deleteFile(Util.ensureFileSeparator(mRecordRootDir)
					+ BackupPropertiesConfig.BACKUP_PROPERTIES_FILE_NAME);
		}
		initRecordDecribe();
		return ret;
	}

	/**
	 * 数据是否损坏
	 *
	 * @return
	 */
	public boolean isDataDamage() {
		return mDataDamage;
	}

	private Date stringDateToDate(String strDate) {
		if (strDate == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = null;
		try {
			date = sdf.parse(strDate);
		} catch (Exception e) {
			e.printStackTrace();
			date = null;
		}
		return date;
	}

	/*
	 * public BackupPropertiesConfig getBackupPropertiesConfig(){ return
	 * mConfig; }
	 */

	public String getRecordRootDir() {
		return mRecordRootDir;
	}

	public boolean isRoot() {
		boolean isRoot = false;
		if (mRecordDescribe != null) {
			return mRecordDescribe.mIsRoot;
		}

		/*
		 * //兼容V1.11之前版本 if(mConfig != null){ isRoot =
		 * mConfig.containsKey(BackupPropertiesConfig.P_ISROOT) ?
		 * Boolean.valueOf((String)
		 * mConfig.get(BackupPropertiesConfig.P_ISROOT)) : false; }
		 */
		return isRoot;
	}

	public int getSystemDataEntryCount() {
		int count = 0;
		if (dataAvailable()) {
			count = getGroupItemsCount(IRecord.GROUP_SYSTEM_DATA);
		} else if (mRecordDescribe != null) {
			count = mRecordDescribe.mSystemItemCount;
		} /*
			* else if (mConfig != null) { //兼容V1.11之前版本 count =
			* mConfig.containsKey(
			* BackupPropertiesConfig.P_BACKUP_SYSTEM_DATA_ITEM_COUNT) ?
			* Integer.valueOf((String)
			* mConfig.get(BackupPropertiesConfig.P_BACKUP_SYSTEM_DATA_ITEM_COUNT))
			* : 0; }
			*/
		return count;
	}

	public int getUserDataEntryCount() {
		int count = 0;
		if (dataAvailable()) {
			count = getGroupItemsCount(IRecord.GROUP_USER_DATA);
		} else if (mRecordDescribe != null) {
			count = mRecordDescribe.mSystemItemCount;
		} /*
			* else if (mConfig != null) { //兼容V1.11之前版本 count =
			* mConfig.containsKey(
			* BackupPropertiesConfig.P_BACKUP_SYSTEM_DATA_ITEM_COUNT) ?
			* Integer.valueOf((String)
			* mConfig.get(BackupPropertiesConfig.P_BACKUP_SYSTEM_DATA_ITEM_COUNT))
			* : 0; }
			*/
		return count;
	}

	public int getUserAppEntryCount() {
		int count = 0;
		if (dataAvailable()) {
			count = getGroupItemsCount(IRecord.GROUP_USER_APP);
		} else if (mRecordDescribe != null) {
			count = mRecordDescribe.mAppCount;
		} /*
			* else if (mConfig != null) { //兼容V1.11之前版本 count =
			* mConfig.containsKey(BackupPropertiesConfig.P_BACKUP_APP_ITEM_COUNT) ?
			* Integer.valueOf((String)
			* mConfig.get(BackupPropertiesConfig.P_BACKUP_APP_ITEM_COUNT)) : 0; }
			*/
		return count;
	}

	public int getSystemAppEntryCount() {
		int count = 0;
		if (dataAvailable()) {
			count = getGroupItemsCount(IRecord.GROUP_SYSTEM_APP);
		} else if (mRecordDescribe != null) {
			count = mRecordDescribe.mAppCount;
		} /*
			* else if (mConfig != null) { //兼容V1.11之前版本 count =
			* mConfig.containsKey(BackupPropertiesConfig.P_BACKUP_APP_ITEM_COUNT) ?
			* Integer.valueOf((String)
			* mConfig.get(BackupPropertiesConfig.P_BACKUP_APP_ITEM_COUNT)) : 0; }
			*/
		return count;
	}

	public int getContactsCount() {
		if (!hasContactsEntry()) {
			return 0;
		}

		// v2.0版本从数据库读取
		if (mRecordDescribe != null) {
			return mRecordDescribe.mContactCount;
		}
		return 0;

		// TODO 兼容更久以前版本
		/*
		 * // V1.11之前版本，直接读取属性文件的记录 String count = (String)
		 * mConfig.get(BackupPropertiesConfig.P_BACKUP_CONTACTS_COUNT); if
		 * (count != null) { return Integer.parseInt(count); } //
		 * 旧版本，扫描文件里面的联系人个数 int contactsCount =
		 * ContactsRestoreEntry.getContactsCount(mRecordRootDir); mConfig.put(
		 * BackupPropertiesConfig.P_BACKUP_CONTACTS_COUNT,
		 * String.valueOf(contactsCount)); mConfig.saveProper(getContext());
		 * return contactsCount;
		 */
	}

	public int getMessagesCount() {
		if (!hasSmsEntry()) {
			return 0;
		}

		if (mRecordDescribe != null) {
			return mRecordDescribe.mSmsCount;
		}
		return 0;

		// TODO 兼容更久以前版本
		/*
		 * // V1.11之前版本，直接读取属性文件的记录 String count = (String)
		 * mConfig.get(BackupPropertiesConfig.P_BACKUP_SMS_COUNT); if (count !=
		 * null) { return Integer.parseInt(count); } // 旧版本，扫描短信文件里面的短信数量 int
		 * msgCount = SmsRestoreEntry.GetSmsItemCount(mRecordRootDir); count =
		 * String.valueOf(msgCount); // 写入配置文件，避免下次进来再需要扫描文件
		 * mConfig.put(BackupPropertiesConfig.P_BACKUP_SMS_COUNT, count);
		 * mConfig.saveProper(getContext()); return msgCount;
		 */
	}

	public int getMMSCount() {
		if (!hasMmsEntry()) {
			return 0;
		}

		// V2.0版本从数据库读取
		if (mRecordDescribe != null) {
			return mRecordDescribe.mMmsCount;
		}
		return 0;
		/*
		 * //V1.11之前版本，从配置文件读取 String count = (String)
		 * mConfig.get(BackupPropertiesConfig.P_BACKUP_MMS_COUNT); return count
		 * != null ? Integer.parseInt(count) : 0;
		 */
	}

	public int getCallLogsCount() {
		if (!hasCallLogEntry()) {
			return 0;
		}
		// V2.0版本，从数据库读取
		if (mRecordDescribe != null) {
			return mRecordDescribe.mCallLogCount;
		}
		return 0;

		// TODO 兼容更久以前版本
		/*
		 * // V1.11之前版本，直接读取属性文件的记录 if(mConfig == null){ return 0; } String
		 * count = (String)
		 * mConfig.get(BackupPropertiesConfig.P_BACKUP_CALLLOG_COUNT); if (count
		 * != null) { return Integer.parseInt(count); } // 旧版本，扫描通话记录备份文件的记录数量
		 * int callLogCount =
		 * CallLogRestoreEntry.GetCallLogItemCount(mRecordRootDir); count =
		 * String.valueOf(callLogCount); //写入配置文件，避免下次进来再需要扫描文件
		 * mConfig.put(BackupPropertiesConfig.P_BACKUP_CALLLOG_COUNT, count);
		 * mConfig.saveProper(getContext()); return callLogCount;
		 */
	}

	public int getCalendarEventCount() {
		if (!hasCalendarBackupFile()) {
			return 0;
		}

		if (mRecordDescribe != null) {
			return mRecordDescribe.mCalendarEventCount;
		}
		return 0;
	}

	public int getBookMarkCount() {
		if (!hasBoorMarkBackupFile()) {
			return 0;
		}

		if (mRecordDescribe != null) {
			return mRecordDescribe.mBookMarkCount;
		}
		return 0;
	}

	public int getUserDictionaryWordCount() {
		if (!hasUserDictionaryEntry()) {
			return 0;
		}
		// V2.0版本，从数据库读取
		if (mRecordDescribe != null) {
			return mRecordDescribe.mUserDictionaryWordCount;
		}
		return 0;
		/*
		 * //v1.11之前版本，从配置文件读取 String count = (String)
		 * mConfig.get(BackupPropertiesConfig.P_BACKUP_DICTIONAY_WORD_COUNT); //
		 * 不扫描文件内的个数？ return count != null ? Integer.parseInt(count) : 0;
		 */
	}

	public int getRingtoneCount() {
		if (!hasRingtoneEntry()) {
			return 0;
		}
		if (mRecordDescribe != null) {
			return mRecordDescribe.mRingtoneCount;
		}
		return 0;
	}

	// record 默认不实际加载备份项数据，这个方法判断是否已经加载了数据
	public boolean dataAvailable() {
		return mDataAvailable;
	}

	/**
	 * 同步加载恢复记录的记录项
	 *
	 * @param ctx
	 * @return
	 */
	public boolean loadData(Context context) {
		if (context == null) {
			return false;
		}

		// 清空队列
		clear();
		BackupDBHelper dbHelper = getBackupDBHelper(getContext());
		addUserDataEntries(context);
		addSystemDataEntries(context, dbHelper);
		addUserAppEntries(context, dbHelper);
		addSystemAppEntries();

		// 排序
		sortEntries(SORT_TYPE.SORT_BY_APP_NAME);

		// 把不可恢复的项移到末尾
		Iterator<List<BaseEntry>> listIter = getRecordDataIterator();
		while (listIter.hasNext()) {
			List<BaseEntry> list = listIter.next();
			Collection<BaseEntry> entriesToMove = new ArrayList<BaseEntry>();
			Iterator<BaseEntry> it = list.iterator();
			while (it.hasNext()) {
				BaseEntry entry = it.next();
				if (entry instanceof BaseRestoreEntry && !((BaseRestoreEntry) entry).isRestorable()) {
					entriesToMove.add(entry);
					it.remove();
				}
			}
			list.addAll(entriesToMove);
		}

		releaseDBHelper(dbHelper);
		mDataAvailable = true;
		return true;
	}

	private void addUserDataEntries(Context context) {
		// 短信
		analysisSms();

		// 彩信
		analysisMms();

		// 联系人
		analyseContacts();

		// 通话记录
		analyseCallLog();

		//浏览器书签
		analyseBookMark();

		// 桌面
		analysisGoLauncher(context);

		// 用户词典
		analyseUserDictionary();
		// 日历
		analyseCalendar();
	}

	private void addSystemDataEntries(Context context, BackupDBHelper dbHelper) {
		// wifi
		analysisWifi(context);

		// LauncherData
		analysisLauncherData(context, dbHelper);

		// 铃声
		analyseRingtone();

		// 壁纸
		analysisWallpaper();
	}

	private void addUserAppEntries(Context context, BackupDBHelper dbHelper) {
		// 加载app
		analysisApp(context, dbHelper);
	}

	private void addSystemAppEntries() {

	}
	private void addImageEntries(Context context, BackupDBHelper dbHelper) {
		// 加载Image
		analyseImage(context, dbHelper);
	}

	/**
	 * 加载备份项数据，异步执行
	 *
	 * @param ctx
	 * @param listener
	 * @return 如果执行错误，直接返回false，否则返回true，加载结果通过异步借口返回
	 */
	public boolean loadData(final Context ctx, final IAsyncTaskListener listener) {
		if (ctx == null || listener == null) {
			return false;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (listener != null) {
					listener.onStart(null, null);
				}
				loadData(ctx);
				if (listener != null) {
					listener.onEnd(true, null, null);
				}
			}
		}).start();
		return true;
	}

	@Override
	public Date getDate() {
		return mDate;
	}

	public void updateDate() {
		// V2.0版本，从数据库读取时间
		if (mRecordDescribe != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mRecordDescribe.mBackupDate);
			mDate = calendar.getTime();
			return;
		}

		// TODO 兼容更久以前版本
		/*
		 * //V1.11之前版本从配置文件读取 if (mConfig == null) { return; } String date =
		 * (String) mConfig.get(BackupPropertiesConfig.P_BACKUP_TIME); if(date
		 * != null){ //V1.06版本以上才有这个字段 Calendar calendar =
		 * Calendar.getInstance(); calendar.setTimeInMillis(Long.valueOf(date));
		 * mDate = calendar.getTime(); return; } //兼容v1.06版本以下 String
		 * tempRootDir = mRecordRootDir.endsWith(File.separator) ?
		 * mRecordRootDir.substring(0, mRecordRootDir.length() - 2) :
		 * mRecordRootDir; date =
		 * tempRootDir.substring(tempRootDir.lastIndexOf(File.separator) + 1);
		 * mDate = stringDateToDate(date); if (mDate != null) { Calendar
		 * calendar = Calendar.getInstance(); calendar.setTime(mDate);
		 * mConfig.put(BackupPropertiesConfig.P_BACKUP_TIME,
		 * String.valueOf(calendar.getTimeInMillis()));
		 * mConfig.saveProper(getContext()); }
		 */
	}

	@Override
	public long getSpaceUsage() {
		// V2.0版本从数据库读取
		if (mRecordDescribe != null) {
			mSpaceUsed = mRecordDescribe.mSpaceUsed;
		}
		/*
		 * else{ //V1.11之前版本，从配置文件读取 if (mConfig != null) { mSpaceUsed =
		 * mConfig.containsKey(BackupPropertiesConfig.P_BACKUP_SIZE) ?
		 * Long.valueOf((String)
		 * mConfig.get(BackupPropertiesConfig.P_BACKUP_SIZE)) : 0; } }
		 */

		if (mSpaceUsed <= 0) {
			File curRecordFile = new File(mRecordRootDir);
			if (curRecordFile.exists()) {
				mSpaceUsed = Util.getFileSize(curRecordFile.getAbsolutePath());
				// TODO 更新数据库
				/*
				 * mConfig.put( BackupPropertiesConfig.P_BACKUP_SIZE,
				 * String.valueOf(mSpaceUsed));
				 */
			}
		}
		return mSpaceUsed;
	}

	@Override
	public String getDescription() {
		//		final Context context = getContext();
		//		final String desc = getSizeDescription() + "  " + getUserDataEntryCount()
		//				+ context.getString(R.string.record_details_user_data) + "  "
		//				+ getSystemDataEntryCount()
		//				+ context.getString(R.string.record_details_system_data) + "  "
		//				+ getUserAppEntryCount() + context.getString(R.string.record_details_user_app)
		//				+ "  " + getSystemAppEntryCount()
		//				+ context.getString(R.string.record_details_system_data);
		//		return desc;
		return "";
	}

	private String getSizeDescription() {
		return Util.formatFileSize(getSpaceUsage());
	}

	public List<BaseRestoreEntry> getEntriesByType(EntryType type) {
		if (type == null) {
			return null;
		}

		if (!dataAvailable()) {
			loadData(getContext());
		}

		List<BaseRestoreEntry> result = null;

		for (Iterator<List<BaseEntry>> recordIterator = getRecordDataIterator(); recordIterator
				.hasNext();) {
			List<BaseEntry> entries = recordIterator.next();
			for (Iterator<BaseEntry> it = entries.iterator(); it.hasNext();) {
				BaseEntry entry = it.next();
				if (entry.getType() != type) {
					continue;
				}
				if (result == null) {
					result = new ArrayList<BaseRestoreEntry>();
				}
				result.add((BaseRestoreEntry) entry);
			}
		}
		return result;
	}

	private boolean hasEntriesOfSomeType(EntryType type) {
		List<BaseRestoreEntry> result = getEntriesByType(type);
		return result != null && result.size() > 0;
	}

	public boolean areAllEntriesRestorable() {
		if (!dataAvailable()) {
			loadData(getContext());
		}

		for (Iterator<List<BaseEntry>> recordIterator = getRecordDataIterator(); recordIterator
				.hasNext();) {
			List<BaseEntry> entries = recordIterator.next();
			for (Iterator<BaseEntry> it = entries.iterator(); it.hasNext();) {
				BaseRestoreEntry entry = (BaseRestoreEntry) it.next();
				if (!entry.isRestorable()) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean exportContactsFile(Context context, String destFilePath) {
		if (destFilePath == null || !hasContactsBackupFile()) {
			return false;
		}
		File srcFile = ContactsRestoreEntry.getDecryptedBackupFile(mRecordRootDir);
		boolean ret = srcFile != null && srcFile.exists()
				&& Util.copyFileToSdCard(context, srcFile, destFilePath);
		if (srcFile != null && srcFile.exists()) {
			srcFile.delete();
		}
		return ret;
	}

	public boolean exportSmsFile(Context context, String destFilePath) {
		if (destFilePath == null || !hasSmsBackupFile()) {
			return false;
		}
		String smsFilePath = mRecordRootDir + SmsRestoreEntry.SMS_RESTORE_FILE_NAME;
		String destFileFullPath = Util.ensureFileSeparator(Util.getSdRootPathOnPreference(context))
				+ destFilePath;
		return SmsRestore.exportDatFileToSd(smsFilePath, destFileFullPath, Constant.getPassword());
	}

	private boolean hasContactsBackupFile() {
		File backupFile = ContactsRestoreEntry.getOrginalFile(new File(mRecordRootDir));
		return backupFile != null && backupFile.exists();
	}

	private boolean hasMmsBackupFile() {
		return MmsRestoreEntry.hasRestorableMmsFiles(new File(mRecordRootDir));
	}

	private boolean hasSmsBackupFile() {
		return SmsRestoreEntry.getOrginalFile(mRecordRootDir) != null ? true : false;
	}

	private boolean hasWallpaperBackupFile() {
		return WallpaperRestoreEntry.hasWallpaperBackupFile(mRecordRootDir);
	}

	private boolean hasCallLogBackupFile() {
		return CallLogRestoreEntry.getOrginalFile(mRecordRootDir) != null ? true : false;
	}

	private boolean hasBookMarkManagerFile() {
		return BookMarkRestoreEntry.getOrginalFile(mRecordRootDir) != null ? true : false;
	}

	private boolean hasCalendarBackupFile() {
		return CalendarRestoreEntry.getOrginalFile(mRecordRootDir) != null ? true : false;
	}

	private boolean hasBoorMarkBackupFile() {
		return BookMarkRestoreEntry.getOrginalFile(mRecordRootDir) != null ? true : false;

	}

	private boolean hasGoLauncherSettingBackupFile() {
		return GoLauncherSettingRestoreEntry.hasOrginalFile(mRecordRootDir);
	}

	private boolean hasWifiBackupFile() {
		return WifiRestoreEntry.getOrginalFile(mRecordRootDir) != null ? true : false;
	}

	private boolean hasUserDictionaryFile() {
		File backupFile = UserDictionaryRestoreEntry.getOrginalFile(new File(mRecordRootDir));
		return backupFile != null && backupFile.exists();
	}

	private boolean hasRingtoneManagerFile() {
		return RingtoneRestoreEntry.hasRestorableRingtoneFiles(mRecordRootDir);
	}

	private boolean hasLauncherDataFile() {
		return LauncherDataRestoreEntry.hasLauncherDataBackupFile(mRecordRootDir);
	}

	public boolean hasContactsEntry() {
		if (!dataAvailable()) {
			return hasContactsBackupFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_USER_CONTACTS);
	}

	public boolean hasSmsEntry() {
		if (!dataAvailable()) {
			return hasSmsBackupFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_USER_SMS);
	}

	public boolean hasMmsEntry() {
		if (!dataAvailable()) {
			return hasMmsBackupFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_USER_MMS);
	}

	public boolean hasCallLogEntry() {
		if (!dataAvailable()) {
			return hasCallLogBackupFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_USER_CALL_HISTORY);
	}

	public boolean hasCalendarEntry() {
		if (!dataAvailable()) {
			return hasCalendarBackupFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_USER_CALENDAR);
	}

	public boolean hasGoLauncherSettingEntry() {
		if (!dataAvailable()) {
			return hasGoLauncherSettingBackupFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_USER_GOLAUNCHER_SETTING);
	}

	public boolean hasWifiEntry() {
		if (!dataAvailable()) {
			return hasWifiBackupFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_SYSTEM_WIFI);
	}

	public boolean hasWallpaperEntry() {
		if (!dataAvailable()) {
			return hasWallpaperBackupFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_SYSTEM_WALLPAPER);
	}

	public boolean hasUserDictionaryEntry() {
		if (!dataAvailable()) {
			return hasUserDictionaryFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_USER_DICTIONARY);
	}

	public boolean hasLauncherDataEntry() {
		if (!dataAvailable()) {
			return hasLauncherDataFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_SYSTEM_LAUNCHER_DATA);
	}

	public boolean hasRingtoneEntry() {
		if (!dataAvailable()) {
			return hasRingtoneManagerFile();
		}
		return hasEntriesOfSomeType(EntryType.TYPE_SYSTEM_RINGTONE);
	}

	public BackupDBHelper getBackupDBHelper(Context context) {
		if (context == null) {
			return null;
		}

		BackupDBHelper dbHelper = null;
		String dbPath = getRecordDbPath();
		File dbFile = new File(dbPath);
		if (!dbFile.exists()) {
			// 数据库文件不存在，数据不完整
			return null;
		}

		final int m7 = 7;
		boolean sdk7 = Util.getAndroidSystemVersion() <= m7;
		// if (sdk7) {
		// 拷贝数据库回到应用程序的目录,解决在2.1机器访问sd数据库出现问题的bug
		// String tempDbName = new Random().nextLong() + "_"
		// + BackupDBHelper.getDBName();
		// File internalDbFile = context.getDatabasePath(tempDbName);
		// String internalDbPath = internalDbFile != null ? internalDbFile
		// .getAbsolutePath() : Util.getDefaultDbPath(context,
		// tempDbName);
		// if (!Util.copyFile(dbFile.getAbsolutePath(), internalDbPath)) {
		// return null;
		// }
		// dbHelper = new BackupDBHelper(context, tempDbName,
		// BackupDBHelper.getDBVersion());
		// } else {
		dbHelper = new BackupDBHelper(context, dbPath, BackupDBHelper.getDBVersion());
		// }
		return dbHelper;
	}

	// TODO 这个方法与Record无关，应抽取为公用函数
	public void releaseDBHelper(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return;
		}
		dbHelper.close();
	}

	private String getRecordDbPath() {
		return Util.ensureFileSeparator(getRecordRootDir()) + BackupDBHelper.getDBName();
	}

	private boolean analysisApp(Context ctx, BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}

		List<AppInfo> appInfos = queryAllAppinfo(dbHelper);
		try {
			if (appInfos != null) {
				List<BaseEntry> appEntrys = new ArrayList<BaseEntry>();
				for (AppInfo data : appInfos) {
					AppRestoreEntry appEntry = new AppRestoreEntry(data, mRecordRootDir);
					appEntrys.add(appEntry);
				}
				addGroup(IRecord.GROUP_USER_APP, appEntrys);
				sortAppEntries(SORT_TYPE.SORT_BY_APP_NAME);
			}

			// 加入数据库升级，更新数据库
			/*
			 * if(mConfig != null){ int dbVersion =
			 * Integer.valueOf(String.valueOf
			 * (mConfig.get(BackupPropertiesConfig.P_DATABASE_VERSION)));
			 * if(dbVersion < BackupDBHelper.getDBVersion()){
			 * if(Util.copyFile(Util.getDefaultDbPath(ctx,
			 * BackupDBHelper.getDBName()), getRecordDbPath())){ //如果更新成功
			 * mConfig.put(BackupPropertiesConfig.P_DATABASE_VERSION,
			 * String.valueOf(BackupDBHelper.getDBVersion()));
			 * mConfig.saveProper(ctx); } } }
			 */
			if (mRecordDescribe != null) {
				if (mRecordDescribe.mDatabaseVersion < BackupDBHelper.getDBVersion()) {
					Util.copyFile(Util.getDefaultDbPath(ctx, BackupDBHelper.getDBName()),
							getRecordDbPath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean analyseImage(Context ctx, BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}
		try {
			ImageRestoreEntry restoreEntry = new ImageRestoreEntry(ctx, dbHelper, mRecordRootDir);
			addEntry(GROUP_USER_IMAGE, restoreEntry);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private List<AppInfo> queryAllAppinfo(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return null;
		}
		Cursor cursor = null;
		List<AppInfo> appDatas = new ArrayList<AppInfo>();
		try {
			cursor = dbHelper.query(AppTable.TABLE_NAME, null, null, null, null);
			if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				return null;
			}
			do {
				try {
					appDatas.add(new AppInfo(cursor));
				} catch (Exception e) {
				}
			} while (cursor.moveToNext());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return !Util.isCollectionEmpty(appDatas) ? appDatas : null;
	}

	private boolean analysisSms() {
		if (!hasSmsBackupFile()) {
			return false;
		}
		SmsRestoreEntry smsRestoreEntry = new SmsRestoreEntry(getContext(), mRecordRootDir);
		addEntry(RestorableRecord.GROUP_USER_DATA, smsRestoreEntry);
		return true;
	}

	private boolean analysisMms() {
		if (!hasMmsBackupFile()) {
			return false;
		}
		MmsRestoreEntry mmsRestoreEntry = new MmsRestoreEntry(getContext(), mRecordRootDir);
		addEntry(RestorableRecord.GROUP_USER_DATA, mmsRestoreEntry);
		return true;
	}

	private boolean analyseContacts() {
		if (!hasContactsBackupFile()) {
			return false;
		}
		BaseRestoreEntry contactsRestoreEntry = new ContactsRestoreEntry(getContext(),
				mRecordRootDir);
		addEntry(RestorableRecord.GROUP_USER_DATA, contactsRestoreEntry);
		return true;
	}

	private boolean analyseCallLog() {
		if (!hasCallLogBackupFile()) {
			return false;
		}
		BaseRestoreEntry callLogRestoreEntry = new CallLogRestoreEntry(getContext(), mRecordRootDir);
		addEntry(RestorableRecord.GROUP_USER_DATA, callLogRestoreEntry);
		return true;
	}

	private boolean analyseBookMark() {
		if (!hasBookMarkManagerFile()) {
			return false;
		}
		addEntry(RestorableRecord.GROUP_USER_DATA, new BookMarkRestoreEntry(getContext(),
				mRecordRootDir));
		return true;
	}

	private boolean analyseUserDictionary() {
		if (!hasUserDictionaryFile()) {
			return false;
		}
		addEntry(IRecord.GROUP_USER_DATA, new UserDictionaryRestoreEntry(getContext(),
				mRecordRootDir));
		return true;
	}

	private boolean analyseCalendar() {
		if (!hasCalendarBackupFile()) {
			return false;
		}
		addEntry(IRecord.GROUP_USER_DATA, new CalendarRestoreEntry(getContext(), mRecordRootDir));
		return true;
	}

	private boolean analyseRingtone() {
		if (!hasRingtoneManagerFile()) {
			return false;
		}
		addEntry(IRecord.GROUP_SYSTEM_DATA, new RingtoneRestoreEntry(getContext(), mRecordRootDir));
		return true;
	}

	private boolean analysisGoLauncher(Context ctx) {
		//		PackageInfo golauncherPackageInfo = null;
		//		try {
		//			PackageManager pm = ctx.getPackageManager();
		//			golauncherPackageInfo = pm.getPackageInfo(
		//					GoLauncherSettingBackupEntry.GOLAUNCHER_PACKAGE_NAME, 0);
		//		} catch (NameNotFoundException e) {
		//			e.printStackTrace();
		//			golauncherPackageInfo = null;
		//		}
		//		if (golauncherPackageInfo == null) {
		//			// 没有安装go桌面程序，不允许恢复桌面设置项
		//			return false;
		//		}

		// File randomFile =
		// GoLauncherSettingRestoreEntry.getRandomFile(mRecordRootDir);
		// if (randomFile != null) {
		// String fileName = randomFile.getName();
		// String randomStr = fileName.substring(0, fileName
		// .indexOf(GoLauncherSettingBackupEntry.BACKUP_GOLAUNCHER));
		// BaseRestoreEntry golauncherRestoreEntry = new
		// GoLauncherSettingRestoreEntry(
		// getContext(), mRecordRootDir, randomStr);
		// addEntry(RestorableRecord.GROUP_SYSTEM, golauncherRestoreEntry);
		// return true;
		// }

		File golauncherFile = GoLauncherSettingRestoreEntry.getRandomFile(mRecordRootDir);
		if (golauncherFile != null) {
			BaseRestoreEntry golauncherRestoreEntry = new GoLauncherSettingRestoreEntry(
					getContext(), mRecordRootDir);
			addEntry(RestorableRecord.GROUP_USER_DATA, golauncherRestoreEntry);
			return true;
		}
		return false;
	}

	private void analysisWifi(Context context) {
		//		GoBackupApplication gapp = null;
		//		RootProcess rp = null;
		//		try {
		//			gapp = (GoBackupApplication) context.getApplicationContext();
		//			rp = gapp.getRootProcess();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		if (rp == null || !rp.isRootProcessValid()) {
		//			return;
		//		}

		File wifiFile = new File(mRecordRootDir, WifiBackupEntry.WIFI_BACKUP_NAME);
		if (!wifiFile.exists()) {
			return;
		}
		addEntry(RestorableRecord.GROUP_SYSTEM_DATA, new WifiRestoreEntry(getContext(),
				mRecordRootDir, mRecordDescribe.mWifiPath));
	}

	private boolean analysisWallpaper() {
		if (!WallpaperRestoreEntry.hasWallpaperBackupFile(mRecordRootDir)) {
			return false;
		}

		addEntry(RestorableRecord.GROUP_SYSTEM_DATA, new WallpaperRestoreEntry(getContext(),
				mRecordRootDir));
		return true;
	}

	private void analysisLauncherData(Context context, BackupDBHelper dbHelper) {
		//		long dt = System.currentTimeMillis();
		//		GoBackupApplication gapp = null;
		//		RootProcess rp = null;
		//		try {
		//			gapp = (GoBackupApplication) context.getApplicationContext();
		//			rp = gapp.getRootProcess();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		if (rp == null || !rp.isRootProcessValid()) {
		//			return;
		//		}
		//
		if (dbHelper == null) {
			return;
		}

		LauncherDataRestoreEntry entry = LauncherDataRestoreEntry.generateLauncherDataBackupEntry(
				context, dbHelper, mRecordRootDir);
		if (entry != null) {
			addEntry(RestorableRecord.GROUP_SYSTEM_DATA, entry);
		}
	}

	private void analysisAccount(Context context) {
		if (!RootShell.isRootValid()) {
			return;
		}

		File accountFile = new File(mRecordRootDir, AccountBackupEntry.ACCOUNT_FILE_NAME);
		if (!accountFile.exists()) {
			return;
		}
		addEntry(RestorableRecord.GROUP_SYSTEM_DATA, new AccountRestoreEntry());
	}

	/**
	 * 恢复记录后是否需要重启，必须在恢复之后调用，恢复之前调用不起作用
	 *
	 * @return
	 */
	public boolean shouldRebootAfterRestore() {
		if (getTotalEntriesCount() == 0) {
			return false;
		}

		for (Iterator<List<BaseEntry>> recordIterator = getRecordDataIterator(); recordIterator
				.hasNext();) {
			List<BaseEntry> entries = recordIterator.next();
			for (Iterator<BaseEntry> it = entries.iterator(); it.hasNext();) {
				BaseRestoreEntry entry = (BaseRestoreEntry) it.next();
				if (entry.getState() == RestoreState.RESTORE_SUCCESSFUL && entry.isNeedReboot()) {
					return true;
				}
			}
		}
		return false;
	}

	public void delete() {
		Util.deleteFile(getRecordRootDir());
	}

	public boolean isSmartMergedRecord() {
		String recordDir = getRecordRootDir();
		if (recordDir != null && recordDir.contains(BackupManager.SMART_MERGED_BACKUP)) {
			return true;
		}
		return false;
	}

	public boolean isScheduleRecord() {
		String recordDir = getRecordRootDir();
		if (recordDir != null && recordDir.contains(BackupManager.SCHEDULE_BACKUP)) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean shouldBeAdded(BaseEntry entry) {
		return entry instanceof BaseRestoreEntry;
	}

	@Override
	public boolean isEmpty() {
		if (!dataAvailable()) {
			loadData(getContext());
		}
		return super.isEmpty();
	}

	public RecordDescribe getRecordDescribe() {
		return mRecordDescribe;
	}

	// TODO 这种方法的效率很低，应该在Entry中增加一个获取日期的接口
	public Date getEntryBackupDate(BaseRestoreEntry entry) {
		if (entry == null) {
			return null;
		}
		BackupDBHelper dbHelper = getBackupDBHelper(getContext());
		Date date = dbHelper.getEntryBackupDate(entry);
		releaseDBHelper(dbHelper);
		return date;
	}

	/**
	 * 恢复描述
	 *
	 * @author maiyonogshen
	 */
	public static final class RecordDescribe implements Parcelable {
		public int mSmsCount;
		public int mMmsCount;
		public int mCallLogCount;
		public int mContactCount;
		public int mBookMarkCount;
		public int mAppCount;
		public int mUserDictionaryWordCount;
		public int mSpaceUsed;
		public int mSystemItemCount;
		public boolean mIsRoot;
		public long mBackupDate;
		public int mDatabaseVersion;
		public String mOsVersion;
		public int mSoftwareVersion;
		public String mSoftwareVersionName;
		public String mWifiPath;
		public int mRingtoneCount;
		public int mCalendarEventCount;
		public int mImageCount;

		public static final Parcelable.Creator<RecordDescribe> CREATOR = new Parcelable.Creator<RecordDescribe>() {
			@Override
			public RecordDescribe createFromParcel(Parcel source) {
				return new RecordDescribe(source);
			}

			@Override
			public RecordDescribe[] newArray(int size) {
				return new RecordDescribe[size];
			}
		};

		public RecordDescribe() {
			mSmsCount = 0;
			mMmsCount = 0;
			mCallLogCount = 0;
			mContactCount = 0;
			mBookMarkCount = 0;
			mAppCount = 0;
			mUserDictionaryWordCount = 0;
			mSpaceUsed = 0;
			mSystemItemCount = 0;
			mDatabaseVersion = 0;
			mSoftwareVersion = 0;
			mCalendarEventCount = 0;
			mRingtoneCount = 0;
			mImageCount = 0;
			mIsRoot = false;
			mBackupDate = 0;
			mOsVersion = null;
			mSoftwareVersionName = null;
			mWifiPath = null;
		}

		public RecordDescribe(Parcel parcel) {
			mSmsCount = parcel.readInt();
			mMmsCount = parcel.readInt();
			mCallLogCount = parcel.readInt();
			mContactCount = parcel.readInt();
			mBookMarkCount = parcel.readInt();
			mAppCount = parcel.readInt();
			mUserDictionaryWordCount = parcel.readInt();
			mSpaceUsed = parcel.readInt();
			mSystemItemCount = parcel.readInt();
			mDatabaseVersion = parcel.readInt();
			mSoftwareVersion = parcel.readInt();
			mCalendarEventCount = parcel.readInt();
			mRingtoneCount = parcel.readInt();
			mIsRoot = parcel.readInt() == 0 ? false : true;
			mBackupDate = parcel.readLong();
			mOsVersion = parcel.readString();
			mSoftwareVersionName = parcel.readString();
			mWifiPath = parcel.readString();
		}

		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(mSmsCount);
			dest.writeInt(mMmsCount);
			dest.writeInt(mCallLogCount);
			dest.writeInt(mContactCount);
			dest.writeInt(mBookMarkCount);
			dest.writeInt(mAppCount);
			dest.writeInt(mUserDictionaryWordCount);
			dest.writeInt(mSpaceUsed);
			dest.writeInt(mSystemItemCount);
			dest.writeInt(mDatabaseVersion);
			dest.writeInt(mSoftwareVersion);
			dest.writeInt(mCalendarEventCount);
			dest.writeInt(mRingtoneCount);
			dest.writeInt(mIsRoot ? 1 : 0);
			dest.writeLong(mBackupDate);
			dest.writeString(mOsVersion);
			dest.writeString(mSoftwareVersionName);
			dest.writeString(mWifiPath);
		}
	}
}
