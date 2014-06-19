package com.jiubang.go.backup.pro.model;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.calendar.CalendarOperator;
import com.jiubang.go.backup.pro.data.AppBackupEntry;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.BackupableRecord;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;
import com.jiubang.go.backup.pro.data.CalendarBackupEntry;
import com.jiubang.go.backup.pro.data.CallLogBackupEntry;
import com.jiubang.go.backup.pro.data.ContactsBackupEntry;
import com.jiubang.go.backup.pro.data.GoLauncherSettingBackupEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.LauncherDataBackupEntry;
import com.jiubang.go.backup.pro.data.MmsBackupEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.RingtoneBackupEntry;
import com.jiubang.go.backup.pro.data.SmsBackupEntry;
import com.jiubang.go.backup.pro.data.UserDictionaryBackupEntry;
import com.jiubang.go.backup.pro.data.WallpaperBackupEntry;
import com.jiubang.go.backup.pro.data.WifiBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageOperater;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider;
import com.jiubang.go.backup.pro.util.PackageUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份数据管理类
 * @author maiyongshen
 */
public class BackupManager {
	public static final String SMART_MERGED_BACKUP = "smart_merged_backup";
	public static final String SCHEDULE_BACKUP = "schedule_backup";

	// 普通备份记录个数不得超过10个
	private static final int MAX_NORMAL_RECORD_COUNT = 10;

	// 普通备份记录最大大小限制为200MB
	private static final int MAX_BACKUP_SIZE_LIMIT = 200 * 1024 * 1024;

	public static final String POSTFIX_ONLINE_BACKUP_DB_CACHE = "_backup_db_cache";
	public static final String UNDERSCORE = "_";

	private static final String[] PACKAGE_FILTER = new String[] { "com.jiubang.go.backup.ex",
			"com.jiubang.go.backup.pro", "com.jiubang.go.backup" };

	private static BackupManager sInstance = null;
	//普通备份的记录集合
	private List<IRecord> mRestoreRecords = null;
	//整合备份的记录集合
	private List<IRecord> mMergableRecords = null;
	//定时备份的记录集合
	private List<IRecord> mScheduleRecords = null;
	//新建备份的记录
	private BackupableRecord mBackupableRecord = null;

	private byte[] mLock = new byte[0];
	private ExecutorService mThreadPool;

	private Map<String, PackageInfo> mInstalledPackages = new HashMap<String, PackageInfo>();
	private Map<String, String> mApplicationNameCaches = new HashMap<String, String>();

	/**
	 * @author wencan
	 */
	public static class BackupType {
		// 备份类型，为了兼容V2.0以前版本，需要遗弃0,1,2
		public static final int BACKUP_TYPE_SYSTEM_DATA = 0x0004; // 系统数据
		public static final int BACKUP_TYPE_USER_DATA = 0x0008; // 用户数据
		public static final int BACKUP_TYPE_SYSTEM_APP = 0x0010; // 系统程序
		public static final int BACKUP_TYPE_USER_APP = 0x0020; // 用户程序
		public static final int BACKUP_TYPE_USER_IMAGE = 0x0040; //照片
		
		private static final int CLOUD_BACKUP_MASK = 0x1000;

		private int mBackupType;

		public BackupType(int backupType) {
			mBackupType = backupType;
			if (!isBackupTypeValid()) {
				throw new IllegalArgumentException("backupType is not a valid type");
			}
		}

		public int getBackupType() {
			return mBackupType;
		}

		public void copy(BackupType type) {
			if (type == null) {
				return;
			}
			mBackupType = type.getBackupType();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			if (o instanceof BackupType) {
				return false;
			}
			return this.mBackupType == ((BackupType) o).mBackupType;
		}

		public boolean isBackupTypeValid() {
			return isBackupSystemApp() | isBackupSystemData() | isBackupUserApp()
					| isBackupUserData();
		}

		public boolean isBackupSystemData() {
			return (mBackupType & BACKUP_TYPE_SYSTEM_DATA) > 0;
		}

		public boolean isBackupUserData() {
			return (mBackupType & BACKUP_TYPE_USER_DATA) > 0;
		}

		public boolean isBackupSystemApp() {
			return (mBackupType & BACKUP_TYPE_SYSTEM_APP) > 0;
		}

		public boolean isBackupImage() {
			return (mBackupType & BACKUP_TYPE_USER_IMAGE) > 0;
		}

		public boolean isBackupUserApp() {
			return (mBackupType & BACKUP_TYPE_USER_APP) > 0;
		}
		
		public boolean isCloudBackup() {
			return (mBackupType & CLOUD_BACKUP_MASK) > 0;
		}
		
		public BackupType enableCloudBackup() {
			mBackupType |= CLOUD_BACKUP_MASK;
			return this;
		}
		
		public BackupType disableCloudBackup() {
			mBackupType &= ~CLOUD_BACKUP_MASK;
			return this;
		}

		public void enableBackupType(int backupType) {
			mBackupType = mBackupType | backupType;
		}

		public void disableBackupType(int backupType) {
			mBackupType = mBackupType & (~backupType);
		}
	}

