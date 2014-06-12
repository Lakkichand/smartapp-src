package com.jiubang.ggheart.plugin.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

import com.go.util.AppUtils;
import com.go.util.SortUtils;
import com.jiubang.ggheart.apps.desks.diy.AppInvoker.IAppInvokerListener;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
/**
 * 
 * <br>类描述:通讯统计控制器
 * <br>功能详细描述:控制未读的增删改查
 * 
 * @author  wuziyi
 * @date  [2012-9-11]
 */
public class NotificationControler implements ISelfObject, IAppInvokerListener {
	private NotificationReceiver mNotificationReceiver;

	private int mUnreadSMSCount = 0;
	private int mUnreadCallCount = 0;
	private int mUnreadGmailCount = 0;
	private int mUnreadK9mailCount = 0;
	private int mUnreadFacebookCount = 0;
	private int mUnreadSinaWeiboCount = 0;

	private boolean mMessageNotificationStarted = false;
	private boolean mCallNotificationStarted = false;
	private boolean mGmailNotificationStarted = false;
	private boolean mK9mailNotificationStarted = false;
	private boolean mFacebookNotificationStarted = false;
	private boolean mSinaWeiboNotificationStarted = false;

	private Context mContext;

	private AppDataEngine mAppDataEngine;
	private NotificationSettingDateModel mNotificationSettingDataModel;
	private HashMap<String, AppItemInfo> mNotificationHashMap; // 已经被勾选监听的
	private boolean mIsStartAccessibility;

	private boolean mIsFirstStart;
	private static final String START_NOTIFICATION_THREAD_NAME = "start_notification_thread_name";

	// public NotificationControler(Context context)
	// {
	// mContext = context;
	// selfConstruct();
	// }

	public NotificationControler(Context context, AppDataEngine appDataEngine) {
		mAppDataEngine = appDataEngine;
		mNotificationSettingDataModel = new NotificationSettingDateModel(context);
		mContext = context;
		// initNotificationHashMap();
		selfConstruct();
	}

	@Override
	public void selfConstruct() {
		registerNotification();
		startNotification();
	}

	@Override
	public void selfDestruct() {
		stopNotification();
		unregisterNotification();
	}

