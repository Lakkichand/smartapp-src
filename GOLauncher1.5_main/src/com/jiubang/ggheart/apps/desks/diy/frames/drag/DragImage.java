package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.SystemClock;
import android.view.View;

public class DragImage extends IDragObject implements ISelfObject {
	private View mParentView;

	// 不负责清理工作
	private Bitmap mDragBitmap = null;

	private boolean mUsedColor = false;
	private Paint mColorPaint;
	private Paint mNormalPaint;

	private Matrix mMatrix;

	private float mScaleX = 1;
	private float mScaleY = 1;

	// 显示信息
	private boolean mIsVisable = true;

	public DragImage(View parent, Bitmap bmp) {
		mParentView = parent;
		mDragBitmap = bmp;

		mNormalPaint = new Paint();

		selfConstruct();
	}

	/**
	 * 设置半透明
	 */
	public void setTranslucency() {
		if (null != mNormalPaint) {
			// mNormalPaint.setAlpha(180);
			mNormalPaint.setAlpha(255);
		}
	}

	@Override
	public void selfConstruct() {
		mMatrix = new Matrix();
	}

	@Override
	public void selfDestruct() {
		mParentView = null;
		mDragBitmap = null;
		mColorPaint = null;
		mNormalPaint = null;
		mMatrix = null;
	}

	public void setDragBitmap(Bitmap bmp) {
		mDragBitmap = bmp;
	}

	public Bitmap getDragBitmap() {
		return mDragBitmap;
	}

	@Override
	public void setColor(int color, PorterDuff.Mode mode) {
		if (0 == color) {
			mUsedColor = false;
		} else {
			mUsedColor = true;

			if (null == mColorPaint) {
				mColorPaint = new Paint();
			}
			mColorPaint.setColorFilter(new PorterDuffColorFilter(color, mode));
		}
	}

	@Override
	public int getLeft() {
		return mLeft;
	}

	@Override
	public int getTop() {
		return mTop;
	}

	@Override
	public int getRight() {
		return mLeft + getWidth();
	}

	@Override
	public int getBottom() {
		return mTop + getHeight();
	}

	@Override
	public int getWidth() {
		return (int) (null == mDragBitmap ? 0 : mDragBitmap.getWidth() * mScaleX);
	}

	@Override
	public int getHeight() {
		return (int) (null == mDragBitmap ? 0 : mDragBitmap.getHeight() * mScaleY);
	}

	@Override
	public void layout(int l, int t, int r, int b) {
		final int oldLeft = mLeft;
		final int oldTop = mTop;
		mLeft = l;
		mTop = t;

		if (mIsVisable) {
			l = l > oldLeft ? oldLeft : l;
			t = t > oldTop ? oldTop : t;
			r = r > oldLeft + getWidth() ? r : oldLeft + getWidth();
			b = b > oldTop + getHeight() ? b : oldTop + getHeight();
			mParentView.invalidate(l, t, r, b);
		}
	}

	@Override
	public void scale(float xScale, float yScale) {
		mScaleX = xScale;
		mScaleY = yScale;

		mMatrix.setScale(xScale, yScale);
	}

	@Override
	public void setVisable(boolean bVisable) {
		mIsVisable = bVisable;
	}

	@Override
	public boolean isVisable() {
		return mIsVisable;
	}

	@Override
	public void draw(Canvas canvas) {
		if (null == mDragBitmap || mDragBitmap.isRecycled() || !mIsVisable) {
			return;
		}

		final int saveCount = canvas.getSaveCount();
		canvas.clipRect(mLeft, mTop, mLeft + getWidth(), mTop + getHeight());
		canvas.translate(mLeft, mTop);
		final Paint paint = mUsedColor ? mColorPaint : mNormalPaint;
		paint.setAlpha(mAlpha);
		canvas.drawBitmap(mDragBitmap, mMatrix, paint);
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
