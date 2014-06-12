package com.jiubang.ggheart.apps.gowidget;

import android.content.res.Resources;

/**
 * 解析GoWidget array.xml后保存的数据
 * 
 * @author luopeihuan
 * 
 */
public class WidgetParseInfo extends AbsWidgetInfo implements Cloneable {
	public String layoutID;
	public String configActivty;
	public String longkeyConfigActivty;
	public Resources resouces;
	public int type;
	public int minHeight;
	public int minWidth;
	public int mAddIndex; // 添加时所在列表的位置
	// 当前界面的主题包
	public String themePackage;
	// widget的"styletypelist",写在array里面的值
	public String styleType;
	// 当前界面对应的主题包的某个主题type
	public int themeType;

	public WidgetParseInfo() {
		configActivty = "";
		longkeyConfigActivty = "";
		layoutID = "";
		resouces = null;
		resouceId = -1;
		title = "";
		mRow = 0;
		mCol = 0;
		type = -1;
		minHeight = 0;
		minWidth = 0;
		themeType = -1;
	}

	@Override
	public Object clone() {
		WidgetParseInfo t = null;
		try {
			t = (WidgetParseInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return t;
	}
}
