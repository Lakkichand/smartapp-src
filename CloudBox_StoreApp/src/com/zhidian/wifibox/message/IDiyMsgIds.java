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
	 * 展示超速提示
	 */
	public static final int SHOW_X_TIP = 1003;
	/**
	 * 移除超速提示
	 */
	public static final int REMOVE_X_TIP = 1004;
	/**
	 * 跳转到超速下载
	 */
	public static final int SHOW_SPEEDING_DOWNLOAD = 1005;
	/**
	 * 更新当前页面
	 */
	public static final int UPDATE_CURRENT_CONTAINER = 1006;
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
	/**
	 * 更改标题
	 */
	public static final int CHANGE_TITLE = 2006;

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

	// -----------------------BEGIN--ActionBar-----------------------------------//
	/**
	 * 在当前导航栏切换页面
	 */
	public static final int JUMP_TITLE = 4001;
	// -----------------------END--ActionBar-----------------------------------//
	// -----------------------BEGIN--AppUninstallActivity-----------------------------------//
	/**
	 * 在当前导航栏切换页面
	 */
	public static final int UPDATE_FREEZE = 5001;
	/**
	 * 更换被恢复的应用
	 */
	public static final int UPDATE_RESTORE = 5002;
	/**
	 * 更新用户应用卸载按钮
	 */
	public static final int UPDATE_UNINSTALL_BTN = 5003;
	// -----------------------END--AppUninstallActivity-----------------------------------//
}
