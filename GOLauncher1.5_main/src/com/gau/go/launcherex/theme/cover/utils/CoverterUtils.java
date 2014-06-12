package com.gau.go.launcherex.theme.cover.utils;

/**
 * 
 * <br>类描述:数据转换
 * 
 * @author  guoyiqing
 * @date  [2012-11-20]
 */
public class CoverterUtils {

	public static String stringToString(String value) {
		if (null == value) {
			return null;
		}
		if (value.length() == 0) {
			return null;
		}
		return value;
	}

	public static int stringToInt(String value) {
		if (null == value) {
			return 0;
		}
		if (value.length() == 0) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	public static float stringToFloat(String value) {
		if (null == value) {
			return 0;
		}
		if (value.length() == 0) {
			return 0;
		}
		return Float.parseFloat(value);
	}

	public static long stringToLong(String value) {
		if (null == value) {
			return 0;
		}
		if (value.length() == 0) {
			return 0;
		}
		return Long.parseLong(value);
	}

	public static boolean stringToBoolean(String value) {
		if (null == value) {
			return false;
		}
		if (value.equals("1") || value.equals("true") || value.equals("TRUE")) {
			return true;
		}
		return false;
	}

	
}
