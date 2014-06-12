package com.jiubang.ggheart.apps.desks.diy;

/**
 * IPreferencesIds
 */
public interface IPreferencesIds {
	public static final String DESK_SHAREPREFERENCES_FILE = "desk";
	// 进入功能表编辑状态次数
	public static final String ENTER_FUNC_EDIT = "ENTER_EDIT";
	// 进入功能表次数
	public static final String ENTER_FUNC = "ENTER_FUNC";

	/**
	 * 屏幕预览编辑时提示次数
	 */
	public static final String PREVIEW_DRAG_TIP_TIME = "preview_drag_tip";

	/**
	 * 长按图标显示提示信息
	 */
	public static final String SCREEN_LONG_PRESS_TIP = "screen_long_press_tip";

	/**
	 * 按dock+号图标显示提示信息
	 */
	// public static final String Dock_PRESS_TIP = "dock_tip";

	/**
	 * 进入预览时提示的次数
	 */
	public static final String PREVIEW_TIP_TIME = "preview_tip_time";

	/**
	 * 特殊提示框次数
	 */
	public static final String SHOW_SPECIAL_DIALOG_TIME = "special_dialog_time";

	/**
	 * 菜单新建文件夹次数
	 */
	public static final String APP_FUNC_NEW_FOLDER = "APP_FUNC_NEW_FOLDER";

	// 进入功能表次数
	public static final String ENTER_PREFERENCE = "ENTER_PREFERENCE";

	/**
	 * 移动图标失败次数
	 */
	public static final String DRAG_FAILED_COUNT = "drag_failed_count";

	/**
	 * GOSTORE可更新软件数目
	 */
	public static final String GOSTORE_UPDATE_COUNT = "gostore_update_count";

	/**
	 * 我的应用，列表当前排序方式
	 */
	public static final String APPS_ORDER_TYPE = "apps_order_type";

	/**
	 * 我的应用，批量卸载 是否首次进入
	 */
	public static final String APPS_UNINSTALL_SHOW_DIALOG = "apps_uninstall_show_dialog";

	/**
	 * 我的应用，记录root权限信息
	 */
	public static final String APPS_UNINSTALL_GET_ROOT = "apps_uninstall_get_root";

	// 特殊UID
	public final static String UID_CONFIG = "uid_config";
	public final static String UID_CONFIG_KEY = "uid";

	/**
	 * 当前桌面的语言
	 */
	public static final String CURRENTLANGUAGE = "current_language";

	/**
	 * 当前桌面的语言
	 */
	public static final String CURRENTSELETELANGUAGE = "currentseltet_language";

	/**
	 * 当前桌面的语言备份
	 */
	public static final String CURRENTSELETELANGUAGEBACK = "currentseltet_language_back";

	/**
	 * 当前桌面的数据库版本号
	 */
	public static final String GO_DB_VERSION = "current_dbversion";

	/**
	 * 桌面设置使用到的SharedPreferences文件
	 */
	public final static String SETTING_PREFERENCES = "setting_preferences";

	public final static String SHOW_ALERT_DIALOG_FOR_ACTION_BAR_SETTING = "show_alert_dialog_for_action_bar_setting";

	public final static String MEDIA_MANAGEMENT_IMAGE_FOLDER_SORT_TYPE = "media_management_image_folder_sort_type";
	public final static String MEDIA_MANAGEMENT_IMAGE_SORT_TYPE = "media_management_image_sorting_type";
	public final static String MEDIA_MANAGEMENT_MUSIC_FOLDER_SORT_TYPE = "media_management_music_folder_sort_type";
	public final static String MEDIA_MANAGEMENT_MUSIC_SORT_TYPE = "media_management_music_sort_type";
	public final static String MEDIA_MANAGEMENT_VIDEO_SORT_TYPE = "media_management_video_sort_type";
	public final static String MEDIA_MANAGEMENT_MUSIC_PLAYLIST_SORT_TYPE = "media_management_music_playlist_sort_type";
	public final static String MEDIA_MANAGEMENT_MUSIC_DEFAULT_OPEN_TYPE = "media_management_music_default_open_type";
	public final static String MEDIA_MANAGEMENT_MUSIC_PLAYLIST_ITEM = "media_management_music_playlist_item";

	public final static String PLAYLIST_SORT_SETIING_PREFERENCES_FILE = "playlist_sort_setiing_preferences_file";

