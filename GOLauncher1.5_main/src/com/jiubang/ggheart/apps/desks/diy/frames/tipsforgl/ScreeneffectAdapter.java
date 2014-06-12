/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.data.info.EffectSettingInfo;

/**
 * @author ruxueqin
 * 
 */
public class ScreeneffectAdapter extends BaseAdapter {
	private Context mContext;
	/**
	 * 全部桌面效果集
	 */
	private String[] mTotalEffectStrings;
	private String[] mTotalValueStrings;

	/**
	 * 挑选出的显示出来供用户选择的效果集
	 */
	private String[] mShowEffectStrings;
	private String[] mShowValueStrings;

	// 选中与否图片
	private Drawable mUnselectDrawable;
	private Drawable mSelectDrawable;

	private int mSelection = -100;

	/**
	 * 
	 */
	public ScreeneffectAdapter(Context context, int[] initarray) {
		mContext = context;
		mTotalEffectStrings = context.getResources().getStringArray(
				R.array.select_desktop_transition);
		mTotalValueStrings = context.getResources()
				.getStringArray(R.array.desktop_transition_value);

		initShowData(initarray);
	}

	/**
	 * 初始化要显示哪些选项，具体数组下标参考 R.array.select_desktop_transition
	 * R.array.desktop_transition_value
	 */
	private void initShowData(int[] initarray) {
		try {
			int length = initarray.length;
			mShowEffectStrings = new String[length];
			mShowValueStrings = new String[length];
			for (int i = 0; i < length; i++) {
				int value = initarray[i];
				mShowEffectStrings[i] = mTotalEffectStrings[value];
				mShowValueStrings[i] = mTotalValueStrings[value];
			}
			mSelectDrawable = mContext.getResources().getDrawable(R.drawable.guide_select);
			mUnselectDrawable = mContext.getResources().getDrawable(R.drawable.guide_unselect);
		} catch (Exception e) {
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return (null != mShowEffectStrings) ? mShowEffectStrings.length : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		if (null != mShowValueStrings && mShowValueStrings.length > position) {
			return mShowValueStrings[position];
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView = null;
		if (null == convertView) {
			textView = new TextView(mContext);
			textView.setTextColor(android.graphics.Color.WHITE);
			int height = mContext.getResources().getDimensionPixelSize(
					R.dimen.guide_screeneffect_listview_item_height);
			textView.setTextSize(16);
			textView.setHeight(height);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setPadding(20, 0, 20, 0);
			convertView = textView;
		}
		textView = (TextView) convertView;
		textView.setText(mShowEffectStrings[position]);
		if (position != mSelection) {
			textView.setCompoundDrawablesWithIntrinsicBounds(null, null, mUnselectDrawable, null);
		} else {
			textView.setCompoundDrawablesWithIntrinsicBounds(null, null, mSelectDrawable, null);
		}

		return convertView;
	}

	public void updateSelection(int position) {
		mSelection = position;
		notifyDataSetInvalidated();
	}

	public void updateSelection(EffectSettingInfo effectInfo) {
		if (null == effectInfo || null == mShowValueStrings) {
			return;
		}
		int value = effectInfo.mEffectorType;
		int selection = 0;
		for (int i = 0; i < mShowValueStrings.length; i++) {
			if (Integer.valueOf(mShowValueStrings[i]) == value) {
				selection = i;
			}
		}
		updateSelection(selection);
	}

	public int getSelectValue() throws IllegalStateException {
		try {
			int value = Integer.valueOf((String) getItem(mSelection));
			return value;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalStateException("dataerror");
		}
	}
}
