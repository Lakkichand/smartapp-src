package com.go.util.animation;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class RotateAnimation extends Animation {

	private int halfWidth;
	private int halfHeight;
	private Camera camera = new Camera();
	/**
	 * 开始的深度
	 */
	private float mStartDeepZ;
	/**
	 * 结束的深度
	 */
	private float mEndDeepZ;
	/**
	 * 深度差
	 */
	private float mZDistance;
	/**
	 * 是否正向(越来越远为正向)
	 */
	private boolean mIsZForward;
	/**
	 * 旋转的开始角度
	 */
	private float mStartDegrees;
	/**
	 * 旋转的结束角度
	 */
	private float mEndDegrees;
	/**
	 * 角度差
	 */
	private float mDegreesDistance;
	/**
	 * 是否正向(右手法则为正向)
	 */
	private boolean mIsDegreesForward;

	public RotateAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 
	 * @param startDeepZ
	 *            开始深度
	 * @param endDeepZ
	 *            结束深度
	 * @param startDegrees
	 *            开始角度
	 * @param endDegrees
	 *            结束角度
	 */
	public RotateAnimation(float startDeepZ, float endDeepZ, float startDegrees, float endDegrees) {
		mStartDeepZ = startDeepZ;
		mEndDeepZ = endDeepZ;
		mZDistance = startDeepZ - endDeepZ;
		if (mZDistance < 0) {
			mIsZForward = true;
		}
		mZDistance = Math.abs(mZDistance);

		mStartDegrees = startDegrees;
		mEndDegrees = endDegrees;
		mDegreesDistance = startDegrees - endDegrees;
		if (mDegreesDistance < 0) {
			mIsDegreesForward = true;
		}
		mDegreesDistance = Math.abs(mDegreesDistance);
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		halfWidth = width / 2;
		halfHeight = height / 2;
		setInterpolator(new LinearInterpolator());
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final Matrix matrix = t.getMatrix();

		camera.save();
		if (mIsZForward) {
			camera.translate(0.0f, 0.0f, mZDistance * interpolatedTime + mStartDeepZ);
		} else {
			camera.translate(0.0f, 0.0f, mZDistance * (1 - interpolatedTime) + mEndDeepZ);
		}
		if (mIsDegreesForward) {
			float deg = mDegreesDistance * interpolatedTime + mStartDegrees;
			camera.rotateY(deg);
		} else {
			float deg = mDegreesDistance * (1 - interpolatedTime) + mEndDegrees;
			camera.rotateY(deg);
		}
		camera.getMatrix(matrix);

		matrix.preTranslate(-halfWidth, -halfHeight);
		matrix.postTranslate(halfWidth, halfHeight);

		camera.restore();
	}
}
