package com.smartapp.rootuninstaller.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class Util {

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * Calculates the free memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Number of bytes available.
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * Calculates the total memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Total number of bytes.
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
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

		if (result != "") {
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
	 * 不弹框 这是一个较耗时的接口，注意不要在循环中使用
	 */
	public static boolean isRootRom(Context context) {
		return findLinuxCmd("su");
	}

	public static boolean findLinuxCmd(final String cmd) {
		boolean ret = false;
		// LinuxShell ls = new LinuxShell();
		// String[] paths = ls.querySystemPath(); //系统path
		String[] paths = getSystemPath();
		if (paths == null) {
			// Error
			return false;
		}

		for (String path : paths) {
			if (!path.endsWith(File.separator)) {
				path += File.separator;
			}
			File desFile = new File(path + cmd);
			if (desFile.exists()) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public static String[] getSystemPath() {
		String[] result = null;
		String path = System.getenv("PATH");
		if (path == null) {
			return null;
		}

		if (path.endsWith("\n")) {
			path = path.substring(0, path.length() - 2);
		}
		result = path.split(":");
		return result;
	}


}
