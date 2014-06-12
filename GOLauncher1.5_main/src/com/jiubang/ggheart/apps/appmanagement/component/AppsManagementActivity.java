package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.go.util.graphics.DrawUtils;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.appmanagement.component.AppsNoUpdateViewContainer.OnButtonClick;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager;
import com.jiubang.ggheart.apps.appmanagement.help.AppsManagementConstants;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>类描述: 应用管理的界面
 * <br>功能详细描述:
 * 
 * @author  zhoujun
 * @date  [2012-9-18]
 */
public class AppsManagementActivity extends Activity {

	public final static int NONE = 0;
	public final static int INSTALL_APPS = 1;
	public final static int UNINSTALL_APPS = 2;
	public final static int SDCARD_IS_OK = 3;
	public final static int APP_LIST_UPDATE = 4;
	public final static int BACK_TO_GOSTORE = 5;
	public final static int SHOW_APP_DETAILS = 6;

	public final static int ROOT_NOT_SELECT = 0; // 用户未选择是否使用ROOT权限
	public final static int ROOT_ALLOW = 1; // 用户允许使用ROOT权限
	public final static int ROOT_NOT_ALLOW = 2; // 用户不允许使用ROOT权限
	public static int sIsAllowRoot = ROOT_NOT_SELECT;

	// public final static int START_GOTASKMANAGER = 7;
	/**
	 * handler消息：点击忽略更新按钮
	 */
	public final static int NO_UPDATE_BUTTON_CLICK = 7;
	/**
	 * handler消息：点击恢复忽略更新按钮
	 */
	public final static int PROMPT_UPDATE_BUTTON_CLICK = 8;

	public final static int GO_TO_APPS_UNINSTALL = 9;

	public final static int BACK_TO_MAINVIEW = 10;

	/**
	 * handler arg1：显示忽略更新页面
	 */
	public final static int MESSAGE_SHOW_NO_UPDATE_VIEW = 0;

	/**
	 * handler arg1： 移除忽略更新页面
	 */
	public final static int MESSAGE_REMOVE_NO_UPDATE_VIEW = 1;

	/**
	 * 主viewGroup，包含应用管理界面、忽略更新界面或以后扩展的其他界面
	 */
	private FrameLayout mMainViewGroup;

	/**
	 * 应用管理界面，包含我的应用，应用更新和应用推荐界面
	 */
	private AppsManageView mAppsManageView;
	private AppsUninstallView mAppsuninstallview;

	/**
	 * 是否在打开批量卸载页面,防止重复点击
	 */
	private boolean mIsOpenUninstallView = false;

	/**
	 * 忽略更新页面
	 */
	private AppsNoUpdateViewContainer mAppNoUpdateContainer;

	/**
	 * 所有要安装的apk路径
	 */
	private ArrayList<String[]> mInstallFileList = new ArrayList<String[]>();
	private ArrayList<String> mUninstallAppList = new ArrayList<String>();
	/**
	 * 当前安装app的位置
	 */
	private int mPosition = -1;
	/**
	 * 当前卸载app的位置
	 */
	private int mCurrUninstallPosition = -1;
	/**
	 * 当前是否有正在安装的应用
	 */
	private boolean mIsIntalling = false;
	/**
	 * 当前是否有正在卸载的应用
	 */
	private boolean mIsUnintalling = false;

