/**
 * 
 */
package com.jiubang.ggheart.data.theme.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.go.util.device.Machine;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeDetailActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemePurchaseManager;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * @author ruxueqin
 * 
 */
public class MyThemeReceiver extends BroadcastReceiver {
	public static final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x00000020;
	public static final String PKGNAME_STRING = "pkgname";
	//	public static final String THEME_BROADCAST_STRING = "com.gau.go.launcherex.MyThemes.mythemeaction";
	public static final String ACTION_TYPE_STRING = "type";
	public static final int CHANGE_THEME = 1;
	public static final int GOTO_DETAIL = 2;
	//	public final static String ACTION_HIDE_THEME_ICON = "com.gau.go.launcherex.action.hide_theme_icon";
	/**
	 * 
	 */
	public MyThemeReceiver() {

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		System.out.println("MyThemeReceiver===" + action);
		if (ICustomAction.ACTION_THEME_BROADCAST.equals(action)) {
			try {
				int type = intent.getIntExtra(ACTION_TYPE_STRING, -1);
				switch (type) {
					case CHANGE_THEME :
						String pkgName = intent.getStringExtra(PKGNAME_STRING);
						applyTheme(context, pkgName);
						break;

					case IDiyMsgIds.REFRESH_SCREENICON_THEME :
						GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.REFRESH_SCREENICON_THEME,
								-1, null, null);
						break;
					case IDiyMsgIds.APPDRAWER_TAB_HOME_THEME_CHANGE :
						GoLauncher.sendBroadcastHandler(this,
								IDiyMsgIds.APPDRAWER_TAB_HOME_THEME_CHANGE, -1, null, null);
						break;
					case IDiyMsgIds.REFRESH_FOLDER_THEME :
						GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.REFRESH_FOLDER_THEME, -1,
								null, null);
						break;
					case IDiyMsgIds.REFRESH_SCREENINDICATOR_THEME :
						GoLauncher.sendBroadcastHandler(this,
								IDiyMsgIds.REFRESH_SCREENINDICATOR_THEME, -1, null, null);
						GoLauncher.sendBroadcastHandler(this,
								IDiyMsgIds.APPDRAWER_INDICATOR_THEME_CHANGE, -1, null, null);
						break;
					case IDiyMsgIds.REFRESH_GGMENU_THEME :
						GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.REFRESH_GGMENU_THEME, -1,
								null, null);
						break;
					case IDiyMsgIds.UPDATE_DOCK_BG :
						GOLauncherApp.getSettingControler().clearDockSettingInfo();
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.UPDATE_DOCK_BG, -1, null, null);
						break;
					case IDiyMsgIds.DOCK_SETTING_CHANGED_STYLE :
						GOLauncherApp.getSettingControler().clearDockSettingInfo();
						GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.DOCK_SETTING_CHANGED_STYLE, -1, ThemeManager
										.getInstance(context).getCurThemePackage(), null);
						break;
					case FunAppSetting.INDEX_BGSWITCH :
						GOLauncherApp.getSettingControler().getFunAppSetting()
								.resetFuncBgObserver();
						break;
					case GOTO_DETAIL :
						pkgName = intent.getStringExtra(PKGNAME_STRING);
						Intent it = new Intent(context, ThemeDetailActivity.class);
						it.putExtra(ThemeConstants.PACKAGE_NAME_EXTRA_KEY, pkgName);
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(it);
					default :
						break;
				}
			} catch (Exception e) {

			}
		} else if (ICustomAction.ACTION_THEME_BROADCAST_SCREENEDIT // 添加界面主题设置广播
				.equals(action)) {
			try {
				int type = intent.getIntExtra(ACTION_TYPE_STRING, -1);
				switch (type) {
					case CHANGE_THEME :
						String pkgName = intent.getStringExtra(PKGNAME_STRING);
						applyTheme(context, pkgName);
						break;
					default :
						break;
				}
			} catch (Exception e) {

			}
		}

	}

	/**
	 * 应用主题
	 * */
	private void applyTheme(Context context, String pkgName) {
		if (null == GoLauncher.getContext()
				|| null == ThemeManager.getInstance(GoLauncher.getContext())) {
			return;
		}

		ThemeManager tmg = ThemeManager.getInstance(GoLauncher.getContext());
		String curThemePackage = tmg.getCurThemePackage();
		if (pkgName != null && pkgName.equals(curThemePackage)) {
			// 如果当前已经应用该主题，则提示
			// Toast.makeText(GoLauncher.getContext(),R.string.theme_already_using,
			// Toast.LENGTH_SHORT).show();
		} else if (!ThemeManager.isInstalledTheme(GoLauncher.getContext(), pkgName)) {
			// new add
			Toast.makeText(GoLauncher.getContext(), "Theme is not installed on your phone",
					Toast.LENGTH_SHORT).show();
		} else {
			tmg.applyThemePackage(pkgName, true);
			Intent intent = new Intent(ICustomAction.ACTION_HIDE_THEME_ICON);
			int level = ThemePurchaseManager.getCustomerLevel(context);
			intent.putExtra("viplevel", level);
			intent.putExtra(PKGNAME_STRING, pkgName);
			if (Machine.IS_HONEYCOMB_MR1) {
				// 3.1之后，系统的package manager增加了对处于“stopped state”应用的管理
				intent.setFlags(FLAG_INCLUDE_STOPPED_PACKAGES);
			}
			context.sendBroadcast(intent);
		}
	}
}
