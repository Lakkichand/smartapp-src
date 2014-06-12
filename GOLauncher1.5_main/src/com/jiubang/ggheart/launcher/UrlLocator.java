package com.jiubang.ggheart.launcher;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;

import com.gau.go.launcherex.R;
import com.go.util.xml.XmlUtils;

public final class UrlLocator {
	public static class UrlAddress {
		int mStringId; // 标题（String）对应的id
		String mUrl; // 下载地址
	}

	private final static String TAG_FTP = "ftp";
	private final static String TAG_URLADDRESS = "urlAddress";

	/**
	 * 获取下载地址 配置文件在res/xml/address.xml
	 * 
	 * @param context
	 * @return
	 */
	public static HashMap<Integer, String> getUrlList(Context context) {
		try {
			XmlResourceParser parser = context.getResources().getXml(R.xml.address);
			AttributeSet attrs = Xml.asAttributeSet(parser);
			XmlUtils.beginDocument(parser, TAG_FTP);

			HashMap<Integer, String> urlMap = new HashMap<Integer, String>(8);
			final int depth = parser.getDepth();
			int type;
			while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
					&& type != XmlPullParser.END_DOCUMENT) {

				if (type != XmlPullParser.START_TAG) {
					continue;
				}

				TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UrlAddress);
				final String name = parser.getName();
				if (TAG_URLADDRESS.equals(name)) {
					UrlAddress address = getUrlAddress(a);
					if (address != null && address.mStringId > 0) {
						urlMap.put(address.mStringId, address.mUrl);
					}
					address = null;
				}
				a.recycle();
			}
			parser.close();
			parser = null;
			return urlMap;
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}

		return null;
	}

	/**
	 * 获取字符串id对应的url
	 * 
	 * @param strId
	 * @param context
	 * @return url
	 */
	public static String getUrl(int strId, Context context) {
		try {
			XmlResourceParser parser = context.getResources().getXml(R.xml.address);
			AttributeSet attrs = Xml.asAttributeSet(parser);
			XmlUtils.beginDocument(parser, TAG_FTP);

			final int depth = parser.getDepth();
			int type;
			while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
					&& type != XmlPullParser.END_DOCUMENT) {

				if (type != XmlPullParser.START_TAG) {
					continue;
				}

				final String name = parser.getName();
				TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UrlAddress);
				if (TAG_URLADDRESS.equals(name)) {
					int id = a.getResourceId(R.styleable.UrlAddress_refId, 0);
					if (id == strId) {
						String url = a.getString(R.styleable.UrlAddress_address);
						a.recycle();
						a = null;
						parser.close();
						parser = null;
						return url;
					}
				}
				a.recycle();
			}
			parser.close();
			parser = null;
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}

		return null;
	}

	private static UrlAddress getUrlAddress(TypedArray a) {
		if (a == null) {
			return null;
		}

		UrlAddress address = new UrlAddress();
		address.mStringId = a.getResourceId(R.styleable.UrlAddress_refId, 0);
		address.mUrl = a.getString(R.styleable.UrlAddress_address);
		return address;
	}
}
