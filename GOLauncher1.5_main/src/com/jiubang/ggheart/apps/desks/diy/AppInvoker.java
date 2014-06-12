package com.jiubang.ggheart.apps.desks.diy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.go.util.log.LogConstants;
import com.go.util.window.WindowControl;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.download.DefaultDownloadListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.ServiceCallbackDownload;
import com.jiubang.ggheart.appgame.recommend.AppKitsActivity;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DockFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageView;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.advert.AdvertConstants;
import com.jiubang.ggheart.components.advert.AdvertControl;
import com.jiubang.ggheart.components.diygesture.gesturemanageview.DiyGestureRecogniser;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureModelImpl;
import com.jiubang.ggheart.components.gohandbook.GoHandBookMainActivity;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.model.ScreenDataModel;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.StatisticsAppsInfoData;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.recommend.localxml.XmlRecommendedApp;
import com.jiubang.ggheart.recommend.localxml.XmlRecommendedAppInfo;

/**
 * 应用启动模块 1.统一做桌面所有Activitiy跳转的工作 2.过滤特殊应用的处理，如内部事件等 3.对外层提供启动监听
 * 
 * @author yuankai
 * @version 1.0
 */
// CHECKSTYLE:OFF
public class AppInvoker {
	public interface IAppInvokerListener {
		void onInvokeApp(Intent intent);
	}

	private Activity mActivity;
	private ArrayList<IAppInvokerListener> mListener = new ArrayList<IAppInvokerListener>();

	/**
	 * 构造方法
	 * 
	 * @param messageSender
	 *            消息发送接口
	 * @param appmanagerFacade
	 *            后台启动接口
	 * @param activity
	 *            上下文
	 * @throws IllegalArgumentException
	 *             参数为空时抛的异常
	 */
	public AppInvoker(Activity activity, IAppInvokerListener... listeners) {
		mActivity = activity;
		for (IAppInvokerListener listener : listeners) {
			mListener.add(listener);
		}
	}

	/**
	 * 设置回调观察者
	 * 
	 * @param listener
	 */
	public void addListener(IAppInvokerListener listener) {
		mListener.add(listener);
	}

	public void removeListener(IAppInvokerListener listener) {
		mListener.remove(listener);
	}

	/**
	 * 通过Intent启动一个应用
	 * 
	 * @param launchIntent
	 *            对应的Intent
	 * @return 是否启动成功
	 */
	public boolean invokeApp(final Intent launchIntent) {
		return invokeApp(launchIntent, null, null);
	}

	/**
	 * 通过Intent启动一个应用, 并加入最近打开
	 * 
	 * @param launchIntent
	 *            对应的Intent
	 * @param rect
	 *            intent 对应的图标所在的位置
	 * @return 是否启动成功
	 */
	public boolean invokeApp(final Intent launchIntent, final Rect rect, boolean[] result) {
		// 保护
		if (launchIntent == null) {
			return false;
		}
		Intent actionIntent = new Intent(launchIntent);
		boolean ret = false;
		// launchIntent.addFlags改变了传过来的intent，如果是快捷方式，
		// 原来的intent和改变后的比较会不同，导致向 sysshortcut多加了一个
		// launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// 自定义的action返回false
		if (invokeFilter(actionIntent)) {
			if (result != null) {
				result[0] = true;
				return true;
			}
			return false;
		}

		//add by licanhui 15屏广告8小时请求
		AdvertControl.getAdvertControlInstance(mActivity).setAdvertAppIsOpen(actionIntent);
		//end by licanhui
		ret = doInvoke(actionIntent, rect);
		
		CommonControler.getInstance(mActivity).recommendAppBeOpen(launchIntent);
		return ret;
	}

	/**
	 * 通过DBItemInfo启动一个应用， 并加入最近打开
	 * 
	 * @param dbItemInfo
	 *            对应的数据结构
	 * @return 是否启动成功
	 */
	public boolean invokeApp(final AppItemInfo dbItemInfo) {
		if (dbItemInfo == null || dbItemInfo.mIntent == null) {
			return false;
		}

		boolean ret = false;
		// 自定义的Action，返回false
		if (invokeFilter(dbItemInfo.mIntent)) {
			return false;
		}

		ret = doInvoke(dbItemInfo);
		return ret;
	}

	/**
	 * 带回调的启动应用
	 * 
	 * @param requestCode
	 *            请求码
	 * @param launchIntent
	 *            对应的Intent
	 * @return 是否启动成功
	 */
	public boolean invokeAppForResult(final int requestCode, final Intent launchIntent) {
		if (launchIntent == null) {
			return false;
		}

		boolean ret = false;
		// TODO 带回调的启动是否要加入到统计和最新打开之中？？
		try {
			mActivity.startActivityForResult(launchIntent, requestCode);
			ret = true;
		} catch (ActivityNotFoundException e) {
			DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
		} catch (Throwable e) {
			DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
		}
		return ret;
	}

