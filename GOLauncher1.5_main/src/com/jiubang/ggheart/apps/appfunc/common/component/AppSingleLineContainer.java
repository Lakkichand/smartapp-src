package com.jiubang.ggheart.apps.appfunc.common.component;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import com.gau.go.launcherex.R;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncDockContent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncImageButton;
import com.jiubang.ggheart.apps.appfunc.timer.ITask;
import com.jiubang.ggheart.apps.appfunc.timer.Scheduler;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.GBaseAdapter;
/**
 * 
 * <br>类描述:用于放置单行子组件的容器，超过一屏时可以左右滑动
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-25]
 */
public abstract class AppSingleLineContainer extends AppFuncDockContent
		implements
			ScreenScrollerListener {
	private static final int GO_TO_OTHER_SCREEN_ICON_DELAY = 500;
	private static final int DEFAULT_MAX_ITEM_COUNT = 5;
	/**
	 * 垂直空白区域高度
	 */
	public static final int PADDING_V = 1;
	/**
	 * 水平空白区域宽度
	 */
	public static final int PADDING_H = 34;

	/**
	 * 竖屏模式指示器图标
	 */
	protected Drawable mNextBGV = null;
	protected Drawable mPreBGV = null;

	protected Drawable mNextBGH = null;
	protected Drawable mPreBGH = null;

	/**
	 * 是否被图标触碰
	 */
	protected boolean mIsTouched;

	protected GBaseAdapter mAdapter = null;
	/**
	 * 列数
	 */
	protected int mColumnNumber = 1;
	/**
	 * 屏幕数
	 */
	protected int mScreen = 1;
	/**
	 * 下一屏指示器按钮
	 */
	protected AppFuncImageButton mNextButton = null;
	/**
	 * 上一屏指示器按钮
	 */
	protected AppFuncImageButton mPreButton = null;

	protected Scheduler mScheduler = null;

	protected ScreenScroller mScroller = null;

	/**
	 * X轴方向偏移量
	 */
	protected volatile int mOffsetX = 0;
	/**
	 * Y轴方向偏移量
	 */
	protected volatile int mOffsetY = 0;
	/**
	 * 横滑下，当前显示屏
	 */
	protected volatile int mCurScreen = 0;
	/**
	 * 是否重绘
	 */
	protected boolean mInvalidated = false;
	/**
	 * 是否显示下一屏指示器按钮
	 */
	protected boolean mNeedIndicatorPre = false;
	/**
	 * 是否显示上一屏指示器按钮
	 */
	protected boolean mNeedIndicatorNext = false;
	/**
	 * 响应触摸事件的子组件
	 */
	protected XComponent mMotionTarget = null;

	/**
	 * 最近一次触摸事件时的坐标
	 */
	protected int mLastTouchX = 0;
	protected int mLastTouchY = 0;

	/**
	 * 手指按下时的位置
	 */
	protected float mTouchDownY = 0;
	protected float mTouchDownX = 0;

	/**
	 * 是否移动过
	 */
	protected boolean mIsMoved = false;

	/**
	 * 手指在屏幕上移动的水平距离达到这个值则认为产生了滑屏事件
	 */
	protected int mMoveDist = 12;
	/**
	 * 图标之间的默认间距
	 */
	protected int mSpaceWidth = 0;
	/**
	 * 图标的默认高宽
	 */
	protected int mIconWidth = 0;
	protected int mIconHeight = 0;

	/**
	 * 是否把触摸事件传递给指示器处理
	 */
	protected boolean mDisPatchToIndicator = false;

	private int mItemSize;

	private int mMaxColumns = DEFAULT_MAX_ITEM_COUNT;

	private Rect mEditBgRect = new Rect(); // 绿色发亮区背景绘制区域
	/**
	 * 滚屏发光指示图片默认高宽
	 */
	private int mSideBgWidth = 231;
	private int mSideBgHeight = 231;

	private SwitchScreenTask mSwitchScreenTask = null;
	protected boolean mCustomItemSize;
	
	protected int mIndicatorSize = 34;

	/**
	 * 高度由内部自适应，外部传入参数无效
	 * 
	 * @param activity
	 * @param tickCount
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public AppSingleLineContainer(Activity activity, int tickCount, int x, int y, int width,
			int height, int type) {
		super(activity, tickCount, x, y, width, height, type);
		mActivity = activity;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mSideBgWidth = mUtils.getStandardSize(231);
		mSideBgHeight = mUtils.getStandardSize(231);
		mMoveDist = mUtils.getScaledSize(mMoveDist);

		//		mChildSize = mUtils.getStandardSize(ICON_WIDTH);

		mTotalStep = 10;
		mCurrentStep = 0;
		initAdapter(mActivity);
		mScheduler = Scheduler.getInstance();

		loadResource(null);

		mScroller = new ScreenScroller(activity, this);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setMaxOvershootPercent(10);
		mScroller.setPadding(0);
		mScroller.setInterpolator(new DecelerateInterpolator(2.5f));
		mScroller.setDuration(650);
	}

	@Override
	public boolean onTouch(MotionEvent event) {

		//先判断是否点击在了指示器按钮上
		if (MotionEvent.ACTION_DOWN == event.getAction()) {
			mDisPatchToIndicator = false;
			mMotionTarget = null;
			final int ex = (int) event.getX() - getAbsX();
			final int ey = (int) event.getY() - getAbsY();

			if (mPreButton != null && mPreButton.XYInRange(ex, ey)) {
				mMotionTarget = mPreButton;
				mDisPatchToIndicator = true;
			} else if (mNextButton != null && mNextButton.XYInRange(ex, ey)) {
				mMotionTarget = mNextButton;
				mDisPatchToIndicator = true;
			}
		}

		if (mDisPatchToIndicator) {
			final int ex = (int) event.getX() - getAbsX();
			final int ey = (int) event.getY() - getAbsY();
			if (mMotionTarget != null) {
				event.setLocation(ex, ey);
				mMotionTarget.onTouch(event);
			}
			return true;
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN : {
				onTouchDown(event);
				break;
			}
			case MotionEvent.ACTION_MOVE : {
				onTouchMove(event);
				break;
			}
			case MotionEvent.ACTION_UP : {
				onTouchUp(event);
				break;
			}
			case MotionEvent.ACTION_CANCEL : {
				onTouchCancel(event);
				break;
			}
		}

		return true;
	}

	protected boolean onTouchDown(MotionEvent event) {
		// 处理滚动器事件
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		mScroller.onTouchEvent(event, MotionEvent.ACTION_DOWN);

		if (mMotionTarget != null) {
			mMotionTarget = null;
		}

		mIsMoved = false;

		boolean ret = false;

		final float ex = event.getX();
		final float ey = event.getY();

		final float xf = ex - getAbsX();
		final float yf = ey - getAbsY();
		final float scrolledXFloat = xf + (-mOffsetX);
		final float scrolledYFloat = yf + (-mOffsetY);

		// 处理划屏操作
		mLastTouchX = (int) xf;
		mLastTouchY = (int) yf;
		mTouchDownX = xf;
		mTouchDownY = yf;

		final int scrolledXInt = (int) scrolledXFloat;
		final int scrolledYInt = (int) scrolledYFloat;
		final ArrayList<XComponent> children = mDrawComponent;
		final int count = children.size();

		for (int i = count - 1; i >= 0; i--) {
			final XComponent child = children.get(i);
			// 如果事件位置包含在子组件中
			if (scrolledXInt > child.mX && scrolledXInt < child.mX + child.getWidth()
					&& scrolledYInt > child.mY && scrolledYInt < child.mY + child.getHeight()) {
				final float xc = scrolledXFloat - child.mX;
				final float yc = scrolledYFloat - child.mY;
				event.setLocation(xc, yc);
				ret = child.onTouch(event);
				if (ret) {
					mMotionTarget = child;
				}
			}
		}

		return ret;
	}

	protected boolean onTouchMove(MotionEvent event) {

		final float ex = event.getX();
		final float ey = event.getY();

		final float xf = ex - getAbsX();
		final float yf = ey - getAbsY();

		float deltaY = mTouchDownY - yf;
		float deltaX = mTouchDownX - xf;
		deltaX = Math.abs(deltaX);
		deltaY = Math.abs(deltaY);

		// 如果还没有产生过移动事件
		if (!mIsMoved) {
			if (mUtils.isVertical()) {
				if (deltaX > mMoveDist) {
					mIsMoved = true;
				}
			} else {
				if (deltaY > mMoveDist) {
					mIsMoved = true;
				}
			}

		}

		boolean ret = false;

		if (mIsMoved) {
			if (mMotionTarget != null) {
				event.setAction(MotionEvent.ACTION_CANCEL);
				mMotionTarget.onTouch(event);
				mMotionTarget = null;
				event.setAction(MotionEvent.ACTION_MOVE);
			}
			ret = mScroller.onTouchEvent(event, MotionEvent.ACTION_MOVE);
		}

		return ret;
	}

	protected boolean onTouchUp(MotionEvent event) {
		boolean ret = false;

		final float ex = event.getX();
		final float ey = event.getY();

		final float scrolledXFloat = ex + (-mOffsetX);
		final float scrolledYFloat = ey + (-mOffsetY);

		final XComponent target = mMotionTarget;
		mMotionTarget = null;

		// 既没有移动，也有接收事件的子组件也有，则应把事件交给子组件处理
		if (!mIsMoved && target != null) {
			final float xc = scrolledXFloat;
			final float yc = scrolledYFloat;
			event.setLocation(xc, yc);
			ret = target.onTouch(event);
			event.setLocation(ex, ey);
		}

		return mScroller.onTouchEvent(event, MotionEvent.ACTION_UP) || ret;

	}

	protected boolean onTouchCancel(MotionEvent event) {
		boolean ret = false;

		final float ex = event.getX();
		final float ey = event.getY();

		final float xf = ex - getAbsX();
		final float yf = ey - getAbsY();
		final float scrolledXFloat = xf + (-mOffsetX);
		final float scrolledYFloat = yf + (-mOffsetY);

		final XComponent target = mMotionTarget;
		mMotionTarget = null;

		// 既没有移动，也有接收事件的子组件也有，则应把事件交给子组件处理
		if (!mIsMoved && target != null) {
			final float xc = scrolledXFloat - target.mX;
			final float yc = scrolledYFloat - target.mY;
			event.setLocation(xc, yc);
			ret = target.onTouch(event);
			event.setLocation(ex, ey);
		}

		return mScroller.onTouchEvent(event, MotionEvent.ACTION_CANCEL) || ret;
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		mScroller.invalidateScroll();

		int save = canvas.save();
		canvas.translate(mOffsetX, mOffsetY);
		super.drawCurrentFrame(canvas);
		canvas.restoreToCount(save);

		if (mNeedIndicatorNext && mNextButton != null) {
			mNextButton.paintCurrentFrame(canvas, mNextButton.mX, mNextButton.mY);
		}

		if (mNeedIndicatorPre && mPreButton != null) {
			mPreButton.paintCurrentFrame(canvas, mPreButton.mX, mPreButton.mY);
		}
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		super.layout(left, top, right, bottom);
		mAdapter.loadApp();
		int iconCount = mAdapter.getCount();
		if (iconCount <= 0) {
			return;
		}

		if (iconCount >= mMaxColumns) {
			mColumnNumber = mMaxColumns;
		} else {
			mColumnNumber = iconCount;
		}

		removeAllComponent();

		mScreen = (iconCount - 1) / mColumnNumber + 1;

		if (mUtils.isVertical()) {

			mPaddingTop = mUtils.getStandardSize(PADDING_V);
			mPaddingBottom = mUtils.getStandardSize(PADDING_V);
			mPaddingLeft = mUtils.getStandardSize(PADDING_H);
			mPaddingRight = mUtils.getStandardSize(PADDING_H);

			if (!mCustomItemSize) {
				mItemSize = (int) ((mWidth - mPaddingLeft - mPaddingRight) / mColumnNumber);
			}

			mIconWidth = mItemSize;
			mIconHeight = mHeight - mPaddingBottom - mPaddingTop;
			// 如果是每屏4,5个则特别排列
			if (mColumnNumber >= 4) {
				mSpaceWidth = (mWidth - mPaddingLeft - mPaddingRight - mColumnNumber * mIconWidth)
						/ (mColumnNumber - 1);
				for (int i = 0; i < mScreen; i++) {
					for (int j = 0; j < mColumnNumber; j++) {

						int index = i * mColumnNumber + j;
						if (index < iconCount) {
							int x = i * mWidth + mPaddingLeft + j * mIconWidth + j * mSpaceWidth;
							int y = mPaddingTop;

							XComponent icon = (XComponent) mAdapter.getComponent(index, x, y,
									mIconWidth, mIconHeight, null, this);
							if (icon != null) {
								addComponent(icon);
							}
						} else {
							break;
						}
					}
				}

			} else {
				mSpaceWidth = (mWidth - mPaddingLeft - mPaddingRight) / mColumnNumber;
				for (int i = 0; i < mScreen; i++) {
					for (int j = 0; j < mColumnNumber; j++) {
						int index = i * mColumnNumber + j;
						if (index < iconCount) {
							int x = i * mWidth + mPaddingLeft + j * mSpaceWidth
									+ ((mSpaceWidth - mIconWidth) / 2);
							int y = mPaddingTop;

							XComponent icon = (XComponent) mAdapter.getComponent(index, x, y,
									mIconWidth, mIconHeight, null, this);
							if (icon != null) {
								addComponent(icon);
							}
						} else {
							break;
						}
					}
				}
			}
			mScroller.setOrientation(ScreenScroller.HORIZONTAL);
		} else {

			mPaddingTop = mUtils.getStandardSize(PADDING_H);
			mPaddingBottom = mUtils.getStandardSize(PADDING_H);
			mPaddingLeft = mUtils.getStandardSize(PADDING_V);
			mPaddingRight = mUtils.getStandardSize(PADDING_V);

			if (!mCustomItemSize) {
				mItemSize = (int) ((mHeight - mPaddingTop - mPaddingBottom) / mMaxColumns);
			}

			mIconWidth = mWidth - mPaddingLeft - mPaddingRight;
			mIconHeight = mItemSize;
			if (mColumnNumber >= 4) {
				mSpaceWidth = (mHeight - mPaddingTop - mPaddingBottom - mColumnNumber * mIconHeight)
						/ (mColumnNumber - 1);
				for (int i = 0; i < mScreen; i++) {
					for (int j = 0; j < mColumnNumber; j++) {

						int index = i * mColumnNumber + j;
						if (index < iconCount) {
							int x = mPaddingLeft;
							int y = i * mHeight + mPaddingTop + j * mIconHeight + j * mSpaceWidth;

							XComponent icon = (XComponent) mAdapter.getComponent(index, x, y,
									mIconWidth, mIconHeight, null, this);
							if (icon != null) {
								addComponent(icon);
							}
						} else {
							break;
						}
					}
				}

			} else {
				mSpaceWidth = (mHeight - mPaddingTop - mPaddingBottom) / mColumnNumber;
				for (int i = 0; i < mScreen; i++) {
					for (int j = 0; j < mColumnNumber; j++) {

						int index = i * mColumnNumber + j;
						if (index < iconCount) {
							int x = mPaddingLeft;
							int y = i * mHeight + mPaddingTop + j * mSpaceWidth
									+ ((mSpaceWidth - mIconHeight) / 2);

							XComponent icon = (XComponent) mAdapter.getComponent(index, x, y,
									mIconWidth, mIconHeight, null, this);
							if (icon != null) {
								addComponent(icon);
							}
						}
					}
				}

			}
			mScroller.setOrientation(ScreenScroller.VERTICAL);
		}
		mScroller.setScreenCount(mScreen);
		mScroller.setScreenSize(mWidth, mHeight);
		if (mCurScreen >= mScreen) {
			mCurScreen = mScreen - 1;
		}
		mScroller.setCurrentScreen(mCurScreen);
		// 布局指示器按钮
		layoutIndicator();
	}

	protected void layoutIndicator() {
		int buttonWidth = 0;
		int buttonhHeight = 0;
		if (mUtils.isVertical()) {
			buttonWidth = mUtils.getStandardSize(mIndicatorSize);
			buttonhHeight = mHeight;
		} else {
			buttonWidth = mWidth;
			buttonhHeight = mUtils.getStandardSize(mIndicatorSize);
		}

		updateIndicatorValue();
		if (mNextButton == null) {
			mNextButton = new AppFuncImageButton(mActivity, 1, 0, 0, 0, 0);
			mNextButton.setClickListener(new AppFuncImageButton.OnClickListener() {

				@Override
				public void onClick(XComponent view) {
					if (mScroller != null && mScroller.isFinished()) {
						if (mCurScreen < mScreen) {
							mScroller.gotoScreen(mCurScreen + 1, 350, true);
						}
					}
				}
			});
		}
		if (mPreButton == null) {
			mPreButton = new AppFuncImageButton(mActivity, 1, 0, 0, 0, 0);
			mPreButton.setClickListener(new AppFuncImageButton.OnClickListener() {

				@Override
				public void onClick(XComponent view) {
					if (mScroller != null && mScroller.isFinished()) {
						if (mCurScreen > 0) {
							mScroller.gotoScreen(mCurScreen - 1, 350, true);
						}
					}
				}
			});
		}

		if (mUtils.isVertical()) {
			int x = mWidth - buttonWidth;
			int y = (mHeight - buttonhHeight) / 2;
			mNextButton.setIcon(mNextBGV);
			mNextButton.layout(x, y, x + buttonWidth, y + buttonhHeight);
		} else {
			int x = (mWidth - buttonWidth) / 2;
			int y = mHeight - buttonhHeight;
			mNextButton.setIcon(mNextBGH);
			mNextButton.layout(x, y, x + buttonWidth, y + buttonhHeight);
		}

		if (mUtils.isVertical()) {
			int x = 0;
			int y = (mHeight - buttonhHeight) / 2;
			mPreButton.setIcon(mPreBGV);
			mPreButton.layout(x, y, x + buttonWidth, y + buttonhHeight);
		} else {
			int x = (mWidth - buttonWidth) / 2;
			int y = 0;
			mPreButton.setIcon(mPreBGH);
			mPreButton.layout(x, y, x + buttonWidth, y + buttonhHeight);
		}

	}

	protected void updateIndicatorValue() {
		if (mScreen > 1) {
			if (mCurScreen == 0) {
				mNeedIndicatorNext = true;
				mNeedIndicatorPre = false;
			} else if (mCurScreen == mScreen - 1) {
				mNeedIndicatorNext = false;
				mNeedIndicatorPre = true;
			} else {
				mNeedIndicatorNext = true;
				mNeedIndicatorPre = true;
			}
		} else {
			mNeedIndicatorPre = false;
			mNeedIndicatorNext = false;
		}
	}

	@Override
	public void setSelected(boolean on) {
		mIsTouched = on;
	}

	@Override
	public void loadResource(String packageName) {
		mNextBGV = mActivity.getResources().getDrawable(R.drawable.appfunc_nextpage_v);
		mNextBGH = mActivity.getResources().getDrawable(R.drawable.appfunc_nextpage_h);
		mPreBGV = mActivity.getResources().getDrawable(R.drawable.appfunc_prepage_v);
		mPreBGH = mActivity.getResources().getDrawable(R.drawable.appfunc_prepage_h);
	}

	@Override
	public void resetResource() {
	}

	/**
	 * ----------------------------------分屏滚动监听器接口实现方法开始------------------------
	 * -----------------------------
	 */
	@Override
	public ScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public int getScrollX() {
		return -mOffsetX;
	}

	@Override
	public int getScrollY() {
		return -mOffsetY;
	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	protected boolean animate() {
		boolean ret = false;
		if (mInvalidated) {
			mInvalidated = false;
			ret = true;
			mScroller.computeScrollOffset();
		}
		ret = super.animate() || ret;

		return ret;
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurScreen = newScreen;
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		updateIndicatorValue();
	}

	@Override
	public void onScrollStart() {
		postInvalidate();

	}

	@Override
	public void invalidate() {
		mInvalidated = true;
	}

	public void postInvalidate() {
		mInvalidated = true;

	}

	@Override
	public void scrollBy(int x, int y) {
		if (x != 0 || y != 0) {
			mOffsetX -= x;
			mOffsetY -= y;
			postInvalidate();
		}
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		mScroller = scroller;
	}

	/**
	 * ----------------------------------分屏滚动监听器接口实现方法结束------------------------
	 * -----------------------------
	 */

	/**
	 * <br>功能简述:初始化数据适配器
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public abstract void initAdapter(Activity activity);

	/**
	 * <br>功能简述:设置容器包含的每个子组件的宽度
	 * ，默认宽度为68个像素，如果需要别的大小需要另外设置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param width 像素值
	 */
	public void setChildWidth(int width) {
		mItemSize = width;
	}
	/**
	 * <br>功能简述:设置最多列数，当子组件个数超过这个值时会多屏显示，每屏子组件个数为col
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param col
	 */
	public void setMaxColumnNumber(int col) {
		mMaxColumns = col;
	}

	public void setItemSize(int size) {
		mCustomItemSize = true;
		mItemSize = size;
	}
	
	public int getItemSize() {
		return mItemSize;
	}

	/**
	 * 启动切换页面任务
	 * @param x
	 * @param y
	 * @return true 启动任务；false 不启动任务
	 */
	protected boolean startSwitchScreenTask(final int x, final int y) {
		int nextOrPre = 0;
		if (mUtils.isVertical()) {
			if (x > mWidth - mPaddingRight && mNeedIndicatorNext) {
				nextOrPre = 0;
				checkNextOrPre(nextOrPre);
				return true;
			} else if (x < mPaddingLeft && mNeedIndicatorPre) {
				nextOrPre = 1;
				checkNextOrPre(nextOrPre);
				return true;
			}
		} else {
			if (y > mHeight - mPaddingBottom && mNeedIndicatorNext) {
				nextOrPre = 0;
				checkNextOrPre(nextOrPre);
				return true;
			} else if (y < mPaddingTop && mNeedIndicatorPre) {
				nextOrPre = 1;
				checkNextOrPre(nextOrPre);
				return true;
			}
		}
		// 发消息通知allapptabbasiccontent不绘制高亮图
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				AppFuncConstants.DRAW_SIDEBG_NO, null);
		// mmIsDrawEditBg = false;
		return false;
	}

	private void checkNextOrPre(int nextOrPre) {
		// mmIsDrawEditBg = true;

		if (nextOrPre == 0) {
			if (mUtils.isVertical()) {

				int centX = getAbsX() + mWidth - mPaddingRight / 2;
				int centY = getAbsY() + mHeight / 2;

				int left = centX - mSideBgWidth / 2;
				int top = centY - mSideBgHeight / 2;

				mEditBgRect.set(left, top, left + mSideBgWidth, top + mSideBgHeight);
			} else {
				int centX = getAbsX() + mWidth / 2;
				int centY = getAbsY() + mHeight - mPaddingBottom / 2;

				int left = centX - mSideBgWidth / 2;
				int top = centY - mSideBgHeight / 2;

				mEditBgRect.set(left, top, left + mSideBgWidth, top + mSideBgHeight);
			}
		} else if (nextOrPre == 1) {
			if (mUtils.isVertical()) {
				int centX = getAbsX() + mPaddingLeft / 2;
				int centY = getAbsY() + mHeight / 2;

				int left = centX - mSideBgWidth / 2;
				int top = centY - mSideBgHeight / 2;

				mEditBgRect.set(left, top, left + mSideBgWidth, top + mSideBgHeight);
			} else {
				int centX = getAbsX() + mWidth / 2;
				int centY = getAbsY() + mPaddingTop / 2;

				int left = centX - mSideBgWidth / 2;
				int top = centY - mSideBgHeight / 2;

				mEditBgRect.set(left, top, left + mSideBgWidth, top + mSideBgHeight);
			}
		}

		// if (mSideEditBg!=null) {
		// mSideEditBg.setBounds(mEditBgRect);
		// }
		// 发消息通知allapptabbasiccontent绘制高亮图
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				AppFuncConstants.DRAW_SIDEBG_YES, mEditBgRect);
		if (mSwitchScreenTask == null) {
			mSwitchScreenTask = new SwitchScreenTask();
			long time = mScheduler.getClock().getTime();
			mSwitchScreenTask.mTaskId = mScheduler.schedule(Scheduler.TASK_TIME, time
					+ GO_TO_OTHER_SCREEN_ICON_DELAY, 100, 75, mSwitchScreenTask, nextOrPre);
		}
	}

	protected void clearSwitchScreenTask() {
		if (mSwitchScreenTask != null) {
			mScheduler.terminate(mSwitchScreenTask.mTaskId);
			mSwitchScreenTask = null;
		}
	}

	/**
	 * 
	 * <br>类描述:滚动图标定时任务
	 * <br>功能详细描述:
	 * 
	 * @author  huangshaotao
	 * @date  [2012-9-25]
	 */
	class SwitchScreenTask implements ITask {
		public long mTaskId;

		@Override
		public void execute(long id, long time, Object userName) {
			int nextOrPre = (Integer) userName;
			int scrollDuration = 350;
			if (XBaseGrid.sIsExit) {
				return;
			}
			switch (nextOrPre) {
				case 0 :
					mScroller.gotoScreen(mCurScreen + 1, scrollDuration, true);
					break;
				case 1 :
					mScroller.gotoScreen(mCurScreen - 1, scrollDuration, true);
					break;
				default :
					break;
			}
		}

		@Override
		public void finish(long id, long time, Object userName) {
			mSwitchScreenTask = null;

		}
	}
	
	@Override
	protected synchronized void onHide() {
		mScroller.gotoScreen(mCurScreen, 350, true);
		super.onHide();
	}
}
