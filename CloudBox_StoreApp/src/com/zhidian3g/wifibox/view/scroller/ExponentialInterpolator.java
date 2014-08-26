package com.zhidian3g.wifibox.view.scroller;

import android.view.animation.Interpolator;

/**
 * 指数函数插值器，其速度变化很快。
 * 
 * @author dengweiming
 * 
 */
public class ExponentialInterpolator implements Interpolator {
	/** 减速 */
	public final static int EASE_OUT = 0;
	/** 加速 */
	public final static int EASE_IN = 1;
	/** 先加速后减速 */
	public final static int EASE_IN_OUT = 2;

	int mType;

	/**
	 * 使用指定的速度变化类型创建插值器
	 * 
	 * @param type
	 *            取值为{@link #EASE_IN}, {@link #EASE_OUT}, {@link #EASE_IN_OUT}。
	 */
	public ExponentialInterpolator(int type) {
		mType = type;
	}

	/**
	 * 创建速度变化类型为{@link #EASE_OUT}的插值器
	 */
	public ExponentialInterpolator() {

	}

	@Override
	public float getInterpolation(float input) {
		switch (mType) {
			case EASE_OUT :
				return 1 - (float) Math.pow(2, -10 * input);
			case EASE_IN :
				return (float) Math.pow(2, 10 * input - 10);
			case EASE_IN_OUT :
				input *= 2;
				if (input < 1) {
					return (float) Math.pow(2, 10 * input - 10) * 0.5f;
				} else {
					return (float) Math.pow(2, 10 - 10 * input) * 0.5f;
				}
		}
		return input;
	}

}
