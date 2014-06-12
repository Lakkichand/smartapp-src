package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

public class ScreenMissIconBugUtil {
	/**
	 * 内测与发给问题用户打开此项，正式版本关闭
	 */
	public static final boolean DEBUG = false;

	public static final int ERROR_A_SCREEN_GETSCREENITEMS_EXCEPTION = 1;
	public static final int ERROR_CUSTOM_WIDGET_INFO_NULL = 2;
	public static final int ERROR_SERCH_INFLATEEXCEPTION = 3;
	public static final int ERROR_CREATE_CUSTOM_INFO_NULL = 4;
	public static final int ERROR_CREATE_CUSTOM_NULL = 5;
	public static final int ERROR_CREATEDESKTOPVIEW_APP_INFO_NULL = 6;
	public static final int ERROR_CREATESHORTCUTITEM_APP_INFO_NULL = 7;
	public static final int ERROR_INFLATEBUBBLETEXT_INFLATEEXCEPTION = 8;
	public static final int ERROR_BUBBLE_NULL = 9;
	public static final int ERROR_ADDINSCREEN_CHILD_NULL = 10;
	public static final int ERROR_ADDINSCREEN_SCREEN_SMALLER_0 = 11;
	public static final int ERROR_ADDINSCREEN_SCREEN_BIGGER_0 = 12;
	public static final int ERROR_UNCHECKHM_ITEM_NULL = 13;
	public static final int ERROR_CONVER_ITEMINFO_NULL = 14;
	public static final int ERROR_ADDVIEW_WIDGET_NULL = 15;
	public static final int ERROR_ADDVIEW_APP_SHORTCUT_NULL = 16;

	/**
	 * 调查桌面丢失图标弹框显示
	 * 
	 * @param errorcode
	 */
	public static void showToast(int errorcode) {
		if (DEBUG) {
			GoLauncher.sendMessage(null, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SHOW_LOST_ICON_ERRORCODE, errorcode, null, null);
		} else {
			return;
		}
	}
}