	/**
	 * 保存圣诞节特效状态的KEY
	 */
	public static final String HOLIDAY_SHAREPREFERENCES_FILE_NEW = "holiday_shareperferences_file_new";
	public static final String HOLIDAY_CHRISTMAS_STATE_NEW = "holiday_christmas_state_new";
	public static final String HOLIDAY_CHRISTMAS_COUNT = "holiday_christmas_count";
	/**

	 * 保存是否安装了圣诞节2的主题的key
	 */
	public static final String HOLIDAY_CHRISTMAS2_FILE = "holiday_christmas2_file";
	public static final String HOLIDAY_CHRISTMAS2_IS_INSTALL = "holiday_christmas2_is_install";

	/**
	 * 保存颜色选择器的坐标--字体
	 */
	public static final String ORIENTATION_XY_FONT = "orientation_xy_font";
	/**
	 * 保存颜色选择器的坐标--图标
	 */
	public static final String ORIENTATION_XY_ICON = "orientation_xy_icon";
	/**
	 * 特殊处理-保存桌面设置-壁纸，功能表，快捷条背景设置
	 */
	public static final String DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW = "desk_setting_visual_background_tab_view";
	public static final String DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_APPDRAWER = "desk_setting_visual_background_tab_view_appdrawer";
	public static final String DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_DOCK = "desk_setting_visual_background_tab_view_dock";
	public static final String DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_WALLPAPER = "desk_setting_visual_background_tab_view_wallpaper";

	/**
	 * 是否支持DB数据共享
	 */
	public final static String DB_PROVIDER_SUPPORT = "db_provider_support";
	public final static String IMPORT_SUPPORT = "support";
	public final static String HAS_IMPORTED = "has_imported";

	public static final String FEATUREDTHEME_CONFIG = "featuredtheme_config";
	public static final String LAUNCHER_FEATUREDTHEME_STAMP = "launcher_featuredtheme_stamp";
	public static final String LCOKER_FEATUREDTHEME_STAMP = "lcoker_featuredtheme_stamp";
	public static final String HASNEWTHEME = "hasnewtheme";
	public static final String HOTTHEME_CONFIG = "hottheme_config";
	public static final String SHOWHASNEWTHEME = "showhasnewtheme";
	public static final String HASSHOWFEATURENOTIFY = "hasshowfeaturenotify";
	public static final String HASSHOWLOCKERNOTIFY = "hasshowlockernotify";
	public static final String HASSHOWHOTNOTIFY = "hasshowhotnotify";
	public static final String LOCKER_HASNEWTHEME = "has_new_locker_theme";
	public static final String LAST_SHOW_NEWTHEME_STAMP = "last_show_stamp";

	public final static String PREFNAME = "install_prefs";
	public final static String KEY_REFERRER_INFO_STORED_FLAG = "referrer_info_store_flag";

	// add by zhoujun 服务器更新推荐应用的标识值，服务器通过该值判断是否有更新
	public static final String APP_MANAGER_RECOMMEND = "recommendedApp";
	public static final String APP_MANAGER_RECOMMEND_PHEAD = "recommendedApp_phead";
	public static final String APP_MANAGER_RECOMMEND_MARK = "mark";
	public static final String APP_MANAGER_CATEGORIES_APP_MARK = "CATEGORIES_APP_MARK";
	public static final String APP_MANAGER_CATEGORIES_GAME_MARK = "CATEGORIES_GAME_MARK";
	public static final String APP_MANAGER_FEATURE_APP_MARK = "FEATURE_APP_MARK";
	public static final String APP_MANAGER_FEATURE_GAME_MARK = "FEATURE_GAME_MARK";

	public static final String PREFENRCE_NETLOG_STATISTICS_DATA = "netlog_statistics_data";
	//应用游戏中心widget，安装后第一次滑动在widget所在的屏幕时，Toast提示用户可以上下滑动切换内容
	public static final String APPGAME_WIDGET_SHOW_MESSAGE = "appgame_widget_show_message";

	/**
	 * 应用市场，公共util
	 */
	public static final String GOMARKET_PUBLIC_UTIL = "gomarket_public_util";

