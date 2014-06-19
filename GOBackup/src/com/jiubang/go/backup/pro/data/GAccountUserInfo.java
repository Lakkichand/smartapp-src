package com.jiubang.go.backup.pro.data;

/**
 * Go账号用户信息
 * 
 * @author ReyZhang
 */
public class GAccountUserInfo {

	public String userName = "";
	public String userID = "";
	private static GAccountUserInfo mInstance = null;

	private GAccountUserInfo() {

	}

	public static synchronized GAccountUserInfo getInstance() {
		if (mInstance == null) {
			mInstance = new GAccountUserInfo();
		}
		return mInstance;
	}

}
