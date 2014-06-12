package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;

/**
 * 
 * <br>类描述:单选对话框
 * <br>功能详细描述:
 * 
 * @author  chenguanyu
 * @date  [2012-9-12]
 */
public class DeskSettingSingleChoiceDialog extends DeskSettingBaseDialog {
	public int mCurPositon = -1;
	public View mView;
	public ListView mListView;
	private View mSeekBarView;
	private DeskSettingSeekbar mSeekbar;
	public DeskSettingSingleChoiceDialog(Context context, DeskSettingInfo deskSettingInfo,
			OnDialogSelectListener onDialogSelectListener) {
		super(context, deskSettingInfo, onDialogSelectListener);
	}

	@Override
	public View getView() {
		final DeskSettingSingleInfo deskSettingSingleInfo = mDeskSettingInfo.getSingleInfo();
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.desk_setting_dialog_for_singleormulti_choice, null);

		mDialogLayout = (LinearLayout) mView.findViewById(R.id.dialog_layout);
		
		final CharSequence[] entryValues = deskSettingSingleInfo.getEntryValues();
		String dialogTittle = deskSettingSingleInfo.getTitle();
		mCurPositon = deskSettingSingleInfo.getValueIndex();

		TextView dialogTitleTextView = (TextView) mView
				.findViewById(R.id.desk_setting_dialog_singleormulti_title);
		dialogTitleTextView.setText(dialogTittle);
		mListView = (ListView) mView.findViewById(R.id.desk_setting_dialog_singleormulti_list);

		final DeskSettingSingleDialogAdapter adapter = new DeskSettingSingleDialogAdapter(mContext,
				deskSettingSingleInfo);
		adapter.setSelectedPosition(mCurPositon);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mCurPositon = position;

				String selectValue = entryValues[mCurPositon].toString();
				deskSettingSingleInfo.setSelectValueTemp(selectValue); //设置临时选择得值

				adapter.setSelectedPosition(position);
				RadioButton radioButton = (RadioButton) view
						.findViewById(R.id.desk_setting_dialog_item_radiobtn);
				radioButton.setChecked(true);

				final int count = adapter.getCount();
				for (int i = 0; i < count - 1; i++) {
					View child = parent.getChildAt(i);
					if (child != null && child != view) {
						radioButton = (RadioButton) child
								.findViewById(R.id.desk_setting_dialog_item_radiobtn);
						if (radioButton != null) {
							radioButton.setChecked(false);
						}
					}
				}
				// 自定义
				int customPosition = mDeskSettingInfo.getCustomPosition();
				if (customPosition == position && customPosition != -1) {
					DeskSettingInfo secondInfo = mDeskSettingInfo.getSecondInfo();
					if (secondInfo != null && mDeskSettingInfo.getCustomPosition() == position) {

						DeskSettingBaseDialog dialog = null;
						if (secondInfo.getMultiInInfo() != null) {
							dialog = DialogFactory.produceDialog(mContext, secondInfo,
									mOnDialogSelectListener);
							if (dialog != null) {
								dialog.show();
							}
							closeDialog();
						} else if (secondInfo.getSeekBarInfo() != null) {
							if (mSeekBarView == null || !mSeekBarView.isShown()) {
								initSeekBar(secondInfo, adapter);
							}
						}
					}
				} else {
					String curSelectValue = entryValues[position].toString();
					deskSettingSingleInfo.setSelectValue(curSelectValue);
					mOnDialogSelectListener.onDialogSelectValue(curSelectValue);
					dismiss();
				}

			}

			
		});
		
		//默认隐藏
		mOkButton = (Button) mView.findViewById(R.id.desk_setting_dialog_singleormulti_ok_btn);

		mOkButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String selectValue = entryValues[adapter.getSelectedPosition()].toString();
				deskSettingSingleInfo.setSelectValue(selectValue);
				mOnDialogSelectListener.onDialogSelectValue(selectValue);
				dismiss();
			}
		});
		mOkButton.setVisibility(View.GONE); //设置默认隐藏

		mCancelButton = (Button) mView
				.findViewById(R.id.desk_setting_dialog_singleormulti_cancel_btn);
		mCancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		// 自定义
		int customPosition = mDeskSettingInfo.getCustomPosition();
		if (customPosition == mCurPositon && customPosition != -1) {
			DeskSettingInfo secondInfo = mDeskSettingInfo.getSecondInfo();
			if (secondInfo.getSeekBarInfo() != null) {
				if (mSeekBarView == null || !mSeekBarView.isShown()) {
					initSeekBar(secondInfo, adapter);
				}
			}
		}
		return mView;
	}

	/**
	 * 由于不带CheckBox的需要关闭对话框
	 */
	public void closeDialog() {
		dismiss();
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
	private void initSeekBar(DeskSettingInfo secondInfo, BaseAdapter adapter) {
		mSeekbar = DeskSettingSeekbar.bulidDeskSettingSeekbar(mContext, secondInfo,
				mOnDialogSelectListener);
		final DeskSettingSingleInfo deskSettingInfo = mDeskSettingInfo.getSingleInfo();
		final CharSequence[] entryValues = deskSettingInfo.getEntryValues();
		String selectValue = entryValues[mCurPositon].toString();
		deskSettingInfo.setSelectValueTemp(selectValue); //设置临时选择得值

		mDeskSettingInfo.getSingleInfo();
		mSeekBarView = mSeekbar.getView();
		mListView.addFooterView(mSeekBarView, null, false);
		mListView.setAdapter(adapter);
		mOkButton.setVisibility(View.VISIBLE);
		mOkButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSeekbar != null && mSeekBarView != null && mSeekBarView.isShown()) {
					mSeekbar.handleCustomAction();
					dismiss();
					return;
				}
			}
		});
	}
}
