package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;

/**
 * 
 * <br>类描述:带checkbox的单选对话框
 * <br>功能详细描述:
 * 
 * @author  chenguanyu
 * @date  [2012-9-12]
 */
public class DeskSettingSingleChoiceWithCheckboxDialog extends DeskSettingSingleChoiceDialog {

	public DeskSettingSingleChoiceWithCheckboxDialog(Context context,
			DeskSettingInfo deskSettingInfo, OnDialogSelectListener onDialogSelectListener) {
		super(context, deskSettingInfo, onDialogSelectListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initCheckBox();
		if (mOkButton != null) {
			mOkButton.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * <br>功能简述:初始化CheckBox信息
	 * <br>功能详细描述:判断是否带CheckBox对话框
	 * <br>注意:要修改一下高度和相对位置
	 * @param view
	 */
	public void initCheckBox() {
		int paddingheightPx = (int) mContext.getResources().getDimension(
				R.dimen.desk_setting_single_button_with_checkbox_height);
		LinearLayout.LayoutParams params1 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		params1.bottomMargin = paddingheightPx;
		LinearLayout listviewLayout = (LinearLayout) mView
				.findViewById(R.id.desk_setting_listview_layout);
		listviewLayout.setLayoutParams(params1);

		LinearLayout.LayoutParams params2 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		LinearLayout buttonLayout = (LinearLayout) mView
				.findViewById(R.id.desk_setting_dialog_buttons);
		params2.topMargin = -paddingheightPx;
		params2.gravity = Gravity.BOTTOM;
		buttonLayout.setLayoutParams(params2);

		CheckBox checkBox = (CheckBox) mView.findViewById(R.id.desk_setting_dialog_with_checkbox);
		checkBox.setVisibility(View.VISIBLE);
		//设置CheckBox提示语
		String checkBoxString = mDeskSettingInfo.getSingleInfo().getCheckBoxString();
		if (checkBoxString != null) {
			checkBox.setText(checkBoxString);
		}

		checkBox.setChecked(mDeskSettingInfo.getSingleInfo().getCheckBoxIsCheck());

		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mDeskSettingInfo.getSingleInfo().setCheckBoxIsCheck(isChecked); //修改选择数据
			}
		});
	}

	/**
	 * 重写closeDialog,由于带CheckBox的不需要关闭对话框
	 */
	@Override
	public void closeDialog() {
		//mDingleChoiceDialog.dismiss();
	}

	/**
	 * <br>功能简述:刷新View
	 * <br>功能详细描述:选择自定义后，前一个dialog没有关闭。需要刷新最后一项自定义的显示内容
	 * <br>注意:
	 */
	public void updateView() {
		int custonPostion = mListView.getAdapter().getCount() - 2; //自定义放在最后一个位置
		CharSequence[] entries = mDeskSettingInfo.getSingleInfo().getEntries();
		View view = mListView.getChildAt(custonPostion);
		if (view != null) {
			TextView textView = (TextView) view.findViewById(R.id.desk_setting_dialog_item_text);
			if (textView != null) {
				textView.setText(entries[custonPostion]);
			}
		}
	}
}
