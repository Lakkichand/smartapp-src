/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import com.gau.go.launcherex.R;

/**
 * @author ruxueqin
 * 
 */
public class InfoScreenIndicator extends ScreenIndicator {
	/**
	 * 特定item使用自定义图片
	 */
	public ArrayList<SpecialInfoScreenIndicatorItem> mSpecialIndicatorItems;

	private int mHeight = 0;

	class SpecialInfoScreenIndicatorItem {
		public int mPosition;
		public Drawable mSelectDrawable;
		public Drawable mUnSelectDrawable;

		public SpecialInfoScreenIndicatorItem(int position, int selectID, int unselectID) {
			mPosition = position;
			try {
				mSelectDrawable = getContext().getResources().getDrawable(selectID);
				mUnSelectDrawable = getContext().getResources().getDrawable(unselectID);
			} catch (Exception e) {
				Log.i("InfoScreenIndicator",
						"SpecialInfoScreenIndicatorItem has exception " + e.getMessage());
			}
		}
	}

	/**
	 * @param context
	 */
	public InfoScreenIndicator(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param att
	 */
	public InfoScreenIndicator(Context context, AttributeSet att) {
		super(context, att);

		mHeight = getResources().getDimensionPixelSize(R.dimen.dots_indicator_height);
		setDefaultDotsIndicatorImage(R.drawable.setting_dotindicator_lightbar,
				R.drawable.setting_dotindicator_normalbar);
		setDotsImage(R.drawable.setting_dotindicator_lightbar,
				R.drawable.setting_dotindicator_normalbar);
		setmLayoutMode(ScreenIndicator.LAYOUT_MODE_ADJUST_PICSIZE);
		setDrawMode(ScreenIndicatorItem.DRAW_MODE_INDIVIDUAL);
	}

	/**
	 * @param context
	 * @param att
	 * @param defStyle
	 */
	public InfoScreenIndicator(Context context, AttributeSet att, int defStyle) {
		super(context, att, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 设置特定位置图片
	 * 
	 * @param position
	 * @param selected
	 * @param unselected
	 */
	public void setSpecialDotImage(int position, int selected, int unselected) {
		if (null == mSpecialIndicatorItems) {
			mSpecialIndicatorItems = new ArrayList<SpecialInfoScreenIndicatorItem>();
			SpecialInfoScreenIndicatorItem item = new SpecialInfoScreenIndicatorItem(position,
					selected, unselected);
			mSpecialIndicatorItems.add(item);
		}
	}

	@Override
	public void setTotal(int total) {
		super.setTotal(total);

		updateSpecialItemImage();
	}

	@Override
	public void setCurrent(int current) {
		super.setCurrent(current);

		updateSpecialItemImage();
	}

	/**
	 * 更新特定图片
	 */
	private void updateSpecialItemImage() {
		if (null != mSpecialIndicatorItems) {
			int count = mSpecialIndicatorItems.size();
			for (int i = 0; i < count; i++) {
				SpecialInfoScreenIndicatorItem item = mSpecialIndicatorItems.get(i);
				if (i < mTotal && i >= 0) {
					ScreenIndicatorItem child = (ScreenIndicatorItem) getChildAt(i);
					Drawable drawable = (mCurrent == item.mPosition)
							? item.mSelectDrawable
							: item.mUnSelectDrawable;
					child.setImageDrawable(drawable);
				}
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, 0, right, mHeight);
	}

	public void resetIndicatorPic() {
		// 设置新的图片给指示器
		setDefaultDotsIndicatorImage(R.drawable.theme_detail_indicator_light,
				R.drawable.theme_detail_indicator_normal);
		setDotsImage(R.drawable.theme_detail_indicator_light,
				R.drawable.theme_detail_indicator_normal);
	}
}
