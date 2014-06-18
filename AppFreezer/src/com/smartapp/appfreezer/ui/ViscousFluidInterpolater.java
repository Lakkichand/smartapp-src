package com.smartapp.appfreezer.ui;

import android.view.animation.Interpolator;

/**
 * 粘性液体插值器，模拟物体在粘性液体中的运动效果。 代码修改自{@link android.widget.Scroller}。
 * 
 * @author dengweiming
 */
public class ViscousFluidInterpolater implements Interpolator {
	// 1/e == exp(-1)

	final static float MID_VALUE = 0.36787944117f;

	// This controls the viscous fluid effect (how much of it)
	final static float VISCOUS_FLUIDSCALE = 8.0f;
	// must be set to 1.0 (used in viscousFluid())
	static float mViscousFluidNormalize = 1.0f;

	static {
		// 在mViscousFluidNormalize为1时，计算y值范围，再取倒数作为y值规范化时的缩放比例
		mViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
	}

	/**
	 * 物体在粘性液体中的运动曲线函数
	 * 
	 * @param x
	 *            插值时间[0, 1]
	 * @return
	 */
	public static float viscousFluid(float x) {
		x *= VISCOUS_FLUIDSCALE;
		float y;
		if (x < 1.0f) {
			y = x - (1.0f - (float) Math.exp(-x)); // 结果范围为[0, MidValue]
		} else {
			y = 1.0f - (float) Math.exp(1.0f - x); // 结果范围为[0, 1)
			y = MID_VALUE + y * (1.0f - MID_VALUE); // 在[MidValue,
													// 1]区间按y插值，结果范围为[0, 1)
		}
		return y * mViscousFluidNormalize; // 规范化到[0, 1]
	}

	@Override
	public float getInterpolation(float input) {
		return viscousFluid(input);
	}
}