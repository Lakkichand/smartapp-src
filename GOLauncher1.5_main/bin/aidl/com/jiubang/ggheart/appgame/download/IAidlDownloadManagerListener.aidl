package com.jiubang.ggheart.appgame.download;

import com.jiubang.ggheart.appgame.download.DownloadTask;

interface IAidlDownloadManagerListener {
	void onStartDownloadTask(in DownloadTask task);
	
	void onRemoveDownloadTask(in DownloadTask task);
	
	void onRestartDownloadTask(in DownloadTask task);
	
	void onFailDownloadTask(in DownloadTask task);
}