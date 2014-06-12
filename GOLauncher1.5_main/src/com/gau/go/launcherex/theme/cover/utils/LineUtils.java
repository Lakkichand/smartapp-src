package com.gau.go.launcherex.theme.cover.utils;

import java.util.Random;

import android.graphics.Point;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 
 * <br>类描述:蜻蜓飞行的折线
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-31]
 */
public class LineUtils {

	private static final Random RANDOM = new Random();
	private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
	
	public static Point nextBrokenLinePoint(Point startPoint, int viewWidth, int viewHeight) {
		Point endPoint = new Point();
		if ((RANDOM.nextInt(2) & 1) == 1) {
			if (startPoint.x < viewWidth / 2) {
				endPoint.x = RANDOM.nextInt(viewWidth / 2) + viewWidth / 2;
			} else {
				endPoint.x = RANDOM.nextInt(viewWidth / 2);
			}
			endPoint.y = RANDOM.nextInt(viewHeight);
		} else {
			if (startPoint.y < viewHeight / 2) {
				endPoint.y = RANDOM.nextInt(viewHeight / 2) + viewHeight / 2;
			} else {
				endPoint.y = RANDOM.nextInt(viewHeight / 2);
			}
			endPoint.x = RANDOM.nextInt(viewWidth);
		} 	
		return endPoint;
	}
	
	public static Point getRattanAreaPoint(Point startPoint, int viewWidth, int viewHeight) {
		Point endPoint = new Point();
		endPoint.y = RANDOM.nextInt(viewHeight / 4);
		endPoint.x = RANDOM.nextInt(viewHeight);
		return endPoint;
	}
	
	public static void getInterpolatorPoint(Point startPoint, Point endPoint, Point outPoint, float t) {
		if (outPoint == null) {
			return;
		}
		t = INTERPOLATOR.getInterpolation(t);
		t = Math.min(t, 1);
		t = Math.max(t, 0);
		outPoint.x = (int) (startPoint.x + (endPoint.x - startPoint.x) * t);
		outPoint.y = (int) (startPoint.y + (endPoint.y - startPoint.y) * t);
	}
	
}
