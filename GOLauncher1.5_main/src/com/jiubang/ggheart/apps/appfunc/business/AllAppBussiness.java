package com.jiubang.ggheart.apps.appfunc.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.go.util.ConvertUtils;
import com.go.util.SortUtils;
import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler.CacheItemInfo;
import com.jiubang.ggheart.apps.appfunc.controler.IndexFinder;
import com.jiubang.ggheart.apps.appfunc.data.AllAppDataModel;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.config.utils.ConfigUtils;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncExceptionHandler;
import com.jiubang.ggheart.bussiness.BaseBussiness;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.FunConverter;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItem;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.tables.AppTable;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.ThreadName;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;

/**
 * 
 * <br>类描述: 所有程序模块业务逻辑处理器
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-12-27]
 */
public class AllAppBussiness extends BaseBussiness {

	private LinkedList<FunItemInfo> mAllAppItemInfos;

	private AllAppDataModel mDataModel;

	private static Object sAllAppLock = new Object();
	
	private static Object sSaveLock = new Object();

	private Handler mHandler;
	
	public AllAppBussiness(Context context, Handler handler) {
		super(context);
		mDataModel = new AllAppDataModel(context);
		mAllAppItemInfos = new LinkedList<FunItemInfo>();
		// 初始化Handler
		setHandler(handler);
	}

	private void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	/**
	 * 异步线程排序并且保存数据到数据库
	 */
	public void startSaveThread() {
		// 若第一次加载或表中没数据(可能之前保存失败)，则存入数据库
		if (mDataModel.isNewDB() || 0 == mDataModel.getSizeOfApps()) {
			new Thread(ThreadName.FUNC_INIT_SORT) {
				@Override
				public void run() {
					synchronized (sSaveLock) {
						// 通知开始保存
						Message message = mHandler.obtainMessage();
						message.what = AllBussinessHandler.MSG_STARTSAVE;
						mHandler.sendMessage(message);

						startSort();

						// 保存整个数据到数据库
						try {
							addFunItems(mAllAppItemInfos.size(), mAllAppItemInfos);
						} catch (DatabaseException e) {
							AppFuncExceptionHandler.handle(e);
						}

						// 通知保存完毕
						message = mHandler.obtainMessage();
						message.what = AllBussinessHandler.MSG_FINISHSAVE;
						mHandler.sendMessage(message);
						
						// 通知处理缓存数据
						handleCacheData();
					}
				}
			}.start();
		}
	}
	
	/**
	 * 异步加载功能表数据
	 * @param mAllFunItemInfoMap 
	 */
	public void startInitThread(final ArrayList<AppDrawerControler.CacheItemInfo> cacheApps, final ConcurrentHashMap<Intent, FunItemInfo> allFunItemInfoMap) {
		// 先异步加载图标和名称
//		synchronized (sSaveLock) {
			mAppDataEngine.startLoadCompletedData();
//		}
		Thread thread = new Thread(ThreadName.FUNC_INIT_DATA) {
			@Override
			public void run() {
				// 设置当前线程优先级为android系统提供的后台线程优先级，避免与主线程抢占资源
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				// 检查是否要更新
				// boolean toUpdate = mStateParaphrase.checkUpdate();
				// 通知开始加载数据
				Message startmessage = mHandler.obtainMessage();
				startmessage.what = AllBussinessHandler.MSG_STARTLOADINGAPP;
				mHandler.sendMessage(startmessage);
				boolean isFirstCreate;
				// 初始化功能表数据
				synchronized (sAllAppLock) {
					isFirstCreate = initAllFunItemInfos(cacheApps, allFunItemInfoMap, false);
				}
				
				Message message = mHandler.obtainMessage();
				message.what = AllBussinessHandler.MSG_FINISHINIT;
				message.obj = isFirstCreate;
				mHandler.sendMessage(message);
				// 通知已初加载完图标和title且未发送过加载完毕消息，可发送消息执行保存线程了
				// 主动询问AppDataEngine程序名称是否已经加载完毕，因为此部分数据在AppDataEngine
				// 中是静态的，在快速退出再进入时，此部分数据可能还未释放
			}
		};
		thread.start();
	}
	
	/**
	 * 按照设置进行排序
	 */
	private void startSort() {
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
		Message message = mHandler.obtainMessage();
		message.what = AllBussinessHandler.MSG_FINISHSORT;
		mHandler.sendMessage(message);
	}
	
