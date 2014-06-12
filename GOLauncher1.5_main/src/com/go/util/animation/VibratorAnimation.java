package com.go.util.animation;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class VibratorAnimation extends Animation {

	private final static int DIS = 30;
	private int halfHeight;
	private Camera camera = new Camera();
	private int dis = -DIS;
	private boolean isUp = true;

	private float mVibratorDis;
	private float mStartPointX;
	private float mEndPointX;
	private int mWidth;

	public VibratorAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 
	 * @param vibratorDis
	 *            震动距离的百分比 [ 0,1 ]
	 * @param startPointX
	 *            开始点，屏幕宽度的百分比[ 0,1 ]
	 * @param endPointX
	 *            结束点，屏幕宽度的百分比[ 0,1 ]
	 */
	public VibratorAnimation(float vibratorDis, float startPointX, float endPointX) {
		mVibratorDis = vibratorDis;
		mStartPointX = startPointX;
		mEndPointX = endPointX;
		if (mVibratorDis > 1 || mVibratorDis < 0) {
			throw new IllegalStateException("vibratorDis must 0 to 1 !!!");
		}
		if (mStartPointX > 1 || mStartPointX < 0) {
			throw new IllegalStateException("vibratorDis must 0 to 1 !!!");
		}
		if (mEndPointX > 1 || mEndPointX < 0) {
			throw new IllegalStateException("vibratorDis must 0 to 1 !!!");
		}
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mWidth = width;
		halfHeight = height / 2;
		setInterpolator(new LinearInterpolator());
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final Matrix matrix = t.getMatrix();
		camera.save();
		float factor = 1 - interpolatedTime;
		float z = factor > mVibratorDis ? 1 : factor + factor * factor;
		camera.translate(0.0f, 0.0f, 800 * z);

		camera.getMatrix(matrix);
		matrix.preTranslate(0, -halfHeight);
		if (interpolatedTime < mVibratorDis) {
			if (dis > 0) {
				dis = 0;
				isUp = false;
			} else if (dis < 0) {
				dis = 0;
				isUp = true;
			} else {
				if (isUp) {
					dis = DIS;
				} else {
					dis = -DIS;
				}
			}
			matrix.postTranslate(mWidth * mStartPointX * (1 - interpolatedTime), halfHeight + dis);
		} else {
			matrix.postTranslate(mWidth * mStartPointX * (1 - interpolatedTime), halfHeight);
		}
		camera.restore();
	}
}
