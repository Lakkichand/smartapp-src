package com.gau.go.launcherex.theme.cover.utils;

import java.text.DecimalFormat;

/**
 * 
 * 类描述:浮点数位数截取工具类
 * 功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-15]
 */
public class DoubleFormatUtils {

	public static double roundDouble(double number, int digit) {
		StringBuilder builder = new StringBuilder(".");
		for (int i = 0; i < digit; i++) {
			builder.append("0");
		}
		DecimalFormat format = new DecimalFormat(builder.toString());
		String value;
		try {
			value = format.format(number);
			number = Double.parseDouble(value);
		} catch (NumberFormatException e) {
//			Log.e("DoubleFormatUtils", e.getMessage());
		}
		return number;
	}

	public static float roundFloat(float number, int digit) {
		return (float) roundDouble(number, digit);
	}

}
