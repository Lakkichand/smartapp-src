package com.gau.go.launcherex.theme.cover;

import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * @author jiangxuwen
 */
public class XmlParserFactory {

	public static XmlPullParser createXmlParser(InputStream inputStream) {
		XmlPullParser xmlPullParser = null;
		if (inputStream == null) {
			return xmlPullParser;
		}
		try {
			xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
			xmlPullParser.setInput(inputStream, null);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			xmlPullParser = null;
		} catch (Exception e) {
			e.printStackTrace();
			xmlPullParser = null;
		}

		return xmlPullParser;
	}

	/**
	 * <br>功能简述:该方法用于创建xml文件夹下的Parser
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param fileName
	 * @param themePackage
	 * @return
	 */
	public static XmlPullParser createXmlParser(Context context, String fileName,
			String themePackage) {
		Context ctx = context;
		if (ctx != null && fileName != null) {

			int end = fileName.indexOf(".xml");
			if (end <= 0) {
				return null;
			}
			String xmlName = fileName.substring(0, end);
			int id = ctx.getResources().getIdentifier(xmlName, "xml", themePackage);
			try {
				return ctx.getResources().getXml(id);
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 创建一个InputStream实例，注意，外部使用者用完后要关闭
	 * 
	 * @author huyong
	 * @param context
	 *            上下文 ，需要用来获取getPackageManager
	 * @param packageName
	 *            包名，生成对应包下的Resources
	 * @param fileName
	 *            对应的资源文件
	 * @return 创建成功，则返回一个XmlPullParser，否则返回null
	 * @return
	 */
	public static InputStream createInputStream(Context context, String packageName, String fileName) {
		if (context == null || packageName == null || fileName == null) {
			return null;
		}
		InputStream inputStream = null;
		Resources resources = context.getResources();
		try {
			inputStream = resources.getAssets().open(fileName);
		} catch (Exception e) {
			// TODO: handle exception
			// e.printStackTrace();
//			Log.i("XmlParserFactor", "Exception for " + packageName);
		}
		try {
			if (inputStream == null) {
				final int id = resources.getIdentifier(fileName, "raw", packageName);
				if (id != 0) {
					inputStream = resources.openRawResource(id);
				}
			}
		} catch (Exception e) {
		}
		return inputStream;
	}

	// XmlPullParser xmlParser = resources.getXml(xmlResourceId);
}
