/*
 * 文 件 名:  IDownloadManagerListener.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-16
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-8-16]
 */
public interface IDownloadManagerListener {
	public final static int METHOD_ON_START_DOWNLOADTASK_ID = 1;
	public final static int METHOD_ON_REMOVE_DOWNLOADTASK_ID = 2;
	public final static int METHOD_ON_RESTART_DOWNLOADTASK_ID = 3;
	public final static int METHOD_ON_FAIL_DOWNLOADTASK_ID = 4;
	/**
	 * DownloadManager尝试开始下载任务 但是downloadTask不一定立刻开始，有可能被放到等待队列里面
	 * 可以通过downloadTask的state来判断
	 * 
	 * @param downloadTask
	 */
	public void onStartDownloadTask(DownloadTask downloadTask);

	/**
	 * DownloadManager移除下载任务 有可能是下载成功后的移除，有可能是取消的移除 可以通过downloadTask的state来判断
	 * 
	 * @param downloadTask
	 */
	public void onRemoveDownloadTask(DownloadTask downloadTask);

	/**
	 * DownloadManager尝试重新开始下载任务
	 * 
	 * @param downloadTask
	 */
	public void onRestartDownloadTask(DownloadTask downloadTask);
}
