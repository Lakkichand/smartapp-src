package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;

import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerEffector;
import com.go.util.scroller.ScreenScrollerListener;

/**
 * 只绘制当前两屏的特效类
 * 
 * @author dengweiming
 * 
 */
public class SubScreenEffector implements ScreenScrollerEffector {

	/** 低质量绘图 */
	public final static int DRAW_QUALITY_LOW = 0;
	/** 中等质量绘图 */
	public final static int DRAW_QUALITY_MID = 1;
	/** 高等质量绘图 */
	public final static int DRAW_QUALITY_HIGH = 2;

	final static int EFFECTOR_TYPE_RANDOM = -1;
	final static int EFFECTOR_TYPE_DEFAULT = 0;
	final static int EFFECTOR_TYPE_CUBOID2 = 1;
	final static int EFFECTOR_TYPE_FLIP2 = 2;
	final static int EFFECTOR_TYPE_ROLL = 4;
	final static int EFFECTOR_TYPE_WINDMILL = 7;

	final static int EFFECTOR_TYPE_BOUNCE = 3;
	final static int EFFECTOR_TYPE_BULLDOZE = 5;
	final static int EFFECTOR_TYPE_CUBOID1 = 6;
	final static int EFFECTOR_TYPE_FLIP = 8;
	final static int EFFECTOR_TYPE_WAVE = 9;
	final static int EFFECTOR_TYPE_STACK = 10;

	SubScreenContainer mContainer;
	ScreenScroller mScroller;
	MSubScreenEffector mEffector;
	MSubScreenEffector[] mRandomEffectors;
	int mCurrentIndex;
	int mType;
	int mBackgroundColor = 0xFF000000;
	int mScreenSize;
	int mOrientation;
	int mQuality;
	int mGap;
	int mTopPadding;

	public SubScreenEffector(ScreenScroller scroller) {
		assert (scroller != null); // 如果为null也就没任何意义了
		mScroller = scroller;
		mScroller.setEffector(this);
	}

