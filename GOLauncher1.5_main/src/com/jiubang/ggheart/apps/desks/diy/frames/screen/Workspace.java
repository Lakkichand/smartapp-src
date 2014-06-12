package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mobi.intuitit.android.widget.WidgetSpace;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.CompatibleUtil;
import com.go.util.Utilities;
import com.go.util.animation.MyAnimationUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.gridscreen.GridScreenContainer;
import com.go.util.graphics.effector.subscreen.SubScreenContainer;
import com.go.util.graphics.effector.subscreen.SubScreenEffector;
import com.go.util.graphics.effector.united.CoupleScreenEffector;
import com.go.util.log.LogConstants;
import com.go.util.multitouch.MultiTouchController;
import com.go.util.multitouch.MultiTouchController.MultiTouchObjectCanvas;
import com.go.util.multitouch.MultiTouchController.PointInfo;
import com.go.util.multitouch.MultiTouchController.PositionAndScale;
import com.go.util.scroller.FastVelocityTracker;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.go.util.window.OrientationControl;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SenseWorkspace;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderIcon.FolderRingAnimator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture.MultiTouchDetector;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture.MutilPointInfo;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture.OnMultiTouchListener;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.components.TouchHelperChooser;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.plugin.mediamanagement.AppInfo;
import com.jiubang.ggheart.screen.back.IWallpaperDrawer;
import com.jiubang.ggheart.screen.touchhelper.TouchHelperDownloadGuideActivity;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * 普通模式下的屏幕容器，管理各个屏幕
 * 
 * @author luopeihuan
 * 
 */
