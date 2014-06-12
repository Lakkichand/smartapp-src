package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingMultiInfo;

/**
 * 
 * <br>类描述:多选对话框适配器
 * <br>功能详细描述:
 * 
 * @author  chenguanyu
 * @date  [2012-9-12]
 */
public class DeskSettingMultiDialogAdapter extends BaseAdapter {

	private Context mContext;
	private CharSequence[] mTitles;
	private int[] mIcons;
	private boolean[] mSelectedGroup = null;
	private boolean mIsAddImageBg;

	public DeskSettingMultiDialogAdapter(Context context, DeskSettingMultiInfo deskSettingMultiInfo) {
		if (context == null || deskSettingMultiInfo == null) {
			return;
		}
		mContext = context;
		mTitles = deskSettingMultiInfo.getEntries();
		mIcons = deskSettingMultiInfo.getImageId();
		mSelectedGroup = deskSettingMultiInfo.getValueIndex();
		mIsAddImageBg = deskSettingMultiInfo.getIsAddImageBg();
	}

	public boolean[] getSelectedGroup() {
		return mSelectedGroup;
	}

	public void setSelectedGroup(boolean[] selectedGroup) {
		mSelectedGroup = selectedGroup;
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
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.desk_setting_multidialog_item, null);
		}
		ImageView iconImg = (ImageView) convertView
				.findViewById(R.id.desk_setting_dialog_item_icon);
		TextView textView = (TextView) convertView.findViewById(R.id.desk_setting_dialog_item_text);
		final CheckBox checkBox = (CheckBox) convertView
				.findViewById(R.id.desk_setting_dialog_item_checkbox);
		checkBox.setVisibility(View.VISIBLE);

		if (mSelectedGroup[position]) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}

		checkBox.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkBox.isChecked()) {
					mSelectedGroup[position] = true;
				} else {
					mSelectedGroup[position] = false;
				}
			}
		});

		textView.setText(mTitles[position]);
		//手动更改字体，不用DeskTextView。因为getView会重复注册。导致无法注销
		DeskSettingConstants.setTextViewTypeFace(textView);
		
		if (mIcons == null || mIcons.length == 0) {
			iconImg.setVisibility(View.GONE);
		} else {
			//给图片添加背景图
			if (mIsAddImageBg) {
				Drawable drawable = DeskSettingConstants.getGoEffectsIcons(mContext,
						mIcons[position]);
				iconImg.setImageDrawable(drawable);
			} else {
				iconImg.setImageDrawable(mContext.getResources().getDrawable(mIcons[position]));
			}
		}

		return convertView;
	}
}
