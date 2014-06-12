/*
 * 文 件 名:  DownloadResult.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-26
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-26]
 */
public class DownloadResult {

	public final static int DOWNLOAD_RUNNING = -1; // 下载运行

	public final static int DOWNLOAD_FAIL = 0; // 下载失败

	public final static int DOWNLOAD_COMPLETE = 1; // 下载完成

	public final static int DOWNLOAD_STOP = 2; // 下载停止

	public final static int DOWNLOAD_DELETE = 3; // 下载删除

	private int mResult = DOWNLOAD_RUNNING;

	private Object mDownloadSizeLocker = new Object();
	
	private long mAlreadyDownloadSize = 0;

	private long mTotalSize = 0;

	private int mPercent = 0;

	private DownloadManager mDownloadManager = null;

	private DownloadTask mDownloadTask = null;

	private ConcurrentHashMap<String, ThreadResultBean> mResultMap = null;

	private long mTime = 0;

	private final static long TIME_STEP = 10000;

	public DownloadResult(DownloadManager manager, DownloadTask task) {
		mDownloadManager = manager;
		mDownloadTask = task;
		mResultMap = new ConcurrentHashMap<String, ThreadResultBean>();
		mTime = 0;
	}

	public void notifyDownloadResult(String tag, ThreadResultBean value) {
		mResultMap.put(tag, value);
		judgeResult();
		DownloadTask.ThreadData data = new DownloadTask.ThreadData();
		data.mEndPos = value.getEndPosition();
		data.mStartPos = value.getStartPosition();
		data.mTag = tag;
		mDownloadTask.getThreadDataMap().put(tag, data);
		// 保存未下载完成，下载完成和下载完成且安装的下载任务到SD卡
		// 避免多个线程同时调用此方法造成频繁保存文件,设置一个时间差
		if (System.currentTimeMillis() - mTime > TIME_STEP) {
			mTime = System.currentTimeMillis();
			mDownloadManager.saveNotCompleteTask();
			mDownloadManager.getDownloadCompleteManager().saveDownlaodCompleteTask();
			mDownloadManager.getDownloadCompleteManager().saveInstalledTask();
		}
	}

	public int getResult() {
		return mResult;
	}

	public void setResult(int result) {
		mResult = result;
	}

	public long getAlreadyDownloadSize() {
		synchronized (mDownloadSizeLocker) {
			return mAlreadyDownloadSize;
		}
	}

	public void updateAlreadyDownloadSize(long size) {
		synchronized (mDownloadSizeLocker) {
			mAlreadyDownloadSize += size;
		}
		if (mTotalSize > 0) {
			mPercent = (int) (((double) mAlreadyDownloadSize / mTotalSize) * 100);
		}
		// 保存未下载完成的任务
		if (System.currentTimeMillis() - mTime > TIME_STEP) {
			mTime = System.currentTimeMillis();
			mDownloadManager.saveNotCompleteTask();
		}
	}

	public long getTotalSize() {
		return mTotalSize;
	}

	public void setTotalSize(long size) {
		mTotalSize = size;
	}

	public int getAlreadyDownloadPercent() {
		return mPercent;
	}

	public ConcurrentHashMap<String, ThreadResultBean> getResultMap() {
		return mResultMap;
	}
	/**
	 * <br>功能简述:分别所有线程的状态值，得到
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void judgeResult() {
		ArrayList<Integer> list = new ArrayList<Integer>();
			Iterator<Entry<String, ThreadResultBean>> iter = mResultMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, ThreadResultBean> entry = iter.next();
				ThreadResultBean bean = entry.getValue();
				list.add(bean.getState());
			}
		if (list.contains(ThreadResultBean.DOWNLOAD_THREAD_RUNNING)) {
			mResult = DOWNLOAD_RUNNING;
		} else if (list.contains(ThreadResultBean.DOWNLOAD_THREAD_ERROR)) {
			mResult = DOWNLOAD_FAIL;
		} else if (list.contains(ThreadResultBean.DOWNLOAD_THREAD_NOT_FINISH)
				&& mDownloadTask.getState() == DownloadTask.STATE_STOP) {
			mResult = DOWNLOAD_STOP;
		} else if (list.contains(ThreadResultBean.DOWNLOAD_THREAD_NOT_FINISH)
				&& mDownloadTask.getState() == DownloadTask.STATE_DELETE) {
			mResult = DOWNLOAD_DELETE;
		} else if (list.contains(ThreadResultBean.DOWNLOAD_THREAD_FINISH)) {
			mResult = DOWNLOAD_COMPLETE;
		}
	}
}
