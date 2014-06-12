package com.jiubang.ggheart.apps.desks.Preferences.info;

import java.util.ArrayList;

/**
 * 
 * <br>类描述:单选对象
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingSeekBarInfo extends DeskSettingBaseInfo {
	private ArrayList<DeskSettingSeekBarItemInfo> mSeekBarItemInfos;

	public ArrayList<DeskSettingSeekBarItemInfo> getSeekBarItemInfos() {
		return mSeekBarItemInfos;
	}
	public void setSeekBarItemInfos(ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfos) {
		this.mSeekBarItemInfos = seekBarItemInfos;
	}

}
