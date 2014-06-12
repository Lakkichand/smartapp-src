package com.jiubang.ggheart.apps.desks.appfunc.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;

/**
 * 一个数据结构，当功能表拖动图标到小房子，这个类作为一个载体附加到Message中。
 * 
 * @author wenjiaming
 * 
 */
public class AppIconForMsg {
	public Drawable mImage;

	public String mIconName;

	public int mX;

	public int mY;

	public Intent mIntent;

	public int mType;

	public long mId;

	public int mWidth;

	public int mHeight;

	public AppItemInfo mInfo;

	public AppIconForMsg(Drawable mImage, String mIconName, int mX, int mY, int width, int height,
			int type, long mId, Intent mIntent) {
		this.mImage = mImage;
		this.mIconName = mIconName;
		this.mX = mX;
		this.mY = mY;
		this.mWidth = width;
		this.mHeight = height;
		this.mType = IItemType.ITEM_TYPE_APPLICATION;
		this.mIntent = mIntent;
	}
}
