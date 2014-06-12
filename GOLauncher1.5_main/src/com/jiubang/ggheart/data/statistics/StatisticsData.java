package com.jiubang.ggheart.data.statistics;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import android.content.Context;

import com.gau.utils.net.request.THttpRequest;
import com.go.util.device.ConfigurationInfo;
import com.jiubang.ggheart.appgame.appcenter.component.CommandManager;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.advert.AdvertConstants;
import com.jiubang.ggheart.components.advert.AdvertControl;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 利用SharedPreferences保存简单类型的统计数据
 * 
 * @author zhouxuewen
 */

public class StatisticsData {

	// 要保存的统计项的KEY

	public static final String MENU_SHARE_DIALOG = "menu_share_dialog";
	public static final String MENU_SHARE_OPEN_DIALOG_TIMES = "menu_share_open_dialog_times";
	public static final String GO_HANDBOOK = "go_handbook";
	public static final String GO_HANDBOOK_OPEN_TIMES = "go_handbook_open_times";
	public static final String GO_HANDBOOK_READ_PERCENT = "go_handbook_read_percent";
	public static final String GO_HANDBOOK_IMMEDIATELY_TRIAL = "go_handbook_immediately_rial";
	public static final String GO_HANDBOOK_CONTINUE_BROWSER = "go_handbook_coutinue_browser";
	public static final String GO_SAFE_LAUNCHER = "go_safe_launcher";
	public static final String GO_SAFE_LAUNCHER_CLOUD_KILLING = "go_safe_launcher_cloud_killing";
	public static final String KEY_OLD_USER = "key_old_user";

	public static final String MENU_KEY_ADD = "menu_add";
	public static final String MENU_KEY_WALLPAPER = "menu_wallpaper";
	public static final String MENU_KEY_THEMES = "menu_themes";
	public static final String MENU_KEY_GOSTORE = "menu_gostore";
	public static final String MENU_KEY_EDIT = "menu_edit";
	public static final String MENU_KEY_PREFERENCES = "menu_preferences";
	public static final String MENU_KEY_SETTING = "menu_setting";
	public static final String MENU_KEY_EFFECT = "menu_effect";

	public static final String MENU_KEY_GOLOCKER = "menu_golocker";
	public static final String MENU_KEY_GOWIDGET = "menu_gowidget";
	public static final String MENU_KEY_NOTIFICATION = "menu_notification";

	public static final String MENU_KEY_UPDATE = "menu_update";
	public static final String MENU_KEY_SHARE = "menu_share";
	public static final String MENU_KEY_RATE = "menu_rate";
	public static final String MENU_KEY_FEEDBACK = "menu_feedback";
	public static final String MENU_KEY_RESTART = "menu_restart";
	public static final String MENU_KEY_STATUSBAR = "menu_statusbar";
	public static final String MENU_KEY_LOCKSCREEN = "menu_lockscreen";

	public static final String HIDE_APP = "hide_app";

	public static final String FUNTAB_KEY_ALL = "tab1";
	public static final String FUNTAB_KEY_RECENT = "tab2";
	public static final String FUNTAB_KEY_RUNNING = "tab3";
	public static final String FUNTAB_KEY_IMAGE = "tabimage";
	public static final String FUNTAB_KEY_AUDIO = "tabaudio";
	public static final String FUNTAB_KEY_VIDEO = "tabvideo";

	public static final String ENTRY_KEY_MEUN = "10001";
	public static final String ENTRY_KEY_GOFOLDER = "10002";
	public static final String ENTRY_KEY_WIDGET_FRAME = "10003";
	public static final String ENTRY_KEY_WIDGET_TOP = "10004";
	public static final String ENTRY_KEY_WIDGET_THEME = "10005";
	public static final String ENTRY_KEY_WIDGET_LOCKER = "10006";
	public static final String ENTRY_KEY_WIDGET_WIDGET = "10007";
	public static final String ENTRY_KEY_APPFUNC = "10008";
	public static final String ENTRY_KEY_THEMEMANAGE = "10009";
	public static final String ENTRY_KEY_APPCENTER = "100010";
	public static final String ENTRY_KEY_WALLPAPER = "100011";
	public static final String ENTRY_KEY_SHOWDONE = "100012";

	public static final String KEY_AUTO_DOWNLOAD = "2";
	public static final String KEY_DOWNLOAD_COMPLETE_ALL = "3";
	public static final String KEY_DOWNLOAD_COMPLETE_APPCENTER = "4";
	public static final String KEY_DOWNLOAD_COMPLETE_GAMEZONE = "5";
	
	public static final String KEY_NOLOAD_ICON = "9";
	public static final String KEY_ONLY_ICON = "10";
	public static final String KEY_LOAD_ALL = "11";
	
	public static final String KEY_DELETE_FILE = "12";
	public static final String KEY_NO_DELETE_FILE = "13";
	
	public static final String KEY_REFRESH_APP = "14";
	public static final String KEY_REFRESH_GAME = "15";

	public static final String KEY_BATCH_UNINSTALL = "16";
	public static final String KEY_NO_BATCH_UNINSTALL = "17";
	
	public static final String KEY_IGNORE_UPDATA = "19";
	public static final String KEY_DOWNLOAD_MANAGER_CLICK = "20";
	public static final String KEY_INSTALL_MANAGER_CLICK = "21";
	public static final String KEY_APP_MOVE_CLICK = "22";
	public static final String KEY_PROVINCE_FLOW_POPUP_YES = "23";
	public static final String KEY_PROVINCE_FLOW_POPUP_NO = "24";
	
	//25“Facebook备份GO桌 面”按钮点击次数
	public static final String KEY_FACEBOOK_BACKUP_CLICK_TIMES = "25";
	//26“Facebook恢复备份”按钮点击次数
	public static final String KEY_FACEBOOK_RESTORE_BACKUP_CLICK_TIMES = "26";
	//27“Facebook帐号登出”按钮点击次数
	public static final String KEY_FACEBOOK_ACOUNT_CHECKOUT = "27";
	
