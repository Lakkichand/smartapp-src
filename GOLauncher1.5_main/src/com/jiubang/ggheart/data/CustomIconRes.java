package com.jiubang.ggheart.data;

import java.util.ArrayList;

public class CustomIconRes {
	public static final String DOCK_DEFAULT_ICON[] = { "shortcut_0_0_phone",
			"shortcut_0_1_contacts", "shortcut_0_2_funclist", "shortcut_0_3_sms",
			"shortcut_0_4_browser", "shortcut_0_application", "shortcut_0_picture",
			"shortcut_0_setting", "shortcut_0_gmail", "shortcut_0_market", "shortcut_0_calendar",
			"shortcut_0_camera", "shortcut_0_music", "shortcut_0_addicon" };

	/**
	 * 初始化系统图标数组
	 * 
	 * @return 系统图标数组
	 */
	public static ArrayList<String> getDefaultResList() {
		ArrayList<String> resourceArray = new ArrayList<String>();

		int size = CustomIconRes.DOCK_DEFAULT_ICON.length;
		for (int i = 0; i < size; i++) {
			resourceArray.add(CustomIconRes.DOCK_DEFAULT_ICON[i]);
		}

		return resourceArray;
	}
}
