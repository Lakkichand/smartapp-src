package com.zhidian.wifibox.handler;

import java.util.LinkedList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;

/**
 * 延时消息处理，通过此类发送的消息只会在系统UI空闲(Idle)时才处理. 需要实现handleIdleMessage接口. 消息队列为先进先出
 * 
 * @author luopeihuan
 * 
 */
public abstract class DeferredHandler {
	private final static int NEXT_MESSAGE = -1000;
	private MessageQueue mMessageQueue = Looper.myQueue();
	private Impl mHandler = new Impl();
	private final LinkedList<Message> mQueue = new LinkedList<Message>();

	/**
	 * 空闲时处理消息
	 * 
	 * @param msg
	 */
	public abstract void handleIdleMessage(Message msg);

	/**
	 * 发送消息到队列
	 * 
	 * @param what
	 *            消息id
	 */
	public void sendEmptyMessage(int what) {
		Message message = new Message();
		message.what = what;
		sendMessage(message);
	}

	/**
	 * 发送消息到队列
	 * 
	 * @param message
	 *            消息
	 */
	public void sendMessage(Message message) {
		synchronized (mQueue) {
			if (message != null) {
				mQueue.add(message);
			}

			if (mQueue.size() == 1) {
				scheduleNextLocked();
			}
		}
	}

	/**
	 * 发送消息到队列
	 * 
	 * @param what
	 *            消息id
	 * @param arg1
	 *            消息参数1
	 * @param arg2
	 *            消息参数2
	 * @param obj
	 *            消息参数obj
	 */
	public void sendMessage(int what, int arg1, int arg2, Object obj) {
		Message message = new Message();
		message.what = what;
		message.arg1 = arg1;
		message.arg2 = arg2;
		message.obj = obj;
		sendMessage(message);
	}

	/**
	 * 清空消息队列
	 */
	public void cancel() {
		// Log.v("DeferredHandler", "cancel");
		mMessageQueue.removeIdleHandler(mHandler);
		synchronized (mQueue) {
			mQueue.clear();
		}
	}

	/**
	 * 删除某条消息
	 * 
	 * @param message
	 */
	public void remove(Message message) {
		if (message != null) {
			synchronized (mQueue) {
				mQueue.remove(message);
			}
		}
	}

	private void scheduleNextLocked() {
		if (mQueue.size() > 0) {
			mMessageQueue.addIdleHandler(mHandler);
		}
	}

	private class Impl extends Handler implements MessageQueue.IdleHandler {
		@Override
		public void handleMessage(Message msg) {
			Message m = null;
			synchronized (mQueue) {
				if (mQueue.size() == 0) {
					return;
				}
				m = mQueue.removeFirst();
			}

			// Log.v("DeferredHandler", "handleIdleMessage");
			// 处理消息
			handleIdleMessage(m);
			synchronized (mQueue) {
				scheduleNextLocked();
			}
		}

		@Override
		public boolean queueIdle() {
			// Log.v("DeferredHandler", "queueIdle");
			sendEmptyMessage(NEXT_MESSAGE);
			return false;
		}
	}
}
