package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.List;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 
 * @author tanshu
 * 
 */
public class AppBasicInfo {
	private Drawable mPic; // TODO:去掉
	private BitmapDrawable mMImage;
	private String mName;
	private String mPkgName; // TODO:去掉
	private Intent mIntent;
	private boolean mIsFolder;
	private List<AppBasicInfo> mAppList;

	public AppBasicInfo(BitmapDrawable image, Drawable pic, String name, String pkg, Intent intent) {
		mMImage = image;
		mPic = pic;
		mName = name;
		mPkgName = pkg;
		mIntent = intent;
		mIsFolder = false;

	}

	public Drawable getPic() {
		return mPic;
	}

	public String getName() {
		return mName;
	}

	public BitmapDrawable getMImage() {
		return mMImage;
	}

	public String getPkg() {
		return mPkgName;
	}

	public boolean isFolder() {
		return mIsFolder;
	}

	public void setAppList(List<AppBasicInfo> appList) {
		mAppList = appList;
	}

	public void setIsFolder() {
		mIsFolder = true;
	}

	public List<AppBasicInfo> getAppList() {
		return mAppList;
	}

	public Intent getIntent() {
		return mIntent;
	}

}
