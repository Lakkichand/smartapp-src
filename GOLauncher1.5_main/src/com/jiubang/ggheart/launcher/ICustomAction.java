package com.jiubang.ggheart.launcher;

/**
 * 广播类型
 */
public interface ICustomAction {

	/***************************************** BEGIN Manifest中使用的Action **************************************************************/

	/**QaTutorialActivity中使用的Action，未找到使用**/
	public static final String ACTION_START_HELP = "com.jiubang.ggheart.apps.desks.START_HELP";

	/**ImagePreviewActivity中使用的Action， 使用的地方被注释掉**/
	public static final String ACTION_IMAGE_PREVIEW = "com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewEx";

	/** ThemeImagePreviewActivity中使用的Action 未找到使用**/
	public static final String ACTION_THEME_IMAGE_PREVIEW = "com.jiubang.ggheart.apps.desks.imagepreview.ThemeImagePreviewActivity";

	// 以下都是gostore中使用的Action
	/** GoStore */
	public static final String ACTION_GOSTORE = "com.gau.go.launcherex.gostore";
	/**  GoStore详情   ItemDetailActivity中使用的Action 未找到使用*/
	public static final String ACTION_GOSTORE_DETAIL = "com.jiubang.ggheart.apps.gowidget.gostore.ITEM_DETAIL";
	
	// 退出GO精品
	public static final String ACTION_GOSTORE_DESTORY = "com.jiubang.ggheart.apps.gowidget.gostore.GOSTORE_DESTORY";

	//用于外部获取GO桌面IMEI号的广播接收器使用的Action，目前是与木瓜移动合作交换数据所需
	public static final String ACTION_GET_VIRTUAL_ID = "com.papayamobile.ACTION_GET_VIRTUAL_ID";
	public static final String ACTION_RETURN_VIRTUAL_ID = "com.papayamobile.ACTION_RETURN_VIRTUAL_ID";

	/** 通知栏动作监听，用于打开桌面菜单*/
	public static final String ACTION_OPEN_GGMENU = "com.gau.go.launcherex.action.open_GGmenu";

	/** ThemeManageActivity中使用的Action 未找到使用**/
	public static final String ACTION_MYTHEMES = "com.gau.go.launcherex.MyThemes";

	// MyThemeReceiver中使用的Action
	public static final String ACTION_THEME_BROADCAST = "com.gau.go.launcherex.MyThemes.mythemeaction";
	//添加界面主题设置action 防止广播接收延迟问题
	public static final String ACTION_THEME_BROADCAST_SCREENEDIT = "com.gau.go.launcherex.MyThemes.mythemeaction_screenedit"; 
	public static final String ACTION_START_MY_THEMES = "com.gau.go.launcherex.action.start_my_themes"; // 未找到使用

	/**ReferrerInfoReceiver中使用的Action **/
	public static final String ACTION_INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";

	/** com.jiubang.ggheart.apps.desks.settings.BackUpSettingReceiver中的Action  未找到使用**/
	public static final String ACTION_BACKRECEIVER = "com.jiubang.ggheart.apps.desks.settings.BackReceiver";

	/**AppsManagementActivity 中使用的Action**/
	public static final String ACTION_APP_MANAGEMENT_CN = "com.gau.go.launcherex.appmanagement.cn";
	/** 点击notification的更新信息，跳转到应用中心的action */
	public static final String ACTION_APP_MANAGEMENT = "com.gau.go.launcherex.appmanagement";

	/** DownloadService中使用的Action */
	public static final String ACTION_DOWNLOAD_SERVICE = "com.jiubang.ggheart.appgame.download.DownloadService";

	// AppInBillingRequestReceiver中使用的Action
	public static final String ACTION_THEME_PARCHASE = "go.launcherex.purchase.state.REQUEST";
	public static final String ACTION_THEME_STOP_SERVICE = "go.launcherex.purchase.stop.service"; // 使用处被注释掉，应该无用

	/** go手册Action 马上使用 */
	public static final String ACTION_GO_HANDBOOK_USE_NOW = "com.gau.go.launcherex.action.goHandBook.useNow";

	/***************************************** END Manifest中使用的Action **************************************************************/

	/********************************************** BEGIN 桌面相关 ***********************************************************************************/

	// 以下是DIY桌面自定义Intent的Action TODO 此处所有Action都可对外部提供，并设置发送权限，以达到支持第三方开发的功能
	public final static String CUSTOM_ACTION = "custom_action";
	/**用于GO桌面 */
	public final static String ACTION_GOLAUNCHER = "com.gau.go.launcherex.MAIN";

	/** 无动作 */
	public final static String ACTION_NONE = "com.jiubang.intent.action.NONE";

	/** 空白显示 */
	public final static String ACTION_BLANK = "com.jiubang.intent.action.BLANK";

	/** 显示功能表*/
	public final static String ACTION_SHOW_FUNCMENU = "com.jiubang.intent.action.SHOW_FUNCMENU";

