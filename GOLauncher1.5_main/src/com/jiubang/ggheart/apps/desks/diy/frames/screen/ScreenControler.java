package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;
import java.util.HashMap;

import android.appwidget.AppWidgetHost;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.FavoriteProvider;
import com.jiubang.ggheart.data.SysFolderControler;
import com.jiubang.ggheart.data.SysShortCutControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FavoriteInfo;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ScreenLiveFolderInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.model.ScreenDataModel;
import com.jiubang.ggheart.data.statistics.StaticScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 屏幕管理，负责屏幕操作与后台数据的管理
 * 
 * @author huyong
 * 
 */
public class ScreenControler {
	// 日志
	private static final String LOG_TAG = "ScreenControlLog";
	private ScreenDataModel mDataModel = null;

	private HashMap<Integer, ArrayList<ItemInfo>> mScreenHashMap = null;
	private FavoriteProvider mFavoriteProvider;

	private boolean mFirstInit = false;
	private Context mContext;

	public ScreenControler(final Context context, AppWidgetHost widgetHost) {
		mContext = context;
		mDataModel = new ScreenDataModel(context);
		mScreenHashMap = new HashMap<Integer, ArrayList<ItemInfo>>();
		mFirstInit = mDataModel.isNewDB();
		mFavoriteProvider = new FavoriteProvider(context, widgetHost);
	}

	private void initFavorites() {
		ArrayList<ItemInfo> favList = mFavoriteProvider.loadFavorite();
		final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
		ArrayList<AppItemInfo> dbItemInfos = dataEngine.getAllAppItemInfos();
		if (favList != null) {
			int count = favList.size();
			GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
			for (int i = 0; i < count; i++) {
				final ItemInfo info = favList.get(i);
				if (info instanceof FavoriteInfo) {
					final FavoriteInfo favoriteInfo = (FavoriteInfo) info;
					GoWidgetBaseInfo widgetBaseInfo = favoriteInfo.mWidgetInfo;
					final String pkgName = widgetManager.getWidgetPackage(widgetBaseInfo);
					if (widgetBaseInfo != null && pkgName != null) {
						boolean isInstalled = AppUtils.isAppExist(mContext, pkgName);
						if (pkgName.equals(LauncherEnv.GOSMS_PACKAGE)) {
							int versionCode = AppUtils.getVersionCodeByPkgName(mContext, pkgName);
							if (versionCode < 80) {
								isInstalled = false;
							}
						}
						if (isInstalled) {
							ScreenAppWidgetInfo appWidgetInfo = new ScreenAppWidgetInfo(
									widgetBaseInfo.mWidgetId, null, info);
							addDesktopItem(info.mScreenIndex, appWidgetInfo);
							// 添加信息到Gowidget表
							widgetManager.addGoWidget(widgetBaseInfo);
						}
						// 仅当有预览图和下载地址才添加到数据库中，方便用户点击
						// NOTE:注释这个条件：/* && favoriteInfo.mUrl !=
						// null*/，因为现在全跳gostore下载
						else if (favoriteInfo.mPreview > 0/*
															* && favoriteInfo.mUrl
															* != null
															*/) {
							addDesktopItem(info.mScreenIndex, info);
						}
					}
				} else if (info instanceof ScreenAppWidgetInfo) {
					addDesktopItem(info.mScreenIndex, info);
				} else if (info instanceof ShortCutInfo) {
					final ShortCutInfo shortCutInfo = (ShortCutInfo) info;
					final ComponentName cn = shortCutInfo.mIntent.getComponent();
					for (AppItemInfo appItemInfo : dbItemInfos) {
						final ComponentName appcn = appItemInfo.mIntent.getComponent();
						if (appcn.equals(cn)) {
							shortCutInfo.mInScreenId = System.currentTimeMillis();
							shortCutInfo.mIntent = appItemInfo.mIntent;
							shortCutInfo.mTitle = appItemInfo.mTitle;
							shortCutInfo.mIcon = appItemInfo.mIcon;
							addDesktopItem(shortCutInfo.mScreenIndex, info);
							break;
						}
					}
				} else if (info instanceof UserFolderInfo) {
					final UserFolderInfo folderInfo = (UserFolderInfo) info;
					addDesktopItem(folderInfo.mScreenIndex, folderInfo);
					int childCount = folderInfo.getChildCount();
					ArrayList<ItemInfo> existChild = new ArrayList<ItemInfo>();
					for (int j = 0; j < childCount; j++) {
						ItemInfo itemInfo = folderInfo.getChildInfo(j);
						if (itemInfo != null && itemInfo instanceof ShortCutInfo) {
							final ShortCutInfo shortCutInfo = (ShortCutInfo) itemInfo;
							final ComponentName cn = shortCutInfo.mIntent.getComponent();
							for (AppItemInfo appItemInfo : dbItemInfos) {
								ComponentName appcn = appItemInfo.mIntent.getComponent();
								if (appcn.equals(cn)) {
									shortCutInfo.mIntent = appItemInfo.mIntent;
									shortCutInfo.mTitle = appItemInfo.mTitle;
									shortCutInfo.mIcon = appItemInfo.mIcon;
									existChild.add(shortCutInfo);
									break;
								}
							}
						}
					}
					addUserFolderContent(folderInfo.mInScreenId, existChild, false);
				}
			}
		}
		mFavoriteProvider.clearFavorite();
	}

