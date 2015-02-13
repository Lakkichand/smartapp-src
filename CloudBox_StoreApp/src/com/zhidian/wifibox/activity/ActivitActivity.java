package com.zhidian.wifibox.activity;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.javascript.CopyJavaScriptInterface;
import com.zhidian.wifibox.javascript.DownloadJavaScriptInterface;
import com.zhidian.wifibox.javascript.OpenAppDetailJavaScriptInterface;
import com.zhidian.wifibox.javascript.OpenTopicsDetailJavaScriptInterface;
import com.zhidian.wifibox.javascript.UUIDJavaScriptInterface;

/**
 * 活动网页Activity
 * 
 * @author zhaoyl
 * 
 */
public class ActivitActivity extends Activity {

	private ImageView ivBack;// 返回
	public static String TITLE = "title";
	public static String URL = "url";
	private static final String PUSH_URL = "activity";
	private static final String PUSH_TITLE = "title";
	private String title, url;// 标题、地址

	private WebView mWebView;
	private static String TAG = ActivitActivity.class.getSimpleName();
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activit_webview);

		mContext = this;
		initIntent();
		initUI();
		initWebView();
	}

	private void initIntent() {
		title = getIntent().getStringExtra(TITLE);
		url = getIntent().getStringExtra(URL);
	}

	private void initUI() {
		TextView tvTitle = (TextView) findViewById(R.id.title);
		tvTitle.setText(title);
		mWebView = (WebView) findViewById(R.id.webview);
		ivBack = (ImageView) findViewById(R.id.back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	/**
	 * WebView
	 */
	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	private void initWebView() {
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);// 启用javascript
		webSettings.setSupportZoom(false);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);// 设为单列显示

		mWebView.setWebChromeClient(new MyWebChromeClient());
		mWebView.addJavascriptInterface(new DownloadJavaScriptInterface(
				mContext), "download");
		mWebView.addJavascriptInterface(new OpenAppDetailJavaScriptInterface(
				mContext), "openAppDetail");
		mWebView.addJavascriptInterface(
				new OpenTopicsDetailJavaScriptInterface(ActivitActivity.this),
				"openTopicsDetail");
		mWebView.addJavascriptInterface(new CopyJavaScriptInterface(mContext),
				"copy");
		mWebView.addJavascriptInterface(new UUIDJavaScriptInterface(), "uuid");
		if (!TextUtils.isEmpty(url)) {
			mWebView.loadUrl(url);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, "活动");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			String content = click.getCustomContent();
			try {
				JSONObject json = new JSONObject(content);
				url = json.optString(PUSH_URL, "");
				title = json.optString(PUSH_TITLE, "");
				TextView tvTitle = (TextView) findViewById(R.id.title);
				tvTitle.setText(title);
				if (!TextUtils.isEmpty(url)) {
					mWebView.loadUrl(url);
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, "活动");
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 必须要调用这句
		setIntent(intent);
	}

	/**
	 * 用于调试javascript
	 * 
	 */
	final class MyWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			Log.e(TAG, message);
			result.confirm();
			return true;
		}
	}
}
