package com.zhidian3g.wifibox.imagemanager;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.smartapp.autostartmanager.AppUtils;
import com.smartapp.autostartmanager.FileUtil;
import com.ta.TAApplication;

/**
 * 
 * 异步加载图片
 * 
 * @author xiedezhi
 * 
 */
public class AsyncImageManager {
	/**
	 * 跟线程池有关的变量
	 */
	private static final String THREAD_POOL_MANAGER_NAME = "asyncimagemanager_threadPoolManager";
	private static final int THREAD_POOL_COREPOOL_SIZE = 1;
	private static final int THREAD_POOL_MAXIMUMPOOL_SIZE = 3;
	private static final long THREAD_POOL_KEEPALIVE_TIME = 20;
	private static final int CONNECT_TIME_OUT = 10000;
	private static final int READ_TIME_OUT = 30000;
	private ThreadPoolManager mThreadPoolManager = null;
	/**
	 * 单例
	 */
	private static AsyncImageManager sInstance = null;
	/**
	 * 同步主线程的Handler
	 */
	private Handler mHandler = new Handler(Looper.getMainLooper());
	/**
	 * 图片缓存池
	 */
	private IImageCache mImageCache = null;

	private AsyncImageManager() {
		this(null);
	}

	private AsyncImageManager(IImageCache imageCache) {
		mImageCache = imageCache;
		if (mImageCache == null) {
			mImageCache = new DefaultImageCache();
		}
		ThreadPoolManager.buildInstance(THREAD_POOL_MANAGER_NAME,
				THREAD_POOL_COREPOOL_SIZE, THREAD_POOL_MAXIMUMPOOL_SIZE,
				THREAD_POOL_KEEPALIVE_TIME, TimeUnit.SECONDS, false);
		mThreadPoolManager = ThreadPoolManager
				.getInstance(THREAD_POOL_MANAGER_NAME);
	}

	public static AsyncImageManager getInstance() {
		return buildInstance(null);
	}

	public synchronized static AsyncImageManager buildInstance(
			IImageCache imageCache) {
		if (sInstance == null) {
			sInstance = new AsyncImageManager(imageCache);
		}
		return sInstance;
	}

	/**
	 * 通过图片的URL从内存获取图片的方法
	 */
	public Bitmap loadImgFromMemery(String imgUrl) {
		Bitmap bm = null;
		if (!TextUtils.isEmpty(imgUrl)) {
			bm = mImageCache.get(imgUrl);
		}
		return bm;
	}

