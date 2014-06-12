package com.jiubang.ggheart.apps.desks.diy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.ClL;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.HolographicOutlineHelper;
import com.go.util.log.LogUnit;
import com.go.util.window.WindowControl;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.MessageManager;
import com.jiubang.ggheart.analytic.Analyst;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingVisualIconTabView;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Search;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideForGlFrame;
import com.jiubang.ggheart.apps.desks.settings.AutoFitDeviceManager;
import com.jiubang.ggheart.apps.desks.snapshot.SnapShotManager;
import com.jiubang.ggheart.components.DeskActivity;
import com.jiubang.ggheart.components.DeskAlertDialog;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.components.advert.AdvertControl;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.DatabaseHelper;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.tables.SettingTable;
import com.jiubang.ggheart.data.theme.broadcastReceiver.MyThemeReceiver;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.ThreadName;
import com.jiubang.ggheart.plugin.shell.ShellPluginFactory;

/**
 * DIY桌面主Activity,DIY入口
 * 
 * @author yuankai
 * @version 1.0
 */
public class GoLauncher extends DeskActivity {
	private static final int FLAG_HARDWARE_ACCELERATED = 0x01000000; // 该值是3.0版本开始有的固定值
	static final int WALLPAPER_SCREENS_SPAN = 2;
	static final boolean LOG_TO_SD = false;
	// 系统Home键的可用标识
	private static boolean sSystemHomeKeyAct = false;

	protected FrameLayout mMainLayout;
	private FrameLayout mFrameLayout;
	private FrameControl mFrameControl;
	private DiyScheduler mScheduler;
	private MessageManager mMessageManager;

	// 第一次运行标记
	private boolean mFirstRun;
	// 新版本第一次运行标记
	private boolean mNewVersionFirstRun;

	private static GoLauncher sContext = null;

	private static int sStartTutorialMask = -1;

	private boolean mNeedRStart = false; // 设置项改变需要回到桌面时重启

	// 用于在其他桌面点击通讯统计图标时，intent中包含的信息字段
//	private static final String GOTO_NOTIFICATION_SETTING_DETAIL = "goto_notification_setting_detail";

	private Analyst mAnalyst = null;

	private int mOpenAppType; // 打开app的动画方式
	private int mLastVersionCode = -1; //更新前的版本号，只在初次启动时有效，以后启动该值与当前版本号相同
	public static final int TYPE_OPEN_APP_NONE = 0; // 无动画
	public static final int TYPE_OPEN_APP_SMALL2BIG = 1; // 由小变大动画
	
	// Handler消息
	private final int mGOOGLE_ANALYTICS = 1;
	private MyThemeReceiver mReceiver_screenedit ;  //添加界面桌面主题设置广播
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case mGOOGLE_ANALYTICS:
				try {
					if (null != mAnalyst) {
						mAnalyst.stopAnalysation();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
	
	public static final int APPFUNC_SEARCH_SUPPORT_MEDIA_VERSION_NUM = 133; //应用搜索支持多媒体搜索版本号
	
	public static final int MEDIA_MANAGEMENT_BECOMES_PLUGIN = 158; //资源管理独立为插件版本号，暂未确定

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hardwareAcceleratedByWindow();
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 未初始化完前强制为竖屏
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 未初始化完前强制为横屏
		}

		DeskResourcesConfiguration.createInstance(this);
		// 初始化log
		initLog();

		sContext = this;

		// 设置density, 全局使用
		DrawUtils.resetDensity(this);
		HolographicOutlineHelper.resetDensity(DrawUtils.sDensity);
		mMessageManager = new MessageManager();

		// 加载主界面
		// mFrameLayout = new DiyFrameLayout(this);
		// setContentView(mFrameLayout, new
		// LayoutParams(LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT));
		setContentView(R.layout.main);
		mMainLayout = (FrameLayout) findViewById(R.id.launcherLayout);
		mFrameLayout = (FrameLayout) mMainLayout
				.findViewById(R.id.diyFrameLayout);
		mFrameControl = new FrameControl(this, mFrameLayout, mMessageManager);

		// 第一次运行，必要在AppCore.build之前检测，否则一些自适应的设置会失效
		checkFirstRun();
		// 当前设备设置自适应
		autoFitDeviceSetting();
		// 功能表模块设置自适应
		autoFitAppFuncSetting();
		// 构造AppCore
		AppCore.build(getApplication());		
		// 设置全局消息过滤器
		mMessageManager.setFilter(AppCore.getInstance());

		// 新版本第一次运行
		checkNewVersionFirstRun();
		// 第一次运行检查设置信息
		checkSettingInfoFirstRun();

		// 设置壁纸分辨率
		// setWallpaperDimension();
		// setWallpaperDimensionWithDefaultPicSize();

		// 添加调度层，由调度层去根据后台数据不同，进行不同的加载动作
		mScheduler = new DiyScheduler(this, mFrameControl,
				IDiyFrameIds.SCHEDULE_FRAME);
		mScheduler.onCreate(savedInstanceState);

		// 设置壁纸分辨率
		WallpaperDensityUtil.setWallpaperDimension(this);

		// 通知周边插件桌面启动
		sendBroadcast(new Intent(ICustomAction.ACTION_LAUNCHER_START));

		// 启动统计.发送信息给Analysation
		new Thread(ThreadName.START_GOOGLE_ANALYTICS) {
			@Override
			public void run() {
				try {
					mAnalyst = new Analyst(GoLauncher.this);
					if (null != mAnalyst) {
						mAnalyst.startAnalysation();
						mAnalyst.uploadReferrerInfo();
						mHandler.sendEmptyMessageDelayed(mGOOGLE_ANALYTICS, 20 * 60 * 1000);
					}
				} catch (Throwable e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}.start();

		try {
			if (Machine.isTablet(sContext)) {
				if (DataProvider.getInstance(this).isNewDB()) {
					saveSharedPreferences(false);
				} else {
					if (mNewVersionFirstRun && getShowPadDialogPref()) {
						toastDialog(sContext);
					}
				}
			} else if (Machine.isONE_X() && !Machine.IS_ICS_MR1) {
				// 恢复默认或者初次安装则弹窗
				if (mFirstRun || mNewVersionFirstRun) {
					guideNotify(sContext);
				}
			}
		} catch (Throwable e) {
			// TODO: handle exception
		}
		checkIntentDetailData(getIntent());
		clDH();
		// //检测不保留后台活动
		checkAFA();
		// 升级或全新安装，检测到语言匹配，则弹出提示框。
		checkLanguage();
		// 弹出语言设置通知栏
		showLanguageSetting();
		
//		Intent in = new Intent();
//		in.setClass(GOLauncherApp.getContext(), DownloadService.class);
//		GOLauncherApp.getContext().startService(in);
//		GOLauncherApp.getApplication().bindDownloadService();
		
		//为添加界面的主题设置增加动态广播接收,防止广播接收延迟
		mReceiver_screenedit = new MyThemeReceiver();
		IntentFilter filter = new IntentFilter(ICustomAction.ACTION_THEME_BROADCAST_SCREENEDIT);
		this.registerReceiver(mReceiver_screenedit, filter);
		
		//facebook初始化
		GoFacebookUtil.initEnable(this);
		
		//Guide导航页初始化
		GuideControler.getInstance(this);
	}
	
	/**
	 * 判断桌面intent是否有携带自定义数据
	 * 
	 * @param intent
	 */
	private void checkIntentDetailData(Intent intent) {
		if (null != intent) {
			String pkg = null;
			// String gotoNotification = null;
			try {
				pkg = intent
						.getStringExtra(ICustomAction.DATA_PKG_GOTO_SPECIFICK_WIDGET_DETAIL);
				// gotoNotification =
				// intent.getStringExtra(GOTO_NOTIFICATION_SETTING_DETAIL);
			} catch (Exception e) {
				// e.printStackTrace();
			}

			if (null != pkg) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_GOTO_GOWIDGET_PAGE, -1, pkg, null);
			}
			// if (null != gotoNotification) {
			// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
			// IDiyMsgIds.SCREEN_OPEN_NOTIFICATION_SETTING, -1, null, null);
			// }
		}
	}

