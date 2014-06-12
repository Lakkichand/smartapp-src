package com.jiubang.ggheart.launcher;

import java.util.Locale;

import org.acra.CrashReport;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;

import com.go.launcher.taskmanager.TaskMgrControler;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.HolographicOutlineHelper;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.appfunc.controler.RecentAppControler;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.config.GOLauncherConfig;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.dock.DockStyleIconManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.billing.AppInBillingManager;
import com.jiubang.ggheart.components.DeskResources;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DataChangeListener;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.SysShortCutControler;
import com.jiubang.ggheart.data.statistics.FunctionalStatistic;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ThemeManager;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 */
public class GOLauncherApp extends Application {
	private static GOLauncherApp sApp = null;
	private static Context sContext = null;
	private DataChangeListener mDataChangeListener = null;
	private static HolographicOutlineHelper sOutlineHelper;

	// 异步线程，用于处理数据库写操作等异步请求
	private static HandlerThread sWorkerThread = new HandlerThread("golauncher-loader");
	static {
		sWorkerThread.start();
	}
	private static Handler sWorker = new Handler(sWorkerThread.getLooper());
		
	@Override
	public void onCreate() {
		super.onCreate();
		initStaticApp(this);
		initStaticContext(getApplicationContext());

		DrawUtils.resetDensity(sContext);
		/*
		 * //定义堆大小 int CWJ_HEAP_SIZE = 9* 1024* 1024 ; // 系统默认 4.xxxM
		 * //增强程序堆内存的处理效率 float TARGET_HEAP_UTILIZATION = 0.7f; // 系统默认是 0.75
		 * 
		 * //强制分配大内存,增强程序堆内存的处理效率
		 * VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);
		 * VMRuntime.getRuntime
		 * ().setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);
		 */

		CrashReport crashReport = new CrashReport();
		crashReport.start(sContext);

		// 创建图标轮廓阴影的工具类
		sOutlineHelper = new HolographicOutlineHelper();
	}

	private static void initStaticApp(GOLauncherApp launcherApp) {
		sApp = launcherApp;
	}

	private static void initStaticContext(Context context) {
		sContext = context;
	}

	/**
	 * 获取创建轮廓阴影工具类
	 * 
	 * @return
	 */
	public static HolographicOutlineHelper getOutlineHelper() {
		return sOutlineHelper;
	}

	public static GOLauncherApp getApplication() {
		return sApp;
	}

	public static Context getContext() {
		return getApplication().getApplicationContext();
	}

