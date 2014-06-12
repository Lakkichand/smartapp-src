package com.jiubang.ggheart.apps.appmanagement.controler;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.appmanagement.bean.AppInfo;
import com.jiubang.ggheart.apps.appmanagement.bean.NoPromptUpdateInfo;
import com.jiubang.ggheart.apps.appmanagement.component.AppsManageView;
import com.jiubang.ggheart.apps.appmanagement.component.AppsManagementActivity;
import com.jiubang.ggheart.apps.appmanagement.download.ApplicationDownloadListener;
import com.jiubang.ggheart.apps.appmanagement.help.AppsManagementConstants;
import com.jiubang.ggheart.apps.appmanagement.help.NoPromptUpdateDataModel;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 */
public class ApplicationManager implements IMessageHandler {

//	public static final String ACTION_APP_MANAGEMENT = "com.gau.go.launcherex.appmanagement";

//	public static final String ACTION_APP_MANAGEMENT_CN = "com.gau.go.launcherex.appmanagement.cn";
	/**
	 * 高级任务管理的包名
	 */
	public static final String GO_TASKMANAGE_PACKAGENAME = "com.gau.go.launcherex.gowidget.taskmanager";
	// private static final int GO_TASKMANAGE_VERSIONCODE = 16;

	private static ApplicationManager sManager;

	private Context mContext;
	private AppDataEngine mEngine;

	private Handler mAppsManagementHandler = null;

	private boolean mShowWarningDialog = false;

	private int mUpdateAppsCount = 0; // 保存可更新应用数

	// 忽略更新数据持久化执行者
	protected NoPromptUpdateDataModel mNoUpdateModel = null;

	private ApplicationManager() {

	}

	private ApplicationManager(Context context) {
		mContext = context;
		mEngine = AppDataEngine.getInstance(context);
		GoLauncher.registMsgHandler(this);
		mNoUpdateModel = new NoPromptUpdateDataModel(mContext);
	}

	public static ApplicationManager getInstance(Context context) {
		if (sManager == null) {
			sManager = new ApplicationManager(context);
		}
		return sManager;
	}

