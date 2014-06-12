package com.jiubang.ggheart.data.theme.bean;

import android.graphics.drawable.Drawable;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-8]
 */
public class ThemeNotifyBean {
	private boolean mShowMenu = false; //是否在菜单上显示new
	private long mShowStartTime; //显示开始时间
	private long mShowEndTime;
	private String mShowContent; //显示内容
	private String mShowIconUrl;
	private Drawable mDrawable;
	private int mType;
	public ThemeNotifyBean() {
		// TODO Auto-generated constructor stub
	}

	public void setIsShowMenu(boolean bool) {
		mShowMenu = bool;
	}

	public void setShowStatTime(long time) {
		mShowStartTime = time;
	}

	public void setShowEndTime(long time) {
		mShowEndTime = time;
	}

	public void setShowIconUrl(String url) {
		mShowIconUrl = url;
	}

	public boolean getIsShowMenu() {
		return mShowMenu;
	}

	public long getShowStatTime() {
		return mShowStartTime;
	}

	public long getShowEndTime() {
		return mShowEndTime;
	}

	public String getShowIconUrl() {
		return mShowIconUrl;
	}

	public Drawable getShowDrawable() {
		return mDrawable;
	}

	public void setShowDrawable(Drawable drawable) {
		mDrawable = drawable;
	}

	public void setType(int type) {
		mType = type;
	}

	public int getType() {
		return mType;
	}

	public String getShowContent() {
		return mShowContent;
	}

	public void setShowContent(String content) {
		mShowContent = content;
	}
}
