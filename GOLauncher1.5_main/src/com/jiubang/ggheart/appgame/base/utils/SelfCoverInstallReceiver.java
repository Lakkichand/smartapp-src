package com.jiubang.ggheart.appgame.base.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:检测是否覆盖安装Go桌面
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2013-2-18]
 */
public class SelfCoverInstallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
				String packageName = intent.getDataString(); 
				if (packageName.equals(context.getPackageName())) {
					PreferencesManager sharedPreferences = new PreferencesManager(context,
							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
					sharedPreferences.putBoolean(LauncherEnv.ALREADY_PUT_GO_MARKET_ICON, false);
					sharedPreferences.commit();
				}
			}
		}
	}
}
