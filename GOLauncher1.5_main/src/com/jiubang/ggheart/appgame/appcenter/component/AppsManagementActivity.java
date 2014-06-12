package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.MessageManager;
import com.jiubang.ggheart.appgame.appcenter.contorler.ApplicationManager;
import com.jiubang.ggheart.appgame.appcenter.help.AppsManagementConstants;
import com.jiubang.ggheart.appgame.base.bean.TabDataGroup;
import com.jiubang.ggheart.appgame.base.component.IMenuHandler;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.component.MoreAppsActivity;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.downloadmanager.AppsDownloadActivity;
import com.jiubang.ggheart.appgame.base.feedback.AppFeedbackActivity;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.LruImageCache;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.setting.AppGameSettingActivity;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.appgame.base.utils.AppGameConfigUtils;
import com.jiubang.ggheart.apps.config.GOLauncherConfig;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.ThreadPoolManager;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 类描述:应用中心主Activity
 */
//TODO:XIEDEZHI 修改Activity名字为AppCenterActivity
public class AppsManagementActivity extends Activity {
	private final boolean mIsDebug = false;
	private static boolean sHasPutIcon = false;
	private static AppsManagementActivity sContext = null;
	private MessageManager mMessageManager = null;

	public final static int NONE = 0;
	/**
	 * 结束activity
	 */
	public final static int FINISH_ACTIVITY = 1;

	/**
	 * handler 消息：下载完成，调用系统安装
	 */
	public final static int INSTALL_APP = 11;
	/**
	 * handler 消息：卸载应用
	 */
	public final static int UNINSTALL_APP = 12;
	
	/**
	 * handler 消息：统计入口
	 */
	private final static int STATISTIC_ENTRACE = 13;
	/**
	 * 主页面，包含应用管理界面、忽略更新界面或以后扩展的其他界面
	 */
	private MainViewGroup mMainViewGroup;

	/**
	 * 所有要安装的apk路径
	 */
	private ArrayList<String> mInstallFileList = new ArrayList<String>();
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
	 * 请求系统安装应用的界面
	 */
	private static final int REQUEST_CODE_FOR_INSTALL = 1;
	/**
	 * 请求系统卸载应用的界面
	 */
	private static final int REQUEST_CODE_FOR_UNINSTALL = 2;
	/**
	 * 由于设置界面与应用中心不在同一个进程 因此利用OnActivityForResult启动设置界面,返回时应用中心再对设置数据进行初始化
	 */
	private static final int REQUEST_CODE_FOR_SETTING = 3;
	/**
	 * 自定义菜单，截获onkeydown和onkeyup的menu事件进行显示
	 */
	private AppGameMenu mMenu = null;
	/**
	 * 是否显示menu，判断按钮消息是否经历过onkeydown和onkeyup
	 */
	private boolean mNeedShowMenu = false;
	/**
	 * 保存返回键点击时间的KEY值
	 */
	private static final String BACK_PRESS_KEY = "APP_BACK_PRESS_KEY";
	/**
	 * 双击返回键退出应用中心的双击间隔为5秒
	 */
	private static final int EXIT_INTERVAL = 5000;
	/**
	 * 应用中心添加桌面快捷方式aciton
	 */
	public static final String GOMARKET_ICON_ATICON = "com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity";
	
