package com.jiubang.ggheart.launcher;

import com.jiubang.ggheart.apps.appfunc.component.AllAppTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncModifyFolderActivity;
import com.jiubang.ggheart.apps.desks.appfunc.HideAppActivity;
import com.jiubang.ggheart.apps.desks.diy.DiyScheduler;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DeskUserFolderFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenModifyFolderActivity;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.DesktopThemeTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.LockerThemeTab;
import com.jiubang.ggheart.apps.desks.settings.AppList;
import com.jiubang.ggheart.apps.desks.settings.AppListMultiple;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.AppService;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.plugin.migrate.MigrateIntoDesk;

/**
 * 桌面用到的线程名称及其说明
 */

public final class ThreadName {
	/**
	 * 初始化后台数据 {@link DiyScheduler#initAppCore}
	 */
	public final static String SCHEDULER_INITIALIZE_APPCORE = "scheduler_initialize_appcore";

	/**
	 * 屏幕加载数据的异步线程 {@link DesktopBinder#BindAsyncHandler}
	 */
	public final static String SCREEN_DESKTOP_BINDER = "screen_desktop_binder";

	/**
	 * 屏幕同步文件夹数据 {@link ScreenFrame#addAppDrawerFolder}
	 * {@link ScreenFrame#addFolderItems}
	 * {@link ScreenFrame#dragFolderFormAllApps}
	 * {@link ScreenFrame#removeFolderItems}
	 */
	public final static String SCREEN_SYNC_FOLDER_DATA = "screen_sync_folder_data";

	/**
	 * 重命名桌面元素 {@link ScreenModifyFolderActivity#handleElments}
	 */
	public final static String RENAME_SCREEN_ITEM = "screen_rename_item";

	/**
	 * 桌面初始多选程序列表 {@link AppListMultiple#initList}
	 */
	public final static String SCREEN_INIT_APPLIST = "screen_init_applist";

	/**
	 * Dock初始单选程序列表 {@link AppList#initList}
	 */
	public final static String DOCK_INIT_APPLIST = "dock_init_applist";

	/**
	 * 功能表_后台_初始化数据 {@link FunControler#startInitThread}
	 */
	public final static String FUNC_INIT_DATA = "func_init_data";

	/**
	 * 功能表_后台_保存名称 {@link FunControler#startSaveTitleThread}
	 */
	public final static String FUNC_SAVE_TITLE = "func_save_title";

	/**
	 * 功能表_后台_排序 {@link AllAppTabBasicContent#beginSortIcon}
	 */
	public final static String FUNC_SORT = "func_sort";

	/**
	 * 功能表后台排序和初始化 {@link FunControler#startSaveThread}
	 */
	public final static String FUNC_INIT_SORT = "func_init_sort";

	/**
	 * 功能表删除文件夹线程 {@link AppFuncModifyFolderActivity#removeFolder}
	 */
	public final static String FUNC_REMOVE_FOLDER = "func_remove_folder";

	/**
	 * 桌面加载详细信息，更新文件夹图标的异步消息线程 {@link AppDataEngine#asynLoadIconsAndTitles}
	 */
	public final static String ASYNC_LOAD_ICONS_AND_TITLES = "async_load_icons_and_titles";

	/**
	 * 初始化隐藏程序列表 {@link HideAppActivity#initList}
	 */
	public final static String INIT_HIDE_APP_LIST = "init_hide_applist";

	/**
	 * 初始化新建文件夹列表 {@link AppFuncModifyFolderActivity#initNewFolderList}
	 * {@link AppFuncNewFolderActivity#initList}
	 */
	public final static String INIT_NEW_FOLDER_APP_LIST = "init_new_folder_applist";

	/**
	 * 初始化修改文件夹列表 {@link AppFuncModifyFolderActivity#initList}
	 * {@link ScreenModifyFolderActivity#start}
	 */
	public final static String INIT_MODIFY_FOLDER_APP_LIST = "init_modify_folder_applist";

	/**
	 * 初始化程序数据列表 {@link AppService#onCreate}
	 */
	public final static String APP_SERVICE_LOAD_DATA = "app_service_load_data";

	/**
	 * GOStore下载组件线程 {@link GoStoreDownloadService#onStartCommand}
	 */
	public final static String GOSTORE_DOWNLOAD_PACKAGE = "gostore_download_package";

	/**
	 * startMigrate,桌面搬家 {@link MigrateIntoDesk#startMigrate}
	 */
	public final static String MIGRATEINTODESK_STARTMIGRARE = "startMigrate";

	/**
	 * 异步解析主题xml配置 {@link ThemeManager#asynParserTheme}
	 */
	public final static String ASYNC_PARSE_THEME = "async_parse_theme";

	/**
	 * 异步扫描当前系统程序中主题包的数量 {@link ThemeManager#scanInitAllInstalledThemes}
	 */
	public final static String SCAN_INSTALLED_THEMES = "scan_installed_themes";

	/**
	 * 异步扫描系统程序 {@link AppDataEngine#asynReScanSysApp}
	 */
	public final static String ASYNC_RESCAN_SYSAPP = "async_rescan_sysapp";

	/**
	 * 异步更新程序图标和标题 {@link AppDataEngine#asynReScanSysApp}
	 */
	public final static String ASYNC_UPDATE_APPS = "async_update_apps";

	/**
	 * GoWidgetManager用于通知widget进入离开屏幕的线程
	 * {@link GoWidgetManager#WidgetAsyncHandler}
	 */
	public final static String GOWIDGET_ASYNC_HANDLER = "gowidget_async_handler";

	/**
	 * 屏幕加载数据的异步线程 {@link DockBinder#BindAsyncHandler}
	 */
	public final static String DOCK_BINDER = "dock_binder";
	/**
	 * killOther线程 {@link AppDataEngine#startLoadCompletedData}
	 */
	public final static String KILL_OTHERLAUNCHER = "kill_otherlauncher";

	/**
	 * 功能表添加文件到桌面，更新文件夹元素到数据库线程 {@link AppDataEngine#startLoadCompletedData}
	 */
	public final static String ADD_USER_FOLDER_CONTENT = "add_user_folder_content";

	/**
	 * 桌面编辑，锁屏数据加载 {@link LockerThemeTab#initListByLoading}
	 * 
	 */
	public final static String SCREEN_EDIT_LOCKERTAB = "screen_init_lockertab";

	/**
	 * 桌面编辑，主题数据加载 {@link DesktopThemeTab#initListByLoading}
	 */
	public final static String SCREEN_EDIT_THEMETAB = "screen_init_themetab";

	/**
	 * 屏幕文件夹与文件夹合并时的数据和UI更新 {@link ScreenFrame#isMergeFolder}
	 */
	public final static String SCREEN_FOLDER_MERGING = "screen_folder_merging";

	/**
	 * 文件夹排序 {@link DeskUserFolderFrame#beginSortIcon}
	 */
	public final static String FOLDER_SORT_ICON = "folder_sort_icon";

	/**
	 *  初始化通讯统计程序列表
	 *  {@link NotificationModifyAtivity#initList}
	 */
	public final static String INIT_NOTIFICATION_APP_LIST = "init_notification_applist";
	
	/**
	 *  启动google统计
	 */
	public final static String START_GOOGLE_ANALYTICS = "start_google_analytics";
	
	/**
	 *  更新桌面壁纸
	 *  {@link WallpaperControler#updateWallpaperInBackground(boolean)}
	 */
	public final static String SCREEN_UPDATE_WALLPAPER = "screen_update_wallpaper";
}
