package com.jiubang.ggheart.data.theme.parser;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.jiubang.ggheart.data.theme.bean.DeskFolderThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.FolderStyle;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.WallpaperBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;

/**
 * 桌面用户文件夹的三个图标解析器
 * 
 * @author jiangxuwen
 * 
 */
public class DeskFolderThemeParser extends IParser {

	@Override
	public void parseXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		// 数据验证
		if (null == xmlPullParser) {
			return;
		}

		DeskFolderThemeBean folderBean = null;
		if (bean instanceof DeskFolderThemeBean) {
			folderBean = (DeskFolderThemeBean) bean;
		}
		if (null == folderBean) {
			return;
		}
		// String attributeValue = null;
		// 解析
		try {
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					// 标签名
					String tagName = xmlPullParser.getName();
					//
					// if (tagName.equals(TagSet.DESK))
					// {
					// attributeValue = xmlPullParser.getAttributeValue(null,
					// AttributeSet.VERSION);
					// folderBean.mDeskVersion = Float.valueOf(attributeValue);
					// }
					if (tagName.equals(TagSet.FOLDERSTYLE)) {
						FolderStyle folderStyle = folderBean.createFolderStyle();
						parceFolderStyle(xmlPullParser, folderBean, folderStyle);
						folderBean.mFolderStyle = folderStyle;
						return;
					}
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parceFolderStyle(XmlPullParser xmlPullParser, DeskFolderThemeBean deskBean,
			FolderStyle folderStyle) throws XmlPullParserException, IOException {
		// 有效次数的统计
		int count = 0;
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(TagSet.WALLPAPER)) {
					WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					// 计数加1
					count++;
					if (null != wallpaper.mIdentity) {
						if (wallpaper.mIdentity.equals(AttributeSet.OPENFOLDER)) {
							folderStyle.mOpendFolder = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.COLSEFOLDER)) {
							folderStyle.mClosedFolder = wallpaper;
						} else {
							folderStyle.mBackground = wallpaper;
						}
					} else {
						folderStyle.mBackground = wallpaper;
					}
				}
			}
			// 超过3的话就跳出返回
			if (count > 3) {
				return;
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceWallpaper(XmlPullParser xmlPullParser, DeskFolderThemeBean deskBean,
			WallpaperBean wallpaper) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IDENTITY);
		wallpaper.mIdentity = stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IMAGE);
		wallpaper.mResName = stringToString(attributeValue);

	}

	private String stringToString(String value) {
		if (null == value) {
			return null;
		}
		if (value.length() == 0) {
			return null;
		}
		return value;
	}

}