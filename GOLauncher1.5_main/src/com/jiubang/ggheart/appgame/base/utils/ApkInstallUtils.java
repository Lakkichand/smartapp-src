/*
 * 文 件 名:  ApkInstallUtils.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-10-8
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.utils;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.jiubang.ggheart.appgame.base.component.AppInstallActivity;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreFileUtil;
import com.jiubang.ggheart.data.statistics.GoStoreAppStatistics;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-10-8]
 */
public class ApkInstallUtils {

	/**
	 * <br>功能简述:安装apk文件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param file
	 */
	public static void installApk(File file) {
		Context context = GOLauncherApp.getContext();
		if (context != null && file != null && file.exists()) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
			intent.setClass(context, AppInstallActivity.class);
			context.startActivity(intent);
		}
	}

	/**
	 * <br>功能简述:安装apk文件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param filePath
	 */
	public static void installApk(String filePath) {
		//判断文件路径指向的文件是不是apk文件
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		String[] token = file.getName().split("\\.");
		String pf = token[token.length - 1];
		if (!pf.equals("apk")) {
			return;
		}
		Context context = GOLauncherApp.getContext();
		if (context != null && file != null && file.exists()) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
			intent.setClass(context, AppInstallActivity.class);
			context.startActivity(intent);
		}
	}

	/**
	 * <br>功能简述:GO精品用于统计的安装方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param pkgName
	 * @param verCode
	 * @param appid
	 * @param classify
	 */
	public static void installApk(String pkgName, int verCode, String appid, String classify) {
		Context context = GOLauncherApp.getContext();
		if (context != null) {
			File file = new File(GoStoreFileUtil.getFilePath(context, pkgName, verCode));
			if (!file.exists()) {
				return;
			}
			String[] token = file.getName().split("\\.");
			String pf = token[token.length - 1];
			if (!pf.equals("apk")) {
				return;
			}

			if (file != null && file.exists()) {
				GoStoreAppStatistics.getInstance(context).sendMonitorAppInstallMessage(pkgName,
						appid, classify);
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
				intent.setClass(context, AppInstallActivity.class);
				context.startActivity(intent);
			}
		}
	}
}
