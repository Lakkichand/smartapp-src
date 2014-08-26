package com.zhidian.wifibox.util;

import java.util.Iterator;
import java.util.Map;

import com.zhidian.wifibox.download.DownloadTask;

public class GetTaskStateUtil {
	

/**
 * 获取任务列表中未开始任务数
 * @param mAllTask   任务列表
 * @return  int     未开始任务数
 */
public static int getNotStartTaskCount(Map<String, DownloadTask> mAllTask){

	Iterator<DownloadTask> iterator = mAllTask.values().iterator();
	int notStartCount = 0;
	while (iterator.hasNext()) {
		DownloadTask task = iterator.next();
		switch (task.state) {
		case DownloadTask.NOT_START:
			notStartCount ++;
			break;
		default:
			break;
		}
	}
	return notStartCount;
}


/**
 * 获取任务列表中下载中任务数
 * @param mAllTask   任务列表
 * @return  int     下载中任务数
 */
public static int getDownloadingTaskCount(Map<String, DownloadTask> mAllTask){

	Iterator<DownloadTask> iterator = mAllTask.values().iterator();
	int downloadingCount = 0;
	while (iterator.hasNext()) {
		DownloadTask task = iterator.next();
		switch (task.state) {
		case DownloadTask.DOWNLOADING:
		case DownloadTask.WAITING:
			downloadingCount++;
			break;
		default:
			break;
		}
	}
	return downloadingCount;
}

/**
 * 获取任务列表中暂停任务数
 * @param mAllTask   任务列表
 * @return  int     暂停任务数
 */
public static int getPausingTaskCount(Map<String, DownloadTask> mAllTask){

	Iterator<DownloadTask> iterator = mAllTask.values().iterator();
	int pauseCount = 0;
	while (iterator.hasNext()) {
		DownloadTask task = iterator.next();
		switch (task.state) {
		case DownloadTask.PAUSING:
			pauseCount ++;
			break;
		

		default:
			break;
		}
	}
	return pauseCount;
}

/**
 * 获取任务列表中已下载任务数
 * @param mAllTask   任务列表
 * @return  int     已下载任务数
 */
public static int getCompleteTaskCount(Map<String, DownloadTask> mAllTask){

	Iterator<DownloadTask> iterator = mAllTask.values().iterator();
	int completeCount = 0;
	while (iterator.hasNext()) {
		DownloadTask task = iterator.next();
		switch (task.state) {
		case DownloadTask.COMPLETE:
			completeCount ++;
			break;

		default:
			break;
		}
	}
	return completeCount;
}

/**
 * 获取任务列表中下载中任务
 * @param mAllTask   任务列表
 * @return  DownloadTask     下载中任务
 */
public static DownloadTask getDownloadingTask(Map<String, DownloadTask> mAllTask){

	Iterator<DownloadTask> iterator = mAllTask.values().iterator();
	
	DownloadTask downloadTask = null;
	while (iterator.hasNext()) {
		DownloadTask task = iterator.next();
		switch (task.state) {
		case DownloadTask.DOWNLOADING:
		case DownloadTask.WAITING:
			downloadTask = task;
			break;
		default:
			break;
		}
	}
	return downloadTask;
}

/**
 * 获取任务列表中暂停下载任务
 * @param mAllTask   任务列表
 * @return  DownloadTask     暂停下载任务
 */
public static DownloadTask getPauseTask(Map<String, DownloadTask> mAllTask){

	Iterator<DownloadTask> iterator = mAllTask.values().iterator();
	
	int downloadingCount = 0;
	DownloadTask downloadTask = null;
	while (iterator.hasNext()) {
		DownloadTask task = iterator.next();
		switch (task.state) {
		case DownloadTask.PAUSING:
			downloadTask = task;
			break;
		
		default:
			break;
		}
	}
	return downloadTask;
}
}
