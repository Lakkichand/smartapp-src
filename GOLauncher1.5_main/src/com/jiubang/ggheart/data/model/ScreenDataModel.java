package com.jiubang.ggheart.data.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.ImageUtil;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenMissIconBugUtil;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DockItemControler;
import com.jiubang.ggheart.data.SelfAppItemInfoControler;
import com.jiubang.ggheart.data.SysFolderControler;
import com.jiubang.ggheart.data.SysShortCutControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ItemInfoFactory;
import com.jiubang.ggheart.data.info.ScreenLiveFolderInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.SelfAppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.StaticScreenSettingInfo;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.data.tables.PartToScreenTable;
import com.jiubang.ggheart.data.tables.ScreenTable;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.recommend.localxml.XmlRecommendedApp;
import com.jiubang.ggheart.recommend.localxml.XmlRecommendedAppInfo;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 */
public class ScreenDataModel extends DataModel {
	private static final String LOG_TAG = "ScreenDataModelLog";
	private static final String GOSTOREPKGNAME = "com.gau.diy.gostore";
	private static final String GOTHEMEPKGNAME = "com.gau.diy.gotheme";
	private static final String GOWIDGETPKGNAME = "com.gau.diy.gowidget";
	public static final String RECOMMAND_APP_FTP_URL_KEY = "recommand_app_ftp_url_key";

	public ScreenDataModel(final Context context) {
		super(context);
	}

	/**
	 * 
	 * @author huyong
	 * @return
	 */
	public void getScreenItems(HashMap<Integer, ArrayList<ItemInfo>> itemInfoMap) {
		// 找出所有屏幕
		ArrayList<Long> screenIDList = new ArrayList<Long>();
		Cursor cursor = mDataProvider.getScreenIDList();
		if (null == cursor) {
			Log.i(LOG_TAG, "query screen index, data cursor is null");
			return;
		}
		// add begin rongjinsong 2011-06-21
		// this block add for fix getColumnIndex return -1
		// if cursor do not contain SCREENID,try 10 times
		int colIndex = cursor.getColumnIndex(ScreenTable.SCREENID);
		int nCount = 10;
		while (-1 == colIndex && 0 < nCount--) {

			cursor.close();
			SystemClock.sleep(50);
			cursor = mDataProvider.getScreenIDList();
			if (null == cursor) {
				Log.i(LOG_TAG, "query screen index, data cursor is null");
				return;
			}
			colIndex = cursor.getColumnIndex(ScreenTable.SCREENID);
		}
		// if still can not get index ,throw IllegalStateException
		if (-1 == colIndex) {
			StringBuffer erroMsg = new StringBuffer("ColumName:");
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				erroMsg.append(cursor.getColumnName(i));
				erroMsg.append(";");
			}
			Log.i(LOG_TAG, erroMsg.toString());

		}
		// add end

		convertCursorToScreenId(cursor, screenIDList);
		if (screenIDList == null || screenIDList.size() < 0) {
			Log.i(LOG_TAG, "no screen");
			return;
		}

