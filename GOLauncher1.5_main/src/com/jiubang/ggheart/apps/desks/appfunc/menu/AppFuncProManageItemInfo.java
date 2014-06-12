package com.jiubang.ggheart.apps.desks.appfunc.menu;

/**
 * 正在运行菜单项
 * @author yejijiong
 *
 */
public class AppFuncProManageItemInfo extends BaseMenuItemInfo {
	public final static int ACTION_REFRESH = 0;
	public final static int ACTION_LOCK_LIST = 1;
	public final static int ACTION_CHANGES_SHOW_LOCK_PROGRAM = 2;
	
	public AppFuncProManageItemInfo(int actionId, int textId) {
		mActionId = actionId;
		mTextId = textId;
	}
	
	public AppFuncProManageItemInfo(int actionId, String text) {
		mActionId = actionId;
		mText = text;
	}
}
