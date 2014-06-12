package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

import com.go.util.scroller.ScreenScroller;

abstract class MSubScreenEffector {

	protected static PaintFlagsDrawFilter DRAW_FILTER_LOW_QUALITY = null;
	protected static PaintFlagsDrawFilter DRAW_FILTER_MID_QUALITY = new PaintFlagsDrawFilter(0,
			Paint.ANTI_ALIAS_FLAG);
	protected static PaintFlagsDrawFilter DRAW_FILTER_HIGH_QUALITY = new PaintFlagsDrawFilter(0,
			Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

	protected int mOrientation;
	protected int mScreenSize;
	protected int mWidth;
	protected int mHeight;
	protected int mScroll;
	protected float mCenterX;
	protected float mCenterY;
	protected boolean mCombineBackground;
	protected int mOvershootPercent = 0;
	protected ScreenScroller mScroller;
	protected SubScreenContainer mContainer;
	protected int mQuality;
	protected boolean mNeedQuality = true;
	protected boolean mReverse = false;
	protected int mAlpha = 255;

	protected void drawView(Canvas canvas, int screen, int offset, int topPadding, boolean first) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			// if(!bgDrawn
			// && mCombineBackground
			// && !mScroller.mCycloid
			// && screen >= 0
			// && screen < mScroller.getScreenCount()){
			// // 作变换，并绘制背景
			// }
			return;
		}
		final DrawFilter filter = canvas.getDrawFilter();
		if (mNeedQuality) {
			requestQuality(canvas, SubScreenEffector.DRAW_QUALITY_HIGH);
		}
		int saveCount = canvas.save();
		// 先适应屏幕缩小状态下的整体偏移
		canvas.translate(0, topPadding);
		if (onDrawScreen(canvas, screen, offset, first)) {
			canvas.clipRect(0, 0, mWidth, mHeight);
			if (mCombineBackground) {
				mScroller.drawBackgroundOnScreen(canvas, screen);
			}
			if (mAlpha == 255) {
				mContainer.drawScreen(canvas, screen);
			} else if (mAlpha > 0) {
				mContainer.drawScreen(canvas, screen, mAlpha);
			}
		}
		canvas.restoreToCount(saveCount);
		canvas.setDrawFilter(filter);
	}

	public void onSizeChanged() {
		mScreenSize = mScroller.getScreenSize();
		mOrientation = mScroller.getOrientation();
		mWidth = mScroller.getScreenWidth();
		mHeight = mScroller.getScreenHeight();
		mCenterX = mWidth * 0.5f;
		mCenterY = mHeight * 0.5f;
	}

	/*
	 * 是否后画第first屏
	 */
	protected boolean toReverse() {
		return mReverse;
	}

	public int getMaxOvershootPercent() {
		return mOvershootPercent;
	}

	protected void onScrollChanged(int scroll, int offset) {
		mScroll = scroll;
	}

	protected void onAttach(SubScreenContainer container, ScreenScroller scroller) {
		mContainer = container;
		mScroller = scroller;
		mScroller.setOvershootPercent(mOvershootPercent);
		onSizeChanged();
	}

	protected void onDetach() {
		mScroller = null;
		mContainer = null;
	}

	public boolean isCombineBackground() {
		return mCombineBackground;
	}

	void setDrawQuality(int quality) {
		mQuality = quality;
	}

	/**
	 * 
	 * @param canvas
	 *            假设当前canvas已经是低质量的，并且调用者在用完canvas之后要负责还原质量
	 * @param quality
	 */
	final protected void requestQuality(Canvas canvas, int quality) {
		quality = Math.min(quality, mQuality);
		switch (quality) {
			case SubScreenEffector.DRAW_QUALITY_MID :
				canvas.setDrawFilter(DRAW_FILTER_MID_QUALITY);
				break;
			case SubScreenEffector.DRAW_QUALITY_HIGH :
				canvas.setDrawFilter(DRAW_FILTER_HIGH_QUALITY);
				break;
		}
	}

	/**
	 * 
	 * @param canvas
	 *            画布
	 * @param offset
	 *            绘制的屏的偏移量
	 * @param first
	 *            是否是索引较小的屏（在左边或者上边）
	 */
	abstract protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first);

	// 添加的topPadding是为了在编辑的缩放模式下整体绘制下移
	static void drawView(SubScreenContainer container, ScreenScroller scroller, Canvas canvas,
			int screen, int offset, int topPadding, boolean bgDrawn) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		final int orientation = scroller.getOrientation();
		final int scroll = scroller.getScroll();
		final int width = scroller.getScreenWidth();
		final int height = scroller.getScreenHeight();
		canvas.save();
		if (orientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(scroll + offset, topPadding);
		} else {
			canvas.translate(0, scroll + offset);
		}
		canvas.clipRect(0, 0, width, height);
		if (!bgDrawn) {
			scroller.drawBackgroundOnScreen(canvas, screen);
		}
		container.drawScreen(canvas, screen);
		canvas.restore();
	}

}
