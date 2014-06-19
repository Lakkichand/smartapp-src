package com.jiubang.go.backup.pro.net.sync;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

/**
 * 网络备份登陆Activity
 * 
 * @author maiyongshen
 */
public class NetBackupLoginActivity extends Activity {
	/**
	 * The extra that goes in an intent to provide your consumer key for Dropbox authentication. You
	 * won't ever have to use this.
	 */
	public static final String EXTRA_CONSUMER_KEY = "CONSUMER_KEY";

	/**
	 * The extra that goes in an intent when returning from Dropbox auth to provide the user's
	 * access token, if auth succeeded. You won't ever have to use this.
	 */
	public static final String EXTRA_ACCESS_TOKEN = "ACCESS_TOKEN";

	/**
	 * The extra that goes in an intent when returning from Dropbox auth to provide the user's
	 * access token secret, if auth succeeded. You won't ever have to use this.
	 */
	public static final String EXTRA_ACCESS_SECRET = "ACCESS_SECRET";

	/**
	 * The extra that goes in an intent when returning from Dropbox auth to provide the user's
	 * Dropbox UID, if auth succeeded. You won't ever have to use this.
	 */
	public static final String EXTRA_UID = "UID";

	/**
	 * Used for internal authentication. You won't ever have to use this.
	 */
	public static final String EXTRA_CONSUMER_SIG = "CONSUMER_SIG";

	/**
	 * Used for internal authentication. You won't ever have to use this.
	 */
	public static final String EXTRA_CALLING_PACKAGE = "CALLING_PACKAGE";

	/*
	 * The authenticate action can be changed in the future if the interface between the official
	 * app and the developer's portion changes as a way to track versions.
	 */
	/**
	 * The Android action which the official Dropbox app will accept to authenticate a user. You
	 * won't ever have to use this.
	 */
	public static final String ACTION_AUTHENTICATE_V1 = "com.dropbox.android.AUTHENTICATE_V1";

	public static final int AUTH_VERSION = 1;

	// For communication between AndroidAuthSesssion and this activity.
	static final String EXTRA_INTERNAL_CONSUMER_KEY = "EXTRA_INTERNAL_CONSUMER_KEY";
	static final String EXTRA_INTERNAL_CONSUMER_SECRET = "EXTRA_INTERNAL_CONSUMER_SECRET";
	static Intent slastResult = null;
	private String mConsumerKey = null;
	private String mConsumerSecret = null;
	private static final String CALLBACK_URL = "http://www.baidu.com";

	private WebView mAuthWebView;
	private WebAuthSession mWebAuthSession;
	private WebAuthInfo mWebAuthInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		Intent intent = getIntent();
		mConsumerKey = intent.getStringExtra(EXTRA_INTERNAL_CONSUMER_KEY);
		mConsumerSecret = intent.getStringExtra(EXTRA_INTERNAL_CONSUMER_SECRET);
		if (TextUtils.isEmpty(mConsumerKey) || TextUtils.isEmpty(mConsumerSecret)) {
			return;
		}
		initWebview();
		startAuth();
	}

	private void initWebview() {
		mAuthWebView = new WebView(this);
		final int m100 = 100;
		mAuthWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		mAuthWebView.setVerticalScrollBarEnabled(true);
		mAuthWebView.setHorizontalScrollBarEnabled(true);
		mAuthWebView.setWebViewClient(new NetBackupWebviewChromeClient());

		WebSettings setting = mAuthWebView.getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setLoadWithOverviewMode(true);
		setting.setBuiltInZoomControls(true);
		setting.setSavePassword(false);
		mAuthWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				NetBackupLoginActivity.this.setProgress(progress * m100);
			}
		});

		setContentView(mAuthWebView);
	}

	private void startAuth() {
		mWebAuthSession = new WebAuthSession(new AppKeyPair(mConsumerKey, mConsumerSecret),
				AccessType.APP_FOLDER);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mWebAuthInfo = mWebAuthSession.getAuthInfo(CALLBACK_URL);
					if (mWebAuthInfo == null) {
						Log.d("TEST", "startAuth : mWebAuthInfo == null");
						return;
					}
					mAuthWebView.loadUrl(mWebAuthInfo.url);
				} catch (DropboxException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 网络备份浏览器
	 * 
	 * @author maiyongshen
	 */
	private class NetBackupWebviewChromeClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			Log.d("TEST", "shouldOverrideUrlLoading : url = " + url);
			if (!url.contains(CALLBACK_URL)) {
				return super.shouldOverrideUrlLoading(view, url);
			} else {
				new Thread(new Runnable() {

					@Override
					public void run() {
						String token = null, secret = null;
						try {
							mWebAuthSession.retrieveWebAccessToken(mWebAuthInfo.requestTokenPair);
						} catch (DropboxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						token = mWebAuthSession.getAccessTokenPair().key;
						secret = mWebAuthSession.getAccessTokenPair().secret;
						slastResult = new Intent();
						if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(secret)) {
							slastResult.putExtra(EXTRA_ACCESS_TOKEN, token);
							slastResult.putExtra(EXTRA_ACCESS_SECRET, secret);
						}
						// 清除cookies
						CookieSyncManager.createInstance(NetBackupLoginActivity.this);
						CookieSyncManager.getInstance().startSync();
						CookieManager.getInstance().removeSessionCookie();
						finish();

					}
				}).start();
			}
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description,
				String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host,
				String realm) {
			super.onReceivedHttpAuthRequest(view, handler, host, realm);
		}
	}

}