	private boolean doInvoke(final Intent intent, final Rect bounds) {
		Intent copyIntent = intent;
		boolean ret = false;
		if (bounds != null) {
			copyIntent = new Intent(intent);
			try {
				// API LEVEL = 7
				copyIntent.setSourceBounds(bounds);
			} catch (NoSuchMethodError e) {
				e.printStackTrace();
			}
		}

		try {
			mActivity.startActivity(copyIntent);
			ret = true;
		} catch (ActivityNotFoundException e) {
			// NOTE判断电子市场
			String pkgString = null;
			if (copyIntent != null && copyIntent.getComponent() != null) {
				pkgString = copyIntent.getComponent().getPackageName();
			}
			if (null != pkgString && pkgString.equals("com.android.vending")) {
				PackageManager pm = GOLauncherApp.getContext().getPackageManager();
				try {
					Intent intent2 = pm.getLaunchIntentForPackage(pkgString);
					if (null != intent2) {
						// NOTE:修改DB
						String oldMarketIntentStr = ConvertUtils.intentToString(intent);
						String newMarketIntentStr = ConvertUtils.intentToString(intent2);

						DataProvider dataProvider = DataProvider.getInstance(GoLauncher
								.getContext());
						dataProvider.replaceOldMarketToNewMarket(oldMarketIntentStr,
								newMarketIntentStr);

						mActivity.startActivity(intent2);
					}
				} catch (Throwable e2) {
				}
			}

			DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			boolean showToast = true;
			try {
				if (copyIntent != null && copyIntent.getAction() != null) {
					/*
					 * 在galaxyS 3机子上创建的直接拨号快捷方式，action为
					 * android.intent.action.CALL_PRIVILEGED,
					 * 但是android.permission
					 * .CALL_PRIVILEGED权限只允许系统app使用，所以要copy一份intent出来，改变action
					 */
					if (copyIntent.getAction().equals(ICustomAction.ACTION_CALL_PRIVILEGED)) {
						Intent intent2 = new Intent(copyIntent);
						intent2.setAction(ICustomAction.ACTION_CALL);
						mActivity.startActivity(intent2);
						showToast = false;
					}
				}
			} catch (Throwable e2) {
			}
			if (showToast) {
				DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT)
						.show();
			}
		} catch (Exception e) {
			DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
		}

