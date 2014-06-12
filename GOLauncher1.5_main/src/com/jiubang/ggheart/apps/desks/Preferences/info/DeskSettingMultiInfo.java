package com.jiubang.ggheart.apps.desks.Preferences.info;

/**
 * 
 * <br>类描述:多选对象
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingMultiInfo extends DeskSettingBaseInfo {
	private String[] mSelectValues; //选择的值

	public String[] getSelectValues() {
		return mSelectValues;
	}

	public void setSelectValues(String[] selectValues) {
		this.mSelectValues = selectValues;
	}
	
	/**
	 * <br>功能简述:获取勾选了哪个选项列表
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean[] getValueIndex() {
		boolean[] selectedGroup = null;
		if (mSelectValues != null && mEntries != null) {
			selectedGroup = new boolean[mEntries.length];
			int size = mSelectValues.length;
			for (int i = 0; i < size; i++) {
				int index = findIndexOfValue(mSelectValues[i]);
				if (index != -1) {
					selectedGroup[index] = true;
				}
			}
		}
		return selectedGroup;
	}
	
}
