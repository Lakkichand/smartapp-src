package com.zhidian.wifibox.view;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jiubang.core.message.IMessageHandler;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.FileHttpResponseHandler;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppUpdateActivity;
import com.zhidian.wifibox.activity.DownloadManagerActivity;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.MainController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.controller.XBoxIdController;
import com.zhidian.wifibox.controller.XTimeOnlineController;
import com.zhidian.wifibox.data.AutoUpdateBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.data.XDataDownload;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.service.InternetTimeService;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.view.dialog.DataDialog;

/**
 * wifi盒子主界面，负责管理TabManageView、SearchView等。 要处理返回键按键事件
 * 
 * @author xiedezhi
 * 
 */
public class MainViewGroup extends FrameLayout implements IMessageHandler {

	private TabManageView mTabManageView;
	/**
	 * 搜索界面
	 */
	private SearchView mSearchView;
	/**
	 * 展示搜索页面前的侧边栏touchmode
	 */
	private int mSlidingTouchMode = SlidingMenu.TOUCHMODE_FULLSCREEN;
	/**
	 * 启动页
	 */
	private View mCover;
	/**
	 * MainActivity的启动intent，带一些启动参数
	 */
	private Intent mIntent;
	/**
	 * 超速提示
	 */
	private ImageView mXTip;
	/**
	 * 应用安装卸载监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				// 通知安装验证器
				InstallingValidator.getInstance().onAppAction(getContext(),
						packageName);
				// 通知MainViewGroup
				onAppAction(packageName);
			}
		}
	};
	/**
	 * 下载广播接收器
	 */
	private final BroadcastReceiver mDownloadListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadTask task = intent.getParcelableExtra("task");
			// 通知MainViewGroup
			notifyDownloadState(task);
		}
	};
	/**
	 * 网络状态监听器
	 */
	private final BroadcastReceiver mNetWorkListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isRap = ModeManager.checkRapidly();
			if (TextUtils.isEmpty(ModeManager.getInstance().getRapName())
					&& isRap) {
				// 展示提示
				mTabManageView.showXTip();
				// 普通-->>极速
				// 去掉超速页面数据
				TabDataManager.getInstance().resetPageData(
						CDataDownloader.getHomeSpeedingDownloadsUrl());
				// 刷新超速页面
				MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.REFRESH_CONTAINER, -1,
						CDataDownloader.getHomeSpeedingDownloadsUrl(), null);
				// 记录极速名字
				ModeManager.getInstance().recordRapName();
				// 获取盒子编号
				gotoGainBoxId();
				// 获取允许上网时间
				gotoGainTimeOnline();
			} else if (!TextUtils.isEmpty(ModeManager.getInstance()
					.getRapName()) && !isRap) {
				// 移除提示
				MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
						IDiyMsgIds.REMOVE_X_TIP, -1, null, null);
				// 极速-->>普通
				// 去掉超速页面数据
				TabDataManager.getInstance().resetPageData(
						CDataDownloader.getHomeSpeedingDownloadsUrl());
				// 刷新超速页面
				MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.REFRESH_CONTAINER, -1,
						CDataDownloader.getHomeSpeedingDownloadsUrl(), null);
				// 记录极速名字
				ModeManager.getInstance().recordRapName();
				// 保存盒子编号
				Setting setting = new Setting(getContext());
				String boxId = AppUtils.readAssetsFile(getContext(), "boxId");
				setting.putString(Setting.WIFI_BOX, boxId);
			}
		}
	};

	public MainViewGroup(Context context, Intent intent) {
		super(context);
		mIntent = intent;
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		// 注册广播接收器
		registerReceiver();
		initView();
		// 注册消息组件
		MainActivity.registMsgHandler(this);
	}

	/**
	 * 初始化view树
	 */
	private void initView() {
		mTabManageView = new TabManageView(getContext());
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		addView(mTabManageView, lp);

		LayoutInflater inflater = LayoutInflater.from(getContext());
		mCover = inflater.inflate(R.layout.activity_start, null);
		addView(mCover, lp);
		ImageView image = (ImageView) mCover.findViewById(R.id.image);
		if (ModeManager.checkRapidly()) {
			// 该门店广告图片
			String filename = "";
			if (FileUtil.isSDCardAvaiable()) {
				filename = PathConstant.AD_PATH
						+ InfoUtil.getCurWifiName(getContext())
								.replaceAll("\"", "").replaceAll(" ", "")
						+ "/adv";
			} else {
				filename = PathConstant.AD_PATH_CACHE
						+ InfoUtil.getCurWifiName(getContext())
								.replaceAll("\"", "").replaceAll(" ", "")
						+ "/adv";
			}
			// 先读取门店广告
			Bitmap bm = BitmapFactory.decodeFile(filename);
			if (bm == null) {
				image.setImageResource(R.drawable.start_front);
			} else {
				image.setImageBitmap(bm);
			}
			// 下载门店广告图片
			AsyncHttpClient syncHttpClient = new AsyncHttpClient();
			FileHttpResponseHandler fHandler = new FileHttpResponseHandler(
					filename) {
				@Override
				public void onFailure(Throwable error) {
					error.printStackTrace();
					Log.e("", "onFailure error = " + error);
				}
			};
			syncHttpClient.download(XDataDownload.getXADUrl(), fHandler);
		} else {
			image.setImageResource(R.drawable.start_front);
		}
		if (ModeManager.checkRapidly()) {
			postDelayed(new Runnable() {

				@Override
				public void run() {
					// 是否要跳转到应用更新
					boolean jumpToUpdate = mIntent.getBooleanExtra(
							MainActivity.JUMP_TO_UPDATECONTAINER, false);
					// 是否跳转到下载管理
					boolean jumpToDownloadManager = mIntent.getBooleanExtra(
							MainActivity.JUMP_TO_DOWNLOADMANAGER, false);
					if (jumpToUpdate) {
						MainActivity.sendHandler(null,
								IDiyFrameIds.NAVIGATIONBAR,
								IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
								TabController.NAVIGATIONMANAGE, null);
						Intent i = new Intent(getContext(),
								AppUpdateActivity.class);
						getContext().startActivity(i);
					} else if (jumpToDownloadManager) {
						MainActivity.sendHandler(null,
								IDiyFrameIds.NAVIGATIONBAR,
								IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
								TabController.NAVIGATIONMANAGE, null);
						Intent i = new Intent(getContext(),
								DownloadManagerActivity.class);
						getContext().startActivity(i);
					} else {
						MainActivity.sendHandler(null,
								IDiyFrameIds.NAVIGATIONBAR,
								IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
								TabController.NAVIGATIONFEATURE, null);
					}
				}
			}, 2500);
		}
		postDelayed(new Runnable() {

			@Override
			public void run() {
				removeView(mCover);
				mCover = null;
				// 检查自动更新
				checkForUpdates();
			}
		}, 3000);
	}

	/**
	 * 检查更新
	 */
	private void checkForUpdates() {
		// 先判断是否自动更新
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.maincontroller),
				new TARequest(MainController.CHECK_FOR_UPDATE, null),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						AutoUpdateBean bean = (AutoUpdateBean) response
								.getData();
						if (bean != null && bean.statusCode == 0
								&& !bean.isLatest) {
							Log.e("", "updateurl = " + bean.updateUrl);
							File file = new File(DownloadUtil
									.getCApkFileFromUrl(bean.updateUrl));
							file.delete();
							file = new File(DownloadUtil
									.getCTempApkFileFromUrl(bean.updateUrl));
							file.delete();
							if (bean.isMust) {
								// 弹出更新对话框，点击取消则退出应用
								DataDialog dataDialog = new DataDialog(
										(MainActivity) getContext(),
										bean.version, FileUtil
												.convertFileSize(bean.size),
										bean.description, bean.updateUrl, true);
								try {
									dataDialog.show();
								} catch (Throwable e) {
									e.printStackTrace();
								}
							} else {
								if (InfoUtil.hasWifiConnection(getContext())) {
									// 弹出更新对话框，点击取消则关闭对话框
									DataDialog dataDialog = new DataDialog(
											(MainActivity) getContext(),
											bean.version,
											FileUtil.convertFileSize(bean.size),
											bean.description, bean.updateUrl,
											false);
									try {
										dataDialog.show();
									} catch (Throwable e) {
										e.printStackTrace();
									}
								} else {
									// do nothing
								}
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
					}
				}, true, false);
	}

	/**
	 * 注册广播接收器
	 */
	private void registerReceiver() {
		// 注册应用安装卸载事件
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		getContext().registerReceiver(mAppInstallListener, intentFilter);
		// 注册下载广播事件
		intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_BROADCAST_ACTION);
		getContext().registerReceiver(mDownloadListener, intentFilter);
		// 注册网络状态监听器
		intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getContext().registerReceiver(mNetWorkListener, intentFilter);
	}

	/**
	 * 反注册广播接收器
	 */
	private void unRegisterReceiver() {
		// 反注册安装广播接收器
		getContext().unregisterReceiver(mAppInstallListener);
		// 反注册下载广播接收器
		getContext().unregisterReceiver(mDownloadListener);
		// 反注册网络状态监听器
		getContext().unregisterReceiver(mNetWorkListener);
	}

	/**
	 * 当系统有安装，卸载，更新应用等操作时回调该接口
	 * 
	 * @param packName
	 *            安装/卸载/更新的包名
	 */
	public void onAppAction(String packName) {
		mTabManageView.onAppAction(packName);
		// 通知SearchView
		if (mSearchView != null) {
			mSearchView.onAppAction(packName);
		}
	}

	/**
	 * activity onresume
	 */
	public void onResume() {
		mTabManageView.onResume();
		// 通知SearchView
		if (mSearchView != null) {
			mSearchView.onResume();
		}
	}

	/**
	 * 获取连接外网权限 http://api.biz.hiwifi.com/v1/auth/add?timeout=1000
	 */
	private void gotoGainOnline() {
		// 调用接口设置WIFI盒子能连外网
		Setting setting = new Setting(getContext());
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
							@Override
							public void onSuccess(String content) {
								try {
									JSONObject json = new JSONObject(content);
									// 返回时json格式就代表能连接外网
									Intent intent = new Intent(getContext(),
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
									getContext().startService(intent);
									return;
								} catch (Exception e) {
									e.printStackTrace();
								}
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
			}

			@Override
			public void onStart() {
			}

			@Override
			public void onFailure(Throwable error) {
				Log.e("", "onFailure  error = " + error);
			}

			@Override
			public void onFinish() {
			}

		});
	}

	/**
	 * 获取盒子编号
	 */
	@SuppressLint("DefaultLocale")
	private void gotoGainBoxId() {
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.xboxidcontroller),
				new TARequest(XBoxIdController.GAIN_BOXID, XDataDownload
						.getXBoxIdUrl()), new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						String boxId = (String) response.getData();
						if (boxId != null
								&& boxId.toLowerCase().contains("<html>")) {
							Log.e("", "非法BoxID = " + boxId);
							return;
						}
						if (boxId != null) {
							boxId = boxId.trim();
							Setting setting = new Setting(getContext());
							setting.putString(Setting.WIFI_BOX, boxId);
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

	/**
	 * 获取上网时间
	 */
	@SuppressLint("DefaultLocale")
	private void gotoGainTimeOnline() {
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.xtimeonlinecontroller),
				new TARequest(XTimeOnlineController.GAIN_TIMEONLINE,
						XDataDownload.getXTimeOnlineUrl()),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						Setting setting = new Setting(getContext());
						String timeOnline = (String) response.getData();
						if (timeOnline != null
								&& timeOnline.toLowerCase().contains("<html>")) {
							Log.e("", "非法上网时间 = " + timeOnline);
							String localtimeOnline = AppUtils.readAssetsFile(
									getContext(), "timeOnline");
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
									getContext(), "timeOnline");
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
						Setting setting = new Setting(getContext());
						String localtimeOnline = AppUtils.readAssetsFile(
								getContext(), "timeOnline");// 设置默认上网时间
						setting.putString(Setting.TIME_ONLINE, localtimeOnline);

						gotoGainOnline();

					}
				}, true, false);
	}

	/**
	 * 更新应用的下载进度
	 */
	public void notifyDownloadState(DownloadTask downloadTask) {
		// 通知TabManageView
		mTabManageView.notifyDownloadState(downloadTask);
		// 通知SearchView
		if (mSearchView != null) {
			mSearchView.notifyDownloadState(downloadTask);
		}
	}

	@Override
	public int getId() {
		return IDiyFrameIds.MAINVIEWGROUP;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean handleMessage(Object who, int type, final int msgId,
			final int param, final Object object, final List objects) {
		switch (msgId) {
		case IDiyMsgIds.SHOW_SEARCHVIEW:
			if (mSearchView == null) {
				mSearchView = (SearchView) LayoutInflater.from(getContext())
						.inflate(R.layout.search_, null);
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.MATCH_PARENT);
				addView(mSearchView, lp);
				mSlidingTouchMode = ((SlidingActivity) getContext())
						.getSlidingMenu().getTouchModeAbove();
				((SlidingActivity) getContext()).getSlidingMenu()
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			}
			break;
		case IDiyMsgIds.REMOVE_SEARCHVIEW:
			if (mSearchView != null) {
				removeView(mSearchView);
				mSearchView = null;
				((SlidingActivity) getContext()).getSlidingMenu()
						.setTouchModeAbove(mSlidingTouchMode);
			}
			break;
		case IDiyMsgIds.SHOW_X_TIP:
			if (mXTip != null) {
				return false;
			}
			mXTip = new ImageView(getContext());
			mXTip.setImageResource(R.drawable.xtip);
			FrameLayout.LayoutParams xlp = new FrameLayout.LayoutParams(
					DrawUtil.dip2px(getContext(), 143), DrawUtil.dip2px(
							getContext(), 65));
			xlp.gravity = Gravity.TOP | Gravity.RIGHT;
			xlp.topMargin = DrawUtil.dip2px(getContext(), 73);
			xlp.rightMargin = DrawUtil.dip2px(getContext(), 7);
			addView(mXTip, xlp);
			postDelayed(new Runnable() {

				@Override
				public void run() {
					MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
							IDiyMsgIds.REMOVE_X_TIP, -1, null, null);
				}
			}, 6000);
			break;
		case IDiyMsgIds.REMOVE_X_TIP:
			mTabManageView.removeXTipMark();
			removeView(mXTip);
			mXTip = null;
			break;
		default:
			break;
		}
		return false;
	}

	/**
	 * activity onDestory
	 */
	public void onDestory() {
		unRegisterReceiver();
		mTabManageView.onDestory();
		MainActivity.unRegistMsgHandler(this);
	}

}
