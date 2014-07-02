package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.BaseTitleBarView;

/**
 * Created by Administrator on 2014/5/28.
 */
public class WebViewFragment extends BaseFragment {
    @InjectView(R.id.webview_layout_linear)
    LinearLayout mWebviewLayoutLinear;
    WebView mWebView;
    WebSettings webSettings;
    String title="";
    String url ="";
    View loadView;

    public WebViewFragment(String title,String url) {
        this.title = title;
        this.url = url;
        }

    @Override
    protected int getViewId() {
        return R.layout.webview_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    protected void loadData() {
        BaseTitleBarView baseTitleBarView = setTitleView();
        baseTitleBarView.setTitleBarMiddleView(null,title);
        loadView = LayoutInflater.from(getActivity()).inflate(R.layout.load_layout_progress,null);
        addView(loadView);
        setWebView();

        webViewLoadUrl(url);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebviewLayoutLinear.removeAllViews();
        mWebView.destroy();
        mWebView =null;
    }

    private void webViewLoadUrl(String url){
        if(url==null )return;
        if("".equals(url))return;
        //CookiesUtil.getInstance(context).setCookies(refreshUrl,UserApplicationManamger.getSid());
        mWebView.loadUrl(url);
    }

    private void addView(View view){
        mWebviewLayoutLinear.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        mWebviewLayoutLinear.addView(view,params);
    }

    /**
     * 设置WebView属性
     */
    private void setWebView(){
        mWebView  = new WebView(getActivity().getApplicationContext());//防止与activity contenxt 相关联 不释放
        mWebView.requestFocusFromTouch();
        webSettings = mWebView.getSettings();
        webSettings.setSaveFormData(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setBlockNetworkImage(true);//1.加载url前，设置图片阻塞
        webSettings.setLoadsImagesAutomatically(true) ;
        webSettings.setDomStorageEnabled(true);//设置可以使用localStorage
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//默认使用缓存
        webSettings.setAppCacheMaxSize(8 * 1024 * 1024);//缓存最多可以有8M
        webSettings.setAllowFileAccess(true);//可以读取文件缓存(manifest生效)
        webSettings.setAppCacheEnabled(true);//应用可以有缓存
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.addJavascriptInterface(this, "responseJS");
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                //加载进度改变
                LOGUtil.d("junjun","---"+progress);
                if(progress>=100){
                    addView(mWebView);
                }

            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //在本页加载新连接
                if(!"".equals(url)){
                    if(url.startsWith("http")){
                        webViewLoadUrl(url);
                    }
                }
                return true;
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //加载失败  在这里修改加载失败之后显示的界面
                Toast.makeText(getActivity(), "页面 加载还是失败了,请重新加载!", Toast.LENGTH_SHORT).show();
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
            }

        });

    }


}
