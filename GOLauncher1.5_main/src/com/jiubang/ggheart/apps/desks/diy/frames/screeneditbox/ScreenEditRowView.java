package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;

/**
 * 
 * <br>
 * 类描述:放置于ScreenEditBoxContainerForApps内，用于放置ScreenEditGridViewForApps <br>
 * 功能详细描述:
 * 
 */
public class ScreenEditRowView extends ViewGroup {

	private int mVMargin = 0;
	private int mMaxCount;

	private int mAppHeight; // 放置app的空间高度
	private int mHeightMargin = 0; 
	int mViewheight = 0; // app高度

	public ScreenEditRowView(Context context) {
		super(context);
		mVMargin = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_vertical_pading);
		mAppHeight = (int) (DrawUtils.sHeightPixels
				* ScreenEditLayout.APPSCALE
				- getContext().getResources().getDimension(
						R.dimen.screen_edit_indicator_height) - getContext()
				.getResources().getDimension(
						R.dimen.screen_edit_tabtitle_height));
		// 根据高度算出间隙
		mViewheight = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_height_app);
		if (DrawUtils.sHeightPixels <= 800) { // 保证 800分辨率的手机能显示三排
			mViewheight = DrawUtils.dip2px(76);
		}
		int mRowCount = mAppHeight / mViewheight;
		if (mAppHeight % mViewheight == 0) {
			mRowCount = mRowCount - 1;
		}
		mHeightMargin = (mAppHeight - mViewheight * mRowCount) / (mRowCount + 1);
		// Log.i("jiang","间距："+mHeightMargin);
	}

	public ScreenEditRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mVMargin = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_vertical_pading);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		int verticalSpace = mViewheight + mHeightMargin + mVMargin;
		int count = getChildCount();
		int templeft = 0;
		for (int i = 0; i < count; i++) {
			// 每一行
			View view = getChildAt(i);
			view.measure(mViewheight, b - t);
			int bottom = t + mViewheight;
			view.layout(templeft, t, r, bottom);
			// Log.i("jiang","行的："+templeft+" "+t+" "+r+" "+bottom);
			t = t + verticalSpace;
		}
	}

	public void setMaxCount(int count) {
		mMaxCount = count;
	}

}
