package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 
 * <br>类描述:VIP付款页
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2013-1-24]
 */
public class ThemeVipPage extends Activity {

	private final static float DENSITY_H = 1.5f;
	private final static float DENSITY_L = 2.0f;
	public static int sPayItem;
	private WebView mWebView;
	private LinearLayout mProcessLayout = null;
	private TextView mProcessText = null;
	private Handler mHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.vippagelayout);
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		if (url == null) {
			finish();
		}
		mHandler = new Handler();
		mWebView = (WebView) findViewById(R.id.vippage);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setDefaultTextEncodingName("utf-8");
		mWebView.setWebViewClient(new MyWebViewClient());
		setWebViewDensity();
		mProcessLayout = (LinearLayout) findViewById(R.id.progressbar_group);
		mProcessText = (TextView) findViewById(R.id.progress_now);
		mWebView.setVerticalScrollbarOverlay(true);
		mWebView.loadUrl(url);
		mWebView.addJavascriptInterface(new WebViewClickInterface(), "themevip");
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2013-1-24]
 */
	class MyWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			showProgressDialog();
		};

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			dismissProgressDialog();
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onLoadResource(view, url);

		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description,
				String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

	}

	/**
	 * <br>功能简述:显示等待进度Dialog
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void showProgressDialog() {
		if (mProcessLayout != null && mProcessLayout.getVisibility() == View.INVISIBLE) {
			mProcessLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * <br>功能简述:关闭等待进度Dialog
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void dismissProgressDialog() {
		if (mProcessLayout != null && mProcessLayout.getVisibility() == View.VISIBLE) {
			mProcessLayout.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2013-1-24]
	 */
	class MyWebChromeClient extends WebChromeClient {
		// 通过JS代码输出log信息
		@Override
		public void onConsoleMessage(String message, int lineNumber, String sourceID) {

		}

		// 设置网页加载的进度条
		@Override
		public void onProgressChanged(WebView view, final int newProgress) {
			super.onProgressChanged(view, newProgress);
			// 用handler来更新UI
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mProcessText.setText(newProgress + "%");
				}
			});
		}
	}

	/**
	 * <br>功能简述:可以让不同的density的情况下，可以让页面进行适配
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void setWebViewDensity() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = this.getResources().getDisplayMetrics();
		float density = dm.density;
		if (density == DENSITY_H) {
			mWebView.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
		} else if (density == DENSITY_L) {
			mWebView.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
		} else {
			mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
		}
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2013-1-24]
 */
	public class WebViewClickInterface {
		public void clickOnAndroid(String level) {
			int l = Integer.valueOf(level);
			ThemePurchaseManager.getInstance(ThemeVipPage.this).payForVip(l);
			sPayItem = l;
			finish();
		}
	}

}
