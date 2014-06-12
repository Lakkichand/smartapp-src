package com.jiubang.ggheart.data.theme.parser;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import com.jiubang.ggheart.data.theme.bean.ThemeBean;

public class SettingThemeParser extends IParser {
	// just for test
	private static String CUSTOMIZEDBG = "customizedbg";
	private static String GRID = "grid";

	// just for test end

	@Override
	public void parseXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		// TODO Auto-generated method stub
		if (xmlPullParser == null || bean == null) {
			Log.i("praseXml", "ThemeInfoPraser.praseXml" + " xmlPullParser == null || bean == null");
			return;
		}

	}
}