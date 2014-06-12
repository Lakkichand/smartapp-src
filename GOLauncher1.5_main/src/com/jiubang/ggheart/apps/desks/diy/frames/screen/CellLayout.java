/*
 * Copyright (C) 2008 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import mobi.intuitit.android.widget.WidgetCellLayout;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.CompatibleUtil;
import com.go.util.Utilities;
import com.go.util.animation.InterruptibleInOutAnimator;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.HolographicOutlineHelper;
import com.jiubang.ggheart.apps.desks.diy.DrawSelectedListener;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SenseWorkspace;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderIcon.FolderRingAnimator;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.LockerThemeTab;
import com.jiubang.ggheart.apps.gowidget.AbsWidgetInfo;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * 单个屏幕布局
 * 
 * @author luopeihuan
 * 
 */
public class CellLayout extends WidgetCellLayout implements DrawSelectedListener {
	public static boolean sPortrait; 
	public static int sCellWidth; //每一个cell的宽度
	static int sCellHeight; //每一个cell的高度
	static int sCellRealWidth; //动态计算的实际宽度
	static int sCellRealHeight; //动态计算的实际高度

	
	
	public static int sLongAxisStartPadding; //cell距离父view CellLayout左边距
	public static int sLongAxisEndPadding; //cell距离父view CellLayout右边距

	public static int sLongAxisStartNoIndicatorPadding;
	// public static int mLongAxisEndNoIndicatorPadding;

	public static int sShortAxisStartPadding; //cell距离父view CellLayout上边距
	public static int sShortAxisEndPadding; //cell距离父view CellLayout下边距

	public static int sWidgetCustomPadding;

	int mShortAxisCells; //x轴方向的cell个数
	int mLongAxisCells; //y轴方向的cell个数

	public static float sWidthGap;
	public static float sHeightGap;

	private final Rect mRect = new Rect();
	private final CellInfo mCellInfo = new CellInfo();

	int[] mCellXY = new int[2];

	boolean[][] mOccupied; // 标记每个cell是否被占用

	private RectF mDragRect = new RectF();

	private boolean mDirtyTag;
	private boolean mLastDownOnOccupiedCell = false;

	// 行列数
	static int sRows = 4;
	static int sColumns = 4;

	static boolean sAutoStretch = false;

	boolean mChildrenDrawingCacheBuilt;
	boolean mChildrenDrawnWithCache;

	private final static int FOLDER_ZOOM_OUT_DURATION = 300; // 合并文件夹动画缩小动画持续时间
	final static int DRAW_STATUS_NORMAL = 1; // 正常状态
	final static int DRAW_STATUS_REPLACE = 2; // 挤压动画过程
	final static int DRAW_STATUS_MERGE_FOLDER = 3; // 合并文件夹过程
	final static int DRAW_STATUS_INTO_FOLDER_ZOOMOUT = 4; // 图标放入文件夹缩小动画过程
	private int mDrawStatus = DRAW_STATUS_NORMAL; // 绘制状态
	private float mStartTime = 0; // 保存每次挤压动画开始时间点
	//	private View mMergeFolderChildView; // 合并文件夹的目标网格原来childView

	/**
	 * 合并完文件夹后的缩小动画变量
	 */
	Drawable mMergeFolderZoomoutDrawable; // 合并完文件夹后的缩小动画的drawable
	private Rect mSrcZoomoutRect; // 合并文件夹后的缩小动画的开始矩形
	private Rect mTargetZoomoutRect; // 合并文件夹缩小动画的目标矩形

	// 在dispatchDraw的时候是否tryCatch的标识
	private boolean mNeedToCatch = false;

	// folder打开、关闭动画
	private int mFolderTop; // folder打开在celllayout的上边界
	private int mFolderBottom; // folder打开在celllayout的下边界
	private int mClipLine; // 裁剪线

//	private int mWidgetLayerType = ViewCompat.LAYER_TYPE_NONE;
	private int mLayerType = Machine.LAYER_TYPE_SOFTWARE;

	public static final int STATE_NORMAL_CONTENT = 0;
	public static final int STATE_BLANK_CONTENT = 1;
	protected int mState = STATE_NORMAL_CONTENT;
	private float mLayoutScale = 1.0f;
	private Drawable mBackground; // 背景
	private Drawable mAddDrawable; // 十字架
	private Drawable mNormalBackground; // 普通状态下的背景
	private Drawable mFullBackground; // 满屏之后的背景
	private Drawable mLightBackground; // 点击时的高亮背景
	private Rect mBgRect = new Rect(); // 屏幕九切图背景的边距
	private boolean mDrawBackground = false;
	public int mBackgroudAlpha = 255;
	// 绘制网格的行数调整常量
	protected static final int CROSS_LINE_OFFSET = 1;
	private Drawable mGridCross; // 网格的十字架图标
	public static final int DRAW_CROSS_MASK = 0x1;
	public static final int DRAW_CURR_EDGE_MASK = 0x10;
	// 绘制额外内容的标记
	protected int mDrawExtraFlag = 0;
	// 边距的变化值
	private static int sTopExtra = 0;
	private static int sBottomExtra = 0;
	private static int sRightExtra = 0;
	// 网格十字架的可见度
	private float mCrosshairsVisibility = 1.0f;
	private final PointF mTmpPointF = new PointF();
	private final Point mDragCenter = new Point();

	private int mCoverBgOffset = 10;
	// These arrays are used to implement the drag visualization on x-large screens.
    // They are used as circular arrays, indexed by mDragOutlineCurrent.
    private Rect[] mDragOutlines = new Rect[4];
    private float[] mDragOutlineAlphas = new float[mDragOutlines.length];
    private InterruptibleInOutAnimator[] mDragOutlineAnims = new InterruptibleInOutAnimator[mDragOutlines.length];
    // Used as an index into the above 3 arrays; indicates which is the most current value.
    private int mDragOutlineCurrent = 0;
    private DecelerateInterpolator mEaseOutInterpolator;
    private final Paint mDragOutlinePaint = new Paint();
    
	/**
	 * 
	 * @param context
	 *            context
	 */
	public CellLayout(Context context) {
		this(context, null);
	}

