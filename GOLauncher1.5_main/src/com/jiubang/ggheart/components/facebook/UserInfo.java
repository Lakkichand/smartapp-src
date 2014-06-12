package com.jiubang.ggheart.components.facebook;


/**
 * 
 * <br>类描述:facebook用户信息
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-12-19]
 */
public class UserInfo {

	private String mId = null;
	private String mName = null;

	public UserInfo(String id, String name) {
		mId = id;
		mName = name;
	}

	public void setId(String id) {
		mId = id;
	}

	public void setName(String name) {
		mName = name;
	}
	
	public String getId() {
		return mId;
	}
	
	public String getName() {
		return mName;
	}
}
