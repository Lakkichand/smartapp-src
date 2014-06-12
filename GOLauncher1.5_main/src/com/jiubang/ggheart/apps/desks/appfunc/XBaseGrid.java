package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.Utilities;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.FadePainter;
import com.go.util.graphics.effector.gridscreen.GridScreenContainer;
import com.go.util.graphics.effector.gridscreen.GridScreenEffector;
import com.go.util.graphics.effector.subscreen.SubScreenContainer;
import com.go.util.graphics.effector.united.CoupleScreenEffector;
import com.go.util.graphics.effector.verticallist.VerticalListContainer;
import com.go.util.graphics.effector.verticallist.WaterFallEffector;
import com.go.util.log.LogUnit;
import com.go.util.scroller.CycloidScroller;
import com.go.util.scroller.FastVelocityTracker;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.go.util.scroller.Scroller;
import com.go.util.scroller.ScrollerEffector;
import com.go.util.scroller.ScrollerListener;
import com.go.util.window.OrientationControl;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.mars.AbstractAnimateListener;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.component.AllAppTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolder;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolderQuickAddBar;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolderQuickAddIcon.SpecialFolderItem;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncHomeComponent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.AppMoveToDesk;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.component.BaseAppIcon;
import com.jiubang.ggheart.apps.appfunc.component.MIndicator;
import com.jiubang.ggheart.apps.appfunc.component.ProManageEditDock;
import com.jiubang.ggheart.apps.appfunc.component.ProManageIcon;
import com.jiubang.ggheart.apps.appfunc.component.ProManageTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.RecentAppsIcon;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.timer.ITask;
import com.jiubang.ggheart.apps.appfunc.timer.Scheduler;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.AnimationInfo;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.BatchAnimationObserver;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppIconForMsg;
import com.jiubang.ggheart.apps.desks.appfunc.model.DataSetObserver;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.FolderIconForMsg;
import com.jiubang.ggheart.apps.desks.appfunc.model.GBaseAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.appfunc.model.MsgEntity;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IndicatorListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideForGlFrame;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.StaticTutorial;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 功能表九宫格排版基类
 * @author yangguanxiang
 *
 */
