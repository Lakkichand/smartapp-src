package com.go.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

public abstract class AsyncHandler {
	private HandlerThread mHandlerThread;
	private HandlerImpl mHandler;

	public AsyncHandler(String threadName) {
		this(threadName, Process.THREAD_PRIORITY_DEFAULT);
	}

	public AsyncHandler(String threadName, int priority) {
		mHandlerThread = new HandlerThread(threadName, priority);
		mHandlerThread.start();
		mHandler = new HandlerImpl(mHandlerThread.getLooper());
	}

	public final Message obtainMessage(int what) {
		return mHandler.obtainMessage(what);
	}

	public final Message obtainMessage(int what, Object obj) {
		return mHandler.obtainMessage(what, obj);
	}

	public final Message obtainMessage(int what, int arg1, int arg2) {
		return mHandler.obtainMessage(what, arg1, arg2);
	}

	public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
		return mHandler.obtainMessage(what, arg1, arg2, obj);
	}

	public boolean sendEmptyMessage(int what) {
		return mHandler.sendEmptyMessage(what);
	}

	public boolean sendEmptyMessageDelay(int what, long delay) {
		return mHandler.sendEmptyMessageDelayed(what, delay);
	}

	public boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
		return mHandler.sendEmptyMessageAtTime(what, uptimeMillis);
	}

	public boolean sendMessage(Message msg) {
		return mHandler.sendMessage(msg);
	}

	public boolean sendMessageDelay(Message msg, long delay) {
		return mHandler.sendMessageDelayed(msg, delay);
	}

	public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
		return mHandler.sendMessageAtTime(msg, uptimeMillis);
	}

	public final boolean post(Runnable r) {
		return mHandler.post(r);
	}

	public final boolean postAtTime(Runnable r, long uptimeMillis) {
		return mHandler.postAtTime(r, uptimeMillis);
	}

	public final boolean postDelayed(Runnable r, long delayMillis) {
		return mHandler.postDelayed(r, delayMillis);
	}

	public void removeCallbacks(Runnable r) {
		mHandler.removeCallbacks(r);
	}

	public final void removeMessages(int what) {
		mHandler.removeMessages(what);
	}

	/**
	 * Remove any pending posts of messages with code 'what' and whose obj is
	 * 'object' that are in the message queue.
	 */
	public final void removeMessages(int what, Object object) {
		mHandler.removeMessages(what, object);
	}

	public void cancel() {
		mHandler.stop();
		mHandlerThread.quit();
	}

	/**
	 * 处理经过handler异步转换的消息
	 * 
	 * @param msg
	 *            消息
	 */
	public abstract void handleAsyncMessage(Message msg);

	private class HandlerImpl extends Handler {
		boolean mStop = false;

		public HandlerImpl(Looper looper) {
			super(looper);
		}

		void stop() {
			mStop = true;
		}

		@Override
		public void handleMessage(Message msg) {
			if (!mStop) {
				handleAsyncMessage(msg);
			}
		}
	}
}
