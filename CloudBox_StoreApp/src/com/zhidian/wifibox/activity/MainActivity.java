package com.zhidian.wifibox.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.slidingmenu.lib.app.SlidingActivity;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPro;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatService;
import com.tencent.stat.common.StatConstants;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.MainController;
import com.zhidian.wifibox.controller.MarketInstallController;
import com.zhidian.wifibox.controller.MiBaoStartUpController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.NormalStartUpController;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PopupCommend;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.download.DownloadService;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.receiver.ScanReceiver;
import com.zhidian.wifibox.service.ADTService;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PopWindowsUtil;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.util.TimeTool;
import com.zhidian.wifibox.view.MainDrawer;
import com.zhidian.wifibox.view.MainViewGroup;
import com.zhidian.wifibox.view.dialog.ActiviDialog;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.LruImageCache;
import com.zhidian3g.wifibox.imagemanager.ThreadPoolManager;

/**
 * wifibox主Activity
 * 
 * @author xiedezhi
 */
public class MainActivity extends SlidingActivity {

	private static final String PUSH_TOPICID = "special";
	private static final String PUSH_TOPICTITLE = "title";

	private static MainActivity sContext = null;
	public static boolean sIsOpen = false;
	private Setting setting;
	private long mExitTime = 0;
	private ActiviDialog activiDialog; // 活动弹框
	/**
	 * 是否跳转到应用更新
	 */
	public static final String JUMP_TO_UPDATECONTAINER = "JUMP_TO_UPDATECONTAINER";
	/**
	 * 是否跳转到下载管理
	 */
	public static final String JUMP_TO_DOWNLOADMANAGER = "JUMP_TO_DOWNLOADMANAGER";

	/**
	 * 主界面
	 */
	private MainViewGroup mMainViewGroup;
	/**
	 * 侧边栏
	 */
	private MainDrawer mMainDrawer;