	//28“批量删除”按钮点击次数
	public static final String KEY_DELETE_ALL = "28";
	
	// 全部更新的统计
	public static final String STAT_KEY_ALLUPDATA = "6";

	// 用于统计屏幕数
	public static int sSCREEN_COUNT = 5;

	// 菜单各个的对应ID
	private final static int MENU_ID_ADD = 100;
	private final static int MENU_ID_WALLPAPER = 101;
	private final static int MENU_ID_THEMES = 102;
	private final static int MENU_ID_GOSTORE = 103;
	private final static int MENU_ID_EDIT = 104;
	private final static int MENU_ID_PREFERENCES = 105;
	private final static int MENU_ID_SETTING = 106;
	private final static int MENU_ID_EFFECT = 107;
	private final static int MENU_ID_GOLOCKER = 108;
	private final static int MENU_ID_GOWIDGET = 109;
	private final static int MENU_ID_NOTIFICATION = 110;
	private final static int MENU_ID_UPDATE = 112;
	private final static int MENU_ID_SHARE = 113;
	private final static int MENU_ID_RATE = 114;
	private final static int MENU_ID_FEEDBACK = 115;
	private final static int MENU_ID_RESTART = 116;
	private final static int MENU_ID_STATUSBAR = 117;
	private final static int MENU_ID_LOCKSCREEN = 118;
	public static final int MENU_ID_MANUAL = 8;
	public static final int MENU_ID_MANUAL_READ_PERCENAGE = 9;
	public static final int MENU_ID_MANUAL_TRY_NOW = 10;
	public static final int MENU_ID_MANUAL_CON_BROWSE = 11;

	public final static int ENTRY_APPS = 1;
	public final static int ENTRY_GAME = 2;

	/**
	 * root权限信息 0：系统没有root权限，1：系统有root权限，2：拒绝桌面root权限,3：授权桌面root权限
	 */
	public final static int ROOT_INFO_NO_ROOT = 0;
	public final static int ROOT_INFO_HAS_ROOT = 1;
	public final static int ROOT_INFO_REFUSE_ROOT = 2;
	public final static int ROOT_INFO_ACCEPT_ROOT = 3;

	public final static int SEARCH_ID_APPS = 1; // 搜索ID:应用中心
//	public final static int SEARCH_ID_GAMES = 2; // 搜索ID:游戏中心
	public final static int SEARCH_ID_GOSTORE = 3; // 搜索ID:GO精品
	public final static int SEARCH_ID_FUNC = 4; // 搜索ID:功能表

	private final static int VERSION = 4; // 版本协议号
	private static final String STATISTICS_DATA_SEPARATE_STRING = "||"; // 统计数据各字段分隔符
	public static final String STATISTICS_DATA_SEPARATE_STRING_ITEM = "\\|\\|"; // 用于分隔字符为数组时要用的关键字

	public static final String APPFUNC_APPLIST_ITEM = "appfuncapplistitem"; // 功能表-锁定应用列表
	public static final String CLEANDIRTYDATA_ITEM = "cleandirtydataitem"; // 高级设置-检查垃圾数据
	public static final String DESKMIGRATE_ITEM = "deskmigrateitem"; // 高级设置-桌面搬家
	public static final String BACKUP_ITEM = "backup_item"; // 备份-备份
	public static final String RESETBACKUP_ITEM = "resetbackup_item"; // 备份-恢复
	public static final String RESETDEFAULT_ITEM = "resetdefault_item"; // 备份-恢复默认
	public static final String GOBACKUP_ITEM = "gobackup_item"; // 备份-GO备份

	//用于分享的统计
	public static final String SHARE_KEY = "share_";
	public static final int SHARE_ID_SHARE = 7;
	//用于桌面用户行为的统计
	public static final int DESK_ACTION_ID_LONG_CLICK_WIDGET = 1;
	public static final int DESK_ACTION_ID_LONG_CLICK_ICON = 2;
	public static final int DESK_ACTION_ID_LONG_CLICK_FLODER = 3;
	public static final int DESK_ACTION_ID_LONG_CLICK_DOCK_ICON = 4;
	public static final int DESK_ACTION_ID_LONG_CLICK_BLANK_SCREEN = 5;
	public static final int DESK_ACTION_ID_CLICK_FLODER = 6;
	public static final int DESK_ACTION_ID_FLODER_SORT = 7;
	public static final int DESK_ACTION_ID_FLODER_SORT_BY_LETTER = 8;
	public static final int DESK_ACTION_ID_FLODER_SORT_BY_TIME_ASC = 9;
	public static final int DESK_ACTION_ID_FLODER_SORT_BY_TIME_DEC = 10;
	public static final int DESK_ACTION_ID_FLODER_SORT_BY_FREQUENCY = 11;
	public static final int DESK_ACTION_ID_FLODER_EDIT = 12;
	public static final int DESK_ACTION_ID_GESTURE_UP = 13;
	public static final int DESK_ACTION_ID_GESTURE_DOWN = 14;
	public static final int DESK_ACTION_ID_DOUBLE_CLICK = 15;
	public static final int DESK_ACTION_ID_PINCH_ACTION_ZOOM = 16;
	public static final int DESK_ACTION_ID_OPEN_MENU = 17;
	public static final int DESK_ACTION_ID_SCREEN_PREVIEW_EDIT = 18;
	public static final int DESK_ACTION_ID_SCREEN_EDIT = 19;
	//用于统计功能表用户行为的统计。
	public static final int FUNC_ACTION_ID_APPLICATION = 1;
	public static final int FUNC_ACTION_ID_IMAGE = 2;
	public static final int FUNC_ACTION_ID_VIDEO = 3;
	//用于桌面主题TAB统计
	public static final int THEME_TAB_ID_CHOICENESS_TAB = 1;
	public static final int THEME_TAB_ID_CHOICENESS_THEME_DETAIL = 2;
	public static final int THEME_TAB_ID_LOCAL_TAB = 3;
	public static final int THEME_TAB_ID_LOCAL_THEME_DETAIL = 4;
	public static final int THEME_TAB_ID_LOCAL_THEME_APPLY = 5;
	public static final int THEME_TAB_ID_LOCAL_THEME_SHARE = 6;
	public static final int THEME_TAB_ID_LOCAL_THEME_DELETE = 7;
	public static final int THEME_TAB_ID_LOCAL_THEME_INFO = 8;
	public static final int THEME_TAB_ID_SETTING = 9;
	public static final int THEME_TAB_ID_GET_MORE_THEME = 10;

