package com.zhidian.wifibox.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.ta.TAApplication;

/**
 * 应用相关的工具类
 * 
 * @author xiedezhi
 * 
 */
public class AppUtils {

	/**
	 * 微信包名
	 */
	public static final String WX_PACKAGE_NAME = "com.tencent.mm";

	/**
	 * 自带浏览器包名
	 */
	public static final String BROWSER_PACKAGE_NAME = "com.android.browser";

	/**
	 * 是否系统应用
	 */
	public static boolean isSystemApp(Context context, String pkg) {
		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取正在运行的应用占用的内存，单位是byte
	 */
	public static Map<String, Long> getRunningApp(Context context) {
		Map<String, Long> map = new HashMap<String, Long>();
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManager
				.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo info : list) {
			int[] pid = new int[] { info.pid };
			Debug.MemoryInfo[] memoryInfo = activityManager
					.getProcessMemoryInfo(pid);
			long memSize = memoryInfo[0].getTotalPss() * 1024L;
			String[] pkgs = info.pkgList;
			long aMemSize = memSize / pkgs.length;
			for (String pkg : pkgs) {
				if (map.containsKey(pkg)) {
					long m = map.get(pkg);
					m = m + aMemSize;
					map.put(pkg, m);
				} else {
					map.put(pkg, aMemSize);
				}
			}
		}
		return map;
	}

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
		List<PackageInfo> packlist = null;
		try {
			packlist = pManager.getInstalledPackages(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (packlist == null) {
			return null;
		}

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
		if (infoList == null) {
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < infoList.size(); i++) {
			PackageInfo info = infoList.get(i);
			String packageName = info.packageName;
			String version = info.versionCode + "";

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

	// 取得版本号
	public static int getVersionCode(Context context, String packName) {
		try {
			PackageInfo manager = context.getPackageManager().getPackageInfo(
					packName, 0);
			return manager.versionCode;
		} catch (NameNotFoundException e) {
			return 0;
		}
	}

	// sim卡是否可读
	public static boolean isCanUseSim() {
		try {
			TelephonyManager mgr = (TelephonyManager) TAApplication
					.getApplication().getSystemService(
							Context.TELEPHONY_SERVICE);

			return TelephonyManager.SIM_STATE_READY == mgr.getSimState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

	/**
	 * 某个服务是否在运行
	 */
	public static boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(100);
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		// Log.e("", className + "  " + isRunning);
		return isRunning;
	}

	/** 是否安装微信 */
	public static boolean isInstallWx(Context mContext, String packageName) {
		try {

			PackageManager manager = mContext.getPackageManager();

			PackageInfo info = manager.getPackageInfo(packageName,
					PackageManager.GET_ACTIVITIES);

			if (info != null) {

				return true;
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 错误信息转换成字符串
	 */
	public static String exceptionTOString(Throwable ex) {

		StringBuffer sb = new StringBuffer();

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);

		return sb.toString();
	}

}
