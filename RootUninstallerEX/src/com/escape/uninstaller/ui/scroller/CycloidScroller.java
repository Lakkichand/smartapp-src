package com.escape.uninstaller.ui.scroller;

import android.content.Context;
import android.view.MotionEvent;

/**
 * 竖向滚动的循环滚动器。 到两端继续切换就到另外一端
 * 
 * @author yejijiong
 * 
 */
public class CycloidScroller extends Scroller {
	/**
	 * 是否正在进行竖向循环滚屏（需解开最大滚动距离的限制）
	 */
	public boolean mIsVerticalCirculateScroll = false;

	public CycloidScroller(Context context, ScrollerListener listener) {
		super(context, listener);
		mCycloid = true;
	}

	@Override
	protected void checkNeedScrollBack(float t) {
		if (!mIsVerticalCirculateScroll) {
			if (t > 0.99f) {
				scrollBack(mScroll);
			}
		}
	}

	// @Override
	// protected int getMaxScrollOnFling() {
	// final int extraValue = mIsVerticalCirculateScroll ? 800 : 0;
	// return (mMaxScrollOnFling + extraValue);
	// }

	// @Override
	// protected int getMinScrollOnFling() {
	// final int extraValue = mIsVerticalCirculateScroll ? -800 : 0;
	// return (mMinScrollOnFling + extraValue);
	// }

	@Override
	protected int getMaxScroll() {
		final int extraValue = mIsVerticalCirculateScroll ? 800 : 0;
		return (mMaxScroll + extraValue);
	}

	@Override
	protected int getmMinScroll() {
		final int extraValue = mIsVerticalCirculateScroll ? -800 : 0;
		return (mMinScroll + extraValue);
	}

	@Override
	protected void onComputeFlingOffset(float t) {
		if (mState == FINISHED && mIsVerticalCirculateScroll) {
			resetCycleState();
		} else {
			super.onComputeFlingOffset(t);
		}
	}

	@Override
	public void abortAnimation() {
		super.abortAnimation();
		resetCycleState();
	}

	@Override
	protected void needScrollToEdge(int scroll, int curVelocity) {
		if ((scroll < 0 || scroll >= mLastScroll) && !mIsVerticalCirculateScroll) {
			scrollToEdge(scroll, curVelocity);
		} else {
			scrollScreenGroup(scroll);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, int action) {
		if (mIsVerticalCirculateScroll) {
			return true;
		}
		resetDeceleration(); // 这里为了预防加速度不能正常还原，在每次Touch时间检测下加速度增量是否不为1，不为1则还原加速度为原值
		return super.onTouchEvent(event, action);
	}

	/**
	 * 竖向循环滚动
	 */
	public void scrollWithCycle(int scroll) {
		mIsVerticalCirculateScroll = true;
		resetDeceleration();
		mDecelerationExtraValue = 5; // 加速度所增加的倍数
		flingByScroll(scroll);
	}

	/**
	 * 还原加速度
	 */
	private void resetDeceleration() {
		if (mDecelerationExtraValue != 1) {
			mDeceleration = mDeceleration / mDecelerationExtraValue;
			mDecelerationExtraValue = 1;
		}
	}

	/**
	 * 重置竖向循环的状态
	 */
	private void resetCycleState() {
		if (mIsVerticalCirculateScroll) {
			mIsVerticalCirculateScroll = false;
			resetDeceleration();
			if (mScroll > 0) {
				setScroll(0);
			} else {
				setScroll(mLastScroll);
			}
		}
	}
}
