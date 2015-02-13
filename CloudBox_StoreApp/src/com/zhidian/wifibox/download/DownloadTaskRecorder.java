package com.zhidian.wifibox.download;

import java.util.HashMap;
import java.util.Map;

/**
 * 当前模式下所有的下载任务列表记录器，单例
 * 
 * 应用列表可以用它拿到当前最新的下载任务列表
 * 
 * @author xiedezhi
 * 
 */
public class DownloadTaskRecorder {
	/**
	 * 当前下载任务列表
	 */
	private Map<String, DownloadTask> mMap = new HashMap<String, DownloadTask>();
	/**
	 * 单实例
	 */
	private volatile static DownloadTaskRecorder sInstance = null;

	/**
	 * 获取TabDataManager实例对象
	 */
	public static DownloadTaskRecorder getInstance() {
		if (sInstance == null) {
			synchronized (DownloadTaskRecorder.class) {
				if (sInstance == null) {
					sInstance = new DownloadTaskRecorder();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 获取当前所有下载任务
	 */
	public Map<String, DownloadTask> getDownloadTaskList() {
		return mMap;
	}

	/**
	 * 记录当前所有下载任务
	 */
	public void recordDownloadTaskList(Map<String, DownloadTask> map) {
		if (map == null) {
			map = new HashMap<String, DownloadTask>();
		}
		Map<String, DownloadTask> newMap = new HashMap<String, DownloadTask>();
		newMap.putAll(map);
		mMap = newMap;
	}

	/**
	 * 初始化函数
	 */
	private DownloadTaskRecorder() {
	}

	/**
	 * 销毁单例
	 */
	public void destory() {
		mMap.clear();
	}

}
