package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * 桌面文件夹的显示层布局
 * 
 * @author jiangxuwen
 * 
 */
public class FolderRelativeLayout extends RelativeLayout {
	private Drawable mBgDrawable; // 背景
	private boolean mIsFirstLayout = true; // 是否为第一次布局
	// 点击的点的坐标
	private Point mLeftPoint;
	private Point mRightPoint;

	private boolean mIsDefaultTheme = false; // 是否默认主题
	private IFolderAnimationListner mFolderAnimationListner; // 打开、关闭动画监听者

	// 文件夹3.0打开关闭动画
	public static final int DRAW_STATUS_NORMAL = 1; // 正常绘制
	public static final int DRAW_STATUS_FOLDER_OPEN = 2; // 打开文件夹动画绘制
	public static final int DRAW_STATUS_FOLDER_CLOSE = 3; // 关闭文件夹动画绘制
	private int mStatus = DRAW_STATUS_FOLDER_OPEN;

	private float mOpenFolderAnimStartTime;
	private float mCloseFolderAnimStartTime;
	private static int sFolderOpenAnimationDuration = 225;
	private boolean mCanHandlerOpenAnimTimeout = false; // 用来解决打开动画最后一帧会卡一下

	// 排版参数
	private int mFolderTop; // 排版可见区域顶
	private int mFolderBottom; // 排版可见区域底
	private int mClipLine; // 分割线

	public static final int ARROW_DIRECTION_UP = 1; // 箭头向上,桌面文件夹
	public static final int ARROW_DIRECTION_DOWN = 2; // 箭头向下,dock文件夹
	private int mArrowDirection; // 箭头方向

	// 高亮图标
	private Bitmap mFolderIconBmp; // 文件夹图标
	private Rect mFolderIconRect; // 高亮图标区域

	// 主界面的展示
	private Bitmap mBg; // 背景图片
	private Rect mFolderRect; // folder主界面的展示rect,用于canvas.clipRect

	// 桌面壁纸
	private Drawable mWallpaper; // 壁纸
	private int[] mWallpaperOffset; // 壁纸偏移

	// 桌面背景视图截图
	private ArrayList<CacheBmp> mCacheBmps; // 截图列表
	private Paint mScreenShotPaint; // 截图画笔

	/**
	 * 背景绘制的一张图片bitmap的信息
	 */
	private class CacheBmp {
		private Bitmap mBmp; // 截图
		private Rect mRect; // 位置

		private CacheBmp(Bitmap bmp, Rect rect) {
			mBmp = bmp;
			mRect = rect;
		}
	}

	/**
	 * @param mWallpaperOffset
	 *            the mWallpaperOffset to set
	 */
	public void setmWallpaperOffset(int[] wallpaperOffset) {
		mWallpaperOffset = wallpaperOffset;
	}

	/**
	 * @param mWallpaper
	 *            the mWallpaper to set
	 */
	public void setmWallpaper(Drawable wallpaper) {
		mWallpaper = wallpaper;
	}

	/**
	 * @param mFolderAnimationListner
	 *            the mFolderAnimationListner to set
	 */
	public void setmFolderAnimationListner(IFolderAnimationListner mFolderAnimationListner) {
		this.mFolderAnimationListner = mFolderAnimationListner;
	}

	public FolderRelativeLayout(Context context) {
		this(context, null);
	}

