package com.jiubang.ggheart.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.go.util.file.media.FileEngine;
import com.go.util.file.media.MediaDataProviderConstants;
import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.FunConverter;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItem;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.tables.AppTable;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.data.tables.MediaManagementHideTable;
import com.jiubang.ggheart.data.tables.MediaManagementPlayListFileTable;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.plugin.notification.NotificationControler;
import com.jiubang.ggheart.plugin.notification.NotificationType;

/**
 * 
 * @author 
 * @version 
 * 
 */
public class FunDataModel extends DataModel {

	// 统一程序数据管理
	private AppDataEngine mAppDataEngine;

	private HashMap<Integer, Integer> mNotificationMap;

	public FunDataModel(Context context, AppDataEngine appDataEngine) {
		super(context);
		mAppDataEngine = appDataEngine;
	}

	/**
	 * 获取AppDataEngine统一的数据TODO:与dock的获取程序列表统一
	 * 
	 * @return 所有程序数据
	 */
	public final ArrayList<AppItemInfo> getAllAppItemInfos() {
		return mAppDataEngine.getAllAppItemInfos();
	}

	public ArrayList<FunItem> getFunItems() {
		// 从数据库获取到数据列表
		Cursor cursor = mDataProvider.getFunAppItems();
		ArrayList<FunItem> infos = new ArrayList<FunItem>();
		try {
			FunConverter.convertToFunItemsFromAppTable(cursor, infos);
		} catch (Exception e) {
			// TODO:
			e.printStackTrace();
		}

		return infos;
	}

	/**
	 * 在funItemInfos中添加folderIds各文件夹中没有的程序到内存，并添加到数据库
	 * 
	 * @param funItemInfos
	 *            列表
	 * @param folderIds
	 *            文件夹id列表
	 * @param funFolderItemInfos
	 *            缓存的文件夹列表
	 * @param toAddItemInfos
	 *            要添加的元素
	 * @param notDuplicate
	 *            不重复添加
	 */
	public void addInList(final ArrayList<FunItemInfo> funItemInfos, ArrayList<Long> folderIds,
			ArrayList<FunFolderItemInfo> funFolderItemInfos,
			final ArrayList<AppItemInfo> toAddItemInfos, final boolean notDuplicate) {
		if (null == funItemInfos || null == folderIds || null == toAddItemInfos) {
			return;
		}

		// 将手机的新数据写到数据库(只添加文件夹里没有的)
		AppItemInfo appItemInfo = null;
		FunAppItemInfo funItemInfo = null;
		int size = toAddItemInfos.size();
		if (size == 0) {
			return;
		}

		// 取得文件夹中的程序元素缓存数组
		HashMap<String, FunAppItemInfo> funAppItemInfos = null;
		if (null != funFolderItemInfos) {
			funAppItemInfos = getHashCacheFolderAppItems(funFolderItemInfos);
		}

		mDataProvider.beginTransaction();
		try {
			for (int i = 0; i < size; ++i) {
				appItemInfo = toAddItemInfos.get(i);
				if (null == appItemInfo) {
					continue;
				}

				// 若不重复添加
				if (notDuplicate) {
					// 若在功能表根目录已存在, 则不添加
					int idx = getAppItemIndex(appItemInfo.mIntent);
					if (idx >= 0) {
						continue;
					}
				}

				if (null != funAppItemInfos) {
					if (isInHashFolderAppItems(funAppItemInfos, appItemInfo.mIntent)) {
						continue;
					}
				} else {
					if (isInDBFolders(folderIds, appItemInfo.mIntent)) {
						continue;
					}
				}

				funItemInfo = new FunAppItemInfo(appItemInfo);
				funItemInfo.setIndex(funItemInfos.size());
				funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
				setAppUnread(funItemInfo);

				// 存入内存
				funItemInfos.add(funItemInfo);
				// 写入数据库
				addFunAppItemInfo(funItemInfo);
			}
			mDataProvider.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDataProvider.endTransaction();
		}

		appItemInfo = null;
	}

