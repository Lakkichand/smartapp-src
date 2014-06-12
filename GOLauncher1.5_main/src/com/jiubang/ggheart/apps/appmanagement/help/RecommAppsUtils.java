package com.jiubang.ggheart.apps.appmanagement.help;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;

/**
 * 应用推荐模块，工具类
 * 
 * @author zhoujun
 * 
 */
public class RecommAppsUtils {

	// 应用推荐默认显示系统图标大小
	private static final int APP_ICON_WIDTH = 72;
	private static final int APP_ICON_HEIGHT = 72;

	/**
	 * 得到文件名(包括后缀)
	 * 
	 * @param fileName
	 * @return
	 */
	public static String formatFileName(String fileName) {
		if (fileName == null) {
			return null;
		}
		if (fileName.lastIndexOf(File.separator) >= 0) {
			return fileName.substring(fileName.lastIndexOf(File.separator) + 1);
		}
		return null;
	}

	/**
	 * 得到文件名(不包括后缀)
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getSimpleName(String fileName) {
		String str = fileName;
		if (str == null) {
			return null;
		}
		if (str.lastIndexOf(File.separator) >= 0) {
			str = str.substring(str.lastIndexOf(File.separator) + 1);
		}
		if (str.indexOf(".") >= 0) {
			str = str.substring(0, str.lastIndexOf("."));
		}

		return str;
	}

	public static BitmapDrawable loadAppIcon(String iconPath, Context context) {
		try {
			Bitmap bitmap = null;
			Bitmap newBitmap = null;
			if (iconPath != null && !"".equals(iconPath)) {
				bitmap = BitmapFactory.decodeFile(iconPath);
			}

			if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
				Log.e("RecommAppsUtils", iconPath + " is not exist");
				return null;
			}
			int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
			float scale = densityDpi / GoStorePublicDefine.STANDARD_DENSITYDPI;
			newBitmap = zoomBitmap(bitmap, (int) (APP_ICON_WIDTH * scale),
					(int) (APP_ICON_HEIGHT * scale));
			// Log.d("RecommAppsUtils", "densityDpi value : " + densityDpi);
			return new BitmapDrawable(context.getResources(), newBitmap);
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
		}
		return null;
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}

	/**
	 * 根据包名判断，是否已经安装
	 * 
	 * @param context
	 * @param packageName
	 * @param versionName
	 * @return
	 */
	public static boolean isInstalled(Context context, String packageName, String versionName) {
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(packageName, 0);
			if (info != null) {
				if (versionName == null || "".equals(versionName)
						|| versionName.equals(info.versionName)
						|| "Varies with device".equals(info.versionName)) {
					return true;
				}
			}
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取设备分辨率
	 * 
	 * @param context
	 * @return
	 */
	public static String getDisplay(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		return width + "*" + height;
	}
}
