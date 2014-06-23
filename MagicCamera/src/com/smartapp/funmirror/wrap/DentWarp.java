package com.smartapp.funmirror.wrap;

public class DentWarp implements PolarWarpStrategy {
	public void warp(float r, float thera, float param, float[] output) {
		output[0] = (param * WarpMathUtil.smoothstep(0.0F, 0.7F * param, r));
		output[1] = thera;
	}
}
