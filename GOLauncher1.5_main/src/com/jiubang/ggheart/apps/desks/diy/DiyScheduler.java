package com.jiubang.ggheart.apps.desks.diy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.LiveFolders;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import com.facebook.Session;
import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.ArrayUtils;
import com.go.util.ConvertUtils;
import com.go.util.Utilities;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.go.util.log.LogConstants;
import com.go.util.log.LogUnit;
import com.go.util.window.OrientationControl;
import com.go.util.window.WindowControl;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IDispatchEventHandler;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingMainActivity;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingQaTutorialActivity;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogLanguageChoice;
import com.jiubang.ggheart.apps.desks.appfunc.AllFoldersActivity;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.NotificationSettingActivity;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncFolderInfoToDesk;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverMonitor.ICoverCallback;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SensePreviewFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenEditAddGoLauncher;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenModifyFolderActivity;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Search;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageCenterActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageView;
import com.jiubang.ggheart.apps.desks.ggmenu.GGMenu;
import com.jiubang.ggheart.apps.desks.ggmenu.GGMenuData;
import com.jiubang.ggheart.apps.desks.ggmenu.GGMenuData.TabData;
import com.jiubang.ggheart.apps.desks.ggmenu.GGMenuProvider;
import com.jiubang.ggheart.apps.desks.ggmenu.GlMenuTabView;
import com.jiubang.ggheart.apps.desks.ggmenu.OnMenuItemSelectedListener;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeIconPreviewActivity;
import com.jiubang.ggheart.apps.desks.settings.AppList;
import com.jiubang.ggheart.apps.desks.settings.AppListMultiple;
import com.jiubang.ggheart.apps.desks.settings.DockAddLauncherActionDialog;
import com.jiubang.ggheart.apps.desks.settings.DockGestureRespond;
import com.jiubang.ggheart.apps.desks.settings.DockSettingDialog;
import com.jiubang.ggheart.apps.desks.settings.LauncherActionList;
import com.jiubang.ggheart.apps.desks.share.ShareFrame;
import com.jiubang.ggheart.apps.desks.snapshot.DialogUtil;
import com.jiubang.ggheart.apps.desks.snapshot.SnapShotManager;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreChannelControl;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.components.advert.AdvertControl;
import com.jiubang.ggheart.components.advert.AdvertUtils;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.components.gohandbook.GoHandBookMainActivity;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.AppService;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DataChangeListener;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.GlobalSetConfig;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.DBImport.DBImporter;
import com.jiubang.ggheart.data.DBImport.LauncherSelectorActivity;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ScreenLiveFolderInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.SysAppInfo;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.FunctionalStatistic;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.MenuBean;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.InstallShortcutReceiver;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.launcher.ThreadName;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.shell.ShellPluginFactory;
import com.jiubang.ggheart.recommend.localxml.XmlRecommendedApp;

/**
 * DIY桌面调度器 负责各层的创建和销毁等工作 负责与Activity外部的消息交互,并进行传递 负责与后台主题数据进行交互的层
 * 
 * @author yuankai
 * @version 1.0
 */
public class DiyScheduler
		implements
			IMessageHandler,
			KeyEvent.Callback,
			IDispatchEventHandler,
			BroadCasterObserver,
			WallpaperControler.IWallpaperChangeListener,
			OnDismissListener,
			ICoverCallback {
	public static final String LAST_MOTION_X = "lastMotionX";
	public static final String LAST_MOTION_Y = "lastMotionY";

	private static final String START_NOTIFICATION_THREAD_NAME = "start_notification_thread_name";

	private Activity mActivity;
	private int mId; // 消息处理ID
	private GestureHandler mGestureHandler; // 手势处理器
	private StatusBarHandler mStatusBarHandler; // 状态栏管理器
	private AppInvoker mAppInvoker; // 应用启动模块
	private FrameControl mFrameControl; // 帧管理器

	private boolean mIsNewIntent; // 是否是新的Intent, 用于接收home事件

	static final String EXTRA_CUSTOM_WIDGET = "custom_widget";
	static final String SEARCH_WIDGET = "search_widget";
	public final static String FIELD_INSERT_INDEX = "insert_index";
	static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

	private final static String SCREEN_LOCKED = "screenlocked";

	private int mLastOrientation; // 最近一次的屏幕方向
	private InstallShortcutReceiver mInstallShortcutReceiver; // 添加会计方式receiver

	private Locale mLastLocale; // 当前语言

	/**
	 * 请求添加快捷方式的操作，请求码在类IShortcutHolderIds里定义
	 */
	private int mShortcutHolerId;

	// 正在添加桌面组件时，不处理横竖屏事件
	private boolean mAddingComponent = false;

	// 判断是否从其他Activity返回
	private boolean mIsOut = false;

	// 自定义菜单
	private GGMenu mGGMenu;
	private boolean mNeedOpenGGMenu;

	private WallpaperControler mWallpaperControler = null;

	private boolean mIsKeyDown = false;
	/** 区分进入快捷方式的发起者 */
	private static final int DOCK_ACTION = 0x1;
	private static final int SCREEN_ACTION = 0x2;
	private static final int DOCK_GESTURE = 0x3;
	private int mShortcutSource = SCREEN_ACTION;
	// add by yangbing 2012-06-20
	private boolean mIsMusicBarIntent;

	private ArrayList<String> mApkLanguageArrayList;

	// private final static boolean ENABLE_SCREEN_CAPTURE =
	// DebugState.isScreenCaptureEnable();
	// 语言选择列表对话框
	private DialogLanguageChoice mLanguageChoiceDialog;
	// 罩子层
	private CoverFrame mCoverFrame;

	private MenuBean mMenuBean = null; //桌面菜单的Bean

	/**
	 * 调度器构造方法
	 * 
	 * @param activity
	 *            UI活动
	 * @param frameManager
	 *            帧管理器
	 * @param id
	 *            惟一ID标识
	 */
	public DiyScheduler(Activity activity, FrameControl frameControl, int id) {
		mWallpaperControler = WallpaperControler.buildInstance(activity, this);
		mActivity = activity;	
		mId = id;
		mFrameControl = frameControl;
		mFrameControl.initCachedFrame();	
		// 添加消息接收者
		GoLauncher.registMsgHandler(this);
		mApkLanguageArrayList = new ArrayList<String>();
		String[] supportLanguages = mActivity.getResources().getStringArray(
				R.array.support_language);
		for (String apkLanguage : supportLanguages) {
			mApkLanguageArrayList.add(apkLanguage);
		}
	}

	private void exit(boolean restart) {
		// 通知周边插件桌面退出
		mActivity.sendBroadcast(new Intent(ICustomAction.ACTION_LAUNCHER_EXIT));
		// 无论是重启还是退出，都把下载管理器销毁
		// DownloadManager.destroy();
		DataProvider.destroy();
		AppUtils.cancelNotificaiton(mActivity, INotificationId.ONE_X_GUIDE);
		AppUtils.cancelNotificaiton(mActivity, INotificationId.LANGUAGE_START_GGMENU);
		AppUtils.cancelNotificaiton(mActivity, INotificationId.MIGRATE_TIP);
		AppUtils.cancelNotificaiton(mActivity, INotificationId.SNAPSHOT_NOTIFICATION);
		if (restart) {
			AppUtils.killProcess();
		} else {
			mActivity.finish();
			// 停止服务
			stopAppService();
			// stopMessageService();
			AppCore.destroy();
			AppUtils.killProcess();
		}
	}

	private void listenDataChanged() {
		DataChangeListener listener = new DataChangeListener() {
			@SuppressWarnings("rawtypes")
			@Override
			public void dataChanged(int dataType, int param, Object object, List objects1) {
				if (DataType.DATATYPE_THEMESETTING == dataType) {
					switch (param) {
						case DataType.APPLY_CHANGE : // 设置更改
							checkPersistence();
							break;

						case DataType.EXIT_GOLAUNCHER :
							exit(false);
							break;

						case DataType.RESTART_GOLAUNCHER :
							exit(true);
							break;

						default :
							break;
					}
				}

				GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.APPCORE_DATACHANGE, dataType,
						object, objects1);
			}
		};

		if (null != GOLauncherApp.getApplication()) {
			GOLauncherApp.getApplication().setDataChangeListener(listener);
		}

		GOLauncherApp.getSettingControler().registerObserver(this);
	}

	private void initAppCore() {
		new Thread(ThreadName.SCHEDULER_INITIALIZE_APPCORE) {
			@Override
			public void run() {
				Looper.prepare();
				{
					// 加载配置文件数据
					GOLauncherApp.getGOLauncherConfig().roadConfig();
					//					ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
					//					if (channelConfig != null && channelConfig.isNeedDownloadService()) {
					//						GOLauncherApp.getApplication().getDownloadController();
					//					}
					// 启动桌面时，在其他桌面添加应用中心快捷方式，只会添加一次
					AppsManagementActivity.addShortcut(mActivity);
					// 暂时监听 TODO:AppCore完善后去除此处
					listenDataChanged();
					// 加载GoWidget信息
					AppCore.getInstance().getGoWidgetManager().prepareGoWidgetInfo();
					// 加载应用程序信息
					if (null != GOLauncherApp.getAppDataEngine()) {
						GOLauncherApp.getAppDataEngine().scanInitAllAppItems();
					}

					// 保存当前语言TODO:与DataModel的checkLanguage()统一
					PreferencesManager sp = new PreferencesManager(mActivity, IPreferencesIds.DIY,
							Context.MODE_PRIVATE);
					Locale locale = Locale.getDefault();
					String language = String.format(LauncherEnv.LANGUAGE_FORMAT,
							locale.getLanguage(), locale.getCountry());
					sp.putString(LauncherEnv.LANGUAGE, language);
					sp.commit();
				}
			}
		}.start();
	}

	/**
	 * 负责所有组件的初始化工作,统一地方
	 */
	private void init() {

		// 应用启动模块
		mAppInvoker = new AppInvoker(mActivity, AppDrawerControler.getInstance(mActivity),
				AppCore.getInstance().getNotificationControler());

		// 加载数据库数据 TODO:改名 不叫initAppCore
		initAppCore();

		// 手势处理器
		if (mFrameControl != null) {
			initGestureHandler(mFrameControl);
		}

		initStatusBarHandler();
		registReceivers();

		// 取消默认
		// showCancelDefaultLauncher();

		// 设置是否支持版本间数据共享
		if (GoLauncher.getContext().getNewVeriosnFirstRun()) {
			PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
					IPreferencesIds.DB_PROVIDER_SUPPORT, Context.MODE_WORLD_READABLE);

			if (!sharedPreferences.getBoolean(IPreferencesIds.IMPORT_SUPPORT, false)) {
				sharedPreferences.putBoolean(IPreferencesIds.IMPORT_SUPPORT, true);
				sharedPreferences.commit();
			}
		}
		// 获得是否是向导那边安装语言包或桌面搬家重启
		PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		String mode = sharedPreferences.getString("mode", GoGuideActivity.NORMAL_MODE);
		// boolean disAcceptAgreement =
		// sharedPreferences.getBoolean(LauncherEnv.DISACCEPT_AGREEMENT, false);
		// 新版本第一次运行
		if (null != GoLauncher.getContext() && GoLauncher.getContext().getNewVeriosnFirstRun()) {
			Intent intent = null;
			intent = new Intent(mActivity, GoGuideActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mActivity.startActivity(intent);
			// 版本更新
			// showTip();

			// 语言包
			// if (null != DeskResourcesConfiguration.getInstance())
			// {
			// int error =
			// DeskResourcesConfiguration.getInstance().getErrorCode();
			// if (DeskResourcesConfiguration.ERROR_LANGUAGE_NO_INSTALL == error
			// || DeskResourcesConfiguration.ERROR_LANGUAGE_NEED_UPDATE ==
			// error)
			// {
			// showInstallLanguageTip();
			// }
			// }
			// }

			// 搬家
			// 第一次运行
			// if (null != GoLauncher.getContext()
			// && GoLauncher.getContext().getFirstRun())
			// {
			// if (AppUtils.isAppExist(mActivity, "com.ma.deskmigrate"))
			// {
			// showMigrateTip();
			// }
			// else
			// {
			// showMigrateAppTip();
			// }
			sharedPreferences = new PreferencesManager(mActivity,
					IPreferencesIds.DB_PROVIDER_SUPPORT, Context.MODE_WORLD_READABLE);
			boolean hasimport = sharedPreferences.getBoolean(IPreferencesIds.HAS_IMPORTED, false);
			if (!hasimport) {
				DBImporter.sendNotify(mActivity);
				sharedPreferences.putBoolean(IPreferencesIds.HAS_IMPORTED, true);
				sharedPreferences.commit();
			}
		}

	}

	// private void showCancelDefaultLauncher()
	// {
	// GoSettingControler controler = GOLauncherApp.getSettingControler();
	// if (null != controler)
	// {
	// ThemeSettingInfo info = controler.getThemeSettingInfo();
	// if (null != info && !info.mTipCancelDefaultDesk)
	// {
	// return;
	// }
	// }
	//
	// final String packageStr = AppUtils.getDefaultLauncherPackage(mActivity);
	//
	// // 没有设置
	// // 设置为Go桌面
	// // 设置为GO锁屏
	// if (null == packageStr
	// || packageStr.equals(LauncherEnv.PACKAGE_NAME)
	// || packageStr.equals(LauncherEnv.GO_LOCK_PACKAGE_NAME))
	// {
	// return;
	// }
	//
	// DeskBuilder builder = new DeskBuilder(mActivity);
	// builder.setTitle(mActivity.getString(R.string.cancel_default_tip_title));
	// final View view =
	// ((LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cancel_default_tip,
	// null);
	// CheckBox box = null;
	// if (null != view)
	// {
	// builder.setView(view);
	// box = (CheckBox)view.findViewById(R.id.cancel_default_no_tip);
	// }
	// else
	// {
	// builder.setMessage(mActivity.getString(R.string.cancel_default_tip_message));
	// }
	// final CheckBox checkBox = box;
	// builder.setPositiveButton(mActivity.getString(R.string.ok), new
	// DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	// AppUtils.showAppDetails(mActivity, packageStr);
	// Toast.makeText(mActivity,
	// mActivity.getString(R.string.cancel_default_toast_message),
	// Toast.LENGTH_LONG).show();
	//
	// if (null != checkBox && checkBox.isChecked())
	// {
	// noTipCancelDefaultDesk();
	// }
	// }
	// });
	// builder.setNegativeButton(mActivity.getString(R.string.cancel), new
	// DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	// if (null != checkBox && checkBox.isChecked())
	// {
	// noTipCancelDefaultDesk();
	// }
	// }
	// });
	// AlertDialog dialog = builder.create();
	// dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
	// {
	// @Override
	// public void onDismiss(DialogInterface dialog)
	// {
	// if (null != checkBox && checkBox.isChecked())
	// {
	// noTipCancelDefaultDesk();
	// }
	// }
	// });
	// dialog.show();
	//
	// try {
	// Vibrator vibrator = (Vibrator)
	// mActivity.getSystemService(Context.VIBRATOR_SERVICE);
	// long pattern[] = { 0, 150 };
	// vibrator.vibrate(pattern, -1);
	// } catch (Throwable e) {
	// // TODO Auto-generated catch block
	// Log.e("Vibrator error"," in showCancelDefaultLauncher");
	// }
	// }

	private void noTipCancelDefaultDesk() {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (null != controler) {
			ThemeSettingInfo info = controler.getThemeSettingInfo();
			if (null != info && info.mTipCancelDefaultDesk) {
				info.mTipCancelDefaultDesk = false;
				controler.updateThemeSettingInfo(info);
			}
		}
	}

	private void showMigrateAppTip() {
		DeskBuilder builder = new DeskBuilder(mActivity);
		builder.setTitle(mActivity.getString(R.string.migrate_app_tip_title));
		builder.setMessage(mActivity.getString(R.string.migrate_app_tip_message));
		builder.setPositiveButton(mActivity.getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String uriStr = LauncherEnv.Market.BY_KEYWORD
								+ LauncherEnv.Plugin.DESKMIGRATE_PACKAGE_NAME;
						if (!AppUtils.gotoMarket(mActivity, uriStr)) {
							AppUtils.gotoBrowser(mActivity, LauncherEnv.Url.DESKMIGRATE_URL);
						}
					}
				});
		builder.setNegativeButton(mActivity.getString(R.string.cancel), null);
		builder.create().show();
	}

	private void showMigrateTip() {
		DeskBuilder builder = new DeskBuilder(mActivity);
		builder.setTitle(R.string.migrate_tip_title);
		builder.setMessage(R.string.migrate_tip_message);
		builder.setPositiveButton(mActivity.getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestMigrateDesk();
					}
				});
		builder.setNegativeButton(mActivity.getString(R.string.cancel), null);
		builder.create().show();
	}

	private void requestMigrateDesk() {
		Intent intent = new Intent("com.ma.deskmigrate.DeskMigrate");
		Bundle bundle = new Bundle();
		bundle.putInt("code", IRequestCodeIds.REQUEST_MIGRATE_DESK);
		intent.putExtras(bundle);
		try {
			mActivity.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 显示版本更新说明对话框
	 * 
	 * @param preferenceString
	 *            IPreferencesIds.SHOW_TIP_FRAME_TIME+版本号
	 */
	private void showUpdateLogDialog() {
		// String aboutTitle = mActivity.getString(R.string.currentversion)
		// + " v"
		// + mActivity.getString(R.string.curVersion)
		// + "\n"
		// + mActivity.getString(R.string.updatelog);
		//
		// Spanned aboutText =
		// Html.fromHtml(mActivity.getString(R.string.golauncher_changelog,
		// TextView.BufferType.SPANNABLE));
		// SpannableStringBuilder b = new SpannableStringBuilder();
		// b.append(aboutText.toString());
		// final String noticeStr = mActivity.getString(R.string.notice_url);
		// int start = aboutText.toString().indexOf(noticeStr);
		// int end = start + noticeStr.length();
		// ClickableSpan clickableSpan = new ClickableSpan() {
		// @Override
		// public void onClick(View widget)
		// {
		// Uri uri=Uri.parse(noticeStr);
		// Intent intent=new Intent(Intent.ACTION_VIEW,uri);
		// mActivity.startActivity(intent);
		// }
		// };
		// b.setSpan(clickableSpan, start, end,
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// final ScrollView scrollView = new ScrollView(mActivity);
		// final TextView textView = new TextView(mActivity);
		// textView.setText(b);
		// textView.setMovementMethod(LinkMovementMethod.getInstance());
		// textView.setTextSize(16);
		// scrollView.addView(textView);
		// AlertDialog dialog = new
		// AlertDialog.Builder(mActivity).setTitle(aboutTitle).setCancelable(true).setIcon(R.drawable.icon).setPositiveButton(
		// mActivity.getString(R.string.tip_btn_txt),
		// null).setView(scrollView).create();
		// dialog.show();
	}

	private void unRegistReceivers() {
		mActivity.unregisterReceiver(mInstallShortcutReceiver);
		// 反注册壁纸更新事件
		mWallpaperControler.unRegistWallpaperReceiver();
	}

	private void registReceivers() {
		mInstallShortcutReceiver = new InstallShortcutReceiver();
		IntentFilter filter = new IntentFilter(ICustomAction.ACTION_INSTALL_SHORTCUT);
		mActivity.registerReceiver(mInstallShortcutReceiver, filter);
		// 注册壁纸更新事件
		mWallpaperControler.registWallpaperReceiver();
	}

	/**
	 * 处理Activity的onNewIntent方法
	 * 
	 * @param intent
	 *            意图
	 */
	public void onNewIntent(Intent intent) {

		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals(ICustomAction.ACTION_STOP_SNAPSHOT)) {
			//从通知栏取消截屏
			boolean isCancelSnapshot = intent.getBooleanExtra(SnapShotManager.NOTIFICATION_RETURN,
					false);
			if (isCancelSnapshot) {
				SnapShotManager.getInstance(mActivity).cancelCapture();
			}
		}

		// 从音乐播放通知栏回来，add by yangbing 2012-06-20
		if (intent != null
				&& intent.getStringExtra(ICustomAction.DESTINATION_KEY) != null
				&& ICustomAction.DESTINATION_RETURN_TO_GOMUSIC.equals(intent.getStringExtra(ICustomAction.DESTINATION_KEY))) {
			if (mFrameControl.getTopFrame().getId() != IDiyFrameIds.APPFUNC_FRAME) {
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
				// 关闭添加界面 add by jiangchao
				if (GoLauncher.getTopFrame() != null
						&& (GoLauncher.getTopFrame().getId() == IDiyFrameIds.SCREEN_EDIT_BOX_FRAME)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT, 0, null, null);
				}
				// 关闭搜索层
				if (GoLauncher.getTopFrame() != null
						&& (GoLauncher.getTopFrame().getId() == IDiyFrameIds.APPFUNC_SEARCH_FRAME)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.REMOVE_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
				}
				onDismissGGMenu(); // 如果桌面菜单已经打开，则关闭菜单
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.SHOW_APP_DRAWER, -1, ICustomAction.DESTINATION_RETURN_TO_GOMUSIC,
						null);
			}
			mIsMusicBarIntent = true;
			return;
		}
		if (intent != null && !onNewIntentFilter(intent)) {
			// 广播给下层
			GoLauncher.sendBroadcastMessage(this, IFrameworkMsgId.SYSTEM_ON_NEW_INTENT, -1, intent,
					null);
		}

		if (intent != null) {
			// add by jiang 用于屏幕锁定 从通知栏 跳菜单
			String s = intent.getStringExtra(SCREEN_LOCKED);
			if (s != null && s.equals(SCREEN_LOCKED)) {
				Intent it = new Intent();;
				it.setAction(ICustomAction.ACTION_SHOW_MENU);
				it.putExtra(SCREEN_LOCKED, SCREEN_LOCKED);
				mAppInvoker.invokeApp(it);
			}
		}
	}

	/**
	 * 过滤操作
	 * 
	 * @param intent
	 *            intent对象
	 * @return 是否处理
	 */
	private boolean onNewIntentFilter(Intent intent) {
		// 关闭系统所有弹出框
		closeSystemDialogs();
		// 关闭自定义弹出框
		closeCustomDialogs();
		mIsNewIntent = true;

		// 过滤HOME事件
		String action = intent.getAction();
		if (Intent.ACTION_MAIN.equals(action)) {
			// 按home健则取消对话框
			if (mLanguageChoiceDialog != null && mLanguageChoiceDialog.isShowing()) {
				mLanguageChoiceDialog.dismiss();
			}
			
			if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
				homeAction();
				return true;
			} else {
				int frameID = GoLauncher.getTopFrame().getId();
				if (frameID == IDiyFrameIds.SCREEN_EDIT_BOX_FRAME
						|| frameID == IDiyFrameIds.SCREEN_PREVIEW_FRAME) {
					homeAction();
				} else {
					// 从外面按下Home进入桌面时关闭功能表,关闭前先通知功能表点击home事件发生了
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IFrameworkMsgId.SYSTEM_HOME_CLICK, 0, null, null);
					GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
					//从外面应用按下Home进入桌面时,清零跳转控制
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 100, null, null);
					
				}
			}
		} else if (ICustomAction.ACTION_SHOW_THEME_PREVIEW.equals(action)) {
			return true;
		}

		return false;
	}

	private void homeAction() {
		final View v = mActivity.getWindow().peekDecorView();
		if (v != null && v.getWindowToken() != null) {
			InputMethodManager imm = (InputMethodManager) mActivity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
		// Home事件发生了
		handleMessage(this, IMsgType.SYNC, IFrameworkMsgId.SYSTEM_HOME_CLICK, -1, null, null);
		boolean open = mFrameControl.isExits(IDiyFrameIds.DESK_USER_FOLDER_FRAME);
		// 如果UserFolder有打开
		if (open) {
			// 发送消息关闭文件夹
			GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
					IFrameworkMsgId.SYSTEM_HOME_CLICK, -1, null, null);
		}
	}

	/**
	 * 处理恢复事件
	 */
	public void onResume() {
		// LogUnit.i("onResume", "onResume");
		// 从音乐播放通知栏恢复，进入功能表音乐播放器界面 add by yangbing 2012-06-20
		if (mIsMusicBarIntent) {
			// 打开功能表
			if (mFrameControl.getTopFrame().getId() == IDiyFrameIds.APPFUNC_FRAME) {
				// 已经在功能表界面了,先调用AppFuncFrame的onResume()，重启绘画引擎
				AppFuncFrame appFuncFrame = (AppFuncFrame) mFrameControl.getTopFrame();
				appFuncFrame.onResume();
				// 发消息切换回所有程序tab页
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.TABCOMPONENT,
						AppFuncConstants.RETURN_MUSIC_PLAY, null);
				// 回到播放界面
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.APP_FUNC_MAIN_VIEW,
						AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
						new Object[] { AppFuncContentTypes.MUSIC_PLAYER,
								ICustomAction.DESTINATION_RETURN_TO_GOMUSIC, null, null });

			}
			mIsMusicBarIntent = false;
			return;
		}
		// 检查状态栏状态
		mStatusBarHandler.checkForStatusBar();

		// 如果桌面加载完成设置方向 更新壁纸
		if (null != GOLauncherApp.getAppDataEngine()
				&& GOLauncherApp.getAppDataEngine().isLoadData()) {
			setOrientation();
		}
		mWallpaperControler.updateWallpaper(false);
