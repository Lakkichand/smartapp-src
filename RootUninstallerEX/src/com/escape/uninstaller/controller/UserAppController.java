package com.escape.uninstaller.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.escape.uninstaller.data.AppDataBean;
import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;

public class UserAppController extends TACommand {

	public static final String TAG = UserAppController.class.getName();

	public static final String SCANUSERAPP = "SCANUSERAPP";

	private Context mContext = TAApplication.getApplication();

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(SCANUSERAPP)) {
			List<AppDataBean> ret = new ArrayList<AppDataBean>();
			PackageManager pm = mContext.getPackageManager();
			List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
			for (PackageInfo info : packageInfos) {
				String packageName = info.packageName;
				if (packageName != null
						&& packageName.equals(mContext.getPackageName())) {
					// 过滤掉自己
					continue;
				}
				ApplicationInfo app = info.applicationInfo;
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
					continue;
				}
				int state = pm.getApplicationEnabledSetting(packageName);
				if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
						|| state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
					continue;
				}
				AppDataBean bean = new AppDataBean();
				bean.pkgName = info.packageName;
				bean.name = info.applicationInfo.loadLabel(pm).toString()
						.trim();
				bean.date = new File(info.applicationInfo.sourceDir)
						.lastModified();
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd");
				Date curDate = new Date(bean.date);// 获取当前时间
				bean.date_str = formatter.format(curDate);
				bean.versionName = info.versionName;
				bean.versionCode = info.versionCode;
				ret.add(bean);
			}
			sendSuccessMessage(ret);
		}
	}

}
