package com.jiubang.ggheart.launcher;

import android.os.Handler;
import android.os.Message;

public class MyThread extends Thread {
	private boolean mRunFlag = true;

	public MyThread() {
		super();
	}

	public MyThread(Runnable runnable) {
		super(runnable);
	}

	public MyThread(Runnable runnable, String threadName) {
		super(runnable, threadName);
	}

	public MyThread(String threadName) {
		super(threadName);
	}

	public synchronized void setRunFlag(boolean flag) {
		mRunFlag = flag;
	}

	public synchronized boolean getRunFlag() {
		return mRunFlag;
	}

	@Override
	public void run() {
		super.run();

		doBackground();
	}

	protected void doBackground() {

	}

	protected void doUpdateUI(Object obj) {

	}

	private final int MESSAGE_UPDATE_UI = 1000;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (null == msg) {
				return;
			}
			if (MESSAGE_UPDATE_UI == msg.what) {
				doUpdateUI(msg.obj);
			}
		};
	};

	protected void updateUI(Object obj) {
		Message msg = new Message();
		msg.what = MESSAGE_UPDATE_UI;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}
