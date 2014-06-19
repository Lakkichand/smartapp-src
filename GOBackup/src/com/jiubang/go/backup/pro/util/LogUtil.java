package com.jiubang.go.backup.pro.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.Environment;
import android.util.Log;

import com.jiubang.go.backup.pro.data.BaseEntry;

/**
 * author GoBackup Dev Team
 */
public class LogUtil {
	private static final String LINE_BREAK = "\r\n";
	// private static Properties mProperties = null;
	// private static int index = 0;
	private static StringBuffer s_buffer;

	private static final int M1024 = 1024;
	private static final int M1000 = 1000;
	private static final int M60 = 60;
	private static final float M1000F = 1000f;

	private LogUtil() {

	}

	public static void putEntrySizeProperty(BaseEntry entry) {
		if (entry == null) {
			return;
		}
		// if (mProperties == null) {
		// mProperties = new Properties();
		// }
		// mProperties.setProperty(entry.getDescription() + " Size",
		// formatSize(entry.getSpaceUsage()));
		if (s_buffer == null) {
			s_buffer = new StringBuffer();
		}
		s_buffer.append(entry.getDescription() + " Size = " + formatSize(entry.getSpaceUsage()));
		s_buffer.append(LINE_BREAK);
	}

	public static void putHeapInfoProperty() {
		// if (mProperties == null) {
		// mProperties = new Properties();
		// }
		// mProperties.setProperty("HeapSize " + index ,
		// formatSize(Debug.getNativeHeapSize()));
		// mProperties.setProperty("AllocatedHeapSize " +index,
		// formatSize(Debug.getNativeHeapAllocatedSize()));
		// mProperties.setProperty("FreeHeapSize " + index,
		// formatSize(Debug.getNativeHeapFreeSize()));
		if (s_buffer == null) {
			s_buffer = new StringBuffer();
		}
		s_buffer.append("AllocatedHeapSize = " + formatSize(Debug.getNativeHeapAllocatedSize()));
		s_buffer.append(LINE_BREAK);
		s_buffer.append("FreeHeapSize = " + formatSize(Debug.getNativeHeapFreeSize()));
		s_buffer.append(LINE_BREAK);
		MemoryInfo mi = new MemoryInfo();
		Debug.getMemoryInfo(mi);
		s_buffer.append(String.format("dalvik pss = %dKB", mi.dalvikPss));
		s_buffer.append(LINE_BREAK);
		s_buffer.append(String.format("native heap pss = %dKB", mi.nativePss));
		s_buffer.append(LINE_BREAK);
		s_buffer.append(String.format("other pss = %dKB", mi.otherPss));
		s_buffer.append(LINE_BREAK);
		s_buffer.append(LINE_BREAK);
		// index++;
	}

	public static void putHeapSizeInfo() {
		if (s_buffer == null) {
			s_buffer = new StringBuffer();
		}
		s_buffer.append("HeapSize = " + formatSize(Debug.getNativeHeapSize()));
		s_buffer.append(LINE_BREAK);
	}

	public static void putDescription(String desc) {
		if (desc == null || desc.equals("")) {
			return;
		}
		if (s_buffer == null) {
			s_buffer = new StringBuffer();
		}
		s_buffer.append(desc);
		s_buffer.append(LINE_BREAK);
	}

	public static void putTimeProperty(String description, long value) {
		if (description == null || description.equals("")) {
			return;
		}
		/*
		 * if (mProperties == null) { mProperties = new Properties(); }
		 * mProperties.setProperty(description, formatTime(value));
		 */
		if (s_buffer == null) {
			s_buffer = new StringBuffer();
		}
		s_buffer.append(description + " = " + formatTime(value));
		s_buffer.append(LINE_BREAK);
	}

	public static void saveToFile() {
		// if (mProperties == null || mProperties.isEmpty()) {
		// return;
		// }
		if (s_buffer == null) {
			return;
		}
		String info = s_buffer.toString();
		if (info == null || info.equals("")) {
			return;
		}
		File destFolder = new File(getSavePath());
		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}
		Date date = new Date(System.currentTimeMillis());
		String dateString = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date);
		File destFile = new File(destFolder, "TimeConsumedReport_" + dateString + ".txt");
		if (destFile.exists()) {
			destFile.delete();
		}
		try {
			destFile.createNewFile();
			// mProperties.store(fos, null);
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(destFile));
			try {
				dos.writeUTF(info);
			} finally {
				dos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reset();
		}
	}

	private static void reset() {
		// if (mProperties != null) {
		// mProperties.clear();
		// }
		// mProperties = null;
		// index = 0;
		s_buffer = null;
	}

	private static String getSavePath() {
		return Util.ensureFileSeparator(Environment.getExternalStorageDirectory().getAbsolutePath());
	}

	public static String formatSize(long size) {
		if (size < M1024) {
			return size + "bytes";
		}
		if (size < M1024 * M1024) {
			return String.format("%.2fKB", (double) size / M1024);
		}
		if (size < M1024 * M1024 * M1024) {
			return String.format("%.2fMB", (double) size / (double) (M1024 * M1024));
		}
		return String.format("%.2fGB", (double) size / (double) (M1024 * M1024 * M1024));
	}

	private static String formatTime(long timeInMillis) {
		if (timeInMillis < M1000) {
			return timeInMillis + "ms";
		}
		if (timeInMillis < M1000 * M60) {
			return String.format("%.2f seconds", timeInMillis / M1000F);
		}
		return String.format("%d minutes %d seconds", timeInMillis / (M1000 * M60), timeInMillis
				/ M1000 % M60);
	}

	public static boolean enabled() {
		return false;
	}
	
	public static void d(String msg) {
		Log.d("GoBackup", msg);
	}
}
