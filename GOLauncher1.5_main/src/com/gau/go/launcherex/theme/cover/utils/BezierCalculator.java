package com.gau.go.launcherex.theme.cover.utils;

import java.util.Random;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * 
 * 类描述:贝塞尔曲线
 * 功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-17]
 */
public class BezierCalculator {

	private static final Random RANDOM = new Random();
	
	/**
	 * 功能简述: 算出二次贝塞尔曲线的点坐标
	 * 功能详细描述:
	 * 注意:
	 * @param points Point的数组，长度3，一个控制点
	 * @param t [0,1]
	 * @return
	 */
	public static Point twiceOrderBezier(Point[] points, float t) {
		Point point = new Point();
		if (points == null || points.length < 3) {
			return point;
		}
		float last = 1 - t;
		float tSquared = t * t;
		float tlast = last * t;
		float lastSquared = last * last;
		point.x = (int) (lastSquared * points[0].x + 2.0f * tlast * points[1].x + tSquared * points[2].x);
		point.y = (int) (lastSquared * points[0].y + 2.0f * tlast * points[1].y + tSquared * points[2].y);
		return point;
	}

	/**
	 * 功能简述:算出三次贝塞尔曲线的点坐标
	 * 功能详细描述:
	 * 注意:
	 * @param points points Point的数组，长度4，两个控制点
	 * @param t [0,1]
	 * @return 
	 */
	public static Point threeOrderBezier(Point[] points, float t) {
		Point result = new Point();
		if (points == null || points.length < 4) {
			return result;
		}
		float ax, bx, cx;
		float ay, by, cy;
		float tSquared, tCubed;

		cx = 3.0f * (points[1].x - points[0].x);
		bx = 3.0f * (points[2].x - points[1].x) - cx;
		ax = points[3].x - points[0].x - cx - bx;

		cy = 3.0f * (points[1].y - points[0].y);
		by = 3.0f * (points[2].y - points[1].y) - cy;
		ay = points[3].y - points[0].y - cy - by;

		tSquared = t * t;
		tCubed = tSquared * t;

		result.x = (int) ((ax * tCubed) + (bx * tSquared) + (cx * t) + points[0].x);
		result.y = (int) ((ay * tCubed) + (by * tSquared) + (cy * t) + points[0].y);

		return result;
	}

	public static Point getEndPoint(Point startPoint, Rect limitRect) {
		Point endPoint = new Point();
		int viewWidth = limitRect.right - limitRect.left;
		int viewHeight = limitRect.bottom - limitRect.top;
		if (startPoint.x < viewWidth / 2) {
			endPoint.x = RANDOM.nextInt(viewWidth * 4 / 9) + viewWidth * 5 / 9 + limitRect.left;
		} else {
			endPoint.x = RANDOM.nextInt(viewWidth * 4 / 9)  + limitRect.left;
		}
		if (startPoint.y < viewHeight / 2) {
			endPoint.y = RANDOM.nextInt(viewHeight * 4 / 9) + viewHeight * 5 / 9 + limitRect.top;
		} else {
			endPoint.y = RANDOM.nextInt(viewHeight * 4 / 9) + limitRect.top;
		}
		return endPoint;
	}
	
	public static Point getControlerByTwice(Point startPoint, Point endPoint) {
		Point point = new Point();
		point.x = startPoint.x;
		point.y = endPoint.y;
		return point;
	}
	
	public static Point[] getControlerByThree(Point startPoint, Point endPoint) {
		Point[] points = new Point[2];
		points[0] = new Point(startPoint.x, endPoint.y);
		points[1] = new Point(endPoint.x, startPoint.y);
		return points;
	}
	
	
}
