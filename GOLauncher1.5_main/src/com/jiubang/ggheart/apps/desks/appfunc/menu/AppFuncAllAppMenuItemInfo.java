package com.jiubang.ggheart.apps.desks.appfunc.menu;

/**
 * 功能表菜单bean
 */
public class AppFuncAllAppMenuItemInfo extends BaseMenuItemInfo {
	public final static int ACTION_SORT_ICON = 0;
	public final static int ACTION_CREATE_NEW_FOLDER = 1;
	public final static int ACTION_HIDE_APP = 2;
	public final static int ACTION_APP_CENTER = 3;
	public final static int ACTION_GAME_CENTER = 4;
	public final static int ACTION_APP_SEARCH = 5;
	public final static int ACTION_APPDRAWER_SETTING = 6;
	public final static int ACTION_APP_MANAGEMENT = 7;

	public AppFuncAllAppMenuItemInfo(int actionId, int textId) {
		mActionId = actionId;
		mTextId = textId;
	}
	
	public AppFuncAllAppMenuItemInfo(int actionId, String text) {
		mActionId = actionId;
		mText = text;
	}
}
