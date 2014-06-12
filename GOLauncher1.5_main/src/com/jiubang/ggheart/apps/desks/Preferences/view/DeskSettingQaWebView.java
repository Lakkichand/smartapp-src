package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.components.GoProgressBar;

/**
 * 
 * <br>类描述:QA每个网页的布局
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-12]
 */
public class DeskSettingQaWebView extends LinearLayout {
	private Handler mHandler = new Handler();
	private WebView mWebView;
	private GoProgressBar mGoProgressBar; // 等待框
	private TextView mProgressNow; //当前进度提示

	private LinearLayout mConnectFailLayout; // 连接失败提示布局
	private Button mRefreshBtn; // 刷新按钮

	private final static float DENSITY_H = 1.5f;
	private final static float DENSITY_L = 2.0f;

	public DeskSettingQaWebView(Context context) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.go_handbook_item_webview, this);

		mGoProgressBar = (GoProgressBar) findViewById(R.id.modify_progress);
		mProgressNow = (TextView) findViewById(R.id.progress_now);

		mConnectFailLayout = (LinearLayout) findViewById(R.id.connect_fail_layout);
		mRefreshBtn = (Button) findViewById(R.id.refreshBtn);

		initWebView();
	}

	/**
	 * <br>功能简述:初始化webView
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initWebView() {
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setBackgroundColor(getResources().getColor(R.color.go_book_webview_bg)); // 设置webview背景颜色

		WebSettings webSettings = mWebView.getSettings();
		webSettings.setSupportZoom(true); // 设置支持缩放
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDefaultTextEncodingName("utf-8");

		//webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //设置打开缓存

//		setWebViewDensity();

		// 通过这个设置来执行加载webview网页时所要执行的一些方法
		mWebView.setWebViewClient(new WebViewClient() {

			// 新开页面时用自己webview来显示，不用系统自带的浏览器来显示
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				//log("打开的超链接地址：" + url);
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				//log("开始加载");
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				//log("加载完成");
				dismissProgressDialog();

			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description,
					String failingUrl) {
				//log("加载报错:" + description);

				if (view.getSettings().getCacheMode() == WebSettings.LOAD_DEFAULT) {
					view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
					view.loadUrl(failingUrl);
				} else {
					showErrorView();
					mRefreshBtn.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							showStartLoadView();
							mWebView.reload();
						}
					});
				}
			}
		});

		mWebView.setWebChromeClient(new MyWebChromeClient()); // 显示网页中的对话框
	}

	/**
	 * <br>功能简述:显示开发加载的视图
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showStartLoadView() {
		mWebView.setVisibility(View.VISIBLE);
		mConnectFailLayout.setVisibility(View.GONE);
		showProgressDialog();
	}

	/**
	 * <br>功能简述: 显示加载失败的提示
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showErrorView() {
		mWebView.setVisibility(View.GONE);
		mConnectFailLayout.setVisibility(View.VISIBLE);
		dismissProgressDialog();
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
	 * <br>类描述:显示网页中的对话框
	 * <br>功能详细描述:
	 * 
	 * @author  licanhui
	 * @date  [2012-9-5]
	 */
	final class MyWebChromeClient extends WebChromeClient {
		// 通过JS代码输出log信息
		@Override
		public void onConsoleMessage(String message, int lineNumber, String sourceID) {
			log(message + " – From line " + lineNumber + " of " + sourceID);
		}

		// 设置网页加载的进度条
		@Override
		public void onProgressChanged(WebView view, final int newProgress) {
			super.onProgressChanged(view, newProgress);
			// 用handler来更新UI
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mProgressNow.setText(newProgress + "%");
				}
			});
		}
	}

	/**
	 * <br>功能简述:显示等待进度Dialog
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void showProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.INVISIBLE) {
			mGoProgressBar.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * <br>功能简述:关闭等待进度Dialog
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void dismissProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.VISIBLE) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	public WebView getWebView() {
		return mWebView;
	}

	/**
	 * <br>功能简述:加载网页
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param url 加载地址
	 */
	public void loadUrl(String url) {
		if (mWebView != null) {
			mWebView.loadUrl(url);
		}
	}

	public void log(String strings) {
		Log.i("lch", strings);
	}

	/**
	 * <br>功能简述:要循环销毁每个Webview
	 * <br>功能详细描述:
	 * <br>注意:外部需要调用注销,有可能webview没有加载完会一直加载。导致Activity注销不了
	 */
	public void onDestroy() {
		mWebView.stopLoading();
		mWebView.destroy();
		mWebView = null;
	}
}
