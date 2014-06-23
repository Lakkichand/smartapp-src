package com.smartapp.funmirror.wrap;

import android.util.FloatMath;

public class TallWarp implements CartesianWarpStrategy {
	public void warp(float x, float y, float[] param, float[] output) {
		output[0] = x;
		output[1] = (0.6F * Math.signum(y) * FloatMath.sqrt(Math.abs(y)) * FloatMath.sqrt(param[1]));
	}
}
