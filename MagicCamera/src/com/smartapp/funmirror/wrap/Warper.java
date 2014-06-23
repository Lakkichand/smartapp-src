package com.smartapp.funmirror.wrap;

import android.util.FloatMath;

public class Warper {

	public static final String WRAP_KEY = "WRAP_KEY";

	public static final String NOWARP = "NOWARP";

	public static final String BULGEWARP = "BULGEWARP";

	public static final String DENTWARP = "DENTWARP";

	public static final String SQUAREWARP = "SQUAREWARP";

	public static final String SWIRLWARP = "SWIRLWARP";

	public static final String TALLWARP = "TALLWARP";

	public static final String WIDEWARP = "WIDEWARP";

	private void applyWarp(float[] src, float[] dst, int count, float width,
			float height, float cx, float cy, CartesianWarpStrategy strategy) {
		float[] output = new float[2];
		float[] params = { width, height };
		float xmin = Float.MAX_VALUE;
		float xmax = Float.MIN_VALUE;
		float ymin = Float.MAX_VALUE;
		float ymax = Float.MIN_VALUE;
		for (int i = 0; i < count * 2; i = i + 2) {
			float x = src[(i + 0)];
			float y = src[(i + 1)];
			strategy.warp(x - cx, y - cy, params, output);
			float xnew = output[0];
			float ynew = output[1];
			float _xres = xnew + cx;
			float _yres = ynew + cy;
			dst[(i + 0)] = _xres;
			dst[(i + 1)] = _yres;
			xmin = Math.min(_xres, xmin);
			xmax = Math.max(_xres, xmax);
			ymin = Math.min(_yres, ymin);
			ymax = Math.max(_yres, ymax);
		}

		float xscale = width / (xmax - xmin);
		float yscale = height / (ymax - ymin);
		for (int j = 0; j < count * 2; j = j + 2) {
			dst[(j + 0)] = (xscale * (dst[(j + 0)] - xmin));
			dst[(j + 1)] = (yscale * (dst[(j + 1)] - ymin));
		}
	}

	private void applyWarp(float[] src, float[] dst, int count, float width,
			float height, float cx, float cy, PolarWarpStrategy strategy) {
		float[] output = new float[2];
		float params = Math.max(width, height);
		float xmin = Float.MAX_VALUE;
		float xmax = Float.MIN_VALUE;
		float ymin = Float.MAX_VALUE;
		float ymax = Float.MIN_VALUE;

		for (int i = 0; i < count * 2; i = i + 2) {
			float x = src[(i + 0)];
			float y = src[(i + 1)];
			float dx = x - cx;
			float dy = y - cy;
			strategy.warp(FloatMath.sqrt(dx * dx + dy * dy),
					(float) Math.atan2(dy, dx), params, output);
			float xnew = output[0] * FloatMath.cos(output[1]);
			float ynew = output[0] * FloatMath.sin(output[1]);
			float _xres = xnew + cx;
			float _yres = ynew + cy;
			dst[(i + 0)] = _xres;
			dst[(i + 1)] = _yres;
			xmin = Math.min(_xres, xmin);
			xmax = Math.max(_xres, xmax);
			ymin = Math.min(_yres, ymin);
			ymax = Math.max(_yres, ymax);
		}

		float xscale = width / (xmax - xmin);
		float yscale = height / (ymax - ymin);
		for (int j = 0; j < count * 2; j = j + 2) {
			dst[(j + 0)] = (xscale * (dst[(j + 0)] - xmin));
			dst[(j + 1)] = (yscale * (dst[(j + 1)] - ymin));
		}
	}

	public void applyWarp(float[] src, float[] dst, int count, float width,
			float height, float cx, float cy, WarpStrategy strategy) {
		if ((strategy instanceof CartesianWarpStrategy)) {
			applyWarp(src, dst, count, width, height, cx, cy,
					(CartesianWarpStrategy) strategy);
		}

		if ((strategy instanceof PolarWarpStrategy)) {
			applyWarp(src, dst, count, width, height, cx, cy,
					(PolarWarpStrategy) strategy);
		}
	}
}
