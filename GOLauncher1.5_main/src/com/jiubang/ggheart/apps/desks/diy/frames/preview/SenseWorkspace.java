package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.TextUtil;
import com.go.util.animation.MyAnimationUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.CellLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.statistics.StaticTutorial;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * @author jiangchao
 *
 */
public class SenseWorkspace extends ViewGroup
		implements
			CardLayout.ICardEventListener,
			ScreenScrollerListener {
	private ISenseWorkspaceListener mListener;
	private ScreenScroller mScroller;
	private boolean mFirstLayout = true;
	private int mCurrentScreen;

	private float mLastMotionX;

	// 设置滚动速度
	private int mScrollingDuration = 500;

	public final static int MAX_CARD_NUMS = 9;

	/**
	 * 图片资源
	 */
	public Drawable mBorderImg;
	public Drawable mBorderLightImg;
	public Drawable mBorderDragImg;
	public Drawable mHomeImg;
	public Drawable mLightHomeImg;
	public Drawable mAddImg;
	public Drawable mLightAddImg;

	public Drawable mNoRoomImg;
	public Drawable mLightNoRoomImg;

	public int mDelImageWidth;
	public int mDelImageHeight;
	public int mDelImageViewTop;
	public int mHomeImageViewLeft;
	public static int sHomeImageViewTop;
	public int mHomeImageWidth;
	public int mHomeImageHeight;

	public int mRoomWidth;
	public int mRoomHeight;

	public Animation mZoomInAnimation;
	public Animation mZoomOutAnimation;

	/**
	 * Fling 切换到下一个屏幕的最小速度
	 */
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private final static int TOUCH_STATE_MULTITOUCH = 2;
	private int mTouchState = TOUCH_STATE_REST;

	private int mTouchSlop;

	private int mRows;
	private int mColumns;
	private int mRowsMulColumns;
	private int mScreens;

	private CardLayout mHomeCard;
	private CardLayout mOpratorCardLayout;

	public final static int SENSE_WAIT_FOR_ENTERING = 0; // 等待进入动画
	public final static int SENSE_ENTERING = 1; // 进入动画
	public final static int SENSE_MOVING = 2; // 移动
	public final static int SENSE_NORMAL = 3; // 静态显示，正常状态
	public final static int SENSE_LEAVING = 4; // 退出动画
	public final static int SENSE_REPLACING = 5; // 换卡片
	public final static int SENSE_REPLACE_BACK = 6; // 取消换卡片

	private boolean mIsHideAddCard = false;
	private boolean mHasFinishRelpace = false;

	private long mStartTime;
	private int mStatus;
	private final int mMovingDuration = 400;
	private final int mReplaceAnimationDuration = 200;
	private int mRemoveCardId = -1; // 被删除的卡片ID
	private int mLeaveCardId = -1; // 离开显示的卡片ID

	// 作离开动画的参数
	private View mLeavingView;
	private float mCx;
	private float mCy;
	private float mScale;
	private int mLeaveDuration = 300;

	// 作进入动画的参数
	private final int mEnterDuration = 250;

	private int mRelpaceSrcIndex = -1;
	private int mRelpaceDestIndex = -1;
	private boolean mReplaceWaitForLayout = false;

	private boolean mLocked = false;

	// 居中使用
	// private boolean mLongTouch = false;

	// 换位取消参数
	private int mDragCardIndex;
	private Rect mDragCardRect;

	/**
	 * 主题相关参数
	 */
	private int mBackgroudColor = 0x7f000000; // 预览背景颜色

	private boolean mEnableUpdate = true;

	// private AlertDialog mDelDialog;

	private PreviewController mPreviewController; // 预览控制

	// 添加GoWidget到桌面需要的数据
	private Bundle mGoWidgetBundle;

	// 保存有足够空间添加widget的屏幕下标列表
	private ArrayList<Integer> mEnoughSpaceList;

	// 判断第一次排版完成
	public static int sCardCount = 0;

	public static boolean showStatusBar = false;

	// 拖拽提示的上边距
	private int mDragCaptionTop = -1;

	private Paint mCaptionHomePaint; // Home键跳屏幕预览提示的画笔
	private String mHomeText; //Home键跳屏幕预览提示信息
	private Paint mCaptionPaint; // 拖拽提示的画笔
	private String mCaptionText; // 拖拽提示文字
	private int mCaptionTextSize; // 拖拽提示文字大小
	private int mCaptionTextW; // 拖拽提示文字宽度
	private int mCaptionPadding; // 拖拽提示的左边距

	public Bitmap mCaptionImg; // 拖拽提示背景图
	public Rect mCaptionRect; // 拖拽提示背景图范围

	private int mDragTipTextTop = 0; // 拖拽提示文字与背景的上边距
	private static final int NO_SHOW_CAPTION = -1;

	/***
	 * 获取最下面一行卡片与底部距离
	 */
	public void setCaptionY() {
		mDragCaptionTop = sMarginTop;
		if (sCardCount <= 3) {
			mDragCaptionTop += (sSpaceY + sCardHeight) + sSpaceY;
		} else if (sCardCount <= 6) {
			mDragCaptionTop += (sSpaceY + sCardHeight) * 2 + sSpaceY;
		} else {
			mDragCaptionTop = NO_SHOW_CAPTION;
		}
	}

	private String[] mCaptionInfo; // 排版信息
	private int mTextLineH; // 文字高度

	/***
	 * 初始化提示内容绘制信息
	 */
	private void initCaptionInfo() {
		mCaptionTextSize = getResources().getDimensionPixelSize(
				R.dimen.screen_preview_drag_tip_textsize);
		mCaptionText = getResources().getString(R.string.preview_drag_explain);
		mCaptionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCaptionPaint.setColor(0xbbffffff);
		mCaptionPaint.setTextSize(mCaptionTextSize);
		mCaptionTextW = (int) mCaptionPaint.measureText(mCaptionText);

		mCaptionHomePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCaptionHomePaint.setColor(0xbbffffff);
		mCaptionHomePaint.setTextSize(getResources().getDimensionPixelSize(
				R.dimen.screen_preview_home_textsize));
		mHomeText = getResources().getString(R.string.choose_mainscreen);
		mCaptionImg = BitmapFactory.decodeResource(getResources(), R.drawable.preview_tip_bg); // bmpDstBitmap;
		FontMetrics fm = mCaptionPaint.getFontMetrics();
		int fontHeight = (int) Math.ceil(fm.descent - fm.top);
		mTextLineH = fontHeight
				+ getResources().getDimensionPixelSize(
						R.dimen.screen_preview_drag_tip_text_line_space);

	}
	/***
	 * 绘制Home键提示信息
	 */
	private void drawHomeCaption(final Canvas canvas) {
		if (mStatus != SENSE_WAIT_FOR_ENTERING && mStatus != SENSE_ENTERING
				&& mStatus != SENSE_LEAVING) {
			if (mHomeText != null) {
				int textLeft;
				textLeft = (sWidth - (int) mCaptionHomePaint.measureText(mHomeText)) / 2;
				canvas.drawText(mHomeText, mCaptionPadding < textLeft ? textLeft : mCaptionPadding,
						mTextLineH, mCaptionHomePaint);
			}
		}
	}
	/***
	 * 绘制拖拽提示文字
	 */
	private void drawCaption(final Canvas canvas) {

		if (mDragCaptionTop != NO_SHOW_CAPTION && mStatus != SENSE_WAIT_FOR_ENTERING
				&& mStatus != SENSE_ENTERING && mStatus != SENSE_LEAVING) {
			mCaptionRect = new Rect(0, mDragCaptionTop, sWidth, sHeight);

			if (mCaptionInfo != null) {
				int textLeft;
				for (int i = 0; i < mCaptionInfo.length; i++) {
					textLeft = (sWidth - (int) mCaptionPaint.measureText(mCaptionInfo[i])) / 2;
					canvas.drawText(mCaptionInfo[i], mCaptionPadding < textLeft
							? textLeft
							: mCaptionPadding, mDragCaptionTop + mCaptionTextSize + mDragTipTextTop
							+ mTextLineH * i, mCaptionPaint);
				}
			}
		}
	}

	/***
	 * 是否正在替换动画状态
	 * 
	 * @return
	 */
	public boolean isRelpaceing() {
		return mStatus == SENSE_REPLACING;
	}

	/***
	 * 是否正在替换动画状态
	 * 
	 * @return
	 */
	public boolean isNormalState() {
		return mStatus == SENSE_NORMAL;
	}

	public static void setCardCount(int count) {
		sCardCount = count;
	}

	/**
	 * Sense风格工作区
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            属性
	 */
	public SenseWorkspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initCaptionInfo();
	}

	/**
	 * Sense风格工作区
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            属性
	 * @param defStyle
	 *            样式
	 */
	public SenseWorkspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new ScreenScroller(context, this);
		mScroller.setMaxOvershootPercent(20);
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SenseWorkspace, defStyle,
				0);
		mBackgroudColor = a.getColor(R.styleable.SenseWorkspace_backgroundColor, 0xa6000000);
		mRows = a.getInt(R.styleable.SenseWorkspace_cardRows, 3);
		mColumns = a.getInt(R.styleable.SenseWorkspace_cardCols, 3);
		sSpaceScaleX = a.getInt(R.styleable.SenseWorkspace_cardSpaceScaleX, 160);
		sSpaceScaleY = a.getInt(R.styleable.SenseWorkspace_cardSpaceScaleY, 160);
		mRowsMulColumns = mRows * mColumns;
		sMarginTop = a.getDimensionPixelSize(R.styleable.SenseWorkspace_marginTop, 25);
		a.recycle();

		requestFocus(); // 希望享有焦点
	}

	public void setPreviewController(PreviewController controller) {
		mPreviewController = controller;
	}

	/**
	 * 设置监听者
	 * 
	 * @param listener
	 *            监听者
	 */
	public void setListener(ISenseWorkspaceListener listener) {
		mListener = listener;
	}

	public int getScrollDuration() {
		return mScrollingDuration;
	}

	/**
	 * 添加组件到指定屏幕
	 */
	public void addInScreen(CardLayout card) {
		card.setEventListener(this);
		card.setPreviewController(mPreviewController);
		addView(card);

		// 缓存方便后面状态切换
		if (card.isHome()) {
			mHomeCard = card;
		}
	}

	/**
	 * 设置是否是第一次排版
	 * 
	 * @param firstLayout
	 */
	public void setFirstLayout(boolean firstLayout) {
		mFirstLayout = firstLayout;
	}

	// CardLayout底图使用NinePatch图片，其边框间隙如下
	static int sCardPaddingLeft = 9;
	static int sCardPaddingTop = 10;
	static int sCardPaddingRight = 9;
	static int sCardPaddingBottom = 21;

	static int sWidth; // SenseWorkspace的宽度
	static int sHeight; // SenseWorkspace的高度
	static int sCardWidth; // CardLayout的宽度
	static int sCardHeight; // CardLayout的高度
	static int sPreViewScaleWidth; // CellLayout缩放后的宽度
	static int sPreViewScaleHeight; // CellLayout缩放后的高度
	static float sPreViewScaleFactor; // CellLayout缩放比例
	static float sX;
	static float sY;

	static int sScreenTop; // 定义的固定上边距
	static int sMarginTop; // SenseWorkspace的上边距
	static int sMarginBottom;
	static int sMarginRight; // SenseWorkspace的左边距
	static int sMarginLeft; // SenseWorkspace的左边距

	static int sSpaceX; // CardLayout的水平间隙
	static int sSpaceY; // CardLayout的垂直间隙
	static int sSpaceScaleX = 160; // SenseWorkspace的宽度相对CardLayout之间水平空隙的最大倍数
	static int sSpaceScaleY = 160; // SenseWorkspace的高度相对CardLayout之间垂直空隙的最大倍数

	private int mTopPadding = 0; // 卡片行数少于mRows时，为了卡片垂直居中，附加的上边距
	private int mLeftPadding = 0; // 卡片个数少于mCols时，为了卡片水平居中，附加的左边距

	/**
	 * 根据视图大小，计算自适应布局参数
	 * 
	 * @param width
	 *            视图宽度
	 * @param height
	 *            视图高度
	 * @param iterator
	 *            递归深度,初始值必须为0，只有横屏模式才需要迭代多一次
	 */
	void getDesiredLayoutConfigure(int width, int height, int iterator) {
		boolean landscapeMode = width > height; // 是否横屏模式
		if (!landscapeMode) {
			// 横屏时卡片之间的空隙
			sSpaceX = getResources().getDimensionPixelSize(
					R.dimen.screen_preview_card_padding_spacex);
			sSpaceY = getResources().getDimensionPixelSize(
					R.dimen.screen_preview_card_padding_spacey);

			sMarginTop = getResources().getDimensionPixelSize(R.dimen.cards_top_port);
			sMarginBottom = getResources().getDimensionPixelSize(R.dimen.cards_bottom_port);
			sMarginLeft = getResources().getDimensionPixelSize(R.dimen.cards_left_port);
			sMarginRight = getResources().getDimensionPixelSize(R.dimen.cards_right_port);
			mDragTipTextTop = getResources().getDimensionPixelSize(
					R.dimen.screen_preview_drag_tip_text_port);

		} else {
			sSpaceX = getResources().getDimensionPixelSize(
					R.dimen.screen_preview_card_padding_spacex_land);
			sSpaceY = getResources().getDimensionPixelSize(
					R.dimen.screen_preview_card_padding_spacey_land);
			sMarginTop = getResources().getDimensionPixelSize(R.dimen.cards_top_land);
			sMarginBottom = getResources().getDimensionPixelSize(R.dimen.cards_bottom_land);
			sMarginLeft = getResources().getDimensionPixelSize(R.dimen.cards_left_land);
			sMarginRight = getResources().getDimensionPixelSize(R.dimen.cards_right_land);
			mDragTipTextTop = getResources().getDimensionPixelSize(
					R.dimen.screen_preview_drag_tip_text_land);
		}
		sScreenTop = sMarginTop;
		if (SensePreviewFrame.sScreenFrameStatus) {
			sMarginTop = sMarginTop - StatusBarHandler.getStatusbarHeight();
		} else {
			if (!SensePreviewFrame.isEnterFromDragView()) {
				if (!SenseWorkspace.showStatusBar) {
					sMarginTop = sMarginTop - StatusBarHandler.getStatusbarHeight();
				}
			}
		}
		sWidth = Math.max(width, 1);
		sHeight = Math.max(height, 1);
		sCardWidth = (sWidth - sMarginLeft - sMarginRight - sSpaceX * 2) / mColumns;
		sCardHeight = (sHeight - sMarginTop - sMarginBottom - sSpaceY * 2) / mRows;
		sCardPaddingLeft = sCardPaddingRight = sCardWidth / 10;
		sCardPaddingTop = sCardHeight / 40;
		sCardPaddingBottom = mHomeImageHeight;
		if (iterator > 0) {
			// 这里是横屏模式
			// 调整预览在卡片里面的左右边距，使得预览的实际部分在卡片内是居中的
			// 并且由于底图的下边框比较大，横屏的高度比较小，由于等比例缩放的问题，
			// 使得水平间距太大，底图被拉伸成接近正方形，需要把水平间距尽量从底图外移进地图内，
			// 即减小SpaceX，增大CardPaddingLeft和CardPaddingRight
			final int diff = CellLayout.sLongAxisEndPadding - CellLayout.sLongAxisStartPadding;
			sCardPaddingLeft += (int) (diff * sPreViewScaleFactor / 2);
			sCardPaddingRight += (int) (diff * sPreViewScaleFactor / 2);

		}
		final float scaleX = ((sWidth - sMarginLeft - sMarginRight - sSpaceX * 2) / mColumns
				- sCardPaddingLeft - sCardPaddingRight)
				/ (float) sWidth;
		final float scaleY = ((sHeight - sMarginTop - sMarginBottom - sSpaceY * 2) / mRows
				- sCardPaddingTop - sCardPaddingBottom)
				/ (float) sHeight;
		if (!SenseWorkspace.showStatusBar) {
			sPreViewScaleFactor = Math.max(0, Math.max(scaleX, scaleY));
		}
		sPreViewScaleWidth = (int) (sPreViewScaleFactor * sWidth);
		sPreViewScaleHeight = (int) (sPreViewScaleFactor * sHeight);

		if (landscapeMode && iterator == 0) {
			// 因为横屏时，dock栏占去的空白部分使得预览的实际部分在卡片内偏左，
			// 所以需要多一次迭代来调整居中
			getDesiredLayoutConfigure(width, height, iterator + 1);
			return;
		}
		mHomeImageViewLeft = (sCardWidth - mHomeImageWidth) / 2;
		sHomeImageViewTop = sCardHeight - mHomeImageHeight - 2;
		mRoomWidth = sCardWidth;
		mRoomHeight = sCardHeight;

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		sWidth = getWidth();
		final int count = getChildCount();
		if (count <= 0) {
			return;
		}

		final int screenWidth = r - l;
		int screen = -1;
		int childLeft = 0;
		int childTop = 0;
		int currentCardId = -1;
		for (int i = 0, j = 0; i < count; i++) {
			final CardLayout child = (CardLayout) getChildAt(i);
			if (child != null) {
				child.setId(i);
				final CardLayout replaceCard = (CardLayout) getChildAt(mRelpaceSrcIndex);
				if (mReplaceWaitForLayout && replaceCard != null && i == replaceCard.getId()) {
					completeReplaceFinish();
				}
				if (child.getVisibility() != View.GONE) {

					// mRows * mColumns方格形式进行摆放
					if (j % mRowsMulColumns == 0) // 在当前屏的左上角
					{
						childTop = sMarginTop;
						childLeft = ++screen * screenWidth + sMarginLeft;
					} else if (j % mRows == 0) // 在当前行的左边（但不是左上角）
					{
						childTop += sCardHeight + sSpaceY;
						childLeft = screen * screenWidth + sMarginLeft;
					} else {
						childLeft += sCardWidth + sSpaceX;
					}

					if (child.isCurrent()) {
						currentCardId = i;
					}

					child.layout(childLeft, childTop, childLeft + sCardWidth, childTop
							+ sCardHeight);
					++j;
				}
			}
		}

		if (mFirstLayout && count == sCardCount) {
			if (currentCardId > -1) {
				mCurrentScreen = currentCardId / mRowsMulColumns;
			}
			mFirstLayout = false;
			if (mIsHideAddCard) {
				hideAddCard();
				setCaptionY();;
			}

			if (mListener != null) {
				mListener.firstLayoutComplete();
			}

			// 更新指示器
			updateIndicator();
		}

		// 注意onLayout方法可能会被分几次调用的，所以要生成绘图缓冲要在每次onLayout中调用
		if (mStatus <= SENSE_ENTERING) {
			enableChildrenDrawingCacheCurrently();
		} else {
			if (mStatus == SENSE_NORMAL) {
				enableChildrenDrawingCacheAll();
			}
			// mScroller.updateScreenCount();
			// mScroller.setScreenCount(getScreenCount());
			if (mScroller.isFinished()) {
				mScroller.setCurrentScreen(mCurrentScreen);
			}
		}

		mScroller.setScreenCount(mScreens = getScreenCount());

		// 提示文字排版
		if (sWidth > mCaptionTextW) {
			mCaptionPadding = (sWidth - mCaptionTextW) / 2;
			mCaptionInfo = new String[1];
			mCaptionInfo[0] = mCaptionText;
		} else {
			mCaptionPadding = getResources().getDimensionPixelSize(
					R.dimen.screen_preview_drag_tip_text_padding);
			if (sWidth > 0) {
				mCaptionInfo = TextUtil.typeString(mCaptionText, sWidth - 2 * mCaptionPadding,
						mCaptionPaint);
			}

		}

	}

	/**
	 * 获取当前屏中的第一个子视图的索引
	 * 
	 * @return
	 */
	public int getCurScreenStartIndex() {
		return mScroller.getDstScreen() * mRowsMulColumns;
	}

	/**
	 * 实际为：获取当前屏的偏移量
	 */
	public int getStartScreenWidth() {
		return mScroller.getDstScreen() * getWidth();
	}

	/**
	 * 替换卡片位置
	 * 
	 * @param srcIndex
	 *            源ID
	 * @param destIndex
	 *            目标ID
	 * @param newRects
	 *            新的替换之后的区域
	 */
	public void replaceCard(final int srcIndex, final int destIndex, List<Rect> newRects) {
		if (newRects == null || srcIndex < 0 || srcIndex >= getChildCount()
				|| destIndex >= getChildCount() || destIndex < 0) {
			return;
		}

		// 获取当前屏最新的区域位置
		newRects.clear();
		List<Rect> rects = getCurScreenRects();
		final int curScreenStartIndex = getCurScreenStartIndex();
		final int size = rects.size();
		// TODO 此处不清楚该如何优化此处代码，感觉优化过后没有目前逻辑清晰，故暂不优化
		final int minIndex = Math.min(srcIndex, destIndex);
		final int maxIndex = Math.max(srcIndex, destIndex);
		for (int i = 0; i < size; i++) {
			final int abusoluteIndex = i + curScreenStartIndex;
			final CardLayout card = (CardLayout) getChildAt(abusoluteIndex);
			if (abusoluteIndex == destIndex) {
				getChildAt(srcIndex).setId(destIndex);
				// 添加源区域
				if (srcIndex > destIndex) {
					card.setId(destIndex + 1);
				} else {
					card.setId(destIndex - 1);
				}
			} else if (abusoluteIndex < maxIndex && abusoluteIndex > minIndex) {
				// 添加前一个区域
				if (srcIndex > destIndex) {
					card.setId(abusoluteIndex + 1);
				} else {
					card.setId(abusoluteIndex - 1);
				}
			}
		}
		//重置当前页索引
		if (SensePreviewFrame.sCurScreenId == destIndex) {
			if (srcIndex > destIndex) {
				SensePreviewFrame.sCurScreenId = destIndex + 1;
			} else {
				SensePreviewFrame.sCurScreenId = destIndex - 1;
			}
		} else if (SensePreviewFrame.sCurScreenId < maxIndex
				&& SensePreviewFrame.sCurScreenId > minIndex) {
			if (srcIndex > destIndex) {
				SensePreviewFrame.sCurScreenId += 1;
			} else {
				SensePreviewFrame.sCurScreenId -= 1;
			}
		} else if (SensePreviewFrame.sCurScreenId == srcIndex) {
			SensePreviewFrame.sCurScreenId = destIndex;
		}

		if (mListener != null && mHomeCard != null && indexOfChild(mHomeCard) != mHomeCard.getId()) {
			// 设置主页
			mListener.setCardHome(mHomeCard.getId());
		}

		newRects.addAll(rects);

		// 保存做动画
		mRelpaceSrcIndex = srcIndex;
		mRelpaceDestIndex = destIndex;
		mStartTime = 0;
		mStatus = SENSE_REPLACING;
		postInvalidate();
	}

	public void replaceBack(int cardIndex, Rect cardRc) {
		if (null == cardRc) {
			return;
		}

		if (cardIndex < 0 || cardIndex >= getChildCount()) {
			return;
		}

		int index = cardIndex % (mColumns * mRows);
		List<Rect> rects = getCurScreenRects();
		if (index >= 0 && index < rects.size()) {
			Rect rc = rects.get(index);
			if (Math.abs(rc.left - cardRc.left) < rc.width() / 10
					&& Math.abs(rc.top - cardRc.top) < rc.height() / 10) {
				showAddCard();
				// setCaptionY();
			}
		}

		mDragCardIndex = cardIndex;
		mDragCardRect = cardRc;

		mStartTime = 0;
		mStatus = SENSE_REPLACE_BACK;
		postInvalidate();
	}

	private void handleReplaceBackFinished() {
		mDragCardIndex = 0;
		mDragCardRect = null;

		showAddCard();
		// setCaptionY();
		mStatus = SENSE_NORMAL;
		// 发信息给屏幕交换层，通知动画结束
		GoLauncher.sendMessage(this, IDiyFrameIds.REPLACE_DRAG_FRAME,
				IDiyMsgIds.REPLACE_DRAG_FINISH, -1, null, null);
	}

	/**
	 * 进入动画之前的动画参数计算
	 */
	public void enterCard(int enterCardId) {
		initZoomTransition(enterCardId);
		mStatus = SENSE_ENTERING;
	}

	/**
	 * 要求指定的卡片做离开动画
	 * 
	 * @param leaveCardId
	 *            卡片ID
	 * @param view
	 *            被放大的视图
	 * @param leaveDuration
	 *            离开时间
	 */
	public void leaveCard(int leaveCardId, View view) {
		lock();
		mLeaveCardId = leaveCardId;
		mStatus = SENSE_LEAVING;
		setBackgroundColor(0);
		mLeavingView = view;

		if (mLeavingView instanceof CellLayout) {
			((CellLayout) mLeavingView).buildChildrenDrawingCache();
		}
		// 初始化时已在setScrollSetting中已经设置过
		// mLeaveDuration = leaveDuration;

		initZoomTransition(leaveCardId);
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

	/**
	 * 是否被锁住
	 * 
	 * @return 是否被锁住
	 */
	public boolean isLocked() {
		return mLocked;
	}

	private void handleLeaveFinish() {
		unlock();
		Assert.assertTrue(mLeavingView instanceof CellLayout);
		((CellLayout) mLeavingView).setChildrenDrawnWithCacheEnabled(false);
		destroyChildrenDrawingCache();
		if (mListener != null) {
			mListener.leaveFinish();
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mScroller.invalidateScroll();
		int currentTime = 0;
		if (mStartTime == 0) {
			mStartTime = SystemClock.uptimeMillis();
		} else {
			currentTime = (int) (SystemClock.uptimeMillis() - mStartTime);
		}
		// jiang Home键 设置为默认桌面后  按home键跳屏幕预览
		if (SensePreviewFrame.sIsHOME) {
			drawHomeCaption(canvas);
		} else {
			drawCaption(canvas);
		}
		switch (mStatus) {
			case SENSE_WAIT_FOR_ENTERING :
				drawWaitForEnter(canvas);
				break;

			case SENSE_ENTERING :
				drawEntering(canvas, currentTime);
				break;

			case SENSE_NORMAL :
				drawNormal(canvas);
				break;

			case SENSE_LEAVING :
				drawLeaving(canvas, currentTime);
				break;

			case SENSE_MOVING :
				drawMoving(canvas, currentTime);
				break;

			case SENSE_REPLACING :
				drawReplacing(canvas, currentTime);
				break;

			case SENSE_REPLACE_BACK :
				drawRelaceBack(canvas, currentTime);
				break;

			default :
				drawNormal(canvas);
				break;
		}
	}

	public void handleReplaceFinish() {
		if (mStatus != SENSE_REPLACING) {
			return;
		}

		// TODO 更改所有相关的ID
		View srcView = getChildAt(mRelpaceSrcIndex);
		if (srcView == null) {
			return;
		}
		removeView(srcView);
		srcView.setVisibility(View.INVISIBLE);
		addView(srcView, mRelpaceDestIndex);

		// 等待排版完成,使View的index == id时再通知
		mReplaceWaitForLayout = true;

		mStatus = SENSE_NORMAL;
		postInvalidate();
	}

	private void completeReplaceFinish() {
		mReplaceWaitForLayout = false;
		if (mHasFinishRelpace) {
			getChildAt(mRelpaceDestIndex).setVisibility(View.VISIBLE);
			mHasFinishRelpace = false;
		}
		mRelpaceSrcIndex = -1;
		mRelpaceDestIndex = -1;
		if (mListener != null) {
			mListener.replaceFinish();
		}
	}

	private void drawReplacing(final Canvas canvas, long currentTime) {
		final int count = getChildCount();
		final int minIndex = Math.min(mRelpaceSrcIndex, mRelpaceDestIndex);
		final int maxIndex = Math.max(mRelpaceSrcIndex, mRelpaceDestIndex);
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int saveCount = canvas.save();
			if (child.getVisibility() == View.VISIBLE) {
				if ((mStatus == SENSE_REPLACING) && i >= minIndex && i <= maxIndex) {
					RectF r2 = getRelpaceDestination(child, i);
					final float t = currentTime >= mReplaceAnimationDuration ? 1 : currentTime
							/ (float) mReplaceAnimationDuration;
					final float x = easeOut(child.getLeft(), r2.left, t);
					final float y = easeOut(child.getTop(), r2.top, t);
					canvas.save();
					canvas.translate(x, y);
					// child.draw(canvas);
					drawView(child, canvas);
					canvas.restore();
				} else {
					super.drawChild(canvas, child, getDrawingTime());
				}
			}
			canvas.restoreToCount(saveCount);
		}

		if (currentTime >= mReplaceAnimationDuration) {
			handleReplaceFinish();
		}
		postInvalidate();
	}

	private void drawRelaceBack(final Canvas canvas, long currentTime) {
		final int count = getChildCount();

		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int saveCount = canvas.save();
			// if(child.getVisibility() == View.VISIBLE)
			{
				if (i == mDragCardIndex) {
					final float t = currentTime >= mReplaceAnimationDuration ? 1 : currentTime
							/ (float) mReplaceAnimationDuration;
					final float x = easeOut(mDragCardRect.left + i / mRowsMulColumns * getWidth(),
							child.getLeft(), t);
					final float y = easeOut(mDragCardRect.top, child.getTop(), t);
					canvas.save();
					canvas.translate(x, y);
					// child.draw(canvas);
					drawView(child, canvas);
					canvas.restore();
				} else {
					super.drawChild(canvas, child, getDrawingTime());
				}
			}
			canvas.restoreToCount(saveCount);
		}

		if (currentTime >= mReplaceAnimationDuration) {
			CardLayout dragCard = (CardLayout) getChildAt(mDragCardIndex);
			if (dragCard != null) {
				dragCard.clearClickState();
			}
			handleReplaceBackFinished();
		}
		postInvalidate();
	}

	private void drawMoving(final Canvas canvas, long currentTime) {
		if (currentTime >= mMovingDuration) {
			mRemoveCardId = getChildCount();
			mStatus = SENSE_NORMAL;
		}

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int saveCount = canvas.save();
			if ((mStatus == SENSE_MOVING)
					&& (child.getId() >= mRemoveCardId && child.getId() <= ((mRemoveCardId - mRemoveCardId
							% mRowsMulColumns) + mRowsMulColumns))) {
				RectF r2 = getMoveDestination(child);
				final float t = currentTime >= mMovingDuration ? 1 : currentTime
						/ (float) mMovingDuration;
				final float x = easeOut(r2.left, child.getLeft(), t);
				final float y = easeOut(r2.top, child.getTop(), t);
				canvas.save();
				canvas.translate(x, y);
				// child.draw(canvas);
				drawView(child, canvas);
				canvas.restore();
			} else {
				if (getChildAt(i).getVisibility() == View.VISIBLE) {
					super.drawChild(canvas, child, getDrawingTime());
				}
			}
			canvas.restoreToCount(saveCount);
		}
		postInvalidate();
	}

	private void drawWaitForEnter(final Canvas canvas) {
		// do nothing
		return;
	}

	void drawView(View view, Canvas canvas) {
		Bitmap bitmap = null;
		try {
			bitmap = view.getDrawingCache(true);
		} catch (Exception e) {
			bitmap = null;
		}
		if (bitmap != null && !bitmap.isRecycled()) {
			canvas.drawBitmap(bitmap, 0, 0, null);
		} else {
			view.draw(canvas);
		}
	}

	private void drawEntering(final Canvas canvas, long currentTime) {
		float t = Math.min(1, currentTime / (float) mEnterDuration);

		// 背景色渐变
		int alpha = (int) easeOut(0, 255, t);
		final int bgAlpha = mBackgroudColor >>> 24;
		final int color = alpha * bgAlpha >> 8 << 24;
		canvas.drawColor(color);

		final float scale = easeOut(mScale, 1, t);
		final int beg = SensePreviewFrame.sCurScreenId - SensePreviewFrame.sCurScreenId
				% mRowsMulColumns;
		final int end = Math.min(mRows * mColumns + beg, getChildCount());

		final int saveCount = canvas.save();

		canvas.scale(scale, scale, mCx, mCy);
		for (int i = beg, tx = 0, ty = 0; i < end; i++) {
			View view = getChildAt(i);
			final int x = view.getLeft();
			final int y = view.getTop();
			canvas.translate(x - tx, y - ty);
			drawView(view, canvas);
			tx = x;
			ty = y;
		}
		canvas.restoreToCount(saveCount);
		postInvalidate();

		if (t >= 1) {
			handleEnterFinished();
		}
	}

	private void drawNormal(Canvas canvas) {
		super.dispatchDraw(canvas);
	}

	private void drawLeaving(final Canvas canvas, final long currentTime) {
		final float t = Math.min(1, currentTime / (float) mLeaveDuration);

		int alpha = (int) easeOut(255, 0, t);
		final int bgAlpha = mBackgroudColor >>> 24;
		final int color = alpha * bgAlpha >> 8 << 24;
		canvas.drawColor(color);

		float scale = easeOut(1, mScale, t);
		final int beg = mLeaveCardId - mLeaveCardId % mRowsMulColumns;
		final int end = Math.min(mRowsMulColumns + beg, getChildCount());
		int saveCount = canvas.save();

		canvas.scale(scale, scale, mCx, mCy);
		canvas.save();
		for (int i = beg; i < end; i++) {
			if (i != mLeaveCardId) {
				View view = getChildAt(i);
				view.setVisibility(View.INVISIBLE);
				// final int x = view.getLeft();
				// final int y = view.getTop();
				// canvas.translate(x - tx, y - ty);
				// // view.draw(canvas);
				// drawView(view, canvas);
				// tx = x;
				// ty = y;
			}
		}
		canvas.restore();
		if (mLeavingView != null) {
			// 产生mLeavingView缩小到previewImageView的效果
			canvas.scale(1.0f / mScale, 1.0f / mScale, mCx, mCy);
			canvas.translate(getScrollX(), 0);
			// TODO: 这里还要画一层preview的背景色？
			mLeavingView.draw(canvas);
		}
		canvas.restoreToCount(saveCount);
		if (t >= 1) {
			handleLeaveFinish();
			return;
		}

		postInvalidate();
	}

	private RectF getMoveDestination(View child) {
		final int index = indexOfChild(child);
		// 获取当前View的前一个View的位置
		final int fromIndex = index + 1;
		if (fromIndex < getChildCount()) {
			View preView = getChildAt(index + 1);
			return new RectF(preView.getLeft(), preView.getTop(), preView.getRight(),
					preView.getBottom());
		} else {
			// 计算其后的位置
			if (fromIndex % mRowsMulColumns == 0) {
				// 最后一屏
				int left = child.getRight();
				int top = 0;
				return new RectF(left, top, left + child.getWidth(), top + child.getHeight());
			} else if (fromIndex % mColumns == 0) {
				int left = 0;
				int top = child.getBottom();
				return new RectF(left, top, left + child.getWidth(), top + child.getHeight());
			} else {
				int left = child.getLeft() + child.getWidth();
				return new RectF(left, child.getTop(), left + child.getWidth(), child.getTop()
						+ child.getHeight());
			}
		}
	}

	private RectF getRelpaceDestination(View child, final int index) {
		RectF rect = new RectF(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
		View destChild = null;
		if (index == mRelpaceSrcIndex) {
			destChild = getChildAt(mRelpaceDestIndex);
		} else {
			if (mRelpaceSrcIndex > mRelpaceDestIndex) {
				destChild = getChildAt(index + 1);
			} else {
				destChild = getChildAt(index - 1);
			}
		}

		if (destChild != null) {
			rect.left = destChild.getLeft();
			rect.top = destChild.getTop();
			rect.right = destChild.getRight();
			rect.bottom = destChild.getBottom();
		}

		return rect;
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
	static float easeOut(float begin, float end, float t) {
		t = 1 - t;
		return begin + (end - begin) * (1 - t * t * t);
	}

	@Override
	public void addView(View child) {
		if (!(child instanceof CardLayout)) {
			throw new IllegalArgumentException(
					"A Sense Workspace can only have CardLayout children.");
		}
		super.addView(child);

		mRemoveCardId = getChildCount();
	}

	@Override
	public void addView(View child, int index) {
		if (!(child instanceof CardLayout)) {
			throw new IllegalArgumentException(
					"A Sense Workspace can only have CardLayout children.");
		}
		super.addView(child, index);

		mRemoveCardId = getChildCount();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mLocked) {
			return true;
		}

		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = x;
				mTouchState = TOUCH_STATE_REST;
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				if (mTouchState != TOUCH_STATE_MULTITOUCH)// 多点触摸
				{
					final int xDiff = (int) Math.abs(x - mLastMotionX);
					// final int yDiff = (int) Math.abs(y - mLastMotionY);

					boolean xMoved = xDiff > mTouchSlop;
					// boolean yMoved = yDiff > mTouchSlop;

					// if (xMoved || yMoved)
					// {
					if (xMoved) {
						mTouchState = TOUCH_STATE_SCROLLING;
						// 将当前位置作为滚动的初始位置
						mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
					}
					// }
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				mTouchState = TOUCH_STATE_REST;
				if (mOpratorCardLayout != null) {
					mOpratorCardLayout.clearClickState();
					mOpratorCardLayout = null;
				}
				break;
			}

			default :
				break;
		}

		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLocked) {
			return true;
		}
		int action = ev.getAction();
		boolean bRet = mScroller.onTouchEvent(ev, action);

		if (MotionEvent.ACTION_DOWN != action) {
			if (null != mOpratorCardLayout) {
				// 长按开始拖动卡片，松开时调用
				mOpratorCardLayout.clearClickState();
				mOpratorCardLayout = null;
			}

		}

		return bRet;
	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	public boolean cansnapToNextScreen() {
		final int screen = getCurScreenIndex();
		return screen < (getScreenCount() - 1);
	}

	public boolean cansnapToPreScreen() {
		final int screen = getCurScreenIndex();
		return screen > 0;
	}

	/**
	 * 跳转到下一屏
	 */
	public final void snapToNextScreen() {
		final int screen = getCurScreenIndex();
		if (screen < getScreenCount() - 1) {
			snapToScreen(screen + 1);
		}
	}

	/**
	 * 跳转到上一屏
	 */
	public final void snapToPreScreen() {
		final int screen = getCurScreenIndex();
		if (screen > 0) {
			snapToScreen(screen - 1);
		}
	}

	/**
	 * 获取当前屏的所有卡片区域
	 * 
	 * @return 区域数组
	 */
	public List<Rect> getCurScreenRects() {
		List<Rect> rects = new ArrayList<Rect>();
		int screen = mScroller.getDstScreen();

		final int startIndex = screen * mRowsMulColumns;
		final int endIndex = Math.min(startIndex + mRowsMulColumns, getChildCount());
		final int startX = screen * getWidth();
		for (int i = startIndex; i < endIndex; i++) {
			CardLayout card = (CardLayout) getChildAt(i);
			if (card.getVisibility() != View.GONE) {
				Rect rect = new Rect(card.getLeft() - startX, card.getTop(), card.getRight()
						- startX, card.getBottom());
				rects.add(rect);
			}
		}
		return rects;
	}

	/**
	 * 隐藏添加卡片
	 */
	public void hideAddCard() {
		mIsHideAddCard = true;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			CardLayout card = (CardLayout) getChildAt(i);
			if (card.getType() == CardLayout.TYPE_ADD) {
				removeView(card);
				sCardCount--;
				break;
			}
		}

		// 更新指示器
		updateIndicator();
	}

	public void hideCard(int cardId) {
		getChildAt(cardId).setVisibility(View.INVISIBLE);
	}

	public void showCard(int cardId) {
		if (getChildAt(cardId) != null) {
			getChildAt(cardId).setVisibility(View.VISIBLE);
		}
	}

	public void endReplace() {
		if (mStatus == SENSE_REPLACING) {
			mStartTime = mReplaceAnimationDuration; // 完成动画
			mHasFinishRelpace = true;
		}
		mTouchState = TOUCH_STATE_REST;

		mScroller.abortAnimation();
		postInvalidate();
	}

	/**
	 * 显示添加卡片
	 */
	public void showAddCard() {
		if (getChildCount() < MAX_CARD_NUMS) {
			mIsHideAddCard = false;
			final int count = getChildCount();
			if (count <= 0 || ((CardLayout) getChildAt(count - 1)).getType() != CardLayout.TYPE_ADD) {
				addAddCardToScreen();
				sCardCount++;
			}
			// 更新指示器
			updateIndicator();
		}
	}

	/**
	 * 获取当前屏索引
	 * 
	 * @return 当前屏索引
	 */
	public int getCurScreenIndex() {
		return mCurrentScreen = mScroller.getDstScreen();
	}

	/**
	 * 放大当前屏对应索引的卡片，并返回放大后卡片的区域
	 * 
	 * @param index
	 *            当前屏的卡片索引
	 * @return 被放大后的区域
	 */
	public Rect enlargeCard(final int index) {
		final int infactIndex = getCurScreenIndex() * mRowsMulColumns + index;
		if (infactIndex < getChildCount()) {
			// 合法索引
			final CardLayout card = (CardLayout) getChildAt(infactIndex);
			final int left = sMarginLeft + (sCardWidth + sSpaceX) * (index % mColumns);
			final int top = sMarginTop + (sCardHeight + sSpaceY) * (index / mColumns);
			Rect rect = card.enlarge(left, top);
			return rect;
		}
		return null;
	}

	/**
	 * 放大当前屏对应索引的卡片，并返回放大后卡片的区域
	 * 
	 * @param index
	 *            当前屏的卡片索引
	 */
	public void resumeCard(final int index) {
		final int infactIndex = getCurScreenIndex() * mRowsMulColumns + index;
		if (infactIndex < getChildCount()) {
			CardLayout cardLayout = (CardLayout) getChildAt(infactIndex);
			if (cardLayout != null) {
				cardLayout.resume();
			}
		}
	}

	public boolean isAddCard(final int index) {
		final int infactIndex = getCurScreenIndex() * mRowsMulColumns + index;
		if (infactIndex < getChildCount()) {
			CardLayout cardLayout = (CardLayout) getChildAt(infactIndex);
			if (cardLayout != null) {
				return cardLayout.getType() == CardLayout.TYPE_ADD;
			}
		}
		return false;
	}

	public void setNormal(final int index) {
		final int infactIndex = getCurScreenIndex() * mRowsMulColumns + index;
		if (infactIndex < getChildCount()) {
			CardLayout cardLayout = (CardLayout) getChildAt(infactIndex);
			if (cardLayout != null) {
				cardLayout.setNormal();
			}
		}
	}

	public void setAdd(final int index) {
		final int infactIndex = getCurScreenIndex() * mRowsMulColumns + index;
		if (infactIndex < getChildCount()) {
			CardLayout cardLayout = (CardLayout) getChildAt(infactIndex);
			if (cardLayout != null) {
				cardLayout.setAdd();
			}
		}
	}

	/**
	 * 获取相对当前屏索引所对应的绝对索引值
	 * 
	 * @param screenIndex
	 *            当前屏索引
	 * @return 绝对索引值
	 */
	public final int getAbsScreenIndex(int screenIndex) {
		return mCurrentScreen * mRowsMulColumns + screenIndex;
	}

	void snapToScreen(int dstScreen) {
		// TODO: 把这里的更新指示器，清除焦点等方法移到外面
		if (!mScroller.isFinished()) {
			return;
		}
		mScroller.gotoScreen(dstScreen, mScrollingDuration, false);
	}

	/**
	 * 回收资源
	 */
	public void recycle() {
		// CardLayout静态资源释放
		mBorderImg = null;
		mBorderLightImg = null;
		// CardLayout.BorderDelImg = null;
		mBorderDragImg = null;
		mHomeImg = null;
		mLightHomeImg = null;
		// CardLayout.DelImg = null;
		// CardLayout.LightDelImg = null;
		mAddImg = null;
		mLightAddImg = null;
		mZoomInAnimation = null;
		mZoomOutAnimation = null;
		mNoRoomImg = null;
		mLightNoRoomImg = null;
		// CardLayout.NotPlace = null;
		// CardLayout.LightNotPlace = null;
		destroyChildrenDrawingCache();

		mHomeCard = null;
		mOpratorCardLayout = null;
		mLeavingView = null;

		// mPreviewController = null;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			CardLayout card = (CardLayout) getChildAt(i);
			card.recycle();
			card.setEventListener(null);
			// card.setPreviewController(null);
		}

		// 移除所有视图
		removeAllViews();

		if (mLocked) {
			unlock();
		}

		setBackgroundColor(mBackgroudColor);
		mStatus = SENSE_NORMAL;
		mCurrentScreen = 0;
		mIsHideAddCard = false;
		mRemoveCardId = -1;
		mLeaveCardId = -1;
	}

	@Override
	public void onCardEvent(CardLayout layout, int event) {
		// 保护
		if (layout == null || mListener == null) {
			return;
		}

		switch (event) {
			case ICardEvent.HOME_CLICK :
				if (!GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
					completeSetHome(layout);
				}
				//用户行为统计
				StatisticsData.countUserActionData(
						StatisticsData.DESK_ACTION_ID_SCREEN_PREVIEW_EDIT,
						StatisticsData.USER_ACTION_ONE, IPreferencesIds.DESK_ACTION_DATA);
				break;

			// case ICardEvent.DEL_CLICK:
			// if(!mEnableUpdate)
			// {
			// layout.clearClickState();
			// showToast(R.string.loading_screen);
			// return;
			// }
			//
			// if(layout.hasContent())
			// {
			// // showDeleteDialog(layout);
			// }
			// else
			// {
			// completeRemoveCard(layout);
			// }
			// break;

			case ICardEvent.PREVIEW_CLICK :
				mListener.preview(layout.getId());
				break;

			case ICardEvent.ADD_CLICK :
				completeAddEmptyCard();
				break;

			case ICardEvent.PREVIEW_LONG_CLICK :
				if (!mEnableUpdate) {
					layout.clearClickState();
					showToast(R.string.loading_screen);
					return;
				}

				mOpratorCardLayout = layout;
				layout.setVisibility(View.INVISIBLE);
				mListener.previewLongClick(indexOfChild(layout));

				break;

			case ICardEvent.TOUCH_DOWN :
				mOpratorCardLayout = layout;
				break;

			default :
				break;
		}
	}

	/**
	 * 是否留有添加的空间
	 * 
	 * @return 是否有剩余空间
	 */
	private boolean hasSpaceToAdd() {
		return getChildCount() <= MAX_CARD_NUMS;
	}

	void completeAddEmptyCard() {
		if (hasSpaceToAdd()) {
			if (mListener.preAddCard()) {
				// 创建一个新的CardLayout添加进来
				addEmptyCardToScreen();
			}
		}
		// else
		// {
		// // 给提示
		// showToast(R.string.no_more_screen);
		// }
	}

	//
	// void showDeleteDialog(CardLayout card)
	// {
	// mOpratorCardLayout = card;
	// if (null != mDelDialog)
	// {
	// return;
	// }
	// // mDelDialog = new AlertDialog.Builder(getContext()).create();
	// mDelDialog = new DeskAlertDialog(getContext());
	// mDelDialog.setTitle(getContext().getString(R.string.del_title_tip));
	// mDelDialog.setMessage(getContext().getString(R.string.del_content_tip));
	// mDelDialog.setButton(DialogInterface.BUTTON_POSITIVE,
	// getContext().getString(R.string.ok),
	// new DialogInterface.OnClickListener()
	// {
	// public void onClick(DialogInterface dialog, int whichButton)
	// {
	// // 确定，则删除
	// if(mOpratorCardLayout != null){
	// final CardLayout layout = SenseWorkspace.this.mOpratorCardLayout;
	// SenseWorkspace.this.mOpratorCardLayout = null;
	// completeRemoveCard(layout);
	// }
	// mDelDialog = null;
	// }
	// });
	//
	// mDelDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
	// getContext().getString(R.string.cancel),
	// new DialogInterface.OnClickListener()
	// {
	// public void onClick(DialogInterface dialog, int whichButton)
	// {
	// // 取消，则恢复底图颜色
	// if(mOpratorCardLayout != null){
	// mOpratorCardLayout.clearClickState();
	// recoverLastState();
	// SenseWorkspace.this.mOpratorCardLayout = null;
	// }
	//
	// mDelDialog = null;
	// }
	// });
	// mDelDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
	// {
	// @Override
	// public void onDismiss(DialogInterface dialog)
	// {
	// // 取消，则恢复底图颜色
	// if(mOpratorCardLayout != null){
	// mOpratorCardLayout.clearClickState();
	// SenseWorkspace.this.mOpratorCardLayout = null;
	// }
	// mDelDialog = null;
	// }
	// });
	// mDelDialog.show();
	// }

	// void dismissDeleteDialog()
	// {
	// if (mDelDialog != null)
	// {
	// try
	// {
	// mDelDialog.dismiss();
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// finally
	// {
	// mDelDialog = null;
	// }
	//
	// }
	// }

	void completeRemoveCard(CardLayout card) {
		// 作保护
		if (card == null) {
			return;
		}

		if (getChildCount() > 1) {
			mListener.removeCard(card.getId());
			card.selfDestruct();
			removeView(card);

			// // 更新指示器
			updateIndicator();

			if (card.isHome()) {
				// 第一屏为home屏
				final CardLayout newHomeCard = (CardLayout) getChildAt(0);
				newHomeCard.setHome(true);
				mHomeCard = newHomeCard;
				mListener.setCardHome(0);
			}

			if (card.isCurrent()) {
				// 第一屏为当前屏
				((CardLayout) getChildAt(0)).setCurrent(true);
				mListener.setCurrent(0);
			}

			// TODO 做删除动画
			mScroller.abortAnimation();
			mRemoveCardId = card.getId();
			mStatus = SENSE_MOVING;
			mStartTime = 0;
			setCardCount(getChildCount());
			postInvalidate();
		} else {
			// 给提示
			showToast(R.string.no_less_screen);
		}
		if (getChildCount() == MAX_CARD_NUMS - 1) {
			showAddCard();
			setCaptionY();
		}
	}

	void completeSetHome(CardLayout card) {
		// 作保护
		if (card != null) {
			if (mHomeCard != null) {
				mHomeCard.setHome(false);
			}
			card.setHome(true);
			mHomeCard = card;

			// TODO 向屏幕层发送消息
			mListener.setCardHome(mHomeCard.getId());
		}
	}

	void addEmptyCardToScreen() {
		CardLayout card = new CardLayout(getContext(), CardLayout.TYPE_PREVIEW, null, true, this);
		card.setEventListener(this);
		card.setPreviewController(mPreviewController);
		// 添加到倒数第二个，在+号之前
		addView(card, getChildCount() - 1);
		setCardCount(getChildCount());
		// 更新指示器
		updateIndicator();
		if (!hasSpaceToAdd()) {
			ScreenUtils.showToast(R.string.no_more_screen, getContext());
			hideAddCard();
		}
		setCaptionY();
	}

	/**
	 * <br>功能简述:更新指示器
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void updateIndicator() {
		if (mListener != null) {
			getScreenCount();
			mListener.updateIndicator(mCurrentScreen, mScreens);
		}
	}

	void addAddCardToScreen() {
		CardLayout cardLayout = new CardLayout(getContext(), CardLayout.TYPE_ADD, null, false, this);
		cardLayout.setEventListener(this);
		cardLayout.setPreviewController(mPreviewController);
		addView(cardLayout);
	}

	/**
	 * 工作区监听者
	 * 
	 * @author yuankai
	 * 
	 */
	public interface ISenseWorkspaceListener {
		/**
		 * 在添加之前通知外部将要添加一个卡片，看是否有条件添加
		 * 
		 * @return 是否可添加
		 */
		public boolean preAddCard();

		/**
		 * 通知外部移除卡片
		 * 
		 * @param cardId
		 *            被移除的卡片
		 */
		public void removeCard(int cardId);

		/**
		 * 设置卡片为主卡
		 * 
		 * @param cardId
		 *            卡片ID
		 */
		public void setCardHome(int cardId);

		/**
		 * 设置为当前屏
		 * 
		 * @param cardId
		 *            卡片ID
		 */
		public void setCurrent(int cardId);

		/**
		 * 预览卡片
		 * 
		 * @param cardId
		 *            卡片ID
		 */
		public void preview(int cardId);

		/**
		 * 卡片长按
		 * 
		 * @param cardId
		 *            卡片
		 */
		public void previewLongClick(int cardId);

		/**
		 * 排版完成
		 */
		public void firstLayoutComplete();

		/**
		 * 离开完成
		 */
		public void leaveFinish();

		/**
		 * 替换完成
		 */
		public void replaceFinish();

		/**
		 * 更新指示器
		 * 
		 * @param current
		 *            当前屏号
		 * @param total
		 *            总数
		 */
		public void updateIndicator(final int current, final int total);

		/**
		 * 设置指示器是否显示
		 * 
		 * @param isVisible
		 *            　true：显示
		 */
		public void setIndicatorVisible(boolean isVisible);

	}

	public void getDrawingResource() {
		PreviewController controller = mPreviewController;
		if (controller != null) {
			try {
				if (mBorderLightImg == null) {
					mBorderLightImg = controller.getLightBorderImg();
				}
				if (mBorderImg == null) {
					mBorderImg = controller.getBorderImg();
				}
				if (mLightHomeImg == null) {
					mLightHomeImg = controller.getLightHomeImg();
				}
				if (mHomeImg == null) {
					mHomeImg = controller.getHomeImg();
				}
				if (mAddImg == null) {
					mAddImg = controller.getAddImg();
				}
				if (mLightAddImg == null) {
					mLightAddImg = controller.getLightAddImg();
				}
				if (mBorderDragImg == null) {
					mBorderDragImg = controller.getFocusBorderImg();
				}
				if (mLightNoRoomImg == null) {
					mLightNoRoomImg = controller.getLightNoRoom();
				}
				if (mNoRoomImg == null) {
					mNoRoomImg = controller.getNoRoom();
				}

				if (mHomeImg != null) {
					mHomeImageWidth = mHomeImg.getIntrinsicWidth();
					mHomeImageHeight = mHomeImg.getIntrinsicHeight();
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}
		}
	}

	/**
	 * @param mStatus
	 *            the mStatus to set
	 */
	public void setmStatus(int mStatus) {
		this.mStatus = mStatus;
	}

	/**
	 * 重置开始时间为０
	 */
	public void resetmStartTimeToZero() {
		this.mStartTime = 0;
	}

	void initZoomTransition(int index) {
		int screen = index / mRowsMulColumns; // 当前屏
		index %= mRowsMulColumns; // 桌面的当前屏的预览在当前屏中的索引
		// 计算桌面的当前屏的预览在SenseWorkspace中的位置
		final int left = sWidth * screen + sMarginLeft + mLeftPadding + (sCardWidth + sSpaceX)
				* (index % mColumns) + sCardPaddingLeft;
		final int top = sMarginTop + mTopPadding + (sCardHeight + sSpaceY) * (index / mColumns)
				+ sCardPaddingTop;

		// 计算桌面的当前屏在SenseWorkspace中的位置
		mScroller.setCurrentScreen(screen);
		final float left2 = getScrollX();
		final float top2 = getScrollY();

		/*
		 * 以SenseWorkspace（不仅仅是屏幕显示部分）的左上角为参考坐标系原点， 区域1(left, top, width,
		 * height)是缩略图, 区域2(left2, top2, width2, heigth2)是屏幕
		 * 那么在动画过程中要把区域1逐渐放大为区域2，在此计算缩放中心和比例
		 */
		mCx = MyAnimationUtils.solveScaleCenterX(left, sPreViewScaleWidth, left2, sWidth);
		mCy = MyAnimationUtils.solveScaleCenterY(top, sPreViewScaleHeight, top2, sHeight);
		mScale = 1 / sPreViewScaleFactor;

		mStartTime = 0;
	}

	/**
	 * 进入动画创建当前屏显示的（9个）子视图的cache，提高动画效率
	 */
	public void enableChildrenDrawingCacheCurrently() {
		final int curIndex = SensePreviewFrame.sCurScreenId;
		final int beg = curIndex - curIndex % mRowsMulColumns;
		final int end = Math.min(mRowsMulColumns + beg, getChildCount());
		for (int i = beg; i < end; i++) {
			final CardLayout layout = (CardLayout) getChildAt(i);
			layout.enableDrawingCache();
		}
	}

	/**
	 * 完成动画创建所有子视图的cache，提高动画效率
	 */
	public void enableChildrenDrawingCacheAll() {
		for (int i = getChildCount() - 1; i >= 0; --i) {
			final CardLayout layout = (CardLayout) getChildAt(i);
			layout.enableDrawingCache();
		}
	}

	public void destroyChildrenDrawingCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final CardLayout layout = (CardLayout) getChildAt(i);
			layout.setDrawingCacheEnabled(false);
		}
	}

	public void setEnableUpdate(boolean enable) {
		mEnableUpdate = enable;
		if (enable) {
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final CardLayout layout = (CardLayout) getChildAt(i);
				layout.checkCanDelete();
				layout.postInvalidate();
				layout.destroyDrawingCache();
				layout.buildDrawingCache(true);
			}
		}
	}

	@Override
	public boolean ismEnableUpdate() {
		return mEnableUpdate;
	}

	void showToast(int id) {
		try {
			String tip = getContext().getString(id);
			DeskToast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
			tip = null;
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {
		AbstractFrame frame = GoLauncher.getTopFrame();
		if (null != frame && frame.getId() == IDiyFrameIds.GUIDE_GL_FRAME) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		}
		View focusedChild = getFocusedChild();
		if (focusedChild != null && mScroller.getDstScreen() != mCurrentScreen) {
			focusedChild.clearFocus();
		}

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurrentScreen = newScreen;
		// 更新指示器
		if (mListener != null) {
			mListener.updateIndicator(newScreen, mScreens);
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// TODO Auto-generated method stub

	}

	// @Override
	public int getScreenCount() {
		return mScreens = (getChildCount() + mRowsMulColumns - 1) / mRowsMulColumns;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
		getDesiredLayoutConfigure(w, h, 0);
		setCaptionY();
	}

	void handleEnterFinished() {
		// 完成进入动画，改变绘制状态
		setBackgroundColor(mBackgroudColor);
		// 启用所有的子视图的缓冲
		enableChildrenDrawingCacheAll();
		mListener.setIndicatorVisible(true);
		mScroller.setScreenCount(getScreenCount());
		mScroller.setCurrentScreen(mCurrentScreen);
		mStatus = SENSE_NORMAL;
		// 第一次进入预览启动向导
		// 如果是从功能表进入预览．则不启动预览向导
		if (!SensePreviewFrame.isEnterFromDragView()) {
			SensePreviewFrame.setIsEnterFromDragView(false);
			//add by jiang  设置为默认桌面后  按home键跳屏幕预览
			if (StaticTutorial.sCheckShowScreenEdit && !SensePreviewFrame.sIsHOME) {
				GuideControler.getInstance(getContext())
						.showPriviewGuide(mCurrentScreen);
			} // end if(StaticTutorial.sCheckShowScreenEdit)
		}
	}
	
	void setScrollSetting(EffectSettingInfo info) {
		if (info != null) {
			// 切屏时间和弹性效果也需要一致？
			mScrollingDuration = info.getDuration();
			mScroller.setDuration(mScrollingDuration);
			mScroller.setMaxOvershootPercent(info.getOvershootAmount());
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

	int getLeaveDuration() {
		return mLeaveDuration;
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	public void setmGoWidgetBundle(Bundle mGoWidgetBundle) {
		this.mGoWidgetBundle = mGoWidgetBundle;
	}

	@Override
	public Bundle getGoWidgetData() {
		return mGoWidgetBundle;
	}

	// public void deleteFirstScreen() {
	// // TODO Auto-generated method stub
	// CardLayout card= (CardLayout) getChildAt(0);
	// onCardEvent(card, ICardEvent.DEL_CLICK);
	// }

	public void tutorialSetMainScreen(int index) {
		// TODO Auto-generated method stub
		if (index > getChildCount()) {
			return;
		}
		CardLayout card = (CardLayout) getChildAt(index);
		onCardEvent(card, ICardEvent.HOME_CLICK);
	}

	/**
	 * 设置有足够空间添加widget的屏幕下标列表
	 * 
	 * @param list
	 */
	public void setEnoughSpaceList(ArrayList<Integer> list) {
		mEnoughSpaceList = list;
	}

	/**
	 * 获取有足够空间添加widget的屏幕下标列表
	 * 
	 * @return
	 */
	public ArrayList<Integer> getEnoughSpaceList() {
		return mEnoughSpaceList;
	}

	/**
	 * 把具有足够空间添加widget的对应的屏幕背景设置为高亮
	 * 
	 * @param list
	 */
	public void setEnoughSpaceCardBG(ArrayList<Integer> list) {
		CardLayout card;
		if (list == null) {
			return;
		}
		if (list.size() == 0) {
			card = (CardLayout) getChildAt(getChildCount() - 1);
			if (card.getType() == CardLayout.TYPE_ADD) {
				card = (CardLayout) getChildAt(getChildCount() - 1);
				card.setBackgroundDrawable(mLightAddImg);

				ArrayList<Integer> noEnoughList = new ArrayList<Integer>();
				int count = getChildCount() - 1;
				for (int i = 0; i < count; i++) {
					noEnoughList.add(i);
				}
				for (Integer index : noEnoughList) {
					card = (CardLayout) getChildAt(index);
					card.setNoRoom();
				}
			} else {
				ArrayList<Integer> noEnoughList = new ArrayList<Integer>();
				int count = getChildCount();
				for (int i = 0; i < count; i++) {
					noEnoughList.add(i);
				}
				for (Integer index : noEnoughList) {
					card = (CardLayout) getChildAt(index);
					card.setNoRoom();
				}
			}
		} else {
			for (Integer index : list) {
				card = (CardLayout) getChildAt(index);
				card.setBackgroundDrawable(mBorderImg);
			}
			ArrayList<Integer> noEnoughList = new ArrayList<Integer>();
			int count = getChildCount() - 1;
			for (int i = 0; i < count; i++) {
				if (!list.contains(i)) {
					noEnoughList.add(i);
				}
			}
			for (Integer index : noEnoughList) {
				card = (CardLayout) getChildAt(index);
				card.setNoRoom();
			}
		}
	}

	public void setmIsHideAddCard(boolean mIsHideAddCard) {
		this.mIsHideAddCard = mIsHideAddCard;
	}

	public boolean getIsHideAddCard() {
		return mIsHideAddCard;
	}

}