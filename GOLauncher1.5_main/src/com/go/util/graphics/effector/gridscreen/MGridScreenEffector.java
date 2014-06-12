package com.go.util.graphics.effector.gridscreen;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

import com.go.util.scroller.ScreenScroller;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;

abstract class MGridScreenEffector {
	protected static PaintFlagsDrawFilter DRAW_FILTER_LOW_QUALITY = null;
	protected static PaintFlagsDrawFilter DRAW_FILTER_MID_QUALITY = new PaintFlagsDrawFilter(0,
			Paint.ANTI_ALIAS_FLAG);
	protected static PaintFlagsDrawFilter DRAW_FILTER_HIGH_QUALITY = new PaintFlagsDrawFilter(0,
			Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

	protected static float interpolate(float start, float end, float t) {
		return (end - start) * t + start;
	}

	protected GridScreenContainer mContainer;
	protected ScreenScroller mScroller;
	protected float mWidth;
	protected float mHeight;
	protected float mCenterX;
	protected float mCenterY;
	protected int mQuality;
	protected boolean mCombineBackground;

	public void onAttach(GridScreenContainer container, ScreenScroller scroller) {
		mContainer = container;
		mScroller = scroller;
		onSizeChanged(mScroller.getScreenWidth(), mScroller.getScreenHeight());
	}

	public void onDetach() {
		mContainer = null;
		mScroller = null;
	}

	public void onSizeChanged(int w, int h) {
		mWidth = w;
		mHeight = h;
		mCenterX = w * 0.5f;
		mCenterY = h * 0.5f;
	}

	// 是否后画当前屏
	protected boolean isCurrentScreenOnTop() {
		return false;
	}

	abstract void onDrawScreen(Canvas canvas, int screen, int offset);

	void drawScreen(Canvas canvas, int screen, int offset, int topPadding, int scroll) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		canvas.save();
		canvas.translate(offset + scroll, topPadding);
		onDrawScreen(canvas, screen, offset);
		canvas.restore();
	}

	public boolean isCombineBackground() {
		return mCombineBackground;
	}

	void setDrawQuality(int quality) {
		mQuality = quality;
	}

	/**
	 * 
	 * @param canvas
	 *            假设当前canvas已经是低质量的，并且调用者在用完canvas之后要负责还原质量
	 * @param quality
	 */
	final protected void requestQuality(Canvas canvas, int quality) {
		quality = Math.min(quality, mQuality);
		switch (quality) {
			case GridScreenEffector.DRAW_QUALITY_MID :
				canvas.setDrawFilter(DRAW_FILTER_MID_QUALITY);
				break;
			case GridScreenEffector.DRAW_QUALITY_HIGH :
				canvas.setDrawFilter(DRAW_FILTER_HIGH_QUALITY);
				break;
		}
	}

	static void drawScreen(GridScreenContainer container, Canvas canvas, int screen, int offset,
			int topPadding, int scroll) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		canvas.save();
		canvas.translate(scroll + offset, topPadding);
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		int lastX = 0, lastY = 0;
		for (int i = 0, cellY = paddingTop; i < row && index < end; ++i) {
			for (int j = 0, cellX = paddingLeft; j < col && index < end; ++j, ++index) {
				canvas.translate(cellX - lastX, cellY - lastY);
				container.drawGridCell(canvas, index);
				lastX = cellX;
				lastY = cellY;
				cellX += cellWidth;
			}
			cellY += cellHeight;
		}
		canvas.restore();
	}

	/**
	 * 在桌面屏幕编辑的小屏幕状态下，画白色半透明的背景
	 * 
	 * @param container
	 * @param canvas
	 * @param screen
	 * @param offset
	 * @param topPadding
	 * @param scroll
	 */
	static void drawScreenBackground(GridScreenContainer container, Canvas canvas, int screen,
			int offset, int topPadding, int scroll) {
		if (screen == ScreenScroller.INVALID_SCREEN || !(container instanceof Workspace)) {
			return;
		}
		final Workspace workspace = (Workspace) container;
		canvas.save();
		canvas.translate(scroll + offset, topPadding);
		workspace.drawCellBackground(canvas, screen);
		canvas.restore();
	}
}
