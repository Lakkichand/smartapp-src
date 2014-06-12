package com.jiubang.ggheart.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.go.util.ConvertUtils;
import com.go.util.DbUtil;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureTable;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.statistics.StatisticsAppsInfoData;
import com.jiubang.ggheart.data.tables.AppFuncSettingTable;
import com.jiubang.ggheart.data.tables.AppHideListTable;
import com.jiubang.ggheart.data.tables.AppSettingTable;
import com.jiubang.ggheart.data.tables.AppTable;
import com.jiubang.ggheart.data.tables.AppWhiteListTable;
import com.jiubang.ggheart.data.tables.ConfigTable;
import com.jiubang.ggheart.data.tables.DeskMenuTable;
import com.jiubang.ggheart.data.tables.DesktopTable;
import com.jiubang.ggheart.data.tables.DynamicEffectTable;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.data.tables.FontTable;
import com.jiubang.ggheart.data.tables.GestureTable;
import com.jiubang.ggheart.data.tables.GoWidgetTable;
import com.jiubang.ggheart.data.tables.GravityTable;
import com.jiubang.ggheart.data.tables.MediaManagementHideTable;
import com.jiubang.ggheart.data.tables.MediaManagementPlayListFileTable;
import com.jiubang.ggheart.data.tables.MediaManagementPlayListTable;
import com.jiubang.ggheart.data.tables.MessageCenterTable;
import com.jiubang.ggheart.data.tables.NotificationAppSettingTable;
import com.jiubang.ggheart.data.tables.PartToFolderTable;
import com.jiubang.ggheart.data.tables.PartToScreenTable;
import com.jiubang.ggheart.data.tables.PartsTable;
import com.jiubang.ggheart.data.tables.RecentAppTable;
import com.jiubang.ggheart.data.tables.ScreenSettingTable;
import com.jiubang.ggheart.data.tables.ScreenStyleConfigTable;
import com.jiubang.ggheart.data.tables.ScreenTable;
import com.jiubang.ggheart.data.tables.SettingTable;
import com.jiubang.ggheart.data.tables.ShortcutSettingTable;
import com.jiubang.ggheart.data.tables.ShortcutTable;
import com.jiubang.ggheart.data.tables.ShortcutUnfitTable;
import com.jiubang.ggheart.data.tables.StatisticsAppDataTable;
import com.jiubang.ggheart.data.tables.StatisticsTable;
import com.jiubang.ggheart.data.tables.ThemeTable;
import com.jiubang.ggheart.data.tables.UsedFontTable;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 用于提供数据以及对数据的具体处理方法
 * @author HuYong
 * @version 1.0
 */

//TODO:封装、提供接口
/**
 * 数据持久化具体操作
 */
public class DataProvider {

	private DatabaseHelper mDBOpenHelper;
	private static boolean sNeedReplaceWidget = true;
	private static boolean sHasReplaceAllGoStoreWidget = false;

	private int mCurDBVersion;
	private String mDBName;
	private Context mContext;
	static private DataProvider sDataProviderSelf;

	public static final Object DB_LOCK = new Object();

	static public synchronized final DataProvider getInstance(Context context) {
		if (null == sDataProviderSelf) {
			sDataProviderSelf = new DataProvider(context);
		}
		return sDataProviderSelf;
	}

	/**
	 * 
	 * @param context
	 */
	private DataProvider(Context context) {
		mContext = context;
		mCurDBVersion = DatabaseHelper.getDB_CUR_VERSION();
		mDBName = DatabaseHelper.getDBName();
		mDBOpenHelper = new DatabaseHelper(context, mDBName, mCurDBVersion);
		checkSharedPreferences();
	}

	private void checkSharedPreferences() {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		sNeedReplaceWidget = sharedPreferences.getBoolean(LauncherEnv.ALREADY_REPLACE_OLD_WIDGET,
				true);
		sHasReplaceAllGoStoreWidget = sharedPreferences.getBoolean(
				LauncherEnv.ALREADY_REPLACE_ALL_GOSTORE_WIDGET, false);
	}

	// NOTE:Just for test
	public boolean isNewDB() {
		// 装载默认配置信息
		return mDBOpenHelper.isNewDB();
	}

	public int getRecentAppItemsCount() {
		String table = RecentAppTable.TABLENAME;
		Cursor cursor = null;
		int count = 0;
		try {
			cursor = mDBOpenHelper.query(table, null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				// cursor.close();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}

	/**
	 * 获取shortcut表中所有字段，不管parts表中有无相关数据
	 * 
	 * @author huyong
	 * @return
	 */
	public Cursor getAllShortCutItems(String themeName) {
		String table = ShortcutTable.TABLENAME;
		String selection = ShortcutTable.THEMENAME + " = " + "'" + themeName + "'";
		String orderby = ShortcutTable.MINDEX + " ASC";
		return mDBOpenHelper.query(table, null, selection, null, orderby);
	}

	/**
	 * 获取shortcut表中指定rowid的所有字段
	 * 
	 * @param rowid
	 * @param themeName
	 * @return
	 */
	public Cursor getRowShortCutItems(int rowid, String themeName) {
		String table = ShortcutTable.TABLENAME;
		String selection = null;
		if (themeName == null) {
			selection = ShortcutTable.ROWSID + " = " + rowid;
		} else {
			selection = ShortcutTable.ROWSID + " = " + rowid + " and " + ShortcutTable.THEMENAME
					+ " = " + "'" + themeName + "'";
		}
		String orderby = ShortcutTable.MINDEX + " ASC";
		return mDBOpenHelper.query(table, null, selection, null, orderby);
	}

	/**
	 * 获取shortcutunfit表中指定rowid的空白项
	 * 
	 * @param rowid
	 * @return
	 */
	public Cursor getRowShortCutUnfitBlank(int rowid) {
		String table = ShortcutUnfitTable.TABLENAME;
		String selection = ShortcutUnfitTable.ROWID + " = " + rowid + " AND "
				+ ShortcutUnfitTable.INTENT + " = " + "'" + ICustomAction.ACTION_BLANK + "'";
		String orderby = ShortcutUnfitTable.INDEX + " ASC";
		return mDBOpenHelper.query(table, null, selection, null, orderby);
	}

	/**
	 * 获取文件夹项
	 * 
	 * @param folderId
	 *            文件夹ID
	 * @return 数据集
	 */
	public Cursor getFolderItems(final long folderId) {
		// 查询表名
		String tables = PartToFolderTable.TABLENAME + ", " + PartsTable.TABLENAME;
		// 查询列数
		String columns[] = {
				// PartToFolderTable.MINDEX,
				PartsTable.TABLENAME + "." + PartsTable.ID, PartsTable.TITLE, PartsTable.INTENT,
				PartsTable.TABLENAME + "." + PartsTable.ITEMTYPE, PartsTable.WIDGETID,
				PartsTable.ICONPACKAGE, PartsTable.ICONRESOURCE, PartsTable.ICON, PartsTable.URI,
				PartsTable.DISPLAYMODE };
		// 查询条件
		String condition = PartToFolderTable.TABLENAME + "." + PartToFolderTable.FOLDERID + " = "
				+ folderId + " and " + PartToFolderTable.TABLENAME + "." + PartToFolderTable.PARTID
				+ " = " + PartsTable.TABLENAME + "." + PartsTable.ID;
		String orderby = PartToFolderTable.MINDEX + " ASC";

		// 查询
		return mDBOpenHelper.queryCrossTables(tables, columns, condition, null, orderby);
	}

	public long getScreenIDByIndex(final int screenIndex) {
		String table = ScreenTable.TABLENAME;
		String columns[] = { ScreenTable.SCREENID };
		String selection = ScreenTable.MINDEX + " = " + screenIndex;
		Cursor cursor = mDBOpenHelper.query(table, columns, selection, null, null);
		long screenId = -1;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					screenId = cursor.getLong(cursor.getColumnIndex(ScreenTable.SCREENID));
				}
			} finally {
				cursor.close();
			}
		}
		return screenId;
	}