	private void initLog() {
		if (LOG_TO_SD) {
			LogUnit.clearLogcat();
			LogUnit.sdMountListener(this, true);
			Timer timer = new Timer("write logcat", false);
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					LogUnit.writeLogcatToSD();
				}
			}, 1000 * 60 * 5, 1000 * 60 * 5);
			LogUnit.setWriteFile(true);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (LOG_TO_SD) {
			LogUnit.sdMountListener(this, false);
			if (LogUnit.isWriteFile()) {
				LogUnit.setWriteFile(false);
			}
		}

		// 先执行mScheduler的清理，依赖于mFrameManager的数据
		mScheduler.onDestroy();
		mFrameControl.cleanup();
		AppCore.destroy();
		mMessageManager.cleanup();
		sContext = null;

		// 通知周边插件桌面退出
		sendBroadcast(new Intent(ICustomAction.ACTION_LAUNCHER_EXIT));

		// 设置壁纸分辨率
		setWallpaperDimension();

		AppUtils.cancelNotificaiton(this, INotificationId.ONE_X_GUIDE);
		AppUtils.cancelNotificaiton(this, INotificationId.LANGUAGE_START_GGMENU);
		AppUtils.cancelNotificaiton(this, INotificationId.MIGRATE_TIP);
		
		//反注册添加界面桌面主题设置广播
		this.unregisterReceiver(mReceiver_screenedit);
		// 结束自己
		AppUtils.killProcess();

	}

	public static GoLauncher getContext() {
		return sContext;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		DrawUtils.resetDensity(this);
		// 向下传
		mScheduler.onConfigurationChanged(newConfig);
		
	}

	@Override
	protected void onStart() {
		// 通知周边插件桌面onStart
		sendBroadcast(new Intent(ICustomAction.ACTION_LAUNCHER_ONSTART));
		super.onStart();
		mScheduler.onStart();
	}

	@Override
	protected void onRestart() {
		if (isUnderSDK14() && mOpenAppType == TYPE_OPEN_APP_SMALL2BIG) {
			overridePendingTransition(R.anim.zoom_golauncher_enter,
					R.anim.zoom_exit);
		}
		super.onRestart();
	}

	@Override
	protected void onStop() {
		if (isUnderSDK14() && mOpenAppType == TYPE_OPEN_APP_SMALL2BIG) {
			overridePendingTransition(R.anim.zoom_enter,
					R.anim.zoom_golauncher_exit);
		}
		super.onStop();
		// 通知周边插件桌面onStop
		sendBroadcast(new Intent(ICustomAction.ACTION_LAUNCHER_STOP));
		mScheduler.onStop();
	}

	@Override
	protected void onPause() {
		if (isUnderSDK14() && mOpenAppType == TYPE_OPEN_APP_SMALL2BIG) {
			overridePendingTransition(R.anim.zoom_enter,
					R.anim.zoom_golauncher_exit);
		}
		super.onPause();
		mScheduler.onPause();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mScheduler.dispatchKeyEvent(event)) {
			return true;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mScheduler.dispatchTouchEvent(event)) {
			return true;
		} else {
			return super.dispatchTouchEvent(event);
		}
	}

	@Override
	public boolean dispatchTrackballEvent(MotionEvent event) {
		if (mScheduler.dispatchTrackballEvent(event)) {
			return true;
		} else {
			return super.dispatchTrackballEvent(event);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mScheduler.onKeyDown(keyCode, event)) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (mScheduler.onKeyUp(keyCode, event)) {
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}
	
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mScheduler.onNewIntent(intent);

		checkIntentDetailData(intent);
		showLanguageGGMenu(intent);
	}

	@Override
	protected void onResume() {
		if (mNeedRStart) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
		}
		if (isUnderSDK14() && mOpenAppType == TYPE_OPEN_APP_SMALL2BIG) {
			overridePendingTransition(R.anim.zoom_golauncher_enter,
					R.anim.zoom_exit);
		}
		super.onResume();

		sContext = this;
		mScheduler.onResume();
		if (sStartTutorialMask != LauncherEnv.MASK_TUTORIAL_NONE) {
			startTutorialFrame();
			sStartTutorialMask = LauncherEnv.MASK_TUTORIAL_NONE;
		}
		// 壁纸滚动
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mWallpaperScroll) {
			// M9
			if (Machine.isM9()) {
				WallpaperDensityUtil.setWallpaperDimension(this);
			}
		}
		// setWallpaperDimensionWithDefaultPicSize();
		// new Thread(ThreadName.KILL_OTHERLAUNCHER){
		// public void run(){
		// try {
		// Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// killOtherLauncher();
		// }
		// }.start();
		
		// 是否显示提醒评分对话框
		checkRate();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			try {
				GoSettingControler controler = GOLauncherApp
						.getSettingControler();
				if (controler != null) {
					boolean isFullScreen = !controler.getDesktopSettingInfo().mShowStatusbar;
					// if(Machine.isTablet(sContext))
					// {
					// isFullScreen = true;
					// }
					if (isFullScreen) {
						WindowControl.setIsFullScreen(this, isFullScreen);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			SnapShotManager.getInstance(GOLauncherApp.getContext()).resumeCapture();
		} else {
			SnapShotManager.getInstance(GOLauncherApp.getContext()).pauseCapture();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mScheduler.onActivityResult(requestCode, resultCode, data);
	}

	// private void setWallpaperDimension()
	// {
	// WallpaperManager wpm = (WallpaperManager)
	// getSystemService(WALLPAPER_SERVICE);
	//
	// Display display = getWindowManager().getDefaultDisplay();
	// boolean isPortrait = display.getWidth() < display.getHeight();
	//
	// final int width = isPortrait ? display.getWidth() : display.getHeight();
	// final int height = isPortrait ? display.getHeight() : display.getWidth();
	// try
	// {
	// wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
	// }
	// catch (Exception e)
	// {
	// Log.i(LogConstants.HEART_TAG, "fail to setWallpaperDimension");
	// }
	// }

	/**
	 * 为特殊机型设置壁纸原始宽高，避免出现壁纸缩放问题
	 */
	private void setWallpaperDimensionWithDefaultPicSize() {
		WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
		try {
			/**
			 * 把建议宽高设为<0,系统会返回不经任何缩放的壁纸图片 NOTE：一般的Launcher是在onCreate时把这个size设死为
			 * （宽*2，高） 这样会使不为（宽*2，高）的壁纸有缩放，如i9000原生ROM可以剪切（宽，高）的壁纸，
			 * 这时出来的效果就是放大了一倍。
			 */
			wpm.suggestDesiredDimensions(-1, -1);
		} catch (IllegalArgumentException e) {
			// NOTE:目前只发现在三星原生ROM时设置为<0不报这个异常，所以我们做法是：
			// 如果可以设置<0来拿不缩放壁纸，就这样做，如果报异常，就不改设置，用原来的（宽*2，高）
		}
	}

	@Override
	public void startSearch(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {
		// 当前显示的是屏幕
		if (mFrameControl.isScreenOnTop()) {
			final AbstractFrame screen = mFrameControl
					.getFrame(IDiyFrameIds.SCREEN_FRAME);
			if (screen != null) {
				ScreenFrame screenFrame = (ScreenFrame) screen;
				Search searchWidget = ((ScreenFrame) screen)
						.findSearchWidgetOnCurrentScreen();
				if (searchWidget == null) {
					if (mScheduler != null) {
						mScheduler.showSearchDialog(null, false, null, true);
					}
				} else {
					searchWidget.startSearch(initialQuery, selectInitialQuery,
							appSearchData, globalSearch);
					// show the currently typed text in the search widget while
					// sliding
					searchWidget.setQuery(screenFrame.getTypedText());
				}
			}
		} else {
			super.startSearch(initialQuery, selectInitialQuery, appSearchData,
					globalSearch);
		}
	}

	/**
	 * 功能简述:消息发送接口 功能详细描述: 注意:
	 * 
	 * @param who
	 *            发送者
	 * @param type
	 *            消息类型
	 * @param handlerId
	 *            接收的ID
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objList
	 *            传递的对象队列
	 * @return 是否处理
	 */
	public static boolean sendMessage(Object who, int type, int handlerId,
			int msgId, int param, Object object, List<?> objList) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.send(who, type, handlerId, msgId,
					param, object, objList);
		}
		return false;
	}

	/**
	 * 功能简述:同步广播消息 功能详细描述: 注意:
	 * 
	 * @param who
	 *            发送者
	 * @param handlerId
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列 return 是否处理
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
	 * 功能简述: 同步广播消息 功能详细描述: 注意:
	 * 
	 * @param who
	 *            发送者
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 */
	public static void sendBroadcastMessage(Object who, int msgId, int param,
			Object object, List<?> objList) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.sendBroadcast(who, msgId, param, object,
					objList);
		}
	}

	/**
	 * 功能简述: 带先后次序的同步广播消息 功能详细描述: 注意:
	 * 
	 * @param who
	 *            发送者
	 * @param type
	 *            消息类型
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 * @param orderedHandlers
	 *            要求被排在前面的接收者ID数组
	 */
	public static void sendBroadcastMessage(Object who, int type, int msgId,
			int param, Object object, List<?> objList,
			final int[] orderedHandlers) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.sendBroadcast(who, type, msgId, param,
					object, objList, orderedHandlers);
		}
	}

	/**
	 * 异步发送消息
	 * 
	 * @param who
	 *            发送者
	 * @param handlerId
	 *            接收者的ID
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 */
	public static void postMessage(Object who, int handlerId, int msgId,
			int param, Object object, List<?> objList) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.post(who, handlerId, msgId, param, object,
					objList);
		}
	}

	/**
	 * 异步广播消息
	 * 
	 * @param who
	 *            发送者
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 */
	public static void postBroadcastMessage(Object who, int msgId, int param,
			Object object, List<?> objList) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.postBroadcast(who, msgId, param, object,
					objList);
		}
	}

	public static void postBroadcastMessage(Object who, int msgId, int param,
			Object object, List<?> objList, final int[] orderedHandlers) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.postBroadcast(who, msgId, param, object,
					objList, orderedHandlers);
		}
	}

	/**
	 * 点对点发送到UI线程上的消息
	 * 
	 * @param who
	 *            发送者指针
	 * @param handlerId
	 *            接收者id
	 * @param msgId
	 *            消息id
	 * @param param
	 * @param object
	 * @param objects
	 */
	public static void sendHandler(Object who, int handlerId, int msgId,
			int param, Object object, List<? extends Object> objects) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.sendHandler(who, handlerId, msgId, param,
					object, objects);
		}
	}

	/**
	 * 广播发送到UI线程上的消息
	 * 
	 * @param who
	 *            发送者指针
	 * @param msgId
	 *            消息id
	 * @param param
	 * @param object
	 * @param objects
	 */
	public static void sendBroadcastHandler(Object who, int msgId, int param,
			Object object, List<? extends Object> objects) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.sendBroadcastHandler(who, msgId, param,
					object, objects);
		}
	}

	/**
	 * 提交一次异步执行的请求
	 * 
	 * @param who
	 * @param runnable
	 *            需要在异步线程上执行的操作
	 * @param atFront
	 *            是否优先执行
	 */
	public static void postRunnable(Object who, Runnable runnable,
			boolean atFront) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.postRunnable(who, runnable, atFront);
		}
	}

	/**
	 * 提交一次在UI线程上执行的请求
	 * 
	 * @param who
	 * @param runnable
	 *            需要在UI线程上执行的操作
	 * @param atFront
	 *            是否优先执行
	 */
	public static void postUiRunnable(Object who, Runnable runnable,
			boolean atFront) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.postUiRunnable(who, runnable, atFront);
		}
	}

	/**
	 * 注册消息接收者
	 * 
	 * @param handler
	 * @return
	 */
	public static boolean registMsgHandler(final IMessageHandler handler) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.registMsgHandler(handler);
		}
		return false;
	}

	/**
	 * 指定特定消息， 注册消息接收者
	 * 
	 * @param handler
	 * @return
	 */
	public static boolean registMsgHandler(final IMessageHandler handler,
			final int msgId) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.registMsgHandler(handler, msgId);
		}
		return false;
	}

	/**
	 * 反注册消息接收者 与{@link GoLauncher#registMsgHandler(IMessageHandler)} 配对使用
	 * 
	 * @param handler
	 * @param msgId
	 * @return
	 */
	public static boolean unRegistMsgHandler(final IMessageHandler handler,
			final int msgId) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.unRegistMsgHandler(handler, msgId);
		}
		return false;
	}

	/**
	 * 反注册消息接收者 与{@link GoLauncher#registMsgHandler(IMessageHandler, int)}配对使用
	 * 
	 * @param handler
	 * @param msgId
	 * @return
	 */
	public static boolean unRegistMsgHandler(IMessageHandler handler) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.unRegistMsgHandler(handler);
		}
		return false;
	}

	/**
	 * 检查是否全屏（通知栏不显示）
	 * 
	 * @return 是否全屏
	 */
	public static boolean isFullScreen() {
		if (sContext != null /* && !Machine.isTablet(sContext) */) {
			return WindowControl.getIsFullScreen(sContext);
		}
		return false;
	}

	/**
	 * 获取状态栏高度
	 * 
	 * @return 全屏时返回0，否则返回状态栏实际高度(px)
	 */
	public static int getStatusbarHeight() {
		boolean isFullScreen = isFullScreen();
		if (!isFullScreen) {
			return StatusBarHandler.getStatusbarHeight();
		}
		return 0;
	}

	/**
	 * 屏幕高度(px)
	 * 
	 * @return
	 */
	public static int getScreenHeight() {
		if (Machine.isTablet(sContext)) {
			return DrawUtils.getTabletScreenHeight(sContext);
		}
		return DrawUtils.sHeightPixels;
	}

	/**
	 * 屏幕宽度(px)
	 * 
	 * @return
	 */
	public static int getScreenWidth() {
		if (Machine.isTablet(sContext)) {
			return DrawUtils.getTabletScreenWidth(sContext);
		}
		return DrawUtils.sWidthPixels;
	}

	/**
	 * 获取GOLauncher实际显示高度(px) 跟当前横竖屏状态有关
	 * 
	 * @return
	 */
	public static int getDisplayHeight() {
		int height = getScreenHeight() - getStatusbarHeight();
		return height;
	}

	/**
	 * 获取GOLauncher实际显示宽度(px) 跟当前横竖屏状态有关
	 * 
	 * @return
	 */
	public static int getDisplayWidth() {
		return getScreenWidth();
	}

	/**
	 * 获取横竖屏模式 {@link Configuration#ORIENTATION_LANDSCAPE} or
	 * {@link Configuration#ORIENTATION_PORTRAIT}
	 * 
	 * @return
	 */
	public static int getOrientation() {
		return (isPortait()) ? Configuration.ORIENTATION_PORTRAIT
				: Configuration.ORIENTATION_LANDSCAPE;
	}

	public static boolean isPortait() {
		if (getScreenHeight() > getScreenWidth()) {
			return true;
		}
		return false;
	}

	public static boolean getSystemHomeKeyAct() {
		return sSystemHomeKeyAct;
	}

	public static void setSystemHomeKeyAct(boolean systemHomeKeyAct) {
		GoLauncher.sSystemHomeKeyAct = systemHomeKeyAct;
	}

	/**
	 * 是否开启高质量绘图
	 * 
	 * @return 高质量返回true
	 */
	public static boolean isHighQualityDrawing() {
		ThemeSettingInfo info = GOLauncherApp.getSettingControler()
				.getThemeSettingInfo();
		if (info != null) {
			return info.mHighQualityDrawing;
		}
		return false;
	}

	/**
	 * 获取层
	 * 
	 * @param frameId
	 *            层id{@link IDiyFrameIds}
	 * @return
	 */
	public static AbstractFrame getFrame(int frameId) {
		if (sContext != null && sContext.mFrameControl != null) {
			return sContext.mFrameControl.getFrame(frameId);
		}
		return null;
	}

	/**
	 * 获取topframe
	 * 
	 * @return
	 */
	public static AbstractFrame getTopFrame() {
		if (sContext != null && sContext.mFrameControl != null) {
			return sContext.mFrameControl.getTopFrame();
		}
		return null;

	}

	// private void checkFirstRun()
	// {
	// boolean firstRun = false;
	//
	// GoSettingControler controler = GOLauncherApp.getSettingControler();
	// if (null != controler)
	// {
	// ThemeSettingInfo info = controler.getThemeSettingInfo();
	// if (null != info && info.mFirstRun)
	// {
	// info.mFirstRun = false;
	// // 如果是高级配置则开启常驻内存和高质量绘图
	// final int deviceLevel = ConfigurationInfo.getDeviceLevel();
	// if(deviceLevel == ConfigurationInfo.HIGH_DEVICE){
	// info.mIsPemanentMemory = true;
	// }
	// controler.updateThemeSettingInfo(info);
	// firstRun = true;
	// }
	// }
	//
	// mFirstRun = firstRun;
	// }
	/**
	 * 检测是否为第一次运行GO桌面
	 */
	private void checkFirstRun() {
		DataProvider dataProvider = DataProvider.getInstance(this);
		boolean firstRun = dataProvider.getFirstRunValue();
		if (firstRun) {
			ContentValues values = new ContentValues();
			values.put(SettingTable.FIRSTRUN, ConvertUtils.boolean2int(false));
			dataProvider.updateFirstRunValue(values);
			
			//add by licanhui 2012-12-6
			//如果是第一次运行桌面。代表不是新用户。则请求15屏广告数据
			AdvertControl.getAdvertControlInstance(this).setCanRequestAdvertState(true);
			
			//24小时重新请求
			AdvertControl.getAdvertControlInstance(this).setCanRequestAgainState(true);
			//add by licanhui 2012-12-6 - end
		}

		mFirstRun = firstRun;
	}

	public boolean getFirstRun() {
		return mFirstRun;
	}

	private void checkNewVersionFirstRun() {
		boolean newVersion = false;
		//
		// final String preferenceString = getString(R.string.curVersion);
		// final String storeString =
		// DataProvider.getInstance(this).getShowTipFrameCurVersion();
		// if (!preferenceString.equals(storeString))
		// {
		// DataProvider.getInstance(this).saveShowTipFrame(preferenceString);
		//
		// newVersion = true;
		// }

		mLastVersionCode = DataProvider.getInstance(this).getVersionCode();
		PackageManager pm = getPackageManager();
		PackageInfo info;
		try {
			info = pm.getPackageInfo(getPackageName(), 0);
			DataProvider.getInstance(this).saveVersionCode(info.versionCode);
			if (info.versionCode != mLastVersionCode) {
				newVersion = true;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mNewVersionFirstRun = newVersion;
	}

	private void checkSettingInfoFirstRun() {
		// SettingInfo settingInfo =
		// GoSettingControler.getInstance(sContext).getSettingInfo();
		// mOpenAppType = settingInfo.mAppOpenType;
		mOpenAppType = DataProvider.getInstance(this).getAppOpenType();

	}

	public boolean getNewVeriosnFirstRun() {
		return mNewVersionFirstRun;
	}

	private void setWallpaperDimension() {
		WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);

		Display display = getWindowManager().getDefaultDisplay();
		boolean isPortrait = display.getWidth() < display.getHeight();

		final int width = isPortrait ? display.getWidth() : display.getHeight();
		final int height = isPortrait ? display.getHeight() : display
				.getWidth();
		try {
			wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
		} catch (Exception e) {
			Log.i("", "fail to setWallpaperDimension");
		}
	}

	public static int getIconSizeStyle() {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (controler != null) {
			DesktopSettingInfo info = controler.getDesktopSettingInfo();
			if (null != info) {
				return info.getIconSizeStyle();
			}
		}
		return DeskSettingVisualIconTabView.DEFAULT_ICON_SIZE;
	}

	public static boolean isLargeIcon() {
		if (getIconSizeStyle() == DeskSettingVisualIconTabView.LARGE_ICON_SIZE) {
			return true;
		}
		return false;
	}

	public static int getAppFontSize() {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (controler != null) {
			DesktopSettingInfo info = controler.getDesktopSettingInfo();
			if (null != info) {
				return info.getFontSize();
			}
		}
		return 13;
	}

	public static boolean getCustomTitleColor() {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (controler != null) {
			DesktopSettingInfo info = controler.getDesktopSettingInfo();
			if (info != null) {
				return info.mCustomTitleColor;
			}
		}
		return false;
	}

	public static int getAppTitleColor() {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (controler != null) {
			DesktopSettingInfo info = controler.getDesktopSettingInfo();
			if (info != null && info.mCustomTitleColor) {
				return info.mTitleColor;
			}
		}
		return 0;
	}

	public static void setTutorialMask(int mask) {
		sStartTutorialMask = mask;
	}

	private int startTutorialFrame() {
		int frameId = -1;
		switch (sStartTutorialMask) {
		case LauncherEnv.MASK_TUTORIAL_PREVIEW:
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.APPFUNC_FRAME,
					null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_SHOW_PREVIEW, -1, null, null);
			break;
		// case LauncherEnv.MASK_TUTORIAL_SCREEN_EFFECTS:
		// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_SCREEN_EFFECT);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME,
		// null, null);
		// break;
		case LauncherEnv.MASK_TUTORIAL_CREATE_FOLDER:
			GuideForGlFrame
					.setmGuideType(GuideForGlFrame.GUIDE_TYPE_FUNC_FOLDER);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_FRAME,
					null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.APPDRAWER_ENTER_CREATE_FOLDER_TUTORIAL, -1,
					null, null);
			break;
		// case LauncherEnv.MASK_TUTORIAL_APPDRAWER_DRAG :
		// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_FUNC_DRAG);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
		// GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
		// IDiyMsgIds.APPDRAWER_ENTER_DRAG_TUTORIAL, -1, null, null);
		// break;
		case LauncherEnv.MASK_TUTORIAL_APPDRAWER_HIDE:
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_FRAME,
					null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.APPDRAWER_ENTER_HIDE_TUTORIAL, -1, null, null);
			break;
		// case LauncherEnv.MASK_TUTORIAL_SCREEN_MENU :
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
		// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_SCREEN_MENU_OPEN);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		// break;
		// case LauncherEnv.MASK_TUTORIAL_SCREEN_WALLPAPER_SETTING:
		// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_WALLPAPER_SETTING);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		// break;
		// case LauncherEnv.MASK_TUTORIAL_SCREEN_FOLDER :
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
		// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_SCREENFOLDER);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		// break;
		// case LauncherEnv.MASK_TUTORIAL_SCREEN_DOCK :
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
		// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_DOCK_AUTO_FIT);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		// break;
		case LauncherEnv.MASK_TUTORIAL_CUSTOM_GESTURE:
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.ANIMATION_FRAME,
					null, null);
			GuideForGlFrame
					.setmGuideType(GuideForGlFrame.GUIDE_TYPE_CUSTOM_GESTURE);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME,
					null, null);
			break;
		case LauncherEnv.MASK_TUTORIAL_DOCK_BAR_ICON :
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.ANIMATION_FRAME,
					null, null);
			GuideForGlFrame
					.setmGuideType(GuideForGlFrame.GUIDE_TYPE_DOCK_BAR_ICON_GESTURE);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME,
					null, null);
			break;
		default:
			break;
		}
		return frameId;
	}

	public void setNeedReStart() {
		mNeedRStart = true;
	}
	
	public void setNotNeedReStart() {
		mNeedRStart = false;
	}
	
	private void hardwareAcceleratedByWindow() {
		// android 3.0 API Level = 11, android 4.0 API Level = 14
		if (Build.VERSION.SDK_INT < 11 /* || Build.VERSION.SDK_INT >= 14 */) {
			return;
		}
		Class[] arrayOfClass = new Class[] { Integer.TYPE, Integer.TYPE };
		Method localMethod;
		try {
			localMethod = Window.class.getMethod("setFlags", arrayOfClass);
			Object[] arrayOfObject = new Object[2];
			Integer localInteger1 = Integer.valueOf(FLAG_HARDWARE_ACCELERATED);
			arrayOfObject[0] = localInteger1;
			Integer localInteger2 = Integer.valueOf(FLAG_HARDWARE_ACCELERATED);
			arrayOfObject[1] = localInteger2;
			localMethod.invoke(getWindow(), arrayOfObject);
		} catch (Throwable e) {

		}
	}

	private boolean getShowPadDialogPref() {
		PreferencesManager prefs = new PreferencesManager(this,
				IPreferencesIds.PREFERENCES_FOR_PAD_DIALOG, MODE_PRIVATE);
		return prefs.getBoolean(
				IPreferencesIds.PREFERENCES_FOR_PAD_DIALOG_SHOW, true);

	}

	private void saveSharedPreferences(boolean show) {
		PreferencesManager prefs = new PreferencesManager(this,
				IPreferencesIds.PREFERENCES_FOR_PAD_DIALOG, MODE_PRIVATE);
		prefs.putBoolean(IPreferencesIds.PREFERENCES_FOR_PAD_DIALOG_SHOW,
				show);
		prefs.commit();
	}

	private void toastDialog(Context context) {
		final AlertDialog dialog = new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.tosat_for_pad_title)
				.setMessage(R.string.tosat_for_pad_content)
				.setPositiveButton(R.string.tosat_for_pad_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// dismiss();
								saveSharedPreferences(false);
								GoSettingControler controler = GOLauncherApp
										.getSettingControler();
								if (controler != null) {
									DesktopSettingInfo info = controler
											.getDesktopSettingInfo();
									if (null != info) {
										info.mAutofit = true;
										info.mIconSize = Math
												.max(48,
														(int) (48 * DrawUtils.sDensity));
										GOLauncherApp.getSettingControler()
												.updateDesktopSettingInfo(info,
														false);
										GoLauncher.sendMessage(this,
												IDiyFrameIds.SCHEDULE_FRAME,
												IDiyMsgIds.RESTART_GOLAUNCHER,
												-1, null, null);
									}
								}
								/*
								 * GoLauncher.sendMessage(this,
								 * IDiyFrameIds.SCHEDULE_FRAME,
								 * IDiyMsgIds.SCREEN_PREFERENCES, -1, null,
								 * null);
								 */
							}
						})
				.setNegativeButton(R.string.tosat_for_pad_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								saveSharedPreferences(false);
							}
						}).create();
		dialog.show();
	}

	private void guideNotify(Context context) {
		final DeskAlertDialog deskAlertDialog = new DeskAlertDialog(
				GoLauncher.this);
		String title = GoLauncher.this.getString(R.string.one_x_guide_dialogtitle);
		String content = getResources().getString(R.string.one_x_guide_dialogcontent);
		TextView text = new TextView(sContext);
		text.setText(content);
		text.setTextSize(18);
		String url = "http://golauncher.goforandroid.com/2012/10/htc-one-xs-update-guide/";
		if (Locale.getDefault().getLanguage().equals("zh")) {
			url = "http://golauncher.goforandroid.com/zh/2012/10/htc-one-xs-update-guide/";
		}
		deskAlertDialog.setTitle(title);
		deskAlertDialog.setView(text);
		final String goUrl = url;
		deskAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
				"Check", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse(goUrl);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						try {
							startActivity(intent);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
		deskAlertDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						deskAlertDialog.selfDestruct();
					}
				});
		deskAlertDialog.show();
	}

	/**
	 * clear桌面
	 */
	private boolean clDH() {
		try {
			final String packageStr = AppUtils.getDefaultLauncherPackage(this);

			// 没有设置
			// 设置为Go桌面
			// 设置为GO锁屏
			if (null == packageStr
					|| packageStr.equals(LauncherEnv.PACKAGE_NAME)
					|| packageStr.equals(LauncherEnv.GO_LOCK_PACKAGE_NAME)
					|| packageStr.equals("com.eikatou0.appspot.home2shortcut")
					|| packageStr.equals("sg.ruqqq.quickdesk")
					|| packageStr.equals("com.shisho.taskswitcher")
					|| packageStr.equals("com.smart.taskswitcher")
					|| packageStr.equals("org.rabold.android.taskswitcher")
					|| packageStr.equals("com.esdmobile.taskswitcher")
					|| packageStr.equals("com.tkdtnek23.app.multitaskinglite")
					|| packageStr.equals("go.launcher.theme.KissMe")
					|| packageStr.equals("com.gau.go.launcherex.theme.xiaowanzi")
					|| packageStr.equals("com.gau.go.launcherex.theme.valenmm")
					|| packageStr.equals("ccom.gau.go.launcherex.theme.love")
					|| packageStr.equals("com.gau.go.launcherex.theme.KissMe")
					|| packageStr.equals("com.gau.go.launcherex.theme.autumn")
					|| packageStr.equals("com.gau.go.launcherex.theme.kissintnerain")
					|| packageStr.equals("com.gau.go.launcherex.theme.glow")
					|| packageStr.equals("com.gau.go.launcherex.theme.retropatterns")
					|| packageStr.equals("com.gau.go.launcherex.zh")
					|| packageStr.equals(LauncherEnv.GOLAUNKER_PACKAGE_NAME)
					|| packageStr.equals("com.gtp.nextlauncher")
					|| packageStr.equals("tw.kewang.padfonelauncherswitcher")
					|| packageStr.equals("com.nextlauncher.defaultlauncherpatch")) {
				return false;
			}
			PackageManager localPackageManager = this.getPackageManager();

			ComponentName component = new ComponentName(this, ClL.class);
			localPackageManager.setComponentEnabledSetting(component, 1, 1);

			Intent intent = new Intent(ICustomAction.ACTION_MAIN);
			intent.addCategory("android.intent.category.HOME");
			this.startActivity(intent);

			localPackageManager.setComponentEnabledSetting(component, 0, 1);

			Intent localIntent3 = new Intent(this, GoLauncher.class);
			this.startActivity(localIntent3);
			return true;
		} catch (Throwable e) {
			// e.printStackTrace();
		}
		return false;
	}

	/**
	 * 检测不保留后台活动
	 */
	private void checkAFA() {
		try {
			// 4.0以上的手机检测不保留后台活动状态
			if (Build.VERSION.SDK_INT >= 14) {
				// 1为不保留后台活动选项打钩 0为正常 状态
				int alwaysStatus = Settings.System.getInt(
						GoLauncher.this.getContentResolver(),
						Settings.System.ALWAYS_FINISH_ACTIVITIES, 0);
				// 弹出提示设置框
				if (alwaysStatus == 1) {
					showSettingFinishDialog();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 不保留后台活动设置对话框
	 */
	private void showSettingFinishDialog() {
		final DeskAlertDialog deskAlertDialog = new DeskAlertDialog(
				GoLauncher.this);
		String title = GoLauncher.this.getString(R.string.finishactivity_title);
		deskAlertDialog.setTitle(title);
		String content = GoLauncher.this
				.getString(R.string.finishactivity_content);
		String positiveBtnText = GoLauncher.this.getString(R.string.ok);
		String negativeBtnText = GoLauncher.this.getString(R.string.cancle);
		deskAlertDialog.setMessage(content);

		deskAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
				positiveBtnText, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// try {
						// Object obj = new Object();
						// //ActivityManagerService类
						// Class ActivityManagerService = null;
						// //设置不保留后台服务的方法
						// Method setAlwaysFinish = null;
						// Method getActivityManagerProx= null;
						// //获得ActivityManagerService类
						// ActivityManagerService =
						// Class.forName("com.android.server.am.ActivityManagerService");
						// if(ActivityManagerService != null){
						// //得到getActivityManagerProx
						// getActivityManagerProx =
						// ActivityManagerService.getMethod("getDefault");
						// if(getActivityManagerProx != null &&
						// getActivityManagerProx.invoke(obj) != null
						// && getActivityManagerProx.invoke(obj).getClass() !=
						// null){
						// //获得设置不保留后台活动方法setAlwaysFinish
						// setAlwaysFinish =
						// (getActivityManagerProx.invoke(obj).getClass()).getMethod("setAlwaysFinish",
						// boolean.class);
						// if(setAlwaysFinish != null){
						// //不设置后台保留活动
						// setAlwaysFinish.invoke(getActivityManagerProx.invoke(obj),
						// false);
						// }
						// }
						// }
						//
						// } catch (Throwable e) {
						// e.printStackTrace();
						// }
						// 跳转正不保留后台活动设置界面
						Intent intent = new Intent();
						intent.setAction("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS");
						GoLauncher.this.startActivity(intent);
					}
				});
		deskAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				negativeBtnText, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		deskAlertDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						deskAlertDialog.selfDestruct();
					}
				});
		deskAlertDialog.show();
	}

	/***
	 * 判断是否低于4.0
	 * 
	 * @return
	 */
	private boolean isUnderSDK14() {
		//避免ANR return android.os.Build.VERSION.SDK_INT < 14;
		return !Machine.IS_ICS;
	}

	/*
	 * 自适应当前设备
	 */
	private void autoFitDeviceSetting() {
		// 如果不是第一次执行GO桌面则不需要执行以下的适配
		if (mFirstRun) {
			AutoFitDeviceManager.autoFit();
		}
	}

	private void checkRate() {
		PreferencesManager preferencesManager = new PreferencesManager(this,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		boolean needToShowRate = preferencesManager.getBoolean(IPreferencesIds.IS_NEED_SHOW_RATE_DIALOG, false);
		if (needToShowRate) {
			AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			Intent rateAppIntent = new Intent(ICustomAction.ACTION_SHOW_RATE_DIALOG);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, rateAppIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000,
					pendingIntent);
		}
	}

	
	private void autoFitAppFuncSetting() {
		if (mFirstRun) {
			AppFuncAutoFitManager.getInstance(GOLauncherApp.getContext())
					.autoFitDeviceSetting();
		}
	}

	/**
	 * 清除其他桌面
	 */
	// private void killOtherLauncher() {
	// try {
	// ActivityManager activityManager = (ActivityManager) GoLauncher.this
	// .getSystemService(Context.ACTIVITY_SERVICE);
	// List<ResolveInfo> infos = null;
	// PackageManager packageMgr = GoLauncher.this.getPackageManager();
	// Intent intent = new Intent("android.intent.action.MAIN");
	// intent.addCategory("android.intent.category.HOME");
	// infos = packageMgr.queryIntentActivities(intent, 0);
	// boolean sdk_init = Build.VERSION.SDK_INT >= 8;
	// if (infos != null && activityManager != null) {
	// for (ResolveInfo info : infos) {
	// if(info != null){
	// String packageName = info.activityInfo.packageName;
	// if (packageName != null) {
	// if(packageName.startsWith(LauncherEnv.PACKAGE_NAME) ||
	// packageName.startsWith("com.jiubang.goscreenlock")){
	// continue;
	// }
	// }
	// if (sdk_init) {
	// activityManager.killBackgroundProcesses(packageName);
	// } else {
	// activityManager.restartPackage(packageName);
	// }
	// }
	// }
	// }
	// } catch (Throwable t) {
	// t.printStackTrace();
	// }
	// }
	// 检测语言是否匹配
	private void checkLanguage() {
		if (mFirstRun || mNewVersionFirstRun && !(Machine.isONE_X() && !Machine.IS_ICS_MR1)) {
			String language = Locale.getDefault().getLanguage();
			if (language.equals("zh")) {
				language = language + "_" + Locale.getDefault().getCountry();
			}
			String[] languageName = { "tr", "cs", "pt", "el", "pl", "ar" , "it", "fr", "ja", "de", "zh_HK"};
			for (String lang : languageName) {
				if (lang.contains(language)) {
					// 如果末安装则弹出提示框
					if (!AppUtils.isAppExist(GoLauncher.this,
							LauncherEnv.Plugin.LANGUAGE_PACKAGE + "."
									+ language)) {
						showDownLanguageDialog(this, language);
						break;
					}
				}
			}
		}
	}

	/**
	 * 下载语言包
	 */
	private void showDownLanguageDialog(Context context, String language) {
		final Context downcontext = context;
		final String downlanguage = language;
		final DeskAlertDialog deskAlertDialog = new DeskAlertDialog(
				GoLauncher.this);
		String title = GoLauncher.this.getString(R.string.downlanguage_title);
		deskAlertDialog.setTitle(title);
		String content1 = GoLauncher.this
				.getString(R.string.downlanguage_content1);
		String content2 = GoLauncher.this
				.getString(R.string.downlanguage_content2);
		String content3 = GoLauncher.this
				.getString(R.string.downlanguage_content3);
		String content4 = GoLauncher.this
				.getString(R.string.downlanguage_content4);
		String languageName = this.getResources().getConfiguration().locale
				.getDisplayLanguage();

		String content = getLanguageContent(language);
		if (null == content) {
			content = content1 + " " + languageName + " " + content2 + "\n"
					+ content3 + " " + languageName + " " + content4;
		}
		String positiveBtnText = GoLauncher.this
				.getString(R.string.install_language_tip_postive);
		deskAlertDialog.setMessage(content);

		deskAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
				positiveBtnText, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						String packageName = LauncherEnv.Plugin.LANGUAGE_PACKAGE
								+ "." + downlanguage;
//						GoStoreOperatorUtil.gotoStoreDetailDirectly(
//								downcontext, packageName);
						AppsDetail.gotoDetailDirectly(downcontext, 
								AppsDetail.START_TYPE_APPRECOMMENDED, packageName);
					}
				});
		deskAlertDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						deskAlertDialog.selfDestruct();
					}
				});
		deskAlertDialog.show();
	}

	private String getLanguageContent(String language) {
		String content = null;
		if ("ru".equals(language)) {
			content = this.getString(R.string.russian_dialogcontent);
		} else if ("iw".equals(language)) {
			content = this.getString(R.string.hebrew_dialogcontent);
		} else if ("ro".equals(language)) {
			content = this.getString(R.string.romanian_dialogcontent);
		} else if ("es".equals(language)) {
			content = this.getString(R.string.spanish_dialogcontent);
		} else if ("it".equals(language)) {
			content = this.getString(R.string.italiano_dialogcontent);
		} else if ("de".equals(language)) {
			content = this.getString(R.string.german_dialogcontent);
		} else if ("tr".equals(language)) {
			content = this.getString(R.string.turkish_dialogcontent);
		} else if ("pl".equals(language)) {
			content = this.getString(R.string.polish_dialogcontent);
		} else if ("pt".equals(language)) {
			content = this.getString(R.string.portugu_dialogcontent);
		} else if ("fr".equals(language)) {
			content = this.getString(R.string.french_dialogcontent);
		} else if ("ja".equals(language)) {
			content = this.getString(R.string.japanese_dialogcontent);
		} else if ("vi".equals(language)) {
			content = this.getString(R.string.vietnamese_dialogcontent);
		} else if ("in".equals(language)) {
			content = this.getString(R.string.indonesia_dialogcontent);
		} else if ("sv".equals(language)) {
			content = this.getString(R.string.swedish_dialogcontent);
		} else if ("ir".equals(language)) {
			content = this.getString(R.string.persian_dialogcontent);
		} else if ("hr".equals(language)) {
			content = this.getString(R.string.croatian_dialogcontent);
		} else if ("sr".equals(language)) {
			content = this.getString(R.string.serbian_dialogcontent);
		} else if ("zh_HK".equals(language)) {
			content = this.getString(R.string.hongkong_dialogcontent);
		}
		return content;
	}
	
	private String getLanguageNotification(String language) {
		String content = null;
		if ("ru".equals(language)) {
			content = this.getString(R.string.russian_notification);
		} else if ("iw".equals(language)) {
			content = this.getString(R.string.hebrew_notification);
		} else if ("ro".equals(language)) {
			content = this.getString(R.string.romanian_notification);
		} else if ("es".equals(language)) {
			content = this.getString(R.string.spanish_notification);
		} else if ("it".equals(language)) {
			content = this.getString(R.string.italiano_notification);
		} else if ("de".equals(language)) {
			content = this.getString(R.string.german_notification);
		} else if ("tr".equals(language)) {
			content = this.getString(R.string.turkish_notification);
		} else if ("pl".equals(language)) {
			content = this.getString(R.string.polish_notification);
		} else if ("pt".equals(language)) {
			content = this.getString(R.string.portugu_notification);
		} else if ("fr".equals(language)) {
			content = this.getString(R.string.french_notification);
		} else if ("ja".equals(language)) {
			content = this.getString(R.string.japanese_notification);
		} else if ("vi".equals(language)) {
			content = this.getString(R.string.vietnamese_notification);
		} else if ("in".equals(language)) {
			content = this.getString(R.string.indonesia_notification);
		} else if ("sv".equals(language)) {
			content = this.getString(R.string.swedish_notification);
		} else if ("ir".equals(language)) {
			content = this.getString(R.string.persian_notification);
		} else if ("hr".equals(language)) {
			content = this.getString(R.string.croatian_notification);
		} else if ("sr".equals(language)) {
			content = this.getString(R.string.serbian_notification);
		} else if ("zh-HK".equals(language)) {
			content = this.getString(R.string.hongkong_notification);
		}
		return content;
	}
	
	// 检测语言是否匹配
	private void showLanguageSetting() {
		try {
			PreferencesManager preferences = new PreferencesManager(this,
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
					Context.MODE_PRIVATE);
			int goDbVersion = preferences.getInt(IPreferencesIds.GO_DB_VERSION,
					0);
			int currentDbVersion = DatabaseHelper.getDB_CUR_VERSION();
			boolean isShowLanguage = false;
			if (goDbVersion == 0) {
				preferences.putInt(IPreferencesIds.GO_DB_VERSION, currentDbVersion);
				preferences.commit();
				isShowLanguage = true;

			} else if (goDbVersion < currentDbVersion) {
				preferences.putInt(IPreferencesIds.GO_DB_VERSION, currentDbVersion);
				preferences.commit();
				isShowLanguage = true;
			}
			if (isShowLanguage) {
				String language = Locale.getDefault().getLanguage();
				if (language.equals("zh")) {
					language = language + "-" + Locale.getDefault().getCountry();
				}
				String[] supportLanguages = this.getResources().getStringArray(
						R.array.support_language);
				for (String lang : supportLanguages) {
					if (language.contains(lang)) {
						// 如果末安装则弹出提示框
						if (!AppUtils.isAppExist(GoLauncher.this,
								LauncherEnv.Plugin.LANGUAGE_PACKAGE + "."
										+ language)) {
							// 发通知广播
							Intent intent = new Intent(this, GoLauncher.class);
							intent.setAction(ICustomAction.ACTION_LANGUAGE_START_GGMENU);

							String title = this
									.getString(R.string.notification_language_title);
							String noteTitle = this
									.getString(R.string.notification_language_notetitle);
							String noteText = getLanguageNotification(lang);
							if (null == noteText) {
								noteText = this
										.getString(R.string.notification_language_context1)
										+ " "
										+ language
										+ " "
										+ this.getString(R.string.notification_language_context2);
							}
							AppUtils.sendNotification(this, intent,
									R.drawable.icon, title, noteTitle,
									noteText,
									INotificationId.LANGUAGE_START_GGMENU);
							return;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showLanguageGGMenu(Intent intent) {
		if (intent == null) {
			return;
		}
		try {
			String action = intent.getAction();
			if (ICustomAction.ACTION_LANGUAGE_START_GGMENU.equals(action)) {
				//发消息通知GGMENU启动
				mScheduler.handleMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_MENU,
						2, null, null);
			}
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
		}
	} // end showLanguageGGMenu
	
	/**
	 * 获取当前GO桌面版本号
	 */
	public int getVersionCode() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * 是否为应用搜索支持多媒体搜索的版本
	 */
	public boolean isAppFuncSearchSupportMediaEdition() {
		return getVersionCode() >= APPFUNC_SEARCH_SUPPORT_MEDIA_VERSION_NUM;
	}

	/**
	 * 升级前的版本是否为应用搜索不支持多媒体搜索的版本
	 */
	public boolean isUpgradeFromAppFuncSearchUnsupportMediaEdition() {
		return mLastVersionCode < APPFUNC_SEARCH_SUPPORT_MEDIA_VERSION_NUM;
	}
	
	/**
	 * 是否为资源管理支持插件化的版本
	 */
	public boolean isMdiamanagementBecomesPlugin() {
		return getVersionCode() >= MEDIA_MANAGEMENT_BECOMES_PLUGIN;
	}

	/**
	 * 升级前的版本是否为资源管理不支持插件化的版本
	 */
	public boolean isUpgradeFromMdiamanagementUnbecomesPlugin() {
		return mLastVersionCode < MEDIA_MANAGEMENT_BECOMES_PLUGIN;
	}
		
	@Override
	public void startActivityForResult(final Intent intent, final int requestCode) {
		if (ShellPluginFactory.isUseShellPlugin(getContext())) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.START_ACTIVITY, 0,
					new Runnable() {

						@Override
						public void run() {
							GoLauncher.super.startActivityForResult(intent, requestCode);
						}
					}, null);
		} else {
			super.startActivityForResult(intent, requestCode);
		}
	}
}