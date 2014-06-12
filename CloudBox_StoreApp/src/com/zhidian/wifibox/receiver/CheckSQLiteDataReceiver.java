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
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.data.AppctivateCount;
import com.zhidian.wifibox.db.dao.AppActivateCountDao;
import com.zhidian.wifibox.db.dao.AppDownloadCountDao;
import com.zhidian.wifibox.db.dao.AppInstallCountDao;
import com.zhidian.wifibox.util.CheckNetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 检测数据库中未上传成功的数据，并重新上传数据到服务器
 * 
 * @author zhaoyl
 * 
 */
public class CheckSQLiteDataReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals("alarm.checksqlite.action")) {

			if (CheckNetwork.isConnect(context)) {// 有网络

				// app下载量统计
				AppDownloadCountDao downloadDao = new AppDownloadCountDao(
						context);
				List<AppDownloadCount> downloadList = downloadDao.getSpkData();
				if (downloadList != null) {
					for (int i = 0; i < downloadList.size(); i++) {
						AppDownloadCount downloadBean = downloadList.get(i);
						UnloadDownloadData(downloadBean, context);
					}
				}

				// app安装量、卸载量统计
				AppInstallCountDao appInstallCountDao = new AppInstallCountDao(
						context);
				List<AppInstallBean> installList = appInstallCountDao.getData();
				if (installList != null) {
					for (int i = 0; i < installList.size(); i++) {
						AppInstallBean installBean = installList.get(i);
						UnloadAppInstallData(installBean, context);
					}
				}

				// app激活量统计
				AppActivateCountDao dao = new AppActivateCountDao(context);
				List<AppctivateCount> actiList = dao.getSpkData();
				if (actiList != null) {
					for (int i = 0; i < actiList.size(); i++) {
						AppctivateCount ativitBean = actiList.get(i);
						UnloadAppctivateData(ativitBean, context);
					}
				}
			}

		}
	}

	/**
	 * 上传app激活数据
	 * 
	 * @param downloadBean
	 */
	private void UnloadAppctivateData(AppctivateCount ativitBean,
			Context context) {
		// TODO Auto-generated method stub
		TAApplication.getApplication().doCommand(
				context.getString(R.string.activatecountcontroller),
				new TARequest(ActivateCountController.SQLITEACTIVATECOUNT,
						ativitBean), new TAIResponseListener() {

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
		// TODO Auto-generated method stub
		TAApplication.getApplication().doCommand(
				context.getString(R.string.installcountcontroller),
				new TARequest(InstallCountController.SQLITE_INSTALLCOUNT,
						installBean), new TAIResponseListener() {

					@Override
					public void onStart() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(TAResponse response) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onRuning(TAResponse response) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onFailure(TAResponse response) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onFinish() {
						// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
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

}
