package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean;

import android.content.res.Resources;
/**
 * <br>类描述:壁纸二级tab bean
 */
public class WallpaperSubInfo {
	private int mType;
	private String mImageResName;
	private int mImageResId;
	private Resources mResource;
	private String mPackageName;

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}

	public String getImageResName() {
		return mImageResName;
	}

	public void setImageResName(String mImageResName) {
		this.mImageResName = mImageResName;
	}

	public int getImageResId() {
		return mImageResId;
	}

	public void setImageResId(int mImageResId) {
		this.mImageResId = mImageResId;
	}

	public Resources getResource() {
		return mResource;
	}

	public void setResource(Resources mResource) {
		this.mResource = mResource;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public void setPackageName(String mPackageName) {
		this.mPackageName = mPackageName;
	}

}
