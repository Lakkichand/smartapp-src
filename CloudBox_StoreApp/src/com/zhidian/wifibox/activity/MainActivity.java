package com.zhidian.wifibox.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.Toast;

import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.MessageManager;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.MainController;
import com.zhidian.wifibox.controller.MarketInstallController;
import com.zhidian.wifibox.controller.MarketStartController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.controller.XBoxIdController;
import com.zhidian.wifibox.controller.XTimeOnlineController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.data.XDataDownload;
import com.zhidian.wifibox.download.DownloadService;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.receiver.ScanReceiver;
import com.zhidian.wifibox.service.ADTService;
import com.zhidian.wifibox.service.InternetTimeService;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.view.MainDrawer;
import com.zhidian.wifibox.view.MainViewGroup;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.LruImageCache;

/**
 * wifibox主Activity
 * 
 * @author xiedezhi
 */
@SuppressLint("SimpleDateFormat")
public class MainActivity extends SlidingActivity {
	private static MainActivity sContext = null;
	public static boolean sIsOpen = false;
	private MessageManager mMessageManager = null;
	private Setting setting;
	private long mExitTime = 0;
	/**
	 * 是否跳转到应用更新
	 */
	public static final String JUMP_TO_UPDATECONTAINER = "JUMP_TO_UPDATECONTAINER";
	/**
	 * 是否跳转到下载管理
	 */
	public static final String JUMP_TO_DOWNLOADMANAGER = "JUMP_TO_DOWNLOADMANAGER";

