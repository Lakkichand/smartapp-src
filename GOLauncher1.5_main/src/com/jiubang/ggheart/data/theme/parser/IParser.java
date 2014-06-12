package com.jiubang.ggheart.data.theme.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.net.CryptTool;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 
 * @author huyong
 * 
 */
public abstract class IParser {
	public final static String VERSION = "version";
	protected String mAutoParserFileName = null;

	public abstract void parseXml(final XmlPullParser xmlPullParser, ThemeBean bean);

	public ThemeBean autoParseAppThemeXml(Context context, String themePackage, boolean isEncrypt) {
		// public ThemeBean autoParseAppThemeXml(Context context, String
		// themePackage) {
		ThemeBean themeBean = null;
		// 解析应用程序过滤器信息
		if (mAutoParserFileName == null) {
			Log.i("IParser", "Auto Parse failed, you should init mAutoParserFileName first");
			return themeBean;
		}
		Log.i("ThemeManager", "begin parserTheme " + mAutoParserFileName);
		InputStream inputStream = null;
		XmlPullParser xmlPullParser = null;
		Context ctx = AppUtils.getAppContext(context, themePackage);
		int end = mAutoParserFileName.indexOf(".xml");
		if (end <= 0) {
			return null;
		}
		String xmlName = mAutoParserFileName.substring(0, end);
		if (isEncrypt) {
			inputStream = XmlParserFactory.createEncryptXmlInputStream(context, themePackage,
					mAutoParserFileName);
			if (inputStream == null) {
				int id = ctx.getResources().getIdentifier(xmlName, "raw", themePackage);
				if (id != 0) {
					try {
						inputStream = ctx.getResources().openRawResource(id);
						String decryptresult = CryptTool.decryptFileToString(inputStream,
								LauncherEnv.THEMM_ENCRYPT_KEY);
						inputStream.close();
						if (decryptresult != null) {
							inputStream = new ByteArrayInputStream(decryptresult.getBytes());
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NotFoundException e) {

					} catch (Exception e) {
						// TODO: handle exception
					}
				} else { //兼容原来加密放在asset下的老主题
					try {
						inputStream = ctx.getResources().getAssets().open(mAutoParserFileName);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}

			}
		} else {
			inputStream = XmlParserFactory.createInputStream(context, themePackage,
					mAutoParserFileName);
			if (inputStream == null) {
				if (ctx != null && mAutoParserFileName != null) {

					int id = ctx.getResources().getIdentifier(xmlName, "xml", themePackage);
					if (id != 0) {
						try {
							xmlPullParser = ctx.getResources().getXml(id);
						} catch (NotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						id = ctx.getResources().getIdentifier(xmlName, "raw", themePackage);
						if (id != 0) {
							inputStream = ctx.getResources().openRawResource(id);
						}
					}
				}
			}
		}
		if (xmlPullParser == null) {
			xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
		}
		if (xmlPullParser != null) {
			themeBean = createThemeBean(themePackage);
			if (themeBean == null) {
				Log.i("IParser", "Auto Parse failed, you should override createThemeBean() method");
				return themeBean;
			}
			parseXml(xmlPullParser, themeBean);
		}
		// 关闭inputStream
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				Log.i("ThemeManager", "IOException for close inputSteam");
			}
		}

		return themeBean;
	}

	protected ThemeBean createThemeBean(String pkgName) {
		return null;
	}
}
