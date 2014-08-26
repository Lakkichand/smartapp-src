package com.zhidian.wifibox.receiver;

import java.util.ArrayList;
import java.util.List;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.ActivateCountController;
import com.zhidian.wifibox.controller.DownloadController;
import com.zhidian.wifibox.controller.DownloadCountController;
import com.zhidian.wifibox.controller.DownloadSpeedController;
import com.zhidian.wifibox.controller.InstallCountController;
import com.zhidian.wifibox.controller.MarketStartController;
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.data.AppctivateCount;
import com.zhidian.wifibox.data.DownloadSpeed;
import com.zhidian.wifibox.data.SpkStart;
import com.zhidian.wifibox.db.dao.AppActivateCountDao;
import com.zhidian.wifibox.db.dao.AppDownloadCountDao;
import com.zhidian.wifibox.db.dao.AppDownloadDao;
import com.zhidian.wifibox.db.dao.AppDownloadSpeedDao;
import com.zhidian.wifibox.db.dao.AppInstallCountDao;
import com.zhidian.wifibox.db.dao.SpkStartDao;
import com.zhidian.wifibox.util.CheckNetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 检测数据库中未上传成功的数据，并重新上传数据到服务器
 * 
 * @author zhaoyl
 * 
 */
public class CheckSQLiteDataReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("alarm.checksqlite.action")) {

			if (CheckNetwork.isConnect(context)) {// 有网络

				// app下载成功量统计
				AppDownloadCountDao downloadDao = new AppDownloadCountDao(
						context);
				List<AppDownloadCount> downloadList = downloadDao.getSpkData();
				if (downloadList != null) {
					for (int i = 0; i < downloadList.size(); i++) {
						AppDownloadCount downloadBean = downloadList.get(i);
						UnloadDownloadData(downloadBean, context);
					}
				}

				// app下载量统计

				AppDownloadDao dDao = new AppDownloadDao(context);
				List<AppDownloadCount> dList = dDao.getSpkData();
				if (dList != null) {
					for (int i = 0; i < dList.size(); i++) {
						AppDownloadCount dbean = dList.get(i);
						unLoadDData(dbean, context);
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

				// 市场启动量统计
				SpkStartDao spkStartDao = new SpkStartDao(context);
				List<SpkStart> startList = spkStartDao.getAllData();
				if (startList != null) {
					for (int i = 0; i < startList.size(); i++) {
						SpkStart spkbean = startList.get(i);
						UnloadSpkStartData(spkbean, context);
					}
				}

				// app下载速度统计
				AppDownloadSpeedDao speedDao = new AppDownloadSpeedDao(context);
				List<DownloadSpeed> speedList = speedDao.getAllData();
				// 每次最多传500条，如超多500，则分多次传送数据到服务器
				if (speedList != null && speedList.size() > 0) {
					if (speedList.size() <= 500) {
						UnloadDownloadSpeed(speedList, context);
					} else {
						int count = speedList.size();
						Log.d("CheckSQLiteDataReceiver", "共有：" + count + "条数据");
						int lun = count / 500;
						Log.d("CheckSQLiteDataReceiver", "lun：" + lun);
						int yu = count % 500;
						Log.d("CheckSQLiteDataReceiver", "yu：" + yu);
						if (yu > 0) {
							lun = lun + 1;
						}
						Log.d("CheckSQLiteDataReceiver", "lun：" + lun);
						int currentIndex = 0;
						for (int i = 0; i < lun; i++) {
							List<DownloadSpeed> list = new ArrayList<DownloadSpeed>();
							for (int j = 0; j < 500; j++) {
								if (currentIndex < speedList.size()) {
									list.add(speedList.get(currentIndex));
									currentIndex++;
								}

							}
							// 上传list
							Log.d("CheckSQLiteDataReceiver",
									"list大小：" + list.size());
							UnloadDownloadSpeed(list, context);
						}

					}

				}
			}

		}
	}

	/**
	 * 上传app下载速度统计数据
	 * 
	 * @param speedList
	 * @param context
	 */
	private void UnloadDownloadSpeed(List<DownloadSpeed> speedList,
			Context context) {
		TAApplication.getApplication()
				.doCommand(
						context.getString(R.string.downloadspeedcontroller),
						new TARequest(DownloadSpeedController.DOWNLOAD_SPEED,
								speedList), new TAIResponseListener() {

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
	 * 上传app下载数据
	 * 
	 * @param dbean
	 * @param context
	 */
	private void unLoadDData(AppDownloadCount dbean, Context context) {
		TAApplication.getApplication().doCommand(
				context.getString(R.string.downloadcontroller),
				new TARequest(DownloadController.SQLITE_DOWNLOAD, dbean),
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
	 * 上传市场启动量数据
	 * 
	 * @param spkbean
	 * @param context
	 */
	private void UnloadSpkStartData(SpkStart spkbean, Context context) {
		TAApplication.getApplication().doCommand(
				context.getString(R.string.marketstartcontroller),
				new TARequest(MarketStartController.START_MARKET_SQLITE,
						spkbean), new TAIResponseListener() {

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
	 * 上传app激活数据
	 * 
	 * @param downloadBean
	 */
	private void UnloadAppctivateData(AppctivateCount ativitBean,
			Context context) {
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
	 * 上传app下载成功量数据
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

}
