package com.smartapp.funmirror.wrap;

public class SquareWarp implements CartesianWarpStrategy {
	public void warp(float x, float y, float[] param, float[] output) {
		float f1 = Math.abs(x);
		float f2 = (0.6F * f1 + f1 * (0.4F * WarpMathUtil.smoothstep(0.2F * param[0], 0.4F * param[0], f1))) * Math.signum(x);
		float f3 = Math.abs(y);
		float f4 = (0.6F * f3 + f3 * (0.4F * WarpMathUtil.smoothstep(0.2F * param[1], 0.4F * param[1], f3))) * Math.signum(y);
		output[0] = f2;
		output[1] = f4;
	}
}