	/**
	 * <br>
	 * 功能简述:重新开始异步加载一遍所有程序数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public final void reloadFunAppItems(final ArrayList<AppDrawerControler.CacheItemInfo> cacheApps, ConcurrentHashMap<Intent, FunItemInfo> allFunItemInfoMap) {
		startInitThread(cacheApps, allFunItemInfoMap);
	}

	private boolean initAllFunItemInfos(ArrayList<AppDrawerControler.CacheItemInfo> cacheApps, ConcurrentHashMap<Intent, FunItemInfo> allFunItemInfoMap, boolean updateOldData) {
		// 从数据库获取到数据列表
		ArrayList<FunItem> infos = getFunItems();
		// 从mAppDataEngine中取出来的手机上安装的程序数据
		ArrayList<AppItemInfo> memAppItemInfos = mAppDataEngine.getAllAppItemInfos();
		// 要添加的，手机里的新数据
		ArrayList<AppItemInfo> toAddItemInfos = (ArrayList<AppItemInfo>) memAppItemInfos
				.clone();
		// 清除旧数据
		mAllAppItemInfos.clear();
		boolean isCreateDB = false;

		if (null != infos && infos.size() > 0) { // 数据库有数据
			// 取出数据库的数据
			int index = 0;
			for (FunItem funItem : infos) {
				FunItemInfo itemInfo = createFunItemInfo(funItem, updateOldData, toAddItemInfos, allFunItemInfoMap);
				if (itemInfo != null) {
					itemInfo.setIndex(index);
					// 加入一个判断逻辑，看数据库索引和内存索引是否一致，若不一致，则说明数据乱了，需要同步数据库的索引,文件夹也需要同样处理
					if (funItem.mIndex != index) {
						funItem.mIndex = index;
						try {
							updateFunItemIndex(funItem.mIntent, index);
						} catch (DatabaseException e) {
							AppFuncExceptionHandler.handle(e);
						}
					}
					index++;
					// 把信息加入功能表列表里
					mAllAppItemInfos.add(itemInfo);
					allFunItemInfoMap.put(itemInfo.getIntent(), itemInfo);
					// 加载到达一定数量通知grid刷新
					if ((index + 1) % 32 == 0) {
						handleAllAppGridRefresh();
					}
				}
				// 暂时无视应用中心逻辑,效率过分的低
			}
			// 新安装的应用程序之后再统一处理
			if (toAddItemInfos != null) {
				for (int i = 0; i < toAddItemInfos.size();) {
					AppItemInfo info = toAddItemInfos.get(i);
					if (isNewInstall(cacheApps, info)) {
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

			// 将手机的新数据写到数据库和当前内存列表中(只添加文件夹里没有的)
			try {
				addFunItems(mAllAppItemInfos, toAddItemInfos);
			} catch (DatabaseException e) {
				AppFuncExceptionHandler.handle(e);
			}

			// Add by wangzhuobin, 2012.07.11
			if (found) {
				// 先更新内存集合位置
				try {
					buildSpecialItemsOrder();
				} catch (DatabaseException e) {
					AppFuncExceptionHandler.handle(e);
				}
			}

			infos = null;
			
			handleCacheData();
			// 置空通讯统计缓存map,新安装应用不会有对应的未读数信息
//			mDataModel.setNotificationMap(null);
		} else { // 数据库没有数据
			isCreateDB = true;
			ArrayList<AppItemInfo> appItemInfos = null;
			appItemInfos = mAppDataEngine.getAllAppItemInfos();

			AppItemInfo appItemInfo = null;
			int size = appItemInfos.size();
			for (int i = 0; i < size; ++i) {
				appItemInfo = appItemInfos.get(i);
				if (null == appItemInfo) {
					continue;
				}
				FunAppItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
				funItemInfo.setIndex(i);
				funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
				mAllAppItemInfos.add(funItemInfo);
				allFunItemInfoMap.put(funItemInfo.getIntent(), funItemInfo);
//				mDataModel.setAppUnread(funItemInfo);
				// 每加载x个通知1次
				if ((i + 1) % 32 == 0) {
					// 通知分批添加了
					handleAllAppGridRefresh();
				}
			}
		}
		// 通知分批添加了
		handleAllAppGridRefresh();
		return isCreateDB;
		// LogUnit.i("FunControler", "initAllAppItemInfos() -- end");
	}

	@Override
	public synchronized void cleanup() {
		super.cleanup();
	}

	/**
	 * 根据intent获取到特定的元素
	 * 注意：该方法要遍历整个列表，不是特别快
	 * @param intent
	 * @return FunAppItemInfo
	 */
	public FunItemInfo getFunItemInfo(Intent intent) {
		FunItemInfo info = null;
		Iterator<FunItemInfo> iter = mAllAppItemInfos.iterator();
		while (iter.hasNext()) {
			info = iter.next();
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
	 * 获得所有应用数据的列表（包括隐藏程序）
	 * @return
	 */
	public final LinkedList<FunItemInfo> getFunItemInfos() {
		return mAllAppItemInfos;
	}
	
	/**
	 * 从数据库中读取一个功能表数据
	 * @param intent
	 * @return
	 */
	private FunItem getFunItem(Intent intent) {
		Cursor cursor = mDataModel.getFunItem(intent);
		FunItem info = null;
		if (null != cursor) {
			try {
				final int index = cursor.getColumnIndex(AppTable.INDEX);
				final int intentIdx = cursor.getColumnIndex(AppTable.INTENT);
				final int folderIdIdx = cursor.getColumnIndex(AppTable.FOLDERID);
				final int folderTitleIdx = cursor.getColumnIndex(AppTable.TITLE);
				if (cursor.moveToFirst()) {
					info = new FunItem();
					info.mIndex = cursor.getInt(index);
					String str = cursor.getString(intentIdx);
					info.mIntent = ConvertUtils.stringToIntent(str);
					info.mFolderId = cursor.getLong(folderIdIdx);
					info.mTitle = cursor.getString(folderTitleIdx);
				}
			} finally {
				cursor.close();
			}
		}
		return info;
	}
	
	/**
	 * 从数据库中读取功能表数据(只给初始化时候用)
	 * @return
	 */
	private ArrayList<FunItem> getFunItems() {
		// 从数据库获取到数据列表
		Cursor cursor = mDataModel.getFunItems();
		ArrayList<FunItem> infos = new ArrayList<FunItem>();
		// classnotfound 先换个方法处理
		FunConverter.convertToFunItemsFromAppTable(cursor, infos);
		return infos;
	}

	/**
	 * 添加元素到功能表(同时加到内存和数据库)
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notDuplicate 排除重复标志
	 * @return
	 * @throws DatabaseException
	 */
	public boolean addFunItemInfo(int location, FunItemInfo funItemInfo, final boolean notDuplicate) throws DatabaseException {
		// 若不重复添加, 则先判断是否已存在
		if (notDuplicate) {
			if (null == funItemInfo || null == funItemInfo.getIntent()) {
				return false;
			}
			if (mAllAppItemInfos.contains(funItemInfo)) {
				return false;
			}
			// 查看数据库中是否已存在
			if (mDataModel.getFunItemIndex(funItemInfo.getIntent()) >= 0) {
				return false;
			}
			
		}
		// 添加元素到列表，并维护内存index
		if (location < 0) {
			location = 0;
		} else if (location > mAllAppItemInfos.size()) {
			location = mAllAppItemInfos.size();
		}
		mAllAppItemInfos.add(location, funItemInfo);
		funItemInfo.setIndex(location);
		refreshIndexWhenAdd(location, 1);
		// 添加到数据库
		mDataModel.addFunItem(funItemInfo);
		return true;
	}

	/**
	 * 批量添加元素到功能表(同时加到内存和数据库)
	 * 
	 * @param index
	 * @param funItemInfo
	 * @return
	 * @throws DatabaseException
	 */
	public void addFunItemInfos(int location, List<FunItemInfo> funItemInfos) throws DatabaseException {
		if (location < 0) {
			location = 0;
		} else if (location > mAllAppItemInfos.size()) {
			location = mAllAppItemInfos.size();
		}
		mAllAppItemInfos.addAll(location, funItemInfos);
		int index = location;
		for (FunItemInfo funItemInfo : funItemInfos) {
			if (!mAllAppItemInfos.contains(funItemInfo)) {
				mAllAppItemInfos.add(index, funItemInfo);
				funItemInfo.setIndex(index);
				// 添加到数据库
				mDataModel.addFunItem(funItemInfo);
				index++;
			}
		}
		refreshIndexWhenAdd(location, funItemInfos.size());
	}

	/**
	 * 添加某个程序至内存和数据库列表（自动根据当前排序设置插入位置，维护Index）
	 * 
	 * @param appItemInfo
	 * @throws DatabaseException
	 */
	public synchronized boolean addFunItemInfoAndSort(FunItemInfo funItemInfo)
			throws DatabaseException {
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
			countIndex = IndexFinder
					.findFirstItemInApps(mAllAppItemInfos, true);
		} else if (FunAppSetting.SORTTYPE_FREQUENCY == sortType) {
			countIndex = IndexFinder.findFirstIndex(mContext, mAllAppItemInfos,
					true, IndexFinder.COMPARE_WITH_FREQUENCE,
					funItemInfo.getClickedCount(mContext), "DESC");
		} else {
			countIndex = IndexFinder.findLastItemInApps(mAllAppItemInfos, true);
		}

		if (countIndex < 0) {
			countIndex = mAllAppItemInfos.size();
		}

		return addFunItemInfo(countIndex, funItemInfo, true);
	}
	
	/**
	 * 添加元素到功能表
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notDuplicate 排除重复标志
	 * @return
	 * @throws DatabaseException
	 */
	private FunItemInfo addFunItem(FunItemInfo funItemInfo, final boolean notDuplicate) throws DatabaseException {
		// 若不重复添加, 则先判断是否已存在
		if (notDuplicate) {
			if (null == funItemInfo || null == funItemInfo.getIntent()) {
				return null;
			}
			// 查看数据库中是否已存在
			// TODO：update index
			if (mDataModel.getFunItemIndex(funItemInfo.getIntent()) >= 0) {
				// TODO:日志
				return null;
			}
		}
		// 添加到数据库
		mDataModel.addFunItem(funItemInfo);
		return funItemInfo;
	}
	
	/**
	 * 批量添加元素到功能表数据库（这个也是有点废渣,暂时只用于初始化时候）
	 * 因为要同时维护内存index
	 * @param funItemInfos
	 * @throws DatabaseException
	 */
	private synchronized void addFunItems(int location, final List<FunItemInfo> funItemInfos)
			throws DatabaseException {
		if (funItemInfos == null || funItemInfos.size() <= 0) {
			return;
		}
		mDataModel.beginTransaction();
		try {
			// 添加数据到数据库
			for (FunItemInfo funItemInfo : funItemInfos) {
				if (null == funItemInfo) {
					continue;
				}
				addFunItem(funItemInfo, true);
			}
			mDataModel.setTransactionSuccessful();
		} finally {
			mDataModel.endTransaction();
		}
	}
	
	/**
	 * 把appiteminfo的数据写进数据库和funItemInfos列表中
	 * @param funItemInfos 功能表内存元素列表
	 * @param appItemInfos 新增加的元素列表
	 * @throws DatabaseException 
	 */
	private void addFunItems(List<FunItemInfo> funItemInfos, ArrayList<AppItemInfo> appItemInfos) throws DatabaseException {
		mDataModel.beginTransaction();
		try {
			for (AppItemInfo appItemInfo : appItemInfos) {
				if (null == appItemInfo) {
					continue;
				}
				FunItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
				funItemInfo.setIndex(funItemInfos.size());
				funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
				
				addFunItemInfo(funItemInfos.size(), funItemInfo, true);
			}
			mDataModel.setTransactionSuccessful();
		}  finally {
			mDataModel.endTransaction();
		}
	}
	
	/**
	 * 更新整个元素列表的index到数据库
	 * @param funItemInfos
	 * @throws DatabaseException
	 */
	private void updateFunItemsIndex(List<FunItemInfo> funItemInfos)
			throws DatabaseException {
		// 更新排序到数据库
		Intent intent = null;
		AppItemInfo appItemInfo = null;
		mDataModel.beginTransaction();
		try {
			for (FunItemInfo funItemInfo : funItemInfos) {
				if (null == funItemInfo) {
					continue;
				}

				// 获取唯一标识Intent
				if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
					intent = funItemInfo.getIntent();
				} else {
					appItemInfo = ((FunAppItemInfo) funItemInfo).getAppItemInfo();
					if (null == appItemInfo) {
						continue;
					}
					intent = appItemInfo.mIntent;
				}

				if (null == intent) {
					continue;
				}
				// 更新元素的index
				updateFunItemIndex(intent, funItemInfo.getIndex());
			}
			mDataModel.setTransactionSuccessful();
		} finally {
			mDataModel.endTransaction();
		}
	}
	
