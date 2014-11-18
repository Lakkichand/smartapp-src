package com.escape.uninstaller.ui.scroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;

/**
 * 分屏视图的滚动器。 到两端继续切换就到另外一端
 * 
 * @author dengweiming
 * 
 */
class CycloidScreenScroller extends ScreenScroller implements CycloidDrawListener{
	boolean mFadeAtEndEnabled = true; // 当切换到两端回绕的时候使用淡入淡出的方式衔接背景壁纸

	static final int reduceOneCycle(int index, int count) {
		if (index < 0) {
			index += count;
		} else if (index >= count) {
			index -= count;
		}
		return index;
	}

	public CycloidScreenScroller(Context context, ScreenScrollerListener screenGroup) {
		this(context, screenGroup, null);
	}

	public CycloidScreenScroller(Context context, ScreenScrollerListener screenGroup,
			FastVelocityTracker tracker) {
		super(context, screenGroup, tracker);
		mCycloid = true;
	}

	@Override
	public void setPadding(float paddingFactor) {
		if (mTotalSize <= 0) {
			return;
		}
		// 忽略 paddingFactor，保留 mPaddingFactor为0.5f的默认值
		if (mPaddingFactor == 0.5f) {
			return;
		}
		abortAnimation();
		mPaddingFactor = 0.5f; // 注意在setScreenCount方法中已经修改它了，这里还原
		// mState = FINISHED;
		mMinScroll = -mScreenSize / 2;
		mMaxScroll = mTotalSize + mMinScroll;
		mScrollRatio = mMaxScroll > mMinScroll ? 1.0f / (mMaxScroll - mMinScroll) : 0;

		// 重设当前滚动量
		scrollScreenGroup(getDstScreen() * mScreenSize);
	}

	@Override
	protected void scrollScreenGroup(int newScroll) {
		int scroll = rewindScroll(newScroll);
		if (mState == ON_SCROLL) {
			mEndScroll += scroll - newScroll;
		}
		super.scrollScreenGroup(scroll);
	}

	@Override
	protected int computeScreenIndex(int scroll) {
		final int index = super.computeScreenIndex(scroll);
		return 0 <= index && index < mScreenCount ? index : 0;
	}

	@Override
	protected int onScrollAtEnd(int delta) {
		if (mScreenCount < 2) { // 只有一屏时不循环滚动
			return super.onScrollAtEnd(delta);
		}
		return delta;
	}

	int rewindScroll(int scroll) {
		if (scroll < mMinScroll) {
			scroll += mTotalSize;
		} else if (scroll >= mMaxScroll) {
			scroll -= mTotalSize;
		}
		return scroll;
	}

	@Override
	protected boolean flingToScreen(int dstScreen, int duration) {
		return gotoScreen(dstScreen, duration, mInterpolatorBak);
	}

	@Override
	protected int checkScreen(int screen) {
		return screen;
	}

	@Override
	/**
	 * @param dstScreen		约定位于范围[-1, mScreenCount]
	 */
	public boolean gotoScreen(int dstScreen, int duration, Interpolator interpolator) {
		if (dstScreen > mCurrentScreen && (dstScreen - mCurrentScreen) * 2 > mScreenCount) {
			dstScreen -= mScreenCount;
		} else if (dstScreen < mCurrentScreen && (mCurrentScreen - dstScreen) * 2 > mScreenCount) {
			dstScreen += mScreenCount;
		}
		return super.gotoScreen(dstScreen, duration, interpolator);
	}

	@Override
	public int getDstScreen() {
		return reduceOneCycle(mDstScreen, mScreenCount);
	}

	@Override
	public boolean isScrollAtEnd() {
		if (mScreenCount < 2) {
			return super.isScrollAtEnd();
		}
		return false;
	}

	@Override
	public boolean isOldScrollAtEnd() {
		if (mScreenCount < 2) {
			return super.isOldScrollAtEnd();
		}
		return false;
	}

