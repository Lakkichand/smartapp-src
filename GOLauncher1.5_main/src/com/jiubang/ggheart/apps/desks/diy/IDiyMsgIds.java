package com.jiubang.ggheart.apps.desks.diy;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;

import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.frames.animation.Transition;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SensePreviewFrame;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;

/**
 * Diy桌面消息ID定义
 * 
 * @author yuankai
 * @version 1.0
 */

/**
 * <p>
 * TODO:此文件定义GOlauncher中用到的消息id, 框架相关的消息定义在IFrameworkMsgId内,
 * 合法的消息id必须大于1000，100～999的消息被框架占用。
 * </p>
 * <ul>
 * 这里定义了几个重要的<strong>消息id区间</strong>：
 * <li>框架通用的消息，从{@link #MSG_VALID_ID}开始</li>
 * <li>后台消息，从{@link #APPCORE_DATACHANGE}开始</li>
 * <li>屏幕消息，从{@link #SCREEN_BEGINE_MSG_ID}开始</li>
 * <li>DOCK消息，从{@link #DOCK_BEGIN_MSG_ID}开始</li>
 * <li>功能表消息，从{@link #APPDRAWER_BEGIN_MSG_ID}开始</li>
 * <li>预览消息，从{@link #PREVIEW_BEGIN_MSG_ID}开始</li>
 * <li>拖动层消息，从{@link #DRAG_BEGIN_MSG_ID}开始</li>
 * <li>WIDGET消息（widget选择、皮肤、编辑），从{@link #WIDGET_BEGIN_MSG_ID}开始</li>
 * <li>动画层消息，从{@link #ANIMATION_BEGIN_MSG_ID}开始</li>
 * <li>用户文件夹消息，从{@link #USER_FOLDER_BEGIN_MSG_ID}开始</li>
 * <li>桌面编辑消息，从{@link #SCREENEDIT_SHOW_GOWIDGET_DETAIL}开始</li>
 * <li>程序管理消息，从{@link #APPS_MANAGEMENT_OPERATION_BUTTON}开始</li>
 * <li>罩子层相关消息，从{@link #COVER_FRAME_BEING_MSG_ID}开始</li>
 * </ul>
 */

public interface IDiyMsgIds extends IFrameworkMsgId {
	// GOlauncher 起始消息id
	public final static int MSG_VALID_ID = 1000;

	// ---------------------BEGIN-框架通用消息---------------------------//
	/**
	 * 要求重启桌面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int RESTART_GOLAUNCHER = 1001;

	/**
	 * 要求退出桌面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int EXIT_GOLAUNCHER = 1002;

	/**
	 * 要求退出桌面
	 * 
	 * @param param
	 *            keyCode
	 * @param object
	 *            KeyEvent
	 * @param objects
	 *            null
	 */
	public final static int SEARCH_EVENT_FILTER = 1003;

	/**
	 * 启动Activity
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Intent对象
	 * @param objects
	 *            ArrayList<Rect> 的第一个item表示intent的位置信息，objects可以为空
	 */
	public final static int START_ACTIVITY = 1010;

	/**
	 * 有回调的启动Activity
	 * 
	 * @param param
	 *            requestCode值
	 * @param object
	 *            Intent对象
	 * @param objects
	 *            null
	 */
	public final static int START_ACTIVITY_FOR_RESULT = 1011;

	/**
	 * 获取最近一次down事件的x,y坐标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle {@link #com.jiubang.ggheart.apps.desks.diy.DiyScheduler}
	 * @param objects
	 *            null
	 */
	public final static int LAST_MOTION_POSITION = 1012;

	/**
	 * 显示搜索对话框
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle对象，其中放置的是四个用于搜索的参数key分别是:
	 *            ScreenFrame.FIELD_INITIAL_QUERY
	 *            ,ScreenFrame.FIELD_SELECT_INITIAL_QUERY,
	 *            ScreenFrame.FIELD_SEARCH_DATA,ScreenFrame.FIELD_GLOBAL_SEARCH
	 * @param objects
	 *            null
	 */
	public final static int SHOW_SEARCH_DIALOG = 1013;

	/**
	 * 显示功能表
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SHOW_APP_DRAWER = 1014;

	/**
	 * 广播消息：回到主屏幕,让其他层做相应的处理
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int BACK_TO_MAIN_SCREEN = 1015;

	/**
	 * 设置背景
	 * 
	 * @param param
	 *            y轴上的偏移量
	 * @param object
	 *            Drawable 当前壁纸
	 * @param objects
	 *            null
	 */
	public final static int SET_WALLPAPER_DRAWABLE = 1020;

	/**
	 * 背景层消息：背景需要偏移
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle对象 其中由 {@link WallpaperControler#FIELD_SCREEN_NUMS}
	 *            获取屏幕总数 {@link WallpaperControler#FIELD_SCROLL_RANGE}偏移比率
	 *            {@link WallpaperControler#FIELD_SCROLL_X}X方向偏移
	 * @param objects
	 *            null
	 */
	public final static int UPDATE_WALLPAPER_OFFSET = 1021;

	/**
	 * 背景层消息：设置wallpaper点击
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle对象
	 * @param objects
	 *            null
	 */
	public final static int SEND_WALLPAPER_COMMAND = 1022;

	/**
	 * 发送背景
	 * 
	 * @param param
	 * @param object
	 *            {@link Drawable}
	 * @param objects
	 *            null
	 */
	public final static int SEND_BACKGROUND = 1023;

	/**
	 * 获取背景
	 * 
	 * @param param
	 *            接收背景的handler的id
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int GET_BACKGROUND = 1024;

	/**
	 * 绘制背景
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            Canvas
	 * @param objects
	 *            null
	 */
	public final static int DRAW_BACKGROUND = 1025;

	/**
	 * 锁屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int LOCK_SCREEN = 1030;

	/**
	 * 屏幕解锁
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int UNLOCK_SCREEN = 1031;

	/**
	 * 要求显示/隐藏状态栏
	 * 
	 * @param param
	 *            -1 显示与隐藏相互切换，并写进数据库， -2 设置显示或隐藏，不写进数据库 ， -3 显示与隐藏相互切换，不写进数据库
	 * @param object
	 *            null 状态相反， 不为null时，指定状态,true 为全屏，隐藏状态栏；false 为不全屏，显示状态栏
	 * @param objects
	 *            null
	 */
	public final static int SHOW_HIDE_STATUSBAR = 1040;

	/**
	 * 要求显示状态栏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SHOW_STATUSBAR = 1041;

	/**
	 * 要求隐藏状态栏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int HIDE_STATUSBAR = 1042;

	/**
	 * 要求显示桌面菜单
	 */
	public final static int SHOW_MENU = 1043;
	
	/**
	 * 显示/隐藏桌面菜单
	 * * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SHOW_HIDE_MENU = 1044;

	/**
	 * 编辑层消息：屏幕层要求编辑层弹出操作菜单
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            当前被操作的View,其中有位置信息与应用程序信息
	 * @param objects
	 *            null
	 */
	public final static int QUICKACTION_EVENT = 1050;

	/**
	 * 通讯统计消息
	 * 
	 * @param param
	 *            统计类型 NotificationType
	 * @param object
	 *            统计数目 Integer
	 * @param objects
	 *            null
	 */
	public final static int NOTIFICATION_CHANGED = 1052;

	/**
	 * 桌面主题改变
	 * 
	 * @param param
	 * @param object
	 * @param objects
	 */
	public final static int DESK_THEME_CHANGED = 1053;

	/**
	 * 显示更新对话框
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SHOW_UPDATE_DIALOG = 1054;

	/**
	 * for debug lost icon
	 */
	public final static int SHOW_LOST_ICON_ERRORCODE = 1055;

	/**
	 * 获取默认图标
	 * 
	 * @param param
	 *            widgetId
	 * @param object
	 *            ItemInfo
	 * @param objects
	 *            数组第一个为BitmapDrawable
	 */
	public final static int GET_ORIGIN_ICON = 1060;

	/**
	 * 恢复默认图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int RESET_DEFAULT_ICON = 1061;

	/**
	 * 添加GoWidget
	 * 
	 * @param param
	 *            widgetId
	 * @param object
	 *            Bundle
	 * @param objects
	 *            null
	 */
	public final static int ADD_GO_WIDGET = 1070;

	/**
	 * 删除GoWidget
	 * 
	 * @param param
	 *            widgetId
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DELETE_GO_WIDGET = 1071;

	/**
	 * 应用GoWidget主题
	 * 
	 * @param param
	 *            widgetId
	 * @param object
	 *            Bundle
	 * @param objects
	 *            null
	 */
	public final static int APPLY_GO_WIDGET_THEME = 1072;

	/**
	 * 指示器模式改变
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int INDICATOR_CHANGE_SHOWMODE = 1080;

	/**
	 * 指示器滑动百分数
	 * 
	 * @param param
	 *            percent 0-100
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int INDICATOR_SLIDE_PERCENT = 1081;

	/**
	 * 检测桌面文件夹是否需要删除
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            folderID
	 * @param objects
	 *            null
	 */
	public final static int CHECK_FOLDER_NEED_DELETE = 1082;
	/**
	 * 手机锁屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            folderID
	 * @param objects
	 *            null
	 */
	public final static int ENABLE_KEYGUARD = 1083;
	/**
	 * 提示用户未安装GO开关或版本低需要更新
	 * 
	 * @param param
	 *            0=安装 1=更新
	 * @param object
	 * @param objects
	 *            null
	 */
	public final static int SHOW_DOWNLOAD_GOSWITCHWIDGET_DIALOG = 1084;

	/**
	 * 添加widget
	 */
	public final static int PICK_WIDGET = 1085;

	/**
	 * 重命名
	 * 
	 * @param param
	 *            -1
	 * @param long itemid
	 * @param ArrayList
	 *            <String> name
	 */
	public final static int RENAME = 1086;

	/***
	 * 刷新屏幕网格
	 */
	public final static int CHECK_GRID_STATE = 1087;

	/***
	 * 显示全屏progressBar
	 */
	public final static int SHOW_PROGRESSBAR = 1088;

	/***
	 * 开始屏幕图标飞的动画
	 */
	public final static int SCREEN_AUTO_FLY = 1089;

	/***
	 * 桌面飞图标到文件夹
	 */
	public final static int FLY_APP_TO_FOLDER = 1090;

	/***
	 * 删除文件夹中飞入的图标
	 */
	public final static int SCREEN_DEL_ITEM_FROM_FOLDER = 1091;

	/***
	 * 离开添加新文件夹状态
	 */
	public final static int LEAVE_NEW_FOLDER_STATE = 1092;

	/***
	 * 离开添加新文件夹状态
	 */
	public final static int IN_NEW_FOLDER_STATE = 1093;

	/***
	 * 添加图标到
	 */
	public final static int ADD_ITEM_TO_SCREEN = 1094;

	// /***
	// * 分享操作
	// */
	// public final static int SHARE_HANDLE = 1095;

	/***
	 * 拿分享的图片
	 */
	public final static int GET_SHARE_IMAGE_NUM = 1095;

	/***
	 * 拿分享的图片
	 */
	public final static int GET_SHARE_IMAGE = 1096;

	/***
	 * 是否能拿到壁纸
	 */
	public final static int CAN_GET_WALLPAPER = 1097;

