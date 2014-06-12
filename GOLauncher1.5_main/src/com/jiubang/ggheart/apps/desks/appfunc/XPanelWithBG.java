package com.jiubang.ggheart.apps.desks.appfunc;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.jiubang.core.mars.XPanel;

/**
 * 可以绘制背景图片的xpanel
 * 
 * @author huangshaotao
 * 
 */
public class XPanelWithBG extends XPanel {

	private Drawable mBg = null;
	private Rect mBgRect = new Rect();

	public XPanelWithBG(int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
	}

	public void setBG(Drawable bg) {
		mBg = bg;
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		super.layout(left, top, right, bottom);
		if (mBg != null) {
			mBgRect.set(0, 0, mWidth, mHeight);
			mBg.setBounds(mBgRect);
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mBg != null) {
			mBg.draw(canvas);
		}
		super.drawCurrentFrame(canvas);
	}
}