public class XBaseGrid extends XPanel
		implements
			OnGestureListener,
			IMsgHandler,
			ScreenScrollerListener,
			GridScreenContainer,
			ScrollerListener,
			IndicatorListner,
			VerticalListContainer,
			SubScreenContainer,
			BatchAnimationObserver,
			OrientationInvoker {
	/**
	 * XBaseGrid用于显示拖动图标到屏幕边缘显示的绿色边缘区域大小，以Hight Density为标准
	 */
	public static int sPaddingSizeId = R.dimen.appfunc_content_padding_size;
	/**
	 * 竖向拖拽跨屏移动步幅
	 */
	public int mMoveVelocity = 0;
	/**
	 * 不滚动距离
	 */
	private float mNotScrollSize = 0.0f;
	/**
	 * 竖向划屏时，最大的划屏速度
	 */
	public static final int MAX_MOVE_VELOCITY = 75;
	/**
	 * 横滑时，最少的X轴差距
	 */
	public static final int MIN_FLING_X = 64;
	/**
	 * 感应区是滚动条区域倍数
	 */
	public static final int SCROLL_SIZE_MULTIPLE = 3;
	/**
	 * 最大可拉伸的长度
	 */
	public static final int MAX_PULL_LENGTH = 280;
	/**
	 * 最小可滑动的长度
	 */
	public static final int MIN_FLING_LENGTH = 240;

	/**
	 * 图标反弹回去的步数
	 */
	public static final byte PULL_STEP = 20;

	/**
	 * 图标反弹回去的步数
	 */
	public static final byte MAX_PULL_STEP = 32;

	/**
	 * 图标滚动时的步数
	 */
	public static final byte SCROLL_STEP = 14;

	/**
	 * 横滑的步数
	 */
	public static final byte SCROLL_STEP_H = 12;
	/**
	 * 水平
	 */
	public static final byte HORIZONTAL = 2;
	/**
	 * 竖直
	 */
	public static final byte VERTICAL = 1;
	/**
	 * 合并程序图标到文件夹
	 */
	public static final int MEREG_ITEM_TO_FOLDER = 1;
	/**
	 * 列数
	 */
	protected volatile int mColunmNums = 4;
	/**
	 * 最新列数值，待重布局使用
	 */
	protected int mNewColunmNums = 0;
	/**
	 * 行数
	 */
	protected volatile int mRowNums = 4;
	/**
	 * 最新行数值，待重布局使用
	 */
	protected int mNewRowNums = 0;
	/**
	 * 最新左边距，待重布局使用
	 */
	protected int mNewPaddingLeft = 0;
	/**
	 * 最新右边距，待重布局使用
	 */
	protected int mNewPaddingRight = 0;
	/**
	 * 最新上边距，待重布局使用
	 */
	protected int mNewPaddingTop = 0;
	/**
	 * 最新下边距，待重布局使用
	 */
	protected int mNewPaddingBottom = 0;

	protected Activity mActivity = null;

	private GestureDetector mDetector = null;
	/**
	 * 单元格高度
	 */
	protected volatile int mCellHeight = 0;
	/**
	 * 单元格宽度
	 */
	protected volatile int mCellWidth = 0;
	/**
	 * 单屏显示单元格数量
	 */
	protected volatile int mSingleScreenCells = 0;
	/**
	 * 功能表显示区域真实的宽度
	 */
	protected volatile int mRealWidth = 0;
	/**
	 * 功能表显示区域真实的高度
	 */
	protected volatile int mRealHeight = 0;
	/**
	 * 接收Down事件的单元格的位置下标
	 */
	private int mTouchDownPostion = -1;
	/**
	 * 接收Up事件的单元格的位置下标
	 */
	private int mTouchUpPostion = -1;
	/**
	 * 第一个可见的组件位置
	 */
	protected volatile int mFVisibleIndex = -1;
	protected int mFVisibleIndexBak = -1; // 切换布局的时候备份第一个可见组件的索引，以回到原来位置
	/**
	 * 最后一个可见的组件位置
	 */
	protected volatile int mLVisibleIndex = -1;
	/**
	 * 获得焦点的位置，默认值为 -1 表示在顶部的上方，如果等于 -2 表示在底部的下方
	 */
	public int mFocusedPosition = -1;
	/**
	 * Adapter.getCount()返回的数量
	 */
	protected int mAdapterCount = 0;
	/**
	 * 竖屏行数
	 */
	protected int mRows = 0;
	/**
	 * 横屏屏数
	 */
	protected int mScreen = 0;
	/**
	 * 方向(默认竖屏)
	 */
	protected byte mOrientation = VERTICAL;
	/**
	 * 方向(默认竖屏)最新方向数值，待重布局使用
	 */
	protected byte mNewOrientation = VERTICAL;

	private DisplayMetrics mMetrics = null;
	/**
	 * 密度
	 */
	private float mViewConScaleFling = 0.0f;
	/**
	 * 数据适配源
	 */
	protected GBaseAdapter mAdapter = null;
	/**
	 * 是否支持滑动
	 */
	private boolean mIsSupportScroll = false;
	/**
	 * 是否竖屏的滚动,(如果不是，则表示是横屏的滚动)
	 */
	protected volatile boolean mIsVScroll = false;
	/**
	 * 指示器
	 */
	protected volatile MIndicator mIndicator = null;

	/**
	 * X轴方向偏移量
	 */
	protected volatile int mOffsetX = 0;
	/**
	 * Y轴方向偏移量
	 */
	protected volatile int mOffsetY = 0;
	/**
	 * 列表实际高度 （其实是竖向滑动时，使最后一行图标居底对齐的偏移量，负数）
	 */
	protected volatile int mTotalHeight = 0;
	protected volatile int mRealTotalHeight = 0;
	/**
	 * 是否停止响应所有事件
	 */
	protected boolean mIsStopResponse = false;
	/**
	 * 图标正在移动数
	 */
	protected volatile int mIconMoveing = 0;
	/**
	 * 是否正在滚动
	 */
	protected volatile boolean mIsScrolling = false;
	/**
	 * 最后接触点的X轴，用于计算滑动时，每次移动距离
	 */
	private int mLastTouchX = 0;
	/**
	 * 最后接触点的Y轴，用于计算滑动时，每次移动距离
	 */
	private int mLastTouchY = 0;
	// /**
	// * 滑动动作
	// */
	// private volatile XMotion mOffsetMotion;
	/**
	 * 是否有动作在运行
	 */
	private volatile boolean mIsMotionRunning = false;
	/**
	 * 是否拥有滚动条
	 */
	protected boolean mIsHasScroller = false;
	/**
	 * 滚动条大小
	 */
	protected int mIndicatorSize = 0;
	/**
	 * 竖滑下，当前显示行
	 */
	protected volatile int mCurRow = 0;
	/**
	 * 横滑下，当前显示屏
	 */
	protected volatile int mCurScreen = 0;
	// //////////////////////////////以下是功能性组件的属性//////////////////////////////////////////
	/**
	 * 是否支持拖拽
	 */
	private boolean mIsDragable = false;
	/**
	 * 是否正处于可拖拽状态
	 */
	private volatile boolean mInDragStatus = false;
	/**
	 * 拖拽的组件
	 */
	private volatile XComponent mDragComponent = null;
	/**
	 * 拖拽的组件原始位置下标
	 */
	private volatile int mDragComponentIndex = -1;
	/**
	 * 拖拽的组件目标位置下标
	 */
	private volatile int mDragComponentTargetIndex = -1;
	/**
	 * 拖拽的组件原始位置下标的X坐标
	 */
	private int mDragComponentX = 0;
	/**
	 * 拖拽的组件原始位置下标的X坐标
	 */
	private int mDragComponentY = 0;
	/**
	 * 是否启动文件夹
	 */
	private boolean mIsFolderEnable = false;
	/**
	 * 创建文件夹的位置下标
	 */
	private int mFolderIndex = 0;
	/**
	 * 创建文件夹的位置的Icon
	 */
	public ApplicationIcon mFolderIndexIcon = null;
	/**
	 * 被拖动组件DB的Icon
	 */
	public ApplicationIcon mDragComponentTargetIcon = null;
	/**
	 * 显示文件夹图标的Icon
	 */
	private ApplicationIcon mFolderImageIcon = null;
	// ///////////////////////////////////川//////////////////
	private float mViewConScaleFlingMax = 0.0f;

	// ///////////////////////暂时额外//////////////////////////
	/**
	 * 暂时属性，是否离开
	 */
	public static boolean sIsExit = false;
	/**
	 * 是否第一次执行布局操作，主要用于横屏翻页模式,第一次的定义就是从任何其他界面进入功能表界面就认为是第一次
	 */
//	private static boolean sFirstLayout = false;
	/**
	 * 定时器
	 */
	private Scheduler mScheduler = null;
	private Toast mToast = null;
	/**
	 * 是否准备拖拽Icon
	 */
	private volatile boolean mIsReadyDragIcon = false;
	/**
	 * 比对是拖拽，其他操作的容错区
	 */
	private volatile int mDragBeginCheckSize = 0;
	/**
	 * Down事件的X坐标
	 */
	private int mDownX = 0;
	/**
	 * Down事件的Y坐标
	 */
	private int mDownY = 0;

	private volatile boolean mIsMoveStop = false; // 拖动图标到屏幕边缘触发滚屏的标志
	private volatile int mXMoveStop = 0; // 图标拖动停留的位置X坐标
	private volatile int mYMoveStop = 0; // 图标拖动停留的位置Y坐标
	private MoveScreenTask mMoveScreenTask = new MoveScreenTask();
	private MoveIconTask mMoveIconTask = new MoveIconTask();

	/**
	 * 是否选中一个图标
	 */
	protected boolean mIsSelectedCell = false;

	/**
	 * 图标是否正在进入图标
	 */
	private boolean mIsEnteringFolder = false;

	/**
	 * 是否被长按
	 */
	protected boolean mLongPressed = false;

	/**
	 * 在选中图标下，累计滑动距离(达到一定值，才启动滑动)
	 */
	private int mScrollEnabledInSelectdCell = 0;
	/**
	 * 是否在x轴方向滑动，主要用于竖屏滚动模式搜索事件处理
	 */
	protected boolean mScrollOnX = false;
	/**
	 * 在x轴方向滑动的距离，主要用于竖屏滚动模式搜索事件处理
	 */
	private int mScrollDisOnX = 0;

	protected ScreenScroller mScreenScroller = null; // 横向滚动的滚动器
	// private GridScreenEffector mGridScreenEffector = null; // 横向滚动的特效
	protected CoupleScreenEffector mCoupleScreenEffector = null;
	private int mScrollDuration = 350;
	private int mMaxOverShootPercent = 0; // 5;
	private boolean mAutoTweakAntialiasing = true;
	protected boolean mInvalidated = false; // 是否重绘

	protected Scroller mScroller = null; // 垂直滚动的滚动器
	protected ScrollerEffector mVerticalEffector = null; // 垂直滚动的特效
	public final static int NO_VERTICAL_EFFECTOR = 0; // 没有特效
	public final static int WATERFALL_VERTICAL_EFFECTOR = 1; // 3D瀑布特效
	protected int mVerticalEffectorType = 0;

	private Rect mFadeRect = new Rect(); // 边缘淡化区域
	private boolean mDrawFadeRect = true; // 是否在边缘绘制淡出背景
	private boolean mDrawHighlightRect = true; // 是否在边缘绘制高亮颜色

	private boolean mDispatchTouchToIndicator = false; // 是否把Touch事件传递给indicator
	private Rect mIndicatorRect = new Rect(); // 指示器矩形区域位置信息

	protected int mUpperHeight = 0; // 上部分的高度，在垂直3D效果时需要
	protected int mUpperBottom = 0; // 上部分的下边界，在垂直3D效果时需要
	protected int mLowerHeight = 0; // 下部分的高度，在垂直3D效果时需要
	protected int mClipTop = 0; // 实际绘制内容的上边界，在垂直3D效果时需要
	protected int mClipBottom = 0; // 实际绘制内容的下边界，在垂直3D效果时需要

	/**
	 * 是否横向循环
	 */
	private boolean mCycleMode = false;
	/**
	 * 是否竖向循环
	 */
	protected boolean mVerticalCycleMode = false;
	/**
	 * 横竖屏
	 */
	protected boolean mIsVertical = false;
	/**
	 * 是否第一次进入功能表
	 */
//	private boolean mIsFirstIn = true;

	/**
	 * Grid的ID，常量定义在AppFuncConstants.ALLAPPS_GRID
	 */
	private int mGridId = -1;
	/**
	 * 竖屏滚动模式下没有搜索结果时的搜索界面高度相对于功能表行高的高度差
	 */
	protected int mSearchRowHeightOffset = 0;
	/**
	 * 是否打开文件夹编辑页面，文件夹为空拖入一个图标时需要打开文件夹编辑界面
	 */
	private boolean mIsOpenEditFolder = false;

	private boolean mShowAddButton = false;

	private boolean mShowFolderTutorial = false;
	private boolean mShowDragTutorial = false;
	/**
	 * 用于竖屏模式标志是否显示垂直滚动条
	 */
	private boolean mShowIndicator = true;
	/**
	 * 加号按钮是否可见
	 */
	private boolean mAddButtonHided = false;

	/**
	 * 用于判断是否定位文件夹里的应用
	 */
	private boolean mfolderintent;

//	private int mTempCurrentScreen = 0;
	/**
	 * 用于判断是否调整到查看程序信息界面，为true是表示跳转到了，返回的时候要保持在编辑状态(正在运行)
	 */
	private boolean mIsGotoInfo = false;
	/**
	 * 合并图标到文件夹的文件夹信息
	 */
	private FunFolderItemInfo mFolderInfo;
	/**
	 * 是否需要打开文件夹
	 */
	private Boolean mNeedOpenFolder = false;

	private boolean mLockScreen = false;

	protected String mIndicatorPos = ScreenIndicator.INDICRATOR_ON_TOP;
	protected boolean mShowTwoLines;
	protected FunAppSetting mFunAppSetting;
	protected int mLastVisiableRow;

	private static final int PAGING_TOUCH_SLOP = 16;
	private int mSwipVelocity; // 上下滑屏识别为手势的速度阈值
	private int mSwipTouchSlop; // 上下划动识别为手势的距离阈值
	private float mInterceptTouchDownY;
	private float mInterceptTouchMoveY;
	private FastVelocityTracker mVelocityTracker;
	private int mTouchSlop; // 区分滑动和点击的阈值
	private float mInterceptTouchVY;
	private boolean mInterceptTouchMoved;
	private float mInterceptTouchDownX;
	private float mInterceptTouchMoveX;
	private static final int TOUCH_STATE_NORMAL = 0;
	private static final int TOUCH_STATE_GLIDE_UP = 1;
	private static final int TOUCH_STATE_GLIDE_DOWN = 2;
	private int mTouchState = TOUCH_STATE_NORMAL;
	protected AppFuncUtils mUtils;
	protected byte[] mLock = new byte[0];
	private boolean mIsInFolderQuickAddBar;
	private boolean mIsInMoveToDesk;

	public XBaseGrid(Activity activity, int tickCount, int x, int y) {
		this(activity, tickCount, x, y, 0, 0);
	}

	/**
	 * 构造函数
	 * 
	 * @param context
	 * @param tickCount
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public XBaseGrid(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
		this.mActivity = activity;
		mDetector = new GestureDetector(this);
		mMetrics = activity.getResources().getDisplayMetrics();
		mViewConScaleFling = mMetrics.density;
		mViewConScaleFlingMax = mViewConScaleFling * mViewConScaleFling;
		mViewConScaleFling = mViewConScaleFlingMax * mViewConScaleFlingMax;
		mViewConScaleFlingMax = mMetrics.density;
		mMoveVelocity = (int) (6 * mMetrics.density);
		mNotScrollSize = 4 * mMetrics.density;
		mIndicatorSize = (int) (AppFuncConstants.SCROLL_SIZE * mMetrics.density);
		mScheduler = Scheduler.getInstance();
		mDragBeginCheckSize = (int) (5 * mMetrics.density);
		mIsMoveStop = false;
		mIsEnteringFolder = false;
		mScreenScroller = new ScreenScroller(mActivity, this);
		// mGridScreenEffector = new GridScreenEffector(mScreenScroller);
		mCoupleScreenEffector = new CoupleScreenEffector(mScreenScroller,
				CoupleScreenEffector.PLACE_MENU, CoupleScreenEffector.GRIDSCREEN_EFFECTOR_TYPE);
		mScreenScroller.setBackgroundAlwaysDrawn(true);
		mScreenScroller.setMaxOvershootPercent(mMaxOverShootPercent);
		mScreenScroller.setInterpolator(new DecelerateInterpolator(2.5f));
		mScreenScroller.setDuration(650);
		// 注册桌面滑屏效果设置的监听器，以此实现桌面滑动参数设置的监听
		// initScreenSettings();

		mScroller = new Scroller(mActivity, this);
		mScroller.setOrientation(Scroller.VERTICAL);
		mFunAppSetting = GOLauncherApp.getSettingControler().getFunAppSetting();

		final ViewConfiguration configuration = ViewConfiguration.get(mContext);
		mSwipTouchSlop = (int) (DrawUtils.sDensity * PAGING_TOUCH_SLOP + 0.5f) * 4;
		mSwipVelocity = configuration.getScaledMinimumFlingVelocity() * 4;
		mTouchSlop = configuration.getScaledTouchSlop();
		mVelocityTracker = new FastVelocityTracker();
		mUtils = AppFuncUtils.getInstance(mActivity);
	}

	// // 初始化应用桌面设置，包括注册桌面设置的监听器
	// private void initScreenSettings() {
	// // 注册
	// ScreenSettingControler controler = AppCore.getInstance(mContext)
	// .getScreenControler().getScreenSettingControler();
	// controler.registerObserver(this);
	//
	// // 读取桌面配置
	// setEffectSettings(controler.getEffectSettingInfo());
	//
	// }

	// private void setEffectSettings(EffectSettingInfo info) {
	// mMaxOverShootPercent = 0;//info.getOvershootAmount();
	// mScrollDuration = info.getDuration();
	// mScreenScroller.setMaxOvershootPercent(mMaxOverShootPercent);
	// mScreenScroller.setDuration(mScrollDuration);
	// }

	/**
	 * 初始化数据
	 */
	public void setAdapter(GBaseAdapter adapter) {
		mAdapter = adapter;
		if (mAdapter == null) {
			return;
		}
		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				updateLayoutParams();
				invalidateAndReInit();
			}

			@Override
			public void onInvalidated() {
				super.onInvalidated();
				updateLayoutParams();
				requestLayout();
			}
		});
	}

	/**
	 * 切换Tab重新初始化数据
	 */
	public void dataInit() {
		// LogUnit.i("dataInit");
		mFVisibleIndex = -1;
		mLVisibleIndex = -1;
		mTouchDownPostion = -1;
		mTouchUpPostion = -1;
		setFocused(false);

		onLongClickUp(false);
		mInDragStatus = false;
		// setDragStatus(false);//通知退出
		resetMotion(); // mOffsetMotion = null; 停止
		// mOffsetX = 0;
		// mOffsetY = 0;
		mScreenScroller.setCurrentScreen(0);
		mScroller.setScroll(0);
		mIsMotionRunning = false;

		mDragComponentIndex = -1;
		mDragComponentTargetIndex = -1;
		mDragComponentX = -1;
		mDragComponentY = -1;
		mIconMoveing = 0;
		mLastTouchX = 0;
		mLastTouchY = 0;
		mTotalHeight = 0;
	}

	/**
	 * 进行子组件布局，注意执行该方法前，必须确保所有参数都设置，如果没有设置，默认参数值为零。
	 */
	@Override
	public void layout(int left, int top, int right, int bottom) {
		if (AppFuncFrame.sVisible == false) {
			return;
		}
		setIconFolderReady(false);
		setPosition(left, top, right, bottom);
		applyVerticalEffect();
		mShowTwoLines = computeCellSize();
		// 总共格子数
		if (mAdapter == null || mCellWidth <= 0 || mCellHeight <= 0) {
			LogUnit.e("Illegal layout parameters");
			return;
		}

		mAdapter.loadApp();
		mAdapterCount = mAdapter.getCount();
		// LogUnit.i("mAdapterCount is " + mAdapterCount);
		// 当发现最新数据数量少于原数据数量，清理多余的列表数量(主要用于卸载)
		int componentSize = 0;
		synchronized (this) {
			componentSize = mStoreComponent.size();
			if (mAdapterCount < componentSize) {
				for (int i = componentSize - 1; i >= mAdapterCount; i--) {
					unRegisterComponentMsgHandler(mStoreComponent.get(i));
					removeComponent(i);
				}
			}
		}

		componentSize = mStoreComponent.size();
		if (componentSize > mAdapterCount) {
			try {
				throw new IllegalStateException(
						"mAdapterCount is less than mStoreComponent size!!!");
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		// 重新初始化行数和屏数
		mRows = -1;
		mScreen = -1;
		// LogUnit.e("Adapter size is " + mAdapterCount);

		if (mAdapterCount == 0) {
			mFocusedPosition = 0;
			return;
		}

		if (getAttachPanel() instanceof AppFuncFolder) {
			mIndicatorPos = ScreenIndicator.INDICRATOR_ON_TOP;
		} else {
			GoSettingControler goSettingControler = GOLauncherApp.getSettingControler();
			ScreenSettingInfo screenInfo = goSettingControler.getScreenSettingInfo();
			mIndicatorPos = screenInfo.mIndicatorPosition;
		}
		mIsVertical = mUtils.isVertical();

		if (mIsVScroll) {

			boolean isNew = true;
			// 计算行数
			int iconStartPos = 0;

			mRows = (mAdapterCount - 1) / mColunmNums + 1;
			iconStartPos = 0;
			mSearchRowHeightOffset = 0;

			// 下标
			int index = 0;
			for (int i = iconStartPos; i < mRows; i++) {
				for (int j = 0; j < mColunmNums; j++) {
					isNew = true;
					index = (i - iconStartPos) * mColunmNums + j;
					int x = mPaddingLeft + j * mCellWidth;
					int y = mPaddingTop + i * mCellHeight - mSearchRowHeightOffset;
					XComponent c = null;
					if (index < componentSize) {
						isNew = false;
						c = getChildAt(index);
						if (c == null) {
							continue;
						}
					}
					if (index < mAdapterCount) {
						XComponent temp = mAdapter.getComponent(index, x, y, mCellWidth,
								mCellHeight, c, this);
						if (temp instanceof BaseAppIcon) {
							BaseAppIcon component = (BaseAppIcon) temp;
							if (component != null) {
								component.setShowTwoLines(mShowTwoLines);
								component.resetLayoutParams(x, y, mCellWidth, mCellHeight);
							}
						}

						if (isNew) {
							if (temp != null) {
								addComponent(temp);
							}
						} else {
							if (temp != null) {
								replaceComponent(index, temp);
							} else {
								unRegisterComponentMsgHandler(mStoreComponent.get(i));
								removeComponent(index);
							}
						}
					} else {
						break;
					}
				}
			}
			XComponent c = getChildAt(getChildCount() - 1);
			if (c != null) {
				mTotalHeight = -1 * (c.mY + c.getHeight() - mRowNums * mCellHeight);
			}
			if (mTotalHeight > 0) {
				mTotalHeight = 0;
			}
			float ratio = getScrollerRatio();
			// 计算图标列表的实际高度
			mRealTotalHeight = mRowNums * mCellHeight - mTotalHeight - mPaddingTop;
			mScroller.setSize(mWidth, mHeight, mWidth, mRealTotalHeight);
			mScroller.setPadding(mPaddingTop, mPaddingBottom);
			mScroller.setScroll((int) (ratio * mScroller.getTotalHeight()));
			if (mVerticalEffector instanceof WaterFallEffector) {
				// FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
				// boolean showTabRow = handler.getShowTabRow() == 1;
				((WaterFallEffector) mVerticalEffector).setLayoutArg(-mUpperHeight, mUpperBottom,
						mLowerHeight, true, mCellHeight * 2, mRowNums, mCellHeight,
						mVerticalCycleMode);
			}

			updateVisiableRegion();

		} else {
			boolean isNew = true;
			// 下标
			int index = 0;
			// 图标开始屏幕索引
			int iconStartPos = 0;
			// 计算行数
			mScreen = (mAdapterCount - 1) / mSingleScreenCells + 1;
			iconStartPos = 0;
			for (int i = iconStartPos; i < mScreen; i++) {
				for (int j = 0; j < mRowNums; j++) {
					for (int k = 0; k < mColunmNums; k++) {
						isNew = true;
						index = (i - iconStartPos) * mSingleScreenCells + j * mColunmNums + k;
						int x = mPaddingLeft + i * mWidth + k * mCellWidth;
						int y = mPaddingTop + j * mCellHeight;
						XComponent c = null;
						if (index < componentSize) {
							isNew = false;
							c = getChildAt(index);
							if (c == null) {
								continue;
							}
						}
						if (index < mAdapterCount) {
							XComponent temp = mAdapter.getComponent(index, x, y, mCellWidth,
									mCellHeight, c, this);
							if (temp instanceof BaseAppIcon) {
								BaseAppIcon component = (BaseAppIcon) temp;
								if (component != null) {
									component.setShowTwoLines(mShowTwoLines);
									component.resetLayoutParams(x, y, mCellWidth, mCellHeight);
								}
							}
							if (isNew) {
								if (temp != null) {
									addComponent(temp);
								}
							} else {
								if (temp != null) {
									replaceComponent(index, temp);
								} else {
									unRegisterComponentMsgHandler(mStoreComponent.get(i));
									removeComponent(index);
								}
							}
						} else {
							break;
						}
					}
				}
			}
			mTotalHeight = (mScreen - 1) * -mWidth;
			updateVisiableRegion();

		}

		// if (mFunAppSetting.getShowActionBar() == FunAppSetting.ON) {
		// if (!mLockScreen) {
		// AppFuncHomeComponent homeComponent = AppFuncHandler
		// .getInstance().getCurrentHomeComponent();
		// XComponent content = XViewFrame.getInstance()
		// .getAppFuncMainView().getSeletedTabContentView();
		// if ((content instanceof AllAppTabBasicContent || content instanceof
		// ProManageTabBasicContent)
		// && !(mAttchPanel instanceof AppFuncFolder)) {
		// homeComponent.switchDeskToHome(false);
		// }
		// }
		// }
		// 编辑状态下，增加新的的图标都有抖动
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (mInDragStatus) {
			for (XComponent component : mStoreComponent) {
				// 通知组件进入编辑状态
				if (component instanceof BaseAppIcon) {
					((BaseAppIcon) component).setEditMode(true);
				}
			}
//			XComponent component = XViewFrame.getInstance().getAppFuncMainView()
//					.getSeletedTabContentView();
			// 以下代码个人认为无用，先注释wuziyi
//			XComponent component = null;
//			XViewFrame xViewFrame = XViewFrame.getInstance();
//			if (xViewFrame != null) {
//				component = xViewFrame.getAppFuncMainView().getSeletedTabContentView();
//			}
//			if (handler != null && handler.isShowActionBar()) {
//				if (component != null) {
//					if (component instanceof AllAppTabBasicContent) {
//						AppFuncHandler.getInstance().setHomeIconChangeDesk(true, false);
//					} else if (component instanceof ProManageTabBasicContent) {
//						AppFuncHandler.getInstance().setHomeIconChangeDesk(false, false);
//					}
//				}
//			}
//
//			if (handler != null && handler.isShowTabRow()) {
//				if (component != null) {
//					if (component instanceof AllAppTabBasicContent) {
////						if (!(mAttchPanel instanceof AppFuncFolder)) {
//							AppFuncHandler.getInstance().setTopChange(true, false);
////						}
//					} else if (component instanceof ProManageTabBasicContent) {
//						AppFuncHandler.getInstance().setTopChange(false, false);
//					}
//				}
//			}
		} else {
//			XComponent component = XViewFrame.getInstance().getAppFuncMainView()
//					.getSeletedTabContentView();
			// 以下代码个人认为无用，先注释wuziyi
			XComponent component = null;
			XViewFrame xViewFrame = XViewFrame.getInstance();
			if (xViewFrame != null) {
				component = xViewFrame.getAppFuncMainView().getSeletedTabContentView();
			}
//			if (handler != null && handler.isShowActionBar()) {
//				if (component != null && component instanceof AllAppTabBasicContent
//						&& !(mAttchPanel instanceof AppFuncFolder)) {
//					AppFuncHandler.getInstance().setHomeIconChangeDesk(false, false);
//				}
//			}
			if (handler != null && handler.isShowTabRow()) {
				if (component != null) {
					if (component instanceof AllAppTabBasicContent
							|| component instanceof ProManageTabBasicContent) {
						AppFuncHandler.getInstance().setTopChange(false, false);
					}
				}
			}
		}
		// LogUnit.i("mTotalHeight : " + mTotalHeight);
		// 如果支持拖拽，更新拖拽组件的原始位置
		if (mIsDragable && mInDragStatus && mDragComponent != null) {
			int indexOfComponent = indexOfComponent(mDragComponent);
			LogUnit.i("mDragComponent index" + indexOfComponent);
			if (indexOfComponent != -1) {
				final XComponent dragComponent = getChildAt(indexOfComponent);
				if (dragComponent instanceof ProManageIcon) {
					((ProManageIcon) dragComponent).changeAlpha(0, 255, 30, null, this, false);
					((ProManageIcon) dragComponent).setInEdge(false);
				}
				mDragComponentX = dragComponent.mX;
				mDragComponentY = dragComponent.mY;
				((BaseAppIcon) mDragComponent).onLongClickUp(mDragComponentX, mDragComponentY,
						null, this);
				((BaseAppIcon) mDragComponent).setInEdge(false);
				// mDragComponent = null;
				setDragComponent(null);
				mDragComponentIndex = -1;
				mDragComponentTargetIndex = -1;
				mLastTouchX = 0;
				mLastTouchY = 0;
			} else {
				// DeliverMsgManager.getInstance().onChange(
				// AppFuncConstants.ALLAPPS_TABCONTENT,
				// AppFuncConstants.ONLONGCLICKUP, null);
				// mDragComponent = null;
				setDragComponent(null);
				mDragComponentIndex = -1;
				mDragComponentTargetIndex = -1;
				mLastTouchX = 0;
				mLastTouchY = 0;
			}
		}
		if (mFocusedPosition >= mAdapterCount) {
			mFocusedPosition = mAdapterCount - 1;
			setItemFocus(true);
		}
		initIndicator(mIndicatorPos);
		if (!mLockScreen) {
			resetPreviewScreens();
		}
	}

	protected boolean computeCellSize() {

		// 实际宽度
		mRealWidth = mWidth - mPaddingLeft - mPaddingRight;
		// 实际高度
		mRealHeight = mHeight - mPaddingTop - mPaddingBottom;
		if (mColunmNums == 0) {
			mCellWidth = mRealWidth;
		} else {
			// 格子宽度
			mCellWidth = mRealWidth / mColunmNums;
		}

		if (mRowNums == 0) {
			mCellHeight = mRealHeight + mUpperHeight;
		} else {
			// 格子高度，在使用waterfall特效的时候因为改变了控件高度，这里需要使用原来的高度
			mCellHeight = Math.round((mRealHeight + mUpperHeight) / mRowNums * 1.0f);
		}

		int wholeIconHeight = getWholeIconHeight(); // 包含两行文字的图标高度
		int fixedIconHeight = wholeIconHeight
				- (int) (GoLauncher.getAppFontSize() * DrawUtils.sDensity); // 包含一行文字的图标高度
		boolean showTwoLines = true;
		if (mCellHeight < wholeIconHeight && mRowNums > 1) {
			int height = Math.round((mRealHeight - fixedIconHeight + mUpperHeight)
					/ (mRowNums - 1.0f)); // 保持最后一行不会超出显示范围
			if (height <= fixedIconHeight) {
				// 算出的高度连包含一行文字的图标都放不下
				mCellHeight = height;
			}
			showTwoLines = false;
		}

		return showTwoLines;
	}

	private void resetPreviewScreens() {
		AppFuncHomeComponent homeComponent = AppFuncHandler.getInstance().getCurrentHomeComponent();
		if (homeComponent != null) {
			XComponent content = homeComponent.getInScreenContent();
			if (content != null && content instanceof AppMoveToDesk) {
				((AppMoveToDesk) content).resetAllScreenIconStatus();
			}
		}
	}

	/**
	 * 初始化指示器
	 */
	protected void initIndicator(String position) {
		mIsHasScroller = false;
		if (mIndicator == null) {
			mIndicator = new MIndicator(1, 0, 0, 1, 1, mActivity);
			mIndicator.setIndicatorListner(this);
		}
		if (mIsVScroll && mRows > mRowNums)// 竖滑
		{
			mIndicator.setXY(mPaddingLeft + mRealWidth - mIndicatorSize, 0);
			mIndicator.setSize(mIndicatorSize, mHeight - mUpperBottom);
			int curRow = mFVisibleIndex / mColunmNums; // 当前行
			mIndicator.setParameter(VERTICAL, mRows, curRow, mRowNums, mCellHeight);
			mIndicator.setCurPosition(-mOffsetY); // 这里由于指示器初始化的时候是根据当前行数设置位置，可能会有偏差，再根据当前mOffsetY设置一次
			addComponent(mIndicator);
			mIsHasScroller = true;
		} else if (!mIsVScroll && mScreen > 1)// 横滑
		{
			if (ScreenIndicator.INDICRATOR_ON_TOP.equals(position)) {
				// //指示器在上面
				mIndicator.setXY(0, (int) (mPaddingTop * 0.5f));
				if (mOrientation == VERTICAL) {
					mIndicator.setSize(mWidth, mPaddingTop);
				} else {
					mIndicator.setSize(mWidth - mPaddingRight, mPaddingTop);
				}
			} else {
				// //指示器在下面
				mIndicator.setXY(0, mHeight - (int) (mPaddingBottom * 1.3f));
				if (mOrientation == VERTICAL) {
					mIndicator.setSize(mWidth, mPaddingBottom);
				} else {
					mIndicator.setSize(mWidth - mPaddingRight, mPaddingBottom);
				}
			}

			int curScreem = mFVisibleIndex / mSingleScreenCells;

			mIndicator.setParameter(HORIZONTAL, mScreen, curScreem, 1);
			addComponent(mIndicator);
			mIsHasScroller = true;
		}
	}

	/**
	 * 移除指示器
	 */
	protected void removeIndicator() {
		int size = mStoreComponent.size();
		if (size > 0) {
			XComponent component = getChildAt(size - 1);
			if (component instanceof MIndicator) {
				removeComponent(component);
			}
		}
	}

	/**
	 * 更新布局数据
	 */
	public void updateLayoutParams() {
		mColunmNums = mNewColunmNums;
		mRowNums = mNewRowNums;
		// 单屏显示单元格数量
		mSingleScreenCells = mColunmNums * mRowNums;
		// if (mOrientation != mNewOrientation) {
		// resetScroller();
		// }
		mOrientation = mNewOrientation;
		if (mOrientation == HORIZONTAL) {
			int temp = mColunmNums;
			mColunmNums = mRowNums;
			mRowNums = temp;
		}
		mPaddingLeft = mNewPaddingLeft;
		mPaddingRight = mNewPaddingRight;
		mPaddingTop = mNewPaddingTop;
		mPaddingBottom = mNewPaddingBottom;
		resetMotion();
		// mOffsetX = 0;
		// mOffsetY = 0;
		mFVisibleIndexBak = mFVisibleIndex;
		mScreenScroller.setCurrentScreen(0);
		// 初始化
		removeIndicator();
		if (mIsGotoInfo) {
			setDragStatus(true);
			mIsGotoInfo = false;
			if (mAdapterCount == 0) {
				XViewFrame.getInstance().getAppFuncMainView().setDragComponent(null);
			}
		} else {
			setDragStatus(mInDragStatus);
		}
		XViewFrame.addQueue(new MsgEntity(0, AppFuncConstants.POST_REPANIT, null));
		resetOrientation();
	}

	/**
	 * 单独刷新某个文件夹ID的图片
	 * 
	 * @param index
	 */
	public void refreshCell(long folderId) {
		int i = 0;
		for (XComponent component : mStoreComponent) {
			if (component instanceof ApplicationIcon) {
				ApplicationIcon icon = (ApplicationIcon) component;
				if (icon.isFolder() && icon.getID() == folderId) {
					mAdapter.getComponent(i, component.mX, component.mY, mCellWidth, mCellHeight,
							component, this);
					break;
				}
			}
			i++;
		}
	}

	/**
	 * 重新布局和清理所有数据
	 */
	public synchronized void invalidateAndReInit() {
		dataInit();
		updateLayoutParams();
		requestLayout();
	}

	/**
	 * 根据最新的布局，重新调整显示区
	 */
	protected void updateVisiableRegion() {
		int index = mFVisibleIndex;
		if (mIsSupportScroll) {
			if (mFVisibleIndexBak != -1) {
				mFVisibleIndex = mFVisibleIndexBak;
				mFVisibleIndexBak = -1;
			}
			if (mIsVScroll) {

				if (mLVisibleIndex - mFVisibleIndex < mSingleScreenCells - mColunmNums
						&& mFVisibleIndex <= mLVisibleIndex) {
					// 补齐最后显示一行不足的组件下标
					int tempLVisibleIndex = mLVisibleIndex
							+ (mColunmNums - 1 - mLVisibleIndex % mColunmNums);

					mFVisibleIndex = tempLVisibleIndex - (mSingleScreenCells - 1);
				} else if (mFVisibleIndex > mLVisibleIndex) {
					mFVisibleIndex = mFVisibleIndex - (mFVisibleIndex % mColunmNums);
					mLVisibleIndex = mFVisibleIndex + mSingleScreenCells - 1;
					fixLastIndex();
				}

				if (mFVisibleIndex < 0) {
					mFVisibleIndex = -1;
				}
				mCurRow = mFVisibleIndex / mColunmNums;

				if (index != mFVisibleIndex) {
					// 只有图标位置改变了才调整垂直位置
					// 否则在垂直偏移一点之后，长按图标并松开会导致调整
					// 因为onLongClickUp方法中调用了requestLayout
					// mScroller.setScroll(mCurRow * mCellHeight);
				}
				mScreenScroller.setCurrentScreen(0); // 取消另一方向的偏移量
				updateVisiblePositionV();
			} else {
				mScreenScroller.setScreenSize(mWidth, mHeight);

				mScreenScroller.setScreenCount(mScreen);

				if (mFVisibleIndex >= mAdapterCount) {
					mFVisibleIndex = mAdapterCount - 1;
				}
				boolean isVertical = mUtils.isVertical();
				int remainder = mFVisibleIndex % mSingleScreenCells;

				if (mFVisibleIndex == 0 || remainder == 0) {
					mCurScreen = mFVisibleIndex / mSingleScreenCells;
				} else {
					// 是横竖屏切换
					if (isVertical) {
						mFVisibleIndex = mFVisibleIndex + remainder;
						mCurScreen = mFVisibleIndex / mSingleScreenCells;

						if (mCurScreen >= mScreen) {

							mCurScreen -= 1;
						}
					} else {
						mFVisibleIndex = mFVisibleIndex - remainder;
						mCurScreen = mFVisibleIndex / mSingleScreenCells;
					}
				}
				mScreenScroller.setCurrentScreen(mCurScreen);
				updateVisiblePositionH();
				mScroller.setScroll(0); // 取消另一方向的偏移量
			}
		}
	}

	@Override
	protected synchronized void drawCurrentFrame(Canvas canvas) {
		if (mAdapterCount == 0) {
			return;
		}
		canvas.save();
		canvas.clipRect(0, mClipTop, mWidth, mClipBottom);
		canvas.save();
		canvas.translate(mOffsetX, mOffsetY);

		final int showStartX = getAbsX();
		final int showStartY = getAbsY();
		final int showEndX = showStartX + mWidth;
		final int showEndY = showStartY + mHeight;

		if (!mIsVScroll) {
			if (mScreenScroller.isFinished()) {
				// 拖动图标时，其他图标会做平移动画，滚动器设置的特效器不支持
				// 增加了getCurrentScreenOffset是否为0的判断是因为，
				drawHorizontalScreen(canvas, mCurScreen, 0);
			} else {
				// 横向切屏时，使用特效绘制
				mScreenScroller.onDraw(canvas);
			}
			mScreenScroller.invalidateScroll();
		} else {
			int saveCount = canvas.save();
			if (mVerticalEffectorType == NO_VERTICAL_EFFECTOR) {
				onDraw(canvas, -mOffsetY, -mOffsetY + mClipBottom, VerticalListContainer.PART_MID);
			} else if (mVerticalEffector != null) {
				mVerticalEffector.onDraw(canvas);
			}
			canvas.restoreToCount(saveCount);
			mScroller.invalidateScroll();
		}

		if (mDrawFadeRect || (mDrawHighlightRect && mIsMoveStop)) {
			// 在边缘绘制淡出背景
			XViewFrame bgView = XViewFrame.getInstance();
			int padding = mUtils.getDimensionPixelSize(sPaddingSizeId);
			if (bgView != null) {
				if (mIsVScroll) {
					canvas.translate(-mOffsetX - showStartX, -mOffsetY - showStartY);
					if (mDrawFadeRect) {
						int top1 = showStartY, bottom1 = showStartY + mPaddingTop;
						int top2 = showEndY - mPaddingBottom, bottom2 = showEndY;
						if (mVerticalEffectorType == WATERFALL_VERTICAL_EFFECTOR) {
							bottom1 = showStartY + mUpperBottom;
							top1 = showStartY - mUpperHeight;
							bottom2 = bgView.getHeight();
							top2 = bottom2 - mLowerHeight;
						}
						mFadeRect.set(showStartX, top1, showEndX - mIndicatorSize, bottom1);
						bgView.drawFadeRect(canvas, mFadeRect, FadePainter.DIR_FROM_TOP);
						mFadeRect.set(showStartX, top2, showEndX - mIndicatorSize, bottom2);
						bgView.drawFadeRect(canvas, mFadeRect, FadePainter.DIR_FROM_BOTTOM);
					}
					if (mDrawHighlightRect && mIsMoveStop) {
						final int color = AppFuncConstants.ICON_IN_EDGE_COLOR;
						if (mYMoveStop < mHeight / 2) {
							mFadeRect.set(showStartX, showStartY, showEndX - mIndicatorSize,
									showStartY + padding);
							bgView.drawHighlightRect(canvas, mFadeRect, FadePainter.DIR_FROM_TOP,
									color);
						} else {
							mFadeRect.set(showStartX, showEndY - padding,
									showEndX - mIndicatorSize, showEndY);
							bgView.drawHighlightRect(canvas, mFadeRect,
									FadePainter.DIR_FROM_BOTTOM, color);
						}
					}
				} else if (!mIsVScroll) {
					canvas.translate(-mOffsetX - showStartX, -mOffsetY - showStartY);
					if (mDrawFadeRect) {
						mFadeRect.set(showStartX, showStartY, showStartX + mPaddingLeft, showEndY);
						bgView.drawFadeRect(canvas, mFadeRect, FadePainter.DIR_FROM_LEFT);
						mFadeRect.set(showEndX - mPaddingRight, showStartY, showEndX, showEndY);
						bgView.drawFadeRect(canvas, mFadeRect, FadePainter.DIR_FROM_RIGHT);
					}
					if (mDrawHighlightRect && mIsMoveStop) {
						final int color = AppFuncConstants.ICON_IN_EDGE_COLOR;
						if (mXMoveStop < mWidth / 2) {
							mFadeRect.set(showStartX, showStartY, showStartX + padding, showEndY);
							bgView.drawHighlightRect(canvas, mFadeRect, FadePainter.DIR_FROM_LEFT,
									color);
						} else {
							mFadeRect.set(showEndX - padding, showStartY, showEndX, showEndY);
							bgView.drawHighlightRect(canvas, mFadeRect, FadePainter.DIR_FROM_RIGHT,
									color);
						}
					}
				}
			}
		}
		canvas.restore();
		if (mIndicator != null && mIsHasScroller) {
			if (mIsVScroll && (mIsMotionRunning || mShowIndicator)) {
				mIndicator.paintCurrentFrame(canvas, mIndicator.mX, mIndicator.mY);
			} else if (!mIsVScroll) {
				mIndicator.paintCurrentFrame(canvas, mIndicator.mX, mIndicator.mY);
			}
		}
		canvas.restore();
		if (mIsDragable && mInDragStatus && mDragComponent != null) {
			XViewFrame.getInstance().getAppFuncMainView().setDragComponent(mDragComponent);
			// mDragComponent.paintCurrentFrame(canvas, mDragComponent.mX,
			// mDragComponent.mY);
		} else {
			XViewFrame.getInstance().getAppFuncMainView().setDragComponent(null);
		}

		// drawSearchPage(canvas);
	}

	@Override
	public synchronized boolean onKey(KeyEvent event) {
		if (mLockScreen) {
			return true;
		}
		// 处理返回按钮
		if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mIsDragable && mInDragStatus) {
				// 离开可拖动状态
				setDragStatus(false);
				FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
				if (handler != null) {
					if (handler.isShowActionBar()) {
						AppFuncHandler.getInstance().setHomeIconChangeDesk(false, true);
					}
					if (handler.isShowTabRow()) {
						AppFuncHandler.getInstance().setTopChange(false, true);
					} else {
						AppFuncHandler.getInstance().setTopChange(false, false);
					}
				}
				// XComponent component =
				// mAppFuncMainView.getSeletedTabContentView();
				// if (mShowAddButton) {
				// if (mInDragStatus) {
				// ((FolderAdapter)mAdapter).setShowAddButton(false);
				// } else {
				// ((FolderAdapter)mAdapter).setShowAddButton(true);
				// }
				// DeliverMsgManager.getInstance().onChange(
				// AppFuncConstants.APPFOLDER,
				// AppFuncConstants.LAYOUTFOLDERGRID, null);
				// }
			} else {
				boolean handled = false;
				if (!handled) {
					if (ApplicationIcon.sIsStartApp == false) {
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
								AppFuncConstants.EXITAPPFUNCFRAME, null);
					}
				}
			}
			return true;
		}

		ApplicationIcon.sIsStartApp = false;

		if (mGridId == AppFuncConstants.ALLAPPS_GRID) {
			// 处理搜索键
			if (event.getAction() == KeyEvent.ACTION_UP
					&& event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
				return true;
			}
		}

		boolean isHandled = false;
		return commonKey(event) || isHandled;
	}

	/**
	 * 处理键盘引起焦点改变和键盘启动应用程序
	 * 
	 * @param count
	 * @param event
	 * @return
	 */
	private boolean commonKey(KeyEvent event) {
		if (mAdapter == null && mAdapterCount <= 0) {
			return false;
		}
		int action = event.getAction();
		if (action != KeyEvent.ACTION_UP) {
			// 沒有得到焦点，初始化焦点位置
			if (mFocusedPosition == -1) {
				return false;
			} else
			// 本来就有焦点
			{
				boolean isHandle = true;
				switch (event.getKeyCode()) {
					case KeyEvent.KEYCODE_DPAD_UP : {
						if (mIsSupportScroll && mIsVScroll) {
							if (mFocusedPosition / mColunmNums > 0) // 不在第一行
							{
								setItemFocus(false);
								mFocusedPosition = mFocusedPosition - mColunmNums;
								setItemFocus(true);
								handleFocusChange(true);
							} else {
								if (mOrientation == VERTICAL) {
									setItemFocus(false);
									mFocusedPosition = -1;
									// AppFuncHandler.getInstance().setTabHasFocus();
									if (mEventListener != null) {
										mEventListener.onEventFired(this, EventType.FOCUSEVENTPASS,
												null, 0, null);
									}
									setFocused(false);
								} else {
									boolean b = AppFuncHandler.getInstance().isInFolder(this);
									if (b) {
										setItemFocus(false);
										mFocusedPosition = -1;
										setFocused(false);
									}
								}
							}
						} else {
							int row = mFocusedPosition / mColunmNums;
							if (row % mRowNums != 0) {
								setItemFocus(false);
								mFocusedPosition = mFocusedPosition - mColunmNums;
								setItemFocus(true);
								handleFocusChange(true);
							} else {
								if (mOrientation == VERTICAL) {
									setItemFocus(false);
									mFocusedPosition = -1;
									// AppFuncHandler.getInstance().setTabHasFocus();
									if (mEventListener != null) {
										mEventListener.onEventFired(this, EventType.FOCUSEVENTPASS,
												null, 0, null);
									}
									setFocused(false);
								}
							}
						}
					}
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN : {
						if (mFocusedPosition < 0) {
							break;
						}
						// 不是最后一行
						if (mIsSupportScroll && mIsVScroll) {
							if (mFocusedPosition / mColunmNums < (mRows - 1)) {
								setItemFocus(false);
								mFocusedPosition = mFocusedPosition + mColunmNums;
								mFocusedPosition = mFocusedPosition < mAdapterCount
										? mFocusedPosition
										: mAdapterCount - 1;
								setItemFocus(true);
							}
							handleFocusChange(true);
						} else {
							int row = mFocusedPosition / mColunmNums;
							if (row % mRowNums != (mRowNums - 1)
									&& (mAdapterCount - 1) / mColunmNums != mFocusedPosition
											/ mColunmNums) {
								setItemFocus(false);
								mFocusedPosition = mFocusedPosition + mColunmNums;
								mFocusedPosition = mFocusedPosition < mAdapterCount
										? mFocusedPosition
										: mAdapterCount - 1;
								setItemFocus(true);
							}
						}
					}
						break;
					case KeyEvent.KEYCODE_DPAD_LEFT : {
						int screemItem = mColunmNums * mRowNums;
						if (mOrientation == VERTICAL) // 竖屏
						{
							// 到达边界不会自动换行
							if (mFocusedPosition % mColunmNums != 0) {
								setItemFocus(false);
								mFocusedPosition--;
								setItemFocus(true);
							} else if (mIsSupportScroll && !mIsVScroll) {
								if (mFocusedPosition >= screemItem) {
									setItemFocus(false);
									mFocusedPosition = mFocusedPosition - screemItem + mColunmNums
											- 1;
									setItemFocus(true);
									handleFocusChange(true);
								}
							}
						} else
						// 横向
						{
							// 焦点在每行第一个格下,焦点切换到Tab
							if (mFocusedPosition % mColunmNums == 0) {
								if (mIsSupportScroll && !mIsVScroll
										&& mFocusedPosition >= screemItem) {
									setItemFocus(false);
									mFocusedPosition = mFocusedPosition - screemItem + mColunmNums
											- 1;
									setItemFocus(true);
									handleFocusChange(true);
								} else {
									boolean b = AppFuncHandler.getInstance().isInFolder(this);
									if (!b) {
										setItemFocus(false);
										mFocusedPosition = -1;
										// AppFuncHandler.getInstance()
										// .setTabHasFocus();
										if (mEventListener != null) {
											mEventListener.onEventFired(this,
													EventType.FOCUSEVENTPASS, null, 0, null);
										}

										setFocused(false);
									}
								}
							} else {
								setItemFocus(false);
								mFocusedPosition--;
								setItemFocus(true);
							}
						}
						isHandle = true;
					}
						break;
					case KeyEvent.KEYCODE_DPAD_RIGHT : {
						int screemItem = mColunmNums * mRowNums;
						if (mFocusedPosition < mAdapterCount - 1) {
							// 到达边界不会自动换行
							if (mFocusedPosition % mColunmNums != mColunmNums - 1) {
								setItemFocus(false);
								mFocusedPosition++;
								setItemFocus(true);
							} else if (mIsSupportScroll && !mIsVScroll) {
								if (mFocusedPosition < (mScreen - 1) * screemItem) {
									setItemFocus(false);
									mFocusedPosition = mFocusedPosition + screemItem - mColunmNums
											+ 1;
									mFocusedPosition = mFocusedPosition < mAdapterCount
											? mFocusedPosition
											: mFocusedPosition / screemItem * screemItem;
									setItemFocus(true);
									handleFocusChange(true);
								}
							}
						}

					}
						break;
					case KeyEvent.KEYCODE_DPAD_CENTER :
					case KeyEvent.KEYCODE_ENTER : {
						if (mFocusedPosition >= 0) {
							// 触发按下事件
							// setItemPress(false);
							mTouchDownPostion = mFocusedPosition;
							// setItemPress(true);
						}
					}
						break;
					default :
						isHandle = false;
				}
				return isHandle;
			}
		} else {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_DPAD_CENTER :
				case KeyEvent.KEYCODE_ENTER : {
					if (!isOutOfIndex(mFocusedPosition, false)) {
						// 触发按下事件
						// setItemPress(false);
						mTouchDownPostion = mFocusedPosition;
						XComponent xComponent = getChildAt(mTouchDownPostion);
						if (xComponent.getEventListener() != null) {
							xComponent.getEventListener().onEventFired(this, EventType.CLICKEVENT,
									null, 0, null);
						}
					}
				}
			}
		}
		return false;
	}

	protected boolean onGestureEvent(MotionEvent ev) {
		if (!mIsVScroll && mDragComponent == null && !mIsScrolling
				&& !XViewFrame.getInstance().getAppFuncMainView().isFolderShow() && !mLockScreen) {
			final float x = ev.getX();
			final float y = ev.getY();
			final int action = ev.getAction() & MotionEvent.ACTION_MASK;
			switch (action) {
				case MotionEvent.ACTION_DOWN : {
					mInterceptTouchDownX = x;
					mInterceptTouchMoveX = 0;
					mInterceptTouchDownY = y;
					mInterceptTouchMoveY = 0;
					mInterceptTouchMoved = false;
					mTouchState = TOUCH_STATE_NORMAL;
					break;
				}
				case MotionEvent.ACTION_MOVE : {
					mTouchState = TOUCH_STATE_NORMAL;
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
					if (mInterceptTouchMoved) {
						if (Math.abs(y - mInterceptTouchDownY) > mSwipTouchSlop) {
							if (mInterceptTouchVY > mSwipVelocity) {
								// 下滑
								mTouchState = TOUCH_STATE_GLIDE_DOWN;
							} else if (mInterceptTouchVY < -mSwipVelocity) {
								// 上滑
								mTouchState = TOUCH_STATE_GLIDE_UP;
							}
						}
					}
					break;
				}
				case MotionEvent.ACTION_UP : {
					switch (mTouchState) {
						case TOUCH_STATE_GLIDE_DOWN :
							onGlideDown();
							break;
						case TOUCH_STATE_GLIDE_UP :
							onGlideUp();
							break;
					}
					mTouchState = TOUCH_STATE_NORMAL;
					break;
				}
			}
			return mTouchState != TOUCH_STATE_NORMAL;
		}
		return false;
	}

	protected void onGlideUp() {
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (handler != null) {
			if (handler.isGlideUpActionEnable()) {
				if (handler.isShowTabRow()) {
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IDiyMsgIds.HIDE_TAB_ROW, 0, null, null);
				} else {
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IDiyMsgIds.SHOW_TAB_ROW, 0, null, null);
				}
			}
		}
	}

	protected void onGlideDown() {
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (handler != null) {
			if (handler.isGlideDownActionEnable()) {
				if (handler.isShowActionBar()) {
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IDiyMsgIds.HIDE_ACTION_BAR, 0, null, null);
				} else {
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IDiyMsgIds.SHOW_ACTION_BAR, 0, null, null);
				}
			}
		}
	}

	@Override
	public synchronized boolean onTouch(MotionEvent event) {

		if (mFocusedPosition > -1) {
			setFocused(false);
			mFocusedPosition = -1;
		}
		// isMoveStop = false;
		if (mIsFolderEnable && mIconMoveing == -1) {
			return false;
		}
		int eX = (int) event.getX(0);
		int eY = (int) event.getY(0);
		// 相对于屏幕偏移
		int x = eX - getAbsX();
		int y = eY - getAbsY();
		int tempX = x + mX;
		int tempY = y + mY;

		final int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			mDispatchTouchToIndicator = false;
			if (ScreenIndicator.INDICRATOR_ON_TOP.equals(mIndicatorPos)) {
				if (null != mIndicator && mIndicator.getMode() == MIndicator.MODE_H
						&& x >= mIndicator.getContentStartX() + mIndicator.mX
						&& x <= mIndicator.getContentEndX() + mIndicator.mX && y >= 0
						&& y <= mIndicator.mY * 2 + mIndicator.getHeight()) {
					mDispatchTouchToIndicator = true;
				}
			} else {
				if (null != mIndicator && mIndicator.getMode() == MIndicator.MODE_H
						&& x >= mIndicator.getContentStartX() + mIndicator.mX
						&& x <= mIndicator.getContentEndX() + mIndicator.mX && y >= mIndicator.mY
						&& y <= mIndicator.mY + 1.5f * mIndicator.getHeight()) {
					mDispatchTouchToIndicator = true;
				}
			}
		}
		if (mDispatchTouchToIndicator) {

			// 指示器处理了，其他组件不处理
			if (mInDragStatus) {
				return true;
			}
			mIndicator.onTouch(event);
			return true;
		} else {
			if (onGestureEvent(event)) {
				return true;
			}
		}
		// 非组件区域事件，不处理
		if (contains(tempX, tempY)) {
			if (action == MotionEvent.ACTION_DOWN) {
				// LogUnit.i("Motion Down");
				touchDown(event, x, y);
				mDetector.onTouchEvent(event);
				// mShowIndicator = true;
				return true;
			}
			// 移动事件
			else if (action == MotionEvent.ACTION_MOVE) {
				// LogUnit.i("Motion Move");
				boolean touchMove = touchMove(event, x, y, true);
				boolean onTouchEvent = mDetector.onTouchEvent(event);
				// mShowIndicator = true;
				return touchMove || onTouchEvent;
				// return true;
			} else {
				// LogUnit.i("Motion Up");
				// mShowIndicator = false;
				boolean onTouchEvent = mDetector.onTouchEvent(event);
				boolean touchUp = touchUp(event, x, y);
				// 这段代码看着有点怪，但是不要把mLongPressed = false;移到条件语句外面。
				if (mLongPressed) {
					mLongPressed = false;
					// showAddButton(true);
				}
				// else{
				// mLongPressed = false;
				// // if (mAddButtonHided==true) {
				// // showAddButton(true);
				// // }
				// }
				// boolean onTouchEvent = false;
				// boolean touchUp = false;
				// if (action == MotionEvent.ACTION_CANCEL) {
				// onTouchEvent = detector.onTouchEvent(event);
				// touchUp = touchUp(event, 0, 0);
				// } else {
				// onTouchEvent = detector.onTouchEvent(event);
				// touchUp = touchUp(event, x, y);
				// }
				// //这段代码看着有点怪，但是不要把mLongPressed = false;移到条件语句外面。
				// if (mLongPressed) {
				// mLongPressed = false;
				// showAddButton(true);
				// }else{
				// mLongPressed = false;
				// if (mAddButtonHided==true) {
				// showAddButton(true);
				// }
				// }
				return touchUp || onTouchEvent;
				// return true;
			}
		} else {
			// int width = AppFuncUtils.getInstance((Activity) mContext)
			// .getScreenWidth();
			// int height = AppFuncUtils.getInstance((Activity) mContext)
			// .getScreenHeight();
			// if (eX >= width || eX < 0 || eY >= height || eY < 0) {
			// resetIconPos(event);
			// onSingleTapUp(event, -1, -1);
			// if (mIsDragable && mInDragStatus && mDragComponent != null) {
			// onLongClickUp(true);
			// return true;
			// }
			// }
			if (action == MotionEvent.ACTION_MOVE) {
				// LogUnit.i("Motion Move");
				if (mIsScrolling) {
					touchUp(event, x, y);
					mIsScrolling = false;
				} else {
					mIsScrolling = false;
					boolean touchMove = touchMove(event, x, y, false)
							|| mDetector.onTouchEvent(event);
				}
				return true;
			} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
				if (!touchUp(event, x, y)) {
					onSingleTapUp(event, -1, -1);
				}
				mLongPressed = false;
				return true;
			}
		}
		return false;
	}

	protected boolean touchDown(MotionEvent event, int x, int y) {
		if (mIsSupportScroll) {
			if (mIsVScroll) {
				if (!mScroller.isFinished()) {
					mIsScrolling = true;
					mScroller.onTouchEvent(event, MotionEvent.ACTION_DOWN);
				}
			} else {
				if (!mScreenScroller.isFinished()) {
					mIsScrolling = true;
					mScreenScroller.onTouchEvent(event, MotionEvent.ACTION_DOWN);
				}
			}
		}
		mIsStopResponse = false;
		mLastTouchX = x;
		mLastTouchY = y;
		mDownX = x;
		mDownY = y;
		boolean isHandle = false;
		isHandle = onDown(event, x, y);
		return isHandle;
	}

	/**
	 * 
	 * @param event
	 * @param x
	 * @param y
	 * @param isInGrid
	 *            位置坐标是否在组件内
	 * @return
	 */
	protected boolean touchMove(MotionEvent event, int x, int y, boolean isInGrid) {
		XComponent component = mDragComponent;
		if (mIsDragable && mInDragStatus && component != null) {
			boolean isInEdge = false;
			final boolean isMoveStopOld = mIsMoveStop;
			mIsMoveStop = false;
			if (mLastTouchX == 0) {
				mLastTouchX = x;
			} else {
				component.offsetLeftAndRight(x - mLastTouchX);
				mLastTouchX = x;
			}
			if (mLastTouchY == 0) {
				mLastTouchY = y;
			} else {
				component.offsetTopAndBottom(y - mLastTouchY);
				mLastTouchY = y;
			}
			// component.setXY((int) (x - component.getWidth() * 0.5f),
			// (int) (y - component.getHeight() * 0.5f));
			// ////////////////////////额外代码//////////////////////
			int centerX = component.centerX();
			int centerY = component.centerY();

			int offsetX = Math.abs(centerX - x);
			int offsetY = Math.abs(centerY - y);
			int halfWidth = component.getWidth() / 2;
			int halfHeight = component.getHeight() / 2;
			// 若图标中点与手指触碰点的距离大于宽或高的一半时，重置图标位置到手指背后。
			// 用于避免由于某种原因导致图标飞出屏幕消失的问题
			if (offsetX > halfWidth || offsetY > halfHeight) {
				component.setXY(x - halfWidth, (int) (y - 1.5f * halfHeight));
			}

			final int absX = getAbsX();
			final int absY = getAbsY();

			int cx = centerX + absX;
			int cy = centerY + absY;

			final long time = mScheduler.getClock().getTime();
			boolean isOverlap = false;
			XComponent content = null;
			FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
			if (handler != null && handler.isShowActionBar()) {
				isOverlap = AppFuncHandler.getInstance().checkDragComponentHomeIconOverlap(cx, cy);
				AppFuncHomeComponent homeComponent = AppFuncHandler.getInstance()
						.getCurrentHomeComponent();

				if (homeComponent != null) {
					content = homeComponent.getInScreenContent();
				}
			}
			if (isOverlap) {
				setIconFolderReady(false);
				if (!sIsExit) {
					if (content != null && content instanceof AppMoveToDesk) {
						((BaseAppIcon) component).setInEdge(false);
						mIsMoveStop = false;
						if (!mIsInMoveToDesk) {
							mIsInMoveToDesk = true;
						}
						return ((AppMoveToDesk) content).onDragMove(cx, cy, this, mDragComponent);
					}
				} else {
					if (content != null && content instanceof AppMoveToDesk) {
						if (mIsInMoveToDesk) {
							((AppMoveToDesk) content).onDragExit(cx, cy, this, mDragComponent,
									false);
							mIsInMoveToDesk = false;
						}
					}
				}
			} else {
				if (content != null && content instanceof AppMoveToDesk) {
					if (mIsInMoveToDesk) {
						((AppMoveToDesk) content).onDragExit(cx, cy, this, mDragComponent, false);
						mIsInMoveToDesk = false;
					}
				}
				if (handler != null && handler.isShowTabRow()) {
					isOverlap = AppFuncHandler.getInstance()
							.checkDragComponentTopBarOverlap(cx, cy);
				}

				AppFuncFolderQuickAddBar folderQuickAddBar = null;
				ProManageEditDock proManageEditDock = null;
				XComponent seletedTabContent = AppFuncHandler.getInstance().getSelectedTabContent();
				if (seletedTabContent instanceof AllAppTabBasicContent) {
					folderQuickAddBar = AppFuncHandler.getInstance().getFolderQuickAddBar();
				} else if (seletedTabContent instanceof ProManageTabBasicContent) {
					proManageEditDock = AppFuncHandler.getInstance().getProManageEditDock();
				}
				if (isOverlap) {
					setIconFolderReady(false);
					if (folderQuickAddBar != null) {
						if (!sIsExit) {
							((BaseAppIcon) component).setInEdge(false);
							mIsMoveStop = false;
							if (!mIsInFolderQuickAddBar) {
								folderQuickAddBar.onDragEnter(cx, cy, this, mDragComponent);
								mIsInFolderQuickAddBar = true;
							}
							if (mDragComponent instanceof ApplicationIcon) {
								((ApplicationIcon) mDragComponent).setIsIntoFolderReady(mIsInFolderQuickAddBar);
							}
							return folderQuickAddBar.onDragMove(cx, cy, this, mDragComponent);
						} else {
							if (mIsInFolderQuickAddBar) {
								folderQuickAddBar.onDragExit(cx, cy, this, mDragComponent, false);
								mIsInFolderQuickAddBar = false;
							}
						}
					} else if (proManageEditDock != null) {
						proManageEditDock.drawEditDockBg(x, y);
					}
				} else {
					if (folderQuickAddBar != null) {
						if (mIsInFolderQuickAddBar) {
							folderQuickAddBar.onDragExit(cx, cy, this, mDragComponent, false);
							mIsInFolderQuickAddBar = false;
						}
					} else if (proManageEditDock != null) {
						proManageEditDock.clearEditDockBg();
					}
					if (mIsSupportScroll) {
						if (isEqualMoveIconRegion(centerX, centerY)) {
							// 补位移动图标
							if (!(mDragComponent instanceof ProManageIcon)) {
								final int delay = 150;
								mScheduler.schedule(Scheduler.TASK_TIME, time + delay, delay * 2,
										delay + delay / 2, mMoveIconTask, new int[] { centerX,
												centerY });
							}
						} else {
							// 滚屏
							// x -= absX;
							// y -= absY;
							// 从触摸位置和图标中心位置，选择更容易产生滚屏的条件
							centerX = x < mWidth / 2 ? Math.min(x, centerX) : Math.max(x, centerX);
							centerY = y < mHeight / 2 ? Math.min(y, centerY) : Math.max(y, centerY);

							if (isEqualMoveScreenRegion(centerX, centerY)
									&& !(mDragComponent instanceof ProManageIcon)) {
								isInEdge = true;
								if (!isMoveStopOld) {
									// 翻动屏幕
									final int delay = mScrollDuration;
									if (mIsVScroll) {
										mMoveScreenTask.mLastScrollTime = time;
										mScheduler
												.schedule(Scheduler.TASK_TIME, time + delay,
														delay * 2, delay + delay / 2,
														mMoveScreenTask, null);
									} else {
										mScheduler
												.schedule(Scheduler.TASK_TIME, time + delay,
														delay * 2, delay + delay / 2,
														mMoveScreenTask, null);
										mMoveScreenTask.mNextTaskExecuteTime = time + delay
												+ MoveScreenTask.MOVE_SCREEN_IDLE_TIME;
									}
								}
							}
						}
						mXMoveStop = centerX;
						mYMoveStop = centerY;
					}
					((BaseAppIcon) component).setInEdge(isInEdge);
					mIsMoveStop = isInEdge;
					return true;
				}
			}

			// //////////////////////////////////////////////////////
		} else if (mIsSupportScroll && isInGrid && isEqualMoveIconRegion(x, y)) {
			// 处理拖动屏幕事件
			if ((!mIsVScroll && mLastTouchX != x && mLastTouchX != 0)
					|| (mIsVScroll && mLastTouchY != y && mLastTouchY != 0)) {
				if (!mIsScrolling) {
					final int factor = 7; // mIsSelectedCell ? 14 : 7;
					final int delta = mIsVScroll ? mLastTouchY - y : mLastTouchX - x;
					mScrollEnabledInSelectdCell += delta;

					if (Math.abs(mScrollEnabledInSelectdCell) > mNotScrollSize * factor) {
						// 拖动一小段距离后才认为开始滚屏
						mIsScrolling = true;
						resetMotion();
						if (mIsVScroll) {
							mScroller.onTouchEvent(event, MotionEvent.ACTION_DOWN);
						} else {
							mScreenScroller.onTouchEvent(event, MotionEvent.ACTION_DOWN);
						}
					}
				}
				if (mIsScrolling) {
					if (mIsVScroll) {
						mScroller.onTouchEvent(event, MotionEvent.ACTION_MOVE);
					} else {
						mScreenScroller.onTouchEvent(event, MotionEvent.ACTION_MOVE);
					}
				}
			}
			mLastTouchX = x;
			mLastTouchY = y;
			return true;
		}
		return false;
	}

	protected boolean touchUp(MotionEvent event, int x, int y) {
		mIsMoveStop = false;
		boolean isHandle = false;
		mScrollEnabledInSelectdCell = 0;
		mScrollDisOnX = 0;
		mIsReadyDragIcon = false;
		// LogUnit.i("Up mInDragStatus : " + mInDragStatus +
		// " mDragComponent : "
		// + mDragComponent != null ? "is not null" : "is null");
		// 处理拖拽模式下，取消被拖拽的组件
		if (mIsDragable && mInDragStatus && mDragComponent != null) {
			// 不显示帮助 add by dingzijian
			// if (!(mDragComponent instanceof ProManageIcon)) {
			// mShowFolderTutorial = true;
			// SharedPreferences sharedPreferences = mActivity
			// .getSharedPreferences(IPreferencesIds.USERTUTORIALCONFIG,
			// Context.MODE_PRIVATE);
			// boolean needStarFolderTutorial = sharedPreferences.getBoolean(
			// LauncherEnv.SHOULD_SHOW_APPFUNC_FOLDER_GUIDE, true);
			// boolean needStarDragTutorial = sharedPreferences.getBoolean(
			// LauncherEnv.SHOULD_SHOW_APPFUNC_DRAG_GUIDE, true);
			// if (mShowDragTutorial == true) {
			// mShowFolderTutorial = false;
			// }
			// if (needStarFolderTutorial && mShowFolderTutorial) {
			// GuideForGlFrame
			// .setmGuideType(GuideForGlFrame.GUIDE_TYPE_FUNC_FOLDER);
			// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
			// IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME,
			// null, null);
			// Editor editor = sharedPreferences.edit();
			// editor.putBoolean(
			// LauncherEnv.SHOULD_SHOW_APPFUNC_FOLDER_GUIDE, false);
			// editor.commit();
			// mShowFolderTutorial = false;
			// } else if (mShowDragTutorial && needStarDragTutorial) {
			// GuideForGlFrame
			// .setmGuideType(GuideForGlFrame.GUIDE_TYPE_FUNC_DRAG);
			// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
			// IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME,
			// null, null);
			// Editor editor = sharedPreferences.edit();
			// editor.putBoolean(
			// LauncherEnv.SHOULD_SHOW_APPFUNC_DRAG_GUIDE, false);
			// editor.commit();
			// mShowDragTutorial = false;
			// }
			// }
			int centerX = mDragComponent.centerX();
			int centerY = mDragComponent.centerY();
			final int absX = getAbsX();
			final int absY = getAbsY();
			int cx = centerX + absX;
			int cy = centerY + absY;
			boolean addToWorkspace = false;
			boolean quickAddFolder = false;
			boolean toInfo = false;
			boolean isOverlap = false;
			FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
			if (handler != null && handler.isShowActionBar()) {
				isOverlap = AppFuncHandler.getInstance().checkDragComponentHomeIconOverlap(cx, cy);
			}
			if (isOverlap) {
				AppFuncHomeComponent homeComponent = AppFuncHandler.getInstance()
						.getCurrentHomeComponent();
				if (homeComponent != null) {
					XComponent content = homeComponent.getInScreenContent();
					if (content != null && content instanceof AppMoveToDesk) {
						addToWorkspace = ((AppMoveToDesk) content).onDrop(cx, cy, this,
								mDragComponent);
						((AppMoveToDesk) content).onDragExit(cx, cy, this, mDragComponent,
								addToWorkspace);
					}
				}
			} else {
				if (handler != null && handler.isShowTabRow()) {
					isOverlap = AppFuncHandler.getInstance()
							.checkDragComponentTopBarOverlap(cx, cy);
				}
				if (isOverlap) {
					XComponent seletedTabContent = AppFuncHandler.getInstance()
							.getSelectedTabContent();
					if (seletedTabContent instanceof AllAppTabBasicContent) {
						AppFuncFolderQuickAddBar bar = AppFuncHandler.getInstance()
								.getFolderQuickAddBar();
						if (bar != null) {
							quickAddFolder = bar.onDrop(cx, cy, this, mDragComponent);
							bar.onDragExit(cx, cy, this, mDragComponent, quickAddFolder);
						}
					} else if (seletedTabContent instanceof ProManageTabBasicContent) {
						// 正在运行tab的dock区域
						if (mDragComponent instanceof ProManageIcon) {
							vibrate();
							if (mUtils.isVertical()) {
								// 竖屏
								if (cx < mWidth / 2) {
									toInfo = true;
								}
							} else {
								// 横屏
								if (cy > mHeight / 2) {
									toInfo = true;
								}

							}
							ProManageIcon pro = (ProManageIcon) mDragComponent;
							AppFuncHandler
									.getInstance()
									.getProManageEditDock()
									.dealOverlapEvent(toInfo,
											pro.getInfo().getAppItemInfo().mIntent);
							if (handler != null && handler.isShowTabRow()) {
								AppFuncHandler.getInstance().setTopChange(false, true);
							}
						}
					}
				}
			}
			if (addToWorkspace) {
				onAddToWorkspace();
			} else if (toInfo) {
				mIsGotoInfo = true;
			} else if (!quickAddFolder) {
				onLongClickUp(true);
			}
			isHandle = true;
		} else if (mIsScrolling) {
			// 滚动结束
			// 1.处理反弹效果
			// 2.如果发现没有反弹效果，把事件继续传入detector.onTouchEvent(event)，以便判断是否触发onFing方法
			setIconFolderReady(false);
			// resetIconPos(event);
			onSingleTapUp(event, x, y);
			isHandle = true;

			mIsScrolling = false;
			resetMotion();
			// mOffsetMotion = new XALinear(1, XALinear.XALINEAR_EBACCEL,
			// mOffsetY, 0, 0, 0, SCROLL_STEP, (-mOffsetY * 7)
			// / (SCROLL_STEP << 2), 0);
			// attachAnimator(mOffsetMotion);
			mIsMotionRunning = true;
			if (mIsVScroll) {
				if (!doCycleScroll(y)) {
					mScroller.onTouchEvent(event, MotionEvent.ACTION_UP);
				}
			} else {
				mScreenScroller.onTouchEvent(event, MotionEvent.ACTION_UP);
			}
			isHandle = true;
			postInvalidate();

			// LogUnit.i("touchUp Scroll");
		} else {
			setIconFolderReady(false);
			isHandle = onSingleTapUp(event, x, y);

			// LogUnit.i("touchUp Normal");
		}
		mScrollOnX = false;
		mIsInFolderQuickAddBar = false;
		mIsInMoveToDesk = false;
		return isHandle;
	}

	/**
	 * 离开功能表时，震动一下手机
	 */
	private void vibrate() {
		Vibrator vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
		long pattern[] = { 0, 50 };
		vibrator.vibrate(pattern, -1);
	}

	/**
	 * 是否开始播放动画，进入编辑状态开始，离开退出
	 * 
	 * @param isStart
	 *            是否开始或者停止编辑动作
	 */
	public synchronized void setDragStatus(boolean isStart) {
		// LogUnit.i("setDragStatus : " + isStart + " Folder : " +
		// mIsFolderEnable);

		if (!mIsDragable) {
			return;
		}
		if (!isStart) {
			// LogUnit.i("DragStatus : " + isStart + " Folder : "
			// + mIsFolderEnable);
			setIconFolderReady(false);
			onLongClickUp(false);
		}
		//		PreferencesManager manager = new PreferencesManager(mActivity,
		//				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		//		int count = manager.getInt(IPreferencesIds.ENTER_FUNC_EDIT, 0);
		//		if (isStart && count < 5) {
		//			count++;
		//			manager.putInt(IPreferencesIds.ENTER_FUNC_EDIT, count);
		// DeskToast.makeText(mActivity, R.string.enter_appfunc_edit_status,
		// Toast.LENGTH_SHORT).show();
		//		}
		for (XComponent component : mStoreComponent) {
			// 通知组件进入编辑状态
			if (component instanceof BaseAppIcon) {
				((BaseAppIcon) component).setEditMode(isStart);
			}
		}
		boolean temp = mInDragStatus;
		mInDragStatus = isStart;
		// AppFuncHandler.getInstance().setHomeIconChangeDesk(mInDragStatus);
		if (mIsFolderEnable == false && isStart != temp) {
			if (mInDragStatus) {
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFOLDER,
						AppFuncConstants.ENTEREDITMODE, null);
			} else {
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFOLDER,
						AppFuncConstants.EXITEDITMODEL, null);
			}
		}
	}

	public boolean isInDragStatus() {
		return mInDragStatus;
	}

	/**
	 * 检查两点是否相同，有一定容错性
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean checkRegionSame(int x, int y, float size) {
		if (mDragComponent != null) {
			int mX = mDragComponent.centerX();
			int mY = mDragComponent.centerY();
			// LogUnit.i("x : " + x + " y : " + y + " mX : " + mX + " mY : " +
			// mY);
			if (x + size >= mX && x - size <= mX && y + size >= mY && y - size <= mY) {
				return true;
			}
		}
		return false;
	}

	private boolean checkRegionSame(int x, int y, int size) {
		int absX = Math.abs(mLastTouchX - x);
		int absY = Math.abs(mLastTouchY - y);
		if (absX < size && absY < size) {
			return true;
		}
		return false;
	}

	/**
	 * 是否在XBaseView范围内，此范围并不包括Padding
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isEqualMoveIconRegion(int x, int y) {
		if (mIsSupportScroll) {
			int size = mIndicatorSize * SCROLL_SIZE_MULTIPLE;
			if (x > mPaddingLeft + size && x < mPaddingLeft + mRealWidth - size
					&& y > mPaddingTop + size && y < mPaddingTop + mRealHeight - size) {
				return true;
			} else {
				return false;
			}
		} else {
			if (x > mPaddingLeft && x < mPaddingLeft + mRealWidth && y > mPaddingTop
					&& y < mPaddingTop + mRealHeight) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 翻动屏幕的区域
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isEqualMoveScreenRegion(int x, int y) {
		boolean isEqual = false;
		int size = mIndicatorSize * SCROLL_SIZE_MULTIPLE;
		if (mIsSupportScroll) {
			if (mIsVScroll) {
				// 到达屏幕底部或者顶部的一定合理范围触发准备滑屏的事件
				isEqual |= y <= mPaddingTop + size && x >= 0 && x <= mWidth;
				isEqual |= y >= mPaddingTop + mRealHeight - size && x >= 0 && x <= mWidth;
			} else {
				// 到达屏幕左边或者右边的一定合理范围触发准备滑屏的事件
				isEqual = x <= mPaddingLeft + size && y >= 0 && y <= mHeight;
				isEqual |= x >= mPaddingLeft + mRealWidth - size && y >= 0 && y <= mHeight;
			}
		}
		return isEqual;
	}

	/**
	 * 是否超出列表界限
	 * 
	 * @param location
	 * @param isThrowException
	 *            出错时是否抛异常，(这个异常会马上被捕获，只作错误提示)
	 * @return true 表示出界
	 */
	private boolean isOutOfIndex(int location, boolean isThrowException) {
		if (location < 0 || location >= mAdapterCount) {
			if (isThrowException) {
				try {
					throw new IndexOutOfBoundsException("location " + location + " list size "
							+ mAdapterCount);
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 处理图标移动，造成补位算法
	 */
	private synchronized void moveIcon() {
		if (mDragComponent == null) {
			return;
		}

		int x = mDragComponent.centerX() - mOffsetX;
		int y = mDragComponent.centerY() - mOffsetY;
		// ////////////////////////////
		// isOutOfIndex(mFVisibleIndex, true);
		// isOutOfIndex(mLVisibleIndex, true);
		// ///////////////////////////

		int startPos = mFVisibleIndex;
		if (startPos < 0) {
			startPos = 0;
		}
		for (int i = startPos; i <= mLVisibleIndex; i++) {
			XComponent component = getChildAt(i);
			if (component == null) {
				continue;
			}
			if (component.contains(x, y)) {
				if (i == mDragComponentTargetIndex) {
					continue;
				}
				// 文件夹
				boolean isHandle = false;
				int folderRegionW = (int) (component.getWidth() * 0.15f);
				if (x <= component.centerX() + folderRegionW
						&& x >= component.centerX() - folderRegionW) {
					if (mIsFolderEnable && mIconMoveing == 0) {
						ApplicationIcon scr = (ApplicationIcon) component;
						ApplicationIcon target = (ApplicationIcon) mDragComponent;
						if ((scr.isFolder() && !target.isFolder())
								|| (!scr.isFolder() && !target.isFolder())) {
							// 创建文件夹计时
							if (mFolderImageIcon != null) {
								if (!scr.equals(mFolderImageIcon)) {
									setIconFolderReady(false);
									mFolderImageIcon = scr;
									setIconFolderReady(true);
								}
							} else {
								mFolderImageIcon = scr;
								setIconFolderReady(true);
							}
							isHandle = true;
						}
					}
				}

				// 触发补位 ,如果同一行
				if (isHandle) {
					return;
				}
				setIconFolderReady(false);
				if ((i / mColunmNums) == (mDragComponentTargetIndex / mColunmNums)) {
					int count = calculateSameRowPosition(mDragComponentTargetIndex, i);
					if (count != 0) {
						mShowDragTutorial = true;
						mShowFolderTutorial = false;
						moveComponent(mDragComponentTargetIndex, mDragComponentTargetIndex + count);
					}
				}
				// 如果移动的View都在最边的View上
				else if (i % mColunmNums == 0 || i % mColunmNums == mColunmNums - 1) {
					mShowDragTutorial = true;
					mShowFolderTutorial = false;
					moveComponent(mDragComponentTargetIndex, i);
				} else {
					int count = calculatePosition(mDragComponentTargetIndex, i);
					if (count != 0) {
						mShowFolderTutorial = false;
						mShowDragTutorial = true;
						moveComponent(mDragComponentTargetIndex, mDragComponentTargetIndex + count);
					}
				}
				mShowFolderTutorial = true;
				return;
			}
		}
		setIconFolderReady(false);
		int pos = mAdapterCount - 1;
		if (pos >= mStoreComponent.size()) {
			pos = mStoreComponent.size() - 1;
		}
		XComponent xComponent = getChildAt(pos);
		if (mIsSupportScroll && !mIsVScroll) {
			if (xComponent.mX / mWidth != x / mWidth) {
				mShowFolderTutorial = true;
				return;
			}
		}
		if ((x > xComponent.mX + xComponent.getWidth() && y > xComponent.mY)
				|| y > xComponent.mY + xComponent.getHeight()) {
			if (mDragComponentTargetIndex != pos) {
				mShowDragTutorial = true;
				mShowFolderTutorial = false;
				moveComponent(mDragComponentTargetIndex, pos);
			}
		}
	}

	/**
	 * 计算实际移动位置
	 * 
	 * @param src
	 * @param target
	 * @return
	 */
	private int calculatePosition(int src, int target) {
		LogUnit.d("new 1590");
		Rect srcRect = new Rect(mDragComponent.mX - mOffsetX, mDragComponent.mY - mOffsetY,
				mDragComponent.mX + mDragComponent.getWidth() - mOffsetX, mDragComponent.mY
						+ mDragComponent.getHeight() - mOffsetY);
		Rect targetRect = new Rect();
		getChildAt(target).getRect(targetRect);
		// 移动View相对位置原View上方
		if ((src / mColunmNums) > (target / mColunmNums)) {
			// 移动View相对位置原View左上方
			if ((src % mColunmNums) > (target % mColunmNums)) {
				if (srcRect.centerX() < targetRect.centerX()) {
					return target - src;
				} else {
					return target - src + 1;
				}
			}
			// 移动View相对位置原View右上方
			else {
				if (srcRect.centerX() > targetRect.centerX()) {
					return target - src + 1;
				} else {
					return target - src;
				}
			}
		}
		// 移动View相对位置原View下方
		else if ((src / mColunmNums) < (target / mColunmNums)) {
			// 移动View相对位置原View左下方
			if ((src % mColunmNums) > (target % mColunmNums)) {
				if (srcRect.centerX() > targetRect.centerX()) {
					return target - src;
				} else {
					return target - src - 1;
				}
			}
			// 移动View相对位置原View右下方
			else {
				if (srcRect.centerX() < targetRect.centerX()) {
					return target - src - 1;
				} else {
					return target - src;
				}
			}
		}
		return 0;
	}

	/**
	 * 原来位置和新的目标位置处于同一行时，计算实际移动位置
	 * 
	 * @param src
	 * @param target
	 * @return
	 */
	private int calculateSameRowPosition(int src, int target) {
		LogUnit.d("new 1646");
		Rect srcRect = new Rect(mDragComponent.mX - mOffsetX, mDragComponent.mY - mOffsetY,
				mDragComponent.mX + mDragComponent.getWidth() - mOffsetX, mDragComponent.mY
						+ mDragComponent.getHeight() - mOffsetY);
		Rect targetRect = new Rect();
		getChildAt(target).getRect(targetRect);
		// scrPosition原位置右边一格
		if (src - target == -1) {
			if (srcRect.centerX() > targetRect.centerX()) {
				return 1;
			}
		}
		// scrPosition原位置左边一格
		else if (src - target == 1) {
			if (srcRect.centerX() < targetRect.centerX()) {
				return -1;
			}
		}
		// scrPosition原位置右边
		else if (src - target <= -1) {
			if (srcRect.centerX() > targetRect.centerX()) {
				return target - src;
			} else {
				return target - src - 1;
			}
		}
		// scrPosition原位置左边
		else if (src - target >= 1) {
			if (srcRect.centerX() < targetRect.centerX()) {
				return target - src;
			} else {
				return target - src + 1;
			}
		}
		return 0;
	}

	/**
	 * 从原来位置移动到新的目标位置
	 * 
	 * @param src
	 * @param target
	 */
	private synchronized void moveComponent(int src, int target) {
		if (src > target) {
			// 向后移动
			for (int i = src - 1; i >= target; i--) {
				if (i >= 0 && i < mStoreComponent.size()) {
					if (i + 1 == src) {
						iconMovingPlus();

						final BaseAppIcon icon = (BaseAppIcon) mStoreComponent.get(i);
						LogUnit.d("new 1699");
						icon.moveMotion(mDragComponentX, mDragComponentY,
								new AbstractAnimateListener() {
									@Override
									public void onFinish(XAnimator animator) {
										iconMovingReduce();
										// LogUnit.i("Moveing: " +
										// mIconMoveing);
										icon.setMotionFilter(null);
									}
								}, this);
					} else {
						XComponent component = getChildAt(i + 1);
						iconMovingPlus();
						final BaseAppIcon icon = (BaseAppIcon) mStoreComponent.get(i);
						LogUnit.d("new 1714");
						icon.moveMotion(component.mX, component.mY, new AbstractAnimateListener() {
							@Override
							public void onFinish(XAnimator animator) {
								iconMovingReduce();
								// LogUnit.i("Moveing: " +
								// mIconMoveing);
								icon.setMotionFilter(null);
							}
						}, this);
					}
				}
			}
		} else {
			// 向前移动
			for (int i = src + 1; i <= target; i++) {
				if (i >= 0 && i < mStoreComponent.size()) {
					if (i - 1 == src) {
						iconMovingPlus();
						final BaseAppIcon icon = (BaseAppIcon) mStoreComponent.get(i);
						LogUnit.d("new 1734");
						icon.moveMotion(mDragComponentX, mDragComponentY,
								new AbstractAnimateListener() {
									@Override
									public void onFinish(XAnimator animator) {
										iconMovingReduce();
										// LogUnit.i("Moveing: " +
										// mIconMoveing);
										icon.setMotionFilter(null);
									}
								}, this);
					} else {
						XComponent component = getChildAt(i - 1);
						iconMovingPlus();
						XComponent c = mStoreComponent.get(i);
						if (c instanceof BaseAppIcon) {
							final BaseAppIcon icon = (BaseAppIcon) c;
							LogUnit.d("new 1749");
							icon.moveMotion(component.mX, component.mY,
									new AbstractAnimateListener() {
										@Override
										public void onFinish(XAnimator animator) {
											iconMovingReduce();
											// LogUnit.i("Moveing: " +
											// mIconMoveing);
											icon.setMotionFilter(null);
										}
									}, this);
						}
					}
				}
			}
		}
		// 修改被选中图标坐标到目标坐标
		// 修改列表组件顺序
		// 修改被选中图标位置到目标位置
		// 修改焦点位置到目标位置
		LogUnit.e("Moveing: " + mIconMoveing);
		final XComponent dragComponent = getChildAt(target);
		if (dragComponent != null) {
			mDragComponentX = dragComponent.mX;
			mDragComponentY = dragComponent.mY;
		}
		XComponent component = getChildAt(src);
		if (component != null) {
			removeAndInstertComponent(target, component);
			mDragComponentTargetIndex = target;
		}
	}

	private void onAddToWorkspace() {
		if (mDragComponent != null) {
			mDragComponent.setXY(mDragComponent.mX - mOffsetX, mDragComponent.mY - mOffsetY);
			if (mDragComponentIndex != mDragComponentTargetIndex) {
				moveComponent(mDragComponentTargetIndex, mDragComponentIndex);
				if (mIconMoveing == 0) {
					moveIcon();
				}
				if (mDragComponentIndex == mFocusedPosition) {
					setItemFocus(false);
					mFocusedPosition = -1;

				}
			}
			final BaseAppIcon icon = (BaseAppIcon) mDragComponent;
			((BaseAppIcon) mDragComponent).changeAlpha(0, 255, 30, new IAnimateListener() {
				
				@Override
				public void onStart(XAnimator arg0) {
					
				}
				
				@Override
				public void onProgress(XAnimator arg0, int arg1) {
					
				}
				
				@Override
				public void onFinish(XAnimator arg0) {
					icon.setAlpha(255);
				}
			}, this, false);
			((BaseAppIcon) mDragComponent).setInEdge(false);
			setDragComponent(null);
			mDragComponentIndex = -1;
			mDragComponentTargetIndex = -1;
			requestLayout();
		}
	}

	/**
	 * 取消被拖拽的组件,退出正在拖拽的状态
	 */
	public void onLongClickUp(boolean isChange) {
		if (mIsDragable && mInDragStatus && mDragComponent != null) {
			// 如果拖动的是正在运行界面的图标，不引起其他图标的移位 add by yangbing
			if (mDragComponent instanceof ProManageIcon) {
				mDragComponent.setXY(mDragComponent.mX - mOffsetX, mDragComponent.mY - mOffsetY);
				((BaseAppIcon) mDragComponent).onLongClickUp(mDragComponentX, mDragComponentY,
						null, this);
				setDragComponent(null);
				mDragComponentIndex = -1;
				mDragComponentTargetIndex = -1;
				if (mAdapterCount == 0) {
					XViewFrame.getInstance().getAppFuncMainView().setDragComponent(null);
				}
				// requestLayout();
				FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
				if (handler != null) {
					if (handler.isShowTabRow()) {
						AppFuncHandler.getInstance().setTopChange(false, true);
					} else {
						AppFuncHandler.getInstance().setTopChange(false, false);
					}
				}
				resetOrientation();
			} else {
//				LogUnit.i("isChange : " + isChange);
				if (isChange) {
					boolean isLast = checkIsLastPostion();
					if (isLast) {
						if (mIconMoveing == 0) {
							moveComponent(mDragComponentTargetIndex, mAdapterCount - 1);
						}
					} else {
						if (mIconMoveing == 0) {
							moveIcon();
						}
					}
					mDragComponent
							.setXY(mDragComponent.mX - mOffsetX, mDragComponent.mY - mOffsetY);
					((BaseAppIcon) mDragComponent).onLongClickUp(mDragComponentX, mDragComponentY,
							null, this);
					if (mDragComponentIndex != mDragComponentTargetIndex) {
						// TODO 返回一个移动成功或者失败的结果
						boolean isSuccessful = true;
						// LogUnit.e("mDragComponentIndex : " +
						// mDragComponentIndex
						// + " mDragComponentIndex : "
						// + mDragComponentTargetIndex);
						isSuccessful = mAdapter.switchPosition(mDragComponentIndex,
								mDragComponentTargetIndex);
						if (mDragComponentIndex == mFocusedPosition) {
							setItemFocus(false);
							mFocusedPosition = -1;

						}
						if (!isSuccessful) {
							AppFuncExceptionHandler.handle(null);
						}
					}
					// mShowFolderTutorial = true;
					isInFolderRegionAndFolderHanlde(); // 通过Up事件创建文件夹
				} else {
					mDragComponent
							.setXY(mDragComponent.mX - mOffsetX, mDragComponent.mY - mOffsetY);
					((BaseAppIcon) mDragComponent).onLongClickUp(mDragComponentX, mDragComponentY,
							null, this);
					// mDragComponent = null;
					setDragComponent(null);
					mDragComponentIndex = -1;
					mDragComponentTargetIndex = -1;
				}

				if (mDragComponent != null) {
					((BaseAppIcon) mDragComponent).setInEdge(false);
				}

				if (isChange) {
					setIconFolderReady(false);
					if (!mIsEnteringFolder) {
						// mDragComponent = null;
						setDragComponent(null);
						mDragComponentIndex = -1;
						mDragComponentTargetIndex = -1;
						// requestLayout();
						mAdapter.loadApp();
						mAdapterCount = mAdapter.getCount();
						resetOrientation();
					}
				}
				// AppFuncHandler.getInstance().setHomeIconChangeDesk(true);
				// showAddButton(true);
			}
		}
	}

	/**
	 * 检查是否最后空白区域
	 * 
	 * @return
	 */
	private synchronized boolean checkIsLastPostion() {
		boolean isLast = false;
		if (mIconMoveing != 0 || mDragComponentTargetIndex == mAdapterCount - 1) {
			isLast = false;
		} else {
			if (!mIsVScroll && mCurScreen != mScreen - 1) {
				isLast = false;
			} else {
				XComponent component = getChildAt(mAdapterCount - 1);
				if ((mDragComponent.mX - mOffsetX > component.mX + component.getWidth() && mDragComponent.mY
						- mOffsetY >= component.mY)
						|| (mDragComponent.mY - mOffsetY >= component.mY + DrawUtils.dip2px(10))) {
					isLast = true;
				}
			}
		}
		return isLast;
	}

	/**
	 * 是否在文件夹区域，并完成相应文件夹操作
	 */
	private boolean isInFolderRegionAndFolderHanlde() {
		// 因为之前已经做了一次被拖拽的组件坐标归位的操作，所以不需要减去offset
		if (!mIsFolderEnable) {
			return false;
		}
		int x = mDragComponent.centerX();
		int y = mDragComponent.centerY();
		// ////////////////////////////
		// isOutOfIndex(mFVisibleIndex, true);
		// isOutOfIndex(mLVisibleIndex, true);
		boolean isReady = isIconFolderReady();
		// LogUnit.i("IconFolderReady : " + isReady);
		if (!isReady) {
			setIconFolderReady(false);
			return false;
		}
		// ///////////////////////////

		int startPos = mFVisibleIndex;
		if (startPos < 0) {
			startPos = 0;
		}

		for (int i = startPos; i <= mLVisibleIndex; i++) {
			XComponent component = getChildAt(i);
			if (component.contains(x, y)) {
				if (i == mDragComponentTargetIndex) {
					continue;
				}
				int folderRegionW = (int) (component.getWidth() * 0.25f);
				if (x <= component.centerX() + folderRegionW
						&& x >= component.centerX() - folderRegionW) {
					if (mIsFolderEnable && mIconMoveing == 0) {
						ApplicationIcon scr = (ApplicationIcon) component;
						ApplicationIcon target = (ApplicationIcon) mDragComponent;
						if ((scr.isFolder() && !target.isFolder())
								|| (!scr.isFolder() && !target.isFolder())) {
							setIconFolderReady(false);
							// mIconMoveing = -1;
							mFolderIndex = i;
							mFolderIndexIcon = scr;
							mDragComponentTargetIcon = target;
							boolean isCreateFolder = scr.isFolder() ? false : true;
							vibrate();
							if (isCreateFolder) {
								mShowFolderTutorial = false;
								mShowDragTutorial = false;
								FunControler controler = null;
								// 1.获得文件夹位置索引
								int folderIndex = getFolderIndex();
								// 2.创建一个文件夹
								controler = AppFuncFrame.getFunControler();
								boolean flag = false;
								try {
									controler.getFunDataModel().beginTransaction();

									String folderName = mActivity.getResources().getString(
											R.string.folder_name);
//									AppDataEngine dataEngine = AppDataEngine.getInstance(mActivity);
//									dataEngine.getAppItem(mDragComponentTargetIcon.getIntent());
//									FunItemInfo dragItemInfo = mDragComponentTargetIcon.getInfo();
//									FunItemInfo folderItemInfo = mFolderIndexIcon.getInfo();
//									if(dragItemInfo instanceof FunAppItemInfo && folderItemInfo instanceof FunAppItemInfo){
//										FunAppItemInfo appItemInfo = (FunAppItemInfo) dragItemInfo;
//										FunAppItemInfo folderAppItemInfo = (FunAppItemInfo) folderItemInfo;
//									ArrayList<AppItemInfo> infos = new ArrayList<AppItemInfo>(2);
//										infos.add(dataEngine.getAppItem(mDragComponentTargetIcon.getIntent()));
//										infos.add(dataEngine.getAppItem(mFolderIndexIcon.getIntent()));
//										String name = CommonControler.getInstance(mActivity).generateFolderName(infos);
//									if (name != null && !name.equals("")) {
//										folderName = name;
//									}
//									}
									FunFolderItemInfo folderInfo = controler.addFunFolderItemInfo(
											folderIndex, folderName, null);

									// 3.往创建好文件夹加入图标
									if (folderInfo != null) {
										folderInfo.setMfolderfirstcreate(true);
										controler.moveFunAppItemToFolder(folderInfo,
												folderInfo.getSize(),
												(FunAppItemInfo) mFolderIndexIcon.getInfo());
										flag = true;
									}
									controler.getFunDataModel().setTransactionSuccessful();
								} catch (DatabaseException e) {
									AppFuncExceptionHandler.handle(e);
									return false;
								} finally {
									controler.getFunDataModel().endTransaction();
								}

								if (flag) {
									XComponent xComponent = getChildAt(mFolderIndex);
									mAdapter.loadApp();
									XComponent com = mAdapter.getComponent(mFolderIndex,
											xComponent.mX, xComponent.mY, xComponent.getWidth(),
											xComponent.getHeight(), xComponent, this);
									replaceComponent(mFolderIndex, com);

									PointF point = ((ApplicationIcon) com).getNextFolderItemPoint();
									mDragComponentTargetIcon.startShrink(com.mX + point.x, com.mY
											+ point.y, mOffsetX, mOffsetY, (ApplicationIcon) com);
								}
							} else {
								FunFolderItemInfo info = (FunFolderItemInfo) mFolderIndexIcon
										.getInfo();
								ArrayList<FunAppItemInfo> list = info.getFunAppItemInfos();
								mShowFolderTutorial = false;
								mShowDragTutorial = false;
								if (list != null) {
									if (list.isEmpty()) {
										mIsOpenEditFolder = true;
									}
								} else {
									mIsOpenEditFolder = true;
								}

								PointF point = mFolderIndexIcon.getNextFolderItemPoint();
								mDragComponentTargetIcon.startShrink(mFolderIndexIcon.mX + point.x,
										mFolderIndexIcon.mY + point.y, mOffsetX, mOffsetY,
										mFolderIndexIcon.mIsInMid, mFolderIndexIcon);
							}

							mIsEnteringFolder = true;
							// LogUnit.i("FolderOpened");
							return true;

						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param event
	 *            触摸事件
	 * @param x
	 *            经过偏移量处理后的X
	 * @param y
	 *            经过偏移量处理后的Y
	 * @return
	 */
	protected boolean onDown(MotionEvent event, int x, int y) {
		// LogUnit.i("onDown");
		if (mAdapterCount == 0) {
			return true;
		}

		x += -mOffsetX;
		y += -mOffsetY;
		boolean isHanlde = false;

		int startPos = mFVisibleIndex;
		if (startPos < 0) {
			startPos = 0;
		}

		for (int i = startPos; i <= mLVisibleIndex; i++) {
			XComponent component = getChildAt(i);
			if (component.contains(x, y)) {
				mTouchDownPostion = i;
				isHanlde = true;
				// mFocusedPosition = mTouchDownPostion;
				if (mTouchDownPostion != -1) {
					// 如果在编辑模式则通过定时器去决定是拖拽，还是打开文件夹
					if (mIsDragable && mInDragStatus) {
						// LogUnit.i("onDown : DragStatus");
						if (mIsMotionRunning || mIsStopResponse) {
							return false;
						}
						if (AppFuncFrame.getFunControler().isHandling()) {
							if (mToast != null) {
								mToast.cancel();
							}
							mToast = DeskToast.makeText(mActivity, R.string.app_fun_strat_loading,
									Toast.LENGTH_SHORT);
							mToast.show();
							return false;
						}
						if (component instanceof ApplicationIcon) {
							ApplicationIcon scr = (ApplicationIcon) component;
							mIsReadyDragIcon = true;
							// LogUnit.i("------schedulerReadyDragFolder");

							if (scr.isFolder()) {
								LogUnit.d("new 2017");
								mScheduler.schedule(
										Scheduler.TASK_FRAME,
										mScheduler.getClock().getFrame() + 10,
										10,
										10,
										new DragFolderTask(),
										new Object[] { mLastTouchX, mLastTouchY, i,
												MotionEvent.obtain(event), scr.getTitle() });
							} else {
								LogUnit.d("new 2028");
								mScheduler.schedule(
										Scheduler.TASK_FRAME,
										mScheduler.getClock().getFrame() + 8,
										8,
										8,
										new DragFolderTask(),
										new Object[] { mLastTouchX, mLastTouchY, i,
												MotionEvent.obtain(event), scr.getTitle() });
							}
						}
						if (component instanceof ProManageIcon) {
							ProManageIcon scr = (ProManageIcon) component;
							mIsReadyDragIcon = true;
							LogUnit.d("new 2028");
							mScheduler.schedule(
									Scheduler.TASK_FRAME,
									mScheduler.getClock().getFrame() + 8,
									8,
									8,
									new DragFolderTask(),
									new Object[] { mLastTouchX, mLastTouchY, i,
											MotionEvent.obtain(event), scr.getTitle() });

							// scr.noticeHomeComponet();

						}

						mIsSelectedCell = dispatchTouchEvent(event);

					} else {
						mIsSelectedCell = dispatchTouchEvent(event);
						return mIsSelectedCell;
					}
				}
			}
		}
		if (!isHanlde) {
			mTouchDownPostion = -1;
		}
		return false;
	}

	/**
	 * 
	 * @param e
	 *            触摸事件
	 * @param x
	 *            经过偏移量处理后的X
	 * @param y
	 *            经过偏移量处理后的YF
	 * @return
	 */
	protected boolean onSingleTapUp(MotionEvent e, int x, int y) {
		// LogUnit.i("onSingleTapUp");
		if (mAdapterCount == 0) {
			return true;
		}
		boolean isHandle = false;
		y += -mOffsetY;
		x += -mOffsetX;
		if (mTouchDownPostion != -1) {
			dispatchTouchEvent(e);
		}
		if (x == -1 && y == -1) {
			return isHandle;
		}

		int startPos = mFVisibleIndex;
		if (startPos < 0) {
			startPos = 0;
		}
		for (int i = startPos; i <= mLVisibleIndex; i++) {
			XComponent component = getChildAt(i);
			if (component.contains(x, y)) {
				mTouchUpPostion = i;
				break;
			}
		}
		if (mTouchDownPostion == mTouchUpPostion && mTouchUpPostion != -1) {
			// 触发onClick事件
			if (!isOutOfIndex(mTouchUpPostion, true) && !mLongPressed
					&& e.getAction() == MotionEvent.ACTION_UP) {
				isHandle = onClick(e);
			}
		}
		return isHandle;
	}

	protected boolean onClick(MotionEvent e) {
		// 触发onClick事件
		boolean isHandle = false;
		// LogUnit.i("onClick " + mTouchUpPostion);
		if (!isOutOfIndex(mTouchUpPostion, true)) {
			// LogUnit.i("Check Event Start " + mTouchUpPostion);
			XComponent xComponent = getChildAt(mTouchUpPostion);
			// int dis = 0;
			boolean isLaunchApp = true;
			// if (mOffsetMotion != null) {
			if (!isScrollFinished()) {
				// float region = 6 * mMetrics.density;
				// // TODO: 从滚动器中获取
				// dis = Math.abs(mOffsetMotion.GetEndX()
				// - mOffsetMotion.GetStartX());
				// if (dis < region) {
				// isLaunchApp = true;
				// } else {
				isLaunchApp = false;
				// }
			}
			if ((xComponent.getEventListener() != null && isLaunchApp && !mIsScrolling && !mScrollOnX)
					|| (xComponent.getEventListener() != null && isLaunchApp && mIsScrolling && isEqualLaunchAppRegion(e))) {
				// LogUnit.i("Click Event Start " + mTouchUpPostion);
				mIsStopResponse = true;
				isHandle = xComponent.getEventListener().onEventFired(this, EventType.CLICKEVENT,
						e, -mOffsetX, -mOffsetY);

				//
			}
		}
		return isHandle;
	}

	/**
	 * 是否处于合理的启动应用程序区域
	 * 
	 * @param e
	 * @return
	 */
	protected boolean isEqualLaunchAppRegion(MotionEvent e) {
		int eX = (int) e.getX(0);
		int eY = (int) e.getY(0);
		// 相对于屏幕偏移
		int x = eX - getAbsX();
		int y = eY - getAbsY();
		float region = 6 * mMetrics.density;
		if (Math.abs(mDownX - x) < region && Math.abs(mDownY - y) < region) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 重定向事件
	 * 
	 * @param e
	 * @return
	 */
	protected boolean dispatchTouchEvent(MotionEvent e) {
		boolean isHandle = false;
		if (!isOutOfIndex(mTouchDownPostion, false)) {
			XComponent component = getChildAt(mTouchDownPostion);
			if (component != null && component.getEventListener() != null) {
				isHandle = component.getEventListener().onEventFired(this, EventType.MOTIONEVENT,
						e, -mOffsetX, -mOffsetY);
			}
		}
		return isHandle;
	}

	/**
	 * 处理因焦点改变而带来的滑动
	 */
	private void handleFocusChange(boolean isAnimate) {
		if (mIsSupportScroll && mIsVScroll) {
			// 焦点位置少于第一个可见位置，或者正处于可见位置的第一行
			if (mFocusedPosition < mFVisibleIndex + mColunmNums) {
				if (mFocusedPosition >= 0) {

					mScroller.setScroll(mFocusedPosition / mColunmNums * mCellHeight);
					resetIconPos(null);
				}

				// if (mFocusedPosition >= (mFVisibleIndex - mColunmNums)) {
				// mScroller.setScroll(mFocusedPosition / mColunmNums *
				// mCellHeight);
				// } else {
				// setItemFocus(false);
				// mFocusedPosition = mFVisibleIndex;
				// setItemFocus(true);
				// }
			}
			// 焦点位置大于最后一个可见位置，或者正处于可见位置的最后一行
			else if (mFocusedPosition > mLVisibleIndex - mColunmNums
					&& (mLVisibleIndex - mFVisibleIndex + 1) >= mSingleScreenCells) {
				if (mFocusedPosition < mStoreComponent.size()) {
					int lastHeight = mFocusedPosition / mColunmNums * mCellHeight;

					mScroller.setScroll(lastHeight - (mRowNums - 1) * mCellHeight);
					resetIconPos(null);
				}

				// if (mFocusedPosition <= (mLVisibleIndex + mColunmNums)) {
				// int lastHeight = mFocusedPosition / mColunmNums
				// * mCellHeight;
				// mScroller.setScroll(lastHeight - (mRowNums - 1) *
				// mCellHeight);
				// resetIconPos(null);
				// } else {
				// setItemFocus(false);
				// mFocusedPosition = mLVisibleIndex;
				// setItemFocus(true);
				// }
			}
		} else {
			// // 焦点位置少于第一个可见位置
			// if (mFocusedPosition < mFVisibleIndex)
			// {
			// onFling(null, null, 700, 0);
			// }
			// // 焦点位置大于最后一个可见位置
			// else if (mFocusedPosition > mLVisibleIndex)
			// {
			// onFling(null, null, -700, 0);
			// }
			onHScroll(isAnimate);
		}
	}

	/**
	 * 设置图标是否得到焦点
	 * 
	 * @param isFocused
	 */
	public void setItemFocus(boolean isFocused) {
		if (!isOutOfIndex(mFocusedPosition, true) && mFocusedPosition < getChildCount()) {
			XComponent component = getChildAt(mFocusedPosition);
			component.setFocused(isFocused);
			if (isFocused) {
				mFocusComponent = component;
			}
		}
	}

	/**
	 * 单独从文件夹中拿出一个元素增加到根部
	 * 
	 * @param component
	 *            被加入的component
	 * @param x
	 *            component 的 X坐标
	 * @param y
	 *            component 的Y坐标
	 */
	public synchronized void addCellComponent(FunAppItemInfo itemInfo, int x, int y, boolean direct) {
		FunControler funControler = AppFuncFrame.getFunControler();
		ApplicationIcon xComponent = (ApplicationIcon) getChildAt(mAdapterCount - 1);
		if (xComponent != null) {
			int position = xComponent.getInfo().getIndex();
			try {
				funControler.addFunAppItemInfo(position + 1, itemInfo);
			} catch (DatabaseException e) {
				AppFuncExceptionHandler.handle(e);
				return;
			}
		} else {
			return;
		}

		boolean b = addCellComponent(mAdapterCount);
		// 如果加入不成功，会出现图标丢失
		if (!b) {
			return;
		}
		// mDragComponent = getChildAt(mAdapterCount - 1);
		setDragComponent(getChildAt(mAdapterCount - 1));
		mDragComponentIndex = mAdapterCount - 1;
		mDragComponentTargetIndex = mAdapterCount - 1;
		mDragComponentX = mDragComponent.mX;
		mDragComponentY = mDragComponent.mY;
		x = x - getAbsX();
		y = y - getAbsY();
		// mDragComponent.setXY((int) (x - mCellWidth * 0.5f),
		// (int) (y - mCellHeight * 0.5f));
		// DeliverMsgManager.getInstance().onChange(
		// AppFuncConstants.ALLAPPS_TABCONTENT,
		// AppFuncConstants.ONLONGCLICKDOWN, null);
		// 计算位置的x，y 的 index
		int index = 0;
		int tempX = x - mOffsetX;
		int tempY = y - mOffsetY;
		// LogUnit.i("F:" + mFVisibleIndex + " L:" + mLVisibleIndex + " Screen:"
		// + mCurScreen);

		int startPos = mFVisibleIndex;
		if (startPos < 0) {
			startPos = 0;
		}
		for (int i = startPos; i <= mLVisibleIndex; i++) {
			XComponent c = getChildAt(i);
			// 矩形比较
			if (c.contains(tempX, tempY)) {
				index = i;
				break;
			}
			if (i == mAdapterCount - 1) {
				index = mAdapterCount - 1;
				break;
			}
		}
		if (index != mAdapterCount - 1) {
			moveComponent(mAdapterCount - 1, index);
			boolean success = mAdapter.switchPosition(mAdapterCount - 1, index);
			if (!success) {
				AppFuncExceptionHandler.handle(null);
				return;
			}
			mAdapter.loadApp();
			mDragComponentIndex = index;
		}
		mDragComponent.setXY((int) (x - mCellWidth * 0.5f), (int) (y - mCellHeight * 0.5f));
		((ApplicationIcon) mDragComponent).setDragEffect();
		if (((ApplicationIcon) mDragComponent).isShowTwoLines() != mShowTwoLines) {
			((ApplicationIcon) mDragComponent).setShowTwoLines(mShowTwoLines);
			((ApplicationIcon) mDragComponent).resetLayoutParams(mDragComponent.mX,
					mDragComponent.mY, mDragComponent.getWidth(), mDragComponent.getHeight());
		}
		mLastTouchX = mDragComponent.mX + mCellWidth / 2;
		mLastTouchY = mDragComponent.mY + mCellHeight / 2;
		// 最后一屏从文件夹中增加一个图标到功能表需要增加一个可视区域
		if (mLVisibleIndex - mFVisibleIndex < mSingleScreenCells - 1) {
			mLVisibleIndex = mAdapterCount - 1;
		}
		if (direct) {
			onLongClickUp(true);
		}
	}

	/**
	 * 单独增加元素到队列最后
	 * 
	 * @return true 增加操作成功
	 */
	private boolean addCellComponent(int index) {
		mAdapter.loadApp();
		mAdapterCount = mAdapter.getCount();
		if (index < 0 || index >= mAdapterCount) {
			try {
				throw new IllegalStateException("index : " + index + " is larger than"
						+ mAdapterCount + " or less than 0");
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			return false;
		}
		// LogUnit.i("index" + index);
		int lastIndex = index - 1;
		removeIndicator();
		XComponent component = getChildAt(lastIndex);
		XComponent c = null;

		int x = 0, y = 0;

		if (!mIsVScroll && lastIndex % mSingleScreenCells == mSingleScreenCells - 1)// 横滑屏幕中最后一个
		{
			c = mAdapter.getComponent(index, mScreen * mWidth + mPaddingLeft, mPaddingTop,
					mCellWidth, mCellHeight, c, this);
			if (c != null) {
				mScreen++;
				// LogUnit.e("--------------" + mScreen);
				mTotalHeight -= mWidth;
				insertComponent(index, c);
				lastIndex = mAdapterCount - 1;
			}
		} else {
			if (lastIndex % mColunmNums == mColunmNums - 1)// 列尾
			{
				c = mAdapter.getComponent(index, component.mX - (mColunmNums - 1) * mCellWidth,
						component.mY + component.getHeight(), mCellWidth, mCellHeight, c, this);
				if (c != null && mIsVScroll) {
					mRows++;
					mTotalHeight -= mCellHeight;
				}
			} else
			// 列中
			{
				c = mAdapter.getComponent(index, component.mX + mCellWidth, component.mY,
						mCellWidth, mCellHeight, c, this);
			}
			if (c != null) {
				insertComponent(index, c);
				if (mIsSupportScroll) {
					if (mIsVScroll) {
						updateVisiblePositionV();
					} else {
						updateVisiblePositionH();
					}
				}
			}
		}
		if (c != null) {
			((BaseAppIcon) c).resetLayoutParams(c.mX, c.mY, mCellWidth, mCellHeight);
			if (mIsDragable) {
				((BaseAppIcon) c).setEditMode(mInDragStatus);
			}
		}
		mAdapterCount = mAdapter.getCount();
		if (mAdapterCount != mStoreComponent.size()) {
			error();
			return false;
		}
		initIndicator(mIndicatorPos);
		return true;
	}

	private void error() {
		try {
			throw new IllegalStateException("mAdapterCount is not equal mStoreComponent size!!!");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 水平方向上，根据偏移量改变，更新可视区域的位置下标
	 */
	protected void updateVisiblePositionH() {
		// final int scrollX = Math.max(0, -mOffsetX);
		// final int curScreen = scrollX / mWidth;
		// // 一屏内剩余偏移量
		// final int restValue = scrollX % mWidth;
		// // 滑屏过程中，第一个显示组件位置与正常静止第一个显示组件偏移量
		// final int firstVisiableIndex = (int) (restValue / mCellWidth);
		// mFVisibleIndex = curScreen * mSingleScreenCells + firstVisiableIndex;
		// // 根据第一个显示组件所属的列，放大缩小显示区
		// int firstVisiableColunm = mFVisibleIndex % mColunmNums;
		// if (firstVisiableColunm == 0) {
		// if (restValue == 0) {
		// mLVisibleIndex = mFVisibleIndex + mSingleScreenCells - 1;
		// } else {
		// mLVisibleIndex = mFVisibleIndex + mColunmNums
		// * (mRowNums * 2 - 1);
		// }
		// } else {
		// mLVisibleIndex = (mColunmNums - firstVisiableColunm) + mColunmNums
		// * (mRowNums - 1) * 2 + firstVisiableColunm + mFVisibleIndex;
		// }
		// fixLastIndex();

		mFVisibleIndex = mScreenScroller.getCurrentScreen() * mSingleScreenCells;
		mLVisibleIndex = mFVisibleIndex + mSingleScreenCells;

		fixLastIndex();
	}

	/**
	 * 竖直方向上，根据偏移量改变，更新可视区域的位置下标
	 */
	protected void updateVisiblePositionV() {

		int offset = mOffsetY;
		if (mOffsetY > 0) {
			offset = 0;
			// return;
		}
		mCurRow = -offset / mCellHeight;

		mFVisibleIndex = mCurRow * mColunmNums;
		if (mFVisibleIndex < 0) {
			mFVisibleIndex = -1;
		}
		// 先计算出没有被分配作为单元格的高度的剩余长度。
		int restLength = mRealHeight % mRowNums;
		mLastVisiableRow = (-offset + mRealHeight) / mCellHeight - 1;
		if (((-offset + mRealHeight - restLength) % mCellHeight) != 0) {
			mLastVisiableRow++;
		}

		int position = (mLastVisiableRow * mColunmNums) + mColunmNums - 1;

		mLVisibleIndex = position;
		fixLastIndex();
		if (mIndicator != null) {
			mIndicator.setCurPosition(-mOffsetY);
		}
	}

	protected void fixLastIndex() {
		if (mLVisibleIndex >= mAdapterCount) {
			mLVisibleIndex = mAdapterCount - 1;
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// 启动应用程序后。停止响应长按事件
		if (mIsStopResponse) {
			return;
		}
		// 让功能表在长按时不能横竖屏切换
		keepCurrentOrientation();
		mLongPressed = true;
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			// 正在运行，不弹锁定编辑提示
			if (!(XViewFrame.getInstance().getAppFuncMainView().getCurrentContent() instanceof ProManageTabBasicContent)) {
				LockScreenHandler.showLockScreenNotification(mContext);
			}
		}
		if (!mIsDragable || (mIsDragable && mInDragStatus)) {
			return;
		}
		if (!isScrollFinished()) {
			return;
		}
		int eX = (int) e.getX();
		int eY = (int) e.getY();
		// 相对于屏幕偏移
		int x = eX - getAbsX();
		int y = eY - getAbsY();
		if (contains(x + mX, y + mY)) {
			if (mIsMotionRunning || mIsStopResponse) {
				return;
			}
			if (AppFuncFrame.getFunControler().isHandling()) {
				if (mToast != null) {
					mToast.cancel();
				}
				try {
					mToast = DeskToast.makeText(mActivity, R.string.app_fun_strat_loading,
							Toast.LENGTH_SHORT);
					mToast.show();
				} catch (OutOfMemoryError error) {
					error.printStackTrace();
					OutOfMemoryHandler.handle();
				}
				return;
			}
			y += -mOffsetY;
			x += -mOffsetX;

			int startPos = mFVisibleIndex;
			if (startPos < 0) {
				startPos = 0;
			}
			for (int i = startPos; i <= mLVisibleIndex; i++) {
				XComponent component = getChildAt(i);
				if (component == null) {
					continue;
				}
				// 矩形比较
				if (component.contains(x, y)) {
					// 1.如果不是在编辑模式，启动编辑模式
					// 2.记录被选中的图标
					if (component.getEventListener() != null && component instanceof BaseAppIcon) {
						boolean b = component.getEventListener().onEventFired(this,
								EventType.LONGCLICKEVENT, e, -mOffsetX, -mOffsetY);
						if (b) {
							if (!mInDragStatus) {
								setDragStatus(true);
								long pattern[] = { 0, 30 };
								mUtils.vibrate(pattern, -1);
								if (AppFuncHandler.getInstance().getSelectedTabContent() instanceof AllAppTabBasicContent) {
									DeliverMsgManager.getInstance().onChange(
											AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
											AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR, null);
								}
								StatisticsData.countUserActionData(
										StatisticsData.FUNC_ACTION_ID_APPLICATION,
										StatisticsData.USER_ACTION_ONE,
										IPreferencesIds.APP_FUNC_ACTION_DATA);

							}

							// showAddButton(false);
							// mDragComponent = component;
							setDragComponent(component);
							mDragComponentIndex = i;
							mDragComponentTargetIndex = i;
							mDragComponentX = component.mX;
							mDragComponentY = component.mY;
							// component
							// .setXY((int) (x - component.getWidth()
							// * 0.5f + mOffsetX),
							// (int) (y - component.getHeight()
							// * 0.5f + mOffsetY));
							component.offsetLeftAndRight(mOffsetX);
							component.offsetTopAndBottom(mOffsetY);
							mLastTouchX = x + mOffsetX;
							mLastTouchY = y + mOffsetY;
							FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
							if (handler != null) {
								if (handler.isShowActionBar()) {
									AppFuncHandler.getInstance().setHomeIconChangeDesk(true, true);
								}
//								if (!(mAttchPanel instanceof AppFuncFolder)) {
									if (handler.isShowTabRow()) {
										AppFuncHandler.getInstance().setTopChange(true, true);
									} else {
										AppFuncHandler.getInstance().setTopChange(true, false);
									}
//								}
							}
						}
					}

					break;
				}
			}
		} else {
			resetIconPos(e);
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// final int threshold = 500;
		// final int tolerance = 30;
		// // 计算速度的方向，以垂直向下为0度，顺时针为(0, -180)，逆时针为(0, 180)
		// int degree = (int)Math.toDegrees(Math.atan2(velocityX, velocityY));
		// if(mIsVScroll){
		// // 竖向滚屏的时候检测是否横滑
		// if(velocityX > threshold && 90 - tolerance < degree && degree < 90 +
		// tolerance){
		// // 向右滑动
		// XViewFrame.getInstance().getAppFuncMainView().gotoNextTab();
		// }
		// else if(velocityX < -threshold && -90 - tolerance < degree && degree
		// < -90 + tolerance){
		// // 向左滑动
		// XViewFrame.getInstance().getAppFuncMainView().gotoPreviousTab();
		// }
		// }
		// else{
		// // 横向滚屏的时候检测是否竖滑
		// if(velocityY > threshold && -tolerance < degree && degree <
		// tolerance){
		// // 向下滑动
		// XViewFrame.getInstance().getAppFuncMainView().gotoNextTab();
		// }
		// else if(velocityY < -threshold
		// && (180 - tolerance < degree || degree < -180 + tolerance)){
		// // 向上滑动
		// XViewFrame.getInstance().getAppFuncMainView().gotoPreviousTab();
		// }
		// }
		return true;
	}

	@Override
	protected boolean animate() {
		// 拖动图标到屏幕边缘触发滚屏
		if (mIsMoveStop) {
			if (mIsVScroll) {
				mScheduler.schedule(Scheduler.TASK_FRAME, mScheduler.getClock().getFrame(), 1, 1,
						mMoveScreenTask, null);
			} else {
				final long time = mScheduler.getClock().getTime();
				if (time > mMoveScreenTask.mNextTaskExecuteTime && mScreenScroller.isFinished()) {
					mScheduler.schedule(Scheduler.TASK_TIME, time, mScrollDuration * 2,
							mScrollDuration, mMoveScreenTask, null);
					mMoveScreenTask.mNextTaskExecuteTime = time + mScrollDuration
							+ MoveScreenTask.MOVE_SCREEN_IDLE_TIME;
				}
			}
		} else {
			if (mMoveScreenTask.mIsStartComputeTime) {
				mMoveScreenTask.mIsStartComputeTime = false;
			}
		}

		// boolean ret = false;
		// if (mOffsetMotion != null) {
		// final boolean finished = mIsVScroll ? mScroller.isFinished()
		// : mScreenScroller.isFinished();
		// if (finished) {
		// detachAnimator(mOffsetMotion);
		// mOffsetMotion = null;
		// isMotionRunning = false;
		// } else {
		// ret = true;
		// }
		// }
		boolean ret = false;

		if (mInvalidated) { // 其他引起重绘的原因
			mInvalidated = false;
			ret = true;
		}
		if (ret) {
			if (mIsVScroll) {
				mIsMotionRunning = mScroller.computeScrollOffset();
			} else {
				if (mScreenScroller != null) {
					mIsMotionRunning = mScreenScroller.computeScrollOffset();
				}
			}
		}
		return super.animate() || ret;
	}

	private boolean onHScroll(boolean isAnimate) // 根据当前的位置和焦点的位置进行横向翻屏
	{
		XComponent component = getChildAt(mFocusedPosition);
		if (component == null) {
			return false;
		}
		if (!mIsSupportScroll || mIsVScroll) {
			return false;
		}
		if (mOffsetY > 0 || mOffsetY < mTotalHeight || mOffsetX > 0 || mOffsetX < mTotalHeight) {
			return false;
		}

		mIsScrolling = false; // 如果是fling状态，则不当做正在滑动

		// int maxDistance = MAX_PULL_LENGTH;
		// int step = SCROLL_STEP_H;
		// int start = 0;

		resetMotion(); // 获取当前动作的位置作为起点

		// start = mOffsetX;

		// // 寻找当前焦点所在的屏
		// for (int i = 1; i <= mScreen; i++) {
		// int distance = i * mWidth;
		// if (compX <= distance) {
		// maxDistance = -(i - 1) * mWidth;
		// break;
		// }
		// }

		// int firstStepDis = (maxDistance - start) * 2 / step;
		// LogUnit.d("new 2857");
		// mOffsetMotion = new XALinear(1, XALinear.XALINEAR_ECACCEL, start
		// + firstStepDis, 0, maxDistance, 0, step,
		// (maxDistance - start - firstStepDis) * 7 / (step << 2), 0);
		//
		// attachAnimator(mOffsetMotion);
		final int dstScreen = component.mX / mWidth;
		if (!isAnimate) {
			// mScreenScroller.gotoScreen(dstScreen, 0, true);
			mScreenScroller.setCurrentScreen(dstScreen);
		} else {
			mScreenScroller.gotoScreen(dstScreen, mScrollDuration, true);
		}

		mIsMotionRunning = true;
		postInvalidate();

		return false;
	}

	protected void resetScroller() {
		final int scroll = mScroller.getScroll();
		final float ratio = mOrientation == VERTICAL ? (float) 1122 / 1278 : (float) 1278 / 1122;
		mScroller.setScroll((int) (scroll * ratio));
	}

	protected float getScrollerRatio() {
		int scroll = mScroller.getScroll();
		int totalHeight = mScroller.getTotalHeight();
		float ratio = 1.0f * scroll / totalHeight;
		return ratio;
	}

	protected void resetMotion() {
		// if (mOffsetMotion != null) {
		// detachAnimator(mOffsetMotion);
		// isMotionRunning = false;
		// mOffsetMotion = null;
		// }
		mIsMotionRunning = false;
		if (mIsVScroll) {
			mScroller.abortAnimation();
		} else {
			mScreenScroller.abortAnimation();
		}
	}

	/**
	 * 还原图标位置.如处理滚动时，当用户放弃当前操作后，做的一些处理。
	 */
	private boolean resetIconPos(MotionEvent event) {
		boolean ret = false;
		mIsScrolling = false;
		if (mIsVScroll) {
			if (mOffsetY > 0) { // 到顶部的时候 ,需要反弹
				resetMotion();
				// LogUnit.d("new 2898");
				// mOffsetMotion = new XALinear(1, XALinear.XALINEAR_EBACCEL,
				// mOffsetY, 0, 0, 0, SCROLL_STEP, (-mOffsetY * 7)
				// / (SCROLL_STEP << 2), 0);
				// attachAnimator(mOffsetMotion);
				mIsMotionRunning = true;
				ret = true;
			} else if (mOffsetY < mTotalHeight) { // 到底部的时候
				resetMotion();
				// int len = mTotalHeight - mOffsetY;
				// LogUnit.d("new 2908");
				// mOffsetMotion = new XALinear(1, XALinear.XALINEAR_EBACCEL,
				// mOffsetY, 0, mTotalHeight, 0, SCROLL_STEP, (len * 7)
				// / (SCROLL_STEP << 2), 0);
				// attachAnimator(mOffsetMotion);
				mIsMotionRunning = true;
				ret = true;
			}
			postInvalidate();
			// 这里加判空是因为handleFocusChange()里直接传了个null进来，应该是当时忘记做判断了，现在加上
			if (event != null) {
				mScroller.onTouchEvent(event, MotionEvent.ACTION_UP);
			}

		} else {
			resetMotion();
			int num = Math.abs(mOffsetX) % mWidth; // 偏移量
			if (num == 0) { // 如果不需要检查
				return false;
			}
			// mOffsetMotion = new XALinear(1, XALinear.XALINEAR_EBACCEL,
			// mOffsetX, 0, 0, 0, SCROLL_STEP_H, (-mOffsetX * 3) /
			// (SCROLL_STEP_H << 1), 0);
			// attachAnimator(mOffsetMotion);
			if (event != null) {
				mScreenScroller.onTouchEvent(event, MotionEvent.ACTION_UP);
			}

			mIsMotionRunning = true;
			postInvalidate();
			ret = true;
		}
		return ret;
	}

	@Override
	protected void onShow() {
		super.onShow();
		setFolderClose();
		onLongClickUp(false);
		// LogUnit.i("onShow");
	}

	@Override
	protected void onHide() {
		super.onHide();
		setIconFolderReady(false);
		setFocused(false);
		setFolderClose();
		setDragStatus(false);
		AppFuncHandler.getInstance().changeHomeIconBackground(false);

		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (handler != null && handler.isShowActionBar()) {
			AppFuncHomeComponent homeComponent = AppFuncHandler.getInstance()
					.getCurrentHomeComponent();
			XComponent component = XViewFrame.getInstance().getAppFuncMainView()
					.getSeletedTabContentView();
			if ((component instanceof AllAppTabBasicContent || component instanceof ProManageTabBasicContent)
					&& !(mAttchPanel instanceof AppFuncFolder)) {
				homeComponent.showDockContent(false);
				XViewFrame.getInstance().getAppFuncMainView().showTopNormalBar(false);
			}
		}
		mIsMoveStop = false;

		fixScoller();

		// onLongClickUp(false);
		// LogUnit.i("onHide");
	}

	/**
	 * 模拟弹起事件，终止图标移动
	 */
	public void startLoading() {
		// LogUnit.d("startLoading InDragStatus : " + mInDragStatus);
		if (mIsDragable) {

			// onLongClickUp(false);
			// 当参数为false时，会包含onLongClickUp(false)执行
			setDragStatus(false);
		}
		if (mToast != null) {
			mToast.cancel();
		}
		// if (mIsShowed) {
		// if (mToast != null) {
		// mToast.cancel();
		// }
		// try {
		// mToast = DeskToast.makeText(mActivity,
		// R.string.app_fun_strat_loading, Toast.LENGTH_SHORT);
		// mToast.show();
		// } catch (OutOfMemoryError e) {
		// e.printStackTrace();
		// OutOfMemoryHandler.handle();
		// }
		// }
	}

	public void endLoading() {
		if (mToast != null) {
			mToast.cancel();
		}
		// setDragable(true);
		// LogUnit.i("endLoading");
		// if (mIsShowed) {
		// mToast = DeskToast.makeText(mActivity,
		// R.string.app_fun_end_loading, Toast.LENGTH_SHORT);
		// mToast.show();
		// }
	}

	private void setIconFolderReady(boolean isReady) {
		if (mFolderImageIcon != null) {
			mFolderImageIcon.setIsFolderReady(isReady);
			if (!isReady) {
				mFolderImageIcon = null;
			}
		}
		if (mDragComponent != null && mDragComponent instanceof ApplicationIcon) {
			ApplicationIcon target = (ApplicationIcon) mDragComponent;
			if (!isReady) {
				target.setIsIntoFolderReady(isReady);
			} else {
				if (!target.isFolder()) {
					target.setIsIntoFolderReady(isReady);
				}
			}
		}
	}

	private boolean isIconFolderReady() {
		if (mFolderImageIcon != null) {
			return mFolderImageIcon.isFolderReady();
		} else {
			return false;
		}
	}

	private synchronized void checkIsInFolderRegion() {
		// 检查是否还在原来文件夹区域，如果是则继续显示，不是就检查最新文件夹区域在哪个图标上，让他显示文件夹
		// if (!mIsFolderEnable) {
		// return;
		// }
		// if (mDragComponent == null) {
		// return;
		// }
		// int x = mDragComponent.centerX() - mOffsetX;
		// int y = mDragComponent.centerY() - mOffsetY;
		// // ////////////////////////////
		// isOutOfIndex(mFVisibleIndex, true);
		// isOutOfIndex(mLVisibleIndex, true);
		// // ///////////////////////////
		// for (int i = mFVisibleIndex; i <= mLVisibleIndex; i++) {
		// XComponent component = getChildAt(i);
		// if (component.contains(x, y)) {
		// if (i == mDragComponentTargetIndex) {
		// continue;
		// }
		// // 文件夹
		// int folderRegionW = (int) (component.getWidth() * 0.15f);
		// if (x <= component.centerX() + folderRegionW
		// && x >= component.centerX() - folderRegionW) {
		// ApplicationIcon scr = ((ApplicationIcon) component);
		// ApplicationIcon target = ((ApplicationIcon) mDragComponent);
		// if ((scr.isFolder() && !target.isFolder())
		// || (!scr.isFolder() && !target.isFolder())) {
		// if (mFolderImageIcon != null) {
		// if (!scr.equals(mFolderImageIcon)) {
		// LogUnit.i("checkIsInFolderRegion : true");
		// setIconFolderReady(false);
		// mFolderImageIcon = scr;
		// setIconFolderReady(true);
		// }
		// } else {
		// LogUnit.i("checkIsInFolderRegion : true");
		// mFolderImageIcon = scr;
		// setIconFolderReady(true);
		// }
		// }
		// }
		// }
		// }
		if (!mIsFolderEnable) {
			return;
		}
		if (mDragComponent == null) {
			return;
		}
		if (mFolderImageIcon != null) {
			int x = mDragComponent.centerX() - mOffsetX;
			int folderRegionW = (int) (mFolderImageIcon.getWidth() * 0.15f);
			if (x <= mFolderImageIcon.centerX() + folderRegionW
					&& x >= mFolderImageIcon.centerX() - folderRegionW) {

			} else {
				setIconFolderReady(false);
			}
		}
	}

	@Override
	public void setFocused(boolean isFocused) {
		super.setFocused(isFocused);
		if (isFocused) {
			if (mAdapterCount > 0) {
				if (mFocusedPosition == -1) {
					mFocusedPosition = mFVisibleIndex;
					setItemFocus(true);
				}
			}
		} else {
			if (mFocusedPosition != -1) {
				if (mAdapterCount > 0) {
					setItemFocus(false);
					mFocusedPosition = -1;
				}
				mFocusedPosition = -1;
			}
		}
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.XGRIDONCHANGE : {
				invalidateAndReInit();
			}
				break;

			case AppFuncConstants.APP_GRID_LOCATE_ITEM : {
				// 根据intent定位到具体的某个图标
				Intent targetIntent = (Intent) obj;
				locateAppByIntent(targetIntent);
			}
				break;

			case AppFuncConstants.XGRID_DRAG_STATUS : {
				boolean isStart = (Boolean) obj;
				onLongClickUp(false);
				setDragStatus(isStart);
			}
				break;
			case AppFuncConstants.EXITAPPFUNCFROMHOME : {
				ApplicationIcon icon = (ApplicationIcon) mDragComponent;
				if (icon == null) {
					return;
				}
				if (!sIsExit) {
					sIsExit = true;
				} else {
					return;
				}
//				sFirstLayout = false;

				// 发送统计信息
				mScheduler.terminateAll();
				AppIconForMsg appIconForMsg = null;
				FolderIconForMsg folderIconForMsg = null;
				if (!icon.isFolder()) {
					LogUnit.d("new 3142");
					appIconForMsg = new AppIconForMsg(icon.getIcon(), icon.getTitle(),
							mDragComponent.mX + getAbsX(), mDragComponent.mY + getAbsY(),
							mDragComponent.getWidth(), mDragComponent.getHeight(),
							icon.getItemType(), icon.getID(), icon.getIntent());
				} else {
					FunFolderItemInfo folderInfo = (FunFolderItemInfo) (icon.getInfo());
					ArrayList<FunAppItemInfo> list = folderInfo.getFunAppItemInfosForShow();
					LogUnit.d("new 3152");
					ArrayList<AppItemInfo> appItemInfoList = new ArrayList<AppItemInfo>();
					for (FunAppItemInfo funappItemInfo : list) {
						// if (!funappItemInfo.isHide())
						// {
						appItemInfoList.add(funappItemInfo.getAppItemInfo());
						// }
					}
					LogUnit.d("new 3160");
					folderIconForMsg = new FolderIconForMsg(folderInfo.getFolderId(),
							icon.getIcon(), icon.getTitle(), mDragComponent.mX + getAbsX(),
							mDragComponent.mY + getAbsY(), mDragComponent.getWidth(),
							mDragComponent.getHeight(), appItemInfoList);
				}
				AbstractFrame appFuncFrame = GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME);
				// 此种情况下退出桌面时不重新加载桌面
				GoLauncher.sendMessage(appFuncFrame, IDiyFrameIds.APPFUNC_FRAME,
						IDiyMsgIds.APPDRAWER_EXIT_FOR_DRAG, 0, null, null);
				// 立刻移除自己
				GoLauncher.sendMessage(appFuncFrame, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);

				// 显示拖动层
				GoLauncher.sendMessage(appFuncFrame, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.DRAG_FRAME, null, null);

				// 将数据信息发送给拖动层
				int screenIndex = (Integer) obj;
				GoLauncher.sendMessage(appFuncFrame, IDiyFrameIds.DRAG_FRAME,
						IDiyMsgIds.APPFUNC_DRAG_START, screenIndex, appIconForMsg == null
								? folderIconForMsg
								: appIconForMsg, null);
				// 移除文件夹 异步了，退出时来不及关闭文件夹，先注释wuziyi
//				AppFuncHandler.getInstance().removeFolder();

				// 通知移动到桌面控件变回原色
				// DeliverMsgManager.getInstance().onChange(
				// AppFuncConstants.ALLAPPS_APPMOVETODESK,
				// AppFuncConstants.MOVETODESKISUNTOUCHED, null);

				DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_TABCONTENT,
						AppFuncConstants.RESETHOMEMOVETODESK, null);
				// 重置各种状态
				vibrate();
				setDragStatus(false);
				icon.clearMotion();
			}
				break;
			//			case AppFuncConstants.TUTORIAL_DRAG_MODE : {
			//				if (StaticTutorial.sCheckFuncDrag) {
			//					StaticTutorial.sCheckFuncDrag = false;
			//					PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
			//							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			//					Editor editor = sharedPreferences.edit();
			//					editor.putBoolean(IPreferencesIds.SHOULD_SHOW_APPFUNC_DRAG_GUIDE, false);
			//					editor.commit();
			//					// 暂时不需要进入编辑模式。
			//					// setDragStatus(true);
			//					// onLongClickUp(true);
			//					GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_FUNC_DRAG);
			//					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
			//							IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
			//				}
			//			}
			//				break;
			case AppFuncConstants.TUTORIAL_CREATE_FOLDER_MODE : {
				if (StaticTutorial.sCheckFuncFolder) {
					StaticTutorial.sCheckFuncFolder = false;
					PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
					sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_APPFUNC_FOLDER_GUIDE,
							false);
					sharedPreferences.commit();
					setDragStatus(true);
					onLongClickUp(true);
					GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_FUNC_FOLDER);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
				}
			}
				break;
			// 半透明所有功能表图标
			case AppFuncConstants.ALL_APP_ALPHA_ICON : {
				HashMap<String, Object> values = (HashMap<String, Object>) obj;
				boolean alpha = (Boolean) values.get("isopen");
				Intent src = (Intent) values.get("target");
				// int value = 255;
				// if (alpha) {
				// value = 51;
				// }
				ApplicationIcon srcIcon = getIconByInfo(src);
				synchronized (this) {
					int size = mStoreComponent.size();
					for (int i = 0; i < size; i++) {
						XComponent c = mStoreComponent.get(i);
						if (c != null && c instanceof ApplicationIcon) {
							ApplicationIcon icon = (ApplicationIcon) c;
							Intent iconIntent = icon.getIntent();
							icon.setDrawingCacheEnabled(true);
							icon.setIsDrawAppText(true);
							// icon.setAlpha(255);
							// icon.setNameVisible(true);
							if (iconIntent != null && iconIntent.getAction() != null) {
								if (!iconIntent.getAction().equals(src.getAction())) {
									// icon.setNameVisible(!alpha);
									// icon.setAlpha(value);
								}
							}

							if (alpha && onSameLine(c, srcIcon)) {
								icon.setDrawingCacheEnabled(!alpha);
								icon.setIsDrawAppText(!alpha);
								// icon.setNameVisible(!alpha);
							}
						}
					}
				}

				// if (srcIcon!=null) {
				// srcIcon.setNameVisible(!alpha);
				// }

				mDrawFadeRect = !alpha;
			}
				break;
			case AppFuncConstants.ADD_ITEM_TO_WORKSPACE_START :
				mIsDragable = false;
				mLockScreen = true;
				keepCurrentOrientation();
				break;
			case AppFuncConstants.ADD_ITEM_TO_WORKSPACE_FINISH :
				mIsDragable = true;
				mLockScreen = false;
				resetOrientation();
				break;
			/*
			 * case IDiyMsgIds.SCREEN_ON: Log.i("", "--------screen on"); if
			 * (mInDragStatusBeforeHide) { setDragStatus(true);
			 * AppFuncHandler.getInstance().setHomeIconChangeDesk(true); }
			 * break;
			 */

			case AppFuncConstants.IS_GO_TO_INFO :
				mIsGotoInfo = true;
				break;
			default :
				break;
		}
	}

	/**
	 * 检测两个图标是否位于同一行
	 * 
	 * @param srcIcon
	 * @param targetIcon
	 * @return
	 */
	private boolean onSameLine(XComponent srcIcon, XComponent targetIcon) {
		if (srcIcon == null || targetIcon == null) {
			return false;
		}
		int srcRow = -1;
		int targetRow = -1;

		if (mIsVScroll) {
			srcRow = (srcIcon.mY + mSearchRowHeightOffset - mPaddingTop) / mCellHeight;
			targetRow = (targetIcon.mY + mSearchRowHeightOffset - mPaddingTop) / mCellHeight;
			if (srcRow == targetRow) {
				return true;
			}
		} else {
			srcRow = (srcIcon.mY - mPaddingTop) / mCellHeight;
			targetRow = (targetIcon.mY - mPaddingTop) / mCellHeight;
			if (srcRow == targetRow) {
				return true;
			}
		}
		return false;
	}

	public boolean isVScroll() {
		return mIsVScroll;
	}

	public void setVScroll(boolean mIsVScroll) {
		this.mIsVScroll = mIsVScroll;
	}

	public boolean isSupportScroll() {
		return mIsSupportScroll;
	}

	public void setSupportScroll(boolean isSupportScroll) {
		this.mIsSupportScroll = isSupportScroll;
	}

	public int getColunmNums() {
		return mNewColunmNums;
	}

	public int getColunm() {
		return mColunmNums;
	}

	public void setColunmNums(int mColunmNums) {
		this.mNewColunmNums = mColunmNums;
	}

	public int getRowNums() {
		return mNewRowNums;
	}

	public void setRowNums(int mRowNums) {
		this.mNewRowNums = mRowNums;
	}

	@Override
	public int getPaddingLeft() {
		return mNewPaddingLeft;
	}

	public void setPaddingLeft(int paddingLeft) {
		this.mNewPaddingLeft = paddingLeft;
	}

	@Override
	public int getPaddingRight() {
		return mNewPaddingRight;
	}

	public void setPaddingRight(int paddingRight) {
		this.mNewPaddingRight = paddingRight;
	}

	@Override
	public int getPaddingTop() {
		return mNewPaddingTop;
	}

	public void setPaddingTop(int paddingTop) {
		this.mNewPaddingTop = paddingTop;
	}

	@Override
	public int getPaddingBottom() {
		return mNewPaddingBottom;
	}

	public void setPaddingBottom(int paddingBottom) {
		this.mNewPaddingBottom = paddingBottom;
	}

	public byte getOrientation() {
		return mNewOrientation;
	}

	/**
	 * 设置网格方向
	 * 
	 * @param mOrientation
	 */
	public void setOrientation(byte mOrientation) {
		this.mNewOrientation = mOrientation;
	}

	/**
	 * 返回实际行数
	 * 
	 * @return
	 */
	public int getRows() {
		return mRows;
	}

	public boolean isIsExit() {
		return sIsExit;
	}

	public static void setIsExit(boolean isExit) {
		sIsExit = isExit;
	}

	public boolean isDragable() {
		return mIsDragable;
	}

	public void setDragable(boolean mIsDragable) {
		this.mIsDragable = mIsDragable;
	}

	public int getFolderIndex() {
		XComponent component = getChildAt(mFolderIndex);
		ApplicationIcon icon = (ApplicationIcon) component;
		return icon.getInfo().getIndex();
	}


	public void setFVisibleIndex(int index) {
		mFVisibleIndex = index;
	}

	public XComponent getDragComponent() {
		return mDragComponent;
	}

	public int getDragComponentIndex() {
		return mDragComponentIndex;
	}

	public void setIsFolderEnable(boolean mIsFolderEnable) {
		this.mIsFolderEnable = mIsFolderEnable;
	}

	public boolean isFolderOpen() {
		return mIconMoveing == -1;
	}

	public void setFolderClose() {
		mIconMoveing = 0;
	}

	public int getTouchUpPostion() {
		return mTouchUpPostion;
	}

	// /////////////////////内部类//////////////////////////////////////////
	/**
	 * 处理图标移动
	 */
	class MoveIconTask implements ITask {

		@Override
		public void execute(long id, long time, Object userName) {
			// LogUnit.i("MoveIconTask");
			int[] position = (int[]) userName;
			if (checkRegionSame(position[0], position[1], mNotScrollSize - mMetrics.density)) {
				if (mIconMoveing == 0) {
					moveIcon();
				}
			} else {
				checkIsInFolderRegion();
			}
		}

		@Override
		public void finish(long id, long time, Object userName) {
		}
	}

	/**
	 * 移动屏幕
	 */
	class MoveScreenTask implements ITask {
		private static final int MOVE_SCREEN_IDLE_TIME = 1000; // 两次横向切屏之间的等待时间
		private volatile long mNextTaskExecuteTime; // 下一次横向滚屏任务的执行时间
		private volatile long mLastScrollTime; // 上一次竖向滚屏任务的执行时间
		private volatile boolean mIsStartComputeTime = false; // 是否开始计算竖向滚屏时间(触发竖向滚屏)
		private long mStartComputeTime; // 开始计算竖向滚屏的时间

		@Override
		public void execute(long id, long time, Object userName) {
			if (!mIsMoveStop) {
				mIsStartComputeTime = false;
				return;
			}
			if (isEqualMoveScreenRegion(mXMoveStop, mYMoveStop)) {
				setIconFolderReady(false);
				int size = mIndicatorSize * SCROLL_SIZE_MULTIPLE;
				if (mIsVScroll) {
					time = mScheduler.getClock().getTime();
					int dy = (int) (mHeight * 0.85f) * (int) (time - mLastScrollTime) / 1000;
					int totalHeight = 0;
					if (mRows > mRowNums) { // 小于一屏totalHeight直接为0
						totalHeight = mTotalHeight + mPaddingTop; // 由于滚动器的mRealTotalHeight被减掉了mPaddingTop，这里也要减回来，否则会出现跳动
						if (mVerticalEffectorType == WATERFALL_VERTICAL_EFFECTOR) {
							// 由于瀑布特效下，单元格高度被扩大，导致totalHeight计算有偏差，会偏大，这里对它进行下特殊处理，得出正确的值
							totalHeight = totalHeight - (mRowNums * mCellHeight - mRealHeight);
						}
						if (totalHeight > 0) {
							totalHeight = 0;
						}
					}
					if (mYMoveStop > mPaddingTop + mRealHeight - size) {
						// 竖滑到达下边界开始滑动
						if (-mOffsetY + dy >= -totalHeight) {
							if (!mIsStartComputeTime || !mVerticalCycleMode) {
								if (mScroller.isFinished()) {
									// 已经到达底部停下来
									mScroller.setScroll(-totalHeight);
									mIsStartComputeTime = true;
									mStartComputeTime = time;
								}
							} else {
								if (time - mStartComputeTime > MOVE_SCREEN_IDLE_TIME
										&& mScroller.isFinished()
										&& mAdapterCount > mSingleScreenCells) {
									if (mScroller instanceof CycloidScroller) { // 底部翻屏
										((CycloidScroller) mScroller)
												.scrollWithCycle((int) (0.5 * getCellHeight())
														+ mRealHeight
														- (mScroller.getScroll() - mScroller
																.getLastScroll()));
									}
									mStartComputeTime = time;
								}
							}
						} else {
							if (!mVerticalCycleMode || !mIsStartComputeTime || mOffsetY > 0
									|| time - mStartComputeTime > MOVE_SCREEN_IDLE_TIME) { // 刚翻屏之后要停顿一下
								mScroller.setScroll(dy - mOffsetY);
								mIsStartComputeTime = false;
							}
						}
					} else if (mYMoveStop < mPaddingTop + size) {
						// 向上滑动，dy取负值
						// 竖滑到达上边界开始滑动
						if (-mOffsetY - dy <= 0) {
							if (!mIsStartComputeTime || !mVerticalCycleMode) {
								if (mScroller.isFinished()) {
									// 已经到达顶部停下来
									mScroller.setScroll(0);
									mIsStartComputeTime = true;
									mStartComputeTime = time;
								}
							} else {
								if (time - mStartComputeTime > MOVE_SCREEN_IDLE_TIME
										&& mScroller.isFinished()
										&& mAdapterCount > mSingleScreenCells) {
									if (mScroller instanceof CycloidScroller) { // 顶部翻屏
										((CycloidScroller) mScroller)
												.scrollWithCycle(-((int) (0.5 * getCellHeight())
														+ mRealHeight + mScroller.getScroll()));
									}
									mStartComputeTime = time;
								}
							}
						} else {
							if (!mVerticalCycleMode || !mIsStartComputeTime
									|| -mOffsetY < -totalHeight - 1
									|| time - mStartComputeTime > MOVE_SCREEN_IDLE_TIME) { // 刚翻屏之后要停顿一下
								mScroller.setScroll(-dy - mOffsetY);
								mIsStartComputeTime = false;
							}
						}
					}
					mLastScrollTime = time;
				} else {
					int screen = mCurScreen;
					if (mXMoveStop >= mPaddingLeft + mRealWidth - size) {
						if (screen < mScreen - 1 || mScreenScroller.isCircular()) {

							++screen; // 横滑到达右边界开始滑动

						}
					} else if (mXMoveStop <= mPaddingLeft + size) {
						if (screen > 0 || mScreenScroller.isCircular()) {

							--screen; // 横滑到达左边界开始滑动

						}
					}
					if (!mIsMotionRunning && screen != mCurScreen) {
						mScreenScroller.gotoScreen(screen, mScrollDuration, true);
						// mOffsetMotion = new XALinear(1,
						// XALinear.XALINEAR_ECACCEL,
						// 0, 0, 1, 0, 1, 0, 0);
						// attachAnimator(mOffsetMotion);
						mIsMotionRunning = true;
						postInvalidate();
					}
				}
			}
		}

		@Override
		public void finish(long id, long time, Object userName) {
		}
	}

	// class MoveHomeIconTask implements ITask {
	//
	// @Override
	// public void execute(long id, long time, Object userName) {
	// if (mIsExit) {
	// return;
	// }
	// int[] position = (int[]) userName;
	// if (checkRegionSame(position[0], position[1], notScrollSize)) {
	// Message message = Message.obtain();
	// message.what = AppFuncConstants.EXITAPPFUNCFROMHOME;
	// message.obj = XBaseGrid.this;
	// UIThreadHandler.getInstance().sendMessage(message);
	// }
	// }
	//
	// @Override
	// public void finish(long id, long time, Object userName) {
	// }
	// }

	/**
	 * 判断是否需要拖拽图标
	 * 
	 * @author wenjiaming
	 * 
	 */
	class DragFolderTask implements ITask {

		@Override
		public void execute(long id, long time, Object userName) {
			Object[] position = (Object[]) userName;
			int x = (Integer) position[0];
			int y = (Integer) position[1];
			int i = (Integer) position[2];
			String title = (String) position[4];
			if (i >= mStoreComponent.size()) {
				return;
			}
			XComponent component = getChildAt(i);
			if ((component == null) || component instanceof RecentAppsIcon
					|| !(component instanceof BaseAppIcon)) {
				return;
			}
			BaseAppIcon icon = (BaseAppIcon) component;
			if (icon.getTitle() != null && !icon.getTitle().equals(title)) {
				return;
			}
			if (mIsReadyDragIcon && checkRegionSame(x, y, mDragBeginCheckSize)) {
				mIsReadyDragIcon = false;
				MotionEvent event = (MotionEvent) position[3];
				boolean b = component.getEventListener().onEventFired(XBaseGrid.this,
						EventType.LONGCLICKEVENT, event, -mOffsetX, -mOffsetY);
				event.recycle();
				if (b) {
					if (mShowAddButton) {
						mAddButtonHided = true;
						// ((FolderAdapter)mAdapter).setShowAddButton(false);
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFOLDER,
								AppFuncConstants.LAYOUTFOLDERGRID, null);
					}
					setDragComponent(component);
					mDragComponentIndex = i;
					mDragComponentTargetIndex = i;
					mDragComponentX = component.mX;
					mDragComponentY = component.mY;
					component.offsetLeftAndRight(mOffsetX);
					component.offsetTopAndBottom(mOffsetY);
					mLastTouchX = x;
					mLastTouchY = y;
					FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
					if (handler != null && handler.isShowActionBar()) {
						AppFuncHandler.getInstance().setHomeIconChangeDesk(true, true);
					}
					if (handler != null && handler.isShowTabRow()) {
						AppFuncHandler.getInstance().setTopChange(true, true);
					}
				}
			}
		}

		@Override
		public void finish(long id, long time, Object userName) {
		}
	}

	/**
	 * 合并图标到文件夹
	 * 
	 * @param b
	 *            是否创建新的文件夹
	 */
	public synchronized void mergeItemToFolder() {
		mIsEnteringFolder = false;

		FunControler controler = null;

		controler = AppFuncFrame.getFunControler();
		//		XComponent component = getChildAt(mFolderIndex);
		ApplicationIcon icon = mFolderIndexIcon;

		if (icon != null && icon.getInfo() instanceof FunFolderItemInfo) {
			mFolderInfo = (FunFolderItemInfo) icon.getInfo();
			if (mDragComponentTargetIcon.getInfo() instanceof FunAppItemInfo) {
				// 第一次创建的时候
				if (mFolderInfo.isMfolderfirstcreate()) {
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IDiyMsgIds.APPDRAWER_ENTER_FOLDER_EDIT_MODE, -1, null, null);
					mFolderInfo.setMfolderfirstcreate(false);
					mNeedOpenFolder = true;
				}
				FunAppItemInfo itemInfo = (FunAppItemInfo) (mDragComponentTargetIcon.getInfo());
				try {
					controler.moveFunAppItemToFolder(mFolderInfo, mFolderInfo.getSize(), itemInfo);
				} catch (DatabaseException e) {
					AppFuncExceptionHandler.handle(e);
					return;
				}
				// 通知桌面同步更新桌面文件夹
				ArrayList<AppItemInfo> addList = new ArrayList<AppItemInfo>();
				addList.add(itemInfo.getAppItemInfo());
				GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FOLDER_ADDITEMS, 0,
						mFolderInfo.getFolderId(), addList);
				GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.SCREEN_FOLDER_ADDITEMS, 0,
						mFolderInfo.getFolderId(), addList);
			

				// 在把程序拖入文件夹前如果文件夹是空的，则把程序放入文件夹后打开文件夹编辑界面
				if (mIsOpenEditFolder) {
					mIsOpenEditFolder = false;
					//					AppFuncMainView.sOpenFuncSetting = true;
					XViewFrame.getInstance().getAppFuncMainView().mOpenFuncSetting = true;
					Intent intent = new Intent(mActivity, AppFuncModifyFolderActivity.class);
					intent.putExtra(AppFuncConstants.FOLDER_ID, mFolderInfo.getFolderId());
					int requestCode = IRequestCodeIds.REQUEST_MODIFY_APPDRAWER_FOLDER;
					mActivity.startActivityForResult(intent, requestCode);
				}
			} else {
				// Some error happens, just return
				return;
			}
			mFolderInfo.sortAfterAdd();
			DeliverMsgManager.getInstance().onChange(
					AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
					AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR_NORMAL_FOLDER, icon.getInfo());
		}
		//
		int temp = mDragComponentTargetIndex;
		setDragComponent(null);
		mDragComponentIndex = -1;
		mDragComponentTargetIndex = -1;

		mFolderIndex = -1;
		mFolderIndexIcon = null;
		mDragComponentTargetIcon = null;

		if (XViewFrame.getInstance().getAppFuncMainView().isFolderShow()) {
			// 刷新文件夹的baseGrid
			requestLayout(false);
		}
		// 再刷新下功能表的baseGrid
		AppFuncHandler.getInstance().getCurrentGrid().requestLayout(false);

		//		if (isCreateFolder) {
		//			FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		//			if (handler != null && handler.isShowActionBar()) {
		//				AppFuncHandler.getInstance().setHomeIconChangeDesk(false, true);
		//			}
		//			if (handler != null && handler.isShowTabRow()) {
		//				AppFuncHandler.getInstance().setTopChange(false, true);
		//			}
		//		}

		ArrayList<AnimationInfo> animInfoList = new ArrayList<AnimationInfo>();
		for (int i = temp; i < mAdapterCount; i++) {
			XComponent xComponent = getChildAt(i);
			XComponent xComponent2 = null;
			if (i + 1 < mAdapterCount) {
				xComponent2 = getChildAt(i + 1);
			}
			if (xComponent != null && xComponent2 != null) {
				XALinear motion = new XALinear(1, XALinear.XALINEAR_ECSPEED, xComponent2.mX,
						xComponent2.mY, xComponent.mX, xComponent.mY, 9,
						(int) (2.1 * (xComponent.mX - xComponent2.mX) / 9),
						(int) (2.1 * (xComponent.mY - xComponent2.mY) / 9));
				// xComponent.setMotionFilter(motion);
				AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION,
						xComponent, motion, null);
				animInfoList.add(animInfo);
			} else if (xComponent != null && xComponent2 == null) {
				if (i % mColunmNums == mColunmNums - 1) {
					xComponent2 = getChildAt(i - 3 < 0 ? 0 : i - 3);
					XALinear motion = new XALinear(
							1,
							XALinear.XALINEAR_ECSPEED,
							xComponent2.mX,
							xComponent2.mY + xComponent2.getHeight(),
							xComponent.mX,
							xComponent.mY,
							9,
							(int) (2.1 * (xComponent.mX - xComponent2.mX) / 9),
							(int) (2.1 * (xComponent.mY - xComponent2.mY + xComponent2.getHeight()) / 9));
					// xComponent.setMotionFilter(motion);
					AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION,
							xComponent, motion, null);
					animInfoList.add(animInfo);
				} else {
					XALinear motion = new XALinear(1, XALinear.XALINEAR_ECSPEED, xComponent.mX
							+ xComponent.getWidth(), xComponent.mY, xComponent.mX, xComponent.mY,
							9, (int) (2.1 * xComponent.getWidth() / 9), 0);
					// xComponent.setMotionFilter(motion);
					AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION,
							xComponent, motion, null);
					animInfoList.add(animInfo);
				}
			}
		}
		if (!animInfoList.isEmpty()) {
			AnimationManager.getInstance(mActivity).attachBatchAnimations(MEREG_ITEM_TO_FOLDER,
					animInfoList, this, this);
		}
	}

	@Override
	public void onScrollStart() {
		postInvalidate(); // 对于需要深度变化的切屏特效（例如圆柱体），这句可以引起深度变化的动画
	}

	@Override
	public void onFlingStart() {
		if (!mIsVScroll) {
			if (mAutoTweakAntialiasing) {
				setDrawQuality(false);
			}
		}
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		if (mIsVScroll) {
			updateVisiblePositionV();
		} else {
			if (null != mIndicator) {
				int offset = mScreenScroller.getIndicatorOffset();
				mIndicator.setCurPosition(offset);
			}
		}
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurScreen = newScreen;

		if (mIndicator != null) {
			if (!(mIndicator.getMode() == MIndicator.MODE_H && mIndicator.isNeedChange())) {
				// 在横向滑动模式且是条状时不在这里更新指示器，不然会出现指示器位置跳动
				mIndicator.setCurIndex(mCurScreen);
			}
		}
		updateVisiblePositionH();
	}

	@Override
	public void onScrollFinish(int currentScreen) {

		if (!mIsVScroll) {
			if (mAutoTweakAntialiasing) {
				setDrawQuality(true);
			}
		}
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
	public int getScrollX() {
		return -mOffsetX;
	}

	@Override
	public int getScrollY() {
		return -mOffsetY;
	}

	/**
	 * 设置是否绘制边缘效果
	 * 
	 * @param fade
	 *            边缘淡化效果(使用功能表背景渐变绘制到图标上方）
	 * @param highlight
	 *            图标在边缘悬停时的高亮效果
	 */
	public void setGridEdgeEffectEnabled(boolean fade, boolean highlight) {
		mDrawFadeRect = fade;
		mDrawHighlightRect = highlight;
	}

	// @Override
	// public void onBCChange(int msgId, int param, Object object, List objects)
	// {
	// // TODO Auto-generated method stub
	// switch (msgId) {
	// case IDiyMsgIds.APPCORE_DATACHANGE: {
	// if (param == DataType.DATATYPE_EFFECTSETTING) {
	// setEffectSettings((EffectSettingInfo) object);
	// }
	// break;
	// }
	// default:
	// break;
	// }
	// }

	@Override
	public int getCellCount() {
		return Math.min(mAdapterCount, mStoreComponent.size());
	}

	@Override
	final public int getCellWidth() {
		return mCellWidth;
	}

	@Override
	final public int getCellHeight() {
		return mCellHeight;
	}

	@Override
	final public int getCellRow() {
		return mRowNums;
	}

	@Override
	final public int getCellCol() {
		return mColunmNums;
	}

	@Override
	public void drawGridCell(Canvas canvas, int index) {
		XComponent component = getChildAt(index);
		if (component != mDragComponent) {
			component.drawComponent(canvas);
		}
	}

	@Override
	public void drawGridCell(Canvas canvas, int index, int alpha) {

		XComponent component = getChildAt(index);
		if (component != mDragComponent) {
			if (component instanceof BaseAppIcon) {
				BaseAppIcon icon = (BaseAppIcon) component;
				int oldAlpha = icon.getAlpha();
				if (oldAlpha != alpha) {
					icon.setAlpha(alpha);
				}
				component.drawComponent(canvas);
				if (oldAlpha != alpha) {
					icon.setAlpha(oldAlpha);
				}
			} else {
				component.drawComponent(canvas);
			}

		}
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return mScreenScroller;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		mScreenScroller = scroller;

	}

	@Override
	public Scroller getScroller() {
		return mScroller;
	}

	@Override
	public void setScroller(Scroller scroller) {
		mScroller = scroller;
	}

	/**
	 * 设置横向滚屏时的图标动画类型
	 * 
	 * @param type
	 *            参考GridEffectorUtil类中的定义。
	 */
	public void setGridEffector(int type) {
		// mGridScreenEffector.setType(type);
		mCoupleScreenEffector.setType(type);

	}

	public void setCustRandomEffectors(int[] effects) {
		mCoupleScreenEffector.setAppIconCustomRandomEffects(effects);
	}

	public void setCycleMode(boolean cycle) {
		int scrollOrientation = AppFuncFrame.getDataHandler().getSlideDirection();
		if (scrollOrientation == FunAppSetting.SCREENMOVEHORIZONTAL && mCycleMode != cycle) {
			mCycleMode = cycle;
			ScreenScroller.setCycleMode(this, cycle);
			mCoupleScreenEffector.onAttachReserveEffector(this);
		} else if (scrollOrientation == FunAppSetting.SCREENMOVEVERTICAL
				&& mVerticalCycleMode != cycle) {
			mVerticalCycleMode = cycle;
			Scroller.setCycleMode(this, cycle, mActivity);
		}
	}

	void setDragComponent(XComponent component) {
		if (mDragComponent != component) {
			// if(mDragComponent != null){
			// ((BaseAppIcon)mDragComponent).setAppTextDrawingCacheEnable(true);
			// }
			if (mDragComponent != null && mDragComponent instanceof BaseAppIcon) { // 当前拖拽组件改变时，将上次拖拽的组件底色置为无色，防止突然锁屏导致上个组件出现底色
				((BaseAppIcon) mDragComponent).setInEdge(false);
			}

			if (component != null && component instanceof BaseAppIcon) {
				((BaseAppIcon) component).setAppTextDrawingCacheEnable(false);
			}
		}
		mDragComponent = component;
		if (component != null) {
			OrientationControl.keepCurrentOrientation(mActivity);
		}
	}

	protected void drawHorizontalScreen(Canvas canvas, int screen, int offset) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		int left = getWidth() * screen;
		int index = -1;
		index = mSingleScreenCells * screen;
		// 为了特效器能够在有搜索页的时候正常绘制所有程序图标在getCellCount()时多加了一屏的假数据，这里不是特效器绘制，要减去
		int cellcount = getCellCount();
		final int end = Math.min(cellcount, index + mSingleScreenCells);
		int lastX = 0, lastY = 0, cellX, cellY;
		canvas.save();
		canvas.translate(offset + mScreenScroller.getScroll(), 0);

		if (index > -1) {
			for (int i = 0; i < mRowNums && index < end; ++i) {
				for (int j = 0; j < mColunmNums && index < end; ++j, ++index) {
					XComponent component = getChildAt(index);
					if (component != mDragComponent) {
						cellX = component.mX - left;
						cellY = component.mY;
						canvas.translate(cellX - lastX, cellY - lastY);
						component.drawComponent(canvas);
						lastX = cellX;
						lastY = cellY;
					}
				}
			}
		}

		canvas.restore();
	}

	protected void setDrawQuality(boolean high) {
		int quality = high
				? GridScreenEffector.DRAW_QUALITY_HIGH
				: GridScreenEffector.DRAW_QUALITY_LOW;
		// mGridScreenEffector.setDrawQuality(quality);
		mCoupleScreenEffector.setDrawQuality(quality);
		int rcount = 0;
		rcount = getCellCount();
		for (int i = 0; i < rcount; ++i) {
			XComponent component = getChildAt(i);
			if (component != mDragComponent && component instanceof BaseAppIcon) {
				BaseAppIcon icon = (BaseAppIcon) component;
				icon.setIconDrawQuality(high);
			}
		}
	}

	@Override
	public void onFlingIntercepted() {
		if (mAutoTweakAntialiasing) {
			setDrawQuality(true);
		}
	}

	/**
	 * 设置是否在甩动屏幕时自动调整绘图反走样质量，以提高速度
	 * 
	 * @param auto
	 */
	public void setAutoTweakAntialiasing(boolean auto) {
		if (mAutoTweakAntialiasing && !auto) {
			setDrawQuality(true);
		}
		mAutoTweakAntialiasing = auto;
	}

	protected boolean isScrollFinished() {
		return mIsVScroll ? mScroller.isFinished() : mScreenScroller.isFinished();
	}

	public void clearDragStatus() {
		if (mDragComponent != null) {
			mDragComponent.setXY(mDragComponent.mX - mOffsetX, mDragComponent.mY - mOffsetY);
			((BaseAppIcon) mDragComponent).onLongClickUp(mDragComponentX, mDragComponentY, null,
					this);
			setDragComponent(null);
		}
		mDragComponentIndex = -1;
		mDragComponentTargetIndex = -1;
		mInDragStatus = false;
		// AppFuncHandler.getInstance().setHomeIconChangeDesk(
		// false);
	}

	@Override
	public void clickIndicatorItem(int index) {
		if (null != mScreenScroller) {
			// TODO:越界判断保护
			mScreenScroller.gotoScreen(index, -1, false);
		}
	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100) {
			mScreenScroller.setScrollPercent(percent);
		}
	}

	public void setVerticalEffect(int type) {
		if (mVerticalEffectorType == type) {
			return;
		}
		switch (type) {
			case NO_VERTICAL_EFFECTOR :
				mVerticalEffector = null;
				break;
			case WATERFALL_VERTICAL_EFFECTOR :
				mVerticalEffector = new WaterFallEffector();
				((WaterFallEffector) mVerticalEffector).setShadowColor(0);
				break;
			default :
				return;
		}
		mVerticalEffectorType = type;
		mScroller.setEffector(mVerticalEffector);
	}

	@Override
	public void onDraw(Canvas canvas, int clipTop, int clipBottom, int part) {
		final int position = mDragComponentTargetIndex;
		// clipTop = Math.max(0, clipTop);
		// clipBottom = Math.min(clipBottom, mScroller.getTotalHeight());
		final int cellHeight = getCellHeight();
		final int col = getCellCol();
		final int countClipTop = Math.max(0, clipTop);
		final int countClipBottom = Math.min(clipBottom, mScroller.getTotalHeight());
		int row1 = (countClipTop - (cellHeight - 1)) / cellHeight;
		int row2 = (countClipBottom + (cellHeight - 1)) / cellHeight;
		int begin = row1 * col, end = row2 * col;
		int lastX = 0, lastY = 0;

		for (int i = begin; i < end; i++) {
			// mStoreComponent可能在其他线程被修改，因此每次都需要重新取一下大小增加保护
			// 最后一个是指示器和搜索页，不用在此绘制
			int storeComponentSize = 0;
			storeComponentSize = mIsHasScroller ? mStoreComponent.size() - 1 : mStoreComponent
					.size();

			if (i != position && i >= 0 && i < storeComponentSize) {
				XComponent component = getChildAt(i);
				canvas.translate(component.mX - lastX, component.mY - lastY);
				component.drawComponent(canvas);
				lastX = component.mX;
				lastY = component.mY;
			}
		}

		if (mAdapterCount > mSingleScreenCells && mIsVScroll && mVerticalCycleMode) {
			if (part != VerticalListContainer.PART_DOWN
					&& ((begin == 0 && end == 0) || mFVisibleIndex == 0 || clipBottom < 0)) { // 竖向循环首部翻到底部时，需要继续绘制列表尾部，防止中间图标都消失。瀑布特效下需要在瀑布特效上部分绘制一屏加两行假图标
				if (mAdapterCount > mSingleScreenCells) {
					int tempY = 0;
					// int lastCount = (mAdapterCount % mColunmNums) == 0 ?
					// mColunmNums : (mAdapterCount % mColunmNums); // 最后一行的图标数

					int realEndIndex = mAdapterCount - 1;
					int top = (Math.abs(clipTop) + (cellHeight - 1)) / cellHeight; // 上部
					int realStartIndex = realEndIndex - top * col;
					if (realStartIndex < 0) {
						realStartIndex = 0;
					}
					if (part == VerticalListContainer.PART_UP) {
						int bottom = (int) (Math.abs(clipBottom) - (1.5 * cellHeight - 1))
								/ cellHeight; // 下部（下部扩大，则上部相应减小）
						if (bottom < 0) {
							bottom = 0;
						}
						realEndIndex = realEndIndex - bottom * col;
					}
					for (int j = realEndIndex/* mAdapterCount - 1 */; j >= realStartIndex
							&& j >= 0 && j >= 0; j--) {
						if (j != position) {
							XComponent component = getChildAt(j);
							if (component != null && component != mDragComponent) {
								tempY = component.mY - (int) ((mRows + 0.5) * cellHeight);
								canvas.translate(component.mX - lastX, tempY - lastY);
								component.drawComponent(canvas);
								lastX = component.mX;
								lastY = tempY;
							}
						}
					}
				}
			} else if (part != VerticalListContainer.PART_UP
					&& ((mLVisibleIndex + 1) == mAdapterCount || clipBottom > mScroller
							.getTotalHeight() + (2 * cellHeight))) { // 瀑布特效下需要在瀑布特效下部分绘制一屏加两行假图标
				// int storeComponentSize = mIsHasScroller ?
				// mStoreComponent.size()
				// - 1 : mStoreComponent.size();
				int tempY = 0;

				int realStartIndex = 0;
				int bottom = (clipBottom - mScroller.getTotalHeight() + (cellHeight - 1))
						/ cellHeight; // 下部
				int realEndIndex = bottom * col; // realEndIndex肯定大于0
				if (realEndIndex > mAdapterCount) {
					realEndIndex = mAdapterCount - 1;
				}
				if (part == VerticalListContainer.PART_DOWN) {
					int top = (clipTop - mScroller.getTotalHeight() - (cellHeight - 1))
							/ cellHeight; // 上部
					if (top < 0) {
						top = 0;
					}
					realStartIndex = top * col;
				}
				for (int j = realStartIndex; j <= realEndIndex && j < mAdapterCount; j++) {
					if (j != position) {
						XComponent component = getChildAt(j);
						if (component != null && component != mDragComponent) {
							tempY = component.mY + (int) ((mRows + 0.5) * cellHeight);
							canvas.translate(component.mX - lastX, tempY - lastY);
							component.drawComponent(canvas);
							lastX = component.mX;
							lastY = tempY;
						}
					}
				}
			}
		}
	}

	@Override
	public boolean contains(int x, int y) {
		return mX <= x && x < mX + mWidth && mY + mClipTop <= y && y < mY + mClipBottom;
	}

	public boolean isUsingWaterfallEffect() {
		return mIsVScroll && mVerticalEffectorType == WATERFALL_VERTICAL_EFFECTOR;
	}

	protected void applyVerticalEffect() {
		mClipTop = 0;
		mClipBottom = mHeight;
		mUpperHeight = 0;
		mUpperBottom = 0;
		mLowerHeight = 0;
		if (mIsVScroll && mVerticalEffectorType == WATERFALL_VERTICAL_EFFECTOR) {
			boolean isVertical = mUtils.isVertical();
			FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
			boolean showTabRow = handler.isShowTabRow();
			int fullHeight = XViewFrame.getInstance().getHeight(); // 整个功能表的高度
			mUpperHeight = fullHeight / 10;
			// int homeHeight = 0;
			// if(mFunAppSetting.getShowActionBar() == FunAppSetting.ON){
			// homeHeight = utils.getHomeComponentHeight();
			// }
			mLowerHeight = mUtils.isVertical()
					? mUtils.getDimensionPixelSize(AppFuncTabBasicContent.sBottomHeight_id)
					: fullHeight / 10;
			if (!showTabRow) {
				// mUpperBottom是图标整体向上偏移的高度，应该为内部的上空白大小
				// 暂时按比例估算
				mUpperBottom = fullHeight * 30 / 800;
				mUpperHeight -= mUpperBottom;
				setPosition(mX, mUpperHeight, mX + mWidth, fullHeight - mLowerHeight);
			} else if (!isVertical) {
				mUpperBottom = fullHeight * 30 / 800;
				mUpperHeight -= mUpperBottom;
				setPosition(mX, mUpperHeight, mX + mWidth, fullHeight - mLowerHeight);
			} else {
				if (!handler.isShowActionBar()) {
					setPosition(mX, mY, mX + mWidth, mY + fullHeight - getAbsY() - mLowerHeight);
				}
			}
			mClipTop = -mUpperHeight;
			mClipBottom = fullHeight - mUpperHeight;
		}
	}

	@Override
	public void drawScreen(Canvas canvas, int screen) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		canvas.save();
		int index = -1;

		index = mRowNums * mColunmNums * screen;
		// 为了特效器能够在有搜索页的时候正常绘制所有程序图标在getCellCount()时多加了一屏的假数据，这里不是特效器绘制，要减去
		int cellcount = getCellCount();

		final int end = Math.min(cellcount, index + mRowNums * mColunmNums);
		int lastX = 0, lastY = 0;

		if (index > -1) {
			for (int i = 0, cellY = mNewPaddingTop; i < mRowNums && index < end; ++i) {
				for (int j = 0, cellX = mNewPaddingLeft; j < mColunmNums && index < end; ++j, ++index) {
					canvas.translate(cellX - lastX, cellY - lastY);
					drawGridCell(canvas, index);
					lastX = cellX;
					lastY = cellY;
					cellX += mCellWidth;
				}
				cellY += mCellHeight;
			}
		}
		canvas.restore();
	}

	@Override
	public void drawScreen(Canvas canvas, int screen, int alpha) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		canvas.save();
		int index = -1;

		index = mRowNums * mColunmNums * screen;
		// 为了特效器能够在有搜索页的时候正常绘制所有程序图标在getCellCount()时多加了一屏的假数据，这里不是特效器绘制，要减去
		int cellcount = getCellCount();

		final int end = Math.min(cellcount, index + mRowNums * mColunmNums);
		int lastX = 0, lastY = 0;

		if (index > -1) {
			for (int i = 0, cellY = mNewPaddingTop; i < mRowNums && index < end; ++i) {
				for (int j = 0, cellX = mNewPaddingLeft; j < mColunmNums && index < end; ++j, ++index) {
					canvas.translate(cellX - lastX, cellY - lastY);
					drawGridCell(canvas, index, alpha);
					lastX = cellX;
					lastY = cellY;
					cellX += mCellWidth;
				}
				cellY += mCellHeight;
			}
		}
		canvas.restore();
	}

	// 图标定位，如果是文件夹外面的图标则直接定位到具体的图标，如果是文件夹内的图标则定位到文件夹
	public void locateAppByIntent(Intent intent) {
		// colin 图标定位(如果是在文件夹里则会调用两次
		if (intent == null) {
			return;
		}
		ArrayList<ApplicationIcon> folders = new ArrayList<ApplicationIcon>();
		ApplicationIcon icon = null;
		int size = mStoreComponent.size();
		for (int i = 0; i < size; i++) {
			XComponent temp = mStoreComponent.get(i);
			if (temp instanceof ApplicationIcon) {
				icon = (ApplicationIcon) temp;
				if (IItemType.ITEM_TYPE_APPLICATION == icon.getItemType()) {
					if (intent.equals(icon.getIntent())) {
						// 取消之前的焦点
						setItemFocus(false);
						mFocusedPosition = i;
						setItemFocus(true);
						handleFocusChange(true);
						if (mfolderintent) {
							mfolderintent = false;
						}
						// 点击定位到图标位置 colin local search-----
						// StatisticsFunTabData.addSearchStatistics(mContext,
						// StatisticsFunTabData.SEARCH_TYPE_LOCAL);
						return;
					}
				} else if (IItemType.ITEM_TYPE_USER_FOLDER == icon.getItemType()) {
					folders.add(icon);
				}
			}
		}

		int foldersSize = folders.size();
		for (int i = 0; i < foldersSize; i++) {
			icon = folders.get(i);
			FunFolderItemInfo info = null;
			if (icon.getInfo() instanceof FunFolderItemInfo) {
				info = (FunFolderItemInfo) icon.getInfo();
			}

			if (info != null) {
				ArrayList<FunAppItemInfo> apps = info.getFunAppItemInfos();
				if (apps != null) {
					for (FunAppItemInfo funAppItemInfo : apps) {
						if (intent.equals(funAppItemInfo.getIntent())) {
							// 取消之前的焦点,为匹配的文件夹设置焦点
							setItemFocus(false);
							mFocusedPosition = icon.getInfo().getIndex();
							handleFocusChange(false);
							mFocusedPosition = -1;
							// TODO:打开当前文件夹，在文件夹内设置焦点
							// 打开文件夹
							AppFuncHandler.getInstance().showFolder(info);
							// 在文件夹内进行定位操作
							AppFuncHandler.getInstance().locateAppInFolder(intent);
							mfolderintent = true;
//							DeliverMsgManager.getInstance().onChange(
//									AppFuncConstants.APPFOLDER_GRID,
//									AppFuncConstants.APP_GRID_LOCATE_ITEM,
//									funAppItemInfo.getAppItemInfo().mIntent);
							return;
						}
					}
					apps = null;
				}
			}
		}
		folders.clear();
		folders = null;
	}

	/**
	 * 用于处理文件夹打开时横竖屏切换后文件夹图标不在当前屏幕的极端情况
	 * 
	 * @param intent
	 */
	public void handlePositionForOpenFolder(Intent intent) {
		// colin 图标定位(如果是在文件夹里则会调用两次
		if (intent == null) {
			return;
		}
		ApplicationIcon icon = null;
		int size = mStoreComponent.size();
		for (int i = 0; i < size; i++) {
			XComponent temp = mStoreComponent.get(i);
			if (temp instanceof ApplicationIcon) {
				icon = (ApplicationIcon) temp;
				if (intent.equals(icon.getIntent())) {
					// 取消之前的焦点
					setItemFocus(false);
					mFocusedPosition = i;
					handleFocusChange(false);
					if (mfolderintent) {
						mfolderintent = false;
					}
					return;
				}
			}
		}
	}

	public void setGridId(int id) {
		mGridId = id;
	}