	/**
	 * 
	 * @param context
	 *            context
	 * @param attrs
	 *            xml属性集
	 */
	public CellLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * 
	 * @param context
	 *            context
	 * @param attrs
	 *            xml属性集
	 * @param defStyle
	 *            默认属性
	 */
	public CellLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyle, 0);
		setCellWidth(a.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10));
		setCellHeight(a.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10));

		sLongAxisStartPadding = a.getDimensionPixelSize(
				R.styleable.CellLayout_longAxisStartPadding, 10);
		sLongAxisEndPadding = a
				.getDimensionPixelSize(R.styleable.CellLayout_longAxisEndPadding, 10);
		sShortAxisStartPadding = a.getDimensionPixelSize(
				R.styleable.CellLayout_shortAxisStartPadding, 10);
		sShortAxisEndPadding = a.getDimensionPixelSize(R.styleable.CellLayout_shortAxisEndPadding,
				10);

		sLongAxisStartNoIndicatorPadding = a.getDimensionPixelSize(
				R.styleable.CellLayout_longAxisEndPadding, 5);

		sWidgetCustomPadding = (int) getResources()
				.getDimension(R.dimen.cell_widget_custom_padding);

		// if(mLargeIcon)
		// {
		// mLongAxisStartPadding = mLongAxisStartNoIndicatorPadding;
		// }
		a.recycle();
		setAlwaysDrawnWithCacheEnabled(false);

		// 设置投影的颜色
		mDragOutlineColor = context.getResources().getColor(R.color.icon_outline_color);
		GoSettingControler settingControler = GOLauncherApp.getSettingControler();
		final DesktopSettingInfo info = settingControler.getDesktopSettingInfo();
		if (info.mCustomAppBg) {
			mDragOutlineColor = info.mPressColor;
		}
		initOutlineAnims();
		mState = STATE_NORMAL_CONTENT;
		initBackgroundAndPadding();
		initAppBoxInnerHeight();
		mReorderHintAnimationMagnitude = REORDER_HINT_MAGNITUDE * Utilities.getStandardIconSize(getContext());
		mPreviousReorderDirection[0] = INVALID_DIRECTION;
		mPreviousReorderDirection[1] = INVALID_DIRECTION;
	}

	// 初始化投影边框的动画
	private void initOutlineAnims() {
		mEaseOutInterpolator = new DecelerateInterpolator(2.5f); // Quint ease out
        for (int i = 0; i < mDragOutlines.length; i++) {
            mDragOutlines[i] = new Rect(-1, -1, -1, -1);
        }
        Resources res = getResources();
		final int duration = res.getInteger(R.integer.config_dragOutlineFadeTime);
		final float fromAlphaValue = 0;
		final float toAlphaValue = (float) res.getInteger(R.integer.config_dragOutlineMaxAlpha);
		Arrays.fill(mDragOutlineAlphas, fromAlphaValue);
		for (int i = 0; i < mDragOutlineAnims.length; i++) {
			final InterruptibleInOutAnimator anim = new InterruptibleInOutAnimator(duration,
					fromAlphaValue, toAlphaValue);
			anim.getAnimator().setInterpolator(mEaseOutInterpolator);
			final int thisIndex = i;
			anim.getAnimator().addUpdateListener(new AnimatorUpdateListener() {
				public void onAnimationUpdate(ValueAnimator animation) {
//					final Bitmap outline = (Bitmap) anim.getTag();
					// If an animation is started and then stopped very quickly, we can still
					// get spurious updates we've cleared the tag. Guard against this.
					if (mOutLineBitmap == null || mOutLineBitmap.isRecycled()) {
//						@SuppressWarnings("all")
						// suppress dead code warning
//						final boolean debug = false;
//						if (debug) {
//							Object val = animation.getAnimatedValue();
//						}
						// Try to prevent it from continuing to run
						animation.cancel();
					} else {
						mDragOutlineAlphas[thisIndex] = (Float) animation.getAnimatedValue();
						CellLayout.this.invalidate(mDragOutlines[thisIndex]);
					}
				}
			});
			// The animation holds a reference to the drag outline bitmap as long is it's
			// running. This way the bitmap can be GCed when the animations are complete.
			anim.getAnimator().addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if ((Float) ((ValueAnimator) animation).getAnimatedValue() == 0f) {
						anim.setTag(null);
					}
				}
			});
			mDragOutlineAnims[i] = anim;
		}
	} // end initOutlineAnims
	
	/**
	 * 响应“+”号屏生成普通屏
	 */
	private void clickAciton() {
		ViewGroup parent = (ViewGroup) getParent();
		if (parent != null && parent instanceof Workspace) {
			blankToNormal();
			// 屏幕的个数限制为9
			if (parent.getChildCount() < SenseWorkspace.MAX_CARD_NUMS) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_ADD_BLANK, -1, null, null);
			}
		}
	}

	private boolean mIsClickBlank; // 标志+卡片外的点击无效。
	// 错误：ADT - 1952 添加界面，切换到添加屏，点击下面菜单Tab栏上的空白区域，会响应成点击添加屏
	// 原因：缩小后的+卡片响应区域是整个屏幕
	// 修改：根据点击位置判断是否在卡片范围内点击。
	// 测试：如bug描述

	public void setBlank(int state) {
		mState = state;
		initBackgroundAndPadding();
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsClickBlank) {
					clickAciton();
				}
			}
		});
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final float x = event.getX();
				final float y = event.getY();
				if (x < 0 || x > (getWidth() - getRightPadding()) * mLayoutScale
						|| y < getTopPadding() * mLayoutScale
						|| y > (getHeight() - getBottomPadding()) * mLayoutScale) {
					mIsClickBlank = false;
					// 问题：ADT-5232 点击添加界面的+号屏，绿色标示一闪而过
					// 修改：down->move时，变换绿色背景，超出cellLayout范围时或者手离开时，恢复
					if (mBackground != mNormalBackground) {
						mBackground = mNormalBackground;
						invalidate();
					}
					return false;
				}
				mIsClickBlank = true;
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN : {
						if (mBackground != mLightBackground) {
							mBackground = mLightBackground;
							invalidate();
						}
					}
						break;
					case MotionEvent.ACTION_MOVE :
						// 问题：ADT-5232 点击添加界面的+号屏，绿色标示一闪而过
						// 修改：down->move时，变换绿色背景，超出cellLayout范围时或者手离开时，恢复
						if (mBackground != mLightBackground) {
							mBackground = mLightBackground;
							invalidate();
						}
						break;
					case MotionEvent.ACTION_CANCEL : {
						if (mBackground != mNormalBackground) {
							mBackground = mNormalBackground;
							invalidate();
						}
					}
						break;
					case MotionEvent.ACTION_UP : {
						if (mBackground == mLightBackground) {
							mBackground = mNormalBackground;
							clickAciton();
						}
					}
						break;
				}
				return false;
			}
		});

	}
	//初始化正常状况的屏幕
	public void blankToNormal() {
		mState = STATE_NORMAL_CONTENT;
		initBackgroundAndPadding();
		setOnClickListener(null);
		setOnTouchListener(null);
		// 转化为正常的cellLayout
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_BLANK_TO_NORMAL,
				-1, null, null);
		invalidate();
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();

		// Cancel long press for all children
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			child.cancelLongPress();
		}
	}

	int getCountX() {
		return sPortrait ? mShortAxisCells : mLongAxisCells;
	}

	int getCountY() {
		return sPortrait ? mLongAxisCells : mShortAxisCells;
	}

	static void setTopExtra(int top) {
		sTopExtra = top;
	}

	static void setBottomExtra(int bottom) {
		sBottomExtra = bottom;
	}

	static void setRightExtra(int right) {
		sRightExtra = right;
	}

	static int getLeftPadding() {
		return sPortrait ? sShortAxisStartPadding : sLongAxisStartPadding;
	}

	static int getTopPadding() {
		return sPortrait ? sLongAxisStartPadding + sTopExtra : sShortAxisStartPadding + sTopExtra;
	}

	static int getRightPadding() {
		return sPortrait ? sShortAxisEndPadding : sLongAxisEndPadding + sRightExtra;
	}

	static int getBottomPadding() {
		return sPortrait ? sLongAxisEndPadding + sBottomExtra : sShortAxisEndPadding + sBottomExtra;
	}

	static int getRows() {
		return sRows;
	}

	static int getColumns() {
		return sColumns;
	}

	static void setRows(int row) {
		sRows = row;
	}

	static void setColums(int col) {
		sColumns = col;
	}

	// 桌面添加文件夹,生成的一张绘制了除新添加文件夹之外所有图标的一张图片
	public Drawable mAlphaScreen;
	// 桌面添加文件夹,当前新添加的文件夹对象
	public BubbleTextView mNewFolderView;

	// public NinePatchDrawable mAddFolderCardBg;
	// 桌面添加新文件夹时，与当前页相邻的页面需要半透明
	public boolean mCoverWithBg;

	/***
	 * 桌面添加文件夹时，当前celllayout需要半透明显示
	 * 
	 * @param alphaScreen
	 *            桌面添加文件夹,生成的一张绘制了除新添加文件夹之外所有图标的一张图片
	 * @param currFolderView
	 *            桌面添加文件夹,当前新添加的文件夹对象
	 */
	public void startNewFolder(Drawable alphaScreen, NinePatchDrawable addFolderCardBg,
			BubbleTextView newFolderView) {
		mAlphaScreen = alphaScreen;
		// mAddFolderCardBg = addFolderCardBg;
		mNewFolderView = newFolderView;
	}

	public void setCoverWithBg(boolean coverWithBg) {
		mCoverWithBg = coverWithBg;
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		canvas.save();
		if (mLayoutScale < 1.0f) {
			canvas.scale(mLayoutScale, mLayoutScale);
		}

		drawBackground(canvas);
		// 桌面添加新文件夹时，页面需要半透明（增加对home键的处理）
		if (mAlphaScreen != null && mLayoutScale < 1.0f) {
			dispatchDrawNewFolder(canvas);
			drawBorderAndCross(canvas);
			canvas.restore();
			return;
		}

		drawBorderAndCross(canvas);
		drawSelectedBorder(canvas);
		switch (mDrawStatus) {
			case DRAW_STATUS_NORMAL :
				drawFolderRing(canvas);
				dispatchDrawNormal(canvas);
				break;

			case DRAW_STATUS_INTO_FOLDER_ZOOMOUT :
				dispatchDrawIntoFolderZoomOut(canvas, getCurrentTime());
				break;

			default :
				break;
		}
		// 桌面添加新文件夹时，与当前页相邻的页面需要半透明
		if (mCoverWithBg) {
			dispatchDrawCoverBg(canvas);
		}
		canvas.restore();
		// 十字架的绘制不缩放，所以缩放恢复后处理
		drawCenterCross(canvas);
	}

	/***
	 * 绘制添加新文件夹时的半透明页面
	 * @param canvas
	 */
	private void dispatchDrawNewFolder(Canvas canvas) {
		if (mLayoutScale >= 1.0f) {
			return;
		}
		canvas.save();
		mAlphaScreen.setBounds(0, 0, getWidth(), getHeight());
		mAlphaScreen.draw(canvas);
		canvas.restore();

		final long drawingTime = getDrawingTime();
		if (mNewFolderView != null && mNewFolderView.getVisibility() == View.VISIBLE) {
			toDrawChild(canvas, mNewFolderView, drawingTime);
		}
	}

	/***
	 * 添加新文件夹时，当前页面左右两侧的页面半透明
	 */
	private void dispatchDrawCoverBg(Canvas canvas) {
		if (mLayoutScale >= 1.0f) {
			return;
		}
		canvas.save();
		if (mGridCross != null) {
			mCoverBgOffset = (int) (mGridCross.getIntrinsicWidth() * mLayoutScale / 2);
		}
		canvas.translate(-mBgRect.left - getLeftPadding(), -mBgRect.top + getTopPadding());
		canvas.clipRect(mCoverBgOffset, mCoverBgOffset, mBgRect.left + getWidth() + mBgRect.right
				- getLeftPadding() - getRightPadding() - mCoverBgOffset, getHeight() + mBgRect.top
				+ mBgRect.bottom - getTopPadding() - getBottomPadding() - 2 * mCoverBgOffset);
		canvas.drawColor(0x6affffff);
		canvas.restore();
	}

	/**
	 * 获取当前的时间，配合动画使用
	 * 
	 * @return
	 */
	private long getCurrentTime() {
		int currentTime = 0;
		if (mStartTime == 0) {
			mStartTime = SystemClock.uptimeMillis();
		} else {
			currentTime = (int) (SystemClock.uptimeMillis() - mStartTime);
		}
		return currentTime;
	}
	/**
	 * 正常状态的绘制
	 * @param canvas
	 */
	private void dispatchDrawNormal(Canvas canvas) {
		final int count = getChildCount();
		final long drawingTime = getDrawingTime();
		for (int i = 0; i < count; i++) {
			View childView = getChildAt(i);
			if (childView != null && childView.getVisibility() == VISIBLE
					&& childView.getResources() != null
					&& childView.getResources().getDisplayMetrics() != null) {
				toDrawChild(canvas, childView, drawingTime);
			}
		}
	}

	private void toDrawChild(Canvas canvas, View childView, long drawingTime) {
		// if (mNeedToCatch) {
		// 全部都进行try-catch，防止absList的越界类型的bug出现 -by Yugi 2012-11-5
		try {
			drawChild(canvas, childView, drawingTime);
		} catch (Exception e) {
			// TODO: handle exception
		}
		// } else {
		// drawChild(canvas, childView, drawingTime);
		// }
	}

	/**
	 * 放入文件夹的图标缩小动画
	 * 
	 * @param canvas
	 */
	private void dispatchDrawIntoFolderZoomOut(Canvas canvas, long currentTime) {
		dispatchDrawNormal(canvas);
		final float time = Math.min(1, currentTime / (float) FOLDER_ZOOM_OUT_DURATION);
		if (time == 0) {
			clearVisualizeDropLocation();
		}
		if (null != mSrcZoomoutRect && null != mTargetZoomoutRect
				&& null != mMergeFolderZoomoutDrawable) {
			final int scaleWidth = (int) ScreenUtils.easeOut(mSrcZoomoutRect.width(),
					mTargetZoomoutRect.width(), time);
			final int scaleHeight = (int) ScreenUtils.easeOut(mSrcZoomoutRect.height(),
					mTargetZoomoutRect.height(), time);
			final int left = (int) ScreenUtils.easeOut(mSrcZoomoutRect.left,
					mTargetZoomoutRect.left, time);
			final int top = (int) ScreenUtils.easeOut(mSrcZoomoutRect.top, mTargetZoomoutRect.top,
					time);
			mMergeFolderZoomoutDrawable
					.setBounds(left, top, left + scaleWidth, top + scaleHeight);
			mMergeFolderZoomoutDrawable.draw(canvas);
		}

		if (currentTime > FOLDER_ZOOM_OUT_DURATION) {
			handleIntoFolderZoomoutTimeout();
		} else {
			postInvalidate();
		}

	}
	/**
	 * 处理判断放入图标放入文件夹超时后进行恢复
	 */
	private void handleIntoFolderZoomoutTimeout() {
		setStatusNormal();
		mMergeFolderZoomoutDrawable = null;
		mSrcZoomoutRect = null;
		mTargetZoomoutRect = null;
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (child == null || child.getParent() != null) {
			return;
		}
		final LayoutParams cellParams = (LayoutParams) params;
		cellParams.regenerateId = true;
		super.addView(child, index, params);
		// final ItemInfo itemInfo = (ItemInfo)child.getTag();
		// if(itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET)
		// {
		// Machine.setHardwareAccelerated(child, Machine.LAYER_TYPE_SOFTWARE);
		// }
	}

	/**
	 * 替换视图
	 */
	public void replaceView(View oldChild, View newChild) {
		if (oldChild == null || oldChild.getParent() != this || newChild == null) {
			return;
		}

		CellLayout.LayoutParams oldParams = (CellLayout.LayoutParams) oldChild.getLayoutParams();
		int cellX = 1, cellY = 1, spanX = 1, spanY = 1;
		if (oldParams != null) {
			cellX = oldParams.cellX;
			cellY = oldParams.cellY;
			spanX = oldParams.cellHSpan;
			spanY = oldParams.cellVSpan;
		}

		removeView(oldChild);
		Object tag = oldChild.getTag();
		newChild.setTag(tag);
		oldChild.setTag(null);

		Workspace workspace = (Workspace) getParent();
		if (workspace != null) {
			int screenIndex = workspace.indexOfChild(this);
			workspace.addInScreen(newChild, screenIndex, cellX, cellY, spanX, spanY, true);
			workspace = null;
		}
	}

	@Override
	public void requestChildFocus(View child, View focused) {
		super.requestChildFocus(child, focused);
		if (child != null) {
			Rect r = new Rect();
			child.getDrawingRect(r);
			requestRectangleOnScreen(r);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mCellInfo.screen = ((ViewGroup) getParent()).indexOfChild(this);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mState == STATE_BLANK_CONTENT) {
			return true;
		}

		final int action = ev.getAction();
		final CellInfo cellInfo = mCellInfo;

		if (action == MotionEvent.ACTION_DOWN) {
			final Rect frame = mRect;
			int x = (int) ev.getX() + getScrollX();
			int y = (int) ev.getY() + getScrollY();

			if (mLayoutScale < 1.0f && mLayoutScale > 0) {
				x = x + Workspace.sPageSpacingX;
				y = y + Workspace.sPageSpacingY;
				float[] realXY = new float[2];
				Workspace.virtualPointToReal(x, y, realXY);
				x = (int) realXY[0];
				y = (int) realXY[1];
			}

			final int count = getChildCount();

			boolean found = false;
			for (int i = count - 1; i >= 0; i--) {
				final View child = getChildAt(i);

				if ((child.getVisibility()) == VISIBLE || child.getAnimation() != null) {
					child.getHitRect(frame);
					if (frame.contains(x, y)) {
						final LayoutParams lp = (LayoutParams) child.getLayoutParams();
						cellInfo.cell = child;
						cellInfo.cellX = lp.cellX;
						cellInfo.cellY = lp.cellY;
						cellInfo.spanX = lp.cellHSpan;
						cellInfo.spanY = lp.cellVSpan;
						cellInfo.valid = true;
						found = true;
						mDirtyTag = false;
						break;
					}
				}
			}

			mLastDownOnOccupiedCell = found;

			if (!found) {
				int cellXY[] = mCellXY;
				pointToCellExact(x, y, cellXY);

				// ----- by luopeihuan 2010.12.1 暂时未发现此代码明显用处，去除以加快滑动速度
				/*
				 * final boolean portrait = mPortrait; final int xCount =
				 * portrait ? mShortAxisCells : mLongAxisCells; final int yCount
				 * = portrait ? mLongAxisCells : mShortAxisCells;
				 * 
				 * final boolean[][] occupied = mOccupied;
				 * findOccupiedCells(xCount, yCount, occupied, null);
				 * 
				 * cellInfo.cell = null; cellInfo.cellX = cellXY[0];
				 * cellInfo.cellY = cellXY[1]; cellInfo.spanX = 1;
				 * cellInfo.spanY = 1; cellInfo.valid = cellXY[0] >= 0 &&
				 * cellXY[1] >= 0 && cellXY[0] < xCount && cellXY[1] < yCount &&
				 * !occupied[cellXY[0]][cellXY[1]];
				 */

				// Instead of finding the interesting vacant cells here, wait
				// until a
				// caller invokes getTag() to retrieve the result. Finding the
				// vacant
				// cells is a bit expensive and can generate many new objects,
				// it's
				// therefore better to defer it until we know we actually need
				// it.

				mDirtyTag = true;
			}
			setTag(cellInfo);
		} else if (action == MotionEvent.ACTION_UP) {
			cellInfo.cell = null;
			cellInfo.cellX = -1;
			cellInfo.cellY = -1;
			cellInfo.spanX = 0;
			cellInfo.spanY = 0;
			cellInfo.valid = false;
			mDirtyTag = false;
			setTag(cellInfo);
		}

		return mLayoutScale < 1.0;
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event)
	// {
	// return true;
	// }

	@Override
	public CellInfo getTag() {
		final CellInfo info = (CellInfo) super.getTag();
		if (mDirtyTag && info.valid) {
			final boolean portrait = sPortrait;
			final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
			final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

			final boolean[][] occupied = mOccupied;
			findOccupiedCells(xCount, yCount, occupied, null);

			findIntersectingVacantCells(info, info.cellX, info.cellY, xCount, yCount, occupied);

			mDirtyTag = false;
		}
		return info;
	}

	private static void findIntersectingVacantCells(CellInfo cellInfo, int x, int y, int xCount,
			int yCount, boolean[][] occupied) {

		cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
		cellInfo.clearVacantCells();

		try {
			if (occupied[x][y]) {
				return;
			}
		} catch (IndexOutOfBoundsException e) {
			return;
		}

		cellInfo.current.set(x, y, x, y);
		findVacantCell(cellInfo.current, xCount, yCount, occupied, cellInfo);
	}
	//寻找空位（单元格）
	private static void findVacantCell(Rect current, int xCount, int yCount, boolean[][] occupied,
			CellInfo cellInfo) {
		for (int l = 0; l < xCount; l++) {
			for (int r = l; r < xCount; r++) {
				for (int t = 0; t < yCount; t++) {
					for (int b = t; b < yCount && isRowEmpty(b, l, r, occupied); b++) {
						current.left = l;
						current.right = r;
						current.top = t;
						current.bottom = b;

						addVacantCell(current, cellInfo);
					}
				}
			}
		}
	}

	private static void findCell(Rect current, int xCount, int yCount, boolean[][] occupied,
			CellInfo cellInfo) {
		for (int l = 0; l < xCount; l++) {
			for (int r = l; r < xCount; r++) {
				for (int t = 0; t < yCount; t++) {
					for (int b = t; b < yCount; b++) {
						current.left = l;
						current.right = r;
						current.top = t;
						current.bottom = b;

						addVacantCell(current, cellInfo);
					}
				}
			}
		}
	}

	// Note the row test in the last for loop. No need to test the whole area,
	// only the
	// newly added row since everything before it would have already been
	// tested.
	public static boolean isEmpty(int x0, int x1, int y0, int y1, boolean[][] occupied) {
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				if (occupied[x][y]) {
					return false;
				}
			}
		}
		return true;
	}

	private static void addVacantCell(Rect current, CellInfo cellInfo) {
		CellInfo.VacantCell cell = CellInfo.VacantCell.acquire();
		cell.cellX = current.left;
		cell.cellY = current.top;
		cell.spanX = current.right - current.left + 1;
		cell.spanY = current.bottom - current.top + 1;
		if (cell.spanX > cellInfo.maxVacantSpanX) {
			cellInfo.maxVacantSpanX = cell.spanX;
			cellInfo.maxVacantSpanXSpanY = cell.spanY;
		}
		if (cell.spanY > cellInfo.maxVacantSpanY) {
			cellInfo.maxVacantSpanY = cell.spanY;
			cellInfo.maxVacantSpanYSpanX = cell.spanX;
		}
		cellInfo.vacantCells.add(cell);
	}

	@SuppressWarnings("unused")
	private static boolean isColumnEmpty(int x, int top, int bottom, boolean[][] occupied) {
		for (int y = top; y <= bottom; y++) {
			if (occupied[x][y]) {
				return false;
			}
		}
		return true;
	}

	private static boolean isRowEmpty(int y, int left, int right, boolean[][] occupied) {
		for (int x = left; x <= right; x++) {
			if (occupied[x][y]) {
				return false;
			}
		}
		return true;
	}

	CellInfo findAllVacantCells(boolean[] occupiedCells, View ignoreView) {
		final boolean portrait = sPortrait;
		final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
		final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

		boolean[][] occupied = mOccupied;

		if (occupiedCells != null) {
			for (int y = 0; y < yCount; y++) {
				for (int x = 0; x < xCount; x++) {
					occupied[x][y] = occupiedCells[y * xCount + x];
				}
			}
		} else {
			findOccupiedCells(xCount, yCount, occupied, ignoreView);
		}

		return findAllVacantCellsFromOccupied(occupied, xCount, yCount);
	}

	CellInfo findAllCells(boolean[] occupiedCells, View ignoreView) {
		final boolean portrait = sPortrait;
		final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
		final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

		boolean[][] occupied = mOccupied;

		if (occupiedCells != null) {
			for (int y = 0; y < yCount; y++) {
				for (int x = 0; x < xCount; x++) {
					occupied[x][y] = occupiedCells[y * xCount + x];
				}
			}
		} else {
			findOccupiedCells(occupied.length, occupied[0].length, occupied, ignoreView);
		}

		return findAllCellsFromOccupied(occupied, occupied.length, occupied[0].length);
	}

	/**
	 * Variant of findAllVacantCells that uses LauncerModel as its source rather
	 * than the views.
	 */
	CellInfo findAllVacantCellsFromOccupied(boolean[][] occupied, final int xCount, final int yCount) {
		CellInfo cellInfo = new CellInfo();

		cellInfo.cellX = -1;
		cellInfo.cellY = -1;
		cellInfo.spanY = 0;
		cellInfo.spanX = 0;
		cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
		cellInfo.screen = mCellInfo.screen;

		Rect current = cellInfo.current;

		/*
		 * for (int x = 0; x < xCount; x++) { for (int y = 0; y < yCount; y++) {
		 * if (!occupied[x][y]) { current.set(x, y, x, y);
		 * findVacantCell(current, xCount, yCount, occupied, cellInfo);
		 * occupied[x][y] = true; } } }
		 */

		findVacantCell(current, xCount, yCount, occupied, cellInfo);
		cellInfo.valid = cellInfo.vacantCells.size() > 0;

		// Assume the caller will perform their own cell searching, otherwise we
		// risk causing an unnecessary rebuild after findCellForSpan()

		return cellInfo;
	}

	/**
	 * Variant of findAllCells that uses LauncerModel as its source rather than
	 * the views.
	 */
	CellInfo findAllCellsFromOccupied(boolean[][] occupied, final int xCount, final int yCount) {
		CellInfo cellInfo = new CellInfo();

		cellInfo.cellX = -1;
		cellInfo.cellY = -1;
		cellInfo.spanY = 0;
		cellInfo.spanX = 0;
		cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
		cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
		cellInfo.screen = mCellInfo.screen;

		Rect current = cellInfo.current;

		/*
		 * for (int x = 0; x < xCount; x++) { for (int y = 0; y < yCount; y++) {
		 * if (!occupied[x][y]) { current.set(x, y, x, y);
		 * findVacantCell(current, xCount, yCount, occupied, cellInfo);
		 * occupied[x][y] = true; } } }
		 */

		findCell(current, xCount, yCount, occupied, cellInfo);
		cellInfo.valid = cellInfo.vacantCells.size() > 0;

		// Assume the caller will perform their own cell searching, otherwise we
		// risk causing an unnecessary rebuild after findCellForSpan()

		return cellInfo;
	}

	/**
	 * Given a point, return the cell that strictly encloses that point
	 * 
	 * @param x
	 *            X coordinate of the point
	 * @param y
	 *            Y coordinate of the point
	 * @param result
	 *            Array of 2 ints to hold the x and y coordinate of the cell
	 * @return true:目标坐标在桌面排版内　false:目标坐标在桌面排版外
	 */
	boolean pointToCellExact(int x, int y, int[] result) {
		boolean isInside = true;

		final boolean portrait = sPortrait;

		final int hStartPadding = getLeftPadding();
		final int vStartPadding = getTopPadding();
		// final int width = (int) (mAutoStretch ? mCellRealWidth : mCellWidth +
		// mWidthGap);
		// final int height = (int) (mAutoStretch ? mCellRealHeight :
		// mCellHeight + mHeightGap);
		final int width = sCellRealWidth;
		final int height = sCellRealHeight;

		result[0] = (x - hStartPadding) / width;
		result[1] = (y - vStartPadding) / height;

		final int xAxis = portrait ? mShortAxisCells : mLongAxisCells;
		final int yAxis = portrait ? mLongAxisCells : mShortAxisCells;

		if (result[0] < 0 || x - hStartPadding < 0) {
			result[0] = 0;
			isInside = false;
		}
		if (result[0] >= xAxis) {
			result[0] = xAxis - 1;
			isInside = false;
		}
		if (result[1] < 0 || y - vStartPadding < 0) {
			result[1] = 0;
			isInside = false;
		}
		if (result[1] >= yAxis) {
			result[1] = yAxis - 1;
			isInside = false;
		}

		return isInside;
	}

	/**
	 * Given a cell coordinate, return the point that represents the upper left
	 * corner of that cell
	 * 
	 * @param cellX
	 *            X coordinate of the cell
	 * @param cellY
	 *            Y coordinate of the cell
	 * 
	 * @param result
	 *            Array of 2 ints to hold the x and y coordinate of the point
	 */
	void cellToPoint(int cellX, int cellY, int[] result) {
		final int hStartPadding = getLeftPadding();
		final int vStartPadding = getTopPadding();
		final int width = (int) (sAutoStretch ? sCellRealWidth : sCellWidth + sWidthGap);
		final int height = (int) (sAutoStretch ? sCellRealHeight : sCellHeight + sHeightGap);

		result[0] = hStartPadding + cellX * width;
		result[1] = vStartPadding + cellY * height;
	}

	/**
	 * Given a cell coordinate, return the point that represents the upper left
	 * corner of that cell
	 * 
	 * @param cellX
	 *            X coordinate of the cell
	 * @param cellY
	 *            Y coordinate of the cell
	 * 
	 * @param result
	 *            Array of 2 ints to hold the x and y coordinate of the point
	 */
	public void cellToCenterPoint(int cellX, int cellY, int[] result) {
		final int hStartPadding = getLeftPadding();
		final int vStartPadding = getTopPadding();
		final int width = (int) (sAutoStretch ? sCellRealWidth : sCellWidth + sWidthGap);
		final int height = (int) (sAutoStretch ? sCellRealHeight : sCellHeight + sHeightGap);

		result[0] = hStartPadding + cellX * width + sCellWidth / 2;
		result[1] = vStartPadding + cellY * height + sCellHeight / 2;
	}
	// Add by xiangliang 飞行动画计算飞行图标左上角的终点位置
		public void cellToGridCenter(int cellX, int cellY, int[] result) {
			final int hStartPadding = getLeftPadding();
			final int vStartPadding = getTopPadding();
			final int width = (int) (sAutoStretch ? sCellRealWidth : sCellWidth
					+ sWidthGap);
			final int height = (int) (sAutoStretch ? sCellRealHeight : sCellHeight
					+ sHeightGap);

			result[0] = (int) (hStartPadding + cellX * width + getResources()
					.getDimensionPixelSize(R.dimen.screen_edit_appgrid_width));
			result[1] = (int) (vStartPadding + cellY * height + getResources()
					.getDimensionPixelSize(R.dimen.screen_edit_appgrid_height));
		}
	public void cellToRealCenterPoint(int cellX, int cellY, int[] result) {
		final int hStartPadding = getLeftPadding();
		final int vStartPadding = getTopPadding();
		final int width = (int) (sAutoStretch ? sCellRealWidth : sCellWidth + sWidthGap);
		final int height = (int) (sAutoStretch ? sCellRealHeight : sCellHeight + sHeightGap);

		result[0] = hStartPadding + cellX * width + sCellRealWidth / 2;
		result[1] = vStartPadding + cellY * height + sCellRealHeight / 2;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (SenseWorkspace.showStatusBar) {
			super.onMeasure(getMeasuredWidth(), getMeasuredHeight());
			return;
		}
		if (LockerThemeTab.sCHANG_LOCKER_THEME) {
			super.onMeasure(getMeasuredWidth(), getMeasuredHeight());
			return;
		}
		// TODO: currently ignoring padding
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
		}

		sPortrait = heightSpecSize > widthSpecSize;
		Resources resources = getResources();
		// 根据横竖屏加载不同尺寸信息
		if (sPortrait) {
			sCellWidth = resources.getDimensionPixelSize(R.dimen.cell_width_port);
			sCellHeight = resources.getDimensionPixelSize(R.dimen.cell_height_port);
			sLongAxisStartPadding = resources
					.getDimensionPixelSize(R.dimen.screen_long_start_padding_port);
			sLongAxisEndPadding = resources
					.getDimensionPixelSize(R.dimen.screen_long_end_padding_port);
			sShortAxisStartPadding = resources
					.getDimensionPixelSize(R.dimen.screen_short_start_padding_port);
			sShortAxisEndPadding = resources
					.getDimensionPixelSize(R.dimen.screen_short_end_padding_port);
			sLongAxisStartNoIndicatorPadding = resources
					.getDimensionPixelSize(R.dimen.screen_long_start_padding_large_icon_port);
			// if(!mLargeIcon)
			// {
			// int tempHeight = Math.max(0,
			// resources.getDimensionPixelSize(R.dimen.dock_bg_height) -
			// DockConstant.getBgHeight());
			// mLongAxisStartPadding += tempHeight;
			// mLongAxisEndPadding -= tempHeight;
			// }
		} else {
			sCellWidth = resources.getDimensionPixelSize(R.dimen.cell_width_land);
			sCellHeight = resources.getDimensionPixelSize(R.dimen.cell_height_land);
			sLongAxisStartPadding = resources
					.getDimensionPixelSize(R.dimen.screen_long_start_padding_land);
			sLongAxisEndPadding = resources
					.getDimensionPixelSize(R.dimen.screen_long_end_padding_land);
			sShortAxisStartPadding = resources
					.getDimensionPixelSize(R.dimen.screen_short_start_padding_land);
			sShortAxisEndPadding = resources
					.getDimensionPixelSize(R.dimen.screen_short_end_padding_land);
			sLongAxisStartNoIndicatorPadding = resources
					.getDimensionPixelSize(R.dimen.screen_long_start_padding_large_icon_land);
		}

		// if(mLargeIcon)
		// {
		// mLongAxisStartPadding = mLongAxisStartNoIndicatorPadding;
		// }
		final int oldShortAxisCells = mShortAxisCells;
		final int oldLongAxisCells = mLongAxisCells;
		if (sPortrait) // 竖屏
		{
			mLongAxisCells = sRows;
			mShortAxisCells = sColumns;

			sCellRealWidth = (widthSpecSize - sShortAxisStartPadding - sShortAxisEndPadding)
					/ sColumns;
			sCellRealHeight = (heightSpecSize - getTopPadding() - getBottomPadding()) / sRows;

		} else {
			mLongAxisCells = sColumns;
			mShortAxisCells = sRows;
			sCellRealWidth = (widthSpecSize - sLongAxisStartPadding - getRightPadding()) / sColumns;
			sCellRealHeight = (heightSpecSize - getTopPadding() - getBottomPadding()) / sRows;

		}
		// 是否需要保留边距的标识
		boolean needPaddingH = true;
		boolean needPaddingV = true;
		// 自动缩放
		if (sAutoStretch /* && !mLargeIcon */) {
			if (sCellWidth > sCellRealWidth) {
				needPaddingH = false;
			}
			if (sCellHeight > sCellRealHeight) {
				needPaddingV = false;
			}
			sCellWidth = sCellRealWidth;
			sCellHeight = sCellRealHeight;
		}

		mNeedPaddingH = needPaddingH;
		mNeedPaddingV = needPaddingV;

		if (mOccupied == null || oldShortAxisCells != mShortAxisCells
				|| oldLongAxisCells != mLongAxisCells) {
			if (sPortrait) {
				mOccupied = new boolean[mShortAxisCells][mLongAxisCells];
				mTmpOccupied = new boolean[mShortAxisCells][mLongAxisCells];
			} else {
				mOccupied = new boolean[mLongAxisCells][mShortAxisCells];
				mTmpOccupied = new boolean[mLongAxisCells][mShortAxisCells];
			}
		}

		final int shortAxisCells = mShortAxisCells;
		final int longAxisCells = mLongAxisCells;
		final int longAxisStartPadding = sLongAxisStartPadding;
		final int longAxisEndPadding = sLongAxisEndPadding;
		final int shortAxisStartPadding = sShortAxisStartPadding;
		final int shortAxisEndPadding = sShortAxisEndPadding;
		final int cellWidth = sCellWidth;
		final int cellHeight = sCellHeight;

		int numShortGaps = shortAxisCells - 1;
		int numLongGaps = longAxisCells - 1;

		final int blankHeight = 0;
		final int blankWidth = 0;
		// final int blankHeight = mLargeIcon?
		// resources.getDimensionPixelSize(R.dimen.cell_layout_blank_height) :
		// 0;
		// final int blankWidth = mLargeIcon?
		// resources.getDimensionPixelSize(R.dimen.cell_layout_blank_width) : 0;

		if (sPortrait) {
			int vSpaceLeft = heightSpecSize - getTopPadding() - getBottomPadding()
					- (cellHeight * longAxisCells) - blankHeight;
			sHeightGap = (float) vSpaceLeft / numLongGaps;

			int hSpaceLeft = widthSpecSize - shortAxisStartPadding - shortAxisEndPadding
					- (cellWidth * shortAxisCells) /*- blankWidth*/;
			if (numShortGaps > 0) {
				sWidthGap = (float) hSpaceLeft / numShortGaps;
			} else {
				sWidthGap = 0;
			}

			setMeasuredDimension(widthSpecSize - shortAxisEndPadding, heightSpecSize
					- longAxisEndPadding);
		} else {
			int hSpaceLeft = widthSpecSize - longAxisStartPadding - getRightPadding()
					- (cellWidth * longAxisCells) - blankWidth;
			sWidthGap = (float) hSpaceLeft / numLongGaps;

			int vSpaceLeft = heightSpecSize - getTopPadding() - getBottomPadding()
					- (cellHeight * shortAxisCells);
			if (numShortGaps > 0) {
				sHeightGap = (float) vSpaceLeft / numShortGaps;
			} else {
				sHeightGap = 0;
			}

			setMeasuredDimension(widthSpecSize, heightSpecSize);
		}

		int count = getChildCount();
		final int leftPadding = sPortrait ? shortAxisStartPadding : longAxisStartPadding;
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child == null || child.getLayoutParams() == null) {
				continue;
			}
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			final boolean isDeskIcon = child instanceof BubbleTextView;
			if (isDeskIcon) {
				lp.setupForBubbleTextView(cellWidth, cellHeight, sWidthGap, sHeightGap,
						leftPadding, getTopPadding(), sAutoStretch, needPaddingH, needPaddingV);
			} else {
				//这里需要将计算的结果封装进LayoutParams中,供CellLayout在布局子控件的时候使用
				lp.setup(cellWidth, cellHeight, sWidthGap, sHeightGap, leftPadding,
						getTopPadding(), sAutoStretch);
			}

			if (lp.regenerateId) {
				child.setId(((getId() & 0xFF) << 16) | (lp.cellX & 0xFF) << 8 | (lp.cellY & 0xFF));
				lp.regenerateId = false;
			}

			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
			int childheightMeasureSpec = MeasureSpec
					.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
			try {
				child.measure(childWidthMeasureSpec, childheightMeasureSpec);
			} catch (Exception e) {
				// 可能空指针
			}
		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();

		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child != null && child.getVisibility() != GONE) {

				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

				int childLeft = lp.x;
				int childTop = lp.y;
				try {
					child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);
				} catch (Throwable e) {
					if (child != null) {
						Object tag = child.getTag();
						if (tag != null && tag instanceof ItemInfo) {
							ItemInfo info = (ItemInfo) tag;
							if (info instanceof ScreenAppWidgetInfo && ((ScreenAppWidgetInfo) info).mProviderIntent != null) {
								ScreenAppWidgetInfo widgetinfo = (ScreenAppWidgetInfo) info;
								ComponentName componentName = widgetinfo.mProviderIntent
										.getComponent();
								String packageName = componentName.getPackageName();
								PackageManager pm = getContext().getPackageManager();
								try {
									ApplicationInfo appInfo = pm.getApplicationInfo(packageName,
											PackageManager.GET_META_DATA);
									String message = getContext().getString(R.string.widget_errors, pm.getApplicationLabel(appInfo));
									Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
									TimerTask task = new TimerTask() {

										@Override
										public void run() {
											GoLauncher.sendHandler(this,
													IDiyFrameIds.SCHEDULE_FRAME,
													IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
										}
									};
									Timer timer = new Timer();
									timer.schedule(task, 2500);
								} catch (NameNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}
				}

				if (lp.dropped) {
					lp.dropped = false;

					final int[] cellXY = mCellXY;
					getLocationOnScreen(cellXY);
					// mWallpaperManager.sendWallpaperCommand(getWindowToken(),
					// "android.home.drop", cellXY[0] + childLeft
					// + lp.width / 2, cellXY[1] + childTop
					// + lp.height / 2, 0, null);
				}
			}
		}
		mChildrenDrawingCacheBuilt = false;
		// buildChildrenDrawingCache();
	}

	@Override
	public void setChildrenDrawnWithCacheEnabled(boolean enabled) {
		mChildrenDrawnWithCache = enabled;
		// setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
		super.setChildrenDrawnWithCacheEnabled(enabled);
	}

	public void buildChildrenDrawingCache() {
		if (!mChildrenDrawnWithCache) {
			setChildrenDrawnWithCacheEnabled(true);
		}
		if (mChildrenDrawingCacheBuilt) {
			return;
		}
		final int count = getChildCount();
		if (count > 0) {
			setChildrenDrawnWithCacheEnabled(true);
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);
				// view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
				view.setDrawingCacheEnabled(true);
				// Update the drawing caches
				//				 view.buildDrawingCache(true);
			}
			mChildrenDrawingCacheBuilt = true;
		}
	}

	public void destroyChildrenDrawingCache() {
		mChildrenDrawingCacheBuilt = false;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View view = getChildAt(i);
			view.setDrawingCacheEnabled(false);
			// view.destroyDrawingCache();
		}
	}

