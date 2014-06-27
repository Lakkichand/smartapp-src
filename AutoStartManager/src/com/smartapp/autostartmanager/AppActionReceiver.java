package com.smartapp.autostartmanager;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.RemoteViews;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;

public class AppActionReceiver extends BroadcastReceiver {
	// 通知的ID
	public static final int ID = 270023;

	@Override
	public void onReceive(Context context, Intent intent) {
		final String packageName = intent.getData().getSchemeSpecificPart();
		if (context.getPackageName().equals(packageName)) {
			return;
		}
		// 检查应用自启动
		TAApplication.getApplication().doCommand("maincontroller",
				new TARequest(MainController.CHECK_APP, packageName),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						DataBean bean = (DataBean) response.getData();
						if (bean != null && !bean.mIsForbid) {
							// 保存到新应用列表
							SharedPreferences preferences = TAApplication
									.getApplication().getSharedPreferences(
											TAApplication.getApplication()
													.getPackageName(),
											Context.MODE_PRIVATE);
							String json = preferences.getString("newapps",
									(new JSONArray()).toString());
							JSONArray array = null;
							try {
								array = new JSONArray(json);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if (array == null) {
								array = new JSONArray();
							}
							array.put(bean.mInfo.packageName);
							Editor editor = preferences.edit();
							editor.putString("newapps", array.toString());
							editor.commit();
							// 从通知栏点进去才展示新应用
							if (array.length() == 1) {
								// TODO 提示某个应用自启动
								NotificationManager notificationManager = (NotificationManager) mContext
										.getSystemService(Context.NOTIFICATION_SERVICE);
								Notification notification = new Notification();
								notification.icon = R.drawable.ic_launcher;
								notification.tickerText = bean.mName
										+ TAApplication.getApplication()
												.getString(R.string.notitick);
								notification.when = System.currentTimeMillis();
								notification.contentView = new RemoteViews(
										mContext.getPackageName(),
										R.layout.notification_apps);
								notification.contentView.setTextViewText(
										R.id.decription_state_tv,
										mContext.getText(R.string.app_name));
								notification.contentView.setTextViewText(
										R.id.apps_state_tv,
										"你有 " + appList.size() + "个应用更新");

								Intent intent = new Intent(mContext,
										MainActivity.class);
								intent.putExtra(
										MainActivity.JUMP_TO_UPDATECONTAINER,
										true);
								PendingIntent pendingIntent = PendingIntent
										.getActivity(
												mContext,
												0,
												intent,
												PendingIntent.FLAG_CANCEL_CURRENT);
								notification.contentIntent = pendingIntent;
								notification.flags |= Notification.FLAG_AUTO_CANCEL;
								Setting setting = new Setting(mContext);
								long lasttime = setting
										.getLong(Setting.OPEN_APP_TIME);
								// 4.发送通知
								if (System.currentTimeMillis() - lasttime >= 3.0
										* 24 * 60 * 60 * 1000) {
									notificationManager
											.notify(ID, notification);
								}
							} else {
								// TODO 提示N个应用自启动
							}
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