	@Override
	public Resources getResources() {
		DeskResourcesConfiguration configuration = DeskResourcesConfiguration.getInstance();
		if (null != configuration) {
			Resources resources = configuration.getDeskResources();
			if (null != resources) {
				return resources;
			}
		}

		return super.getResources();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		try {
			Resources res = getResources();
			if (res instanceof DeskResources) {
				res.updateConfiguration(super.getResources().getConfiguration(), super
						.getResources().getDisplayMetrics());
			}
			//获得设置对象
			Configuration config = res.getConfiguration();
			//获得屏幕参数：主要是分辨率，像素等。
			DisplayMetrics dm = res.getDisplayMetrics();
			PreferencesManager preferences = new PreferencesManager(this,
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
			String currentlanguage = preferences.getString(IPreferencesIds.CURRENTSELETELANGUAGE,
					"");
			if (currentlanguage != null && !currentlanguage.equals("")) {
				if (currentlanguage.length() == 5) {
					String language = currentlanguage.substring(0, 2);
					String country = currentlanguage.substring(3, 5);
					config.locale = new Locale(language, country);
				} else {
					config.locale = new Locale(currentlanguage);
				}
				res.updateConfiguration(config, dm);
			}
		} catch (Exception e) {
			//			e.printStackTrace();
		}
	}

	// GO widget管理
	private GoWidgetManager mGoWidgetManager = null;

	private GoWidgetManager getWidgetManager() {
		if (null == mGoWidgetManager) {
			mGoWidgetManager = new GoWidgetManager(sContext);
		}
		return mGoWidgetManager;
	}

	// 设置
	public static GoSettingControler getSettingControler() {
		try {
			return GoSettingControler.getInstance(sContext);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return null;
	}

	//
	private SysShortCutControler mSysShortcutControler = null;

	private SysShortCutControler getShortCutControler() {
		if (null == mSysShortcutControler) {
			mSysShortcutControler = new SysShortCutControler(sContext);
		}

		return mSysShortcutControler;
	}

	//
	private static AppDataEngine sAppDataEngine = null;

	public static AppDataEngine getAppDataEngine() {
		sAppDataEngine = AppDataEngine.getInstance(sContext);
		return sAppDataEngine;
	}

	public static ThemeManager getThemeManager() {
		return ThemeManager.getInstance(sContext);
	}

	//
	private DeskThemeControler mDeskThemeControler = null;

	private DeskThemeControler getDeskThemeControler() {
		mDeskThemeControler = new DeskThemeControler(sContext);
		return mDeskThemeControler;
	}

	//
	//	private NotificationControler mNotificationControler = null;

	//	private NotificationControler getNotificationControler() {
	//		if (null == mNotificationControler) {
	//			mNotificationControler = new NotificationControler(mContext, getAppDataEngine());
	//		}
	//
	//		return mNotificationControler;
	//	}

	//
	private TaskMgrControler mTaskMgrControler = null;

	private TaskMgrControler getTaskMgrControler() {
		if (null == mTaskMgrControler) {
			mTaskMgrControler = new TaskMgrControler(sContext, getAppDataEngine());
		}

		return mTaskMgrControler;
	}

	//
	private RecentAppControler mRecentAppControler = null;

	private RecentAppControler getRecentAppControler() {
		if (null == mRecentAppControler) {
			mRecentAppControler = new RecentAppControler(sContext, getAppDataEngine());
		}

		return mRecentAppControler;
	}

	//
	private FunctionalStatistic mFunctionalStatistic = null;

	private FunctionalStatistic getFunctionalStatistic() {
		if (null == mFunctionalStatistic) {
			mFunctionalStatistic = new FunctionalStatistic();
		}

		return mFunctionalStatistic;
	}

	public static DockStyleIconManager getDockStyleIconManager() {
		return DockStyleIconManager.getInstance(sContext);
	}

	/**
	 * 设置数据改变监听
	 * 
	 * @param listener
	 *            监听者
	 */
	public void setDataChangeListener(DataChangeListener listener) {
		mDataChangeListener = listener;
	}

	/**
	 * 获取桌面配置文件信息类
	 * 
	 * @return
	 */
	public static GOLauncherConfig getGOLauncherConfig() {
		return GOLauncherConfig.getInstance(sContext);
	}

	/**
	 * 获取与渠道相关的桌面配置文件信息类
	 * 
	 * @return
	 */
	public static ChannelConfig getChannelConfig() {
		return GOLauncherConfig.getInstance(sContext).getChannelConfig();
	}

	
	private IDownloadService mDownloadController = null;
	/**
	 * <br>功能简述:获取下载控制接口
	 * <br>功能详细描述:
	 * <br>注意:获取下载控制接口后，注意是否为null
	 * @return
	 */
	public IDownloadService getDownloadController() {
		return mDownloadController;
	}

	/**
	 * <br>功能简述:设置下载服务控制接口
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setDownloadController(IDownloadService downloadController) {
		mDownloadController = downloadController;
	}

	/**
	 * 退出心脏
	 */
	public void exit(boolean restart) {
		//桌面退出的时候，同时也退出下载服务
		//发送广播到downloadService，通知其保存下载信息
		Intent intent = new Intent();
		intent.setAction(ICustomAction.ACTION_SERVICE_BROADCAST);
		sendBroadcast(intent);
		if (null != mDataChangeListener) {
			final int msgId = restart ? DataType.RESTART_GOLAUNCHER : DataType.EXIT_GOLAUNCHER;
			mDataChangeListener.dataChanged(DataType.DATATYPE_THEMESETTING, msgId, null, null);
		}
		// 停止服务
		// TODO:暂时去掉service.by huyong 2011-02-28
		// stopAppService();

		//清除付费管理类
		destoryAppInBilling();
	}

	/**
	 * <br>功能简述: 当数据加载完毕时调用此方法，初始化一些数据
	 * <br>功能详细描述:
	 * <br>注意: 这个方法是在主线程中调用，如果初始化的操作很耗时，需要在方法内启动线程
	 */
	public void dataLoadFinish() {
		//初始化内付费的管理类
		initAppInBilling();
	}

	/**
	 * <br>功能简述: 初始化应用内付费的管理类
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initAppInBilling() {
		ChannelConfig channelConfig = getChannelConfig();
		if (channelConfig != null) {
			//非200渠道的包，默认是不支持内购的，不需要启动内购的服务
			if (channelConfig.isNeedBillingService()) {
				GoLauncher goLauncher = GoLauncher.getContext();
				if (goLauncher != null) {
					AppInBillingManager.createInstance(goLauncher);
				}
			}
		}
	}

	/**
	 * <br>功能简述: 退出桌面的时候，清空付费的管理类
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void destoryAppInBilling() {
		AppInBillingManager instance = AppInBillingManager.getInstance();
		if (instance != null) {
			instance.destory();
		}
	}

	/**
	 * 提交一个Runable到异步线程上
	 * 
	 * @param r
	 */
	public static void postRunable(Runnable r) {
		sWorker.post(r);
	}
}