	public FolderRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FolderRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		String theme = GOLauncherApp.getThemeManager().getCurThemePackage();
		// 3.0主题暂时当默认主题处理，modify by yangbing 2012-05-08
		if (ThemeManager.isAsDefaultThemeToDo(theme)) {
			mIsDefaultTheme = true;
		}
		mCacheBmps = new ArrayList<CacheBmp>();
		mFolderRect = new Rect();
		mScreenShotPaint = new Paint();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int count = getChildCount();
		if (count < 1 || !mIsFirstLayout) {
			return;
		}
		if (null != mBg) {
			Canvas cv = new Canvas(mBg);
			cv.save();
			cv.translate(0, -mFolderTop);
			super.dispatchDraw(cv);
			cv.restore();
		}
		mIsFirstLayout = false;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		int saveid = canvas.save();
		if (null != mWallpaper && null != mWallpaperOffset) {
			// 桌面壁纸
			canvas.translate(-mWallpaperOffset[0], -mWallpaperOffset[1]);
			mWallpaper.draw(canvas);
			canvas.restoreToCount(saveid);
		}
		float time = 1.0f;
		int translateUp = 0;
		int translateDown = 0;
		switch (mStatus) {
			case DRAW_STATUS_FOLDER_OPEN :
				// 动态变化程度计算
				int currentTimeOpen = 0;
				if (mOpenFolderAnimStartTime == 0) {
					// 打开第一帧，当开始了25ms,才有第一帧的打开动画
					mOpenFolderAnimStartTime = SystemClock.uptimeMillis() - 25;
					currentTimeOpen = 25;
				} else {
					currentTimeOpen = (int) (SystemClock.uptimeMillis() - mOpenFolderAnimStartTime);
				}
				time = currentTimeOpen >= sFolderOpenAnimationDuration ? 1 : currentTimeOpen
						/ (float) sFolderOpenAnimationDuration;

				translateUp = (int) ((mFolderTop - mClipLine) * time);
				translateDown = (int) ((getHeight() - getPaddingBottom() - mClipLine) * time);
				mScreenShotPaint.setAlpha(255 - (int) (205 * time));

				int topOpen = mClipLine + translateUp;
				int bottomOpen = mClipLine + translateDown;
				mFolderRect.set(0, topOpen, getWidth(), bottomOpen);
				break;

			case DRAW_STATUS_FOLDER_CLOSE :
				// 动态变化程度计算
				int currentTimeClose = (int) (SystemClock.uptimeMillis() - mCloseFolderAnimStartTime);
				time = currentTimeClose >= sFolderOpenAnimationDuration ? 1 : currentTimeClose
						/ (float) sFolderOpenAnimationDuration;

				translateUp = (int) ((mFolderTop - mClipLine) * (1 - time));
				translateDown = (int) ((getHeight() - getPaddingBottom() - mClipLine) * (1 - time));
				mScreenShotPaint.setAlpha(50 + (int) (205 * time));

				int topClose = mClipLine + translateUp;
				int bottomClose = mClipLine + translateDown;
				mFolderRect.set(0, topClose, getWidth(), bottomClose);
				break;

			case DRAW_STATUS_NORMAL :
				translateUp = mFolderTop - mClipLine;
				translateDown = getHeight() - getPaddingBottom() - mClipLine;
				mScreenShotPaint.setAlpha(50);
				break;

			default :
				break;
		}
		// 画上提
		drawScreenshot(canvas, translateUp, 0, 0, getWidth(), mClipLine);
		// 画下移
		drawScreenshot(canvas, translateDown, 0, mClipLine, getWidth(), getBottom());
		// 画主展示界面
		if (mStatus != DRAW_STATUS_NORMAL && null != mBg) {
			canvas.clipRect(mFolderRect);
			canvas.drawBitmap(mBg, 0, mFolderTop, null);
		} else {
			canvas.clipRect(mFolderRect);
			drawSelf(canvas);
		}

		canvas.restoreToCount(saveid);

