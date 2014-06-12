/**
 * 
 */
package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * @author liuxinyang
 * 
 */
public class AppsManagementSearchFooterView extends LinearLayout {
	private ImageView mWarningIcon = null;
	private ProgressBar mProgressBar;
	private TextView mTextView;
	private Button mRetryBtn;
	private ImageView mErrorIcon;

	private int mState = STATE_GONE;

	public static final int STATE_GONE = -1;
	public static final int STATE_LOADING = 1;
	public static final int STATE_RETRY = 2;
	public static final int STATE_MORE = 3;

	public AppsManagementSearchFooterView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		mWarningIcon = (ImageView) this.findViewById(R.id.apps_mgr_listview_foot_warning);
		mProgressBar = (ProgressBar) this.findViewById(R.id.apps_management_search_progress);
		mTextView = (TextView) this.findViewById(R.id.apps_management_search_text);
		mRetryBtn = (Button) this.findViewById(R.id.apps_management_search_retry);
		mErrorIcon = (ImageView) this.findViewById(R.id.apps_mgr_listview_foot_warning);
		Drawable drawable = getContext().getResources().getDrawable(R.drawable.go_progress_green);
		mProgressBar.setIndeterminateDrawable(drawable);
		super.onFinishInflate();
	}

	public void showLoading() {
		mState = STATE_LOADING;
		this.setVisibility(View.VISIBLE);
		mWarningIcon.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
		mTextView.setVisibility(View.VISIBLE);
		mTextView.setText(getContext().getString(R.string.apps_management_search_loading));
		mTextView.setTextColor(0xFF000000);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
		mRetryBtn.setVisibility(View.GONE);
		mErrorIcon.setVisibility(View.GONE);
	}

	public void showRetry(OnClickListener listener) {
		mState = STATE_RETRY;
		this.setVisibility(View.VISIBLE);
		mWarningIcon.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		mTextView.setTextColor(0xFF757575);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.667f);
		mTextView.setVisibility(View.VISIBLE);
		mTextView.setText(getContext().getString(R.string.apps_mgr_listview_foot_net_error));
		mRetryBtn.setVisibility(View.VISIBLE);
		mErrorIcon.setVisibility(View.VISIBLE);
		mRetryBtn.setOnClickListener(listener);
	}

	public void showMore() {
		mState = STATE_MORE;
		this.setVisibility(View.VISIBLE);
		mWarningIcon.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.VISIBLE);
		mTextView.setText(getContext().getString(R.string.apps_management_search_more));
		mTextView.setTextColor(0xFF000000);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
		mRetryBtn.setVisibility(View.GONE);
		mErrorIcon.setVisibility(View.GONE);
	}

	public void viewGone() {
		mState = STATE_GONE;
		this.setVisibility(View.GONE);
		mWarningIcon.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.GONE);
		mRetryBtn.setVisibility(View.GONE);
		mErrorIcon.setVisibility(View.GONE);
	}

	public int getState() {
		return mState;
	}
}
