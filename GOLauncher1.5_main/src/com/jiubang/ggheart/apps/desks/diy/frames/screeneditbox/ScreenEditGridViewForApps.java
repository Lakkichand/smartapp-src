package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
/**
 * 
 * <br>类描述:程序的基本展示view
 * <br>功能详细描述:
 * 
 */
public class ScreenEditGridViewForApps extends ViewGroup {

	private int mVMargin = 0;
	private int mMaxCount; // 最多有几个
	private boolean mFlag = true;
	int mHorizontalSpace;

	public ScreenEditGridViewForApps(Context context) {
		super(context);
		mVMargin = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_vertical_pading);
		mHorizontalSpace = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_horizontal_space);
	}

	public ScreenEditGridViewForApps(Context context, AttributeSet attrs) {
		super(context, attrs);
		mVMargin = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_vertical_pading);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int viewWidth = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_width);
		int count = getChildCount();
		// 根据最大item数确定item之间的间距，而不是根据前屏的item 数
		int temp = (GoLauncher.getDisplayWidth() - mHorizontalSpace) / (viewWidth + mHorizontalSpace);
		int rightSpace = GoLauncher.getDisplayWidth() - mHorizontalSpace - temp
				* (viewWidth + mHorizontalSpace);
		if (rightSpace >= viewWidth) {
			++temp;
		}
		if (mMaxCount == temp) {
			if (mFlag) {
				mHorizontalSpace = (r - l - (temp * viewWidth)) / (temp + 1);
				mFlag = false;
			}
		}
		int left = mHorizontalSpace;
		for (int i = 0; i < count; i++) {
			// 每一项
			View view = getChildAt(i);
			view.measure(viewWidth, b - t);
			int top = 0;
			int right = left + viewWidth;
			view.layout(left, top + mVMargin, right, b - t);
			// Log.i("jiang","----------------行"+i+":"+left+" "+(top +
			// mVMargin)+" "+right+" "+(b-t));
			left = right + mHorizontalSpace;
		}
	}

	public void setMaxCount(int count) {
		mMaxCount = count;
	}

}
