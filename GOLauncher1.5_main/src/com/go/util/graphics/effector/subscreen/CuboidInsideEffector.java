package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;

import com.go.util.scroller.ScreenScroller;

class CuboidInsideEffector extends FlipEffector {
	final static float PI4 = (float) Math.PI / 4;
	final static float SQRT2 = (float) Math.sqrt(2);

	int mTranZ = 0; // （当盒子边长大于CameraZ时），为了使其他视图不位于 Camera 后面（-Z方向），
					// 需要将视图向前移动 mTranZ。只是裁剪视图位于 Camera 后面的部分是不可行的，
					// 貌似是因为底层实现是使用裁剪前的顶点坐标来投影并计算屏幕上的增量。
	float mScale = 1; // 向前移动之后视图在屏幕上的投影会变小，那么必须放大一个倍数来修正

	CuboidInsideEffector() {
		mCombineBackground = false;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRadRatio = -HALF_PI / mScreenSize;
		mAngleRatio = -90.0f / mScreenSize;
		mInnerRadius = -mScreenSize * 0.5f;
		mOuterRadius = mInnerRadius * SQRT2;
		mScale = 1 + mScreenSize / CameraZ;
		mTranZ = mScreenSize;

	}

	@Override
	float computeCurrentDepthZ(float rad) {
		// 对于盒子来说，b=pi/2, 1/sin(b/2)=sqrt(2)
		return mInnerRadius + (mInnerRadius - (float) (Math.cos(rad - PI4)) * mOuterRadius) // 动态深度控制
				+ mTranZ;
	}

	@Override
	void transform(Canvas canvas, float angle) {
		mCamera.translate(0, 0, mCenterZ);
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + mCenterX, mCenterY);
			canvas.scale(mScale, mScale);
			mCamera.rotateY(angle);
		} else {
			canvas.translate(mCenterX, mScroll + mCenterY);
			canvas.scale(mScale, mScale);
			mCamera.rotateX(-angle);
		}
		if (mInnerRadius != 0) {
			mCamera.translate(0, 0, -mInnerRadius);
		}
		// mCamera.applyToCanvas(canvas);
		mCamera.getMatrix(MATRIX);
		canvas.concat(MATRIX);
		canvas.translate(-mCenterX, -mCenterY);
	}

}
