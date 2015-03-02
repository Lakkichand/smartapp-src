package com.zhidian.wifibox.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.util.download.DownLoadCallback;
import com.ta.util.download.DownloadManager;
import com.zhidian.util.CAPathConstant;
import com.zhidian.util.DownloadUtil;
import com.zhidian.util.FileUtil;
import com.zhidian.util.InfoUtil;

/**
 * 普通模式下的下载管理器
 * 
 * 任务的保存和读取由自己管理，对外只负责管理所有下载任务，添加、暂停、继续、删除下载任务
 * 
 * @author xiedezhi
 * 
 */
public class CDownloadManager implements IDownloadInterface {

	/**
	 * 正在下载
	 */
	public static final String DOWNLOADING = "正在下载";
	/**
	 * 暂停下载
	 */
	public static final String PAUSE_DOWNLOADING = "暂停下载";
	/**
	 * 这个是DownloadService实例
	 */
	private Context mContext;

	/**
	 * 所有下载任务
	 */
	private Map<String, DownloadTask> mAllTask = new HashMap<String, DownloadTask>();
	/**
	 * 普通模式下的下载管理器，CDownloadManager依赖于它
	 */
	private DownloadManager mDownloadManager;
	/**
	 * 主线程handler
	 */
	private Handler mHandler = new Handler(Looper.getMainLooper());
	/**
	 * toast
	 */
	private Toast mToast = Toast.makeText(TAApplication.getApplication(),
			"玩命下载ing...", Toast.LENGTH_SHORT);

	private Toast mNoNetworkToast = Toast.makeText(
			TAApplication.getApplication(), "网络连接失败", Toast.LENGTH_SHORT);
	/**
	 * 每个任务都有一个重试的Toast提示
	 */
	private Map<DownloadTask, Toast> mToasts = new HashMap<DownloadTask, Toast>();

	/**
	 * 下载回调
	 */
	private DownLoadCallback mCallback = new DownLoadCallback() {
		public void onStart() {
		}

		public void onAdd(String url, Boolean isInterrupt) {
			DownloadTask task = mAllTask.get(url);
			if (task != null) {
				task.state = DownloadTask.WAITING;
				saveAllTask();
				sendBroadcast(task);
			}
		}

		public void onLoading(String url, long totalSize, long currentSize,
				long speed, long time) {
			if (currentSize <= 0) {
				return;
			}
			// 发广播
			DownloadTask task = mAllTask.get(url);
			if (task != null) {
				if (task.state == DownloadTask.PAUSING) {
					pauseTask(url, true);
					return;
				}
				task.state = DownloadTask.DOWNLOADING;
				int newPercent = (int) (currentSize * 100.0 / totalSize + 0.5);
				// 下载进度的差超过2%时才保存任务和发送广播
				task.alreadyDownloadPercent = newPercent;
				saveAllTask();
				sendBroadcast(task);
			}
		}

		/**
		 * 下载完成
		 */
		public void onSuccess(String url) {
			// 发广播
			DownloadTask task = mAllTask.get(url);
			if (task != null) {
				task.state = DownloadTask.COMPLETE;
				sendBroadcast(task);
				saveAllTask();
				File file = new File(DownloadUtil.getCApkFileFromUrl(url));
				if (!FileUtil.isSDCardAvaiable()) {
					DownloadUtil.chmod(file.getParentFile().getParentFile());
					DownloadUtil.chmod(file.getParentFile());
					DownloadUtil.chmod(file);
				}
			}
		}

		public void onFailure(final String url, String strMsg) {
			if (strMsg != null && strMsg.contains("ENOSPC")) {
				// 暂停所有任务
				for (DownloadTask task : mAllTask.values()) {
					pauseTask(task.url, false);
				}
				return;
			}
			if (strMsg != null && strMsg.contains("404")) {
				// 文件不存在
				Toast.makeText(mContext, "下载失败，文件不存在", Toast.LENGTH_SHORT)
						.show();
				deleteTask(url);
				return;
			}
			if (strMsg != null && strMsg.contains("500")) {
				// 文件不存在
				Toast.makeText(mContext, "下载失败，文件不存在", Toast.LENGTH_SHORT)
						.show();
				deleteTask(url);
				return;
			}
			DownloadTask task = mAllTask.get(url);
			if (task != null
					&& (task.state == DownloadTask.DOWNLOADING || task.state == DownloadTask.WAITING)) {
				if (InfoUtil.hasWifiConnection(mContext)) {
					if (mToasts.containsKey(task)) {
						mToasts.get(task).show();
					} else {
						Toast toast = Toast.makeText(mContext, "下载失败，正重新下载",
								Toast.LENGTH_SHORT);
						toast.show();
						mToasts.put(task, toast);
					}
					postDelayed(new Runnable() {

						@Override
						public void run() {
							// 下载失败后自动重试
							mDownloadManager.addHandler(url);
						}
					}, 1000);
				} else {
					pauseTask(url, true);
					if (!InfoUtil.hasNetWorkConnection(mContext)) {
						mNoNetworkToast.show();
					}
				}
			}
		}

		public void onFinish(String url) {
		}

		public void onStop() {
		}
	};

