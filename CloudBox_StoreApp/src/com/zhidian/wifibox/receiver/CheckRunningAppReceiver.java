package com.zhidian.wifibox.receiver;

import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.ActivateCountController;
import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.db.dao.AppPackageDao;
import com.zhidian.wifibox.util.Setting;

/**
 * 检测应用激活，上传数据到服务器
 * 
 * @author zhaoyl
 * 
 */
public class CheckRunningAppReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("alarm.check.action")) {
			Setting setting = new Setting(TAApplication.getApplication());
			long t = setting.getLong(Setting.FIRST_RECORD_RUNNINGAPP);
			if (t <= 0) {
				setting.putLong(Setting.FIRST_RECORD_RUNNINGAPP,
						System.currentTimeMillis());
			}
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			// 激活量从市场已下载app信息表判断
			try {
				ComponentName topActivity = am.getRunningTasks(1).get(0).topActivity;
				String packageName = topActivity.getPackageName();
				if (!context.getPackageName().equals(packageName)) {
					setting.putLong(
							packageName + Setting.APP_LAST_RUNNING_TIME,
							System.currentTimeMillis());
				}
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

	/**
	 * 判断是系统应用还是三方应用
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

}