	protected void onNewIntent(Intent intent) {
		Log.e("", "MainActivity onNewIntent");
		// 是否要跳转到应用更新
		boolean jumpToUpdate = intent.getBooleanExtra(JUMP_TO_UPDATECONTAINER,
				false);
		if (jumpToUpdate) {
			TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
					IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
					TabController.NAVIGATIONMANAGE, null);
			Intent i = new Intent(MainActivity.this, AppUpdateActivity.class);
			startActivity(i);
		}
		boolean jumpToDownloadManager = intent.getBooleanExtra(
				JUMP_TO_DOWNLOADMANAGER, false);
		if (jumpToDownloadManager) {
			TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
					IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
					TabController.NAVIGATIONMANAGE, null);
			Intent i = new Intent(MainActivity.this,
					DownloadManagerActivity.class);
			startActivity(i);
		}

		int timeOnline = intent.getIntExtra("time", -1);
		if (timeOnline <= 0 && ModeManager.checkRapidly()) {
			TAApplication.getApplication().doCommand(
					TAApplication.getApplication().getString(
							R.string.mibaostartupcontroller),
					new TARequest(MiBaoStartUpController.MIBAO_STARTUP, null),
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
		// 必须要调用这句
		setIntent(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sContext = this;
		sIsOpen = true;
		setting = new Setting(this);
		// 刚进入时根据网络决定进入普通模式还是极速模式
		ModeManager.getInstance().recordRapName();

		setting.putBoolean(Setting.METER_NEED_CALCULATE, true);
		boolean boo = setting.getBoolean(Setting.FIRST_TIME);
		// 装机大师的安装量
		if (!boo) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String nowTime = formatter.format(curDate);
			setting.putString(Setting.INSTALL_TIME, nowTime);

			String boxId = AppUtils.readAssetsFile(this, "boxId");
			setting.putString(Setting.WIFI_BOX, boxId);// 保存盒子编号
			// 首次进入时，保存spk安装信息到数据库。
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
			TAApplication.getApplication().getAsyncHttpClient().get(url, null);
			// 获取盒子的可上网时间并向盒子申请这个时间
			TAApplication.getApplication().doCommand(
					TAApplication.getApplication().getString(
							R.string.mibaostartupcontroller),
					new TARequest(MiBaoStartUpController.MIBAO_STARTUP, null),
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
		} else {
			// 保存盒子编号
			String boxId = AppUtils.readAssetsFile(this, "boxId");
			setting.putString(Setting.WIFI_BOX, boxId);
			// 普通网络启动
			TAApplication.getApplication()
					.doCommand(
							TAApplication.getApplication().getString(
									R.string.normalstartupcontroller),
							new TARequest(
									NormalStartUpController.NORMAL_STARTUP,
									null), new TAIResponseListener() {

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
		// 启动下载服务
		Intent intent = new Intent(MainActivity.this, DownloadService.class);
		startService(intent);

		// 检查更新
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
		// 初始化主界面
		mMainViewGroup = new MainViewGroup(this, getIntent());
		setContentView(mMainViewGroup);
		LayoutInflater inflater = LayoutInflater.from(this);
		mMainDrawer = (MainDrawer) inflater.inflate(R.layout.frame_menu, null);
		setBehindContentView(mMainDrawer);
		// 侧边栏
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidth(DrawUtil.dip2px(this, 0));
		sm.setShadowDrawable(null);
		sm.setBehindOffset(DrawUtil.dip2px(this, 80));
		sm.setFadeEnabled(true);
		sm.setFadeDegree(0.5f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setOnOpenedListener(new OnOpenedListener() {

			@Override
			public void onOpened() {
				if (mMainDrawer != null) {
					mMainDrawer.getMemory();
				}
			}
		});

		// 腾讯云统计
		try {
			StatService.startStatService(this, "A16UE3P2FZSC",
					StatConstants.VERSION);
		} catch (MtaSDkException e) {
			e.printStackTrace();
			Log.e("", "MTA start failed");
		}
		StatService.reportQQ(this, "613775462");
		// 信鸽注册
		XGPushConfig.setInstallChannel(getApplicationContext(),
				AppUtils.readAssetsFile(this, "boxId"));
		try {
			XGPro.enableXGPro(getApplicationContext(), true);
		} catch (Exception e) {
			Log.e("", "开启信鸽Pro失败");
			e.printStackTrace();
		}
		XGPushManager.registerPush(getApplicationContext(),
				InfoUtil.getUUID(getApplicationContext()),
				new XGIOperateCallback() {
					@Override
					public void onSuccess(Object data, int flag) {
						Log.e("TPush", "注册成功，设备token为：" + data);
					}

					@Override
					public void onFail(Object data, int errCode, String msg) {
						Log.e("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
					}
				});
		Intent service = new Intent(getApplicationContext(),
				XGPushService.class);
		getApplicationContext().startService(service);
		// 普通网络立即加载数据，盒子网络等启动页消失再加载数据
		if (!ModeManager.checkRapidly()) {
			// 是否要跳转到应用更新
			boolean jumpToUpdate = getIntent().getBooleanExtra(
					JUMP_TO_UPDATECONTAINER, false);
			// 是否跳转到下载管理
			boolean jumpToDownloadManager = getIntent().getBooleanExtra(
					JUMP_TO_DOWNLOADMANAGER, false);
			if (jumpToUpdate) {
				TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONMANAGE, null);
				Intent i = new Intent(MainActivity.this,
						AppUpdateActivity.class);
				startActivity(i);
			} else if (jumpToDownloadManager) {
				TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONMANAGE, null);
				Intent i = new Intent(MainActivity.this,
						DownloadManagerActivity.class);
				startActivity(i);
			} else {
				TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONFEATURE, null);
			}
		}
		// 活动框
		if (InfoUtil.hasNetWorkConnection(getApplicationContext())
				&& PopWindowsUtil.getFirstPop(sContext)) {
			gainActivitPopCommend();
		}
		// 校验时间
		TimeTool.verifyTime(true);
		// 门店广告
		mibaoAdvertisement();
		// 标签
		XGPushManager.setTag(TAApplication.getApplication(),
				android.os.Build.MANUFACTURER);
		XGPushManager.setTag(TAApplication.getApplication(),
				android.os.Build.MODEL);
		int open_app = setting.getInt(Setting.OPEN_APP_COUNT);
		open_app = open_app + 1;
		setting.putInt(Setting.OPEN_APP_COUNT, open_app);
		if (open_app >= 5) {
			XGPushManager.deleteTag(getApplicationContext(), "老用户x10");
			XGPushManager.setTag(getApplicationContext(), "老用户x5");
		} else if (open_app >= 10) {
			XGPushManager.deleteTag(getApplicationContext(), "老用户x5");
			XGPushManager.setTag(getApplicationContext(), "老用户x10");
		}
	}

	/**
	 * 门店广告
	 */
	private void mibaoAdvertisement() {
		// 门店广告
		if (ModeManager.checkRapidly()) {
			Setting setting = new Setting(TAApplication.getApplication());
			long lasttime = setting.getLong(Setting.SHOW_ADVERTISEMENT_TIME);
			// 一天内最多展示一次门店广告
			if (System.currentTimeMillis() - lasttime > 24L * 60L * 60L * 1000L) {
				TAApplication.getApplication()
						.doCommand(
								TAApplication.getApplication().getString(
										R.string.maincontroller),
								new TARequest(
										MainController.MIBAO_ADVERTISEMENT,
										null), new TAIResponseListener() {

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
	}

	/**
	 * 活动弹窗推荐
	 */
	private void gainActivitPopCommend() {
		TAApplication.getApplication().doCommand(
				getString(R.string.maincontroller),
				new TARequest(MainController.POPUP_RECOMEND, null),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						final PopupCommend pop = (PopupCommend) response
								.getData();
						if (pop != null) {
							setting.putLong(Setting.TODAYTIME,
									PopWindowsUtil.getTodayTime());
							if (mMainViewGroup != null) {
								mMainViewGroup.postDelayed(new Runnable() {

									@Override
									public void run() {
										if (!sIsOpen) {
											return;
										}
										activiDialog = new ActiviDialog(
												sContext, pop);
										activiDialog.show();
									}
								}, 3500);
							}
						} else {
							Log.e("活动弹窗推荐", "获取数据失败");
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
					}
				}, true, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMainViewGroup.onResume();
		Intent intent = new Intent(IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
		intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_CHECKTASK);
		sendBroadcast(intent);
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			String content = click.getCustomContent();
			try {
				JSONObject json = new JSONObject(content);
				String topicid = json.optString(PUSH_TOPICID, "");
				String title = json.optString(PUSH_TOPICTITLE, "");
				if (!TextUtils.isEmpty(topicid)) {
					long id = Long.valueOf(topicid);
					final TopicDataBean bean = new TopicDataBean();
					bean.id = id;
					bean.title = TextUtils.isEmpty(title) ? title
							: ("推送专题" + id);
					final List<Object> list = new ArrayList<Object>();
					list.add(bean);
					if (TabDataManager.getInstance().getTabStackSize() <= 0) {
						mMainViewGroup.postDelayed(new Runnable() {

							@Override
							public void run() {
								if (!sIsOpen) {
									return;
								}
								// 跳转到专题内容
								TAApplication.sendHandler(null,
										IDiyFrameIds.TABMANAGEVIEW,
										IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
										CDataDownloader.getTopicContentUrl(
												bean.id, 1), list);
							}
						}, 3000);
					} else {
						// 跳转到专题内容
						TAApplication.sendHandler(null,
								IDiyFrameIds.TABMANAGEVIEW,
								IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
								CDataDownloader.getTopicContentUrl(bean.id, 1),
								list);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMainViewGroup.onPause();
		XGPushManager.onActivityStoped(this);
	}

	/**
	 * 首次进入时，保存spk安装信息到数据库。
	 */
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

	@Override
	protected void onDestroy() {
		// 标签
		XGPushManager.deleteTag(getApplicationContext(), "平均10s以下");
		XGPushManager.deleteTag(getApplicationContext(), "平均10-30秒");
		XGPushManager.deleteTag(getApplicationContext(), "平均30-60秒");
		XGPushManager.deleteTag(getApplicationContext(), "平均1-3分钟");
		XGPushManager.deleteTag(getApplicationContext(), "平均3-10分钟");
		XGPushManager.deleteTag(getApplicationContext(), "平均10-30分钟");
		XGPushManager.deleteTag(getApplicationContext(), "平均30分钟以上");
		long opentime = setting.getLong(Setting.OPEN_APP_TIME);
		long usetime = System.currentTimeMillis() - opentime;
		long totaltime = setting.getLong(Setting.APP_USED_TOTAL_TIME);
		totaltime = totaltime + usetime;
		setting.putLong(Setting.APP_USED_TOTAL_TIME, totaltime);
		int totalcount = setting.getInt(Setting.OPEN_APP_COUNT);
		long average = totaltime / totalcount;
		if (average <= 10 * 1000L) {
			XGPushManager.setTag(getApplicationContext(), "平均10s以下");
		} else if (average <= 30 * 1000L) {
			XGPushManager.setTag(getApplicationContext(), "平均10-30秒");
		} else if (average <= 60 * 1000L) {
			XGPushManager.setTag(getApplicationContext(), "平均30-60秒");
		} else if (average <= 3 * 60 * 1000L) {
			XGPushManager.setTag(getApplicationContext(), "平均1-3分钟");
		} else if (average <= 10 * 60 * 1000L) {
			XGPushManager.setTag(getApplicationContext(), "平均3-10分钟");
		} else if (average <= 30 * 60 * 1000L) {
			XGPushManager.setTag(getApplicationContext(), "平均10-30分钟");
		} else {
			XGPushManager.setTag(getApplicationContext(), "平均30分钟以上");
		}
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
		// 销毁线程池
		ThreadPoolManager.destory();
		super.onDestroy();
		sIsOpen = false;
	}

	public void onBackPressed() {
		if (TabDataManager.getInstance().getTabStackSize() > 1) {
			TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
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
