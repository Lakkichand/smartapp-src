package com.jiubang.ggheart.apps.desks.snapshot;

import java.io.DataOutputStream;
import java.io.OutputStream;

import android.app.Dialog;
import android.content.Context;
import android.os.Build.VERSION;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * @author dengdazhong
 *
 */
public class DialogUtil {

	public static void openTutorialDialog(final Context context) {
		DialogConfirm mNormalDialog = new DialogConfirm(context);
		mNormalDialog.show();
		mNormalDialog.setTitle(context.getString(R.string.snapshot_mode));
		View view = LayoutInflater.from(context).inflate(R.layout.snapshot_notice_layout, null);
		TextView notice = (TextView) view.findViewById(R.id.snapshot_notice);
		String notice1 = context.getString(R.string.snapshot_tutorial_notice1);
		String notice2 = context.getString(R.string.snapshot_tutorial_notice2);
		SpannableStringBuilder text = new SpannableStringBuilder(notice1 + notice2);
		text.setSpan(
				new ForegroundColorSpan(context.getResources().getColor(
						R.color.snapshot_tutorial_notice_color)), 0, notice1.length() - 1,
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		text.setSpan(
				new ForegroundColorSpan(context.getResources().getColor(
						R.color.snapshot_tutorial_color)), notice1.length(),
				(notice1.length() + notice2.length()) - 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		notice.setText(text);
		LinearLayout messageLayout = (LinearLayout) mNormalDialog.findViewById(R.id.message_layout);
		messageLayout.removeAllViews();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		messageLayout.addView(view, params);
		mNormalDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switchMode(context);
			}
		});