	@Override
	public boolean onDraw(Canvas canvas) {
		final int extraX = mGap * 2;
		final int scroll = mScroller.getScroll();
		boolean bgDrawn = mScroller.isBackgroundAlwaysDrawn();
		boolean combinebg = mEffector != null && mEffector.isCombineBackground();
		if (!combinebg) {
			bgDrawn |= mScroller.drawBackground(canvas, scroll);
		}
		if (!bgDrawn) {
			canvas.drawColor(mBackgroundColor);
		}
		int curOffset = mScroller.getCurrentScreenOffset();
		int offset = curOffset;
		if (offset > 0) {
			offset -= mScreenSize;
		}

		final int top = mTopPadding;
		final int screenA = mScroller.getDrawingScreenA();
		final int screenB = mScroller.getDrawingScreenB();
		if (offset == 0 && mScroller.getCurrentDepth() == 0) {
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA, offset + extraX,
					top, bgDrawn);
			if (mScroller.getLayoutScale() < 1.0f) {
				MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA - 1, offset
						+ extraX - mScreenSize, top, bgDrawn);
				MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA + 1, offset
						+ extraX + mScreenSize, top, bgDrawn);
			}
		} else if (mEffector == null) {
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA, offset + extraX,
					top, bgDrawn);
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenB, offset + extraX
					+ mScreenSize, top, bgDrawn);
			if (mScroller.getLayoutScale() < 1.0f) {
				if (Math.abs(offset) >= mScreenSize / 2) {
					MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenB + 1, offset
							+ extraX + mScreenSize + mScreenSize, top, bgDrawn);
				} else {
					MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA - 1, offset
							+ extraX - mScreenSize, top, bgDrawn);
				}
			}

		} else {
			mEffector.onScrollChanged(mScroller.getScroll() + extraX, curOffset);
			if (mEffector.toReverse()) {
				mEffector.drawView(canvas, screenB, offset + mScreenSize, top, false);
				mEffector.drawView(canvas, screenA, offset, top, true);
			} else {
				mEffector.drawView(canvas, screenA, offset, top, true);
				mEffector.drawView(canvas, screenB, offset + mScreenSize, top, false);
			}
		}
		return true;
	}

	@Override
	public void setType(int type) {
		MSubScreenEffector oldEffector = mEffector;
		if (type <= EFFECTOR_TYPE_RANDOM) {
			if (mRandomEffectors == null) {
				mRandomEffectors = new MSubScreenEffector[] { new BounceEffector(),
						new BulldozeEffector(), new CuboidInsideEffector(),
						new CuboidOutsideEffector(), new FlipEffector(), // not
																			// contain
																			// Flip2Effector
						new RollEffector(), new WaveEffector(), new WindmillEffector(), };
				mCurrentIndex = -1;
			}
			mType = type;
			int index = (int) (Math.random() * mRandomEffectors.length);
			if (index == mCurrentIndex) {
				index = (index + 1) % mRandomEffectors.length;
			}
			mEffector = mRandomEffectors[index];
			mCurrentIndex = index;
		} else if (mType == type) {
			return;
		} else {
			mType = type;
			mRandomEffectors = null;
			switch (type) {
				case EFFECTOR_TYPE_BOUNCE :
					mEffector = new BounceEffector();
					break;
				case EFFECTOR_TYPE_BULLDOZE :
					mEffector = new BulldozeEffector();
					break;
				case EFFECTOR_TYPE_CUBOID1 :
					mEffector = new CuboidInsideEffector();
					break;
				case EFFECTOR_TYPE_CUBOID2 :
					mEffector = new CuboidOutsideEffector();
					break;
				case EFFECTOR_TYPE_FLIP :
					mEffector = new FlipEffector();
					break;
				case EFFECTOR_TYPE_FLIP2 :
					mEffector = new Flip2Effector();
					break;
				case EFFECTOR_TYPE_ROLL :
					mEffector = new RollEffector();
					break;
				case EFFECTOR_TYPE_WAVE :
					mEffector = new WaveEffector();
					break;
				case EFFECTOR_TYPE_WINDMILL :
					mEffector = new WindmillEffector();
					break;
				case EFFECTOR_TYPE_STACK :
					mEffector = new StackEffector();
					break;
				default :
					mEffector = null;
					break;
			}
		}
		if (oldEffector != mEffector) {
			if (oldEffector != null) {
				oldEffector.onDetach();
			}
			if (mEffector != null) {
				mEffector.setDrawQuality(mQuality);
				mEffector.onAttach(mContainer, mScroller);
			} else {
				mScroller.setOvershootPercent(100);
			}
		}
	}

	@Override
	public void updateRandomEffect() {
		if (mType == EFFECTOR_TYPE_RANDOM) {
			setType(EFFECTOR_TYPE_RANDOM);
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		mOrientation = mScroller.getOrientation();
		mScreenSize = mScroller.getScreenSize();
		if (mEffector != null) {
			mEffector.onSizeChanged();
		}

	}

	@Override
	public int getMaxOvershootPercent() {
		return mEffector == null ? 100 : mEffector.getMaxOvershootPercent();
	}

	@Override
	public void onAttach(ScreenScrollerListener container) {
		if (container != null && container instanceof SubScreenContainer) {
			ScreenScroller scroller = container.getScreenScroller();
			mContainer = (SubScreenContainer) container;
			if (scroller == null) {
				throw new IllegalArgumentException("Container has no ScreenScroller.");
			} else if (mScroller != scroller) {
				mScroller = scroller;
				mOrientation = mScroller.getOrientation();
				mScreenSize = mScroller.getScreenSize();
				int oldType = mType;
				mType = EFFECTOR_TYPE_DEFAULT;
				mEffector = null;
				setType(oldType);
			}
		} else {
			throw new IllegalArgumentException(
					"container is not an instance of SubScreenEffector.SubScreenContainer");
		}

	}

	@Override
	public void onDetach() {
		mContainer = null;
		mScroller = null;
		// mRandomEffectors = null;
		if (mEffector != null) {
			mEffector.onDetach();
		}

	}

	@Override
	public void setDrawQuality(int quality) {
		mQuality = quality;
		if (mEffector != null) {
			mEffector.setDrawQuality(quality);
		}

	}

	@Override
	public void recycle() {
		mRandomEffectors = null;
	}

	@Override
	public void setScreenGap(int gap) {
		mGap = gap;
	}

	@Override
	public void setTopPadding(int top) {
		mTopPadding = top;
	}
}
