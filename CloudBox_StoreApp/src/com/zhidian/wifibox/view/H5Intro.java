package com.zhidian.wifibox.view;

import com.zhidian.wifibox.util.DrawUtil;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * h5引导页
 * 
 * @author xiedezhi
 * 
 */
public class H5Intro extends LinearLayout {

	public H5Intro(Context context, int width, int height, int clipLeft,
			int clipTop, int clipRight, int clipBottom) {
		super(context);
		this.setOrientation(VERTICAL);
		View content = new View(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, height);
		this.addView(content, lp);
		View empty = new View(context);
		empty.setBackgroundColor(0xaf000000);
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		this.addView(empty, lp);

		content.setBackgroundDrawable(new BitmapDrawable(DrawUtil
				.createH5IntroBitmap(width, height, clipLeft, clipTop,
						clipRight, clipBottom)));
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (this.getParent() != null && this.getParent() instanceof ViewGroup) {
			ViewGroup parent = (ViewGroup) this.getParent();
			parent.removeView(this);
		}
		return true;
	}

}
