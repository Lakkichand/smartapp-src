package com.jiubang.ggheart.data.theme.bean;

import java.util.ArrayList;

public class PreviewThemeBean extends ThemeBean {

	private ArrayList<String> mWidgetStyleList; // widget样式集合
	private ArrayList<String> mWidgetPreviewList; // widget预览图集合
	private ArrayList<String> mWidgetTitleList; // widget主题title
	private ArrayList<String> mWidgetThemeTypeList; // widget主题type

	public PreviewThemeBean() {
		mBeanType = THEMEBEAN_TYPE_WIDGET;

		mWidgetStyleList = new ArrayList<String>();
		mWidgetPreviewList = new ArrayList<String>();
		mWidgetTitleList = new ArrayList<String>();
		mWidgetThemeTypeList = new ArrayList<String>();
	}

	public ArrayList<String> getWidgetStyleList() {
		return mWidgetStyleList;
	}

	public ArrayList<String> getWidgetPreviewList() {
		return mWidgetPreviewList;
	}

	public ArrayList<String> getWidgetTitleList() {
		return mWidgetTitleList;
	}

	public ArrayList<String> getWidgetThemeTypeList() {
		return mWidgetThemeTypeList;
	}
}