	public static final int USER_ACTION_DEFAULT = 0;
	public static final int USER_ACTION_ONE = 1;
	public static final int USER_ACTION_TWO = 2;
	public static final int USER_ACTION_THREE = 3;
	public static final int USER_ACTION_FOUR = 4;
	public static final int USER_ACTION_FIVE = 5;
	public static final int USER_ACTION_SIX = 6;
	public static final int USER_ACTION_SEVEN = 7;
	public static final int USER_ACTION_EIGHT = 8;
	public static final int USER_ACTION_NINE = 9;
	public static final int USER_ACTION_TEN = 10;
	public static final int USER_ACTION_ELEVEN = 11;
	public static final int USER_ACTION_TWELVE = 12;
	public static final int USER_ACTION_THIRTEEN = 13;
	public static final int USER_ACTION_FOUTEEN = 14;
	public static final int USER_ACTION_FIFTEEN = 15;
	public static final int USER_ACTION_SIXTEEN = 16;
	public static final int USER_ACTION_SEVENTEEN = 17;
	public static final int USER_ACTION_EIGHTEEN = 18;
	public static final int USER_ACTION_NINETEEN = 19;
	//”GO桌面引导页“推荐GO精品主题
	public static final String GUIDE_PAGE_FOR_GOSTORE = "7";
	public static final String ENTER_GUIDE_PAGE_FOR_GOSTORE = "1";
	public static final String CLICK_BANNER_ENTER_GOSTORE_TOPIC = "2";
	public static final String CLICK_HOME_SCREEN_BUTTON = "3";
	public static final String CLICK_MORE_THEME_BUTTON = "4";
	