	/***
	 * 获取主屏索引
	 */
	public final static int GET_MAIN_SCREEN_INDEX = 1098;

	/**
	 * 开始导入外部数据库
	 */
	public final static int IMPORT_OTHER_DB = 1099;
	// ---------------------END-框架通用消息---------------------------//

	// ---------------------BEGIN-Appcore后台消息 ---------------------------//
	/**
	 * 后台消息定义，范围为1100～1199 包含AppDataEngine、ThemeManager中的消息定义
	 */

	/**
	 * 后台数据改变
	 */
	public static final int APPCORE_DATACHANGE = 1100;

	/**
	 * 安装程序
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            String pkgName
	 * @param objects
	 *            ArrayList<AppItemInfo>
	 */
	public static final int EVENT_INSTALL_APP = 1101;

	/**
	 * 卸载程序
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            String pkgName
	 * @param objects
	 *            ArrayList<AppItemInfo>
	 */
	public static final int EVENT_UNINSTALL_APP = 1102;

	/**
	 * 安装包（不在AppDrawer显示的应用程序，如主题、GoWidget，Notification）
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            String pkgName
	 * @param objects
	 *            null
	 */
	public static final int EVENT_INSTALL_PACKAGE = 1103;

	/**
	 * 卸载包（不在AppDrawer显示的应用程序，如主题、GoWidget，Notification）
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            String pkgName
	 * @param objects
	 *            null
	 */
	public static final int EVENT_UNINSTALL_PACKAGE = 1104;

	/**
	 * 升级包
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            String pkgName
	 * @param objects
	 *            null
	 */
	public static final int EVENT_UPDATE_PACKAGE = 1105;

	/**
	 * 安装在SD卡上且不出现在appdrawer的包，例如桌面或widget主题包
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<String> package list
	 */
	public static final int EVENT_UPDATE_EXTERNAL_PACKAGES = 1106;

	/**
	 * 初始化加载完成
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int EVENT_LOAD_FINISH = 1110;

	/**
	 * 所有程序标题加载完毕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int EVENT_LOAD_TITLES_FINISH = 1111;

	/**
	 * 所有程序图标加载完毕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int EVENT_LOAD_ICONS_FINISH = 1112;

	/**
	 * SD卡mount（手机连接为充电模式），刷新一下自定义图标
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int EVENT_SD_MOUNT = 1120;

	/**
	 * SD卡选择了磁盘共享模式
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int EVENT_SD_SHARED = 1121;

	/**
	 * sd卡准备好，需要刷新 对应于系统消息{@link Intent#ACTION_EXTERNAL_APPLICATIONS_AVAILABLE}
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<AppItemInfo> 变更的程序列表
	 */
	public static final int EVENT_REFLUSH_SDCARD_IS_OK = 1122;

	/**
	 * 定时扫描时间到，需要刷新
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<AppItemInfo> 变更的程序列表
	 */
	public static final int EVENT_REFLUSH_TIME_IS_UP = 1123;

	/**
	 * 主题改变
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int EVENT_THEME_CHANGED = 1130;

	/**
	 * 主题应用失败
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int EVENT_THEME_APPLY_FAILED = 1131;

	/**
	 * 检验主题图标
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int EVENT_CHECK_THEME_ICON = 1132;

	/**
	 * 刷新主题预览
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int REFRESH_THEME_SCAN_VIEW = 1133;
	/**
	 * 刷新GGMENU
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int REFRESH_GGMENU_THEME = 1134;
	/**
	 * 刷新桌面图标
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int REFRESH_SCREENICON_THEME = 1135;
	/**
	 * 刷新文件夹
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int REFRESH_FOLDER_THEME = 1136;
	/**
	 * 刷新桌面指示器
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int REFRESH_SCREENINDICATOR_THEME = 1137;
	/**
	 * 显示或隐藏图标底座
	 * 
	 * @param param
	 *            1 or 0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int EVENT_SHOW_OR_HIDE_ICON_BASE = 1138;

	public final static int EVENT_CHANGE_WIDGET_THEME = 1139;

	public final static int RESPONSE_GLIDE_UP_DOWN_ACTION = 1140;

	/**
	 * 内购付费成功
	 */
	public final static int THEME_INAPP_PAID_FINISHED = 1141;
	
	/**
	 * 通知开始加载多媒体数据（用于功能表搜索多媒体）
	 */
	public final static int START_LOAD_MEDIA_DATA = 1142;
	
	/**
	 * 多媒体数据加载完成（图片、视频、音乐）
	 */
	public final static int MEDIA_DATA_LOAD_FINISH = 1143;
	
	/**
	 * 通知清空内存中的多媒体数据，销毁FileEngine
	 */
	public final static int DESTROY_FILE_ENGINE = 1144; 
	
	/**
	 * 通知打开多媒体文件（图片、音乐、视频）
	 */
	public final static int OPEN_MEIDA_FILE = 1145;
	/**
	 * 指示器位置改变
	 */
	public final static int INDICATOR_CHANGE_POSITION = 1146;
	/**
	 * 屏幕方向改变
	 */
	public final static int SCREEN_ORIENTATION_CHANGE = 1147;
	/**
	 * 是否显示状态栏改变
	 */
	public final static int SHOW_STATUS_BAR_SHOW_CHANGE = 1148;
	/**
	 * 多媒体插件安装或卸载
	 */
	public final static int MEDIA_PLUGIN_CHANGE = 1149;
	/**
	 * 定位多媒体文件
	 */
	public final static int LOCATE_MEDIA_ITEM = 1150;

	// ---------------------END-Appcore后台消息 ---------------------------//

	// ---------------------BEGINE-Screen消息---------------------------//
	public final static int SCREEN_BEGINE_MSG_ID = 2000;

	/**
	 * 屏幕层消息完成屏幕的加载
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Rect
	 * @param objects
	 */
	public final static int SCREEN_FINISH_LOADING = 2001;

	/**
	 * 屏幕层消息：要求显示主屏幕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SHOW_HOME = 2002;

	/**
	 * 屏幕层消息：桌面上向上滑动
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SLIDE_UP = 2003;

	/**
	 * 屏幕层消息：桌面上下滑动
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SLIDE_DOWN = 2004;

	/**
	 * 屏幕层消息：桌面双指向内滑动
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_PINCH_IN = 2005;

	/**
	 * 屏幕层消息：在空白地方长按事件
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_LONG_CLICK = 2006;

	/**
	 * 屏幕层消息：取消长按事件
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CANCEL_LONG_CLICK = 2007;

	/**
	 * 屏幕层消息：点击图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            触发的view
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CLICK_SHORTCUT = 2008;

	/**
	 * 屏幕层消息：要求显示屏幕预览
	 * 
	 * @param param
	 *            1 表示从设置进入预览，0表示其他途径进入预览
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SHOW_PREVIEW = 2010;

	/**
	 * 屏幕层消息：要求显示主屏幕，如果当前是主屏幕，则显示屏幕预览
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SHOW_MAIN_SCREEN_OR_PREVIEW = 2011;

	/**
	 * 屏幕层消息：更新指示器
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_UPDATE_INDICATOR = 2020;

	/**
	 * 发送屏幕层消息：获取当前屏的空间数
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            int[0]
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_GET_VANCANT_COUNT = 2021;

	/**
	 * 屏幕层消息：判断响应的应用程序是否已经存在于桌面上了
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_GET_SHORTCUT_ISEXIST = 2022;

	/**
	 * 发送屏幕层消息：是否显示操作菜单
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_IS_SHOW_QUICKACTION = 2023;

	/**
	 * 发送屏幕层消息：要求AppwidgetHost生成一个新的appwidget的ID
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle对象,key为"id"的值表示ID，将新的ID通过value返回
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_GET_ALLOCATE_APPWIDGET_ID = 2030;

	/**
	 * 发送屏幕层消息：要求AppwidgetHost删除一个appwidget的ID
	 * 
	 * @param param
	 *            id
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_DEL_APPWIDGET_ID = 2031;

	/**
	 * 发送屏幕层消息：要求跳转到widget所在的屏幕
	 * 
	 * @param param
	 *            widgetId
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_WIDGET_REQUEST_FOCUS = 2032;

	/**
	 * 发送屏幕层消息：通知当前屏所在的gowidget进入显示区域
	 * 
	 * @param param
	 *            int 屏幕索引, -1 表示当前屏幕
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_FIRE_WIDGET_ONENTER = 2033;

	/**
	 * 发送屏幕层消息：通知当前屏所在的gowidget离开显示区域
	 * 
	 * @param param
	 *            int 屏幕索引， -1 表示当前屏幕
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_FIRE_WIDGET_ONLEAVE = 2034;

	/**
	 * 发送屏幕层消息：取消通知gowidget进入显示区域
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CANCEL_WIDGET_ONENTER = 2035;

	/**
	 * 发送屏幕层消息：取消通知gowidget离开显示区域
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CANCEL_WIDGET_ONLEAVE = 2036;

	/**
	 * 发给屏幕层消息：要求添加指定的快捷方式
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ApplicationItemInfo对象
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD_SHORTCUT = 2040;

	/**
	 * 发给屏幕层消息：要求添加指定的应用
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ApplicationItemInfos对象
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD_APPLICATIONS = 2041;

	/**
	 * 发送屏幕层消息：要求屏幕层添加一个appwidget到屏幕上
	 * 
	 * @param param
	 *            从系统获取的ID
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD_APPWIDGET = 2042;

	/**
	 * 发送屏幕层消息：要求屏幕层添加一个空的文件夹到屏幕上
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            UseFolderInfo对象
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD_USER_FOLDER = 2043;

	/**
	 * 发送屏幕层消息：要求屏幕层添加一个激活的文件夹到屏幕上
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            LiveFolderInfo对象
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD_LIVE_FOLDER = 2044;

	/**
	 * 屏幕层消息：在当前屏添加一个搜索框
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_ADD_SEARCH_WIDGET = 2045;

	/**
	 * 发给屏幕层消息：要求添加指定的应用,如果当前屏已满,则选择一个最近的屏添加
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ApplicationItemInfo对象
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_ADD_SHORTCUT_COMPLETE = 2046;

	/**
	 * 发送屏幕层消息：要求屏幕层添加一个空的文件夹到屏幕上
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            UseFolderInfo ArrayLiat对象
	 */
	public final static int SCREEN_ADD_APPDRAWER_FOLDER = 2047;

