package com.zhidian.wifibox.activity;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.javascript.Play68Interface;
import com.zhidian.wifibox.javascript.WeChatShareJavaScriptInterface;

/**
 * HTML5游戏界面
 * 
 * @author xiedezhi
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class HTMLGameActivity extends Activity {

	public static final String GAMEURLKEY = "GAMEURLKEY";

	private static final String PUSH_URL = "html5";

	private TextView mTitle;
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.htmlgame);

		String url = getIntent().getStringExtra(GAMEURLKEY);

		findViewById(R.id.back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mWebView.canGoBack()) {
					while (mWebView.canGoBack()) {
						mWebView.goBack();
					}
				} else {
					finish();
				}

			}
		});
		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setText("加载中...");

		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setSupportZoom(false);
		mWebView.getSettings().setBuiltInZoomControls(false);
		mWebView.setBackgroundColor(0);
		mWebView.addJavascriptInterface(new Play68Interface(this), "mibao");
		View parent = findViewById(R.id.main_layout);
		mWebView.addJavascriptInterface(new WeChatShareJavaScriptInterface(
				this, parent), "wechatShare");

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);// 使用当前WebView处理跳转
				view.loadUrl("javascript: window.isMibao = true");
				return true;// true表示此事件在此处被处理，不需要再广播
			}

			@Override
			// 转向错误时的处理
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Log.e("", "onReceivedError errorCode = " + errorCode
						+ "  description = " + description + "  failingUrl = "
						+ failingUrl);
			}
		});
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				view.loadUrl("javascript: window.isMibao = true");
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				view.loadUrl("javascript: window.isMibao = true");
				mTitle.setText(title);
			}
		});
		if (!TextUtils.isEmpty(url)) {
			mWebView.loadUrl(url);
			mWebView.loadUrl("javascript: window.isMibao = true");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, "HTML5游戏");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			String content = click.getCustomContent();
			try {
				JSONObject json = new JSONObject(content);
				String url = json.optString(PUSH_URL, "");
				if (!TextUtils.isEmpty(url)) {
					mWebView.loadUrl(url);
					mWebView.loadUrl("javascript: window.isMibao = true");
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, "HTML5游戏");
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 必须要调用这句
		setIntent(intent);
	}

	@Override
	protected void onDestroy() {
		WebStorage.getInstance().deleteAllData();
		ViewGroup vg = (ViewGroup) mWebView.getParent();
		vg.removeView(mWebView);
		mWebView.stopLoading();
		mWebView.destroy();
		super.onDestroy();
		// 把进程杀死
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