	/** 屏幕翻转 */
	public final static String ACTION_TURN_SCREEN = "com.jiubang.intent.action.TURN_SCREEN";

	/** 显示主屏 */
	public final static String ACTION_SHOW_MAIN_SCREEN = "com.jiubang.intent.action.SHOW_MAIN_SCREEN";

	/**显示主屏或者预览*/
	public final static String ACTION_SHOW_MAIN_OR_PREVIEW = "com.jiubang.intent.action.SHOW_MAIN_OR_PREVIEW";

	/**显示预览*/
	public final static String ACTION_SHOW_PREVIEW = "com.jiubang.intent.action.SHOW_PREVIEW";

	/** 显示预览*/
	public final static String ACTION_SHOW_MENU = "com.jiubang.intent.action.SHOW_MENU";

	/**显示自定义手势*/
	public final static String ACTION_SHOW_DIYGESTURE = "com.jiubang.intent.action.SHOW_DIYGESTURE";

	/**显示通知系统*/
	public final static String ACTION_SHOW_EXPEND_BAR = "com.jiubang.intent.action.SHOW_EXPEND_BAR";

	/**显示/隐藏状态栏*/
	public final static String ACTION_SHOW_HIDE_STATUSBAR = "com.jiubang.intent.action.SHOW_HIDE_STATUSBAR";

	/**显示主题预览*/
	public final static String ACTION_SHOW_THEME_PREVIEW = "com.jiubang.intent.action.SHOW_THEME_PREVIEW";

	/**显示锁屏主题预览*/
	public final static String ACTION_SHOW_LOCKER_THEME_PREVIEW = "com.jiubang.intent.action.SHOW_LOCKER_THEME_PREVIEW";

	/**检查更新*/
	public final static String ACTION_AUTO_CHECK_UPDATE = "com.jiubang.intent.action.AUTO_CHECK_UPDATE";

	/**统计数据*/
	public final static String ACTION_TIDY_DATA = "com.jiubang.intent.action.ACTION_TIDY_DATA";

	/**弹出评分对话框*/
	public final static String ACTION_SHOW_RATE_DIALOG = "com.jiubang.intent.action.ACTION_SHOW_RATE_DIALOG";

	/**扫描应用程序*/
	public final static String ACTION_SCAN_APPS = "com.jiubang.intent.action.SCAN_APPS";

	/**功能表go主题假图标*/
	public static final String ACTION_FUNC_SPECIAL_APP_GOTHEME = "com.jiubang.intent.action.FUNC_SPECIAL_APP_GOTHEME";

	/**功能表go精品假图标*/
	public static final String ACTION_FUNC_SPECIAL_APP_GOSTORE = "com.jiubang.intent.action.FUNC_SPECIAL_APP_GOSTORE";

	/**功能表gowidget假图标*/
	public static final String ACTION_FUNC_SPECIAL_APP_GOWIDGET = "com.jiubang.intent.action.FUNC_SPECIAL_APP_GOWIDGET";

	/**GO桌面启动后通知周边插件*/
	public final static String ACTION_LAUNCHER_START = "com.jiubang.intent.action.LAUNCHER_START";

	/**GO桌面退出前通知周边插件*/
	public final static String ACTION_LAUNCHER_EXIT = "com.jiubang.intent.action.LAUNCHER_EXIT";

	/**桌面设置*/
	public final static String ACTION_SHOW_PREFERENCES = "com.jiubang.intent.action.SHOW_PREFERENCES";

	/**显示功能表*/
	public final static String ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON = "com.jiubang.intent.action.SHOW_APPDRAWER";

	/**GO锁屏设置*/
	public final static String ACTION_SHOW_LOCKER_SETTING = "com.jiubang.intent.action.SHOW_LOCKER_SETTING";

	/**启动锁屏*/
	public final static String ACTION_ENABLE_SCREEN_GUARD = "com.jiubang.intent.action.ENABLE_SCREEN_GUARD";

	/**隐藏DOCK*/
	public final static String ACTION_SHOW_DOCK = "com.jiubang.intent.action.SHOW_HIDE_DOCK";

	/**更新消息*/
	public final static String ACTION_AUTO_UPDATE_MSG = "com.jiubang.intent.action.UPDATE_MESSAGE";

	/**go锁屏主题预览*/
	public final static String ACTION_SHOW_GOLOCKER_THEME = "com.jiubang.intent.action.GOLOCKER_THEME";

	/**GO桌面恢复默认时，通知周边插件*/
	public final static String ACTION_LAUNCHER_RESETDEFAULT = "com.jiubang.intent.action.RESET_TO_DEFAULT_DATA";

	/**推荐下载*/
	public final static String ACTION_RECOMMEND_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_DEFENDER2";

	/**gostore中主题页面*/
	public static final String ACTION_FUNC_SPECIAL_APP_GOSTORE_THEME = "com.jiubang.intent.action.FUNC_SPECIAL_APP_GOSTORE_THEME";

