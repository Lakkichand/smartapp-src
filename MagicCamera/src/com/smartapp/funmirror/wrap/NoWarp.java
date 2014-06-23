package com.smartapp.funmirror.wrap;

public class NoWarp implements CartesianWarpStrategy {
	public void warp(float x, float y, float[] param, float[] output) {
		output[0] = x;
		output[1] = y;
	}
}
