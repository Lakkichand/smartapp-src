package com.jiubang.ggheart.plugin.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.PackageName;
/**
 * 
 * <br>类描述:通讯统计广播接收者
 * <br>功能详细描述:接收通讯统计包发出的消息
 * 
 * @author  wuziyi
 * @date  [2012-9-12]
 */
public class NotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (null == intent) {
			return;
		}
		NotificationControler controler = AppCore.getInstance().getNotificationControler();
		if (null == controler) {
			return;
		}

		String action = intent.getAction();
		if (null == action) {
			return;
		}
		// 通讯统计插件被删除
		if (action.equals(ICustomAction.ACTION_NOTIFICATIONACTION_DESTROY)) {
			//需要清hashmap，数据库等数据
			try {
				controler.clearAllNotificationAppItems();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		} else if (action.equals(ICustomAction.ACTION_NOTIFICATIONACTION_FACEBOOK_LOGIN_ERROR)) {
			String msg = intent.getStringExtra("msg");
			controler.closeFacebookMonitorFlag(msg);
		} else if (action.equals(ICustomAction.ACTION_NOTIFICATIONACTION_SINAWEIBO_LOGIN_ERROR)) {
			String msg = intent.getStringExtra("msg");
			controler.closeSinaWeiboMonitor(msg);
		}
		// start 兼容老版本 1.1
		else if (action.equals(ICustomAction.ACTION_NOTIFICATIONACTION_COUNT_UNREAD_SMS)) {
			int type = NotificationType.NOTIFICATIONTYPE_SMS;
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				try {
					int count = bundle.getInt("count");
					controler.updateNotification(type, count);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (action.equals(ICustomAction.ACTION_NOTIFICATIONACTION_COUNT_UNREAD_CALL)) {
			int type = NotificationType.NOTIFICATIONTYPE_CALL;
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				try {
					int count = bundle.getInt("count");
					controler.updateNotification(type, count);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (action.equals(ICustomAction.ACTION_NOTIFICATIONACTION_COUNT_UNREAD_GMAIL)) {
			int type = NotificationType.NOTIFICATIONTYPE_GMAIL;
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				try {
					int count = bundle.getInt("count");
					controler.updateNotification(type, count);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// end
		else if (action.equals(ICustomAction.ACTION_NOTIFICATIONACTION_RESPOND)) {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				try {
					int type = bundle.getInt("type");
					int count = bundle.getInt("count");
					controler.updateNotification(type, count);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		// 从其他桌面启动通讯统计后启动设置窗口
		} else if (action.equals(ICustomAction.ACTION_NOTIFICATION_SETTING_DETAIL)) {
			//打开设置界面
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SCREEN_OPEN_NOTIFICATION_SETTING, -1, null, null);
		// 更多应用的更新广播
		} else if (action.equals(ICustomAction.ACTION_NOTIFICATIONACTION_RESPOND_APPLICATION)) {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				try {
					CharSequence packageName = bundle.getCharSequence("application");
					int count = bundle.getInt("count");
					// Log.i("wuziyi", "接收到: " + packageName + " " + count);
					// google talk的特殊处理
					if (packageName.equals(PackageName.GOOGLE_TALK_ANDROID_GSF)) {
						packageName = PackageName.GOOGLE_TALK_ANDROID_TALK;
					}
					controler.updateNotificationHashMap(packageName.toString(), count);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		// 辅助功能被关闭
		} else if (action.equals(ICustomAction.ACTION_NOTIFICATION_STOP_ACCESSIBILITY)) {
			try {
				controler.clearAllNotificationAppItems();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}
}
