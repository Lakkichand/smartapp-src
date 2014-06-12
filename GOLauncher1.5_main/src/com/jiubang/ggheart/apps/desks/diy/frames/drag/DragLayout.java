package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class DragLayout extends RelativeLayout implements ISelfObject {
	private DragImage mDragImage;
	private DragView mDragView;

	public DragLayout(Context context) {
		super(context);
	}

	public DragLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		mDragImage = null;
	}

	public void setDragImage(DragImage image) {
		mDragImage = image;
	}

	public DragImage getDragImage() {
		return mDragImage;
	}

	public void setDragView(DragView dragView) {
		mDragView = dragView;
	}

	public DragView getDragView() {
		return mDragView;
	}

	@Override
	public void removeView(View view) {
		super.removeView(view);
	}

	@Override
	public void removeViewAt(int index) {
		super.removeViewAt(index);
	}

	@Override
	public void removeAllViews() {
		super.removeAllViews();
	}

	@Override
	public void removeViewInLayout(View view) {
		super.removeViewInLayout(view);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (null != mDragImage) {
			mDragImage.draw(canvas);
		}

		if (null != mDragView) {
			mDragView.draw(canvas);
		}
	}
}