//		mWallpaperControler.updateWallpaperInBackground(false);
		
		
		
		if (mIsNewIntent) {
			// Post to a handler so that this happens after the search dialog
			// tries to open
			// itself again.
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SearchManager searchManagerService = (SearchManager) mActivity
							.getSystemService(Context.SEARCH_SERVICE);
					try {
						if (searchManagerService != null) {
							searchManagerService.stopSearch();
						}
					} catch (Exception e) {
					}
				}
			});

		}

		mIsNewIntent = false;
		if (mFrameControl == null) {
			return;
		}
		final AbstractFrame topFrame = mFrameControl.getTopFrame();
		if (topFrame == null) // 全部都不显示，就显示dock和屏幕
		{
			mFrameControl.setFrameVisiable(IDiyFrameIds.DOCK_FRAME, View.VISIBLE);
			mFrameControl.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME, View.VISIBLE);
		}

		// 从设置->预览->返回键->返回键
		if (SensePreviewFrame.backFromSetting()) {
			mFrameControl.showOrHide(IDiyFrameIds.SCREEN_PREVIEW_FRAME);
		}

		// 从其他界面返回
		if (mIsOut) {
			// 清除从其他activity返回状态
			mIsOut = false;
			if (mFrameControl.isScreen(topFrame.getId())) {
				mFrameControl.setFrameVisiable(IDiyFrameIds.DOCK_FRAME, View.VISIBLE);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null, null);
			}
		}
		
		// 从其他地方返回桌面，需要发广播给多屏多壁纸调整
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_SEND_BROADCASTTO_MULTIPLEWALLPAPER, -1,
				null, null);
		
		// 广播resume事件
		GoLauncher.sendBroadcastMessage(this, IFrameworkMsgId.SYSTEM_ON_RESUME, -1, null, null);
		if (mCoverFrame != null) {
			mCoverFrame.onResume();
		}
	}

	/**
	 * 系统pause事件回调
	 */
	public void onPause() {
		// 进入pause状态时的时间
		WallpaperControler.sStopedTime = System.currentTimeMillis();
		// 保存上次的屏幕方向
		Configuration configuration = mActivity.getResources().getConfiguration();
		mLastLocale = configuration.locale;
		mLastOrientation = configuration.orientation;
		GoLauncher.sendBroadcastMessage(this, IFrameworkMsgId.SYSTEM_ON_PAUSE, -1, null, null);
		if (mCoverFrame != null) {
			mCoverFrame.onPause();
		}
	}

	/**
	 * 系统start事件回调
	 */
	public void onStart() {
		Log.i("DemoService", "DiyScheduer.onStart");
		// LogUnit.i("onStart", "onStart");
		Log.i(LogConstants.HEART_TAG, "onStart");
		GoLauncher.sendBroadcastMessage(this, IFrameworkMsgId.SYSTEM_ON_START, -1, null, null);
		// SharedPreferences sharedPreferences
		// =mActivity.getSharedPreferences(IPreferencesIds.USERTUTORIALCONFIG,Context.MODE_PRIVATE);
		// boolean disAcceptAgreement =
		// sharedPreferences.getBoolean(LauncherEnv.DISACCEPT_AGREEMENT, false);
		// if (disAcceptAgreement)//不接受协议不能进入go桌面
		// {
		// Intent intent = new Intent(mActivity,UserAgreementActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// mActivity.startActivity(intent);
		// }
	}

	/**
	 * 系统stop事件回调
	 */
	public void onStop() {
		// LogUnit.i("onStop", "onStop");
		Log.i(LogConstants.HEART_TAG, "onStop");
		// 进入stop状态时的时间
		WallpaperControler.sStopedTime = System.currentTimeMillis();
		GoLauncher.sendBroadcastMessage(this, IFrameworkMsgId.SYSTEM_ON_STOP, -1, null, null);

		if (null != mGGMenu) {
			if (mGGMenu.isShowing()) {
				mGGMenu.dismiss();
			}
		}
		if (mCoverFrame != null) {
			mCoverFrame.onPause();
		}
	}

	/**
	 * 系统create事件回调
	 */
	public void onCreate(Bundle savedInstance) {
		Log.i("DemoService", "DiyScheduler onCreate");
		// LogUnit.i("onCreate", "onCreate");

		// 初始化
		init();
		checkPersistence();
		// mWallpaperControler.updateWallpaper(false);//第一次壁纸设置提前到此处
		startAppService();
		startNotificationService();
		// startMessageCenterService();
	}

	/**
	 * 启动后台服务
	 * 
	 * @author huyong
	 */
	private void startAppService() {
		Intent intent = new Intent();
		intent.setClass(mActivity.getApplicationContext(), AppService.class);
		Log.i("DemoService", "Before startAppService");
		mActivity.getApplicationContext().startService(intent);
		Log.i("DemoService", "End startAppService");
	}

	// private void startMessageCenterService()
	// {
	// Intent intent = new Intent();
	// intent.setClass(mActivity.getApplicationContext(),
	// MessageCenterService.class);
	// mActivity.getApplicationContext().startService(intent);
	// }

	private void startNotificationService() {
		if (AppUtils.isAppExist(mActivity, ICustomAction.NOTIFICATION_PACKAGE)) {
			new Thread(START_NOTIFICATION_THREAD_NAME) {
				@Override
				public void run() {
					Intent it = new Intent(ICustomAction.ACTION_NOTIFICATIONACTION_START_SERVICE);
					mActivity.startService(it);
					super.run();
				}
			}.start();
		}
	}

	// private void stopNotificationService(){
	// if (AppUtils.isAppExist(mActivity, InnerAction.NOTIFICATION_PACKAGE)){
	// Intent it = new Intent(InnerAction.NOTIFICATIONACTION_START_SERVICE);
	// mActivity.stopService(it);
	// }
	// }
	/**
	 * 停止后台服务
	 * 
	 * @author huyong
	 */
	private void stopAppService() {
		Intent intent = new Intent();
		intent.setClass(mActivity.getApplicationContext(), AppService.class);
		Log.i("DemoService", "Before stopAppService");
		mActivity.getApplicationContext().stopService(intent);
		Log.i("DemoService", "End stopAppService");
	}

	// private void stopMessageService()
	// {
	// Intent intent = new Intent();
	// intent.setClass(mActivity.getApplicationContext(),
	// MessageCenterService.class);
	// mActivity.getApplicationContext().stopService(intent);
	// }
	/**
	 * 系统destroy事件回调
	 */
	public void onDestroy() {
		// LogUnit.i("onDestroy", "onDestroy");
		GoLauncher.sendBroadcastMessage(this, IFrameworkMsgId.SYSTEM_ON_DESTROY, -1, null, null);
		unRegistReceivers();

		// 清理框架代码,后面会做杀进程的事情，所以此处不需要作回收
		// mFrameManager.cleanFrameWork();
		// 前台清理工作
		mFrameControl.cleanup();
	}

	/**
	 * 设置屏幕翻转
	 */
	private void setOrientation() {
		// 检查屏幕翻转设置，并应用
		OrientationControl.setOrientation(mActivity);
	}

	/**
	 * 初始化手势处理器
	 */
	private void initGestureHandler(FrameControl frameControl) {
		mGestureHandler = new GestureHandler(frameControl, mActivity);
	}

	/**
	 * 状态栏管理器
	 */
	private void initStatusBarHandler() {
		mStatusBarHandler = new StatusBarHandler();
	}

	private void closeSystemDialogs() {
		try {
			mActivity.removeDialog(IDialogIds.DIALOG_DIY);
			mActivity.getWindow().closeAllPanels();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void closeCustomDialogs() {
		try {
			DockSettingDialog.close();
			// DockEditShortcut.close();
			DockGestureRespond.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	//	private void gotoWallpaperSelect() {
	//		if (!checkShowWallpapersettingGuide()) {
	//			final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
	//			Bundle bundle = new Bundle();
	//			bundle.putString(ChooseWallpaper.CHOOSERTYPE, ChooseWallpaper.TYPE_WALLPAPERCHOOSER);
	//			pickWallpaper.putExtras(bundle);
	//
	//			Intent chooser = Intent.createChooser(pickWallpaper,
	//					mActivity.getText(R.string.chooser_wallpaper));
	//
	//			WallpaperManager wm = (WallpaperManager) mActivity
	//					.getSystemService(Context.WALLPAPER_SERVICE);
	//			WallpaperInfo wi = wm.getWallpaperInfo();
	//			if (wi != null && wi.getSettingsActivity() != null) {
	//				LabeledIntent li = new LabeledIntent(mActivity.getPackageName(),
	//						R.string.configure_wallpaper, 0);
	//				li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
	//				chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
	//			}
	//
	//			startActivity(chooser, null);
	//		}
	//	}

	private boolean checkShowWallpapersettingGuide() {
		// SharedPreferences sharedPreferences = mActivity
		// .getSharedPreferences(IPreferencesIds.USERTUTORIALCONFIG,
		// Context.MODE_PRIVATE);
		// boolean shouldShowGuide =
		// sharedPreferences.getBoolean(LauncherEnv.SHOULD_SHOW_WAPAPERSETTING_GUIDE,
		// true);
		// if (shouldShowGuide)
		// {
		// 新加了设置按钮，改为每次点击都弹用户指引
		// GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_WALLPAPER_SETTING);
		// handleMessage(this, IMsgType.SYNC,
		// IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		// }
		// SharedPreferences.Editor editor = sharedPreferences.edit();
		// editor.putBoolean(LauncherEnv.SHOULD_SHOW_WAPAPERSETTING_GUIDE,
		// false);
		// editor.commit();

		return true;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case IFrameworkMsgId.REMOVE_FRAME :
					mFrameControl.hideFrame(msg.arg1);
					break;

				case IFrameworkMsgId.SHOW_FRAME :
					mFrameControl.showFrame(msg.arg1);
					break;

				default :
					break;
			}
		};
	};

	void checkPersistence() {
		final ThemeSettingInfo themeSettingInfo = GOLauncherApp.getSettingControler()
				.getThemeSettingInfo();
		if (themeSettingInfo != null) {
			boolean isPemanentMemory = themeSettingInfo.mIsPemanentMemory;
			setPersistent(mActivity, isPemanentMemory);
			if (mWallpaperControler != null) {
				mWallpaperControler
						.setTransparentStatusbarSupport(themeSettingInfo.mTransparentStatusbar);
			}
		}
	}

	private void setPersistent(Activity activity, boolean isPemanentMemory) {
		if (activity == null) {
			return;
		}
		// 为兼容4.0以上采用反射机制
		try {
			Method setPersistent = activity.getClass().getMethod("setPersistent", boolean.class);
			setPersistent.invoke(activity, isPemanentMemory);
		} catch (Throwable e) {
			Log.i("DiyScheduler", "setPersistent" + e.getMessage());
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		boolean ret = false;
		switch (msgId) {
			case IFrameworkMsgId.SHOW_FRAME : {
				if (ShellPluginFactory.isUseShellPlugin(mActivity)) {
					if (param == IDiyFrameIds.SCREEN_FRAME || param == IDiyFrameIds.DOCK_FRAME
							|| param == IDiyFrameIds.APPFUNC_FRAME) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
								IDiyMsgIds.SHOW_FRAME, param, object, objects);
						ret = true;
					}
				}

				if (!ret) {
					// 显示层
					ret = mFrameControl.showFrame(param);
				}
			}
				break;

			case IFrameworkMsgId.REMOVE_FRAME : {
				if (ShellPluginFactory.isUseShellPlugin(mActivity)) {
					if (param == IDiyFrameIds.SCREEN_FRAME || param == IDiyFrameIds.DOCK_FRAME
							|| param == IDiyFrameIds.APPFUNC_FRAME) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
								IDiyMsgIds.REMOVE_FRAME, param, object, objects);
						ret = true;
					}
				}
				
				if (!ret) {
					// 移除显示层
					if (type == IMsgType.SYNC) {
						ret = mFrameControl.hideFrame(param);
					} else {
						// 异步处理消息
						mHandler.sendMessage(mHandler.obtainMessage(msgId, param, -1));
					}
				}
			}
				break;

			case IFrameworkMsgId.HIDE_FRAME : {
				if (ShellPluginFactory.isUseShellPlugin(mActivity)) {
					if (param == IDiyFrameIds.SCREEN_FRAME || param == IDiyFrameIds.DOCK_FRAME
							|| param == IDiyFrameIds.APPFUNC_FRAME) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
								IDiyMsgIds.HIDE_FRAME, param, object, objects);
						ret = true;
					}
				}
				
				if (!ret) {
					mFrameControl.hide(param);
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.SHOW_APP_DRAWER : {
				if (null == mGGMenu || !mGGMenu.isShowing()) {
					// 进入功能表
//					mFrameControl.showFrame(IDiyFrameIds.APPFUNC_FRAME);
					handleMessage(this, -1, IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
					// 回到音乐播放界面 add by yangbing 2012-06-20
					if (object != null) {
						String str = (String) object;
						if (ICustomAction.DESTINATION_RETURN_TO_GOMUSIC.equals(str)) {
							// 回到播放界面
							DeliverMsgManager.getInstance().onChange(
									AppFuncConstants.APP_FUNC_MAIN_VIEW,
									AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
									new Object[] { AppFuncContentTypes.MUSIC_PLAYER,
											ICustomAction.DESTINATION_RETURN_TO_GOMUSIC, null, null });
						}
					}
				}
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_LONG_CLICK : {
				// 弹出桌面菜单前，回收一次内存
				OutOfMemoryHandler.handle();
				// 弹出菜单
				// mFrameControl.removeFrame(IDiyFrameIds.DOCK_FRAME);
				// mFrameControl.showFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
				// mActivity.showDialog(IDialogIds.DIALOG_DIY);
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_SLIDE_DOWN : {
				final GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_DOWN_ID);
				mGestureHandler.handleGesture(info, mIsOut);
				//用户行为统计--上滑手势
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_GESTURE_DOWN,
						StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;

			case IDiyMsgIds.SCREEN_SLIDE_UP : {
			final GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_UP_ID);
			mGestureHandler.handleGesture(info, mIsOut);
			// 用户行为统计--下滑手势
			StatisticsData.countUserActionData(
					StatisticsData.DESK_ACTION_ID_GESTURE_UP,
					StatisticsData.USER_ACTION_DEFAULT,
					IPreferencesIds.DESK_ACTION_DATA);
			}
				break;

			case IFrameworkMsgId.SYSTEM_HOME_CLICK : {
				boolean isMenu = false;
				// 如果桌面已加载完则系统home键响应
				if (GoLauncher.getSystemHomeKeyAct()) {
					final GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_HOME_ID);
					boolean isGoLauncherAPP = false;
					if (info != null && info.mAction != null
							&& info.mAction.contains(LauncherEnv.PACKAGE_NAME)) {
						Intent intent = ConvertUtils.stringToIntent(info.mAction);
						if (intent != null) {
							final ComponentName component = intent.getComponent();
							if (component != null && component.getPackageName() != null
									&& component.getPackageName().equals(LauncherEnv.PACKAGE_NAME)) {
								isGoLauncherAPP = true;
								GoLauncher.sendBroadcastMessage(this,
										IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null, null);
							} // end if component
						} // end if intent
					} // end if info
					
					// home键跳转过滤,判断是跳当前屏幕还是主屏
				AbstractFrame top = GoLauncher.getTopFrame();
					if (top != null) {
						int id = top.getId();
						if (id == IDiyFrameIds.DOCK_FRAME
								|| id == IDiyFrameIds.SCREEN_FRAME) {
							if (mGGMenu != null && mGGMenu.isShowing()) {
								GoLauncher
										.sendMessage(
												this,
												IDiyFrameIds.SCREEN_FRAME,
												IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
												0, null, null);
							}
						} else {
							// 如果是在其他层
							GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
									0, null, null);
						}
					}
					
					if (!isGoLauncherAPP) {
					
						mGestureHandler.handleGesture(info, mIsOut, true);
					}
					isMenu = info != null
							&& info.mGestureAction == GlobalSetConfig.GESTURE_GOSHORTCUT
							&& info.mGoShortCut == GlobalSetConfig.GESTURE_SHOW_MENU;
				}
				if (!isMenu) {
					onDismissGGMenu();
				}
				GoLauncher.sendMessage(this, IDiyFrameIds.SHARE_FRAME,
						IFrameworkMsgId.SYSTEM_HOME_CLICK, 0, null, null);

				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IFrameworkMsgId.SYSTEM_HOME_CLICK, 0, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
						IFrameworkMsgId.SYSTEM_HOME_CLICK, 0, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_SEARCH_FRAME,
						IFrameworkMsgId.SYSTEM_HOME_CLICK, 0, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.IMAGE_BROWSER_FRAME,
						IFrameworkMsgId.SYSTEM_HOME_CLICK, 0, null, null);
			}
				break;
			case IDiyMsgIds.SCREEN_DOUBLE_CLICK : {
				final GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_DOUBLLE_CLICK_ID);
				mGestureHandler.handleGesture(info, mIsOut);
				//用户行为统计--下滑手势
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_DOUBLE_CLICK,
						StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;
			case IDiyMsgIds.SCREEN_DOUBLE_CLICK_VALID :
				final GestureSettingInfo infoG = getGestureSetting(GestureSettingInfo.GESTURE_DOUBLLE_CLICK_ID);
				ret = infoG.mGestureAction != GlobalSetConfig.GESTURE_DISABLE;
				break;
			case IDiyMsgIds.SCREEN_PINCH_IN : {
				// 发消息给屏幕层要求进入预览界面
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_SHOW_PREVIEW, 0, null, null);
				//用户行为统计--下滑手势
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_PINCH_ACTION_ZOOM,
						StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;

			case IDiyMsgIds.SHOW_HIDE_STATUSBAR : {
				if (param == -1) {
					setFullScreen(!mStatusBarHandler.isFullScreen());
				} else if (param == -2) {
					if (object != null) {
						boolean fullScreen = (Boolean) object;
						setFullScreen(fullScreen, false);
					}
				}
				// else if(param == -3){ //与-1区别：不保存数据库
				// setFullScreen(!mStatusBarHandler.isFullScreen(), false);
				// }

				// 通知DOCK层，因为重新排版会引起DOCK层排序错乱
				GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.SHOW_HIDE_STATUSBAR, -1, null, null);
				ret = true;
			}
				break;

			case IDiyMsgIds.SHOW_HIDE_MENU : {
				if (mGGMenu != null && mGGMenu.isShowing()) {
					mGGMenu.dismiss();
				} else {
					if (ShellPluginFactory.isUseShellPlugin(mActivity)
							|| mFrameControl.isScreenOnTop()) {
						handleMessage(who, type, IDiyMsgIds.SHOW_MENU, param, object, objects);
					}
				}
			}
				break;

			case IDiyMsgIds.SHOW_MENU : {
				int selectTab = 0;

				if (param == 1) {
					selectTab = 2;
				}

				else if (param == 2) {
					selectTab = 1;
				}
				GGMenu menu = onPrepareGGMenu(selectTab);
				if (menu != null) {
					if (menu.isShowing()) {
						menu.getContainer().gotoTab(selectTab);
						menu.setSelectTab(selectTab);
					} else {
						if (object != null && object instanceof Boolean) {
							menu.show((Boolean) object);
						} else {
							menu.show();
						}
					}
				}
			}
				break;
			case IDiyMsgIds.SCREEN_MENU_SHOW :
				if (mGGMenu != null && mGGMenu.isShowing()) {
					ret = true;
				} else {
					ret = false;
				}
				break;
			case IDiyMsgIds.SHOW_STATUSBAR : {
				setFullScreen(false);
				ret = true;
			}
				break;

			case IDiyMsgIds.HIDE_STATUSBAR : {
				setFullScreen(true);
				ret = true;
			}
				break;

			case IDiyMsgIds.SHOW_SEARCH_DIALOG : {
				if (object != null && object instanceof Bundle) {
					final Bundle bundle = (Bundle) object;
					final String initialQuery = bundle.getString(Search.FIELD_INITIAL_QUERY);
					final boolean selectInitialQuery = bundle
							.getBoolean(Search.FIELD_SELECT_INITIAL_QUERY);
					final Bundle searchData = bundle.getBundle(Search.FIELD_SEARCH_DATA);
					final boolean globalSearch = bundle.getBoolean(Search.FIELD_GLOBAL_SEARCH);
					showSearchDialog(initialQuery, selectInitialQuery, searchData, globalSearch);
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.START_ACTIVITY : {
				if (object != null && object instanceof Intent) {
					synchronized (this) {
						Rect rect = null;
						boolean[] result = null;
						if (objects != null && objects.size() > 0) {
							rect = (Rect) objects.get(0);
							if (objects.size() > 1) {
								result = (boolean[]) objects.get(1);
							}
						}
						startActivity((Intent) object, rect, result);
					}
				}
			}
				break;

			case IDiyMsgIds.START_ACTIVITY_FOR_RESULT : {
				if (object != null && object instanceof Intent) {
					ret = mAppInvoker.invokeAppForResult(param, (Intent) object);
				}
			}
				break;

			case IDiyMsgIds.DOCK_ENTER_SHORTCUT_SELECT : {
				mShortcutHolerId = IShortcutHolderIds.SHORTCUT_DOCK_CLICK;
				mShortcutSource = DOCK_ACTION;
				pickShortcut(IRequestCodeIds.REQUEST_PICK_SHORTCUT, R.string.select_app_icon, param);
			}
				break;

			case IDiyMsgIds.DOCK_ENTER_SHORTCUT_SELECT_FOR_GESTURE : {
				mShortcutHolerId = IShortcutHolderIds.SHORTCUT_DOCK_GESTURE;
				mShortcutSource = DOCK_GESTURE;
				pickShortcut(IRequestCodeIds.REQUEST_DOCK_PICK_SHORTCUT_FOR_GESTURE,
						R.string.select_app_icon, param);
			}
				break;

			case IDiyMsgIds.LAST_MOTION_POSITION : {
				if (mFrameControl != null && object != null && object instanceof Bundle) {
					final float x = mFrameControl.getLastMotionX();
					final float y = mFrameControl.getLastMotionY();
					((Bundle) object).putFloat(LAST_MOTION_X, x);
					((Bundle) object).putFloat(LAST_MOTION_Y, y);
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.SEND_WALLPAPER_COMMAND : {
				mWallpaperControler.sendWallpaperCommand(mFrameControl.getRootView(),
						(Bundle) object);
				ret = true;
			}
				break;

			case IDiyMsgIds.UPDATE_WALLPAPER_OFFSET : // 壁纸背景偏移
			{
				mWallpaperControler.updateWallpaperOffset(mFrameControl.getRootView(),
						(Bundle) object);
				ret = true;
				break;
			}

			case IDiyMsgIds.SCREEN_FINISH_LOADING : {
				setOrientation(); // 初始化成之后设置为正确的横竖屏模式
				if (null != GOLauncherApp.getAppDataEngine()) {
					GOLauncherApp.getAppDataEngine().startLoadCompletedData();
				}
				// add by zhoujun
				// 当桌面数据加载完毕时，在application中初始化其他一些操作，比如绑定付费的管理的service
				GOLauncherApp.getApplication().dataLoadFinish();
				// add by zhoujun 2012-09-10 end
				
				// add by licanhui
				// 当桌面数据加载完毕时，请求服务器下载广告图标信息
				AdvertControl.getAdvertControlInstance(mActivity).requestAdvertData();
				// add by licanhui 2012-12-5 end
			}
				break;

			case IDiyMsgIds.REFRESH_GGMENU_THEME :
				createGGMenuBean();
				break;

			case IDiyMsgIds.EVENT_THEME_CHANGED : {
				//onGGMenuThemeChanged();
				createGGMenuBean();
				if (object != null && object instanceof String) {
					String curPackageName = (String) object;
					ThemeInfoBean infoBean = ThemeManager.getInstance(mActivity)
							.getCurThemeInfoBean();
					if (infoBean != null) {
						if (infoBean.isMaskView()) {
							handleMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.COVER_FRAME_ADD_VIEW, CoverFrame.COVER_VIEW_THEME,
									curPackageName, null);
						} else {
							handleMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.COVER_FRAME_REMOVE_VIEW,
									CoverFrame.COVER_VIEW_THEME, null, null);
							handleMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.COVER_FRAME_REMOVE_VIEW,
									CoverFrame.COVER_VIEW_HOLIDAY, false, null);
						}
					}

				}
				break;
			}

			case IDiyMsgIds.RESTART_GOLAUNCHER : {
				exit(true);
				break;
			}
			case IDiyMsgIds.EXIT_GOLAUNCHER : {
				exit(false);
				break;
			}

			case IDiyMsgIds.EVENT_INSTALL_APP :
			case IDiyMsgIds.EVENT_INSTALL_PACKAGE : {
				if (null != object && object instanceof String) {
					String objectString = (String) object;
					if (XmlRecommendedApp.getRecommededAppInfoByPackage(objectString) != null) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_REPLACE_RECOMMEND_ICON, -1, object, null);
					}
					if (LauncherEnv.Plugin.DESKMIGRATE_PACKAGE_NAME.equals(objectString)) {
						showMigrateTip();
					} else if (LauncherEnv.Plugin.LOCKER_PACKAGE.equals(objectString)
							|| LauncherEnv.Plugin.LOCKER_PRO_PACKAGE.equals(objectString)) {
						// 重启菜单
						if (null != mGGMenu && mGGMenu.isShowing()) {
							mGGMenu.dismiss();
						}
						mGGMenu = null;

						// 更新推荐文件夹里面的图标为应用图标
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.REPLACE_RECOMMAND_ICON_IN_FOLDER, -1, object, null);
						// 替换桌面的推荐下载图标
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_REPLACE_RECOMMEND_ICON, -1, object, null);
					} else if (LauncherEnv.Plugin.NOTIFICATION_PACKAGE_NAME.equals(objectString)) {
						startNotificationService();
					}

					else if (LauncherEnv.Plugin.RECOMMEND_PACKAGE.equals(objectString)
							|| LauncherEnv.Plugin.RECOMMEND_EVERNOTE_PACKAGE.equals(objectString)
							|| LauncherEnv.Plugin.RECOMMEND_NEXTLAUNCHER_PACKAGE.equals(objectString)
							|| PackageName.MEDIA_PLUGIN.equals(objectString)
							|| LauncherEnv.Plugin.RECOMMEND_BAIDUBROWSER_PACKAGE.equals(objectString)) {
						// 更新推荐文件夹里面的图标为应用图标
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.REPLACE_RECOMMAND_ICON_IN_FOLDER, -1, object, null);
						// 替换桌面推荐图标。可在判断条件后面增加需要替换的包名判断
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_REPLACE_RECOMMEND_ICON, -1, objectString, null);
					}
					
					else if (AdvertUtils.checkInAdvertList(mActivity, objectString)) {
						//更新推荐文件夹里面的图标为应用图标
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.REPLACE_RECOMMAND_ICON_IN_FOLDER, -1, objectString, null);
						// 替换桌面推荐图标。可在判断条件后面增加需要替换的包名判断
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_REPLACE_RECOMMEND_ICON, -1, objectString, null);
					}
					// else
					// if(LauncherEnv.Plugin.RECOMMEND_EVERNOTE_PACKAGE.equals(objectString)){
					// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					// IDiyMsgIds.SCREEN_REPLACE_EVERNOTE_ICON,
					// -1, null, null);
					// }
					else {
						// 推荐图标处理
						handleRecommandIcon(objectString);
					}
				}
			}
				break;
			case IDiyMsgIds.EVENT_UNINSTALL_APP :
			case IDiyMsgIds.EVENT_UNINSTALL_PACKAGE : {
				if (null != object && object instanceof String) {
					String objectString = (String) object;
					if (LauncherEnv.Plugin.LOCKER_PACKAGE.equals(objectString)) {
						// 重启菜单
						if (null != mGGMenu && mGGMenu.isShowing()) {
							mGGMenu.dismiss();
						}
						mGGMenu = null;
					} else if (objectString.contains(LauncherEnv.Plugin.LANGUAGE_PACKAGE)) {
						String language = mActivity.getResources().getConfiguration().locale
								.getLanguage();
						language = LauncherEnv.Plugin.LANGUAGE_PACKAGE + "." + language;
						if (object.equals(language)) {
							exit(true);
						}
					} else if (ICustomAction.NOTIFICATION_PACKAGE.equals(objectString)) {
						// stopNotificationService();
					}
				}
			}
				break;

			case IDiyMsgIds.SCREEN_RESET_ORIENTATION : {
				setOrientation();
			}
				break;

			case IDiyMsgIds.SCREEN_SET_ORIENTATION : {
				OrientationControl.setOrientation(mActivity, param);
			}
				break;

			case IDiyMsgIds.SCREEN_PREFERENCES : {
				startActivity(new Intent(mActivity, DeskSettingMainActivity.class), null);
			}
				break;

			case IDiyMsgIds.GOWIDGET_UNINSTALL_GOWIDGET_SWITCH : {
				// 卸载开关gowidget
				Uri packageURI = Uri.parse("package:" + ICustomAction.PKG_GOWIDGET_SWITCH);
				Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
				mActivity.startActivityForResult(uninstallIntent,
						IRequestCodeIds.REQUEST_GOWIDGET_UNINSTALL_SWITCH);
			}
				break;

			case IDiyMsgIds.ENABLE_KEYGUARD : {
				Intent loackIntent = new Intent(ICustomAction.ACTION_ENABLE_SCREEN_GUARD);
				startActivity(loackIntent, null);
			}
				break;

			case IDiyMsgIds.SHOW_DOWNLOAD_GOSWITCHWIDGET_DIALOG : {
				int resId = R.string.no_install_goswitchwidget_tips;
				if (param == 1) {
					resId = R.string.update_goswitchwidget_tips;
				}
				// showInstallSwitchWidgetDialog(resId);
				// add by Ryan 2012.7.11
				String title = mActivity.getResources().getString(R.string.onoff);
				String content = mActivity.getResources().getString(resId);
				String[] linkArray = new String[] { ICustomAction.PKG_GOWIDGET_SWITCH,
						LauncherEnv.Url.GOSWITCHWIDGET_FTP_URL };
				CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
						LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK, title,
						System.currentTimeMillis(), Machine.isCnUser(mActivity),
						CheckApplication.FROM_SCREENEDIT);
			}
				break;

			case IDiyMsgIds.SCREENEDIT_ADD_TAB_ADD_SHORTCUT : {
				mAddingComponent = true;
				mShortcutSource = SCREEN_ACTION;
				// 添加应用程序图标
				pickShortcut(IRequestCodeIds.REQUEST_PICK_SHORTCUT, R.string.select_app_icon, param);
				mShortcutHolerId = IShortcutHolderIds.SHORTCUT_SCREEN;
			}
				break;

			case IDiyMsgIds.PICK_WIDGET :
				mAddingComponent = true;
				pickWidget();
				break;

			case IDiyMsgIds.SCREENEDIT_ADD_TAB_ADD_GO_SHORTCUT : {
				Intent laIntent = new Intent(mActivity, ScreenEditAddGoLauncher.class);
				startActivityForResult(laIntent,
						IRequestCodeIds.REQUEST_SCREEN_ADD_GOLAUNCHER_SHORTCUT);
			}
				break;
			case IDiyMsgIds.SCREENEDIT_WALLPAPER_TAB_CHECK_GUIDEGL : {
				checkShowWallpapersettingGuide();
			}
				break;
			case IDiyMsgIds.OPEN_SCREEN_MENU : {
				mNeedOpenGGMenu = true;
				pressMenuKey(false);
			}
				break;

			case IDiyMsgIds.SCREEN_OPEN_NOTIFICATION_SETTING : {
				handleNotificationClick();
			}
				break;
			case IDiyMsgIds.IMPORT_OTHER_DB : {
				if (object != null) {
					importDBFromOtherLauncher((String) object);
				}
			}
				break;

			case IDiyMsgIds.COVER_FRAME_ADD_VIEW :
				if (mActivity instanceof GoLauncher) {
					ViewGroup viewGroup = ((GoLauncher) mActivity).mMainLayout;
					if (mCoverFrame == null) {
						mCoverFrame = mFrameControl.getCoverFrame();
						mCoverFrame.setContainer(viewGroup);
						mCoverFrame.registerCoverCallback(this);
					}
				}
				// 不需要break;
			case IDiyMsgIds.COVER_FRAME_REMOVE_VIEW :
			case IDiyMsgIds.COVER_FRAME_HIDE_ALL :
			case IDiyMsgIds.COVER_FRAME_SHOW_ALL :
			case IDiyMsgIds.COVER_FRAME_REMOVE_ALL : {
				coverFrameHandle(msgId, param, object);
			}
				break;

			case IDiyMsgIds.EVENT_LOAD_FINISH :// 收到广播,初始化加载完成
				createGGMenuBean();
				break;

			case IFrameworkMsgId.SYSTEM_FULL_SCREEN_CHANGE : {
				// 一般3.X的pad状态栏在下面，上面是没有的，所以不需要重新排版
				if (!(Machine.IS_HONEYCOMB && !Machine.IS_ICS)) {
					if (mCoverFrame != null) {
						final boolean isFullScreen = param == 1;
						final int yOffset = isFullScreen
								? StatusBarHandler.getStatusbarHeight()
								: 0;
						mCoverFrame.onStatusBarChange(yOffset);
					}
				}
				break;
			}
			case IDiyMsgIds.SNAPSHOT_DRAW_THROUND_LAUNCHER:
			{
				if (mActivity instanceof GoLauncher && object instanceof Canvas) {
					ViewGroup viewGroup = ((GoLauncher) mActivity).mMainLayout;
					if (GoLauncher.getTopFrame().getId() == IDiyFrameIds.APPFUNC_FRAME) {
						GoLauncher.getTopFrame().getContentView().draw((Canvas) object);
						GoLauncher.getTopFrame().getContentView().destroyDrawingCache();
					} else {
						viewGroup.draw((Canvas) object);
						GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME, IDiyMsgIds.SNAPSHOT_DESTROY_APPFUNCFRAME_CACHE, -1, null, null);
					}
					if (mCoverFrame != null) {
						mCoverFrame.drawSurfaceView((Canvas) object);
					}
				}
			}
				break;
				
			default :
				ret = false;
		}

		return ret;
	} // end handleMessage

	/**
	 * 设置全屏，但不更改数据库的设置信息
	 * 
	 * @param isFullScreen
	 * @param updateData
	 */
	private void setFullScreen(final boolean isFullScreen, boolean updateData) {
		mStatusBarHandler.setFullScreen(isFullScreen, updateData);
	}

	private void setFullScreen(final boolean isFullScreen) {
		setFullScreen(isFullScreen, true);
	}

	/**
	 * 进入快捷方式选择界面
	 * 
	 * @param requestCode
	 *            请求码
	 * @param title
	 * @param source
	 *            发起者
	 */
	private void pickShortcut(int requestCode, int title, int type) {
		Bundle bundle = null;
		if (DOCK_GESTURE != mShortcutSource) {
			bundle = new Bundle();
			if (mShortcutSource == DOCK_ACTION) {
				//判断是否DOCK自适应模式。，自适应模式没有GO快捷方式
				if (type != 1) {
					ArrayList<String> shortcutNames = new ArrayList<String>();
					shortcutNames.add(mActivity.getString(R.string.launcher_action));
					bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
					ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
					shortcutIcons.add(ShortcutIconResource.fromContext(mActivity,
							R.drawable.screen_edit_go_shortcut));
					bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
				}
				
			} else {
				ArrayList<String> shortcutNames = new ArrayList<String>();
				shortcutNames.add(mActivity.getString(R.string.dialog_name_system_folder));
				bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

				ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
				shortcutIcons.add(ShortcutIconResource.fromContext(mActivity,
						R.drawable.ic_launcher_add_folder));
				bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
			}
		}

		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
		pickIntent.putExtra(Intent.EXTRA_TITLE, mActivity.getText(title));
		if (null != bundle) {
			pickIntent.putExtras(bundle);
		}

		startActivityForResult(pickIntent, requestCode);
	}

	private void processShortcut(Intent intent, int requestCodeShortcut) {
		String applicationName = mActivity.getResources().getString(R.string.launcher_action);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		String systemFolderName = mActivity.getResources().getString(
				R.string.dialog_name_system_folder);

		if (applicationName != null && applicationName.equals(shortcutName)) {
			if (mShortcutSource == SCREEN_ACTION) {
				Intent laIntent = new Intent(mActivity, LauncherActionList.class);
				startActivityForResult(laIntent,
						IRequestCodeIds.REQUEST_SCREEN_ADD_GOLAUNCHER_SHORTCUT);
			} else if (mShortcutSource == DOCK_ACTION) {
				DockAddLauncherActionDialog aDialog = DockAddLauncherActionDialog
						.getDialog(mActivity);
				aDialog.show();
			}
		} else if (systemFolderName != null && systemFolderName.equals(shortcutName)) {
			pickFolder();
		} else {
			startActivityForResult(intent, requestCodeShortcut);
		}
	}

	// 通过GO快捷方式启动的操作（可能是程序或者GGMenu等，也要返回一个开启成功的标识）
	private boolean startActivity(Intent intent, Rect rect, boolean[] result) {
		if (intent != null) {
			// Home键跳转修改
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 0, null, null);
			
			return mAppInvoker.invokeApp(intent, rect, result);
		}
		return false;
	}

	private boolean startActivity(Intent intent, Rect rect) {
		if (intent != null) {
			mIsOut = mAppInvoker.invokeApp(intent, rect, null);
			return mIsOut;
		}
		return false;
	}

	private void startActivityForResult(Intent intent, int requestCode) {
		if (intent == null) {
			return;
		}

		try {
			mActivity.startActivityForResult(intent, requestCode);
			mIsOut = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取设置值
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	private GestureSettingInfo getGestureSetting(int key) {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (null != controler) {
			return controler.getGestureSettingInfo(key);
		}
		return null;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (!mFrameControl.onKeyLongPress(keyCode, event)) {
			// 如果所有frame不处理该key事件，则过滤之
			return false;
		}
		return true;
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		if (!mFrameControl.onKeyMultiple(keyCode, repeatCount, event)) {
			// 如果所有frame不处理该key事件，则过滤之
			return false;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Log.i(LogConstants.HEART_TAG, "onKeyDown keyCode = " + keyCode);
		boolean handled = mFrameControl.onKeyDown(keyCode, event);
		if (!handled) {
			boolean searchFilter = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SEARCH_EVENT_FILTER, keyCode, event, null);

			if (!searchFilter) {
				// 如果所有frame不处理该key事件，则过滤之
				if (keyCode == KeyEvent.KEYCODE_MENU) {
					/**
					 * NOTICE: 对KEYCODE_MENU事件处理不能返回true， 否则系统认为已经被处理就不会弹出软键盘
					 */
					if (event.isLongPress()) {
						mNeedOpenGGMenu = false;

						// 统计
						FunctionalStatistic st = AppCore.getInstance().getFunctionalStatistic();
						if (null != st) {
							st.statistic(FunctionalStatistic.Rule.RULE_ADDVALUE, "long_menu_key", 1);
						}
						AbstractFrame topFrame = mFrameControl.getTopFrame();
						// 在功能表长按菜单事件显示搜索
						if (topFrame != null && topFrame.getId() == IDiyFrameIds.APPFUNC_FRAME) {
							GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
									IDiyMsgIds.EVENT_FUNCFRAM_LONGPRESS, 0, null, null);
							handled = true;
						}
					} else if (!mIsKeyDown) {
						// 需要弹出菜单
						mNeedOpenGGMenu = true;
					}
				} else if (keyCode == KeyEvent.KEYCODE_BACK) {
					// 屏蔽返回键
					handled = true;
				} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
					// 在功能表不响应长按搜索键事件
					if (event.isLongPress()) {
						AbstractFrame topFrame = mFrameControl.getTopFrame();
						if (topFrame != null && topFrame.getId() == IDiyFrameIds.APPFUNC_FRAME) {
							handled = true;
						}
					}
				}
			}
		}
		mIsKeyDown = true;
		return handled;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Log.i(LogConstants.HEART_TAG, "onKeyUp keyCode = " + keyCode);
		mIsKeyDown = false;
		// TODO 后面考虑做成独立一个按键过滤模块
		if (!mFrameControl.onKeyUp(keyCode, event)) {
			// if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
			// || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
			// {
			// if (ENABLE_SCREEN_CAPTURE)
			// {
			// captureScreen();
			// return true;
			// }
			// }

			// 如果所有frame不处理该key事件，则过滤之
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				closeSystemDialogs();
				// 桌面添加在推出时需要动画，不必麻烦mFrameControl直接关闭 -by Yugi
				AbstractFrame topFrame = mFrameControl.getTopFrame();
				if (topFrame != null
						&& (topFrame.getId() == IDiyFrameIds.SCREEN_EDIT_BOX_FRAME
								|| topFrame.getId() == IDiyFrameIds.SHARE_FRAME || topFrame.getId() == IDiyFrameIds.REPLACE_DRAG_FRAME)) {
					return false;
				}
				mFrameControl.backToScreen();
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				return pressMenuKey(0 != (event.getFlags() & KeyEvent.FLAG_VIRTUAL_HARD_KEY));
			} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
				// 统计
				FunctionalStatistic st = AppCore.getInstance().getFunctionalStatistic();
				if (null != st) {
					st.statistic(FunctionalStatistic.Rule.RULE_ADDVALUE, "search_key", 1);
				}
			}

			return false;
		}
		return true;
	}

	@Override
	public int getId() {
		return mId;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (!mFrameControl.dispatchKeyEvent(event)) {
			// 过滤处理
		}
		return false;
	}

	@Override
	public boolean dispatchTrackballEvent(MotionEvent event) {
		if (!mFrameControl.dispatchTrackballEvent(event)) {
			// 过滤处理
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mCoverFrame != null && mCoverFrame.dispatchTouchEvent(event)) {
			return true;
		} else if (!mFrameControl.dispatchTouchEvent(event)) {
			// 过滤处理
		}
		return false;
	}

	/**
	 * 处理Activity的返回事件
	 * 
	 * @param requestCode
	 *            返回码
	 * @param resultCode
	 *            结果码
	 * @param data
	 *            数据
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mAddingComponent = false;
		switch (requestCode) {
			case IRequestCodeIds.REQUEST_THEME_SCAN : {
				// 主题管理来的
				handleThemeScanResult(resultCode, data);
			}
				break;

			case IRequestCodeIds.REQUEST_PICK_APPWIDGET : {
				processPickAppwidget(resultCode, data);
			}
				break;

			case IRequestCodeIds.REQUEST_CREATE_APPWIDGET : {
				completeAddAppWidget(data);
			}
				break;

			case IRequestCodeIds.REQUEST_PICK_SHORTCUT : {
				if (resultCode == Activity.RESULT_OK) {
					processShortcut(data,
					/* IRequestCodeIds.REQUEST_PICK_APPLICATION, */
					IRequestCodeIds.REQUEST_CREATE_SHORTCUT);
				}
			}
				break;

			// case IRequestCodeIds.REQUEST_PICK_APPLICATION:
			// {
			// if (resultCode == Activity.RESULT_OK)
			// {
			// completeAddApplication(data);
			// }
			// }
			// break;

			case IRequestCodeIds.REQUEST_CREATE_SHORTCUT : {
				completeAddShortcut(data);
			}
				break;

			case IRequestCodeIds.REQUEST_PICK_LIVE_FOLDER : {
				if (resultCode == Activity.RESULT_OK) {
					addLiveFolder(data);
				}
			}
				break;

			case IRequestCodeIds.REQUEST_CREATE_LIVE_FOLDER : {
				if (resultCode == Activity.RESULT_OK) {
					completeAddLiveFolder(data);
				}
			}
				break;

			case IRequestCodeIds.REQUEST_CREATE_APPDRAWER_FOLDER : {
				if (resultCode == Activity.RESULT_OK) {
					completeAddAppDrawerFolder(data);
				}
			}
				break;

			case IRequestCodeIds.REQUEST_DOCK_PICK_SHORTCUT_FOR_GESTURE : {
				if (resultCode == Activity.RESULT_OK) {
					processShortcut(data,
					/* IRequestCodeIds.REQUEST_PICK_APPLICATION_IN_DOCK_GESTURE, */
					IRequestCodeIds.REQUEST_CREAT_SHORTCUT_IN_DOCK_GESTURE);
				}
			}
				break;

			case IRequestCodeIds.REQUEST_PICK_APPLICATION_IN_DOCK_GESTURE : {
				if (resultCode == Activity.RESULT_OK) {
					Bundle bundle = data.getExtras();
					Intent intent = bundle.getParcelable(ScreenModifyFolderActivity.INTENT_STRING);
					// mApplicationHolerId =
					// IShortcutHolderIds.SHORTCUT_DOCK_GESTURE;
					completeAddApplication(intent);
				}
			}
				break;

			case IRequestCodeIds.REQUEST_CREAT_SHORTCUT_IN_DOCK_GESTURE : {
				if (resultCode == Activity.RESULT_OK) {
					completeAddShortcut(data);
				}
			}
				break;

			// case IRequestCodeIds.REQUEST_DOCK_CHANGE_APP_STYLE_ICON:
			// {
			// dockChangeIcon(requestCode, resultCode, data);
			// }
			// break;

			case IRequestCodeIds.REQUEST_PREVIEWIMAGE_FORICON : {
				screenChangeIcon(requestCode, resultCode, data);
			}
				break;

			case IRequestCodeIds.REQUEST_THEME_FORICON : {
				if (resultCode == Activity.RESULT_OK) {
					int frameid = ChangeIconPreviewActivity.sFromWhatRequester;
					if (frameid == ChangeIconPreviewActivity.DOCK_STYLE_FROM_EDIT
							|| frameid == ChangeIconPreviewActivity.DOCK_FOLDER_STYLE) {
						dockChangeIcon(requestCode, resultCode, data);
					} else if (frameid == ChangeIconPreviewActivity.SCREEN_STYLE
							|| frameid == ChangeIconPreviewActivity.USER_FOLDER_STYLE) {
						screenChangeIcon(requestCode, resultCode, data);
					} else if (frameid == ChangeIconPreviewActivity.SCREEN_FOLDER_ITEM_STYLE
							|| frameid == ChangeIconPreviewActivity.DOCK_FOLDER_ITEM_STYLE) {
						Bundle bundle = data.getExtras();
						GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
								IDiyMsgIds.QUICKACTION_EVENT, IQuickActionId.CHANGE_ICON, bundle,
								null);
					}
				}
			}
				break;

			case IRequestCodeIds.REQUEST_DOCK_CHANGE_APP_ICON : {
				if (resultCode == Activity.RESULT_OK) {
					Bundle bundle = data.getExtras();
					Intent intent = bundle.getParcelable(AppList.INTENT_STRING);

					if (intent == null) {
						return;
					}

					// 获取出intent中包含的应用
					final ShortCutInfo info = infoFromApplicationIntent2(mActivity, intent);
					if (info == null) {
						// 取不到快捷方式，大多情况出现在用户使用磁盘模式
						DeskToast.makeText(mActivity,
								mActivity.getString(R.string.cannot_read_app), Toast.LENGTH_LONG)
								.show();
						return;
					}

					GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.DOCK_ADD_APPLICATION, -1, info, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_ADD_APPLICATION,
							-1, info, null);
				}
			}
				break;

			case IRequestCodeIds.REQUEST_SCREEN_ADD_APP : {
				if (resultCode == Activity.RESULT_OK) {
					Bundle bundle = data.getExtras();
					ArrayList<Intent> intents = bundle
							.getParcelableArrayList(AppListMultiple.INTENT_LIST_STRING);
					// mApplicationHolerId = IShortcutHolderIds.SHORTCUT_SCREEN;
					completeAddApplications(intents);
				}
			}
				break;

			case IRequestCodeIds.REQUEST_THEME_SCAN_VIEW_REFRESH : {
			}
				break;

			case IRequestCodeIds.REQUEST_DESKTOP_FOLDER_EDIT : {
				if (resultCode != Activity.RESULT_CANCELED) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
							null, null);
				} else {
					GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
							IDiyMsgIds.BACK_FROM_EDIT, -1, null, null);
				}
			}
				break;

			case IRequestCodeIds.REQUEST_RENAME : {
				if (resultCode == Activity.RESULT_OK) {
					String name = data.getStringExtra(RenameActivity.NAME);
					int handlerid = data.getIntExtra(RenameActivity.HANDLERID, -1);
					long itemid = data.getLongExtra(RenameActivity.ITEMID, -1);
					ArrayList<String> list = new ArrayList<String>();
					list.add(name);
					GoLauncher.sendMessage(this, handlerid, IDiyMsgIds.RENAME, -1, itemid, list);
					
					GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.QUICKACTION_EVENT,
							IQuickActionId.RENAME, itemid, list);
					list.clear();
					list = null;
				}
			}
				break;

			default :
			//facebook
			Session session = Session.getActiveSession();
			if (session != null) {
				session.onActivityResult(GoLauncher.getContext(), requestCode, resultCode, data);
			}
			break;
		}
	}

	private void dockChangeIcon(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Bundle bundle = data.getExtras();
			bundle.putInt("OkOrCancle", 1);
			GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_CHANGE_STYLE_APP,
					-1, bundle, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_CHANGE_STYLE_APP,
					-1, bundle, null);
		}
	}

	private void screenChangeIcon(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Bundle bundle = data.getExtras();
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.QUICKACTION_EVENT,
					IQuickActionId.CHANGE_ICON, bundle, null);
			
			GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.QUICKACTION_EVENT,
					IQuickActionId.CHANGE_ICON, bundle, null);
		}
	}

	void showSearchDialog(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
			boolean globalSearch) {
		if (appSearchData == null) {
			appSearchData = new Bundle();
			appSearchData.putString(SearchManager.QUERY, "launcher-search");
		}

		final SearchManager searchManager = (SearchManager) mActivity
				.getSystemService(Context.SEARCH_SERVICE);

		try {
			searchManager.startSearch(initialQuery, selectInitialQuery,
					mActivity.getComponentName(), appSearchData, globalSearch);
		} catch (Exception e) {
			// NO permission: android.permission.GLOBAL_SEARCH_CONTROL
			Log.i(LogConstants.HEART_TAG, "showSearchDialog error");
			// e.printStackTrace();
		}
	}

	private void addLiveFolder(Intent intent) {
		// Handle case where user selected "Folder"
		String folderName = mActivity.getResources().getString(R.string.group_folder);
		String appDrawerfolderName = mActivity.getResources().getString(R.string.app_drawer_folder);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

		if (folderName != null && folderName.equals(shortcutName)) {
			addFolder();
		} else if (folderName != null && appDrawerfolderName.equals(shortcutName)) {
			addAppDrawerFolder();
		} else {
			startActivityForResult(intent, IRequestCodeIds.REQUEST_CREATE_LIVE_FOLDER);
		}
		folderName = null;
		shortcutName = null;
	}

	private void addFolder() {
		UserFolderInfo folderInfo = new UserFolderInfo();
		folderInfo.mTitle = mActivity.getText(R.string.folder_name);
		// folderInfo.setIcon(null);

		// 发送给屏幕层要求添加一个文件夹
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ADD_USER_FOLDER,
				-1, folderInfo, null);
	}

	private void addAppDrawerFolder() {
		int count[] = new int[1];
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_GET_VANCANT_COUNT, -1, count, null);
		if (0 == count[0]) {
			// 没有空间
			DeskToast.makeText(mActivity, R.string.no_more_room, Toast.LENGTH_SHORT).show();
		} else {
			// 启动选择功能表文件夹Activity
			Intent drawerFolderIntent = new Intent(mActivity, AllFoldersActivity.class);
			drawerFolderIntent.putExtra(AppFuncConstants.FOLDERCOUNT, count[0]);
			try {
				mActivity.startActivityForResult(drawerFolderIntent,
						IRequestCodeIds.REQUEST_CREATE_APPDRAWER_FOLDER);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void completeAddAppDrawerFolder(Intent data) {
		if (null == data) {
			Log.i("AddDrawerFolder", "Intent is null");
			return;
		}
		Bundle bundle = data.getExtras();
		if (null == bundle) {
			Log.i("AddDrawerFolder", "Intent bundle is null");
			return;
		}
		ArrayList<AppFuncFolderInfoToDesk> folderList = bundle
				.getParcelableArrayList(AppFuncConstants.FOLDERINFOLIST);
		if (null == folderList) {
			Log.i("AddDrawerFolder", "Intent data is null");
			return;
		}

		// 循环取出,进行数据转化
		ArrayList<UserFolderInfo> deskFolderList = new ArrayList<UserFolderInfo>();

		int sz = folderList.size();
		for (int i = 0; i < sz; i++) {
			UserFolderInfo deskFolder = dataChanged(folderList.get(i));
			if (null != deskFolder) {
				deskFolderList.add(deskFolder);
			}
		}

		// 发送给屏幕层要求添加一个文件夹
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_ADD_APPDRAWER_FOLDER, -1, null, deskFolderList);
	}

	private UserFolderInfo dataChanged(AppFuncFolderInfoToDesk funcFolder) {
		if (null == funcFolder) {
			return null;
		}
		UserFolderInfo deskFolder = new UserFolderInfo();
		deskFolder.mInScreenId = -1;
		deskFolder.mRefId = funcFolder.folderId;
		deskFolder.mTitle = funcFolder.name;
		deskFolder.mSpanX = 1;
		deskFolder.mSpanY = 1;
		if (null != funcFolder.intentList) {
			AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
			if (null == dataEngine) {
				return deskFolder;
			}
			int sz = funcFolder.intentList.size();
			for (int i = 0; i < sz; i++) {
				AppItemInfo info = dataEngine.getAppItem(funcFolder.intentList.get(i));
				if (null == info) {
					continue;
				}
				ShortCutInfo shortCutInfo = new ShortCutInfo();
				shortCutInfo.mIcon = info.mIcon;
				shortCutInfo.mIntent = info.mIntent;
				shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
				shortCutInfo.mSpanX = 1;
				shortCutInfo.mSpanY = 1;
				shortCutInfo.mTitle = info.mTitle;
				shortCutInfo.mTimeInFolder = System.currentTimeMillis();
				deskFolder.add(shortCutInfo);
			}
		}
		return deskFolder;
	}

	private void completeAddLiveFolder(Intent data) {
		final ScreenLiveFolderInfo info = addLiveFolder(mActivity, data);

		if (info != null) {
			// 将对象发送给屏幕层要求显示
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_ADD_LIVE_FOLDER, -1, info, null);
		}
	}

	private static ScreenLiveFolderInfo addLiveFolder(Context context, Intent data) {
		return SysAppInfo.createLiveFolder(context, data);
	}

	private void processPickAppwidget(final int resultCode, final Intent data) {
		if (data == null) {
			return;
		}

		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

		if (resultCode == Activity.RESULT_OK) {
			String customWidget = data.getStringExtra(EXTRA_CUSTOM_WIDGET);
			if (SEARCH_WIDGET.equals(customWidget)) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_DEL_APPWIDGET_ID, appWidgetId, null, null);
				// 要求屏幕层添加搜索框
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_ADD_SEARCH_WIDGET, -1, null, null);
			} else {
				// 获取到的AppwidgetID
				configureOrAddAppWidget(data);
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_DEL_APPWIDGET_ID, appWidgetId, null, null);
		}
	}

	private void configureOrAddAppWidget(Intent data) {
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		AppWidgetProviderInfo appWidget = AppWidgetManager.getInstance(mActivity).getAppWidgetInfo(
				appWidgetId);
		if (appWidget == null) {
			return;
		}
		if (appWidget.configure != null) {
			// Launch over to configure widget, if needed
			Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidget.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			startActivityForResult(intent, IRequestCodeIds.REQUEST_CREATE_APPWIDGET);
		} else {
			// Otherwise just add it
			onActivityResult(IRequestCodeIds.REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
		}
	}

	/**
	 * 添加快捷方式到对应UI层
	 * 
	 * @param data
	 *            The intent describing the shortcut.
	 */
	private void completeAddShortcut(Intent data) {
		if (data == null) {
			return;
		}

		// 获取出intent中包含的应用
		final ShortCutInfo info = infoFromShortcutIntent(mActivity, data);

		if (info == null) {
			// 取不到快捷方式，大多情况出现在用户使用磁盘模式
			DeskToast.makeText(mActivity, mActivity.getString(R.string.cannot_read_app),
					Toast.LENGTH_LONG).show();
			return;
		}

		// 将本应用发送给屏幕层进行添加工作
		switch (mShortcutHolerId) {
			case IShortcutHolderIds.SHORTCUT_SCREEN : {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_ADD_SHORTCUT, -1, info, null);
			}
				break;

			case IShortcutHolderIds.SHORTCUT_DOCK_CLICK : {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_ADD_SHORTCUT,
						-1, info, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_ADD_SHORTCUT,
						-1, info, null);
			}
				break;

			case IShortcutHolderIds.SHORTCUT_DOCK_GESTURE : {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.DOCK_ADD_SHORTCUT_FOR_GESTURE, -1, info, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
						IDiyMsgIds.DOCK_ADD_SHORTCUT_FOR_GESTURE, -1, info, null);
			}
				break;

			default :
				break;
		}
	}

	/**
	 * 添加widget到屏幕层
	 * 
	 * @param data
	 *            The intent describing the shortcut.
	 */
	private void completeAddAppWidget(Intent data) {
		if (data == null) {
			return;
		}

		Bundle bundle = data.getExtras();
		if (bundle != null) {
			final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

			// 将本应用发送给屏幕层进行添加工作
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_ADD_APPWIDGET, appWidgetId, null, null);
		}
	}

	/**
	 * 添加应用程序到DOCK层
	 * 
	 * @param data
	 *            Intent数据
	 */
	private void completeAddApplication(Intent data) {
		if (data == null) {
			return;
		}

		// 获取出intent中包含的应用
		// final ShortCutInfo info = infoFromApplicationIntent(mActivity, data);
		final ShortCutInfo info = infoFromApplicationIntent2(mActivity, data);
		if (info == null) {
			// 取不到快捷方式，大多情况出现在用户使用磁盘模式
			DeskToast.makeText(mActivity, mActivity.getString(R.string.cannot_read_app),
					Toast.LENGTH_LONG).show();
			return;
		}

		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
				IDiyMsgIds.DOCK_ADD_APPLICATION_GESTURE, -1, info, null);
		GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
				IDiyMsgIds.DOCK_ADD_APPLICATION_GESTURE, -1, info, null);
	}

	/**
	 * 添加应用程序合集到屏幕
	 * 
	 * @param intents
	 */
	private void completeAddApplications(ArrayList<Intent> intents) {
		if (intents == null) {
			return;
		}

		// 获取出intent中包含的应用
		ArrayList<ShortCutInfo> infos = new ArrayList<ShortCutInfo>();
		int size = intents.size();
		for (int i = 0; i < size; i++) {
			final ShortCutInfo info = infoFromApplicationIntent2(mActivity, intents.get(i));
			if (info == null) {
				// 取不到快捷方式，大多情况出现在用户使用磁盘模式
				Toast.makeText(mActivity, mActivity.getString(R.string.cannot_read_app),
						Toast.LENGTH_SHORT).show();
			} else {
				infos.add(info);
			}
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ADD_APPLICATIONS,
				-1, null, infos);

		infos.clear();
		infos = null;
	}

	private ShortCutInfo infoFromApplicationIntent2(Context context, Intent data) {
		AppItemInfo appItemInfo = GOLauncherApp.getAppDataEngine().getAppItem(data);
		if (appItemInfo == null) {
			// 异常情况
			Log.i(LogConstants.HEART_TAG, "infoFromApplicationIntent2 appItemInfo = null ");
			return infoFromApplicationIntent(context, data);
		}

		if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE.equals(data.getAction())
				|| ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME.equals(data.getAction())
				|| ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET.equals(data.getAction())
				|| ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(data.getAction())
				|| ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(data.getAction())) {
			ShortCutInfo shortCutInfo = new ShortCutInfo();
			shortCutInfo.mIcon = appItemInfo.mIcon;
			shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			shortCutInfo.mFeatureTitle = appItemInfo.mTitle;
			shortCutInfo.mIntent = appItemInfo.mIntent;
			shortCutInfo.mTitle = appItemInfo.mTitle;
			shortCutInfo.setRelativeItemInfo(appItemInfo);
			return shortCutInfo;
		} else {
			ComponentName component = data.getComponent();
			PackageManager packageManager = context.getPackageManager();
			ActivityInfo activityInfo = null;
			try {
				activityInfo = packageManager.getActivityInfo(component, 0);
			} catch (NameNotFoundException e) {
				Log.i(LogConstants.HEART_TAG,
						"Couldn't find ActivityInfo for selected application", e);
			}

			if (activityInfo != null) {
				ShortCutInfo itemInfo = new ShortCutInfo();
				// 注册
				// appItemInfo.registerObserver(itemInfo);

				itemInfo.mTitle = appItemInfo.getTitle();
				if (itemInfo.mTitle == null) {
					itemInfo.mTitle = activityInfo.name;
				}
				itemInfo.setActivity(component, Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

				itemInfo.mIcon = appItemInfo.getIcon();
				return itemInfo;
			}
		}
		return null;
	}

	private ShortCutInfo infoFromApplicationIntent(Context context, Intent data) {
		ComponentName component = data.getComponent();
		PackageManager packageManager = context.getPackageManager();
		ActivityInfo activityInfo = null;
		try {
			activityInfo = packageManager.getActivityInfo(component, 0 /*
																		 * no
																		 * flags
																		 */);
		} catch (NameNotFoundException e) {
			Log.i(LogConstants.HEART_TAG, "Couldn't find ActivityInfo for selected application", e);
		}

		if (activityInfo != null) {
			ShortCutInfo itemInfo = new ShortCutInfo();

			itemInfo.mTitle = activityInfo.loadLabel(packageManager);
			if (itemInfo.mTitle == null) {
				itemInfo.mTitle = activityInfo.name;
			}

			itemInfo.setActivity(component, Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			itemInfo.mIcon = Utilities.createIconThumbnail(activityInfo.loadIcon(packageManager),
					context);
			return itemInfo;
		}

		return null;
	}

	private ShortCutInfo infoFromShortcutIntent(Context context, Intent data) {
		return SysAppInfo.createFromShortcut(context, data);
	}

	private void handleThemeScanResult(int resultCode, Intent data) {
		// String key =
		// mActivity.getResources().getString(R.string.needthemeid);
		if (data == null) {
			// 自动返回，则不处理
			return;
		}
	}

	/**
	 * 系统配置改变事件
	 * 
	 * @param newConfig
	 *            新的配置
	 */
	public void onConfigurationChanged(Configuration newConfig) {
		PreferencesManager preferences = new PreferencesManager(mActivity,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		String currentlanguage = preferences.getString(IPreferencesIds.CURRENTSELETELANGUAGE, "");
		// 用户更换语言，重启桌面
		if ((null != mLastLocale) && !newConfig.locale.equals(mLastLocale)
				&& "".equals(currentlanguage)) {
			exit(true);
//			AppUtils.killProcess();
		}

		//		if (GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.PREVIEW_SHOWING, 0,
		//				null, null)) {
		//			return;
		//		}

		final boolean isChange = mLastOrientation != newConfig.orientation;
		// 正在添加桌面组件 时不广播屏幕切换事件
		if (isChange && !mAddingComponent) {
			GoLauncher.sendBroadcastMessage(this, IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED,
					newConfig.orientation, newConfig, null);
		}

		// 实体键盘处于推出状态，在此处添加额外的处理代码
		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
			OrientationControl.changeOrientationByKeyboard(mActivity, true, newConfig);
		} else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			OrientationControl.changeOrientationByKeyboard(mActivity, false, newConfig);
		}

		mLastOrientation = newConfig.orientation;
		onGGMenuConfigurationChanged(newConfig);

		mLastLocale = newConfig.locale;
		// 更新壁纸
		mWallpaperControler.updateWallpaper(true);
//		mWallpaperControler.updateWallpaperInBackground(true);
		//		// 横竖屏切换，语言选择列表对话框消失
		//		if (mSingleChoiceDialog != null && mSingleChoiceDialog.isShowing()) {
		//			mSingleChoiceDialog.dismiss();
		//		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPCORE_DATACHANGE : {
				if (param == DataType.DATATYPE_THEMESETTING) {
					checkPersistence();
				}
			}
				break;

			default :
				break;
		}
	}

	private void pickWidget() {
		// 添加widget
		Bundle bundle = new Bundle();
		bundle.putInt("id", -1);

		// 向屏幕层要求一个ID
		GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_GET_ALLOCATE_APPWIDGET_ID, -1, bundle, null);

		int allocateAppWidget = bundle.getInt("id");
		if (allocateAppWidget > -1) {
			Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
			pickIntent.putExtra(Intent.EXTRA_TITLE, mActivity.getText(R.string.select_widget_app));
			pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allocateAppWidget);

			// add the search widget
			ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
			AppWidgetProviderInfo info = new AppWidgetProviderInfo();
			info.provider = new ComponentName(mActivity.getPackageName(), "XXX.YYY");
			info.label = mActivity.getString(R.string.group_search);
			info.icon = R.drawable.ic_search_widget;
			customInfo.add(info);
			pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);

			// 添加搜索widget
			ArrayList<Bundle> customExtras = new ArrayList<Bundle>();
			Bundle b = new Bundle();
			b.putString(EXTRA_CUSTOM_WIDGET, SEARCH_WIDGET);
			customExtras.add(b);
			pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS,
					customExtras);

			// start the pick activity
			startActivityForResult(pickIntent, IRequestCodeIds.REQUEST_PICK_APPWIDGET);
		}
	}

	private void pickFolder() {
		// Insert extra item to handle inserting folder
		Bundle bundle = new Bundle();

		// ArrayList<String> shortcutNames = new ArrayList<String>();
		// shortcutNames.add(mActivity.getResources().getString(R.string.group_folder));
		// shortcutNames.add(mActivity.getResources().getString(R.string.app_drawer_folder));
		// bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME,
		// shortcutNames);

		// ArrayList<ShortcutIconResource> shortcutIcons = new
		// ArrayList<ShortcutIconResource>();
		// shortcutIcons.add(ShortcutIconResource.fromContext(
		// mActivity, R.drawable.ic_launcher_add_folder));
		// shortcutIcons.add(ShortcutIconResource.fromContext(
		// mActivity, R.drawable.ic_launcher_add_folder));
		// bundle.putParcelableArrayList(
		// Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(LiveFolders.ACTION_CREATE_LIVE_FOLDER));
		pickIntent.putExtra(Intent.EXTRA_TITLE,
				mActivity.getText(R.string.title_select_live_folder));
		pickIntent.putExtras(bundle);

		startActivityForResult(pickIntent, IRequestCodeIds.REQUEST_PICK_LIVE_FOLDER);
	}

	@Override
	public void onWallpaperChange(Drawable wallpaperDrawable) {
		final int yOffset = mStatusBarHandler.isFullScreen() ? 0 : StatusBarHandler
				.getStatusbarHeight();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SET_WALLPAPER_DRAWABLE,
				yOffset, wallpaperDrawable, null);
		GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME, IDiyMsgIds.SET_WALLPAPER_DRAWABLE,
				-1, null, null);
		mFrameControl.getRootView().requestLayout();
	}

	private boolean pressMenuKey(boolean vibrate) {
		if (mNeedOpenGGMenu) {
			mNeedOpenGGMenu = false;
			// 特殊情况，主桌面菜单怎么放在DIY里面 // 目前判断主桌面是否可见(DOCK可见)
			if (mFrameControl.isScreenOnTop()) {
				final GGMenu menu = onPrepareGGMenu(0);
				if (null != menu) {
					if (menu.isShowing()) {
						menu.dismiss();
					} else {
						menu.show(vibrate);
					}
				}
			}
		}
		return true;
	}

	private GGMenu createGGMenu(int frameId, int position) {
		View curView = mFrameControl.getFrame(frameId).getContentView();
		if (null == curView || curView.getWindowToken() == null) {
			return null;
		}
		int rows = 2;
		int columns = 4;
		OnMenuItemSelectedListener listener = new OnMenuItemSelectedListener() {
			@Override
			public void onMenuItemSelected(int id) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 0, null, null);
				switch (id) {
					case GGMenuData.GLMENU_ID_ADD : // 添加
						// mActivity.showDialog(IDialogIds.DIALOG_DIY);
						// 判断当前是否锁屏
						if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
							LockScreenHandler.showLockScreenNotification(mActivity);
							return;
						}
						// 发消息通知进入桌面编辑状态
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT, 1, null, null);
						
						GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
								IDiyMsgIds.SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT, 1, null, null);
						
						break;

					case GGMenuData.GLMENU_ID_WALLPAPER : // 壁纸
						// gotoWallpaperSelect();
						// 判断当前是否锁屏
						if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
							LockScreenHandler.showLockScreenNotification(mActivity);
							return;
						}
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT, 1,
								BaseTab.TAB_WALLPAPER, null);
						break;

					case GGMenuData.GLMENU_ID_SCREENEDIT : // 屏幕设置
						GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_SHOW_PREVIEW, 0, null, null);
						// add by jiang
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
								100, null, null);
						break;

					case GGMenuData.GLMENU_ID_THEME : // 主题设置
						// MyThemes Process
						PreferencesManager manager = new PreferencesManager(mActivity,
								IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
						boolean has = manager.getBoolean(IPreferencesIds.HASNEWTHEME, false);
						if (has) {
							manager.putBoolean(IPreferencesIds.HASNEWTHEME, false);
							manager.commit();
							GuiThemeStatistics.setCurrentEntry(GuiThemeStatistics.ENTRY_MENU_NEW,
									mActivity);
						} else {
							GuiThemeStatistics.setCurrentEntry(GuiThemeStatistics.ENTRY_MENU,
									mActivity);
						}
						Intent mythemesIntent = new Intent();
						mythemesIntent.putExtra("entrance", ThemeManageView.LAUNCHER_THEME_VIEW_ID);
						mythemesIntent.setClass(mActivity, ThemeManageActivity.class);
						mActivity.startActivity(mythemesIntent);
						
						break;

					case GGMenuData.GLMENU_ID_NOTIFICATIONBAR : // 通知栏
						try {
							WindowControl.expendNotification(mActivity);
						} catch (Exception e) {
							e.printStackTrace();
						}
						// add by jiang
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
								100, null, null);
						break;

					case GGMenuData.GLMENU_ID_SHARE : // 分享
					{
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.SET_SHARE_TYPE, ShareFrame.TYPE_SHARE, null, null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.SHARE_FRAME, null, null);
					}
						break;

					case GGMenuData.GLMENU_ID_SNAPSHOT : // 截屏
					{
						if (SnapShotManager.getInstance(mActivity).status != SnapShotManager.STATUS_STOP) {
							SnapShotManager.getInstance(mActivity).cancelCapture();
						} else {
							DialogUtil.checkSnapShotTutorial(mActivity);
						}
						// add by jiang
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
								100, null, null);
					}
						break;
					case GGMenuData.GLMENU_ID_FACEBOOK_LIKE_US : // Facebook Like us
						String facebookUrl = null;
						if (Machine.getCountry(mActivity).equals("kr")) {
							facebookUrl = "https://www.facebook.com/GoLauncherExKorea";
						} else {
							facebookUrl = "https://www.facebook.com/golauncher";
						}
						AppUtils.gotoDefaultBrowser(mActivity, facebookUrl);
						break;
					case GGMenuData.GLMENU_ID_PREFERENCE : // 更多设置
						startActivity(new Intent(mActivity, DeskSettingMainActivity.class), null);
						break;

					case GGMenuData.GLMENU_ID_EFFECT :
						// NOTE:起特效设置 (修改为跳桌面添加模块)
						// startActivity(new Intent(mActivity,
						// GoEffectsSettingActivity.class), null);
						// 判断当前是否锁屏
						if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
							LockScreenHandler.showLockScreenNotification(mActivity);
							return;
						}
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT, 1,
								BaseTab.TAB_EFFECTS, null);
						break;

					/*					case GGMenuData.GLMENU_ID_RATE :
											AppUtils.viewAppDetail(mActivity, ThemeManager.DEFAULT_THEME_PACKAGE);
											break;*/

					case GGMenuData.GLMENU_ID_UPDATE :
						// 点击跳转GOStore详情界面
						AppsDetail.gotoDetailDirectly(mActivity, 
								AppsDetail.START_TYPE_APPRECOMMENDED, mActivity.getPackageName());