	/**
	 * 屏幕层消息：要求从屏幕层添加一个新的屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD = 2050;

	/**
	 * 屏幕层消息：要求从屏幕层删除一个屏
	 * 
	 * @param param
	 *            屏索引
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_REMOVE = 2051;

	/**
	 * 屏幕层消息：要求从屏幕层进入指定的屏显示
	 * 
	 * @param param
	 *            屏索引
	 * @param object
	 *            小于-100 表示有拖拽, 大于100表示屏幕跳转时间
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ENTER = 2052;

	/**
	 * 屏幕层消息：要求屏幕层设置指定屏为主屏
	 * 
	 * @param param
	 *            屏索引
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SET_HOME = 2053;

	/**
	 * 屏幕层消息：要求屏幕层设置指定屏为当前屏
	 * 
	 * @param param
	 *            屏索引
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SET_CURRENT = 2054;

	/**
	 * 屏幕层消息：要求屏幕层显示上一屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SHOW_PRESCREEN = 2055;

	/**
	 * 屏幕层消息：要求屏幕层显示下一屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SHOW_NEXTSCREEN = 2056;

	/**
	 * 屏幕层消息：要求刷新屏幕层组件的屏幕索引
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_REFRESH_INDEX = 2057;

	/**
	 * 是否存在垃圾数据, 通过sendMessage的返回值获取结果
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int IS_EXIST_TRASH_DATA = 2060;

	/**
	 * 清理垃圾数据, 通过sendMessage的返回值获取结果
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int CLEAN_TRASH_DATA = 2061;

	/**
	 * 屏幕层消息：文件夹操作事件
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_FOLDER_EVENT = 2070;

	/**
	 * 屏幕层消息：关闭桌面上所有打开的文件夹
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CLOSE_ALL_FOLDERS = 2071;

	/**
	 * 拖动层消息：传递当前屏幕内文件夹的位置
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            -1
	 * @param objects
	 *            ArrayList<Rect>
	 */
	public final static int SCREEN_FOLDER_AREA_LIST = 2072;

	/**
	 * 拖动层消息：更新文件夹图标
	 * 
	 * @param param
	 *            0为关闭， 1为打开
	 * @param object
	 *            Rect
	 * @param objects
	 */
	public final static int SCREEN_FOLDER_ICON_STATE = 2073;

	/**
	 * 发送屏幕层消息：文件夹改变：增加
	 * 
	 * @param param
	 *            folderid
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<>
	 */
	public final static int SCREEN_FOLDER_ADDITEMS = 2074;

	/**
	 * 发送屏幕层消息：文件夹改变：减少
	 * 
	 * @param param
	 *            folderid
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<>
	 */
	public final static int SCREEN_FOLDER_REMOVEITEMS = 2075;

	/**
	 * 发送屏幕层消息：文件夹重命名
	 * 
	 * @param param
	 *            folderid
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<>
	 */
	public final static int SCREEN_FOLDER_RENAME = 2076;

	/**
	 * 屏幕层消息：要求屏幕层异步加载预览图
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ASYN_LOAD_PREVIEW = 2080;

	/**
	 * 屏幕层消息：获取索引所对应的屏幕视图
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            第一个为需要传递的视图
	 */
	public static final int SCREEN_GET_CELLLAYOUT = 2081;

	/**
	 * 屏幕层消息：预览dock拖动到桌面图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            iteminfo
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_DOCKEXCHANGE_PREVIEW = 2082;
	/**
	 * 屏幕层消息：拖拽结束，删除预览桌面图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            iteminfo
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_REMOVE_DOCK_PREVIEW = 2083;

	/**
	 * 屏幕层消息：隐藏指示器
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int HIDE_INDICATOR = 2084;

	/**
	 * 屏幕层消息：显示指示器
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SHOW_INDICATOR = 2085;

	/**
	 * 屏幕层消息：删除屏幕上item,当前操作文件夹为目标文件夹
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            View
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_REMOVE_FOLDER_ITEM = 2086;

	/**
	 * 屏幕层消息：删除屏幕上文件夹View,此消息用于桌面文件夹拖动到DOCK条上， 删除桌面文件夹View,但保存文件夹在DB里的数据
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            View
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_REMOVE_FOLDER_VIEW = 2087;

	/**
	 * 屏幕层消息：屏幕是否load完毕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<Boolean>
	 */
	public static final int SCREEN_ISLOAD_FINISH = 2088;
	/**
	 * 屏幕层消息：重新设置横竖屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<Boolean>
	 */
	public static final int SCREEN_RESET_ORIENTATION = 2089;

	/**
	 * 屏幕层消息：设置横竖屏模式
	 * 
	 * @param param
	 *            要设置的屏幕模式，-1则读取数据库
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_SET_ORIENTATION = 2090;

	/**
	 * 屏幕层消息：屏幕挤压图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Point 要腾出的目标坐标
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_REPLACE_VIEW = 2091;

	/**
	 * 屏幕层消息：一批组件更改位置，要保存数据库
	 * 
	 * @param param
	 *            screenIndex
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<ItemInfo> 要修改的数据
	 */
	public static final int SCREEN_CHANGE_VIEWS_POSITIONS = 2092;

	/**
	 * 屏幕层消息：当前屏所有挤压图标换位置复位
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_RESET_REPLACE = 2093;

	/**
	 * 屏幕层消息：点击GOLauncher快捷方式之“桌面设置”，启动桌面设置Activity
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_PREFERENCES = 2094;

	/**
	 * 屏幕层消息：屏幕滑屏时需再检查是否要显示向导
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_NEED_CHECK_SHOW_SCREEN_EFFECT_GUIDE = 2095;

	/**
	 * 屏幕层消息：询问屏幕层是否在顶层
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null return true:是　false：否
	 */
	public static final int SCREEN_FRAME_IS_TOP = 2096;

	/**
	 * 屏幕层消息：跳转指定gowidget:桌面有添加，跳转那一屏；桌面无添加，跳转gowidget详情界面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            pkg
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_GOTO_GOWIDGET_PAGE = 2097;

	/**
	 * 屏幕层消息：该消息用来在4.0强制刷新celllayout
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_FORCE_REDRAW_CELLLAYOUT = 2098;

	/**
	 * 屏幕层消息：指示器位置重置
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_INDICRATOR_POSITION = 2099;

	/**
	 * 屏幕层消息：长按预览层卡片时记录当前屏和主屏的值
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int GET_HOME_CURRENT = 2100;

	/**
	 * 屏幕层消息：拖拽结束后，通知当前的cellLayout清除投影
	 * 
	 * @param param
	 *            1 表示显示指示器 0 表示不显示
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_CLEAR_OUTLINE_BITMAP = 2101;

	/**
	 * 重命名文件夹
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            inscreenid 类型：long
	 * @param objects
	 *            name 类型：ArrayList<String>
	 */
	public static final int FOLDER_RENAME = 2102;

	/**
	 * 桌面绘制文件夹打开需要的参数
	 * 
	 * @param param
	 *            int clipline 裁剪线
	 * @param object
	 *            view 文件夹view
	 * @param objects
	 *            ArrayList<Rect> list[0]:view相对在celllayout里的position rect
	 */
	public static final int SCREEN_OPEN_FOLDER_DATA = 2103;

	/**
	 * 屏幕层消息：要求从屏幕层添加一个带“+”号的屏
	 * 
	 * @param param
	 *            要添加的屏幕的索引，-1表示添加都最后一屏
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD_BLANK = 2105;

	/**
	 * 屏幕层消息：告诉屏幕将带“+”号的屏幕转化为正常的屏幕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_BLANK_TO_NORMAL = 2106;

	/**
	 * 通知桌面产生桌面缩略图
	 * 
	 * @param
	 * @param
	 * @param objects
	 *            ArrayList<Object> 盛放缩略图
	 */
	public static final int SCREEN_GENERATE_PREVIEW_BMP = 2107;
	/**
	 * 通知桌面产生指定屏幕缩略图
	 * 
	 * @param param
	 *            int 屏幕index
	 * @param
	 * @param objects
	 *            ArrayList<Object> 盛放缩略图
	 */
	public static final int SCREEN_GENERATE_SCREEN_BMP = 2108;

	/**
	 * 重新加载数据
	 */
	public final static int SCREEN_RELOAD_DESK = 2109;

	/**
	 * 屏幕层消息：该消息用来提示GO锁屏切换
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int GO_LOCKER_PRECHANGE = 2110;

	/**
	 * 屏幕层消息：该消息用来提示GO锁屏切换完成
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int GO_LOCKER_CHANGED = 2111;

	/**
	 * 移动桌面文件夹到dock
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            View
	 * @param objects
	 *            null
	 */
	public static final int MOVE_SCREEN_FOLDER_TO_DOCK = 2112;

	/**
	 * 移动桌面shortcut到dock
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            View
	 * @param objects
	 *            null
	 */
	public static final int MOVE_SCREEN_SHORTCUT_TO_DOCK = 2113;

	/**
	 * 由于布局里面其他的元素的可见性发生变化，通知workspace调整布局
	 * 
	 * @param param
	 *            1 == Dock， 2 == 指示器， 3 == 状态栏
	 * @param object
	 *            1 == SHOW, 0 == HIDE
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_REQUEST_LAYOUT = 2114;
	/**
	 * 替换网秦下载图标为网秦应用图标
	 */
	public static final int SCREEN_REPLACE_RECOMMEND_ICON = 2115;

	/**
	 * 打开桌面菜单
	 */
	public static final int OPEN_SCREEN_MENU = 2116;

	/**
	 * 桌面菜单是否显示
	 */
	public final static int SCREEN_MENU_SHOW = 2117;

	/**
	 * 替换文件夹里面的推荐图标为应用图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            应用包名
	 * @param objects
	 *            null
	 */
	public final static int REPLACE_RECOMMAND_ICON_IN_FOLDER = 2118;

	/**
	 * 屏幕层消息：桌面空白处双击
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_DOUBLE_CLICK = 2119;
	/***
	 * 屏幕层消息：桌面空白处双击是否有效
	 */
	public final static int SCREEN_DOUBLE_CLICK_VALID = 2120;

	/**
	 * 桌面图标恢复默认
	 */
	public final static int SCREEN_RESET_DEFAULT = 2121;

	/**
	 * 屏幕层消息：跳转到通讯统计设置界面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_OPEN_NOTIFICATION_SETTING = 2122;

	/**
	 * 屏幕层消息：甩动后通知当前的cellLayout清除投影
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_FLING_TO_CLEAR_OUTLINE_BITMAP = 2123;

	/**
	 * 挤压dock图标到桌面放置
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Iteminfo
	 * @param objects
	 *            null
	 */
	public final static int MOVE_DOCK_ITEM_TO_SCREEN = 2124;

	/**
	 * 挤压dock图标到桌面放置,向screenFrame拿放置位置
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Point center
	 * @param objects
	 *            ArrayList<int[]> 保存网格点
	 */
	public final static int GET_DROP_DOCK_LOCATION = 2125;

	/**
	 * 删除屏幕上指定folderId内指定iteminfo的图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Long folderId
	 * @param objects
	 *            ArrayList<Iteminfo> list:[0]:iteminfo
	 */
	public final static int SCREEN_DELETE_FOLDER_ITEM = 2126;

	/**
	 * 移除下载图标
	 */
	public static final int SCREEN_REMOVE_DOWNLOADICON = 2199;
	/**
	 * 替换Evernote下载图标为Evernote应用图标
	 */
	public static final int SCREEN_REPLACE_EVERNOTE_ICON = 2200;
	
	/**
	 * 隐藏桌面中间层
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_HIDE_MIDDLE_VIEW = 2201;
	
	/**
	 * 显示桌面中间层
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SHOW_MIDDLE_VIEW = 2202;
	
	/**
	 * 移除桌面中间层
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_REMOVE_MIDDLE_VIEW = 2203;
	
	/**
	 * 发送广播给多屏多壁纸应用，通知当前屏幕数与当前屏幕下标
	 */
	public static final int SCREEN_SEND_BROADCASTTO_MULTIPLEWALLPAPER = 2204;
	
