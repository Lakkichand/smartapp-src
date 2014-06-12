package com.jiubang.ggheart.components.advert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

/**
 * 
 * <br>类描述:15屏广告图标8小时通知栏提示广播接收器
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-12-22]
 */
public class AdvertOpenAppReceive extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		Log.i("lch", "onReceive:" + intent);
		String packageName = intent.getStringExtra(AdvertConstants.ADVERT_PACK_NAME);
		if (packageName != null && !packageName.equals("")) {
			boolean isAppExist = AppUtils.isAppExist(context, packageName);
			// 如果该包名则打开该应用
			if (isAppExist) {
				try {
					PackageManager pm = context.getPackageManager();
					Intent openIntent = pm.getLaunchIntentForPackage(packageName);
					if (openIntent != null) {
						//判断桌面是否正在运行
						if (GoLauncher.getContext() != null) {
//							Log.i("lch3", "launcher open");
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.START_ACTIVITY, -1, openIntent, null);
						} else {
//							Log.i("lch3", "system open");
							AdvertControl.getAdvertControlInstance(context).setOpenCache(
									packageName, AdvertConstants.ADVERT_IS_OPENED); //设置缓存代表这个应用已经打开过,8小时后不提示
							context.startActivity(openIntent);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