//						GoStoreOperatorUtil.gotoStoreDetailDirectly(mActivity,
//								mActivity.getPackageName());
						break;

					case GGMenuData.GLMENU_ID_GOLOCKER :
						// AppUtils.gotoGolocker(mActivity);
						if (AppUtils.isGoLockerExist(mActivity)) {
							manager = new PreferencesManager(mActivity,
									IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
							has = manager.getBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, false);
							if (has) {
								manager.putBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, false);
								manager.commit();
							}
							mythemesIntent = new Intent();
							mythemesIntent.putExtra("entrance",
									ThemeManageView.LOCKER_THEME_VIEW_ID);
							mythemesIntent.setClass(mActivity, ThemeManageActivity.class);
							mActivity.startActivity(mythemesIntent);
						} else {
							// modify by Ryan 2012.08.29
							CheckApplication.downloadAppFromMarketGostoreDetail(mActivity,
									LauncherEnv.GO_LOCK_PACKAGE_NAME,
									LauncherEnv.Url.GOLOCKER_IN_MENU_WITH_GOOGLE_REFERRAL_LINK);
						}
						break;

					case GGMenuData.GLMENU_ID_GOWIDGET :
						// 判断当前是否锁屏
						if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
							LockScreenHandler.showLockScreenNotification(mActivity);
							return;
						}
						// NOTE:起一个GoWidgetFrame
						// GoLauncher.sendMessage(this,
						// IDiyFrameIds.SCHEDULE_FRAME,
						// IDiyMsgIds.SHOW_FRAME,
						// IDiyFrameIds.GOWIDGET_MANAGER_FRAME, null, null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT, 1,
								BaseTab.TAB_GOWIDGET, null);
						break;

					case GGMenuData.GLMENU_ID_NOTIFICATION :
						handleNotificationClick();
						break;

					case GGMenuData.GLMENU_ID_LANGUAGE :
						// NOTE:语言设置
						PreferencesManager sharedPreferencesLanguage = new PreferencesManager(
								mActivity, IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
						sharedPreferencesLanguage.putBoolean(
								IPreferencesIds.SHOULD_SHOW_LANGUAGE_GUIDE, false);
						sharedPreferencesLanguage.commit();
						showInstallLanguageTip(mActivity);
						// add by jiang
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
								100, null, null);
						break;

					case GGMenuData.GLMENU_ID_SYSSETTING : // 系统设置
					{
						final Intent settings = new Intent(
								android.provider.Settings.ACTION_SETTINGS);
						settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

						boolean bOk = startActivity(settings, null);
						if (!bOk) {
							PackageManager pm = mActivity.getPackageManager();
							Intent intent = pm.getLaunchIntentForPackage(LauncherEnv.SETTINGS);
							startActivity(intent, null);
							intent = null;
							pm = null;
						}
					}
						break;

					case GGMenuData.GLMENU_ID_FEEDBACK : // 意见反馈
					{
						if (AppUtils.isAppExist(mActivity, LauncherEnv.GO_DEBUG_HELPER_PKGNAME) 
								&& AppUtils.getVersionCodeByPkgName(mActivity, LauncherEnv.GO_DEBUG_HELPER_PKGNAME) > 1) {
							// 采用新的feedback形式
							Log.i("debugservice", "-----4----DiyScheduler-----feedback-");
							LogUnit.feedbackDebugService(mActivity);
							
						} else {
							DeskSettingQaTutorialActivity.startFeedbackIntent(mActivity);
						}
						
						// add by jiang
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
								100, null, null);
						
					}
						break;

					case GGMenuData.GLMENU_ID_GOSTORE : // Go精品
					{
//						Intent intent = new Intent();
//						intent.setClass(mActivity, GoStore.class);
						// 跳转到GO Store时把菜单项标识带上,用于GO精品入口统计
//						intent.putExtra(GoStorePublicDefine.APP_ID_KEY,
//								GoStorePublicDefine.GOLAUNCHER_MENU_ID);
//						mActivity.startActivity(intent);
						if (ChannelConfig.getInstance(mActivity).isNeedAppCenter()) {
							AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
									MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
						} else {
							AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
									MainViewGroup.ACCESS_FOR_APPCENTER_RECOMMEND, false);
						}
						// Log.e(null, "colin 跳转go store-------");
						StatisticsData.countStatData(mActivity, StatisticsData.ENTRY_KEY_MEUN);