	/**
	 * 是否已经绑定下载服务的标记
	 */
	private boolean mHasBindService = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AppCore.getInstance() == null) {
			// 此处是为了解决更新已应用到桌面上的GoWidget后会导致桌面重启，这时AppCore可能未被初始化，导致一些资源加载会出问题。
			// 暂时解决方案：强制关闭此Activity，用户需要重新打开。后续版本会修改这个问题
			this.finish();
		}
		getApplicationManager().setHandler(mHandler);
		Intent intent = getIntent();
		int entranceId = intent.getIntExtra(AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY,
				-1);
		int showViewId = intent.getIntExtra(AppsManagementConstants.APPS_MANAGEMENT_VIEW_KEY, 0);
		int startType = intent.getIntExtra(AppsManagementConstants.APPS_MANAGEMENT_START_TYPE_KEY,
				0);
		if (startType == AppsManageView.APPS_START_TYPE) {
			// 从Notifaction跳转过来，进行统计
			// 从通知栏更新提示进入
			AppManagementStatisticsUtil.getInstance().saveCurrentEnter(this,
					AppManagementStatisticsUtil.ENTRY_TYPE_NOTICE);
			StatisticsData.saveEntryCount(this, StatisticsData.ENTRY_APPS,
					AppManagementStatisticsUtil.ENTRY_TYPE_NOTICE,
					AppRecommendedStatisticsUtil.NEW_VERSION_ID);
		}
		// 统计:保存推荐界面入口 为1
		AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(this,
				AppRecommendedStatisticsUtil.UIENTRY_TYPE_LIST);
		mMainViewGroup = new FrameLayout(this);
		mAppsManageView = new AppsManageView(this, entranceId, showViewId);
		mAppsManageView.setHandler(mHandler);
		// setContentView(mAppsManageView);
		mMainViewGroup.addView(mAppsManageView, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		setContentView(mMainViewGroup);

		GOLauncherApp.getContext().startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
		// 再bind服务
		if (!mHasBindService) {
			mHasBindService = GOLauncherApp.getContext().bindService(
					new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), mConnenction,
					Context.BIND_AUTO_CREATE);
		}
	}

	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IDownloadService mDownloadController = IDownloadService.Stub.asInterface(service);
			try {
				if (mDownloadController != null) {
					mDownloadController.addRunningActivityClassName(AppsManagementActivity.class
							.getName());
					// 因为详情界面属于桌面进程
					// 而下载时候用的是GoLauncherApp里的DownloadController
					// 所以这里需要赋值
					GOLauncherApp.getApplication().setDownloadController(mDownloadController);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			GOLauncherApp.getApplication().setDownloadController(null);
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case INSTALL_APPS :
					// String packageName = (String) msg.obj;
					// Log.d("mHandler ",
					// "mHandler install app is rnuning packageName:");
					// + packageName);
					// if (ApplicationManager.GO_TASKMANAGE_PACKAGENAME
					// .equals(packageName)) {
					// Log.d("mHandler ", "start taskManage service");
					// Message message = this.obtainMessage();
					// message.what = START_GOTASKMANAGER;
					// this.sendMessageDelayed(message, 5000);
					// Log.d("mHandler", "sleep 5000 start service");
					// }
					// mAppsManageView.getUpdateAppsContainer().updateList();
					// mAppsManageView.updateMyAppsList();
					mAppsManageView.refreshData((String) msg.obj, true);

					// 安装或卸载后，忽略更新列表可能已经改变，要记得刷新一遍
					if (mAppNoUpdateContainer != null) {
						mAppNoUpdateContainer.refreshView();
					}
					break;
				case UNINSTALL_APPS :
					// mAppsManageView.getMyAppsContainer().updateList();
					// mAppsManageView.getUpdateAppsContainer().updateList();
					mAppsManageView.refreshData((String) msg.obj, false);
					if (mAppNoUpdateContainer != null) {
						mAppNoUpdateContainer.refreshView();
					}
					if (mAppsuninstallview != null) {
						mAppsuninstallview.updateList();
					}
					break;
				// case SDCARD_IS_OK:
				// mAppsManageView.getUpdateAppsContainer().updateList();
				// mAppsManageView.updateMyAppsList();
				// break;
				case APP_LIST_UPDATE :
					// 后台扫描服务完成后，会通知应用管理更新数据，这里好像没有必要
					// mAppsManageView.getUpdateAppsContainer().requestData();
					break;
				case BACK_TO_GOSTORE : {
//					Intent intent = new Intent();
//					intent.setClass(AppsManagementActivity.this, GoStore.class);
//					AppsManagementActivity.this.startActivity(intent);

					finish();
				}
					break;
				case SHOW_APP_DETAILS : {
					String pkg = (String) msg.obj;
					showAppDetails(pkg);
				}
					break;
				case NO_UPDATE_BUTTON_CLICK :
					if (msg.arg1 == MESSAGE_SHOW_NO_UPDATE_VIEW) {
						ArrayList<AppBean> appBeanList = mAppsManageView.getUpdateAppsContainer()
								.getAppBeanList();
						if (mAppNoUpdateContainer == null) {
							mAppNoUpdateContainer = new AppsNoUpdateViewContainer(
									AppsManagementActivity.this);
							mAppNoUpdateContainer.setClickable(true);
							mAppNoUpdateContainer.setmHandler(mHandler);
							mMainViewGroup.addView(mAppNoUpdateContainer, new LayoutParams(
									LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
							mAppNoUpdateContainer.setmButtonClick(new OnButtonClick() {
								@Override
								public void click(String packageName, int position) {
									mAppsManageView.getUpdateAppsContainer().promptUpdate(
											packageName, position);
								}
							});
						}
						mAppNoUpdateContainer.setmAppBeanList(appBeanList);
					} else {
						if (mAppNoUpdateContainer != null) {
							mMainViewGroup.removeView(mAppNoUpdateContainer);
							mAppNoUpdateContainer.clean();
							mAppNoUpdateContainer = null;
						}
						mAppsManageView.getUpdateAppsContainer().updateList();
					}
					break;
				case PROMPT_UPDATE_BUTTON_CLICK : {
					String pkg = (String) msg.obj;
					int position = msg.arg1;
					mAppsManageView.getUpdateAppsContainer().promptUpdate(pkg, position);
				}
					break;
				// case START_GOTASKMANAGER: {
				// Log.d("mHandler", "sleep 5000 start service");
				// AppsManagementActivity.this.sendBroadcast(new Intent(
				// AppsManageView.APPMANAGEMENT_ACTION_START_TASKMANAGER));
				// }
				// break;

				case GO_TO_APPS_UNINSTALL :
					gotoAppsUninstall();
					break;
				case BACK_TO_MAINVIEW :
					if (mAppsuninstallview != null) {
						mMainViewGroup.removeView(mAppsuninstallview);
						mAppsuninstallview.cleanup();
						mAppsuninstallview = null;
						mIsOpenUninstallView = false;
					}
					//					setContentView(mMainViewGroup);
					break;
				default :
					break;
			}
		}
	};

	private void showAppDetails(String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return;
		}
		final String sSCHEME = "package";
		/**
		 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
		 */
		final String mAppPkgName21 = "com.android.settings.ApplicationPkgName";
		/**
		 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
		 */
		final String mAppPkgName22 = "pkg";
		/**
		 * InstalledAppDetails所在包名
		 */
		final String mAppDetailPkgName = "com.android.settings";
		/**
		 * InstalledAppDetails类名
		 */
		final String mAppDetailClassName = "com.android.settings.InstalledAppDetails";

		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) {
			// 2.3（ApiLevel 9）以上，使用SDK提供的接口
			intent.setAction(ICustomAction.ACTION_SETTINGS);
			Uri uri = Uri.fromParts(sSCHEME, packageName, null);
			intent.setData(uri);
		} else {
			// 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
			// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			final String appPkgName = apiLevel == 8 ? mAppPkgName22 : mAppPkgName21;
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(mAppDetailPkgName, mAppDetailClassName);
			intent.putExtra(appPkgName, packageName);
		}

		try {
			this.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// Log.d("appsmanagementActivity", "onRestart");
		// mAppsManageView.sendBrocastReceicerForAppSize();
		mAppsManageView.updateMyAppsList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAppsManageView.unregisterReceiver(this);
		mAppsManageView.recycle();
		getApplicationManager().setHandler(null);

		// 解除绑定下载服务
		if (mHasBindService) {
			GOLauncherApp.getApplication().setDownloadController(null);
			GOLauncherApp.getContext().unbindService(mConnenction);
			mHasBindService = false;
		}
	};

	public static ApplicationManager getApplicationManager() {
		ApplicationManager applicationManager = null;
		if (AppCore.getInstance() == null) {
			applicationManager = ApplicationManager.getInstance(GOLauncherApp.getContext());
		} else {
			applicationManager = AppCore.getInstance().getApplicationManager();
		}
		return applicationManager;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Log.d("AppManagementActivity",
		// "requestCode:"+requestCode+" resultCode : "+resultCode+" data :"+data);
		if (requestCode == 1) {
			mIsIntalling = false;
			installApp();
		} else if (requestCode == 2) {
			mIsUnintalling = false;
			uninstallApp();
		}

	}

	/**
	 * 将apk添加到安装列表中
	 */
	public void addInstallApp(String filePath, String packageName) {
		synchronized (mInstallFileList) {
			mInstallFileList.add(new String[] { filePath, packageName });
			if (mPosition < mInstallFileList.size() - 1) {
				if (!mIsIntalling) {
					installApp();
				}
			}
		}
	}

	/**
	 * 安装apk
	 */
	private void installApp() {
		synchronized (mInstallFileList) {
			if (mPosition < mInstallFileList.size() - 1) {
				mPosition = mPosition + 1;
				String[] appInfo = mInstallFileList.get(mPosition);
				installApp(appInfo[0]);
				// currPackageName = appInfo[1];
				mIsIntalling = true;
				// Log.d("AppManagementActivity",
				// " installApp : "+currPackageName);
			}
		}
	}

	public void installApp(String filePath) {
		//		File file = new File(filePath);
		//		Intent intent = new Intent();
		//		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//		intent.setAction(android.content.Intent.ACTION_VIEW);
		//		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		//		this.startActivityForResult(intent, 1);
		ApkInstallUtils.installApk(filePath);
	}

	/**
	 * 将app添加到卸载列表中
	 */
	public void addUninstallApp(String packageName) {
		synchronized (mUninstallAppList) {
			mUninstallAppList.add(packageName);
			if (mCurrUninstallPosition < mUninstallAppList.size() - 1) {
				if (!mIsUnintalling) {
					uninstallApp();
				}
			}
		}
	}

	/**
	 * 安装apk
	 */
	private void uninstallApp() {
		synchronized (mUninstallAppList) {
			if (mCurrUninstallPosition < mUninstallAppList.size() - 1) {
				mCurrUninstallPosition = mCurrUninstallPosition + 1;
				String packageName = mUninstallAppList.get(mCurrUninstallPosition);
				uninstallApp(packageName);
				mIsUnintalling = true;
			}
		}
	}

	public void uninstallApp(String packageName) {
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		// uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivityForResult(uninstallIntent, 2);
	}

	/**
	 * 使用批量卸载功能
	 */
	// private void GotoAppsUninstall(){
	// if(appsuninstallview == null)
	// {
	// appsuninstallview = new AppsUninstallView(AppsManagementActivity.this);
	// appsuninstallview.setHandler(mHandler);
	// }
	//
	//
	// //判断当前手机是否为“已 ROOT”
	// //对于“未 ROOT/ROOT 出现问题“的用户，直接进入使用非ROOT卸载
	// if(!CommandManager.IsRoot()){
	// mIsAllowRoot = ROOT_NOT_ALLOW;
	// }
	// // else{
	// // CommandManager.getInstance();
	// // if(!CommandManager.isCreatRootProcess()){
	// // mIsAllowRoot = ROOT_NOT_ALLOW;
	// // }
	// // }
	//
	// //用户未选择是否使用ROOT时
	// if(mIsAllowRoot == ROOT_NOT_SELECT){
	// new
	// AlertDialog.Builder(AppsManagementActivity.this).setTitle(AppsManagementActivity.this.getString(R.string.appsuninstall_root_alert_title))
	// .setMessage(AppsManagementActivity.this.getString(R.string.appsuninstall_root_alert_text))
	// .setPositiveButton(AppsManagementActivity.this.getString(R.string.ok),
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	// // 确定按钮事件
	// if(CommandManager.getInstance().getRoot()){
	// appsuninstallview.setRootStatus(true);
	// appsuninstallview.updateList();
	// setContentView(appsuninstallview);
	// mIsAllowRoot = ROOT_ALLOW;
	// }else{
	// Toast.makeText(AppsManagementActivity.this,
	// AppsManagementActivity.this.getString(R.string.getroot_failed),
	// Toast.LENGTH_LONG).show();
	// appsuninstallview.setRootStatus(false);
	// appsuninstallview.updateList();
	// setContentView(appsuninstallview);
	// mIsAllowRoot = ROOT_NOT_ALLOW;
	// }
	//
	//
	// }
	// }).setNegativeButton(AppsManagementActivity.this.getString(R.string.cancle),
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int which) {
	// appsuninstallview.setRootStatus(false);
	// appsuninstallview.updateList();
	// setContentView(appsuninstallview);
	// mIsAllowRoot = ROOT_NOT_ALLOW;
	// }
	// }).show();
	// }else{
	// if(mIsAllowRoot == ROOT_ALLOW){
	// appsuninstallview.setRootStatus(true);
	// }else{
	// appsuninstallview.setRootStatus(false);
	// }
	// appsuninstallview.updateList();
	// setContentView(appsuninstallview);
	// }
	// }

	private void gotoAppsUninstall() {
		if (mIsOpenUninstallView) {
			return;
		}
		mIsOpenUninstallView = true;

		if (mAppsuninstallview == null) {
			mAppsuninstallview = new AppsUninstallView(AppsManagementActivity.this);
			mAppsuninstallview.setHandler(mHandler);
		}
		// appsuninstallview.updateList();
		mAppsuninstallview.setClickable(true);
		mAppsuninstallview.setFocusable(true);
		mAppsuninstallview.requestFocus();
		mMainViewGroup.addView(mAppsuninstallview, new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		DrawUtils.resetDensity(this);
		if (mAppNoUpdateContainer != null) {
			mAppNoUpdateContainer.refreshView();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mAppNoUpdateContainer != null) {
				Message message = mHandler
						.obtainMessage(AppsManagementActivity.NO_UPDATE_BUTTON_CLICK);
				message.arg1 = AppsManagementActivity.MESSAGE_REMOVE_NO_UPDATE_VIEW;
				mHandler.sendMessage(message);
				return true;
			}
			if (mAppsuninstallview != null) {
				Message msg = new Message();
				msg.what = AppsManagementActivity.BACK_TO_MAINVIEW;
				mHandler.sendMessage(msg);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
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
}
