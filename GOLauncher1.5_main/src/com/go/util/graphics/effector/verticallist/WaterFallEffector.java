package com.go.util.graphics.effector.verticallist;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;

import com.go.util.scroller.Scroller;
import com.go.util.scroller.ScrollerEffector;
import com.go.util.scroller.ScrollerListener;

public class WaterFallEffector implements ScrollerEffector {
	VerticalListContainer mContainer;
	Scroller mScroller;
	int mOrientation;
	int mWidth;
	int mHeight;
	int mTotalHeight;

	static final float CameraZ = 576;
	Matrix mMatrix1 = new Matrix();
	Matrix mMatrix2 = new Matrix();
	Rect mSrcRect = new Rect();
	Rect mDstRect = new Rect();

	float mProjectY1;
	float mProjectY2;

	int mUpperBottom; // 上半部分的下边界
	int mUpperTop; // 上半部分的上边界
	int mMidHeight; // 中间部分的高度
	int mLowerHeight; // 下半部分的高度
	int mUpperFarClipPlaneZ;
	int mLowerFarClipPlaneZ;
	boolean mExternal; // 上半部分和下半部分是否在视图之外

	float mShadowBlurRadius = 8;

	Bitmap mBackground;
	int mBackgroundWidth;
	int mBackgroundHeight;
	int mBackgroundOffsetX;
	int mBackgroundOffsetY;

	/**
	 * 单屏的行数
	 */
	int mRowNums = 4;
	/**
	 * 单元格高度
	 */
	int mCellHeight = 0;
	/**
	 * 是否竖向循环
	 */
	boolean mVerticalCycleMode = false;

	Paint mPaint = new Paint();
	int mShadowColor = 0x40000000;
	LinearGradient mShader1 = new LinearGradient(0, 0, 0, 1, 0xFF000000, mShadowColor,
			TileMode.CLAMP);
	LinearGradient mShader2 = new LinearGradient(0, 0, 0, 1, mShadowColor, 0, TileMode.CLAMP);
	Matrix mShaderMatrix = new Matrix();

	public WaterFallEffector() {
	}

	@Override
	public boolean onDraw(Canvas canvas) {
		if (mExternal) {
			canvas.translate(0, mUpperTop);
		}
		int extendDistance = 0;
		if (mVerticalCycleMode) {
			extendDistance = (int) (mRowNums + 2.5) * mCellHeight;
		}
		// draw upper part
		final int upperHeight = mUpperBottom - mUpperTop;
		final int scroll = mScroller.getScroll();
		if (scroll > -mUpperBottom - extendDistance || mBackground != null) {
			canvas.save();
			canvas.translate(0, scroll);
			canvas.concat(mMatrix1);
			canvas.clipRect(0, -mUpperFarClipPlaneZ, mWidth, 0);
			if (mBackground != null) {
				int bottom = upperHeight - mBackgroundOffsetY;
				mSrcRect.set(0, Math.max(bottom - mUpperFarClipPlaneZ, 0), mBackgroundWidth, bottom);
				mDstRect.set(mBackgroundOffsetX, -mUpperFarClipPlaneZ, mBackgroundWidth
						+ mBackgroundOffsetX, 0);
				canvas.drawBitmap(mBackground, mSrcRect, mDstRect, null);
			}
			if (scroll > -mUpperBottom - extendDistance) {
				canvas.translate(0, -scroll - mUpperBottom);
				mContainer.onDraw(canvas, scroll + mUpperBottom - mUpperFarClipPlaneZ, scroll
						+ mUpperBottom, VerticalListContainer.PART_UP);
			}
			canvas.restore();
		}
		// draw middle part
		canvas.save();
		canvas.translate(0, scroll);
		canvas.clipRect(0, upperHeight, mWidth, mHeight - mLowerHeight);
		if (mBackground != null) {
			canvas.drawBitmap(mBackground, mBackgroundOffsetX, mBackgroundOffsetY, null);
			// if(mShadowColor != 0){
			// canvas.drawColor(mShadowColor);
			// }
		}
		final int y = scroll + mMidHeight;
		canvas.translate(0, -mUpperTop - scroll);
		mContainer.onDraw(canvas, scroll + mUpperBottom, y, VerticalListContainer.PART_MID);
		canvas.restore();
		// draw lower part
		if (mTotalHeight + extendDistance >= y || mBackground != null) {
			canvas.save();
			canvas.translate(0, scroll);
			canvas.concat(mMatrix2);
			canvas.clipRect(0, 0, mWidth, mLowerFarClipPlaneZ);
			if (mBackground != null) {
				int top = mHeight - mLowerHeight - mBackgroundOffsetY;
				mSrcRect.set(0, top, mBackgroundWidth,
						Math.min(top + mLowerFarClipPlaneZ, mBackgroundHeight));
				mDstRect.set(mBackgroundOffsetX, 0, mBackgroundWidth + mBackgroundOffsetX,
						mLowerFarClipPlaneZ);
				canvas.drawBitmap(mBackground, mSrcRect, mDstRect, null);
			}
			if (mTotalHeight + extendDistance >= y) {
				canvas.translate(0, -y);
				mContainer.onDraw(canvas, y, y + mLowerFarClipPlaneZ,
						VerticalListContainer.PART_DOWN);
			}
			canvas.restore();
		}
		if (mShadowColor != 0) {
			// draw upper shadow
			canvas.translate(0, scroll + upperHeight);
			{
				mPaint.setShader(mShader1);
				mShaderMatrix.setTranslate(0, -mProjectY1);
				mShaderMatrix.preScale(1, mProjectY1);
				mShader1.setLocalMatrix(mShaderMatrix);
				canvas.drawRect(0, -upperHeight, mWidth, 0, mPaint);

				mPaint.setShader(mShader2);
				mShaderMatrix.setScale(1, mShadowBlurRadius);
				mShader2.setLocalMatrix(mShaderMatrix);
				canvas.drawRect(0, 0, mWidth, mShadowBlurRadius, mPaint);
			}
			// draw lower shadow
			canvas.translate(0, -mUpperBottom + mMidHeight);
			{
				mPaint.setShader(mShader1);
				mShaderMatrix.setTranslate(0, mProjectY2);
				mShaderMatrix.preRotate(180);
				mShaderMatrix.preScale(1, mProjectY2);
				mShader1.setLocalMatrix(mShaderMatrix);
				canvas.drawRect(0, 0, mWidth, mLowerHeight, mPaint);

				mPaint.setShader(mShader2);
				mShaderMatrix.setRotate(180);
				mShaderMatrix.preScale(1, mShadowBlurRadius);
				mShader2.setLocalMatrix(mShaderMatrix);
				canvas.drawRect(0, -mShadowBlurRadius, mWidth, 0, mPaint);
			}
			mPaint.setShader(null);
		}
		return true;
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		mOrientation = orientation;
		mWidth = w;
		if (mExternal) {
			mMidHeight = h;
		} else {
			mHeight = h;
		}
		mTotalHeight = mScroller.getTotalHeight();
		if (mOrientation != Scroller.VERTICAL) {
			throw new IllegalArgumentException("WaterFallEffector only support vertical direction!");
		}
		updateLayout();
	}

