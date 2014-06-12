package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

import com.gau.go.launcherex.R;
import com.go.util.DeferredHandler;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.appgame.appcenter.appmigration.AppMigrationManagerView;
import com.jiubang.ggheart.appgame.appcenter.bean.NoPromptUpdateInfo;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManageView;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.component.AppsNoUpdateViewContainer;
import com.jiubang.ggheart.appgame.appcenter.component.AppsNoUpdateViewContainer.OnButtonClick;
import com.jiubang.ggheart.appgame.appcenter.component.AppsUninstallView;
import com.jiubang.ggheart.appgame.appcenter.component.PackageManagementView;
import com.jiubang.ggheart.appgame.appcenter.contorler.ApplicationManager;
import com.jiubang.ggheart.appgame.appcenter.contorler.AppsManageViewController;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.AppGameNetLogControll;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.downloadmanager.AppsDownloadActivity;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameInstalledFilter;
import com.jiubang.ggheart.appgame.base.utils.AppGameInstallingValidator;
import com.jiubang.ggheart.appgame.base.utils.LoadingTipUtil;
import com.jiubang.ggheart.appgame.base.utils.SearchKeywordUtil;
import com.jiubang.ggheart.appgame.base.utils.WifiTimerTask;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appmanagement.help.AppsManagementConstants;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 应用中心/游戏中心根视图，负责管理TabManageView，AppsManagementSearchView等
 * 
 */
public class MainViewGroup extends FrameLayout implements IMessageHandler {

	/**
	 * 进入应用中心/游戏中心的入口,点击快捷方式进入
	 */
	public static final int ACCESS_FOR_SHORTCUT = 1;

	/**
	 * 进入应用中心/游戏中心的入口,点击功能表菜单项进入
	 */
	public static final int ACCESS_FOR_FUNC_MUNE = 2;

	/**
	 * 进入应用中心/游戏中心的入口,点击goStore更新按钮进入
	 */
	public static final int ACCESS_FOR_GOSTORE_UPDATE = 3;

	/**
	 * 进入应用中心/游戏中心的入口,点击应用图标上面的更新提示进入
	 */
	public static final int ACCESS_FOR_UPDATE = 4;
	/**
	 * 进入应用中心/游戏中心的入口, 点击一键装机中的按钮进入
	 */
	public static final int ACCESS_FOR_RECOMMENDLIST = 5;
	/**
	 * 进入应用中心/游戏中心的入口,点击goStore应用中心按钮进入
	 */
	public static final int ACCESS_FOR_GOSTORE = 6;
	/**
	 * 进入应用中心/游戏中心的入口,点击桌面菜单进入
	 */
	public static final int ACCESS_FOR_LAUNCHER_MENU = 7;
	/**
	 * 进入应用中心/游戏中心的入口,点击更新提示notifaction进入应用更新
	 */
	public static final int ACCESS_FOR_UPDATE_NOTIFACTION = 8;

	/**
	 * 进入应用中心/游戏中心的入口，点击应用游戏中心widget的搜索按钮，进入应用中心搜索页面
	 */
	public static final int ACCESS_FOR_WIDGET_SEARCH = 9;
	/**
	 * 进入应用中心/游戏中心的入口，点击应用游戏中心widget的管理按钮，进入应用中心管理页面
	 */
	public static final int ACCESS_FOR_WIDGET_MANAGER = 10;
	/**
	 * 进入应用中心/游戏中心的入口，点击应用中心图标上面的更新数字，进入应用中心首页
	 */
	public static final int ACCESS_FOR_APPCENTER_UPATE = 11;
	/**
	 * 进入应用中心/游戏中心的入口，进入主题页
	 */
	public static final int ACCESS_FOR_APPCENTER_THEME = 12;
	/**
	 * 进入应用中心/游戏中心的入口，进入壁纸页
	 */
	public static final int ACCESS_FOR_APPCENTER_WALLPAPER = 13;

	/**
	 * 进入应用中心/游戏中心的入口，进入搜索页
	 */
	public static final int ACCESS_FOR_APPFUNC_SEARCH = 14;
	/**
	 * 进入应用中心/游戏中心的入口，进入应用页
	 */
	public static final int ACCESS_FOR_APPCENTER_APPS = 15;
	/**
	 * 进入应用中心/游戏中心的入口，进入游戏页
	 */
	public static final int ACCESS_FOR_APPCENTER_GAME = 16;
	/**
	 * 进入应用中心/游戏中心的入口，进入锁屏页
	 */
	public static final int ACCESS_FOR_APPCENTER_LOCKER = 17;
	/**
	 * 进入应用中心/游戏中心的入口，进入推荐页
	 */
	public static final int ACCESS_FOR_APPCENTER_RECOMMEND = 18;
	/**
	 * 进入应用中心/游戏中心的入口，进入软件页
	 */
	public static final int ACCESS_FOR_APPCENTER_SOFTWARE = 19;
	/**
	 * 进入应用中心/游戏中心的入口，进入插件页
	 */
	public static final int ACCESS_FOR_APPCENTER_WIDGET = 20;
	/**
	 * 从桌面主题预览界面进入 ==>主题-推荐，收费入口
	 */
	public static final int ACCESS_FOR_APPCENTER_THEME_PAY = 21;

	public static final int FLAG_NONE = 1000;
	public static final int FLAG_INSTALL = 1001; // 新安装应用
	public static final int FLAG_UNINSTALL = 1002; // 卸载应用
	public static final int FLAG_CHANGE = 1003; // 更改应用
	public static final int FLAG_UPDATE = 1004; // 升级应用