	/**
	 * 用户向导配置
	 */
	public static final String USERTUTORIALCONFIG = "tutorial";
	public static final int SHOW_SCREEN_EFFECT_FIRST_GUIDE_COUNT = 20; // 第一次提示滑屏次数
	public static final int SHOW_SCREEN_EFFECT_SECONDE_GUIDE_COUNT = 20; // 第二次提示滑屏次数
	public static final String SCREEN_CHANGE_COUNT = "screenchangecount"; // 一共滑了几次屏
	// 是否在第一次桌面滑屏特效提示中设置了特效
	public static final String SHOULD_SHOW_SCREEN_EFFECT_SECOND_TIP = "should_show_screen_effect_second_tip";
	public static final String SHOULD_SHOW_DOCK_SLIP_GUIDE = "should_show_dock_slip_guide";
	public static final String SHOULD_SHOW_DOCK_GESTURE_GUIDE = "should_show_dock_gesture_guide";
	public static final String SHOULD_SHOW_APPFUNC_FOLDER_GUIDE = "show_appfunc_folder_guide";
	public static final String SHOULD_SHOW_APPFUNC_DRAG_GUIDE = "show_appfunc_drag_guide";
	public static final String SHOULD_SHOW_APPFUNC_MENU_GUIDE = "show_appfunc_menu_guide";
	public static final String SHOULD_SHOW_SCREEN_LOCK_GUIDE = "show_screen_lock_guide";
	public static final String SHOULD_SHOW_PREVIEW_HOME = "show_preview_home"; //add by jiang 设置为默认桌面后  按home键跳屏幕预览
	public static final String SHOULD_SHOW_SCREEN_LOCK_GGMENU = "show_screen_lock_ggmenu";  // 是否高亮显示ggmenu锁定编辑
	public static final String SHOULD_SHOW_WAPAPERSETTING_GUIDE = "should_show_wallpapersetting_guide";
	public static final String SHOULD_SHOW_PRIVIEW_GUIDE = "sensetutorial";
	public static final String SHOULD_SHOW_SCREENFOLDER_GUIDE = "should_show_screenfolder_guide";
	public static final String SHOULD_SHOW_SCREEN_MENU_OPEN_GUIDE = "show_screen_menu_open_guide";
	public static final String SHOULD_SHOW_APPFUNC_APP_UPDATE_GUIDE = "show_appfunc_app_update_guide";
	public static final String SHOULD_SHOW_DOCK_AUTO_FIT_GUIDE = "should_show_dock_auto_fit_guide";
	public static final String SHOULD_SHOW_CUSTOM_GESTURE = "show_custom_gesture_guide"; // 自定义手势提示
	public static final String CANCLE_DIYGESTURE_TIME = "cancle_diygesture_time"; // 关闭自定义手势的次数
	public static final String SHOULD_SHOW_SHARE = "sharetutorial";
	public static final String SHOULD_SHOW_SNAPSHOT = "snapshottutorial"; // 截图提示框
	public static final String SNAPSHOT_IS_ROOTED = "snapshot_is_rooted"; // 机器是否已经root
	public static final String REQUIRE_ROOT_NEVER_SHOW = "require_root_never_show"; // 请求root权限
	public static final String SHOULD_SHOW_NEED_ROOT = "should_should_need_root"; // 截图提示框
	public static final String IS_SCREENSHOT_RUNNING = "is_screenshot_running"; // 截图功能是否运行，异常退出标志，用来重启
	public static final String IS_SCREENSHOT_RUNNING_IN_ROOT_MODE = "is_screenshot_running_in_root_mode"; // 截图模式，异常退出标志，用来重启
	public static final String SHOULD_SHOW_ONE_X_GUIDE = "should_show_one_x_guide";
	public static final String SHOULD_SHOW_LANGUAGE_GUIDE = "languagetutorial";
	public static final String SHOULD_SHOW_DOCK_BAR_ICON_GESTURE = "should_show_dock_bar_icon_guide"; // dock栏图标提示 
	public static final String SHOULD_SHOW_FULL_SCREEN_WIDGET = "should_show_full_screen_widget";

	public final static String NOTIFICATION_SETTING = "notification_setting";

	public final static String DIY = "DIY";

	public final static String PREFERENCES_FOR_PAD_DIALOG = "needToDialog";
	public final static String PREFERENCES_FOR_PAD_DIALOG_SHOW = "NewShowDialog";

	public static final String ERRORREPORTER = "ErrorReport";

