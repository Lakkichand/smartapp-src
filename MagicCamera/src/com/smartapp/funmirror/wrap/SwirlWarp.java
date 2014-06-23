package com.smartapp.funmirror.wrap;

public class SwirlWarp implements PolarWarpStrategy {
	public void warp(float r, float thera, float param, float[] output) {
		output[0] = r;
		output[1] = (thera + 4.0F * (1.0F - WarpMathUtil.smoothstep(-0.5F * param, 0.5F * param, r)));
	}
}
