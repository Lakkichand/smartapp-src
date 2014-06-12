package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingFontSingleInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;
import com.jiubang.ggheart.apps.font.FontBean;

/**
 * 
 * <br>类描述:单选和多选对话框的适配器
 * <br>功能详细描述:
 * 
 * @author  chenguanyu
 * @date  [2012-9-12]
 */
public class DeskSettingSingleDialogAdapter extends BaseAdapter {

	private Context mContext;
	private CharSequence[] mTitles;
	private int[] mImageId;
	private Drawable[] mImageDrawable;
	private boolean mIsAddImageBg;
	private int mSelectedPosition = -1;

	private final static int CACHE_SIZE = 20;

	private HashMap<String, FontBean> mFontBeanMap = null;

	private ArrayList<FontBean> mCache = null;

	public DeskSettingSingleDialogAdapter(Context context,
			DeskSettingSingleInfo deskSettingSingleInfo) {
		if (context == null || deskSettingSingleInfo == null) {
			return;
		}

		mContext = context;
		mTitles = deskSettingSingleInfo.getEntries();
		mImageDrawable = deskSettingSingleInfo.getImageDrawable();
		mImageId = deskSettingSingleInfo.getImageId();
		mIsAddImageBg = deskSettingSingleInfo.getIsAddImageBg();
		if (deskSettingSingleInfo instanceof DeskSettingFontSingleInfo) {
			mFontBeanMap = ((DeskSettingFontSingleInfo) deskSettingSingleInfo).getmFontBeanMap();
			mCache = new ArrayList<FontBean>(CACHE_SIZE);
		}
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
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
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

		final ViewGroup parentView = parent;
		final View childView = convertView;
		singleChoiceButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				singleChoiceButton.setChecked(true);
				mSelectedPosition = position;
				final int count = parentView.getChildCount();
				for (int i = 0; i < count; i++) {
					View child = parentView.getChildAt(i);
					if (child != null && child != childView) {
						RadioButton radioButton = (RadioButton) child
								.findViewById(R.id.desk_setting_dialog_item_radiobtn);
						radioButton.setChecked(false);
					}
				}
			}
		});

		//先判断数组是否有资源
		if (mImageId == null || mImageId.length == 0) {
			//判断图片Drawable数组
			if (mImageDrawable == null || mImageDrawable.length == 0) {
				iconImg.setVisibility(View.GONE);
			} else {
				if (position < mImageDrawable.length) {
					iconImg.setImageDrawable(mImageDrawable[position]);
				}
			}
		} else {
			if (position < mImageId.length) {
				//给图片添加背景图
				if (mIsAddImageBg) {
					Drawable drawable = DeskSettingConstants.getGoEffectsIcons(mContext,
							mImageId[position]);
					iconImg.setImageDrawable(drawable);
				} else {
					iconImg.setImageDrawable(mContext.getResources()
							.getDrawable(mImageId[position]));
				}
			}
		}

		textView.setText(mTitles[position]);
		//手动更改字体，不用DeskTextView。因为getView会重复注册。导致无法注销
		DeskSettingConstants.setTextViewTypeFace(textView);

		//如果是字体单选框，则设置textView的字体显示
		if (null != mFontBeanMap && null != mCache) {
			FontBean bean = mFontBeanMap.get(mTitles[position]);
			if (bean != null) {
				boolean flag = bean.initTypeface(mContext);
				if (flag) {
					mCache.add(bean);
				}
				textView.setTypeface(bean.mFontTypeface, bean.mFontStyle);
				if (mCache.size() >= CACHE_SIZE) {
					for (int i = 0; i < mCache.size(); i++) {
						mCache.get(i).mFontTypeface = null;
					}
					mCache.clear();
					// Because too many font typeface may be generated during scrolling the list dialog, suggest to execute real-time
					// GC to avoid the OutofMemoryError occur.
					System.gc();
				}
			}
		}
		return convertView;
	}
}