	//统计
	public static final String ALL_PURPOSE_STAT = "all_purpose_stat"; //万能统计的SharedPreferences
	public static final String DESK_ACTION_DATA = "desk_action_data"; //桌面用户行为统计的SharedPreferences
	public static final String APP_FUNC_ACTION_DATA = "app_func_action_data"; //功能表用户行为的SharedPreferences
	public static final String THEME_TAB_STAT_DATA = "theme_tab_stat_data"; //桌面主题tab统计数据
	public static final String USERECORD_STAT = "userecord_stat"; // 使用记录用SharedPreferences
	public static final String MENU_COUNT_DATA = "menu_count_data"; // 使用记录用SharedPreferences
	public static final String STATISTIC_HTTP_EXCEPTION_DATE = "statistic_http_exception_date"; // 使用记录用SharedPreferences
	public static final String STATISTIC_NO_UPLOAD_DATE = "statistic_no_upload_date"; // 使用记录用SharedPreferences
	public static final String APP_DATA = "app_data"; // 使用记录用SharedPreferences
	public static final String APP_SHOW_DATA = "app_show_data"; // 使用记录用SharedPreferences
	public static final String STATISTIC_DATA = "statistic_data"; // 使用记录用SharedPreferences
	public static final String APPFUNC_INOUT_EFFECT = "menu_inout_effect";
	public static final String APPFUNC_HORIZONTAL_EFFECT = "menu_horizontal_effect";
	public static final String APPFUNC_VERTICAL_EFFECT = "menu_vertical_effect";
	public static final String BACKUP = "backup";
	public static final String ENTRY_COUNT_STAT = "entry_count_stat";
	public static final String SEARCH_KEYWORDS_STAT = "search_keywords_stat";
	public static final String USER_BASE_STAT = "user_base_stat";
	public static final String STATISTICS_DATA_FILE_NAME = "statisticsData"; // 保存统计数据的sharedPreference文件名
	public static final String STATISTICS_USE_TIME_KEY = "use_time"; // 保存桌面使用时间所使用的KEY
	public static final String STATISTICS_NET_TIME_KEY = "net_time"; // 保存网络请求成功使用时间所使用的KEY
	public static final String STAT_GUI_TAB = "stat_gui_tab"; //GUI　TAB点击次数统计的SharedPreferences
	public static final String STAT_GUI_ENTRY = "stat_gui_entry"; //GUI进入次数统计的SharedPreferences
	public static final String APPGAME_TREATMENT = "appgame_treatment"; //应用游戏中心应用treatment的SharedPreferences

	public final static String SHAREDPREFERENCES_THEME = "themepackage"; // 主题的sharedpreferences名称

	public final String CUR_THEME_PKG = "cur_theme_pkg";
	public final String CUR_THEME_PKG_PREFERENCES = "pubicthemespreferences";

	public static final String NOTIFICATION_MORE_APP_TIP = "notification_more_app_tip";
	public static final String SHOULD_SHOW_MORE_APP_TIP = "should_show_more_app_tip";
	public static final String HIDE_APP_SHOW_TIP = "hide_app_show_tip";
	public static final String HIDE_APP_SHOW_TIP_COUNT = "hide_app_show_tip_count";

	public static final String ADVERT_SCREEN_DATA = "advert_screen_data"; // 使用记录用SharedPreferences 15屏广告点击统计
	public static final String ADVERT_NEET_OPEN_DATA = "advert_net_open_data"; //15屏广告安装后需要进行打开提示

	//消息中心
	public final static String SHAREDPREFERENCES_MSG_UPDATE = "msg_update";
	public final static String SHAREDPREFERENCES_MSG_STATISTICSDATA = "msgcenter_statisticsdata";
	public final static String SHAREDPREFERENCES_MSG_SHOW_TIMES = "show_times";
	public final static String SHAREDPREFERENCES_MSG_CLICK_TIMES = "click_times";
	public final static String SHAREDPREFERENCES_MSG_PUSH_TIMES = "push_times";
	public final static String SHAREDPREFERENCES_MSG_NEW_PRODUCT_STAMP = "new_product_stamp";
	public final static String SHAREDPREFERENCES_MSG_BUTTON_NAME = "button_name";
	public final static String SHAREDPREFERENCES_MSG_BUTTON_CLICK_TIMES = "button_click_times";
	public final static String SHAREDPREFERENCES_MSG_COVER_FRAME_CLOSE_BUTTON_CLICK_TIMES = "cover_frame_button_click_times";

	public final static String SHAREDPREFERENCES_MSG_THEME_NOTIFY_STATICS_DATA = "theme_notify_statics";

	public final static String SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_STATICS_DATA = "locker_theme_notify_statics";

	public final static String SHAREDPREFERENCES_MSG_THEME_NOTIFY_SHOW_STATICS_DATA = "theme_notify_show_statics";
	public final static String SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_SHOW_STATICS_DATA = "locker_notify_show_statics";

