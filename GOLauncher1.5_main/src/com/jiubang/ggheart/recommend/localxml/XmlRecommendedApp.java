package com.jiubang.ggheart.recommend.localxml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;

import com.gau.go.launcherex.R;
import com.go.util.xml.XmlUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.StaticScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.Statistics;
/**
 * 
 * <br>类描述:本地推荐工具
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-12-27]
 */
public class XmlRecommendedApp {

	private final static String TAG_RECOMMENDED_APPS = "recommendedapps";
	private final static String TAG_RECOMMENDED_APP = "recommended_app";
	private static HashMap<Integer, ArrayList<XmlRecommendedAppInfo>> sRecommendedAppMap;

	public static void praseRecommendedApp() {
		try {
			Context context = GoLauncher.getContext();
			XmlResourceParser parser = context.getResources().getXml(R.xml.recommendedapp);
			AttributeSet attrs = Xml.asAttributeSet(parser);
			XmlUtils.beginDocument(parser, TAG_RECOMMENDED_APPS);

			sRecommendedAppMap = new HashMap<Integer, ArrayList<XmlRecommendedAppInfo>>();
			final int depth = parser.getDepth();
			int type;
			while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
					&& type != XmlPullParser.END_DOCUMENT) {

				if (type != XmlPullParser.START_TAG) {
					continue;
				}

				TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecommendedApp);
				final String name = parser.getName();
				if (TAG_RECOMMENDED_APP.equals(name)) {
					final XmlRecommendedAppInfo appInfo = getRecommendedAppInfo(a, context);
					if (appInfo != null) {
						ArrayList<XmlRecommendedAppInfo> list = sRecommendedAppMap.get(Integer
								.valueOf(appInfo.mGroup));
						if (list != null) {
							list.add(appInfo);
						} else {
							list = new ArrayList<XmlRecommendedAppInfo>();
							list.add(appInfo);
							sRecommendedAppMap.put(appInfo.mGroup, list);
						}
					}
				}
				a.recycle();
			}
			parser.close();
			parser = null;
			sortList();
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
	}

	private static XmlRecommendedAppInfo getRecommendedAppInfo(TypedArray a, Context context) {
		if (a == null) {
			return null;
		}

		XmlRecommendedAppInfo info = new XmlRecommendedAppInfo();
		info.mTitle = a.getResourceId(R.styleable.RecommendedApp_titleid, 0);
		// 图标
		info.mIconId = a.getResourceId(R.styleable.RecommendedApp_iconid, 0);

		info.mPackagename = a.getString(R.styleable.RecommendedApp_packagename);
		info.mAction = a.getString(R.styleable.RecommendedApp_action);
		info.mDownloadUrl = a.getString(R.styleable.RecommendedApp_download_url);
		// 修改：列数也支持取负值，负值则为倒数
		int group = a.getInt(R.styleable.RecommendedApp_group, -1);
		if (group < 0) {
			info.mGroup = StaticScreenSettingInfo.sScreenCulumn + group;
		} else if (group > StaticScreenSettingInfo.sScreenCulumn - 1) {
			info.mGroup = StaticScreenSettingInfo.sScreenCulumn - 1;
		} else {
			info.mGroup = group;
		}
		// end 修改
		info.mPriority = a.getInt(R.styleable.RecommendedApp_priority, 0);
		info.mAppId = a.getInt(R.styleable.RecommendedApp_appid, 0);
		info.mChannelId = a.getString(R.styleable.RecommendedApp_channelid);
		int index = a.getInt(R.styleable.RecommendedApp_rowindex, -1);
		if (index < 0) {
			info.mRowIndex = StaticScreenSettingInfo.sScreenRow + index;
		} else if (index > StaticScreenSettingInfo.sScreenRow - 1) {
			info.mRowIndex = StaticScreenSettingInfo.sScreenRow - 1;
		} else {
			info.mRowIndex = index;
		}
		info.mScreenIndex = a.getInt(R.styleable.RecommendedApp_screenindex,
				ScreenSettingInfo.DEFAULT_MAIN_SCREEN);
		info.mSTime = a.getString(R.styleable.RecommendedApp_stime);
		info.mETime = a.getString(R.styleable.RecommendedApp_etime);
		info.mActType = a.getInt(R.styleable.RecommendedApp_acttype, -1);
		if (info.mActType == -1) {
			if (GoStorePhoneStateUtil.is200ChannelUid(context)) {
				info.mActType = XmlRecommendedAppInfo.GOTO_MARKET;
			} else {
				info.mActType = XmlRecommendedAppInfo.GOTO_FTP;
			}
		}
		info.mShowInstallIcon = a.getBoolean(R.styleable.RecommendedApp_showinstallicon, false);
		
		// 推荐应用的描述信息
//		info.mDescription = a.getString(R.styleable.RecommendedApp_description);
		
		//实时统计参数
		info.mClickurl = a.getString(R.styleable.RecommendedApp_clickurl);
		info.mId = a.getString(R.styleable.RecommendedApp_id);
		info.mMapId = a.getString(R.styleable.RecommendedApp_mapid);
		return info;
	}

	@SuppressWarnings("unchecked")
	private static void sortList() {
		Iterator iter = sRecommendedAppMap.entrySet().iterator();
		ListComparator comparator = new ListComparator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			ArrayList<XmlRecommendedAppInfo> list = (ArrayList<XmlRecommendedAppInfo>) entry
					.getValue();
			Collections.sort(list, comparator);
		}
	}
/**
 * 
 * <br>类描述:排序工具
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-12-27]
 */
	@SuppressWarnings("rawtypes")
	public static class ListComparator implements Comparator {

		@Override
		public int compare(Object object1, Object object2) {
			// TODO Auto-generated method stub
			XmlRecommendedAppInfo info1 = (XmlRecommendedAppInfo) object1;
			XmlRecommendedAppInfo info2 = (XmlRecommendedAppInfo) object2;
			if (info1.mPriority == info2.mPriority) {
				return 0;
			} else if (info1.mPriority < info2.mPriority) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	public static HashMap<Integer, ArrayList<XmlRecommendedAppInfo>> getRecommendedAppMap() {

		if (sRecommendedAppMap == null) {
			praseRecommendedApp();
		}
		return sRecommendedAppMap;
	}

	public static XmlRecommendedAppInfo getRecommededAppInfoByPackage(String packageName) {
		if (sRecommendedAppMap == null) {
			praseRecommendedApp();
		}
		Iterator iter = sRecommendedAppMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			ArrayList<XmlRecommendedAppInfo> list = (ArrayList<XmlRecommendedAppInfo>) entry
					.getValue();
			for (int i = 0; i < list.size(); i++) {
				XmlRecommendedAppInfo info = list.get(i);
				if (null != info && null != info.mPackagename
						&& info.mPackagename.equals(packageName)) {
					return info;
				}
			}
		}
		return null;
	}
	public static XmlRecommendedAppInfo getRecommededAppInfoByAction(String action) {
		if (sRecommendedAppMap == null) {
			praseRecommendedApp();
		}
		Iterator iter = sRecommendedAppMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			ArrayList<XmlRecommendedAppInfo> list = (ArrayList<XmlRecommendedAppInfo>) entry
					.getValue();
			for (int i = 0; i < list.size(); i++) {
				XmlRecommendedAppInfo info = list.get(i);
				if (null != info
						&& null != info.mAction
						&& info.mAction.equals(action)
						&& Statistics.getUid(GoLauncher.getContext()).equals(
								info.mChannelId)) {
					return info;
				}
			}
		}
		return null;
	}
}