	/**
	 * 通过屏幕screenId查找屏幕索引
	 * 
	 * @param screenId
	 * @return
	 */
	public int getScreenIndexById(final long screenId) {
		String table = ScreenTable.TABLENAME;
		String columns[] = { ScreenTable.MINDEX };
		String selection = ScreenTable.SCREENID + " = " + screenId;
		Cursor cursor = mDBOpenHelper.query(table, columns, selection, null, null);
		int screenIndex = -1;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					screenIndex = cursor.getInt(cursor.getColumnIndex(ScreenTable.MINDEX));
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				cursor.close();
			}
		}
		return screenIndex;
	}

	/**
	 * 返回屏幕信息
	 * 
	 * @return
	 */
	public Cursor getScreenIDList() {
		String table = ScreenTable.TABLENAME;
		String orderby = ScreenTable.MINDEX + " ASC";
		return mDBOpenHelper.query(table, null, null, null, orderby);

	}

	/**
	 * 获取所有组件信息
	 * 
	 * @author huyong
	 * @param itemInfos
	 */
	public Cursor getAllItems() {
		return mDBOpenHelper.query(PartsTable.TABLENAME, null, null, null, null);
		// convertCursorToItem(cursor, itemInfos);
	}

	/**
	 * 功能简述:屏幕上是否存在某个元素
	 * 功能详细描述:
	 * 注意:
	 * @param itemIntent
	 * @return
	 */
	public boolean isExistItemInScreen(final String itemIntent) {
		// 表名
		String table = PartToScreenTable.TABLENAME;
		// 返回一列数据，增加查询效率。
		String columns[] = { PartToScreenTable.ID };
		// 查询条件
		String selection = PartToScreenTable.INTENT + " = '" + itemIntent + "'";
		boolean isExist = false;
		try {
			Cursor cursor = mDBOpenHelper.query(table, columns, selection, null, null);
			if (cursor != null) {
				try {
					isExist = cursor.moveToFirst();
				} finally {
					cursor.close();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return isExist;
	}

	/**
	 * 根据intent和widgetid来判断该数据项是否已存在
	 * 
	 * @author huyong
	 * @param itemIntent
	 * @param itemWidgetId
	 * @return 查找到的itemId，不存在则为-1
	 */
	public long isItemInTable(final String itemIntent, final int itemWidgetId) {
		// 表名
		String table = PartsTable.TABLENAME;
		// 返回一列数据，增加查询效率。
		String columns[] = { PartsTable.ID };
		// 查询条件
		String selection = PartsTable.INTENT + " = '" + itemIntent + "' and " + PartsTable.WIDGETID
				+ " = " + itemWidgetId;
		Cursor cursor = mDBOpenHelper.query(table, columns, selection, null, null);
		long itemId = -1;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					itemId = cursor.getLong(cursor.getColumnIndex(PartsTable.ID));
				}
			} finally {
				cursor.close();
			}
		}
		return itemId;
	}

	/**
	 * 根据itemid来判断该数据项是否已存在
	 * 
	 * @author huyong
	 * @param itemIntent
	 * @param itemWidgetId
	 * @return
	 */
	public boolean isItemInTable(final Long itemId) {
		boolean result = false;
		String table = PartsTable.TABLENAME;
		String columns[] = { PartsTable.ID };
		// 查询条件
		String selection = PartsTable.ID + " = " + itemId;
		Cursor cursor = mDBOpenHelper.query(table, columns, selection, null, null);
		if (cursor != null) {
			try {
				result = cursor.moveToFirst();
			} finally {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 获取功能表根目录元素个数
	 * 
	 * @return 个数
	 */
	public int getSizeOfFunAppItem() {
		int size = -1;
		Cursor cursor = getFunAppItems();
		// 如果数据库查询成功
		if (cursor != null) {
			try {
				size = cursor.getCount();
			} finally {
				cursor.close();
			}
		}
		return size;
	}

	/**
	 * 获取功能表元素数据
	 * 
	 * @return
	 */
	public Cursor getFunAppItems() {
		// 表名
		String tables = AppTable.TABLENAME;
		// 查询列数
		String columns[] = { AppTable.INDEX, AppTable.INTENT, AppTable.FOLDERID, AppTable.TITLE,
				AppTable.FOLDERICONPATH };

		// 排序条件
		String sortOrder = AppTable.INDEX + " ASC";

		return mDBOpenHelper.query(tables, columns, null, null, sortOrder);
	}

	/**
	 * 获取文件夹列表
	 * 
	 * @return
	 */
	public Cursor getFolderIds() {
		// 表名
		String tables = AppTable.TABLENAME;
		// 查询列数
		String columns[] = { AppTable.FOLDERID };

		String where = FolderTable.FOLDERID + " != " + 0;
		return mDBOpenHelper.query(tables, columns, where, null, null);
	}

	/**
	 * 获取功能表元素数据
	 * 
	 * @return
	 */
	public Cursor getRecentAppItems() {
		// 表名
		String tables = RecentAppTable.TABLENAME;
		// 查询列数
		String columns[] = { RecentAppTable.INDEX, RecentAppTable.INTENT };

		// 排序条件
		String sortOrder = RecentAppTable.INDEX + " ASC";

		return mDBOpenHelper.query(tables, columns, null, null, sortOrder);
	}

	/**
	 * 元素是否在文件夹中
	 * 
	 * @param intent
	 * @return
	 */
	public boolean isInFolders(final long folderId, final Intent intent) {
		// 表名
		String tables = FolderTable.TABLENAME;
		// 查询列数
		String columns[] = { FolderTable.INTENT, FolderTable.INDEX };

		String where = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.INTENT
				+ " = " + "'" + ConvertUtils.intentToString(intent) + "'";

		Cursor cursor = mDBOpenHelper.query(tables, columns, where, null, null);
		int count = 0;
		if (null != cursor) {
			try {
				count = cursor.getCount();
			} finally {
				cursor.close();
			}
		}
		return 0 != count;
	}

	/**
	 * 取得文件夹中的特定元素
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param index
	 *            下标
	 * @return
	 */
	public Cursor getAppInFolder(final long folderId, final int index) {
		// 表名
		String tables = FolderTable.TABLENAME;
		// 查询列数
		String columns[] = { FolderTable.INTENT, FolderTable.INDEX };

		// 排序条件
		String where = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.INDEX
				+ " = " + index;

		return mDBOpenHelper.query(tables, columns, where, null, null);
	}

	public Cursor getFunAppItems(final Intent intent) {
		if (null == intent) {
			return null;
		}

		// 表名
		String tables = AppTable.TABLENAME;
		// 查询列数
		String columns[] = { AppTable.INDEX };

		// 排序条件
		String where = AppTable.INTENT + " = " + "'" + ConvertUtils.intentToString(intent) + "'";

		return mDBOpenHelper.query(tables, columns, where, null, null);
	}

	public Cursor getAppInFolder(final long folderId, final Intent intent) {
		if (null == intent) {
			return null;
		}

		// 表名
		String tables = FolderTable.TABLENAME;
		// 查询列数
		String columns[] = { FolderTable.INTENT, FolderTable.INDEX };

		// 排序条件
		String where = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.INTENT
				+ " = " + "'" + ConvertUtils.intentToString(intent) + "'";

		return mDBOpenHelper.query(tables, columns, where, null, null);
	}

	/**
	 * 取得文件夹中的元素
	 * 
	 * @param folderId
	 *            文件夹id
	 * @return 游标
	 */
	public Cursor getAppsInFolder(final long folderId) {
		// 表名
		String tables = FolderTable.TABLENAME;
		// 查询列数
		String columns[] = { FolderTable.INTENT, FolderTable.INDEX, FolderTable.USERTITLE,
				FolderTable.TIMEINFOLDER };

		// 排序条件
		String sortOrder = FolderTable.INDEX + " ASC";
		String where = FolderTable.FOLDERID + " = " + folderId;

		for (int i = 0; i < 2; i++) {
			Cursor cursor = mDBOpenHelper.query(tables, columns, where, null, sortOrder);
			if (cursor != null && cursor.moveToFirst()) {
				return cursor;
			} else {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return mDBOpenHelper.query(tables, columns, where, null, sortOrder);

	}

	public Cursor getAllAppItems() {
		String tables = AppTable.TABLENAME + ", " + PartsTable.TABLENAME;
		// 查询列数
		String columns[] = { AppTable.INDEX, AppTable.ISSYSAPP,
				PartsTable.TABLENAME + "." + PartsTable.ID, PartsTable.TITLE, PartsTable.INTENT,
				PartsTable.ITEMTYPE, PartsTable.WIDGETID, PartsTable.ICONPACKAGE,
				PartsTable.ICONRESOURCE, PartsTable.ICON, PartsTable.URI, PartsTable.DISPLAYMODE };
		// 查询条件
		String condition = AppTable.TABLENAME + "." + AppTable.PARTID + " = "
				+ PartsTable.TABLENAME + "." + PartsTable.ID;
		// 排序条件
		String sortOrder = AppTable.INDEX + " ASC";
		return mDBOpenHelper.queryCrossTables(tables, columns, condition, null, sortOrder);
	}

	/**
	 * 获取所有桌面组件信息
	 * 
	 * @author huyong
	 * @param desktopItemInfos
	 */
	public Cursor getScreenItems(final long screenId) {

		String tables = PartToScreenTable.TABLENAME;
		// 查询列数
		String columns[] = { PartToScreenTable.ID, PartToScreenTable.SCREENID,
				PartToScreenTable.PARTID, PartToScreenTable.SCREENX,
				PartToScreenTable.SCREENY,
				PartToScreenTable.SPANX,
				PartToScreenTable.SPANY,
				PartToScreenTable.USERTITLE,
				// PartToScreenTable.USERICON,
				PartToScreenTable.ITEMTYPE,
				PartToScreenTable.WIDGETID,
				PartToScreenTable.INTENT,
				PartToScreenTable.URI,
				// PartToScreenTable.FOLDERID,
				// PartToScreenTable.FOLDERINDEX,
				PartToScreenTable.USERICONTYPE, PartToScreenTable.USERICONID,
				PartToScreenTable.USERICONPACKAGE, PartToScreenTable.USERICONPATH,
				PartToScreenTable.SORTTYPE };
		// 查询条件
		// TODO:需要修改
		// String condition = PartToScreenTable.SCREENID + " = " + screenId +
		// " and " + PartToScreenTable.FOLDERID + " <= 0";
		String condition = PartToScreenTable.SCREENID + " = " + screenId;

		// 查询
		return mDBOpenHelper.query(tables, columns, condition, null, null);
	}

	/**
	 * 通过component在PartToScreenTable中查询桌面元素的ScreenID
	 * 
	 * @param component
	 * @return ScreenID
	 */
	public long getScreenIDByComponent(String component) {
		String table = PartToScreenTable.TABLENAME;
		String columns[] = { PartToScreenTable.ID };
		String selection = PartToScreenTable.INTENT + " LIKE '%" + component + "%'";
		Cursor cursor = mDBOpenHelper.query(table, columns, selection, null, null);
		long screenId = -1;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					screenId = cursor.getLong(0);
				}
			} finally {
				cursor.close();
			}
		}
		return screenId;
	}

	/**
	 * 文件夹内部移动
	 * 
	 * @param folderId
	 * @param srcIndex
	 * @param desIndex
	 */
	public boolean moveFolderItem(long folderId, int srcIndex, int desIndex) {
		boolean result = false;
		String add = srcIndex > desIndex ? " + 1 " : " - 1 ";
		String comp1 = srcIndex > desIndex ? " >= " : " <= ";
		String comp2 = srcIndex > desIndex ? " < " : " > ";

		String table = FolderTable.TABLENAME;
		String mindex = FolderTable.INDEX;
		String addCondition = " and " + FolderTable.FOLDERID + " = " + folderId;

		mDBOpenHelper.beginTransaction();
		try {
			String sql = "update " + table + " set " + mindex + " = " + " -1" + " where " + mindex
					+ " = " + srcIndex + addCondition + ";";
			// 先更新成-1，方便查找更新。
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + mindex + " = " + mindex + add + " where " + mindex
					+ comp1 + desIndex + " and " + mindex + comp2 + srcIndex + addCondition + ";";
			mDBOpenHelper.exec(sql);
			sql = " update " + table + " set " + mindex + " = " + desIndex + " where " + mindex
					+ " = " + " -1" + addCondition + ";";
			mDBOpenHelper.exec(sql);

			mDBOpenHelper.setTransactionSuccessful();
			result = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result = false;
		} finally {
			mDBOpenHelper.endTransaction();
		}
		return result;
	}

	/**
	 * 遍历文件夹得所有项
	 * 
	 * @param folderId
	 * @return
	 */
	public Cursor getScreenFolderItems(long folderId) {
		String condition = FolderTable.FOLDERID + " = " + folderId;
		String orderby = FolderTable.INDEX + " ASC";

		return mDBOpenHelper.query(FolderTable.TABLENAME, null, condition, null, orderby);
	}

	public Cursor getScreenFolderItem(long folderId, long itemScreenId) {
		String condition = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.ID
				+ " = " + itemScreenId;
		String orderby = FolderTable.INDEX + " ASC";

		return mDBOpenHelper.query(FolderTable.TABLENAME, null, condition, null, orderby);
	}

	/**
	 * 删除文件夹所有项
	 * 
	 * @param folderId
	 */
	public void delScreenFolderItems(long folderId) {
		String condition = FolderTable.FOLDERID + " = " + folderId;

		try {
			mDBOpenHelper.delete(FolderTable.TABLENAME, condition, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取屏幕的表项
	 * 
	 * @param screenItemId
	 * @return
	 */
	public Cursor getScreenItem(long screenItemId) {
		String condition = PartToScreenTable.ID + " = " + screenItemId;
		return mDBOpenHelper.query(PartToScreenTable.TABLENAME, null, condition, null, null);
	}

	/**
	 * 通过GowidgetID查找添加到的那个屏幕的screenId
	 * 
	 * @param widgetId
	 * @return
	 */
	public long getScreenIdByGowidgetId(int widgetId) {
		String table = PartToScreenTable.TABLENAME;
		String columns[] = { PartToScreenTable.SCREENID };
		String selection = PartToScreenTable.WIDGETID + " = " + widgetId;
		Cursor cursor = mDBOpenHelper.query(table, columns, selection, null, null);
		long screenId = -1;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					screenId = cursor.getLong(cursor.getColumnIndex(PartToScreenTable.SCREENID));
				}
			} catch (Exception e) {
			} finally {
				cursor.close();
			}
		}
		return screenId;
	}

	// TODO 删除文件夹

	/**
	 * 添加桌面组件
	 * 
	 * @author huyong
	 * @param values
	 */
	public void addItemToScreen(ContentValues values) {
		try {
			mDBOpenHelper.insert(PartToScreenTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 新增一个item项
	 * 
	 * @author huyong
	 * @param values
	 */
	public void addItem(ContentValues values) {
		// TODO:通过判断增加的项的intent与widgetid，是否已存在。

		try {
			mDBOpenHelper.insert(PartsTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 新增一个屏幕
	 * 
	 * @author huyong
	 * @param values
	 */
	public void addScreen(ContentValues values) {
		try {
			mDBOpenHelper.insert(ScreenTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
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
		String add = srcScreenIndex > DescScreenIndex ? " + 1 " : " - 1 ";
		String comp1 = srcScreenIndex > DescScreenIndex ? " >= " : " <= ";
		String comp2 = srcScreenIndex > DescScreenIndex ? " < " : " > ";

		String table = ScreenTable.TABLENAME;
		String mindex = ScreenTable.MINDEX;
		String sql = "update " + table + " set " + mindex + " = " + " -1" + " where " + mindex
				+ " = " + srcScreenIndex + ";";

		mDBOpenHelper.beginTransaction();
		try {
			// 先更新成-1，方便查找更新。
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + mindex + " = " + mindex + add + " where " + mindex
					+ comp1 + DescScreenIndex + " and " + mindex + comp2 + srcScreenIndex + ";";
			mDBOpenHelper.exec(sql);
			sql = " update " + table + " set " + mindex + " = " + DescScreenIndex + " where "
					+ mindex + " = " + " -1" + ";";
			mDBOpenHelper.exec(sql);

			mDBOpenHelper.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	/**
	 * 更新屏幕后的索引值(仅仅更新屏幕索引值)
	 * 
	 * @author huyong
	 * @param screenIndex
	 *            要更新的屏幕索引位置
	 * @param isAddIndex
	 *            是否是增加屏幕索引
	 */
	public void updateScreenIndex(final int screenIndex, final boolean isAddIndex) {
		String table = ScreenTable.TABLENAME;
		String mindex = ScreenTable.MINDEX;
		String changeValue = null;
		if (isAddIndex) {
			changeValue = " + 1";
		} else {
			changeValue = " - 1";
		}
		String sql = "update " + table + " set " + mindex + " = " + mindex + changeValue
				+ " where " + mindex + " >= " + screenIndex + ";";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 移除指定屏幕
	 * 
	 * @author huyong
	 * @param screenIndex
	 */
	public void removeScreen(final int screenIndex) {
		// 级联更新操作
		// 1. 删除PartToScreen表
		// 2. 删除Screen表
		// 3. 更新表中index字段
		mDBOpenHelper.beginTransaction();
		try {
			long screenId = getScreenIDByIndex(screenIndex);
			String whereStr = PartToScreenTable.SCREENID + " = " + screenId;
			mDBOpenHelper.delete(PartToScreenTable.TABLENAME, whereStr, null);

			String table = ScreenTable.TABLENAME;
			whereStr = ScreenTable.MINDEX + " = " + screenIndex;
			mDBOpenHelper.delete(table, whereStr, null);

			String mindex = ScreenTable.MINDEX;
			String changeValue = " - 1";
			String sql = "update " + table + " set " + mindex + " = " + mindex + changeValue
					+ " where " + mindex + " >= " + screenIndex + ";";
			mDBOpenHelper.exec(sql);

			mDBOpenHelper.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	public void removeScreenContent(final int screenIndex) {
		long screenId = getScreenIDByIndex(screenIndex);
		String whereStr = PartToScreenTable.SCREENID + " = " + screenId;
		try {
			mDBOpenHelper.delete(PartToScreenTable.TABLENAME, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 增加最近打开
	 * 
	 * @author huyong
	 * @param values
	 */
	public void addRecentAppItem(ContentValues values) {
		try {
			mDBOpenHelper.insert(RecentAppTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 增加快捷项
	 * 
	 * @author huyong
	 * @param values
	 */
	public void addShortcutItem(ContentValues values) {
		try {
			mDBOpenHelper.insert(ShortcutTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除快捷项
	 * 
	 * @param id
	 */
	public void deleteShortcutItem(long id) {
		String whereStr = ShortcutTable.PARTID + " = " + id;
		try {
			mDBOpenHelper.delete(ShortcutTable.TABLENAME, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述:删除shortcut　table里指定intent所有数据项
	 * <br>功能详细描述:
	 * <br>注意:方法内会增加itemtype = IItemType.ITEM_TYPE_APPLICATION or itemtype = ITEM_TYPE_SHORTCUT的条件
	 * @param intentStr
	 */
	public void deleteShortcutItems(String intentStr) {
		String whereStr = ShortcutTable.INTENT + " = '" + intentStr + "'" + " AND ("
				+ ShortcutTable.ITEMTYPE + " = " + IItemType.ITEM_TYPE_APPLICATION + " or "
				+ ShortcutTable.ITEMTYPE + " = " + IItemType.ITEM_TYPE_SHORTCUT + ")";
		try {
			mDBOpenHelper.delete(ShortcutTable.TABLENAME, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @author huyong
	 * @param values
	 */
	public void updateScreen(final long desktopItemInScreenId, ContentValues values) {
		// TODO:where条件
		String whereStr = PartToScreenTable.ID + " = " + desktopItemInScreenId;
		try {
			mDBOpenHelper.update(PartToScreenTable.TABLENAME, values, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void removeDesktopItem(final long desktopItemInScreenId) {
		String whereStr = PartToScreenTable.ID + " = " + desktopItemInScreenId;
		try {
			mDBOpenHelper.delete(PartToScreenTable.TABLENAME, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 卸载程序后，删除桌面表中对应程序的数据
	 * 
	 * @author huyong
	 * @param itemId
	 */
	public void unInstallDesktopItem(final long itemId) {
		String whereStr = PartToScreenTable.PARTID + " = " + itemId;
		try {
			mDBOpenHelper.delete(PartToScreenTable.TABLENAME, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新快捷设置
	 * 
	 * @param values
	 *            数据集
	 * @param themeName
	 *            要针对修改的主题包名
	 */
	public void updateShortCutSetting(ContentValues values, String themeName) {
		String selection = ShortcutSettingTable.THEMENAME + " = " + "'" + themeName + "'";
		try {
			mDBOpenHelper.update(ShortcutSettingTable.TABLENAME, values, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新快捷设置,全局性，针对所有主题包
	 * 
	 * @param values
	 *            数据集
	 */
	public void updateShortCutSetting(ContentValues values) {
		try {
			mDBOpenHelper.update(ShortcutSettingTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入快捷设置
	 * 
	 * @param values
	 *            数据集
	 */
	public void insertShortCutSetting(ContentValues values) {
		try {
			mDBOpenHelper.insert(ShortcutSettingTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询快捷设置
	 * 
	 * @return 数据集
	 */
	public Cursor queryShortCutSetting(String themeName) {
		String selection = ShortcutSettingTable.THEMENAME + " = " + "'" + themeName + "'";
		return mDBOpenHelper.query(ShortcutSettingTable.TABLENAME, null, selection, null, null);
	}

	/**
	 * 更新手势
	 * 
	 * @param gestureid
	 *            手势ID
	 * @param values
	 *            数据
	 */
	public void updateGestureSetting(int gestureid, ContentValues values) {
		String whereStr = GestureTable.GESTUREID + " = " + gestureid;
		try {
			mDBOpenHelper.update(GestureTable.TABLENAME, values, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入手势
	 * 
	 * @param gestureid
	 *            手势ID
	 * @param values
	 *            数据
	 */
	public void insertGestureSetting(int gestureid, ContentValues values) {
		// String whereStr = GestureTable.GESTUREID + " = " + gestureid;
		try {
			mDBOpenHelper.insert(GestureTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询手势
	 * 
	 * @return 数据
	 */
	public Cursor queryGestureSetting(int gestureid) {
		String whereStr = GestureTable.GESTUREID + " = " + gestureid;
		return mDBOpenHelper.query(GestureTable.TABLENAME, null, whereStr, null, null);
	}

	/**
	 * 更新重力感应
	 * 
	 * @param values
	 *            数据
	 */
	public void updateGravitySetting(ContentValues values) {
		try {
			mDBOpenHelper.update(GravityTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入重力感应
	 * 
	 * @param values
	 *            数据
	 */
	public void insertGravitySetting(ContentValues values) {
		try {
			mDBOpenHelper.insert(GravityTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询重力感应
	 * 
	 * @return 数据
	 */
	public Cursor queryGravitySetting() {
		return mDBOpenHelper.query(GravityTable.TABLENAME, null, null, null, null);
	}

	/**
	 * 更新桌面菜单设置
	 * 
	 * @param values
	 *            数据
	 */
	public void updateDeskMenuSetting(ContentValues values) {
		try {
			mDBOpenHelper.update(DeskMenuTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入桌面菜单设置
	 * 
	 * @param values
	 *            数据
	 */
	public void insertDeskMenuSetting(ContentValues values) {
		try {
			mDBOpenHelper.insert(DeskMenuTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询桌面菜单设置
	 * 
	 * @return 数据
	 */
	public Cursor queryDeskMenuSetting() {
		return mDBOpenHelper.query(DeskMenuTable.TABLENAME, null, null, null, null);
	}

	/**
	 * 更新特效设置
	 * 
	 * @param values
	 *            数据
	 */
	public void updateEffectSetting(ContentValues values) {
		try {
			mDBOpenHelper.update(DynamicEffectTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入特效设置
	 * 
	 * @param values
	 *            数据
	 */
	public void insertEffectSetting(ContentValues values) {
		try {
			mDBOpenHelper.insert(DynamicEffectTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询特效设置
	 * 
	 * @return 数据
	 */
	public Cursor queryEffectSetting() {
		return mDBOpenHelper.query(DynamicEffectTable.TABLENAME, null, null, null, null);
	}

	/**
	 * 更新桌面设置
	 * 
	 * @param values
	 *            数据
	 */
	public void updateDesktopSetting(ContentValues values) {
		try {
			mDBOpenHelper.update(DesktopTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入桌面设置
	 * 
	 * @param values
	 *            数据
	 */
	public void insertDesktopSetting(ContentValues values) {
		try {
			mDBOpenHelper.insert(DesktopTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询桌面设置
	 * 
	 * @return 数据
	 */
	public Cursor queryDesktopSetting() {
		return mDBOpenHelper.query(DesktopTable.TABLENAME, null, null, null, null);
	}

	/**
	 * 更新屏幕设置
	 * 
	 * @param values
	 *            数据
	 */
	public void updateScreenSetting(ContentValues values) {
		try {
			mDBOpenHelper.update(ScreenSettingTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入屏幕设置
	 * 
	 * @param values
	 *            数据
	 */
	public void insertScreenSetting(ContentValues values) {
		try {
			mDBOpenHelper.insert(ScreenSettingTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询屏幕设置
	 * 
	 * @return 数据
	 */
	public Cursor queryScreenSetting() {
		return mDBOpenHelper.query(ScreenSettingTable.TABLENAME, null, null, null, null);
	}

	/**
	 * 更新主题设置
	 * 
	 * @param values
	 *            数据
	 */
	public void updateThemeSetting(ContentValues values) {
		try {
			mDBOpenHelper.update(ThemeTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入主题设置
	 * 
	 * @param values
	 *            数据
	 */
	public void insertThemeSetting(ContentValues values) {
		try {
			mDBOpenHelper.insert(ThemeTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询主题设置
	 * 
	 * @return 数据
	 */
	public Cursor queryThemeSetting() {
		return mDBOpenHelper.query(ThemeTable.TABLENAME, null, null, null, null);
	}

	/**
	 * 更新
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param values
	 *            新值
	 * @throws DatabaseException
	 */
	public void updateFunAppItem(final long folderId, final ContentValues values)
			throws DatabaseException {
		String whereStr = AppTable.FOLDERID + " = " + folderId;
		mDBOpenHelper.update(AppTable.TABLENAME, values, whereStr, null);
	}

	/**
	 * 更新元素
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @param values
	 *            新值
	 * @throws DatabaseException
	 */
	public void updateFunAppItemInFolder(final long folderId, final Intent intent,
			final ContentValues values) throws DatabaseException {
		String whereStr = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.INTENT
				+ " = " + "'" + ConvertUtils.intentToString(intent) + "'";
		mDBOpenHelper.update(FolderTable.TABLENAME, values, whereStr, null);
	}

	/**
	 * 更新元素
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @param values
	 *            新值
	 * @throws DatabaseException
	 */
	public void updateFunAppItem(final Intent intent, final ContentValues values)
			throws DatabaseException {
		String whereStr = AppTable.INTENT + " = " + "'" + ConvertUtils.intentToString(intent) + "'";
		mDBOpenHelper.update(AppTable.TABLENAME, values, whereStr, null);
	}

	/**
	 * 在文件夹表中添加一个程序
	 * 
	 * @param values
	 * @throws DatabaseException
	 */
	public void addFolderItem(ContentValues values) {
		try {
			mDBOpenHelper.insert(FolderTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新元素
	 * 
	 * @param values
	 * @throws DatabaseException
	 */
	public void updateFunAppItem(ContentValues values) throws DatabaseException {
		mDBOpenHelper.update(AppTable.TABLENAME, values, null, null);
	}

	/**
	 * 获取文件夹中元素个数
	 * 
	 * @param folderId
	 *            文件夹id
	 * @return 元素个数
	 */
	public int getSizeOfFolder(final long folderId) {
		String table = FolderTable.TABLENAME;
		String where = FolderTable.FOLDERID + " = " + folderId;
		Cursor cursor = mDBOpenHelper.query(table, null, where, null, null);
		int count = 0;
		if (cursor != null) {
			try {
				count = cursor.getCount();
			} finally {
				cursor.close();
			}
		}
		return count;
	}

	/**
	 * 获取功能表根目录元素个数
	 * 
	 * @return 个数
	 */
	public int getSizeOfApps() {
		String table = AppTable.TABLENAME;
		Cursor cursor = mDBOpenHelper.query(table, null, null, null, null);
		int count = 0;
		if (cursor != null) {
			try {
				count = cursor.getCount();
			} finally {
				cursor.close();
			}
		}
		return count;
	}

	/**
	 * 在文件夹中添加一个程序,并维护index
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param index
	 *            插入位置
	 * @param intent
	 *            程序唯一标识
	 * @throws DatabaseException
	 */
	public void addFunAppToFolder(final long folderId, final int index, final Intent intent,
			final String title) throws DatabaseException {
		if (null == intent) {
			return;
		}
		if (index < 0) {
			return;
		}

		// 获取folderId对于的文件夹的size
		int size = getSizeOfFolder(folderId);

		// 维护index
		String updateSql = "update " + FolderTable.TABLENAME + " set " + FolderTable.INDEX + " = "
				+ FolderTable.INDEX + " + 1 " + " where " + FolderTable.FOLDERID + " = " + folderId
				+ " and " + FolderTable.INDEX + " >= " + index + ";";
		mDBOpenHelper.exec(updateSql);

		// 处理特殊情况
		int idx = index > size ? size : index;
		// 添加
		addFunAppToFolderDirect(folderId, idx, intent, title);
	}

	/**
	 * 直接添加元素到文件夹
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param index
	 *            位置
	 * @param intent
	 *            元素
	 * @throws DatabaseException
	 */
	private void addFunAppToFolderDirect(final long folderId, final int index, final Intent intent,
			final String title) throws DatabaseException {
		if (null == intent) {
			return;
		}
		// 直接添加
		ContentValues contentValues = new ContentValues();
		contentValues.put(FolderTable.FOLDERID, folderId);
		contentValues.put(FolderTable.INDEX, index);
		contentValues.put(FolderTable.INTENT, ConvertUtils.intentToString(intent));
		contentValues.put(FolderTable.USERTITLE, title);
		contentValues.put(FolderTable.TIMEINFOLDER, System.currentTimeMillis());
		mDBOpenHelper.insert(FolderTable.TABLENAME, contentValues);
		contentValues.clear();
		contentValues = null;
	}

	/**
	 * 屏幕文件夹操作：从文件夹删除项
	 * 
	 * @param screenItemId
	 * @param folderId
	 */
	public void removeAppFromFolder(long screenItemId, long folderId) {
		int index = getFolderItemIndex(screenItemId, folderId);
		if (index < 0) {
			// TODO LOG 异常
			return;
		}

		try {
			mDBOpenHelper.beginTransaction();
			// 删除
			String sql = "delete from " + FolderTable.TABLENAME + " where " + FolderTable.FOLDERID
					+ " = " + folderId + " and " + FolderTable.ID + " = " + screenItemId;
			mDBOpenHelper.exec(sql);

			// 维护index
			String updateSql = "update " + FolderTable.TABLENAME + " set " + FolderTable.INDEX
					+ " = " + FolderTable.INDEX + " - 1 " + " where " + FolderTable.FOLDERID
					+ " = " + folderId + " and " + FolderTable.INDEX + " > " + index + ";";
			mDBOpenHelper.exec(updateSql);
			mDBOpenHelper.setTransactionSuccessful();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	public void removeAppFromFolder(Intent intent, boolean fromDrawer, long folderId) {
		int index = getFolderItemIndex(intent, fromDrawer, folderId);
		if (index < 0) {
			// TODO LOG 异常
			return;
		}
		try {
			mDBOpenHelper.beginTransaction();
			// 删除
			String sql = "delete from " + FolderTable.TABLENAME + " where " + FolderTable.FOLDERID
					+ " = " + folderId + " and " + FolderTable.FROMAPPDRAWER + " = "
					+ (fromDrawer ? 1 : 0) + " and " + FolderTable.INTENT + " = " + "'"
					+ ConvertUtils.intentToString(intent) + "'";
			mDBOpenHelper.exec(sql);

			// 维护index
			String updateSql = "update " + FolderTable.TABLENAME + " set " + FolderTable.INDEX
					+ " = " + FolderTable.INDEX + " - 1 " + " where " + FolderTable.FOLDERID
					+ " = " + folderId + " and " + FolderTable.INDEX + " > " + index + ";";
			mDBOpenHelper.exec(updateSql);
			mDBOpenHelper.setTransactionSuccessful();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	/**
	 * 屏幕文件夹操作：通过ItemID获取索引值
	 * 
	 * @param screenItemId
	 * @param folderId
	 * @return
	 */
	public int getFolderItemIndex(long screenItemId, long folderId) {
		String selection = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.ID
				+ " = " + screenItemId;

		// 查询列数
		String columns[] = { FolderTable.INDEX };

		int retIndex = -1;
		Cursor cursor = mDBOpenHelper.query(FolderTable.TABLENAME, columns, selection, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int idx = cursor.getColumnIndex(FolderTable.INDEX);
					retIndex = cursor.getInt(idx);
				}
			} finally {
				cursor.close();
			}
		}

		return retIndex;
	}

	public int getFolderItemIndex(Intent intent, boolean fromDrawer, long folderId) {
		String selection = FolderTable.FOLDERID + " = " + folderId + " and "
				+ FolderTable.FROMAPPDRAWER + " = " + (fromDrawer ? 1 : 0) + " and "
				+ FolderTable.INTENT + " = " + "'" + ConvertUtils.intentToString(intent) + "'";

		// 查询列数
		String columns[] = { FolderTable.INDEX };

		int retIndex = -1;
		Cursor cursor = mDBOpenHelper.query(FolderTable.TABLENAME, columns, selection, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int idx = cursor.getColumnIndex(FolderTable.INDEX);
					retIndex = cursor.getInt(idx);
				}
			} finally {
				cursor.close();
			}
		}

		return retIndex;
	}

	/**
	 * 在文件夹中删除一个程序
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param intent
	 * @throws DatabaseException
	 */
	public void removeFunAppFromFolder(final long folderId, final Intent intent)
			throws DatabaseException {
		if (null == intent) {
			return;
		}

		// 获取index
		int index = getFunAppIndexByIntentInFolder(folderId, intent);
		if (index < 0) {
			// TODO:打日志
			return;
		}
		try {
			mDBOpenHelper.beginTransaction();
			// 删除
			String sql = "delete from " + FolderTable.TABLENAME + " where " + FolderTable.FOLDERID
					+ " = " + folderId + " and " + FolderTable.INTENT + " = " + "'"
					+ ConvertUtils.intentToString(intent) + "'";
			mDBOpenHelper.exec(sql);

			// 维护index
			String updateSql = "update " + FolderTable.TABLENAME + " set " + FolderTable.INDEX
					+ " = " + FolderTable.INDEX + " - 1 " + " where " + FolderTable.FOLDERID
					+ " = " + folderId + " and " + FolderTable.INDEX + " > " + index + ";";
			mDBOpenHelper.exec(updateSql);
			mDBOpenHelper.setTransactionSuccessful();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	private int getFunAppIndexByIntentInFolder(final long folderId, final Intent intent) {
		if (null == intent) {
			return -1;
		}

		String str = ConvertUtils.intentToString(intent);
		String selection = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.INTENT
				+ " = " + "'" + str + "'";

		// 查询列数
		String columns[] = { FolderTable.INDEX };

		int retIndex = -1;
		Cursor cursor = mDBOpenHelper.query(FolderTable.TABLENAME, columns, selection, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int idx = cursor.getColumnIndex(FolderTable.INDEX);
					retIndex = cursor.getInt(idx);
				}
			} finally {
				cursor.close();
			}
		}

		return retIndex;
	}

	/**
	 * 添加一个元素，以ContentValues的index为添加位置，若越界则添加到末尾
	 * 
	 * @param values
	 * @throws DatabaseException
	 */
	public void addAppItem(ContentValues values) throws DatabaseException {
		try {
			mDBOpenHelper.beginTransaction();
			// 更新index
			int index = values.getAsInteger(AppTable.INDEX);
			// 获取size, -1表示查询失败
			int size = getSizeOfFunAppItem();

			// 维护index
			String updateSql = "update " + AppTable.TABLENAME + " set " + AppTable.INDEX + " = "
					+ AppTable.INDEX + " + 1 " + " where " + FolderTable.INDEX + " >= " + index
					+ ";";
			mDBOpenHelper.exec(updateSql);

			int idx = (index > size && size != -1) ? size : index;
			// 添加
			values.put(AppTable.INDEX, idx);
			mDBOpenHelper.insert(AppTable.TABLENAME, values);
			mDBOpenHelper.setTransactionSuccessful();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	/**
	 * 清除表中的所有数据
	 * 
	 * @param tableName
	 *            表名
	 */
	public void clearTable(String tableName) {
		String sql = "delete from " + tableName + "; ";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void clearFunAppItems() throws DatabaseException {
		String sql = "delete from " + AppTable.TABLENAME + "; ";
		mDBOpenHelper.exec(sql);
	}

	/**
	 * 清除文件夹中元素数据
	 * 
	 * @param
	 * @throws DatabaseException
	 */
	public void clearFolderAppItems(final long folderId) throws DatabaseException {
		String sql = "delete from " + FolderTable.TABLENAME + " where " + FolderTable.FOLDERID
				+ " = " + folderId + "; ";
		mDBOpenHelper.exec(sql);
	}

	public void clearTable(String tableName, String whereStr) {
		if (null == whereStr) {
			clearTable(tableName);
		} else {
			String sql = "delete from " + tableName + " where " + whereStr + "; ";
			try {
				mDBOpenHelper.exec(sql);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @author huyong
	 * @param resIndex
	 * @param tarIndex
	 */
	public boolean moveAppItem(final int resIndex, final int tarIndex) {
		boolean result = false;
		String add = resIndex > tarIndex ? " + 1 " : " - 1 ";
		String comp1 = resIndex > tarIndex ? " >= " : " <= ";
		String comp2 = resIndex > tarIndex ? " < " : " > ";

		String mindex = AppTable.INDEX;
		mDBOpenHelper.beginTransaction();
		try {
			String sql = "update application set " + mindex + " = " + " -1 " + " where " + mindex
					+ " = " + resIndex + ";";
			mDBOpenHelper.exec(sql);
			sql = "update application set " + mindex + " = " + mindex + add + " where " + mindex
					+ comp1 + tarIndex + " and " + mindex + comp2 + resIndex + ";";
			mDBOpenHelper.exec(sql);
			sql = " update application set " + mindex + " = " + tarIndex + " where " + mindex
					+ " = " + " -1 " + ";";
			mDBOpenHelper.exec(sql);
			mDBOpenHelper.setTransactionSuccessful();
			result = true;
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}
		return result;
	}

	public void updateAppItem(final long itemId, ContentValues values) throws DatabaseException {
		String whereStr = AppTable.PARTID + " = " + itemId;
		mDBOpenHelper.update(AppTable.TABLENAME, values, whereStr, null);
	}

	public ArrayList<Long> getUnInstalledAppInfos() {
		String selection = AppTable.ISEXIST + " = 0 ";
		// 查询列数
		String columns[] = { AppTable.PARTID, };
		ArrayList<Long> idArrayList = new ArrayList<Long>();
		Cursor cursor = mDBOpenHelper.query(AppTable.TABLENAME, columns, selection, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						Long appId = cursor.getLong(cursor.getColumnIndex(AppTable.PARTID));
						idArrayList.add(appId);
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
		}
		return idArrayList;
	}

	/**
	 * 删除所有已卸载的程序
	 * 
	 * @author huyong
	 */
	public void deleteUnInstallApp() {
		ArrayList<Long> idArrayList = getUnInstalledAppInfos();
		if (idArrayList == null) {
			return;
		}
		int size = idArrayList.size();
		for (int i = 0; i < size; i++) {
			Long appId = idArrayList.get(i);
			try {
				removeAppItem(appId);
				// 删除桌面上对应数据
				unInstallDesktopItem(appId);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		// mDBOpenHelper.delete(AppTable.TABLENAME, selection, null);
	}

	/**
	 * 更新所有程序安装卸载的状态
	 * 
	 * @author huyong
	 */
	public void updateAllAppExistFlag() {
		String isExist = AppTable.ISEXIST;
		String sql = "update application set " + isExist + " = " + "0" + " where " + isExist
				+ " = " + "1" + ";";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param index
	 * @throws DatabaseException
	 */
	// public void removeFunappItemInfo(int index) throws DatabaseException {
	// try {
	// mDBOpenHelper.beginTransaction();
	// // 删除元素
	// String selection = AppTable.INDEX + " = " + index;
	// mDBOpenHelper.delete(AppTable.TABLENAME, selection, null);
	//
	// // 更新mItemInAppIndex
	// String sql = "update application set " + AppTable.INDEX + " = "
	// + AppTable.INDEX + " - 1 " + " where " + AppTable.INDEX
	// + " > " + index + ";";
	// mDBOpenHelper.exec(sql);
	// mDBOpenHelper.setTransactionSuccessful();
	// } finally {
	// mDBOpenHelper.endTransaction();
	// }
	// }

	/**
	 * 根据Intent删除对应的元素
	 * 
	 * @param intent
	 *            Intent
	 * @throws DatabaseException
	 */
	public void removeFunappItemInfo(final Intent intent) throws DatabaseException {
		// int index = getFunappItemIndexByIntent(intent);
		// if (index < 0) {
		// return ;
		// }
		// removeFunappItemInfo(index);
		removeFunappItemInfoByIntent(intent);
	}

	/**
	 * 根据intent删除对应的元素
	 * 
	 * @param intent
	 * @throws DatabaseException
	 */
	public void removeFunappItemInfoByIntent(Intent intent) throws DatabaseException {

		if (intent == null) {
			return;
		}
		try {
			mDBOpenHelper.beginTransaction();
			int index = getFunappItemIndexByIntent(intent);
			if (index < 0) {
				return;
			}
			String str = ConvertUtils.intentToString(intent);
			String selection = AppTable.INTENT + " = " + "'" + str + "'";
			// 删除元素
			mDBOpenHelper.delete(AppTable.TABLENAME, selection, null);

			// 更新mItemInAppIndex
			String sql = "update application set " + AppTable.INDEX + " = " + AppTable.INDEX
					+ " - 1 " + " where " + AppTable.INDEX + " > " + index + ";";
			mDBOpenHelper.exec(sql);
			mDBOpenHelper.setTransactionSuccessful();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	/**
	 * 根据intent找到对应的index
	 * 
	 * @param intent
	 *            索引
	 * @return
	 */
	public int getFunappItemIndexByIntent(final Intent intent) {
		if (null == intent) {
			return -1;
		}
		// TODO:使用统一的转换
		String str = ConvertUtils.intentToString(intent);
		String selection = AppTable.INTENT + " = " + "'" + str + "'";

		// 查询列数
		String columns[] = { AppTable.INDEX };

		int retIndex = -1;
		Cursor cursor = mDBOpenHelper.query(AppTable.TABLENAME, columns, selection, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int idx = cursor.getColumnIndex(AppTable.INDEX);
					retIndex = cursor.getInt(idx);
				}
			} finally {
				cursor.close();
			}
		}

		return retIndex;
	}

	/**
	 * 删除程序接口
	 * 
	 * @author huyong
	 * @param itemId
	 * @throws DatabaseException
	 */
	public void removeAppItem(final long itemId) throws DatabaseException {
		try {
			mDBOpenHelper.beginTransaction();
			// TODO:
			String selection = PartsTable.ID + " = " + itemId;
			mDBOpenHelper.delete(PartsTable.TABLENAME, selection, null);

			selection = AppTable.PARTID + " = " + itemId;

			// TODO:之后的id索引--
			String[] projection = { AppTable.INDEX };
			int index = -1;
			Cursor cursor = mDBOpenHelper.query(AppTable.TABLENAME, projection, selection, null,
					null);
			if (cursor != null) {
				try {
					if (cursor.moveToFirst()) {
						index = cursor.getInt(cursor.getColumnIndex(AppTable.INDEX));
					}
				} finally {
					cursor.close();
				}
			}
			if (index > 0) {
				// 首先删除该条记录，然后更新该记录后的所有字段索引值减一
				mDBOpenHelper.delete(AppTable.TABLENAME, selection, null);
				String sql = "update application set " + AppTable.INDEX + " = " + AppTable.INDEX
						+ " - 1 " + " where " + AppTable.INDEX + " > " + index + ";";
				mDBOpenHelper.exec(sql);
			}
			mDBOpenHelper.setTransactionSuccessful();
		} finally {
			mDBOpenHelper.endTransaction();
		}

	}

	/**
	 * 删除最近打开
	 * 
	 * @author huyong
	 * @param itemId
	 */
	public void removeRecentAppItem(final long itemId) {
		String selection = RecentAppTable.PARTID + " = " + itemId;
		try {
			mDBOpenHelper.delete(RecentAppTable.TABLENAME, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除最近打开
	 * 
	 * @author huyong
	 * @param itemId
	 */
	public void removeRecentAppItem(final Intent intent) {
		if (null == intent) {
			return;
		}
		// TODO:统一转换
		String str = ConvertUtils.intentToString(intent);
		String selection = RecentAppTable.INTENT + " = " + "'" + str + "'";
		try {
			mDBOpenHelper.delete(RecentAppTable.TABLENAME, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除最近打开第index条
	 * 
	 * @author huyong
	 * @param itemId
	 */
	public void removeRecentAppItem(final int index) {

		String table = RecentAppTable.TABLENAME;
		Cursor cursor = mDBOpenHelper.query(table, null, null, null, null);
		if (null == cursor) {
			return;
		}
		try {
			int inetntIdx = cursor.getColumnIndexOrThrow(RecentAppTable.INTENT);
			if (cursor.moveToPosition(index)) {
				String str = cursor.getString(inetntIdx);
				Intent intent = ConvertUtils.stringToIntent(str);
				removeRecentAppItem(intent);
			}
		} finally {
			cursor.close();
		}
	}

	/**
	 * 删除所有最近打开的项
	 * 
	 * @author huyong
	 */
	public void removeRecentAppItems() {
		String tableName = RecentAppTable.TABLENAME;
		String selection = RecentAppTable.INDEX + " >= 0 ";
		try {
			mDBOpenHelper.delete(tableName, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新功能表设置
	 * 
	 * @author huyong
	 * @param settingItem
	 * @param value
	 */
	public void setAppSetting(final int settingItem, final String value) {
		String sql = "update " + AppSettingTable.TABLENAME + " set " + AppSettingTable.INFOVALUE
				+ " = " + "'" + value + "'" + " where " + AppSettingTable.INFOTYPE + " = "
				+ settingItem + ";";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public Cursor getAppSetting(final String packageName, final String columnName) {
		String selection = AppSettingTable.THEMEPACKAGENAME + " = '" + packageName + "'";
		String tableName = AppSettingTable.TABLENAME;
		// 查询列数
		String columns[] = { columnName, };
		return mDBOpenHelper.query(tableName, columns, selection, null, null);
	}

	public void setAppSetting2(final String packageName, final ContentValues value) {
		String selection = AppSettingTable.THEMEPACKAGENAME + " = '" + packageName + "'";
		try {
			mDBOpenHelper.update(AppSettingTable.TABLENAME, value, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 添加设置项
	 * 
	 * @param packageName
	 * @param value
	 */
	public void addAppSetting(final String packageName, final ContentValues value) {
		String selection = AppSettingTable.THEMEPACKAGENAME + " = '" + packageName + "'";
		String tableName = AppSettingTable.TABLENAME;
		Cursor cursor = mDBOpenHelper.query(tableName, null, selection, null, null);
		boolean b = false;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					b = true;
				}
			} finally {
				cursor.close();
			}
		}

		if (!b) {
			try {
				mDBOpenHelper.insert(tableName, value);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取AppFuncSetting
	 * 
	 * @param key
	 * @return
	 */
	public Cursor getAppFuncSetting(String pkname, int key) {
		String selection = AppFuncSettingTable.PKNAME + "='" + pkname + "' and "
				+ AppFuncSettingTable.SETTINGKEY + "=" + key;
		String tableName = AppFuncSettingTable.TABLENAME;
		String columns[] = { AppFuncSettingTable.SETTINGVALUE, };
		return mDBOpenHelper.query(tableName, columns, selection, null, null);
	}

	/**
	 * 插入或更新AppFuncSetting
	 * 
	 * @param key
	 * @param value
	 */
	public void setAppFuncSetting(String pkname, int key, String value) {
		boolean update = false;
		String selection = AppFuncSettingTable.PKNAME + "='" + pkname + "' and "
				+ AppFuncSettingTable.SETTINGKEY + "=" + key;
		String tableName = AppFuncSettingTable.TABLENAME;
		String columns[] = { AppFuncSettingTable.SETTINGVALUE, };
		Cursor cur = mDBOpenHelper.query(tableName, columns, selection, null, null);
		if (cur != null) {
			try {
				if (cur.moveToFirst()) {
					update = true;
				}
			} finally {
				cur.close();
			}
		}
		ContentValues values = new ContentValues();
		values.put(AppFuncSettingTable.SETTINGKEY, key);
		values.put(AppFuncSettingTable.SETTINGVALUE, value);
		values.put(AppFuncSettingTable.PKNAME, pkname);
		try {
			if (update) {
				mDBOpenHelper.update(tableName, values, selection, null);
			} else {
				mDBOpenHelper.insert(tableName, values);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入或更新AppFuncSetting
	 * 
	 * @param key
	 * @param value
	 */
	public void addAppFuncSetting(String pkname, int key, String value) {
		boolean exist = false;
		String selection = AppFuncSettingTable.PKNAME + "='" + pkname + "' and "
				+ AppFuncSettingTable.SETTINGKEY + "=" + key;
		String tableName = AppFuncSettingTable.TABLENAME;
		String columns[] = { AppFuncSettingTable.SETTINGVALUE, };
		Cursor cur = mDBOpenHelper.query(tableName, columns, selection, null, null);
		if (cur != null) {
			try {
				if (cur.moveToFirst()) {
					exist = true;
				}
			} finally {
				cur.close();
			}
		}

		try {
			if (!exist) {
				ContentValues values = new ContentValues();
				values.put(AppFuncSettingTable.SETTINGKEY, key);
				values.put(AppFuncSettingTable.SETTINGVALUE, value);
				values.put(AppFuncSettingTable.PKNAME, pkname);
				mDBOpenHelper.insert(tableName, values);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 添加桌面个性搭配设置项
	 * 
	 * @param packageName
	 * @param value
	 */
	public void addScreenStyleSetting(final String packageName, final ContentValues value) {
		String selection = ScreenStyleConfigTable.THEMEPACKAGE + " = '" + packageName + "'";
		String tableName = ScreenStyleConfigTable.TABLENAME;
		Cursor cursor = mDBOpenHelper.query(tableName, null, selection, null, null);
		boolean b = false;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					b = true;
				}
			} finally {
				cursor.close();
			}
		}

		if (!b) {
			try {
				mDBOpenHelper.insert(tableName, value);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateScreenStyleSetting(final String packageName, final ContentValues value) {
		String selection = ScreenStyleConfigTable.THEMEPACKAGE + " = '" + packageName + "'";
		try {
			mDBOpenHelper.update(ScreenStyleConfigTable.TABLENAME, value, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void clearDirtyStyleSetting(final String uninstallPackageName) {

		try {
			// 删除已卸载的主题的搭配
			String selection = ScreenStyleConfigTable.THEMEPACKAGE + " = '" + uninstallPackageName
					+ "'";
			mDBOpenHelper.delete(ScreenStyleConfigTable.TABLENAME, selection, null);

			// 如果搭配中有被卸载的主题包名更新为该主题自己
			String update = "update " + ScreenStyleConfigTable.TABLENAME + " set "
					+ ScreenStyleConfigTable.FOLDERSTYLEPACKAGE + " = "
					+ ScreenStyleConfigTable.THEMEPACKAGE + " where "
					+ ScreenStyleConfigTable.FOLDERSTYLEPACKAGE + " = '" + uninstallPackageName
					+ "'";
			mDBOpenHelper.exec(update);

			update = "update " + ScreenStyleConfigTable.TABLENAME + " set "
					+ ScreenStyleConfigTable.ICONSTYLEPACKAGE + " = "
					+ ScreenStyleConfigTable.THEMEPACKAGE + " where "
					+ ScreenStyleConfigTable.ICONSTYLEPACKAGE + " = '" + uninstallPackageName + "'";
			mDBOpenHelper.exec(update);

			update = "update " + ScreenStyleConfigTable.TABLENAME + " set "
					+ ScreenStyleConfigTable.GGMENUPACKAGE + " = "
					+ ScreenStyleConfigTable.THEMEPACKAGE + " where "
					+ ScreenStyleConfigTable.GGMENUPACKAGE + " = '" + uninstallPackageName + "'";
			mDBOpenHelper.exec(update);

			update = "update " + ScreenStyleConfigTable.TABLENAME + " set "
					+ ScreenStyleConfigTable.INDICATOR + " = "
					+ ScreenStyleConfigTable.THEMEPACKAGE + " where "
					+ ScreenStyleConfigTable.INDICATOR + " = '" + uninstallPackageName + "'";
			mDBOpenHelper.exec(update);

			update = "update " + AppSettingTable.TABLENAME + " set " + AppSettingTable.TAB_HOME_BG
					+ " = " + AppSettingTable.THEMEPACKAGENAME + " where "
					+ AppSettingTable.TAB_HOME_BG + " = '" + uninstallPackageName + "'";
			mDBOpenHelper.exec(update);

			update = "update " + AppSettingTable.TABLENAME + " set "
					+ AppSettingTable.INDICATOR_STYLE + " = " + AppSettingTable.THEMEPACKAGENAME
					+ " where " + AppSettingTable.INDICATOR_STYLE + " = '" + uninstallPackageName
					+ "'";
			mDBOpenHelper.exec(update);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public Cursor getScreenStyleSetting(final String packageName, final String columnName) {
		String selection = ScreenStyleConfigTable.THEMEPACKAGE + " = '" + packageName + "'";
		String tableName = ScreenStyleConfigTable.TABLENAME;
		// 查询列数
		String columns[] = { columnName, };
		return mDBOpenHelper.query(tableName, columns, selection, null, null);
	}

	/**
	 * 更新统计
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public void updateStatistics(final int key, final long value) {
		ContentValues values = new ContentValues();
		values.put(StatisticsTable.VALUE, value);
		String selection = StatisticsTable.KEY + " = " + key;
		try {
			mDBOpenHelper.update(StatisticsTable.TABLENAME, values, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入统计
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public void insertStatistics(final int key, final long value) {
		ContentValues values = new ContentValues();
		values.put(StatisticsTable.KEY, key);
		values.put(StatisticsTable.VALUE, value);
		try {
			mDBOpenHelper.insert(StatisticsTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询统计
	 * 
	 * @return 数据
	 */
	public Cursor queryStatistics() {
		return mDBOpenHelper.query(StatisticsTable.TABLENAME, null, null, null, null);
	}

	public void updateShortCutItem(final long id, final ContentValues values, final String themeName) {
		if (values == null) {
			return;
		}
		String tableName = ShortcutTable.TABLENAME;
		String selection = ShortcutTable.PARTID + " = " + id + " and " + ShortcutTable.THEMENAME
				+ " = " + "'" + themeName + "'";
		try {
			mDBOpenHelper.update(tableName, values, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @author huyong
	 * @param itemId
	 * @param values
	 */
	public void updateItem(final long itemId, ContentValues values) {
		if (values == null) {
			return;
		}
		String tableName = PartsTable.TABLENAME;
		String selection = PartsTable.ID + " = " + itemId;
		try {
			mDBOpenHelper.update(tableName, values, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取程序原生图片
	 * 
	 * @param itemInScreenId
	 * @return
	 */
	public Cursor getOriginalIcon(final long itemInScreenId) {
		String tables = PartToScreenTable.TABLENAME + ", " + PartsTable.TABLENAME;
		// 查询列数
		String columns[] = { PartsTable.INTENT, PartsTable.ICON };
		// 查询条件

		// 查询条件
		String condition = PartToScreenTable.ID + " = " + itemInScreenId + " and "
				+ PartToScreenTable.TABLENAME + "." + PartToScreenTable.PARTID + " = "
				+ PartsTable.TABLENAME + "." + PartsTable.ID;
		return mDBOpenHelper.queryCrossTables(tables, columns, condition, null, null);
	}

	/**
	 * 
	 * @author huyong
	 * @param itemUri
	 * @return
	 */
	public Cursor getItemIdByUri(Uri itemUri) {
		String table = PartsTable.TABLENAME;
		// 查询列数
		String columns[] = { PartsTable.ID, };
		// 查询条件
		String condition = PartsTable.URI + " = '" + itemUri.toString() + "'";
		return mDBOpenHelper.query(table, columns, condition, null, null);
	}

	/**
	 * 
	 * @author huyong
	 * @param intent
	 * @return
	 */
	public Cursor getItemByIntent(final String itemIntent) {
		String table = PartsTable.TABLENAME;
		// 查询条件
		String selection = PartsTable.INTENT + " = '" + itemIntent + "'";
		return mDBOpenHelper.query(table, null, selection, null, null);

	}

	public void addItemToFolder(ContentValues contentValues) {
		String table = PartToFolderTable.TABLENAME;
		try {
			mDBOpenHelper.insert(table, contentValues);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void delItemFromFolder(final long itemId, final long folderId) {
		String table = PartToFolderTable.TABLENAME;
		String selection = PartToFolderTable.FOLDERID + " = " + folderId + " and "
				+ PartToFolderTable.PARTID + " = " + itemId;
		String sql = "delete from " + table + " where " + selection + ";";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void updateFolderIndex(long folderID, long itemID, int index) {
		// TODO:更新索引
		String table = FolderTable.TABLENAME;
		String mindex = FolderTable.INDEX;

		String selection = FolderTable.FOLDERID + " = " + folderID + " and " + FolderTable.ID
				+ " = " + itemID;

		String sql = "update " + table + " set " + mindex + " = " + index + " where " + selection
				+ ";";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新文件夹内的一个item的数据信息 add by jiangxuwen
	 */
	public void updateFolderIntentAndType(long folderID, long itemID, String intentString,
			int typeInt, int iconType) {
		// TODO:更新Intent
		String table = FolderTable.TABLENAME;
		String intent = FolderTable.INTENT;
		String type = FolderTable.TYPE;
		String userIconType = FolderTable.USERICONTYPE;
		String selection = FolderTable.FOLDERID + " = " + folderID + " and " + FolderTable.ID
				+ " = " + itemID;
		String sql = "update " + table + " set " + intent + " = \"" + intentString + "\"," + type
				+ " = " + typeInt + "," + userIconType + " = " + iconType + " where " + selection
				+ ";";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新指定文件夹内指定一项的值
	 * 
	 * @param folderID
	 * @param itemID
	 * @param values
	 */
	public void updateFolderItem(long folderID, long itemID, ContentValues values) {
		try {
			String selection = FolderTable.FOLDERID + " = " + folderID + " and " + FolderTable.ID
					+ " = " + itemID;

			mDBOpenHelper.update(FolderTable.TABLENAME, values, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	// 添加表记录
	public boolean addRecord(String tableName, ContentValues values) {
		try {
			mDBOpenHelper.insert(tableName, values);
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 更新表记录
	public boolean updateRecord(String tableName, ContentValues values, String selection) {
		try {
			mDBOpenHelper.update(tableName, values, selection, null);
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 删除表记录
	public boolean delRecord(String tableName, String selection) {
		try {
			mDBOpenHelper.delete(tableName, selection, null);
			return true;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Cursor getAllRecord(String tableName) {
		return mDBOpenHelper.query(tableName, null, null, null, null);
	}

	/**
	 * *****************************程序管理模块*************************
	 */

	public void addIgnoreTaskAppItem(final ContentValues values) {
		try {
			mDBOpenHelper.insert(AppWhiteListTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void delTaskAppItem(final Intent intent) {
		String intentString = ConvertUtils.intentToString(intent);
		String selection = AppWhiteListTable.INTENT + " = '" + intentString + "'";
		try {
			mDBOpenHelper.delete(AppWhiteListTable.TABLENAME, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void clearAllIgnoreTaskAppItems() {
		String selection = AppWhiteListTable.ISIGNORE + " = 1 ";
		try {
			mDBOpenHelper.delete(AppWhiteListTable.TABLENAME, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public Cursor getAllIgnoreTaskAppItems() {
		String selection = AppWhiteListTable.ISIGNORE + " = 1 ";
		return mDBOpenHelper.query(AppWhiteListTable.TABLENAME, null, selection, null, null);
	}

	/**
	 * 添加隐藏的Item
	 * 
	 * @param values
	 * @throws DatabaseException
	 */
	public void addHideAppItem(final ContentValues values) throws DatabaseException {
		mDBOpenHelper.insert(AppHideListTable.TABLENAME, values);
	}

	/**
	 * 删除隐藏的Item
	 * 
	 * @param intent
	 * @throws DatabaseException
	 */
	public void delHideAppItem(final Intent intent) throws DatabaseException {
		String intentString = ConvertUtils.intentToString(intent);
		String selection = AppHideListTable.INTENT + " = '" + intentString + "'";
		mDBOpenHelper.delete(AppHideListTable.TABLENAME, selection, null);
	}

	/**
	 * 清空隐藏的Item
	 * 
	 * @throws DatabaseException
	 */
	public void clearAllHideAppItems() throws DatabaseException {
		String selection = AppHideListTable.ISHIDE + " = 1 ";
		mDBOpenHelper.delete(AppHideListTable.TABLENAME, selection, null);
	}

	/**
	 * 获取隐藏的Items
	 * 
	 * @return 隐藏的Items
	 */
	public Cursor getAllHideAppItems() {
		String selection = AppHideListTable.ISHIDE + " = 1 ";
		return mDBOpenHelper.query(AppHideListTable.TABLENAME, null, selection, null, null);
	}

	/**
	 * 应用更新，忽略更新应用管理模块
	 */
	public void addNoPromptUpdateApp(String tableName, ContentValues values) {
		try {
			mDBOpenHelper.insert(tableName, values);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void delNoPromptUpdateApp(String tableName, String selection) {
		try {
			mDBOpenHelper.delete(tableName, selection, null);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Cursor getAllNoPromptUpdateApp(String tableName) {
		return mDBOpenHelper.query(tableName, null, null, null, null);
	}

	public void beginTransaction() {
		mDBOpenHelper.beginTransaction();
	}

	public void setTransactionSuccessful() {
		mDBOpenHelper.setTransactionSuccessful();
	}

	public void endTransaction() {
		mDBOpenHelper.endTransaction();
	}

	// add by huyong 2011-03-17 for 清理桌面脏数据
	/**
	 * 判断桌面是否有脏数据
	 */
	public boolean isScreenDirtyData() {
		boolean result = false;
		String table = PartToScreenTable.TABLENAME;
		String columns[] = { PartToScreenTable.SCREENID };
		// 查询条件
		String groupBy = PartToScreenTable.SCREENID + ", " + PartToScreenTable.SCREENX + ", "
				+ PartToScreenTable.SCREENY;
		String having = "count(*) > 1";

		Cursor cursor = mDBOpenHelper.query(table, columns, null, null, groupBy, having, null);
		if (cursor != null) {
			try {
				result = cursor.moveToFirst();
			} finally {
				cursor.close();
			}
		}
		if (result) {
			return result;
		}

		// 查询是否有screenid<0的脏数据.
		table = ScreenTable.TABLENAME;
		groupBy = ScreenTable.SCREENID;
		String screenidColumns[] = { ScreenTable.SCREENID };
		String selection = ScreenTable.SCREENID + " < 0";
		cursor = mDBOpenHelper.query(table, screenidColumns, selection, null, null, null, null);
		if (cursor != null) {
			try {
				result = cursor.moveToFirst();
			} finally {
				cursor.close();
			}
		}
		if (result) {
			return result;
		}

		// TODO:查询是否有screenIndex不连续的脏数据
		table = ScreenTable.TABLENAME;
		String screenIndexColumns[] = { ScreenTable.MINDEX };
		String sortOrder = ScreenTable.MINDEX + " DESC";
		cursor = mDBOpenHelper.query(table, screenIndexColumns, null, null, null, null, sortOrder);
		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (cursor.moveToFirst()) {
					int maxIndex = cursor.getInt(0);
					if (maxIndex >= count) {
						result = true;
					}
				}
			} finally {
				cursor.close();
			}
		}

		return result;
	}

	/**
	 * 清理桌面表中脏数据
	 * 
	 * @author huyong
	 */
	public void clearScreenDirtyData() {

		// TODO:不采取hardcode
		String sql = "delete from parttoscreen where itemInScreenId in ( "
				+ "select itemInScreenId from parttoscreen inner join"
				+ "(select screenid, screenx, screeny from parttoscreen "
				+ "group by screenx, screeny, screenid having count(*)>1)" + " as T "
				+ "on parttoscreen.screenid = T.screenid "
				+ "and parttoscreen.screenx = T.screenx " + "and parttoscreen.screeny = T.screeny "
				+ " )";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		mDBOpenHelper.beginTransaction();
		try {
			// 清理screenid为负数的脏数据
			sql = "update screen set screenid = -screenid where screenid < 0;";
			String column = ScreenTable.SCREENID;
			sql = "update " + ScreenTable.TABLENAME + " set " + column + " = -" + column
					+ " where " + column + " < 0;";
			mDBOpenHelper.exec(sql);

			column = PartToScreenTable.SCREENID;
			sql = "update parttoscreen set screenid = -screenid where screenid < 0;";
			sql = "update " + PartToScreenTable.TABLENAME + " set " + column + " = -" + column
					+ " where " + column + " <0;";
			mDBOpenHelper.exec(sql);

			mDBOpenHelper.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}

		// 清理screenindx不连续的问题
		String tableName = ScreenTable.TABLENAME;
		String projection[] = { ScreenTable.SCREENID };
		String sortOrder = ScreenTable.MINDEX + " ASC";
		ArrayList<Long> screenIdList = new ArrayList<Long>();
		Cursor cursor = mDBOpenHelper.query(tableName, projection, null, null, sortOrder);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						Long screenId = cursor.getLong(0);
						screenIdList.add(screenId);
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
		}

		int screenSize = screenIdList.size();
		String updateSql = "update " + tableName + " set " + ScreenTable.MINDEX + " = ";
		String tmpSql = null;
		String where = " where " + ScreenTable.SCREENID + " = ";
		for (int i = 0; i < screenSize; i++) {
			tmpSql = updateSql + i + where + screenIdList.get(i);
			try {
				mDBOpenHelper.exec(tmpSql);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}

	}

	// add by huyong end 2011-03-17 for 清理桌面脏数据

	/**
	 * 保存主题包名
	 * 
	 * @author huyong
	 * @return
	 */
	public void saveThemeName(String themeName) {
		String table = ConfigTable.TABLENAME;
		String column = ConfigTable.THEMENAME;
		String sql = "update " + table + " set " + column + " = '" + themeName + "';";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取主题包名
	 * 
	 * @author huyong
	 * @return
	 */
	public String getThemeName() {
		String themeName = ThemeManager.DEFAULT_THEME_PACKAGE;
		String table = ConfigTable.TABLENAME;
		String columns[] = { ConfigTable.THEMENAME };
		Cursor cursor = null;
		try {
			cursor = mDBOpenHelper.query(table, columns, null, null, null);
			if (null != cursor && cursor.moveToFirst()) {
				int index = cursor.getColumnIndex(ConfigTable.THEMENAME);
				themeName = cursor.getString(index);
			}
		} catch (Exception e) {
			Log.i("dataprovider", "getThemeName() = " + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return themeName;
	}

	/**
	 * 保存显示提示次数
	 * 
	 * @author huyong
	 * @param showCount
	 */
	public void saveShowTipFrame(String curVersion) {
		String table = ConfigTable.TABLENAME;
		String column = ConfigTable.TIPFRAMETIMECURVERSION;
		String sql = "update " + table + " set " + column + " = '" + curVersion + "';";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void saveVersionCode(int curCode) {
		String table = ConfigTable.TABLENAME;
		String column = ConfigTable.VERSIONCODE;
		String sql = "update " + table + " set " + column + " = " + curCode + ";";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取显示提示次数
	 * 
	 * @author huyong
	 * @return
	 */
	public String getShowTipFrameCurVersion() {
		String curVersion = "novalue";
		String table = ConfigTable.TABLENAME;
		String columns[] = { ConfigTable.TIPFRAMETIMECURVERSION };
		Cursor cursor = mDBOpenHelper.query(table, columns, null, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ConfigTable.TIPFRAMETIMECURVERSION);
					curVersion = cursor.getString(index);
				}
			} finally {
				cursor.close();
			}
		}
		return curVersion;
	}

	public int getVersionCode() {
		int versionCode = 0;
		String table = ConfigTable.TABLENAME;
		String columns[] = { ConfigTable.VERSIONCODE };
		Cursor cursor = mDBOpenHelper.query(table, columns, null, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ConfigTable.VERSIONCODE);
					versionCode = cursor.getInt(index);
				}
			} finally {
				cursor.close();
			}
		}
		return versionCode;
	}

	// 字体数据表维护

	public FontBean getUsedFont() {
		FontBean bean = null;

		String table = UsedFontTable.TABLENAME;
		Cursor cursor = mDBOpenHelper.query(table, null, null, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					bean = new FontBean();
					bean.getValues(cursor);
				}
			} finally {
				cursor.close();
			}
		}

		return bean;
	}

	public void updateUsedFont(FontBean bean) {
		String table = UsedFontTable.TABLENAME;

		if (null == bean) {
			return;
		}

		ContentValues values = new ContentValues();
		bean.setValues(values);
		try {
			mDBOpenHelper.update(table, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void insertUsedFont(FontBean bean) {
		if (null == bean) {
			return;
		}

		String table = UsedFontTable.TABLENAME;
		ContentValues values = new ContentValues();
		bean.setValues(values);
		try {
			mDBOpenHelper.insert(table, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<FontBean> getAllFont() {
		ArrayList<FontBean> beans = null;

		String table = FontTable.TABLENAME;
		Cursor cursor = mDBOpenHelper.query(table, null, null, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					beans = new ArrayList<FontBean>();
					do {
						FontBean bean = new FontBean();
						bean.getValues(cursor);
						beans.add(bean);
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
		}

		return beans;
	}

	public void updateAllFont(ArrayList<FontBean> beans) {
		String table = FontTable.TABLENAME;
		clearTable(table);

		if (null == beans) {
			return;
		}

		int sz = beans.size();
		for (int i = 0; i < sz; i++) {
			FontBean bean = beans.get(i);
			if (null == bean) {
				continue;
			}
			ContentValues values = new ContentValues();
			bean.setValues(values);
			try {
				mDBOpenHelper.insert(table, values);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}

	// Go widget
	public Cursor getAllGoWidget() {
		return mDBOpenHelper.query(GoWidgetTable.TABLENAME, null, null, null, null);
	}

	public ArrayList<GoWidgetBaseInfo> getAllGoWidgetInfos() {
		final Cursor cursor = getAllGoWidget();
		ArrayList<GoWidgetBaseInfo> list = new ArrayList<GoWidgetBaseInfo>();
		if (cursor == null) {
			return list;
		}
		try {
			if (cursor.moveToFirst()) {
				do {
					GoWidgetBaseInfo info = new GoWidgetBaseInfo();
					try {
						info.readObject(cursor, GoWidgetTable.TABLENAME);
						if (sNeedReplaceWidget || !sHasReplaceAllGoStoreWidget) {
							replaceGoStoreWidget(info);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					list.add(info);
				} while (cursor.moveToNext());
			}
			if (sNeedReplaceWidget || !sHasReplaceAllGoStoreWidget) {
				PreferencesManager sharedPreferences = new PreferencesManager(mContext,
						IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
				sharedPreferences.putBoolean(LauncherEnv.ALREADY_REPLACE_OLD_WIDGET, false);
				sharedPreferences.putBoolean(LauncherEnv.ALREADY_REPLACE_ALL_GOSTORE_WIDGET, true);
				sharedPreferences.commit();
			}
		} catch (Exception e) {
			Log.e("gowidget", "getAllGoWidgetInfos error");
		} finally {
			cursor.close();
		}
		return list;
	}

	public static final String NAME_GO_STORE_43 = "gostorewidget";
	public static final String NAME_GO_STORE_42 = "gostorewidget42";
	public static final String NAME_GO_STORE_41_NEW = "gostore41widget";
	public static final String NAME_GO_STORE_41 = "gostorewidget41";
	public static final String NAME_APPGAME_41 = "appgamewidget41";
	/***
	 * 替换GOStroe 4*3和4*2   成新的4*1的 
	 */
	private void replaceGoStoreWidget(GoWidgetBaseInfo info) {
		if (info == null || info.mLayout == null) {
			return;
		}
		if (info.mLayout.equals(NAME_GO_STORE_43) || info.mLayout.equals(NAME_GO_STORE_42)
				|| info.mLayout.equals(NAME_GO_STORE_41_NEW)
				|| info.mLayout.equals(NAME_GO_STORE_41)) {
			info.mLayout = NAME_APPGAME_41;
			try {
				String sqlString = "update " + GoWidgetTable.TABLENAME + " set "
						+ GoWidgetTable.LAYOUT + " = '" + NAME_APPGAME_41 + "', "
						+ GoWidgetTable.THEME + " = '" + "" + "', " + GoWidgetTable.THEMEID + " = "
						+ "-1" + " where " + GoWidgetTable.WIDGETID + " = " + info.mWidgetId + ";";
				mDBOpenHelper.exec(sqlString);
				sqlString = null;
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String sqlString = "update " + PartToScreenTable.TABLENAME + " set "
						+ PartToScreenTable.SPANX + " = " + 4 + ", " + PartToScreenTable.SPANY
						+ " = " + 1 + " where " + PartToScreenTable.WIDGETID + " = "
						+ info.mWidgetId + ";";
				mDBOpenHelper.exec(sqlString);
				sqlString = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void deleteGoWidget(int widgetid) {
		try {
			String condition = GoWidgetTable.WIDGETID + " = " + widgetid;
			mDBOpenHelper.delete(GoWidgetTable.TABLENAME, condition, null);
			condition = null;
		} catch (Exception e) {
			Log.i("gowidget", "deleteGoWidget error, widget id = " + widgetid);
		}
	}

	public void deleteGoWidget(String pkgName) {
		try {
			String condition = GoWidgetTable.PACKAGE + " = '" + pkgName + "'";
			mDBOpenHelper.delete(GoWidgetTable.TABLENAME, condition, null);
			condition = null;
		} catch (Exception e) {
			Log.i("gowidget", "deleteGoWidget error, pkgName  = " + pkgName);
		}
	}

	public void addGoWidget(GoWidgetBaseInfo info) {
		try {
			// 排序条件
			String where = GoWidgetTable.WIDGETID + " = " + info.mWidgetId;
			Cursor cursor = mDBOpenHelper.query(GoWidgetTable.TABLENAME, null, where, null, null);
			boolean isExist = false;
			if (cursor != null) {
				try {
					isExist = cursor.getCount() > 0;
				} finally {
					cursor.close();
				}
			}

			if (!isExist) {
				ContentValues values = new ContentValues();
				info.writeObject(values, GoWidgetTable.TABLENAME);
				mDBOpenHelper.insert(GoWidgetTable.TABLENAME, values);
				values = null;
			}
		} catch (Exception e) {
			Log.i("gowidget", "addGoWidget error, widget info  = " + info.toString());
		}
	}

	public void updateGoWidgetTheme(GoWidgetBaseInfo info) {
		// update gowidget set theme='test', type=2 where widgetid = -102
		try {
			String sqlString = "update " + GoWidgetTable.TABLENAME + " set " + GoWidgetTable.THEME
					+ " = '" + info.mTheme + "', " + GoWidgetTable.THEMEID + " = " + info.mThemeId
					+ " where " + GoWidgetTable.WIDGETID + " = " + info.mWidgetId + ";";

			mDBOpenHelper.exec(sqlString);
			sqlString = null;
		} catch (Exception e) {
			Log.i("gowidget", "update theme failed, widgetid = " + info.mWidgetId + " theme = "
					+ info.mTheme);
		}
	}

	public int getScreenCount() {
		String tableName = ScreenTable.TABLENAME;
		Cursor cursor = mDBOpenHelper.query(tableName, null, null, null, null);
		if (null == cursor) {
			return 0;
		}
		int count = 0;
		try {
			count = cursor.getCount();
		} finally {
			cursor.close();
		}
		return count;
	}

	public Cursor getStatisticsAllDataInfos() {
		String table = StatisticsAppDataTable.TABLENAME;
		return mDBOpenHelper.query(table, null, null, null, null);
	}

	public void insertStatisticsAllDataInfos(ContentValues values) {
		String table = StatisticsAppDataTable.TABLENAME;
		try {
			mDBOpenHelper.insert(table, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void resetStatisticsAllDataInfos() {
		try {
			mDBOpenHelper.beginTransaction();
			// 首先清理掉数据
			String clearSql = "drop table if exists " + StatisticsAppDataTable.TABLENAME;
			mDBOpenHelper.exec(clearSql);

			mDBOpenHelper.exec(StatisticsAppDataTable.CREATETABLESQL);
			mDBOpenHelper.setTransactionSuccessful();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	public void addClickCntStatisticsAllDataInfos(String pkgName) {
		String changeValue = " + 1";
		String table = StatisticsAppDataTable.TABLENAME;
		String clickCount = StatisticsAppDataTable.CLICKCNT;
		String packageName = StatisticsAppDataTable.PKGNAME;
		String sql = "update " + table + " set " + clickCount + " = " + clickCount + changeValue
				+ " where " + packageName + " = '" + pkgName + "';";
		try {
			mDBOpenHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

	}

	public int getClickCntStatisticsAllDataInfos(String pkgName) {
		int count = 0;
		String table = StatisticsAppDataTable.TABLENAME;
		Cursor cur = null;
		try {
			cur = mDBOpenHelper.query(table, new String[] { StatisticsAppDataTable.CLICKCNT },
					StatisticsAppDataTable.PKGNAME + "=?", new String[] { pkgName }, null);
			if (cur.moveToNext()) {
				count = DbUtil.getInt(cur, StatisticsAppDataTable.CLICKCNT);
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		return count;
	}

	public int isExistStatisticsDataInfo(String pkgName) {
		int result = StatisticsAppsInfoData.ERROR_CODE_NO_SUCH_COLUMN;

		if (pkgName == null) {
			return result;
		}

		// 表名
		String table = StatisticsAppDataTable.TABLENAME;
		// 返回一列数据，增加查询效率。
		String columns[] = { StatisticsAppDataTable.PKGNAME };
		// 查询条件
		String selection = StatisticsAppDataTable.PKGNAME + " = '" + pkgName + "'";
		Cursor cursor = null;
		try {
			cursor = mDBOpenHelper.query(table, columns, selection, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					result = StatisticsAppsInfoData.ERROR_CODE_EXIST_SUCH_COLUMN;
				}
			} else {
				// cursor为null，表明内部有异常，需要当成无合理表结构来处理
				result = StatisticsAppsInfoData.ERROR_CODE_NO_SUCH_TABLE;
			}

		} catch (Exception e) {
			// TODO: handle exception
			result = StatisticsAppsInfoData.ERROR_CODE_NO_SUCH_TABLE;
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return result;
	}

	/**
	 * 獲取功能表內所有程序的數據，不包括文件夾
	 * 
	 * @return
	 */
	public Cursor getFunAppItemsNoFolder() {
		// 表名
		String tables = AppTable.TABLENAME;
		// 查询列数
		String columns[] = { AppTable.INDEX, AppTable.INTENT, AppTable.FOLDERID, AppTable.TITLE,
				AppTable.FOLDERICONPATH };

		// 排序条件
		String sortOrder = AppTable.INDEX + " ASC";
		// 查询条件
		String selection = AppTable.FOLDERID + " = 0 ";
		return mDBOpenHelper.query(tables, columns, null, null, sortOrder);
	}

	public void insertMessage(ContentValues values) {
		try {
			mDBOpenHelper.insert(MessageCenterTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void updateMessage(ContentValues values, String id) {
		String whereStr = MessageCenterTable.ID + " = " + id;
		try {
			mDBOpenHelper.update(MessageCenterTable.TABLENAME, values, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void deleteAllMessage() {
		try {
			mDBOpenHelper.delete(MessageCenterTable.TABLENAME, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void deleteMessage(String id) {
		String select = MessageCenterTable.ID + " = " + id;
		try {
			mDBOpenHelper.delete(MessageCenterTable.TABLENAME, select, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询消息中心数据
	 * 
	 * @return 数据
	 */
	public Cursor queryMessages() {
		return mDBOpenHelper.query(MessageCenterTable.TABLENAME, null, null, null, null);
	}

	/**
	 * 查询消息中心数据
	 * 
	 * @return 数据
	 */
	public Cursor queryReadedMessages() {
		String select = MessageCenterTable.READED + " =1 ";
		return mDBOpenHelper.query(MessageCenterTable.TABLENAME, null, select, null, null);
	}

	/**
	 * 查询消息中心数据： 需要弹框或通知的消息
	 * 
	 * @return
	 */
	public Cursor queryNeedShowMessages() {
		String select = MessageCenterTable.READED + " !=1 " + " AND " + MessageCenterTable.VIEWTYPE
				+ " !=1 ";
		return mDBOpenHelper.query(MessageCenterTable.TABLENAME, null, select, null, null);
	}

	/**
	 * 查询指定消息
	 * 
	 * @return
	 */
	public Cursor queryMessages(String id) {
		String select = MessageCenterTable.ID + " = " + id;
		return mDBOpenHelper.query(MessageCenterTable.TABLENAME, null, select, null, null);
	}

	/**
	 * 查询手势
	 * 
	 * @return 数据
	 */
	public Cursor queryDiyGestures() {
		// 排序条件
		String sortOrder = DiyGestureTable.ID + " DESC";
		return mDBOpenHelper.query(DiyGestureTable.TABLENAME, null, null, null, sortOrder);
	}

	public DatabaseHelper getmDBOpenHelper() {
		return mDBOpenHelper;
	}

	/**
	 * 把原数据库里的旧市场intent改为新市场intent
	 * 
	 * @param oldintent
	 * @param newIntent
	 *            author:rxq data:2012/3/8 原因：旧市场升级后改了componentName
	 */
	public void replaceOldMarketToNewMarket(String oldintent, String newIntent) {
		// NOTE:parttoscreen table
		String whereStr = PartToScreenTable.INTENT + " = " + "'" + oldintent + "'";
		ContentValues valuesScreenClick = new ContentValues();
		valuesScreenClick.put(PartToScreenTable.INTENT, newIntent);
		try {
			mDBOpenHelper.update(PartToScreenTable.TABLENAME, valuesScreenClick, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		// NOTE:shortcut table for click
		whereStr = ShortcutTable.INTENT + " = " + "'" + oldintent + "'";
		ContentValues valuesDockClick = new ContentValues();
		valuesDockClick.put(ShortcutTable.INTENT, newIntent);
		try {
			mDBOpenHelper.update(ShortcutTable.TABLENAME, valuesDockClick, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		// NOTE:shortcut table for gesture
		whereStr = ShortcutTable.UPINTENT + " = " + "'" + oldintent + "'";
		ContentValues valuesDockGesture = new ContentValues();
		valuesDockGesture.put(ShortcutTable.UPINTENT, newIntent);
		try {
			mDBOpenHelper.update(ShortcutTable.TABLENAME, valuesDockGesture, whereStr, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		// 会引起功能表两个市场图标
		// //NOTE:folder table
		// whereStr = FolderTable.INTENT + " = " + "'" + oldintent + "'";
		// ContentValues valuesFolder = new ContentValues();
		// valuesFolder.put(FolderTable.INTENT, newIntent);
		// mDBOpenHelper.update(FolderTable.TABLENAME, valuesFolder, whereStr,
		// null);
	}

	/**
	 * 关闭数据库，释放数据库资源
	 */
	public void close() {
		if (mDBOpenHelper != null) {
			try {
				mDBOpenHelper.close();
			} catch (Exception e) {
				// do nothing
				e.printStackTrace();
			}
		}
	}
	/**
	 * open数据库，使用外部可读模式
	 */
	public boolean openWithWorldReadable() {
		if (mDBOpenHelper != null) {
			try {
				return mDBOpenHelper.openDBWithWorldReadable();
			} catch (Exception e) {
				// do nothing
			}
		}
		return false;
	}
	/**
	 * open数据库，使用默认的MODE_PRIVATE模式
	 */
	public void openWithDefaultMode() {
		if (mDBOpenHelper != null) {
			try {
				close();
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public static void destroy() {
		if (sDataProviderSelf != null) {
			sDataProviderSelf.close();
		}
		sDataProviderSelf = null;
	}

	/**
	 * 当前shortcut,screen,folder包含dock功能表图标数目是否<=1
	 * 
	 * @return
	 */
	public boolean isTheLastDockAppdrawer() {
		Intent intent = AppIdentifier.createAppdrawerIntent();
		String appdrawerStr = intent.toUri(0);

		int count = 0;
		// 1:shortcuttable
		Cursor cursor = null;
		String selection = null;
		try {
			selection = ShortcutTable.INTENT + " = " + "'" + appdrawerStr + "'";
			cursor = mDBOpenHelper.query(ShortcutTable.TABLENAME, null, selection, null, null);
			if (cursor != null) {
				count += cursor.getCount();
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		// 2:parttoscreentable
		if (count <= 1) {
			try {
				selection = PartToScreenTable.INTENT + " = " + "'" + appdrawerStr + "'";
				cursor = mDBOpenHelper.query(PartToScreenTable.TABLENAME, null, selection, null,
						null);
				if (cursor != null) {
					count += cursor.getCount();
				}
			} catch (Exception e) {
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}
		}

		// 3:foldertable
		if (count <= 1) {
			try {
				selection = FolderTable.INTENT + " = " + "'" + appdrawerStr + "'";
				cursor = mDBOpenHelper.query(FolderTable.TABLENAME, null, selection, null, null);
				if (cursor != null) {
					count += cursor.getCount();
				}
			} catch (Exception e) {
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}
		}

		return count <= 1;
	}

	/**
	 * 功能表设置方法(封装了几种常见类型的设置值的接口，避免每次新加设置项都更新表结构)
	 * 
	 * @param key
	 *            设置项名称
	 * @param value
	 *            设置项值
	 */
	/******************************************************************************/
	// public void setAppFuncSetting(int key, int value){
	// AppFuncSettingTable.setIntValue(mDBOpenHelper, key, value);
	// }
	// public void setAppFuncSetting(int key,boolean value){
	// AppFuncSettingTable.setBooleanValue(mDBOpenHelper, key, value);
	// }
	// public void setAppFuncSetting(int key,String value){
	// AppFuncSettingTable.setStringValue(mDBOpenHelper, key, value);
	// }

	// public int getAppFuncSetting(int key,int defaultValue){
	// return AppFuncSettingTable.getIntValue(mDBOpenHelper, key, defaultValue);
	// }
	// public boolean getAppFuncSetting(int key,boolean defaultValue){
	// return AppFuncSettingTable.getBooleanValue(mDBOpenHelper, key,
	// defaultValue);
	// }
	// public String getAppFuncSetting(int key,String defaultValue){
	// return AppFuncSettingTable.getStringValue(mDBOpenHelper, key,
	// defaultValue);
	// }
	/******************************************************************************/

	/***
	 * 获取打开App的动画类型
	 * 
	 * @return
	 */
	public int getAppOpenType() {
		int openAppType = 0;
		String table = SettingTable.TABLENAME;
		String columns[] = { SettingTable.APPOPENTYPE };
		Cursor cursor = mDBOpenHelper.query(table, columns, null, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(SettingTable.APPOPENTYPE);
					if (index >= 0) {
						openAppType = cursor.getInt(index);
					}
				}
			} finally {
				cursor.close();
			}
		}
		return openAppType;
	}

	/***
	 * 获取是否第一次运行程序的标识
	 * 
	 * @return
	 */
	public boolean getFirstRunValue() {
		boolean isFirstRun = false;
		String table = SettingTable.TABLENAME;
		String columns[] = { SettingTable.FIRSTRUN };
		Cursor cursor = mDBOpenHelper.query(table, columns, null, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(SettingTable.FIRSTRUN);
					if (index >= 0) {
						isFirstRun = ConvertUtils.int2boolean(cursor.getInt(index));
					}
				}
			} finally {
				cursor.close();
			}
		}
		return isFirstRun;
	} // end getFirstRunValue

	/**
	 * 更新第一次运行程序的标识
	 * 
	 * @param value
	 */
	public void updateFirstRunValue(ContentValues values) {
		try {
			mDBOpenHelper.update(SettingTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	} // end updateFirstRunValue

	/**
	 * 添加通讯统计应用的Item
	 * 
	 * @param values
	 * @throws DatabaseException
	 */
	public void addNotificationAppItem(final ContentValues values) throws DatabaseException {
		mDBOpenHelper.insert(NotificationAppSettingTable.TABLENAME, values);
	}

	/**
	 * 删除通讯统计应用的Item
	 * 
	 * @param intent
	 * @throws DatabaseException
	 */
	public void delNotificationAppItem(final Intent intent) throws DatabaseException {
		String intentString = ConvertUtils.intentToString(intent);
		String selection = NotificationAppSettingTable.INTENT + " = '" + intentString + "'";
		mDBOpenHelper.delete(NotificationAppSettingTable.TABLENAME, selection, null);
	}

	/**
	 * 清空通讯统计应用的Item
	 * 
	 * @throws DatabaseException
	 */
	public void clearAllNotificationAppItems() throws DatabaseException {
		String selection = NotificationAppSettingTable.ISSELECTED + " = 1 ";
		mDBOpenHelper.delete(NotificationAppSettingTable.TABLENAME, selection, null);
	}

	/**
	 * 获取通讯统计应用的Items
	 * 
	 * @return 勾选为通讯统计应用的Items
	 */
	public Cursor getAllNotificationAppItems() {
		String selection = NotificationAppSettingTable.ISSELECTED + " = 1 ";
		return mDBOpenHelper.query(NotificationAppSettingTable.TABLENAME, null, selection, null,
				null);
	}

	/**
	 * <br>
	 * 功能简述:是否3.12之前的老用户 <br>
	 * 功能详细描述:过滤dock默认浏览器intent <br>
	 * 注意:
	 * 
	 * @return
	 */
	public boolean isVersionBefore312() {
		boolean isVersionBefore312 = false;

		String table = ConfigTable.TABLENAME;
		String columns[] = { ConfigTable.ISVERSIONBEFORE312 };
		Cursor cursor = mDBOpenHelper.query(table, columns, null, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ConfigTable.ISVERSIONBEFORE312);
					isVersionBefore312 = cursor.getInt(index) == 1;
				}
			} catch (Exception e) {
				// 不处理
			} finally {
				cursor.close();
			}
		}
		return isVersionBefore312;
	}

	/**
	 * <br>功能简述:用于在导入其他go桌面DB后修正默认的pkgName
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param srcPkg 源桌面的包名
	 */
	public void checkPkgNameForImportDB(String srcPkg) {
		String table = ShortcutTable.TABLENAME;
		mDBOpenHelper.beginTransaction();
		try {
			//***********************ShortcutTable begin************************//
			String sql = "update " + table + " set " + ShortcutTable.THEMENAME + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + ShortcutTable.THEMENAME + " = '"
					+ srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + ShortcutTable.USERICONPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + ShortcutTable.USERICONPACKAGE
					+ " = '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			//*************************ShortcutTable end***********************//
			//******************ScreenStyleConfigTable begin******************//
			table = ScreenStyleConfigTable.TABLENAME;
			sql = "update " + table + " set " + ScreenStyleConfigTable.THEMEPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + ScreenStyleConfigTable.THEMEPACKAGE
					+ " = '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + ScreenStyleConfigTable.ICONSTYLEPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where "
					+ ScreenStyleConfigTable.ICONSTYLEPACKAGE + "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + ScreenStyleConfigTable.FOLDERSTYLEPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where "
					+ ScreenStyleConfigTable.FOLDERSTYLEPACKAGE + "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + ScreenStyleConfigTable.GGMENUPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + ScreenStyleConfigTable.GGMENUPACKAGE
					+ "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + ScreenStyleConfigTable.INDICATOR + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + ScreenStyleConfigTable.INDICATOR
					+ "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			//*****************************ScreenStyleConfigTable end******************//
			//*****************************ScreenSettingTable begin******************//
			table = ScreenSettingTable.TABLENAME;
			sql = "update " + table + " set " + ScreenSettingTable.INDICATORSTYLEPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where "
					+ ScreenSettingTable.INDICATORSTYLEPACKAGE + "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);

			//******************************ScreenSettingTable end************************//
			//******************************PartToScreenTable end************************//
			table = PartToScreenTable.TABLENAME;
			sql = "update " + table + " set " + PartToScreenTable.USERICONPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + PartToScreenTable.USERICONPACKAGE
					+ "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			//******************************PartToScreenTable end************************//
			//******************************PartToScreenTable end************************//
			table = FolderTable.TABLENAME;
			sql = "update " + table + " set " + FolderTable.USERICONPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + FolderTable.USERICONPACKAGE + "= '"
					+ srcPkg + "';";
			mDBOpenHelper.exec(sql);
			//******************************PartToScreenTable end************************//
			//******************************DesktopTable end************************//
			table = DesktopTable.TABLENAME;
			sql = "update " + table + " set " + DesktopTable.THEMEICONPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + DesktopTable.THEMEICONPACKAGE + "= '"
					+ srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + DesktopTable.FOLDERTHEMEICONPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + DesktopTable.FOLDERTHEMEICONPACKAGE
					+ "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + DesktopTable.GGMENUTHEMEICONPACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + DesktopTable.GGMENUTHEMEICONPACKAGE
					+ "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			//******************************AppSettingTable end************************//
			table = AppSettingTable.TABLENAME;
			sql = "update " + table + " set " + AppSettingTable.THEMEPACKAGENAME + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + AppSettingTable.THEMEPACKAGENAME
					+ "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + AppSettingTable.TAB_HOME_BG + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + AppSettingTable.TAB_HOME_BG + "= '"
					+ srcPkg + "';";
			mDBOpenHelper.exec(sql);
			sql = "update " + table + " set " + AppSettingTable.INDICATOR_STYLE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + AppSettingTable.INDICATOR_STYLE
					+ "= '" + srcPkg + "';";
			mDBOpenHelper.exec(sql);
			//******************************AppFuncSettingTable begin************************//
			table = AppFuncSettingTable.TABLENAME;
			sql = "update " + table + " set " + AppFuncSettingTable.PKNAME + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + AppFuncSettingTable.PKNAME + "= '"
					+ srcPkg + "';";
			mDBOpenHelper.exec(sql);
			//***************************GoWidgetTable begin***********************************//
			table = GoWidgetTable.TABLENAME;
			sql = "update " + table + " set " + GoWidgetTable.PACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + GoWidgetTable.PACKAGE + "= '"
					+ srcPkg + "';";
			mDBOpenHelper.exec(sql);
			mDBOpenHelper.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}

	}

	/**
	 * <br>功能简述:修正DB内默认包名
	 * <br>功能详细描述:主要用于修正恢复不同包名的GO桌面数据库
	 * <br>注意:
	 */
	public void checkBackDB() {
		try {
			mDBOpenHelper.beginTransaction();
			String table = GoWidgetTable.TABLENAME;
			String sql = "update " + table + " set " + GoWidgetTable.PACKAGE + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + GoWidgetTable.PROTOTYPE + "!= "
					+ GoWidgetBaseInfo.PROTOTYPE_NORMAL + " AND " + GoWidgetTable.PACKAGE + " != '"
					+ LauncherEnv.PACKAGE_NAME + "';";
			mDBOpenHelper.exec(sql);

			table = ShortcutTable.TABLENAME;
			sql = "update " + table + " set " + ShortcutTable.THEMENAME + " = '"
					+ LauncherEnv.PACKAGE_NAME + "' where " + ShortcutTable.THEMENAME + " != '"
					+ LauncherEnv.PACKAGE_NAME + "';";
			mDBOpenHelper.exec(sql);
			mDBOpenHelper.setTransactionSuccessful();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			mDBOpenHelper.endTransaction();
		}
	}

	/**
	 * 获取所有多媒体数据（播放列表，播放列表文件，隐藏文件表）
	 * @return
	 */
	public ArrayList<Cursor> getAllMediaData() {
		Cursor playListCursor = mDBOpenHelper.query(MediaManagementPlayListTable.TABLENAME, null,
				null, null, null);
		Cursor playListFileCursor = mDBOpenHelper.query(MediaManagementPlayListFileTable.TABLENAME,
				null, null, null, null);
		Cursor hideFileCursor = mDBOpenHelper.query(MediaManagementHideTable.TABLENAME, null, null,
				null, null);
		ArrayList<Cursor> cursorList = new ArrayList<Cursor>();
		cursorList.add(playListCursor);
		cursorList.add(playListFileCursor);
		cursorList.add(hideFileCursor);
		return cursorList;
	}

	/**
	 * 删除所有多媒体数据（播放列表，播放列表文件，隐藏文件表）
	 * @return
	 */
	public boolean deleteAllMediaData() {
		boolean flag = false;
		try {
			mDBOpenHelper.delete(MediaManagementPlayListTable.TABLENAME, null, null);
			mDBOpenHelper.delete(MediaManagementPlayListFileTable.TABLENAME, null, null);
			mDBOpenHelper.delete(MediaManagementHideTable.TABLENAME, null, null);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public SQLiteOpenHelper getDatabaseHelper() {
		return mDBOpenHelper;
	}
}
