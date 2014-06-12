package com.go.util.graphics;

import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.Xfermode;

/**
 * 用于绘制淡出图片/颜色的工具类
 * 
 * @author dengweiming
 * 
 */
public class FadePainter {
	public final static int DIR_FROM_LEFT = 0;
	public final static int DIR_FROM_TOP = 1;
	public final static int DIR_FROM_RIGHT = 2;
	public final static int DIR_FROM_BOTTOM = 3;

	private final static Xfermode XFERMODE_COMPOSE = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

	private static void computeFadeMatrix(Rect rect, int dir, Matrix matrix) {
		switch (dir) {
			case DIR_FROM_LEFT :
				matrix.setScale(1, rect.width());
				matrix.postRotate(-90);
				matrix.postTranslate(rect.left, rect.top);
				break;
			case DIR_FROM_TOP :
				matrix.setScale(1, rect.height());
				matrix.postTranslate(rect.left, rect.top);
				break;
			case DIR_FROM_RIGHT :
				matrix.setScale(1, rect.width());
				matrix.postRotate(90);
				matrix.postTranslate(rect.right, rect.top);
				break;
			case DIR_FROM_BOTTOM :
				matrix.setScale(1, rect.height());
				matrix.postRotate(180);
				matrix.postTranslate(rect.left, rect.bottom);
				break;
		}
	}

	Matrix mMatrix = new Matrix();
	Paint mPaint = new Paint();
	LinearGradient mGradient = new LinearGradient(0, 0, 0, 1, 0xFF000000, 0, TileMode.CLAMP);
	LinearGradient mColorfulGradient = mGradient;
	ComposeShader mComposeShader;

	BitmapShader mBitmapShaderRef;
	int mColor = 0xFF000000;

	/**
	 * 根据指定的区域和渐变方向，逐渐变淡地绘制位图
	 * 
	 * @param canvas
	 *            画布
	 * @param rect
	 *            画布的绘制区域
	 * @param dir
	 *            变淡的方向，取值为{@link #DIR_FROM_TOP}, {@link #DIR_FROM_BOTTOM},
	 *            {@link #DIR_FROM_LEFT}, {@link #DIR_FROM_RIGHT}
	 * @param bitmapShader
	 *            位图着色器，如果要对位图作变换，请使用BitmapShader.setLocalMatrix方法
	 */
	public void drawFadeBitmap(Canvas canvas, Rect rect, int dir, BitmapShader bitmapShader) {
		if (mBitmapShaderRef != bitmapShader) {
			mBitmapShaderRef = bitmapShader;
			mComposeShader = new ComposeShader(mBitmapShaderRef, mGradient, XFERMODE_COMPOSE);
		}
		mPaint.setShader(mComposeShader);
		computeFadeMatrix(rect, dir, mMatrix);
		mGradient.setLocalMatrix(mMatrix);
		canvas.drawRect(rect, mPaint);
	}

	/**
	 * 根据指定的区域和渐变方向，逐渐变淡地绘制颜色
	 * 
	 * @param canvas
	 *            画布
	 * @param rect
	 *            画布的绘制区域
	 * @param dir
	 *            变淡的方向，取值为{@link #DIR_FROM_TOP}, {@link #DIR_FROM_BOTTOM},
	 *            {@link #DIR_FROM_LEFT}, {@link #DIR_FROM_RIGHT}
	 * @param color
	 *            指定的颜色
	 */
	public void drawFadeColor(Canvas canvas, Rect rect, int dir, int color) {
		if (mColor != color) {
			mColor = color;
			mColorfulGradient = mColor == 0xFF000000 ? mGradient : new LinearGradient(0, 0, 0, 1,
					mColor, mColor & 0x00FFFFFF, TileMode.CLAMP);
		}
		computeFadeMatrix(rect, dir, mMatrix);
		mColorfulGradient.setLocalMatrix(mMatrix);
		mPaint.setShader(mColorfulGradient);
		mPaint.setXfermode(null);
		canvas.drawRect(rect, mPaint);
	}

	public void recycle() {
		mBitmapShaderRef = null;
		mComposeShader = null;
	}
}
