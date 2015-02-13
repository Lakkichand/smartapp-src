package com.zhidian.wifibox.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.tencent.android.tpush.XGPushManager;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.SpkStart;
import com.zhidian.wifibox.data.XDataDownload;
import com.zhidian.wifibox.db.dao.SpkStartDao;
import com.zhidian.wifibox.service.InternetTimeService;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.BoxIdManager;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.IdleCounter;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.Setting;

/**
 * 连接盒子WIFI后启动装机大师的逻辑处理
 * 
 * @author xiedezhi
 * 
 */
public class MiBaoStartUpController extends TACommand {
	/**
	 * 公司内部盒子
	 */
	private static Set<String> sInternalBox = new HashSet<String>();

	static {
		sInternalBox = new HashSet<String>();
		sInternalBox.add("D4EE071DE93A");
		sInternalBox.add("D4EE0705D35C");
		sInternalBox.add("D4EE071DE988");
		sInternalBox.add("D4EE071DE8DC");
		sInternalBox.add("D4EE071DF662");
		sInternalBox.add("D4EE071DF2CE");
		sInternalBox.add("D4EE071DE956");
		sInternalBox.add("D4EE071DE932");
		sInternalBox.add("D4EE071DE852");
		sInternalBox.add("D4EE071DF9BC");
	}

	private static boolean sCanUpload = true;// 设置是否可上传

	private Handler mHandler = new Handler(Looper.getMainLooper());

