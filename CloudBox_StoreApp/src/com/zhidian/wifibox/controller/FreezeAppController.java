package com.zhidian.wifibox.controller;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

/**
 * 回收站控制器
 * 
 * @author xiedezhi
 * 
 */
public class FreezeAppController extends TACommand {
	/**
	 * 扫描应用
	 */
	public static final String SCAN = "FREEZEAPPCONTROLLER_SCAN";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(SCAN)) {
			List<AppUninstallBean> list = new ArrayList<AppUninstallBean>();
			PackageManager pManager = TAApplication.getApplication()
					.getPackageManager();
			List<PackageInfo> packlist = null;
			try {
				packlist = pManager.getInstalledPackages(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (packlist != null) {
				final CountDownLatch countdownlatch = new CountDownLatch(
						packlist.size());
				final int totalCount = packlist.size();
				for (PackageInfo info : packlist) {
					int state = pManager
							.getApplicationEnabledSetting(info.packageName);
					// 被冻结系统应用
					if (((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
							&& (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)) {
						final AppUninstallBean bean = new AppUninstallBean();
						bean.packname = info.packageName;
						bean.appname = info.applicationInfo.loadLabel(pManager)
								+ "";
						bean.isSelect = false;
						bean.installTime = (new File(
								info.applicationInfo.sourceDir)).lastModified();
						bean.isSystemApp = true;
						try {
							Method getPackageSizeInfo = pManager.getClass()
									.getDeclaredMethod("getPackageSizeInfo",
											String.class,
											IPackageStatsObserver.class);
							getPackageSizeInfo.invoke(pManager, bean.packname,
									new IPackageStatsObserver.Stub() {

										@Override
										public void onGetStatsCompleted(
												PackageStats pStats,
												boolean succeeded)
												throws RemoteException {
											bean.size = pStats.cacheSize
													+ pStats.codeSize
													+ pStats.dataSize;
											countdownlatch.countDown();
											int count = (int) countdownlatch
													.getCount();
											sendRuntingMessage((int) ((totalCount - count)
													* 1.0 / totalCount * 100.0 + 0.5));
										}
									});
						} catch (Exception e) {
							e.printStackTrace();
							bean.size = (new File(
									info.applicationInfo.sourceDir)).length()
									+ (new File(info.applicationInfo.dataDir))
											.length();
							countdownlatch.countDown();
							int count = (int) countdownlatch.getCount();
							sendRuntingMessage((int) ((totalCount - count)
									* 1.0 / totalCount * 100.0 + 0.5));
						}
						list.add(bean);
					} else {
						countdownlatch.countDown();
						int count = (int) countdownlatch.getCount();
						sendRuntingMessage((int) ((totalCount - count) * 1.0
								/ totalCount * 100.0 + 0.5));
					}
				}
				try {
					countdownlatch.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			sendSuccessMessage(list);
		}
	}

}