	/**
	 * 从数据库获取文件id列表
	 * 
	 * @return 文件夹id列表
	 */
	public final ArrayList<Long> getFolderIds() {
		ArrayList<Long> folderIds = new ArrayList<Long>();
		Cursor cursor = mDataProvider.getFolderIds();
		FunConverter.convertToFolderIdsFromAppTable(cursor, folderIds);
		return folderIds;
	}

	/**
	 * 初始化文件夹缓存数组
	 */
	private final ArrayList<FunAppItemInfo> getCacheFolderAppItems(
			final ArrayList<FunFolderItemInfo> funFolderItemInfos) {
		ArrayList<FunAppItemInfo> funAppItemInfos = new ArrayList<FunAppItemInfo>();
		FunFolderItemInfo funFolderItemInfo = null;
		int size = funFolderItemInfos.size();
		for (int i = 0; i < size; ++i) {
			funFolderItemInfo = funFolderItemInfos.get(i);
			if (null == funFolderItemInfo) {
				continue;
			}

			// 将文件夹中的元素放到缓存数组
			funAppItemInfos.addAll(funFolderItemInfo.getFunAppItemInfos());
		}
		return funAppItemInfos;
	}

	/**
	 * 初始化文件夹缓存数组
	 */
	private final HashMap<String, FunAppItemInfo> getHashCacheFolderAppItems(
			final ArrayList<FunFolderItemInfo> funFolderItemInfos) {
		HashMap<String, FunAppItemInfo> funAppItemInfos = new HashMap<String, FunAppItemInfo>();
		FunFolderItemInfo funFolderItemInfo = null;
		ArrayList<FunAppItemInfo> appsInFolder = null;
		int size = funFolderItemInfos.size();
		for (int i = 0; i < size; ++i) {
			funFolderItemInfo = funFolderItemInfos.get(i);
			if (null == funFolderItemInfo) {
				continue;
			}

			// 将文件夹中的元素放到缓存数组
			appsInFolder = funFolderItemInfo.getFunAppItemInfos();
			if (null == appsInFolder) {
				continue;
			}

			for (FunAppItemInfo funAppItemInfo : appsInFolder) {
				if (null == funAppItemInfo) {
					continue;
				}
				funAppItemInfos.put(ConvertUtils.intentToString(funAppItemInfo.getIntent()),
						funAppItemInfo);
			}
		}
		return funAppItemInfos;
	}

	private boolean isInHashFolderAppItems(final HashMap<String, FunAppItemInfo> funAppItemInfos,
			final Intent intent) {
		return funAppItemInfos.containsKey(ConvertUtils.intentToString(intent));
	}

