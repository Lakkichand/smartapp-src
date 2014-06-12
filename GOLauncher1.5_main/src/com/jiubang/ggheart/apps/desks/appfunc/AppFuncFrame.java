package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.animation.AnimationFactory;
import com.go.util.device.Machine;
import com.go.util.file.media.FileInfo;
import com.go.util.file.media.ThumbnailManager;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.mars.XAEngine;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolder;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.appfunc.timer.Scheduler;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncSwitchMenuItemInfo;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.appfunc.model.UIThreadHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.mediamanagement.MediaOpenSettingActivity;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaUIManager;

/**
 * 功能表框架层，生命周期由桌面框架维护
 * 
 * @author tanshu
 * 
 */
public class AppFuncFrame extends AbstractFrame implements AnimationListener, IMsgHandler {
	/**
	 * 本层布局
	 */
	private FrameLayout mAppFuncFrameLayout;
	/**
	 * 内部显示层
	 */
	private XViewFrame mViewFrame = null;
	/**
	 * 后台程序管理控制器
	 */
	private static FunControler sFunControler;
	/**
	 * 程序配置管理控制器：例如程序是否隐藏
	 */
	private static AppConfigControler sAppConfigControler;
	/**
	 * 刷新帧速
	 */
	public static int sRefreshStep;
	/**
	 * 程序第一次运行时调用
	 */
	private boolean mIsFirstBootUp;
	/**
	 * 是否被显示
	 */
	public static boolean sVisible;

	/**
	 * 主题控制器
	 */
	private static AppFuncThemeController sThemeCtrl;
	/**
	 * 前后台数据衔接
	 */
	private static FuncAppDataHandler sDataHandler;

	public static final int STATE_LEAVED = 0; // 离开功能表后
	public static final int STATE_NORMAL = 1; // 进入功能表后
	public static final int STATE_ENTERING = 2; // 开始进入功能表
	public static final int STATE_LEAVING = 3; // 开始离开功能表
	public static int sCurrentState = STATE_LEAVED;
	/**
	 * 是否不透明（使用非动态壁纸）
	 */
	private boolean mOpaque;
	/**
	 * 这个值为false，正在做动画代表。
	 */
	private boolean mIsOpaque = true;
	/**
	 * 是否过滤掉菜单弹出事件
	 */
	private boolean mFilterMenu = false;

	private boolean mNeedReLoadDesk = false;
	private int mEnterOrientation; // 进入时横竖屏

	/**
	 * 是否在文件夹编辑界面
	 */
	private boolean mInFolderEditMode = false;

	private boolean mExitForDrag = false; // 因为启动桌面拖动层退出功能表

//	private Animation mAnimation = null;
//	private AppFuncUtils mFuncUtils;

//	private FunAppSetting mFunAppSetting;
	private boolean mRespUpDownAction = true;
	private boolean mShowSwitchMenu = true;
	/**
	 * 
	 */
	private ThemeSettingInfo mThemeSettingInfo;
	/**
	 * 默认动画时间。
	 */
	public static final int DEFAULT_DURATION = 500;
	private static final int CHECK_SETTING_ITEM_CHANGE = 0;
	