	/**
	 * 桌面退出添加界面，恢复正常
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_SMALL_TO_NORMAL = 2205;
	
	/**
	 * 节日版的设置项开关状态发生变化时发生消息
	 * 
	 * @param param
	 *            1 true 0 false
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREEN_HOLIDAY_SWITCHER_STATE_CHANGE = 2206;
	
	/**
	 * 发给屏幕层消息：桌面1、5屏添加广告图标
	 * 
	 * @param param
	 *            插入哪个到哪个屏幕
	 * @param object
	 *            ItemInfo对象
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD_ADVERT_SHORT_CUT = 2207;
	
	/**
	 * 发给屏幕层消息：桌面1、5屏添加广告文件夹
	 * 
	 * @param param
	 *            插入哪个到哪个屏幕
	 * @param object
	 *            ItemInfo对象
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_ADD_ADVERT_FOLDER = 2208;
	
	
	/**
	 * 发给屏幕层消息：判断桌面1、5屏是否可以插入广告图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *           -1
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CAN_ADD_ADVERT_SHORT_CUT = 2209;
	
	/**
	 * 发给屏幕层消息：判断桌面1、5屏第一次请求成功后判断是否对屏幕做过修改
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *           -1
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CAN_CHANGE_ADVERT_SHORT_CUT = 2211;
	
	/**
	 * 发给屏幕层消息：清除15屏广告图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *           -1
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CLEAR_ADVERT_ICON = 2212;
	
	/**
	 * celllayout是否有足够的格子用于甩动
	 * 
	 * @param param
	 * @param object
	 *           -1
	 * @param objects
	 *            null
	 */
	public final static int IS_CELLLAYOUT_HAS_ENOUGHT_VACANT = 2213;	
	/**
	 * 缩放的目标区域是否比widget的最小区域小
	 * 
	 * @param param
	 * @param object
	 *           缩放目标区域
	 * @param objects
	 *            null
	 */
	public final static int IS_WIDGET_EDIT_SIZE_SMALLER_THAN_MIN_SIZE = 2214;
	
	/**
	 * 前一屏或后一屏是否可用
	 * 
	 * @param param -1:前一屏;1:后一屏
	 * @param object
	 *           缩放目标区域
	 * @param objects
	 *            null
	 */
	public final static int IS_PREV_NEXT_SCREEN_AVALIBLE = 2215;
	
	/**
	 * 显示QuickActionMenu
	 * 
	 * @param param -1
	 * @param object
	 *           	要显示quickaction menu 的view
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_SHOW_QUICK_ACTION_MENU = 2216;
	
	/**
	 * 空间不足，移动图标到下一屏失败，返回原位
	 * 
	 * @param param -1
	 * @param object
	 *           	null
	 * @param objects
	 *            null
	 */
	public final static int BACK_TO_ORIGINAL_POSITION = 2217;
	
	/**
	 * 发给屏幕层消息：推荐图标抖动
	 * 
	 * @param param 屏幕数
	 *            
	 * @param object
	 *           -1
	 * @param objects
	 *            null
	 */
	public final static int RECOMMEND_ICON_SHANK = 2218;
	
	/**
	 * 发给屏幕层消息：推荐图标抖动
	 * 
	 * @param param 屏幕数
	 *            
	 * @param object
	 *           -1
	 * @param objects
	 *            null
	 */
	public final static int SET_HOME_SCREEN_ICON_CACHE = 2219;
	
	
	/**
	 * 发给屏幕层消息：判断桌面首屏可以插入广告图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *           -1
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_CAN_ADD_ADVERT_TO_HOME_SCREEN = 2220;
	
	/**
	 * 发给屏幕层消息：应用中心推荐图标安装后添加快捷方式到桌面并进行抖动效果
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *           -1
	 * @param objects
	 *            null
	 */
	public final static int ADD_STORE_RECOMMEND_ICON_AND_SHAKE = 2221;
	
	// ---------------------END-Screen消息---------------------------//

	// ---------------------BEGINE-DOCK消息---------------------------//
	public final static int DOCK_BEGIN_MSG_ID = 3000;

	/**
	 * Dock层消息：Dock条控制是否显示Dock条，如果显示则不显示，如果不显示则显示
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SHOW_HIDE = 3001;

	/**
	 * Dock层消息：Dock条控制转屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_TURN_SCREEN = 3002;

	/**
	 * Dock层消息：Dock条"更换风格图标"对话框信息
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            bundle 风格图标ID载体
	 * @param objects
	 *            null
	 */
	public final static int DOCK_CHANGE_STYLE_APP = 3101;

	/**
	 * Dock层消息： Dock条对话框点击选项“快捷方式”，进入快捷方式选择界面
	 * 
	 * @param param
	 *            -1：非自适应模式 - 有GO快捷方式
	 *            1：自适应模式 - 没有GO快捷方式
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ENTER_SHORTCUT_SELECT = 3102;

	/**
	 * 发给DOCK层消息：要求添加指定的应用
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ApplicationItemInfo对象
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ADD_APPLICATION = 3103;

	/**
	 * 发给DOCK层消息：要求添加指定的快捷方式
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ApplicationItemInfo对象
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ADD_SHORTCUT = 3104;

	/**
	 * 发给DOCK层消息：要求添加指定的应用到DOCK的手势识别
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ApplicationItemInfo对象
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ADD_APPLICATION_GESTURE = 3105;

	/**
	 * Dock层消息： Dock条对话框点击选项“快捷方式”，进入快捷方式选择界面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ENTER_SHORTCUT_SELECT_FOR_GESTURE = 3106;

	/**
	 * 发给DOCK层消息：要求添加指定的手势快捷方式
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ApplicationItemInfo对象
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ADD_SHORTCUT_FOR_GESTURE = 3107;

	/**
	 * 发给DOCK层消息：要求当前操作图标更换程序默认图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	// public final static int DOCK_RESET_CURRENT_ICON_DEFAULT_ICON = 3108;

	/**
	 * 发给DOCK层消息：显示DOCK
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SHOW = 3109;

	/**
	 * 发给DOCK层消息：隐藏DOCK
	 * 
	 * @param param
	 *            -1 是否做动画
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_HIDE = 3110;

	/**
	 * 发给DOCK层消息：DOCK设置更改了
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SETTING_CHANGED = 3111;

	/**
	 * 发给DOCK层消息：DOCK风格更改了
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            style 风格字符串
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SETTING_CHANGED_STYLE = 3112;

	/**
	 * 是否存在垃圾数据, 通过sendMessage的返回值获取结果
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int IS_EXIST_DOCK_TRASH_DATA = 3113;

	/**
	 * 清理垃圾数据, 通过sendMessage的返回值获取结果
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int CLEAN_DOCK_TRASH_DATA = 3114;

	/**
	 * 更新ＤＯＣＫ背景图片
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int UPDATE_DOCK_BG = 3115;

	/**
	 * dock弹出垃圾数据提示框
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SHOW_DIRTYDATA_TIPS = 3116;

	/**
	 * dock层消息：卸载了通讯程序
	 * 
	 * @param -1
	 * @object null
	 * @objects ArrayList<Integer> list 保存卸载的通讯程序在dock中的下标索引集合
	 */
	public static int DOCK_APP_UNINSTALL_NOTIFICATION = 3117;

	/**
	 * 发给DOCK层消息：DOCK行数更改了
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            style 风格字符串
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SETTING_CHANGED_ROW = 3118;

	/**
	 * 发给DOCK层消息：DOCK设置要更新
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SETTING_NEED_UPDATE = 3119;

	/**
	 * 发给DOCK层消息：发送一个坐标，dock判断是否在dock响应范围内，如果是，是第几个图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Point
	 * @param objects
	 *            ArrayList<Integer>
	 */
	public final static int DOCK_CHECK_POSITION = 3120;

	/**
	 * 发给DOCK层消息：拖动DOCK图标，移除菜单
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int REMOVE_ACTION_MENU = 3121;

	/**
	 * 获取dock当前打开文件夹图标的layout的位置信息
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Rect
	 * @param objects
	 *            null
	 */
	public final static int GET_DOCK_OPEN_FOLDER_ICON_LAYOUTDATA = 3122;

	/**
	 * 发给DOCK层消息：要求添加文件夹
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            UserFolderInfo对象
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ADD_FOLDER = 3123;

	/**
	 * 发给DOCK层消息：要求删除文件夹内Item
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            UserFolderInfo对象
	 * @param objects
	 *            null
	 */
	public final static int DOCK_DELETE_FOLDERITEM = 3124;

	/**
	 * 从dock发送到向导提示层消息：当前手势操作提示是长按哪个dockiconview触发的 用于手势操作向导的一张图片的显示位置定位
	 * 
	 * @param param
	 *            0~4 第几个icon
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_GUIDE_GESTURE_ICON_INDEX = 3125;

	/**
	 * dock层消息：弹出手势选择框
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SHOW_GESTURE_SELETION = 3126;

	/**
	 * dock层消息：刷新文件夹内容
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int REFRASH_FOLDER_CONTENT = 3127;

	/**
	 * dock层息：图标从DOCK条移动
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ICON_VIEW_MOVE = 3128;

	/**
	 * 删除一个dock图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            long inscreenid
	 * @param objects
	 *            null
	 */
	public final static int DELETE_DOCK_ITEM = 3129;

	/**
	 * dock换页
	 * 
	 * @param param
	 *            edgeType 对应是那个屏幕那个位置边界
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_SNAP_TO_SCREEN = 3130;

	/**
	 * dock卸载程序消息
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            View dragview
	 * @param objects
	 *            null
	 */
	public final static int DOCK_UNINSTALL_APP = 3131;

	/**
	 * dock当前操作的icon
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<DockIconView>
	 */
	public final static int DOCK_CURRENT_ICON = 3132;

	/**
	 * dock自适应提示退出
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_AUTO_FIT_GUIDE_QUITE = 3133;

	/**
	 * 询问dock层是否打开合并文件夹 发送时机：screenFrame在长按完，垃圾桶消失后调用
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_ASK_OPEN_MERGE_FOLDER = 3134;

	/**
	 * dock恢复默认图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DOCK_RESET_DEFAULT = 3135;

	/**
	 * 删除文件夹全部内容
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            long folderid
	 * @param objects
	 *            null
	 */
	public final static int REMOVE_FOLDER_CONTENT = 3136;

	/***
	 * 去拿dock条当前的图片
	 */
	public final static int DOCK_GET_VIEW_IMAGE = 3137;

	/**
	 * 释放背景底座图片
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int CLEAR_BG = 3137;
	
	/**
	 * 自适应模式dock行批量添加程序
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<AppItemInfo> addItemInfos
	 */
	public final static int ADD_APPS_TO_DOCK_FIT = 3138;
	
	/**
	 * dock条长按弹出导航对话框初始化
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<AppItemInfo> addItemInfos
	 */
	public final static int DOCK_ADD_ICON_INIT = 3139;
	
	
	/**
	 * dock条长按添加图标-添加一个图标完成
	 * 
	 * @param param 
	 *            -1
	 * @param object
	 *            Point
	 * @param objects
	 *            ArrayList<Integer>
	 */
	public final static int DOCK_ADD_ICON_ADD_FINISH = 3140;
	
