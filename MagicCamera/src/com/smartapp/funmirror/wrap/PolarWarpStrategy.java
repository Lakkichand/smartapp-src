package com.smartapp.funmirror.wrap;

public abstract interface PolarWarpStrategy extends WarpStrategy {
	public abstract void warp(float r, float thera, float param, float[] output);
}
