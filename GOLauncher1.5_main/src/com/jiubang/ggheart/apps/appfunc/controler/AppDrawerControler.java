package com.jiubang.ggheart.apps.appfunc.controler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.go.launcher.taskmanager.TaskMgrControler;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.appfunc.business.AllAppBussiness;
import com.jiubang.ggheart.apps.appfunc.business.AllBussinessHandler;
import com.jiubang.ggheart.apps.appfunc.business.RecentAppBussiness;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncExceptionHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.apps.desks.diy.AppInvoker.IAppInvokerListener;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.Controler;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述: 功能表模块控制器
 * <br>功能详细描述: 负责功能表模块内各业务逻辑处理器的生命周期控制，各业务逻辑处理器返回数据的合成和整理，任务派遣，状态监听等。
 * 
 * @author  yangguanxiang
 * @date  [2012-12-27]
 */
public class AppDrawerControler extends Controler implements ICleanable, IAppInvokerListener {
	// 这堆外部消息不想放在这里，很TMD烦
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
	public static final int HIDE_APPS = 17; // 隐藏程序更新
	public static final int ADDITEMS = 18;
	public static final int REMOVEITEMS = 19;
	
	private static AppDrawerControler sInstance;
	
	//	private FileEngine mFileEngine;
	private AllAppBussiness mAllAppBussiness;
	private RecentAppBussiness mRecentAppBussiness;
//	private RunningAppBussiness mRunningAppBussiness;
	//	private SearchBussiness mSearchBussiness;
	private AppConfigControler mAppConfigControler;
	
	private AllBussinessHandler mAllAppHandler;
	// 安装卸载的缓存数据
	private ArrayList<CacheItemInfo> mCachedApps;
	/**
	 * 最近打开的缓存数据
	 */
	ArrayList<FunAppItemInfo> mRecFunAppItems;
	/**
	 * 正在运行的缓存数据
	 */
	ArrayList<FunAppItemInfo> mProManageAppItems;
	
	private ConcurrentHashMap<Intent, FunItemInfo> mAllFunItemInfoMap;
	protected ArrayList<FunItemInfo> mItemInfosExceptHide;
	
	public static final String GOSTORECOUNT = "gostorecount";
	public static final String GOSTORE_SHOW_MESSAGE = "gostore_show_message";
	public static final String APPFUNC_APPMENU_SHOW_MESSAGE = "appfunc_appmenu_show_message";
	public static final String APPICON_SHOW_MESSSAGE = "appicon_show_message";

	private IBackgroundInfoChangedObserver mRecentAppObserver;
	private IBackgroundInfoChangedObserver mProManageObserver;
	private Object mInitAllAppLock = new Object();
	private TaskMgrControler mTaskMgrControler;

	public void setRecentAppObserver(IBackgroundInfoChangedObserver recentAppObserver) {
		mRecentAppObserver = recentAppObserver;
	}
	
	public void setProManageObserver(IBackgroundInfoChangedObserver proManageObserver) {
		mProManageObserver = proManageObserver;
	}