	/**
	 * dock条长按添加图标超过5个时DOCK条变红色
	 * 
	 * @param param 是否需要红色背景
	 *            1:true
	 *            -1:false
	 * @param object
	 *            Point
	 * @param objects
	 *            ArrayList<Integer>
	 */
	public final static int DOCK_ADD_ICON_RED_BG = 3141;
	
	/**
	 * dock条长按添加图标-添加一个图标
	 * 
	 * @param param 
	 *            -1:
	 * @param object
	 *            Point
	 * @param objects
	 *            ArrayList<Integer>
	 */
	public final static int DOCK_ADD_ICON_ADD_ONE = 3142;
	
	// ---------------------END-DOCK消息---------------------------//

	// ---------------------BEGIN-功能表消息---------------------------//
	public final static int APPDRAWER_BEGIN_MSG_ID = 4000;
	/**
	 * 发给功能表，tab和底座主题应用改变
	 */
	public final static int APPDRAWER_TAB_HOME_THEME_CHANGE = 4001;
	/*
	 * 发给功能表，指示器主题应用改变
	 */
	public final static int APPDRAWER_INDICATOR_THEME_CHANGE = 4002;
	/*
	 * 发给功能表，当menu消失时移走帮助
	 */
	public final static int APPDRAWER_REMOVE_GGMENU_GUIDE = 4003;

	/*
	 * 发给功能表，检查ggmenu帮助是否存在
	 */
	public final static int APPDRAWER_IS_GGMENU_GUIDE_SHOWING = 4004;

	/*
	 * 发给功能表，进入拖动帮助教程
	 */
	public final static int APPDRAWER_ENTER_DRAG_TUTORIAL = 4005;

	/*
	 * 发给功能表，进入创建文件夹教程
	 */
	public final static int APPDRAWER_ENTER_CREATE_FOLDER_TUTORIAL = 4006;

	/*
	 * 发给功能表，进入隐藏app教程
	 */
	public final static int APPDRAWER_ENTER_HIDE_TUTORIAL = 4007;

	/*
	 * 发给功能表，文件夹主题改变
	 */
	public final static int APPDRAWER_FOLDER_THEME_CHANGE = 4008;

	/*
	 * 功能表长按菜单事件
	 */
	public static final int EVENT_FUNCFRAM_LONGPRESS = 4009;

	/*
	 * 程序列表可更新消息
	 */
	public static final int EVENT_APPS_LIST_UPDATE = 4010;

	/*
	 * 功能表发给屏幕层，向屏幕层添加快捷方式或文件夹
	 */
	public final static int APPDRAWER_ADD_ITEM_TO_SCREEN = 4011;

	/*
	 * 由于拖动图标到桌面退出时不重新加载桌面
	 */
	public final static int APPDRAWER_EXIT_FOR_DRAG = 4012;

	/*
	 * 功能表进入文件夹编辑界面
	 */
	public final static int APPDRAWER_ENTER_FOLDER_EDIT_MODE = 4013;
	/*
	 * 功能表退出文件夹编辑界面
	 */
	public final static int APPDRAWER_EXIT_FOLDER_EDIT_MODE = 4014;
	/*
	 * 获取所有程序页面的XBASEGRID对象
	 */
	public final static int APPDRAWER_OBTAIN_ALLAPP_TAB_XBASEGRID = 4015;
	/*
	 * 功能表操作失败
	 */
	public final static int APPDRAWER_OPERATION_FAILED = 4016;
	/**
	 * 显示功能表记载框
	 */
	public final static int APPDRAWER_PROGRESSBAR_SHOW = 4017;
	/**
	 * 隐藏功能表记载框
	 */
	public final static int APPDRAWER_PROGRESSBAR_HIDE = 4018;
	/**
	 * 显示资源默认打开方式设置界面
	 */
	public final static int SHOW_MEDIA_OPEN_SETTING_ACTIVITY = 4019;
	// ---------------------END-功能表消息---------------------------//

	// ---------------------BEGIN-Screen Preview消息---------------------------//
	public final static int PREVIEW_BEGIN_MSG_ID = 5000;

	/**
	 * 屏幕预览层消息：要求显示屏幕预览
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            PreviewMsgBean对象
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_INIT = 5001;

	/**
	 * 屏幕预览层消息：要求屏幕预览显示下一屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            获取上下屏的所有卡片区域信息
	 */
	public static final int PREVIEW_SNAP_NEXT = 5002;

	/**
	 * 屏幕预览层消息：要求屏幕预览显示上一屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            获取上一屏的所有卡片区域信息
	 */
	public static final int PREVIEW_SNAP_PRE = 5003;

	/**
	 * 屏幕预览层消息：要求获取当前屏的所有卡片区域信息
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            Rect对象数组
	 */
	public static final int PREVIEW_GET_CUR_CARDS_RECT = 5004;

	/**
	 * 屏幕预览层消息：要求放大某一个卡片并获取被放大的卡片的位置信息
	 * 
	 * @param param
	 *            当前屏的卡片索引
	 * @param object
	 *            被放大的卡片的Rect对象
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_ENLARGE_CARD = 5005;

	/**
	 * 屏幕预览层消息：要求恢复一个被放大的卡片
	 * 
	 * @param param
	 *            当前屏的卡片索引
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_RESUME_CARD = 5006;

	/**
	 * 屏幕预览层消息：要求隐藏添加卡片
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_HIDE_ADD_CARD = 5007;

	/**
	 * 屏幕预览层消息：要求显示添加卡片
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	// public static final int PREVIEW_SHOW_ADD_CARD = 5008;

	/**
	 * 屏幕预览层消息：注册加载监听工作，当加载完成，则回复完成消息
	 * 
	 * @param param
	 *            监听的层ID
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_REGISTER_LOAD_LISTENER = 5009;

	/**
	 * 屏幕预览层消息：注册加载监听者，当加载完成，则回复完成消息
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_LOAD_COMPLETE = 5010;

	/**
	 * 屏幕预览层消息：反注册加载监听者
	 * 
	 * @param param
	 *            监听的层ID
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_UNREGISTER_LOAD_LISTENER = 5011;

	/**
	 * 屏幕预览层消息：获取当前屏索引所对应的绝对屏索引
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle对象，ScreenPreviewFrame.FIELD_ABS_INDEX获取绝对的索引值
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_GET_ABS_SCREEN_INDEX = 5012;

	/**
	 * 屏幕预览层消息：要求作离开动作
	 * 
	 * @param param
	 *            屏索引
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_LEAVE_ANIMATE = 5013;

	/**
	 * 屏幕预览层消息：要求预览层做位置交换动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle对象
	 * @param objects
	 *            List<Rect>预览层更新当前屏的所有区域
	 */
	public static final int PREVIEW_REPLACE_CARD = 5014;

	/**
	 * 屏幕预览层消息：要求获取一屏总数
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle对象
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_GET_START_SCREEN_WIDTH = 5015;

	/**
	 * 屏幕预览层消息：注册位置替换监听，则回复完成消息
	 * 
	 * @param param
	 *            监听的层ID
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_REGISTER_REPLACE_LISTENER = 5016;

	/**
	 * 屏幕预览层消息：反注册位置替换监听者
	 * 
	 * @param param
	 *            监听的层ID
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_UNREGISTER_REPLACE_LISTENER = 5017;

	/**
	 * 屏幕预览层消息：替换完成消息
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_REPLACE_COMPLETE = 5018;

	/**
	 * 屏幕预览层消息：显示提示
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_MAKE_TIP = 5019;

	/**
	 * 屏幕预览层消息：要求屏幕预览显示下一屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_CAN_SNAP_NEXT = 5020;

	/**
	 * 屏幕预览层消息：要求屏幕预览显示上一屏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_CAN_SNAP_PRE = 5021;

	/**
	 * 屏幕预览层消息：换位后返回
	 * 
	 * @param param
	 *            Drag索引
	 * @param object
	 *            Drag区域
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_REPLACE_CARD_BACK = 5022;

	/**
	 * 屏幕预览层消息：获取屏幕的滚动速度
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle {@link SensePreviewFrame#FIELD_SCROLL_DURATION int}
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_SCROLL_DURATION = 5023;

	/**
	 * 屏幕预览层消息：通知屏幕显示或隐藏
	 * 
	 * @param param
	 *            0 - 隐藏桌面 , 1 - 显示桌面
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_NOTIFY_DESKTOP = 5024;

	/**
	 * 屏幕预览层消息：添加GoWidget到桌面
	 * 
	 * @param param
	 *            gowidget的ID
	 * @param object
	 *            Bundle
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_ADD_GOWIDGET = 5025;

	// /**
	// * 用户帮助层通知预览删除第一屏
	// */
	// public static final int PREVIEW_DELETE_FIRSTSCREEN = 5026;

	/**
	 * 用户帮助层通知预览设置第一屏为主屏
	 */
	public static final int PREVIEW_HOME_FIRSTSCREEN = 5027;
	/**
	 * 用户帮助层通知预览添加一屏
	 */
	public static final int PREVIEW_ADD_SCREEN = 5028;
	/**
	 * 屏幕预览当前屏
	 */
	public static final int PREVIEW_CURRENT_SCREEN_INDEX = 5029;
	/**
	 * 删除一个屏幕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_DELETE_SCREEN = 5030;
	/**
	 * 使CardLayout颜色变成红色
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int CARDLAYOUTTURN_TO_RED = 5031;
	/**
	 * 使CardLayout颜色变成正常颜色
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int CARDLAYOUTTURN_TO_NORMAL = 5032;
	/**
	 * 把mDragView传递给previewdraghandler
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            捉起的view
	 * @param objects
	 *            null
	 */
	public static final int SENDVIEWTOPREVIEW = 5033;
	/**
	 * 使加号变成一个屏幕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int TURNADD_TO_CARD = 5034;
	/**
	 * 使屏幕变成一个加号
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int TURNCARD_TO_ADD = 5035;
	/**
	 * 添加一个屏幕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int ADD_NEW_CARD = 5036;
	/**
	 * 为新加的屏幕New mOccupied
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int NEW_ADD_MOCCUPIED = 5037;
	/**
	 * 是否还可以放置桌面元素
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int IS_SET_CONTENT = 5038;
	/**
	 * 是否为加号的CardLayout
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int IS_ADD_CARD = 5039;
	/**
	 * 设置当前屏幕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SET_CURRENTSCREEN = 5040;
	/**
	 * 重绘屏幕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int REDRAW_SCREEN = 5041;
	/**
	 * 屏数为最小值时，即不删除屏幕
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEW_NOLESS_SCREEN = 5042;
	/**
	 * 离开屏幕层时,显示指示器
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int DISPLAY_INDICATOR = 5043;
	/**
	 * 从屏幕层到预览层标志
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREENTOPREW = 5044;
	/**
	 * 刷新屏幕层
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int REFRESH_SCREENPREW = 5045;
	/**
	 * 隐藏指示器
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int HIDDEN_INDICATOR = 5046;
	/**
	 * 获取足够空间的list
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int GET_ENOUGHSPACELIST = 5047;
	/**
	 * 添加WIDGET到屏幕预览层,空间不够 高亮显示
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int ADD_WIDGET_NOROOM = 5048;
	/**
	 * 添加WIDGET到屏幕预览层,空间不够 正常显示
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int ADD_WIDGET_NOROOM_TONOMAL = 5049;
	/**
	 * 屏幕预览层移动结束后保存数据
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SAVE_SCREEN_DATA = 5050;
	/**
	 * 显示预览层指示器
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int DEPLAY_INDICATOR = 5051;

	/***
	 * 卡片换位动画是否完成
	 */
	public static final int PREVIEW_REPLACE_FINISH = 5052;

