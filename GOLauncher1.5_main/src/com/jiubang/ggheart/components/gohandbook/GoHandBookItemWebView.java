package com.jiubang.ggheart.components.gohandbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
 * <br>类描述: 每个网页的布局
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class GoHandBookItemWebView extends LinearLayout {
	private Context mContext;
	private Handler mHandler = new Handler();
	private WebView mWebView;
	private GoProgressBar mGoProgressBar; // 等待框
	private LinearLayout mConnectFailLayout; // 连接失败提示布局
	private Button mRefreshBtn; // 刷新按钮
	private boolean mIsRefresh; //是否刷新标志
	private String mKeyName; //
	private int mCurrentPage; // 当前页面
	private boolean mIsLoadFinish = false; // 是否加载完成标志
	private int mOpenPage; // 要打开的页面
	private String mUrl; // 打开的URL
	private TextView mProgressNow; //当前进度提示
	private GoHandBookIndexListner mGoHandBookIndexListner;

	private final static float DENSITY_H = 1.5f;
	private final static float DENSITY_L = 2.0f;

	public GoHandBookItemWebView(Context context) {
		super(context);
	}

	public GoHandBookItemWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GoHandBookItemWebView(Context context, String keyName, int currentPage, int openPage) {
		super(context);
		this.mContext = context;
		this.mKeyName = keyName;
		this.mCurrentPage = currentPage;
		this.mOpenPage = openPage;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.go_handbook_item_webview, this);

		mProgressNow = (TextView) findViewById(R.id.progress_now);
		mGoProgressBar = (GoProgressBar) findViewById(R.id.modify_progress);
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
		this.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		webSettings.setAllowFileAccess(true); // 设置允许访问文件数据
		webSettings.setSavePassword(false); // 设置是否保存密码
		webSettings.setSaveFormData(false);
		webSettings.setJavaScriptEnabled(true); // 设置支持JavaScript脚本
		webSettings.setSupportZoom(true); // 设置支持缩放
		webSettings.setDefaultTextEncodingName("utf-8");

		webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //设置打开缓存

		// 启用数据库
		webSettings.setDatabaseEnabled(true);
		String dir = mContext.getDir("database", Context.MODE_PRIVATE).getPath();
		webSettings.setDatabasePath(dir); // 设置数据库路径
		webSettings.setDomStorageEnabled(true); // 使用localStorage则必须打开

		webSettings.setLoadWithOverviewMode(true);

		// 自定义的Demo，供js网页调用
		mWebView.addJavascriptInterface(new DemoJavaScriptInterface(), "demo");

		setWebViewDensity();

		// 通过这个设置来执行加载webview网页时所要执行的一些方法
		mWebView.setWebViewClient(new WebViewClient() {

			// 新开页面时用自己webview来显示，不用系统自带的浏览器来显示
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				//log("打开的超链接地址：" + url);
				view.loadUrl(url);
				return true;
			}

			// 开始加载
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				//log("开始加载" + mCurrentPage);
				super.onPageStarted(view, url, favicon);
			}

			// 加载完成
			@Override
			public void onPageFinished(WebView view, String url) {
				//log("加载完成" + mCurrentPage);
				dismissProgressDialog();

				if (mOpenPage != -1) {
					mIsLoadFinish = true;
					// 判断打开的第一页要加载完后才设置已读，当前页要等于当前滚动的页
					//mIsRefresh看是否点刷新按钮刷新。是就更新状态
					if (mCurrentPage == mOpenPage || mIsRefresh) {
						updateHaveReadData(mKeyName);
						mIsRefresh = false;
					}
				}
			}

			// 加载错误
			@Override
			public void onReceivedError(WebView view, int errorCode, String description,
					String failingUrl) {
				//				log("加载报错:" + description);
				mIsRefresh = false;
				mIsLoadFinish = false;
				showErrorView();

				mRefreshBtn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						showStartLoadView();
						mIsRefresh = true; //加载失败时如果点了刷新按钮就设置标志。重新加载时更新是否已读
						mWebView.loadUrl(mUrl);
					}
				});
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
	 * <br>类描述:网页 JS 需要调用的方法类
	 * <br>功能详细描述:
	 * 
	 * @author  licanhui
	 * @date  [2012-9-5]
	 */
	final class DemoJavaScriptInterface {

		DemoJavaScriptInterface() {

		}

		/**
		 * <br>功能简述:获取是否Load完毕
		 * <br>功能详细描述:
		 * <br>注意:
		 * @return
		 */
		public boolean getIsLoadFinish() {
			return mIsLoadFinish;

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
	 * <br>功能简述:加载网页
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param url 加载地址
	 */
	public void loadUrl(String url) {
		mUrl = url;
		mWebView.loadUrl(url);
	}

	/**
	 * <br>功能简述:执行JS方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param methodName 方法名
	 */
	public void loadJavaScript(String methodName) {
		mWebView.loadUrl("javascript:" + methodName);
	}

	/**
	 * <br>功能简述:更新是否已读数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param idName 对应缓存的KEY
	 */
	public void updateHaveReadData(String idName) {
		if (mIsLoadFinish) {
			loadJavaScript("updateHaveReadData('" + idName + "')");
		}

	}

	/**
	 * <br>功能简述:设置首页"重试"监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param goHandBookIndexListner
	 */
	public void setGoHandBookIndexListner(GoHandBookIndexListner goHandBookIndexListner) {
		this.mGoHandBookIndexListner = goHandBookIndexListner;
	}

	/**
	 * <br>功能简述:	设置"重试"按钮监听
	 * <br>功能详细描述:当加载首页URL失败时，设置"重试"按钮的监听事件。重新请求接口获取URL
	 * <br>注意:
	 */
	public void setReTryBtnListner() {
		showErrorView(); //显示加载失败的提示 

		mRefreshBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showStartLoadView();
				if (mGoHandBookIndexListner != null) {
					mGoHandBookIndexListner.retTry();
				}
			}
		});
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

	public void log(String strings) {
		//		Log.i("lch", strings);
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
