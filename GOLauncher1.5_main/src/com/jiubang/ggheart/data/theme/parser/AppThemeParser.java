package com.jiubang.ggheart.data.theme.parser;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import com.jiubang.ggheart.data.theme.ThemeConfig;
import com.jiubang.ggheart.data.theme.bean.AppDataThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
/**
 * 
 *
 */
public class AppThemeParser extends IParser {

	// private static String THEMEIMG = "themeimg";
	private static String ICONBACK = "iconback";
	private static String ICONUPON = "iconupon";
	// mask蒙版
	private static String ICONMASK = "iconmask";
	private static String SCALE = "scale";
	private static String FACTOR = "factor";

	private static String ITEM = "item";
	private static String COMPONENT = "component";
	private static String DRAWABLE = "drawable";

	public AppThemeParser() {
		mAutoParserFileName = ThemeConfig.APPFILTERFILENAME;
	}

	@Override
	protected ThemeBean createThemeBean(String pkgName) {
		// TODO Auto-generated method stub
		return new AppDataThemeBean(pkgName);
	}

	@Override
	public void parseXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		Log.i("praseXml", "ThemeInfoPraser.praseXml");

		if (xmlPullParser == null || bean == null) {
			Log.i("praseXml", "ThemeInfoPraser.praseXml"
					+ " xmlPullParser == null || bean == null");
			return;
		}

		AppDataThemeBean appThemeBean = (AppDataThemeBean) bean;
		try {
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String attrName = xmlPullParser.getName();
					if (attrName != null) {
						if (attrName.equals(VERSION)) {
							String version = xmlPullParser.nextText();
							int verId = 0;
							try {
								verId = Integer.parseInt(version);
							} catch (NumberFormatException e) {
								// TODO: handle exception
								e.printStackTrace();
							}
							appThemeBean.setVerId(verId);
						} else if (attrName.equals(ICONBACK)) {
							int attributeCount = xmlPullParser
									.getAttributeCount();
							String img = null;
							ArrayList<String> iconbackList = appThemeBean
									.getIconbackNameList();
							if (iconbackList != null) {
								for (int i = 0; i < attributeCount; i++) {
									img = xmlPullParser.getAttributeValue(i);
									iconbackList.add(img);
								}
							}
						} else if (attrName.equals(ICONMASK)) { // 解析mask蒙版
							int attributeCount = xmlPullParser
									.getAttributeCount();
							String img = null;
							ArrayList<String> iconmaskList = appThemeBean
									.getmIconmaskNameList();
							if (iconmaskList != null) {
								for (int i = 0; i < attributeCount; i++) {
									img = xmlPullParser.getAttributeValue(i);
									iconmaskList.add(img);
								}
							}
						} else if (attrName.equals(ICONUPON)) {
							int attributeCount = xmlPullParser
									.getAttributeCount();
							String img = null;
							ArrayList<String> iconuponList = appThemeBean
									.getIconuponNameList();
							if (iconuponList != null) {
								for (int i = 0; i < attributeCount; i++) {
									img = xmlPullParser.getAttributeValue(i);
									iconuponList.add(img);
								}
							}
							/*
							 * } else if (attrName.equals(THEMEIMG)) { String
							 * iconName = xmlPullParser.getAttributeValue(null,
							 * ICONBACK); if (iconName != null) {
							 * appThemeBean.setIconbackName(iconName); }
							 * iconName = xmlPullParser.getAttributeValue(null,
							 * ICONUPON); if (iconName != null) {
							 * appThemeBean.setIconuponName(iconName); }
							 * continue;
							 */
						} else if (attrName.equals(SCALE)) {
							String factor = xmlPullParser.getAttributeValue(
									null, FACTOR);
							if (factor != null) {
								try {
									float scaleFactor = Float.valueOf(factor);
									appThemeBean.setScaleFactor(scaleFactor);
								} catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
								}
							}

						} else if (attrName.equals(ITEM)) {
							String component = xmlPullParser.getAttributeValue(
									null, COMPONENT);
							String drawableName = xmlPullParser
									.getAttributeValue(null, DRAWABLE);
							if (component != null && drawableName != null) {
								appThemeBean.getFilterAppsMap().put(component,
										drawableName);
							}
						}
					}

				}
				eventType = xmlPullParser.next();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return;

	}
}
