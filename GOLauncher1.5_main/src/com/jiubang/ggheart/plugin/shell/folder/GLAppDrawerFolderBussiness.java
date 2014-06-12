package com.jiubang.ggheart.plugin.shell.folder;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.data.FolderDataModel;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncExceptionHandler;
import com.jiubang.ggheart.bussiness.BaseBussiness;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.FunConverter;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItem;
import com.jiubang.ggheart.data.tables.AppTable;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-2-25]
 */
public class GLAppDrawerFolderBussiness extends BaseBussiness {

//	private FunDataModel mAppDrawerDataModel;
	
	private FolderDataModel mDataModel;

	private AppDrawerControler mDrawerControler;

	public GLAppDrawerFolderBussiness() {
		super(GOLauncherApp.getContext());
//		mAppDrawerDataModel = new FunDataModel(mContext, mAppDataEngine);
		mDataModel = new FolderDataModel(mContext);
		mDrawerControler = AppDrawerControler.getInstance(GOLauncherApp.getContext());
	}

	/**
	 * 在数据库中获取某个文件夹中的所有程序列表
	 * 
	 * @param folderId
	 * @return
	 */
	public final ArrayList<FunAppItemInfo> getFolderContentFromDB(FunFolderItemInfo folderItemInfo) {
		ArrayList<FunAppItemInfo> appItemInfos = getAppsInFolder(folderItemInfo.getFolderId(), false);
		return appItemInfos;
	}

	public void createFolderInAppDrawer(FunFolderItemInfo folderInfo, int folderStartIndex, int location) {
		try {
			creatFolderInDB(folderInfo, location, folderInfo.getFolderContent());
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
	}
	
	private void updateFunAppItemIndexInFolder(long folderId, Intent intent, int index) throws DatabaseException {
		if (null == intent) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(FolderTable.INDEX, index);

		mDataModel.updateFunAppItemInFolder(folderId, intent, values);
	}
	
	/**
	 * 获取文件夹中的元素
	 * 
	 * @param folderId
	 *            文件夹id
	 * @return 元素数组
	 */
	public ArrayList<FunAppItemInfo> getAppsInFolder(final long folderId, boolean updateOldData) {
		Cursor cursor = mDataModel.getAppsInFolder(folderId);
		ArrayList<FunItem> infos = new ArrayList<FunItem>();
		// 其实我个人不太喜欢这样转换，浪费循环
		FunConverter.convertToFunItemsFromFolderTable(cursor, infos);

		AppItemInfo appItemInfo = null;
		FunAppItemInfo funItemInfo = null;
		ArrayList<FunAppItemInfo> appsInFolder = new ArrayList<FunAppItemInfo>();

//		int unreadCount = 0;
		int index = 0;
		for (FunItem funItem : infos) {
			// 加入一个判断逻辑，看数据库索引和内存索引是否一致，若不一致，则说明数据乱了，需要同步数据库的索引
			// 同步内存和数据的索引（在某些情况下会有索引不一致）
			if (funItem.mIndex != index) {
				funItem.mIndex = index;
				try {
					updateFunAppItemIndexInFolder(folderId, funItem.mIntent, index);
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
					// 从数据库中删除
					try {
						removeFunAppFromFolder(folderId, funItem.mIntent);
					} catch (DatabaseException e) {
						
					}
				} else {
					// 如果不删除数据库中的数据, 显示默认的数据
					appItemInfo = new AppItemInfo();
					appItemInfo.mIntent = funItem.mIntent;
					appItemInfo.mIcon = mAppDataEngine.getSysBitmapDrawable();
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
			funItemInfo.setInWhitchFolder(folderId);
			funItemInfo.setIndex(funItem.mIndex);
			funItemInfo.setHideInfo(AppConfigControler.getInstance(mContext).getHideInfo(funItemInfo.getIntent()));
//			if (setAppUnread(funItemInfo)) {
//				unreadCount += funItemInfo.getUnreadCount();
//			}
			appsInFolder.add(funItemInfo);
		}
//		if (null != itemInfo) {
//			// 重新统计文件夹未读数
//			itemInfo.setUnreadCount(unreadCount);
//		}
		return appsInFolder;
	}

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
		mDataModel.removeFunAppFromFolder(folderId, intent);
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
	private void updateFunAppItem(final long folderId, final String folderName)
			throws DatabaseException {
		ContentValues values = new ContentValues();
		values.put(AppTable.TITLE, folderName);

		mDataModel.updateFunAppItem(folderId, values);
	}
	
	public void addFunAppToFolder(FunFolderItemInfo folderInfo, FunAppItemInfo appInfo) {
		try {
			addFunAppItemInfoInDB(folderInfo.getFolderId(), folderInfo.getFolderSize(), appInfo);
			mDrawerControler.removeFunItemInfo(appInfo, true);
		} catch (DatabaseException e) {
			AppFuncExceptionHandler.handle(e);
		}
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
//	public void addFunAppToFolder(final long folderId, final int index, final Intent intent,
//			final String title) throws DatabaseException {
//		mDataModel.addFunAppToFolder(folderId, index, intent, title);
//	}
	
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
		// 添加到数据，并更新index
		AppItemInfo appItemInfo = funItemInfo.getAppItemInfo();
		String title = appItemInfo.mTitle;
		if (null == title) {
			// TODO:默认的title
			title = "AppName";
		}
//		addFunAppToFolder(folderId, index, appItemInfo.mIntent, title);
		mDataModel.addFunAppToFolder(folderId, index, appItemInfo.mIntent, title);

		return funItemInfo;
	}
	
	/**
	 * 批量元素添加到数据库文件夹表
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notify
	 * @throws DatabaseException
	 */
	private synchronized void addFunAppItemInfosInDB(final long folderId, int startIndex,
			ArrayList<FunAppItemInfo> funItemInfos) throws DatabaseException {
		if (null == funItemInfos) {
			return;
		}

		int add = 0;
		FunAppItemInfo addItem = null;
		FunAppItemInfo funAppItemInfo = null;
		int size = funItemInfos.size();

		mDataModel.beginTransaction();
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
			mDataModel.setTransactionSuccessful();
		} finally {
			mDataModel.endTransaction();
		}
	}
	
	private void creatFolderInDB(FunFolderItemInfo folderInfo, int location,
			ArrayList<FunAppItemInfo> addInfos) throws DatabaseException {
		FunAppItemInfo addInfo = null;
		int size = addInfos.size();
		mDataModel.beginTransaction();
		try {
			mDrawerControler.addFunItemInfo(location, folderInfo, true);
			for (int i = 0; i < size; ++i) {
				addInfo = addInfos.get(i);
				if (null == addInfo) {
					continue;
				}
				mDrawerControler.removeFunItemInfo(addInfo, false);
				// 添加到文件夹数据库
				addFunAppItemInfoInDB(folderInfo.getFolderId(), i, addInfo);
			}
			mDataModel.setTransactionSuccessful();
		} finally {
			mDataModel.endTransaction();
		}
	}
}
