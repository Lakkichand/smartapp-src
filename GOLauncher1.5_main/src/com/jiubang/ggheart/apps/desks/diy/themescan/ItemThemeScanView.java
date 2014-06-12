package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.ICleanable;

/**
 * 主题列表项
 * 
 * @author yangbing
 * 
 */
public class ItemThemeScanView extends ViewGroup implements ICleanable {
	private int mLayoutWidth = 0;
	private int mLayoutHeight = 0;

	private ArrayList<ItemThemeView> mItemThemeViews = new ArrayList<ItemThemeView>();

	public ItemThemeScanView(Context context, boolean setBg) {
		super(context);
		if (setBg) {
			setBackgroundResource(R.color.theme_bg);
		}

	}

	public ArrayList<ItemThemeView> getmItemThemeViews() {
		return mItemThemeViews;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		mLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
		mLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int itemCount = SpaceCalculator.getThemeListItemCount();
		if (!SpaceCalculator.sPortrait) { //横屏布局不变
			int onethemewidth = mLayoutWidth / itemCount;
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				int page = i / itemCount;
				int col = i % itemCount;
				int left = page * mLayoutWidth + col * onethemewidth;
				int top = page * mLayoutHeight;
				int right = left + onethemewidth;
				int bottom = top + mLayoutHeight;
				View childView = getChildAt(i);
				childView.measure(onethemewidth, mLayoutHeight);
				childView.layout(left, top, right, bottom);
			}
		} else { //竖屏改变布局 by liulixia
			int count = getChildCount();
			int onethemewidth = SpaceCalculator.getInstance(getContext()).getImageWidth();
			for (int i = 0; i < count; i++) {
				int page = i / itemCount;
				int col = i % itemCount;
				int left = page * mLayoutWidth + SpaceCalculator.getEdgePadding() +
						col * (onethemewidth + SpaceCalculator.getEachotherPadding());
				int right = left + onethemewidth;
//				int left = page * mLayoutWidth + col * onethemewidth;
				int top = page * mLayoutHeight;
//				int right = left + onethemewidth;
				int bottom = top + mLayoutHeight;
				View childView = getChildAt(i);
				childView.measure(onethemewidth, mLayoutHeight);
				childView.layout(left, top, right, bottom);
			}
		}
	}

	@Override
	public void cleanup() {
		removeAllViews();
	}

	public void cleanupItemThemeView() {
		for (ItemThemeView itemView : mItemThemeViews) {
			itemView.cleanup();
		}

	}

	/**
	 * 添加view
	 * */
	public void addItemView(ItemThemeView itemView) {
		mItemThemeViews.add(itemView);
		addView(itemView);
	}

}
