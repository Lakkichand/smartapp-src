package com.zhidian.wifibox.controller;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.data.AppUninstallBean;
import com.zhidian.wifibox.data.AppUninstallGroup;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.Setting;

/**
 * 应用卸载控制器
 * 
 * @author xiedezhi
 * 
 */
public class AppUninstallController extends TACommand {
	/**
	 * 扫描应用
	 */
	public static final String SCAN = "APPUNINSTALLCONTROLLER_SCAN";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(SCAN)) {
			List<AppUninstallBean> userappInfo = new ArrayList<AppUninstallBean>();// 个人应用
			List<AppUninstallGroup> userappGroup = new ArrayList<AppUninstallGroup>();
			List<AppUninstallBean> systemappInfo = new ArrayList<AppUninstallBean>();// 系统应用

			PackageManager packmanager = TAApplication.getApplication()
					.getPackageManager();
			List<PackageInfo> packinfos = packmanager.getInstalledPackages(0);
			Setting setting = new Setting(TAApplication.getApplication());

			final CountDownLatch countdownlatch = new CountDownLatch(
					packinfos.size());
			final int totalCount = packinfos.size();
			for (PackageInfo info : packinfos) {
				if (TAApplication.getApplication().getPackageName()
						.equals(info.packageName)) {
					countdownlatch.countDown();
					continue;
				}
				int state = packmanager
						.getApplicationEnabledSetting(info.packageName);
				if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
						|| state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
					countdownlatch.countDown();
					continue;
				}
				final AppUninstallBean myApp = new AppUninstallBean();
				myApp.packname = info.packageName;
				ApplicationInfo appinfo = info.applicationInfo;
				myApp.appname = appinfo.loadLabel(packmanager).toString();
				myApp.lastOpenTime = setting.getLong(info.packageName
						+ Setting.APP_LAST_RUNNING_TIME);
				myApp.installTime = new File(info.applicationInfo.sourceDir)
						.lastModified();
				if (AppUtils.isSystemApp(TAApplication.getApplication(),
						myApp.packname)) {
					myApp.isSystemApp = true;
					systemappInfo.add(myApp);
				} else {
					myApp.isSystemApp = false;
					userappInfo.add(myApp);
				}
				try {
					Method getPackageSizeInfo = packmanager.getClass()
							.getDeclaredMethod("getPackageSizeInfo",
									String.class, IPackageStatsObserver.class);
					getPackageSizeInfo.invoke(packmanager, myApp.packname,
							new IPackageStatsObserver.Stub() {

								@Override
								public void onGetStatsCompleted(
										PackageStats pStats, boolean succeeded)
										throws RemoteException {
									myApp.size = pStats.cacheSize
											+ pStats.codeSize + pStats.dataSize;
									countdownlatch.countDown();
									int count = (int) countdownlatch.getCount();
									sendRuntingMessage((int) ((totalCount - count)
											* 1.0 / totalCount * 100.0 + 0.5));
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
					myApp.size = (new File(info.applicationInfo.sourceDir))
							.length()
							+ (new File(info.applicationInfo.dataDir)).length();
					countdownlatch.countDown();
					int count = (int) countdownlatch.getCount();
					sendRuntingMessage((int) ((totalCount - count) * 1.0
							/ totalCount * 100.0 + 0.5));
				}
			}
			try {
				countdownlatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long firstRecord = setting.getLong(Setting.FIRST_RECORD_RUNNINGAPP);
			long now = System.currentTimeMillis();
			if (now - firstRecord <= 7 * 24 * 60 * 60 * 1000L) {
				// 按安装时间排序
				Collections.sort(userappInfo,
						new Comparator<AppUninstallBean>() {
							public int compare(AppUninstallBean arg0,
									AppUninstallBean arg1) {
								if (arg0.installTime == arg1.installTime) {
									return 0;
								}
								return (arg0.installTime - arg1.installTime > 0 ? -1
										: 1);
							}
						});
				AppUninstallGroup group = new AppUninstallGroup();
				group.mList = userappInfo;
				group.type = 2;
				group.mTitle = "最近安装的应用";
				userappGroup.add(group);
			} else {
				// 最近使用
				List<AppUninstallBean> list1 = new ArrayList<AppUninstallBean>();
				// 最近7天未使用
				List<AppUninstallBean> list2 = new ArrayList<AppUninstallBean>();
				// 最近30天未使用
				List<AppUninstallBean> list3 = new ArrayList<AppUninstallBean>();
				for (AppUninstallBean ab : userappInfo) {
					long gap = now - ab.lastOpenTime;
					if (now - ab.lastOpenTime <= 7 * 24 * 60 * 60 * 1000L) {
						// 最近使用
						list1.add(ab);
					} else if (gap > 7 * 24 * 60 * 60 * 1000L
							&& gap <= 30 * 24 * 60 * 60 * 1000L) {
						// 最近7天未使用
						list2.add(ab);
					} else {
						// 最近30天未使用
						list3.add(ab);
					}
				}
				Collections.sort(list1, new Comparator<AppUninstallBean>() {
					public int compare(AppUninstallBean arg0,
							AppUninstallBean arg1) {
						if (arg0.lastOpenTime == arg1.lastOpenTime) {
							return 0;
						}
						return (arg0.lastOpenTime - arg1.lastOpenTime > 0 ? 1
								: -1);
					}
				});
				Collections.sort(list2, new Comparator<AppUninstallBean>() {
					public int compare(AppUninstallBean arg0,
							AppUninstallBean arg1) {
						if (arg0.lastOpenTime == arg1.lastOpenTime) {
							return 0;
						}
						return (arg0.lastOpenTime - arg1.lastOpenTime > 0 ? 1
								: -1);
					}
				});
				Collections.sort(list3, new Comparator<AppUninstallBean>() {
					public int compare(AppUninstallBean arg0,
							AppUninstallBean arg1) {
						if (arg0.lastOpenTime == arg1.lastOpenTime) {
							return 0;
						}
						return (arg0.lastOpenTime - arg1.lastOpenTime > 0 ? 1
								: -1);
					}
				});
				if (list3.size() > 0) {
					AppUninstallGroup group = new AppUninstallGroup();
					group.mList.addAll(list3);
					group.mTitle = "30天未使用的应用";
					group.type = 1;
					userappGroup.add(group);
				}
				if (list2.size() > 0) {
					AppUninstallGroup group = new AppUninstallGroup();
					group.mList.addAll(list2);
					group.mTitle = "7天未使用的应用";
					group.type = 1;
					userappGroup.add(group);
				}
				if (list1.size() > 0) {
					AppUninstallGroup group = new AppUninstallGroup();
					group.mList.addAll(list1);
					group.mTitle = "经常使用的应用";
					group.type = 1;
					userappGroup.add(group);
				}
			}
			// 系统应用排序
			Collections.sort(systemappInfo, new Comparator<AppUninstallBean>() {
				public int compare(AppUninstallBean arg0, AppUninstallBean arg1) {
					if (arg0.size == arg1.size) {
						return 0;
					}
					return (arg0.size - arg1.size > 0 ? -1 : 1);
				}
			});
			sendSuccessMessage(new Object[] { userappGroup, systemappInfo });
		}
	}
}
