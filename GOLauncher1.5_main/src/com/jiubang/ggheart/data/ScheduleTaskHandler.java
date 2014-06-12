package com.jiubang.ggheart.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.gau.utils.net.HttpAdapter;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManageView;
import com.jiubang.ggheart.appgame.appcenter.help.AppsManagementConstants;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.appsupdate.AppsListUpdateManager;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.RateDialogContentActivity;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.HttpUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageCenterActivity;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageBaseBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.desks.net.VersionManager;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.components.advert.AdvertControl;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsAppsInfoData;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.LockerManager;
import com.jiubang.ggheart.data.theme.OnlineThemeGetter;
import com.jiubang.ggheart.data.theme.PaidThemeInfoGetter;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeNotifyBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 定时任务管理者
 * @author yangguanxiang
 *
 */
public final class ScheduleTaskHandler implements BroadCasterObserver {
	// 将时间统一抽至最前
	private final static long AUTO_CHECK_DELAY = 5*60 * 1000; // 启动后3mins后检查一次更新
//		private final static long AUTO_CHECK_DELAY = 5*60 * 1000;  						// 检测时为10秒
	private final static long SCAN_APPS_DELAY = (int) (2.5 * 60 * 1000); // < 2.2 version 启动2.5mins后开始扫描
	private final static long SCAN_APPS_DELAY_FROYO = (int) (2.5 * 60 * 1000); // 2.2 version启动2.5mins后开始扫描
	private final static long SHOW_RATE_DIALOG = 24 * 60 * 60 * 1000; // 新安装24小时提醒用户进行评分
	//	private final static long SCAN_APPS_DELAY = 25 * 1000;  						// < 2.2 version 启动25s后开始扫描
	//	private final static long SCAN_APPS_DELAY_FROYO = 25 * 1000 ;  					// 2.2 version启动25s后开始扫描
	private final static long UPDATE_INTERVAL = 5*60 * 1000; // 每隔 8小时检查一次更新
//		private final static long UPDATE_INTERVAL = 5*60 * 1000;						// 每隔 10s检查一次更新
	// 请注意，上述代码涉及到统计上传

	//	private final static long AUTO_CHECK_DELAY = 30 * 1000; 						//检测时为30秒
	//	private final static long AUTO_CHECK_DELAY = 3* 60 * 1000;  					// 启动后3mins后检查一次更新
	//	private final static long AUTO_CHECK_DELAY = AlarmManager.INTERVAL_DAY;  		// 启动ngwe后24小时后检查一次更新
	//	private final static long SCAN_APPS_DELAY = (int)(2.5 * 60  * 1000);  			// < 2.2 version 启动2.5mins后开始扫描
	//	private final static long SCAN_APPS_DELAY_FROYO = (int)(2.5 * 60 * 1000) ;  	// 2.2 version启动2.5mins后开始扫描
	//	private final static long UPDATE_INTERVAL = AlarmManager.INTERVAL_HALF_DAY;		// 每隔 12小时检查一次更新
	//	private final static long UPDATE_INTERVAL = 55 * 1000;							//检测时为55秒
	//	private final static long UPDATE_INTERVAL = AlarmManager.INTERVAL_DAY;		// 每隔 24小时检查一次更新
	//	private final static long UPDATE_INTERVAL = 2*60*1000;		// 每隔 12小时检查一次更新，测试时使用4分钟

	// 2012-2-3 间隔时间从3天更改为2天 by 敖日明
	private final static int UPLOAD_DELAY_DAY_FOR_NORMAL = 3 * 2; //一般更新提示间隔时间，由于每次是8h，因此用3*n表示间隔n天	
	private final static int UPLOAD_DELAY_DAY_FOR_NOTIFICATION = 3 * 10; //通知栏更新提示间隔时间，由于每次是8h，因此用3*n表示间隔n天

	private static final String SHAREDPREFERENCES_CHECK = "autocheck";
	private static final String SHAREDPREFERENCES_CHECK_KEY = "check"; //检查的sharedpreferences中的key
	private static final String SHAREDPREFERENCES_CHECK_TIME_KEY = "check_time"; //检查的sharedpreferences中的上次时间值
	private static final String SHAREDPREFERENCES_TRY_UPLOAD_KEY = "upload"; //检查的sharedpreferences中的上传
	private static final String SHAREDPREFERENCES_FIRST_HANDLE_APPUPDATE_MSG_KEY = "first_handle_appupdate_msg_key"; //检查的sharedpreferences中的上传
	public static final long ONE_DAY = AlarmManager.INTERVAL_DAY;

	private final HashMap<String, PendingIntent> mPendingHashMap = new HashMap<String, PendingIntent>(
			2);

	private Context mContext;
	private AlarmManager mAlarmManager;
	private TaskReceiver mReceiver;
	private MessageManager mMsgManager;
	//	private long mLastCheckUpdateTime = 0;

