package com.jiubang.ggheart.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.gau.go.launcherex.R;
import com.gau.utils.net.HttpAdapter;
import com.go.launcher.taskmanager.TaskMgrControler;
import com.go.util.file.FileUtil;
import com.jiubang.core.framework.CleanManager;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageFilter;
import com.jiubang.ggheart.apps.appfunc.controler.RecentAppControler;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager;
import com.jiubang.ggheart.apps.desks.appfunc.appsupdate.AppsListUpdateManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.dock.DockStyleIconManager;
import com.jiubang.ggheart.apps.desks.net.VersionManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.statistics.FunctionalStatistic;
import com.jiubang.ggheart.data.statistics.SearchStatisticSaver;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PreferenceConfiguration;
import com.jiubang.ggheart.plugin.migrate.MigrateControler;
import com.jiubang.ggheart.plugin.notification.NotificationControler;

/**
 * UI交互接口，参与业务逻辑的处理，为UI层提供数据及数据持久化的接口。
 * 
 * @author huyong
 * 
 */
public class AppCore implements ICleanable, IMessageFilter {
	// 单例
	private static/* volatile */AppCore sSelfInstance = null;

	private Context mContext = null;

	// 框架基础服务
	private AppDataEngine mAppDataEngine; // 原始数据，与系统的一致
	private HttpAdapter mHttpAdapter = null;
	private VersionManager mVersionManager = null;
	private GoSettingControler mSettingControler = null; // 主设置

	private RecentAppControler mRecentAppControler; // 最近打开
	private TaskMgrControler mTaskMgrControler; // 进程管理 // 功能表设置

	private SysShortCutControler mSysShortcutControler;
	private SysFolderControler mSysFolderControler;
	private SelfAppItemInfoControler mSelfAppItemControler;
	private DockItemControler mDockItemControler;

	private ThemeManager mThemeManager = null; // 主题模块
	private ImageExplorer mImageExplorer = null;
	private DeskThemeControler mDeskThemeControler;

	private NotificationControler mNotificationControler; // 统计模块
	private MigrateControler mMigrateControler; // 桌面搬家模块

	private DockStyleIconManager mDockStyleIconManager; // dock风格图标安装包管理器

	private GoWidgetManager mGoWidgetManager;
	private CleanManager mCleanManager = null;

	private FunctionalStatistic mFunctionalStatistic; // 收索统计

	private ScheduleTaskHandler mTaskHandler = null;

	//	private DownloadManager mDownloadManager = null; // 下载管理

	private AppsListUpdateManager mAppsListUpdateManager = null; // 程序更新管理

	private ApplicationManager mApplicationManager; // 应用程序管理

	private CommonControler mCommonControler;
	/**
	 * 获取Core实例 在不同的Activity之间共享
	 * 
	 * @param context
	 *            构造所需上下文
	 * @return AppCore的一个实例
	 */
	static public/* synchronized */AppCore getInstance() {
		return sSelfInstance;
	}

	static public void build(Context context) {
		sSelfInstance = new AppCore(context);
		sSelfInstance.construct();
	}

	static public void destroy() {
		if (sSelfInstance != null) {
			sSelfInstance.cleanup();
		}
	}

	private AppCore(Context context) {
		mContext = context;
		mCleanManager = new CleanManager();

		// 创建字体文件夹
		FileUtil.mkDir(LauncherEnv.Path.SDCARD + LauncherEnv.Path.FONT_PATH);
		// 尝试影藏GO桌面小图片
		FileUtil.hideMedia(LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_DIR);
	}

	private void init() {

//		DeskResourcesConfiguration.createInstance(mContext);

		// 数据库模块最先初始化，后面有对数据库是否首次创建有依赖
		// DataModelTmp.getDataModel(mContext);

		DataProvider.getInstance(mContext);

		mHttpAdapter = new HttpAdapter(mContext);
		// mExportDataBase = new ExportDataBase(mContext);

		mThemeManager = ThemeManager.getInstance(mContext);

		mImageExplorer = ImageExplorer.getInstance(mContext);

		mSettingControler = GoSettingControler.getInstance(mContext);

		// mAppDataEngine = new AppDataEngine(mContext);
		mAppDataEngine = AppDataEngine.getInstance(mContext);

		mDeskThemeControler = new DeskThemeControler(mContext);
		mCleanManager.add(mDeskThemeControler);

		mDockStyleIconManager = DockStyleIconManager.getInstance(mContext);

		// GoWidgetManager
		mGoWidgetManager = new GoWidgetManager(mContext);
		mCleanManager.add(mGoWidgetManager);

		// 下载管理
		//		mDownloadManager = DownloadManager.getInstance(mContext);

		mApplicationManager = ApplicationManager.getInstance(mContext);

		
	}