	/**
	 * 更新元素的index
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @param index
	 *            对于的index
	 * @throws DatabaseException
	 */
	private void updateFunItemIndex(final Intent intent, final int index) throws DatabaseException {
		if (null == intent) {
			return;
		}
		ContentValues values = new ContentValues();
		values.put(AppTable.INDEX, index);
		mDataModel.updateFunItem(intent, values);
	}

	/**
	 * 删除某一位置的元素，并维护index
	 * @param index
	 * @return
	 * @throws DatabaseException
	 */
	public FunItemInfo removeFunItemInfo(int index) throws DatabaseException {
		FunItemInfo funItemInfo = null;
		if (0 <= index && index <= mAllAppItemInfos.size()) {
			// 从内存中删除
			funItemInfo = mAllAppItemInfos.remove(index);
			refreshIndexWhenRemove(index);
			// 从数据库中删除
			removeFunItem(funItemInfo.getIntent());
		}
		return funItemInfo;
	}

	/**
	 * 删除某一指定的元素，并维护index
	 * @param funItemInfo
	 * @throws DatabaseException 
	 */
	public void removeFunItemInfo(FunItemInfo funItemInfo) throws DatabaseException {
		if (funItemInfo != null) {
			int index = funItemInfo.getIndex();
			// 从内存中删除
			mAllAppItemInfos.remove(funItemInfo);
			refreshIndexWhenRemove(index);
			// 从数据库中删除
			removeFunItem(funItemInfo.getIntent());
		}
	}
	
