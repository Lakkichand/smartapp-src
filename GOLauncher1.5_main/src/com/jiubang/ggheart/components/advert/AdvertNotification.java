package com.jiubang.ggheart.components.advert;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.RemoteViews;

import com.gau.go.launcherex.R;

/**
 * 
 * <br>类描述:15屏广告图标8小时通知栏
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-12-22]
 */
public class AdvertNotification {

	public static void showNotification(Context context, String appName, String packageName,
			BitmapDrawable drawable, int notificationId) {

		String titleString = context.getResources().getString(R.string.advert_dialog_message,
				appName);
		Bitmap bitmap = drawable.getBitmap();

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.advert_open_tip,
				titleString, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL; // 点击后自动消失

		notification.contentView = new RemoteViews(context.getPackageName(),
				R.layout.msg_center_noitify_content);
		notification.contentView.setTextViewText(R.id.theme_title, titleString);
		
		String contentString = context.getResources().getString(R.string.advert_dialog_message_content);
		notification.contentView.setTextViewText(R.id.theme_content, contentString);
		if (bitmap != null) {
			notification.contentView.setImageViewBitmap(R.id.theme_view_image, bitmap);
		}

		Intent intent = new Intent("open_app_broadcast");
		intent.putExtra(AdvertConstants.ADVERT_PACK_NAME, packageName);
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, notificationId, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = contentIntent;
		notificationManager.notify(notificationId, notification);
	}
}
