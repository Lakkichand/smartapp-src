package com.jiubang.ggheart.appgame.base.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 
 * <br>类描述: 进度条和异常显示类
 * <br>功能详细描述:
 * 
 * @author  zhoujun
 * @date  [2012-10-18]
 */
public class NetworkTipsTool {

	public static final int TYPE_NONE = 0;
	public static final int TYPE_NETWORK_EXCEPTION = 1;
	public static final int TYPE_SDCARD_EXCEPTION = 2;
	public static final int TYPE_NO_GAMES = 3;
	public static final int TYPE_NO_DOWNLOAD_ITEM = 4;
	public static final int TYPE_NO_UPDATE = 5;
	public static final int TYPE_NO_APPS_ON_SDCARD = 6;
	public static final int TYPE_NO_APPS_ON_PHONE = 7;
	public static final int TYPE_NO_APPS_UNMOVABLE = 8;
	
	
	private Context mContext;
	private ViewGroup mRoot;
	private LinearLayout mProgressLinearLayout; // 进度条
	private RelativeLayout mNoUpdateDataTipRelativeLayout; // 没有结果提示

	public NetworkTipsTool(ViewGroup root) {
		mContext = root.getContext();
		mRoot = root;
	}

	private void initProgress() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		mProgressLinearLayout = (LinearLayout) layoutInflater.inflate(
				R.layout.appgame_btmprogress, null);
	}

	private void initNoDataTipRelativeLayout() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		mNoUpdateDataTipRelativeLayout = (RelativeLayout) layoutInflater.inflate(
				R.layout.recomm_appsmanagement_nodata_tip_full, null);
	}

	public void showProgress() {
		mRoot.setVisibility(View.VISIBLE);
		mRoot.removeAllViews();

		if (mProgressLinearLayout == null) {
			initProgress();
		}

		if (mProgressLinearLayout != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			mRoot.addView(mProgressLinearLayout, params);
		}
	}

	public void dismissProgress() {
		if (mProgressLinearLayout != null) {
			mRoot.removeView(mProgressLinearLayout);
		}
	}

	public void removeProgress() {
		showNothing();
	}

	/**
	 * 显示错误重试页并显示反馈按钮
	 * 
	 * @param retryListener
	 *            重试按钮点击监听器
	 * @param feedbackListener
	 *            反馈按钮点击监听器
	 * 
	 * @author xiedezhi
	 */
	public void showRetryErrorTip(OnClickListener retryListener, OnClickListener feedbackListener) {
		mRoot.setVisibility(View.VISIBLE);
		mRoot.removeAllViews();

		if (mNoUpdateDataTipRelativeLayout == null) {
			initNoDataTipRelativeLayout();
		}

		if (mNoUpdateDataTipRelativeLayout != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

			Button retryBtn = (Button) mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.retrybutton);
			retryBtn.setText(R.string.apps_recomm_network_refresh);
			retryBtn.setVisibility(View.VISIBLE);
			retryBtn.setOnClickListener(retryListener);
			
			View retryWithFeedBack = mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.appgame_error_feedback);
			retryWithFeedBack.setVisibility(View.VISIBLE);
			retryWithFeedBack.setOnClickListener(feedbackListener);
			
			// 网络异常的提示信息
			TextView errorText = (TextView) mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.appgame_error_text);
			errorText.setText(R.string.appgame_network_error_message);
			
			ImageView image = (ImageView) mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.appgame_error_image);
			image.setImageResource(R.drawable.appsmanagement_network_exception_bg);
			
			View tip = mNoUpdateDataTipRelativeLayout.findViewById(R.id.appgame_error_nettip);
			tip.setVisibility(View.GONE);

			mRoot.addView(mNoUpdateDataTipRelativeLayout, params);
		}
	}

	public void showRetryErrorTip(OnClickListener listener, boolean isNetworkError) {
		int errorType = TYPE_NETWORK_EXCEPTION;
		if (!isNetworkError) {
			errorType = TYPE_SDCARD_EXCEPTION;
		}
		showRetryErrorTip(listener, errorType);
	}

	public void showRetryErrorTip(OnClickListener listener, int errorType) {
		mRoot.setVisibility(View.VISIBLE);
		mRoot.removeAllViews();
		if (mNoUpdateDataTipRelativeLayout == null) {
			initNoDataTipRelativeLayout();
		}

		if (mNoUpdateDataTipRelativeLayout != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			TextView errorText = (TextView) mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.appgame_error_text);
			Button retryBtn = (Button) mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.retrybutton);
			ImageView image = (ImageView) mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.appgame_error_image);
			View retryWithFeedBack = mNoUpdateDataTipRelativeLayout
					.findViewById(R.id.appgame_error_feedback);
			retryWithFeedBack.setVisibility(View.GONE);
			View tip = mNoUpdateDataTipRelativeLayout.findViewById(R.id.appgame_error_nettip);
			tip.setVisibility(View.GONE);

			if (listener != null) {
				retryBtn.setVisibility(View.VISIBLE);
				retryBtn.setOnClickListener(listener);
				retryBtn.setText(R.string.apps_recomm_network_refresh);
			} else {
				retryBtn.setVisibility(View.GONE);
			}

			// 网络异常的提示信息
			int errorMessage = R.string.appgame_network_error_message;
			int errorDrawable = R.drawable.appsmanagement_network_exception_bg;

			if (errorType == TYPE_SDCARD_EXCEPTION) {
				//sdcard 异常
				errorMessage = R.string.appgame_sdcard_error_message;
				errorDrawable = R.drawable.appsmanagement_sdcard_exception_bg;
			} else if (errorType == TYPE_NO_UPDATE) {
				//没有应用更新
				errorMessage = R.string.appgame_no_update_message;
				errorDrawable = R.drawable.appsmanagement_no_update_bg;
				retryBtn.setText(R.string.refresh);
			} else if (errorType == TYPE_NO_DOWNLOAD_ITEM) {
				errorMessage = R.string.appgame_no_download_message;
				errorDrawable = R.drawable.appsmanagement_no_download_bg;
				if (listener != null) {
					int paddingLeft = mContext.getResources().getDimensionPixelSize(
							R.dimen.appcenter_list_item_padding);
					retryBtn.setPadding(paddingLeft, 0, paddingLeft, 0);
					retryBtn.setText(R.string.download_manager_get_more_apps);
				}
			} else if (errorType == TYPE_NO_GAMES) {
				errorMessage = R.string.appgame_no_game_message;
				errorDrawable = R.drawable.appsmanagement_no_game_bg;
			} else if (errorType == TYPE_NO_APPS_ON_PHONE) {
				errorMessage = R.string.appgame_migration_no_application_on_phone;
				errorDrawable = R.drawable.appgame_appmigration_no_item;
			} else if (errorType == TYPE_NO_APPS_ON_SDCARD) {
				errorMessage = R.string.appgame_migration_no_application_on_sdcard;
				errorDrawable = R.drawable.appgame_appmigration_no_item;
			} else if (errorType == TYPE_NO_APPS_UNMOVABLE) {
				errorMessage = R.string.appgame_migration_no_application_unmovable;
				errorDrawable = R.drawable.appgame_appmigration_no_item;
			}

			errorText.setText(errorMessage);
			image.setImageResource(errorDrawable);

			mRoot.addView(mNoUpdateDataTipRelativeLayout, params);
		}
	}

	public void dismissTip() {
		if (mNoUpdateDataTipRelativeLayout != null) {
			mRoot.removeView(mNoUpdateDataTipRelativeLayout);
		}
	}

	public void showNothing() {
		mRoot.removeAllViews();
	}

}