	/**
	 * 批量删除列表的元素，并维护index
	 * @param funItemInfos
	 * @throws DatabaseException
	 */
	public void removeFunItemInfos(List<? extends FunItemInfo> funItemInfos) throws DatabaseException {
		try {
			mDataModel.beginTransaction();
			for (FunItemInfo funItemInfo : funItemInfos) {
				removeFunItemInfo(funItemInfo);
			}
			mDataModel.setTransactionSuccessful();
		} catch (Exception e) {
			AppFuncExceptionHandler.handle(e);
		} finally {
			mDataModel.endTransaction();
		}
		
	}
	
	/**
	 * 根据intent删除数据库中数据,并维护数据库中的mIndex
	 * @param intent
	 * @throws DatabaseException
	 */
	private void removeFunItem(Intent intent) throws DatabaseException {
		if (intent ==  null) {
			return;
		}
		mDataModel.removeFunItem(intent);
	}
	
	/**
	 * 数据库中批量删除,并维护数据库中的mIndex(这个也是有点废渣)
	 * (因为要同时维护内存的index，而且暂时没有业务是只删除数据库，不删除内存)
	 * @param funItemInfos
	 * @throws DatabaseException
	 */
	private synchronized void removeFunItems(final List<FunItemInfo> funItemInfos)
			throws DatabaseException {
		if (null == funItemInfos) {
			return;
		}

		mDataModel.beginTransaction();
		try {
			for (FunItemInfo funItemInfo : funItemInfos) {
				if (null == funItemInfo) {
					continue;
				}
				removeFunItem(funItemInfo.getIntent());
			}
			mDataModel.setTransactionSuccessful();
		} finally {
			mDataModel.endTransaction();
		}
	}
	
