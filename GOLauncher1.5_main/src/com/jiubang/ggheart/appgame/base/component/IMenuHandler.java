package com.jiubang.ggheart.appgame.base.component;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;

/**
 * 菜单处理函数，目前设计用于应用中心/游戏中心的IContainer的抽象化处理
 * 
 * @author songzhaochun
 * 
 */
public interface IMenuHandler {

	/**
	 * 刷新
	 */
	public static final int MENU_ITEM_FRESH = R.string.appgame_menu_item_refresh;

	/**
	 * 下载管理
	 */
	public static final int MENU_ITEM_DOWNLOAD_MANAGER = R.string.appgame_menu_item_downloadmanager;
	/**
	 * 设置
	 */
	public static final int MENU_ITEM_SETTING = R.string.appgame_menu_item_setting;
	/**
	 * 我的应用专有——应用排序方式
	 */
	public static final int MENU_ITEM_SORT_BY = R.string.appgame_menu_item_sort_app;
	/**
	 * 
	 */
	public static final int MENU_ITEM_BATCH_UNINSTALL = R.string.appgame_menu_item_batch_uninstall;
	
	/**
	 * 意见反馈
	 */
	public static final int MENU_ITEM_FEEDBACK = R.string.appgame_menu_item_feedback;

	/**
	 * 创建弹出菜单时被调用
	 * 
	 * @param menu
	 *            可在此对象上增加子菜单项
	 * @return 处理后返回true，不处理返回false
	 */
	public boolean onPrepareOptionsMenu(AppGameMenu menu);

	/**
	 * 点击菜单时,由activity -> view逐层被调用
	 * 
	 * @param id
	 * @see IMenuHandler
	 * @return 处理后返回true，不处理返回false
	 */
	public boolean onOptionItemSelected(int id);
}