	private Comparator<IRecord> mRecordComparator = new Comparator<IRecord>() {
		// 降序
		@Override
		public int compare(IRecord paramT1, IRecord paramT2) {
			final Date date1 = paramT1.getDate();
			final Date date2 = paramT2.getDate();
			if (date1 == null && date2 == null) {
				return 0;
			} else if (date1 == null) {
				return 1;
			} else if (date2 == null) {
				return -1;
			}
			return date2.compareTo(date1);
		}
	};

	private BackupManager() {
		mThreadPool = Executors.newFixedThreadPool(3);
	}

	public synchronized static BackupManager getInstance() {
		if (sInstance == null) {
			sInstance = new BackupManager();
		}
		return sInstance;
	}

	/**
	 * 重新初始化，构建所有备份记录数据
	 *
	 * @param context
	 */
	public void init(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context cannot be null");
		}
		long t = System.currentTimeMillis();
		//先把之前内存的数据清理掉
		releaseRestoreRecords();
		//获取所有SD卡根路径
		String[] sdCardPaths = Util.getAllSdPath();
		//		if (sdCardPaths == null || sdCardPaths.length < 1) {
		//			// 若mount命令出现问题，则获取手机内置SD卡路径
		//			sdCardPaths = new String[1];
		//			sdCardPaths[0] = Util.getInternalSdPath();
		//		}

		// 修复4.2的rom有些升级后将原来的存储内容备份到0目录而导致不能识别之前的备份记录的bug
		final int androidVersion42 = 17;
		if (Util.getAndroidSystemVersion() >= androidVersion42) {
			//如果是4.2的系统，除了获取到的SD卡根路径外，还要把0目录的路径也添加上去
			if (sdCardPaths != null && sdCardPaths.length > 0) {
				List<String> sdcardPathArray = new ArrayList<String>();
				for (String path : sdCardPaths) {
					sdcardPathArray.add(path);
					sdcardPathArray.add(path + "0" + File.separator);
				}
				sdCardPaths = new String[sdcardPathArray.size()];
				sdCardPaths = sdcardPathArray.toArray(sdCardPaths);
			}
		}

