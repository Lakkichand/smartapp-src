package com.jiubang.go.backup.pro.net.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.data.AppBackupEntry;
import com.jiubang.go.backup.pro.data.AppBackupEntry.AppBackArgs;
import com.jiubang.go.backup.pro.data.AppBackupEntry.AppBackupType;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseBackupEntry.BackupState;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;
import com.jiubang.go.backup.pro.data.CalendarBackupEntry;
import com.jiubang.go.backup.pro.data.CallLogBackupEntry;
import com.jiubang.go.backup.pro.data.ContactsBackupEntry;
import com.jiubang.go.backup.pro.data.GoLauncherSettingBackupEntry;
import com.jiubang.go.backup.pro.data.IBackupable.BackupArgs;
import com.jiubang.go.backup.pro.data.LauncherDataBackupEntry;
import com.jiubang.go.backup.pro.data.MmsBackupEntry;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.data.RingtoneBackupEntry;
import com.jiubang.go.backup.pro.data.SmsBackupEntry;
import com.jiubang.go.backup.pro.data.UserDictionaryBackupEntry;
import com.jiubang.go.backup.pro.data.WallpaperBackupEntry;
import com.jiubang.go.backup.pro.data.WifiBackupEntry;
import com.jiubang.go.backup.pro.image.util.GroupBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageBean;
import com.jiubang.go.backup.pro.image.util.OneImageBackupEntry;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.BaseBackupEntryInfo;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.Task;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskObject;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskState;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskType;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.FileHostingServiceException;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 */
public class NetBackupEngine extends AsyncWorkEngine implements IAsyncTaskListener, ActionListener {
	private static final String LOG_TAG = NetBackupEngine.class.getSimpleName();
	private static final int UPLOAD_DATABASE_INTERVAL = 10;

	private static final int MAX_WAIT_TIME = 30 * 1000;

	private static final float UPLOAD_PROGRESS_WEIGHT = 0.5f;
	private static final float BACKUP_PROGRESS_WEIGHT = 1.0f - UPLOAD_PROGRESS_WEIGHT;

	private Context mContext = null;
	private IAsyncTaskListener mListener = null;
	private FileHostingServiceProvider mService = null;
	private File mTempBackupCacheDir;
	private boolean mIsRoot;
	private List<Task> mAllNetworkTask = null;
	private List<ResultBean> mTaskResults = new ArrayList<ResultBean>();
	private BackupDBHelper mBackupDbHelper = null;
	private NetSyncTaskDbHelper mBackupTaskDbHelper = null;

	private Task mCurTask = null;
	private BaseBackupEntry mCurBackupEntry = null;
	private int mCurBackupTaskIndex = -1;
	private long mTotalBackupSize = 0;

	private int mTotalTaskCount = 0;
	private int mTotalSuccessfulTaskCount = 0;
	private int mAppTaskCount = 0;
	private int mCompletedAppTaskCount = 0;
	private int mFinishedAppTaskCount = 0;
	private int mCanceledAppTaskCount = 0;
	private int mFailedAppTaskCount = 0;

	private int mImageTaskCount = 0;
	private int mCompletedImageTaskCount = 0;
	private int mFinishedImageTaskCount = 0;
	private int mCanceledImageTaskCount = 0;
	private int mFailedImageTaskCount = 0;

	private List<UploadFileInfo> mFilesToUpload;
	private int mCurUploadFileIndex;
	private CancelableTask mUploadTask;

	private float mCurrentProgress;
	private float mProgressUnit;

	// 上传网络数据后返回的版本号
	private String mNetBackupDbRev = null;

	private String mExtraTips;

	private String[] mSdCarkPaths;
	public TreeSet<String> mSdCarkPathSet;
	private boolean mIsNetError = false;
	
	private boolean mUploadDbRightNow = false;
	/**
	 * @author maiyongshen
	 */
	class UploadFileInfo {
		File mFileToUpload;
		String mDestPath;

		public UploadFileInfo(File file, String path) {
			mFileToUpload = file;
			mDestPath = path;
		}
	}

	// private static final int MSG_INIT_TASK = 0x3001;
	// private static final int MSG_START_LOCAL_BACKUP = 0x3002;
	private static final int MSG_UPLOAD_FILE = 0x3003;
	private static final int MSG_SHOW_TOAST = 0x3004;
	private Handler mInternalHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/*
			 * case MSG_INIT_TASK: initBackupTask((Task)(msg.obj)); break; case
			 * MSG_START_LOCAL_BACKUP: startLocalBackupEntry((Task)(msg.obj)); break;
			 */
				case MSG_UPLOAD_FILE :
					uploadFile((UploadFileInfo) (msg.obj));
					break;

