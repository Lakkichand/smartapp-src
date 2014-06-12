package com.jiubang.ggheart.apps.desks.Preferences.info;

/**
 * 
 * <br>类描述:单选对象
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingGestureInfo extends DeskSettingBaseInfo {
	private String mSelectValue; //选择的值
	private String mCheckBoxString;

	public String getSelectValue() {
		return mSelectValue;
	}

	public void setSelectValue(String selectValue) {
		this.mSelectValue = selectValue;
	}

	public String getCheckBoxString() {
		return mCheckBoxString;
	}

	public void setCheckBoxString(String checkBoxString) {
		this.mCheckBoxString = checkBoxString;
	}

	/**
	 * Returns the entry corresponding to the current value.
	 * 
	 * @return The entry corresponding to the current value, or null.
	 */
	public CharSequence getEntry() {
		int index = getValueIndex();
		return index >= 0 && mEntries != null ? mEntries[index] : null;
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

	/**
	 * <br>功能简述:获取选择值在哪个位置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getValueIndex() {
		if (mSelectValue != null) {
			return findIndexOfValue(mSelectValue);
		}
		return -1;
	}

	/**
	 * <br>功能简述:获取选择值对应的内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public String getSelectValueText() {
		int index = getValueIndex();
		if (index != -1) {
			return mEntries[index].toString();
		}
		return "";
	}
}
