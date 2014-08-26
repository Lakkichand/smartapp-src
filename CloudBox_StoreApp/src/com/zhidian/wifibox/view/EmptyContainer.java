package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.view.EmptyNavigationBar.IndexChangeListener;

/**
 * 空的容器，用于包裹多个子页面
 * 
 * 没有属于自己的数据，只负责把消息分发给子container
 * 
 * @author xiedezhi
 */
public class EmptyContainer extends LinearLayout implements IContainer {

	private List<IContainer> mSubContainer = new ArrayList<IContainer>();

	private FrameLayout mFrame;

	private PageDataBean mBean;

	private EmptyNavigationBar mBar;

	private FrameLayout.LayoutParams mLP = new FrameLayout.LayoutParams(
			FrameLayout.LayoutParams.MATCH_PARENT,
			FrameLayout.LayoutParams.MATCH_PARENT);

	public EmptyContainer(Context context) {
		super(context);
	}

	public EmptyContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mFrame = (FrameLayout) findViewById(R.id.container);
		mBar = (EmptyNavigationBar) findViewById(R.id.bar);
		mBar.setIndexChangeListener(new IndexChangeListener() {

			@Override
			public void indexChange(int index) {
				mBean.mIndex = index;
			}
		});
	}

	@Override
	public void onAppAction(String packName) {
		for (IContainer container : mSubContainer) {
			container.onAppAction(packName);
		}
	}

	@Override
	public String getDataUrl() {
		return mBean.mUrl;
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		for (IContainer container : mSubContainer) {
			container.notifyDownloadState(downloadTask);
		}
	}

	@Override
	public void updateContent(PageDataBean bean) {
		mBean = bean;
	}

	@Override
	public void onResume() {
		for (IContainer container : mSubContainer) {
			container.onResume();
		}
	}

	/**
	 * 清除之前的子页面
	 */
	public void clearContainer() {
		mSubContainer.clear();
		mFrame.removeAllViews();
		mBar.update(null, null);
	}

	/**
	 * 添加子页面
	 */
	public void addContainer(IContainer container) {
		mSubContainer.add(container);
		mFrame.addView((View) container, mLP);
		if (mSubContainer.size() == mBean.mSubContainer.size()) {
			List<String> titles = new ArrayList<String>();
			List<View> views = new ArrayList<View>();
			for (PageDataBean bean : mBean.mSubContainer) {
				titles.add(bean.mTitle);
			}
			for (IContainer sub : mSubContainer) {
				views.add((View) sub);
			}
			mBar.update(titles, views);
			mBar.setSelect(mBean.mIndex);
		}
	}

	/**
	 * 获取子container
	 */
	public List<IContainer> getContainers() {
		return mSubContainer;
	}

}
