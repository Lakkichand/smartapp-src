package com.zhidian.wifibox.message;

/**
 * 消息传送组件的消息id
 * 
 * @author xiedezhi
 * 
 */
public class IDiyMsgIds {

	// -----------------------BEGIN--MainViewGroup-----------------------------------//
	/**
	 * 展示搜索页
	 */
	public static final int SHOW_SEARCHVIEW = 1001;
	/**
	 * 移除搜索页
	 */
	public static final int REMOVE_SEARCHVIEW = 1002;
	/**
	 * 切换网络模式
	 */
	public static final int SWITCH_MODE = 1003;
	/**
	 * 展示普通模式首次进入的提示页面
	 */
	public static final int SHOW_C_TIPS_PAGE = 1004;
	/**
	 * 展示极速模式首次进入的提示页面
	 */
	public static final int SHOW_X_TIPS_PAGE = 1005;
	/**
	 * 移除普通模式首次进入的提示页面
	 */
	public static final int REMOVE_C_TIPS_PAGE = 1006;
	/**
	 * 移除极速模式首次进入的提示页面
	 */
	public static final int REMOVE_X_TIPS_PAGE = 1007;
	/**
	 * 展示模式切换的提示
	 */
	public static final int SHOW_SWITCH_MODE_TIP = 1008;
	/**
	 * 移除模式切换的提示
	 */
	public static final int REMOVE_SWITCH_MODE_TIP = 1009;
	// -----------------------END--MainViewGroup-----------------------------------//

	// -----------------------BEGIN--TabManageView-----------------------------------//
	/**
	 * 首层切换导航栏
	 */
	public static final int SWITCH_NAVIGATION = 2001;
	/**
	 * 进入下一层级
	 */
	public static final int ENTER_NEXT_LEVEL = 2002;
	/**
	 * 回退上一层级
	 */
	public static final int BACK_ON_ONE_LEVEL = 2003;
	/**
	 * 后台拿到新数据后刷新对应的页面
	 */
	public static final int REFRESH_CONTAINER = 2004;

	// -----------------------END--TabManageView-----------------------------------//

	// -----------------------BEGIN--NavigationBar-----------------------------------//
	/**
	 * 通知导航栏切换
	 */
	public static final int NAV_SWITCH_NAVIGATION = 3001;
	/**
	 * 展示应用更新个数
	 */
	public static final int SHOW_UPDATE_COUNT = 3004;
	// -----------------------END--NavigationBar-----------------------------------//
}
