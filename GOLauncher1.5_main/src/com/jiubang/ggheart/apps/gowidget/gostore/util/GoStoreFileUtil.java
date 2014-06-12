package com.jiubang.ggheart.apps.gowidget.gostore.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.theme.broadcastReceiver.MyThemeReceiver;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>类描述:精品内文件相关工具类
 * <br>功能详细描述:打开文件，打开APK或ZIP名等功能
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
public class GoStoreFileUtil {

	public static void openApkFile(Context context, String pkgName, int verCode, String appid,
			String classify) {
		ApkInstallUtils.installApk(pkgName, verCode, appid, classify);
	}

	public static boolean isFileExists(Context context, String pkgName, int verCode) {
		String saveFilePath = getFilePath(context, pkgName, verCode);
		File file = new File(saveFilePath);
		if (file.exists()) {
			return true;
		}
		return false;
	}

	public static String getFilePath(Context context, String pkgName, int verCode) {
		return GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + pkgName + "_" + verCode + ".apk";
	}

	/**
	 * <br>功能简述:应用Zip包主题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param packageName
	 */
	public static void applyZipTheme(Context context, String packageName) {
		Intent intentGoLauncher = new Intent();
		intentGoLauncher.setClass(context, GoLauncher.class);
		context.startActivity(intentGoLauncher);
		Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
		intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, MyThemeReceiver.CHANGE_THEME);
		intent.putExtra(MyThemeReceiver.PKGNAME_STRING, packageName);
		context.sendBroadcast(intent);
	}
}
