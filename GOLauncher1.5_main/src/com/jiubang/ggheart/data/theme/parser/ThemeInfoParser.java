package com.jiubang.ggheart.data.theme.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.go.util.ConvertUtils;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.data.theme.LockerManager;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.SpecThemeViewConfig;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.bean.ThemeNotifyBean;
import com.jiubang.ggheart.data.theme.bean.ThemeSpecDataBean;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 主题基本信息
 * 
 * @author huyong
 * 
 */
public class ThemeInfoParser extends IParser {

	private final static String THEMENAME = "themeName";
	private final static String THEMEINFO = "themeInfo";
	private final static String PREVIEW = "preview";
	private final static String MASKVIEW = "maskView";
	private final static String MIDDLEVIEW = "middleView";
	private final static String SURFACEVIEW = "surfaceView";
	private final static String MINVERSION = "minLauncherVersion";
	private final static String VERSIONCODE = "versioncode";
	private final static String VERSIONNAME = "versionname";
	private final static String THEMETYPE = "themeType";
	private final static String URL_FTP = "url_ftp";
	private final static String URL_GOOGLEMARKET = "url_googlemarket";
	private final static String URL_GOSTORE = "url_gostore";
	private final static String URL_OTHER = "url_other";
	private final static String URL_WEB_GOOGLEMARKET = "url_key5";
	private final static String ISNEW = "isnew";

	// new themecfg
	private final static String GOLAUNCHER = "golauncher";
	private final static String GOLOCK = "golock";
	private final static String GOWIDGET = "gowidget";
	private final static String NEWTHEMEPKG = "newthemepkg";

	private final static String ITEM = "item";
	private final static String PKGNAME = "pkgname";
	private final static String RECOMMEND = "recommend";
	private final static String LANGUAGE = "language";
	private final static String ENCRYPT = "encrypt";
	private final static String FEATUREDID = "FeaturedId";
	private final static String FEETYPE = "FEETYPE";
	private final static String PAYTYPE = "paytype";
	private final static String PAYID = "payid";
	private final static String DOWNURL = "downurl";
	private final static String MWIDGET = "mwidget";
	private final static String MLOCKER = "mlocker";
	private final static String BEANTYPE = "beantype";
	private final static String IMGSOURCE = "imgsource";
	private final static String IMGURLS = "imgurls";
	private final static String PRICE = "price";

	//通知栏信息
	private final static String STIME = "stime";
	private final static String ETIME = "etime";
	private final static String CONTENT = "content";
	private final static String ICONURL = "iconurl";
	private final static String TYPE = "type";

	//专题页数据
	private final static String SPECID = "id";
	private final static String SPEC_TITLE_BG = "banner";
	private final static String SPEC_BG_IMG = "backgroundimg";
	private final static String SPEC_BG_COLOR = "backgroundcolor";
	private final static String SPEC_BACKBTN_BG = "backbtn";
	private final static String SPEC_TITLECOLOR = "titlecolor";
	private final static String BTN_TEXT_COLOR = "downwordcolor";

	// 激活码
	private final static String ACTIVATION_CODE = "activationCode";
	private final static String ACTIVATION_CODE_NEED = "need";
	private final static String ACTIVATION_CODE_URL = "url";

	// 收费主题
	private final static String CLASS_DEX_NAMES = "classdexnames";
	private final static String CLASS_DEX_SPLIT_TAG = ","; // 分隔符
	private final static String MASKVIEW_PATH = "maskViewPath";
	private final static String MIDDLEVIEW_PATH = "middleViewPath";
	private final static String CLASS_DEX_NAME = "classesDexName";

	private final static String USETYPE = "useType";

