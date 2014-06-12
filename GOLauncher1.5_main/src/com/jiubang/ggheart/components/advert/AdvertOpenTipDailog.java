package com.jiubang.ggheart.components.advert;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-12-24]
 */
public class AdvertOpenTipDailog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final String packageName = getIntent().getStringExtra(AdvertConstants.ADVERT_PACK_NAME);
		if (packageName == null || packageName.equals("")) {
			AdvertOpenTipDailog.this.finish();
			return;
		}
		
		//是否在桌面上还是其他程序打开
		boolean isLauncherTop = Machine.isTopActivity(GOLauncherApp.getContext(),
				LauncherEnv.PACKAGE_NAME);
//		log3("isLauncherTop:" + isLauncherTop);
		//判断是否在屏幕层
		if (isLauncherTop) {
			final AppItemInfo appItemInfo = AdvertConstants.getAppName(this, packageName);	//获取包命对应的程序信息
			if (appItemInfo == null || appItemInfo.mTitle == null) {
				AdvertOpenTipDailog.this.finish();
				return;
			}
			String messageString = this.getResources().getString(
					R.string.advert_dialog_content, appItemInfo.mTitle);
			
			AdvertTipDialog dialog = new AdvertTipDialog(this);
			dialog.show();
			dialog.setTitle(R.string.notification_tip_title);
			if (appItemInfo.mIcon != null) {
				dialog.setImageView(appItemInfo.mIcon.getBitmap());
			}
			dialog.setMessage(messageString);
			dialog.setPositiveButton(R.string.ok, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setOpenCache(packageName, AdvertConstants.ADVERT_IS_OPENED); //设置缓存代表这个应用已经打开过
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.START_ACTIVITY, -1, appItemInfo.mIntent, null);
					AdvertOpenTipDailog.this.finish();
				}
			});
			dialog.setNegativeButton(R.string.cancel, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AdvertOpenTipDailog.this.finish();
				}
			});
			
			dialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					AdvertOpenTipDailog.this.finish();
				}
			});
		} else {
			AdvertOpenTipDailog.this.finish();
		}
	}
	
	/**
	 * <br>功能简述:设置8小时请求对应的缓存信息
	 * <br>功能详细描述:没有打开过的设置当前时间值，否则设置ture表示已经打开过
	 * <br>注意:
	 * @param packageName
	 * @param content
	 */
	public void setOpenCache(String packageName, String content) {
			PreferencesManager openPreferencesManager = new PreferencesManager(this,
					IPreferencesIds.ADVERT_NEET_OPEN_DATA, Context.MODE_WORLD_READABLE);
			openPreferencesManager.putString(packageName, content);
			openPreferencesManager.commit();
	}

}