	/**
	 * 盒子WIFI网络启动
	 */
	public static final String MIBAO_STARTUP = "MIBAOSTARTUPCONTROLLER_MIBAO_STARTUP";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(MIBAO_STARTUP)) {
			ModeManager.getInstance().setExtranet(false);
			getBoxId();
			getInternetAccessTime();
		}
	}

	/**
	 * 获取盒子编号
	 */
	private void getBoxId() {
		TAApplication.getApplication().doCommand(
				TAApplication.getApplication().getString(
						R.string.xboxidcontroller),
				new TARequest(XBoxIdController.GAIN_BOXID, XDataDownload
						.getXBoxIdUrl()), new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						Setting setting = new Setting(TAApplication
								.getApplication());
						String boxId = (String) response.getData();
						if (boxId != null) {
							boxId = boxId.trim();
						}
						// 标签
						if (boxId != null && sInternalBox.contains(boxId)) {
							int count = setting
									.getInt(Setting.CONNECT_INTERNAL_WIFI_COUNT);
							count = count + 1;
							setting.putInt(Setting.CONNECT_INTERNAL_WIFI_COUNT,
									count);
							if (count >= 3) {
								XGPushManager.setTag(
										TAApplication.getApplication(),
										"公司内部用户");
							}
						}
						if (boxId != null
								&& boxId.toLowerCase().contains("<html>")) {
							Log.e("", "非法BoxID = " + boxId);
							BoxIdManager.getInstance().saveBoxId(
									BoxIdManager.getInstance().readAssetsBoxId());
							return;
						}
						if (boxId != null) {
							if (!TextUtils.isEmpty(boxId.trim())) {
								BoxIdManager.getInstance().saveBoxId(
										boxId.trim());
								if (BoxIdManager.getInstance().isDefaultBoxId(
										BoxIdManager.getInstance()
												.readAssetsBoxId())) {
									BoxIdManager.getInstance().saveAssetsBoxId(
											boxId.trim());
								}
								BoxIdManager.getInstance().checkMarketInstallBoxId();
								// 获取地理位置，一天内只获取某个盒子位置一次
								long lasttime = setting
										.getLong(Setting.MIBAO_LOCATION_TIME_PREFIX
												+ BoxIdManager.getInstance()
														.getBoxId());
								if (System.currentTimeMillis() - lasttime <= 24L * 60L * 60L * 1000L) {
									return;
								}
								TAApplication
										.getApplication()
										.doCommand(
												TAApplication
														.getApplication()
														.getString(
																R.string.maincontroller),
												new TARequest(
														MainController.MIBAO_GETLOCATION,
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
						}
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
						BoxIdManager.getInstance().saveBoxId(
								BoxIdManager.getInstance().readAssetsBoxId());
					}
				}, true, false);
	}

	/**
	 * 获取盒子的可上网时间并向盒子申请这个时间
	 */
	private void getInternetAccessTime() {
		TAApplication.getApplication().doCommand(
				TAApplication.getApplication().getString(
						R.string.xtimeonlinecontroller),
				new TARequest(XTimeOnlineController.GAIN_TIMEONLINE,
						XDataDownload.getXTimeOnlineUrl()),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						Setting setting = new Setting(TAApplication
								.getApplication());
						String timeOnline = (String) response.getData();
						if (timeOnline != null
								&& !(timeOnline.toLowerCase()
										.contains("<html>"))) {
							timeOnline = timeOnline.trim();
							List<String> lines = FileUtil.readLine(timeOnline);
							if (lines != null && lines.size() > 0) {
								setting.putString(Setting.TIME_ONLINE,
										lines.get(0));
							} else {
								setting.putString(Setting.TIME_ONLINE, ""
										+ InfoUtil.DEFAULT_INTERNET_TIME);
							}
							applyInternetAccess();
							return;
						}
						{
							String localtimeOnline = AppUtils.readAssetsFile(
									TAApplication.getApplication(),
									"timeOnline");
							if (localtimeOnline != null) {
								localtimeOnline = localtimeOnline.trim();
							}
							List<String> lines = FileUtil
									.readLine(localtimeOnline);
							if (lines != null && lines.size() > 0) {
								setting.putString(Setting.TIME_ONLINE,
										lines.get(0));
							} else {
								setting.putString(Setting.TIME_ONLINE, ""
										+ InfoUtil.DEFAULT_INTERNET_TIME);
							}
							applyInternetAccess();
						}
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
						Setting setting = new Setting(TAApplication
								.getApplication());
						String localtimeOnline = AppUtils.readAssetsFile(
								TAApplication.getApplication(), "timeOnline");
						setting.putString(Setting.TIME_ONLINE, localtimeOnline);
						applyInternetAccess();
					}
				}, true, false);
	}

	/**
	 * 获取连接外网权限
	 */
	private void applyInternetAccess() {
		Setting setting = new Setting(TAApplication.getApplication());
		// 调用接口设置WIFI盒子能连外网
		final String timeOnline = setting.getString(Setting.TIME_ONLINE);
		String url = "http://api.biz.hiwifi.com/v1/auth/add?timeout="
				+ timeOnline;
		int timeout = InfoUtil.DEFAULT_INTERNET_TIME;
		try {
			timeout = Integer.valueOf(timeOnline);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 监控空闲连接
		IdleCounter.listenIdle(
				InfoUtil.getCurWifiName(TAApplication.getApplication()),
				timeout * 1000L + 60 * 60 * 1000L, 5000L);
		AsyncHttpClient client = TAApplication.getApplication()
				.getAsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(final String ex_content) {
				Log.e("", "onSuccess  content = " + ex_content);
				if (ex_content == null) {
					return;
				}
				if (!ex_content.toLowerCase().trim().equals("ok")
						&& !ex_content.toLowerCase().trim().equals("vip")) {
					return;
				}
				// 判断能连外网才显示上网时间
				AsyncHttpClient client = TAApplication.getApplication()
						.getAsyncHttpClient();
				client.get(CDataDownloader.getExtranetUrl(),
						new AsyncHttpResponseHandler() {

							@Override
							public void onSuccess(String content) {
								try {
									JSONObject json = new JSONObject(content);
									ModeManager.getInstance().setExtranet(true);
									// 返回时json格式就代表能连接外网
									Intent intent = new Intent(TAApplication
											.getApplication(),
											InternetTimeService.class);
									int timeout = InfoUtil.DEFAULT_INTERNET_TIME;
									try {
										timeout = Integer.valueOf(timeOnline);
									} catch (Exception e) {
										e.printStackTrace();
									}
									intent.putExtra(
											InternetTimeService.INTERNET_TIME_KEY,
											timeout);
									if (ex_content.toLowerCase().trim()
											.equals("ok")) {
										TAApplication.getApplication()
												.startService(intent);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								// 首次进入上传phone表基本数据
								registerUserInfo();
							}

							@Override
							public void onFailure(Throwable error) {
								ModeManager.getInstance().setExtranet(false);
								// 首次进入上传phone表基本数据
								registerUserInfo();
							}
						});
			}

			@Override
			public void onFailure(Throwable error) {
				// 首次进入上传phone表基本数据
				registerUserInfo();
			}

		});
	}

	/**
	 * 极速模式下调用，首次进入调用激活应用接口
	 */
	private void registerUserInfo() {
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
								String startTime = formatter.format(curDate);
								SpkStart bean = new SpkStart();
								SpkStartDao dao = new SpkStartDao(TAApplication
										.getApplication());
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
										Log.e("MiBaoStartUpController",
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
