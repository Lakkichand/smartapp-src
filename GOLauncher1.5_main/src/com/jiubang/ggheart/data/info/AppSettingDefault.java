package com.jiubang.ggheart.data.info;

import com.gau.go.launcherex.R;
import com.go.util.device.ConfigurationInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class AppSettingDefault {
	public static int MENUAPPSTYLE = 0;
	public static int TURNSCREENDIRECTION = 1;
	public static int APPNAMEVISIABLE = 1;
	public static int APP_SEARCH_VISIABLE = 0;
	public static int BACKGROUNDPICPATH = 0;
	public static int BACKGROUNDVISIABLE = 3;
	public static int SORTTYPE = 0;
	public static int SHOWNEGLECTAPPS = 1;
	public static int INOUTEFFECT = 1;
	public static int ICONEFFECT = 0;
	public static int SCROLL_LOOP = 1;
	public static int BLUR_BACKGROUND = 0;
	public static int SHOW_TAB_ROW = 1;
	public static int VERTICAL_SCROLL_EFFECT = 0;
	public static int PROUPDATEAPP = 1;
	public static int SELECTINOUTEFFECT = -1;
	public static int SELECTICONEFFECT = -1;
	public static int SELECT_VERTICAL_SCROLL_EFFECT = -1;
	public static int SHOW_HOME_KEY_ONLY = 0;
	public static int SHOW_ACTION_BAR = 1;
	public static int ENABLE_GLIDE_UP_ACTION = 0;
	public static int ENABLE_GLIDE_DOWN_ACTION = 0;

	/**
	 * 功能表默认行列数的样式
	 */
	public static int LINECOLUMNNUM = GOLauncherApp.getContext().getResources()
			.getInteger(R.integer.appfunc_col_row_style);
	/**
	 * 功能表默认行数
	 */
	public static int APPFUNC_ROWNUM = GOLauncherApp.getContext().getResources()
			.getInteger(R.integer.appfunc_default_row);
	/**
	 * 功能表默认列数
	 */
	public static int APPFUNC_COLNUM = GOLauncherApp.getContext().getResources()
			.getInteger(R.integer.appfunc_default_column);
	/**
	 * 功能表是否是否开启动画特效
	 */
	public static boolean APPFUNC_OPEN_EFFECT = ConfigurationInfo.getDeviceLevel() == ConfigurationInfo.LOW_DEVICE
			? false
			: true;
}
