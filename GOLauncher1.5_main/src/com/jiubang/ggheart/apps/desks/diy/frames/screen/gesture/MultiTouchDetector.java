package com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * 多点触摸手势识别
 * 
 * @author luopeihuan
 * 
 */

public class MultiTouchDetector {
	private static final String TAG = "gesture";
	private static final boolean DEBUG = false;

	private static final String ACTION_NAMES[] = { "ACTION_DOWN", "ACTION_UP", "ACTION_MOVE",
			"ACTION_CANCEL", "ACTION_OUTSIDE", "ACTION_POINTER_DOWN", "ACTION_POINTER_UP", "7?",
			"8?", "9?" };

	private static final String STATE_NAMES[] = { "degreeDelta", "STATE_TOUCH", "STATE_SCROLL",
			"STATE_SWIPE", "STATE_SCALE", "STATE_ROTATE" };

	private static final int STATE_NONE = 0; // 初始状态，没有touch事件
	private static final int STATE_TOUCH = 1; // 滑动
	private static final int STATE_SCROLL = 2; // 单指滑动
	private static final int STATE_SWIPE = 3; // 多指滑动
	private static final int STATE_SCALE = 4; // 多点触摸

	// // constants for Message.what used by GestureHandler below
	// private static final int LONG_PRESS = 10;
	// private static final int TAP = 11;

	/* 同时缓存触摸点个数 */
	public static final int MAX_TOUCH_POINTS = 20;
	private final float[] mXs = new float[MAX_TOUCH_POINTS];
	private final float[] mYs = new float[MAX_TOUCH_POINTS];
	private final float[] mPresures = new float[MAX_TOUCH_POINTS];
	private final int[] mPointerIds = new int[MAX_TOUCH_POINTS];

	// private final int mTouchSlopSquare;
	private final int mTouchSlop;

	// private static final int LONGPRESS_TIMEOUT =
	// ViewConfiguration.getLongPressTimeout();
	// private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
	private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout(); // 双击手势超时常量

	/**
	 * The smallest possible distance between multitouch points (used to avoid
	 * div-by-zero errors and display glitches)
	 */
	private static final float MIN_MULTITOUCH_SEPARATION = 30.0f; // 最小多指操作距离

	// /**
	// * 判断pinch手势的压力阈值，当前事件的压力值/上次事件的压力值大于此值才有效
	// * copy from {@link ScaleGestureDetector}
	// **/
	// private static final float PRESSURE_THRESHOLD = 0.67f;

	/**
	 * 判断缩放操作时忽略的角度变化，大于这个值即认为是旋转操作
	 */
	private static final float MIN_ROTATE_ANGLE = 7.5f; // 最小旋转角度（弧度）
	private static final float MIN_SCALE_DIFF = 15f; // 缩放差值

	/**
	 * 由多指滑动向缩放手势变化的阈值（临界值）
	 */
	private static final float SWIPE_TO_SCALE_ANGLE_THRESHOLD = 18f; // 角度
	private static final float SWIPE_TO_SCALE_DIFF_THRESHOLD = 35f;  // 差值

	private static final int SWIPE_VELOCITY = 300; // 上下滑屏识别为手势的速度阈值

	/**
	 * 多点触摸样本点 >=3;
	 */
	private static final int SAMPLE_SIZE = 3;

	private OnMultiTouchListener mListener;
	private MutilPointInfo mPrevPoint = new MutilPointInfo(); // 上次事件
	private MutilPointInfo mCurrPoint = new MutilPointInfo(); // 本次事件

	private float mLastMotionY; // 上一次事件Y坐标
	private float mLastMotionX; // 上一次事件X坐标

	// private long mTimeDelta;
	private int mTouchState = STATE_NONE;
	private boolean mTouchMoved = false;

	private int mActiveId;
	private boolean mStartDecodeMultiTouch = false;

	private MutilPointInfo mLastHistoryPoint;  // 上一次历史点击触摸点
	private ArrayList<MutilPointInfo> mHistory = new ArrayList<MutilPointInfo>(SAMPLE_SIZE);

	private float mStartAngle;  // 开始的角度
	private float mStartDistance;  // 开始的距离

	// for single tap, double tap, long press detect
	// private boolean mStillDown;
	private boolean mInLongPress;  // 正在长按
	// private boolean mAlwaysInTapRegion;

	private int mDoubleTapSlopSquare;
	private boolean mIsLongpressEnabled;

