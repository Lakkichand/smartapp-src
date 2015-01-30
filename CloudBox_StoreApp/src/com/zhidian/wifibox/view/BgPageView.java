/**
 * 
 */
package com.zhidian.wifibox.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zhidian.wifibox.R;

/**
 * 加载数据中显示界面
 * 
 * @author zhaoyl
 * 
 */
public class BgPageView implements OnClickListener {
	private Context context;
	private LinearLayout linear_proLayout; // 加载数据进度条
	private View linear_connetLayout;// 加载成功后原界面要显示的内容
	private onCallBackOnClickListener backOnClickListener;

	public interface onCallBackOnClickListener {
		void onClick();
	}

	public BgPageView(Context context, LinearLayout linear_proLayout,
			View linear_connetLayout) {
		this.context = context;
		this.linear_proLayout = linear_proLayout;
		this.linear_connetLayout = linear_connetLayout;
	}

	// 显示进度
	public void showProgress() {
		linear_proLayout.setVisibility(View.VISIBLE);
		linear_connetLayout.setVisibility(View.GONE);
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.mydialog, null);
		linear_proLayout.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		linear_proLayout.addView(view, lp);

	}

	// 取消进度
	public void showContent() {
		linear_proLayout.removeAllViews();
		linear_proLayout.setVisibility(View.GONE);
		linear_connetLayout.setVisibility(View.VISIBLE);
	}

	
	public void showLoadException(onCallBackOnClickListener listener) {
		linear_connetLayout.setVisibility(View.GONE);
		linear_proLayout.setVisibility(View.VISIBLE);
		View view = ((Activity) context).getLayoutInflater().inflate(
				R.layout.loadingexeception, null);
		
		Button btnBackIndex = (Button)view.findViewById(R.id.back_index);
		btnBackIndex.setOnClickListener(this);
		Button btnSetNetWork = (Button) view
				.findViewById(R.id.seting_network);
		btnSetNetWork.setOnClickListener(this);

		linear_proLayout.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		linear_proLayout.addView(view, lp);
		backOnClickListener = listener;
	}
	
	/**
	 * 无网络连接
	 * @param listener
	 */
	public void showNoNetWorkException() {
		linear_connetLayout.setVisibility(View.GONE);
		linear_proLayout.setVisibility(View.VISIBLE);
		View view = ((Activity) context).getLayoutInflater().inflate(
				R.layout.view_no_network, null);
		Button btnBackIndex = (Button)view.findViewById(R.id.seting_network);
		btnBackIndex.setOnClickListener(this);
		
		linear_proLayout.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		linear_proLayout.addView(view, lp);
	}

	/**
	 * 提示内容正在更新
	 */
	public void showUpdating(OnClickListener listener) {
		linear_connetLayout.setVisibility(View.GONE);
		linear_proLayout.setVisibility(View.VISIBLE);
		View view = ((Activity) context).getLayoutInflater().inflate(
				R.layout.updating_page, null);
		Button btnSetNetWork = (Button) view
				.findViewById(R.id.loading_exception_set);
		btnSetNetWork.setOnClickListener(listener);
		linear_proLayout.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		linear_proLayout.addView(view, lp);
	}

	/**
	 * 展示没有内容的提示
	 */
	public void showNoContentTip() {
		linear_proLayout.setVisibility(View.VISIBLE);
		linear_connetLayout.setVisibility(View.GONE);
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.nothing_page, null);
		linear_proLayout.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		linear_proLayout.addView(view, lp);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_index:
			if (backOnClickListener != null) {
				linear_proLayout.setVisibility(View.VISIBLE);
				linear_connetLayout.setVisibility(View.GONE);
				backOnClickListener.onClick();
			}
			break;

		case R.id.seting_network:
			context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			break;
		}
	}

}