	private Handler mHandler = null;
	
//	private boolean mIsNoAfToken = true ; //　没有木瓜sdk　token缓存

//	private Handler mTimeOutHandler = null; // 用于控制timeOut的handler
	/**
	 * 刷新热门搜索关键词
	 */
	private static final int DMESSAGE_UPDATE_EXTRA_DATA = 3422;
	/**
	 * 获取更新数据
	 */
	private static final int DMESSAGE_GET_UPDATEDATA = 3426;
	/**
	 * 预加载，后台拿到新的数据，更新缓存的数据,并如果当前页面数据有变化，刷新当前页面
	 */
	private static final int DMESSAGE_REFRESH_TOPTAB_DATA = 3428;
	/**
	 * 上传网络日志
	 */
	private static final int DMESSAGE_SEND_NETLOG = 3431;
	/**
	 * 注册所有的监听器
	 */
	private static final int DMESSAGE_REGISTER_ALLRECEIVER = 3432;
	/**
	 * 启动下载服务
	 */
	private static final int DMESSAGE_START_DOWNLOAD_SERVICE = 3433;
	/**
	 * 统计tab点击
	 */
	private static final int DMESSAGE_SAVE_TAB_CLICK = 3434;
	/**
	 * UI线程空闲时才会执行消息的handler,用于优化体验
	 */
	private DeferredHandler mDHandler = new DeferredHandler() {

		@Override
		public void handleIdleMessage(Message msg) {
			if (msg == null) {
				return;
			}
			int what = msg.what;
			switch (what) {
				case DMESSAGE_UPDATE_EXTRA_DATA :
					getHotSearchKeywords();
					break;
				case DMESSAGE_GET_UPDATEDATA :
					getUpdateData();
					break;
				case DMESSAGE_REFRESH_TOPTAB_DATA :
					try {
						List<Object> list = (List<Object>) msg.obj;
						List<Integer> ids = (List<Integer>) list.get(0);
						List<ClassificationDataBean> subDataBeanList = (List<ClassificationDataBean>) list
								.get(1);
						if (subDataBeanList.size() > 0) {
							Log.e("MainViewGroup", "DMESSAGE_REFRESH_TOPTAB_DATA list.size() = "
									+ subDataBeanList.size());
						}
						// 顶级tab栏数据已经压栈
						if (TabDataManager.getInstance().getTabStackSize() > 0) {
							// 更新顶级tab栏数据
							TabDataManager.getInstance().refreshTopTabData(ids,
									AppGameInstalledFilter.filterDataBeanList(subDataBeanList));
							// 如果当前是顶层tab栏
							if (TabDataManager.getInstance().getTabStackSize() == 1) {
								for (ClassificationDataBean bean : subDataBeanList) {
									if (bean == null) {
										continue;
									}
									int typeid = bean.typeId;
									// 根据分类id找出对应的缓存数据和container
									ClassificationDataBean cacheBean = TabDataManager.getInstance()
											.getTabData(typeid);
									IContainer container = mTabManageView.getContainerByID(typeid);
									if (container != null) {
										container.updateContent(cacheBean, true);
									}
								}
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
					break;
				case DMESSAGE_SEND_NETLOG:
					// 上传收集的网络数据
					AppGameNetLogControll.getInstance().sendNetLog(mContext);
					break;
				case DMESSAGE_REGISTER_ALLRECEIVER:
					// 注册所有的监听器
					registerAllReceiver();
					break;
				case DMESSAGE_START_DOWNLOAD_SERVICE:
					// 先启动下载服务
					GOLauncherApp.getContext().startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
					// 再bind服务
					if (!mHasBindService) {
						mHasBindService = GOLauncherApp.getContext().bindService(
								new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), mConnenction,
								Context.BIND_AUTO_CREATE);
					}
					break;
				case DMESSAGE_SAVE_TAB_CLICK:
					// 统计tab点击
					try {
						AppManagementStatisticsUtil.getInstance();
						AppManagementStatisticsUtil.saveTabClickData(getContext(), msg.arg1, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default :
					break;
			}
		}
	};

	/**
	 * tab栏视图
	 */
	private TabManageView mTabManageView;

	/**
	 * 忽略更新页面
	 */
	private AppsNoUpdateViewContainer mAppNoUpdateContainer;

	/**
	 * 搜索页面，在分类应用和精品应用点击搜索时显示
	 */
	private AppsManagementSearchView mSearchView;
	/**
	 * 批量卸载界面
	 */
	private AppsUninstallView mAppsuninstallview;
	/**
	 * “应用搬家”界面
	 */
	private AppMigrationManagerView mAppMigrationManagerView = null;

	/**
	 * 安装包管理节目
	 */
	private PackageManagementView mPackageManagementView = null;

	/**
	 * 是否在打开批量卸载页面,防止重复点击
	 */
	private boolean mIsOpenUninstallView = false;
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	
	/**
	 * 　搜索按钮
	 */
	
	private RelativeLayout mSearchButton = null;
	/**
	 * 入口值，1:从快捷方式进入2:从menu进入3:goStore更新按钮进入4:点击应用图标上面的更新提示进入5:点击一键装机中的按钮进入
	 * 入口类型定义见6:goStore应用中心按钮进入 {@link MainViewGroup}
	 */
	private int mEntranceId;

	/**
	 * 是否已经绑定下载服务的标记
	 */
	private boolean mHasBindService = false;
	/**
	 * 启动loading页
	 */
	private View mStartLoading = null;
	/**
	 * 是否展示启动loading页
	 */
	private boolean mShowFrontCover = false;
	/**
	 * 让loading提示页消失的runnable，当这个runnable一执行就马上把它致空，表示这个runable已经执行过了
	 */
	private Runnable mDismissLoadingRunnable = new Runnable() {

		@Override
		public void run() {
			// 把mTabManageView设为可见
			mTabManageView.setVisibility(View.VISIBLE);
			// 致空，表示这个runable已经执行过了
			mDismissLoadingRunnable = null;
			// 消失loading页
			MainViewGroup.this.removeView(mStartLoading);
			mStartLoading = null;
				// TODO 木瓜sdk才开的代码
//            	if (mIsNoAfToken) {
//            		// 到要消失加载界面却还没拿到token就强刷界面，并设mIsNoAfToken为true,就是之后token回来了也不刷新的界面
//            		AppsManagementActivity.sendHandler(mContext,
//            				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
//            				IDiyMsgIds.LOAD_MAIN_VIEW, 0,
//            				null, null);
//            		mIsNoAfToken = false;
//            	}
		}
	};
	/**
	 * 更新数据的controller
	 */
	private AppsManageViewController mUpdateController;
	/**
	 * 更新数据结果监听器
	 */
	private IModeChangeListener mUpdateListener = new IModeChangeListener() {

		@Override
		public void onModleChanged(int action, int state, Object value) {
			switch (action) {
				case AppsManageViewController.ACTION_START_REQUEST : {
					mUpdateState = state;
					mUpdateData = value;
					//				mTabManageView.setUpdateData(value, state, true);
					// // 发消息更新tab
					if (value != null && value instanceof AppsBean) {
						AppsBean mAppsBean = (AppsBean) value;
						//
						int size = 0;
						ArrayList<AppBean> allAppBeanList = mAppsBean.mListBeans;
						if (allAppBeanList != null) {
							// int size = 0;
							// size = allAppBeanList.size();
							// // 保存现在可更新应用的个数
							// saveUpdateCount(size);
							//
							// // 修改应用更新tab头上更新的数字
							// AppsManagementActivity.sendMessage(this,
							// IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
							// IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT, size,
							// null, null);
							//
							int noUpdateCount = filterNoUpdateApp(allAppBeanList);

							size = allAppBeanList.size() - noUpdateCount;
						}
						// 保存现在可更新应用的个数
						saveUpdateCount(size);
						// 修改应用更新tab头上更新的数字
						AppsManagementActivity.sendMessage(this,
								IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
								IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT, size, null, null);

						mTabManageView.setUpdateData(value, state, true);

						HashMap<Integer, Byte> controlMap = mAppsBean.mControlcontrolMap;
						if (controlMap != null && !controlMap.isEmpty()) {
							setmControlInfo(controlMap.get(2), controlMap.get(3), controlMap.get(4));
						}
						// 更新提示notification
						if (ChannelConfig.getInstance(mContext).isNeedAppCenter()) {
							sendUpdateInfoToNotification(mAppsBean);
						}
						// }
					} else {
						mTabManageView.setUpdateData(value, state, true);
						if (state == AppsManageViewController.MSG_ID_FINISH) {
							saveUpdateCount(0);
							// 修改应用更新tab头上更新的数字
							AppsManagementActivity.sendMessage(this,
									IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
									IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT, 0, null, null);
						}
					}
				}
					break;
				default :
					break;
			}

		}
	};

	/**
	 * 过滤忽略更新的应用
	 */
	private int filterNoUpdateApp(ArrayList<AppBean> appListBeans) {
		int count = 0;
		if (appListBeans != null && appListBeans.size() > 0) {
			ArrayList<NoPromptUpdateInfo> noUpdateAppList = ApplicationManager.getInstance(
					getContext()).getAllNoPromptUpdateApp();
			if (noUpdateAppList != null && noUpdateAppList.size() > 0) {
				for (NoPromptUpdateInfo noUpdateInfo : noUpdateAppList) {
					String packageName = getPkgNameByIntent(noUpdateInfo.getIntent());
					if (packageName != null) {
						for (AppBean appBean : appListBeans) {
							if (packageName.equals(appBean.mPkgName)) {
								appBean.mIsIngore = true;
								count++;
								break;
							}
						}
					}
				}
			}
		}
		return count;
	}

	/**
	 * 根据intent获取包名
	 * 
	 * @param intent
	 * @return
	 */
	private String getPkgNameByIntent(Intent intent) {
		String packageName = null;
		if (intent != null) {
			ComponentName comonentName = intent.getComponent();
			if (comonentName != null) {
				packageName = comonentName.getPackageName();
			}
		}
		return packageName;
	}

	/**
	 * 当网络状态变成ok时是否需要刷新当前tab
	 */
	private boolean mNeedToRefreshWhenNetworkOK = false;
	/**
	 * 获取更新数据的状态
	 */
	private int mUpdateState = AppsManageViewController.MSG_ID_NOT_START;
	/**
	 * 可更新应用数据
	 */
	private Object mUpdateData;
	/**
	 * 下载服务的控制接口Connector
	 */
	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IDownloadService mController = IDownloadService.Stub.asInterface(service);
			// 设置整个进程通用的下载控制接口
			GOLauncherApp.getApplication().setDownloadController(mController);
			try {
				if (mController == null) {
					return;
				}
				Map<Long, DownloadTask> map = mController.getDownloadConcurrentHashMap();
				ArrayList<DownloadTask> downloadTaskList = new ArrayList<DownloadTask>();
				for (DownloadTask dt : map.values()) {
					downloadTaskList.add(dt);
				}
				showNotification(downloadTaskList);
				mTabManageView.setmDownloadTasks(downloadTaskList);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("", "MainViewGroup onServiceDisconnected");
		}
	};
	/**
	 * SD卡事件监听器
	 */
	private final BroadcastReceiver mSDCardListener = new BroadcastReceiver() {
		Boolean mStatus = false;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
				// 通知各子页面SD卡连接开启
				if (mTabManageView != null) {
					mTabManageView.onSDCardStateChange(true);
				}
				// “应用搬家“通知SD卡连接开启，需要刷新列表数据
				if (mAppMigrationManagerView != null) {
					mAppMigrationManagerView.sdCardTurnOn();
				}
				// “安装包管理“通知SD卡连接开启，需要刷新列表数据
				if (mPackageManagementView != null) {
					mPackageManagementView.sdCardTurnOn();
				}
				mStatus = false;
			} else if (Intent.ACTION_MEDIA_REMOVED.equals(action)
					|| Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
					|| Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
				if (mStatus == false) {
					// 通知各子页面SD卡连接关闭
					if (mTabManageView != null) {
						mTabManageView.onSDCardStateChange(false);
					}
					// “应用搬家“通知SD卡连接关闭，需要刷新列表数据
					if (mAppMigrationManagerView != null) {
						mAppMigrationManagerView.sdCardTurnOff();
					}
					// “安装包管理“通知SD卡连接关闭，需要刷新列表数据
					if (mPackageManagementView != null) {
						mPackageManagementView.sdCardTurnOff();
					}
					mStatus = true;
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
				int installFlag = FLAG_NONE;
				if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					installFlag = FLAG_INSTALL;
					if (replacing) {
						installFlag = FLAG_UPDATE;
					}
				} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
					if (!replacing) {
						installFlag = FLAG_UNINSTALL;
					}
				} else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
					installFlag = FLAG_UPDATE;
				}
				AppGameInstallingValidator.getInstance().onAppAction(getContext(), packageName);
				// 假如正在显示“应用搬家”的界面
				if (mAppMigrationManagerView != null) {
					mAppMigrationManagerView.onAppAction(packageName, installFlag);
				}
				if (mPackageManagementView != null) {
					mPackageManagementView.onAppAction(packageName, installFlag);
				}
				// 通知各子页面应用安装卸载事件
				if (mTabManageView != null) {
					mTabManageView.onAppAction(packageName, installFlag);
					// 更新应用更新数据，并重新通知TabManageView
					if (mUpdateData != null && mUpdateData instanceof AppsBean) {
						AppsBean mAppsBean = (AppsBean) mUpdateData;
						ArrayList<AppBean> allAppBeanList = mAppsBean.mListBeans;
						if (allAppBeanList != null) {
							int position = -1;
							int size = allAppBeanList.size();
							AppBean appBean = null;
							for (int i = 0; i < size; i++) {
								appBean = allAppBeanList.get(i);
								if (appBean.mAppId > 0 && appBean.mPkgName.equals(packageName)) {
									position = i;
									break;
								}
							}
							if (position != -1) {
								allAppBeanList.remove(position);
//								mUpdateData = allAppBeanList;
								mTabManageView.setUpdateData(mUpdateData,
										AppsManageViewController.MSG_ID_FINISH, false);
							}
						}
					}
				}

				if (mSearchView != null) {
					mSearchView.onAppAction(packageName, installFlag);
				}

				if (installFlag == FLAG_INSTALL || installFlag == FLAG_UNINSTALL) {
					// onPackageChanged(packageName,installFlag);
					// 安装或卸载后，忽略更新列表可能已经改变，要记得刷新一遍
					if (mAppNoUpdateContainer != null) {
						mAppNoUpdateContainer.refreshView();
					}
				}

			}
		}
	};
	/**
	 * 网络状态广播接收器
	 */
	private BroadcastReceiver mNetworkListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 通知图片管理器网络状态发生改变
			AsyncImageManager.getInstance().netWorkStateChange();
			if (Machine.isNetworkOK(getContext()) && mNeedToRefreshWhenNetworkOK) {
				if (mUpdateState == AppsManageViewController.MSG_ID_EXCEPTION
						|| mUpdateState == AppsManageViewController.MSG_ID_NOT_START) {
					getUpdateData();
				}
				TabController.refreshCurrentTab();
				mNeedToRefreshWhenNetworkOK = false;
			}
		}
	};
	// 应用中心独立进程后，从下载管理器回调回来的信息不能用IMessageHandler这种方式传递，要改用广播方式
	private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadTask downloadTask = intent
					.getParcelableExtra(AppDownloadListener.UPDATE_DOWNLOAD_INFO);
			mTabManageView.notifyDownloadState(downloadTask);
			if (mSearchView != null) {
				mSearchView.notifyDownloadState(downloadTask);
			}
		}
	};

	public MainViewGroup(Context context, int entranceId, boolean showFrontCover) {
		super(context);
		init(context, entranceId, showFrontCover);
	}

	public MainViewGroup(Context context, AttributeSet attrs, int entranceId, boolean showFrontCover) {
		this(context, attrs, 0, entranceId, showFrontCover);
	}

	public MainViewGroup(Context context, AttributeSet attrs, int defStyle, int entranceId,
			boolean showFrontCover) {
		super(context, attrs, defStyle);
		init(context, entranceId, showFrontCover);
	}

	/**
	 * 初始化MainViewGroup
	 * 
	 * @param context
	 * @param entranceId
	 *            入口id，
	 * @param showViewId
	 */
	private void init(Context context, int entranceId, boolean showFrontCover) {
		TabController.mT = System.currentTimeMillis();
		// 初始化
		mContext = context;
		mEntranceId = entranceId;
		mShowFrontCover = showFrontCover;
		mLayoutInflater = LayoutInflater.from(context);
		AppsManagementActivity.registMsgHandler(this);

		initView(entranceId);
	}

	/**
	 * 返回入口值，1:从快捷方式进入2:从menu进入3:goStore更新按钮进入4:点击应用图标上面的更新提示进入5:点击一键装机中的按钮进入
	 * 入口类型定义见6:goStore应用中心按钮进入{@link MainViewGroup}
	 */
	public int getEntrance() {
		return mEntranceId;
	}

	private void initView(int entranceId) {
		long mT= System.currentTimeMillis();
		mTabManageView = new AppsManageView(mContext, entranceId);

		this.addView(mTabManageView, new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		
		
		TabController.setTabManageView(mTabManageView);

		//　加入搜索按钮
		initSearchButton();
		
		if (mShowFrontCover) {
			//有启动loading页时把mTabManageView设为GONE，避免mTabManageView没必要的onMessure和onLayout
			mTabManageView.setVisibility(View.GONE);
			// 展示启动loading页
			showFrontCover();
		} else {
			mDismissLoadingRunnable = null;
		}
		
		// 木瓜sdk的初始化，与普通初始化互斥
//		afSdkInit();
		normalInit();
	}
	
//	private void afSdkInit() {
//		// TODO 木瓜SDK代码
//		Activity activity = GoMarketPublicUtil.getInstance(mContext).getActivity();
//		AFRequestDelegate initDelegate = new AFRequestDelegate() {
//            @Override
//            public void onFinish(JSONObject arg0) {
//            	// 成功拿到token,停止超时计时
//            	//　保存木瓜token
//            	GoMarketPublicUtil.getInstance(mContext).setAfToken(arg0.optString("token", ""));
//                // 启动顶级tab栏
//            	if (mIsNoAfToken) {
//            		// token获取成功，取消超时
////            		mTimeOutHandler.removeMessages(0);
//            		// 如本来没有token则刷新界面，如本来有token则只更新token
//            		AppsManagementActivity.sendHandler(mContext,
//            				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
//            				IDiyMsgIds.LOAD_MAIN_VIEW, 0,
//            				null, null);
//            	}
//            	
//            	mIsNoAfToken = false;
//            }
//        };
//        
//		String afToken = GoMarketPublicUtil.getInstance(mContext).getAfToken();
//		
//		if (afToken != null && !afToken.equals("")) {
//			//　木瓜token已经存在，直接走流程且后台更新token
//			mIsNoAfToken = false;
//			TabController.skipToTheNextTab(ClassificationDataBean.TOP_TYPEID, "", mEntranceId,
//					true, -1, -1, null);
//		}
//		
//        if (NetUtil.isPerferNetWorkCanUse(mContext)) {
//        	// 如果有网络可用，拿木瓜token
//        	AppFlood.initialize(activity, GoMarketPublicUtil.AF_APP_KEY, GoMarketPublicUtil.AF_SECRET_KEY, 
//        			AppFlood.AD_DATA, initDelegate);
////        	if (mIsNoAfToken) {
////        		mTimeOutHandler = new Handler() {
////        			public void handleMessage(Message msg) {
////        				//　如果超时没有成功获取token，则不拿token走一般流程，强制标记mIsNoAfToken为false,以免再出现界面刷新
////        				mIsNoAfToken = false ;
////                		AppsManagementActivity.sendHandler(mContext,
////                				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
////                				IDiyMsgIds.LOAD_MAIN_VIEW, 0,
////                				null, null);
////        			};
////        		};
////        		mTimeOutHandler.sendEmptyMessageDelayed(0, 4000); // ４秒后如无取消超时handler就发送超时消息
////        	}
//        } else {
//            if (mIsNoAfToken) {
//            	// 如果网络不可用，又没有token通知刷新ui
//            	AppsManagementActivity.sendHandler(mContext,
//            			IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
//            			IDiyMsgIds.LOAD_MAIN_VIEW, 0,
//            			null, null);
//            }
//        }
//	}
	
	/**
	 * 一般的初始化
	 */
	private void normalInit() {
		TabController.skipToTheNextTab(ClassificationDataBean.TOP_TYPEID, "", mEntranceId,
				true, -1, -1, null);
	}
	
	/**
	 * 　初始化搜索按钮
	 */
	private void initSearchButton() {
		mSearchButton = new RelativeLayout(mContext);
		mSearchButton.setBackgroundResource(R.drawable.gomarket_search_button_bg);
		int padButton = DrawUtils.dip2px(TabManageView.GRID_TITLE_BAR_HEIGHT);
		LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		params.setMargins(0, 0, 0, padButton);
			
		final ImageView searchIcon = new ImageView(mContext);
		searchIcon.setImageResource(R.drawable.gomarket_search_button_src);
		searchIcon.setScaleType(ScaleType.CENTER);
		int iconSize = DrawUtils.dip2px(35);
		RelativeLayout.LayoutParams icon_params = new RelativeLayout.LayoutParams(
				iconSize,
				iconSize);
		icon_params.addRule(RelativeLayout.BELOW);
		icon_params.addRule(RelativeLayout.LEFT_OF);
		mSearchButton.setGravity(Gravity.BOTTOM);
		mSearchButton.addView(searchIcon, icon_params);
			
		this.addView(mSearchButton, params);
		
		mSearchButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int state = event.getAction();
				switch (state) {
					case MotionEvent.ACTION_DOWN :
						searchIcon.setBackgroundResource(R.drawable.tabbar_press);
						break;
					case MotionEvent.ACTION_MOVE :
						break;
					case MotionEvent.ACTION_UP :
						searchIcon.setBackgroundDrawable(null);
						AppManagementStatisticsUtil.getInstance().saveTabClickData(mContext, 6, "");
						AppsManagementActivity.sendHandler(this,
								IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME, IDiyMsgIds.SHOW_SEARCH_VIEW,
								0, null, null);
						break;
					case MotionEvent.ACTION_CANCEL :
						searchIcon.setBackgroundDrawable(null);
					default :
						break;
				}
				return true;
			}
		});
	}

	private void sendUpdateInfoToNotification(AppsBean appsBean) {
		if (appsBean != null && !appsBean.mListBeans.isEmpty()) {
			// add by zhoujun 2010--0-14, 控制是否显示
			if (appsBean.mControlcontrolMap != null && !appsBean.mControlcontrolMap.isEmpty()
					&& appsBean.mControlcontrolMap.get(1) == 0) {
				return;
			}
			List<AppBean> appBeans = appsBean.mListBeans;
			Resources res = mContext.getResources();
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(AppsManagementConstants.NOTIFY_TAG,
					AppsManagementConstants.NOTIFY_UPDATE_INFO_ID);

			Intent intent = new Intent(ICustomAction.ACTION_APP_MANAGEMENT);

			// 下载进度点击时，目前不做任何处理
			// intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_VIEW_KEY,
			// AppsManageView.APPS_UPDATE_VIEW_ID);

			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_START_TYPE_KEY,
					AppsManageView.APPS_START_TYPE);
			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY,
					MainViewGroup.ACCESS_FOR_UPDATE_NOTIFACTION);

			PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			PackageManager pkgMgr = mContext.getPackageManager();

			int size = 0;
			StringBuilder sb = new StringBuilder();
			for (AppBean appBean : appBeans) {
				if (!appBean.mIsIngore) {
					size++;
					sb.append(appBean.getAppName(pkgMgr)).append(", ");
				}
			}
			if (size > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				String updateInfo = size
						+ " "
						+ res.getString(R.string.apps_management_notification_title_update_info_suffix);
				Notification notification = new Notification(R.drawable.notification_update_icon,
						updateInfo, System.currentTimeMillis());
				notification.flags = Notification.FLAG_AUTO_CANCEL;

				notification.setLatestEventInfo(mContext, updateInfo, sb.toString(), pendingIntent);
				notificationManager.notify(AppsManagementConstants.NOTIFY_TAG,
						AppsManagementConstants.NOTIFY_UPDATE_INFO_ID, notification);
			}

		}
	}

	/**
	 * 保存应用更新是否提示开关信息
	 * 
	 * @param appIconControl
	 * @param gostoreControl
	 * @param appFuncMenuControl
	 */
	public void setmControlInfo(byte appIconControl, byte gostoreControl, byte appFuncMenuControl) {
		PreferencesManager preferences = new PreferencesManager(getContext(),
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		// 功能表图标右上角
		preferences.putInt(FunControler.APPICON_SHOW_MESSSAGE, appIconControl);
		// GO Store功能表图标右上角
		preferences.putInt(FunControler.GOSTORE_SHOW_MESSAGE, gostoreControl);
		// 功能表Menu菜单
		preferences.putInt(FunControler.APPFUNC_APPMENU_SHOW_MESSAGE, appFuncMenuControl);
		preferences.commit();
	}

	private void saveUpdateCount(int size) {
		// 保存现在可更新应用的个数
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		preferences.putInt(FunControler.GOSTORECOUNT, size);
		preferences.commit();
	}

	/**
	 * 获取应用更新数据，并把获取的状态和结果通知TabManageView
	 */
	private void getUpdateData() {
		if (mUpdateController == null) {
			mUpdateController = new AppsManageViewController(getContext(), mUpdateListener);
		}
		if (mUpdateState != AppsManageViewController.MSG_ID_START) {
			// 请求网络数据
			mUpdateController.sendRequest(AppsManageViewController.ACTION_START_REQUEST, null);
			mUpdateState = AppsManageViewController.MSG_ID_START;
		}
		// 通知TabManageView正在刷新更新数据
		mTabManageView.setUpdateData(null, mUpdateState, true);
	}

	/**
	 * 展示loading页，loading页至少展示1S； 如果时间在1S至3S范围内，loading页在获取完首页顶部Tab栏内容之后，消失；
	 * 如果加载顶部tab栏内容时间超过3S，loading页消失，直接展现应用中心内容；loading页消失采用渐变消失动画；
	 * 另外，如果取的是默认loading图，要加上文字，如果取的不是默认loading图，只显示图片
	 */
	private void showFrontCover() {
		mStartLoading = LoadingTipUtil.getDefaultStartLoading(getContext());
		if (mStartLoading != null) {
			// 展示启动loading页
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			this.addView(mStartLoading, lp);
			// 默认4秒后消失loading页
			postDelayed(mDismissLoadingRunnable, 4000);
		} else {
			mDismissLoadingRunnable = null;
		}
	}

	/**
	 * 应用游戏中心，每次启动都连网获取最新的热门搜索关键字，并保存在SD卡
	 */
	private void getHotSearchKeywords() {
		int ty = 1;
		SearchKeywordUtil.refreshHotSearchKeywords(getContext(), ty, null);
	}

	private void installApk(String filePath) {
		if (filePath != null && !"".equals(filePath)) {
			Message message = mHandler.obtainMessage(AppsManagementActivity.INSTALL_APP);
			message.obj = filePath;
			mHandler.sendMessage(message);
		}
	}

	private void uninstallApk(String packageName) {
		if (packageName != null && !"".equals(packageName)) {
			Message message = mHandler.obtainMessage(AppsManagementActivity.UNINSTALL_APP);
			message.obj = packageName;
			mHandler.sendMessage(message);
		}
	}

	public void onResume() {
		if (mTabManageView != null) {
			mTabManageView.onResume();
		}
		if (mAppMigrationManagerView != null) {
			mAppMigrationManagerView.onResume();
		}
		if (mPackageManagementView != null) {
			mPackageManagementView.onResume();
		}
	}

	public void onStop() {
		if (mTabManageView != null) {
			mTabManageView.onStop();
		}
	}

	public void onDestory() {
		unregisterAllReceiver();
		mTabManageView.recycle();
//		mTimeOutHandler = null;
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			mHasBindService = false;
		}
		if (mPackageManagementView != null) {
			mPackageManagementView.cleanup();
		}
	}

	/**
	 * 注册所有的监听器
	 */
	private void registerAllReceiver() {
		registerSDCardReceiver();
		registerInstallUninstallReceiver();
		registerDownloadReceiver();
		registerNetworkReceiver();
	}

	/**
	 * 取消注册所有的监听器
	 */
	private void unregisterAllReceiver() {
		try {
			unregisterSDCardReceiver();
			unregisterAppInstallReceiver();
			// unregisterGetDownloadListReceiver();
			unRegisterDownloadReceiver();
			unRegisterNetworkReceiver();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean onPrepareOptionMenu(AppGameMenu menu) {
		if (mTabManageView != null) {
			return mTabManageView.onPrepareOptionMenu(menu);
		}
		return false;
	}

	public boolean onOptionsItemSelected(int id) {
		if (mTabManageView != null) {
			return mTabManageView.onOptionsItemSelected(id);
		}
		return false;
	}

	@Override
	public int getId() {
		return IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME;
	}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId, final int param,
			final Object object, final List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPS_MANAGEMENT_INSTALL_APP :
				String filePath = (String) object;
				installApk(filePath);
				break;
			case IDiyMsgIds.APPS_MANAGEMENT_UNINSTALL_APP :
				String packageName = (String) object;
				uninstallApk(packageName);
				break;
			case IDiyMsgIds.SHOW_NO_PROMPT_UPDATE_VIEW :
				showNoUpdateView();
				break;
			case IDiyMsgIds.REMOVE_NO_PROMPT_UPDATE_VIEW :
				removeNoUpdateView();
				break;
			case IDiyMsgIds.SHOW_UNINSTALL_APP_VIEW :
				showUninstallAppView();
				break;
			case IDiyMsgIds.REMOVE_UNINSTALL_APP_VIEW :
				removeUninstallAppView();
				break;
			case IDiyMsgIds.SHOW_SEARCH_VIEW :
				showSearchView();
				break;
			case IDiyMsgIds.REMOVE_SEARCH_VIEW :
				removeSearchView();
				break;
			case IDiyMsgIds.SKIP_SIDE_TAB :
				if (object != null && object instanceof int[]) {
					int[] array = (int[]) object;
					if (array.length == 2) {
						GridTitleBar bar = mTabManageView.getGridTitleBar();
						if (bar != null) {
							bar.showSideTab(array[0], array[1]);
						}
					}
				}
				break;
			case IDiyMsgIds.TOPTAB_VIEW_LOAD_FINISH :
				// 如果runnable还没执行，启动loading页还在展示
				if (mDismissLoadingRunnable != null) {
					removeCallbacks(mDismissLoadingRunnable);
					post(mDismissLoadingRunnable);
				}
				// 注册所有的监听器
				mDHandler.sendEmptyMessage(DMESSAGE_REGISTER_ALLRECEIVER);
				// 启动下载服务
				mDHandler.sendEmptyMessage(DMESSAGE_START_DOWNLOAD_SERVICE);
				// 获取更新数据
				mDHandler.sendEmptyMessage(DMESSAGE_GET_UPDATEDATA);
				// 刷新热门搜索关键词
				// mDHandler.sendEmptyMessage(DMESSAGE_UPDATE_EXTRA_DATA);
				// 上传收集的网络数据
				mDHandler.sendEmptyMessage(DMESSAGE_SEND_NETLOG);
				break;
			case IDiyMsgIds.REFRESH_UPDATE_DATA :
				// 获取更新数据
				getUpdateData();
				break;
			case IDiyMsgIds.REFRESH_WHEN_NETWORK_OK :
				mNeedToRefreshWhenNetworkOK = true;
				break;
			case IDiyMsgIds.SHOW_PREVLOAD_PROGRESS :
				if (mTabManageView != null) {
					mTabManageView.showPrevLoadProgress();
				}
				break;
			case IDiyMsgIds.HIDE_PREVLOAD_PROGRESS :
				if (mTabManageView != null) {
					mTabManageView.hidePrevLoadProgress();
				}
				break;
			case IDiyMsgIds.SHOW_APP_MIGRATION_VIEW :
				showAppMigrationView();
				break;
			case IDiyMsgIds.REMOVE_APP_MIGRATION_VIEW :
				removeAppMigrationView();
				break;
			case IDiyMsgIds.REFRESH_TOPTAB_DATA :
				mDHandler.sendMessage(DMESSAGE_REFRESH_TOPTAB_DATA, 0, 0, object);
				break;
			case IDiyMsgIds.SHOW_PACKAGE_MANAGEMENT_VIEW :
				showPackageManagementView();
				break;
			case IDiyMsgIds.REMOVE_PACKAGE_MANAGEMENT_VIEW :
				removePackageManagementView();
				break;
			case IDiyMsgIds.SHOW_SEARCH_BUTTON:
				// 搜索按钮，参数为１时显示，参数为０时隐藏
				if (param == 1) {
					setSearchButtonVisbility(View.VISIBLE);
				} else {
					setSearchButtonVisbility(View.GONE);
				}
				break;
			case IDiyMsgIds.LOAD_MAIN_VIEW:
				// 隐蔽加载界面
				post(mDismissLoadingRunnable);
				//　启动加载首页
                TabController.skipToTheNextTab(ClassificationDataBean.TOP_TYPEID, "", mEntranceId,
                		true, -1, -1, null);
				break;
			case IDiyMsgIds.SAVE_TAB_CLICK:
				// 统计tab点击
				mDHandler.sendMessage(DMESSAGE_SAVE_TAB_CLICK, param, Integer.MIN_VALUE, null);
				break;
			default :
				break;
		}
		return false;
	}

	/**
	 * 显示忽略更新页面
	 */
	private void showNoUpdateView() {
		if (mAppNoUpdateContainer == null) {
			mAppNoUpdateContainer = new AppsNoUpdateViewContainer(mContext);
			mAppNoUpdateContainer.setClickable(true);
			mAppNoUpdateContainer.setmHandler(mHandler);
			this.addView(mAppNoUpdateContainer, new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT));
			mAppNoUpdateContainer.setmButtonClick(new OnButtonClick() {
				@Override
				public void click(String packageName, int position) {
					// mAppsManageView.getUpdateAppsContainer().promptUpdate(packageName,position);
				}
			});
		}
		// ArrayList<AppBean> appBeanList =
		// mAppsManageView.getUpdateAppsContainer().getAppBeanList();
		ArrayList<AppBean> appBeanList = null;
		mAppNoUpdateContainer.setmAppBeanList(appBeanList);
	}

	private void removeNoUpdateView() {
		if (mAppNoUpdateContainer != null) {
			this.removeView(mAppNoUpdateContainer);
			mAppNoUpdateContainer.clean();
			mAppNoUpdateContainer = null;
		}
		// mAppsManageView.getUpdateAppsContainer().updateList();
	}

	private void showSearchView() {
		if (mSearchView != null) {
			removeSearchView();
		}
		mSearchView = (AppsManagementSearchView) mLayoutInflater.inflate(
				R.layout.apps_management_search, null);
		mSearchView.setAccess(mEntranceId);
		// 填充搜索关键字
		this.addView(mSearchView, new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		mSearchView.setClickable(true);
		mSearchView.setBackClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppsManagementActivity.sendHandler(getContext(),
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.REMOVE_SEARCH_VIEW, 0, null, null);
			}
		});
		mSearchView.setVisibility(View.VISIBLE);