	/**推荐APP（一键装机）,桌面图标*/
	public static final String ACTION_SHOW_RECOMMENDLIST = "com.jiubang.intent.action.SHOW_RECOMMENDLIST";

	/**推荐APP（一键装机）,功能表图标*/
	public static final String ACTION_FUNC_SHOW_RECOMMENDLIST = "com.jiubang.intent.action.FUNC_SHOW_RECOMMENDLIST";

	/**应用中心,桌面图标*/
	public static final String ACTION_SHOW_RECOMMENDCENTER = "com.jiubang.intent.action.SHOW_RECOMENDCENTER";

	/**应用中心,功能表图标*/
	public static final String ACTION_FUNC_SHOW_RECOMMENDCENTER = "com.jiubang.intent.aciton_FUNC_RECOMMENDCENTER";

	/**游戏中心,桌面图标*/
	public static final String ACTION_SHOW_GAMECENTER = "com.jiubang.intent.action.SHOW_GAMECENTER";

	/**游戏中心,功能表图标*/
	public static final String ACTION_FUNC_SHOW_GAMECENTER = "com.jiubang.intent.aciton_FUNC_GAMECENTER";

	/**推荐GAME（一键玩机）,桌面图标*/
	public static final String ACTION_SHOW_RECOMMENDGAME = "com.jiubang.intent.action.SHOW_RECOMMENDGAME";

	/**推荐GAME（一键玩机）,功能表图标*/
	public static final String ACTION_FUNC_SHOW_RECOMMENDGAME = "com.jiubang.intent.action.FUNC_SHOW_RECOMMENDGAME";
	public static final String ACTION_DOWNLOAD_ANZHIMARKET = "com.jiubang.intent.action.DOWNLOAD_ANZHIMARKET";

	/**推荐EverNote,桌面图标*/
	public static final String ACTION_SHOW_EVERNOTE = "com.jiubang.intent.action.SHOW_EVERNOTE";

	/**GO短信推荐下载图标*/
	public final static String ACTION_RECOMMAND_GOSMS_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_GOSMS";

	/**GO省电推荐下载图标*/
	public final static String ACTION_RECOMMAND_GOPOWERMASTER_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_GOPOWERMASTER";

	/**GO任务管理器推荐下载图标*/
	public final static String ACTION_RECOMMAND_GOTASKMANAGER_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_GOTASKMASTER";

	/**GO输入法推荐下载图标*/
	public final static String ACTION_RECOMMAND_GOKEYBOARD_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_GOKEYBOARD";

	/**GO锁屏推荐下载图标*/
	public final static String ACTION_RECOMMAND_GOLOCKER_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_GOLOCKER";

	/**GO备份推荐下载图标*/
	public final static String ACTION_RECOMMAND_GOBACKUP_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_GOBACKUP";

	/**一键锁屏推荐下载图标*/
	public final static String ACTION_RECOMMAND_LOCKSCREEN_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_LOCKSCREEN";
	
	/**GO天气推荐下载图标*/
	public final static String ACTION_RECOMMAND_GOWEATHEREX_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_GOWEATHEREX";
	
	/**NextLauncher推荐下载图标*/
	public final static String ACTION_RECOMMAND_NEXTLAUNCHER_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_NEXTLAUNCHER";
	
	/**资源管理插件包*/
	public final static String ACTION_RECOMMAND_MEDIA_PLUGIN_DOWNLOAD = "com.jiubang.intent.action.DOWNLOAD_MEDIA_PLUGIN";

	/**GO桌面停止后通知周边插件*/
	public final static String ACTION_LAUNCHER_STOP = "com.jiubang.intent.action.LAUNCHER_STOP";

	/**GO桌面执行onStart后通知周边插件*/
	public final static String ACTION_LAUNCHER_ONSTART = "com.jiubang.intent.action.LAUNCHER_ONSTART";

	/**zip主题包卸载*/
	public final static String ACTION_ZIP_THEME_REMOVED = "android.intent.action.ZIP_THEME_REMOVED";

	/**接收语言包下载通知后启动GGMENU*/
	public final static String ACTION_LANGUAGE_START_GGMENU = "com.jiubang.intent.action.LANGUAGE_START_GGMENU";

	/**GO手册*/
	public final static String ACTION_SHOW_GO_HANDBOOK = "com.jiubang.intent.action.go_handbook";

	/**风格包共同的Intent-filter*/
	public static final String ACTION_PKG_NAME = "com.gau.go.launcherex.dock";

	/**设置widget大小 ScreenFrame*/
	public static final String ACTION_SET_WIDGET_SIZE = "com.motorola.blur.home.ACTION_SET_WIDGET_SIZE";

	/**DesktopThemeTab中使用的Action*/
	public static final String ACTION_SEND_TO_GOLOCK = "com.gau.go.launcherex_action_send_to_golock";

	// InstallShortcutReceiver
	public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

