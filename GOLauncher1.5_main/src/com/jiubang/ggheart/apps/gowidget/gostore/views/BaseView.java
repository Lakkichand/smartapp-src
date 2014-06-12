package com.jiubang.ggheart.apps.gowidget.gostore.views;

import android.content.Context;
import android.widget.LinearLayout;

import com.jiubang.ggheart.apps.gowidget.gostore.component.ThemeTitle;

/**
 * 所有界面视图基类
 * 
 * @author huyong
 * 
 */
public abstract class BaseView extends LinearLayout {

	private byte mViewType = 0; // 界面类型
	private String mBaseUrl = null; // 界面初始url
	protected boolean mIsRecycled = false; // 是否已被回收

	public BaseView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public BaseView(Context context, BaseViewData viewData) {
		super(context);

		mViewType = viewData.mViewType;
		mBaseUrl = viewData.mBaseUrl;
	}

	public byte getViewType() {
		return mViewType;
	}

	public void setViewType(byte viewType) {
		this.mViewType = viewType;
	}

	public String getBaseUrl() {
		return mBaseUrl;
	}

	public void setmBaseUrl(String baseUrl) {
		this.mBaseUrl = baseUrl;
	}

	/**
	 * 资源回收
	 * 
	 * @author huyong
	 */
	public void recycle() {
		mIsRecycled = true;
		mBaseUrl = null;
	}

	/**
	 * 是否没有数据
	 * 
	 * @author huyong
	 * @return
	 */
	public abstract boolean hasNoData();

	/**
	 * 加载数据
	 * 
	 * @author huyong
	 */
	public abstract void loadData();

	/**
	 * 更新UI
	 * 
	 * @author huyong
	 */
	public void updateUI() {

	}

	/**
	 * 设置返回按钮的点击操作,可以不实现，不实现为默认操作
	 * 
	 * @author zhouxuewen
	 * @param listener
	 */
	public void setBackViewLisitener(OnClickListener listener) {
	}

	/**
	 * 获取TitleView 指对不同界面的Title按钮能实现不同功能
	 * 
	 * @author zhouxuewen
	 * @return view的title,如具体VIEW没实现就返回null，使用方防空
	 */
	public ThemeTitle getTitleView() {
		return null;
	}

	public void onVisable() {
	}

	public void onInVisable() {
	}
}