		Context appContext = context.getApplicationContext();
		createRestoreRecords(appContext, sdCardPaths);
		createMergableRecords(appContext, sdCardPaths);
		createScheduleRecords(appContext, sdCardPaths);
		t = System.currentTimeMillis() - t;
//		LogUtil.d("BackupManager init time = " + t);
	}

	public BackupableRecord getBackupRecord() {
		return mBackupableRecord;
	}

	public BackupableRecord createScheduleBackupableRecord(final Context context,
			final BackupType backupType) {
		final String preferableBackupRootPath = Util.getDefalutValidBackupRootPath(context);
		File scheduleDir = new File(BackupManager.getBackupsResRootFile(preferableBackupRootPath),
				BackupManager.SCHEDULE_BACKUP);
		mBackupableRecord = new BackupableRecordBuilder(context, scheduleDir.getAbsolutePath())
				.createBackupRecord(backupType);
		return mBackupableRecord;
	}

	// 同步接口
	public BackupableRecord createBackupableRecord(final Context context,
			final BackupType backupType) {
		mBackupableRecord = new BackupableRecordBuilder(context).createBackupRecord(backupType);
		return mBackupableRecord;
	}

	// 异步接口
	public void createBackupableRecord(final Context context, final BackupType backupType,
			final CreateRecordListener listener) {
		releaseBackupableRecord();
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (listener != null) {
					listener.onStartCreateRecord();
				}
				BackupableRecord record = createBackupableRecord(context, backupType);
				if (listener != null) {
					listener.onFinishCreateRecord(record);
				}
			}
		});
	}

	public void releaseBackupableRecord() {
		if (mBackupableRecord != null) {
			mBackupableRecord.clear();
		}
		mBackupableRecord = null;
	}

	/**
	 * 更新Record内存对象队列，扫描新增记录或去除已不存在的记录
	 * 主要是防止使用第三方工具在程序运行过程中未移除SD卡的情况下添加新的备份记录或删除备份记录
	 *
	 * @param context
	 */
	public void updateRestoreRecords(Context context) {
		//		long t = System.currentTimeMillis();
		removeNotExistRecords();
		//		 t = System.currentTimeMillis() - t;
		//		 LogUtil.d("removeNotExistRecords time = " + t);
		//		 t = System.currentTimeMillis();
		scanNewRecords(context);
		//		t = System.currentTimeMillis() - t;
		//		LogUtil.d("updateRestoreRecords time = " + t);
	}

	public void scanNewRecords(Context context) {
		if (context == null) {
			return;
		}
		final Context appContext = context.getApplicationContext();
		File dir = new File(Util.getDefalutValidBackupRootPath(context),
				Constant.BACKUP_RES_ROOT_DIR);
		if (!dir.exists()) {
			return;
		}
		File[] subFiles = dir.listFiles();
		if (subFiles == null || subFiles.length < 1) {
			return;
		}
		Set<String> allRecordsPath = getAllRestoreRecordsPaths();
		final int count = subFiles.length;
		for (int i = 0; i < count; i++) {
			final String recordPath = Util.ensureFileSeparator(subFiles[i].getAbsolutePath());
			if (Util.isCollectionEmpty(allRecordsPath) || !allRecordsPath.contains(recordPath)) {
				RestorableRecord newRecord = new RestorableRecord(appContext, recordPath);
				addRestoreRecord(newRecord);
			}
		}
		if (allRecordsPath != null) {
			allRecordsPath.clear();
		}
	}

	private Set<String> getAllRestoreRecordsPaths() {
		int recordCount = mRestoreRecords != null ? mRestoreRecords.size() : 0;
		recordCount += mMergableRecords != null ? mMergableRecords.size() : 0;
		recordCount += mScheduleRecords != null ? mScheduleRecords.size() : 0;
		if (recordCount < 1) {
			return null;
		}
		Set<String> recordPaths = new HashSet<String>(recordCount);
		if (mRestoreRecords != null) {
			for (IRecord record : mRestoreRecords) {
				final RestorableRecord restoreRecord = (RestorableRecord) record;
				recordPaths.add(restoreRecord.getRecordRootDir());
			}
		}
		if (mMergableRecords != null) {
			for (IRecord record : mMergableRecords) {
				final RestorableRecord restoreRecord = (RestorableRecord) record;
				recordPaths.add(restoreRecord.getRecordRootDir());
			}
		}
		if (mScheduleRecords != null) {
			for (IRecord record : mScheduleRecords) {
				final RestorableRecord restoreRecord = (RestorableRecord) record;
				recordPaths.add(restoreRecord.getRecordRootDir());
			}
		}
		return recordPaths;
	}

	private void removeInvalidRecords(List<IRecord> records) {
		if (Util.isCollectionEmpty(records)) {
			return;
		}
		for (Iterator<IRecord> it = records.iterator(); it.hasNext();) {
			IRecord record = it.next();
			if (!isRecordExist((RestorableRecord) record)) {
				it.remove();
			}
		}
	}

	private void removeNotExistRecords() {
		removeInvalidRecords(mRestoreRecords);
		removeInvalidRecords(mMergableRecords);
		removeInvalidRecords(mScheduleRecords);
	}

	private boolean isRecordExist(RestorableRecord record) {
		File recordDir = new File(record.getRecordRootDir());
		return recordDir.exists();
	}
	/**
	 * <br>功能简述:初始化普通备份的记录的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param sdCardPaths 备份数据有可能存在的目录路径集合
	 */
	private void createRestoreRecords(Context context, String[] sdCardPaths) {
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		if (mRestoreRecords == null) {
			mRestoreRecords = new ArrayList<IRecord>();
		}
		mRestoreRecords.clear();
		for (String path : sdCardPaths) {
			//先查找备份的文件夹到底是在哪个文件目录底下
			File dir = getBackupsResRootFile(path);
			if (dir == null || !dir.exists()) {
				continue;
			}
			//找到目录后就进行解释
			List<IRecord> records = createRestoreRecords(context, dir);
			if (records != null && records.size() > 0) {
				mRestoreRecords.addAll(records);
			}
		}
		//按时间进行排序
		Collections.sort(mRestoreRecords, mRecordComparator);
	}
	/**
	 * <br>功能简述:初始化整合备份的记录的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param sdCardPaths
	 */
	private void createMergableRecords(Context context, String[] sdCardPaths) {
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		if (mMergableRecords == null) {
			mMergableRecords = new ArrayList<IRecord>();
		}
		mMergableRecords.clear();
		for (String sdCardPath : sdCardPaths) {
			File dir = new File(getBackupsResRootFile(sdCardPath), SMART_MERGED_BACKUP);
			if (!dir.exists()) {
				continue;
			}
			RestorableRecord record = new RestorableRecord(context, dir.getAbsolutePath());
			if (!record.isDataDamage()) {
				mMergableRecords.add(record);
			}
		}
	}
	/**
	 * <br>功能简述:初始化定时备份的记录的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param sdCardPaths
	 */
	private void createScheduleRecords(Context context, String[] sdCardPaths) {
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		if (mScheduleRecords == null) {
			mScheduleRecords = new ArrayList<IRecord>();
		}
		mScheduleRecords.clear();
		for (String sdCardPath : sdCardPaths) {
			File dir = new File(getBackupsResRootFile(sdCardPath), SCHEDULE_BACKUP);
			if (!dir.exists()) {
				continue;
			}
			RestorableRecord record = new RestorableRecord(context, dir.getAbsolutePath());
			if (!record.isDataDamage()) {
				mScheduleRecords.add(record);
			}
		}
	}
	/**
	 * <br>功能简述:从SD卡备份数据文件夹下，获取所有普通备份包文件夹路径的集合的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param root
	 * @return
	 */
	private String[] listAllBackupDirs(File root) {
		if (root == null || !root.exists() || !root.isDirectory()) {
			return null;
		}

		// File[] fileDirs = allBackupDir.listFiles();
		// 过滤“一键整合”的备份记录，暂时的解决方案
		File[] subDirs = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				//过滤掉整合备份和定时备份
				if (SMART_MERGED_BACKUP.equals(filename)) {
					return false;
				}
				if (SCHEDULE_BACKUP.equals(filename)) {
					return false;
				}
				return true;
			}
		});
		if (subDirs == null) {
			return null;
		}
		//确保这些路径都是文件夹，然后把路径返回
		ArrayList<String> allDirs = new ArrayList<String>();
		for (File file : subDirs) {
			if (file.isDirectory()) {
				allDirs.add(file.getAbsolutePath());
			}
		}
		return allDirs.toArray(new String[allDirs.size()]);
	}
	/**
	 * <br>功能简述:初始化普通备份的记录的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param rootFile 备份数据存在的目录路径
	 * @return
	 */
	private List<IRecord> createRestoreRecords(Context context, File rootFile) {
		if (context == null || rootFile == null || !rootFile.exists() || !rootFile.isDirectory()) {
			return null;
		}
		//找出备份数据文件夹下，所有普通备份的备份包路径
		String[] recordDirs = listAllBackupDirs(rootFile);
		if (recordDirs == null || recordDirs.length < 1) {
			return null;
		}
		//
		List<IRecord> records = new ArrayList<IRecord>();
		for (String dir : recordDirs) {
			RestorableRecord record = new RestorableRecord(context, dir);
			// 是否数据损坏，根据需求，如果数据损坏，不再列表上显示
			if (!record.isDataDamage()) {
				records.add(record);
			}
		}
		return records;
	}

	public void releaseAll() {
		releaseBackupableRecord();
		releaseRestoreRecords();
	}

	public void releaseRestoreRecords() {
		releaseNormalRecords();
		releaseSmartMergedRecords();
		releaseScheduleRecords();
	}

	private void releaseNormalRecords() {
		if (mRestoreRecords != null) {
			mRestoreRecords.clear();
			mRestoreRecords = null;
		}
	}

	private void releaseSmartMergedRecords() {
		if (mMergableRecords != null) {
			mMergableRecords.clear();
			mMergableRecords = null;
		}
	}

	private void releaseScheduleRecords() {
		if (mScheduleRecords != null) {
			mScheduleRecords.clear();
			mScheduleRecords = null;
		}
	}

	public List<IRecord> getAllRestoreRecords() {
		return mRestoreRecords;
	}

	//获得备份包个数
	public int getAllRestoreRecordsCount() {
		return mRestoreRecords != null ? mRestoreRecords.size() : 0;
	}
	public List<IRecord> getMergableRecords() {
		return mMergableRecords;
	}

	public List<IRecord> getScheduleRecords() {
		return mScheduleRecords;
	}

	public RestorableRecord getLatestRestoreRecord() {
		RestorableRecord latestRecord = null;
		long dateInMills = -1;
		if (mRestoreRecords != null) {
			for (IRecord record : mRestoreRecords) {
				Date date = record.getDate();
				if (date != null && date.getTime() > dateInMills) {
					dateInMills = date.getTime();
					latestRecord = (RestorableRecord) record;
				}
			}
		}
		if (mMergableRecords != null) {
			for (IRecord record : mMergableRecords) {
				Date date = record.getDate();
				if (date != null && date.getTime() > dateInMills) {
					dateInMills = date.getTime();
					latestRecord = (RestorableRecord) record;
				}
			}
		}
		if (mScheduleRecords != null) {
			for (IRecord record : mScheduleRecords) {
				Date date = record.getDate();
				if (date != null && date.getTime() > dateInMills) {
					dateInMills = date.getTime();
					latestRecord = (RestorableRecord) record;
				}
			}
		}
		return latestRecord;
	}

	public long getMaxBackupSizeLimit(Context context) {
		return Util.isInland(context) ? Long.MAX_VALUE : MAX_BACKUP_SIZE_LIMIT;
	}

	public int getMaxBackupCount() {
		return MAX_NORMAL_RECORD_COUNT;
	}

	public int getRecordCount() {
		int count = 0;
		count += mRestoreRecords != null ? mRestoreRecords.size() : 0;
		count += mMergableRecords != null ? mMergableRecords.size() : 0;
		count += mScheduleRecords != null ? mScheduleRecords.size() : 0;
		return count;
	}

	private RestorableRecord getRecordById(List<IRecord> records, long id) {
		if (Util.isCollectionEmpty(records)) {
			return null;
		}
		for (IRecord record : records) {
			if (record.getId() == id) {
				return (RestorableRecord) record;
			}
		}
		return null;
	}

	public RestorableRecord getRecordById(long id) {
		RestorableRecord result = null;
		result = getRecordById(mRestoreRecords, id);
		if (result == null) {
			result = getRecordById(mMergableRecords, id);
		}
		if (result == null) {
			result = getRecordById(mScheduleRecords, id);
		}
		return result;
	}

	private void addMergableRecord(RestorableRecord record) {
		if (record == null || record.isDataDamage()) {
			return;
		}
		if (mMergableRecords == null) {
			mMergableRecords = new ArrayList<IRecord>();
		}
		for (IRecord mergeRecord : mMergableRecords) {
			if (TextUtils.equals(((RestorableRecord) mergeRecord).getRecordRootDir(),
					record.getRecordRootDir())) {
				mMergableRecords.remove(mergeRecord);
				break;
			}
		}
		mMergableRecords.add(record);
	}

	private void addScheduleRecord(RestorableRecord record) {
		if (record == null || record.isDataDamage()) {
			return;
		}
		if (mScheduleRecords == null) {
			mScheduleRecords = new ArrayList<IRecord>();
		}
		for (IRecord scheduleRecord : mScheduleRecords) {
			if (TextUtils.equals(((RestorableRecord) scheduleRecord).getRecordRootDir(),
					record.getRecordRootDir())) {
				mScheduleRecords.remove(scheduleRecord);
				break;
			}
		}
		mScheduleRecords.add(record);
	}

	public void addRestoreRecord(RestorableRecord record) {
		if (record == null || record.isDataDamage()) {
			return;
		}
		if (record.getRecordRootDir().contains(SMART_MERGED_BACKUP)) {
			addMergableRecord(record);
			return;
		}
		if (record.getRecordRootDir().contains(SCHEDULE_BACKUP)) {
			addScheduleRecord(record);
			return;
		}
		if (mRestoreRecords == null) {
			mRestoreRecords = new ArrayList<IRecord>();
		}
		for (IRecord restoreRecord : mRestoreRecords) {
			if (TextUtils.equals(((RestorableRecord) restoreRecord).getRecordRootDir(),
					record.getRecordRootDir())) {
				mRestoreRecords.remove(restoreRecord);
				break;
			}
		}
		mRestoreRecords.add(record);
		Collections.sort(mRestoreRecords, mRecordComparator);
	}

	public boolean removeRecord(RestorableRecord record) {
		if (record == null) {
			return false;
		}

		if (mRestoreRecords != null && mRestoreRecords.remove(record)) {
			return true;
		}
		if (mMergableRecords != null && mMergableRecords.remove(record)) {
			return true;
		}
		if (mScheduleRecords != null && mScheduleRecords.remove(record)) {
			return true;
		}
		return false;
	}

	public boolean deleteRecord(RestorableRecord record) {
		if (record == null) {
			return false;
		}
		if (removeRecord(record)) {
			record.delete();
		}
		return false;
	}

	public void deleteRecordById(long id) {
		RestorableRecord record = getRecordById(id);
		deleteRecord(record);
	}

	public List<IRecord> getRecordsNeedToUpdate() {
		List<IRecord> recordsToUpdate = new ArrayList<IRecord>();
		if (mRestoreRecords != null) {
			for (IRecord record : mRestoreRecords) {
				if (((RestorableRecord) record).needUpdate()) {
					recordsToUpdate.add(record);
				}
			}
		}
		if (mMergableRecords != null) {
			for (IRecord record : mMergableRecords) {
				if (((RestorableRecord) record).needUpdate()) {
					recordsToUpdate.add(record);
				}
			}
		}
		if (mScheduleRecords != null) {
			for (IRecord record : mScheduleRecords) {
				if (((RestorableRecord) record).needUpdate()) {
					recordsToUpdate.add(record);
				}
			}
		}
		if (recordsToUpdate.size() > 0) {
			return recordsToUpdate;
		}
		return null;
	}

	public static File getBackupsResRootFile(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		if (!path.contains(Constant.GOBACKUP_ROOT_DIR)) {
			path = Util.ensureFileSeparator(path) + Constant.GOBACKUP_ROOT_DIR;
		}
		return new File(Util.ensureFileSeparator(path) + Constant.BACKUP_RES_ROOT_DIR);
	}

	public static File getBackupsRootFile(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}

		File file = null;
		if (path.contains(Constant.GOBACKUP_ROOT_DIR)) {
			file = new File(path);
		} else {
			file = new File(path, Constant.GOBACKUP_ROOT_DIR);
		}
		return file;
	}

	public static RestorableRecord buildEmptyRecord(Context context, String dirPath) {
		if (context == null) {
			return null;
		}
		dirPath = Util.ensureFileSeparator(dirPath);

		// 创建文件夹
		File recordDir = new File(dirPath);
		if (!recordDir.exists()) {
			recordDir.mkdirs();
		}

		// 创建空数据库
		BackupDBHelper dbHelper = new BackupDBHelper(context, BackupDBHelper.getDBName(),
				BackupDBHelper.getDBVersion());
		dbHelper.cleanAllData();
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CONFIG);
		PackageManager pm = context.getPackageManager();
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			cv.put(DataTable.DATA1, pi.versionCode);
			cv.put(DataTable.DATA2, pi.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		cv.put(DataTable.DATA3, Build.VERSION.RELEASE);
		cv.put(DataTable.DATA4, BackupDBHelper.getDBVersion());
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		cv.put(DataTable.DATA5, calendar.getTimeInMillis());
		File file = new File(recordDir.getAbsolutePath());
		if (file.exists()) {
			long size = Util.getFileSize(file.getAbsolutePath());
			cv.put(DataTable.DATA9, size);
		}
		dbHelper.reflashDatatable(cv);
		dbHelper.close();
		File dbFilePath = context.getDatabasePath(BackupDBHelper.getDBName());
		Util.copyFile(dbFilePath.getPath(), dirPath + BackupDBHelper.getDBName());
		context.deleteDatabase(BackupDBHelper.getDBName());
		return new RestorableRecord(context, dirPath);
	}

	public boolean isApplicationInstalled(AppInfo appInfo) {
		if (appInfo == null) {
			return false;
		}
		synchronized (mLock) {
			if (mInstalledPackages == null || mInstalledPackages.size() < 1) {
				return false;
			}
			return mInstalledPackages.containsKey(appInfo.packageName);
		}
	}

	public void initPackagesInfo(final Context context) {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				loadPackagesInfo(context);
				//				long t = System.currentTimeMillis();
				Set<String> packages = null;
				synchronized (mLock) {
					packages = mInstalledPackages.keySet();
					for (String packageName : packages) {
						loadApplicationName(context, packageName);
					}
					//					t = System.currentTimeMillis() - t;
					//					LogUtil.d("load applications' name time = " + t);
				}
			}
		});
	}

	//加载所有应用程序包
	private void loadPackagesInfo(Context context) {
		//		long t = System.currentTimeMillis();
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		synchronized (mLock) {
			mInstalledPackages.clear();
			List<PackageInfo> installedPackages = Util.syncLoadInstalledPackages(context);
			if (installedPackages == null || installedPackages.size() <= 0) {
				return;
			}

			for (PackageInfo pi : installedPackages) {
				if (pi == null) {
					continue;
				}
				mInstalledPackages.put(pi.packageName, pi);
			}
		}
		//		t = System.currentTimeMillis() - t;
		//		LogUtil.d("initPackageInfo time = " + t);
	}

	public List<AppInfo> getUserAppInfoList(Context context) {
		AppInfo appInfo;
		List<AppInfo> appInfoList = null;
		synchronized (mLock) {
			Set<String> packageNameSet = mInstalledPackages.keySet();
			appInfoList = new ArrayList<AppInfo>();
			for (String name : packageNameSet) {
				appInfo = getAppInfo(context, name);
				// 过滤系统应用
				if (appInfo.isSystemApp()) {
					continue;
				}
				// 过滤指定包
				if (packageShouldBeFiltered(appInfo.packageName)) {
					continue;
				}
				appInfoList.add(appInfo);
			}
		}
		return appInfoList;
	}

	public AppInfo getAppInfo(Context context, String packageName) {
		if (context == null) {
			return null;
		}

		if (TextUtils.isEmpty(packageName)) {
			return null;
		}

		PackageInfo packageInfo = null;
		synchronized (mLock) {
			if (mInstalledPackages == null || mInstalledPackages.size() < 1) {
				return null;
			}

			packageInfo = mInstalledPackages.get(packageName);
		}
		if (packageInfo == null) {
			return null;
		}

		final PackageManager pm = context.getPackageManager();
		AppInfo appInfo = new AppInfo(packageInfo, pm);
		return appInfo;
	}

	public void onPackageAdded(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return;
		}
		synchronized (mLock) {
			PackageManager pm = context.getPackageManager();
			try {
				PackageInfo pi = pm.getPackageInfo(packageName, 0);
				mInstalledPackages.put(pi.packageName, pi);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void onPackageRemoved(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return;
		}
		synchronized (mLock) {
			mInstalledPackages.remove(packageName);
		}
	}

	public static boolean packageShouldBeFiltered(String packageName) {
		if (PACKAGE_FILTER == null) {
			return false;
		}
		for (String str : PACKAGE_FILTER) {
			if (str.equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @author maiyongshen
	 */
	private class BackupableRecordBuilder {
		private BackupableRecord mRecord;
		private Context mContext;

		public BackupableRecordBuilder(Context context, String recordDir) {
			if (context == null) {
				throw new IllegalArgumentException("Context cannot be null!");
			}
			mContext = context.getApplicationContext();
			mRecord = new BackupableRecord(mContext, recordDir);
		}

		public BackupableRecordBuilder(Context context) {
			if (context == null) {
				throw new IllegalArgumentException("Context cannot be null!");
			}
			mContext = context.getApplicationContext();
			mRecord = new BackupableRecord(mContext);
		}

		public BackupableRecord createBackupRecord(BackupType backupType) {
			if (backupType == null) {
				return null;
			}

			if (backupType.isBackupUserData()) {
				addUserDataEntries();
			}
			if (backupType.isBackupSystemData()) {
				addSystemDataEntries();
			}
			if (backupType.isBackupUserApp()) {
				addUserAppEntries();
			}
			if (backupType.isBackupSystemApp()) {

			}
			if (backupType.isBackupImage()) {
				addUserImageEntryies();
			}

			// 排序
			mRecord.sortEntries(SORT_TYPE.SORT_BY_APP_NAME);

			mRecord.loadAppinfo(mContext);
			return mRecord;
		}

		private void addSystemDataEntries() {
			// 桌面数据
			BaseBackupEntry launcherDataEntry = buildLauncherDataBackupEntry(mContext);
			if (launcherDataEntry != null) {
				mRecord.addEntry(IRecord.GROUP_SYSTEM_DATA, launcherDataEntry);
			}
			// wifi
			BaseBackupEntry wifiBackupEntry = buildWifiBackupEntry(mContext);
			if (wifiBackupEntry != null) {
				mRecord.addEntry(IRecord.GROUP_SYSTEM_DATA, wifiBackupEntry);
			}

			// 铃声
			mRecord.addEntry(IRecord.GROUP_SYSTEM_DATA, new RingtoneBackupEntry(mContext));

			// 壁纸
			mRecord.addEntry(IRecord.GROUP_SYSTEM_DATA, new WallpaperBackupEntry(mContext));
		}

		private void addUserDataEntries() {
			// 短信
			mRecord.addEntry(IRecord.GROUP_USER_DATA, new SmsBackupEntry(mContext));
			// 彩信
			mRecord.addEntry(IRecord.GROUP_USER_DATA, new MmsBackupEntry(mContext));
			// 联系人
			mRecord.addEntry(IRecord.GROUP_USER_DATA, new ContactsBackupEntry(mContext));
			// 通话记录
			mRecord.addEntry(IRecord.GROUP_USER_DATA, new CallLogBackupEntry(mContext));
			//浏览器书签
			if (BookMarkBackupEntry.getLocalBookMarkCount(mContext) > 0) {
				mRecord.addEntry(IRecord.GROUP_USER_DATA, new BookMarkBackupEntry(mContext));
			}
			if (!CalendarOperator.isCalendarEmpty(mContext)) {
				mRecord.addEntry(IRecord.GROUP_USER_DATA, new CalendarBackupEntry(mContext));
			}
			// 用户词典
			if (!Util.isUserDictionaryEmpty(mContext)) {
				mRecord.addEntry(IRecord.GROUP_USER_DATA, new UserDictionaryBackupEntry(mContext));
			}
			// GO桌面
			BaseBackupEntry goLauncherBackupEntry = buildGoLauncherSettingBackupEntry(mContext,
					mInstalledPackages);
			if (goLauncherBackupEntry != null) {
				mRecord.addEntry(IRecord.GROUP_USER_DATA, goLauncherBackupEntry);
			}
		}

		private void addUserAppEntries() {
			// 获取应用程序备份列表
			synchronized (mLock) {
				List<BaseEntry> appEntries = buildUserAppBackupEntries(mContext, mInstalledPackages);
				if (!Util.isCollectionEmpty(appEntries)) {
					mRecord.addGroup(BackupableRecord.GROUP_USER_APP, appEntries);
					// mRecord.loadAppinfo(mContext);
				}
			}
		}

		//获取图片列表
		private void addUserImageEntryies() {
			if (ImageOperater.hasImage(mContext)) {
				mRecord.addEntry(IRecord.GROUP_USER_IMAGE, new ImageBackupEntry(mContext));
			}
		}

		private List<BaseEntry> buildUserAppBackupEntries(Context context,
				Map<String, PackageInfo> installedPackages) {
			if (installedPackages == null || installedPackages.size() < 1) {
				return null;
			}
			PackageManager pm = context.getPackageManager();
			boolean isRooted = Util.isRootRom(context);
			List<BaseEntry> appEntries = new ArrayList<BaseEntry>();
			Collection<PackageInfo> packageCollection = installedPackages.values();
			for (PackageInfo packageInfo : packageCollection) {
				AppInfo appInfo = new AppInfo(packageInfo, pm);
				// 过滤系统应用
				if (appInfo.isSystemApp()) {
					continue;
				}
				// 过滤指定包
				if (packageShouldBeFiltered(appInfo.packageName)) {
					continue;
				}

				AppBackupEntry appEntry = new AppBackupEntry(context, appInfo);
				// 如果非root用户，过滤private的app
				if (!isRooted && appEntry.getAppInfo().isPrivateApp()) {
					continue;
				}
				appEntries.add(appEntry);
			}
			return appEntries;
		}

		private BaseBackupEntry buildGoLauncherSettingBackupEntry(Context context,
				Map<String, PackageInfo> installedPackages) {
			PackageInfo pi = null;
			if (installedPackages != null && installedPackages.size() > 0) {
				pi = installedPackages.get(GoLauncherSettingBackupEntry.GOLAUNCHER_PACKAGE_NAME);
			}
			if (pi == null) {
				PackageManager pm = context.getPackageManager();
				try {
					pi = pm.getPackageInfo(GoLauncherSettingBackupEntry.GOLAUNCHER_PACKAGE_NAME, 0);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					pi = null;
					return null;
				}
			}
			BaseBackupEntry golauncherEntry = null;
			if (pi.versionCode >= GoLauncherSettingBackupEntry.GO_LAUNCHER_SETTING_MIN_VERSION_CODE) {
				golauncherEntry = new GoLauncherSettingBackupEntry(context);
			}
			return golauncherEntry;
		}

		private BaseBackupEntry buildWifiBackupEntry(Context context) {
			if (context == null) {
				return null;
			}
			// 非root用户不能备份wifi
			if (!isRootValid(context)) {
				return null;
			}
			return new WifiBackupEntry(context);
		}

		private BaseBackupEntry buildLauncherDataBackupEntry(Context context) {
			if (context == null) {
				return null;
			}

			// 非root用户不能备份桌面布局
			if (!isRootValid(context)) {
				return null;
			}

			LauncherDataBackupEntry entry = null;
			// List<AppInfo> appInfos = null;
			AppInfo appInfo = null;
			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> resolveInfos = PackageUtil.getAppWithHomeAction(context);
			for (ResolveInfo info : resolveInfos) {
				PackageInfo packageInfo = mInstalledPackages.get(info.activityInfo.packageName);
				if (packageInfo == null) {
					continue;
				}
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					continue;
				}
				if (LauncherDataBackupEntry.filterIgnorePackage(packageInfo.packageName)) {
					// 过滤指定的包名,htc的sences界面会由于个性化设置还有问题，需要过滤htc的该总类型的机型
					continue;
				}

				appInfo = new AppInfo(packageInfo, pm);
				appInfo.appName = getApplicationName(context, appInfo.packageName);
				break;
			}

			// 如果存在launcher应用，并且是系统应用
			if (appInfo != null) {
				entry = new LauncherDataBackupEntry(context, appInfo);
			}
			return entry;
		}

		private boolean isRootValid(Context context) {
			return RootShell.isRootValid();
		}
	}

	public static boolean isMergedRecord(RestorableRecord record) {
		if (record == null) {
			return false;
		}

		String recordDir = record.getRecordRootDir();
		return !TextUtils.isEmpty(recordDir) && recordDir.contains(SMART_MERGED_BACKUP);
	}

	public static boolean isScheduleRecord(RestorableRecord record) {
		if (record == null) {
			return false;
		}

		String recordDir = record.getRecordRootDir();
		return !TextUtils.isEmpty(recordDir) && recordDir.contains(SCHEDULE_BACKUP);
	}

	public static File getOnlineBackupCacheDbFile(Context context,
			final FileHostingServiceProvider service) {
		if (context == null || service == null) {
			return null;
		}
		File cacheDir = context.getCacheDir();
		File[] files = cacheDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(POSTFIX_ONLINE_BACKUP_DB_CACHE)
						&& filename.startsWith(service.getServiceProviderName());
			}
		});
		if (files == null || files.length < 1) {
			return null;
		}
		return files[0];
	}

	public static File getCloudTaskCacheDbFile(Context context) {
		if (context == null) {
			return null;
		}
		return new File(context.getCacheDir(), NetSyncTaskDbHelper.getDbName());
	}

	public static String getOnlineBackupDbVersion(Context context,
			FileHostingServiceProvider serviceProvider) {
		if (context == null || serviceProvider == null) {
			return null;
		}
		File onlineBackupDbCacheFile = getOnlineBackupCacheDbFile(context, serviceProvider);
		if (onlineBackupDbCacheFile == null) {
			return null;
		}
		String fileName = onlineBackupDbCacheFile.getName();
		String prefix = serviceProvider.getServiceProviderName() + UNDERSCORE;
		int start = prefix.length();
		int end = fileName.indexOf(POSTFIX_ONLINE_BACKUP_DB_CACHE);
		if (end > 0) {
			return fileName.substring(start, end);
		}
		return null;
	}

	public static File saveOnlineBackupDbFileToCache(Context context,
			FileHostingServiceProvider serviceProvider, File srcFile, String fileVersion) {
		if (context == null || serviceProvider == null || srcFile == null || !srcFile.exists()) {
			return null;
		}
		File cacheDir = context.getCacheDir();
		File destFile = new File(cacheDir, serviceProvider.getServiceProviderName() + UNDERSCORE
				+ fileVersion + POSTFIX_ONLINE_BACKUP_DB_CACHE);
		Util.copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
		return destFile;
	}

	private String getApplicationNameFromCache(String pakcageName) {
		return mApplicationNameCaches.get(pakcageName);
	}

	private String loadApplicationName(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		String name = null;
		PackageInfo pi = null;
		synchronized (mLock) {
			pi = mInstalledPackages.get(packageName);
		}
		if (pi != null && pi.applicationInfo != null) {
			//			LogUtil.d("loadApplicationName " + packageName);
			PackageManager pm = context.getPackageManager();
			CharSequence result = pi.applicationInfo.loadLabel(pm);
			if (result != null) {
				name = Util.trimAllSpace(result.toString());
			}
		}
		if (name != null) {
			synchronized (this) {
				mApplicationNameCaches.put(packageName, name);
			}
		}
		return name;
	}

	public String getApplicationName(Context context, String packageName) {
		String name = getApplicationNameFromCache(packageName);
		if (name == null) {
			name = loadApplicationName(context, packageName);
		} else {
			//			LogUtil.d("getApplicationNameFromCache " + packageName);
		}
		return name;
	}

	public String getApplicationName(final Context context, final String packageName,
			final AppInfo.OnAppNameLoadListener listener) {
		String name = getApplicationNameFromCache(packageName);
		if (name != null) {
			//			LogUtil.d("getApplicationNameFromCache " + packageName);
			return name;
		}
		if (listener != null) {
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					listener.onAppNameLoaded(packageName, loadApplicationName(context, packageName));
				}
			});
		}
		return null;
	}

	/**
	 * @author maiyongshen
	 */
	public interface CreateRecordListener {
		public void onStartCreateRecord();

		public void onFinishCreateRecord(IRecord record);
	}
}