//	public static void setFirstLayout(boolean first) {
//		sFirstLayout = first;
//	}

	public void setShowAddButton(boolean show) {
		mShowAddButton = show;
	}

	/**
	 * 在文件夹模式根据触摸事件处理是否显示加号按钮
	 */
	// public void showAddButton(boolean show){
	// if (mShowAddButton) {
	// if (show) {
	// ((FolderAdapter)mAdapter).setShowAddButton(true);
	// mAddButtonHided = false;
	// } else {
	// ((FolderAdapter)mAdapter).setShowAddButton(false);
	// mAddButtonHided = true;
	// }
	// DeliverMsgManager.getInstance().onChange(
	// AppFuncConstants.APPFOLDER,
	// AppFuncConstants.LAYOUTFOLDERGRID, null);
	// }
	// }

	/**
	 * 把需要移动的组件插入到功能表组件列表的相应位置 同步为了解决主线程与MoveIconTask同时执行moveIcon()造成的同步问题
	 * 
	 * @param view
	 */
	public synchronized void removeAndInstertComponent(int target, XComponent component) {
		if (component != null) {
			removeComponent(component);
			try {
				insertComponent(target, component);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public void setIsMoveStop(boolean stop) {
		mIsMoveStop = stop;
	}

	public int getOffset() {
		if (!mIsVScroll) {
			return -mOffsetX;
		} else {
			return -mOffsetY;
		}
	}

	/**
	 * * 根据后台数据对象获取对应的图标对象
	 * 
	 * @return 与该 info匹配的图标
	 * @param info
	 */
	public ApplicationIcon getIconByInfo(Intent intent) {
		if (intent == null) {
			return null;
		}
		synchronized (this) {
			for (XComponent xc : mStoreComponent) {
				if (xc != null && xc instanceof ApplicationIcon) {
					ApplicationIcon icon = (ApplicationIcon) xc;
					Intent iconIntent = icon.getIntent();
					if (iconIntent != null
							&& intent.getAction().equalsIgnoreCase(iconIntent.getAction())) {
						return icon;
					}
				}
			}
			return null;
		}
	}

	/**
	 * 当删除图标时需要为删除的图标反注册消息接收器以避免内存泄露
	 * 
	 * @param component
	 */
	protected synchronized void unRegisterComponentMsgHandler(XComponent component) {
		if (component != null && component instanceof BaseAppIcon) {
			BaseAppIcon icon = (BaseAppIcon) component;
			icon.unRegister();
		}
	}

	public void reloadAllApps() {
		mAdapter.reloadApps();
	}

	// public void moveIconForFolder(ApplicationIcon icon){
	// if (icon==null) {
	// return;
	// }
	//
	// AppFuncMainView mainView = XViewFrame.getInstance().getAppFuncMainView();
	// if (!mainView.isFolderShow()) {
	//
	// }
	// AppFuncFolder mFolder = mainView.getCurrentFolder();
	// //判断当前界面能否
	// }

	protected int getWholeIconHeight() {
		int imgSize = Utilities.getStandardIconSize(mActivity);
		int imgHeight = (int) (imgSize * (1 + 0.194));
		int iconTextDst = mUtils.getStandardSize(2);

		boolean drawText = true;
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (handler != null) {
			drawText = (handler.getShowName() < FunAppSetting.APPNAMEVISIABLEYES) ? false : true;
		}
		int textHeight = 0;
		if (drawText) {
			textHeight = (int) (GoLauncher.getAppFontSize() * DrawUtils.sDensity);
		}

		// int textCtrlHeight = textHeight + mUtils.getStandardSize(4);
		// if (AppFuncFrame.getDataHandler()!= null){
		// int standard = AppFuncFrame.getDataHandler().getStandard();
		// if (standard != FunAppSetting.LINECOLUMNNUMXY_THICK &&
		// standard != FunAppSetting.LINECOLUMNNUMXY_MIDDLE_2){
		// mTextCtrlHeight = mTextHeight * 2 + mUtils.getStandardSize(4);
		// }
		// }
		int textCtrlHeight = textHeight * 2;
		return imgHeight + iconTextDst + textCtrlHeight;
	}

	/**
	 * 功能表带动画滚回功能表顶端
	 */
	public void scrollToFirst() {
		// mFocusedPosition = 0;
		if (mIsSupportScroll && mIsVScroll) {
			boolean isBotton = false;
			if (mLVisibleIndex >= mSingleScreenCells && mLVisibleIndex + 1 == mAdapterCount) {
				isBotton = true; // 超过第一屏并且位于最末端
			}
			if (isBotton) {
				int scroll = ((mRows - mRowNums) * mCellHeight) - 1;
				if (mScroller.getScroll() > scroll) {
					mScroller.setScroll(scroll - 10); // 如果超过第一屏并且位于最末端，需做特殊处理,否则滚不回顶端
				} else {
					mScroller.setScroll(mScroller.getScroll() - 10);
				}
			}
			mScroller.flingByScroll(-(3 * mScroller.getScroll()));
		} else {
			// onHScroll(true);
			mScreenScroller.gotoScreen(0, mScrollDuration, true);
		}
	}

	/**
	 * 满足条件则进行竖向循环滚动，返回true;不满足条件返回false
	 * 
	 * @return
	 */
	protected boolean doCycleScroll(int touchUpY) {
		if (mVerticalCycleMode && mIsVScroll && mAdapterCount > mSingleScreenCells) {
			int threshold = 0;
			int screenWidth = GoLauncher.getScreenWidth();
			if ((GoLauncher.isPortait() && screenWidth <= 320)
					|| (!GoLauncher.isPortait() && screenWidth <= 480)) { // 小屏手机由于较难滑，缩小阀值
				threshold = DrawUtils.dip2px(10);
			} else {
				threshold = DrawUtils.dip2px(30);
			}
			int scroll = mScroller.getScroll();
			int lastScroll = mScroller.getLastScroll();
			if ((touchUpY < mDownY) && scroll > lastScroll + threshold) { // 超过底部条件判断
				if (mScroller instanceof CycloidScroller) {
					((CycloidScroller) mScroller).scrollWithCycle((int) (0.5 * getCellHeight())
							+ mRealHeight - (mScroller.getScroll() - mScroller.getLastScroll()));
					return true;
				}
			} else if ((touchUpY > mDownY) && scroll < -threshold) { // 超过顶部条件判断
				if (mScroller instanceof CycloidScroller) {
					((CycloidScroller) mScroller).scrollWithCycle(-((int) (0.5 * getCellHeight())
							+ mRealHeight + mScroller.getScroll()));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 直接设置XBaseGrid当前滚动量
	 */
	public void setBaseGridScroll(int scroll) {
		mScroller.setScroll(scroll);
	}

	public void fixIconsPosition() {
		if (mAdapterCount > 0) {
			boolean showTwoLines = computeCellSize();
			int row = 0;
			int screen = mFVisibleIndex / mSingleScreenCells;
			for (int i = mFVisibleIndex; i <= mLVisibleIndex; i++) {
				XComponent c = getChildAt(i);
				if (c instanceof BaseAppIcon) {
					int idx = i - screen * mSingleScreenCells;
					BaseAppIcon icon = (BaseAppIcon) c;
					icon.setShowTwoLines(showTwoLines);
					icon.mX = mPaddingLeft + screen * mWidth + (idx % mColunmNums) * mCellWidth;
					if (idx > 0 && idx % mColunmNums == 0) {
						row++;
					}
					icon.mY = mPaddingTop + row * mCellHeight;
				}
			}

			initIndicator(mIndicatorPos);
		}
		mClipTop = 0;;
		mClipBottom = mHeight;
	}

	@Override
	public void onStart(int what, Object[] params) {
	}

	@Override
	public void onFinish(int what, Object[] params) {
		switch (what) {
			case MEREG_ITEM_TO_FOLDER :
				if (mNeedOpenFolder) {
					mNeedOpenFolder = false;
					// 再刷新下功能表的baseGrid
					AppFuncHandler.getInstance().getCurrentGrid().requestLayout(false);
					AppFuncHandler.getInstance().showFolder(mFolderInfo);
				}
				break;

			default :
				break;
		}
	}

	@Override
	public void keepCurrentOrientation() {
		OrientationControl.keepCurrentOrientation(mActivity);
	}

	@Override
	public void resetOrientation() {
		if (mDragComponent == null) {
			OrientationControl.setOrientation(mActivity);
		}
	}

	public int getCurrentRow() {
		return mCurRow;
	}

	public int getScreenCount() {
		return mScreen;
	}

	public int getCurrentScreen() {
		return mCurScreen;
	}
	/**
	 * <br>功能简述: 修正因滚动过程中锁屏，横竖屏切换等导致的卡在中间的问题
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void fixScoller() {
		// 在组件被掩藏的时候将横向或竖向滚动器还原下正常状态，防止突然锁屏的时候滚动器卡在中间
		if (mIsVScroll && mScroller != null) { // 竖向滚动处理
			int scroll = mScroller.getScroll();
			int lastScroll = mScroller.getLastScroll();
			if (scroll < 0) {
				mScroller.setScroll(0);
			} else if (scroll > mScroller.getLastScroll()) {
				mScroller.setScroll(lastScroll);
			}
		} else if (!mIsVScroll && mScreenScroller != null) { // 横向滚动处理
			mScreenScroller.gotoScreen(mCurScreen, mScrollDuration, false);
		}
		mIsScrolling = false; // 必须设为false，否则会出现跳动现象
	}

	/**
	 * 从功能表的grid中找到文件夹的icon
	 * @param info
	 * @return
	 */
	public ApplicationIcon findTargetFolderIcon(FunFolderItemInfo info) {
		// TMD保证获取功能表的grid
		XBaseGrid curGrid = AppFuncHandler.getInstance().getCurrentGrid();
		int count = curGrid.getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent child = curGrid.getChildAt(i);
			if (child instanceof ApplicationIcon) {
				ApplicationIcon icon = (ApplicationIcon) child;
				if (icon.isFolder() && icon.getInfo() == info) {
					mFolderIndex = i;
					mFolderIndexIcon = icon;
					return icon;
				}
			}
		}
		return null;
	}

	public void removeDragComponent() {
		if (mDragComponent != null) {
			removeComponent(mDragComponent);
		}
		setDragComponent(null);
	}

	public void onCreateNewFolder(FunAppItemInfo info) {
		int idx = 0; //新建的文件夹放在第一页第一个位置上
		FunControler controler = null;
		controler = AppFuncFrame.getFunControler();
		boolean isCreated = false;
		FunFolderItemInfo folderInfo = null;
		try {
			controler.getFunDataModel().beginTransaction();
			String folderName = mActivity.getResources().getString(R.string.folder_name);
			folderInfo = controler.addFunFolderItemInfo(idx, folderName, null);
			if (folderInfo != null) {
				controler.moveFunAppItemToFolder(folderInfo, folderInfo.getSize(), info);
				isCreated = true;
			}
			controler.getFunDataModel().setTransactionSuccessful();
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
			return;
		} finally {
			controler.getFunDataModel().endTransaction();
		}
		if (isCreated) {
			scrollToFirst();
			requestLayout();
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.APPDRAWER_ENTER_FOLDER_EDIT_MODE, -1, null, null);
			if (folderInfo != null) {
				Intent intent = new Intent(mActivity, AppFuncModifyFolderActivity.class);
				intent.putExtra(AppFuncConstants.FOLDER_ID, folderInfo.getFolderId());
				intent.putExtra(AppFuncConstants.IS_TEMP_FOLDER, true);
//				AppFuncFolder folder = AppFuncFolder.getInstance();
//				// 编辑模式下，拖动到上方工具条新建文件夹时，暂不清除FolderInfo。
//				if (folder != null && !folder.isInEditMode()) {
//					folder.removeFolderInfo();
//				}
				int requestCode = IRequestCodeIds.REQUEST_MODIFY_APPDRAWER_FOLDER;
				mActivity.startActivityForResult(intent, requestCode);
			}
		}
	}
	
	public void onAddItemToNormalFolder(FunFolderItemInfo info) {
		mDragComponentTargetIcon = (ApplicationIcon) mDragComponent;
		findTargetFolderIcon(info);
	}

	public void onAddItemToSpecialFolder(FunFolderItemInfo folderInfo, FunAppItemInfo info) {
		int idx = 0; //新建的文件夹放在第一页第一个位置上
		FunControler controler = null;
		controler = AppFuncFrame.getFunControler();
		boolean flag = false;
		FunFolderItemInfo newFolderInfo = null;
		try {
			controler.getFunDataModel().beginTransaction();
			String folderName = folderInfo.getTitle();
			newFolderInfo = controler.addFunFolderItemInfo(idx, folderName, null);
			if (newFolderInfo != null) {
				controler.moveFunAppItemToFolder(newFolderInfo, newFolderInfo.getSize(), info);
				flag = true;
			}
			controler.getFunDataModel().setTransactionSuccessful();
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
			return;
		} finally {
			controler.getFunDataModel().endTransaction();
		}

		if (flag) {
			if (XViewFrame.getInstance().getAppFuncMainView().isFolderShow()) {
				// 延后到文件夹关闭后刷新，给mainView一个标识，然后收起文件夹后时处理
				XViewFrame.getInstance().getAppFuncMainView().requestLayout();
			} else {
				requestLayout(false);
			}
			DeliverMsgManager.getInstance().onChange(
					AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
					AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR_SEPCIAL_FOLDER,
					new SpecialFolderItem(folderInfo.getFolderType(), newFolderInfo));
			scrollToFirst();
		}
	}
	
	@Override
	public void requestLayout() {
		requestLayout(true);
	}

	public void requestLayout(boolean needRefreshFolderBar) {
		super.requestLayout();
		if (needRefreshFolderBar && isInDragStatus()) {
			DeliverMsgManager.getInstance().onChange(
					AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
					AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR, null);
		}
	}
	
	/**
	 * 
	 * @param idx 目标图标的下标
	 * @return true表示需要滚动，false表示不需要执行滚动
	 */
	public boolean scrolltoTargetScreen(int idx) {
		if (mIsSupportScroll) {
			if (mIsVScroll) {
				
			} else {
				int curIconPerScereen = mColunmNums * mRowNums;
				int targetScreen = idx / curIconPerScereen;
				if (targetScreen == mCurScreen) {
					return false;
				}
				mScreenScroller.gotoScreen(targetScreen, mScrollDuration, true);
			}
			return true;
		} else {
			return false; 
		}
	}
	
	private void iconMovingPlus() {
		synchronized (mLock) {
			int tmp = mIconMoveing;
			++tmp;
			mIconMoveing = tmp;
		}
	}
	
	private void iconMovingReduce() {
		synchronized (mLock) {
			if (mIconMoveing > 0) {
				int tmp = mIconMoveing;
				--tmp;
				mIconMoveing = tmp;
			}
		}
	}
}