	private void models() {
		// 最近打开
		mRecentAppControler = new RecentAppControler(mContext, mAppDataEngine);
		mCleanManager.add(mRecentAppControler);

		mSysShortcutControler = new SysShortCutControler(mContext);
		mCleanManager.add(mSysShortcutControler);

		mSysFolderControler = new SysFolderControler(mContext);
		mCleanManager.add(mSysFolderControler);

		mSelfAppItemControler = new SelfAppItemInfoControler(mContext);
		mCleanManager.add(mSelfAppItemControler);

		mDockItemControler = new DockItemControler(mContext);
		mCleanManager.add(mDockItemControler);
		

		mVersionManager = new VersionManager(mContext);
		mCommonControler = CommonControler.getInstance(mContext);

		// 监控
		mNotificationControler = new NotificationControler(mContext, mAppDataEngine);
		mMigrateControler = new MigrateControler(mContext);

		PreferenceConfiguration.createInstance(mContext);

		// 判断Bata版本
		String preferenceString = mContext.getResources().getString(R.string.curVersion);
		if (preferenceString.contains("Beta") || preferenceString.contains("beta")
				|| preferenceString.contains("BETA")) {
			mFunctionalStatistic = new FunctionalStatistic();
			SearchStatisticSaver.initStatistic(mContext, mFunctionalStatistic.getContent());
		}
	}

	private void construct() {
		// 基本初始化
		init();
		//
		models();
		// 启动定时任务
		startSchedulTask();
	}

	public Context getContext() {
		return mContext;
	}

	public SysFolderControler getSysFolderControler() {
		return mSysFolderControler;
	}

	public SysShortCutControler getSysShortCutControler() {
		return mSysShortcutControler;
	}

	public SelfAppItemInfoControler getSelfAppItemInfoControler() {
		return mSelfAppItemControler;
	}

	public DockItemControler getDockItemControler() {
		return mDockItemControler;
	}

	public DeskThemeControler getDeskThemeControler() {
		return mDeskThemeControler;
	}

	public NotificationControler getNotificationControler() {
		return mNotificationControler;
	}

	public FunctionalStatistic getFunctionalStatistic() {
		return mFunctionalStatistic;
	}

	// TODO:整理Explorer
	public ImageExplorer getImageExplorer() {
		return mImageExplorer;
	}

	// 获取goWidgetmanager
	public GoWidgetManager getGoWidgetManager() {
		return mGoWidgetManager;
	}

	// 获取下载管理
	//	public DownloadManager getDownloadManager() {
	//		return mDownloadManager;
	//	}

	/**
	 * 获取最近打开
	 * 
	 * @return 最近打开
	 */
	public RecentAppControler getRecentAppControler() {
		return mRecentAppControler;
	}

	/**
	 * 获取进程管理控制器
	 * 
	 * @return
	 */
	public TaskMgrControler getTaskMgrControler() {
		// 进程管理
		if (null == mTaskMgrControler) {
			mTaskMgrControler = new TaskMgrControler(mContext, mAppDataEngine);
		}
		return mTaskMgrControler;
	}

	/**
	 * 释放进程管理控制器
	 */
	public void releaseTaskMgrControler() {
		mTaskMgrControler = null;
	}

	/**
	 * http事务处理
	 * 
	 * @author huyong
	 * @return
	 */
	public final HttpAdapter getHttpAdapter() {
		return mHttpAdapter;
	}

	public final VersionManager getVersionManager() {
		if (mVersionManager == null) {
			mVersionManager = new VersionManager(mContext);
		}
		return mVersionManager;
	}

	public final AppsListUpdateManager getAppsListUpdateManager() {
		if (mAppsListUpdateManager == null) {
			mAppsListUpdateManager = AppsListUpdateManager.getInstance(mContext);
		}
		return mAppsListUpdateManager;
	}

