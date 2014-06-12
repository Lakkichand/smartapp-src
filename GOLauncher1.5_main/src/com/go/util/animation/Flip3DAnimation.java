package com.go.util.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 3D空间中绕Y轴旋转的动画
 * 
 * @author dengweiming
 * 
 */
public class Flip3DAnimation extends Animation {
	float mFromAngle;
	float mToAngle;
	float mPivotX;
	float mPivotY;
	Camera mCamera;

	public Flip3DAnimation(float fromAngle, float toAngle) {
		mFromAngle = fromAngle;
		mToAngle = toAngle;
		mCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final Matrix matrix = t.getMatrix();
		float angle = mFromAngle + (mToAngle - mFromAngle) * interpolatedTime;
		// 移入一点深度，使得旋转中的最近边总是在z=0的位置
		float z = (float) Math.cos(Math.abs(Math.toRadians(angle)) - Math.PI * 0.5f) * mPivotX;
		mCamera.save();
		mCamera.translate(0, 0, z);
		mCamera.rotateY(angle);
		mCamera.getMatrix(matrix);
		mCamera.restore();
		matrix.postTranslate(mPivotX, mPivotY);
		matrix.preTranslate(-mPivotX, -mPivotY);
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);

		mPivotX = resolveSize(RELATIVE_TO_SELF, 0.5f, width, parentWidth);
		mPivotY = resolveSize(RELATIVE_TO_SELF, 0.5f, height, parentHeight);
	}

	@Override
	public boolean willChangeBounds() {
		return false;
	}
}
