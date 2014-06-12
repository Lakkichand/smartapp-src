package com.go.util.graphics.effector.united;

import java.util.Random;

import android.graphics.Canvas;

import com.go.util.graphics.effector.gridscreen.GridScreenEffector;
import com.go.util.graphics.effector.subscreen.SubScreenEffector;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerEffector;
import com.go.util.scroller.ScreenScrollerListener;

/**
 * 屏幕特效和单元格特效的合并类
 * 
 * @author jiangxuwen
 * 
 */
public class CoupleScreenEffector implements ScreenScrollerEffector {
	/** 单元格特效类型 */
	public final static int GRIDSCREEN_EFFECTOR_TYPE = 1;
	/** 屏幕特效类型 */
	public final static int SUBSCREEN_EFFECTOR_TYPE = 2;
	/** 桌面场所单元格类特效的总数 */
	public final static int GRIDSCREEN_EFFECTOR_COUNT_IN_DESK = 6;
	/** 桌面场所屏幕类特效的总数 */
	public final static int SUBSCREEN_EFFECTOR_COUNT_IN_DESK = 11;
	/** 功能表场所单元格类特效的总数 */
	public final static int GRIDSCREEN_EFFECTOR_COUNT_IN_MENU = 7;
	/** 功能表场所屏幕类特效的总数 */
	public final static int SUBSCREEN_EFFECTOR_COUNT_IN_MENU = 9;
	/** 桌面场所 */
	public final static int PLACE_DESK = 1;
	/** 功能表场所 */
	public final static int PLACE_MENU = 2;
	final static int EFFECTOR_TYPE_RANDOM = -1;
	final static int EFFECTOR_TYPE_DEFAULT = 0;
	public final static int EFFECTOR_TYPE_RANDOM_CUSTOM = -2;

	private GridScreenEffector mGridScreenEffector;
	private SubScreenEffector mSubScreenEffector;
	private ScreenScroller mScroller;
	/** 特效的类型 */
	int mEffectorType = SUBSCREEN_EFFECTOR_TYPE;
	// 特效的场所：Desk or Menu
	private int mPlaceType;
	private boolean mIsRandom = false;
	private boolean mIsCustomRandom = false;
	private Random mRandom;
	private int mType = 0;

	private int[] mDeskCustomRandomEffects;
	private int[] mAppIconCustomRandomEffects;

	private int mTop;
	private int mLeft;

	/**
	 * 
	 * @param scroller
	 * @param placeType
	 *            特效作用的场所
	 * @param effectorType
	 *            特效的类型
	 */
	public CoupleScreenEffector(ScreenScroller scroller, int placeType, int effectorType) {
		assert (scroller != null);// 如果为null也就没任何意义了
		mEffectorType = effectorType;
		mPlaceType = placeType;
		mScroller = scroller;
		mScroller.setEffector(this);
		mRandom = new Random();
	}

	/** 获取当前使用的特效 */
	public ScreenScrollerEffector getScrollerEffector() {
		if (mEffectorType == GRIDSCREEN_EFFECTOR_TYPE) {
			if (mGridScreenEffector == null) {
				ScreenScroller scroller = mScroller;
				mGridScreenEffector = new GridScreenEffector(mScroller); // 这里会引起onDetach
				mGridScreenEffector.setScreenGap(mLeft);
				mGridScreenEffector.setTopPadding(mTop);
				mScroller = scroller;
				mScroller.setEffector(this);
			}

			return mGridScreenEffector;
		} else if (mEffectorType == SUBSCREEN_EFFECTOR_TYPE) {
			if (mSubScreenEffector == null) {
				ScreenScroller scroller = mScroller;
				mSubScreenEffector = new SubScreenEffector(mScroller); // 这里会引起onDetach
				mSubScreenEffector.setScreenGap(mLeft);
				mSubScreenEffector.setTopPadding(mTop);
				mScroller = scroller;
				mScroller.setEffector(this);
			}
			return mSubScreenEffector;
		}
		return null;
	}

	/**
	 * 屏幕循环切换开关变化时调用，把非当前的特效与scroller绑定
	 */
	public void onAttachReserveEffector(ScreenScrollerListener container) {
		if (mEffectorType == GRIDSCREEN_EFFECTOR_TYPE && null != mSubScreenEffector) {
			mSubScreenEffector.onAttach(container);
		} else if (mEffectorType == SUBSCREEN_EFFECTOR_TYPE && null != mGridScreenEffector) {
			mGridScreenEffector.onAttach(container);
		}
	}

	@Override
	public boolean onDraw(Canvas canvas) {
		getScrollerEffector().onDraw(canvas);
		return true;
	}

