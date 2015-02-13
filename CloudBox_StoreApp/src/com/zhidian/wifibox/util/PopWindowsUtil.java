package com.zhidian.wifibox.util;

import android.content.Context;

/**
 * 每天第一次登录判断
 * 
 * @author zhaoyl
 * 
 */
public class PopWindowsUtil {

	public static boolean getFirstPop(Context context) {
		Setting setting = new Setting(context);
		long nowTime = getTodayTime();// 当前时间
		long lastTime = setting.getLong(Setting.TODAYTIME);// 上次弹窗时间
		if (nowTime - lastTime < (3L * 24L * 3600L * 1000L)) {// 表示3天内已显示过弹窗
			return false;
		} else {
			return true;
		}
	}

	public static long getTodayTime() {
//		Time t = new Time();
//		t.setToNow();
//		int lastmonth = t.month + 1;// 月份的值是从0~11
//		final String str = t.year + "年" + lastmonth + "月" + t.monthDay + "日";

		long nowTime = System.currentTimeMillis();
		
		
		return nowTime;

	}
}
