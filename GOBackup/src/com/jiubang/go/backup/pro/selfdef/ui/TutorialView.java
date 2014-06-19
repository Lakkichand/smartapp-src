package com.jiubang.go.backup.pro.selfdef.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.jiubang.go.backup.pro.util.Util;

/**
 * TutorialView
 * 
 * @author GoBackup Dev Team
 */
public class TutorialView extends FrameLayout {
	private Rect mOriginalBound;
	private Rect mSpotlightViewBound;
	private OnSpotlightClickListener mListener;
	private Paint mPaint;
	private Bitmap mSpotlightBitmap;

	public TutorialView(Context context, int layoutId, int spotlightResId, Rect bound) {
		super(context);
		LayoutInflater.from(context).inflate(layoutId, this, true);
		setBackgroundColor(0x00000000);
		mOriginalBound = bound;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
		mSpotlightBitmap = BitmapFactory.decodeResource(getResources(), spotlightResId);

		// 关闭硬件加速功能
		Util.setHardwareAccelerated(this, Util.LAYER_TYPE_SOFTWARE);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		reLayoutSpotlightBound();
	}

	private void reLayoutSpotlightBound() {
		if (mOriginalBound == null) {
			return;
		}

		int[] point = new int[2];
		getLocationOnScreen(point);

		final int left = mOriginalBound.left - point[0];
		final int top = mOriginalBound.top - point[1];
		final int right = left + mOriginalBound.width();
		final int bottom = top + mOriginalBound.height();
		final int centerX = (left + right) / 2;
		final int centerY = (top + bottom) / 2;
		int radius = mOriginalBound.width() <= mOriginalBound.height()
				? mOriginalBound.width() / 2
				: mOriginalBound.height() / 2;

		if (mSpotlightViewBound == null) {
			mSpotlightViewBound = new Rect();
		}
		mSpotlightViewBound.set(centerX - radius, centerY - radius, centerX + radius, centerY
				+ radius);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		canvas.drawColor(0xcc000000, PorterDuff.Mode.XOR);
		canvas.drawBitmap(mSpotlightBitmap, mSpotlightViewBound.left, mSpotlightViewBound.top, mPaint);
		canvas.restore();
	}

	public void setOnSpotlightClickListener(OnSpotlightClickListener listener) {
		mListener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mSpotlightViewBound != null) {
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			if (mSpotlightViewBound.contains(x, y)) {
				if (mListener != null) {
					mListener.onSpotlightClick(x, y);
					return true;
				}
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * onSpotlightClickListener
	 * 
	 * @author GoBackup Dev Team
	 */
	public interface OnSpotlightClickListener {
		void onSpotlightClick(int x, int y);
	}
}
