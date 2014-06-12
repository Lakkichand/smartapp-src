package com.jiubang.ggheart.apps.desks.Preferences.info;

import java.util.HashMap;

import com.jiubang.ggheart.apps.font.FontBean;

/**
 * 
 * <br>类描述:单选字体对象
 * <br>功能详细描述:
 * 
 * @author  kuanghaojun
 * @date  [2012-9-24]
 */
public class DeskSettingFontSingleInfo extends DeskSettingSingleInfo {
	private HashMap<String, FontBean> mFontBeanMap;

	public HashMap<String, FontBean> getmFontBeanMap() {
		return mFontBeanMap;
	}
	public void setmFontBeanMap(HashMap<String, FontBean> mFontBeanMap) {
		this.mFontBeanMap = mFontBeanMap;
	}
}
