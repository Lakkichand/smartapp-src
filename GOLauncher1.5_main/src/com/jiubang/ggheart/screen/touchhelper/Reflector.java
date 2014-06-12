package com.jiubang.ggheart.screen.touchhelper;

import java.lang.reflect.Method;

import android.app.Activity;

/**
 * 
 * <br>类描述:发射调用的工具类
 * 
 * @author  guoyiqing
 * @date  [2013-1-22]
 */
public class Reflector {

	private static final String TAG = "Refelector";
	private static final String OVERRIDE_PENDING_TRANSITION_METHOD = "overridePendingTransition";
	private static Method sMethodPendingTransition;

	static {
		try {
			sMethodPendingTransition = Activity.class.getMethod(OVERRIDE_PENDING_TRANSITION_METHOD,
					new Class[] { Integer.TYPE, Integer.TYPE });
		} catch (NoSuchMethodException e) {
			sMethodPendingTransition = null;
		}
	}

	public static void overridePendingTransition(Activity activity, int animEnter, int animExit) {
		if (sMethodPendingTransition != null) {
			try {
				sMethodPendingTransition.invoke(activity, animEnter, animExit);
			} catch (Exception e) {
			}
		}
	}

}