	/***
	 * 卡片是否处于静止正常状态
	 */
	public static final int PREVIEW_NORMAL_STATE = 5053;

	/***
	 * 预览页面，按返回键返回操作
	 */
	public static final int PREVIEW_BACK_HADLE = 5054;

	/***
	 * 预览页面，获取主屏索引
	 */
	public static final int PREVIEW_MAIN_SCREEN_INDEX = 5055;

	/***
	 * 预览页面，手势返回主页
	 */
	public static final int PREVIEW_TO_MAIN_SCREEN = 5056;

	/***
	 * 预览页面，手势返回主页（带动画）
	 */
	public static final int PREVIEW_TO_MAIN_SCREEN_ANIMATE = 5057;

	/***
	 * 预览页面，是否正显示
	 */
	public static final int PREVIEW_SHOWING = 5058;

	/***
	 * 预览页面，是否正拖拽卡片
	 */
	public static final int PREVIEW_DRAG_ING = 5059;

	/**
	 * 从预览层到屏幕层标志
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int PREVIEWWTOSCREEN = 5061;

	/***
	 * 设置屏幕方向
	 */
	public static final int SET_ORIENTATION = 5062;
	
	/***
	 * 立即显示指示器
	 */
	public static final int DISPLAY_INDICATOR_IMMEDIATELY = 5063;
	
	// ---------------------END-Screen Preview消息---------------------------//

	// ---------------------BEGIN-DRAG消息---------------------------//
	public final static int DRAG_BEGIN_MSG_ID = 6000;

	/**
	 * 拖动层消息：开始拖动
	 * 
	 * @param param
	 *            需要回调的层ID
	 * @param object
	 *            拖动的view
	 * @param objects
	 *            null
	 */
	public final static int DRAG_START = 6001;

	/**
	 * 拖动层消息：正在拖动图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int DRAG_MOVE = 6002;

	/**
	 * 拖动层消息：结束拖动图标
	 * 
	 * @param param
	 *            屏ID,如果是当前屏，则默认返回-1
	 * @param object
	 *            拖动的view
	 * @param objects
	 *            结束时Rect对象
	 */
	public final static int DRAG_OVER = 6003;

	/**
	 * 拖动层消息：取消拖动图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            拖动的view
	 * @param objects
	 *            null
	 */
	public final static int DRAG_CANCEL = 6004;

	/**
	 * 拖动层消息：从功能表开始拖动
	 * 
	 * @param param
	 *            被拖动的元素type，应用程序或者文件夹
	 * @param object
	 *            AppIconForMsg功能表图标拖动数据结构
	 * @param objects
	 *            null
	 */
	public final static int APPFUNC_DRAG_START = 6005;

	/**
	 * 屏幕预览位置替换层消息：初始化
	 * 
	 * @param param
	 *            屏索引
	 * @param object
	 *            View视图
	 * @param objects
	 *            List<Rect>当前屏的所有卡片的区域信息
	 */
	public static final int REPLACE_DRAG_INIT = 6010;

	/**
	 * 屏幕预览位置替换层消息：替换结束
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int REPLACE_DRAG_OVER = 6011;

	/**
	 * 屏幕预览位置替换层消息：替换取消
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int REPLACE_DRAG_CANCEL = 6012;

	/**
	 * 拖动层消息：获得拖动图标位置
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int REPLACE_GET_DRAG_LOCATION = 6013;

	/**
	 * 卡片没有足够空间时结束拖动
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int FINISH_DRAG_WHEN_NO_ROOM = 6014;
	/**
	 * 屏幕预览位置替换层消息：交换结束
	 * 
	 * @param param
	 * @param object
	 * @param objects
	 */
	public static final int REPLACE_DRAG_FINISH = 6015;

	/**
	 * 拖动层消息：正在拖动图标(上面有个DRAG_MOVE也是拖动图标的消息，但是用于处理第一次拖拽事件，故另起一个用来更新网格的位置)
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            正在拖拽的图标
	 * @param objects
	 *            触点的坐标（x,y）
	 */
	public final static int DRAG_MOVING = 6016;

	/***
	 * 屏幕预览位置替换层消息：按下home键
	 */
	public final static int REPLACE_DRAG_HOME_CLICK = 6017;

	/**
	 * 屏幕预览位置替换层消息：替换结束
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int REPLACE_DRAG_OVER_SYNC = 6018;

	/**
	 * 广播拖动结束，用于通知清除各层缓存
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int DRAG_FINISH_BROADCAST = 6019;

	/**
	 * 通知拖拽层，告之拖拽点坐标发生变化（例如从文件夹里面拖出图标到桌面，这时候状态栏收起）
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            坐标变化值（offsetX, offsetY）
	 */
	public static final int UPDATE_DRAG_POINTS = 6020;

	/***
	 * 是否显示垃圾箱
	 */
	public static final int CHECK_SHOW_TRASH = 6021;

	/**
	 * 初始化时不显示垃圾箱
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int TRASH_GONE = 6022;

	/**
	 * 获取垃圾箱是否隐藏
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null return true:隐藏　false:显示
	 */
	public static final int IS_TRASH_GONE = 6023;

	/**
	 * 通知关闭拖动层
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int CLOSE_SCREEN_DRAG_FRAME = 6024;

	/**
	 * 拖动层消息：开始进行飞行动画
	 * 
	 * @param param
	 *            需要回调的层ID
	 * @param object
	 *            飞行的view
	 * @param objects
	 *            目标位置
	 */
	public final static int START_TO_AUTO_FLY = 6025;

	/**
	 * 设置显示垃圾箱
	 */
	public static final int TRASH_VISIBLE = 6026;

	/**
	 * 拖动层消息：桌面拖拽的相对区域发生变化（现分桌面、文件夹内、dock三个区域）
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            int[0] 拖拽的上一个区域ID（变化前）, int[1] 拖拽的新区域ID（变化后）
	 * @param objects
	 *            null
	 */
	public final static int DRAG_AREA_CHANGE = 6027;

	/***
	 * 延迟刷新widget
	 */
	public final static int WIDGET_DELAY_REFRESH = 6028;
	// ---------------------END-DRAG消息---------------------------//

	// ---------------------BEGIN-WDIGET消息---------------------------//
	public final static int WIDGET_BEGIN_MSG_ID = 7000;

	/**
	 * widget编辑层消息，传递编辑widget需要的参数
	 * {@link com.jiubang.ggheart.apps.desks.diy.frames.screen.WidgetEditFrame}
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle
	 * @param objects
	 *            ArrayList<RectF>
	 */
	public final static int WIDGET_EDIT_FRAME_SETUP = 7001;

	/**
	 * widget编辑层消息，验证Widget缩放的区域是否合法
	 * {@link com.jiubang.ggheart.apps.desks.diy.frames.screen.WidgetEditFrame}
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Rect
	 * @param objects
	 *            null
	 */
	public final static int WIDGET_EDIT_FRAME_VALIDATE_RECT = 7002;

	/**
	 * widget编辑层消息，停止编辑widget
	 * {@link com.jiubang.ggheart.apps.desks.diy.frames.screen.WidgetEditFrame}
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int WIDGET_EDIT_FRAME_STOP_EIDT = 7003;

	/**
	 * widget选择样式传递
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            {@link AppWidgetProviderInfo}
	 * @param objects
	 *            null
	 */
	public final static int WIDGETCHOOSE_PROGRAM = 7100;

	/**
	 * widget皮肤
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            {@link GoWidgetBaseInfo}
	 * @param objects
	 *            null
	 */
	public final static int WIDGETCHOOSE_SKIN = 7200;

	/**
	 * 检查GoWidget详情界面是否要romove,因为卸载Gowidget在OnActivityResult里
	 * 不能分辨是否卸载成功，如果成功，GoWidget详情界面要被romove，返回widget管理主界面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int GOWIDGET_CHECK_GOWIDGETDETAIL_NEED_ROMOVE = 7300;

	/**
	 * 卸载开关gowidget
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int GOWIDGET_UNINSTALL_GOWIDGET_SWITCH = 7301;

	/**
	 * 进入指定gowidget详情界面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            pkgName gowidget包名
	 * @param objects
	 *            null
	 */
	public final static int GOWIDGET_GOTO_SPECIFIC_WIDGTE_DETAIL = 7302;

	/**
	 * 进入gowidget未安装界面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int GOWIDGET_SHOW_NOTINSTALL_LIST = 7303;

	// ---------------------END-WDIGET消息---------------------------//

	// ---------------------BEGIN-AnimationFrame消息---------------------------//
	public final static int ANIMATION_BEGIN_MSG_ID = 8000;

	/**
	 * 设置功能表进入动画的监听者
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            {@link Animation.AnimationListener}
	 * @param objects
	 *            null
	 */
	public final static int SET_APP_DRAWER_TRANSITION_LISTENER = 8001;

	/**
	 * 启动功能表进入动画
	 * 
	 * @param param
	 *            持续时间
	 * @param object
	 *            {@link Transition}
	 * @param objects
	 *            null
	 */
	public final static int START_APP_DRAWER_ENTER_TRANSITION = 8002;

	/**
	 * 启动功能表离开动画
	 * 
	 * @param param
	 *            持续时间
	 * @param object
	 *            {@link Transition}
	 * @param objects
	 *            null
	 */
	public final static int START_APP_DRAWER_LEAVE_TRANSITION = 8003;

	/**
	 * 完成功能表进入动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int FINISH_APP_DRAWER_ENTER_TRANSITION = 8004;

	/**
	 * 完成功能表离开动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int FINISH_APP_DRAWER_LEAVE_TRANSITION = 8005;

	/**
	 * 设置功能表进出动画中桌面的动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            {@link Animation}
	 * @param objects
	 *            null
	 */
	public final static int SET_APP_DRAWER_TRANSITION_DESKTOP_ANIMATION = 8006;

	/**
	 * 设置功能表进出动画中功能表的动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            {@link Animation}
	 * @param objects
	 *            null
	 */
	public final static int SET_APP_DRAWER_TRANSITION_DRAWER_ANIMATION = 8007;

	/**
	 * 设置文件夹进出动画中功能表的动画
	 * 
	 * @param param
	 *            1 or 0
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SET_WORKSPACE_DRAWING_CACHE = 8008;

	/**
	 * 检查动画是否已经完成
	 * 
	 */
	public final static int CHECK_ANIMATION_FINISH = 8009;
	/**
	 * 启动功能表搜索进入动画
	 * 
	 * @param param
	 *            持续时间
	 * @param object
	 *            {@link Transition}
	 * @param objects
	 *            null
	 */
	public final static int START_APP_DRAWER_SEARCH_ENTER_TRANSITION = 8010;

	/**
	 * 启动功能表搜索离开动画
	 * 
	 * @param param
	 *            持续时间
	 * @param object
	 *            {@link Transition}
	 * @param objects
	 *            null
	 */
	public final static int START_APP_DRAWER_SEARCH_LEAVE_TRANSITION = 8011;

