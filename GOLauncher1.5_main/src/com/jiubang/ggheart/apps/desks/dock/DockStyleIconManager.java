/**
 * 
 */
package com.jiubang.ggheart.apps.desks.dock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * @author ruxueqin
 * 
 */
public class DockStyleIconManager implements ICleanable {
	private static DockStyleIconManager sManager;
	private Context mContext;

	public static DockStyleIconManager getInstance(Context context) {
		if (sManager == null) {
			sManager = new DockStyleIconManager(context);
		}
		return sManager;
	}

	public DockStyleIconManager(Context context) {
		mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jiubang.core.framework.frame.ICleanable#cleanup()
	 */
	@Override
	public void cleanup() {

	}

	/**
	 * 获取当前安装的dock风格包基础信息，每次重新扫描，调用者调用完后要对此返回列表清空
	 * 
	 * @return　
	 */
	public ArrayList<StyleBaseInfo> getAllStyleBaseInfos() {
		ArrayList<StyleBaseInfo> mList = new ArrayList<StyleBaseInfo>();

		Intent intent = new Intent(ICustomAction.ACTION_PKG_NAME);
		intent.addCategory(Intent.CATEGORY_DEFAULT);

		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> styles = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		int size = styles.size();
		for (int i = 0; i < size; i++) {
			String appPackageName = styles.get(i).activityInfo.packageName;
			String appName = (String) pm
					.getApplicationLabel(styles.get(i).activityInfo.applicationInfo);
			if (null != appPackageName && null != appName) {
				DockStylePkgInfo info = new DockStylePkgInfo();
				info.mPkgName = appPackageName;
				info.mStyleName = appName;
				mList.add(info);
			}
		}
		styles.clear();
		styles = null;

		return mList;
	}

	/**
	 * 获取指定风格包具体信息
	 * 
	 * @param pkgString
	 *            　风格包名
	 * @return　指定风格包具体信息.找不到返回null
	 */
	public DockStylePkgInfo getDockStylePkgInfo(String pkgString) {
		if (null == pkgString) {
			return null;
		}

		PackageManager pm = mContext.getPackageManager();
		try {
			PackageInfo pkgInfo = pm.getPackageInfo(pkgString, PackageManager.GET_ACTIVITIES);

			InputStream inputStream = XmlParserFactory.createInputStream(mContext, pkgString,
					DockStylePkgInfo.CONFIG_XML);
			// if(null == inputStream)
			// {
			// 找不到文件,判断为版本过旧
			// Toast.makeText(mContext,
			// mContext.getString(R.string.change_dock_version_tip),
			// Toast.LENGTH_SHORT).show();
			// }
			XmlPullParser xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
			DockStylePkgInfo info = new DockStylePkgInfo();
			// 拿基本属性
			info.mPkgName = pkgString;
			info.mStyleName = (String) pm.getApplicationLabel(pkgInfo.applicationInfo);
			if (xmlPullParser != null) {

				// 解析XML得到更多属性
				IStyleParser parser = new DockStyleParser();
				parser.parseXml(xmlPullParser, info);
				parser = null;
			}
			// 关闭inputStream
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}

			return info;
		} catch (NameNotFoundException e) {
			return null;
		}
	}
}