	@Override
	public void parseXml(final XmlPullParser xmlPullParser, ThemeBean bean) {

		if (xmlPullParser == null || bean == null) {
			Log.i("praseXml", "ThemeInfoPraser.praseXml" + " xmlPullParser == null || bean == null");
			return;
		}

		ThemeInfoBean baseInfoThemeBean = (ThemeInfoBean) bean;
		try {
			while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
				String attrName = xmlPullParser.getName();
				if (attrName == null) {
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
					baseInfoThemeBean.setVerId(verId);
					continue;
					/*
					 * } else if (attrName.equals(THEMENAME)) {
					 * baseInfoThemeBean.setThemeName( xmlPullParser.nextText()
					 * ); continue; } else if (attrName.equals(THEMEINFO)) {
					 * baseInfoThemeBean.setThemeInfo( xmlPullParser.nextText()
					 * ); continue;
					 */
				} else if (attrName.equals(THEMETYPE)) {
					baseInfoThemeBean.setThemeType(xmlPullParser.nextText());
					continue;
				} else if (attrName.equals(PREVIEW)) {
					int count = xmlPullParser.getAttributeCount();
					baseInfoThemeBean.clearPreviewName();
					for (int i = 0; i < count; i++) {
						String drawableName = xmlPullParser.getAttributeValue(i);
						baseInfoThemeBean.addDrawableName(drawableName);
					}
					// 此处可以解析完毕，可以退出解析
					// break;
					xmlPullParser.next();
					continue;
				} else if (attrName.equals(MINVERSION)) {
					// 要求桌面的最低版本
					String version = xmlPullParser.nextText();
					if (version != null) {
						try {
							int minVersion = Integer.valueOf(version);
							baseInfoThemeBean.setMinGOLauncherVersion(minVersion);
						} catch (Exception e) {
							Log.i("ThemeInfoParser",
									"parser min golauncher version for theme has exception"
											+ e.getMessage());
						}

					}
					break;
				} else if (attrName.equals(ENCRYPT)) {
					boolean isEncrypt = ConvertUtils.int2boolean(Integer.valueOf(xmlPullParser
							.nextText()));
					baseInfoThemeBean.setIsEncrypt(isEncrypt);
				} else if (attrName.equals(MASKVIEW)) {
					boolean maskView = ConvertUtils.int2boolean(Integer.valueOf(xmlPullParser
							.nextText()));
					baseInfoThemeBean.setMaskView(maskView);
				} else if (attrName.equals(MIDDLEVIEW)) {
					boolean isSurfaceView = ConvertUtils.int2boolean(Integer.valueOf(xmlPullParser
							.getAttributeValue(null, SURFACEVIEW)));
					boolean hasMiddleView = ConvertUtils.int2boolean(Integer.valueOf(xmlPullParser
							.nextText()));
					baseInfoThemeBean.setMiddleViewBean(hasMiddleView, isSurfaceView);
				} else if (attrName.equals(ACTIVATION_CODE)) {
					String needActivationCode = xmlPullParser.getAttributeValue(null,
							ACTIVATION_CODE_NEED);
					String activationCodeUrl = xmlPullParser.getAttributeValue(null,
							ACTIVATION_CODE_URL);
					if (TextUtils.isEmpty(needActivationCode)
							|| TextUtils.isEmpty(activationCodeUrl)) {
						continue;
					}
					int need = 0;
					try {
						need = Integer.parseInt(needActivationCode);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						continue;
					}
					baseInfoThemeBean.setNeedActivationCode(need == 1);
					baseInfoThemeBean.setActivationCodeUrl(activationCodeUrl);
					continue;
				} else if (attrName.equals(CLASS_DEX_NAMES)) {
					String maskViewPath = xmlPullParser.getAttributeValue(null, MASKVIEW_PATH);
					String middleViewPath = xmlPullParser.getAttributeValue(null, MIDDLEVIEW_PATH);
					baseInfoThemeBean.setMaskViewPath(maskViewPath);
					baseInfoThemeBean.setMiddleViewPath(middleViewPath);
					// id的集合字符串
					final String nameList = xmlPullParser.getAttributeValue(null, CLASS_DEX_NAME);
					if (!TextUtils.isEmpty(nameList)) {
						baseInfoThemeBean.setClassDexNames(nameList.split(CLASS_DEX_SPLIT_TAG));
					} // end if (!TextUtils.isEmpty(nameList))
					xmlPullParser.next();
					continue;
				} else if (attrName.equals(USETYPE)) {
					try {
						int type = Integer.parseInt(xmlPullParser.nextText());
						baseInfoThemeBean.setUserType(type);
					} catch (NumberFormatException e) {
						// TODO: handle exception
						e.printStackTrace();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return;
	}

	public void parseNewThemecfg(final XmlPullParser xmlPullParser, ThemeBean bean) {
		if (xmlPullParser == null || bean == null) {
			return;
		}
		ThemeInfoBean baseInfoThemeBean = (ThemeInfoBean) bean;
		try {
			while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
				String attrName = xmlPullParser.getName();
				if (attrName == null) {
					continue;
				}
				if (attrName.equals(GOLAUNCHER)) {
					String existGolauncher = xmlPullParser.nextText();
					int golauncherFlag = 0;
					try {
						golauncherFlag = Integer.parseInt(existGolauncher);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					if (golauncherFlag == 1) {
						baseInfoThemeBean.setExistGolauncher(true);
					} else {
						baseInfoThemeBean.setExistGolauncher(false);
					}
					continue;
				} else if (attrName.equals(GOLOCK)) {
					String existGolock = xmlPullParser.nextText();
					int golockFlag = 0;
					try {
						golockFlag = Integer.parseInt(existGolock);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					if (golockFlag == 1) {
						baseInfoThemeBean.setExistGolock(true);
					} else {
						baseInfoThemeBean.setExistGolock(false);
					}
					continue;
				} else if (attrName.equals(GOWIDGET)) {
					int count = xmlPullParser.getAttributeCount();
					String pkgName;
					for (int i = 0; i < count; i++) {
						// 添加widget包名
						pkgName = xmlPullParser.getAttributeValue(i);
						if (pkgName != null) {
							baseInfoThemeBean.addGoWidgetPkgName(pkgName);
						}
					}
					continue;
				} else if (attrName.equals(NEWTHEMEPKG)) {
					int count = xmlPullParser.getAttributeCount();
					String newThemePkg;
					for (int i = 0; i < count; i++) {
						newThemePkg = xmlPullParser.getAttributeValue(i);
						baseInfoThemeBean.getNewThemeInfo().addNewThemePkg(newThemePkg);
					}
					// 此处可以解析完毕，可以退出解析
					break;
				} else if (attrName.equals(ENCRYPT)) {
					boolean isEncrypt = ConvertUtils.int2boolean(Integer.valueOf(xmlPullParser
							.nextText()));
					baseInfoThemeBean.setIsEncrypt(isEncrypt);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通过配置文件解析，生成数据bean列表
	 * 
	 * @author huyong
	 * @param xmlFile
	 * @param beansList
	 * @return 版本号
	 */
	public void parseLauncherThemeXml(Context context, final String xmlFile,
			StringBuffer versionBuffer, StringBuffer recommendCount,
			ArrayList<ThemeInfoBean> beansList, int type) {
		if (beansList == null) {
			return;
		}

		/*
		 * File file = new File(xmlFile); if (file == null || !file.exists()) {
		 * Log.i("ThemeInfoParser", "parseGoThemeXml " + xmlFile +
		 * " not exists"); return; }
		 */
		try {
			File file = new File(xmlFile);
			if (file.exists() && file.length() == 0) {
				file.delete();
			}
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			InputStream inputStream = null;
			boolean isDefaultPkg = false; // 是否默认包中数据
			try {
				inputStream = new FileInputStream(xmlFile);
			} catch (Exception e) {
				// TODO: handle exception
				Log.i("ThemeInfoParser", "parseGoThemeXml " + xmlFile + " not exists");
			}

			if (inputStream == null) {
				isDefaultPkg = true;
				int index = xmlFile.lastIndexOf("/");
				String defaultFile = xmlFile.substring(index + 1);
				inputStream = XmlParserFactory.createInputStream(context,
						ThemeManager.DEFAULT_THEME_PACKAGE, defaultFile);
			}

			parser.setInput(inputStream, null);

			String gothemePath = null;
			if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
					|| type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
				gothemePath = LauncherEnv.Path.GOTHEMES_PATH + "icon/";

			} else {
				gothemePath = LauncherEnv.Path.GOTHEMES_PATH + "lockericon/";
			}
			String value = null;
			String attrName = null;
			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				attrName = parser.getName();
				if (attrName == null) {
					continue;
				}
				// 解析版本
				if (attrName.equals(VERSION)) {
					String version = parser.getAttributeValue("", VERSION);
					versionBuffer.append(version);
					parser.next();
				} else if (attrName.equals(RECOMMEND)) {
					// 解析推荐个数
					String recommend = parser.getAttributeValue("", RECOMMEND);
					recommendCount.append(recommend);
					parser.next();
				} else if (attrName.equals(ITEM)) {
					ThemeInfoBean bean = new ThemeInfoBean();
					value = parser.getAttributeValue("", VERSIONCODE);
					if (value != null) {
						bean.setVersionCode(value);
					}

					value = parser.getAttributeValue("", VERSIONNAME);
					if (value != null) {
						bean.setVersionName(value);
					}

					value = parser.getAttributeValue("", PKGNAME);
					bean.setPackageName(value);

					value = parser.getAttributeValue("", PREVIEW);
					if (isDefaultPkg) {
						// 是默认，则取默认包下
						bean.addDrawableName(value);
					} else {
						bean.addDrawableName(gothemePath + value);
					}
					// value = parser.getAttributeValue("", ICONID);
					// bean.setFeaturedImageId(value);
					value = parser.getAttributeValue("", FEETYPE);
					if (value != null) {
						try {
							bean.setFeeType(Integer.valueOf(value));
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					value = parser.getAttributeValue("", FEATUREDID);
					if (value != null) {
						try {
							bean.setFeaturedId(Integer.valueOf(value));
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					value = parser.getAttributeValue("", PAYTYPE);
					if (value != null) {
						try {
							bean.setPayType(splitString(value, "#"));
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					value = parser.getAttributeValue("", PAYID);
					if (value != null) {
						try {
							bean.setPayId(value);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					value = parser.getAttributeValue("", DOWNURL);
					if (value != null) {
						try {
							bean.setDownloadUrl(value);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					value = parser.getAttributeValue("", MWIDGET);
					if (value != null) {
						try {
							bean.setMwidgetThemeName(value);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					value = parser.getAttributeValue("", MLOCKER);
					if (value != null) {
						try {
							bean.setMlcokerThemeName(value);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					value = parser.getAttributeValue("", BEANTYPE);
					if (value != null) {
						try {
							bean.setBeanType(Integer.valueOf(value));
						} catch (Exception e) {
							// TODO: handle exception
						}
					} else {
						bean.setBeanType(Integer.valueOf(type));
					}

					value = parser.getAttributeValue("", ISNEW);
					if (value != null) {
						try {
							bean.setIsNew(Boolean.valueOf(value));
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					value = parser.getAttributeValue("", IMGSOURCE);
					if (value != null) {
						bean.setImgSource(Integer.valueOf(value));
					} else {
						bean.setImgSource(0);
					}
					value = parser.getAttributeValue("", IMGURLS);
					if (value != null) {
						bean.setImgUrls(splitString(value, "#"));
					}
					value = parser.getAttributeValue("", PRICE);
					if (value != null && !value.trim().equals("")) {
						bean.setPrice(value);
					}
					HashMap<Integer, String> urlMap = new HashMap<Integer, String>();
					value = parser.getAttributeValue("", URL_FTP);
					if (value != null) {
						urlMap.put(ThemeInfoBean.URL_KEY_FTP, value);
					}
					value = parser.getAttributeValue("", URL_GOOGLEMARKET);
					if (value != null) {
						urlMap.put(ThemeInfoBean.URL_KEY_GOOGLEMARKET, value);
					}
					value = parser.getAttributeValue("", URL_GOSTORE);
					if (value != null) {
						urlMap.put(ThemeInfoBean.URL_KEY_GOSTORE, value);
					}
					value = parser.getAttributeValue("", URL_OTHER);
					if (value != null) {
						urlMap.put(ThemeInfoBean.URL_KEY_OTHER, value);
					}
					value = parser.getAttributeValue("", URL_WEB_GOOGLEMARKET);
					if (value != null) {
						urlMap.put(ThemeInfoBean.URL_KEY_WEB_GOOGLEMARKET, value);
					}
					if (urlMap.size() > 0) {
						bean.setUrlMap(urlMap);
					}

					if (type == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
						value = parser.nextText();
						bean.setThemeName(value);
						if (bean.getPackageName() != null && bean.getPackageName().charAt(0) == 's') {
							LockerManager.sSellModeCount++;
						}
					} else {
						value = parser.getAttributeValue("", THEMEINFO);
						if (value != null) {
							bean.setThemeInfo(value);
						}

						value = parser.nextText();
						if (isDefaultPkg) {
							// 是默认，则取国际化语言
							int stringId = context.getResources().getIdentifier(value, "string",
									ThemeManager.DEFAULT_THEME_PACKAGE);
							String name = context.getResources().getString(stringId);
							bean.setThemeName(name);

						} else {
							bean.setThemeName(value);
						}
					}

					beansList.add(bean);
				}

			}

			if (inputStream != null) {
				inputStream.close();
			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 通过数据bean列表，写入信息到xml配置文件中
	 * 
	 * @author huyong
	 * @param beansList
	 *            ：信息列表
	 * @param xmlFile
	 *            ：文件名
	 */
	public boolean writeGoThemeToXml(String version, int recommendCount,
			ArrayList<ThemeInfoBean> beansList, String xmlFile) {
		boolean result = false;
		if (version == null || beansList == null || beansList.size() <= 0) {
			return result;
		}
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer xmlSerializer = XmlPullParserFactory.newInstance().newSerializer();
			xmlSerializer.setOutput(writer);

			xmlSerializer.startDocument("UTF-8", true);

			xmlSerializer.startTag("", LANGUAGE);
			xmlSerializer.attribute("", LANGUAGE, Locale.getDefault().getLanguage()
					+ Locale.getDefault().getCountry());
			xmlSerializer.endTag("", LANGUAGE);

			xmlSerializer.startTag("", VERSION);
			xmlSerializer.attribute("", VERSION, version);
			xmlSerializer.endTag("", VERSION);

			xmlSerializer.startTag("", RECOMMEND);
			xmlSerializer.attribute("", RECOMMEND, String.valueOf(recommendCount));
			xmlSerializer.endTag("", RECOMMEND);

			int size = beansList.size();
			ThemeInfoBean bean = null;
			for (int i = 0; i < size; i++) {
				bean = beansList.get(i);
				xmlSerializer.startTag("", ITEM);
				if (bean.getVersionCode() != null) {
					xmlSerializer.attribute("", VERSIONCODE, bean.getVersionCode());
				}
				if (bean.getVersionName() != null) {
					xmlSerializer.attribute("", VERSIONNAME, bean.getVersionName());
				}
				xmlSerializer.attribute("", PKGNAME, bean.getPackageName());
				xmlSerializer.attribute("", PREVIEW, bean.getPreViewDrawableNames().get(0));
				// NOTE:注意调试主题预览模块themeinfo为空
				if (bean.getThemeInfo() != null) {
					xmlSerializer.attribute("", THEMEINFO, bean.getThemeInfo());
				}
				// if(bean.getFirstPreViewDrawableName() != null){
				// xmlSerializer.attribute("", ICONID,
				// bean.getFeaturedImageId());
				// }
				xmlSerializer.attribute(null, FEATUREDID, String.valueOf(bean.getFeaturedId()));
				xmlSerializer.attribute("", FEETYPE, String.valueOf(bean.getFeeType()));
				String payType = list2String(bean.getPayType(), "#");
				if (payType != null) {
					xmlSerializer.attribute("", PAYTYPE, payType);
				}
				xmlSerializer.attribute("", ISNEW, String.valueOf(bean.getIsNew()));
				if (bean.getPayId() != null) {
					xmlSerializer.attribute("", PAYID, bean.getPayId());
				}
				if (bean.getDownLoadUrl() != null) {
					xmlSerializer.attribute("", DOWNURL, bean.getDownLoadUrl());
				}
				if (bean.getMlcokerThemeName() != null) {
					xmlSerializer.attribute("", MWIDGET, bean.getMlcokerThemeName());
				}
				if (bean.getMwidgetThemeName() != null) {
					xmlSerializer.attribute("", MLOCKER, bean.getMwidgetThemeName());
				}
				if (bean.getPrice() != null && !bean.getPrice().trim().equals("")) {
					xmlSerializer.attribute("", PRICE, bean.getPrice());
				}
				xmlSerializer.attribute("", BEANTYPE, String.valueOf(bean.getBeanType()));
				xmlSerializer.attribute("", IMGSOURCE, String.valueOf(bean.getImgSource()));
				String urls = list2String(bean.getImgUrls(), "#");
				if (urls != null) {
					xmlSerializer.attribute("", IMGURLS, urls);
				}
				HashMap<Integer, String> map = bean.getUrlMap();
				int count = map == null ? 0 : map.size();
				if (count > 0) {
					String url = map.get(ThemeInfoBean.URL_KEY_FTP);
					if (url != null) {
						xmlSerializer.attribute("", URL_FTP, url);
					}
					url = map.get(ThemeInfoBean.URL_KEY_GOOGLEMARKET);
					if (url != null) {
						xmlSerializer.attribute("", URL_GOOGLEMARKET, url);
					}
					url = map.get(ThemeInfoBean.URL_KEY_GOSTORE);
					if (url != null) {
						xmlSerializer.attribute("", URL_GOSTORE, url);
					}
					url = map.get(ThemeInfoBean.URL_KEY_OTHER);
					if (url != null) {
						xmlSerializer.attribute("", URL_OTHER, url);
					}
					url = map.get(ThemeInfoBean.URL_KEY_WEB_GOOGLEMARKET);
					if (url != null) {
						xmlSerializer.attribute("", URL_WEB_GOOGLEMARKET, url);
					}
				}

				xmlSerializer.text(bean.getThemeName());
				xmlSerializer.endTag("", ITEM);
			}
			xmlSerializer.endDocument();

			File file = FileUtil.createNewFile(xmlFile, false);
			OutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			osw.write(writer.toString());
			osw.close();
			os.close();
			result = true;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 通过数据bean列表，写入信息到xml配置文件中
	 * 
	 * @author huyong
	 * @param beansList
	 *            ：信息列表
	 * @param xmlFile
	 *            ：文件名
	 */
	public void writeNotifyDataToXml(ThemeNotifyBean bean) {
		if (bean != null) {
		}
		String xmlFile = null;
		if (bean.getType() == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
			xmlFile = ThemeManager.LOCKER_NOTIFY_DATA_BEAN_XML;
		} else if (bean.getType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			xmlFile = ThemeManager.FEATURED_NOTIFY_DATA_BEAN_XML;
		} else if (bean.getType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			xmlFile = ThemeManager.HOT_NOTIFY_DATA_BEAN_XML;
		}
		StringWriter writer = new StringWriter();
		XmlSerializer xmlSerializer;
		try {
			xmlSerializer = XmlPullParserFactory.newInstance().newSerializer();
			xmlSerializer.setOutput(writer);

			xmlSerializer.startDocument("UTF-8", true);

			xmlSerializer.startTag("", ITEM);
			if (bean.getShowIconUrl() != null) {
				xmlSerializer.attribute("", ICONURL, bean.getShowIconUrl());
			}
			xmlSerializer.attribute("", STIME, String.valueOf(bean.getShowStatTime()));
			xmlSerializer.attribute("", ETIME, String.valueOf(bean.getShowEndTime()));
			xmlSerializer.attribute("", TYPE, String.valueOf(bean.getType()));
			if (bean.getShowContent() != null) {

				xmlSerializer.attribute("", CONTENT, bean.getShowContent());
			}
			xmlSerializer.endTag("", ITEM);
			xmlSerializer.endDocument();
			File file = FileUtil.createNewFile(xmlFile, false);
			OutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			osw.write(writer.toString());
			osw.close();
			os.close();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bean
	 */
	public void writePageViewConfigToXml(ThemeSpecDataBean bean) {
		//		if (bean != null) {
		//		}
		//		String xmlFile = null;
		//		StringWriter writer = new StringWriter();
		//		XmlSerializer xmlSerializer;
		//		try {
		//			xmlSerializer = XmlPullParserFactory.newInstance().newSerializer();
		//			xmlSerializer.setOutput(writer);
		//
		//			xmlSerializer.startDocument("UTF-8", true);
		//
		//			xmlSerializer.startTag("", ITEM);
		//			if (bean.getShowIconUrl() != null) {
		//				xmlSerializer.attribute("", ICONURL, bean.getShowIconUrl());
		//			}
		//			xmlSerializer.attribute("", STIME, String.valueOf(bean.getShowStatTime()));
		//			xmlSerializer.attribute("", ETIME, String.valueOf(bean.getShowEndTime()));
		//			xmlSerializer.attribute("", TYPE, String.valueOf(bean.getType()));
		//			if (bean.getShowContent() != null) {
		//
		//				xmlSerializer.attribute("", CONTENT, bean.getShowContent());
		//			}
		//			xmlSerializer.endTag("", ITEM);
		//			xmlSerializer.endDocument();
		//			File file = FileUtil.createNewFile(xmlFile, false);
		//			OutputStream os = new FileOutputStream(file);
		//			OutputStreamWriter osw = new OutputStreamWriter(os);
		//			osw.write(writer.toString());
		//			osw.close();
		//			os.close();
		//		} catch (XmlPullParserException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (IllegalArgumentException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (IllegalStateException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

	}

	public SpecThemeViewConfig parseSpecViewConfig() {
		SpecThemeViewConfig config = null;
		return config;

	}

	public ThemeNotifyBean parseNotifyData(String xmlFile) {
		ThemeNotifyBean bean = null;
		try {
			File file = new File(xmlFile);
			if (!file.exists() || (file.exists() && file.length() == 0)) {
				file.delete();
				return null;
			}
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(xmlFile);
			} catch (Exception e) {
				// TODO: handle exception
			}

			if (inputStream == null) {
				return null;
			}

			parser.setInput(inputStream, null);

			String value = null;
			String attrName = null;
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					attrName = parser.getName();
					if (attrName == null) {
						continue;
					}
					if (attrName.equals(ITEM)) {
						bean = new ThemeNotifyBean();
						value = parser.getAttributeValue("", ICONURL);
						if (value != null) {
							bean.setShowIconUrl(value);
						}

						value = parser.getAttributeValue("", STIME);
						if (value != null) {
							bean.setShowStatTime(Long.valueOf(value));
						}

						value = parser.getAttributeValue("", ETIME);
						if (value != null) {
							bean.setShowEndTime(Long.valueOf(value));
						}

						value = parser.getAttributeValue("", TYPE);
						if (value != null) {
							try {
								bean.setType(Integer.valueOf(value));
							} catch (Exception e) {
								// TODO: handle exception
							}
						}

						value = parser.getAttributeValue("", CONTENT);
						if (value != null) {
							try {
								bean.setShowContent(value);
							} catch (Exception e) {
								// TODO: handle exception
							}
						}

					}

				}
				eventType = parser.next();
			}
			if (inputStream != null) {
				inputStream.close();
			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bean;
	}

	@Override
	protected ThemeBean createThemeBean(String pkgName) {
		// TODO Auto-generated method stub
		ThemeInfoBean bean = new ThemeInfoBean();
		bean.setPackageName(pkgName);
		return bean;
	}
	private List<String> splitString(String urlString, String regularExpression) {
		if (urlString == null || urlString.equals("")) {
			return null;
		}

		String[] urls = urlString.split(regularExpression);
		if (urls != null && urls.length > 0) {
			return Arrays.asList(urls);
		}
		return null;
	}

	private String list2String(List<String> list, String spliter) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		for (String item : list) {
			result.append(item + spliter);
		}
		return result.toString();
	}

	public SpecThemeViewConfig parseSpecThemeViewConfig(String xmlFile) {
		SpecThemeViewConfig config = null;
		try {
			File file = new File(xmlFile);
			if (!file.exists() || (file.exists() && file.length() == 0)) {
				file.delete();
				return null;
			}
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(xmlFile);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (inputStream == null) {
				return null;
			}
			parser.setInput(inputStream, null);

			String value = null;
			String attrName = null;
			config = new SpecThemeViewConfig();
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					attrName = parser.getName();
					if (attrName == null) {
						continue;
					}
					if (attrName.equals(SPECID)) {
						value = parser.nextText();
						if (value != null) {
							config.mId = value;
						}
					} else if (attrName.equals(SPEC_TITLE_BG)) {
						value = parser.nextText();
						if (value != null) {
							config.mTileGroupBgImage = value;
						}
					} else if (attrName.equals(SPEC_BG_IMG)) {
						value = parser.nextText();
						if (value != null) {
							config.mListViewBgImage = value;
						}
					} else if (attrName.equals(SPEC_BG_COLOR)) {
						value = parser.nextText();
						config.mListViewBgColor = DeskThemeParser.parseColor(value);
					} else if (attrName.equals(SPEC_BACKBTN_BG)) {
						value = parser.nextText();
						if (value != null) {
							config.mBackBtnBgImage = value;
						}
					} else if (attrName.equals(SPEC_TITLECOLOR)) {
						value = parser.nextText();
						if (value != null) {
							config.mTitleColor = DeskThemeParser.parseColor(value);
						}
					} else if (attrName.equals(BTN_TEXT_COLOR)) {
						value = parser.nextText();
						if (value != null) {
							config.mBtnTextColor = DeskThemeParser.parseColor(value);
						}
					}
				}
				eventType = parser.next();
			}
			if (inputStream != null) {
				inputStream.close();
			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return config;
	}
}