	private void updateLayout() {
		if (mExternal) {
			mHeight = mMidHeight - mUpperTop + mLowerHeight;
		} else {
			mMidHeight = mHeight + mUpperTop - mLowerHeight;
		}
		if (mBackground != null) {
			mBackgroundWidth = mBackground.getWidth();
			mBackgroundHeight = mBackground.getHeight();
			mBackgroundOffsetX = (mWidth - mBackgroundWidth) / 2;
			mBackgroundOffsetY = (mHeight - mBackgroundHeight) / 2;
		}

		final float centerX = mWidth * 0.5f;
		float scale = mUpperFarClipPlaneZ / (mUpperFarClipPlaneZ + CameraZ);

		int upperHeight = mUpperBottom - mUpperTop;
		final float tan1 = (float) Math.tan(Math.toRadians(40));
		// make sure cameraY1 * scale = mProjectY1 <= upperHeight * 0.9f
		final float cameraY1 = Math.min(centerX * tan1, upperHeight * 0.9f / scale);
		mProjectY1 = cameraY1 * scale;
		Camera camera = new Camera();
		camera.save();
		camera.translate(-centerX, -cameraY1, 0);
		camera.rotateX(90);
		camera.getMatrix(mMatrix1);
		mMatrix1.postTranslate(centerX, upperHeight - cameraY1);
		camera.restore();

		scale = mLowerFarClipPlaneZ / (mLowerFarClipPlaneZ + CameraZ);
		final float tan2 = (float) Math.tan(Math.toRadians(45));
		final float cameraY2 = Math.min(centerX * tan2, mLowerHeight * 0.9f / scale);
		mProjectY2 = cameraY2 * scale;
		camera.save();
		camera.translate(-centerX, cameraY2, 0);
		camera.rotateX(-90);
		camera.getMatrix(mMatrix2);
		mMatrix2.postTranslate(centerX, mHeight - mLowerHeight + cameraY2);
		camera.restore();

	}

	public void setBackground(Bitmap bitmap) {
		mBackground = bitmap;
		updateLayout();
	}

	/**
	 * 设置折痕处的阴影颜色
	 * 
	 * @param color
	 *            默认为0x40000000，设为0则不绘制
	 */
	public void setShadowColor(int color) {
		mShadowColor = color;
		if ((mShadowColor >>> 24) == 0) {
			mShadowColor = 0;
		}
	}

	/**
	 * 设置布局参数
	 * 
	 * @param upperHeight
	 *            上部的高度
	 * @param lowerHeight
	 *            下部的高度
	 * @param external
	 *            上部和下部是否位于视图之外（需要取消裁剪）
	 * @param foldLen
	 *            翻折部分的长度
	 * @param rowNums
	 *            单屏行数
	 * @param cellHeight
	 *            单元格高度
	 * @param verticalCycleMode
	 *            是否竖向循环
	 */
	public void setLayoutArg(int upperTop, int upperBottom, int lowerHeight, boolean external,
			int foldLen, int rowNums, int cellHeight, boolean verticalCycleMode) {
		mUpperTop = upperTop;
		mUpperBottom = upperBottom;
		mLowerHeight = lowerHeight;
		mExternal = external;
		mUpperFarClipPlaneZ = Math.max(1, foldLen);
		mLowerFarClipPlaneZ = Math.max(1, foldLen);
		mRowNums = rowNums;
		mCellHeight = cellHeight;
		mVerticalCycleMode = verticalCycleMode;
		updateLayout();
	}

	@Override
	public void onAttach(Scroller scroller, ScrollerListener container) {
		assert (scroller != null);
		if (container != null && container instanceof VerticalListContainer) {
			mContainer = (VerticalListContainer) container;
			if (mScroller != scroller) {
				mScroller = scroller;
				mOrientation = mScroller.getOrientation();
			}
		} else {
			throw new IllegalArgumentException(
					"container is not an instance of SubScreenEffector.SubScreenContainer");
		}

		mShadowBlurRadius = mScroller.getDensity() * 5;

	}

	@Override
	public void onDetach() {
		mScroller = null;
		mContainer = null;

	}

	@Override
	public void setDrawQuality(int quality) {

	}
}