	/**
	 * 注册消息接收者
	 * 
	 * @param handler
	 * @return
	 */
	public static boolean registMsgHandler(final IMessageHandler handler) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.registMsgHandler(handler);
		}
		return false;
	}

	/**
	 * 点对点发送到UI线程上的消息
	 * 
	 * @param who
	 *            发送者指针
	 * @param handlerId
	 *            接收者id
	 * @param msgId
	 *            消息id
	 * @param param
	 * @param object
	 * @param objects
	 */
	public static void sendHandler(Object who, int handlerId, int msgId,
			int param, Object object, List<? extends Object> objects) {
		if (sContext != null && sContext.mMessageManager != null) {
			sContext.mMessageManager.sendHandler(who, handlerId, msgId, param,
					object, objects);
		}
	}

	/**
	 * 同步广播消息
	 * 
	 * @param type
	 *            消息类型
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列 return 是否处理
	 */
	public static boolean sendMessage(Object who, int handlerId, int msgId,
			int param, Object object, List<?> objList) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.send(who, handlerId, msgId, param,
					object, objList);
		}
		return false;
	}

	/**
	 * 反注册消息接收者 与{@link GoLauncher#registMsgHandler(IMessageHandler, int)}配对使用
	 * 
	 * @param handler
	 * @param msgId
	 * @return
	 */
	public static boolean unRegistMsgHandler(IMessageHandler handler) {
		if (sContext != null && sContext.mMessageManager != null) {
			return sContext.mMessageManager.unRegistMsgHandler(handler);
		}
		return false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 判断是不是要跳到应用更新
		if (intent != null) {
			if (MainViewGroup.ACCESS_FOR_UPDATE_NOTIFACTION == intent
					.getIntExtra(
							AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY,
							-1)) {
				TabController.skipToUpdateContainer();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sContext = this;
		mMessageManager = new MessageManager();

		// 根据SIM卡初始化语言配置信息
		AppGameConfigUtils.updateResourcesLocaleBySim(this, super.getResources());

		// 加载系统渠道配置信息
		GOLauncherConfig.getInstance(this).roadConfig();
		
		//　TODO 木瓜sdk
		// 初始化公共类，保存公共Activity
//		GoMarketPublicUtil.getInstance(sContext);

		// 初始化图片管理器
		final int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE))
				.getMemoryClass();
		// Use 1/8th of the available memory for this memory cache. 
		final int cacheSize = 1024 * 1024 * memClass / 8;
		AsyncImageManager.buildInstance(new LruImageCache(cacheSize));
		
		Intent intent = getIntent();
		int entranceId = intent.getIntExtra(
				AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY, -1);
		boolean showFrontCover = intent.getBooleanExtra(
				AppsManagementConstants.APPS_MANAGEMENT_SHOW_FRONTCOVER, false);
		// int showViewId = intent.getIntExtra(
		// AppsManagementConstants.APPS_MANAGEMENT_VIEW_KEY, 0);
		int startType = intent.getIntExtra(
				AppsManagementConstants.APPS_MANAGEMENT_START_TYPE_KEY, 0);

		mMainViewGroup = new MainViewGroup(this, entranceId, showFrontCover);
		mMainViewGroup.setHandler(mHandler);
		setContentView(mMainViewGroup);
		if (mIsDebug) {
			Log.e("zj", "onCreate is running---");
		}
		
		// 统计:保存推荐界面入口 为1
		Message msg = new Message();
		msg.what = STATISTIC_ENTRACE;
		msg.arg1 = startType;
		mHandler.sendMessageDelayed(msg, 5000);
		
		// Home键跳转标识
		// 因为应用中心进程和桌面进程不一样，下面这句代码不会有执行效果，暂时注释掉  fixed by XIEDEZHI 2013.01.14
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 0, null, null);
		
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FINISH_ACTIVITY:
				AppsManagementActivity.this.finish();
				break;
			case INSTALL_APP:
				String filePath = (String) msg.obj;
				addInstallApp(filePath);
				break;
			case UNINSTALL_APP:
				String packageName = (String) msg.obj;
				addUninstallApp(packageName);
				break;
			case STATISTIC_ENTRACE:
				int startType = msg.arg1;
				if (startType == AppsManageView.APPS_START_TYPE) {
					// 从Notifaction跳转过来，进行统计
					// 从通知栏更新提示进入
					AppManagementStatisticsUtil.getInstance().saveCurrentEnter(
							AppsManagementActivity.this,
							AppManagementStatisticsUtil.ENTRY_TYPE_NOTICE);
					StatisticsData.saveEntryCount(AppsManagementActivity.this,
							StatisticsData.ENTRY_APPS,
							AppManagementStatisticsUtil.ENTRY_TYPE_NOTICE,
							AppRecommendedStatisticsUtil.NEW_VERSION_ID);
				}
				// 统计:保存推荐界面入口 为1
				AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(
						AppsManagementActivity.this,
						AppRecommendedStatisticsUtil.UIENTRY_TYPE_LIST);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		mMainViewGroup.onResume();
		if (mIsDebug) {
			Log.e("zj", "onResume is running---");
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mMainViewGroup.onStop();
		if (mIsDebug) {
			Log.e("zj", "onStop is running---");
		}
	}

	@Override
	protected void onDestroy() {
		if (mIsDebug) {
			Log.e("zj", "onDestroy is running---");
		}
		super.onDestroy();
		mMainViewGroup.onDestory();
		mMessageManager.cleanup();
		
		//　TODO 木瓜sdk
//		AppFlood.destroy();
		//　销毁公共类
//		GoMarketPublicUtil.getInstance(sContext).destroy();
		// 销毁线程池
		ThreadPoolManager.destory();
		//取消通知栏提示
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(AppsManagementConstants.NOTIFY_TAG,
				AppsManagementConstants.NOTIFY_UPDATE_INFO_ID);
		// 把进程杀死
		android.os.Process.killProcess(android.os.Process.myPid());
	};

	public static ApplicationManager getApplicationManager() {
		ApplicationManager applicationManager = null;
		// applicationManager =
		// ApplicationManager.getInstance(getApplicationContext());
		applicationManager = ApplicationManager.getInstance(sContext);
		return applicationManager;
	}

	/**
	 * 进入应用中心
	 * 
	 * @param context
	 * @param entranceId
	 *            入口值，服务器根据此值，判断哪一屏作为首屏显示
	 * @param showFrontCover 
	 *            是否展示启动loading页
	 */
	public static void startAppCenter(final Context context, int entranceId, boolean showFrontCover) {
		if (context != null) {
			Thread thread = new Thread("saveEntryCount") {
				@Override
				public void run() {
					StatisticsData.saveEntryCount(context, StatisticsData.ENTRY_APPS,
							AppRecommendedStatisticsUtil.getmCurrentEnterCode(context),
							AppRecommendedStatisticsUtil.NEW_VERSION_ID);
				}
			};
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
			Intent intent = new Intent(context, AppsManagementActivity.class);
			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY, entranceId);
			intent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_SHOW_FRONTCOVER, showFrontCover);
			//如果context不是Activity，则要加上NEW_TASK标志
			if (!(context instanceof Activity)) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			context.startActivity(intent);
		}
	}
	
	/**
	 * 从桌面某个地方直接进入一个专题，不需要经过应用中心
	 * 
	 * @param context
	 * @param typeId 分类id，每个进入专题的地方都会有一个固定的分类id
	 * @param returnToAppCenter 退出该专题是否需要返回应用中心
	 * 
	 * @author xiedezhi
	 */
	public static void startTopic(Context context, int typeId, boolean returnToAppCenter) {
		if (context != null) {
			Intent intent = new Intent(context, MoreAppsActivity.class);
			intent.putExtra(MoreAppsActivity.TOPIC_TYPEID_KEY, typeId);
			intent.putExtra(MoreAppsActivity.TOPIC_RETUREACTION_KEY, returnToAppCenter);
			//如果context不是Activity，则要加上NEW_TASK标志
			if (!(context instanceof Activity)) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			context.startActivity(intent);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Log.d("AppManagementActivity",
		// "requestCode:"+requestCode+" resultCode : "+resultCode+" data :"+data);
		if (requestCode == REQUEST_CODE_FOR_INSTALL) {
			mIsIntalling = false;
			installApp();
		} else if (requestCode == REQUEST_CODE_FOR_UNINSTALL) {
			mIsUnintalling = false;
			uninstallApp();
		}
		// else if (requestCode == REQUEST_CODE_FOR_SETTING){
		// mSettingData.initData();
		// }
	}

	/**
	 * 将apk添加到安装列表中
	 */
	public void addInstallApp(String filePath) {
		synchronized (mInstallFileList) {
			mInstallFileList.add(filePath);
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
				String filePath = mInstallFileList.get(mPosition);
				installApp(filePath);
				// mIsIntalling = true;
				// Log.d("AppManagementActivity",
				// " installApp : "+currPackageName);
			}
		}
	}

	public void installApp(String filePath) {
		// File file = new File(filePath);
		// Intent intent = new Intent();
		// // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setAction(android.content.Intent.ACTION_VIEW);
		// intent.setDataAndType(Uri.fromFile(file),
		// "application/vnd.android.package-archive");
		// this.startActivityForResult(intent, REQUEST_CODE_FOR_INSTALL);
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
	 * 卸载应用
	 */
	private void uninstallApp() {
		synchronized (mUninstallAppList) {
			if (mCurrUninstallPosition < mUninstallAppList.size() - 1) {
				mCurrUninstallPosition = mCurrUninstallPosition + 1;
				String packageName = mUninstallAppList
						.get(mCurrUninstallPosition);
				uninstallApp(packageName);
				mIsUnintalling = true;
			}
		}
	}

	public void uninstallApp(String packageName) {
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		// uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivityForResult(uninstallIntent, REQUEST_CODE_FOR_UNINSTALL);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			mNeedShowMenu = true;
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && mNeedShowMenu) {
			mNeedShowMenu = false;
			if (mMenu == null) {
				mMenu = new AppGameMenu(this);
				mMenu.setOnItemClickListener(mMenuClickListener);
			}
			if (mMainViewGroup != null) {
				if (mMainViewGroup.onPrepareOptionMenu(mMenu)) {
					return true;
				}
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private OnItemClickListener mMenuClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (view.getId()) {
			case IMenuHandler.MENU_ITEM_DOWNLOAD_MANAGER:
				Intent i = new Intent(sContext, AppsDownloadActivity.class);
				startActivity(i);
				break;
			case IMenuHandler.MENU_ITEM_FRESH:
				// 统计代码
				StatisticsData.countStatData(AppsManagementActivity.this,
						StatisticsData.KEY_REFRESH_APP);
				mMainViewGroup
						.onOptionsItemSelected(IMenuHandler.MENU_ITEM_FRESH);
				break;
			case IMenuHandler.MENU_ITEM_SETTING:
				Intent in = new Intent(sContext, AppGameSettingActivity.class);
				startActivityForResult(in, REQUEST_CODE_FOR_SETTING);
				break;
			case IMenuHandler.MENU_ITEM_SORT_BY:
				mMainViewGroup
						.onOptionsItemSelected(IMenuHandler.MENU_ITEM_SORT_BY);
				break;
			case IMenuHandler.MENU_ITEM_BATCH_UNINSTALL:
				mMainViewGroup
						.onOptionsItemSelected(IMenuHandler.MENU_ITEM_BATCH_UNINSTALL);
				break;
			case IMenuHandler.MENU_ITEM_FEEDBACK:
				Intent intent = new Intent();
				intent.setClass(AppsManagementActivity.this, AppFeedbackActivity.class);
				intent.putExtra(AppFeedbackActivity.ENTRANCE, AppFeedbackActivity.ENTRANCE_FROM_APP_LIST);
				AppsManagementActivity.this.startActivity(intent);
//				AppFeedback.showAppMenuFeedback(sContext);
				break;
			default:
				break;
			}
			mMenu.dismiss();
		}
	};

	@Override
	public void onBackPressed() {
		// add by xiedezhi，处理返回键事件
		PreferencesManager manager = new PreferencesManager(
				AppsManagementActivity.this,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		// 上一次按返回键的时间
		long prevTime = manager.getLong(BACK_PRESS_KEY, -1);
		manager.putLong(BACK_PRESS_KEY, -1);
		if (TabDataManager.getInstance().getTabStackSize() > 1) {
			// 有两级或以上的tab
			// 如果正在刷新，则停止刷新，返回之前一页
			if (TabController.isTabRefreshing()) {
				Log.e("AppsManagementActivity", "tab is refreshing");
				TabController.stopRefreshing();
				return;
			}
			TabController.fallBackTab();
		} else if (TabDataManager.getInstance().getTabStackSize() == 1) {
			// 有一级tab
			// 如果正在刷新，则停止刷新，返回之前一页
			if (TabController.isTabRefreshing()) {
				Log.e("AppsManagementActivity", "tab is refreshing");
				TabController.stopRefreshing();
				return;
			}
			// 如果不是在首页，先返回首页；如果是在首页，直接退出
			TabDataGroup cGroup = TabDataManager.getInstance().peekTab();
			if (cGroup == null || cGroup.position == 0 || (!cGroup.isIconTab)
					|| cGroup.categoryData == null
					|| cGroup.categoryData.size() <= 0
					|| cGroup.subGroupList == null
					|| cGroup.subGroupList.size() <= 0) {
				// 如果是第一次按下，弹toast提示，如果是5秒内第二次按下，退出
				long currentTime = System.currentTimeMillis();
				manager.putLong(BACK_PRESS_KEY, currentTime);
				if (currentTime - prevTime > EXIT_INTERVAL) {
					// 弹出toast
					// 应用中心国内的某些渠道要修改名称，修改为 安卓应用市场（中文）/GO Market（英文），所以要进行渠道控制
					// Add by wangzhuobin 2012.10.31
					int id = R.string.app_center_exit_toast;
					Toast.makeText(getApplicationContext(), id,
							Toast.LENGTH_SHORT).show();
				} else {
					// 当前已经是首页，直接退出
					super.onBackPressed();
				}
			} else {
				// TODO:XIEDEZHI
				// 这里应该是发一个消息到TabManageView，让它来切换tab，这样就可以控制顶级tab栏的UI切换了
				AppsManagementActivity.sendHandler("",
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.SKIP_SIDE_TAB, 0, new int[] { 0, -1 }, null);
			}
		} else {
			// 没有tab，直接退出
			super.onBackPressed();
		}
		manager.commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// 在android4.x机器上，在处理onBackPress时会报以下错误：
		// Can not perform this action after
		// onSaveInstanceStatejava.lang.IllegalStateException: Can not perform
		// this action after onSaveInstanceState
		// 解决方法参考http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-h?rq=1
		// 把super.onSaveInstanceState(outState);注释掉
		// super.onSaveInstanceState(outState);
	}

	public static void addShortcut(Activity activity) {
		PreferencesManager sharedPreferences = new PreferencesManager(activity,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		sHasPutIcon = sharedPreferences.getBoolean(
				LauncherEnv.ALREADY_PUT_GO_MARKET_ICON, false);
		if (sHasPutIcon) {
			return;
		}
		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

		// 快捷方式的名称  
		String name = activity.getResources().getString(R.string.appcenter_title);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		// 不允许重复创建  
		shortcut.putExtra("duplicate", false);

		// 指定当前的Activity为快捷方式启动的对象: 如 com.everest.video.VideoPlayer  
		// 这里必须为Intent设置一个action，可以任意(但安装和卸载时该参数必须一致)  
		Intent respondIntent = new Intent(activity, AppsManagementActivity.class);
		respondIntent.setAction(GOMARKET_ICON_ATICON);
		respondIntent.putExtra(AppsManagementConstants.APPS_MANAGEMENT_SHOW_FRONTCOVER, true);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, respondIntent);
		// 下面的方法与上面的效果是一样的,另一种构建形式而已  
		// 注意: ComponentName的第二个参数必须加上点号(.)，否则快捷方式无法启动相应程序  
		//	    String appClass = this.getPackageName() + "." + this.getLocalClassName();  
		//	    ComponentName comp = new ComponentName(this.getPackageName(), appClass);  
		//	    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(action).setComponent(comp));  

		// 快捷方式的图标  
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(activity,
				R.drawable.app_center_icon_large);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		
		activity.sendBroadcast(shortcut);

		activity.setResult(RESULT_OK, shortcut);

		sharedPreferences.putBoolean(LauncherEnv.ALREADY_PUT_GO_MARKET_ICON, true);
		sharedPreferences.commit();
	}  
}
