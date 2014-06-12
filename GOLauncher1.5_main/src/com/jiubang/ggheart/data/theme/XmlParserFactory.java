package com.jiubang.ggheart.data.theme;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.net.CryptTool;
import com.jiubang.ggheart.data.theme.zip.ZipResources;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-13]
 */
public class XmlParserFactory {

	/**
	 * 创建一个XmlPullParser实例
	 * 
	 * @author huyong
	 */
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
		if (themePackage.equals(ThemeManager.DEFAULT_THEME_PACKAGE_3)) {
			themePackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		}
		Context ctx = AppUtils.getAppContext(context, themePackage);
		if (ctx != null && fileName != null) {

			int end = fileName.indexOf(".xml");
			if (end <= 0) {
				return null;
			}
			String xmlName = fileName.substring(0, end);
			XmlPullParser xmlPullParser = null;
			int id = ctx.getResources().getIdentifier(xmlName, "xml", themePackage);
			if (id != 0) {
				try {
					 xmlPullParser = ctx.getResources().getXml(id);
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				 id = ctx.getResources().getIdentifier(xmlName, "raw", themePackage);
				 if (id != 0) {
					 InputStream inputStream = ctx.getResources().openRawResource(id);
					 xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
				 }
			}
			 return xmlPullParser;

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
		if (packageName.equals(ThemeManager.DEFAULT_THEME_PACKAGE_3)) {
			packageName = ThemeManager.DEFAULT_THEME_PACKAGE;
		}
		InputStream inputStream = null;
		try {
			Resources resources = null;
			if (AppUtils.isAppExist(context, packageName)) {
				resources = context.getPackageManager().getResourcesForApplication(packageName);
			} else {
				resources = ZipResources.getThemeResourcesFromReflect(context, packageName);
			}
			inputStream = resources.getAssets().open(fileName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.i("XmlParserFactor", "NameNotFoundException for " + packageName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.i("XmlParserFactor", "IOException for " + packageName);
		} catch (Exception e) {
			// TODO: handle exception
			// e.printStackTrace();
			Log.i("XmlParserFactor", "Exception for " + packageName);
		}
		return inputStream;
	}

	public static InputStream createEncryptXmlInputStream(Context context, String packageName,
			String fileName) {
		InputStream inputStream = null;
		try {
			if (packageName.equals(ThemeManager.DEFAULT_THEME_PACKAGE_3)) {
				packageName = ThemeManager.DEFAULT_THEME_PACKAGE;
			}
			inputStream = createInputStream(context, packageName, fileName);
			String decryptresult = CryptTool.decryptFileToString(inputStream,
					LauncherEnv.THEMM_ENCRYPT_KEY);
			if (inputStream != null) {
				inputStream.close();
				if (decryptresult != null) {
					inputStream = new ByteArrayInputStream(decryptresult.getBytes());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputStream;
	}

	// XmlPullParser xmlParser = resources.getXml(xmlResourceId);
}