	/**
	 * 是否在文件夹缓存数组中
	 * 
	 * @param funAppItemInfos
	 * @param intent
	 * @return
	 */
	private boolean isInMemFolderAppItems(final ArrayList<FunAppItemInfo> funAppItemInfos,
			final Intent intent) {
		FunAppItemInfo funAppItemInfo = null;
		for (int i = 0; i < funAppItemInfos.size(); ++i) {
			funAppItemInfo = funAppItemInfos.get(i);
			if (null == funAppItemInfo) {
				continue;
			}

			// 若存在文件夹中
			if (ConvertUtils.intentCompare(intent, funAppItemInfo.getIntent())) {
				// 则从缓存数组中删除，以提高效率
				funAppItemInfos.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否存在于文件夹,根据内存的文件夹查找
	 * 
	 * @param funFolderItemInfos
	 * @param intent
	 * @return
	 */
	private boolean isInMemFolders(final ArrayList<FunFolderItemInfo> funFolderItemInfos,
			final Intent intent) {
		FunFolderItemInfo funFolderItemInfo = null;
		int size = funFolderItemInfos.size();
		for (int i = 0; i < size; ++i) {
			funFolderItemInfo = funFolderItemInfos.get(i);
			if (null == funFolderItemInfo) {
				continue;
			}

			// 若存在文件夹中
			if (funFolderItemInfo.getFunAppItem(intent) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否存在于文件夹,根据数据库的文件夹id查找
	 * 
	 * @param appItemInfo
	 * @return
	 */
	public boolean isInDBFolders(final ArrayList<Long> folderIds, final Intent intent) {
		int size = folderIds.size();
		for (int i = 0; i < size; ++i) {
			if (mDataProvider.isInFolders(folderIds.get(i), intent)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 取得公用数据bean
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @return 数据bean
	 */
	public AppItemInfo getAppItem(final Intent intent) {
		if (null == intent) {
			return null;
		}
		return mAppDataEngine.getAppItem(intent);
	}

	/**
	 * 获取文件夹中的元素
	 * 
	 * @param folderId
	 *            文件夹id
	 * @return 元素数组
	 */
	public ArrayList<FunAppItemInfo> getAppsInFolder(final long folderId, boolean updateOldData,
			FunItemInfo itemInfo) {
		Cursor cursor = mDataProvider.getAppsInFolder(folderId);
		ArrayList<FunItem> infos = new ArrayList<FunItem>();
		FunConverter.convertToFunItemsFromFolderTable(cursor, infos);

		AppItemInfo appItemInfo = null;
		FunAppItemInfo funItemInfo = null;
		ArrayList<FunAppItemInfo> appsInFolder = new ArrayList<FunAppItemInfo>();
		FunItem funItem = null;

		int unreadCount = 0;

		for (int i = 0; i < infos.size(); ++i) {
			funItem = infos.get(i);
			if (null == funItem) {
				continue;
			}

			// 不处理文件夹中的文件夹
			if (0 != funItem.mFolderId) {
				continue;
			}

			// 加入一个判断逻辑，看数据库索引和内存索引是否一致，若不一致，则说明数据乱了，需要同步数据库的索引
			// 同步内存和数据的索引（在某些情况下会有索引不一致）
			if (funItem.mIndex != i) {
				funItem.mIndex = i;
				try {
					updateFunAppItemIndexInFolder(folderId, funItem.mIntent, i);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}

			// 从AppDataEngine获得统一数据
			appItemInfo = mAppDataEngine.getAppItem(funItem.mIntent);
			// 若不存在
			if (null == appItemInfo) {
				if (updateOldData) {
					// TODO:处理sd卡
					// 从内存中删除
					removeFunItem(infos, i);
					// 从数据库中删除
					try {
						removeFunAppFromFolder(folderId, funItem.mIntent);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					continue;
				} else {
					// 如果不删除数据库中的数据, 显示默认的数据
					appItemInfo = new AppItemInfo();
					appItemInfo.mIntent = funItem.mIntent;
					appItemInfo.mIcon = /*
										 * (BitmapDrawable)
										 * mContext.getApplicationContext().
										 * getResources
										 * ().getDrawable(android.R.drawable
										 * .sym_def_app_icon)
										 */mAppDataEngine.getSysBitmapDrawable();
					appItemInfo.setIsTemp(true);
				}
			}

			// 若title没load好,使用保存起来的名称
			if (null == appItemInfo.mTitle) {
				appItemInfo.mTitle = funItem.mTitle;
				if (null == funItem.mTitle) {
					appItemInfo.mTitle = "Loading...";
				}
			}

			funItemInfo = new FunAppItemInfo(appItemInfo);
			funItemInfo.setTimeInFolder(funItem.mTimeInFolder);
			funItemInfo.setIndex(funItem.mIndex);
			funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));

			if (setAppUnread(funItemInfo)) {
				unreadCount += funItemInfo.getUnreadCount();
			}

			appsInFolder.add(funItemInfo);
		}
		if (null != itemInfo) {
			// 重新统计文件夹未读数
			itemInfo.setUnreadCount(unreadCount);
		}
		return appsInFolder;
	}

	/**
	 * 从列表中删除一个元素，并维护mIndex
	 */
	public void removeFunItem(ArrayList<FunItem> funItems, int index) {
		if (null == funItems) {
			return;
		}
		// 移除
		FunItem funItem = funItems.remove(index);
		// 更新mIndex
		int removeIdx = funItem.mIndex;
		int size = funItems.size();
		for (int i = 0; i < size; ++i) {
			funItem = funItems.get(i);
			if (null == funItem) {
				continue;
			}

			if (funItem.mIndex > removeIdx) {
				funItem.mIndex -= 1;
			}
		}
	}

//	/**
//	 * 在文件夹中删除一个程序
//	 * 
//	 * @param folderId
//	 *            文件夹id
//	 * @param index
//	 *            索引
//	 */
//	public void removeFunAppFromFolder(long folderId, final int index) {
//		// TODO:
//	}

	/**
	 * 在文件夹中删除一个程序
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param intent
	 *            唯一标识的Intent
	 * @throws DatabaseException
	 */
	public void removeFunAppFromFolder(long folderId, final Intent intent) throws DatabaseException {
		mDataProvider.removeFunAppFromFolder(folderId, intent);
	}

	/**
	 * 文件夹改名
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param folderName
	 *            文件夹名称
	 * @throws DatabaseException
	 */
	public void updateFunAppItem(final long folderId, final String folderName)
			throws DatabaseException {
		ContentValues values = new ContentValues();
		values.put(AppTable.TITLE, folderName);

		mDataProvider.updateFunAppItem(folderId, values);
	}

	/**
	 * 元素改名
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @param title
	 *            title
	 * @throws DatabaseException
	 */
	public void updateFunAppItem(final Intent intent, final String title) throws DatabaseException {
		if (null == intent || null == title) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(AppTable.TITLE, title);

		mDataProvider.updateFunAppItem(intent, values);
	}

	/**
	 * 批量删除
	 * 
	 * @param funItemInfos
	 * @throws DatabaseException
	 */
	public synchronized void removeFunAppItemInfosInDB(final ArrayList<FunAppItemInfo> funItemInfos)
			throws DatabaseException {
		if (null == funItemInfos) {
			return;
		}

		// int idx = -1;
		FunItemInfo funItemInfo = null;
		int size = funItemInfos.size();

		mDataProvider.beginTransaction();
		try {
			for (int i = 0; i < size; ++i) {
				funItemInfo = funItemInfos.get(i);
				if (null == funItemInfo) {
					continue;
				}

				// idx = getAppItemIndex(funItemInfo.getIntent());
				// if (idx < 0) {
				// continue;
				// }
				//
				// // 从数据库删除
				// removeFunappItemInfo(idx);
				mDataProvider.removeFunappItemInfoByIntent(funItemInfo.getIntent());
			}
			mDataProvider.setTransactionSuccessful();
		} finally {
			mDataProvider.endTransaction();
		}
	}

	/**
	 * 在数据库中批量添加
	 * 
	 * @param startIndex
	 * @param funItemInfos
	 */
	public void addFunAppItemsInDB(final int startIndex,
			final ArrayList<FunAppItemInfo> funItemInfos) {
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

		mDataProvider.beginTransaction();
		try {
			for (int i = 0; i < size; ++i) {
				funItemInfo = funItemInfos.get(i);
				if (null == funItemInfo) {
					continue;
				}

				// 从startIndex开始添加
				addItem = addFunAppItemInfoInDB(startIndex + add, funItemInfo, true);
				if (null != addItem) {
					++add;
				}
			}
			mDataProvider.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDataProvider.endTransaction();
		}
	}

	/**
	 * 批量元素添加到数据库文件夹表
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notify
	 * @throws DatabaseException
	 */
	public synchronized void addFunAppItemInfosInDB(final long folderId, int startIndex,
			ArrayList<FunAppItemInfo> funItemInfos) throws DatabaseException {
		if (null == funItemInfos) {
			return;
		}

		int add = 0;
		FunAppItemInfo addItem = null;
		FunAppItemInfo funAppItemInfo = null;
		int size = funItemInfos.size();

		mDataProvider.beginTransaction();
		try {
			for (int i = 0; i < size; ++i) {
				funAppItemInfo = funItemInfos.get(i);
				if (null == funAppItemInfo) {
					continue;
				}

				// 添加到数据库
				addItem = addFunAppItemInfoInDB(folderId, startIndex + add, funAppItemInfo);
				if (null != addItem) {
					++add;
				}
			}
			mDataProvider.setTransactionSuccessful();
		} finally {
			mDataProvider.endTransaction();
		}
	}

	/**
	 * 添加元素到文件夹表, 用于批量添加
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notify
	 * @throws DatabaseException
	 */
	private synchronized FunAppItemInfo addFunAppItemInfoInDB(final long folderId, int index,
			FunAppItemInfo funItemInfo) throws DatabaseException {
		if (null == funItemInfo) {
			return null;
		}
		// 不支持添加文件夹到文件夹
		if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
			return null;
		}

		if (null == funItemInfo.getAppItemInfo()) {
			return null;
		}

		// 文件夹中元素个数TODO:根据内存中的获取
		int size = getSizeOfFolder(folderId);
		// 越界
		if (index < 0 || index > size) {
			return null;
		}

		// 添加到数据，并更新index
		AppItemInfo appItemInfo = funItemInfo.getAppItemInfo();
		String title = appItemInfo.mTitle;
		if (null == title) {
			// TODO:默认的title
			title = "AppName";
		}
		addFunAppToFolder(folderId, index, appItemInfo.mIntent, title);

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
	 * 添加元素
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notDuplicate
	 * @return
	 * @throws DatabaseException
	 */
	private FunItemInfo addFunAppItemInfoInDB(int index, FunItemInfo funItemInfo,
			final boolean notDuplicate) throws DatabaseException {
		// 若不重复添加, 则先判断是否已存在
		if (notDuplicate) {
			if (!checkValid(funItemInfo)) {
				return null;
			}

			// 查看数据库中是否已存在
			// TODO：update index
			if (getAppItemIndex(funItemInfo.getIntent()) >= 0) {
				// TODO:日志
				return null;
			}
		}

		// 添加到数据库
		addFunAppItemInfo(funItemInfo);

		return funItemInfo;
	}

	/**
	 * 更新文件夹中的下标
	 * 
	 * @param folderId
	 * @param funAppItemInfos
	 */
	public void updateFunAppItemsIndexInFolder(final long folderId,
			final ArrayList<FunAppItemInfo> funAppItemInfos) {
		// 更新排序到数据库
		Intent intent = null;
		AppItemInfo appItemInfo = null;
		FunAppItemInfo funAppItemInfo = null;
		int size = funAppItemInfos.size();
		mDataProvider.beginTransaction();
		try {
			for (int i = 0; i < size; ++i) {
				funAppItemInfo = funAppItemInfos.get(i);
				if (null == funAppItemInfo) {
					continue;
				}

				// 获取唯一标识Intent
				if (FunItemInfo.TYPE_FOLDER == funAppItemInfo.getType()) {
					continue;
				}

				appItemInfo = funAppItemInfo.getAppItemInfo();
				if (null == appItemInfo) {
					continue;
				}
				intent = appItemInfo.mIntent;

				if (null == intent) {
					continue;
				}

				// 更新元素的index
				updateFunAppItemIndexInFolder(folderId, intent, funAppItemInfo.getIndex());
			}
			mDataProvider.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			mDataProvider.endTransaction();
		}
	}

	public void updateFunAppItemsIndex(ArrayList<FunItemInfo> funAppItemInfos)
			throws DatabaseException {
		// 更新排序到数据库
		Intent intent = null;
		AppItemInfo appItemInfo = null;
		FunItemInfo funItemInfo = null;
		int size = funAppItemInfos.size();
		mDataProvider.beginTransaction();
		try {
			for (int i = 0; i < size; ++i) {
				funItemInfo = funAppItemInfos.get(i);
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
				updateFunAppItem(intent, funItemInfo.getIndex());
			}
			mDataProvider.setTransactionSuccessful();
		} finally {
			mDataProvider.endTransaction();
		}
	}

	private void updateFunAppItemIndexInFolder(final long folderId, final Intent intent,
			final int index) throws DatabaseException {
		if (null == intent) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(FolderTable.INDEX, index);

		mDataProvider.updateFunAppItemInFolder(folderId, intent, values);
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
	public void updateFunAppItem(final Intent intent, final int index) throws DatabaseException {
		if (null == intent) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(AppTable.INDEX, index);

		mDataProvider.updateFunAppItem(intent, values);
	}

	/**
	 * 根据索引信息删除对应item
	 * 
	 * @param index
	 * @throws DatabaseException
	 */
	// public void removeFunappItemInfo(final int index) throws
	// DatabaseException {
	// mDataProvider.removeFunappItemInfo(index);
	// }

	/**
	 * 根据唯一标识的Intent删除元素
	 * 
	 * @param intent
	 *            Intent
	 * @throws DatabaseException
	 */
	public void removeFunappItemInfo(final Intent intent) throws DatabaseException {
		mDataProvider.removeFunappItemInfo(intent);
	}

	/**
	 * 在第一次加载数据时维护一次索引，使数据库索引和内存索引保持一致，因此，不会再有索引不一致导致删除数据的情况发生，
	 * 跟据intent删除数据库中数据的方法可以舍弃
	 * 
	 * @throws DatabaseException
	 * @edit by huangshaotao
	 * @date 2012-3-5
	 */
	// public void removeFunappItemInfoByIntent(final Intent intent) throws
	// DatabaseException {
	// mDataProvider.removeFunappItemInfoByIntent(intent);
	// }

	/**
	 * 移动appItem的位置
	 * 
	 * @author huyong
	 * @param itemId
	 * @param positionIndex
	 */
	public boolean moveAppItem(final int resIndex, final int tarIndex) {
		return mDataProvider.moveAppItem(resIndex, tarIndex);
	}

	/**
	 * 移动文件夹中的元素
	 * 
	 * @param folderId
	 * @param resIndex
	 * @param tarIndex
	 */
	public boolean moveFolderItem(final long folderId, final int resIndex, final int tarIndex) {
		return mDataProvider.moveFolderItem(folderId, resIndex, tarIndex);
	}

	/**
	 * 取得文件夹中第index个元素
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param index
	 *            下标
	 * @return 功能表元素
	 */
	public FunAppItemInfo getFunAppItemInFolder(final long folderId, final int index) {
		AppItemInfo appItemInfo = getAppItemInFolder(folderId, index);
		if (null == appItemInfo) {
			return null;
		}
		FunAppItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
		funItemInfo.setIndex(index);
		funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
		return funItemInfo;
	}

	/**
	 * 根据唯一标识Intent取得文件夹中的元素
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param intent
	 *            唯一标识Intent
	 * @return
	 */
	public FunItemInfo getFunAppItemInFolder(final long folderId, final Intent intent) {
		int idx = getAppItemIndexInFolder(folderId, intent);
		if (idx < 0) {
			return null;
		}

		AppItemInfo appItemInfo = getAppItemInFolder(folderId, idx);
		if (null == appItemInfo) {
			return null;
		}

		FunItemInfo funItemInfo = new FunAppItemInfo(appItemInfo);
		funItemInfo.setIndex(idx);
		funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
		return funItemInfo;
	}

	/**
	 * 取得文件夹中第index个元素
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param index
	 *            下标
	 * @return 程序元素
	 */
	public AppItemInfo getAppItemInFolder(final long folderId, final int index) {
		Cursor cursor = mDataProvider.getAppInFolder(folderId, index);
		AppItemInfo appItemInfo = null;
		if (null != cursor) {
			try {
				final int intentIdx = cursor.getColumnIndex(FolderTable.INTENT);
				if (cursor.moveToFirst()) {
					String str = cursor.getString(intentIdx);
					Intent intent = ConvertUtils.stringToIntent(str);
					appItemInfo = mAppDataEngine.getAppItem(intent);
				}
			} finally {
				cursor.close();
			}
		}
		return appItemInfo;
	}

	/**
	 * 获取元素在功能表根目录中的索引
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @return 索引
	 */
	public int getAppItemIndex(final Intent intent) {
		Cursor cursor = mDataProvider.getFunAppItems(intent);
		int idx = -1;
		if (null != cursor) {
			try {
				final int idxIndex = cursor.getColumnIndex(AppTable.INDEX);
				if (cursor.moveToFirst()) {
					idx = cursor.getInt(idxIndex);
				}
			} finally {
				cursor.close();
			}
		}
		return idx;
	}

	/**
	 * 
	 * @param folderId
	 * @param intent
	 * @return
	 */
	public int getAppItemIndexInFolder(final long folderId, final Intent intent) {
		Cursor cursor = mDataProvider.getAppInFolder(folderId, intent);
		int idx = -1;
		if (null != cursor) {
			try {
				final int idxIndex = cursor.getColumnIndex(FolderTable.INDEX);
				if (cursor.moveToFirst()) {
					idx = cursor.getInt(idxIndex);
				}
			} finally {
				cursor.close();
			}
		}
		return idx;
	}

	/**
	 * 清除表中的所有数据
	 * 
	 * @param tableName
	 *            表名
	 * @throws DatabaseException
	 */
	public void clearFunAppItems() throws DatabaseException {
		mDataProvider.clearFunAppItems();
	}

	/**
	 * 清空文件夹中元素数据
	 * 
	 * @param folderId
	 * @throws DatabaseException
	 */
	public void clearFolderAppItems(final long folderId) throws DatabaseException {
		mDataProvider.clearFolderAppItems(folderId);
	}

	/**
	 * 批量添加数据
	 * 
	 * @author huyong
	 * @param funAppItemInfos
	 * @throws DatabaseException
	 */
	public void addFunAppItemInfos(final ArrayList<FunItemInfo> funAppItemInfos)
			throws DatabaseException {
		if (funAppItemInfos == null || funAppItemInfos.size() <= 0) {
			return;
		}
		mDataProvider.beginTransaction();
		try {
			// 添加数据到数据库
			FunItemInfo funItemInfo = null;
			for (int i = 0; i < funAppItemInfos.size(); ++i) {
				funItemInfo = funAppItemInfos.get(i);
				if (null == funItemInfo) {
					continue;
				}
				addFunAppItemInfo(funItemInfo);
			}
			mDataProvider.setTransactionSuccessful();
		} finally {
			mDataProvider.endTransaction();
		}

	}

	/**
	 * 添加功能表元素
	 * 
	 * @param funItemInfo
	 * @throws DatabaseException
	 */
	public void addFunAppItemInfo(FunItemInfo funItemInfo) throws DatabaseException {
		if (null == funItemInfo) {
			return;
		}
		ContentValues contentValues = new ContentValues();
		contentValues.put(AppTable.INDEX, funItemInfo.getIndex());

		if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
			// TODO:统一intent.toUri(0)
			contentValues.put(AppTable.INTENT, funItemInfo.getIntent().toUri(0));
			contentValues.put(AppTable.FOLDERID, funItemInfo.getFolderId());
			contentValues.put(AppTable.TITLE, ((FunFolderItemInfo) funItemInfo).getTitle());
			contentValues.put(AppTable.FOLDERICONPATH, funItemInfo.getIconPath());
		} else {
			// 不是文件夹，使用真正的intent
			AppItemInfo appItemInfo = ((FunAppItemInfo) funItemInfo).getAppItemInfo();
			if (null == appItemInfo) {
				return;
			}
			// TODO:统一intent.toUri(0)
			Intent intent = appItemInfo.mIntent;
			if (null == intent) {
				return;
			}
			String title = appItemInfo.mTitle;
			contentValues.put(AppTable.INTENT, intent.toUri(0));
			contentValues.put(AppTable.TITLE, title);
		}
		mDataProvider.addAppItem(contentValues);
	}

	/**
	 * 在文件夹中添加一个程序
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param index
	 *            添加到的位置
	 * @param intent
	 *            程序唯一标识
	 * @throws DatabaseException
	 */
	public void addFunAppToFolder(final long folderId, final int index, final Intent intent,
			final String title) throws DatabaseException {
		mDataProvider.addFunAppToFolder(folderId, index, intent, title);
	}

	/**
	 * 获取文件夹中元素的个数
	 * 
	 * @param folderId
	 *            文件id
	 * @return 个数
	 */
	public int getSizeOfFolder(final long folderId) {
		return mDataProvider.getSizeOfFolder(folderId);
	}

	/**
	 * 获取功能表根目录元素个数
	 * 
	 * @return 个数
	 */
	public int getSizeOfApps() {
		return mDataProvider.getSizeOfApps();
	}

	/**
	 * 獲取所有數據庫中應用程序的intent，文件夾除外
	 * 
	 * @return
	 */
	public ArrayList<String> getAllAppsIntentStr() {
		Cursor cursor = mDataProvider.getFunAppItems();

		if (null == cursor) {
			return null;
		}
		ArrayList<String> strs = new ArrayList<String>();
		try {
			String intentStr = null;
			final int intentIdx = cursor.getColumnIndex(AppTable.INTENT);
			if (cursor.moveToFirst()) {
				do {
					intentStr = cursor.getString(intentIdx);
					strs.add(intentStr);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}

		return strs;
	}

	public void beginTransaction() {
		mDataProvider.beginTransaction();
	}

	public void setTransactionSuccessful() {
		mDataProvider.setTransactionSuccessful();
	}

	public void endTransaction() {
		mDataProvider.endTransaction();
	}

	/**
	 * 用于初始化时，把保存于mNotificationMap中的信息添加到info里面
	 * 
	 * @param itemInfo
	 */
	public boolean setAppUnread(FunAppItemInfo itemInfo) {
		if (null != itemInfo) {
			int type = AppIdentifier.whichTypeOfNotification(mContext, itemInfo.getAppItemInfo());
			if (NotificationType.IS_NOT_NOTIFICSTION != type) {
				itemInfo.setNotificationType(type);
				Integer count = null;
				if (null != mNotificationMap) {
					count = mNotificationMap.get(type);
					if (null != count) {
						itemInfo.setUnreadCount(count);
						return true;
					} else {
						count = itemInfo.getAppItemInfo().getUnreadCount();
						itemInfo.setUnreadCount(count);
					}
				} else {
					NotificationControler controler = AppCore.getInstance()
							.getNotificationControler();
					if (null != controler) {
						count = controler.getNotification(type);
						if (count == 0) { // 更多应用的存在于appiteminfo
							count = itemInfo.getAppItemInfo().getUnreadCount();
						}
						itemInfo.setUnreadCount(count);
						return true;
					}
				}
			}
		}
		return false;
	}

	public HashMap<Integer, Integer> getNotificationMap() {
		return mNotificationMap;
	}

	public void setNotificationMap(HashMap<Integer, Integer> mNotificationMap) {
		this.mNotificationMap = mNotificationMap;
	}

	/**
	 * 获取多媒体隐藏数据（图片、音乐、视频）
	 * @param type
	 * @return
	 */
	public HashMap<String, String> getAllHideMediaDatas(int type) {
		HashMap<String, String> datas = new HashMap<String, String>();
		String select = MediaManagementHideTable.TYPE + "=" + type + " ";
		Cursor cursor = mContext.getContentResolver().query(MediaDataProviderConstants.HideData.CONTENT_DATA_URI, null, select,
				null, null);
		
		if (cursor != null) {
			try {
				String uri = null;
				final int uriIdx = cursor.getColumnIndex(MediaManagementHideTable.URI);
				if (cursor.moveToFirst()) {
					do {
						uri = cursor.getString(uriIdx);
						datas.put(uri, uri);
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
		}
		return datas;
	}

	/**
	 * 获取播放列表中隐藏音乐数据
	 * @return
	 */
	public HashSet<Integer> getAllHidePlayListAudioDatas() {
		HashSet<Integer> datas = new HashSet<Integer>();
		HashMap<String, String> hidePlaylistId = getAllHideMediaDatas(FileEngine.TYPE_PLAYLIST);
		for (String playListId : hidePlaylistId.values()) {
			HashMap<String, Long> hideFiles = getPlayListFiles(Long.parseLong(playListId));
			for (Long fileId : hideFiles.values()) {
				datas.add(fileId.intValue());
			}
		}
		return datas;
	}
	
	/**
	 * 根据播放列表id获取其包含的音乐文件
	 * @param playListId
	 * @return
	 */
	public HashMap<String, Long> getPlayListFiles(long playListId) {
		HashMap<String, Long> datas = new HashMap<String, Long>();
		Cursor cursor = mContext.getContentResolver().query(MediaDataProviderConstants.PlayListFile.CONTENT_DATA_URI, null,
				MediaManagementPlayListFileTable.PLAYLISTID + "=" + playListId, null, null);
		if (cursor != null) {
			try {
				final int idx = cursor.getColumnIndex(MediaManagementPlayListFileTable.FILEID);
				if (cursor.moveToFirst()) {
					do {
						long id = cursor.getLong(idx);
						datas.put(String.valueOf(id), id);
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
		}
		return datas;
	}
}
