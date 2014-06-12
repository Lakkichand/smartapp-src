package com.zhidian.wifibox.view;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.util.AttributeSet;
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
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.MainController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.controller.XBoxIdController;
import com.zhidian.wifibox.controller.XTimeOnlineController;
import com.zhidian.wifibox.data.AutoUpdateBean;
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
import com.zhidian.wifibox.view.dialog.SwitchModeDialog;
import com.zhidian.wifibox.view.dialog.XNetDialog;

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
	 * 普通模式提示页
	 */
	private CModeTipPage mCTipPage;
	/**
	 * 极速模式提示页
	 */
	private XModeTipPage mXTipPage;
	/**
	 * 展示搜索页面前的侧边栏touchmode
	 */
	private int mSlidingTouchMode = SlidingMenu.TOUCHMODE_FULLSCREEN;
	/**
	 * 网络状态
	 */
	private int mNetMode = ModeManager.COMMON_MODE;
	/**
	 * 从极速模式切换到普通模式的对话框
	 */
	private SwitchModeDialog mSwitchModeDialog = null;
	/**
	 * 从普通模式切换到极速模式的提示UI
	 */
	private SwitchImageView mSwitchModeTips = null;
	/**
	 * 是否已经展示从普通模式切换到极速模式的提示UI
	 */
	private boolean mHasShowSwitchModeTips = false;
	/**
	 * 启动页
	 */
	private View mCover;

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
			if ((isRap && mNetMode == ModeManager.COMMON_MODE)
					|| (!isRap && mNetMode == ModeManager.X_MODE)) {
				mHasShowSwitchModeTips = false;
				// 当前是普通模式，可以切换到极速模式，通知MainViewGroup提示切换
				showSwitchModeTip();
			} else {
				// 取消提示
				removeSwitchModeTip();
				if (isRap && mNetMode == ModeManager.X_MODE) {
					// 如果是极速模式但连上了不同的盒子，则提示用户切换网络
					String rapName = ModeManager.getInstance().getRapName();
					if (rapName != null
							&& !rapName.equals(InfoUtil
									.getCurWifiName(getContext()))) {
						if (XNetDialog.sDialog != null) {
							XNetDialog.sDialog.show();
						}
					} else {
						if (XNetDialog.sDialog != null) {
							XNetDialog.sDialog.dismiss();
						}
					}
				}
			}
		}
	};

	public MainViewGroup(Context context) {
		super(context);
		init();
	}

	public MainViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MainViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		// 当前网络模式
		mNetMode = ModeManager.getInstance().isRapidly() ? ModeManager.X_MODE
				: ModeManager.COMMON_MODE;
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
						+ "/adv.png";
			} else {
				filename = PathConstant.AD_PATH_CACHE
						+ InfoUtil.getCurWifiName(getContext())
								.replaceAll("\"", "").replaceAll(" ", "")
						+ "/adv.png";
			}
			// 先读取门店广告
			Bitmap bm = BitmapFactory.decodeFile(filename);
			if (bm == null) {
				image.setImageResource(R.drawable.start_front_green);
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
			image.setImageResource(R.drawable.start_front_blue);
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
	 * 网络模式发生改变
	 */
	public void onModeChange() {
		if (ModeManager.checkRapidly()) {
			// 获取允许上网时间
			gotoGainTimeOnline();
			// 调用接口设置WIFI盒子能连外网
			// AsyncHttpClient client = new AsyncHttpClient();
			// client.get("http://api.biz.hiwifi.com/v1/auth/add",
			// new AsyncHttpResponseHandler() {
			// @Override
			// public void onSuccess(String content) {
			// Log.e("", "onSuccess  content = " + content);
			// }
			//
			// @Override
			// public void onStart() {
			// }
			//
			// @Override
			// public void onFailure(Throwable error) {
			// Log.e("", "onFailure  error = " + error);
			// }
			//
			// @Override
			// public void onFinish() {
			// }
			//
			// });
		}
		// 通知DownloadService当前网络模式
		getContext().sendBroadcast(
				new Intent(IDownloadInterface.DOWNLOAD_MODECHANGE_ACTION));
		// 当前网络模式
		mNetMode = ModeManager.getInstance().isRapidly() ? ModeManager.X_MODE
				: ModeManager.COMMON_MODE;
		// 通知TabManageView
		mTabManageView.onModeChange();
		// 根据当前是普通模式或急速模式通知导航栏跳转
		if (ModeManager.getInstance().isRapidly()) {
			MainActivity.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
					IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
					TabController.XNAVIGATIONNEW, null);
			// 获取盒子编号
			gotoGainBoxId();

		} else {
			Setting setting = new Setting(getContext());
			String boxId = AppUtils.readAssetsFile(getContext(), "boxId");
			setting.putString(Setting.WIFI_BOX, boxId);// 保存盒子编号

			MainActivity.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
					IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
					TabController.NAVIGATIONFEATURE, null);
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
				Intent intent = new Intent(getContext(),
						InternetTimeService.class);
				int timeout = InfoUtil.DEFAULT_INTERNET_TIME;
				try {
					timeout = Integer.valueOf(timeOnline);
				} catch (Exception e) {
					e.printStackTrace();
				}
				intent.putExtra(InternetTimeService.INTERNET_TIME_KEY, timeout);
				getContext().startService(intent);
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
		case IDiyMsgIds.SWITCH_MODE:
			onModeChange();
			break;
		case IDiyMsgIds.SHOW_C_TIPS_PAGE:
			if (mCTipPage == null) {
				if (mCover != null) {
					mCover.setVisibility(View.GONE);
				}
				LayoutInflater inflater = LayoutInflater.from(getContext());
				mCTipPage = (CModeTipPage) inflater.inflate(
						R.layout.cmode_tip_page, null);
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.MATCH_PARENT);
				this.addView(mCTipPage, lp);
			}
			break;
		case IDiyMsgIds.SHOW_X_TIPS_PAGE:
			if (mXTipPage == null) {
				if (mCover != null) {
					mCover.setVisibility(View.GONE);
				}
				LayoutInflater inflater = LayoutInflater.from(getContext());
				mXTipPage = (XModeTipPage) inflater.inflate(
						R.layout.xmode_tip_page, null);
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.MATCH_PARENT);
				this.addView(mXTipPage, lp);
			}
			break;
		case IDiyMsgIds.REMOVE_C_TIPS_PAGE:
			if (mCTipPage != null) {
				removeView(mCTipPage);
				mCTipPage = null;
			}
			break;
		case IDiyMsgIds.REMOVE_X_TIPS_PAGE:
			if (mXTipPage != null) {
				removeView(mXTipPage);
				mXTipPage = null;
			}
			break;
		case IDiyMsgIds.SHOW_SWITCH_MODE_TIP:
			boolean isRap = ModeManager.checkRapidly();
			if (isRap && mNetMode == ModeManager.COMMON_MODE) {
				if (mSwitchModeTips == null
						&& TabDataManager.getInstance().getTabStackSize() == 1
						&& !mHasShowSwitchModeTips) {
					mHasShowSwitchModeTips = true;
					mSwitchModeTips = new SwitchImageView(getContext(),
							getContext().getResources().getDrawable(
									R.drawable.tips1), getContext()
									.getResources().getDrawable(
											R.drawable.tips2));
					mSwitchModeTips.setClickable(false);
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
							DrawUtil.dip2px(getContext(), 190.8f),
							DrawUtil.dip2px(getContext(), 76.4399999961f));
					lp.gravity = Gravity.RIGHT | Gravity.TOP;
					lp.topMargin = DrawUtil.dip2px(getContext(), 38);
					lp.rightMargin = DrawUtil.dip2px(getContext(), 8);
					addView(mSwitchModeTips, lp);
				}
			}
			break;
		case IDiyMsgIds.REMOVE_SWITCH_MODE_TIP:
			if (mSwitchModeTips != null) {
				removeView(mSwitchModeTips);
				mSwitchModeTips = null;
			}
			break;
		default:
			break;
		}
		return false;
	}

	/**
	 * 提示用户可以切换网络模式
	 */
	private void showSwitchModeTip() {
		boolean isRap = ModeManager.checkRapidly();
		if (isRap && mNetMode == ModeManager.COMMON_MODE) {
			if (mSwitchModeTips == null
					&& TabDataManager.getInstance().getTabStackSize() == 1
					&& !mHasShowSwitchModeTips) {
				mHasShowSwitchModeTips = true;
				mSwitchModeTips = new SwitchImageView(getContext(),
						getContext().getResources().getDrawable(
								R.drawable.tips1), getContext().getResources()
								.getDrawable(R.drawable.tips2));
				mSwitchModeTips.setClickable(false);
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						DrawUtil.dip2px(getContext(), 190.8f), DrawUtil.dip2px(
								getContext(), 76.4399999961f));
				lp.gravity = Gravity.RIGHT | Gravity.TOP;
				lp.topMargin = DrawUtil.dip2px(getContext(), 38);
				lp.rightMargin = DrawUtil.dip2px(getContext(), 8);
				addView(mSwitchModeTips, lp);
			}
		} else if (!isRap && mNetMode == ModeManager.X_MODE) {
			if (mSwitchModeDialog != null && mSwitchModeDialog.isShowing()) {
				return;
			}
			mSwitchModeDialog = new SwitchModeDialog(getContext());
			mSwitchModeDialog.show();
		}
	}

	/**
	 * 移除可以切换网络模式的提示
	 */
	private void removeSwitchModeTip() {
		if (mSwitchModeDialog != null) {
			mSwitchModeDialog.dismiss();
			mSwitchModeDialog = null;
		}
		if (mSwitchModeTips != null) {
			removeView(mSwitchModeTips);
			mSwitchModeTips = null;
		}
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
