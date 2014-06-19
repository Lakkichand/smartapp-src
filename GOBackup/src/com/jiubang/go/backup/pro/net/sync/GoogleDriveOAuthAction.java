package com.jiubang.go.backup.pro.net.sync;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;

/**
 *
 * @author ReyZhang
 *
 */
public class GoogleDriveOAuthAction extends AsyncTask<String, Void, GoogleTokenResponse> {

	private GoogleDriveOAuthListener mListener;
	private Context mContext;

	private static final int RESOPNSE_CODE_IS_NULL = 0x2022;
	private static final int IOEXCEPTION = 0x2023;

	/**
	 * Global instance of the HTTP transport.
	 */
	private HttpTransport mHttpTransport;

	/**
	 * Global instance of the JSON factory.
	 */
	private JacksonFactory mJsonFactory;

	private GoogleClientSecrets mClientSecrets;


	private boolean mIsUpdate = false;
	private ActionListener mActionListener;

	public GoogleDriveOAuthAction(Context context, GoogleClientSecrets clientSecrets,
			HttpTransport httpTransport, JacksonFactory jsonFactory, ActionListener actionListener,
			GoogleDriveOAuthListener listener) {
		mContext = context;
		mListener = listener;
		mClientSecrets = clientSecrets;
		mHttpTransport = httpTransport;
		mJsonFactory = jsonFactory;
		mActionListener = actionListener;
	}

	@Override
	protected GoogleTokenResponse doInBackground(String... code) {
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(mHttpTransport,
				mJsonFactory, mClientSecrets, GoogleDriveManager.SCOPES)
				.setAccessType("offline").build();
		GoogleTokenResponse response = null;
		if (!TextUtils.isEmpty(code[0])) {
			GoogleRefreshTokenRequest googleRefreshTokenRequest = new GoogleRefreshTokenRequest(
					mHttpTransport, mJsonFactory, code[1],
					GoogleDriveManager.CLIENT_ID,
					GoogleDriveManager.CLIENT_SECRET);
			try {
				response = googleRefreshTokenRequest.execute();
				mIsUpdate = true;
			} catch (IOException e) {
				e.printStackTrace();
				mListener.onOAuthFail("IOExcetion", IOEXCEPTION, mActionListener);
				return null;
			}
		} else {
			try {
				response = flow.newTokenRequest(code[1])
						.setRedirectUri(GoogleDriveManager.REDIRECT_URI).execute();
			} catch (IOException e) {
				e.printStackTrace();
				mListener.onOAuthFail("IOExcetion", IOEXCEPTION, mActionListener);
				return null;
			}
		}
		return response;
	}

	@Override
	protected void onPostExecute(GoogleTokenResponse result) {
		super.onPostExecute(result);
		if (result == null) {
			mListener.onOAuthFail("responseCode is null", RESOPNSE_CODE_IS_NULL, mActionListener);
			return;
		}

		mListener.onOAuthSuccess(result, mIsUpdate, mActionListener);
		mIsUpdate = false;
	}
}