	/**
	 * 增加元素时，维护后续内存元素index
	 * @param location
	 */
	private void refreshIndexWhenAdd(int location, int jump) {
		// 这里针对linklist做了遍历修改，这个index的维护需要，可根据性能情况,取舍修改
		// 更新mItemInAppIndex
//		FunItemInfo info = null;
		int count = location + jump;
		int index = 0;
		for (FunItemInfo info : mAllAppItemInfos) {
			if (index >= count) {
				info.setIndex(info.getIndex() + jump);
			}
			index++;
		}
//		for (int i = location + jump; i < mAllAppItemInfos.size(); ++i) {
//			info = mAllAppItemInfos.get(i);
//			info.setIndex(info.getIndex() + jump);
//		}
	}

	/**
	 * 移除元素时，维护后续内存元素index
	 * @param index
	 */
	private void refreshIndexWhenRemove(int location) {
		// 更新mItemInAppIndex
//		FunItemInfo info = null;
		int count = location;
		int index = 0;
		for (FunItemInfo info : mAllAppItemInfos) {
			if (index >= count) {
				info.setIndex(info.getIndex() - 1);
			}
			index++;
		}
//		for (int i = index; i < mAllAppItemInfos.size(); ++i) {
//			info = mAllAppItemInfos.get(i);
//			if (info != null) {
//				info.setIndex(info.getIndex() - 1);
//			}
//		}
	}
	
