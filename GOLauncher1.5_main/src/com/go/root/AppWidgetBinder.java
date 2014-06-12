package com.go.root;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class AppWidgetBinder {
	private static final String LOG_TAG = "AppWidgetBinder";
	private static final String ROOT_HELPER = "com.go.root.RootHelper";
	private static final String JAR_FILE = "root_helper.jar";

	/**
	 * 使用root权限bindAppWidgetId
	 * 
	 * @param context
	 * @param appWidgetId
	 *            通过 {@link AppWidgetHost#allocateAppWidgetId()}分配的appWidgetId
	 * @param provider
	 *            AppWidget的{@link AppWidgetProviderInfo#provider}
	 */
	public static void bindAppWidgetId(Context context, int appWidgetId, ComponentName provider) {
		String path = context.getFilesDir().getAbsolutePath();
		String jarPath = path + "/" + JAR_FILE;
		boolean needCopy = !isFileExists(context, jarPath);
		try {
			if (needCopy) {
				copyJar(context, jarPath);
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "copy root_helper.jar error!", e);
			return;
		}

		final String bindAppWidgetIdCmd = "bindAppWidgetId " + appWidgetId + " "
				+ provider.flattenToString();

		String cmd = "";
		if (Build.VERSION.SDK_INT >= 14) {
			cmd = "LD_LIBRARY_PATH=/vendor/lib:/system/lib ";
		}
		StringBuilder sb = new StringBuilder(cmd).append("exec app_process ").append(path)
				.append(" ").append(ROOT_HELPER).append(" ").append(bindAppWidgetIdCmd);

		String[] commands = null;
		if (needCopy) {
			// 如果文件不存在，拷贝完成后还需要修改权限为755
			commands = new String[] { "export CLASSPATH=" + jarPath, "chmod 755 " + jarPath,
					sb.toString() };
		} else {
			commands = new String[] { "export CLASSPATH=" + jarPath, sb.toString() };
		}

		try {
			RootUtils.sendShell(commands, 0, null, true, RootUtils.sTimeout);
		} catch (IOException e) {
			Log.e(LOG_TAG, "bindAppWidgetId error! id = " + appWidgetId + ", provider = "
					+ provider, e);
		} catch (RootToolsException e) {
			Log.e(LOG_TAG, "bindAppWidgetId error! id = " + appWidgetId + ", provider = "
					+ provider, e);
		} catch (TimeoutException e) {
			Log.e(LOG_TAG, "bindAppWidgetId error! id = " + appWidgetId + ", provider = "
					+ provider, e);
		}
	}

	private static boolean isFileExists(Context context, String jarPath) {
		File file = new File(jarPath);
		return file.exists();
	}

	private static void copyJar(Context context, String destPath) throws IOException {
		File file = new File(destPath);
		InputStream inputStream = context.getResources().getAssets().open(JAR_FILE);
		FileOutputStream outputStream = new FileOutputStream(file);
		byte[] buffer = new byte[4096]; // 4k

		while (true) {
			int size = inputStream.read(buffer);
			if (size == -1) {
				break;
			}
			outputStream.write(buffer, 0, size);
		}

		inputStream.close();
		outputStream.close();
	}
}
