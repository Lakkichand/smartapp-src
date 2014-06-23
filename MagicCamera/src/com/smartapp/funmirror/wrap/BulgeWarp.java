package com.smartapp.funmirror.wrap;

import android.util.FloatMath;

public class BulgeWarp implements PolarWarpStrategy {
	public void warp(float r, float thera, float param, float[] output) {
		output[0] = (0.6F * FloatMath.sqrt(r) * FloatMath.sqrt(param));
		output[1] = thera;
	}
}
