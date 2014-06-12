package com.gau.go.launcherex.theme.cover.utils;

import android.graphics.Point;

/**
 * 
 * <br>类描述:计算角度
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-16]
 */
public class AngleAndPointUtils {

	/**
	 * <br>功能简述: 计算图像在此时速度的偏移方向
	 * <br>注意:图默认角度正向
	 * @param speedX
	 * @param speedY
	 * @param defaultAngle
	 * @return
	 */
	public static int angleOnSpeed(float speedX, float speedY, int defaultAngle) {
		int angle = 0;
		if (speedY != 0) {
			double tan = Math.atan((float) Math.abs(speedX) / Math.abs(speedY));
			tan = Math.toDegrees(tan);
			if (speedX > 0) {
				if (speedY > 0) {
					angle = (int) (180 - tan) - defaultAngle;
				} else {
					angle = (int) tan - defaultAngle;
				}
			} else {
				if (speedY > 0) {
					angle = (int) (tan - 180) - defaultAngle;
				} else {
					angle = (int) -tan - defaultAngle;
				}
			}
		} else {
			if (speedX > 0) {
				angle = 90 - defaultAngle;
			} else {
				angle = -90 - defaultAngle;
			}
		}
		return angle;
	}

	/**
	 * <br>功能简述:求在平均角度，避免偏移太大
	 * @return
	 */
	public static int angleOnAverage(int x1, int y1, int x2, int y2, int x3, int y3/*, int x4, int y4*/) {
		int angle1 = angleOnSpeed(x2 - x1, y2 - y1, 0);
		int angle2 = angleOnSpeed(x3 - x2, y3 - y2, 0);
//		int angle3 = angleOnSpeed(x4 - x3, y4 - y3, 0);
		return (angle1 + angle2) / 2;
	}
	
	public static boolean isLongerThan(Point startPoint, Point endPoint, int distance) {
		int tempx = startPoint.x - endPoint.x;
		int tempy = startPoint.y - endPoint.y;
		if (Math.sqrt(tempx * tempx + tempy * tempy) > distance) {
			return true;
		}
		return false;
	}

}
