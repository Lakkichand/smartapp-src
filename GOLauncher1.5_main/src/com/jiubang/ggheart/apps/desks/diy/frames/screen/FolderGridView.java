package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.launcher.colorpicker.IconHighlights;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.gridscreen.GridScreenContainer;
import com.go.util.graphics.effector.subscreen.SubScreenContainer;
import com.go.util.graphics.effector.subscreen.SubScreenEffector;
import com.go.util.graphics.effector.united.CoupleScreenEffector;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenFolderInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * @author jiangxuwen
 * 
 */
public class FolderGridView extends ViewGroup
		implements
			ScreenScrollerListener,
			GridScreenContainer,
			SubScreenContainer,
			IFolderCellGridViewListner {

	// 触屏状态
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	// GridView集合
	// private ArrayList<GridView> mAllGridViews = new ArrayList<GridView>();
	// GridView的列数
	private int mNumCulumns = 4;
	// 上下文
	private Context mContext;
	// 当前触屏状态
	private int mTouchState = TOUCH_STATE_REST;
	// 上次触屏离开的x坐标
	private float mLastMotionX;
	// 消息处理器
	private IMessageHandler mMessageHandler;
	// 滚动器
	private ScreenScroller mScroller;
	// 屏幕的个数
	private int mTotalScreenNum = 1; // 初始值为1
	// 每一屏图标的最大个数
	private int mItemsCountPerScreen = 12;
	// 当前显示的是第几屏
	private int mCurrentScreen = 0;
	// 横竖屏切换标识
	private boolean mChgOrientation = false;
	// 翻屏时间
	private int mDuration = 450;
	// 弹性特效
	private int mScrollingBounce = 40;
	// 背景图片
	private Drawable mBackground;
	// 字体颜色
	private int mTextColor;
	// 字体背景颜色
	private Color mTextBgColor;
	// 底边预留高度
	private int mImageBottomH;
	// 动画的运行时间
	private long mTime;
	// 每毫秒改变的距离,即动画的运行速度，与功能表的保持一致
	private int mSpeed = 35;
	// 动画的持续帧数
	private int mTotalStep;
	// 图标特效
	private CoupleScreenEffector mDeskScreenEffector;
	//
	private int mEffectorType;
	// 图标总数
	private int mCount;
	// 是否循环滚动的标识
	private boolean mCycleMode = false;
	// 单元格宽度
	private int mGridWidth;
	// 单元格高度
	private int mGridHeight;
	// 区域的最小高度
	private int mMinHeight;
	// 画笔
	private Paint mPaint;

	private IIndicatorUpdateListner mIndicatorUpdateListner;

	// 是否在长按状态
	private boolean mIsLongClick = false;
	private boolean mReplaced = false;  // 是否发生过换位，发生过的话，退出时要把改变的位置数据写入数据库
	private boolean mRespondMove = true; // 是否响应移位
	private boolean mOutOfBound = false; // 当前拖动点是否出了响应边界
	private FolderdragItem mFolderdragItem; // 拖动数据保存体
	private FolderTargetPosition mTargetPosition; // 拖动目标位置数据
	private int mItemViewWidth = 0; // 一个图标宽度
	private int mItemViewHeight = 0; // 一个图标高度

	private ArrayList<ShortCutInfo> mContents; // 文件夹内全部内容

	private boolean mOpened = false; // 打开动画是否完成

	/**
	 * 保存dragView 原因：因为换位后，adapter.notifyDataChanged,
	 * 4.0之前不会再循环使用原来的view,原来其他地方使用的dragView不会释放也不会修改 android
	 * 4.0会不匹配地使用原来的view,dragView会被修改数据
	 */
	private View mDragView;

	/**
	 * @return the mContents
	 */
	public ArrayList<ShortCutInfo> getContents() {
		return mContents;
	}

	/**
	 * 保存文件夹内拖动的所有数据
	 * 
	 * @author ruxueqin
	 * 
	 */
	protected class FolderdragItem {
		// public View mDragView;//抓起图标
		public Bitmap mDragBmp; // 抓起bmp,用于放手后drop动画
		public Object mDragInfo; // 抓起图标的数据
		public int mDragScreen = -1; // 抓起图标所在文件夹内第几屏
		public int mDragIndex = -1; // 抓起图标在当前cellGridView中的索引

		public void clear() {
			if (null != mDragBmp) {
				mDragBmp.recycle();
				mDragBmp = null;
			}
			mDragInfo = null;
			mDragScreen = -1;
			mDragIndex = -1;
		}
	};

	/**
	 * 目标位置
	 * 
	 * @author ruxueqin
	 * 
	 */
	protected class FolderTargetPosition {
		public int mTargetScreen = -1; // 第几屏
		public int mTargetIndex = -1; // 索引

		public void clear() {
			mTargetScreen = -1;
			mTargetIndex = -1;
		}
	}

	public FolderGridView(Context context) {
		super(context);
		this.initOtherComponent(context);
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            xml属性
	 */
	public FolderGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initOtherComponent(context);
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            xml属性
	 * @param defStyle
	 *            默认风格
	 */
	public FolderGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initOtherComponent(context);
	}

	private void initOtherComponent(Context context) {
		this.mContext = context;
		mContents = new ArrayList<ShortCutInfo>();
		setScrollSetting();
		if (mScroller != null) {
			setOvershootAmount(mScrollingBounce);
			setScrollDuration(mDuration);
			setCycleMode(mCycleMode);
			mDeskScreenEffector = new CoupleScreenEffector(mScroller,
					CoupleScreenEffector.PLACE_DESK, CoupleScreenEffector.SUBSCREEN_EFFECTOR_TYPE);
			mPaint = new Paint();
		}
		applyTheme();

		mFolderdragItem = new FolderdragItem();
		mTargetPosition = new FolderTargetPosition();
	}

	private void setScrollSetting() {
		mScroller = new ScreenScroller(getContext(), this);
		// 总是有显示背景
		mScroller.setBackgroundAlwaysDrawn(true);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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

	// 横竖屏切换的调整，主要是指示器的调整
	public void changeOrientation(int orientation) {
		mChgOrientation = true;
		// mCurrentScreen
		mFolderdragItem.clear();
		mTargetPosition.clear();
		mIsLongClick = false;
	}

	public void setAdapters(ScreenFolderInfo info) {
		if (!(info instanceof UserFolderInfo)) {
			return;
		}
		UserFolderInfo folderInfo = (UserFolderInfo) info;
		// 图标总数
		mCount = folderInfo.getChildCount();
		initAdapterLayoutData();

		mContents.clear();
		int count = (mTotalScreenNum > 0) ? 1 : 0;
		for (int i = 0; i < count; i++) {
			ArrayList<ShortCutInfo> tempList = new ArrayList<ShortCutInfo>();
			for (int j = 0; j < mItemsCountPerScreen && mItemsCountPerScreen * i + j < mCount; j++) {
				final ShortCutInfo itemInfo = folderInfo.getChildInfo(mItemsCountPerScreen * i + j);
				tempList.add(itemInfo);
			}
			initGridViewData(i, tempList);
			mContents.addAll(tempList);
			tempList = null;
		}// end for
	}

	public void initGridViewDataAfterOpened(ScreenFolderInfo info) {
		if (!(info instanceof UserFolderInfo)) {
			return;
		}
		UserFolderInfo folderInfo = (UserFolderInfo) info;
		for (int i = 1; i < mTotalScreenNum; i++) {
			ArrayList<ShortCutInfo> tempList = new ArrayList<ShortCutInfo>();
			for (int j = 0; j < mItemsCountPerScreen && mItemsCountPerScreen * i + j < mCount; j++) {
				final ShortCutInfo itemInfo = folderInfo.getChildInfo(mItemsCountPerScreen * i + j);
				tempList.add(itemInfo);
			}
			initGridViewData(i, tempList);
			mContents.addAll(tempList);
			tempList = null;
		}
	}

	public void setAdapters() {
		if (null != mContents && !mContents.isEmpty()) {
			final int count = mContents.size();
			initAdapterLayoutData();

			for (int i = 0; i < mTotalScreenNum; i++) {
				ArrayList<ShortCutInfo> tempList = new ArrayList<ShortCutInfo>();
				for (int j = 0; j < mItemsCountPerScreen && mItemsCountPerScreen * i + j < count; j++) {
					final ShortCutInfo itemInfo = mContents.get(mItemsCountPerScreen * i + j);
					tempList.add(itemInfo);
				}
				initGridViewData(i, tempList);
				tempList = null;
			}
		}
	}

	private void initGridViewData(int index, ArrayList<ShortCutInfo> tempList) {
		CellGridView gridView = new CellGridView(mContext);
		gridView.setNumColumns(mNumCulumns);
		gridView.setHorizontalSpacing(0);
		gridView.setVerticalSpacing(0);
		// 针对一些特效的设false
		// gridView.setClipChildren(false);
		if (index == mTotalScreenNum - 1) {
			if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
				gridView.setAdapter(new FolderAdapter(mContext, tempList));
			} else {
				try {
					gridView.setAdapter(new FolderAdapter(mContext, tempList));
				} catch (Exception e) {
				}
			}

		} else {
			gridView.setAdapter(new FolderAdapter(mContext, tempList));
		}
		gridView.requestLayout();
		gridView.setVisibility(VISIBLE);
		if (mBackground != null) {
			gridView.setSelector(mBackground);
		}
		gridView.setmIndex(index);
		gridView.setmListner(this);
		addView(gridView);
		gridView = null;
	}

	private void initAdapterLayoutData() {
		countTotalNum(mCount);
		// Gridview的实际高度
		int realWidth = 0;
		int realHeight = 0;

		final int layoutid = GoLauncher.isLargeIcon()
				? R.layout.application_boxed_large
				: R.layout.application_boxed;
		BubbleTextView bubbleTextView = (BubbleTextView) LayoutInflater.from(this.getContext())
				.inflate(layoutid, this, false);
		int line_height = bubbleTextView.getLayoutParams().height;

		final int perHeight = line_height;
		// 行数
		if (mTotalScreenNum > 1) {
			realHeight = perHeight * (mItemsCountPerScreen / mNumCulumns);
			mTime = 320;
		} else {
			realHeight = perHeight * (mCount / mNumCulumns);
			mTime = 160 + 80 * (mCount / mNumCulumns);
			// 个数等于0或者不满整行都要补上一行
			if (mCount == 0 || mCount % mNumCulumns != 0) {
				realHeight += perHeight;
				mTime += 80;
			}
		}
		mMinHeight = realHeight;
		realHeight += mImageBottomH;
		if (GoLauncher.isLargeIcon()) {
			realHeight += 4;
		}
		realWidth = GoLauncher.getScreenWidth();

		mGridWidth = realWidth / mNumCulumns;
		mGridHeight = perHeight;

		RelativeLayout.LayoutParams gridParams = (RelativeLayout.LayoutParams) getLayoutParams();
		gridParams.height = realHeight; // 当前控件的高度
		gridParams.width = realWidth; // 当前控件的宽度
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
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
		if (mChgOrientation) {
			// 回到第一屏
			mScroller.setCurrentScreen(0);
			mChgOrientation = false;
		}
		// 设置总屏数
		mScroller.setScreenCount(mTotalScreenNum);
		
	}

	public void show() {
		setVisibility(View.VISIBLE);
	}

	public void setEffector(int type) {
		if (mEffectorType == type) {
			return;
		}
		mEffectorType = type;
		mDeskScreenEffector.setType(type);
	}

	public void setCustomRandomEffectorEffects(int[] effects) {
		mDeskScreenEffector.setDeskCustomRandomEffects(effects);
	}

	// 高质量绘图设置
	// private void setDrawQuality(boolean high){
	// int quality = high ? GridScreenEffector.DRAW_QUALITY_HIGH :
	// GridScreenEffector.DRAW_QUALITY_LOW;
	// mGridScreenEffector.setDrawQuality(quality);
	// for(int i = 0, count = getCellCount(); i < count; ++i){
	// XComponent component = getChildAt(i);
	// if(component != mDragComponent){
	// BaseAppIcon icon = (BaseAppIcon)component;
	// icon.setIconDrawQuality(high);
	// }
	// }
	// }

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mScroller.invalidateScroll();
		// super.dispatchDraw(canvas);
		canvas.save();
		final int scrollX = getScrollX();
		final int height = Math.max(mMinHeight, getHeight() - mImageBottomH);
		canvas.clipRect(scrollX, 0, getWidth() + scrollX, height);
		if (!mScroller.isFinished()) {
			mScroller.onDraw(canvas);
		} else {
			super.dispatchDraw(canvas);
		}
		canvas.restore();
	}

	public void setListeners(OnItemClickListener listener, OnItemLongClickListener longListener) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			CellGridView gridView = (CellGridView) getChildAt(i);
			if (gridView != null) {
				gridView.setOnItemLongClickListener(longListener);
				gridView.setOnItemClickListener(listener);
			}
		}
	}

	public CellGridView getCurrentView() {
		return (CellGridView) getChildAt(mCurrentScreen);
	}

	public int getCurrentViewIndex() {
		return mCurrentScreen;
	}

	@Override
	public boolean ismIsLongClick() {
		return mIsLongClick;
	}

	public int setStartDragViewIndex(View view) {
		CellGridView cellGridView = getCurrentView();
		if (cellGridView != null) {
			int count = cellGridView.getChildCount();
			for (int i = 0; i < count; i++) {
				View childView = cellGridView.getChildAt(i);
				if (childView == view) {
					return i;
				}
			}
		}
		return -1;
	}

	public void startDragItem(View view) {
		setmIsLongClick(true);

		// drag data class
		mDragView = view;
		mFolderdragItem.mDragBmp = BitmapUtility.createBitmap(view, 1.0f);

		mFolderdragItem.mDragInfo = view.getTag();
		mFolderdragItem.mDragScreen = getCurrentViewIndex();
		mFolderdragItem.mDragIndex = setStartDragViewIndex(view);

		mTargetPosition.mTargetScreen = mFolderdragItem.mDragScreen;
		mTargetPosition.mTargetIndex = mFolderdragItem.mDragIndex;

		mRespondMove = false;

		getDragData();
		updateIndicatorWhenLongClick();
		invalidate();
	}

	private void updateIndicatorWhenLongClick() {
		int cellcount = mCount;
		if (!GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			// 没锁屏就减少一个cell,因为长按要隐藏+号
			cellcount--;
		}
		updateIndicator(cellcount, mCurrentScreen);
	}

	private void updateIndicator(int cellcount, int current) {
		if (cellcount >= 0) {
			int num = 0;
			num = cellcount / mItemsCountPerScreen;
			if (cellcount % mItemsCountPerScreen > 0) {
				// 多出一屏
				num++;
			}
			mIndicatorUpdateListner.updateIndicator(num, current);
		}
	}

	/**
	 * 计算拖动需要的数据
	 */
	private void getDragData() {
		try {
			mItemViewWidth = ((CellGridView) getChildAt(0)).getChildAt(0).getWidth();
			mItemViewHeight = ((CellGridView) getChildAt(0)).getChildAt(0).getHeight();
		} catch (Exception e) {
			// 获取长按换位数据时异常，提示用户重新长按
			Toast.makeText(getContext(), "ERROR:getDragData,please long press again.",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * @param mIsLongClick
	 *            the mIsLongClick to set
	 */
	public void setmIsLongClick(boolean mIsLongClick) {
		this.mIsLongClick = mIsLongClick;
	}

	/**
	 * 此方法返回false，则手势事件会向子控件传递；返回true，则调用onTouchEvent方法。
	 */
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
				if (getCurrentView() != null) {
					getCurrentView().setmCenterPoint(0, 0);
				}
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

				CellGridView currentCellGridView = getCurrentView();
				if (currentCellGridView != null) {
					currentCellGridView.handleUpEventAfterDrag();
				}
				updateIndicator(mCount, mCurrentScreen);

				setmIsLongClick(false);

				break;
			}

			default :
				break;
		}

		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mScroller.onTouchEvent(event, action);
				break;

			case MotionEvent.ACTION_MOVE :
				mScroller.onTouchEvent(event, action);
				break;

			case MotionEvent.ACTION_UP :
				mScroller.onTouchEvent(event, action);
				mTouchState = TOUCH_STATE_REST;
				break;
			case MotionEvent.ACTION_CANCEL :
				mTouchState = TOUCH_STATE_REST;
				break;

			default :
				break;
		}
		return true;
	}

	private void updateSliderIndicator() {
		if (mMessageHandler == null) {
			return;
		}

		final int offset = mScroller.getIndicatorOffset();
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.OFFSET, offset);

		mMessageHandler.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.USER_FOLDER_UPDATE_INDICATOR,
				DesktopIndicator.UPDATE_SLIDER_INDICATOR, dataBundle, null);

		dataBundle = null;
	}

	private void updateDotsIndicator(final int current) {
		if (mMessageHandler == null) {
			return;
		}
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.CURRENT, current);
		mMessageHandler.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.USER_FOLDER_UPDATE_INDICATOR,
				DesktopIndicator.UPDATE_DOTS_INDICATOR, dataBundle, null);
		dataBundle = null;
	}

	/**
	 * 计算有几屏，一屏有几个元素
	 */
	private void countTotalNum(int count) {
		caculateLayoutData();

		mTotalScreenNum = count / mItemsCountPerScreen;
		mTotalScreenNum = count % mItemsCountPerScreen > 0 ? ++mTotalScreenNum : mTotalScreenNum;
	}

	// 获取屏幕总数
	public int getmTotalScreenNum() {
		return mTotalScreenNum;
	}

	// 匹配主题资源
	private void applyTheme() {
		GoSettingControler settingControler = GOLauncherApp.getSettingControler();
		final DesktopSettingInfo info = settingControler.getDesktopSettingInfo();
		// 设置样式
		Resources resources = getContext().getResources();
		DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
		if (themeControler != null && themeControler.isUesdTheme()) {
			DeskThemeBean themeBean = themeControler.getDeskThemeBean();
			if (themeBean != null && themeBean.mScreen != null) {
				if (themeBean.mScreen.mIconStyle != null) {
					if (themeBean.mScreen.mIconStyle.mTextBackgroud != null) {
						if (info.mCustomAppBg) {
							mBackground = IconHighlights.getDrawable(IconHighlights.TYPE_DESKTOP,
									info.mFocusColor, info.mPressColor);
						} else {
							mBackground = themeControler
									.getThemeResDrawable(themeBean.mScreen.mIconStyle.mTextBackgroud.mResName);
						}
					}
				}
				// 字体颜色先用功能表的字体颜色，以后再换回桌面的颜色
				// if (null != themeBean.mScreen.mFont)
				// {
				// int color = themeBean.mScreen.mFont.mColor;
				// int bgcolor = themeBean.mScreen.mFont.mBGColor;
				// mTextColor = (0 == color && 0 == bgcolor)? Color.WHITE:color;
				// }
			}
		}
		// 使用默认值
		if (mBackground == null) {
			if (info.mCustomAppBg) {
				mBackground = IconHighlights.getDrawable(IconHighlights.TYPE_DESKTOP,
						info.mFocusColor, info.mPressColor);
			} else {
				mBackground = resources.getDrawable(R.drawable.shortcut_selector);
			}
		}

	}

	// 启动缓存
	private void buildChildrenDrawingCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final CellGridView gridView = (CellGridView) getChildAt(i);
			gridView.buildDrawingCache(true);
		}
	}

	// 清除缓存
	public void destroyChildrenDrawingCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final CellGridView gridView = (CellGridView) getChildAt(i);
			gridView.destroyChildrenDrawingCache();
		}
		// System.gc();
	}

	// @Override
	// protected void onDraw(Canvas canvas)
	// {
	// super.onDraw(canvas);
	// }

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
		if (getCurrentView() != null) {
			getCurrentView().setStatus(CellGridView.DRAW_NORMAL);
		}
	}

	public void setmTextColor(int mTextColor) {
		this.mTextColor = mTextColor;
	}

	public void setmImageBottomH(int mImageBottomH) {
		this.mImageBottomH = mImageBottomH;
	}

	public long getmTime() {
		return mTime;
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
			mDeskScreenEffector.onAttachReserveEffector(this);
		}
	}

	/**
	 * 
	 * 从之前的UserFolder中搬运过来
	 * 
	 */
	public class FolderAdapter extends ArrayAdapter<ShortCutInfo> {
		private LayoutInflater mInflater;
		// private Drawable mBackground;
		// private ArrayList<ItemInfo> mInfoList;
		private ArrayList<ShortCutInfo> mItemInfos;

		/**
		 * @return the mItemInfos
		 */
		public ArrayList<ShortCutInfo> getItemInfos() {
			return mItemInfos;
		}

		public FolderAdapter(Context context, ArrayList<ShortCutInfo> itemInfos) {
			super(context, 0, itemInfos);
			// mInfoList = icons;
			mInflater = LayoutInflater.from(context);
			mItemInfos = itemInfos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ShortCutInfo info = null;
			try {
				info = getItem(position);
			} catch (IndexOutOfBoundsException e) {
			}
			if (info == null) {
				return null;
			}
			if (convertView == null || convertView == mDragView
					|| (null != info.mIntent && !(convertView instanceof BubbleTextView))) {
				try {
					final int id = GoLauncher.isLargeIcon()
							? R.layout.application_boxed_large
							: R.layout.application_boxed;
					convertView = mInflater.inflate(id, parent, false);
				} catch (InflateException e) {

				}
			}
			if (convertView == null) {
				return null;
			}
			if (convertView instanceof BubbleTextView) {
				final BubbleTextView textView = (BubbleTextView) convertView;
				textView.setCounter(0);
				textView.setShowShadow(false);
				textView.setIcon(info.mIcon);
				if (GOLauncherApp.getSettingControler().getDesktopSettingInfo().isShowTitle()) {
					textView.setText(info.mTitle);
				}
				if (mTextColor != 0) {
					textView.setTextColor(mTextColor);
				}
				if (mOpened) {
					// 打开动画完成后才setTag
					textView.setClickable(false);
					textView.setFocusable(false);
					textView.setTag(info);
					info.registerObserver(textView);
				}
			}
			
			return convertView;
		}
	}

	/*----------------------------------滚动器的动作监听操作----------------------------------------------*/
	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// newC
		mScroller.setScreenSize(w, h);
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		this.mScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
		setDrawQuality(true);
	}

	@Override
	public void onScrollStart() {
		postInvalidate();
	}

	@Override
	public void onFlingStart() {
		View focusedChild = getFocusedChild();
		if (focusedChild != null && mScroller.getDstScreen() != mCurrentScreen) {
			focusedChild.clearFocus();
		}
		setDrawQuality(false);

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		updateSliderIndicator();

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		// 更新指示器
		mCurrentScreen = newScreen;
		updateDotsIndicator(mCurrentScreen);
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		mCurrentScreen = currentScreen;
		setDrawQuality(true);

		// 解决换屏换位置后，有些屏的显示图标不对
		if (getCurrentView() != null) {
			getCurrentView().destroyDrawingCache();
		}
	}

	public void setmMessageHandler(IMessageHandler mMessageHandler) {
		this.mMessageHandler = mMessageHandler;

	}

	/*---------------------------屏幕特效-------------------------------*/
	@Override
	public void drawScreen(Canvas canvas, int screen) {
		CellGridView gridview = (CellGridView) getChildAt(screen);
		if (gridview != null) {
			if (!mScroller.isFinished()) {
				gridview.buildChildrenDrawingCache();
			}
			gridview.draw(canvas);
		}
	}

	@Override
	public void drawScreen(Canvas canvas, int screen, int alpha) {
		CellGridView gridview = (CellGridView) getChildAt(screen);
		if (gridview != null) {
			if (!mScroller.isFinished()) {
				gridview.buildChildrenDrawingCache();
			}
			final int count = gridview.getChildCount();
			for (int i = 0; i < count; i++) {
				BubbleTextView child = (BubbleTextView) gridview.getChildAt(i);
				if (child != null) {
					final int oldAlpha = child.getAlphaValue();
					if (alpha != oldAlpha) {
						child.setAlphaValue(alpha);
					}
					canvas.save();
					canvas.translate(child.getLeft(), child.getTop());
					child.draw(canvas);
					canvas.restore();
					if (oldAlpha != alpha) {
						child.setAlphaValue(oldAlpha);
					}
				}
			}
		}
	}

	/*----------------------------图标特效---------------------------------*/
	@Override
	public void drawGridCell(Canvas canvas, int index) {
		final int realIndex = index % mItemsCountPerScreen;
		final int screen = index / mItemsCountPerScreen;

		final CellGridView gridView = (CellGridView) getChildAt(screen);

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
				gridView.buildChildrenDrawingCache();
			}
			drawChild(canvas, view, getDrawingTime());
			canvas.restore();
		}
	}

	@Override
	public void drawGridCell(Canvas canvas, int index, int alpha) {
		final int realIndex = index % mItemsCountPerScreen;
		final int screen = index / mItemsCountPerScreen;

		final CellGridView gridView = (CellGridView) getChildAt(screen);

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
		return mCount;
	}

	@Override
	public int getCellWidth() {
		return mGridWidth;
	}

	@Override
	public int getCellHeight() {
		return mGridHeight;
	}

	@Override
	public int getCellRow() {
		return mItemsCountPerScreen / mNumCulumns;
	}

	@Override
	public int getCellCol() {
		return mNumCulumns;
	}

	public void setDrawQuality(boolean high) {
		int quality = high
				? SubScreenEffector.DRAW_QUALITY_HIGH
				: SubScreenEffector.DRAW_QUALITY_LOW;
		mDeskScreenEffector.setDrawQuality(quality);
	}

	@Override
	public int getNumColumns() {
		// TODO Auto-generated method stub
		return mNumCulumns;
	}

	@Override
	public int getItemsCountPerScreen() {
		return mItemsCountPerScreen;
	}

	@Override
	public void onReplaceFinish() {
		mReplaced = true;
		// NOTE:内存数据修改
		updateInfosAfterReplace();

		// update drag data
		if (mIsLongClick) {
			mFolderdragItem.mDragScreen = mTargetPosition.mTargetScreen;
			mFolderdragItem.mDragIndex = mTargetPosition.mTargetIndex;
		}
	}

	private void updateInfosAfterReplace() {
		int dragIndex = mFolderdragItem.mDragScreen * mItemsCountPerScreen
				+ mFolderdragItem.mDragIndex;
		int targetIndex = mTargetPosition.mTargetScreen * mItemsCountPerScreen
				+ mTargetPosition.mTargetIndex;

		try {
			ShortCutInfo info = mContents.remove(dragIndex);
			mContents.add(targetIndex, info);
			updateAdaptersData();
		} catch (Exception e) {
			Toast.makeText(getContext(), "update position info ERROR", Toast.LENGTH_LONG).show();
		}
	}

	private void updateAdaptersData() {
		int startNum = 0;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			FolderAdapter folderAdapter = (FolderAdapter) ((CellGridView) getChildAt(i))
					.getAdapter();
			ArrayList<ShortCutInfo> infos = folderAdapter.getItemInfos();
			int num = infos.size();
			infos.clear();

			for (int j = 0; j < num; j++) {
				try {
					infos.add(mContents.get(startNum));
				} catch (Exception e) {
					// 越界则不添加
				}
				startNum++;
			}
			folderAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public FolderdragItem getDragItemData() {
		return mFolderdragItem;
	}

	/**
	 * 是否发生过换位
	 * 
	 * @return
	 */
	public boolean getReplaced() {
		return mReplaced;
	}

	@Override
	public FolderTargetPosition getTargetPosition() {
		return mTargetPosition;
	}

	@Override
	public void startReplace() {

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
	}

	@Override
	public int getmItemViewWidth() {
		return mItemViewWidth;
	}

	@Override
	public int getmItemViewHeight() {
		return mItemViewHeight;
	}

	public void exchangeItem(ShortCutInfo itemInfo) {
		try {
			if (null != mFolderdragItem && mFolderdragItem.mDragScreen >= 0
					&& mFolderdragItem.mDragIndex >= 0 && null != mFolderdragItem.mDragInfo) {
				CellGridView cellGridView = (CellGridView) getChildAt(mFolderdragItem.mDragScreen);
				FolderAdapter folderAdapter = (FolderAdapter) cellGridView.getAdapter();
				ArrayList<ShortCutInfo> list = folderAdapter.getItemInfos();
				list.remove(mFolderdragItem.mDragIndex);
				list.add(mFolderdragItem.mDragIndex, itemInfo);

				int indexincontents = getCurrentViewIndex() * mItemsCountPerScreen
						+ mFolderdragItem.mDragIndex;
				mContents.remove(indexincontents);
				if (null == ((mContents.get(mContents.size() - 1))).mIntent) {
					// 最后一个是+号
					mContents.add(mContents.size() - 1, itemInfo);
				} else {
					mContents.add(mContents.size(), itemInfo);
				}
				updateAdaptersData();
			}
		} catch (Exception e) {
			// 交换发生异常就退出folder层
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					IDiyFrameIds.DESK_USER_FOLDER_FRAME, null, null);
		}
	}

	/**
	 * 删除一项ＵＩ项
	 * 
	 * @param itemInfo
	 */
	public void deleteItem(ItemInfo itemInfo) {
		mContents.remove(itemInfo);
		mCount--;
		int contentsSize = mContents.size();
		if (contentsSize <= 1 || (contentsSize == 2 && null == (mContents.get(1)).mIntent)) {
			// 达到了删除文件夹的要求，退出文件夹层
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					IDiyFrameIds.DESK_USER_FOLDER_FRAME, null, null);
		}

		updateAdaptersData();

		int contentPageNum = contentsSize / mItemsCountPerScreen;
		if (contentsSize % mItemsCountPerScreen != 0) {
			contentPageNum++;
		}
		int childCount = getChildCount();
		int removeCount = childCount - contentPageNum;
		if (removeCount > 0) {
			// 删除项后更新显示
			removeViews(childCount - removeCount, removeCount);

			mTotalScreenNum = contentPageNum;
			mScroller.setScreenCount(contentPageNum);
			mCurrentScreen = (mCurrentScreen < contentPageNum) ? mCurrentScreen : 0;
			mScroller.setCurrentScreen(mCurrentScreen);

			// 更新指示器
			Bundle data = new Bundle();
			data.putInt(DesktopIndicator.TOTAL, mTotalScreenNum);
			data.putInt(DesktopIndicator.CURRENT, mCurrentScreen);
			mMessageHandler.handleMessage(this, IMsgType.SYNC,
					IDiyMsgIds.USER_FOLDER_UPDATE_INDICATOR, DesktopIndicator.UPDATE_SCREEN_NUM,
					data, null);

			mMessageHandler.handleMessage(this, IMsgType.SYNC,
					IDiyMsgIds.USER_FOLDER_UPDATE_INDICATOR,
					DesktopIndicator.UPDATE_DOTS_INDICATOR, data, null);

			if (!GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
				CellGridView cellGridView = (CellGridView) getChildAt(getChildCount() - 1);
				FolderAdapter folderAdapter = (FolderAdapter) cellGridView.getAdapter();
			}

			postInvalidate();
		}
	}

	/**
	 * @param mOutOfBound
	 *            the mOutOfBound to set
	 */
	public void setmOutOfBound(boolean mOutOfBound) {
		this.mOutOfBound = mOutOfBound;
	}

	/**
	 * @param mIndicatorUpdateListner
	 *            the mIndicatorUpdateListner to set
	 */
	public void setmIndicatorUpdateListner(IIndicatorUpdateListner mIndicatorUpdateListner) {
		this.mIndicatorUpdateListner = mIndicatorUpdateListner;
	}

	/**
	 * @param mCurrentScreen
	 *            the mCurrentScreen to set
	 */
	public void setmCurrentScreen(int mCurrentScreen) {
		this.mCurrentScreen = mCurrentScreen;
	}

	@Override
	public boolean isOutOfBound() {
		return mOutOfBound;
	}

	@Override
	public boolean isScrollFinished() {
		return mScroller.isFinished();
	}

	private void caculateLayoutData() {
		if (GoLauncher.isPortait()) {
			try {
				mNumCulumns = GoSettingControler.getInstance(getContext()).getDesktopSettingInfo().mColumn;
				mItemsCountPerScreen = mNumCulumns * 3;
			} catch (Exception e) {

			}
		} else {
			// 计算mNumCulumns
			// 方法：取高度，令宽度＝高度，计算mNumCulumns
			final int id = GoLauncher.isLargeIcon()
					? R.dimen.user_folder_line_height_large
					: R.dimen.user_folder_line_height;
			final int line_height = mContext.getResources().getDimensionPixelSize(id);
			final int screenWidth = GoLauncher.getScreenWidth();
			mNumCulumns = screenWidth / line_height;

			mItemsCountPerScreen = mNumCulumns;
		}
	}

	/**
	 * @param mOpened
	 *            the mOpened to set
	 */
	public void setmOpened(boolean mOpened) {
		this.mOpened = mOpened;
	}

	/**
	 * 对里面内容进行重新排列
	 * 
	 * @param type
	 */
	public void sortIcon() {
		if (mContents == null) {
			return;
		}
		updateAdaptersData();
	} // end sortIcon
}
