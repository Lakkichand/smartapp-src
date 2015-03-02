package com.escape.uninstaller.controller;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.os.Debug;
import android.os.RemoteException;
import android.text.format.Formatter;

import com.escape.uninstaller.data.AppDataBean;
import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;

public class MainController extends TACommand {

	public static final String TAG = MainController.class.getName();

	public static final String GETPACKAGESIZEINFO = "GETPACKAGESIZEINFO";

	public static final String CALCULATERAM = "CALCULATERAM";

	private Context mContext = TAApplication.getApplication();

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(GETPACKAGESIZEINFO)) {
			AppDataBean[] beans = (AppDataBean[]) request.getData();
			if (beans == null || beans.length <= 0) {
				return;
			}
			for (final AppDataBean bean : beans) {
				try {
					Method getPackageSizeInfo = mContext
							.getPackageManager()
							.getClass()
							.getDeclaredMethod("getPackageSizeInfo",
									String.class, IPackageStatsObserver.class);
					getPackageSizeInfo.invoke(mContext.getPackageManager(),
							bean.pkgName, new IPackageStatsObserver.Stub() {

								@Override
								public void onGetStatsCompleted(
										PackageStats pStats, boolean succeeded)
										throws RemoteException {
									bean.size = pStats.cacheSize
											+ pStats.codeSize + pStats.dataSize;
									bean.size_str = Formatter.formatFileSize(
											mContext, bean.size);
									sendSuccessMessage(bean);
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (command.equals(CALCULATERAM)) {
			AppDataBean[] beans = (AppDataBean[]) request.getData();
			if (beans == null || beans.length <= 0) {
				return;
			}
			ActivityManager activityManager = (ActivityManager) TAApplication
					.getApplication()
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> list = activityManager
					.getRunningAppProcesses();
			Map<String, AppDataBean> map = new HashMap<String, AppDataBean>();
			for (final AppDataBean bean : beans) {
				bean.ram = 0;
				bean.ram_str = "";
				map.put(bean.pkgName, bean);
			}
			for (ActivityManager.RunningAppProcessInfo info : list) {
				boolean ca = false;
				for (String pkg : info.pkgList) {
					if (map.keySet().contains(pkg)) {
						ca = true;
						break;
					}
				}
				if (ca) {
					int[] myMempid = new int[] { info.pid };
					Debug.MemoryInfo[] memoryInfo = activityManager
							.getProcessMemoryInfo(myMempid);
					int memSize = memoryInfo[0].getTotalPss();
					int aMemSize = memSize / info.pkgList.length;
					for (String pkg : info.pkgList) {
						AppDataBean bean = map.get(pkg);
						if (bean != null) {
							bean.ram = bean.ram + aMemSize;
							bean.ram_str = Formatter.formatFileSize(mContext,
									bean.ram);
						}
					}
				}
			}
			sendSuccessMessage(beans);
		}
	}

}