	//facebook
	public final static String FACEBOOK_RECORD = "facebook_record";
	public final static String FACEBOOK_REMIND_SHARE_TIEM = "remind_share_time"; // 用于实现定时弹框
	public final static String FACEBOOK_USE_DIALOG_LOGIN = "usedialoglogin"; // 强制使用FBDialog登陆
	public final static String FACEBOOK_LAST_BACKUP_TIME = "last_backup_time"; // 最近备份时间
	public final static String FACEBOOK_LOGIN_AS_USER = "login_user"; // 当前登陆用户
	public final static String FACEBOOK_FIRST_SWITCH_THEME = "first_switch_theme"; // 用户安装后第一次切换主题
	public final static String FACEBOOK_OPEN_GRAPH_SWITCH = "open_graph_switch"; // OPenGraph开关
	public final static String FACEBOOK_MESSAGE_CENTER_SWITCH = "message_center_switch"; //消息中心控制开关
	public final static String FACEBOOK_RESTORE_NAME_LIST = "restore_name_list"; // 此机上用过facebook恢复备份的帐号
	public final static String FACEBOOK_RESTART_AFTER_RESTORE = "restart_after_restore"; // facebook恢复备份后,是否显示分享框

	public final static String THEME_CUSTOMER_LEVEL_1 = "theme_customer_level_1";
	public final static String THEME_CUSTOMER_LEVEL_2 = "theme_customer_level_2";
	public final static String CUSTOMER_VIP = "customer_vip";
	public final static String HAD_SHOW_VIP_TIP = "had_show_vip_tip";
	
	//touchhelper
	public final static String DEFAULT_TOUCHHELPER_PKG = "default_touchhelper_pkg";

	//如果恢复默认时需要清除某个sharepreference请将它加入这里
	public static final String[] NEED_CLEAR_PREFERENCES = {
			IPreferencesIds.DESK_SHAREPREFERENCES_FILE, IPreferencesIds.USERTUTORIALCONFIG,
			IPreferencesIds.DB_PROVIDER_SUPPORT, IPreferencesIds.NOTIFICATION_MORE_APP_TIP,
			IPreferencesIds.HIDE_APP_SHOW_TIP, SHAREDPREFERENCES_MSG_UPDATE,
			IPreferencesIds.FEATUREDTHEME_CONFIG, IPreferencesIds.HOTTHEME_CONFIG,
			SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_SHOW_STATICS_DATA,
			SHAREDPREFERENCES_MSG_THEME_NOTIFY_SHOW_STATICS_DATA,
			SHAREDPREFERENCES_MSG_THEME_NOTIFY_STATICS_DATA,
			SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_STATICS_DATA, ADVERT_NEET_OPEN_DATA,
			IPreferencesIds.FOLDER_DATA_CORRUPTION };

	/**
	 * 是否再次提醒评分
	 */
	public static final String REMIND_RATE = "remind_rate";

	/**
	 * 记录当前弹出评分对话框时间
	 */
	public static final String REMIND_RATE_TIME = "remind_rate_time";

	/**
	 * 是否需要弹出评分
	 */
	public static final String IS_NEED_SHOW_RATE_DIALOG = "is_need_show_rate_dialog";

	/**
	 * 记录是否第一次运行。并进行评分提醒
	 */
	public static final String FIRST_RUN_REMIND_RATE = "first_run_remind_rate";

	/**
	 * 功能表搜索的perference；
	 */
	public static final String APPFUNC_SEARCH_STATISTIC_DATA = "appfunc_search_statistic_data";

	public static final String FOLDER_DATA_CORRUPTION = "folder_data_corruption";

	public static final String LOCKER_SETTING_PRF = "locker_setting_prf";
	public static final String ENTER_LOCKER_TAB = "enter_locker_tab";
	public static final String CLICK_LOCKER_SETTING = "click_locker_setting";
	public static final String HAS_SHOW_LOCKER_SETTING_NOTIFY = "has_show_locker_setting_notify";

	public static final String THEME_SETTING_CONFIG = "theme_setting_config";
	public static final String HAS_SHOW_VIPUPGRADE = "has_show_vipupgrade";
	
	/**
	 * 不再提示全屏插件下载提示
	 */
	public static final String NEVER_SHOW_TOUCHHELPER_RECOMMAND = "never_show_touchhelper_recommand";
	
	/**
	 * 下载管理，记录下载任务状态
	 */
	public static final String DOWNLOAD_MANAGER_TASK_STATE = "download_manager_task_state";
}
