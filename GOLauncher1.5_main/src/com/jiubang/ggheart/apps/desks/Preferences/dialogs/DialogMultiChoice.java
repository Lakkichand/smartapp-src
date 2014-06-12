package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;

/**
 * 
 * <br>类描述:普通多选对话框
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-11-8]
 */
public class DialogMultiChoice extends DialogBase {
	private View mView;
	private ListView mListView;
	private OnMultiChoiceClickListener multiChoiceClickListener;
	private MultiDialogAdapter mAdapter;

	public DialogMultiChoice(Context context) {
		super(context);
	}

	public DialogMultiChoice(Context context, int theme) {
		super(context, theme);
	}

	@Override
	public View getView() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.desk_setting_dialog_for_singleormulti_choice, null);

		mDialogLayout = (LinearLayout) mView.findViewById(R.id.dialog_layout);
		mTitle = (TextView) mView.findViewById(R.id.desk_setting_dialog_singleormulti_title);

		//默认隐藏
		mOkButton = (Button) mView.findViewById(R.id.desk_setting_dialog_singleormulti_ok_btn);
		mCancelButton = (Button) mView
				.findViewById(R.id.desk_setting_dialog_singleormulti_cancel_btn);

		return mView;
	}

	/**
	 * <br>功能简述:listview内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param items	标题内容
	 * @param imageId 图片数组
	 * @param selectedGroup	已勾选的选项
	 */
	public void setItemData(CharSequence[] items, int[] imageId, boolean[] selectedGroup,
			OnMultiChoiceClickListener listener) {
		if (listener == null) {
			return;
		}
		multiChoiceClickListener = listener;
		mAdapter = new MultiDialogAdapter(mContext, items, imageId, null, selectedGroup);
		initListViewData(mAdapter);
	}

	/**
	 * <br>功能简述:listview内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param items	标题内容
	 * @param imageDrawable 图片数组
	 * @param selectedGroup	已勾选的选项
	 */
	public void setItemData(CharSequence[] items, Drawable[] imageDrawable,
			boolean[] selectedGroup, OnMultiChoiceClickListener listener) {
		if (listener == null) {
			return;
		}
		multiChoiceClickListener = listener;
		mAdapter = new MultiDialogAdapter(mContext, items, null, imageDrawable, selectedGroup);
		initListViewData(mAdapter);
	}

	/**
	 * <br>功能简述:listview内容 不带图片
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param items
	 * @param selectedGroup
	 */
	public void setItemData(CharSequence[] items, boolean[] selectedGroup,
			OnMultiChoiceClickListener listener) {
		if (listener == null) {
			return;
		}
		multiChoiceClickListener = listener;
		mAdapter = new MultiDialogAdapter(mContext, items, null, null, selectedGroup);
		initListViewData(mAdapter);
	}

	public void initListViewData(final MultiDialogAdapter adapter) {
		mListView = (ListView) mView.findViewById(R.id.desk_setting_dialog_singleormulti_list);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				boolean[] selectedGroup = adapter.getSelectedGroup();
				CheckBox checkBox = (CheckBox) view
						.findViewById(R.id.desk_setting_dialog_item_checkbox);

				if (checkBox.isChecked()) {
					checkBox.setChecked(false);
					selectedGroup[position] = false;
				} else {
					checkBox.setChecked(true);
					selectedGroup[position] = true;
				}

				setOkButtonEnabled(selectedGroup);
				multiChoiceClickListener.onClick(null, position, checkBox.isChecked());
			}
		});
		setOkButtonEnabled(adapter.getSelectedGroup()); //设置确定按钮是否可点
	}

	/**
	 * <br>功能简述:获取多选选择的值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public ArrayList<Integer> getCheckList() {
		if (mAdapter != null) {
			boolean[] selectedGroup = mAdapter.getSelectedGroup();
			if (selectedGroup != null) {
				ArrayList<Integer> selectIndexs = new ArrayList<Integer>();
				int size = selectedGroup.length;
				for (int i = 0; i < size; i++) {
					if (selectedGroup[i]) {
						selectIndexs.add(i);
					}
				}
			}
		}
		return null;
	}

	/**
	 * <br>功能简述:设置确定按钮是否可点
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param selectedGroup 勾选列表
	 */
	public void setOkButtonEnabled(boolean[] selectedGroup) {
		if (mOkButton == null) {
			return;
		}

		if (selectedGroup != null) {
			int selectedGroupSize = selectedGroup.length;
			for (int i = 0; i < selectedGroupSize; i++) {
				if (selectedGroup[i]) {
					mOkButton.setEnabled(true);
					mOkButton.setTextColor(mContext.getResources().getColor(
							R.color.desk_setting_button_color));
					return;
				}
			}
		}
		mOkButton.setEnabled(false);
		mOkButton.setTextColor(mContext.getResources().getColor(
				R.color.desk_setting_item_summary_color));
	}

	/**
	 * <br>类描述:多选对话框适配器
	 * <br>功能详细描述:
	 * 
	 * @author  licanhui
	 * @date  [2012-11-8]
	 */
	public class MultiDialogAdapter extends BaseAdapter {

		private Context mContext;
		private CharSequence[] mTitles;
		private int[] mImageId;
		private Drawable[] mImageDrawable;
		private boolean[] mSelectedGroup = null;

		public MultiDialogAdapter(Context context, CharSequence[] titles, int[] imageId,
				Drawable[] imageDrawable, boolean[] selectedGroup) {
			if (context == null || selectedGroup == null) {
				return;
			}
			mContext = context;
			mTitles = titles;
			mImageId = imageId;
			mImageDrawable = imageDrawable;
			mSelectedGroup = selectedGroup;
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
			TextView textView = (TextView) convertView
					.findViewById(R.id.desk_setting_dialog_item_text);
			final CheckBox checkBox = (CheckBox) convertView
					.findViewById(R.id.desk_setting_dialog_item_checkbox);

			if (mSelectedGroup[position]) {
				checkBox.setChecked(true);
			} else {
				checkBox.setChecked(false);
			}

			textView.setText(mTitles[position]);
			//手动更改字体，不用DeskTextView。因为getView会重复注册。导致无法注销
			DeskSettingConstants.setTextViewTypeFace(textView);

			//先判断数组是否有资源
			if (mImageId != null && mImageId.length != 0) {
				if (position < mImageId.length) {
					iconImg.setImageDrawable(mContext.getResources()
							.getDrawable(mImageId[position]));
				} else {
					iconImg.setVisibility(View.GONE);
				}
			} else {
				//判断图片Drawable数组
				if (mImageDrawable == null || mImageDrawable.length == 0) {
					iconImg.setVisibility(View.GONE);
				} else {
					if (position < mImageDrawable.length) {
						iconImg.setImageDrawable(mImageDrawable[position]);
					} else {
						iconImg.setVisibility(View.GONE);
					}
				}
			}
			return convertView;
		}
	}
}
