package com.jiubang.ggheart.apps.gowidget.gostore.util;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.download.DefaultDownloadListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.appgame.gostore.base.component.AppsThemeDetailActivity;
import com.jiubang.ggheart.apps.gowidget.gostore.bean.BaseItemBean;
import com.jiubang.ggheart.apps.gowidget.gostore.bean.DetailItemBean;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.url.FTPUrlOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.util.url.GoogleMarketUrlOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.util.url.OtherUrlOperator;
import com.jiubang.ggheart.billing.IPurchaseStateListener;
import com.jiubang.ggheart.billing.PurchaseSupportedManager;
import com.jiubang.ggheart.billing.ThemeAppInBillingManager;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.statistics.GoStoreAppStatistics;
import com.jiubang.ggheart.data.theme.zip.ZipResources;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 */
public class GoStoreOperatorUtil {

	// 下载文件保存目录
	public final static String DOWNLOAD_DIRECTORY_PATH = Environment.getExternalStorageDirectory()
			+ "/GoStore/download/";

	// 付费方式
	public final static int PAY_MARKET = 0; // 普通谷歌收费
	public final static int PAY_INAPP = 1; // 应用内付费
	public final static int PAY_GETJAR = 2; // getjar
	public final static int PAY_VIP = 4; // getjar

	// 付费监听KEY
	public final static String LISTENER_KEY_INAPP = "inapplistener";

	/**
	 * 下载软件的方法
	 * 
	 * @param context
	 * @param detailItemBean
	 */
	public static void operateItem(Context context, DetailItemBean detailItemBean) {
		if (context != null && detailItemBean != null) {
			HashMap<Integer, String> urlMap = new HashMap<Integer, String>(2);
			urlMap.put(GoStorePublicDefine.URL_TYPE_HTTP_SERVER, detailItemBean.getDownurl());
			urlMap.put(GoStorePublicDefine.URL_TYPE_GOOGLE_MARKET, detailItemBean.getMarketurl());
			urlMap.put(GoStorePublicDefine.URL_TYPE_OTHER_ADDRESS, detailItemBean.getOtherurl());
			operateItem(context, urlMap, detailItemBean.getItemNameString(),
					detailItemBean.isFree(), detailItemBean.getId(), detailItemBean.getPkgName(),
					DownloadTask.ICON_TYPE_ID, detailItemBean.getItemIconBitmapId(),
					detailItemBean.getVerCode());
		}
	}

	/**
	 * 下载软件的方法
	 * 
	 * @param context
	 * @param baseItemBean
	 */
	public static void operateItem(Context context, BaseItemBean baseItemBean) {
		if (context != null && baseItemBean != null) {
			operateItem(context, baseItemBean.mUrlMap, baseItemBean.getItemNameString(),
					baseItemBean.isFree(), baseItemBean.getAdID(), baseItemBean.getPkgName(),
					DownloadTask.ICON_TYPE_ID, baseItemBean.getItemIconId(),
					baseItemBean.getVerCode());
		}
	}

