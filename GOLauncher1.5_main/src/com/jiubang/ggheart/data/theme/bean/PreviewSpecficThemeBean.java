package com.jiubang.ggheart.data.theme.bean;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class PreviewSpecficThemeBean extends ThemeBean {

	private String mWidgetStyle = null; // 所选择widget样式
	// private String mWidgetPreview = null; //所选择widget样式的主题预览图
	// private String mWidgetTitle = null; //所选择widget样式的主题title

	// private WidgetInfo mWidgetInfo = null; //与themeBean绑定的widgetInfo
	// private ParseSpecficWidgetTheme mParser = null;
	// //与themeBean绑定的ParseSpecficWidgetTheme
	// private XmlPullParser mXmlPullParser = null;
	// //与themeBean绑定的ParseSpecficWidgetTheme
	private InputStream mInputStream = null; // 与themeBean绑定的InputStream
	// private BroadCaster mBroadCaster = null;

	// private int mPosition = -1; //themeBean所在位置
	// private int mThemePosition = -1; //theme在主题配置文件中位置

	private HashMap<String, ArrayList<String>> mAttribHashMap;

	private ArrayList<Integer> mThemePositionList;

	private ArrayList<Integer> mStyleIdsList;

	public PreviewSpecficThemeBean() {
		mBeanType = THEMEBEAN_TYPE_WIDGET;
		mAttribHashMap = new HashMap<String, ArrayList<String>>();
		mThemePositionList = new ArrayList<Integer>();
		mStyleIdsList = new ArrayList<Integer>();;
	}

	public ArrayList<String> getWidgetAttrib(String attribType) {
		return mAttribHashMap.get(attribType);
	}

	public void setWidgetAttrib(String attribType, ArrayList<String> attrib) {
		mAttribHashMap.put(attribType, attrib);
	}

	public ArrayList<Integer> getThemePositionList() {
		return mThemePositionList;
	}

	public void setThemePosition(ArrayList<Integer> themePositionList) {
		mThemePositionList = themePositionList;
	}

	public String getWidgetStyle() {
		return mWidgetStyle;
	}

	public void setWidgetStyle(String widgetStyle) {
		mWidgetStyle = widgetStyle;
	}

	public InputStream getInputStream() {
		return mInputStream;
	}

	public void setInputStream(InputStream inputStream) {
		mInputStream = inputStream;
	}

	/**
	 * @return the mStyleIdsList
	 */
	public ArrayList<Integer> getmStyleIdsList() {
		return mStyleIdsList;
	}

	/*
	 * public ParseSpecficWidgetTheme getParser(){ return mParser; }
	 * 
	 * public void setParser(ParseSpecficWidgetTheme parser){ mParser = parser;
	 * }
	 * 
	 * public XmlPullParser getXmlPullParser(){ return mXmlPullParser; }
	 * 
	 * public void setXmlPullParser(XmlPullParser parser){ mXmlPullParser =
	 * parser; }
	 */

	// public int getPosition(){
	// return mPosition;
	// }
	//
	// public void setPosition(int position){
	// mPosition = position;
	// }

	// public void sendFinishMsg()
	// {
	// if (mBroadCaster != null)
	// {
	// mBroadCaster.broadCast(EVENT_THEME_PARSE_OK, mPosition, null, null);
	// }
	// }

	/*
	 * public void registerObserver(BroadCasterObserver oberver) { if
	 * (mBroadCaster == null) { mBroadCaster = new BroadCaster(); }
	 * mBroadCaster.registerObserver(oberver); }
	 * 
	 * public boolean unRegisterObserver(BroadCasterObserver observer) { return
	 * mBroadCaster.unRegisterObserver(observer); }
	 */
}
