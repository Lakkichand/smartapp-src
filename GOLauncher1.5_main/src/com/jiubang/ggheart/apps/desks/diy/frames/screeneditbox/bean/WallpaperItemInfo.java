package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean;

import android.content.Intent;
import android.graphics.drawable.Drawable;
/**
 * 
 * <br>类描述:壁纸一级tab bean
 */
public class WallpaperItemInfo {
	private String mAppLabel; // 应用程序标签
	private Drawable mAppIcon; // 应用程序图像
	private Intent mIntent; // 启动应用程序的Intent
	private String mPkgName; // 应用程序所对应的包名

	public String getmAppLabel() {
		return mAppLabel;
	}

	public void setAppLabel(String mAppLabel) {
		this.mAppLabel = mAppLabel;
	}

	public Drawable getAppIcon() {
		return mAppIcon;
	}

	public void setAppIcon(Drawable mAppIcon) {
		this.mAppIcon = mAppIcon;
	}

	public Intent getIntent() {
		return mIntent;
	}

	public void setIntent(Intent mIntent) {
		this.mIntent = mIntent;
	}

	public String getPkgName() {
		return mPkgName;
	}

	public void setPkgName(String mPkgName) {
		this.mPkgName = mPkgName;
	}

}
