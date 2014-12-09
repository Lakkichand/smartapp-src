package com.escape.uninstaller.controller;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
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
		}
	}

}
