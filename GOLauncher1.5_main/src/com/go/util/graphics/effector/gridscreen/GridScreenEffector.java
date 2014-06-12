package com.go.util.graphics.effector.gridscreen;

import android.graphics.Canvas;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerEffector;
import com.go.util.scroller.ScreenScrollerListener;

/**
 * 只绘制当前两屏网格的特效类
 * 
 * @author dengweiming
 * 
 */
public class GridScreenEffector implements ScreenScrollerEffector {
	/** 低质量绘图 */
	public final static int DRAW_QUALITY_LOW = 0;
	/** 中等质量绘图 */
	public final static int DRAW_QUALITY_MID = 1;
	/** 高等质量绘图 */
	public final static int DRAW_QUALITY_HIGH = 2;

	final static int GRID_EFFECTOR_TYPE_SCRIBBLE = -2;
	final static int GRID_EFFECTOR_TYPE_RANDOM = -1;
	final static int GRID_EFFECTOR_TYPE_DEFAULT = 0;
	final static int GRID_EFFECTOR_TYPE_BINARY_STAR = 1;
	public final static int GRID_EFFECTOR_TYPE_CHARIOT = 2;
	public final static int GRID_EFFECTOR_TYPE_SHUTTER = 3;
	public final static int GRID_EFFECTOR_TYPE_CHORD = 4;
	public final static int GRID_EFFECTOR_TYPE_CYLINDER = 5;
	public final static int GRID_EFFECTOR_TYPE_SPHERE = 6;

	final static Interpolator DecelerateInterpolator3 = new DecelerateInterpolator(1.5f);
	final static Interpolator DecelerateInterpolator5 = new DecelerateInterpolator(2.5f);

	GridScreenContainer mContainer;
	ScreenScroller mScroller;
	int mOrientation;
	int mScreenSize;
	MGridScreenEffector[] mRandomEffectors;
	MGridScreenEffector mEffector;
	int mCurrentIndex;
	int mType;
	int mQuality;
	int mGap;
	int mTopPadding;

	public GridScreenEffector(ScreenScroller scroller) {
		assert (scroller != null); // 如果为null也就没任何意义了
		mScroller = scroller;
		mScroller.setEffector(this);
	}

	@Override
	public boolean onDraw(Canvas canvas) {
		if (mOrientation == ScreenScroller.VERTICAL) {
			return false; // 暂时不支持垂直滚屏特效
		}
		boolean bgDrawn = mScroller.isBackgroundAlwaysDrawn();

		final int screenA = mScroller.getDrawingScreenA();
		final int screenB = mScroller.getDrawingScreenB();
		final int scroll = mScroller.getScroll() + mGap * 2;

		boolean combinebg = mEffector != null && mEffector.isCombineBackground();
		if (!combinebg) {
			bgDrawn |= mScroller.drawBackground(canvas, mScroller.getScroll());
		}
		if (!bgDrawn) {
			canvas.drawColor(0x00000000);
		}

		int offset = mScroller.getCurrentScreenOffset();
		if (offset > 0) {
			offset -= mScreenSize;
		}
		// offset += mGap * 2;

		final int top = mTopPadding;

		if (offset == 0 && mScroller.getCurrentDepth() == 0) {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA - 1, offset
					- mScreenSize, top, scroll);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA + 1, offset
					+ mScreenSize, top, scroll);
			MGridScreenEffector.drawScreen(mContainer, canvas, screenA, offset, top, scroll);
		} else if (mEffector == null) {
			MGridScreenEffector.drawScreen(mContainer, canvas, screenA, offset, top, scroll);
			MGridScreenEffector.drawScreen(mContainer, canvas, screenB, offset + mScreenSize, top,
					scroll);
		} else {
			canvas.save();
			if (mEffector.isCurrentScreenOnTop() && screenA == mScroller.getCurrentScreen()) {
				MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
						+ mScreenSize, top, scroll);
				mEffector.drawScreen(canvas, screenB, offset + mScreenSize, top, scroll);
				MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
						scroll);
				mEffector.drawScreen(canvas, screenA, offset, top, scroll);
			} else {
				MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
						scroll);
				mEffector.drawScreen(canvas, screenA, offset, top, scroll);
				MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
						+ mScreenSize, top, scroll);
				mEffector.drawScreen(canvas, screenB, offset + mScreenSize, top, scroll);
			}
			canvas.restore();
		}
		return true;
	}

	@Override
	public void setType(int type) {
		MGridScreenEffector oldEffector = mEffector;
		if (type == GRID_EFFECTOR_TYPE_RANDOM) {
			if (mType != type) {
				mRandomEffectors = new MGridScreenEffector[] { new BinaryStarEffector(),
						new ChariotEffector(), new ShutterEffector(), new ChordEffector(),
						new CylinderEffector(), new SphereEffector(), };
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
				case GRID_EFFECTOR_TYPE_BINARY_STAR :
					mEffector = new BinaryStarEffector();
					break;
				case GRID_EFFECTOR_TYPE_CHARIOT :
					mEffector = new ChariotEffector();
					break;
				case GRID_EFFECTOR_TYPE_SHUTTER :
					mEffector = new ShutterEffector();
					break;
				case GRID_EFFECTOR_TYPE_CHORD :
					mEffector = new ChordEffector();
					break;
				case GRID_EFFECTOR_TYPE_CYLINDER :
					mEffector = new CylinderEffector();
					break;
				case GRID_EFFECTOR_TYPE_SPHERE :
					mEffector = new SphereEffector();
					break;
				case GRID_EFFECTOR_TYPE_SCRIBBLE :
					mEffector = new ScribbleEffector();
					break;
				default :
					mEffector = null;
					break;
			}
		}
		mScroller.setInterpolator(mEffector == null
				? DecelerateInterpolator3
				: DecelerateInterpolator5);
		if (oldEffector != mEffector) {
			if (oldEffector != null) {
				oldEffector.onDetach();
			}
			if (mEffector != null) {
				mEffector.setDrawQuality(mQuality);
				mEffector.onAttach(mContainer, mScroller);
			}
		}
	}

	@Override
	public void updateRandomEffect() {
		if (mType == GRID_EFFECTOR_TYPE_RANDOM) {
			setType(GRID_EFFECTOR_TYPE_RANDOM);
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		mOrientation = mScroller.getOrientation();
		mScreenSize = mScroller.getScreenSize();
		if (mEffector != null) {
			mEffector.onSizeChanged(w, h);
		}

	}

	@Override
	public int getMaxOvershootPercent() {
		return 0;
	}

	@Override
	public void onAttach(ScreenScrollerListener container) {
		if (container != null && container instanceof GridScreenContainer) {
			ScreenScroller scroller = container.getScreenScroller();
			mContainer = (GridScreenContainer) container;
			if (scroller == null) {
				throw new IllegalArgumentException("Container has no ScreenScroller.");
			} else if (mScroller != scroller) {
				mScroller = scroller;
				mOrientation = mScroller.getOrientation();
				mScreenSize = mScroller.getScreenSize();
				int oldType = mType;
				mType = GRID_EFFECTOR_TYPE_DEFAULT;
				mEffector = null;
				setType(oldType);
			}
		} else {
			throw new IllegalArgumentException(
					"container is not an instance of GridScreenEffector.GridScreenContainer");
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
