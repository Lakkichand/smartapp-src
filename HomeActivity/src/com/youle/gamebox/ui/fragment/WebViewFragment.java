package com.youle.gamebox.ui.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.*;
import android.widget.LinearLayout;
import butterknife.InjectView;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.activity.ComunityActivity;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.UIUtil;

import java.io.File;

/**
 * Created by Administrator on 2014/5/28.
 */
public class WebViewFragment extends BaseFragment {
    @InjectView(R.id.test_webView)
    WebView mWebView;
    WebSettings webSettings;
    String title = "";
    String url = "";
    private boolean loadSuccess = false;
public static final int FILECHOOSER_RESULTCODE = 3 ;
    ValueCallback<Uri> mUploadMessage ;
    public WebViewFragment(String title, String url) {
        this.title = title;
        this.url = url;
    }

    @Override
    protected int getViewId() {
        return R.layout.webview_layout;
    }

    @Override
    protected String getModelName() {
        return "网页";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!loadSuccess) {
            setWebView();
            loadData();
        }
        if(getActivity() instanceof ComunityActivity){
            ((ComunityActivity)getActivity()).setWebViewFragment(this);
        }
    }

    protected void loadData() {
        setDefaultTitle(title);
        webViewLoadUrl(url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        mWebView = null;
    }

    private void webViewLoadUrl(String url) {
        if (url == null) return;
        if ("".equals(url)) return;
        mWebView.loadUrl(url);
    }

    /**
     * 设置WebView属性
     */
    private void setWebView() {
        mWebView.requestFocusFromTouch();
        webSettings = mWebView.getSettings();
        webSettings.setSaveFormData(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setBlockNetworkImage(true);//1.加载url前，设置图片阻塞
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDomStorageEnabled(false);//设置可以使用localStorage
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//默认使用缓存
//        webSettings.setAppCacheMaxSize(8 * 1024 * 1024);//缓存最多可以有8M
        webSettings.setAllowFileAccess(false);//可以读取文件缓存(manifest生效)
        webSettings.setAppCacheEnabled(false);//应用可以有缓存
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.addJavascriptInterface(this, "appJs");
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebChromeClient(new CustomWebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //在本页加载新连接
                if (!"".equals(url)) {
                    if (url.startsWith("http")) {
                        if(url.contains("?")){
                            url = url + "&fromWhere=app" ;
                        }else {
                            url = url+"?fromWhere=app";
                        }
                        webViewLoadUrl(url);
                    }
                }
                return true;
            }


            @Override
            public void onLoadResource(WebView view, String url) {
                // TODO Auto-generated method stub
                webSettings.setBlockNetworkImage(false);
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                //加载完毕后，关闭图片阻塞
                webSettings.setBlockNetworkImage(false);
                super.onPageFinished(view, url);
                if(!url.equals(url)) {
                    setDefaultTitle(view.getTitle());
                }
            }

        });
    }

    //flipscreen not loading again
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

// To handle "Back" key press event for WebView to go back to previous screen.
/*@Override
public boolean onKeyDown(int keyCode, KeyEvent event)
{
    if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
        web.goBack();
        return true;
    }
    return super.onKeyDown(keyCode, event);
}*/

    protected class CustomWebChromeClient extends WebChromeClient
    {
        // For Android 3.0+
        public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType )
        {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult( Intent.createChooser( i, "File Chooser" ), FILECHOOSER_RESULTCODE );
        }

        // For Android < 3.0
        public void openFileChooser( ValueCallback<Uri> uploadMsg )
        {
            openFileChooser( uploadMsg, "" );
        }
        //For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult( Intent.createChooser( i, "File Chooser" ), FILECHOOSER_RESULTCODE );
        }

    }

    @Override
    public void goBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            ((BaseActivity)getActivity()).backTitle();
        } else {
            super.goBack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.reload();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != Activity.RESULT_OK? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
//            mUploadMessage = null;
        }
    }
    @JavascriptInterface
    public void showLogin() {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_LOGIN, -1);
            }
        });
    }

    @JavascriptInterface
    public int getClientid() {
        return 0;
    }
    @JavascriptInterface
    public void saveemeil(String email){
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        userInfo.setContact(email);
        DaoManager.getDaoSession().getUserInfoDao().insertOrReplace(userInfo);
    }
    @JavascriptInterface
    public void toast(String content){
        UIUtil.toast(getActivity(),content);
    }
}