	private void registerNotification() {
		if (null == mNotificationReceiver) {
			IntentFilter filter = new IntentFilter();
			// start 兼容老版本 1.1
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_COUNT_UNREAD_SMS);
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_COUNT_UNREAD_CALL);
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_COUNT_UNREAD_GMAIL);
			// end
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_RESPOND);
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_DESTROY);
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_FACEBOOK_LOGIN_ERROR);
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_SINAWEIBO_LOGIN_ERROR);
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_GMAIL_LOGIN_ERROR);
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_GMAIL_MONITOR_CANCLED);

			filter.addAction(ICustomAction.ACTION_NOTIFICATION_SETTING_DETAIL);
			filter.addAction(ICustomAction.ACTION_NOTIFICATIONACTION_RESPOND_APPLICATION);
			filter.addAction(ICustomAction.ACTION_NOTIFICATION_STOP_ACCESSIBILITY);

			mNotificationReceiver = new NotificationReceiver();

			try {
				mContext.registerReceiver(mNotificationReceiver, filter);
			} catch (Exception e) {
				// 注册异常
				e.printStackTrace();
			}
		}
	}

	private void unregisterNotification() {
		if (null != mNotificationReceiver) {
			try {
				mContext.unregisterReceiver(mNotificationReceiver);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mNotificationReceiver = null;
		}
	}

	private void startNotification() {
		if (ShortCutSettingInfo.mAutoMessageStatistic) {
			startSMSMonitor();
		} else {
			stopSMSMonitor();
		}
		if (ShortCutSettingInfo.mAutoMisscallStatistic) {
			startCallMonitor();
		} else {
			stopCallMonitor();
		}
		if (ShortCutSettingInfo.mAutoMissmailStatistic) {
			startGmailMonitor();
		} else {
			stopGmailMonitor();
		}
		if (ShortCutSettingInfo.mAutoMissk9mailStatistic) {
			startK9mailMonitor();
		} else {
			stopK9mailMonitor();
		}
		if (ShortCutSettingInfo.mAutoMissfacebookStatistic) {
			startFacebookMonitor();
		} else {
			stopFacebookMonitor();
		}
		if (ShortCutSettingInfo.mAutoMissSinaWeiboStatistic) {
			startSinaWeiboMonitor();
		} else {
			stopSinaWeiboMonitor();
		}
	}

	private void stopNotification() {
		// if (ShortCutSettingInfo.mAutoMessageStatistic)
		{
			stopSMSMonitor();
		}

		// if (ShortCutSettingInfo.mAutoMisscallStatistic)
		{
			stopCallMonitor();
		}

		// if (ShortCutSettingInfo.mAutoMissmailStatistic)
		{
			stopGmailMonitor();
		}

		// if (ShortCutSettingInfo.mAutoMissk9mailStatistic)
		{
			stopK9mailMonitor();
		}

		// if (ShortCutSettingInfo.mAutoMissfacebookStatistic)
		{
			stopFacebookMonitor();
		}
	}

	@SuppressWarnings("unchecked")
	public void handleLauncherEvent(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.EVENT_UNINSTALL_APP :
				doUninstallApps((ArrayList<AppItemInfo>) objects);
				break;

			case IDiyMsgIds.EVENT_UNINSTALL_PACKAGE :
				if (object instanceof String) {
					doUninstall((String) object);
				}
				break;

			case IDiyMsgIds.EVENT_UPDATE_PACKAGE :
				//(升级or降级)TODO：//Gmail版本的特殊处理
				//				Log.i("wuziyi", "升级or降级?");
				String packageName = (String) object;
				if (packageName.equals(PackageName.GMAIL)
						&& AppUtils.canReadGmailLabels(mContext)) {
					Intent intent = new Intent("appUpdate");
					intent.putExtra("updatePackageName", packageName);
					mContext.sendBroadcast(intent);
				}
				break;

			default :
				break;
		}
	}

	private void doUninstallApps(final ArrayList<AppItemInfo> appItemInfos) {
		if (null == appItemInfos) {
			return;
		}

		// 批量删除
		AppItemInfo appItemInfo = null;
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			appItemInfo = appItemInfos.get(i);
			if (appItemInfo != null && appItemInfo.mIntent != null) {
				final ComponentName cn = appItemInfo.mIntent.getComponent();
				if (cn != null) {
					doUninstall(cn.getPackageName());
				}
			}
		}
	}

	private void doUninstall(String packageName) {
		if (packageName == null) {
			return;
		}

		if (packageName.contains(LauncherEnv.Plugin.NOTIFICATION_PACKAGE_NAME)) {
			// stopNotification();

			ShortCutSettingInfo info = getShortCutSettingInfo();

			if (ShortCutSettingInfo.mAutoMessageStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_SMS, 0);
			}

			if (ShortCutSettingInfo.mAutoMisscallStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_CALL, 0);
			}

			if (ShortCutSettingInfo.mAutoMissmailStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_GMAIL, 0);
			}

			if (ShortCutSettingInfo.mAutoMissk9mailStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_K9MAIL, 0);
			}

			if (ShortCutSettingInfo.mAutoMissfacebookStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_FACEBOOK, 0);
			}

			if (ShortCutSettingInfo.mAutoMissSinaWeiboStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_SinaWeibo, 0);
			}
			if (mIsStartAccessibility) {
				// 暂时用于更新文件夹
				updateNotification(NotificationType.NOTIFICATIONTYPE_MORE_APP, 0);
				try {
					clearAllNotificationAppItems();
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}

			ShortCutSettingInfo.mAutoMessageStatistic = false;
			ShortCutSettingInfo.mAutoMisscallStatistic = false;
			ShortCutSettingInfo.mAutoMissmailStatistic = false;
			ShortCutSettingInfo.mAutoMissk9mailStatistic = false;
			ShortCutSettingInfo.mAutoMissfacebookStatistic = false;
			ShortCutSettingInfo.mAutoMissSinaWeiboStatistic = false;

			updateShortCutSettingInfo(info);
		} else if (packageName.equals(PackageName.K9MAIL)) // K9
		{
			ShortCutSettingInfo info = getShortCutSettingInfo();
			if (ShortCutSettingInfo.mAutoMissk9mailStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_K9MAIL, 0);
			}
			ShortCutSettingInfo.mAutoMissk9mailStatistic = false;
			updateShortCutSettingInfo(info);
		} else if (packageName.equals(PackageName.GMAIL)) // gm
		{
			ShortCutSettingInfo info = getShortCutSettingInfo();
			if (ShortCutSettingInfo.mAutoMissmailStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_GMAIL, 0);
			}
			ShortCutSettingInfo.mAutoMissmailStatistic = false;
			updateShortCutSettingInfo(info);
		} else if (packageName.equals(PackageName.FACEBOOK)) // facebook
		{
			ShortCutSettingInfo info = getShortCutSettingInfo();
			if (ShortCutSettingInfo.mAutoMissfacebookStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_FACEBOOK, 0);
			}
			ShortCutSettingInfo.mAutoMissfacebookStatistic = false;
			updateShortCutSettingInfo(info);
		} else if (packageName.equals(PackageName.SINA_WEIBO)) {
			ShortCutSettingInfo info = getShortCutSettingInfo();
			if (ShortCutSettingInfo.mAutoMissSinaWeiboStatistic) {
				updateNotification(NotificationType.NOTIFICATIONTYPE_SinaWeibo, 0);
			}
			ShortCutSettingInfo.mAutoMissSinaWeiboStatistic = false;
			updateShortCutSettingInfo(info);
		} else if (mNotificationHashMap != null) {
			if (mNotificationHashMap.containsKey(packageName)) {
				Intent intent = mNotificationHashMap.get(packageName).mIntent;
				try {
					delNotificationAppItem(intent, true);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				Intent resetIntent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_RESET_APPLICATION);
				resetIntent.putExtra("resetpackagename", packageName);
				mContext.sendBroadcast(resetIntent);
			}
		}
	}

	/**
	 * <br>功能简述:关闭facebook的勾选标志
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param msg
	 */
	public void closeFacebookMonitorFlag(String msg) {
		ShortCutSettingInfo info = getShortCutSettingInfo();
		ShortCutSettingInfo.setAutoMissfacebookStatistic(false);
		updateShortCutSettingInfo(info);
		NotificationInvoke.stopFacebookMonitor(mContext);
		GoLauncher.sendMessage(this, IDiyFrameIds.NOTIFICATION_FRAME,
				IDiyMsgIds.FACEBOOK_LOGIN_FAIL, -1, null, null);

		if (null != msg) {
			DeskToast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * <br>功能简述:关闭微博的勾选标志
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param msg
	 */
	public void closeSinaWeiboMonitor(String msg) {
		ShortCutSettingInfo info = getShortCutSettingInfo();
		ShortCutSettingInfo.setAutoMissSinaWeiboStatistic(false);
		updateShortCutSettingInfo(info);
		NotificationInvoke.stopSinaWeiboMonitor(mContext);
		GoLauncher.sendMessage(this, IDiyFrameIds.NOTIFICATION_FRAME,
				IDiyMsgIds.SINA_WEIBO_LOGIN_FAIL, -1, null, null);

		if (null != msg) {
			DeskToast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * <br>功能简述:更新原有6应用的未读数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param type
	 * @param count
	 */
	public void updateNotification(int type, int count) {
		boolean isChanged = false;
		switch (type) {
			case NotificationType.NOTIFICATIONTYPE_SMS :
				if (count != mUnreadSMSCount) {
					mUnreadSMSCount = count;
					isChanged = true;
				}
				break;

			case NotificationType.NOTIFICATIONTYPE_CALL :
				if (count != mUnreadCallCount) {
					mUnreadCallCount = count;
					isChanged = true;
				}
				break;

			case NotificationType.NOTIFICATIONTYPE_GMAIL :
				if (count != mUnreadGmailCount) {
					mUnreadGmailCount = count;
					isChanged = true;
				}
				break;

			case NotificationType.NOTIFICATIONTYPE_K9MAIL :
				if (count != mUnreadK9mailCount) {
					mUnreadK9mailCount = count;
					isChanged = true;
				}
				break;

			case NotificationType.NOTIFICATIONTYPE_FACEBOOK :
				if (count != mUnreadFacebookCount) {
					mUnreadFacebookCount = count;
					isChanged = true;
				}
				break;

			case NotificationType.NOTIFICATIONTYPE_SinaWeibo :
				if (count != mUnreadSinaWeiboCount) {
					mUnreadSinaWeiboCount = count;
					isChanged = true;
				}
				break;

			default :
				break;
		}
		if (isChanged) {
			Integer integer = Integer.valueOf(count);
			GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.NOTIFICATION_CHANGED, type, integer,
					null);
		}
	}

	/**
	 * <br>功能简述:获得对应程序的未读
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param type
	 * @return
	 */
	public int getNotification(int type) {
		switch (type) {
			case NotificationType.NOTIFICATIONTYPE_SMS :
				return mUnreadSMSCount;

			case NotificationType.NOTIFICATIONTYPE_CALL :
				return mUnreadCallCount;

			case NotificationType.NOTIFICATIONTYPE_GMAIL :
				return mUnreadGmailCount;

			case NotificationType.NOTIFICATIONTYPE_K9MAIL :
				return mUnreadK9mailCount;

			case NotificationType.NOTIFICATIONTYPE_FACEBOOK :
				return mUnreadFacebookCount;

			case NotificationType.NOTIFICATIONTYPE_SinaWeibo :
				return mUnreadSinaWeiboCount;

			default :
				return 0;
		}
	}

	/**
	 * 获得folder中通讯应用未读总和
	 * 
	 * @param folderInfo
	 *            桌面文件夹info
	 * @return
	 */
	public int getTotalUnreadCount(UserFolderInfo folderInfo) {
		if (null == folderInfo) {
			return 0;
		}
		int total = 0;
		for (int i = 0; i < folderInfo.getChildCount(); i++) {
			ShortCutInfo shortCutInfo = folderInfo.getChildInfo(i);
			int appType = AppIdentifier.getNotificationType(mContext, shortCutInfo);
			if (appType != NotificationType.IS_NOT_NOTIFICSTION) {
				int unreadCount = 0;
				if (appType == NotificationType.NOTIFICATIONTYPE_MORE_APP) {
					unreadCount = shortCutInfo.getRelativeItemInfo().getUnreadCount();
				} else {
					unreadCount = getNotification(appType);
				}
				total += unreadCount;
			}
		}
		return total;
	}

	/**
	 * 获得folder中通讯应用未读总和
	 * 
	 * @param folderInfo
	 *            功能表文件夹info
	 * @return
	 */
	public int getTotalUnreadCount(FunFolderItemInfo folderInfo) {
		if (null == folderInfo) {
			return 0;
		}
		int total = 0;
		ArrayList<FunAppItemInfo> infos = folderInfo.getFunAppItemInfos();
		if (null == infos) {
			return 0;
		}
		for (FunAppItemInfo info : infos) {
			int type = info.getNotificationType();
			// int type = AppIdentifier.whichTypeOfNotification(mContext,
			// info.getIntent());
			if (type != NotificationType.IS_NOT_NOTIFICSTION) {
				int unreadCount = 0;
				if (type == NotificationType.NOTIFICATIONTYPE_MORE_APP) {
					unreadCount = info.getAppItemInfo().getUnreadCount();
				} else {
					unreadCount = getNotification(type);
				}
				total += unreadCount;
			}
		}
		return total;
	}

	/**
	 * <br>功能简述:重新获取一次未读数
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void checkNotification() {
		isAccessibilityStart();
		if (ShortCutSettingInfo.mAutoMessageStatistic) {
			startSMSMonitor();
		} else {
			stopSMSMonitor();
		}
		if (ShortCutSettingInfo.mAutoMisscallStatistic) {
			startCallMonitor();
		} else {
			stopCallMonitor();
		}
		if (ShortCutSettingInfo.mAutoMissmailStatistic) {
			startGmailMonitor();
		} else {
			stopGmailMonitor();
		}
		if (ShortCutSettingInfo.mAutoMissk9mailStatistic) {
			startK9mailMonitor();
		} else {
			stopK9mailMonitor();
		}
		if (ShortCutSettingInfo.mAutoMissfacebookStatistic) {
			startFacebookMonitor();
		} else {
			stopFacebookMonitor();
		}
		if (ShortCutSettingInfo.mAutoMissSinaWeiboStatistic) {
			startSinaWeiboMonitor();
		} else {
			stopSinaWeiboMonitor();
		}
		if (mIsStartAccessibility) {
			ArrayList<Intent> intentList = mNotificationSettingDataModel.getNotificationAppItems();
			ArrayList<String> packageNames = new ArrayList<String>();
			for (int i = 0; i < intentList.size(); i++) {
				String packageName = intentList.get(i).getComponent().getPackageName();
				if (packageName.equals(PackageName.GOOGLE_TALK_ANDROID_TALK)) {
					packageName = PackageName.GOOGLE_TALK_ANDROID_GSF;
				}
				packageNames.add(packageName);
			}
			NotificationInvoke.startAccessibility(mContext, packageNames);
		}
	}
	
	public void resetFlags() {
		mMessageNotificationStarted = false;
		mCallNotificationStarted = false;
		mGmailNotificationStarted = false;
		mK9mailNotificationStarted = false;
		mFacebookNotificationStarted = false;
		mSinaWeiboNotificationStarted = false;
	}

	public int getUnreadSMSCount() {
		return mUnreadSMSCount;
	}

	public int getUnreadCallCount() {
		return mUnreadCallCount;
	}

	public int getUnreadGmailCount() {
		return mUnreadGmailCount;
	}

	public int getUnreadK9mailCount() {
		return mUnreadK9mailCount;
	}

	public int getUnreadFacebookCount() {
		return mUnreadFacebookCount;
	}

	public int getUnreadSinaWeiboCount() {
		return mUnreadSinaWeiboCount;
	}

	/**
	 * <br>功能简述:启动通行统计服务
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void startNotifyServer() {
		try {
			new Thread(START_NOTIFICATION_THREAD_NAME) {
				@Override
				public void run() {
					Intent it = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_START_SERVICE);
					mContext.startService(it);
					super.run();
				}
			}.start();
		} catch (Exception e) {
		}
	}

	public void startSMSMonitor() {
		mMessageNotificationStarted = true;
		NotificationInvoke.startSMSMonitor(mContext);
	}

	public void stopSMSMonitor() {
		mMessageNotificationStarted = false;
		NotificationInvoke.stopSMSMonitor(mContext);

		updateNotification(NotificationType.NOTIFICATIONTYPE_SMS, 0);
	}

	public void startCallMonitor() {
		mCallNotificationStarted = true;
		NotificationInvoke.startCallMonitor(mContext);
	}

	public void stopCallMonitor() {
		mCallNotificationStarted = false;
		NotificationInvoke.stopCallMonitor(mContext);

		updateNotification(NotificationType.NOTIFICATIONTYPE_CALL, 0);
	}

	public void startGmailMonitor() {
		mGmailNotificationStarted = true;
		NotificationInvoke.startGmailMonitor(mContext);
	}

	public void stopGmailMonitor() {
		mGmailNotificationStarted = false;
		NotificationInvoke.stopGmailMonitor(mContext);

		updateNotification(NotificationType.NOTIFICATIONTYPE_GMAIL, 0);
	}

	public void startK9mailMonitor() {
		mK9mailNotificationStarted = true;
		NotificationInvoke.startK9mailMonitor(mContext);
	}

	public void stopK9mailMonitor() {
		mK9mailNotificationStarted = false;
		NotificationInvoke.stopK9mailMonitor(mContext);

		updateNotification(NotificationType.NOTIFICATIONTYPE_K9MAIL, 0);
	}

	public void startFacebookMonitor() {
		mFacebookNotificationStarted = true;
		NotificationInvoke.startFacebookMonitor(mContext);
	}

	public void stopFacebookMonitor() {
		mFacebookNotificationStarted = false;
		NotificationInvoke.stopFacebookMonitor(mContext);

		updateNotification(NotificationType.NOTIFICATIONTYPE_FACEBOOK, 0);
	}

	public void startSinaWeiboMonitor() {
		mSinaWeiboNotificationStarted = true;
		NotificationInvoke.startSinaWeiboMonitor(mContext);
	}

	public void stopSinaWeiboMonitor() {
		mSinaWeiboNotificationStarted = false;
		NotificationInvoke.stopSinaWeiboMonitor(mContext);

		updateNotification(NotificationType.NOTIFICATIONTYPE_SinaWeibo, 0);
	}

	private ShortCutSettingInfo getShortCutSettingInfo() {
		return GOLauncherApp.getSettingControler().getShortCutSettingInfo();
	}

	private void updateShortCutSettingInfo(ShortCutSettingInfo info) {
		GOLauncherApp.getSettingControler().updateShortCutSetting_NonIndepenceTheme(info);
	}

	/**
	 * 初始化已勾选的通讯统计程序数据
	 */
	public void initNotificationHashMap() {
		if (mNotificationHashMap != null) {
			mNotificationHashMap.clear();
		} else {
			mNotificationHashMap = new HashMap<String, AppItemInfo>();
		}
		ArrayList<Intent> intentList = mNotificationSettingDataModel.getNotificationAppItems();
		for (int i = 0; i < intentList.size(); i++) {
			Intent intent = intentList.get(i);
			if (AppUtils.isAppExist(mContext, intent)) {
				AppItemInfo appItemInfo = mAppDataEngine.getCompletedAppItem(intent);
				if (appItemInfo == null) {
					continue;
				}
				appItemInfo.setNotificationType(NotificationType.NOTIFICATIONTYPE_MORE_APP);
				ComponentName cn = intent.getComponent();
				if (cn != null) {
					String packageName = cn.getPackageName();
					mNotificationHashMap.put(packageName, appItemInfo);
				}
			} else {
				//某些情况下，卸载的时候，桌面已死，数据库的数据未能及时清空
				try {
					delNotificationAppItem(intent, true);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取所有已勾选的通讯统计程序
	 */
	public ArrayList<AppItemInfo> getNotificationAppItems() {
		Iterator<Entry<String, AppItemInfo>> it = mNotificationHashMap.entrySet().iterator();
		ArrayList<AppItemInfo> appItemList = new ArrayList<AppItemInfo>();
		while (it.hasNext()) {
			Entry<String, AppItemInfo> entry = it.next();
			AppItemInfo appItemInfo = entry.getValue();
			appItemList.add(appItemInfo);
		}
		return appItemList;
	}

	/**
	 * 添加已勾选的通讯统计应用程序
	 * 
	 * @author
	 * @param intent
	 * @param isModifySQL 是否修改数据库
	 * @throws DatabaseException
	 */
	public void addNotificationAppItem(final Intent intent, boolean isModifySQL)
			throws DatabaseException {
		if (intent == null) {
			return;
		}
		final String packageName = intent.getComponent().getPackageName();
		if (isModifySQL) {
			mNotificationSettingDataModel.addNotificationAppItem(intent);
		}
		if (packageName == null || mNotificationHashMap.containsKey(packageName)) {
			return;
		}
		AppItemInfo appItemInfo = mAppDataEngine.getAppItem(intent);
		appItemInfo.setNotificationType(NotificationType.NOTIFICATIONTYPE_MORE_APP);
		mNotificationHashMap.put(packageName, appItemInfo);
	}

	/**
	 * 取消已勾选的通讯统计应用程序
	 * 
	 * @author
	 * @param intent
	 * @param isModifySQL 是否修改数据库
	 * @throws DatabaseException
	 */
	public void delNotificationAppItem(final Intent intent, boolean isModifySQL)
			throws DatabaseException {
		if (intent == null) {
			return;
		}
		final String packageName = intent.getComponent().getPackageName();
		if (packageName == null) {
			return;
		}
		if (isModifySQL) {
			mNotificationSettingDataModel.delNotificationAppItem(intent);
		}
		if (mNotificationHashMap.containsKey(packageName)) {
			AppItemInfo appItemInfo = mNotificationHashMap.remove(packageName);
			appItemInfo.setNotificationType(NotificationType.IS_NOT_NOTIFICSTION);
			appItemInfo.setUnreadCount(0);
			GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.NOTIFICATION_CHANGED,
					NotificationType.NOTIFICATIONTYPE_MORE_APP, 0, null);
		}
	}

	/**
	 * 批量保存已勾选的通讯统计应用程序
	 *  1.首先清除数据库中原有的部分
	 *  2.重新初始化hashmap
	 *  3.逐个添加新的intent
	 * @author
	 * @param appItems
	 * @throws DatabaseException
	 */
	public void addNotificationAppItems(final ArrayList<Intent> appItems) throws DatabaseException {
		if (appItems == null) {
			return;
		}
		// 1.首先清除数据库中原有的部分
		// 2.重新初始化hashmap
		// 3.逐个添加新的intent
		mNotificationSettingDataModel.clearAllNotificationAppItems();
		if (mNotificationHashMap != null) {
			Iterator<Entry<String, AppItemInfo>> it = mNotificationHashMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, AppItemInfo> entry = it.next();
				AppItemInfo appItemInfo = entry.getValue();
				appItemInfo.setNotificationType(NotificationType.IS_NOT_NOTIFICSTION);
				appItemInfo.setUnreadCount(0);
				GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.NOTIFICATION_CHANGED,
						NotificationType.NOTIFICATIONTYPE_MORE_APP, 0, null);
			}
			mNotificationHashMap.clear();
		}
		int size = appItems.size();
		for (int i = 0; i < size; i++) {
			Intent intent = appItems.get(i);
			addNotificationAppItem(intent, true);
		}
	}

	/**
	 * 通过intent判定程序是否被设置成通讯统计应用
	 * 
	 * @author
	 * @param intent
	 * @return
	 */
	public boolean isMoreApp(final Intent intent) {
		boolean result = false;
		if (intent == null) {
			return result;
		}

		final String packageName = intent.getComponent().getPackageName();
		if (packageName == null) {
			return result;
		}
		AppItemInfo appItemInfo = mNotificationHashMap.get(packageName);
		if (appItemInfo != null) {
			result = true;
		}
		return result;
	}

	/**
	 * 获取所有通讯统计应用程序，并按照已被勾选程序中的程序在前，不在已被勾选程序中的程序在后的顺序来排列
	 * 
	 * @author
	 * @return
	 */
	public ArrayList<AppItemInfo> getAllAppItemInfos() {
		// initNotificationHashMap();
		if (mNotificationHashMap == null) {
			mNotificationHashMap = new HashMap<String, AppItemInfo>();
		}
		ArrayList<AppItemInfo> allItemInfos = mAppDataEngine.getAppItemInfosForNotification();
		ArrayList<AppItemInfo> itemInfosFin = new ArrayList<AppItemInfo>();
		Iterator<Entry<String, AppItemInfo>> it = mNotificationHashMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, AppItemInfo> entry = it.next();
			AppItemInfo appItemInfo = entry.getValue();
			if (allItemInfos.remove(appItemInfo)) {
				if (appItemInfo != null) {
					itemInfosFin.add(appItemInfo);
					try {
						SortUtils.sort(itemInfosFin, "getTitle", null, null, null);
					} catch (IllegalArgumentException e) {
						// 可能因为用户手机Java运行时环境的问题出错
						e.printStackTrace();
					}
				}
			}
		}
		try {
			SortUtils.sort(allItemInfos, "getTitle", null, null, null);
		} catch (IllegalArgumentException e) {
			// 可能因为用户手机Java运行时环境的问题出错
			e.printStackTrace();
		}
		itemInfosFin.addAll(allItemInfos);
		return itemInfosFin;
	}

	/**
	 * 清空所有通讯统计应用程序
	 * 
	 * @throws DatabaseException
	 */
	public void clearAllNotificationAppItems() throws DatabaseException {
		if (mNotificationHashMap != null) {
			Iterator<Entry<String, AppItemInfo>> it = mNotificationHashMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, AppItemInfo> entry = it.next();
				AppItemInfo appItemInfo = entry.getValue();
				appItemInfo.setNotificationType(NotificationType.IS_NOT_NOTIFICSTION);
				appItemInfo.setUnreadCount(0);
				GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.NOTIFICATION_CHANGED,
						NotificationType.NOTIFICATIONTYPE_MORE_APP, 0, null);
			}
			mNotificationHashMap.clear();
			mNotificationHashMap = null;
		}
		mNotificationSettingDataModel.clearAllNotificationAppItems();
	}

	/**
	 * 收到通讯统计包的广播后更新
	 */
	public void updateNotificationHashMap(String packageName, int count) {
		if (mNotificationHashMap == null) {
			initNotificationHashMap();
		}
		if (mNotificationHashMap.containsKey(packageName)) {
			AppItemInfo appItemInfo = mNotificationHashMap.get(packageName);
			if (appItemInfo.getUnreadCount() != count) {
				appItemInfo.setNotificationType(NotificationType.NOTIFICATIONTYPE_MORE_APP);
				appItemInfo.setUnreadCount(count);
				// 旧的方式通知桌面文件夹更新
				Integer integer = Integer.valueOf(count);
				GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.NOTIFICATION_CHANGED,
						NotificationType.NOTIFICATIONTYPE_MORE_APP, integer, null);
			}
		}
	}

	public AppItemInfo getNotificationAppItem(Intent intent) {
		return mAppDataEngine.getAppItem(intent);
	}

	public boolean isAccessibilityStart() {
		Context notificationContext;
		try {
			notificationContext = mContext.createPackageContext(ICustomAction.NOTIFICATION_PACKAGE,
					Context.CONTEXT_IGNORE_SECURITY);
			PreferencesManager sp = new PreferencesManager(notificationContext,
					IPreferencesIds.NOTIFICATION_SETTING, Context.MODE_WORLD_READABLE);
			mIsStartAccessibility = sp.getBoolean("IsEnableAccessibility", false);
		} catch (NameNotFoundException e) {
			//			e.printStackTrace();
			mIsStartAccessibility = false;
		}
		return mIsStartAccessibility;
	}

	@Override
	public void onInvokeApp(Intent intent) {
		// 主包上相应应用未读清0
		String packageName = null;
		if (intent != null) {
			ComponentName component = intent.getComponent();
			if (component != null) {
				packageName = component.getPackageName();
			}
		}
		if (packageName != null) {
			if (mNotificationHashMap != null && mIsStartAccessibility) {
				if (mNotificationHashMap.containsKey(packageName)) {
					// 通讯统计包相应应用未读清0
					updateNotificationHashMap(packageName, 0);
					Intent resetIntent = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_RESET_APPLICATION);
					if (packageName.equals(PackageName.GOOGLE_TALK_ANDROID_TALK)) {
						packageName = PackageName.GOOGLE_TALK_ANDROID_GSF;
					}
					resetIntent.putExtra("resetpackagename", packageName);
					mContext.sendBroadcast(resetIntent);
				}
			}
		}
	}

	public boolean isAccessibilityFirstRun() {
		Context notificationContext;
		try {
			notificationContext = mContext.createPackageContext(ICustomAction.NOTIFICATION_PACKAGE,
					Context.CONTEXT_IGNORE_SECURITY);
			PreferencesManager sp = new PreferencesManager(notificationContext,
					IPreferencesIds.NOTIFICATION_SETTING, Context.MODE_WORLD_READABLE);
			mIsFirstStart = sp.getBoolean("isFirstRun", true);
		} catch (NameNotFoundException e) {
			//			e.printStackTrace();
			mIsFirstStart = true;
		}
		return mIsFirstStart;
	}
}
