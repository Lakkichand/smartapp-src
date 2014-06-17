package com.smartapp.autostartmanager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import com.ta.TAApplication;

/**
 * 应用相关的工具类
 * 
 * @author xiedezhi
 * 
 */
public class AppUtils {

	/**
	 * 检查是安装某包
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @return
	 */
	public static boolean isAppExist(final Context context,
			final String packageName) {
		if (context == null || packageName == null) {
			return false;
		}
		boolean result = false;
		try {
			context.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_SHARED_LIBRARY_FILES);
			result = true;
		} catch (NameNotFoundException e) {
			result = false;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/***************************
	 * 获取手机内所有应用
	 ***************************/
	public static List<PackageInfo> getAllApps(Context context) {

		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager pManager = context.getPackageManager();
		List<PackageInfo> packlist = pManager.getInstalledPackages(0);
		for (int i = 0; i < packlist.size(); i++) {
			PackageInfo pak = (PackageInfo) packlist.get(i);

			// 判断是否为非系统预装的应用程序
			// 这里还可以添加系统自带的，这里就先不添加了，如果有需要可以自己添加
			// if()里的值如果<=0则为自己装的程序，否则为系统工程自带
			if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
				// 添加自己已经安装的应用程序
				apps.add(pak);
			}

		}
		return apps;
	}

	/***************************
	 * 获取手机内所有应用String packageName 包名 version 版本号
	 ***************************/
	public static String getAllAppsString(Context context) {
		List<PackageInfo> infoList = getAllApps(TAApplication.getApplication());
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < infoList.size(); i++) {
			PackageInfo info = infoList.get(i);
			String packageName = info.packageName;
			String version = info.versionName;

			if (i == infoList.size() - 1) {
				stringBuffer.append(packageName).append("|").append(version);
			} else {
				stringBuffer.append(packageName).append("|").append(version)
						.append(",");
			}
		}

		return stringBuffer.toString();
	}

	/***************************
	 * 判断手机是否有Root权限 如有权限，自动弹出授权对话框。
	 * 
	 * @return true表示有Root
	 ***************************/
	public static boolean isRoot() {
		try {
			if (Runtime.getRuntime().exec("su").getOutputStream() != null) {
				// 有root权限

				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断手机是否ROOT
	 */
	public static boolean isRoot2() {

		boolean root = false;

		try {
			if ((!new File("/system/bin/su").exists())
					&& (!new File("/system/xbin/su").exists())) {
				root = false;
			} else {
				root = true;
			}

		} catch (Exception e) {
		}

		return root;
	}

	// 取得版本号
	public static String getVersion(Context context, String packName) {
		try {
			PackageInfo manager = context.getPackageManager().getPackageInfo(
					packName, 0);
			return manager.versionName;
		} catch (NameNotFoundException e) {
			return "Unknown";
		}
	}

	/**
	 * 判断是否有SD卡
	 * 
	 * @return
	 */
	public static String isHaveSdCard() {
		String result = "0";
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String cdcard_path = Environment.getExternalStorageDirectory()
					.getParent() + "/sdcard2";
			File file = new File(cdcard_path);
			if (file.exists()) {
				// 外置SD 卡
				result = "1";
			} else {
				// 内置SD卡
				result = "1";
			}
		} else {
			// 没有安装SD卡！
			result = "0";
		}
		return result;

	}

	/**
	 * 验证输入的邮箱格式是否符合
	 * 
	 * @param email
	 * @return 是否合法
	 */
	public static boolean emailFormat(String email) {
		Pattern p = Pattern
				.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
		Matcher m = p.matcher(email);
		return m.find();
	}

	public static String readAssetsFile(Context ctx, String fileName) {
		try {
			InputStream is = ctx.getAssets().open(fileName);

			int size = is.available();

			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			if (buffer != null) {
				return (new String(buffer, "UTF-8")).trim();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取应用信息
	 */
	public static ApplicationInfo getAppInfo(Context ctx, String pkgName) {
		try {
			PackageInfo info = ctx.getPackageManager().getPackageInfo(pkgName,
					0);
			return info.applicationInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static final String SCHEME = "package";
	/**
	 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
	 */
	private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
	/**
	 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
	 */
	private static final String APP_PKG_NAME_22 = "pkg";
	/**
	 * InstalledAppDetails所在包名
	 */
	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
	/**
	 * InstalledAppDetails类名
	 */
	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

	/**
	 * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息。 对于Android 2.3（Api Level
	 * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）。
	 * 
	 * @param context
	 * 
	 * @param packageName
	 *            应用程序的包名
	 */
	public static void showInstalledAppDetails(Context context,
			String packageName) {
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) {
			// 2.3（ApiLevel 9）以上，使用SDK提供的接口
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts(SCHEME, packageName, null);
			intent.setData(uri);
		} else {
			// 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
			// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
					: APP_PKG_NAME_21);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(APP_DETAILS_PACKAGE_NAME,
					APP_DETAILS_CLASS_NAME);
			intent.putExtra(appPkgName, packageName);
		}
		context.startActivity(intent);
	}

	public static Method getMethod(PackageManager packageManager,
			String methodName) {
		for (Method method : packageManager.getClass().getMethods()) {
			if (method.getName().equals(methodName))
				return method;
		}
		return null;
	}

	public static void invokeMethod(PackageManager packageManager,
			String method, Object... args) {
		try {
			getMethod(packageManager, method).invoke(packageManager, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
