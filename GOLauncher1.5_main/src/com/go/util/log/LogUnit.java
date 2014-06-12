package com.go.util.log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.acra.ErrorReporter;
import org.acra.ErrorReporter.ReportsSenderWorker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;

import com.go.util.device.Machine;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

//CHECKSTYLE:OFF
/**
 * 
 * @author wenjiaming
 * 
 */
public class LogUnit {

	/**
	 * 文件路径
	 */
	private final static String DAILY_PATH = File.separator + "GOLauncherEX" + File.separator
			+ "daily_log" + File.separator;

	/**
	 * 文件编码格式
	 */
	private final static String CHARSET = "UTF-8";

	/**
	 * Priority constant for the println method; use Log.v.
	 */
	public static final String VERBOSE = "VERBOSE";

	/**
	 * Priority constant for the println method; use Log.d.
	 */
	public static final String DEBUG = "DEBUG";

	/**
	 * Priority constant for the println method; use Log.i.
	 */
	public static final String INFO = "INFO";

	/**
	 * Priority constant for the println method; use Log.w.
	 */
	public static final String WARN = "WARN";

	/**
	 * Priority constant for the println method; use Log.e.
	 */
	public static final String ERROR = "ERROR";

	// //////////////////////////////////开关设置////////////
	/**
	 * 选择显示那种类型的信息
	 */
	public volatile static boolean isShowDebugText = false;
	public volatile static boolean isShowInfoText = false;
	public volatile static boolean isShowWarnText = false;
	public volatile static boolean isShowErrorText = false;
	public volatile static boolean isShowVerboseText = false;
	
	/**
	 * 将log信息打印到的文件名
	 */
	public static String sLogToFile = null;

	/**
	 * 是否把系统日志附加写到文件(原有LogCat还会显示)
	 */
	private volatile static boolean isWriteFile = false;

	/**
	 * 用于SD卡离开和回归
	 */
	private volatile static boolean isTempNotWriteFile = false;
	/**
	 * 是否马上把缓冲区内容写入文件标志位
	 */
	private volatile static boolean isWriteImmediately = false;
	// ////////////////////////////////////////////////////////////////

	/**
	 * 标准Tag或者自己使用Tag
	 */
	private final static String TAG = "FUNINFO";
	/**
	 * 标准Tag或者自己使用Tag
	 */
	// private final static String ONLY_SHOW_TAG = "FUNINFO";

	/**
	 * 是否只显示Tag为TAG的信息
	 */
	// public static boolean isOnlyShowStandardTag = false;
	/**
	 * 文件流
	 */
	private static FileOutputStream mFileOut;
	/**
	 * 缓冲流
	 */
	private static BufferedOutputStream mBufferedOut;
	/**
	 * 链接字节流和字符流的过滤流
	 */
	private static OutputStreamWriter mWrite;
	/**
	 * 缓冲流大小
	 */
	private final static int SIZE = 1024 * 4;
	/**
	 * 日志文件
	 */
	private static File mFile;

	private static StringBuilder mStringBuilder;

	private static String BLANK = " ";

	/**
	 * 监听SD卡状态消息
	 */
	private static BroadcastReceiver broadcastReceiver = null;
	// //////////////////////文件内容格式/////////////////////////////////////////
	private final static int TIME_LENGTH = 25;
	private final static int INFO_LENGTH = 10;
	private final static int TAG_BLANK_LENGTH = 5;

