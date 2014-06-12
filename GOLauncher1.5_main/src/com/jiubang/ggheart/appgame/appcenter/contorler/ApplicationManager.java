package com.jiubang.ggheart.appgame.appcenter.contorler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.bean.NoPromptUpdateInfo;
import com.jiubang.ggheart.appgame.appcenter.help.NoPromptUpdateDataModel;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 */
public class ApplicationManager {

	private static ApplicationManager sManager;

	private Context mContext;

	private static boolean sShowWarningDialog = false;

	// 忽略更新数据持久化执行者
	protected NoPromptUpdateDataModel mNoUpdateModel = null;

	private ApplicationManager() {

	}

	private ApplicationManager(Context context) {
		mContext = context;
		mNoUpdateModel = new NoPromptUpdateDataModel(mContext);
	}

	public static ApplicationManager getInstance(Context context) {
		if (sManager == null) {
			sManager = new ApplicationManager(context);
		}
		return sManager;
	}

	public boolean checkIfVersionSyn(AppBean appBean) {
		File downloadFile = new File(GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + appBean.mPkgName
				+ "_" + appBean.mVersionName + ".apk");
		if (downloadFile.exists() && downloadFile.isFile()) {
			appBean.setStatus(AppBean.STATUS_DOWNLOAD_COMPLETED);
			appBean.setFilePath(downloadFile.getAbsolutePath());
			return true;
		} else {
			if (appBean.getStatus() == AppBean.STATUS_DOWNLOAD_COMPLETED) {
				appBean.setStatus(AppBean.STATUS_NORMAL);
			}
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

	public String startDownload(AppBean appBean, int iconType, String iconUrlInfo, int module) {
		String appName = appBean.getAppName(mContext.getPackageManager());
		// 跳电子市场详情界面
		GoStoreStatisticsUtil.saveUserDataTouch(String.valueOf(appBean.mAppId), appName,
				appBean.mPkgName, true, mContext, "0", appBean.mSource, appBean.mCallbackUrl);
		String googleMarketurl = LauncherEnv.Market.APP_DETAIL + appBean.mPkgName;
		appBean.mUrlMap.put(GoStorePublicDefine.URL_TYPE_GOOGLE_MARKET, googleMarketurl);
		String downloadFileName = appBean.mPkgName + "_" + appBean.mVersionName + ".apk";
		GoStoreOperatorUtil.operateItemFTPPriority(mContext, appBean.mUrlMap, appName, true,
				appBean.mAppId, appBean.mPkgName, new Class[] { AppDownloadListener.class},
				downloadFileName, iconType, iconUrlInfo, module);
		String ftpUrl = appBean.mUrlMap.get(GoStorePublicDefine.URL_TYPE_HTTP_SERVER);
		if (ftpUrl != null && ftpUrl.contains(".apk")) {
			appBean.setStatus(AppBean.STATUS_WAITING_DOWNLOAD);
		}
		return ftpUrl;
	}

	public void cancelDownload(AppBean appBean) {
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
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.mAppId > 0
							&& (appBean.getStatus() == AppBean.STATUS_NORMAL || appBean.getStatus() == AppBean.STATUS_DOWNLOAD_FAILED)) {
						DownloadTask task = mDownloadController.getDownloadTaskById(appBean.mAppId);
						if (task == null) {
							startDownload(appBean, DownloadTask.ICON_TYPE_LOCAL, appBean.mPkgName,
									AppsDetail.START_TYPE_APPMANAGEMENT);
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
				if (sShowWarningDialog) {
					sShowWarningDialog = false;
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
	 * 
	 */
	public interface IDownloadInvoker {
		public void invokeDownload();
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
}
