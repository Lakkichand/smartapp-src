package com.go.util.graphics.effector.subscreen;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.go.util.scroller.ScreenScroller;

class FlipEffector extends MSubScreenEffector {
	static final float CameraZ = 576f; // 底层skia图形库定义的常量
	static final float HALF_PI = (float) Math.PI / 2;
	static final Matrix MATRIX = new Matrix();
	float mInnerRadius = 0; // 旋转中心到视图中心的距离（内切圆半径）
	float mOuterRadius; // 旋转中心到视图两边的距离（外接圆半径）
	float mCenterZ; // 旋转中心的z坐标
	float mRadRatio;
	float mAngleRatio;
	Camera mCamera = new Camera(); // 相机，提供透视投影效果

	FlipEffector() {
		mCombineBackground = false;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRadRatio = (float) Math.PI / mScreenSize;
		mAngleRatio = 180.0f / mScreenSize;
		mOuterRadius = mScreenSize * 0.5f;
	}

	@Override
	protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first) {
		final float angle = offset * mAngleRatio;
		final float angleAbs = Math.abs(angle);
		if (angleAbs >= 90) {
			return false;
		}
		mCamera.save();
		transform(canvas, angle);
		mCamera.restore();
		return true;
	}

	float computeCurrentDepthZ(float rad) {
		// 在和旋转轴垂直的平面上，旋转中心到边（视图在zOx平面的投影）两端形成的角度为b时，
		// 则距离为mOuterRadius=mHalfScreenSize/sin(b/2)，
		// 当旋转角度为rad时，则该距离在Z轴上投影为d'=d*cos(rad-b/2)，
		// 则需将旋转中心移入深度d'，使得整个视图都在z+半空间中
		// 对于单面翻转模型，b=pi，1/sin(b/2)=1
		return (float) Math.cos(rad - HALF_PI) * mOuterRadius;
	}

	void transform(Canvas canvas, float angle) {
		mCamera.translate(0, 0, mCenterZ);
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + mCenterX, mCenterY);
			mCamera.rotateY(angle);
		} else {
			canvas.translate(mCenterX, mScroll + mCenterY);
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

	@Override
	protected void onScrollChanged(int scroll, int offset) {
		mCenterZ = computeCurrentDepthZ(Math.abs(offset * mRadRatio));
		super.onScrollChanged(scroll, offset);
	}
}
