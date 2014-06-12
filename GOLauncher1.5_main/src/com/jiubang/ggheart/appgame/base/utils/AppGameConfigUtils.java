package com.jiubang.ggheart.appgame.base.utils;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
/**
 * 
 * <br>类描述:应用游戏中心配置信息的工具类
 * <br>功能详细描述:
 * 
 * @author  wangzhuobin
 * @date  [2012-12-20]
 */
public class AppGameConfigUtils {
	/**
	 * <br>功能简述:根据SIM卡运营商的国家信息来设置Resources的Locale信息
	 * <br>功能详细描述:如果是运营商是中国，则设置为zh_CN,如果是韩国则设置为ko_KR,如果是其它的国家，则设置为en_US
	 * <br>注意:
	 * @param context
	 * @param resources
	 * @return
	 */
	private static final String TW = "466"; //台湾地区运营商代码前缀
	private static final String HK = "454"; //香港嗲去运营商代码前缀
	
	public static void updateResourcesLocaleBySim(Context context, Resources resources) {
		if (context == null || resources == null) {
			return;
		}
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telManager != null) {
			String simCountry = telManager.getSimCountryIso();
			String phoneType = null;
			if (telManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
				phoneType = telManager.getSimOperator();
			}
			if (!TextUtils.isEmpty(simCountry)) {
				DisplayMetrics metrics = resources.getDisplayMetrics();
				Configuration configuration = resources.getConfiguration();
				Locale locale = null;
				if (simCountry.equalsIgnoreCase(Locale.CHINA.getCountry())) {
					if (!TextUtils.isEmpty(phoneType) && (phoneType.startsWith(TW)
							|| phoneType.startsWith(HK))) {
						locale = Locale.TRADITIONAL_CHINESE;
					} else {
						locale = Locale.CHINA;
					}
				} else if (simCountry.equalsIgnoreCase(Locale.KOREA.getCountry())) {
					locale = Locale.KOREA;
				} else {
					locale = Locale.US;
				}
				configuration.locale = locale;
				resources.updateConfiguration(configuration, metrics);
			}
		}
	}
}