	public CDownloadManager(Context context) {
		mContext = context;
		if (FileUtil.isSDCardAvaiable()) {
			mDownloadManager = DownloadManager
					.getDownloadManager(CAPathConstant.C_APK_ROOTPATH);
		} else {
			mDownloadManager = DownloadManager
					.getDownloadManager(CAPathConstant.C_APK_ROOTPATH_CACHE);
		}
		mDownloadManager.setDownLoadCallback(mCallback);
	}

	/**
	 * 把下载任务发送到Activity
	 */
	private void sendBroadcast(DownloadTask task) {
		Intent intent = new Intent(DOWNLOAD_BROADCAST_ACTION);
		intent.putExtra("task", task);
		mContext.sendBroadcast(intent);
	}

	@Override
	public void initSavedTask() {
		// 对于已经开始下载和下载完成的任务，要判断APK文件是否存在，如果不存在，则删除该任务
		byte[] b = FileUtil.getByteFromFile(CAPathConstant.C_TASK_PATH);
		if (b == null) {
			return;
		}
		try {
			JSONArray array = new JSONArray(new String(b));
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				if (json != null) {
					DownloadTask task = new DownloadTask();
					task.unique = json.optString("unique",
							"" + System.currentTimeMillis());
					task.state = json.getInt("state");
					task.url = json.getString("url");
					task.alreadyDownloadPercent = json
							.getInt("alreadyDownloadPercent");
					task.boxNum = json.getString("boxNum");
					task.code = json.getString("code");
					task.versionCode = json.getInt("versionCode");
					task.size = json.getInt("size");
					task.rank = json.getInt("rank");
					mAllTask.put(task.url, task);
				}
			}
			// 更新任务记录器的任务
			DownloadTaskRecorder.getInstance().recordDownloadTaskList(mAllTask);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void checkDownloadingTask() {
		for (String url : mAllTask.keySet()) {
			DownloadTask task = mAllTask.get(url);
			if (task.state == DownloadTask.DOWNLOADING) {
				mDownloadManager.addHandler(url);
			}
		}
		for (String url : mAllTask.keySet()) {
			DownloadTask task = mAllTask.get(url);
			if (task.state == DownloadTask.WAITING) {
				mDownloadManager.addHandler(url);
			}
		}
	}

	@Override
	public void addTask(String url, String boxNum, String code,
			int versionCode, int rank, String config) {
		DownloadTask task = new DownloadTask();
		task.unique = System.currentTimeMillis() + "";
		task.state = DownloadTask.WAITING;
		task.url = url;
		task.alreadyDownloadPercent = 0;
		task.boxNum = boxNum;
		task.code = code;
		task.versionCode = versionCode;
		task.rank = rank;
		task.config = config;
		mAllTask.put(url, task);
		saveAllTask();
		sendBroadcast(task);

		mDownloadManager.addHandler(url);
		// Notification
		mHandler.post(new Runnable() {
			public void run() {
				mToast.show();
			}
		});
	}

	@Override
	public void pauseTask(String url, boolean showNotifycation) {
		if (mAllTask.containsKey(url)) {
			DownloadTask task = mAllTask.get(url);
			task.state = DownloadTask.PAUSING;
			mDownloadManager.pauseHandler(url);
			saveAllTask();
			sendBroadcast(task);
			if (showNotifycation) {

			}
		}
	}

	@Override
	public void continueTask(String url) {
		if (mAllTask.containsKey(url)) {
			DownloadTask task = mAllTask.get(url);
			task.state = DownloadTask.WAITING;
			if (mDownloadManager.hasHandler(url)) {
				mDownloadManager.continueHandler(url);
			} else {
				mDownloadManager.addHandler(url);
			}
			saveAllTask();
			sendBroadcast(task);
			mHandler.post(new Runnable() {
				public void run() {
					mToast.show();
				}
			});
		}
	}

	@Override
	public void deleteTask(String url) {
		mDownloadManager.deleteHandler(url);
		// 要删除任务和文件
		if (mAllTask.containsKey(url)) {
			DownloadTask task = mAllTask.get(url);
			task.state = DownloadTask.NOT_START;
			task.alreadyDownloadPercent = 0;
			mAllTask.remove(url);
			File file = new File(DownloadUtil.getCApkFileFromUrl(task.url));
			file.delete();
			file = new File(DownloadUtil.getCTempApkFileFromUrl(task.url));
			file.delete();
			saveAllTask();
			sendBroadcast(task);
		}
	}

	@Override
	public void redownloadTask(final String url, final String boxNum,
			final String code, final int versionCode, final int rank) {
	}

	private void saveAllTask() {
		// 更新任务记录器的任务
		// DownloadTaskRecorder.getInstance().recordDownloadTaskList(mAllTask);
		// // 先把旧数据删除
		// File file = new File(CAPathConstant.C_TASK_PATH);
		// file.delete();
		// if (mAllTask.size() <= 0) {
		// return;
		// }
		// JSONArray array = new JSONArray();
		// Collection<DownloadTask> collection = mAllTask.values();
		// for (DownloadTask task : collection) {
		// try {
		// JSONObject json = new JSONObject();
		// json.put("unique", task.unique);
		// json.put("state", task.state);
		// json.put("url", task.url);
		// json.put("alreadyDownloadPercent", task.alreadyDownloadPercent);
		// json.put("boxNum", task.boxNum);
		// json.put("code", task.code);
		// json.put("versionCode", task.versionCode);
		// json.put("size", task.size);
		// json.put("rank", task.rank);
		// array.put(json);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// FileUtil.saveByteToFile(array.toString().getBytes(),
		// CAPathConstant.C_TASK_PATH);
	}

	@Override
	public Map<String, DownloadTask> getAllDownloadTasks() {
		return mAllTask;
	}

	@Override
	public void checkTask() {
		Map<String, DownloadTask> newMap = new HashMap<String, DownloadTask>();
		for (String url : mAllTask.keySet()) {
			DownloadTask task = mAllTask.get(url);
			if (task.state == DownloadTask.DOWNLOADING
					|| task.state == DownloadTask.PAUSING) {
				if (task.alreadyDownloadPercent > 0) {
					String fileName = DownloadUtil
							.getCTempApkFileFromUrl(task.url);
					File file = new File(fileName);
					if (!file.exists()) {
						mDownloadManager.deleteHandler(task.url);
						task.state = DownloadTask.NOT_START;
						task.alreadyDownloadPercent = 0;
						sendBroadcast(task);
						continue;
					}
				}
			} else if (task.state == DownloadTask.COMPLETE) {
				String fileName = DownloadUtil.getCApkFileFromUrl(task.url);
				File file = new File(fileName);
				if (!file.exists()) {
					mDownloadManager.deleteHandler(task.url);
					task.state = DownloadTask.NOT_START;
					task.alreadyDownloadPercent = 0;
					sendBroadcast(task);
					continue;
				}
			} else if (task.state == DownloadTask.FAIL) {
				File file = new File(DownloadUtil.getCApkFileFromUrl(task.url));
				file.delete();
				file = new File(DownloadUtil.getCTempApkFileFromUrl(task.url));
				file.delete();
				mDownloadManager.deleteHandler(task.url);
				task.state = DownloadTask.NOT_START;
				task.alreadyDownloadPercent = 0;
				sendBroadcast(task);
				continue;
			}
			newMap.put(url, task);
		}
		mAllTask = newMap;
		// saveAllTask();
	}

	@Override
	public void clearNotification() {

	}

	@Override
	public void onAppAction(String packName) {
		// showNotification();
	}

}
