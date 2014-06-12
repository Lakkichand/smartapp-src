package com.zhidian.wifibox.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhidian.wifibox.R;


/***
 * ListView底部加载更多View
 * @author Zhaoyl
 *
 */
public class ListViewSearchFooterView extends LinearLayout {

	private ProgressBar progressBar;
	private TextView tvText;
	private Button btnAfresh; //重新加载
	
	private int mState = STATE_GONE;

	public static final int STATE_GONE = -1;
	public static final int STATE_LOADING = 1;
	public static final int STATE_RETRY = 2;
	public static final int STATE_MORE = 3;
	
	public ListViewSearchFooterView(Context context) {
		super(context);
	}
	
	public ListViewSearchFooterView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**********************
	 * 初始化UI
	 *********************/
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		progressBar = (ProgressBar) findViewById(R.id.footer_search_progress);
		tvText = (TextView) findViewById(R.id.footer_search_text);
		btnAfresh = (Button) findViewById(R.id.footer_search_retry);
	}
	
	/**********************
	 * 加载中
	 *********************/
	public void showLoading(){
		mState = STATE_LOADING;
		this.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		btnAfresh.setVisibility(View.GONE);
		tvText.setVisibility(View.VISIBLE);
		tvText.setText("加载中...");
		
		
	}
	
	/**********************
	 * 显示 查看更多
	 *********************/
	public void showMore(){
		mState = STATE_MORE;
		this.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		btnAfresh.setVisibility(View.GONE);
		tvText.setVisibility(View.VISIBLE);
		tvText.setText("查看更多");
		
	}
	
	/**********************
	 * 隐藏所有
	 *********************/
	public void viewGone(){
		mState = STATE_GONE;
		progressBar.setVisibility(View.GONE);
		btnAfresh.setVisibility(View.GONE);
		tvText.setVisibility(View.GONE);
		
	}
	
	/**********************
	 * 显示 重试
	 *********************/
	public void showRetry(OnClickListener listener) {
		mState = STATE_RETRY;
		this.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		btnAfresh.setVisibility(View.VISIBLE);
		tvText.setVisibility(View.GONE);
		btnAfresh.setOnClickListener(listener);
	}
	
	/**********************
	 * 获取状态 
	 *********************/
	public int getState(){
		return mState;		
	}

}
