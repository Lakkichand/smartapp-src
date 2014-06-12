package com.jiubang.ggheart.data.theme.parser;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.graphics.Color;
import android.util.Log;

import com.jiubang.ggheart.data.theme.ThemeConfig;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
public class DeskThemeParser extends IParser {

	// private Context mContext;
	// public void setContext(Context context)
	// {
	// mContext = context;
	// }
	public DeskThemeParser() {
		mAutoParserFileName = ThemeConfig.DESKTHEMEFILENAME;
	}

	@Override
	protected ThemeBean createThemeBean(String pkgName) {
		return new DeskThemeBean(pkgName);
	}

	@Override
	public void parseXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		// // 测试代码
		// bean = new DeskThemeBean();
		// xmlPullParser = mContext.getResources().getXml(R.xml.desk);

		// 数据验证
		if (null == xmlPullParser) {
			return;
		}
		DeskThemeBean deskBean = null;
		if (bean instanceof DeskThemeBean) {
			deskBean = (DeskThemeBean) bean;
		}
		if (null == deskBean) {
			return;
		}

		String attributeValue = null;
		// 解析
		try {
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					// 标签名
					String tagName = xmlPullParser.getName();

					if (tagName.equals(TagSet.DESK)) {
						attributeValue = xmlPullParser
								.getAttributeValue(null, AttributeSet.VERSION);
						deskBean.mDeskVersion = Float.valueOf(attributeValue);
						// attributeValue =
						// xmlPullParser.getAttributeValue(null, VERSIONCODE);
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.WALLPAPER);
						deskBean.mWallpaper.mResName = stringToString(attributeValue);
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.SCOLLWALLPAPER);
						deskBean.mIsScollWallpaper = stringToBoolean(attributeValue);
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.WALLPAPERFILL);
						deskBean.mWallpaper.mWallpaperFill = stringToFill(attributeValue);
					} else if (tagName.equals(TagSet.COMMONSTYLES)) {

					} else if (tagName.equals(TagSet.ICONSTYLE)) {
						DeskThemeBean.IconStyle iconStyle = deskBean.createIconStyle();
						parceIconStyle(xmlPullParser, deskBean, iconStyle);
						deskBean.mCommonStyles.mIconStyle = iconStyle;
					} else if (tagName.equals(TagSet.SCREEN)) {
						attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.SOURCE);
						deskBean.mScreen.mSource = stringToString(attributeValue);
					} else if (tagName.equals(TagSet.SCREENSTYLES)) {
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.SCREENCOUNT);
						// deskBean.mScreen.mScreenCount =
						// stringToInt(attributeValue);
					} else if (tagName.equals(TagSet.SCREENICONSTYLE)) {
						DeskThemeBean.ScreenIconStyle iconStyle = deskBean.createScreenIconStyle();
						parceScreenIconStyle(xmlPullParser, deskBean, iconStyle);
						deskBean.mScreen.mIconStyle = iconStyle;
					} else if (tagName.equals(TagSet.FOLDERSTYLE)) {
						DeskThemeBean.FolderStyle folderStyle = deskBean.createFolderStyle();
						parceFolderStyle(xmlPullParser, deskBean, folderStyle);
						deskBean.mScreen.mFolderStyle = folderStyle;
					} else if (tagName.equals(TagSet.LIGHT)) {
						DeskThemeBean.Light light = deskBean.createLight();
						parceLight(xmlPullParser, deskBean, light);
						deskBean.mScreen.mLight = light;
					} else if (tagName.equals(TagSet.FONT)) {
						DeskThemeBean.Font font = deskBean.createFont();
						parceFont(xmlPullParser, deskBean, font);
						deskBean.mScreen.mFont = font;
					} else if (tagName.equals(TagSet.TRASHSTYLE)) {
						DeskThemeBean.TrashStyle trashStyle = deskBean.createTrashStyle();
						parceTrashStyle(xmlPullParser, deskBean, trashStyle);
						deskBean.mScreen.mTrashStyle = trashStyle;
					} else if (tagName.equals(TagSet.DOCK)) {
						attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.SOURCE);
						deskBean.mDock.mSource = stringToString(attributeValue);
					} else if (tagName.equals(TagSet.DOCKSETTING)) {
						DeskThemeBean.DockSettingBean setting = deskBean.createDockSettingBean();
						parceDockSetting(xmlPullParser, deskBean, setting);
						deskBean.mDock.mDockSetting = setting;
					} else if (tagName.equals(TagSet.NOTIFYS)) {

					} else if (tagName.equals(TagSet.NOTIFYITEM)) {
						DeskThemeBean.NotifyItem notifyItem = deskBean.createNotifyItem();
						parceNotifyItem(xmlPullParser, deskBean, notifyItem);
						deskBean.mDock.mNotifys.add(notifyItem);
					} else if (tagName.equals(TagSet.DOCKSTYLES)) {
						attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.COLOR);
						deskBean.mDock.mColor = parseColor(attributeValue);
						attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.WIDTH);
						// deskBean.mDock.mWidth = stringToInt(attributeValue);
						deskBean.mDock.setWidth(stringToInt(attributeValue));
						attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.HEIGHT);
						// deskBean.mDock.mHeight = stringToInt(attributeValue);
						deskBean.mDock.setHeight(stringToInt(attributeValue));
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.LINEITEMCOUNT);
						deskBean.mDock.mLineItemCount = stringToInt(attributeValue);
					} else if (tagName.equals(TagSet.DOCKICONSTYLE)) {
						DeskThemeBean.Layer layer = deskBean.createLayer();
						parceLayer(xmlPullParser, deskBean, layer);
						deskBean.mDock.mIconStyle.add(layer);
					} else if (tagName.equals(TagSet.SYMTEMDEFUALT)) {

					} else if (tagName.equals(TagSet.SYMTEMDOCKITEM)) {
						DeskThemeBean.SystemDefualtItem item = deskBean.createSystemDefualt();
						parceSymtemDockItem(xmlPullParser, deskBean, item);
						deskBean.mDock.mSymtemDefualt.add(item);
					} else if (tagName.equals(TagSet.NOAPPLICATIONICON)) {
						DeskThemeBean.SystemDefualtItem item = deskBean.createSystemDefualt();
						parceSymtemDockItem(xmlPullParser, deskBean, item);
						deskBean.mDock.mNoApplicationIcon = item;
					} else if (tagName.equals(TagSet.NULLICON)) {
						DeskThemeBean.SystemDefualtItem item = deskBean.createSystemDefualt();
						parceSymtemDockItem(xmlPullParser, deskBean, item);
						deskBean.mDock.mNullIcon = item;
					} else if (tagName.equals(TagSet.NOTIFYSTYLE)) {
						DeskThemeBean.NotifyStyle notifyStyle = deskBean.createNotifyStyle();
						parceNotifyStyle(xmlPullParser, deskBean, notifyStyle);
						deskBean.mDock.mNotifyStyle = notifyStyle;
					} else if (tagName.equals(TagSet.THEMEDEFUALT)) {

					} else if (tagName.equals(TagSet.THEMEDOCKITEM)) {
						DeskThemeBean.ThemeDefualtItem item = deskBean.createThemeDefualt();
						parceThemeDockItem(xmlPullParser, deskBean, item);
						deskBean.mDock.mThemeDefualt.add(item);
					} else if (tagName.equals(TagSet.INDICATOR)) {
						attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.SOURCE);
						deskBean.mIndicator.mSource = stringToString(attributeValue);
					} else if (tagName.equals(TagSet.INDICATORSTYLES)) {
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.SHOWMODE);
						deskBean.mIndicator.mIndicatorShowMode = stringToIndicatorShowMode(attributeValue);
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.WHENSCREENCOUNT);
						deskBean.mIndicator.mWhenScreenCount = stringToInt(attributeValue);
					} else if (tagName.equals(TagSet.INDICATORITEM)) {
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.IDENTITY);
						if (attributeValue != null) {
							DeskThemeBean.IndicatorItem indicatorItem = deskBean
									.createIndicatorItem();
							parceIndicatorItem(xmlPullParser, deskBean, indicatorItem);
							if (attributeValue.equals(AttributeSet.DOTS_INDICATOR)) {
								deskBean.mIndicator.mDots = indicatorItem;
							} else if (attributeValue.equals(AttributeSet.SLIDE_INDICATOR)) {
								deskBean.mIndicator.mSlide = indicatorItem;
							}
						}
					} else if (tagName.equals(TagSet.PREVIEW)) {
						attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.SOURCE);
						deskBean.mPreview.mSource = stringToString(attributeValue);
					} else if (tagName.equals(TagSet.PREVIEWSTYLES)) {
						parcePreviewStyle(xmlPullParser, deskBean, deskBean.mPreview);
					} else if (tagName.equals(TagSet.GlMenu)) {
						DeskThemeBean.MenuBean menubean = deskBean.createMenuBean();
						parceMenu(xmlPullParser, deskBean, menubean);
						if (null != menubean.mIdentity) {
							if (menubean.mIdentity.equals("desk")) {
								deskBean.mDeskMenuBean = menubean;
								deskBean.mDeskMenuBean.mPackageName = deskBean.getPackageName();
							} else if (menubean.mIdentity.equals("appdrawer")) {
								deskBean.mAppDrawerMenuBean = menubean;
								deskBean.mAppDrawerMenuBean.mPackageName = deskBean
										.getPackageName();
							} else if (menubean.mIdentity.equals("program")) {
								deskBean.mProgramMenuBean = menubean;
								deskBean.mProgramMenuBean.mPackageName = deskBean.getPackageName();
							}
						}
					} else if (tagName.equals(TagSet.PREFERENCE)) {
						DeskThemeBean.PreferenceAppearanceBean preferenceBean = deskBean
								.createPreferenceAppearanceBean();
						parcePreference(xmlPullParser, deskBean, preferenceBean);
						deskBean.mPreferenceAppearanceBean = preferenceBean;
					}
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
			Log.i("DeskThemeParser", "parseXml has XmlPullParserException = " + e.getMessage());
		} catch (IOException e) {
			Log.i("DeskThemeParser", "parseXml has IOException = " + e.getMessage());
		} catch (Exception e) {
			Log.i("DeskThemeParser", "parseXml has Exception = " + e.getMessage());
		}
	}

	public void parseXmlToDeskMenuBean(XmlPullParser xmlPullParser, ThemeBean bean) {
		if (null == xmlPullParser) {
			return;
		}
		DeskThemeBean deskBean = null;
		if (bean instanceof DeskThemeBean) {
			deskBean = (DeskThemeBean) bean;
		}
		if (null == deskBean) {
			return;
		}

		// 解析
		try {
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					// 标签名
					String tagName = xmlPullParser.getName();

					if (tagName.equals(TagSet.GlMenu)) {
						DeskThemeBean.MenuBean menubean = deskBean.createMenuBean();
						parceMenu(xmlPullParser, deskBean, menubean);
						if (null != menubean.mIdentity) {
							if (menubean.mIdentity.equals("desk")) {
								deskBean.mDeskMenuBean = menubean;
								break;
							}

						}
					}
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
			Log.i("DeskThemeParser",
					"parseXmlToDeskMenuBean has XmlPullParserException = " + e.getMessage());
		} catch (IOException e) {
			Log.i("DeskThemeParser", "parseXmlToDeskMenuBean has IOException = " + e.getMessage());
		} catch (Exception e) {
			Log.i("DeskThemeParser", "parseXmlToDeskMenuBean has Exception = " + e.getMessage());
		}
	}

	public void parseXmlToDockBeanPics(XmlPullParser xmlPullParser, DeskThemeBean bean) {
		// 数据验证
		if (null == xmlPullParser || null == bean) {
			return;
		}

		// 解析
		try {
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					// 标签名
					String tagName = xmlPullParser.getName();

					if (tagName.equals(TagSet.SYMTEMDOCKITEM)) {
						DeskThemeBean.SystemDefualtItem item = bean.createSystemDefualt();
						parceSymtemDockItem(xmlPullParser, bean, item);
						bean.mDock.mSymtemDefualt.add(item);
					} else if (tagName.equals(TagSet.NOAPPLICATIONICON)) {
						DeskThemeBean.SystemDefualtItem item = bean.createSystemDefualt();
						parceSymtemDockItem(xmlPullParser, bean, item);
						bean.mDock.mNoApplicationIcon = item;
					} else if (tagName.equals(TagSet.NULLICON)) {
						DeskThemeBean.SystemDefualtItem item = bean.createSystemDefualt();
						parceSymtemDockItem(xmlPullParser, bean, item);
						bean.mDock.mNullIcon = item;
					}
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
			Log.i("DeskThemeParser",
					"parseXmlToDockBeanPics() has XmlPullParserException = " + e.getMessage());
		} catch (IOException e) {
			Log.i("DeskThemeParser", "parseXmlToDockBeanPics() has IOException = " + e.getMessage());
		} catch (Exception e) {
			Log.i("DeskThemeParser", "parseXmlToDockBeanPics() has Exception = " + e.getMessage());
		}
	}

	private void parceLight(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.Light light) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.COLOR);
		light.mColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IMAGE);
		light.mResImage = stringToString(attributeValue);
	}

	private void parceFont(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.Font font) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.COLOR);
		font.mColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.BACKGROUNDCOLOR);
		font.mBGColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.SIZE);
		font.mSize = stringToInt(attributeValue);
		font.mAppearence = xmlPullParser.getAttributeValue(null, AttributeSet.APPEARANCE);
	}

	private void parceWallpaper(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.WallpaperBean wallpaper) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.COLOR);
		wallpaper.mColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IMAGE);
		wallpaper.mResName = stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.FILL);
		wallpaper.mWallpaperFill = stringToFill(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.BORDER);
		wallpaper.mBorder = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.BORDERLINE);
		wallpaper.mBorderLine = stringToBorderLine(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.BORDERCOLOR);
		wallpaper.mBorderColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.MARGINS);
		wallpaper.mMargins = deskBean.createMargins(stringToString(attributeValue));
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.PORTMARGINS);
		wallpaper.mPortMargins = deskBean.createMargins(stringToString(attributeValue));
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.LANDMARGINS);
		wallpaper.mLandMargins = deskBean.createMargins(stringToString(attributeValue));
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IDENTITY);
		wallpaper.mIdentity = stringToString(attributeValue);
	}

	private void parceLayer(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.Layer layer) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.WIDTH);
		// layer.mWidth = stringToInt(attributeValue);
		layer.setWidth(stringToInt(attributeValue));
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.HEIGHT);
		// layer.mHeight = stringToInt(attributeValue);
		layer.setHeight(stringToInt(attributeValue));
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.VALIGN);
		layer.mValign = stringToValign(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.HALIGN);
		layer.mHalign = stringToHalign(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.MARGINS);
		layer.mMargins = deskBean.createMargins(stringToString(attributeValue));
	}

	private void parceShowItemLayer(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.ShowItemLayer layer) throws XmlPullParserException, IOException {
		// 自身属性
		parceLayer(xmlPullParser, deskBean, layer);

		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(TagSet.WALLPAPER)) {
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					if (null != wallpaper.mIdentity) {
						if (wallpaper.mIdentity.equals(AttributeSet.BACKGROUND)) {
							layer.mBackImage = wallpaper;
						} else {
							layer.mForeImage = wallpaper;
						}
					} else {
						layer.mBackImage = wallpaper;
					}
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceIconStyle(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.IconStyle iconStyle) throws XmlPullParserException, IOException {
		// 自身属性

		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(TagSet.SHOWITEMLAYER)) {
					DeskThemeBean.ShowItemLayer layer = deskBean.createShowItemLayer();
					parceShowItemLayer(xmlPullParser, deskBean, layer);
					iconStyle.mIconItems.add(layer);
				} else if (tagName.equals(TagSet.LAYER)) {
					DeskThemeBean.Layer layer = deskBean.createLayer();
					parceLayer(xmlPullParser, deskBean, layer);
					iconStyle.mIconItems.add(layer);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceScreenIconStyle(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.ScreenIconStyle iconStyle) throws XmlPullParserException, IOException {
		// 自身属性
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.LIGHTMODE);
		iconStyle.mShowlightMode = stringToShowlightMode(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.CELLWIDTHPORT);
		iconStyle.mCellWidthPort = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.CELLWIDTHLAND);
		iconStyle.mCellWidthLand = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.CELLHEIGHTPORT);
		iconStyle.mCellHeightPort = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.CELLHEIGHTLAND);
		iconStyle.mCellHeightLand = stringToInt(attributeValue);
		parceLayer(xmlPullParser, deskBean, iconStyle);

		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SCREENICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(TagSet.WALLPAPER)) {
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					if (null != wallpaper.mIdentity) {
						if (wallpaper.mIdentity.equals(AttributeSet.ICON_BG)) {
							iconStyle.mIconBackgroud = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.TEXT_BG)) {
							iconStyle.mTextBackgroud = wallpaper;
						}
					} else {
						iconStyle.mIconBackgroud = wallpaper;
					}
				}
			}
			eventType = xmlPullParser.next();
		}

	}

	private void parceFolderStyle(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.FolderStyle folderStyle) throws XmlPullParserException, IOException {
		// 自身属性

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
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
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
			eventType = xmlPullParser.next();
		}
	}

	private void parceTrashLayer(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.TrashLayer trashLayer) throws XmlPullParserException, IOException {
		// 自身属性
		String attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IMAGE);
		trashLayer.mResImage = stringToString(attributeValue);
		parceShowItemLayer(xmlPullParser, deskBean, trashLayer);
	}

	private void parceTrashStyle(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.TrashStyle trashStyle) throws XmlPullParserException, IOException {
		// 自身属性
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.COLOR);
		trashStyle.mIconForeColor = parseColor(attributeValue);

		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.TRASHSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(TagSet.TRASHING)) {
					DeskThemeBean.TrashLayer layer = deskBean.createTrashLayer();
					layer.mTrashing = true;
					parceTrashLayer(xmlPullParser, deskBean, layer);
					trashStyle.mTrashingLayer = layer;
				} else if (tagName.equals(TagSet.TRASHED)) {
					DeskThemeBean.TrashLayer layer = deskBean.createTrashLayer();
					layer.mTrashing = false;
					parceTrashLayer(xmlPullParser, deskBean, layer);
					trashStyle.mTrashedLayer = layer;
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceNotifyItem(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.NotifyItem notifyItem) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.KEY);
		notifyItem.mNotifyType = stringToNotifyTypes(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.VALUE);
		notifyItem.mOpen = stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.KEYWORD);
		notifyItem.mIntentKeyWord = stringToString(attributeValue);
	}

	// private void parceNotifys(XmlPullParser xmlPullParser, DeskThemeBean
	// deskBean, DeskThemeBean.DockBean dockBean)
	// throws XmlPullParserException, IOException
	// {
	// // 子属性
	// int eventType = xmlPullParser.next();
	// while (XmlPullParser.END_DOCUMENT != eventType)
	// {
	// String tagName = xmlPullParser.getName();
	// if (XmlPullParser.END_TAG == eventType)
	// {
	// // 解析完毕
	// if (tagName.equals(TagSet.NOTIFYS))
	// {
	// break;
	// }
	// }
	// else if (XmlPullParser.START_TAG == eventType)
	// {
	// if (tagName.equals(TagSet.NOTIFYITEM))
	// {
	// DeskThemeBean.NotifyItem notifyItem = deskBean.createNotifyItem();
	// parceNotifyItem(xmlPullParser, deskBean, notifyItem);
	// dockBean.mNotifys.add(notifyItem);
	// }
	// }
	// eventType = xmlPullParser.next();
	// }
	// }

	private void parceSymtemDockItem(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.SystemDefualtItem systemDefualtItem) throws XmlPullParserException,
			IOException {
		// 自身属性
		String attribureValue = null;
		attribureValue = xmlPullParser.getAttributeValue(null, AttributeSet.GESTUREINTENT);
		systemDefualtItem.setGestureIntent(stringToString(attribureValue));
		attribureValue = xmlPullParser.getAttributeValue(null, AttributeSet.INDEX);
		systemDefualtItem.mIndex = stringToInt(attribureValue);

		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SYMTEMDOCKITEM))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(TagSet.WALLPAPER)) {
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					systemDefualtItem.mIcon = wallpaper;
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceThemeDockItem(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.ThemeDefualtItem themeDefualtItem) throws XmlPullParserException,
			IOException {
		String attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.INTENT);
		themeDefualtItem.setGestureIntent(stringToString(attributeValue));
		parceSymtemDockItem(xmlPullParser, deskBean, themeDefualtItem);
	}

	private void parceNotifyStyle(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.NotifyStyle notifyStyle) throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.VALIGN);
		notifyStyle.mValign = stringToValign(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.TEXTVALIGN);
		notifyStyle.mTextValign = stringToValign(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.MARGINS);
		notifyStyle.mMargins = deskBean.createMargins(stringToString(attributeValue));

		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.NOTIFYSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(TagSet.WALLPAPER)) {
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					notifyStyle.mTipImage = wallpaper;
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceDockSetting(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.DockSettingBean setting) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.ROWCOUNT);
		setting.mRowCount = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.ISBACKGROUND);
		setting.mIsBackground = stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.BACKGROUND);
		setting.mBackground = stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.BACKGROUNDFILL);
		setting.mBackgroundFill = stringToFill(attributeValue);
	}

	private void parceIndicatorItem(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.IndicatorItem indicatorItem) throws XmlPullParserException, IOException {
		parceLayer(xmlPullParser, deskBean, indicatorItem);

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
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					if (null != wallpaper.mIdentity
							&& wallpaper.mIdentity.equals(AttributeSet.SELECTED)) {
						indicatorItem.mSelectedBitmap = wallpaper;
					} else {
						indicatorItem.mUnSelectedBitmap = wallpaper;
					}
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceCardItem(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.CardItem cardItem) throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IDENTITY);
		cardItem.mIdentity = stringToString(attributeValue);

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
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					if (null != wallpaper.mIdentity) {
						if (wallpaper.mIdentity.equals(AttributeSet.BACKGROUND)) {
							cardItem.mBackground = wallpaper;
						} else {
							cardItem.mFore = wallpaper;
						}
					} else {
						cardItem.mBackground = wallpaper;
					}
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceCard(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.Card card) throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IDENTITY);
		card.mIdentity = stringToString(attributeValue);

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
				if (tagName.equals(TagSet.ITEM)) {
					DeskThemeBean.CardItem cardItem = deskBean.createCardItem();
					parceCardItem(xmlPullParser, deskBean, cardItem);
					card.mItem = cardItem;
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parcePreviewStyle(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.PreviewBean preview) throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.LINEITEMCOUNT);
		deskBean.mPreview.mLineItemCount = stringToInt(attributeValue);

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
				if (tagName.equals(TagSet.CARD)) {
					DeskThemeBean.Card card = deskBean.creaCard();
					parceCard(xmlPullParser, deskBean, card);
					if (null != card.mIdentity) {
						if (card.mIdentity.equals(AttributeSet.CURRENTSCREEN)) {
							preview.mCurrScreen = card;
						} else if (card.mIdentity.equals(AttributeSet.NOMALSCREEN)) {
							preview.mScreen = card;
						} else if (card.mIdentity.equals(AttributeSet.ADDSCREEN)) {
							preview.mAddScreen = card;
						} else if (card.mIdentity.equals(AttributeSet.FOCUSSCREEN)) {
							preview.mFucosScreen = card;
						} else if (card.mIdentity.equals(AttributeSet.FOCUSADDSCREEN)) {
							preview.mFocusAddScreen = card;
						} else if (card.mIdentity.equals(AttributeSet.DELETESCREEN)) {
							preview.mDeleteScreen = card;
						} else {
							preview.mScreen = card;
						}
					} else {
						preview.mScreen = card;
					}
				} else if (tagName.equals(TagSet.WALLPAPER)) {
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					if (null != wallpaper.mIdentity) {
						if (wallpaper.mIdentity.equals(AttributeSet.HOME)) {
							preview.mHome = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.NOTHOME)) {
							preview.mNotHome = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.COLSED)) {
							preview.mColsed = wallpaper;
						} else {
							preview.mColsing = wallpaper;
						}
					} else {
						preview.mColsing = wallpaper;
					}
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceMenu(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.MenuBean menu) throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IDENTITY);
		menu.mIdentity = attributeValue;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.ROW);
		menu.mRows = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.COLUMN);
		menu.mColumns = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.TEXTCOLOR);
		menu.mTextColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.SELECTTEXTCOLOR);
		menu.mTabSelectFontColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.UNSELECTTEXTCOLOR);
		menu.mTabUnselectFontColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.GLMENU_TEXT_HIGH_COLOR);
		if (attributeValue != null) {
			menu.mHighLightTextColor = parseColor(attributeValue);
		}
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
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					if (null != wallpaper.mIdentity) {
						if (wallpaper.mIdentity.equals(AttributeSet.BACKGROUND)) {
							menu.mBackground = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.ITEM_BACKGROUND)) {
							menu.mItemBackground = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.ITEM_LINE)) {
							menu.mItemLineBean = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.UNSELECT_TAB_LINE)) {
							menu.mUnselectTabLineBean = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.SELECT_TAB_LINE)) {
							menu.mSelectTabLineBean = wallpaper;
						} else if (wallpaper.mIdentity.equals(AttributeSet.NEW_MESSAGE_NOTIFY)) {
							menu.mNewMessageNotify = wallpaper;
						}
					} else {
						menu.mBackground = wallpaper;
					}
				} else if (tagName.equals(TagSet.ITEM)) {
					DeskThemeBean.MenuItemBean menuItem = deskBean.createMenuItemBean();
					parceMenuItem(xmlPullParser, deskBean, menuItem);
					if (null != menuItem.mIdentity) {
						if (menuItem.mIdentity.equals("More")) {
							menu.mMoreItem = menuItem;
						} else if (menuItem.mIdentity.equals("Back")) {
							menu.mBackItem = menuItem;
						}
					} else {
						menu.mItems.add(menuItem);
					}
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceMenuItem(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.MenuItemBean menuitem) throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.IDENTITY);
		menuitem.mIdentity = attributeValue;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.ID);
		menuitem.mId = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.TEXT);
		menuitem.mName = attributeValue;

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
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					String identity = xmlPullParser.getAttributeValue(null, AttributeSet.IDENTITY);
					if (identity != null && identity.equals(AttributeSet.GLMENU_HIGH_IMG)) {
						parceWallpaper(xmlPullParser, deskBean, wallpaper);
						menuitem.mHighColorImage = wallpaper;
					} else {
						parceWallpaper(xmlPullParser, deskBean, wallpaper);
						menuitem.mImage = wallpaper;
					}
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parcePreference(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.PreferenceAppearanceBean preferenceAppearance)
			throws XmlPullParserException, IOException {
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
				if (tagName.equals(TagSet.APPEARANCE)) {
					parcePreferenceAppearance(xmlPullParser, deskBean, preferenceAppearance);
				} else if (tagName.equals(TagSet.ITEM)) {
					parcePreferenceItem(xmlPullParser, deskBean, preferenceAppearance);
				} else if (tagName.equals(TagSet.CATEGORY)) {
					parcePreferenceCategory(xmlPullParser, deskBean, preferenceAppearance);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parcePreferenceAppearance(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.PreferenceAppearanceBean preferenceAppearance)
			throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.STYLE);
		preferenceAppearance.mTitleStyle = stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.TITLECOLOR);
		preferenceAppearance.mTitleColor = parseColor(attributeValue);

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
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					if (null != wallpaper.mIdentity) {
						if (wallpaper.mIdentity.equals("Background")) {
							preferenceAppearance.mBackground = wallpaper;
						} else if (wallpaper.mIdentity.equals("SeparateLine")) {
							preferenceAppearance.mSeparateLine = wallpaper;
						} else if (wallpaper.mIdentity.equals("Scroll")) {
							preferenceAppearance.mScroll = wallpaper;
						}
					}
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parcePreferenceItem(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.PreferenceAppearanceBean preferenceAppearance)
			throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.TITLECOLOR);
		preferenceAppearance.mItemTitleColor = parseColor(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.SUMMARYCOLOR);
		preferenceAppearance.mItemSummaryColor = parseColor(attributeValue);

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
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					preferenceAppearance.mItemBackground = wallpaper;
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parcePreferenceCategory(XmlPullParser xmlPullParser, DeskThemeBean deskBean,
			DeskThemeBean.PreferenceAppearanceBean preferenceAppearance)
			throws XmlPullParserException, IOException {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null, AttributeSet.TITLECOLOR);
		preferenceAppearance.mCategoryColor = parseColor(attributeValue);

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
					DeskThemeBean.WallpaperBean wallpaper = deskBean.createWallpaperBean();
					parceWallpaper(xmlPullParser, deskBean, wallpaper);
					preferenceAppearance.mCategoryBackground = wallpaper;
				}
			}
			eventType = xmlPullParser.next();
		}
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

	private int stringToInt(String value) {
		if (null == value) {
			return 0;
		}
		if (value.length() == 0) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	private boolean stringToBoolean(String value) {
		if (null == value) {
			return false;
		}
		if (value.equals("1") || value.equals("true") || value.equals("TRUE")) {
			return true;
		}
		return false;
	}

	private DeskThemeBean.Fill stringToFill(String value) {
		if (null == value) {
			return DeskThemeBean.Fill.None;
		}

		if (value.equals("1")) {
			return DeskThemeBean.Fill.Center;
		}
		if (value.equals("2")) {
			return DeskThemeBean.Fill.Tensile;
		}
		if (value.equals("3")) {
			return DeskThemeBean.Fill.Tensile;
		}
		if (value.equals("4")) {
			return DeskThemeBean.Fill.Nine;
		} else {
			return DeskThemeBean.Fill.None;
		}
	}

	private DeskThemeBean.Valign stringToValign(String value) {
		if (null == value) {
			return DeskThemeBean.Valign.None;
		}

		if (value.equals("1")) {
			return DeskThemeBean.Valign.Top;
		}
		if (value.equals("2")) {
			return DeskThemeBean.Valign.Mid;
		}
		if (value.equals("3")) {
			return DeskThemeBean.Valign.Botton;
		} else {
			return DeskThemeBean.Valign.None;
		}
	}

	private DeskThemeBean.Halign stringToHalign(String value) {
		if (null == value) {
			return DeskThemeBean.Halign.None;
		}

		if (value.equals("1")) {
			return DeskThemeBean.Halign.Left;
		}
		if (value.equals("2")) {
			return DeskThemeBean.Halign.Center;
		}
		if (value.equals("3")) {
			return DeskThemeBean.Halign.Right;
		} else {
			return DeskThemeBean.Halign.None;
		}
	}

	private DeskThemeBean.ShowlightMode stringToShowlightMode(String value) {
		if (null == value) {
			return DeskThemeBean.ShowlightMode.None;
		}

		if (value.equals("1")) {
			return DeskThemeBean.ShowlightMode.AndroidSytem;
		}
		if (value.equals("2")) {
			return DeskThemeBean.ShowlightMode.Light;
		} else {
			return DeskThemeBean.ShowlightMode.None;
		}
	}

	private DeskThemeBean.IndicatorShowMode stringToIndicatorShowMode(String value) {
		if (null == value) {
			return DeskThemeBean.IndicatorShowMode.None;
		}

		if (value.equals("1")) {
			return DeskThemeBean.IndicatorShowMode.Point;
		} else if (value.equals("2")) {
			return DeskThemeBean.IndicatorShowMode.Line;
		} else {
			return DeskThemeBean.IndicatorShowMode.None;
		}
	}

	private DeskThemeBean.NotifyTypes stringToNotifyTypes(String value) {
		if (null == value) {
			return DeskThemeBean.NotifyTypes.None;
		}

		if (value.equals("SMS")) {
			return DeskThemeBean.NotifyTypes.SMS;
		}
		if (value.equals("CALL")) {
			return DeskThemeBean.NotifyTypes.CALL;
		}
		if (value.equals("GMAIL")) {
			return DeskThemeBean.NotifyTypes.GMAIL;
		} else {
			return DeskThemeBean.NotifyTypes.None;
		}
	}

	private DeskThemeBean.BorderLine stringToBorderLine(String value) {
		if (null == value) {
			return DeskThemeBean.BorderLine.None;
		}

		if (value.equals("1")) {
			return DeskThemeBean.BorderLine.Solid;
		} else if (value.equals("2")) {
			return DeskThemeBean.BorderLine.Dotted;
		} else {
			return DeskThemeBean.BorderLine.None;
		}
	}

	public static int parseColor(String colorString) {
		if (colorString == null || colorString.length() == 0) {
			return Color.TRANSPARENT;
		}

		try {
			final int color = Color.parseColor(colorString);
			return color;
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
		}
		return Color.TRANSPARENT;
	}
}