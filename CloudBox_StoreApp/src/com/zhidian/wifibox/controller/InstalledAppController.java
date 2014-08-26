package com.zhidian.wifibox.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.data.AppInfo;
import com.zhidian.wifibox.util.AppInfoProvider;

/**
 * 获取已安装APP控制器
 * 
 * @author zhaoyl
 * 
 */
public class InstalledAppController extends TACommand {

	public static final String INSTALLED_APP = "INSTALLED_APP";
	private static final String MYSELF_PACKNAME = "com.zhidian.wifibox";//过滤掉本应用

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(INSTALLED_APP)) {
			PackageManager packmanager = TAApplication.getApplication()
					.getPackageManager();
			HashMap<String, List<AppInfo>> map = new HashMap<String, List<AppInfo>>();
			List<AppInfo> userappInfo = new ArrayList<AppInfo>();// 个人应用
			List<AppInfo> systemappInfo = new ArrayList<AppInfo>();// 系统应用

			List<PackageInfo> packinfos = packmanager
					.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

			List<String> disableList = AppInfoProvider.filterDisabled(); // 获得已被停用的系统应用;
			// TODO

			for (PackageInfo info : packinfos) {
				if (TAApplication.getApplication().getPackageName()
						.equals(info.applicationInfo.packageName)) {
					continue;
				}
				int index = packinfos.indexOf(info);
				int progress = (int) (index * 1.0 / packinfos.size() * 100.0 + 0.5);
				sendRuntingMessage(progress);

				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				AppInfo myApp = new AppInfo();
				String packname = info.packageName;
				myApp.setPackname(packname);
				ApplicationInfo appinfo = info.applicationInfo;
				// Drawable icon = appinfo.loadIcon(packmanager);
				// myApp.setIcon(icon);
				String appname = appinfo.loadLabel(packmanager).toString();
				myApp.setAppname(appname);

				if (AppInfoProvider.filterApp(appinfo)) {// 非系统应用
					if (!MYSELF_PACKNAME.equals(packname)) {
						myApp.setSystemApp(false);
						userappInfo.add(myApp);
					}
					
				} else {
					if (null != disableList && disableList.size() > 0) {
						if (!disableList.contains(packname)) {
							myApp.setSystemApp(true);
							systemappInfo.add(myApp);
						}

					} else {
						myApp.setSystemApp(true);
						systemappInfo.add(myApp);
					}

				}

			}

			map.put("user", userappInfo);
			map.put("system", systemappInfo);
			sendSuccessMessage(map);
		}

	}

}
