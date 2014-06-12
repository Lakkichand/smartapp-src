/*
 * 文 件 名:  DownloadManager1.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-10-18
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppFileUtil;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-10-18]
 */
public class DownloadManager {
	/**
	 * 重连之后依然下载失败的任务，ID将会加入到此列表
	 * 等待下次网络重新连接的时候，再继续下载
	 */
	private List<Long> mFailList = null;
	/** 
	 * 下载没有完成时，文件将以 保存路径.tmp 保存，下载完成后，将会把.tmp去掉
	 */
	public static final String sDOWNLOADING_FORMAT = ".tmp";
	/**
	 * 单例
	 */
	private static DownloadManager staticSelf = null;
	
	/**
	 * 正在下载任务的ID集合
	 */
	private List<Long> mDownloadingTasksId = null;

	/**
	 * 等待下载任务的ID集合
	 */
	private List<Long> mWaitingTasksIdList = null;

	/**
	 * 下载任务的集合
	 */
	private ConcurrentHashMap<Long, DownloadTask> mDownloadConcurrentHashMap = null;

	/**
	 * 本身管理器的监听器
	 */
	private ConcurrentHashMap<Long, IAidlDownloadManagerListener> mDownloadManagerListener = null;
	/**
	 * context
	 */
	private Context mContext = null;
	/**
	 * 下载任务的并发个数
	 */
	//TODO:下载wangzhuobin 该值的修改是否要考虑线程同步，不应该由外部调用的人去保证
	private int mMaxConcurrentDownloadCount = 1;
	/**
	 * 下载完成的管理器
	 */
	private DownloadCompleteManager mCompleteManager = null;
	/**
	 * ASYNCTASK必须在UI线程创建，采用handler的方式进行创建
	 */
	public static final int MSG_START_DOWNLOAD = 1234;
	
	private Handler mHandler = null;
	
