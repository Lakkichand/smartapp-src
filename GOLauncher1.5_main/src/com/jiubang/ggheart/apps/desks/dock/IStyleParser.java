/**
 * 
 */
package com.jiubang.ggheart.apps.desks.dock;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author ruxueqin
 * 
 */
public abstract class IStyleParser {
	public abstract void parseXml(final XmlPullParser xmlPullParser, StyleBaseInfo info);
}
