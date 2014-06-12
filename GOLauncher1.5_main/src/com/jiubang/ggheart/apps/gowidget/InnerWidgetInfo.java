package com.jiubang.ggheart.apps.gowidget;

public class InnerWidgetInfo {
	// 内置widget的类型：
	/**
	 * 全部内置，包括资源包和代码，例如GoStore widget
	 */
	public final static int BUILDIN_ALL = 0;
	/**
	 * 代码内置，资源包为单独的apk包，如任务管理器widget
	 */
	public final static int BUILDIN_CODE_ONLY = 1;

	public String mTitle;
	public int mIconId;

	public int mPrototype = -1;
	public int mBuildin = -1;
	public String mInflatePkg;
	public String mWidgetPkg;
	public String mThemeConfig;

	// mBuildin = BUILDIN_ALL，需要以下信息
	public String mStatisticPackage;
	public int mPreviewList = -1;
	public int mNameList = -1;
	public int mTypeList = -1;
	public int mLayoutList = -1;
	public int mRowList = -1;
	public int mColumnList = -1;
	public int mMinHeightList = -1;
	public int mMinWidthList = -1;
	public int mConfigList = -1;
	public int mSettingList = -1;
	public String mConfigName;
}