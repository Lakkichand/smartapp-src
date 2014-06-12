package com.go.util.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import com.go.util.AppUtils;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.gowidget.GoWidgetConstant;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * @author jiangxuwen
 *
 */
// CHECKSTYLE:OFF
public class Machine {
	public static int LEPHONE_ICON_SIZE = 72;
	private static boolean sCheckTablet = false;
	private static boolean sIsTablet = false;

	// 硬件加速
	public static int LAYER_TYPE_NONE = 0x00000000;
	public static int LAYER_TYPE_SOFTWARE = 0x00000001;
	public static int LAYER_TYPE_HARDWARE = 0x00000002;
	public static boolean IS_HONEYCOMB = Build.VERSION.SDK_INT >= 11;
	public static boolean IS_HONEYCOMB_MR1 = Build.VERSION.SDK_INT >= 12;
	public static boolean IS_ICS = Build.VERSION.SDK_INT >= 14;
	public static boolean IS_ICS_MR1 = Build.VERSION.SDK_INT >= 15 && Build.VERSION.RELEASE.equals("4.0.4");// HTC oneX 4.0.4系统
	public static boolean IS_JELLY_BEAN = Build.VERSION.SDK_INT >= 16;
	public static boolean sLevelUnder3 = Build.VERSION.SDK_INT < 11;// 版本小于3.0
	private static Method sAcceleratedMethod = null;

	private final static String LEPHONEMODEL[] = { "3GW100", "3GW101", "3GC100", "3GC101" };
	private final static String MEIZUBOARD[] = { "m9", "M9", "mx", "MX" };
	private final static String M9BOARD[] = { "m9", "M9" };
	private final static String ONE_X_MODEL[] = { "HTC One X", "HTC One S" };