	public final ApplicationManager getApplicationManager() {
		if (mApplicationManager == null) {
			mApplicationManager = ApplicationManager.getInstance(mContext);
		}
		return mApplicationManager;
	}

	private void startSchedulTask() {
		if (mTaskHandler == null) {
			mTaskHandler = new ScheduleTaskHandler(mContext);
		}
		mTaskHandler.startScanAppTask();
		mTaskHandler.startCheckUpdateTask();
		// 开启统计任务
		mTaskHandler.startStatisticsTask();
		// 开启定时弹出评分对话框任务
		mTaskHandler.startRateDialogTask();
	}

	private void cancelSchedulTask() {
		if (mTaskHandler != null) {
			mTaskHandler.cancel();
			mTaskHandler = null;
		}
	}

	/**
	 * 清除后台内存中数据
	 * 
	 * @author huyong
	 */

	@Override
	public void cleanup() {
		// 取消定时任务
		cancelSchedulTask();
		GOLauncherApp.getApplication().setDataChangeListener(null);
		mGoWidgetManager.onDestory();
		mCleanManager.cleanup();

		if (null != mSettingControler) {
			mSettingControler.cleanup();
		}

		if (null != mThemeManager) {
			mThemeManager.cleanup();
		}

		// 清理监控
		if (null != mNotificationControler) {
			mNotificationControler.selfDestruct();
			mNotificationControler = null;
		}

		// 桌面搬家
		if (null != mMigrateControler) {
			mMigrateControler.selfDestruct();
			mMigrateControler = null;
		}

		// 设置配置
		PreferenceConfiguration.destroyInstance();

		DeskResourcesConfiguration.destroyInstance();

		// 功能统计
		if (null != mFunctionalStatistic) {
			SearchStatisticSaver.saveStatistic(mContext, mFunctionalStatistic.getContent());
		}
		// 下载管理
		//		DownloadManager.destroy();
		DataProvider.destroy();

	}

	// @Override
	// public boolean isConsumed(int msgId)
	// {
	// // TODO 在此处对关心的消息返回true
	// boolean ret = false;
	// switch (msgId)
	// {
	// case IDiyMsgIds.EVENT_UNINSTALL_INTENT:
	// case IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK:
	// case IDiyMsgIds.EVENT_REFLUSH_TIME_IS_UP:
	// case IDiyMsgIds.EVENT_UNINSTALL_APP:
	// case IDiyMsgIds.EVENT_UNINSTALL_APPS:
	// case IDiyMsgIds.EVENT_THEME_CHANGED:
	// ret = true;
	// break;
	//
	// default:
	// break;
	// }
	// return ret;
	// }

	@Override
	public int getId() {
		return IDiyFrameIds.APPCORE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		switch (msgId) {
			case IFrameworkMsgId.SYSTEM_ON_DESTROY : {
				mGoWidgetManager.onDestory();
			}
				break;
			case IDiyMsgIds.EVENT_SD_MOUNT :
			case IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK :
			case IDiyMsgIds.EVENT_REFLUSH_TIME_IS_UP : {
				mThemeManager.onBCChange(msgId, param, object, objects);
				// 通知功能表重新检查是否需要冲洗加载主题
				GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.APPDRAWER_INDICATOR_THEME_CHANGE,
						-1, null, null);
				GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.APPDRAWER_TAB_HOME_THEME_CHANGE,
						-1, null, null);

			}
				break;

