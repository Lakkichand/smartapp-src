package com.jiubang.ggheart.apps.gowidget.gostore.brocastreceiver;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.component.AppRecommendDialogActivity;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.net.InstallCallbackManager;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.download.DefaultDownloadListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.ServiceCallbackDownload;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.services.GoStoreUpdateInstallService;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 */
public class GoStoreInstalledBrocastReceiver extends BroadcastReceiver {

	public static final String PACKAGE_NAME_KEY = "packageName";
	public static final String PACKAGE_ACTION_KEY = "packageAction";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (context != null && intent != null) {
			final String action = intent.getAction();
			String data = intent.getDataString();
			String packageName = null;
			if (data != null && !"".equals(data)) {
				String[] items = data.split(":");
				if (items != null && items.length >= 2) {
					packageName = items[1];
				}
			}
			if (packageName != null) {
				// 记录GO精品软件安装数
				StatisticsData.updateAppInstallData(context, packageName);
				Intent serviceIntent = new Intent();
				serviceIntent.putExtra(PACKAGE_NAME_KEY, packageName);
				serviceIntent.putExtra(PACKAGE_ACTION_KEY, action);
				serviceIntent.setClass(context, GoStoreUpdateInstallService.class);
				context.startService(serviceIntent);
				// 清除GO精品下载通知
				final String pkgName = packageName;
				ServiceCallbackDownload.ServiceCallbackRunnable runnable = new ServiceCallbackDownload.ServiceCallbackRunnable() {
					@Override
					public void run() {
						try {
							NotificationManager notificationManager = (NotificationManager) GOLauncherApp
									.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
							if (notificationManager != null && mDownloadController != null) {
								if (action.equals(Intent.ACTION_PACKAGE_ADDED)
										|| action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
									mDownloadController.addInstalledPackage(pkgName);
								}
								ArrayList<Long> ids = (ArrayList<Long>) mDownloadController
										.getCompleteIdsByPkgName(pkgName);
								// 移除下载完成点击安装的通知栏提示
								if (ids != null) {
									for (long id : ids) {
										notificationManager.cancel(AppDownloadListener.NOTIFY_TAG,
												(int) id);
										notificationManager.cancel(
												DefaultDownloadListener.NOTIFY_TAG, (int) id);
									}
								}
								// 判断需不需要进行回调
								String icbackurl = InstallCallbackManager.getIcbackurl(pkgName);
								if (icbackurl != null && !icbackurl.equals("")) {
									DownloadUtil.sendCBackUrl(icbackurl);
								}
								int treatment = InstallCallbackManager.getTreatment(pkgName);
								if (treatment == -1) {
									return;
								}
								if (treatment == 1) {
									// 添加安装安成打开应用的信息
									ArrayList<DownloadTask> list = (ArrayList<DownloadTask>) mDownloadController
											.getDownloadCompleteList();
									DownloadTask task = null;
									if (list == null) {
										return;
									}
									for (DownloadTask dt : list) {
										if (dt != null
												&& dt.getDownloadApkPkgName().equals(pkgName)) {
											task = dt;
											break;
										}
									}
									if (task == null) {
										return;
									}
									PackageManager pm = GOLauncherApp.getContext()
											.getPackageManager();
									if (pm == null) {
										return;
									}
									Intent in = null;
									in = pm.getLaunchIntentForPackage(pkgName);
									if (in == null) {
										return;
									}
									in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									in.setAction(android.content.Intent.ACTION_VIEW);
									PendingIntent installComplete = PendingIntent.getActivity(
											GOLauncherApp.getContext(), 0, in, 0);
									Notification notification = new Notification(
											R.drawable.open_app_notification,
											task.getDownloadName()
													+ " "
													+ GOLauncherApp.getContext().getString(
															R.string.installed_tap_to_start),
											System.currentTimeMillis());
									notification.setLatestEventInfo(GOLauncherApp.getContext(),
											task.getDownloadName(), GOLauncherApp.getContext()
													.getString(R.string.installed_tap_to_start),
											installComplete);
									notification.flags = Notification.FLAG_AUTO_CANCEL;
									notificationManager.notify(AppDownloadListener.NOTIFY_TAG,
											(int) task.getId(), notification);
								} else if (treatment == 2) {
									if (Machine.isTopActivity(GOLauncherApp.getContext(), LauncherEnv.PACKAGE_NAME)) {
										Intent in = new Intent();
										in.setClass(GOLauncherApp.getContext(),
												AppRecommendDialogActivity.class);
										in.putExtra("packageName", pkgName);
										in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										GOLauncherApp.getContext().startActivity(in);
									} else {
										//如果没有弹出激活提示框，在桌面生成快捷块
										GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
													IDiyMsgIds.ADD_STORE_RECOMMEND_ICON_AND_SHAKE, -1, pkgName, null);
									}
								} 
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				};
				ServiceCallbackDownload.callbackDownload(GOLauncherApp.getContext(), runnable);
			}
		}
	}
}
