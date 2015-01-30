package com.zhidian.wifibox.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.util.Setting;

/**
 * 网络可用时上传数据库数据
 */
public class NetChangeStatisticsController extends TACommand {

	private static boolean sCanUpload = true;// 设置是否可上传

	private Handler mHandler = new Handler(Looper.getMainLooper());

	/**
	 * 网络可用时上传所有统计数据
	 */
	public static final String UPLOAD_STATISTICS_DATA = "NETCHANGESTATISTICSCONTROLLER_UPLOAD_STATISTICS_DATA";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(UPLOAD_STATISTICS_DATA)) {
			registerUserInfo(TAApplication.getApplication());
		}
	}

	/**
	 * 上传phone表
	 */
	private void registerUserInfo(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean b = prefs.getBoolean(Setting.HASREGISTERUSERINFO, false);
		if (!b) {
			TAApplication.getApplication().doCommand(
					TAApplication.getApplication().getString(
							R.string.maincontroller),
					new TARequest(MainController.REGISTER_USERINFO, null),
					new TAIResponseListener() {

						@Override
						public void onSuccess(TAResponse response) {
							checkFirstInstall();
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
		} else {
			checkFirstInstall();
		}
	}

	/**
	 * 检查是否已经上传装机大师的安装量
	 */
	private void checkFirstInstall() {
		Setting setting = new Setting(TAApplication.getApplication());
		boolean b = setting.getBoolean(Setting.INSTALL_STATUS);
		if (!b) {
			// 表示还没上传数据到服务器
			uploadFirstInstall();
		} else {
			// 上传数据
			TAApplication.getApplication().doCommand(
					TAApplication.getApplication().getString(
							R.string.statisticscontroller),
					new TARequest(StatisticsController.UPLOAD_ALL_DATA, null),
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
	}

	/**
	 * 上传装机大师的安装量
	 */
	private void uploadFirstInstall() {
		// 这里可能有多条市场安装量
		TAApplication.getApplication()
				.doCommand(
						TAApplication.getApplication().getString(
								R.string.marketinstallcontroller),
						new TARequest(
								MarketInstallController.INSTALL_MARKET_SQL,
								null), new TAIResponseListener() {

							@Override
							public void onSuccess(TAResponse response) {
								// 如果数据库中有数据，上传
								if (sCanUpload) {
									sCanUpload = false;
									TAApplication
											.getApplication()
											.doCommand(
													TAApplication
															.getApplication()
															.getString(
																	R.string.statisticscontroller),
													new TARequest(
															StatisticsController.UPLOAD_ALL_DATA,
															null),
													new TAIResponseListener() {

														@Override
														public void onSuccess(
																TAResponse response) {
														}

														@Override
														public void onStart() {
														}

														@Override
														public void onRuning(
																TAResponse response) {
														}

														@Override
														public void onFinish() {
														}

														@Override
														public void onFailure(
																TAResponse response) {
														}
													}, true, false);
								}
								mHandler.postDelayed(new Runnable() {

									@Override
									public void run() {
										sCanUpload = true;
										Log.e("NetChangeStatisticsController",
												"20秒后设置为可上传模式sCanUpload=true");
									}
								}, 20 * 1000);
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