	@Override
	public boolean drawBackground(Canvas canvas, int scroll) {
		if (!super.drawBackground(canvas, scroll)) {
			return false;
		}
//		if (!mBackgroundScrollEnabled || !mFadeAtEndEnabled || mBgAlwaysDrawn
//				|| mWallpaperDrawer == null || mWallpaperDrawer.getBackgroundDrawable() == null 
//				|| mScreenCount < 2 || (mState == MScroller.ON_FLING && mIsOvershooting)) {
//			return true;
//		}
//		int alpha = 0;
//		if (scroll > mLastScreenPos) {
//			alpha = (scroll - mLastScreenPos) * 255 / mScreenSize;
//			scroll -= mTotalSize;
//		} else if (scroll < 0) {
//			alpha = -scroll * 255 / mScreenSize;
//			scroll += mTotalSize;
//		}
//		if (alpha != 0) {
//			mWallpaperDrawer.setAlpha(alpha);
//			super.drawBackground(canvas, scroll);
//			mWallpaperDrawer.setAlpha(255);
//		}
		return true;
	}

	@Override
	protected int getBackgroundOffsetX(int scroll) {
		if (scroll > mLastScreenPos) {
			scroll = (scroll + mLastScreenPos) / 2;
		} else if (scroll < 0) {
			scroll /= 2;
		}
		return super.getBackgroundOffsetX(scroll);
	}

	@Override
	public void setBackground(Drawable drawable) {
		super.setBackground(drawable);
		if (mWallpaperDrawer != null && mWallpaperDrawer.getBackgroundBitmap() != null) {
			// 背景壁纸为BitmapDrawable类型的
			mFadeAtEndEnabled = true;
		} else {
			mFadeAtEndEnabled = false;
		}
	}

	@Override
	public int getPreviousScreen() {
		return reduceOneCycle(mCurrentScreen - 1, mScreenCount);
	}

	@Override
	public int getNextScreen() {
		return reduceOneCycle(mCurrentScreen + 1, mScreenCount);
	}

	/**
	 * 返回当前绘制的左边子屏索引
	 * 
	 * @return -1表示无效索引
	 */
	@Override
	public int getDrawingScreenA() {
		int drawingScreenA = mCurrentScreen;
		if (getCurrentScreenOffset() > 0) {
			--drawingScreenA;
		}
		int res = reduceOneCycle(drawingScreenA, mScreenCount);
		if (mScreenCount < 2 && res != drawingScreenA) {
			return -1;
		}
		return res;
	}

	/**
	 * 返回当前绘制的右边子屏索引
	 * 
	 * @return -1表示无效索引（在只绘制一屏的时候也是返回-1）
	 */
	@Override
	public int getDrawingScreenB() {
		int drawingScreenB = mCurrentScreen;
		final int offset = getCurrentScreenOffset();
		if (offset == 0) {
			return -1;
		}
		if (offset < 0) {
			++drawingScreenB;
		}
		int res = reduceOneCycle(drawingScreenB, mScreenCount);

		if (mScreenCount < 2 && res != drawingScreenB) {
			return -1;
		}
		return res;
	}

	@Override
	public void setWallpaperDrawer(IWallpaperDrawer wallpaperDrawer) {
		super.setWallpaperDrawer(wallpaperDrawer);
		if (wallpaperDrawer != null) {
			wallpaperDrawer.setCycloidDrawListener(this);
		}
	}
	
	@Override
	public int getCurrentAlpha() {
		int alpha = 0;
		int scroll = mScroll;
		if (!mBackgroundScrollEnabled || !mFadeAtEndEnabled || mBgAlwaysDrawn
				|| mWallpaperDrawer == null || mWallpaperDrawer.getBackgroundDrawable() == null 
				|| mScreenCount < 2 || (mState == MScroller.ON_FLING && mIsOvershooting)) {
			return alpha;
		}
		if (scroll > mLastScreenPos) {
			alpha = (scroll - mLastScreenPos) * 255 / mScreenSize;
			scroll -= mTotalSize;
		} else if (scroll < 0) {
			alpha = -scroll * 255 / mScreenSize;
			scroll += mTotalSize;
		}
		return alpha;
	}
	
	@Override
	public int getBackgroundX(int scroll) {
		int x = -getBackgroundOffsetX(scroll);
		if (mOrientation == HORIZONTAL) {
			x += mScroll;
		}
		return x;
	}
	
	@Override
	public int getCycloidScroll() {
		int scroll = mScroll;
		if (scroll > mLastScreenPos) {
			scroll -= mTotalSize;
		} else if (scroll < 0) {
			scroll += mTotalSize;
		}
		return scroll;
	}
}
