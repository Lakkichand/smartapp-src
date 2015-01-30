package com.zhidian.wifibox.javascript;

import java.util.ArrayList;
import java.util.List;

import android.webkit.JavascriptInterface;

import com.ta.TAApplication;
import com.zhidian.wifibox.activity.ActivitActivity;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;

public class OpenTopicsDetailJavaScriptInterface {

	private ActivitActivity mContext;

	public OpenTopicsDetailJavaScriptInterface(ActivitActivity context) {
		mContext = context;
	}

	/**
	 * This is not called on the UI thread. Post a runnable to invoke loadUrl on
	 * the UI thread.
	 */
	@JavascriptInterface
	public void OpenTopicsDetailOnAndroid(long id, String title) {
		try {
			// 跳转到专题内容
			List<Object> list = new ArrayList<Object>();
			TopicDataBean bean = new TopicDataBean();
			bean.id = id;
			bean.title = title;
			list.add(bean);
			// 通知TabManageView跳转下一层级，把TopicDataBean带过去
			TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
					IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
					CDataDownloader.getTopicContentUrl(id, 1), list);
			mContext.finish();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
