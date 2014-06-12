package com.jiubang.ggheart.analytic;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class InstallationUtils {
	private final static String PREFNAME = IPreferencesIds.PREFNAME;
	private final static String KEY_REFERRER_INFO_STORED_FLAG = IPreferencesIds.KEY_REFERRER_INFO_STORED_FLAG;

	public static boolean isRefInfoStored(Context context) {
		PreferencesManager manager = new PreferencesManager(context, PREFNAME, Context.MODE_PRIVATE);
		return manager.getBoolean(KEY_REFERRER_INFO_STORED_FLAG, false);
	}

	public static void refInfoStored(Context context) {
		PreferencesManager manager = new PreferencesManager(context, PREFNAME, Context.MODE_PRIVATE);
		manager.putBoolean(KEY_REFERRER_INFO_STORED_FLAG, true);
		manager.commit();
	}

	public static void needStoreRefInfo(Context context) {
		PreferencesManager manager = new PreferencesManager(context, PREFNAME, Context.MODE_PRIVATE);
		manager.putBoolean(KEY_REFERRER_INFO_STORED_FLAG, false);
		manager.commit();
	}
}
