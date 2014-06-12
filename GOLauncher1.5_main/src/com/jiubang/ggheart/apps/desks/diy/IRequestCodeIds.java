package com.jiubang.ggheart.apps.desks.diy;

/**
 * 请求码定义
 * 
 * @author yuankai
 * @version 1.0
 */
public interface IRequestCodeIds {
	/**
	 * 请求主题管理
	 */
	public final static int REQUEST_THEME_SCAN = 0;

	/**
	 * 请求快捷方式
	 */
	public final static int REQUEST_PICK_SHORTCUT = 1;

	/**
	 * 请求widget
	 */
	public final static int REQUEST_PICK_APPWIDGET = 2;

	/**
	 * 请求应用程序
	 */
	public final static int REQUEST_PICK_APPLICATION = 3;

	/**
	 * 请求创建快捷方式
	 */
	public final static int REQUEST_CREATE_SHORTCUT = 4;

	/**
	 * 创建AppWidget
	 */
	public final static int REQUEST_CREATE_APPWIDGET = 5;

	/**
	 * 选取文件夹
	 */
	public final static int REQUEST_PICK_LIVE_FOLDER = 6;

	/**
	 * 创建文件夹
	 */
	public final static int REQUEST_CREATE_LIVE_FOLDER = 7;

	/**
	 * 图片预览返回
	 */
	public final static int REQUEST_PREVIEWIMAGE_FORICON = 8;

	/**
	 * 换图标
	 */
	public final static int REQUEST_THEME_FORICON = 9;

	/**
	 * 创建功能表文件夹
	 */
	public final static int REQUEST_CREATE_APPDRAWER_FOLDER = 10;

	/**
	 * 桌面文件夹编辑
	 */
	public final static int REQUEST_DESKTOP_FOLDER_EDIT = 11;

	/**
	 * 
	 * 编辑功能表文件夹
	 */
	public final static int REQUEST_MODIFY_APPDRAWER_FOLDER = 12;

	/**
	 * 重命名
	 */
	public final static int REQUEST_RENAME = 13;

	/**
	 * DOCK更换程序
	 */
	public final static int REQUEST_DOCK_CHANGE_APP_ICON = 100;

	/**
	 * DOCK更换程序风格图标
	 */
	public final static int REQUEST_DOCK_CHANGE_APP_STYLE_ICON = 101;

	/**
	 * 请求DOCK手势对应应用程序
	 */
	public final static int REQUEST_PICK_APPLICATION_IN_DOCK_GESTURE = 102;

	/**
	 * 请求DOCK手势对应快捷方式
	 */
	public final static int REQUEST_CREAT_SHORTCUT_IN_DOCK_GESTURE = 103;

	/**
	 * DOCK请求手势快捷方式
	 */
	public final static int REQUEST_DOCK_PICK_SHORTCUT_FOR_GESTURE = 104;

	/**
	 * DOCK请求使用默认背景图片
	 */
	public static final int DOCK_DEFAULTE_BG = 105;

	/**
	 * DOCK请求裁剪自定义背景
	 */
	public static final int DOCK_CROP_CUSTOM_BG = 106;

	/**
	 * DOCK请求使用其他主题背景
	 */
	public static final int DOCK_GO_THEME_BG = 107;

	/**
	 * 更换图标，自定义图标
	 */
	public final static int REQUEST_CHANGE_ICON = 200;

	/**
	 * 裁剪图标
	 */
	public final static int REQUEST_CHANGE_CROP_ICON = 201;

	/**
	 * 更换图标，自定义图标
	 */
	public final static int REQUEST_CHANGE_THEMESICON = 202;

	/**
	 * 桌面添加应用程序
	 */
	public final static int REQUEST_SCREEN_ADD_APP = 300;

	/**
	 * 桌面添加GO桌面快捷方式
	 */
	public final static int REQUEST_SCREEN_ADD_GOLAUNCHER_SHORTCUT = 301;

	/**
	 * 操作设置－》ＨＯＭＥ－》应用程序
	 */
	public final static int REQUEST_OPERATION_HOME_OPEN_APP = 400;

	/**
	 * 操作设置－》上滑手势－》应用程序
	 */
	public final static int REQUEST_OPERATION_UP_GESTURE_OPEN_APP = 401;

	/**
	 * 操作设置－》下滑手势－》应用程序
	 */
	public final static int REQUEST_OPERATION_DOWN_GESTURE_OPEN_APP = 402;

	/**
	 * 操作设置－》双击手势－》应用程序
	 */
	public final static int REQUEST_OPERATION_DOUBLE_CLICK_GESTURE_OPEN_APP = 403;

	/**
	 * 功能表设置--》更换背景
	 */
	public final static int REQUEST_OPERATION_SELECT_BACKGROUND = 404;

	/**
	 * DOCK设置--》更换背景
	 */
	public final static int REQUEST_OPERATION_SELECT_DOCK_BACKGROUND = 405;

	/**
	 * 刷新主题预览
	 */
	public final static int REQUEST_THEME_SCAN_VIEW_REFRESH = 500;

	/**
	 * 刷新widget预览
	 */
	public final static int REQUEST_GOWIDGET_UNINSTALL = 600;

	/**
	 * 卸载开关gowidget
	 */
	public final static int REQUEST_GOWIDGET_UNINSTALL_SWITCH = 601;

	/**
	 * 桌面同步 大于1000
	 */
	public final static int REQUEST_MIGRATE_DESK = 1100;
}