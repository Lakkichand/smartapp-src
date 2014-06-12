package com.zhidian.wifibox.receiver;

import java.util.List;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.ActivateCountController;
import com.zhidian.wifibox.controller.DownloadCountController;
import com.zhidian.wifibox.controller.InstallCountController;
import com.zhidian.wifibox.controller.MarketInstallController;
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.db.dao.AppDownloadCountDao;
import com.zhidian.wifibox.db.dao.AppInstallCountDao;
import com.zhidian.wifibox.db.dao.AppPackageDao;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.util.Setting;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * 检测应用激活，上传数据到服务器
 * 
 * @author zhaoyl
 * 
 */
public class CheckRunningAppReceiver extends BroadcastReceiver {

	// private static final String TAG =
	// CheckRunningAppReceiver.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("alarm.check.action")) {
			if (CheckNetwork.isConnect(context)) {
				// 检测市场安装数据有没有上传
				firstInstallUserInfo(context);
			}

			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);

			try {
				ComponentName topActivity = am.getRunningTasks(1).get(0).topActivity;
				String packageName = topActivity.getPackageName();
				String runPackageName = TAApplication.runPackageName;
				Log.i("正在运行：", packageName);
				Log.i("已记录：", runPackageName);
				if (!runPackageName.equals(packageName)) {
					List<String> downloadList = TAApplication.dao
							.queryPagName();
					if (downloadList != null) {
						for (int i = 0; i < downloadList.size(); i++) {
							if (packageName.equals(downloadList.get(i))) {
								AppPackageDao dao = new AppPackageDao(context);
								AppInstallBean bean = dao
										.queryPackage(packageName);
								if (bean != null) {

									// 检测数据库中是否有数据
									CheckSQL(context);
									// 上传激活数据
									gotoUploading(bean, context);

								}
							}
						}
					}
				}

				TAApplication.runPackageName = packageName;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void CheckSQL(Context context) {
		// app下载量统计
		AppDownloadCountDao downloadDao = new AppDownloadCountDao(context);
		List<AppDownloadCount> download = downloadDao.getSpkData();
		if (download != null) {
			for (int j = 0; j < download.size(); j++) {
				AppDownloadCount downloadBean = download.get(j);
				UnloadDownloadData(downloadBean, context);
			}
		}

		// app安装量、卸载量统计
		AppInstallCountDao appInstallCountDao = new AppInstallCountDao(context);
		List<AppInstallBean> installList = appInstallCountDao.getData();
		if (installList != null) {
			for (int k = 0; k < installList.size(); k++) {
				AppInstallBean installBean = installList.get(k);
				UnloadAppInstallData(installBean, context);
			}
		}
	}

	/**
	 * 判断是系统应用还是三方应用
	 * 
	 * @param info
	 * @return true表示为系统应用
	 */
	public boolean filterApp(ApplicationInfo info) {
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 上传激活数据到数据库
	 * 
	 * @param bean
	 * @param context
	 */
	private void gotoUploading(AppInstallBean bean, Context context) {
		TAApplication.getApplication().doCommand(
				context.getString(R.string.activatecountcontroller),
				new TARequest(ActivateCountController.ACTIVATECOUNT, bean),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {

					}
				}, true, false);
	}

	/**
	 * 上传app安装、卸载数据
	 * 
	 * @param downloadBean
	 */
	private void UnloadAppInstallData(AppInstallBean installBean,
			Context context) {
		TAApplication.getApplication().doCommand(
				context.getString(R.string.installcountcontroller),
				new TARequest(InstallCountController.SQLITE_INSTALLCOUNT,
						installBean), new TAIResponseListener() {

					@Override
					public void onStart() {

					}

					@Override
					public void onSuccess(TAResponse response) {

					}

					@Override
					public void onRuning(TAResponse response) {

					}

					@Override
					public void onFailure(TAResponse response) {

					}

					@Override
					public void onFinish() {

					}
				}, true, false);
	}

	/**
	 * 上传app下载数据
	 * 
	 * @param downloadBean
	 */
	private void UnloadDownloadData(AppDownloadCount downloadBean,
			Context context) {
		TAApplication.getApplication().doCommand(
				context.getString(R.string.downloadcountcontroller),
				new TARequest(DownloadCountController.SQLITE_DOWNLOADCOUNT,
						downloadBean), new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {

					}
				}, true, false);
	}

	/**
	 * 首次安装调用插件--市场spk安装接口
	 */
	private void firstInstallUserInfo(Context context) {
		Setting setting = new Setting(context);
		boolean b = setting.getBoolean(Setting.INSTALL_STATUS);
		if (!b) {// 表示还没上传数据到服务器
			TAApplication.getApplication()
					.doCommand(
							TAApplication.getApplication().getString(
									R.string.marketinstallcontroller),
							new TARequest(
									MarketInstallController.INSTALL_MARKET,
									null), new TAIResponseListener() {

								@Override
								public void onSuccess(TAResponse response) {
								}

								@Override
								public void onStart() {
								}

								@Override
								public void onRuning(TAResponse response) {
								}

								@Override
								public void onFinish() {
								}

								@Override
								public void onFailure(TAResponse response) {
								}
							}, true, false);
		}
	}

}