public class Workspace extends WidgetSpace
		implements
			MultiTouchObjectCanvas<Object>,
			OnClickListener,
			OnLongClickListener,
			ScreenScrollerListener,
			SubScreenContainer,
			GridScreenContainer,
			AnimationListener, OnMultiTouchListener {
	private IMessageHandler mListener;
	private final static String LOG_TAG = "Workspace";
	private static final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x00000020;
	// 触屏状态
	private final static int TOUCH_STATE_RESET = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private final static int TOUCH_SWIPE_DOWN_GESTURE = 2;
	private final static int TOUCH_SWIPE_UP_GESTURE = 3;

	public static final int CHANGE_SOURCE_DOCK = 1;
	public static final int CHANGE_SOURCE_INDICATOR = 2;
	public static final int CHANGE_SOURCE_STATUSBAR = 3;

	// Related to dragging, folder creation and reordering
	private static final int DRAG_MODE_NONE = 0;
	protected static final int DRAG_MODE_CREATE_FOLDER = 1;
	protected static final int DRAG_MODE_ADD_TO_FOLDER = 2;
	protected static final int DRAG_MODE_REORDER = 3;
	private int mDragMode = DRAG_MODE_NONE;

	private int mTouchState = TOUCH_STATE_RESET;
	private boolean mFirstLayout = true;
	private boolean mTouchedScrollableWidget = false;

	// 设置滚动速度
	private int mScrollingDuration = 450;

	// 默认屏幕
	private int mMainScreen;

	// 多点触摸控制器
	private MultiTouchController<Object> multiTouchController;
	private boolean mMultiTouchOccured = false;
	// 多点触摸识别
	private MultiTouchDetector mTouchDetector;
	// Wysie: Values taken from CyanogenMod (Donut era) Browser
	private static final double ZOOM_SENSITIVITY = 1.6;
	public static final double ZOOM_LOG_BASE_INV = 1.0 / Math.log(2.0 / ZOOM_SENSITIVITY);

	private float mInterceptTouchDownX;
	private float mInterceptTouchDownY;
	private float mInterceptTouchMoveX;
	private float mInterceptTouchMoveY;
	private boolean mInterceptTouchMoved;
	private float mInterceptTouchVY;
	private FastVelocityTracker mVelocityTracker;
	private int mSwipVelocity; // 上下滑屏识别为手势的速度阈值
	private int mSwipTouchSlop; // 上下划动识别为手势的距离阈值

	// lock 标志，为ture时不响应触屏事件
	private boolean mLocked;
	// private boolean mAllowLongPress;
	// 标志是否响应touch事件
	private boolean mTouch = true;
	private boolean mIsTouchUp = true;

	private int mTouchSlop; // 区分滑动和点击的阈值
	private boolean mLongClickView = false; // 是否长按图标
	private boolean mLongClickVacant = false; // 是否长按空白区域
	private View mEditView = null;
	private View mClickView = null;
	// private boolean mChangeOrientation = false;
	private boolean mAutoStretch = false;
	/**
	 * Cache of vacant cells, used during drag events and invalidated as needed.
	 */
	private CellLayout.CellInfo mVacantCache = null;
	private final int[] mTempCell = new int[2];
	private final int[] mTempEstimate = new int[2];
	private CellLayout.CellInfo mDragInfo = null;
	protected int[] mTargetCell = new int[2]; // 目标网格
	private int mDragOverX = -1;
	private int mDragOverY = -1;
	// 桌面行数和列数
	private int mDesktopRows = 4;
	private int mDesktopColumns = 4;
	// 单屏幕的格子数
	private int mGridCount;
	// 单个格子宽度
	private int mGridWidth;
	// 单个格子高度
	private int mGridHeight;
	// 实际图标的大小
	private int mSubWidth;
	private int mSubHeight;
	// 当前屏和下一屏的单元格的显示状态 point.x :1为bubbletext -2为widget 0为空; point.y :对应的view的索引
	private Point[] mCurGridsState;
	private Point[] mNextGridsState;
	// 要绘制的当前屏（A）和下一屏（B）
	private int mScreenA = -101;
	private int mScreenB = -102;
	// 弹性特效
	private int mScrollingBounce = 40;
	// 是否显示预览
	private boolean mShowPreview = false;
	private boolean mPreventFC = true;

	ScreenScroller mScroller;
	// 图标特效
	private CoupleScreenEffector mDeskScreenEffector;
	int mEffectorType;
	private boolean mCycleMode = false;

	public final static int DRAW_STATE_ALL = 0; // 正常状态，绘制所有内容
	public final static int DRAW_STATE_ONLY_BACKGROUND = 1; // 被半透明物体遮挡，只绘制背景
	public final static int DRAW_STATE_DISABLE = 2; // 完全隐藏
	public final static int DRAW_STATE_NORMAL_TO_SMALLER_ENTERING = 3; // 进入添加模块动画进行时
	public final static int DRAW_STATE_SMALLER_TO_NORMAL_ENTERING = 4; // 退出添加模块动画进行时
	public final static int DRAW_STATE_ZOOM_OUT = 5; // 缩小动画进行时
	public final static int DRAW_STATE_ZOOM_IN = 6; // 放大动画进行时
	int mDrawState;

	private boolean mWallpaperScrollEnabled = true;

	// TODO: 如果会影响滑屏速度，可以考虑做成设置选项，默认为false
	boolean mSaveMomoryWhenIdle = false; // 空闲时节省内存

	private final static int ANIMATION_NONE = 0;
	private final static int ANIMATION_DROP = 1;
	private int mAnimationType;
	private Paint mPaint;
	/**
	 * 通知widget进入或离开当前屏幕的Runnable
	 */
	private WidgetRunnable mEnterRunnable = new WidgetRunnable(true);
	private WidgetRunnable mLeaveRunnable = new WidgetRunnable(false);
	// 通知widget延时，当停留在某个屏幕操过这个时间才通知widget
	public final static int FIRE_WIDGET_DELAY = 1000;

	private boolean mNeedToCatch = false;

	// 自动播放特效的标识
	private boolean mShowAutoEffect = false;
	// 自动特效的返回屏
	private int mNextDestScreen;
	// 自动特效的展示时间
	private static final int AUTO_EFFECT_TIME = 750;
	// 是否正在拖拽
	protected boolean mDragging;
	// 第一次获取文件夹的区域
	protected boolean mFirstGetFolderRects = true;
	/**
	 * Distance a touch can wander before we think the user is attempting a
	 * paged scroll (in dips)
	 */
	private static final int PAGING_TOUCH_SLOP = 16;
	private Rect mGridRect; // 在格子特效（带透明参数）中的裁剪单元格区域
	private Rect mDestRect = new Rect(); // 在格子特效（带透明参数）中的裁剪目标（矩形区域）
	private Matrix mMatrix = new Matrix(); // 绘制格子特效（带透明度）时的矩阵
	protected int mScreenWidth;
	private static final int VIBRATE_DURATION = 15;
	private Vibrator mVibrator;
//	private AlarmHandler mAlarmHandler; // 节日彩蛋定时处理者
	
	/**
	 * 
	 * @param context
	 *            context
	 */
	public Workspace(Context context) {
		this(context, null);
	}

	/***
	 * 
	 * @param context
	 *            Context
	 * @param att
	 *            属性集
	 */
	public Workspace(Context context, AttributeSet att) {
		super(context, att);
		mVelocityTracker = new FastVelocityTracker();
		mScroller = new ScreenScroller(context, this, mVelocityTracker);
		mDeskScreenEffector = new CoupleScreenEffector(mScroller, CoupleScreenEffector.PLACE_DESK,
				CoupleScreenEffector.SUBSCREEN_EFFECTOR_TYPE);

		setOvershootAmount(mScrollingBounce);
		setScrollDuration(mScrollingDuration);

		final ViewConfiguration configuration = ViewConfiguration.get(getContext());

		mTouchSlop = configuration.getScaledTouchSlop();
		mSwipTouchSlop = (int) (DrawUtils.sDensity * PAGING_TOUCH_SLOP + 0.5f) * 4;
		mSwipVelocity = configuration.getScaledMinimumFlingVelocity() * 4;
		multiTouchController = new MultiTouchController<Object>(this, false);
		// 多点触摸识别
		mTouchDetector = new MultiTouchDetector(context, this);
		mPaint = new Paint();
		mMaxDistanceForFolderCreation = 0.55f * Utilities.getStandardIconSize(context);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//		mAlarmHandler = new AlarmHandler(context);
		// initCheckShowGuideState();
	}

	/**
	 * 启动判断是否需要弹出桌面帮助提示层向导用户
	 */
	// private void initCheckShowGuideState()
	// {
	// SharedPreferences sharedPreferences = getContext()
	// .getSharedPreferences(IPreferencesIds.USERTUTORIALCONFIG,
	// Context.MODE_PRIVATE);
	// int count = sharedPreferences.getInt(LauncherEnv.SCREEN_CHANGE_COUNT, 0);
	// boolean shouldshowsecondtip =
	// sharedPreferences.getBoolean(LauncherEnv.SHOULD_SHOW_SCREEN_EFFECT_SECOND_TIP,
	// false);
	// if (count > LauncherEnv.SHOW_SCREEN_EFFECT_FIRST_GUIDE_COUNT &&
	// !shouldshowsecondtip)
	// {
	// setmNeedToCheckShowGuide(false);
	// }
	// }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		if (count < 1) {
			return;
		}

		final int height = b - t;
		final int width = r - l;

		if (!mHaveChange) {
			mHaveChange = true;
			mScroller.setLayoutScale(sLayoutScale);
			sPageSpacingX = (int) (getWidth() * (1 - sLayoutScale)) >> 1;
			// 竖直方向上不是居中屏幕
			// mPageSpacingY = (int)(getHeight() * (1 - mLayoutScale)) >> 1;
			sPageSpacingY = 0;
			if (sLayoutScale < 1.0f) {
				// Dock可见情况下，top值较大
				final int topValue = ShortCutSettingInfo.sEnable ? 45 : 5; // 由28改成0，处理隐藏DOCK栏进入添加界面，屏幕预览区域和操作区域有点重叠
				sPageSpacingY = (int) (getHeight() * ((topValue + 0.1f) / 800));
			}
			mScroller.setScreenSize((int) (getWidth() * sLayoutScale) + sPageSpacingX / 2,
					(int) (getHeight() * sLayoutScale));

			mDeskScreenEffector.setScreenGap(sPageSpacingX >> 1);
			mDeskScreenEffector.setTopPadding(sPageSpacingY);
		}

		int childLeft = 0;
		final int childTop = sPageSpacingY;
		final int gap = sPageSpacingX;

		int realWitdh = width;
		int realHeight = height;

		if (mScaleState == STATE_SMALL) {
			realHeight *= sLayoutScale;
			realWitdh *= sLayoutScale;
			childLeft += gap;
		}

		mGridWidth = (int) (CellLayout.getViewOffsetW() * sLayoutScale);
		mGridHeight = (int) (CellLayout.getViewOffsetH() * sLayoutScale);
		mGridRect = new Rect(0, 0, mGridWidth, mGridHeight);

		Resources resources = getResources();
		mSubWidth = (int) (resources.getDimensionPixelSize(R.dimen.cell_width_port) * sLayoutScale);
		mSubHeight = (int) (resources.getDimensionPixelSize(R.dimen.cell_width_port) * sLayoutScale); // 深度为1时则为正方形

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				// child.layout(childLeft, 0, childLeft + width, height);
				child.layout(childLeft, childTop, childLeft + width, childTop + height);
				// childLeft += width;
				childLeft += realWitdh + gap / 2;
			}
		}

		if (mFirstLayout) {
			mFirstLayout = false;
			mScreenA = getCurrentScreen();
			if (mListener != null) {
				mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_ASYN_LOAD_PREVIEW,
						-1, null, null);
			}

			// 设置缩放
			if (mPendingScaleState != null) {
				changeScaleState(mPendingScaleState.mState, sLayoutScale, mPendingScaleState.mAnimate);
				mPendingScaleState = null;
			}
		}
		mScroller.setScreenCount(getChildCount());
		updateSliderIndicator();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScreenWidth = w;
		mGridWidth = CellLayout.getViewOffsetW();
		mGridHeight = CellLayout.getViewOffsetH();
		mScroller.setScreenSize((int) (w * sLayoutScale) + sPageSpacingX / 2,
				(int) (h * sLayoutScale));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		// int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		// final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
		}

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			if (view != null) {
				view.measure(widthMeasureSpec, heightMeasureSpec);
			}
		}
		// boolean isLandscape = widthSize > heightSize;
		// 到目前为止添加模块还不支持横屏，多以去掉横竖屏判断 -by Yugi 2012.7.28
		// mFinalScaleFactor = /*isLandscape ? SCALE_FACTOR_LANDSCAPE :
		// */SCALE_FACTOR_FOR_EDIT_PORTRAIT;
	}

	public boolean isWidgetAtLocationScrollable(int x, int y) {
		// will return true if widget at this position is scrollable.
		// Get current screen from the whole desktop
		final CellLayout currentScreen = getCurrentScreenView();
		if (currentScreen == null) {
			return false;
		}

		int[] cell_xy = new int[2];
		// Get the cell where the user started the touch event
		currentScreen.pointToCellExact(x, y, cell_xy);
		int count = currentScreen.getChildCount();

		// Iterate to find which widget is located at that cell
		// Find widget backwards from a cell does not work with
		// (View)currentScreen.getChildAt(cell_xy[0]*currentScreen.getCountX etc
		// etc); As the widget is positioned at the very first cell of the
		// widgetspace
		for (int i = 0; i < count; i++) {
			View child = currentScreen.getChildAt(i);
			if (child == null) {
				continue;
			}

			Object tag = child.getTag();
			if (tag != null && tag instanceof ScreenAppWidgetInfo) {
				// Get Widget ID
				int id = ((ScreenAppWidgetInfo) tag).mAppWidgetId;

				// Get Layount graphical info about this widget
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
				// Calculate Cell Margins
				int left_cellmargin = lp.cellX;
				int rigth_cellmargin = lp.cellX + lp.cellHSpan;
				int top_cellmargin = lp.cellY;
				int botton_cellmargin = lp.cellY + lp.cellVSpan;
				// See if the cell where we touched is inside the Layout of the
				// widget beeing analized
				if (cell_xy[0] >= left_cellmargin && cell_xy[0] < rigth_cellmargin
						&& cell_xy[1] >= top_cellmargin && cell_xy[1] < botton_cellmargin) {
					boolean isGoWidget = GoWidgetManager.isGoWidget(id);
					boolean isScrollableWidget = isWidgetScrollable(id);
					// if(Build.VERSION.SDK_INT < 14){
					return isGoWidget || isScrollableWidget;
					// }
					// else{
					// // 只要滑动的触摸起点在widget范围内（不区分是否为scroll
					// Widget），都不响应手势，如果有很好的方式可以在4.0以上区分系统widget是否为scroll类型，则可以把上3行代码打开
					// - By Yugi 2012.4.28
					// return true;
					// }
				}
			}
		}
		cell_xy = null;
		return false;
	}

	public void unbindWidgetScrollableViews() {
		unbindWidgetScrollable();
	}

	public void unbindWidgetScrollableViewsForWidget(int widgetId) {
		Log.d(LogConstants.HEART_TAG, "trying to completely unallocate widget ID=" + widgetId);
		unbindWidgetScrollableId(widgetId);
	}

	/***
	 * 绘制指定屏幕的壁纸
	 * 
	 * @param index
	 */
	public void drawScreenBg(Canvas canvas, int index) {
		int count = getChildCount();
		if (index < 0 || index >= count) {
			return;
		}

		int scroll = index * mScreenWidth;
		int temp = mScroller.getScroll();
		mScroller.setScroll(0);
		mScroller.drawBackground(canvas, scroll);
		mScroller.setScroll(temp);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mScroller.invalidateScroll();
		switch (mDrawState) {
			case DRAW_STATE_ALL :
				if (!mScroller.isFinished()) {
					mScroller.onDraw(canvas);
				} else {
					mScroller.drawBackground(canvas, mScroller.getScroll());
					// if(mScaleState == STATE_SMALL){
					// canvas.drawColor(mBackgroudColor);
					// }
					// 当前屏幕处于不绘制状态（如文件夹打开状态）时，workspace也不绘制
					super.dispatchDraw(canvas);
					if (mShowAutoEffect) {
						mShowAutoEffect = false;
						snapToScreen(mNextDestScreen, false, AUTO_EFFECT_TIME);
					}
				}
				break;

			case DRAW_STATE_DISABLE :
				break;

			case DRAW_STATE_ONLY_BACKGROUND :
				mScroller.drawBackground(canvas, mScroller.getScroll());
				// if (mScaleState == STATE_SMALL) {
				// canvas.drawColor(mBackgroudColor);
				// }
				break;

			case DRAW_STATE_NORMAL_TO_SMALLER_ENTERING : {
				mScroller.drawBackground(canvas, mScroller.getScroll());
				drawZoom(canvas, 1 / SCALE_FACTOR_FOR_EDIT_PORTRAIT, 1, 0, 255, getDrawTime());
			}
				break;

			case DRAW_STATE_SMALLER_TO_NORMAL_ENTERING : {
				mScroller.drawBackground(canvas, mScroller.getScroll());
				drawZoom(canvas, SCALE_FACTOR_FOR_EDIT_PORTRAIT, 1, 255, 0, getDrawTime());
			}
				break;

			case DRAW_STATE_ZOOM_IN : {
				mScroller.drawBackground(canvas, mScroller.getScroll());
				drawZoom(canvas, mLastLayoutScale / sLayoutScale, 1, 255, 255, getDrawTime());
			}
				break;

			case DRAW_STATE_ZOOM_OUT : {
				mScroller.drawBackground(canvas, mScroller.getScroll());
				drawZoom(canvas, mLastLayoutScale / sLayoutScale, 1, 255, 255, getDrawTime());
			}
				break;

			default :
				break;
		}
	}

	/*
	 * 获取绘制的当前时间
	 */
	private long getDrawTime() {
		int currentTime = 0;
		if (mStartTime == 0) {
			mStartTime = SystemClock.uptimeMillis();
		} else {
			currentTime = (int) (SystemClock.uptimeMillis() - mStartTime);
		}
		return currentTime;
	}

	/**
	 * 绘制背景
	 * 
	 * @param canvas
	 * @param filterColor
	 */
	public void drawBackground(Canvas canvas, int filterColor) {
	//	canvas.translate(-getScrollX(), 0);
		mScroller.drawCurBackground(canvas, mScroller.getScroll());
	//	canvas.translate(getScrollX(), 0);
		if ((filterColor >>> 24) != 0) {
			canvas.drawColor(filterColor);
		}
	}

	/**
	 * 获取桌面背景
	 * 
	 * @return
	 */
	@Override
	public Drawable getBackground() {
		if (mScroller != null) {
			return mScroller.getBackground();
		}
		return null;
	}

	/**
	 * Wysie: Multitouch methods/events
	 */
	@Override
	public Object getDraggableObjectAtPoint(PointInfo pt) {
		return this;
	}

	@Override
	public void getPositionAndScale(Object obj, PositionAndScale objPosAndScaleOut) {
		objPosAndScaleOut.set(0.0f, 0.0f, true, 1.0f, false, 0.0f, 0.0f, false, 0.0f);
	}

	@Override
	public void selectObject(Object obj, PointInfo pt) {
		mAllowLongPress = false;
	}

	@Override
	public boolean setPositionAndScale(Object obj, PositionAndScale update, PointInfo touchPoint) {
		// 多点触摸前有长按事件，则抛弃此次多点触摸事件
		if (mLongClickView || mLongClickVacant || mMultiTouchOccured) {
			return false;
		}

		float newRelativeScale = update.getScale();

		int targetZoom = (int) Math.round(Math.log(newRelativeScale) * ZOOM_LOG_BASE_INV);
		// Only works for pinch in
		if (targetZoom < 0 && !mShowPreview) {
			// 发生多点触摸，屏幕之后的点击事件
			mMultiTouchOccured = true;
			if (mListener != null) {
				// 取消后期可能产生的长按事件
				mAllowLongPress = false;

				// 跳转到预览界面
				mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_PINCH_IN, 0, null,
						null);
			}
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (mListener != null && !mLongClickView && !mMultiTouchOccured) {
			mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_CLICK_SHORTCUT, 0, v,
					null);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// 判断当前是否锁屏
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(getContext());
			return true;
		}
		if (mScaleState == STATE_SMALL) {
			if (mClickView != null) {
				v = mClickView;
			} else {
				return true;
			}
		}
		vibrate();
		if (mListener == null || !mAllowLongPress || v instanceof FolderView) {
			return false;
		}

		if (v instanceof CellLayout) {
			if (mScaleState == STATE_NORMAL) {
				//解决 (ADT-5116) 打开文件夹时,在文件夹打开的瞬间长按桌面.屏幕添加层也同时出现  add by zzf
				if (null != GoLauncher.getFrame(IDiyFrameIds.DESK_USER_FOLDER_FRAME)
						&& GoLauncher.getFrame(IDiyFrameIds.DESK_USER_FOLDER_FRAME).getVisibility() == View.VISIBLE) {
					return true;
				}
				normalToSmallPreAction(null , null);
			} else {
				smallToNormal(true);
			}
			// 在空白区域长按
			mLongClickVacant = true;
			//用户行为统计
			StatisticsData.countUserActionData(
					StatisticsData.DESK_ACTION_ID_LONG_CLICK_BLANK_SCREEN,
					StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
			return mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_LONG_CLICK, 0, v,
					null);
		}

		// 开始拖动
		if (mTouch) {
			startDrag(v);
		}
		return true;
	}

	// 删除带“+”号的屏幕
	private void removeAddScreen() {
		if (sLayoutScale >= 1.0f) {
			return;
		}
		final int lastIndex = getChildCount() - 1;
		CellLayout layout = (CellLayout) getChildAt(lastIndex);
		if (layout != null && layout instanceof CellLayout) {
			if (layout.getState() == CellLayout.STATE_BLANK_CONTENT) {
				removeScreen(lastIndex);
			}
		} // end if1
	}

	/**
	 * 进入桌面编辑前的准备工作
	 */
	public void normalToSmallPreAction(String editTab , String pkg) {
		final int orientation = OrientationControl.getRequestOrientation(GoLauncher.getContext());
		OrientationControl.setSmallModle(true);
		// 使屏幕保持竖直方向，不能旋转
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				IDiyMsgIds.SCREEN_SET_ORIENTATION, OrientationControl.VERTICAL, null, null);
		// 竖屏情况下直接进去添加模块
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			normalToSmall(editTab , pkg);
		} else {
			mNeedToSmall = true;
		}
	}

	protected void normalToSmall(String editTab , String widgetName) {
		if (sLayoutScale < 1.0f) {
			return;
		}
		// 修复-->增加了 "+"屏后，mCurrentCount改变了，mDstScreen屏幕索引有误
		if ((mCurrentScreen == mScroller.getScreenCount() - 1 || mCurrentScreen == 0) && mCycleMode) {
			mScroller.setCurrentScreen(mCurrentScreen);
		}
		mNeedToSmall = false;
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				IDiyFrameIds.SCREEN_EDIT_BOX_FRAME, null, null);
		HashMap map = new HashMap();
		map.put("editTab", editTab);
		map.put("pkg", widgetName);
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
				IDiyMsgIds.SCREENEDIT_SHOW_ANIMATION_IN, 0, map, null);
		// 屏幕的个数限制为9
		if (getChildCount() < SenseWorkspace.MAX_CARD_NUMS) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ADD_BLANK,
					-1, null, null);
			// 屏幕数目发生变化，发送广播通知多屏多壁纸
			sendBroadcastToMultipleWallpaper(true, true);
		} else {
			sendBroadcastToMultipleWallpaper(false, true);
		}
		changeScaleState(STATE_SMALL, SCALE_FACTOR_FOR_EDIT_PORTRAIT, false);
		initZoomTransition();
		mDrawState = DRAW_STATE_NORMAL_TO_SMALLER_ENTERING;
		mClickView = null;
	}

	/**
	 * 屏幕进行缩放动画前的准备工作
	 * 
	 * @param scaleFactor
	 *            缩放比例
	 */
	protected void prepareToZoom(float scaleFactor) {
		mHaveChange = false;
		setLayoutScale(scaleFactor);
		int screenCount = getChildCount();
		for (int i = 0; i < screenCount; i++) {
			final CellLayout cl = (CellLayout) getChildAt(i);
			cl.setLayoutScale(scaleFactor);
			cl.postInvalidate();
		} // end for
			// 进行非进入或者退出添加模块的动画时，缩放动画的y轴的点为最顶边
			// initZoomTransition();
		mDrawState = sLayoutScale > mLastLayoutScale ? DRAW_STATE_ZOOM_IN : DRAW_STATE_ZOOM_OUT;
		final int width = getWidth();
		mCy = sPageSpacingY;
		mCx = width * sLayoutScale / 2 + sPageSpacingX + mCurrentScreen
				* (width * sLayoutScale + sPageSpacingX / 2);
	}
	
	//用于解决Home键回主屏无过渡动画与防止退出添加界面按home键时,屏幕层会显示罩子层白边的冲突
	public static boolean sFlag = false; //false 普通情况下按home键,不进入smallToNormal方法,
	public void smallToNormal(boolean animation) {
		sFlag = animation;
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.LEAVE_NEW_FOLDER_STATE,
				-1, null, null);
		//防止退出添加界面按home键时,屏幕层会显示罩子层白边svn20985
		if (sLayoutScale >= 1.0f && animation) {
			return;
		}
		removeAddScreen();
		changeScaleState(STATE_NORMAL, 1.0f, false);
		mCx = getWidth() * sLayoutScale / 2 + sPageSpacingX + mCurrentScreen
				* (getWidth() * sLayoutScale + sPageSpacingX / 2);
		// 需要动画
		if (animation) {
			// 让指示器归位
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREENEDIT_WORKSPACE_INDICATOR_DOWN, 1, null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
					IDiyMsgIds.SCREENEDIT_SHOW_ANIMATION_OUT, 0, null, null);
			mDrawState = DRAW_STATE_SMALLER_TO_NORMAL_ENTERING;
			invalidate();
		} else {
			// 让指示器归位
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREENEDIT_WORKSPACE_INDICATOR_DOWN, 0, null, null);
			final int screenCount = getChildCount();
			for (int i = 0; i < screenCount; i++) {
				final CellLayout cl = (CellLayout) getChildAt(i);
				cl.mBackgroudAlpha = 255;
				cl.setDrawBackground(false);
				cl.invalidate();
			}
			resetOrientation();
			// 关闭桌面编辑，下半部分
			GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					IDiyFrameIds.SCREEN_EDIT_BOX_FRAME, null, null);
		}
		// 屏幕数目发生变化，发送广播通知多屏多壁纸
		sendBroadcastToMultipleWallpaper(false, true);
	}

	/**
	 * 设置监听者
	 * 
	 * @param listener
	 *            IMessageHandler
	 */
	public void setListener(IMessageHandler listener) {
		mListener = listener;
	}

	/**
	 * 设置翻屏时间
	 * 
	 * @param duration
	 *            翻屏时间
	 */
	public void setScrollDuration(int duration) {
		mScrollingDuration = duration;
		mScroller.setDuration(duration);
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

	/**
	 * 添加组件到当前屏幕
	 * 
	 * @param child
	 *            自组件
	 * @param x
	 *            组件所在列
	 * @param y
	 *            组件所在行
	 * @param spanX
	 *            宽度所占格数
	 * @param spanY
	 *            高度所占格数
	 */
	void addInCurrentScreen(View child, int x, int y, int spanX, int spanY) {
		addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
	}

	/**
	 * 添加组件到当前屏幕
	 * 
	 * @param child
	 *            自组件
	 * @param x
	 *            组件所在列
	 * @param y
	 *            组件所在行
	 * @param spanX
	 *            宽度所占格数
	 * @param spanY
	 *            高度所占格数
	 * @param insert
	 *            是否插入
	 */
	void addInCurrentScreen(View child, int x, int y, int spanX, int spanY, boolean insert) {
		addInScreen(child, mCurrentScreen, x, y, spanX, spanY, insert);
	}

	/**
	 * 添加组件到指定屏幕
	 * 
	 * @param child
	 *            组件
	 * @param screen
	 *            屏幕索引
	 * @param x
	 *            组件所在列
	 * @param y
	 *            组件所在行
	 * @param spanX
	 *            组件宽度
	 * @param spanY
	 *            组件高度
	 */
	void addInScreen(View child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
		if (child == null || screen < 0 || screen >= getChildCount()) {
			if (child == null) {
				ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_ADDINSCREEN_CHILD_NULL);
			} else if (screen < 0) {
				ScreenMissIconBugUtil
						.showToast(ScreenMissIconBugUtil.ERROR_ADDINSCREEN_SCREEN_SMALLER_0);
			} else if (screen >= getChildCount()) {
				ScreenMissIconBugUtil
						.showToast(ScreenMissIconBugUtil.ERROR_ADDINSCREEN_SCREEN_BIGGER_0);
			}

			return;
		}

		if (x >= mDesktopColumns || y >= mDesktopRows) {
			// NOTE:这里不打印日志调查丢失图标，从多行设置到少行，会走这里
			return;
		}

		// 清空位置信息
		clearVacantCache();

		final CellLayout group = (CellLayout) getChildAt(screen);
		blankCellToNormal(group);
		ViewGroup.LayoutParams lp = child.getLayoutParams();
		if (lp == null || !(lp instanceof CellLayout.LayoutParams)) {
			lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
		} else {
			((CellLayout.LayoutParams) lp).cellX = x;
			((CellLayout.LayoutParams) lp).cellY = y;
			((CellLayout.LayoutParams) lp).cellHSpan = spanX;
			((CellLayout.LayoutParams) lp).cellVSpan = spanY;
		}
		child.setLayoutParams(lp);
		group.addView(child, insert ? 0 : -1, lp);
		child.setOnLongClickListener(this);
		child.setOnClickListener(this);
		//对widget开启硬件加速
		Object info = child.getTag();
		if (null != info
				&& info instanceof ItemInfo
				&& ((ItemInfo) info).mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
			Machine.setHardwareAccelerated(child, Machine.LAYER_TYPE_HARDWARE);
		}
		if (info != null && info instanceof ShortCutInfo
				&& ((ItemInfo) info).mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			AppItemInfo appItemInfo = ((ShortCutInfo) info).getRelativeItemInfo();
			if (appItemInfo != null) {
				CommonControler.getInstance(GOLauncherApp.getContext()).checkShortCutIsIsRecommend(
						appItemInfo); // 检查加入屏幕的快捷方式是否推荐应用
			}
		}
		refreshSubView();
	}

	public void removeInScreen(View child, int screen) {
		if (child == null || screen < 0 || screen >= getChildCount()) {
			if (child == null) {
				ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_ADDINSCREEN_CHILD_NULL);
			}
			return;
		}

		// 清空位置信息
		clearVacantCache();
		final CellLayout group = (CellLayout) getChildAt(screen);
		group.removeView(child);
		group.clearDisappearingChildren();
	}

	void previewDockItem(View child, int screen, int x, int y, int spanX, int spanY) {
		if (child == null || screen < 0 || screen >= getChildCount()) {
			if (child == null) {
				ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_ADDINSCREEN_CHILD_NULL);
			}

			return;
		}
		// 清空位置信息
		clearVacantCache();
		final CellLayout group = (CellLayout) getChildAt(screen);
		ViewGroup.LayoutParams lp = child.getLayoutParams();
		if (lp == null || !(lp instanceof CellLayout.LayoutParams)) {
			lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
		} else {
			((CellLayout.LayoutParams) lp).cellX = x;
			((CellLayout.LayoutParams) lp).cellY = y;
			((CellLayout.LayoutParams) lp).cellHSpan = spanX;
			((CellLayout.LayoutParams) lp).cellVSpan = spanY;
		}
		child.setLayoutParams(lp);
		group.addView(child, -1, lp);
	}

	@Override
	public void addView(View child) {
		if (!(child instanceof CellLayout)) {
			return;
		}
		initCellLayout((CellLayout) child);
		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (!(child instanceof CellLayout)) {
			return;
		}
		initCellLayout((CellLayout) child);
		super.addView(child, index);
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		if (!(child instanceof CellLayout)) {
			return;
		}
		initCellLayout((CellLayout) child);
		super.addView(child, index, params);
	}

	@Override
	public void addView(View child, int width, int height) {
		if (!(child instanceof CellLayout)) {
			return;
		}
		initCellLayout((CellLayout) child);
		super.addView(child, width, height);
	}

	@Override
	public void addView(View child, LayoutParams params) {
		if (!(child instanceof CellLayout)) {
			return;
		}
		initCellLayout((CellLayout) child);
		super.addView(child, params);
	}

	protected void initCellLayout(CellLayout screen) {
		CellLayout.setRows(mDesktopRows);
		CellLayout.setColums(mDesktopColumns);
		CellLayout.setAutoStretch(mAutoStretch);
		screen.setOnLongClickListener(this);
//		screen.enableHardwareLayers();
	}

	/**
	 * 添加屏幕
	 * 
	 * @param screen
	 *            屏幕
	 * @param position
	 *            添加位置
	 */
	public void addScreen(CellLayout screen, int position) {
		addView(screen, position);
		// 更新指示器屏幕总数
		updateIndicatorItems();
		moveItemPositions(position, +1);
	}

	/**
	 * 删除屏幕
	 * 
	 * @param screen
	 *            屏幕id
	 */
	public void removeScreen(int screen) {
		if (screen < 0 || screen >= getChildCount()) {
			return;
		}

		final CellLayout layout = (CellLayout) getChildAt(screen);
		if (layout == null) {
			return;
		}

		int childCount = layout.getChildCount();
		for (int j = 0; j < childCount; j++) {
			final View view = layout.getChildAt(j);
			Object tag = view.getTag();
			// DELETE ALL ITEMS FROM SCREEN
			final ItemInfo item = (ItemInfo) tag;
			if (item instanceof ScreenAppWidgetInfo) {
				final ScreenAppWidgetInfo launcherAppWidgetInfo = (ScreenAppWidgetInfo) item;
				// 删除 widgetId
				if (mListener != null) {
					mListener.handleMessage(this, IMsgType.SYNC,
							IDiyMsgIds.SCREEN_DEL_APPWIDGET_ID, launcherAppWidgetInfo.mAppWidgetId,
							null, null);
				}
			}
		}

		// 屏幕在可循环且当前屏是第一屏时，需要重置当前屏
		boolean refresh = (mCycleMode && mCurrentScreen == 0) ? true : false;
		moveItemPositions(screen, -1);
		removeView(layout);

		int currentScreen = mCurrentScreen;
		if (screen < currentScreen) // 删除的屏幕在当前屏之前，要更新屏幕索引
		{
			currentScreen -= 1;
			refresh = true;
		} else if (screen == currentScreen) // 删除的屏幕就是当前屏，要更新屏幕索引
		{
			currentScreen = 0;
			refresh = true;
		}

		// 确保当前屏在0 ~ max之间
		mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));

		// 以上两种情况或者 屏幕是循环且是最后一屏（循环模式下最后一屏索引为0）也要保存屏幕索引
		if (refresh) {
			setCurrentScreen(mCurrentScreen);
		}

		if (screen <= mMainScreen) {
			int mainScreen = mMainScreen;
			if (screen < mMainScreen) {
				mainScreen -= 1;
			} else if (screen == mMainScreen) {
				mainScreen = 0;
			}
			mMainScreen = Math.max(0, Math.min(mainScreen, getChildCount() - 1));
			if (mListener != null) {
				mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_SET_HOME,
						mMainScreen, null, null);
			}
		}
		updateIndicatorItems();
		// 此方法会重置当前屏幕索引，当为循环时，当前屏为"+"屏时，mCurrentScreen会置为0
		mScroller.setScreenCount(getChildCount()); // 同步更新，不要等到onLayout
		// updateSliderIndicator();
		// updateDotsIndicator(mCurrentScreen);
		
		// 屏幕数目发生变化，发送广播通知多屏多壁纸
		sendBroadcastToMultipleWallpaper(false, true);
	}

	CellLayout.CellInfo findAllVacantCells(boolean[] occupied) {
		final CellLayout group = getCurrentScreenView();
		if (group != null) {
			return group.findAllVacantCells(occupied, null);
		}
		return null;
	}

	/**
	 * 指定屏是否有子元素
	 * 
	 * @param screenId
	 *            屏ID
	 */
	public boolean hasChildElement(int screenId) {
		CellLayout child = (CellLayout) getChildAt(screenId);
		return child != null && child.getChildCount() > 0;
	}

	/**
	 * Unlocks the SlidingDrawer so that touch events are processed.
	 * 
	 * @see #lock()
	 */
	public void unlock() {
		mLocked = false;
	}

	/**
	 * Locks the SlidingDrawer so that touch events are ignores.
	 * 
	 * @see #unlock()
	 */
	public void lock() {
		mLocked = true;
	}

	public boolean getTouchState() {
		return mTouch;
	}

	public void allowTouch() {
		mTouch = true;
	}

	public void unTouch() {
		mTouch = false;
	}

