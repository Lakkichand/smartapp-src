package com.zhidian.wifibox.util;

import java.io.DataOutputStream;
import java.io.File;
import com.zhidian.wifibox.root.RootShell;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class AppUninstaller {

	public static void commonUninstall(Context paramContext, String paramString) {
		if ((paramContext == null) || (TextUtils.isEmpty(paramString))) {
			return;
		}
		Uri localUri = Uri.parse("package:" + paramString);
		Intent localIntent = new Intent("android.intent.action.DELETE");
		localIntent.setData(localUri);
		paramContext.startActivity(localIntent);
	}

	private static void remountSystemApp(RootShell paramRootShell) {
		new RootShell.Command("mount -o rw,remount  /system")
				.execute(paramRootShell);
		if (!new File("/system/app").canWrite()) {
			new RootShell.Command("busybox mount -o remount,rw /system")
					.execute(paramRootShell);
		}
		if (!new File("/system/app").canWrite()) {
			new RootShell.Command("chmod 0777 /system/app")
					.execute(paramRootShell);
		}
	}

	public static boolean silentUninstall(Context paramContext,
			String paramString) {
		if ((paramContext == null) || (TextUtils.isEmpty(paramString))) {
			return false;
		}
		// for (;;) {
		// return false;
		try {
			RootShell localRootShell = RootShell.startShell();
			if (localRootShell != null) {
				String str = new RootShell.Command("pm uninstall "
						+ paramString).execute(localRootShell);
				if ((str != null) && (str.toLowerCase().contains("success"))) {
					return true;
				}
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		// }
		return false;
	}

	/**
	 * 永久卸载系统应用
	 * 
	 * @param sourceDir
	 * @param dataDir
	 * @return
	 */
	public static boolean silentUninstallSystemApp(String sourceDir,
			String dataDir,String packname) {
		do {
			RootShell localRootShell;
			try {
				localRootShell = RootShell.startShell();
				if (localRootShell == null) {
					return false;
				}
			} catch (Exception localException) {
				localException.printStackTrace();
				return false;
			}
			String str3 = "rm " + sourceDir;
			String str4 = "rm -r " + dataDir;
			
			String odex = sourceDir.substring(0, sourceDir.lastIndexOf("."));
			String odexStr = odex  + ".odex";
			remountSystemApp(localRootShell);
			new RootShell.Command(str3).execute(localRootShell);
			
			if (new File(odexStr).exists()) {				
				new RootShell.Command("rm " + odexStr).execute(localRootShell);
			}
			
			new RootShell.Command(str4).execute(localRootShell);
			
		} while (new File(sourceDir).exists());
		return true;
	}

	/*
	 * 对要卸载的apk赋予权限
	 */
	public static void chmodApk(String busybox) {
		try {

			Process process = null;
			DataOutputStream os = null;

			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(busybox);
			os.flush();

//			os.writeBytes(chmod);
//			os.flush();

			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * 卸载apk
	 */
	public static void uninstallApk(String uninstallapk) {
		try {

			Process process = null;
			DataOutputStream os = null;
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(uninstallapk);
			os.flush();

			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
