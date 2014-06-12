package com.jiubang.ggheart.billing;

import android.content.Context;
import android.util.Log;

import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.billing.base.Consts;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 是否支持应用内付费
 * @author zhoujun
 *
 */
public class PurchaseSupportedManager {

	private static final String APP_IN_BILLING_SUPPORTED = "appinbilling";
	private static final String APP_IN_BILLING_SUPPORTED_KEY = "supported";

	/**
	 * 保存是否支持应用内付费，避免重复请求
	 * @param supported
	 */
	public static void saveSupported(Context context, boolean supported) {
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					APP_IN_BILLING_SUPPORTED, Context.MODE_WORLD_READABLE);
			sharedPreferences.putBoolean(APP_IN_BILLING_SUPPORTED_KEY, supported);
			sharedPreferences.commit();
		}
	}

	/**
	 * 获取当前是否支持应用内付费
	 * @return
	 */
	public static boolean checkBillingSupported(Context context) {
		if (context != null) {
			ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
			if (channelConfig != null) {
				//非200和210渠道的包，默认是不支持内购的，不需要启动内购的服务
				if (channelConfig.isNeedBillingService()) {
					PreferencesManager sharedPreferences = new PreferencesManager(context,
							APP_IN_BILLING_SUPPORTED, Context.MODE_WORLD_READABLE);
					boolean supported = sharedPreferences.getBoolean(APP_IN_BILLING_SUPPORTED_KEY,
							false);
					if (Consts.DEBUG) {
						Log.d(Consts.TAG, "ThemeAppInBillingManager mSupported:" + supported);
					}
					return supported;
				}
			}
		}
		return false;
	}
}
