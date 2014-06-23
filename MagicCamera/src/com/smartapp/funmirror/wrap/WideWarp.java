package com.smartapp.funmirror.wrap;

import android.util.FloatMath;

public class WideWarp implements CartesianWarpStrategy {
	public void warp(float x, float y, float[] params, float[] output) {
		output[0] = (0.6F * Math.signum(x) * FloatMath.sqrt(Math.abs(x)) * FloatMath.sqrt(params[0]));
		output[1] = y;
	}
}
