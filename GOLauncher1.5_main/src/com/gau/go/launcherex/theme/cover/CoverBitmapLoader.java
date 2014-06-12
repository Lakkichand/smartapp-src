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
 * <br>类描述:罩子层bitmap加载和缓存器
 * <br>注意：罩子层和中间层已经分开
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-25]
 */
public class CoverBitmapLoader {

	private static final int INIT_BITMAP_CAPACITY = 50;
	private HashMap<String, SoftReference<Bitmap>> mCoverCache;
	private HashMap<String, String> mCoverBitmapToNameMap;
	private static CoverBitmapLoader sInstance;
	private float mScale = 1.0f; // 图片的缩放比
	private Context mContext;
	private Options mOptions;


	private CoverBitmapLoader(Context context) {
		mContext = context;
		mOptions = new BitmapFactory.Options();
		mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		mOptions.inPurgeable = true;
		mOptions.inInputShareable = true;
		setScale();
		mCoverCache = new HashMap<String, SoftReference<Bitmap>>(INIT_BITMAP_CAPACITY);
		mCoverBitmapToNameMap = new HashMap<String, String>(INIT_BITMAP_CAPACITY);
		
	}

	private void setScale() {
		final int srcWidth = DrawUtils.isPort() ? 480 : 800;
		// 用屏幕宽作为缩放比例参照
		mScale = (DrawUtils.getScreenViewWidth() + 0.1f) / srcWidth;
	}

	public static synchronized CoverBitmapLoader getLoader(Context context) {
		if (sInstance == null) {
			sInstance = new CoverBitmapLoader(context);
		}
		return sInstance;
	}

	/**
	 * <br>功能简述:销毁罩子层bitmap
	 * <br>注意:罩子层和中间层已分开
	 */
	public void recyleAllCoverBitmap() {
		if (mCoverCache != null) {
			for (SoftReference<Bitmap> reference : mCoverCache.values()) {
				if (reference != null) {
					Bitmap bitmap = reference.get();
					if (bitmap != null && !bitmap.isRecycled()) {
						bitmap.recycle();
						bitmap = null;
					}
				}
			}
			mCoverCache.clear();
		}
		if (mCoverBitmapToNameMap != null) {
			mCoverBitmapToNameMap.clear();
		}
	}

	/**
	 * <br>功能简述:统一销毁单个罩子层Bitmap的地方
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 */
	public void recyleCoverBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			String drawableName = mCoverBitmapToNameMap.get(bitmap.toString());
			if (drawableName != null) {
				mCoverCache.remove(drawableName);
				mCoverBitmapToNameMap.remove(bitmap.toString());
				if (!bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
	}


	/**
	 * <br>功能简述:获取罩子层bitmap
	 * <br>注意:罩子层和中间层已分开
	 * @param drawableName
	 * @return
	 */
	public Bitmap getCoverBitmap(String drawableName) {
		if (drawableName == null || mContext == null) {
			return null;

		}
		Bitmap bitmap = null;
		SoftReference<Bitmap> bitmapReference = mCoverCache.get(drawableName);
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
			if (mScale != 1.0 && bitmap != null) {
				bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * mScale),
						(int) (bitmap.getHeight() * mScale), true);
			}
			if (bitmap != null) {
				mCoverCache.put(drawableName, new SoftReference<Bitmap>(bitmap));
				mCoverBitmapToNameMap.put(bitmap.toString(), drawableName);
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

}
