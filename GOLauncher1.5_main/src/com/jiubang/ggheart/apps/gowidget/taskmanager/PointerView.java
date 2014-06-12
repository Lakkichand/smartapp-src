package com.jiubang.ggheart.apps.gowidget.taskmanager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class PointerView extends ImageView {
	/**
	 * 旋转中心 x
	 */
	private float centralX = 0;
	/**
	 * 旋转中心 y
	 */
	private float centralY = 0;

	/**
	 * 旋转最大角度
	 */
	private int maxAngel = 45;

	/**
	 * 当前旋转角度
	 */
	private float curAngel = 0;

	/**
	 * 恒定速度旋转
	 */
	private final float constSpeed = 1000 / 45;

	/**
	 * 初始角度
	 */
	private final int STARTANGEL = -45;

	public PointerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PointerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PointerView(Context context) {
		super(context, null);
	}

	public void updateCentral() {
		Drawable image = getDrawable();
		if (image != null) {
			centralX = image.getIntrinsicWidth() / 2;
			centralY = (float) (image.getIntrinsicWidth() * 0.7);
		}
	}

	public void updateAnagel(float percent) {
		float angel;
		angel = (float) (0.9 * percent - 45.0);
		chooseAnimation(angel);
	}

	private void chooseAnimation(float toAngel) {

		// if (isMoving()) {
		// return;
		// }

		int toCapare = (int) Math.abs(toAngel);
		if (toCapare >= maxAngel) {
			if (toAngel > 0) {
				toAngel = maxAngel;
			} else {
				toAngel = -maxAngel;
			}
		}

		if ((int) toAngel == STARTANGEL) {
			startAnimation(toAngel);
		} else {
			startBackAnimation(toAngel);
		}
	}

	/*
	 * 从当前动画返回到初始位置
	 */
	private void startBackAnimation(float toAngel) {

		float offsetAngel = Math.abs(STARTANGEL - curAngel);
		int druation = (int) (offsetAngel * constSpeed) / 3;

		Animation anim = null;
		anim = new RotateAnimation(curAngel, STARTANGEL, centralX, centralY);
		anim.setDuration(druation);
		anim.setFillAfter(true);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				startAnimation(curAngel);
			}
		});

		startAnimation(anim);
		curAngel = toAngel;
	}

	/*
	 * 从初始位置到指定角度
	 */
	private void startAnimation(float toAngel) {

		float offsetAngel = Math.abs(toAngel - STARTANGEL);
		int druation = (int) (offsetAngel * constSpeed);

		Animation anim = null;

		anim = new RotateAnimation(STARTANGEL, toAngel, centralX, centralY);
		anim.setDuration(druation);
		anim.setFillAfter(true);
		startAnimation(anim);

		curAngel = toAngel;
	}

	/*
	 * 从初始位置到指定角度
	 */
	public int backToZeroAngel() {

		float offsetAngel = Math.abs(STARTANGEL - curAngel);
		int druation = (int) (offsetAngel * constSpeed) / 3;

		Animation anim = null;
		anim = new RotateAnimation(curAngel, STARTANGEL, centralX, centralY);
		anim.setDuration(druation);
		anim.setFillAfter(true);

		startAnimation(anim);
		curAngel = STARTANGEL;

		return druation;
	}

	public boolean isMoving() {
		return (getAnimation() != null && getAnimation().hasStarted() && !getAnimation().hasEnded());
	}
}
