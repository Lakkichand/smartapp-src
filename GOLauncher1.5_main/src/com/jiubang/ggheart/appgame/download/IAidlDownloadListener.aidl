package com.jiubang.ggheart.appgame.download;

import com.jiubang.ggheart.appgame.download.DownloadTask;

interface IAidlDownloadListener {

	void onStart(in DownloadTask task);
	
	void onWait(in DownloadTask task);
	
	void onUpdate(in DownloadTask task);
	
	void onComplete(in DownloadTask task);
	
	void onFail(in DownloadTask task);
	
	void onReset(in DownloadTask task);
	
	void onStop(in DownloadTask task);
	
	void onCancel(in DownloadTask task);
	
	void onDestroy(in DownloadTask task);
	
	void onConnectionSuccess(in DownloadTask task);
	
	void onException(in DownloadTask task);
	
}