	/**
	 * 功能简述:从SD卡加载图片的方法 功能详细描述: 注意:
	 * 
	 * @param imgPath
	 *            图片所在路径
	 * @param imgName
	 *            图片名称
	 * @param imgUrl
	 *            图片URL
	 * @param isCache
	 *            是否添加到内存的缓存里面
	 * @return
	 */
	public Bitmap loadImgFromSD(String imgPath, String imgName, String imgUrl,
			boolean isCache) {
		Bitmap result = null;
		try {
			if (FileUtil.isSDCardAvaiable()) {
				File file = new File(imgPath + imgName);
				if (file.exists()) {
					result = BitmapFactory.decodeFile(imgPath + imgName);
					if (result != null && isCache) {
						mImageCache.set(imgUrl, result);
					}
				}
			}
		} catch (OutOfMemoryError ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * 根据URL生成HttpURLConnection的方法
	 */
	public HttpURLConnection createURLConnection(String url) throws Exception {
		HttpURLConnection urlCon = null;
		urlCon = (HttpURLConnection) new URL(url).openConnection();
		urlCon.setConnectTimeout(CONNECT_TIME_OUT);
		urlCon.setReadTimeout(READ_TIME_OUT);
		return urlCon;
	}

	/**
	 * 加载应用图标
	 */
	public Bitmap loadAppIcon(Context ctx, String pkgName) {
		ApplicationInfo info = AppUtils.getAppInfo(ctx, pkgName);
		if (info != null) {
			Drawable drawable = info.loadIcon(ctx.getPackageManager());
			if (drawable != null && drawable instanceof BitmapDrawable) {
				return ((BitmapDrawable) drawable).getBitmap();
			}
		}
		return null;
	}

	/**
	 * 从网络加载图片的方法
	 */
	public Bitmap loadImgFromNetwork(String imgUrl) {
		Bitmap result = null;
		InputStream inputStream = null;
		HttpURLConnection urlCon = null;
		try {
			urlCon = createURLConnection(imgUrl);
			inputStream = (InputStream) urlCon.getContent();
			if (inputStream != null) {
				result = BitmapFactory.decodeStream(inputStream);
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (urlCon != null) {
				urlCon.disconnect();
			}
		}
		return result;
	}

	/**
	 * 功能简述: 加载图片，如果图片在内存里，则直接返回图片，否则异步从SD卡或者网络加载图片 功能详细描述: 注意:
	 * 
	 * @param imgPath
	 *            图片保存的SD卡目录
	 * @param imgName
	 *            图片保存的名称
	 * @param imgUrl
	 *            从网络加载图片的URL
	 * @param isCache
	 *            是否缓存到内存
	 * @param fillet
	 *            是否加上圆角
	 * @param callBack
	 *            回调
	 * @return
	 */
	public Bitmap loadImage(final String imgPath, final String imgName,
			final String imgUrl, final boolean isCache,
			final AsyncImageLoadedCallBack callBack) {
		if (imgUrl == null || imgUrl.equals("")) {
			return null;
		}
		Bitmap result = null;
		result = loadImgFromMemery(imgUrl);
		if (result == null) {
			if (mThreadPoolManager == null || mThreadPoolManager.isShutdown()) {
				Log.e("AsyncImageManager",
						"threadPoll == null || threadPoll.isShutdown()");
				return null;
			}
			Runnable loadimg = new Runnable() {
				@Override
				public void run() {
					Bitmap b = null;
					try {
						b = loadImgFromSD(imgPath, imgName, imgUrl, isCache);
						if (b == null) {
							b = loadImgFromNetwork(imgUrl);
							if (b != null) {
								if (FileUtil.isSDCardAvaiable()) {
									FileUtil.saveBitmapToSDFile(b, imgPath
											+ imgName,
											Bitmap.CompressFormat.PNG);
								}
							}
						}
					} catch (OutOfMemoryError error) {
						// 爆内存
						error.printStackTrace();
					}
					if (b != null) {
						if (isCache) {
							mImageCache.set(imgUrl, b);
						}
					}
					// 主线程显示图片
					CallBackRunnable cbRunnable = new CallBackRunnable(b,
							callBack, imgUrl);
					mHandler.post(cbRunnable);

				}
			};
			mThreadPoolManager.execute(loadimg);
		}
		return result;
	}

	/**
	 * 加载已安装应用图标
	 * 
	 * @param pkgName
	 *            包名
	 * @param isCache
	 *            是否缓存
	 * @param fillet
	 *            是否加上圆角
	 * @param callBack
	 *            回调
	 * @return
	 */
	public Bitmap loadIcon(final String pkgName, final boolean isCache,
			final AsyncImageLoadedCallBack callBack) {
		if (pkgName == null || pkgName.equals("")) {
			return null;
		}
		Bitmap result = null;
		result = loadImgFromMemery(pkgName);
		if (result == null) {
			if (mThreadPoolManager == null || mThreadPoolManager.isShutdown()) {
				Log.e("AsyncImageManager",
						"threadPoll == null || threadPoll.isShutdown()");
				return null;
			}
			Runnable loadimg = new Runnable() {
				@Override
				public void run() {
					Bitmap b = null;
					try {
						b = loadAppIcon(TAApplication.getApplication(), pkgName);
					} catch (OutOfMemoryError error) {
						// 爆内存
						error.printStackTrace();
					}
					if (b != null) {
						if (isCache) {
							mImageCache.set(pkgName, b);
						}
					}
					// 主线程显示图片
					CallBackRunnable cbRunnable = new CallBackRunnable(b,
							callBack, pkgName);
					mHandler.post(cbRunnable);

				}
			};
			mThreadPoolManager.execute(loadimg);
		}
		return result;
	}

	/**
	 * 取消所有未执行的加载图标任务
	 */
	public void removeAllTask() {
		mThreadPoolManager.removeAllTask();
	}

	/**
	 * 销毁单例
	 */
	public synchronized static void destory() {
		if (sInstance != null) {
			sInstance.clear();
			sInstance = null;
		}
	}

	public void clear() {
		if (mImageCache != null) {
			mImageCache.clear();
		}
	}

	/**
	 * 获取图片后，同步到主线程，回调Runable
	 */
	public static class CallBackRunnable implements Runnable {
		private Bitmap mBitmap = null;
		private AsyncImageLoadedCallBack mCallBack = null;
		private String mImgUrl = null;

		CallBackRunnable(Bitmap img, AsyncImageLoadedCallBack callBack,
				String imgUrl) {
			this.mBitmap = img;
			this.mCallBack = callBack;
			this.mImgUrl = imgUrl;
		}

		@Override
		public void run() {
			if (mCallBack != null) {
				mCallBack.imageLoaded(mBitmap, mImgUrl);
			}
		}
	}

	/**
	 * 获取图片成功后的回调接口
	 */
	public static interface AsyncImageLoadedCallBack {
		public void imageLoaded(Bitmap imageBitmap, String imgUrl);
	}
}
