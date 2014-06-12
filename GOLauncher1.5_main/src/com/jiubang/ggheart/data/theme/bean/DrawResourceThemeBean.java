package com.jiubang.ggheart.data.theme.bean;

import java.util.ArrayList;

public class DrawResourceThemeBean extends ThemeBean {

	private ArrayList<String> mDrawrResourceList;

	public DrawResourceThemeBean() {
		mBeanType = THEMEBEAN_TYPE_DRAWRESOURCE;
	}

	public ArrayList<String> getDrawrResourceList() {
		return mDrawrResourceList;
	}

	public void addDrawableName(String drawableName) {
		if (drawableName == null) {
			return;
		}
		if (mDrawrResourceList == null) {
			mDrawrResourceList = new ArrayList<String>();
		}
		mDrawrResourceList.add(drawableName);
	}
}
