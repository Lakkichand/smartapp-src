package com.jiubang.ggheart.apps.desks.Preferences.dialogs;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;

/**
 * 
 * <br>类描述:手势对话框适配器
 * <br>功能详细描述:
 * 
 * @author  chenguanyu
 * @date  [2012-9-14]
 */
public class DeskSettingGestureDialogAdapter extends BaseAdapter {

	private Context mContext;
	private CharSequence[] mTitles;
	private int[] mIcons;
	private int mSelectedPosition = -1;

	public DeskSettingGestureDialogAdapter(Context context, CharSequence[] titles, int[] icons) {
		if (context == null || titles == null) {
			return;
		}
		mContext = context;
		mTitles = titles;
		mIcons = icons;
	}

	public int getSelectedPosition() {
		return mSelectedPosition;
	}

	public void setSelectedPosition(int selectedPosition) {
		mSelectedPosition = selectedPosition;
	}

	@Override
	public int getCount() {
		int count = 0;
		if (mTitles != null) {
			count = mTitles.length;
		}
		return count;
	}

	@Override
	public Object getItem(int position) {

		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.desk_setting_singledialog_item, null);
		}
		ImageView iconImg = (ImageView) convertView
				.findViewById(R.id.desk_setting_dialog_item_icon);
		TextView textView = (TextView) convertView.findViewById(R.id.desk_setting_dialog_item_text);
		final RadioButton singleChoiceButton = (RadioButton) convertView
				.findViewById(R.id.desk_setting_dialog_item_radiobtn);
		if (mSelectedPosition == position) {
			singleChoiceButton.setChecked(true);
		} else {
			singleChoiceButton.setChecked(false);
		}
		singleChoiceButton.setVisibility(View.VISIBLE);
		singleChoiceButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO:
			}
		});
		singleChoiceButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO:
			}
		});

		textView.setText(mTitles[position]);
		//手动更改字体，不用DeskTextView。因为getView会重复注册。导致无法注销
		DeskSettingConstants.setTextViewTypeFace(textView);
		if (mIcons == null || mIcons.length == 0) {
			iconImg.setVisibility(View.GONE);
		} else {
			iconImg.setImageDrawable(mContext.getResources().getDrawable(mIcons[position]));
		}

		return convertView;
	}
}