			case IDiyMsgIds.EVENT_INSTALL_PACKAGE :
			case IDiyMsgIds.EVENT_INSTALL_APP : {
				mThemeManager.onBCChange(msgId, param, object, objects);
				mGoWidgetManager.updateGoWidget((String) object);
			}
				final String packageName = (String) object;
				if (packageName.startsWith(LauncherEnv.Plugin.LANGUAGE_PACKAGE)) {
					String currentLanguage = packageName.substring(
							LauncherEnv.Plugin.LANGUAGE_PACKAGE.length() + 1, packageName.length());
					String language = Locale.getDefault().toString();
					if (language.contains(currentLanguage)) {
						PreferencesManager preferences = new PreferencesManager(mContext,
								IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
						preferences.putString(IPreferencesIds.CURRENTSELETELANGUAGE, currentLanguage);
						preferences.commit();
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
					}
				} else if (packageName.equals(LauncherEnv.Plugin.NOTIFICATION_PACKAGE_NAME)) {
					ShortCutSettingInfo.setAutoMessageStatistic(true);
					ShortCutSettingInfo.setAutoMisscallStatistic(true);
					ShortCutSettingInfo shortCutSettingInfo = GOLauncherApp.getSettingControler()
							.getShortCutSettingInfo();
					GOLauncherApp.getSettingControler().updateShortCutSetting_NonIndepenceTheme(
							shortCutSettingInfo);
				}
				break;

			case IDiyMsgIds.EVENT_UNINSTALL_PACKAGE : {
				mThemeManager.onBCChange(msgId, param, object, objects);
				mNotificationControler.handleLauncherEvent(msgId, param, object, objects);
				mGoWidgetManager.updateGoWidget((String) object);
			}
				break;

			case IDiyMsgIds.EVENT_UPDATE_PACKAGE : {
				mThemeManager.onBCChange(msgId, param, object, objects);
				// widget升级后，重启桌面
				final String pkgName = (String) object;
				final GoWidgetManager widgetManager = getGoWidgetManager();
				if (widgetManager.containsPackage(pkgName)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
				}
				// 如果语言包升级后。重启桌面
				String language = mContext.getResources().getConfiguration().locale.getLanguage();
				String languagePackage = LauncherEnv.Plugin.LANGUAGE_PACKAGE + "." + language;
				if (languagePackage.equals(pkgName)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
				} else if (pkgName.equals(LauncherEnv.Plugin.NOTIFICATION_PACKAGE_NAME)) {
					if (getNotificationControler() != null) {
						getNotificationControler().resetFlags();
						getNotificationControler().checkNotification();
					}
				}
			}
				break;

			case IDiyMsgIds.EVENT_UNINSTALL_APP : {
				mThemeManager.onBCChange(msgId, param, object, objects);
				mRecentAppControler.onBCChange(msgId, param, object, objects);
				mNotificationControler.handleLauncherEvent(msgId, param, object, objects);
				mGoWidgetManager.updateGoWidget((String) object);
			}
				break;

			case IDiyMsgIds.EVENT_UPDATE_EXTERNAL_PACKAGES : {
				mGoWidgetManager.refreshExternalWidget((ArrayList<String>) objects);
			}
				break;

			case IDiyMsgIds.EVENT_THEME_CHANGED : {
				mSettingControler.onBCChange(msgId, param, object, objects);
				mSettingControler.addScreenStyleSetting((String) object);
				mDeskThemeControler.handleLauncherEvent(msgId, param, object, objects);
				mAppDataEngine.onHandleThemeIconStyleChanged();
				// edit by chenguanyu：将widget应用皮肤抽离到case
				// IDiyMsgIds.EVENT_CHANGE_WIDGET_THEME
				// mGoWidgetManager.startApplyWidgetTheme((String)object);
				// end edit

				if (null != PreferenceConfiguration.getInstance()) {
					PreferenceConfiguration.getInstance().onBCChange(msgId, param, object, objects);
				}
				// 自定义应用换肤
				mSelfAppItemControler.handleMessage(who, type, msgId, param, object, objects);
			}
				break;

			case IDiyMsgIds.EVENT_CHANGE_WIDGET_THEME : {
				mGoWidgetManager.startApplyWidgetTheme((String) object);
			}
				break;

			case IDiyMsgIds.EVENT_CHECK_THEME_ICON : {
				mAppDataEngine.onHandleThemeIconStyleChanged();
			}
				break;
			case IDiyMsgIds.REFRESH_SCREENICON_THEME : {
				mAppDataEngine.onHandleScreenThemeIconStyleChanged();
			}
				break;
			case IDiyMsgIds.REFRESH_FOLDER_THEME : {
				mAppDataEngine.onHandleFolderThemeIconStyleChanged();
			}
				break;
			case IDiyMsgIds.REFRESH_SCREENINDICATOR_THEME : {
				mAppDataEngine.onHandleScreenIndicatorStyleChanged();
			}
				break;

			case IDiyMsgIds.EVENT_SHOW_OR_HIDE_ICON_BASE : {
				boolean showBase = param == 1 ? true : false;
				mAppDataEngine.onHandleShowIconBaseChanged(showBase);
			}
				break;

			default :
				break;
		}
		return false;
	}

}
