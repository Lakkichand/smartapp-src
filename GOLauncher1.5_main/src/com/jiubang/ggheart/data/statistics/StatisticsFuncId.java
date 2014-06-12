package com.jiubang.ggheart.data.statistics;

import java.util.HashMap;

/**
 * 统计功能点ID 产品、客户端、服务器端约定
 * 
 * @author masanbing
 * 
 */
public class StatisticsFuncId {
	// ////////////////////////////////////////////////////////////////////////新统计功能用到的功能ID///////////////////////////////////////////////////////////////////////
	// 一级功能ID
	public static final int STATICTISC_LEVEL1_FUNID_GOLAUNCHER = 1; // GO桌面本身
	public static final int STATICTISC_LEVEL1_FUNID_GOWIDGET = 2; // GO Widget
	public static final int STATICTISC_LEVEL1_FUNID_GOSTOREAPP = 3; // GO精品模块
	public static final int STATICTISC_LEVEL1_FUNID_GOLOCKER = 4; // GO锁屏模块
	public static final int STATICTISC_LEVEL1_FUNID_GOALLAPPS = 5; // GO桌面所有apps信息
	public static final int STATICTISC_LEVEL1_FUNID_HTTP_EXCEPTOIN = 6; // GO桌面网络错误信息
	public static final int STATICTISC_LEVEL1_FUNID_USER_ACTION = 7; // 用户行为
	public static final int STATICTISC_LEVEL1_FUNID_FUNTAB_APP_DATA = 10; // 桌面推广应用点击及安装统计
	public static final int STATICTISC_LEVEL1_FUNID_NO_UPLOAD_DATA = 13; // 桌面没上传统计的数据
	public static final int STATICTISC_LEVEL1_FUNID_IS_BIND_GMAIL = 14; // 用户是否绑定gmail账号
	public static final int STATICTISC_LEVEL1_APPMANAGEMENT_TAB_CLICK = 16; // 应用管理TAB点击统计
	public static final int STATICTISC_LEVEL1_GAMEGEMENT_TAB_CLICK = 18; // 游戏中心TAB点击统计
	public static final int STATICTISC_LEVEL1_FUNID_FUNTAB_SEARCH = 24; // 功能表搜索
	public static final int STATICTISC_LEVEL1_FUNID_NEW_APP_STAT = 25; // 新应用中心统计

	// 二级功能ID
	public static final int STATICTISC_LEVEL2_FUNID_DESK_BASE = 1; // 桌面基本信息
	public static final int STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING = 2; // 桌面屏幕设置信息

	// 用户行为2级ID
	public static final int STATICTISC_LEVEL2_FUNID_BASE_USER_ACTION = 1; // 一般行为统计
	public static final int STATICTISC_LEVEL2_FUNID_MEUN_COUNT = 2; // 桌面菜单点击计数
	public static final int STATICTISC_LEVEL2_FUNID_DESK_SETTING = 3; // 桌面设置统计

	// 用户行为3级ID
	public static final int STATICTISC_LEVEL3_FUNID_DESK_EFFECT = 1; // 桌面特效
//	public static final int STATICTISC_LEVEL3_FUNID_APPFUNC_EFFECT = 2; // 功能表特效
	public static final int STATICTISC_LEVEL3_FUNID_GESTURE_ACTION = 3; // 手势设置
	public static final int STATICTISC_LEVEL3_FUNID_BAES_ACTION = 4; // 基础行为统计
	public static final int STATICTISC_LEVEL3_FUNID_TAB_ACTION = 5; // TAB行为统计
	public static final int STATICTISC_LEVEL3_FUNID_AUTO_FIT_ICON = 6; // 自适应ICON统计
	public static final int STATICTISC_LEVEL3_FUNID_DESK_ACTION = 7; // 桌面行为
	public static final int STATICTISC_LEVEL3_FUNID_FUNC_ACTION = 8; // 功能表行为
	public static final int STATICTISC_LEVEL3_FUNID_THEME_TAB = 9; // 桌面主题TAB
	// 用户行为
	// GO LAUNCHER三级功能ID
	public static final int STATICTISC_LEVEL3_FUNID_DESK_SHOWPATTEM_SETTING = 1; // 桌面程序名
	public static final int STATICTISC_LEVEL3_FUNID_DESK_ROWCOLUMN_SETTING = 2; // 桌面行列数
	public static final int STATICTISC_LEVEL3_FUNID_DESK_SHOWSTATUSBAR_SETTING = 3; // 桌面状态栏
	public static final int STATICTISC_LEVEL3_FUNID_DISPLAY_WALLPAPERSCROLL_SETTING = 4; // 壁纸滚动
	public static final int STATICTISC_LEVEL3_FUNID_DISPLAY_SCREENLOOPING_SETTING = 5; // 屏幕循环切换
	public static final int STATICTISC_LEVEL3_FUNID_DISPLAY_TRANSITION_SETTING = 6; // 屏幕切换效果
	public static final int STATICTISC_LEVEL3_FUNID_DISPLAY_ENABLEINDICATOR_SETTING = 7; // 页面指示器
	public static final int STATICTISC_LEVEL3_FUNID_DISPLAY_AUTOHIDEINDICATOR_SETTING = 8; // 指示器自动隐藏
	public static final int STATICTISC_LEVEL3_FUNID_DISPLAY_INDICATORSHOWMODE_SETTING = 9; // 指示器模式
	public static final int STATICTISC_LEVEL3_FUNID_DOCK_ROW_SETTING = 10; // 快捷条行数
	public static final int STATICTISC_LEVEL3_FUNID_DOCK_REVOLVE_SETTING = 11; // 快捷条循环滚动

	public static final int STATICTISC_LEVEL2_FUNID_GOALLAPPS_INFO = 1; // GO桌面所有apps具体信息

	// GO Widget二级功能ID
	public static final HashMap<String, Integer> STATICTISC_LEVEL2_FUNID_WIDGETS = new HashMap<String, Integer>();
	// GO精品二级功能ID
	public static final int STATICTISC_LEVEL2_FUNID_GOSTORE = 1; // GO精品功能点
	public static final int STATICTISC_LEVEL2_FUNID_GOSTORE_WIDGET = 3; // GO精品插件
	// GO锁频二级功能ID
	public static final int STATICTISC_LEVEL2_FUNID_GOLOCKER_MAIN = 1; // GO锁屏主程序
	public static final int STATICTISC_LEVEL2_FUNID_GOLOCKER_THEME = 2; // GO锁屏主题
	public static final int STATICTISC_LEVEL2_FUNID_GOLOCKER_DEFAULT_THEME = 3; // GO锁屏内置主题
	// GO精品三级功能ID
	public static final int STATICTISC_LEVEL3_FUNID_GOSTORE_CONTENT = 2; // GO精品功能点内容统计
	public static final int STATICTISC_LEVEL3_FUNID_GOSTORE_SORT = 3; // GO精品功能点分类统计

	static {
		// 初始化GO Widget二级功能ID对应关系
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.weibowidget",
		// 1);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.taskmanager",
		// 2);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.switchwidget",
		// 3);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.clockwidget",
		// 4);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.calendarwidget",
		// 5);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.bookmark",
		// 6);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.smswidget",
		// 7);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.contactwidget",
		// 8);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.fbwidget",
		// 9);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.twitterwidget",
		// 10);
		// STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.searchwidget",
		// 11);
		// GO精品Widget
		STATICTISC_LEVEL2_FUNID_WIDGETS.put("com.gau.go.launcherex.gowidget.gostore", 2);
	}

}
