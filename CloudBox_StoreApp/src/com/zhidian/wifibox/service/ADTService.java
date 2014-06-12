package com.zhidian.wifibox.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.DownloadCountController;
import com.zhidian.wifibox.controller.InstallCountController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.data.AppDownloadBean;
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.db.dao.AppPackageDao;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.receiver.CheckRunningAppReceiver;
import com.zhidian.wifibox.receiver.CheckSQLiteDataReceiver;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.util.InfoUtil;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * 插件服务
 * 
 * @author zhaoyl
 * 
 */
@SuppressLint("SimpleDateFormat")
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

		// 注册查询数据库广播事件
		Intent checksqliteIntent = new Intent(this,
				CheckSQLiteDataReceiver.class);
		checksqliteIntent.setAction("alarm.checksqlite.action");
		PendingIntent checkPending = PendingIntent.getBroadcast(this, 0,
				checksqliteIntent, 0);
		TAApplication.am.setInexactRepeating(AlarmManager.RTC,
				System.currentTimeMillis(), 30 * 60 * 1000, checkPending);// 每半时执行一次

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
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {// 一个已存在的应用程序包已经改变，包括包名

			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) { // 卸载
				AppPackageDao dao = new AppPackageDao(
						TAApplication.getApplication());
				AppDownloadBean bean = dao.queryPackage2(packageName);
				if (bean != null) {// 表示是在我们市场安装的

					putAppInstallorUninstallData(context, bean, "1");
					dao.deletePackageName(packageName);// 删除数据库中的信息
				}
			} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {// 安装
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
								// TODO Auto-generated method stub

							}

							@Override
							public void onSuccess(TAResponse response) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onRuning(TAResponse response) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onFailure(TAResponse response) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub

							}
						}, true, false);
	}

	/**************************
	 * 上传app下载数据到服务器
	 * 
	 * @param task
	 * @param context
	 **************************/
	private void putDownloadData(DownloadTask task, Context context) {
		AppDownloadCount bean = new AppDownloadCount();
		bean.uuId = InfoUtil.getUUID(TAApplication.getApplication());
		bean.boxNum = InfoUtil.getBoxId(TAApplication.getApplication());
		bean.appId = String.valueOf(task.appId);
		bean.packageName = task.packName;
		bean.version = task.version;
		if (ModeManager.getInstance().isRapidly()) {// true表示为急速模式
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
		// TODO Auto-generated method stub
		unregisterReceiver(mAppInstallListener);
		unregisterReceiver(mDownloadListener);
		unregisterReceiver(screenReceiver);
		startService(new Intent(getApplicationContext(), ADTService.class));
		Log.i(TAG, "服务已被销毁");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, Service.START_STICKY, startId);
	}

}
