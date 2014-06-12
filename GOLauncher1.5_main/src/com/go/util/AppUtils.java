package com.go.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.log.LogConstants;
import com.go.util.log.Loger;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.desks.diy.INotificationId;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 应用相关的工具类
 * @author yangguanxiang
 *
 */
public class AppUtils {
	private static final int ONT_TIME_COUNT = 30;

	/**
	 * 检查是安装某包
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @return
	 */
	public static boolean isAppExist(final Context context, final String packageName) {
		if (context == null || packageName == null) {
			return false;
		}
		if (ThemeManager.DEFAULT_THEME_PACKAGE_3.equals(packageName)) {
			return true;
		}
		boolean result = false;
		try {
			// context.createPackageContext(packageName,
			// Context.CONTEXT_IGNORE_SECURITY);
			context.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_SHARED_LIBRARY_FILES);
			result = true;
		} catch (NameNotFoundException e) {
			// TODO: handle exception
			result = false;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public static boolean isAppExist(final Context context, final Intent intent) {
		List<ResolveInfo> infos = null;
		try {
			infos = context.getPackageManager().queryIntentActivities(intent, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (infos != null) && (infos.size() > 0);
	}

	/**
	 * 安全启动activity，捕获异常
	 * 
	 * @param context
	 * @param intent
	 *            启动的intent
	 * @return
	 */
	public static void safeStartActivity(final Context context, Intent intent) {
		try {
			if (context != null) {
				context.startActivity(intent);
			}
		} catch (ActivityNotFoundException e) {
			Log.e(LogConstants.HEART_TAG, "saveStartActivity err " + e.getMessage());
		} catch (SecurityException e) {
			Log.e(LogConstants.HEART_TAG, "saveStartActivity err " + e.getMessage());
		}
	}

	/**
	 * 安全启动activity for result，捕获异常
	 * 
	 * @param context
	 * @param intent
	 *            启动的intent
	 * @return
	 */
	public static void safeStartActivityForResult(final Activity activity, Intent intent,
			int requestCode) {
		try {
			if (activity != null) {
				activity.startActivityForResult(intent, requestCode);
			}
		} catch (ActivityNotFoundException e) {
			Log.e(LogConstants.HEART_TAG, "saveStartActivityForResult err " + e.getMessage());
		} catch (SecurityException e) {
			Log.e(LogConstants.HEART_TAG, "saveStartActivityForResult err " + e.getMessage());
		}
	}

	/**
	 * 获取app包信息
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @return
	 */
	public static PackageInfo getAppPackageInfo(final Context context, final String packageName) {
		PackageInfo info = null;
		try {
			info = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (Exception e) {
			info = null;
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * 手机上是否有电子市场
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMarketExist(final Context context) {
		return isAppExist(context, LauncherEnv.Market.PACKAGE);
	}

	/**
	 * 服务是否正在运行
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @param serviceName
	 *            服务名
	 * @return
	 */
	public static boolean isServiceRunning(Context context, String packageName, String serviceName) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		return isServiceRunning(activityManager, packageName, serviceName);
	}

	public static boolean isServiceRunning(ActivityManager activityManager, String packageName,
			String serviceName) {
		List<RunningServiceInfo> serviceTasks = activityManager.getRunningServices(ONT_TIME_COUNT);
		int sz = null == serviceTasks ? 0 : serviceTasks.size();
		for (int i = 0; i < sz; i++) {
			RunningServiceInfo info = serviceTasks.get(i);
			if (null != info && null != info.service) {
				final String pkgName = info.service.getPackageName();
				final String className = info.service.getClassName();

				if (pkgName != null && pkgName.contains(packageName) && className != null
						&& className.contains(serviceName)) {
					Log.i("Notification", "package = " + info.service.getPackageName()
							+ " class = " + info.service.getClassName());
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 发送通知栏信息(点击后打开Activity)
	 * 
	 * @param context
	 * @param intent
	 * @param iconId
	 *            图标id
	 * @param tickerText
	 *            通知栏显示的信息
	 * @param title
	 *            展开通知栏后显示的标题
	 * @param content
	 *            展开通知栏显示的文字
	 * @param notificationId
	 *            通知消息id {@link INotificationId}, 全局唯一
	 */
	public static void sendNotification(Context context, Intent intent, int iconId,
			CharSequence tickerText, CharSequence title, CharSequence content, int notificationId) {
		sendNotification(context, intent, iconId, tickerText, title, content, notificationId,
				Notification.FLAG_AUTO_CANCEL);
	}

	/**
	 * 发送通知栏信息(点击后打开Activity)
	 * 
	 * @param context
	 * @param intent
	 * @param iconId
	 *            图标id
	 * @param tickerText
	 *            通知栏显示的信息
	 * @param title
	 *            展开通知栏后显示的标题
	 * @param content
	 *            展开通知栏显示的文字
	 * @param notificationId
	 *            通知消息id {@link INotificationId}, 全局唯一
	 * @param flags
	 * 			  标志
	 */
	public static void sendNotification(Context context, Intent intent, int iconId,
			CharSequence tickerText, CharSequence title, CharSequence content, int notificationId,
			int flags) {
		sendIconNotification(context, intent, iconId, tickerText, title, content, notificationId,
				flags, null);
	}
	/**
	 * 自定义图片发送通知栏信息(点击后打开Activity)
	 * @param context
	 * @param intent
	 * @param iconId
	 * @param tickerText
	 * @param title
	 * @param content
	 * @param notificationId
	 * @param icon 
	 * 			展示图片
	 */
	public static void sendIconNotification(Context context, Intent intent, int iconId,
			CharSequence tickerText, CharSequence title, CharSequence content, int notificationId,
			Bitmap icon) {
		if (icon != null) {
			RemoteViews contentView = new RemoteViews(context.getPackageName(),
					R.layout.msg_center_noitify_content);
			contentView.setTextViewText(R.id.theme_title, title);
			contentView.setTextViewText(R.id.theme_content, content);
			contentView.setImageViewBitmap(R.id.theme_view_image, icon);
			sendIconNotification(context, intent, iconId, tickerText, title, content,
					notificationId, Notification.FLAG_AUTO_CANCEL, contentView);
		} else {
			sendIconNotification(context, intent, iconId, tickerText, title, content,
					notificationId, Notification.FLAG_AUTO_CANCEL, null);
		}

	}
	public static void sendIconNotification(Context context, Intent intent, int iconId,
			CharSequence tickerText, CharSequence title, CharSequence content, int notificationId,
			int flags, RemoteViews contentView) {
		try {
			PendingIntent contentIntent = null;
			if (notificationId == INotificationId.GOTO_THEME_PREVIEW
					|| notificationId == INotificationId.GOTO_LOCKERTHEME_PREVIEW) {
				contentIntent = PendingIntent.getActivity(context, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
			} else {
				contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
			}

			Notification notification = new Notification(iconId, tickerText,
					System.currentTimeMillis());
			notification.setLatestEventInfo(context, title, content, contentIntent);
			if (contentView != null) {
				notification.contentIntent = contentIntent;
				notification.contentView = contentView;
			}
			// 设置标志
			notification.flags |= flags;
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(notificationId, notification);
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "start notification error id = " + notificationId);
		}
	}
	/**
	 * 发送通知栏信息(点击后触发广播)
	 * 
	 * @param context
	 * @param intent
	 * @param iconId
	 *            图标id
	 * @param tickerText
	 *            通知栏显示的信息
	 * @param title
	 *            展开通知栏后显示的标题
	 * @param content
	 *            展开通知栏显示的文字
	 * @param notificationId
	 *            通知消息id {@link INotificationId}, 全局唯一
	 */
	public static void sendNotificationBCintent(Context context, Intent intent, int iconId,
			CharSequence tickerText, CharSequence title, CharSequence content, int notificationId) {
		try {
			PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			Notification notification = new Notification(iconId, tickerText,
					System.currentTimeMillis());
			notification.setLatestEventInfo(context, title, content, contentIntent);

			// 点击后自动消失
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(notificationId, notification);
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "start notification error id = " + notificationId);
		}
	}

	/**
	 * 发送短暂显示的通知栏信息
	 * 
	 * @param context
	 * @param iconId
	 *            　图标id
	 * @param tickerText
	 *            　通知栏显示的信息
	 * @param notificationId
	 *            　通知消息id {@link INotificationId}, 全局唯一
	 */
	public static void sendNotificationDisplaySeconds(Context context, int iconId,
			CharSequence tickerText, int notificationId) {
		sendNotification(context, null, iconId, tickerText, null, null, notificationId);

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(notificationId);
	}

	/**
	 * 取消指定的ID的Notificaiton
	 * @param context
	 * @param notificationId
	 */
	public static void cancelNotificaiton(Context context, int notificationId) {
		if (context != null) {
			try {
				NotificationManager nm = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancel(notificationId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 跳转到Android Market
	 * 
	 * @param uriString
	 *            market的uri
	 * @return 成功打开返回true
	 */
	public static boolean gotoMarket(Context context, String uriString) {
		boolean ret = false;
		Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
		marketIntent.setPackage(LauncherEnv.Market.PACKAGE);
		marketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(marketIntent);
			ret = true;
		} catch (ActivityNotFoundException e) {
			Log.i(LogConstants.HEART_TAG, "gotoMarketForAPK error, uri = " + uriString);
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "gotoMarketForAPK error, uri = " + uriString);
		}
		return ret;
	}

	public static void gotoBrowserInRunTask(Context context, String url) {
		// 跳转intent
		Uri uri = Uri.parse(url);
		Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);

		// 1:已安装的浏览器列表
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveList = pm.queryIntentActivities(myIntent, 0);
		boolean hasRun = false;

		if (resolveList != null && !resolveList.isEmpty()) {
			// 2:获取当前运行程序列表
			ArrayList<FunTaskItemInfo> curRunList = null;
			try {
				curRunList = AppCore.getInstance().getTaskMgrControler().getProgresses();
			} catch (Throwable e) {
			}
			int curRunSize = (curRunList != null) ? curRunList.size() : 0;

			// 两个列表循环遍历
			for (int i = curRunSize - 1; i > 0; i--) {
				FunTaskItemInfo funTaskItemInfo = curRunList.get(i);
				Intent funIntent = funTaskItemInfo.getAppItemInfo().mIntent;
				ComponentName funComponentName = funIntent.getComponent();
				for (ResolveInfo resolveInfo : resolveList) {
					if (resolveInfo.activityInfo.packageName != null
							&& resolveInfo.activityInfo.packageName.equals(funComponentName
									.getPackageName())) {
						// 找到正在运行的浏览器，直接拉起
						if (funIntent.getComponent() != null) {
							String pkgString = funIntent.getComponent().getPackageName();
							if (pkgString != null) {
								if (pkgString.equals("com.android.browser")
										|| pkgString.equals("com.dolphin.browser.cn")
										|| pkgString.equals("com.android.chrome")
										|| pkgString.equals("com.qihoo.browser")) {
									//上述浏览器后台拉起会跳转浏览器首页，而非保存的用户原来页面
									hasRun = true;
									funIntent.setAction(Intent.ACTION_VIEW);
									funIntent.setData(uri);
									context.startActivity(funIntent);
								}
							}
						}
					}
				}
			}
			//无正在运行的浏览器，直接取浏览器列表的第1个打开
			if (!hasRun) {
				ResolveInfo resolveInfo = resolveList.get(0);
				String pkgString = resolveInfo.activityInfo.packageName;
				String activityName = resolveInfo.activityInfo.name;
				myIntent.setClassName(pkgString, activityName);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(myIntent);
			}
		}
	}

	/**
	 * 浏览器直接访问uri
	 * 
	 * @param uriString
	 * @return 成功打开返回true
	 */
	public static boolean gotoBrowser(Context context, String uriString) {
		boolean ret = false;
		if (uriString == null) {
			return ret;
		}
		Uri browserUri = Uri.parse(uriString);
		if (null != browserUri) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
			browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(browserIntent);
				ret = true;
			} catch (ActivityNotFoundException e) {
				Log.i(LogConstants.HEART_TAG, "gotoBrowser error, uri = " + uriString);
			} catch (Exception e) {
				Log.i(LogConstants.HEART_TAG, "gotoBrowser error, uri = " + uriString);
			}
		}
		return ret;
	}
	public static void gotoDefaultBrowser(Context mContext, String uriString) {
		// 跳转intent
		Uri uri = Uri.parse(uriString);
		Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);

		// 1:已安装的浏览器列表
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> resolveList = pm.queryIntentActivities(myIntent, 0);
		boolean hasRun = false;

		if (resolveList != null && !resolveList.isEmpty()) {
			// 2:获取当前运行程序列表
			ArrayList<FunTaskItemInfo> curRunList = null;
			try {
				curRunList = AppCore.getInstance().getTaskMgrControler().getProgresses();
			} catch (Throwable e) {
			}
			int curRunSize = (curRunList != null) ? curRunList.size() : 0;

			// 两个列表循环遍历
			for (int i = curRunSize - 1; i > 0; i--) {
				FunTaskItemInfo funTaskItemInfo = curRunList.get(i);
				Intent funIntent = funTaskItemInfo.getAppItemInfo().mIntent;
				ComponentName funComponentName = funIntent.getComponent();
				for (ResolveInfo resolveInfo : resolveList) {
					if (resolveInfo.activityInfo.packageName != null
							&& resolveInfo.activityInfo.packageName.equals(funComponentName
									.getPackageName())) {
						// 找到正在运行的浏览器，直接拉起
						if (funIntent.getComponent() != null) {
							String pkgString = funIntent.getComponent().getPackageName();
							if (pkgString != null) {
								if (pkgString.equals("com.android.browser")
										|| pkgString.equals("com.dolphin.browser.cn")
										|| pkgString.equals("com.android.chrome")
										|| pkgString.equals("com.qihoo.browser")) {
									//上述浏览器后台拉起会跳转浏览器首页，而非保存的用户原来页面
									hasRun = true;
									funIntent.setAction(Intent.ACTION_VIEW);
									funIntent.setData(uri);
									mContext.startActivity(funIntent);
								}
							}
						}
					}
				}
			}
			//无正在运行的浏览器，直接取浏览器列表的第1个打开
			if (!hasRun) {
				ResolveInfo resolveInfo = resolveList.get(0);
				String pkgString = resolveInfo.activityInfo.packageName;
				String activityName = resolveInfo.activityInfo.name;
				myIntent.setClassName(pkgString, activityName);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(myIntent);
			}
		}
	}
	/**
	 * 优先跳转到market，如果失败则转到浏览器
	 * 
	 * @param context
	 * @param marketUrl
	 *            market地址
	 * @param browserUrl
	 *            浏览器地址
	 */
	public static void gotoBrowserIfFailtoMarket(Context context, String marketUrl,
			String browserUrl) {
		boolean toMarket = gotoMarket(context, marketUrl);
		if (!toMarket) {
			gotoBrowser(context, browserUrl);
		}
	}

	/**
	 * 获取在功能菜单出现的程序列表
	 * 
	 * @param context
	 *            上下文
	 * @return 程序列表，类型是 List<ResolveInfo>
	 */
	public static List<ResolveInfo> getLauncherApps(Context context) {
		List<ResolveInfo> infos = null;
		PackageManager packageMgr = context.getPackageManager();
		Intent intent = new Intent(ICustomAction.ACTION_MAIN);
		intent.addCategory("android.intent.category.LAUNCHER");
		try {
			infos = packageMgr.queryIntentActivities(intent, 0);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		packageMgr = null;
		return infos;
	}

	/**
	 * 卸载程序
	 * 
	 * @param context
	 *            上下文
	 * @param packageURI
	 *            需要卸载的程序的Uri
	 */
	public static void uninstallApp(Context context, Uri packageURI) {
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		context.startActivity(uninstallIntent);
		uninstallIntent = null;
	}

	/**
	 * 卸载包
	 * 
	 * @param context
	 *            上下文
	 * @param packageURI
	 *            需要卸载的程序的Uri
	 */
	public static void uninstallPackage(Context context, String pkgName) {
		Uri packageURI = Uri.parse("package:" + pkgName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		context.startActivity(uninstallIntent);
		packageURI = null;
		uninstallIntent = null;
	}

	public static String getDefaultLauncherPackage(Context context) {
		PackageManager pm = context.getPackageManager();

		// 默认列表
		List<ComponentName> componentNames = new ArrayList<ComponentName>();
		List<IntentFilter> intentFilters = new ArrayList<IntentFilter>();
		pm.getPreferredActivities(intentFilters, componentNames, null);

		// Launcher
		Intent intent = new Intent(ICustomAction.ACTION_MAIN);
		intent.addCategory("android.intent.category.HOME");
		intent.addCategory("android.intent.category.DEFAULT");
		List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);

		int launcherSz = infos.size();
		int preferredSz = componentNames.size();
		for (int i = 0; i < launcherSz; i++) {
			ResolveInfo info = infos.get(i);
			if (null == info) {
				continue;
			}
			String packageStr = info.activityInfo.packageName;
			if (null == packageStr) {
				continue;
			}

			for (int j = 0; j < preferredSz; j++) {
				ComponentName componentName = componentNames.get(j);
				if (null == componentName) {
					continue;
				}
				if (packageStr.equals(componentName.getPackageName())) {
					return packageStr;
				}
			}
		}

		return null;
	}

	public static void showAppDetails(Context context, String packageName) {
		final String scheme = "package";
		/**
		 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
		 */
		final String appPkgName21 = "com.android.settings.ApplicationPkgName";
		/**
		 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
		 */
		final String appPkgName22 = "pkg";
		/**
		 * InstalledAppDetails所在包名
		 */
		final String appDetailsPackageName = "com.android.settings";
		/**
		 * InstalledAppDetails类名
		 */
		final String appDetailsClassName = "com.android.settings.InstalledAppDetails";

		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) {
			// 2.3（ApiLevel 9）以上，使用SDK提供的接口
			intent.setAction(ICustomAction.ACTION_SETTINGS);
			Uri uri = Uri.fromParts(scheme, packageName, null);
			intent.setData(uri);
		} else {
			// 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
			// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			final String appPkgName = apiLevel == 8 ? appPkgName22 : appPkgName21;
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(appDetailsPackageName, appDetailsClassName);
			intent.putExtra(appPkgName, packageName);
		}

		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取包名
	 * 
	 * @param intent
	 * @return
	 */
	public static String getPackage(Intent intent) {
		if (intent != null) {
			final ComponentName cn = intent.getComponent();
			if (cn != null) {
				return cn.getPackageName();
			}
		}
		return null;
	}

	/**
	 * 杀手当前进程
	 */
	public static void killProcess() {
		Loger.w(LogConstants.HEART_TAG, "killProcess");
		Process.killProcess(Process.myPid());
	}

	/**
	 * 获取指定包的版本号
	 * 
	 * @author huyong
	 * @param context
	 * @param pkgName
	 */
	public static int getVersionCodeByPkgName(Context context, String pkgName) {
		int versionCode = 0;
		if (pkgName != null) {
			PackageManager pkgManager = context.getPackageManager();
			try {
				PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
				versionCode = pkgInfo.versionCode;
			} catch (NameNotFoundException e) {
				Log.i("AppUtils", "getVersionCodeByPkgName=" + pkgName + " has " + e.getMessage());
			}
		}
		return versionCode;
	}

	/**
	 * 获取指定包的版本名称
	 * 
	 * @author huyong
	 * @param context
	 * @param pkgName
	 */
	public static String getVersionNameByPkgName(Context context, String pkgName) {
		String versionName = "0.0";
		if (pkgName != null) {
			PackageManager pkgManager = context.getPackageManager();
			try {
				PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
				versionName = pkgInfo.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return versionName;
	}

	/**
	 * 将版本名称转换为一位小数点的float型数据
	 * 
	 * @param context
	 * @param pkgName
	 */
	public static float changeVersionNameToFloat(String versionName) {
		float versionNumber = 0.0f;
		if (versionName != null && !versionName.equals("")) {
			try {
				String beta = "beta";
				if (versionName.contains(beta)) {
					versionName = versionName.replace(beta, "");
				}
				int firstPoint = versionName.indexOf(".");
				int secondPoint = versionName.indexOf(".", firstPoint + 1);
				if (secondPoint != -1) {
					String temp = versionName.substring(0, secondPoint)
							+ versionName.substring(secondPoint + 1, versionName.length());
					versionNumber = Float.parseFloat(temp);
				} else {
					versionNumber = Float.parseFloat(versionName);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return versionNumber;
	}

	/**
	 * market上查看程序信息
	 * 
	 * @param packageName
	 */
	public static void viewAppDetail(Context context, String packageName) {
		String keyword = LauncherEnv.Market.APP_DETAIL + packageName;
		Uri uri = Uri.parse(keyword);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		keyword = null;
	}

	/**
	 * 打开golocker
	 */
	public static void gotoGolocker(final Context context) {
		if (isGoLockerExist(context)) {
			PackageManager pm = context.getPackageManager();
			try {
				Intent tmpIntent = pm.getLaunchIntentForPackage(ICustomAction.ACTION_LOCKER);
				safeStartActivity(context, tmpIntent);
			} catch (Exception e) {
			}
		} else {
			gotoDownloadGolocker(context);
		}
	}

	/**
	 * 打开golocker
	 */
	public static void gotoGolockerSetting(final Context context) {
		if (isGoLockerExist(context)) {
			safeStartActivity(context, new Intent(ICustomAction.ACTION_LOCKER_SETTING));
		} else {
			gotoDownloadGolocker(context);
		}
	}

	private static void gotoDownloadGolocker(final Context context) {
		DeskBuilder builder = new DeskBuilder(context);
		builder.setTitle(context.getString(R.string.locker_tip_title));
		builder.setMessage(context.getString(R.string.locker_tip_message));
		builder.setPositiveButton(context.getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!AppUtils.gotoMarket(context, LauncherEnv.Market.BY_PKGNAME
						+ LauncherEnv.Plugin.LOCKER_PACKAGE)) {
					AppUtils.gotoBrowser(context, LauncherEnv.Url.GOLOCKER_DOWNLOAD_URL);
				}
			}
		});
		builder.setNegativeButton(context.getString(R.string.cancel), null);
		builder.create().show();
	}

	/**
	 * 打开gobackup
	 */
	public static void gotoGobackup(final Context context) {
		if (isAppExist(context, LauncherEnv.Plugin.RECOMMAND_GOBACKUPEX_PACKAGE)) {
			PackageManager pm = context.getPackageManager();
			try {
				Intent tmpIntent = pm
						.getLaunchIntentForPackage(LauncherEnv.Plugin.RECOMMAND_GOBACKUPEX_PACKAGE);
				safeStartActivity(context, tmpIntent);
			} catch (Exception e) {
			}
		} else {
			// gotoDownloadGobackup(context);
			// add by chenguanyu 2012.7.10
			String title = context.getString(R.string.recommand_gobackup);
			String content = context.getString(R.string.fav_app);
			String[] linkArray = new String[] { LauncherEnv.Plugin.RECOMMAND_GOBACKUPEX_PACKAGE,
					LauncherEnv.Url.GOBACKUP_EX_FTP_URL };
			CheckApplication.downloadAppFromMarketFTPGostore(context, content, linkArray,
					LauncherEnv.GOBACKUP_GOOGLE_REFERRAL_LINK, title, System.currentTimeMillis(),
					Machine.isCnUser(context), CheckApplication.FROM_MENU);
		}
	}

	private static void gotoDownloadGobackup(final Context context) {
		DeskBuilder builder = new DeskBuilder(context);
		builder.setTitle(context.getString(R.string.attention_title));
		builder.setMessage(context.getString(R.string.backup_tip_message));
		builder.setPositiveButton(context.getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 跳到GO Store上的GO备份界面
				AppsDetail.gotoDetailDirectly(context, AppsDetail.START_TYPE_APPRECOMMENDED,
						LauncherEnv.Plugin.GOBACKUP_PACKAGE);
				//				GoStoreOperatorUtil.gotoStoreDetailDirectly(context,
				//						LauncherEnv.Plugin.GOBACKUP_PACKAGE);
			}
		});
		builder.setNegativeButton(context.getString(R.string.cancel), null);
		builder.create().show();
	}

	/**
	 * 判断应用是否安装在手机内存里
	 * 
	 * @author kingyang
	 * @param context
	 * @param intent
	 * @return
	 */
	public static boolean isInternalApp(Context context, Intent intent) {
		if (context != null) {
			PackageManager pkgMgr = context.getPackageManager();
			try {
				String internalPath = Environment.getDataDirectory().getAbsolutePath();
				String dir = pkgMgr.getActivityInfo(intent.getComponent(), 0).applicationInfo.publicSourceDir;
				if (dir != null && dir.length() > 0) {
					return dir.startsWith(internalPath);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 判断应用是否是系统应用
	 * 
	 * @author kingyang
	 * @param context
	 * @param intent
	 * @return
	 */
	public static boolean isSystemApp(Context context, Intent intent) {
		boolean isSystemApp = false;
		if (context != null) {
			PackageManager pkgMgr = context.getPackageManager();
			try {
				ApplicationInfo applicationInfo = pkgMgr.getActivityInfo(intent.getComponent(), 0).applicationInfo;
				if (applicationInfo != null) {
					isSystemApp = ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
							|| ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return isSystemApp;
	}

	/**
	 * 下载HD版本
	 */
	public static void gotoHDLauncher(final Context context) {
		PackageManager pa = context.getPackageManager();
		List<PackageInfo> packages = pa.getInstalledPackages(0);
		for (PackageInfo info : packages) {
			if (info.packageName.equals(LauncherEnv.Plugin.GOHD_LAUNCHER_PACKAGE)) {
				Intent intent = pa
						.getLaunchIntentForPackage(LauncherEnv.Plugin.GOHD_LAUNCHER_PACKAGE);
				context.startActivity(intent);
				return;
			}
		}
		if (GoStorePhoneStateUtil.is200ChannelUid(context)) {
			if (!AppUtils.gotoMarket(context, LauncherEnv.Market.BY_PKGNAME
					+ LauncherEnv.Plugin.GOHD_LAUNCHER_PACKAGE)) {
				// 如果没有电子市场跳转到网页版电子市场
				AppUtils.gotoBrowser(context, LauncherEnv.Url.GOHDLAUNCHER_WEB_URL);
			}
		} else {
			gotoDownloadHDLauncher(context);
		}
	}

	// 跳转FTP下载
	private static void gotoDownloadHDLauncher(final Context context) {
		DeskBuilder builder = new DeskBuilder(context);
		builder.setTitle(context.getString(R.string.attention_title));
		builder.setMessage(context.getString(R.string.hdlauncher_tip_message));
		builder.setPositiveButton(context.getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AppUtils.gotoBrowser(context, LauncherEnv.Url.GOHDLAUNCHER_DOWNLOAD_URL);
			}
		});
		builder.setNegativeButton(context.getString(R.string.cancel), null);
		builder.create().show();
	}

	/**
	 * 获取安装在手机内所有GO桌面的包名
	 * @param context
	 * @return
	 */
	public static List<String> getAllGoLauncherPackageNames(Context context) {
		List<String> packageNames = null;
		Intent intent = new Intent(ICustomAction.ACTION_GOLAUNCHER);
		List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent,
				0);
		if (resolveInfos != null && !resolveInfos.isEmpty()) {
			int size = resolveInfos.size();
			packageNames = new ArrayList<String>(size);
			for (ResolveInfo resolveInfo : resolveInfos) {
				packageNames.add(resolveInfo.activityInfo.packageName);
			}
		}
		return packageNames;
	}

	/**
	 * 获取指定应用的Context
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static Context getAppContext(Context context, String packageName) {
		Context ctx = null;
		try {
			ctx = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return ctx;
	}

	/**
	 * 用Intent方法判断GO锁屏是否存在
	 * @param context
	 * @return
	 */
	public static boolean isGoLockerExist(Context context) {
		Intent golockerIntent = new Intent(ICustomAction.ACTION_LOCKER);
		boolean isExist = false;
		if (isAppExist(context, golockerIntent)) {
			isExist = true;
		}
		return isExist;
	}

	public static ArrayList<String> getLockerPkgName(Context context) {
		Intent golockerIntent = new Intent(ICustomAction.ACTION_LOCKER);
		ArrayList<String> packageNames = null;
		List<ResolveInfo> infos = null;
		infos = context.getPackageManager().queryIntentActivities(golockerIntent, 0);
		if (infos != null && !infos.isEmpty()) {
			int size = infos.size();
			packageNames = new ArrayList<String>(size);
			for (ResolveInfo resolveInfo : infos) {
				if (resolveInfo.activityInfo.packageName.equals(LauncherEnv.GO_LOCK_PACKAGE_NAME)) {
					packageNames.add(resolveInfo.activityInfo.packageName);
				} else {
					packageNames.add(0, resolveInfo.activityInfo.packageName);
				}
			}
		}
		return packageNames;
	}
	/**
	 * 判断用户安装的锁屏版本是否为普通版本，即跟默认包名一致
	 * @param context
	 * @return
	 */
	public static boolean isDefGolockerExist(Context context) {
		List<String> lockPkgName = getLockerPkgName(context);
		for (String pkgName : lockPkgName) {
			if (pkgName.equals(LauncherEnv.GO_LOCK_PACKAGE_NAME)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 返回当前可使用的锁屏包名
	 * @param context
	 * @return
	 */
	public static String getCurLockerPkgName(Context context) {
		if (isGoLockerExist(context) && !isDefGolockerExist(context)) {
			return getLockerPkgName(context).get(0);
		} else {
			return LauncherEnv.GO_LOCK_PACKAGE_NAME;
		}
	}
	/**
	 * Check if the installed Gmail app supports querying for label information.
	 * 
	 * @param c
	 *            an application Context
	 * @return true if it's safe to make label API queries
	 */
	public static boolean canReadGmailLabels(Context c) {
		/**
		 * Permission required to access this
		 * {@link android.content.ContentProvider}
		 */
		final String permission = "com.google.android.gm.permission.READ_CONTENT_PROVIDER";
		/**
		 * Authority for the Gmail content provider.
		 */
		final String authority = "com.google.android.gm";
		String gmailPackageName = "com.google.android.gm";

		boolean supported = false;

		try {
			final PackageInfo info = c.getPackageManager().getPackageInfo(gmailPackageName,
					PackageManager.GET_PROVIDERS | PackageManager.GET_PERMISSIONS);
			boolean allowRead = false;
			if (info.permissions != null) {
				for (int i = 0, len = info.permissions.length; i < len; i++) {
					final PermissionInfo perm = info.permissions[i];
					if (permission.equals(perm.name)
							&& perm.protectionLevel < PermissionInfo.PROTECTION_SIGNATURE) {
						allowRead = true;
						break;
					}
				}
			}
			if (allowRead && info.providers != null) {
				for (int i = 0, len = info.providers.length; i < len; i++) {
					final ProviderInfo provider = info.providers[i];
					if (authority.equals(provider.authority)
							&& TextUtils.equals(permission, provider.readPermission)) {
						supported = true;
					}
				}
			}
		} catch (NameNotFoundException e) {
			// Gmail app not found
		}
		return supported;
	}
}
