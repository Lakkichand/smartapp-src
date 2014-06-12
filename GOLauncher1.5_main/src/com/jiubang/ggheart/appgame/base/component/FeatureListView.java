package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 精品推荐listview，可展示加载下一页的footview
 * 
 * @author xiedezhi
 * 
 */
public class FeatureListView extends ListView {

	private Context mContext;

	private LayoutInflater mInflater;
	/**
	 * footerview的根视图，包括进度条，重试按钮和文字
	 */
	private View mRootFooterView;
	/**
	 * 进度条footerview
	 */
	private View mProgressFooterView;
	/**
	 * 重试按钮footerview
	 */
	private View mRetryFooterView;
	/**
	 * 文字footerview
	 */
	private View mTextFooterView;

	public FeatureListView(Context context) {
		super(context);
		init(context);
	}

	public FeatureListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FeatureListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * 检查是否所有的footer都不为空
	 * 
	 * @return 所有footerview不为空则返回true，否则返回false
	 */
	private boolean checkFooterView() {
		if (mRootFooterView != null && mProgressFooterView != null && mRetryFooterView != null
				&& mTextFooterView != null) {
			return true;
		}
		return false;
	}

	/**
	 * 初始化
	 */
	private void init(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		// 初始化footview
		mRootFooterView = mInflater.inflate(R.layout.apps_mgr_listview_foot_more, null);
		mProgressFooterView = mRootFooterView.findViewById(R.id.apps_mgr_listview_foot_loading);
		mTextFooterView = mRootFooterView.findViewById(R.id.apps_mgr_listview_foot_end);
		mRetryFooterView = mRootFooterView.findViewById(R.id.apps_mgr_listview_foot_retry);
	}

	/**
	 * 假如没有添加footerview，就把footerview加入到listview，并把footerview设为GONE
	 */
	public void addFooterView() {
		if (checkFooterView() && this.getFooterViewsCount() < 1) {
			this.addFooterView(mRootFooterView);
			mRootFooterView.setVisibility(View.GONE);
		}
	}

	/**
	 * 移除footerview
	 */
	public void removeFooterView() {
		mRootFooterView.setVisibility(View.GONE);
		mProgressFooterView.setVisibility(View.GONE);
		mTextFooterView.setVisibility(View.GONE);
		mRetryFooterView.setVisibility(View.GONE);
	}

	/**
	 * 显示加载下一页的进度条
	 */
	public void showProgressFooterView() {
		if (checkFooterView()) {
			addFooterView();
			mRootFooterView.setVisibility(View.VISIBLE);
			mProgressFooterView.setVisibility(View.VISIBLE);
			mTextFooterView.setVisibility(View.GONE);
			mRetryFooterView.setVisibility(View.GONE);
		}

	}

	/**
	 * 显示加载失败时的重试按钮
	 * 
	 * @param listener
	 *            重试按钮点击监听器
	 */
	public void showRetryFooterView(OnClickListener listener) {
		if (checkFooterView()) {
			addFooterView();
			mRootFooterView.setVisibility(View.VISIBLE);
			mProgressFooterView.setVisibility(View.GONE);
			mTextFooterView.setVisibility(View.GONE);
			mRetryFooterView.setVisibility(View.VISIBLE);
			// 设置重试点击事件
			Button retryBtn = (Button) mRetryFooterView
					.findViewById(R.id.apps_mgr_listview_foot_retry_btn);
			retryBtn.setOnClickListener(listener);
		}
	}

	/**
	 * 设置文字footerview
	 * 
	 * @param text
	 *            文字
	 */
	public void showTextFooterView(String text) {
		if (checkFooterView()) {
			addFooterView();
			mRootFooterView.setVisibility(View.VISIBLE);
			mProgressFooterView.setVisibility(View.GONE);
			mTextFooterView.setVisibility(View.VISIBLE);
			mRetryFooterView.setVisibility(View.GONE);
			// 设置footerview文字
			TextView tv = (TextView) mTextFooterView
					.findViewById(R.id.apps_mgr_listview_foot_end_tip);
			tv.setText(text);
		}
	}

}
