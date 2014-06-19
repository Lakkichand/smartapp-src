package com.jiubang.go.backup.pro.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

/**
 * 日记打印类
 * 
 * @author wencan
 */
public class Logger {
	// 日志文件总开关
	private static final Boolean SWITCH = true;
	// 日记写入缓存开关
	private static final Boolean WRITE_TO_CACHE = true;
	// 日志写入文件开关
	private static final Boolean WRITE_TO_FILE = true;
	// 日记文件名
	private static final String LOG_FILE_NAME = Environment.getExternalStorageDirectory().getPath()
			+ "/gobackup_debug.txt";
	// 日记debug文件最大1M
	private static final int MAX_FILE_SIZE = 512 * 1024;
	// 时间格式化格式
	private static final SimpleDateFormat TIME_FORMATER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// 缓存长度
	private static final int MAX_CACHE_SIZE = 2048;
	// 字符串缓存
	private static StringBuffer sBuffer = new StringBuffer();
	
	public static File getLoggerFile() {
		return new File(LOG_FILE_NAME);
	}

	public static void w(String tag, Object msg) {
		log(tag, msg.toString(), 'w');
	}

	public static void e(String tag, Object msg) {
		log(tag, msg.toString(), 'e');
	}

	public static void d(String tag, Object msg) {
		log(tag, msg.toString(), 'd');
	}

	public static void i(String tag, Object msg) {
		log(tag, msg.toString(), 'i');
	}

	public static void v(String tag, Object msg) {
		log(tag, msg.toString(), 'v');
	}

	public static void w(String tag, String text) {
		log(tag, text, 'w');
	}

	public static void e(String tag, String text) {
		log(tag, text, 'e');
	}

	public static void d(String tag, String text) {
		log(tag, text, 'd');
	}

	public static void i(String tag, String text) {
		log(tag, text, 'i');
	}

	public static void v(String tag, String text) {
		log(tag, text, 'v');
	}

	public static void flush() {
		writeLogToFile();
		sBuffer.setLength(0);
	}

	/**
	 * 根据tag, msg和等级，输出日志
	 * 
	 * @param tag
	 * @param msg
	 * @param level
	 * @return void
	 * @since v 1.0
	 */
	private static void log(String tag, String msg, char level) {
		if (SWITCH) {
			if ('e' == level) {
				Log.e(tag, msg);
			} else if ('w' == level) {
				Log.w(tag, msg);
			} else if ('d' == level) {
				Log.d(tag, msg);
			} else if ('i' == level) {
				Log.i(tag, msg);
			} else {
				Log.v(tag, msg);
			}

			if (WRITE_TO_CACHE) {
				writeLogToCache(String.valueOf(level), tag, msg);
			}
		}
	}

	private static void writeLogToCache(String level, String tag, String text) {
		Date nowTime = new Date();
		String strTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTime);
		String message = strTime + "    " + level + "    " + tag + "    " + text;
		sBuffer.append(message);
		sBuffer.append("\n");

		if (sBuffer.length() > MAX_CACHE_SIZE) {
			// 长度大于缓存长度，写入文件
			writeLogToFile();
			sBuffer.setLength(0);
		}
	}

	private static void writeLogToFile() {
		if (!WRITE_TO_FILE) {
			return;
		}

		File debugFile = new File(LOG_FILE_NAME);
		FileWriter filerWriter = null;
		BufferedWriter bufWriter = null;
		try {
			if (!debugFile.exists()) {
				debugFile.createNewFile();
			}

			boolean append = true;
			if (debugFile.length() > MAX_FILE_SIZE) {
				append = false;
			}

			filerWriter = new FileWriter(debugFile, append);
			bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(sBuffer.substring(0));
			bufWriter.newLine();
		} catch (Exception e) {
		} finally {
			try {
				if (bufWriter != null) {
					bufWriter.close();
				}
				if (filerWriter != null) {
					filerWriter.close();
				}
			} catch (Exception e) {
			}
		}
	}
}