	public static boolean isLephone() {
		final String model = android.os.Build.MODEL;
		if (model == null) {
			return false;
		}
		final int size = LEPHONEMODEL.length;
		for (int i = 0; i < size; i++) {
			if (model.equals(LEPHONEMODEL[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isM9() {
		return isPhone(M9BOARD);
	}
	public static boolean isMeizu() {
		return isPhone(MEIZUBOARD);
	}
	public static boolean isONE_X() {
		return checkModel(ONE_X_MODEL);
	}

	private static boolean isPhone(String[] boards) {
		final String board = android.os.Build.BOARD;
		if (board == null) {
			return false;
		}
		final int size = boards.length;
		for (int i = 0; i < size; i++) {
			if (board.equals(boards[i])) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkModel(String[] models) {
		final String board = android.os.Build.MODEL;
		if (board == null) {
			return false;
		}
		final int size = models.length;
		for (int i = 0; i < size; i++) {
			if (board.equals(models[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否为非中国大陆用户 针对GOStore而起的判断，必须要在拿到SIM的情况下，而且移动国家号不属于中国大陆
	 * 
	 * @return
	 */
	public static boolean isNotCnUser() {
		return !isCnUser(GOLauncherApp.getContext());
	}

	/**
	 * 判断是否中国大陆用户 该接口不推荐使用，请使用{@link #isCnUser(Context)}代替
	 * 
	 * @return
	 */
	@Deprecated
	public static boolean isCnUser() {
		return isCnUser(GOLauncherApp.getContext());
	}

	/**
	 * 因为主题2.0新起进程，无法获取GoLauncher.getContext()， 所以重载此方法，以便主题2.0调用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isCnUser(Context context) {
		boolean result = false;

		if (context != null) {
			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;
			String simOperator = manager.getSimOperator();

			if (simCardUnable || TextUtils.isEmpty(simOperator)) {
				// 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理
				// 获取当前国家或地区，如果当前手机设置为简体中文-中国，则使用此方法返回CN
				String curCountry = Locale.getDefault().getCountry();
				if (curCountry != null && curCountry.contains("CN")) {
					// 如果获取的国家信息是CN，则返回TRUE
					result = true;
				} else {
					// 如果获取不到国家信息，或者国家信息不是CN
					result = false;
				}
			} else if (simOperator.startsWith("460")) {
				// 如果有SIM卡，并且获取到simOperator信息。
				/**
				 * 中国大陆的前5位是(46000) 中国移动：46000、46002 中国联通：46001 中国电信：46003
				 */
				result = true;
			}
		}

		return result;
	}

	/**
	 * 是否国外用户或国内的有电子市场的用户，true for yes，or false for no
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isOverSeaOrExistMarket(Context context) {
		boolean result = false;
		boolean isCnUser = isCnUser(context);
		// 全部的国外用户 + 有电子市场的国内用户
		if (isCnUser) {
			// 是国内用户，则进一步判断是否有电子市场
			result = AppUtils.isMarketExist(context);
		} else {
			// 是国外用户
			result = true;
		}
		return result;
	}

	/**
	 * 判断当前运营商是否在指定数组内
	 * 
	 * @param areaArray
	 * @return
	 */
	public static boolean isLocalAreaCodeMatch(String[] areaArray) {
		if (null == areaArray || areaArray.length == 0) {
			return false;
		} else if (GoWidgetConstant.GOWIDGET_ALL_AREA.equals(areaArray[0])) {
			// -1表示全地区
			return true;
		}

		// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
		TelephonyManager manager = (TelephonyManager) GOLauncherApp.getContext().getSystemService(
				Context.TELEPHONY_SERVICE);
		String simOperator = manager.getSimOperator();
		if (null == simOperator) {
			return false;
		}

		boolean ret = false;
		for (String areastring : areaArray) {
			if (null == areastring || areastring.length() == 0
					|| areastring.length() > simOperator.length()) {
				continue;
			} else {
				String subString = simOperator.substring(0, areastring.length());
				if (subString.equals(areastring)) {
					ret = true;
					break;
				} else {
					continue;
				}
			}
		}
		return ret;
	}

	// 根据系统版本号判断时候为华为2.2 or 2.2.1, Y 则catch
	public static boolean isHuaweiAndOS2_2_1() {
		boolean resault = false;
		String androidVersion = Build.VERSION.RELEASE;// os版本号
		String brand = Build.BRAND;// 商标
		if (androidVersion == null || brand == null) {
			return resault;
		}
		if (brand.equalsIgnoreCase("Huawei")
				&& (androidVersion.equals("2.2") || androidVersion.equals("2.2.2")
						|| androidVersion.equals("2.2.1") || androidVersion.equals("2.2.0"))) {
			resault = true;
		}
		return resault;
	}

	// 判断当前设备是否为平板
	private static boolean isPad() {
		if (DrawUtils.sDensity >= 1.5 || DrawUtils.sDensity <= 0) {
			return false;
		}
		if (DrawUtils.sWidthPixels < DrawUtils.sHeightPixels) {
			if (DrawUtils.sWidthPixels > 480 && DrawUtils.sHeightPixels > 800) {
				return true;
			}
		} else {
			if (DrawUtils.sWidthPixels > 800 && DrawUtils.sHeightPixels > 480) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTablet(Context context) {
		if (sCheckTablet == true) {
			return sIsTablet;
		}
		sCheckTablet = true;
		sIsTablet = isPad();
		// if(null == context || DrawUtils.sDensity >= 1.5)
		// {
		// sIsTablet = isPad();
		// }
		// else
		// {
		// sIsTablet = (context.getResources().getConfiguration().screenLayout &
		// Configuration.SCREENLAYOUT_SIZE_MASK) >=
		// Configuration.SCREENLAYOUT_SIZE_LARGE;
		// }
		//
		return sIsTablet;
	}

	/**
	 * 判断当前网络是否可以使用
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isNetworkOK(Context context) {
		boolean result = false;
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo networkInfo = cm.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * 设置硬件加速
	 * 
	 * @param view
	 * @param accelerate
	 */
	public static void setHardwareAccelerated(View view, int mode) {
		if (sLevelUnder3) {
			return;
		}
		try {
			if (null == sAcceleratedMethod) {
				sAcceleratedMethod = View.class.getMethod("setLayerType", new Class[] {
						Integer.TYPE, Paint.class });
			}
			sAcceleratedMethod.invoke(view, new Object[] { Integer.valueOf(mode), null });
		} catch (Throwable e) {
			sLevelUnder3 = true;
		}
	}

	public static boolean isIceCreamSandwichOrHigherSdk() {
		return Build.VERSION.SDK_INT >= 14;
	}

	/**
	 * 获取Android中的Linux内核版本号
	 * 
	 */
	public static String getLinuxKernel() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cat /proc/version");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (null == process) {
			return null;
		}

		// get the output line
		InputStream outs = process.getInputStream();
		InputStreamReader isrout = new InputStreamReader(outs);
		BufferedReader brout = new BufferedReader(isrout, 8 * 1024);
		String result = "";
		String line;

		// get the whole standard output string
		try {
			while ((line = brout.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (result.equals("")) {
			String Keyword = "version ";
			int index = result.indexOf(Keyword);
			line = result.substring(index + Keyword.length());
			if (null != line) {
				index = line.indexOf(" ");
				return line.substring(0, index);
			}
		}
		return null;
	}

	/**
	 * 获得手机内存的可用空间大小
	 * 
	 * @author kingyang
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获得手机内存的总空间大小
	 * 
	 * @author kingyang
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获得手机sdcard的可用空间大小
	 * 
	 * @author kingyang
	 */
	public static long getAvailableExternalMemorySize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获得手机sdcard的总空间大小
	 * 
	 * @author kingyang
	 */
	public static long getTotalExternalMemorySize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 是否存在SDCard
	 * 
	 * @author chenguanyu
	 * @return
	 */
	public static boolean isSDCardExist() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取当前的语言
	 * 
	 * @author zhoujun
	 * @param context
	 * @return
	 */
	public static String getLanguage(Context context) {
		String language = context.getResources().getConfiguration().locale.getLanguage();
		return language;
	}

	/**
	 * 判断应用软件是否运行在前台
	 * 
	 * @param context
	 * @param packageName
	 *            应用软件的包名
	 * @return
	 */
	public static boolean isTopActivity(Context context, String packageName) {
		try {
			ActivityManager activityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
			if (tasksInfo.size() > 0) {
				// 应用程序位于堆栈的顶层
				if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}
	/**
	 * <br>功能简述:获取Android ID的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static String getAndroidId(){
		String androidId = null;
		Context context = GOLauncherApp.getApplication();
		if(context != null){
			androidId = Settings.Secure.getString(context.getContentResolver(),
	                Settings.Secure.ANDROID_ID);
		}
		return androidId;
	}
	
	/**
	 * 获取国家
	 * @param context
	 * @return
	 */
	public static String getCountry(Context context) {
		String ret = null;

		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso().toLowerCase();
			}
			if (ret == null || ret.equals("")) {
				ret = Locale.getDefault().getCountry().toLowerCase();
			}

		} catch (Throwable e) {
			//			 e.printStackTrace();
		}
		return ret;
	}
}
