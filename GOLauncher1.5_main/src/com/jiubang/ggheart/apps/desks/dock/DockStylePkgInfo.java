/**
 * 
 */
package com.jiubang.ggheart.apps.desks.dock;

import java.util.ArrayList;

/**
 * @author ruxueqin dock风格包信息
 */
public class DockStylePkgInfo extends StyleBaseInfo {
//	public static final String PKG_NAME = "com.gau.go.launcherex.dock"; // 风格包共同的Intent-filter

	public static final String PHONE_NAME = "dock_phone";
	public static final String CONTACTS_NAME = "dock_contacts";
	public static final String APPDRAWER_NAME = "dock_appdrawer";
	public static final String SMS_NAME = "dock_sms";
	public static final String BROWSER_NAME = "dock_browser";
	public static final String ADD_NAME = "dock_addicon";

	public static final String CONFIG_XML = "config.xml";

	public ArrayList<String> mImageResList;

	public DockStylePkgInfo() {
		mImageResList = new ArrayList<String>();
	}

	@Override
	public void cleanup() {
		super.cleanup();

		if (null != mImageResList) {
			mImageResList.clear();
			mImageResList = null;
		}
	}
}
