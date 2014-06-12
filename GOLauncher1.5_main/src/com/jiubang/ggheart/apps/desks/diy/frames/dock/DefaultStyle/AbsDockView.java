package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.CompatibleUtil;
import com.go.util.Utilities;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ScreenDragHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderRelativeLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IScreenFolder;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.components.DeskIcon;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.SysShortCutControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenFolderInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.plugin.notification.NotificationControler;
import com.jiubang.ggheart.plugin.notification.NotificationType;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * 
 * <br>类描述:dock视图基类
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-10-16]
 */
public abstract class AbsDockView extends ViewGroup
		implements
			OnFocusChangeListener,
			OnDockSettingListener,
			OnDockGestureListner,
			OnClickListener,
			OnLongClickListener,
			IDragPositionListner {
	public static boolean sPortrait; // 是否竖屏
	public final static int DRAW_STATUS_NORMAL = 1; // 正常状态
	public final static int DRAW_STATUS_INTO_FOLDER_ZOOMOUT = 2; // 图标放入文件夹缩小动画过程
	public final static int DRAW_STATUS_MERGE_FOLDER = 3; // 合并文件夹过程
	protected int mDrawStatus = DRAW_STATUS_NORMAL; // 绘制状态

	public IOperationHandler mOperationHandler;
	protected DockViewUtil mUtil; // 静态工具类
	protected int mDockBgHeight; // dock栏的背景高度
	protected View mDragView = null; // 移动的View

	protected Drawable mBg; // 底座图片
	protected int mLayoutH; // 当前排版区域高,与横竖屏相关
	protected int mLayoutW; // 当前排版区域宽,与横竖屏相关
	protected int mIconPortraitH; // 竖屏图标高,即LineLayout的行高
	protected int mIconLandscapeW; // 横屏图标宽，即LineLayout的列宽

	protected final int mFolderZoomoutAnimationContinueDuration = 200; // 合并文件夹动画缩小动画持续时间
	protected Drawable mMergeFolderZoomoutDrawable; // 进入文件夹的缩小动画的drawable
	protected Rect mSrcZoomoutRect; // 进入文件夹的缩小动画的开始矩形
	protected Rect mTargetZoomoutRect; // 进入文件夹的缩小动画的目标矩形
	protected float mZoomoutStartTime = 0; // 进入文件夹的缩小动画开始时间点
	protected DockItemInfo mZoomoutFolderInfo; // 用于在动画做完后刷新此info对应图标
	protected Drawable mMergeFolderDrawable; // 合并生成文件夹的底图
	protected float mMergeFolderStartTime = 0;
	protected DockIconView mMergeFolderView;
	protected final int mergeFolderAnimDuration = 300;
	protected int mMergeFolderCenterX; // 合并文件夹的底图中心点x
	protected int mMergeFolderCenterY; // 合并文件夹的底图中心点y

	protected DockLineLayoutContainer mLineLayoutContainer; // 3条LineLayout的父容器
	protected HashMap<Integer, ArrayList<DockIconView>> mIconViewsHashMap; // 装载三行dock条的hashMap
	protected DockIconView mOpenFolderView; // 文件夹打开状态的view
	protected DockIconView mLastMergeFolderView; // 上一次合并文件夹的底view
	protected int mIndexOnMiddle; // 当前在哪个图标中央，不在中央为-1

	protected ArrayList<DockIconView> mCurDockViewList; // 每条LineLayout所包含的DockIconView
	protected Context mContext;
	protected DockLogicControler mDockControler;
	protected DockIconView mMergeFordleIconToOpen; // 刚完成合并，等待自动打开的文件夹

	/**BEGIN 挤压移动到桌面动画逻辑*/
	private ImageView mMoveToScreenView; //挤压到桌面动画视图
	protected DockIconView mLastMoveToScreenView; //上一个挤压视图
	private int[] mScreenCell = null; //在屏幕层当前屏上安放的网格
	private Rect mScreenRect = null; //在屏幕层上的安放位置
	private ValueAnimator mTransactionAnimator; //位移动画
	private ValueAnimator mShakeAnimator; //抖动动画

	public boolean mIsRedBg = false;	//是否需要添加红色背景
	
	public AbsDockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mUtil = new DockViewUtil();

		mDockBgHeight = DockUtil.getBgHeight();
		mIconPortraitH = mDockBgHeight;
		mIconLandscapeW = mDockBgHeight;

		mLineLayoutContainer = new DockLineLayoutContainer(context);
		mContext = context;
	}

	/***
	 * 初始化
	 */
	public void init() {
		// 初始化数据
		initIconsData();

		// 初始化背景
		initBg();

		// 排版
		initLayout();
	};

	/**
	 * 获取每个快捷键的包含图标个数
	 */
	protected void initIconsData() {
		// 清空之前的数据
		if (null != mIconViewsHashMap) {
			int size = mIconViewsHashMap.size();
			for (int i = 0; i < size; i++) {
				int iconsize = mIconViewsHashMap.get(i).size();
				for (int j = 0; j < iconsize; j++) {
					DockIconView view = mIconViewsHashMap.get(i).get(j);
					view.getInfo().unRegisterObserver(view);
					view = null; // 内存回收
				}
			}
			mIconViewsHashMap.clear();
			mIconViewsHashMap = null;
		}

		// 重新获取数据
		mIconViewsHashMap = new HashMap<Integer, ArrayList<DockIconView>>();
		ConcurrentHashMap<Integer, ArrayList<DockItemInfo>> infoMap = mDockControler
				.getShortCutItems();
		int size = infoMap.size();
		for (int i = 0; i < size; i++) {
			ArrayList<DockIconView> mDockIconViewList = initIcon(i);
			mIconViewsHashMap.put(i, mDockIconViewList);
		}
	}

	/**
	 * 初始化背景图片
	 */
	private void initBg() {
		mBg = mDockControler.getDockBgDrawable();
		updateBgBounds();
	}

	/**
	 * 功能简述:更新底座图片mBg的bounds <br>
	 * 功能详细描述:与{@link#dispatchDraw(Canvas canvas)}绘制底座位置计算相关} <br>
	 * 注意:竖屏直接放在屏幕底；横屏放在屏幕中间，由{@link#dispatchDraw(Canvas canvas)}旋转、移动canvas计算位置
	 */
	private void updateBgBounds() {
		if (mBg != null) {
			if (sPortrait) {
				mBg.setBounds(0, mLayoutH - mDockBgHeight, mLayoutW, mLayoutH);
			} else {
				int left = (mLayoutW - mLayoutH) / 2;
				int right = (mLayoutW + mLayoutH) / 2;
				int top = (mLayoutH / 2) - mDockBgHeight;
				int bottom = mLayoutH / 2;

				mBg.setBounds(left, top, right, bottom);
			}
		}
	}

	/**
	 * 排版，必须在所有资源准备好后
	 */
	public void initLayout() {
		reLayout();
		// 滚动器显示页初始化
		mLineLayoutContainer.setScreenCount(mLineLayoutContainer.getChildCount());
		int orientation = sPortrait ? ScreenScroller.HORIZONTAL : ScreenScroller.VERTICAL;
		mLineLayoutContainer.setOrientation(orientation);
	}

	/**
	 * 重新排版
	 */
	public void reLayout() {
		if (null == mIconViewsHashMap) {
			return;
		}
		caculateIconsPadding();
		removeAllViews();
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		int size = mIconViewsHashMap.size();
		for (int i = 0; i < size; i++) {
			ArrayList<DockIconView> dockViewList = mIconViewsHashMap.get(i);
			int listCount = dockViewList.size();
			AbsLineLayout layout = getLineLayout();
			if (layout != null) {
				layout.setLineID(i);
				for (int j = 0; j < listCount; j++) {
					DockIconView view = dockViewList.get(j);
					// if (view.getParent() != null) {
					// ((ViewGroup) view.getParent()).removeView(view);
					// }
					layout.addView(view);
				}
				mLineLayoutContainer.addView(layout, params);
			}
		}
		addView(mLineLayoutContainer);
	}

	/**
	 * 各图标的边距，初始化及
	 */
	protected void caculateIconsPadding() {
		if (null == mIconViewsHashMap) {
			return;
		}
		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;
		int bitmap_size = 0;
		int size = mIconViewsHashMap.size();
		for (int i = 0; i < size; i++) {
			ArrayList<DockIconView> dockViewList = mIconViewsHashMap.get(Integer.valueOf(i));
			int listCount = dockViewList.size();
			if (listCount != 0) {
				bitmap_size = getDockIconSize(listCount);
				if (sPortrait) {
					// 确认padding的大小
					left = right = 0;
					top = bottom = (mDockBgHeight - bitmap_size) / 2;
				} else {
					// 确认padding的大小
					top = bottom = 0;
					left = right = (mDockBgHeight - bitmap_size) / 2;
				}
			}
			for (int j = 0; j < listCount; j++) {
				DockIconView mDockIconView = dockViewList.get(j);
				mDockIconView.setPadding(left, top, right, bottom);
			}
		}
	}

	public DockIconView getCurretnIcon() {
		return mLineLayoutContainer.getCurrentIcon();
	}

	public void setCurretnIcon(DockIconView view) {
		mLineLayoutContainer.setCurrentIcon(view);
	}

	/**
	 * 更新背景图片
	 */
	public void updateSlaverBg() {
		initBg();
		postInvalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mLayoutH = b - t;
		mLayoutW = r - l;

		if (changed) {
			caculateIconsPadding();
		}
		if (sPortrait) {
			layoutPort(changed, l, t, r, b);
		} else {
			layoutLand(changed, l, t, r, b);
		}

		updateBgBounds();
	}

	protected void layoutPort(boolean changed, int l, int t, int r, int b) {
		final int top = b - mIconPortraitH;

		mLineLayoutContainer.layout(l, top, r, b);
	}

	protected void layoutLand(boolean changed, int l, int t, int r, int b) {
		final int left = r - mIconLandscapeW;

		mLineLayoutContainer.layout(left, t, r, b);
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		switch (mDrawStatus) {
			case DRAW_STATUS_NORMAL :
				dispatchDrawNormal(canvas);
				break;

			case DRAW_STATUS_INTO_FOLDER_ZOOMOUT :
				dispatchDrawFolderZoomout(canvas);
				break;

			case DRAW_STATUS_MERGE_FOLDER :
				dispatchDrawMergeFolder(canvas);
				break;

			default :
				break;
		}
	}

	public void dispatchDrawMergeFolder(Canvas canvas) {
		int currentTime = 0;
		if (mMergeFolderStartTime == 0) {
			mMergeFolderStartTime = SystemClock.uptimeMillis();
		} else {
			currentTime = (int) (SystemClock.uptimeMillis() - mMergeFolderStartTime);
		}

		final float time = currentTime >= mergeFolderAnimDuration ? 1 : currentTime
				/ (float) mergeFolderAnimDuration;
		// NOTE:画文件夹底图动画
		// 合并文件夹动画
		if (null != mMergeFolderDrawable) {
			float zoomProportion = getZoomProportion();
			float scale_width = DockViewUtil.easeOut(0.6f, zoomProportion, time);
			float scale_height = DockViewUtil.easeOut(0.6f, zoomProportion, time);
			float alpha = DockViewUtil.easeOut(100.0f, 255.0f, time);
			int w = (int) (DrawUtils.dip2px(48f) * scale_width);
			int h = (int) (DrawUtils.dip2px(48f) * scale_height);
			int l = mMergeFolderCenterX - w / 2;
			int t = mMergeFolderCenterY - h / 2;
			int r = l + w;
			int b = t + h;

			mMergeFolderDrawable.setBounds(l, t, r, b);
			mMergeFolderDrawable.setAlpha((int) alpha);
			mMergeFolderDrawable.draw(canvas);
		}
		if (time >= 1) {
			handleMergeFolderTimeout();
		} else {
			postInvalidate();
		}
		dispatchDrawNormal(canvas);
	}

	/**
	 * 绘制普通状态
	 * 
	 * @param canvas
	 */
	protected void dispatchDrawNormal(Canvas canvas) {
		// draw bg
		if (null != getSettingInfo() && getSettingInfo().mBgPicSwitch && mBg != null) {
			canvas.save();
			if (sPortrait) {
				mBg.draw(canvas);
			} else {
				int px = mLayoutW / 2;
				int py = mLayoutH / 2;
				canvas.rotate(-90, px, py);
				canvas.translate(0, mLayoutW / 2);

				mBg.draw(canvas);
			}
			setRedBg(canvas);
			canvas.restore();
		}

		super.dispatchDraw(canvas);
	}

	/***
	 * 图标放入文件夹缩小动画过程
	 * 
	 * @param canvas
	 */
	protected void dispatchDrawFolderZoomout(Canvas canvas) {
		dispatchDrawNormal(canvas);

		int currentTime = 0;
		if (mZoomoutStartTime == 0) {
			mZoomoutStartTime = SystemClock.uptimeMillis();
		} else {
			currentTime = (int) (SystemClock.uptimeMillis() - mZoomoutStartTime);
		}

		final float time = currentTime >= mFolderZoomoutAnimationContinueDuration ? 1 : currentTime
				/ (float) mFolderZoomoutAnimationContinueDuration;

		if (null != mSrcZoomoutRect && null != mTargetZoomoutRect
				&& null != mMergeFolderZoomoutDrawable) {
			final int scale_width = (int) DockViewUtil.easeOut(mSrcZoomoutRect.width(),
					mTargetZoomoutRect.width(), time);
			final int scale_height = (int) DockViewUtil.easeOut(mSrcZoomoutRect.height(),
					mTargetZoomoutRect.height(), time);
			final int left = (int) DockViewUtil.easeOut(mSrcZoomoutRect.left,
					mTargetZoomoutRect.left, time);
			final int top = (int) DockViewUtil.easeOut(mSrcZoomoutRect.top, mTargetZoomoutRect.top,
					time);
			mMergeFolderZoomoutDrawable
					.setBounds(left, top, left + scale_width, top + scale_height);
			mMergeFolderZoomoutDrawable.draw(canvas);
		}

		if (currentTime > mFolderZoomoutAnimationContinueDuration) {
			handleIntoFolderZoomoutTimeout();
		} else {
			postInvalidate();
		}
	}

	/**
	 * 启动进入文件夹后的缩小动画
	 * 
	 * @param dragView
	 *            拖拽视图
	 * @param targetView
	 *            拖拽到目标网格所在原来的视图，用来计算缩小动画的初始数据
	 * @param dockItemInfo
	 *            用于在动画做完后刷新此info对应图标
	 * @param rect
	 *            　拖拽的矩形
	 * @param position
	 *            放入文件夹第几个图标　从1开始
	 * @return
	 */
	// 暂没有用到
	public boolean startIntoFolderZoomoutAnimation(View dragView, View targetView,
			DockItemInfo dockItemInfo, Rect rect, int position) {
		if (null == rect || null == dragView || null == targetView || position <= 0) {
			return false;
		}

		Drawable drawable = null;
		if (dragView instanceof DeskIcon) {
			// 桌面与文件夹内图标
			drawable = ((DeskIcon) dragView).getCompoundDrawables()[1];
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
			if (targetView instanceof DockIconView) {
				// 计算targetRect
				mTargetZoomoutRect = DockViewUtil.getAIconRectInAFolder(position,
						(DockIconView) targetView);

				// 计算srcRect
				mSrcZoomoutRect = ScreenUtils.getZoomoutSrcRect(rect, dragView);

				if (null != mSrcZoomoutRect && null != mTargetZoomoutRect) {
					mZoomoutFolderInfo = dockItemInfo;
					// 开始动画
					mDrawStatus = DRAW_STATUS_INTO_FOLDER_ZOOMOUT;
					return true;
				}
			}
		}

		return false;
	}

	/***
	 * 图标放入文件夹缩小动画完成
	 * 
	 * @param canvas
	 */
	protected void handleIntoFolderZoomoutTimeout() {
		recycleMergeFolderCache();
	}

	/***
	 * 文件夹合并动画完成后的处理
	 */
	protected void handleMergeFolderTimeout() {

	};

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v instanceof DockIconView) {
			DockIconView view = (DockIconView) v;
			if (hasFocus) {
				mUtil.judgeShowCurrentIconLight(view);
			} else {
				view.setmIsBgShow(false);
			}
		}
	}

	protected DockIconView initIcon(int rowid, int index) {
		DockItemInfo info = null;
		try {
			info = getShortCutItems().get(rowid).get(index);
		} catch (Exception e) {
			// 后台数据异常
		}
		if (null == info) {
			// 说明这一行这个索引没有数据
			return null;
		}
		return initIcon(info);
	}

	protected DockIconView initIcon(DockItemInfo info) {
		if (null == info) {
			return null;
		}

		Bitmap bitmap = mDockControler.getIconBitmap(info);
		DockIconView iconView = new DockIconView(getContext());
		iconView.setImageBitmap(bitmap);

		iconView.setOnTouchListener(mLineLayoutContainer);
		iconView.setOnClickListener(this);
		iconView.setOnLongClickListener(this);
		iconView.setFocusable(true);
		iconView.setOnFocusChangeListener(this);
		iconView.setInfo(info);

		return iconView;
	}

	/**
	 * 发消息给拖动层：开始拖动，创建缩略图
	 * 
	 * @param dragType
	 * @param v
	 */
	private void startDragFram(int dragType, View v) {
		setLongClicked();
		GoLauncher.sendMessage(mMessageHandler, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_START,
				dragType, v, null);
	}

	public void setDockViewTag(DockIconView v) {
		ItemInfo itemInfo = v.getInfo().mItemInfo;
		v.setTag(itemInfo);
	}

	@Override
	public void respondGesture(Intent intent) {
		DockIconView curView = getCurretnIcon();
		if (null == curView) {
			return;
		}
		ArrayList<Rect> posArrayList = new ArrayList<Rect>();
		Rect rect = new Rect();
		curView.getGlobalVisibleRect(rect);
		posArrayList.add(rect);

		if (Machine.isIceCreamSandwichOrHigherSdk() && intent != null
				&& ICustomAction.ACTION_ENABLE_SCREEN_GUARD.equals(intent.getAction())) {
			curView.invalidate();
		}
		GoLauncher.sendMessage(mMessageHandler, IDiyFrameIds.SCHEDULE_FRAME,
				IDiyMsgIds.START_ACTIVITY, -1, intent, posArrayList);
		posArrayList.clear();
		posArrayList = null;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
		}
		boolean oldsPortrait = sPortrait;
		sPortrait = heightSpecSize > widthSpecSize;
		if (sPortrait != oldsPortrait) {
			doWithDirectionChange();
		}
	}

	/**
	 * 切屏处理
	 */
	protected void doWithDirectionChange() {
		if (null != getCurretnIcon()) {
			getCurretnIcon().setVisibility(View.VISIBLE);
		}
		recycleMergeFolderCache();
		clearAnimationAndresetFlag();
		reLayout();
		int orientation = sPortrait ? ScreenScroller.HORIZONTAL : ScreenScroller.VERTICAL;
		mLineLayoutContainer.setOrientation(orientation);
	}

	public void doStyleChange() {

	}

	/**
	 * 内存释放
	 */
	public void clearSelf() {
		removeAllViews();
	}

	@Override
	public void removeAllViews() {
		int count = mLineLayoutContainer.getChildCount();
		for (int i = 0; i < count; i++) {
			AbsLineLayout lineLayout = (AbsLineLayout) mLineLayoutContainer.getChildAt(i);
			lineLayout.removeAllViews();
		}
		mLineLayoutContainer.removeAllViews();

		if (null != mIconViewsHashMap) {
			int size = mIconViewsHashMap.size();
			for (int i = 0; i < size; i++) {
				ArrayList<DockIconView> dockViewList = mIconViewsHashMap.get(i);
				int listCount = dockViewList.size();
				AbsLineLayout layout = getLineLayout();
				if (layout != null) {
					for (int j = 0; j < listCount; j++) {
						DockIconView view = dockViewList.get(j);
						if (view.getParent() != null) {
							((ViewGroup) view.getParent()).removeView(view);
						}
					}
				}
			}
		}
		super.removeAllViews();
	}

	/**
	 * 修改设置里面的行数
	 * 
	 * @throws IllegalAccessException
	 */
	private void doWithRowChange() throws IllegalArgumentException {
		// 1:infoMap改变
		mDockControler.doWithRowChange();

		// 2:view改变
		if (mIconViewsHashMap == null) {
			// 当修改自适应模式与行数同时发生时，mIconViewsHashMap还没有初始化,此时行数变化响应，会在自适应模工切换后重新进行初始化时得到修改
			return;
		}
		ShortCutSettingInfo settingInfo = GOLauncherApp.getSettingControler()
				.getShortCutSettingInfo();
		int numOfRowInSetting = settingInfo.mRows;
		if (numOfRowInSetting <= 0 || numOfRowInSetting > DockUtil.TOTAL_ROWS) {
			throw new IllegalArgumentException("setting row is wrong.row = " + numOfRowInSetting);
		}
		int oldRow = mIconViewsHashMap.size();
		if (numOfRowInSetting > oldRow) {
			// 加行
			do {
				ArrayList<DockIconView> list = initIcon(oldRow);
				mIconViewsHashMap.put(oldRow, list);
				oldRow++;
			} while (numOfRowInSetting > oldRow);
		} else if (numOfRowInSetting < oldRow) {
			// 减行
			do {
				oldRow--;
				ArrayList<DockIconView> list = mIconViewsHashMap.remove(oldRow);
				for (DockIconView dockIconView : list) {
					dockIconView.clearSelf();
				}
			} while (numOfRowInSetting < oldRow);
		}

		// 3:重排版
		initLayout();
		mLineLayoutContainer.setCurrentScreen(0);
	}

	/**
	 * 清除合并文件夹动画的缓存
	 */
	private void recycleMergeFolderCache() {
		// 某手机不能自动刷新，需要手动强制刷新
		if (mDrawStatus == DRAW_STATUS_MERGE_FOLDER) {
			mDrawStatus = DRAW_STATUS_NORMAL;
			postInvalidate();
		}

		mDrawStatus = DRAW_STATUS_NORMAL;
		// 放入文件夹
		mZoomoutFolderInfo = null;
		mMergeFolderZoomoutDrawable = null;
		mSrcZoomoutRect = null;
		mTargetZoomoutRect = null;
		mZoomoutStartTime = 0;

		// 合并文件夹
		mMergeFolderView = null;
		mMergeFolderDrawable = null;
		mMergeFolderStartTime = 0;
		mLastMergeFolderView = null;
		mIndexOnMiddle = -1;
		if (null != mOpenFolderView) {
			mDockControler.closeFolderIcon(mOpenFolderView.getInfo());
			mOpenFolderView = null;
		}

		mHandler.removeMessages(DockUtil.HANDLE_ANIMATION_MERGE_FOLDER);

	}

	protected void setmDrawStatus(int status) {
		mDrawStatus = status;
	}

	public int getmDrawStauts() {
		return mDrawStatus;
	}

	/**
	 * 获取上一次合并文件夹的底view
	 * 
	 * @return
	 */
	public DockIconView getmMergeFolderTargetView() {
		return mMergeFolderView;
	}

	public int getmIndexOnMiddle() {
		return mIndexOnMiddle;
	}

	@Override
	public void onLeft(DockIconView dockIconView) {
		recycleMergeFolderCache();
		isStartViewMoveToScreenAnim(dockIconView);
	}

	@Override
	public void onRight(DockIconView dockIconView) {
		recycleMergeFolderCache();
		isStartViewMoveToScreenAnim(dockIconView);
	}

	@Override
	public void setRecycleDragCache() {
		recycleMergeFolderCache();
	}

	@Override
	public void onMiddle(DockIconView dockIconView, Rect rect, int indexinrow) {
		Object obj = mDragView.getTag();
		if (null == obj || dockIconView == null || rect == null || !(obj instanceof ShortCutInfo)) {
			return;
		}

		clearMoveToScreenAnim();
		if (dockIconView.getInfo().mItemInfo instanceof ShortCutInfo) {
			if (dockIconView != mLastMergeFolderView) {
				mHandler.removeMessages(DockUtil.HANDLE_ANIMATION_MERGE_FOLDER);
				Message msg = new Message();
				msg.what = DockUtil.HANDLE_ANIMATION_MERGE_FOLDER;
				msg.obj = dockIconView;
				msg.arg1 = rect.centerX();
				msg.arg2 = rect.centerY();
				mHandler.sendMessageDelayed(msg, 300);
				mLastMergeFolderView = dockIconView;
			}
		} else if (dockIconView.getInfo().mItemInfo instanceof UserFolderInfo
				&& dockIconView != mOpenFolderView) {
			if (mDockControler != null) {
				mDockControler.openFolderIcon(dockIconView.getInfo());
			}
			//修复 (ADT-7929) 在Dock条文件夹间快速拖动图标 文件打开状态未关   add by zzf 
			//给mOpenFolderView 重新指向之前先置空，置空前先关闭文件打开Icon
			if (null != mOpenFolderView && null != mDockControler) {
				mDockControler.closeFolderIcon(mOpenFolderView.getInfo());
				mOpenFolderView = null;
			}
			//end 
			mOpenFolderView = dockIconView;
		}
		mIndexOnMiddle = indexinrow;
	}

	/**
	 * 当桌面图标拖动Dock条时进行初始化区域
	 * 
	 * @param mMergeFolderView
	 *            the mMergeFolderView to set
	 */
	public void startMergeFolder(DockIconView dockIconView, int centerX, int centerY) {
		if (mDrawStatus == DRAW_STATUS_MERGE_FOLDER) {
			return;
		}

		mMergeFolderCenterX = centerX;
		mMergeFolderCenterY = centerY;
		try {
			mMergeFolderView = dockIconView;
			if (null == mMergeFolderDrawable) {
				mMergeFolderDrawable = getContext().getResources().getDrawable(
						R.drawable.appfunc_folderback_4_def3);
			}
			mMergeFolderStartTime = 0;

			mDrawStatus = DRAW_STATUS_MERGE_FOLDER;
		} catch (Exception e) {

		}
		postInvalidate();
	}

	private Activity mActivity;
	private IMessageHandler mMessageHandler;

	/**
	 * 设置监听者
	 * 
	 * @param handler
	 */
	public void setHandler(IOperationHandler operationHandler, Activity activity,
			IMessageHandler messageHandler, DockLogicControler dockControler) {
		mOperationHandler = operationHandler;
		mMessageHandler = messageHandler;
		mActivity = activity;

		mDockControler = dockControler;
		mLineLayoutContainer.setmGestureListner(this);
	}

	public void setLongClicked() {
		mLineLayoutContainer.setLongClicked();
	}

	/**
	 * 获取LineLayout的父容器
	 */
	public DockLineLayoutContainer getLineLayoutContainer() {
		if (mLineLayoutContainer == null) {
			mLineLayoutContainer = new DockLineLayoutContainer(mContext);
		}
		return mLineLayoutContainer;
	}

	/**
	 * 返回装载三行dock条的hashMap
	 * 
	 * @return
	 */
	public HashMap<Integer, ArrayList<DockIconView>> getIcons() {
		return mIconViewsHashMap;
	}

	/**
	 * 获取文件夹打开状态的view
	 * 
	 * @return
	 */
	public DockIconView getmOpenFolderView() {
		return mOpenFolderView;
	}

	public boolean refreshUiChange(DockIconView view, int index) {
		return false;
	}

	/**
	 * 修改图标索引 1:先判断保证可以修改索引(索引都正确且不重复) 2:修改索引
	 */
	public boolean modifyShortcutItemIndexUnfit() {
		ArrayList<DockIconView> currentDockIcons = getRowDockIcons(getLineLayoutContainer()
				.getCurLine());
		return mDockControler.modifyShortcutItemIndexUnfit(currentDockIcons);
	}

	/**
	 * UI、内存数据校验
	 */
	public void verifyData() {
		boolean correct = true;
		int infoSize = getShortCutItems().size();
		int iconSize = getIcons().size();
		if (infoSize != iconSize) {
			correct = false;
		} else {
			for (int i = 0; i < iconSize; i++) {
				ArrayList<DockIconView> icons = getIcons().get(i);
				int lineIconSize = icons.size();
				if (lineIconSize != getShortCutItems().get(i).size()) {
					correct = false;
					break;
				}
				for (int j = 0; j < lineIconSize; j++) {
					DockIconView view = icons.get(j);
					if (null == view || null == view.getInfo()) {
						correct = false;
						break;
					}
				}
			}
		}
		if (!correct) {
			// 前后台数据有误，重新初始化
			init();
			int size = getShortCutItems().size();
			for (int i = 0; i < size; i++) {
				updateLineLayoutIconsSize(i);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v instanceof DockIconView) {
			DockIconView view = (DockIconView) v;
			DockItemInfo info = view.getInfo();
			if (null != info) {
				AppItemInfo appInfo = info.mItemInfo.getRelativeItemInfo();

				if (info.mItemInfo instanceof UserFolderInfo) {
					mDockControler.openFolder((ScreenFolderInfo) info.mItemInfo, view,
							mMessageHandler, getLineLayoutContainer());
				} else if (!pressPlussign(view) && info.mItemInfo instanceof ShortCutInfo) {

					Intent intent = (null != appInfo && null != appInfo.mIntent)
							? appInfo.mIntent
							: ((ShortCutInfo) info.mItemInfo).mIntent;
					ArrayList<Rect> posArrayList = new ArrayList<Rect>();
					Rect rect = new Rect();
					view.getGlobalVisibleRect(rect);
					posArrayList.add(rect);
					intent = DockUtil.filterDockBrowserIntent(getContext(),
							info.mItemInfo.mItemType, intent);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.START_ACTIVITY, -1, intent, posArrayList);
					posArrayList.clear();
					posArrayList = null;
				}
			}
		} else if (v instanceof AbsLineLayout) {
			clickBlank(v);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (!mLineLayoutContainer.isTouching()) {
			return false;
		}
		boolean ret = false;
		// 判断是否锁定编辑，是就通知栏发送提示消息
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(mActivity);
			ret = true;
		} else if (v instanceof DockIconView) {
			boolean isLoading = GoLauncher.sendMessage(mMessageHandler, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_ISLOAD_FINISH, -1, null, null);
			if (pressPlussign((DockIconView) v)) {
				return true;
			}
			if (!isLoading) {
				// 屏幕未LOAD完不允许长按
				DockIconView dockIconView = (DockIconView) v;
				dockIconView.setmIsBgShow(false);
				GuideControler guideCloudView = GuideControler.getInstance(mContext);
				guideCloudView.showDockGesture();
				try {
					setDockViewTag(dockIconView);
					mOperationHandler.showQuickActionMenu(dockIconView);
					initFirstDragDockViewData(dockIconView); // 初始化正常区域数据

					// 初始化起始位置和区域大小等
					startDragFram(DragFrame.TYPE_DOCK_DRAG, dockIconView); // 设置图标类型
					ret = true;
				} catch (Throwable e) {
					// 有异常，不执行显示对话框操作
				}

			} else {
				Toast.makeText(mActivity, R.string.loading_screen, Toast.LENGTH_SHORT).show();
			}
		} else if (v instanceof AbsLineLayout) {
			ret = longClickBlank(v);
		}

		if (ret) {
			// 长按震动
			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		}
		return ret;
	}

	/**
	 * version:2.88 dock3.0升级：操作+号、空白显示，弹出指示框，按确定后把+号、空白显示全部删除
	 */
	public boolean pressPlussign(DockIconView view) {
		boolean ret = false;
		if (null == view) {
			return ret;
		}

		DockItemInfo info = view.getInfo();
		if (null == info || null == info.mItemInfo) {
			return ret;
		}

		if (info.mItemInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION
				|| info.mItemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			Intent intent = ((ShortCutInfo) info.mItemInfo).mIntent;
			if (null == intent || ICustomAction.ACTION_BLANK.equals(intent.getAction())) {
				//				mDockControler.checkNeedShowDockAutoFitGuide(mMessageHandler);
				deleteAddIconBeforeVersion30();
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * 清除3.0版本前的残留+号
	 */
	private void deleteAddIconBeforeVersion30() {
		ConcurrentHashMap<Integer, ArrayList<DockItemInfo>> infos = mDockControler
				.getShortCutItems();
		int size = infos.size();
		boolean delete = false;
		for (int i = 0; i < size; i++) {
			ArrayList<DockItemInfo> list = infos.get(i);
			int listsize = list.size();
			for (int j = 0; j < listsize; j++) {
				DockItemInfo itemInfo = list.get(j);
				if (itemInfo.mItemInfo instanceof ShortCutInfo) {
					Intent intent = ((ShortCutInfo) itemInfo.mItemInfo).mIntent;
					if (null == intent || ICustomAction.ACTION_BLANK.equals(intent.getAction())
							|| ICustomAction.ACTION_NONE.equals(intent.getAction())) {
						mDockControler.deleteShortcutItem(itemInfo.mItemInfo.mInScreenId);
						delete = true;
						int newlistsize = list.size();
						if (listsize != newlistsize) {
							listsize = newlistsize;
							j--;
						}
					}
				}
			}
		}
		if (delete) {
			init();
		}
		for (int i = 0; i < size; i++) {
			updateLineLayoutIconsSize(i);
		}
	}

	/**
	 * 获取快捷条数据
	 * 
	 * @return 快捷条列表
	 */
	public ConcurrentHashMap<Integer, ArrayList<DockItemInfo>> getShortCutItems() {
		if (null != mDockControler) {
			return mDockControler.getShortCutItems();
		}
		return null;
	}

	/**
	 * 获取当前Dock显示的总图标 外部负责释放list
	 * 
	 * @return
	 */
	public ArrayList<DockIconView> getCurrentAllDockIcons() {
		ArrayList<DockIconView> listDockIconViews = new ArrayList<DockIconView>();
		int count = mLineLayoutContainer.getChildCount();
		for (int i = 0; i < count; i++) {
			AbsLineLayout lineLayout = (AbsLineLayout) mLineLayoutContainer.getChildAt(i);
			int lineLayoutCount = lineLayout.getChildCount();
			for (int j = 0; j < lineLayoutCount; j++) {
				DockIconView view = (DockIconView) lineLayout.getChildAt(j);
				listDockIconViews.add(view);
			}
		}
		return listDockIconViews;
	}

	@Override
	public void onDataChange(int msg) {
		if (null == getCurretnIcon()) {
			return;
		}
		DockItemInfo info = getCurretnIcon().getInfo();
		mDockControler.gestureDataChange(msg, info);
	}

	@Override
	public void setBlank() {
	}

	@Override
	public Bitmap getAppDefaultIcon() {
		if (null == getCurretnIcon()) {
			return null;
		}

		DockItemInfo appItemInfo = getCurretnIcon().getInfo();
		// 选择更换图标第一个是否显示为程序原生图
		BitmapDrawable drawable = null;
		if (null != appItemInfo) {
			if (appItemInfo.mItemInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION
					|| appItemInfo.mItemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
				drawable = mDockControler.getOriginalIcon((ShortCutInfo) appItemInfo.mItemInfo);
			} else {
				// 其他类型
				drawable = appItemInfo.mItemInfo.getRelativeItemInfo().getIcon();
			}
		}
		if (null != drawable) {
			return drawable.getBitmap();
		} else {
			return null;
		}
	}

	@Override
	public void selectShortCut(boolean clickOrGesture) {
		if (clickOrGesture) {
			GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.DOCK_ENTER_SHORTCUT_SELECT, -1, null, null);
		} else {
			GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.DOCK_ENTER_SHORTCUT_SELECT_FOR_GESTURE, -1, null, null);
		}
	}

	@Override
	public void resetToDefaultIcon() {
		if (getCurretnIcon() != null) {
			if (null != mDockControler) {
				mDockControler.resetDockItemIcon(getCurretnIcon().getInfo());
			}
		}
	}

	public void openMergeFolder() {
		if (null != mMergeFordleIconToOpen && mMergeFordleIconToOpen.getInfo() != null) {
			DockItemInfo info = mMergeFordleIconToOpen.getInfo();
			final BitmapDrawable bDrawable = info.getFolderBackIcon();
			// 得到与bDrawable size相同的bmp
			final Bitmap bitmap = (null != bDrawable)
					? info.prepareOpenFolderIcon(bDrawable)
					: null;
			// 缩放bitmap，符合当前dock显示大小
			final Bitmap icon = (null != bitmap) ? BitmapUtility.createScaledBitmap(bitmap,
					info.getBmpSize(), info.getBmpSize()) : null;
			mMergeFordleIconToOpen.setImageBitmap(icon);
			mMergeFordleIconToOpen.performClick();
			// 解决当从DOCK文件夹拖动图标到DOCK上合并文件夹后进行图标删除，无法获取当前VIEW的问题
			setCurretnIcon(mMergeFordleIconToOpen);
			mMergeFordleIconToOpen = null;

			mDockControler.updateFolderIconAsync(info, false);
		}
	}

	public void cleanHandlerMsg() {
		mOperationHandler.cleanHandlerMsg();
	}

	/***
	 * 插入
	 * 
	 * @param info
	 * @return
	 */
	public boolean insertShortcutItem(DockItemInfo info) {
		boolean ret = false;
		// 索引判断
		int index = info.getmIndexInRow();
		if (0 <= index && index < DockUtil.ICON_COUNT_IN_A_ROW) {
			ret = mDockControler.insertShortcutItem(info);
		}
		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		boolean ret = false;
		switch (msgId) {
			case IDiyMsgIds.DOCK_CHANGE_STYLE_APP :

				Bundle bundle = (Bundle) object;
				actionChangeIcon(bundle);
				break;
			case IDiyMsgIds.DOCK_ADD_APPLICATION_GESTURE : {
				if (getCurretnIcon() != null && (object != null)
						&& (object instanceof ShortCutInfo)) {
					ShortCutInfo info = (ShortCutInfo) object;
					DockIconView view = getCurretnIcon();
					if (null != view.getInfo() && null != view.getInfo().mItemInfo) {
						mDockControler.changeApp(view.getInfo().mItemInfo.mInScreenId, info,
								DockUtil.CHANGE_FROM_GESTURE);
					}
				}
			}
				break;
			case IDiyMsgIds.DOCK_ADD_SHORTCUT_FOR_GESTURE : {
				if (getCurretnIcon() != null && (object != null)
						&& (object instanceof ShortCutInfo)) {
					ShortCutInfo info = (ShortCutInfo) object;
					DockIconView view = getCurretnIcon();
					if (null != view.getInfo() && null != view.getInfo().mItemInfo) {
						mDockControler.changeApp(view.getInfo().mItemInfo.mInScreenId, info,
								DockUtil.CHANGE_FROM_GESTURE);
					}
				}
			}
				break;
			case IDiyMsgIds.DOCK_SETTING_CHANGED_ROW : {
				doWithRowChange();
				updateAllNotifications();
			}
				break;
			case IDiyMsgIds.GET_CURRENT_VIEW_CACHE_BMP : {
				if (object == null || !(object instanceof FolderRelativeLayout) || !sPortrait) {
					return false;
				}

				FolderRelativeLayout folderLayout = (FolderRelativeLayout) object;
				int bmpWidth = mLayoutW;
				int bmpHeight = DockUtil.getBgHeight();

				Bitmap bmp = null;
				try {
					bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
				} catch (Throwable e) {
					// 异常,则不生成bmp,直接返回
					return false;
				}
				if (bmp != null) {
					Canvas cv = new Canvas(bmp);
					if (getSettingInfo().mBgPicSwitch && mBg != null) {
						Rect bounds = mBg.getBounds();
						Rect oldBounds = new Rect(bounds.left, bounds.top, bounds.right,
								bounds.bottom);
						mBg.setBounds(0, 0, bmpWidth, bmpHeight);
						mBg.draw(cv);
						mBg.setBounds(oldBounds);
					}

					int index = mLineLayoutContainer.getCurLine();
					if (0 <= index && index < mLineLayoutContainer.getChildCount()) {
						AbsLineLayout lineLayout = (AbsLineLayout) mLineLayoutContainer
								.getChildAt(index);
						lineLayout.draw(cv);
					}
					Rect rect = new Rect(mLineLayoutContainer.getLeft(),
							mLineLayoutContainer.getTop(), mLineLayoutContainer.getRight(),
							mLineLayoutContainer.getBottom());
					folderLayout.addCacheBmp(bmp, rect);
				}
			}
				break;
			// 删除文件夹里面的子项
			case IDiyMsgIds.DOCK_DELETE_FOLDERITEM : {
				ret = deleteFolderItem(objects, object);
			}
				break;

			case IDiyMsgIds.SCREEN_FOLDER_EVENT : {
				handleFolderEvent(param, object, objects);
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS : {
				if (object instanceof Long) {
					ArrayList<AppItemInfo> items = (ArrayList<AppItemInfo>) objects;
					List<DockIconView> list = getCurrentAllDockIcons();
					int dockItemCnt = list.size();
					for (int i = 0; i < dockItemCnt; i++) {
						DockItemInfo dockItmInfo = list.get(i).getInfo();
						if (null != dockItmInfo && null != dockItmInfo.mItemInfo
								&& dockItmInfo.mItemInfo instanceof UserFolderInfo
								&& (Long) object == dockItmInfo.mItemInfo.mRefId) {
							synchronized (dockItmInfo.mItemInfo) {
								mDockControler.removeFolderItem((UserFolderInfo) dockItmInfo.mItemInfo,
										items);
							}
							deleteFolderOrNot(list.get(i), true);
							mDockControler.updateFolderIconAsync(dockItmInfo, false);
						}
					}
					list.clear();
					list = null;
					items = null;
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.CHECK_FOLDER_NEED_DELETE : {
				if (object != null && object instanceof Long) {
					try {
						long id = (Long) object;
						List<DockIconView> icons = getCurrentAllDockIcons();
						for (DockIconView dockIconView : icons) {
							if (dockIconView.getInfo().mItemInfo.mInScreenId == id) {
								deleteFolderOrNot(dockIconView, true);
							}
						}
						icons.clear();
						icons = null;
					} catch (Exception e) {
					}
				}
			}
				break;

			// dock判断是否在dock响应范围内,执行图标动画
			case IDiyMsgIds.DOCK_CHECK_POSITION : {
				ret = checkPosition(objects, object, param);
				if (!ret) {
					clearMoveToScreenAnim();
				}
			}
				break;
			case IDiyMsgIds.EVENT_FOLDER_CLOSE : {
				if (null != getCurretnIcon() && null != getCurretnIcon().getInfo()
						&& getCurretnIcon().getInfo().mItemInfo instanceof UserFolderInfo) {
					mDockControler.updateFolderIconAsync(getCurretnIcon().getInfo(), false);
				}
			}
				break;
			case IDiyMsgIds.SCREEN_FOLDER_ADDITEMS :
				if (object instanceof Long) {
					if (param == 0) {
						ArrayList<AppItemInfo> items = (ArrayList<AppItemInfo>) objects;
						List<DockIconView> dockViews = getCurrentAllDockIcons();
						int dockItemCnt = dockViews.size();
						for (int i = 0; i < dockItemCnt; i++) {
							DockItemInfo dockItmInfo = dockViews.get(i).getInfo();
							if (dockItmInfo.mItemInfo instanceof UserFolderInfo
									&& (Long) object == dockItmInfo.mItemInfo.mRefId) {
								mDockControler.addItemToFolder(
										(UserFolderInfo) dockItmInfo.mItemInfo, items);
								mDockControler.updateFolderIconAsync(dockItmInfo, false);
							}
						}
						dockViews.clear();
						dockViews = null;
						items = null;
					}
					ret = true;
				}
				break;

			case IDiyMsgIds.SCREEN_FOLDER_RENAME : {
				ret = mDockControler.screenFolderRename(objects, object, getCurrentAllDockIcons());
			}
				break;
			// 删除一个dock图标
			case IDiyMsgIds.DELETE_DOCK_ITEM : {
				if (null != object && object instanceof Long) {
					Long id = (Long) object;
					if (getSettingInfo().mAutoFit) {
						delDockItem(id);
					} else {
						delDockItemUnFit(id);
						modifyShortcutItemIndexUnfit();
					}

				}
				//				mDockControler.checkNeedShowDockAutoFitGuide(mMessageHandler);
			}
				break;

			case IDiyMsgIds.FOLDER_RENAME : {
				ret = mDockControler.folderRename(objects, object);
			}
				break;

			// 图标放手后执行的操作
			case IDiyMsgIds.DRAG_FINISH_BROADCAST : {
				dragFinishBroadcast();
			}
				break;

			case IDiyMsgIds.REFRASH_FOLDER_CONTENT : {
				refreshFolderContent(object, param);
			}
				break;

			case IDiyMsgIds.GET_DOCK_OPEN_FOLDER_ICON_LAYOUTDATA : {
				getOpenFolderIconLayoutData(objects, object);
			}
				break;

			// 结束拖动图标
			case IDiyMsgIds.DRAG_OVER : {
				if (null != object && object instanceof View && null != objects
						&& objects instanceof ArrayList<?>) {
					View dragView = (View) object;
					Rect dragRect = (Rect) objects.get(0);
					ret = dragOver(param, dragView, dragRect);
				}
			}
				break;

			case IDiyMsgIds.DRAG_CANCEL : {
				mDockControler.dragCancle((View) object, getCurretnIcon());
			}
				break;

			case IDiyMsgIds.DOCK_SETTING_NEED_UPDATE : {
				getLineLayoutContainer().setCycle(getSettingInfo().mAutoRevolve); // 设置循环模式
			}
				break;

			case IDiyMsgIds.DOCK_AUTO_FIT_GUIDE_QUITE : {
				deleteAddIconBeforeVersion30();
			}
				break;

			case IDiyMsgIds.DOCK_ASK_OPEN_MERGE_FOLDER : {
				openMergeFolder();
			}
				break;

			case IDiyMsgIds.DOCK_RESET_DEFAULT : {
				resetToDefaultIcon();
			}
				break;
			case IDiyMsgIds.FOLDER_CLOSED : {
				setDrawingCacheEnabled(false);
			}
				break;
			case IDiyMsgIds.DOCK_CURRENT_ICON : {
				if (null != objects && null != getCurretnIcon()) {
					objects.add(getCurretnIcon());
				}
			}
				break;
			case IDiyMsgIds.FOLDER_REPLACE_INDEX : {
				try {
					UserFolderInfo userFolderInfo = (UserFolderInfo) object;
					ArrayList<ItemInfo> infos = (ArrayList<ItemInfo>) objects;

					mDockControler.updateFolderIndex(userFolderInfo.mInScreenId, infos);

					DockItemInfo dockItemInfo = getCurretnIcon().getInfo();
					if (null != dockItemInfo && dockItemInfo.mItemInfo instanceof UserFolderInfo) {
						UserFolderInfo folderInfo = (UserFolderInfo) dockItemInfo.mItemInfo;
						folderInfo.mContentsInit = false;
						if (param == 1) {
							mDockControler.updateDockItem(folderInfo.mInScreenId, dockItemInfo);
						}
						mDockControler.updateFolderIconAsync(dockItemInfo, true);
					}
				} catch (Exception e) {
				}
			}
				break;
			case IDiyMsgIds.UPDATE_FOLDER_ITEM_INFO : {
				if (null != object && object instanceof Long && null != objects) {
					long folderid = (Long) object;
					ArrayList<ShortCutInfo> infos = (ArrayList<ShortCutInfo>) objects;
					int size = infos.size();
					for (int i = 0; i < size; i++) {
						ShortCutInfo info = infos.get(i);
						mDockControler.updateFolderItem(folderid, info);
					}
				}
			}
				break;
			case IDiyMsgIds.REMOVE_FOLDER_CONTENT : {
				if (null != object && object instanceof Long) {
					long id = (Long) object;
					mDockControler.removeDockFolder(id);
				}
			}
				break;
			case IDiyMsgIds.UPDATE_DOCK_BG : {
				updateSlaverBg();
			}
				break;
			case IDiyMsgIds.CLEAR_BG : {
				mBg = null;
			}
				break;
			case IDiyMsgIds.DRAG_AREA_CHANGE : {
				if (object != null && object instanceof int[] && ((int[]) object).length == 2) {
					int[] types = (int[]) object;
					if (types[1] != ScreenDragHandler.DRAG_IN_DOCK) {
						recycleMergeFolderCache();
					}
				}
			}
				break;

			//NOTE:不能加default,有些消息是在子类处理的
			
			//设置红色背景
			case IDiyMsgIds.DOCK_ADD_ICON_RED_BG : {
				if (param == 1) {
					mIsRedBg = true;
				} else {
					mIsRedBg = false;
				}
				invalidate();
			}
				break;

		}
		return ret;
	}

	/***
	 * 发送一个坐标，dock判断是否在dock响应范围内，如果是，是第几个图标
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean checkPosition(List objects, Object object, int param) {
		// dock3.0
		boolean isIn = false;
		if (null != object && object instanceof Point && null != objects
				&& objects instanceof ArrayList<?>) {
			// 判断dock条是否隐藏
			if (ShortCutSettingInfo.sEnable) {
				int dragType = param;
				Point point = (Point) object;
				View dragView = (View) objects.get(0);
				int visibility = getVisibility();
				if (visibility == View.VISIBLE) {
					isIn = viewMoveAnimation(dragType, point, dragView);
				}
			}
		}
		return isIn;
	}

	/***
	 * 获取dock当前打开文件夹图标的layout的位置信息
	 * 
	 * @param objects
	 * @param object
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getOpenFolderIconLayoutData(List objects, Object object) {
		if (null != object && object instanceof Rect && null != getCurretnIcon()) {
			Rect rect = (Rect) object;
			DockIconView view = getCurretnIcon();
			DockLineLayoutContainer container = getLineLayoutContainer();
			rect.left = container.getLeft() + view.getLeft();
			rect.top = container.getTop() + view.getTop();
			rect.right = container.getLeft() + view.getRight();
			rect.bottom = container.getTop() + view.getBottom();
		}
		if (null != objects && objects instanceof ArrayList<?> && null != getCurretnIcon()) {
			DockIconView view = getCurretnIcon();
			Bitmap bmp = BitmapUtility.createBitmap(view, 1.0f);
			if (null != bmp) {
				objects.add(bmp);
			}
		}
	}

	/***
	 * 广播拖动结束，用于通知清除各层缓存
	 */
	private void dragFinishBroadcast() {
		try {
			clearMoveToScreenAnim();
			if (getSettingInfo().mAutoFit) {
				clearAnimationAndresetFlag();
				recycleMergeFolderCache();
				if (null != getCurretnIcon()) {
					getCurretnIcon().setVisibility(View.VISIBLE);
				}
				resetFirstDragDockViewData();
			} else {
				boolean modify = modifyShortcutItemIndexUnfit();
				if (modify) {
					refreshUiChange();
				}
				clearAnimationAndresetFlag();
				recycleMergeFolderCache();
				if (null != getCurretnIcon()) {
					getCurretnIcon().setVisibility(View.VISIBLE);
					int curLine = getLineLayoutContainer().getCurLine();
					AbsLineLayout lineLayout = (AbsLineLayout) getLineLayoutContainer().getChildAt(
							curLine);
					lineLayout.updateLayout();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case DockUtil.HANDLE_REFRESH_DOCK_ITEM_UI :
					refreshUiAdd((DockItemInfo) msg.obj);
					break;
				case DockUtil.HANDLE_ANIMATION_MERGE_FOLDER : { // 合并文件夹
					DockIconView dockIconView = (DockIconView) (msg.obj);
					startMergeFolder(dockIconView, msg.arg1, msg.arg2);
				}
					break;
				case DockUtil.HANDLE_ANIMATION_START_MOVE_TO_SCREEN :
					boolean canMove = prepareViewForMoveToScreen((DockIconView) msg.obj);
					if (canMove) {
						prepareAnimForMoveToScreen();
						if (mLastMoveToScreenView != null) {
							mLastMoveToScreenView.setVisibility(View.INVISIBLE);
						}
					}
					break;
				case DockUtil.HANDLE_ADD_APPLICATION :
					if (msg.obj instanceof AppItemInfo) {
						addApplication(msg.obj);
					} else if (msg.obj instanceof ShortCutInfo) {
						addShortcut(msg.obj);
					}
					break;

				default :
					break;
			}
		}
	};

	/***
	 * 通讯统计消息
	 * 
	 * @param object
	 * @param param
	 */
	public void notificationChanged(Object object, int param) {
		boolean isNeedUpdateDockFolder = false;
		int count = ((Integer) object).intValue();
		if (NotificationType.NOTIFICATIONTYPE_SMS == param) {
			isNeedUpdateDockFolder = true;
			setMissCount(NotificationType.NOTIFICATIONTYPE_SMS,
					ShortCutSettingInfo.mAutoMessageStatistic, count);
		} else if (NotificationType.NOTIFICATIONTYPE_CALL == param) {
			isNeedUpdateDockFolder = true;
			setMissCount(NotificationType.NOTIFICATIONTYPE_CALL,
					ShortCutSettingInfo.mAutoMisscallStatistic, count);
		} else if (NotificationType.NOTIFICATIONTYPE_GMAIL == param) {
			isNeedUpdateDockFolder = true;
			setMissCount(NotificationType.NOTIFICATIONTYPE_GMAIL,
					ShortCutSettingInfo.mAutoMissmailStatistic, count);
		} else if (NotificationType.NOTIFICATIONTYPE_K9MAIL == param) {
			isNeedUpdateDockFolder = true;
			setMissCount(NotificationType.NOTIFICATIONTYPE_K9MAIL,
					ShortCutSettingInfo.mAutoMissk9mailStatistic, count);
		} else if (NotificationType.NOTIFICATIONTYPE_FACEBOOK == param) {
			isNeedUpdateDockFolder = true;
			setMissCount(NotificationType.NOTIFICATIONTYPE_FACEBOOK,
					ShortCutSettingInfo.mAutoMissfacebookStatistic, count);
		} else if (NotificationType.NOTIFICATIONTYPE_SinaWeibo == param) {
			isNeedUpdateDockFolder = true;
			setMissCount(NotificationType.NOTIFICATIONTYPE_SinaWeibo,
					ShortCutSettingInfo.mAutoMissSinaWeiboStatistic, count);
		} else if (NotificationType.NOTIFICATIONTYPE_MORE_APP == param) {
			isNeedUpdateDockFolder = true;
			setMissCount(NotificationType.NOTIFICATIONTYPE_MORE_APP, true, count);
		}
		if (isNeedUpdateDockFolder) {
			// 通讯统计2.0 文件夹支持
			setMissCount(NotificationType.NOTIFICATIONTYPE_DESKFOLDER, true, 0);
			isNeedUpdateDockFolder = false;
		}
	}

	/**
	 * 设置通讯统计
	 * 
	 * @param type
	 *            短信，电话，邮件
	 * @param bool
	 *            开关
	 * @param count
	 *            数值
	 */
	public void setMissCount(int type, boolean bool, int count) {
		ArrayList<DockIconView> list = getCurrentAllDockIcons();
		mDockControler.setMissCount(type, bool, count, list);
		list.clear();
		list = null;
	}

	/**
	 * 更新当前DOCK上全部通讯统计
	 */
	public void updateAllNotifications() {
		NotificationControler controler = AppCore.getInstance().getNotificationControler();
		if (null == controler) {
			return;
		}
		ArrayList<DockIconView> list = getCurrentAllDockIcons();
		mDockControler.updateAllNotifications(controler, list);
	}

	/**
	 * 更新某一行图标的图片size
	 * 
	 * @param rowid
	 */
	public void updateLineLayoutIconsSize(int rowid) {
		ArrayList<DockItemInfo> infos = getShortCutItems().get(rowid);
		int size = infos.size();
		int newsize = (getSettingInfo().mAutoFit) ? DockUtil.getIconSize(size) : DockUtil
				.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
		for (int i = 0; i < size; i++) {
			DockItemInfo info = infos.get(i);
			info.setBmpSize(newsize);
			if (info.mItemInfo instanceof UserFolderInfo) {
				// 因为folder图标刷新是异步，会出现闪一下的情况，所以这里先同步seticon
				info.setIcon(info.getIcon());
			}
			// 重设icon size大小
			mDockControler.updateDockIcon(info);
		}
	}

	/***
	 * 设置信息
	 * 
	 * @return
	 */
	private ShortCutSettingInfo getSettingInfo() {
		return GOLauncherApp.getSettingControler().getShortCutSettingInfo();
	}

	private boolean dragOver(int dragType, View dragView, Rect dragRect) {
		if (null == dragView || null == dragView.getTag() || null == dragRect) {
			return false;
		}
		boolean ret = false;
		cleanHandlerMsg();
		try {
			// 2.88　+号操作升级判断
			AbsLineLayout lineLayout = (AbsLineLayout) getLineLayoutContainer().getChildAt(
					getLineLayoutContainer().getCurLine());
			DockIconView dockIconView = (DockIconView) lineLayout.getChildAt(getmIndexOnMiddle());
			if (pressPlussign(dockIconView)) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		ItemInfo itemInfo = (ItemInfo) dragView.getTag();
		if (itemInfo.mInScreenId == 0 || itemInfo.mInScreenId == -1) {
			// 如果拖入的图标id没赋值，则初始化
			itemInfo.mInScreenId = System.currentTimeMillis();
		}
		boolean mergeFolder = false; // 是否合并文件夹
		boolean intoFolder = false; // 是否放入文件夹
		// 是否合并文件夹
		mergeFolder = isMergeFolder(dragType, dragView);
		if (!mergeFolder) {
			// 是否放入文件夹
			intoFolder = isIntoFolder(dragType, dragView);
		}
		int rowId = getLineLayoutContainer().getCurLine();
		final int toIndexInRow = getDockInAreaIndex();
		AbsLineLayout lineLayout = (AbsLineLayout) getLineLayoutContainer().getChildAt(rowId);

		ret = dragOverHandle(dragType, mergeFolder, intoFolder, toIndexInRow, lineLayout, rowId,
				dragView);

		recycleMergeFolderCache();
		return ret;
	}

	/***
	 * 要求删除文件夹内Item
	 * 
	 * @param objects
	 * @param object
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean deleteFolderItem(List objects, Object object) {
		DockIconView curView = getCurretnIcon();
		if (null == curView) {
			return false;
		}
		View view = (View) object;
		ItemInfo itemInfo = (ItemInfo) view.getTag();
		DockItemInfo dockItemInfo = curView.getInfo();
		if (null != dockItemInfo && dockItemInfo.mItemInfo instanceof UserFolderInfo) {
			UserFolderInfo folderInfo = (UserFolderInfo) dockItemInfo.mItemInfo;
			folderInfo.remove(itemInfo.mInScreenId);
			itemInfo.unRegisterObserver(dockItemInfo);
			Long deleteid = itemInfo.mInScreenId;
			if (null != objects && !objects.isEmpty()) {
				deleteid = (Long) objects.get(0);
			}
			mDockControler.removeDockFolderItem(deleteid, folderInfo.mInScreenId);
			// 删除打开folder的指定内容　
			GoLauncher.sendMessage(mMessageHandler, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
					IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER, -1, itemInfo, null);
			deleteFolderOrNot(curView, true);

			try {
				// 删除一项后，刷新文件夹图标
				mDockControler.updateFolderIconAsync(dockItemInfo, false);
			} catch (Exception e) {
				// 出现异常，则不刷新图标
			}
		}
		return true;
	}

	/**
	 * 判断是否删除文件夹
	 * 
	 * @param userFolderInfo
	 * @param deleteOne
	 *            剩一个图标时是否删除文件夹
	 * @return
	 */
	private boolean deleteFolderOrNot(DockIconView dockIconView, boolean deleteOne) {
		if (null == dockIconView || null == dockIconView.getInfo()
				|| null == dockIconView.getInfo().mItemInfo
				|| !(dockIconView.getInfo().mItemInfo instanceof UserFolderInfo)) {
			return false;
		}

		UserFolderInfo userFolderInfo = (UserFolderInfo) dockIconView.getInfo().mItemInfo;
		boolean ret = false;
		int count = 0;
		synchronized (userFolderInfo) {
			/**
			 * ADT-3723 非必现：桌面文件夹消失，有消失动画 修改方法：对userFolderInfo加锁
			 */
			count = userFolderInfo.getChildCount();
		}
		if (count == 0) {
			// delete folder
			DockIconView mDeleteDockFolderView = dockIconView;
			// mHandler.sendEmptyMessage(DockConstant.HANDLE_ANIMATION_DELETE_FOLDER);
			if (mDeleteDockFolderView != null) {
				Long id = mDeleteDockFolderView.getInfo().mItemInfo.mInScreenId;
				handleMessage(this, -1, IDiyMsgIds.DELETE_DOCK_ITEM, -1, id, null);
				mDeleteDockFolderView = null;
			}

			ret = true;
		} else if (deleteOne && count == 1) {
			// delete folder & move item to desktop
			ShortCutInfo shortCutInfo = userFolderInfo.getChildInfo(0);

			mDockControler.changeApp(userFolderInfo.mInScreenId, shortCutInfo,
					DockUtil.CHANGE_FROM_DELETEFOLER);
			mDockControler.removeDockFolder(userFolderInfo.mInScreenId);
			Message msg = new Message();
			msg.what = DockUtil.HANDLE_ANIMATION_ADD_ITEM_FROM_FOLDER;
			msg.obj = dockIconView;
			mHandler.sendMessage(msg);
			ret = true;
		} else {
			ret = false;
		}

		return ret;
	}

	/***
	 * 刷新文件夹内容
	 */
	public void refreshFolderContent(Object object, int param) {
		try {
			if (null != object && object instanceof Long) {
				Long id = (Long) object;
				ConcurrentHashMap<Integer, ArrayList<DockItemInfo>> infosHashMap = getShortCutItems();
				int sizeHashMap = infosHashMap.size();
				for (int i = 0; i < sizeHashMap; i++) {
					ArrayList<DockItemInfo> list = infosHashMap.get(i);
					int sizeList = list.size();
					for (int j = 0; j < sizeList; j++) {
						DockItemInfo dockItemInfo = list.get(j);
						if (id == dockItemInfo.mItemInfo.mInScreenId
								&& dockItemInfo.mItemInfo instanceof UserFolderInfo) {
							// 判断个数是否为0个。0个就删除图标，否则刷新图标
							if (param == 0) {
								handleMessage(this, -1, IDiyMsgIds.DELETE_DOCK_ITEM, -1, id, null);
							} else {
								UserFolderInfo userFolderInfo = (UserFolderInfo) dockItemInfo.mItemInfo;
								userFolderInfo.mContentsInit = false;
								mDockControler.updateFolderIconAsync(dockItemInfo, true);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 强制刷新文件内容、图标
	 * 
	 * @param info
	 */
	public void reloadFolderContent() {
		mDockControler.reloadFolderContent();
	}

	public void delDockItem(Long id) {
		DockIconView deleteView = null;
		if (mDockControler.deleteShortcutItem(id)) {
			int curRow = getLineLayoutContainer().getCurLine();
			AbsLineLayout lineLayout = (AbsLineLayout) getLineLayoutContainer().getChildAt(curRow);
			int oldcount = (null != lineLayout)
					? lineLayout.getChildCount()
					: DockUtil.ICON_COUNT_IN_A_ROW;
			// 更新UI
			deleteView = refreshUiDel(id);
			if (null != deleteView) {
				// 释放内存
				deleteView.clearSelf();
				mOperationHandler.hideQuickActionMenu(false);
				// 判断图片size是否改变
				if (oldcount == DockUtil.ICON_COUNT_IN_A_ROW) {
					updateLineLayoutIconsSize(curRow);
				}
			}
		};
		// UI同步数据库校验
		verifyData();
	}

	public void delDockItemUnFit(Long id) {
		DockIconView deleteView = null;
		if (mDockControler.deleteShortcutItem(id)) {
			deleteView = refreshUiDel(id);

			if (null != deleteView) {
				// 释放内存
				deleteView.clearSelf();
				mOperationHandler.hideQuickActionMenu(false);
			}
		}
	}

	/**
	 * 是否合并文件夹
	 * 
	 * @param dragtype
	 * @param dragView
	 * @return
	 */
	protected boolean isMergeFolder(int dragtype, View dragView) {
		boolean ret = false;

		// 如果当前屏绘制状态是DRAW_STATUS_MERGE_FOLDER，就判定为合并文件夹
		if (getmDrawStauts() == AbsDockView.DRAW_STATUS_MERGE_FOLDER) {
			// 合并文件夹操作
			DockIconView mergeView_target = getmMergeFolderTargetView();
			DockItemInfo itemInfo_target = (mergeView_target != null)
					? mergeView_target.getInfo()
					: null;

			View mergeView_drag = dragView;
			ItemInfo itemInfo_drag = (mergeView_drag != null)
					? (ItemInfo) mergeView_drag.getTag()
					: null;
			if (null != mergeView_target && null != itemInfo_target
					&& null != itemInfo_target.mItemInfo
					&& itemInfo_target.mItemInfo instanceof ShortCutInfo && null != mergeView_drag
					&& null != itemInfo_drag && itemInfo_drag instanceof ShortCutInfo) {
				// 添加文件夹
				ItemInfo oldInfo = itemInfo_target.mItemInfo;
				UserFolderInfo folderInfo = new UserFolderInfo();
				folderInfo.mTitle = getContext().getText(R.string.folder_name); // 文件夹名称
				folderInfo.mInScreenId = System.currentTimeMillis(); // 文件夹ID
				itemInfo_target.setInfo(folderInfo);
				mDockControler.updateDockItem(oldInfo.mInScreenId, itemInfo_target);
				if (oldInfo.mInScreenId == 0 || oldInfo.mInScreenId == -1) {
					oldInfo.mInScreenId = System.currentTimeMillis() + 1;
				}
				mDockControler.addItemToFolder(oldInfo, folderInfo);
				mDockControler.addItemToFolder(itemInfo_drag, folderInfo);

				mMergeFordleIconToOpen = mergeView_target;
				// 更新图标updateIcon(itemInfo_target)放在收到DOCK_ASK_OPEN_MERGE_FOLDER消息里处理
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * 是否插入文件夹
	 * 
	 * @param dragType
	 * @param dragView
	 * @return
	 */
	public boolean isIntoFolder(int dragType, View dragView) {
		boolean ret = false;

		try {
			DockIconView mergeViewTarget = getmOpenFolderView();
			DockItemInfo itemInfoTarget = mergeViewTarget.getInfo();
			View mergeViewDrag = dragView;
			ItemInfo itemInfoDrag = (ItemInfo) mergeViewDrag.getTag();
			boolean intoSelf = dragType == DragFrame.TYPE_DOCK_FOLDERITEM_DRAG
					&& null != getCurretnIcon() && itemInfoTarget == getCurretnIcon().getInfo();
			if (null != itemInfoTarget && null != itemInfoTarget.mItemInfo
					&& itemInfoTarget.mItemInfo instanceof UserFolderInfo && null != itemInfoDrag
					&& itemInfoDrag instanceof ShortCutInfo && !intoSelf) {
				UserFolderInfo folderInfo = (UserFolderInfo) itemInfoTarget.mItemInfo;
				// 图标去重
				mDockControler.removeDockFolderItems(folderInfo, (ShortCutInfo) itemInfoDrag);
				mDockControler.addItemToFolder(itemInfoDrag, folderInfo);
				mDockControler.updateFolderIconAsync(itemInfoTarget, false);
				invalidate(); // 插入文件夹后需要刷新界面，解决挤压后重影的问题
				ret = true;
			}
		} catch (Exception e) {
		}

		return ret;
	}

	/***
	 * 文件夹操作事件
	 * 
	 * @param msgId
	 * @param object
	 * @param objList
	 */
	@SuppressWarnings("rawtypes")
	private void handleFolderEvent(int msgId, Object object, List objList) {
		switch (msgId) {
			case IScreenFolder.START_DRAG : {
				startDragFram(DragFrame.TYPE_DOCK_FOLDERITEM_DRAG, (View) object);
			}
				break;
			case IScreenFolder.START_ACTIVITY :
				if (object instanceof Intent) {
					// 点击live folder的item
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.START_ACTIVITY, -1, object, null);
				} else if (object instanceof View) {
					launchApp((View) object);
				}
				break;
		}
	}

	/***
	 * 启动程序
	 * 
	 * @param view
	 * @return
	 */
	private boolean launchApp(View view) {
		boolean ret = false;
		Object tag = view.getTag();
		if (tag != null && tag instanceof ShortCutInfo) {
			ShortCutInfo shortcut = (ShortCutInfo) tag;
			Rect rect = new Rect();
			view.getGlobalVisibleRect(rect);

			ArrayList<Rect> posArrayList = new ArrayList<Rect>();
			posArrayList.add(rect);

			Intent intent = DockUtil.filterDockBrowserIntent(getContext(), shortcut.mItemType,
					shortcut.mIntent);
			ret = GoLauncher.sendMessage(mMessageHandler, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.START_ACTIVITY, -1, intent, posArrayList);
			posArrayList.clear();
			posArrayList = null;
		}

		return ret;
	}

	private void actionChangeIcon(Bundle bundle) {
		DockIconView view = getCurretnIcon();
		if (null == view || null == view.getInfo() || null == view.getInfo().mItemInfo) {
			return;
		}

		FeatureItemInfo featureItemInfo = view.getInfo().mItemInfo;

		// 数据库
		int imagetype = bundle.getInt(ImagePreviewResultType.TYPE_STRING);
		String packageStr = bundle.getString(ImagePreviewResultType.IMAGE_PACKAGE_NAME);
		String path = bundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
		mDockControler.updateShortCutItemIcon(featureItemInfo.mInScreenId, imagetype, 0,
				packageStr, path);

		// view修改
		Drawable icon = featureItemInfo.getFeatureIcon();
		if (null == icon || !(icon instanceof BitmapDrawable)) {
			return;
		}
		if (featureItemInfo instanceof ShortCutInfo) {
			ShortCutInfo shortCutInfo = (ShortCutInfo) featureItemInfo;
			shortCutInfo.setIcon(icon, true);
			view.getInfo().setIcon((BitmapDrawable) icon);
		} else if (featureItemInfo instanceof UserFolderInfo) {
			mDockControler.updateFolderIconAsync(view.getInfo(), false);
		}
	}

	public void doWithSettingChange() {
		if (mLineLayoutContainer != null) {
			mLineLayoutContainer.setCycle(getSettingInfo().mAutoRevolve); // 设置循环模式
		}
	}

	/**BEGIN 挤压移动到桌面动画逻辑*/

	/**
	 * 触发动画
	 */
	private void isStartViewMoveToScreenAnim(DockIconView view) {
		if (view != null && view != mLastMoveToScreenView && !(mDragView instanceof DockIconView)) {
			int curLine = getLineLayoutContainer().getCurLine();
			AbsLineLayout lineLayout = (AbsLineLayout) getLineLayoutContainer().getChildAt(curLine);
			if (lineLayout.getChildCount() == DockUtil.ICON_COUNT_IN_A_ROW) {
				if (mLastMoveToScreenView != null) {
					mLastMoveToScreenView.setVisibility(View.VISIBLE);
				}
				mHandler.removeMessages(DockUtil.HANDLE_ANIMATION_START_MOVE_TO_SCREEN);
				Message msg = new Message();
				msg.what = DockUtil.HANDLE_ANIMATION_START_MOVE_TO_SCREEN;
				msg.obj = view;
				mHandler.sendMessageDelayed(msg, 100);
				mLastMoveToScreenView = view;
			}
		}
	}

	/**
	 * <br>功能简述:动画view创建
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param dockIconView
	 */
	private boolean prepareViewForMoveToScreen(DockIconView dockIconView) {
		if (mMoveToScreenView == null) {
			//1:创建view
			mMoveToScreenView = new ImageView(mActivity);
			addView(mMoveToScreenView);
		}

		//2:view参数设置
		mMoveToScreenView.setImageDrawable(dockIconView.getDrawable());
		mMoveToScreenView.setPadding(dockIconView.getPaddingLeft(), dockIconView.getPaddingTop(),
				dockIconView.getPaddingRight(), dockIconView.getPaddingBottom());

		int l = mLineLayoutContainer.getLeft() + dockIconView.getLeft();
		int t = mLineLayoutContainer.getTop() + dockIconView.getTop();
		int r = mLineLayoutContainer.getLeft() + dockIconView.getRight();
		int b = mLineLayoutContainer.getTop() + dockIconView.getBottom();
		mMoveToScreenView.layout(l, t, r, b);
		mMoveToScreenView.setTag(new Rect(l, t, r, b));

		// 拿桌面放置位置
		return getScreenLocation();
	}

	private boolean getScreenLocation() {
		int[] center = new int[2];
		ArrayList<int[]> list = new ArrayList<int[]>();
		list.add(center);
		boolean isVacant = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.GET_DROP_DOCK_LOCATION, -1, mDragView, list);
		if (isVacant && list.size() > 1) {
			mScreenCell = list.get(1);
			mScreenRect = new Rect();
			mScreenRect.left = center[0] - mMoveToScreenView.getWidth() / 2;
			mScreenRect.right = mScreenRect.left + mMoveToScreenView.getWidth();
			mScreenRect.top = center[1] - mMoveToScreenView.getHeight() / 2;
			mScreenRect.bottom = mScreenRect.top + mMoveToScreenView.getHeight();

			return true;
		}
		return false;
	}

	/**
	 * <br>功能简述:设置view的动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void prepareAnimForMoveToScreen() {
		//1:先清原来动画缓存
		CompatibleUtil.unwrap(mMoveToScreenView);

		//2:位移动画
		setTransactionAnim();

		//3:抖动动画
		setShakeAnim();
	}

	private void setTransactionAnim() {
		if (mTransactionAnimator == null) {
			//设置位移动画
			mTransactionAnimator = ValueAnimator.ofFloat(0f, 1f);
			mTransactionAnimator.setDuration(DockUtil.MOVE_TO_SCREEN_TRANSACTION_DURATION);
			mTransactionAnimator.setStartDelay(0);
			float screenIconSize = Utilities.getStandardIconSize(mActivity);
			float dockIconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
			final float scale = screenIconSize / dockIconSize;
			mTransactionAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float r = ((Float) animation.getAnimatedValue()).floatValue();
					float s = r * scale + (1 - r);
					CompatibleUtil.setScaleX(mMoveToScreenView, s);
					CompatibleUtil.setScaleY(mMoveToScreenView, s);
					Rect oldRect = (Rect) mMoveToScreenView.getTag();
					int x = (int) ((1 - r) * oldRect.left + r * mScreenRect.left);
					int y = (int) ((1 - r) * oldRect.top + r * mScreenRect.top);
					mMoveToScreenView.layout(x, y, x + mMoveToScreenView.getWidth(), y
							+ mMoveToScreenView.getHeight());
				}
			});
		} else {
			mTransactionAnimator.cancel();
		}

		mTransactionAnimator.start();
	}

	private void setShakeAnim() {
		if (mShakeAnimator == null) {
			mShakeAnimator = ValueAnimator.ofFloat(0f, 1f);
			mShakeAnimator.setRepeatMode(ValueAnimator.REVERSE);
			mShakeAnimator.setRepeatCount(ValueAnimator.INFINITE);
			mShakeAnimator.setDuration(DockUtil.MOVE_TO_SCREEN_SHAKE_DURATION);
			mShakeAnimator.setStartDelay(DockUtil.MOVE_TO_SCREEN_TRANSACTION_DURATION);

			float screenIconSize = Utilities.getStandardIconSize(mActivity);
			float dockIconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
			final float scale = screenIconSize / dockIconSize;
			mShakeAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float r = ((Float) animation.getAnimatedValue()).floatValue();
					float y = r * (-10); //位移-10
					CompatibleUtil.setTranslationY(mMoveToScreenView, y);
					float s = scale * (r * 1.05f + (1 - r)); //从1.0到1.05变化
					CompatibleUtil.setScaleX(mMoveToScreenView, s);
					CompatibleUtil.setScaleY(mMoveToScreenView, s);
				}
			});
		} else {
			mShakeAnimator.cancel();
		}

		mShakeAnimator.start();
	}

	/**
	 * <br>功能简述:dock图标挤压到桌面，数据写入
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	protected final boolean saveMoveToScreenData() {
		if (mLastMoveToScreenView != null && mDragView != null && mTransactionAnimator != null
				&& mShakeAnimator != null
				&& (mTransactionAnimator.isStarted() || mShakeAnimator.isStarted())) {
			// 添加到屏幕层
			ItemInfo moveItemInfo = mLastMoveToScreenView.getInfo().mItemInfo;
			moveItemInfo.mCellX = mScreenCell[0];
			moveItemInfo.mCellY = mScreenCell[1];
			boolean isAddSuccess = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.MOVE_DOCK_ITEM_TO_SCREEN, -1, moveItemInfo, null);

			if (isAddSuccess) {
				FeatureItemInfo tag = (FeatureItemInfo) mDragView.getTag();

				DockItemInfo dockItemInfo = mLastMoveToScreenView.getInfo();
				long oldId = dockItemInfo.mItemInfo.mInScreenId;

				dockItemInfo.setInfo(tag);
				if (tag instanceof UserFolderInfo) {
					final BitmapDrawable bDrawable = dockItemInfo.getFolderBackIcon();
					final Bitmap bitmap = dockItemInfo.prepareOpenFolderIcon(bDrawable);
					final BitmapDrawable icon = new BitmapDrawable(mContext.getResources(), bitmap);
					dockItemInfo.setIcon(icon);
				} else {
					mDockControler.updateDockIcon(dockItemInfo);
				}
				mDockControler.updateDockItem(oldId, dockItemInfo);
				return true;
			}
		}
		return false;
	}

	protected DockItemInfo createDockItemInfo(AppItemInfo appItemInfo, int rowid, int indexinrow,
			int iconsize) {
		if (appItemInfo == null || rowid < 0 || rowid >= DockUtil.TOTAL_ROWS || indexinrow < 0
				|| indexinrow >= DockUtil.ICON_COUNT_IN_A_ROW) {
			return null;
		}

		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_APPLICATION, iconsize);
		dockItemInfo.setmRowId(rowid);
		dockItemInfo.setmIndexInRow(indexinrow);

		ShortCutInfo shortCutInfo_application = new ShortCutInfo();
		shortCutInfo_application.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		shortCutInfo_application.mIntent = appItemInfo.mIntent;
		shortCutInfo_application.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
		shortCutInfo_application.mInScreenId = System.currentTimeMillis();
		mDockControler.prepareItemInfo(shortCutInfo_application);

		dockItemInfo.setInfo(shortCutInfo_application);

		return dockItemInfo;
	}

	protected DockItemInfo createDockItemInfo(ShortCutInfo info, int rowid, int indexinrow,
			int iconsize) {
		if (null == info || null == info.mIntent || rowid < 0 || rowid >= DockUtil.TOTAL_ROWS
				|| indexinrow < 0 || indexinrow >= DockUtil.ICON_COUNT_IN_A_ROW) {
			// M9,返回的intent为null
			return null;
		}

		String title = null;
		Intent intent = info.mIntent;
		if (null != info.mTitle) {
			title = info.mTitle.toString();
		}
		BitmapDrawable icon = null;
		if (null != info.mIcon && info.mIcon instanceof BitmapDrawable) {
			icon = (BitmapDrawable) info.mIcon;
		}

		DockItemInfo dockItemInfo = null;
		SysShortCutControler sysShortCutControler = AppCore.getInstance().getSysShortCutControler();
		if (sysShortCutControler != null) {
			// 新增
			sysShortCutControler.addSysShortCut(intent, title, icon);

			dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT, iconsize);
			dockItemInfo.setmRowId(rowid);
			dockItemInfo.setmIndexInRow(indexinrow);
			info.mInScreenId = System.currentTimeMillis();
			mDockControler.prepareItemInfo(info);
			dockItemInfo.setInfo(info);
		}

		return dockItemInfo;
	}

	@Override
	public void setAppFunIcon() {

	}
	
	protected ArrayList<DockIconView> getRowDockIcons(int rowid) {
		if (rowid >= 0 && rowid < mIconViewsHashMap.size()) {
			return mIconViewsHashMap.get(rowid);
		} else {
			return null;
		}
	}

	/**
	 * <br>功能简述:清除挤压移动到桌面动画相关的缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void clearMoveToScreenAnim() {
		mHandler.removeMessages(DockUtil.HANDLE_ANIMATION_START_MOVE_TO_SCREEN);
		CompatibleUtil.unwrap(mMoveToScreenView);
		removeView(mMoveToScreenView);
		if (mLastMoveToScreenView != null) {
			mLastMoveToScreenView.setVisibility(View.VISIBLE);
			mLastMoveToScreenView = null;
		}
		if (mTransactionAnimator != null) {
			mTransactionAnimator.cancel();
			mTransactionAnimator = null;
		}
		if (mShakeAnimator != null) {
			mShakeAnimator.cancel();
			mShakeAnimator = null;
		}
		mMoveToScreenView = null;
		mScreenRect = null;
		mScreenCell = null;
	}
	/**END 挤压移动到桌面动画逻辑*/
	
	
	/**
	 * <br>功能简述:设置背景红色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas
	 */
	public void setRedBg(Canvas canvas) {
		if (mIsRedBg) {
			if (sPortrait) {
				canvas.clipRect(0, mLayoutH - mDockBgHeight, mLayoutW, mLayoutH);
			} else {
				int left = (mLayoutW - mLayoutH) / 2;
				int right = (mLayoutW + mLayoutH) / 2;
				int top = (mLayoutH / 2) - mDockBgHeight;
				int bottom = mLayoutH / 2;
				canvas.clipRect(left, top, right, bottom);
			}
			canvas.drawColor(Color.parseColor("#4cff0000"));
		}
	}
	
	// 切换横竖屏
	public void configurationChange() {
		
	}; 

	public abstract AbsLineLayout getLineLayout();

	public abstract DockIconView refreshUiDel(Long id);

	public abstract boolean refreshUiAdd(DockItemInfo info);

	public abstract void clearAnimationAndresetFlag();

	public abstract float getZoomProportion();

	public abstract int getDockInAreaIndex();

	public abstract void initFirstDragDockViewData(DockIconView dockIconView);

	protected abstract ArrayList<DockIconView> initIcon(int row);

	public abstract boolean setDockDragViewNewInAreaIndex();

	public abstract boolean viewMoveAnimation(int dragType, Point point, View dragView2);

	public abstract int getDockIconSize(int iconNum);

	protected abstract void clickBlank(View v);

	protected abstract boolean longClickBlank(View v);

	protected abstract void addApplication(Object object);

	protected abstract void addShortcut(Object object);

	public abstract boolean dragOverHandle(int dragType, boolean mergeFolder, boolean intoFolder,
			int toIndexInRow, AbsLineLayout lineLayout, int rowId, View dragView);

	public void resetFirstDragDockViewData() {
	};

	public void refreshUiChange() {
	};

}
