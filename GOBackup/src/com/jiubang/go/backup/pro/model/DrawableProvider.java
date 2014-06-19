package com.jiubang.go.backup.pro.model;

import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 *
 */
public class DrawableProvider {
	private static final int THREAD_POOL_SIZE = 3;
	private static final String CACHE_DIR = "img_cache";
	private static DrawableProvider sInstance = null;
	private Map<Object, DrawableReference> mCaches = null;
	private ReferenceQueue<Drawable> mReferenceQueue;
	private ExecutorService mThreadPool;
	
	private DrawableProvider() {
		mCaches = new HashMap<Object, DrawableReference>();
		mReferenceQueue = new ReferenceQueue<Drawable>();
		mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	}
	
	public synchronized static DrawableProvider getInstance() {
		if (sInstance == null) {
			sInstance = new DrawableProvider();
		}
		return sInstance;
	}
	
	public Drawable getDrawable(Context context, DrawableKey drawableKey, Drawable defaultDrawable) {
		Drawable drawable = getDrawable(context, drawableKey);
		return drawable != null ? drawable : defaultDrawable;
	}
	
	public Drawable getDrawable(Context context, DrawableKey drawableKey) {
		Drawable drawable = null;
		
		drawable = getDrawableFromCaches(drawableKey);
		
		if (drawable == null) {
			drawable = loadDrawable(context, drawableKey);
		}
		return drawable;
	}
	
	public Drawable getDrawable(final Context context, final DrawableKey drawableKey, final OnDrawableLoadedListener listener) {
		Drawable drawable = getDrawableFromCaches(drawableKey);
		if (drawable != null) {
//			LogUtil.d("getDrawableFromCaches " + drawableKey.getKey().toString());
			return drawable;
		}
		if (listener != null) {
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					Drawable drawable = loadDrawable(context, drawableKey);
					listener.onDrawableLoaded(drawable);
				}
			});
		}
		return null;
	}
	
	public Drawable getDrawable(Context context, DrawableKey drawableKey, Drawable defaultDrawable, OnDrawableLoadedListener listener) {
		Drawable drawable = getDrawable(context, drawableKey, listener);
		if (drawable != null) {
			return drawable;
		}
		return defaultDrawable;
	}
	
	public Drawable getDrawableFromCaches(DrawableKey drawableKey) {
		Drawable drawable = null;
		if (mCaches.containsKey(drawableKey.getKey())) {
			DrawableReference ref = mCaches.get(drawableKey.getKey());
			if (ref != null) {
				drawable = ref.get();
			}
		}
		return drawable;
	}
	
	private Drawable loadDrawable(Context context, DrawableKey drawableKey) {
//		Drawable drawable = getDrawableFromCacheFile(context, drawableKey);
		Drawable drawable = null;
		if (drawable == null) {
			if (drawableKey instanceof DrawableLoader) {
				drawable = ((DrawableLoader) drawableKey).load(context);
			}
			if (drawable != null) {
				cacheDrawable(context, drawable, drawableKey);
			}
		}
		return drawable;
	}
	
	private void cleanCache() {
		DrawableReference ref = null;
		while ((ref = (DrawableReference) mReferenceQueue.poll()) != null) {
			Object key = ref.getKey();
			mCaches.remove(key);
			Drawable drawable = ref.get();
			if (drawable instanceof BitmapDrawable) {
				((BitmapDrawable) drawable).getBitmap().recycle();
			}
//			LogUtil.d("Cache " + key.toString() + " cleaned");
		}
	}
	
	public void cacheDrawable(final Context context, final Drawable drawable, DrawableKey drawableKey) {
		cleanCache();
		final Object innerKey = drawableKey.getKey();
		mCaches.put(innerKey, new DrawableReference(innerKey, drawable, mReferenceQueue));
//		LogUtil.d("cache drawable " + innerKey.toString());
		
//		if (drawableKey instanceof PackageDrawable) {
//			mThreadPool.execute(new Runnable() {
//				@Override
//				public void run() {
//					cacheDrawableToFile(context, drawable, innerKey.toString());
//				}
//			});
//		}
	}
	
	public static Drawable getDefaultActivityIcon(Context context) {
		if (context == null) {
			return null;
		}
		PackageManager pm = context.getPackageManager();
		if (pm != null) {
			return pm.getDefaultActivityIcon();
		}
		return null;
	}
	