	/**
	 * True when the user is still touching for the second tap (down, move, and
	 * up events). Can only be true if there is a double tap listener attached.
	 */
	private boolean mIsDoubleTapping;

	// 用于判断是否为滑动操作，主要为双击手势的第一次点击准备 add by chenqiang
	private boolean mIsMoved = false;

	// private int mMinimumFlingVelocity;
	private int mMaximumFlingVelocity; // 滚动速率
	// /**
	// * Determines speed during touch scrolling
	// */
	private VelocityTracker mVelocityTracker;  // 速率监听

	private MutilPointInfo mCurrDownPoint = new MutilPointInfo();  // 当前按下
	private MutilPointInfo mPrevUpPoint = new MutilPointInfo();   // 上一次放手
	// private final Handler mHandler;

	public MultiTouchDetector(Context context, OnMultiTouchListener listener) {
		mListener = listener;
		mIsLongpressEnabled = true;
		// mHandler = new GestureHandler();

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		// mMinimumFlingVelocity =
		// configuration.getScaledMinimumFlingVelocity();
		mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();

		mTouchSlop = configuration.getScaledTouchSlop();
		int doubleTapSlop = configuration.getScaledDoubleTapSlop();
		mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
	}

	/**
	 * <br>
	 * 功能简述:触摸回调事件 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param ev
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;

		if (DEBUG) {
			Log.i(TAG, "action = " + action + ", actionMask = " + actionCode);
		}
		// int pIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
		// MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		// int pid = ev.getPointerId(pIndex);
		// Log.i(TAG, ACTION_NAMES[actionCode] + " pindex = " + pIndex +
		// " pid = " + pid + " pcount = " + pCount );

		acquireVelocityTrackerAndAddMovement(ev);
		gatherPointInfo(ev, actionCode);
		return analyseTouchEvent(ev, actionCode);
	}

	/**
	 * <br>
	 * 功能简述:需要跟踪触屏事件速度，获取VelocityTracker类实例并添加事件 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param ev
	 */
	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain(); // 需要跟踪触摸屏事件的速度,使用obtain()方法来获得VelocityTracker类的一个实例对象
		}
		mVelocityTracker.addMovement(ev); // onTouchEvent回调函数中，使用addMovement(MotionEvent)函数将当前的移动事件传递给VelocityTracker对象
	}

	/**
	 * <br>
	 * 功能简述:释放触屏跟踪 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	/**
	 * Set whether longpress is enabled, if this is enabled when a user presses
	 * and holds down you get a longpress event and nothing further. If it's
	 * disabled the user can press and hold down and then later moved their
	 * finger and you will get scroll events. By default longpress is enabled.
	 * 
	 * @param isLongpressEnabled
	 *            whether longpress should be enabled.
	 */
	public void setIsLongpressEnabled(boolean isLongpressEnabled) {
		mIsLongpressEnabled = isLongpressEnabled;
	}

	/**
	 * @return true if longpress is enabled, else false.
	 */
	public boolean isLongpressEnabled() {
		return mIsLongpressEnabled;
	}

	// /**
	// * 设置最小fling最小速度值，大于此值才回调onFling接口
	// * @param velocity
	// */
	// public void setMinFlingVelocity(int velocity) {
	// mMinimumFlingVelocity = velocity;
	// }

	/**
	 * <br>
	 * 功能简述:收集Touch事件信息 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param event
	 *            事件
	 * @param actionCode
	 *            动作类型
	 */
	private void gatherPointInfo(MotionEvent event, int actionCode) {
		int count = event.getPointerCount();  // 获取点的个数
		if (count == 1) // 只有一个点
		{
			mXs[0] = event.getX();
			mYs[0] = event.getY();
			mPresures[0] = event.getPressure(); // for the first pointer index
												// (may be an arbitrary pointer
												// identifier).
			mPointerIds[0] = event.getPointerId(0);
		} else {
			final int pointNum = Math.min(count, MAX_TOUCH_POINTS);
			for (int i = 0; i < pointNum; i++) {
				int ptrId = event.getPointerId(i);
				mPointerIds[i] = ptrId;
				mXs[i] = event.getX(i);
				mYs[i] = event.getY(i);
				mPresures[i] = event.getPressure(i);
				// Log.i(TAG, "index " + i + "= (" + "pid " + mPointerIds[i]
				// + ": " + mXs[i] + ", " + mYs[i] + ")");
			}
		}

		// Swap curr/prev points
		MutilPointInfo tmp = mPrevPoint;
		mPrevPoint = mCurrPoint;
		mCurrPoint = tmp;

		// 更新当前事件
		boolean isDown = actionCode == MotionEvent.ACTION_DOWN
				|| actionCode == MotionEvent.ACTION_MOVE
				|| actionCode == MotionEvent.ACTION_POINTER_DOWN;

		mCurrPoint.set(count, mXs, mYs, mPresures, mPointerIds, actionCode, isDown,
				event.getEventTime());
	}

	/**
	 * <br>
	 * 功能简述:分析点击事件 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param ev
	 * @param actionCode
	 * @return
	 */
	private boolean analyseTouchEvent(MotionEvent ev, int actionCode) {
		final float y = ev.getY();
		final float x = ev.getX();

		boolean handle = false;
		int pCount = ev.getPointerCount();

		switch (actionCode) {
		// 当屏幕检测到有手指按下之后就触发到这个事件
			case MotionEvent.ACTION_DOWN : {
				mHistory.clear(); // 清空历史点的信息
				mStartDecodeMultiTouch = false;

				mActiveId = ev.getPointerId(0);
				// 第一个点
				mLastMotionX = x;
				mLastMotionY = y;

				// 如果第一次的up不是滑动的up 第二次down即可标志位doubletap
				if (!mIsMoved) {
					mIsDoubleTapping = isConsideredDoubleTap(mCurrDownPoint, mPrevUpPoint,
							mCurrPoint);
					mIsMoved = false;
				}
				mTouchMoved = false;

				mCurrDownPoint.set(mCurrPoint);
				// mAlwaysInTapRegion = true;
				// mStillDown = true;
				mInLongPress = false;

				// 长按
				// if (mIsLongpressEnabled) {
				// mHandler.removeMessages(LONG_PRESS);
				// mHandler.sendEmptyMessageAtTime(LONG_PRESS,
				// mCurrPoint.getEventTime() + TAP_TIMEOUT
				// + LONGPRESS_TIMEOUT);
				// }
				// TODO 屏蔽单击双击

			}
				break;

			// 实现多点的关键，当屏幕检测到有多个手指同时按下之后，就触发了这个事件。
			case MotionEvent.ACTION_POINTER_DOWN : {
				cancel();
				// Log.i("luoph", "ACTION_POINTER_DOWN " + getPointerIndex(ev));
				if (mLastHistoryPoint == null) {
					addToHistory(mCurrPoint);
				} else if (isConsideredAddToHistory(mCurrPoint, mLastHistoryPoint)) {
					addToHistory(mCurrPoint);
				}
			}
				break;

			case MotionEvent.ACTION_MOVE : {
				// 当前正在发生长按消息
				if (mInLongPress) {
					break;
				}

				if (mTouchState == STATE_NONE) {
					mTouchState = STATE_TOUCH;
				}

				final float dx = x - mLastMotionX;
				final float dy = y - mLastMotionY;

				final int touchSlop = mTouchSlop;
				boolean xMoved = Math.abs(dx) > touchSlop;
				boolean yMoved = Math.abs(dy) > touchSlop;
				if (!mTouchMoved) {
					mTouchMoved = xMoved || yMoved;
				}

				// 多点触摸
				if (isConsideredAddToHistory(mCurrPoint, mLastHistoryPoint)) {
					mTouchMoved = true;
					addToHistory(mCurrPoint);
				}
				handle |= notifyMultiTouchState(dx, dy);
				if (mTouchMoved) {
					mLastMotionX = x;
					mLastMotionY = y;
					mIsDoubleTapping = false; // 如果处于移动状态，mIsDoubleTapping为false
					// clear hanlder msg
					// mAlwaysInTapRegion = false;
					// TODO 屏蔽单击和双击
					// mHandler.removeMessages(TAP);
					// mHandler.removeMessages(LONG_PRESS);
				}

				// 判断手指是否超出双击区域
				if (isOutOfDoubleTapRange(pCount, x, y)) {
					mIsDoubleTapping = false;
				}
			}
				break;
			// 实现多点的关键，当屏幕检测到有多个手指同时按下放手，就触发了这个事件。
			case MotionEvent.ACTION_POINTER_UP : {
				mLastHistoryPoint = null;
				final int pointerIndex = getPointerIndex(ev);
				final int pointerId = ev.getPointerId(pointerIndex);
				if (pointerId == mActiveId) {
					// This was our active pointer going up. Choose a new
					// active pointer and adjust accordingly.
					final int newIndex = pointerIndex == 0 ? 1 : 0;
					mActiveId = ev.getPointerId(newIndex);
					mLastMotionX = ev.getX(newIndex);
					mLastMotionY = ev.getY(newIndex);

					// Log.i(TAG, "left index = " + newIndex + " [" +
					// mLastMotionX + ", " + mLastMotionY + "]");
				}

				releaseVelocityTracker();

				// 取消多点触摸状态
				if (mTouchState == STATE_SWIPE || mTouchState == STATE_SCALE) {
					mTouchState = STATE_SCROLL;
				}
				mStartDecodeMultiTouch = false;
			}
				break;
			// 当屏幕检测到有手指按下放手就触发到这个事件
			case MotionEvent.ACTION_UP : {
				// mStillDown = false;
				if (mIsDoubleTapping) {
					if (mTouchMoved) {
						mIsDoubleTapping = false;
					} else {
						handle |= mListener.onDoubleTap(mCurrDownPoint);
						// Log.i("luoph", "onDoubleTap");
						mIsDoubleTapping = false;
					}

				} else {
					// TODO 屏蔽长按
					// if (mInLongPress) {
					// mHandler.removeMessages(TAP);
					// mInLongPress = false;
					// }
					// else {
					// A fling must travel the minimum tap distance
					// TODO 屏蔽Fling事件
					// final VelocityTracker velocityTracker = mVelocityTracker;
					// velocityTracker.computeCurrentVelocity(1000,
					// mMaximumFlingVelocity);
					// final float velocityY = velocityTracker.getYVelocity();
					// final float velocityX = velocityTracker.getXVelocity();
					//
					// if ((Math.abs(velocityY) > mMinimumFlingVelocity)
					// || (Math.abs(velocityX) > mMinimumFlingVelocity)) {
					// handle |= mListener.onFling(mCurrPoint, velocityX,
					// velocityY);
					// }
					// }
				}

				// Hold the event we obtained above - listeners may have changed
				// the original.
				mPrevUpPoint.set(mCurrPoint);
				// mHandler.removeMessages(LONG_PRESS);

				releaseVelocityTracker();

				mIsMoved = mTouchMoved; // 把move的状态给mIsMoved
				// 重置状态
				mIsDoubleTapping = false;
				mStartDecodeMultiTouch = false;
				mTouchMoved = false;
				mTouchState = STATE_NONE;
			}
				break;

			case MotionEvent.ACTION_CANCEL : {
				cancel();
				// 重置状态
				mStartDecodeMultiTouch = false;
				mTouchMoved = false;
				mTouchState = STATE_NONE;
			}
				break;

			default :
				break;
		}

		return handle;
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void decodeHistoryState() {
		int size = mHistory.size();
		if (size >= 2) {
			float[] fingerDist = new float[size];  // 手指间的距离
			float[] fingerAngle = new float[size]; // 手指间的角度
			float[] fingerPressure = new float[size];  // 手指的压力
			float[] p0diffX = new float[size - 1];  // 第一个点的X差值
			float[] p0diffY = new float[size - 1];  // 第一个点的Y差值
			float[] p1diffX = new float[size - 1];  // 第二个点的X差值
			float[] p1diffY = new float[size - 1];  // 第二个点的Y差值

			for (int i = 0; i < size; i++) {
				final MutilPointInfo currInfo = mHistory.get(i);
				fingerDist[i] = getFingerDiff(currInfo);
				fingerAngle[i] = getFingerAngle(currInfo);
				fingerPressure[i] = currInfo.getPressure();

				if (i > 0) {
					final MutilPointInfo prevInfo = mHistory.get(i - 1);
					p0diffX[i - 1] = currInfo.getXs()[0] - prevInfo.getXs()[0];  // 当前点的值减去前面一个点的值
					p0diffY[i - 1] = currInfo.getYs()[0] - prevInfo.getYs()[0];

					p1diffX[i - 1] = currInfo.getXs()[1] - prevInfo.getXs()[1];
					p1diffY[i - 1] = currInfo.getYs()[1] - prevInfo.getYs()[1];
				}
			}

			// dumpArray(fingerDist, "Dist", TAG);
			// dumpArray(fingerAngle, "Angle", TAG);
			// dumpArray(fingerPressure, "Pressure", TAG);

			// // 判断连续几次事件的压力值是否合法
			// boolean pressureValid = false;
			// for (int i = 1; i < size; i++)
			// {
			// if (fingerPressure[i - 1] > 0)
			// {
			// final float ratio = fingerPressure[i] / fingerPressure[i - 1];
			// if (ratio > PRESSURE_THRESHOLD)
			// {
			// pressureValid = true;
			// }
			// }
			// }
			//
			// if (!pressureValid)
			// {
			// return;
			// }

			boolean swipe = true;
			for (int i = 0; i < size - 1; i++) {
				swipe = isSwipe(p0diffX[i], p0diffY[i], p1diffX[i], p1diffY[i]);
				if (!swipe) {
					// Log.e("luoph", "decodeHistoryState swipe = " + swipe);
					break;
				}
			}

			if (swipe) {
				mTouchState = STATE_SWIPE; // 触摸状态为多指滑动
			} else {
				float angleMaxDiff = getDiff(fingerAngle, true);  // 获取最大的角度差值
				float distMaxDiff = getDiff(fingerDist, true);   // 获取最大的距离差值
				// 双指旋转角度大于阈值（边界差值）
				if ((angleMaxDiff > MIN_ROTATE_ANGLE) || (distMaxDiff > MIN_SCALE_DIFF)) {
					mTouchState = STATE_SCALE;  // 触摸状态为多指旋转
				}
			}
			// Log.i("luoph", "decodeHistoryState mTouchState = " +
			// mTouchState);
			MutilPointInfo startPoint = mHistory.get(0);  // 获取起点
			mStartAngle = getFingerAngle(startPoint);  // 获取起点的角度
			mStartDistance = getFingerDiff(startPoint); // 获取起点的距离
			if (DEBUG) {
				Log.d(TAG, "decodeHistoryState = " + STATE_NAMES[mTouchState]);
			}
		}
		mStartDecodeMultiTouch = true;  // 改变状态
	}

	/**
	 * <br>
	 * 功能简述:通知多点触摸状态 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param dx
	 * @param dy
	 * @return
	 */
	private boolean notifyMultiTouchState(float dx, float dy) {
		if (!mPrevPoint.isMultiTouch() || !mCurrPoint.isMultiTouch()) {
			if (mTouchMoved && mTouchState == STATE_TOUCH) {
				mTouchState = STATE_SCROLL;  // 滚动状态
			}
		} else {
			final float[] prevXs = mPrevPoint.getXs();
			final float[] prevYs = mPrevPoint.getYs();
			final float[] currXs = mCurrPoint.getXs();
			final float[] currYs = mCurrPoint.getYs();

			float p0dx = currXs[0] - prevXs[0];
			float p0dy = currYs[0] - prevYs[0];

			float p1dx = currXs[1] - prevXs[1];
			float p1dy = currYs[1] - prevYs[1];

			// final boolean oppsite = isOpposite(p0dx, p0dy, p1dx, p1dy);

			// 历史数据已经判断出手势
			if (mStartDecodeMultiTouch) {
				final boolean swipe = isSwipe(p0dx, p0dy, p1dx, p1dy);

				int newState = mTouchState;
				if (mTouchState == STATE_SCALE) {
					if (swipe) {   // 缩放到多指滑动状态的改变
						newState = STATE_SWIPE;
					}
				} else if (mTouchState == STATE_SWIPE) {
					if (!swipe) {   // 多指滑动到缩放状态的改变
						final float currentAngle = getFingerAngle(mCurrPoint);
						final float currentDist = getFingerDiff(mCurrPoint);
						float angleDiff = Math.abs(currentAngle - mStartAngle);
						float distDiff = Math.abs(currentDist - mStartDistance);

						// 从多指滑动向缩放手势的转变，设定一定较大的阈值
						if (angleDiff > SWIPE_TO_SCALE_ANGLE_THRESHOLD    // 由多指滑动向缩放手势变化的阈值（临界值）
								|| distDiff > SWIPE_TO_SCALE_DIFF_THRESHOLD) {
							newState = STATE_SCALE;
							mStartAngle = currentAngle;
							mStartDistance = currentDist;
						}
					}
				}

				if (newState != mTouchState) {
					if (DEBUG) {
						Log.e(TAG, "state change from " + STATE_NAMES[mTouchState] + " to "
								+ STATE_NAMES[newState]);
					}
					mTouchState = newState;
				}
			}
		}

		if (DEBUG) {
			Log.i(TAG, "state5 = " + STATE_NAMES[mTouchState]);
		}

		if (mListener != null) {
			switch (mTouchState) {
				case STATE_SCROLL :  // 滚动状态
					final VelocityTracker velocityTracker = mVelocityTracker;   // 获取当前的速率监听
					velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);  // 计算当前的速率
																							// ,
																							// mMaximumFlingVelocity此时已经经过ViewConfiguration获取系统值
					final float velocityY = velocityTracker.getYVelocity(); // 获取Y方向的速率
					final float velocityX = velocityTracker.getYVelocity(); // 获取X方向的速率

					if (Math.abs(velocityX) > SWIPE_VELOCITY   // 如果X方向或者Y方向上的速率大于
																// 阈值（上下滑屏识别为手势的速度阈值）
							|| Math.abs(velocityY) > SWIPE_VELOCITY) {
						float xOffset = mCurrPoint.getX() - mPrevPoint.getX();
						float yOffset = mCurrPoint.getY() - mPrevPoint.getY();
						return mListener.onSwipe(mCurrPoint, xOffset, yOffset,
								getMoveDirection(xOffset, yOffset));
					}
					break;

				case STATE_SCALE :   // 缩放状态
					final float degreeDelta = getFingerAngle(mCurrPoint) - mStartAngle;
					final float scaleDelta = getFingerDiff(mCurrPoint) / mStartDistance;
					// Log.i("luoph", "onScale " + scaleDelta + ", degree = " +
					// degreeDelta);
					return mListener.onScale(mCurrPoint, scaleDelta, degreeDelta);

				case STATE_SWIPE :   // 多指滑动状态
					float xOffset = mCurrPoint.getX() - mPrevPoint.getX();
					float yOffset = mCurrPoint.getY() - mPrevPoint.getY();
					// Log.i("luoph", "onSwipe point count = " +
					// mCurrPoint.getPointCount());
					return mListener.onSwipe(mCurrPoint, xOffset, yOffset,
							getMoveDirection(xOffset, yOffset));

				default :
					break;
			}
		}
		return false;
	}

	/**
	 * <br>
	 * 功能简述:获取移动的方向 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param dx
	 * @param dy
	 * @return
	 */
	private int getMoveDirection(float dx, float dy) {
		if (Math.abs(dy) > Math.abs(dx)) {
			return dy > 0 ? OnMultiTouchListener.DIRECTION_DOWN : OnMultiTouchListener.DIRECTION_UP;
		} else {
			return dx > 0
					? OnMultiTouchListener.DIRECTION_RIGHT
					: OnMultiTouchListener.DIRECTION_LEFT;
		}
	}

	/**
	 * 获取一个数组中差值最大的2个数的比值
	 * 
	 * @param array
	 * @param max
	 *            true时返回 max / min, 否则返回 min / max;
	 * @return
	 */
	public static float getRatio(float[] array, boolean maxRatio) {
		int length = array.length;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;

		for (int i = 0; i < length; i++) {
			if (array[i] > max) {
				max = array[i];  // 获取最大值
			}

			if (array[i] < min) {
				min = array[i];  // 获取最小值
			}
		}

		if (maxRatio) {  // 如果需要获取max / min
			if (min != 0f) {
				return max / min;
			}
			return Float.MAX_VALUE;
		} else {
			if (max != 0f) {
				return min / max;
			}
			return Float.MIN_VALUE;
		}
	}

	/**
	 * 获取一个数组中差值最大的2个数的差值
	 * 
	 * @param array
	 * @param max
	 *            true时返回 max - min, 否则返回 min - max;
	 * @return
	 */
	public static float getDiff(float[] array, boolean maxDiff) {
		int length = array.length;
		if (length > 0) {
			float min = array[0];
			float max = array[0];
			for (int i = 1; i < length; i++) {
				if (array[i] > max) {
					max = array[i];
				}

				if (array[i] < min) {
					min = array[i];
				}
			}
			return maxDiff ? max - min : min - max;
		}
		return 0;
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param array
	 * @param prefix
	 * @param tag
	 */
	public void dumpArray(float[] array, String prefix, String tag) {
		int length = array.length;
		String msg = prefix + " ";
		for (int i = 0; i < length; i++) {
			msg += array[i];
			if (i != length - 1) {
				msg += ", ";
			}
		}

		Log.i(tag, msg);
	}

	/**
	 * <br>
	 * 功能简述:获取当前的点 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public MutilPointInfo getCurPointInfo() {
		return mCurrPoint;
	}

	public float getFingerDiff(MutilPointInfo p) {
		return Math.max(MIN_MULTITOUCH_SEPARATION * 0.71f, p.getFingerDistance());
	}

	public float getFingerAngle(MutilPointInfo p) {
		return MutilPointInfo.radian2Degree(p.getFingerAngle());
	}

	/**
	 * MotionEvent has no getRawX(int) method; simulate it pending future API
	 * approval.
	 */
	public static float getRawX(MotionEvent event, int pointerIndex) {
		float offset = event.getX() - event.getRawX();
		return event.getX(pointerIndex) + offset;
	}

	/**
	 * MotionEvent has no getRawY(int) method; simulate it pending future API
	 * approval.
	 */
	public static float getRawY(MotionEvent event, int pointerIndex) {
		float offset = event.getY() - event.getRawY();
		return event.getY(pointerIndex) + offset;
	}

	/**
	 * 输出MotionEvent信息
	 * 
	 * @param event
	 * @param tag
	 */
	public void dumpEvent(MotionEvent event, String tag) {
		int pointCount = event.getPointerCount();
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;

		sb.append("event ").append(ACTION_NAMES[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");

		int hcount = event.getHistorySize();
		for (int i = 0; i < hcount; i++) {
			sb.append("$").append(i);
			sb.append("(h ").append(i);
			sb.append(")=").append((int) event.getHistoricalX(i));
			sb.append(",").append((int) event.getHistoricalY(i));
			sb.append("; ");
		}

		for (int i = 0; i < pointCount; i++) {
			sb.append("#").append(i);
			int pid = event.getPointerId(i);
			int index = event.findPointerIndex(pid);
			sb.append("(pid ").append(pid).append(" index:").append(index);
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount()) {
				sb.append(";");
			}
		}

		sb.append("]");
		Log.d(tag, sb.toString());
	}

	// private boolean isOpposite(float p0dx, float p0dy, float p1dx, float
	// p1dy) {
	// final boolean xMoved = Math.abs(p0dx) > mTouchSlop
	// || Math.abs(p1dx) > mTouchSlop;
	// final boolean yMoved = Math.abs(p0dy) > mTouchSlop
	// || Math.abs(p1dy) > mTouchSlop;
	//
	// final boolean xReverse = xMoved ? (p0dx >= 0 && p1dx <= 0)
	// || (p0dx <= 0 && p1dx >= 0) : !xMoved;
	// final boolean yReverse = yMoved ? (p0dy >= 0 && p1dy <= 0)
	// || (p0dy <= 0 && p1dy >= 0) : !yMoved;
	//
	// boolean result = xReverse && yReverse;
	// // if (!result)
	// // {
	// // Log.i(TAG, "same direction!");
	// // }
	// return result;
	// }

	/**
	 * <br>
	 * 功能简述:是否为滑动 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param p0dx
	 * @param p0dy
	 * @param p1dx
	 * @param p1dy
	 * @return
	 */
	private boolean isSwipe(float p0dx, float p0dy, float p1dx, float p1dy) {
		boolean swipe = false; // 设置滑动为false
		boolean xDiffMore = Math.abs(p0dx) > Math.abs(p0dy) && Math.abs(p1dx) >= Math.abs(p1dy);
		boolean xDirSame = Math.signum(p0dx) == Math.signum(p1dx);

		boolean yDiffMore = Math.abs(p0dx) < Math.abs(p0dy) && Math.abs(p1dx) <= Math.abs(p1dy);
		boolean yDirSame = Math.signum(p0dy) == Math.signum(p1dy);
		/**
		 * 某方向上的多指同向滑动需满足以下两个条件： 1)此方向上移动的距离（绝对值）较大 2)此方向上两个点的移动方向相同
		 */
		if ((xDiffMore && xDirSame) || (yDiffMore && yDirSame)) {
			swipe = true;
		}
		// Log.i("luoph", "isSwipe " + p0dx + ", " + p0dy + ", " + p1dx + ", " +
		// p1dy + " = " + swipe);
		return swipe;
	}

	// private class GestureHandler extends Handler {
	// GestureHandler() {
	// super();
	// }
	//
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	//
	// // TODO 屏蔽长按事件
	// case LONG_PRESS:
	// dispatchLongPress();
	// break;
	//
	// // TODO 屏蔽单击事件
	// case TAP:
	// if (mListener != null && !mStillDown && mAlwaysInTapRegion) {
	// mListener.onSingleTap(mCurrDownPoint);
	// }
	// break;
	//
	// default:
	// throw new RuntimeException("Unknown message " + msg); // never
	// }
	// }
	// }

	/**
	 * <br>
	 * 功能简述:获取点的索引 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param ev
	 * @return
	 */
	private static int getPointerIndex(MotionEvent ev) {
		try {
			return (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		} catch (Exception e) {
			return (ev.getAction() & 0xff00) >> 8;
		}
	}

	/**
	 * <br>
	 * 功能简述:添加点到历史点集合 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param p
	 */
	private void addToHistory(MutilPointInfo p) {
		MutilPointInfo info = new MutilPointInfo(p);
		mLastHistoryPoint = info;
		mHistory.add(info);
		if (mHistory.size() >= SAMPLE_SIZE) {
			decodeHistoryState();
		}
		// Log.i("luoph", "add to history " + mLastHistoryPoint + " pcount = " +
		// p.getPointCount());
	}

	/**
	 * <br>
	 * 功能简述:被认为可添加进历史点集 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param current
	 *            当前点击的点
	 * @param preview
	 *            上一次点击的点
	 * @return
	 */
	private boolean isConsideredAddToHistory(MutilPointInfo current, MutilPointInfo preview) {
		if (mStartDecodeMultiTouch || mHistory.size() >= SAMPLE_SIZE) {  // 多点触摸样本点大于3为满
			if (DEBUG) {
				Log.d(TAG, "history record is full");
			}
			return false;
		}

		if (current == null || !current.isMultiTouch() || preview == null
				|| !preview.isMultiTouch()) {  // 当前和上一点均不是多点触摸
			return false;
		}

		final float[] prevXs = preview.getXs();  // 前一个点的x坐标
		final float[] prevYs = preview.getYs();  // 前一个点的Y坐标
		final float[] currXs = current.getXs();  // 当前点的x坐标
		final float[] currYs = current.getYs();  // 当前点的Y坐标

		float p0dx = Math.abs(currXs[0] - prevXs[0]);
		float p0dy = Math.abs(currYs[0] - prevYs[0]);

		float p1dx = Math.abs(currXs[1] - prevXs[1]);
		float p1dy = Math.abs(currYs[1] - prevYs[1]);

		if ((p0dx > mTouchSlop || p0dy > mTouchSlop) || (p1dx > mTouchSlop || p1dy > mTouchSlop)) {
			// Log.i("luoph", "isConsideredAddToHistory " + p0dx + ", " + p0dy +
			// ", " + p1dx + ", " + p1dy);
			return true;
		}
		return false;
	}

	/**
	 * 被认为是双击手势
	 * 
	 * @param firstDown
	 *            第一次下
	 * @param firstUp
	 *            第一次上
	 * @param secondDown
	 *            第二次下
	 * @return
	 */
	private boolean isConsideredDoubleTap(MutilPointInfo firstDown, MutilPointInfo firstUp,
			MutilPointInfo secondDown) {
		if (!firstDown.isUpdate() || !firstUp.isUpdate()) {
			return false;
		}

		// 上一次点击的up到这一次点击的down时间间隔大于DOUBLE_TAP_TIMEOUT则不算是双击
		if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
			return false;
		}

		// 根据距离判断是否 在双击的范围之内
		int deltaX = (int) (firstDown.getX() - secondDown.getX());
		int deltaY = (int) (firstDown.getY() - secondDown.getY());
		return deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare;
	}

	/**
	 * 判断是否超出双击范围
	 * 
	 * @return
	 */
	private boolean isOutOfDoubleTapRange(int pointCount, float x, float y) {
		final int deltaX = (int) (x - mCurrDownPoint.getX());
		final int deltaY = (int) (y - mCurrDownPoint.getY());
		int distance = (deltaX * deltaX) + (deltaY * deltaY);
		if (pointCount >= 2 || distance > mDoubleTapSlopSquare) { // 点的个数大于2或者距离大于双击范围
			return true;
		}
		return false;
	}

	// TODO 屏蔽长按事件
	// private void dispatchLongPress() {
	// mHandler.removeMessages(TAP);
	// mInLongPress = true;
	// if (mListener != null && mStillDown) {
	// mListener.onLongPress(mCurrDownPoint);
	// }
	// }

	private void cancel() {
		// mHandler.removeMessages(LONG_PRESS);
		// mHandler.removeMessages(TAP);
		mIsDoubleTapping = false;
		// mStillDown = false;
		mInLongPress = false;
	}
}