	// LockScreenActivity
	public static final String ACTION_LAUNCHER_LOCK = "com.jiubang.intent.action.LAUNCHER_LOCK";
	
	/**发送当前屏幕数给多屏多壁纸应用的Action 当前屏幕数*/
	public static final String ACTION_CURRENT_WALLPAPER_NUMBER = "com.jiubang.intent.action.ACTION_CURRENT_WALLPAPER_NUMBER";
	
	/**发送当前屏幕数给多屏多壁纸应用的Action 当前屏幕数和当前桌面下标*/
	public static final String ACTION_CURRENT_WALLPAPER_NUMBER_AND_CURRENT_SCREEN_INDEX = "com.jiubang.intent.action.ACTION_CURRENT_WALLPAPER_NUMBER_AND_CURRENT_SCREEN_INDEX";
	
	/**发送当前屏幕数给多屏多壁纸应用的Action 当前桌面下标*/
	public static final String ACTION_CURRENT_SCREEN_INDEX = "com.jiubang.intent.action.ACTION_CURRENT_SCREEN_INDEX";

	/************************************************ END 桌面相关 ***********************************************************************************/

	/********************************************** BEGIN 功能表相关 *********************************************************************************/

	/**FunAppSetting功能表设置数据 中使用*/
	public static final String ACTION_RESPOND_IS_SHOWLOCK = "com.gau.go.launcherex.gowidget.taskmanager.action.respondIsShowLock";

	/**显示功能表图片*/
	public final static String ACTION_SHOW_PHOTO = "com.jiubang.intent.action.SHOW_photo";
	/**显示功能表音乐*/
	public final static String ACTION_SHOW_MUSIC = "com.jiubang.intent.action.SHOW_music";
	/**显示功能表视频*/
	public final static String ACTION_SHOW_VIDEO = "com.jiubang.intent.action.SHOW_video";

	/************************************************ END 功能表相关 *********************************************************************************/

	/********************************************** BEGIN 主题相关 ***********************************************************************************/

	/**隐藏主题图标*/
	public static final String ACTION_HIDE_THEME_ICON = "com.gau.go.launcherex.action.hide_theme_icon";

	/**主题更改Action*/
	public static final String ACTION_THEME_CHANGED = "com.jiubang.ggheart.launcher.themechanged";

	/**桌面主题包名*/
	public static final String ACTION_MAIN_THEME_PACKAGE = "com.gau.go.launcherex.theme";
	/**GO锁屏主题包名*/
	public static final String ACTION_GOLOCK_THEME = "com.jiubang.goscreenlock.theme";
	public static final String ACTION_SEND_TO_GOLOCK_FOR_GOLOCKER = "com.gau.go.launcherex_action_send_to_golock";

	/**Go锁屏Action*/
	public static final String ACTION_LOCKER_DETAIL = "com.jiubang.goscreenlock.themeDetail";

	/** ThemeDetailView相关*/
	public static String ACTION_SEND_TO_GOLOCK_FOR_THEME_DETAIL = "com.gau.go.launcherex_action_send_to_golock";

	// 主题管理相关
	public static final String ACTION_FEATURED_THEME_CHANGED = "android.intent.action.FEATURED_THEME_CHANGED";
	public static final String ACTION_NEW_THEME_INSTALLED = "android.intent.action.NEW_THEME_INSTALLED";
	public static final String ACTION_SPEC_THEME_CHANGED = "android.intent.action.SPEC_THEME_CHANGED";
	public static final String ACTION_SPEC_THEME_TITLE = "android.intent.action.SPEC_THEME_TITLE";
	public static final String ACTION_BANNER_DATA_CHANGEED = "android.intent.action.BANNER_DATA_CHANGEED";
	public static final String ACTION_CHANGE_FULLSCREEN_IMAGE = "android.intent.action.CHANGE_FULLSCREEN_IMAGE";

	/************************************************ END 主题相关 ***********************************************************************************/

	/********************************************** BEGIN 后台相关 ***********************************************************************************/

	// 音乐播放后台服务相关Action
	public static final String ACTION_MUSIC_SERVICECMD = "com.android.music.musicservicecommand";
	/**返回到go音乐播放器界面*/
	public static final String DESTINATION_RETURN_TO_GOMUSIC = "destination_to_gomusic_action";
	/** 参数key */
	public static final String DESTINATION_KEY = "destination_key";
	/** 播放文件改变*/
	public static final String ACTION_MEDIA_PLAY_CHANGED = "media_play_changed";
	/**播放服务关闭*/
	public static final String ACTION_MEDIA_PLAY_CLOSED = "media_play_closed";
	/**耳机线控**/
	public static final String ACTION_MEDIA_BUTTON_EVENT = "media_button_event";
	public static final String ACTION_STATUS_BAR_COVER_CLICK = "status_bar_return";
	public static final String ACTION_STATUS_BAR_PREV_BUTTON_CLICK = "status_bar_prev";
	public static final String ACTION_STATUS_BAR_PAUSE_BUTTON_CLICK = "status_bar_pause";
	public static final String ACTION_STATUS_BAR_NEXT_BUTTON_CLICK = "status_bar_next";
	public static final String ACTION_STATUS_BAR_COLSE_BUTTON_CLICK = "status_bar_colse";
	/**音乐默认播放设置界面移除*/
	public final static String ACTION_MEDIA_OPEN_SETTING_ACTIVITY_ON_STOP = "media_open_setting_activity_on_stop";

