package com.jiubang.ggheart.appgame.download;

import java.util.List;

import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IAidlDownloadManagerListener;

interface IDownloadService {
            
    long addDownloadTask(in DownloadTask task);
    
    void startDownload(long taskId);
	
	void stopDownloadById(long taskId);
	
	void restartDownloadById(long taskId);
	
	Map getDownloadConcurrentHashMap();
	
	void removeDownloadTaskById(long taskId);
	
	void removeDownloadTasksById(in List list);
	
	DownloadTask getDownloadTaskById(long taskId);
	
	void removeTaskIdFromDownloading(long taskId);
	
	void removeListener(long id);
	
	long addDownloadTaskListener(long taskId, IAidlDownloadListener listener);
	
	long addDownloadTaskListenerByName(long taskId, String name);
	
	void removeDownloadTaskListener(long taskId, long listenerId);
	
	void removAllDownloadTaskListeners(long taskId);
	
	List getCompleteIdsByPkgName(String packageName);
	
	List getDownloadCompleteList();
	
	void removeDownloadCompleteItem(long taskId);
	
	List getDownloadingTaskSortByTime();
	
	void addInstalledPackage(String packageName);
	
	List getInstalledTaskList();
	
	void addRunningActivityClassName(String className);
}