//		// 展示热门搜索关键字
//		mSearchView.showHotSearchKeyword();
		// 使输入框获取焦点
		EditText et = (EditText) mSearchView.findViewById(R.id.apps_management_search_edt);
		// 由二级TAB进入搜索，直接显示输入框
		mSearchView.showIM(true);
		if (!et.isFocused()) {
			OnFocusChangeListener listener = et.getOnFocusChangeListener();
			et.setOnFocusChangeListener(null);
			et.requestFocus();
			et.setOnFocusChangeListener(listener);
		}
	}

	private void removeSearchView() {
		if (mSearchView != null) {
			mSearchView.showIM(false);
			this.removeView(mSearchView);
			mSearchView = null;
		}
	}
	
	private void setSearchButtonVisbility(int visibility) {
		if (mSearchButton != null) {
			mSearchButton.setVisibility(visibility);
		}
	}

	private void showUninstallAppView() {
		if (mIsOpenUninstallView) {
			return;
		}
		mIsOpenUninstallView = true;
		if (mAppsuninstallview == null) {
			mAppsuninstallview = new AppsUninstallView(mContext);
		}
		mAppsuninstallview.setClickable(true);
		this.addView(mAppsuninstallview, new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		mAppsuninstallview.setFocusable(true);
		mAppsuninstallview.requestFocus();
	}

	private void removeUninstallAppView() {
		if (mAppsuninstallview != null) {
			this.removeView(mAppsuninstallview);
			mAppsuninstallview.cleanup();
			mAppsuninstallview = null;
		}
		mIsOpenUninstallView = false;
	}

	private void showAppMigrationView() {
		if (mAppMigrationManagerView != null) {
			removeAppMigrationView();
		}
		if (mAppMigrationManagerView == null) {
			mAppMigrationManagerView = new AppMigrationManagerView(mContext);
			mAppMigrationManagerView.setOnBackClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AppsManagementActivity.sendHandler(getContext(),
							IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
							IDiyMsgIds.REMOVE_APP_MIGRATION_VIEW, -1, null, null);
				}
			});
		}
		this.addView(mAppMigrationManagerView, new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		mAppMigrationManagerView.setClickable(true);
		mAppMigrationManagerView.setFocusable(true);
		mAppMigrationManagerView.setFocusableInTouchMode(true);
		mAppMigrationManagerView.requestFocus();
	}

	private void removeAppMigrationView() {
		if (mAppMigrationManagerView != null) {
			this.removeView(mAppMigrationManagerView);
			mAppMigrationManagerView.cleanup();
			mAppMigrationManagerView = null;
		}
	}

	private void showPackageManagementView() {
		if (mPackageManagementView == null) {
			mPackageManagementView = (PackageManagementView) LayoutInflater.from(mContext).inflate(
					R.layout.apps_management_packagemanagement_layout, null);
		}
		ViewGroup viewParent = (ViewGroup) mPackageManagementView.getParent();
		if (viewParent != null) {
			viewParent.bringChildToFront(mPackageManagementView);
		} else {
			this.addView(mPackageManagementView, new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT));
			mPackageManagementView.setClickable(true);
			mPackageManagementView.setFocusable(true);
			mPackageManagementView.setFocusableInTouchMode(true);
			mPackageManagementView.requestFocus();
		}
	}

	private void removePackageManagementView() {
		if (mPackageManagementView != null) {
			this.removeView(mPackageManagementView);
			mPackageManagementView = null;
		}
	}

	/**
	 * 注册SD卡事件
	 * 
	 * @param context
	 */
	private void registerSDCardReceiver() {
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		intentFilter.addDataScheme("file");
		mContext.registerReceiver(mSDCardListener, intentFilter);
	}

	/**
	 * 注册应用安装卸载事件
	 * 
	 * @param context
	 */
	private void registerInstallUninstallReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		mContext.registerReceiver(mAppInstallListener, intentFilter);
	}

	/**
	 * 注册下载状态的事件
	 */
	private void registerDownloadReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_APP_DOWNLOAD);
		mContext.registerReceiver(mDownloadReceiver, intentFilter);
	}

	/**
	 * 注册网络状态广播接收器
	 */
	private void registerNetworkReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mNetworkListener, filter);
	}

	/**
	 * 去除SD卡事件监听
	 * 
	 * @param context
	 */
	private void unregisterSDCardReceiver() {
		mContext.unregisterReceiver(mSDCardListener);
	}

	/**
	 * 去除应用安装卸载监听器
	 * 
	 * @param context
	 */
	private void unregisterAppInstallReceiver() {
		mContext.unregisterReceiver(mAppInstallListener);
	}

	/**
	 * 取消注册下载状态的监听器
	 */
	private void unRegisterDownloadReceiver() {
		mContext.unregisterReceiver(mDownloadReceiver);
	}

	/**
	 * 取消注册网络广播接收器
	 */
	private void unRegisterNetworkReceiver() {
		mContext.unregisterReceiver(mNetworkListener);
	}

	/**
	 * 设置handler，用来在各层之间传递信息
	 * 
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		mHandler = handler;
		if (mTabManageView != null) {
			mTabManageView.setHandler(mHandler);
		}
	}

	/**
	 * 通知当前页面省流量模式发生改变
	 */
	public void onTrafficSavingModeChange() {
		if (mTabManageView != null) {
			mTabManageView.onTrafficSavingModeChange();
		}
	}

	/**
	 * <br>
	 * 功能简述:进入应用中心之后，假如有“已暂停”的任务，就在通知栏提示用户 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param list
	 */
	public void showNotification(ArrayList<DownloadTask> list) {
		int count = 0;
		for (int i = 0; i < list.size(); i++) {
			DownloadTask task = list.get(i);
			if (task.getState() == DownloadTask.STATE_STOP) {
				count++;
			}
		}
		if (count == 0) {
			return;
		}
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClass(mContext, AppsDownloadActivity.class);
		intent.putExtra(AppsDownloadActivity.QUITSTYLE, AppsDownloadActivity.QUIT_TO_OLD_ACTIVITY);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		String tickerText = count
				+ mContext.getString(R.string.app_center_uncomplete_download_notification);
		Notification notification = new Notification(
				R.drawable.notification_download_uncomplete_icon, tickerText,
				System.currentTimeMillis());
		notification.setLatestEventInfo(mContext, tickerText,
				mContext.getString(R.string.app_center_uncomplete_download_text), pendingIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(WifiTimerTask.TAG, WifiTimerTask.NOTIFICATIONID, notification);
	}
}