package com.jiubang.ggheart.apps.desks.diy.frames.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import com.go.util.device.Machine;

public class BinaryView extends ViewGroup {
	View mView1;
	int mLeft1;
	int mTop1;
	View mView2;
	int mLeft2;
	int mTop2;
	boolean mReverseDrawOrder;
	private boolean mNeedToCatch = false;

	public BinaryView(Context context) {
		super(context);
		mNeedToCatch = Machine.isHuaweiAndOS2_2_1();
	}

	public void setViews(View view1, View view2) {
		mView1 = view1;
		mLeft1 = 0;
		mTop1 = 0;
		mView2 = view2;
		mLeft2 = 0;
		mTop2 = 0;
	}

	public void setReverseDrawOrderEnabled(boolean enabled) {
		mReverseDrawOrder = enabled;
	}

	public void setFirstView(View view, int left, int top) {
		mView1 = view;
		mLeft1 = left;
		mTop1 = top;
	}

	public void offsetFirstView(int dx, int dy) {
		mLeft1 += dx;
		mTop1 += dy;
	}

	public View getFirstView() {
		return mView1;
	}

	public int getFirstViewLeft() {
		return mLeft1;
	}

	public int getFirstViewTop() {
		return mTop1;
	}

	public void drawFirstView(Canvas canvas, long drawingTime) {
		if (mView1 != null && mView1.getResources() != null
				&& mView1.getResources().getDisplayMetrics() != null) {
			canvas.translate(mLeft1, mTop1);
			toDrawChild(canvas, mView1, drawingTime);
			canvas.translate(-mLeft1, -mTop1);
		}
	}

	public View getSecondView() {
		return mView2;
	}

	public void setSecondView(View view, int left, int top) {
		mView2 = view;
		mLeft2 = left;
		mTop2 = top;
	}

	public void offsetSecondView(int dx, int dy) {
		mLeft2 += dy;
		mTop2 += dy;
	}

	public int getSecondViewLeft() {
		return mLeft2;
	}

	public int getSecondViewTop() {
		return mTop2;
	}

	public void drawSecondView(Canvas canvas, long drawingTime) {
		if (mView2 != null) {
			canvas.translate(mLeft2, mTop2);
			toDrawChild(canvas, mView2, drawingTime);
			canvas.translate(-mLeft2, -mTop2);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		final long drawingTime = getDrawingTime();
		if (mReverseDrawOrder) {
			drawSecondView(canvas, drawingTime);
			drawFirstView(canvas, drawingTime);
		} else {
			drawFirstView(canvas, drawingTime);
			drawSecondView(canvas, drawingTime);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mView1 != null && mView1 instanceof BinaryView) {
			mView1.layout(0, 0, r - l, b - t);
		}
		if (mView2 != null && mView2 instanceof BinaryView) {
			mView2.layout(0, 0, r - l, b - t);
		}
	}

	private void toDrawChild(Canvas canvas, View childView, long drawingTime) {
		if (mNeedToCatch) {
			try {
				drawChild(canvas, childView, drawingTime);
			} catch (NullPointerException e) {
				// TODO: handle exception
			}
		} else {
			drawChild(canvas, childView, drawingTime);
		}
	}

}
