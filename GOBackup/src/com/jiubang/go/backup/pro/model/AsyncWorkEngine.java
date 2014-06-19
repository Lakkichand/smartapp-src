package com.jiubang.go.backup.pro.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;

/**
 * 抽象异步工作引擎
 *
 * @author maiyongshen
 */
public abstract class AsyncWorkEngine {
	private static final int MSG_START_WORK = 0x1001;
	private static final int MSG_CONTINUE_TO_WORK = 0x1002;
	private static final int MSG_END_WORK = 0x1003;
	private static final int MSG_PAUSE = 0x1004;
	private String mName;
	private WorkerHandler mWorkerHandler;
	private boolean mForceStopFlag;
	private WorkState mCurrentWorkState;

	/**
	 * @author maiyongshen
	 */
	private class WorkerHandler extends Handler {
		boolean mIsPaused = false;

		public WorkerHandler(Looper looper) {
			super(looper);
		}

		public boolean isPaused() {
			return mIsPaused;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_START_WORK :
					onWorkStart();
					sendMessageAtFrontOfQueue(Message.obtain(this, MSG_CONTINUE_TO_WORK));
					break;
				case MSG_CONTINUE_TO_WORK :
					// 当前任务未完成，不允许执行下一个
					if (mCurrentWorkState == WorkState.WORKING) {
						return;
					}
					// 当前任务被暂停，重做当前任务
					if (mCurrentWorkState == WorkState.PAUSED) {
						continueUnfinishedWork();
						return;
					}
					if (!shouldStop()) {
						resetWorkState();
						doNextWork();
					} else {
						stopWork();
					}
					break;
				case MSG_END_WORK :
					// 最后一个任务仍未完成时，需继续等待
					while (mCurrentWorkState == WorkState.WORKING) {
						synchronized (this) {
							final int waitTime = 1000;
							waitForMoment(waitTime);
						}
					}
					onWorkFinish(!mForceStopFlag);
					break;
				case MSG_PAUSE :
					pause();
					break;
				default :
					break;
			}
		}

		private void waitForMoment(long millis) {
			try {
				wait(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void pause() {
			synchronized (this) {
				mIsPaused = true;
				if (mCurrentWorkState == WorkState.WORKING) {
					setCurrentWorkState(WorkState.PAUSED);
				}
				waitForMoment(0);
			}
		}

		public void resume() {
			synchronized (this) {
				// 如果暂停消息仍未被线程执行，则移除它
				if (hasMessages(MSG_PAUSE)) {
					removeMessages(MSG_PAUSE);
					return;
				}
				notify();
				mIsPaused = false;
			}
		}
	}

	public AsyncWorkEngine(String name) {
		mName = name;
		mForceStopFlag = false;
	}

	public synchronized void startRun() {
		if (mWorkerHandler != null) {
			return;
		}
		HandlerThread thread = new HandlerThread(mName);
		thread.start();
		mWorkerHandler = new WorkerHandler(thread.getLooper());
		mWorkerHandler.sendEmptyMessage(MSG_START_WORK);
	}

	public synchronized void forceToStop() {
		mForceStopFlag = true;
		if (isPaused()) {
			resume();
		}
	}

	public synchronized void pause() {
		if (mWorkerHandler != null && !mWorkerHandler.hasMessages(MSG_PAUSE)) {
			mWorkerHandler.sendEmptyMessage(MSG_PAUSE);
		}
	}

	public synchronized boolean isForceStopped() {
		return mForceStopFlag;
	}

	public synchronized void resume() {
		if (mWorkerHandler != null) {
			mWorkerHandler.resume();
		}
		continueToWork();
	}

	public synchronized boolean isPaused() {
		if (mWorkerHandler == null) {
			return false;
		}
		return mWorkerHandler.isPaused() || mWorkerHandler.hasMessages(MSG_PAUSE);
	}

	public String getNotificationMessage() {
		return null;
	}

	protected void continueToWork() {
		if (mWorkerHandler == null) {
			return;
		}
		if (mWorkerHandler.hasMessages(MSG_CONTINUE_TO_WORK)) {
			return;
		}
		mWorkerHandler.sendEmptyMessage(MSG_CONTINUE_TO_WORK);
	}

	protected void stopWork() {
		if (mWorkerHandler != null) {
			mWorkerHandler.sendEmptyMessage(MSG_END_WORK);
		}
	}

	protected void onWorkStart() {

	}

	protected void onWorkFinish(boolean finishNormal) {
		mWorkerHandler.getLooper().quit();
		mWorkerHandler = null;
	}

	private void resetWorkState() {
		setCurrentWorkState(WorkState.NOT_START);
	}

	/**
	 * 子类必须正确设置工作状态
	 *
	 * @param state
	 */
	protected void setCurrentWorkState(WorkState state) {
		if (state == null) {
			throw new IllegalArgumentException("invalid argument!");
		}
		mCurrentWorkState = state;
	}

	protected boolean isCurrentWorkFinished() {
		return mCurrentWorkState == WorkState.COMPLETED || mCurrentWorkState == WorkState.FAILED;
	}

	protected boolean isCurrentWorkStarted() {
		return mCurrentWorkState != WorkState.NOT_START;
	}

	protected synchronized boolean shouldStop() {
		return mForceStopFlag || !hasNextWork();
	}

	/**
	 * 继续完成上一次未完成的操作
	 */
	protected void continueUnfinishedWork() {
		setCurrentWorkState(WorkState.WORKING);
	}

	/**
	 * 执行任务实际操作 必须重载此方法
	 */
	protected abstract void doNextWork();

	protected abstract boolean hasNextWork();

	/**
	 * @author maiyongshen
	 */
	public static class WorkDetailBean {
		public int workId;
		// public String workObject;
		// public String workState;
		public String title;
		public String workProgress;
		public EntryType workObjectType;
	}

	/**
	 * @author maiyongshen
	 */
	public enum WorkState {
		NOT_START, WORKING, COMPLETED, FAILED, PAUSED,
	}
}
