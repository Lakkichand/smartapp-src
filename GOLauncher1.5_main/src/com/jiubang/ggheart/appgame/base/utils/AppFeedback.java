/**
 * 
 */
package com.jiubang.ggheart.appgame.base.utils;

import java.io.File;

import org.acra.ErrorReporter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * @author liguoliang
 * 
 */
public class AppFeedback {
	public static final String APP_FEEDBACK_MAIL = "goappgamecenter@gmail.com";

	public static final String[] FEEDBACK_RECEIVER = new String[] { APP_FEEDBACK_MAIL };

	public static void showAppMenuFeedback(final Context context) {
		if (context == null) {
			return;
		}
		final String[] feedbackArray = context.getResources().getStringArray(
				R.array.appgame_menu_feedback_array);
		OnClickListener listener = createFeedbackListener(context, feedbackArray);
		if (feedbackArray == null || listener == null) {
			return;
		}
		showFeedbackDialog(context, feedbackArray, listener);
	}

	public static void showDetailFeedback(Context context) {
		if (context == null) {
			return;
		}
		final String[] feedbackArray = context.getResources().getStringArray(
				R.array.appgame_detail_feedback_array);
		OnClickListener listener = createFeedbackListener(context, feedbackArray);
		if (feedbackArray == null || listener == null) {
			return;
		}
		showFeedbackDialog(context, feedbackArray, listener);
	}

	private static OnClickListener createFeedbackListener(final Context context,
			final String[] items) {
		if (context == null || items == null) {
			return null;
		}
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which < 0 || which > items.length - 1) {
					return;
				}
				String subject = "Feedback for AppCenter&GameZone(" + items[which] + " "
						+ context.getString(R.string.curVersion) + ")";
				String body = "\n\n";
				body += "\nProduct=" + android.os.Build.PRODUCT;
				body += "\nPhoneModel=" + android.os.Build.MODEL;
				body += "\nKernel=" + Machine.getLinuxKernel();
				body += "\nROM=" + android.os.Build.DISPLAY;
				body += "\nBoard=" + android.os.Build.BOARD;
				body += "\nDevice=" + android.os.Build.DEVICE;
				body += "\nDensity="
						+ String.valueOf(context.getResources().getDisplayMetrics().density);
				body += "\nPackageName=" + context.getPackageName();
				body += "\nAndroidVersion=" + android.os.Build.VERSION.RELEASE;
				body += "\nTotalMemSize="
						+ (ErrorReporter.getTotalInternalMemorySize() / 1024 / 1024) + "MB";
				body += "\nFreeMemSize="
						+ (ErrorReporter.getAvailableInternalMemorySize() / 1024 / 1024) + "MB";
				body += "\nRom App Heap Size="
						+ Integer
								.toString((int) (Runtime.getRuntime().maxMemory() / 1024L / 1024L))
						+ "MB";

				File file = new File(
						LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH);
				Uri uri = null;
				if (file.exists()) {
					// 判断是否有网络日志信息存在，如果存在则添加到附件中
					uri = Uri.parse("file://"
							+ LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH);
				}
				startMalil(context, FEEDBACK_RECEIVER, subject, body, uri);
			}
		};
		return listener;
	}

	public static void showFeedbackDialog(Context context, String[] items,
			OnClickListener itemListener) {
		if (context == null || items == null || itemListener == null) {
			return;
		}
		DialogSingleChoice alertDialog = new DialogSingleChoice(context);
		alertDialog.show();
		alertDialog.setTitle(R.string.feedback_select_type_title);
		alertDialog.setItemData(items, -1, false);
		alertDialog.setOnItemClickListener(itemListener);
	}

	public static void startMalil(Context context, String[] receiver, String subject, String body,
			Uri uri) {
		// 开启邮箱，发送邮件
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

		emailIntent.putExtra(Intent.EXTRA_TEXT, body);

		if (uri != null) {
			// 如果有附件
			emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		}

		emailIntent.setType("plain/text");
		try {
			context.startActivity(emailIntent);
		} catch (Exception e) {
			Toast.makeText(context, R.string.appgame_error_record_noemail, Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
	}
}