	FavoriteInfo getFavoriteInfo(FavoriteInfo info) {
		if (info != null && info.mWidgetInfo != null) {
			final FavoriteInfo cachedInfo = mFavoriteProvider
					.getFavoriteInfo(info.mWidgetInfo.mWidgetId);
			if (cachedInfo != null) {
				// 补齐从partsToScreen表中缺少的信息
				info.mPreview = cachedInfo.mPreview;
				info.mUrl = cachedInfo.mUrl;
				info.mWidgetInfo = cachedInfo.mWidgetInfo;
				info.mTitleId = cachedInfo.mTitleId;
			}
		}
		return info;
	}

	/**
	 * 是否存在初始化推荐widget，如果存在，则需要解析对应的xml文件
	 * 
	 * @return
	 */
	boolean isExistFavorite() {
		if (mScreenHashMap != null) {
			int size = mScreenHashMap.size();
			for (int i = 0; i < size; i++) {
				ArrayList<ItemInfo> list = mScreenHashMap.get(i);
				if (list != null) {
					int count = list.size();
					for (int j = 0; j < count; j++) {
						ItemInfo itemInfo = list.get(j);
						if (itemInfo != null && itemInfo.mItemType == IItemType.ITEM_TYPE_FAVORITE) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 加载屏幕 如果确认的、未确认的都为空 则重新获取
	 * 
	 * @return
	 */
	public synchronized HashMap<Integer, ArrayList<ItemInfo>> loadScreen() {
		if (mScreenHashMap.size() <= 0) {
			getScreenItems();
			// 如果存在初始化推荐widget，需要解析xml文件
			if (isExistFavorite()) {
				mFavoriteProvider.loadFavorite();
			}
		}
		if (mScreenHashMap.size() <= 0) {
			// 获取数据出错，恢复数据
			Log.i(LOG_TAG, "loadScreen screen info failed!!");

			// 恢复数据
			mDataModel.cleanDesktopItem();
			mDataModel.initDesktopItem();

			// 再次获取
			getScreenItems();
			// Gowidget初始化列表
			initFavorites();
		}
		//add by dengdazhong
		//resolve ADT-7595 低配置手机，新安装后恢复默认桌面时，桌面中的指示器会显示为5，等待一段时间才变成3
		//如果低配需要减屏,就减掉前面和最后面的两个屏
		if (StaticScreenSettingInfo.sNeedDelScreen) {
			delScreen(0);
			delScreen(mScreenHashMap.size() - 1);
			StaticScreenSettingInfo.sNeedDelScreen = false;
		}
		// 保存当前有多少屏，供挂掉的时候err report反馈
		int count = mScreenHashMap.size();
		saveScreenCount(count);

		return mScreenHashMap;
	}

	private void getScreenItems() {
		mDataModel.getScreenItems(mScreenHashMap);
	}

	public void updateFolderIndex(long folderID, ArrayList<ItemInfo> infos) {
		mDataModel.updateFolderIndex(folderID, infos);
	}

	public void updateFolderItem(long folderID, ItemInfo info) {
		mDataModel.updateFolderItem(folderID, info);
		mDataModel.prepareItemInfo(info);
	}

	/***
	 ****************************** 屏幕操作*******************************************
	 */

	/**
	 * 移动屏幕位置
	 * 
	 * @author huyong
	 * @param srcScreenIndex
	 *            屏幕源索引
	 * @param DescScreenIndex
	 *            屏幕目标索引
	 */
	synchronized boolean moveScreen(final int srcScreenIndex, final int descScreenIndex) {
		if (srcScreenIndex == descScreenIndex) {
			return true;
		}

		try {
			mDataModel.moveScreen(srcScreenIndex, descScreenIndex);

			// 修改缓存
			// 删除源，加入目标
			ArrayList<ItemInfo> data = delScreenToHashMap(srcScreenIndex);
			addScreenToHashMap(descScreenIndex, data);

			return true;
		} catch (Exception e) {
			Log.i(LOG_TAG, "keep data (move screen) exception");
			return false;
		}
	}

	/**
	 * 增加一个屏幕
	 * 
	 * @author huyong
	 * @param screenIndex
	 *            屏幕索引值
	 */
	synchronized boolean addScreen(int screenIndex) {

		int index = 0;
		if (mScreenHashMap != null) {
			index = screenIndex == -1 ? mScreenHashMap.size() : screenIndex;
		}
		/*
		 * if (screenExsit(screenIndex)) { Log.i(LOG_TAG,
		 * "add screen already exist"); return false; }
		 */
		try {
			// 保存数据
			mDataModel.addScreen(index);

			// 缓存
			addScreenToHashMap(index, null);

			return true;
		} catch (Exception e) {
			Log.i(LOG_TAG, "keep data (add screen) exception");
			return false;
		}
	}

	/**
	 * 删除指定屏幕
	 * 
	 * @author huyong
	 * @param delScreenIndex
	 *            屏幕索引值
	 */
	synchronized boolean delScreen(final int delScreenIndex) {
		// 不存在
		if (!screenExsit(delScreenIndex)) {
			Log.i(LOG_TAG, "delete screen not exsit");
			return false;
		}

		try {
			// 删除数据库中数据
			mDataModel.delScreen(delScreenIndex);

			// 删除内存中屏幕
			delScreenToHashMap(delScreenIndex);

			return true;
		} catch (Exception e) {
			Log.i(LOG_TAG, "keep data (delete screen) exception");
			return false;
		}
	}

	/**
	 * 返回屏幕个数
	 * 
	 * @author huyong
	 * @return
	 */
	public	int getScreenCount() {
		// 新数据库返回默认屏幕个数
		if (mFirstInit) {
			mFirstInit = false;
			//add by dengdazhong
			//resolve ADT-7595 低配置手机，新安装后恢复默认桌面时，桌面中的指示器会显示为5，等待一段时间才变成3
			//如果低配需要减屏,就减掉前面和最后面的两个屏
			//为什么不直接修改ScreenSettingInfo.DEFAULT_SCREEN_COUNT？因为其他地方会用到DEFAULT_SCREEN_COUNT
			if (StaticScreenSettingInfo.sNeedDelScreen) {
				return ScreenSettingInfo.DEFAULT_SCREEN_COUNT - 2;
			}
			return ScreenSettingInfo.DEFAULT_SCREEN_COUNT;
		}
		return mDataModel.getScreenCount();
	}

	private boolean screenExsit(int screenIndex) {
		if (mScreenHashMap != null) {
			int sz = mScreenHashMap.size();
			if (screenIndex >= 0 && screenIndex < sz) {
				return true;
			}
		}
		return false;
	}

	/***
	 ****************************** 桌面组件操作*******************************************
	 */

	/**
	 * 添加文件夹、快捷方式到桌面，parts表将新增一条记录
	 * 
	 * @author huyong
	 * @param screenIndex
	 *            //目标屏幕索引
	 * @param itemInfo
	 *            //添加的item项
	 */
	public synchronized boolean addDesktopItem(final int screenIndex, ItemInfo itemInfo) {
		if (null == itemInfo) {
			Log.i(LOG_TAG, "add null item");
			return false;
		}
		if (!screenExsit(screenIndex)) {
			Log.i(LOG_TAG, "add item to not exist screen");
			return false;
		}

		// 特殊处理
		// 快捷方式
		if (IItemType.ITEM_TYPE_SHORTCUT == itemInfo.mItemType) {
			final SysShortCutControler shortCutControler = AppCore.getInstance()
					.getSysShortCutControler();
			if (null != shortCutControler) {
				ShortCutInfo sInfo = (ShortCutInfo) itemInfo;
				// 添加保护，防止空指针的出现
				String itemNameString = sInfo.mTitle == null ? null : sInfo.mTitle.toString();
				BitmapDrawable itemIcon = (sInfo.mIcon != null && sInfo.mIcon instanceof BitmapDrawable)
						? (BitmapDrawable) sInfo.mIcon
						: null;

				boolean bRet = shortCutControler.addSysShortCut(sInfo.mIntent, itemNameString,
						itemIcon);
				if (!bRet) {
					Log.i(LOG_TAG, "add system shortcut is fail");
					return false;
				}
			} else {
				Log.i(LOG_TAG, "system shortcut controler is null");
				return false;
			}
		}
		// 系统文件夹
		else if (IItemType.ITEM_TYPE_LIVE_FOLDER == itemInfo.mItemType) {
			final SysFolderControler folderControler = AppCore.getInstance()
					.getSysFolderControler();
			if (null != folderControler) {
				ScreenLiveFolderInfo fInfo = (ScreenLiveFolderInfo) itemInfo;
				// 添加保护，防止空指针的出现
				String itemNameString = fInfo.mTitle == null ? null : fInfo.mTitle.toString();
				BitmapDrawable itemIcon = (fInfo.mIcon != null && fInfo.mIcon instanceof BitmapDrawable)
						? (BitmapDrawable) fInfo.mIcon
						: null;

				boolean bRet = folderControler.addSysFolder(fInfo.mBaseIntent, fInfo.mUri,
						fInfo.mDisplayMode, itemNameString, itemIcon);
				if (!bRet) {
					Log.i(LOG_TAG, "add system folder fail");
					return false;
				}
			} else {
				Log.i(LOG_TAG, "system folder controler is null");
				return false;
			}
		} else {
			// 其他 无特殊处理
		}

		try {
			if (itemInfo.mInScreenId == 0 || itemInfo.mInScreenId == -1) {
				// 如果没赋值，就赋值，有值的就不修改，因为其他层拖过来的item，还要通过此id来删除原视图
				itemInfo.mInScreenId = System.currentTimeMillis();
			}
			mDataModel.addDesktopItem(screenIndex, itemInfo);

			mDataModel.prepareItemInfo(itemInfo);

			// 缓存相应修改
			addItemInfoToHashMap(itemInfo, screenIndex);

			return true;
		} catch (Exception e) {
			Log.i(LOG_TAG, "keep data (add item) exception");
			return false;
		}
	}

	/**
	 * 移动桌面item的位置
	 * 
	 * @author huyong
	 * @param screenIndex
	 *            目标屏幕
	 * @param desktopItemInScreenId
	 *            组件在屏幕上的id
	 * @param screenX
	 *            屏幕x坐标
	 * @param screenY
	 *            屏幕y坐标
	 * @param spanX
	 *            占x方向网格个数
	 * @param spanY
	 *            占y方向网格个数
	 */
	public synchronized boolean updateDesktopItem(final int screenIndex, ItemInfo itemInfo) {
		if (null == itemInfo) {
			Log.i(LOG_TAG, "move null item");
			return false;
		}
		if (!screenExsit(screenIndex)) {
			Log.i(LOG_TAG, "move item to not exist screen");
			return false;
		}

		try {
			mDataModel.updateDesktopItem(screenIndex, itemInfo);

			// 修改缓存数据
			delItemInfoFromHashMap(itemInfo.mInScreenId);
			addItemInfoToHashMap(itemInfo, screenIndex);

			return true;
		} catch (Exception e) {
			Log.i(LOG_TAG, "keep data (move item) exception");
			return false;
		}
	}

	/**
	 * 只清除db及hashmap中的数据
	 * 
	 * @param itemInfo
	 */
	public synchronized void removeDesktopItemInDBAndCache(ItemInfo itemInfo) {
		mDataModel.removeDesktopItem(itemInfo.mInScreenId);
		// 修改相应缓存
		delItemInfoFromHashMap(itemInfo.mInScreenId);
	}

	/**
	 * 
	 * @author huyong
	 * @param desktopItemInScreenId
	 *            组件在屏幕上的id
	 */
	public synchronized boolean removeDesktopItem(ItemInfo itemInfo) {
		if (null == itemInfo) {
			Log.i(LOG_TAG, "remove desk item param is null");
			return false;
		}

		try {
			removeDesktopItemInDBAndCache(itemInfo);
			// 清除绑定
			itemInfo.selfDestruct();
		} catch (Exception e) {
			return false;
		}

		// 特殊处理
		// 快捷方式
		if (IItemType.ITEM_TYPE_SHORTCUT == itemInfo.mItemType) {
			final SysShortCutControler shortCutControler = AppCore.getInstance()
					.getSysShortCutControler();
			if (null != shortCutControler) {
				ShortCutInfo shortCutInfoItem = (ShortCutInfo) itemInfo;
				boolean bRet = shortCutControler.delSysShortCut(shortCutInfoItem.mIntent);
				if (!bRet) {
					Log.i(LOG_TAG, "delete system shortcut fail");
				}
			} else {
				Log.i(LOG_TAG, "system shortcut controler is null");
			}
		}
		// 系统文件夹
		else if (IItemType.ITEM_TYPE_LIVE_FOLDER == itemInfo.mItemType) {
			final SysFolderControler folderControler = AppCore.getInstance()
					.getSysFolderControler();
			if (null != folderControler) {
				ScreenLiveFolderInfo liveFolderInfo = (ScreenLiveFolderInfo) itemInfo;
				boolean bRet = folderControler.delSysFolder(liveFolderInfo.mBaseIntent,
						liveFolderInfo.mUri);
				if (!bRet) {
					Log.i(LOG_TAG, "delete system folder fail");
					return false;
				}
			} else {
				Log.i(LOG_TAG, "system folder controler is null");
				return false;
			}
		} else {
			// 其他 无特殊处理
		}

		return true;
	}

	/**
	 * **************************************文件夹操作******************************
	 * *
	 */
	/**
	 * 移动桌面上指定item到指定文件夹
	 * 
	 * @param itemInScreenId
	 * @param folderId
	 * @param index
	 *            -1 追尾
	 */
	synchronized boolean moveDesktopItemToFolder(final ItemInfo itemInScreen, final long folderId,
			int index) {
		try {
			mDataModel.moveScreenItemToFolder(itemInScreen, folderId, index);

			// 缓存控制
			delItemInfoFromHashMap(itemInScreen.mInScreenId);

			return true;
		} catch (Exception e) {
			Log.i(LOG_TAG, "move to fold exception");
			return false;
		}
	}

	/**
	 * **************************************文件夹操作******************************
	 * *
	 */
	/**
	 * 添加ItemInfo到指定文件夹
	 * 
	 * @param itemInfo
	 * @param folderId
	 */
	synchronized boolean addItemInfoToFolder(ItemInfo itemInfo, final long folderId) {
		try {
			mDataModel.addItemToFolder(itemInfo, folderId);
			return true;
		} catch (Exception e) {
			Log.i(LOG_TAG, "move to fold exception");
			return false;
		}
	}

	/**
	 * 移动桌面上指定item到指定文件夹
	 * 
	 * @param itemInScreenId
	 * @param folderId
	 * @return
	 */
	public synchronized boolean moveDesktopItemToFolder(final ItemInfo itemInScreen, final long folderId) {
		try {
			return moveDesktopItemToFolder(itemInScreen, folderId, -1);
		} catch (Exception e) {
			Log.i(LOG_TAG, "get fold count exception");
			return false;
		}
	}

	/**
	 * 从文件夹移除
	 * 
	 * @param itemInScreenId
	 * @param folderId
	 * @return
	 */
	synchronized boolean moveDesktopItemFromFolder(ItemInfo info, int screenIndex,
			final long folderId) {
		if (null == info) {
			Log.i(LOG_TAG, "move item from folder, the item info is null");
			return false;
		}
		if (!screenExsit(screenIndex)) {
			Log.i(LOG_TAG, "move item from folder, the des screen is not exsit");
			return false;
		}
		try {
			mDataModel.moveScreenItemFromFolder(info, folderId);

			return true;
		} catch (Exception e) {
			Log.i(LOG_TAG, "move from fold exception");
			return false;
		}
	}

	/**
	 * 增加用户文件夹 将数据写入文件夹表 * 在此函数前必须addDesktopItem增加一个屏幕文件夹项
	 * 
	 * @param itemInfos
	 * @return
	 */
	synchronized boolean addUserFolderContent(UserFolderInfo folderInfo, boolean fromDrawer) {
		if (null == folderInfo) {
			return false;
		}
		int count = folderInfo.getChildCount();
		for (int i = 0; i < count; i++) {
			ShortCutInfo info = folderInfo.getChildInfo(i);
			if (null == info) {
				continue;
			}
			info.mInScreenId = System.currentTimeMillis();
			mDataModel.addItemToFolder(info, folderInfo.mInScreenId, i + count, fromDrawer);
		}
		return count > 0;
	}

	/**
	 * 增加用户文件夹 将数据写入文件夹表 * 在此函数前必须addDesktopItem增加一个屏幕文件夹项
	 * 
	 * @param itemInfos
	 * @return
	 */
	synchronized boolean addUserFolderContent(final long folderId, ArrayList<ItemInfo> itemInfos,
			boolean fromDrawer) {
		if (null == itemInfos) {
			Log.i(LOG_TAG, "add folder content, but the content is null");
			return false;
		}
		int count = mDataModel.getUserFolderCount(folderId);
		int sz = itemInfos.size();
		for (int i = 0; i < sz; i++) {
			ItemInfo info = itemInfos.get(i);
			if (null == info) {
				continue;
			}
			info.mInScreenId = System.currentTimeMillis();
			mDataModel.addItemToFolder(info, folderId, i + count, fromDrawer);
		}
		return true;
	}

	synchronized boolean removeUserFolderContent(final long folderId,
			ArrayList<ItemInfo> itemInfos, boolean fromDrawer) {
		if (null == itemInfos) {
			Log.i(LOG_TAG, "add folder content, but the content is null");
			return false;
		}
		int sz = itemInfos.size();
		for (int i = 0; i < sz; i++) {
			ItemInfo info = itemInfos.get(i);
			if (null == info) {
				continue;
			}
			removeItemFromFolder(info, folderId, fromDrawer);
			// 移除时取出绑定
			info.selfDestruct();
		}
		return true;
	}

	synchronized void removeItemFromFolder(ItemInfo info, long folderId, boolean fromDrawer) {
		if (info.mInScreenId != 0) {
			mDataModel.removeItemFromFolder(info.mInScreenId, folderId);
		} else {
			if (info instanceof ShortCutInfo) {
				mDataModel
						.removeItemFromFolder(((ShortCutInfo) info).mIntent, fromDrawer, folderId);
			}
		}
	}

	/**
	 * 删除指定文件夹内指定ShortCutInfo的图标
	 * 
	 * @param folderInfo
	 * @param intent
	 */
	public void removeItemsFromFolder(UserFolderInfo folderInfo, ShortCutInfo shortCutInfo) {
		// 1:删除缓存
		ArrayList<ShortCutInfo> list = folderInfo.remove(shortCutInfo);
		// 2:删除DB
		int size = list.size();
		if (size > 0) {
			final long folderId = folderInfo.mInScreenId;
			for (int i = 0; i < size; i++) {
				ShortCutInfo info = list.get(i);
				mDataModel.removeItemFromFolder(info.mInScreenId, folderId);
			}
		}
		list.clear();
		list = null;
	}

	synchronized boolean removeUserFolder(ItemInfo info) {
		if (null == info) {
			Log.i(LOG_TAG, "remove user folder, the param is null");
			return false;
		}
		try {
			// 1. 删除屏幕项
			// 2. 删除文件夹表对应项
			boolean bOk = removeDesktopItem(info);
			if (bOk) {
				mDataModel.removeUserFolder(info.mInScreenId);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.i(LOG_TAG, "delete folder table exception");
			return false;
		}
	}

	/**
	 * 获取文件夹项
	 * 
	 * @param folderId
	 * @return
	 */
	public synchronized ArrayList<ItemInfo> getFolderItems(final long folderId) {
		return mDataModel.getScreenFolderItems(folderId, -1, true);
	}

	synchronized ArrayList<ItemInfo> getFolderItems(final long folderId, boolean prepare) {
		return mDataModel.getScreenFolderItems(folderId, -1, prepare);
	}

	synchronized ArrayList<ItemInfo> getFolderItems(final long folderId, int count) {
		return mDataModel.getScreenFolderItems(folderId, count, true);
	}

	ArrayList<ItemInfo> handleSDIsReady() {
		ArrayList<ItemInfo> ret = new ArrayList<ItemInfo>();

		int mapSz = mScreenHashMap.size();
		for (int i = 0; i < mapSz; i++) {
			ArrayList<ItemInfo> infos = mScreenHashMap.get(i);
			if (null == infos) {
				continue;
			}
			int listSz = infos.size();
			for (int j = 0; j < listSz; j++) {
				ItemInfo info = infos.get(j);
				if (null == info) {
					continue;
				}
				boolean bChanged = mDataModel.prepareItemInfo(info);

				// 文件夹
				if (IItemType.ITEM_TYPE_USER_FOLDER == info.mItemType) {
					UserFolderInfo folderInfo = (UserFolderInfo) info;
					bChanged |= mDataModel.prepareItemInfo(folderInfo);
				}

				if (bChanged) {
					ret.add(info);
				}
			}
		}
		return ret;
	}

	synchronized ArrayList<ItemInfo> unInstallApp(Intent intent) {
		if (null == intent || null == mScreenHashMap) {
			return null;
		}

		ArrayList<ItemInfo> itemInfos = null;
		int mapSz = mScreenHashMap.size();
		for (int i = 0; i < mapSz; i++) {
			ArrayList<ItemInfo> infos = mScreenHashMap.get(i);
			if (null == infos) {
				continue;
			}
			int listSz = infos.size();
			for (int j = 0; j < listSz; j++) {
				ItemInfo info = infos.get(j);
				if (null == info) {
					continue;
				}
				if (!(info instanceof UserFolderInfo)) {
					continue;
				}

				// 清理文件夹 子项
				if (IItemType.ITEM_TYPE_USER_FOLDER == info.mItemType) {
					ArrayList<ItemInfo> deskInfos = getFolderItems(info.mInScreenId, false);
					int deskSz = deskInfos.size();
					for (int k = 0; k < deskSz; k++) {
						ItemInfo deskInfo = deskInfos.get(k);
						if (null == deskInfo) {
							continue;
						}
						if (ConvertUtils.intentCompare(intent, ((ShortCutInfo) deskInfo).mIntent)) {
							try {
								mDataModel.removeItemFromFolder(deskInfo.mInScreenId,
										info.mInScreenId);

								if (null == itemInfos) {
									itemInfos = new ArrayList<ItemInfo>();
								}
								itemInfos.add(info);
							} catch (Exception e) {
								Log.i(LOG_TAG, "uninstall process remove folder exception");
							}

						}
					}
				}

				// 屏幕项清理
				// Intent itemIntent = getItemInfoIntent(info);
				// if (ConvertUtils.intentCompare(intent, itemIntent))
				// {
				// removeDesktopItem(info);
				// }
			}
		}

		return itemInfos;
	}

	/**
	 * **************************************缓存处理*******************************
	 * **
	 * ************************************文件夹不缓存*******************************
	 */

	/**
	 * 增加一个屏幕 大于指定位置的屏幕向后 插入屏幕
	 * 
	 * @param screenIndex
	 *            屏幕位置,MAP不对位置做保护
	 * @param itemInfos
	 *            屏幕元素 NULL 则为空屏幕
	 */
	private void addScreenToHashMap(int screenIndex, ArrayList<ItemInfo> itemInfos) {
		int sz = mScreenHashMap.size();
		for (int i = sz - 1; i >= screenIndex; i--) {
			ArrayList<ItemInfo> infos = mScreenHashMap.get(i);
			if (null != infos) {
				mScreenHashMap.put(i + 1, infos);
			}
		}
		if (null == itemInfos) {
			itemInfos = new ArrayList<ItemInfo>();
		}
		mScreenHashMap.put(screenIndex, itemInfos);

		// 保存当前有多少屏，供挂掉的时候err report反馈
		int count = mScreenHashMap.size();
		saveScreenCount(count);
	}

	/**
	 * 删除一个屏幕 小于屏幕索引不变 等于屏幕索引取值 大于屏幕索引的依次前靠 移除最后一个
	 * 
	 * @param screenIndex
	 *            屏幕位置
	 * @return 屏幕内容
	 */
	private ArrayList<ItemInfo> delScreenToHashMap(int screenIndex) {
		ArrayList<ItemInfo> data = null;
		int sz = mScreenHashMap.size();
		if (screenIndex < sz) {
			data = mScreenHashMap.get(screenIndex);
		}
		for (int i = screenIndex + 1; i < sz; i++) {
			ArrayList<ItemInfo> infos = mScreenHashMap.get(i);
			if (null != infos) {
				mScreenHashMap.put(i - 1, infos);
			}
		}
		mScreenHashMap.remove(sz - 1);

		// 保存当前有多少屏，供挂掉的时候err report反馈
		int count = mScreenHashMap.size();
		saveScreenCount(count);
		return data;
	}

	private void saveScreenCount(int count) {
		Context context = GOLauncherApp.getContext();
		PreferencesManager spf = new PreferencesManager(context, IPreferencesIds.ERRORREPORTER, 0);
		spf.putInt("SCREEN_COUNT", count);
		spf.commit();
		try {
			spf.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 增加一个元素
	 * 
	 * @param info
	 * @param screenIndex
	 */
	private void addItemInfoToHashMap(ItemInfo info, int screenIndex) {
		info.mScreenIndex = screenIndex;
		ArrayList<ItemInfo> itemInfos = mScreenHashMap.get(screenIndex);
		if (null == itemInfos) {
			itemInfos = new ArrayList<ItemInfo>();
			itemInfos.add(info);

			mScreenHashMap.put(screenIndex, itemInfos);
		} else {
			itemInfos.add(info);
		}

		// 保存当前有多少屏，供挂掉的时候err report反馈
		int count = mScreenHashMap.size();
		saveScreenCount(count);
	}

	/**
	 * 删除一个元素
	 * 
	 * @param itemScreenId
	 */
	private void delItemInfoFromHashMap(long itemScreenId) {
		int sz = mScreenHashMap.size();
		for (int i = 0; i < sz; i++) {
			ArrayList<ItemInfo> itemInfos = mScreenHashMap.get(i);
			if (null != itemInfos) {
				for (int j = 0; j < itemInfos.size(); j++) {
					ItemInfo itemInfo = itemInfos.get(j);
					if (null != itemInfo && itemInfo.mInScreenId == itemScreenId) {
						itemInfos.remove(j);
					}
				}
			}
		}
	}

	// add by huyong 2011-03-17 for 清理桌面脏数据
	/**
	 * 判断桌面是否有脏数据
	 */
	boolean isScreenDirtyData() {
		return mDataModel.isScreenDirtyData();
	}

	/**
	 * 清理桌面表中脏数据
	 * 
	 * @author huyong
	 */
	void clearScreenDirtyData() {
		mDataModel.clearScreenDirtyData();
	}

	// add by huyong end 2011-03-17 for 清理桌面脏数据

	protected ArrayList<GoWidgetBaseInfo> getAllGoWidgetInfos() {
		return mDataModel.getAllGoWidgetInfos();
	}

	protected int findSpecificGoWidgetScreenIndex(int gowidgetId) {
		return mDataModel.getGoWidgetScreenIndexByWidgetId(gowidgetId);
	}

	/**
	 * 图标丢失调查
	 */
	public HashMap<Integer, ArrayList<ItemInfo>> getmScreenHashMap() {
		return mScreenHashMap;
	}

	public void getScreenItems(int index, ArrayList<ItemInfo> itemArray) {
		mDataModel.getScreenItems(index, itemArray);
	}

	public void updateDBItem(ItemInfo itemInfo) {
		mDataModel.updateDBItem(itemInfo);
	}
}
