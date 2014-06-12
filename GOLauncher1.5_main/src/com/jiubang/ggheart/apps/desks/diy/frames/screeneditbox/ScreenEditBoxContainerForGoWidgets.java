package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WidgetSubTab;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:widget预览界面容器
 * <br>功能详细描述:设置容器中的子view，监听子view的事件
 * 
 * @date  [2012-9-10]
 */
public class ScreenEditBoxContainerForGoWidgets extends LinearLayout implements
		ScreenScrollerListener, IDataSetChangeListener, SubScreenContainer, ILoadDrawableProducer {
	private ScreenScroller mScroller;

	public ScreenScroller getmScroller() {
		return mScroller;
	}

	private DrawableLoadTasker<ImageView, IDrawableLoader> mLoadTasker;
	private boolean mIsAsyncTask;
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

	private int mPageCount = 0;
	private int mWidgetWeight = 0;
	private List<Integer> mLoadedScreens;
	private int mOldScreen;
	
	// 特效
	private SubScreenEffector mDeskScreenEffector;
		
	public ScreenEditBoxContainerForGoWidgets(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		mScroller = new ScreenScroller(getContext(), this);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setDuration(SCROLLER_DURATION);
		mScroller.setMaxOvershootPercent(0);
		mWidgetWeight = (int) context.getResources().getDimension(
				R.dimen.screen_edit_subview_widght);
		
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
			ViewGroup childView = (ViewGroup) getChildAt(i);
			childView.measure(r - l, b - t);
			right = left + width;
			
			childView.layout(left, top, right, b);
			left += width;
		}
	}

	private void initChildView(boolean isRefreshAll) {
		unInitWidgetFont();
		unInitImage();
		removeAllViews();
		if (!isRefreshAll) {
			return;
		}
				int totleItemCount = mCurTab.getItemCount();
		if (totleItemCount == 0) {
			return;
		}

		for (int i = 0; i < totleItemCount; i++) {
			ScreenEditGridViewForGoWidgets layout = new ScreenEditGridViewForGoWidgets(
					getContext());
			View childView = mCurTab.getView(i);
			if (childView != null) {
				childView.setOnClickListener(mCurTab);
				childView.setOnLongClickListener(mCurTab);
				//childView.setBackgroundResource(R.drawable.screen_edit_item_select);
				layout.addView(childView);
			}
			//layout.setBackgroundColor(0xff229988);
			//区分普通预览页和详情页，用于ScreenEditGridViewForGoWidgets onLayout（）的时候区分
			if (i == totleItemCount - 1) {
				layout.setTag("info");
			}
		addView(layout);
		}
		addLoadTaskItem(0);
		mScroller.setScreenCount(totleItemCount);
		mScroller.setCurrentScreen(0);
		mPageCount = totleItemCount;
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
	// 添加任务到生产队列
	@Override
	public void addLoadTaskItem(int position) {
		if (!mIsAsyncTask) {
			return;
		}
		if (position < 0 || position >= mCurTab.getItemCount() - 1) {
			return;
		}
		View view = getChildAt(position);
		if (view == null) {
			return;
		}
		ImageView image = (ImageView) view
				.findViewById(R.id.screenedit_upperhalf);
		if (image.getTag() != Boolean.valueOf(true)) {
			mLoadTasker
					.addLoadingInfo(new LoadingDrawableItem<ImageView, IDrawableLoader>(
							image, (IDrawableLoader) mCurTab, position));
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mCurTab == null) {
			return true;
		}
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
	public void clearTabFont() {
		if (mCurTab instanceof WidgetSubTab) {
			((WidgetSubTab) mCurTab).unInitInfoFont();
		}
	}
	
	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		mIndicatorUpdateListner.onScrollChanged(mScroller.getIndicatorOffset());
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		int childcount = getChildCount();
		if (newScreen == childcount - 1) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
					IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET_INFO, 0, null, null);
		} else if (oldScreen == childcount - 1 && newScreen != childcount - 1) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
					IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET_PIC, 0, null, null);
		}
		mOldScreen = oldScreen;
		mIndicatorUpdateListner.onScreenChanged(newScreen);
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		View childView = getChildAt(currentScreen);
		if (childView != null) {
			childView.postInvalidate();
		}
		if (mIsAsyncTask
				&& !mLoadedScreens.contains(Integer.valueOf(currentScreen))) {
			addLoadTaskItem(currentScreen);
			mLoadedScreens.add(Integer.valueOf(currentScreen));
		}
		
		if (mOldScreen != this.getChildCount()) {
			ViewGroup view = (ViewGroup) getChildAt(mOldScreen);
			if (view == null) {
				return;
			}
			for (int i = 0; i < view.getChildCount(); i++) {
				View childView2 = view.getChildAt(i);
				if (childView2 instanceof LinearLayout) {
					LinearLayout line = (LinearLayout) childView2;
					for (int j = 0; j < line.getChildCount(); j++) {
						View v = line.getChildAt(j);
						if (v instanceof ImageView) {
							ImageView image = (ImageView) v;
							image.setImageDrawable(null);
							image.setTag(Boolean.valueOf(false));
							mLoadedScreens.remove(Integer.valueOf(mOldScreen));
						}
					}
				}
					
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mCurTab == null) {
			return true;
		}
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

	public void snapToScreen(int screen, boolean noElastic, int duration) {
		// get the valid layout page
		mScroller.gotoScreen(screen, duration, noElastic);
	}

	// public void setCurrentTab(BaseTab tab) {
	// tab.setDataSetListener(this);
	// mCurTab = tab;
	// // initChildView();
	// }

	public void setCurrentLargeTab(BaseTab tab) {
		tab.setDataSetListener(this);
		mCurTab = tab;
		mIsAsyncTask = mCurTab instanceof WidgetSubTab;
		if (mIsAsyncTask) {
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
		// initChildView();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mScroller.invalidateScroll();
		// super.dispatchDraw(canvas);
		canvas.save();
		final int scrollX = getScrollX();
		if (DrawUtils.sHeightPixels < 800) {
			final int height = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE);
			canvas.clipRect(scrollX, 0, getWidth() + scrollX, height);
		} else {
			final int height = (int) getContext().getResources().getDimension(
					R.dimen.screen_edit_box_container_gowidgets);
			canvas.clipRect(scrollX, 0, getWidth() + scrollX, height);
		}
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
	//释放字体
	public void unInitWidgetFont() {
		int mcount = this.getChildCount();
		for (int i = 0; i < mcount; i++) {
			ViewGroup cellTab = (ViewGroup) this.getChildAt(i);
			int childCount = cellTab.getChildCount();
			for (int j = 0; j < childCount; j++) { 
				View textView = cellTab.getChildAt(j);
				if (textView instanceof DeskTextView) {
					((DeskTextView) textView).selfDestruct();
					textView = null;
				}						
			}
		}
		clearTabFont();
	}
	
	public void unInitImage() {
		for (int i = 0; i < this.getChildCount(); i++) {
			ViewGroup childView = (ViewGroup) this.getChildAt(i);
			if (childView != null) {
				for (int j = 0; j < childView.getChildCount(); j++) {
					View childView2 = childView.getChildAt(j);
					if (childView2 instanceof LinearLayout) {
						LinearLayout line = (LinearLayout) childView2;
						for (int k = 0; k < line.getChildCount(); k++) {
								View v = line.getChildAt(k);
							if (v instanceof ImageView) {
								ImageView image = (ImageView) v;
								image.setImageDrawable(null);
								image = null;
							//	image.setTag(Boolean.valueOf(false));
							//	mLoadedScreens.remove(Integer.valueOf(mOldScreen));
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
					ViewGroup gridview = (ViewGroup) getChildAt(screen);
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