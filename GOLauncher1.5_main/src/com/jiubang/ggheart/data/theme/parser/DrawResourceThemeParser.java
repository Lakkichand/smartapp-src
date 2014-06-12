package com.jiubang.ggheart.data.theme.parser;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import com.jiubang.ggheart.data.theme.bean.DrawResourceThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;

public class DrawResourceThemeParser extends IParser {

	private static String ITEM = "item";
	private static String DRAWABLE = "drawable";

	@Override
	public void parseXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		Log.i("praseXml", "DrawResourceThemeParser.parseXml");

		if (xmlPullParser == null || bean == null) {
			Log.i("praseXml", "DrawResourceThemeParser.parseXml"
					+ " xmlPullParser == null || bean == null");
			return;
		}

		DrawResourceThemeBean resourceBean = null;
		if (bean instanceof DrawResourceThemeBean) {
			resourceBean = (DrawResourceThemeBean) bean;
		}
		if (null == resourceBean) {
			return;
		}
		try {
			String attributeValue = null;
			while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
				String attrName = xmlPullParser.getName();
				if (attrName == null || xmlPullParser.getEventType() == XmlPullParser.END_TAG) {
					continue;
				}
				if (attrName.equals(VERSION)) {
					String version = xmlPullParser.nextText();
					int verId = 0;
					try {
						verId = Integer.parseInt(version);
					} catch (NumberFormatException e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					resourceBean.setVerId(verId);
					continue;
				} else if (attrName.equals(ITEM)) {
					attributeValue = xmlPullParser.getAttributeValue(null, DRAWABLE);
					resourceBean.addDrawableName(attributeValue);
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
