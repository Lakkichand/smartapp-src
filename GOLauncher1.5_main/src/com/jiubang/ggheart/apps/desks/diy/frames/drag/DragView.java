package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.os.SystemClock;
import android.view.View;

public class DragView extends IDragObject implements ISelfObject {
	private View mDragView = null;
	private View mParentView;

	public DragView(View parent, View view) {
		mParentView = parent;
		mDragView = view;
	}

	@Override
	public void selfConstruct() {
	}

	@Override
	public void selfDestruct() {
		mParentView = null;
		mDragView = null;
	}

	public View getDragView() {
		return mDragView;
	}

	@Override
	public int getLeft() {
		return null == mDragView ? 0 : mDragView.getLeft();
	}

	@Override
	public int getTop() {
		return null == mDragView ? 0 : mDragView.getTop();
	}

	@Override
	public int getRight() {
		return null == mDragView ? 0 : mDragView.getRight();
	}

	@Override
	public int getBottom() {
		return null == mDragView ? 0 : mDragView.getBottom();
	}

	@Override
	public int getWidth() {
		return null == mDragView ? 0 : mDragView.getWidth();
	}

	@Override
	public int getHeight() {
		return null == mDragView ? 0 : mDragView.getHeight();
	}

	public void setStartLocation(int locX, int locY) {
		mLeft = locX;
		mTop = locY;
	}

	@Override
	public void layout(int l, int t, int r, int b) {
		if (null != mDragView) {
			mDragView.layout(l, t, r, b);
			final int oldLeft = mLeft;
			final int oldTop = mTop;
			mLeft = l;
			mTop = t;

			l = l > oldLeft ? oldLeft : l;
			t = t > oldTop ? oldTop : t;
			r = r > oldLeft + getWidth() ? r : oldLeft + getWidth();
			b = b > oldTop + getHeight() ? b : oldTop + getHeight();
			mParentView.invalidate(l, t, r, b);
		}
	}

	@Override
	public void scale(float xScale, float yScale) {

	}

	@Override
	public void setColor(int color, Mode mode) {

	}

	@Override
	public void setVisable(boolean bVisable) {
		if (null != mDragView) {
			int i = bVisable ? View.VISIBLE : View.INVISIBLE;
			mDragView.setVisibility(i);
		}
	}

	@Override
	public boolean isVisable() {
		if (null != mDragView) {
			int i = mDragView.getVisibility();
			return i == View.VISIBLE;
		}
		return false;
	}

	@Override
	public void draw(Canvas canvas) {
		if (null != mDragView) {
			int saveCount = canvas.getSaveCount();
			canvas.translate(mLeft, mTop);
			mDragView.draw(canvas);
			canvas.restoreToCount(saveCount);
			if (mDrawState == STATE_AUTO_FLY) {
				// 不可以定义为long型
				int currentTime = 0;
				if (mStartTime == 0) {
					mStartTime = SystemClock.uptimeMillis();
				} else {
					currentTime = (int) (SystemClock.uptimeMillis() - mStartTime);
				}
				drawAutoFly(canvas, currentTime);
			}
		}
	}

}
