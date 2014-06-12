package com.jiubang.ggheart.apps.appmanagement.help;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;

public class NetworkTipsTool {

	private Context mContext;
	private ViewGroup mRoot;
	private LinearLayout mProgressLinearLayout; // 进度条
	private RelativeLayout mNoUpdateDataTipRelativeLayout; // 没有结果提示

	public static final String[] LANGUAGE = { "zh", "en" };

	public NetworkTipsTool(ViewGroup root) {
		mContext = root.getContext();
		mRoot = root;
		initProgress();
		initNoDataTipRelativeLayout();
	}

	private void initProgress() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		mProgressLinearLayout = (LinearLayout) layoutInflater.inflate(
				R.layout.themestore_btmprogress, null);
	}

	private void initNoDataTipRelativeLayout() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		mNoUpdateDataTipRelativeLayout = (RelativeLayout) layoutInflater.inflate(
				R.layout.appsmanagement_nodata_tip_full, null);

	}

	public void showProgress() {
		mRoot.removeView(mNoUpdateDataTipRelativeLayout);
		if (mProgressLinearLayout != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			mRoot.removeView(mProgressLinearLayout);
			mRoot.addView(mProgressLinearLayout, params);
		}
	}

	public void dismissProgress() {
		mRoot.removeView(mProgressLinearLayout);
	}

	public void showNoUpdateDataTip() {
		mRoot.removeAllViews();
		if (mNoUpdateDataTipRelativeLayout != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			ImageView bg = (ImageView) mNoUpdateDataTipRelativeLayout.findViewById(R.id.background);
			ImageView info = (ImageView) mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.appsmanagement_network_error_text);
			bg.setBackgroundResource(R.drawable.appsmanagement_all_up_to_date);
			mRoot.removeView(mNoUpdateDataTipRelativeLayout);
			mRoot.addView(mNoUpdateDataTipRelativeLayout, params);
			if (LANGUAGE[0].equals(Machine.getLanguage(mRoot.getContext()))) {
				info.setBackgroundResource(R.drawable.appsmanagement_no_update_zh);
			} else {
				info.setBackgroundResource(R.drawable.appsmanagement_no_update_en);
			}
			// if (info.getVisibility() == View.VISIBLE) {
			// info.setVisibility(View.GONE);
			// }
		}
	}

	// public void showNetworkErrorTip(){
	// mRoot.removeAllViews();
	// if (mNoUpdateDataTipRelativeLayout != null) {
	// LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	// LinearLayout.LayoutParams.FILL_PARENT,
	// LinearLayout.LayoutParams.WRAP_CONTENT);
	// ImageView bg =
	// (ImageView)mNoUpdateDataTipRelativeLayout.findViewById(R.id.background);
	// ImageView info =
	// (ImageView)mNoUpdateDataTipRelativeLayout.findViewById(R.id.appsmanagement_network_error_text);
	//
	// bg.setBackgroundResource(R.drawable.appsmanagement_network_exception_bg);
	// if (language[0].equals(Machine.getLanguage(mRoot.getContext()))) {
	// info.setBackgroundResource(R.drawable.appsmanagement_network_exception_zh);
	// } else {
	// info.setBackgroundResource(R.drawable.appsmanagement_network_exception_en);
	// }
	//
	// if (info.getVisibility() == View.GONE) {
	// info.setVisibility(View.VISIBLE);
	// }
	//
	// mRoot.removeView(mNoUpdateDataTipRelativeLayout);
	// mRoot.addView(mNoUpdateDataTipRelativeLayout, params);
	// }
	// }

	public void showErrorTip(boolean isNetworkError) {
		mRoot.removeAllViews();
		if (mNoUpdateDataTipRelativeLayout != null) {

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			// ImageView bg =
			// (ImageView)mNoUpdateDataTipRelativeLayout.findViewById(R.id.background);
			ImageView info = (ImageView) mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.appsmanagement_network_error_text);

			if (isNetworkError) {
				// 网络异常的提示信息
				if (LANGUAGE[0].equals(Machine.getLanguage(mRoot.getContext()))) {
					info.setBackgroundResource(R.drawable.appsmanagement_network_exception_zh);
				} else {
					info.setBackgroundResource(R.drawable.appsmanagement_network_exception_en);
				}
			} else {
				// sdcard异常的提示信息
				if (LANGUAGE[0].equals(Machine.getLanguage(mRoot.getContext()))) {
					info.setBackgroundResource(R.drawable.appsmanagement_sdcard_exception_zh);
				} else {
					info.setBackgroundResource(R.drawable.appsmanagement_sdcard_exception_en);
				}
			}

			mRoot.removeView(mNoUpdateDataTipRelativeLayout);
			mRoot.addView(mNoUpdateDataTipRelativeLayout, params);
		}
	}

	// public void showSdcardErrorTip(){
	// mRoot.removeAllViews();
	// if (mNoUpdateDataTipRelativeLayout != null) {
	// LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	// LinearLayout.LayoutParams.FILL_PARENT,
	// LinearLayout.LayoutParams.WRAP_CONTENT);
	// ImageView bg =
	// (ImageView)mNoUpdateDataTipRelativeLayout.findViewById(R.id.background);
	// ImageView info =
	// (ImageView)mNoUpdateDataTipRelativeLayout.findViewById(R.id.appsmanagement_network_error_text);
	// if (language[0].equals(Machine.getLanguage(mRoot.getContext()))) {
	// bg.setBackgroundResource(R.drawable.appsmanagement_sdcard_exception_bg_zh);
	// } else {
	// bg.setBackgroundResource(R.drawable.appsmanagement_sdcard_exception_bg_en);
	// }
	//
	// if (info.getVisibility() == View.VISIBLE) {
	// info.setVisibility(View.GONE);
	// }
	//
	// mRoot.removeView(mNoUpdateDataTipRelativeLayout);
	// mRoot.addView(mNoUpdateDataTipRelativeLayout, params);
	// }
	// }

	public void dismissTip() {
		mRoot.removeView(mNoUpdateDataTipRelativeLayout);
	}

	public void showNothing() {
		mRoot.removeAllViews();
	}
}