	/**
	 * 注册消息接收者
	 */
	public static boolean registMsgHandler(final IMessageHandler handler) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.registMsgHandler(handler);
		}
		return false;
	}

	/**
	 * 点对点发送到UI线程上的消息
	 */
	public static void sendHandler(Object who, int handlerId, int msgId,
			int param, Object object, List<? extends Object> objects) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.sendHandler(who, handlerId, msgId, param,
					object, objects);
		}
	}

	/**
	 * 点对点发送到UI线程上的消息
	 */
	public static boolean sendMessage(Object who, int handlerId, int msgId,
			int param, Object object, List<?> objList) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.send(who, handlerId, msgId, param,
					object, objList);
		}
		return false;
	}

	/**
	 * 反注册消息接收者 与{@link GoLauncher#registMsgHandler(IMessageHandler, int)}配对使用
	 */
	public static boolean unRegistMsgHandler(IMessageHandler handler) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.unRegistMsgHandler(handler);
		}
		return false;
	}

	/**
	 * 主界面
	 */
	private MainViewGroup mMainViewGroup;
	/**
	 * 侧边栏
	 */
	private MainDrawer mMainDrawer;

	protected void onNewIntent(Intent intent) {
		// 是否要跳转到应用更新
		boolean jumpToUpdate = intent.getBooleanExtra(JUMP_TO_UPDATECONTAINER,
				false);
		if (jumpToUpdate) {
			MainActivity.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
					IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
					TabController.NAVIGATIONMANAGE, null);
			Intent i = new Intent(MainActivity.this, AppUpdateActivity.class);
			startActivity(i);
		}
		boolean jumpToDownloadManager = intent.getBooleanExtra(
				JUMP_TO_DOWNLOADMANAGER, false);
		if (jumpToDownloadManager) {
			MainActivity.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
					IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
					TabController.NAVIGATIONMANAGE, null);
			Intent i = new Intent(MainActivity.this,
					DownloadManagerActivity.class);
			startActivity(i);
		}

		int timeOnline = intent.getIntExtra("time", -1);
		if (timeOnline <= 0 && ModeManager.checkRapidly()) {
			gotoGainTimeOnline();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sContext = this;
		sIsOpen = true;
		setting = new Setting(this);
		mMessageManager = new MessageManager();
		// 刚进入时根据网络决定进入普通模式还是极速模式
		ModeManager.getInstance().recordRapName();

		boolean boo = setting.getBoolean(Setting.FIRST_TIME);

		if (!boo) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String nowTime = formatter.format(curDate);
			setting.putString(Setting.INSTALL_TIME, nowTime);

			// 首次进入时，保存spk安装信息到数据库。
			String boxId = AppUtils.readAssetsFile(this, "boxId");
			setting.putString(Setting.WIFI_BOX, boxId);// 保存盒子编号

			String marketStatus = MarketInstallController.APP_INSTALL_DATA;
			gotoUploadData(marketStatus);
			setting.putBoolean(Setting.FIRST_TIME, true);
		}
		// 记录打开应用的时间
		setting.putLong(Setting.OPEN_APP_TIME, System.currentTimeMillis());

		if (ModeManager.checkRapidly()) {// 急速模式
			// 先申请一个默认上网时间，因为一进来首页的几个页面需要连接外网加载数据
			String url = "http://api.biz.hiwifi.com/v1/auth/add?timeout="
					+ InfoUtil.DEFAULT_INTERNET_TIME;
			new AsyncHttpClient().get(url, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
				}

				@Override
				public void onStart() {
				}

				@Override
				public void onFailure(Throwable error) {
				}

				@Override
				public void onFinish() {
				}
			});

			// 获取盒子的可上网时间并向盒子申请这个时间
			gotoGainTimeOnline();

			

		} else {
			// String boxId = AppUtils.readAssetsFile(this, "boxId");
			// setting.putString(Setting.WIFI_BOX, boxId);// 保存盒子编号

			// 首次进入调用激活应用接口
			registerUserInfo2();

		}
		// 启动下载服务
		Intent intent = new Intent(MainActivity.this, DownloadService.class);
		startService(intent);

		// 去除title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 检查
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent scanIntent = new Intent(TAApplication.getApplication(),
				ScanReceiver.class);
		scanIntent.setAction("alarm.scan.action");
		PendingIntent scanIntentPi = PendingIntent.getBroadcast(
				TAApplication.getApplication(), 0, scanIntent, 0);
		am.cancel(scanIntentPi);
		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),
				4 * 60 * 60 * 1000, scanIntentPi);// 一小时检查一次

		// 开启插件Service
		Intent adtService = new Intent(sContext, ADTService.class);
		this.startService(adtService);

		// 初始化图片管理器
		int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE))
				.getMemoryClass();
		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = 1024 * 1024 * memClass / 8;
		AsyncImageManager.buildInstance(new LruImageCache(cacheSize));

		mMainViewGroup = new MainViewGroup(this, getIntent());
		setContentView(mMainViewGroup);
		LayoutInflater inflater = LayoutInflater.from(this);
		mMainDrawer = (MainDrawer) inflater.inflate(R.layout.frame_menu, null);
		setBehindContentView(mMainDrawer);

		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidth(DrawUtil.dip2px(this, 0));
		sm.setShadowDrawable(null);
		sm.setBehindOffset(DrawUtil.dip2px(this, 80));
		sm.setFadeEnabled(true);
		sm.setFadeDegree(0.5f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		// 设置软键盘弹出模式
		setSoftInputMode();

		// 普通网络立即加载数据，盒子网络等启动页消失再加载数据
		if (!ModeManager.checkRapidly()) {
			// 是否要跳转到应用更新
			boolean jumpToUpdate = getIntent().getBooleanExtra(
					JUMP_TO_UPDATECONTAINER, false);
			// 是否跳转到下载管理
			boolean jumpToDownloadManager = getIntent().getBooleanExtra(
					JUMP_TO_DOWNLOADMANAGER, false);
			if (jumpToUpdate) {
				MainActivity.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONMANAGE, null);
				Intent i = new Intent(MainActivity.this,
						AppUpdateActivity.class);
				startActivity(i);
			} else if (jumpToDownloadManager) {
				MainActivity.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONMANAGE, null);
				Intent i = new Intent(MainActivity.this,
						DownloadManagerActivity.class);
				startActivity(i);
			} else {
				MainActivity.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONFEATURE, null);
			}
		}
		if (!TAApplication.DEBUG) {
			// 统计
			MobclickAgent.updateOnlineConfig(MainActivity.this);
		}
	}

	/**
	 * 非极速模式下调用，上传phone信息
	 */
	private void registerUserInfo2() {
		// TODO Auto-generated method stub
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean b = prefs.getBoolean("hasRegisterUserInfo", false);
		if (!b) {
			TAApplication.getApplication().doCommand(
					getString(R.string.maincontroller),
					new TARequest(MainController.REGISTER_USERINFO, null),
					new TAIResponseListener() {

						@Override
						public void onSuccess(TAResponse response) {
							// 首次安装调用插件--市场spk安装接口
							firstInstallUserInfo();
							// 市场启动--调用插件接口
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
							// TODO
							// 市场启动--调用插件接口
							everyTimeStartUserInfo();

						}
					}, true, false);
		} else {
			// TODO
			// 首次安装调用插件--市场spk安装接口
			firstInstallUserInfo();
			// 市场启动--调用插件接口
			everyTimeStartUserInfo();
		}
	}

	/**
	 * 市场启动--调用插件接口
	 */
	private void everyTimeStartUserInfo() {
		TAApplication.getApplication().doCommand(
				getString(R.string.marketstartcontroller),
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

	/**
	 * 获取盒子编号
	 */
	@SuppressLint("DefaultLocale")
	private void gotoGainBoxId() {
		TAApplication.getApplication().doCommand(
				getString(R.string.xboxidcontroller),
				new TARequest(XBoxIdController.GAIN_BOXID, XDataDownload
						.getXBoxIdUrl()), new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						String boxId = (String) response.getData();
						if (boxId != null
								&& boxId.toLowerCase().contains("<html>")) {
							Log.e("", "非法BoxID = " + boxId);
							String localboxId = AppUtils.readAssetsFile(
									sContext, "boxId");
							setting.putString(Setting.WIFI_BOX, localboxId);// 保存盒子编号
							return;
						}
						if (boxId != null) {
							boxId = boxId.trim();
							Setting setting = new Setting(sContext);
							setting.putString(Setting.WIFI_BOX, boxId);
						}

						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(MainActivity.this);
						boolean b = prefs.getBoolean("hasRegisterUserInfo",
								false);
						if (b) {// 表示phone数据已成功上传。

							// 首次安装调用插件--市场spk安装接口
							firstInstallUserInfo();
						}

						// 市场启动--调用插件接口
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
						String localboxId = AppUtils.readAssetsFile(sContext,
								"boxId");
						setting.putString(Setting.WIFI_BOX, localboxId);// 保存盒子编号

						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(MainActivity.this);
						boolean b = prefs.getBoolean("hasRegisterUserInfo",
								false);
						if (b) {// 表示phone数据已成功上传。

							// 首次安装调用插件--市场spk安装接口
							firstInstallUserInfo();
						}
						// 市场启动--调用插件接口
						everyTimeStartUserInfo();

					}
				}, true, false);
	}

	/**
	 * 获取上网时间
	 */
	@SuppressLint("DefaultLocale")
	private void gotoGainTimeOnline() {
		TAApplication.getApplication().doCommand(
				getString(R.string.xtimeonlinecontroller),
				new TARequest(XTimeOnlineController.GAIN_TIMEONLINE,
						XDataDownload.getXTimeOnlineUrl()),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						String timeOnline = (String) response.getData();
						if (timeOnline != null
								&& timeOnline.toLowerCase().contains("<html>")) {
							Log.e("", "非法上网时间 = " + timeOnline);
							String localtimeOnline = AppUtils.readAssetsFile(
									MainActivity.this, "timeOnline");
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
							gotoGainOnline();
							return;
						}
						if (timeOnline != null) {
							timeOnline = timeOnline.trim();
							List<String> lines = FileUtil.readLine(timeOnline);
							if (lines != null && lines.size() > 0) {
								setting.putString(Setting.TIME_ONLINE,
										lines.get(0));
							} else {
								setting.putString(Setting.TIME_ONLINE, ""
										+ InfoUtil.DEFAULT_INTERNET_TIME);
							}
							gotoGainOnline();
							return;
						}
						{
							String localtimeOnline = AppUtils.readAssetsFile(
									MainActivity.this, "timeOnline");
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
							gotoGainOnline();
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
						String localtimeOnline = AppUtils.readAssetsFile(
								MainActivity.this, "timeOnline");
						setting.putString(Setting.TIME_ONLINE, localtimeOnline);

						gotoGainOnline();

					}
				}, true, false);
	}

	/**
	 * 获取连接外网权限 http://api.biz.hiwifi.com/v1/auth/add?timeout=1000
	 */
	private void gotoGainOnline() {
		// 调用接口设置WIFI盒子能连外网
		final String timeOnline = setting.getString(Setting.TIME_ONLINE);
		String url = "http://api.biz.hiwifi.com/v1/auth/add?timeout="
				+ timeOnline;

		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String content) {
				Log.e("", "onSuccess  content = " + content);
				// 判断能连外网才显示上网时间
				AsyncHttpClient client = new AsyncHttpClient();
				client.setTimeout(5000);
				client.get(CDataDownloader.getExtranetUrl(),
						new AsyncHttpResponseHandler() {
							@SuppressWarnings("unused")
							@Override
							public void onSuccess(String content) {
								try {
									JSONObject json = new JSONObject(content);
									// 返回时json格式就代表能连接外网
									Intent intent = new Intent(
											MainActivity.this,
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
									startService(intent);									
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								// 首次进入上传phone表基本数据
								registerUserInfo();
							}

							@Override
							public void onStart() {
							}

							@Override
							public void onFailure(Throwable error) {
								// 首次进入上传phone表基本数据
								registerUserInfo();
							}

							@Override
							public void onFinish() {
							}
						});
			}

			@Override
			public void onStart() {
			}

			@Override
			public void onFailure(Throwable error) {
				// 首次进入上传phone表基本数据
				registerUserInfo();
			}

			@Override
			public void onFinish() {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMainViewGroup.onResume();
		Intent intent = new Intent(IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
		intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_CHECKTASK);
		sendBroadcast(intent);
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("主页面 ");
			MobclickAgent.onResume(this, "53547a5556240b09f712c2fb",
					AppUtils.readAssetsFile(this, "boxId"));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("主页面 ");
			MobclickAgent.onPause(this);
		}
	}

	private void gotoUploadData(String marketStatus) {
		TAApplication.getApplication().doCommand(
				getString(R.string.marketinstallcontroller),
				new TARequest(marketStatus, null), new TAIResponseListener() {

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
	 * 首次安装调用插件--市场spk安装接口
	 */
	private void firstInstallUserInfo() {
		boolean b = setting.getBoolean(Setting.INSTALL_STATUS);
		if (!b) {// 表示还没上传数据到服务器
			String marketStatus = MarketInstallController.INSTALL_MARKET_SQL;
			gotoUploadData(marketStatus);
		}

	}

	/**
	 * 设置软键盘弹出模式
	 */
	private void setSoftInputMode() {
		this.getWindow().setSoftInputMode(
				android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	/**
	 * 极速模式下调用，首次进入调用激活应用接口
	 */
	private void registerUserInfo() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean b = prefs.getBoolean("hasRegisterUserInfo", false);
		if (!b) {
			TAApplication.getApplication().doCommand(
					getString(R.string.maincontroller),
					new TARequest(MainController.REGISTER_USERINFO, null),
					new TAIResponseListener() {

						@Override
						public void onSuccess(TAResponse response) {
							gotoGainBoxId();
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
							gotoGainBoxId();

						}
					}, true, false);
		} else {
			gotoGainBoxId();
		}
	}

	@Override
	protected void onDestroy() {
		// 清理图标
		TAApplication.getApplication().doCommand(
				getString(R.string.maincontroller),
				new TARequest(MainController.CLEAN_ICON, null),
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
		// 清除单例
		TabDataManager.getInstance().destory();
		InstallingValidator.getInstance().destory();
		AsyncImageManager.destory();
		// 反注册广播接收器
		mMainViewGroup.onDestory();
		super.onDestroy();
		sIsOpen = false;
	}

	public void onBackPressed() {
		if (TabDataManager.getInstance().getTabStackSize() > 1) {
			MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
					IDiyMsgIds.BACK_ON_ONE_LEVEL, -1, null, null);
		} else {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, "再按一次返回键关闭程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				super.onBackPressed();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			getSlidingMenu().toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
