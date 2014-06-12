package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingGestureInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;

/**
 * 
 * <br>类描述:手势对话框 
 * <br>功能详细描述:
 * 
 * @author  chenguanyu
 * @date  [2012-9-14]
 */
public class DeskSettingGestureDialog extends DeskSettingBaseDialog {
	private int mCurPositon = -1;

	public DeskSettingGestureDialog(Context context, DeskSettingInfo deskSettingInfo,
			OnDialogSelectListener onDialogSelectListener) {
		super(context, deskSettingInfo, onDialogSelectListener);
	}
	@Override
	public View getView() {
		final DeskSettingGestureInfo deskSettingGestureInfo = mDeskSettingInfo.getmGestureInfo();
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.desk_setting_dialog_for_singleormulti_choice, null);
		mDialogLayout = (LinearLayout) view.findViewById(R.id.dialog_layout);
		CharSequence[] titles = deskSettingGestureInfo.getEntries();
		int[] icons = deskSettingGestureInfo.getImageId();
		String dialogTittle = deskSettingGestureInfo.getTitle();
		int selectValue = Integer.parseInt(String.valueOf(deskSettingGestureInfo.getSelectValue()));
		mCurPositon = selectValue;

		TextView dialogTitleTextView = (TextView) view
				.findViewById(R.id.desk_setting_dialog_singleormulti_title);
		dialogTitleTextView.setText(dialogTittle);
		ListView listView = (ListView) view
				.findViewById(R.id.desk_setting_dialog_singleormulti_list);
		// TODO:传入手势info
		final DeskSettingGestureDialogAdapter adapter = new DeskSettingGestureDialogAdapter(
				mContext, titles, icons);
		adapter.setSelectedPosition(mCurPositon);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//TODO:每一项响应
				mCurPositon = position;
				RadioButton radioButton = (RadioButton) view
						.findViewById(R.id.desk_setting_dialog_item_radiobtn);
				radioButton.setChecked(true);

				final int count = parent.getChildCount();
				for (int i = 0; i < count; i++) {
					View child = parent.getChildAt(i);
					if (child != null && child != view) {
						radioButton = (RadioButton) child
								.findViewById(R.id.desk_setting_dialog_item_radiobtn);
						radioButton.setChecked(false);
					}
				}
				dismiss();
				mOnDialogSelectListener.onDialogSelectValue(mCurPositon);
			}
		});

		Button okButton = (Button) view.findViewById(R.id.desk_setting_dialog_singleormulti_ok_btn);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		Button cancelButton = (Button) view
				.findViewById(R.id.desk_setting_dialog_singleormulti_cancel_btn);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return view;
	}
}
