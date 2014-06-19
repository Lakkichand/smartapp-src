package com.jiubang.go.backup.pro.net.sync;

import com.dropbox.client2.DropboxAPI.Account;

/**
 * Dropbox账号
 *
 * @author maiyongshen
 */
public class DropboxAccount implements AccountInfo {
	private String mName;
	private long mUserId = -1;

	public DropboxAccount(Account account) {
		if (account == null) {
			throw new IllegalArgumentException("invalid argument");
		}
		mName = account.displayName;
		mUserId = account.uid;
	}

	public DropboxAccount(long uid, String name) {
		mUserId = uid;
		mName = name;
	}

	@Override
	public Object getUID() {
		return mUserId;
	}

	@Override
	public String getDisplayName() {
		return mName;
	}

}
