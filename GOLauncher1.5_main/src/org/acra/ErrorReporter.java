/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import com.go.util.MemoryUtil;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <p>
 * The ErrorReporter is a Singleton object in charge of collecting crash context
 * data and sending crash reports. It registers itself as the Application's
 * Thread default {@link UncaughtExceptionHandler}.
 * </p>
 * <p>
 * When a crash occurs, it collects data of the crash context (device, system,
 * stack trace...) and writes a report file in the application private
 * directory. This report file is then sent :
 * <ul>
 * <li>immediately if {@link #mReportingInteractionMode} is set to
 * {@link ReportingInteractionMode#SILENT} or
 * {@link ReportingInteractionMode#TOAST},</li>
 * <li>on application start if in the previous case the transmission could not
 * technically be made,</li>
 * <li>when the user accepts to send it if {@link #mReportingInteractionMode} is
 * set to {@link ReportingInteractionMode#NOTIFICATION}.</li>
 * </p>
 */
//CHECKSTYLE:OFF
public class ErrorReporter implements Thread.UncaughtExceptionHandler {
	private static final String LOG_TAG = CrashReport.LOG_TAG;

	/**
	 * Checks and send reports on a separate Thread.
	 * 
	 * @author Kevin Gaudin
	 */
	public final class ReportsSenderWorker extends Thread {
		private String mBody = null;
		private String mReportFileName = null;
		private String mReceiver = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			// checkAndSendReports(mContext, mReportFileName);
			try {
				sendMail(mContext, mReportFileName, mBody, mReceiver);
			} catch (Exception e) {
				Log.e(LOG_TAG, "send report Email failed");
			}
		}

		public void setCommentReportFileName(String reportFileName) {
			mReportFileName = reportFileName;
		}

		public void setCustomComment(String body) {
			mBody = body;
		}
		
		public void setEmailReceiver(String receiver) {
			mReceiver = receiver;
		}
		
	}

	/**
	 * This is the number of previously stored reports that we send in
	 * {@link #checkAndSendReports(Context)}. The number of reports is limited
	 * to avoid ANR on application start.
	 */
	private static final int MAX_SEND_REPORTS = 5;
	private static final String SVN = "35900";

	// These are the fields names in the POST HTTP request sent to
	// the GoogleDocs form. Any change made on the structure of the form
	// will need a mapping check of these constants.
	private static final String VERSION_NAME_KEY = "VersionName";
	private static final String VERSION_CODE_KEY = "VersionCode";
	private static final String PACKAGE_NAME_KEY = "PackageName";
	private static final String SVN_CODE = "SVN";
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
	private static final String TOTAL_MEM_SIZE_KEY = "TotalMemSize";
	private static final String AVAILABLE_MEM_SIZE_KEY = "AvaliableMemSize";
	private static final String CUSTOM_DATA_KEY = "CustomData";
	private static final String STACK_TRACE_KEY = "StackTrace";
	private static final String DENSITY_KEY = "DENSITY";
	private static final String CURRENT_MEM_KEY = "Current Heap";
	private static final String CURRENT_SCREEN_COUNT = "Screen count";
	private static final String TOTAL_USE_TIME = "Total use time(mins)";
	private static final String OUT_OF_MEMORY_ERROR = "bitmap size exceeds VM";
	private static final String WIDGET_INIT = "android.widget.RemoteViews.<init>";
	private static final String UPGRADE_ERROR = "result:3java.lang.ArrayIndexOutOfBoundsException: result:3";
	private static final String OUT_OF_MEMORY_FITER_EA = "java.lang.OutOfMemoryError: bitmap size exceeds VM budget";
	private static final String OUT_OF_MEMORY_FITER_EB = "bitmap size exceeds VM budgetjava.lang.OutOfMemoryError: bitmap size exceeds VM budget";
	private static final String OUT_OF_MEMORY_FITER_A = "android.graphics.Bitmap.nativeCreate(Native Method)";
	private static final String OUT_OF_MEMORY_FITER_B = "android.graphics.BitmapFactory.nativeDecodeAsset(Native Method)";
	private static final String MACHINE_MEMORY_INFOS = "Mem Infos";
	// This is where we collect crash data
	private Properties mCrashProperties = new Properties();

	// Some custom parameters can be added by the application developer. These
	// parameters are stored here.
	Map<String, String> mCustomParameters = new HashMap<String, String>();
	// This key is used in the mCustomParameters Map to store user comment in
	// NOTIFICATION interaction mode.
	static final String USER_COMMENT_KEY = "user.comment";
	// This key is used to store the silent state of a report sent by
	// handleSilentException().
	static final String IS_SILENT_KEY = "silent";
	static final String SILENT_PREFIX = IS_SILENT_KEY + "-";
	static final String ERROR_FILE_TYPE = "_stk.txt";

	static final String EXTRA_REPORT_FILE_NAME = "REPORT_FILE_NAME";

	// A reference to the system's previous default UncaughtExceptionHandler
	// kept in order to execute the default exception handling after sending
	// the report.
	private Thread.UncaughtExceptionHandler mDfltExceptionHandler;

	// Our singleton instance.
	private static ErrorReporter sInstanceSingleton;

	// The application context
	private Context mContext;

	// User interaction mode defined by the application developer.
	private ReportingInteractionMode mReportingInteractionMode = ReportingInteractionMode.SILENT;

	// Bundle containing resources to be used in UI elements.
	private Bundle mCrashResources = new Bundle();

	// The Url we have to post the reports to.
	private static Uri sFormUri;

	private String mCrashFilePath = null;

	private boolean mIsOutOfMemoryError = false;

	private boolean mIsUpgradeError = false;

	/**
	 * Use this method to provide the Url of the crash reports destination.
	 * 
	 * @param formUri
	 *            The Url of the crash reports destination (HTTP POST).
	 */
	public static void setFormUri(Uri formUri) {
		sFormUri = formUri;
	}

	/**
	 * <p>
	 * Use this method to provide the ErrorReporter with data of your running
	 * application. You should call this at several key places in your code the
	 * same way as you would output important debug data in a log file. Only the
	 * latest value is kept for each key (no history of the values is sent in
	 * the report).
	 * </p>
	 * <p>
	 * The key/value pairs will be stored in the GoogleDoc spreadsheet in the
	 * "custom" column, as a text containing a 'key = value' pair on each line.
	 * </p>
	 * 
	 * @param key
	 *            A key for your custom data.
	 * @param value
	 *            The value associated to your key.
	 */
	public void addCustomData(String key, String value) {
		mCustomParameters.put(key, value);
	}

	/**
	 * Generates the string which is posted in the single custom data field in
	 * the GoogleDocs Form.
	 * 
	 * @return A string with a 'key = value' pair on each line.
	 */
	private String createCustomInfoString() {
		String customInfo = "";
		Iterator<String> iterator = mCustomParameters.keySet().iterator();
		while (iterator.hasNext()) {
			String currentKey = iterator.next();
			String currentVal = mCustomParameters.get(currentKey);
			customInfo += currentKey + " = " + currentVal + "\n";
		}
		return customInfo;
	}

	/**
	 * Create or return the singleton instance.
	 * 
	 * @return the current instance of ErrorReporter.
	 */
	public static synchronized ErrorReporter getInstance() {
		if (sInstanceSingleton == null) {
			sInstanceSingleton = new ErrorReporter();
		}
		return sInstanceSingleton;
	}

	/**
	 * <p>
	 * This is where the ErrorReporter replaces the default
	 * {@link UncaughtExceptionHandler}.
	 * </p>
	 * 
	 * @param context
	 *            The android application context.
	 */
	public void init(Context context) {
		mDfltExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		mContext = context;
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

	public String collectMachineData(Context context) {
		mCrashProperties.clear();
		retrieveCrashData(context);
		String machineData = null;
		try {
			OutputStream out = new ByteArrayOutputStream();
			storeToOutputStream(out, mCrashProperties);
			machineData = out.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCrashProperties.clear();
		
		return machineData;
	}
	
	
	/**
	 * Collects crash data.
	 * 
	 * @param context
	 *            The application context.
	 */
	private void retrieveCrashData(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi;
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			if (pi != null) {
				// Application Version
				mCrashProperties.put(VERSION_NAME_KEY, pi.versionName != null
						? pi.versionName
						: "not set");
				mCrashProperties.put(VERSION_CODE_KEY, pi.versionCode != 0
						? pi.versionCode + " "
						: "not set");
			} else {
				// Could not retrieve package info...
				mCrashProperties.put(PACKAGE_NAME_KEY, "Package info unavailable");
			}

			mCrashProperties.put(SVN_CODE, SVN);
				
			// Application Package name
			mCrashProperties.put(PACKAGE_NAME_KEY, context.getPackageName());
			// Device model
			mCrashProperties.put(PHONE_MODEL_KEY, android.os.Build.MODEL);
			// Android version
			mCrashProperties.put(ANDROID_VERSION_KEY, android.os.Build.VERSION.RELEASE);

			// Android build data
			mCrashProperties.put(BOARD_KEY, android.os.Build.BOARD);
			mCrashProperties.put(BRAND_KEY, android.os.Build.BRAND);
			mCrashProperties.put(DEVICE_KEY, android.os.Build.DEVICE);
			mCrashProperties.put(DISPLAY_KEY, android.os.Build.DISPLAY);
			mCrashProperties.put(FINGERPRINT_KEY, android.os.Build.FINGERPRINT);
			mCrashProperties.put(HOST_KEY, android.os.Build.HOST);
			mCrashProperties.put(ID_KEY, android.os.Build.ID);
			mCrashProperties.put(MODEL_KEY, android.os.Build.MODEL);
			mCrashProperties.put(PRODUCT_KEY, android.os.Build.PRODUCT);
			mCrashProperties.put(TAGS_KEY, android.os.Build.TAGS);
			mCrashProperties.put(TIME_KEY, "" + android.os.Build.TIME);
			mCrashProperties.put(TYPE_KEY, android.os.Build.TYPE);
			mCrashProperties.put(USER_KEY, android.os.Build.USER);

			// Device Memory
			mCrashProperties.put(TOTAL_MEM_SIZE_KEY, "" + getTotalInternalMemorySize());
			mCrashProperties.put(AVAILABLE_MEM_SIZE_KEY, "" + getAvailableInternalMemorySize());

			// Application file path
			mCrashProperties.put(FILE_PATH_KEY, context.getFilesDir().getAbsolutePath());

			String infos = null;
			MemoryUtil mem_uitl = new MemoryUtil(mContext);
			if (null != mem_uitl) {
				infos = mem_uitl.getMemInfos();
			}

			if (null != infos) {
				mCrashProperties.put(MACHINE_MEMORY_INFOS, infos);
			} else {
				mCrashProperties.put(MACHINE_MEMORY_INFOS, "error");
			}

			// GoWidget Version
			if (CrashReportConfig.REPORT_ADDITIONAL_INFO) {
				String pkgInfo = "{\n";
				int size = CrashReportConfig.ADDITIONAL_PACKAGES.length;
				for (int i = 0; i < size; i++) {
					final String pkgName = CrashReportConfig.ADDITIONAL_PACKAGES[i];
					pkgInfo += "\t" + pkgName + "=";
					try {
						pi = pm.getPackageInfo(pkgName, 0);
						if (pi != null) {
							pkgInfo += pi.versionName;
						} else {
							pkgInfo += "Package info unavailable";
						}
						pi = null;
					} catch (Exception e) {
						Log.i(LOG_TAG, "Error while retrieving crash data === " + pkgName + " not found");
						pkgInfo += "Package info unavailable";
					}
					pkgInfo += "\n";
				}
				pkgInfo += "}";
				mCrashProperties.put(CrashReportConfig.ADDITIONAL_TAG, pkgInfo);
			}

			mCrashProperties.put(DENSITY_KEY,
					String.valueOf(context.getResources().getDisplayMetrics().density));
			mCrashProperties.put(CURRENT_MEM_KEY,
					Integer.toString((int) (Runtime.getRuntime().maxMemory() / 1024L / 1024L))
							+ "MB");

			// 当前桌面多少屏
			int currentScreenCount = getCurrentCount();
			mCrashProperties.put(CURRENT_SCREEN_COUNT, Integer.toString(currentScreenCount));
			String totalUseTime = getTotalUseTime();
			mCrashProperties.put(TOTAL_USE_TIME, totalUseTime);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error while retrieving crash data", e);
		}
	}

	private int getCurrentCount() {
		PreferencesManager spf = new PreferencesManager(mContext, "ErrorReport", 0);
		return spf.getInt("SCREEN_COUNT", 1);
	}

	private String getTotalUseTime() {
		try {
			PreferencesManager spf = new PreferencesManager(mContext, "ErrorReport", 0);
			long startTime = spf.getLong("STARTTIME", 0);
			if (startTime == 0) {
				return null;
			}
			int totalUseTime = (int) ((System.currentTimeMillis() - startTime) / (1000 * 60));
			// final Calendar calendar = Calendar.getInstance();
			// calendar.setTimeInMillis(totalUseTime);
			// int hours = calendar.get(Calendar.HOUR);
			// int mins = calendar.get(Calendar.MINUTE);
			// if (hours >= 0 && mins >=0){
			// return (Integer.toString(hours) + "Hour" + Integer.toString(mins)
			// + "mins");
			// }else{
			// return null;
			// }

			return Integer.toString(totalUseTime);
		} catch (Throwable e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
	 * .Thread, java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		try {
			disable();
			// Generate and send crash report
			handleException(e);
		} catch (Exception err) {
		}

		if (mReportingInteractionMode == ReportingInteractionMode.TOAST) {
			try {
				// Wait a bit to let the user read the toast
				Thread.sleep(4000);
			} catch (InterruptedException e1) {
				Log.e(LOG_TAG, "Error : ", e1);
			}
		}

		if (mReportingInteractionMode == ReportingInteractionMode.SILENT) {
			// If using silent mode, let the system default handler do it's job
			// and display the force close diaLoger.
			mDfltExceptionHandler.uncaughtException(t, e);
		} else {
			// If ACRA handles user notifications whit a Toast or a Notification
			// the Force Close dialog is one more notification to the user...
			// We choose to close the process ourselves using the same actions.
			CharSequence appName = "Application";
			try {
				PackageManager pm = mContext.getPackageManager();
				appName = pm.getApplicationInfo(mContext.getPackageName(), 0).loadLabel(
						mContext.getPackageManager());
				Log.e(LOG_TAG, appName + " fatal error : " + e.getMessage(), e);
			} catch (NameNotFoundException e2) {
				Log.e(LOG_TAG, "Error : ", e2);
			} finally {
				Log.i(LOG_TAG, "process id" + android.os.Process.myPid());
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(10);
			}
		}
	}

	/**
	 * Try to send a report, if an error occurs stores a report file for a later
	 * attempt. You can set the {@link ReportingInteractionMode} for this
	 * specific report. Use {@link #handleException(Throwable)} to use the
	 * Application default interaction mode.
	 * 
	 * @param e
	 *            The Throwable to be reported. If null the report will contain
	 *            a new Exception("Report requested by developer").
	 * @param reportingInteractionMode
	 *            The desired interaction mode.
	 */
	void handleException(Throwable e, ReportingInteractionMode reportingInteractionMode) {
		if (reportingInteractionMode == null) {
			reportingInteractionMode = mReportingInteractionMode;
		}

		if (e == null) {
			e = new Exception("Report requested by developer");
		}

		if (reportingInteractionMode == ReportingInteractionMode.TOAST) {
			new Thread() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Thread#run()
				 */
				@Override
				public void run() {
					Looper.prepare();
					Toast.makeText(mContext, mCrashResources.getInt(CrashReport.RES_TOAST_TEXT),
							Toast.LENGTH_LONG).show();
					Looper.loop();
				}

			}.start();
		}
		retrieveCrashData(mContext);
		// TODO: add a field in the googledoc form for the crash date.
		// Date CurDate = new Date();
		// Report += "Error Report collected on : " + CurDate.toString();

		// Add custom info, they are all stored in a single field
		mCrashProperties.put(CUSTOM_DATA_KEY, createCustomInfoString());

		// Build stack trace
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		printWriter.append(e.getMessage());
		e.printStackTrace(printWriter);
		Log.getStackTraceString(e);
		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		Throwable cause = e.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		mCrashProperties.put(STACK_TRACE_KEY, result.toString());
		printWriter.close();

		mIsOutOfMemoryError = false;
		mIsUpgradeError = false;
		// Always write the report file
		String reportFileName = saveCrashReportFile();
		Log.i(LOG_TAG, reportFileName);
		if (!mIsOutOfMemoryError && !mIsUpgradeError) {
			if (reportingInteractionMode == ReportingInteractionMode.SILENT
					|| reportingInteractionMode == ReportingInteractionMode.TOAST) {
				// Send reports now
				checkAndSendReports(mContext, null);
			} else if (reportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
				// Send reports when user accepts
				notifySendReport(reportFileName);
			}
		}

	}

	/**
	 * Send a report for this Throwable.
	 * 
	 * @param e
	 *            The Throwable to be reported. If null the report will contain
	 *            a new Exception("Report requested by developer").
	 */
	public void handleException(Throwable e) {
		handleException(e, mReportingInteractionMode);
	}

	public void handleSilentException(Throwable e) {
		// Mark this report as silent.
		mCrashProperties.put(IS_SILENT_KEY, "true");
		handleException(e, ReportingInteractionMode.SILENT);
	}

	/**
	 * Send a status bar notification. The action triggered when the
	 * notification is selected is to start the {@link CrashReportDialog}
	 * Activity.
	 * 
	 * @see CrashReport#getCrashResources()
	 */
	void notifySendReport(String reportFileName) {
		// This notification can't be set to AUTO_CANCEL because after a crash,
		// clicking on it restarts the application and this triggers a check
		// for pending reports which issues the notification back.
		// Notification cancellation is done in the dialog activity displayed
		// on notification click.
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// Default notification icon is the warning symbol
		int icon = android.R.drawable.stat_notify_error;
		if (mCrashResources.containsKey(CrashReport.RES_NOTIF_ICON)) {
			// Use a developer defined icon if available
			icon = mCrashResources.getInt(CrashReport.RES_NOTIF_ICON);
		}

		CharSequence tickerText = mContext.getText(mCrashResources
				.getInt(CrashReport.RES_NOTIF_TICKER_TEXT));
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);

		CharSequence contentTitle = mContext.getText(mCrashResources
				.getInt(CrashReport.RES_NOTIF_TITLE));
		CharSequence contentText = mContext.getText(mCrashResources
				.getInt(CrashReport.RES_NOTIF_TEXT));

		Intent notificationIntent = new Intent(mContext, CrashReportDialog.class);
		notificationIntent.putExtra(EXTRA_REPORT_FILE_NAME, reportFileName);
		Log.i(LOG_TAG, "crash report fileName = " + reportFileName);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

		notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
		notificationManager.notify(CrashReport.NOTIF_CRASH_ID, notification);
	}

	/**
	 * Sends the report in an HTTP POST to a GoogleDocs Form
	 * 
	 * @param context
	 *            The application context.
	 * @param errorContent
	 *            Crash data.
	 * @throws IOException
	 *             If unable to send the crash report.
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 *             Might be thrown if sending over https.
	 * @throws KeyManagementException
	 *             Might be thrown if sending over https.
	 */
	private static void sendCrashReport(Context context, Properties errorContent)
			throws UnsupportedEncodingException, IOException, KeyManagementException,
			NoSuchAlgorithmException {
		// values observed in the GoogleDocs original html form
		errorContent.put("pageNumber", "0");
		errorContent.put("backupCache", "");
		errorContent.put("submit", "Envoyer");

		URL reportUrl = new URL(sFormUri.toString());
		Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
		HttpUtils.doPost(errorContent, reportUrl);
	}

	/**
	 * When a report can't be sent, it is saved here in a file in the root of
	 * the application private directory.
	 */
	private String saveCrashReportFile() {
		try {
			Log.d(LOG_TAG, "Writing crash report file.");
			long timestamp = System.currentTimeMillis();
			String isSilent = mCrashProperties.getProperty(IS_SILENT_KEY);
			String fileName = createSaveFilePath();
			fileName += (isSilent != null ? SILENT_PREFIX : "") + "stack-" + timestamp
					+ ERROR_FILE_TYPE;
			File file = new File(fileName);
			FileOutputStream trace = new FileOutputStream(file, true);
			Log.i(LOG_TAG, fileName);
			String track = mCrashProperties.getProperty(STACK_TRACE_KEY);
			if (track.contains(OUT_OF_MEMORY_ERROR) && track.contains(WIDGET_INIT)) {
				mIsOutOfMemoryError = true;
			} else if (track.contains(UPGRADE_ERROR)) {
				mIsUpgradeError = true;
			}
//			 else if ((track.contains(OUT_OF_MEMORY_FITER_EA) ||
//			 track.contains(OUT_OF_MEMORY_FITER_EB))
//			 && (track.contains(OUT_OF_MEMORY_FITER_A) ||
//			 track.contains(OUT_OF_MEMORY_FITER_B))){
//			 mIsOutOfMemoryError = true;
//			 }

			track = track.replaceAll("\\n\\t", "\n");
			mCrashProperties.setProperty(STACK_TRACE_KEY, track);
			// mCrashProperties.store(trace, "");
			storeToOutputStream(trace, mCrashProperties);
			trace.flush();
			trace.close();
			return fileName;
		} catch (Exception e) {
			Log.e(LOG_TAG, "An error occured while writing the report file...", e);
		}
		return null;
	}

	private String createSaveFilePath() {
		if (mCrashFilePath == null) {
			mCrashFilePath = CrashReportConfig.LOG_PATH;
			File destDir = new File(mCrashFilePath);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
		}

		return mCrashFilePath;
	}

	/**
	 * Returns an array containing the names of available crash report files.
	 * 
	 * @return an array containing the names of available crash report files.
	 */
	String[] getCrashReportFilesList() {
		File dir = mContext.getFilesDir();

		if (dir == null) {
			return null;
		}

		Log.d(LOG_TAG, "Looking for error files in " + dir.getAbsolutePath());

		// Filter for ".stacktrace" files
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(ERROR_FILE_TYPE);
			}
		};
		return dir.list(filter);
	}

	/**
	 * <p>
	 * You can call this method in your main {@link Activity} onCreate() method
	 * in order to check if previously unsent crashes occured and immediately
	 * send them.
	 * </p>
	 * <p>
	 * This is called by default in any Application extending
	 * {@link CrashReport}.
	 * </p>
	 * 
	 * @param context
	 *            The application context.
	 */
	void checkAndSendReports(Context context, String userCommentReportFileName) {
		try {

			String[] reportFilesList = getCrashReportFilesList();
			if (reportFilesList != null && reportFilesList.length > 0) {
				TreeSet<String> sortedFiles = new TreeSet<String>();
				sortedFiles.addAll(Arrays.asList(reportFilesList));

				Properties previousCrashReport = new Properties();
				// send only a few reports to avoid ANR
				int curIndex = 0;
				boolean commentedReportFound = false;
				for (String curFileName : sortedFiles) {
					Log.i(LOG_TAG, curFileName);
					if (curIndex < MAX_SEND_REPORTS) {
						FileInputStream input = context.openFileInput(curFileName);
						previousCrashReport.load(input);
						input.close();
						// Insert the optional user comment written in
						// CrashReportDialog, only on the latest report file
						if (!commentedReportFound
								&& (curFileName.equals(userCommentReportFileName) || (curIndex == sortedFiles
										.size() - 1 && mCustomParameters
										.containsKey(USER_COMMENT_KEY)))) {
							String custom = previousCrashReport.getProperty(CUSTOM_DATA_KEY);
							if (custom == null) {
								custom = "";
							} else {
								custom += "\n";
							}
							previousCrashReport.put(CUSTOM_DATA_KEY, custom + USER_COMMENT_KEY
									+ " = " + mCustomParameters.get(USER_COMMENT_KEY));
							mCustomParameters.remove(USER_COMMENT_KEY);

						}
						sendCrashReport(context, previousCrashReport);

						// DELETE FILES !!!!
						File curFile = new File(context.getFilesDir(), curFileName);
						curFile.delete();
					}
					curIndex++;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Get rid of any user comment which would not be relevant anymore
			mCustomParameters.remove(USER_COMMENT_KEY);
		}
	}

	private void sendMail(Context context, String file, String body, String receiver) {
		Log.i(LOG_TAG, file);
		PreferencesManager pm = new PreferencesManager(context);
		//是否发送db
		boolean withDB = pm.getBoolean("need_to_send_db", false);
		Intent emailIntent = new Intent(withDB
				? android.content.Intent.ACTION_SEND_MULTIPLE
				: android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		String[] receivers = new String[] { CrashReportConfig.EMAIL_RECEIVER };
		if (receiver != null) {
			receivers = new String[] { receiver };	
		}

		String versionString = " v" + mContext.getString(CrashReportConfig.RES_APP_VERSION)
				+ " Fix Report " + SVN;

		String subject = CrashReportConfig.APP_NAME + versionString
				+ mContext.getString(CrashReportConfig.RES_EMAIL_SUBJECT);

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receivers);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		if (body != null) {
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		}
		File fileIn = new File(file);
		
		if (withDB) {
			ArrayList<Uri> uris = new ArrayList<Uri>();
			String dbPath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_DIR
					+ "/androidheart.db";
			FileUtil.copyFile(context.getDatabasePath("androidheart.db").getAbsolutePath(), dbPath);
			emailIntent.setType("*/*");
			uris.add(Uri.fromFile(new File(dbPath)));
			uris.add(Uri.fromFile(fileIn));
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		} else {
			emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileIn));
			emailIntent.setType("plain/text");
		}
		context.startActivity(emailIntent);

	}

	/**
	 * Set the wanted user interaction mode for sending reports.
	 * 
	 * @param reportingInteractionMode
	 */
	void setReportingInteractionMode(ReportingInteractionMode reportingInteractionMode) {
		mReportingInteractionMode = reportingInteractionMode;
	}

	/**
	 * This method looks for pending reports and does the action required
	 * depending on the interaction mode set.
	 */
	public void checkReportsOnApplicationStart() {
		String[] filesList = getCrashReportFilesList();
		if (filesList != null && filesList.length > 0) {
			boolean onlySilentReports = containsOnlySilentReports(filesList);
			if (mReportingInteractionMode == ReportingInteractionMode.SILENT
					|| mReportingInteractionMode == ReportingInteractionMode.TOAST
					|| (mReportingInteractionMode == ReportingInteractionMode.NOTIFICATION && onlySilentReports)) {
				if (mReportingInteractionMode == ReportingInteractionMode.TOAST) {
					Toast.makeText(mContext, mCrashResources.getInt(CrashReport.RES_TOAST_TEXT),
							Toast.LENGTH_LONG).show();
				}
				new ReportsSenderWorker().start();
			} else if (mReportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
				ErrorReporter.getInstance().notifySendReport(filesList[filesList.length - 1]);
			}
		}

	}

	/**
	 * Delete all report files stored.
	 */
	public void deletePendingReports() {
		String[] filesList = getCrashReportFilesList();
		if (filesList != null) {
			for (String fileName : filesList) {
				new File(mContext.getFilesDir(), fileName).delete();
			}
		}
	}

	/**
	 * Provide the UI resources necessary for user interaction.
	 * 
	 * @param crashResources
	 */
	void setCrashResources(Bundle crashResources) {
		mCrashResources = crashResources;
	}

	/**
	 * Disable ACRA : sets this Thread's {@link UncaughtExceptionHandler} back
	 * to the system default.
	 */
	public void disable() {
		if (mDfltExceptionHandler != null) {
			Thread.setDefaultUncaughtExceptionHandler(mDfltExceptionHandler);
		}
	}

	/**
	 * Checks if the list of pending reports contains only silently sent
	 * reports.
	 * 
	 * @param reports
	 *            the list of reports provided by
	 *            {@link #getCrashReportFilesList()}
	 * @return True if there only silent reports. False if there is at least one
	 *         nont-silent report.
	 */
	public boolean containsOnlySilentReports(String[] reportFileNames) {
		for (String reportFileName : reportFileNames) {
			if (!reportFileName.startsWith(SILENT_PREFIX)) {
				return false;
			}
		}
		return true;
	}

	public synchronized void storeToOutputStream(OutputStream out, Properties properties)
			throws IOException {
		if (properties == null) {
			return;
		}

		String lineSeparator = System.getProperty("line.separator");
		if (lineSeparator == null) {
			lineSeparator = "\n";
		}

		StringBuilder buffer = new StringBuilder(200);
		OutputStreamWriter writer = new OutputStreamWriter(out, "ISO8859_1"); //$NON-NLS-1$
		// 输出日期
		writer.write("#"); //$NON-NLS-1$
		writer.write(new Date().toString());
		writer.write(lineSeparator);

		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			buffer.append(key);
			buffer.append('=');
			buffer.append((String) entry.getValue());
			buffer.append(lineSeparator);
			writer.write(buffer.toString());
			buffer.setLength(0);
		}
		writer.flush();
	}
}