	// 下载相关
	/**AppDownloadListener所使用的Action 注册下载状态的事件中使用*/
	public static final String ACTION_APP_DOWNLOAD = "Action_Download";

	/**ItemDetailActivity所使用的Action，更新进度条的action*/
	public final static String ACTION_UPDATE_DOWNLOAD_PERCENT_FOR_ITEM_DETAIL = "com.jiubang.ggheart.apps.gowidget.gostore.ItemDetailActivity_UPDATE_DOWNLOAD_PERCENT";

	/**完成下载*/
	public static final String ACTION_DOWNLOAD_COMPLETE = "com.jiubang.ggheart.apps.download.DownloadBrocastReceiver.GOSTORE_DOWNLOAD_COMPLETE";
	/***更新进度条的action*/
	public static final String ACTION_UPDATE_DOWNLOAD_PERCENT = "com.jiubang.ggheart.apps.gowidget.gostore.ItemDetailActivity_UPDATE_DOWNLOAD_PERCENT";
	/**下载终止*/
	public static final String ACTION_DOWNLOAD_DESTROY = "com.jiubang.ggheart.apps.download.DownloadBrocastReceiver.GOSTORE_DOWNLOAD_DESTROY";
	/**下载停止*/
	public static final String ACTION_UPDATE_DOWNLOAD_STOP = "com.jiubang.ggheart.apps.gowidget.gostore.ItemDetailActivity_UPDATE_DOWNLOAD_STOP";
	/**下载失败*/
	public static final String ACTION_UPDATE_DOWNLOAD_FAILED = "com.jiubang.ggheart.apps.gowidget.gostore.ItemDetailActivity_UPDATE_DOWNLOAD_FAILED";
	/**下载停止*/
	public static final String ACTION_DOWNLOAD_STOP = "com.jiubang.ggheart.apps.download.DownloadBrocastReceiver.GOSTORE_DOWNLOAD_STOP";

	/**downloadservice广播*/
	public static final String ACTION_SERVICE_BROADCAST = "com.jiubang.downloadservice.stop";

	/** 增加DB读权限*/
	public final static String ACTION_ADD_DB_READ_PERMISSION = "com.gau.go.launcherex.action.ADD_DB_READ_PERMISSION";
	/** 去掉DB读权限*/
	public final static String ACTION_REMOVE_DB_READ_PERMISSION = "com.gau.go.launcherex.action.REMOVE_DB_READ_PERMISSION";
	/** 桌面DB读权限添加完毕*/
	public final static String ACTION_REMOTE_DB_READ_PERMISSION_OK = "com.gau.go.launcherex.action.REMOTE_DB_READ_PERMISSION";
	/** 桌面DB读权限添加失败*/
	public final static String ACTION_REMOTE_DB_READ_PERMISSION_FAILED = "com.gau.go.launcherex.action.REMOTE_DB_READ_PERMISSION_FAILED";

	/************************************************ END 后台相关 ***********************************************************************************/

	/********************************************** BEGIN 精品、应用中心、游戏中心 ********************************************************************/

	/**渠道验证成功，发送广播的Action 原本定义于 {@link #GoStoreChannelControl}*/
	public static final String ACTION_CHANNEL_CHECK_NAME_SUCCESS = "com.jiubang.ggheart.apps.gowidget.gostore.views.GoStoreChannelControl.ACTION_CHANNEL_CHECK_NAME_SUCCESS";

	/**gostore no network Action 原本定义于 {@link #MainViewController}*/
	public static final String ACTION_NO_NETWORK = "com.jiubang.ggheart.apps.gowidget.gostore.mainview.NO_NETWORK";

	/**窗体finish action 原本定义于 {@link #GoStorePublicDefine}*/
	public static final String ACTION_ACTIVITY_FINISH = "com.jiubang.ggheart.apps.gowidget.gostore.ACTIVITY_FINISH";

	/**GoStore中加载widget数据 原本定义于 {@link #GoStoreWidgetDataManager}*/
	public static final String ACTION_LOAD_WIDGET_DATA_FOR_GOSTORE = "com.jiubang.intent.action.ACTION_LOAD_WIDGET_DATA_GO_STORE";
	
	/**GoStore页面关闭时通知widget更新的广播*/
	public static final String ACTION_LOAD_WIDGET_DATA_FOR_GOSTORE_CLOSED = "com.jiubang.intent.action.ACTION_LOAD_WIDGET_DATA_GO_STORE_CLOSED";
	
