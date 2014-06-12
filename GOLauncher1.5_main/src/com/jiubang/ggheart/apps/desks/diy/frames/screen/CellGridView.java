package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import android.view.View;
import android.widget.GridView;

import com.jiubang.ggheart.apps.desks.diy.DrawSelectedListener;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderGridView.FolderTargetPosition;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderGridView.FolderdragItem;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.data.info.ItemInfo;

/**
 * 桌面文件夹的图片单元格区
 * 
 * @author jiangxuwen
 * 
 */
public class CellGridView extends GridView implements DrawSelectedListener {
	// 绘图缓冲的标识
	boolean mChildrenDrawingCacheBuilt;
	boolean mChildrenDrawnWithCache;

	/**
	 * 绘制状态
	 */
	private int mDrawStatus = DRAW_NORMAL;
	protected static final int DRAW_NORMAL = 1; // 正常状态
	protected static final int DRAW_REPLACE = 2; // 换位状态

	// 换图标位置数据
	private int mIndex = -1;
	private IFolderCellGridViewListner mListner;
	private long mInNewCellTime = 0; // 进入新格子的时间
	private long mStartReplaceTime = 0;// 开始换位动画的时间
	private static final int REPLACE_START_TIME = 300;// 进入新网格后，持续这个时间，就开始换位置动画
	private static final int REPLACE_LAST_TIME = 300;// 换位置动画持续时间
	private static final int DROP_ITEM_ANIMATION_TIME = 300; // 放手后放下动画持续时间
	// private Bitmap mDropBmp; //放下动画的bmp.
	private long mStartDropBmpTime; // 放下动画开始时间
	private Paint mPaint; // 放下动画使用画笔
	private Point mCenterPoint;// 每次保存移动值

	public CellGridView(Context context) {
		super(context);
		mCenterPoint = new Point();

		mPaint = new Paint();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mChildrenDrawingCacheBuilt = false;
	}