		// 每个屏幕找到所有元素
		int sz = screenIDList.size();
		for (int index = 0; index < sz; index++) {
			ArrayList<ItemInfo> itemArray = new ArrayList<ItemInfo>();
			try {
				getScreenItems(screenIDList.get(index), itemArray);
			} catch (Exception e) {
				// 获取屏幕元素失败，清理该屏幕所有元素
				// 获取过程中出现问题就清理整一个屏幕的元素不合理，所以注释掉删除屏幕元素的代码 -by Yugi 2012.9.6
				// mDataProvider.removeScreenContent(index);
				// itemArray.clear();
				ScreenMissIconBugUtil
						.showToast(ScreenMissIconBugUtil.ERROR_A_SCREEN_GETSCREENITEMS_EXCEPTION);
			}
			itemInfoMap.put(index, itemArray);
		}
	}

	public int getGoWidgetScreenIndexByWidgetId(int widgetId) {
		long screenId = mDataProvider.getScreenIdByGowidgetId(widgetId);
		if (screenId != -1) {
			return mDataProvider.getScreenIndexById(screenId);
		}

		return -1;
	}

	/**
	 * 
	 * @author huyong
	 * @param screenId
	 * @return
	 */
	public void getScreenItems(final long screenId, ArrayList<ItemInfo> itemArray) {
		Cursor itemCursor = mDataProvider.getScreenItems(screenId);
		if (null != itemCursor) {
			convertCursorToItemInfo(itemCursor, itemArray);
			prepareItemInfo(itemArray);
		} else {
			Log.i(LOG_TAG, "get screen items, the data cursor is null");
		}
	}

	private void convertCursorToScreenId(final Cursor cursor, ArrayList<Long> screenList) {
		if (cursor.moveToFirst()) {
			do {
				int index = cursor.getColumnIndex(ScreenTable.SCREENID);
				if (-1 == index) {
					// masanbing 2011-05-31 11:24
					// 尝试解决数据库读取字段 -1 的问题
					// 中断次数 10
					// 中断主线程50ms
					int count = 10;
					long wait = 50;
					for (int i = 0; i < count; i++) {
						SystemClock.sleep(wait);
						index = cursor.getColumnIndex(ScreenTable.SCREENID);
						if (-1 != index) {
							break;
						}
					}
				}
				Long screenId = cursor.getLong(index);
				screenList.add(screenId);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	private void convertCursorToItemInfo(final Cursor cursor, ArrayList<ItemInfo> itemArray) {
		if (cursor.moveToFirst()) {
			do {
				try {
					int itemType = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.ITEMTYPE));
					ItemInfo info = ItemInfoFactory.createItemInfo(itemType);
					info.readObject(cursor, PartToScreenTable.TABLENAME);
					itemArray.add(info);
				} catch (Exception e) {
					// 获取元素过程中出现问题，将跳过继续获取下一个元素 -by Yugi 2012.9.6
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	public boolean prepareItemInfo(UserFolderInfo folderInfo) {
		boolean bRet = false;
		final int count = folderInfo.getChildCount();
		for (int i = 0; i < count; i++) {
			ShortCutInfo item = folderInfo.getChildInfo(i);
			if (null != item) {
				if (prepareItemInfo(item)) {
					bRet = true;
				}
			}
		}
		return bRet;
	}

	public boolean prepareItemInfo(ArrayList<ItemInfo> itemArray) {
		boolean bRet = false;
		int sz = itemArray.size();
		for (int i = 0; i < sz; i++) {
			if (prepareItemInfo(itemArray.get(i))) {
				bRet = true;
			}
		}
		return bRet;
	}

	private AppItemInfo getAppItemInfo(Intent intent, Uri uri, int type) {
		AppItemInfo info = null;
		switch (type) {
			case IItemType.ITEM_TYPE_APPLICATION : {
				AppDataEngine appEngine = GOLauncherApp.getAppDataEngine();
				if (appEngine != null) {
					info = appEngine.getAppItem(intent);
				}
			}
				break;
			case IItemType.ITEM_TYPE_SHORTCUT : {
				DockItemControler dockItemControler = AppCore.getInstance().getDockItemControler();
				if (null != dockItemControler) {
					info = dockItemControler.getDockAppItemInfo(intent);
				}
				if (null == info) {
					SysShortCutControler shortcutEngine = AppCore.getInstance()
							.getSysShortCutControler();
					if (shortcutEngine != null) {
						info = shortcutEngine.getSysShortCutItemInfo(intent);
					}
				}
			}
				break;
			case IItemType.ITEM_TYPE_USER_FOLDER : {
				SelfAppItemInfoControler selfAppEngine = AppCore.getInstance()
						.getSelfAppItemInfoControler();
				if (selfAppEngine != null) {
					info = selfAppEngine.getUserFolder();
				}
			}
				break;

			case IItemType.ITEM_TYPE_LIVE_FOLDER : {
				SysFolderControler liveFolderEngine = AppCore.getInstance().getSysFolderControler();
				if (liveFolderEngine != null) {
					info = liveFolderEngine.getSysFolderItemInfo(intent, uri);
				}
			}
				break;

			default :
				break;
		}

		if (null == info) {
			SelfAppItemInfoControler selfAppEngine = AppCore.getInstance()
					.getSelfAppItemInfoControler();
			if (selfAppEngine != null) {
				info = selfAppEngine.getDefaultApplication();
			}
		}
		return info;
	}

	public boolean prepareItemInfo(ItemInfo info) {
		boolean bRet = false;
		if (null == info) {
			return bRet;
		}
		try {
			// 关联
			// 图标、名称
			switch (info.mItemType) {
				case IItemType.ITEM_TYPE_APPLICATION : {
					ShortCutInfo sInfo = (ShortCutInfo) info;
					if (null == sInfo.getRelativeItemInfo()) {
						bRet |= sInfo.setRelativeItemInfo(getAppItemInfo(sInfo.mIntent, null,
								sInfo.mItemType));
					} else if (sInfo.getRelativeItemInfo() instanceof SelfAppItemInfo) {
						AppItemInfo appItemInfo = getAppItemInfo(sInfo.mIntent, null,
								sInfo.mItemType);
						if (!(appItemInfo instanceof SelfAppItemInfo)) {
							bRet |= sInfo.setRelativeItemInfo(appItemInfo);
						}
					}
					if (null == sInfo.getFeatureIcon()) {
						bRet |= sInfo.prepareFeatureIcon();
					}

					// 数据冗余导致
					if (null != sInfo.getFeatureIcon()) {
						sInfo.mIcon = sInfo.getFeatureIcon();
						sInfo.mIsUserIcon = true;
					} else {
						if (null != sInfo.getRelativeItemInfo()) {
							sInfo.mIcon = sInfo.getRelativeItemInfo().getIcon();
						}
						sInfo.mIsUserIcon = false;
					}
					if (null != sInfo.getFeatureTitle()) {
						sInfo.mTitle = sInfo.getFeatureTitle();
						sInfo.mIsUserTitle = true;
					} else {
						if (null != sInfo.getRelativeItemInfo()) {
							sInfo.mTitle = sInfo.getRelativeItemInfo().getTitle();
						}
						sInfo.mIsUserTitle = false;
					}
				}
					break;

				case IItemType.ITEM_TYPE_SHORTCUT : {
					ShortCutInfo sInfo = (ShortCutInfo) info;
					if (null == sInfo.getRelativeItemInfo()) {
						bRet |= sInfo.setRelativeItemInfo(getAppItemInfo(sInfo.mIntent, null,
								sInfo.mItemType));
					}
					if (null == sInfo.getFeatureIcon()) {
						bRet |= sInfo.prepareFeatureIcon();
					}

					// 数据冗余导致
					if (null != sInfo.getFeatureIcon()) {
						sInfo.mIcon = sInfo.getFeatureIcon();
						sInfo.mIsUserIcon = true;
					} else {
						if (null != sInfo.getRelativeItemInfo()) {
							sInfo.mIcon = sInfo.getRelativeItemInfo().getIcon();
						}
						sInfo.mIsUserIcon = false;
					}
					if (null != sInfo.getFeatureTitle()) {
						sInfo.mTitle = sInfo.getFeatureTitle();
						sInfo.mIsUserTitle = true;
					} else {
						if (null != sInfo.getRelativeItemInfo()) {
							sInfo.mTitle = sInfo.getRelativeItemInfo().getTitle();
						}
						sInfo.mIsUserTitle = false;
					}
				}
					break;

				case IItemType.ITEM_TYPE_USER_FOLDER : {
					UserFolderInfo fInfo = (UserFolderInfo) info;
					if (null == fInfo.getRelativeItemInfo()) {
						bRet |= fInfo.setRelativeItemInfo(getAppItemInfo(null, null,
								fInfo.mItemType));
					}
					if (null == fInfo.getFeatureIcon()) {
						bRet |= fInfo.prepareFeatureIcon();
					}

					// 数据冗余导致
					if (null != fInfo.getFeatureIcon()) {
						fInfo.mIcon = fInfo.getFeatureIcon();
						fInfo.mIsUserIcon = true;
					} else {
						if (null != fInfo.getRelativeItemInfo()) {
							fInfo.mIcon = fInfo.getRelativeItemInfo().getIcon();
						}
						fInfo.mIsUserIcon = false;
					}
					if (null != fInfo.getFeatureTitle()) {
						fInfo.mTitle = fInfo.getFeatureTitle();
						// fInfo.mIsUserTitle = true;
					} else {
						if (null != fInfo.getRelativeItemInfo()) {
							fInfo.mTitle = fInfo.getRelativeItemInfo().getTitle();
						}
						// fInfo.mIsUserTitle = false;
					}
				}
					break;

				case IItemType.ITEM_TYPE_LIVE_FOLDER : {
					ScreenLiveFolderInfo fInfo = (ScreenLiveFolderInfo) info;
					if (null == fInfo.getRelativeItemInfo()) {
						bRet |= fInfo.setRelativeItemInfo(getAppItemInfo(fInfo.mBaseIntent,
								fInfo.mUri, fInfo.mItemType));
					}
					if (null == fInfo.getFeatureIcon()) {
						bRet |= fInfo.prepareFeatureIcon();
					}

					// 数据冗余导致
					if (null != fInfo.getFeatureIcon()) {
						fInfo.mIcon = fInfo.getFeatureIcon();
						// fInfo.mIsUserIcon = true;
					} else {
						if (null != fInfo.getRelativeItemInfo()) {
							fInfo.mIcon = fInfo.getRelativeItemInfo().getIcon();
						}
						// fInfo.mIsUserIcon = false;
					}
					if (null != fInfo.getFeatureTitle()) {
						fInfo.mTitle = fInfo.getFeatureTitle();
						// fInfo.mIsUserTitle = true;
					} else {
						if (null != fInfo.getRelativeItemInfo()) {
							fInfo.mTitle = fInfo.getRelativeItemInfo().getTitle();
						}
						// fInfo.mIsUserTitle = false;
					}
				}
					break;

				default :
					break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return bRet;
	}

	/*********************************** 屏幕操作 **************************************/
	/**
	 * 添加屏幕
	 * 
	 * @author huyong
	 * @param screenIndex
	 *            屏幕索引
	 * @return 新增的屏幕id
	 */
	public long addScreen(final int screenIndex) {
		ContentValues contentValues = new ContentValues();
		long screenId = System.currentTimeMillis();
		screenId += screenIndex;
		contentValues.put(ScreenTable.SCREENID, screenId);
		contentValues.put(ScreenTable.MINDEX, screenIndex);
		mDataProvider.addScreen(contentValues);

		return screenId;
	}

	/**
	 * 移除指定屏幕
	 * 
	 * @author huyong
	 * @param screenIndex
	 */
	public void delScreen(final int screenIndex) {
		mDataProvider.removeScreen(screenIndex);
	}

	/**
	 * 移动屏幕位置
	 * 
	 * @author huyong
	 * @param srcScreenIndex
	 *            屏幕源索引
	 * @param DescScreenIndex
	 *            屏幕目标索引
	 */
	public void moveScreen(final int srcScreenIndex, final int DescScreenIndex) {
		mDataProvider.moveScreen(srcScreenIndex, DescScreenIndex);
	}

	/**
	 * 获取屏幕数量
	 * 
	 * @return 屏幕数量
	 */
	public int getScreenCount() {
		return mDataProvider.getScreenCount();
	}

	/********************************* 屏幕元素操作 ************************************/
	/**
	 * 修改桌面元素
	 * 
	 * @param screenIndex
	 * @param itemInfo
	 */
	public void updateDesktopItem(final int screenIndex, final ItemInfo info) {
		long screenId = mDataProvider.getScreenIDByIndex(screenIndex);
		if (screenId == -1) {
			Log.i(LOG_TAG, "update item position, can not find screen id from index");
			return;
		}
		ContentValues values = new ContentValues();
		info.writeObject(values, PartToScreenTable.TABLENAME);
		values.put(PartToScreenTable.SCREENID, screenId);
		mDataProvider.updateScreen(info.mInScreenId, values);
	}

	public void updateDBItem(final ItemInfo info) {
		ContentValues values = new ContentValues();
		info.writeObject(values, PartToScreenTable.TABLENAME);
		mDataProvider.updateScreen(info.mInScreenId, values);
	}

	public void updateFolderIndex(long folderID, ArrayList<ItemInfo> infos) {
		int count = infos.size();
		for (int i = 0; i < count; i++) {
			mDataProvider.updateFolderIndex(folderID, infos.get(i).mInScreenId, i);
		}
	}

	public void updateFolderItem(long folderID, ItemInfo info) {
		ContentValues values = new ContentValues();
		info.writeObject(values, FolderTable.TABLENAME);
		mDataProvider.updateFolderItem(folderID, info.mInScreenId, values);
	}

	/**
	 * 增加桌面元素
	 * 
	 * @param screenIndex
	 * @param itemInfo
	 */
	public void addDesktopItem(final int screenIndex, ItemInfo itemInfo) {
		long screenId = mDataProvider.getScreenIDByIndex(screenIndex);
		if (screenId <= 0) {
			Log.i(LOG_TAG, "add desk item, the des screen id no exsit");
			return;
		}

		ContentValues values = new ContentValues();
		itemInfo.writeObject(values, PartToScreenTable.TABLENAME);
		values.put(PartToScreenTable.SCREENID, screenId);
		mDataProvider.addItemToScreen(values);
	}

	/**
	 * 移除桌面元素
	 * 
	 * @param desktopItemInScreenId
	 */
	public void removeDesktopItem(final long desktopItemInScreenId) {
		mDataProvider.removeDesktopItem(desktopItemInScreenId);
	}

	/********************************* 文件夹操作 *************************************/
	/**
	 * 移动桌面元素到文件夹 1. 获取屏幕相关数据信息 2. 转化为文件夹项相关数据信息 3. 插入文件夹表 4. 从屏幕表删除
	 * 
	 * @param screenItemId
	 * @param folderId
	 * @param index
	 */
	// TODO 暂时不考虑拖到相应位子
	public void moveScreenItemToFolder(ItemInfo screenItem, long folderId, int index)
			throws Exception {
		ContentValues values = new ContentValues();
		screenItem.writeObject(values, FolderTable.TABLENAME);
		values.put(FolderTable.FOLDERID, folderId);
		int sz = mDataProvider.getSizeOfFolder(folderId);
		values.put(FolderTable.INDEX, sz);
		final long time = System.currentTimeMillis();
		values.put(FolderTable.TIMEINFOLDER, time);

		mDataProvider.addFolderItem(values);
		mDataProvider.removeDesktopItem(screenItem.mInScreenId);
		if (screenItem instanceof ShortCutInfo) {
			((ShortCutInfo) screenItem).mTimeInFolder = time;
		}
	}

	/**
	 * 移动元素到文件夹 1. 获取屏幕相关数据信息 2. 转化为文件夹项相关数据信息 3. 插入文件夹表 4. 从屏幕表删除
	 * 
	 * @param screenItemId
	 * @param index
	 */
	public void addItemToFolder(ItemInfo itemInfo, long folderId) throws Exception {

		ContentValues values = new ContentValues();
		values.put(FolderTable.FOLDERID, folderId);
		boolean bTrans = itemInfoToFolderContentValues(itemInfo, values);
		if (!bTrans) {
			Log.i(LOG_TAG, "move screen to folder, tran the screen item info err");
			throw new Exception("move screen to folder, tran the screen item info err");
			// return;
		}
		int sz = mDataProvider.getSizeOfFolder(folderId);
		values.put(FolderTable.INDEX, sz);
		final long time = System.currentTimeMillis();
		values.put(FolderTable.TIMEINFOLDER, time);
		mDataProvider.addFolderItem(values);
		if (itemInfo instanceof ShortCutInfo) {
			((ShortCutInfo) itemInfo).mTimeInFolder = time;
		}
	}

	/**
	 * 移除桌面元素从文件夹 1. 获取文件夹表项信息 2. 转化为屏幕表信息 3. 插入屏幕表信息 4. 删除文件夹表信息
	 * 
	 * @param screenItemId
	 * @param folderId
	 * @param index
	 */
	public void moveScreenItemFromFolder(ItemInfo screenItem, long folderId) throws Exception {
		mDataProvider.removeAppFromFolder(screenItem.mInScreenId, folderId);
	}

	/**
	 * 增加文件夹项
	 * 
	 * @param folderId
	 * @param intent
	 */
	public void addItemToFolder(ItemInfo screenItem, long folderId, int index, boolean fromDrawer) {
		ContentValues values = new ContentValues();
		screenItem.writeObject(values, FolderTable.TABLENAME);
		values.put(FolderTable.FOLDERID, folderId);
		values.put(FolderTable.INDEX, index);
		int tempInt = fromDrawer ? 1 : 0;
		values.put(FolderTable.FROMAPPDRAWER, tempInt);
		final long time = System.currentTimeMillis();
		values.put(FolderTable.TIMEINFOLDER, time);
		if (screenItem instanceof ShortCutInfo) {
			((ShortCutInfo) screenItem).mTimeInFolder = time;
		}
		mDataProvider.addFolderItem(values);
	}

	/**
	 * 移除文件夹项
	 * 
	 * @param screenItemId
	 * @param folderId
	 */
	public void removeItemFromFolder(long screenItemId, long folderId) {
		mDataProvider.removeAppFromFolder(screenItemId, folderId);
	}

	public void removeItemFromFolder(Intent intent, boolean fromDrawer, long folderId) {
		mDataProvider.removeAppFromFolder(intent, fromDrawer, folderId);
	}

	/**
	 * 文件夹内部移动
	 * 
	 * @param folderId
	 * @param srcIndex
	 * @param desIndex
	 */
	public void moveFolderItem(long folderId, int srcIndex, int desIndex) {
		mDataProvider.moveFolderItem(folderId, srcIndex, desIndex);
	}

	/**
	 * 获取Folder列表
	 * 
	 * @param folderId
	 * @return
	 */
	public ArrayList<ItemInfo> getScreenFolderItems(long folderId, int count, boolean prepare) {
		ArrayList<ItemInfo> folderItemList = new ArrayList<ItemInfo>();

		Cursor cursor = mDataProvider.getScreenFolderItems(folderId);
		convertCursorToFolderItem(cursor, folderItemList, count);

		if (prepare) {
			prepareItemInfo(folderItemList);
		}

		return folderItemList;
	}

	public int getUserFolderCount(long folderId) {
		int count = 0;
		Cursor cursor = mDataProvider.getScreenFolderItems(folderId);
		if (null != cursor) {
			count = cursor.getCount();
			cursor.close();
		}

		return count;
	}

	public void removeUserFolder(long folderId) {
		mDataProvider.delScreenFolderItems(folderId);
	}

	private boolean itemInfoToFolderContentValues(ItemInfo itemInfo, ContentValues values) {
		boolean bRet = false;
		if (null != itemInfo) {
			ShortCutInfo myInfo = (ShortCutInfo) itemInfo;
			long id = myInfo.mInScreenId;
			String inttentStr = ConvertUtils.intentToString(myInfo.mIntent);
			int type = myInfo.mItemType;
			String userTitleStr = null;
			if (null != myInfo.mFeatureTitle) {
				userTitleStr = myInfo.mFeatureTitle.toString();
			} else if (null != myInfo.mTitle) {
				userTitleStr = myInfo.mTitle.toString();
			}
			int iconType = myInfo.mFeatureIconType;
			int iconId = myInfo.mFeatureIconId;
			String iconPackage = myInfo.mFeatureIconPackage;
			String iconPath = myInfo.mFeatureIconPath;

			values.put(FolderTable.ID, id);
			values.put(FolderTable.INTENT, inttentStr);
			values.put(FolderTable.TYPE, type);
			values.put(FolderTable.USERTITLE, userTitleStr);
			values.put(FolderTable.USERICONTYPE, iconType);
			values.put(FolderTable.USERICONID, iconId);
			values.put(FolderTable.USERICONPACKAGE, iconPackage);
			values.put(FolderTable.USERICONPATH, iconPath);
			bRet = true;
		}
		return bRet;
	}

	private void convertCursorToFolderItem(final Cursor cursor,
			ArrayList<ItemInfo> folderItemInfos, int count) {
		final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
		if (cursor == null || dataEngine == null) {
			return;
		}

		if (cursor.moveToFirst()) {
			int transCount = 0;
			do {
				int type = cursor.getInt(cursor.getColumnIndex(FolderTable.TYPE));
				ItemInfo info = ItemInfoFactory.createItemInfo(type);
				info.readObject(cursor, FolderTable.TABLENAME);

				folderItemInfos.add(info);

				// 个数判定
				transCount++;
				if (-1 != count) {
					if (transCount == count) {
						break;
					}
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	/********************************* 初始化默认数据 ************************************/

	/**
	 * 初始化默认数据
	 * 
	 * @author huyong
	 */
	public void initDefaultData() {
		if (mDataProvider.isNewDB()) {
			initDesktopItem();
		}
	}

	public void cleanDesktopItem() {
		mDataProvider.clearTable(PartToScreenTable.TABLENAME);
		mDataProvider.clearTable(ScreenTable.TABLENAME);
	}

	/**
	 * 添加图标到桌面
	 * @param intent		图标的intent
	 * @param itemType		图标的类型————IItemType里面的几种类型
	 * @param columnCount	列数
	 * @param rowCount		行数
	 * @param screenIndex	屏幕下标
	 * @param titleResId	图标的名字resid————如果不是IItemType.ITEM_TYPE_SHORTCUT，则填-1
	 * @param icon			图标icon————如果不是IItemType.ITEM_TYPE_SHORTCUT，则填null
	 */
	private void createItemToDesk(Context context, Intent intent, int itemType, int columnCount,
			int rowCount, int screenIndex, int titleResId, Drawable icon) {
		ShortCutInfo shortCutInfo = new ShortCutInfo();
		shortCutInfo.mIntent = intent;
		shortCutInfo.mInScreenId = System.currentTimeMillis();
		shortCutInfo.mItemType = itemType;
		shortCutInfo.mCellX = columnCount;
		shortCutInfo.mCellY = rowCount;
		shortCutInfo.mSpanX = 1;
		shortCutInfo.mSpanY = 1;
		if (titleResId > 0) {
			// 如果id不为0
			shortCutInfo.mTitle = context.getString(titleResId);
		}
		if (icon != null) {
			// 如果icon不为空
			shortCutInfo.mIcon = icon;
		}
		if (itemType == IItemType.ITEM_TYPE_SHORTCUT) {
			writeToShortCutTable(shortCutInfo);
		}
		addDesktopItem(screenIndex, shortCutInfo);
	}
	
	/**
	 * 初始化默认桌面数据
	 * 
	 * @author huyong
	 * @param context
	 */
	public void initDesktopItem() {
		String[] packageName = ScreenUtils.getDefaultInitAppPkg();

		final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
		ArrayList<AppItemInfo> dbItemInfos = dataEngine.getAllAppItemInfos();
		if (dbItemInfos == null) {
			return;
		}

		// 添加屏幕
		for (int i = 0; i < ScreenSettingInfo.DEFAULT_SCREEN_COUNT; i++) {
			addScreen(i);
		}

		int rowCount = StaticScreenSettingInfo.sScreenRow - 1; // 最后一行

		int columnCount = 0;
		int dbItemsSize = dbItemInfos.size();
		int packageCount = packageName.length;
		int count = StaticScreenSettingInfo.sScreenCulumn >= 5 ? 5 : 4;
		// TODO:这里的扫描匹配算法可以优化
		ArrayList<ShortCutInfo> infos = addRecommendedApp(-1, -1, -1, true);
		for (int i = 0; i < packageCount; i++) {
			if (columnCount >= count) {
				// 我们只初始化前5个
				break;
			}

//			if (columnCount == 0) {
//				if (!checkHasAddRecommendApp(infos, columnCount, rowCount,
//						ScreenSettingInfo.DEFAULT_MAIN_SCREEN)) {
//					final UserFolderInfo userFolderInfo = new UserFolderInfo();
//					userFolderInfo.mInScreenId = System.currentTimeMillis();
//					userFolderInfo.mCellX = columnCount;
//					userFolderInfo.mCellY = rowCount;
//					userFolderInfo.setFeatureTitle(mContext.getResources().getString(
//							R.string.go_apps));
//					addDesktopItem(ScreenSettingInfo.DEFAULT_MAIN_SCREEN, userFolderInfo);
//					// 把GO系列应用添加到文件夹里
//					addGoAppsToDeskFolder(userFolderInfo);
//				}
//
//				++columnCount;
//				continue;
//			}

			if (columnCount == 1) {
				if (!checkHasAddRecommendApp(infos, columnCount, rowCount,
						ScreenSettingInfo.DEFAULT_MAIN_SCREEN)) {
					// 添加GO锁屏到桌面 add by Ryan 2012.10.11
					Intent golockerIntent = null;
					int itemType = -1;
					Drawable lockerIcon = null;
					if (AppUtils.isGoLockerExist(mContext)) {
						itemType = IItemType.ITEM_TYPE_APPLICATION;
						PackageManager pm = mContext.getPackageManager();
						golockerIntent = pm.getLaunchIntentForPackage(LauncherEnv.Plugin.RECOMMAND_GOLOCKER_PACKAGE);
						
						//几个版本的GO锁屏安装包,包名不同
						if (golockerIntent == null) {
							try {
								Intent queryIntent = new Intent(ICustomAction.ACTION_LOCKER);
								List<ResolveInfo> infosList = null;
								infosList = mContext.getPackageManager().queryIntentActivities(
										queryIntent, 0);
								if (infosList != null && infosList.size() > 0) {
									ResolveInfo resolveInfo = infosList.get(0);
									String pkg = resolveInfo.activityInfo.packageName;
									golockerIntent = pm.getLaunchIntentForPackage(pkg);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else {
						itemType = IItemType.ITEM_TYPE_SHORTCUT;
						golockerIntent = new Intent(
								ICustomAction.ACTION_RECOMMAND_GOLOCKER_DOWNLOAD);
						ComponentName cmpName = new ComponentName(
								LauncherEnv.Plugin.RECOMMAND_GOLOCKER_PACKAGE,
								LauncherEnv.Plugin.RECOMMAND_GOLOCKER_PACKAGE);
						golockerIntent.setComponent(cmpName);
						golockerIntent.putExtra(RECOMMAND_APP_FTP_URL_KEY,
								LauncherEnv.Url.GOLOCKER_FTP_URL);
						try {
							// 加download Tag
							lockerIcon = getGoAppsIcons(R.drawable.screen_edit_golocker);
						} catch (OutOfMemoryError e) {
							OutOfMemoryHandler.handle();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					createItemToDesk(mContext, golockerIntent, itemType, columnCount,
							rowCount, ScreenSettingInfo.DEFAULT_MAIN_SCREEN,
							R.string.customname_golocker, lockerIcon);
				}
				++columnCount;
				continue;
			}

			// 应用中心 add by Ryan 2012.07.27
			if (columnCount == 2) {
				if (!checkHasAddRecommendApp(infos, columnCount, rowCount,
						ScreenSettingInfo.DEFAULT_MAIN_SCREEN)) {
					Intent intent = null;
					ComponentName component = null;
					int itemType = -1; // 图标类型
					int screenIndex = -1; // 屏幕下标
					int titleResId = -1; // 标题resId
					Drawable icon = null; // 图标icon
					
					String country = Machine.getCountry(mContext);
					if (country.equals("in") || country.equals("id")
							|| country.equals("th") || country.equals("br")) {
						// 如果国家是印度、印尼、泰国或巴西，则显示搜索
						intent = new Intent(
								ICustomAction.ACTION_DESK_SHOW_BAIDUBROWSER);
						component = new ComponentName(
								LauncherEnv.Plugin.RECOMMEND_BAIDUBROWSER_PACKAGE,
								LauncherEnv.Plugin.RECOMMEND_BAIDUBROWSER_PACKAGE);
						intent.setComponent(component);
						itemType = IItemType.ITEM_TYPE_SHORTCUT;
						screenIndex = ScreenSettingInfo.DEFAULT_MAIN_SCREEN;
						titleResId = R.string.recommend_baidu;
						icon = mContext.getResources().getDrawable(
								R.drawable.recommend_icon_baidu);
					} else {
						// 应用中心
						intent = new Intent(
								ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER);
						component = new ComponentName(
								LauncherEnv.RECOMMAND_CENTER_PACKAGE_NAME,
								ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER);
						intent.setComponent(component);
						itemType = IItemType.ITEM_TYPE_APPLICATION;
						screenIndex = ScreenSettingInfo.DEFAULT_MAIN_SCREEN;
					}
					
					createItemToDesk(mContext, intent, itemType,
							columnCount, rowCount, screenIndex, titleResId,
							icon);
				}
				++columnCount;
				continue;
			}
			
			if (columnCount == 3) {
				// 添加GO主题到桌面 add by Ryan 2013.1.6
				if (!checkHasAddRecommendApp(infos, columnCount, rowCount,
						ScreenSettingInfo.DEFAULT_MAIN_SCREEN)) {
					Intent goThemeIntent = new Intent(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME);
					ComponentName goThemeCom = new ComponentName(GOTHEMEPKGNAME,
							ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME);
					goThemeIntent.setComponent(goThemeCom);
					goThemeIntent.setData(Uri.parse("package:" + GOTHEMEPKGNAME));
					
					createItemToDesk(mContext, goThemeIntent,
							IItemType.ITEM_TYPE_APPLICATION, columnCount,
							rowCount, ScreenSettingInfo.DEFAULT_MAIN_SCREEN,
							-1, null);
					
					++columnCount;
					continue;
				}
			}

			for (int j = 0; j < dbItemsSize; j++) {
				boolean isExsit = checkHasAddRecommendApp(infos, columnCount, rowCount,
						ScreenSettingInfo.DEFAULT_MAIN_SCREEN);
				if (isExsit) {
					++columnCount;
					break;
				}
				AppItemInfo dbItemInfo = dbItemInfos.get(j);
				if (null == dbItemInfo.mIntent.getComponent()) {
					continue;
				}
				String dbPackageName = dbItemInfo.mIntent.getComponent().getPackageName();
				if (dbPackageName.equals(packageName[i])) {
					try {
						createItemToDesk(mContext, dbItemInfo.mIntent,
								dbItemInfo.mItemType, columnCount, rowCount,
								ScreenSettingInfo.DEFAULT_MAIN_SCREEN, -1, null);
						ScreenUtils.sScreenInitedDefaultAppCount++;
						++columnCount;
						break; // 添加下一个图标
					} catch (Exception e) {
					}
					break;
				}
			}
		}

		// add search widget
		/*
		 * ScreenAppWidgetInfo desktopItemInfo = new
		 * ScreenAppWidgetInfo(ICustomWidgetIds.SEARCH_WIDGET);
		 * desktopItemInfo.mInScreenId = System.currentTimeMillis();
		 * desktopItemInfo.mItemType = IItemType.ITEM_TYPE_APP_WIDGET;
		 * desktopItemInfo.mCellX = 0; desktopItemInfo.mCellY = 0;
		 * desktopItemInfo.mSpanX = 4; desktopItemInfo.mSpanY = 1; try {
		 * addDesktopItem(ScreenSettingInfo.DEFAULT_MAIN_SCREEN,
		 * desktopItemInfo); } catch (Exception e) { }
		 */
	}

	// add by huyong 2011-03-17 for 清理桌面脏数据
	/**
	 * 判断桌面是否有脏数据
	 */
	public boolean isScreenDirtyData() {
		return mDataProvider.isScreenDirtyData();
	}

	/**
	 * 清理桌面表中脏数据
	 * 
	 * @author huyong
	 */
	public void clearScreenDirtyData() {
		mDataProvider.clearScreenDirtyData();
	}

	// add by huyong end 2011-03-17 for 清理桌面脏数据

	public ArrayList<GoWidgetBaseInfo> getAllGoWidgetInfos() {
		return mDataProvider.getAllGoWidgetInfos();
	}

	/*** debug for lost icon **************************************/
	@Deprecated
	public void getScreenItems(final int index, ArrayList<ItemInfo> itemArray) {
		ArrayList<Long> screenIDList = new ArrayList<Long>();
		Cursor cursor = mDataProvider.getScreenIDList();
		convertCursorToScreenId(cursor, screenIDList);
		long screenId = screenIDList.get(index);
		getScreenItems(screenId, itemArray);
	}

	/**
	 * 
	 * @param packageName
	 *            包名
	 * @param className
	 *            启动的acitivity
	 * @param downloadAction
	 *            自定义action
	 * @param location
	 *            摆放在主屏位置
	 * @param titleId
	 *            名称资源
	 * @param iconId
	 *            icon资源
	 */
	private void addDownLoadShortCut(String packageName, String className, String downloadAction,
			int[] location, int titleId, int iconId) {
		if (null == packageName || null == className || null == downloadAction || null == location
				|| titleId <= 0 || iconId <= 0) {
			return;
		}
		ShortCutInfo itemInfo = new ShortCutInfo();
		Intent intent = new Intent(downloadAction);
		ComponentName cmpName = new ComponentName(packageName, className);
		intent.setComponent(cmpName);
		itemInfo.mIntent = intent;
		itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
		itemInfo.mTitle = mContext.getString(titleId);
		try {
			itemInfo.mIcon = mContext.getResources().getDrawable(iconId);
			itemInfo.mInScreenId = SystemClock.elapsedRealtime();
			itemInfo.mCellX = location[0];
			itemInfo.mCellY = location[1];
			itemInfo.mSpanX = 1;
			itemInfo.mSpanY = 1;
			writeToShortCutTable(itemInfo);
			addDesktopItem(ScreenSettingInfo.DEFAULT_MAIN_SCREEN, itemInfo);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		itemInfo = null;
		intent = null;
		cmpName = null;
	}

	/**
	 * 添加进快捷方式的表里
	 * 
	 * @param shortCutInfo
	 */
	public void writeToShortCutTable(ShortCutInfo shortCutInfo) {
		final SysShortCutControler shortCutControler = AppCore.getInstance()
				.getSysShortCutControler();
		if (null != shortCutControler) {
			// 添加保护，防止空指针的出现
			String itemNameString = shortCutInfo.mTitle == null ? null : shortCutInfo.mTitle
					.toString();
			BitmapDrawable itemIcon = (shortCutInfo.mIcon != null && shortCutInfo.mIcon instanceof BitmapDrawable)
					? (BitmapDrawable) shortCutInfo.mIcon
					: null;

			boolean bRet = shortCutControler.addSysShortCut(shortCutInfo.mIntent, itemNameString,
					itemIcon);
			if (!bRet) {
				Log.i(LOG_TAG, "add system shortcut is fail");
			}
		}
	}

	private ShortCutInfo addExistAppShortCut(String packageName, int[] location, int screen,
			boolean result) {
		ShortCutInfo shortCutInfo = null;
		final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
		ArrayList<AppItemInfo> dbItemInfos = dataEngine.getAllAppItemInfos();
		int dbItemsSize = dbItemInfos.size();
		for (int j = 0; j < dbItemsSize; j++) {
			AppItemInfo dbItemInfo = dbItemInfos.get(j);
			if (null == dbItemInfo.mIntent.getComponent()) {
				continue;
			}
			String dbPackageName = dbItemInfo.mIntent.getComponent().getPackageName();
			if (dbPackageName.equals(packageName)) {
				ShortCutInfo desktopItemInfo = new ShortCutInfo();
				desktopItemInfo.mInScreenId = System.currentTimeMillis();
				desktopItemInfo.mItemType = dbItemInfo.mItemType;
				desktopItemInfo.mCellX = location[0];
				desktopItemInfo.mCellY = location[1];
				desktopItemInfo.mSpanX = 1;
				desktopItemInfo.mSpanY = 1;
				desktopItemInfo.mIntent = dbItemInfo.mIntent;
				try {
					screen = screen == -1 ? ScreenSettingInfo.DEFAULT_MAIN_SCREEN : screen;
					desktopItemInfo.mScreenIndex = screen;
					addDesktopItem(screen, desktopItemInfo);
					if (result) {
						shortCutInfo = desktopItemInfo;
					}
					break; // 添加下一个图标
				} catch (Exception e) {
				}
				break;
			}
		}
		return shortCutInfo;
	}

	/**
	 * 
	 * @param packageName 包名
	 * @param className 启动的acitivity
	 * @param downloadAction 自定义action
	 * @param location 摆放在主屏位置
	 * @param titleId 名称资源
	 * @param iconId icon资源
	 * @param screen 添加到的指定屏幕,如果指定屏幕为-1，则默认添加到主屏幕
	 * @param result 是否需要返回结果
	 */
	private ShortCutInfo addDownLoadShortCut(String packageName, String downloadAction,
			int[] location, int titleid, int iconId, int screen, boolean result) {

		if (null == packageName || null == downloadAction || null == location || titleid <= 0
				|| iconId <= 0) {
			return null;
		}
		ShortCutInfo itemInfo = new ShortCutInfo();
		Intent intent = new Intent(downloadAction);
		ComponentName cmpName = new ComponentName(packageName, packageName);
		intent.setComponent(cmpName);
		itemInfo.mIntent = intent;
		itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
		itemInfo.mTitle = mContext.getString(titleid);
		try {
			itemInfo.mIcon = mContext.getResources().getDrawable(iconId);
			itemInfo.mInScreenId = System.currentTimeMillis();
			itemInfo.mCellX = location[0];
			itemInfo.mCellY = location[1];
			itemInfo.mSpanX = 1;
			itemInfo.mSpanY = 1;
			writeToShortCutTable(itemInfo);
			// 如果指定屏幕为-1，则默认添加到主屏幕
			screen = screen == -1 ? ScreenSettingInfo.DEFAULT_MAIN_SCREEN : screen;
			itemInfo.mScreenIndex = screen;
			addDesktopItem(screen, itemInfo);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!result) {
			itemInfo = null;
		}
		intent = null;
		cmpName = null;
		return itemInfo;
	}
	/**
	 * 添加推荐app,每个位置可能有几个候选，选第一个未安装的做为推荐
	 * @param screen -1表示为主屏幕
	 * @param cellX  -1表示直接读取 info.mGroup
	 * @param cellY  -1表示直接读取 info.RowIndex
	 * @param result 是否返回 iteminfo
	 */
	private ArrayList<ShortCutInfo> addRecommendedApp(int screen, int cellX, int cellY,
			boolean result) {
		HashMap<Integer, ArrayList<XmlRecommendedAppInfo>> map = XmlRecommendedApp
				.getRecommendedAppMap();
		ShortCutInfo shortCutInfo = null;
		String uid = GoStorePhoneStateUtil.getUid(mContext);
		ArrayList<ShortCutInfo> shortCutInfos = new ArrayList<ShortCutInfo>();
		if (map != null) {
			ArrayList<XmlRecommendedAppInfo> list = null;
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				list = (ArrayList<XmlRecommendedAppInfo>) entry.getValue();
				if (list != null) {
					XmlRecommendedAppInfo installedInfo = null;
					int size = list.size();
					Date date = Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).getTime();
					SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					String today = sf.format(date);
					//保存已加的row信息
					Set<Integer> rows = new HashSet<Integer>();
					for (int j = 0; j < size; j++) {
						XmlRecommendedAppInfo info = list.get(j);
						if ((info.mSTime != null && today.compareTo(info.mSTime) < 0)
								|| (info.mETime != null && today.compareTo(info.mETime) > 0)) {
							continue;
						}
						if ((info.mChannelId == null || (info.mChannelId != null && info.mChannelId
								.equals(uid))) && null != info.mPackagename) {
							// 判断该行是否已添加了推荐应用
							if (rows.contains(info.mRowIndex)) {
								continue;
							} else {
								rows.add(info.mRowIndex);
							}
							
							if (!AppUtils.isAppExist(mContext, info.mPackagename)) {
								if (info.mPackagename.equals("com.UCMobile")
										&& AppUtils.isAppExist(mContext, "com.uc.browser")) {
									info.mPackagename = "com.uc.browser";
									installedInfo = info;
									continue;
								}
								
								int s = screen >= 0 ? screen : info.mScreenIndex;
								int x = cellX == -1 ? info.mGroup : cellX;
								int y = cellY == -1 ? info.mRowIndex : cellY;
								shortCutInfo = addDownLoadShortCut(info.mPackagename, info.mAction,
										new int[] { x, y }, info.mTitle, info.mIconId, s, result);
								shortCutInfos.add(shortCutInfo);
								continue;
								
							} else {
								installedInfo = info;
								if (installedInfo != null && installedInfo.mShowInstallIcon) {
									int s = screen >= 0 ? screen : installedInfo.mScreenIndex;
									int x = cellX == -1 ? installedInfo.mGroup : cellX;
									int y = cellY == -1 ? installedInfo.mRowIndex : cellY;
									shortCutInfo = addExistAppShortCut(installedInfo.mPackagename, new int[] {
											x, y }, s, result);
									shortCutInfos.add(shortCutInfo);
									installedInfo = null;;
								}
							}
						}
					}
				}
			}
		}
		return shortCutInfos;
	}
	/**
	 * 添加GO应用到桌面文件夹
	 */
	private void addGoAppsToDeskFolder(UserFolderInfo userFolderInfo) {
		PackageManager pm = mContext.getPackageManager();
		String[] goAppsPkg = ScreenUtils.getGoAppsPkgName(); // GO系列应用的包名数组
		int[] goAppsNameIds = ScreenUtils.getGoAppsNameIds(); // GO系列应用的程序名称的id数组
		int[] goAppsIconIds = ScreenUtils.getGoAppsIconIds(); // GO系列应用的图标id数组
		String[] goAppsActions = ScreenUtils.getGoAppsActions(); // GO系列应用的Action数组
		String[] goAppsFtpUrls = ScreenUtils.getGoAppsFtpUrl(); // GO系列应用的FTP下载地址数组
		int itemX = 0;
		int itemY = 0;
		if (goAppsPkg.length == goAppsNameIds.length) {
			ArrayList<ShortCutInfo> installList = new ArrayList<ShortCutInfo>(); // 保存已安装的程序
			ArrayList<ShortCutInfo> recommandList = new ArrayList<ShortCutInfo>(); // 保存未安装的推荐程序
			for (int k = 0; k < goAppsPkg.length; k++) {
				ShortCutInfo itemInfo = new ShortCutInfo();
				itemInfo.mInScreenId = System.currentTimeMillis() + k;
				itemInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
				itemInfo.mCellX = itemX;
				itemInfo.mCellY = itemY;
				itemInfo.mSpanX = 1;
				itemInfo.mSpanY = 1;

				if (goAppsPkg[k].equals(GOSTOREPKGNAME)) {
					Intent goStoreIntent = new Intent(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE);
					itemInfo.mIntent = goStoreIntent;
					ComponentName goStoreCom = new ComponentName(GOSTOREPKGNAME,
							ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE);
					itemInfo.mIntent.setComponent(goStoreCom);
					itemInfo.mIntent.setData(Uri.parse("package:" + GOSTOREPKGNAME));
					installList.add(itemInfo);
				} else if (goAppsPkg[k].equals(GOWIDGETPKGNAME)) {
					Intent goWidgetIntent = new Intent(
							ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET);
					itemInfo.mIntent = goWidgetIntent;
					ComponentName goWidgetCom = new ComponentName(GOWIDGETPKGNAME,
							ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET);
					itemInfo.mIntent.setComponent(goWidgetCom);
					itemInfo.mIntent.setData(Uri.parse("package:" + GOWIDGETPKGNAME));
					installList.add(itemInfo);
				} else if (goAppsPkg[k].equals(GOTHEMEPKGNAME)) {
					Intent goThemeIntent = new Intent(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME);
					itemInfo.mIntent = goThemeIntent;
					ComponentName goThemeCom = new ComponentName(GOTHEMEPKGNAME,
							ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME);
					itemInfo.mIntent.setComponent(goThemeCom);
					itemInfo.mIntent.setData(Uri.parse("package:" + GOTHEMEPKGNAME));
					installList.add(itemInfo);
				} else {
					if (AppUtils.isAppExist(mContext, goAppsPkg[k])) {
						if (goAppsPkg[k].equals(LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE)
								&& (AppUtils.getVersionCodeByPkgName(mContext, goAppsPkg[k]) < 10)) {
							// 如果是GO天气，则需要判断其版本号是否低于10，低于10的版本为widget版本，无法获取图标，则换成推荐图
							Intent downloadIntent = new Intent(goAppsActions[k]);
							ComponentName cmpName = new ComponentName(goAppsPkg[k], goAppsPkg[k]);
							downloadIntent.setComponent(cmpName);
							downloadIntent.putExtra(RECOMMAND_APP_FTP_URL_KEY, goAppsFtpUrls[k]);
							itemInfo.mIntent = downloadIntent;
							itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
							itemInfo.mTitle = mContext.getString(goAppsNameIds[k]);
							try {
								Drawable drawable = getGoAppsIcons(goAppsIconIds[k]);
								itemInfo.mIcon = drawable;
								writeToShortCutTable(itemInfo);
							} catch (OutOfMemoryError e) {
								OutOfMemoryHandler.handle();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							itemInfo.mIntent = pm.getLaunchIntentForPackage(goAppsPkg[k]);
						}
						installList.add(itemInfo);
					} else if ((LauncherEnv.Plugin.LOCKER_PACKAGE.equals(goAppsPkg[k])) && AppUtils.isGoLockerExist(mContext)) {
						Intent golockerIntent = new Intent(ICustomAction.ACTION_LOCKER);
						List<ResolveInfo> infosList = null;
						infosList = mContext.getPackageManager().queryIntentActivities(
								golockerIntent, 0);
						if (infosList != null && infosList.size() > 0) {
							ResolveInfo resolveInfo = infosList.get(0);
							String pkg = resolveInfo.activityInfo.packageName;
							itemInfo.mIntent = pm.getLaunchIntentForPackage(pkg);
							installList.add(itemInfo);
						}
					} else {
						Intent downloadIntent = new Intent(goAppsActions[k]);
						ComponentName cmpName = new ComponentName(goAppsPkg[k], goAppsPkg[k]);
						downloadIntent.setComponent(cmpName);
						downloadIntent.putExtra(RECOMMAND_APP_FTP_URL_KEY, goAppsFtpUrls[k]);
						itemInfo.mIntent = downloadIntent;
						itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
						itemInfo.mTitle = mContext.getString(goAppsNameIds[k]);
						try {
							// 加download Tag
							Drawable drawable = getGoAppsIcons(goAppsIconIds[k]);
							itemInfo.mIcon = drawable;
							writeToShortCutTable(itemInfo);
						} catch (OutOfMemoryError e) {
							OutOfMemoryHandler.handle();
						} catch (Exception e) {
							e.printStackTrace();
						}
						recommandList.add(itemInfo);
					}
				}
			}
			try {
				// 添加未安装的程序
				for (ShortCutInfo recommandItem : recommandList) {
					addItemToFolder(recommandItem, userFolderInfo.mInScreenId);
					if (itemX >= 4) {
						itemX = 0;
						++itemY;
					} else {
						++itemX;
					}
				}

				// 添加已安装的程序
				for (ShortCutInfo installItem : installList) {
					addItemToFolder(installItem, userFolderInfo.mInScreenId);
					if (itemX >= 4) {
						itemX = 0;
						++itemY;
					} else {
						++itemX;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:通过drawableId拿推荐图标图片 <br>
	 * 功能详细描述:可以过滤某些图标进行download tag标签合成图片（tag图片共享一张，减少图片资源） <br>
	 * 注意:
	 * 
	 * @param drawableId
	 * @return　经过合成规则处理后的图片
	 */
	private Drawable getGoAppsIcons(int drawableId) {
		Drawable drawable = mContext.getResources().getDrawable(drawableId);

		if (drawableId == R.drawable.goweatherex_4_def3 || drawableId == R.drawable.gosmspro_4_def3
				|| drawableId == R.drawable.screen_edit_golocker
				|| drawableId == R.drawable.lock_screen) {
			Drawable tag = mContext.getResources().getDrawable(R.drawable.recommand_icon_tag);
			try {
				int width = drawable.getIntrinsicWidth();
				int height = drawable.getIntrinsicHeight();
				Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				Canvas cv = new Canvas(bmp);
				ImageUtil.drawImage(cv, drawable, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
				ImageUtil.drawImage(cv, tag, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
				BitmapDrawable bmd = new BitmapDrawable(bmp);
				drawable = bmd;
			} catch (Throwable e) {
				// 出错则不进行download Tag合成图
			}
		}

		return drawable;
	}

	
	/**
	 * <br>功能简述:检查屏幕某个位置是否已被推荐图标占据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param recommedApps
	 * @param cellx
	 * @param celly
	 * @return
	 */
	boolean checkHasAddRecommendApp(ArrayList<ShortCutInfo> recommedApps, int cellx, int celly,
			int screen) {
		try {
			if (recommedApps != null && !recommedApps.isEmpty()) {
				for (ShortCutInfo info : recommedApps) {
					if (info.mCellX == cellx && info.mCellY == celly && info.mScreenIndex == screen) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
}