	/**
	 * 移动元素时，维护移动范围内存元素index
	 * @param index
	 */
	private void refreshIndexWhenMove(int resIdx, int tarIdx) {
		if (resIdx == tarIdx) {
			return;
		}
		int min = 0;
		int max = 0;
		int jump = 0;
		// 后面的元素往前移动
		if (resIdx > tarIdx) {
			min = tarIdx;
			max = resIdx;
			jump = 1;
		}
		// 前面的元素往后移动
		if (resIdx < tarIdx) {
			min = resIdx;
			max = tarIdx;
			jump = -1;
		}
		FunItemInfo info = null;
		for (int i = min; i < max; ++i) {
			info = mAllAppItemInfos.get(i);
			if (info != null) {
				info.setIndex(info.getIndex() + jump);
			}
		}
	}

	/**
	 * 清除原来的排序(只对内存数据操作)
	 */
	private void refreshIndex() {
		int i = 0;
		for (FunItemInfo funItemInfo : mAllAppItemInfos) {
			if (null == funItemInfo) {
				continue;
			}
			funItemInfo.setIndex(i);
			i++;
		}
	}

	/**
	 * 图标交换数据库操作
	 * @param resIndex
	 * @param tarIndex
	 * @throws DatabaseException
	 */
	private void moveFunItem(int resIndex, int tarIndex) throws DatabaseException {
		mDataModel.moveFunItem(resIndex, tarIndex);
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
	public boolean moveFunItemInfo(int resIdx, int tarIdx) throws DatabaseException {
		if (resIdx == tarIdx) {
			return true;
		}
		boolean success = true;
		FunItemInfo itemInfo = mAllAppItemInfos.remove(resIdx);
		if (0 <= tarIdx && tarIdx <= mAllAppItemInfos.size()) {
			mAllAppItemInfos.add(tarIdx, itemInfo);
			itemInfo.setIndex(tarIdx);
		}
		refreshIndexWhenMove(resIdx, tarIdx);
		moveFunItem(resIdx, tarIdx);
		return success;
	}
	
	/**
	 * 更新某个元素到某位置
	 * @param itemInfo 需要更新的元素
	 * @param tarIdx 将要移动到的目标位置
	 * @throws DatabaseException 
	 */
	public void moveFunItemInfo(FunItemInfo itemInfo, int tarIdx) throws DatabaseException {
		int resIdx = mAllAppItemInfos.indexOf(itemInfo);
		if (resIdx != -1 || resIdx != tarIdx) {
			mAllAppItemInfos.remove(resIdx);
			if (0 <= tarIdx && tarIdx <= mAllAppItemInfos.size()) {
				mAllAppItemInfos.add(tarIdx, itemInfo);
				itemInfo.setIndex(tarIdx);
			}
			refreshIndexWhenMove(resIdx, tarIdx);
			moveFunItem(resIdx, tarIdx);
		}
	}

	/**
	 * 把数据库的元素对象转换成功能表元素对象
	 * @param funItem
	 * @param updateOldData 是否删除旧数据
	 * @param toAddItemInfos 新从系统拉出的应用数据，如果应用已存在功能表，就从该列表中移除
	 * @param allFunItemInfoMap 
	 * @return 只有当updateOldData为true时，返回值有可能是null，并且不是文件夹元素对象
	 */
	private FunItemInfo createFunItemInfo(FunItem funItem, boolean updateOldData, ArrayList<AppItemInfo> toAddItemInfos, ConcurrentHashMap<Intent, FunItemInfo> allFunItemInfoMap) {
		FunItemInfo itemInfo = null;
		if (funItem != null) {
			boolean isFolder = funItem.mFolderId != 0;
			if (isFolder) {
				FunFolderItemInfo folderItemInfo = new FunFolderItemInfo(funItem);
				GLAppFolderInfo folderInfo = new GLAppFolderInfo(folderItemInfo);
				GLAppFolderController.getInstance().addFolderInfo(folderInfo);
				ArrayList<FunAppItemInfo> funAppItemInfos = folderItemInfo.getFolderContent();
				for (FunAppItemInfo funAppItemInfo : funAppItemInfos) {
					AppItemInfo info = funAppItemInfo.getAppItemInfo();
					allFunItemInfoMap.put(info.mIntent, funAppItemInfo);
					toAddItemInfos.remove(info);
				}
				return folderItemInfo;
			} else {
				AppItemInfo appItemInfo = mAppDataEngine.getAppItem(funItem.mIntent);
				// 拿不到表示该应用被删掉，桌面没接到这个消息
				if (appItemInfo == null) {
					if (updateOldData) {
						// 如果删除数据库中的旧数据
						// 从数据库中删除
						try {
							removeFunItem(funItem.mIntent);
						} catch (DatabaseException e) {
							AppFuncExceptionHandler.handle(e);
						} 
						return null;
					} else {
						appItemInfo = new AppItemInfo();
						appItemInfo.mIntent = funItem.mIntent;
						appItemInfo.mIcon = mAppDataEngine.getSysBitmapDrawable();
						appItemInfo.setIsTemp(true);
					}
				}
				
				// 次奥，这个能不能改成通知形式？每个应用都要去进行一次intent比对，要花不少时间吧...
				// 根据渠道配置信息，把应用游戏中心的两个假图标移除
				// 这种场景主要发生在用户从有两个中心的假图标的渠道包升级到没有两个中心的渠道包
				// add by wangzhuobin 2012.07.20
				if (ConfigUtils
						.isNeedCheckAppGameInFunItemByChannelConfig()) {
					// 只有不需要在功能表添加应用中心或者游戏中心图标的时候，我们才做这样的检查和删除操作
					if (ConfigUtils
							.isNeedRemoveAppGameFromFunByChannelConfig(appItemInfo)) {
						// 从数据库和内存中移除应用游戏中心的图标信息
						// 从数据库中删除
						try {
							removeFunItem(funItem.mIntent);
						} catch (DatabaseException e) {
							AppFuncExceptionHandler.handle(e);
						}
						return null;
					}
				}
				
				// 不是手机里的新数据，从暂存的列表中移除
				if (toAddItemInfos != null) {
					toAddItemInfos.remove(appItemInfo);
				}
				// 若title没load好,使用保存起来的名称
				if (null == appItemInfo.mTitle) {
					appItemInfo.mTitle = funItem.mTitle;
					if (null == funItem.mTitle) {
						// TODO:
						appItemInfo.mTitle = "Loading...";
					}
				}
				
				itemInfo = new FunAppItemInfo(appItemInfo);
				itemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(itemInfo.getIntent()));
			}
		}
		return itemInfo;
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
			message.what = AllBussinessHandler.MSG_SORTFAILED;
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
			message.what = AllBussinessHandler.MSG_SORTFAILED;
			mHandler.sendMessage(message);
		}
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
			message.what = AllBussinessHandler.MSG_SORTFAILED;
			mHandler.sendMessage(message);
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
		if (mAllAppItemInfos.isEmpty()) {
			return;
		}

