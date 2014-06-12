package com.jiubang.ggheart.data;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.go.util.AppUtils;
import com.go.util.log.LogUnit;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 监听服务
 * 
 * @author guodanyang
 * 
 */
public class MonitorSver extends BroadCaster {
	private static final String MONITORSVERLOG = "MonitorSver";

	// 上下文
	private Context mContext;

	public static final int APPCHANGE = 3;
	public static final int EXTERNAL_APP_AVAILABLE = 4;
	public static final int EXTERNAL_APP_UNAVAILABLE = 5;

	// SD卡mount上了
	public static final int SDMOUNT = 6;
	public static final int SDSHARED = 7;

	// 程序变更状态
	public static final int FLAG_NONE = 0;
	public static final int FLAG_INSTALL = 1; // 安装
	public static final int FLAG_UNINSTALL = 2; // 卸载
	public static final int FLAG_CHANGE = 3; // 更改
	public static final int FLAG_UPDATE = 4; // 升级

	private BroadcastReceiver mFunAppReceiver; // 安装卸载
	private BroadcastReceiver mExternalAppsReceiver;
	private BroadcastReceiver msdMountReceiver;

	// private BroadcastReceiver mScreenReceiver;

	/**
	 * 构造
	 * 
	 * @param context
	 *            上下文
	 */
	public MonitorSver(Context context) {
		mContext = context;

		// 安装卸载监听
		installUninstallListener(context);

		// SD卡程序监控
		if (isSDK8()) {
			externalAppsListener(context);
		}

		// SD卡Mount的监控
		sdMountListener(context);
		// screenOnOffListener(context);
	}

