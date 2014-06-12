package com.jiubang.ggheart.plugin.notification;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>������:
 * <br>������ϸ����:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class NotificationInvoke {
	public static void startSMSMonitor(Context context) {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_START_SMS_MONITOR);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopSMSMonitor(Context context) {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_STOP_SMS_MONITOR);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void startCallMonitor(Context context) {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_START_CALL_MONITOR);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopCallMonitor(Context context) {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_STOP_CALL_MONITOR);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void startGmailMonitor(Context context) {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_START_GMAIL_MONITOR);
			Bundle bundle = new Bundle();
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					LauncherEnv.GMAIL_ACCOUNT_SETTING, Context.MODE_PRIVATE);
			String name = sharedPreferences.getString(LauncherEnv.GMAIL_ACCOUNT_SETTING_ACCOUNT,
					null);
			String pwsd = sharedPreferences.getString(LauncherEnv.GMAIL_ACCOUNT_SETTING_PSWD, null);
			if (name != null && pwsd != null) {
				bundle.putString(LauncherEnv.GMAIL_ACCOUNT_SETTING_ACCOUNT, name);
				bundle.putString(LauncherEnv.GMAIL_ACCOUNT_SETTING_PSWD, pwsd);
				intent.putExtras(bundle);
			}
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopGmailMonitor(Context context) {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_STOP_GMAIL_MONITOR);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopAllMonitor(Context context) {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_STOP_ALL_MONITOR);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void startK9mailMonitor(Context context) {
		request(context, NotificationRequestType.NOTIFICATIONREQUESTTYPE_START_K9MAIL_MONITOR);
	}

	public static void stopK9mailMonitor(Context context) {
		request(context, NotificationRequestType.NOTIFICATIONREQUESTTYPE_STOP_K9MAIL_MONITOR);
	}

	public static void startFacebookMonitor(Context context) {
		request(context, NotificationRequestType.NOTIFICATIONREQUESTTYPE_START_FACEBOOK_MONITOR);
	}

	public static void stopFacebookMonitor(Context context) {
		request(context, NotificationRequestType.NOTIFICATIONREQUESTTYPE_STOP_FACEBOOK_MONITOR);
	}

	public static void startSinaWeiboMonitor(Context context) {
		request(context, NotificationRequestType.NOTIFICATIONREQUESTTYPE_START_SINAWEIBO_MONITOR);
	}

	public static void stopSinaWeiboMonitor(Context context) {
		request(context, NotificationRequestType.NOTIFICATIONREQUESTTYPE_STOP_SINAWEIBO_MONITOR);
	}

	private static void request(Context context, int requestType) {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_REQUEST);
			Bundle bundle = new Bundle();
			bundle.putInt("type", requestType);
			intent.putExtras(bundle);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述: 通知插件更新辅助功能监听列表
	 * <br>功能详细描述:通过广播通知
	 * <br>注意:
	 * @param context
	 * @param packageNames 需要监听的包名列表
	 */
	public static void startAccessibility(Context context, ArrayList<String> packageNames) {
		if (!packageNames.isEmpty()) {
			try {
				Intent intent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_REQUEST_APPLICATION);
				Bundle bundle = new Bundle();
				bundle.putStringArrayList("packagenames", packageNames);
				bundle.putString("launcher", context.getPackageName());
				intent.putExtras(bundle);
				context.sendBroadcast(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
