package com.jiubang.ggheart.apps.desks.diy;

import com.jiubang.ggheart.launcher.ICustomAction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBarReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ICustomAction.ACTION_OPEN_GGMENU)) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_MENU, 2,
					null, null);
		}
	}
}