	public final static long ONE_MIN = 60 * 1000; // 1min
	public final static long HALF_AN_HOUR = 30 * ONE_MIN; // 30min
	public final static String SHAREDPREFERENCES_MSG_UPDATE = "msg_update";
	public final static String SHAREDPREFERENCES_CHECK_MSG = "msg_autocheck";
	public final static String SHAREDPREFERENCES_NEED_START_UPDATE = "start"; //网络连接上时是否需要更新
	public final static String SHAREDPREFERENCES_READED_LIST = "readed_msg_list";
	private static final int MSG_SHOW_DIALOG = 0X01;
	private static final int MSG_ADD_VIEW_COERFRAME = 0X02;
	private static final int MSG_PULL_NEW_THEME = 0X03;

	//facebook
	private final int mFACEBOOK_SHARE_DELEY_TIME = 70 * 60 * 60 * 1000; //facebook分享弹框间隔时间

	public Handler mNotifyHandler = new Handler();

	//	/**
	//	 * 点击notification的更新信息，跳转到应用中心的action
	//	 */
	//	private static final String ACTION_APP_MANAGEMENT = "com.gau.go.launcherex.appmanagement";

	public ScheduleTaskHandler(Context context) {
		mContext = context;
		mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ICustomAction.ACTION_SCAN_APPS);
		filter.addAction(ICustomAction.ACTION_AUTO_CHECK_UPDATE);
		filter.addAction(ICustomAction.ACTION_TIDY_DATA);
		filter.addAction(ICustomAction.ACTION_MSGCENTER_SHOWMSG);
		filter.addAction(ICustomAction.ACTION_MSGCENTER_REMOVEMSG);
		filter.addAction(ICustomAction.ACTION_CHECK_NEWTHEME_NOTIFY);

		if (null != GoLauncher.getContext() && GoLauncher.getContext().getFirstRun()) {
			setFirstRunForRate(GoLauncher.getContext().getFirstRun());
		}
		if (getFirstRunForRate()) {
			filter.addAction(ICustomAction.ACTION_SHOW_RATE_DIALOG);
		}
		mReceiver = new TaskReceiver();
		mContext.registerReceiver(mReceiver, filter);
		mMsgManager = MessageManager.getMessageManager(GOLauncherApp.getContext());
		mMsgManager.registerObserver(this);

