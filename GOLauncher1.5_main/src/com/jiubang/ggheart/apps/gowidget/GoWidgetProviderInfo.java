package com.jiubang.ggheart.apps.gowidget;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;

public class GoWidgetProviderInfo {
	public AppWidgetProviderInfo mProvider;
	public InnerWidgetInfo mInnerWidgetInfo;
	public String mDownloadUrl; // 下载地址
	public String mIconPath; // 图标地址
	public int mVersionCode; // 版本号
	public String mVersionName; // 版本名称
	public String mGoWidgetPkgName; // gowidget包名

	// 如果是内置weidget，mInnerWidgetInfo 不为空
	public GoWidgetProviderInfo() {
		mProvider = new AppWidgetProviderInfo();
	}

	public GoWidgetProviderInfo(String packageName, String className) {
		mProvider = new AppWidgetProviderInfo();
		mProvider.provider = new ComponentName(packageName, className);
	}
}
