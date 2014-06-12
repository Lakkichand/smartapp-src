package com.go.util.animation;

import android.view.animation.Interpolator;

public class ExponentialInterpolator implements Interpolator {
	public final static int EASE_OUT = 0;
	public final static int EASE_IN = 1;
	public final static int EASE_IN_OUT = 2;

	int mType;

	public ExponentialInterpolator(int type) {
		mType = type;
	}

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