	/**
	 * 完成功能表搜索进入动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int FINISH_APP_DRAWER_SEARCH_ENTER_TRANSITION = 8012;

	/**
	 * 完成功能表搜索离开动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int FINISH_APP_DRAWER_SEARCH_LEAVE_TRANSITION = 8013;
	/**
	 * 设置功能表进出动画中搜索层的动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            {@link Animation}
	 * @param objects
	 *            null
	 */
	public final static int SET_APP_DRAWER_SEARCH_TRANSITION_SEARCH_ANIMATION = 8014;

	/**
	 * 显示功能表选项卡
	 */
	public final static int SHOW_TAB_ROW = 8015;
	/**
	 * 隐藏功能表选项卡
	 */
	public final static int HIDE_TAB_ROW = 8016;
	/**
	 * 显示功能表操作栏
	 */
	public final static int SHOW_ACTION_BAR = 8017;
	/**
	 * 隐藏功能表操作栏
	 */
	public final static int HIDE_ACTION_BAR = 8018;
	/**
	 * 启动图片浏览器进入动画
	 * 
	 * @param param
	 *            持续时间
	 * @param object
	 *            {@link Transition}
	 * @param objects
	 *            null
	 */
	public final static int START_IMAGE_BROWSER_ENTER_TRANSITION = 8019;
	/**
	 * 启动图片浏览器离开动画
	 * 
	 * @param param
	 *            持续时间
	 * @param object
	 *            {@link Transition}
	 * @param objects
	 *            null
	 */
	public final static int START_IMAGE_BROWSER_LEAVE_TRANSITION = 8020;
	/**
	 * 完成图片浏览器进入动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int FINISH_IMAGE_BROWSER_ENTER_TRANSITION = 8021;
	/**
	 * 完成图片浏览器离开动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int FINISH_IMAGE_BROWSER_LEAVE_TRANSITION = 8022;
	/**
	 * 设置图片浏览器进出动画中浏览器层的动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            {@link Animation}
	 * @param objects
	 *            null
	 */
	public final static int SET_IMAGE_BROWSER_TRANSITION_BROWSER_ANIMATION = 8023;

	// ---------------------END-AnimationFrame消息---------------------------//

	// ---------------------BEGIN-UserFoder消息---------------------------//
	public final static int USER_FOLDER_BEGIN_MSG_ID = 9000;

	/**
	 * userFolder加载ScreenFolderInfo
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ScreenFolderInfo
	 * @param objects
	 *            null
	 */
	public final static int USER_FOLDER_ADD_INFO = 9001;

	/**
	 * userFolder更新指示器
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Bundle
	 * @param objects
	 *            null
	 */
	public final static int USER_FOLDER_UPDATE_INDICATOR = 9002;

	/**
	 * 移除桌面上的图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ItemInfo
	 * @param objects
	 *            null
	 */
	public final static int REMOVE_DESK_ITEMINFO = 9003;

	/**
	 * 创建桌面文件夹
	 * 
	 * @param param
	 *            0
	 * @param object
	 *            UserFolderInfo
	 * @param objects
	 *            null
	 */
	public final static int CREATE_DESK_USERFOLDER = 9004;

	/**
	 * 文件夹关闭
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int EVENT_FOLDER_CLOSE = 9006;

	/**
	 * 文件夹类型
	 * 
	 * @param param
	 *            type
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int FOLDER_TYPE = 9007;

	/**
	 * Folder层向外发送的消息：folder内容，位置有改变，外部要修改数据库
	 * 
	 * @param param
	 *            1 表示要更新文件夹排序方式， 0 表示不需要
	 * @param object
	 *            ScreenFolderInfo
	 * @param objects
	 *            ArrayList<Iteminfo> contents
	 */
	public static final int FOLDER_REPLACE_INDEX = 9008;

	/**
	 * 发给FOLDRE层消息：发送一个坐标，FOLDER层判断是否在FOLER响应(换位)范围内
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Point
	 * @param objects
	 *            null
	 */
	public static final int FOLDER_CHECK_POSITION = 9009;

	/**
	 * 删除内存中指定shortcutInfo
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ItemInfo
	 * @param objects
	 *            null
	 */
	public static final int DELETE_CACHE_INFO_IN_FOLDER = 9011;

	/**
	 * 交换UserFolderInfo里拖动项与dock项
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            ItemInfo
	 * @param objects
	 *            null
	 */
	public static final int EXCHANGE_CACHE_INFO_IN_FOLDER = 9012;

	/**
	 * 全部刷新folder层显示内容
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int REFRASH_CACHE_FOLDER_CONTENT = 9013;

	/**
	 * 在拖动过程在要求显示垃圾箱
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SHOW_TRASH_DURING_DRAGING = 9014;
	/**
	 * 合并文件夹过程中要求缩放view
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCALE_DRAG_FOR_MERGE_FOLDER = 9015;
	/**
	 * 恢复合并文件夹缩放的图标
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int RESET_DRAG_FOR_MERGE_FOLDER = 9016;

	/**
	 * 询问folder　layout的高度
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            Point x:宽度　y:高度
	 * @param objects
	 *            null
	 */
	public static final int FOLDER_LAYOUT_HEIGHT = 9017;

	/**
	 * folder layout的参数，用于celllayout告诉folder层，以便folder层配合做动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            int[2] int[0]:folderTop int[1]:folderIconBottom
	 * @param objects
	 *            null
	 */
	public static final int FOLDER_LAYOUT_DATA = 9018;

	/**
	 * folder关闭动画结束，通知各层配合
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int FOLDER_CLOSED = 9019;

	/**
	 * 获取当前view的drawingcache
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            ArrayList<Bitmap> list[0]:返回的bmp
	 */
	public static final int GET_CURRENT_VIEW_CACHE_BMP = 9020;

	/**
	 * 从编辑界面按取消返回
	 * 
	 * @param param
	 *            模糊层颜色值
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int BACK_FROM_EDIT = 9021;

	/**
	 * 获取folder的排版参数，由外部传入int[3]
	 * 定义规则:folderLayoutData[0]为文件夹层的mFolderTop,folderLayoutData
	 * [1]为文件夹层的mFolderBottom,folderLayoutData[2]为文件夹层的mClipLine
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            int[3]
	 * @param objects
	 *            null
	 */
	public static final int GET_FOLDER_LAYOUT_DATA = 9022;

	/**
	 * folder层向外 通知，文件夹内一图标需要更新（写进数据库）
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            long folderid
	 * @param objects
	 *            ArrayList<Iteminfo> 要更新的项集
	 */
	public static final int UPDATE_FOLDER_ITEM_INFO = 9023;

	// -----------------------END-UserFoder消息---------------------------//

	// -----------------------BEGIN--屏幕编辑层-----------------------------------//
	/**
	 * 查看widget详情
	 */
	public static final int SCREENEDIT_SHOW_GOWIDGET_DETAIL = 10000;

	/**
	 * 添加快捷方式tab
	 */
	public static final int SCREENEDIT_ADD_TAB_ADD_SHORTCUT = 10001;

	/**
	 * 设置特效tab（预览）
	 */

	public static final int SCREENEDIT_SHOW_TAB_EFFECT_SETTING = 10002;

	/**
	 * 通知添加层添加widget完成
	 */
	public static final int SCREENEDIT_PICK_WIDGET_FININSH = 10003;

	/**
	 * 通知添加层添开始翻页动画
	 */
	public static final int SCREENEDIT_PICK_WIDGET_START_ROTATION = 10004;

	/**
	 * 添加widget空间不足view下移动画
	 */
	public static final int SCREENEDIT_PICK_WIDGET_VIEW_DOWN = 10005;

	/**
	 * 添加widget空间不足view下移后点击恢复到原来状态
	 */
	public static final int SCREENEDIT_PICK_WIDGET_VIEW_UP = 10006;

	/**
	 * 进入桌面编辑状态时，通知添加模块进行进入动画
	 */
	public static final int SCREENEDIT_SHOW_ANIMATION_IN = 10007;

	/**
	 * 进入桌面编辑状态时，通知添加模块进行退出动画
	 */
	public static final int SCREENEDIT_SHOW_ANIMATION_OUT = 10008;

	// /**
	// * 反面的动画与 PICK_WIDGET_START_ROTATION = 10004 对应
	// */
	// public static final int SCREENEDIT_PICK_WIDGET_REVERSE_ROTATION = 10009;

	/**
	 * 通知screenFrame关闭桌面编辑
	 * 
	 * @param param
	 *            1 表示带动画， 否则表示不带动画
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT = 10010;

	/**
	 * 通知screenFrame进入桌面编辑
	 * 
	 * @param param
	 *            1 表示带动画， 否则表示不带动画
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT = 10011;

	/***
	 * 屏幕指示器位置在下方时，通知指示器上移
	 * 
	 * @param param
	 *            1 表示带动画 ，0 表示不带
	 */
	public static final int SCREENEDIT_WORKSPACE_INDICATOR_UP = 10012;

	/***
	 * 屏幕指示器位置在上方时，通知指示器下移
	 * 
	 * @param param
	 *            1 表示带动画 ，0 表示不带
	 */
	public static final int SCREENEDIT_WORKSPACE_INDICATOR_DOWN = 10013;

	/**
	 * 添加GO桌面快捷方式tab
	 */
	public static final int SCREENEDIT_ADD_TAB_ADD_GO_SHORTCUT = 10014;

	/**
	 * 检查壁纸TAB是否需要操作提示
	 */
	public static final int SCREENEDIT_WALLPAPER_TAB_CHECK_GUIDEGL = 10015;

	/***
	 * 屏幕已满时，屏幕背景变红提示
	 */
	public static final int SCREENEDIT_SCREEN_FULL_TIP = 10016;

	/**
	 * 添加GoWidget(在需要设置前，先判断当前屏幕空间)
	 */
	public final static int ADD_GO_WIDGET_BYCONFIG = 10017;

	/**
	 * 添加剩余GoWidget的预览图(上升动画做完后,再进行添加)
	 */
	public final static int ADD_MORE_GO_WIDGET_PICVIEW = 10018;

	/**
	 * 添加前先判断屏幕空间
	 */
	public final static int SCREEN_EDIT_PRE_ADD = 10019;

	/**
	 * 通知添加模块刷新壁纸选项列表
	 */
	public final static int SCREEN_EDIT_UPDATE_WALLPAPER_ITEMS = 10020;
	/**
	 * 添加应用程序
	 */
	public final static int SCREEN_EDIT_ADD_APPS = 10021;
	/**
	 * 添加文件夹
	 */
	public final static int SCREEN_EDIT_ADD_FORLDER = 10022;

	/**
	 * 添加GO快捷方式
	 */
	public final static int SCREEN_EDIT_ADD_GOSHORTCUT = 10023;

	/**
	 * 添加GO小部件
	 */
	public final static int SCREEN_EDIT_ADD_GOWIDGET = 10024;

	/**
	 * 添加GO小部件(切换到info界面)
	 */
	public final static int SCREEN_EDIT_ADD_GOWIDGET_INFO = 10025;

	/**
	 * 添加GO小部件(切换到预览界面)
	 */
	public final static int SCREEN_EDIT_ADD_GOWIDGET_PIC = 10026;

