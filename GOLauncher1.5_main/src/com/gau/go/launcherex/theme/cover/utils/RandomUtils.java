package com.gau.go.launcherex.theme.cover.utils;

import java.util.Random;

/**
 * 
 * <br>类描述:随机取范围值
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-15]
 */
public class RandomUtils {

	private static final Random RANDOM = new Random();
	
	private static boolean equalZero(float x) {
		if (Math.abs(x) < 1E-2) {
			return true;
		}
		return false;
	}

	
	/**
	 * <br>功能简述: 防止 IllegalArgumentException 的随机范围取值
	 * <br>功能详细描述:
	 * <br>注意: max > min
	 * @param max
	 * @param min
	 * @return
	 */
	public static int randomInt(int max, int min) {
		return min + RANDOM.nextInt(max - min <= 0 ? 1 : max - min); 
	}
	
	/**
	 * @see randomInt(int max, int min)
	 * @param max
	 * @param min
	 * @return
	 */
	public static float randomFloat(float max, float min) {
		return min + (float) RANDOM.nextInt((int) ((equalZero(max - min) ? 0.1 : max - min) * 10))
				/ 10;
	}
	
	public static float getFloatRandom(float min, float max) {
		float ret = 0;
		while (ret <= min) {
			ret = (float) (RANDOM.nextInt((int) (max * 100)) / 100);
		}
		return ret;
	}
	
}