	public void show(int entranceId, int showViewId) {
		if (mContext != null) {
			mShowWarningDialog = true;
			Intent intent = new Intent(ICustomAction.ACTION_APP_MANAGEMENT_CN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY, entranceId);
			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_VIEW_KEY, showViewId);
			mContext.startActivity(intent);
		}
	}

	public ArrayList<AppItemInfo> getAllNonSystemApps() {
		if (mEngine != null) {
			ArrayList<AppItemInfo> allApps = mEngine.getAllAppItemInfos();
			// ArrayList<AppItemInfo> allApps =
			// mEngine.getAppItemInfosExceptHide();
			ArrayList<AppItemInfo> nonSystemApps = new ArrayList<AppItemInfo>();
			for (AppItemInfo appItemInfo : allApps) {
				if (AppUtils.isAppExist(mContext, appItemInfo.mIntent)
						&& !AppUtils.isSystemApp(mContext, appItemInfo.mIntent)) {
					nonSystemApps.add(appItemInfo);
				}
			}
			return nonSystemApps;
		}
		return null;
	}

	public ArrayList<AppItemInfo> getInternalApps() {
		ArrayList<AppItemInfo> allApps = getAllNonSystemApps();
		ArrayList<AppItemInfo> internalApps = null;
		if (allApps != null && !allApps.isEmpty()) {
			internalApps = new ArrayList<AppItemInfo>();
			for (AppItemInfo appItemInfo : allApps) {
				if (AppUtils.isInternalApp(mContext, appItemInfo.mIntent)) {
					internalApps.add(appItemInfo);
				}
			}
		}
		return internalApps;
	}

	public ArrayList<AppItemInfo> getExternalApps() {
		ArrayList<AppItemInfo> allApps = getAllNonSystemApps();
		ArrayList<AppItemInfo> externalApps = null;
		if (allApps != null && !allApps.isEmpty()) {
			externalApps = new ArrayList<AppItemInfo>();
			for (AppItemInfo appItemInfo : allApps) {
				if (!AppUtils.isInternalApp(mContext, appItemInfo.mIntent)) {
					externalApps.add(appItemInfo);
				}
			}
		}
		return externalApps;
	}

	// private BitmapDrawable getDrawableFromAppItemInfo(ArrayList<AppItemInfo>
	// appItemInfos, String pkgName) {
	// if (pkgName == null
	// || appItemInfos == null || appItemInfos.size() == 0)
	// {
	// return null;
	// }
	// for (AppItemInfo appItemInfo : appItemInfos)
	// {
	// if ( pkgName.equals( appItemInfo.getAppPackageName() ))
	// {
	// return appItemInfo.mIcon;
	// }
	// }
	// return null;
	// }

	/**
	 * 获取设备上所有已安装的应用程序，区分手机和sdcard应用
	 * 
	 * @param internalApps
	 * @param externalApps
	 */
	public void getAllInstalledApp(ArrayList<AppInfo> internalApps, ArrayList<AppInfo> externalApps) {
		try {
			final PackageManager pkgmanager = mContext.getPackageManager();
			// update by zhoujun 下面方法可能会报 java.lang.RuntimeException: Package manager has died,故加异常保护 2013-03-01
			List<PackageInfo> packages = pkgmanager.getInstalledPackages(0);
			if (packages == null) {
				return;
			}
	
			AppInfo appInfo = null;
			int appCount = packages.size();
			if (appCount > 0) {
				String internalPath = Environment.getDataDirectory().getAbsolutePath();
	
				// AppDataEngine appDataEngine = GOLauncherApp.getAppDataEngine();
				// ArrayList<AppItemInfo> appItemInfos =
				// mEngine.getAllAppItemInfos();
				for (int i = 0; i < appCount; i++) {
					PackageInfo packageInfo = packages.get(i);
	
					// 获取用户安装的应用程序
					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						appInfo = new AppInfo();
	
						appInfo.mLocation = getinstallLocation(packageInfo);
						appInfo.mPackageName = packageInfo.packageName;
						appInfo.mTitle = packageInfo.applicationInfo.loadLabel(pkgmanager).toString()
								.trim();
	
						appInfo.setAppInfo(packageInfo.applicationInfo.publicSourceDir, internalPath);
	
						if (appInfo.mIsInternal) {
							internalApps.add(appInfo);
						} else {
							externalApps.add(appInfo);
						}
					}
				}
			}
		} catch (Exception e) {
			// pkgmanager.getInstalledPackages(0) 这个方法 可能会报异常。故加保护  
			// java.lang.RuntimeException: Package manager has died
		}

	}

	public void getAllInstalledApp(ArrayList<AppInfo> allApps) {
		final PackageManager pkgmanager = mContext.getPackageManager();
		List<PackageInfo> packages = pkgmanager.getInstalledPackages(0);

		if (packages == null) {
			return;
		}

		AppInfo appInfo = null;
		int appCount = packages.size();
		if (appCount > 0) {
			String internalPath = Environment.getDataDirectory().getAbsolutePath();

			for (int i = 0; i < appCount; i++) {
				PackageInfo packageInfo = packages.get(i);

				// 获取用户安装的应用程序
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					appInfo = new AppInfo();

					appInfo.mLocation = getinstallLocation(packageInfo);
					appInfo.mPackageName = packageInfo.packageName;
					appInfo.mTitle = packageInfo.applicationInfo.loadLabel(pkgmanager).toString()
							.trim();

					appInfo.setAppInfo(packageInfo.applicationInfo.publicSourceDir, internalPath);
					allApps.add(appInfo);
					// if (appInfo.mIsInternal) {
					// internalApps.add(appInfo);
					// } else {
					// externalApps.add(appInfo);
					// }
				}
			}
		}

	}

	/**
	 * 获取指定包的安装位置
	 * 
	 * @param packageInfo
	 * @return
	 */
	private int getinstallLocation(PackageInfo packageInfo) {
		try {
			Field field = packageInfo.getClass().getField("installLocation");
			Object obj = field.get(packageInfo);
			return Integer.parseInt(obj.toString());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	// /**
	// * 分别获取所有手机软件和sdcard软件
	// *
	// * @param internalApps
	// * @param externalApps
	// */
	// public void getAllApps(ArrayList<AppItemInfo> internalApps,
	// ArrayList<AppItemInfo> externalApps) {
	//
	// if (internalApps == null || externalApps == null) {
	// return;
	// }
	//
	// internalApps.clear();
	// externalApps.clear();
	//
	// ArrayList<AppItemInfo> allApps = getAllNonSystemApps();
	//
	// if (allApps != null && !allApps.isEmpty()) {
	// // boolean hasInstalledTaskmanager = false;
	// for (AppItemInfo appItemInfo : allApps) {
	// if (AppUtils.isInternalApp(mContext, appItemInfo.mIntent)) {
	// internalApps.add(appItemInfo);
	// } else {
	// externalApps.add(appItemInfo);
	// }
	//
	// // 判断是否已经安装高级任务管理器
	// String packageName = appItemInfo.getAppPackageName();
	// appItemInfo.mPackageName = packageName;
	// // if (GO_TASKMANAGE_PACKAGENAME.equals(packageName)) {
	// // if (getVersionCodeByPackageName(GO_TASKMANAGE_PACKAGENAME) >=
	// // GO_TASKMANAGE_VERSIONCODE) {
	// // hasInstalledTaskmanager = true;
	// // }
	// // }
	// }
	// // GOLauncherApp.getApplication().setHasInstalledTaskmanager(
	// // hasInstalledTaskmanager);
	// }
	// }

	// /**
	// * 获取指定包名的版本号
	// *
	// * @param packageName
	// * @return
	// */
	// private int getVersionCodeByPackageName(String packageName) {
	// PackageManager manager = mContext.getPackageManager();
	// PackageInfo info;
	// try {
	// info = manager.getPackageInfo(packageName, 0);
	// return info.versionCode;
	// } catch (NameNotFoundException e) {
	//
	// e.printStackTrace();
	// }
	// return -1;
	// }

	public void uninstallApp(String packageName) {
		if (mContext != null) {
			Uri packageURI = Uri.parse("package:" + packageName);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(uninstallIntent);
		}
	}

	public void installApp(File file) {
//		Intent intent = new Intent();
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent.setAction(android.content.Intent.ACTION_VIEW);
//		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//		mContext.startActivity(intent);
		ApkInstallUtils.installApk(file);
	}

	// public void removeOutdateFile(final String packageName, int versionCode)
	// {
	// File baseDownload = new File(BASE_DOWNLOAD_PATH);
	// File[] files = baseDownload.listFiles(new FileFilter() {
	// @Override
	// public boolean accept(File file) {
	// return file.getName().startsWith(packageName);
	// }
	// });
	// for (File file : files) {
	// String fileName = file.getName();
	// if (!fileName.endsWith(String.valueOf(versionCode))) {
	// file.delete();
	// }
	// }
	// }

	// public void restartDownload(long taskId) {
	// DownloadManager downManager = AppCore.getInstance()
	// .getDownloadManager();
	// downManager.restartDownloadById(taskId);
	// }

	public boolean checkIfVersionSyn(AppBean appBean) {
		File downloadFile = new File(GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + appBean.mPkgName
				+ "_" + appBean.mVersionName + ".apk");
		if (downloadFile.exists() && downloadFile.isFile()) {
			appBean.setStatus(AppBean.STATUS_DOWNLOAD_COMPLETED);
			appBean.setFilePath(downloadFile.getAbsolutePath());
			return true;
		}
		return false;
	}

	public void checkDownloadStatus(Context context, AppBean appBean) {
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				DownloadTask task = mDownloadController.getDownloadTaskById(appBean.mAppId);
				if (task != null) {
					int state = task.getState();
					switch (state) {
						case DownloadTask.STATE_WAIT :
							appBean.setStatus(AppBean.STATUS_WAITING_DOWNLOAD);
							break;

						case DownloadTask.STATE_START :
						case DownloadTask.STATE_DOWNLOADING :
						case DownloadTask.STATE_STOP :
						case DownloadTask.STATE_RESTART :
						case DownloadTask.STATE_FINISH :
							appBean.setStatus(AppBean.STATUS_DOWNLOADING);
							appBean.setAlreadyDownloadSize(task.getAlreadyDownloadSize());
							appBean.setAlreadyDownloadPercent(task.getAlreadyDownloadPercent());
							break;
						default :
							break;
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public String startDownload(AppBean appBean) {
		String appName = appBean.getAppName(mContext.getPackageManager());
		// 跳电子市场详情界面
		GoStoreStatisticsUtil.saveUserDataTouch(String.valueOf(appBean.mAppId), appName,
				appBean.mPkgName, true, mContext, "0", appBean.mSource, appBean.mCallbackUrl);
		String googleMarketurl = LauncherEnv.Market.APP_DETAIL + appBean.mPkgName;
		appBean.mUrlMap.put(GoStorePublicDefine.URL_TYPE_GOOGLE_MARKET, googleMarketurl);
		String downloadFileName = appBean.mPkgName + "_" + appBean.mVersionName + ".apk";
		GoStoreOperatorUtil.operateItemFTPPriority(mContext, appBean.mUrlMap, appName, true,
				appBean.mAppId, appBean.mPkgName,
				new Class[] { ApplicationDownloadListener.class }, downloadFileName,
				DownloadTask.ICON_TYPE_LOCAL, appBean.mPkgName,
				AppsDetail.START_TYPE_APPMANAGEMENT);

		// public static void operateItemFTPPriority(Context context,
		// HashMap<Integer, String> urlMap, String name, boolean isFree,
		// long id, String packageName, String iconId)
		// GoStoreOperatorUtil.downloadFileDirectly(getContext(),
		// mRecommApp.mName, mRecommApp.mDownloadurl,
		// Long.parseLong(mRecommApp.mAppId),
		// mRecommApp.mPackname,
		// new Class[] { ApplicationDownloadListener.class },
		// downloadFileName, 0, null);
		String ftpUrl = appBean.mUrlMap.get(GoStorePublicDefine.URL_TYPE_HTTP_SERVER);
		if (ftpUrl != null && ftpUrl.contains(".apk")) {
			appBean.setStatus(AppBean.STATUS_GET_READY);
		}
		return ftpUrl;
	}

	public void cancelDownload(AppBean appBean) {
		//		DownloadManager manager = AppCore.getInstance().getDownloadManager();
		//		manager.removeDownloadTaskById(appBean.mAppId);
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				DownloadTask task = mDownloadController.getDownloadTaskById(appBean.mAppId);
				if (task != null) {
					mDownloadController.removeDownloadTaskById(appBean.mAppId);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void cancelAllDownload(ArrayList<Long> downloadIdList) {
		//		Intent intent = new Intent(DownloadBrocastReceiver.ACTION_DOWNLOAD_DELETE);
		//		intent.setData(Uri.parse("download://"));
		//		intent.putExtra(DownloadManager.DOWNLOAD_TASK_IDS_KEY, downloadIdList);
		//		mContext.sendBroadcast(intent);
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				ArrayList<Long> list = new ArrayList<Long>();
				for (long id : downloadIdList) {
					if (null != mDownloadController.getDownloadTaskById(id)) {
						list.add(id);
					}
				}
				mDownloadController.removeDownloadTasksById(list);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	public void downloadAll(List<AppBean> appBeans) {
		// 统计：应用更新--改变界面入口的值
		AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(mContext,
				AppManagementStatisticsUtil.UIENTRY_TYPE_LIST);
		//		DownloadManager manager = DownloadManager.getInstance(mContext);
		//		for (AppBean appBean : appBeans) {
		//			if (appBean.getStatus() == AppBean.STATUS_NORMAL
		//					|| appBean.getStatus() == AppBean.STATUS_DOWNLOAD_FAILED) {
		//				DownloadTask task = manager.getDownloadTaskById(appBean.mAppId);
		//				if (task == null) {
		//					startDownload(appBean);
		//					// 统计：应用更新--更新点击
		//					AppManagementStatisticsUtil.getInstance().saveUpdataClick(mContext,
		//							appBean.mPkgName, appBean.mAppId, 1);
		//				}
		//			}
		//		}
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.getStatus() == AppBean.STATUS_NORMAL
							|| appBean.getStatus() == AppBean.STATUS_DOWNLOAD_FAILED) {
						DownloadTask task = mDownloadController.getDownloadTaskById(appBean.mAppId);
						if (task == null) {
							startDownload(appBean);
							// 统计：应用更新--更新点击
							AppManagementStatisticsUtil.getInstance().saveUpdataClick(mContext,
									appBean.mPkgName, appBean.mAppId, 1);
						}
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void actionDownload(Context context, final IDownloadInvoker invoker) {
		if (GoStorePhoneStateUtil.isNetWorkAvailable(mContext)) {
			if (GoStorePhoneStateUtil.isWifiEnable(mContext)) {
				if (invoker != null) {
					invoker.invokeDownload();
				}
			} else {
				if (mShowWarningDialog) {
					mShowWarningDialog = false;
					new AlertDialog.Builder(context)
							.setTitle(R.string.apps_management_network_warning_title)
							.setMessage(R.string.apps_management_network_warning_message)
							.setPositiveButton(R.string.update,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											if (invoker != null) {
												invoker.invokeDownload();
											}
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
										}
									}).show();
				} else {
					if (invoker != null) {
						invoker.invokeDownload();
					}
				}
			}
		} else {
			DeskToast
					.makeText(mContext, R.string.apps_management_network_error, Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 */
	public interface IDownloadInvoker {
		public void invokeDownload();
	}

	@Override
	public int getId() {
		return IDiyFrameIds.APP_MANAGER;
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		Message msg = new Message();
		msg.what = AppsManagementActivity.NONE;
		switch (msgId) {
			case IDiyMsgIds.EVENT_INSTALL_APP :
			case IDiyMsgIds.EVENT_INSTALL_PACKAGE :
			case IDiyMsgIds.EVENT_UPDATE_PACKAGE :
				msg.obj = object;
				msg.what = AppsManagementActivity.INSTALL_APPS;
				break;
			case IDiyMsgIds.EVENT_UNINSTALL_APP :
				msg.obj = object;
				msg.what = AppsManagementActivity.UNINSTALL_APPS;
				break;
			case IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK :
				msg.what = AppsManagementActivity.SDCARD_IS_OK;
				break;
			case IDiyMsgIds.EVENT_APPS_LIST_UPDATE :
				msg.what = AppsManagementActivity.APP_LIST_UPDATE;
				AppsBean appsBean = (AppsBean) object;
				mUpdateAppsCount = appsBean.mListBeans.size();
				break;
			case IDiyMsgIds.EVENT_APPS_LIST_UPDATE_NOTIFICATION :
				appsBean = (AppsBean) object;
				if (appsBean != null && !appsBean.mListBeans.isEmpty()) {
					// add by zhoujun 2010--0-14, 控制是否显示
					if (appsBean.mControlcontrolMap != null
							&& !appsBean.mControlcontrolMap.isEmpty()
							&& appsBean.mControlcontrolMap.get(1) == 0) {
						return false;
					}
					sendUpdateInfoToNotification(appsBean.mListBeans);
				}
				break;
		}
		if (mAppsManagementHandler != null && msg.what != AppsManagementActivity.NONE) {
			mAppsManagementHandler.handleMessage(msg);
		}
		return false;
	}

	// private void sendUpdateInfoToNotification(List<AppBean> appBeans) {
	// Resources res = mContext.getResources();
	// NotificationManager notificationManager = (NotificationManager) mContext
	// .getSystemService(Context.NOTIFICATION_SERVICE);
	// RemoteViews notificationRemoteViews = new RemoteViews(
	// mContext.getPackageName(),
	// android.R.layout.);
	// Intent intent = new Intent(ACTION_APP_MANAGEMENT);
	// PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
	// intent, PendingIntent.FLAG_CANCEL_CURRENT);
	// String updateInfo = appBeans.size()
	// + " "
	// +
	// res.getString(R.string.apps_management_notification_title_update_info_suffix);
	// Notification notification = new Notification(R.drawable.icon,
	// updateInfo, System.currentTimeMillis());
	// notification.contentIntent = pendingIntent;
	// notification.flags = Notification.FLAG_ONGOING_EVENT;
	// notificationRemoteViews.setCharSequence(R.id.update_info_view,
	// "setText", updateInfo);
	// SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	// String datetime = format.format(new Date());
	// notificationRemoteViews.setCharSequence(R.id.datetime_view, "setText",
	// datetime);
	// PackageManager pkgMgr = mContext.getPackageManager();
	// StringBuilder sb = new StringBuilder();
	// for (AppBean appBean : appBeans) {
	// sb.append(appBean.getAppName(pkgMgr)).append(", ");
	// }
	// sb.delete(sb.lastIndexOf(","), sb.length());
	// notificationRemoteViews.setCharSequence(R.id.app_info_view, "setText",
	// sb.toString());
	// notification.contentView = notificationRemoteViews;
	// notificationManager.notify(ApplicationManager.NOTIFY_TAG,
	// new Random().nextInt(), notification);
	// }

	private void sendUpdateInfoToNotification(List<AppBean> appBeans) {
		Resources res = mContext.getResources();
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(AppsManagementConstants.NOTIFY_TAG,
				AppsManagementConstants.NOTIFY_UPDATE_INFO_ID);
		Intent intent = new Intent(ICustomAction.ACTION_APP_MANAGEMENT_CN);
		intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_VIEW_KEY,
				AppsManageView.APPS_UPDATE_VIEW_ID);
		intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_START_TYPE_KEY,
				AppsManageView.APPS_START_TYPE);
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

	/**
	 * 根据包名，添加忽略更新的应用
	 * 
	 * @param packageName
	 */
	public void addNoUpdateApp(String packageName) {
		if (mNoUpdateModel != null && packageName != null) {
			Intent intent = findActivitiesForPackage(packageName);
			mNoUpdateModel.addNoUpdateApp(intent);
		}
	}

	/**
	 * 删除忽略更新的应用
	 * 
	 * @param packageName
	 */
	public void deleteNoUpdateApp(String packageName) {
		if (mNoUpdateModel != null) {
			Intent intent = null;
			if (packageName != null) {
				intent = findActivitiesForPackage(packageName);
				if (intent == null) {
					Log.e("NoPromptUpdateDataModel",
							" deleteNoUpdateApp delete app fault !!!!!!!!!!!!!!!!!!!!!!!!!!");
					return;
				}
			}
			// 当packageName为null时，删除所有应用
			mNoUpdateModel.deleteNoUpdateApp(intent);
		}
	}

	public ArrayList<NoPromptUpdateInfo> getAllNoPromptUpdateApp() {
		if (mNoUpdateModel != null) {
			return mNoUpdateModel.getAllNoPromptUpdateApp();
		}
		return null;
	}

	/**
	 * 找到包名对应的ResolveInfo列表
	 * 
	 * @param context
	 *            上下文
	 * @param packageName
	 *            包名
	 * @return 匹配的ResolveInfo列表
	 */
	private Intent findActivitiesForPackage(String packageName) {
		final PackageManager packageManager = mContext.getPackageManager();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
		Intent intent = null;
		if (apps != null) {
			// Find all activities that match the packageName
			int count = apps.size();
			ResolveInfo info = null;
			ActivityInfo activityInfo = null;
			for (int i = 0; i < count; i++) {
				info = apps.get(i);
				if (info != null) {
					activityInfo = info.activityInfo;
					if (activityInfo != null && packageName.equals(activityInfo.packageName)) {
						if (activityInfo.name != null) {
							ComponentName c = new ComponentName(packageName, activityInfo.name);
							intent = new Intent(Intent.ACTION_MAIN);
							intent.setComponent(c);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
							break;
						}
					}
				}

			}
		}
		return intent;
	}

	public Handler getHandler() {
		return mAppsManagementHandler;
	}

	public void setHandler(Handler handler) {
		mAppsManagementHandler = handler;
	}

	public int getUpdateAppsCount() {
		return mUpdateAppsCount;
	}

	public void setUpdateAppsCount(int updateCount) {
		mUpdateAppsCount = updateCount;
	}
}
