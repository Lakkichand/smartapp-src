package com.zhidian.wifibox.receiver;

import java.util.ArrayList;

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
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.UpdateController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.UpdateAppBean;
import com.zhidian.wifibox.util.Setting;

public class ScanReceiver extends BroadcastReceiver {

	private Context mContext;

	// 通知的ID
	public static final int ID = 10023;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("", "ScanReceiver onReceive");
		mContext = context;
		Setting setting = new Setting(context);
		long lasttime = setting.getLong(Setting.OPEN_APP_TIME);
		if (System.currentTimeMillis() - lasttime >= 3.0 * 24 * 60 * 60 * 1000) {
			getUpdateAppData();
		}
	}

	/********************
	 * 获取更新数据
	 *******************/

	private void getUpdateAppData() {
		TAApplication.getApplication().doCommand(
				mContext.getString(R.string.updatecontroller),
				new TARequest(UpdateController.GAIN_UPDATE_NETWORK,
						CDataDownloader.getUpdateAppUrl()),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						PageDataBean bean = (PageDataBean) response.getData();
						ArrayList<UpdateAppBean> appList = (ArrayList<UpdateAppBean>) bean.uAppList;
						if (appList.size() > 0) {
							NotificationManager notificationManager = (NotificationManager) mContext
									.getSystemService(Context.NOTIFICATION_SERVICE);
							Notification notification = new Notification();
							notification.icon = R.drawable.icon;
							notification.tickerText = "你有 " + appList.size()
									+ "个应用更新 ";
							notification.when = System.currentTimeMillis();

							notification.contentView = new RemoteViews(mContext
									.getPackageName(),
									R.layout.notification_apps);
							notification.contentView.setTextViewText(
									R.id.decription_state_tv,
									mContext.getText(R.string.app_name));
							notification.contentView.setTextViewText(
									R.id.apps_state_tv, "你有 " + appList.size()
											+ "个应用更新");

							Intent intent = new Intent(mContext,
									MainActivity.class);
							intent.putExtra(
									MainActivity.JUMP_TO_UPDATECONTAINER, true);
							PendingIntent pendingIntent = PendingIntent
									.getActivity(mContext, 0, intent,
											PendingIntent.FLAG_CANCEL_CURRENT);
							notification.contentIntent = pendingIntent;
							notification.flags |= Notification.FLAG_AUTO_CANCEL;
							// 4.发送通知
							notificationManager.notify(ID, notification);

						}

					}

					@Override
					public void onStart() {

					}

					@Override
					public void onRuning(TAResponse response) {

					}

					@Override
					public void onFailure(TAResponse response) {
						PageDataBean bean = (PageDataBean) response.getData();
						Log.e("mytest",
								"nnnnnnnnnnn   ScanReceiver onFailure  nnnnnnnnn   "
										+ bean.mUrl);
					}

					@Override
					public void onFinish() {

					}
				}, true, false);

	}

}
