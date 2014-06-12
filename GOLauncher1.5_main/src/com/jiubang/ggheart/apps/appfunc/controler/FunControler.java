package com.jiubang.ggheart.apps.appfunc.controler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.SortUtils;
import com.go.util.file.media.AudioFile;
import com.go.util.file.media.Category;
import com.go.util.file.media.FileEngine;
import com.go.util.file.media.FileEngine.FileObserver;
import com.go.util.file.media.FileInfo;
import com.go.util.file.media.ImageFile;
import com.go.util.file.media.MediaDataProviderConstants;
import com.go.util.file.media.MediaDbUtil;
import com.go.util.file.media.MediaFileUtil;
import com.go.util.file.media.ThumbnailManager;
import com.go.util.file.media.VideoFile;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.config.utils.ConfigUtils;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncExceptionHandler;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.Controler;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.ISettingObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItem;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.model.FunDataModel;
import com.jiubang.ggheart.data.tables.MediaManagementHideTable;
import com.jiubang.ggheart.data.tables.MediaManagementPlayListFileTable;
import com.jiubang.ggheart.data.tables.MediaManagementPlayListTable;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.launcher.ThreadName;
import com.jiubang.ggheart.plugin.mediamanagement.MediaManagementOpenChooser;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaManager;
import com.jiubang.ggheart.plugin.notification.NotificationControler;
import com.jiubang.ggheart.plugin.notification.NotificationType;

/**
 * 应用程序管理，负责手机所有应用程序的安装卸以及数据保存
 * 
 * @author huyong
 * 
 */
public class FunControler extends Controler implements ICleanable {
	private static FunControler sInstance;
	
	public static final int INSTALL_APP = 1;
	public static final int INSTALL_APPS = 2;
	public static final int UNINSTALL_APP = 3;
	public static final int UNINSTALL_APPS = 4;
	public static final int LOAD_FINISH = 5;
	public static final int ADDITEM = 6;
	public static final int REMOVEITEM = 7;
	public static final int SORTFINISH = 8;
	public static final int SDCARDOK = 9;
	public static final int BATADD = 10;
	public static final int FINISHLOADINGSDCARD = 11;
	public static final int STARTSAVE = 12; // 开始保存
	public static final int FINISHSAVE = 13; // 已保存完毕
	public static final int FINISHLOADICONTITLE = 14; // 已加载icon和title完毕
	public static final int TIMEISUP = 15; // 2分钟计时到点
	public static final int BATUPDATE = 16; // 有一批程序可更新
	public static final int STARTLOADINGAPP = 17;
	
	//
	public static final String CLEAN_FOLDER_DATA  = "clean_folder_data";
	public static final String CHECK_FOLDER_DATA  = "check_folder_data";
	// 内部同步消息
	private static final int MSG_STARTSAVE = 0; // 开始保存
	private static final int MSG_FINISHSAVE = 1; // 保存完成
	private static final int MSG_CACHEDAPPS = 2; // 批量安装卸载
	// private static final int MSG_UNINSTALLAPPS = 3; // 批量卸载
	private static final int MSG_SDCARDAPPS = 3; // SDcard缓存数据
	private static final int MSG_FINISHSORT = 4; // 排序完成
	private static final int MSG_SORTFAILED = 5; // 排序失败
	private static final int MSG_FINISHINIT = 6; // 初始化线程完成
	private static final int MSG_BATADD = 7; // 分批添加x个
	
	private static final int MSG_SHOW_CLEAN_DIALOG = 10;
	/**
	 * 从搜索打开多媒体文件
	 */
	public static final int MEDIA_FILE_OPEN_BY_SEARCH = 1;
	/**
	 * 从插件包打开多媒体文件
	 */
	public static final int MEDIA_FILE_OPEN_BY_PLUGIN = 2;

	private volatile boolean mHasInit = false;
	private volatile boolean mHasStartInit = false;
	// 列表（包括程序和文件夹）
	private volatile ArrayList<FunItemInfo> mAllAppItemInfos = null;
	// 是否正在加载数据
	private volatile boolean mIsHandling = true;
	// SD缓存的数据是否已经被处理
	private volatile boolean mIsSDHandled = false;
	// Cache缓存的数据是否已经被处理
	private volatile boolean mIsCacheHandled = false;
	// Uninstall缓存的数据是否已经被处理
	// private volatile boolean mIsUninstallHandled = false;

	// 统一程序数据管理
	private AppDataEngine mAppDataEngine;
	// 持久化操作
	private FunDataModel mFunDataModel = null;
	// 状态控制
	private StateParaphrase mStateParaphrase = null;
	// 排序是否发生变化
	private boolean mSortChanged = false;
	//
	private Handler mHandler = null;

	// TODO:??
	private byte[] mLocker = new byte[0]; // 锁对象
	private boolean mIsLoadFinish = false;
	private volatile boolean mIsIconTitleLoadFinish = false;
	private volatile boolean mIsInitFinish = false; // 是否初始化完毕

	// 安装卸载的缓存数据
	private ArrayList<CacheItemInfo> mCachedApps;
	// SD卡的缓存数据
	private ArrayList<AppItemInfo> mCacheSDcard;

	// 程序注册信息
	private AppConfigControler mAppConfigControler;
	// 排序设置监听者
	private ISettingObserver mSortObserver;

	private Object mLock = new Object();
	/**
	 * 是否新安装了主题
	 */
	public static boolean sInstalledNewTheme = false;

	private AppsBean mUpdateBeans = null;
	private AppsBean mUpdateBeansForFolder = null;

	// 更新后更新软件的集合
	private ArrayList<AppBean> mBeanlist = null;

	public static final String GOSTORECOUNT = "gostorecount";
	public static final String GOSTORE_SHOW_MESSAGE = "gostore_show_message";
	public static final String APPFUNC_APPMENU_SHOW_MESSAGE = "appfunc_appmenu_show_message";
	public static final String APPICON_SHOW_MESSSAGE = "appicon_show_message";

	

	private FileEngine mFileEngine;
	private AppFuncUtils mAppFuncUtils;

	public synchronized static FunControler getInstance(Context context) {
		if (sInstance == null && context != null) {
			sInstance = new FunControler(context);
		}
		return sInstance;
	}

	public FunControler(Context context) {
		super(context);

		// 监听程序注册信息的改变
		mAppConfigControler = AppConfigControler.getInstance(context);
		// 注册为监听者
		initAppConfigControlerHandler();

		mStateParaphrase = new StateParaphrase();
		mStateParaphrase.updateState(StateParaphrase.NONE);

		mAllAppItemInfos = new ArrayList<FunItemInfo>();

		// 监听数据改变
		mAppDataEngine = GOLauncherApp.getAppDataEngine();
		// 数据处理
		mFunDataModel = new FunDataModel(context, mAppDataEngine);
//		mFileEngine = new FileEngine(context);

		mAppFuncUtils = AppFuncUtils.getInstance(context);

		// 初始化Handler
		initHandler();

		// 注册排序监听者
		registerSortObserver();
	}

