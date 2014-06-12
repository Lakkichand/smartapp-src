package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.desks.diy.INotificationId;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageManager;
import com.jiubang.ggheart.components.DeskResources;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.OnlineThemeGetter;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 主题管理Activity
 * 
 * @author yangbing
 * */
public class ThemeManageActivity extends Activity {

	// public static final String ACTION_FEATURED_THEME_CHANGED =
	// "android.intent.action.FEATURED_THEME_CHANGED";
	// public static final String ACTION_NEW_THEME_INSTALLED =
	// "android.intent.action.NEW_THEME_INSTALLED";

	private ThemeManageView mThemeManageView; // 主界面

	public static boolean sRefreshFlag = false;
	private BroadcastReceiver mThemeChangeReceiver;
	public static boolean sRuning = false;
	private boolean mStoped = false;

	/**
	 * 是否已经绑定下载服务的标记
	 */
	private boolean mHasBindService = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		confirmOrientation();
		registerThemeChangedReceiver();
		Intent intent = getIntent();
		String viewTabName = intent.getAction();

		if (ICustomAction.ACTION_THEME_PAYVIPPAGE.equals(viewTabName)) {
			Intent it = new Intent(ThemeManageActivity.this, ThemeVipPage.class);
			mThemeManageView = new ThemeManageView(this, ThemeManageView.LAUNCHER_THEME_VIEW_ID,
					OnlineThemeGetter.TAB_LAUNCHER_FEATURED_ID);
			String url = ThemeManager.getVipPayPageUrl(ThemeManageActivity.this);
			it.putExtra("url", url);
			startActivity(it);
		} else if (ICustomAction.ACTION_SHOW_THEME_PREVIEW.equals(viewTabName)) {

			int tab = intent.getExtras().getInt(ThemeConstants.TAB_THEME_KEY);
			mThemeManageView = new ThemeManageView(this, ThemeManageView.LAUNCHER_THEME_VIEW_ID,
					tab);
			if (tab == OnlineThemeGetter.TAB_LAUNCHER_FEATURED_ID) {
				PreferencesManager sp = new PreferencesManager(this,
						IPreferencesIds.SHAREDPREFERENCES_MSG_THEME_NOTIFY_STATICS_DATA,
						Context.MODE_PRIVATE);
				int cnt = sp.getInt(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES, 0);
				sp.putInt(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES, cnt + 1);
				sp.commit();
				MessageManager.getMessageManager(getApplication()).updateThemeNotifyStatisticsData(
						ThemeConstants.STATICS_ID_FEATURED_NOTIFY, 0, false);
				StatisticsData.saveGuiTabStat(this,
						String.valueOf(ThemeConstants.STATICS_ID_FEATURED));
			} else if (tab == OnlineThemeGetter.TAB_LAUNCHER_HOT_ID) {
				StatisticsData.saveGuiTabStat(this, String.valueOf(ThemeConstants.STATICS_ID_HOT));
			}
		} else if (ICustomAction.ACTION_SHOW_LOCKER_THEME_PREVIEW.equals(viewTabName)) {
			mThemeManageView = new ThemeManageView(this, ThemeManageView.LOCKER_THEME_VIEW_ID, 0);
			PreferencesManager sp = new PreferencesManager(this,
					IPreferencesIds.SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_STATICS_DATA,
					Context.MODE_PRIVATE);
			int cnt = sp.getInt(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES, 0);
			sp.putInt(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES, cnt + 1);
			sp.commit();
			MessageManager.getMessageManager(getApplication()).updateThemeNotifyStatisticsData(
					ThemeConstants.STATICS_ID_LOCKER_NOTIFY, 0, false);
			StatisticsData.saveGuiTabStat(this, String.valueOf(ThemeConstants.STATICS_ID_LOCKER));
		} else {
			Bundle bundle = getIntent().getExtras();
			int mEntrance = ThemeManageView.LAUNCHER_THEME_VIEW_ID;
			if (bundle != null) {
				mEntrance = bundle.getInt("entrance");
			}
			mThemeManageView = new ThemeManageView(this, mEntrance, 0);
			if (mEntrance == ThemeManageView.LAUNCHER_THEME_VIEW_ID) {
				StatisticsData.saveGuiTabStat(this,
						String.valueOf(ThemeConstants.STATICS_ID_FEATURED));
			} else {
				StatisticsData.saveGuiTabStat(this,
						String.valueOf(ThemeConstants.STATICS_ID_LOCKER));
			}
		}
		setContentView(mThemeManageView);
		sRuning = true;
		mStoped = false;
		sRefreshFlag = false;

