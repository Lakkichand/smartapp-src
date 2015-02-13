package com.zhidian.wifibox.util;

import android.os.Environment;

import com.ta.TAApplication;

/**
 * 
 * 路径常量
 * 
 * @author xiedezhi
 * 
 */
public class PathConstant {

	/**
	 * sdcard路径
	 */
	public final static String SDCARD = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	/**
	 * 缓存文件路径
	 */
	public final static String CACHE = TAApplication.getApplication()
			.getCacheDir().getAbsolutePath();
	/**
	 * 存放图标的文件目录
	 */
	public static final String ICON_ROOT_PATH = SDCARD + "/MIBAO/ICONS_/";
	/**
	 * 普通模式下存放apk的根目录(SD卡)
	 */
	public static final String C_APK_ROOTPATH = SDCARD
			+ "/MIBAO/commondownload/";
	/**
	 * 普通模式下存放apk的根目录(cache)
	 */
	public static final String C_APK_ROOTPATH_CACHE = CACHE
			+ "/MIBAO/commondownload/";
	/**
	 * 存放普通模式downloadtask的文件地址
	 */
	public static final String C_TASK_PATH = CACHE
			+ "/MIBAO/commondownloadtask";
	/**
	 * 临时文件后缀
	 */
	public static final String TEMP_SUFFIX = ".download";
	/**
	 * 崩溃日志存放目录
	 */
	public static final String CRASH_LOG_PATH = CACHE + "/MIBAO/crashlog";
	/**
	 * 极速模式门店广告路径
	 */
	public static final String AD_PATH = SDCARD + "/MIBAO/adimage/";
	/**
	 * 极速模式门店广告路径
	 */
	public static final String AD_PATH_CACHE = CACHE + "/MIBAO/adimage/";

}
