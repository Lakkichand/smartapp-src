package com.zhidian.wifibox.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.DownloadCountController;
import com.zhidian.wifibox.controller.DownloadSpeedController;
import com.zhidian.wifibox.controller.InstallCountController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.data.AppDownloadBean;
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.data.DownloadSpeed;
import com.zhidian.wifibox.db.dao.AppDownloadSpeedDao;
import com.zhidian.wifibox.db.dao.AppPackageDao;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.receiver.CheckRunningAppReceiver;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;

/**
 * 插件服务
 * 
 * @author zhaoyl
 * 
 */
public class ADTService extends Service {

	private PendingIntent checkPi;
	private Intent checkIntent;
	private static final String TAG = ADTService.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		// 注册应用安装卸载事件
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mAppInstallListener, intentFilter);

		// 注册应用下载广播事件
		intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_BROADCAST_ACTION);
		registerReceiver(mDownloadListener, intentFilter);

		// 注册应用激活广播事件
		checkIntent = new Intent(this, CheckRunningAppReceiver.class);
		checkIntent.setAction("alarm.check.action");
		checkPi = PendingIntent.getBroadcast(this, 0, checkIntent, 0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(screenReceiver, filter);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		boolean screen = pm.isScreenOn();
		if (screen) {
			TAApplication.am.setInexactRepeating(AlarmManager.RTC,
					System.currentTimeMillis(), 5 * 1000, checkPi);// 每5秒执行一次
		}

		gotoCheckDownloadApp();
	}

	/**
	 * 查询安装的应用是否已被下载
	 */
	private void gotoCheckDownloadApp() {
//		Log.e(TAG, "开启服务");
		AppPackageDao dao = new AppPackageDao(TAApplication.getApplication());
		List<AppInstallBean> aiList = dao.getAllDownloadPackageName();
		if (aiList == null) {
			return;
		}

		for (int i = 0; i < aiList.size(); i++) {
			AppInstallBean bean = aiList.get(i);
			String packName = bean.packageName;
			if (!AppUtils.isInstallWx(TAApplication.getApplication(), packName)) {
//				Log.e(TAG, "应用已被卸载");
				bean.uuId = InfoUtil.getUUID(TAApplication.getApplication());
				bean.boxNum = InfoUtil.getBoxId(TAApplication.getApplication());

				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				bean.installTime = formatter.format(curDate);
				bean.installType = "1";// 表示是卸载
				bean.networkWay = CheckNetwork.getAPNType(TAApplication
						.getApplication());
				bean.status = "1";// 表示卸载成功

				putAppUninstallData(bean);
				dao.deletePackageName(packName);// 删除数据库中已下载的应用的信息
			}

		}
	}

	private void putAppUninstallData(AppInstallBean installbean) {
		TAApplication.getApplication()
				.doCommand(
						TAApplication.getApplication().getString(
								R.string.installcountcontroller),
						new TARequest(InstallCountController.INSTALLCOUNT,
								installbean), new TAIResponseListener() {

							@Override
							public void onStart() {
							}

							@Override
							public void onSuccess(TAResponse response) {
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

	/**
	 * 下载广播接收器
	 */
	private final BroadcastReceiver mDownloadListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadTask task = intent.getParcelableExtra("task");
			if (task.state == DownloadTask.COMPLETE) {// 表示下载完成
				putDownloadData(task, context);
				Map<String, DownloadTask> mMap = DownloadTaskRecorder
						.getInstance().getDownloadTaskList();
//				Log.e("正在下载", mMap.size() + "个");
				if (mMap.size() > 0) {

					boolean isUnload = true;
					Set keysSet = mMap.keySet();
					Iterator iterator = keysSet.iterator();
					while (iterator.hasNext()) {// 遍历下载任务列表
						String key = (String) iterator.next();
						DownloadTask dt = mMap.get(key);
						if (dt.state == DownloadTask.DOWNLOADING
								|| dt.state == DownloadTask.WAITING) {
							isUnload = false;
							return;
						}
					}

					if (isUnload) {
//						Log.e("", "开始上传下载速度数据");
						// app下载速度统计
						AppDownloadSpeedDao speedDao = new AppDownloadSpeedDao(
								TAApplication.getApplication());
						List<DownloadSpeed> speedList = speedDao.getAllData();
						// 每次最多传500条，如超多500，则分多次传送数据到服务器
						if (speedList != null && speedList.size() > 0) {
							if (speedList.size() <= 500) {
								UnloadDownloadSpeed(speedList,
										TAApplication.getApplication());
							} else {
								int count = speedList.size();
//								Log.e("ADTService", "共有：" + count + "条数据");
								int lun = count / 500;
								int yu = count % 500;
								if (yu > 0) {
									lun = lun + 1;
								}
								int currentIndex = 0;
								for (int i = 0; i < lun; i++) {
									List<DownloadSpeed> list = new ArrayList<DownloadSpeed>();
									for (int j = 0; j < 500; j++) {
										if (currentIndex < speedList.size()) {
											list.add(speedList
													.get(currentIndex));
											currentIndex++;
										}

									}
									// 上传list
//									Log.e("ADTService", "list大小：" + list.size());
									UnloadDownloadSpeed(list,
											TAApplication.getApplication());
								}

							}

						}
					}

				}
			}
		}
	};

	/**
	 * 应用安装卸载监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String packageName = intent.getData().getSchemeSpecificPart();
			InstallingValidator.getInstance().onAppAction(
					TAApplication.getApplication(), packageName);
			if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) { // 卸载
				AppPackageDao dao = new AppPackageDao(
						TAApplication.getApplication());
				AppDownloadBean bean = dao.queryPackage2(packageName);
				if (bean != null) {// 表示是在我们市场安装的
					putAppInstallorUninstallData(context, bean, "1");
					dao.deletePackageName(packageName);// 删除数据库中已下载的应用的信息
				}
			} else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {// 安装
				AppPackageDao dao = new AppPackageDao(
						TAApplication.getApplication());
				AppDownloadBean bean = dao.queryPackage2(packageName);
				if (bean != null) {// 表示是在本市场下载的。
					putAppInstallorUninstallData(context, bean, "0");

					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					String installTime = formatter.format(curDate);
					dao.addInstallTime(packageName, installTime);// 更新安装时间
				}
			}

		}
	};

	/**************************
	 * 上传app安装、卸载数据到服务器
	 **************************/
	private void putAppInstallorUninstallData(Context context,
			AppDownloadBean bean, String installType) {
		AppInstallBean installbean = new AppInstallBean();
		installbean.uuId = InfoUtil.getUUID(TAApplication.getApplication());
		installbean.boxNum = InfoUtil.getBoxId(TAApplication.getApplication());
		installbean.appId = bean.appId;
		installbean.packageName = bean.packageName;
		installbean.version = bean.version;
		installbean.downloadSource = bean.downloadSource;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		installbean.installTime = formatter.format(curDate);
		installbean.installType = installType;
		installbean.status = "1";
		installbean.downloadModel = bean.downloadModel;
		installbean.networkWay = CheckNetwork.getAPNType(context);
		TAApplication.getApplication()
				.doCommand(
						context.getString(R.string.installcountcontroller),
						new TARequest(InstallCountController.INSTALLCOUNT,
								installbean), new TAIResponseListener() {

							@Override
							public void onStart() {
							}

							@Override
							public void onSuccess(TAResponse response) {
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

	/**************************
	 * 上传app下载成功量
	 **************************/
	private void putDownloadData(DownloadTask task, Context context) {
		AppDownloadCount bean = new AppDownloadCount();
		bean.uuId = InfoUtil.getUUID(TAApplication.getApplication());
		bean.boxNum = InfoUtil.getBoxId(TAApplication.getApplication());
		bean.appId = String.valueOf(task.appId);
		bean.packageName = task.packName;
		bean.version = task.version;
		if (!TextUtils.isEmpty(task.src)) {// true表示为急速模式
			bean.downloadModel = "0";
		} else {
			bean.downloadModel = "1";
		}
		if (ModeManager.checkRapidly()) {
			bean.downloadSource = "0";// 表示是在门店下载（无论是否是急速模式）
		} else {
			bean.downloadSource = "1";
		}
		bean.networkWay = CheckNetwork.getAPNType(context);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		bean.downloadTime = formatter.format(curDate);

		TAApplication.getApplication().doCommand(
				context.getString(R.string.downloadcountcontroller),
				new TARequest(DownloadCountController.DOWNLOADCOUNT, bean),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {

					}
				}, true, false);
	}

	/**
	 * 上传app下载速度统计数据
	 * 
	 * @param speedList
	 * @param context
	 */
	private void UnloadDownloadSpeed(List<DownloadSpeed> speedList,
			Context context) {
		TAApplication.getApplication()
				.doCommand(
						context.getString(R.string.downloadspeedcontroller),
						new TARequest(DownloadSpeedController.DOWNLOAD_SPEED,
								speedList), new TAIResponseListener() {

							@Override
							public void onSuccess(TAResponse response) {

							}

							@Override
							public void onStart() {

							}

							@Override
							public void onRuning(TAResponse response) {

							}

							@Override
							public void onFinish() {

							}

							@Override
							public void onFailure(TAResponse response) {

							}
						}, true, false);

	}

	BroadcastReceiver screenReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				TAApplication.am.setInexactRepeating(AlarmManager.RTC,
						System.currentTimeMillis(), 5 * 1000, checkPi);// 每5秒执行一次
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				TAApplication.am.cancel(checkPi);
			}
		}
	};

	@Override
	public void onDestroy() {
		unregisterReceiver(mAppInstallListener);
		unregisterReceiver(mDownloadListener);
		unregisterReceiver(screenReceiver);
		startService(new Intent(getApplicationContext(), ADTService.class));
//		Log.i(TAG, "服务已被销毁");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startForegroundCompat();
		return super.onStartCommand(intent, Service.START_STICKY, startId);
	}

	/**
	 * 常驻内存
	 */
	private void startForegroundCompat() {
		try {
			if (Build.VERSION.SDK_INT < 18) {
//				Log.v(TAG, "startForgroundCompat");
				startForeground(1120, new Notification());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
