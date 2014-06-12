package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * 
 * <br>类描述: * 类描述:加载Drawable任务的异步线程，模块退出时要取消 
 * 
 * @author guoyiqing
 * @param <V> 
 * @param <T>
 */
public class DrawableLoadTasker<V extends ImageView, T extends IDrawableLoader>
		extends
			AsyncTask<Void, LoadingDrawableItem<V, T>, Void> {
	private LinkedBlockingQueue<LoadingDrawableItem<V, T>> mQueue;
	private static DrawableLoadTasker<ImageView, IDrawableLoader> sTasker;

	public static synchronized DrawableLoadTasker<ImageView, IDrawableLoader> getInstance() {
		if (sTasker == null) {
			sTasker = new DrawableLoadTasker<ImageView, IDrawableLoader>();
		}
		return sTasker;
	}

	private DrawableLoadTasker() {
		mQueue = new LinkedBlockingQueue<LoadingDrawableItem<V, T>>();
	}

	public void reset() {
		mQueue.clear();
	}

	public static void clear() {
		if (sTasker != null) {
			sTasker.cancel(true);
		}
		sTasker = null;
	}

	public void addLoadingInfo(LoadingDrawableItem<V, T> item) {
		if (item.isLoaded()) {
			return;
		}
		mQueue.add(item);
		if (getStatus().equals(Status.PENDING)) {
			execute((Void) null);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground(Void... params) {
		while (true) {
			try {
				LoadingDrawableItem<V, T> item = mQueue.poll(1, TimeUnit.SECONDS); // 查看队列中是否有新任务
				if (isCancelled()) {
					break;
				}
				if (item == null) {
					continue;
				}
				item.doLoading();
				publishProgress(item);
			} catch (InterruptedException e) {
				Log.e("DrawableLoadTasker", e.getMessage());
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(LoadingDrawableItem<V, T>... values) {
		if (!values[0].isLoaded()) {
			return;
		}
		values[0].displayResult();
	}

}
