package com.jiubang.ggheart.apps.desks.appfunc.model;

import android.graphics.Bitmap;

/**
 * 桌面缩略图数据对象类型
 * 
 * @author huangshaotao
 * 
 */
public class AppFuncScreenItemInfo {
	public int mIndex;// 屏幕索引
	private Bitmap mScreenBmp;// 屏幕预览图
	private int mVancantCellCnt;// 空格单元个数

	public boolean hasVancantCell() {
		return mVancantCellCnt > 0;
	}

	public void setScreenPreviewData(Bitmap bmp, int index) {
		mScreenBmp = bmp;
		mIndex = index;
	}

	public void setScreenBmp(Bitmap bmp) {
		mScreenBmp = bmp;
	}

	public void setVancantCellCnt(int count) {
		mVancantCellCnt = count;
	}

	public Bitmap getScreenPreviewBmp() {
		return mScreenBmp;
	}

	public int getScreenindex() {
		return mIndex;
	}

	public void recycleBmp() {
		if (mScreenBmp != null && !mScreenBmp.isRecycled()) {
			mScreenBmp.recycle();
			mScreenBmp = null;
		}
	}
}
