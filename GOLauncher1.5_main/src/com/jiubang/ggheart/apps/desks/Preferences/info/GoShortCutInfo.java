package com.jiubang.ggheart.apps.desks.Preferences.info;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.jiubang.ggheart.data.info.IItemType;

/**
 * 快捷方式对象
 * @author licanhui
 *
 */
public class GoShortCutInfo {
	/**
	 * The intent used to start the application
	 */
	public Intent mIntent;
	
	/**
	 * 每个goShortCut对应的值
	 */
	public String mShortCutId;
	
	/**
	 * 标题
	 */
	public CharSequence mTitle;
	
	/**
	 * 对应的图片
	 */
	public Drawable mIcon;
	
	/**
	 * One of {@link IItemType#ITEM_TYPE_APPLICATION},
	 * {@link IItemType#ITEM_TYPE_SHORTCUT},
	 * {@link IItemType#ITEM_TYPE_USER_FOLDER}, or
	 * {@link IItemType#ITEM_TYPE_APPWIDGET}.
	 */
	public int mItemType = IItemType.ITEM_TYPE_SHORTCUT;

	public Intent getIntent() {
		return mIntent;
	}

	public void setmIntent(Intent intent) {
		this.mIntent = intent;
	}

	public String getShortCutId() {
		return mShortCutId;
	}

	public void setmShortCutId(String shortCutId) {
		this.mShortCutId = shortCutId;
	}

	public CharSequence getTitle() {
		return mTitle;
	}

	public void setmTitle(CharSequence title) {
		this.mTitle = title;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public void setmIcon(Drawable icon) {
		this.mIcon = icon;
	}

	public int getItemType() {
		return mItemType;
	}

	public void setItemType(int itemType) {
		this.mItemType = itemType;
	}
	
	
}
