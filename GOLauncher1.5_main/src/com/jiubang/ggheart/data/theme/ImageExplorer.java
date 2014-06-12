package com.jiubang.ggheart.data.theme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.data.theme.zip.ZipResources;

/**
 * 图片管理器
 *
 */
public class ImageExplorer implements IExplorer {

	private Context mContext = null;
	private static ImageExplorer sInstance = null;
	private Method mLocalMethod = null;
	private Class[] mArrayOfClass = null;
	private boolean mCanDrawableForDensity = true;
	private HashMap<String, Integer> mResourceIdMap = new HashMap<String, Integer>();

	private ImageExplorer(Context context) {
		mContext = context;
	}

	public synchronized static ImageExplorer getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ImageExplorer(context);
		}
		return sInstance;
	}

	@Override
	public void clearData() {
		mResourceIdMap.clear();
	}

	public Drawable getDrawable(String drawableName) {
		String themePackageName = ThemeManager.getInstance(mContext).getCurThemePackage();
		Drawable drawable = getDrawable(themePackageName, drawableName);
		return drawable;
	}

	public int getResourceId(String drawableName) {
		if (drawableName == null) {
			return -1;
		}
		int id = -1;
		String themePackageName = ThemeManager.getInstance(mContext).getCurThemePackage();
		if (ThemeManager.DEFAULT_THEME_PACKAGE_3.equals(themePackageName)) {
			themePackageName = ThemeManager.DEFAULT_THEME_PACKAGE;
		}
		if (mResourceIdMap.containsKey(drawableName)) {
			id = mResourceIdMap.get(drawableName);
		}
		if (id == -1) {
			try {
				Resources themeResources = null;
				if (AppUtils.isAppExist(mContext, themePackageName)) {
					themeResources = mContext.getPackageManager().getResourcesForApplication(
							themePackageName);
				} else {
					themeResources = ZipResources.getThemeResourcesFromReflect(mContext,
							themePackageName);
				}
				id = themeResources.getIdentifier(drawableName, "drawable", themePackageName);
				mResourceIdMap.put(drawableName, id);
			} catch (Exception e) {
				Log.i("ImageExplorer", "getResourceId" + drawableName + " has Exception");
			}
		}
		return id;
	}

	public int getResourceId(String drawableName, String packageName) {
		if (drawableName == null) {
			return -1;
		}
		int id = -1;
		if (ThemeManager.DEFAULT_THEME_PACKAGE_3.equals(packageName)) {
			packageName = ThemeManager.DEFAULT_THEME_PACKAGE;
		}
		try {
			Resources themeResources = null;
			if (AppUtils.isAppExist(mContext, packageName)) {
				themeResources = mContext.getPackageManager().getResourcesForApplication(
						packageName);
			} else {
				themeResources = ZipResources.getThemeResourcesFromReflect(mContext, packageName);
			}
			id = themeResources.getIdentifier(drawableName, "drawable", packageName);
		} catch (Exception e) {
			Log.i("ImageExplorer", "getResourceId" + drawableName + " has Exception");
		}
		return id;
	}

	public Drawable getDrawable(String themePackage, String drawableName) {
		if (drawableName == null) {
			return null;
		}
		if (ThemeManager.DEFAULT_THEME_PACKAGE_3.equals(themePackage)) {
			themePackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		}
		Drawable drawable = null;
		try {
			Resources themeResources = null;
			if (AppUtils.isAppExist(mContext, themePackage)) {
				themeResources = mContext.getPackageManager().getResourcesForApplication(
						themePackage);
			} else {
				themeResources = ZipResources.getThemeResourcesFromReflect(mContext, themePackage);
			}
			int resourceId = themeResources.getIdentifier(drawableName, "drawable", themePackage);
			if (Machine.isTablet(mContext)) {
				drawable = getDrawableForDensity(themeResources, resourceId);
			} else {
				drawable = themeResources.getDrawable(resourceId);
			}
		} catch (NameNotFoundException e) {
			Log.i("ImageExplorer", "getDrawable() " + drawableName + " NameNotFoundException");
		} catch (NotFoundException e) {
			Log.i("ImageExplorer", "getDrawable() " + drawableName + " NotFoundException");
		} catch (OutOfMemoryError e) {
			Log.i("ImageExplorer", "getDrawable() " + drawableName + " OutOfMemoryError");
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			Log.i("ImageExplorer", "getDrawable()" + drawableName + " has Exception");
		} catch (StackOverflowError e) {
			//			throw new Error("ImageExplorer.getDrawable() " + drawableName + " from themePackage "
			//					+ themePackage + " throws StackOverflowError");
			Log.i("ImageExplorer", "getDrawable()" + drawableName + " has StackOverflowError");
		}
		return drawable;
	}

	public Drawable getDrawable(String themePackage, int resourceId) {
		if (resourceId <= 0) {
			return null;
		}
		if (ThemeManager.DEFAULT_THEME_PACKAGE_3.equals(themePackage)) {
			themePackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		}
		Drawable drawable = null;
		// try
		{
			try {
				Resources themeResources = null;
				if (AppUtils.isAppExist(mContext, themePackage)) {
					themeResources = mContext.getPackageManager().getResourcesForApplication(
							themePackage);
				} else {
					themeResources = ZipResources.getThemeResourcesFromReflect(mContext,
							themePackage);
				}
				if (Machine.isTablet(mContext)) {
					drawable = getDrawableForDensity(themeResources, resourceId);
				} else {
					drawable = themeResources.getDrawable(resourceId);
				}
			} catch (Exception e) {
			}
		}
		return drawable;
	}

	public Drawable getDefaultDrawable(int resourceId) {
		Drawable drawable = null;
		try {
			if (Machine.isTablet(mContext)) {
				drawable = getDrawableForDensity(mContext.getResources(), resourceId);
			} else {
				drawable = mContext.getResources().getDrawable(resourceId);
			}
		} catch (Exception e) {
		}
		return drawable;
	}

	public Drawable getDrawableForDensity(Resources paramResources, int id) {
		Drawable localObject = null;
		if (mCanDrawableForDensity) {
			try {
				if (null == mArrayOfClass) {
					mArrayOfClass = new Class[] { Integer.TYPE, Integer.TYPE };
				}
				if (null == mLocalMethod) {
					mLocalMethod = Resources.class
							.getMethod("getDrawableForDensity", mArrayOfClass);
				}
				localObject = (Drawable) mLocalMethod.invoke(Integer.valueOf(id),
						Integer.valueOf(240));
				return localObject;
			} catch (Throwable localThrowable) {
				mCanDrawableForDensity = false;
			}
		}

		Drawable drawable = paramResources.getDrawable(id);
		if (drawable instanceof NinePatchDrawable || drawable instanceof StateListDrawable) {
			return drawable;
		} else {
			DisplayMetrics localDisplayMetrics = paramResources.getDisplayMetrics();
			float f = localDisplayMetrics.density;
			localDisplayMetrics.density = 1.5F;
			Configuration localConfiguration1 = paramResources.getConfiguration();
			paramResources.updateConfiguration(localConfiguration1, localDisplayMetrics);
			BitmapFactory.Options localOptions = new BitmapFactory.Options();
			localOptions.inTargetDensity = 240;
			Bitmap localBitmap = BitmapFactory.decodeResource(paramResources, id, localOptions);
			localBitmap.setDensity((int) (160 * DrawUtils.sDensity));
			// localBitmap.setDensity(160);
			localDisplayMetrics.density = f;
			Configuration localConfiguration2 = paramResources.getConfiguration();
			paramResources.updateConfiguration(localConfiguration2, localDisplayMetrics);
			localObject = new BitmapDrawable(localBitmap);
			return localObject;
		}
	}

	public Bitmap createBitmap(String themePackage, String drawableName) {
		if (ThemeManager.DEFAULT_THEME_PACKAGE_3.equals(themePackage)) {
			themePackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		}

		// 从主题资源中获取Bitmap
		Resources themeResources = null;
		try {
			if (AppUtils.isAppExist(mContext, themePackage)) {
				themeResources = mContext.getPackageManager().getResourcesForApplication(
						themePackage);
			} else {
				themeResources = ZipResources.getThemeResourcesFromReflect(mContext, themePackage);
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (themeResources != null) {
			int resourceId = themeResources.getIdentifier(drawableName, "drawable", themePackage);
			return BitmapFactory.decodeResource(themeResources, resourceId);
		} else {
			return null;
		}

	}

	// add by 沈金保
	public File saveMyBitmap(String bitName, Bitmap bitmap) throws IOException {

		File file = new File("/sdcard/share_image/");
		if (!file.exists()) {
			file.mkdirs(); // 创建文件夹
			// 防止媒体扫描应用搜索到该目录
			File nomediafile = new File("/sdcard/share_image/.nomedia");
			nomediafile.createNewFile();
		}

		String fileName = "/sdcard/share_image/" + bitName + ".jpg";
		File newfile = new File(fileName);
		if (!newfile.exists()) {
			newfile.createNewFile();
		} else {
			return newfile;
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(newfile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos); // 把数据写入文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return newfile;
	}
}
