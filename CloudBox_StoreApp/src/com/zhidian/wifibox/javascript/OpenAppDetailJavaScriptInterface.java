package com.zhidian.wifibox.javascript;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.zhidian.wifibox.activity.AppDetailActivity;
import com.zhidian.wifibox.data.AppDataBean;

/**
 * 打开app详情接口，提供给js调用
 * 
 */
public class OpenAppDetailJavaScriptInterface {

	private Context mContext;

	public OpenAppDetailJavaScriptInterface(Context context) {
		mContext = context;
	}

	/**
	 * This is not called on the UI thread. Post a runnable to invoke loadUrl on
	 * the UI thread.
	 */
	@JavascriptInterface
	public void OpenAppDetailOnAndroid(long id) {
		AppDataBean bean = null;
		Intent intent = new Intent(mContext, AppDetailActivity.class);
		intent.putExtra("bean", bean);
		intent.putExtra("appId", id);
		mContext.startActivity(intent);
	}
}
