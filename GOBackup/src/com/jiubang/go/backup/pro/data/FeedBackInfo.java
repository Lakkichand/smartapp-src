package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import com.jiubang.go.backup.pro.statistics.StatisticsTool;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 意见反馈 邮箱正文 包含邮箱反馈信息中手机基本信息
 * 
 * @author ReyZhang
 */

public class FeedBackInfo {

	private static final String VERSION_NAME_KEY = "VersionName";
	private static final String VERSION_CODE_KEY = "VersionCode";
	private static final String PACKAGE_NAME_KEY = "PackageName";
	private static final String CHANNEL_CODE_KEY = "Channel";
	private static final String FILE_PATH_KEY = "FilePath";
	private static final String PHONE_MODEL_KEY = "PhoneModel";
	private static final String ANDROID_VERSION_KEY = "AndroidVersion";
	private static final String BOARD_KEY = "BOARD";
	private static final String BRAND_KEY = "BRAND";
	private static final String DEVICE_KEY = "DEVICE";
	private static final String DISPLAY_KEY = "DISPLAY";
	private static final String FINGERPRINT_KEY = "FINGERPRINT";
	private static final String HOST_KEY = "HOST";
	private static final String ID_KEY = "ID";
	private static final String MODEL_KEY = "MODEL";
	private static final String PRODUCT_KEY = "PRODUCT";
	private static final String TAGS_KEY = "TAGS";
	private static final String TIME_KEY = "TIME";
	private static final String TYPE_KEY = "TYPE";
	private static final String USER_KEY = "USER";
	private static final String TOTAL_STORAGE_SIZE_KEY = "TotalInternalStorageSize";
	private static final String AVAILABLE_STORAGE_SIZE_KEY = "AvaliableInternalStorageSize";
	private static final String AVALIABLE_MEMORY_SIZE_KEY = "AvalidableMemorySize";
	private static final String LOG_TAG = "GoBackup Freeback error";

	public static String getProperties(Context context) {
		StringBuffer properties = new StringBuffer();
		properties.append("\n");
		properties.append("\n");
		properties.append("\n");
		properties.append("\n");
		properties.append("\n");
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi;
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			if (pi != null) {
				// Application Version
				properties.append(VERSION_NAME_KEY + ":");
				properties.append(pi.versionName != null ? pi.versionName : "not set");
				properties.append("\n");
				properties.append(VERSION_CODE_KEY + ": " + pi.versionCode + "\n");
			} else {
				// Could not retrieve package info...
				properties.append(PACKAGE_NAME_KEY + ":");
				properties.append("Package info unavailable");
				properties.append("\n");
			}

			// Application Package name
			properties.append(PACKAGE_NAME_KEY + ":");
			properties.append(context.getPackageName());
			properties.append("\n");
			
			properties.append(CHANNEL_CODE_KEY + ": " + StatisticsTool.getProductChannelCode(context) + "\n");

			// Device model
			properties.append(PHONE_MODEL_KEY + ":");
			properties.append(android.os.Build.MODEL);
			properties.append("\n");
			// Android version
			properties.append(ANDROID_VERSION_KEY + ":");
			properties.append(android.os.Build.VERSION.RELEASE);
			properties.append("\n");

			// Android build data
			properties.append(BOARD_KEY + ":");
			properties.append(android.os.Build.BOARD);
			properties.append("\n");
			properties.append(BRAND_KEY + ":");
			properties.append(android.os.Build.BRAND);
			properties.append("\n");
			properties.append(DEVICE_KEY + ":");
			properties.append(android.os.Build.DEVICE);
			properties.append("\n");
			properties.append(DISPLAY_KEY + ":");
			properties.append(android.os.Build.DISPLAY);
			properties.append("\n");
			properties.append(FINGERPRINT_KEY + ":");
			properties.append(android.os.Build.FINGERPRINT);
			properties.append("\n");
			properties.append(HOST_KEY + ":");
			properties.append(android.os.Build.HOST);
			properties.append("\n");
			properties.append(ID_KEY + ":");
			properties.append(android.os.Build.ID);
			properties.append("\n");
			properties.append(MODEL_KEY + ":");
			properties.append(android.os.Build.MODEL);
			properties.append("\n");
			properties.append(PRODUCT_KEY + ":");
			properties.append(android.os.Build.PRODUCT);
			properties.append("\n");
			properties.append(TAGS_KEY + ":");
			properties.append(android.os.Build.TAGS);
			properties.append("\n");
			properties.append(TIME_KEY + ":");
			properties.append("" + android.os.Build.TIME);
			properties.append("\n");
			properties.append(TYPE_KEY + ":");
			properties.append(android.os.Build.TYPE);
			properties.append("\n");
			properties.append(USER_KEY + ":");
			properties.append(android.os.Build.USER);
			properties.append("\n");

			properties.append(AVALIABLE_MEMORY_SIZE_KEY + ": " + Formatter.formatFileSize(context, Util.getAvaliableRamSize(context)) + "\n");
			// Device Memory
			properties.append(TOTAL_STORAGE_SIZE_KEY + ":");
			properties.append("" + Formatter.formatFileSize(context, getTotalInternalStorageSize()));
			properties.append("\n");
			properties.append(AVAILABLE_STORAGE_SIZE_KEY + ":");
			properties.append("" + Formatter.formatFileSize(context, getAvailableInternalStorageSize()));
			properties.append("\n");

			// Application file path
			properties.append(FILE_PATH_KEY + ":");
			properties.append(context.getFilesDir().getAbsolutePath());
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error while retrieving crash data", e);
		}
		return properties.toString();
	}

	/**
	 * 内存总共大小
	 * 
	 * @return
	 */
	public static long getTotalInternalStorageSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 可用内存容量
	 */
	public static long getAvailableInternalStorageSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}
}