//	public void buildChildHardwareLayers() {
//		if (getWindowToken() != null) {
//			ViewCompat.buildChildrenLayer(this);
//		}
//	}
//
//	private void updateChildrenLayersEnabled() {
//		final int count = getChildCount();
//		for (int i = 0; i < count; i++) {
//			ViewCompat.setChildrenLayersEnabled((ViewGroup) getChildAt(i), true);
//		}
//	}

	void buildChildrenDrawingCache() {
//		if (ViewCompat.isHardwareAccelerated(this)) {
//			updateChildrenLayersEnabled();
//		} else {
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final CellLayout layout = (CellLayout) getChildAt(i);
				layout.buildChildrenDrawingCache();
			}
//		}
	}

	void destroyChildrenDrawingCache(boolean keepCurrentScreen) {
		final int count = getChildCount();
		final int cur = getCurrentScreen();
		final int prev = mScroller.getPreviousScreen();
		final int next = mScroller.getNextScreen();
		for (int i = 0; i < count; i++) {
			if (keepCurrentScreen) {
				if (i == prev || i == cur || i == next) {
					continue;
				}
			}
			final CellLayout layout = (CellLayout) getChildAt(i);
			layout.destroyChildrenDrawingCache();
		}
		// System.gc();
	}

	/**
	 * 设置默认屏幕
	 * 
	 * @param screen
	 *            屏幕
	 */
	public void setMainScreen(int screen) {
		if (screen >= getChildCount() || screen < 0) {
			Log.i(LOG_TAG, "Cannot reset default screen to " + screen);
			return;
		}
		mMainScreen = screen;
	}

	/**
	 * 
	 * @return 当前主屏
	 */
	public int getMainScreen() {
		return mMainScreen;
	}

	/**
	 * @return 当前是否显示默认屏幕
	 */
	public boolean isMainScreenShowing() {
		return mCurrentScreen == mMainScreen;
	}

	/**
	 * 获取当前显示的屏幕id
	 * 
	 * @return 当前屏幕.
	 */
	public int getCurrentScreen() {
		return mCurrentScreen = mScroller.getDstScreen();
	}

	/**
	 * 设置当前屏幕
	 * 
	 * @param currentScreen
	 *            当前屏幕id.
	 */
	public void setCurrentScreen(int currentScreen) {
		clearVacantCache();
		mCurrentScreen = currentScreen;
		mScroller.setCurrentScreen(currentScreen);
	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		if (mDrawState != DRAW_STATE_ALL) {
			return false;
		}

		if (direction == View.FOCUS_LEFT) {
			snapToScreen(mCurrentScreen - 1, false, -1);
			return true;
		} else if (direction == View.FOCUS_RIGHT) {
			snapToScreen(mCurrentScreen + 1, false, -1);
			return true;
		}
		return super.dispatchUnhandledMove(focused, direction);
	}

	// @Override
	// public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
	// boolean immediate)
	// {
	// final int screen = indexOfChild(child);
	// if (screen != mCurrentScreen || !mScroller.isFinished())
	// {
	// if (!mLocked)
	// {
	// snapToScreen(screen, true, -1);
	// }
	// return true;
	// }
	// return false;
	// }

	@Override
	protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
		if (mDrawState != DRAW_STATE_ALL) {
			return false;
		}

		final FolderView openFolder = getOpenFolder();
		if (openFolder != null) {
			return openFolder.requestFocus(direction, previouslyFocusedRect);
		} else {
			int dstScreen = mScroller.getDstScreen();
			View child = getChildAt(dstScreen);
			if (child != null) {
				return child.requestFocus(direction, previouslyFocusedRect);
			}
		}
		return false;
	}

	@Override
	public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
		if (mDrawState != DRAW_STATE_ALL) {
			return;
		}

		final FolderView openFolder = getOpenFolder();
		final CellLayout currentScreen = getCurrentScreenView();
		if (currentScreen == null) {
			return;
		}

		if (openFolder == null) {
			currentScreen.addFocusables(views, direction);
			views.add(currentScreen);

			int count = getChildCount();
			int pre = mCycleMode ? (mCurrentScreen - 1 + count) % count : mCurrentScreen - 1;
			int next = mCycleMode ? (mCurrentScreen + 1) % count : mCurrentScreen + 1;
			if (direction == View.FOCUS_LEFT) {
				if (pre >= 0 && pre < count) {
					View leftChild = getChildAt(pre);
					leftChild.addFocusables(views, direction);
					views.add(leftChild);
				}
			} else if (direction == View.FOCUS_RIGHT) {
				if (next >= 0 && next < count) {
					View rightChild = getChildAt(next);
					rightChild.addFocusables(views, direction);
					views.add(rightChild);
				}
			}
		} else {
			openFolder.addFocusables(views, direction);
		}
	}

	//单指和双指滑动会有冲突
	private boolean mSingleTouch = false; //是否响应单指上下滑的手势
	private boolean mDoubleTouch = false; //是否响应双击事件

	private int mRecordD = 0;
	private long mRecordTime = 0;
	private static final int PRESSED_SPACE = 300; // 双击间隔

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			mIsTouchUp = true;
		} else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			mIsTouchUp = false;
		}
		if (mShowPreview) {
			return true;
		}
		if (mListener
				.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_MENU_SHOW, 0, null, null)) {
			return true;
		}
		// 如果是多点触摸，直接截获事件
		if (mTouchDetector.onTouchEvent(ev)) {
			return true;
		}
		
		// 如果是多点触摸，直接截获事件，不再传给子视图，然后在onTouchEvent里面处理后续的多点触摸事件
		if (!mDoubleTouch && !mSingleTouch && multiTouchController.onTouchEvent(ev)) {
			return true;
		}

		if (mLocked) {
			return true;
		}
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_RESET)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				// 一接触屏幕操作就取消彩蛋消息
//				cancleAlarm();
				mDoubleTouch = false;
				mSingleTouch = false;
				mEditView = null;
				mClickView = null;
				mInterceptTouchDownX = x;
				mInterceptTouchDownY = y;
				mInterceptTouchMoveX = 0;
				mInterceptTouchMoveY = 0;
				mInterceptTouchMoved = false;
				mAllowLongPress = true;
				mLongClickVacant = false;
				mLongClickView = false;

				mMultiTouchOccured = false;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESET : TOUCH_STATE_SCROLLING;
				mTouchedScrollableWidget = isWidgetAtLocationScrollable((int) mInterceptTouchDownX,
						(int) mInterceptTouchDownY);

				mTouchDownTime = SystemClock.uptimeMillis();
				float[] matchPoint = new float[] { x - sPageSpacingX, y - sPageSpacingY };
				final float mapX = matchPoint[0] / sLayoutScale;
				final float mapY = matchPoint[1] / sLayoutScale;
				int[] cellXY = new int[2];
				CellLayout layout = getCurrentScreenView();
				if (mScaleState == STATE_SMALL) {
					if (layout != null) {
						if (layout.pointToCellExact((int) mapX, (int) mapY, cellXY)) {
							View child = layout.getChildViewByCell(cellXY);
							if (child != null) {
								mClickView = child;
							} else {
								if (layout.getState() == STATE_NORMAL) {
									mClickView = layout;
								}
							} // end else
						}
					} // end if layout
				} else {
					// 双击是否设置了响应
					boolean isValid = mListener.handleMessage(this, IMsgType.SYNC,
							IDiyMsgIds.SCREEN_DOUBLE_CLICK_VALID, 0, null, null);

					if (isValid) {
						// 双击手势判断
						boolean isClickView = false;
						if (layout != null) {
							if (layout.pointToCellExact((int) mapX, (int) mapY, cellXY)) {
								View child = layout.getChildViewByCell(cellXY);
								if (child != null) {
									isClickView = true;
								}
							}
						}

						if (!isClickView
								&& (SystemClock.uptimeMillis() - mRecordTime) < PRESSED_SPACE) {
							mRecordD++;
						} else {
							mRecordD = 0;
						}
						mRecordTime = SystemClock.uptimeMillis();
						if (mRecordD >= 1 && mScroller.isFinished()) {
							mDoubleTouch = true;
							mRecordD = 0;
							mListener.handleMessage(this, IMsgType.SYNC,
									IDiyMsgIds.SCREEN_DOUBLE_CLICK, 0, null, null);
							return true;
						}
					}
				}

				if (mTouch) {
					mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				mVelocityTracker.addMovement(ev);
				mVelocityTracker.computeCurrentVelocity(1000);
				mInterceptTouchVY = mVelocityTracker.getYVelocity();

				if (!mInterceptTouchMoved) {
					// 一旦超出拖动范围不会再更新，作为初始的拖动斜率
					mInterceptTouchMoveX = Math.abs(x - mInterceptTouchDownX);
					mInterceptTouchMoveY = Math.abs(y - mInterceptTouchDownY);
					mInterceptTouchMoved = mInterceptTouchMoveX > mTouchSlop
							|| mInterceptTouchMoveY > mTouchSlop;
				}

				if (!mLongClickVacant && mInterceptTouchMoved) {
					mRecordTime = 0;
					if (mLongClickView) {
						mLongClickView = false;
						if (mListener != null) {
							mListener.handleMessage(this, IMsgType.SYNC,
									IDiyMsgIds.SCREEN_CANCEL_LONG_CLICK, 0, null, null);
						}
					} else if (getOpenFolder() == null && !mMultiTouchOccured) {
						// 当前屏幕没有打开的文件夹，不是多点触摸，则处理滑屏事件
						if (mInterceptTouchMoveY > mInterceptTouchMoveX) {
							// 竖向滑动，判断是否有widget处理上下滑动事件，没有则判断是否发生上划下划手势
							if (!mTouchedScrollableWidget) {
								if (Math.abs(y - mInterceptTouchDownY) > mSwipTouchSlop
										&& mListener != null && mScaleState == STATE_NORMAL) {
									mSingleTouch = true;
									if (mInterceptTouchVY > mSwipVelocity) {
										mTouchState = TOUCH_SWIPE_DOWN_GESTURE;
										mListener.handleMessage(this, IMsgType.SYNC,
												IDiyMsgIds.SCREEN_SLIDE_DOWN, 0, null, null);
									} else if (mInterceptTouchVY < -mSwipVelocity) {
										mTouchState = TOUCH_SWIPE_UP_GESTURE;
										mListener.handleMessage(this, IMsgType.SYNC,
												IDiyMsgIds.SCREEN_SLIDE_UP, 0, null, null);
									}
								}
							}
						} else {
							// 横向滑动
							mTouchState = TOUCH_STATE_SCROLLING;
							if (mTouch) {
								mScroller.onTouchEvent(ev, action);
							}
						}
					}

					if (mAllowLongPress) {
						mAllowLongPress = false;
						final View currentScreen = getCurrentScreenView();
						if (currentScreen != null) {
							currentScreen.cancelLongPress();
						}
					}
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				mSingleTouch = false;
				mDoubleTouch = false;
				if (mScaleState == STATE_SMALL) {
					// mClickView.onTouchEvent(ev);
					// resizeWidget();
					if (mClickView != null && mClickView instanceof CellLayout) {
						if (((CellLayout) mClickView).mState == CellLayout.STATE_NORMAL_CONTENT) {
							smallToNormal(true);
						}
					} else if (mClickView != null
							&& (SystemClock.uptimeMillis() - mTouchDownTime) <= PRESSED_STATE_DURATION) {
						// 点击屏幕任何位置均响应退出编辑操作，去掉点击图标或小部件无响应的交互形式
						smallToNormal(true);
					}
				}
				boolean shortFling = false; // 在阈值距离之内发生的急促甩动
				if (mTouchState == TOUCH_STATE_RESET && !mInterceptTouchMoved) {
					final CellLayout currentScreen = getCurrentScreenView();
					if (currentScreen != null && !currentScreen.lastDownOnOccupiedCell()) {
						getLocationOnScreen(mTempCell);
						// Send a tap to the wallpaper if the last down was on
						// empty space
						if (mListener != null) {
							Bundle bundle = WallpaperControler.createWallpaperCommandBundle(
									mTempCell[0] + (int) ev.getX(), mTempCell[1] + (int) ev.getY());

							mListener.handleMessage(this, IMsgType.SYNC,
									IDiyMsgIds.SEND_WALLPAPER_COMMAND, -1, bundle, null);
							bundle = null;
						}
					}
					// 如果水平滑动速度不足以切屏，还需要判断竖向是否发生急促甩动（防止图标被点击，但是不促发手势，因为距离太短）
					if (mTouch) {
						shortFling = mScroller.onTouchEvent(ev, action)
								|| Math.abs(mScroller.getFlingVelocityY()) > mSwipVelocity;
					} else {
						shortFling = false;
					}

				}

				mTouchState = TOUCH_STATE_RESET;
				mAllowLongPress = false;
				// 动作结束后开始重新计时
//				startAlarm();
				if (mInterceptTouchMoved && !mTouchedScrollableWidget) {
					// 要拦截掉上下滑动，否则widget有可能会当作点击事件，
					// 在这里直接返回true，不用增加新状态
					return true;
				}
				if (shortFling) {
					return true;
				}
				break;
			}

			default :
				break;
		}
		return mTouchState != TOUCH_STATE_RESET;
	}

	private float mTouchMoveX;
	private float mTouchMoveY;
	private float mTouchDownX;
	private float mTouchDownY;
	private boolean mTouchMoved;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			mIsTouchUp = true;
		} else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			mIsTouchUp = false;
		}
		if (mLocked || mShowPreview || !mTouch) {
			return true;
		}
		// 如果是多点触摸，直接截获事件
		if (mTouchDetector.onTouchEvent(ev)) {
			return true;
		}
		
		if (!mDoubleTouch && !mSingleTouch && mScaleState == STATE_NORMAL
				&& multiTouchController.onTouchEvent(ev)) {
			return true;
		}

				
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				// 一接触屏幕操作就取消彩蛋消息
//				cancleAlarm();
				mTouchDownX = x;
				mTouchDownY = y;
				// 上一次滚屏还没结束就触屏，在这里收到ACTION_DOWN事件
				if (mTouchState == TOUCH_STATE_SCROLLING) {
					mScroller.onTouchEvent(ev, action);
				}
				break;
			}

			case MotionEvent.ACTION_MOVE : {

				if (!mInterceptTouchMoved) {
					// 一旦超出拖动范围不会再更新，作为初始的拖动斜率
					mTouchMoveX = Math.abs(x - mTouchDownX);
					mTouchMoveY = Math.abs(y - mTouchDownY);
					mTouchMoved = mTouchMoveX > mTouchSlop || mTouchMoveY > mTouchSlop;
				}
				if (mTouchMoved) {
					mRecordTime = 0;
				}
				if (mTouchState == TOUCH_STATE_SCROLLING) {
					mScroller.onTouchEvent(ev, action);
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				mLongClickView = false;
				mLongClickVacant = false;
				if (mTouchState == TOUCH_STATE_SCROLLING) {
					mScroller.onTouchEvent(ev, action);
				}
				mTouchState = TOUCH_STATE_RESET;
				// 一接触屏幕操作就取消彩蛋消息
//				startAlarm();
				break;
			}

			default :
				break;
		}

		return true;
	}

	public void adjustCurrentScreen() // 已废弃
	{
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState state = new SavedState(super.onSaveInstanceState());
		state.mCurrentScreen = mCurrentScreen;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		try {
			SavedState savedState = (SavedState) state;
			super.onRestoreInstanceState(savedState.getSuperState());
			if (savedState.mCurrentScreen != -1) {
				setCurrentScreen(savedState.mCurrentScreen);
			}
		} catch (Exception e) {
			super.onRestoreInstanceState(null);
			// Log.d("WORKSPACE",
			// "Google bug http://code.google.com/p/android/issues/detail?id=3981 found, bypassing...");
		}
	}

	/**
	 * 查找组件所在屏幕索引
	 * 
	 * @param v
	 *            组件
	 * @return 屏幕索引
	 */
	public int getScreenForView(View v) {
		int result = -1;
		if (v != null) {
			ViewParent vp = v.getParent();
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				if (vp == getChildAt(i)) {
					return i;
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * 类描述:保存临时的数据
	 * 功能详细描述:
	 * 
	 * @date  [2012-9-7]
	 */
	public static class SavedState extends BaseSavedState {
		int mCurrentScreen = -1;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			mCurrentScreen = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(mCurrentScreen);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	/**
	 * @return 行数
	 */
	public int getDesktopRows() {
		return mDesktopRows;
	}

	/**
	 * @return 列数
	 */
	public int getDesktopColumns() {
		return mDesktopColumns;
	}

	/**
	 * 
	 * @return 单屏幕的格子总数
	 */
	public int getGridCount() {
		return mGridCount;
	}

	public void setDesktopRowAndCol(int row, int col) {
		if (row >= 0 && col >= 0) {
			mDesktopRows = row;
			mDesktopColumns = col;
			mGridCount = row * col;
			CellLayout.setRows(row);
			CellLayout.setColums(col);

			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				CellLayout layout = (CellLayout) getChildAt(i);
				if (layout != null) {
					layout.requestLayout();
				}
			}
			requestLayout();
		}
	}

	/**
	 * 
	 * @return 当前屏幕View
	 */
	public CellLayout getCurrentScreenView() {
		return (CellLayout) getChildAt(getCurrentScreen());
	}

	/**
	 * 通过索引拿cellLayout
	 * 
	 * @param screenindex
	 * @return
	 */
	public CellLayout getScreenView(int screenindex) {
		if (screenindex < 0 || screenindex >= getChildCount()) {
			return null;
		}

		return (CellLayout) getChildAt(screenindex);
	}

	/**
	 * 计算放手后图标的位置
	 * 
	 * @param pixelX
	 *            图标x位置
	 * @param pixelY
	 *            图标y位置
	 * @param spanX
	 *            图标水平宽度
	 * @param spanY
	 *            图标水平宽度
	 * @param ignoreView
	 *            忽略的组件
	 * @param layout
	 *            某个屏幕的信息
	 * @param recycle
	 * @return 可以放置的位置
	 */
	public int[] estimateDropCell(int pixelX, int pixelY, int spanX, int spanY, View ignoreView,
			CellLayout layout, int[] recycle) {
		if (layout == null) {
			return null;
		}
		// Create vacant cell cache if none exists
		if (mVacantCache == null) {
			mVacantCache = layout.findAllVacantCells(null, ignoreView);
		}

		// Find the best target drop location
		return layout.findNearestVacantArea(pixelX, pixelY, spanX, spanY, mVacantCache, recycle);
	}

	/**
	 * 
	 * 计算放手后图标的位置
	 * 
	 * @param pixelX
	 *            图标x位置
	 * @param pixelY
	 *            图标y位置
	 * @param spanX
	 *            图标水平宽度
	 * @param spanY
	 *            图标水平宽度
	 * @param ignoreView
	 *            忽略的组件
	 * @param screenIndex
	 *            屏幕索引
	 * @param recycle
	 * @return 可以放置的位置
	 */
	public int[] estimateDropCell(int pixelX, int pixelY, int spanX, int spanY, View ignoreView,
			int screenIndex, int[] recycle) {

		if (screenIndex < 0 || screenIndex >= getChildCount()) {
			return null;
		}

		if (screenIndex != getCurrentScreen()) {
			clearVacantCache();
		}

		CellLayout screen = (CellLayout) getChildAt(screenIndex);
		return estimateDropCell(pixelX, pixelY, spanX, spanY, ignoreView, screen, recycle);
	}

	protected int[] getDropCell(int pixelX, int pixelY, int spanX, int spanY, View ignoreView,
			int screenIndex, int[] recycle) {

		if (screenIndex < 0 || screenIndex >= getChildCount()) {
			return null;
		}

		if (screenIndex != getCurrentScreen()) {
			clearVacantCache();
		}

		CellLayout screen = (CellLayout) getChildAt(screenIndex);
		int[] resultSpan = new int[2];
		return screen.createArea(pixelX, pixelY, spanX, spanY, spanX, spanY, ignoreView,
				mTargetCell, resultSpan, CellLayout.MODE_ON_DROP);
	}

	// /**
	// *
	// * @return 获取当前正在编辑的view
	// */
	// public View editView()
	// {
	// return mEditView;
	// }

	// 拖拽item进行放大动画
	private void zoomIn(final View view, final float scale, final CellLayout parent, final List list) {
		try {
			lock();
			// Animate the view into the correct position
			ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
			animator.setDuration(150);
			animator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float value = (Float) animation.getAnimatedValue();

					CompatibleUtil.setScaleX(view, 1 + (value * (scale - 1)));
					CompatibleUtil.setScaleY(view, 1 + (value * (scale - 1)));

					if (view.getParent() == null) {
						animation.cancel();
						CompatibleUtil.unwrap(view);
					} else {
						CompatibleUtil.setTranslationX(view, CompatibleUtil.getTranslationX(view));
						CompatibleUtil.setTranslationY(view, CompatibleUtil.getTranslationY(view));
					}
				}
			});
			animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					CompatibleUtil.unwrap(view);
					handleDrag(parent, list);
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
					CompatibleUtil.unwrap(view);
					handleDrag(parent, list);
				}
			});
			animator.start();

		} catch (Throwable e) {
			handleDrag(parent, list);
		}
	}

	/**
	 * 开始拖拽图标
	 */
	private void startDrag(View target) {
		mEditView = target;
		if (target == null) {
			return;
		}

		mEditView.clearFocus();
		mEditView.setPressed(false);
		mEditView.postInvalidate();
		mLongClickView = true;

		CellLayout parent = getCurrentScreenView();
		if (parent == null) {
			return;
		}

		// 清除缓存
		destroyChildrenDrawingCache(true);
		// 将抓起位置标记为不占据
		parent.markCellsAsUnoccupiedForView(mEditView);
		setCurrentDropLayout(parent);
		if (mPreventFC) {
			OutOfMemoryHandler.handle();
		}

		mDragInfo = parent.getTag();

		if (mDragInfo != null) {
			parent.clearVisualizeDropLocation();
			// 清除图标投影， 改变图标点击效果后的代码
			// parent.clearVisualizeDropLocation();

			mDragInfo.screen = mCurrentScreen;
			ArrayList<Object> list = new ArrayList<Object>();
			if (mScaleState == STATE_SMALL) {
				if ((mEditView.getVisibility()) == VISIBLE && mEditView.getParent() == parent) {
					final CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mEditView
							.getLayoutParams();
					mDragInfo.cell = mEditView;
					mDragInfo.cellX = lp.cellX;
					mDragInfo.cellY = lp.cellY;
					mDragInfo.spanX = lp.cellHSpan;
					mDragInfo.spanY = lp.cellVSpan;
					mDragInfo.valid = true;
				}
				list.add(sLayoutScale);
				list.add(sPageSpacingX);
				list.add(sPageSpacingY);
				handleDrag(parent, list);
			} else {
				final float scaleDps = DrawUtils.dip2px(12);
				final float scale = (mEditView.getWidth() + scaleDps) / mEditView.getWidth();
				zoomIn(mEditView, scale, parent, null);
			}
		}
	}

	private void handleDrag(CellLayout parent, List list) {
		unlock();
		boolean ret = mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.DRAG_START,
				mListener.getId(), mEditView, list);
		if (ret) {
			lock();
			setEditViewVisible(false);
			notifyWidgetVisible(false);
			postInvalidate();
		} else {
			unlock();
		}

		// 桌面加载还未完成时，长按有时会引起空指针，故加保护
		try {
			// 一开始抓起图标就画投影
			parent.caculateCellXY(mEditView, new int[] { mDragInfo.cellX, mDragInfo.cellY },
					(int) mTouchDownX, (int) mTouchDownY);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//ADT-9694 长按桌面widget会出现该widget重叠的情况
		if (mIsTouchUp) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_SHOW_QUICK_ACTION_MENU, -1, mEditView, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					IDiyFrameIds.DRAG_FRAME, null, null);
		}

	}

	/**
	 * 计算是否可以接受当前拖动的位置
	 * 
	 * @param x
	 *            组件x位置(px)
	 * @param y
	 *            组件y位置(px)
	 * @param xOffset
	 *            x方向上的偏移
	 * @param yOffset
	 *            y方向上的偏移
	 * @param screenIndex
	 *            目标屏幕索引
	 * @return 可以放置返回true
	 */
	public boolean acceptDrop(int x, int y, int xOffset, int yOffset, int screenIndex) {
		final CellLayout layout = (CellLayout) getChildAt(screenIndex);
		final CellLayout.CellInfo cellInfo = mDragInfo;
		final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
		final int spanY = cellInfo == null ? 1 : cellInfo.spanY;

		if (mVacantCache == null) {
			final View ignoreView = cellInfo == null ? null : cellInfo.cell;
			mVacantCache = layout.findAllVacantCells(null, ignoreView);
		}

		return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY, false);
	}

	/**
	 * 安放图标
	 * 
	 * @param x
	 *            组件x坐标
	 * @param y
	 *            组件y坐标
	 * @param xy
	 *            更新后的网格位置，由外界申请空间
	 * @param screenIndex
	 *            目标屏幕索引
	 * @param move
	 *            是否移动图标，false 无需更新数据库
	 * @param scale
	 *            放大系数，处理放下后的动画
	 * @param animation
	 *            放下动画
	 */
	public boolean drop(int x, int y, int[] xy, int screenIndex, boolean[] move, float scale,
			boolean animate) {
		if (mDragInfo == null || mDragInfo.cell == null) {
			return false;
		}
		// int realX = x;
		// int realY = y;
		int centerX = (int) (x + mDragInfo.cell.getWidth() * sLayoutScale / 2);
		int centerY = (int) (y + mDragInfo.cell.getHeight() * sLayoutScale / 2);
		if (mScaleState == STATE_SMALL) {
			float[] realXY = new float[2];
			// 先转换为真实值
			// virtualPointToReal(x, y, realXY);
			// realX = (int) realXY[0];
			// realY = (int) realXY[1];
			virtualPointToReal(centerX, centerY, realXY);
			centerX = (int) realXY[0];
			centerY = (int) realXY[1];
		}

		move[0] = false;
		final View cell = mDragInfo.cell;
		// final float diffRatio = (scale - 1) * 0.5f;
		// // 计算放大后的视图按中心点缩小到原来大小后的左上角位置
		// final int x2 = realX + (int)(diffRatio * cell.getWidth());
		// final int y2 = realY + (int)(diffRatio * cell.getHeight());

		final CellLayout cellLayout = (CellLayout) getChildAt(screenIndex);

		int[] resultSpan = new int[2];
		mTargetCell = cellLayout.createArea(centerX, centerY, mDragInfo.spanX, mDragInfo.spanY,
				mDragInfo.spanX, mDragInfo.spanY, cell, mTargetCell, resultSpan,
				CellLayout.MODE_ON_DROP);

		// 计算目标网格,看是否有空间可以放置
		// mTargetCell = estimateDropCell(x2, y2, mDragInfo.spanX,
		// mDragInfo.spanY,
		// cell, cellLayout, mTargetCell);

		final boolean foundCell = mTargetCell != null && mTargetCell[0] >= 0 && mTargetCell[1] >= 0;
		// 找不到
		if (!foundCell) {
			clearVacantCache();
			ViewParent parent = cell.getParent();
			// 需要找到拖拽view的父容器，然后将该位置置为已占用
			if (parent != null && parent instanceof CellLayout) {
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
				mTargetCell[0] = lp.cellX;
				mTargetCell[1] = lp.cellY;
				((CellLayout) parent).markCellsAsOccupiedForView(cell);
			}
			return false;
		}

		if (screenIndex != mDragInfo.screen) {
			ViewParent viewParent = cell.getParent();
			if (viewParent != null) {
				((ViewGroup) viewParent).removeView(cell);
			}
			blankCellToNormal(cellLayout);
			cellLayout.addView(cell);
			move[0] = true;
		}

		cellLayout.onDropChild(cell, mTargetCell);
		cellLayout.clearVisualizeDropLocation();

		mDragging = false;
		Animation animation = null;
		if (animate) {
			// 开始动画
			int[] point = new int[2];
			cellLayout.cellToPoint(mTargetCell[0], mTargetCell[1], point);
			if (mScaleState == STATE_SMALL) {
				float[] virtualXY = new float[2];
				// 先转换为真实值
				realPointToVirtual(point[0], point[1], virtualXY);
				point[0] = (int) virtualXY[0];
				point[1] = (int) virtualXY[1];
			}
			cellLayout.bringChildToFront(cell); // 放到最前面，防止从其他cell底下穿过
			animation = MyAnimationUtils.getRectAnimation(0, 0, x - point[0], y - point[1], scale,
					true, -1); // 注意动画是相对于子视图来说的，区域1的左上角应为(0,0)
			mAnimationType = ANIMATION_DROP;
			if (animation != null) {
				animation.setAnimationListener(this);
				cell.startAnimation(animation);
			}
			point = null;
		}

		// 动态壁纸响应放下事件
		if (mListener != null) {
			getLocationOnScreen(mTempCell);
			Bundle bundle = new Bundle();
			bundle.putString(WallpaperControler.COMMAND, WallpaperControler.COMMAND_DROP);
			final int dropX = mTargetCell[0] * CellLayout.sCellRealWidth
					+ CellLayout.sCellRealWidth / 2;
			final int dropY = mTargetCell[1] * CellLayout.sCellRealHeight
					+ CellLayout.sCellRealHeight / 2;
			bundle.putInt(WallpaperControler.FIELD_COMMAND_X, mTempCell[0] + dropX);
			bundle.putInt(WallpaperControler.FIELD_COMMAND_Y, mTempCell[1] + dropY);
			mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SEND_WALLPAPER_COMMAND, -1,
					bundle, null);
			bundle = null;
		}

		if (mTargetCell[0] != mDragInfo.cellX || mTargetCell[1] != mDragInfo.cellY) {
			move[0] = true;
		}

		xy[0] = mTargetCell[0];
		xy[1] = mTargetCell[1];

		// 动画没有启动时，直接完成
		if (animation == null) {
			onAnimationEnd(null);
			showStatusbar();
		}
		return true;
	}

	/**
	 * 取消编辑状态
	 */
	public void clearDragState() {
		unlock();
		setEditViewVisible(true);
		clearVacantCache();
		mTouchState = TOUCH_STATE_RESET;
		mLongClickVacant = false;
		mLongClickView = false;
		mEditView = null;
		
		if (mDragInfo != null) {
			// 防止内存泄漏
			mDragInfo.cell = null;
			mDragInfo = null;
		}
		setCurrentDropLayout(null);
	}

	public void setEditViewVisible(boolean visible) {
		if (mEditView != null) {
			mEditView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		}
	}

	// /**
	// * 发生屏幕方向的切换
	// * @param change true发生切换
	// */
	// public void changeOrientation(boolean change)
	// {
	// mChangeOrientation = change;
	// }
	//
	/**
	 * 设置图标自动拉伸
	 * 
	 * @param autoStretch
	 *            true自动拉伸
	 */
	public void setAutoStretch(boolean autoStretch) {
		if (mAutoStretch != autoStretch) {
			mAutoStretch = autoStretch;
			CellLayout.setAutoStretch(mAutoStretch);
			CellLayout child = null;
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				child = (CellLayout) getChildAt(i);
				child.requestLayout();
			}
		}
	}

	private void dropComplete() {
		unlock();
		clearVacantCache();
		mDragInfo = null;
		notifyWidgetVisible(true);
		if (getChildAt(mCurrentScreen) != null) {
			getChildAt(mCurrentScreen).invalidate();
		}
		mLastReorderX = -1;
		mLastReorderY = -1;
		
	}

	private void clearVacantCache() {
		if (mVacantCache != null) {
			mVacantCache.clearVacantCells();
			mVacantCache = null;
		}
	}

	private void updateWallpaperOffset(int scroll) {
		if (mListener == null || !mWallpaperScrollEnabled) {
			return;
		}

		final int count = getChildCount();
		if (count <= 0) {
			return;
		}

		final int scrollRange = getChildAt(count - 1).getLeft();
		if (scroll >= 0 && scroll <= scrollRange) {
			Bundle dataBundle = WallpaperControler.createWallpaperOffsetBundle(
					count, scroll, scrollRange);
			mListener.handleMessage(this, IMsgType.SYNC,
					IDiyMsgIds.UPDATE_WALLPAPER_OFFSET, -1, dataBundle, null);
			dataBundle = null;
		}
	}

	private void moveItemPositions(int screen, int diff) {
		// MOVE THE REMAINING ITEMS FROM OTHER SCREENS
		int screenNum = getChildCount();
		for (int i = screen + 1; i < screenNum; i++) {
			final CellLayout layout = (CellLayout) getChildAt(i);
			layout.setScreen(layout.getScreen() + diff);
		}
	}

	/***
	 * 指示器相关
	 */
	private void updateIndicatorItems() {
		if (mListener == null) {
			return;
		}

		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.TOTAL, getChildCount());
		mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_UPDATE_INDICATOR,
				DesktopIndicator.UPDATE_SCREEN_NUM, dataBundle, null);
		dataBundle = null;
	}

	private synchronized void updateSliderIndicator() {
		if (mListener == null) {
			return;
		}

		final int offset = mScroller.getIndicatorOffset();
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.OFFSET, offset);

		mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_UPDATE_INDICATOR,
				DesktopIndicator.UPDATE_SLIDER_INDICATOR, dataBundle, null);

		dataBundle = null;
	}

	private void updateDotsIndicator(final int current) {
		if (mListener == null) {
			return;
		}

		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.CURRENT, current);
		mListener.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_UPDATE_INDICATOR,
				DesktopIndicator.UPDATE_DOTS_INDICATOR, dataBundle, null);
		dataBundle = null;
	}

	/**
	 * 获取所有打开的文件夹
	 * 
	 * @return 所有打开的文件夹视图列表
	 */
	public List<FolderView> getAllOpenFolders() {
		final List<FolderView> list = new ArrayList<FolderView>();
		final int screenSize = getChildCount();
		for (int i = 0; i < screenSize; i++) {
			CellLayout currentScreen = (CellLayout) getChildAt(i);
			if (currentScreen == null) {
				continue;
			}

			final int count = currentScreen.getChildCount();
			for (int j = 0; j < count; j++) {
				View child = currentScreen.getChildAt(j);
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
				if (lp.cellHSpan == mDesktopColumns && lp.cellVSpan == mDesktopRows
						&& child instanceof FolderView) {
					list.add((FolderView) child);
				}
			}
		}
		return list;
	}

	/**
	 * @return 返回当前屏幕打开的文件夹
	 */
	FolderView getOpenFolder() {
		if (mCurrentScreen >= getChildCount()) {
			return null;
		}

		final CellLayout currentScreen = getCurrentScreenView();
		if (currentScreen == null) {
			return null;
		}

		int count = currentScreen.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = currentScreen.getChildAt(i);
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
			if (lp.cellHSpan == mDesktopColumns && lp.cellVSpan == mDesktopRows
					&& child instanceof FolderView) {
				return (FolderView) child;
			}
		}
		return null;
	}

	/**
	 * @return 返回所有屏幕的文件夹
	 */
	ArrayList<FolderView> getOpenFolders() {
		final int screens = getChildCount();
		ArrayList<FolderView> folders = new ArrayList<FolderView>(screens);

		for (int screen = 0; screen < screens; screen++) {
			CellLayout currentScreen = (CellLayout) getChildAt(screen);
			int count = currentScreen.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = currentScreen.getChildAt(i);
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
				if (lp.cellHSpan == mDesktopColumns && lp.cellVSpan == mDesktopRows
						&& child instanceof FolderView) {
					folders.add((FolderView) child);
					break;
				}
			}
		}

		return folders;
	}

	/***
	 * 通过ID找到对应的文件夹图标
	 * 
	 * @return
	 */
	public synchronized View getFolderByRefId(long refId) {
		int screenCount = getChildCount();
		for (int i = 0; i < screenCount; i++) {
			getFolderByRefId(refId, i);
		}

		return null;
	}

	public synchronized View getFolderByRefId(long refId, int index) {
		if (index < 0 || index >= getChildCount()) {
			return null;
		}
		ViewGroup viewGroup = (ViewGroup) getChildAt(index);
		if (null == viewGroup) {
			return null;
		}

		int viewCount = viewGroup.getChildCount();

		for (int j = 0; j < viewCount; j++) {
			View view = viewGroup.getChildAt(j);
			if (null == view || !(view instanceof FolderIcon)) {
				continue;
			}

			Object obj = view.getTag();
			if (null != obj && obj instanceof UserFolderInfo) {
				UserFolderInfo info = (UserFolderInfo) obj;
				if (info.mRefId == refId || info.mInScreenId == refId) {
					return view;
				}
			}
		}
		return null;
	}

	/***
	 * 查找文件夹所在屏幕的索引
	 * 
	 * @return
	 */
	public synchronized int findScreenIndex(long refId) {
		int screenCount = getChildCount();
		for (int i = 0; i < screenCount; i++) {
			ViewGroup viewGroup = (ViewGroup) getChildAt(i);
			if (null == viewGroup) {
				continue;
			}

			int viewCount = viewGroup.getChildCount();

			for (int j = 0; j < viewCount; j++) {
				View view = viewGroup.getChildAt(j);
				if (null == view || !(view instanceof FolderIcon)) {
					continue;
				}

				Object obj = view.getTag();
				if (null != obj && obj instanceof UserFolderInfo) {
					UserFolderInfo info = (UserFolderInfo) obj;
					if (info.mRefId == refId || info.mInScreenId == refId) {
						return i;
					}
				}
			}
		}

		return -1;
	}

	/**
	 * 
	 * @param tag
	 *            文件夹的数据
	 * @return 对应的文件夹
	 */
	public FolderView getFolderForTag(Object tag) {
		int screenCount = getChildCount();
		for (int screen = 0; screen < screenCount; screen++) {
			CellLayout currentScreen = (CellLayout) getChildAt(screen);
			int count = currentScreen.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = currentScreen.getChildAt(i);
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
				if (lp.cellHSpan == mDesktopColumns && lp.cellVSpan == mDesktopRows
						&& child instanceof FolderView) {
					FolderView f = (FolderView) child;
					if (f.getInfo() == tag) {
						return f;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 获取某个屏幕的文件夹位置信息
	 * 
	 * @param screenIndex
	 *            屏幕索引
	 * @return 文件夹位置列表
	 */
	// public ArrayList<Rect> getFolderRects(int screenIndex, View ignoreView)
	// {
	// if(screenIndex < 0 || screenIndex >= getChildCount())
	// {
	// return null;
	// }
	// CellLayout group = (CellLayout)getChildAt(screenIndex);
	// int topExtra = 0;
	// if(mFirstGetFolderRects && mDragging && !StatusBarHandler.isHide()){
	// topExtra = StatusBarHandler.getStatusbarHeight();
	// mFirstGetFolderRects = false;
	// }
	// ArrayList<Rect> rectList = group.getFolderRrects(ignoreView, topExtra);
	//
	// return rectList;
	// }

	/**
	 * 长按震动
	 */
	private void vibrate() {
		mVibrator.vibrate(VIBRATE_DURATION);
		//		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
		//				HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
	}

	// public void setShowPreview(boolean showPreview)
	// {
	// mShowPreview = showPreview;
	// if(mShowPreview){
	// setDrawState(DRAW_STATE_ONLY_BACKGROUND);
	// }
	// else{
	// setDrawState(DRAW_STATE_ALL);
	// }
	// }

	@Override
	public Activity getLauncherActivity() {
		return null;
	}

	/**
	 * 
	 * @param enable
	 *            true 不使用缓存绘制
	 */
	public void setPreventFC(boolean enable) {
		mPreventFC = enable;
	}

	@Override
	public void onScrollStart() {
		notifyWidgetVisible(false);
		clearVacantCache();
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		updateWallpaperOffset(newScroll);
		updateSliderIndicator();
		prepareSubView();
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		// 更新指示器
		mCurrentScreen = newScreen;
		updateDotsIndicator(mCurrentScreen);
		prepareSubView();
		// checkShowGuide();
		checkGridState(newScreen, oldScreen);
	}

	// private void checkShowGuide()
	// {
	// if (mNeedToCheckShowGuide)
	// {
	// boolean isTop = GoLauncher.sendMessage(this,
	// IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FRAME_IS_TOP,
	// -1, null, null);
	// if (!isTop)
	// {
	// //不在顶层，不处理
	// return;
	// }
	//
	// SharedPreferences sharedPreferences = getContext()
	// .getSharedPreferences(IPreferencesIds.USERTUTORIALCONFIG,
	// Context.MODE_PRIVATE);
	// int count = sharedPreferences.getInt(LauncherEnv.SCREEN_CHANGE_COUNT, 1);
	// boolean shouldshowsecondtip =
	// sharedPreferences.getBoolean(LauncherEnv.SHOULD_SHOW_SCREEN_EFFECT_SECOND_TIP,
	// false);
	// if (count == LauncherEnv.SHOW_SCREEN_EFFECT_FIRST_GUIDE_COUNT)
	// {
	// //NOTE：第一个提示
	// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_SCREEN_EFFECT);
	// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
	// IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
	// }else if (count > LauncherEnv.SHOW_SCREEN_EFFECT_FIRST_GUIDE_COUNT
	// && count % LauncherEnv.SHOW_SCREEN_EFFECT_SECONDE_GUIDE_COUNT == 0 &&
	// shouldshowsecondtip)
	// {
	// //NOTE:第二个提示
	// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_SCREEN_EFFECT_SECOND_TIP);
	// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
	// IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
	//
	// //进入时先设置为下次不弹出第二次提示
	// SharedPreferences.Editor editor = sharedPreferences.edit();
	// editor.putBoolean(LauncherEnv.SHOULD_SHOW_SCREEN_EFFECT_SECOND_TIP,
	// false);
	// editor.commit();
	// }else if (count > LauncherEnv.SHOW_SCREEN_EFFECT_FIRST_GUIDE_COUNT &&
	// !shouldshowsecondtip)
	// {
	// setmNeedToCheckShowGuide(false);
	// }
	// count++;
	// SharedPreferences.Editor editor = sharedPreferences.edit();
	// editor.putInt(LauncherEnv.SCREEN_CHANGE_COUNT, count);
	// editor.commit();
	// }
	// }

	@Override
	public void onScrollFinish(int currentScreen) {
		CellLayout cell = getCurrentScreenView();
		// 从桌面预览回来的时候桌面也在滚动，如果禁用绘图缓冲会变慢，因此加入了状态限制
		if (cell != null) {
//			cell.openWidgetHardwareAccelerated();
//			cell.enableWidgetHardwareAccelerated(true);
			if (mDrawState == DRAW_STATE_ALL) {
				cell.setChildrenDrawnWithCacheEnabled(false);
			}
		}

		if (mSaveMomoryWhenIdle) {
			destroyChildrenDrawingCache(true); // 释放绘图缓冲
		}
		
		notifyWidgetVisible(true);
		setDrawQuality(true);
		
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.RECOMMEND_ICON_SHANK, currentScreen, null, null);
	}

	@Override
	public void onFlingStart() {
		notifyWidgetVisible(false);
		setDrawQuality(false);
	}

	/**
	 * 
	 * @param screen
	 * @param noElastic
	 *            是否使用弹性效果
	 * @param duration
	 *            小于0则自动计算时间
	 */
	public void snapToScreen(int screen, boolean noElastic, int duration) {
		buildChildrenDrawingCache();
		mScroller.gotoScreen(screen, duration, noElastic);
		prepareSubView();
	}

	/**
	 * 设置背景
	 * 
	 * @param drawable
	 * @param offset
	 */
	public void setWallpaper(Drawable drawable, int offset) {
		// drawable为null表示有动态壁纸，背景一定被绘制了
		mScroller.setBackgroundAlwaysDrawn(drawable == null);
		mScroller.setBackground(drawable);
		mScroller.setScreenOffsetY(offset);
		postInvalidate();
	}

	public void setWallpaperYOffset(int offset) {
		mScroller.setScreenOffsetY(offset);
	}

	public void setEffector(int type) {
		if (mEffectorType == type && type != CoupleScreenEffector.EFFECTOR_TYPE_RANDOM_CUSTOM) {
			return;
		}
		mEffectorType = type;
		mDeskScreenEffector.setType(type);
	}

	public void setCustomRandomEffectorEffects(int[] effects) {
		mDeskScreenEffector.setDeskCustomRandomEffects(effects);
	}

	public void setCycleMode(boolean cycle) {
		if (mCycleMode != cycle) {
			mCycleMode = cycle;
			ScreenScroller.setCycleMode(this, cycle);
			mDeskScreenEffector.onAttachReserveEffector(this);
		}
	}

	public void setWallpaperScroll(boolean enabled) {
		mWallpaperScrollEnabled = enabled;
		mScroller.setBackgroundScrollEnabled(enabled);
	}

	/**
	 * 设置绘制哪些内容
	 * 
	 * @param state
	 *            {@link #DRAW_STATE_ALL} 表示背景和视图， {@link #DRAW_STATE_DISABLE}
	 *            表示不绘制任何东西， {@link #DRAW_STATE_ONLY_BACKGROUND} 表示只绘制背景。
	 */
	public void setDrawState(int state) {
		switch (state) {
			case DRAW_STATE_ALL :
			case DRAW_STATE_DISABLE :
			case DRAW_STATE_ONLY_BACKGROUND :
				mDrawState = state;
				break;
		}
	}

	/*
	 * 拖拽结束后，设置为非全屏
	 */
	protected void showStatusbar() {
		if (!StatusBarHandler.isHide() && Workspace.getLayoutScale() >= 1.0f) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
			// 显示指示器
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SHOW_INDICATOR, -1,
					null, null);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mAnimationType == ANIMATION_DROP) {
			mAnimationType = ANIMATION_NONE;
			dropComplete();
			showStatusbar();
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void drawScreen(Canvas canvas, int screen) {
		if (mScroller.getCurrentDepth() != 0) {
			mScroller.setDepthEnabled(false);
		}
		CellLayout cell = (CellLayout) getChildAt(screen);
		if (cell != null) {
			if (!mScroller.isFinished()) {
				cell.buildChildrenDrawingCache();
				cell.draw(canvas);
			}
		}

	}

	@Override
	public void drawScreen(Canvas canvas, int screen, int alpha) {
		if (mScroller.getCurrentDepth() != 0) {
			mScroller.setDepthEnabled(false);
		}
		CellLayout cell = (CellLayout) getChildAt(screen);
		if (cell != null) {
			if (!mScroller.isFinished()) {
				cell.buildChildrenDrawingCache();
				canvas.save();
				if (mScaleState == STATE_SMALL) {
					cell.drawCenterCross(canvas);
					canvas.scale(sLayoutScale, sLayoutScale);
					cell.drawBackground(canvas);
				}
				final int count = cell.getChildCount();
				for (int i = 0; i < count; i++) {
					View child = cell.getChildAt(i);
					if (child != null) {
						int oldAlpha = 255;
						final int saveCount = canvas.save();
						if (child instanceof BubbleTextView) {
							BubbleTextView bubbleTextView = (BubbleTextView) child;
							oldAlpha = bubbleTextView.getAlphaValue();
							if (alpha != oldAlpha) {
								bubbleTextView.setAlphaValue(alpha);
							}
							canvas.translate(child.getLeft(), child.getTop());
							bubbleTextView.draw(canvas);
							if (oldAlpha != alpha) {
								bubbleTextView.setAlphaValue(oldAlpha);
							}
						} else {
							Bitmap icon = child.getDrawingCache();
							boolean willDraw = child.willNotCacheDrawing();
							if (icon == null && willDraw) {
								child.setWillNotCacheDrawing(false);
								icon = child.getDrawingCache();
							}
							if (icon != null) {
								oldAlpha = mPaint.getAlpha();
								if (oldAlpha != alpha) {
									mPaint.setAlpha(alpha);
								}
								canvas.drawBitmap(icon, child.getLeft(), child.getTop(), mPaint);
								if (oldAlpha != alpha) {
									mPaint.setAlpha(oldAlpha);
								}
							} // end icon
							child.setWillNotCacheDrawing(willDraw);
						}
						canvas.restoreToCount(saveCount);
					} // end child
				} // end for
				canvas.restore();
			}
		}

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
		setDrawQuality(true);
	}

	// 通知widget 进入/离开屏幕
	private void notifyWidgetVisible(boolean visible) {
		// Log.i("luoph", visible ? "OnEnter": "onLeave");
		int msgid = visible
				? IDiyMsgIds.SCREEN_FIRE_WIDGET_ONENTER
				: IDiyMsgIds.SCREEN_FIRE_WIDGET_ONLEAVE;
		mListener.handleMessage(this, IMsgType.SYNC, msgid, getCurrentScreen(), null, null);
	}

	// 取消通知widget 进入/离开屏幕
	// private void cancelNotifyWidgetVisible(boolean visible)
	// {
	// Log.i("luoph", visible ? "cancel OnEnter": "cancel onLeave");
	// int msgid = visible ? IDiyMsgIds.SCREEN_CANCEL_WIDGET_ONENTER:
	// IDiyMsgIds.SCREEN_CANCEL_WIDGET_ONLEAVE;
	// mListener.handleMessage(this, IMsgType.SYNC, msgid, -1, null, null);
	// }

	/**
	 * 获取通知widget的runnable
	 * 
	 * @param visible
	 * @return
	 */
	public WidgetRunnable getWidgetRunnable(boolean visible) {
		return visible ? mEnterRunnable : mLeaveRunnable;
	}

	/**
	 * 
	 * 类描述:操作widget可见性
	 * 功能详细描述:
	 * 
	 * @date  [2012-9-7]
	 */
	public static class WidgetRunnable implements Runnable {
		ViewGroup mViewGroup = null;
		boolean mVisible;

		public WidgetRunnable(boolean visible) {
			mVisible = visible;
		}

		void setScreen(ViewGroup viewGroup) {
			mViewGroup = viewGroup;
		}

		@Override
		public void run() {
			if (mViewGroup != null) {
				final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
				if (null != widgetManager) {
					widgetManager.fireVisible(mViewGroup, mVisible);
				}
				mViewGroup = null;
			}
		}
	}

	/**
	 * 
	 * @return 相应View的索引
	 */
	private int getCurViewIndex(CellLayout screenLayout, int cellX, int cellY, int[] style) {
		int count = screenLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			final View childView = screenLayout.getChildAt(i);
			final ItemInfo itemInfo = (ItemInfo) childView.getTag();
			if (childView instanceof BubbleTextView) {
				if (itemInfo.mCellX == cellX && itemInfo.mCellY == cellY) {
					style[0] = 1;
					return i;
				}
			} else if (itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET
					|| itemInfo.mItemType == IItemType.ITEM_TYPE_FAVORITE) {
				// 目标区域落在上面
				if (itemInfo.mCellX + itemInfo.mSpanX - 1 >= cellX
						&& itemInfo.mCellY + itemInfo.mSpanY - 1 >= cellY
						&& itemInfo.mCellX <= cellX && itemInfo.mCellY <= cellY) {
					style[0] = -2;
					return i;
				}

			}
		}
		return -1;
	}

	@Override
	public void drawGridCell(Canvas canvas, int index) {
		final int realIndex = index % mGridCount;
		final int screen = index / mGridCount;
		CellLayout layout = (CellLayout) getChildAt(screen);

		final int culumn = realIndex % mDesktopColumns; // 第几列
		final int row = realIndex / mDesktopColumns; // 第几行

		if (layout != null) {
			if (layout.getChildCount() < 1 || null == mCurGridsState || null == mNextGridsState) {
				return;
			}
			final int length = mDesktopRows * mDesktopColumns;
			if (mCurGridsState.length != length || mNextGridsState.length != length) {
				prepareSubView();
			}
			Point gridPoint = null;
			if (screen == mScreenA) {
				gridPoint = mCurGridsState[realIndex];
			} else if (screen == mScreenB) {
				gridPoint = mNextGridsState[realIndex];
			}

			if (null == gridPoint || gridPoint.x == 0) {
				return;
			}

			// 如果是单个格子
			if (gridPoint.x == 1) {
				View view = layout.getChildAt(gridPoint.y);
				if (null == view || view.getVisibility() != VISIBLE) { // 应用特效时，滑屏不显示隐藏图标
					return;
				}
				canvas.save();
				canvas.translate(-culumn * mGridWidth, -row * mGridHeight);
				if (!mScroller.isFinished()) {
					// layout.buildChildrenDrawingCache();
					toDrawChild(canvas, view, getDrawingTime());
				} else if (view.getVisibility() == View.VISIBLE || view.getAnimation() != null) {
					toDrawChild(canvas, view, getDrawingTime());
				}
				canvas.restore();
			} else if (gridPoint.x == -2) {
				View view = layout.getChildAt(gridPoint.y);
				if (null == view || view.getVisibility() != VISIBLE) { // 应用特效时，滑屏不显示隐藏图标
					return;
				}
				float depth = mScroller.getCurrentDepth(); // 获取当前深度

				final float clipWidth = Math.min(mGridWidth, mGridWidth - (mGridWidth - mSubWidth)
						* depth);
				final float clipHeight = Math.min(mGridHeight, mGridHeight
						- (mGridHeight - mSubHeight) * depth);

				final float scale_x = clipWidth / mGridWidth;
				final float scale_y = clipHeight / mGridHeight;
				canvas.save();
				canvas.scale(scale_x, scale_y);
				canvas.clipRect(CellLayout.getLeftPadding(), CellLayout.getTopPadding(), mGridWidth
						+ CellLayout.getLeftPadding(), mGridHeight + CellLayout.getTopPadding());
				canvas.translate(-culumn * mGridWidth + (mGridWidth - clipWidth) / 2, -row
						* mGridHeight + (mGridHeight - clipHeight) / 2);
				if (!mScroller.isFinished()) {
					toDrawChild(canvas, view, getDrawingTime());
				}
				canvas.restore();

				// final View view = layout.getChildAt(gridPoint.y);
				// if(null == view)
				// {
				// return ;
				// }
				// if (!mScroller.isFinished())
				// {
				// layout.buildChildrenDrawingCache();
				// Machine.setHardwareAccelerated(this,
				// Machine.LAYER_TYPE_SOFTWARE);
				// }
				// final Bitmap icon = view.getDrawingCache();
				// if(null == icon)
				// {
				// return ;
				// }
				// final float depth = mScroller.getCurrentDepth();// 获取当前深度
				//
				// final float clipWidth = Math.min(mGridWidth, mGridWidth -
				// (mGridWidth - mSubWidth)* depth);
				// final float clipHeight = Math.min(mGridHeight, mGridHeight -
				// (mGridHeight - mSubHeight)* depth);
				// canvas.save();
				// canvas.scale(clipWidth / mGridWidth, clipHeight /
				// mGridHeight);
				// canvas.translate( CellLayout.getLeftPadding() + (mGridWidth -
				// clipWidth) / 2, CellLayout.getTopPadding() + (mGridHeight -
				// clipHeight) / 2);
				// final ItemInfo itemInfo = (ItemInfo)view.getTag();
				// canvas.drawBitmap(icon,
				// new Rect((culumn - itemInfo.mCellX) * mGridWidth,
				// (row - itemInfo.mCellY) * mGridHeight,
				// (culumn - itemInfo.mCellX + 1) * mGridWidth,
				// (row - itemInfo.mCellY + 1) * mGridHeight),
				// new Rect(0, 0, mGridWidth, mGridHeight), mPaint
				// );
				//
				// canvas.restore();
			}
		}
	}

	@Override
	public void drawGridCell(Canvas canvas, int index, int alpha) {
		final int realIndex = index % mGridCount;
		final int screen = index / mGridCount;
		CellLayout layout = (CellLayout) getChildAt(screen);

		final int culumn = realIndex % mDesktopColumns; // 第几列
		final int row = realIndex / mDesktopColumns; // 第几行

		if (layout != null) {
			if (layout.getChildCount() < 1 || null == mCurGridsState || null == mNextGridsState) {
				return;
			}
			final int length = mDesktopRows * mDesktopColumns;
			if (mCurGridsState.length != length || mNextGridsState.length != length) {
				prepareSubView();
			}
			Point gridPoint = null;
			if (screen == mScreenA) {
				gridPoint = mCurGridsState[realIndex];
			} else if (screen == mScreenB) {
				gridPoint = mNextGridsState[realIndex];
			}

			if (null == gridPoint || gridPoint.x == 0) {
				return;
			}
			// 如果是单个格子
			if (gridPoint.x == 1) {
				final View view = layout.getChildAt(gridPoint.y);
				if (null == view || view.getVisibility() != VISIBLE) { // 应用特效时，滑屏不显示隐藏图标
					return;
				}
				// if (!mScroller.isFinished())
				// {
				// layout.buildChildrenDrawingCache();
				// }
				// if(view instanceof BubbleTextView){
				// BubbleTextView child = (BubbleTextView) view;
				// final int oldAlpha = child.getAlphaValue();
				// if (oldAlpha != alpha)
				// {
				// child.setAlphaValue(alpha);
				// }
				// canvas.save();
				// // canvas.translate( -culumn * mGridWidth, -row * mGridHeight
				// );
				// if (!mScroller.isFinished())
				// {
				// // layout.buildChildrenDrawingCache();
				// // toDrawChild(canvas, view, getDrawingTime());
				// canvas.translate(CellLayout.getLeftPadding(),
				// CellLayout.getTopPadding());
				// child.draw(canvas);
				// }
				// canvas.restore();
				// if (oldAlpha != alpha)
				// {
				// child.setAlphaValue(oldAlpha);
				// }
				// }
				// else {
				final Bitmap icon = view.getDrawingCache();
				if (null == icon) {
					return;
				}
				canvas.save();
				// canvas.clipRect(CellLayout.getLeftPadding(),
				// CellLayout.getTopPadding(),
				// mGridWidth + CellLayout.getLeftPadding(), mGridHeight +
				// CellLayout.getTopPadding());
				canvas.translate(-culumn * mGridWidth, -row * mGridHeight);
				final int oldAlpha = mPaint.getAlpha();
				boolean isDiff = oldAlpha != alpha;
				if (isDiff) {
					mPaint.setAlpha(alpha);
				}
				canvas.scale(sLayoutScale, sLayoutScale);
				canvas.drawBitmap(icon, view.getLeft(), view.getTop(), mPaint);
				canvas.restore();
				if (isDiff) {
					mPaint.setAlpha(oldAlpha);
				}
			} else if (gridPoint.x == -2) {
				final View view = layout.getChildAt(gridPoint.y);
				if (null == view || view.getVisibility() != VISIBLE) { // 应用特效时，滑屏不显示隐藏图标
					return;
				}
				final Bitmap icon = view.getDrawingCache();
				if (null == icon) {
					return;
				}
				final float depth = mScroller.getCurrentDepth(); // 获取当前深度
				final float scale_x = Math.min(mGridWidth, mGridWidth - (mGridWidth - mSubWidth)
						* depth)
						/ mGridWidth;
				final float scale_y = Math.min(mGridHeight, mGridHeight
						- (mGridHeight - mSubHeight) * depth)
						/ mGridHeight;
				canvas.save();
				mMatrix.setValues(new float[] { scale_x * sLayoutScale, 0,
						2 * CellLayout.getLeftPadding() * sLayoutScale, 0, scale_y * sLayoutScale,
						2 * CellLayout.getTopPadding() * sLayoutScale, 0, 0, 1 });
				canvas.concat(mMatrix);
				final int oldAlpha = mPaint.getAlpha();
				boolean isDiff = oldAlpha != alpha;
				if (isDiff) {
					mPaint.setAlpha(alpha);
				}
				final ItemInfo itemInfo = (ItemInfo) view.getTag();
				mDestRect.set((culumn - itemInfo.mCellX) * mGridWidth, (row - itemInfo.mCellY)
						* mGridHeight, (culumn - itemInfo.mCellX + 1) * mGridWidth, (row
						- itemInfo.mCellY + 1)
						* mGridHeight);
				canvas.drawBitmap(icon, mDestRect, mGridRect, mPaint);
				canvas.restore();
				if (isDiff) {
					mPaint.setAlpha(oldAlpha);
				}
			}
		}
	}

	@Override
	public int getCellCount() {
		return mGridCount * getChildCount();
	}

	@Override
	public int getCellWidth() {
		if (mGridWidth == 0) {
			mGridWidth = CellLayout.getViewOffsetW();
		}
		return mGridWidth;
	}

	@Override
	public int getCellHeight() {
		if (mGridHeight == 0) {
			mGridHeight = CellLayout.getViewOffsetH();
		}
		return mGridHeight;
	}

	@Override
	public int getCellRow() {
		return mDesktopRows;
	}

	@Override
	public int getCellCol() {
		return mDesktopColumns;
	}

	public View getEditView() {
		return mEditView;
	}

	public void setDrawQuality(boolean high) {
		int quality = high
				? SubScreenEffector.DRAW_QUALITY_HIGH
				: SubScreenEffector.DRAW_QUALITY_LOW;
		mDeskScreenEffector.setDrawQuality(quality);
	}

	private void changeGridState(Point[] grids, int screen) {
		final int gridCount = grids.length;
		for (int i = 0; i < gridCount; i++) {
			grids[i] = new Point(0, 0);
		}
		final CellLayout layout = (CellLayout) getChildAt(screen);
		if (layout == null) {
			return;
		}
		if (!mScroller.isFinished()) {
			layout.buildChildrenDrawingCache();
		}
		int count = layout.getChildCount();
		for (int i = 0; i < count; i++) {
			View subView = layout.getChildAt(i);
			if (null == subView) {
				continue;
			}
			final ItemInfo itemInfo = (ItemInfo) subView.getTag();
			if (null == itemInfo || itemInfo.mCellX < 0 || itemInfo.mCellY < 0) {
				continue;
			}

			// 初始位置
			final int index = mDesktopColumns * itemInfo.mCellY + itemInfo.mCellX;
			if (index >= gridCount || index < 0) {
				continue;
			}
			if (subView instanceof BubbleTextView) {
				grids[index].x = 1;
				grids[index].y = i;
			} else if (itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET
					|| itemInfo.mItemType == IItemType.ITEM_TYPE_FAVORITE) {
				if (itemInfo.mSpanX == 1 && itemInfo.mSpanY == 1) {
					grids[index].x = 1;
					grids[index].y = i;
					continue;
				}
				grids[index].x = -2;
				grids[index].y = i;
				for (int j = 0; j < itemInfo.mSpanX; j++) {
					for (int k = 0; k < itemInfo.mSpanY; k++) {
						final int subIndex = index + mDesktopColumns * k + j;
						if (subIndex >= 0 && subIndex < gridCount) {
							grids[subIndex].x = -2;
							grids[subIndex].y = i;
						}
					}
				}
			}
		} // end for
	}

	// 内部调用
	private void prepareSubView() {
		// 如果当前特效不是单元格特效，那就什么也不做
		if (mDeskScreenEffector.getmType() < CoupleScreenEffector.SUBSCREEN_EFFECTOR_COUNT_IN_DESK) {
			return;
		}
		final int length = mDesktopRows * mDesktopColumns;
		if (null == mCurGridsState || mCurGridsState.length != length) {
			mCurGridsState = new Point[length];
			// 设为较大的负数，是为了一定进入下面的changeGridState
			mScreenA = -101;
		}
		if (null == mNextGridsState || mNextGridsState.length != length) {
			mNextGridsState = new Point[length];
			mScreenB = -102;
		}

		if (mScreenA != mScroller.getDrawingScreenA()) {
			mScreenA = mScroller.getDrawingScreenA();
			changeGridState(mCurGridsState, mScreenA);
		}
		if (mScreenB != mScroller.getDrawingScreenB()) {
			mScreenB = mScroller.getDrawingScreenB();
			changeGridState(mNextGridsState, mScreenB);
		}
	} // end prepareSubView

	// 供外部调用
	public void refreshSubView() {
		// 如果当前特效不是单元格特效，那就什么也不做
		if (mDeskScreenEffector.getmType() < CoupleScreenEffector.SUBSCREEN_EFFECTOR_COUNT_IN_DESK) {
			return;
		}
		mScreenA = mScroller.getDrawingScreenA();
		final int length = mDesktopRows * mDesktopColumns;
		if (null == mCurGridsState || mCurGridsState.length != length) {
			mCurGridsState = new Point[length];
		}
		changeGridState(mCurGridsState, mScreenA);

		mScreenB = mScroller.getDrawingScreenB();
		if (null == mNextGridsState || mNextGridsState.length != length) {
			mNextGridsState = new Point[length];
		}
		changeGridState(mNextGridsState, mScreenB);
	}

	public void setmAutoStretch(boolean mAutoStretch) {
		this.mAutoStretch = mAutoStretch;
	}

	public boolean getmAutoStretch() {
		return mAutoStretch;
	}

	public void setmNeedToCatch(boolean mNeedToCatch) {
		this.mNeedToCatch = mNeedToCatch;
	}

	public void refreshScreenIndex() {
		int screenNum = getChildCount();
		for (int i = 0; i < screenNum; i++) {
			final CellLayout layout = (CellLayout) getChildAt(i);
			layout.setScreen(i);
			layout.updateItemInfoScreenIndex();
		}
	}

	private void toDrawChild(Canvas canvas, View childView, long drawingTime) {
		canvas.save();
		if (mScaleState == STATE_SMALL) {
			canvas.scale(sLayoutScale, sLayoutScale);
		}
		if (mNeedToCatch) {
			try {
				drawChild(canvas, childView, drawingTime);
			} catch (NullPointerException e) {
				// TODO: handle exception
			}
		} else {
			drawChild(canvas, childView, drawingTime);
		}
		canvas.restore();
	}

	// public void setmNeedToCheckShowGuide(boolean bool)
	// {
	// mNeedToCheckShowGuide = bool;
	// }

	public void mworkRequest() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			if (view != null) {
				view.requestLayout();
				view.postInvalidate();
			}
		}
	}

	/*-------------------------------以下code为缩放模式-------------------------------------*/

	// workspace的状态
	public static final int STATE_NORMAL = 0; // 默认
	public static final int STATE_SMALL = 1; // 缩小（编辑界面）

	private int mScaleState = STATE_NORMAL;
	private PendingScaleState mPendingScaleState;
	// workspace 缩放比例
	public static final float SCALE_FACTOR_FOR_ADD_APP_PORTRAIT = 0.415f; // 竖屏添加app时界面的缩放比例
	public static final float SCALE_FACTOR_FOR_ADD_GOWIDGET_PORTRAIT = 0.515f; // 竖屏添加widget时界面的缩放比例
	public static final float SCALE_FACTOR_FOR_EDIT_PORTRAIT = 0.715f; // 竖屏添加模块的缩放比例
	private static final float SCALE_FACTOR_LANDSCAPE = 0.682f; // 横屏缩放比例
	protected static float sLayoutScale = 1.0f;
	private float mLastLayoutScale = 1.0f;
	// 页间距
	protected static int sPageSpacingX;
	protected static int sPageSpacingY;
	private boolean mHaveChange = false;
	private static final int ANIMDURATION = 450;
	private float mCx;
	private float mCy;
	private long mStartTime = 0;
	protected boolean mNeedToSmall = false; // 屏幕加载完之后是否需要进入编辑模式的标识
	private long mTouchDownTime; // 点击下去的当时时间
	private static final int PRESSED_STATE_DURATION = 125; // 单击图标的响应时间差

	/**
	 * 
	 * 类描述:缩放状态数据
	 * 功能详细描述:
	 * 
	 * @date  [2012-9-7]
	 */
	private static class PendingScaleState {
		int mState;
		boolean mAnimate;
	}

	public void changeScaleState(int state, float scaleFactor, boolean animate) {
		mHaveChange = false;
		if (mFirstLayout) {
			/**
			 * 横竖屏切换发生在编辑界面时，还没有进行onLayout 等onLayout完成后再更改缩放状态
			 */
			PendingScaleState pendingScaleState = new PendingScaleState();
			pendingScaleState.mState = state;
			pendingScaleState.mAnimate = animate;
			mPendingScaleState = pendingScaleState;
			return;
		}

		mScaleState = state;
		boolean isSmall = mScaleState == STATE_SMALL;
		setLayoutScale(scaleFactor);
		// if (isSmall) {
		// setLayoutScale(mFinalScaleFactor);
		// }
		// else {
		// setLayoutScale(1.0f);
		// }

		int screenCount = getChildCount();
		// final float finalScaleFactor = isSmall ? mFinalScaleFactor : 1.0f;
		final float finalScaleFactor = scaleFactor;
		for (int i = 0; i < screenCount; i++) {
			final CellLayout cl = (CellLayout) getChildAt(i);
			cl.setLayoutScale(finalScaleFactor);
			if (isSmall) {
				cl.setDrawBackground(true);
			}
			if (i == mCurrentScreen) {
				cl.setDrawCross(isSmall);
			}
			cl.postInvalidate();
		}
		// 调整当前屏幕位置
		// snapToScreen(mCurrentScreen, false, -1);
	}

	/**
	 * 设置缩放比例
	 * 
	 * @param childrenScale
	 *            1.0表示原始大小
	 */
	private void setLayoutScale(float childrenScale) {
		mLastLayoutScale = sLayoutScale;
		sLayoutScale = childrenScale;

		// Trigger a full re-layout (never just call onLayout directly!)
		int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
		requestLayout();
		measure(widthSpec, heightSpec);
		layout(getLeft(), getTop(), getRight(), getBottom());
	}

	public static float getLayoutScale() {
		return sLayoutScale;
	}

	/**
	 * 获取相对于大屏幕的坐标
	 * 
	 * @param x
	 * @param y
	 * @param real
	 *            相对于大屏幕的真实值
	 */
	public static void virtualPointToReal(int x, int y, float[] real) {
		if (sLayoutScale < 1.0f) {
			float[] matchPoint = new float[] { x - sPageSpacingX, y - sPageSpacingY };
			real[0] = matchPoint[0] / sLayoutScale;
			real[1] = matchPoint[1] / sLayoutScale;
		} else {
			real[0] = x;
			real[1] = y;
		}
	}

	/**
	 * 获取相对于小屏幕的坐标
	 * 
	 * @param x
	 * @param y
	 * @param virtual
	 *            相对于小屏幕的虚拟值
	 */
	public static void realPointToVirtual(int x, int y, float[] virtual) {
		if (sLayoutScale < 1.0f) {
			float[] matchPoint = new float[] { x * sLayoutScale, y * sLayoutScale };
			virtual[0] = matchPoint[0] + sPageSpacingX;
			virtual[1] = matchPoint[1] + sPageSpacingY;
		} else {
			virtual[0] = x;
			virtual[1] = y;
		}

	}

	/**
	 * 自动播放特效
	 * 
	 * @param type
	 *            指定的特效类型（此处自提供播放，不存进数据库）
	 */
	public void effectorAutoShow(int type) {
		setEffector(type);
		mShowAutoEffect = true;
		mNextDestScreen = mCurrentScreen;
		if (mCurrentScreen == 0) {
			int dest = mCurrentScreen + 1;
			snapToScreen(dest, false, AUTO_EFFECT_TIME);
		} else {
			int dest = mCurrentScreen - 1;
			snapToScreen(dest, false, AUTO_EFFECT_TIME);
		}
	} // end effectorAutoShow

	public static int getPageSpacingX() {
		return sPageSpacingX;
	}

	public static int getPageSpacingY() {
		return sPageSpacingY;
	}

	/**
	 * 减速的三次曲线插值
	 * 
	 * @param begin
	 * @param end
	 * @param t
	 *            应该位于[0, 1]
	 * @return
	 */
	private float easeOut(float begin, float end, float t) {
		t = 1 - t;
		return begin + (end - begin) * (1 - t * t * t);
	}

	private void drawView(CellLayout cellLayout, Canvas canvas) {
		if (cellLayout != null) {
			cellLayout.buildChildrenDrawingCache();
			cellLayout.draw(canvas);
		} // end cellLayout != null
	} // end drawView

	private void initZoomTransition() {
		// 计算桌面的当前屏的预览在SenseWorkspace中的位置
		// final float left = mPageSpacingX;
		final float top = sPageSpacingY;

		// final int left2 = mScroller.getScroll();
		final int top2 = getScrollY();

		/*
		 * 以SenseWorkspace（不仅仅是屏幕显示部分）的左上角为参考坐标系原点， 区域1(left, top, width,
		 * height)是缩略图, 区域2(left2, top2, width2, heigth2)是屏幕
		 * 那么在动画过程中要把区域1逐渐放大为区域2，在此计算缩放中心和比例
		 */
		// mCx = MyAnimationUtils.solveScaleCenterX(left, getWidth() *
		// mFinalScaleFactor, left2, getWidth());
		mCx = getWidth() * sLayoutScale / 2 + sPageSpacingX + mCurrentScreen
				* (getWidth() * sLayoutScale + sPageSpacingX / 2);
		mCy = MyAnimationUtils
				.solveScaleCenterY(top, getHeight() * sLayoutScale, top2, getHeight());

		mStartTime = 0;
	}

	/**
	 * 绘制屏幕缩放动画
	 * 
	 * @param canvas
	 * @param srcScale
	 *            原缩放比例
	 * @param destScale
	 *            目标缩放比例
	 * @param srcAlpha
	 *            初始透明度
	 * @param destAlpha
	 *            结尾透明度
	 * @param currentTime
	 */
	private void drawZoom(final Canvas canvas, float srcScale, float destScale, float srcAlpha,
			float destAlpha, long currentTime) {
		float t = Math.min(1, currentTime / (float) ANIMDURATION);

		final float scale = easeOut(srcScale, destScale, t);
		final boolean drawAlpha = srcAlpha != destAlpha;
		final int alpha = drawAlpha ? (int) easeOut(srcAlpha, destAlpha, t) : 255;
		// final int bgAlpha = mBackgroudColor >>> 24;
		// final int color = alpha * bgAlpha >> 8 << 24;
		// canvas.drawColor(color);

		final int beg = mCurrentScreen - 1;
		final int end = mCurrentScreen + 1;
		final int count = getChildCount();
		final int saveCount = canvas.save();

		canvas.scale(scale, scale, mCx, mCy);
		for (int i = beg, tx = 0, ty = 0; i <= end; i++) {
			if (i > -1 && i < count) {
				View child = getChildAt(i);
				if (child != null && child instanceof CellLayout) {
					CellLayout layout = (CellLayout) child;
					final int x = layout.getLeft();
					final int y = layout.getTop();
					canvas.translate(x - tx, y - ty);
					if (drawAlpha) {
						layout.mBackgroudAlpha = alpha;
					}
					drawView(layout, canvas);
					tx = x;
					ty = y;
				}
			}
		}
		canvas.restoreToCount(saveCount);
		postInvalidate();

		if (t >= 1) {
			if (mDrawState == DRAW_STATE_SMALLER_TO_NORMAL_ENTERING) {
				final int screenCount = getChildCount();
				for (int i = 0; i < screenCount; i++) {
					final CellLayout cl = (CellLayout) getChildAt(i);
					cl.mBackgroudAlpha = 255;
					cl.setDrawBackground(false);
					cl.postInvalidate();
				}
				resetOrientation();
			}
			mDrawState = DRAW_STATE_ALL;
			mStartTime = 0;
		}
	}

	public void resetOrientation() {
		// 重置屏幕的横竖屏模式
		OrientationControl.setSmallModle(false);
		// 恢复进入添加状态前的屏幕状态
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				IDiyMsgIds.SCREEN_RESET_ORIENTATION, -1, null, null);
	}

	public void drawCellBackground(Canvas canvas, int screen) {
		if (mScaleState == STATE_NORMAL) {
			return;
		}
		CellLayout layout = getScreenView(screen);
		if (layout != null) {
			final int saveCount = canvas.save();
			canvas.scale(sLayoutScale, sLayoutScale);
			layout.drawBackground(canvas);
			canvas.restoreToCount(saveCount);
			layout.drawCenterCross(canvas);
		}
	}

	/**
	 * 往屏幕上添加组件的时候，如果是带“+”号的屏幕，要先转化为正常态的屏幕
	 * 
	 * @param cellLayout
	 */
	protected void blankCellToNormal(CellLayout cellLayout) {
		if (mScaleState == STATE_SMALL && cellLayout.getState() == CellLayout.STATE_BLANK_CONTENT) {
			cellLayout.blankToNormal();
			// 屏幕的个数限制为9
			if (getChildCount() < SenseWorkspace.MAX_CARD_NUMS) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_ADD_BLANK, -1, null, null);
			}
		}
	} // end blankCellToNormal

	protected void resizeWidget() {
		if (mClickView == null || mClickView instanceof CellLayout) {
			return;
		}
		final ItemInfo itemInfo = (ItemInfo) mClickView.getTag();
		// 如果是widget，单击就进入resize状态
		if (itemInfo != null && itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.QUICKACTION_EVENT,
					IQuickActionId.RESIZE, mClickView, null);
		}
	}

	/***
	 * 屏幕满时，桌面背景变红
	 */
	public void resetScreenBg(boolean isFull) {
		final CellLayout group = (CellLayout) getChildAt(mCurrentScreen);
		if (group != null) {
			group.changeBackground(isFull);
		}
	}

	/***
	 * 重设所有背景为默认颜色
	 */
	public void resetAllScreenBg() {
		final int screenCount = getChildCount();
		for (int i = 0; i < screenCount; i++) {
			final CellLayout cl = (CellLayout) getChildAt(i);
			cl.changeBackground(false);
		}
	}

	/**
	 * 根据screenScroll中的虚拟屏幕宽度，返回真实宽度（解决超过10屏之后的slide指示器的UI问题）
	 * 
	 * @param virtualW
	 * @return
	 */
	public static int getRealWidth(int virtualW) {
		if (sLayoutScale != 0 && sLayoutScale < 1.0f) {
			return (int) ((virtualW - sPageSpacingX / 2) / sLayoutScale);
		}
		return virtualW;
	}

	/**
	 * 根据screenScroll中的虚拟屏幕gao度，返回真实宽度（解决超过10屏之后的slide指示器的UI问题）
	 * 
	 * @param virtualW
	 * @return
	 */
	public static int getRealHeight(int virtualH) {
		if (sLayoutScale != 0 && sLayoutScale < 1.0f) {
			return (int) ((virtualH - sPageSpacingY / 2) / sLayoutScale);
		}
		return virtualH;
	}

	/**
	 * 设置指定的屏幕的网格状态
	 * 
	 * @param index
	 *            屏幕索引
	 * @param show
	 * @param drawState
	 */
	public void setCellLayoutGridState(int index, boolean show, int drawState) {
		CellLayout cellLayout = getScreenView(index);
		if (cellLayout != null && cellLayout.getState() == CellLayout.STATE_NORMAL_CONTENT) {
			final boolean tempShow = (index == mCurrentScreen && mScaleState == STATE_SMALL)
					? true
					: show;
			cellLayout.setDrawCross(tempShow);
			if (drawState != -1) {
				cellLayout.setmDrawStatus(drawState);
			}
		}
	}

	public void checkGridState(int newScreen, int oldScreen) {
		AbstractFrame topFrame = GoLauncher.getTopFrame();
		boolean isDrag = topFrame != null && topFrame.getId() == IDiyFrameIds.DRAG_FRAME
				&& ((DragFrame) topFrame).getCurHandler() == DragFrame.TYPE_SCREEN_HANDLER;
		if (isDrag || mScaleState == STATE_SMALL) {
			setCellLayoutGridState(newScreen, true, -1);
			setCellLayoutGridState(oldScreen, false, CellLayout.DRAW_STATUS_NORMAL);
		}
	}

	/**
	 * <br>功能简述:判断是否在dock区域内
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param dragView 拖动view
	 * @param point 拖动点
	 * @param workspaceHeight　屏幕高
	 * @param worspaceWidth 屏幕宽
	 * @return
	 */
	public static boolean isOverDock(View dragView, Point point, int workspaceHeight,
			int worspaceWidth) {
		if (sLayoutScale == 1.0f && ShortCutSettingInfo.sEnable
				&& (dragView instanceof BubbleTextView || dragView instanceof DockIconView)
				&& point != null) {
			final int centerX = point.x;
			final int centerY = point.y;
			final int bottom = DockUtil.getBgHeight();
			// 竖屏
			if (CellLayout.sPortrait) {
				return centerY >= workspaceHeight - bottom;
			} else {
				return centerX >= worspaceWidth - bottom;
			}
		}
		return false;
	}

	/***
	 * 判断是否在workspace区域内<br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param point
	 * @return
	 */
	private boolean isInsideWorkspace(Point point) {
		if (point != null) {
			int extraH = 0;
			int extraW = 0;
			if (sLayoutScale == 1.0f && ShortCutSettingInfo.sEnable) {
				if (CellLayout.sPortrait) {
					extraH = DockUtil.getBgHeight();
				} else {
					extraW = DockUtil.getBgHeight();
				}
			}
			final int centerX = point.x;
			final int centerY = point.y;
			boolean inside = (centerY >= sPageSpacingY && centerY <= sPageSpacingY + getHeight()
					* sLayoutScale - extraH)
					&& (centerX >= sPageSpacingX && centerX <= sPageSpacingX + getWidth()
							* sLayoutScale - extraW);
			return inside;
		}
		return false;
	}
	/***
	 * 寻找图标放下的位置
	 * 
	 * @param dragView
	 * @param point
	 * @param isAddState
	 *            针对添加页面的情况，只有在屏幕范围内才可放下
	 */
	protected void visualizeDropLocation(View dragView, Point point, boolean isAddState) {
		CellLayout cellLayout = getScreenView(mCurrentScreen);
		if (cellLayout != null && cellLayout.mState == CellLayout.STATE_NORMAL_CONTENT) {
			if (cellLayout != mDragTargetLayout) {
				setCurrentDropLayout(cellLayout);
			}
			if (!isInsideWorkspace(point)) {
				cellLayout.clearVisualizeDropLocation();
				cellLayout.revertTempState();
				return;
			}
			ViewGroup.LayoutParams lp = dragView.getLayoutParams();
			int cellHSpan = 1; // 从文件夹内拖出来的一定是1行1列
			int cellVSpan = 1;
			if (lp instanceof CellLayout.LayoutParams) {
				CellLayout.LayoutParams dragViewLp = (CellLayout.LayoutParams) lp;
				cellHSpan = dragViewLp.cellHSpan;
				cellVSpan = dragViewLp.cellVSpan;
			}

			int realX = point.x;
			int realY = point.y;
			if (mScaleState == STATE_SMALL) {
				float[] realXY = new float[2];
				// 先转换为真实值
				virtualPointToReal(realX, realY, realXY);
				realX = (int) realXY[0];
				realY = (int) realXY[1];
			}
			mDragViewVisualCenter[0] = realX;
			mDragViewVisualCenter[1] = realY;
			// 自动添加图标的代码？
			if (isAddState) {
				if (!isOverWorkspace(realX, realY)) {
					cellLayout.caculateCellXY(dragView, mTargetCell, realX, realY);
					return;
				}
			}
			cellLayout.findAllVacantCells(null, dragView);
			mTargetCell = cellLayout.findNearestArea(realX, realY, cellHSpan, cellVSpan,
					mTargetCell);
			setCurrentDropOverCell(mTargetCell[0], mTargetCell[1]);

			float targetCellDistance = cellLayout.getDistanceFromCell(realX, realY, mTargetCell);

			final View dragOverView = cellLayout.getChildViewByCell(mTargetCell);
			ItemInfo info = (ItemInfo) dragView.getTag();
			manageFolderFeedback(info, cellLayout, mTargetCell, targetCellDistance, dragOverView);

			int minSpanX = cellHSpan;
			int minSpanY = cellVSpan;
			boolean nearestDropOccupied = cellLayout.isNearestDropLocationOccupied(realX, realY,
					minSpanX, minSpanY, dragView, mTargetCell);
			if (!nearestDropOccupied) {
				cellLayout.caculateCellXY(dragView, mTargetCell, realX, realY);
			} else if ((mDragMode == DRAG_MODE_NONE || mDragMode == DRAG_MODE_REORDER)
					&& !mReorderAlarm.alarmPending()
					&& (mLastReorderX != mTargetCell[0] || mLastReorderY != mTargetCell[1])) {
				ReorderAlarmListener listener = new ReorderAlarmListener(mDragViewVisualCenter,
						minSpanX, minSpanY, minSpanX, minSpanY, dragView);
				mReorderAlarm.setOnAlarmListener(listener);
				mReorderAlarm.setAlarm(REORDER_TIMEOUT);
			}

			if (mDragMode == DRAG_MODE_CREATE_FOLDER || mDragMode == DRAG_MODE_ADD_TO_FOLDER
					|| !nearestDropOccupied) {
				cellLayout.revertTempState();
			}
			else {
				cellLayout.setOutlineVisible(true);
			}
		}
	}

	/***
	 * 检查是否在当前屏幕范围内
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isOverWorkspace(int x, int y) {
		CellLayout cellLayout = getScreenView(mCurrentScreen);
		if (x < 0 || x > cellLayout.getWidth() || y < 0
				|| y > (cellLayout.getHeight() - DockUtil.getBgHeight())) {
			return false;
		}
		return true;
	}

	/**
	 * dock, 指示器， 状态栏发生状态改变时（隐藏或显示）调用
	 * 
	 * @param object
	 * @param state
	 * @param isDrag
	 *            是否因拖动引起的
	 */
	protected void requestLayout(int object, int state) {
		// 是否扩充范围
		final boolean expand = state == 0 ? true : false;
		int topExtra = 0;
		int bottomExtra = 0;
		int rightExtra = 0;
		final boolean isPortrait = DrawUtils.sHeightPixels > DrawUtils.sWidthPixels;
		boolean indicatorInBottom = false;
		boolean showIndicator = true;
		ScreenLayout screenLayout = null;
		// 指示器是否在下面
		if (mListener instanceof ScreenFrame) {
			showIndicator = ((ScreenFrame) mListener).mShowIndicator;
			screenLayout = (ScreenLayout) ((ScreenFrame) mListener).getContentView();
			if (screenLayout != null) {
				indicatorInBottom = screenLayout.isIndicatorOnBottom();
			}
		}
		int indicatorHeight = getResources().getDimensionPixelSize(R.dimen.slider_indicator_height);

		switch (object) {
		// Dock
			case CHANGE_SOURCE_DOCK :
				final int dockHeight = DockUtil.getBgHeight();
				if (isPortrait) {
					bottomExtra = expand ? -dockHeight : 0;
				} else {
					rightExtra = expand ? -dockHeight : 0;
				}
				if (screenLayout != null) {
					screenLayout.setDockVisibleFlag(!expand);
				}
				break;

			// 指示器
			case CHANGE_SOURCE_INDICATOR :
				if (!ShortCutSettingInfo.sEnable) {
					if (isPortrait) {
						bottomExtra -= DockUtil.getBgHeight();
					} else {
						rightExtra -= DockUtil.getBgHeight();
					}
				}
				break;

			// 状态栏
			case CHANGE_SOURCE_STATUSBAR :
				if (mDragging) {
					// 状态栏的处理比较特殊
					final int statusBarHeight = StatusBarHandler.getStatusbarHeight();
					topExtra = expand ? 0 : statusBarHeight;
				}
				// dock是否可见
				if (!ShortCutSettingInfo.sEnable) {
					if (isPortrait) {
						bottomExtra -= DockUtil.getBgHeight();
					} else {
						rightExtra -= DockUtil.getBgHeight();
					}
				}
				break;

			default :
				break;
		}
		if (indicatorInBottom) {
			topExtra -= indicatorHeight;
			int h = showIndicator ? indicatorHeight / 2 : 0;
			bottomExtra += h;
		} else {
			int h = showIndicator ? 0 : indicatorHeight;
			topExtra -= h;
		}

		startToLayout(topExtra, bottomExtra, rightExtra);
	} // end requestLayout

	private void startToLayout(int topPadding, int bottomPadding, int rightPadding) {
		// 先变当前屏
		CellLayout layout = getCurrentScreenView();
		if (layout != null) {
			CellLayout.setTopExtra(topPadding);
			CellLayout.setBottomExtra(bottomPadding);
			CellLayout.setRightExtra(rightPadding);
			// layout.requestLayout();
		}
		final int count = getChildCount();
		// 再变其它屏
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child != null) {
				child.requestLayout();
			}
		} // end for
	}

	/*-------------------------- 以下代码是拖拽的新逻辑  by Yugi 2012-8-11------------------------------*/

	private static final int FOLDER_CREATION_TIMEOUT = 0; // 创建新文件夹的等待时间为0
	private static final int REORDER_TIMEOUT = 250; // 进行图标抖动的等待时间
	private final Alarm mFolderCreationAlarm = new Alarm();
	private final Alarm mReorderAlarm = new Alarm();
	private FolderRingAnimator mDragFolderRingAnimator = null;
	private FolderIcon mDragOverFolderIcon = null;
	private boolean mCreateUserFolderOnDrop = false;
	private float mMaxDistanceForFolderCreation;
	private float[] mDragViewVisualCenter = new float[2];
	private CellLayout mDragTargetLayout = null;
	private int mLastReorderX = -1;
	private int mLastReorderY = -1;

	void setCurrentDropLayout(CellLayout layout) {
		if (mDragTargetLayout != null) {
			mDragTargetLayout.revertTempState();
			// mDragTargetLayout.onDragExit();
		}
		mDragTargetLayout = layout;
		if (mDragTargetLayout != null) {
			// mDragTargetLayout.onDragEnter();
		}
		cleanupReorder(true);
		cleanupFolderCreation();
		setCurrentDropOverCell(-1, -1);
	}

	private void setCurrentDropOverCell(int x, int y) {
		if (x != mDragOverX || y != mDragOverY) {
			mDragOverX = x;
			mDragOverY = y;
			setDragMode(DRAG_MODE_NONE);
		}
	}

	private void manageFolderFeedback(ItemInfo info, CellLayout targetLayout, int[] targetCell,
			float distance, View dragOverView) {
		boolean userFolderPending = willCreateUserFolder(info, targetLayout, targetCell, distance,
				false);

		if (mDragMode == DRAG_MODE_NONE && userFolderPending
				&& !mFolderCreationAlarm.alarmPending()) {
			mFolderCreationAlarm.setOnAlarmListener(new FolderCreationAlarmListener(targetLayout,
					targetCell[0], targetCell[1]));
			mFolderCreationAlarm.setAlarm(FOLDER_CREATION_TIMEOUT);
			return;
		}

		boolean willAddToFolder = willAddToExistingUserFolder(info, targetLayout, targetCell,
				distance);

		if (willAddToFolder && mDragMode == DRAG_MODE_NONE) {
			mDragOverFolderIcon = (FolderIcon) dragOverView;
			mDragOverFolderIcon.onDragEnter(info);
			if (targetLayout != null) {
//				targetLayout.clearVisualizeDropLocation();
				targetLayout.setOutlineVisible(false);
			}
			setDragMode(DRAG_MODE_ADD_TO_FOLDER);
			return;
		}

		if (mDragMode == DRAG_MODE_ADD_TO_FOLDER && !willAddToFolder) {
			setDragMode(DRAG_MODE_NONE);
		}
		if (mDragMode == DRAG_MODE_CREATE_FOLDER && !userFolderPending) {
			setDragMode(DRAG_MODE_NONE);
		}

		return;
	}

	// 是否创建新的文件夹
	boolean willCreateUserFolder(ItemInfo info, CellLayout target, int[] targetCell,
			float distance, boolean considerTimeout) {
		if (distance > mMaxDistanceForFolderCreation) {
			return false;
		}
		View dropOverView = target.getChildViewByCell(targetCell);

		if (dropOverView != null) {
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
			if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY)) {
				return false;
			}
		}

		boolean hasntMoved = false;
		if (mDragInfo != null) {
			hasntMoved = dropOverView == mDragInfo.cell;
		}

		if (dropOverView == null || hasntMoved || (considerTimeout && !mCreateUserFolderOnDrop)) {
			return false;
		}

		boolean aboveShortcut = dropOverView.getTag() instanceof ShortCutInfo;
		boolean aboveUserFolder = dropOverView.getTag() instanceof UserFolderInfo;
		boolean willBecomeShortcut = info.mItemType == IItemType.ITEM_TYPE_SHORTCUT
				|| info.mItemType == IItemType.ITEM_TYPE_APPLICATION;
		// 符合两个icon合成一新文件夹
		if (aboveShortcut) {
			return aboveShortcut && willBecomeShortcut;
		}
		// 符合合并两个文件夹
		else if (aboveUserFolder) {
			return info.mItemType == IItemType.ITEM_TYPE_USER_FOLDER;
		}
		return false;
	}

	// 是否放进已存在的文件夹
	boolean willAddToExistingUserFolder(ItemInfo dragInfo, CellLayout target, int[] targetCell,
			float distance) {
		if (distance > mMaxDistanceForFolderCreation) {
			return false;
		}
		View dropOverView = target.getChildViewByCell(targetCell);

		if (dropOverView != null) {
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
			if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY)) {
				return false;
			}
			boolean addToFolder = true;
			if (mDragInfo != null) {
				addToFolder = dropOverView != mDragInfo.cell;
			}
			if (dragInfo != null) {
				addToFolder = !(dragInfo instanceof UserFolderInfo);
			}
			if (addToFolder && dropOverView instanceof FolderIcon) {
				// FolderIcon folderIcon = (FolderIcon) dropOverView;
				// 文件夹没有个数限制
				return true;
				// if (fi.acceptDrop(dragInfo)) {
				// return true;
				// }
			}
		}
		return false;
	}

	void setDragMode(int dragMode) {
		if (dragMode != mDragMode) {
			if (dragMode == DRAG_MODE_NONE) {
				cleanupAddToFolder();
				cleanupReorder(false);
				cleanupFolderCreation();
			} else if (dragMode == DRAG_MODE_ADD_TO_FOLDER) {
				cleanupReorder(true);
				cleanupFolderCreation();
			} else if (dragMode == DRAG_MODE_CREATE_FOLDER) {
				cleanupAddToFolder();
				cleanupReorder(true);
			} else if (dragMode == DRAG_MODE_REORDER) {
				cleanupAddToFolder();
				cleanupFolderCreation();
			}
			mDragMode = dragMode;
		}
	}

	private void cleanupAddToFolder() {
		if (mDragOverFolderIcon != null) {
			mDragOverFolderIcon.onDragExit();
			mDragOverFolderIcon = null;
		}
	}

	private void cleanupFolderCreation() {
		if (mDragFolderRingAnimator != null) {
			mDragFolderRingAnimator.animateToNaturalState();
		}
		mFolderCreationAlarm.cancelAlarm();
	}

	private void cleanupReorder(boolean cancelAlarm) {
		// Any pending reorders are canceled
		if (cancelAlarm) {
			mReorderAlarm.cancelAlarm();
		}
		mLastReorderX = -1;
		mLastReorderY = -1;
	}

	/**
	 *  
	 * 类描述:内部类，“创建新文件夹”的监听器
	 * 功能详细描述:
	 * 
	 * @date  [2012-9-7]
	 */
	class FolderCreationAlarmListener implements OnAlarmListener {
		CellLayout mLayout;
		int mCellX;
		int mCellY;

		public FolderCreationAlarmListener(CellLayout layout, int cellX, int cellY) {
			this.mLayout = layout;
			this.mCellX = cellX;
			this.mCellY = cellY;
		}

		@Override
		public void onAlarm(Alarm alarm) {
			if (mDragFolderRingAnimator == null) {
				mDragFolderRingAnimator = new FolderRingAnimator(GOLauncherApp.getContext(), null);
			}
			mDragFolderRingAnimator.setFolderBg(GOLauncherApp.getContext());
			mDragFolderRingAnimator.setCell(mCellX, mCellY);
			mDragFolderRingAnimator.setCellLayout(mLayout);
			mDragFolderRingAnimator.animateToAcceptState();
			mLayout.showFolderAccept(mDragFolderRingAnimator);
//			mLayout.clearVisualizeDropLocation();
			mLayout.setOutlineVisible(false);
			setDragMode(DRAG_MODE_CREATE_FOLDER);
		}
	}

	/**
	 *  
	 * 类描述:内部类，图标/widget抖动的监听器
	 * 功能详细描述:
	 * 
	 * @date  [2012-9-7]
	 */
	class ReorderAlarmListener implements OnAlarmListener {
		float[] mDragViewCenter;
		int minSpanX, minSpanY, mSpanX, mSpanY;
		View mChild;

		public ReorderAlarmListener(float[] dragViewCenter, int minSpanX, int minSpanY, int spanX,
				int spanY, View child) {
			this.mDragViewCenter = dragViewCenter;
			this.minSpanX = minSpanX;
			this.minSpanY = minSpanY;
			this.mSpanX = spanX;
			this.mSpanY = spanY;
			this.mChild = child;
		}

		@Override
		public void onAlarm(Alarm alarm) {
			CellLayout targetLayout = mDragTargetLayout;
			if (targetLayout == null) {
				targetLayout = getCurrentScreenView();
				return;
			}
			mTargetCell = targetLayout.findNearestArea((int) mDragViewVisualCenter[0],
					(int) mDragViewVisualCenter[1], mSpanX, mSpanY, mTargetCell);
			mLastReorderX = mTargetCell[0];
			mLastReorderY = mTargetCell[1];

			int[] resultSpan = new int[2];
			mTargetCell = targetLayout.createArea((int) mDragViewVisualCenter[0],
					(int) mDragViewVisualCenter[1], minSpanX, minSpanY, mSpanX, mSpanY, mChild,
					mTargetCell, resultSpan, CellLayout.MODE_DRAG_OVER);

			if (mTargetCell[0] < 0 || mTargetCell[1] < 0) {
				targetLayout.revertTempState();
			} else {
				setDragMode(DRAG_MODE_REORDER);
			}
			targetLayout.caculateCellXY(mChild, mTargetCell, (int) mDragViewVisualCenter[0],
					(int) mDragViewVisualCenter[1]);
		}
	}

	protected int getDragMode() {
		return mDragMode;
	}
	
	protected void setWallpaperDrawer(IWallpaperDrawer drawer) {
		if (mScroller != null) {
			mScroller.setWallpaperDrawer(drawer);
		}
	}	

	public static final String MULTIPLE_WALLPAPER_CURRENT_SCREEN_NUMBER = "currentScreenNumber";
	public static final String MULTIPLE_WALLPAPER_CURRENT_SCREEN_REAL_NUMBER = "currentScreenRealNumber";
	public static final String MULTIPLE_WALLPAPER_CURRENT_SCREEN_INDEX = "currentScreenIndex";

	/**
	 * 发送广播给多屏多壁纸应用，通知当前屏幕数与当前屏幕下标
	 * 
	 * @param isAddBlankCellLayout
	 * @param isScreenNumberChanged
	 */
	public void sendBroadcastToMultipleWallpaper(boolean isAddBlankCellLayout,
			boolean isScreenNumberChanged) {

		// 多屏多壁纸应用都没安装，不用发广播了
		// if (!AppUtils.isAppExist(getContext(),
		// LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
		// return;
		// }

		int currentScreenNumber = getChildCount();
		int currentScreenRealNumber = currentScreenNumber;
		int currentScreenIndex = getCurrentScreen();

		if (isAddBlankCellLayout) {
			// 这里是长按进入编辑页面，屏幕多一屏，是添加屏幕，不算真正的屏幕，需要减去1
			currentScreenRealNumber = currentScreenNumber - 1;
		}

		Intent intent = new Intent();
		if (Machine.IS_HONEYCOMB_MR1) {
			// 3.1之后，系统的package manager增加了对处于“stopped state”应用的管理
			intent.setFlags(FLAG_INCLUDE_STOPPED_PACKAGES);
		}

		// 壁纸设置模式
		boolean isScrollMode = GOLauncherApp.getSettingControler()
				.getScreenSettingInfo().mWallpaperScroll;

		if (!isScrollMode) {
			// 如果壁纸设置是竖屏模式而不是默认模式，即屏幕不可滚动
			
			if (isScreenNumberChanged) {

				// 如果屏幕数目发生变化，则需要发送屏幕数目
				intent.setAction(ICustomAction.ACTION_CURRENT_WALLPAPER_NUMBER);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_REAL_NUMBER,
						currentScreenRealNumber);
			}

		} else {
			if (isScreenNumberChanged) {

				// 如果屏幕数目发生变化，则需要发送屏幕数目和当前屏幕下标
				intent.setAction(ICustomAction.ACTION_CURRENT_WALLPAPER_NUMBER_AND_CURRENT_SCREEN_INDEX);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_NUMBER,
						currentScreenNumber);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_REAL_NUMBER,
						currentScreenRealNumber);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_INDEX,
						currentScreenIndex);
			} else {

				// 如果屏幕数目没发生变化，则只需要发送当前屏幕下标
				intent.setAction(ICustomAction.ACTION_CURRENT_SCREEN_INDEX);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_INDEX,
						currentScreenIndex);
			}
		}

		getContext().sendBroadcast(intent);
	}

	@Override
	public boolean onSwipe(MutilPointInfo p, float dx, float dy, int direction) {
		if (mMultiTouchOccured) {
			return true;
		}
		PreferencesManager sp = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);


		if (p.getPointCount() == 2 && direction == OnMultiTouchListener.DIRECTION_DOWN) {
			// 双指下滑打开全屏插件
			String defaultTouch = TouchHelperChooser.getDefaultTouchhelperPkg(getContext());
			if (defaultTouch != null) {
				try {
					PackageManager packageManager = GoLauncher.getContext().getPackageManager();
					Intent intent = packageManager.getLaunchIntentForPackage(defaultTouch);
					AppUtils.safeStartActivity(getContext(), intent);
				} catch (Exception e) {
				}
			} else {
				List<AppInfo> list = TouchHelperChooser.getAllTouchhelper(getContext());
				if (list == null || list.isEmpty()) {
					// 用户是否设置了不再显示全屏插件下载提示
					boolean neverShow = sp.getBoolean(IPreferencesIds.NEVER_SHOW_TOUCHHELPER_RECOMMAND, false);
					if (!neverShow) {
						// 没有安装全屏插件打开下载介绍页面
						Intent intent = new Intent(GoLauncher.getContext(),
								TouchHelperDownloadGuideActivity.class);
						AppUtils.safeStartActivity(getContext(), intent);
					}
				} else if (list.size() == 1) {
					// 只装一个全屏插件,立即启动
					AppInfo appInfo = list.get(0);
					Intent intent = new Intent();
					intent.setClassName(appInfo.pkName, appInfo.actName);
					AppUtils.safeStartActivity(getContext(), intent);
				} else {
					TouchHelperChooser chooser = new TouchHelperChooser(getContext());
					chooser.showDialog();
				}
			}
			mMultiTouchOccured = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean onScale(com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture.MutilPointInfo p,
			float scale, float angle) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture.MutilPointInfo p) {
		// TODO Auto-generated method stub
		return false;
	}
	
//	protected void startAlarm() {
//       mAlarmHandler.startAlarm();
//	}
//	
//	protected void cancleAlarm() {
//		mAlarmHandler.cancleAlarm();
//	}
//	
//	protected void destroyAlarm() {
//		mAlarmHandler.cancleAlarm();
//		mAlarmHandler.cleanUp();
//		mAlarmHandler = null;
//	}
//	
//	protected void addState(int state) {
//		mAlarmHandler.addState(state);
//	}
//	
//	protected void setSwitcher(int state) {
//		mAlarmHandler.setSwitcher(state);
//	}
}