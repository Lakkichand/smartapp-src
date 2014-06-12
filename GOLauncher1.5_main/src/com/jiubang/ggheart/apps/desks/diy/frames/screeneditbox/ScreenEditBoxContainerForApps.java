package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

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
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>
 * 类描述:app和文件夹使用的容器 <br>
 * 功能详细描述:
 */
public class ScreenEditBoxContainerForApps extends LinearLayout implements
		ScreenScrollerListener, IDataSetChangeListener, SubScreenContainer {
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

	private int mItemsCount = 4; // 一页多少个 默认是4
	private int mPageCount = 0; // 页数
	private int mRowCount = 0; // 行数
	// 特效
	private SubScreenEffector mDeskScreenEffector;
		
	public ScreenEditBoxContainerForApps(Context context, AttributeSet attrs) {
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
		int rightSpace = GoLauncher.getDisplayWidth() - horizontalpading
				- mItemsCount * (viewWidth + horizontalpading);
		if (rightSpace >= viewWidth) {
			++mItemsCount;
		}

		int viewheight = (int) getContext().getResources().getDimension(
				R.dimen.screen_edit_view_height_app); // app高度
		if (DrawUtils.sHeightPixels <= 800) { // 保证 800分辨率的手机能显示三排
			viewheight = DrawUtils.dip2px(76);
		}
		int mAppHeight = (int) (DrawUtils.sHeightPixels
				* ScreenEditLayout.APPSCALE
				- getContext().getResources().getDimension(
						R.dimen.screen_edit_indicator_height) - getContext()
				.getResources().getDimension(
						R.dimen.screen_edit_tabtitle_height));
		mRowCount = mAppHeight / viewheight;
		if (mAppHeight % viewheight == 0) {
			mRowCount = mRowCount - 1;
		}

		// 判断是否为循环显示
		GoSettingControler mGoSettingControler = GOLauncherApp
				.getSettingControler();
		ScreenSettingInfo mScreenSettingInfo = mGoSettingControler
				.getScreenSettingInfo();
		if (mScreenSettingInfo != null) {
			boolean islooper = mScreenSettingInfo.mScreenLooping;
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

	// 初始化container的子view
	private void initChildView(boolean isRefreshAll) {
		unInitAppFont();
		unInitImage();
		removeAllViews();
		if (!isRefreshAll) {
			return;
		}
		int totleItemCount = mCurTab.getItemCount();
		int pageCount = totleItemCount / (mItemsCount * mRowCount)
				+ (totleItemCount % (mItemsCount * mRowCount) > 0 ? 1 : 0);
		ViewGroup.LayoutParams params1 = new ViewGroup.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		// j=屏数 k=行数 i=列数
		for (int j = 0; j < pageCount; j++) {
			ScreenEditRowView pageLayout = new ScreenEditRowView(getContext());
			// pageLayout.setBackgroundColor(R.color.theme_tab_no_focus);
			pageLayout.setMaxCount(mRowCount);
			// Log.i("jiang", "第一层 屏数："+j+" totleItemCount: " +totleItemCount);
			// 已加item
			int currenItemNunm = 0;
			for (int k = 0; k < mRowCount; k++) {
				// Log.i("jiang", "第"+j+"屏  第"+k+"行："+currenItemNunm);
				// 多少项的添加
				ScreenEditGridViewForApps rowLayout = new ScreenEditGridViewForApps(
						getContext());
				rowLayout.setMaxCount(mItemsCount);
				for (int i = 0; i < mItemsCount
						&& j * mItemsCount * mRowCount + k * mItemsCount + i < totleItemCount; i++) {
					// Log.i("jiang", "第三层 条目数："+( j*(mItemsCount*3)+
					// k*mItemsCount + i));
					View childView = mCurTab.getView(j
							* (mItemsCount * mRowCount) + k * mItemsCount + i);
					if (childView != null) {
						childView.setOnClickListener(mCurTab);
						childView.setOnLongClickListener(mCurTab);
						childView
								.setBackgroundResource(R.drawable.screen_edit_item_select);
						rowLayout.addView(childView);
					}
				}

				// rowLayout.setBackgroundResource(R.drawable.appfunc_movetodesk);

				pageLayout.addView(rowLayout, params);
				currenItemNunm = currenItemNunm + mRowCount * j * k
						* mItemsCount + k * mItemsCount;
			}
			addView(pageLayout, params1);
		}
		mScroller.setScreenCount(pageCount);
		mScroller.setCurrentScreen(0);
		mPageCount = pageCount;
		// 指示器可见
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditLayout mLayView = (ScreenEditLayout) screenEditBoxFrame
					.getContentView();
			mLayView.getLargeTabView()
					.findViewById(R.id.indicator_layout_large)
					.setVisibility(VISIBLE);
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
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
	}

	// 处理touch事件
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mCurTab.showDragFrame()) {
			return true;
		}
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mLastMotionX = x;
			mlastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final int xoffset = (int) (x - mLastMotionX);
			final int yoffset = (int) (y - mlastMotionY);
			if (Math.abs(yoffset) < Math.abs(xoffset)
					&& Math.abs(xoffset) > DrawUtils.sTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
				mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
			}

			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			mTouchState = TOUCH_STATE_REST;
			break;
		}

		default:
			break;
		}

		return mTouchState != TOUCH_STATE_REST;

	}

	@Override
	public void computeScroll() {
		// super.computeScroll();
		mScroller.computeScrollOffset();
	}

	// 屏幕跳转
	public void snapToScreen(int screen, boolean noElastic, int duration) {
		// get the valid layout page
		mScroller.gotoScreen(screen, duration, noElastic);
	}

	// 设置当前tab
	public void setCurrentTab(BaseTab tab) {
		tab.setDataSetListener(this);
		mCurTab = tab;
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
		final int height = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.APPSCALE);
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

	// 取出TextView释放字体
	public void unInitAppFont() {
		try {
			int mcount = this.getChildCount();
			for (int i = 0; i < mcount; i++) {
				ScreenEditRowView rowGridView = (ScreenEditRowView) this
						.getChildAt(i);
				int rowCount = rowGridView.getChildCount();
				for (int j = 0; j < rowCount; j++) {
					ScreenEditGridViewForApps cellGridView = (ScreenEditGridViewForApps) rowGridView
							.getChildAt(j);
					int cellCount = cellGridView.getChildCount();
					for (int k = 0; k < cellCount; k++) {
						LinearLayout cellTab = (LinearLayout) cellGridView
								.getChildAt(k);
						int childCount = cellTab.getChildCount();
						for (int h = 0; h < childCount; h++) {
							View textView = cellTab.getChildAt(h);
							if (textView instanceof DeskTextView) {
								((DeskTextView) textView).selfDestruct();
								textView = null;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void unInitImage() {
		int mcount = this.getChildCount();
		for (int i = 0; i < mcount; i++) {
			ScreenEditRowView rowGridView = (ScreenEditRowView) this.getChildAt(i);
			int rowCount = rowGridView.getChildCount();
			for (int j = 0; j < rowCount; j++) {
				ScreenEditGridViewForApps cellGridView = (ScreenEditGridViewForApps) rowGridView
						.getChildAt(j);
				int cellCount = cellGridView.getChildCount();
				for (int k = 0; k < cellCount; k++) {
					LinearLayout cellTab = (LinearLayout) cellGridView
							.getChildAt(k);
					int childCount = cellTab.getChildCount();
					for (int h = 0; h < childCount; h++) {
						View v = cellTab.getChildAt(h);
						/*if (v instanceof ImageView) {
							((ImageView) v).setImageDrawable(null);
							v = null;
						}
						if (v instanceof TextView) {
							((TextView) v).setText(null);
							v = null;
						}*/
						
						if (v != null && v instanceof FrameLayout) {
							FrameLayout frameLayout = (FrameLayout) v;
							for (int g = 0; g < childCount; g++) {
								View img = frameLayout.getChildAt(g);
								if (img instanceof ImageView) {
									((ImageView) img).setImageDrawable(null);
									img = null;
								}
							}
						}
					}
				}
			}
		}
}
	// ------------------------------以下为特效部分
			@Override
			public void drawScreen(Canvas canvas, int screen) {
				// TODO Auto-generated method stub
				ScreenEditRowView gridview = (ScreenEditRowView) getChildAt(screen);
				if (gridview != null) {
					gridview.draw(canvas);
				}
			}

			@Override
			public void drawScreen(Canvas canvas, int screen, int alpha) {
				// TODO Auto-generated method stub
				
			}
			public void recyle() {
				mDeskScreenEffector.recycle();
				mDeskScreenEffector = null;
			}
			
}