	/**ImageScanActivity中使用的计时器*/
	public static final String ACTION_NAVIGATION_TIME_FOR_IMAGE_SCAN = "navigation_time";

	/**游戏中心导航 计时器 AppDetailImageActivity*/
	public static final String ACTION_NAVIGATION_TIME = "navigation_time";

	/** 游戏中心相关 加载widget数据 AppGameWidgetDataManager*/
	public static final String ACTION_LOAD_WIDGET_DATA = "com.jiubang.intent.action.ACTION_LOAD_WIDGET_DATA";

	/**应用下载*/
	public static final String ACTION_APP_DOWNLOAD_FOR_APPS = "appsmanagement_download_info";

	/************************************************ END 精品、应用中心、游戏中心 ********************************************************************/

	/********************************************** BEGIN WIDGET相关 *********************************************************************************/

	// taskmanager包中相关的使用
	/** 接收广播 动作 */
	public final static String ACTION_RESPONDISSHOWLOCK = "com.gau.go.launcherex.gowidget.taskmanager.action.respondIsShowLock";
	/** 接收更新广播 动作 */
	public final static String ACTION_RESPOND_UPDATE_FROMAPP = "com.gau.go.launcherex.gowidget.taskmanager.action.respond_update_fromapp";
	/** 请求APP更新 */
	public static String ACTION_REQUEST_UPDATE_TOAPP = "com.gau.go.launcherex.gowidget.taskmanager.action.request_update_toapp";
	/** 请求广播动作 */
	public final static String ACTION_REQUESTISSHOWLOCK = "com.gau.go.launcherex.gowidget.taskmanager.action.requestIsShowLock";

	/**Widget主题包 名*/
	public static final String ACTION_WIDGET_THEME_PACKAGE = "com.gau.go.launcherex.theme.gowidget";

	/**GoWidget包名*/
	public static final String ACTION_MAIN_GOWIDGET_PACKAGE = "com.gau.go.launcherex.gowidget";

	public static final String GOWIDGET = "com.gau.gowidget";
	// for intent
	public static final String ACTION_ADD = GOWIDGET + "_action_add";
	public static final String ACTION_DELETE = GOWIDGET + "_action_delete";
	public static final String ACTION_UPDATE = GOWIDGET + "_action_update";

	/**通过反射调用GoWidgetView的 OnStart(Bundle)*/
	public static final String ACTION_START = GOWIDGET + "_action_start";
	/**通过反射调用GoWidgetView的 OnStop()*/
	public static final String ACTION_STOP = GOWIDGET + "_action_stop";

	public static final String ACTION_ITEM_CLICK = GOWIDGET + "_action_item_click";
	public static final String ACTION_VIEW_CLICK = GOWIDGET + "_action_view_click";

	/**完成gowidget设置*/
	public static final String ACTION_CONFIG_FINISH = GOWIDGET + "_action_config_finish";

	/**桌面被销毁后通知GOWidget*/
	public static final String ACTION_DESTROY_GOWIDGETS = GOWIDGET + "_action_destroy_gowidgets";

	/**直接跳转进入指定gowidget详情界面*/
	public static final String DATA_PKG_GOTO_SPECIFICK_WIDGET_DETAIL = GOWIDGET
			+ "_data_pkg_goto_specific_widget_detail";

	/**桌面恢复默认*/
	public static final String ACTION_RESET_TO_DEFAULT = GOWIDGET + "_action_reset_to_default";

	/**请求获取焦点，桌面响应请求会跳转到GOwidget所在的屏幕*/
	public static final String ACTION_REQUEST_FOCUS = GOWIDGET + "_action_request_focus";

	/**大主题，所有放在桌面的widget更换皮肤*/
	public static final String ACTION_CHANGE_WIDGETS_THEME = GOWIDGET
			+ "_action_change_widgets_theme";

	/**大主题，跳到gowidget未安装页面*/
	public static final String ACTION_GOTO_GOWIDGET_FRAME = GOWIDGET
			+ "_action_goto_gowidget_frame";

	public static final String WIDGET_THEME_KEY = "widget_theme_key";

	//开关gowidget卸载的命令
	/**开关包名**/
	public static final String PKG_GOWIDGET_SWITCH = "com.gau.go.launcherex.gowidget.switchwidget";

	/** 广播将卸载开关**/
	public static final String ACTION_ON_OFF_UNINSTALL_BROADCAST = GOWIDGET
			+ "_uninstall_gowidget_on_off";

	/** 桌面广播是否卸载开关成功**/
	//	public static final String ACTION_ON_OFF_RECEIVER__UNINSTALL_IS_SUCCESS = GOWIDGET + "_uninstall_gowidget_on_off_is_success";

	/**桌面广播是否卸载开关成功，解析字段*/
	public static final String ACTION_ON_OFF_RECEIVER_UNINSTALL_IS_SUCCESS = "is_success";