/*	private Drawable getDrawableFromCacheFile(Context context, DrawableKey drawableKey) {
		if (context == null || drawableKey == null) {
			return null;
		}
		String innerKey = drawableKey.getKey().toString();
		File cacheFile = new File(getCacheDir(context), innerKey);
		if (cacheFile.exists()) {
			return new ImageFileDrawable(cacheFile).load(context);
		}
		return null;
	}
	
	private void cacheDrawableToFile(Context context, Drawable drawable, String fileName) {
		if (!(drawable instanceof BitmapDrawable) || fileName == null) {
			return;
		}
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		try {
			FileOutputStream fos = new FileOutputStream(new File(getCacheDir(context), fileName));
			bitmap.compress(CompressFormat.JPEG, 100, new BufferedOutputStream(fos));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	private File getCacheDir(Context context) {
		File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return cacheDir;
	}
	
	public void clearAllCaches(Context context) {
		cleanCache();
		Util.deleteFile(getCacheDir(context).getAbsolutePath());
	}
	
	
	/**
	 * @author maiyongshen
	 *
	 */
	private class DrawableReference extends SoftReference<Drawable> {
		private Object mKey;
		
		public DrawableReference(Object key, Drawable drawable, ReferenceQueue<Drawable> referenceQueue) {
			super(drawable, referenceQueue);
			if (key == null) {
				throw new IllegalArgumentException();
			}
			mKey = key;
		}
		
		public Object getKey() {
			return mKey;
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static interface OnDrawableLoadedListener {
		public void onDrawableLoaded(Drawable drawable);
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static interface DrawableKey {
		public Object getKey();
	}
	
	public static DrawableKey buildDrawableKey(int resId) {
		return new ResourceDrawable(resId);
	}
	
	public static DrawableKey buildDrawableKey(String packageName) {
		return new PackageDrawable(packageName);
	}
	
	public static DrawableKey buildDrawableKey(Context context, File apkFile) {
		String packageName = Util.getPackageNameFromApk(context, apkFile);
		return buildDrawableKey(packageName, null, apkFile);
	}
	
	public static DrawableKey buildDrawableKey(String packageName, byte[] rawData) {
		return buildDrawableKey(packageName, rawData, null);
	}
	
	public static DrawableKey buildDrawableKey(String packageName, byte[] rawData, File apkFile) {
		PackageDrawable drawableKey = new PackageDrawable(packageName);
		if (rawData != null && rawData.length > 0) {
			drawableKey.withRawData(rawData);
		}
		if (apkFile != null && apkFile.exists()) {
			drawableKey.withApkFile(apkFile.getAbsolutePath());
		}
		return drawableKey;
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	private static interface DrawableLoader {
		public Drawable load(Context context);
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	private static class ResourceDrawable implements DrawableLoader, DrawableKey {
		private int mResourceId;
		
		public ResourceDrawable(int resId) {
			mResourceId = resId;
		}
		
		@Override
		public Object getKey() {
			return new Integer(mResourceId);
		}

		@Override
		public Drawable load(Context context) {
			return context.getResources().getDrawable(mResourceId);
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	private static class PackageDrawable implements DrawableLoader, DrawableKey {
		private String mPackageName;
		private String mApkFilePath;
		private byte[] mRawData;
		
		public PackageDrawable(String packageName) {
			if (TextUtils.isEmpty(packageName)) {
				throw new IllegalArgumentException();
			}
			mPackageName = packageName;
		}
		
		@Override
		public Object getKey() {
			return mPackageName;
		}

		@Override
		public Drawable load(Context context) {
			Drawable drawable = null;
			try {
				PackageManager pm = context.getPackageManager();
				drawable = pm.getApplicationIcon(mPackageName);
//				if (drawable != null) {
//					LogUtil.d("load drawable for package " + mPackageName);
//				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
			if (drawable == null && mRawData != null && mRawData.length > 0) {
				Bitmap bitmap = Util.byteArrayToBitmap(mRawData);
				if (bitmap != null) {
					drawable = new BitmapDrawable(context.getResources(), bitmap);
				}
//				if (drawable != null) {
//					LogUtil.d("load drawable from raw data");
//				}
			}
			
			if (drawable == null && !TextUtils.isEmpty(mApkFilePath)) {
				drawable = Util.loadIconFromAPK(context, mApkFilePath);
//				if (drawable != null) {
//					LogUtil.d("load drawable from apk");
//				}
			}
			
			return drawable;
		}
		
		public PackageDrawable withApkFile(String apkFilePath) {
			mApkFilePath = apkFilePath;
			return this;
		}
		
		public PackageDrawable withRawData(byte[] rawData) {
			mRawData = rawData;
			return this;
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	private class ImageFileDrawable implements DrawableLoader, DrawableKey {
		private File mImageFile;
		
		public ImageFileDrawable(File imageFile) {
			if (imageFile == null || !imageFile.exists()) {
				throw new IllegalArgumentException();
			}
			mImageFile = imageFile;
		}

		@Override
		public Object getKey() {
			return mImageFile.getAbsolutePath();
		}

		@Override
		public Drawable load(Context context) {
			try {
				return new BitmapDrawable(context.getResources(), mImageFile.getAbsolutePath());
			} catch (OutOfMemoryError e) {
				
			}
			return null;
		}
	}
}
