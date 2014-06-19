package com.jiubang.go.backup.pro.net.sync;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;

/**
 * @author ReyZhang
 */
public class GoogleDriveBrowserActivity extends Activity {
	public static final String EXTRA_REFRESH_TOKEN = "refresh_token";
	private static final int PROGRESS_WEIGHT = 100;
	private WebView mWebView;
	public static String sLastAuthorizationCode = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		
		Intent intent = getIntent();
		if (intent != null) {
			boolean toRefreshToken = intent.getBooleanExtra(EXTRA_REFRESH_TOKEN, false);
			// 只是为了刷新token，让验证流程完整跑完
			if (toRefreshToken) {
				finish();
				return;
			}
		}
		
		mWebView = new WebView(this);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.clearCache(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				LogUtil.d("shouldOverrideUrlLoading url = " + url);
				if (url != null && url.startsWith(GoogleDriveManager.REDIRECT_URI)) {
					Uri uri = Uri.parse(url);
					sLastAuthorizationCode = uri.getQueryParameter("code");
					GoogleDriveBrowserActivity.this.finish();
					return true;
				}
				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
//				LogUtil.d("onPageStarted url = " + url);
			}

			@Override
	        public void onPageFinished(WebView view, String url) {
//				LogUtil.d("onPageFinished url = " + url);
			}

			@Override
	        public void onReceivedError(WebView view, int errorCode,
	                String description, String failingUrl) {
//				LogUtil.d("onReceivedError");
				super.onReceivedError(view, errorCode, description, failingUrl);
			}

		});

		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
//				LogUtil.d("progress = " + (newProgress * 100));
				GoogleDriveBrowserActivity.this.setProgress(newProgress * PROGRESS_WEIGHT);
			}
		});
		setContentView(mWebView);
	}

	@Override
	protected void onResume() {
		super.onResume();

		sLastAuthorizationCode = null;
		String authorizationUrl = null;
		Intent intent = getIntent();
		if (intent != null) {
			String url = intent.getDataString();
			if (url != null) {
				authorizationUrl = url;
			}
		}

		if (authorizationUrl == null) {
			authorizationUrl = new GoogleAuthorizationCodeRequestUrl(
					GoogleDriveManager.CLIENT_ID,
					GoogleDriveManager.REDIRECT_URI,
					GoogleDriveManager.SCOPES).setAccessType("offline")
					.setApprovalPrompt("force").build();
		}

		mWebView.loadUrl(authorizationUrl);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWebView != null) {
			mWebView.stopLoading();
			mWebView.destroy();
		}
	}
}
