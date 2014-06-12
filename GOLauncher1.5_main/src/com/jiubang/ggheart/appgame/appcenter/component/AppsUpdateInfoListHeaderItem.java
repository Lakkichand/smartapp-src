package com.jiubang.ggheart.appgame.appcenter.component;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 
 * <br>类描述: 应用更新了列表中，加入的“所有应用” 和“可更新应用”列表项
 * <br>功能详细描述:
 * 
 * @author  zhoujun
 * @date  [2012-10-17]
 */
public class AppsUpdateInfoListHeaderItem extends LinearLayout implements View.OnClickListener {

	public AppsUpdateInfoListHeaderItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
//	private AppBean mAppBean;
//	private TextView mAppNameTextView;
//	private ImageView mAppMoreImage;
//	private Button mButton;
//
//	public AppsUpdateInfoListHeaderItem(Context context, AttributeSet attrs) {
//		super(context, attrs);
//	}
//
//	public AppsUpdateInfoListHeaderItem(Context context) {
//		super(context);
//	}
//
//	@Override
//	protected void onFinishInflate() {
//		super.onFinishInflate();
//		init();
//	}
//
//	/**
//	 * 初始化方法
//	 */
//	private void init() {
//		mAppNameTextView = (TextView) findViewById(R.id.appcenter_update_list_head_text);
//		mAppMoreImage = (ImageView) findViewById(R.id.appcenter_update_list_head_image);
//		mButton = (Button) findViewById(R.id.appcenter_update_list_head_button);
//		mButton.setOnClickListener(this);
//		mButton.setVisibility(View.GONE);
//	}
//
//	/**
//	 * 重置默认状态的方法
//	 */
//	public void resetDefaultStatus() {
//		this.setTag(null);
//		mAppBean.setAppBeanStatusChangeListener(null);
//		mAppBean.setAppBeanDownloadListener(null);
//		mAppBean = null;
//		if (mAppNameTextView != null) {
//			mAppNameTextView.setText("");
//		}
//		if (mButton != null) {
//			mButton.setVisibility(View.VISIBLE);
////			ButtonUtils.setButtonTextSize(mButton);
//			mButton.setEnabled(true);
//			mButton.setBackgroundResource(R.drawable.appgame_install_btn_selector);
//			mButton.setText(R.string.apps_management_operation_button_update_all_label);
////			mButton.setShadowLayer(1, 0, -1, 0xffad7300);
//		}
//	}
//
//	public void destory() {
//		mAppNameTextView = null;
//		mAppMoreImage = null;
//		if (mButton != null) {
//			mButton.setOnClickListener(null);
//			mButton = null;
//		}
//		mAppBean = null;
//	}
//
//	private void setAppName(String name) {
//		if (mAppNameTextView != null) {
//			mAppNameTextView.setText(name);
//		}
//	}
//
//	public void bindAppBean(final Context context, final int position, AppBean appBean) {
//		mAppBean = appBean;
//		setTag(mAppBean);
//		setAppName(mAppBean.mAppName);
//		if (position == 0) {
//			mAppMoreImage.setVisibility(View.VISIBLE);
//			mButton.setVisibility(View.GONE);
//		} else {
//			mAppMoreImage.setVisibility(View.GONE);
//			mButton.setVisibility(View.VISIBLE);
//			mButton.setText(R.string.apps_management_operation_button_update_all_label);
//			changeOperationType(appBean.getStatus());
//		}
//	}
//
//	public void changeOperationType(int type) {
//		switch (type) {
//			case AppsUpdateViewContainer.OPERATION_TYPE_UPDATE_ALL :
//				mButton.setText(R.string.apps_management_operation_button_update_all_label);
//				mButton.setClickable(true);
//				break;
//			case AppsUpdateViewContainer.OPERATION_TYPE_CANCEL_ALL :
//				mButton.setText(R.string.apps_management_operation_button_cancel_all_label);
//				mButton.setClickable(true);
//				break;
//			case AppsUpdateViewContainer.OPERATION_TYPE_OPERATING_CANCEL :
//			case AppsUpdateViewContainer.OPERATION_TYPE_OPERATING_UPDATE :
//				mButton.setClickable(false);
//				break;
//		}
//	}
//
//	@Override
//	public void onClick(View v) {
//		AppsManagementActivity.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
//				IDiyMsgIds.APPCENTER_APPMANAGER_ALL_UPDATE_OR_CANCEL, 0, null, null);
//	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}