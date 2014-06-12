package com.jiubang.ggheart.apps.desks.diy;

import android.graphics.Canvas;

import com.jiubang.ggheart.components.BubbleTextView;

/**
 * 实现点击效果的接口 Interface defining an object that draw the selected icon's
 * background.
 * 
 * @author jiangxuwen
 * 
 */
public interface DrawSelectedListener {

	void setPressedOrFocusedIcon(BubbleTextView icon);

	void invalidateBubbleTextView(BubbleTextView icon);

	void drawSelectedBorder(Canvas canvas);
}