	@Override
	public void setType(int type) {
		if (mPlaceType == PLACE_DESK) {
			if (type == EFFECTOR_TYPE_RANDOM)// 随机
			{
				int index = mRandom.nextInt(SUBSCREEN_EFFECTOR_COUNT_IN_DESK
						+ GRIDSCREEN_EFFECTOR_COUNT_IN_DESK);
				setType(index);
				mIsRandom = true;
				mIsCustomRandom = false;
			} else if (type == EFFECTOR_TYPE_RANDOM_CUSTOM) {
				if (null != mDeskCustomRandomEffects) {
					int index = mRandom.nextInt(mDeskCustomRandomEffects.length);
					setType(mDeskCustomRandomEffects[index]);
					mIsCustomRandom = true;
					mIsRandom = false;
				}
			} else if (type >= EFFECTOR_TYPE_DEFAULT && type < SUBSCREEN_EFFECTOR_COUNT_IN_DESK) {
				mEffectorType = SUBSCREEN_EFFECTOR_TYPE;
				getScrollerEffector().setType(type);
				mScroller.setDepthEnabled(false);
				mIsRandom = false;
				mIsCustomRandom = false;
				mType = type;
			} else if (type >= SUBSCREEN_EFFECTOR_COUNT_IN_DESK) {
				mEffectorType = GRIDSCREEN_EFFECTOR_TYPE;
				mType = type;
				type = type - SUBSCREEN_EFFECTOR_COUNT_IN_DESK + 1;
				// 暂时只有咕噜咕噜（下标为2）特效要设深度
				if (type == GridScreenEffector.GRID_EFFECTOR_TYPE_CHARIOT
						|| type == GridScreenEffector.GRID_EFFECTOR_TYPE_CYLINDER
						|| type == GridScreenEffector.GRID_EFFECTOR_TYPE_SPHERE) {
					mScroller.setDepthEnabled(true);
				} else {
					mScroller.setDepthEnabled(false);
				}
				getScrollerEffector().setType(type);
				mIsRandom = false;
				mIsCustomRandom = false;
			}

		} else if (mPlaceType == PLACE_MENU) {
			if (type == EFFECTOR_TYPE_RANDOM)// 随机
			{
				int index = mRandom.nextInt(SUBSCREEN_EFFECTOR_COUNT_IN_MENU
						+ GRIDSCREEN_EFFECTOR_COUNT_IN_MENU) + 1;
				setType(index);
				mIsRandom = true;
				mIsCustomRandom = false;
			} else if (type == EFFECTOR_TYPE_RANDOM_CUSTOM) {
				int index = mRandom.nextInt(mAppIconCustomRandomEffects.length);
				setType(mAppIconCustomRandomEffects[index]);
				mIsCustomRandom = true;
				mIsRandom = false;
			} else if (type >= EFFECTOR_TYPE_DEFAULT && type < GRIDSCREEN_EFFECTOR_COUNT_IN_MENU) {
				mEffectorType = GRIDSCREEN_EFFECTOR_TYPE;
				getScrollerEffector().setType(type);
				// 3种特效要设深度
				if (type == GridScreenEffector.GRID_EFFECTOR_TYPE_CHARIOT
						|| type == GridScreenEffector.GRID_EFFECTOR_TYPE_CYLINDER
						|| type == GridScreenEffector.GRID_EFFECTOR_TYPE_SPHERE) {
					mScroller.setDepthEnabled(true);
				} else {
					mScroller.setDepthEnabled(false);
				}
				mIsRandom = false;
				mIsCustomRandom = false;
				mType = type;
			} else if (type >= GRIDSCREEN_EFFECTOR_COUNT_IN_MENU) {
				mEffectorType = SUBSCREEN_EFFECTOR_TYPE;
				mType = type;
				type = type - GRIDSCREEN_EFFECTOR_COUNT_IN_MENU + 1;
				mScroller.setDepthEnabled(false);
				getScrollerEffector().setType(type);
				mIsRandom = false;
				mIsCustomRandom = false;
			}
		}

	}

	@Override
	public void updateRandomEffect() {
		if (mIsRandom) {
			setType(EFFECTOR_TYPE_RANDOM);
		} else if (mIsCustomRandom) {
			setType(EFFECTOR_TYPE_RANDOM_CUSTOM);
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		if (mSubScreenEffector != null) {
			mSubScreenEffector.onSizeChanged(w, h, orientation);
		}
		if (mGridScreenEffector != null) {
			mGridScreenEffector.onSizeChanged(w, h, orientation);
		}
	}

	@Override
	public int getMaxOvershootPercent() {
		int result = getScrollerEffector().getMaxOvershootPercent();
		return result;
	}

	@Override
	public void onAttach(ScreenScrollerListener container) {
		assert (container != null);
		mScroller = container.getScreenScroller();
		getScrollerEffector().onAttach(container);
	}

	@Override
	public void onDetach() {
		getScrollerEffector().onDetach();
	}

	@Override
	public void setDrawQuality(int quality) {
		getScrollerEffector().setDrawQuality(quality);
	}

	@Override
	public void recycle() {
		getScrollerEffector().recycle();
	}

	public int getmType() {
		return mType;
	}

	public void setDeskCustomRandomEffects(int[] effects) {
		mDeskCustomRandomEffects = effects;
	}

	public void setAppIconCustomRandomEffects(int[] effects) {
		mAppIconCustomRandomEffects = effects;
	}

	@Override
	public void setScreenGap(int gap) {
		mLeft = gap;
		if (mSubScreenEffector != null) {
			mSubScreenEffector.setScreenGap(gap);
		}
		if (mGridScreenEffector != null) {
			mGridScreenEffector.setScreenGap(gap);
		}
	}

	@Override
	public void setTopPadding(int top) {
		mTop = top;
		if (mSubScreenEffector != null) {
			mSubScreenEffector.setTopPadding(top);
		}
		if (mGridScreenEffector != null) {
			mGridScreenEffector.setTopPadding(top);
		}
	}
}
