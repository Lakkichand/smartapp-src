package com.jiubang.ggheart.apps.desks.diy;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;

public class LockScreenHandler {
	public static void showLockScreenNotification(Context context) {
		// AppUtils.sendNotificationDisplaySeconds(context,
		// R.drawable.screen_lock,
		// context.getString(R.string.notification_screen_lock),
		// INotificationId.DISPLAY_SCREEN_LOCK);
		Toast.makeText(context, R.string.toast_screen_unlock, Toast.LENGTH_SHORT).show();
	}

	public static void showUnlockScreenNotification(Context context) {
		// AppUtils.sendNotificationDisplaySeconds(context,
		// R.drawable.screen_unlock,
		// context.getString(R.string.notification_screen_unlock),
		// INotificationId.DISPLAY_SCREEN_UNLOCK);
		Toast.makeText(context, R.string.notification_screen_unlock, Toast.LENGTH_SHORT).show();
	}

	public static void showUnlockScreenNotificationLong(Context context) {
		Intent it = new Intent(context, GoLauncher.class);
		it.putExtra("screenlocked", "screenlocked");
		AppUtils.sendNotification(context, it, R.drawable.screen_lock,
				context.getString(R.string.screen_lock_tutoria_title),
				context.getString(R.string.screen_lock_tutoria_title),
				context.getString(R.string.screen_lock_edit_tutoria),
				INotificationId.DISPLAY_SCREEN_UNLOCK);
	}
}
