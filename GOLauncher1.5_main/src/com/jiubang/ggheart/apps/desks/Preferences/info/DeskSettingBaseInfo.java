package com.jiubang.ggheart.apps.desks.Preferences.info;

import android.graphics.drawable.Drawable;

/**
 * 
 * <br>类描述:桌面设置对象
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingBaseInfo extends DeskSettingNormalInfo {
	protected  boolean mIsAddImageBg; //是否需要给图片添加背景图
	protected int[] mImageId; //图片数组(资源id)
	protected Drawable[] mImageDrawable; //图片数组(drawable)
	protected CharSequence[] mEntries; //内容的数组
	protected CharSequence[] mEntryValues; //内容的数组对应的value数组

	protected String mCheckBoxString;	//CheckBox提示语
	protected boolean mCheckBoxIsCheck; //CheckBox是否勾选
	protected boolean mIsHaveCheckBox; //是否带有CheckBox
	
	public boolean getIsAddImageBg() {
		return mIsAddImageBg;
	}

	public void setIsAddImageBg(boolean isAddImageBg) {
		this.mIsAddImageBg = isAddImageBg;
	}

	public int[] getImageId() {
		return mImageId;
	}

	public void setImageId(int[] imageId) {
		this.mImageId = imageId;
	}

	public Drawable[] getImageDrawable() {
		return mImageDrawable;
	}

	public void setImageDrawable(Drawable[] imageDrawable) {
		this.mImageDrawable = imageDrawable;
	}

	public CharSequence[] getEntries() {
		return mEntries;
	}

	public void setEntries(CharSequence[] entries) {
		this.mEntries = entries;
	}
	public CharSequence[] getEntryValues() {
		return mEntryValues;
	}

	public void setEntryValues(CharSequence[] entryValues) {
		this.mEntryValues = entryValues;
	}
	
	public String getCheckBoxString() {
		return mCheckBoxString;
	}

	public void setCheckBoxString(String checkBoxString) {
		this.mCheckBoxString = checkBoxString;
	}
	
	public boolean getCheckBoxIsCheck() {
		return mCheckBoxIsCheck;
	}

	public void setCheckBoxIsCheck(boolean checkBoxIsCheck) {
		this.mCheckBoxIsCheck = checkBoxIsCheck;
	}
	
	

	public boolean getIsHaveCheckBox() {
		return mIsHaveCheckBox;
	}

	public void setIsHaveCheckBox(boolean isHaveCheckBox) {
		this.mIsHaveCheckBox = isHaveCheckBox;
	}
	
	/**
	 * Returns the index of the given value (in the entry values array).
	 * 
	 * @param value The value whose index should be returned.
	 * @return The index of the value, or -1 if not found.
	 */
	public int findIndexOfValue(String value) {
		if (value != null && mEntryValues != null) {
			for (int i = mEntryValues.length - 1; i >= 0; i--) {
				if (mEntryValues[i].equals(value)) {
					return i;
				}
			}
		}
		return -1;
	}
}
