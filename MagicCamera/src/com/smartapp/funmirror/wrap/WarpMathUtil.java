package com.smartapp.funmirror.wrap;

public class WarpMathUtil {
	public static float smoothstep(float edge0, float edge1, float x) {
		float f = Math.min(Math.max(0.0F, (x - edge0) / (edge1 - edge0)), 1.0F);
		return f * f * (3.0F - 2.0F * f);
	}
}
