package com.jiubang.go.backup.pro.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.Environment;

/**
 * author GoBackup Dev Team
 */
public class DebugLog {
	private static final String DEBUG_FILE = "debug.txt";
	private static final String LINE_BREAK = "\r\n";
	private static StringBuffer s_BUFFER;

	private static final int M1024 = 1024;
	private static final int M1000 = 1000;
	private static final int M60 = 60;
	private static final float M1000F = 1000f;

	private static final boolean DEBUG = true;

	private DebugLog() {

	}

	public static File getDebugFile() {
		return new File(getSavePath(), DEBUG_FILE);
	}

	public static void putHeapInfoProperty() {
		if (s_BUFFER == null) {
			s_BUFFER = new StringBuffer();
		}
		s_BUFFER.append("AllocatedHeapSize = " + formatSize(Debug.getNativeHeapAllocatedSize()));
		s_BUFFER.append(LINE_BREAK);
		s_BUFFER.append("FreeHeapSize = " + formatSize(Debug.getNativeHeapFreeSize()));
		s_BUFFER.append(LINE_BREAK);
		MemoryInfo mi = new MemoryInfo();
		Debug.getMemoryInfo(mi);
		s_BUFFER.append(String.format("dalvik pss = %dKB", mi.dalvikPss));
		s_BUFFER.append(LINE_BREAK);
		s_BUFFER.append(String.format("native heap pss = %dKB", mi.nativePss));
		s_BUFFER.append(LINE_BREAK);
		s_BUFFER.append(String.format("other pss = %dKB", mi.otherPss));
		s_BUFFER.append(LINE_BREAK);
		s_BUFFER.append(LINE_BREAK);
	}

	public static void putHeapSizeInfo() {
		if (s_BUFFER == null) {
			s_BUFFER = new StringBuffer();
		}
		s_BUFFER.append("HeapSize = " + formatSize(Debug.getNativeHeapSize()));
		s_BUFFER.append(LINE_BREAK);
	}

	public static void putDescription(String desc) {
		if (desc == null || desc.equals("")) {
			return;
		}
		if (s_BUFFER == null) {
			s_BUFFER = new StringBuffer();
		}
		s_BUFFER.append(desc);
		s_BUFFER.append(LINE_BREAK);
	}

	public static void putTimeProperty(String description, long value) {
		if (description == null || description.equals("")) {
			return;
		}
		/*
		 * if (mProperties == null) { mProperties = new Properties(); }
		 * mProperties.setProperty(description, formatTime(value));
		 */
		if (s_BUFFER == null) {
			s_BUFFER = new StringBuffer();
		}
		s_BUFFER.append(description + " = " + formatTime(value));
		s_BUFFER.append(LINE_BREAK);
	}

	public static void saveToFile(boolean append) {
		if (s_BUFFER == null) {
			return;
		}
		String info = s_BUFFER.toString();
		if (info == null || info.equals("")) {
			return;
		}
		File destFolder = new File(getSavePath());
		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}

		File destFile = new File(destFolder, DEBUG_FILE);
		if (destFile.exists() && destFile.length() > M1024 * M1024) {
			append = false;
		}
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(destFile, append));
			dos.writeUTF(info);
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reset();
		}
	}

	public static void appendDebugFile(File destFile) {
		if (destFile == null || !destFile.exists()) {
			return;
		}

		String info = loadDebugFile();
		if (info == null) {
			return;
		}

		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(destFile, true));
			dos.writeUTF(LINE_BREAK);
			dos.writeUTF(info);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			reset();
		}
	}

	private static String loadDebugFile() {
		File debugFile = getDebugFile();
		if (debugFile == null || !debugFile.exists()) {
			return null;
		}

		StringBuffer result = null;
		DataInputStream dis = null;
		try {
			result = new StringBuffer();
			dis = new DataInputStream(new FileInputStream(debugFile));
			while (dis.available() > 0) {
				result.append(dis.readUTF());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result != null ? result.toString() : null;
	}

	private static void reset() {
		// if (mProperties != null) {
		// mProperties.clear();
		// }
		// mProperties = null;
		// index = 0;
		s_BUFFER = null;
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

	public static String formatTime(long timeInMillis) {
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
		return DEBUG;
	}

}
