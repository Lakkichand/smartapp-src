package com.jiubang.ggheart.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.gridscreen.GridScreenContainer;
import com.go.util.graphics.effector.subscreen.SubScreenContainer;
import com.go.util.graphics.effector.subscreen.SubScreenEffector;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IIndicatorUpdateListner;

/**
 * 
 * 多选图标对话框九宫格View
 *
 */
public class MutilCheckGridView extends ViewGroup
		implements
			ScreenScrollerListener,
			GridScreenContainer,
			SubScreenContainer {

	public static final int UPDATEINDICATOR = 1001;
	private ScreenScroller mScroller;
	private int mDefaultScreen = 0;

	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;

	private int mTouchState = TOUCH_STATE_REST;
	private float mLastMotionX;
	// 单元格宽度
	private int mGridWidth;
	// 单元格高度
	private int mGridHeight;
	// 屏幕的个数
	private int mTotalScreenNum = 1; // 初始值为1

	// GridView的列数
	private int mNumCulumns = 4;

	// 每一屏图标的最大个数
	private int mItemsCountPerScreen = 12;
	// 动画的运行时间
	// private long mTime ;
	// 图标总数
	private int mCount;
	private Paint mPaint;
	// 翻屏时间
	private int mDuration = 450;
	// 弹性特效
	private int mScrollingBounce = 40;

	// 是否循环滚动的标识
	private boolean mCycleMode = false;
	// 图标特效
	private SubScreenEffector mDeskScreenEffector;
	private int mCurrentScreen = 0;
	private Handler mHandler;
	// private GridView mGridView;
	private IIndicatorUpdateListner mIndicatorUpdateListner;
	private boolean mRespondMove = true; // 是否响应移位
	// 区域的最小高度
	private int mMinHeight;
	// 横竖屏切换标识
	private boolean mChgOrientation = false;

	public MutilCheckGridView(Context context) {
		this(context, null);
	}

	public MutilCheckGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context) {
		mScroller = new ScreenScroller(context, this);
		setOvershootAmount(mScrollingBounce);
		setScrollDuration(mDuration);
		mScroller.setBackgroundAlwaysDrawn(true);
		setCycleMode(false);
		mDeskScreenEffector = new SubScreenEffector(mScroller);
		mPaint = new Paint();
		mCurrentScreen = mDefaultScreen;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = x;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
				mRespondMove = true;
				// if(getCurrentView() != null){
				// getCurrentView().setmCenterPoint(0, 0);
				// }
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				final int xoffset = (int) (x - mLastMotionX);
				if (Math.abs(xoffset) > DrawUtils.sTouchSlop && mRespondMove) {
					mTouchState = TOUCH_STATE_SCROLLING;
					mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}

				break;
			}

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				mTouchState = TOUCH_STATE_REST;

				updateIndicator(mCount, mCurrentScreen);

				break;
			}

			default :
				break;
		}

		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (mScroller == null) {
			return false;
		}
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mScroller.onTouchEvent(event, action);
				break;

			case MotionEvent.ACTION_MOVE :
				mScroller.onTouchEvent(event, action);
				break;

			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				mScroller.onTouchEvent(event, action);
				break;

			default :
				break;
		}
		return true;
	}

	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (mScroller != null) {
			mScroller.computeScrollOffset();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		if (mScroller != null) {
			mScroller.setScreenSize(w, h);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = getChildCount();
		for (int i = 0; i < size; i++) {
			View childView = getChildAt(i);
			if (childView == null || childView.getLayoutParams() == null) {
				continue;
			}
			if (childView.getVisibility() != GONE) {
				childView.measure(widthMeasureSpec, heightMeasureSpec);
			}
		}
	}

	@Override
	public ScreenScroller getScreenScroller() {
		// TODO Auto-generated method stub
		return mScroller;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		// TODO Auto-generated method stub
		mScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub
		postInvalidate();
	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub
		View focusedChild = getFocusedChild();
		if (focusedChild != null && mScroller.getDstScreen() != mCurrentScreen) {
			focusedChild.clearFocus();
		}
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub
		if (null != mHandler) {
			final int offset = mScroller.getIndicatorOffset();
			Bundle dataBundle = new Bundle();
			dataBundle.putInt(DesktopIndicator.OFFSET, offset);
			Message msg = Message.obtain();
			msg.what = UPDATEINDICATOR;
			msg.obj = dataBundle;
			msg.arg1 = DesktopIndicator.UPDATE_SLIDER_INDICATOR;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		mCurrentScreen = newScreen;
		if (null != mHandler) {
			Bundle dataBundle = new Bundle();
			dataBundle.putInt(DesktopIndicator.CURRENT, newScreen);
			Message msg = Message.obtain();
			msg.what = UPDATEINDICATOR;
			msg.obj = dataBundle;
			msg.arg1 = DesktopIndicator.UPDATE_DOTS_INDICATOR;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// TODO Auto-generated method stub
		mCurrentScreen = currentScreen;
		// //解决换屏换位置后，有些屏的显示图标不对
		if (getCurrentView() != null) {
			getCurrentView().destroyDrawingCache();
			getCurrentView().postInvalidate();
		}
	}

	public GridView getCurrentView() {
		return (GridView) getChildAt(mCurrentScreen);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int childCount = getChildCount();
		int childLeft = 0;
		final int childWidth = r - l;
		int childHeight = 0;
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				childHeight = child.getMeasuredHeight();
				child.layout(childLeft, 0, childLeft + childWidth, childHeight);
				childLeft += childWidth;
			}
		}
		if (mScroller != null) {
			if (mChgOrientation) {
				// 回到第一屏
				mScroller.setCurrentScreen(0);
				mChgOrientation = false;
			}
			// 设置总屏数
			mScroller.setScreenCount(mTotalScreenNum);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.save();
		final int scrollX = getScrollX();
		final int height = Math.max(mMinHeight, getHeight());
		canvas.clipRect(scrollX, 0, getWidth() + scrollX, height);
		if (mScroller != null && !mScroller.isFinished()) {
			mScroller.onDraw(canvas);
		} else {
			super.dispatchDraw(canvas);
		}
		canvas.restore();
	}

	/**
	 * @param mIndicatorUpdateListner
	 *            the mIndicatorUpdateListner to set
	 */
	public void setmIndicatorUpdateListner(IIndicatorUpdateListner indicatorUpdateListner) {
		mIndicatorUpdateListner = indicatorUpdateListner;
	}

	public void snapToScreen(int screen, boolean noElastic, int duration) {
		// get the valid layout page
		if (mScroller != null) {
			mScroller.gotoScreen(screen, duration, noElastic);
		}
	}

	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurrentScreen = whichScreen;
		scrollTo(whichScreen * getWidth(), 0);
	}

	/**
	 * 设置翻屏时间
	 * 
	 * @param duration
	 *            翻屏时间
	 */
	public void setScrollDuration(int duration) {
		mDuration = duration;
		mScroller.setDuration(mDuration);
	}

	/**
	 * 设置弹性特效参数
	 * 
	 * @param bounce
	 *            屏幕边缘可以拉伸的距离相对半屏的百分比
	 */
	public void setOvershootAmount(int bounce) {
		if (mScrollingBounce == bounce) {
			return;
		}
		mScrollingBounce = bounce;
		mScroller.setMaxOvershootPercent(bounce);
	}

	public void setAutoTweakElasicity(boolean enabled) {
		mScroller.setEffectorMaxOvershootEnabled(enabled);
	}

	public void setCycleMode(boolean cycle) {
		if (mCycleMode != cycle) {
			mCycleMode = cycle;
			ScreenScroller.setCycleMode(this, cycle);
		}
	}

	@Override
	public void drawScreen(Canvas canvas, int screen) {
		// TODO Auto-generated method stub
		GridView gridview = (GridView) getChildAt(screen);
		if (null != gridview) {
			gridview.draw(canvas);
		}
	}

	@Override
	public void drawScreen(Canvas canvas, int screen, int alpha) {

	}

	@Override
	public void drawGridCell(Canvas canvas, int index) {
		// TODO Auto-generated method stub
		final int realIndex = index % mItemsCountPerScreen;
		final int screen = index / mItemsCountPerScreen;

		final GridView gridView = (GridView) getChildAt(screen);

		final int culumn = realIndex % mNumCulumns; // 第几列
		final int row = realIndex / mNumCulumns; // 第几行

		if (gridView != null) {
			final View view = gridView.getChildAt(realIndex);
			if (null == view) {
				return;
			}
			canvas.save();
			canvas.translate(-culumn * mGridWidth, -row * mGridHeight);
			if (!mScroller.isFinished()) {
				gridView.buildDrawingCache();
			}
			drawChild(canvas, view, getDrawingTime());
			canvas.restore();
		}
	}

	@Override
	public void drawGridCell(Canvas canvas, int index, int alpha) {
		// TODO Auto-generated method stub
		final int realIndex = index % mItemsCountPerScreen;
		final int screen = index / mItemsCountPerScreen;

		final GridView gridView = (GridView) getChildAt(screen);

		final int culumn = realIndex % mNumCulumns; // 第几列
		final int row = realIndex / mNumCulumns; // 第几行

		if (gridView != null) {
			final View view = gridView.getChildAt(realIndex);
			if (null == view) {
				return;
			}
			final Bitmap icon = view.getDrawingCache();
			if (null == icon) {
				return;
			}
			canvas.save();
			canvas.translate(-culumn * mGridWidth, -row * mGridHeight);
			final int oldAlpha = mPaint.getAlpha();
			if (oldAlpha != alpha) {
				mPaint.setAlpha(alpha);
			}
			canvas.drawBitmap(icon, view.getLeft(), view.getTop(), mPaint);
			canvas.restore();
			if (oldAlpha != alpha) {
				mPaint.setAlpha(oldAlpha);
			}
		}
	}

	@Override
	public int getCellCount() {
		// TODO Auto-generated method stub
		return mCount;
	}

	@Override
	public int getCellWidth() {
		// TODO Auto-generated method stub
		return mGridWidth;
	}

	@Override
	public int getCellHeight() {
		// TODO Auto-generated method stub
		return mGridHeight;
	}

	@Override
	public int getCellRow() {
		// TODO Auto-generated method stub
		return mItemsCountPerScreen / mNumCulumns;
	}

	@Override
	public int getCellCol() {
		// TODO Auto-generated method stub
		return mNumCulumns;
	}

	private void updateIndicator(int cellcount, int current) {
		if (cellcount >= 0) {
			int num = 0;
			num = cellcount / mItemsCountPerScreen;
			if (cellcount % mItemsCountPerScreen > 0) {
				// 多出一屏
				num++;
			}
			if (mIndicatorUpdateListner != null) {
				mIndicatorUpdateListner.updateIndicator(num, current);
			}
		}
	}

	public void initAdapterLayoutData() {
		countTotalNum(mCount);
		// Gridview的实际高度
		int realWidth = 0;
		int realHeight = 0;
		final int screenDisplayWidth = getContext().getResources().getDisplayMetrics().widthPixels;
		final int grid_width_v = getContext().getResources().getDimensionPixelSize(
				R.dimen.user_folder_grid_width_v);
		final int grid_width_h = getContext().getResources().getDimensionPixelSize(
				R.dimen.user_folder_grid_width_h);
		final int line_height = getContext().getResources().getDimensionPixelSize(
				R.dimen.user_folder_line_height);
		final int width_v = screenDisplayWidth * grid_width_v / 480;
		final int width_h = screenDisplayWidth * grid_width_h / 800;

		final int perHeight = line_height;
		// 行数
		if (mTotalScreenNum > 1) {
			realHeight = perHeight * (mItemsCountPerScreen / mNumCulumns);
		} else {
			realHeight = perHeight * (mCount / mNumCulumns);
			// 个数等于0或者不满整行都要补上一行
			if (mCount == 0 || mCount % mNumCulumns != 0) {
				realHeight += perHeight;
			}
		}
		// realHeight += mImageBottomH;
		mMinHeight = realHeight;
		if (GoLauncher.isLargeIcon()) {
			realHeight += 4;
		}
		realWidth = GoLauncher.getOrientation() == 1 ? width_v : width_h;

		mGridWidth = realWidth / mNumCulumns;
		mGridHeight = perHeight;

		getLayoutParams().height = realHeight; // 当前控件的高度
		getLayoutParams().width = screenDisplayWidth; // 当前控件的宽度
	}

	private void countTotalNum(int count) {
		boolean port = getContext().getResources().getConfiguration().orientation == 1;
		int layoutH = (int) getContext().getResources().getDimension(
				R.dimen.folder_edit_view_height);
		int indicatorH = (int) getContext().getResources().getDimension(
				R.dimen.folder_edit_indicator_hight);
		int topBottomH = (int) getContext().getResources().getDimension(
				R.dimen.folder_edit_top_bottom_hight);
		int cellH = (int) getContext().getResources().getDimension(R.dimen.folder_edit_item_height);
		int row = (layoutH - indicatorH - topBottomH * 2) / cellH; // 图标加上文字高度
		mNumCulumns = port ? 4 : 5;
		mItemsCountPerScreen = mNumCulumns * row;
		mTotalScreenNum = count / mItemsCountPerScreen;
		mTotalScreenNum = count % mItemsCountPerScreen > 0 ? ++mTotalScreenNum : mTotalScreenNum;
		mScroller.setScreenCount(mTotalScreenNum);
	}

	public void initLayoutData(int itemsCount) {
		mCount = itemsCount;
		initAdapterLayoutData();
	}

	public void recyle() {
		removeAllViews();
//		mScroller = null;
		mIndicatorUpdateListner = null;
		mDeskScreenEffector.recycle();
		mDeskScreenEffector = null;
		mHandler = null;
	}

	// 横竖屏切换的调整，主要是指示器的调整
	public void changeOrientation() {
		mChgOrientation = true;
	}

	public int getScreenCount() {
		return mTotalScreenNum;
	}

	public void setHanler(Handler handler) {
		mHandler = handler;
	}

	public int getCountPerPage() {
		return mItemsCountPerScreen;
	}
}