//						GoStoreStatisticsUtil.setCurrentEntry(
//								GoStoreStatisticsUtil.ENTRY_TYPE_MENU, mActivity);
						AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mActivity, AppRecommendedStatisticsUtil.ENTRY_TYPE_MEUN_GOSTORE);
					}
						break;

					case GGMenuData.GLMENU_ID_LOCKEDIT : // 锁屏
					{
						// 锁屏
						showLockScreenDialog();
						// add by jiang
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
								100, null, null);
					}
						break;

					case GGMenuData.GLMENU_ID_UNLOCKEDIT : {
						// 解锁 处理逻辑
						ScreenSettingInfo info = GOLauncherApp.getSettingControler()
								.getScreenSettingInfo();
						info.mLockScreen = false;
						GOLauncherApp.getSettingControler().updateScreenSettingInfo(info);
						GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.UNLOCK_SCREEN, -1, null,
								null);
						LockScreenHandler.showUnlockScreenNotification(mActivity);

						// NOTE:updateItem
						//						ThemeManager themeManager = ThemeManager.getInstance(mActivity);
						//						ImageExplorer imageExplorer = ImageExplorer.getInstance(mActivity);
						//						String packageName = GoSettingControler.getInstance(mActivity)
						//								.getScreenStyleSettingInfo().getGGmenuStyle();
						//						MenuBean menuBean = themeManager.getGGmenuBean(packageName);
						//						Drawable drawable = GGMenuProvider.getMenuItemImage(menuBean,
						//								GGMenuData.GLMENU_ID_LOCKEDIT, mActivity, imageExplorer,
						//								packageName);
						//						String name = mActivity.getString(R.string.menuitem_lockdesktop_lock);
						//						mGGMenu.updateItem(GGMenuData.GLMENU_ID_UNLOCKEDIT,
						//								GGMenuData.GLMENU_ID_LOCKEDIT, drawable, name);
					}
						break;

					case GGMenuData.GLMENU_ID_RESTART : // 重启
						exit(true);
						break;
					case GGMenuData.GLMENU_ID_MESSAGE : // 消息中心
						startActivity(new Intent(mActivity, MessageCenterActivity.class), null);
						break;

					case GGMenuData.GLMENU_ID_GOBACKUP : // go备份
						AppUtils.gotoGobackup(mActivity);
						break;
					case GGMenuData.GLMENU_ID_GOHDLAUNCHER :
						AppUtils.gotoHDLauncher(mActivity);
						break;
					case GGMenuData.GLMENU_ID_APPCENTER :
						AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mActivity,
								AppRecommendedStatisticsUtil.ENTRY_TYPE_MENU);
						AppsManagementActivity.startAppCenter(mActivity,
								MainViewGroup.ACCESS_FOR_LAUNCHER_MENU, true);
						break;
					case GGMenuData.GLMENU_ID_ONE_X_GUIDE :
						// 跳转去下载专用版
						if (AppUtils.gotoBrowser(mActivity,
								"http://golauncher.goforandroid.com/?p=1728")) {
							PreferencesManager sharedPreferences = new PreferencesManager(
									mActivity, IPreferencesIds.USERTUTORIALCONFIG,
									Context.MODE_PRIVATE);
							sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_ONE_X_GUIDE,
									false);
							sharedPreferences.commit();
						}
						break;
					case GGMenuData.GLMENU_ID_GOHANDBOOK :
						// 跳转go手册
						startActivity(new Intent(mActivity, GoHandBookMainActivity.class), null);
						break;
					case GGMenuData.GLMENU_ID_MEDIA_MANAGEMENT_PLUGIN :
						if (MediaPluginFactory.isMediaPluginExist(GOLauncherApp.getContext())) {
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.SHOW_FRAME, IDiyFrameIds.APPFUNC_FRAME, null, null);
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
							} else {
								showMedPlugDownDialog();
							}
						break;
					default :
						break;
				}
			}
		};
		String packageName = GoSettingControler.getInstance(mActivity).getScreenStyleSettingInfo()
				.getGGmenuStyle();
		ImageExplorer imageExplorer = ImageExplorer.getInstance(mActivity);
		int textcolor = mMenuBean == null ? 0 : mMenuBean.mTextColor;
		int selecttabcolor = mMenuBean == null
				? GlMenuTabView.DEFALUTSELECTTABCOLOR
				: mMenuBean.mTabSelectFontColor;
		int unselecttabcolor = mMenuBean == null
				? GlMenuTabView.DEFALUTUNSELECTTABCOLOR
				: mMenuBean.mTabUnselectFontColor;

		// menu2.0
		TabData[] tabs = initScreenMenuData();
		Drawable drawableBg = GGMenuProvider.getBackgroundImage(mMenuBean, mActivity,
				imageExplorer, packageName);
		Drawable drawableItemLightBg = GGMenuProvider.getItemBackgroundImage(mMenuBean, mActivity,
				imageExplorer, packageName);
		Drawable drawableItemLline = GGMenuProvider.getItemLineImage(mMenuBean, mActivity,
				imageExplorer, packageName);
		Drawable drawableUnselectTabline = GGMenuProvider.getUnselectTabLineImage(mMenuBean,
				mActivity, imageExplorer, packageName);
		Drawable drawableSelectTabline = GGMenuProvider.getSelectTabLineImage(mMenuBean, mActivity,
				imageExplorer, packageName);
		Drawable drawableNewMsg = GGMenuProvider.getNewMessageNotifyImage(mMenuBean, mActivity,
				imageExplorer, packageName);

		GGMenu menu = createGGMenu(mActivity, curView, listener, rows, columns, tabs, textcolor,
				selecttabcolor, unselecttabcolor, drawableBg, drawableItemLightBg,
				drawableItemLline, drawableUnselectTabline, drawableSelectTabline, drawableNewMsg,
				position);
		menu.setmDismissListener(this);
		return menu;
	}
	/**
	 * <br>功能简述:创建菜单的Bean
	 * <br>功能详细描述:为了快速打开菜单。桌面加载完成后就加载菜单的BEAN。当更换主题后才重新加载
	 * <br>注意:
	 */
	public void createGGMenuBean() {
		ThemeManager themeManager = ThemeManager.getInstance(mActivity);
		String packageName = GoSettingControler.getInstance(mActivity).getScreenStyleSettingInfo()
				.getGGmenuStyle();
		mMenuBean = themeManager.getGGmenuBean(packageName);
	}

	private TabData[] initScreenMenuData() {
		// 获取当前的消息中心里面新消息的个数
//		int unReadMsg = MessageManager.getMessageManager(GOLauncherApp.getContext())
//				.getUnreadedCnt();

		String packageName = GoSettingControler.getInstance(mActivity).getScreenStyleSettingInfo()
				.getGGmenuStyle();
		ImageExplorer imageExplorer = ImageExplorer.getInstance(mActivity);
//		int[] tabUsalIds = new int[] { GGMenuData.GLMENU_ID_ADD, GGMenuData.GLMENU_ID_WALLPAPER,
//				GGMenuData.GLMENU_ID_THEME, GGMenuData.GLMENU_ID_GOSTORE,
//				GGMenuData.GLMENU_ID_SCREENEDIT, GGMenuData.GLMENU_ID_PREFERENCE,
//				GGMenuData.GLMENU_ID_SYSSETTING,
//				// GGMenuData.GLMENU_ID_EFFECT,
//				GGMenuData.GLMENU_ID_SNAPSHOT };
//		int[] tabUsalTextIds = new int[] { R.string.menuitem_addprogram,
//				R.string.menuitem_wallpaper, R.string.menuitem_themesetting,
//				R.string.menuitem_gostore, R.string.menuitem_screensetting,
//				R.string.menuitem_moresetting, R.string.menuitem_setting,
//				// R.string.effects,
//				R.string.menuitem_snapshot, };

		// Menu中当消息中心中有新的未读消息，消息中心与分享的位置互换
		final int[] tabUsalIds = new int[] { GGMenuData.GLMENU_ID_ADD,
				GGMenuData.GLMENU_ID_WALLPAPER, GGMenuData.GLMENU_ID_THEME,
				GGMenuData.GLMENU_ID_GOSTORE, GGMenuData.GLMENU_ID_SCREENEDIT,
				GGMenuData.GLMENU_ID_PREFERENCE, GGMenuData.GLMENU_ID_SYSSETTING,
				GGMenuData.GLMENU_ID_MESSAGE };
		final int[] tabUsalTextIds = new int[] { R.string.menuitem_addprogram,
				R.string.menuitem_wallpaper, R.string.menuitem_themesetting,
				R.string.menuitem_gostore, R.string.menuitem_screensetting,
				R.string.menuitem_moresetting, R.string.menuitem_setting,
				R.string.menuitem_msgcenter, };

		// 当消息中心还有新的消息的时候就替换数值
		//modified by liulixia 无论有无新消息，消息中心都显示在第一屏
//		if (unReadMsg > 0) {
//			tabUsalIds = tabUsalIdsTemp;
//			tabUsalTextIds = tabUsalTextIdsTemp;
//		}

		Drawable[] tabUsualImages = GGMenuProvider.getMenuItemImages(mMenuBean, tabUsalIds,
				mActivity, imageExplorer, packageName);
		String title = mActivity.getString(R.string.glmenu_screen_tab_common);
		TabData tabUsual = new TabData(title, tabUsalIds, tabUsalTextIds, tabUsualImages);

		// 语言设置特殊处理，要判断要不要显示
		int error = 0;
		int[] tabAroundIds = null;
		int[] tabAroundTextIds = null;
		if (null != DeskResourcesConfiguration.getInstance()) {
			error = DeskResourcesConfiguration.getInstance().getErrorCode();
		}
		if (Machine.isTablet(mActivity)) {
			tabAroundIds = new int[] { GGMenuData.GLMENU_ID_GOLOCKER,
					GGMenuData.GLMENU_ID_GOWIDGET, GGMenuData.GLMENU_ID_NOTIFICATION,
					GGMenuData.GLMENU_ID_GOBACKUP, GGMenuData.GLMENU_ID_LANGUAGE,
					GGMenuData.GLMENU_ID_APPCENTER, GGMenuData.GLMENU_ID_GAMEZONE,
					GGMenuData.GLMENU_ID_GOHDLAUNCHER, GGMenuData.GLMENU_ID_MEDIA_MANAGEMENT_PLUGIN };
			tabAroundTextIds = new int[] { R.string.menuitem_locker, R.string.menuitem_gowidget,
					R.string.menuitem_notification, R.string.menuitem_gobackup,
					R.string.menuitem_language, R.string.appcenter_title,
					R.string.gamecenter_title, R.string.menuitem_hdlauncher, 
					R.string.menuitem_mediamanagement_plguin };
		} else {
			tabAroundIds = new int[] { GGMenuData.GLMENU_ID_GOLOCKER,
					GGMenuData.GLMENU_ID_GOWIDGET, GGMenuData.GLMENU_ID_NOTIFICATION,
					GGMenuData.GLMENU_ID_GOBACKUP, GGMenuData.GLMENU_ID_LANGUAGE,
					GGMenuData.GLMENU_ID_APPCENTER, GGMenuData.GLMENU_ID_GAMEZONE,
					GGMenuData.GLMENU_ID_GOHANDBOOK, GGMenuData.GLMENU_ID_MEDIA_MANAGEMENT_PLUGIN };
			tabAroundTextIds = new int[] { R.string.menuitem_locker, R.string.menuitem_gowidget,
					R.string.menuitem_notification, R.string.menuitem_gobackup,
					R.string.menuitem_language, R.string.appcenter_title,
					R.string.gamecenter_title, R.string.go_handbook_title,
					R.string.menuitem_mediamanagement_plguin
			};
		}
		// 根据配置信息检查是否需要添加应用游戏中心菜单项
		// 如果不需要的话，我们统一在这里删除掉
		// Add by wangzhuobin, 2012.07.25
		ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
		if (null != channelConfig) {
			if (!channelConfig.isAddAppMainMenuItem()) {
				tabAroundIds = ArrayUtils.delete(tabAroundIds, GGMenuData.GLMENU_ID_APPCENTER);
				tabAroundTextIds = ArrayUtils.delete(tabAroundTextIds, R.string.appcenter_title);
			}
			if (!channelConfig.isAddGameMainMenuItem()) {
				tabAroundIds = ArrayUtils.delete(tabAroundIds, GGMenuData.GLMENU_ID_GAMEZONE);
				tabAroundTextIds = ArrayUtils.delete(tabAroundTextIds, R.string.gamecenter_title);
			}
		}

		Drawable[] tabAroundImages = GGMenuProvider.getMenuItemImages(mMenuBean, tabAroundIds,
				mActivity, imageExplorer, packageName);
		title = mActivity.getString(R.string.glmenu_screen_tab_common);
		TabData tabAround = new TabData(title, tabAroundIds, tabAroundTextIds, tabAroundImages);

		// 锁屏／解锁 处理逻辑
		int lockId = 0;
		int lockTextid = 0;
		ScreenSettingInfo info = GOLauncherApp.getSettingControler().getScreenSettingInfo();
		if (null != info) {
			if (!info.mLockScreen) {
				lockId = GGMenuData.GLMENU_ID_LOCKEDIT;
				lockTextid = R.string.menuitem_lockdesktop_lock;
			} else {
				lockId = GGMenuData.GLMENU_ID_UNLOCKEDIT;
				lockTextid = R.string.menuitem_lockdesktop_unlock;
			}
		}
//		int[] tabMoreIds = new int[] {
//				GGMenuData.GLMENU_ID_FEEDBACK, //GGMenuData.GLMENU_ID_RATE,
//				// GGMenuData.GLMENU_ID_SHARE,
//				GGMenuData.GLMENU_ID_UPDATE, lockId, GGMenuData.GLMENU_ID_NOTIFICATIONBAR,
//				GGMenuData.GLMENU_ID_RESTART, GGMenuData.GLMENU_ID_MESSAGE, };
//		int[] tabMoreTextIds = new int[] {
//				R.string.menuitem_feedback, //R.string.menuitem_rate,
//				// R.string.ShareMenu,
//				R.string.menuitem_update, lockTextid, R.string.menuitem_expend,
//				R.string.menuitem_lockdesktop_restart, R.string.menuitem_msgcenter, };

		// Menu中当消息中心中有新的未读消息，消息中心与分享的位置互换
		int[] tabMoreIds = null;
		int[] tabMoreTextIds = null;
		if (GoFacebookUtil.isEnable()) {
			tabMoreIds = new int[] {
					GGMenuData.GLMENU_ID_FEEDBACK,
					//GGMenuData.GLMENU_ID_RATE,
					// GGMenuData.GLMENU_ID_SHARE,
					GGMenuData.GLMENU_ID_UPDATE, lockId, GGMenuData.GLMENU_ID_NOTIFICATIONBAR,
					GGMenuData.GLMENU_ID_RESTART, GGMenuData.GLMENU_ID_SNAPSHOT,
					GGMenuData.GLMENU_ID_FACEBOOK_LIKE_US };
			tabMoreTextIds = new int[] {
					R.string.menuitem_feedback,
					//R.string.menuitem_rate,
					// R.string.ShareMenu,
					R.string.menuitem_update, lockTextid, R.string.menuitem_expend,
					R.string.menuitem_lockdesktop_restart, R.string.menuitem_snapshot,
					R.string.menuitem_facebook_like_us };
		} else {
			tabMoreIds = new int[] {
					GGMenuData.GLMENU_ID_FEEDBACK,
					//GGMenuData.GLMENU_ID_RATE,
					// GGMenuData.GLMENU_ID_SHARE,
					GGMenuData.GLMENU_ID_UPDATE, lockId, GGMenuData.GLMENU_ID_NOTIFICATIONBAR,
					GGMenuData.GLMENU_ID_RESTART, GGMenuData.GLMENU_ID_SNAPSHOT };
			tabMoreTextIds = new int[] {
					R.string.menuitem_feedback,
					//R.string.menuitem_rate,
					// R.string.ShareMenu,
					R.string.menuitem_update, lockTextid, R.string.menuitem_expend,
					R.string.menuitem_lockdesktop_restart, R.string.menuitem_snapshot };
		}

		// 当消息中心还有新的消息的时候就替换数值
//		if (unReadMsg > 0) {
//			tabMoreIds = tabMoreIdsTemp;
//			tabMoreTextIds = tabMoreTextIdsTemp;
//		}

		Drawable[] tabMoreImages = GGMenuProvider.getMenuItemImages(mMenuBean, tabMoreIds,
				mActivity, imageExplorer, packageName);
		title = mActivity.getString(R.string.glmenu_screen_tab_more);
		TabData tabMore = new TabData(title, tabMoreIds, tabMoreTextIds, tabMoreImages);

		TabData[] tabs = new TabData[] { tabUsual, tabAround, tabMore, };
		return tabs;
	}

	/**
	 * 弹出锁定屏幕提示对话框
	 */
	private void showLockScreenDialog() {
		DialogConfirm lockScreenDialog = new DialogConfirm(mActivity);
		lockScreenDialog.show();
		lockScreenDialog.setTitle(R.string.menuitem_lockdesktop_lock);
		lockScreenDialog.setMessage(R.string.lockdesktop_dialog_context);
		lockScreenDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					ScreenSettingInfo info = GOLauncherApp.getSettingControler()
							.getScreenSettingInfo();
					info.mLockScreen = true;
					GOLauncherApp.getSettingControler().updateScreenSettingInfo(info);
					GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.LOCK_SCREEN, -1, null, null);
					LockScreenHandler.showLockScreenNotification(mActivity);
					PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
					boolean needShowLockTutorial = sharedPreferences.getBoolean(
							IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GUIDE, true);
					if (needShowLockTutorial) {
						LockScreenHandler.showUnlockScreenNotificationLong(mActivity);
						sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GUIDE,
								false);

						sharedPreferences.putBoolean(
								IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GGMENU, true);
						sharedPreferences.commit();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}

	private GGMenu createGGMenu(Context context, View parentView,
			OnMenuItemSelectedListener listener, int rows, int columns, TabData[] tabs,
			int textColor, int selecttabColor, int unselecttabColor, Drawable background,
			Drawable itembackground, Drawable itemline, Drawable unselecttabline,
			Drawable selecttabline, Drawable newMsg, int position) {
		GGMenu menu = new GGMenu(context, parentView, R.layout.ggmenu_default, textColor,
				selecttabColor, unselecttabColor, background, itembackground, itemline,
				unselecttabline, selecttabline, newMsg);
		menu.setmMenuItemSelectedListener(listener);
		menu.setMenuData(tabs, GGMenuData.GGMENU_MAX_COLOUMNS, R.layout.ggmenu_default,
				R.layout.ggmenu_item_default, position);
		menu.setSelectTab(GlMenuTabView.COMMONTAB_ID);
		return menu;
	}

	public GGMenu onPrepareGGMenu(int position) {
		// 弹出菜单前，回收一次内存
		OutOfMemoryHandler.handle();
		if (null == mGGMenu) {
			int frameId = IDiyFrameIds.SCREEN_FRAME;
			if (ShellPluginFactory.isUseShellPlugin(mActivity)) {
				frameId = IDiyFrameIds.SHELL_FRAME;
			}
			mGGMenu = createGGMenu(frameId, position);
		}

		// 根据GO精品的渠道信息，更新菜单显示的文本
		updateGoStoreChannelName();

		return mGGMenu;
	}

	private void updateGoStoreChannelName() {
		if (null == mGGMenu || null == mActivity) {
			return;
		}
		// 菜单更新入口
		// GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_MENU,
		// mActivity);
		DeskThemeBean.MenuBean menuBean = null;
		ThemeManager themeManager = ThemeManager.getInstance(mActivity);
		// if (themeManager.isUsedTheme())
		// {
		// DeskThemeBean bean =
		// (DeskThemeBean)themeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_DESK);
		// if (null != bean)
		// {
		// menuBean = bean.mDeskMenuBean;
		// }
		// }
		String packageName = GoSettingControler.getInstance(mActivity).getScreenStyleSettingInfo()
				.getGGmenuStyle();
		menuBean = themeManager.getGGmenuBean(packageName);
		ImageExplorer imageExplorer = ImageExplorer.getInstance(mActivity);
		// NOTE:索引3是GO精品此项在数组常量中的下标，不需要try-catch,但如果对数组常量初始值修改要注意这个问题
		Drawable drawable = GGMenuProvider.getMenuItemImage(menuBean, GGMenuData.GLMENU_ID_GOSTORE,
				mActivity, imageExplorer, packageName);
		// 渠道名称
		String name = GoStoreChannelControl.getChannelCheckName(mActivity);
		mGGMenu.updateItem(GGMenuData.GLMENU_ID_GOSTORE, GGMenuData.GLMENU_ID_GOSTORE, drawable,
				name);
	}

	public void onDismissGGMenu() {
		if (null != mGGMenu && mGGMenu.isShowing()) {
			mGGMenu.dismiss();
		}
	}

	public void onGGMenuConfigurationChanged(Configuration newConfig) {
		recreateGGMenu();
	}

	public void onGGMenuThemeChanged() {
		recreateGGMenu();
	}

	private void recreateGGMenu() {
		if (null == mGGMenu) {
			// 没有生成直接返回
			return;
		}
		boolean bShow = mGGMenu.isShowing();
		if (!bShow) {
			// 没有显示：删除GGMENU
			mGGMenu = null;
		} else {
			// 显示：创建、显示
			mGGMenu.dismiss();
			int frameId = IDiyFrameIds.SCREEN_FRAME;
			if (ShellPluginFactory.isUseShellPlugin(mActivity)) {
				frameId = IDiyFrameIds.SHELL_FRAME;
			}
			mGGMenu = createGGMenu(frameId, 0);

			// 根据GO精品的渠道信息，更新菜单显示的文本
			updateGoStoreChannelName();
		}
	}

	private void captureScreen() {
		long time = System.currentTimeMillis();
		View view = mFrameControl.getRootView();
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = view.getDrawingCache();
		if (bitmap != null) {
			String fileName = LauncherEnv.Path.SCREEN_CAPUTRE_PATH + Long.toString(time) + ".png";
			BitmapUtility.saveBitmap(bitmap, fileName);

			DeskToast.makeText(mActivity, fileName, Toast.LENGTH_SHORT).show();
		}
		view.setDrawingCacheEnabled(false);
	}

	@Override
	public void onDismiss() {
		mGGMenu.cleanup();
		mGGMenu = null;
	}

	private void showInstallSwitchWidgetDialog(int msgId) {
		AlertDialog dialog = new AlertDialog.Builder(mActivity).setMessage(msgId)
				.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
//						GoStoreOperatorUtil.gotoStoreDetailDirectly(mActivity,
//								ICustomAction.PKG_GOWIDGET_SWITCH);
						AppsDetail.gotoDetailDirectly(mActivity, 
								AppsDetail.START_TYPE_APPMANAGEMENT, ICustomAction.PKG_GOWIDGET_SWITCH);
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	/**
	 * 弹出语言列表
	 */
	public void showInstallLanguageTip(final Context context) {
		mLanguageChoiceDialog = new DialogLanguageChoice(mActivity);
		mLanguageChoiceDialog.show();
	}

	public void showRestartDialog(final Context context, SharedPreferences preferences, int item) {
		final SharedPreferences restartPreferences = preferences;
		final int restartItem = item;
		AlertDialog mAlertDialog = null;
		if (mAlertDialog == null) {
			mAlertDialog = new AlertDialog(context) {
				@Override
				public void onBackPressed() {
				}
			};
			mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
			mAlertDialog.setTitle(context.getString(R.string.restart_title));
			mAlertDialog.setMessage(context.getString(R.string.restart_content));
			// 添加确定按钮
			mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					context.getString(R.string.restart_confirm),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
							Editor editor = restartPreferences.edit();
							editor.putInt(IPreferencesIds.CURRENTSELETELANGUAGE, restartItem);
							editor.commit();
							// 重启桌面
							exit(true);
						}
					});
			// 添加取消按钮
			mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
						}
					});
		}
		// 对话框没打开时才执行
		if (!mAlertDialog.isShowing()) {
			mAlertDialog.show();
		}
	}

	private void handleRecommandIcon(String pkgName) {
		if (LauncherEnv.Plugin.RECOMMAND_GOSMS_PACKAGE.equals(pkgName)
				|| LauncherEnv.Plugin.RECOMMAND_GOPOWERMASTER_PACKAGE.equals(pkgName)
				|| LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE.equals(pkgName)
				|| LauncherEnv.Plugin.RECOMMAND_GOKEYBOARD_PACKAGE.equals(pkgName)
				|| LauncherEnv.Plugin.RECOMMAND_GOBACKUPEX_PACKAGE.equals(pkgName)
				|| LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE.equals(pkgName)
				|| LauncherEnv.Plugin.RECOMMAND_LOCKSCREEN_PACKAGE.equals(pkgName)) {
			// go短信、go省电、go任务管理器、go输入法、go备份、go天气,一键锁屏

			// 替换文件夹内的推荐下载图标
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.REPLACE_RECOMMAND_ICON_IN_FOLDER, -1, pkgName, null);
			// 替换在桌面的推荐下载图标
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_REPLACE_RECOMMEND_ICON, -1, pkgName, null);
		}
	}

	/**
	 * ggmenu通讯统计项处理
	 */
	private void handleNotificationClick() {
		final boolean isCnUser = Machine.isCnUser(mActivity);
		PackageInfo info = AppUtils
				.getAppPackageInfo(mActivity, ICustomAction.NOTIFICATION_PACKAGE);
		// 未安装或对应通讯统计包版本低于1.96
		String title = null;
		String content = null;
		boolean isNeedShowTip = false;
		if (null == info) {
			title = mActivity.getString(R.string.notification_tip_title);
			content = mActivity.getString(R.string.notification_tip_content);
			isNeedShowTip = true;
		} else if (info.versionCode < 16) {
			title = mActivity.getString(R.string.notification_tip_title);
			content = mActivity.getString(R.string.notification_update_tip_content);
			isNeedShowTip = true;
		}
		if (isNeedShowTip) {
			// String marketUrl = LauncherEnv.Market.BY_PKGNAME
			// + LauncherEnv.Plugin.NOTIFICATION_PACKAGE_NAME;
			String linkArray[] = { LauncherEnv.Plugin.NOTIFICATION_PACKAGE_NAME,
					LauncherEnv.Url.NOTIFICATION_URL };
			// CheckApplication.showTip(context, title, content, linkArray,
			// isCnUser);
			// add by chenguanyu 2012.7.9
			CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
					LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK, title, System.currentTimeMillis(),
					isCnUser, CheckApplication.FROM_MENU);
			linkArray = null;
			title = null;
			content = null;
			isNeedShowTip = false;
		} else {
			Intent intent = new Intent(mActivity, NotificationSettingActivity.class);
			mActivity.startActivity(intent);
		}
	}

	/**
	 * <br>功能简述:从其他版本go桌面导入数据库
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param pkg 源数据库的包名
	 */
	public void importDBFromOtherLauncher(final String pkg) {

		new Thread(ThreadName.MIGRATEINTODESK_STARTMIGRARE) {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				DBImporter.importDB(mActivity, pkg, mActivity.getPackageName());
				DataProvider.getInstance(mActivity).checkPkgNameForImportDB(pkg);
				Intent it = new Intent(ICustomAction.ACTION_REMOVE_DB_READ_PERMISSION);
				it.putExtra("pkg", pkg);
				mActivity.sendBroadcast(it);
				LauncherSelectorActivity.sImportDB = false;
				exit(true);
			}
		}.start();

	}

	/**
	 * 罩子层消息处理
	 */
	private void coverFrameHandle(int msgId, int viewId, Object object) {
		if (mCoverFrame != null) {
			mCoverFrame.handleMessages(msgId, viewId, object);
		}
	} // end showCoverFrame

	/**
	 * 移除罩子层
	 */
	private void removeCoverFrame() {
		if (mCoverFrame != null) {
			mFrameControl.removeCoverFrame();
			mCoverFrame = null;
		}
	} // end showCoverFrame

	@Override
	public void handleRemoveCoverView() {
		removeCoverFrame();
	}

	@Override
	public void handleHideMaskView() {
		if (mCoverFrame != null) {
			mCoverFrame.hideMaskView();
		}
	}

	@Override
	public void handleShowMaskView() {
		if (mCoverFrame != null) {
			mCoverFrame.onWakeUp();
		}
	}

	@Override
	public void handleRemoveHolidayView() {
		coverFrameHandle(IDiyMsgIds.COVER_FRAME_REMOVE_VIEW, CoverFrame.COVER_VIEW_HOLIDAY, null);
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
		dialog.setPositiveButton(R.string.download_mediamanagement_plugin_dialog_download_btn_text, 
				new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 跳转进行下载
				Context context = GOLauncherApp.getContext();
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
}