//	public void enableHardwareLayers() {
//		if (ViewCompat.IS_HONEYCOMB) {
//			ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_HARDWARE, null);
//		}
//	}
//
//	public void enableWidgetHardwareAccelerated(boolean enabled) {
//		if (ViewCompat.IS_HONEYCOMB) {
//			int layerType = enabled
//					? ViewCompat.LAYER_TYPE_HARDWARE
//					: ViewCompat.LAYER_TYPE_SOFTWARE;
//			if (mWidgetLayerType != layerType) {
//				mWidgetLayerType = layerType;
//				int childCount = getChildCount();
//				for (int i = 0; i < childCount; i++) {
//					final View view = getChildAt(i);
//					final ItemInfo itemInfo = (ItemInfo) view.getTag();
//					if (itemInfo != null && itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
//						ViewCompat.setLayerType(view, layerType, null);
//					}
//				}
//			}
//		}
//	}

	private void setWidgetHardwareAccelerated(int mode) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View view = getChildAt(i);
			final ItemInfo itemInfo = (ItemInfo) view.getTag();
			if (itemInfo != null && itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
				Machine.setHardwareAccelerated(view, mode);
			}
		}
	}

	public void openWidgetHardwareAccelerated() {
		if (mLayerType != Machine.LAYER_TYPE_HARDWARE) {
			mLayerType = Machine.LAYER_TYPE_HARDWARE;
			setWidgetHardwareAccelerated(Machine.LAYER_TYPE_HARDWARE);
		}
	}

	public void closeWidgetHardwareAccelerated() {
		if (mLayerType != Machine.LAYER_TYPE_SOFTWARE) {
			mLayerType = Machine.LAYER_TYPE_SOFTWARE;
			setWidgetHardwareAccelerated(Machine.LAYER_TYPE_SOFTWARE);
		}
	}
	
	/**
	 * Find a vacant area that will fit the given bounds nearest the requested
	 * cell location. Uses Euclidean distance to score multiple vacant areas.
	 * 
	 * @param pixelX
	 *            The X location at which you want to search for a vacant area.
	 * @param pixelY
	 *            The Y location at which you want to search for a vacant area.
	 * @param spanX
	 *            Horizontal span of the object.
	 * @param spanY
	 *            Vertical span of the object.
	 * @param vacantCells
	 *            Pre-computed set of vacant cells to search.
	 * @param recycle
	 *            Previously returned value to possibly recycle.
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	int[] findNearestVacantArea(int pixelX, int pixelY, int spanX, int spanY, CellInfo vacantCells,
			int[] recycle) {

		// Keep track of best-scoring drop area
		final int[] bestXY = recycle != null ? recycle : new int[2];
		final int[] cellXY = mCellXY;
		double bestDistance = Double.MAX_VALUE;

		// Bail early if vacant cells aren't valid
		if (!vacantCells.valid) {
			return null;
		}

		// Look across all vacant cells for best fit
		final int size = vacantCells.vacantCells.size();
		for (int i = 0; i < size; i++) {
			final CellInfo.VacantCell cell = vacantCells.vacantCells.get(i);

			// Reject if vacant cell isn't our exact size
			if (cell.spanX != spanX || cell.spanY != spanY) {
				continue;
			}

			// Score is center distance from requested pixel
			cellToPoint(cell.cellX, cell.cellY, cellXY);

			double distance = (cellXY[0] - pixelX) * (cellXY[0] - pixelX) + (cellXY[1] - pixelY)
					* (cellXY[1] - pixelY);

			if (distance <= bestDistance) {
				bestDistance = distance;
				bestXY[0] = cell.cellX;
				bestXY[1] = cell.cellY;
			}
		}

		// Return null if no suitable location found
		if (bestDistance < Double.MAX_VALUE) {
			return bestXY;
		} else {
			return null;
		}
	}

	/**
	 * Drop a child at the specified position
	 * 
	 * @param child
	 *            The child that is being dropped
	 * @param targetXY
	 *            Destination area to move to
	 */
	void onDropChild(View child, int[] targetXY) {
		if (child != null) {
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			lp.cellX = lp.tmpCellX = targetXY[0];
			lp.cellY = lp.tmpCellY = targetXY[1];
			lp.isLockedToGrid = true;
			lp.isDragging = false;
			lp.dropped = true;
			mDragRect.setEmpty();
			child.setVisibility(View.VISIBLE); // 设置可见
			child.requestLayout();
			invalidate();
		}
	}

	void onDropAborted(View child) {
		if (child != null) {
			((LayoutParams) child.getLayoutParams()).isDragging = false;
			invalidate();
		}
		mDragRect.setEmpty();
	}

	/**
	 * Start dragging the specified child
	 * 
	 * @param child
	 *            The child that is being dragged
	 */
	void onDragChild(View child) {
		LayoutParams lp = (LayoutParams) child.getLayoutParams();
		lp.isDragging = true;
		mDragRect.setEmpty();
	}

	/**
	 * Computes the required horizontal and vertical cell spans to always fit
	 * the given rectangle.
	 * 
	 * @param width
	 *            Width in pixels
	 * @param height
	 *            Height in pixels
	 * @return 返回屏幕格子行列数
	 */
	public int[] rectToCell(int width, int height) {
		final Resources resources = getResources();
		int actualWidth = 0;
		int actualHeight = 0;
		if (Machine.isTablet(getContext())) {
			if (sPortrait) {
				actualWidth = resources.getDimensionPixelSize(R.dimen.actual_cell_width_port);
				actualHeight = resources.getDimensionPixelSize(R.dimen.actual_cell_height_port);
			} else {
				actualWidth = resources.getDimensionPixelSize(R.dimen.actual_cell_width_land);
				actualHeight = resources.getDimensionPixelSize(R.dimen.actual_cell_height_land);
			}
		} else {
			if (sPortrait) {
				actualWidth = resources.getDimensionPixelSize(R.dimen.cell_width_port);
				actualHeight = resources.getDimensionPixelSize(R.dimen.cell_height_port);
			} else {
				actualWidth = resources.getDimensionPixelSize(R.dimen.cell_width_land);
				actualHeight = resources.getDimensionPixelSize(R.dimen.cell_height_land);
			}
		}

		final int smallerSize = Math.min(actualWidth, actualHeight);
		// Always round up to next largest cell
		int spanX = (width + smallerSize) / smallerSize;
		int spanY = (height + smallerSize) / smallerSize;

		spanX = Math.max(1, Math.min(spanX, sColumns));
		spanY = Math.max(1, Math.min(spanY, sRows));
		return new int[] { spanX, spanY };
	}

	/**
	 * Find the first vacant cell, if there is one.
	 * 
	 * @param vacant
	 *            Holds the x and y coordinate of the vacant cell
	 * @param spanX
	 *            Horizontal cell span.
	 * @param spanY
	 *            Vertical cell span.
	 * 
	 * @return True if a vacant cell was found
	 */
	public boolean getVacantCell(int[] vacant, int spanX, int spanY) {
		final boolean portrait = sPortrait;
		final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
		final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
		final boolean[][] occupied = mOccupied;

		findOccupiedCells(xCount, yCount, occupied, null);

		return findVacantCell(vacant, spanX, spanY, xCount, yCount, occupied);
	}

	/**
	 * 获取当前屏幕空单元格的个数
	 * 
	 * @param vacantList
	 * @return
	 */
	public int getSingleVacantCellCount(List<Point> vacantList) {
		final boolean portrait = sPortrait;
		final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
		final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
		final boolean[][] occupied = mOccupied;
		findOccupiedCells(xCount, yCount, occupied, null);
		return findSingleVacantCellCount(occupied, xCount, yCount, vacantList);
	}

	/**
	 * 获取多少个空单元格
	 * 
	 * @param occupied
	 * @param xCount
	 * @param yCount
	 * @param vacantList
	 * @return
	 */
	static int findSingleVacantCellCount(boolean[][] occupied, int xCount, int yCount,
			List<Point> vacantList) {
		int count = 0;
		if (vacantList != null) {
			vacantList.clear();
		}

		for (int y = 0; y < yCount; y++) {
			for (int x = 0; x < xCount; x++) {
				boolean available = !occupied[x][y];
				if (available) {
					count++;
					if (vacantList != null) {
						vacantList.add(new Point(x, y));
					}
				}
			}
		}
		return count;
	}

	static boolean findVacantCell(int[] vacant, int spanX, int spanY, int xCount, int yCount,
			boolean[][] occupied) {
		for (int y = 0; y < yCount; y++) {
			for (int x = 0; x < xCount; x++) {
				boolean available = !occupied[x][y];
				out : for (int i = y; i < y + spanY && i < yCount; i++) {
					for (int j = x; j < x + spanX && j < xCount; j++) {
						available = available && !occupied[j][i];
						if (!available) {
							break out;
						}
					}
				}

				// 确保不越界
				if (available && (x + spanX - 1 < xCount) && (y + spanY - 1 < yCount)) {
					vacant[0] = x;
					vacant[1] = y;
					return true;
				}
			}
		}
		return false;
	}

	boolean[] getOccupiedCells() {
		final boolean portrait = sPortrait;
		final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
		final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
		final boolean[][] occupied = mOccupied;

		findOccupiedCells(xCount, yCount, occupied, null);

		final boolean[] flat = new boolean[xCount * yCount];
		for (int y = 0; y < yCount; y++) {
			for (int x = 0; x < xCount; x++) {
				flat[y * xCount + x] = occupied[x][y];
			}
		}

		return flat;
	}

	private void findOccupiedCells(int xCount, int yCount, boolean[][] occupied, View ignoreView) {
		for (int x = 0; x < xCount; x++) {
			for (int y = 0; y < yCount; y++) {
				occupied[x][y] = false;
			}
		}

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			// TODO 如果当前是文件夹
			if (child == ignoreView) {
				continue;
			}

			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			for (int x = lp.cellX; x < lp.cellX + lp.cellHSpan && x < xCount && x >= 0; x++) {
				for (int y = lp.cellY; y < lp.cellY + lp.cellVSpan && y < yCount && y >= 0; y++) {
					occupied[x][y] = true;
				}
			}
		}
	}

	/**
	 * 标记当前的占用格子
	 */
	void markOccupied() {
		boolean[][] occupied = mOccupied;
		findOccupiedCells(occupied.length, occupied[0].length, occupied, null);
	}
	
	public boolean lastDownOnOccupiedCell() {
		return mLastDownOnOccupiedCell;
	}

	synchronized void recycleBitmap() {
		destroyDrawingCache();
		setDrawingCacheEnabled(false);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new CellLayout.LayoutParams(getContext(), attrs);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof CellLayout.LayoutParams;
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new CellLayout.LayoutParams(p);
	}

	/**
	 * 
	 * @author jiangxuwen
	 *
	 */
	//CHECKSTYLE:OFF
	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
		/**
		 *  该child view占用的第几列的cell(若横向占用多个cell，表示最左边的cellx)
		 * Horizontal location of the item in the grid.
		 */
		@ViewDebug.ExportedProperty
		public int cellX;

		/**
		 * 该child view占用的第几行的cell(若纵向占用多个cell，表示最上边的celly)
		 * Vertical location of the item in the grid.
		 */
		@ViewDebug.ExportedProperty
		public int cellY;

		/**
		 * Temporary horizontal location of the item in the grid during reorder
		 */
		public int tmpCellX;

		/**
		 * Temporary vertical location of the item in the grid during reorder
		 */
		public int tmpCellY;

		/**
		 * Indicates that the temporary coordinates should be used to layout the
		 * items
		 */
		public boolean useTmpCoords;

		/**
		 * Indicates whether this item can be reordered. Always true except in
		 * the case of the the AllApps button.
		 */
		public boolean canReorder = true;

		/**
		 * 横向跨越的列数
		 * Number of cells spanned horizontally by the item.
		 *  
		 */
		@ViewDebug.ExportedProperty
		public int cellHSpan;

		/**
		 * 纵向跨越行数
		 * Number of cells spanned vertically by the item.
		 */
		@ViewDebug.ExportedProperty
		public int cellVSpan;

		/**
		 * Indicates whether the item will set its x, y, width and height
		 * parameters freely, or whether these will be computed based on cellX,
		 * cellY, cellHSpan and cellVSpan.
		 */
		public boolean isLockedToGrid = true;

		/**
		 * 该child是否正在被拖动
		 * Is this item currently being dragged
		 */
		public boolean isDragging;

		// X coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		int x;
		// Y coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		int y;
		/**
		 * 是否重新生成view id 
		 */
		boolean regenerateId;

		boolean dropped;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			cellHSpan = 1;
			cellVSpan = 1;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
			cellHSpan = 1;
			cellVSpan = 1;
		}

		public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
			super(android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT);
			this.cellX = cellX;
			this.cellY = cellY;
			this.cellHSpan = cellHSpan;
			this.cellVSpan = cellVSpan;
		}
		/**
		 * 计算了cell的width，height，起始坐标x和y。
		 */
		public void setup(int cellWidth, int cellHeight, float widthGap, float heightGap,
				int hStartPadding, int vStartPadding, boolean autoStretch) {
			if (isLockedToGrid) {
				final int myCellHSpan = cellHSpan;
				final int myCellVSpan = cellVSpan;
				final int myCellX = useTmpCoords ? tmpCellX : cellX;
				final int myCellY = useTmpCoords ? tmpCellY : cellY;

				if (autoStretch) {
					width = myCellHSpan * sCellRealWidth - leftMargin - rightMargin;
					height = myCellVSpan * sCellRealHeight - topMargin - bottomMargin;
					x = hStartPadding + myCellX * sCellRealWidth + leftMargin;
					y = vStartPadding + myCellY * sCellRealHeight + topMargin;
				} else {
					width = (int) (myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap)
							- leftMargin - rightMargin);
					height = (int) (myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap)
							- topMargin - bottomMargin);
					x = (int) (hStartPadding + myCellX * (cellWidth + widthGap) + leftMargin);
					y = (int) (vStartPadding + myCellY * (cellHeight + heightGap) + topMargin);
				}
			}
		}

		public void setupForBubbleTextView(int cellWidth, int cellHeight, float widthGap,
				float heightGap, int hStartPadding, int vStartPadding, boolean autoStretch,
				boolean needPaddingH, boolean needPaddingV) {
			if (isLockedToGrid) {
				final int myCellHSpan = cellHSpan;
				final int myCellVSpan = cellVSpan;
				final int myCellX = useTmpCoords ? tmpCellX : cellX;
				final int myCellY = useTmpCoords ? tmpCellY : cellY;

				final int paddingH = needPaddingH ? leftMargin + rightMargin : 0;
				final int paddingV = needPaddingV ? topMargin + bottomMargin : 0;
				width = cellWidth - paddingH;
				height = cellHeight - paddingV;

				if (autoStretch) {
					// 布局3*3的时候使4*4widget不超出显示范围
					if (myCellHSpan == 4 && sColumns == 3) {
						width -= cellWidth + widthGap;
					}
					if (myCellVSpan == 4 && sRows == 3) {
						height -= cellHeight + heightGap;
					}
					x = hStartPadding + myCellX * sCellRealWidth + (sCellRealWidth - width) / 2;
					x = x > 0 ? x : 0;
					y = vStartPadding + myCellY * sCellRealHeight + (sCellRealHeight - height) / 2;
					y = y > 0 ? y : 0;
				} else {
					x = (int) (hStartPadding + myCellX * (cellWidth + widthGap) + leftMargin);
					y = (int) (vStartPadding + myCellY * (cellHeight + heightGap) + topMargin);
				}
			} // end if(isLockedToGrid)
		}
	}

	/**
	 * 
	 * @author jiangxuwen
	 *
	 */
	static final class CellInfo implements ContextMenu.ContextMenuInfo {
		/**
		 * See View.AttachInfo.InvalidateInfo for futher explanations about the
		 * recycling mechanism. In this case, we recycle the vacant cells
		 * instances because up to several hundreds can be instanciated when the
		 * user long presses an empty cell.
		 */
		static final class VacantCell {
			int cellX;
			int cellY;
			int spanX;
			int spanY;

			// We can create up to 523 vacant cells on a 4x4 grid, 100 seems
			// like a reasonable compromise given the size of a VacantCell and
			// the fact that the user is not likely to touch an empty 4x4 grid
			// very often
			private static final int POOL_LIMIT = 100; // //池最多缓存100个VacantCell  
			private static final Object sLock = new Object(); // 同步锁

			private static int sAcquiredCount = 0;
			private static VacantCell sRoot;

			private VacantCell next;

			static VacantCell acquire() {
				synchronized (sLock) {
					if (sRoot == null) {
						return new VacantCell(); //一开始没有的时候,一直新创建再返回  
					}
					 //如果池存在,则从池中取 
					VacantCell info = sRoot;
					sRoot = info.next;
					sAcquiredCount--; //将统计更新  

					return info;
				}
			}
			   //release这个对象自身  
			void release() {
				synchronized (sLock) {
					if (sAcquiredCount < POOL_LIMIT) {
						sAcquiredCount++;
						next = sRoot;
						sRoot = this;
					}
				}
			}

			@Override
			public String toString() {
				return "VacantCell[x=" + cellX + ", y=" + cellY + ", spanX=" + spanX + ", spanY="
						+ spanY + "]";
			}
		}
		//VacantCell的大小信息 
		View cell;
		int cellX;
		int cellY;
		int spanX;
		int spanY;
		int screen;
		boolean valid;

		final ArrayList<VacantCell> vacantCells = new ArrayList<VacantCell>(VacantCell.POOL_LIMIT);
		int maxVacantSpanX;
		int maxVacantSpanXSpanY;
		int maxVacantSpanY;
		int maxVacantSpanYSpanX;
		final Rect current = new Rect();
		
		/**
		 * 将Vacant清空：具体是释放每个cell，将list清空
		 */
		void clearVacantCells() {
			final ArrayList<VacantCell> list = vacantCells;
			final int count = list.size();

			for (int i = 0; i < count; i++) {
				list.get(i).release();
			}

			list.clear();
		}
		void findVacantCellsFromOccupied(boolean[] occupied, int xCount, int yCount) {
			if (cellX < 0 || cellY < 0) {
				maxVacantSpanX = maxVacantSpanXSpanY = Integer.MIN_VALUE;
				maxVacantSpanY = maxVacantSpanYSpanX = Integer.MIN_VALUE;
				clearVacantCells();
				return;
			}

			final boolean[][] unflattened = new boolean[xCount][yCount];
			for (int y = 0; y < yCount; y++) {
				for (int x = 0; x < xCount; x++) {
					unflattened[x][y] = occupied[y * xCount + x];
				}
			}
			CellLayout.findIntersectingVacantCells(this, cellX, cellY, xCount, yCount, unflattened);
		}

		/**
		 * This method can be called only once! Calling
		 * #findVacantCellsFromOccupied will restore the ability to call this
		 * method.
		 * 
		 * Finds the upper-left coordinate of the first rectangle in the grid
		 * that can hold a cell of the specified dimensions.
		 * 
		 * @param cellXY
		 *            The array that will contain the position of a vacant cell
		 *            if such a cell can be found.
		 * @param spanX
		 *            The horizontal span of the cell we want to find.
		 * @param spanY
		 *            The vertical span of the cell we want to find.
		 * 
		 * @return True if a vacant cell of the specified dimension was found,
		 *         false otherwise.
		 */
		boolean findCellForSpan(int[] cellXY, int spanX, int spanY) {
			return findCellForSpan(cellXY, spanX, spanY, true);
		}

		boolean findCellForSpan(int[] cellXY, int spanX, int spanY, boolean clear) {
			final ArrayList<VacantCell> list = vacantCells;
			final int count = list.size();

			boolean found = false;

			if (this.spanX >= spanX && this.spanY >= spanY) {
				cellXY[0] = cellX;
				cellXY[1] = cellY;
				found = true;
			}

			// Look for an exact match first
			for (int i = 0; i < count; i++) {
				VacantCell cell = list.get(i);
				if (cell.spanX == spanX && cell.spanY == spanY) {
					cellXY[0] = cell.cellX;
					cellXY[1] = cell.cellY;
					found = true;
					break;
				}
			}

			// Look for the first cell large enough
			for (int i = 0; i < count; i++) {
				VacantCell cell = list.get(i);
				if (cell.spanX >= spanX && cell.spanY >= spanY) {
					cellXY[0] = cell.cellX;
					cellXY[1] = cell.cellY;
					found = true;
					break;
				}
			}

			if (clear) {
				clearVacantCells();
			}

			return found;
		}

		@Override
		public String toString() {
			return "Cell[view=" + (cell == null ? "null" : cell.getClass()) + ", x=" + cellX
					+ ", y=" + cellY + "]";
		}
	}
	//CHECKSTYLE:ON
	
	protected void setScreen(int screen) {
		mCellInfo.screen = screen;
	}

	protected int getScreen() {
		return mCellInfo.screen;
	}

	/**
	 * 设置图标自动拉伸
	 * 
	 * @param autoStretch
	 *            true自动拉伸
	 */
	static void setAutoStretch(boolean autoStretch) {
		sAutoStretch = autoStretch;
	}

	static void setCellWidth(int width) {
		sCellRealWidth = sCellWidth = width;
	}

	static void setCellHeight(int height) {
		sCellRealHeight = sCellHeight = height;
	}

	static int getViewOffsetW() {
		return sCellWidth + (int) sWidthGap;
	}

	static int getViewOffsetH() {
		return sCellHeight + (int) sHeightGap;
	}

	//	// 重新设置bubleTextView的title
	//	private void resetTitleAndAlpha() {
	//		if (mMergeFolderChildView != null && mMergeFolderChildView instanceof BubbleTextView) {
	//			BubbleTextView bubbleTextView = ( BubbleTextView ) mMergeFolderChildView;
	//			bubbleTextView.setAlphaValue(255);
	//			bubbleTextView.setText(bubbleTextView.getTitleIgnoreVisible());
	//		}
	//	}

	/**
	 * 获取指定区域（格子）的view
	 * 
	 * @param cell
	 * @return 一行一列的图标或者多行多列的widget
	 */
	public View getChildViewByCell(int[] cell) {
		if (null == cell) {
			return null;
		}

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View childView = getChildAt(i);
			LayoutParams lp = (LayoutParams) childView.getLayoutParams();
			if (null != lp) {
				if (cell[0] >= lp.cellX && cell[0] < lp.cellX + lp.cellHSpan && cell[1] >= lp.cellY
						&& cell[1] < lp.cellY + lp.cellVSpan) {
					return childView;
				}
			}
		}
		return null;
	}
	/**
	 * 取得绘制状态
	 * @return
	 */
	public int getDrawStatus() {
		return mDrawStatus;
	}

	/**
	 * 设置绘制正常状态
	 */
	public void setStatusNormal() {
		this.mStartTime = 0;
		this.mDrawStatus = DRAW_STATUS_NORMAL;
		mOutlineVisible = true;
		setDrawingCacheEnabled(false);
	}

	// public ArrayList<Rect> getFolderRrects(View ignoreView, int topExtra)
	// {
	// ArrayList<Rect> rectList = new ArrayList<Rect>();
	// int count = getChildCount();
	// for (int i = 0; i < count; i++)
	// {
	// View childView = getChildAt(i);
	// if(childView == ignoreView){
	// continue;
	// }
	//
	// Object tagObject = childView.getTag();
	// if(childView instanceof FolderIcon
	// && tagObject instanceof UserFolderInfo)
	// {
	// Rect rect = new Rect(childView.getLeft(), childView.getTop() + topExtra,
	// childView.getRight(), childView.getBottom() + topExtra);
	//
	// rectList.add(rect);
	// }
	// }
	//
	// return rectList;
	// }
	public void setNeedToTryCatch(boolean need) {
		mNeedToCatch = need;
	}
	
	protected View getmMergerFolderChildView() {
		//		return mMergeFolderChildView;
		return null;
	}

	/**
	 * 启动进入文件夹后的缩小动画
	 * 
	 * @param dragView
	 *            　拖拽视图
	 * @param dragView
	 *            　拖拽到目标网格所在原来的视图，用来计算缩小动画的初始数据
	 * @param rect
	 *            　拖拽的矩形
	 * @param position
	 *            放入文件夹第几个图标　从1开始
	 * @return
	 */
	public boolean startIntoFolderZoomoutAnimation(View dragView, View targetView, Rect rect,
			int position) {
		if (null == rect || null == dragView || null == targetView || position <= 0) {
			return false;
		}

		Drawable drawable = null;
		if (dragView instanceof BubbleTextView) {
			// 桌面与文件夹内图标
			drawable = ((BubbleTextView) dragView).getIcon();
		} else if (dragView instanceof DockIconView) {
			Object object = dragView.getTag();
			if (null != object && object instanceof ShortCutInfo) {
				drawable = ((ShortCutInfo) object).mIcon;
				if (null == drawable) {
					drawable = ((ShortCutInfo) object).getFeatureIcon();
				}
			}
		}
		if (drawable != null) {
			try {
				// 新建Drawable的原因是，如果有同样两个程序的图标在屏幕上，实际上drawable指向同一个实例，
				// 在做动画过程中会改变Bounds,会第二个图标
				mMergeFolderZoomoutDrawable = new BitmapDrawable(getResources(),
						((BitmapDrawable) drawable).getBitmap());
			} catch (Exception e) {
				mMergeFolderZoomoutDrawable = null;
			}
		} else {
			mMergeFolderZoomoutDrawable = null;
		}

		if (null != mMergeFolderZoomoutDrawable) {
			if (targetView instanceof BubbleTextView) {
				// 计算targetRect
				mTargetZoomoutRect = ScreenUtils.getAIconRectInAFolder(position,
						(BubbleTextView) targetView);

				// 计算srcRect
				mSrcZoomoutRect = ScreenUtils.getZoomoutSrcRect(rect, dragView);

				if (null != mSrcZoomoutRect && null != mTargetZoomoutRect) {
					// 开始动画
					return true;
				}
			}
		}
		mMergeFolderZoomoutDrawable = null;
		mTargetZoomoutRect = null;
		mSrcZoomoutRect = null;

		return false;
	}

	public void setmDrawStatus(int status) {
		mDrawStatus = status;
	}

	/**
	 * 计算打开文件夹上、下移的区域 return int mFolderTop folder的上边界
	 */
	protected int calulateOpenFolderPositionData(int folderheight) {
		int folderBottomMargin = getContext().getResources().getDimensionPixelSize(
				R.dimen.folder_margin_botton);

		mFolderTop = mClipLine;
		mFolderBottom = mFolderTop + folderheight;
		// 如果超出底边界，则上移
		if (mFolderBottom > getHeight() - folderBottomMargin) {
			mFolderBottom = getHeight() - folderBottomMargin;
			mFolderTop = mFolderBottom - folderheight;
		}
		return mFolderTop;
	}

	protected void updateItemInfoScreenIndex() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child != null) {
				Object tagObject = child.getTag();
				if (tagObject != null && tagObject instanceof ItemInfo) {
					((ItemInfo) tagObject).mScreenIndex = getScreen();
				}
			}
		}
	}

	/*------------------------以下code为绘制点击效果-----------------------------*/

	// 当前选中的BubbleTextView，绘制点击效果时用到
	private BubbleTextView mPressedOrFocusedIcon;

	@Override
	public void setPressedOrFocusedIcon(BubbleTextView icon) {
		// We draw the pressed or focused BubbleTextView's background in
		// CellLayout because it
		// requires an expanded clip rect (due to the glow's blur radius)
		BubbleTextView oldIcon = mPressedOrFocusedIcon;
		mPressedOrFocusedIcon = icon;
		if (oldIcon != null) {
			invalidateBubbleTextView(oldIcon);
		}
		if (mPressedOrFocusedIcon != null) {
			invalidateBubbleTextView(mPressedOrFocusedIcon);
		}
	}

	@Override
	public void invalidateBubbleTextView(BubbleTextView icon) {
		final int padding = icon.getPressedOrFocusedBackgroundPadding();
		invalidate(icon.getLeft() + getPaddingLeft() - padding, icon.getTop() + getPaddingTop()
				- padding, icon.getRight() + getPaddingLeft() + padding, icon.getBottom()
				+ getPaddingTop() + padding);
	}

	@Override
	public void drawSelectedBorder(Canvas canvas) {
		// We draw the pressed or focused BubbleTextView's background in
		// CellLayout because it
		// requires an expanded clip rect (due to the glow's blur radius)
		canvas.save();
		if (mPressedOrFocusedIcon != null) {
			final int padding = mPressedOrFocusedIcon.getPressedOrFocusedBackgroundPadding();
			final Bitmap b = mPressedOrFocusedIcon.getPressedOrFocusedBackground();
			if (b != null) {
				canvas.drawBitmap(b, mPressedOrFocusedIcon.getLeft() + getPaddingLeft() - padding,
						mPressedOrFocusedIcon.getTop() + getPaddingTop() - padding, null);
			}
		}
		canvas.restore();
	}

	/*--------------------------以下code为绘制图标轮廓---------------------------------*/
	// 画图标轮廓
	private int mDragOutlineColor;
	private final Rect mTempRect = new Rect();
	private int[] mTargetCell = new int[2];
	private Bitmap mOutLineBitmap = null;
	// 是否绘制图标轮廓的标记
	private boolean mOutlineVisible = true;

	protected void caculateCellXY(View dragView, int[] locXY, int originX, int originY) {
		if (mDrawStatus != DRAW_STATUS_NORMAL || dragView == null) {
			mOutlineVisible = false;
			return;
		}
		visualizeDropLocation(dragView, locXY, originX, originY);
	}

	public void setOutlineVisible(boolean visible) {
		if (mOutlineVisible != visible) {
			mOutlineVisible = visible;
			mTargetCell[0] = mTargetCell[1] = -1;
			clearDragOutlinesRect();
			postInvalidate();
		}
	}

	// 清除轮廓相关信息
	public void clearVisualizeDropLocation() {
		if (mOutLineBitmap != null) {
			mOutLineBitmap.recycle();
			mOutLineBitmap = null;
			mOutlineVisible = true;
//			postInvalidate();
		}
		mTargetCell[0] = mTargetCell[1] = -1;
		clearDragOutlinesRect();
	}

	private void clearDragOutlinesRect() {
		final int oldIndex = mDragOutlineCurrent;
		mDragOutlineAnims[oldIndex].animateOut();
		mDragOutlineCurrent = (oldIndex + 1) % mDragOutlineAnims.length;
		for (int i = 0; i < mDragOutlines.length; i++) {
			//ADT-9502 防止小米手机合并图标为文件夹时屏幕不断闪烁(不能将宽高设为0)
			mDragOutlines[i].set(-4, -3, -2, -1);
		}
	}
	
	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize
	 * the drop location. Responsibility for the bitmap is transferred to the
	 * caller.
	 */
	private Bitmap createDragOutline(View v, Canvas canvas, int padding) {
		int width = v.getWidth();
		int height = v.getHeight();

		boolean pruneToDrawable = true;
		if (v instanceof BubbleTextView) {
			Drawable drawable = ((BubbleTextView) v).getIcon()/*
																 * getCompoundDrawables
																 * ()[1]
																 */;
			if (drawable == null) {
				pruneToDrawable = false;
			} else {
				Rect bount = drawable.getBounds();
				width = bount.width();
				height = bount.height();
			}
		}
		Bitmap bitmap = null;
		// 创建前先回收一次内存
		System.gc();
		try {
			bitmap = Bitmap
					.createBitmap(width + padding, height + padding, Bitmap.Config.ARGB_8888);
			canvas.setBitmap(bitmap);
			CellLayout.drawDragView(mTempRect, v, canvas, padding, pruneToDrawable);
			HolographicOutlineHelper outlineHelper = GOLauncherApp.getOutlineHelper();
			outlineHelper.applyMediumExpensiveOutlineWithBlur(bitmap, canvas, mDragOutlineColor,
					mDragOutlineColor);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}

		if (Machine.isIceCreamSandwichOrHigherSdk()) {
			canvas.setBitmap(null);
		}
		return bitmap;
	}

	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize
	 * the drop location. Responsibility for the bitmap is transferred to the
	 * caller.
	 */
	private Bitmap createWidgetDragOutline(View v, Canvas canvas, int padding) {

		AbsWidgetInfo info = (AbsWidgetInfo) v.getTag();
		int width = info.mCol * CellLayout.sCellRealWidth;
		int height = info.mRow * CellLayout.sCellRealHeight;

		mTempRect.set(0, 0, width, height);
		Bitmap bitmap = null;
		// 创建前先回收一次内存
		System.gc();
		try {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			canvas.setBitmap(bitmap);
			canvas.clipRect(mTempRect);
			canvas.translate(-v.getScrollX() + sWidgetCustomPadding, -v.getScrollY()
					+ sWidgetCustomPadding);
			// v.draw(canvas);

			HolographicOutlineHelper outlineHelper = GOLauncherApp.getOutlineHelper();
			outlineHelper.applyMediumExpensiveOutlineWithBlur(bitmap, canvas, mDragOutlineColor,
					mDragOutlineColor);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}

		if (Machine.isIceCreamSandwichOrHigherSdk()) {
			canvas.setBitmap(null);
		}
		return bitmap;
	}

	void visualizeDropLocation(View v, int[] targetCell, int originX, int originY) {
		if (v == null || targetCell == null || targetCell[0] < 0 || targetCell[1] < 0) {
			clearVisualizeDropLocation();
			revertTempState();
			return;
		}

		final int bitmapPadding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
		if (mOutLineBitmap == null && mOutlineVisible) {
			final Canvas canvas = new Canvas();
			// We need to add extra padding to the bitmap to make room for the
			// glow effect
			if (v.getTag() != null && v.getTag() instanceof AbsWidgetInfo) {
				// widget单独处理
				mOutLineBitmap = createWidgetDragOutline(v, canvas, bitmapPadding);
			} else {
				mOutLineBitmap = createDragOutline(v, canvas, bitmapPadding);
			}
		}

		// 如果创建投影视图失败，则不显示
		if (mOutLineBitmap == null) {
			mOutlineVisible = false;
			return;
		}

		int leftMargin = 0;
		int topMargin = 0;

		if (v.getLayoutParams() != null && v.getLayoutParams() instanceof MarginLayoutParams) {
			MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
			if (v instanceof BubbleTextView) {
				// leftMargin = lp.leftMargin + (mCellRealWidth -
				// mOutLineBitmap.getWidth()) / 2;
				// topMargin = lp.topMargin;
				if (sAutoStretch) {
					leftMargin = (sCellRealWidth - mOutLineBitmap.getWidth()) >> 1;
//		            topMargin = (mCellRealHeight - v.getHeight()) >> 1;
					topMargin = ((sCellRealHeight - v.getHeight()) >> 1) + v.getPaddingTop() - bitmapPadding / 2;
				} else {
					leftMargin = lp.leftMargin;
					topMargin = lp.topMargin;
				}
			} else {
				leftMargin = lp.leftMargin - bitmapPadding / 2;
				topMargin = lp.topMargin - bitmapPadding / 2;
			}
		} else {
			if (v instanceof BubbleTextView) {
				leftMargin = (sCellRealWidth - mOutLineBitmap.getWidth()) >> 1;
//	          	topMargin = (mCellRealHeight - v.getHeight()) >> 1;
				topMargin = ((sCellRealHeight - v.getHeight()) >> 1) + v.getPaddingTop() - bitmapPadding / 2;
			} else if (v instanceof DockIconView) {
				leftMargin = (sCellRealWidth - mOutLineBitmap.getWidth()) >> 1;
				topMargin = (sCellRealHeight - mAppBoxInnerHeight) >> 1;
			} else {
				// TODO: 获取dragView的边距
				leftMargin = ((sCellRealWidth - v.getWidth()) / 2) - bitmapPadding / 2;
				topMargin = ((sCellRealHeight - v.getHeight()) / 2) - bitmapPadding / 2;
			}
		}

		final int width = (int) (sAutoStretch ? sCellRealWidth : sCellWidth + sWidthGap);
		final int height = (int) (sAutoStretch ? sCellRealHeight : sCellHeight + sHeightGap);
		
		int newX = getLeftPadding() + targetCell[0] * width + leftMargin;
		int newY = getTopPadding() + targetCell[1] * height + topMargin;

		mDragCenter.set(originX + (v.getWidth() / 2), originY + (v.getHeight() / 2));
		if (newX != mTargetCell[0] || newY != mTargetCell[1]) {
			mTargetCell[0] = newX;
			mTargetCell[1] = newY;
			final int oldIndex = mDragOutlineCurrent;
			mDragOutlineAnims[oldIndex].animateOut();
			mDragOutlineCurrent = (oldIndex + 1) % mDragOutlines.length;
			Rect r = mDragOutlines[mDragOutlineCurrent];
			r.set(mTargetCell[0], mTargetCell[1],
					mTargetCell[0] + mOutLineBitmap.getWidth(), mTargetCell[1] + mOutLineBitmap.getHeight());
//          if (resize) {
//              cellToRect(cellX, cellY, spanX, spanY, r);
//          }
//			mDragOutlineAnims[mDragOutlineCurrent].setTag(mOutLineBitmap);
			mDragOutlineAnims[mDragOutlineCurrent].animateIn();
//			postInvalidate();
		}
		
          
	}

	public static void drawDragView(Rect clipRect, View v, Canvas destCanvas, int padding,
			boolean pruneToDrawable) {
		v.getDrawingRect(clipRect);
		destCanvas.save();

		if (v instanceof BubbleTextView && pruneToDrawable) {
			Drawable d = ((BubbleTextView) v).getIcon()/*
														* ((TextView)
														* v).getCompoundDrawables
														* ()[1]
														*/;
			clipRect.set(0, 0, d.getIntrinsicWidth() + padding, d.getIntrinsicHeight() + padding);
			destCanvas.translate(padding / 2, padding / 2);
			final int size = Utilities.getStandardIconSize(v.getContext());
			d.setBounds(0, 0, size, size);
			d.draw(destCanvas);
		} else {
			if (v instanceof BubbleTextView) {
				final BubbleTextView tv = (BubbleTextView) v;
				clipRect.bottom = tv.getLineTop() - (int) BubbleTextView.PADDING_V;
			}
			destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
			destCanvas.clipRect(clipRect, Op.REPLACE);
			v.draw(destCanvas);
		}
		destCanvas.restore();
	}

	/**
	 * @param mClipLine
	 *            the mClipLine to set
	 */
	public void setmClipLine(int clipLine) {
		this.mClipLine = clipLine;
	}

	public boolean isOccupied(int cellX, int cellY) {
		return mOccupied[cellX][cellY];
	}

	public void setLayoutScale(float scale) {
		mLayoutScale = scale;
	}

	/**
	 * 初始化背景及九切图边距
	 */
	private void initBackgroundAndPadding() {
		final Resources resources = getResources();
		if (mState == STATE_NORMAL_CONTENT) {
			mAddDrawable = null;
			mLightBackground = null;
			mBackground = mNormalBackground = resources
					.getDrawable(R.drawable.edit_layout_background);
			mFullBackground = resources.getDrawable(R.drawable.edit_layout_background_full);
			mGridCross = resources.getDrawable(R.drawable.drag_gird_point);
		} else if (mState == STATE_BLANK_CONTENT) {
			mFullBackground = null;
			mGridCross = null;
			mBackground = mNormalBackground = resources
					.getDrawable(R.drawable.workspace_edit_add_bg);
			mAddDrawable = resources.getDrawable(R.drawable.workspace_edit_add_cross);
			mLightBackground = resources.getDrawable(R.drawable.edit_layout_background_light);
		}
		if (mBackground != null) {
			// 获取九切图的边距
			mBackground.getPadding(mBgRect);
		}
	}

	protected void drawBackground(Canvas canvas) {
		if (mLayoutScale >= 1.0f && !mDrawBackground) {
			return;
		}
		// TODO:满屏的时候画红色
		canvas.save();
		if (mBackground != null) {
			canvas.translate(-mBgRect.left - getLeftPadding(), -mBgRect.top + getTopPadding());
			mBackground.setBounds(0, 0, mBgRect.left + getWidth() + mBgRect.right
					- getLeftPadding() - getRightPadding(), getHeight() + mBgRect.top
					+ mBgRect.bottom - getTopPadding() - getBottomPadding());
			mBackground.setAlpha(mBackgroudAlpha);
			mBackground.draw(canvas);
		}
		canvas.restore();
	} // end drawBackground

	// 画十字架
	protected void drawCenterCross(Canvas canvas) {
		if (mState == STATE_BLANK_CONTENT && mAddDrawable != null) {
			canvas.save();
			canvas.translate((getWidth() * mLayoutScale - mAddDrawable.getIntrinsicWidth()) / 2,
					(getHeight() * mLayoutScale - getBottomPadding() - mAddDrawable
							.getIntrinsicHeight()) / 2);
			mAddDrawable.setBounds(0, 0, mAddDrawable.getIntrinsicWidth(),
					mAddDrawable.getIntrinsicHeight());
			mAddDrawable.draw(canvas);
			canvas.restore();
		}
	}

	public void changeBackground(boolean isFull) {
		if (mState == STATE_NORMAL_CONTENT) {
			mBackground = isFull ? mFullBackground : mNormalBackground;
			postInvalidate();
		}
	}

	public int getState() {
		return mState;
	}

	public void setDrawBackground(boolean draw) {
		mDrawBackground = draw;
	}

	public void setDrawExtraFlag(int flag) {
		if (mDrawExtraFlag != flag) {
			mDrawExtraFlag = flag;
			postInvalidate();
		}
	}

	/**
	 * 设置是否绘制网格的方法
	 * 
	 * @param flag
	 */
	public void setDrawCross(boolean flag) {
		int newFlag = mDrawExtraFlag;
		if (flag) {
			newFlag |= DRAW_CROSS_MASK;
		} else {
			newFlag &= ~DRAW_CROSS_MASK;
		}
		setDrawExtraFlag(newFlag);
	}

	/**
	 * 设置是否绘制边框的方法
	 * 
	 * @param flag
	 */
	public void setDrawBorder(boolean flag) {
		int newFlag = mDrawExtraFlag;
		if (flag) {
			newFlag |= DRAW_CURR_EDGE_MASK;
		} else {
			newFlag &= ~DRAW_CURR_EDGE_MASK;
		}
		setDrawExtraFlag(newFlag);
	}

	/**
	 * 绘制边框和网格的方法
	 * 
	 * @param canvas
	 */
	private void drawBorderAndCross(Canvas canvas) {
		final boolean drawCross = (mDrawExtraFlag & DRAW_CROSS_MASK) != 0;
		// 在画网格十字架
		if (drawCross && mGridCross != null) {
			canvas.save();
			final Rect rect = mRect;
			rect.set(getLeftPadding(), getTopPadding(), getWidth() - getRightPadding(), getHeight()
					- getBottomPadding());

			final int countX = getCountX();
			final int countY = getCountY();

			final int cellWidth = sCellRealWidth;
			final int cellHeight = sCellRealHeight;
			final Drawable drawable = mGridCross;
			final int crossWidth = drawable.getIntrinsicWidth();
			final int crossHeight = drawable.getIntrinsicHeight();

			final int minX = rect.left - crossWidth / 2;
			final int minY = rect.top - crossHeight / 2;
			final int maxX = rect.right - crossWidth / 2;
			final int maxY = rect.bottom - crossHeight / 2;

			final float maxAlpha = 1.0f;
			final int maxVisibleDistance = 500;
			final float distanceMultupler = 0.002f;

			int startX = minX;

			for (int col = 0; col <= countX; col++) {
				int x = startX;
				if (col == 0) {
					x = minX + CROSS_LINE_OFFSET;
				} else if (col == countX) {
					x = maxX - CROSS_LINE_OFFSET;
				}

				int startY = minY;
				for (int row = 0; row <= countY; row++) {
					int y = startY;
					if (row == 0) {
						y = minY + CROSS_LINE_OFFSET;
					} else if (row == countY) {
						y = maxY - CROSS_LINE_OFFSET;
					}
					float alpha = 1.0f;
					if (mLayoutScale == 1.0f) {
						mTmpPointF.set(x - mDragCenter.x, y - mDragCenter.y);
						float dist = mTmpPointF.length();
						// Crosshairs further from the drag point are more faint
						alpha = Math.min(maxAlpha, distanceMultupler
								* (maxVisibleDistance - dist));
					}
					if (alpha > 0.0f) {
						drawable.setBounds(x, y, x + crossWidth, y + crossHeight);
						drawable.setAlpha((int) (alpha * 255 * mCrosshairsVisibility));
						drawable.draw(canvas);
					}
					startY += cellHeight;
				}
				startX += cellWidth;
			}
			// 画拖拽图标在网格中的轮廓
			if (mOutlineVisible && null != mOutLineBitmap && !mOutLineBitmap.isRecycled()) {
//				canvas.drawBitmap(mOutLineBitmap, mTargetCell[0], mTargetCell[1], null);
				final Paint paint = mDragOutlinePaint;
				for (int i = 0; i < mDragOutlines.length; i++) {
					final float alpha = mDragOutlineAlphas[i];
					if (alpha > 0) {
						final Rect r = mDragOutlines[i];
//						final Bitmap b = (Bitmap) mDragOutlineAnims[i].getTag();
					    paint.setAlpha((int) (alpha + .5f));
						canvas.drawBitmap(mOutLineBitmap, null, r, paint);
					}
				}
			}
			canvas.restore();
		}
	}

	// BubbleTextView里面显示的图标 + 图文间隙 + 文字 的高度
	private int mAppBoxInnerHeight = -1;

	private void initAppBoxInnerHeight() {
		if (mAppBoxInnerHeight == -1) {
			final Context context = getContext();
			// 5.0f 和 3.0f 是根据WorkspaceIcon中设置的图文间隙得来
			final int drawablePadding = sPortrait ? DrawUtils.dip2px(5.0f) : DrawUtils.dip2px(3.0f);
			// final int textSize = view.getLineHeight();
			// 不根据传入的BubbleTextView， 根据字体大小 +
			// 上下边距值（约为3px），这样可以解决有些屏幕没有BubbleTextView时mAppBoxInnerHeight无法赋值的问题
			final int extraPadding = 3;
			final int textSize = DrawUtils.px2sp(DrawUtils.dip2px(13.0f)) // 13.0f是根据WorkspaceIcon中设置的字体大小参数得来的
					+ extraPadding;
			mAppBoxInnerHeight = Utilities.getStandardIconSize(context) + drawablePadding + textSize;
		}
	}// end initAppBoxInnerHeight

	/*-----------------------------以下代码与item的挤压相关 by Yugi 2012-08-06-----------------------------*/
	public static final int MODE_DRAG_OVER = 0;
	public static final int MODE_ON_DROP = 1;
	public static final int MODE_ON_DROP_EXTERNAL = 2;
	public static final int MODE_ACCEPT_DROP = 3;
	private static final int INVALID_DIRECTION = -100;
	private ArrayList<View> mIntersectingViews = new ArrayList<View>();
	private Rect mOccupiedRect = new Rect();
	private int[] mDirectionVector = new int[2];
	int[] mPreviousReorderDirection = new int[2];
	private final int[] mTmpPoint = new int[2];
	private final int[] mTmpXY = new int[2];
	int[] mTempLocation = new int[2];
	boolean[][] mTmpOccupied;
	private static final float REORDER_HINT_MAGNITUDE = 0.12f;
	private static final int REORDER_ANIMATION_DURATION = 150;
	private float mReorderHintAnimationMagnitude;
	private static final boolean DESTRUCTIVE_REORDER = false;
	private HashMap<CellLayout.LayoutParams, Animator> mReorderAnimators = new HashMap<CellLayout.LayoutParams, Animator>();
	private HashMap<View, ReorderHintAnimation> mShakeAnimators = new HashMap<View, ReorderHintAnimation>();
	// 是否需要保留边距的标识
	boolean mNeedPaddingH = false;
	boolean mNeedPaddingV = false;
	private boolean mItemPlacementDirty = false;
	private ArrayList<FolderRingAnimator> mFolderOuterRings = new ArrayList<FolderRingAnimator>();

	/**
	 * 
	 * @author jiangxuwen
	 *
	 */
	//CHECKSTYLE:OFF
	private class CellAndSpan {
		int x, y;
		int spanX, spanY;

		public CellAndSpan(int x, int y, int spanX, int spanY) {
			this.x = x;
			this.y = y;
			this.spanX = spanX;
			this.spanY = spanY;
		}
	} // end CellAndSpan

	/**
	 * 
	 * @author jiangxuwen
	 *
	 */
	private class ItemConfiguration {
		HashMap<View, CellAndSpan> map = new HashMap<View, CellAndSpan>();
		boolean isSolution = false;
		int dragViewX, dragViewY, dragViewSpanX, dragViewSpanY;

		int area() {
			return dragViewSpanX * dragViewSpanY;
		}
	} // end ItemConfiguration
	//CHECKSTYLE:ON
	
	private void copyCurrentStateToSolution(ItemConfiguration solution, boolean temp) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			CellAndSpan c;
			if (temp) {
				c = new CellAndSpan(lp.tmpCellX, lp.tmpCellY, lp.cellHSpan, lp.cellVSpan);
			} else {
				c = new CellAndSpan(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan);
			}
			solution.map.put(child, c);
		}
	} // end copyCurrentStateToSolution

	private void copyOccupiedArray(boolean[][] occupied) {
		for (int i = 0; i < getCountX(); i++) {
			for (int j = 0; j < getCountY(); j++) {
				occupied[i][j] = mOccupied[i][j];
			}
		}
	} // end copyOccupiedArray

	private void animateItemsToSolution(ItemConfiguration solution, View dragView,
			boolean commitDragView) {

		boolean[][] occupied = DESTRUCTIVE_REORDER ? mOccupied : mTmpOccupied;
		for (int i = 0; i < getCountX(); i++) {
			for (int j = 0; j < getCountY(); j++) {
				occupied[i][j] = false;
			}
		}

		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child == dragView) {
				continue;
			}
			CellAndSpan c = solution.map.get(child);
			if (c != null) {
				animateChildToPosition(child, c.x, c.y, REORDER_ANIMATION_DURATION, 0,
						DESTRUCTIVE_REORDER, false);
				markCellsForView(c.x, c.y, c.spanX, c.spanY, occupied, true);
			}
		}
		if (commitDragView) {
			markCellsForView(solution.dragViewX, solution.dragViewY, solution.dragViewSpanX,
					solution.dragViewSpanY, occupied, true);
		}
	}

	public boolean animateChildToPosition(final View child, int cellX, int cellY, int duration,
			int delay, boolean permanent, boolean adjustOccupied) {
		boolean[][] occupied = mOccupied;
		if (!permanent) {
			occupied = mTmpOccupied;
		}

		if (indexOfChild(child) != -1) {
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();
			final ItemInfo info = (ItemInfo) child.getTag();

			// We cancel any existing animations
			if (mReorderAnimators.containsKey(lp)) {
				mReorderAnimators.get(lp).cancel();
				mReorderAnimators.remove(lp);
			}

			final int oldX = lp.x;
			final int oldY = lp.y;
			if (adjustOccupied) {
				occupied[lp.cellX][lp.cellY] = false;
				occupied[cellX][cellY] = true;
			}
			lp.isLockedToGrid = true;
			if (permanent) {
				lp.cellX = info.mCellX = cellX;
				lp.cellY = info.mCellY = cellY;
			} else {
				lp.tmpCellX = cellX;
				lp.tmpCellY = cellY;
			}
			setupLp(lp, child instanceof BubbleTextView);
			lp.isLockedToGrid = false;
			final int newX = lp.x;
			final int newY = lp.y;

			lp.x = oldX;
			lp.y = oldY;

			// Exit early if we're not actually moving the view
			if (oldX == newX && oldY == newY) {
				lp.isLockedToGrid = true;
				return true;
			}

			ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
			va.setDuration(duration);
			mReorderAnimators.put(lp, va);

			va.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float r = ((Float) animation.getAnimatedValue()).floatValue();
					lp.x = (int) ((1 - r) * oldX + r * newX);
					lp.y = (int) ((1 - r) * oldY + r * newY);
					child.requestLayout();
				}
			});
			va.addListener(new AnimatorListenerAdapter() {
				boolean mCancelled = false;

				@Override
				public void onAnimationEnd(Animator animation) {
					// If the animation was cancelled, it means that another
					// animation
					// has interrupted this one, and we don't want to lock the
					// item into
					// place just yet.
					if (!mCancelled) {
						lp.isLockedToGrid = true;
						child.requestLayout();
					}
					if (mReorderAnimators.containsKey(lp)) {
						mReorderAnimators.remove(lp);
					}
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					mCancelled = true;
				}
			});
			va.setStartDelay(delay);
			va.start();
			return true;
		}
		return false;
	}

	private boolean rearrangementExists(int cellX, int cellY, int spanX, int spanY,
			int[] direction, View ignoreView, ItemConfiguration solution) {
		// Return early if get invalid cell positions
		if (cellX < 0 || cellY < 0) {
			return false;
		}

		mIntersectingViews.clear();
		mOccupiedRect.set(cellX, cellY, cellX + spanX, cellY + spanY);

		// Mark the desired location of the view currently being dragged.
		if (ignoreView != null) {
			CellAndSpan c = solution.map.get(ignoreView);
			if (c != null) {
				c.x = cellX;
				c.y = cellY;
			}
		}
		Rect r0 = new Rect(cellX, cellY, cellX + spanX, cellY + spanY);
		Rect r1 = new Rect();
		for (View child : solution.map.keySet()) {
			if (child == ignoreView) {
				continue;
			}
			CellAndSpan c = solution.map.get(child);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			r1.set(c.x, c.y, c.x + c.spanX, c.y + c.spanY);
			if (Rect.intersects(r0, r1)) {
				if (!lp.canReorder) {
					return false;
				}
				mIntersectingViews.add(child);
			}
		}

		// First we try to find a solution which respects the push mechanic.
		// That is,
		// we try to find a solution such that no displaced item travels through
		// another item
		// without also displacing that item.
		if (attemptPushInDirection(mIntersectingViews, mOccupiedRect, direction, ignoreView,
				solution)) {
			return true;
		}

		// Next we try moving the views as a block, but without requiring the
		// push mechanic.
		if (addViewsToTempLocation(mIntersectingViews, mOccupiedRect, direction, false, ignoreView,
				solution)) {
			return true;
		}

		// Ok, they couldn't move as a block, let's move them individually
		for (View v : mIntersectingViews) {
			if (!addViewToTempLocation(v, mOccupiedRect, direction, solution)) {
				return false;
			}
		}
		return true;
	} // end rearrangementExists

	private boolean addViewToTempLocation(View v, Rect rectOccupiedByPotentialDrop,
			int[] direction, ItemConfiguration currentState) {
		CellAndSpan c = currentState.map.get(v);
		boolean success = false;
		markCellsForView(c.x, c.y, c.spanX, c.spanY, mTmpOccupied, false);
		markCellsForRect(rectOccupiedByPotentialDrop, mTmpOccupied, true);

		findNearestArea(c.x, c.y, c.spanX, c.spanY, direction, mTmpOccupied, null, mTempLocation);

		if (mTempLocation[0] >= 0 && mTempLocation[1] >= 0) {
			c.x = mTempLocation[0];
			c.y = mTempLocation[1];
			success = true;

		}
		markCellsForView(c.x, c.y, c.spanX, c.spanY, mTmpOccupied, true);
		return success;
	} // end addViewToTempLocation

	// This method tries to find a reordering solution which satisfies the push
	// mechanic by trying
	// to push items in each of the cardinal directions, in an order based on
	// the direction vector
	// passed.
	private boolean attemptPushInDirection(ArrayList<View> intersectingViews, Rect occupied,
			int[] direction, View ignoreView, ItemConfiguration solution) {
		if ((Math.abs(direction[0]) + Math.abs(direction[1])) > 1) {
			// If the direction vector has two non-zero components, we try
			// pushing
			// separately in each of the components.
			int temp = direction[1];
			direction[1] = 0;
			if (addViewsToTempLocation(intersectingViews, occupied, direction, true, ignoreView,
					solution)) {
				return true;
			}
			direction[1] = temp;
			temp = direction[0];
			direction[0] = 0;
			if (addViewsToTempLocation(intersectingViews, occupied, direction, true, ignoreView,
					solution)) {
				return true;
			}
			// Revert the direction
			direction[0] = temp;

			// Now we try pushing in each component of the opposite direction
			direction[0] *= -1;
			direction[1] *= -1;
			temp = direction[1];
			direction[1] = 0;
			if (addViewsToTempLocation(intersectingViews, occupied, direction, true, ignoreView,
					solution)) {
				return true;
			}

			direction[1] = temp;
			temp = direction[0];
			direction[0] = 0;
			if (addViewsToTempLocation(intersectingViews, occupied, direction, true, ignoreView,
					solution)) {
				return true;
			}
			// revert the direction
			direction[0] = temp;
			direction[0] *= -1;
			direction[1] *= -1;

		} else {
			// If the direction vector has a single non-zero component, we push
			// first in the
			// direction of the vector
			if (addViewsToTempLocation(intersectingViews, occupied, direction, true, ignoreView,
					solution)) {
				return true;
			}

			// Then we try the opposite direction
			direction[0] *= -1;
			direction[1] *= -1;
			if (addViewsToTempLocation(intersectingViews, occupied, direction, true, ignoreView,
					solution)) {
				return true;
			}
			// Switch the direction back
			direction[0] *= -1;
			direction[1] *= -1;

			// If we have failed to find a push solution with the above, then we
			// try
			// to find a solution by pushing along the perpendicular axis.

			// Swap the components
			int temp = direction[1];
			direction[1] = direction[0];
			direction[0] = temp;
			if (addViewsToTempLocation(intersectingViews, occupied, direction, true, ignoreView,
					solution)) {
				return true;
			}

			// Then we try the opposite direction
			direction[0] *= -1;
			direction[1] *= -1;
			if (addViewsToTempLocation(intersectingViews, occupied, direction, true, ignoreView,
					solution)) {
				return true;
			}
			// Switch the direction back
			direction[0] *= -1;
			direction[1] *= -1;

			// Swap the components back
			temp = direction[1];
			direction[1] = direction[0];
			direction[0] = temp;
		}
		return false;
	} // end attemptPushInDirection

	private boolean addViewsToTempLocation(ArrayList<View> views, Rect rectOccupiedByPotentialDrop,
			int[] direction, boolean push, View dragView, ItemConfiguration currentState) {
		if (views.size() == 0) {
			return true;
		}

		boolean success = false;
		Rect boundingRect = null;
		// We construct a rect which represents the entire group of views passed
		// in
		for (View v : views) {
			CellAndSpan c = currentState.map.get(v);
			if (boundingRect == null) {
				boundingRect = new Rect(c.x, c.y, c.x + c.spanX, c.y + c.spanY);
			} else {
				boundingRect.union(c.x, c.y, c.x + c.spanX, c.y + c.spanY);
			}
		}

		@SuppressWarnings("unchecked")
		ArrayList<View> dup = (ArrayList<View>) views.clone();
		// We try and expand the group of views in the direction vector passed,
		// based on
		// whether they are physically adjacent, ie. based on "push mechanics".
		while (push
				&& addViewInDirection(dup, boundingRect, direction, mTmpOccupied, dragView,
						currentState)) {
		}

		// Mark the occupied state as false for the group of views we want to
		// move.
		for (View v : dup) {
			CellAndSpan c = currentState.map.get(v);
			markCellsForView(c.x, c.y, c.spanX, c.spanY, mTmpOccupied, false);
		}

		boolean[][] blockOccupied = new boolean[boundingRect.width()][boundingRect.height()];
		int top = boundingRect.top;
		int left = boundingRect.left;
		// We mark more precisely which parts of the bounding rect are truly
		// occupied, allowing
		// for tetris-style interlocking.
		for (View v : dup) {
			CellAndSpan c = currentState.map.get(v);
			markCellsForView(c.x - left, c.y - top, c.spanX, c.spanY, blockOccupied, true);
		}

		markCellsForRect(rectOccupiedByPotentialDrop, mTmpOccupied, true);

		if (push) {
			findNearestAreaInDirection(boundingRect.left, boundingRect.top, boundingRect.width(),
					boundingRect.height(), direction, mTmpOccupied, blockOccupied, mTempLocation);
		} else {
			findNearestArea(boundingRect.left, boundingRect.top, boundingRect.width(),
					boundingRect.height(), direction, mTmpOccupied, blockOccupied, mTempLocation);
		}

		// If we successfuly found a location by pushing the block of views, we
		// commit it
		if (mTempLocation[0] >= 0 && mTempLocation[1] >= 0) {
			int deltaX = mTempLocation[0] - boundingRect.left;
			int deltaY = mTempLocation[1] - boundingRect.top;
			for (View v : dup) {
				CellAndSpan c = currentState.map.get(v);
				c.x += deltaX;
				c.y += deltaY;
			}
			success = true;
		}

		// In either case, we set the occupied array as marked for the location
		// of the views
		for (View v : dup) {
			CellAndSpan c = currentState.map.get(v);
			markCellsForView(c.x, c.y, c.spanX, c.spanY, mTmpOccupied, true);
		}
		return success;
	} // end addViewsToTempLocation

	private void markCellsForView(int cellX, int cellY, int spanX, int spanY, boolean[][] occupied,
			boolean value) {
		if (cellX < 0 || cellY < 0) {
			return;
		}
		for (int x = cellX; x < cellX + spanX && x < getCountX(); x++) {
			for (int y = cellY; y < cellY + spanY && y < getCountY(); y++) {
				occupied[x][y] = value;
			}
		}
	} // end markCellsForView

	private void markCellsForRect(Rect r, boolean[][] occupied, boolean value) {
		markCellsForView(r.left, r.top, r.width(), r.height(), occupied, value);
	} // end

	// This method looks in the specified direction to see if there is an
	// additional view
	// immediately adjecent in that direction
	private boolean addViewInDirection(ArrayList<View> views, Rect boundingRect, int[] direction,
			boolean[][] occupied, View dragView, ItemConfiguration currentState) {
		boolean found = false;

		int childCount = getChildCount();
		Rect r0 = new Rect(boundingRect);
		Rect r1 = new Rect();

		int deltaX = 0;
		int deltaY = 0;
		if (direction[1] < 0) {
			r0.set(r0.left, r0.top - 1, r0.right, r0.bottom);
			deltaY = -1;
		} else if (direction[1] > 0) {
			r0.set(r0.left, r0.top, r0.right, r0.bottom + 1);
			deltaY = 1;
		} else if (direction[0] < 0) {
			r0.set(r0.left - 1, r0.top, r0.right, r0.bottom);
			deltaX = -1;
		} else if (direction[0] > 0) {
			r0.set(r0.left, r0.top, r0.right + 1, r0.bottom);
			deltaX = 1;
		}

		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (views.contains(child) || child == dragView) {
				continue;
			}
			CellAndSpan c = currentState.map.get(child);

			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			r1.set(c.x, c.y, c.x + c.spanX, c.y + c.spanY);
			if (Rect.intersects(r0, r1)) { // if 两个矩形有交集
				if (!lp.canReorder) {
					return false;
				}
				boolean pushed = false;
				for (int x = c.x; x < c.x + c.spanX; x++) {
					for (int y = c.y; y < c.y + c.spanY; y++) {
						boolean inBounds = x - deltaX >= 0 && x - deltaX < getCountX()
								&& y - deltaY >= 0 && y - deltaY < getCountY();
						if (inBounds && occupied[x - deltaX][y - deltaY]) {
							pushed = true;
						}
					}
				}
				if (pushed) {
					views.add(child);
					boundingRect.union(c.x, c.y, c.x + c.spanX, c.y + c.spanY);
					found = true;
				}
			}
		}
		return found;
	} // end addViewInDirection

	/*
	 * Returns a pair (x, y), where x,y are in {-1, 0, 1} corresponding to
	 * vector between the provided point and the provided cell
	 */
	private void computeDirectionVector(float deltaX, float deltaY, int[] result) {
		double angle = Math.atan(deltaY / deltaX);

		result[0] = 0;
		result[1] = 0;
		if (Math.abs(Math.cos(angle)) > 0.5f) {
			result[0] = (int) Math.signum(deltaX);
		}
		if (Math.abs(Math.sin(angle)) > 0.5f) {
			result[1] = (int) Math.signum(deltaY);
		}
	} // end computeDirectionVector

	/**
	 * Find a vacant area that will fit the given bounds nearest the requested
	 * cell location, and will also weigh in a suggested direction vector of the
	 * desired location. This method computers distance based on unit grid
	 * distances, not pixel distances.
	 * 
	 * @param cellX
	 *            The X cell nearest to which you want to search for a vacant
	 *            area.
	 * @param cellY
	 *            The Y cell nearest which you want to search for a vacant area.
	 * @param spanX
	 *            Horizontal span of the object.
	 * @param spanY
	 *            Vertical span of the object.
	 * @param direction
	 *            The favored direction in which the views should move from x, y
	 * @param exactDirectionOnly
	 *            If this parameter is true, then only solutions where the
	 *            direction matches exactly. Otherwise we find the best matching
	 *            direction.
	 * @param occoupied
	 *            The array which represents which cells in the CellLayout are
	 *            occupied
	 * @param blockOccupied
	 *            The array which represents which cells in the specified block
	 *            (cellX, cellY, spanX, spanY) are occupied. This is used when
	 *            try to move a group of views.
	 * @param result
	 *            Array in which to place the result, or null (in which case a
	 *            new array will be allocated)
	 * @return The X, Y cell of a vacant area that can contain this object,
	 *         nearest the requested location.
	 */
	private int[] findNearestArea(int cellX, int cellY, int spanX, int spanY, int[] direction,
			boolean[][] occupied, boolean blockOccupied[][], int[] result) {
		// Keep track of best-scoring drop area
		final int[] bestXY = result != null ? result : new int[2];
		float bestDistance = Float.MAX_VALUE;
		int bestDirectionScore = Integer.MIN_VALUE;

		final int countX = getCountX();
		final int countY = getCountY();

		for (int y = 0; y < countY - (spanY - 1); y++) {
			inner : for (int x = 0; x < countX - (spanX - 1); x++) {
				// First, let's see if this thing fits anywhere
				for (int i = 0; i < spanX; i++) {
					for (int j = 0; j < spanY; j++) {
						if (occupied[x + i][y + j]
								&& (blockOccupied == null || blockOccupied[i][j])) {
							continue inner;
						}
					}
				}

				float distance = (float) Math.sqrt((x - cellX) * (x - cellX) + (y - cellY)
						* (y - cellY));
				int[] curDirection = mTmpPoint;
				computeDirectionVector(x - cellX, y - cellY, curDirection);
				// The direction score is just the dot product of the two
				// candidate direction
				// and that passed in.
				int curDirectionScore = direction[0] * curDirection[0] + direction[1]
						* curDirection[1];
				boolean exactDirectionOnly = false;
				boolean directionMatches = direction[0] == curDirection[0]
						&& direction[1] == curDirection[1];
				if ((directionMatches || !exactDirectionOnly)
						&& Float.compare(distance, bestDistance) < 0
						|| (Float.compare(distance, bestDistance) == 0 && curDirectionScore > bestDirectionScore)) {
					bestDistance = distance;
					bestDirectionScore = curDirectionScore;
					bestXY[0] = x;
					bestXY[1] = y;
				}
			}
		}

		// Return -1, -1 if no suitable location found
		if (bestDistance == Float.MAX_VALUE) {
			bestXY[0] = -1;
			bestXY[1] = -1;
		}
		return bestXY;
	} // end findNearestArea

	private int[] findNearestAreaInDirection(int cellX, int cellY, int spanX, int spanY,
			int[] direction, boolean[][] occupied, boolean blockOccupied[][], int[] result) {
		// Keep track of best-scoring drop area
		final int[] bestXY = result != null ? result : new int[2];
		bestXY[0] = -1;
		bestXY[1] = -1;
		float bestDistance = Float.MAX_VALUE;

		// We use this to march in a single direction
		if ((direction[0] != 0 && direction[1] != 0) || (direction[0] == 0 && direction[1] == 0)) {
			return bestXY;
		}

		// This will only incrememnet one of x or y based on the assertion above
		int x = cellX + direction[0];
		int y = cellY + direction[1];
		while (x >= 0 && x + spanX <= getCountX() && y >= 0 && y + spanY <= getCountY()) {

			boolean fail = false;
			for (int i = 0; i < spanX; i++) {
				for (int j = 0; j < spanY; j++) {
					if (occupied[x + i][y + j] && (blockOccupied == null || blockOccupied[i][j])) {
						fail = true;
					}
				}
			}
			if (!fail) {
				float distance = (float) Math.sqrt((x - cellX) * (x - cellX) + (y - cellY)
						* (y - cellY));
				if (Float.compare(distance, bestDistance) < 0) {
					bestDistance = distance;
					bestXY[0] = x;
					bestXY[1] = y;
				}
			}
			x += direction[0];
			y += direction[1];
		}
		return bestXY;
	} // end findNearestAreaInDirection

	ItemConfiguration simpleSwap(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX,
			int spanY, int[] direction, View dragView, boolean decX, ItemConfiguration solution) {
		// Copy the current state into the solution. This solution will be
		// manipulated as necessary.
		copyCurrentStateToSolution(solution, false);
		// Copy the current occupied array into the temporary occupied array.
		// This array will be
		// manipulated as necessary to find a solution.
		copyOccupiedArray(mTmpOccupied);

		// We find the nearest cell into which we would place the dragged item,
		// assuming there's
		// nothing in its way.
		int result[] = new int[2];
		result = findNearestArea(pixelX, pixelY, spanX, spanY, result);

		boolean success = false;
		// First we try the exact nearest position of the item being dragged,
		// we will then want to try to move this around to other neighbouring
		// positions
		success = rearrangementExists(result[0], result[1], spanX, spanY, direction, dragView,
				solution);

		if (!success) {
			// We try shrinking the widget down to size in an alternating
			// pattern, shrink 1 in
			// x, then 1 in y etc.
			if (spanX > minSpanX && (minSpanY == spanY || decX)) {
				return simpleSwap(pixelX, pixelY, minSpanX, minSpanY, spanX - 1, spanY, direction,
						dragView, false, solution);
			} else if (spanY > minSpanY) {
				return simpleSwap(pixelX, pixelY, minSpanX, minSpanY, spanX, spanY - 1, direction,
						dragView, true, solution);
			}
			solution.isSolution = false;
		} else {
			solution.isSolution = true;
			solution.dragViewX = result[0];
			solution.dragViewY = result[1];
			solution.dragViewSpanX = spanX;
			solution.dragViewSpanY = spanY;
		}
		return solution;
	} // end simpleSwap

	int[] findNearestArea(int pixelX, int pixelY, int spanX, int spanY, int[] result) {
		return findNearestArea(pixelX, pixelY, spanX, spanY, null, false, result);
	}

	int[] findNearestArea(int pixelX, int pixelY, int spanX, int spanY, View ignoreView,
			boolean ignoreOccupied, int[] result) {
		return findNearestArea(pixelX, pixelY, spanX, spanY, spanX, spanY, ignoreView,
				ignoreOccupied, result, null, mOccupied);
	}

	int[] findNearestVacantArea(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX,
			int spanY, View ignoreView, int[] result, int[] resultSpan) {
		return findNearestArea(pixelX, pixelY, minSpanX, minSpanY, spanX, spanY, ignoreView, true,
				result, resultSpan, mOccupied);
	}

	private final Stack<Rect> mTempRectStack = new Stack<Rect>();

	private void lazyInitTempRectStack() {
		if (mTempRectStack.isEmpty()) {
			for (int i = 0; i < getCountX() * getCountY(); i++) {
				mTempRectStack.push(new Rect());
			}
		}
	}

	private void recycleTempRects(Stack<Rect> used) {
		while (!used.isEmpty()) {
			mTempRectStack.push(used.pop());
		}
	}

	public void markCellsAsOccupiedForView(View view) {
		markCellsAsOccupiedForView(view, mOccupied);
	}

	public void markCellsAsOccupiedForView(View view, boolean[][] occupied) {
		if (view == null || view.getParent() != this) {
			return;
		}
		LayoutParams lp = (LayoutParams) view.getLayoutParams();
		markCellsForView(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, occupied, true);
	}

	public void markCellsAsUnoccupiedForView(View view) {
		markCellsAsUnoccupiedForView(view, mOccupied);
	}

	public void markCellsAsUnoccupiedForView(View view, boolean occupied[][]) {
		if (view == null || view.getParent() != this) {
			return;
		}
		LayoutParams lp = (LayoutParams) view.getLayoutParams();
		markCellsForView(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, occupied, false);
	}

	int[] findNearestArea(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX, int spanY,
			View ignoreView, boolean ignoreOccupied, int[] result, int[] resultSpan,
			boolean[][] occupied) {
		lazyInitTempRectStack();
		// mark space take by ignoreView as available (method checks if
		// ignoreView is null)
		markCellsAsUnoccupiedForView(ignoreView, occupied);

		// For items with a spanX / spanY > 1, the passed in point (pixelX,
		// pixelY) corresponds
		// to the center of the item, but we are searching based on the top-left
		// cell, so
		// we translate the point over to correspond to the top-left.
		pixelX -= (sCellWidth + sWidthGap) * (spanX - 1) / 2f;
		pixelY -= (sCellHeight + sHeightGap) * (spanY - 1) / 2f;

		// Keep track of best-scoring drop area
		final int[] bestXY = result != null ? result : new int[2];
		double bestDistance = Double.MAX_VALUE;
		final Rect bestRect = new Rect(-1, -1, -1, -1);
		final Stack<Rect> validRegions = new Stack<Rect>();

		final int countX = getCountX();
		final int countY = getCountY();

		if (minSpanX <= 0 || minSpanY <= 0 || spanX <= 0 || spanY <= 0 || spanX < minSpanX
				|| spanY < minSpanY) {
			return bestXY;
		}

		for (int y = 0; y < countY - (minSpanY - 1); y++) {
			inner : for (int x = 0; x < countX - (minSpanX - 1); x++) {
				int ySize = -1;
				int xSize = -1;
				if (ignoreOccupied) {
					// First, let's see if this thing fits anywhere
					for (int i = 0; i < minSpanX; i++) {
						for (int j = 0; j < minSpanY; j++) {
							if (occupied[x + i][y + j]) {
								continue inner;
							}
						}
					}
					xSize = minSpanX;
					ySize = minSpanY;

					// We know that the item will fit at _some_ acceptable size,
					// now let's see
					// how big we can make it. We'll alternate between
					// incrementing x and y spans
					// until we hit a limit.
					boolean incX = true;
					boolean hitMaxX = xSize >= spanX;
					boolean hitMaxY = ySize >= spanY;
					while (!(hitMaxX && hitMaxY)) {
						if (incX && !hitMaxX) {
							for (int j = 0; j < ySize; j++) {
								if (x + xSize > countX - 1 || occupied[x + xSize][y + j]) {
									// We can't move out horizontally
									hitMaxX = true;
								}
							}
							if (!hitMaxX) {
								xSize++;
							}
						} else if (!hitMaxY) {
							for (int i = 0; i < xSize; i++) {
								if (y + ySize > countY - 1 || occupied[x + i][y + ySize]) {
									// We can't move out vertically
									hitMaxY = true;
								}
							}
							if (!hitMaxY) {
								ySize++;
							}
						}
						hitMaxX |= xSize >= spanX;
						hitMaxY |= ySize >= spanY;
						incX = !incX;
					}
					incX = true;
					hitMaxX = xSize >= spanX;
					hitMaxY = ySize >= spanY;
				}
				final int[] cellXY = mTmpXY;
				cellToCenterPoint(x, y, cellXY);

				// We verify that the current rect is not a sub-rect of any of
				// our previous
				// candidates. In this case, the current rect is disqualified in
				// favour of the
				// containing rect.
				Rect currentRect = mTempRectStack.pop();
				currentRect.set(x, y, x + xSize, y + ySize);
				boolean contained = false;
				for (Rect r : validRegions) {
					if (r.contains(currentRect)) {
						contained = true;
						break;
					}
				}
				validRegions.push(currentRect);
				double distance = Math.sqrt(Math.pow(cellXY[0] - pixelX, 2)
						+ Math.pow(cellXY[1] - pixelY, 2));

				if ((distance <= bestDistance && !contained) || currentRect.contains(bestRect)) {
					bestDistance = distance;
					bestXY[0] = x;
					bestXY[1] = y;
					if (resultSpan != null) {
						resultSpan[0] = xSize;
						resultSpan[1] = ySize;
					}
					bestRect.set(currentRect);
				}
			}
		}
		// re-mark space taken by ignoreView as occupied
		markCellsAsOccupiedForView(ignoreView, occupied);

		// Return -1, -1 if no suitable location found
		if (bestDistance == Double.MAX_VALUE) {
			bestXY[0] = -1;
			bestXY[1] = -1;
		}
		recycleTempRects(validRegions);
		return bestXY;
	} // end

	void regionToRect(int cellX, int cellY, int spanX, int spanY, Rect result) {
		final int hStartPadding = getLeftPadding();
		final int vStartPadding = getTopPadding();
		final int left = (int) (hStartPadding + cellX * (sCellWidth + sWidthGap));
		final int top = (int) (vStartPadding + cellY * (sCellHeight + sHeightGap));
		result.set(left, top, (int) (left + (spanX * sCellWidth + (spanX - 1) * sWidthGap)),
				(int) (top + (spanY * sCellHeight + (spanY - 1) * sHeightGap)));
	}

	// For a given cell and span, fetch the set of views intersecting the
	// region.
	private void getViewsIntersectingRegion(int cellX, int cellY, int spanX, int spanY,
			View dragView, Rect boundingRect, ArrayList<View> intersectingViews) {
		if (boundingRect != null) {
			boundingRect.set(cellX, cellY, cellX + spanX, cellY + spanY);
		}
		intersectingViews.clear();
		Rect r0 = new Rect(cellX, cellY, cellX + spanX, cellY + spanY);
		Rect r1 = new Rect();
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child == dragView) {
				continue;
			}
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			r1.set(lp.cellX, lp.cellY, lp.cellX + lp.cellHSpan, lp.cellY + lp.cellVSpan);
			if (Rect.intersects(r0, r1)) {
				mIntersectingViews.add(child);
				if (boundingRect != null) {
					boundingRect.union(r1);
				}
			}
		}
	}

	private void getDirectionVectorForDrop(int dragViewCenterX, int dragViewCenterY, int spanX,
			int spanY, View dragView, int[] resultDirection) {
		int[] targetDestination = new int[2];

		findNearestArea(dragViewCenterX, dragViewCenterY, spanX, spanY, targetDestination);
		Rect dragRect = new Rect();
		regionToRect(targetDestination[0], targetDestination[1], spanX, spanY, dragRect);
		dragRect.offset(dragViewCenterX - dragRect.centerX(), dragViewCenterY - dragRect.centerY());

		Rect dropRegionRect = new Rect();
		getViewsIntersectingRegion(targetDestination[0], targetDestination[1], spanX, spanY,
				dragView, dropRegionRect, mIntersectingViews);

		int dropRegionSpanX = dropRegionRect.width();
		int dropRegionSpanY = dropRegionRect.height();

		regionToRect(dropRegionRect.left, dropRegionRect.top, dropRegionRect.width(),
				dropRegionRect.height(), dropRegionRect);

		int deltaX = (dropRegionRect.centerX() - dragViewCenterX) / spanX;
		int deltaY = (dropRegionRect.centerY() - dragViewCenterY) / spanY;

		if (dropRegionSpanX == getCountX() || spanX == getCountX()) {
			deltaX = 0;
		}
		if (dropRegionSpanY == getCountY() || spanY == getCountY()) {
			deltaY = 0;
		}

		if (deltaX == 0 && deltaY == 0) {
			// No idea what to do, give a random direction.
			resultDirection[0] = 1;
			resultDirection[1] = 0;
		} else {
			computeDirectionVector(deltaX, deltaY, resultDirection);
		}
	}

	void setItemPlacementDirty(boolean dirty) {
		mItemPlacementDirty = dirty;
	}

	boolean isItemPlacementDirty() {
		return mItemPlacementDirty;
	}

	int[] createArea(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX, int spanY,
			View dragView, int[] result, int resultSpan[], int mode) {
		// First we determine if things have moved enough to cause a different
		// layout
		result = findNearestArea(pixelX, pixelY, spanX, spanY, result);

		if (resultSpan == null) {
			resultSpan = new int[2];
		}

		// When we are checking drop validity or actually dropping, we don't
		// recompute the
		// direction vector, since we want the solution to match the preview,
		// and it's possible
		// that the exact position of the item has changed to result in a new
		// reordering outcome.
		if ((mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL || mode == MODE_ACCEPT_DROP)
				&& mPreviousReorderDirection[0] != INVALID_DIRECTION) {
			mDirectionVector[0] = mPreviousReorderDirection[0];
			mDirectionVector[1] = mPreviousReorderDirection[1];
			// We reset this vector after drop
			if (mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL) {
				mPreviousReorderDirection[0] = INVALID_DIRECTION;
				mPreviousReorderDirection[1] = INVALID_DIRECTION;
			}
		} else {
			getDirectionVectorForDrop(pixelX, pixelY, spanX, spanY, dragView, mDirectionVector);
			mPreviousReorderDirection[0] = mDirectionVector[0];
			mPreviousReorderDirection[1] = mDirectionVector[1];
		}

		ItemConfiguration swapSolution = simpleSwap(pixelX, pixelY, minSpanX, minSpanY, spanX,
				spanY, mDirectionVector, dragView, true, new ItemConfiguration());

		// We attempt the approach which doesn't shuffle views at all
		ItemConfiguration noShuffleSolution = findConfigurationNoShuffle(pixelX, pixelY, minSpanX,
				minSpanY, spanX, spanY, dragView, new ItemConfiguration());

		ItemConfiguration finalSolution = null;
		if (swapSolution.isSolution && swapSolution.area() >= noShuffleSolution.area()) {
			finalSolution = swapSolution;
		} else if (noShuffleSolution.isSolution) {
			finalSolution = noShuffleSolution;
		}

		boolean foundSolution = true;
		if (!DESTRUCTIVE_REORDER) {
			setUseTempCoords(true);
		}

		if (finalSolution != null) {
			result[0] = finalSolution.dragViewX;
			result[1] = finalSolution.dragViewY;
			resultSpan[0] = finalSolution.dragViewSpanX;
			resultSpan[1] = finalSolution.dragViewSpanY;

			// If we're just testing for a possible location (MODE_ACCEPT_DROP),
			// we don't bother
			// committing anything or animating anything as we just want to
			// determine if a solution
			// exists
			if (mode == MODE_DRAG_OVER || mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL) {
				if (!DESTRUCTIVE_REORDER) {
					copySolutionToTempState(finalSolution, dragView);
				}
				setItemPlacementDirty(true);
				animateItemsToSolution(finalSolution, dragView, mode == MODE_ON_DROP);

				if (!DESTRUCTIVE_REORDER && (mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL)) {
					commitTempPlacement(dragView, result);
					completeAndClearReorderHintAnimations();
					CompatibleUtil.unwrap(dragView);
					//					setItemPlacementDirty(false);
				} else {
					beginOrAdjustHintAnimations(finalSolution, dragView, REORDER_ANIMATION_DURATION);
				}
			}
		} else {
			foundSolution = false;
			result[0] = result[1] = resultSpan[0] = resultSpan[1] = -1;
		}

		if ((mode == MODE_ON_DROP || !foundSolution) && !DESTRUCTIVE_REORDER) {
			setUseTempCoords(false);
		}

		requestLayout();
		return result;
	} // end createArea

	private void commitTempPlacement(View dragView, int[] dragViewXY) {
		for (int i = 0; i < getCountX(); i++) {
			for (int j = 0; j < getCountY(); j++) {
				mOccupied[i][j] = mTmpOccupied[i][j];
			}
		}
		ArrayList<ItemInfo> itemInfos = new ArrayList<ItemInfo>();
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child != null) {
				LayoutParams lp = (LayoutParams) child.getLayoutParams();
				ItemInfo info = (ItemInfo) child.getTag();
				if (info != null) {
					if (child == dragView) {
						lp.cellX = lp.tmpCellX = dragViewXY[0];
						lp.cellY = lp.tmpCellY = dragViewXY[1];
					}
					boolean add = lp.cellX != lp.tmpCellX || lp.cellY != lp.tmpCellY;
					info.mCellX = lp.cellX = lp.tmpCellX;
					info.mCellY = lp.cellY = lp.tmpCellY;
					info.mSpanX = lp.cellHSpan;
					info.mSpanY = lp.cellVSpan;
					if (add) {
						itemInfos.add(info);
					}
				}
			}
		}
		if (!itemInfos.isEmpty()) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_CHANGE_VIEWS_POSITIONS, getScreen(), null, itemInfos);
		}
		itemInfos.clear();
		itemInfos = null;
	}

	void revertTempState() {
		if (!isItemPlacementDirty() || DESTRUCTIVE_REORDER) {
			return;
		}
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			if (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY) {
				lp.tmpCellX = lp.cellX;
				lp.tmpCellY = lp.cellY;
				// 做换位之后的回归动画
				animateChildToPosition(child, lp.cellX, lp.cellY, REORDER_ANIMATION_DURATION, 0,
						false, false);
			}
		}
		completeAndClearReorderHintAnimations();
		setItemPlacementDirty(false);
	}

	private void completeAndClearReorderHintAnimations() {
		for (ReorderHintAnimation a : mShakeAnimators.values()) {
			a.completeAnimationImmediately();
		}
		mShakeAnimators.clear();
	}

	private void copySolutionToTempState(ItemConfiguration solution, View dragView) {
		for (int i = 0; i < getCountX(); i++) {
			for (int j = 0; j < getCountY(); j++) {
				mTmpOccupied[i][j] = false;
			}
		}

		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child == dragView) {
				continue;
			}
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			CellAndSpan c = solution.map.get(child);
			if (c != null) {
				lp.tmpCellX = c.x;
				lp.tmpCellY = c.y;
				lp.cellHSpan = c.spanX;
				lp.cellVSpan = c.spanY;
				markCellsForView(c.x, c.y, c.spanX, c.spanY, mTmpOccupied, true);
			}
		}
		markCellsForView(solution.dragViewX, solution.dragViewY, solution.dragViewSpanX,
				solution.dragViewSpanY, mTmpOccupied, true);
	}

	public void setUseTempCoords(boolean useTempCoords) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
			lp.useTmpCoords = useTempCoords;
		}
	}

	ItemConfiguration findConfigurationNoShuffle(int pixelX, int pixelY, int minSpanX,
			int minSpanY, int spanX, int spanY, View dragView, ItemConfiguration solution) {
		int[] result = new int[2];
		int[] resultSpan = new int[2];
		findNearestVacantArea(pixelX, pixelY, minSpanX, minSpanY, spanX, spanY, null, result,
				resultSpan);
		if (result[0] >= 0 && result[1] >= 0) {
			copyCurrentStateToSolution(solution, false);
			solution.dragViewX = result[0];
			solution.dragViewY = result[1];
			solution.dragViewSpanX = resultSpan[0];
			solution.dragViewSpanY = resultSpan[1];
			solution.isSolution = true;
		} else {
			solution.isSolution = false;
		}
		return solution;
	} // end findConfigurationNoShuffle

	// This method starts or changes the reorder hint animations
	private void beginOrAdjustHintAnimations(ItemConfiguration solution, View dragView, int delay) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child == dragView) {
				continue;
			}
			CellAndSpan c = solution.map.get(child);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			if (c != null) {
				ReorderHintAnimation rha = new ReorderHintAnimation(child, lp.cellX, lp.cellY, c.x,
						c.y, c.spanX, c.spanY);
				rha.animate();
			}
		}
	} // end beginOrAdjustHintAnimations

	void regionToCenterPoint(int cellX, int cellY, int spanX, int spanY, int[] result) {
		final int hStartPadding = getLeftPadding();
		final int vStartPadding = getTopPadding();
		result[0] = (int) (hStartPadding + cellX * (sCellWidth + sWidthGap) + (spanX * sCellWidth + (spanX - 1)
				* sWidthGap) / 2);
		result[1] = (int) (vStartPadding + cellY * (sCellHeight + sHeightGap) + (spanY
				* sCellHeight + (spanY - 1) * sHeightGap) / 2);
	}

	// Class which represents the reorder hint animations. These animations show
	// that an item is
	// in a temporary state, and hint at where the item will return to.
	/**
	 * 
	 * @author jiangxuwen
	 *
	 */
	//CHECKSTYLE:OFF
	class ReorderHintAnimation {
		View child;
		float finalDeltaX;
		float finalDeltaY;
		float initDeltaX;
		float initDeltaY;
		float finalScale;
		float initScale;
		private static final int DURATION = 300;
		Animator a;

		public ReorderHintAnimation(View child, int cellX0, int cellY0, int cellX1, int cellY1,
				int spanX, int spanY) {
			regionToCenterPoint(cellX0, cellY0, spanX, spanY, mTmpPoint);
			final int x0 = mTmpPoint[0];
			final int y0 = mTmpPoint[1];
			regionToCenterPoint(cellX1, cellY1, spanX, spanY, mTmpPoint);
			final int x1 = mTmpPoint[0];
			final int y1 = mTmpPoint[1];
			final int dX = x1 - x0;
			final int dY = y1 - y0;
			finalDeltaX = 0;
			finalDeltaY = 0;
			if (dX == dY && dX == 0) {
			} else {
				if (dY == 0) {
					finalDeltaX = -Math.signum(dX) * mReorderHintAnimationMagnitude;
				} else if (dX == 0) {
					finalDeltaY = -Math.signum(dY) * mReorderHintAnimationMagnitude;
				} else {
					double angle = Math.atan((float) dY / dX);
					finalDeltaX = (int) (-Math.signum(dX) * Math.abs(Math.cos(angle)
							* mReorderHintAnimationMagnitude));
					finalDeltaY = (int) (-Math.signum(dY) * Math.abs(Math.sin(angle)
							* mReorderHintAnimationMagnitude));
				}
			}
			initDeltaX = CompatibleUtil.getTranslationX(child);
			initDeltaY = CompatibleUtil.getTranslationY(child);
			finalScale = 1.0f - 4.0f / child.getWidth();
			initScale = CompatibleUtil.getScaleX(child);

			CompatibleUtil.setPivotX(child, child.getMeasuredWidth() * 0.5f);
			CompatibleUtil.setPivotY(child, child.getMeasuredHeight() * 0.5f);
			this.child = child;
		}

		void animate() {
			if (mShakeAnimators.containsKey(child)) {
				ReorderHintAnimation oldAnimation = mShakeAnimators.get(child);
				oldAnimation.cancel();
				mShakeAnimators.remove(child);
				if (finalDeltaX == 0 && finalDeltaY == 0) {
					completeAnimationImmediately();
					return;
				}
			}
			if (finalDeltaX == 0 && finalDeltaY == 0) {
				return;
			}
			ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
			a = va;
			va.setRepeatMode(ValueAnimator.REVERSE);
			va.setRepeatCount(ValueAnimator.INFINITE);
			va.setDuration(DURATION);
			va.setStartDelay((int) (Math.random() * 60));
			va.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float r = ((Float) animation.getAnimatedValue()).floatValue();
					float x = r * finalDeltaX + (1 - r) * initDeltaX;
					float y = r * finalDeltaY + (1 - r) * initDeltaY;
					CompatibleUtil.setTranslationX(child, x);
					CompatibleUtil.setTranslationY(child, y);
					float s = r * finalScale + (1 - r) * initScale;
					CompatibleUtil.setScaleX(child, s);
					CompatibleUtil.setScaleY(child, s);
				}
			});
			va.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationRepeat(Animator animation) {
					// We make sure to end only after a full period
					initDeltaX = 0;
					initDeltaY = 0;
					initScale = 1.0f;
				}
			});
			mShakeAnimators.put(child, this);
			va.start();
		}

		private void cancel() {
			if (a != null) {
				a.cancel();
				//				CompatibleUtil.unwrap(child);
			}
		}

		private void completeAnimationImmediately() {
			if (a != null) {
				a.cancel();
			}

			AnimatorSet s = new AnimatorSet();
			a = s;
			s.playTogether(ObjectAnimator.ofFloat(child, "scaleX", 1f),
					ObjectAnimator.ofFloat(child, "scaleY", 1f),
					ObjectAnimator.ofFloat(child, "translationX", 0f),
					ObjectAnimator.ofFloat(child, "translationY", 0f));
			s.setDuration(REORDER_ANIMATION_DURATION);
			s.setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f));
			s.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					CompatibleUtil.unwrap(child);
				}
			});
			s.start();
		}
	} // end ReorderHintAnimation
	//CHECKSTYLE:ON
	
	private void setupLp(CellLayout.LayoutParams lp, boolean isDeskIcon) {
		if (isDeskIcon) {
			lp.setupForBubbleTextView(sCellWidth, sCellHeight, sWidthGap, sHeightGap,
					getLeftPadding(), getTopPadding(), sAutoStretch, mNeedPaddingH, mNeedPaddingV);
		} else {
			lp.setup(sCellWidth, sCellHeight, sWidthGap, sHeightGap, getLeftPadding(),
					getTopPadding(), sAutoStretch);
		}
	}

	boolean isNearestDropLocationOccupied(int pixelX, int pixelY, int spanX, int spanY,
			View dragView, int[] result) {
		result = findNearestArea(pixelX, pixelY, spanX, spanY, result);
		getViewsIntersectingRegion(result[0], result[1], spanX, spanY, dragView, null,
				mIntersectingViews);
		return !mIntersectingViews.isEmpty();
	}

	public float getDistanceFromCell(float x, float y, int[] cell) {
		cellToCenterPoint(cell[0], cell[1], mTmpPoint);
		float distance = (float) Math.sqrt(Math.pow(x - mTmpPoint[0], 2)
				+ Math.pow(y - mTmpPoint[1], 2));
		return distance;
	}

	public void showFolderAccept(FolderRingAnimator fra) {
		mFolderOuterRings.add(fra);
	}

	public void hideFolderAccept(FolderRingAnimator fra) {
		if (mFolderOuterRings.contains(fra)) {
			mFolderOuterRings.remove(fra);
		}
		invalidate();
	}

	// 绘制文件夹背景图
	private void drawFolderRing(Canvas canvas) {
		// The folder outer / inner ring image(s)
		for (int i = 0; i < mFolderOuterRings.size(); i++) {
			FolderRingAnimator fra = mFolderOuterRings.get(i);
			Drawable d = FolderRingAnimator.sSharedInnerRingDrawable;
			int width = (int) fra.getOuterRingSize();
			int height = width;
			cellToPoint(fra.mCellX, fra.mCellY, mTempLocation);

			int centerX = mTempLocation[0] + sCellWidth / 2;
			int centerY = mTempLocation[1] + sCellHeight / 2;
			canvas.save();
			// 使文件夹背景的绘制向上偏移5个像素
			canvas.translate(centerX - width / 2, centerY - width / 2 - 5);
			d.setBounds(0, 0, width, height);
			d.draw(canvas);
			canvas.restore();
		}
	} // end drawFolderRing
}
