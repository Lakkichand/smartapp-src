package com.jiubang.ggheart.apps.appfunc.common.component;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.go.util.graphics.ImageUtil;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;

/**
 * 
 * @author YeJijiong
 * @version 创建时间：2012-8-29 下午3:38:40
 * 可触摸进度条公用组件
 */
public class CommonTouchProgressBar extends CommonProgressBar {
	/**
	 * 播放进度条滑块图片
	 */
	protected Drawable mProcessCurrentImg;
	/**
	 * 播放进度条滑块背后矩形
	 */
	protected Rect mProcessCurrentImgRect;
	/**
	 * 播放进度条滑块的宽度
	 */
	protected int mProcessCurrentImgWidth;
	protected AppFuncUtils mUtils;
	/**
	 * 进度条是否被触摸
	 */
	private boolean mIsProcessActionDown;
	/**
	 * 是否一直显示进度条滑块
	 */
	private boolean mIsAlwayShowCurrentImg = false;

	/**
	 * 
	 * @param isAlwayShowCurrentImg 是否一直显示进度条滑块
	 */
	public CommonTouchProgressBar(Activity activity, int tickCount, int x, int y, int width,
			int height, boolean isAlwayShowCurrentImg) {
		super(activity, tickCount, x, y, width, height);
		mUtils = AppFuncUtils.getInstance(mActivity);
		mProcessCurrentImgRect = new Rect();
		mIsAlwayShowCurrentImg = isAlwayShowCurrentImg;
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		super.layout(left, top, right, bottom);
		if (mIsAlwayShowCurrentImg && !mIsProcessActionDown) {
			mProcessCurrentImgRect.set(mPrgDrawableRect.right - mProcessCurrentImgWidth / 2,
					mPrgDrawableRect.top + mProcessCurrentImgWidth / 2 + mUtils.getStandardSize(1),
					mPrgDrawableRect.right + mProcessCurrentImgWidth / 2, mPrgDrawableRect.top
							- mProcessCurrentImgWidth / 2 + mUtils.getStandardSize(1));
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		super.drawCurrentFrame(canvas);
		if (mProcessCurrentImg != null && (mIsProcessActionDown || mIsAlwayShowCurrentImg)) {
			ImageUtil.drawImage(canvas, mProcessCurrentImg, ImageUtil.STRETCHMODE,
					mProcessCurrentImgRect.left, mProcessCurrentImgRect.top,
					mProcessCurrentImgRect.right, mProcessCurrentImgRect.bottom, mPaint);
		}
	}
	@Override
	public boolean onTouch(MotionEvent event) {
		final int action = event.getAction();
		int x = (int) event.getX() - getAbsX();
		int y = (int) event.getY() - getAbsY();
		if (isInProcessArea(x, y) && action == MotionEvent.ACTION_DOWN) {
			mIsProcessActionDown = true;
			mProcessCurrentImgRect.set(x - mProcessCurrentImgWidth / 2, mMaxDrawableRect.top
					+ mProcessCurrentImgWidth / 2 + mUtils.getStandardSize(1), x
					+ mProcessCurrentImgWidth / 2, mMaxDrawableRect.top - mProcessCurrentImgWidth
					/ 2 + mUtils.getStandardSize(1));
			mPrgDrawableRect.set(mMaxDrawableRect.left, mMaxDrawableRect.top, x,
					mMaxDrawableRect.bottom);
			return true;
		}
		if (action == MotionEvent.ACTION_MOVE && mIsProcessActionDown && x > mMaxDrawableRect.left
				&& x < mMaxDrawableRect.right) {
			mProcessCurrentImgRect.set(x - mProcessCurrentImgWidth / 2, mMaxDrawableRect.top
					+ mProcessCurrentImgWidth / 2 + mUtils.getStandardSize(1), x
					+ mProcessCurrentImgWidth / 2, mMaxDrawableRect.top - mProcessCurrentImgWidth
					/ 2 + mUtils.getStandardSize(1));
			mPrgDrawableRect.set(mMaxDrawableRect.left, mMaxDrawableRect.top, x,
					mMaxDrawableRect.bottom);
			return true;
		}
		if (action == MotionEvent.ACTION_UP && mIsProcessActionDown) {
			setProgress((int) ((1.0f * (mPrgDrawableRect.right - mPrgDrawableRect.left))
					/ (mMaxDrawableRect.right - mMaxDrawableRect.left) * mMaxProgress));
			mIsProcessActionDown = false;
			return true;
		}
		return false;
	}

	private boolean isInProcessArea(int x, int y) {
		int extendsDistance = (mProcessCurrentImgWidth - (mMaxDrawableRect.top - mMaxDrawableRect.bottom)) / 2; // 上下边界扩大的模糊距离，取决于滑块宽度
		return x > mMaxDrawableRect.left && x < mMaxDrawableRect.right
				&& y > mMaxDrawableRect.top - extendsDistance
				&& y < mMaxDrawableRect.bottom + extendsDistance;
	}

	/**
	 * 
	 * 进度条是否被触摸
	 * @return
	 */
	public boolean isProcessActionDown() {
		return mIsProcessActionDown;
	}
}
