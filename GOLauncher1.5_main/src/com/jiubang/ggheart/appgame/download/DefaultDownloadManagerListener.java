/*
 * 文 件 名:  DefaultDownloadManagerListener.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-17
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.util.ArrayList;

import android.os.RemoteException;

import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-17]
 */
public class DefaultDownloadManagerListener extends IAidlDownloadManagerListener.Stub {

	@Override
	public void onStartDownloadTask(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoveDownloadTask(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		handleRemoveStatistics(task);
	}

	@Override
	public void onRestartDownloadTask(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFailDownloadTask(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		int module = task.getModule();
		if (module == AppsDetail.START_TYPE_DOWNLOAD_GO) {
			ArrayList<Exception> list = task.getExceptionList();
			for (Exception e : list) {
				String ip2 = e.getMessage();
				GoStoreOperatorUtil.sendDownloadFailNotification(GOLauncherApp.getContext(), ip2,
						task.getDownloadName());
			}
//			Exception e = task.getmException();
//			String ip2 = e.getMessage();
//			GoStoreOperatorUtil.sendDownloadFailNotification(GOLauncherApp.getContext(), ip2,
//					task.getDownloadName());
		}
	}

	private void handleRemoveStatistics(DownloadTask downloadTask) {
		if (downloadTask == null) {
			return;
		}
		// 此处接口有隐患：1.必须确保包名无误 2.必须确保task的id就是appId
		int module = downloadTask.getModule();
		int state = downloadTask.getState();
		switch (state) {
			case DownloadTask.STATE_DELETE :
				if (downloadTask.getId() != Long.MIN_VALUE && module != Integer.MAX_VALUE) {
					if (module == AppsDetail.START_TYPE_APPMANAGEMENT) {
						AppManagementStatisticsUtil.getInstance().saveDownloadComplete(
								GOLauncherApp.getContext(), downloadTask.getDownloadApkPkgName(),
								String.valueOf(downloadTask.getId()), 0);
					} else if (module == AppsDetail.START_TYPE_APPRECOMMENDED
							|| module == AppsDetail.START_TYPE_WIDGET_APP
							|| module == AppsDetail.START_TYPE_APPFUNC_SEARCH
							|| module == AppsDetail.START_TYPE_GO_SEARCH_WIDGET) {
						AppRecommendedStatisticsUtil.getInstance().saveDownloadComplete(
								GOLauncherApp.getContext(), downloadTask.getDownloadApkPkgName(),
								String.valueOf(downloadTask.getId()), 0);
					}
				}
				break;
			case DownloadTask.STATE_FINISH :
				StatisticsData.countStatData(GOLauncherApp.getContext(),
						StatisticsData.KEY_DOWNLOAD_COMPLETE_ALL);
				if (downloadTask.getId() != Long.MIN_VALUE && module != Integer.MAX_VALUE) {
					if (module == AppsDetail.START_TYPE_APPMANAGEMENT) {
						AppManagementStatisticsUtil.getInstance().saveUpdataComplete(
								GOLauncherApp.getContext(), downloadTask.getDownloadApkPkgName(),
								String.valueOf(downloadTask.getId()), 1);
					} else if (module == AppsDetail.START_TYPE_APPRECOMMENDED
							|| module == AppsDetail.START_TYPE_WIDGET_APP
							|| module == AppsDetail.START_TYPE_APPFUNC_SEARCH
							|| module == AppsDetail.START_TYPE_GO_SEARCH_WIDGET) {
						AppRecommendedStatisticsUtil.getInstance().saveDownloadComplete(
								GOLauncherApp.getContext(), downloadTask.getDownloadApkPkgName(),
								String.valueOf(downloadTask.getId()), 1);
					}
				}
				break;
			default :
				break;
		}
	}
}
