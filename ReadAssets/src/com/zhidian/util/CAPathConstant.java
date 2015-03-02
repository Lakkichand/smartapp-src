package com.zhidian.util;

import android.os.Environment;

import com.ta.TAApplication;

/**
 * 
 * 路径常量
 * 
 * @author zhaoyl
 * 
 */
public class CAPathConstant {

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
	 * 普通模式下存放apk的根目录(SD卡)
	 */
	public static final String C_APK_ROOTPATH = SDCARD
			+ "/ZhidianApk/commondownload/";
	/**
	 * 普通模式下存放apk的根目录(cache)
	 */
	public static final String C_APK_ROOTPATH_CACHE = CACHE
			+ "/ZhidianApk/commondownload/";
	/**
	 * 存放普通模式downloadtask的文件地址
	 */
	public static final String C_TASK_PATH = CACHE
			+ "/ZhidianApk/commondownloadtask";
	/**
	 * 临时文件后缀
	 */
	public static final String TEMP_SUFFIX = ".download";

}
