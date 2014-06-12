package com.jiubang.ggheart.data.theme.parser;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import com.jiubang.ggheart.data.theme.bean.PreviewSpecficThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;

public class ParseSpecficWidgetTheme extends IParser {
	public static String WIDGET_PREVIEW = "theme_preview";
	public static String WIDGET_TITLE = "theme_title";
	public static String WIDGET_THEME_TYPE = "widget_theme_type";
	public static String WIDGET_STYLE = "widget_style";

	// private int mThemePosition = -1;
	private ArrayList<Integer> mThemePositionList = null;

	private ArrayList<Integer> mStyleIdsList;

	@SuppressWarnings("unused")
	@Override
	public void parseXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		Log.i("ParseWidgetTheme", "ThemeInfoPraser.praseXml");

		if (xmlPullParser == null || bean == null) {
			Log.i("ParseWidgetTheme", "ThemeInfoPraser.praseXml"
					+ " xmlPullParser == null || bean == null");
			return;
		}

		PreviewSpecficThemeBean appThemeBean = (PreviewSpecficThemeBean) bean;

		// mThemePositionList = new ArrayList<Integer>();

		mThemePositionList = appThemeBean.getThemePositionList();
		String style = appThemeBean.getWidgetStyle();

		try {
			while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
				String attrName = xmlPullParser.getName();
				if (attrName == null || xmlPullParser.getEventType() == XmlPullParser.END_TAG) {
					continue;
				}

				if (attrName.equals(WIDGET_STYLE)) {
					int attributeCount = xmlPullParser.getAttributeCount();

					mStyleIdsList = appThemeBean.getmStyleIdsList();
					for (int i = 0; i < attributeCount; i++) {
						if (style == null) {
							// 如果没设置style值，则扫出所有这个widget在这个皮肤包下的缩略图
							mThemePositionList.add(i);
						} else if (style.equals(xmlPullParser.getAttributeValue(i))) {
							mThemePositionList.add(i);
						}
						String style_tmp = xmlPullParser.getAttributeValue(i);
						if (null != style_tmp) {
							mStyleIdsList.add(Integer.valueOf(style_tmp));
						}
					}

					continue;
				} else if (attrName.equals(WIDGET_PREVIEW) || attrName.equals(WIDGET_TITLE)
						|| attrName.equals(WIDGET_THEME_TYPE)) {
					int attributeCount = xmlPullParser.getAttributeCount();

					ArrayList<String> themeAttribList = new ArrayList<String>();
					if (attributeCount < 0 || null == themeAttribList) {
						continue;
					}

					for (int i = 0; i < mThemePositionList.size(); i++) {
						int position = mThemePositionList.get(i);

						if (position < attributeCount) {
							themeAttribList.add(xmlPullParser.getAttributeValue(position));
						}
					}

					appThemeBean.setWidgetAttrib(attrName, themeAttribList);

					if (attrName.equals(WIDGET_TITLE)) {
						break;
					} else {
						continue;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return;

	}

	/*
	 * public void parseXml(XmlPullParser xmlPullParser, String style,
	 * ArrayList<Integer> themePosition) { Log.i("ParseWidgetTheme",
	 * "ThemeInfoPraser.praseXml2");
	 * 
	 * if (xmlPullParser == null || themePosition == null) {
	 * Log.i("ParseWidgetTheme", "ThemeInfoPraser.praseXml" +
	 * " xmlPullParser == null || themePosition == null"); return; }
	 * 
	 * try { while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) { String
	 * attrName = xmlPullParser.getName(); if (attrName == null){ continue; }
	 * 
	 * if (attrName.equals(WIDGET_STYLE)) { int attributeCount =
	 * xmlPullParser.getAttributeCount();
	 * 
	 * for (int i = 0; i < attributeCount; i++) { if
	 * (style.equals(xmlPullParser.getAttributeValue(i))) {
	 * themePosition.add(i); } }
	 * 
	 * return; } } } catch (Exception e) { // TODO: handle exception
	 * e.printStackTrace(); }
	 * 
	 * return; }
	 */
}