package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;
import android.graphics.Matrix;

import com.go.util.scroller.ScreenScroller;

class CuboidOutsideEffector extends FlipEffector {
	final static float PI4 = (float) Math.PI / 4;
	final static float SQRT2 = (float) Math.sqrt(2);
	float mCullPassAngle; // 旋转小于该角度时，视图是正面的
	float mCullFailAngle; // 旋转大于该角度时，视图是反面的
	float[] mPoints = new float[4];
	Matrix mMatrix = new Matrix();

	CuboidOutsideEffector() {
		mCombineBackground = false;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRadRatio = HALF_PI / mScreenSize;
		mAngleRatio = 90.0f / mScreenSize;
		mInnerRadius = mScreenSize * 0.5f;
		mOuterRadius = mInnerRadius * SQRT2;
		final float maxZ = computeCurrentDepthZ(PI4);
		// 角度的绝对值增大时，在透视作用下会逐渐从正面变为反面，临界角的方程为
		// cos(angle)=mInnerRadius/(mCenterZ+CameraZ), mCenterZ=[mInnerRadius,
		// maxZ]
		// 深度增大时，解出来的角度的绝对值也增大，因此mCullPassAngle一定还是正面的，
		// mCullFailAngle一定已经是反面了
		mCullPassAngle = (float) Math.toDegrees(Math.acos(mInnerRadius / (mInnerRadius + CameraZ)));
		mCullFailAngle = (float) Math.toDegrees(Math.acos(mInnerRadius / (maxZ + CameraZ)));
		mOvershootPercent = (int) ((1 - mCullFailAngle / 90) * 100);
		mScroller.setOvershootPercent(mOvershootPercent);
		// 当mScreenSize=480时，mCullPassAngle=72.89536，
		// mCullFailAngle=74.800705，mOvershootPercent=16
	}

	@Override
	float computeCurrentDepthZ(float rad) {
		// 对于盒子来说，b=pi/2, 1/sin(b/2)=sqrt(2)
		return (float) Math.cos(rad - PI4) * mOuterRadius;
	}

	@Override
	protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first) {
		final float angle = offset * mAngleRatio;
		final float angleAbs = Math.abs(angle);
		if (angleAbs > mCullFailAngle) {
			return false;
		}
		mCamera.save();
		transform(canvas, angle);
		if (angleAbs < mCullPassAngle) {
			mCamera.restore();
			return true;
		}
		mCamera.getMatrix(mMatrix);
		mCamera.restore();
		return frontFaceTest(mMatrix, mPoints, this);
	}

	static boolean frontFaceTest(Matrix matrix, float[] points, MSubScreenEffector effector) {
		points[0] = 0;
		points[1] = 0;
		points[2] = effector.mWidth;
		points[3] = effector.mHeight;
		matrix.mapPoints(points);
		if (effector.mOrientation == ScreenScroller.HORIZONTAL) {
			// 透视投影后，左边界和右边界未穿越，表示还是正面
			return points[0] + 1 < points[2];
		}
		return points[1] + 1 < points[3];
	}
}
