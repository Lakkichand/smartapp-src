package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;

/**
 * 
 * <br>类描述:桌面设置-自定义对话框基类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-12]
 */
public class DeskSettingBaseDialog extends Dialog {
	public Context mContext = null;
	public DeskSettingInfo mDeskSettingInfo;
	public OnDialogSelectListener mOnDialogSelectListener;
	public Button mOkButton;
	public Button mCancelButton;
	public LinearLayout mDialogLayout;

	public DeskSettingBaseDialog(Context context, DeskSettingInfo deskSettingInfo,
			OnDialogSelectListener onDialogSelectListener) {
		super(context, R.style.Dialog);
		mContext = context;
		mDeskSettingInfo = deskSettingInfo;
		mOnDialogSelectListener = onDialogSelectListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mContext == null || mDeskSettingInfo == null || mOnDialogSelectListener == null) {
			return;
		}

		View view = getView();
		if (view != null) {
			DialogBase.setDialogWidth(mDialogLayout, mContext);
			setContentView(view);
		}
	}

	public View getView() {
		return null;
	}

	public void setOkButtonVisible(int visible) {
		if (mOkButton != null) {
			mOkButton.setVisibility(visible);
		}
	}

	public void setCancelButtonVisible(int visible) {
		if (mCancelButton != null) {
			mCancelButton.setVisibility(visible);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		//对话框关闭时遍历所有控件，把DeskView和DeskButton反注册
		DeskSettingConstants.selfDestruct(getWindow().getDecorView());
	}

}