	public static AppDrawerControler getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new AppDrawerControler(context);
		}
		return sInstance;
	}

	private AppDrawerControler(Context context) {
		super(context);
//		mFunControler = FunControler.getInstance(context);
		// 监听程序注册信息的改变
		mAppConfigControler = AppConfigControler.getInstance(context);
		// 注册为监听者
		mAppConfigControler.registerObserver(this);
		mAllAppHandler = new AllBussinessHandler(context, this);
		mAllAppBussiness = new AllAppBussiness(context, mAllAppHandler);
		mRecentAppBussiness = new RecentAppBussiness(context);
		mAllFunItemInfoMap = new ConcurrentHashMap<Intent, FunItemInfo>();
		mItemInfosExceptHide = new ArrayList<FunItemInfo>();
		mTaskMgrControler = AppCore.getInstance().getTaskMgrControler();
	}

	public ArrayList<AppItemInfo> getRecentAppItems() {
		return mRecentAppBussiness.getRecentAppItems();
	}
	
	
	public ArrayList<FunAppItemInfo> getRecentFunAppItems(int maxCount) {
		if (mRecFunAppItems == null) {
			mRecFunAppItems  = new ArrayList<FunAppItemInfo>();
		} else {
			mRecFunAppItems.clear();
		}
		ArrayList<AppItemInfo> recAppItems = mRecentAppBussiness.getRecentAppItems();
		
		for (int i = 0; i < recAppItems.size() && i < maxCount; i++) {
			FunAppItemInfo itemInfo = (FunAppItemInfo) mAllFunItemInfoMap.get(recAppItems.get(i).mIntent);
			if (itemInfo != null) {
				mRecFunAppItems.add(itemInfo);
			}
		}
		return mRecFunAppItems;
	}

	/**
	 * <br>功能简述: 添加最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intent
	 * @param index
	 */
	public void addRecentAppItem(final Intent intent) {
		mRecentAppBussiness.addRecentAppItem(intent);
		if (mRecentAppObserver != null) {
			mRecentAppObserver.handleChanges(MessageID.APP_ADDED, null, null);
		}
	}

	/**
	 * <br>功能简述: 删除最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intent
	 */
	public void removeRecentAppItem(final Intent intent) {
		mRecentAppBussiness.removeRecentAppItem(intent);
		if (mRecentAppObserver != null) {
			mRecentAppObserver.handleChanges(MessageID.APP_REMOVED, null, null);
		}
	}

	/**
	 * <br>功能简述: 删除所有最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void removeAllRecentAppItems() {
		mRecentAppBussiness.removeAllRecentAppItems();
		if (mRecentAppObserver != null) {
			mRecentAppObserver.handleChanges(MessageID.CLEAR_RECENTAPP, null, null);
		}
	}

	/**
	 * <br>功能简述: 把缓存数据更新到真实数据集合中
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void refreshItemsFromCopy() {
		mRecentAppBussiness.refreshItemsFromCopy();
	}

	/**
	 * 获取功能表所有数据，包含隐藏数据（没执行过加载就是空数据）
	 */
	public List<FunItemInfo> getFunItemInfos() {
		return mAllAppBussiness.getFunItemInfos();
	}
	
	public List<FunItemInfo> getFunItemInfosExceptHide(boolean isRefreshList) {
		if (isRefreshList) {
			pickUpHideApps(getFunItemInfos());
		}
		return mItemInfosExceptHide;
	}
	
	public LinkedList<FunAppItemInfo> getFunItemInfosExceptFolder() {
		LinkedList<FunAppItemInfo> appItemInfos = new LinkedList<FunAppItemInfo>();
		Iterator<FunItemInfo> iter = mItemInfosExceptHide.iterator();
		while (iter.hasNext()) {
			FunItemInfo funItemInfo = iter.next();
			if (funItemInfo instanceof FunAppItemInfo) {
				appItemInfos.add((FunAppItemInfo) funItemInfo);
			}
		}
		return appItemInfos;
	} 
	
	/**
	 * 从应用列表中挑出隐藏应用，提供新的列表给grid显示<br>
	 * 注意：目前该方法可能会与加载的线程同时执行
	 * @param allAppList
	 */
	private void pickUpHideApps(List<FunItemInfo> allAppList) {
		// 做次垃圾回收，以防泄漏
		if (mItemInfosExceptHide != null) {
			mItemInfosExceptHide.clear();
		}
		synchronized (AllAppBussiness.getAllAppLock()) {
			Iterator<FunItemInfo> iter = allAppList.iterator();
			while (iter.hasNext()) {
				FunItemInfo funItemInfo = iter.next();
				if (!funItemInfo.isHide()) {
					mItemInfosExceptHide.add(funItemInfo);
				}
			}
			AllAppBussiness.getAllAppLock().notify();
		}
	}
	
	/**
	 * 异步加载功能表数据
	 */
	public void startAllAppInitThead() {
		synchronized (mInitAllAppLock) {
			if (mAllAppHandler.isStartedInitAllApp()) {
				return;
			}
			if (mAllFunItemInfoMap != null && !mAllFunItemInfoMap.isEmpty()) {
				mAllFunItemInfoMap.clear();
			}
			mAllAppBussiness.startInitThread(mCachedApps, mAllFunItemInfoMap);
		}
	}

	@Override
	public void onInvokeApp(Intent intent) {
		if (intent != null) {
			addRecentAppItem(intent);
		}
	}

	public void handleMessage(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.EVENT_UNINSTALL_APP : {
				mRecentAppBussiness.handleUnInstall((ArrayList<AppItemInfo>) objects);
				mRecentAppObserver.handleChanges(MessageID.APP_REMOVED, object, objects);
			}
				break;
			default :
				break;
		}
	}

	@Override
	public void cleanup() {
		if (mRecentAppBussiness != null) {
			mRecentAppBussiness.cleanup();
			mRecentAppBussiness = null;
		}
	}

	public static void destroy() {
		if (sInstance != null) {
			sInstance.cleanup();
			sInstance = null;
		}
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
//			case AppConfigControler.ADDHIDEITEM: {
//				handleHideItem((Intent) object, true);
//			}
//				break;
			case AppConfigControler.ADDHIDEITEMS: {
				ArrayList<Intent> list = (ArrayList<Intent>) objects;
				for (Intent intent : list) {
					FunItemInfo info = mAllAppBussiness.getFunItemInfo(intent);
					if (info != null) {
						info.setHideInfo(mAppConfigControler.getHideInfo(intent));
					}
				}
				broadCast(HIDE_APPS, param, object, objects);
			}
				break;
//			case AppConfigControler.DELETEHIDEITEM: {
//				handleHideItem((Intent) object, false);
//			}
//				break;
//			case AppConfigControler.DELETEHIDEITEMS: {
//				handleHideItems((ArrayList<Intent>) objects, false);
//			}
//				break;
//			case AppConfigControler.HIDEAPPCHANG: {
//				handleHideItems((ArrayList<Intent>) objects);
//			}
//				break;
			default:
				break;
		}
	}

	private void handleHideItems(ArrayList<Intent> intents) {
		
		
	}

