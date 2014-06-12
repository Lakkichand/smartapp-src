package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
/**
 * 
 * <br>类描述:tab的基本展示view
 * <br>功能详细描述:
 */
public class ScreenEditGridView extends ViewGroup {

	private int mVMargin = 0;
	private int mMaxCount;

	public ScreenEditGridView(Context context) {
		super(context);
		mVMargin = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_vertical_pading);
	}

	public ScreenEditGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mVMargin = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_vertical_pading);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int horizontalSpace = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_horizontal_space);
		int viewWidth = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_width);
		int count = getChildCount();
		// 根据最大item数确定item之间的间距，而不是根据前屏的item 数
		int temp = (GoLauncher.getDisplayWidth() - horizontalSpace) / (viewWidth + horizontalSpace);
		int rightSpace = GoLauncher.getDisplayWidth() - horizontalSpace - temp
				* (viewWidth + horizontalSpace);
		if (rightSpace >= viewWidth) {
			++temp;
		}
		if (mMaxCount == temp) {
			horizontalSpace = (r - l - (temp * viewWidth)) / (temp + 1);
		}
		int left = horizontalSpace;
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			view.measure(viewWidth, b - t);
			int top = 0;
			int right = left + viewWidth;
			view.layout(left, top + mVMargin, right, b - t);
			left = right + horizontalSpace;
		}
	}

	public void setMaxCount(int count) {
		mMaxCount = count;
	}

}
