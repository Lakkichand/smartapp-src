package com.zhidian.wifibox.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
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

/**
 * 统计相关的逻辑处理
 * 
 * @author xiedezhi
 * 
 */
public class StatisticsController extends TACommand {
	/**
	 * 上传所有统计数据
	 */
	public static final String UPLOAD_ALL_DATA = "STATISTICSCONTROLLER_UPLOAD_ALL_DATA";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(UPLOAD_ALL_DATA)) {
			uploadAllData();
		}
	}

	/**
	 * 插件上传数据到服务端
	 */
	private void uploadAllData() {
		// 首次进入调用激活应用接口
		// app下载成功量统计
		AppDownloadCountDao downloadDao = new AppDownloadCountDao(
				TAApplication.getApplication());
		List<AppDownloadCount> downloadList = downloadDao.getSpkData();
		if (downloadList != null && downloadList.size() > 0) {
			for (int i = 0; i < downloadList.size(); i++) {
				AppDownloadCount downloadBean = downloadList.get(i);
				UnloadDownloadData(downloadBean, TAApplication.getApplication());
			}
		}

		// app下载量统计
		AppDownloadDao dDao = new AppDownloadDao(TAApplication.getApplication());
		List<AppDownloadCount> dList = dDao.getSpkData();
		if (dList != null && dList.size() > 0) {
			for (int i = 0; i < dList.size(); i++) {
				AppDownloadCount dbean = dList.get(i);
				unLoadDData(dbean, TAApplication.getApplication());
			}
		}

		// app安装量、卸载量统计
		AppInstallCountDao appInstallCountDao = new AppInstallCountDao(
				TAApplication.getApplication());
		List<AppInstallBean> installList = appInstallCountDao.getData();
		if (installList != null && installList.size() > 0) {
			for (int i = 0; i < installList.size(); i++) {
				AppInstallBean installBean = installList.get(i);
				UnloadAppInstallData(installBean,
						TAApplication.getApplication());
			}
		}

		// app激活量统计
		AppActivateCountDao dao = new AppActivateCountDao(
				TAApplication.getApplication());
		List<AppctivateCount> actiList = dao.getSpkData();
		if (actiList != null && actiList.size() > 0) {
			for (int i = 0; i < actiList.size(); i++) {
				AppctivateCount ativitBean = actiList.get(i);
				UnloadAppctivateData(ativitBean, TAApplication.getApplication());
			}
		}

		// 市场启动量统计
		SpkStartDao spkStartDao = new SpkStartDao(
				TAApplication.getApplication());
		List<SpkStart> startList = spkStartDao.getAllData();
		if (startList != null && startList.size() > 0) {
			for (int i = 0; i < startList.size(); i++) {
				SpkStart spkbean = startList.get(i);
				UnloadSpkStartData(spkbean, TAApplication.getApplication());
			}
		}

		// app下载速度统计
		AppDownloadSpeedDao speedDao = new AppDownloadSpeedDao(
				TAApplication.getApplication());
		List<DownloadSpeed> speedList = speedDao.getAllData();
		// 每次最多传500条，如超多500，则分多次传送数据到服务器
		if (speedList != null && speedList.size() > 0) {
			if (speedList.size() <= 500) {
				UnloadDownloadSpeed(speedList, TAApplication.getApplication());
			} else {
				int count = speedList.size();
				Log.d("StatisticsController", "共有：" + count + "条数据");
				int lun = count / 500;
				int yu = count % 500;
				if (yu > 0) {
					lun = lun + 1;
				}
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
					Log.d("StatisticsController", "list大小：" + list.size());
					UnloadDownloadSpeed(list, TAApplication.getApplication());
				}

			}

		}

	}

	/**
	 * 上传app下载成功量数据
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

	/**
	 * 上传app下载数据
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
	 * 上传app下载速度统计数据
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

}
