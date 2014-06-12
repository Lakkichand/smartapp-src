package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;

/**
 * 一个数据结构，当功能表拖动图标到小房子，这个类作为一个载体附加到Message中。
 * 
 * @author wenjiaming
 * 
 */

public class FolderIconForMsg {

	public long mFolderId;
	public Drawable mImage;
	public String mIconName;
	public int mX;
	public int mY;
	public ArrayList<AppItemInfo> mAppItemInfoList;
	public int mType;
	public int mWidth;
	public int mHeight;

	public FolderIconForMsg(long folderId, Drawable mImage, String mIconName, int x, int y,
			int width, int height, ArrayList<AppItemInfo> appItemInfoList) {
		this.mFolderId = folderId;
		this.mImage = mImage;
		this.mIconName = mIconName;
		this.mX = x;
		this.mY = y;
		this.mWidth = width;
		this.mHeight = height;
		this.mAppItemInfoList = appItemInfoList;
		mType = IItemType.ITEM_TYPE_USER_FOLDER;
	}

}