	@Override
	public void setChildrenDrawnWithCacheEnabled(boolean enabled) {
		mChildrenDrawnWithCache = enabled;
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
				view.setDrawingCacheEnabled(true);
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
		}
	}

	/**
	 * 设置绘制状态
	 * 
	 * @param status
	 *            {@link #DRAW_NORMAL},{@link #DRAW_REPLACE}
	 */
	public void setStatus(int status) {
		mDrawStatus = status;
	}

	public int getStatus() {
		return mDrawStatus;
	}

	public void checkReplacePosition(Point point) {
		if (mDrawStatus == DRAW_REPLACE) {
			return;
		}

		mCenterPoint.set(point.x, point.y);
		int[] location = new int[2];
		getLocationOnScreen(location);// 这里得出的坐标是与statusbar无关的，即总是全屏坐标

		int statusbarHeight = GoLauncher.isFullScreen() ? 0 : StatusBarHandler.getStatusbarHeight();
		int x = point.x - location[0];
		int y = point.y - location[1] + statusbarHeight; // 因为传过来的point.y是计算了statusbarHeight的

		if (x < 0 || y < 0 || x > getRight() || y > getBottom()) {
			// 响应区域外
			return;
		}

		if (null == mListner || 0 == mListner.getmItemViewWidth()
				|| 0 == mListner.getmItemViewHeight()) {
			// 判断条件不符合
			return;
		}

		int target_cellx = x / mListner.getmItemViewWidth();
		int target_celly = y / mListner.getmItemViewHeight();

		FolderTargetPosition targetPosition = mListner.getTargetPosition();
		int oldIndex = targetPosition.mTargetIndex;
		int oldScreen = targetPosition.mTargetScreen;
		targetPosition.mTargetScreen = mIndex;
		targetPosition.mTargetIndex = target_celly * mListner.getNumColumns() + target_cellx;
		if (targetPosition.mTargetIndex >= getCount() - 1) {
			targetPosition.mTargetIndex = getCount() - 1;
		}
		long currentTime = SystemClock.uptimeMillis();
		if (oldScreen != targetPosition.mTargetScreen || targetPosition.mTargetIndex != oldIndex) {
			mInNewCellTime = currentTime;
		}
		if (mInNewCellTime != 0 && currentTime - mInNewCellTime > REPLACE_START_TIME) {
			startReplaceAnimation(currentTime);
		}
	}

	/**
	 * @param mCenterPoint
	 *            the mCenterPoint to set
	 */
	public void setmCenterPoint(int x, int y) {
		if (null == mCenterPoint) {
			mCenterPoint = new Point();
		}
		mCenterPoint.set(x, y);
	}

	private void setStatusNormal() {
		mDrawStatus = DRAW_NORMAL;
		mInNewCellTime = 0;
	}

	public void startReplaceAnimation(long time) {
		mDrawStatus = DRAW_REPLACE;
		mStartReplaceTime = time;

		mListner.startReplace();

		// 解决换了屏后换位出现invalidate()但不调用cellGridView的dispatchDraw()问题
		destroyDrawingCache();
		invalidate();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		drawSelectedBorder(canvas);
		switch (mDrawStatus) {
			case DRAW_NORMAL : {
				draw_Normal(canvas);
			}
				break;

			case DRAW_REPLACE : {
				draw_Replace(canvas);
			}
				break;

			default :
				super.dispatchDraw(canvas);
				break;
		}
		try {
			drawDropBmp(canvas);
		} catch (Exception e) {
			// 有异常就不绘制放下动画
		}
	}

	private void draw_Normal(Canvas canvas) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View childView = getChildAt(i);
			Object tagObject = childView.getTag();
			if (null != tagObject && tagObject instanceof ItemInfo && null != mListner
					&& null != mListner.getDragItemData().mDragInfo
					&& mListner.getDragItemData().mDragInfo == tagObject
					&& (mStartDropBmpTime != 0 || mListner.ismIsLongClick())) {
				continue;
			} else {
				drawChild(canvas, childView, 0);
			}
		}
	}

	private void draw_Replace(Canvas canvas) {
		FolderdragItem folderdragItem = mListner.getDragItemData();
		FolderTargetPosition targetPosition = mListner.getTargetPosition();

		int currentTime = (int) (SystemClock.uptimeMillis() - mStartReplaceTime);
		final float t = currentTime >= REPLACE_LAST_TIME ? 1 : currentTime
				/ (float) REPLACE_LAST_TIME;

		if (folderdragItem.mDragScreen < targetPosition.mTargetScreen) {
			draw_Replace_forword(canvas, targetPosition.mTargetIndex, t);
		} else if (folderdragItem.mDragScreen > targetPosition.mTargetScreen) {
			draw_Replace_backword(canvas, targetPosition.mTargetIndex, t);
		} else {
			draw_Replace_self(canvas, folderdragItem.mDragIndex, targetPosition.mTargetIndex, t);
		}

		if (t >= 1) {
			handleReplaceTimeout();
		}
		postInvalidate();
	}

	/**
	 * 从前面页拖到当前屏，动画集体前移
	 * 
	 * @param canvas
	 */
	private void draw_Replace_forword(Canvas canvas, int targetindex, float t) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View chilView = getChildAt(i);
			int save_count = canvas.save();

			if (i == 0) {
				final float translate_x = ScreenUtils.easeOut(0, 0 - chilView.getRight(), t);
				canvas.translate(translate_x, 0);
				drawChild(canvas, chilView, 0);
			} else if (i <= targetindex) {
				// 集体前移动画
				final float translate_x = ScreenUtils.easeOut(0, getChildAt(i - 1).getLeft()
						- chilView.getLeft(), t);
				final float translate_y = ScreenUtils.easeOut(0, getChildAt(i - 1).getTop()
						- chilView.getTop(), t);
				canvas.translate(translate_x, translate_y);
				drawChild(canvas, chilView, 0);

			} else {
				drawChild(canvas, chilView, 0);
			}
			canvas.restoreToCount(save_count);
		}
	}

	/**
	 * 从后面页拖到当前屏，动画集体后移
	 * 
	 * @param canvas
	 */
	private void draw_Replace_backword(Canvas canvas, int targetindex, float t) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View chilView = getChildAt(i);
			int save_count = canvas.save();

			if (i == count - 1) {
				final float translate_x = ScreenUtils
						.easeOut(0, getWidth() - chilView.getLeft(), t);
				canvas.translate(translate_x, 0);
				drawChild(canvas, chilView, 0);
			} else if (i >= targetindex) {
				// 集体后退动画
				try {
					final float translate_x = ScreenUtils.easeOut(0, getChildAt(i + 1).getLeft()
							- chilView.getLeft(), t);
					final float translate_y = ScreenUtils.easeOut(0, getChildAt(i + 1).getTop()
							- chilView.getTop(), t);
					canvas.translate(translate_x, translate_y);
				} catch (Exception e) {
				}
				drawChild(canvas, chilView, 0);
			} else {
				drawChild(canvas, chilView, 0);
			}
			canvas.restoreToCount(save_count);
		}
	}

	/**
	 * 换位只影响当前屏
	 * 
	 * @param canvas
	 */
	private void draw_Replace_self(Canvas canvas, int startindex, int targetindex, float t) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View childView = getChildAt(i);
			Object tagObject = childView.getTag();
			int save_count = canvas.save();

			if (0 <= startindex && startindex < i && i <= targetindex && targetindex < getCount()) {
				// 集体前移动画
				final float translate_x = ScreenUtils.easeOut(0, getChildAt(i - 1).getLeft()
						- childView.getLeft(), t);
				final float translate_y = ScreenUtils.easeOut(0, getChildAt(i - 1).getTop()
						- childView.getTop(), t);
				canvas.translate(translate_x, translate_y);
				drawChild(canvas, childView, 0);
			} else if (0 <= targetindex && targetindex <= i && i < startindex
					&& startindex < getCount()) {
				// 集体后退动画
				final float translate_x = ScreenUtils.easeOut(0, getChildAt(i + 1).getLeft()
						- childView.getLeft(), t);
				final float translate_y = ScreenUtils.easeOut(0, getChildAt(i + 1).getTop()
						- childView.getTop(), t);
				canvas.translate(translate_x, translate_y);
				drawChild(canvas, childView, 0);
			} else if (null != tagObject && tagObject instanceof ItemInfo
					&& null != mListner.getDragItemData().mDragInfo
					&& mListner.getDragItemData().mDragInfo == tagObject) {
				continue;
			} else {
				drawChild(canvas, childView, 0);
			}
			canvas.restoreToCount(save_count);
		}
	}

	/**
	 * 绘制长按图标放手后，图标drop定位的动画
	 * 
	 * @param canvas
	 */
	private void drawDropBmp(Canvas canvas) {
		if (mStartDropBmpTime != 0 && null != mListner.getDragItemData()
				&& null != mListner.getDragItemData().mDragBmp
				&& !mListner.getDragItemData().mDragBmp.isRecycled()) {
			int currentTime = (int) (SystemClock.uptimeMillis() - mStartDropBmpTime);
			final float t = currentTime >= DROP_ITEM_ANIMATION_TIME ? 1 : currentTime
					/ (float) DROP_ITEM_ANIMATION_TIME;

			View targetView = getChildAt(mListner.getTargetPosition().mTargetIndex);
			final int targetview_left = targetView.getLeft();
			final int targetview_top = targetView.getTop();

			int location[] = new int[2];
			targetView.getLocationOnScreen(location);

			if (!GoLauncher.isFullScreen()) {
				// TODO:开始位置
			}

			int distance_x = 0;
			int distance_y = 0;
			if (mCenterPoint.x != 0 && mCenterPoint.y != 0) {
				distance_x = location[0] + targetView.getWidth() / 2 - mCenterPoint.x;
				distance_y = location[1] + targetView.getHeight() / 2 - mCenterPoint.y;
			}

			int begin_x = (targetview_left - distance_x >= 0) ? targetview_left - distance_x : 0;
			int begin_y = (targetview_top - distance_y >= 0) ? targetview_top - distance_y : 0;
			final int draw_x = (int) ScreenUtils.easeOut(begin_x, targetview_left, t);
			final int draw_y = (int) ScreenUtils.easeOut(begin_y, targetview_top, t);

			canvas.drawBitmap(mListner.getDragItemData().mDragBmp, draw_x, draw_y, mPaint);

			if (SystemClock.uptimeMillis() - mStartDropBmpTime > DROP_ITEM_ANIMATION_TIME) {
				mStartDropBmpTime = 0;
			}
			invalidate();
		}
	}

	public void startDropAnimation() {
		mStartDropBmpTime = SystemClock.uptimeMillis();
	}

	private void handleReplaceTimeout() {
		mDrawStatus = DRAW_NORMAL;
		mInNewCellTime = 0;

		mListner.onReplaceFinish();
	}

	/**
	 * @param mListner
	 *            the mListner to set
	 */
	public void setmListner(IFolderCellGridViewListner mListner) {
		this.mListner = mListner;
	}

	/**
	 * @param mIndex
	 *            the mIndex to set
	 */
	public void setmIndex(int mIndex) {
		this.mIndex = mIndex;
	}

	public void handleUpEventAfterDrag() {
		if (mListner.ismIsLongClick() && !mListner.isOutOfBound()) {
			FolderdragItem folderdragItem = mListner.getDragItemData();
			FolderTargetPosition targetPosition = mListner.getTargetPosition();
			if (mDrawStatus == CellGridView.DRAW_NORMAL
					&& (folderdragItem.mDragScreen != targetPosition.mTargetScreen || folderdragItem.mDragIndex != targetPosition.mTargetIndex)) {
				// 迅速拖动放手
				startReplaceAnimation(SystemClock.uptimeMillis());
			}
			if (mListner.isScrollFinished()) {
				// 放手后drop动画
				startDropAnimation();
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
}