	/**
	 * 通知桌面进行（缩小/放大）动画，注：不包括进入（退出）添加模块的动画
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            destScale 目标的缩放比例
	 * @param objects
	 *            null
	 */
	public static final int SCREENEDIT_SCREEN_ZOOM = 10027;

	/**
	 * 添加GO小部件至桌面
	 * 
	 * @param param
	 *            索引值
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_EDIT_ADD_GOWIDGET_TO_SCREEN = 10028;

	/**
	 * 切换到go小部件tab
	 * 
	 * @param param
	 *            null
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_EDIT_GOTO_GO_WIDGET = 10029;
	
	/**
	 * 刷新添加界面桌面主题列表（激活码主题验证选中效果）
	 * 
	 * @param param
	 *            null
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public final static int SCREEN_EDIT_REFRESH_THEME = 10030;

	/**
	 * 获取添加模块显示模式
	 * 
	 * @param param
	 *            null
	 * @param object
	 *            int[]，int[0]存放了模式代码
	 * @param objects
	 *            null
	 */
	public final static int GET_SCREEN_EDIT_BOX_CONTAINER_MODE = 10031;
	
	/**
	 * 启动指示器上升下移动画
	 * 
	 * @param param
	 * 				移动距离
	 * @param object
	 *            boolean ，true为上升，false为下降
	 * @param objects
	 *            null
	 */
	public final static int START_INDICATOR_ANIMATION_FOR_SCREEN_EDIT = 10032;
	
	// -----------------------END--屏幕编辑层-----------------------------------//

	// -----------------------BEGIN-程序管理消息-----------------------------//
	/**
	 * “全部更新”行为按钮消息
	 */
	public static final int APPS_MANAGEMENT_OPERATION_BUTTON = 11000;

	/**
	 * 等待下载消息
	 */
	public static final int APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD = 11001;
	/**
	 * 开始下载消息
	 */
	public static final int APPS_MANAGEMENT_START_DOWNLOAD = 11002;
	/**
	 * 正在下载消息
	 */
	public static final int APPS_MANAGEMENT_DOWNLOADING = 11003;

	/**
	 * 下载完毕消息
	 */
	public static final int APPS_MANAGEMENT_DOWNLOAD_COMPLETED = 11004;

	/**
	 * 取消下载消息
	 */
	public static final int APPS_MANAGEMENT_DOWNLOAD_CANCELED = 11005;

	/**
	 * 下载失败消息
	 */
	public static final int APPS_MANAGEMENT_DOWNLOAD_FAILED = 11006;

	/**
	 * 通知栏显示更新信息消息
	 */
	public static final int EVENT_APPS_LIST_UPDATE_NOTIFICATION = 11007;

	/**
	 * 通知“软件更新”TAB更新显示信息
	 */
	public static final int APPS_MANAGEMENT_UPDATE_COUNT = 11008;

	/**
	 * 点击“更新全部” 或 “取消全部”按钮
	 */
	public static final int APPCENTER_APPMANAGER_ALL_UPDATE_OR_CANCEL = 11009;
	/**
	 * 通知AppsManageView向任务管理器发送获取应用程序size的广播
	 */
	public static final int APPS_MANAGEMENT_QUERY_APP_SIZE = 13009;

	/**
	 * 通知推荐应用可以更新的应用
	 */
	public static final int APPS_MANAGEMENT_RECOMMENDED_APP = 13010;
	/**
	 * 应用下载或更新完成后，通知安装的消息
	 */
	public static final int APPS_MANAGEMENT_INSTALL_APP = 13011;

	/**
	 * 通知应用管理卸载应用
	 */
	public static final int APPS_MANAGEMENT_UNINSTALL_APP = 13012;

	/**
	 * 显示忽略更新界面
	 */
	public static final int SHOW_NO_PROMPT_UPDATE_VIEW = 13013;

	/**
	 * 移除忽略更新界面
	 */
	public static final int REMOVE_NO_PROMPT_UPDATE_VIEW = 13014;

	/**
	 * 显示批量卸载界面
	 */
	public static final int SHOW_UNINSTALL_APP_VIEW = 13015;

	/**
	 * 移除批量卸载界面
	 */
	public static final int REMOVE_UNINSTALL_APP_VIEW = 13016;

	/**
	 * 显示搜索界面
	 */
	public static final int SHOW_SEARCH_VIEW = 13017;

	/**
	 * 移除搜索界面
	 */
	public static final int REMOVE_SEARCH_VIEW = 13018;
	
	/**
	 * 显示所有应用页面
	 */
	public static final int SHOW_ALL_APPS_VIEW = 13019;
	/**
	 * 移除所有应用页面
	 */
	public static final int REMOVE_ALL_APPS_VIEW = 13020;
	/**
	 * 应用游戏中心，切换tab栏
	 */
	public static final int SKIP_SIDE_TAB = 13021;
	/**
	 * 应用游戏中心，首次进入首层tab界面加载完毕
	 */
	public static final int TOPTAB_VIEW_LOAD_FINISH = 13022;
	/**
	 * 应用游戏中心，联网获取可更新应用数据
	 */
	public static final int REFRESH_UPDATE_DATA = 13023;
	/**
	 * 应用游戏中心，通知主界面当网络可用时刷新界面
	 */
	public static final int REFRESH_WHEN_NETWORK_OK = 13024;
	/**
	 * 应用游戏中心，主页面上展示一个progressbar表示正在后台加载新数据
	 */
	public static final int SHOW_PREVLOAD_PROGRESS = 13025;
	/**
	 * 应用游戏中心，主页面上把progressbar移除表示后台已经加载完新数据
	 */
	public static final int HIDE_PREVLOAD_PROGRESS = 13026;
	/**
	 * 应用游戏中心，展示应用搬家界面
	 */
	public static final int SHOW_APP_MIGRATION_VIEW = 13027;
	/**
	 * 应用游戏中心，移除应用搬家界面
	 */
	public static final int REMOVE_APP_MIGRATION_VIEW = 13028;
	/**
	 * 显示应用游戏中心安装包管理页面
	 */
	public static final int SHOW_PACKAGE_MANAGEMENT_VIEW = 13029;
	/**
	 * 移除应用游戏中心安装包管理页面
	 */
	public static final int REMOVE_PACKAGE_MANAGEMENT_VIEW = 13030;
	/**
	 * 应用游戏中心，通知TabDataManager更新有变化的数据，并如果当前页面数据有变化，刷新当前页面
	 */
	public static final int REFRESH_TOPTAB_DATA = 13031;
	/**
	 * 应用游戏中心，忽略更新通知应用更新界面更新数据bean
	 */
	public static final int SEND_APP_TO_UPDATE_VIEW = 13032;
	/**
	 * 应用游戏中心，忽略更新与可更行应用数据交换
	 */
	public static final int CHANGE_APPLIST_INFO = 13033;
	/**
	 * 应用游戏中心，应用更新-用户点击后实现自动上滑到可见区域
	 */
	public static final int TOP_OF_LISTVIEW = 13034;
	/**
	 * 应用游戏中心，显示搜索按钮
	 */
	public static final int SHOW_SEARCH_BUTTON = 13035;
	/**
	 * 应用游戏中心，导步启动加载首页
	 */
	public static final int LOAD_MAIN_VIEW = 13036;
	/**
	 * 应用游戏中心，统计tab点击
	 */
	public static final int SAVE_TAB_CLICK = 13037;
	
	// -----------------------END-程序管理消息-----------------------------//

	// -----------------------START-图片浏览层消息-----------------------------//

	/**
	 * 接收图片信息。
	 */
	public static final int IMAGE_BROWSER_RECEVIE_IMG_PATH = 14000;
	/**
	 * 删除图片。
	 */
	public static final int IMAGE_BROWSER_DELETE_IMAGE = 14001;

	// -----------------------END-图片浏览层消息-----------------------------//

	// -----------------------START-游戏中心-----------------------------//
	public static final int GAME_CENTER_ADD_GAME = 15000;
	// -----------------------END-游戏中心-----------------------------//

	// -----------------------START-通讯统计-----------------------------//

	/**
	 * 新浪微博登陆失败。
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int SINA_WEIBO_LOGIN_FAIL = 16000;
	/**
	 * facebook登陆失败。
	 * 
	 * 添加GO小部件至桌面
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int FACEBOOK_LOGIN_FAIL = 16001;

	// -----------------------END-通讯统计-----------------------------//
	
	// ---------------------BEGIN-罩子层相关消息---------------------------//
	public static final int COVER_FRAME_BEING_MSG_ID = 17000;
	
	/**
	 * 通知DiyScheduler添加一个view到罩子层
	 * 
	 * @param param
	 *            view的coverID
	 * @param object
	 *            附带参数：包名或者是view对象
	 * @param objects
	 *            null
	 */
	public static final int COVER_FRAME_ADD_VIEW = 17001;
	
	/**
	 * 通知DiyScheduler移除罩子层的一个view
	 * 
	 * @param param
	 *            view的coverID
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int COVER_FRAME_REMOVE_VIEW = 17002;


	/**
	 * 通知DiyScheduler显示罩子层（整个）
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int COVER_FRAME_SHOW_ALL = 17003;
	
	/**
	 * 通知DiyScheduler隐藏罩子层（整个）
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int COVER_FRAME_HIDE_ALL = 17004;
	
	/**
	 * 通知DiyScheduler移除罩子层（整个）
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            null
	 * @param objects
	 *            null
	 */
	public static final int COVER_FRAME_REMOVE_ALL = 17005;
	// ---------------------END-罩子层相关消息---------------------------//
	
	// ---------------------START-分享相关消息---------------------------//
	/**
	 * 设置分享类型
	 * 
	 * @param param
	 *            -1
	 *            {@link ShareFrame.TYPE_SHARE} 分享桌面
	 *            {@link ShareFrame.TYPE_CAPTURE} 分享截图
	 * @param object
	 *            null 
	 * @param objects
	 *            null
	 */
	public static final int SET_SHARE_TYPE = 18001;
	/**
	 * 获取分享类型
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            保存分享类型到整形 
	 * @param objects
	 *            null
	 */
	public static final int GET_SHARE_TYPE = 18002;
	/**
	 * 屏幕截图启动
	 * 
	 * @param param
	 *            -1
	 * @param object
	 *            
	 * @param objects
	 *            null
	 */
	public static final int SNAPSHOT_START = 18003;
	/**
	 * 通知功能表取消缓存
	 * 
	 * @param param
	 *            -1
	 * @param object
	 * 
	 * @param objects
	 *            null
	 */
	public static final int SNAPSHOT_DESTROY_APPFUNCFRAME_CACHE = 18004;
	/**
	 * 通知桌面绘制截屏
	 * 
	 * @param param
	 *            -1
	 * @param object 画布
	 * 
	 * @param objects
	 *            null
	 */
	public static final int SNAPSHOT_DRAW_THROUND_LAUNCHER = 18005;
	// ---------------------END-分享相关消息---------------------------//

	
	// ---------------------START-Home键跳转相应相关消息---------------------------//
	public static final int CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK = 19000;
	// ---------------------END-Home键跳转相应相关消息---------------------------//
	
	// ---------------------START-资源管理相关消息---------------------------//
		public static final int SET_IMAGE_BROWSER_DATA = 20000;
		// ---------------------END-资源管理相关消息---------------------------//
}
