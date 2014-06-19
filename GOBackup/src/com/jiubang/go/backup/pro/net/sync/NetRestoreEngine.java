package com.jiubang.go.backup.pro.net.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppRestoreEntry;
import com.jiubang.go.backup.pro.data.AppRestoreEntry.AppRestoreArgs;
import com.jiubang.go.backup.pro.data.AppRestoreEntry.AppRestoreType;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry.RestoreState;
import com.jiubang.go.backup.pro.data.BookMarkRestoreEntry;
import com.jiubang.go.backup.pro.data.CalendarRestoreEntry;
import com.jiubang.go.backup.pro.data.CallLogRestoreEntry;
import com.jiubang.go.backup.pro.data.ContactsRestoreEntry;
import com.jiubang.go.backup.pro.data.GoLauncherSettingRestoreEntry;
import com.jiubang.go.backup.pro.data.IRestorable.RestoreArgs;
import com.jiubang.go.backup.pro.data.LauncherDataRestoreEntry;
import com.jiubang.go.backup.pro.data.LauncherDataRestoreEntry.LauncherDataExtraInfo;
import com.jiubang.go.backup.pro.data.MessageReceiver;
import com.jiubang.go.backup.pro.data.MmsBackupEntry;
import com.jiubang.go.backup.pro.data.MmsRestoreEntry;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.data.RingtoneRestoreEntry;
import com.jiubang.go.backup.pro.data.SmsRestoreEntry;
import com.jiubang.go.backup.pro.data.UserDictionaryRestoreEntry;
import com.jiubang.go.backup.pro.data.WallpaperRestoreEntry;
import com.jiubang.go.backup.pro.data.WifiRestoreEntry;
import com.jiubang.go.backup.pro.image.util.GroupRestoreEntry;
import com.jiubang.go.backup.pro.image.util.ImageBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageBean;
import com.jiubang.go.backup.pro.image.util.ImageRestoreEntry;
import com.jiubang.go.backup.pro.image.util.OneImageRestoreEntry;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.Task;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskObject;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskState;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskType;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 */
public class NetRestoreEngine extends AsyncWorkEngine
		implements
			IAsyncTaskListener,
			MessageReceiver,
			ActionListener {
	// private static final int MSG_INIT_TASK = 0x1001;
	private static final int MSG_DOWNLOAD_BACKUP_FILE = 0x1002;
	// private static final int MSG_START_RESTORE = 0x1003;
	private static final int MSG_SHOW_TOAST = 0x1004;

	private static final float DOWNLOAD_PROGRESS_WEIGHT = 0.5f;
	private static final float RESTORE_PROGRESS_WEIGHT = 1.0f - DOWNLOAD_PROGRESS_WEIGHT;

	private static final int STATE_RESTORING = 2;
	private static final int STATE_IDLE = 3;

	private FileHostingServiceProvider mService;
	private Context mContext;
	private IAsyncTaskListener mListener;
	// 需要操作的数据库
	private NetSyncTaskDbHelper mTaskDbHelper;
	private List<Task> mAllRestoreTasks;
	private List<ResultBean> mTaskResults = new ArrayList<ResultBean>();
	private List<String> mTaskFilesToDownload;
	private List<File> mFilesDownloaded = new ArrayList<File>();
	private int mCurDownloadFileIndex;
	private Task mCurRestoreTask;
	//	private boolean mCurTaskCanceled = false;
	private BaseRestoreEntry mCurRestoreEntry;
	private CancelableTask mCurDownloadTask;
	// 总共恢复的Entry的数目
	private int mTotalTaskCount;
	// 恢复成功的Entry的数目
	private int mSuccessfulTaskCount;
	// 当前恢复指示
	private int mCurRestoreIndex;
	// 要恢复的应用程序个数
	private int mAppTaskCount;
	private int mImageTaskCount;
	// 已完成的应用程序任务个数
	private int mCompletedAppTaskCount = 0;
	private int mFinishedAppTaskCount = 0;
	private int mCanceledAppTaskCount = 0;
	private int mFailedAppTaskCount = 0;

	private int mCompletedImageTaskCount = 0;
	private int mFinishedImageTaskCount = 0;
	private int mCanceledImageTaskCount = 0;
	private int mFailedImageTaskCount = 0;

	// 当前恢复的进度
	private float mCurrentProgress;
	private float mProgressUnit;
	// 额外提示信息
	private String mExtraTips;
	// 是否需要重启
	private boolean mShouldReboot;
	// 当前恢复的文件名称
	private boolean mIsRoot;
	// private boolean mHasLastTaskFinished = true;
	private File mCacheDir;
	private boolean mIsNetError = false;

	private int mState;
	public static String sSdCarkPath = null;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/*
			 * case MSG_INIT_TASK: initRestoreTask(( Task ) msg.obj); break;
			 */
				case MSG_DOWNLOAD_BACKUP_FILE :
					downloadFile(msg.obj.toString());
					break;
				/*
				 * case MSG_START_RESTORE : startRestore(( Task ) msg.obj); break;
				 */
				case MSG_SHOW_TOAST :
					if (msg.obj != null) {
						Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_LONG).show();
					}
					break;
				default :
					break;
			}
		}
	};

	private Handler mAsyncWorkHandler;

	public NetRestoreEngine(Context context, NetSyncTaskDbHelper dbHelper,
			IAsyncTaskListener listener) {
		super("RestoreEngine");
		if (context == null) {
			throw new IllegalArgumentException("RestoreEngine invalid argument");
		}
		mContext = context;
		mService = CloudServiceManager.getInstance().getCurrentService();
		sSdCarkPath = Util.getDefalutValidSdPath(mContext);
		mListener = listener;
		mTaskDbHelper = dbHelper;
	}

	@Override
	protected void onWorkStart() {
		init();
		if (mListener != null) {
			mListener.onStart(initAllTaskDetail(), null);
		}
	}

	@Override
	protected void onWorkFinish(boolean finishNormal) {
		if (!finishNormal) {
			cancelAllRemainTasks();
		}
		mTaskDbHelper.close();
		boolean result = mTotalTaskCount == mSuccessfulTaskCount;
		if (result) {
			mContext.deleteDatabase(NetSyncTaskDbHelper.getDbName());
		}
		if (mListener != null) {
			mListener.onEnd(result, getResults(), mShouldReboot);
		}

		if (mAsyncWorkHandler != null) {
			mAsyncWorkHandler.getLooper().quit();
		}

		if (mService != null) {
			mService.release();
		}
		super.onWorkFinish(finishNormal);
	}

	@Override
	public boolean handleMessage(int arg1, int arg2, Object obj) {
		if (mCurRestoreEntry != null && mCurRestoreEntry instanceof MessageReceiver) {
			return ((MessageReceiver) mCurRestoreEntry).handleMessage(arg1, arg2, obj);
		}
		return false;
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
		if (arg2 instanceof BaseRestoreEntry) {
			notifyRestoreProgressUpdated((Float) progress, (BaseRestoreEntry) arg2, mExtraTips);
		} else if (arg2 instanceof CancelableTask) {
			notifyDownloadProgressUpdated((Float) progress);
		}
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		if (success) {
			mSuccessfulTaskCount++;
		}
		if (arg1 instanceof AppRestoreEntry) {
			mCompletedAppTaskCount++;
		}
		if (arg1 instanceof OneImageRestoreEntry) {
			mCompletedImageTaskCount++;
		}
		mState = STATE_IDLE;
		onFinishTask(mCurRestoreTask, success ? TaskState.FINISHED : TaskState.FAILED);
	}

	private void onFinishTask(Task task, TaskState state) {
		if (task.taskObject == TaskObject.APP) {
			// 应用程序无论成功还是失败，都计入完成
			//			mCompletedAppTaskCount++;
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
			//			mCompletedImageTaskCount++;
			if (state == TaskState.CANCELED) {
				mCanceledImageTaskCount++;
			} else if (state == TaskState.FAILED) {
				mFailedImageTaskCount++;
			} else if (state == TaskState.FINISHED) {
				mFinishedImageTaskCount++;
			}
		}

		if (mCurRestoreEntry != null
				&& TaskObject.valueOf(mCurRestoreEntry.getType()) == task.taskObject) {
			RestoreState entryState = state == TaskState.FINISHED
					? RestoreState.RESTORE_SUCCESSFUL
					: state == TaskState.FAILED
							? RestoreState.RESTORE_ERROR_OCCURRED
							: RestoreState.RESTORE_CANCELED;
			mCurRestoreEntry.setState(entryState);
		}

		updateTaskState(task, state);
		addRestoreTaskResult(buildResultBean(task));

		if (mCurRestoreEntry != null) {
			if (mCurRestoreEntry.getState() == RestoreState.RESTORE_ERROR_OCCURRED) {
				mExtraTips = mContext.getString(R.string.state_failed);
			}
			notifyRestoreProgressUpdated(1.0f, mCurRestoreEntry, mExtraTips);
		}
		// 更新进度
		mCurrentProgress += getProgressWeight(mCurRestoreTask) * mProgressUnit;
		if (mListener != null) {
			mListener.onProceeding((int) mCurrentProgress, mCurRestoreTask, null, null);
		}

		WorkState currentWorkState = null;
		if (state == TaskState.CANCELED || state == TaskState.FAILED) {
			currentWorkState = WorkState.FAILED;
		} else {
			currentWorkState = WorkState.COMPLETED;
		}
		setCurrentWorkState(currentWorkState);

		deleteDownloadedFile();
		if (mTaskFilesToDownload != null) {
			mTaskFilesToDownload.clear();
		}
		mCurDownloadFileIndex = 0;
		mCurDownloadTask = null;

		continueToWork();
	}

	private void notifyDownloadProgressUpdated(float progress) {
		if (mListener == null) {
			return;
		}
		float curTaskProgressSum = getProgressWeight(mCurRestoreTask) * mProgressUnit;
		float curTaskProgress = (curTaskProgressSum * DOWNLOAD_PROGRESS_WEIGHT) * progress;
		String progressFormat = mContext
				.getString(R.string.progress_format, (int) (progress * 100));
		WorkDetailBean workDetailBean = buildWorkDetail(mCurRestoreTask, WorkState.WORKING,
				progressFormat);
		final int curProgress = (int) (mCurrentProgress + curTaskProgress);
		mListener.onProceeding(curProgress, mCurRestoreTask, workDetailBean, null);
	}

	private void notifyRestoreProgressUpdated(float progress, BaseRestoreEntry entry,
			String progressMsg) {
		if (mListener == null || entry == null) {
			return;
		}
		float curTaskProgressSum = getProgressWeight(mCurRestoreTask) * mProgressUnit;
		final float restoreProgress = (curTaskProgressSum * RESTORE_PROGRESS_WEIGHT) * progress;
		final float downloadProgress = curTaskProgressSum * DOWNLOAD_PROGRESS_WEIGHT;
		float curTaskProgress = downloadProgress + restoreProgress;
		String extraTip = progressMsg;
		if (entry instanceof AppRestoreEntry) {
			extraTip = mContext.getString(R.string.progress_detail, mCompletedAppTaskCount,
					mAppTaskCount);
		}

		if (entry instanceof ImageRestoreEntry) {
			extraTip = mContext.getString(R.string.progress_detail, mCompletedImageTaskCount,
					mImageTaskCount);
		}
		if (extraTip == null) {
			extraTip = mContext.getString(R.string.state_finished);
		}
		WorkDetailBean workDetailBean = buildWorkDetail(mCurRestoreTask.taskObject.value(), entry,
				extraTip);
		final int curProgress = (int) (mCurrentProgress + curTaskProgress);
		mListener.onProceeding(curProgress, mCurRestoreTask, workDetailBean, null);
	}

	private void deleteDownloadedFile() {
		// Log.d("GOBackup", "NetRestoreEngine : deleteDownloadedFile()");
		if (!Util.isCollectionEmpty(mFilesDownloaded)) {
			for (File file : mFilesDownloaded) {
				Util.deleteFile(file.getAbsolutePath());
			}
			mFilesDownloaded.clear();
		}
		File file = new File(mCacheDir, MmsBackupEntry.MMS_DIR_NAME);
		if (file.exists()) {
			Util.deleteFile(file.getAbsolutePath());
		}
		File imageFile = new File(mCacheDir, ImageBackupEntry.IMAGE_DIR_NAME);
		if (imageFile.exists()) {
			Util.deleteFile(imageFile.getAbsolutePath());
		}
	}

	@Override
	protected boolean hasNextWork() {
		return !Util.isCollectionEmpty(mAllRestoreTasks) && mCurRestoreIndex + 1 < mTotalTaskCount;
	}

	@Override
	protected void continueUnfinishedWork() {
		super.continueUnfinishedWork();

		if (isForceStopped()) {
			// 若已被强制停止，直接结束
			onFinishTask(mCurRestoreTask, TaskState.CANCELED);
			return;
		}

		if (mState == STATE_RESTORING) {
			return;
		}
		redoLastTask();
	}

	@Override
	protected void doNextWork() {
		// Log.d("GOBackup", "NetRestoreEngine : doNextWork()");
		// 重做上一次的任务由框架来控制
		/*
		 * // 上次任务是否完成，有可能因为暂停而没有完成 if (!hasLastTaskFinished()) { Log.d("GOBackup",
		 * "doNextWork : redoLastTask"); redoLastTask(); return; }
		 */
		setCurrentWorkState(WorkState.WORKING);
		mCurRestoreTask = getNextRestoreTask();
		//		mCurTaskCanceled = false;
		mCurRestoreEntry = null;
		final int curProgress = (int) mCurrentProgress;
		if (mListener != null) {
			mListener.onProceeding(curProgress, mCurRestoreTask, null, null);
		}
		// Message.obtain(mHandler, MSG_INIT_TASK,
		// mCurRestoreTask).sendToTarget();
		initRestoreTask(mCurRestoreTask);

	}

	/*
	 * private boolean hasLastTaskFinished() { // if (mCurRestoreTask != null &&
	 * mCurRestoreTask.taskState == // TaskState.NOT_START) { // return false; // } // return true;
	 * return mHasLastTaskFinished; }
	 */

	private void redoLastTask() {
		if (mTaskFilesToDownload != null && mCurDownloadFileIndex < mTaskFilesToDownload.size()) {
			// Log.d("GOBackup", "redoLastTask : mTaskFilesToDownload = " + mTaskFilesToDownload
			// + ", mCurDownloadFileIndex = " + mCurDownloadFileIndex
			// + ", mTaskFilesToDownload.size = " + mTaskFilesToDownload.size());
			Message.obtain(mHandler, MSG_DOWNLOAD_BACKUP_FILE,
					mTaskFilesToDownload.get(mCurDownloadFileIndex)).sendToTarget();
			return;
		}
		// Log.d("GOBackup", "redoLastTask : start restore");
		startRestore(mCurRestoreTask);
	}

	private Task getNextRestoreTask() {
		if (mCurRestoreIndex + 1 >= mTotalTaskCount) {
			return null;
		}
		return mAllRestoreTasks.get(++mCurRestoreIndex);
	}

	@Override
	public String getNotificationMessage() {
		if (mCurRestoreEntry != null) {
			return mContext.getString(R.string.msg_restoring, mCurRestoreEntry.getDescription(),
					mExtraTips != null ? mExtraTips : "");
		}
		if (mCurRestoreTask != null) {
			return mContext.getString(R.string.msg_downloading,
					getTaskDescription(mCurRestoreTask), mExtraTips != null ? mExtraTips : "");
		}
		return "";
	}

	private void init() {
		if (mTaskDbHelper == null) {
			return;
		}
		mIsRoot = RootShell.isRootValid();
		// 根据数据库里面的数据生成恢复列表
		mAllRestoreTasks = mTaskDbHelper.getAllNotFinishedTasks(TaskType.ONLINE_RESTORE);
		mTotalTaskCount = mAllRestoreTasks != null ? mAllRestoreTasks.size() : 0;
		mAppTaskCount = calcAppTaskCount(mAllRestoreTasks);
		mImageTaskCount = calcImageTaskCount(mAllRestoreTasks);
		mCompletedAppTaskCount = 0;
		mCompletedImageTaskCount = 0;
		mSuccessfulTaskCount = 0;
		mCurRestoreIndex = -1;
		mCurRestoreEntry = null;
		mCurrentProgress = 0.0f;
		calcProgressUnit();
		mCacheDir = getCacheDir();

		initEngineLocalThread();
	}

	private void initEngineLocalThread() {
		HandlerThread thread = new HandlerThread("AsyncThread");
		thread.start();
		mAsyncWorkHandler = new Handler(thread.getLooper());
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

	private void calcProgressUnit() {
		if (Util.isCollectionEmpty(mAllRestoreTasks)) {
			mProgressUnit = 1.0f;
			return;
		}
		final float m100f = 100.0f;
		int sum = 0;
		for (Task task : mAllRestoreTasks) {
			sum += getProgressWeight(task);
		}
		mProgressUnit = sum > 0 ? m100f / sum : 1.0f;
	}

	private int getProgressWeight(Task task) {
		if (task == null) {
			return 0;
		}
		// TODO 目前所有Task的进度权重相同
		final int taskProgressWeight = 10;
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
			case CALENDAR :
			case BOOKMARK :
			case IMAGE :
				return taskProgressWeight;
			default :
				break;
		}
		return 0;
	}

	@Override
	public synchronized void forceToStop() {
		//		mCurTaskCanceled = true;
		if (mCurDownloadTask != null) {
			mCurDownloadTask.cancel();
		}
		if (mCurRestoreEntry != null) {
			mCurRestoreEntry.stopRestore();
		}
		super.forceToStop();
	}

	@Override
	public synchronized void pause() {
		super.pause();
		// Log.d("GOBackup", "pause");
		if (mCurDownloadTask != null) {
			mCurDownloadTask.cancel();
			mCurDownloadTask = null;
		}
	}

	private ResultBean[] getResults() {
		return mTaskResults.toArray(new ResultBean[mTaskResults.size()]);
	}

	private void initRestoreTask(Task task) {
		if (mTaskFilesToDownload == null) {
			mTaskFilesToDownload = new ArrayList<String>();
		}
		mTaskFilesToDownload.clear();

		// mHasLastTaskFinished = false;
		mExtraTips = null;

		if (task.paths == null || task.paths.length < 1) {
			return;
		}
		final int count = task.paths.length;
		for (int i = 0; i < count; i++) {
			if (TextUtils.isEmpty(task.paths[i])) {
				continue;
			}
			mTaskFilesToDownload.add(task.paths[i]);
		}
		mCurDownloadFileIndex = 0;
		// 文件下载使用了AsyncTask， 必须在主线程上执行
		String fileToDownload = mTaskFilesToDownload.get(mCurDownloadFileIndex);
		Message.obtain(mHandler, MSG_DOWNLOAD_BACKUP_FILE, fileToDownload).sendToTarget();
	}

	private void downloadFile(String path) {
		if (isPaused()) {
			return;
		}

		if (isForceStopped()) {
			onFinishTask(mCurRestoreTask, TaskState.CANCELED);
			return;
		}

		if (TextUtils.isEmpty(path)) {
			throw new IllegalArgumentException("invalid path!");
		}
		if (mCurRestoreTask.taskObject == TaskObject.IMAGE) {
			String imageDir = ImageBackupEntry.IMAGE_DIR_NAME;
			String imagePath = mCacheDir.getAbsolutePath() + File.separator
					+ path.substring(path.indexOf(imageDir), path.lastIndexOf(File.separator));
			File cacheDir = new File(imagePath);
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			mCurDownloadTask = mService.downloadFile(path, cacheDir, null, this);
		} else {
			mCurDownloadTask = mService.downloadFile(path, mCacheDir, null, this);
		}
	}

	@Override
	public void onComplete(Object data) {
		OnlineFileInfo fileInfo = (OnlineFileInfo) data;
		// Log.d("GOBackup", "onComplete : fileInfo = " + fileInfo.getFileName());
		File cacheFile = new File(mCacheDir, fileInfo.getFileName());
		if (cacheFile.exists()) {
			mFilesDownloaded.add(cacheFile);
		}

		float curDownloadProgress = (float) (mCurDownloadFileIndex + 1)
				/ mTaskFilesToDownload.size();
		onProceeding(curDownloadProgress, mCurDownloadTask, null, null);

		// 如果已经暂停，则不再继续下一步操作
		if (isPaused()) {
			return;
		}

		if (isForceStopped()) {
			onFinishTask(mCurRestoreTask, TaskState.CANCELED);
			return;
		}

		continueNextDownloadTask();
	}

	private void continueNextDownloadTask() {
		mCurDownloadTask = null;
		// 全部备份文件已下完成，开始恢复
		if (++mCurDownloadFileIndex >= mTaskFilesToDownload.size()) {
			// Message.obtain(mHandler, MSG_START_RESTORE, mCurRestoreTask)
			// .sendToTarget();
			if (mAsyncWorkHandler != null) {
				// onComplete 是从ui线程回调，不能做耗时操作
				mState = STATE_RESTORING;
				mAsyncWorkHandler.post(new Runnable() {
					@Override
					public void run() {
						startRestore(mCurRestoreTask);
					}
				});
			}
		} else {
			Message.obtain(mHandler, MSG_DOWNLOAD_BACKUP_FILE,
					mTaskFilesToDownload.get(mCurDownloadFileIndex)).sendToTarget();
		}
	}

	@Override
	public void onError(int errCode, String errMessage, Object data) {
		// Log.d("GOBackup", "NetRestoreEngine : onError : errCode = " + errCode + ", errMessage = "
		// + errMessage);
		if (errCode == FileHostingServiceProvider.NETWORK_IO_ERROR && !mIsNetError) {
			mIsNetError = true;
			Message.obtain(mHandler, MSG_SHOW_TOAST, mContext.getString(R.string.neterror))
					.sendToTarget();
		}
		synchronized (this) {
			// 备份文件下载失败，当前网络状态不佳，直接驱动下一项
			onFinishTask(mCurRestoreTask, TaskState.FAILED);
		}
	}

	@Override
	public void onCancel(Object data) {
		// Log.d("GOBackup", "onCancel");
		if (isForceStopped()) {
			onFinishTask(mCurRestoreTask, TaskState.CANCELED);
		}
	}

	private void updateTaskState(Task task, TaskState state) {
		if (task == null || state == null) {
			return;
		}
		task.taskState = state;
		mTaskDbHelper.updateTaskState(task, state);
	}

	private void startRestore(Task task) {
		// Log.d("GOBackup", "NetRestoreEngine : startRestore()");
		mCurDownloadTask = null;

		mCurRestoreEntry = buildLocalRestoreEntry(task);
		if (mCurRestoreEntry == null) {
			onFinishTask(task, TaskState.FAILED);
			return;
		}

		notifyRestoreProgressUpdated(0, mCurRestoreEntry,
				mContext.getString(R.string.progress_format, 0));

		boolean result = false;
		if (mCurRestoreEntry instanceof AppRestoreEntry) {
			AppRestoreArgs args = new AppRestoreArgs();
			args.mIsRoot = mIsRoot;
			args.mAppRestoreType = mIsRoot ? AppRestoreType.APP_DATA : AppRestoreType.APP;
			args.mSilentRestore = mIsRoot;
			args.mRestoreResPath = mCacheDir.getAbsolutePath();
			result = mCurRestoreEntry.restore(mContext, args, this);
		} else if (mCurRestoreEntry instanceof ImageRestoreEntry) {
			RestoreArgs args = new RestoreArgs();
			args.mRestorePath = mCacheDir.getAbsolutePath();
			result = mCurRestoreEntry.restore(mContext, args, this);
		} else {
			RestoreArgs args = new RestoreArgs();
			args.mRestorePath = mCacheDir.getAbsolutePath();
			result = mCurRestoreEntry.restore(mContext, args, this);
		}
		if (!result) {
			// 驱动下一项
			onFinishTask(task, TaskState.FAILED);
		}
	}

	private void cancelAllRemainTasks() {
		Task task = null;
		while ((task = getNextRestoreTask()) != null) {
			task.taskState = TaskState.CANCELED;
			// mTaskDbHelper.updateTaskState(task, TaskState.CANCELED);
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
			addRestoreTaskResult(buildResultBean(task));
		}
	}

	private void addRestoreTaskResult(ResultBean resultBean) {
		if (!mTaskResults.contains(resultBean)) {
			mTaskResults.add(resultBean);
		}
	}

	/*
	 * private ResultBean buildResultBean(BaseRestoreEntry curRestoreEntry) { ResultBean resultBean
	 * = new ResultBean(); RestoreState state = curRestoreEntry.getState(); resultBean.result =
	 * state == RestoreState.RESTORE_SUCCESSFUL; String entryDesc =
	 * curRestoreEntry.getDescription(); if (state == RestoreState.RESTORE_SUCCESSFUL) {
	 * resultBean.title = mContext.getString(R.string.msg_restore_successful, entryDesc); } else if
	 * (state == RestoreState.RESTORE_ERROR_OCCURRED) { resultBean.title =
	 * mContext.getString(R.string.msg_restore_failed, entryDesc); } else if (state ==
	 * RestoreState.RESTORE_CANCELED) { resultBean.title =
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
				resultBean.title = mContext.getString(R.string.msg_restore_failed, "", entryDesc);
				if (task.taskObject == TaskObject.APP) {
					String count = mContext.getString(R.string.count_format, mFailedAppTaskCount);
					resultBean.title = mContext.getString(R.string.msg_restore_failed, count, " "
							+ entryDesc);
				}
				if (task.taskObject == TaskObject.IMAGE) {
					String count = mContext.getString(R.string.count_format, mFailedImageTaskCount);
					resultBean.title = mContext.getString(R.string.msg_restore_failed, count, " "
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
				resultBean.title = mContext.getString(R.string.msg_restore_canceled, "", entryDesc);
				if (task.taskObject == TaskObject.APP) {
					String count = mContext.getString(R.string.count_format, mCanceledAppTaskCount);
					resultBean.title = mContext.getString(R.string.msg_restore_canceled, count, " "
							+ entryDesc);
				}
				if (task.taskObject == TaskObject.IMAGE) {
					String count = mContext.getString(R.string.count_format,
							mCanceledImageTaskCount);
					resultBean.title = mContext.getString(R.string.msg_restore_canceled, count, " "
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
				resultBean.title = mContext.getString(R.string.msg_restore_successful, "",
						entryDesc);
				if (task.taskObject == TaskObject.APP) {
					String count = mContext.getString(R.string.count_format, mFinishedAppTaskCount);
					resultBean.title = mContext.getString(R.string.msg_restore_successful, count,
							" " + entryDesc);
				}
				if (task.taskObject == TaskObject.IMAGE) {
					String count = mContext.getString(R.string.count_format,
							mFinishedImageTaskCount);
					resultBean.title = mContext.getString(R.string.msg_restore_successful, count,
							" " + entryDesc);
				}
				break;
		}
		return resultBean;
	}

	private File getCacheDir() {
		File cacheDir = new File(Constant.buildNetworkBackupCacheDir(mContext));
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return cacheDir;
	}

	private BaseRestoreEntry buildLocalRestoreEntry(Task task) {
		final String localCacheDir = mCacheDir.getAbsolutePath();
		switch (task.taskObject) {
			case SMS :
				return new SmsRestoreEntry(mContext, localCacheDir);
			case CALLLOG :
				return new CallLogRestoreEntry(mContext, localCacheDir);
			case CONTACTS :
				return new ContactsRestoreEntry(mContext, localCacheDir);
			case GOLAUNCHER_SETTING :
				return new GoLauncherSettingRestoreEntry(mContext, localCacheDir);
			case MMS :
				return new MmsRestoreEntry(mContext, localCacheDir);
			case BOOKMARK :
				return new BookMarkRestoreEntry(mContext, localCacheDir);
			case USER_DICTIONARY :
				return new UserDictionaryRestoreEntry(mContext, localCacheDir);
			case WIFI :
				String wifiPath = task.extraInfo != null ? task.extraInfo[1].toString() : null;
				return new WifiRestoreEntry(mContext, localCacheDir, wifiPath);
			case LAUNCHER_DATA :
				String packageName = task.extraInfo == null ? null : String
						.valueOf(task.extraInfo[0]);
				LauncherDataExtraInfo launcherDataExtraInfo = new LauncherDataExtraInfo();
				launcherDataExtraInfo.packageName = packageName;
				return new LauncherDataRestoreEntry(mContext, localCacheDir, launcherDataExtraInfo);
			case RINGTONE :
				return new RingtoneRestoreEntry(mContext, localCacheDir);
			case WALLPAPER :
				return new WallpaperRestoreEntry(mContext, localCacheDir);
			case APP :
				AppInfo appInfo = new AppInfo();
				appInfo.appName = task.extraInfo != null ? task.extraInfo[0].toString() : null;
				appInfo.packageName = task.extraInfo != null ? task.extraInfo[1].toString() : null;
				appInfo.appType = task.extraInfo != null ? Integer
						.valueOf((String) task.extraInfo[2]) : AppInfo.APP_USER;
				return new AppRestoreEntry(appInfo, localCacheDir);
			case CALENDAR :
				return new CalendarRestoreEntry(mContext, localCacheDir);

			case IMAGE :
				BaseRestoreEntry entry = new ImageRestoreEntry(mContext, false);
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

				OneImageRestoreEntry restoreEntry = new OneImageRestoreEntry(mContext, image,
						localCacheDir);
				((GroupRestoreEntry) entry).addEntry(restoreEntry);
				return entry;
			default :
				break;
		}
		return null;
	}

	private String getTaskDescription(Task task) {
		if (task == null) {
			return null;
		}
		switch (task.taskObject) {
			case SMS :
				return mContext.getString(R.string.sms);
			case CALLLOG :
				return mContext.getString(R.string.call_log);
			case CONTACTS :
				return mContext.getString(R.string.contacts);
			case GOLAUNCHER_SETTING :
				return mContext.getString(R.string.golauncher_setting);
			case MMS :
				return mContext.getString(R.string.mms);
			case USER_DICTIONARY :
				return mContext.getString(R.string.user_dictionary);
			case WIFI :
				return mContext.getString(R.string.wifi_access_points);
			case LAUNCHER_DATA :
				return mContext.getString(R.string.launcher_layout);
			case RINGTONE :
				return mContext.getString(R.string.ringtone);
			case APP :
				return task.extraInfo[0].toString();
			case CALENDAR :
				return mContext.getString(R.string.calendar);
			case BOOKMARK :
				return mContext.getString(R.string.bookmark);
			case WALLPAPER :
				return mContext.getString(R.string.wallpaper);
			case IMAGE :
				return task.extraInfo[0].toString();
			default :
				break;
		}
		return null;
	}

	@Override
	public void onProgress(long progress, long total, Object data) {
		// float curDownloadFileProgress = (float) progress / (float) total;
		// float curDownloadProgress = (float) mCurDownloadFileIndex /
		// mTaskFilesToDownload.size() * curDownloadFileProgress;
		// onProceeding(curDownloadProgress, mCurDownloadTask, null, null);

		float curDownloadFileProgress = (float) progress / (float) total;
		final int fileCount = mTaskFilesToDownload.size();
		float curDownloadProgress = (mCurDownloadFileIndex + curDownloadFileProgress) / fileCount;
		onProceeding(curDownloadProgress, mCurDownloadTask, null, null);
	}

	private List<WorkDetailBean> initAllTaskDetail() {
		if (Util.isCollectionEmpty(mAllRestoreTasks)) {
			return null;
		}
		List<WorkDetailBean> allTaskDetail = new ArrayList<AsyncWorkEngine.WorkDetailBean>();
		// 应用程序任务归为一个分类
		WorkDetailBean appWorkDetailBean = null;
		WorkDetailBean imageWorkDetailBean = null;
		for (Task task : mAllRestoreTasks) {
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

	private WorkDetailBean buildWorkDetail(int taskId, BaseRestoreEntry entry, String progress) {
		WorkDetailBean workDetail = new WorkDetailBean();
		workDetail.workId = taskId;
		workDetail.workProgress = progress;
		String workObject = null;
		if (entry instanceof AppRestoreEntry) {
			// workDetail.workObject = EntryType.getDescription(mContext,
			// EntryType.TYPE_USER_APP) +
			// mContext.getString(R.string.parenthesized_msg,
			// entry.getDescription());
			workObject = EntryType.getDescription(mContext, EntryType.TYPE_USER_APP)
					+ mContext.getString(R.string.parenthesized_msg, entry.getDescription());
			workDetail.workObjectType = EntryType.TYPE_USER_APP;
		} else if (entry instanceof ImageRestoreEntry) {
			workObject = EntryType.getDescription(mContext, EntryType.TYPE_USER_IMAGE)
					+ mContext.getString(R.string.parenthesized_msg, ((ImageRestoreEntry) entry)
							.getEntryList().get(0).getDescription());
			workDetail.workObjectType = EntryType.TYPE_USER_IMAGE;
		} else {
			// workDetail.workObject = entry.getDescription();
			workObject = entry.getDescription();
			workDetail.workObjectType = entry.getType();
		}
		// final String workDesc = mContext.getString(R.string.work_restore);
		String workDesc = "";
		BaseRestoreEntry.RestoreState state = entry.getState();
		if (state == RestoreState.READY_TO_RESTORE) {
			workDetail.title = workObject;
		} else if (state == RestoreState.RESTORING) {
			workDetail.title = mContext.getString(R.string.state_restoring, workObject);
		} else {
			workDetail.title = workObject;
		}
		return workDetail;
	}

	private WorkDetailBean buildWorkDetail(Task task, WorkState workState, String progress) {
		WorkDetailBean workDetail = new WorkDetailBean();
		workDetail.workId = task.taskObject.value();
		EntryType entryType = TaskObject.toEntryType(task.taskObject);
		// workDetail.workObject = EntryType.getDescription(mContext,
		// entryType);
		String workObject = EntryType.getDescription(mContext, entryType);
		if (task.taskObject == TaskObject.APP && workState != WorkState.NOT_START) {
			// workDetail.workObject +=
			// mContext.getString(R.string.parenthesized_msg,
			// task.extraInfo[0]);
			workObject += mContext.getString(R.string.parenthesized_msg, task.extraInfo[0]);
		}
		if (task.taskObject == TaskObject.IMAGE && workState != WorkState.NOT_START) {
			workObject += mContext.getString(R.string.parenthesized_msg, task.extraInfo[0]);
		}
		workDetail.workProgress = progress;
		workDetail.workObjectType = entryType;
		// String workDesc = mContext.getString(R.string.work_download);
		if (workState == WorkState.NOT_START) {
			workDetail.title = workObject;
		} else if (workState == WorkState.WORKING) {
			workDetail.title = mContext.getString(R.string.state_downloading, workObject);
		} else {
			workDetail.title = workObject;
		}
		return workDetail;
	}
}
