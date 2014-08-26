package com.zhidian.wifibox.download;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ta.TAApplication;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.util.Setting;

/**
 * 应用安装卸载广播接收器
 * 
 * @author xiedezhi
 * 
 */
public class AppActionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		// 判断是否安装完成删除软件包
		Setting setting = new Setting(context);
		if (!setting.getBoolean(Setting.DELETE_AFTER_INSTALL)) {
			return;
		}
		// 是否安装升级应用，如果是，去sdcard或者cache找相应的安装包并删除
		String action = intent.getAction();
		if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
				|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			final String packageName = intent.getData().getSchemeSpecificPart();
			new Thread("AppActionReceiver") {
				public void run() {
					// 普通模式sdcard
					File[] files = (new File(PathConstant.C_APK_ROOTPATH))
							.listFiles();
					if (files != null && files.length > 0) {
						for (File file : files) {
							String packName = DownloadUtil.getApkFilePackName(
									TAApplication.getApplication(),
									file.getAbsolutePath());
							if (packName.equals(packageName)) {
								file.delete();
								Intent intent = new Intent("DELETE_APK");
								intent.putExtra("packName", packName);
								context.sendBroadcast(intent);
							}
						}
					}
					// 普通模式cache
					files = (new File(PathConstant.C_APK_ROOTPATH_CACHE))
							.listFiles();
					if (files != null && files.length > 0) {
						for (File file : files) {
							String packName = DownloadUtil.getApkFilePackName(
									TAApplication.getApplication(),
									file.getAbsolutePath());
							if (packName.equals(packageName)) {
								file.delete();
							}
						}
					}
				};
			}.start();
		}
	}
}
