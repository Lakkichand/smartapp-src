package com.go.util.file.media;

import android.os.Handler;
import android.os.Looper;

/**
 * 
 * 多媒体数据加载线程
 */
public class FileManageTask {
	private final static String THREAD_POOL_MANAGER_NAME = "FileManageTask_pool";
	private final static byte[] LOCKER = new byte[0];
	private final static Handler HANDLER;
	public TaskCallBack callback;
	public Runnable runnable;
	private Runnable mTask;
	public int mType;
	static {
		HANDLER = new Handler(Looper.getMainLooper());
	}

	public FileManageTask(int type, Runnable runnable, TaskCallBack callback) {
		mType = type;
		this.runnable = runnable;
		this.callback = callback;
	}

	private void onPreExecute() {
		if (callback != null) {
			HANDLER.post(new Runnable() {
				@Override
				public void run() {
					callback.onPreExecute(FileManageTask.this);
				}
			});

		}
	}

	private void doInBackground() {
		synchronized (LOCKER) {
			if (runnable != null) {
				runnable.run();
			}
			if (callback != null) {
				callback.doInBackground(FileManageTask.this);
			}
		}
	}

	private void onPostExecute() {
		if (callback != null) {
			HANDLER.post(new Runnable() {
				@Override
				public void run() {
					callback.onPostExecute(FileManageTask.this);
				}
			});
		}
	}

	public void execute() {
		if (runnable != null) {
			mTask = new Runnable() {

				@Override
				public void run() {
					onPreExecute();
					doInBackground();
					onPostExecute();
				}
			};
			MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME).execute(mTask);
		}
	}

	public void cancel() {
		if (mTask != null) {
			MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME).cancel(mTask);
		}
	}

	/**
	 * 
	 * 回调函数
	 */
	public static interface TaskCallBack {
		public void onPreExecute(FileManageTask mTask);

		public Object doInBackground(FileManageTask mTask);

		public void onPostExecute(FileManageTask mTask);
	}
}
