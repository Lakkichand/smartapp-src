package com.smartapp.escape_android.service;

import java.io.IOException;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.escape.local.http.server.IHttpServer;
import com.smartapp.easyproxyforandroid.R;
import com.smartapp.escape_android.MainActivity;

public class ProxyService extends Service {

	private static final int ONGOING_NOTIFICATION_ID = 12345;
	private IHttpServer mHttpServer;

	@Override
	public void onCreate() {
		Log.e("Test", "ProxyService onCreate");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("Test", "ProxyService onStartCommand");
		Notification notification = new Notification(R.drawable.ic_launcher,
				getText(R.string.ticker_text), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, getText(R.string.app_name),
				getText(R.string.notificationmsg), pendingIntent);
		startForeground(ONGOING_NOTIFICATION_ID, notification);

		try {
			mHttpServer = new IHttpServer();
			mHttpServer.start(18081);
			System.out.println("escape start");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return START_STICKY;

	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.e("Test", "ProxyService onBind");
		return null;
	}

	@Override
	public void onDestroy() {
		Log.e("Test", "ProxyService onDestroy");
		stopForeground(true);
		if (mHttpServer != null) {
			mHttpServer.shutdown();
		}
		super.onDestroy();
	}

	/**
	 * 本方法判断自己些的一个Service-->com.smartapp.escape_android.service.
	 * ProxyService是否已经运行
	 */
	public static boolean isWorked(Context context) {
		ActivityManager myManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(30);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service.getClassName().toString()
					.equals("com.smartapp.escape_android.service.ProxyService")) {
				return true;
			}
		}
		return false;
	}

}
