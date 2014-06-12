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
 * 类描述:widget的基本展示view <br>
 * 功能详细描述:
 */
public class ScreenEditGridViewForGoWidgets extends ViewGroup {

	private int mWidgetWeight = 0;

	public ScreenEditGridViewForGoWidgets(Context context) {
		super(context);
		mWidgetWeight = (int) context.getResources().getDimension(
				R.dimen.screen_edit_subview_widght);
	}

	public ScreenEditGridViewForGoWidgets(Context context, AttributeSet attrs) {
		super(context, attrs);
		mWidgetWeight = (int) context.getResources().getDimension(
				R.dimen.screen_edit_subview_widght);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		View childView = getChildAt(0);
		if (childView == null) {
			return;
		}
		// 详情页
		if (this.getTag() != null && this.getTag().equals("info")) {
			int left = (DrawUtils.sWidthPixels - DrawUtils.dip2px(266)) / 2;
			int top = 0;
			int right = 0;
			right = left + DrawUtils.dip2px(266);
			childView.measure(DrawUtils.dip2px(266), b - t);
			childView.layout(left, top, right, b);
		} else {
			// 普通预览页
			int left = (DrawUtils.sWidthPixels - mWidgetWeight) / 2;
			int top = 0;
			int right = 0;
			right = left + mWidgetWeight;
			childView.measure(mWidgetWeight, b - t);
			childView.layout(left, top, right, b);
		}
	}

}
