package com.smartapp.funmirror.wrap;

public abstract interface CartesianWarpStrategy extends WarpStrategy {
	public abstract void warp(float x, float y, float[] params, float[] output);
}
