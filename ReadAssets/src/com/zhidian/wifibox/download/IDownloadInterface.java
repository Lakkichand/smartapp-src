package com.zhidian.wifibox.download;

import java.util.Map;

/**
 * 普通模式和极速模式下下载管理器的公共接口
 * 
 * @author xiedezhi
 * 
 */
public interface IDownloadInterface {
	/**
	 * 下载请求action（Activity向Service发送）
	 */
	public static final String DOWNLOAD_REQUEST_ACTION = "DOWNLOAD_REQUEST_ACTION_VERIFICATION";
	/**
	 * 下载请求命令，添加下载任务
	 */
	public static final String REQUEST_COMMAND_ADD = "REQUEST_COMMAND_ADD_VERIFICATION";
	/**
	 * 下载请求命令，暂停下载任务
	 */
	public static final String REQUEST_COMMAND_PAUSE = "REQUEST_COMMAND_PAUSE_VERIFICATION";
	/**
	 * 下载请求命令，继续下载任务
	 */
	public static final String REQUEST_COMMAND_CONTINUE = "REQUEST_COMMAND_CONTINUE_VERIFICATION";
	/**
	 * 下载请求命令，删除下载任务
	 */
	public static final String REQUEST_COMMAND_DELETE = "REQUEST_COMMAND_DELETE_VERIFICATION";
	/**
	 * 下载请求命令，重新下载
	 */
	public static final String REQUEST_COMMAND_REDOWNLOAD = "REQUEST_COMMAND_REDOWNLOAD_VERIFICATION";
	/**
	 * 下载请求命令，检查下载任务
	 */
	public static final String REQUEST_COMMAND_CHECKTASK = "REQUEST_COMMAND_CHECKTASK_VERIFICATION";

	/**
	 * 下载广播Action（Service向Activity发送）
	 */
	public static final String DOWNLOAD_BROADCAST_ACTION = "DOWNLOAD_BROADCAST_ACTION_VERIFICATION";

	/**
	 * 暂停所有下载任务
	 */
	public static final String PAUSE_ALL_TASK_ACTION = "PAUSE_ALL_TASK_ACTION_VERIFICATION";

	/**
	 * 暂停所有下载任务完成
	 */
	public static final String PAUSE_ALL_TASK_ACTION_COMPLETE = "PAUSE_ALL_TASK_ACTION_COMPLETE_VERIFICATION";

	/**
	 * 从本地文件读取已保存的下载任务
	 */
	public void initSavedTask();

	/**
	 * 开始处于DOWNLOADING状态的任务，暂停处于PAUSING状态的任务
	 */
	public void checkDownloadingTask();

	/**
	 * 添加下载任务
	 */
	public void addTask(String url, String boxNum, String code,
			int versionCode, int rank, String config);

	/**
	 * 暂停下载任务
	 */
	public void pauseTask(String url, boolean showNotifycation);

	/**
	 * 继续下载
	 */
	public void continueTask(String url);

	/**
	 * 删除下载任务
	 */
	public void deleteTask(String url);

	/**
	 * 重新下载任务
	 */
	public void redownloadTask(String url, String boxNum, String code,
			int versionCode, int rank);

	/**
	 * 获取当前所有下载任务
	 */
	public Map<String, DownloadTask> getAllDownloadTasks();

	/**
	 * 检查正在下载的任务是否被删除了apk
	 */
	public void checkTask();

	/**
	 * 清除下载状态通知栏
	 */
	public void clearNotification();

	/**
	 * 应用安装卸载
	 */
	public void onAppAction(String packName);

}
