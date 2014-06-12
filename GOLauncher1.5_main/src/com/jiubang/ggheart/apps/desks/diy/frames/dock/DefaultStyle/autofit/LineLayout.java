/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit;

import android.content.Context;
import android.view.View;

import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsLineLayout;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;

/**
 * @author ruxueqin
 * 
 */
public class LineLayout extends AbsLineLayout {
	public LineLayout(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (AbsDockView.sPortrait) {
			layoutPort(changed, l, t, r, b);
		} else {
			layoutLand(changed, l, t, r, b);
		}
	}

	private void layoutPort(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		if (count == 0) {
			return;
		}
		int oneView_W = (r - l) / count;
		int bitmap_size = DockUtil.getIconSize(count);
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			int left = (oneView_W - bitmap_size) / 2 + oneView_W * i;
			int top = 0;
			int right = left + bitmap_size;
			view.layout(left, top, right, b);
		}
	}

	private void layoutLand(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		if (count == 0) {
			return;
		}
		int oneView_H = (b - t) / count;
		int bitmap_size = DockUtil.getIconSize(count);
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			int left = 0;
			int top = oneView_H * (count - i - 1) + (oneView_H - bitmap_size) / 2; // 控制点击范围
			int right = r;
			int bottom = top + bitmap_size;
			view.layout(left, top, right, bottom);
		}
	}
}
