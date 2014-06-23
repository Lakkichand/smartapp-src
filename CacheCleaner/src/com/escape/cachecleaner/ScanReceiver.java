package com.escape.cachecleaner;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;

public class ScanReceiver extends BroadcastReceiver {

	private Context mContext;

	// 通知的ID
	public static final int ID = 10023;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		if (!MainActivity.sIsClosed) {
			return;
		}
		Log.e("", "获取应用缓存列表");
		// 获取应用缓存列表
		TAApplication.getApplication().doCommand("cachecleanercontroller",
				new TARequest(CacheCleanerController.CACHE_APP, null),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						List<CacheDataBean> apps = (List<CacheDataBean>) response
								.getData();
						long totalSize = 0;
						for (CacheDataBean bean : apps) {
							totalSize += bean.mCache;
						}
						if (totalSize > 100L * 1024L * 1024L) {
							NotificationManager notificationManager = (NotificationManager) mContext
									.getSystemService(Context.NOTIFICATION_SERVICE);
							Notification notification = new Notification();
							notification.icon = R.drawable.ic_launcher;
							notification.tickerText = mContext
									.getString(R.string.text1);
							notification.when = System.currentTimeMillis();

							notification.contentView = new RemoteViews(mContext
									.getPackageName(),
									R.layout.notification_apps);
							notification.contentView.setTextViewText(
									R.id.decription_state_tv,
									mContext.getString(R.string.text1));
							notification.contentView.setTextViewText(
									R.id.apps_state_tv,
									mContext.getString(R.string.text2));

							Intent intent = new Intent(mContext,
									MainActivity.class);
							PendingIntent pendingIntent = PendingIntent
									.getActivity(mContext, 0, intent,
											PendingIntent.FLAG_CANCEL_CURRENT);
							notification.contentIntent = pendingIntent;
							notification.flags |= Notification.FLAG_AUTO_CANCEL;
							notificationManager.notify(ID, notification);
						}
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFailure(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}
				}, true, false);
	}

}
