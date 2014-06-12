/**
 * 
 */
package com.jiubang.ggheart.apps.desks.dock;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author ruxueqin
 * 
 */
public class DockStyleParser extends IStyleParser {

	/**
	 * 
	 */
	public DockStyleParser() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jiubang.ggheart.apps.desks.dock.IStyleParser#parseXml(org.xmlpull
	 * .v1.XmlPullParser, com.jiubang.ggheart.apps.desks.dock.DockStylePkgInfo)
	 */
	@Override
	public void parseXml(XmlPullParser xmlPullParser, StyleBaseInfo info)
			throws IllegalArgumentException {
		if (null == xmlPullParser || null == info) {
			throw new IllegalArgumentException("params cannot be null");
		} else if (!(info instanceof DockStylePkgInfo)) {
			throw new IllegalArgumentException(
					"the second param is not instanceof DockStylePkgInfo");
		}

		DockStylePkgInfo dockInfo = (DockStylePkgInfo) info;

		try {
			int eventType = xmlPullParser.getEventType();
			String attributeValue = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					// 标签名
					String tagName = xmlPullParser.getName();
					if (StyleTagSet.CONFIG.equals(tagName)) {
						// version
						attributeValue = xmlPullParser.getAttributeValue(null, StyleTagSet.VERSION);
						dockInfo.mVersion = Float.valueOf(attributeValue);

						// versioncode
						attributeValue = xmlPullParser.getAttributeValue(null,
								StyleTagSet.VERSIONCODE);
						dockInfo.mVersionCode = Float.valueOf(attributeValue);
					} else if (StyleTagSet.RESOURCE.equals(tagName)) {

					} else if (StyleTagSet.ITEM.equals(tagName)) {
						attributeValue = xmlPullParser
								.getAttributeValue(null, StyleTagSet.DRAWABLE);
						dockInfo.mImageResList.add(attributeValue);
					}
				}
				eventType = xmlPullParser.next();
			}
		} catch (Exception e) {

		}
	}

}