		// 先启动下载服务
		GOLauncherApp.getContext().startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
		// 再bind服务
		if (!mHasBindService) {
			mHasBindService = GOLauncherApp.getContext().bindService(
					new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), mConnenction,
					Context.BIND_AUTO_CREATE);
		}
		clearAllNotifyData();
		StatisticsData.saveGuiEntry(this);
		checkShowVipTip();
		//		showVipTip();
	}

	/**
	 * 下载服务的控制接口Connector
	 */
	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IDownloadService mController = IDownloadService.Stub.asInterface(service);
			// 设置整个进程通用的下载控制接口
			GOLauncherApp.getApplication().setDownloadController(mController);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("", "Theme onServiceDisconnected");
			GOLauncherApp.getApplication().setDownloadController(null);
		}
	};

	@Override
	public Resources getResources() {
		DeskResourcesConfiguration configuration = DeskResourcesConfiguration.createInstance(this
				.getApplicationContext());
		if (null != configuration) {
			Resources resources = configuration.getDeskResources();
			if (null != resources) {
				return resources;
			}
		}
		return super.getResources();
	}

	private void registerThemeChangedReceiver() {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addAction(ICustomAction.ACTION_FEATURED_THEME_CHANGED);
		intentFilter.addAction(ICustomAction.ACTION_ZIP_THEME_REMOVED);
		intentFilter.addAction(ICustomAction.ACTION_NEW_THEME_INSTALLED);
		intentFilter.addAction(ICustomAction.ACTION_BANNER_DATA_CHANGEED);
		intentFilter.addDataScheme("package");
		intentFilter.setPriority(Integer.MAX_VALUE);
		mThemeChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String packageName = intent.getDataString();
				if (ICustomAction.ACTION_FEATURED_THEME_CHANGED.equals(intent.getAction())) {
					if (mThemeManageView != null) {
						mThemeManageView.updateMainView(-1, -1);
					}
				} else if (ICustomAction.ACTION_NEW_THEME_INSTALLED.equals(intent.getAction())) {
					mThemeManageView.addNewLog();
					ThemeDataManager.getInstance(context).clearup();
					if (mThemeManageView != null && !mStoped) {
						mThemeManageView.updateMainView(-1, -1);
					} else {
						sRefreshFlag = true;
					}
				} else if (packageName.contains(ThemeConstants.LAUNCHER_THEME_PREFIX)
						|| packageName.contains(ThemeConstants.LOCKER_THEME_PREFIX)
						|| ICustomAction.ACTION_ZIP_THEME_REMOVED.equals(intent.getAction())) {
					ThemeDataManager.getInstance(context).clearup();
					if (mThemeManageView != null && !mStoped) {
						mThemeManageView.updateMainView(-1, -1);
					} else {
						sRefreshFlag = true;
					}
				} else if (packageName.contains(LauncherEnv.Plugin.LOCKER_PACKAGE)) {
					if (mThemeManageView != null
							&& mThemeManageView.getEntranceId() == ThemeManageView.LOCKER_THEME_VIEW_ID) {
						mThemeManageView.updateMainView(ThemeManageView.LAUNCHER_THEME_VIEW_ID, -1);
					}
				} else if (ICustomAction.ACTION_BANNER_DATA_CHANGEED.equals(intent.getAction())) {
					if (mThemeManageView != null) {
						int type = intent.getIntExtra("type",
								ThemeConstants.LAUNCHER_FEATURED_THEME_ID);
						mThemeManageView.reLoadBannerData(type);
					}
				}
				// }
			}
		};
		try {
			registerReceiver(mThemeChangeReceiver, intentFilter);
		} catch (Throwable e) {
			try {
				unregisterReceiver(mThemeChangeReceiver);
				registerReceiver(mThemeChangeReceiver, intentFilter);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		confirmOrientation();
		sRuning = true;
		mStoped = false;
		if (sRefreshFlag) {
			mThemeManageView.updateMainView(-1, -1);
			sRefreshFlag = false;
		}
		mThemeManageView.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mStoped = true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent == null || mThemeManageView == null) {
			return;
		}

		String viewTabName = intent.getAction();
		if (ICustomAction.ACTION_THEME_PAYVIPPAGE.equals(viewTabName)) {
			Intent it = new Intent(this, ThemeVipPage.class);
			String url = ThemeManager.getVipPayPageUrl(ThemeManageActivity.this);
			it.putExtra("url", url);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
		} else if (ICustomAction.ACTION_SHOW_THEME_PREVIEW.equals(viewTabName)) {
			// 点击通知栏，刷新桌面主题界面
			int tab = intent.getExtras().getInt(ThemeConstants.TAB_THEME_KEY);
			if (tab == OnlineThemeGetter.TAB_LAUNCHER_FEATURED_ID) {
				mThemeManageView.updateMainView(ThemeManageView.LAUNCHER_THEME_VIEW_ID,
						ThemeManageView.FEATURED_THEME_VIEW_ID);
			} else {
				mThemeManageView.updateMainView(ThemeManageView.LAUNCHER_THEME_VIEW_ID,
						ThemeManageView.HOT_THEME_VIEW_ID);
			}
		} else if (ICustomAction.ACTION_SHOW_LOCKER_THEME_PREVIEW.equals(viewTabName)) {
			// 点击通知栏，刷新锁屏主题界面
			mThemeManageView.updateMainView(ThemeManageView.LOCKER_THEME_VIEW_ID,
					ThemeManageView.FEATURED_THEME_VIEW_ID);
		}

	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		sRuning = false;
		super.onDestroy();
		GuiThemeStatistics.setCurrentEntry(GuiThemeStatistics.ENTRY_TYPE_OTHER, this);
		if (mThemeChangeReceiver != null) {
			unregisterReceiver(mThemeChangeReceiver);
		}
		ThemeImageManager.getInstance(this).destory();
		ThemeDataManager.getInstance(this).clearup();
		mThemeManageView.onDestroy();
		ThemePurchaseManager.getInstance(getApplicationContext()).destory();
		// 解除下载服务的绑定
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			GOLauncherApp.getApplication().setDownloadController(null);
			mHasBindService = false;
		}
		exit();
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Resources res = getResources();
		if (res instanceof DeskResources) {
			res.updateConfiguration(super.getResources().getConfiguration(), super.getResources()
					.getDisplayMetrics());

			try {
				Configuration config = res.getConfiguration(); // 获得设置对象
				DisplayMetrics dm = res.getDisplayMetrics(); // 获得屏幕参数：主要是分辨率，像素等。
				PreferencesManager preferences = new PreferencesManager(this,
						IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
				String currentlanguage = preferences.getString(
						IPreferencesIds.CURRENTSELETELANGUAGE, "");
				if (currentlanguage != null && !currentlanguage.equals("")) {
					if (currentlanguage.length() == 5) {
						String language = currentlanguage.substring(0, 2);
						String country = currentlanguage.substring(3, 5);
						config.locale = new Locale(language, country);
					} else {
						config.locale = new Locale(currentlanguage);
					}
					res.updateConfiguration(config, dm);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//		DisplayMetrics mMetrics = getResources().getDisplayMetrics();
		//		if (mMetrics.widthPixels <= mMetrics.heightPixels) {
		//			SpaceCalculator.setIsPortrait(true);
		//		} else {
		//			SpaceCalculator.setIsPortrait(false);
		//		}
		//TODO
		confirmOrientation();
		if (null != mThemeManageView) {
			mThemeManageView.changeOrientation();
		}
	}

	/**
	 * 判断是横屏还是竖屏
	 * */
	public void confirmOrientation() {
		DisplayMetrics mMetrics = getResources().getDisplayMetrics();
		if (mMetrics.widthPixels <= mMetrics.heightPixels) {
			SpaceCalculator.setIsPortrait(true);
			SpaceCalculator.getInstance(this).calculateItemViewInfo();
		} else {
			SpaceCalculator.setIsPortrait(false);
			SpaceCalculator.getInstance(this).calculateThemeListItemCount();
		}
		//		SpaceCalculator.getInstance(this).calculateThemeListItemCount();
	}

	public static void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * 跳转到gostore下载更多
	 * */
	public void gotoGoStore() {
		if (mThemeManageView != null) {
			mThemeManageView.gotoGoStore();
		}
	}

	/**
	 * 跳转到Banner的列表内容
	 */
	public void gotoBannerList(int ty, String title) {
		Intent intent = new Intent();
		intent.putExtra("ty", ty);
		intent.putExtra("title", title);
		intent.setClass(this, BannerDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		this.startActivity(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// 传递到ThemeManagerView中处理
		Boolean bool = mThemeManageView.keyDown(keyCode, event);
		if (bool) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	private void clearAllNotifyData() {
		ThemeManager.getInstance(getApplicationContext()).removeNotifyBean(
				ThemeConstants.LAUNCHER_FEATURED_THEME_ID);
		ThemeManager.getInstance(getApplicationContext()).removeNotifyBean(
				ThemeConstants.LAUNCHER_HOT_THEME_ID);
		ThemeManager.getInstance(getApplicationContext()).removeNotifyBean(
				ThemeConstants.LOCKER_FEATURED_THEME_ID);

	}

	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void checkShowVipTip() {
		int level = ThemePurchaseManager.getCustomerLevel(this);
		if (level == ThemeConstants.CUSTOMER_LEVEL0) {
			PreferencesManager pm = new PreferencesManager(this,
					IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
			boolean bool = pm.getBoolean(IPreferencesIds.HAD_SHOW_VIP_TIP, false);
			if (!bool) {
				ArrayList<String> pkgs = ThemePurchaseManager.getInstance(this).getPaidPkgs();
				if (pkgs != null) {
					for (int i = 0; i < pkgs.size(); i++) {
						String pkg = pkgs.get(i);
						if (AppUtils.isAppExist(this, pkg)) {
							showVipTip();
							ThemePurchaseManager.clearPaidThemePkgList();
							pm.putBoolean(IPreferencesIds.HAD_SHOW_VIP_TIP, true);
							pm.commit();
							break;
						}
					}
				}
			}
		}
	}

	private void showVipTip() {
		Intent intent = new Intent(this, ThemeManageActivity.class);
		intent.setAction(ICustomAction.ACTION_THEME_PAYVIPPAGE);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		AppUtils.sendNotification(this, intent, R.drawable.icon,
				getString(R.string.vip_notification_tip), getString(R.string.vip_notification_tip),
				getString(R.string.vip_notification_tip), INotificationId.THEME_VIP_NOTIFICATION);
	}
}
