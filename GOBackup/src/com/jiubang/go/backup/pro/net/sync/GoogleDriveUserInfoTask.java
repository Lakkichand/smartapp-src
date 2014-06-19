package com.jiubang.go.backup.pro.net.sync;

import java.io.IOException;

import android.os.AsyncTask;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;

/**
 *
 * @author ReyZhang
 *
 */
public class GoogleDriveUserInfoTask extends AsyncTask<Credential, Void, AccountInfo> {
	private ActionListener mListener;
	private int mErrorCode;
	private String mErrorMessage;

	public GoogleDriveUserInfoTask(ActionListener actionListener) {
		mListener = actionListener;
	}

	public static AccountInfo getAccount(Credential credential) throws IOException {
		Oauth2 userInfoService = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(),
				credential).build();
		Userinfo userInfo = userInfoService.userinfo().get().execute();
		return new GoogleDriveAccount(userInfo);
	}

	@Override
	protected AccountInfo doInBackground(Credential... credential) {
		try {
			return getAccount(credential[0]);
		} catch (IOException e) {
			mErrorCode = FileHostingServiceProvider.SERVER_UNAVALIABLE_ERROR;
			mErrorMessage = "cannot access server";
		}
		return null;
	}

	@Override
	protected void onPostExecute(AccountInfo account) {
		if (mListener == null) {
			return;
		}
		if (account == null) {
			mListener.onError(mErrorCode, mErrorMessage, account);
		} else {
			mListener.onComplete(account);
		}
	}
}
