package com.zhidian.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 安装包工具类
 * 
 * @author zhaoyl
 * 
 */
public class APKUtil {

//	static {
//		System.loadLibrary("zip");
//		System.loadLibrary("readres");
//	}
	
	/**
	 * 获取apk包的版本号
	 * @param context
	 * @param path
	 * @return
	 */
	public static int getVersionCode(Context context, String path) {

		if (path.toLowerCase().endsWith(".apk")) {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(path,
					PackageManager.GET_ACTIVITIES);
			if (packageInfo == null) {
				return -1;
			}
			int versionCode = packageInfo.versionCode;
			return versionCode;
		}
		return -1;

	}
	
	/**
	 * 获取APK包中assets中的文件
	 * @param ass
	 * @param filename
	 */
//	public static native String readFromAssetsLibzip(String ass,String filename);

}
