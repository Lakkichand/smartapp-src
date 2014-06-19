package com.jiubang.go.backup.pro.net.sync;

import com.google.api.services.oauth2.model.Userinfo;


/**
 * @author ReyZhang
 */
public class GoogleDriveAccount implements AccountInfo {
	private String mUserId;
	private String mUserName;

	public GoogleDriveAccount(String userId, String userName) {
		mUserId = userId;
		mUserName = userName;
	}

	public GoogleDriveAccount(Userinfo userInfo) {
		if (userInfo == null) {
			throw new IllegalArgumentException("invalid google userInfo");
		}
		mUserId = userInfo.getId();
		mUserName = userInfo.getName();
	}

	@Override
	public Object getUID() {
		return mUserId;
	}

	@Override
	public String getDisplayName() {
		return mUserName;
	}

}
