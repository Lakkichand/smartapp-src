package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarItemInfo;

/**
 * 
 * <br>类描述:调节条对话框
 * <br>功能详细描述:
 * 
 * @author  chenguanyu
 * @date  [2012-9-12]
 */
public class DeskSettingSeekbar {
	private SeekBar mSeekBar1 = null;
	private SeekBar mSeekBar2 = null;
	private DeskSettingSeekBarItemInfo mSeekBarInfo1 = null;
	private DeskSettingSeekBarItemInfo mSeekBarInfo2 = null;
	public Context mContext = null;
	public DeskSettingInfo mDeskSettingInfo;
	public OnDialogSelectListener mOnDialogSelectListener;

	public DeskSettingSeekbar(Context context, DeskSettingInfo deskSettingInfo,
			final OnDialogSelectListener onDialogSelectListener) {
		mContext = context;
		mDeskSettingInfo = deskSettingInfo;
		mOnDialogSelectListener = onDialogSelectListener;
	}

	public static DeskSettingSeekbar bulidDeskSettingSeekbar(Context context,
			DeskSettingInfo deskSettingInfo, final OnDialogSelectListener onDialogSelectListener) {
		return new DeskSettingSeekbar(context, deskSettingInfo, onDialogSelectListener);
	}

	//	@Override
	public View getView() {
		DeskSettingSeekBarInfo deskSettingSeekBarInfo = mDeskSettingInfo.getSeekBarInfo();
		if (deskSettingSeekBarInfo == null) {
			return null;
		}
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.desk_setting_dialog_for_seekbar, null);
		ImageView baseLine1 = (ImageView) view.findViewById(R.id.seekbar1_baseline);
		ImageView baseLine2 = (ImageView) view.findViewById(R.id.seekbar2_baseline);
		//		mDialogLayout = (LinearLayout) view.findViewById(R.id.dialog_layout);

		//		TextView titleTextView = (TextView) view
		//				.findViewById(R.id.desk_setting_dialog_seekbar_title);

		//		titleTextView.setText(deskSettingSeekBarInfo.getTitle());
		LinearLayout seekbarLayout2 = (LinearLayout) view.findViewById(R.id.seekbar2_layout);

		final TextView seekBarTitle1 = (TextView) view.findViewById(R.id.seekbar1_title);
		final TextView seekBarTitle2 = (TextView) view.findViewById(R.id.seekbar2_title);

		TextView seekBarMin1 = (TextView) view.findViewById(R.id.seekbar1_min_value);
		TextView seekBarMax1 = (TextView) view.findViewById(R.id.seekbar1_max_value);

		TextView seekBarMin2 = (TextView) view.findViewById(R.id.seekbar2_min_value);
		TextView seekBarMax2 = (TextView) view.findViewById(R.id.seekbar2_max_value);

		mSeekBar1 = (SeekBar) view.findViewById(R.id.desk_setting_dialog_seekbar1);
		DeskSettingConstants.setSeekBarPadding(mSeekBar1, mContext);
		mSeekBar2 = (SeekBar) view.findViewById(R.id.desk_setting_dialog_seekbar2);
		DeskSettingConstants.setSeekBarPadding(mSeekBar2, mContext);

		ArrayList<DeskSettingSeekBarItemInfo> seekBarInfos = deskSettingSeekBarInfo
				.getSeekBarItemInfos();
		if (seekBarInfos != null) {
			int size = seekBarInfos.size();
			if (size != 0) {
				mSeekBarInfo1 = seekBarInfos.get(0);
				mSeekBar1.setMax(mSeekBarInfo1.getMaxValue() - mSeekBarInfo1.getMinValue()); //设置进度条的总长度:最大值-最小值
				mSeekBar1.setProgress(mSeekBarInfo1.getSelectValue() - mSeekBarInfo1.getMinValue());
				seekBarTitle1.setText(mSeekBarInfo1.getTitle() + ":"
						+ mSeekBarInfo1.getSelectValue());
				seekBarMin1.setText(String.valueOf(mSeekBarInfo1.getMinValue()));
				seekBarMax1.setText(String.valueOf(mSeekBarInfo1.getMaxValue()));
				if (size == 1) {
					seekbarLayout2.setVisibility(View.GONE);
					if (mDeskSettingInfo.getType() != DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX) {
						baseLine1.setVisibility(View.GONE);
					} else {
						baseLine1.setVisibility(View.VISIBLE);
					}
					baseLine2.setVisibility(View.GONE);
				} else {
					mSeekBarInfo2 = seekBarInfos.get(1);
					mSeekBar2.setMax(mSeekBarInfo2.getMaxValue() - mSeekBarInfo2.getMinValue());
					mSeekBar2.setProgress(mSeekBarInfo2.getSelectValue()
							- mSeekBarInfo2.getMinValue());
					seekBarTitle2.setText(mSeekBarInfo2.getTitle() + ":"
							+ mSeekBarInfo2.getSelectValue());
					seekBarMin2.setText(String.valueOf(mSeekBarInfo2.getMinValue()));
					seekBarMax2.setText(String.valueOf(mSeekBarInfo2.getMaxValue()));
					if (mDeskSettingInfo.getType() != DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX) {
						baseLine2.setVisibility(View.GONE);
					} else {
						baseLine2.setVisibility(View.VISIBLE);
					}
				}
			}
		}

		mSeekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//设置名称为：选择值+最小值
				seekBarTitle1.setText(mSeekBarInfo1.getTitle() + ":"
						+ String.valueOf(progress + mSeekBarInfo1.getMinValue()));
			}
		});

		mSeekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				seekBarTitle2.setText(mSeekBarInfo2.getTitle() + ":"
						+ String.valueOf(progress + mSeekBarInfo2.getMinValue()));
			}
		});

		//		Button cancelButton = (Button) view
		//				.findViewById(R.id.desk_setting_dialog_seekbar_cancel_btn);
		//		cancelButton.setOnClickListener(new View.OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				dismiss();
		//			}
		//		});

		return view;
	}
	public void handleCustomAction() {
		//设置父类单选框选择得值 
		DeskSettingConstants.setparentInfoSelectValueTemp(mDeskSettingInfo);

		int seekbarSelectValue1 = mSeekBar1.getProgress() + mSeekBarInfo1.getMinValue();
		mSeekBarInfo1.setSelectValue(seekbarSelectValue1);
		int seekbarSelectValue2 = 0;
		if (mSeekBarInfo2 != null) {
			seekbarSelectValue2 = mSeekBar2.getProgress() + mSeekBarInfo2.getMinValue();
			// 只有一条调节条时，mSeekBarInfo2为null
			mSeekBarInfo2.setSelectValue(seekbarSelectValue2);
		}
		String[] selectedValues = new String[] { String.valueOf(seekbarSelectValue1),
				String.valueOf(seekbarSelectValue2) };
		mOnDialogSelectListener.onDialogSelectValue(selectedValues);
	}
}