	/**
	 * 取消注册
	 */
	public void unregister() {
		// 去注册
		try {
			mContext.unregisterReceiver(mFunAppReceiver);
			if (isSDK8()) {
				mContext.unregisterReceiver(mExternalAppsReceiver);
			}

			mContext.unregisterReceiver(msdMountReceiver);
			// mContext.unregisterReceiver(mScreenReceiver);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// 方便回收
		mFunAppReceiver = null;
		mExternalAppsReceiver = null;
	}

	/**
	 * 监听安装卸载
	 * 
	 * @param context
	 *            上下文
	 */
	public void installUninstallListener(Context context) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");

		mFunAppReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
						|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
						|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					final String packageName = intent.getData().getSchemeSpecificPart();

					// 需要判断是否覆盖安装，如果是覆盖安装，则不广播消息
					final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

					if (packageName == null || packageName.length() == 0) {
						// they sent us a bad intent
						return;
					}
					
					/**
					 * 这里处理的情况是为了处理与桌面主题同样包名的GO桌面美化版的情况
					 * Ad by  shenjinbao
					 */
					boolean isLauncherReplaceTheme = false ;
					/*
					 * 这里需要分隔第三方应用的更新情况，因为主题的包名命名规则都是有规律的，
					 * 要么是ThemeManager.MAIN_THEME_PACKAGE这个开头，要么是ThemeManager.OLD_THEME_PACKAGE
					 */
					if (replacing
							&& (packageName.contains(ThemeManager.MAIN_THEME_PACKAGE) || packageName
									.contains(ThemeManager.OLD_THEME_PACKAGE))) {

						isLauncherReplaceTheme = true;
						Intent intentTheme = new Intent(ThemeManager.MAIN_THEME_PACKAGE);
						intentTheme.addCategory(ThemeManager.THEME_CATEGORY);
						List<ResolveInfo> themes = mContext.getPackageManager()
								.queryIntentActivities(intentTheme, 0);
						int capacity = themes != null ? themes.size() : 0;
						for (int i = 0; i < capacity; i++) {
							if (themes.get(i).activityInfo.packageName.equals(packageName)) {
								isLauncherReplaceTheme = false;
								break;
							}
						}
					}

					int installFlag = FLAG_NONE;
					if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
						installFlag = FLAG_INSTALL;
						if (replacing) {
							installFlag = FLAG_UPDATE;
							//如果是GO桌面美化版更替主题的，就将Flag设置为卸载
							if (isLauncherReplaceTheme) {
								installFlag = FLAG_UNINSTALL;
							}
						}
					} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
						if (!replacing) {
							installFlag = FLAG_UNINSTALL;
						}
					} else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
						installFlag = FLAG_UPDATE;
						//add by huyong 2013-01-30 for 三星Note机测试（泽润）发现，安装桌面启动后，会收到ACTION_PACKAGE_CHANGED消息，在widget检查时，进而引起桌面重启，这里做过滤本桌面包处理
						if (LauncherEnv.PACKAGE_NAME.equals(packageName)) {
							return;
						}
						//add by huyong 2013-01-30 end
					}

					// add by huyong 2013-03-02 for 安装debughelper调试器后自动启动
					checkDebugHelperInstalled(packageName);
					// add by huyong 2013-03-02 for 安装debughelper调试器后自动启动 end
					
					Log.d(MONITORSVERLOG, "Monitor package Change flag = " + installFlag);
					broadCast(APPCHANGE, installFlag, intent, null);
				}
			}
		};

		try {
			context.registerReceiver(mFunAppReceiver, intentFilter);
		} catch (Throwable e) {
			try {
				context.unregisterReceiver(mFunAppReceiver);
				context.registerReceiver(mFunAppReceiver, intentFilter);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		}
	}

	public void externalAppsListener(Context context) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		intentFilter.addAction(ICustomAction.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);

		mExternalAppsReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.i(MONITORSVERLOG, "externalAppsListener -- action: " + action);
				if (action.equals(ICustomAction.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)) {
					// 通知
					broadCast(EXTERNAL_APP_AVAILABLE, 0, intent, null);
				} else if (action.equals(ICustomAction.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE)) {
					// 通知
					broadCast(EXTERNAL_APP_UNAVAILABLE, 0, intent, null);
				}
			}
		};
		try {
			context.registerReceiver(mExternalAppsReceiver, intentFilter);
		} catch (Throwable e) {
			try {
				context.unregisterReceiver(mExternalAppsReceiver);
				context.registerReceiver(mExternalAppsReceiver, intentFilter);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		}
	}

	public void sdMountListener(Context context) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(ICustomAction.ACTION_MEDIA_SHARED);
		intentFilter.addDataScheme("file");

		msdMountReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				Log.i(MONITORSVERLOG, "msdMountReceiver -- action: " + action);
				if (action.equals(ICustomAction.ACTION_MEDIA_MOUNTED)) {
					// 通知
					broadCast(SDMOUNT, 0, intent, null);
				} else if (action.equals(ICustomAction.ACTION_MEDIA_SHARED)) {
					broadCast(SDSHARED, 0, intent, null);
				}
			}
		};
		try {
			context.registerReceiver(msdMountReceiver, intentFilter);
		} catch (Throwable e) {
			try {
				context.unregisterReceiver(msdMountReceiver);
				context.registerReceiver(msdMountReceiver, intentFilter);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		}
	}

	// public void screenOnOffListener(Context context) {
	// IntentFilter intentFilter1 = new IntentFilter();
	// intentFilter1.addAction(Intent.ACTION_SCREEN_OFF);
	// intentFilter1.addAction(Intent.ACTION_SCREEN_ON);
	//
	// mScreenReceiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// // receive start slide show intent or screen_off message
	// if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
	// OutOfMemoryHandler.handle();
	// // receive stop slide show intent or screen_off message
	// } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
	// OutOfMemoryHandler.handle();
	// }
	// }
	//
	// };
	// context.registerReceiver(mScreenReceiver, intentFilter1);
	// }

	private boolean isSDK8() {
		String stringSDK = android.os.Build.VERSION.SDK;
		int sdkversion = Integer.valueOf(stringSDK).intValue();
		if (sdkversion > 7) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isUninstall(Context context, String packagename) {
		// 首先判断是不是桌面主题
		Intent it = new Intent(ICustomAction.ACTION_MAIN_THEME_PACKAGE);
		it.addCategory(LauncherEnv.THEME_CATEGORY);
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> themes = pm.queryIntentActivities(it, 0);
		int size = 0;
		if (themes != null) {
			size = themes.size();
		}
		for (int i = 0; i < size; i++) {
			String appPackageName = themes.get(i).activityInfo.packageName;
			if (packagename.equals(appPackageName)) {
				return true;
			}
		}

		// 再判断是不是GO锁屏主题
		Intent searchIntent = new Intent(ICustomAction.ACTION_GOLOCK_THEME);
		searchIntent.addCategory(LauncherEnv.GOLOCK_THEME_CATEGORY);
		List<ResolveInfo> lockThemes = pm.queryIntentActivities(searchIntent, 0);
		size = 0;
		if (lockThemes != null) {
			size = lockThemes.size();
		}
		for (int i = 0; i < size; i++) {
			String appPackageName = lockThemes.get(i).activityInfo.packageName;
			if (packagename.equals(appPackageName)) {
				return true;
			}
		}
		return false;
	}
	
	private void checkDebugHelperInstalled(String pkgName) {
		if (LauncherEnv.GO_DEBUG_HELPER_PKGNAME.equals(pkgName)
				&& AppUtils.getVersionCodeByPkgName(mContext, LauncherEnv.GO_DEBUG_HELPER_PKGNAME) > 1) {
			//启动debug
			Log.i("debugservice", "----1----MonitorSver-----checkDebugHelperInstalled-" + pkgName);
			LogUnit.startDebugService(mContext);
		}
	}
	
}