	// //////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 * @param type
	 *            消息类型
	 * @param tag
	 *            消息ID
	 * @param content
	 *            消息内容
	 */
	private synchronized static void writerFileToSD(String type, String tag, String content) {
		try {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				mStringBuilder.delete(0, mStringBuilder.length());
				// 日期
				String date = TimeUnit.getStringDateLongest();
				mStringBuilder.append(date);
				int length = TIME_LENGTH - date.length();
				for (int i = 0; i < length; i++) {
					mStringBuilder.append(BLANK);
				}
				// 消息类型
				mStringBuilder.append(type);
				length = INFO_LENGTH - type.length();
				for (int i = 0; i < length; i++) {
					mStringBuilder.append(BLANK);
				}
				// 消息ID
				mStringBuilder.append(tag);
				for (int i = 0; i < TAG_BLANK_LENGTH; i++) {
					mStringBuilder.append(BLANK);
				}
				// 消息内容
				mStringBuilder.append(content);
				mStringBuilder.append("\n");
				mWrite.append(mStringBuilder.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void i(String text) {
		if (isShowInfoText) {
			Log.i(TAG, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.INFO, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void i(String key, String text) {
		if (isShowInfoText) {
			Log.i(key, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.INFO, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void i(String text, Throwable tr) {
		if (isShowInfoText) {
			Log.i(TAG, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.INFO, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void i(String key, String text, Throwable tr) {
		if (isShowInfoText) {
			Log.i(key, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.INFO, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////
	/**
	 * 直接打印出需要的信息
	 * @param key
	 * @param text
	 */
	public synchronized static void diyInfo(String key, String text) {
		Log.i(key, text);
		setWriteFile(true, sLogToFile);
		writerFileToSD(LogUnit.INFO, key, text);
		immediatelyWriterFileToSD();
	}
	
	/**
	 * 打印出当前函数被调用的堆栈信息，方便查看
	 * @param key
	 */
	public static void whoCallme(String key) {
		try {
			Exception e = new Exception();
			e.fillInStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			diyInfo(key, sw.toString());
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	/**
	 * 打印出指定的异常信息
	 * @param key
	 * @param e
	 */
	public static void whoCallme(String key, Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			diyInfo(key, sw.toString());
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	/**
	 * 发送日志信息
	 */
	public static void sendDiyInfoFile(String receiver) {
		try {
			ErrorReporter err = ErrorReporter.getInstance();
			ReportsSenderWorker worker = err.new ReportsSenderWorker();
			String sendToFile = LauncherEnv.Path.SDCARD + DAILY_PATH + sLogToFile + ".txt";
			worker.setCommentReportFileName(sendToFile);
			String machineInfo = err.collectMachineData(GOLauncherApp.getContext());
			worker.setCustomComment(machineInfo);
			worker.setEmailReceiver(receiver);
			worker.start();
		} catch (Exception e) {
			Log.e(TAG, "LogUnit-sendDiyInfoFile()", e);
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	public static void e(String text) {
		if (isShowErrorText) {
			Log.e(TAG, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.ERROR, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void e(String key, String text) {
		if (isShowErrorText) {
			Log.e(key, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.ERROR, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void e(String text, Throwable tr) {
		if (isShowErrorText) {
			Log.e(TAG, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.ERROR, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void e(String key, String text, Throwable tr) {
		if (isShowErrorText) {
			Log.i(key, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.ERROR, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	public static void w(String text) {
		if (isShowWarnText) {
			Log.w(TAG, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.WARN, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void w(String key, String text) {
		if (isShowWarnText) {
			Log.w(key, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.WARN, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void w(String text, Throwable tr) {
		if (isShowWarnText) {
			Log.w(TAG, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.WARN, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void w(String key, String text, Throwable tr) {
		if (isShowWarnText) {
			Log.i(key, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.WARN, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	public static void d(String text) {
		if (isShowDebugText) {
			Log.d(TAG, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.DEBUG, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void d(String key, String text) {
		if (isShowDebugText) {
			Log.d(key, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.DEBUG, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void d(String text, Throwable tr) {
		if (isShowDebugText) {
			Log.d(TAG, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.DEBUG, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void d(String key, String text, Throwable tr) {
		if (isShowDebugText) {
			Log.i(key, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.DEBUG, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	public static void v(String text) {
		if (isShowVerboseText) {
			Log.v(TAG, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.VERBOSE, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void v(String key, String text) {
		if (isShowVerboseText) {
			Log.v(key, text);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.VERBOSE, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void v(String text, Throwable tr) {
		if (isShowVerboseText) {
			Log.v(TAG, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.VERBOSE, TAG, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	public static void v(String key, String text, Throwable tr) {
		if (isShowVerboseText) {
			Log.i(key, text, tr);
			if (isWriteFile && !isTempNotWriteFile) {
				writerFileToSD(LogUnit.VERBOSE, key, text);
				if (isWriteImmediately) {
					immediatelyWriterFileToSD();
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * 马上把缓冲区的内容写到文件
	 */
	public static void immediatelyWriterFileToSD() {
		if (mWrite != null) {
			try {
				mWrite.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isWriteFile() {
		return isWriteFile;
	}

	public static void setWriteFile(boolean isWriteToFile) {
		setWriteFile(isWriteToFile, null);
	}

	public synchronized static void setWriteFile(boolean isWriteToFile, final String logToFile) {
		if (LogUnit.isWriteFile != isWriteToFile) {
			LogUnit.isWriteFile = isWriteToFile;
			if (isWriteToFile) {
				openStream(logToFile);
			} else {
				LogUnit.isWriteFile = isWriteToFile;
				closeStream();
			}
		}
	}
	
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 清空LogCat的缓冲区
	 */
	public static void clearLogcat() {
		LogUnit.i("ClearLogcat Content");
		try {
			Runtime.getRuntime().exec("logcat -c");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 马上把LogCat缓冲区的内容写到SD
	 */
	public static void writeLogcatToSD() {
		LogUnit.i("WriteLogcatToSD");
		new Thread(new Runnable() {

			@Override
			public void run() {
				String state = Environment.getExternalStorageState();
				try {
					if (Environment.MEDIA_MOUNTED.equals(state)) {
						File directory = Environment.getExternalStorageDirectory();
						File path = new File(directory, DAILY_PATH);
						File file = new File(path, "LogCat "
								+ TimeUnit.getStringDate("yyyy-MM-dd HH-mm-ss") + ".txt");
						if (!path.exists()) {
							if (path.mkdir()) {
								if (!file.exists()) {
									file.createNewFile();
								}
							}
						} else {
							if (!file.exists()) {
								file.createNewFile();
							}
						}
						FileOutputStream fout = new FileOutputStream(file, false);
						BufferedOutputStream bout = new BufferedOutputStream(fout, SIZE);
						OutputStreamWriter writer = new OutputStreamWriter(bout, CHARSET);

						String[] LOGCAT_CMD = { "logcat", "-d", "-v", "time", "*:V" };
						Process process = Runtime.getRuntime().exec(LOGCAT_CMD);
						BufferedReader reader = new BufferedReader(new InputStreamReader(
								process.getInputStream()));
						String line = null;
						while ((line = reader.readLine()) != null) {
							writer.append(line);
							writer.append("\n");
						}
						reader.close();
						reader = null;
						process.destroy();
						process = null;
						writer.close();
						writer = null;
						bout.close();
						bout = null;
						fout.close();
						fout = null;
						clearLogcat();
					}
				} catch (IOException e) {
				}
			}
		}).start();
	}

	private synchronized static void resetBroadCastReceiver(Context context) {
		if (broadcastReceiver != null) {
			context.unregisterReceiver(broadcastReceiver);
		}

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.i(TAG, action);
				if (!isWriteFile) {
					return;
				}
				if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {
					isTempNotWriteFile = true;
					closeStream();
					LogUnit.i(Intent.ACTION_MEDIA_REMOVED);
				} else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {
					isTempNotWriteFile = true;
					closeStream();
					LogUnit.i(Intent.ACTION_MEDIA_SHARED);
				} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
					isTempNotWriteFile = true;
					closeStream();
					LogUnit.i(Intent.ACTION_MEDIA_UNMOUNTED);
				} else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
					isTempNotWriteFile = true;
					closeStream();
					LogUnit.i(Intent.ACTION_MEDIA_EJECT);
				} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
					openStream(null);
					isTempNotWriteFile = false;
					LogUnit.i(Intent.ACTION_MEDIA_MOUNTED);
				}
			}
		};
	}

	public static void sdMountListener(Context context, boolean isRegister) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addDataScheme("file");
		if (isRegister) {
			resetBroadCastReceiver(context);
			context.registerReceiver(broadcastReceiver, intentFilter);
			LogUnit.i("RegisterReceiver MountListener");
		} else {
			if (broadcastReceiver != null) {
				context.unregisterReceiver(broadcastReceiver);
				LogUnit.i("UnrgisterReceiver MountListener");
			}
		}
	}

	private synchronized static void openStream(final String logToFile) {
		if (isWriteFile) {
			try {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mStringBuilder = new StringBuilder();
					File path = new File(LauncherEnv.Path.SDCARD, DAILY_PATH);
					String logFile = logToFile;
					if (logFile == null) {
						logFile = "Daily Log " + TimeUnit.getStringDateShort();
					}
					mFile = new File(path, logFile + ".txt");
					if (!path.exists()) {
						if (path.mkdir()) {
							if (!mFile.exists()) {
								mFile.createNewFile();
							}
						}
					} else {
						if (!mFile.exists()) {
							mFile.createNewFile();
						}
					}
					mFileOut = new FileOutputStream(mFile, true);
					mBufferedOut = new BufferedOutputStream(mFileOut, SIZE);
					mWrite = new OutputStreamWriter(mBufferedOut, CHARSET);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized static void closeStream() {
		try {
			if (mWrite != null) {

				mWrite.close();
				mWrite = null;
			}
			if (mBufferedOut != null) {
				mBufferedOut.close();
				mBufferedOut = null;
			}
			if (mFileOut != null) {
				mFileOut.close();
				mFileOut = null;
			}
			mStringBuilder = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void startDebugService(final Context context) {
		try {
			Intent intent = new Intent("com.daqi.debug.startservice");
			if (Machine.IS_HONEYCOMB_MR1) {
				final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x00000020;
				intent.setFlags(FLAG_INCLUDE_STOPPED_PACKAGES);
			}
			Log.i("debugservice", "-----2----LogUnit-----startDebugService-");
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static void feedbackDebugService(final Context context) {
		try {
			Intent intent = new Intent("com.daqi.debug.feedback");
			if (Machine.IS_HONEYCOMB_MR1) {
				final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x00000020;
				intent.setFlags(FLAG_INCLUDE_STOPPED_PACKAGES);
			}
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}