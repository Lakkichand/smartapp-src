package com.jiubang.ggheart.apps.appfunc.common.component;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.XComponent;

/**
 * 进度条基类
 * @author yangguanxiang
 *
 */
public class CommonProgressBar extends XComponent {

	/**
	 * 水平样式
	 */
	public final static int ORIENTATION_HORIZONTAL = 0;
	/**
	 * 垂直样式
	 */
	public final static int ORIENTATION_VERTICAL = 1;
	protected long mMaxProgress;
	protected long mProgress;
	protected Rect mMaxDrawableRect;
	protected Rect mPrgDrawableRect;
	protected Drawable mMaxDrawable;
	protected Drawable mPrgDrawable;
	protected int mOrientation;
	protected Paint mPaint;
	protected Activity mActivity;
	/**
	 * 垂直样式的进度高度
	 */
	protected int mProgressHeightV;

	public CommonProgressBar(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mMaxDrawableRect = new Rect();
		mPrgDrawableRect = new Rect();
	}

	public CommonProgressBar(Activity activity, int tickCount, int x, int y, int width, int height,
			int orientation, int maxProgress, int progress) {
		this(activity, tickCount, x, y, width, height);
		mOrientation = orientation;
		mMaxProgress = maxProgress;
		mProgress = progress;
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		setPosition(left, top, right, bottom);
		mMaxDrawableRect.left = mPaddingLeft;
		mMaxDrawableRect.top = mPaddingTop;
		mMaxDrawableRect.right = mWidth - mPaddingRight;
		mMaxDrawableRect.bottom = mHeight - mPaddingBottom;
		if (mOrientation == ORIENTATION_HORIZONTAL) {
			int prgRight = mMaxDrawableRect.left
					+ (int) (1.0f * (mMaxDrawableRect.right - mMaxDrawableRect.left) * mProgress / mMaxProgress);
			mPrgDrawableRect.left = mMaxDrawableRect.left;
			mPrgDrawableRect.top = mMaxDrawableRect.top;
			mPrgDrawableRect.right = prgRight;
			mPrgDrawableRect.bottom = mMaxDrawableRect.bottom;
		} else {
			// int prgTop = mMaxDrawableRect.top
			// + (int) (1.0f * (mMaxDrawableRect.bottom - mMaxDrawableRect.top)
			// * (mMaxProgress - mProgress) / mMaxProgress);
			// mPrgDrawableRect.left = mMaxDrawableRect.left;
			// mPrgDrawableRect.top = prgTop;
			// mPrgDrawableRect.right = mMaxDrawableRect.right;
			// mPrgDrawableRect.bottom = mMaxDrawableRect.bottom;

			mProgressHeightV = (int) (1.0f * (mMaxDrawableRect.bottom - mMaxDrawableRect.top)
					* mProgress / mMaxProgress);
			mPrgDrawableRect.left = (int) (mMaxDrawableRect.centerX() - mProgressHeightV / 2.0f);
			mPrgDrawableRect.top = (int) (mMaxDrawableRect.bottom - mProgressHeightV / 2.0f - mMaxDrawableRect
					.width() / 2.0f);
			mPrgDrawableRect.right = mPrgDrawableRect.left + mProgressHeightV;
			mPrgDrawableRect.bottom = mPrgDrawableRect.top + mMaxDrawableRect.width();
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mMaxDrawable != null) {
			ImageUtil.drawImage(canvas, mMaxDrawable, ImageUtil.STRETCHMODE, mMaxDrawableRect.left,
					mMaxDrawableRect.top, mMaxDrawableRect.right, mMaxDrawableRect.bottom, mPaint);
		}
		if (mPrgDrawable != null) {
			if (mOrientation == ORIENTATION_HORIZONTAL) {
				ImageUtil.drawImage(canvas, mPrgDrawable, ImageUtil.STRETCHMODE,
						mPrgDrawableRect.left, mPrgDrawableRect.top, mPrgDrawableRect.right,
						mPrgDrawableRect.bottom, mPaint);
			} else {
				canvas.save();
				int centerX = mPrgDrawableRect.centerX();
				int centerY = mPrgDrawableRect.centerY();
				canvas.rotate(-90, centerX, centerY);
				ImageUtil.drawImage(canvas, mPrgDrawable, ImageUtil.STRETCHMODE,
						mPrgDrawableRect.left, mPrgDrawableRect.top, mPrgDrawableRect.right,
						mPrgDrawableRect.bottom, mPaint);
				canvas.restore();
			}
		}
	}

	@Override
	protected boolean animate() {
		return false;
	}

	public void setProgress(long progress) {
		mProgress = progress;
		requestLayout();
	}

	public long getProgress() {
		return mProgress;
	}

	public void setMaxProgress(long max) {
		mMaxProgress = max;
		requestLayout();
	}

	public long getMaxProgress() {
		return mMaxProgress;
	}

	public void setOrientation(int orientation) {
		mOrientation = orientation;
	}

	public int getOrientation() {
		return mOrientation;
	}
}