				case MSG_SHOW_TOAST :
					if (msg.obj != null) {
						Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_LONG).show();
					}
					break;

				default :
					super.handleMessage(msg);
					break;
			}
		}
	};

	private File mOriginBackupDbFile = null;
	private File mOriginTaskDbFile = null;
	private boolean mFinishOneTaskAtLeast = false;

	public NetBackupEngine(Context context, File taskDbFile, File backupDbFile,
			IAsyncTaskListener listener) {
		super("NetworkBackupEngine0");
		if (context == null) {
			throw new IllegalArgumentException("NetworkBackupEngine invalid argument");
		}
		mContext = context;
		mListener = listener;
		
		if (backupDbFile != null && backupDbFile.exists()) {
			mOriginBackupDbFile = backupDbFile;
		} else {
			mOriginBackupDbFile = mContext.getDatabasePath(BackupDBHelper.getDBName());
		}
		mBackupDbHelper = new BackupDBHelper(mContext, mOriginBackupDbFile.getAbsolutePath(),
				BackupDBHelper.getDBVersion());
		
		if (taskDbFile != null && taskDbFile.exists()) {
			mOriginTaskDbFile = taskDbFile;
		} else {
			mOriginTaskDbFile = new File(mContext.getCacheDir(), NetSyncTaskDbHelper.getDbName());
		}
		mBackupTaskDbHelper = new NetSyncTaskDbHelper(mContext,
				mOriginTaskDbFile.getAbsolutePath(), NetSyncTaskDbHelper.getDbVersion());
	}

	private void initBackupTask(Task task) {
		if (task == null) {
			throw new IllegalArgumentException("task can not be null");
		}
		mCurUploadFileIndex = 0;
		startLocalBackupEntry(task);
	}

	private void startLocalBackupEntry(Task task) {
		if (task == null) {
			throw new IllegalArgumentException("task can not be null");
		}

		mCurBackupEntry = buildBackupEntry(task);
		if (mCurBackupEntry == null) {
			onTaskFinished(task, TaskState.FAILED);
			return;
		}

		notifyBackupProgressUpdated(0, mCurBackupEntry,
				mContext.getString(R.string.progress_format, 0));

		boolean ret = false;
		ret = mCurBackupEntry.backup(mContext, buildBackupArgs(mCurBackupEntry), this);
		if (!ret) {
			// 本地备份失败
			onTaskFinished(task, TaskState.FAILED);
		}
	}

	private void onTaskFinished(Task task, TaskState state) {
		// Log.d("GoBackup", "onTask finished, task = " + task.taskObject + ", state = " + state);
		if (task == null || state == null) {
			setCurrentWorkState(WorkState.FAILED);
			continueToWork();
			return;
		}

		if (state == TaskState.FINISHED) {
			mTotalSuccessfulTaskCount++;
			mFinishOneTaskAtLeast = true;
		}

		if (task.taskObject == TaskObject.APP) {
			// 应用程序无论成功还是失败，都计入完成
			mCompletedAppTaskCount++;
			if (state == TaskState.CANCELED) {
				mCanceledAppTaskCount++;
			} else if (state == TaskState.FAILED) {
				mFailedAppTaskCount++;
			} else if (state == TaskState.FINISHED) {
				mFinishedAppTaskCount++;
			}
		}

		if (task.taskObject == TaskObject.IMAGE) {
			// 照片无论成功还是失败，都计入完成
			mCompletedImageTaskCount++;
			if (state == TaskState.CANCELED) {
				mCanceledImageTaskCount++;
			} else if (state == TaskState.FAILED) {
				mFailedImageTaskCount++;
			} else if (state == TaskState.FINISHED) {
				mFinishedImageTaskCount++;
			}
		}

		if (task.taskObject != TaskObject.UPLOAD_DB) {
			// 更新普通备份任务的状态
			if (mCurBackupEntry != null
					&& TaskObject.valueOf(mCurBackupEntry.getType()) == task.taskObject) {
				BackupState backupState = state == TaskState.FINISHED
						? BackupState.BACKUP_SUCCESSFUL
						: state == TaskState.FAILED
								? BackupState.BACKUP_ERROR_OCCURRED
								: BackupState.BACKUP_CANCELED;
				mCurBackupEntry.setState(backupState);
			}
		}
		updateTaskState(task, state);

		if (mCurTask.taskObject != TaskObject.UPLOAD_DB) {
			addUploadTaskResult(buildResultBean(task));
		} else {
			// 数据库上传不展示结果项
			// 拷贝数据库文件
//			BackupManager.saveOnlineBackupDbFileToCache(mContext, mService, mOriginBackupDbFile,
//					mNetBackupDbRev);
		}

		if (mCurBackupEntry != null
				&& mCurBackupEntry.getState() == BackupState.BACKUP_ERROR_OCCURRED) {
			notifyBackupProgressUpdated(1.0f, mCurBackupEntry,
					mContext.getString(R.string.state_failed));
		} else {
			notifyUploadProgressUpdated(1.0f);
		}

		// 更新进度
		mCurrentProgress += getProgressWeight(task) * mProgressUnit;
		if (mListener != null) {
			mListener.onProceeding((int) mCurrentProgress, task, null, null);
		}

		// 删除已上传文件
		deleteUploadedFiles();

		WorkState currentWorkState = state == TaskState.FINISHED ? WorkState.COMPLETED : WorkState.FAILED;
		setCurrentWorkState(currentWorkState);
		
		continueToWork();
	}

	private void uploadFile(UploadFileInfo fileInfo) {
		if (isPaused()) {
			return;
		}

		// TODO 临时解决数据库文件在某些情况下被异常删除时程序报错的问题
		if (fileInfo == null || fileInfo.mFileToUpload == null || !fileInfo.mFileToUpload.exists()
				|| TextUtils.isEmpty(fileInfo.mDestPath)) {
			onTaskFinished(mCurTask, TaskState.FAILED);
			return;
		}

		if (isForceStopped()) {
			// 若已被强制停止，直接结束
			onTaskFinished(mCurTask, TaskState.CANCELED);
			return;
		}

		if (fileInfo == null || fileInfo.mFileToUpload == null
				|| TextUtils.isEmpty(fileInfo.mDestPath)) {
			throw new IllegalArgumentException("invalid file!");
		}

		Log.d("GOBackup",
				"NetBacckupEngine : uploadFile : fileInfo = " + fileInfo.mFileToUpload.toString()
						+ "-----------mService" + mService);
		mUploadTask = mService.uploadFile(fileInfo.mFileToUpload, fileInfo.mDestPath, true, this);
	}
	
	private boolean shouldUploadDatabaseFile() {
		if (mUploadDbRightNow) {
			return false;
		}
		
		Task nextTask = null;
		if (mCurBackupTaskIndex > 0 && mCurBackupTaskIndex + 1 < mAllNetworkTask.size()) {
			nextTask = mAllNetworkTask.get(mCurUploadFileIndex + 1);
		}
		return mCurBackupTaskIndex != 0 && (mCurBackupTaskIndex + 1) % UPLOAD_DATABASE_INTERVAL == 0 && nextTask != null
				&& nextTask.taskObject != TaskObject.UPLOAD_DB;
	}
	
	private void uploadBackupDatabaseFile() {
		// 更新数据库
		boolean ret = updatePropertiesToDb();
		mFilesToUpload.clear();
		mCurUploadFileIndex = 0;
		File tempFile = new File(mTempBackupCacheDir, BackupDBHelper.getDBName());
		if (Util.copyFile(mOriginBackupDbFile.getAbsolutePath(), tempFile.getAbsolutePath())) {
			mFilesToUpload.add(new UploadFileInfo(tempFile, getUploadOnlinePath(null)));
			Message.obtain(mInternalHandler, MSG_UPLOAD_FILE,
					mFilesToUpload.get(mCurUploadFileIndex)).sendToTarget();
		} else {
			onTaskFinished(mCurTask, TaskState.FAILED);
		}
//		if (!TextUtils.equals(mOriginBackupDbFile.getName(), BackupDBHelper.getDBName())) {
//			mOriginBackupDbFile = Util
//					.renameFile(mOriginBackupDbFile, BackupDBHelper.getDBName());
//		}
	}

	@Override
	protected void onWorkStart() {
		init();
		if (mListener != null) {
			mListener.onStart(initAllTaskDetail(), null);
		}
	}

	@Override
	public String getNotificationMessage() {
		// TODO UI交互要修改
		return mContext.getString(R.string.msg_uploading_backup, mCurBackupEntry != null
				? mCurBackupEntry.getDescription()
				: "", "");
	}

	private void init() {
		mTempBackupCacheDir = new File(Constant.buildNetworkBackupCacheDir(mContext));
		if (!mTempBackupCacheDir.exists()) {
			mTempBackupCacheDir.mkdirs();
		}

		mFilesToUpload = new ArrayList<UploadFileInfo>();

		mIsRoot = RootShell.isRootValid();

		initTaskList();
		mTotalTaskCount = mAllNetworkTask == null ? 0 : mAllNetworkTask.size();
		mAppTaskCount = calcAppTaskCount(mAllNetworkTask);
		mImageTaskCount = calcImageTaskCount(mAllNetworkTask);

		mService = CloudServiceManager.getInstance().getCurrentService();

		mCurrentProgress = 0;
		calcProgressUnit();
		mSdCarkPathSet = new TreeSet<String>(new Comparator<String>() {

			@Override
			public int compare(String stra, String strb) {
				return strb.compareTo(stra);
			}

		});
		mSdCarkPaths = Util.getAllSdPath();
		for (String sdPath : mSdCarkPaths) {
			mSdCarkPathSet.add(sdPath);
		}

	}

	private void initTaskList() {
		mAllNetworkTask = mBackupTaskDbHelper.getAllNotFinishedTasks(TaskType.ONLINE_BACKUP);
	}

	private int calcAppTaskCount(List<Task> allTasks) {
		if (Util.isCollectionEmpty(allTasks)) {
			return 0;
		}
		int count = 0;
		for (Task task : allTasks) {
			if (task.taskObject == TaskObject.APP) {
				count++;
			}
		}
		return count;
	}

	private int calcImageTaskCount(List<Task> allTasks) {
		if (Util.isCollectionEmpty(allTasks)) {
			return 0;
		}
		int count = 0;
		for (Task task : allTasks) {
			if (task.taskObject == TaskObject.IMAGE) {
				count++;
			}
		}
		return count;
	}

	private String getUploadOnlinePath(Task task) {
		if (mService == null) {
			return null;
		}

		if (task != null && task.taskObject == TaskObject.MMS) {
			return Util.ensureFileSeparator(mService.getOnlineBackupPath())
					+ MmsBackupEntry.MMS_DIR_NAME;
		}
		if (task != null && task.taskObject == TaskObject.IMAGE && task.extraInfo.length >= 3) {
			String mImageDir = ImageBackupEntry.IMAGE_DIR_NAME;
			String imageParentPath = (String) task.extraInfo[2];
			if (TextUtils.isEmpty(imageParentPath)) {
				return null;
			}
			TreeSet<String> sdPathSet = mSdCarkPathSet;
			String imageSdpath = null;
			//确保父路径包含SD卡路径
			String imageParentPathHasSeperator = imageParentPath + File.separator;
			for (String sdpath : sdPathSet) {
				if (imageParentPathHasSeperator.startsWith(sdpath)) {
					imageSdpath = sdpath;
					break;
				}
			}
			if (TextUtils.isEmpty(imageSdpath)) {
				return null;
			}

			String imagePath = Util.ensureFileSeparator(mService.getOnlineBackupPath())
					+ imageParentPath.replace(imageSdpath, mImageDir + File.separator);
			return imagePath;
		}
		return mService.getOnlineBackupPath();
	}

	@Override
	protected void onWorkFinish(boolean finishNormal) {
		Log.d("GoBackup", "NetBackupEngine onWorkFinish");
		if (!finishNormal) {
			cancelAllRemainTasks();
		}
		boolean success = finishNormal && (mTotalSuccessfulTaskCount == mTotalTaskCount);
		if (mBackupDbHelper != null) {
			mBackupDbHelper.close();
		}
		if (mBackupTaskDbHelper != null) {
			mBackupTaskDbHelper.close();
		}
		if (!success && mFinishOneTaskAtLeast) {
			copyBackupDbToCacheWhenFinishAbnormal();
		}
		if (mListener != null) {
			mListener.onEnd(success, getResults(), null);
		}

		release();
		super.onWorkFinish(success);
	}

	private void copyBackupDbToCacheWhenFinishAbnormal() {
		if (mOriginBackupDbFile == null) {
			return;
		}
		File cacheFile = mContext.getCacheDir();
		if (mOriginBackupDbFile.getAbsolutePath().startsWith(cacheFile.getAbsolutePath())) {
			return;
		}
		BackupManager.saveOnlineBackupDbFileToCache(mContext, mService, mOriginBackupDbFile, "0");
		mOriginBackupDbFile.delete();
		LogUtil.d("delete upload database file");
		mOriginBackupDbFile = null;
	}

	private void release() {
		if (mFilesToUpload != null) {
			mFilesToUpload.clear();
		}
		if (mService != null) {
			mService.release();
		}
	}

	private boolean updatePropertiesToDb() {
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CONFIG);
		PackageManager pm = mContext.getPackageManager();
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			cv.put(DataTable.DATA1, pi.versionCode);
			cv.put(DataTable.DATA2, pi.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		cv.put(DataTable.DATA3, Build.VERSION.RELEASE);
		cv.put(DataTable.DATA4, BackupDBHelper.getDBVersion());

		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		cv.put(DataTable.DATA5, calendar.getTimeInMillis());
		cv.put(DataTable.DATA6, mIsRoot);
		cv.put(DataTable.DATA7, getBackupItemCount(true));
		cv.put(DataTable.DATA8, getBackupItemCount(false));

		// 这一步需联网操作，较耗时
		long size = getOnlineBackupSize();
		// 加上数据库自身大小
		if (mOriginBackupDbFile != null) {
			size += mOriginBackupDbFile.length();
		}
		cv.put(DataTable.DATA9, size);
		return mBackupDbHelper.reflashDatatable(cv);
	}

	private long getOnlineBackupSize() {
		FutureTask<Long> task = new FutureTask<Long>(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				//				long t = System.currentTimeMillis();
				OnlineFileInfo root = mService.getFileInfo(mService.getOnlineBackupPath());
				long size = getOnlineFileSizeRecursion(root);
				//				LogUtil.d("sum size = " + size);
				//				t = System.currentTimeMillis() - t;
				//				LogUtil.d("get size time = " + t);
				return size;
			}
		});
		task.run();
		Long result = null;
		try {
			result = task.get(60 * 1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return result != null ? result : 0;
	}

	private long getOnlineFileSizeRecursion(OnlineFileInfo onlineFile)
			throws FileHostingServiceException {
		if (onlineFile == null) {
			return 0;
		}

		if (!onlineFile.isDirectory()) {
			return onlineFile.getSize();
		}

		long size = 0;
		OnlineFileInfo[] subOnlineFiles = onlineFile.listContent();
		if (subOnlineFiles == null || subOnlineFiles.length == 0) {
			return 0;
		}

		for (OnlineFileInfo subOnlineFile : subOnlineFiles) {
			long subFileSize = getOnlineFileSizeRecursion(subOnlineFile);
			//			LogUtil.d("File " + subOnlineFile.getFileName() + " size = " + subFileSize);
			size += subFileSize;
		}
		return size;
	}

	private int getBackupItemCount(boolean isAppItem) {
		List<BaseBackupEntryInfo> itemInfos = null;
		if (isAppItem) {
			return mBackupDbHelper.getAppEntriesCount();
		} else {
			return mBackupDbHelper.getSystemEntriesCount();
		}
	}

	@Override
	protected boolean hasNextWork() {
		return hasNextTask();
	}

	private boolean hasNextTask() {
		return mAllNetworkTask != null && mAllNetworkTask.size() > 0
				&& mCurBackupTaskIndex + 1 < mTotalTaskCount;
	}

	@Override
	public synchronized void pause() {
		LogUtil.d("pause");
		super.pause();
		// 如果当前在做上传操作，则取消，下次回来时候继续上传
		if (mUploadTask != null) {
			mUploadTask.cancel();
			mUploadTask = null;
		}
	}

	@Override
	protected void continueUnfinishedWork() {
		//		Log.d("GoBackup", "continueUnfinishedWork");
		super.continueUnfinishedWork();

		if (isForceStopped()) {
			// 若已被强制停止，直接结束
			onTaskFinished(mCurTask, TaskState.CANCELED);
			return;
		}

		// 重做上次暂停没有完成的工作
		redoLastTask();
	}

	@Override
	protected void doNextWork() {
		//		LogUtil.d("doNextWork");
		setCurrentWorkState(WorkState.WORKING);
		mCurTask = getNextTask();
		mCurBackupEntry = null;
		mUploadTask = null;
		if (mListener != null) {
			mListener.onProceeding((int) mCurrentProgress, mCurTask, null, null);
		}
		if (mCurTask.taskObject == TaskObject.UPLOAD_DB) {
			uploadBackupDatabaseFile();
		} else {
			initBackupTask(mCurTask);
		}
	}

	private BaseBackupEntry buildBackupEntry(Task task) {
		if (task == null) {
			return null;
		}

		BaseBackupEntry entry = null;
		EntryType entryType = TaskObject.toEntryType(task.taskObject);
		if (entryType == null) {
			return null;
		}

		switch (entryType) {
			case TYPE_SYSTEM_APP :
			case TYPE_USER_APP : {
				String packageName = task.extraInfo == null ? null : task.extraInfo[1].toString();
				if (packageName == null) {
					return null;
				}
				BackupManager bm = BackupManager.getInstance();
				AppInfo appinfo = bm.getAppInfo(mContext, packageName);
				if (appinfo == null) {
					return null;
				}
				if (!appinfo.isApplicationNameValid()) {
					appinfo.appName = bm.getApplicationName(mContext, packageName);
				}
				entry = new AppBackupEntry(mContext, appinfo);
				break;
			}

			case TYPE_USER_CALL_HISTORY :
				entry = new CallLogBackupEntry(mContext);
				break;

			case TYPE_USER_CONTACTS :
				entry = new ContactsBackupEntry(mContext);
				break;

			case TYPE_USER_DICTIONARY :
				entry = new UserDictionaryBackupEntry(mContext);
				break;

			case TYPE_USER_GOLAUNCHER_SETTING :
				entry = new GoLauncherSettingBackupEntry(mContext);
				break;

			case TYPE_USER_MMS :
				entry = new MmsBackupEntry(mContext);
				break;

			case TYPE_SYSTEM_RINGTONE :
				entry = new RingtoneBackupEntry(mContext);
				break;

			case TYPE_USER_SMS :
				entry = new SmsBackupEntry(mContext);
				break;

			case TYPE_USER_BOOKMARK :
				entry = new BookMarkBackupEntry(mContext);
				break;

			case TYPE_SYSTEM_WALLPAPER :
				entry = new WallpaperBackupEntry(mContext);
				break;

			case TYPE_SYSTEM_LAUNCHER_DATA : {
				String packageName = task.extraInfo == null ? null : task.extraInfo[0].toString();
				if (packageName == null) {
					return null;
				}
				BackupManager bm = BackupManager.getInstance();
				AppInfo appinfo = bm.getAppInfo(mContext, packageName);
				if (appinfo == null) {
					return null;
				}
				if (!appinfo.isApplicationNameValid()) {
					appinfo.appName = bm.getApplicationName(mContext, packageName);
				}
				entry = new LauncherDataBackupEntry(mContext, appinfo);
				break;
			}

			case TYPE_SYSTEM_WIFI :
				entry = new WifiBackupEntry(mContext);
				break;

			case TYPE_USER_CALENDAR :
				entry = new CalendarBackupEntry(mContext);
				break;

			case TYPE_USER_IMAGE :
				entry = new ImageBackupEntry(mContext, false);
				ImageBean image = new ImageBean();
				image.mImagePath = task.paths == null ? null : task.paths[0];
				image.mImageDisplayName = task.extraInfo == null
						? null
						: (String) task.extraInfo[0];
				image.mImageSize = task.extraInfo != null && task.extraInfo.length > 1 ? Long
						.parseLong(task.extraInfo[1].toString()) : 0;
				image.mImageParentFilePath = task.extraInfo != null && task.extraInfo.length > 2
						? (String) task.extraInfo[2]
						: null;
				String imageSdpath = null;
				for (String sdpath : mSdCarkPathSet) {
					if (image.mImagePath.startsWith(sdpath)) {
						imageSdpath = sdpath;
						break;
					}
				}
				OneImageBackupEntry bakupEntry = new OneImageBackupEntry(mContext, image,
						imageSdpath);
				((GroupBackupEntry) entry).addEntry(bakupEntry);
				break;

			default :
				break;
		}
		return entry;
	}

	private Task getNextTask() {
		if (mAllNetworkTask == null || mAllNetworkTask.size() == 0) {
			return null;
		}

		if (mCurBackupTaskIndex + 1 >= mTotalTaskCount) {
			return null;
		}
		
		if (shouldUploadDatabaseFile()) {
			LogUtil.d("should upload database, index = " + mCurBackupTaskIndex);
			mUploadDbRightNow = true;
			Task uploadDbTask = new Task();
			uploadDbTask.taskObject = TaskObject.UPLOAD_DB;
			uploadDbTask.taskType = TaskType.ONLINE_BACKUP;
			uploadDbTask.taskState = TaskState.NOT_START;
			return uploadDbTask;
		}
		mUploadDbRightNow = false;
		return mAllNetworkTask.get(++mCurBackupTaskIndex);
	}

	private void redoLastTask() {
		// 表示目前仍处于备份状态，还没有文件可以上传
		if (Util.isCollectionEmpty(mFilesToUpload)) {
			Log.d("GoBackup", "no file to upload");
			return;
		}
		Log.d("GoBackup", "upload file " + mCurUploadFileIndex);
		// 目前如果上次本地备份完成，网络上传未完成，则任务未完成，目前只处理这种情况
		Message.obtain(mInternalHandler, MSG_UPLOAD_FILE, mFilesToUpload.get(mCurUploadFileIndex))
				.sendToTarget();
	}

	private BackupArgs buildBackupArgs(BaseBackupEntry entry) {
		if (entry == null) {
			return null;
		}

		BackupArgs args = null;
		EntryType type = entry.getType();
		switch (type) {
			case TYPE_USER_APP :
			case TYPE_SYSTEM_APP :
				AppBackArgs appArgs = new AppBackupEntry.AppBackArgs();
				appArgs.mDbHelper = mBackupDbHelper;
				appArgs.mBackupPath = Util.ensureFileSeparator(mTempBackupCacheDir
						.getAbsolutePath());
				appArgs.mIsRoot = mIsRoot;
				appArgs.mAppBackupType = shouldBackupAppData()
						? AppBackupType.APK_DATA
						: AppBackupType.APK;
				args = appArgs;
				break;

			default :
				args = new BackupArgs();
				args.mDbHelper = mBackupDbHelper;
				args.mBackupPath = Util.ensureFileSeparator(mTempBackupCacheDir.getAbsolutePath());
				args.mIsRoot = mIsRoot;
				break;
		}
		return args;
	}

	@Override
	public synchronized void forceToStop() {
		Log.d("GoBackup", "forceToStop");
		if (mUploadTask != null) {
			Log.d("GoBackup", "try to cancel upload task");
			mUploadTask.cancel();
			mUploadTask = null;
		}
		super.forceToStop();
	}

	@Override
	public void onStart(Object arg1, Object arg2) {
	}

	@Override
	public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
		if (mListener == null) {
			return;
		}
		mExtraTips = arg3 != null ? arg3.toString() : null;
		if (arg2 instanceof BaseBackupEntry) {
			notifyBackupProgressUpdated((Float) progress, (BaseBackupEntry) arg2, mExtraTips);
		} else if (arg2 instanceof CancelableTask) {
			notifyUploadProgressUpdated((Float) progress);
		}
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		Log.d("GoBackup", "NetBackupEngine : onEnd()");
		if (!success || arg2 == null || !(arg2 instanceof String[])) {
			// TODO 本地备份不成功，不再网络上传
			onTaskFinished(mCurTask, TaskState.FAILED);
			return;
		}

		String[] filePaths = (String[]) arg2;
		for (String path : filePaths) {
			File file = new File(path);
			if (file.isDirectory()) {
				File[] subFiles = file.listFiles();
				if (subFiles != null && subFiles.length > 0) {
					for (File subFile : subFiles) {
						mFilesToUpload.add(new UploadFileInfo(subFile, Util
								.ensureFileSeparator(getUploadOnlinePath(mCurTask))
								+ file.getName()));
					}
				}
				continue;
			}
			mFilesToUpload.add(new UploadFileInfo(file, getUploadOnlinePath(mCurTask)));
		}

		if (isPaused()) {
			// 暂停时，不再继续上传文件
			return;
		}

		// 在备份完成前点击了“停止”，则将当前任务设置为取消
		if (isForceStopped()) {
			Log.d("GoBackup", "backup end and cancel upload");
			onTaskFinished(mCurTask, TaskState.CANCELED);
			return;
		}

		notifyUploadProgressUpdated(0);
		Message.obtain(mInternalHandler, MSG_UPLOAD_FILE, mFilesToUpload.get(mCurUploadFileIndex))
				.sendToTarget();
	}

	@Override
	public void onProgress(long progress, long total, Object data) {
		if (mUploadTask == null) {
			return;
		}
		float curUploadFileProgress = (float) progress / (float) total;
		final int fileCount = mFilesToUpload.size();
		float curUploadProgress = (mCurUploadFileIndex + curUploadFileProgress) / fileCount;
		onProceeding(curUploadProgress, mUploadTask, null, null);
	}

	@Override
	public void onComplete(Object data) {
		if (data != null && data instanceof OnlineFileInfo
				&& mCurTask.taskObject == TaskObject.UPLOAD_DB) {
			// 保存数据库文件的rev值
			mNetBackupDbRev = ((OnlineFileInfo) data).getRevCode();
		}

		float curUploadProgress = (float) (mCurUploadFileIndex + 1) / mFilesToUpload.size();
		onProceeding(curUploadProgress, mUploadTask, null, null);

		continueNextUploadTask();
	}

	private void continueNextUploadTask() {
		if (++mCurUploadFileIndex >= mFilesToUpload.size()) {
			// 全部上传完成
			onTaskFinished(mCurTask, TaskState.FINISHED);
			return;
		}

		// 继续上传文件
		mUploadTask = null;
		Message.obtain(mInternalHandler, MSG_UPLOAD_FILE, mFilesToUpload.get(mCurUploadFileIndex))
				.sendToTarget();
	}

	@Override
	public void onError(int errCode, String errMessage, Object data) {
		// Log.d("GOBackup", "onError() : errCode = " + errCode + ", errMessage = " + errMessage);
		if (errCode == FileHostingServiceProvider.NETWORK_IO_ERROR && !mIsNetError) {
			mIsNetError = true;
			Message.obtain(mInternalHandler, MSG_SHOW_TOAST, mContext.getString(R.string.neterror))
					.sendToTarget();
		}
		LogUtil.d("upload file error");
		synchronized (this) {
			onTaskFinished(mCurTask, TaskState.FAILED);
		}
	}

	@Override
	public void onCancel(Object data) {
		Log.d("GoBackup", "Task onCancel");
		if (isForceStopped()) {
			Log.d("GoBackup", "onTaskFinished Cancel");
			onTaskFinished(mCurTask, TaskState.CANCELED);
		}
	}

	private void updateTaskState(Task task, TaskState state) {
		if (task == null || state == null) {
			return;
		}
		task.taskState = state;
		mBackupTaskDbHelper.updateTaskState(task, state);
	}

	private boolean shouldBackupAppData() {
		if (!mIsRoot) {
			return false;
		}
		PreferenceManager pm = PreferenceManager.getInstance();
		return pm.getBoolean(mContext, PreferenceManager.KEY_SHOULD_BACKUP_RESTORE_APP_DATA, true);
	}

	private void deleteUploadedFiles() {
		if (!Util.isCollectionEmpty(mFilesToUpload)) {
			for (UploadFileInfo fileInfo : mFilesToUpload) {
				if (fileInfo.mFileToUpload == null) {
					continue;
				}
				if (fileInfo.mFileToUpload.getAbsolutePath().startsWith(
						mTempBackupCacheDir.getAbsolutePath())) {
					Util.deleteFile(fileInfo.mFileToUpload.getAbsolutePath());
				}
			}
			mFilesToUpload.clear();
		}
		mCurUploadFileIndex = 0;
	}

	private int getProgressWeight(Task task) {
		if (task == null) {
			return 0;
		}
		switch (task.taskObject) {
			case SMS :
			case CALLLOG :
			case CONTACTS :
			case GOLAUNCHER_SETTING :
			case MMS :
			case USER_DICTIONARY :
			case WIFI :
			case LAUNCHER_DATA :
			case RINGTONE :
			case APP :
			case WALLPAPER :
			case CALENDAR :
			case BOOKMARK :
			case IMAGE :
				return 10;
			case UPLOAD_DB :
				return 5;
			default :
				break;
		}
		return 0;
	}

	private void calcProgressUnit() {
		if (Util.isCollectionEmpty(mAllNetworkTask)) {
			mProgressUnit = 1.0f;
			return;
		}
		int sum = 0;
		for (Task task : mAllNetworkTask) {
			sum += getProgressWeight(task);
		}
		// 加上中途上传的数据库次数
		Task task = new Task();
		task.taskObject = TaskObject.UPLOAD_DB;
		sum += getProgressWeight(task) * ((mAllNetworkTask.size() - 2) / UPLOAD_DATABASE_INTERVAL);
		final float progressSum = 100f;
		mProgressUnit = sum > 0 ? progressSum / sum : 1.0f;
	}

	private void notifyUploadProgressUpdated(float progress) {
		if (mListener == null) {
			return;
		}
		float curTaskProgressSum = getProgressWeight(mCurTask) * mProgressUnit;
		float uploadProgress = (curTaskProgressSum * UPLOAD_PROGRESS_WEIGHT) * progress;
		float curTaskProgress = curTaskProgressSum * BACKUP_PROGRESS_WEIGHT + uploadProgress;
		String progressFormat = null;
		if (mCurTask.taskObject == TaskObject.APP) {
			progressFormat = mContext.getString(R.string.progress_detail, mCompletedAppTaskCount,
					mAppTaskCount);
		} else if (mCurTask.taskObject == TaskObject.IMAGE) {
			progressFormat = mContext.getString(R.string.progress_detail, mCompletedImageTaskCount,
					mImageTaskCount);
		} else {
			final int percentage = 100;
			progressFormat = mContext.getString(R.string.progress_format,
					(int) (progress * percentage));
		}
		WorkDetailBean workDetailBean = buildWorkDetail(mCurTask, (int) progress < 1
				? WorkState.WORKING
				: WorkState.COMPLETED, progressFormat);
		final int curProgress = (int) (mCurrentProgress + curTaskProgress);
		mListener.onProceeding(curProgress, mCurTask, workDetailBean, null);
	}

	private void notifyBackupProgressUpdated(float progress, BaseBackupEntry entry,
			String progressMsg) {
		if (mListener == null) {
			return;
		}
		float curTaskProgressSum = getProgressWeight(mCurTask) * mProgressUnit;
		final float backupProgress = (curTaskProgressSum * BACKUP_PROGRESS_WEIGHT) * progress;
		float curTaskProgress = backupProgress;
		String extraTip = progressMsg;
		if (entry instanceof AppBackupEntry) {
			extraTip = mContext.getString(R.string.progress_detail, mCompletedAppTaskCount,
					mAppTaskCount);
		}
		if (entry instanceof ImageBackupEntry) {
			extraTip = mContext.getString(R.string.progress_detail, mCompletedImageTaskCount,
					mImageTaskCount);
		}

		WorkDetailBean workDetailBean = buildWorkDetail(mCurTask.taskObject.value(), entry,
				extraTip);
		final int curProgress = (int) (mCurrentProgress + curTaskProgress);
		// Log.d("GoBackup", "backup progress = " + curProgress +
		// ", mCurProgress = " + mCurrentProgress);
		mListener.onProceeding(curProgress, mCurTask, workDetailBean, null);
	}

	private List<WorkDetailBean> initAllTaskDetail() {
		if (Util.isCollectionEmpty(mAllNetworkTask)) {
			return null;
		}
		List<WorkDetailBean> allTaskDetail = new ArrayList<AsyncWorkEngine.WorkDetailBean>();
		// 应用程序任务归为一个分类
		WorkDetailBean appWorkDetailBean = null;
		WorkDetailBean imageWorkDetailBean = null;
		for (Task task : mAllNetworkTask) {
			final String progressDesc = mContext.getString(R.string.state_waiting);
			if (task.taskObject == TaskObject.APP) {
				if (appWorkDetailBean == null) {
					appWorkDetailBean = buildWorkDetail(task, WorkState.NOT_START, progressDesc);
					allTaskDetail.add(appWorkDetailBean);
				}
				continue;
			}

			if (task.taskObject == TaskObject.IMAGE) {
				if (imageWorkDetailBean == null) {
					imageWorkDetailBean = buildWorkDetail(task, WorkState.NOT_START, progressDesc);
					allTaskDetail.add(imageWorkDetailBean);
				}
				continue;
			}
			allTaskDetail.add(buildWorkDetail(task, WorkState.NOT_START, progressDesc));
		}
		return allTaskDetail;
	}

	private WorkDetailBean buildWorkDetail(int taskId, BaseBackupEntry entry, String progress) {
		WorkDetailBean workDetail = new WorkDetailBean();
		workDetail.workId = taskId;
		workDetail.workProgress = progress;
		String workObjectDesc = null;
		if (entry instanceof AppBackupEntry) {
			workObjectDesc = EntryType.getDescription(mContext, EntryType.TYPE_USER_APP)
					+ mContext.getString(R.string.parenthesized_msg, entry.getDescription());
			workDetail.workObjectType = EntryType.TYPE_USER_APP;
		} else if (entry instanceof OneImageBackupEntry) {
			workObjectDesc = EntryType.getDescription(mContext, EntryType.TYPE_USER_IMAGE)
					+ mContext.getString(R.string.parenthesized_msg, entry.getDescription());
			workDetail.workObjectType = EntryType.TYPE_USER_IMAGE;
		} else {
			workObjectDesc = entry.getDescription();
			workDetail.workObjectType = entry.getType();
		}
		BaseBackupEntry.BackupState state = entry.getState();
		if (state == BackupState.READY_TO_BACKUP) {
			workDetail.title = workObjectDesc;
		} else if (state == BackupState.BACKUPING) {
			workDetail.title = mContext.getString(R.string.state_backingup, workObjectDesc);
		} else {
			workDetail.title = workObjectDesc;
		}
		return workDetail;
	}

	private WorkDetailBean buildWorkDetail(Task task, WorkState workState, String progress) {
		WorkDetailBean workDetail = new WorkDetailBean();
		workDetail.workId = task.taskObject.value();
		EntryType entryType = TaskObject.toEntryType(task.taskObject);
		String workObjectDesc = EntryType.getDescription(mContext, entryType);
		if (task.taskObject == TaskObject.UPLOAD_DB) {
			workObjectDesc = mContext.getString(R.string.database_file);
		} else if (task.taskObject == TaskObject.APP && workState != WorkState.NOT_START) {
			workObjectDesc += mContext.getString(R.string.parenthesized_msg, task.extraInfo[0]);
		} else if (task.taskObject == TaskObject.IMAGE && workState != WorkState.NOT_START) {
			workObjectDesc += mContext.getString(R.string.parenthesized_msg, task.extraInfo[0]);
		}
		workDetail.workObjectType = entryType;
		workDetail.workProgress = progress;
		if (workState == WorkState.NOT_START) {
			workDetail.title = workObjectDesc;
		} else if (workState == WorkState.WORKING) {
			workDetail.title = mContext.getString(R.string.state_uploading, workObjectDesc);
		} else {
			workDetail.title = workObjectDesc;
		}
		return workDetail;
	}

	private void cancelAllRemainTasks() {
		Task task = null;
		while ((task = getNextTask()) != null) {
			task.taskState = TaskState.CANCELED;
			if (task.taskObject == TaskObject.APP) {
				mCanceledAppTaskCount++;
			}

			if (task.taskObject == TaskObject.IMAGE) {
				mCanceledImageTaskCount++;
			}

			// 数据库不展示结果
			if (task.taskObject == TaskObject.UPLOAD_DB) {
				continue;
			}

			addUploadTaskResult(buildResultBean(task));
		}
	}

	private void addUploadTaskResult(ResultBean resultBean) {
		if (!mTaskResults.contains(resultBean)) {
			mTaskResults.add(resultBean);
		}
	}

	private ResultBean[] getResults() {
		return mTaskResults.toArray(new ResultBean[mTaskResults.size()]);
	}

	/*
	 * private ResultBean buildResultBean(BaseBackupEntry curBackupEntry) { ResultBean resultBean =
	 * new ResultBean(); BackupState state = curBackupEntry.getState(); resultBean.result = state ==
	 * BackupState.BACKUP_SUCCESSFUL; String entryDesc = curBackupEntry.getDescription(); if (state
	 * == BackupState.BACKUP_SUCCESSFUL) { if(curBackupEntry instanceof AppBackupEntry) { if
	 * (mAppsSuccessfulResultBean == null) { mAppsSuccessfulResultBean = new ResultBean(); }
	 * resultBean = mAppsSuccessfulResultBean; } resultBean.title =
	 * mContext.getString(R.string.msg_restore_successful, entryDesc); } else if (state ==
	 * BackupState.BACKUP_ERROR_OCCURRED) { if(curBackupEntry instanceof AppBackupEntry) { if
	 * (mAppsFailedResultBean == null) { mAppsFailedResultBean = new ResultBean(); } resultBean =
	 * mAppsFailedResultBean; } if (curBackupEntry instanceof AppBackupEntry) { resultBean.title =
	 * mContext.getString(R.string.msg_restore_failed, EntryType.getDescription(mContext,
	 * EntryType.TYPE_USER_APP)); resultBean.title += mContext.getString(R.string.count_format,
	 * mFailedAppTaskCount); } else { resultBean.title =
	 * mContext.getString(R.string.msg_restore_failed, entryDesc); } } else if (state ==
	 * BackupState.BACKUP_CANCELED) { resultBean.title =
	 * mContext.getString(R.string.msg_restore_canceled, entryDesc); } return resultBean; }
	 */

	private ResultBean mAppsSuccessfulResultBean = null;
	private ResultBean mAppsFailedResultBean = null;
	private ResultBean mAppsCancelResultBean = null;

	private ResultBean mImageSuccessfulResultBean = null;
	private ResultBean mImageFailedResultBean = null;
	private ResultBean mImageCancelResultBean = null;

	private ResultBean buildResultBean(Task task) {
		ResultBean resultBean = new ResultBean();
		String entryDesc = EntryType.getDescription(mContext,
				TaskObject.toEntryType(task.taskObject));

		switch (task.taskState) {
			case FAILED :
				if (task.taskObject == TaskObject.APP) {
					if (mAppsFailedResultBean == null) {
						mAppsFailedResultBean = new ResultBean();
					}
					resultBean = mAppsFailedResultBean;
				}

				if (task.taskObject == TaskObject.IMAGE) {
					if (mImageFailedResultBean == null) {
						mImageFailedResultBean = new ResultBean();
					}
					resultBean = mImageFailedResultBean;
				}
				resultBean.result = false;
				resultBean.title = mContext.getString(R.string.msg_upload_failed, "", entryDesc);
				if (task.taskObject == TaskObject.APP) {
					String count = mContext.getString(R.string.count_format, mFailedAppTaskCount);
					resultBean.title = mContext.getString(R.string.msg_upload_failed, count,
							entryDesc);
				}
				if (task.taskObject == TaskObject.IMAGE) {
					String count = mContext.getString(R.string.count_format, mFailedImageTaskCount);
					resultBean.title = mContext.getString(R.string.msg_upload_failed, count, " "
							+ entryDesc);
				}
				break;
			case CANCELED :
				if (task.taskObject == TaskObject.APP) {
					if (mAppsCancelResultBean == null) {
						mAppsCancelResultBean = new ResultBean();
					}
					resultBean = mAppsCancelResultBean;
				}

				if (task.taskObject == TaskObject.IMAGE) {
					if (mImageCancelResultBean == null) {
						mImageCancelResultBean = new ResultBean();
					}
					resultBean = mImageCancelResultBean;
				}

				resultBean.result = false;
				resultBean.title = mContext.getString(R.string.msg_upload_canceled, "", entryDesc);
				if (task.taskObject == TaskObject.APP) {
					String count = mContext.getString(R.string.count_format, mCanceledAppTaskCount);
					resultBean.title = mContext.getString(R.string.msg_upload_canceled, count,
							entryDesc);
				}
				if (task.taskObject == TaskObject.IMAGE) {
					String count = mContext.getString(R.string.count_format,
							mCanceledImageTaskCount);
					resultBean.title = mContext.getString(R.string.msg_upload_canceled, count, " "
							+ entryDesc);
				}
				break;
			case FINISHED :
				if (task.taskObject == TaskObject.APP) {
					if (mAppsSuccessfulResultBean == null) {
						mAppsSuccessfulResultBean = new ResultBean();
					}
					resultBean = mAppsSuccessfulResultBean;
				}
				if (task.taskObject == TaskObject.IMAGE) {
					if (mImageSuccessfulResultBean == null) {
						mImageSuccessfulResultBean = new ResultBean();
					}
					resultBean = mImageSuccessfulResultBean;
				}
				resultBean.result = true;
				resultBean.title = mContext
						.getString(R.string.msg_upload_successful, "", entryDesc);
				if (task.taskObject == TaskObject.APP) {
					String count = mContext.getString(R.string.count_format, mFinishedAppTaskCount);
					resultBean.title = mContext.getString(R.string.msg_upload_successful, count,
							" " + entryDesc);
				}
				if (task.taskObject == TaskObject.IMAGE) {
					String count = mContext.getString(R.string.count_format,
							mFinishedImageTaskCount);
					resultBean.title = mContext.getString(R.string.msg_upload_successful, count,
							" " + entryDesc);
				}
				break;
		}
		return resultBean;
	}

}
