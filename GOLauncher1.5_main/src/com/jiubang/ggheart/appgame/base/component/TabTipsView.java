package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 显示进度条和异常信息，包括网络异常和sdcard异常
 * 
 * @author zhoujun
 * 
 */
public class TabTipsView extends LinearLayout {

	private Context mContext;
	
	private LayoutInflater mInflater;
	
	private LinearLayout.LayoutParams mParams;
	// 进度条
	private RelativeLayout mProgressLinearLayout;
	// 没有结果提示
	private RelativeLayout mErrorView;

	public TabTipsView(Context context) {
		super(context);
		initView(context);
	}

	public TabTipsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mParams = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
	}
	
	/**
	 * 初始化进度条
	 */
	private void initProgress() {
		mProgressLinearLayout = (RelativeLayout) mInflater.inflate(
				R.layout.appgame_tabtips_progress, null);
		ProgressBar progressBar = (ProgressBar) mProgressLinearLayout.findViewById(R.id.themestore_btmprogress);
		progressBar.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		Drawable drawable = getContext().getResources().getDrawable(R.drawable.go_progress_green);
		progressBar.setIndeterminateDrawable(drawable);
		mProgressLinearLayout.setVisibility(View.GONE);
	}
	
	/**
	 * 初始化错误提示页
	 */
	private void initErrorTip() {
		mErrorView = (RelativeLayout) mInflater.inflate(
				R.layout.recomm_appsmanagement_nodata_tip_full, null);
		mErrorView.setVisibility(View.GONE);
	}

	/**
	 * 显示错误提示信息，没有反馈按钮
	 * 
	 * @param listener
	 * @param isNetworkOK
	 *            网络是否正常连接，如果是则显示重试按钮，如果不是则显示设置按钮
	 */
	public void showRetryErrorTip(boolean isNetworkOK, OnClickListener listener, String title) {
		// 隐藏进度条，显示错误信息
		this.removeView(mProgressLinearLayout);
		if (mErrorView == null) {
			initErrorTip();
		}
		if (mErrorView.getParent() == null) {
			this.addView(mErrorView, mParams);
		}
		if (mErrorView != null) {
			mErrorView.setVisibility(View.VISIBLE);
			TextView textview = (TextView) mErrorView.findViewById(R.id.appgame_error_title);
			if (title == null || title.trim().equals("")) {
				textview.setVisibility(View.GONE);
			} else {
				textview.setVisibility(View.VISIBLE);
				textview.setText(title);
			}
			View retryAndFeedback = mErrorView.findViewById(R.id.appgame_error_feedback);
			retryAndFeedback.setVisibility(View.GONE);

			Button retryBtn = (Button) mErrorView.findViewById(R.id.retrybutton);
			retryBtn.setVisibility(View.VISIBLE);
			retryBtn.setOnClickListener(listener);
			if (isNetworkOK) {
				retryBtn.setText(R.string.apps_recomm_network_refresh);
				View tip = mErrorView.findViewById(R.id.appgame_error_nettip);
				tip.setVisibility(View.GONE);
			} else {
				retryBtn.setText(R.string.appgame_menu_item_setting);
				View tip = mErrorView.findViewById(R.id.appgame_error_nettip);
				tip.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 显示错误提示信息，有反馈按钮
	 * 
	 * @param retryListener
	 * @param feedbackListener
	 * @param isNetworkOK
	 *            网络是否正常连接，如果是则显示重试按钮，如果不是则显示设置按钮
	 */
	public void showRetryErrorTip(boolean isNetworkOK, OnClickListener retryListener,
			OnClickListener feedbackListener, String title) {
		// 隐藏进度条，显示错误信息
		this.removeView(mProgressLinearLayout);
		if (mErrorView == null) {
			initErrorTip();
		}
		if (mErrorView.getParent() == null) {
			this.addView(mErrorView, mParams);
		}
		if (mErrorView != null) {
			mErrorView.setVisibility(View.VISIBLE);
			TextView textview = (TextView) mErrorView.findViewById(R.id.appgame_error_title);
			if (title == null || title.trim().equals("")) {
				textview.setVisibility(View.GONE);
			} else {
				textview.setVisibility(View.VISIBLE);
				textview.setText(title);
			}
			Button retryBtn = (Button) mErrorView.findViewById(R.id.retrybutton);
			retryBtn.setOnClickListener(retryListener);
			retryBtn.setVisibility(View.VISIBLE);
			if (isNetworkOK) {
				retryBtn.setText(R.string.apps_recomm_network_refresh);
				View tip = mErrorView.findViewById(R.id.appgame_error_nettip);
				tip.setVisibility(View.GONE);
			} else {
				retryBtn.setText(R.string.appgame_menu_item_setting);
				View tip = mErrorView.findViewById(R.id.appgame_error_nettip);
				tip.setVisibility(View.VISIBLE);
			}

			View retryAndFeedback = mErrorView.findViewById(R.id.appgame_error_feedback);
			retryAndFeedback.setVisibility(View.VISIBLE);
			retryAndFeedback.setOnClickListener(feedbackListener);
		}
	}

	/**
	 * 取消错误提示
	 */
	public void removeRetryErrorTip() {
		this.removeView(mErrorView);
	}

	/**
	 * 显示进度条
	 */
	public void showProgress(String title) {
		this.removeView(mErrorView);
		if (mProgressLinearLayout == null) {
			initProgress();
		}
		if (mProgressLinearLayout.getParent() == null) {
			this.addView(mProgressLinearLayout, mParams);
		}
		if (mProgressLinearLayout != null) {
			mProgressLinearLayout.setVisibility(View.VISIBLE);
		}
	}
	/**
	 * 取消进度条
	 */
	public void removeProgress() {
		this.removeView(mProgressLinearLayout);
	}
	
	/**
	 * 移除所有视图
	 */
	public void showNothing() {
		this.removeAllViews();
	}

}
