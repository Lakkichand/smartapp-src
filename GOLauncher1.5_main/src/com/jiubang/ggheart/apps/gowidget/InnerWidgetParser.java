package com.jiubang.ggheart.apps.gowidget;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;

import com.gau.go.launcherex.R;
import com.go.util.xml.XmlUtils;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @date  [2012-12-24]
 */
public final class InnerWidgetParser {
	private final static String TAG_GO_WIDGETS = "gowidgets";
	private final static String TAG_INNER_WIDGET = "innner_widget";

	public static ArrayList<InnerWidgetInfo> getInnerWidgets(Context context) {
		try {
			// long start = System.currentTimeMillis();
			XmlResourceParser parser = context.getResources().getXml(R.xml.inner_widget);
			AttributeSet attrs = Xml.asAttributeSet(parser);
			XmlUtils.beginDocument(parser, TAG_GO_WIDGETS);

			ArrayList<InnerWidgetInfo> widgetList = new ArrayList<InnerWidgetInfo>();
			final int depth = parser.getDepth();
			int type;
			while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
					&& type != XmlPullParser.END_DOCUMENT) {

				if (type != XmlPullParser.START_TAG) {
					continue;
				}

				TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InnerWidget);
				final String name = parser.getName();
				if (TAG_INNER_WIDGET.equals(name)) {
					final InnerWidgetInfo innerWidgetInfo = getInnerWidgetInfo(a, context);
					if (innerWidgetInfo != null) {
						widgetList.add(innerWidgetInfo);
					}
				}
				a.recycle();
			}
			parser.close();
			parser = null;

			// Log.i("luoph", "parse innner widget cost = " +
			// (System.currentTimeMillis() - start));

			return widgetList;
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}

		return null;
	}

	private static InnerWidgetInfo getInnerWidgetInfo(TypedArray a, Context context) {
		if (a == null) {
			return null;
		}

		InnerWidgetInfo info = new InnerWidgetInfo();
		info.mPrototype = a.getInt(R.styleable.InnerWidget_prototype,
				GoWidgetBaseInfo.PROTOTYPE_NORMAL);
		info.mBuildin = a.getInt(R.styleable.InnerWidget_buildin, InnerWidgetInfo.BUILDIN_ALL);
		info.mInflatePkg = a.getString(R.styleable.InnerWidget_inflatePackage);
		info.mWidgetPkg = a.getString(R.styleable.InnerWidget_widgetPackage);

		Resources resources = context.getResources();
		int labelId = a.getResourceId(R.styleable.InnerWidget_title, 0);
		// 标题
		if (labelId > 0) {
			info.mTitle = resources.getString(labelId);
			if (!ChannelConfig.getInstance(context).isNeedAppCenter()) {
				info.mTitle = resources.getString(R.string.go_store);
			}
		}

		// 图标
		info.mIconId = a.getResourceId(R.styleable.InnerWidget_icon, 0);

		if (info.mBuildin == InnerWidgetInfo.BUILDIN_ALL) {
			info.mThemeConfig = a.getString(R.styleable.InnerWidget_themeConfig);
			info.mStatisticPackage = a.getString(R.styleable.InnerWidget_statisticPackage);
			info.mPreviewList = a.getResourceId(R.styleable.InnerWidget_previewList, 0);
			info.mNameList = a.getResourceId(R.styleable.InnerWidget_nameList, 0);
			info.mTypeList = a.getResourceId(R.styleable.InnerWidget_typeList, 0);
			info.mLayoutList = a.getResourceId(R.styleable.InnerWidget_layoutList, 0);
			info.mRowList = a.getResourceId(R.styleable.InnerWidget_rowList, 0);
			info.mColumnList = a.getResourceId(R.styleable.InnerWidget_columnList, 0);
			info.mMinHeightList = a.getResourceId(R.styleable.InnerWidget_minHeightList, 0);
			info.mMinWidthList = a.getResourceId(R.styleable.InnerWidget_minWidthList, 0);
			info.mConfigList = a.getResourceId(R.styleable.InnerWidget_configList, 0);
			info.mConfigName = a.getString(R.styleable.InnerWidget_configName);
		}
		return info;
	}

	public static ArrayList<WidgetParseInfo> getWidgetParseInfos(Context context,
			InnerWidgetInfo info) {
		if (info != null && info.mBuildin == InnerWidgetInfo.BUILDIN_ALL) {
			Resources resources = context.getResources();
			if (resources != null) {
				ArrayList<WidgetParseInfo> parseInfos = new ArrayList<WidgetParseInfo>();
				try {
					// 获取图片
					final String[] extras = resources.getStringArray(info.mPreviewList);
					for (String extra : extras) {
						int res = resources.getIdentifier(extra, "drawable", info.mWidgetPkg);
						if (res != 0) {
							WidgetParseInfo item = new WidgetParseInfo();
							item.resouceId = res;
							item.resouces = resources;
							item.themePackage = null;
							parseInfos.add(item);
						}
					}

					if (parseInfos.size() == 0) {
						parseInfos.clear();
						parseInfos = null;
						return null;
					}

					// 获取图片文字
					final String[] titles = resources.getStringArray(info.mNameList);
					int count = 0;
					for (String titl : titles) {
						int res = resources.getIdentifier(titl, "string", info.mWidgetPkg);
						if (res != 0) {
							WidgetParseInfo item = parseInfos.get(count);
							item.title = resources.getString(res);
							count++;
						}
					}

					// 获取类型
					final int[] typeLists = resources.getIntArray(info.mTypeList);
					count = 0;
					for (int types : typeLists) {
						WidgetParseInfo item = parseInfos.get(count);
						item.type = types;
						item.styleType = String.valueOf(types);
						count++;
					}

					// 获取行数
					final int[] rowLists = resources.getIntArray(info.mRowList);
					count = 0;
					for (int row : rowLists) {
						WidgetParseInfo item = parseInfos.get(count);
						item.mRow = row;
						count++;
					}

					// 获取列数
					final int[] colListS = resources.getIntArray(info.mColumnList);
					count = 0;
					for (int col : colListS) {
						WidgetParseInfo item = parseInfos.get(count);
						item.mCol = col;
						count++;
					}

					// 获取layout id
					final String[] layouIds = resources.getStringArray(info.mLayoutList);
					count = 0;
					for (String id : layouIds) {
						WidgetParseInfo item = parseInfos.get(count);
						item.layoutID = id;
						count++;
					}

					// 获取竖屏最小宽度
					final int[] widthIds = resources.getIntArray(info.mMinWidthList);
					count = 0;
					for (int w : widthIds) {
						WidgetParseInfo item = parseInfos.get(count);
						item.minWidth = w;
						count++;
					}

					// 获取竖屏最小高度
					final int[] heightIds = resources.getIntArray(info.mMinHeightList);
					count = 0;
					for (int h : heightIds) {
						WidgetParseInfo item = parseInfos.get(count);
						item.minHeight = h;
						count++;
					}

					// 设置
					if (info.mConfigList > 0) {
						final String[] configids = resources.getStringArray(info.mConfigList);
						count = 0;
						for (String id : configids) {
							WidgetParseInfo item = parseInfos.get(count);
							item.configActivty = id;
							count++;
						}
					}

					// 长按设置
					if (info.mSettingList > 0) {
						final String[] settingids = resources.getStringArray(info.mSettingList);
						count = 0;
						for (String id : settingids) {
							WidgetParseInfo item = parseInfos.get(count);
							item.longkeyConfigActivty = id;
							count++;
						}
					}
				} catch (NotFoundException e) {
				}

				return parseInfos;
			}
		}
		return null;
	}

}
