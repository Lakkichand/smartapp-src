package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.subscreen.SubScreenContainer;
import com.go.util.graphics.effector.subscreen.SubScreenEffector;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.DesktopThemeTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.LockerThemeTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WallpaperSubTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WidgetTab;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:普通的tab容器
 * <br>功能详细描述:
 */
public class ScreenEditBoxContainer extends LinearLayout
		implements
			ScreenScrollerListener,
			IDataSetChangeListener,
			SubScreenContainer,
			ILoadDrawableProducer {
	private ScreenScroller mScroller;
	private final static int SCROLLER_DURATION = 400;
	private static final int TOUCH_STATE_REST = 0;
	// 触屏状态
	private final static int TOUCH_STATE_SCROLLING = 1;
	// 当前触屏状态
	private int mTouchState = TOUCH_STATE_REST;
	// 上次触屏离开的x坐标
	private float mLastMotionX;
	private float mlastMotionY;
	private BaseTab mCurTab;
	private TabIndicatorUpdateListner mIndicatorUpdateListner;
	private boolean mIsNeedAsyncTask = false;
	private DrawableLoadTasker<ImageView, IDrawableLoader> mLoadTasker;
	private int mItemsCount = 4; // 一页多少个 默认是4
	private int mPageCount = 0;
	private int mTotalCount = 0;
	private List<Integer> mLoadedScreens;
	// 特效
	private SubScreenEffector mDeskScreenEffector;
		
	public ScreenEditBoxContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new ScreenScroller(getContext(), this);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setDuration(SCROLLER_DURATION);
		mScroller.setMaxOvershootPercent(0);
		int horizontalpading = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_horizontal_space);
		int viewWidth = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_width);
		mItemsCount = (GoLauncher.getDisplayWidth() - horizontalpading)
				/ (viewWidth + horizontalpading);
		int rightSpace = GoLauncher.getDisplayWidth() - horizontalpading - mItemsCount
				* (viewWidth + horizontalpading);
		if (rightSpace >= viewWidth) {
			++mItemsCount;
		}
		
		//判断是否为循环显示
				GoSettingControler	mGoSettingControler = GOLauncherApp.getSettingControler();
				ScreenSettingInfo mScreenSettingInfo = mGoSettingControler.getScreenSettingInfo();
				if (mScreenSettingInfo != null) {
					boolean islooper = mScreenSettingInfo.mScreenLooping ;
					mScroller.setCycleMode(this, islooper);
					}
				mDeskScreenEffector = new SubScreenEffector(mScroller);
				mDeskScreenEffector.setType(0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int maxheight = 0;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childheight = child.getMeasuredHeight();
			if (maxheight < childheight) {
				maxheight = childheight;
			}
		}
		setMeasuredDimension(getMeasuredWidth(), maxheight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int width = r - l;
		int childcount = getChildCount();

		int left = 0;
		int top = 0;
		int right = 0;

		for (int i = 0; i < childcount; i++) {
			View childView = getChildAt(i);
			childView.measure(r - l, b - t);
			right = left + width;
			childView.layout(left, top, right, b);
			left += width;
		}
	}
   // 初始化container的子view，页数，
	private void initChildView(boolean isRefreshAll) {
		unInitFront();
		removeAllViews();
		if (!isRefreshAll) {
			return;
		}	
		int totleItemCount = mCurTab.getItemCount();
		mTotalCount = totleItemCount;
		if (totleItemCount == 0) {
			return;
		}
		int pageCount = totleItemCount / mItemsCount + (totleItemCount % mItemsCount > 0 ? 1 : 0);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		for (int j = 0; j < pageCount; j++) {
			ScreenEditGridView layout = new ScreenEditGridView(getContext());
			layout.setMaxCount(mItemsCount);
			for (int i = 0; i < mItemsCount && j * mItemsCount + i < totleItemCount; i++) {
				View childView = mCurTab.getView(j * mItemsCount + i);
				if (childView != null) {
					childView.setOnClickListener(mCurTab);
					childView.setOnLongClickListener(mCurTab);
					childView.setBackgroundResource(R.drawable.screen_edit_item_select);
					layout.addView(childView);
				}
			}
			addView(layout, params);
		}
		addLoadTaskItem(0);
		mScroller.setScreenCount(pageCount);
		mScroller.setCurrentScreen(0);
		mPageCount = pageCount;

		// 指示器可见
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditLayout mLayView = (ScreenEditLayout) screenEditBoxFrame.getContentView();
			mLayView.findViewById(R.id.indicator_layout).setVisibility(VISIBLE);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mCurTab.showDragFrame()) {
			return true;
		}
		if (null != mScroller) {
			mScroller.onTouchEvent(event, event.getAction());
		}
		return true;
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		mScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		postInvalidate();
	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		mIndicatorUpdateListner.onScrollChanged(mScroller.getIndicatorOffset());
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mIndicatorUpdateListner.onScreenChanged(newScreen);
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		View childView = getChildAt(currentScreen);
		if (childView != null) {
			childView.postInvalidate();
		}
		if (mIsNeedAsyncTask && !mLoadedScreens.contains(Integer.valueOf(currentScreen))) { // 滑动停止，按需异步加载壁纸
			addLoadTaskItem(currentScreen);
			mLoadedScreens.add(Integer.valueOf(currentScreen));
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mCurTab.showDragFrame()) {
			return true;
		}
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = x;
				mlastMotionY = y;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				final int xoffset = (int) (x - mLastMotionX);
				final int yoffset = (int) (y - mlastMotionY);
				if (Math.abs(yoffset) < Math.abs(xoffset)
						&& Math.abs(xoffset) > DrawUtils.sTouchSlop) {
					mTouchState = TOUCH_STATE_SCROLLING;
					mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}

				break;
			}

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				mTouchState = TOUCH_STATE_REST;
				break;
			}

			default :
				break;
		}

		return mTouchState != TOUCH_STATE_REST;

	}

	@Override
	public void computeScroll() {
		// super.computeScroll();
		mScroller.computeScrollOffset();
	}

	public void snapToScreen(int screen, boolean noElastic, int duration) {
		// get the valid layout page
		mScroller.gotoScreen(screen, duration, noElastic);
	}
    // 设置当前tab
	public void setCurrentTab(BaseTab tab) {
		tab.setDataSetListener(this);
		mCurTab = tab;
		// 是否需要异步加载Drawable，壁纸，主题，锁屏，Go小部件需要
		mIsNeedAsyncTask = mCurTab instanceof WallpaperSubTab || mCurTab instanceof DesktopThemeTab
				|| mCurTab instanceof LockerThemeTab || mCurTab instanceof WidgetTab;
		if (mIsNeedAsyncTask) {
			mLoadTasker = DrawableLoadTasker.getInstance();
			mLoadTasker.reset();
			mLoadedScreens = new ArrayList<Integer>();
			mLoadedScreens.add(Integer.valueOf(0));
		}
		if (tab.isNeedAsyncLoadData() && tab.isFirstConstruct()) {
			initChildView(false);
			tab.setFirstConstruct(false);
		} else {
			initChildView(true);
		}
	}

	public void setIndicatorUpdateListner(TabIndicatorUpdateListner listner) {
		mIndicatorUpdateListner = listner;
	}

	public int getPageCount() {
		return mPageCount;
	}

	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		scrollTo(whichScreen * getWidth(), 0);
	}

	private void updateIndicator(int current) {
		mIndicatorUpdateListner.updateIndicator(mPageCount, current);
	}

	@Override
	public void dataChanged() {
		initChildView(true);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mScroller.invalidateScroll();
		// super.dispatchDraw(canvas);
		canvas.save();
		final int scrollX = getScrollX();
		final int height = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_box_container_normal);;
		canvas.clipRect(scrollX, 0, getWidth() + scrollX, height);
		if (!mScroller.isFinished()) {
			mScroller.onDraw(canvas);
		} else {
			super.dispatchDraw(canvas);
		}
		canvas.restore();
	}

	public BaseTab getCurTab() {
		return mCurTab;
	}
	// 生产者添加加载任务
	@Override
	public void addLoadTaskItem(int position) {
		if (mIsNeedAsyncTask) {
			ScreenEditGridView gridView = (ScreenEditGridView) getChildAt(position);
			if (gridView == null) {
				return;
			}
			for (int i = 0; i < mItemsCount; i++) {
				if (!(position * mItemsCount + i < mTotalCount)) {
					return;
				}
				View subView = gridView.getChildAt(i);
				if (subView == null) {
					return;
				}
				ImageView image = (ImageView) subView.findViewById(R.id.thumb);
				if (image.getTag() != Boolean.valueOf(true)) {
					mLoadTasker.addLoadingInfo(new LoadingDrawableItem<ImageView, IDrawableLoader>(
							image, (IDrawableLoader) mCurTab, position * mItemsCount + i));
				}
			}
		}
	}	
	//释放字体
	public void unInitFront() {
		int mcount = this.getChildCount();
		for (int i = 0; i < mcount; i++) {
			ScreenEditGridView cellGridView = (ScreenEditGridView) this.getChildAt(i);
			int cellCount = cellGridView.getChildCount();
			for (int j = 0; j < cellCount; j++) {
				LinearLayout cellTab = (LinearLayout) cellGridView.getChildAt(j);
				int childCount = cellTab.getChildCount();
				for (int k = 0; k < childCount; k++) {
					View textView = cellTab.getChildAt(k);
					if (textView != null && textView instanceof DeskTextView) {
						((DeskTextView) textView).selfDestruct();
						((DeskTextView) textView).setText(null);
						textView = null;
					}
				}				
			}
		}
	}
	
	public void unInitImage() {
		int mcount = this.getChildCount();
		for (int i = 0; i < mcount; i++) {
			ScreenEditGridView cellGridView = (ScreenEditGridView) this
					.getChildAt(i);
			int cellCount = cellGridView.getChildCount();
			for (int j = 0; j < cellCount; j++) {
				LinearLayout cellTab = (LinearLayout) cellGridView
						.getChildAt(j);
				int childCount = cellTab.getChildCount();
				for (int k = 0; k < childCount; k++) {
					View v = cellTab.getChildAt(k);
					if (v != null && v instanceof FrameLayout) {
						FrameLayout textView = (FrameLayout) cellTab
								.getChildAt(k);
						for (int g = 0; g < textView.getChildCount(); g++) {
							View img = textView.getChildAt(g);
							if (img != null && img instanceof ImageView) {
								// Toast.makeText(mContext, "mContainer",
								// Toast.LENGTH_SHORT).show();
								((ImageView) img).setImageDrawable(null);
								img = null;
							}
						}
					}
				}
			}
		}
	}
	@Override
	public void drawScreen(Canvas canvas, int screen) {
		ScreenEditGridView gridview = (ScreenEditGridView) getChildAt(screen);
		if (gridview != null) {
			gridview.draw(canvas);
		}
	}

	@Override
	public void drawScreen(Canvas canvas, int screen, int alpha) {
		// TODO Auto-generated method stub
		
	}
	
}