		// add by huyong 2011-12-27 for all appsinfo
		if (ret) {
			// 防止ANR问题
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					addAppActiveCount(intent);
				}
			});
			thread.start();
		}
		// add by huyong 2011-12-27 for all appsinfo end

		// 通知
		if (null != mListener) {
			for (IAppInvokerListener listener : mListener) {
				listener.onInvokeApp(intent);
			}
		}
		return ret;
	}

	private void addAppActiveCount(final Intent intent) {
		AppItemInfo info = AppDataEngine.getInstance(mActivity).getAppItem(intent);
		if (info != null) {
			info.addActiveCount(mActivity, 1);
		}
		StatisticsAppsInfoData.addAppInfoClickedCount(intent, mActivity);
	}

	private boolean doInvoke(AppItemInfo itemInfo) {
		if (null == itemInfo || null == itemInfo.mIntent) {
			return false;
		}

		// 若是文件夹或widget，则不启动
		if (IItemType.ITEM_TYPE_USER_FOLDER == itemInfo.mID
				|| IItemType.ITEM_TYPE_APP_WIDGET == itemInfo.mID) {
			return false;
		}
		return doInvoke(itemInfo.mIntent, null);
	}

	/**
	 * 过滤启动的Intent
	 * 
	 * @param launchIntent
	 *            被过滤的Intent
	 * @return 是否被过滤，如果是，外部不需要再启动
	 */
	private boolean invokeFilter(final Intent launchIntent) {
		// 此处不用判空，提高效率
		final String action = launchIntent.getAction();

		// 判断是否为中国用户，作为下载推荐应用的参数 add by chenguanyu
		boolean isCnUser = Machine.isCnUser(mActivity);

		if (action == null) {
			return false;
		}

		if (ICustomAction.ACTION_SHOW_FUNCMENU.equals(action)) {
			// 打开功能表
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_APP_DRAWER,
					-1, null, null);
			return true;
		} else if (ICustomAction.ACTION_NONE.equals(action)) {
			// do nothing
			return true;
		} else if (ICustomAction.ACTION_BLANK.equals(action)) {
			// do nothing
			return true;
		} else if (ICustomAction.ACTION_SHOW_HIDE_STATUSBAR.equals(action)) {
			// 显示/隐藏状态栏
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -1, null, null);
			return true;
		} else if (ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW.equals(action)) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_SHOW_MAIN_SCREEN_OR_PREVIEW, -1, null, null);
			return true;
		} else if (ICustomAction.ACTION_SHOW_MAIN_SCREEN.equals(action)) {
			// 防止go快捷方式图标,DOCKgo快捷方式,Dock手势,自定义手势 中相应跳主屏幕功能失效
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 100, null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_SHOW_HOME,
					-1, null, null);
			return true;
		} else if (ICustomAction.ACTION_SHOW_PREVIEW.equals(action)) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_SHOW_PREVIEW,
					0, null, null);

			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
					IDiyMsgIds.PREVIEW_MAKE_TIP, -1, null, null);
			return true;
		} else if (ICustomAction.ACTION_SHOW_MENU.equals(action)) {
			// add by jiang
			String s = launchIntent.getStringExtra("screenlocked");
			if (s != null && s.equals("screenlocked")) {
				// 关闭提示
				if (GoLauncher.getTopFrame() != null
						&& (GoLauncher.getTopFrame().getId() == IDiyFrameIds.GUIDE_GL_FRAME)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
				}
				// 关闭文件夹
				if (GoLauncher.getTopFrame() != null
						&& (GoLauncher.getTopFrame().getId() == IDiyFrameIds.DESK_USER_FOLDER_FRAME)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
							null, null);
				}
				// 关闭功能表
				if (GoLauncher.getTopFrame() != null
						&& (GoLauncher.getTopFrame().getId() == IDiyFrameIds.APPFUNC_FRAME)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
				}
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_MENU, 1,
						null, null);
			} else {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_MENU, -1,
						null, null);
			}
			return true;
		} else if (ICustomAction.ACTION_TURN_SCREEN.equals(action)) {
			GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_TURN_SCREEN, -1,
					null, null);
			return true;
		} else if (ICustomAction.ACTION_SHOW_EXPEND_BAR.equals(action)) {
			try {
				WindowControl.setIsFullScreen(mActivity, false);
				WindowControl.expendNotification(mActivity);
			} catch (Exception e) {
				Log.i(LogConstants.HEART_TAG, e.toString());
			}
			return true;
		} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME.equals(action)) {
			// 功能表中go主题假图标被点击
			// MyThemes Process
			Intent mythemesIntent = new Intent();
			mythemesIntent.putExtra("entrance", ThemeManageView.LAUNCHER_THEME_VIEW_ID);
			mythemesIntent.setClass(mActivity, ThemeManageActivity.class);
			mActivity.startActivity(mythemesIntent);
			addAppActiveCount(launchIntent);
			GuiThemeStatistics.setCurrentEntry(GuiThemeStatistics.ENTRY_DESK_ICON, mActivity);
			return true;
		} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE.equals(action)) {
			// 功能表中go精品假图标被点击
//			Intent intent = new Intent();
//			intent.setClass(mActivity, GoStore.class);
//			mActivity.startActivity(intent);
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(GoLauncher.getContext(), 
					AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_ICO_GOSTORE);
			if (ChannelConfig.getInstance(mActivity).isNeedAppCenter()) {
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
			} else {
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_APPCENTER_RECOMMEND, false);
			}
			addAppActiveCount(launchIntent);

			return true;
		} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET.equals(action)) {
			// 功能表中gowidget假图标被点击
			// 修改为跳桌面添加的gowidget
			if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
				LockScreenHandler.showLockScreenNotification(mActivity);
			} else {
				if (GoLauncher.getTopFrame() != null
						&& (GoLauncher.getTopFrame().getId() == IDiyFrameIds.APPFUNC_FRAME)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
				}
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT, 1, BaseTab.TAB_GOWIDGET,
						null);
				addAppActiveCount(launchIntent);
			}
			return true;

		} else if (ICustomAction.ACTION_SHOW_PREFERENCES.equals(action)) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SCREEN_PREFERENCES, -1, null, null);
			return true;
		} else if (ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON.equals(action)) {
			// 打开功能表
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_APP_DRAWER,
					-1, null, null);
			return true;
		} else if (ICustomAction.ACTION_SHOW_LOCKER_SETTING.equals(action)) {
			// GO锁屏设置
			try {
				mActivity.startActivity(new Intent(ICustomAction.ACTION_LOCKER_SETTING));
			} catch (Exception e) {
				// TODO: handle exception
			}
			return true;
		} else if (ICustomAction.ACTION_SHOW_DOCK.equals(action)) {
			// 显示隐藏DOCK
			if (ShortCutSettingInfo.sEnable) {
				final int type = DiyGestureModelImpl.sOpeningActivityFlag == 0 ? DockFrame.HIDE_ANIMATION
						: DockFrame.HIDE_ANIMATION_NO;
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_HIDE, type,
						null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_HIDE, type,
						null, null);
			} else {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_SHOW,
						DockFrame.HIDE_ANIMATION, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_SHOW,
						DockFrame.HIDE_ANIMATION, null, null);
			}
			return true;
		} else if (ICustomAction.ACTION_ENABLE_SCREEN_GUARD.equals(action)) {
			// 锁屏
			if (AppUtils.isAppExist(mActivity, ICustomAction.PKG_GOWIDGET_SWITCH)) {
				PackageManager manager = mActivity.getPackageManager();
				try {
					PackageInfo info = manager.getPackageInfo(ICustomAction.PKG_GOWIDGET_SWITCH, 0);
					if (info != null && info.versionCode >= 18) {
						Intent it = new Intent(ICustomAction.ACTION_SWITCH_SERVICE);
						Bundle bundle = new Bundle();
						bundle.putInt("switchId", 12);
						it.putExtras(bundle);
						mActivity.startService(it);// 启动锁屏开关
					} else {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.SHOW_DOWNLOAD_GOSWITCHWIDGET_DIALOG, 1, null, null);
					}
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.SHOW_DOWNLOAD_GOSWITCHWIDGET_DIALOG, 0, null, null);
			}

			// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
			// IDiyMsgIds.ENABLE_KEYGUARD, -1, null, null);
			return true;
		} else if (ICustomAction.ACTION_SHOW_GOLOCKER_THEME.equals(action)) {// golocker主题
			AppUtils.gotoGolocker(mActivity);
			return true;
		} else if (ICustomAction.ACTION_RECOMMEND_DOWNLOAD.equals(action)) {
			StatisticsData.updateAppClickData(mActivity.getApplicationContext(),
					LauncherEnv.Plugin.RECOMMEND_PACKAGE);
			if (AppUtils.isMarketExist(mActivity)) {
				AppUtils.gotoMarket(mActivity, LauncherEnv.Market.APP_DETAIL
						+ LauncherEnv.Plugin.RECOMMEND_PACKAGE
						+ LauncherEnv.Plugin.RECOMMEND_GOOGLE_REFERRAL_LINK);
			} else {
				AppUtils.gotoBrowser(mActivity, LauncherEnv.Market.BROWSER_APP_DETAIL
						+ LauncherEnv.Plugin.RECOMMEND_PACKAGE);
			}
			return true;
		} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE_THEME.equals(action)) {
//			Intent it = new Intent(mActivity, GoStore.class);
//			Bundle bundle = new Bundle();
//			bundle.putString("sort", String.valueOf(SortsBean.SORT_THEME));
//			it.putExtras(bundle);
//			mActivity.startActivity(it);
//			StatisticsData.countStatData(mActivity, StatisticsData.ENTRY_KEY_GOFOLDER);
//			GoStoreStatisticsUtil
//					.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_THEME, mActivity);
			AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
					MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
			StatisticsData.countStatData(mActivity, StatisticsData.ENTRY_KEY_GOFOLDER);
			StatisticsAppsInfoData.addAppInfoClickedCount(launchIntent, mActivity);
			return true;
		} else if (ICustomAction.ACTION_SHOW_RECOMMENDLIST.equals(action)
				|| ICustomAction.ACTION_FUNC_SHOW_RECOMMENDLIST.equals(action)) {
			// 桌面快捷方式点击进入装机必备
			ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
			if (channelConfig != null && channelConfig.isNeedAppsKit()) {
				// 根据渠道配置信息，如果本渠道需要有装机必备
				AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mActivity,
						AppManagementStatisticsUtil.ENTRY_TYPE_DESK);
				StatisticsData.updateAppClickData(mActivity, action);
				Intent intent = new Intent(mActivity, AppKitsActivity.class);
				intent.putExtra(AppKitsActivity.ENTRANCE_KEY, AppKitsActivity.ENTRANCE_ID_SHORTCUTS);
				mActivity.startActivity(intent);
				// 一键装机增加点击数
				StatisticsAppsInfoData.addAppInfoClickedCount(launchIntent, mActivity);
			} else {
				// 如果本渠道不需要装机必备，提示找不到该应用
				// 主要是针对从有用户从有装机/玩机必备的渠道包，升级到没有装机/玩机必备的渠道包
				// 这时不能让它启动
				// Add by wangzhuobin 2012.08.10
				DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		} else if (ICustomAction.ACTION_SHOW_RECOMMENDGAME.equals(action)
				|| ICustomAction.ACTION_FUNC_SHOW_RECOMMENDGAME.equals(action)) {
			// 桌面或者功能表点击进入应用中心
			ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
			if (channelConfig != null && channelConfig.isNeedAppCenter()) {
				// 根据渠道配置信息，如果本渠道需要有应用中心
				AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mActivity,
						AppManagementStatisticsUtil.ENTRY_TYPE_APPFUNC_ICON);
				AppsManagementActivity.startAppCenter(mActivity, MainViewGroup.ACCESS_FOR_SHORTCUT, true);

				// 应用中心增加点击数
				StatisticsAppsInfoData.addAppInfoClickedCount(launchIntent, mActivity);
			} else {
				// 如果本渠道不需要应用中心，提示找不到该应用
				// 主要是针对从有用户从有应用中心的渠道包，升级到没有应用中心的渠道包
				// 这时不能让它启动
				// Add by wangzhuobin 2012.08.18
				DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		} else if (ICustomAction.ACTION_SHOW_RECOMMENDCENTER.equals(action)
				|| ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(action)) {
			// 桌面或者功能表点击进入应用中心
			ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
			if (channelConfig != null && channelConfig.isNeedAppCenter()) {
				// 根据渠道配置信息，如果本渠道需要有应用中心
				AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mActivity,
						AppManagementStatisticsUtil.ENTRY_TYPE_APPFUNC_ICON);
				AppsManagementActivity.startAppCenter(mActivity, MainViewGroup.ACCESS_FOR_SHORTCUT, true);

				// 应用中心增加点击数
				StatisticsAppsInfoData.addAppInfoClickedCount(launchIntent, mActivity);
			} else {
				// 如果本渠道不需要应用中心，提示找不到该应用
				// 主要是针对从有用户从有应用中心的渠道包，升级到没有应用中心的渠道包
				// 这时不能让它启动
				// Add by wangzhuobin 2012.08.18
				DeskToast.makeText(mActivity, R.string.activity_not_found, Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		} else if (ICustomAction.ACTION_SHOW_EVERNOTE.equals(action)) {
			StatisticsData.updateAppClickData(mActivity.getApplicationContext(),
					LauncherEnv.Plugin.RECOMMEND_EVERNOTE_PACKAGE);
			FileUtil.saveByteToSDFile(LauncherEnv.EVERNOTE_STRING.getBytes(),
					LauncherEnv.EVERNOTE_FILE_NAME);

			if (AppUtils.isMarketExist(mActivity)) {
				AppUtils.gotoMarket(mActivity, LauncherEnv.Market.APP_DETAIL
						+ LauncherEnv.Plugin.RECOMMEND_EVERNOTE_PACKAGE
						+ LauncherEnv.GOLAUNCHER_FOREVERNOTE_GOOGLE_REFERRAL_LINK);
			} else {
				AppUtils.gotoBrowser(mActivity, LauncherEnv.Market.BROWSER_APP_DETAIL
						+ LauncherEnv.Plugin.RECOMMEND_EVERNOTE_PACKAGE
						+ LauncherEnv.GOLAUNCHER_FOREVERNOTE_GOOGLE_REFERRAL_LINK);
			}
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_GOSMS_DOWNLOAD.equals(action)) {
			// go短信
			String title = mActivity.getString(R.string.recommand_gosms);
			String content = mActivity.getString(R.string.fav_app);
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK_FOR_GOFOLDER, title,
					System.currentTimeMillis(), isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_GOPOWERMASTER_DOWNLOAD.equals(action)) {
			// go省电
			String title = mActivity.getString(R.string.recommand_gopowermaster);
			String content = mActivity.getString(R.string.fav_app);
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK_FOR_GOFOLDER, title,
					System.currentTimeMillis(), isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_GOTASKMANAGER_DOWNLOAD.equals(action)) {
			// go任务管理器
			String title = mActivity.getString(R.string.recommand_gotaskmanager);
			String content = mActivity.getString(R.string.fav_app);
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK_FOR_GOFOLDER, title,
					System.currentTimeMillis(), isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_GOKEYBOARD_DOWNLOAD.equals(action)) {
			// go输入法
			String title = mActivity.getString(R.string.recommand_gokeyboard);
			String content = mActivity.getString(R.string.fav_app);
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK_FOR_GOFOLDER, title,
					System.currentTimeMillis(), isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_GOLOCKER_DOWNLOAD.equals(action)) {
			// go锁屏
			String title = mActivity.getString(R.string.customname_golocker);
			String content = mActivity.getString(R.string.fav_app);
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK_FOR_GOFOLDER, title,
					System.currentTimeMillis(), isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_GOBACKUP_DOWNLOAD.equals(action)) {
			// go备份
			String title = mActivity.getString(R.string.recommand_gobackup);
			String content = mActivity.getString(R.string.fav_app);
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK_FOR_GOFOLDER, title,
					System.currentTimeMillis(), isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_GOWEATHEREX_DOWNLOAD.equals(action)) {
			// go天气
			String title = mActivity.getString(R.string.recommand_goweatherex);
			String content = mActivity.getString(R.string.fav_app);
			if (AppUtils.isAppExist(mActivity, LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE)
					&& (AppUtils.getVersionCodeByPkgName(mActivity,
							LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE) < 10)) {
				// 如果go天气已安装且版本号低于10，点击图标后，提示用户要可更新
				content = mActivity.getString(R.string.fav_update);
			}
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK_FOR_GOFOLDER, title,
					System.currentTimeMillis(), isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} else if (ICustomAction.ACTION_SHOW_DIYGESTURE.equals(action)) {
			Intent intent = new Intent(mActivity, DiyGestureRecogniser.class);
			mActivity.startActivity(intent);

			return true;
		} else if (ICustomAction.ACTION_SHOW_GO_HANDBOOK.equals(action)) {
			Intent intent = new Intent(mActivity, GoHandBookMainActivity.class);
			mActivity.startActivity(intent);
			return true;
		} else if (ICustomAction.ACTION_SHOW_PHOTO.equals(action)) {
			if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
				if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
					// 打开功能表并进入图片管理界面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_IMAGE);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.IMAGE });
				}
			} else {
				showMedPlugDownDialog();
			}
			return true;
		} else if (ICustomAction.ACTION_SHOW_MUSIC.equals(action)) {
			if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
				if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
					// 打开功能表并进入音乐管理界面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_AUDIO);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.MUSIC });
				}
			} else {
				showMedPlugDownDialog();
			}
			return true;
		} else if (ICustomAction.ACTION_SHOW_VIDEO.equals(action)) {
			if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
				if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
					// 打开功能表并进入视频管理界面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_VIDEO);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.VIDEO });
				}
			} else {
				showMedPlugDownDialog();
			}
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_LOCKSCREEN_DOWNLOAD.equals(action)) {
			// 一键锁屏
			String title = mActivity.getString(R.string.recommand_lockscreen);
			String content = mActivity.getString(R.string.fav_app);
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.LOCK_SCREEN_REFERRAL_LINK, title, System.currentTimeMillis(),
					isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} else if (ICustomAction.ACTION_RECOMMAND_MEDIA_PLUGIN_DOWNLOAD.equals(action)) {
			// 资源管理插件
			String title = mActivity.getString(R.string.recommend_media_plugin);
			String content = mActivity.getString(R.string.fav_app);
			String[] linkArray = { launchIntent.getComponent().getPackageName(),
					launchIntent.getStringExtra(ScreenDataModel.RECOMMAND_APP_FTP_URL_KEY) };
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.Plugin.RECOMMEND_MEDIA_PLUGIN_GOOGLE_REFERRAL_LINK, title, System.currentTimeMillis(),
					isCnUser, CheckApplication.FROM_GO_FOLDER);
			return true;
		} 

		// 15屏的广告
		else if (ICustomAction.ACTION_SCREEN_ADVERT.equals(action)) {
			String packageNameString = launchIntent
					.getStringExtra(AdvertConstants.ADVERT_PACK_NAME);

			if (packageNameString == null) {
				return false;
			}
			
			String idString = launchIntent.getStringExtra(AdvertConstants.ADVERT_ID);
			String clickUrl = launchIntent.getStringExtra(AdvertConstants.ADVERT_CLICK_URL);
			String mapId = launchIntent.getStringExtra(AdvertConstants.ADVERT_MAPID);
			
			// 统计
			if (packageNameString != null && !packageNameString.equals("")) {
				StatisticsData.updateAppClickData(mActivity, packageNameString,
						AdvertConstants.ADVERT_STATISTICS_TYPE, mapId, idString);
			}
			AdvertControl advertControl = AdvertControl.getAdvertControlInstance(mActivity);
			advertControl.requestAdvertStatOnClick(packageNameString, idString, clickUrl, mapId);

//			boolean isAppExist = AppUtils.isAppExist(mActivity, packageNameString);
//			// 如果该包名则打开该应用
//			if (isAppExist) {
//				PackageManager pm = mActivity.getPackageManager();
//				try {
//					Intent intent = pm.getLaunchIntentForPackage(packageNameString);
//					if (null != intent) {
//						mActivity.startActivity(intent);
//					}
//				} catch (Throwable e2) {
//					e2.printStackTrace();
//				}
//			} else {
//				// 打开对应的action操作
//				String actionString = launchIntent.getStringExtra(AdvertConstants.ADVERT_ACTVALUE);
//				if (actionString != null) {
//					MessageManager.getMessageManager(mActivity).action(actionString);
//				}
//			}
			
			String title = launchIntent.getStringExtra(AdvertConstants.ADVERT_TITLE);
			String content = mActivity.getString(R.string.fav_app);
			String downloadUrl = launchIntent.getStringExtra(AdvertConstants.ADVERT_ACTVALUE);
			String[] linkArray = new String[2];
			String googleLink = null; // google分析链接
			linkArray[0] = packageNameString;
			
			if (downloadUrl.startsWith(ConstValue.PREFIX_MARKET)) {
				String subString = ConstValue.PREFIX_MARKET + packageNameString;
				googleLink = downloadUrl.substring(subString.length());
			}
			
			if (downloadUrl.startsWith(ConstValue.PREFIX_HTTP)) {
				linkArray[1] = downloadUrl;
			} else {
				linkArray[1] = null;
			}
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content,
					linkArray, googleLink, title, System.currentTimeMillis(),
					isCnUser, CheckApplication.FROM_RECOMMEND_APP);

			return true;
		} else if (ICustomAction.ACTION_DESK_SHOW_BAIDUBROWSER.equals(action)) {
			// 百度浏览器相关的实时统计信息
			String id = "85"; // 广告id
			String mapId = "2369953"; // 统计id
			// 回调url
			String clickUrl = "http://69.28.52.42:8090/recommendedapp/manage/appcallback.action?cburl=&ctype=0&pname=com.baidu.browser.inter&uid=&aid=&from=golaunchermsgAdv&mapid=2369953&corpid=2&referrer=utm_source%3Dgo_launcher%26utm_medium%3Dhyperlink%26utm_campaign%3Dhomescreen_ads";

			StatisticsData.updateAppClickData(
					mActivity.getApplicationContext(),
					LauncherEnv.Plugin.RECOMMEND_BAIDUBROWSER_PACKAGE,
					AdvertConstants.ADVERT_STATISTICS_TYPE, mapId, id);
			AdvertControl advertControl = AdvertControl.getAdvertControlInstance(mActivity);
			advertControl.requestAdvertStatOnClick(LauncherEnv.Plugin.RECOMMEND_BAIDUBROWSER_PACKAGE, id, clickUrl, mapId);
			AppUtils.gotoBrowser(mActivity, LauncherEnv.Url.BAIDU_BROWSER_URL);
			
			return true;
		} else {
			XmlRecommendedAppInfo info = XmlRecommendedApp.getRecommededAppInfoByAction(action);
			if (info != null) {
				// 统计,走15屏广告统计
				StatisticsData.updateAppClickData(mActivity.getApplicationContext(),
						info.mPackagename,AdvertConstants.ADVERT_STATISTICS_TYPE, info.mMapId, info.mId);
				
				AdvertControl advertControl = AdvertControl.getAdvertControlInstance(mActivity);
				advertControl.requestAdvertStatOnClick(info.mPackagename, info.mId, info.mClickurl, info.mMapId);
				if (info.mActType == XmlRecommendedAppInfo.GOTO_MARKET) {
					if (AppUtils.isMarketExist(mActivity)) {
						AppUtils.gotoMarket(mActivity, LauncherEnv.Market.APP_DETAIL
								+ info.mDownloadUrl);
					} else {
						AppUtils.gotoBrowser(mActivity, LauncherEnv.Market.BROWSER_APP_DETAIL
								+ info.mDownloadUrl);
					}
				} else {
					showDownLoadConfirmDialog(info);
				}
				return true;
			}
		}
		/*
		 * else if (ICustomAction.ACTION_GOOGLE_URI.equals(action)) { try {
		 * PackageManager pm = mActivity.getPackageManager(); Uri uri =
		 * Uri.parse("http://www.google.com"); Intent intent = new
		 * Intent(Intent.ACTION_VIEW, uri); //试图拿默认浏览器 ResolveInfo info =
		 * pm.resolveActivity(intent, 0); String pkg =
		 * info.activityInfo.applicationInfo.packageName;
		 * 
		 * //"android".equals(pkg) = true 意思是用户没设置默认浏览器 ResolveInfo resolveInfo
		 * = ("android".equals(pkg)) ? pm.queryIntentActivities(intent,
		 * 0).get(0) : info; String resolveInfoPkg =
		 * resolveInfo.activityInfo.applicationInfo.packageName; String
		 * resolveInfoCls = resolveInfo.activityInfo.name; Intent newIntent =
		 * new Intent(); ComponentName componentName = new
		 * ComponentName(resolveInfoPkg, resolveInfoCls);
		 * newIntent.setComponent(componentName);
		 * mActivity.startActivity(newIntent); } catch (Exception e) {
		 * DeskToast.makeText(mActivity, "Fail to start default browser",
		 * Toast.LENGTH_SHORT).show(); } return true; }
		 */

		// TODO 此处可添加任意多的过滤项
		// 过滤资源管理插件图标的响应事件，使用包名判断
		ComponentName componentName = launchIntent.getComponent();
		if (componentName != null) {
			String pkgName = componentName.getPackageName();
			if (pkgName.equals(PackageName.MEDIA_PLUGIN)) {
				if (MediaPluginFactory.isMediaPluginExist(GOLauncherApp.getContext())) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
					switch (AppFuncContentTypes.sType_for_setting) {
						case AppFuncContentTypes.IMAGE :
							DeliverMsgManager
									.getInstance()
									.onChange(
											AppFuncConstants.APP_FUNC_MAIN_VIEW,
											AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
											new Object[] { AppFuncContentTypes.IMAGE });
							break;
						case AppFuncContentTypes.MUSIC :
							DeliverMsgManager.getInstance().onChange(
									AppFuncConstants.APP_FUNC_MAIN_VIEW,
									AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
									new Object[] { AppFuncContentTypes.MUSIC });
							break;
						case AppFuncContentTypes.VIDEO :
							DeliverMsgManager
									.getInstance()
									.onChange(
											AppFuncConstants.APP_FUNC_MAIN_VIEW,
											AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
											new Object[] { AppFuncContentTypes.VIDEO });
							break;
						default :
							DeliverMsgManager
									.getInstance()
									.onChange(
											AppFuncConstants.APP_FUNC_MAIN_VIEW,
											AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
											new Object[] { AppFuncContentTypes.IMAGE });
							break;
					}
				}
				CommonControler.getInstance(mActivity).recommendAppBeOpen(launchIntent);
				return true;
			}
			
			else if (pkgName.equals(LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
				// 在桌面启动多屏多壁纸，则启动壁纸设置界面；如果第三方桌面则启动引导页
				if (AppUtils.isAppExist(mActivity, LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
					Intent intent = new Intent();
					intent.setClassName(AppUtils.getAppContext(mActivity, pkgName),
							"com.go.multiplewallpaper.MultipleWallpaperSettingActivity");
					intent.putExtra("isgolauncher", true);
					mActivity.startActivity(intent);
					return true;
				}
			}
			
		}
		return false;
	}

	private void showMedPlugDownDialog() {
		final Context context = GOLauncherApp.getContext();
		String textFirst = context.getString(R.string.download_mediamanagement_plugin_dialog_text_first);
		String textMiddle = context.getString(R.string.download_mediamanagement_plugin_dialog_text_middle);
		String textLast = context.getString(R.string.download_mediamanagement_plugin_dialog_text_last);
		SpannableStringBuilder messageText = new SpannableStringBuilder(textFirst + textMiddle + textLast);
		messageText.setSpan(new RelativeSizeSpan(0.8f), textFirst.length(), messageText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		messageText.setSpan(
				new ForegroundColorSpan(context.getResources().getColor(
						R.color.snapshot_tutorial_notice_color)), textFirst.length(),
				textFirst.length() + textMiddle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //设置提示为绿色
		
		DialogConfirm dialog = new DialogConfirm(mActivity);
		dialog.show();
		dialog.setTitle(R.string.download_mediamanagement_plugin_dialog_title);
		dialog.setMessage(messageText);
		dialog.setPositiveButton(R.string.download_mediamanagement_plugin_dialog_download_btn_text, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 跳转进行下载
				String packageName = PackageName.MEDIA_PLUGIN;
				String url = LauncherEnv.Url.MEDIA_PLUGIN_FTP_URL; // 插件包ftp地址
				String linkArray[] = { packageName, url };
				String title = context
						.getString(R.string.mediamanagement_plugin_download_title);
				boolean isCnUser = Machine.isCnUser(context);

				CheckApplication.downloadAppFromMarketFTPGostore(context, "",
						linkArray, LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK, title,
						System.currentTimeMillis(), isCnUser,
						CheckApplication.FROM_MEDIA_DOWNLOAD_DIGLOG);	
			}
		});
		dialog.setNegativeButton(R.string.download_mediamanagement_plugin_dialog_later_btn_text, null);
	}
	
	private void showDownLoadConfirmDialog(final XmlRecommendedAppInfo info) {
		if (info == null) {
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		String msg = mActivity.getString(R.string.recommended_download_dialog_msg);
		msg = msg.replace("%s", mActivity.getString(info.mTitle));
		builder.setMessage(msg).setTitle(R.string.recommended_download_dialog_title)
				.setCancelable(true)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						final String appName;
						if (info.mTitle > 0) {
							appName = mActivity.getString(info.mTitle);
						} else {
							appName = info.mPackagename;
						}
						//						IDownloadService mDownloadController = GOLauncherApp.getApplication()
						//								.getDownloadController();
						ServiceCallbackDownload.ServiceCallbackRunnable runnable = new ServiceCallbackDownload.ServiceCallbackRunnable() {
							@Override
							public void run() {
								boolean downloading = false;
								long trueTaskId = 0;
								if (mDownloadController == null) {
									return;
								}
								Map<Long, DownloadTask> downloadmap;
								try {
									downloadmap = mDownloadController
											.getDownloadConcurrentHashMap();
									for (DownloadTask task : downloadmap.values()) {
										if (info.mDownloadUrl.equals(task.getDownloadUrl())) {
											downloading = true;
											trueTaskId = task.getId();
											break;
										}
									}
									if (!downloading) {
//										StatisticsData.updateAppClickData(
//												mActivity.getApplicationContext(),
//												info.mPackagename);
										long taskId = System.currentTimeMillis();
										String fileName = appName.trim();
										String downloadUrl = info.mDownloadUrl.trim();
										String customDownloadFileName = taskId + "_"
												+ info.mPackagename + ".apk";
										String savePath = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH
												+ customDownloadFileName;
										DownloadTask task = new DownloadTask(taskId, downloadUrl,
												fileName, savePath);
										task.setIsApkFile(true);
										taskId = mDownloadController.addDownloadTask(task);
										if (taskId != -1) {
											// 添加默认的下载监听器
											mDownloadController.addDownloadTaskListenerByName(
													taskId, DefaultDownloadListener.class.getName());
											mDownloadController.startDownload(taskId);
										}
									} else {
										mDownloadController.startDownload(trueTaskId);
									}
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};
						ServiceCallbackDownload.callbackDownload(mActivity, runnable);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.create().show();
	}
	
	/**
	 * 启动浏览器
	 * @param context
	 * @param url
	 */
	private void startBrowser(Context context, String url) {
		Uri uri = Uri.parse(url);
		Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);
		try {

			// 1:已安装的浏览器列表
			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> resolveList = pm.queryIntentActivities(myIntent, 0);

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
											|| pkgString.equals("com.qihoo.browser")
											||pkgString.equals("com.UCMobile")
											||pkgString.equals("com.uc.browser")
											||pkgString.equals("com.tencent.mtt")) {
										//上述浏览器后台拉起会跳转浏览器首页，而非保存的用户原来页面
										funIntent.setAction(Intent.ACTION_VIEW);
									}
									funIntent.setData(uri);
									mActivity.startActivity(funIntent);
									return;
								}
							}
						}
					}

				}
				ResolveInfo resolveInfo = resolveList.get(0);
				String pkgString = resolveInfo.activityInfo.packageName;
				String activityName = resolveInfo.activityInfo.name;
				myIntent.setClassName(pkgString, activityName);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(myIntent);
				return;

			}
		} catch (Throwable e) {
			//有错，则不进行intent过滤
		}
	}
}
