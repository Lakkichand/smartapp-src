package com.smartapp.rootuninstaller.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.smartapp.rootuninstaller.ListDataBean;

/**
 * 应用卸载工具类
 * 
 * @author xiedezhi
 * 
 */
public class AppUninstaller {

	/**
	 * 调用系统的卸载程序
	 */
	public static void commonUninstall(Context context, String pkgName) {
		if (context == null || TextUtils.isEmpty(pkgName)) {
			return;
		}
		// 通过程序的包名创建URL
		Uri packageURI = Uri.parse("package:" + pkgName);
		// 创建Intent意图
		Intent intent = new Intent(Intent.ACTION_DELETE);
		// 设置Uri
		intent.setData(packageURI);
		// 卸载程序
		context.startActivity(intent);
	}

	/**
	 * 静默卸载普通程序 pm uninstall com.immomo.momo
	 */
	public static boolean silentUninstall(Context context, String pkgName) {
		if (context == null || TextUtils.isEmpty(pkgName)) {
			return false;
		}
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (rootShell == null) {
			return false;
		}
		String cmd = "pm uninstall " + pkgName;
		String result = new RootShell.Command(cmd).execute(rootShell);
		// 判断result
		if (result != null && result.toLowerCase().contains("success")) {
			return true;
		}
		return false;
	}

	/**
	 * 更改/system权限
	 */
	private static void remountSystemApp(RootShell shell) {
		new RootShell.Command("mount -o rw,remount  /system").execute(shell);
		if (!new File("/system/app").canWrite()) {
			new RootShell.Command("busybox mount -o remount,rw /system")
					.execute(shell);
		}
		if (!new File("/system/app").canWrite()) {
			new RootShell.Command("chmod 0777 /system/app").execute(shell);
		}
	}

	/**
	 * 静默删除系统软件
	 */
	public static boolean silentUninstallSystemApp(Context context,
			ListDataBean bean) {
		String apkfile = bean.mInfo.applicationInfo.sourceDir;
		String datafile = bean.mInfo.applicationInfo.dataDir;
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (rootShell == null) {
			return false;
		}
		String uninstallapk = "rm " + apkfile;
		String rmdata = "rm -r " + datafile;
		remountSystemApp(rootShell);
		String result = "";
		result = new RootShell.Command(uninstallapk).execute(rootShell);
		result = new RootShell.Command(rmdata).execute(rootShell);
		File sysFile = new File(apkfile);
		// 判断是否成功
		if (!sysFile.exists()) {
			return true;
		} else {
			return false;
		}

	}

}
