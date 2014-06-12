package com.jiubang.ggheart.apps.gowidget.gostore.views;

public class BaseViewData {

	public byte mViewType; // 视图类型
	public String mViewTitle; // 界面title
	public String mBaseUrl; // 首次进入url
	public Object mDataObject; // 要携带的其它数据
	public byte mContainerType; // 容器类型(用于记录当前VIEW,所处于那和类Activity中)

	public void recycle() {
		mViewTitle = null;
		mBaseUrl = null;
		mDataObject = null;
	}
}
