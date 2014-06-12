package com.gau.go.launcherex.theme.cover;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

/**
 * 
 * <br>类描述:中间层bitmap加载和缓存器
 * <br>注意：罩子层和中间层已经分开
 * 
 * @author  guoyiqing
 * @date  [2012-12-1]
 */
public class MiddleBitmapLoader {

	private static final int INIT_BITMAP_CAPACITY = 50;
	private static final int DEFAULT_MIDDLE_IMG_WIDTH = 480;
	private static final int DEFAULT_MIDDLE_IMG_HEIGHT = 800;
	private static MiddleBitmapLoader sInstance;
	private HashMap<String, SoftReference<Bitmap>> mMiddleCache; // 重新一个缓存，原因罩子层和中间层可分开出现
	private HashMap<String, String> mMiddleBitmapToNameMap; // 重新一个缓存，原因罩子层和中间层可分开出现
	private Context mContext;
	private Options mOptions;
	private int mViewWidth;
	private int mViewHeight;
	
	private MiddleBitmapLoader(Context context) {
		mContext = context;
		mOptions = new BitmapFactory.Options();
		mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		mOptions.inPurgeable = true;
		mOptions.inInputShareable = true;
		mMiddleCache = new HashMap<String, SoftReference<Bitmap>>(INIT_BITMAP_CAPACITY);
		mMiddleBitmapToNameMap = new HashMap<String, String>(INIT_BITMAP_CAPACITY);
	}
	
	public static synchronized MiddleBitmapLoader getLoader(Context context) {
		if (sInstance == null) {
			sInstance = new MiddleBitmapLoader(context);
		}
		return sInstance;
	}
	
	/**
	 * <br>功能简述:销毁中间层bitmap
	 * <br>注意:罩子层和中间层已分开
	 */
	public void recyleAllMiddleBitmap() {
		if (mMiddleCache != null) {
			for (SoftReference<Bitmap> reference : mMiddleCache.values()) {
				if (reference != null) {
					Bitmap bitmap = reference.get();
					if (bitmap != null && !bitmap.isRecycled()) {
						bitmap.recycle();
						bitmap = null;
					}
				}
			}
			mMiddleCache.clear();
		}
		if (mMiddleBitmapToNameMap != null) {
			mMiddleBitmapToNameMap.clear();
		}
	}

	/**
	 * <br>功能简述:统一销毁单个中间层Bitmap的地方
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 */
	public void recyleMiddleBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			String drawableName = mMiddleBitmapToNameMap.get(bitmap.toString());
			if (drawableName != null) {
				mMiddleCache.remove(drawableName);
				mMiddleBitmapToNameMap.remove(bitmap.toString());
				if (!bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
	}
	
	/**
	 * <br>功能简述:获取中间层bitmap
	 * <br>注意:罩子层和中间层已分开
	 * @param drawableName
	 * @return
	 */
	public Bitmap getMiddleBitmap(String drawableName) {
		if (drawableName == null || mContext == null) {
			return null;

		}
		Bitmap bitmap = null;
		SoftReference<Bitmap> bitmapReference = mMiddleCache.get(drawableName);
		if (bitmapReference != null) {
			bitmap = bitmapReference.get();
			if (bitmap != null) {
				return bitmap;
			}
		}
		try {
			Resources res = mContext.getResources();
			int resId = res.getIdentifier(drawableName, "raw", CoverBean.PACKAGE_NAME);
			bitmap = readBitMap(res, resId);
			mViewHeight = DrawUtils.getMiddleViewHeight();
			mViewWidth = DrawUtils.getMiddleViewWidth();
			if (mViewWidth > mViewHeight) {
				int temp = mViewWidth;
				mViewWidth = mViewHeight;
				mViewHeight = temp;
			}
			float widthScale = 0.0f;
			float heightScale = 0.0f;
			widthScale = (mViewWidth + 0.1f) / DEFAULT_MIDDLE_IMG_WIDTH;
			heightScale = (mViewHeight + 0.1f) / DEFAULT_MIDDLE_IMG_HEIGHT;
			if (!isEquals(widthScale, 1.0f) && !isEquals(heightScale, 1.0f) 
					&& bitmap != null) {
				bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * widthScale),
						(int) (bitmap.getHeight() * heightScale), true);
			}
			if (bitmap != null) {
				mMiddleCache.put(drawableName, new SoftReference<Bitmap>(bitmap));
				mMiddleBitmapToNameMap.put(bitmap.toString(), drawableName);
			}
			
		} catch (NotFoundException e) {
			//			Log.i("ImageExplorer", "getDrawable() " + drawableName + " NotFoundException");
		} catch (OutOfMemoryError e) {
			//			Log.i("ImageExplorer", "getDrawable() " + drawableName + " OutOfMemoryError");
		} catch (Exception e) {
			//			Log.i("ImageExplorer", "getDrawable()" + drawableName + " has Exception");
		}
		return bitmap;
	}
	
	private Bitmap readBitMap(Resources res, int resId) {
		InputStream is = res.openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, mOptions);
	}
	
	private boolean isEquals(float num1, float num2) {
		if (Math.abs(num1 - num2) < 1E-2) {
			return true;
		}
		return false;
	}
}