	/**AppInvoker中使用*/
	public static final String ACTION_SWITCH_SERVICE = "com.gau.go.launcherex.gowidget.switchwidget.SwitchService";

	/************************************************ END WIDGET ***************************************************************************************/

	/************************************************ BEGIN 其他 *****************************************************************************/

	// Intent actions that we receive in the BillingReceiver from Market.
	// These are defined by Market and cannot be changed.
	public static final String ACTION_NOTIFY = "com.android.vending.billing.IN_APP_NOTIFY";
	public static final String ACTION_RESPONSE_CODE = "com.android.vending.billing.RESPONSE_CODE";
	public static final String ACTION_PURCHASE_STATE_CHANGED = "com.android.vending.billing.PURCHASE_STATE_CHANGED";

	// Intent actions that we send from the BillingReceiver to the
	// BillingService. Defined by this application.
	public static final String ACTION_CONFIRM_NOTIFICATION = "com.example.subscriptions.CONFIRM_NOTIFICATION";
	public static final String ACTION_GET_PURCHASE_INFORMATION = "com.example.subscriptions.GET_PURCHASE_INFORMATION";
	public static final String ACTION_RESTORE_TRANSACTIONS = "com.example.subscriptions.RESTORE_TRANSACTIONS";

	/** This is the action we use to bind to the MarketBillingService. */
	public static final String ACTION_MARKET_BILLING_SERVICE = "com.android.vending.billing.MarketBillingService.BIND";

	// ThemeAppInBillinManager
	public static final String ACTION_PURCHASE_STATE_RESULT = "go.launcherex.purchase.state.RESPONSE";
	public static final String ACTION_PURCHASE_SUPPORTED_RESULT = "go.launcherex.purchase.supported.RESPONSE";

	// GO备份广播的action BackUpSettingReceiver
	public static final String ACTION_RESTORE = "com.jiubang.go.backup.ACTION_RESTORE_GOLAUNCHER_FINISH";
	public static final String ACTION_BACKUP = "com.jiubang.go.backup.ACTION_BACKUP_GOLAUNCHER_FINISH";

	// 与桌面搬家相关的Action
	/**桌面搬家 */
	public static final String ACTION_DESK_MIGRATE = "com.ma.deskmigrate.DeskMigrate";
	/**桌面搬家准备*/
	public static final String ACTION_DESK_MIGRATE_PREPARED = "com.ma.deskmigrate.Prepared";

	/**下载消息广播到消息中心*/
	public static final String ACTION_MESSAGECENTER_DOWNLOAD = "MessageCenter_Action_Download";

	/**用于启动程序的信息界面*/
	public static final String ACTION_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS";

	/**CropImageActivity中使用到的Action 内联数据*/
	public static final String ACTION_INLINE_DATA = "inline-data";

	// locker
	public static final String ACTION_LOCKER = "com.jiubang.goscreenlock";
	public static final String ACTION_LOCKER_SETTING = "com.jiubang.goscreenlock.setting";
	public static final String ACTION_LOCKER_THEME = "com.jiubang.goscreenlock.theme";

	// notification 包
	/** 包名**/
	public final static String NOTIFICATION_PACKAGE = "com.gau.golauncherex.notification";
	public final static String ACTION_NOTIFICATION_SETTING_DETAIL = "com.gau.golauncherex.notification.detail";
	public final static String ACTION_NOTIFICATION_LAUCHER_BOOTUP = "com.gau.golauncherex.notification.bootup";

	// start 兼容老版本 1.1
	public final static String ACTION_NOTIFICATIONACTION_START_SMS_MONITOR = "com.gau.golauncherex.notification.startsmsmonitor";
	public final static String ACTION_NOTIFICATIONACTION_STOP_SMS_MONITOR = "com.gau.golauncherex.notification.stopsmsmonitor";
	public final static String ACTION_NOTIFICATIONACTION_COUNT_UNREAD_SMS = "com.gau.golauncherex.notification.countunreadsms";
	public final static String ACTION_NOTIFICATIONACTION_START_CALL_MONITOR = "com.gau.golauncherex.notification.startcallmonitor";
	public final static String ACTION_NOTIFICATIONACTION_STOP_CALL_MONITOR = "com.gau.golauncherex.notification.stopcallmonitor";
	public final static String ACTION_NOTIFICATIONACTION_COUNT_UNREAD_CALL = "com.gau.golauncherex.notification.countunreadcall";
	public final static String ACTION_NOTIFICATIONACTION_START_GMAIL_MONITOR = "com.gau.golauncherex.notification.startgmailmonitor";
	public final static String ACTION_NOTIFICATIONACTION_STOP_GMAIL_MONITOR = "com.gau.golauncherex.notification.stopgmailmonitor";
	public final static String ACTION_NOTIFICATIONACTION_COUNT_UNREAD_GMAIL = "com.gau.golauncherex.notification.countunreadgmail";
	public final static String ACTION_NOTIFICATIONACTION_ACCOUNT_SETTING_GMAIL = "com.gau.golauncherex.notification.gmailaccount";
	public final static String ACTION_NOTIFICATIONACTION_GMAIL_LOGIN_ERROR = "com.gau.golauncherex.notification.gmail_login_error";
	public final static String ACTION_NOTIFICATIONACTION_GMAIL_MONITOR_STOPED = "com.gau.golauncherex.notification.gmail_stoped";
	public final static String ACTION_NOTIFICATIONACTION_GMAIL_MONITOR_CANCLED = "com.gau.golauncherex.notification.gmail_cancled";
	public final static String ACTION_NOTIFICATIONACTION_STOP_ALL_MONITOR = "com.gau.golauncherex.notification.stopallmonitor";
	// end

