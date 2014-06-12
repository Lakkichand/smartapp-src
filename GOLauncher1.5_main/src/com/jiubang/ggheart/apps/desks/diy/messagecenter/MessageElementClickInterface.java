package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import android.app.Activity;
import android.webkit.WebView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageWidgetBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * webview js交互类
 * @author liulixia
 *
 */
public class MessageElementClickInterface {
	private WebView mWebView = null;
	private Activity mActivity = null;
	private MessageManager mManager = null;
	private String mMsgInstall = null;
	private String mMsgNotInstall = null;
	
	public MessageElementClickInterface(WebView webview, Activity activity) {
		mWebView = webview;
		mActivity = activity;
		init();
	}
	
	private void init() {
		mManager = MessageManager.getMessageManager(GOLauncherApp.getContext());
		mMsgInstall = mActivity.getString(R.string.message_file_install);			//已安装
		mMsgNotInstall = mActivity.getString(R.string.message_file_not_install);	//未安装
	}
	
	 public void clickOnAndroid(final String type, final String value, final String actiontype, final String actionvalue) {
		 Runnable runnable = null;
		 final MessageWidgetBean widgetBean = new MessageWidgetBean();
		 widgetBean.mType = type;
		 widgetBean.mValue = value;
		 widgetBean.mActtype = Integer.parseInt(actiontype);
		 widgetBean.mActvaule = actionvalue;
		 runnable = new Runnable() {
			 public void run() {
				 mManager.handleWidgetClick(widgetBean, mActivity);
			 }
		 };
		 sendRunnable(runnable);
	 }
	 
	 public void updateDownloadText(final String id, final String actionvalue) {
		 final MessageWidgetBean widgetBean = new MessageWidgetBean();
		 widgetBean.mType = MessageWidgetBean.TYPE_BTN;
		 widgetBean.mActtype = 6;
		 widgetBean.mActvaule = actionvalue;
		
		 Runnable runnable = new Runnable() {
			 public void run() {
				 int state = mManager.getFileState(widgetBean);
	    		 switch(state) {
	    			 case MessageManager.FILE_INSTALLED:
	    				 mWebView.loadUrl("javascript:setDownloadButtonText(\'" + id + "\',\'" + mMsgInstall + "\')");
	    				 break;
	    			 case MessageManager.FILE_NOT_INSTALL:
	    				 String url = "javascript:setDownloadButtonText(\'" + id + "\',\'" + mMsgNotInstall + "\')";
	    				 mWebView.loadUrl(url);
	    				 break;
	    			 default:
	    				 break;
	    		 }
			 }
		 };
		 sendRunnable(runnable);
	 }
	 
	 private void sendRunnable(Runnable runnable) {
		 if (mActivity instanceof MessageContentActivity) {
			 ((MessageContentActivity) mActivity).sendRunnable(runnable);
		 } else if (mActivity instanceof MessageDialogContentActivity) {
			 ((MessageDialogContentActivity) mActivity).sendRunnable(runnable);
		 }
	 }
	 
	 public void loadUrl(String url) {
		 if (mWebView != null) {
			 mWebView.loadUrl(url);
		 }
	 }
	 
	 public void onDestory() {
		if (mWebView != null) {
			mWebView.stopLoading();
			mWebView = null;
		}
		mManager = null;
		mActivity = null;
		mMsgInstall = null;
		mMsgNotInstall = null;
	 }
}