	/**
	 * <br>
	 * 功能简述:获取保存在shareprefencd中的应用程序可更新个数。 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public int getmBeancount() {
		// 从shareprefencd里得到数字
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
				Context.MODE_PRIVATE);
		int mBeancount = preferences.getInt(GOSTORECOUNT, 0);
		return mBeancount;
	}

	/**
	 * <br>
	 * 功能简述:保存应用程序可更新个数到shareprefencd中 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param mBeancount
	 */
	public void setmBeancount(int mBeancount) {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
				Context.MODE_PRIVATE);
		preferences.putInt(GOSTORECOUNT, mBeancount);
		preferences.commit();
	}

	/**
	 * 保存应用更新提示是否开关信息
	 * 
	 * @param appIconControl
	 *            是否显示功能表图标右上角应用更新提示 0为不显示，1为显示
	 * @param gostoreControl
	 *            是否显示GO Store功能表图标右上角更新提示 0为不显示，1为显示
	 * @param appFuncMenuControl
	 *            是否显示功能表Menu菜单应用更新提示 0为不显示，1为显示
	 */
	public void setmControlInfo(byte appIconControl, byte gostoreControl,
			byte appFuncMenuControl) {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
				Context.MODE_PRIVATE);
		preferences.putInt(APPICON_SHOW_MESSSAGE, appIconControl); // 功能表图标右上角
		preferences.putInt(GOSTORE_SHOW_MESSAGE, gostoreControl); // GO
																	// Store功能表图标右上角
		preferences.putInt(APPFUNC_APPMENU_SHOW_MESSAGE, appFuncMenuControl); // 功能表Menu菜单

		// Log.d("FunControler",
		// appIconControl+" : "+gostoreControl+" : "+appFuncMenuControl);
		preferences.commit();
	}

	/**
	 * <br>
	 * 功能简述:获取从网络中下载并缓存在内存中的应用更新信息列表 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public ArrayList<AppBean> getBeanlist() {
		return mBeanlist;
	}

	/**
	 * <br>
	 * 功能简述:保存从网络中下载的应用更新信息列表到缓存 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param newbeanlist
	 */
	public void setBeanlist(ArrayList<AppBean> newbeanlist) {
		this.mBeanlist = newbeanlist;
	}

	public FunDataModel getFunDataModel() {
		return mFunDataModel;
	}

	/**
	 * 注册排序设置的监听者
	 */
	private void registerSortObserver() {
		mSortObserver = new ISettingObserver() {
			@Override
			public void onSettingChange(int index, int value, Object object) {
				switch (index) {
				case FunAppSetting.INDEX_SORTTYPE: {
					mSortChanged = true;
					break;
				}
				default:
					break;
				}
			}
		};
		GOLauncherApp.getSettingControler().getFunAppSetting()
				.setISettingObserver(mSortObserver);
	}

	public void unRegisterSortObserver() {
		GOLauncherApp.getSettingControler().getFunAppSetting()
				.unsetISettingObserver(mSortObserver);
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_STARTSAVE: {
					broadCast(STARTSAVE, 0, null, null);
				}
					break;
				case MSG_FINISHSAVE: {
					broadCast(FINISHSAVE, 0, null, null);
				}
					break;
				case MSG_CACHEDAPPS: {
					if (!mIsCacheHandled) {
						mIsCacheHandled = true;
						handleCachedAppsList();
					}
				}
					break;
				// case MSG_UNINSTALLAPPS: {
				// if (!mIsUninstallHandled) {
				// mIsUninstallHandled = true;
				// handleCacheUnInstallList();
				// }
				// }
				// break;
				case MSG_SDCARDAPPS: {
					if (!mIsSDHandled) {
						mIsSDHandled = true;
						handleCacheSDcardList();
					}
				}
					break;
				case MSG_FINISHSORT: {
					broadCast(SORTFINISH, 0, null, null);
				}
					break;
				case MSG_SORTFAILED: {
					DeskToast.makeText(mContext, R.string.sort_fail,
							Toast.LENGTH_SHORT).show();
				}
					break;
					case MSG_FINISHINIT : {
						final PreferencesManager manager = new PreferencesManager(mContext,
								IPreferencesIds.FOLDER_DATA_CORRUPTION, Context.MODE_PRIVATE);
						boolean isNeedClean = manager.getBoolean(CLEAN_FOLDER_DATA, false);
						boolean isCheckedData = manager.getBoolean(CHECK_FOLDER_DATA, false);
						if (!mIsInitFinish) {
							mIsInitFinish = true;
							startSave();
//							if (isNeedClean) {
//								confirmCleanDuplicate();
//							} else if (!isCheckedData && !GoLauncher.getContext().getFirstRun()) {
//								try {
//									manager.putBoolean(CHECK_FOLDER_DATA, true);
//									manager.commit();
//								} catch (Exception e) {
//									Log.i("FunCtroler", "MSG_FINISHINIT " + e.getMessage());
//								}
//								new Thread() {
//									@Override
//									public void run() {
//										try {
//											// 设置当前线程优先级为android系统提供的后台线程优先级，避免与主线程抢占资源
//											Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
//											checkDuplicateData();
//										} catch (Exception e) {
//											Log.i("FunCtroler", "MSG_FINISHINIT in thread " + e.getMessage());
//										}
//									}
//								}.start();
//							}
						} 
					}
					break;
				case MSG_BATADD: {
					PreferencesManager sharedPreferences = new PreferencesManager(
							mContext, IPreferencesIds.USERTUTORIALCONFIG,
							Context.MODE_PRIVATE);
					boolean needStarDragTutorial = sharedPreferences
							.getBoolean(
									IPreferencesIds.SHOULD_SHOW_APPFUNC_DRAG_GUIDE,
									true);
					if (needStarDragTutorial) {
						DeliverMsgManager
								.getInstance()
								.onChange(
										AppFuncConstants.TABCOMPONENT,
										AppFuncConstants.APP_COMP_SET_CURRENT_TAB_INDEX,
										GoLauncher.isPortait() ? AppFuncConstants.ALLAPPS
												: AppFuncConstants.PROCESSMANAGEMENT);
						DeliverMsgManager.getInstance().onChange(
								AppFuncConstants.ALLAPPS_GRID,
								AppFuncConstants.TUTORIAL_DRAG_MODE, null);
					}
					synchronized (mLock) {
						broadCast(BATADD, 0, null, null);
						mLock.notify();
					}
				}
					break;
				case FINISHLOADINGSDCARD: {
					broadCast(FINISHLOADINGSDCARD, 0, null, null);
				}
				case STARTLOADINGAPP: {
					DeliverMsgManager.getInstance().onChange(
							AppFuncConstants.APPFUNCFRAME,
							AppFuncConstants.PROGRESSBAR_HIDE, null);
					DeliverMsgManager.getInstance().onChange(
							AppFuncConstants.APPFUNCFRAME,
							AppFuncConstants.SHOW_SWITCH_MENU, null);
				}
					break;
				case MSG_SHOW_CLEAN_DIALOG: {
					confirmCleanDuplicate();
				}
				default:
					break;
				}
			}
		};
	}
	private void restartGolauncher() {
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.RESTART_GOLAUNCHER,
				-1, null, null);
	}
	
	private void confirmCleanDuplicate() {
		DialogConfirm dialogConfirm = new DialogConfirm(mContext);
		dialogConfirm.show();
		dialogConfirm.setTitle(R.string.hint);
		dialogConfirm
				.setMessage(R.string.app_fun_folder_clean_rubbish_folder);
		dialogConfirm.setPositiveButton(R.string.sure,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						cleanDuplicatData();
					}

				});
		dialogConfirm.setNegativeButtonVisible(View.GONE);
	}
	
	private void cleanDuplicatData() {
		// 从数据库获取到数据列表
		ArrayList<FunItem> infos = mFunDataModel.getFunItems();

		// 所有应用intent，用于某些情况去重复图标
		HashMap<String, FunItemInfo> appMap = new HashMap<String, FunItemInfo>();
		// 所有文件夹中包含的数据（用于最后去掉重复图标的保护处理）
		boolean isDuplicate = false;
		// 重复的程序列表
		ArrayList<FunItemInfo> appDuplicateList = new ArrayList<FunItemInfo>();

		if (null != infos && infos.size() > 0) { // 数据库有数据
			// LogUnit.i("FunControler",
			// "initAllAppItemInfos() -- db has data");
			// 取出数据库的数据
			boolean isFolder = false;
			FunItem funItem = null;
			AppItemInfo appItemInfo = null;
			for (int i = 0; i < infos.size(); ++i) {
				funItem = infos.get(i);
				if (null == funItem) {
					continue;
				}
//				// 加入一个判断逻辑，看数据库索引和内存索引是否一致，若不一致，则说明数据乱了，需要同步数据库的索引,文件夹也需要同样处理
//				if (funItem.mIndex != i) {
//					funItem.mIndex = i;
//					try {
//						mFunDataModel.updateFunAppItem(funItem.mIntent, i);
//					} catch (DatabaseException e) {
//						e.printStackTrace();
//					}
//				}
				// TODO：application表中应用程序的folderId需要写明为0
				isFolder = 0 != funItem.mFolderId;
				// 不是文件夹
				if (!isFolder) {
					appItemInfo = mAppDataEngine.getAppItem(funItem.mIntent);
					// 程序
					FunAppItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
					if (appMap.containsKey(funItem.mIntent.toString())) {
						isDuplicate = true;
						appDuplicateList.add(funItemInfo);
					} else {
						appMap.put(funItem.mIntent.toString(), funItemInfo);
					}
				} else {
					// 文件夹
					FunFolderItemInfo funItemInfo = new FunFolderItemInfo(
							mFunDataModel, funItem.mTitle);
					// 设置文件夹id
					funItemInfo.setFolderId(funItem.mFolderId);
					// 文件夹的intent
					funItemInfo.setIntent(funItem.mIntent);
					// 获取文件夹中的图标
					ArrayList<FunAppItemInfo> funAppItemInfos = funItemInfo
							.getFunAppItemInfos();
					if (funAppItemInfos == null || funAppItemInfos.isEmpty()) {
						isDuplicate = true;
						cleanRubbishFolderDB(funItemInfo);
						continue;
					}
					// 排查重复
					ArrayList<FunAppItemInfo> duplicateInFolder = null;
					for (FunAppItemInfo funAppItemInfo : funAppItemInfos) {
						if (appMap.containsKey(funAppItemInfo.getIntent()
								.toString())) {
							isDuplicate = true;
							FunItemInfo exsitApp = appMap.get(funAppItemInfo
									.getIntent().toString());
							if (exsitApp instanceof FunAppItemInfo) {
								FunAppItemInfo firstDuplicateApp = (FunAppItemInfo) exsitApp;
								long inFolderID = firstDuplicateApp
										.getInWhitchFolder();
								// 表示已经在其他文件夹中存在
								if (inFolderID != 0) {
									// 则删除当前文件夹里面这个
									if (duplicateInFolder == null) {
										duplicateInFolder = new ArrayList<FunAppItemInfo>();
									}
									duplicateInFolder.add(funAppItemInfo);
								} else {
									appDuplicateList.add(exsitApp);
								}
							}
						} else {
							funAppItemInfo.setInWhitchFolder(funItemInfo
									.getFolderId());
							appMap.put(funAppItemInfo.getIntent().toString(),
									funAppItemInfo);
						}
					}
					if (duplicateInFolder != null) {
						try {
							funAppItemInfos.removeAll(duplicateInFolder);
							for (FunAppItemInfo info : duplicateInFolder) {
								mFunDataModel.removeFunAppFromFolder(
										funItemInfo.getFolderId(), info.getIntent());
							}
							if (funAppItemInfos == null
									|| funAppItemInfos.isEmpty()) {
								cleanRubbishFolderDB(funItemInfo);
								continue;
							}
						} catch (Exception e) {
							AppFuncExceptionHandler.handle(e);
						} finally {
							isFoundFolderRubbish(false);
						}
					}
					// 文件夹排查结束
				}
			}

			// 功能表和文件夹中的图标去重
			if (isDuplicate) {
				try {
					ArrayList<FunAppItemInfo> list = (ArrayList<FunAppItemInfo>) appDuplicateList
							.clone();
					mFunDataModel.removeFunAppItemInfosInDB(list);
					list.clear();
					appMap.clear();
				} catch (DatabaseException e) {
					AppFuncExceptionHandler.handle(e);
				}
				isFoundFolderRubbish(false);
				restartGolauncher();
			}
			infos = null;
		}
	}
	
	private void checkDuplicateData() {

		// 从数据库获取到数据列表
		ArrayList<FunItem> infos = mFunDataModel.getFunItems();

		// 所有应用intent，用于某些情况去重复图标
		HashMap<String, FunItemInfo> appMap = new HashMap<String, FunItemInfo>();
		// 所有文件夹中包含的数据（用于最后去掉重复图标的保护处理）
		boolean isDuplicate = false;

		if (null != infos && infos.size() > 0) { // 数据库有数据
			// LogUnit.i("FunControler",
			// "initAllAppItemInfos() -- db has data");
			// 取出数据库的数据
			boolean isFolder = false;
			FunItem funItem = null;
			AppItemInfo appItemInfo = null;
			for (int i = 0; i < infos.size(); ++i) {
				funItem = infos.get(i);
				if (null == funItem) {
					continue;
				}
				// TODO：application表中应用程序的folderId需要写明为0
				isFolder = 0 != funItem.mFolderId;
				// 不是文件夹
				if (!isFolder) {
					appItemInfo = mAppDataEngine.getAppItem(funItem.mIntent);
					// 程序
					FunAppItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
					if (appMap.containsKey(funItem.mIntent.toString())) {
						isDuplicate = true;
						break;
					} else {
						appMap.put(funItem.mIntent.toString(), funItemInfo);
					}
				} else {
					// 文件夹
					FunFolderItemInfo funItemInfo = new FunFolderItemInfo(
							mFunDataModel, funItem.mTitle);
					// 设置文件夹id
					funItemInfo.setFolderId(funItem.mFolderId);
					// 文件夹的intent
					funItemInfo.setIntent(funItem.mIntent);
					// 获取文件夹中的图标
					ArrayList<FunAppItemInfo> funAppItemInfos = funItemInfo
							.getFunAppItemInfos();
					if (funAppItemInfos == null || funAppItemInfos.isEmpty()) {
						isDuplicate = true;
						break;
					}
					// 排查重复
					for (FunAppItemInfo funAppItemInfo : funAppItemInfos) {
						if (appMap.containsKey(funAppItemInfo.getIntent()
								.toString())) {
							FunItemInfo exsitApp = appMap.get(funAppItemInfo
									.getIntent().toString());
							if (exsitApp instanceof FunAppItemInfo) {
								FunAppItemInfo firstDuplicateApp = (FunAppItemInfo) exsitApp;
								long inFolderID = firstDuplicateApp
										.getInWhitchFolder();
								// 表示已经在其他文件夹中存在
								if (inFolderID != 0) {
									// 则删除当前文件夹里面这个
								} else {
									isDuplicate = true;
									break;
								}
							}
						} else {
							funAppItemInfo.setInWhitchFolder(funItemInfo
									.getFolderId());
							appMap.put(funAppItemInfo.getIntent().toString(),
									funAppItemInfo);
						}
					}
					// 文件夹排查结束
				}
			}

			// 功能表中的图标去重
			if (isDuplicate) {
				appMap.clear();
				mHandler.sendEmptyMessage(MSG_SHOW_CLEAN_DIALOG);
			}
			infos = null;
		}
	}
	
	private void initAppConfigControlerHandler() {
		BroadCasterObserver appConfigCtrlObsever = new BroadCasterObserver() {
			@Override
			public void onBCChange(int msgId, int param, Object object,
					List objects) {
				switch (msgId) {
				case AppConfigControler.ADDHIDEITEM: {
					handleHideItem((Intent) object, true);
				}
					break;
				case AppConfigControler.ADDHIDEITEMS: {
					handleHideItems((ArrayList<Intent>) objects, true);
				}
					break;
				case AppConfigControler.DELETEHIDEITEM: {
					handleHideItem((Intent) object, false);
				}
					break;
				case AppConfigControler.DELETEHIDEITEMS: {
					handleHideItems((ArrayList<Intent>) objects, false);
				}
					break;
				default:
					break;
				}
			}
		};
		mAppConfigControler.registerObserver(appConfigCtrlObsever);
	}

	/**
	 * 是否正在处理数据
	 * 
	 * @return 是否正在处理
	 */
	public boolean isHandling() {
		return mIsHandling;
	}

	/**
	 * <br>
	 * 功能简述:获取功能表所有程序界面程序列表数据（包括程序和文件夹）TODO:未进功能表就监控到安装卸载事件 <br>
	 * 功能详细描述:采用了异步加载机制，如果数据没有加载完成获取到的有可能不是所有数据。 <br>
	 * 注意:
	 * 
	 * @return
	 */
	public final ArrayList<FunItemInfo> getFunAppItems() {
		// LogUnit.i("FunControler", "getFunAppItems()");
		if (mHasInit) {
			// LogUnit.i("FunControler", "getFunAppItems() -- mHasInit");
			return mAllAppItemInfos;
		}

		if (mHasStartInit) {
			// LogUnit.i("FunControler", "getFunAppItems() -- mHasStartInit");
			return mAllAppItemInfos;
		}

		// 先从AppDataEngine获取4个应用程序用于第一次显示
		// 注释by敖日明 2011-7-23 16:53
		// 在屏幕层load完之后，会开始load功能表里面的图标
		// 这里load16个图标，但并不是第一屏对应的16个，因此没有必要，而且，这里是同步load！
		// initFirst16AppItemInfos();
		// 提示加载中
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
				AppFuncConstants.PROGRESSBAR_SHOW, null);
		// /再另启线程进行具体的图标比对
		startInitThread();

		return mAllAppItemInfos;
	}

	/**
	 * <br>
	 * 功能简述:重新开始异步加载一遍所有程序数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public final void reloadFunAppItems() {
		startInitThread();
	}

	/**
	 * 获取功能表图标，不包括已存在于文件夹的图标，也不包括文件夹和已经被隐藏的图标
	 * 
	 * @return 图标数组，需要使用者释放
	 */
	public final ArrayList<FunAppItemInfo> getFunAppItemsExceptFolder() {
		final ArrayList<FunItemInfo> appItems = mAllAppItemInfos;
		ArrayList<FunAppItemInfo> appItemsExceptFolder = new ArrayList<FunAppItemInfo>();
		int size = appItems.size();
		FunItemInfo info = null;
		for (int i = 0; i < size; ++i) {
			info = appItems.get(i);
			if ((info.getType() == FunItemInfo.TYPE_APP) && (!info.isHide())) {
				appItemsExceptFolder.add((FunAppItemInfo) info);
			}
		}
		return appItemsExceptFolder;
	}

	/**
	 * 获取功能表文件夹列表
	 * 
	 * @return
	 */
	public ArrayList<FunFolderItemInfo> getFunFolders() {
		if (mAllAppItemInfos == null) {
			return null;
		} else {
			final ArrayList<FunItemInfo> appItems = (ArrayList<FunItemInfo>) mAllAppItemInfos
					.clone();
			ArrayList<FunFolderItemInfo> folderList = new ArrayList<FunFolderItemInfo>();
			int size = appItems.size();
			FunItemInfo info = null;
			for (int i = 0; i < size; ++i) {
				info = appItems.get(i);
				if (info != null && info.getType() == FunItemInfo.TYPE_FOLDER) {
					folderList.add((FunFolderItemInfo) info);
				}
			}
			return folderList;
		}
	}

	private void startInitThread() {
		mHasStartInit = true;
		// 先异步加载图标和名称:已经调整到桌面启动时
		mAppDataEngine.startLoadCompletedData();

		Thread thread = new Thread(ThreadName.FUNC_INIT_DATA) {
			@Override
			public void run() {
				// 设置当前线程优先级为android系统提供的后台线程优先级，避免与主线程抢占资源
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				// LogUnit.i("FunControler", "startInitThread() -- run()");
				// 检查是否要更新
				boolean toUpdate = mStateParaphrase.checkUpdate();
				// 初始化功能表数据
				synchronized (mLock) {
					initAllAppItemInfos(mAllAppItemInfos, toUpdate);
				}
				mHasInit = true;
				// LogUnit.i("FunControler",
				// "startInitThread() -- run() mHasInit = true");

				// 通知已初加载完图标和title且未发送过加载完毕消息，可发送消息执行保存线程了
				if (mIsIconTitleLoadFinish == false) {
					// 主动询问AppDataEngine程序名称是否已经加载完毕，因为此部分数据在AppDataEngine
					// 中是静态的，在快速退出再进入时，此部分数据可能还未释放
					if (mAppDataEngine.isLoadedCompletedData()) {
						mIsIconTitleLoadFinish = true;
					}
				}
				if (mIsIconTitleLoadFinish && !mIsInitFinish) {
					Message message = mHandler.obtainMessage();
					message.what = MSG_FINISHINIT;
					mHandler.sendMessage(message);
				}

				// 把通讯统计保存的map信息增加到列表中
				// updateAppUnread(mAllAppItemInfos, mNotificationMap);

				Object mLocker = mAppFuncUtils.getLock();
				synchronized (mLocker) {
					mLocker.notify();
				}
			}
		};
		// thread.setPriority(4);
		thread.start();
	}

	private void startSave() {
		// 若第一次加载或表中没数据(可能之前保存失败)，则存入数据库
		if (mFunDataModel.isNewDB() || 0 == mFunDataModel.getSizeOfApps()) {
			startSaveThread();
		} else {
			// 检测是否在进功能表之前有改变排序设置
			if (mSortChanged) {
				startSaveThread();
			} else {
				// 通知处理缓存数据
				sendToHandleCacheData();
				mIsHandling = false;
				// DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
				// AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR, null);
			}

		}
	}

	private void startSaveThread() {
		new Thread(ThreadName.FUNC_INIT_SORT) {
			@Override
			public void run() {
				// 通知开始保存
				Message message = mHandler.obtainMessage();
				message.what = MSG_STARTSAVE;
				mHandler.sendMessage(message);

				// 按照设置进行排序
				int sortType = GOLauncherApp.getSettingControler()
						.getFunAppSetting().getSortType();
				switch (sortType) {
				case FunAppSetting.SORTTYPE_LETTER:
					sortByLetter("ASC");
					break;
				case FunAppSetting.SORTTYPE_TIMENEAR:
					sortByTime("DESC");
					break;
				case FunAppSetting.SORTTYPE_TIMEREMOTE:
					sortByTime("ASC");
					break;
				case FunAppSetting.SORTTYPE_FREQUENCY:
					sortByTime("DESC");
					break;
				default:
					sortByLetter("ASC");
					break;
				}
				// 清除原来的排序
				refreshIndex();
				// 通知
				message = mHandler.obtainMessage();
				message.what = MSG_FINISHSORT;
				mHandler.sendMessage(message);

				// 保存
				saveToDB();

				// 通知保存完毕
				message = mHandler.obtainMessage();
				message.what = MSG_FINISHSAVE;
				mHandler.sendMessage(message);

				// 通知处理缓存数据
				sendToHandleCacheData();

				mIsHandling = false;
				// DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
				// AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR, null);
			}
		}.start();
	}

	/**
	 * 通知处理缓存数据
	 */
	private void sendToHandleCacheData() {
		// 处理缓存池中的安装卸载数据及sd卡
		Message message = mHandler.obtainMessage();
		message = mHandler.obtainMessage();
		message.what = MSG_CACHEDAPPS;
		mHandler.sendMessage(message);

		// message = mHandler.obtainMessage();
		// message.what = MSG_UNINSTALLAPPS;
		// mHandler.sendMessage(message);

		message = mHandler.obtainMessage();
		message.what = MSG_SDCARDAPPS;
		mHandler.sendMessage(message);
	}

	/**
	 * 添加隐藏程序
	 * 
	 * @param intent
	 * @throws DatabaseException
	 */
	public void addHideAppItem(final Intent intent) throws DatabaseException {
		mAppConfigControler.addHideAppItem(intent, true);
	}

	/**
	 * 删除隐藏程序
	 * 
	 * @param intent
	 * @throws DatabaseException
	 */
	public void delHideAppItem(final Intent intent) throws DatabaseException {
		mAppConfigControler.delHideAppItem(intent, true);
	}

	/**
	 * 初始化功能表数据
	 * 
	 * @param funItemInfos
	 * @param updateOldData
	 */
	private void initAllAppItemInfos(ArrayList<FunItemInfo> funItemInfos,
			boolean updateOldData) {
		// LogUnit.i("FunControler", "initAllAppItemInfos() -- updateOldData = "
		// + updateOldData);
		// // 先异步加载图标和名称
		// mAppDataEngine.startLoadCompletedData();

		// 从数据库获取到数据列表
		ArrayList<FunItem> infos = mFunDataModel.getFunItems();
		// 从mAppDataEngine中取出来的手机上安装的程序数据
		ArrayList<AppItemInfo> memAppItemInfos = mAppDataEngine
				.getAllAppItemInfos();

		// 要添加的，手机里的新数据
		ArrayList<AppItemInfo> toAddItemInfos = (ArrayList<AppItemInfo>) memAppItemInfos
				.clone();
		// 所有的文件夹id
		ArrayList<Long> folderIds = new ArrayList<Long>();
		// 文件夹缓存数据
		ArrayList<FunFolderItemInfo> funFolderItemInfos = new ArrayList<FunFolderItemInfo>();

		// 根据数据列表从AppDataEngine获取到真正的数据

		// 清除旧数据
		funItemInfos.clear();

		// 通知开始加载数据 清除ProgressDialog
		Message startmessage = mHandler.obtainMessage();
		startmessage.what = STARTLOADINGAPP;
		mHandler.sendMessage(startmessage);

		if (null != infos && infos.size() > 0) { // 数据库有数据
			// LogUnit.i("FunControler",
			// "initAllAppItemInfos() -- db has data");
			// 取出数据库的数据
			boolean isFolder = false;
			FunItem funItem = null;
			AppItemInfo appItemInfo = null;
			for (int i = 0; i < infos.size(); ++i) {
				funItem = infos.get(i);
				if (null == funItem) {
					continue;
				}

				// 加入一个判断逻辑，看数据库索引和内存索引是否一致，若不一致，则说明数据乱了，需要同步数据库的索引,文件夹也需要同样处理
				if (funItem.mIndex != i) {
					funItem.mIndex = i;
					try {
						mFunDataModel.updateFunAppItem(funItem.mIntent, i);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				}

				// TODO：application表中应用程序的folderId需要写明为0
				isFolder = 0 != funItem.mFolderId;
				// 不是文件夹
				if (!isFolder) {
					// LogUnit.i("FunControler",
					// "initAllAppItemInfos() -- !isFolder");

					appItemInfo = mAppDataEngine.getAppItem(funItem.mIntent);
					if (null == appItemInfo) {
						if (updateOldData) {
							// 如果删除数据库中的旧数据
							// 从内存中删除
							mFunDataModel.removeFunItem(infos, i);
							--i;
							// 从数据库中删除
							try {
								mFunDataModel
										.removeFunappItemInfo(funItem.mIntent);
							} catch (DatabaseException e) {
								e.printStackTrace();
							}
							// 从隐藏名单中去除
							// mAppConfigControler.delHideAppItem(funItem.mIntent);
							continue;

						} else {

							appItemInfo = new AppItemInfo();
							appItemInfo.mIntent = funItem.mIntent;
							appItemInfo.mIcon = /*
												 * (BitmapDrawable)
												 * mContext.getApplicationContext
												 * ().
												 * getResources().getDrawable
												 * (android
												 * .R.drawable.sym_def_app_icon)
												 */mAppDataEngine
									.getSysBitmapDrawable();
							appItemInfo.setIsTemp(true);
						}
					}
					// 根据渠道配置信息，把应用游戏中心的两个假图标移除
					// 这种场景主要发生在用户从有两个中心的假图标的渠道包升级到没有两个中心的渠道包
					// add by wangzhuobin 2012.07.20
					if (ConfigUtils
							.isNeedCheckAppGameInFunItemByChannelConfig()) {
						// 只有不需要在功能表添加应用中心或者游戏中心图标的时候，我们才做这样的检查和删除操作
						if (ConfigUtils
								.isNeedRemoveAppGameFromFunByChannelConfig(appItemInfo)) {
							// 从数据库和内存中移除应用游戏中心的图标信息
							// 从内存中删除
							mFunDataModel.removeFunItem(infos, i);
							--i;
							// 从数据库中删除
							try {
								mFunDataModel
										.removeFunappItemInfo(funItem.mIntent);
							} catch (DatabaseException e) {
								e.printStackTrace();
							}
							continue;
						}
					}

					// 加入一个判断，看是否文件夹中已有的数据若是则不从暂存列表中移除同时不加入功能表根目录

					// 不是手机里的新数据，从暂存的列表中移除
					toAddItemInfos.remove(appItemInfo);

					// 若title没load好,使用保存起来的名称
					if (null == appItemInfo.mTitle) {
						appItemInfo.mTitle = funItem.mTitle;
						if (null == funItem.mTitle) {
							// TODO:
							appItemInfo.mTitle = "Loading...";
						}
					}

					// 程序
					FunAppItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
					funItemInfo.setIndex(funItem.mIndex);
					funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
					// 初始化未读数，数据库未有数据时，说明第一次安装，不用加入该行
					mFunDataModel.setAppUnread(funItemInfo);
					// 添加到功能表列表里
					funItemInfos.add(funItemInfo);
					// LogUnit.i("FunControler",
					// "initAllAppItemInfos() -- add app end");
				} else {
					// LogUnit.i("FunControler",
					// "initAllAppItemInfos() -- isFolder");
					// 文件夹
					FunFolderItemInfo funItemInfo = new FunFolderItemInfo(
							mFunDataModel, funItem.mTitle);
					// 设置文件夹id
					funItemInfo.setFolderId(funItem.mFolderId);
					// 文件夹的intent
					funItemInfo.setIntent(funItem.mIntent);
					// 放到文件夹列表
					folderIds.add(funItem.mFolderId);
					// 放到文件夹缓存数据中
					funFolderItemInfos.add(funItemInfo);
					funItemInfo.setIndex(funItem.mIndex);
					// 获取文件夹中的图标
					ArrayList<FunAppItemInfo> funAppItemInfos = funItemInfo
							.getFunAppItemInfos();
					if (funAppItemInfos == null || funAppItemInfos.isEmpty()) {
						isFoundFolderRubbish(true);
						continue;
					}
					// 根据渠道配置信息，把应用游戏中心的两个假图标从文件夹中移除
					// 这种场景主要发生在用户从有两个中心的假图标的渠道包升级到没有两个中心的渠道包
					// add by wangzhuobin 2012.07.20
					if (ConfigUtils
							.isNeedCheckAppGameInFunItemByChannelConfig()) {
						// 只有不需要在功能表添加应用中心或者游戏中心图标的时候，我们才做这样的检查和删除操作
						ArrayList<FunAppItemInfo> removeItemInfos = new ArrayList<FunAppItemInfo>();
						for (FunAppItemInfo itemInfo : funAppItemInfos) {
							// 先找出两个图标对应的FunAppItemInfo
							if (ConfigUtils
									.isNeedRemoveAppGameFromFunByChannelConfig(itemInfo)) {
								removeItemInfos.add(itemInfo);
							}
						}
						for (FunAppItemInfo itemInfo : removeItemInfos) {
							// 从内存和数据库中删除
							funAppItemInfos.remove(itemInfo);
							try {
								funItemInfo.removeFunAppItemInfo(itemInfo,
										false);
							} catch (DatabaseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					// 把信息加入功能表列表里
					funItemInfos.add(funItemInfo);
					// 文件夹排查结束
					// LogUnit.i("FunControler",
					// "initAllAppItemInfos() -- add folder end");
				}

				// 每加载x个通知1次
				if ((i + 1) % 32 == 0) {
					// 通知分批添加了
					Message message = mHandler.obtainMessage();
					message.what = MSG_BATADD;
					mHandler.sendMessage(message);
					try {
						mLock.wait();
						// Thread.currentThread();
						// Thread.sleep(100);
					} catch (InterruptedException e) {
						// Do nothing
					}

				}
			}
			// 新安装的应用程序之后再统一处理
			if (toAddItemInfos != null) {
				for (int i = 0; i < toAddItemInfos.size();) {
					AppItemInfo info = toAddItemInfos.get(i);
					if (isNewInstall(info)) {
						toAddItemInfos.remove(info);
						continue;
					} else {
						i++;
					}
				}
			}

			// 检测应用中心、游戏中心功能表图标不存在的情况，这种情况通常发生在用户升级到此版本，或者用户删除了这组图标：触发排序，仍旧把应用中心、游戏中心图标放到第一第二位
			// 注：用户删除了图标之后，另有记录标记处理，不会触发这里的处理
			// Add by songzhaochun, 2012.06.14

			// 用户从没有应用游戏中心的版本升级到该版本时，就会在功能表添加应用游戏中心的两个假图标
			// 这时toAddItemInfos就会有两个假图标的ItemInfo
			// 我们希望把这两个假图标放在前两位，但是不能触发排序。因为用户在原来的版本有自己的图标顺序，升级上来后我们不能改变他原来的排序。
			// DEBUG FOR : ADT-6433
			// Add by wangzhuobin, 2012.07.11

			// 先查找一下toAddItemInfos里面有没有两个中心的图标
			boolean found = false;
			if (toAddItemInfos != null) {
				if (!found) {
					for (AppItemInfo app : toAddItemInfos) {
						if (app != null
								&& app.mIntent != null
								&& ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER
										.equals(app.mIntent.getAction())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					for (AppItemInfo app : toAddItemInfos) {
						if (app != null
								&& app.mIntent != null
								&& ICustomAction.ACTION_FUNC_SHOW_GAMECENTER
										.equals(app.mIntent.getAction())) {
							found = true;
							break;
						}
					}
				}
			}

			// 将手机的新数据写到数据库(只添加文件夹里没有的)
			mFunDataModel.addInList(funItemInfos, folderIds,
					funFolderItemInfos, toAddItemInfos, true);

			// Add by wangzhuobin, 2012.07.11
			if (found) {
				// 如果有两个中心的图标
				// 经过上面的步骤后，那两个图标已经添加到功能表的最后了，现在我们把它从内存和数据库中提到最前面来
				try {
					// 先更新内存集合位置
					buildSpecialItemsOrder();
					// 刷新内存下标
					refreshIndex();
					// 更新数据库
					mFunDataModel.updateFunAppItemsIndex(funItemInfos);
				} catch (DatabaseException e) {
					// 如果更新数据库出错，就让它们放在最后吧
					e.printStackTrace();
				}
			}

			infos = null;

			// 置空通讯统计缓存map,新安装应用不会有对应的未读数信息
			mFunDataModel.setNotificationMap(null);

			// 通知分批添加了
			Message message = mHandler.obtainMessage();
			message.what = MSG_BATADD;
			mHandler.sendMessage(message);
		} else { // 数据库没有数据
			// LogUnit.i("FunControler",
			// "initAllAppItemInfos() -- db has no data");

			ArrayList<AppItemInfo> appItemInfos = null;
			appItemInfos = mAppDataEngine.getAllAppItemInfos();

			AppItemInfo appItemInfo = null;
			int size = appItemInfos.size();
			for (int i = 0; i < size; ++i) {
				appItemInfo = appItemInfos.get(i);
				if (null == appItemInfo) {
					continue;
				}
				// 新安装的程序之后处理
				if (isNewInstall(appItemInfo)) {
					continue;
				}
				FunAppItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
				funItemInfo.setIndex(i);
				funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
				funItemInfos.add(funItemInfo);
				mFunDataModel.setAppUnread(funItemInfo);
				// LogUnit.i("FunControler",
				// "initAllAppItemInfos() -- add app end");
				// 每加载x个通知1次
				if ((i + 1) % 32 == 0) {
					// 通知分批添加了
					Message message = mHandler.obtainMessage();
					message.what = MSG_BATADD;
					mHandler.sendMessage(message);

					try {
						mLock.wait();
						// Thread.currentThread();
						// Thread.sleep(100);
					} catch (InterruptedException e) {
						// Do nothing
					}
				}
			}
			appItemInfo = null;

			// 通知分批添加了
			Message message = mHandler.obtainMessage();
			message.what = MSG_BATADD;
			mHandler.sendMessage(message);
		}
		// LogUnit.i("FunControler", "initAllAppItemInfos() -- end");
	}

	private void cleanRubbishFolderDB(FunItemInfo funItemInfo) {
		try {
			mFunDataModel.removeFunappItemInfo(funItemInfo.getIntent());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void isFoundFolderRubbish(boolean isNeed) {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.FOLDER_DATA_CORRUPTION, Context.MODE_PRIVATE);
		manager.putBoolean(CLEAN_FOLDER_DATA, isNeed);
		manager.commit();
	}

	/**
	 * 处理sd卡程序，更新内存及数据库
	 */
	private synchronized void handleSDAppItems(
			final ArrayList<AppItemInfo> appItemInfos, final boolean updateData) {

		new Thread() {
			@Override
			public void run() {
				synchronized (mLock) {
					if (null == appItemInfos) {
						return;
					}
					// 所有的文件夹
					ArrayList<Long> folderIds = new ArrayList<Long>();
					// 缓存数据
					ArrayList<AppItemInfo> toAddItemInfos = (ArrayList<AppItemInfo>) appItemInfos
							.clone();

					if (mHasInit) {
						AppItemInfo appItemInfo = null;
						FunItemInfo funItemInfo = null;
						// TODO:线程安全性
						int size = mAllAppItemInfos.size();
						for (int i = size - 1; i >= 0; --i) {
							// LogUnit.i("FunControler",
							// "handleSDAppItems() --  mAllAppItemInfos.get(i)"
							// + mAllAppItemInfos.get(i));
							funItemInfo = mAllAppItemInfos.get(i);
							if (null == funItemInfo) {
								continue;
							}

							// 若是文件夹
							if (0 != funItemInfo.getFolderId()) {
								// if (FunItemInfo.TYPE_FOLDER ==
								// funItemInfo.getType()) {
								// LogUnit.i("FunControler",
								// "handleSDAppItems() -- FunItemInfo.TYPE_FOLDER == funItemInfo.getType()");
								// TODO:去除此行
								folderIds.add(funItemInfo.getFolderId());
								// 处理文件夹中的
								((FunFolderItemInfo) funItemInfo)
										.handleSDAppItems(toAddItemInfos,
												updateData);
								continue;
							}

							appItemInfo = ((FunAppItemInfo) funItemInfo)
									.getAppItemInfo();
							if (null == appItemInfo) {
								continue;
							}

							int idx = findInList(toAddItemInfos,
									appItemInfo.mIntent);
							// 不是暂存数据
							if (!appItemInfo.isTemp()) {
								// LogUnit.i("FunControler",
								// "handleSDAppItems() -- !appItemInfo.isTemp()");
								// 若功能表列表中已存在，则从添加列表中删除
								if (idx >= 0) {
									// 从toAddItemInfos中删除
									// LogUnit.i("FunControler",
									// "handleSDAppItems() -- idx >= 0");
									toAddItemInfos.remove(idx);
								}
								continue;
							}

							if (idx >= 0) {
								// 若在sd卡程序数组中
								// 更新数据
								AppItemInfo info = mFunDataModel
										.getAppItem(appItemInfo.mIntent);
								((FunAppItemInfo) funItemInfo)
										.setAppItemInfo(info);
								// 从toAddItemInfos中删除
								toAddItemInfos.remove(idx);
								// LogUnit.i("FunControler",
								// "handleSDAppItems() -- toAddItemInfos.remove(idx)");
							} else {
								if (updateData) {
									// 不在sd卡程序数组中,从内存及数据库中移除
									try {
										removeFunAppItemInfo(i, true);
									} catch (DatabaseException e) {
										e.printStackTrace();
									}
									// mAppConfigControler.delHideAppItem(appItemInfo.mIntent);
									// LogUnit.i("FunControler",
									// "handleSDAppItems() -- removeFunAppItemInfo(i, true)");
								}
							}
						}
					} else {
						// LogUnit.i("FunControler",
						// "handleSDAppItems() -- mFunDataModel.getFolderIds()");
						// 从数据库中取:桌面也用folder表，因此此方法有问题
						folderIds = mFunDataModel.getFolderIds();
					}

					if (updateData) {
						// LogUnit.i("FunControler",
						// "handleSDAppItems() -- if (updateData)");
						// 将sd卡数组infos剩余的元素(如果这些元素不存在于文件夹中)添加到内存及数据库中TODO:处理没进功能表时的情况
						mFunDataModel.addInList(mAllAppItemInfos, folderIds,
								null, toAddItemInfos, true);
						// 移除文件夹中已被卸载的程序
						// Log.i("FunControler",
						// "-------------------checkFolders");
						checkFolders(folderIds);
					}

					// 通知sd卡数据已加载好

					Message message = mHandler.obtainMessage();
					message.what = FINISHLOADINGSDCARD;
					mHandler.sendMessage(message);
				}
			}
		}.start();
		// LogUnit.i("FunControler", "handleSDAppItems() -- mHasInit" +
		// mHasInit);

	}

	/**
	 * 在文件夹中删除已卸载的程序(删除数据库中的)
	 * 
	 * @param folderIds
	 *            文件夹id列表
	 * @param intent
	 *            唯一标识Intent
	 */
	private void handleRemoveInfolders(final ArrayList<Long> folderIds,
			final Intent intent) {
		if (null == intent || null == folderIds) {
			return;
		}

		// LogUnit.i("FunControler", "handleRemoveInfolders()");
		int size = folderIds.size();
		for (int i = 0; i < size; ++i) {
			// 删除数据库中的
			try {
				mFunDataModel.removeFunAppFromFolder(folderIds.get(i), intent);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 更新文件夹中的元素
	 * 
	 * @param folderIds
	 *            文件夹id列表
	 */
	private void checkFolders(ArrayList<Long> folderIds) {
		// LogUnit.i("FunControler", "checkFolders()");
		int size = folderIds.size();
		for (int i = 0; i < size; ++i) {
			// TODO:内存中的
			// TODO:单独提取update部分
			// 数据库中的
			FunItemInfo itemInfo = getFunAppItemInfo(folderIds.get(i));
			mFunDataModel.getAppsInFolder(folderIds.get(i), true, itemInfo);
		}
	}

	/**
	 * 是否存在于数据中
	 * 
	 * @param appItemInfos
	 *            数组
	 * @param intent
	 *            唯一标识Intent
	 * @return 是否存在
	 */
	private int findInList(final ArrayList<AppItemInfo> appItemInfos,
			Intent intent) {
		if (null == appItemInfos || null == intent) {
			return -1;
		}

		AppItemInfo appItemInfo = null;
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			appItemInfo = appItemInfos.get(i);
			if (null == appItemInfo) {
				continue;
			}

			if (ConvertUtils.intentCompare(intent, appItemInfo.mIntent)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 取得元素个数
	 * 
	 * @return 个数
	 */
	public int size() {
		return mAllAppItemInfos.size();
	}

	/**
	 * 根据folderId获取文件夹
	 * 
	 * @param folderId
	 *            文件夹id
	 * @return 文件夹
	 */
	public FunItemInfo getFunAppItemInfo(final long folderId) {
		FunItemInfo info = null;
		for (int i = 0; i < mAllAppItemInfos.size(); ++i) {
			info = mAllAppItemInfos.get(i);
			if (null == info) {
				continue;
			}

			// 若不是文件夹
			if (FunItemInfo.TYPE_APP == info.getType()) {
				continue;
			}

			if (info.getFolderId() == folderId) {
				return info;
			}
		}
		return null;
	}

	/**
	 * 根据intent获取到特定的元素
	 * 
	 * @param intent
	 * @return FunAppItemInfo
	 */
	public FunItemInfo getFunItemInfo(Intent intent) {
		FunItemInfo info = null;
		for (int i = 0; i < mAllAppItemInfos.size(); ++i) {
			info = mAllAppItemInfos.get(i);
			if (null == info) {
				continue;
			}

			// 比对唯一标识
			Intent it = info.getIntent();
			if (ConvertUtils.intentCompare(it, intent)) {
				return info;
			}
		}
		return null;
	}

	/**
	 * 在列表中找出元素
	 * 
	 * @param funItemInfo
	 *            要查找的元素
	 * @return index 索引
	 */
	public int findInList(FunItemInfo funItemInfo) {
		FunItemInfo info = null;
		int count = mAllAppItemInfos.size();
		for (int i = 0; i < count; ++i) {
			info = mAllAppItemInfos.get(i);
			if (funItemInfo == info) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 按时间排序，并保存到数据库
	 * 
	 * @param order
	 * @throws DatabaseException
	 */
	public synchronized void sortByTimeAndSave(String order)
			throws DatabaseException {
		if (!mHasInit) {
			return;
		}

		// LogUnit.i("FunControler", "sortByTimeAndSave()");
		// 排序
		sortByTime(order);
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		mFunDataModel.updateFunAppItemsIndex(mAllAppItemInfos);
		// 文件夹中排序
		// sortFoldersByTimeAndSave(order);
		// 通知
		Message message = mHandler.obtainMessage();
		message.what = MSG_FINISHSORT;
		mHandler.sendMessage(message);
	}

	private void sortFoldersByTimeAndSave(String order) {
		if (!mHasInit) {
			return;
		}

		int size = mAllAppItemInfos.size();
		FunItemInfo funItemInfo = null;
		for (int i = 0; i < size; ++i) {
			funItemInfo = mAllAppItemInfos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			if (funItemInfo.getType() == FunItemInfo.TYPE_APP) {
				continue;
			}

			((FunFolderItemInfo) funItemInfo)
					.sortByTimeAndSave(mContext, order);
		}
	}

	private void sortFoldersByLetterAndSave(String order) {
		if (!mHasInit) {
			return;
		}

		int size = mAllAppItemInfos.size();
		FunItemInfo funItemInfo = null;
		for (int i = 0; i < size; ++i) {
			funItemInfo = mAllAppItemInfos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			if (funItemInfo.getType() == FunItemInfo.TYPE_APP) {
				continue;
			}

			((FunFolderItemInfo) funItemInfo).sortByLetterAndSave(order);
		}
	}

	/**
	 * 按字母排序，并保存到数据库
	 * 
	 * @param order
	 * @throws DatabaseException
	 */
	public synchronized void sortByLetterAndSave(String order)
			throws DatabaseException {
		if (!mHasInit) {
			return;
		}

		// LogUnit.i("FunControler", "sortByLetterAndSave()");
		// 排序
		sortByLetter(order);
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		mFunDataModel.updateFunAppItemsIndex(mAllAppItemInfos);
		// 文件夹中排序
		// sortFoldersByLetterAndSave(order);
		// 通知
		Message message = mHandler.obtainMessage();
		message.what = MSG_FINISHSORT;
		mHandler.sendMessage(message);
	}

	/**
	 * 清除原来的排序
	 */
	private void refreshIndex() {
		FunItemInfo funItemInfo = null;
		for (int i = 0; i < mAllAppItemInfos.size(); ++i) {
			funItemInfo = mAllAppItemInfos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			funItemInfo.setIndex(i);
		}
	}

	/**
	 * 设置特殊图标的顺序
	 */
	void buildSpecialItemsOrder() {
		//
		for (int i = 0; i < mAllAppItemInfos.size(); i++) {
			FunItemInfo info = mAllAppItemInfos.get(i);
			if (null == info || null == info.getIntent()) {
				continue;
			}
			if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDLIST.equals(info
					.getIntent().getAction())) {
				for (int j = i - 1; j >= 0; j--) {
					mAllAppItemInfos.set(j + 1, mAllAppItemInfos.get(j));
				}
				mAllAppItemInfos.set(0, info);
				break;
			}
		}
		//
		for (int i = 0; i < mAllAppItemInfos.size(); i++) {
			FunItemInfo info = mAllAppItemInfos.get(i);
			if (null == info || null == info.getIntent()) {
				continue;
			}
			if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDGAME.equals(info
					.getIntent().getAction())) {
				for (int j = i - 1; j >= 0; j--) {
					mAllAppItemInfos.set(j + 1, mAllAppItemInfos.get(j));
				}
				mAllAppItemInfos.set(0, info);
				break;
			}
		}
		// 游戏中心
		for (int i = 0; i < mAllAppItemInfos.size(); i++) {
			FunItemInfo info = mAllAppItemInfos.get(i);
			if (null == info || null == info.getIntent()) {
				continue;
			}
			if (ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(info
					.getIntent().getAction())) {
				for (int j = i - 1; j >= 0; j--) {
					mAllAppItemInfos.set(j + 1, mAllAppItemInfos.get(j));
				}
				mAllAppItemInfos.set(0, info);
				break;
			}
		}
		// 应用中心
		for (int i = 0; i < mAllAppItemInfos.size(); i++) {
			FunItemInfo info = mAllAppItemInfos.get(i);
			if (null == info || null == info.getIntent()) {
				continue;
			}
			if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(info
					.getIntent().getAction())) {
				for (int j = i - 1; j >= 0; j--) {
					mAllAppItemInfos.set(j + 1, mAllAppItemInfos.get(j));
				}
				mAllAppItemInfos.set(0, info);
				break;
			}
		}
	}

	private void sortByLetter(String order) {
		String sortMethod = "getTitle";
		String pritMethod = "isPriority";
		try {
			SortUtils.sortForApps(mContext, mAllAppItemInfos, pritMethod,
					sortMethod, null, null, order);
			buildSpecialItemsOrder();
		} catch (Exception e) {
			// 弹出提示排序失败
			Message message = mHandler.obtainMessage();
			message.what = MSG_SORTFAILED;
			mHandler.sendMessage(message);
		}
	}

	private void sortByTime(String order) {
		String sortMethod = "getTime";
		String pritMethod = "isPriority";
		PackageManager packageMgr = mContext.getPackageManager();
		Class[] methodArgClasses = new Class[] { PackageManager.class };
		Object[] methodArg = new Object[] { packageMgr };
		try {
			SortUtils.sortSomePriority(mAllAppItemInfos, pritMethod,
					sortMethod, methodArgClasses, methodArg, order,
					SortUtils.COMPARE_TYPE_LONG);
			buildSpecialItemsOrder();
		} catch (Exception e) {
			// 弹出提示排序失败
			Message message = mHandler.obtainMessage();
			message.what = MSG_SORTFAILED;
			mHandler.sendMessage(message);
		}
	}

	/**
	 * 保存到数据库
	 * 
	 * @throws DatabaseException
	 */
	private void saveToDB() {
		try {
			mFunDataModel.beginTransaction();
			// 清空数据
			mFunDataModel.clearFunAppItems();

			// 添加数据到数据库
			mFunDataModel.addFunAppItemInfos(mAllAppItemInfos);
			mFunDataModel.setTransactionSuccessful();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			mFunDataModel.endTransaction();
		}
	}

	/**
	 * 保存顺序到数据库
	 * 
	 * @throws DatabaseException
	 */
	@Deprecated
	private void saveSequenceToDB() throws DatabaseException {
		// 更新排序到数据库
		Intent intent = null;
		AppItemInfo appItemInfo = null;
		FunItemInfo funItemInfo = null;
		try {
			mFunDataModel.beginTransaction();
			for (int i = 0; i < mAllAppItemInfos.size(); ++i) {
				funItemInfo = mAllAppItemInfos.get(i);
				if (null == funItemInfo) {
					continue;
				}

				// 获取唯一标识Intent
				if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
					intent = funItemInfo.getIntent();
				} else {
					appItemInfo = ((FunAppItemInfo) funItemInfo)
							.getAppItemInfo();
					if (null == appItemInfo) {
						continue;
					}
					intent = appItemInfo.mIntent;
				}

				if (null == intent) {
					continue;
				}

				// 更新元素的index
				mFunDataModel.updateFunAppItem(intent, funItemInfo.getIndex());
				mFunDataModel.setTransactionSuccessful();
			}
		} finally {
			mFunDataModel.endTransaction();
		}
	}

	/**
	 * 添加一个元素（包括程序和文件夹）
	 * 
	 * @param index
	 *            添加位置
	 * @param funItemInfo
	 *            元素
	 * @throws DatabaseException
	 */
	public synchronized FunItemInfo addFunAppItemInfo(int index,
			FunItemInfo funItemInfo) throws DatabaseException {
		// LogUnit.i("FunControler", "addFunAppItemInfo()");
		return addFunAppItemInfo(index, funItemInfo, true, true);
	}

	/**
	 * 添加一个元素（包括程序和文件夹）
	 * 
	 * @param index
	 *            添加位置
	 * @param funItemInfo
	 *            元素
	 * @param notDuplicate
	 *            不重复添加
	 * @throws DatabaseException
	 */
	public synchronized FunItemInfo addFunAppItemInfo(int index,
			FunItemInfo funItemInfo, final boolean notDuplicate,
			boolean handleDB) throws DatabaseException {

		// LogUnit.i("FunControler", "addFunAppItemInfo() -- index: " + index
		// + "notDuplicate: " + notDuplicate + "handleDB: " + handleDB);
		if (index < 0) {
			index = 0;
		}

		// 若不重复添加, 则先判断是否已存在
		if (notDuplicate) {
			Intent intent = null;
			if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
				intent = funItemInfo.getIntent();
				FunItemInfo item = getFunItemInfo(intent);
				// 若内存中已经存在, 则不添加
				if (null != item) {
					return null;
				}
			} else {
				AppItemInfo appItemInfo = ((FunAppItemInfo) funItemInfo)
						.getAppItemInfo();
				if (null == appItemInfo) {
					return null;
				}
				intent = appItemInfo.mIntent;
				// 若内存中已经存在, 则重新设置AppItemInfo
				FunItemInfo item = getFunItemInfo(intent);
				if (null != item) {
					if (item.getType() == FunItemInfo.TYPE_APP) {
						((FunAppItemInfo) item).setAppItemInfo(appItemInfo);
					}
					return null;
				}
			}
			if (null == intent) {
				return null;
			}

			// 查看数据库中是否已存在
			// TODO：update index
			if (handleDB && mFunDataModel.getAppItemIndex(intent) >= 0) {
				// TODO:日志
				return null;
			}
		}

		// 若内存已经有数据,则加到内存中
		if (mHasInit) {
			// 取小的
			int idx = index > mAllAppItemInfos.size() ? mAllAppItemInfos.size()
					: index;
			// 添加到内存
			// LogUnit.i("FunControler",
			// "addFunAppItemInfo() --  mAllAppItemInfos.add(idx, funItemInfo)");
			mAllAppItemInfos.add(idx, funItemInfo);
			// 更新mItemInAppIndex
			funItemInfo.setIndex(idx);
			FunItemInfo info = null;
			for (int i = idx + 1; i < mAllAppItemInfos.size(); ++i) {
				info = mAllAppItemInfos.get(i);
				info.setIndex(info.getIndex() + 1);
			}
		}

		// 添加到数据库
		if (handleDB) {
			mFunDataModel.addFunAppItemInfo(funItemInfo);
		}

		// 通知
		broadCast(ADDITEM, index, funItemInfo, null);

		return funItemInfo;
	}

	/**
	 * 在内存中批量添加
	 * 
	 * @param startIndex
	 * @param funItemInfos
	 */
	private synchronized void addFunAppItemsInMem(final int startIndex,
			final ArrayList<FunAppItemInfo> funItemInfos) {
		// LogUnit.i("FunControler", "addFunAppItemsInMem() -- startIndex: "
		// + startIndex);
		if (startIndex < 0) {
			return;
		}

		if (null == funItemInfos) {
			return;
		}

		int add = 0;
		FunItemInfo addItem = null;
		FunAppItemInfo funItemInfo = null;
		int size = funItemInfos.size();
		for (int i = 0; i < size; ++i) {
			funItemInfo = funItemInfos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			// 从startIndex开始添加
			addItem = addFunAppItemInfoInMem(startIndex + add, funItemInfo,
					true);
			if (null != addItem) {
				++add;
			}
		}
		// LogUnit.i("FunControler", "addFunAppItemsInMem() -- end");
	}

	/**
	 * 添加到内存中，用于批量添加
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notDuplicate
	 * @return
	 */
	private synchronized FunItemInfo addFunAppItemInfoInMem(int index,
			FunItemInfo funItemInfo, final boolean notDuplicate) {
		// LogUnit.i("FunControler", "addFunAppItemInfoInMem() -- notDuplicate:"
		// + notDuplicate);
		// 若不重复添加, 则先判断是否已存在
		if (notDuplicate) {
			if (!checkValid(funItemInfo)) {
				return null;
			}

			// 若内存中已经存在, 则不添加
			FunItemInfo funInfo = getFunItemInfo(funItemInfo.getIntent());
			if (null != funInfo) {
				// 若是app且是暂存的，则标志为不是暂存的(安装时)
				if (FunItemInfo.TYPE_APP == funInfo.getType()) {
					AppItemInfo appItemInfo = mAppDataEngine
							.getAppItem(funItemInfo.getIntent());
					if (null != appItemInfo) {
						((FunAppItemInfo) funInfo).setAppItemInfo(appItemInfo);
					}
				}
				return null;
			}
		}

		// 若内存已经有数据,则加到内存中
		if (mHasInit) {
			// 取小的
			int idx = index > mAllAppItemInfos.size() ? mAllAppItemInfos.size()
					: index;

			// LogUnit.i("FunControler", "addFunAppItemInfoInMem() -- idx:" +
			// idx);
			// 添加到内存
			mAllAppItemInfos.add(idx, funItemInfo);
			// 更新mItemInAppIndex
			funItemInfo.setIndex(idx);
			FunItemInfo info = null;
			for (int i = idx + 1; i < mAllAppItemInfos.size(); ++i) {
				info = mAllAppItemInfos.get(i);
				info.setIndex(info.getIndex() + 1);
			}
		}

		return funItemInfo;
	}

	/**
	 * check合法性 TODO:提取到共同的地方
	 * 
	 * @param funItemInfo
	 * @return
	 */
	private boolean checkValid(final FunItemInfo funItemInfo) {
		if (null == funItemInfo || null == funItemInfo.getIntent()) {
			return false;
		}
		return true;
	}

	/**
	 * 删除文件夹，此接口会自动将文件夹中的元素添加到文件夹被删位置
	 * 
	 * @param funFolderItemInfo
	 *            文件夹信息
	 * @return 被删的文件夹中的元素
	 * @throws DatabaseException
	 */
	public synchronized ArrayList<AppItemInfo> removeFolder(
			final FunFolderItemInfo funFolderItemInfo) throws DatabaseException {
		if (null == funFolderItemInfo) {
			return null;
		}
		ArrayList<AppItemInfo> removeList = null;
		funFolderItemInfo.setMfolderchange(true);
		// 获取文件夹中的元素
		ArrayList<FunAppItemInfo> appsInFolder = funFolderItemInfo
				.getFunAppItemInfos();
		try {
			mFunDataModel.beginTransaction();
			// 删除文件夹
			removeFunAppItemInfo(funFolderItemInfo.getIndex(), true);
			// 清除文件夹中的元素
			funFolderItemInfo.clearFunAppItems();

			// 将文件夹中的元素插入到功能表正确位置
			int index;

			// 先处理内存中的数据
			for (FunAppItemInfo funAppItemInfo : appsInFolder) {
				index = IndexFinder.findIndex(mContext, mAllAppItemInfos, true,
						funAppItemInfo);
				if (removeList == null) {
					removeList = new ArrayList<AppItemInfo>();
				}
				FunAppItemInfo itemInfo = (FunAppItemInfo) addFunAppItemInfo(
						index, funAppItemInfo, true, true);
				if (itemInfo != null) {
					removeList.add(itemInfo.getAppItemInfo());
				}
			}
			// 更新数据库
			// mFunDataModel.updateFunAppItemsIndex(mAllAppItemInfos);
			mFunDataModel.setTransactionSuccessful();
		} finally {
			mFunDataModel.endTransaction();
		}
		return removeList;
	}

	/**
	 * 删除文件夹，此接口会自动将文件夹中的元素添加到文件夹被删位置
	 * 
	 * @param funFolderItemInfo
	 *            文件夹信息
	 * @return 被删的文件夹中的元素
	 */
	/*
	 * public synchronized ArrayList<AppItemInfo> removeFoldertoapp( final
	 * FunFolderItemInfo funFolderItemInfo,final FunAppItemInfo funforshow) { if
	 * (null == funFolderItemInfo || null == funforshow) { return null; }
	 * funFolderItemInfo.setMfolderchange(true); // 获取文件夹中的元素
	 * ArrayList<FunAppItemInfo> appsInFolder = funFolderItemInfo
	 * .getFunAppItemInfos(); int mindex = funFolderItemInfo.getIndex(); //
	 * 删除文件夹 removeFunAppItemInfo(funFolderItemInfo.getIndex(), true); //
	 * 清除文件夹中的元素 funFolderItemInfo.clearFunAppItems();
	 * 
	 * // 将文件夹中的元素插入到功能表正确位置 int index; ArrayList<AppItemInfo> removeList =
	 * null; if (removeList == null) { removeList = new
	 * ArrayList<AppItemInfo>(); } if(removeList != null){ FunAppItemInfo
	 * itemInfoforshow = (FunAppItemInfo) addFunAppItemInfo(mindex, funforshow,
	 * true, true); if(null != itemInfoforshow){
	 * removeList.add(itemInfoforshow.getAppItemInfo());
	 * appsInFolder.remove(funforshow); } }
	 * 
	 * // 先处理内存中的数据 for (FunAppItemInfo funAppItemInfo : appsInFolder) { index =
	 * IndexFinder.findIndex(mContext, mAllAppItemInfos, true, funAppItemInfo);
	 * FunAppItemInfo itemInfo = (FunAppItemInfo) addFunAppItemInfo(index,
	 * funAppItemInfo, true, true); if (itemInfo != null && removeList != null)
	 * { removeList.add(itemInfo.getAppItemInfo()); } } // 更新数据库 //
	 * mFunDataModel.updateFunAppItemsIndex(mAllAppItemInfos);
	 * 
	 * return removeList; }
	 */
	/**
	 * 移动一个元素到文件夹中
	 * 
	 * @param funFolderItemInfo
	 *            将要移入的目标文件夹
	 * @param index
	 *            将要移入文件夹中的位置
	 * @param funAppItemInfo
	 *            要移动的元素
	 * @throws DatabaseException
	 */
	public synchronized void moveFunAppItemToFolder(final FunFolderItemInfo funFolderItemInfo,
			final int index, FunAppItemInfo funAppItemInfo) throws DatabaseException {
		if (null == funFolderItemInfo || null == funAppItemInfo) {
			return;
		}
		if (!funFolderItemInfo.isExistAppInFolder(funAppItemInfo)) {
			// 搜索全局，把原有的删除
			removeFunAppItemInfo(funAppItemInfo);
			funFolderItemInfo.addFunAppItemInfo(index, funAppItemInfo, true, true);
			funFolderItemInfo.setMfolderchange(true);
		}
		if (!funFolderItemInfo.isMfolderfirstcreate()) {
			String folderName = funFolderItemInfo.getTitle();
			String defaultFolderName = GOLauncherApp.getContext().getString(R.string.folder_name);
			if (folderName != null && defaultFolderName != null
					&& folderName.equals(defaultFolderName)) {
				//智能命名文件夹
				ArrayList<FunAppItemInfo> itemInfos = funFolderItemInfo.getFunAppItemInfosForShow();
				ArrayList<AppItemInfo> appItemInfos = new ArrayList<AppItemInfo>(itemInfos.size());
				for (FunAppItemInfo itemInfo : itemInfos) {
					AppItemInfo appItemInfo = itemInfo.getAppItemInfo();
					if (appItemInfo == null) {
						continue;
					}
					appItemInfos.add(appItemInfo);
				}
				String newFolderName = CommonControler.getInstance(mContext).generateFolderName(
						appItemInfos);
				if (newFolderName != null && !newFolderName.equals("")) {
					notifyFolderNameChanged(newFolderName, funFolderItemInfo);
				}
			}
		}
		// 在文件夹中添加一个元素
		// if (funFolderItemInfo.addFunAppItemInfo(index, funAppItemInfo, true,
		// true)) {
		// // 从原位置删除
		// removeFunAppItemInfo(funAppItemInfo);
		// funFolderItemInfo.setMfolderchange(true);
		// return;
		// }
	}

	/**
	 * 从文件夹中移动一个元素到功能表根目录
	 * 
	 * @param funFolderItemInfo
	 *            文件夹
	 * @param index
	 *            功能表根目录的位置
	 * @param funAppItemInfo
	 *            要移动的元素
	 * @throws DatabaseException
	 */
	public synchronized void moveFunAppItemFromFolder(
			final FunFolderItemInfo funFolderItemInfo, final int index,
			FunAppItemInfo funAppItemInfo) throws DatabaseException {
		if (null == funFolderItemInfo || null == funAppItemInfo) {
			return;
		}
		funFolderItemInfo.setMfolderchange(true);
		// LogUnit.i("FunControler", "moveFunAppItemFromFolder()");
		// 添加到功能表根目录
		addFunAppItemInfo(index, funAppItemInfo);
		// 从文件夹中删除
		funFolderItemInfo.removeFunAppItemInfo(funAppItemInfo, true);
	}

	/**
	 * 从文件夹移动一组应用程序到功能表根目录
	 */
	// public synchronized void moveFunAppItemsFromFolder(
	// final FunFolderItemInfo folderInfo,
	// ArrayList<FunAppItemInfo> appList) {
	// if (null == folderInfo || null == appList) {
	// return;
	// }
	//
	// }

	/**
	 * 添加一个文件夹(空文件夹)
	 * 
	 * @param index
	 *            添加的位置
	 * @param folderTitle
	 *            文件夹名称 TODO:
	 * @param iconPath
	 *            文件夹图标路径 TODO:
	 * @throws DatabaseException
	 */
	public synchronized FunFolderItemInfo addFunFolderItemInfo(int index,
			String folderTitle, String iconPath) throws DatabaseException {
		FunFolderItemInfo funItemInfo = new FunFolderItemInfo(mFunDataModel,
				folderTitle);

		// LogUnit.i("FunControler", "addFunFolderItemInfo()");
		addFunAppItemInfo(index, funItemInfo);
		return funItemInfo;
	}

	/**
	 * 添加一个新文件夹
	 * 
	 * @param index
	 * @param folderInfo
	 * @return
	 * @throws DatabaseException
	 */
	public synchronized void addFunFolderItemInfo(int index,
			FunFolderItemInfo folderInfo) throws DatabaseException {
		// LogUnit.i("FunControler", "addFunFolderItemInfo() with folderInfo");
		addFunAppItemInfo(index, folderInfo);
	}

	/**
	 * 添加一个文件夹（自动查找合适的位置进行插入）
	 * 
	 * @param folderTitle
	 * @param iconPath
	 * @return
	 * @throws DatabaseException
	 */
	public synchronized FunFolderItemInfo addFunFolderItemInfo(
			String folderTitle, String iconPath) throws DatabaseException {
		FunFolderItemInfo funItemInfo = new FunFolderItemInfo(mFunDataModel,
				folderTitle);

		int index = IndexFinder.findIndex(mContext, mAllAppItemInfos, false,
				funItemInfo);

		// LogUnit.i("FunControler", "addFunFolderItemInfo() without index");
		addFunAppItemInfo(index, funItemInfo);
		return funItemInfo;
	}

	/**
	 * 更新名称
	 */
	private void updateTitle() {
		FunDataModel model = mFunDataModel;
		if (model != null) {
			ArrayList<AppItemInfo> apps = model.getAllAppItemInfos();
			if (null == apps) {
				return;
			}

			// LogUnit.i("FunControler", "updateTitle()");
			AppItemInfo appItemInfo = null;
			int size = apps.size();
			model.beginTransaction();
			try {
				for (int i = 0; i < size; ++i) {
					appItemInfo = apps.get(i);
					if (null == appItemInfo) {
						continue;
					}

					// 更新数据库中的title
					model.updateFunAppItem(appItemInfo.mIntent,
							appItemInfo.mTitle);
				}
				model.setTransactionSuccessful();
			} catch (DatabaseException e) {
				e.printStackTrace();
			} finally {
				model.endTransaction();
			}
		}
	}

	/**
	 * 批量删除
	 * 
	 * @param funItemInfos
	 */
	public void removeFunAppItemInfo(final ArrayList<FunItemInfo> funItemInfos) {

	}

	/**
	 * 删除一个元素（包括文件夹和程序图标）
	 * 
	 * @param funItemInfo
	 *            要删除的元素
	 * @throws DatabaseException
	 */
	public synchronized FunItemInfo removeFunAppItemInfo(FunItemInfo funItemInfo)
			throws DatabaseException {
		// LogUnit.i("FunControler", "removeFunAppItemInfo() -- funItemInfo"
		// + funItemInfo);
		// 从内存中删除
		int idx = findInList(funItemInfo);
		if (idx == -1) {
			// 若功能表中列表中找不到该程序数据则从功能表中的文件夹中找
			// 如果是从文件夹内的图标,则删除后如果是剩下一个图标，并且删除文件夹
			// TODO 这里用了两个循环来做效果不是很好，应该还可以改进
			ArrayList<FunFolderItemInfo> folders = getFunFolders();
			for (FunFolderItemInfo folder : folders) {
				ArrayList<FunAppItemInfo> appItems = folder
						.getFunAppItemInfos();
				for (FunAppItemInfo appItem : appItems) {
					if (appItem != null
							&& appItem.getIntent().equals(
									funItemInfo.getIntent())) {
						folder.removeFunAppItemInfo(appItem, true);
						ArrayList<FunAppItemInfo> appsInFolder = folder
								.getFunAppItemInfosForShow();
						if (appsInFolder.size() <= 1) {
							removeFolder(folder);
							AppFuncHandler handler = AppFuncHandler
									.getInstance();
							handler.hideFolder();
							handler.refreshAllAppGrid();
						}
						return funItemInfo;
					}
				}
			}
			return null;
		} else {
			return removeFunAppItemInfo(idx, true);
		}
	}

	/**
	 * 从内存和数据库中删除,根据FunItemInfo进行删除
	 * 
	 * @param funItemInfo
	 * @return
	 * @throws DatabaseException
	 */
	// private synchronized FunItemInfo
	// removeFunAppItemInfoInMemAndDb(FunItemInfo funItemInfo) throws
	// DatabaseException {
	// if (mHasInit == false) {
	// return null;
	// }
	//
	// if (funItemInfo==null) {
	// return null;
	// }
	// int index = mAllAppItemInfos.indexOf(funItemInfo);
	// if (index!=-1) {
	// // LogUnit.i("FunControler", "removeFunAppItemInfoInMem() -- index"
	// // + index);
	// // 从内存中删除
	// mAllAppItemInfos.remove(funItemInfo);
	// // 更新mItemInAppIndex
	// FunItemInfo info = null;
	//
	// for (int i = index; i < mAllAppItemInfos.size(); ++i) {
	// info = mAllAppItemInfos.get(i);
	// if (info!=null) {
	// info.setIndex(info.getIndex() - 1);
	// }
	// }
	// }
	//
	// mFunDataModel.removeFunappItemInfoByIntent(funItemInfo.getIntent());
	//
	// return funItemInfo;
	// }

	/**
	 * 从内存中批量删除
	 * 
	 * @param funItemInfos
	 * @return
	 */
	private synchronized void removeFunAppItemInfosInMem(
			final ArrayList<FunAppItemInfo> funItemInfos) {
		if (null == funItemInfos) {
			return;
		}

		// LogUnit.i("FunControler", "removeFunAppItemInfosInMem()");
		FunItemInfo funItemInfo = null;
		int size = funItemInfos.size();
		for (int i = 0; i < size; ++i) {
			funItemInfo = funItemInfos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			// 从内存中删除
			removeFunAppItemInfoInMem(funItemInfo.getIndex());
		}
	}

	/**
	 * 从内存中删除, 用于批量删除
	 * 
	 * @param index
	 * @return
	 */
	private synchronized FunItemInfo removeFunAppItemInfoInMem(int index) {
		if (mHasInit == false) {
			return null;
		}
		if (index < 0 || index >= mAllAppItemInfos.size()) {
			// TODO:打日志
			return null;
		}

		// LogUnit.i("FunControler", "removeFunAppItemInfoInMem() -- index"
		// + index);
		// 从内存中删除
		FunItemInfo tempInfo = mAllAppItemInfos.remove(index);
		// 更新mItemInAppIndex
		FunItemInfo info = null;
		for (int i = index; i < mAllAppItemInfos.size(); ++i) {
			info = mAllAppItemInfos.get(i);
			if (info != null) {
				info.setIndex(info.getIndex() - 1);
			}
		}

		return tempInfo;
	}

	/**
	 * <br>
	 * 功能简述:从所有程序界面批量删除应用数据，同时删除内存和数据库中的数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param funItemInfos
	 *            要删除的应用数据列表
	 * @throws DatabaseException
	 */
	public synchronized void removeFunAppItemInfos(
			final ArrayList<FunAppItemInfo> funItemInfos)
			throws DatabaseException {
		// LogUnit.i("FunControler", "removeFunAppItemInfos()");
		// 从内存中批量删除
		ArrayList<FunAppItemInfo> list = (ArrayList<FunAppItemInfo>) funItemInfos
				.clone();
		removeFunAppItemInfosInMem(list);
		list = null;
		// 从数据库中批量删除
		mFunDataModel.removeFunAppItemInfosInDB(funItemInfos);
	}

	/**
	 * 删除一个元素（包括文件夹和程序图标）
	 * 
	 * @param index
	 *            删除的index
	 * @return 被删除的元素
	 * @throws DatabaseException
	 */
	public synchronized FunItemInfo removeFunAppItemInfo(int index,
			boolean handleDB) throws DatabaseException {
		if (index < 0) {
			return null;
		}

		// LogUnit.i("FunControler", "removeFunAppItemInfo() -- index:" + index
		// + "handleDB:" + handleDB);
		// 从内存中删除
		FunItemInfo tempInfo = removeFunAppItemInfoInMem(index);
		// if (null != tempInfo) {
		// // 若是文件夹
		// if (FunItemInfo.TYPE_FOLDER == tempInfo.getType()) {
		// ((FunFolderItemInfo)tempInfo).clearFunAppItems();
		// }
		// }

		if (handleDB) {
			// 从数据库删除
			if (tempInfo != null) {
				mFunDataModel.removeFunappItemInfo(tempInfo.getIntent());
			}

		}

		// 通知
		broadCast(REMOVEITEM, index, tempInfo, null);

		return tempInfo;
	}

	/**
	 * 移动图标（包括文件夹和程序图标）
	 * 
	 * @param resIdx
	 *            源位置
	 * @param tarIdx
	 *            目标位置
	 * @throws DatabaseException
	 */
	@Deprecated
	public void moveFunAppItemInfo(int resIdx, int tarIdx)
			throws DatabaseException {
		if (resIdx == tarIdx) {
			return;
		}

		// LogUnit.i("FunControler", "moveFunAppItemInfo()");
		// 移动
		try {
			mFunDataModel.beginTransaction();
			FunItemInfo delInfo = removeFunAppItemInfo(resIdx, true);
			addFunAppItemInfo(tarIdx, delInfo);
			mFunDataModel.setTransactionSuccessful();
		} finally {
			mFunDataModel.endTransaction();
		}
	}

	/**
	 * 移动图标（包括文件夹和程序图标）
	 * 
	 * @param resIdx
	 *            源位置
	 * @param tarIdx
	 *            目标位置
	 */
	public boolean moveFunAppItemInfo2(int resIdx, int tarIdx) {
		if (resIdx == tarIdx) {
			return true;
		}
		boolean success = true;
		// LogUnit.i("FunControler", "moveFunAppItemInfo2()");
		// 操作数据库
		if (mFunDataModel.moveAppItem(resIdx, tarIdx)) {
			// 在数据库操作成功的情况下才操作内存
			FunItemInfo delInfo = removeFunAppItemInfoInMem(resIdx);
			if (delInfo != null) {
				try {
					addFunAppItemInfo(tarIdx, delInfo, true, false);
				} catch (DatabaseException e) {
					e.printStackTrace();
					success = false;
				}
			}
		} else {
			success = false;
		}
		return success;
	}

	/**
	 * 从位置startIndex开始批量添加FunAppItemInfo
	 * 
	 * @param startIndex
	 * @param funItemInfos
	 */
	private synchronized void addFunAppItems(final int startIndex,
			final ArrayList<FunAppItemInfo> funItemInfos) {
		if (startIndex < 0) {
			return;
		}

		if (null == funItemInfos) {
			return;
		}

		// LogUnit.i("FunControler", "addFunAppItems()");
		// 在内存中批量添加
		addFunAppItemsInMem(startIndex, funItemInfos);
		// 在数据库中批量添加
		mFunDataModel.addFunAppItemsInDB(startIndex, funItemInfos);
	}

	/**
	 * 批量删除
	 * 
	 * @param appItemInfos
	 * @param folderIds
	 *            文件夹id列表
	 * @return
	 * @throws DatabaseException
	 */
	private synchronized ArrayList<FunItemInfo> removeAppItems(
			final ArrayList<AppItemInfo> appItemInfos,
			final ArrayList<Long> folderIds) throws DatabaseException {
		if (null == appItemInfos) {
			return null;
		}

		// LogUnit.i("FunControler", "removeAppItems()");
		FunItemInfo funItemInfo = null;
		ArrayList<FunItemInfo> infos = new ArrayList<FunItemInfo>();

		// 批量删除
		AppItemInfo appItemInfo = null;
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			appItemInfo = appItemInfos.get(i);
			if (null == appItemInfo) {
				continue;
			}
			funItemInfo = removeAppItem(appItemInfo, folderIds);
			if (null == funItemInfo) {
				continue;
			}

			infos.add(funItemInfo);
		}

		return infos;
	}

	/**
	 * 卸载一个程序
	 * 
	 * @param appItemInfo
	 *            卸载的程序
	 * @return 若功能表内存中有，则返回该程序
	 * @throws DatabaseException
	 */
	private synchronized FunItemInfo removeAppItem(
			final AppItemInfo appItemInfo, final ArrayList<Long> folderIds)
			throws DatabaseException {
		if (null == appItemInfo) {
			return null;
		}

		// LogUnit.i("FunControler", "removeAppItem()");
		// TODO:
		if (0 == mAllAppItemInfos.size()) {
			// LogUnit.i("FunControler",
			// "removeAppItem() -- 0 == mAllAppItemInfos.size()");
			// 删除数据库中的
			// 功能表根目录
			mFunDataModel.removeFunappItemInfo(appItemInfo.mIntent);
			// 从隐藏名单中去除
			mAppConfigControler.delHideAppItem(appItemInfo.mIntent, true);
			// 文件夹中的
			handleRemoveInfolders(folderIds, appItemInfo.mIntent);
			return null;
		}

		// 从内存中删除
		FunItemInfo tempInfo = null;
		FunItemInfo funItemInfo = null;
		int size = mAllAppItemInfos.size();
		for (int i = size - 1; i >= 0; --i) {
			funItemInfo = mAllAppItemInfos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			// 若是文件夹
			if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
				// LogUnit.i("FunControler",
				// "removeAppItem() -- FunItemInfo.TYPE_FOLDER == funItemInfo.getType()");
				// 从内存及数据库中删除
				tempInfo = ((FunFolderItemInfo) funItemInfo)
						.removeFunAppItemInfo(appItemInfo.mIntent, true, true);
				if (tempInfo != null) {
					break;
				} else {
					continue;
				}
			}

			// 若不是文件夹
			if (((FunAppItemInfo) funItemInfo).getAppItemInfo() == appItemInfo) {
				// LogUnit.i(
				// "FunControler",
				// "removeAppItem() -- ((FunAppItemInfo) funItemInfo).getAppItemInfo() == appItemInfo");
				tempInfo = removeFunAppItemInfo(i, true);
				// 从隐藏名单中去除
				mAppConfigControler.delHideAppItem(appItemInfo.mIntent, true);
				break;
			}
		}

		return tempInfo;
	}

	// TODO:添加监控到安装时的列表维护
	private synchronized ArrayList<FunAppItemInfo> addAppItems(
			final ArrayList<AppItemInfo> appItemInfos) throws DatabaseException {
		if (null == appItemInfos) {
			return null;
		}

		// LogUnit.i("FunControler", "addAppItems()");
		// 生成要添加的数组
		ArrayList<FunAppItemInfo> funItemInfos = new ArrayList<FunAppItemInfo>();
		AppItemInfo appItemInfo = null;
		FunAppItemInfo funItemInfo = null;
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			appItemInfo = appItemInfos.get(i);
			if (null == appItemInfo) {
				continue;
			}

			funItemInfo = new FunAppItemInfo(appItemInfo);
			funItemInfo.setIsNew(true);
			funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
			funItemInfos.add(funItemInfo);
		}

		// 插入操作
		int sortType = GOLauncherApp.getSettingControler().getFunAppSetting()
				.getSortType();
		int countIndex;
		for (FunAppItemInfo itemInfo : funItemInfos) {
			countIndex = -1;
			// 获取添加位置
			if (FunAppSetting.SORTTYPE_LETTER == sortType) {
				// 如果Title为空，则直接放在末尾
				if (itemInfo.getTitle() != null) {
					countIndex = IndexFinder.findFirstIndex(mContext,
							mAllAppItemInfos, true,
							IndexFinder.COMPARE_WITH_STRING,
							itemInfo.getTitle(), "ASC");
				}

			} else if (FunAppSetting.SORTTYPE_TIMENEAR == sortType) {
				// countIndex = findFirstAppInApps();
				countIndex = IndexFinder.findFirstItemInApps(mAllAppItemInfos,
						true);
			} else if (FunAppSetting.SORTTYPE_FREQUENCY == sortType) {
				countIndex = IndexFinder.findFirstIndex(mContext,
						mAllAppItemInfos, true,
						IndexFinder.COMPARE_WITH_FREQUENCE,
						itemInfo.getClickedCount(mContext), "DESC");
			} else {
				// countIndex = findLastAppInApps();
				countIndex = IndexFinder.findLastItemInApps(mAllAppItemInfos,
						true);
			}

			if (countIndex < 0) {
				countIndex = mFunDataModel.getSizeOfApps();
			}

			addFunAppItemInfo(countIndex, itemInfo);
			// Log.d("XViewFrame", "countIndex = " + countIndex);
		}

		return funItemInfos;
	}

	/**
	 * 添加某个程序至列表（内部确定Index）
	 * 
	 * @param appItemInfo
	 * @throws DatabaseException
	 */
	private synchronized FunAppItemInfo addAppItem(AppItemInfo appItemInfo)
			throws DatabaseException {
		if (appItemInfo == null) {
			return null;
		}

		// 构造插入对象
		FunAppItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
		funItemInfo.setIsNew(true);
		funItemInfo.setHideInfo(mAppConfigControler.getHideInfo(funItemInfo.getIntent()));
		// 插入操作
		int sortType = GOLauncherApp.getSettingControler().getFunAppSetting()
				.getSortType();
		int countIndex = -1;
		// 获取添加位置
		if (FunAppSetting.SORTTYPE_LETTER == sortType) {
			// 如果Title为空，则直接放在末尾
			if (funItemInfo.getTitle() != null) {
				countIndex = IndexFinder.findFirstIndex(mContext,
						mAllAppItemInfos, true,
						IndexFinder.COMPARE_WITH_STRING,
						funItemInfo.getTitle(), "ASC");
			}

		} else if (FunAppSetting.SORTTYPE_TIMENEAR == sortType) {
			// countIndex = findFirstAppInApps();
			countIndex = IndexFinder
					.findFirstItemInApps(mAllAppItemInfos, true);
		} else if (FunAppSetting.SORTTYPE_FREQUENCY == sortType) {
			countIndex = IndexFinder.findFirstIndex(mContext, mAllAppItemInfos,
					true, IndexFinder.COMPARE_WITH_FREQUENCE,
					funItemInfo.getClickedCount(mContext), "DESC");
		} else {
			// countIndex = findLastAppInApps();
			countIndex = IndexFinder.findLastItemInApps(mAllAppItemInfos, true);
		}

		if (countIndex < 0) {
			countIndex = mFunDataModel.getSizeOfApps();
		}

		addFunAppItemInfo(countIndex, funItemInfo);

		return funItemInfo;
	}

	private boolean findAndSetAppItemInFolder(AppItemInfo appItemInfo) {
		boolean find = false;
		for (FunItemInfo funItemInfo : mAllAppItemInfos) {
			if (funItemInfo instanceof FunFolderItemInfo) {
				FunFolderItemInfo folderItemInfo = (FunFolderItemInfo) funItemInfo;
				int idx = folderItemInfo.getFunAppItem(appItemInfo.mIntent);
				if (idx > -1) {
					FunAppItemInfo funAppItemInfo = folderItemInfo
							.getFunAppItemInFolder(idx);
					if (funAppItemInfo != null) {
						funAppItemInfo.setAppItemInfo(appItemInfo);
						find = true;
						break;
					}
				}
			}
		}
		return find;
	}

	// TODO:?
	/**
	 * 获取当前程序列表是否加载完毕
	 * 
	 * @author huyong
	 * @return
	 */
	public boolean getIsLoadFinish() {
		boolean result = false;
		synchronized (mLocker) {
			result = mIsLoadFinish;
		}
		return result;
	}

	/**
	 * 是否已经加载完title和icon
	 * 
	 * @return 是否已经加载完
	 */
	public boolean getIsIconTitleLoadFinish() {
		return mIsIconTitleLoadFinish;
	}

	/**
	 * 处理缓存中的安装卸载数据
	 */
	private synchronized void handleCachedAppsList() {
		if (null == mCachedApps) {
			return;
		}
		synchronized (mCachedApps) {
			for (CacheItemInfo cacheInfo : mCachedApps) {
				if (cacheInfo.isInstall) {
					// 先从内存中的文件夹里搜索，如果找不到才添加到文件夹外部
					boolean find = findAndSetAppItemInFolder(cacheInfo.itemInfo);
					if (!find) {
						// 安装程序
						FunAppItemInfo funAppItemInfo = null;
						try {
							funAppItemInfo = addAppItem(cacheInfo.itemInfo);
						} catch (DatabaseException e) {
							e.printStackTrace();
						}
						// 通知安装了程序
						broadCast(INSTALL_APP, 0, funAppItemInfo, null);
					}
				} else {
					// 卸载程序
					final ArrayList<Long> folderIds = getFolderIds();
					FunItemInfo funItemInfo = null;
					try {
						funItemInfo = removeAppItem(cacheInfo.itemInfo,
								folderIds);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					// 通知卸载了程序
					broadCast(UNINSTALL_APP, 0, funItemInfo, null);
				}
			}

			// 清除缓存数据
			mCachedApps.clear();

			mCachedApps = null;
		}
	}

	/**
	 * 获取文件夹id列表
	 * 
	 * @return 文件夹id列表
	 */
	private final ArrayList<Long> getFolderIds() {
		// 文件夹id列表
		ArrayList<Long> folderIds = null;
		if (mHasInit) {
			// 从内存中取
			folderIds = new ArrayList<Long>();
			FunItemInfo funItemInfo = null;
			int size = mAllAppItemInfos.size();
			for (int i = 0; i < size; ++i) {
				funItemInfo = mAllAppItemInfos.get(i);
				if (null == funItemInfo) {
					continue;
				}

				if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
					folderIds.add(funItemInfo.getFolderId());
				}
			}
		} else {
			// 从数据库中取
			folderIds = mFunDataModel.getFolderIds();
		}
		return folderIds;
	}

	/**
	 * 处理缓存中SDcard的数据
	 */
	private void handleCacheSDcardList() {
		if (null == mCacheSDcard) {
			return;
		}
		boolean toUpdate = mStateParaphrase.checkUpdate();

		// Log.i("FunControler", "handleCacheSDcardList() -- toUpdate: "
		// + toUpdate);
		// sd卡数据更新了, 更新列表
		handleSDAppItems(mCacheSDcard, toUpdate);
		// 清除缓存数据
		mCacheSDcard = null;
		// 更新title到数据库
		startSaveTitleThread();
	}

	private synchronized void startSaveTitleThread() {
		// LogUnit.i("FunControler", "startSaveTitleThread()");
		// 更新title到数据库
		new Thread(ThreadName.FUNC_SAVE_TITLE) {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				updateTitle();
			}
		}.start();
	}

	/**
	 * 将新数据加入缓存列表
	 * 
	 * @param cacheList
	 * @param addInfo
	 */
	// private void addInCacheList(AppItemInfo itemInfo, boolean isInstall) {
	// if (null == itemInfo) {
	// return;
	// }
	//
	// CacheItemInfo cacheInfo = new CacheItemInfo(itemInfo, isInstall);
	// synchronized (mCachedApps) {
	// mCachedApps.add(cacheInfo);
	// }
	// }

	/**
	 * 将新数据加入缓存列表
	 * 
	 * @param addList
	 * @param isInstall
	 */
	private void addInCacheList(ArrayList<AppItemInfo> addList,
			boolean isInstall) {
		if (null == addList) {
			return;
		}

		int size = addList.size();
		synchronized (mCachedApps) {
			for (int i = 0; i < size; ++i) {
				CacheItemInfo cacheItemInfo = new CacheItemInfo(addList.get(i),
						isInstall);
				mCachedApps.add(cacheItemInfo);
			}
		}
	}

	/**
	 * 设置隐藏程序
	 * 
	 * @param intent
	 */
	private void handleHideItem(final Intent intent, final boolean hide) {
		if (null == intent) {
			return;
		}

		if (mHasInit) {
			FunItemInfo info = null;
			for (int i = 0; i < mAllAppItemInfos.size(); ++i) {
				info = mAllAppItemInfos.get(i);
				if (null == info) {
					continue;
				}

				// 若在根目录
				Intent it = info.getIntent();
				if (ConvertUtils.intentCompare(it, intent)) {
					info.setHideInfo(mAppConfigControler.getHideInfo(intent));
					return;
				}

				// 若是文件夹
				if (FunItemInfo.TYPE_FOLDER == info.getType()) {
					if (((FunFolderItemInfo) info).setHideFunAppItemInfo(
							intent, hide, mContext)) {
						return;
					}
				}
			}
		}
	}

	/**
	 * 设置批量隐藏/不隐藏程序
	 * 
	 * @param appItems
	 */
	private void handleHideItems(final ArrayList<Intent> appItems,
			final boolean hide) {
		if (null == appItems) {
			return;
		}

		Intent it = null;
		int size = appItems.size();
		for (int i = 0; i < size; ++i) {
			it = appItems.get(i);
			if (null == it) {
				continue;
			}

			handleHideItem(it, hide);
		}
	}

	/**
	 * 将安装卸载消息加入到缓存中
	 * 
	 * @param msgId
	 *            消息类型
	 * @param object
	 *            对应的列表
	 */
	private void addMsgToCacheList(int msgId, Object object) {
		if (object == null) {
			return;
		}
		// LogUnit.i("FunControler", "addMsgToCacheList() -- msgId" + msgId);
		if (mCachedApps == null) {
			mCachedApps = new ArrayList<CacheItemInfo>();
		}
		switch (msgId) {
		case IDiyMsgIds.EVENT_INSTALL_APP: {
			addInCacheList((ArrayList<AppItemInfo>) object, true);
			break;
		}
		case IDiyMsgIds.EVENT_UNINSTALL_APP: {
			addInCacheList((ArrayList<AppItemInfo>) object, false);
			break;
		}
		default:
			break;
		}
	}

	public void handleMessage(int msgId, int param, Object object, List objects) {
		switch (msgId) {
		case IDiyMsgIds.EVENT_INSTALL_APP: {
//			Log.d("XViewFrame", "EVENT_INSTALL_APPS");
			addMsgToCacheList(msgId, objects);
			checkIsMediaManagementPluginInstall(object);
			// 如果数据正在处理
			if ((mHasInit == false) || (!mIsCacheHandled)) {
				return;
			}
			// LogUnit.i("FunControler",
			// "onHandleBCChange() -- handleCacheInstallList()");
			handleCachedAppsList();
			// // 添加，取得新增的程序 TODO:根据排序风格决定添加位置
			// ArrayList<FunAppItemInfo> infos =
			// addAppItems((ArrayList<AppItemInfo>) objects);
			// // 通知批量安装了程序
			// broadCast(INSTALL_APPS, 0, null, infos);
		}
			break;
		case IDiyMsgIds.EVENT_INSTALL_PACKAGE: {
			if (object != null
					&& object instanceof String
					&& ((String) object)
							.startsWith(AppFuncConstants.THEME_PACKGE_NAME)) {
				FunControler.sInstalledNewTheme = true;
				// DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.THEME_NEW_INSTALLED,
				// object);
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.APP_FUNC_MAIN_VIEW,
						AppFuncConstants.THEME_NEW_INSTALLED, object);
			}
			checkIsMediaManagementPluginInstall(object);
		}
			break;
		// case IDiyMsgIds.EVENT_UNINSTALL_APP: {
		// Log.d("XViewFrame", "EVENT_UNINSTALL_APP");
		// addMsgToCacheList(msgId, object);
		// // 如果数据正在处理
		// if ((mHasInit == false) || (!mIsCacheHandled)) {
		// LogUnit.i("FunControler", "EVENT_UNINSTALL_APP -- mHasInit = "
		// + mHasInit);
		// LogUnit.i("FunControler",
		// "EVENT_UNINSTALL_APP -- mIsCacheHandled ="
		// + mIsCacheHandled);
		// return;
		// }
		// LogUnit.i("FunControler",
		// "onHandleBCChange() -- handleCacheUnInstallList()");
		// handleCachedAppsList();
		// // final ArrayList<Long> folderIds = getFolderIds();
		// // FunItemInfo funItemInfo = removeAppItem((AppItemInfo) object,
		// // folderIds);
		// // // 通知卸载了程序
		// // broadCast(UNINSTALL_APP, 0, funItemInfo, null);
		// }
		// break;
		case IDiyMsgIds.EVENT_UNINSTALL_APP: {
			addMsgToCacheList(msgId, objects);
			checkIsMediaManagementPluginUnInstall(object);
			// 如果数据正在处理
			if ((mHasInit == false) || (!mIsCacheHandled)) {
				return;
			}
			// LogUnit.i("FunControler",
			// "onHandleBCChange() -- handleCacheUnInstallList()");
			handleCachedAppsList();
			// final ArrayList<Long> folderIds = getFolderIds();
			// ArrayList<FunItemInfo> infos = removeAppItems(
			// (ArrayList<AppItemInfo>) objects, folderIds);
			// // 通知批量卸载了程序
			// broadCast(UNINSTALL_APPS, 0, null, infos);
		}
		case IDiyMsgIds.EVENT_UNINSTALL_PACKAGE: {	
			checkIsMediaManagementPluginUnInstall(object);
		}
			break;
		case IDiyMsgIds.EVENT_LOAD_FINISH:
			mIsLoadFinish = true;
			// 通知加载完毕
			broadCast(LOAD_FINISH, 0, null, null);
			break;
		case IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK: {
			mStateParaphrase.updateState(StateParaphrase.SDCARD_IS_OK);
			mCacheSDcard = (ArrayList<AppItemInfo>) objects;
			// 如果数据仍未被初始化完或者正在被处理，则放入缓存中
			if ((mHasInit == false) || (!mIsSDHandled)) {
				return;
			}
			// LogUnit.i("FunControler",
			// "onHandleBCChange() -- handleCacheSDcardList()");
			handleCacheSDcardList();
			// // 检查是否要更新
			// boolean toUpdate = mStateParaphrase.checkUpdate();
			//
			// // sd卡数据更新了, 更新列表
			// handleSDAppItems(mCacheSDcard, toUpdate);

			// 通知sd card ok
			broadCast(SDCARDOK, 0, null, null);
		}
			break;
		case IDiyMsgIds.EVENT_REFLUSH_TIME_IS_UP: {
			mStateParaphrase.updateState(StateParaphrase.TIME_IS_UP);
			mCacheSDcard = (ArrayList<AppItemInfo>) objects;
			// 如果数据仍未被初始化完或者正在被处理，则放入缓存中
			if ((mHasInit == false) || (!mIsSDHandled)) {
				return;
			}
			// LogUnit.i("FunControler",
			// "onHandleBCChange() -- handleCacheSDcardList()");
			handleCacheSDcardList();
			// // 检查是否要更新
			// boolean toUpdate = mStateParaphrase.checkUpdate();
			//
			// // sd卡数据更新了, 更新列表
			// handleSDAppItems(mCacheSDcard, toUpdate);

			// 通知2分钟计时到点
			broadCast(TIMEISUP, 0, null, null);
		}
			break;
		case IDiyMsgIds.EVENT_LOAD_TITLES_FINISH: {
			mIsIconTitleLoadFinish = true;
			broadCast(FINISHLOADICONTITLE, 0, null, null);
			// TODO:信号量 线程通信
			// 第一次且已经完成其它初始化且未发送初始化完成消息
			if (mHasInit && !mIsInitFinish) {
				Message message = mHandler.obtainMessage();
				message.what = MSG_FINISHINIT;
				mHandler.sendMessage(message);
			}
		}
			break;
		case IDiyMsgIds.EVENT_APPS_LIST_UPDATE: {
			// Log.d("FunControler", "EVENT_APPS_LIST_UPDATE is runnning ");
			clearAllAppUpdate();
			AppsBean beans = (AppsBean) object;

			if (beans != null) {
				mUpdateBeans = beans;
				try {
					mUpdateBeansForFolder = mUpdateBeans.clone();
				} catch (Exception e) {

				}

				HashMap<Integer, Byte> controlMap = beans.mControlcontrolMap;
				if (controlMap != null && !controlMap.isEmpty()) {
					setmControlInfo(controlMap.get(2), controlMap.get(3),
							controlMap.get(4));
				}

				setBeanlist(beans.mListBeans);
				if (mBeanlist != null /* && mBeanlist.size() >= 0 */) {
					setmBeancount(mBeanlist.size());
					if (mHasInit/* && mBeanlist.size() > 0 */) {
						checkAppUpdate(mAllAppItemInfos, false);
						broadCast(BATUPDATE, param, object, objects);
					}
				}
			}
		}
			break;
		case IDiyMsgIds.EVENT_UPDATE_PACKAGE: {
			if (mUpdateBeans != null && mUpdateBeans.mListBeans != null) {
				String packageName = (String) object;
				ArrayList<AppBean> beanList = mUpdateBeans.mListBeans;
				Iterator<AppBean> it = beanList.iterator();
				while (it.hasNext()) {
					AppBean appBean = it.next();
					if (packageName.equals(appBean.mPkgName)) {
						PackageManager pm = mContext.getPackageManager();
						try {
							String versionName = pm.getPackageInfo(packageName,
									0).versionName;
							if (versionName != null
									&& versionName.equals(appBean.mVersionName)) {
								it.remove();
								GoLauncher.sendHandler(null,
										IDiyFrameIds.APPFUNC_FRAME,
										IDiyMsgIds.EVENT_APPS_LIST_UPDATE, 0,
										mUpdateBeans, null);
							}
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
			checkIsMediaManagementPluginUpdate(object);
		}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 检查是否资源管理插件包安装
	 */
	private void checkIsMediaManagementPluginInstall(Object object) {
		if (object != null && object instanceof String) {
			if (((String) object).equals(PackageName.MEDIA_PLUGIN)) {
				// 通知多媒体插件包安装
				MediaPluginFactory.setMediaPluginExist(1);
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.APP_FUNC_HOME_ICON,
						AppFuncConstants.MEDIA_PLUGIN_CHANGE, true);
				GoLauncher.sendMessage(this,
						IDiyFrameIds.APPFUNC_SEARCH_FRAME,
						IDiyMsgIds.MEDIA_PLUGIN_CHANGE, -1, true, null);
			}
		}
	}
	
	/**
	 * 检查是否资源管理插件包卸载
	 * @param object
	 */
	private void checkIsMediaManagementPluginUnInstall(Object object) {
		if (object != null && object instanceof String) {
			if (((String) object).equals(PackageName.MEDIA_PLUGIN)) {
				// 多媒体插件包卸载，删除.dex文件
				MediaPluginFactory.deleteDexFile(PackageName.MEDIA_PLUGIN);
//				// 多媒体插件包卸载，重启桌面
//				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//						IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
			}
		}
	}
	
	/**
	 * 检查是否资源管理插件包更新
	 * @param object
	 */
	private void checkIsMediaManagementPluginUpdate(Object object) {
		if (object != null && object instanceof String) {
			if (((String) object).equals(PackageName.MEDIA_PLUGIN)) {
				// 多媒体插件包更新，删除.dex文件
				MediaPluginFactory.deleteDexFile(PackageName.MEDIA_PLUGIN);
				// 多媒体插件包更新，重启桌面
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
			}
		}
	}

	/**
	 * 是否为新安装的应用程序.用于构建内部列表时判断
	 * 
	 * @param info
	 * @return
	 */
	private synchronized boolean isNewInstall(AppItemInfo info) {
		if (info != null && mCachedApps != null && mCachedApps.size() > 0) {
			synchronized (mCachedApps) {
				for (CacheItemInfo cacheItem : mCachedApps) {
					if (cacheItem != null && cacheItem.isInstall) {
						if (info.equals(cacheItem.itemInfo)) {
							return true;
						}
					}

				}
			}
		}
		return false;
	}

	/**
	 * <br>
	 * 功能简述:设置更新信息 <br>
	 * 功能详细描述:根据网络获取的应用程序更新信息检查itemInfos中的程序信息是否有更新，如有则设置显示更新标识 <br>
	 * 注意:
	 * 
	 * @param itemInfos
	 * @param inFolder
	 */
	public void checkAppUpdate(ArrayList<? extends FunItemInfo> itemInfos,
			boolean inFolder) {
		if (itemInfos == null) {
			return;
		}
		AppsBean beans = null;
		if (inFolder) {
			beans = mUpdateBeansForFolder;
		} else {
			beans = mUpdateBeans;
		}

		int size = itemInfos.size();
		FunAppItemInfo appInfo = null;

		for (int i = 0; i < size; i++) {
			FunItemInfo info = itemInfos.get(i);
			if (info instanceof FunAppItemInfo) {
				appInfo = (FunAppItemInfo) info;
				appInfo.setIsUpdate(false);
			}
		}

		if (beans != null && beans.mListBeans != null) {
			for (AppBean bean : beans.mListBeans) {
				for (int i = 0; i < size; i++) {
					FunItemInfo info = itemInfos.get(i);
					if (info != null && info.getType() == FunItemInfo.TYPE_APP) {
						String pkName = info.getIntent().getComponent()
								.getPackageName();
						if (pkName != null && pkName.equals(bean.mPkgName)) {
							if (info instanceof FunAppItemInfo) {
								appInfo = (FunAppItemInfo) info;
								appInfo.setIsUpdate(true);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:清除所有更新信息，包括功能表中所有程序的更新信息和缓存中保存的从网络中获取的应用更新数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void clearAllAppUpdate() {
		mUpdateBeans = null;
		mUpdateBeansForFolder = null;
		synchronized (mLock) {
			if (mAllAppItemInfos != null) {
				for (FunItemInfo info : mAllAppItemInfos) {
					FunAppItemInfo appInfo = null;
					if (info != null && info instanceof FunAppItemInfo) {
						appInfo = (FunAppItemInfo) info;
						appInfo.setIsUpdate(false);
					}
				}
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:清除某一项更新信息 <br>
	 * 功能详细描述:单独清除某一个应用程序的更新标识 <br>
	 * 注意:
	 * 
	 * @param item
	 */
	public void clearAppUpdateInfo(FunItemInfo item) {
		if (mAllAppItemInfos != null && item != null) {
			if (item.getIntent().getComponent() == null) {
				return;
			}
			String pkName = item.getIntent().getComponent().getPackageName();
			synchronized (mLock) {
				for (FunItemInfo info : mAllAppItemInfos) {
					FunAppItemInfo appInfo = null;
					if (info instanceof FunAppItemInfo) {
						appInfo = (FunAppItemInfo) info;
						ComponentName temp = info.getIntent().getComponent();
						if (temp != null && pkName != null) {
							if (pkName.equals(temp.getPackageName())) {
								appInfo.setIsUpdate(false);
							}
						}
					}
				}
			}
		}

		clearAppUpdateInfoForFolder(item);
	}

	/**
	 * 删除文件夹内图标的更新信息
	 * 
	 * @param item
	 */
	public void clearAppUpdateInfoForFolder(FunItemInfo item) {

		if (item != null && item instanceof FunAppItemInfo) {
			FunAppItemInfo appInfo = (FunAppItemInfo) item;
			appInfo.setIsUpdate(false);
		}
		if (mUpdateBeansForFolder != null && item != null
				&& mUpdateBeansForFolder.mListBeans != null) {
			if (item.getIntent().getComponent() == null) {
				return;
			}
			String pkName = item.getIntent().getComponent().getPackageName();

			Iterator<AppBean> iterator = mUpdateBeansForFolder.mListBeans
					.iterator();
			while (iterator.hasNext()) {
				AppBean bean = iterator.next();
				if (pkName != null && pkName.equals(bean.mPkgName)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	/**
	 * 释义类，决策该状态下的操作
	 * 
	 * @author guodanyang
	 * 
	 */
	private class StateParaphrase {
		// 状态
		private int mState = NONE;

		// State
		private static final int NONE = 0;
		private static final int SDCARD_IS_OK = 1;
		private static final int TIME_IS_UP = 2;

		public void updateState(final int state) {
			// LogUnit.i("FunControler",
			// "StateParaphrase::updateState() -- mState" + mState);
			mState = state;
		}

		public boolean checkUpdate() {
			boolean toUpdate = false;
			switch (mState) {
			case NONE: {
				toUpdate = false;
				break;
			}
			case SDCARD_IS_OK: {
				toUpdate = true;
				break;
			}

			case TIME_IS_UP: {
				// 若SD被移走或者位于正常的mount状态,则更新
				String sdState = Environment.getExternalStorageState();
				// Log.i("FunControler",
				// "---------------checkUpdate, status: "
				// + sdState);
				if (sdState.equals(Environment.MEDIA_MOUNTED)
						|| sdState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
				/* || sdState.equals(Environment.MEDIA_REMOVED) */) {
					toUpdate = true;
				}
				// toUpdate = !Environment.getExternalStorageState().equals(
				// Environment.MEDIA_SHARED);
				break;
			}
			default:
				break;
			}
			return toUpdate;
		}
	}

	@Override
	public void cleanup() {
		clearAllObserver();
	}

	/**
	 * 缓存类，用于记录安装卸载程序信息
	 * 
	 * @author yangguanxiang
	 * 
	 */
	private class CacheItemInfo {
		public AppItemInfo itemInfo;
		public boolean isInstall; // True为安装, False为卸载

		public CacheItemInfo(AppItemInfo info, boolean isInstall) {
			this.itemInfo = info;
			this.isInstall = isInstall;
		}
	}

	/**
	 * 获取当前可更新的应用程序列表信息
	 * 
	 * @return
	 */
	public AppsBean getCurrentAppUpdateBeans() {
		return mUpdateBeans;
	}

	/**
	 * 是否需要清理重複數據，重複數據的定義就是文件夾外部有文件夾裏面也有的數據
	 * 
	 * @return
	 */
	public boolean needCleanDirtyData() {
		ArrayList<Long> folderIds = getFolderIds();
		if (folderIds == null || folderIds.isEmpty()) {
			return false;
		}

		ArrayList<String> appsIntent = mFunDataModel.getAllAppsIntentStr();
		if (appsIntent == null || appsIntent.isEmpty()) {
			return false;
		}

		int size = appsIntent.size();
		Intent intent = null;
		String intents = null;
		for (int i = 0; i < size; i++) {
			intents = appsIntent.get(i);
			if (intents == null || "".equals(intents)) {
				continue;
			}
			intent = ConvertUtils.stringToIntent(intents);
			// 有重複數據
			if (intent != null
					&& mFunDataModel.isInDBFolders(folderIds, intent)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <br>
	 * 功能简述:清除功能表所有程序重复数据 <br>
	 * 功能详细描述:清除功能表所有程序重复数据，规则是：如果功能表文件夹中已经有某个程序了而此时文件夹外部也存在这个程序的数据，则将文件夹外部的数据删除 <br>
	 * 注意:
	 */
	public void cleanDirtyData() {
		ArrayList<Long> folderIds = getFolderIds();
		if (folderIds == null || folderIds.isEmpty()) {
			return;
		}

		ArrayList<String> appsIntent = mFunDataModel.getAllAppsIntentStr();
		if (appsIntent == null || appsIntent.isEmpty()) {
			return;
		}

		int size = appsIntent.size();
		Intent intent = null;
		String intents = null;
		for (int i = 0; i < size; i++) {
			intents = appsIntent.get(i);
			if (intents == null || "".equals(intents)) {
				continue;
			}
			intent = ConvertUtils.stringToIntent(intents);
			// 刪除重複數據
			if (intent != null
					&& mFunDataModel.isInDBFolders(folderIds, intent)) {
				try {
					mFunDataModel.removeFunappItemInfo(intent);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 是否隱藏程序
	 * 
	 * @return
	 */
	public boolean isHiddeApp(Intent intent) {
		if (mAppConfigControler != null) {
			return mAppConfigControler.isHideApp(intent);
		}
		return false;
	}

	/**
	 * 更新 通讯统计
	 * 
	 * @param type
	 *            统计类型
	 * @param count
	 *            未读短信（邮件、来电）个数
	 */
	public void updateNotification(int type, int count) {
		// 如果收到广播时候，功能表未初始化，将信息保存在map中
		// 否则直接更新内存
		if (mHasInit) {
			// 未知会不会出现更新失败
			updateAppUnread(mAllAppItemInfos, type, count, null);
		} else {
			HashMap<Integer, Integer> notificationMap = mFunDataModel
					.getNotificationMap();
			if (null == notificationMap) {
				notificationMap = new HashMap<Integer, Integer>();
			}
			notificationMap.put(type, count);
		}
	}

	/**
	 * 更新列表中通讯统计应用的未读数
	 * 
	 * @param itemList
	 *            程序列表（包含文件夹+程序）
	 * @param type
	 *            通讯统计应用类型
	 * @param count
	 *            应用未读数
	 * @param folder
	 *            不为空，表示对应文件夹内的应用列表
	 * @return
	 */
	public boolean updateAppUnread(ArrayList<? extends FunItemInfo> itemList,
			int type, int count, FunFolderItemInfo folder) {
		if (null == itemList) {
			return false;
		}
		int total = 0;
		int oldCount = 0;
		boolean isInFolder = false;
		if (null != folder) {
			isInFolder = true;
			total = folder.getUnreadCount();
		}
		// SMS和CALL可能存在多个，因此需要遍历全部。
		boolean result = false;
		switch (type) {
		case NotificationType.NOTIFICATIONTYPE_SMS:
			for (FunItemInfo funItemInfo : itemList) {
				if (funItemInfo instanceof FunAppItemInfo) {
					if (AppIdentifier.isMessge(mContext,
							funItemInfo.getIntent())) {
						if (isInFolder) {
							oldCount = funItemInfo.getUnreadCount();
							total += count - oldCount;
							folder.setUnreadCount(total);
						}
						funItemInfo.setNotificationType(type);
						funItemInfo.setUnreadCount(count);
						result = true;
					}
				}
				// 查看是否在文件夹里面
				else if (funItemInfo instanceof FunFolderItemInfo) {
					if (updateFolderItemsUnread(
							(FunFolderItemInfo) funItemInfo, type, count)) {
						result = true;
					}
				}
			}
			return result;

		case NotificationType.NOTIFICATIONTYPE_CALL:
			for (FunItemInfo funItemInfo : itemList) {
				if (funItemInfo instanceof FunAppItemInfo) {
					if (AppIdentifier.isDial(mContext, funItemInfo.getIntent())) {
						if (isInFolder) {
							oldCount = funItemInfo.getUnreadCount();
							total += count - oldCount;
							folder.setUnreadCount(total);
						}
						funItemInfo.setNotificationType(type);
						funItemInfo.setUnreadCount(count);
						result = true;
					}
				} else if (funItemInfo instanceof FunFolderItemInfo) {
					if (updateFolderItemsUnread(
							(FunFolderItemInfo) funItemInfo, type, count)) {
						result = true;
					}
				}
			}
			return result;

		case NotificationType.NOTIFICATIONTYPE_GMAIL:
			for (FunItemInfo funItemInfo : itemList) {
				if (funItemInfo instanceof FunAppItemInfo) {
					if (AppIdentifier
							.isGmail(mContext, funItemInfo.getIntent())) {
						if (isInFolder) {
							oldCount = funItemInfo.getUnreadCount();
							total += count - oldCount;
							folder.setUnreadCount(total);
						}
						funItemInfo.setNotificationType(type);
						funItemInfo.setUnreadCount(count);
						return true;
					}
				} else if (funItemInfo instanceof FunFolderItemInfo) {
					if (updateFolderItemsUnread(
							(FunFolderItemInfo) funItemInfo, type, count)) {
						return true;
					}
				}
			}
			break;

		case NotificationType.NOTIFICATIONTYPE_K9MAIL:
			for (FunItemInfo funItemInfo : itemList) {
				if (funItemInfo instanceof FunAppItemInfo) {
					if (AppIdentifier.isK9mail(mContext,
							funItemInfo.getIntent())) {
						if (isInFolder) {
							oldCount = funItemInfo.getUnreadCount();
							total += count - oldCount;
							folder.setUnreadCount(total);
						}
						funItemInfo.setNotificationType(type);
						funItemInfo.setUnreadCount(count);
						return true;
					}
				} else if (funItemInfo instanceof FunFolderItemInfo) {
					if (updateFolderItemsUnread(
							(FunFolderItemInfo) funItemInfo, type, count)) {
						return true;
					}
				}
			}
			break;

		case NotificationType.NOTIFICATIONTYPE_FACEBOOK:
			for (FunItemInfo funItemInfo : itemList) {
				if (funItemInfo instanceof FunAppItemInfo) {
					if (AppIdentifier.isFacebook(mContext,
							funItemInfo.getIntent())) {
						if (isInFolder) {
							oldCount = funItemInfo.getUnreadCount();
							total += count - oldCount;
							folder.setUnreadCount(total);
						}
						funItemInfo.setNotificationType(type);
						funItemInfo.setUnreadCount(count);
						return true;
					}
				} else if (funItemInfo instanceof FunFolderItemInfo) {
					if (updateFolderItemsUnread(
							(FunFolderItemInfo) funItemInfo, type, count)) {
						return true;
					}
				}
			}
			break;

		case NotificationType.NOTIFICATIONTYPE_SinaWeibo:
			for (FunItemInfo funItemInfo : itemList) {
				if (funItemInfo instanceof FunAppItemInfo) {
					if (AppIdentifier.isSinaWeibo(mContext,
							funItemInfo.getIntent())) {
						if (isInFolder) {
							oldCount = funItemInfo.getUnreadCount();
							total += count - oldCount;
							folder.setUnreadCount(total);
						}
						funItemInfo.setNotificationType(type);
						funItemInfo.setUnreadCount(count);
						return true;
					}
				} else if (funItemInfo instanceof FunFolderItemInfo) {
					if (updateFolderItemsUnread(
							(FunFolderItemInfo) funItemInfo, type, count)) {
						return true;
					}
				}
			}
			break;

		case NotificationType.NOTIFICATIONTYPE_MORE_APP:
			for (FunItemInfo funItemInfo : itemList) {
				if (funItemInfo instanceof FunAppItemInfo) {
					if (funItemInfo.getNotificationType() == type) {
						if (isInFolder) {
							NotificationControler control = AppCore
									.getInstance().getNotificationControler();
							total = control.getTotalUnreadCount(folder);
							folder.setUnreadCount(total);
						}
						funItemInfo
								.setUnreadCount(funItemInfo.getUnreadCount());
						result = true;
					}
				} else if (funItemInfo instanceof FunFolderItemInfo) {
					if (updateFolderItemsUnread(
							(FunFolderItemInfo) funItemInfo, type, count)) {
						result = true;
					}
				}
			}
			break;

		default:
			return false;
		}
		return false;
	}

	/**
	 * 更新文件夹中apps的未读数
	 * 
	 * @param folderInfo
	 * @param type
	 *            通讯统计应用类型
	 * @param count
	 *            未读数
	 */
	public boolean updateFolderItemsUnread(FunFolderItemInfo folderInfo,
			int type, int count) {
		if (null == folderInfo) {
			return false;
		}
		ArrayList<FunAppItemInfo> items = folderInfo.getFunAppItemInfos();
		// 迭代（支持文件夹嵌套文件夹）
		if (updateAppUnread(items, type, count, folderInfo)) {
			return true;
		}
		return false;
	}

	/**
	 * 更新 通讯统计 (报废)
	 * 
	 * @param itemList
	 * @param map
	 *            未初始化功能表之前，收到通讯统计应用更新的通知，保存在map中
	 */
	public boolean updateAppUnread(ArrayList<? extends FunItemInfo> itemList,
			HashMap<Integer, Integer> map) {
		if (null == itemList || null == map || map.isEmpty()) {
			return false;
		}
		boolean result = false;
		ArrayList<Integer> removeKeyList = new ArrayList<Integer>();
		for (HashMap.Entry<Integer, Integer> entry : map.entrySet()) {
			// 更新完后把map清空
			if (updateAppUnread(itemList, entry.getKey(), entry.getValue(),
					null)) {
				removeKeyList.add(entry.getKey());
				result = true;
			}
		}
		for (Integer key : removeKeyList) {
			map.remove(key);
		}
		if (map.isEmpty()) {
			map = null;
		}
		return result;
	}

	// public FileEngine getFileEngine(){
	// return mFileEngine;
	// }

	public void scanSDCard() {
		if (mFileEngine != null) {
			mFileEngine.scanSDCard();
		}
	}

	/**
	 * 获取所有音频文件,可以滤掉被隐藏的专辑和播放列表内的文件
	 * 
	 * @param filterCategoryHide
	 *            是否过滤掉隐藏专辑内的文件
	 * @param filterCategoryHide
	 *            是否过滤掉隐藏播放列表内的文件
	 * @author yangbing
	 * */
	public ArrayList<FileInfo> getAllAudio(boolean filterCategoryHide,
			boolean filterPlaylistHide) {
		ArrayList<FileInfo> files = null;
		if (mFileEngine != null) {
			files = mFileEngine.getAllAudio();
		}
		if (filterCategoryHide) {
			HashMap<String, String> hideDatas = mFunDataModel
					.getAllHideMediaDatas(FileEngine.TYPE_AUDIO);
			filterHideAudioFiles(files, hideDatas);
		}
		if (filterPlaylistHide) {
			HashSet<Integer> hideDatas = mFunDataModel
					.getAllHidePlayListAudioDatas();
			filterHidePlaylistAudioFiles(files, hideDatas);
		}

		return files;
	}

	public void registerFileObserver(FileObserver observer) {
		if (mFileEngine != null) {
			mFileEngine.setFileObserver(observer);
		}
	}

	public void destroyFileEngine() {
		if (mFileEngine != null) {
			mFileEngine.destroy();
			mFileEngine = null;
		}
	}

	public void buildFileEngine() {
		if (null == mFileEngine) {
			mFileEngine = new FileEngine(mContext);
			if (MediaPluginFactory.isMediaPluginExist(mContext)) { 
				IMediaManager mediaManager = MediaPluginFactory.getMediaManager();
				if (mediaManager != null) {
					mediaManager.setFileEngine(mFileEngine);
				}
			}
		}

	}

	private boolean mediaInHidePool(String uri, HashMap<String, String> pool) {
		if (uri == null || pool == null) {
			return false;
		}
		String temp = pool.get(uri);
		if (temp != null && uri.equals(temp)) {
			return true;
		}
		return false;
	}

	public boolean isInited() {
		return mHasInit;
	}

	/**
	 * 按使用频率排序，并保存到数据库
	 * 
	 * @param order
	 * @throws DatabaseException
	 */
	public synchronized void sortByFrequencyAndSave(String order)
			throws DatabaseException {
		if (!mHasInit) {
			return;
		}
		// 排序
		sortByFrequency(order);
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		mFunDataModel.updateFunAppItemsIndex(mAllAppItemInfos);
		// 文件夹中排序
		// sortFoldersByFrequencyAndSave(order);
		// 通知
		Message message = mHandler.obtainMessage();
		message.what = MSG_FINISHSORT;
		mHandler.sendMessage(message);
	}

	private void sortByFrequency(String order) {
		String sortMethod = "getClickedCount";
		String pritMethod = "isPriority";
		Class[] methodArgClasses = new Class[] { Context.class };
		Object[] methodArg = new Object[] { mContext };
		try {
			// SortUtils.sortByInt(mAllAppItemInfos, sortMethod,
			// methodArgClasses,
			// methodArg, order);
			SortUtils.sortSomePriority(mAllAppItemInfos, pritMethod,
					sortMethod, methodArgClasses, methodArg, order,
					SortUtils.COMPARE_TYPE_INT);
			buildSpecialItemsOrder();
		} catch (Exception e) {
			// 弹出提示排序失败
			Message message = mHandler.obtainMessage();
			message.what = MSG_SORTFAILED;
			mHandler.sendMessage(message);
		}
	}

	// private void sortFoldersByFrequencyAndSave(String order) {
	// if (!mHasInit) {
	// return;
	// }
	//
	// int size = mAllAppItemInfos.size();
	// FunItemInfo funItemInfo = null;
	// for (int i = 0; i < size; ++i) {
	// funItemInfo = mAllAppItemInfos.get(i);
	// if (null == funItemInfo) {
	// continue;
	// }
	//
	// if (funItemInfo.getType() == FunItemInfo.TYPE_APP) {
	// continue;
	// }
	//
	// ((FunFolderItemInfo) funItemInfo).sortByFrequencyAndSave(mContext,
	// order);
	// }
	// }


	/**
	 * 过滤掉隐藏专辑中的音乐文件
	 * 
	 * @param files
	 * @param hideDatas
	 */
	private void filterHideAudioFiles(ArrayList<FileInfo> files,
			HashMap<String, String> hideDatas) {
		if (files == null || hideDatas == null) {
			return;
		}

		Iterator<FileInfo> it = files.iterator();
		while (it.hasNext()) {
			AudioFile info = (AudioFile) it.next();
			if (mediaInHidePool(info.album, hideDatas)) {
				it.remove();
			}
		}
	}

	/**
	 * 过滤掉隐藏播放列表中的音乐文件
	 * 
	 * @param files
	 * @param hideDatas
	 */
	private void filterHidePlaylistAudioFiles(ArrayList<FileInfo> files,
			HashSet<Integer> hideDatas) {
		if (files == null || hideDatas == null) {
			return;
		}

		Iterator<FileInfo> it = files.iterator();
		while (it.hasNext()) {
			FileInfo info = it.next();
			if (hideDatas.contains(info.dbId)) {
				it.remove();
			}
		}
	}

	/**
	 * 
	 * 加载所有多媒体数据，并过滤后发消息返回（用于功能表搜索多媒体数据）
	 */
	public void refreshAllMediaData() {
		new Thread(new Runnable() { // 启动异步线程加载多媒体数据
				@Override
				public void run() {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					boolean isFilter = false;
					if (MediaPluginFactory.isMediaPluginExist(mContext)) { // 存在插件包，需要从plugin包中取出隐藏数据进行过滤
						isFilter = true;
					}
					if (mFileEngine != null) {
						try {
							if (!mFileEngine.isMediaDataInited(FileEngine.TYPE_IMAGE)) {
								mFileEngine.setInitingData(FileEngine.TYPE_IMAGE, true);
								// 加载图片数据
								mFileEngine.initImage();
								mFileEngine.setInitingData(FileEngine.TYPE_IMAGE, false);
							}
							ArrayList<FileInfo> imageData = getAllImage(isFilter);
							GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_SEARCH_FRAME,
									IDiyMsgIds.MEDIA_DATA_LOAD_FINISH, FileEngine.TYPE_IMAGE, null,
									imageData);
						
							if (!mFileEngine.isMediaDataInited(FileEngine.TYPE_AUDIO)) {
								mFileEngine.setInitingData(FileEngine.TYPE_AUDIO, true);
								// 加载音乐数据
								mFileEngine.initAudio();
								mFileEngine.setInitingData(FileEngine.TYPE_AUDIO, false);
							}
							ArrayList<FileInfo> audioData = getAllAudio(isFilter, isFilter);
							if (audioData == null) {
								audioData = new ArrayList<FileInfo>();
							}
							GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_SEARCH_FRAME,
									IDiyMsgIds.MEDIA_DATA_LOAD_FINISH, FileEngine.TYPE_AUDIO, null,
									audioData);
						
							if (!mFileEngine.isMediaDataInited(FileEngine.TYPE_VIDEO)) {
								mFileEngine.setInitingData(FileEngine.TYPE_VIDEO, true);
								// 加载视频数据
								mFileEngine.initVideo();
								mFileEngine.setInitingData(FileEngine.TYPE_VIDEO, false);
							}
							ArrayList<FileInfo> videoData = getAllVideo(isFilter);
							GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_SEARCH_FRAME,
									IDiyMsgIds.MEDIA_DATA_LOAD_FINISH, FileEngine.TYPE_VIDEO, null,
									videoData);

							DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
									AppFuncConstants.PROGRESSBAR_HIDE, null);
						} catch (NullPointerException ex) {
							// 如果在跑这段方法的时候执行退出搜索层的动作，外界会把引擎销毁，导致空指针，在这里进行捕捉，
							// 所带来的后果为本次搜索所请求的多媒体数据无法返回，导致搜索中无多媒体数据
						}
					}
				}
			}).start();
	}

	/**
	 * 
	 * 获取所有视频文件信息
	 * 
	 * @param filterCategoryHide
	 *            是否过滤隐藏视频文件
	 * @return
	 */
	@SuppressWarnings("null")
	public ArrayList<FileInfo> getAllVideo(boolean filterCategoryHide) {
		ArrayList<FileInfo> temp = new ArrayList<FileInfo>();
		if (mFileEngine != null) {
			ArrayList<FileInfo> videoInfoList = mFileEngine.getAllVideo();
			if (videoInfoList != null) {
				HashMap<String, String> hideDatas = mFunDataModel
						.getAllHideMediaDatas(FileEngine.TYPE_VIDEO);
				for (FileInfo fileInfo : videoInfoList) {
					if (filterCategoryHide && mediaInHidePool(fileInfo.uri, hideDatas)) {
						continue;
					}
					fileInfo.needHide = false;
					temp.add(fileInfo);
				}
			}
		}
		return temp;
	}

	/**
	 * 
	 * 获取所有视频图片信息
	 * 
	 * @param filterCategoryHide
	 *            是否过滤隐藏图片文件
	 * @return
	 */
	public ArrayList<FileInfo> getAllImage(boolean filterCategoryHide) {
		ArrayList<FileInfo> temp = new ArrayList<FileInfo>();
		if (mFileEngine != null) {
			ArrayList<Category> imageCategoryInfoList = mFileEngine
					.getImagePaths();
			if (imageCategoryInfoList != null) {
				HashMap<String, String> hideDatas = mFunDataModel
						.getAllHideMediaDatas(FileEngine.TYPE_IMAGE);
				for (Category imageCategoryInfo : imageCategoryInfoList) {
					if (imageCategoryInfo.files.isEmpty()
							|| (filterCategoryHide && mediaInHidePool(imageCategoryInfo.uri, hideDatas))) {
						continue;
					}
					for (FileInfo fileInfo : imageCategoryInfo.files) {
						if (fileInfo != null) {
							temp.add(fileInfo);
						}
					}
				}
			}
		}
		return temp;
	}

	/**
	 * 
	 * 打开多媒体文件（音乐、图片、视频）
	 */
	public void openMediaFile(FileInfo info, int openType, List<?> objs) {
		if (info instanceof AudioFile) {
			if (openType == MEDIA_FILE_OPEN_BY_SEARCH) {
				openAudioFile((AudioFile) info, true);
			} else {
				openAudioFile((AudioFile) info, false);
			}
		} else if (info instanceof ImageFile) {
			if (openType == MEDIA_FILE_OPEN_BY_SEARCH) {
				ArrayList<FileInfo> itemInfos = new ArrayList<FileInfo>();
				itemInfos.add(info);
				Bitmap b = ThumbnailManager.getInstance(mContext).getThumbnail(
						null, ThumbnailManager.TYPE_IMAGE, info.thumbnailId,
						info.thumbnailPath);
				if (b == null) { // 如果图片为空，则获取默认图片
					return;
				}
//				GoLauncher.sendMessage(this, IDiyFrameIds.MEDIA_CONTROLER,
//						IDiyMsgIds.SET_IMAGE_BROWSER_DATA, MEDIA_FILE_OPEN_BY_SEARCH, b, itemInfos);
				openImageFile(info, b, itemInfos);
			} else {
				openImageFile(info, null, null);
			}
		} else if (info instanceof VideoFile) {
			openVideoFile(info);
		}
	}

	/**
	 * 打开音频文件
	 * 
	 * @param fileInfo
	 * @param contentType
	 */
	public void openAudioFile(AudioFile info, boolean isOpenBySearch) {
		if (checkFile(mContext, info.fullFilePath)) {
			// String uri = null;
			// if (AppUtils.isMediaPluginExist(mContext)) {
			// uri = MediaManagerFactory.getMediaManager()
			// .getMusicDefaultOpenWay();
			// } else {
			// uri = MediaManagementOpenChooser.APP_NONE;
			// }
			FunAppSetting setting = GoSettingControler.getInstance(mContext)
					.getFunAppSetting();
			String uri = setting.getMediaOpenWay(FileEngine.TYPE_AUDIO);
			if (MediaManagementOpenChooser.APP_NONE.equals(uri)) {
				MediaManagementOpenChooser.getInstance(mContext).openChooser(
						info, info.mimeType,
						FileEngine.TYPE_AUDIO, info, isOpenBySearch);
			} else if (MediaManagementOpenChooser.APP_GO_MUSIC_PLAYER
					.equals(uri)) {
//				if (isOpenBySearch) {
//					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//							IFrameworkMsgId.HIDE_FRAME,
//							IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
//					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//							IFrameworkMsgId.SHOW_FRAME,
//							IDiyFrameIds.APPFUNC_FRAME, null, null);

					// DeliverMsgManager.getInstance().onChange(
					// AppFuncConstants.APP_FUNC_MAIN_VIEW,
					// AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
					// new Object[] {
					// MediamanagementTabBasicContent.CONTENT_TYPE_MUSIC_PLAY,
					// contentType, (long) info.dbId, info.album, isOpenBySearch
					// });
//				}
				if (MediaPluginFactory.isMediaPluginExist(mContext)) { // 存在插件包才打开播放器
					// 回到播放界面
					DeliverMsgManager.getInstance().onChange(
							AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.MUSIC_PLAYER, (long) info.dbId,
									info.album, isOpenBySearch, info });
				} else {
					MediaManagementOpenChooser.getInstance(mContext).openChooser(
							info, info.mimeType,
							FileEngine.TYPE_AUDIO, info, isOpenBySearch);
				}
			} else {
				try {
					// 使用系统播放器打开
					ApplicationIcon.sIsStartApp = true;
					Intent intent = Intent.getIntent(uri);
					intent.setDataAndType(
							Uri.parse("file://" + info.fullFilePath),
							info.mimeType);
					mContext.startActivity(intent);
				} catch (ActivityNotFoundException ex) {
					DeskToast.makeText(mContext, R.string.no_way_to_open_file,
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					DeskToast.makeText(mContext, R.string.no_way_to_open_file,
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public boolean checkFile(Context context, String path) {
		if (MediaFileUtil.isFileExist(path) && MediaFileUtil.getFileSize(path) > 0) {
			return true;
		} else {
			showFileNotFoundDialog(context);
			return false;
		}
	}

	private void showFileNotFoundDialog(Context context) {
		new AlertDialog.Builder(context).setTitle(R.string.media_file_not_found_title)
				.setMessage(R.string.media_file_not_found_message)
				.setPositiveButton(R.string.refresh, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						scanSDCard();
					}
				}).setNegativeButton(R.string.cancel, null).create().show();
	}
	
	/**
	 * 
	 * 打开图片文件
	 * 
	 * @param info
	 * @param itemInfos
	 * @param b
	 */
	public void openImageFile(FileInfo info, Bitmap b, ArrayList<FileInfo> itemInfos) {
		if (checkFile(mContext, info.fullFilePath)) {
			String mimeType = "image/*";
			// String uri = null;
			// if (AppUtils.isMediaPluginExist(mContext)) {
			// uri = MediaManagerFactory.getMediaManager()
			// .getImageDefaultOpenWay();
			// } else {
			// uri = MediaManagementOpenChooser.APP_NONE;
			// }
			FunAppSetting setting = GoSettingControler.getInstance(mContext)
					.getFunAppSetting();
			String uri = setting.getMediaOpenWay(FileEngine.TYPE_IMAGE);
			if (MediaManagementOpenChooser.APP_NONE.equals(uri)) {
				MediaManagementOpenChooser.getInstance(mContext).openChooser(info, mimeType,
						FileEngine.TYPE_IMAGE, b, itemInfos);
			} else if (MediaManagementOpenChooser.APP_GO_PIC_VIEWER.equals(uri)) {
				// 使用go图片浏览器打开
				if (MediaPluginFactory.isMediaPluginExist(mContext)) {
//					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//							IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.IMAGE_BROWSER_FRAME, null,
//							null);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.OPEN_IMAGE_BROWSER, new Object[]{b, itemInfos});
				} else {
					MediaManagementOpenChooser.getInstance(mContext).openChooser(info, mimeType,
							FileEngine.TYPE_IMAGE, new Object[]{b, itemInfos});
				}
			} else {
				try {
					// 使用系统播放器打开
					ApplicationIcon.sIsStartApp = true;
					Intent intent = Intent.getIntent(uri);
					intent.setDataAndType(
							Uri.parse("file://" + info.fullFilePath), mimeType);
					mContext.startActivity(intent);
				} catch (ActivityNotFoundException ex) {
					DeskToast.makeText(mContext, R.string.no_way_to_open_file,
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					DeskToast.makeText(mContext, R.string.no_way_to_open_file,
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/**
	 * 
	 * 打开视频文件
	 * 
	 * @param info
	 */
	public void openVideoFile(FileInfo info) {
		if (checkFile(mContext, info.fullFilePath)) {
			String mimeType = "video/*";
			if (mimeType != null) {
				ApplicationIcon.sIsStartApp = true;
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setType(mimeType);
				intent.setDataAndType(Uri.parse("file://" + info.uri), mimeType);
				try {
					mContext.startActivity(intent);
				} catch (ActivityNotFoundException ex) {
					DeskToast.makeText(mContext, R.string.no_way_to_open_file,
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/**
	 * 获取引擎
	 * 
	 * @return
	 */
	public FileEngine getFileEngine() {
		return mFileEngine;
	}

	/**
	 * 复制所有资源管理数据至插件包
	 */
	public boolean copyAllMediaData() {
		ArrayList<Cursor> cursorList = DataProvider.getInstance(mContext)
				.getAllMediaData();
		boolean ret = false;
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		Cursor playListCursor = cursorList.get(0); // 播放列表
		Cursor playListFileCursor = cursorList.get(1); // 播放列表文件
		Cursor hideFileCursor = cursorList.get(2); // 隐藏文件表
		try {
			if (playListCursor != null && playListCursor.moveToFirst()) { // 播放列表
				do {
					ContentValues values = new ContentValues();
					values.put(MediaManagementPlayListTable.ID, MediaDbUtil
							.getLong(playListCursor,
									MediaManagementPlayListTable.ID));
					values.put(MediaManagementPlayListTable.NAME, MediaDbUtil
							.getString(playListCursor,
									MediaManagementPlayListTable.NAME));
					values.put(MediaManagementPlayListTable.TYPE, MediaDbUtil
							.getInt(playListCursor,
									MediaManagementPlayListTable.TYPE));
					values.put(MediaManagementPlayListTable.UDATE, MediaDbUtil
							.getLong(playListCursor,
									MediaManagementPlayListTable.UDATE));
					ops.add(ContentProviderOperation
							.newInsert(
									MediaDataProviderConstants.PlayList.CONTENT_DATA_URI)
							.withValues(values).build());
				} while (playListCursor.moveToNext());
			}

			if (playListFileCursor != null && playListFileCursor.moveToFirst()) { // 播放列表文件
				do {
					ContentValues values = new ContentValues();
					values.put(
							MediaManagementPlayListFileTable.PLAYLISTID,
							MediaDbUtil
									.getLong(
											playListFileCursor,
											MediaManagementPlayListFileTable.PLAYLISTID));
					values.put(MediaManagementPlayListFileTable.FILEID,
							MediaDbUtil.getLong(playListFileCursor,
									MediaManagementPlayListFileTable.FILEID));
					values.put(MediaManagementPlayListFileTable.DATE,
							MediaDbUtil.getLong(playListFileCursor,
									MediaManagementPlayListFileTable.DATE));
					ops.add(ContentProviderOperation
							.newInsert(
									MediaDataProviderConstants.PlayListFile.CONTENT_DATA_URI)
							.withValues(values).build());
				} while (playListFileCursor.moveToNext());
			}

			if (hideFileCursor != null && hideFileCursor.moveToFirst()) { // 隐藏文件表
				do {
					ContentValues values = new ContentValues();
					values.put(MediaManagementHideTable.TYPE, MediaDbUtil
							.getInt(hideFileCursor,
									MediaManagementHideTable.TYPE));
					values.put(MediaManagementHideTable.URI, MediaDbUtil
							.getString(hideFileCursor,
									MediaManagementHideTable.URI));
					ops.add(ContentProviderOperation
							.newInsert(
									MediaDataProviderConstants.HideData.CONTENT_DATA_URI)
							.withValues(values).build());
				} while (hideFileCursor.moveToNext());
			}
			mContext.getContentResolver().applyBatch(
					MediaDataProviderConstants.AUTHORITY, ops);
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (playListCursor != null) {
				playListCursor.close();
			}
			if (playListFileCursor != null) {
				playListFileCursor.close();
			}
			if (hideFileCursor != null) {
				hideFileCursor.close();
			}
		}
		return ret;
	}

	/**
	 * 删除桌面所有资源管理数据
	 */
	public boolean deleteAllMediaData() {
		return DataProvider.getInstance(mContext).deleteAllMediaData();
	}
	
	private void notifyFolderNameChanged(final String name, FunFolderItemInfo folderItemInfo) {
		if (folderItemInfo == null) {
			return;
		}
		String folderOldName = folderItemInfo.getTitle();
		try {
			folderItemInfo.setTitle(name);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		// 通知桌面重命名
		ArrayList<String> nameList = new ArrayList<String>();
		// 第一个为新的名字
		nameList.add(name);
		// 第二个为以前的名字
		nameList.add(folderOldName);
		GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
				IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FOLDER_RENAME, 0,
				folderItemInfo.getFolderId(), nameList);
		GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
				IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.SCREEN_FOLDER_RENAME, 0,
				folderItemInfo.getFolderId(), nameList);
	}
}
