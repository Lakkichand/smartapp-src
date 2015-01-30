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
import android.view.ViewGroup;
import android.widget.Button;
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
import com.zhidian.wifibox.util.IdleCounter;
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

	private String mRapName;
	/**
	 * 当前正在显示的页面
	 */
	private IContainer mCurrentContianer;

	public static final String IPCACTION = "com.zhidian.wifibox.IPCACTION";
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
			ModeManager.reCheckRapidly();
			boolean isRap = ModeManager.checkRapidly();
			if (TextUtils.isEmpty(mRapName) && isRap) {
				// 展示提示
				mTabManageView.showXTip();
				// 普通-->>极速
				// 去掉超速页面数据
				TabDataManager.getInstance().resetPageData(
						CDataDownloader.getHomeSpeedingDownloadsUrl());
				// 刷新超速页面
				TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.REFRESH_CONTAINER, -1,
						CDataDownloader.getHomeSpeedingDownloadsUrl(), null);
				// 记录极速名字
				ModeManager.getInstance().recordRapName();
				mRapName = ModeManager.getInstance().getRapName();
				// 获取盒子编号
				gotoGainBoxId();
				// 获取允许上网时间
				gotoGainTimeOnline();
			} else if (!TextUtils.isEmpty(mRapName) && !isRap) {
				// 移除提示
				TAApplication.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
						IDiyMsgIds.REMOVE_X_TIP, -1, null, null);
				// 极速-->>普通
				// 去掉超速页面数据
				TabDataManager.getInstance().resetPageData(
						CDataDownloader.getHomeSpeedingDownloadsUrl());
				// 刷新超速页面
				TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.REFRESH_CONTAINER, -1,
						CDataDownloader.getHomeSpeedingDownloadsUrl(), null);
				// 记录极速名字
				ModeManager.getInstance().recordRapName();
				mRapName = ModeManager.getInstance().getRapName();
				// 保存盒子编号
				Setting setting = new Setting(getContext());
				String boxId = AppUtils.readAssetsFile(getContext(), "boxId");
				setting.putString(Setting.WIFI_BOX, boxId);
			}
		}
	};

	/**
	 * 子进程给主进程发送消息
	 */
	private final BroadcastReceiver mIPCReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int action = intent.getIntExtra("action", -1);
			if (action == 1) {
				// 跳转到游戏：分类
				TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONGAME, null);
				postDelayed(new Runnable() {
					public void run() {
						// 跳转到分类
						TAApplication.sendHandler(null, IDiyFrameIds.ACTIONBAR,
								IDiyMsgIds.JUMP_TITLE, 0, null, null);
					}
				}, 300);
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
		ModeManager.getInstance().recordRapName();
		mRapName = ModeManager.getInstance().getRapName();
		// 注册广播接收器
		registerReceiver();
		initView();
		// 注册消息组件
		TAApplication.registMsgHandler(this);
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
			AsyncHttpClient syncHttpClient = TAApplication.getApplication()
					.getAsyncHttpClient();
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
						TAApplication.sendHandler(null,
								IDiyFrameIds.NAVIGATIONBAR,
								IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
								TabController.NAVIGATIONMANAGE, null);
						Intent i = new Intent(getContext(),
								AppUpdateActivity.class);
						getContext().startActivity(i);
					} else if (jumpToDownloadManager) {
						TAApplication.sendHandler(null,
								IDiyFrameIds.NAVIGATIONBAR,
								IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
								TabController.NAVIGATIONMANAGE, null);
						Intent i = new Intent(getContext(),
								DownloadManagerActivity.class);
						getContext().startActivity(i);
					} else {
						TAApplication.sendHandler(null,
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
				showH5Intro();
				// 检查自动更新
				checkForUpdates();
			}
		}, 3000);
	}

	/**
	 * 展示HTML5标签引导页
	 */
	private void showH5Intro() {
		Setting setting = new Setting(getContext());
		if (setting.getBoolean(Setting.SHOW_H5INTRO, false)) {
			return;
		}
		if (mTabManageView != null) {
			IContainer container = mTabManageView.getCurrentContainer();
			if (container != null && container instanceof HomeFeatureContainer) {
				HomeFeatureNavigation navigation = ((HomeFeatureContainer) container)
						.getNavigation();
				if (navigation != null && navigation.getH5Btn() != null) {
					Button btn = navigation.getH5Btn();
					if (btn.getWidth() > 0 && btn.getHeight() > 0
							&& btn.getRight() > 0 && btn.getBottom() > 0) {
						int height = DrawUtil
								.dip2px(getContext(), 45 + 40 + 30)
								+ navigation.getTop() + navigation.getHeight();
						int gap = DrawUtil.dip2px(getContext(), 2);
						int clipLeft = btn.getLeft() - gap;
						int clipTop = ((ViewGroup) (((HomeFeatureContainer) container)
								.getParent())).getTop()
								+ navigation.getTop()
								+ btn.getTop() - gap;
						int clipRight = clipLeft + btn.getWidth() + 2 * gap;
						int clipBottom = clipTop + btn.getHeight() + 2 * gap;
						H5Intro intro = new H5Intro(getContext(),
								MainViewGroup.this.getWidth(), height,
								clipLeft, clipTop, clipRight, clipBottom);
						FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
								FrameLayout.LayoutParams.MATCH_PARENT,
								FrameLayout.LayoutParams.MATCH_PARENT);
						addView(intro, lp);
						setting.putBoolean(Setting.SHOW_H5INTRO, true);
					}
				}
			}
		}
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
		// 进程间通信
		intentFilter = new IntentFilter();
		intentFilter.addAction(IPCACTION);
		getContext().registerReceiver(mIPCReceiver, intentFilter);
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
		// 进程间通信
		getContext().unregisterReceiver(mIPCReceiver);
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
		// 页面统计
		IContainer newContainer = mSearchView;
		if (newContainer == null) {
			newContainer = mTabManageView.getCurrentContainer();
		}
		changeActiveContainer(mCurrentContianer, newContainer);
	}

	/**
	 * activity onpause
	 */
	public void onPause() {
		// 页面统计
		changeActiveContainer(mCurrentContianer, null);
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
				ModeManager.getInstance().setExtranet(false);
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
									if (ex_content.toLowerCase().trim()
											.equals("ok")) {
										getContext().startService(intent);
									}
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
								ModeManager.getInstance().setExtranet(false);
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
							if (!TextUtils.isEmpty(boxId.trim())) {
								Setting setting = new Setting(getContext());
								setting.putString(Setting.WIFI_BOX,
										boxId.trim());
								// 获取地理位置，一天内只获取某个盒子位置一次
								long lasttime = setting.getLong(Setting.MIBAO_LOCATION_TIME_PREFIX
										+ InfoUtil.getBoxId(TAApplication
												.getApplication()));
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
			changeActiveContainer(mCurrentContianer, mSearchView);
			break;
		case IDiyMsgIds.REMOVE_SEARCHVIEW:
			if (mSearchView != null) {
				removeView(mSearchView);
				mSearchView = null;
				((SlidingActivity) getContext()).getSlidingMenu()
						.setTouchModeAbove(mSlidingTouchMode);
			}
			changeActiveContainer(mCurrentContianer,
					mTabManageView.getCurrentContainer());
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
					TAApplication.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
							IDiyMsgIds.REMOVE_X_TIP, -1, null, null);
				}
			}, 6000);
			break;
		case IDiyMsgIds.REMOVE_X_TIP:
			mTabManageView.removeXTipMark();
			removeView(mXTip);
			mXTip = null;
			break;
		case IDiyMsgIds.SHOW_SPEEDING_DOWNLOAD:
			// 返回MainActivity
			Intent intent = new Intent(TAApplication.getApplication(),
					MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			TAApplication.getApplication().startActivity(intent);
			// 去除搜索
			TAApplication.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
					IDiyMsgIds.REMOVE_SEARCHVIEW, -1, null, null);
			// 返回第一层
			for (int i = TabDataManager.getInstance().getTabStackSize(); i > 1; i--) {
				TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.BACK_ON_ONE_LEVEL, -1, null, null);
			}
			// 跳转到首页
			TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
					IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
					TabController.NAVIGATIONFEATURE, null);
			postDelayed(new Runnable() {

				@Override
				public void run() {
					// 跳转到超速下载
					TAApplication.sendHandler(null, IDiyFrameIds.ACTIONBAR,
							IDiyMsgIds.JUMP_TITLE, 3, null, null);
				}
			}, 50);
			break;
		case IDiyMsgIds.UPDATE_CURRENT_CONTAINER:
			// 搜索页不显示时，应用列表页才算显示
			if (mSearchView == null) {
				changeActiveContainer(mCurrentContianer,
						mTabManageView.getCurrentContainer());
			}
			break;
		default:
			break;
		}
		return false;
	}

	/**
	 * 更换当前的正在显示的页面
	 */
	private void changeActiveContainer(final IContainer oldContainer,
			final IContainer newContainer) {
		if (oldContainer == newContainer) {
			return;
		}
		post(new Runnable() {

			@Override
			public void run() {
				if (oldContainer != null) {
					oldContainer.endPage();
				}
				if (newContainer != null) {
					newContainer.beginPage();
				}
				mCurrentContianer = newContainer;
			}
		});
	}

	/**
	 * activity onDestory
	 */
	public void onDestory() {
		unRegisterReceiver();
		mTabManageView.onDestory();
		TAApplication.unRegistMsgHandler(this);
	}

}
