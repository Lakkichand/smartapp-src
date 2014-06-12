package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
/**
 * 消息中心webview页面
 * @author liulixia
 *
 */
public class MessageCenterWebView extends RelativeLayout {
	private WebView mWebView = null;
	private LinearLayout mProcessLayout = null;
	private TextView mProcessText = null;
	private Handler mHandler = new Handler();
	private MessageWebViewClient mWebViewClient = null;
	private MessageElementClickInterface mClickInterface = null;
	private Activity mActivity = null;
	
	private String mMsgTitle = null;
	private String mMsgStamp = null;
	
	private final static float DENSITY_H = 1.5f;
	private final static float DENSITY_L = 2.0f;
	
	public MessageCenterWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MessageCenterWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
	}
	
	public void initWebView(Activity activity) {
	 	mWebView = (WebView) findViewById(R.id.webview);
	 	mProcessText = (TextView) findViewById(R.id.progress_now);
	 	mProcessLayout = (LinearLayout) findViewById(R.id.modify_progress);
	 	mActivity = activity;
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
//        webSettings.setBuiltInZoomControls(true);
        mWebView.setVerticalScrollbarOverlay(true);
        mWebViewClient = new MessageWebViewClient();
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(new MessageWebChromeClient());
        
        setWebViewDensity();
        
        mProcessLayout.setVisibility(VISIBLE);
        mClickInterface = new MessageElementClickInterface(mWebView, mActivity);
        mWebView.addJavascriptInterface(mClickInterface, "buttonClick");
	}
	
	public void setTitleAndStamp(String title, String stamp) {
		mMsgTitle = title;
		mMsgStamp = stamp;
	}
	
	public void setOriginalUrl(String originalUrl) {
		mWebViewClient.setOriginalUrl(originalUrl);
		mWebView.loadUrl(originalUrl);
	}
	
	/**
	 * 
	 * @author liulixia
	 *
	 */
	class MessageWebViewClient extends WebViewClient {
		private String mOriginalUrl = "";
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
			if (mOriginalUrl.equals(url)) {
				view.loadUrl("javascript:init(\'" + mMsgTitle + "\',\'" + mMsgStamp + "\')");
			}
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
		
		// 新开页面时用自己webview来显示，不用系统自带的浏览器来显示
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
		
		public void setOriginalUrl(String originalUrl) {
			mOriginalUrl = originalUrl;
		}
	}
	/**
	 * 
	 * @author liulixia
	 *
	 */
	class MessageWebChromeClient extends WebChromeClient {
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
	
	public WebView getWebView() {
		return mWebView;
	}
	
	public void onDestory() {
		if (mClickInterface != null) {
			mClickInterface.onDestory();
			mClickInterface = null;
		}
		mWebView = null;
		mWebViewClient = null;
	}
	
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView != null && mWebView.canGoBack()) {  
            mWebView.goBack();  
            return true;  
        }
        return false;
    }
	
	public void loadUrl(String url) {
		if (mWebView != null) {
			mWebView.loadUrl(url);
		}
	}
	
}
