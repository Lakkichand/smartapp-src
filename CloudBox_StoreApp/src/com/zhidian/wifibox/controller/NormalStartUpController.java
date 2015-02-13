package com.zhidian.wifibox.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

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
import com.zhidian.wifibox.data.SpkStart;
import com.zhidian.wifibox.db.dao.SpkStartDao;
import com.zhidian.wifibox.util.BoxIdManager;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.Setting;

/**
 * 正常网络启动装机大师的逻辑处理
 * 
 * @author xiedezhi
 * 
 */
public class NormalStartUpController extends TACommand {

	private static boolean sCanUpload = true;// 设置是否可上传

	private Handler mHandler = new Handler(Looper.getMainLooper());
	/**
	 * 普通网络启动
	 */
	public static final String NORMAL_STARTUP = "NORMALSTARTUPCONTROLLER_NORMAL_STARTUP";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(NORMAL_STARTUP)) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(TAApplication.getApplication());
			boolean b = prefs.getBoolean(Setting.HASREGISTERUSERINFO, false);
			if (!b) {
				// 还没注册phone表
				TAApplication.getApplication().doCommand(
						TAApplication.getApplication().getString(
								R.string.maincontroller),
						new TARequest(MainController.REGISTER_USERINFO, null),
						new TAIResponseListener() {

							@Override
							public void onSuccess(TAResponse response) {
								// 装机大师的安装量
								checkFirstInstall();
								// 装机大师的启动量
								everyTimeStartUserInfo();
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
								try {
									// 保存数据到本地不作上传
									SimpleDateFormat formatter = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss");
									Date curDate = new Date(System
											.currentTimeMillis());// 获取当前时间
									String startTime = formatter
											.format(curDate);
									SpkStart bean = new SpkStart();
									SpkStartDao dao = new SpkStartDao(
											TAApplication.getApplication());
									bean.boxNum = BoxIdManager.getInstance()
											.getBoxId();
									bean.uuId = InfoUtil.getUUID(TAApplication
											.getApplication());
									bean.startTime = startTime;
									bean.mac = InfoUtil
											.getLocalMacAddress(TAApplication
													.getApplication());
									dao.save(bean);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}, true, false);
			} else {
				// 已经注册phone表
				// 装机大师的安装量
				checkFirstInstall();
				// 装机大师的启动量
				everyTimeStartUserInfo();
			}
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
										Log.e("NormalStartUpController",
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
	 * 装机大师的启动量
	 */
	private void everyTimeStartUserInfo() {
		TAApplication.getApplication().doCommand(
				TAApplication.getApplication().getString(
						R.string.marketstartcontroller),
				new TARequest(MarketStartController.START_MARKET, null),
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
