package com.jiubang.ggheart.apps.desks.Preferences.info;


/**
 * 
 * <br>类描述:普通对话框对象
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingNormalInfo {
	private String mTitle;
	private String mMessage;

	public String getTitle() {
		if (mTitle == null) {
			return "";
		}
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getmMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}
}