	public final static String ACTION_NOTIFICATIONACTION_REQUEST = "com.gau.golauncherex.notification.request";
	public final static String ACTION_NOTIFICATIONACTION_RESPOND = "com.gau.golauncherex.notification.respond";
	public final static String ACTION_NOTIFICATIONACTION_DESTROY = "com.gau.golauncherex.notification.destroy";
	public final static String ACTION_NOTIFICATIONACTION_FACEBOOK_LOGIN_ERROR = "com.gau.golauncherex.notification.facebook_login_error";
	public final static String ACTION_NOTIFICATIONACTION_SINAWEIBO_LOGIN_ERROR = "com.gau.golauncherex.notification.sinaweibo_login_error";

	/** DiyScheduler中使用的Action**/
	public final static String ACTION_NOTIFICATIONACTION_START_SERVICE = "com.gau.golauncherex.notification.start";

	// 用于通用application广播action
	public final static String ACTION_NOTIFICATIONACTION_REQUEST_APPLICATION = "com.gau.golauncherex.notification.request_application";
	public final static String ACTION_NOTIFICATIONACTION_RESPOND_APPLICATION = "com.gau.golauncherex.notification.respond_application";
	public final static String ACTION_NOTIFICATIONACTION_RESET_APPLICATION = "com.gau.golauncherex.notification.reset_application";
	public final static String ACTION_NOTIFICATIONACTION_RESET_SHOW_DIALOG = "com.gau.golauncherex.notification.reset_show_dialog";
	public final static String ACTION_NOTIFICATION_STOP_ACCESSIBILITY = "com.gau.golauncherex.notification.stop.accessibility";

	// notification包结束

	/************************************************ END 其他 ****************************************************************************************/

	/************************************* BEGIN 系统Action ******************************************************************************************/

	// AppInvoker中使用的系统Action
	public static final String ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED";
	public static final String ACTION_CALL = "android.intent.action.CALL";

	// MonitorSver中使用的系统Action
	public static final String ACTION_EXTERNAL_APPLICATIONS_AVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
	public static final String ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
	public static final String ACTION_MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";
	public static final String ACTION_MEDIA_SHARED = "android.intent.action.MEDIA_SHARED";

	// widget 使用
	/** 屏幕关闭 */
	public static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
	/** 屏幕开启 */
	public static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";

	/**用于启动GoLauncher*/
	public static final String ACTION_MAIN = "android.intent.action.MAIN";

	/********************************************* END 系统Action************ *************************************************************************/

	/********************************************* BEGIN 包名 *****************************************************************************************/
	/**GoWidget包名**/
	public static String MAIN_GOWIDGET_PACKAGE = "com.gau.go.launcherex.gowidget";

	/*********************************************** END 包名 *****************************************************************************************/

	public static final String ACTION_MSGCENTER_SHOWMSG = "android.intent.action.MSGCENTER_SHOWMSG";
	/**显示时间已过移除消息**/
	public static final String ACTION_MSGCENTER_REMOVEMSG = "android.intent.action.MSGCENTER_REMOVEMSG";
	
	/**新主题通知栏消息**/
	public static final String ACTION_CHECK_NEWTHEME_NOTIFY =  "android.intent.action.CHECK_NEWTHEME_NOTIFY";
	public static final String ACTION_REMOVE_NEWTHEME_NOTIFY =  "android.intent.action.REMOVE_NEWTHEME_NOTIFY";
	
	public static final String ACTION_STOP_SNAPSHOT =  "com.jiubang.ggheart.apps.desks.snapshot.STOP_SNAPSHOT";

	/**1 5屏的广告action**/
	public static final String ACTION_SCREEN_ADVERT =  "com.jiubang.intent.aciton.screen.advert";
	
	/**百度图标,桌面图标*/
	public static final String ACTION_DESK_SHOW_BAIDUBROWSER = "com.jiubang.intent.aciton_BAIDUBROWSER";
	
	public static final String ACTION_THEME_PAYVIPPAGE = "com.jiubang.intent.aciton_THEME_PAYVIPPAGE";
	
	public static final String ACTION_TOUCHHELPER = "com.gau.go.launcher.superwidget.start";
}