		// LogUnit.i("FunControler", "sortByLetterAndSave()");
		// 排序
		sortByLetter(order);
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		updateFunItemsIndex(mAllAppItemInfos);
		// 文件夹中排序
		// sortFoldersByLetterAndSave(order);
		// 通知
		Message message = mHandler.obtainMessage();
		message.what = AllBussinessHandler.MSG_FINISHSORT;
		mHandler.sendMessage(message);
	}
	
	/**
	 * 按时间排序，并保存到数据库
	 * 
	 * @param order
	 * @throws DatabaseException
	 */
	public synchronized void sortByTimeAndSave(String order)
			throws DatabaseException {
		if (mAllAppItemInfos.isEmpty()) {
			return;
		}

		// LogUnit.i("FunControler", "sortByTimeAndSave()");
		// 排序
		sortByTime(order);
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		updateFunItemsIndex(mAllAppItemInfos);
		// 文件夹中排序
		// sortFoldersByTimeAndSave(order);
		// 通知
		Message message = mHandler.obtainMessage();
		message.what = AllBussinessHandler.MSG_FINISHSORT;
		mHandler.sendMessage(message);
	}
	
	/**
	 * 按使用频率排序，并保存到数据库
	 * 
	 * @param order
	 * @throws DatabaseException
	 */
	public synchronized void sortByFrequencyAndSave(String order)
			throws DatabaseException {
		if (mAllAppItemInfos.isEmpty()) {
			return;
		}
		// 排序
		sortByFrequency(order);
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		updateFunItemsIndex(mAllAppItemInfos);
		// 文件夹中排序
		// sortFoldersByFrequencyAndSave(order);
		// 通知
		Message message = mHandler.obtainMessage();
		message.what = AllBussinessHandler.MSG_FINISHSORT;
		mHandler.sendMessage(message);
	}
	
	/**
	 * 设置特殊图标的顺序
	 * @throws DatabaseException 
	 */
	private void buildSpecialItemsOrder() throws DatabaseException {
		int index = 0;
		for (FunItemInfo info : mAllAppItemInfos) {
			if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDLIST.equals(info
					.getIntent().getAction())) {
				moveFunItemInfo(index, 0);
			} else if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDGAME.equals(info
					.getIntent().getAction())) {
				moveFunItemInfo(index, 0);
			} else if (ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(info
					.getIntent().getAction())) {
				moveFunItemInfo(index, 0);
			} else if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(info
					.getIntent().getAction())) {
				moveFunItemInfo(index, 0);
			}
			index++;
		}
	}
	
	
	
	/**
	 * 保存到数据库(重新刷写一次DB数据)
	 * 刷写量很大，慎用！
	 * @throws DatabaseException
	 */