	public static final String GUIDE_ENTER_GUIDE_PAGE = "8";
	public static final String GUIDE_ENTER_GUIDE_PAGE_ID = "0";
	private static Object sLocker = new Object();
	/**
	 * 保存数据，只接受int型数值
	 * 
	 * @param key
	 *            键值
	 * @param value
	 *            现值
	 */
	public static void saveIntData(Context context, String key, int value) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_DATA,
				Context.MODE_PRIVATE);
		sp.putInt(key, value);
		sp.commit();
	}

	/**
	 * 保存数据，只接受string型数值，非记录点击记数类的统计数据都用这个
	 * 
	 * @param key
	 *            键值
	 * @param value
	 *            现值
	 */
	public static void saveStringData(Context context, String key, String value) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_DATA,
				Context.MODE_PRIVATE);
		sp.putString(key, value);
		sp.commit();
	}

	/**
	 * 读取数据，返回int型数值，需要计数的才用这个
	 * 
	 * @param key
	 *            键值
	 * @param defValue
	 *            默认值
	 */
	public static int readIntData(Context context, String key, int defValue) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.MENU_COUNT_DATA,
				Context.MODE_PRIVATE);
		return sp.getInt(key, defValue);
	}

	/**
	 * 读取数据，返回string型数值
	 * 
	 * @param key
	 *            键值
	 * @param defValue
	 *            默认值
	 */
	public static String readStringData(Context context, String key, String defValue) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_DATA,
				Context.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}

	// /**
	// * 记数加1,每调用一次key所对应数值加1
	// * @param context
	// * @param key
	// * @param defValue
	// */
	// public static void countIntData(Context context , String key ,int
	// defValue){
	// SharedPreferences sp = context.getSharedPreferences(
	// IPreferencesIds.STATISTIC_DATA, Context.MODE_PRIVATE);
	// int value = sp.getInt(key, defValue);
	// value++;
	// Editor editor = sp.edit();
	// editor.putInt(key,value);
	// editor.commit();
	// }

	/**
	 * 用到清理所有数据
	 * 
	 * @param context
	 */
	public static void clearData(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_DATA,
				Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.MENU_COUNT_DATA, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_HTTP_EXCEPTION_DATE,
				Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.APP_DATA, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.APP_SHOW_DATA, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.ENTRY_COUNT_STAT, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.SEARCH_KEYWORDS_STAT,
				Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.USERECORD_STAT, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.ALL_PURPOSE_STAT, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.DESK_ACTION_DATA, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.APP_FUNC_ACTION_DATA,
				Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.THEME_TAB_STAT_DATA,
				Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.USER_BASE_STAT, Context.MODE_PRIVATE);
		sp.clear();
		
		sp = new PreferencesManager(context, IPreferencesIds.STAT_GUI_TAB, Context.MODE_PRIVATE);
		sp.clear();
		
		sp = new PreferencesManager(context, IPreferencesIds.STAT_GUI_ENTRY, Context.MODE_PRIVATE);
		sp.clear();

		saveRootPreferences(context, ROOT_INFO_NO_ROOT);
	}

	/**
	 * 用于重置数据，部分数据上传后应该清0
	 * 
	 * @param context
	 */
	public static void resetData(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_DATA,
				Context.MODE_PRIVATE);
		sp.putString(IPreferencesIds.BACKUP, "0");
		sp.putString(HIDE_APP, "0");
		sp.commit();

		sp = new PreferencesManager(context, IPreferencesIds.MENU_COUNT_DATA, Context.MODE_PRIVATE);
		sp.clear();
		sp.commit();

		sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_HTTP_EXCEPTION_DATE,
				Context.MODE_PRIVATE);
		sp.clear();
		sp.commit();

		sp = new PreferencesManager(context, IPreferencesIds.APP_DATA, Context.MODE_PRIVATE);
		sp.clear();
		sp.commit();

		sp = new PreferencesManager(context, IPreferencesIds.APP_SHOW_DATA, Context.MODE_PRIVATE);
		sp.clear();
		sp.commit();

		sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_NO_UPLOAD_DATE,
				Context.MODE_PRIVATE);
		sp.clear();
		sp.commit();

		sp = new PreferencesManager(context, IPreferencesIds.ENTRY_COUNT_STAT, Context.MODE_PRIVATE);
		sp.clear();
		sp.commit();

		sp = new PreferencesManager(context, IPreferencesIds.SEARCH_KEYWORDS_STAT,
				Context.MODE_PRIVATE);
		sp.clear();
		sp.commit();

		sp = new PreferencesManager(context, IPreferencesIds.USERECORD_STAT, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.DESK_ACTION_DATA, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.APP_FUNC_ACTION_DATA,
				Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.ALL_PURPOSE_STAT, Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.THEME_TAB_STAT_DATA,
				Context.MODE_PRIVATE);
		sp.clear();

		sp = new PreferencesManager(context, IPreferencesIds.USER_BASE_STAT, Context.MODE_PRIVATE);
		sp.clear();
		
		sp = new PreferencesManager(context, IPreferencesIds.STAT_GUI_TAB, Context.MODE_PRIVATE);
		sp.clear();
		
		sp = new PreferencesManager(context, IPreferencesIds.STAT_GUI_ENTRY, Context.MODE_PRIVATE);
		sp.clear();

		saveRootPreferences(context, ROOT_INFO_NO_ROOT);
	}

	public static void countMenuData(Context context, int menuid) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.MENU_COUNT_DATA,
				Context.MODE_PRIVATE);
		// String keyname = getMenuNameById(menuid);
		int value = sp.getInt("" + menuid, 0);
		value++;
		sp.putInt("" + menuid, value);
		sp.commit();
	}

	public static void countMenuData(Context context, int menuid, int value) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.MENU_COUNT_DATA,
				Context.MODE_PRIVATE);
		sp.putInt("" + menuid, value);
		sp.commit();
	}

	public static void countMenuData(Context context, String keyname) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.MENU_COUNT_DATA,
				Context.MODE_PRIVATE);
		int value = sp.getInt(keyname, 0);
		value++;
		sp.putInt(keyname, value);
		sp.commit();
	}

	public static Map<String, ?> getMenuCountData(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.MENU_COUNT_DATA,
				Context.MODE_PRIVATE);
		return sp.getAll();
	}

	public static Map<String, ?> getUserActionData(String type) {
		PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(), type,
				Context.MODE_PRIVATE);
		return sp.getAll();
	}
	/**
	 * <br>功能简述:统计用户行为的异步方法。
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param actionID
	 * @param action
	 * @param actionType
	 */
	public static void countUserActionData(final int actionID, final int action,
			final String actionType) {
		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					countUserActionDataSync(actionID, action, actionType);
				}

			}) {

			}.start();
		} finally {

		}
	}
	private static void countUserActionDataSync(final int actionID, final int action,
			String actionType) {
		synchronized (sLocker) {
			String key = actionID + Statistics.STATISTICS_DATA_SEPARATE_STRING + action;
			PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(), actionType,
					Context.MODE_PRIVATE);
			int value = sp.getInt(key, 0);
			value++;
			sp.putInt(key, value);
			sp.commit();
		}
	}
	public static void countThemeTabData(final int tabId) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					countThemeTabDataSync(tabId);
				} finally {

				}
			}

		}) {

		}.start();
	}
	private static void countThemeTabDataSync(int tabId) {
		synchronized (sLocker) {
			String key = tabId + Statistics.STATISTICS_DATA_SEPARATE_STRING + 6;
			PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(),
					IPreferencesIds.THEME_TAB_STAT_DATA, Context.MODE_PRIVATE);
			int value = sp.getInt(key, 0);
			value++;
			sp.putInt(key, value);
			sp.commit();
		}
	}

	public static Map<String, ?> getThemeTabData() {
		PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(),
				IPreferencesIds.THEME_TAB_STAT_DATA, Context.MODE_PRIVATE);
		return sp.getAll();
	}
	public static String getMenuNameById(int id) {
		String name = "";
		switch (id) {
			case MENU_ID_ADD :
				name = MENU_KEY_ADD;
				break;
			case MENU_ID_WALLPAPER :
				name = MENU_KEY_WALLPAPER;
				break;
			case MENU_ID_THEMES :
				name = MENU_KEY_THEMES;
				break;
			case MENU_ID_GOSTORE :
				name = MENU_KEY_GOSTORE;
				break;
			case MENU_ID_EDIT :
				name = MENU_KEY_EDIT;
				break;
			case MENU_ID_PREFERENCES :
				name = MENU_KEY_PREFERENCES;
				break;
			case MENU_ID_SETTING :
				name = MENU_KEY_SETTING;
				break;
			case MENU_ID_EFFECT :
				name = MENU_KEY_EFFECT;
				break;
			case MENU_ID_GOLOCKER :
				name = MENU_KEY_GOLOCKER;
				break;
			case MENU_ID_GOWIDGET :
				name = MENU_KEY_GOWIDGET;
				break;
			case MENU_ID_NOTIFICATION :
				name = MENU_KEY_NOTIFICATION;
				break;
			case MENU_ID_UPDATE :
				name = MENU_KEY_UPDATE;
				break;
			case MENU_ID_SHARE :
				name = MENU_KEY_SHARE;
				break;
			case MENU_ID_RATE :
				name = MENU_KEY_RATE;
				break;
			case MENU_ID_FEEDBACK :
				name = MENU_KEY_FEEDBACK;
				break;
			case MENU_ID_RESTART :
				name = MENU_KEY_RESTART;
				break;
			case MENU_ID_STATUSBAR :
				name = MENU_KEY_STATUSBAR;
				break;
			case MENU_ID_LOCKSCREEN :
				name = MENU_KEY_LOCKSCREEN;
				break;

			default :
				break;
		}
		return name;
	}

	// public static String getDeskEffectName(int id){
	// String name = "default";
	// switch (id) {
	// case -1:
	// name = "random";
	// break;
	// case 0:
	// name = "default";
	// break;
	// case 1:
	// name = "cabe_outside";
	// break;
	// case 2:
	// name = "flip_with_wallpaper";
	// break;
	// case 3:
	// name = "bounce";
	// break;
	// case 4:
	// name = "roll";
	// break;
	// case 5:
	// name = "bulldoze";
	// break;
	// case 6:
	// name = "cube_intside";
	// break;
	// case 7:
	// name = "windmill";
	// break;
	// case 8:
	// name = "flip_simple";
	// break;
	// case 9:
	// name = "wave";
	// break;
	// case 11:
	// name = "binary_star";
	// break;
	// case 12:
	// name = "chariot";
	// break;
	// case 13:
	// name = "shutter";
	// break;
	// case 14:
	// name = "chord";
	// break;
	// case 15:
	// name = "cylinder";
	// break;
	// case 16:
	// name = "sphere";
	// break;
	// default:
	// break;
	// }
	// return name;
	// }

	public static String getAppInOutlEffectId(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_DATA,
				Context.MODE_PRIVATE);
		return "" + sp.getInt(IPreferencesIds.APPFUNC_INOUT_EFFECT, 1);
		// int id = sp.getInt(APPFUNC_INOUT_EFFECT, 1);
		// String name = "default";
		// switch (id) {
		// case 0:
		// name = "random";
		// break;
		// case 1:
		// name = "default";
		// break;
		// case 2:
		// name = "windmill";
		// break;
		// case 3:
		// name = "tv";
		// break;
		// case 4:
		// name = "zoom";
		// break;
		// case 5:
		// name = "flip";
		// break;
		// default:
		// break;
		// }
		// return name;
	}

	public static String getAppfuncHorizontalEffectId(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_DATA,
				Context.MODE_PRIVATE);
		// int id = sp.getInt(APPFUNC_HORIZONTAL_EFFECT, 0);
		// String name = "default";
		// switch (id) {
		// case -1:
		// name = "random";
		// break;
		// case 0:
		// name = "default";
		// break;
		// case 1:
		// name = "binary_star";
		// break;
		// case 2:
		// name = "chariot";
		// break;
		// case 3:
		// name = "shutter";
		// break;
		// case 4:
		// name = "chord";
		// break;
		// case 5:
		// name = "cylinder";
		// break;
		// case 6:
		// name = "sphere";
		// break;
		// case 7:
		// name = "cabe_outside";
		// break;
		// case 9:
		// name = "bounce";
		// break;
		// case 10:
		// name = "roll";
		// break;
		// case 11:
		// name = "bulldoze";
		// break;
		// case 12:
		// name = "cube_intside";
		// break;
		// case 13:
		// name = "windmill";
		// break;
		// case 14:
		// name = "flip_simple";
		// break;
		// case 15:
		// name = "wave";
		// break;
		// default:
		// break;
		// }
		// return name;

		return "" + sp.getInt(IPreferencesIds.APPFUNC_HORIZONTAL_EFFECT, 0);
	}

	public static String getAppfuncVerticalEffectId(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.STATISTIC_DATA,
				Context.MODE_PRIVATE);
		// int id = sp.getInt(APPFUNC_VERTICAL_EFFECT, 0);
		// String name = "default";
		// switch (id) {
		//
		// case 0:
		// name = "default";
		// break;
		// case 1:
		// name = "waterfall";
		// break;
		// default:
		// break;
		// }
		// return name;
		return "" + sp.getInt(IPreferencesIds.APPFUNC_VERTICAL_EFFECT, 0);
	}

	// public static String getGestureActionById(int id){
	// String name = "default";
	// switch (id) {
	// case 0:
	// name = "disable";
	// break;
	// case 1:
	// name = "shortcut";
	// break;
	// case 2:
	// name = "show_home_screen";
	// break;
	// case 3:
	// name = "show_home_screen_then_preview";
	// break;
	// case 4:
	// name = "show_preview";
	// break;
	// case 5:
	// name = "show_app_drawer";
	// break;
	// case 6:
	// name = "show_notification";
	// break;
	// case 7:
	// name = "show_hide_status_bar";
	// break;
	// case 8:
	// name = "application";
	// break;
	// case 9:
	// name = "show_hide_dock_bar";
	// break;
	// default:
	// break;
	// }
	// return name;
	// }

	public static void saveHttpExceptionDate(Context context, THttpRequest request, int reason) {
		try {
			PreferencesManager sp = new PreferencesManager(context,
					"statistic_http_exception_date", Context.MODE_PRIVATE);

			StringBuffer statisticsDataStringBuffer = new StringBuffer();

			// 记录错误码
			statisticsDataStringBuffer.append(reason).append(STATISTICS_DATA_SEPARATE_STRING);
			// 记录IP
			statisticsDataStringBuffer.append(request.getUrl().getHost()).append(
					STATISTICS_DATA_SEPARATE_STRING);

			Calendar curDate = Calendar.getInstance();
			curDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			int month = curDate.get(Calendar.MONTH) + 1;
			int day = curDate.get(Calendar.DAY_OF_MONTH);
			int hour = curDate.get(Calendar.HOUR_OF_DAY);
			int minute = curDate.get(Calendar.MINUTE);
			String date = month + "-" + day + "-" + hour + "-" + minute;
			// 记录时间
			statisticsDataStringBuffer.append(date);

			int count = sp.getAll().size();
			count++;

			sp.putString("" + count, statisticsDataStringBuffer.toString());
			sp.commit();
		} catch (Throwable e) {
			// TODO: handle exception
		}
	}

	public static Map<String, ?> getHttpExceptionDate(Context context) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.STATISTIC_HTTP_EXCEPTION_DATE, Context.MODE_PRIVATE);
		return sp.getAll();
	}

	// 更新点击数（点击时开始的统计）
	public static void updateAppClickData(Context context, String appname) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.APP_DATA,
				Context.MODE_PRIVATE);
		try {
			// 读取数据
			String data = sp.getString(appname, "");
			String[] item = null;
			if (data != null && !data.equals("")) {
				item = data.split(";");
			}

			// 更新数据
			Long time = System.currentTimeMillis();

			int clickValue = 0;
			int installValue = 0;

			if (item != null && item.length > 1) {
				clickValue = Integer.valueOf(item[0]);
				installValue = Integer.valueOf(item[1]);
			}
			clickValue++;

			// 组装数据保存
			String newData = clickValue + ";" + installValue + ";" + time;
			sp.putString(appname, newData);
			sp.commit();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	// 更新点击数带类型（点击时开始的统计）
	public static void updateAppClickData(Context context, String appname, int type, String mapId, String id) {
//		//发消息实时上传桌面广告的点击统计数量
//		if (type == AdvertConstants.ADVERT_STATISTICS_TYPE) {
//			AdvertControl.getAdvertControlInstance(context).requestAdvertStatistics();
//		}
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.APP_DATA,
				Context.MODE_PRIVATE);
		try {
			// 读取数据
			String data = sp.getString(appname, "");
			String[] item = null;
			if (data != null && !data.equals("")) {
				item = data.split(";");
			}

			// 更新数据
			Long time = System.currentTimeMillis();

			int clickValue = 0;
			int installValue = 0;

			if (item != null && item.length > 1) {
				clickValue = Integer.valueOf(item[0]);
				installValue = Integer.valueOf(item[1]);
			}
			clickValue++;

			// 组装数据保存
			String newData = clickValue + ";" + installValue + ";" + time + ";" + type + ";" + mapId + ";" + id;
			sp.putString(appname, newData);
			sp.commit();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 更新安装数（安装后的统计）
	public static void updateAppInstallData(Context context, String appname) {
		
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.APP_DATA,
				Context.MODE_PRIVATE);
		try {
			// 读取数据
			String data = sp.getString(appname, "");
			String[] item = null;
			if (data != null && !data.equals("")) {
				item = data.split(";");
			}

			// 更新数据
			Long time = 0L;
			int clickValue = 0;
			int installValue = 0;
			int type = 0;
			String mapId = "0";
			String id = "0";

			if (item != null && item.length > 2) {
				clickValue = Integer.valueOf(item[0]);
				installValue = Integer.valueOf(item[1]);
				time = Long.valueOf(item[2]);
				if (item.length > 3) {
					type = Integer.valueOf(item[3]);
				}
				
				if (item.length > 5) {
					mapId = item[4];
					id = item[5];
				}
				
				
			}

			if (System.currentTimeMillis() - time < 30 * 60 * 1000L) {
				installValue++;
				time = 0L;
				if (type == AdvertConstants.ADVERT_STATISTICS_TYPE) {
					AdvertControl advertControl = AdvertControl.getAdvertControlInstance(context);
					advertControl.requestAdvertStatInstall(appname);
				}
				
				// 组装数据保存
				String newData = clickValue + ";" + installValue + ";" + time + ";" + type + ";" + mapId + ";" + id;
				sp.putString(appname, newData);
				sp.commit();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 获取桌面应用点击及安装统计全部数据
	public static Map<String, ?> getAllAppData(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.APP_DATA,
				Context.MODE_PRIVATE);
		return sp.getAll();
	}

	// 获取桌面应用点击及安装统计个别数据
	public static String getAppData(Context context, String appName) {
		String data = "0;0";
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.APP_DATA,
				Context.MODE_PRIVATE);
		if (sp != null) {
			data = sp.getString(appName, "0;0");
		}
		return data;
	}

	// 更新应用展示数
	public static void updateAppShowData(Context context, String appName) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.APP_SHOW_DATA,
				Context.MODE_PRIVATE);
		try {
			// 读取数据
			int data = sp.getInt(appName, 0);
			data++;

			sp.putInt(appName, data);
			sp.commit();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 获取桌面应用展示数统计全部数据
	public static Map<String, ?> getAllAppShowData(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.APP_SHOW_DATA,
				Context.MODE_PRIVATE);
		return sp.getAll();
	}

	public static void saveNoUploadDate(Context context) {
		try {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.STATISTIC_NO_UPLOAD_DATE, Context.MODE_PRIVATE);

			Calendar curDate = Calendar.getInstance();
			int year = curDate.get(Calendar.YEAR);
			int month = curDate.get(Calendar.MONTH) + 1;
			int day = curDate.get(Calendar.DAY_OF_MONTH);
			String date = year + "-" + month + "-" + day;
			sp.putString(date, "");
			sp.commit();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static Map<String, ?> getNoUploadData(Context context) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.STATISTIC_NO_UPLOAD_DATE, Context.MODE_PRIVATE);
		return sp.getAll();
	}

	/**
	 * root权限信息 0：系统没有root权限，1：系统有root权限，2：拒绝桌面root权限,3：授权桌面root权限
	 */
	public static void saveRootPreferences(Context context, int rootType) {
		PreferencesManager preferences = new PreferencesManager(context,
				IPreferencesIds.APPS_UNINSTALL_GET_ROOT, Context.MODE_WORLD_READABLE);
		preferences.putInt("rootType", rootType);
		preferences.commit();

	}

	public static int getRootPreferences(Context context) {
		PreferencesManager preferences = new PreferencesManager(context,
				IPreferencesIds.APPS_UNINSTALL_GET_ROOT, Context.MODE_WORLD_READABLE);
		return preferences.getInt("rootType", ROOT_INFO_NO_ROOT);
	}

	/**
	 * 使用记录统计（用于统计一个功能或设置是否有使用过）
	 */
	public static void saveUseRecordPreferences(Context context, String name) {
		PreferencesManager preferences = new PreferencesManager(context,
				IPreferencesIds.USERECORD_STAT, Context.MODE_WORLD_READABLE);
		preferences.putBoolean(name, true);
		preferences.commit();

	}

	public static boolean getUseRecordPreferences(Context context, String name) {
		PreferencesManager preferences = new PreferencesManager(context,
				IPreferencesIds.USERECORD_STAT, Context.MODE_WORLD_READABLE);
		return preferences.getBoolean(name, false);
	}

	/**
	 * 统计时，获取当前用户的root信息。
	 * 
	 * @param context
	 * @return
	 */
	public static int getRootInfo(Context context) {
		int rootType = getRootPreferences(context);
		if (rootType == ROOT_INFO_NO_ROOT) {
			// 可能用户本身有root权限，但是没有点击批量卸载，此时，默认值是没有root的，故需要再判断一次
			if (CommandManager.findSu()) {
				// 将有root的情况保存到SharedPreferences中，避免下次再重复拿
				// saveRootPreferences(context,ROOT_INFO_HAS_ROOT);
				return ROOT_INFO_HAS_ROOT;
			}
		}
		return rootType;
	}

	/**
	 * 统计两个中心的进入次数（业务ID19)
	 * 
	 * @author zhouxuewen
	 * @param context
	 * @param id
	 *            进入类型（1为应用中心，2为游戏中心）
	 * @param entryId
	 *            入口ID
	 * @param version
	 *            协议版本号
	 */
	public static void saveEntryCount(Context context, int id, int entryId, int version) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.ENTRY_COUNT_STAT,
				Context.MODE_WORLD_READABLE);

		String key = "19||" + id + STATISTICS_DATA_SEPARATE_STRING + entryId
				+ STATISTICS_DATA_SEPARATE_STRING + Statistics.getUid(context)
				+ STATISTICS_DATA_SEPARATE_STRING + version + STATISTICS_DATA_SEPARATE_STRING;

		try {
			// 读取数据
			int data = sp.getInt(key, 0);
			data++;

			sp.putInt(key, data);
			sp.commit();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static Map<String, ?> getEntryCount(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.ENTRY_COUNT_STAT,
				Context.MODE_PRIVATE);
		return sp.getAll();
	}

	/**
	 * 统计搜索关键字（业务ID21)
	 * 
	 * @author zhouxuewen
	 * @param context
	 * @param id
	 *            对象ID【1：应用中心，2：游戏中心，3：go精品，4：功能表搜索】
	 * @param keyWord
	 *            关键字
	 * @param isClick
	 *            是否点击
	 */
	public static void saveSearchKeywordStat(Context context, int id, String keyWord,
			boolean isClick) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.SEARCH_KEYWORDS_STAT, Context.MODE_WORLD_READABLE);

		String key = "21||" + Statistics.getUid(context) + STATISTICS_DATA_SEPARATE_STRING
				+ VERSION + STATISTICS_DATA_SEPARATE_STRING + id + STATISTICS_DATA_SEPARATE_STRING
				+ keyWord + STATISTICS_DATA_SEPARATE_STRING;

		try {
			// 读取数据
			String data = sp.getString(key, "");

			int searchCount = 0;
			int clickCount = 0;

			if (data != null && !data.equals("")) {
				String[] datas = data.split(STATISTICS_DATA_SEPARATE_STRING_ITEM);
				if (datas != null && datas.length > 1) {
					searchCount = Integer.valueOf(datas[0]);
					clickCount = Integer.valueOf(datas[1]);
				}
			}

			if (isClick) {
				clickCount++;
			} else {
				searchCount++;
			}

			data = searchCount + STATISTICS_DATA_SEPARATE_STRING + clickCount;
			sp.putString(key, data);

			sp.commit();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static Map<String, ?> getSearchKeywordStat(Context context) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.SEARCH_KEYWORDS_STAT, Context.MODE_PRIVATE);
		return sp.getAll();
	}

	public static void saveIsOldUser(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.USER_BASE_STAT,
				Context.MODE_PRIVATE);

		sp.putString(KEY_OLD_USER, "1");
		sp.commit();
	}

	public static String getIsOldUser(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.USER_BASE_STAT,
				Context.MODE_PRIVATE);

		return sp.getString(KEY_OLD_USER, "0");
	}

	public static int getDeviceLevel() {
		return ConfigurationInfo.getDeviceLevel();
	}

	public static void countCloudkillingTimes(Context context) {
		PreferencesManager sp = new PreferencesManager(context, GO_SAFE_LAUNCHER,
				Context.MODE_PRIVATE);
		int value = sp.getInt(GO_SAFE_LAUNCHER_CLOUD_KILLING, 0);
		value++;
		sp.putInt(GO_SAFE_LAUNCHER_CLOUD_KILLING, value);
		sp.commit();
	}

	/**
	 * 备份当前语言设置选择的值
	 */
	public static void backUpLanguageSetting(Context context) {
		PreferencesManager sharePreferences = new PreferencesManager(context,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		String currentlanguage = sharePreferences.getString(IPreferencesIds.CURRENTSELETELANGUAGE,
				"");
		sharePreferences.putString(IPreferencesIds.CURRENTSELETELANGUAGEBACK, currentlanguage);
		sharePreferences.commit();
	}

	/**
	 * 恢复当前语言设置选择的值
	 */
	public static void restoreLanguageSetting(Context context) {
		PreferencesManager sharePreferences = new PreferencesManager(context,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		String currentlanguageBack = sharePreferences.getString(
				IPreferencesIds.CURRENTSELETELANGUAGEBACK, "");
		sharePreferences.putString(IPreferencesIds.CURRENTSELETELANGUAGE, currentlanguageBack);
		sharePreferences.commit();
	}

	/**
	 * <br>功能简述:万能统计计数
	 * <br>功能详细描述:相应statKey内容如果为数字，则内容计数加1，如果不为数字，会变成1
	 * <br>注意:确定不要对不清楚的KEY进行计数，否则相对KEY对应值会变为1
	 * @param context
	 * @param statKey
	 */
	public static void countStatData(Context context, String statKey) {
		if (context != null) {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.ALL_PURPOSE_STAT, Context.MODE_PRIVATE);
			int count = 0;
			String value = sp.getString(statKey, "0");
			try {
				count = Integer.valueOf(value);
			} catch (Exception e) {
				// TODO: handle exception
			}
			count++;
			sp.putString(statKey, String.valueOf(count));
			sp.commit();
		}
	}

	/**
	 * <br>功能简述:万能统计的写值
	 * <br>功能详细描述:对应KEY值会变成写入的VALUE值
	 * <br>注意:
	 * @param context
	 * @param statKey
	 * @param statValue
	 */
	public static void saveStatData(Context context, String statKey, String statValue) {
		if (context != null) {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.ALL_PURPOSE_STAT, Context.MODE_PRIVATE);
			sp.putString(statKey, statValue);
			sp.commit();
		}
	}

	/**
	 * <br>功能简述:万能统计的内容读取
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public static String getStatData(Context context) {
		String statisticsData = null;
		if (context != null) {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.ALL_PURPOSE_STAT, Context.MODE_PRIVATE);
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> map = sp.getAll();
			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				String record = String.valueOf(map.get(key));
				if (record != null) {
					statisticsDataBuffer.append("22" + STATISTICS_DATA_SEPARATE_STRING).append(key)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(record)
							.append(Statistics.STATISTICS_DATA_LINEFEED);
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}
	
	/**
	 * GUI Tab点击次数统计
	 * @param context
	 * @param classid
	 */
	public static void saveGuiTabStat(Context context , String classid) {
		if (context != null) {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.STAT_GUI_TAB, Context.MODE_PRIVATE);
			int count = sp.getInt(classid, 0);
			count++;
			sp.putInt(classid, count);
			sp.commit();
		}
	}
	
	public static String getGuiTabData(Context context) {
		String statisticsData = null;
		if (context != null) {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.STAT_GUI_TAB, Context.MODE_PRIVATE);
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> map = sp.getAll();
			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				Object obj = map.get(key);
				if (obj instanceof Integer) {
					int record = (Integer) map.get(key);
					statisticsDataBuffer.append("15||4" + STATISTICS_DATA_SEPARATE_STRING).append(key)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(record)
							.append(Statistics.STATISTICS_DATA_LINEFEED);
					
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}
	
	/**
	 * GUI进入次数统计
	 * @param context
	 */
	public static void saveGuiEntry(Context context) {
		if (context != null) {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.STAT_GUI_ENTRY, Context.MODE_PRIVATE);
			String key = String.valueOf(GuiThemeStatistics.getCurrentEntry(context));
			int count = sp.getInt(key, 0);
			count++;
			sp.putInt(key, count);
			sp.commit();
		}
	}
	
	public static String getGuiEntry(Context context) {
		String statisticsData = null;
		if (context != null) {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.STAT_GUI_ENTRY, Context.MODE_PRIVATE);
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> map = sp.getAll();
			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				Object obj = map.get(key);
				if (obj instanceof Integer) {
					int record = (Integer) map.get(key);
					statisticsDataBuffer.append("15||3" + STATISTICS_DATA_SEPARATE_STRING).append(key)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(record)
							.append(Statistics.STATISTICS_DATA_LINEFEED);
					
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}
	
//	/**
//	 * 保存Treatment
//	 * @param context
//	 * @param pkgName 包名
//	 */
//	public static void saveTreatment(Context context, String pkgName, int treatment) {
//		if (context != null) {
//			PreferencesManager sp = new PreferencesManager(context,
//					IPreferencesIds.APPGAME_TREATMENT, Context.MODE_PRIVATE);
//			sp.putInt(pkgName, treatment);
//			sp.commit();
//		}
//	}
//	
//	/**
//	 * 获取Treatment
//	 * @param context
//	 * @param pkgName 包名
//	 */
//	public static int getTreatment(Context context, String pkgName) {
//		int treatment = -1;
//		if (context != null) {
//			PreferencesManager sp = new PreferencesManager(context,
//					IPreferencesIds.APPGAME_TREATMENT, Context.MODE_PRIVATE);
//			treatment = sp.getInt(pkgName, -1);
//		}
//		return treatment;
//	}
//	
//	/**
//	 * 清除Treatment
//	 * @param context
//	 * @param pkgName 包名
//	 */
//	public static void removeTreatment(Context context, String pkgName) {
//		if (context != null) {
//			PreferencesManager sp = new PreferencesManager(context,
//					IPreferencesIds.APPGAME_TREATMENT, Context.MODE_PRIVATE);
//			sp.remove(pkgName);
//			sp.commit();
//		}
//	}
}
