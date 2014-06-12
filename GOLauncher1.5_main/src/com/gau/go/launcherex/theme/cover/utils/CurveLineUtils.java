package com.gau.go.launcherex.theme.cover.utils;

import android.graphics.Point;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 
 * <br>类描述:计算圆
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-13]
 */
public class CurveLineUtils {

	private static final Interpolator DE_INTERPOLATOR = new DecelerateInterpolator(1.5f);
	private static final Interpolator AC_DE_INTERPOLATOR = new AccelerateDecelerateInterpolator();

	/**
	 * <br>注意:顺时针计算
	 * @param t [0, 1]
	 * @param startAngle [180, -180]
	 * @param endAngle [180, -180]
	 */
	public static void getCurvePoint(Point center, int r, int startAngle, int endAngle, float t,
			Point outPoint) {
		if (outPoint == null) {
			return;
		}
		int angle = startAngle - endAngle;
		t = startAngle - angle * t;
		if (t <= 0) {
			t += 360;
		}
		t = (float) Math.toRadians(t);
		outPoint.x = (int) (center.x + r * Math.cos(t));
		outPoint.y = (int) (center.y - r * Math.sin(t));
	}

	public static void getLinePoint(Point startPoint, Point endPoint, float t, Point outPoint) {
		if (outPoint == null) {
			return;
		}
		t = DE_INTERPOLATOR.getInterpolation(t);
		t = Math.min(t, 1);
		t = Math.max(t, 0);
		outPoint.x = (int) (startPoint.x + (endPoint.x - startPoint.x) * t);
		outPoint.y = (int) (startPoint.y + (endPoint.y - startPoint.y) * t);
	}

	public static int getInterpolatorAngle(int startAngle, int endAngle, float t) {
		int angle;
		t = AC_DE_INTERPOLATOR.getInterpolation(t);
		t = Math.min(t, 1);
		t = Math.max(t, 0);
		angle = (int) (startAngle + (endAngle - startAngle) * t);
		return angle;
	}

}
