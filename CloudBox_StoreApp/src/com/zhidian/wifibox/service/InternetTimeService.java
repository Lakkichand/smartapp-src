package com.zhidian.wifibox.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.TimeTool;

/**
 * 上网时间计时服务
 * 
 * @author xiedezhi
 */
public class InternetTimeService extends Service {

	public static final String INTERNET_TIME_KEY = "INTERNET_TIME_KEY";

	public static final int ONGOING_NOTIFICATION_ID = 10014;

	private Notification mNotification = null;

	private boolean mDestory = false;
	/**
	 * 是否正在展示倒计时通知栏
	 */
	public static boolean sCounting;

	private MyCount mCount;

	/**
	 * 正在倒计时的WIFI名称
	 */
	private String mCurrentWIFI;
	/**
	 * 显示通知栏
	 */
	private boolean mShowNotification = false;

	/**
	 * 网络状态监听器
	 */
	private final BroadcastReceiver mNetWorkListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			sCounting = false;
			if (ModeManager.checkRapidly()
					&& InfoUtil.getCurWifiName(InternetTimeService.this)
							.equals(mCurrentWIFI)) {
				// 显示通知栏
				mShowNotification = true;
				if (mCount != null && mCount.isFinish()) {
					mNotification.icon = R.drawable.small_wai_wu;
					mNotification.when = System.currentTimeMillis();
					mNotification.contentView = new RemoteViews(
							getPackageName(), R.layout.timecountdown);
					mNotification.contentView.setImageViewResource(
							R.id.item_recommend_image, R.drawable.big_wai_wu);
					mNotification.contentView.setTextViewText(R.id.title,
							"目前无法连接网络");
					mNotification.contentView.setTextColor(R.id.title,
							Color.RED);
					mNotification.contentView.setTextViewText(R.id.message,
							"点击获取免费上网");
					mNotification.contentView.setViewVisibility(R.id.message,
							View.VISIBLE);
					mNotification.contentView.setViewVisibility(
							R.id.time_layout, View.GONE);

					intent = new Intent(InternetTimeService.this,
							MainActivity.class);
					intent.putExtra("time", 0);
					PendingIntent pendingIntent = PendingIntent.getActivity(
							InternetTimeService.this, 0, intent,
							PendingIntent.FLAG_UPDATE_CURRENT);
					mNotification.contentIntent = pendingIntent;
					startForeground(ONGOING_NOTIFICATION_ID, mNotification);
					sCounting = true;
				}
			} else {
				// 不显示通知栏
				mShowNotification = false;
				stopForeground(true);
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDestory = false;
		// 注册网络状态监听器
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mNetWorkListener, intentFilter);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		sCounting = false;
		if (mCount != null) {
			mCount.cancel();
		}
		if (intent == null) {
			return START_STICKY;
		}
		int timeout = intent.getIntExtra(INTERNET_TIME_KEY,
				InfoUtil.DEFAULT_INTERNET_TIME);
		if (timeout <= 0) {
			stopSelf();
			return START_STICKY;
		}
		mCurrentWIFI = InfoUtil.getCurWifiName(this);
		mShowNotification = true;
		mNotification = new Notification(R.drawable.small_wai,
				getText(R.string.app_name), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.putExtra("time", timeout);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mNotification.icon = R.drawable.small_wai;
		mNotification.when = System.currentTimeMillis();

		mNotification.contentView = new RemoteViews(getPackageName(),
				R.layout.timecountdown);

		mNotification.contentView.setImageViewResource(
				R.id.item_recommend_image, R.drawable.big_wai);
		mNotification.contentView.setTextViewText(R.id.title, "剩余免费上网时间");
		mNotification.contentView.setTextColor(R.id.title, Color.GRAY);

		mNotification.contentView.setViewVisibility(R.id.message, View.GONE);
		mNotification.contentView.setViewVisibility(R.id.time_layout,
				View.VISIBLE);
		long[] i = TimeTool.TimeForString(timeout);

		mNotification.contentView.setTextViewText(R.id.hour, i[0] + "");
		mNotification.contentView.setTextViewText(R.id.minute, i[1] + "");
		mNotification.contentView.setTextViewText(R.id.second, i[2] + "");

		mNotification.contentIntent = pendingIntent;
		startForeground(ONGOING_NOTIFICATION_ID, mNotification);
		sCounting = true;
		mCount = new MyCount(timeout * 1000, 1000);
		mCount.start();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sCounting = false;
		if (mCount != null) {
			mCount.cancel();
		}
		mDestory = true;
		// 反注册网络状态监听器
		unregisterReceiver(mNetWorkListener);
	}

	/**
	 * 倒计时
	 * 
	 * @author xiedezhi
	 * 
	 */
	private class MyCount extends CountDownTimer {

		private boolean finish = false;

		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		public boolean isFinish() {
			return finish;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			finish = false;
			sCounting = false;
			if (!mDestory && mNotification != null) {
				mNotification.icon = R.drawable.small_wai;
				mNotification.contentView = new RemoteViews(getPackageName(),
						R.layout.timecountdown);
				mNotification.contentView.setImageViewResource(
						R.id.item_recommend_image, R.drawable.big_wai);
				mNotification.contentView.setTextViewText(R.id.title,
						"剩余免费上网时间");
				mNotification.contentView.setTextColor(R.id.title, Color.GRAY);
				mNotification.contentView.setViewVisibility(R.id.message,
						View.GONE);
				mNotification.contentView.setViewVisibility(R.id.time_layout,
						View.VISIBLE);
				long[] i = TimeTool.TimeForString(millisUntilFinished);

				mNotification.contentView.setTextViewText(R.id.hour, i[0] + "");
				mNotification.contentView.setTextViewText(R.id.minute, i[1]
						+ "");
				mNotification.contentView.setTextViewText(R.id.second, i[2]
						+ "");

				Intent intent = new Intent(InternetTimeService.this,
						MainActivity.class);
				intent.putExtra("time", millisUntilFinished);
				PendingIntent pendingIntent = PendingIntent.getActivity(
						InternetTimeService.this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				mNotification.contentIntent = pendingIntent;
				if (mShowNotification) {
					startForeground(ONGOING_NOTIFICATION_ID, mNotification);
					sCounting = true;
				} else {
					stopForeground(true);
				}
			}
		}

		@Override
		public void onFinish() {
			finish = true;
			sCounting = false;
			if (!mDestory && mNotification != null) {
				mNotification.icon = R.drawable.small_wai_wu;
				mNotification.when = System.currentTimeMillis();
				mNotification.contentView = new RemoteViews(getPackageName(),
						R.layout.timecountdown);
				mNotification.contentView.setImageViewResource(
						R.id.item_recommend_image, R.drawable.big_wai_wu);
				mNotification.contentView.setTextViewText(R.id.title,
						"目前无法连接网络");
				mNotification.contentView.setTextColor(R.id.title, Color.RED);
				mNotification.contentView.setTextViewText(R.id.message,
						"点击获取免费上网");
				mNotification.contentView.setViewVisibility(R.id.message,
						View.VISIBLE);
				mNotification.contentView.setViewVisibility(R.id.time_layout,
						View.GONE);

				Intent intent = new Intent(InternetTimeService.this,
						MainActivity.class);
				intent.putExtra("time", 0);
				PendingIntent pendingIntent = PendingIntent.getActivity(
						InternetTimeService.this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				mNotification.contentIntent = pendingIntent;
				if (mShowNotification) {
					startForeground(ONGOING_NOTIFICATION_ID, mNotification);
				} else {
					stopForeground(true);
				}
			}
		}
	}

}