		if (time >= 1) {
			if (mStatus == DRAW_STATUS_FOLDER_CLOSE) {
				handlerAnimTimeout();
			} else if (mStatus == DRAW_STATUS_FOLDER_OPEN) {
				if (mCanHandlerOpenAnimTimeout) {
					handlerAnimTimeout();
				} else {
					invalidate();
					mCanHandlerOpenAnimTimeout = true;
				}
			}
		} else {
			invalidate();
		}
	}

	private void drawScreenshot(Canvas canvas, int translate, int clipLeft, int clipTop,
			int clipRight, int clipBottom) {
		int saveid = canvas.save();

		canvas.translate(0, translate);
		canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
		int size = mCacheBmps.size();
		for (int i = 0; i < size; i++) {
			CacheBmp cacheBmp = mCacheBmps.get(i);
			int top = cacheBmp.mRect.top;
			int bottom = cacheBmp.mRect.bottom;
			if (((clipTop <= top && top <= clipBottom) || (clipTop <= bottom && bottom <= clipBottom))
					&& cacheBmp.mBmp != null) {
				canvas.drawBitmap(cacheBmp.mBmp, cacheBmp.mRect.left, cacheBmp.mRect.top,
						mScreenShotPaint);
			}
		}

		// 高亮图标
		if (null != mFolderIconBmp && clipTop <= mFolderIconRect.top
				&& mFolderIconRect.bottom <= clipBottom) {
			canvas.drawBitmap(mFolderIconBmp, mFolderIconRect.left, mFolderIconRect.top, null);
		}

		canvas.restoreToCount(saveid);
	}

	private void drawSelf(Canvas canvas) {
		// 正常绘制
		if (null != mBg) {
			canvas.drawBitmap(mBg, 0, mFolderTop, null);
		} else {
			canvas.drawColor(0xff282828);
		}
		// 画图标及按钮
		super.dispatchDraw(canvas);
	}

	private void handlerAnimTimeout() {
		switch (mStatus) {
			case DRAW_STATUS_FOLDER_OPEN :
				mStatus = DRAW_STATUS_NORMAL;
				mFolderAnimationListner.onOpened();
				break;

			case DRAW_STATUS_FOLDER_CLOSE :
				// 关闭文件夹,一定异步发送此消息，同步的话会出现空指针异常
				GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DESK_USER_FOLDER_FRAME, null,
						null);
				break;

			default :
				break;
		}
		if (null != mBg) {
			mBg.recycle();
			mBg = null;
			mBg = generateBg();

			// 释放无用资源
			mBgDrawable = null;
		}
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (null == mLeftPoint || null == mRightPoint) {
			return true;
		}
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		if (x >= mLeftPoint.x && x <= mRightPoint.x && y >= mLeftPoint.y && y <= mRightPoint.y) {
			return false;
		}
		return true;
	}

	public void setmLeftPoint(Point mLeftPoint) {
		this.mLeftPoint = mLeftPoint;
	}

	public void setmRightPoint(Point mRightPoint) {
		this.mRightPoint = mRightPoint;
	}

	public void setmBgDrawable(Drawable mBgDrawable) {
		this.mBgDrawable = mBgDrawable;
	}

	/**
	 * @param mStatus
	 *            the mStatus to set
	 */
	public void setmStatus(int status) {
		this.mStatus = status;
		switch (status) {
			case DRAW_STATUS_FOLDER_CLOSE :
				if (null != mBg) {
					Canvas cv = new Canvas(mBg);
					cv.save();
					cv.translate(0, -mFolderTop);
					super.dispatchDraw(cv);
					cv.restore();
				}
				mCloseFolderAnimStartTime = SystemClock.uptimeMillis();
				break;

			default :
				break;
		}

		postInvalidate();
	}

	/**
	 * @return the mStatus
	 */
	public int getmStatus() {
		return mStatus;
	}

	/**
	 * 开始打开文件夹动画
	 * 
	 * @param folderTop
	 *            　文件夹layout上边界
	 * @param folderIconBottom
	 *            　文件夹图标下边界
	 * @param point
	 *            箭头点
	 */
	public void startOpenAnimation(Bitmap bmp, Rect rect) {
		mFolderIconBmp = bmp;

		int folderheight = getMeasuredHeight();
		int folderBottomMargin = getContext().getResources().getDimensionPixelSize(
				R.dimen.folder_margin_botton);

		mClipLine = (mArrowDirection == FolderRelativeLayout.ARROW_DIRECTION_UP)
				? rect.bottom
				: rect.top/*
							* - mTriangleH
							*/;
		if (mClipLine < 0) {
			mClipLine = 0;
		}
		mFolderTop = mClipLine;
		mFolderBottom = mFolderTop + folderheight;
		// 如果超出底边界，则上移
		int screenheight = GoLauncher.getDisplayHeight();
		if (mFolderBottom > screenheight - folderBottomMargin) {
			//edit by zzf 
			mFolderBottom = screenheight - DockUtil.getBgHeight();
			//end
			mFolderTop = mFolderBottom - folderheight;
			if (mClipLine > mFolderBottom) {
				mClipLine = mFolderBottom;
			}
		}

		mFolderIconRect = rect;
		mBg = generateBg();
	}

	public void setmFolderIconBmp(Bitmap bmp) {
		mFolderIconBmp = bmp;
	}

	public void setmFolderIconRect(Rect rect) {
		mFolderIconRect = rect;
	}

	/**
	 * @param mArrowDirection
	 *            the mArrowDirection to set
	 */
	public void setmArrowDirection(int arrowDirection) {
		this.mArrowDirection = arrowDirection;
	}

	/**
	 * @return the mFolderTop
	 */
	public int getmFolderTop() {
		return mFolderTop;
	}

	public int getmFolderBottom() {
		return mFolderBottom;
	}

	public int getmFolderClipLine() {
		return mClipLine;
	}

	private Bitmap generateBg() {
		Bitmap bg = null;
		if (mBgDrawable != null) {
			final int width = GoLauncher.getScreenWidth();
			final int height = mFolderBottom - mFolderTop;
			try {
				bg = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			} catch (OutOfMemoryError e) {
				// NOTE:OOM，没有背景图片
				return null;
			}
			Canvas canvas = new Canvas(bg);
			// 给文件夹背景画底图
			ImageUtil.drawImage(canvas, mBgDrawable, 1, 0, 0, width, height, null);
			if (mIsDefaultTheme) {
				Drawable grankDrawable = getContext().getResources().getDrawable(
						R.drawable.folder_bg_grank);
				ImageUtil.drawImage(canvas, grankDrawable, 0, 0, 1, width, height - 1, new Paint());
			}
		}

		return bg;
	}

	public void clear() {
		if (null != mFolderIconBmp) {
			mFolderIconBmp.recycle();
			mFolderIconBmp = null;
		}
		removeAllCacheBmps();
		setDrawingCacheEnabled(false);
	}

	/**
	 * 打开动画完毕，返回高亮图标的rect
	 * 
	 * @return
	 */
	public Rect getmFolderIconRectOnOpened() {
		Rect rect = new Rect(mFolderIconRect);

		int top = mFolderTop;
		int bottom = getHeight() - getPaddingBottom();

		if (mFolderIconRect.top < mClipLine) {
			int reduce = mClipLine - top;
			rect.top -= reduce;
			rect.bottom -= reduce;
		} else {
			int add = bottom - mClipLine;
			rect.top += add;
			rect.bottom += add;
		}

		return rect;
	}

	/**
	 * 添加一张背景底图
	 * 
	 * @param bmp
	 * @param rect
	 */
	public void addCacheBmp(Bitmap bmp, Rect rect) {
		CacheBmp cacheBmp = new CacheBmp(bmp, rect);
		mCacheBmps.add(cacheBmp);
	}

	/**
	 * 回收全部屏幕截图
	 */
	public void removeAllCacheBmps() {
		if (mCacheBmps != null) {
			while (!mCacheBmps.isEmpty()) {
				CacheBmp cacheBmp = mCacheBmps.remove(0);
				Bitmap bmp = cacheBmp.mBmp;
				if (bmp != null && !bmp.isRecycled()) {
					bmp.recycle();
				}
			}
		}
	}
}
