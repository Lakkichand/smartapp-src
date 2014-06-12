package com.jiubang.ggheart.data.theme.parser;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import com.jiubang.ggheart.data.theme.bean.PreviewThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;

public class ParseWidgetTheme extends IParser {
	private static String WIDGET_STYLE = "widget_style";
	private static String WIDGET_PREVIEW = "theme_preview";
	private static String WIDGET_TITLE = "theme_title";
	private static String WIDGET_THEME_TYPE = "widget_theme_type";

	@Override
	public void parseXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		Log.i("ParseWidgetTheme", "ThemeInfoPraser.praseXml");

		if (xmlPullParser == null || bean == null) {
			Log.i("ParseWidgetTheme", "ThemeInfoPraser.praseXml"
					+ " xmlPullParser == null || bean == null");
			return;
		}

		PreviewThemeBean appThemeBean = (PreviewThemeBean) bean;
		try {
			while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
				String attrName = xmlPullParser.getName();
				if (attrName == null) {
					continue;
				}

				if (attrName.equals(WIDGET_STYLE)) {
					int attributeCount = xmlPullParser.getAttributeCount();
					String style = null;
					ArrayList<String> widgetStyleList = appThemeBean.getWidgetStyleList();
					if (widgetStyleList == null) {
						continue;
					}

					for (int i = 0; i < attributeCount; i++) {
						style = xmlPullParser.getAttributeValue(i);
						widgetStyleList.add(style);
					}
					continue;
				} else if (attrName.equals(WIDGET_PREVIEW)) {
					int attributeCount = xmlPullParser.getAttributeCount();
					String img = null;
					ArrayList<String> widgetPreviewList = appThemeBean.getWidgetPreviewList();
					if (widgetPreviewList == null) {
						continue;
					}
					for (int i = 0; i < attributeCount; i++) {
						img = xmlPullParser.getAttributeValue(i);
						widgetPreviewList.add(img);
					}
					continue;
				} else if (attrName.equals(WIDGET_TITLE)) {
					int attributeCount = xmlPullParser.getAttributeCount();
					String title = null;
					ArrayList<String> widgetTitleList = appThemeBean.getWidgetTitleList();
					if (widgetTitleList == null) {
						continue;
					}
					for (int i = 0; i < attributeCount; i++) {
						title = xmlPullParser.getAttributeValue(i);
						widgetTitleList.add(title);
					}

					continue;
				} else if (attrName.equals(WIDGET_THEME_TYPE)) {
					int attributeCount = xmlPullParser.getAttributeCount();
					String title = null;
					ArrayList<String> widgetThemeTypeList = appThemeBean.getWidgetThemeTypeList();
					if (widgetThemeTypeList == null) {
						continue;
					}
					for (int i = 0; i < attributeCount; i++) {
						title = xmlPullParser.getAttributeValue(i);
						widgetThemeTypeList.add(title);
					}

					continue;
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return;

	}
}
