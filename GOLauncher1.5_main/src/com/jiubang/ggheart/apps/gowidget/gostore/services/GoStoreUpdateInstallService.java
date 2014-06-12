package com.jiubang.ggheart.apps.gowidget.gostore.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.gowidget.gostore.brocastreceiver.GoStoreInstalledBrocastReceiver;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.MonitorAppstatisManager;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 */
public class GoStoreUpdateInstallService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if (intent != null) {
			final String packageName = intent
					.getStringExtra(GoStoreInstalledBrocastReceiver.PACKAGE_NAME_KEY);
			final String packageAction = intent
					.getStringExtra(GoStoreInstalledBrocastReceiver.PACKAGE_ACTION_KEY);
			if (packageName != null && !"".equals(packageName)) {
				new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						// 新安装统计
						MonitorAppstatisManager.getInstance(getApplicationContext())
								.handleAppInstalled(packageName);

						// 老安装统计
						GoStoreStatisticsUtil.doInstallStistics(GoStoreUpdateInstallService.this,
								packageName);

						// 如果是从电子市场下载，则下载完成后再统计 “下载量”
						if ("200".equals(GoStorePhoneStateUtil
								.getUid(GoStoreUpdateInstallService.this))
								|| !Machine.isCnUser(GoStoreUpdateInstallService.this)) {

							// 应用更新
							// 统计：国外，先将UI入口设置为2(详细)
							AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(
									GoStoreUpdateInstallService.this,
									AppManagementStatisticsUtil.UIENTRY_TYPE_DETAIL);
							// 统计 应用更新 下载完成
							AppManagementStatisticsUtil.getInstance().saveUpdataComplete(
									GoStoreUpdateInstallService.this,
									packageName,
									AppManagementStatisticsUtil.getInstance().getDownloadAppID(
											GoStoreUpdateInstallService.this, packageName), 1);
							// 统计 应用更新 安装完成
							AppManagementStatisticsUtil.getInstance().saveUpdataSetup(
									GoStoreUpdateInstallService.this, packageName);

							// 应用推荐：
							// 统计:先保存推荐界面入口
							AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(
									GoStoreUpdateInstallService.this,
									AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);

							// 统计:应用推荐--下载/更新完成
							AppRecommendedStatisticsUtil.getInstance().saveDownloadComplete(
									GoStoreUpdateInstallService.this,
									packageName,
									AppRecommendedStatisticsUtil.getInstance().getDownloadAppID(
											GoStoreUpdateInstallService.this, packageName), 1);

							// 统计：应用推荐--安装完成
							AppRecommendedStatisticsUtil.getInstance().saveDownloadSetup(
									GoStoreUpdateInstallService.this, packageName);

						} else {
							// 统计 应用更新 安装完成
							AppManagementStatisticsUtil.getInstance().saveUpdataSetup(
									GoStoreUpdateInstallService.this, packageName);

							// 统计：应用推荐--安装完成
							AppRecommendedStatisticsUtil.getInstance().saveDownloadSetup(
									GoStoreUpdateInstallService.this, packageName);
						}

						stopSelf();
					}

				}.start();
			}
		}
	}

	public void onStart2(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			final String packageName = intent
					.getStringExtra(GoStoreInstalledBrocastReceiver.PACKAGE_NAME_KEY);
			if (packageName != null && !"".equals(packageName)) {
				new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						MonitorAppstatisManager.getInstance(getApplicationContext())
								.handleAppInstalled(packageName);

						stopSelf();
					}
				}.start();
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