		//注册网络状态监听
		filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mReceiver, filter);
	}
	/**
	 * 启动统计桌面数据的任务
	 */
	public void startStatisticsTask() {
		scheduleNextStatistics(Statistics.STATICTISC_USEDHOURS_FREQUENCY);
	}
	private void scheduleNextStatistics(long time) {
		try {
			final long tiggertTime = System.currentTimeMillis() + time;
			Intent intent = new Intent(ICustomAction.ACTION_TIDY_DATA);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, tiggertTime, pendingIntent);
			mPendingHashMap.put(ICustomAction.ACTION_TIDY_DATA, pendingIntent);
			intent = null;
			pendingIntent = null;
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "startStatisticsTask error");
		}
	}

	private void scheduleNextRate(long time) {
		try {
			final long tiggertTime = System.currentTimeMillis() + time;
			Intent intent = new Intent(ICustomAction.ACTION_SHOW_RATE_DIALOG);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, tiggertTime, pendingIntent);
			mPendingHashMap.put(ICustomAction.ACTION_SHOW_RATE_DIALOG, pendingIntent);
			intent = null;
			pendingIntent = null;
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "startRateTask error");
		}
	}

	/**
	 * 检查更新
	 */
	public void startCheckUpdateTask() {
		scheduleNextCheck(AUTO_CHECK_DELAY);
	}

	private void scheduleNextCheck(long time) {
		try {
			final long tiggertTime = System.currentTimeMillis() + time;
			Intent updateIntent = new Intent(ICustomAction.ACTION_AUTO_CHECK_UPDATE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, updateIntent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, tiggertTime, pendingIntent);
			mPendingHashMap.put(ICustomAction.ACTION_AUTO_CHECK_UPDATE, pendingIntent);
			updateIntent = null;
			pendingIntent = null;
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "startCheckUpdateTask error");
		}
	}	

	public void startScanAppTask() {
		try {
			final int apiLevel = Build.VERSION.SDK_INT;
			long tiggerTime = System.currentTimeMillis();
			if (apiLevel >= 8) {
				tiggerTime += SCAN_APPS_DELAY_FROYO;
			} else {
				tiggerTime += SCAN_APPS_DELAY;
			}

			Intent scanAppIntent = new Intent(ICustomAction.ACTION_SCAN_APPS);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, scanAppIntent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, tiggerTime, pendingIntent);
			mPendingHashMap.put(ICustomAction.ACTION_SCAN_APPS, pendingIntent);
			scanAppIntent = null;
			pendingIntent = null;
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "startScanAppTask error");
		}
	}

	public void startRateDialogTask() {
		try {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
			long lastCheckUpdate = 0L;
			if (sharedPreferences != null) {
				lastCheckUpdate = sharedPreferences.getLong(IPreferencesIds.REMIND_RATE_TIME, 0L);
			}
			long now = System.currentTimeMillis();
			long nextCheckTime = 0L; //下一次上传间隔时间

//			if (lastCheckUpdate == 0L || (now - lastCheckUpdate <= 0L)) {
//				nextCheckTime = SHOW_RATE_DIALOG; //下一次上传间隔时间
//			} else if (now - lastCheckUpdate >= SHOW_RATE_DIALOG) {
//				nextCheckTime = 8 * 60 * 1000;
//			} else {
//				//动态调整下一次的间隔时间
//				nextCheckTime = SHOW_RATE_DIALOG - (now - lastCheckUpdate);
//			}
//			now += nextCheckTime;
//			Intent rateAppIntent = new Intent(ICustomAction.ACTION_SHOW_RATE_DIALOG);
//			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, rateAppIntent, 0);
//			mAlarmManager.set(AlarmManager.RTC_WAKEUP, now, pendingIntent);
//			mPendingHashMap.put(ICustomAction.ACTION_SHOW_RATE_DIALOG, pendingIntent);
//			rateAppIntent = null;
//			pendingIntent = null;
			if (lastCheckUpdate == 0L || (now - lastCheckUpdate <= 0L)) {
				nextCheckTime = SHOW_RATE_DIALOG; //下一次上传间隔时间
				now += nextCheckTime;
				Intent rateAppIntent = new Intent(ICustomAction.ACTION_SHOW_RATE_DIALOG);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, rateAppIntent, 0);
				mAlarmManager.set(AlarmManager.RTC_WAKEUP, now, pendingIntent);
				mPendingHashMap.put(ICustomAction.ACTION_SHOW_RATE_DIALOG, pendingIntent);
				rateAppIntent = null;
				pendingIntent = null;			
			}
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "startShowRateDialogTask error");
		}
	}

	public synchronized void cancel() {
		Collection<PendingIntent> collection = mPendingHashMap.values();
		for (PendingIntent pendingIntent : collection) {
			mAlarmManager.cancel(pendingIntent);
		}
		mPendingHashMap.clear();
		mContext.unregisterReceiver(mReceiver);
	}

	private void doAutoCheck() {
		long now = System.currentTimeMillis();
		long nextCheckTime = UPDATE_INTERVAL; //下一次上传间隔时间
		long lastCheckUpdate = getLastCheckedTime(); //上一次的检查时间
		if (lastCheckUpdate == 0L || (now - lastCheckUpdate >= UPDATE_INTERVAL)
				|| (now - lastCheckUpdate <= 0L)) {
			final VersionManager versionManager = AppCore.getInstance().getVersionManager();
			//检查更新同时上传统计数据
			versionManager.startCheckUpdate();

			//检查程序列表更新
			int tryUpload = getLastTryUpload();
			if (tryUpload % 3 == 0) {
				//每隔24h上传一次，此处需要间隔一次。
				AppsListUpdateManager appsListUpdateManager = AppCore.getInstance()
						.getAppsListUpdateManager();
				HttpAdapter httpAdapter = AppCore.getInstance().getHttpAdapter();
				IConnectListener receiver = getUpdateConnectListener();
				appsListUpdateManager.startCheckUpdate(httpAdapter, receiver, true);
			}
			setLastTryUpload(++tryUpload);

			//保存本次检查的时长
			setLastCheckedTime(now);
			//检查新主题
			mHandler.sendEmptyMessageDelayed(MSG_PULL_NEW_THEME, 600);
		} else {
			//动态调整下一次的间隔时间
			nextCheckTime = UPDATE_INTERVAL - (now - lastCheckUpdate);
		}

		// 启动下一次定时检查
		scheduleNextCheck(nextCheckTime);
	}

	/**
	 * 更新连接监听
	 * @return
	 */
	private IConnectListener getUpdateConnectListener() {
		final IConnectListener receiver = new IConnectListener() {

			@Override
			public void onStart(THttpRequest arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish(THttpRequest arg0, IResponse arg1) {
				// TODO Auto-generated method stub
				//1、清理统计数据
				StatisticsAppsInfoData.resetStatisticsAllDataInfos(mContext);

				//2、可以更新的数据bean
				if (arg1 != null) {
					ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) arg1.getResponse();
					if (listBeans != null && listBeans.size() > 0) {
						final AppsBean appsBean = (AppsBean) listBeans.get(0);
						if (appsBean != null && appsBean.mListBeans != null
								&& appsBean.mListBeans.size() > 0) {
							//取出渠道配置信息，后面更新处理要根据这个而定
							//Add by wangzhuobin 2012.07.28
							final ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();

							int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

							int tryUpload = getLastTryUpload();

							boolean hasHandledUpdateMsg = getHandledAppUpdateMsg();
							if (!hasHandledUpdateMsg) {
								setHandledAppUpdateMsg(true);
							}
							FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
							if (tryUpload % UPLOAD_DELAY_DAY_FOR_NORMAL == 1) {
								//相隔7d，是海外用户								
								if (handler != null && handler.isShowAppUpdate()) {
									//从服务器得到新的AppsBean时,把ClickAppupdate设为false.
									if (null != AppFuncFrame.getDataHandler()) {
										AppFuncFrame.getDataHandler().setmClickAppupdate(false);
									}
									if (currentHour >= 8 && currentHour < 23) {

										if (GoLauncher.getContext().getFirstRun()
												&& !hasHandledUpdateMsg) {
											//如果是第一次安装运行则延迟8小时后通知
											mNotifyHandler.postDelayed(new Runnable() {

												@Override
												public void run() {
													sendUpdateMsgToAppFunc(channelConfig, appsBean);
												}
											}, 8 * 60 * 60 * 1000);
										} else {
											sendUpdateMsgToAppFunc(channelConfig, appsBean);
										}

									} else {
										//延迟10小时通知
										mNotifyHandler.postDelayed(new Runnable() {

											@Override
											public void run() {
												sendUpdateMsgToAppFunc(channelConfig, appsBean);
											}
										}, 10 * 60 * 60 * 1000);
									}

								}
							}
							if (tryUpload % UPLOAD_DELAY_DAY_FOR_NOTIFICATION == 1) {
								if (handler != null && handler.isShowAppUpdate()) {
									if (currentHour >= 8 && currentHour < 23) {
										if (GoLauncher.getContext().getFirstRun()
												&& !hasHandledUpdateMsg) {
											//如果是第一次安装运行则延迟8小时后通知
											mNotifyHandler.postDelayed(new Runnable() {

												@Override
												public void run() {
													sendUpdateMsgToNotification(channelConfig,
															appsBean);
												}
											}, 8 * 60 * 60 * 1000);
										} else {
											sendUpdateMsgToNotification(channelConfig, appsBean);
										}
									} else {
										// 延迟10小时通知

										mNotifyHandler.postDelayed(new Runnable() {

											@Override
											public void run() {
												sendUpdateMsgToNotification(channelConfig, appsBean);
											}
										}, 10 * 60 * 60 * 1000);
									}
								}
							}
						}
					}

				}

			}

			@Override
			public void onException(THttpRequest arg0, int arg1) {
				// TODO Auto-generated method stub
				StatisticsData.saveHttpExceptionDate(mContext, arg0, arg1);
			}
		};

		return receiver;
	}

	private boolean isNeedToCheckWhenNetWorked() {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				SHAREDPREFERENCES_CHECK, Context.MODE_PRIVATE);
		boolean result = false;
		if (sharedPreferences != null) {
			result = sharedPreferences.getBoolean(SHAREDPREFERENCES_CHECK_KEY, false);
		}
		return result;
	}

	private void setNeedToCheckWhenNetWorked(boolean checked) {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				SHAREDPREFERENCES_CHECK, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.putBoolean(SHAREDPREFERENCES_CHECK_KEY, checked);
			sharedPreferences.commit();
		}
	}

	private int getLastTryUpload() {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				SHAREDPREFERENCES_CHECK, Context.MODE_PRIVATE);
		int lastTryUpload = 0;
		if (sharedPreferences != null) {
			lastTryUpload = sharedPreferences.getInt(SHAREDPREFERENCES_TRY_UPLOAD_KEY, 0);
		}
		return lastTryUpload;
	}

	private void setLastTryUpload(int tryUploadTime) {
		if (tryUploadTime >= Integer.MAX_VALUE) {
			tryUploadTime = 0;
		}
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				SHAREDPREFERENCES_CHECK, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.putInt(SHAREDPREFERENCES_TRY_UPLOAD_KEY, tryUploadTime);
			sharedPreferences.commit();
		}
	}

	private long getLastCheckedTime() {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				SHAREDPREFERENCES_CHECK, Context.MODE_PRIVATE);
		long lastCheckedTime = 0L;
		if (sharedPreferences != null) {
			lastCheckedTime = sharedPreferences.getLong(SHAREDPREFERENCES_CHECK_TIME_KEY, 0L);
		}
		return lastCheckedTime;
	}

	private void setLastCheckedTime(long checkedTime) {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				SHAREDPREFERENCES_CHECK, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.putLong(SHAREDPREFERENCES_CHECK_TIME_KEY, checkedTime);
			sharedPreferences.commit();
		}
	}

	/**
	 * 任务广播接受者
	 * @author yangguanxiang
	 *
	 */
	private class TaskReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			//			Log.i("TaskReceiver", "action = " + action);
			try {
				if (action.equals(ICustomAction.ACTION_SCAN_APPS)) {
					mPendingHashMap.remove(action);
					// 超时开始扫描app
					final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
					dataEngine.asynReScanSysApp(IDiyMsgIds.EVENT_REFLUSH_TIME_IS_UP, null);
				}
				//检查更新同时上传统计数据
				else if (action.equals(ICustomAction.ACTION_AUTO_CHECK_UPDATE)) {
					if (Machine.isNetworkOK(mContext)) {
						mPendingHashMap.remove(action);

						doAutoCheckMsg(); //消息中心更新
//						doAutoGetUrl();	

						//当前网络可用，直接检查更新及上传统计
						doAutoCheck();

					} else {
						//当前网络不可用，则保存当前状态值，以便网络状态OK时，再次进行上传统计
						setNeedToCheckWhenNetWorked(true);
					}
					saveStartTime();
				}
				//监控网络更改状态
				else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					//1.网络可用，且上次未上传成功，则上传统计，并修改状态值
					if (Machine.isNetworkOK(mContext)) {
						if (isNeedToCheckWhenNetWorked()) {
							doAutoCheckMsg();
//							doAutoGetUrl();
							doAutoCheck();
							//修改状态值
							setNeedToCheckWhenNetWorked(false);
						}

					}
				}
				//统计桌面数据
				else if (action.equals(ICustomAction.ACTION_TIDY_DATA)) {
					mPendingHashMap.remove(action);
					VersionManager versionManager = AppCore.getInstance().getVersionManager();
					if (versionManager != null) {
						Statistics statistics = versionManager.getStatistics();
						if (statistics != null) {
							//更新使用时间
							statistics.increaseUseTime();
						}
					}

					// 保存用户使用信息
					StatisticsData.saveNoUploadDate(mContext);

					// 启动下一次定时统计
					scheduleNextStatistics(Statistics.STATICTISC_USEDHOURS_FREQUENCY);

					//广告8小时判断一下是否进行通知栏提示
					AdvertControl.getAdvertControlInstance(mContext).checkIsNotOpen();
					AdvertControl.getAdvertControlInstance(mContext).checkRequestAgain();
				}
				//弹出评分对话框
				else if (action.equals(ICustomAction.ACTION_SHOW_RATE_DIALOG)) {
					mPendingHashMap.remove(action);
					showRateDialog();
				} else if (action.equals(ICustomAction.ACTION_MSGCENTER_SHOWMSG)) {
					checkViewType();
				} else if (action.equals(ICustomAction.ACTION_MSGCENTER_REMOVEMSG)) {
					removeCoverMsg();
				} else if (action.equals(ICustomAction.ACTION_CHECK_NEWTHEME_NOTIFY)) {
					checkThemeNotify();
				}

			} catch (Exception e) {
				Log.i(LogConstants.HEART_TAG, "TaskReceiver onReceive err, action = " + action);
			}
		}
	}

	private void saveStartTime() {
		try {
			Context context = GOLauncherApp.getContext();
			PreferencesManager spf = new PreferencesManager(context, "ErrorReport", 0);
			spf.putLong("STARTTIME", System.currentTimeMillis());
			spf.commit();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void doAutoCheckMsg() {
		long now = System.currentTimeMillis();
		long lastCheckUpdate = HttpUtil.getLastUpdateMsgTime(mContext); //上一次的检查时间
		if (lastCheckUpdate == 0L || (now - lastCheckUpdate >= UPDATE_INTERVAL)
				|| (now - lastCheckUpdate <= 0L)) {
			mMsgManager.postUpdateRequest(1);
			mMsgManager.postGetUrlRequest();
		} else {
			checkViewType();
		}
	}
	
//	private void doAutoGetUrl() {
//		long now = System.currentTimeMillis();
//		long lastCheckUpdate = HttpUtil.getLastUpdateMsgTime(mContext); //上一次的检查时间
//		if (lastCheckUpdate == 0L || (now - lastCheckUpdate >= UPDATE_INTERVAL)
//				|| (now - lastCheckUpdate <= 0L)) {
//			mMsgManager.postGetUrlRequest();
//		}
//	}

	/**
	 * <br>功能简述:检查消息中心中是否有未读需要主动显示的消息
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void checkViewType() {
		Vector<MessageHeadBean> msgs = mMsgManager.getMessageList();
		boolean hasDialogMsg = false;
		boolean hasStatusBarMsg = false;
		boolean hasCoverFrameMsg = false;
		long nextScheduleTime = Long.MAX_VALUE;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		for (int i = 0; msgs != null && i < msgs.size(); i++) {
			MessageHeadBean bean = msgs.get(i);
			if (!bean.misReaded && (bean.mViewType & MessageBaseBean.VIEWTYPE_DIALOG) != 0
					&& !hasDialogMsg) {
				String hms = getCurrentHMS();
				if (bean.mEndTime != null
						&& bean.mStartTime != null
						&& isInTime(hms, bean.mStartTime, bean.mEndTime)
						|| (bean.mEndTime == null || bean.mStartTime == null
								|| bean.mEndTime.equals("") || bean.mStartTime.equals(""))) {
					if (!mMsgManager.isInMessageCenter()) {
						mMsgManager.showDialogMessage(bean.mId);
						hasDialogMsg = true;
					}
				} else if (bean.mStartTime != null && !bean.mStartTime.equals("")) {
					try {
						Date d1 = sdf.parse(hms);
						Date d2 = sdf.parse(bean.mStartTime);
						nextScheduleTime = Math.abs(d1.getTime() - d2.getTime());
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (Exception e) {

					}
				}
			}

			if (!bean.misReaded && !bean.mIsRemoved && (bean.mViewType & MessageBaseBean.VIEWTYPE_STATUS_BAR) != 0
					&& !hasStatusBarMsg) {
				String hms = getCurrentHMS();
				if (bean.mEndTime != null
						&& bean.mStartTime != null
						&& isInTime(hms, bean.mStartTime, bean.mEndTime)
						|| (bean.mEndTime == null || bean.mStartTime == null
								|| bean.mEndTime.equals("") || bean.mStartTime.equals(""))) {
					if (!mMsgManager.isInMessageCenter()) {
						mMsgManager.sendNotify(bean);
						hasStatusBarMsg = true;
					}
				} else if (bean.mStartTime != null && !bean.mStartTime.equals("")) {
					try {
						Date d1 = sdf.parse(hms);
						Date d2 = sdf.parse(bean.mStartTime);
						long time = Math.abs(d1.getTime() - d2.getTime());
						if (nextScheduleTime > time) {
							nextScheduleTime = time;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (Exception e) {

					}
				}
			}

			String hms = getCurrentHMS();
			if (!bean.misReaded && (bean.mViewType & MessageBaseBean.VIEWTYPE_DESK_TOP) != 0
					&& !hasCoverFrameMsg && !bean.mClickClosed) {
				// begin 临时代码3.25以后要去除
				if (bean.mId != null
						&& (bean.mId.equals("308") || bean.mId.equals("309") || bean.mId
								.equals("310"))) {
					mMsgManager.markAsReaded(bean.mId);
					continue;
				}
				// end

				if (bean.mEndTime != null
						&& bean.mStartTime != null
						&& isInTime(hms, bean.mStartTime, bean.mEndTime)
						|| (bean.mEndTime == null || bean.mStartTime == null
								|| bean.mEndTime.equals("") || bean.mStartTime.equals(""))) {
					Message msg = Message.obtain();
					msg.what = MSG_ADD_VIEW_COERFRAME;
					msg.obj = bean;
					mHandler.sendMessage(msg);
					hasCoverFrameMsg = true;
					if (bean.mEndTime != null && !bean.mEndTime.equals("")) {
						try {
							Date d1 = sdf.parse(hms);
							Date d2 = sdf.parse(bean.mEndTime);
							long removeT = Math.abs(d1.getTime() - d2.getTime());
							scheduleRemoveCoverMsg(removeT); //超出显示时间移除视图
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				} else if (bean.mStartTime != null && !bean.mStartTime.equals("")) {
					try {
						Date d1 = sdf.parse(hms);
						Date d2 = sdf.parse(bean.mStartTime);
						long time = Math.abs(d1.getTime() - d2.getTime());
						if (nextScheduleTime > time) {
							nextScheduleTime = time;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (Exception e) {

					}
				}
			}
			if (nextScheduleTime != Long.MAX_VALUE) {
				scheduleNextMsgCheck(nextScheduleTime);
				nextScheduleTime = Long.MAX_VALUE;
			}
		}

	}
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_ADD_VIEW_COERFRAME :
					if (msg.obj != null) {
						mMsgManager.prepareZipRes((MessageHeadBean) msg.obj);
					}
					break;
				case MSG_PULL_NEW_THEME :
					doPullNewTheme();
					break;
				default :
					break;
			}
		}

	};

	/**
	 * 用notification显示应用更新提示
	 * @param appsBean
	 * @author zhoujun
	 */
	private void sendUpdateInfoToNotification(AppsBean appsBean) {
		if (appsBean != null && !appsBean.mListBeans.isEmpty()) {
			//add by zhoujun  2010--0-14, 控制是否显示  
			if (appsBean.mControlcontrolMap != null && !appsBean.mControlcontrolMap.isEmpty()
					&& appsBean.mControlcontrolMap.get(1) == 0) {
				return;
			}
			List<AppBean> appBeans = appsBean.mListBeans;
			Resources res = mContext.getResources();
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(AppsManagementConstants.NOTIFY_TAG,
					AppsManagementConstants.NOTIFY_UPDATE_INFO_ID);

			Intent intent = new Intent(ICustomAction.ACTION_APP_MANAGEMENT);

			//下载进度点击时，目前不做任何处理 
			//		intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_VIEW_KEY,
			//				AppsManageView.APPS_UPDATE_VIEW_ID);

			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_START_TYPE_KEY,
					AppsManageView.APPS_START_TYPE);
			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY,
					MainViewGroup.ACCESS_FOR_UPDATE_NOTIFACTION);
			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_SHOW_FRONTCOVER, false);

			PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			String updateInfo = appBeans.size() + " "
					+ res.getString(R.string.apps_management_notification_title_update_info_suffix);
			Notification notification = new Notification(R.drawable.notification_update_icon,
					updateInfo, System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			PackageManager pkgMgr = mContext.getPackageManager();
			StringBuilder sb = new StringBuilder();
			for (AppBean appBean : appBeans) {
				sb.append(appBean.getAppName(pkgMgr)).append(", ");
			}
			sb.delete(sb.lastIndexOf(","), sb.length());
			notification.setLatestEventInfo(mContext, updateInfo, sb.toString(), pendingIntent);
			notificationManager.notify(AppsManagementConstants.NOTIFY_TAG,
					AppsManagementConstants.NOTIFY_UPDATE_INFO_ID, notification);
		}
	}
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		// TODO Auto-generated method stub
		switch (msgId) {
			case MessageCenterActivity.GET_MSG_LIST_FINISH :
				boolean bool = ConvertUtils.int2boolean(param);
				if (bool) {
					if (mMsgManager.getUnreadedCnt() > 0) {
						checkViewType();
					}
				}
				break;
			default :
				break;
		}
	}

	private void sendUpdateMsgToAppFunc(ChannelConfig channelConfig, AppsBean appsBean) {
		//在8:00到23:00之间，则通知
		GoLauncher.sendHandler(null, IDiyFrameIds.APPFUNC_FRAME, IDiyMsgIds.EVENT_APPS_LIST_UPDATE,
				0, appsBean, null);
		if (!channelConfig.isNeedAppCenter()) {
			//如果本渠道没有应用中心，则要交给应用管理来处理
			//Add by wangzhuobin 2012.07.28
			GoLauncher.sendHandler(null, IDiyFrameIds.APP_MANAGER,
					IDiyMsgIds.EVENT_APPS_LIST_UPDATE, 0, appsBean, null);
		}
	}

	private void sendUpdateMsgToNotification(ChannelConfig channelConfig, AppsBean appsBean) {
		// 在8:00到23:00之间，则通知
		if (!channelConfig.isNeedAppCenter()) {
			//如果本渠道没有应用中心，则要交给应用管理来处理
			//Add by wangzhuobin 2012.07.28
			GoLauncher.sendHandler(null, IDiyFrameIds.APP_MANAGER,
					IDiyMsgIds.EVENT_APPS_LIST_UPDATE_NOTIFICATION, 0, appsBean, null);
		} else {
			sendUpdateInfoToNotification(appsBean);
		}
	}

	/**
	 * 是否已经处理过一次应用程序更新消息
	 * @return
	 */
	private boolean getHandledAppUpdateMsg() {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				SHAREDPREFERENCES_CHECK, Context.MODE_PRIVATE);
		boolean result = false;
		if (sharedPreferences != null) {
			result = sharedPreferences.getBoolean(SHAREDPREFERENCES_FIRST_HANDLE_APPUPDATE_MSG_KEY,
					false);
		}
		return result;
	}

	private void setHandledAppUpdateMsg(boolean handled) {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				SHAREDPREFERENCES_CHECK, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.putBoolean(SHAREDPREFERENCES_FIRST_HANDLE_APPUPDATE_MSG_KEY, handled);
			sharedPreferences.commit();
		}
	}
	/**
	 * <br>功能简述:检查是否有新的主题
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void doPullNewTheme() {
		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					checkThemeNotify();
					new OnlineThemeGetter(mContext).getFeaturedThemeInfoBeans(ThemeManager
							.getInstance(mContext).getAllThemeInfosWithoutDefaultTheme(),
							ThemeConstants.LAUNCHER_FEATURED_THEME_ID, true, null);
					if (AppUtils.isGoLockerExist(mContext)) {
						new OnlineThemeGetter(mContext).getFeaturedThemeInfoBeans(LockerManager
								.getInstance(mContext).getInstallThemeInfoBean(),
								ThemeConstants.LOCKER_FEATURED_THEME_ID, true, null);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

		}.start();
	}

	// 弹出评分的对话框
	private void showRateDialog() {
		long now = System.currentTimeMillis();
		PreferencesManager preferencesManager = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		boolean remind_rate = preferencesManager.getBoolean(IPreferencesIds.REMIND_RATE, true);
		boolean launcherIsTop = Machine.isTopActivity(GOLauncherApp.getContext(),
				LauncherEnv.PACKAGE_NAME);
		if (remind_rate) {
			boolean isNeedShowRate = false;
			if (launcherIsTop) {
				preferencesManager.putLong(IPreferencesIds.REMIND_RATE_TIME, now);
				preferencesManager.commit();
				Intent intent = new Intent(GOLauncherApp.getContext(),
						RateDialogContentActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				GOLauncherApp.getContext().startActivity(intent);
				// 注释下面不再提醒
//				// 启动下一次定时弹出评分对话框
//				scheduleNextRate(SHOW_RATE_DIALOG);
			} else {
				// 当前桌面不在顶层，下次启动桌面一份钟后再弹
				isNeedShowRate = true;
				// 注释下面不再提醒
//				scheduleNextRate(SHOW_RATE_DIALOG);
			}
			preferencesManager.putBoolean(IPreferencesIds.IS_NEED_SHOW_RATE_DIALOG, isNeedShowRate);
			preferencesManager.commit();
		}
	}

	private void setFirstRunForRate(boolean firstRun) {
		PreferencesManager preferencesManager = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		preferencesManager.putBoolean(IPreferencesIds.FIRST_RUN_REMIND_RATE, firstRun);
		preferencesManager.commit();
	}

	private boolean getFirstRunForRate() {
		PreferencesManager preferencesManager = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		boolean isFirstRun = preferencesManager.getBoolean(IPreferencesIds.FIRST_RUN_REMIND_RATE,
				false);
		return isFirstRun;
	}

	/**
	 * <br>功能简述:获得当前HH:MM:SS
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private String getCurrentHMS() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			long t = System.currentTimeMillis();
			Date date = new Date(t);
			String result = sdf.format(date);
			return result;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	/**
	 * <br>功能简述:时间dstTime是否在begin~end区间内
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param dstTime
	 * @param begin
	 * @param end
	 * @return
	 */
	private boolean isInTime(String dstTime, String begin, String end) {
		if (dstTime == null || begin == null || end == null
				|| (dstTime.compareTo(begin) >= 0 && dstTime.compareTo(end) < 0)) {
			return true;
		}
		return false;
	}

	/**
	 * <br>功能简述:启动下一次检查是否有主动显示型消息需要显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param nextTime
	 */
	private void scheduleNextMsgCheck(long nextTime) {
		try {
			final long tiggertTime = System.currentTimeMillis() + nextTime;
			Intent intent = new Intent(ICustomAction.ACTION_MSGCENTER_SHOWMSG);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, tiggertTime, pendingIntent);
			mPendingHashMap.put(ICustomAction.ACTION_MSGCENTER_SHOWMSG, pendingIntent);
			intent = null;
			pendingIntent = null;
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "scheduleNextMsgCheck error");
		}
	}

	/**
	 * <br>功能简述:定时移除罩子层视图
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param nextTime
	 */
	private void scheduleRemoveCoverMsg(long nextTime) {
		try {
			final long tiggertTime = System.currentTimeMillis() + nextTime;
			Intent intent = new Intent(ICustomAction.ACTION_MSGCENTER_REMOVEMSG);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, tiggertTime, pendingIntent);
			mPendingHashMap.put(ICustomAction.ACTION_MSGCENTER_REMOVEMSG, pendingIntent);
			intent = null;
			pendingIntent = null;
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "scheduleNextMsgCheck error");
		}
	}
	/**
	 * <br>功能简述:移除罩子层消息视图
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void removeCoverMsg() {
		if (mMsgManager != null) {
			mMsgManager.removeCoverFrameView();
		}
	}

	/**
	 * <br>功能简述:主题通知栏
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void checkThemeNotify() {
		ArrayList<ThemeNotifyBean> beans = ThemeManager.getInstance(mContext).getNotifyBean();
		if (beans != null && !beans.isEmpty()) {
			for (int i = 0; i < beans.size(); i++) {
				ThemeNotifyBean bean = beans.get(i);
				if (bean != null) {
					long stime = bean.getShowStatTime();
					long etime = bean.getShowEndTime();
					long now = System.currentTimeMillis();
					if (now >= stime && now <= etime) {
						PreferencesManager sp = new PreferencesManager(mContext,
								IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
						long lastShowTime = sp.getLong(IPreferencesIds.LAST_SHOW_NEWTHEME_STAMP, 0);
						if (lastShowTime > 0) {
							long diff = now - lastShowTime;
							if (diff < ONE_DAY) {
								ThemeManager.getInstance(mContext).removeNotifyBean(bean.getType());
								return;
							}
						}
						int type = bean.getType();
						String id = null;
						String configId = null;
						if (bean.getType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
							id = IPreferencesIds.HASSHOWFEATURENOTIFY;
							configId = IPreferencesIds.SHAREDPREFERENCES_MSG_THEME_NOTIFY_SHOW_STATICS_DATA;
						} else if (bean.getType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
							id = IPreferencesIds.HASSHOWHOTNOTIFY;
							configId = IPreferencesIds.SHAREDPREFERENCES_MSG_THEME_NOTIFY_SHOW_STATICS_DATA;

						} else {
							id = IPreferencesIds.HASSHOWLOCKERNOTIFY;
							configId = IPreferencesIds.SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_SHOW_STATICS_DATA;

						}
						sp.putLong(IPreferencesIds.LAST_SHOW_NEWTHEME_STAMP, now);
						sp.putBoolean(id, true);
						sp.commit();
						new OnlineThemeGetter(mContext).sendAsynNewThemesNotification(type,
								bean.getShowContent(), bean.getShowIconUrl());
						PreferencesManager manager = new PreferencesManager(mContext, configId,
								Context.MODE_PRIVATE);
						int cnt = manager.getInt(configId, 0);
						manager.putInt(configId, cnt + 1);
						manager.commit();
						int staticsId = ThemeConstants.STATICS_ID_FEATURED_NOTIFY;
						if (type == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
							staticsId = ThemeConstants.STATICS_ID_LOCKER_NOTIFY;
						}
						mMsgManager.updateThemeNotifyStatisticsData(staticsId, 0, true);
						ThemeManager.getInstance(mContext).removeNotifyBean(bean.getType());

					} else if (stime > now) {
						scheduleNextThemeNotifyCheck(stime - now);
					}
				}
			}

		}
	}

	/**
	 * <br>功能简述:启动下一次检查是否有主动显示型消息需要显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param nextTime
	 */
	private void scheduleNextThemeNotifyCheck(long nextTime) {
		try {
			final long tiggertTime = System.currentTimeMillis() + nextTime;
			Intent intent = new Intent(ICustomAction.ACTION_CHECK_NEWTHEME_NOTIFY);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, tiggertTime, pendingIntent);
			mPendingHashMap.put(ICustomAction.ACTION_CHECK_NEWTHEME_NOTIFY, pendingIntent);
			intent = null;
			pendingIntent = null;
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "scheduleNextMsgCheck error");
		}
	}

}
