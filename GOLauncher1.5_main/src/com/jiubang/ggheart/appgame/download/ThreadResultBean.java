/*
 * 文 件 名:  ThreadResultBean.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-26
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
 * @date  [2012-11-26]
 */
public class ThreadResultBean {
	
	public static final int DOWNLOAD_THREAD_FINISH = 1;

	public static final int DOWNLOAD_THREAD_NOT_FINISH = 2;

	public static final int DOWNLOAD_THREAD_ERROR = 3;
	
	public static final int DOWNLOAD_THREAD_RUNNING = 4;
	
	private long mStartPosition;

	private long mEndPosition;

	private int mState;

	private Exception mException;

	public void setStartPosition(long startPosition) {
		mStartPosition = startPosition;
	}

	public void setEndPosition(long endPosition) {
		mEndPosition = endPosition;
	}

	public void setState(int state) {
		mState = state;
	}

	public void setException(Exception e) {
		mException = e;
	}

	public long getStartPosition() {
		return mStartPosition;
	}

	public long getEndPosition() {
		return mEndPosition;
	}

	public int getState() {
		return mState;
	}

	public Exception getException() {
		if (mState == ThreadResultBean.DOWNLOAD_THREAD_ERROR) {
			return mException;
		} else {
			mException = null;
			return null;
		}
	}
	
	
}