	/**
	 * 下载软件的方法
	 * 
	 * @author huyong
	 * @param adOptDataString
	 *            下载软件的操作信息
	 */
	public static void operateItem(Context context, HashMap<Integer, String> urlMap, String name,
			boolean isFree, long id, String packageName, int iconType, String iconInfo, int verCode) {
		// 参数检查
		if (null == context) {
			return;
		}
		if (null == urlMap || urlMap.size() == 0) {
			Toast.makeText(context, R.string.themestore_url_empty, Toast.LENGTH_LONG).show();
			return;
		}
		// 谷歌电子市场处理
		GoogleMarketUrlOperator googleMarketUrlOperator = GoogleMarketUrlOperator.getInstance();
		// 其它地址处理
		OtherUrlOperator otherUrlOperator = OtherUrlOperator.getInstance();
		// FTP地址处理
		FTPUrlOperator ftpUrlOperator = FTPUrlOperator.getInstance();
		ftpUrlOperator.setVerCode(verCode);
		ftpUrlOperator.setFree(isFree);
		ftpUrlOperator.setName(name);
		ftpUrlOperator.setIconInfo(iconInfo);
		ftpUrlOperator.setIconType(iconType);
		ftpUrlOperator.setId(id);
		ftpUrlOperator.setPackageName(packageName);
		otherUrlOperator.setUrlOperator(ftpUrlOperator);
		googleMarketUrlOperator.setUrlOperator(otherUrlOperator);
		if (!googleMarketUrlOperator.handleUrl(context, urlMap)) {
			Toast.makeText(context, R.string.themestore_url_fail, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 下载软件的方法,FTP优先
	 * 
	 * @author huyong
	 * @param adOptDataString
	 *            下载软件的操作信息
	 */
	public static void operateItemFTPPriority(Context context, HashMap<Integer, String> urlMap,
			String name, boolean isFree, long id, String packageName, String iconId) {
		// 参数检查
		if (null == context) {
			return;
		}
		if (null == urlMap || urlMap.size() == 0) {
			Toast.makeText(context, R.string.themestore_url_empty, Toast.LENGTH_LONG).show();
			return;
		}
		FTPUrlOperator ftpUrlOperator = initOperators(name, isFree, id, packageName,
				DownloadTask.ICON_TYPE_ID, iconId);
		if (!ftpUrlOperator.handleUrl(context, urlMap)) {
			Toast.makeText(context, R.string.themestore_url_fail, Toast.LENGTH_LONG).show();
		}
	}

	public static void operateItemFTPPriority(Context context, HashMap<Integer, String> urlMap,
			String name, boolean isFree, long id, String packageName,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName, int iconType, String iconUrlInfo, int module) {
		// 参数检查
		if (null == context) {
			return;
		}
		if (null == urlMap || urlMap.size() == 0) {
			Toast.makeText(context, R.string.themestore_url_empty, Toast.LENGTH_LONG).show();
			return;
		}
		FTPUrlOperator ftpUrlOperator = initOperators(name, isFree, id, packageName, iconType,
				iconUrlInfo);
		if (!ftpUrlOperator.handleUrl(context, urlMap, listenerClazzArray, customDownloadFileName,
				iconType, iconUrlInfo, module)) {
			Toast.makeText(context, R.string.themestore_url_fail, Toast.LENGTH_LONG).show();
		}
	}

	private static FTPUrlOperator initOperators(String name, boolean isFree, long id,
			String packageName, int iconType, String iconInfo) {
		// 谷歌电子市场处理
		GoogleMarketUrlOperator googleMarketUrlOperator = GoogleMarketUrlOperator.getInstance();
		// 其它地址处理
		OtherUrlOperator otherUrlOperator = OtherUrlOperator.getInstance();
		// FTP地址处理
		FTPUrlOperator ftpUrlOperator = FTPUrlOperator.getInstance();
		ftpUrlOperator.setFree(isFree);
		ftpUrlOperator.setName(name);
		ftpUrlOperator.setIconInfo(iconInfo);
		ftpUrlOperator.setIconType(iconType);
		ftpUrlOperator.setId(id);
		ftpUrlOperator.setPackageName(packageName);
		googleMarketUrlOperator.setUrlOperator(otherUrlOperator);
		ftpUrlOperator.setUrlOperator(googleMarketUrlOperator);
		return ftpUrlOperator;
	}

	/**
	 * 去市场查找APK的方法
	 * 
	 * @param uriString
	 *            地址
	 * @param isGotoMarket
	 *            是否跳转到市场
	 */
	public static void gotoMarketForAPK(Context context, String uriString) {
		if (uriString == null) {
			uriString = GoStorePublicDefine.GOLAUNCHER_THEME_SITE_URL;
		}
		// 如果满足去电子市场的条件
		Intent mEMarketintent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
		mEMarketintent.setPackage(LauncherEnv.Market.PACKAGE);
		mEMarketintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (context != null) {
			try {
				context.startActivity(mEMarketintent);
			} catch (ActivityNotFoundException e) {
				gotoBrowser(context, uriString);
			}
		}

	}

	/**
	 * 去市场查找APK的方法
	 * 
	 * @author huyong
	 * @param context
	 * @param uriString
	 * @return
	 */
	public static boolean gotoMarket(Context context, String uriString) {
		boolean result = false;
		if (context == null || uriString == null || "".equals(uriString.trim())) {
			return result;
		}
		Intent mEMarketintent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
		mEMarketintent.setPackage("com.android.vending");
		mEMarketintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(mEMarketintent);
			result = true;
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 去市场查找APK的方法(木瓜专用）
	 * 
	 * @author zhouxuewen
	 * @param context
	 * @param uriString
	 * @return
	 */
	public static boolean gotoMarket(final Context context, String uriString, String callBackUrl) {
		boolean result = false;
		if (context == null || uriString == null || "".equals(uriString.trim())) {
			return result;
		}
		try {
//			Activity activity = GoMarketPublicUtil.getInstance(context).getActivity();
//			AppFlood.handleAFClick(activity, callBackUrl, uriString);
			result = true;
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 用浏览器访问Http
	 * 
	 * @param context
	 * @param uriString
	 *            访问地址
	 */
	public static void gotoBrowser(Context context, String uriString) {
		if (context == null || uriString == null || "".equals(uriString.trim())) {
			return;
		}
		Uri browserUri = Uri.parse(uriString);
		if (null != browserUri) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
			browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(browserIntent);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 下载普通文件方法
	 * 
	 * @param context
	 * @param fileName
	 *            在通知条显示的文件名字
	 * @param path
	 * @param downloadUrl
	 * @param id
	 * @param customDownloadFileName
	 *            下载到本地后所保存的文件名字
	 */
	public static long downloadFileDirectly(Context context, String fileName, String path,
			String downloadUrl, long id, String customDownloadFileName) {
		return downloadFileDirectly(context, fileName, path, downloadUrl, id,
				customDownloadFileName, false);
	}
	/**
	 * 下载普通文件方法
	 * 
	 * @param context
	 * @param fileName
	 *            在通知条显示的文件名字
	 * @param path
	 * @param downloadUrl
	 * @param id
	 * @param customDownloadFileName
	 *            下载到本地后所保存的文件名字
	 * @param isApk
	 */
	public static long downloadFileDirectly(Context context, String fileName, String path,
			String downloadUrl, long id, String customDownloadFileName, boolean isApk) {
		long taskId = downloadFileDirectly(context, fileName, path, downloadUrl, id,
				customDownloadFileName, isApk, 0, new Class[] { AppDownloadListener.class });
		return taskId;
	}

	public static long downloadFileDirectly(Context context, String fileName, String path,
			String downloadUrl, long id, String customDownloadFileName, boolean isApk, int module,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray) {
		long taskId = -1;
		if (context == null || fileName == null || "".equals(fileName.trim())
				|| downloadUrl == null || "".equals(downloadUrl.trim())) {
			return taskId;
		}
		fileName = fileName.trim();
		downloadUrl = downloadUrl.trim();
		String saveFilePath = null;
		if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
			saveFilePath = path + customDownloadFileName;
		} else {
			saveFilePath = path + fileName + System.currentTimeMillis() + ".apk";
		}
		String[] listenerClassNames = null;
		if (listenerClazzArray != null && listenerClazzArray.length > 0) {
			listenerClassNames = new String[listenerClazzArray.length];
			for (int i = 0; i < listenerClassNames.length; i++) {
				listenerClassNames[i] = listenerClazzArray[i].getName();
			}
		}
		DownloadTask task = null;;
		if (module == 0) {
			task = new DownloadTask(id, downloadUrl, fileName, saveFilePath);
		} else {
			task = new DownloadTask(id, downloadUrl, fileName, saveFilePath, null, 0, null, module);
		}
		task.setIsApkFile(isApk);
		IDownloadService mController = GOLauncherApp.getApplication().getDownloadController();
		try {
			if (mController != null) {
				taskId = mController.addDownloadTask(task);
				if (taskId != -1) {
					if (listenerClassNames != null && listenerClassNames.length > 0) {
						for (String name : listenerClassNames) {
							mController.addDownloadTaskListenerByName(taskId, name);
						}
					} else {
						// 添加默认下载监听器
						mController.addDownloadTaskListenerByName(taskId,
								DefaultDownloadListener.class.getName());
					}
					mController.startDownload(taskId);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return taskId;
	}
	
	public static long downloadFileDirectly(Context context, String fileName, String path,
			String downloadUrl, long id, String customDownloadFileName, boolean isApk, int module) {
		long taskId = -1;
		if (context == null || fileName == null || "".equals(fileName.trim())
				|| downloadUrl == null || "".equals(downloadUrl.trim())) {
			return taskId;
		}
		fileName = fileName.trim();
		downloadUrl = downloadUrl.trim();
		String saveFilePath = null;
		if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
			saveFilePath = path + customDownloadFileName;
		} else {
			saveFilePath = path + fileName + System.currentTimeMillis() + ".apk";
		}
		DownloadTask task = null;;
		if (module == 0) {
			task = new DownloadTask(id, downloadUrl, fileName, saveFilePath);
		} else {
			task = new DownloadTask(id, downloadUrl, fileName, saveFilePath, null, 0, null, module);
		}
		task.setIsApkFile(isApk);
		IDownloadService mController = GOLauncherApp.getApplication().getDownloadController();
		try {
			if (mController == null) {
				return -1;
			}
			taskId = mController.addDownloadTask(task);
			if (taskId != -1) {
				// 添加默认的下载监听器
				mController.addDownloadTaskListenerByName(taskId,
						DefaultDownloadListener.class.getName());
				mController.startDownload(taskId);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return taskId;
	}

	/**
	 * 直接下载APK并执行安装的方法
	 * 
	 * @param context
	 * @param fileName
	 * @param downloadUrl
	 * @param id
	 */
	public static long downloadFileDirectly(Context context, String fileName, String downloadUrl,
			long id, String packageName, String customDownloadFileName, int iconType,
			String iconInfo) {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		long taskId = -1;
		if (!sdCardExist) {
			Toast.makeText(context, R.string.gostore_no_sdcard, Toast.LENGTH_SHORT).show();
			return taskId;
		}
		if (context == null || fileName == null || "".equals(fileName.trim())
				|| downloadUrl == null || "".equals(downloadUrl.trim())) {
			return taskId;
		}
		fileName = fileName.trim();
		downloadUrl = downloadUrl.trim();
		String saveFilePath = null;
		if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
			saveFilePath = customDownloadFileName;
		} else {
			saveFilePath = DOWNLOAD_DIRECTORY_PATH + fileName + System.currentTimeMillis() + ".apk";
		}
		DownloadTask task = new DownloadTask(id, downloadUrl, fileName, saveFilePath, packageName,
				iconType, iconInfo);
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				taskId = mDownloadController.addDownloadTask(task);
				if (taskId != -1) {
					// 添加默认的下载监听器
					mDownloadController.addDownloadTaskListenerByName(taskId,
							DefaultDownloadListener.class.getName());
					mDownloadController.startDownload(taskId);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return taskId;
	}

	public static long downloadFileDirectly(Context context, String fileName, String downloadUrl,
			long id, String packageName,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName, int iconType, String iconInfo) {
		long taskId = -1;
		if (context == null || fileName == null || "".equals(fileName.trim())
				|| downloadUrl == null || "".equals(downloadUrl.trim())) {
			return taskId;
		}
		fileName = fileName.trim();
		downloadUrl = downloadUrl.trim();
		String saveFilePath = null;
		if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
			saveFilePath = DOWNLOAD_DIRECTORY_PATH + customDownloadFileName;
		} else {
			saveFilePath = DOWNLOAD_DIRECTORY_PATH + fileName + System.currentTimeMillis() + ".apk";
		}
		String[] listenerClassNames = null;
		if (listenerClazzArray != null && listenerClazzArray.length > 0) {
			listenerClassNames = new String[listenerClazzArray.length];
			for (int i = 0; i < listenerClassNames.length; i++) {
				listenerClassNames[i] = listenerClazzArray[i].getName();
			}
		}
		DownloadTask task = new DownloadTask(id, downloadUrl, fileName, saveFilePath, packageName,
				iconType, iconInfo);
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				taskId = mDownloadController.addDownloadTask(task);
				if (taskId != -1) {
					// 添加下载监听器
					if (listenerClassNames != null && listenerClassNames.length > 0) {
						for (String name : listenerClassNames) {
							mDownloadController.addDownloadTaskListenerByName(taskId, name);
						}
					} else {
						// 添加默认下载监听器
						mDownloadController.addDownloadTaskListenerByName(taskId,
								DefaultDownloadListener.class.getName());
					}
					// 开始下载
					mDownloadController.startDownload(taskId);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return taskId;
	}

	/**
	 * 功能简述:重载方法，加入图标类型和图标Url信息 功能详细描述: 注意:
	 * 
	 * @param context
	 * @param fileName
	 * @param downloadUrl
	 * @param id
	 * @param packageName
	 * @param listenerClazzArray
	 * @param customDownloadFileName
	 * @param iconType
	 *            图标类型
	 * @param iconUrl
	 *            图标url信息
	 */
	public static long downloadFileDirectly(Context context, String fileName, String downloadUrl,
			long id, String packageName,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName, int iconType, String iconUrl, int module) {
		long taskId = -1;
		if (context == null || fileName == null || "".equals(fileName.trim())
				|| downloadUrl == null || "".equals(downloadUrl.trim())) {
			return taskId;
		}
		fileName = fileName.trim();
		downloadUrl = downloadUrl.trim();
		String saveFilePath = null;
		if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
			saveFilePath = DOWNLOAD_DIRECTORY_PATH + customDownloadFileName;
		} else {
			saveFilePath = DOWNLOAD_DIRECTORY_PATH + fileName + System.currentTimeMillis() + ".apk";
		}
		String[] listenerClassNames = null;
		if (listenerClazzArray != null && listenerClazzArray.length > 0) {
			listenerClassNames = new String[listenerClazzArray.length];
			for (int i = 0; i < listenerClassNames.length; i++) {
				listenerClassNames[i] = listenerClazzArray[i].getName();
			}
		}
		DownloadTask task = new DownloadTask(id, downloadUrl, fileName, saveFilePath, packageName,
				iconType, iconUrl, module);
//		task.setTreatment(treatment);
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				taskId = mDownloadController.addDownloadTask(task);
				if (taskId != -1) {
					if (listenerClassNames != null && listenerClassNames.length > 0) {
						for (String name : listenerClassNames) {
							mDownloadController.addDownloadTaskListenerByName(taskId, name);
						}
					} else {
						// 添加默认下载监听器
						mDownloadController.addDownloadTaskListenerByName(taskId,
								DefaultDownloadListener.class.getName());
					}
					mDownloadController.startDownload(taskId);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return taskId;
	}

	/**
	 * <br>功能简述:重载方法，加入图标类型和图标Url信息 
	 * <br>功能详细描述:
	 * <br>注意: 此方法不会添加默认的Listener，使用需要根据返回的taskId加入listener
	 * @param context
	 * @param fileName
	 * @param downloadUrl
	 * @param id
	 * @param packageName
	 * @param customDownloadFileName
	 * @param iconType
	 * @param iconUrl
	 * @param module
	 * @return
	 */
	public static long downloadFileDirectly(Context context, String fileName, String downloadUrl,
			long id, String packageName, String customDownloadFileName, int iconType,
			String iconUrl, int module) {
		long taskId = -1;
		if (context == null || fileName == null || "".equals(fileName.trim())
				|| downloadUrl == null || "".equals(downloadUrl.trim())) {
			return taskId;
		}
		fileName = fileName.trim();
		downloadUrl = downloadUrl.trim();
		String saveFilePath = null;
		if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
			saveFilePath = DOWNLOAD_DIRECTORY_PATH + customDownloadFileName;
		} else {
			saveFilePath = DOWNLOAD_DIRECTORY_PATH + fileName + System.currentTimeMillis() + ".apk";
		}
		DownloadTask task = new DownloadTask(id, downloadUrl, fileName, saveFilePath, packageName,
				iconType, iconUrl, module);
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				taskId = mDownloadController.addDownloadTask(task);
				if (taskId != -1) {
					mDownloadController.startDownload(taskId);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return taskId;
	}

	/**
	 * <br>功能简述: 自动选择方法去下载
	 * <br>功能详细描述:不是具体的下载，而是去下载的触发界面，比如电子市场或第三方下载界面
	 * <br>注意:
	 * @param context	
	 * @param itemBean		应用数据
	 * @param paylistener	付费回调
	 */
	public static void gotoPay(Context context, AppDetailInfoBean itemBean,
			Object paylistener) {
		int[] payTypes = itemBean.mPayType;
		if (payTypes != null && payTypes.length > 0) {
			gotoOnePay(context, itemBean, payTypes[0], paylistener);
		}
	}
	
	/**
	 * 只有一种付费方式的处理方法
	 * @param context
	 * @param itemBean
	 * @param payType
	 * @param paylistener
	 */
	public static void gotoOnePay(Context context, AppDetailInfoBean itemBean, int payType, Object paylistener) {
		switch (payType) {
		case PAY_INAPP :
			IPurchaseStateListener purchaseListener = (IPurchaseStateListener) paylistener;
			gotoPayForInApp(context, itemBean, purchaseListener);
			break;
		default :
			break;
		}
	}
	
	/**
	 * <br>
	 * 功能简述:下载内付费的主题包 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param infoBean
	 */
	public static void startDownloadZip(Context context, String url, String name, String pkgname,
			long appid) {
		if (url == null || url.equals("")) {
			return;
		}
		if (!android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			DeskToast
					.makeText(context, R.string.import_export_sdcard_unmounted, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String downLoadUrl = url + "&imei=" + GoStorePhoneStateUtil.getVirtualIMEI(context);
		IDownloadService mController = GOLauncherApp.getApplication().getDownloadController();
		try {
			DownloadTask task = mController.getDownloadTaskById(appid);
			if (task != null && task.getDownloadUrl().equals(downLoadUrl)) {
				Toast.makeText(context, R.string.themestore_downloading, 600).show();
				return;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//统计安装量
		GoStoreAppStatistics.getInstance(context).onAppInstalled(pkgname);
		GoStoreOperatorUtil.downloadFileDirectly(GOLauncherApp.getContext(), name,
				LauncherEnv.Path.GOT_ZIP_HEMES_PATH, downLoadUrl, appid, pkgname
						+ ZipResources.ZIP_POSTFIX, false);
	}
	
	/**
	 * 应用内付费的付费方式
	 * @param context
	 * @param itemBean
	 * @param payListener
	 */
	public static void gotoPayForInApp(Context context, AppDetailInfoBean itemBean, IPurchaseStateListener payListener) {
		if (payListener != null) {
			String pkgName = itemBean.mPkgName;
			String payId = itemBean.mPayId;
			
			ThemeAppInBillingManager inBillingManager = ThemeAppInBillingManager
					.getInstance(context);
			inBillingManager.requestPurchase(pkgName, payId, payListener);
		}
	}

	/**
	 * <br>功能简述: 自动选择方法去下载
	 * <br>功能详细描述:不是具体的下载，而是去下载的触发界面，比如电子市场或第三方下载界面
	 * <br>注意:
	 * @param context
	 * @param itemBean
	 * @param sortId
	 */
	public static void gotoDownloadForPay(Context context, BaseItemBean itemBean, String sortId,
			HashMap<String, Object> listenerList) {
		String[] payTypes = itemBean.getPaytype();
		if (payTypes != null && payTypes.length > 0) {
			if (payTypes.length == 1) {
				gotoDetailOnePay(context, itemBean, payTypes[0], sortId, listenerList);
			} else {
				//				selectPayType(context, itemBean, sortId, listenerList);
				gotoDetailOnePay(context, itemBean, payTypes[0], sortId, listenerList);
			}
		} else {
			gotoDetail(context, itemBean, sortId);
		}
	}

	/**
	 * <br>功能简述: 自动选择方法去下载
	 * <br>功能详细描述:不是具体的下载，而是去下载的触发界面，比如电子市场或第三方下载界面
	 * <br>注意:
	 * @param context
	 * @param itemBean
	 * @param sortId
	 */
	public static void gotoDownloadForPay(Activity context, DetailItemBean itemBean, String sortId,
			HashMap<String, Object> listenerList) {
		String[] payTypes = itemBean.getPaytype();
		if (payTypes != null && payTypes.length > 0) {
			if (payTypes.length == 1) {
				gotoDetailOnePay(context, itemBean, payTypes[0], sortId, listenerList);
			} else {
				//				selectPayType(context, itemBean, sortId, listenerList);
				gotoDetailOnePay(context, itemBean, payTypes[0], sortId, listenerList);
			}
		} else {
			GoStoreStatisticsUtil.saveUserDataTouch(itemBean, false, context, sortId);
			GoStoreOperatorUtil.operateItem(context, itemBean);
		}
	}

	/**
	 * <br>功能简述:使用BaseItemBean进行应用跳转或下载的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param itemBean
	 * @param sortId
	 */
	public static void gotoDetail(Context context, BaseItemBean itemBean, String sortId) {
		Intent intent = new Intent();
		int style = itemBean.getStyle();
		if (style == GoStorePublicDefine.GOSTORE_DETAIL_BIG_STYLE) {
//			intent.setClass(context, GoStoreThemeDetailActivity.class);
		} else {
			intent.setClass(context, AppsThemeDetailActivity.class);
		}
		String itemId = String.valueOf(itemBean.getAdID());
		// 详情的url地址
		HashMap<Integer, String> urlMap = itemBean.getUrlMap();
		String itemUrl = null;
		String marketUrl = null;
		String otherUrl = null;
		if (urlMap != null) {
			itemUrl = urlMap.get(3);
			marketUrl = urlMap.get(2);
			otherUrl = urlMap.get(GoStorePublicDefine.URL_TYPE_OTHER_ADDRESS);
		}
		if (otherUrl != null && !otherUrl.equals("")) {
			GoStoreStatisticsUtil.saveUserDataDetailShow(String.valueOf(itemBean.getAdID()),
					itemBean.getItemNameString(), context, sortId);
			GoStoreStatisticsUtil.saveUserDataTouch(itemBean, GoStoreAppInforUtil
					.isNewToAlreadyInstall(context, itemBean.getPkgName(), itemBean.getVerCode()),
					context, sortId);
			GoStoreOperatorUtil.gotoBrowser(context, otherUrl);
			return;
		}
		String[] paytypes = itemBean.getPaytype();
		if (itemUrl != null) {
			intent.putExtra(GoStorePublicDefine.ITEM_URL, itemUrl);
		} else {
			boolean isInApp = isInApp(paytypes);
			if (!(marketUrl == null || isInApp)) {
				GoStoreStatisticsUtil.saveUserDataDetailShow(String.valueOf(itemBean.getAdID()),
						itemBean.getItemNameString(), context, sortId);
				GoStoreStatisticsUtil.saveUserDataTouch(itemBean, GoStoreAppInforUtil
						.isNewToAlreadyInstall(context, itemBean.getPkgName(),
								itemBean.getVerCode()), context, sortId);
				// 去掉GA
				// marketUrl = marketUrl
				// +
				// LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
				gotoMarketForAPK(context, marketUrl);
				return;
			}
			intent.putExtra(GoStorePublicDefine.ITEM_ID_KEY, itemId);
		}
		intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, itemBean.getAdID());
		intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME, itemBean.getPkgName());
		intent.putExtra(AppsThemeDetailActivity.START_RECOMMENDED_CATEGORYID, sortId);
		context.startActivity(intent);

	}

	public static boolean isInApp(String[] paytypes) {
		boolean isInApp = false;
		if (paytypes != null && paytypes.length > 0) {
			isInApp = paytypes[0].equals("1");
		}
		return isInApp;
	}

	/**
	 * <br>功能简述:只有一种付费方式的跳转方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param itemBean
	 * @param payType
	 * @param sortId
	 */
	public static void gotoDetailOnePay(Context context, BaseItemBean itemBean, String payType,
			String sortId, HashMap<String, Object> listenerList) {
		int payTypeId = 0;
		try {
			payTypeId = Integer.valueOf(payType);
		} catch (Exception e) {
			// TODO: handle exception
		}

		switch (payTypeId) {
			case PAY_MARKET :
				gotoDetail(context, itemBean, sortId);
				break;
			case PAY_INAPP :
				gotoPayForInApp(context, itemBean, listenerList, sortId);
				break;
			case PAY_GETJAR :
				gotoDetail(context, itemBean, sortId);
				break;

			default :
				break;
		}
	}

	/**
	 * <br>功能简述:只有一种付费方式的跳转方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param itemBean
	 * @param payType
	 * @param sortId
	 */
	public static void gotoDetailOnePay(Context context, DetailItemBean itemBean, String payType,
			String sortId, HashMap<String, Object> listenerList) {
		int payTypeId = 0;
		try {
			payTypeId = Integer.valueOf(payType);
		} catch (Exception e) {
			// TODO: handle exception
		}

		switch (payTypeId) {
			case PAY_MARKET :
				GoStoreStatisticsUtil.saveUserDataTouch(itemBean, false, context, sortId);
				GoStoreOperatorUtil.operateItem(context, itemBean);
				break;
			case PAY_INAPP :
				gotoPayForInApp(context, itemBean, listenerList, sortId);
				break;
			case PAY_GETJAR :
				GoStoreStatisticsUtil.saveUserDataTouch(itemBean, false, context, sortId);
				GoStoreOperatorUtil.operateItem(context, itemBean);
				break;

			default :
				break;
		}
	}

	/**
	 * <br>功能简述:去内购界面付费
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param itemBean
	 * @param listenerList
	 * @param sortId
	 */
	public static void gotoPayForInApp(Context context, BaseItemBean itemBean,
			HashMap<String, Object> listenerList, String sortId) {
		if (listenerList != null) {
			//点击统计
			GoStoreStatisticsUtil.saveUserDataTouchForZip(itemBean, context, sortId);
			IPurchaseStateListener listener = (IPurchaseStateListener) listenerList
					.get(LISTENER_KEY_INAPP);
			ThemeAppInBillingManager inBillingManager = ThemeAppInBillingManager
					.getInstance(context);
			inBillingManager.requestPurchase(itemBean.getPkgName(), itemBean.getPayId(), listener);
		}
	}

	/**
	 * <br>功能简述:去内购界面付费
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param itemBean
	 * @param listenerList
	 * @param sortId
	 */
	public static void gotoPayForInApp(Context context, DetailItemBean itemBean,
			HashMap<String, Object> listenerList, String sortId) {
		if (listenerList != null) {
			GoStoreStatisticsUtil.saveUserDataTouchForZip(itemBean, context, sortId);
			IPurchaseStateListener listener = (IPurchaseStateListener) listenerList
					.get(LISTENER_KEY_INAPP);
			ThemeAppInBillingManager inBillingManager = ThemeAppInBillingManager
					.getInstance(context);
			inBillingManager.requestPurchase(itemBean.getPkgName(), itemBean.getPayId(), listener);
		}
	}

	/**
	 * <br>功能简述:判断是否一般下载安装
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param paytypes
	 * @return
	 */
	public static boolean getIsAppCommonDownload(String[] paytypes) {
		if (paytypes == null) {
			return true;
		}
		if (paytypes.length < 1) {
			return true;
		}
		if (paytypes.length == 1 && paytypes[0].equals("0")) {
			return true;
		}
		return false;
	}
	
	/**
	 * <br>功能简述:判断是否一般下载安装
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param paytypes
	 * @return
	 */
	public static boolean isNoPay(int[] paytypes) {
		if (paytypes == null) {
			return true;
		}
		if (paytypes.length < 1) {
			return true;
		}
		if (paytypes.length == 1 && paytypes[0] == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * <br>
	 * 功能简述:下载内付费的主题包 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param infoBean
	 */
	public static void startDownloadZip(Context context, BaseItemBean infoBean) {
		if (infoBean == null) {
			return;
		}
		String url = infoBean.getDownurl();
		if (url == null || url.equals("")) {
			return;
		}
		if (!android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			DeskToast
					.makeText(context, R.string.import_export_sdcard_unmounted, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String name = infoBean.getItemNameString();
		String pkgname = infoBean.getPkgName();
		String downLoadUrl = url + "&imei=" + GoStorePhoneStateUtil.getVirtualIMEI(context);
		IDownloadService mController = GOLauncherApp.getApplication().getDownloadController();
		try {
			DownloadTask task = mController.getDownloadTaskById(infoBean.getAdID());
			if (task != null && task.getDownloadUrl().equals(downLoadUrl)) {
				Toast.makeText(context, R.string.themestore_downloading, 600).show();
				return;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//统计安装量
		GoStoreAppStatistics.getInstance(context).onAppInstalled(infoBean.getPkgName());
		GoStoreOperatorUtil.downloadFileDirectly(GOLauncherApp.getContext(), name,
				LauncherEnv.Path.GOT_ZIP_HEMES_PATH, downLoadUrl, infoBean.getAdID(), pkgname
						+ ZipResources.ZIP_POSTFIX, false, AppsDetail.START_TYPE_DOWNLOAD_GO);
	}
	/**
	 * <br>
	 * 功能简述:下载内付费的主题包 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param infoBean
	 */
	public static void startDownloadZip(Context context, AppDetailInfoBean infoBean) {
		if (infoBean == null) {
			return;
		}
		String url = infoBean.mResourceUrl;
		if (url == null || url.equals("")) {
			return;
		}
		if (!android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			DeskToast
					.makeText(context, R.string.import_export_sdcard_unmounted, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String name = infoBean.mName;
		String pkgname = infoBean.mPkgName;
		String downLoadUrl = url + "&imei=" + GoStorePhoneStateUtil.getVirtualIMEI(context);
		IDownloadService mController = GOLauncherApp.getApplication().getDownloadController();
		try {
			DownloadTask task = mController.getDownloadTaskById(infoBean.mAppId);
			if (task != null && task.getDownloadUrl().equals(downLoadUrl)) {
				Toast.makeText(context, R.string.themestore_downloading, 600).show();
				return;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//统计安装量
		GoStoreAppStatistics.getInstance(context).onAppInstalled(infoBean.mPkgName);
		GoStoreOperatorUtil.downloadFileDirectly(GOLauncherApp.getContext(), name,
				LauncherEnv.Path.GOT_ZIP_HEMES_PATH, downLoadUrl, infoBean.mAppId, pkgname
						+ ZipResources.ZIP_POSTFIX, false, AppsDetail.START_TYPE_DOWNLOAD_GO);
	}

	/**
	 * <br>
	 * 功能简述:下载内付费的主题包 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param infoBean
	 */
	public static void startDownloadZip(Context context, DetailItemBean infoBean) {
		if (infoBean == null) {
			return;
		}
		String url = infoBean.getZipDownurl();
		if (url == null || url.equals("")) {
			return;
		}
		if (!android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			DeskToast
					.makeText(context, R.string.import_export_sdcard_unmounted, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String name = infoBean.getItemNameString();
		String pkgname = infoBean.getPkgName();
		String downLoadUrl = url + "&imei=" + GoStorePhoneStateUtil.getVirtualIMEI(context);
		IDownloadService mController = GOLauncherApp.getApplication().getDownloadController();
		try {
			if (mController != null) {
				DownloadTask task = mController.getDownloadTaskById(infoBean.getId());
				if (task != null && task.getDownloadUrl().equals(downLoadUrl)) {
					Toast.makeText(context, R.string.themestore_downloading, 600).show();
					return;
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//统计安装量
		GoStoreAppStatistics.getInstance(context).onAppInstalled(infoBean.getPkgName());
		GoStoreOperatorUtil.downloadFileDirectly(GOLauncherApp.getContext(), name,
				LauncherEnv.Path.GOT_ZIP_HEMES_PATH, downLoadUrl, infoBean.getId(), pkgname
						+ ZipResources.ZIP_POSTFIX, false, AppsDetail.START_TYPE_DOWNLOAD_GO);
	}

	/**
	 * <br>
	 * 功能简述:弹出选择付费方式的弹出框
	 * 功能详细描述: <br>
	 * 注意:如果不支持内付费则只显示getjar
	 * 
	 * @param activity
	 * @param infoBean
	 * @param purchaseStateListener
	 */
	public static void selectPayType(final Context context, final BaseItemBean infoBean,
			final String sortid, final HashMap<String, Object> listenerList) {
		final Dialog dialog = new Dialog(context, R.style.ThemePaidDialog);
		dialog.setContentView(R.layout.theme_paid_choice_dialog);
		TextView title = (TextView) dialog.findViewById(R.id.dialog_title_text);
		title.setText(infoBean.getItemNameString());
		LinearLayout item1 = (LinearLayout) dialog.findViewById(R.id.dialog_item1);

		if (!Machine.isCnUser(context) && PurchaseSupportedManager.checkBillingSupported(context)) {
			LinearLayout item2 = (LinearLayout) dialog.findViewById(R.id.dialog_item2);
			item2.setVisibility(View.VISIBLE);
			item2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					gotoPayForInApp(context, infoBean, listenerList, sortid);
				}
			});
		}

		item1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				gotoDetail(context, infoBean, sortid);

			}
		});
		dialog.show();
	}

	/**
	 * <br>
	 * 功能简述:弹出选择付费方式的弹出框
	 * 功能详细描述: <br>
	 * 注意:如果不支持内付费则只显示getjar
	 * 
	 * @param activity
	 * @param infoBean
	 * @param purchaseStateListener
	 */
	public static void selectPayType(final Activity context, final DetailItemBean infoBean,
			final String sortid, final HashMap<String, Object> listenerList) {
		final Dialog dialog = new Dialog(context, R.style.ThemePaidDialog);
		dialog.setContentView(R.layout.theme_paid_choice_dialog);
		TextView title = (TextView) dialog.findViewById(R.id.dialog_title_text);
		title.setText(infoBean.getItemNameString());
		LinearLayout item1 = (LinearLayout) dialog.findViewById(R.id.dialog_item1);

		if (!Machine.isCnUser(context) && PurchaseSupportedManager.checkBillingSupported(context)) {
			LinearLayout item2 = (LinearLayout) dialog.findViewById(R.id.dialog_item2);
			item2.setVisibility(View.VISIBLE);
			item2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					gotoPayForInApp((Context) context, infoBean, listenerList, sortid);
				}
			});
		}

		item1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				GoStoreOperatorUtil.operateItem(context, infoBean);
			}
		});
		dialog.show();
	}

	public static void sendDownloadFailNotification(Context context, String ip, String theme) {

		String ns = Context.NOTIFICATION_SERVICE;

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(ns);

		int icon = R.drawable.icon;

		CharSequence tickerText = context.getString(R.string.gostore_downlaod_fail_note_title);

		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		CharSequence contentTitle = context.getString(R.string.gostore_downlaod_fail_note_title);

		CharSequence contentText = context.getString(R.string.gostore_downlaod_fail_note);

		PackageManager pm = context.getPackageManager();
		PackageInfo pi = null;
		String versionName = "";
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			if (pi != null) {
				versionName = pi.versionName;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Locale locale = Locale.getDefault();
		String country = locale.getLanguage().toLowerCase();

		StringBuffer mailDody = new StringBuffer();

		mailDody.append("\r\n").append(context.getString(R.string.gostore_downlaod_fail_mail))
				.append("\r\n")
				.append(context.getString(R.string.gostore_downlaod_fail_mail_country))
				.append(country).append("\r\n")
				.append(context.getString(R.string.gostore_downlaod_fail_mail_ip1))
				.append(GoStorePhoneStateUtil.getLocalIPAddress()).append("\r\n")
				.append(context.getString(R.string.gostore_downlaod_fail_mail_ip2)).append(ip)
				.append("\r\n")
				.append(context.getString(R.string.gostore_downlaod_fail_mail_gmail)).append("")
				.append("\r\n")
				.append(context.getString(R.string.gostore_downlaod_fail_mail_theme)).append(theme)
				.append("\r\n");

		Intent notificationIntent = getSendMailIntent(context, "golauncherexbug@gmail.com",
				context.getString(R.string.gostore_downlaod_fail_mail_title) + versionName,
				mailDody.toString());

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		notification.flags = Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify(1, notification);
	}
	public static Intent getSendMailIntent(Context context, String to, String title, String body) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		String[] receiver = new String[] { to };

		//    	String versionString = "_v" + mContext.getString(R.string.curVersionupdata) + "_Fix_";
		String subject = title;

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		if (body != null) {
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		}
		emailIntent.setType("plain/text");

		return emailIntent;
	}

	public static long downloadFileDirectly(Context context, String fileName, String downloadUrl,
			long id, String packageName,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName, int iconType, String iconUrl, int module,
			boolean storageInSD) {
		long taskId = -1;
		if (context == null || fileName == null || "".equals(fileName.trim())
				|| downloadUrl == null || "".equals(downloadUrl.trim())) {
			return taskId;
		}
		fileName = fileName.trim();
		downloadUrl = downloadUrl.trim();
		String saveFilePath = null;
		if (storageInSD) {
			if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
				saveFilePath = DOWNLOAD_DIRECTORY_PATH + customDownloadFileName;
			} else {
				saveFilePath = DOWNLOAD_DIRECTORY_PATH + fileName + System.currentTimeMillis()
						+ ".apk";
			}
		} else {
			if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
				saveFilePath = context.getFilesDir().getAbsolutePath() + "/"
						+ customDownloadFileName;
			} else {
				saveFilePath = context.getFilesDir().getAbsolutePath() + "/" + fileName
						+ System.currentTimeMillis() + ".apk";
			}
		}
		String[] listenerClassNames = null;
		if (listenerClazzArray != null && listenerClazzArray.length > 0) {
			listenerClassNames = new String[listenerClazzArray.length];
			for (int i = 0; i < listenerClassNames.length; i++) {
				listenerClassNames[i] = listenerClazzArray[i].getName();
			}
		}
		DownloadTask task = new DownloadTask(id, downloadUrl, fileName, saveFilePath, packageName,
				iconType, iconUrl, module);
		task.setIsApkFile(true);
//		task.setTreatment(treatment);
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				taskId = mDownloadController.addDownloadTask(task);
				if (taskId != -1) {
					if (listenerClassNames != null && listenerClassNames.length > 0) {
						for (String name : listenerClassNames) {
							mDownloadController.addDownloadTaskListenerByName(taskId, name);
						}
					} else {
						// 添加默认下载监听器
						mDownloadController.addDownloadTaskListenerByName(taskId,
								DefaultDownloadListener.class.getName());
					}
					mDownloadController.startDownload(taskId);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return taskId;
	}
	
	public static long downloadFileDirectly(Context context, String fileName, String path,
			String downloadUrl, long id, String customDownloadFileName,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray, boolean isApk,
			int module) {
		long taskId = -1;
		if (context == null || fileName == null || "".equals(fileName.trim())
				|| downloadUrl == null || "".equals(downloadUrl.trim())) {
			return taskId;
		}
		fileName = fileName.trim();
		downloadUrl = downloadUrl.trim();
		String saveFilePath = null;
		if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
			saveFilePath = path + customDownloadFileName;
		} else {
			saveFilePath = path + fileName + System.currentTimeMillis() + ".apk";
		}
		String[] listenerClassNames = null;
		if (listenerClazzArray != null && listenerClazzArray.length > 0) {
			listenerClassNames = new String[listenerClazzArray.length];
			for (int i = 0; i < listenerClassNames.length; i++) {
				listenerClassNames[i] = listenerClazzArray[i].getName();
			}
		}
		DownloadTask task = null;;
		if (module == 0) {
			task = new DownloadTask(id, downloadUrl, fileName, saveFilePath);
		} else {
			task = new DownloadTask(id, downloadUrl, fileName, saveFilePath, null, 0, null, module);
		}
		task.setIsApkFile(isApk);
		IDownloadService mController = GOLauncherApp.getApplication().getDownloadController();
		try {
			if (mController == null) {
				return -1;
			}
			taskId = mController.addDownloadTask(task);
			if (taskId != -1) {
				// 添加下载监听器
				if (listenerClassNames != null && listenerClassNames.length > 0) {
					for (String name : listenerClassNames) {
						mController.addDownloadTaskListenerByName(taskId, name);
					}
				} else {
					// 添加默认下载监听器
					mController.addDownloadTaskListenerByName(taskId,
							DefaultDownloadListener.class.getName());
				}
				mController.startDownload(taskId);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return taskId;
	}
}