//	private void refreshDB() {
//		try {
//			mDataModel.beginTransaction();
//			// 清空数据
//			mDataModel.clearFunAppItems();
//
//			// 添加数据到数据库
//			mDataModel.addFunAppItemInfos(mAllAppItemInfos);
//			mDataModel.setTransactionSuccessful();
//		} catch (DatabaseException e) {
//			e.printStackTrace();
//		} finally {
//			mDataModel.endTransaction();
//		}
//	}
	
	/**
	 * 通知处理缓存数据
	 */
	private void handleCacheData() {
		// 处理缓存池中的安装卸载数据及sd卡
		Message message = mHandler.obtainMessage();
		message = mHandler.obtainMessage();
		message.what = AllBussinessHandler.MSG_CACHEDAPPS;
		mHandler.sendMessage(message);

		message = mHandler.obtainMessage();
		message.what = AllBussinessHandler.MSG_SDCARDAPPS;
		mHandler.sendMessage(message);
	}
	
	/**
	 * 通知刷新grid数据
	 */
	private void handleAllAppGridRefresh() {
		Message message = mHandler.obtainMessage();
		message.what = AllBussinessHandler.MSG_BATADD;
		mHandler.sendMessage(message);
		try {
			sAllAppLock.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Object getSaveLock() {
		return sSaveLock;
	}
	
	public static Object getAllAppLock() {
		return sAllAppLock;
	}

	/**
	 * 是否为新安装的应用程序.用于构建内部列表时判断(缓存机制后续完成)
	 * 缓存机制：用于还没进入功能表就监听到安装事件
	 * @param info
	 * @return
	 */
	private boolean isNewInstall(ArrayList<AppDrawerControler.CacheItemInfo> cacheApps, AppItemInfo info) {
		if (info != null && cacheApps != null && cacheApps.size() > 0) {
			synchronized (cacheApps) {
				for (CacheItemInfo cacheItem : cacheApps) {
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
	 * 获取功能表所有文件夹信息
	 * @return
	 */
	public LinkedList<FunFolderItemInfo> getFunFolders() {
		if (mAllAppItemInfos == null) {
			return null;
		} else {
			final LinkedList<FunItemInfo> appItems = (LinkedList<FunItemInfo>) mAllAppItemInfos
					.clone();
			LinkedList<FunFolderItemInfo> folderList = new LinkedList<FunFolderItemInfo>();
			for (FunItemInfo info : appItems) {
				if (info != null && info.getType() == FunItemInfo.TYPE_FOLDER) {
					folderList.add((FunFolderItemInfo) info);
				}
			}
			return folderList;
		}
	}
	
}
