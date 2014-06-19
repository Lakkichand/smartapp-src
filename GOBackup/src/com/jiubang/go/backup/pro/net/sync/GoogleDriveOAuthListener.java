package com.jiubang.go.backup.pro.net.sync;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;

/**
 * 
 * @author ReyZhang
 *
 */
public interface GoogleDriveOAuthListener {
	public void onOAuthSuccess(GoogleTokenResponse response, boolean isUpdate,
			ActionListener listener);

	public void onOAuthFail(String msg, int errorCode, ActionListener listener);
}
