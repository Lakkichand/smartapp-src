package com.jiubang.ggheart.apps.desks.diy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Debug;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.go.util.debug.DebugState;
import com.go.util.log.Duration;
import com.go.util.log.FpsCounter;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DeskUserFolderFrame;

/**
 * Diy桌面自定义framelayout
 * 
 * @author yuankai
 * @version 1.0
 */
public class DiyFrameLayout extends FrameLayout {
	public final static String DRAW_COST = "Draw cost:";
	public final static String DRAW_MS = "ms";
	public final static String KB = "KB";
	public final static String TOTAL = "Total:";
	public final static String UESD = "Used:";
	public final static String FREE = "Free:";

	public final static boolean DEBUG = DebugState.isDrawCost(); // 打印绘制时间，内存使用
	public final static boolean FPS = false; // 打印绘制帧率（通过统计单位时间内实际绘制次数）
	public final static boolean TEXT_SHADOWED = false; // 打印文字时绘制阴影
	public final static int TEXT_COLOR = Color.WHITE;
	public final static int SHADOW_COLOR = Color.BLACK;

	Paint mPaint;
	private StringBuilder mBuilder;
	private float mLastMotionX;
	private float mLastMotionY;

	private FpsCounter mFpsCounter = FPS ? new FpsCounter(1) : null;

	public DiyFrameLayout(Context context) {
		this(context, null);
	}

	@SuppressWarnings("unused")
	public DiyFrameLayout(Context context, AttributeSet attribute) {
		super(context, attribute);
		if (DEBUG || FPS) {
			mPaint = new Paint();
			mBuilder = new StringBuilder();
			mPaint.setTextSize(20);
			mPaint.setColor(Color.RED);
			if (TEXT_SHADOWED) {
				mPaint.setColor(TEXT_COLOR);
				mPaint.setAntiAlias(true);
			}
		}
	}

	/**
	 * @return 最近一次down事件的x坐标
	 */
	public float getLastMotionX() {
		return mLastMotionX;
	}

	/**
	 * @return 最近一次down事件的y坐标
	 */
	public float getLastMotionY() {
		return mLastMotionY;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			mLastMotionX = ev.getX();
			mLastMotionY = ev.getY();
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (DEBUG) {
			Duration.setStart(DiyFrameLayout.class.getName());
		}

		final AbstractFrame topFrame = GoLauncher.getTopFrame();
		// 如果topframe不透明，仅绘制topframe
		if (topFrame != null && topFrame.isOpaque()) {
			// 全屏且不透明
			drawFrame(topFrame, canvas);
		} else if (topFrame != null && topFrame instanceof DragFrame) {
			DeskUserFolderFrame folderFrame = (DeskUserFolderFrame) GoLauncher
					.getFrame(IDiyFrameIds.DESK_USER_FOLDER_FRAME);
			if (folderFrame != null && folderFrame.getVisibility() == View.VISIBLE) {
				drawFrame(folderFrame, canvas);
				drawFrame(topFrame, canvas);
			} else {
				super.dispatchDraw(canvas);
			}
		} else {
			super.dispatchDraw(canvas);
		}

		int yOffset = 20;
		if (DEBUG) {
			// 这段代码其实耗时蛮多的，会使实际帧率下降很多，所以提供了单独打印帧率的选项
			Duration.setEnd(DiyFrameLayout.class.getName());
			mBuilder.delete(0, mBuilder.length());
			mBuilder.append(DRAW_COST);
			mBuilder.append(Duration.getDuration(DiyFrameLayout.class.getName()));
			mBuilder.append(DRAW_MS);
			drawText(canvas, mBuilder.toString(), 0, yOffset, mPaint);

			final long nativeMax = Debug.getNativeHeapSize() >> 10;
			final long nativeAllocated = Debug.getNativeHeapAllocatedSize() >> 10;
			final long nativFree = Debug.getNativeHeapFreeSize() >> 10;
			mBuilder.delete(0, mBuilder.length());
			mBuilder.append(TOTAL);
			mBuilder.append(nativeMax);
			mBuilder.append(KB);
			drawText(canvas, mBuilder.toString(), 0, yOffset + 30, mPaint);

			mBuilder.delete(0, mBuilder.length());
			mBuilder.append(UESD);
			mBuilder.append(nativeAllocated);
			mBuilder.append(KB);
			drawText(canvas, mBuilder.toString(), 0, yOffset + 60, mPaint);

			mBuilder.delete(0, mBuilder.length());
			mBuilder.append(FREE);
			mBuilder.append(nativFree);
			mBuilder.append(KB);
			drawText(canvas, mBuilder.toString(), 0, yOffset + 90, mPaint);
			yOffset += 120;
		}
		if (FPS) {
			mFpsCounter.computeFps(SystemClock.uptimeMillis());
			drawText(canvas, "" + mFpsCounter.GetFps(), 0, yOffset, mPaint);

		}
	}

	/**
	 * 绘制文字，根据{@link #TEXT_SHADOWED}的取值选择是否绘制边缘阴影
	 */
	public static void drawText(Canvas canvas, String text, float x, float y, Paint paint) {
		if (TEXT_SHADOWED) {
			paint.setColor(SHADOW_COLOR);
			canvas.drawText(text, x - 1, y, paint);
			canvas.drawText(text, x, y + 1, paint);
			canvas.drawText(text, x + 1, y, paint);
			canvas.drawText(text, x, y - 1, paint);
			paint.setColor(TEXT_COLOR);
			canvas.drawText(text, x, y, paint);
		} else {
			canvas.drawText(text, x, y, paint);
		}
	}

	private void drawFrame(AbstractFrame frame, Canvas canvas) {
		View child = frame.getContentView();
		if (child != null) {
			drawChild(canvas, child, getDrawingTime());
		}
	}
}
