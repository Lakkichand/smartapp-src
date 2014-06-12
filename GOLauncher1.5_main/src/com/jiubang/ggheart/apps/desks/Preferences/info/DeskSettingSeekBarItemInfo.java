package com.jiubang.ggheart.apps.desks.Preferences.info;

/**
 * 
 * <br>类描述:选择条对象
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingSeekBarItemInfo {
	private String mTitle;
	private int mMinValue;
	private int mMaxValue;
	private int mSelectValue;

	public DeskSettingSeekBarItemInfo() {

	}

	public DeskSettingSeekBarItemInfo(String title, int minValue, int maxValue, int selectValue) {
		this.mTitle = title;
		this.mMinValue = minValue;
		this.mMaxValue = maxValue;
		this.mSelectValue = selectValue;
	}

	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String title) {
		this.mTitle = title;
	}
	public int getMaxValue() {
		return mMaxValue;
	}
	public void setMaxValue(int maxValue) {
		this.mMaxValue = maxValue;
	}
	public int getMinValue() {
		return mMinValue;
	}
	public void setMinValue(int minValue) {
		this.mMinValue = minValue;
	}
	public int getSelectValue() {
		return mSelectValue;
	}
	public void setSelectValue(int selectValue) {
		this.mSelectValue = selectValue;
	}

}