//	/**
//	 * 设置批量隐藏/不隐藏程序
//	 * 
//	 * @param appItems
//	 */
//	private void handleHideItems(final ArrayList<Intent> appItems,
//			final boolean hide) {
//		if (null == appItems) {
//			return;
//		}
//		Intent it = null;
//		int size = appItems.size();
//		for (int i = 0; i < size; ++i) {
//			it = appItems.get(i);
//			if (null == it) {
//				continue;
//			}
//			handleHideItem(it, hide);
//		}
//	}

//	/**
//	 * 设置隐藏程序
//	 * 
//	 * @param intent
//	 */
//	private void handleHideItem(final Intent intent, final boolean hide) {
//		if (null == intent) {
//			return;
//		}
//		if (mAllAppHandler.isInitedAllFunItemInfo()) {
//			FunItemInfo info = mAllAppBussiness.getFunItemInfo(intent);
//				// 若在根目录
//			if (info != null) {
//				if (info instanceof FunAppItemInfo) {
//					info.setHide(hide);
//				} else if (info instanceof FunFolderItemInfo) {
//					// 若是文件夹
//					((FunFolderItemInfo) info).setHideFunAppItemInfo(intent, hide);
//				}
//			}
//		}
//	}

	public void startSaveThread() {
		mAllAppBussiness.startSaveThread();
	}
	
	public boolean isInitedAllFunItemInfo() {
		return mAllAppHandler.isInitedAllFunItemInfo();
	}
	
	public void handleCacheInfo(Object object, boolean isInstall) {
		addInCacheList((ArrayList<AppItemInfo>) object, isInstall);
		if (mAllAppHandler.isInitedAllFunItemInfo()) {
			handleCachedAppsList();
		}
		
	}
	
	public void addFunItemInfo(int location, FunItemInfo funItemInfo, boolean needRefreash) {
		try {
			if (funItemInfo instanceof FunFolderItemInfo) {
				mAllFunItemInfoMap.put(funItemInfo.getIntent(), funItemInfo);
			}
			mItemInfosExceptHide.add(location, funItemInfo);
			mAllAppBussiness.addFunItemInfo(location, funItemInfo, true);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
		if (needRefreash) {
			Message msg = mAllAppHandler.obtainMessage();
			msg.what = AllBussinessHandler.MSG_ADDITEM;
			msg.arg1 = location;
			msg.obj = funItemInfo;
			mAllAppHandler.sendMessage(msg);
		}
	}
	
	public void addFunItemInfos(int location, ArrayList<FunItemInfo> funItemInfos, boolean needRefreash) {
		try {
			for (FunItemInfo funItemInfo : funItemInfos) {
				if (funItemInfo instanceof FunFolderItemInfo) {
					mAllFunItemInfoMap.put(funItemInfo.getIntent(), funItemInfo);
				}
			}
			mItemInfosExceptHide.addAll(location, funItemInfos);
			mAllAppBussiness.addFunItemInfos(location, funItemInfos);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
		if (needRefreash) {
			Message msg = mAllAppHandler.obtainMessage();
			msg.what = AllBussinessHandler.MSG_ADDITEMS;
			msg.arg1 = location;
			msg.obj = funItemInfos;
			mAllAppHandler.sendMessage(msg);
		}
	}
	
	public void removeFunItemInfo(FunItemInfo funItemInfo, boolean needRefreash) {
		int index = -1;
		try {
			if (funItemInfo instanceof FunFolderItemInfo) {
				mAllFunItemInfoMap.remove(funItemInfo.getIntent());
			}
			index = mItemInfosExceptHide.indexOf(funItemInfo);
			mItemInfosExceptHide.remove(funItemInfo);
			mAllAppBussiness.removeFunItemInfo(funItemInfo);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
		if (needRefreash) {
			Message msg = mAllAppHandler.obtainMessage();
			msg.what = AllBussinessHandler.MSG_REMOVEITEM;
			msg.obj = funItemInfo;
			msg.arg1 = index;
			mAllAppHandler.sendMessage(msg);
		}
	}
	
	public void removeFunItemInfo(int location, boolean needRefreash) {
		FunItemInfo itemInfo = null;
		int index = -1;
		try {
			itemInfo = mAllAppBussiness.removeFunItemInfo(location);
			index = mItemInfosExceptHide.indexOf(itemInfo);
			mItemInfosExceptHide.remove(index);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
		if (needRefreash) {
			Message msg = mAllAppHandler.obtainMessage();
			msg.what = AllBussinessHandler.MSG_REMOVEITEM;
			msg.obj = itemInfo;
			msg.arg1 = index;
			mAllAppHandler.sendMessage(msg);
		}
	}
	
	public void removeFunItemInfos(ArrayList<? extends FunItemInfo> funItemInfos, boolean needRefreash) {
		try {
			for (FunItemInfo funItemInfo : funItemInfos) {
				if (funItemInfo instanceof FunFolderItemInfo) {
					mAllFunItemInfoMap.remove(funItemInfo.getIntent());
				}
			}
			mItemInfosExceptHide.removeAll(funItemInfos);
			mAllAppBussiness.removeFunItemInfos(funItemInfos);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
		if (needRefreash) {
			Message msg = mAllAppHandler.obtainMessage();
			msg.what = AllBussinessHandler.MSG_REMOVEITEMS;
			msg.obj = funItemInfos;
			mAllAppHandler.sendMessage(msg);
		}
	}
	
	public void moveInfoWhenExtrusion(int resIdx, int tarIdx) {
		FunItemInfo itemInfo = mItemInfosExceptHide.remove(resIdx);
		mItemInfosExceptHide.add(tarIdx, itemInfo);
	}
	
	public void moveFunItemInfo(FunItemInfo itemInfo, int tarIdx) {
		try {
			mAllAppBussiness.moveFunItemInfo(itemInfo, tarIdx);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
	}
	
	public void moveFunItemInfo(int resIdx, int tarIdx) {
		try {
			mAllAppBussiness.moveFunItemInfo(resIdx, tarIdx);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
	}
	
	public void sortByLetterAndSave(String order) {
		try {
			mAllAppBussiness.sortByLetterAndSave(order);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
	}
	
	public void sortByTimeAndSave(String order) {
		try {
			mAllAppBussiness.sortByTimeAndSave(order);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
	}
	
	public void sortByFrequencyAndSave(String order) {
		try {
			mAllAppBussiness.sortByFrequencyAndSave(order);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
	}
	
	/**
	 * 缓存类，用于记录安装卸载程序信息
	 * 
	 * @author yangguanxiang
	 * 
	 */
	public class CacheItemInfo {
		public AppItemInfo itemInfo;
		public boolean isInstall; // True为安装, False为卸载

		public CacheItemInfo(AppItemInfo info, boolean isInstall) {
			this.itemInfo = info;
			this.isInstall = isInstall;
		}
	}
	
	/**
	 * 将安装卸载数据加入缓存列表
	 * 
	 * @param addList
	 * @param isInstall
	 */
	public void addInCacheList(ArrayList<AppItemInfo> addList,	boolean isInstall) {
		if (null == addList) {
			return;
		}
		if (mCachedApps == null) {
			mCachedApps = new ArrayList<CacheItemInfo>();
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
	 * 处理缓存中的安装卸载数据(其实这玩意应该通用到其他grid，如文件夹，最近打开)
	 */
	public synchronized void handleCachedAppsList() {
		if (null == mCachedApps) {
			return;
		}
		synchronized (mCachedApps) {
			for (CacheItemInfo cacheInfo : mCachedApps) {
				if (cacheInfo.isInstall) {
					// 我有个很严重的问题，全新安装的应用会在文件夹里面？
					// 先从内存中的文件夹里搜索，如果找不到才添加到文件夹外部
//					boolean find = findAndSetAppItemInFolder(cacheInfo.itemInfo);
//					if (!find) {
						// 安装程序
					FunAppItemInfo addItemInfo = null;
						try {
							if (cacheInfo.itemInfo != null) {
								// 构造插入对象
								addItemInfo = new FunAppItemInfo(cacheInfo.itemInfo);
								addItemInfo.setIsNew(true);
								addItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(addItemInfo.getIntent()));
								mAllFunItemInfoMap.put(addItemInfo.getIntent(), addItemInfo);
								mAllAppBussiness.addFunItemInfoAndSort(addItemInfo);
							}
						} catch (DatabaseException e) {
							AppFuncExceptionHandler.handle(e);
						}
						// 通知尼马，上级通知你的，让他去通知刷新UI，你的上级毛都不用干么?
//						// 通知安装了程序
						broadCast(INSTALL_APP, 0, addItemInfo, null);
//					}
				} else {
					// 卸载程序
					
					// 应用有可能在文件夹里面，这里查找删除，后续完成
					
//					final ArrayList<Long> folderIds = getFolderIds();
					FunItemInfo delItemInfo = null;
					try {
						if (cacheInfo.itemInfo != null) {
							delItemInfo = mAllAppBussiness.getFunItemInfo(cacheInfo.itemInfo.mIntent);
							mAllAppBussiness.removeFunItemInfo(delItemInfo);
							mAllFunItemInfoMap.remove(delItemInfo.getIntent());
							// 从隐藏名单中去除
							mAppConfigControler.delHideAppItem(delItemInfo.getIntent(), false);
						}						
					} catch (Exception e) {
						AppFuncExceptionHandler.handle(e);
					}
					
					// 通知尼马，上级通知你的，让他去通知刷新UI，你的上级毛都不用干么?
//					// 通知卸载了程序
					broadCast(UNINSTALL_APP, 0, delItemInfo, null);
				}
			}

			// 清除缓存数据
			mCachedApps.clear();

			mCachedApps = null;
		}
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
	 * 是否存在隐藏应用
	 * @return
	 */
	public boolean isExistHideApps() {
		return mAppConfigControler.isExistHideApp();
	}
	
	/**
	 * 获取正在运行程序数据
	 */
	public ArrayList<FunAppItemInfo> getProManageFunAppItems() {
		ArrayList<FunTaskItemInfo> proManageItems = mTaskMgrControler.getProgresses();
		if (mProManageAppItems == null) {
			mProManageAppItems  = new ArrayList<FunAppItemInfo>();
		} else {
			mProManageAppItems.clear();
		}
		for (int i = 0; i < proManageItems.size(); i++) {
			FunTaskItemInfo taskItemInfo = proManageItems.get(i);
			FunAppItemInfo itemInfo = (FunAppItemInfo) mAllFunItemInfoMap.get(taskItemInfo.getAppItemInfo().mIntent);
			if (itemInfo != null) {
				itemInfo.setPid(taskItemInfo.getPid());
				itemInfo.setIsIgnore(taskItemInfo.isInWhiteList());
				mProManageAppItems.add(itemInfo);
			}
		}
		return getFilteredList(mProManageAppItems);
	}
	
	/**
	 * 过滤正在运行中的白名单程序
	 * @param originArrayList
	 * @return
	 */
	private ArrayList<FunAppItemInfo> getFilteredList(ArrayList<FunAppItemInfo> originArrayList) {
		if (null == originArrayList) {
			return null;
		}
		Iterator<FunAppItemInfo> appIterator = originArrayList.iterator();
		// 设置不显示忽略列表内的程序
		if (FunAppSetting.NEGLECTAPPS == GOLauncherApp.getSettingControler().getFunAppSetting()
				.getShowNeglectApp()) {
			while (appIterator.hasNext()) {
				FunAppItemInfo aInfo = appIterator.next();
				if (aInfo.isIgnore()) {
					appIterator.remove();
				}
			}
		}
		return originArrayList;
	}
	
	/**
	 * 清除所有正在运行程序（白名单程序除外）
	 */
	public void terminateAllProManageTask() {
		if (mProManageAppItems != null) {
			mTaskMgrControler.terminateAllProManageTask(mProManageAppItems);
			mProManageObserver.handleChanges(MessageID.ALL_TASKMANAGE, null, null);
		}
	}
	
	/**
	 * 获取功能表所有文件夹信息
	 * @return
	 */
	public List<FunFolderItemInfo> getFunFolders() {
		return mAllAppBussiness.getFunFolders();
	}
	
	/**
	 * 打开程序详细信息
	 * @param intent
	 */
	public void skipAppInfobyIntent(Intent intent) {
		mTaskMgrControler.skipAppInfobyIntent(intent);
	}
	
	/**
	 * 添加程序至白名单中
	 * @param intent
	 */
	public void addIgnoreAppItem(Intent intent) {
		mTaskMgrControler.addIgnoreAppItem(intent);
		notifyLockListChange();
	}
	
	/**
	 * 在白名单中删除程序
	 * @param intent
	 */
	public void delIgnoreAppItem(Intent intent) {
		mTaskMgrControler.delIgnoreAppItem(intent);
		notifyLockListChange();
	}
	
	/**
	 * 结束进程
	 * @param pid 进程唯一的标识
	 */
	public void terminateApp(int pid) {
		mTaskMgrControler.terminateApp(pid);
		mProManageObserver.handleChanges(MessageID.SINGLE_TASKMANAGE, null, null);
	}
	
	/**
	 * 通知锁定程序列表发生变化
	 */
	public void notifyLockListChange() {
		if (mProManageObserver != null) {
			mProManageObserver.handleChanges(MessageID.LOCK_LIST_CHANGED, null, null);
		}
	}
}
