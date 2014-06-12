package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;

/**
 * 
 * <br>类描述:普通单选对话框
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-16]
 */
public class DialogSingleChoice extends DialogBase {
	private View mView;
	private ListView mListView;
	private OnClickListener mOnClickListener;

	public DialogSingleChoice(Context context) {
		super(context);
	}

	public DialogSingleChoice(Context context, int theme) {
		super(context, theme);
	}

	@Override
	public View getView() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.desk_setting_dialog_for_singleormulti_choice, null);
		
		mDialogLayout = (LinearLayout) mView.findViewById(R.id.dialog_layout);
		mTitle = (TextView) mView.findViewById(R.id.desk_setting_dialog_singleormulti_title);

		//默认隐藏
		mOkButton = (Button) mView.findViewById(R.id.desk_setting_dialog_singleormulti_ok_btn);
		mOkButton.setVisibility(View.GONE); //设置默认隐藏
		mCancelButton = (Button) mView.findViewById(R.id.desk_setting_dialog_singleormulti_cancel_btn);

		return mView;
	}

	/**
	 * <br>功能简述:listview内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param items	内容数组
	 * @param imageId	图片资源数组
	 * @param checkItem	checkBox选择的位置 -1:代表不勾选
	 * @param isShowCheckBox 是否显示checkBox
	 */
	public void setItemData(CharSequence[] items, int[] imageId, int checkItem, boolean isShowCheckBox) {
		MyAdapter adapter = new MyAdapter(mContext, items, imageId, null, checkItem, isShowCheckBox);
		mListView = (ListView) mView.findViewById(R.id.desk_setting_dialog_singleormulti_list);
		mListView.setAdapter(adapter);
	}
	
	/**
	 * <br>功能简述:listview内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param items	内容数组
	 * @param imageDrawable	图片Drawable数组
	 * @param checkItem	checkBox选择的位置 -1:代表不勾选
	 * @param isShowCheckBox 是否显示checkBox
	 */
	public void setItemData(CharSequence[] items, Drawable[] imageDrawable, int checkItem, boolean isShowCheckBox) {
		MyAdapter adapter = new MyAdapter(mContext, items, null, imageDrawable, checkItem, isShowCheckBox);
		mListView = (ListView) mView.findViewById(R.id.desk_setting_dialog_singleormulti_list);
		mListView.setAdapter(adapter);
	}
	
	/**
	 * <br>功能简述:listview内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param items	内容数组
	 * @param checkItem	checkBox选择的位置 -1:代表不勾选
	 * @param isShowCheckBox 是否显示checkBox
	 */
	public void setItemData(CharSequence[] items, int checkItem, boolean isShowCheckBox) {
		MyAdapter adapter = new MyAdapter(mContext, items, null, null, checkItem, isShowCheckBox);
		mListView = (ListView) mView.findViewById(R.id.desk_setting_dialog_singleormulti_list);
		mListView.setAdapter(adapter);
	}

	public void setOnItemClickListener(OnClickListener listener) {
		if (listener == null || mListView == null) {
			return;
		}

		mOnClickListener = listener;
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//设置点击的时候单选按钮跳到对应的位置
				RadioButton radioButton = (RadioButton) view.findViewById(R.id.desk_setting_dialog_item_radiobtn);
				radioButton.setChecked(true);

				final int count = parent.getChildCount();
				for (int i = 0; i < count; i++) {
					View child = parent.getChildAt(i);
					if (child != null && child != view) {
						radioButton = (RadioButton) child.findViewById(R.id.desk_setting_dialog_item_radiobtn);
						radioButton.setChecked(false);
					}
				}
				mOnClickListener.onClick(null, position);	//返回监听方法
				dismiss();
			}
		});
	}

	/**
	 * 
	 * <br>类描述:单选和多选对话框的适配器
	 * <br>功能详细描述:
	 * 
	 * @author  licanhui
	 * @date  [2012-9-12]
	 */
	public class MyAdapter extends BaseAdapter {
		private Context mContext;
		private CharSequence[] mTitles;
		private int[] mImageId;
		private Drawable[] mImageDrawable;
		private int mCheckItem = -1;
		private boolean mIsShowCheckBox = false;

		/**
		 * <默认构造函数>
		 */
		public MyAdapter(Context context, CharSequence[] titles, int[] imageId,
				Drawable[] imageDrawable, int checkItem, boolean isShowCheckBox) {
			if (context == null || titles == null) {
				return;
			}
			mContext = context;
			mTitles = titles;
			mImageId = imageId;
			mImageDrawable = imageDrawable;
			mCheckItem = checkItem;
			mIsShowCheckBox = isShowCheckBox;
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
				convertView = inflater.inflate(R.layout.desk_setting_singledialog_item, null);
			}
			ImageView iconImg = (ImageView) convertView.findViewById(R.id.desk_setting_dialog_item_icon);
			TextView textView = (TextView) convertView

			.findViewById(R.id.desk_setting_dialog_item_text);
			final RadioButton singleChoiceButton = (RadioButton) convertView
					.findViewById(R.id.desk_setting_dialog_item_radiobtn);
			
			if (mIsShowCheckBox) {
				singleChoiceButton.setVisibility(View.VISIBLE);
				if (mCheckItem >= 0 && mCheckItem == position) {
					singleChoiceButton.setChecked(true);
				} else {
					singleChoiceButton.setChecked(false);
				}
			} else {
				singleChoiceButton.setVisibility(View.GONE);
			}
			

			if (position < mTitles.length) {
				textView.setText(mTitles[position]);
				//手动更改字体，不用DeskTextView。因为getView会重复注册。导致无法注销
				DeskSettingConstants.setTextViewTypeFace(textView);
			}

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
