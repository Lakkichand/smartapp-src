package com.zhidian.wifibox.view;

import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.download.DownloadTask;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 普通的应用列表
 * 
 * @author xiedezhi
 * 
 */
public class CommonContainer extends LinearLayout implements IContainer {

	private PageDataBean mBean;

	public CommonContainer(Context context) {
		super(context);
	}

	public CommonContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	@Override
	public void onAppAction(String packName) {

	}

	@Override
	public String getDataUrl() {
		return mBean.mUrl;
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {

	}

	@Override
	public void updateContent(PageDataBean bean) {
		mBean = bean;
	}

	@Override
	public void onResume() {

	}

}