	public AppFuncFrame(Activity activity, IFrameManager frameManager, int frameId) {
		super(activity, frameManager, frameId);
		initThemeController(activity);
		AppDataEngine appDataEngine = GOLauncherApp.getAppDataEngine();
		sAppConfigControler = AppConfigControler.getInstance(activity);
		sFunControler = FunControler.getInstance(activity);
		mAppFuncFrameLayout = new FrameLayout(activity);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		mAppFuncFrameLayout.setLayoutParams(lp);
		XComponent.mContext = activity;
		init();
		// 第一次启动加载资源
		// 通知注册此消息的组件重新获取主题资源
		DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.LOADTHEMERES, null);
		sThemeCtrl.setChangeTheme(false);
	}

	private void init() {
//		mFunAppSetting = GOLauncherApp.getSettingControler().getFunAppSetting();
		// 初始化工具类
//		mFuncUtils = AppFuncUtils.getInstance(mActivity);
		// 初始化数据控制器
		sDataHandler = FuncAppDataHandler.getInstance(mActivity);
		// 初始化显示层
		XViewFrame.createInstance(mActivity);
		mViewFrame = XViewFrame.getInstance();
		mIsFirstBootUp = true;
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.APPFUNCFRAME, this);
		UIThreadHandler.getInstance();
	}
	
	private static void initThemeController(Activity activity) {
		sThemeCtrl = AppFuncThemeController.getInstance(activity);
	}

	public static FunControler getFunControler() {
		if (sFunControler == null) {
			Context context = GoLauncher.getContext();
			if (context != null) {
				sFunControler = FunControler.getInstance(context);
			}
		}
		return sFunControler;
	}

	public static FuncAppDataHandler getDataHandler() {
		if (sDataHandler == null) {
			Context context = GoLauncher.getContext();
			if (context != null) {
				sDataHandler = FuncAppDataHandler.getInstance(context);
			}
		}
		return sDataHandler;
	}

	public static AppConfigControler getAppConfigControler() {
		if (sAppConfigControler == null) {
			Context context = GoLauncher.getContext();
			if (context != null) {
				sAppConfigControler = AppConfigControler.getInstance(context);
			}
		}
		return sAppConfigControler;
	}

	public static AppFuncThemeController getThemeController() {
		if (sThemeCtrl == null) {
			Context context = GoLauncher.getContext();
			if (context != null) {
				sThemeCtrl = AppFuncThemeController.getInstance(context);
			}
		}
		return sThemeCtrl;
	}

	/**
	 * 接收来自DIY桌面和功能表内部的消息
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
		// 安装卸载消息，直接转给FunControler
			case IDiyMsgIds.EVENT_INSTALL_APP :
			case IDiyMsgIds.EVENT_INSTALL_PACKAGE :
			case IDiyMsgIds.EVENT_UNINSTALL_APP :
			case IDiyMsgIds.EVENT_LOAD_FINISH :
			case IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK :
			case IDiyMsgIds.EVENT_REFLUSH_TIME_IS_UP :
			case IDiyMsgIds.EVENT_LOAD_TITLES_FINISH :
			case IDiyMsgIds.EVENT_UPDATE_PACKAGE : 
			case IDiyMsgIds.EVENT_UNINSTALL_PACKAGE : {
				if (sFunControler != null) {
					sFunControler.handleMessage(msgId, param, object, objects);
				}
				AppDrawerControler.getInstance(mActivity).handleMessage(msgId, param, object, objects);
				break;
			}
			// 主题改变
			case IDiyMsgIds.EVENT_THEME_CHANGED : {
				if (sThemeCtrl != null) {
					sThemeCtrl.handleMessage(msgId, param, object, objects);
				}
				if (sDataHandler != null) {
					sDataHandler.handleMessage(msgId, param, object, objects);
				}
				break;
			}
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_APPTODESK,
						AppFuncConstants.SYSTEM_CONFIGURATION_CHANGED, null);
				if (mViewFrame != null) {
					mViewFrame.setIsForceLayout(true);
				}
				if (sVisible) {
					mNeedReLoadDesk = true;
				}
				if (sVisible && mViewFrame != null) {
					mViewFrame.dismissAllAppMenu(); // 如果存在菜单，关闭该菜单
					mViewFrame.dismissProgressDialog();
					// 重新布局
					mViewFrame.setIsForceLayout(true);
					mViewFrame.requestLayout();
					mViewFrame.handleConfigChanged();
					ret = true;
				}
				/*
				 * if (null != mViewFrame) {
				 * mViewFrame.onGGMenuConfigurationChanged(null); }
				 */
				if (MediaPluginFactory.isMediaPluginExist(GoLauncher.getContext())) { // 存在资源管理插件包
					 IMediaUIManager mediaUIManager = MediaPluginFactory.getMediaUIManager();
					 if (mediaUIManager != null) {
						 mediaUIManager.onConfiguractionChange(GoLauncher.getScreenWidth(), GoLauncher.getScreenHeight()); // 通知横竖屏发生变化
					 }
				}
			}
				break;

			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : { // 隐藏自己回到主屏幕
				if (sVisible) {
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_TABCONTENT,
							AppFuncConstants.EXITEDITMODEL, null);
					if (sFunControler != null && sFunControler.getIsIconTitleLoadFinish()) {
						exit(true);
					} else {
						exit(false);
					}
				}