		//		String message = context.getString(R.string.snapshot_tutorial)
		//				+ "\n"
		//				+ context.getString(R.string.snapshot_tutorial_notice);
		//		AlertDialog.Builder builder = new AlertDialog.Builder(context)
		//		.setTitle(R.string.snapshot_mode)
		//		.setMessage(message)
		//		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		//			@Override
		//			public void onClick(DialogInterface dialog, int which) {
		//				startSnapShot(context);
		//			}
		//		})
		//		.setNegativeButton(R.string.cancel, null);
		//		return mNormalDialog;
	}

	public static void checkSnapShotTutorial(Context context) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		int times = sharedPreferences.getInt(IPreferencesIds.SHOULD_SHOW_SNAPSHOT, 0);
		if (times >= 3) {
			switchMode(context);
		} else {
			sharedPreferences.putInt(IPreferencesIds.SHOULD_SHOW_SNAPSHOT, times + 1);
			sharedPreferences.commit();
			openTutorialDialog(context);
		}
	}
	
	/**
	 *  是否需要请求root权限
	 * @return
	 */
	public static boolean isNeedRequireRoot() {
		PreferencesManager sharedPreferences = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		// root状态值
		// 0为没有保存
		// 1为root
		// 2为非root
		int rootValue = sharedPreferences.getInt(IPreferencesIds.SNAPSHOT_IS_ROOTED, 0);
		switch (rootValue) {
			case 0 :
				return true;
			case 1 :
				return true;
			case 2 :
				return false;
			default :
				return true;
		}
	}

	public static void saveRootValue(boolean isRoot) {
		PreferencesManager sharedPreferences = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		sharedPreferences.putInt(IPreferencesIds.SNAPSHOT_IS_ROOTED, isRoot ? 1 : 2);
		sharedPreferences.commit();
	}
	
	/**
	 * 选择采用root或者非root模式
	 * @param context
	 */
	public static void switchMode(Context context) {
		boolean isNeedRequireRoot = isNeedRequireRoot();
		if (isNeedRequireRoot) {
			requireRoot();
		} else {
			PreferencesManager sharedPreferences = new PreferencesManager(GoLauncher.getContext(),
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean showNeedRoot = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_NEED_ROOT, true);
			if (showNeedRoot) {
				showNeedRootDialog(context);
				sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_NEED_ROOT, false);
				sharedPreferences.commit();
			} else {
				startSnapShot(GOLauncherApp.getContext(), false);
			}
		}
	}

	public static void requireRoot() {
		if (VERSION.SDK_INT >= 17) { // 4.2不兼容root默认采用view的方式
			PreferencesManager sharedPreferences = new PreferencesManager(GoLauncher.getContext(),
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_NEED_ROOT, false);
			sharedPreferences.commit();
			saveRootValue(false); // 保存没有root
			showNeedRootDialogInUiThread();
			return;
		}
		final Dialog dialog = new Dialog(GoLauncher.getContext(), R.style.Dialog);
		dialog.setContentView(R.layout.snapshot_watting_for_require_root_dialog);
		dialog.show();
		new Thread() {
			public void run() {
				PreferencesManager sharedPreferences = new PreferencesManager(GoLauncher.getContext(),
						IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
				try {
					if (SnapShotManager.isAccessGiven()) {
						// 成功获取到权限
						cancelDialog(dialog);
						startSnapShot(GOLauncherApp.getContext(), true);
						saveRootValue(true);
						return;
					}
					Process process = Runtime.getRuntime().exec("su");
					OutputStream os = process.getOutputStream();
					DataOutputStream dos = new DataOutputStream(os);
					try {
						for (int i = 0; i < 40; i++) {
							sleep(250);
							dos.writeBytes("chmod 777 /dev/graphics/fb0\n");
							if (SnapShotManager.isAccessGiven()) {
								cancelDialog(dialog);
								startSnapShot(GOLauncherApp.getContext(), true);
								saveRootValue(true);
								return;
							}
						}
					} catch (Exception e) {
						cancelDialog(dialog);
						showNeedRootAuthorizationDialogInUiThread();
						return;
					}
				} catch (Exception e) {
					// 这里发生exception说明用户没有root
					sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_NEED_ROOT, false);
					sharedPreferences.commit();
					saveRootValue(false); // 保存没有root
					cancelDialog(dialog);
					showNeedRootDialogInUiThread();
					return;
				}
				sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_NEED_ROOT, false);
				sharedPreferences.commit();
				saveRootValue(false); // 保存没有root
				cancelDialog(dialog);
				showNeedRootDialogInUiThread();
			}
		}.start();
	}
	
	public static void cancelDialog(final Dialog dialog) {
		GoLauncher.postUiRunnable(null, new Runnable() {

			@Override
			public void run() {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
			}

		}, false);
	}
	
	/**
	 * 在ui线程中创建需要获取root权限对话框
	 */
	public static void showNeedRootDialogInUiThread() {
		GoLauncher.postUiRunnable(null, new Runnable() {

			@Override
			public void run() {
				showNeedRootDialog(GoLauncher.getContext());
			}

		}, false);
	}
	
	/**
	 * 在ui线程中创建需要打开root权限对话框
	 */
	public static void showNeedRootAuthorizationDialogInUiThread() {
		GoLauncher.postUiRunnable(null, new Runnable() {

			@Override
			public void run() {
				showNeedRootAuthorizationDialog(GoLauncher.getContext());
			}

		}, false);
	}
	
	public static void showNeedRootDialog(final Context context) {
		DialogConfirm mNormalDialog = new DialogConfirm(context);
		mNormalDialog.show();
		mNormalDialog.setTitle(context.getString(R.string.snapshot_mode));
		View view = LayoutInflater.from(context).inflate(R.layout.snapshot_notice_layout, null);
		view.findViewById(R.id.snapshot_icon).setVisibility(View.GONE);
		TextView notice = (TextView) view.findViewById(R.id.snapshot_message);
		String noticeStr = context.getString(R.string.snapshot_need_root_for_more);
		notice.setText(noticeStr);
		LinearLayout messageLayout = (LinearLayout) mNormalDialog.findViewById(R.id.message_layout);
		messageLayout.removeAllViews();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		messageLayout.addView(view, params);
		mNormalDialog.findViewById(R.id.dialog_cancel).setVisibility(View.GONE);
		mNormalDialog.findViewById(R.id.dialog_split).setVisibility(View.GONE);
		mNormalDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startSnapShot(GOLauncherApp.getContext(), false);
			}
		});
	}
	
	public static void showNeedRootAuthorizationDialog(final Context context) {
		DialogConfirm mNormalDialog = new DialogConfirm(context);
		mNormalDialog.show();
		mNormalDialog.setTitle(context.getString(R.string.snapshot_mode));
		View view = LayoutInflater.from(context).inflate(R.layout.snapshot_notice_layout, null);
		view.findViewById(R.id.snapshot_icon).setVisibility(View.GONE);
		TextView notice = (TextView) view.findViewById(R.id.snapshot_message);
		String noticeStr = context.getString(R.string.snapshot_require_root);
		notice.setText(noticeStr);
		LinearLayout messageLayout = (LinearLayout) mNormalDialog.findViewById(R.id.message_layout);
		messageLayout.removeAllViews();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		messageLayout.addView(view, params);
		mNormalDialog.findViewById(R.id.dialog_cancel).setVisibility(View.GONE);
		mNormalDialog.findViewById(R.id.dialog_split).setVisibility(View.GONE);
		mNormalDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startSnapShot(GOLauncherApp.getContext(), false);
			}
		});
	}
	
	public static void startSnapShot(Context context, boolean isRoot) {
		SnapShotManager.getInstance(context).setRootMode(isRoot);
		SnapShotManager.getInstance(context).startCapture();
	}
}
