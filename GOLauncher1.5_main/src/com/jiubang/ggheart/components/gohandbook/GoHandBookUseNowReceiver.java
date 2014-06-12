package com.jiubang.ggheart.components.gohandbook;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>类描述:GO手册最后一页"马上试用"广播接收器
 * <br>功能详细描述:主要因为GO手册是放到一个独立进程里面。无法调用Go桌面的代码。所以只能通过GO手册进程发送广播，然后桌面广播接收器接收后进行"试用"跳转
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class GoHandBookUseNowReceiver extends BroadcastReceiver {
//	public static final String GO_HANDBOOK_USE_NOW_ACTION = "com.gau.go.launcherex.action.goHandBook.useNow";

	@Override
	public void onReceive(Context context, Intent intent) {
		int useNowType = intent.getIntExtra(GoHandBookConstants.GO_HANDBOOK_USE_NOW_TYPE, -1);
		switch (useNowType) {
			case GoHandBookConstants.BROWSE_PAGE_DESK :

				break;

			case GoHandBookConstants.BROWSE_PAGE_DOCK :
				break;

			case GoHandBookConstants.BROWSE_PAGE_FUNCTION :
				// 功能表
				executeIntent(ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON);
				break;

			case GoHandBookConstants.BROWSE_PAGE_FOLDER :
				break;

			case GoHandBookConstants.BROWSE_PAGE_WIDGET :
				break;

			case GoHandBookConstants.BROWSE_PAGE_GESTURE :
				break;

			case GoHandBookConstants.BROWSE_PAGE_CUSTOM :
				// 进入桌面预览状态
				//				executeIntent(ICustomAction.ACTION_SHOW_PREVIEW);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT, 1, BaseTab.TAB_THEMELOCKER,
						null);
				break;

			case GoHandBookConstants.BROWSE_PAGE_PERIPHERAL :
				// 打开菜单——周边
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_MENU, 2,
						null, null);
				break;

			case GoHandBookConstants.BROWSE_PAGE_MORE :
				// 桌面设置界面
				executeIntent(ICustomAction.ACTION_SHOW_PREFERENCES);
				break;

			default :
				break;
		}
	}

	/**
	 * 执行"马上应用"
	 * @param clsName 打开的类型名字
	 */
	public void executeIntent(String clsName) {
		Intent intent = new Intent(clsName);
		ComponentName cmpName = new ComponentName("com.gau.launcher.action", clsName);
		intent.setComponent(cmpName);
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.START_ACTIVITY, -1,
				intent, null);
	}

}