	protected DownloadManager(Context context) {
		mContext = context;
		mCompleteManager = new DownloadCompleteManager(context);
		mDownloadingTasksId = Collections.synchronizedList(new ArrayList<Long>());
		mWaitingTasksIdList = Collections.synchronizedList(new ArrayList<Long>());
		mDownloadConcurrentHashMap = new ConcurrentHashMap<Long, DownloadTask>();
		mDownloadManagerListener = new ConcurrentHashMap<Long, IAidlDownloadManagerListener>();
		mFailList = Collections.synchronizedList(new ArrayList<Long>());
		Looper mainLooper = Looper.getMainLooper();
		if (mainLooper != null) {
			mHandler = new Handler(mainLooper) {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case MSG_START_DOWNLOAD :
							DownloadTask task = (DownloadTask) msg.obj;
							DownloadAsyncTask downloadAsyncTask = new DownloadAsyncTask(task,
									staticSelf);
							downloadAsyncTask.execute();
							break;
						default :
							break;
					}
				};
			};
		}
	}

	public synchronized static DownloadManager getInstance(Context context) {
		if (staticSelf == null) {
			staticSelf = new DownloadManager(context);
			//添加默认的管理监听器,用于统计
			staticSelf.addDownloadManagerListener(new DefaultDownloadManagerListener());
		}
		return staticSelf;
	}

	/**
	 * 添加监听器的方法
	 * 
	 * @param downloadManagerListener
	 */
	public long addDownloadManagerListener(IAidlDownloadManagerListener downloadManagerListener) {
		long id = -1;
		if (mDownloadManagerListener != null
				&& !mDownloadManagerListener.contains(downloadManagerListener)) {
			//TODO:下载wangzhuobin 直接用时间做ID，有可能会重复
			id = System.currentTimeMillis();
			mDownloadManagerListener.put(id, downloadManagerListener);
		}
		return id;
	}

	/**
	 * 移除监听器的方法
	 * 
	 * @param downloadManagerListener
	 */
	public void removeDownloadManagerListener(long id) {
		if (mDownloadManagerListener != null && mDownloadManagerListener.get(id) != null) {
			mDownloadManagerListener.remove(id);
		}
	}

	//---------------------------------------------添加下载任务----------------------------------------//
	/**
	 * <br>
	 * 功能简述:aidl的接口方法实现 <br>
	 * 功能详细描述: 往下载管理添加一个下载任务但并不进行下载，返回taskId<br>
	 * 注意:
	 * 
	 * @param task
	 * @return 返回taskId
	 *         taskId为-1，表示下载任务没有加载成功
	 */
	public long addDownloadTask(DownloadTask task) {
		long id = task.getId();
		String downloadUrl = task.getDownloadUrl();
		String downloadName = task.getDownloadName();
		String saveFilePath = task.getSaveFilePath();
		// 判断必要参数是否符合规范
		if (downloadUrl != null && !"".equals(downloadUrl.trim()) && downloadName != null
				&& !"".equals(downloadName.trim()) && saveFilePath != null
				&& !"".equals(saveFilePath.trim())) {
			if (mDownloadConcurrentHashMap != null) {
				boolean flag = false;
				for (DownloadTask dt : mDownloadConcurrentHashMap.values()) {
					if (dt.getId() == id || dt.getDownloadUrl().equals(task.getDownloadUrl())) {
						flag = true;
						id = dt.getId();
						break;
					}
				}
				if (!flag) {
					//增加任务进入下载队列的时间
					if (task.getConstructTime() == 0) {
						task.setConstructTime(System.currentTimeMillis());
					}
					mDownloadConcurrentHashMap.put(id, task);
				}
				return id;
			} else {
				return -1;
			}
		}
		return -1;
	}

	//---------------------------------------------开始下载任务----------------------------------------//
	/**
	 * <br>
	 * 功能简述:aidl的接口方法实现 <br>
	 * 功能详细描述:通过下载任务的ID找到下载任务进行下载 <br>
	 * 注意:
	 * 
	 * @param taskId
	 */
	public void startDownload(long taskId) {
		if (mDownloadConcurrentHashMap != null && mDownloadConcurrentHashMap.containsKey(taskId)) {
			DownloadTask task = mDownloadConcurrentHashMap.get(taskId);
			// 对mDownloadingTasksId和mWaitingTasksIdList进行判断，避免重复下载同一个应用(比如桌面直接下载推荐APP)
			if (task != null && mDownloadingTasksId != null && !mDownloadingTasksId.contains(task.getId())
					&& mWaitingTasksIdList != null && !mWaitingTasksIdList.contains(task.getId())) {
				if (mDownloadingTasksId.size() < mMaxConcurrentDownloadCount) {
					// 记录下载任务ID
					mDownloadingTasksId.add(taskId);
					// 使用handler，因为下载的AsyncTask必须在UI线程创建
					if (mHandler == null) {
						task.setState(DownloadTask.STATE_FAIL);
						task.notifyListener(IDownloadListenerConstance.METHOD_ON_DOWNLOAD_FAIL_ID);
						// 加入到网络重连列表
						mFailList.add(task.getId());
						return;
					}
					mHandler.obtainMessage(MSG_START_DOWNLOAD, task).sendToTarget();
				} else {
					// 如果已经到了最大并发任务数，就添加到等待队列中
					task.setState(DownloadTask.STATE_WAIT);
					// 任务状态改变之后，保存下载任务的信息
					saveNotCompleteTask();
					mWaitingTasksIdList.add(taskId);
				}
				// DownloadTask的回调下载监听
				task.notifyListener(IDownloadListenerConstance.METHOD_ON_WAIT_DOWNLOAD_ID);
				// 通知DownloadManager的监听器
				notifyListener(IDownloadManagerListener.METHOD_ON_START_DOWNLOADTASK_ID, task);
			}
		}
	}

	//---------------------------------------------停止下载任务----------------------------------------//
	/**
	 * 停止下载的方法
	 * 
	 * @param taskId
	 */
	public void stopDownloadById(long taskId) {
		if (mDownloadConcurrentHashMap != null) {
			DownloadTask downloadTask = mDownloadConcurrentHashMap.get(taskId);
			if (downloadTask != null) {
				downloadTask.setState(DownloadTask.STATE_STOP);
				//TODO:下载wangzhuobin 是否应该在真正停止的时候去保存，以保持信息的准确
				// 任务状态改变之后，保存下载任务的信息
				saveNotCompleteTask();
				//任务不再处于wait状态的时候，需要从mWaitingTasksIdList中删除该任务ID
				//等到下载任务真正停止时，将会从mDownloadingTasksId中删除
				if (mWaitingTasksIdList != null
						&& mWaitingTasksIdList.contains(downloadTask.getId())) {
					mWaitingTasksIdList.remove(downloadTask.getId());
				}
			}
		}
	}

	/**
	 * 功能简述:
	 * 功能详细描述:
	 * 注意:
	 * @return 返回正在下载的被暂停的任务个数
	 */
	public int stopAllDownloadTask() {
		int count = 0;
		if (mDownloadConcurrentHashMap != null) {
			for (DownloadTask downloadTask : mDownloadConcurrentHashMap.values()) {
				if (downloadTask != null) {
					if (downloadTask.getState() == DownloadTask.STATE_DOWNLOADING) {
						count++;
					}
					downloadTask.setState(DownloadTask.STATE_STOP);
					//任务不再处于wait状态的时候，需要从mWaitingTasksIdList中删除该任务ID
					if (mWaitingTasksIdList != null
							&& mWaitingTasksIdList.contains(downloadTask.getId())) {
						mWaitingTasksIdList.remove(downloadTask.getId());
					}
				}
			}
			//任务状态改变之后，保存下载任务的信息
			saveNotCompleteTask();
		}
		return count;
	}

	//------------------------------------------重新开始下载任务-----------------------------------------//
	/**
	 * 重新开始下载的方法
	 * 
	 * @param context
	 * @param taskId
	 */
	public void restartDownload(long taskId) {
		if (mDownloadConcurrentHashMap != null) {
			DownloadTask downloadTask = mDownloadConcurrentHashMap.get(taskId);
			if (downloadTask != null) {
				downloadTask.setState(DownloadTask.STATE_RESTART);
				// 任务状态改变之后，保存下载任务的信息
				saveNotCompleteTask();
				// 通知DownloadManager的监听器
				notifyListener(IDownloadManagerListener.METHOD_ON_RESTART_DOWNLOADTASK_ID,
						downloadTask);
				startDownload(downloadTask.getId());
			}
		}
	}

	//-----------------------------------------删除下载任务--------------------------------------------//
	/**
	 * 从正在下载任务的ID集合中删除某个ID的方法
	 * 
	 * @param taskId
	 */
	public void removeTaskIdFromDownloading(Long taskId, boolean isStartWaitingTask) {
		//首先mDownloadingTasksIdArrayList与 mDownloadConcurrentHashMap的一致性
		//可能会出现id在mDownloadingTasksIdArrayList有，而在mDownloadConcurrentHashMap没有的情况
		if (mDownloadingTasksId != null) {
//			Iterator<Long> iter = mDownloadingTasksId.iterator();
//			while (iter.hasNext()) {
//				Long id = iter.next();
//				if (!mDownloadConcurrentHashMap.containsKey(id)) {
//					iter.remove();
//					if (mWaitingTasksIdList != null && mWaitingTasksIdList.contains(id)) {
//						mWaitingTasksIdList.remove(id);
//					}
//				}
//			}
			ArrayList<Long> deleteIds = new ArrayList<Long>();
			for (int i = 0; i < mDownloadingTasksId.size(); i++) {
				Long id = mDownloadingTasksId.get(i);
				if (!mDownloadConcurrentHashMap.containsKey(id)) {
					deleteIds.add(id);
				}
			}
			for (int i = 0; i < deleteIds.size(); i++) {
				Long id = deleteIds.get(i);
				mDownloadingTasksId.remove(id);
				if (mWaitingTasksIdList != null && mWaitingTasksIdList.contains(id)) {
					mWaitingTasksIdList.remove(id);
				}
			}
			if (mDownloadingTasksId.contains(taskId)) {
				mDownloadingTasksId.remove(taskId);
			}
			if (isStartWaitingTask && mDownloadingTasksId.size() < mMaxConcurrentDownloadCount) {
				startWaitingTask();
			}
		}
	}

	/**
	 * <br>功能简述:从集合中移除下载任务的方法 批量移除一组下载任务，在移除最后一个任务时，检查等待队列是否还有任务需要启动
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param taskId
	 */
	public void removeDownloadTaskById(ArrayList<Long> taskIds) {
		if (taskIds != null && !taskIds.isEmpty()) {
			ArrayList<Long> downloadingIds = new ArrayList<Long>();
			ArrayList<Long> waitingIds = new ArrayList<Long>();
			for (Long id : taskIds) {
				if (mDownloadConcurrentHashMap.get(id).getState() == DownloadTask.STATE_WAIT) {
					waitingIds.add(id);
				} else {
					downloadingIds.add(id);
				}
			}
			for (int i = 0; i < waitingIds.size(); i++) {
				mDownloadConcurrentHashMap.get(waitingIds.get(i)).setState(DownloadTask.STATE_DELETE);
				removeDownloadTaskById(waitingIds.get(i), false);
			}
			boolean isStartWaitingTask = false;
			for (int i = 0; i < downloadingIds.size(); i++) {
				if (i == downloadingIds.size() - 1) {
					isStartWaitingTask = true;
				}
				mDownloadConcurrentHashMap.get(downloadingIds.get(i)).setState(DownloadTask.STATE_DELETE);
				removeDownloadTaskById(downloadingIds.get(i), isStartWaitingTask);
			}
		}
	}

	/**
	 * <br>功能简述:删除下载任务方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param taskId 被删除的任务的ID
	 * @param isStartWaitingTask 是否自动下载等待任务
	 * @return
	 */
	public DownloadTask removeDownloadTaskById(long taskId, boolean isStartWaitingTask) {
		DownloadTask downloadTask = null;
		// 把ID从两个集合中移除
		if (mDownloadingTasksId != null && mDownloadingTasksId.contains(taskId)) {
			mDownloadingTasksId.remove(taskId);
		}
		if (mWaitingTasksIdList != null && mWaitingTasksIdList.contains(taskId)) {
			mWaitingTasksIdList.remove(taskId);
		}
		if (mDownloadConcurrentHashMap != null) {
			// 从任务集合中移除
			downloadTask = mDownloadConcurrentHashMap.remove(taskId);
			if (downloadTask != null) {
				if (downloadTask.getState() == DownloadTask.STATE_FINISH
						&& mCompleteManager != null) {
					if (downloadTask.getIsApkFile()
							&& (downloadTask.getDownloadApkPkgName() == null || downloadTask
									.getDownloadApkPkgName().equals(""))) {
						// 桌面推荐的应用是没有包名的，所以在下载完成之后，需要从下载完的apk包解析出包名
						PackageManager pm = mContext.getPackageManager();
						if (pm != null) {
							PackageInfo info = pm.getPackageArchiveInfo(
									downloadTask.getSaveFilePath(), PackageManager.GET_ACTIVITIES);
							if (info != null && info.packageName != null) {
								downloadTask.setDownloadApkPkgName(info.packageName);
							}
						}
					}
					mCompleteManager.setCompeleteDownloadInfo(downloadTask.getId(),
							downloadTask.getDownloadApkPkgName());
					//加入到“已下载”列表
					mCompleteManager.addDownloadCompleteTask(downloadTask);
					// 如果已经下载了一个安装的程序包，从安装的列表中移去这个程序包名
					// addDownloadCompleteTask会根据实际apk包修改包名，所以放于addDownloadCompleteTask后面执行
					mCompleteManager.removeInstalledTask(downloadTask.getDownloadApkPkgName());
				} else {
					if (downloadTask.getState() != DownloadTask.STATE_START
							&& downloadTask.getState() != DownloadTask.STATE_DOWNLOADING) {
						downloadTask.setState(DownloadTask.STATE_DELETE);
						downloadTask
								.notifyListener(IDownloadListenerConstance.METHOD_ON_DOWNLOAD_DELETE_ID);
						downloadTask.destory();
					} else {
						downloadTask.setState(DownloadTask.STATE_DELETE);
					}
				}
				// 任务状态改变之后，保存下载任务的信息
				saveNotCompleteTask();
				// 通知DownloadManager的监听器
				notifyListener(IDownloadManagerListener.METHOD_ON_REMOVE_DOWNLOADTASK_ID,
						downloadTask);
			}
		}
		if (isStartWaitingTask && mDownloadingTasksId != null
				&& mDownloadingTasksId.size() < mMaxConcurrentDownloadCount) {
			startWaitingTask();
		}
		return downloadTask;
	}

	//-----------------------------------------下载等待任务--------------------------------------------//
	/**
	 * <br>功能简述:启动下载一个处于等待的任务
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void startWaitingTask() {
		if (mWaitingTasksIdList != null && mWaitingTasksIdList.size() > 0) {
			Long waitId = null;
			for (int i = 0; i < mWaitingTasksIdList.size(); i++) {
				Long id = mWaitingTasksIdList.get(i);
				if (id != null && getDownloadTaskById(id) != null) {
					if (getDownloadTaskById(id).getState() == DownloadTask.STATE_WAIT) {
						waitId = mWaitingTasksIdList.remove(i);
						break;
					}
				}
			}
			if (waitId != null) {
				startDownload(waitId);
			}
		}
	}

	/**
	 * 通知监听器的方法
	 * 
	 * @param methodId
	 */
	public void notifyListener(int methodId, DownloadTask downloadTask) {
		if (mDownloadManagerListener != null) {
			Iterator<IAidlDownloadManagerListener> it = mDownloadManagerListener.values()
					.iterator();
			while (it.hasNext()) {
				IAidlDownloadManagerListener listener = it.next();
				try {
					switch (methodId) {
						case IDownloadManagerListener.METHOD_ON_START_DOWNLOADTASK_ID : {
							listener.onStartDownloadTask(downloadTask);
						}
							break;
						case IDownloadManagerListener.METHOD_ON_REMOVE_DOWNLOADTASK_ID : {
							listener.onRemoveDownloadTask(downloadTask);
						}
							break;
						case IDownloadManagerListener.METHOD_ON_RESTART_DOWNLOADTASK_ID : {
							listener.onRestartDownloadTask(downloadTask);
						}
							break;
						case IDownloadManagerListener.METHOD_ON_FAIL_DOWNLOADTASK_ID : {
							listener.onFailDownloadTask(downloadTask);
						}
							break;
						default :
							break;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//------------------------------------------------get,set------------------------------------------//
	
	public List<Long> getFailTaskIdList() {
		return mFailList;
	}
	/**
	 * @return the mMaxConcurrentDownloadCount
	 */
	public int getMaxConcurrentDownloadCount() {
		return mMaxConcurrentDownloadCount;
	}

	/**
	 * @param mMaxConcurrentDownloadCount
	 *            the mMaxConcurrentDownloadCount to set
	 */
	public void setMaxConcurrentDownloadCount(int maxConcurrentDownloadCount) {
		this.mMaxConcurrentDownloadCount = maxConcurrentDownloadCount;
	}

	/**
	 * 通过ID获取下载任务的方法
	 * 
	 * @return
	 */
	public DownloadTask getDownloadTaskById(long taskId) {
		DownloadTask downloadTask = null;
		if (mDownloadConcurrentHashMap != null) {
			downloadTask = mDownloadConcurrentHashMap.get(taskId);
		}
		return downloadTask;
	}

	/**
	 * 获取下载任务集合的方法
	 * 
	 * @return the mDownloadConcurrentHashMap
	 */
	public ConcurrentHashMap<Long, DownloadTask> getDownloadConcurrentHashMap() {
		return mDownloadConcurrentHashMap;
	}

	/**
	 * 获取已经按照时间顺序排列好的下载任务列表
	 */
	public ArrayList<DownloadTask> getDownloadTaskSortByTime() {
		ArrayList<DownloadTask> taskList = new ArrayList<DownloadTask>();
		for (DownloadTask task : mDownloadConcurrentHashMap.values()) {
			taskList.add(task);
		}
		Comparator<DownloadTask> comparator = new Comparator<DownloadTask>() {
			@Override
			public int compare(DownloadTask object1, DownloadTask object2) {
				return (int) (object1.getConstructTime() - object2.getConstructTime());
			}
		};
		Collections.sort(taskList, comparator);
		return taskList;
	}

	public DownloadCompleteManager getDownloadCompleteManager() {
		return mCompleteManager;
	}

	//TODO:下载wangzhuobin 外部数据读写，考虑建一个下载数据保存的辅助工具类
	//------------------------------未完成下载的任务数据保存与读取------------------------//
	// 下载服务退出的时候保存未下载完成的任务到SD卡
	public void saveNotCompleteTask() {
		JSONArray notCompleteArray = new JSONArray();
		ArrayList<DownloadTask> list = getDownloadTaskSortByTime();
		for (DownloadTask task : list) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", task.getId());
				obj.put("iconType", task.getIconType());
				obj.put("iconInfo", task.getIconInfo());
				obj.put("name", task.getDownloadName());
				obj.put("package", task.getDownloadApkPkgName());
				obj.put("path", task.getSaveFilePath());
				obj.put("totalSize", task.getTotalSize());
				obj.put("alreadyDownloadSize", task.getAlreadyDownloadSize());
				obj.put("alreadyDownloadPercent", task.getAlreadyDownloadPercent());
				obj.put("module", task.getModule());
				obj.put("state", task.getState());
				obj.put("downloadUrl", task.getDownloadUrl());
//				obj.put("treatment", task.getTreatment());
				obj.put("threadInfo", task.getThreadDataAsJson());
				obj.put("constructTime", task.getConstructTime());
				notCompleteArray.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		list.clear();
		FileUtil.saveByteToSDFile(notCompleteArray.toString().getBytes(),
				LauncherEnv.Path.DOWNLOAD_NOT_COMPLETE_PATH);
	}

	// 下载服务启动的时候，从SD卡读取未下载完成的任务
	public void getNotDownloadCompleteTask() {
		String info = RecommAppFileUtil
				.readFileToString(LauncherEnv.Path.DOWNLOAD_NOT_COMPLETE_PATH);
		int maxConcurrentDownloadCount = getMaxConcurrentDownloadCount();
		ArrayList<Long> continueList = new ArrayList<Long>();
		try {
			if (info == null) {
				return;
			}
			JSONArray jsonArray = new JSONArray(info);
			//先把并发个数设置为0，方便批量添加下载任务
			setMaxConcurrentDownloadCount(0);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				long taskId = obj.optLong("id", System.currentTimeMillis());
				int iconType = obj.optInt("iconType");
				String iconInfo = obj.optString("iconInfo");
				String name = obj.optString("name");
				String packageName = obj.optString("package");
				String path = obj.optString("path");
				long totalSize = obj.optLong("totalSize");
				long alreadyDownloadSize = obj.optLong("alreadyDownloadSize");
				int alreadyDownloadPercent = obj.optInt("alreadyDownloadPercent");
				int state = obj.optInt("state");
				int module = obj.optInt("module");
				String downloadUrl = obj.optString("downloadUrl");
				int treatment = obj.optInt("treatment", 0);
				// 获得APP下载的分段线程的信息
				JSONArray jarray = obj.optJSONArray("threadInfo");
				long constructTime = obj.optLong("constructTime", System.currentTimeMillis());
				DownloadTask task = new DownloadTask(taskId, downloadUrl, name, path, packageName,
						iconType, iconInfo, module);
				task.setAlreadyDownloadPercent(alreadyDownloadPercent);
				task.setAlreadyDownloadSize(alreadyDownloadSize);
				task.setTotalSize(totalSize);
//				task.setTreatment(treatment);
				task.setConstructTime(constructTime);
				if (jarray != null) {
					task.setThreadData(jarray);
				}
				task.addDownloadListener(new AppDownloadListener(GOLauncherApp.getContext()));
				addDownloadTask(task);
				//添加任务之后，再设置任务为停止状态
				//否则设置状态将不会产生效果
				PreferencesManager sp = new PreferencesManager(mContext, IPreferencesIds.DOWNLOAD_MANAGER_TASK_STATE,
						Context.MODE_PRIVATE);
				String ts = sp.getString(String.valueOf(taskId), String.valueOf(DownloadTask.TASK_STATE_EXCEPTION));
				if (ts != null && ts.equals(String.valueOf(DownloadTask.TASK_STATE_NORMAL))) {
					task.setState(DownloadTask.STATE_STOP);
				} else {
					continueList.add(taskId);
					task.setState(DownloadTask.STATE_DOWNLOADING);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			//最后把并发个数设置为原来的值
			setMaxConcurrentDownloadCount(maxConcurrentDownloadCount);
			for (long taskId : continueList) {
				restartDownload(taskId);
			}
		}
	}

	// ----------------------------------------保存未下载完成，下载完成未安装 ，下载完成且安装 的下载任务 ------------------------//
	public void saveAllTaskInfo() {
		saveNotCompleteTask();
		mCompleteManager.saveDownlaodCompleteTask();
		mCompleteManager.saveInstalledTask();
	}

	public void getAllTaskInfo() {
		getNotDownloadCompleteTask();
		mCompleteManager.getDownloadCompleteTask();
		mCompleteManager.getInstalledTaskFromSD();
	}
}
