package com.jiubang.ggheart.apps.desks.diy;

/**
 * 用于DIY桌面各个不同层的ID定义接口 层次之间ID不能重复，否则添加图层时会报错
 * 
 * @author yuankai
 * @version 1.0
 */
public interface IDiyFrameIds {
	/**
	 * 屏幕层
	 */
	public final static int SCREEN_FRAME = 1000;

	/**
	 * widget缩放层
	 */
	public final static int SCREEN_WIDGET_EDIT_FRAME = 1002;

	/**
	 * 屏幕预览层
	 */
	public final static int SCREEN_PREVIEW_FRAME = 3000;

	/**
	 * 功能表层
	 */
	public final static int APPFUNC_FRAME = 4000;

	/**
	 * 功能表搜索层
	 */
	public final static int APPFUNC_SEARCH_FRAME = 4001;

	/**
	 * 快捷条层
	 */
	public final static int DOCK_FRAME = 6000;

	/**
	 * 快捷条编辑层
	 */
	public final static int DOCK_EDIT_FRAME = 6100;
	
	/**
	 * Dock条添加图标层
	 */
	public static final int DOCK_ADD_ICON_FRAME = 6200;

	/**
	 * 调度层
	 */
	public final static int SCHEDULE_FRAME = 7000;

	/**
	 * 图标拖动层
	 */
	public final static int DRAG_FRAME = 9000;

	/**
	 * 屏幕预览位置替换层
	 */
	public final static int REPLACE_DRAG_FRAME = 10000;

	/**
	 * AppCore
	 */
	public final static int APPCORE = 11000;

	/**
	 * widget样式选择层
	 */
	public final static int WIDGET_STYLE_CHOOSE = 13000;

	/**
	 * widget主题选择层
	 */
	public final static int WIDGET_THEME_CHOOSE = 14000;

	/**
	 * 桌面UserFolder打开时的显示层
	 */
	public final static int DESK_USER_FOLDER_FRAME = 16000;

	/**
	 * 全屏动画层
	 */
	public final static int ANIMATION_FRAME = 17000;

	/**
	 * 桌面帮助提示层
	 */
	public final static int GUIDE_GL_FRAME = 18000;

	/**
	 * 软件管理员
	 */
	public static final int APP_MANAGER = 19000;

	/**
	 * 软件管理主层
	 */
	public static final int APPS_MANAGEMENT_MAIN_FRAME = 20000;
	/**
	 * 我的软件管理页面
	 */
	public static final int APPS_MANAGEMENT_UNINSTALL_APP_FRAME = 21000;

	/**
	 * 应用更新管理页面
	 */
	public static final int APPS_MANAGEMENT_UPDATE_APP_FRAME = 22000;

	/**
	 * GOStore层
	 */
	public static final int GO_STORE_FRAME = 23000;

	/**
	 * 屏幕编辑底层
	 */
	public final static int SCREEN_EDIT_BOX_FRAME = 24000;
	/**
	 * 图片浏览层
	 */
	public final static int IMAGE_BROWSER_FRAME = 25000;

	/**
	 * 应用推荐页面
	 */
	public static final int APPS_MANAGEMENT_RECOMMENDED_APP_FRAME = 26000;

	/**
	 * 应用中心主页面
	 */
	public static final int APPS_MANAGEMENT_MAIN_VIEW_FRAME = 26001;

	/**
	 * 我的游戏
	 */
	public static final int GAME_CENTER_MY_GAME_FRAME = 26002;

	/**
	 * 分享桌面
	 */
	public static final int SHARE_FRAME = 27000;

	/**
	 * 通讯统计设置
	 */
	public static final int NOTIFICATION_FRAME = 28000;
	
	/**
	 * 罩子层
	 */
	public static final int COVER_FRAME = 29000;

	/**
	 * 截图显示页面
	 */
	public static final int SNAPSHOT_FRAME = 30000;
	/**
	 * 资源管理MediaControler
	 */
	public static final int MEDIA_CONTROLER = 40000;
	
	/**
	 * 3D插件Frame
	 */
	public static final int SHELL_FRAME = 32000;
}