//				DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
//						AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
//						new Object[] { AppFuncContentTypes.APP });
			}
				break;

			case IFrameworkMsgId.SYSTEM_HOME_CLICK : {
				if (mViewFrame != null) {
					mViewFrame.onDismissGGMenu();
				}
				// 重置功能表页面
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.APP_FUNC_SEARCH_RESULT_CONTAINER,
						AppFuncConstants.ALL_APP_SEARCH_RESET, "");
			}
				break;

			case IDiyMsgIds.SEND_BACKGROUND : {
				mOpaque = object != null;
				if (mViewFrame != null) {
					mViewFrame.setDeskTopBg((Drawable) object);
				}
			}
				break;

			case IDiyMsgIds.LOCK_SCREEN :
			case IDiyMsgIds.UNLOCK_SCREEN : {
				// 通知所有程序和文件夹不能进入编辑模式
				DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.LOCKCHANGES, null);
			}
				break;
			case IDiyMsgIds.APPDRAWER_TAB_HOME_THEME_CHANGE : {
				if (sThemeCtrl != null) {
					sThemeCtrl.handleMessage(msgId, param, object, objects);
				}
			}
				break;
			case IDiyMsgIds.APPDRAWER_INDICATOR_THEME_CHANGE : {
				if (sThemeCtrl != null) {
					sThemeCtrl.handleMessage(msgId, param, object, objects);
				}
			}
				break;
			case IDiyMsgIds.APPDRAWER_REMOVE_GGMENU_GUIDE : {
				if (mViewFrame != null) {
					mViewFrame.removeMenuGuideView();
				}
			}
				break;
			case IDiyMsgIds.APPDRAWER_IS_GGMENU_GUIDE_SHOWING : {
				if (mViewFrame != null) {
					ret = mViewFrame.isGGmenuTutorialShowing();
				}
			}
				break;
			case IDiyMsgIds.APPDRAWER_ENTER_DRAG_TUTORIAL : {
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.TABCOMPONENT,
						AppFuncConstants.APP_COMP_SET_CURRENT_TAB_INDEX,
						GoLauncher.isPortait()
								? AppFuncConstants.ALLAPPS
								: AppFuncConstants.PROCESSMANAGEMENT);
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_GRID,
						AppFuncConstants.TUTORIAL_DRAG_MODE, null);
			}
				break;
			case IDiyMsgIds.APPDRAWER_ENTER_CREATE_FOLDER_TUTORIAL : {
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.TABCOMPONENT,
						AppFuncConstants.APP_COMP_SET_CURRENT_TAB_INDEX,
						GoLauncher.isPortait()
								? AppFuncConstants.ALLAPPS
								: AppFuncConstants.PROCESSMANAGEMENT);
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_GRID,
						AppFuncConstants.TUTORIAL_CREATE_FOLDER_MODE, null);
			}
				break;
			case IDiyMsgIds.APPDRAWER_ENTER_HIDE_TUTORIAL : {
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.TABCOMPONENT,
						AppFuncConstants.APP_COMP_SET_CURRENT_TAB_INDEX,
						GoLauncher.isPortait()
								? AppFuncConstants.ALLAPPS
								: AppFuncConstants.PROCESSMANAGEMENT);
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.XVIEW,
						AppFuncConstants.TUTORIAL_HIDE_APP_MODE, null);
			}
				break;
			case IDiyMsgIds.APPDRAWER_FOLDER_THEME_CHANGE : {
				if (sThemeCtrl != null) {
					sThemeCtrl.handleMessage(msgId, param, object, objects);
				}
			}
				break;

			// 接收到程序批量更新消息
			case IDiyMsgIds.EVENT_APPS_LIST_UPDATE :
				if (sDataHandler != null && sDataHandler.isShowAppUpdate()) {
					sFunControler.handleMessage(IDiyMsgIds.EVENT_APPS_LIST_UPDATE, 0, object, null);
				}
				break;
			case IDiyMsgIds.EVENT_FUNCFRAM_LONGPRESS : {
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
						AppFuncConstants.APP_FUNC_MENUKEY_LONGPRESS, false);
				mFilterMenu = true;
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
				mFilterMenu = false; // 进入搜索层后还原该值
				break;
			}
			case IDiyMsgIds.RENAME : {
				Bundle bundle = new Bundle();
				bundle.putString("newName", (String) objects.get(0));
				bundle.putLong("folderId", (Long) object);
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFOLDER,
						AppFuncConstants.FOLDER_RENAME, bundle);
			}
				break;

			/**
			 * 檢查是否有垃圾數據
			 */
			case IDiyMsgIds.IS_EXIST_TRASH_DATA : {
				if (sFunControler != null) {
					ret = sFunControler.needCleanDirtyData();
				}
				break;
			}
			/**
			 * 清理垃圾數據
			 */
			case IDiyMsgIds.CLEAN_TRASH_DATA : {
				if (sFunControler != null) {
					sFunControler.cleanDirtyData();
				}
				break;
			}
			case IDiyMsgIds.SCREEN_FINISH_LOADING :
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_APPTODESK,
						AppFuncConstants.SCREEN_LOAD_FINISH, null);
				break;

			case IDiyMsgIds.DRAG_OVER :
				if (mNeedReLoadDesk
						&& mEnterOrientation != mActivity.getResources().getConfiguration().orientation) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREEN_RELOAD_DESK, 0, null, null);
				}
				mNeedReLoadDesk = false;
				mEnterOrientation = mActivity.getResources().getConfiguration().orientation;
				break;
			case IDiyMsgIds.APPDRAWER_EXIT_FOR_DRAG : {
				mExitForDrag = true;
			}
				break;
			case IDiyMsgIds.APPDRAWER_ENTER_FOLDER_EDIT_MODE :
				mInFolderEditMode = true;
				break;
			case IDiyMsgIds.APPDRAWER_EXIT_FOLDER_EDIT_MODE :
				mInFolderEditMode = false;
				break;
			case IDiyMsgIds.APPDRAWER_OPERATION_FAILED :
				mViewFrame.getAppFuncMainView().reloadAllApps();
				DeskToast.makeText(mActivity, R.string.operation_failed, Toast.LENGTH_LONG).show();
				break;

			case IDiyMsgIds.NOTIFICATION_CHANGED :
				if (object instanceof Integer) {
					int count = ((Integer) object).intValue();
					if (sFunControler != null) {
						sFunControler.updateNotification(param, count);
					}
				}
				break;
			case IDiyMsgIds.RESPONSE_GLIDE_UP_DOWN_ACTION :
				mRespUpDownAction = param == 0 ? false : true;
				break;
			case IDiyMsgIds.SHOW_TAB_ROW :
				if (mRespUpDownAction) {
					mViewFrame.getAppFuncMainView().showTabRow();
				}
				break;
			case IDiyMsgIds.HIDE_TAB_ROW :
				if (mRespUpDownAction) {
					mViewFrame.getAppFuncMainView().hideTabRow();
				}
				break;
			case IDiyMsgIds.SHOW_ACTION_BAR :
				if (mRespUpDownAction) {
					mViewFrame.getAppFuncMainView().showActionBar();
				}
				break;
			case IDiyMsgIds.HIDE_ACTION_BAR :
				if (mRespUpDownAction) {
					mViewFrame.getAppFuncMainView().hideActionBar();
				}
				break;
			case IDiyMsgIds.SET_WALLPAPER_DRAWABLE : {
				if (GoLauncher.getTopFrame() == this && mViewFrame != null) {
					mViewFrame.setReMergeBg(true);
					mViewFrame.postInvalidate();
				}
				break;
			}
			case IFrameworkMsgId.SYSTEM_ON_RESUME :
				// 防止拖动图标离开文件夹时，动画未完成，就锁屏了，然后解锁后就造成功能表假死。
				if (AppFuncFrame.sVisible && mViewFrame != null) {
					AppFuncMainView mainView = mViewFrame.getAppFuncMainView();
					if (mainView != null && mainView.mIsAddCellComponent) {
						mainView.removeFolder();
						mainView.mAddCellComponentDirectly = true;
					}
				}
				break;
			case IDiyMsgIds.START_LOAD_MEDIA_DATA : {
				if (sFunControler != null) {
					sFunControler.registerFileObserver(null);
					sFunControler.buildFileEngine();
					sFunControler.refreshAllMediaData();
				}
				break;
			}
			case IDiyMsgIds.DESTROY_FILE_ENGINE : {
				if (sFunControler != null && param == AppFuncSwitchMenuItemInfo.ACTION_GO_TO_APP) {
					sFunControler.destroyFileEngine(); // 销毁多媒体引擎
				}
				ThumbnailManager.destory(); // 删除多媒体搜索缩略图缓存
				break;
			}
			case IDiyMsgIds.OPEN_MEIDA_FILE : {
				AppFuncMainView mainView = mViewFrame.getAppFuncMainView();
				if (sFunControler != null) {
					if (object instanceof FileInfo) {
						sFunControler.openMediaFile((FileInfo) object, param, objects);
					}
				}
				break;
			}
			case IDiyMsgIds.SNAPSHOT_DESTROY_APPFUNCFRAME_CACHE: // 取消由于截屏产生到缓存
			{
				mAppFuncFrameLayout.destroyDrawingCache();
			}
				break;
			case IDiyMsgIds.APPDRAWER_PROGRESSBAR_SHOW : {
				mViewFrame.showProgressDialog();
			}
				break;
			case IDiyMsgIds.APPDRAWER_PROGRESSBAR_HIDE : {
				mViewFrame.dismissProgressDialog();
			}
				break;
			case IDiyMsgIds.SHOW_MEDIA_OPEN_SETTING_ACTIVITY : {
				Intent intent = new Intent(mActivity, MediaOpenSettingActivity.class);
				intent.putExtra(MediaOpenSettingActivity.SETTING_EXTRA_KEY, param);
				mActivity.startActivity(intent);
			}
			case IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE : {
				mediaManagementPluginSettingChange(AppFuncMainView.INDICATOR_SHOW_MODE_CHANGE, object);
				break;
			}
			case IDiyMsgIds.INDICATOR_CHANGE_POSITION : {
				mediaManagementPluginSettingChange(AppFuncMainView.INDICATOR_POSITION_CHANGE, object);
				break;
			}
			case IDiyMsgIds.SCREEN_ORIENTATION_CHANGE : {
				mediaManagementPluginSettingChange(AppFuncMainView.ORIENTATION_TYPE_CHANGE, object);
				break;
			}
			case IDiyMsgIds.SHOW_STATUS_BAR_SHOW_CHANGE : {
				mediaManagementPluginSettingChange(AppFuncMainView.SHOW_STATUS_BAR_CHANGE, object);
				break;
			}
			case IDiyMsgIds.LOCATE_MEDIA_ITEM : {
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.APP_FUNC_MAIN_VIEW,
						AppFuncConstants.LOCATE_MEDIA_ITEM, object);
			}
			default :
				break;
		}
		return ret;
	}
	
	private void mediaManagementPluginSettingChange(int type, Object obj) {
		if (mViewFrame != null) {
			AppFuncMainView mainView = mViewFrame.getAppFuncMainView();
			if (mainView != null) {
				mainView.mediaManagementPluginSettingChange(type, obj);
			}
		}
	}

	@Override
	public View getContentView() {
		return mAppFuncFrameLayout;
	}

	public XViewFrame getXViewFrame() {
		return mViewFrame;
	}

	@Override
	public void onForeground() {
		// 重写此方法避免默认的实现
		registKey();
	}

	@Override
	public void onBackground() {
		// 重写此方法避免默认的实现
		unRegistKey();
		if (mViewFrame != null) {
			mViewFrame.onHide();
		}
	}

	@Override
	public boolean isOpaque() {
		return mIsOpaque;
	}
	
	public void setIsOpaque(boolean isOpaque) {
		this.mIsOpaque = isOpaque;
	}
	
	private static void setStaticVisibleField(boolean visible) {
		sVisible = visible;
	}

	@Override
	public void onVisiable(int visibility) {
		super.onVisiable(visibility);
		if (visibility == View.VISIBLE) {
			// Log.d("XViewFrame", "step 1");
			// 如果还未初始化，先初始化之
			// Log.d("XViewFrame", "step 2");
			setStaticVisibleField(true);
			// 检测设置项是否有变化
			if (mIsFirstBootUp) {
				mAppFuncFrameLayout.addView(mViewFrame);
				// 初始化动画引擎
				initEngine();
				mThemeSettingInfo = GOLauncherApp.getSettingControler().getThemeSettingInfo();
			} else {
				sDataHandler.checkSettingItemChanged();
				mViewFrame.setIsForceLayout(true);
				// 重新布局
				mViewFrame.requestLayout();
			}
			XBaseGrid.setIsExit(false);

			// 注册按键事件
			mFrameManager.registKey(this);

			// 更新“进程管理”Tab数据
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.TABCOMPONENT,
					AppFuncConstants.REFRESHPROCESSGRID, null);
			// 重新获取“最近打开”后台列表数据
			if (sDataHandler.updateRecentApps()) {
				sDataHandler.sendInfoToForeground(MessageID.UPDATE_RECENTAPP, null, null);
			}
			// 合成功能表背景
			mViewFrame.setReMergeBg();
			// Log.d("XViewFrame", "step 3");
			// 先查看后台是否加载完毕，如果没有，则直接进入而不做动画
			enter(true);
//			if (sFunControler.getIsIconTitleLoadFinish() && !mIsFirstBootUp) {
//				// 进入动画
//				enter(true);
//			} else {
//				enter(false);
//			}

			mNeedReLoadDesk = false;
			mEnterOrientation = mActivity.getResources().getConfiguration().orientation;

			if (mIsFirstBootUp) {
				GoLauncher launcher = GoLauncher.getContext();
				if (launcher.getNewVeriosnFirstRun()  
						&& !launcher.getFirstRun()
						&& launcher.isMdiamanagementBecomesPlugin()
						&& launcher.isUpgradeFromMdiamanagementUnbecomesPlugin()
						&& !MediaPluginFactory.isMediaPluginExist(GoLauncher.getContext())) {
					showDownloadDialog();
				}
				mIsFirstBootUp = false;
			}

			boolean b = sFunControler.isHandling();
			if (b) {
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.XVIEW,
						AppFuncConstants.FINISH_REFRESH_GRID_LIST, true);
			}

			mInFolderEditMode = false;
		} else {
			if (null != getFrameManager().getTopFrame()
					&& IDiyFrameIds.GUIDE_GL_FRAME == getFrameManager().getTopFrame().getId()) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
			}
			// 还未加载过功能表，直接忽略之
			if (mViewFrame == null || mIsFirstBootUp) {
				return;
			}
			// 退出功能表时关闭菜单
			/**
			 * @date 2012-2-4
			 * @edit by huangshaotao
			 *       之前由于功能表这边收不到home键点击事件，导致菜单出现后点击home键回到桌面后功能表菜单没有隐藏
			 *       ，所以把隐藏菜单的操作放到这里， 现在修改home键事件传递规则后home事件能传递到功能表来，所以这段代码不需要了
			 * 
			 *       if (mViewFrame != null) { mViewFrame.onDismissGGMenu(); }
			 */
			setStaticVisibleField(false);
			// 反注册按键事件
			mFrameManager.unRegistKey(this);

			// 重置功能表页面
			DeliverMsgManager.getInstance().onChange(
					AppFuncConstants.APP_FUNC_SEARCH_RESULT_CONTAINER,
					AppFuncConstants.ALL_APP_SEARCH_RESET, "");
			// 通知AllAppTabWidget重新设置底部房子控件和“移动到桌面”的状态防止
			// 某些情况下因为收不到up事件而造成状态不一致
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_TABCONTENT,
					AppFuncConstants.RESETCOMPONENTS, null);
			mViewFrame.getAppFuncMainView().removeFolder();
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
					AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
					new Object[] { AppFuncContentTypes.APP });
			// DeliverMsgManager.getInstance().onChange(
			// AppFuncConstants.ALLAPPS_APPMOVETODESK,
			// AppFuncConstants.MOVETODESKISUNTOUCHED, null);

			// 将view从动画引擎中移出
			// removeFromeXAEngine();
			// 暂停定时器
			XAEngine.stop();
			AnimationManager.getInstance(mActivity).clearAllAnimations();
			Scheduler.getInstance().stop();
			mViewFrame.onHide();
			// Register to receive the ON_DESTROY message
			// mFrameManager.getMessageManager().registMsgHandler(this,
			// IDiyMsgIds.SYSTEM_ON_DESTROY);
			mViewFrame.clearAnimation();
			mViewFrame.dismissAllAppMenu();
			mViewFrame.dismissProgressDialog();
			// mViewFrame.recycleMergeBg();
			mViewFrame.destroyComponentsDrawingCache();
			// mIsAnimationWorking = false;
			// LogUnit.i("mIsAnimationWorking : " + mIsAnimationWorking);

			// //退出功能表时 如果有更新图标给点击清除所有应用程序更新信息
			//			if (null != AppFuncFrame.getDataHandler()
			//					&& AppFuncFrame.getDataHandler().ismClickAppupdate()) {
			//				getFunControler().clearAllAppUpdate();
			//			}
			if (mExitForDrag) {
				mExitForDrag = false;
			} else if (mEnterOrientation != mActivity.getResources().getConfiguration().orientation) {
				mEnterOrientation = mActivity.getResources().getConfiguration().orientation;
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_RELOAD_DESK, 0, null, null);
			}
			// 要求一下回收
			try {
				OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mInFolderEditMode = false;
			setCurTab();
			sCurrentState = STATE_LEAVED;
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		// DIY桌面不在最前台
		if (sVisible) {
			if (!mInFolderEditMode) {
				XAEngine.stop();
			}
			if (mViewFrame != null) {
				mViewFrame.onHide();
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (sVisible) {			
			ApplicationIcon.sIsUninstall = false;
			XViewFrame.getInstance().getAppFuncMainView().mOpenFuncSetting = false;
		}
	}

	@Override
	public void onResume() {
		// 还未进行初始化，直接返回
		if (mViewFrame == null) {
			return;
		}
		super.onResume();
		if (sVisible) {
			XAEngine.resume();
			if (XAEngine.isStopped()) {
				XAEngine.resume();
			}

			// 检测设置项是否有变化
			if (!mIsFirstBootUp && sDataHandler != null) {
				sDataHandler.checkSettingItemChanged();
			}

			// int selectedTab =
			// mMainFrame.getSurfaceView().getTabComponent().
			// getSelectedIndex();

			// Done 重构
			int selectedTab = mViewFrame.getSeletedTab();

			if (selectedTab == AppFuncConstants.PROCESSMANAGEMENT) {
				// 更新“进程管理”Tab数据
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.TABCOMPONENT,
						AppFuncConstants.REFRESHPROCESSGRID, null);
			}
			if (sDataHandler != null && sDataHandler.updateRecentApps()) {
				// 重新获取“最近打开”后台列表数据
				sDataHandler.sendInfoToForeground(MessageID.UPDATE_RECENTAPP, null, null);
			}

			// 重新布局
			mViewFrame.setIsForceLayout(true);
			mViewFrame.requestLayout();
			mRespUpDownAction = true;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 收到系统的ON_DESTROY消息，需要将引擎置空
		XAEngine.destory();
		// 反注册后台设置监听者
		if (sDataHandler != null) {
			sDataHandler.onDestroy();
			sDataHandler = null;
		}
		if (sFunControler != null) {
			sFunControler.unRegisterSortObserver();
			sFunControler = null;
		}
		sAppConfigControler = null;
		sThemeCtrl = null;
		// Clean the static object
		AppFuncUtils.destroyInstance();
		AppFuncFolder.destroyInstance();
		UIThreadHandler.destroyInstance();
		DeliverMsgManager.destroyInstance();
		if (mAppFuncFrameLayout != null && mViewFrame != null) {
			mAppFuncFrameLayout.removeView(mViewFrame);
			XViewFrame.destroyInstance();
		}
		XComponent.mContext = null;
		// AppFuncTabWidget.destroyStaticImages();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.EVENT_FUNCFRAM_LONGPRESS, 0, null, null);
		}
		if (mViewFrame != null && !mFilterMenu) {
			return mViewFrame.onKey(keyCode, event);
		} else {
			return false;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (KeyEvent.ACTION_UP == event.getAction()
				&& (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU)) {
			AbstractFrame frame = GoLauncher.getTopFrame();
			if (frame != null && frame.getId() == IDiyFrameIds.GUIDE_GL_FRAME) {
				return true;
			}
		}
	
		if (mViewFrame != null && !mFilterMenu) {
			return mViewFrame.onKey(keyCode, event);
		} else {
			mFilterMenu = false;
			return false;
		}
	}

	private void initEngine() {
		sRefreshStep = 15; // 30;
		XAEngine.init(sRefreshStep);
		XAEngine.start();
		XAEngine.addTicker(mViewFrame);
	}

	/**
	 * 是否在做动画
	 * 
	 * @return
	 */
	// public boolean isAnimating() {
	// return mIsAnimationWorking;
	// }

	@Override
	public void onAnimationStart(Animation animation) {
//		Machine.setHardwareAccelerated(mAppFuncFrameLayout, Machine.LAYER_TYPE_SOFTWARE);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (sCurrentState == STATE_LEAVING) {
			// add by jiang
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK,
						100, null, null);
			// 防止在退出动画完成之前按键再次启动退出
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					mId, null, null);
			
//			if (AppUtils.isMediaPluginExist(GOLauncherApp.getContext())) { // 如果存在插件包，清除资源
//				IMediaManager mediaManager = MediaManagerFactory.getMediaManager();
//				if (mediaManager != null) {
//					mediaManager.onExitMediaManagement(); // 这里保留Container对象，不做清除
//				}
//				if (sFunControler != null) {
//					sFunControler.destroyFileEngine(); // 销毁FileEngine
//				}
//				ThumbnailManager.destory(); // 删除多媒体搜索缩略图缓存
//			}
		} else if (sCurrentState == STATE_ENTERING) {
			checkCount(); // 前3次进入功能表，弹出引导提示
			showSwitchMenu();
			XAEngine.resume();
			//开启定时服务。
			Scheduler.getInstance().run();
			Machine.setHardwareAccelerated(mAppFuncFrameLayout, Machine.LAYER_TYPE_HARDWARE);
			sCurrentState = STATE_NORMAL;
		}
		setIsOpaque(true);
		mViewFrame.setDrawingCacheEnabled(false);
	}

	private void showSwitchMenu() {
		GoLauncher launcher = GoLauncher.getContext();
		if (mShowSwitchMenu && launcher != null && launcher.isAppFuncSearchSupportMediaEdition()
				&& launcher.isUpgradeFromAppFuncSearchUnsupportMediaEdition()) {
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_SWITCH_BUTTON,
					AppFuncConstants.SHOW_SWITCH_MENU, null);
			mShowSwitchMenu = false;
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.EXITAPPFUNCFRAME : {
				if (AppFuncFrame.getFunControler().getIsIconTitleLoadFinish()) {
					exit(true);
				} else {
					exit(false);
				}
			}
				break;
			case AppFuncConstants.EXIT_APPFUNC_FRAME_WITHOUT_ANIMATION :
				exit(false);
				break;
			case AppFuncConstants.ALL_APP_MENU_SHOW : {
				KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU);
				if (event != null) {
					onKeyUp(event.getKeyCode(), event);
				}
				break;
			}
			case AppFuncConstants.ALL_APP_MENU_HIDE : {
				KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
				if (event != null) {
					onKeyDown(event.getKeyCode(), event);
				}
				break;
			}
			case AppFuncConstants.PRO_MANAGE_MENU_SHOW : {
				KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU);
				if (event != null) {
					onKeyUp(event.getKeyCode(), event);
				}
				break;
			}
			case AppFuncConstants.PRO_MANAGE_MENU_HIDE : {
				KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
				if (event != null) {
					onKeyDown(event.getKeyCode(), event);
				}
				break;
			}
			case AppFuncConstants.PROGRESSBAR_SHOW : {
				mViewFrame.showProgressDialog();
				break;
			}
			case AppFuncConstants.PROGRESSBAR_HIDE : {
				mViewFrame.dismissProgressDialog();
				break;
			}
			case AppFuncConstants.SHOW_SWITCH_MENU : {
				showSwitchMenu();
				break;
			}
			default :
				break;
		}
	}

	/**
	 * 进入功能表
	 */
	private void enter(boolean needAnimation) {
		if (sCurrentState != STATE_LEAVED) {
			return;
		}
		sCurrentState = STATE_ENTERING;
		Animation animation = null;
		if (needAnimation) {
			int effect = getDataHandler().getInoutEffect();
			animation = AnimationFactory.createEnterAnimation(effect, mActivity);
		}
		if (mViewFrame != null) {
			//为null的时候就是无特效
			if (animation != null) {
				if (mThemeSettingInfo != null && !mThemeSettingInfo.mTransparentStatusbar && mViewFrame.isDrawMergeBg()) {
					setIsOpaque(false);
				}
				mViewFrame.setDrawingCacheEnabled(true);
				animation.setAnimationListener(this);
				animation.setDuration(DEFAULT_DURATION);
				//数据未加载或者首次启动缩短动画时间。
				if (!sFunControler.getIsIconTitleLoadFinish() && mIsFirstBootUp) {
					animation.setDuration(100);
				}
				mViewFrame.startAnimation(animation);
			} else {
				onAnimationEnd(null);
			}
		}
	}

	/**
	 * 退出功能表
	 */
	private void exit(boolean needAnimation) {
		if (sCurrentState != STATE_NORMAL) {
			return;
		}
		sCurrentState = STATE_LEAVING;
		//退出功能表时 如果有更新图标给点击清除所有应用程序更新信息
		if (null != AppFuncFrame.getDataHandler()
				&& AppFuncFrame.getDataHandler().ismClickAppupdate()) {
			getFunControler().clearAllAppUpdate();
		}
		Animation animation = null;
		if (needAnimation) {
			int effect = getDataHandler().getInoutEffect();
			animation = AnimationFactory.createExitAnimation(effect, mActivity);
		}
		if (mViewFrame != null) {
			mViewFrame.dismissAllAppMenu();
			mViewFrame.dismissProgressDialog();
			if (animation != null) {
				if (mThemeSettingInfo != null && !mThemeSettingInfo.mTransparentStatusbar && mViewFrame.isDrawMergeBg()) {
					setIsOpaque(false);
				}
				mViewFrame.setDrawingCacheEnabled(true);
				animation.setAnimationListener(this);
				animation.setDuration(DEFAULT_DURATION);
				mViewFrame.startAnimation(animation);
			} else {
				onAnimationEnd(null);
			}
		}
	}
	
	private void checkCount() {
		// Done 重构
		// if(mMainFrame.getSurfaceView().getTabComponent().getSelectedIndex()
		// ==
		// AppFuncConstants.ALLAPPS){
		if (mViewFrame != null && mViewFrame.getSeletedTab() == AppFuncConstants.ALLAPPS) {

			// !Attention:在AllAppTabWidget.java中会读取IPreferencesIds.ENTER_FUNC
			PreferencesManager manager = new PreferencesManager(mActivity,
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
			int count = manager.getInt(IPreferencesIds.ENTER_FUNC, 0);
			if (count < 3) {
				count++;
				manager.putInt(IPreferencesIds.ENTER_FUNC, count);
				manager.commit();
				try {
					DeskToast.makeText(mActivity, R.string.screen_goto_func_tip, Toast.LENGTH_LONG)
							.show();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}
		}
	}

	public void registKey() {
		mFrameManager.registKey(this);
	}

	public void unRegistKey() {
		mFrameManager.unRegistKey(this);

	}

	/**
	 * 获取框架层管理器
	 * 
	 * @return
	 */
	public IFrameManager getFrameManager() {
		return mFrameManager;
	}

	/**
	 * 发送消息请求将功能表设置为[All]Tab
	 */
	public void setCurTab() {
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				AppFuncConstants.SET_ALL_APPS_TAB, null);
	}
	
	/**
	 * 显示下载资源管理插件提示窗口
	 */
	private void showDownloadDialog() {
		DialogConfirm downloadDialog = new DialogConfirm(mActivity);
		downloadDialog.show();
		downloadDialog.setTitle(R.string.download_mediamanagement_plugin_dialog_title);
		downloadDialog.setMessage(R.string.download_mediamanagement_plugin_dialog_text);
		downloadDialog.setPositiveButton(R.string.yes, new View.OnClickListener() {
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
		downloadDialog.setNegativeButton(R.string.no, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogConfirm savaDialog = new DialogConfirm(mActivity);
				savaDialog.show();
				savaDialog.setTitle(R.string.mediamanagement_delete_data_dialog_title);
				savaDialog.setMessage(R.string.mediamanagement_delete_data_dialog_text);
				savaDialog.setPositiveButton(R.string.yes, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new Thread(new Runnable() { // 启动异步线程删除数据库数据，避免阻塞主线程
							@Override
							public void run() {
								android.os.Process
										.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
								AppFuncFrame.getDataHandler().deleteAllMediaData();
							}
						}).start();
					}
				});
				savaDialog.setNegativeButton(R.string.no, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 保存资源管理数据，即不进行任何清除操作 
					}
				});
			}
		});
	}
}