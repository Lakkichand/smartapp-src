package com.jiubang.ggheart.apps.appfunc.component;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.jiubang.core.mars.XComponent;

public class XLine extends XComponent {

	private Paint mPaint;

	public XLine(int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
		mPaint = new Paint();
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		canvas.drawLine(mX, mY, mX, mY, mPaint);

	}

	@Override
	protected boolean animate() {
		return false;
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub

	}

}
