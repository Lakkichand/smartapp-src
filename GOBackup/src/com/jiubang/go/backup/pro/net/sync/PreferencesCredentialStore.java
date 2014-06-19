package com.jiubang.go.backup.pro.net.sync;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

/**
 *
 * @author maiyongshen
 *
 */
public class PreferencesCredentialStore implements CredentialStore {

	private static final String GOOGLE_DRIVE_ACCOUNT_PREFS = "google_account_prefs";

	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String EXPIRATION_TIME = "expiration_time";
	private static final String USER_ID = "user_id";
	private static final String USER_NAME = "user_name";
	private static final String AUTHORIZATION_CODE = "authroization_code";

	private SharedPreferences mPrefs;

	public PreferencesCredentialStore(Context context) {
		mPrefs = context.getSharedPreferences(GOOGLE_DRIVE_ACCOUNT_PREFS,
				Context.MODE_PRIVATE);
	}

	public GoogleTokenResponse read() {
		GoogleTokenResponse googleTokenResponse = new GoogleTokenResponse();
		googleTokenResponse.setAccessToken(mPrefs.getString(ACCESS_TOKEN, ""));
		googleTokenResponse.setRefreshToken(mPrefs.getString(REFRESH_TOKEN, ""));
		return googleTokenResponse;
	}

	@Override
	public void delete(String userId, Credential credential) throws IOException {
		Editor editor = mPrefs.edit();
		editor.remove(ACCESS_TOKEN);
		editor.remove(REFRESH_TOKEN);
		editor.remove(USER_ID);
		editor.remove(AUTHORIZATION_CODE);
		editor.commit();
	}

	public void saveAccountInfo(AccountInfo account) {
		if (account == null) {
			return;
		}
		Editor editor = mPrefs.edit();
		editor.putString(USER_NAME, account.getDisplayName());
		editor.putString(USER_ID, account.getUID().toString());
		editor.commit();
	}

	public AccountInfo loadAccountInfo() {
		String userId = mPrefs.getString(USER_ID, null);
		String userName = mPrefs.getString(USER_NAME, null);
		return new GoogleDriveAccount(userId, userName);
	}

	@Override
	public boolean load(String userId, Credential credential) throws IOException {
		if (credential == null) {
			return false;
		}
		String accessToken = mPrefs.getString(ACCESS_TOKEN, null);
		credential.setAccessToken(accessToken);
		String refreshToken = mPrefs.getString(REFRESH_TOKEN, null);
		credential.setRefreshToken(refreshToken);
		long expirationTime = mPrefs.getLong(EXPIRATION_TIME, -1);
		credential.setExpirationTimeMilliseconds(expirationTime);
		return true;
	}

	@Override
	public void store(String userId, Credential credential) throws IOException {
		Editor editor = mPrefs.edit();
		String accessToken = credential.getAccessToken();
		if (!TextUtils.isEmpty(accessToken)) {
			editor.putString(ACCESS_TOKEN, accessToken);
		}
		String refreshToken = credential.getRefreshToken();
		if (!TextUtils.isEmpty(refreshToken)) {
			editor.putString(REFRESH_TOKEN, refreshToken);
		}
		editor.putLong(EXPIRATION_TIME, credential.getExpirationTimeMilliseconds());
		editor.commit();
	}
}
