package com.jiubang.ggheart.apps.desks.dock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockViewUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DockItemControler;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.SelfAppItemInfoControler;
import com.jiubang.ggheart.data.SysShortCutControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ItemInfoFactory;
import com.jiubang.ggheart.data.info.SelfAppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.model.DataModel;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.data.tables.ShortcutSettingTable;
import com.jiubang.ggheart.data.tables.ShortcutTable;
import com.jiubang.ggheart.data.tables.ShortcutUnfitTable;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.DockBean;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * dock数据库访问类
 * 
 * @author ruxueqin
 * 
 */
//CHECKSTYLE:OFF
public class DockDataModel extends DataModel {
	private AppDataEngine mAppEngine; // 系统应用控制器
	private SysShortCutControler mSysShortCutControler; // 系统shortcut控制器
	private DockItemControler mDockItemControler; // dock特殊图标控制器
	private DeskThemeControler mDeskThemeControler; // 桌面主题控制器

	private DockBinder mDockBinder; // dock文件夹加载器

	public DockDataModel(Context context, AppDataEngine appEngine,
			SysShortCutControler sysShortCutControler) {
		super(context);

		mAppEngine = appEngine;
		mSysShortCutControler = sysShortCutControler;
		mDockItemControler = AppCore.getInstance().getDockItemControler();
		mDeskThemeControler = AppCore.getInstance().getDeskThemeControler();
		mDockBinder = new DockBinder(mContext, this);
	}

	/**
	 * 获取快捷栏中item项
	 * 
	 * @return
	 */
	public ArrayList<DockItemInfo> getShortCutItems(int rowId) {
		// 创建队列
		ArrayList<DockItemInfo> infos = new ArrayList<DockItemInfo>();
		// 读取Dock表数据
		Cursor cursor = mDataProvider.getRowShortCutItems(rowId, getThemeName());

		// 填充到数据Bean
		convertCursorToShortCutItem(cursor, infos);
		// 关联源数据
		contactOriginAppInfo(infos);

		return infos;
	}

	/**
	 * 获取非自适应模式的空白显示
	 * 
	 * @return
	 */
	public ConcurrentHashMap<Integer, ArrayList<Integer>> getShortCutUnfitBlanks() {
		ConcurrentHashMap<Integer, ArrayList<Integer>> blanks = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < DockUtil.TOTAL_ROWS; i++) {
			ArrayList<Integer> list = new ArrayList<Integer>();

			Cursor cursor = mDataProvider.getRowShortCutUnfitBlank(i);

			try {
				if (cursor.moveToFirst()) {
					int columnIndex = cursor.getColumnIndexOrThrow(ShortcutUnfitTable.INDEX);
					do {
						int index = cursor.getInt(columnIndex);
						list.add(index);
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
			} finally {
				if (null != cursor) {
					cursor.close();
					cursor = null;
				}
			}
			blanks.put(i, list);
		}
		return blanks;
	}

	/**
	 * 填充到数据Bean
	 * 
	 * @param cursor
	 * @param itemInfos
	 */
	private void convertCursorToShortCutItem(final Cursor cursor, ArrayList<DockItemInfo> itemInfos) {
		if (cursor == null) {
			return;
		}
		final int shortCutRowId = cursor.getColumnIndexOrThrow(ShortcutTable.ROWSID);
		final int shortCutIndex = cursor.getColumnIndexOrThrow(ShortcutTable.MINDEX);
		final int usePackageIndex = cursor.getColumnIndexOrThrow(ShortcutTable.USEPACKAGE);
		final int typeIndex = cursor.getColumnIndexOrThrow(ShortcutTable.ITEMTYPE);

		if (cursor.moveToFirst()) {
			do {
				int itemtype = cursor.getInt(typeIndex);
				DockItemInfo itemInfo = new DockItemInfo(itemtype,
						DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW));

				itemInfo.readObject(cursor, ShortcutTable.TABLENAME);

				itemInfo.setmRowId(cursor.getInt(shortCutRowId));
				itemInfo.setmIndexInRow(cursor.getInt(shortCutIndex));
				itemInfo.mIndex = cursor.getInt(shortCutIndex);
				itemInfo.mUsePackage = cursor.getString(usePackageIndex);

				// TODO 特殊处理
				// 数据库升级遗留问题（dock +号图标 数据问题）
				// 1. 没有Intent
				// 2. IconType 是默认
				// 改为 + 资源
				if (((itemInfo.mItemInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION || itemInfo.mItemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) && null == ((ShortCutInfo) itemInfo.mItemInfo).mIntent)
						&& ImagePreviewResultType.TYPE_DEFAULT == itemInfo.mItemInfo.mFeatureIconType) {
					itemInfo.mItemInfo.mFeatureIconType = ImagePreviewResultType.TYPE_PACKAGE_RESOURCE;
					itemInfo.mItemInfo.mFeatureIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
				}

				// 判断是否文件夹
				if (itemInfo.mItemInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
					final ArrayList<ItemInfo> contents = getDockFolderItems(
							((UserFolderInfo) itemInfo.mItemInfo).mInScreenId, -1, true);
					if (contents != null) {
						((UserFolderInfo) itemInfo.mItemInfo).addAll(contents);
						for (ItemInfo item : contents) {
							item.registerObserver(itemInfo); // 注册文件夹里面的图标
						}
						contents.clear();
					}
				}
				itemInfos.add(itemInfo);
			} while (cursor.moveToNext());
		}

		// 图标大小
		int size = itemInfos.size();
		boolean autofit = GoSettingControler.getInstance(mContext).getShortCutSettingInfo().mAutoFit;
		int iconsize = autofit ? DockUtil.getIconSize(size) : DockUtil
				.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
		for (int i = 0; i < size; i++) {
			DockItemInfo info = itemInfos.get(i);
			info.setBmpSize(iconsize);
		}

		cursor.close();
	}

	/**
	 * 刷新文件夹图标
	 * 
	 * @param dockItemInfo
	 *            dock文件信息
	 * @param checkDel
	 *            刷新完后是否要检查删除文件夹
	 */
	public void updateFolderIconAsync(DockItemInfo dockItemInfo, int type, boolean checkDel) {
		if (null != mDockBinder && null != dockItemInfo) {
			mDockBinder.updateFolderIconAsync(dockItemInfo, type, checkDel);
		}
	}

	/**
	 * 
	 * @param itemIntent
	 * @param getDefaultIcon
	 *            是否获取dock栏的默认图标
	 * @return
	 */
	public BitmapDrawable getOriginalIcon(final ShortCutInfo itemInfo) {
		if (null == itemInfo) {
			return null;
		}

		AppItemInfo info = getContactAppInfo(itemInfo);
		if (null != info) {
			return info.mIcon;
		}

		return null;

	}

	public void contactOriginAppInfo(ArrayList<DockItemInfo> itemInfos) {
		if (null == itemInfos) {
			return;
		}

		int sz = itemInfos.size();
		for (int i = 0; i < sz; i++) {
			DockItemInfo item = itemInfos.get(i);
			if (null == item) {
				continue;
			}
			try {
				if (item.mItemInfo instanceof ShortCutInfo) {
					AppItemInfo appInfo = getContactAppInfo(item);
					item.mItemInfo.setRelativeItemInfo(appInfo);
				}

				prepareItemInfo(item.mItemInfo);
			} catch (Exception e) {

			}
		}
	}

	// 找到关联应用
	// 1. 快捷方式
	// 2. 应用程序
	private AppItemInfo getContactAppInfo(final ShortCutInfo itemInfo) {
		if (null == itemInfo) {
			return null;
		}

		AppItemInfo info = null;

		if (itemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			if (null != mDockItemControler) {
				info = mDockItemControler.getDockAppItemInfo(itemInfo.mIntent);
			}
			if (null == info && null != mSysShortCutControler) {
				info = mSysShortCutControler.getSysShortCutItemInfo(itemInfo.mIntent);
			}
		} else if (itemInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION && null != mAppEngine) {
			info = mAppEngine.getCompletedAppItem(itemInfo.mIntent);
		}

		return info;
	}

	// 找到关联应用
	// 1. 快捷方式
	// 2. 应用程序
	private AppItemInfo getContactAppInfo(DockItemInfo dockItemInfo) {
		if (null == dockItemInfo || null == dockItemInfo.mItemInfo
				|| !(dockItemInfo.mItemInfo instanceof ShortCutInfo)) {
			return null;
		}

		boolean needUpdate = false;
		ShortCutInfo shortCutInfo = (ShortCutInfo) dockItemInfo.mItemInfo;
		AppItemInfo info = null;
		if (shortCutInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			if (null != mDockItemControler) {
				info = mDockItemControler.getDockAppItemInfo(shortCutInfo.mIntent);
			}
			if (null == info && null != mSysShortCutControler) {
				info = mSysShortCutControler.getSysShortCutItemInfo(shortCutInfo.mIntent);
			}
			if (null == info && null != mAppEngine) {
				// 在ITEM_TYPE_SHORTCUT内仍然找mAppEngine.getCompletedAppItem原因是
				// 有些用户的itemInfo.mItemType类型不正确
				info = mAppEngine.getCompletedAppItem(shortCutInfo.mIntent);
				if (null != info) {
					shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
					needUpdate = true;
				}
			}
		} else if (shortCutInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION && null != mAppEngine) {
			info = mAppEngine.getCompletedAppItem(shortCutInfo.mIntent);
			if (null == info && null != mSysShortCutControler) {
				info = mSysShortCutControler.getSysShortCutItemInfo(shortCutInfo.mIntent);
				if (null != info) {
					shortCutInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
					needUpdate = true;
				}
			}
		}
		if (needUpdate) {
			// 原来数据类型有错误，修改更新数据库
			updateDockItem(dockItemInfo.mItemInfo.mInScreenId, dockItemInfo);
		}

		return info;
	}

	public String getShortCutTitle(final ShortCutInfo itemInfo) {
		String title = null;
		AppItemInfo info = getContactAppInfo(itemInfo);
		if (null != info) {
			title = info.mTitle;
		}

		return title;
	}

	/**
	 * 是否需要初始化
	 * 
	 * @return
	 */
	public boolean checkNeedInit() {
		Cursor cursor = mDataProvider.getRowShortCutItems(-1, null);
		boolean result = true;
		if (cursor != null) {
			result = cursor.getCount() < 1;
			cursor.close();
		}
		return result;
	}

	// TODO 手势目前没有在Add参数中
	// 需要确认现在的手势是那种手势
	/**
	 * 初始化Dock条的5个默认应用图标和10个推荐应用
	 */
	public void initShortcutItem() {
		long time = System.currentTimeMillis();
		insertDefaultShortCut(time);
		insertRecommendShortCut(time);

		// 加入假数据，用于判断是否初次安装
		addShotcutItem(-1, -1, -1, -1, -1, null, -1, null, null, null, null, null, -1);
	}

	/***
	 * 初始化5个默认图标
	 * 
	 * @param time
	 */
	private void insertDefaultShortCut(long time) {
		PackageManager packageManager = mContext.getPackageManager();

		// 前5个 自带应用程序
		for (int i = 0; i < 5; i++) {
			DockItemInfo info1 = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT,
					DockUtil.ICON_COUNT_IN_A_ROW);
			((ShortCutInfo) info1.mItemInfo).mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
			((ShortCutInfo) info1.mItemInfo).mItemType = IItemType.ITEM_TYPE_SHORTCUT;
			switch (i) {
				case 0 :
					((ShortCutInfo) info1.mItemInfo).mIntent = AppIdentifier
							.createSelfDialIntent(mContext);

					((ShortCutInfo) info1.mItemInfo).mFeatureIconPath = "shortcut_0_0_phone";
					((ShortCutInfo) info1.mItemInfo).mFeatureTitle = GOLauncherApp.getContext()
							.getString(R.string.customname_dial);
					break;

				case 1 :
					((ShortCutInfo) info1.mItemInfo).mIntent = AppIdentifier
							.createSelfContactIntent(mContext);

					((ShortCutInfo) info1.mItemInfo).mFeatureIconPath = "shortcut_0_1_contacts";
					((ShortCutInfo) info1.mItemInfo).mFeatureTitle = GOLauncherApp.getContext()
							.getString(R.string.customname_contacts);
					break;

				case 2 :
					((ShortCutInfo) info1.mItemInfo).mIntent = new Intent(
							ICustomAction.ACTION_SHOW_FUNCMENU);

					((ShortCutInfo) info1.mItemInfo).mFeatureIconPath = "shortcut_0_2_funclist";
					((ShortCutInfo) info1.mItemInfo).mFeatureTitle = GOLauncherApp.getContext()
							.getString(R.string.customname_Appdrawer);
					break;

				case 3 :
					((ShortCutInfo) info1.mItemInfo).mIntent = AppIdentifier
							.createSelfMessageIntent();

					((ShortCutInfo) info1.mItemInfo).mFeatureIconPath = "shortcut_0_3_sms";
					((ShortCutInfo) info1.mItemInfo).mFeatureTitle = GOLauncherApp.getContext()
							.getString(R.string.customname_sms);
					break;

				case 4 :
					((ShortCutInfo) info1.mItemInfo).mIntent = AppIdentifier
							.createSelfBrowseIntent(packageManager);

					((ShortCutInfo) info1.mItemInfo).mFeatureIconPath = "shortcut_0_4_browser";
					((ShortCutInfo) info1.mItemInfo).mFeatureTitle = null;
					break;

				default :
					break;
			}
			((ShortCutInfo) info1.mItemInfo).mInScreenId = time + i;
			((ShortCutInfo) info1.mItemInfo).mFeatureIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
			addShotcutItem(((ShortCutInfo) info1.mItemInfo).mInScreenId, 0, i,
					ImagePreviewResultType.TYPE_PACKAGE_RESOURCE,
					((ShortCutInfo) info1.mItemInfo).mFeatureIconType, info1.mUsePackage,
					((ShortCutInfo) info1.mItemInfo).mFeatureIconId,
					((ShortCutInfo) info1.mItemInfo).mFeatureIconPackage,
					((ShortCutInfo) info1.mItemInfo).mFeatureIconPath,
					((ShortCutInfo) info1.mItemInfo).mFeatureTitle,
					((ShortCutInfo) info1.mItemInfo).mIntent.toUri(0), getThemeName(),
					((ShortCutInfo) info1.mItemInfo).mItemType);
		}
	}

	/***
	 * 初始化5个推荐图标
	 * 
	 * @param time
	 */
	private void insertRecommendShortCut(long time) {
		// dock3.0 NOTE:初始化推荐app
		int addCount = 0;
		ArrayList<DockItemInfo> initDockItemInfos = DockViewUtil.getInitDockData();
		int infosize = initDockItemInfos.size();
		if (infosize > 0) {
			for (int j = 1; j < 3; j++) {
				for (int k = 0; k < 5; k++) {
					DockItemInfo info = initDockItemInfos.get(addCount);
					info.mItemInfo.mInScreenId = time + (j * 5 + k);
					addShotcutItem(info.mItemInfo.mInScreenId, j, k,
							((ShortCutInfo) info.mItemInfo).mFeatureIconType,
							((ShortCutInfo) info.mItemInfo).mFeatureIconType, info.mUsePackage,
							((ShortCutInfo) info.mItemInfo).mFeatureIconId,
							((ShortCutInfo) info.mItemInfo).mFeatureIconPackage,
							((ShortCutInfo) info.mItemInfo).mFeatureIconPath,
							((ShortCutInfo) info.mItemInfo).mFeatureTitle,
							((ShortCutInfo) info.mItemInfo).mIntent.toUri(0), getThemeName(),
							((ShortCutInfo) info.mItemInfo).mItemType);
					addCount++;
					if (addCount >= infosize) {
						initDockItemInfos.clear();
						initDockItemInfos = null;
						return;
					}
				}
			}
		}
	}

	private String getThemeName() {
		// TODO 1.5版本暂时不按主题分数据库
		return ThemeManager.DEFAULT_THEME_PACKAGE;
	}

	public String getThemeName1() {
		if (null != mDeskThemeControler) {
			return mDeskThemeControler.getDeskThemeBean().getPackageName();
		}
		return null;
	}

	public DeskThemeBean.ThemeDefualtItem getThemeDefualtItem(int index) {
		DeskThemeBean.ThemeDefualtItem retItem = null;
		if (null != mDeskThemeControler) {
			List<DeskThemeBean.ThemeDefualtItem> items = mDeskThemeControler.getDeskThemeBean().mDock.mThemeDefualt;
			if (null != items) {
				int sz = items.size();
				for (int i = 0; i < sz; i++) {
					DeskThemeBean.ThemeDefualtItem item = items.get(i);
					if (null != item) {
						if (item.mIndex == index) {
							retItem = item;
							break;
						}
					}
				}

			}
		}
		return retItem;
	}

	public DeskThemeBean.SystemDefualtItem getDockThemeAddItem() {
		if (null != mDeskThemeControler) {
			return mDeskThemeControler.getDeskThemeBean().mDock.mNoApplicationIcon;
		}
		return null;
	}

	/**
	 * 从某一主题获得+号
	 * 
	 * @return
	 */
	public DeskThemeBean.SystemDefualtItem getDockThemeAddItem(String themePkg) {
		if (null == themePkg) {
			return null;
		}

		DockBean bean = DockChangeIconControler.getInstance(mContext).getDockBean(themePkg);
		return bean.mNoApplicationIcon;
	}

	public void addShotcutItem(long itemId, int rowid, int shortCutIndex, int userIcontype,
			int iconType, String usePackage, int userIconid, String userIconpackage,
			String userIconpath, String usertitle, String intentString, String themeName, int type) {
		// TODO：删除该之前索引对应的item信息
		ContentValues contentValues = new ContentValues();
		contentValues.put(ShortcutTable.PARTID, itemId);
		contentValues.put(ShortcutTable.ROWSID, rowid);
		contentValues.put(ShortcutTable.MINDEX, shortCutIndex);
		contentValues.put(ShortcutTable.USERICONTYPE, userIcontype);
		contentValues.put(ShortcutTable.ICONTYPE, iconType);
		contentValues.put(ShortcutTable.USEPACKAGE, usePackage);
		contentValues.put(ShortcutTable.USERICONID, userIconid);
		contentValues.put(ShortcutTable.USERICONPATH, userIconpath);
		contentValues.put(ShortcutTable.USERICONPACKAGE, userIconpackage);
		contentValues.put(ShortcutTable.USERTITLE, usertitle);
		contentValues.put(ShortcutTable.INTENT, intentString);
		contentValues.put(ShortcutTable.THEMENAME, themeName);
		contentValues.put(ShortcutTable.ITEMTYPE, type);
		mDataProvider.addShortcutItem(contentValues);
	}

	public void deleteShortcutItem(long id) {
		mDataProvider.deleteShortcutItem(id);
	}

	/**
	 * <br>功能简述:删除shortcuttable里指定intent所有数据项
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intentStr
	 */
	public void deleteShortcutItems(String intentStr) {
		mDataProvider.deleteShortcutItems(intentStr);
	}

	/**
	 * 清理快捷条表数据
	 */
	public void cleanShortCutItem() {
		mDataProvider.clearTable(ShortcutTable.TABLENAME);
		mDataProvider.clearTable(ShortcutUnfitTable.TABLENAME);
	}

	public boolean updateShortCutSettingStyle(String themeName, String style) {
		ContentValues values = new ContentValues();
		values.put(ShortcutSettingTable.STYLE_STRING, style);

		mDataProvider.updateShortCutSetting(values, themeName);
		return true;
	}

	public boolean updateShortCutSettingBgSwitch(String themeName, boolean isOn) {
		ContentValues values = new ContentValues();
		values.put(ShortcutSettingTable.BGPICSWITCH, ConvertUtils.boolean2int(isOn));

		mDataProvider.updateShortCutSetting(values, themeName);
		return true;
	}

	public boolean updateShortCutSettingCustomBgSwitch(String themeName, boolean isOn) {
		ContentValues values = new ContentValues();
		values.put(ShortcutSettingTable.CUSTOMBGPICSWITCH, ConvertUtils.boolean2int(isOn));

		mDataProvider.updateShortCutSetting(values, themeName);
		return true;
	}

	/**
	 * 更新DOCK背景相关信息,针对主题修改
	 * 
	 * @param useThemeName
	 *            修改设置针对的主题名
	 * @param targetThemeName
	 *            选择图片是哪个主题包的资源
	 * @param isCustomPic
	 *            是否用户自定义图片（裁减）
	 * @param resName
	 *            如果非用户自定义图片，存为资源名；如果是用户自定义图片，存文件路径名
	 */
	public boolean updateShortCutBG(String useThemeName, String targetThemeName, String resName,
			boolean isCustomPic) {
		ContentValues values = new ContentValues();

		values.put(ShortcutSettingTable.BG_TARGET_THEME_NAME, targetThemeName);
		values.put(ShortcutSettingTable.BG_RESNAME, resName);
		values.put(ShortcutSettingTable.CUSTOM_PIC_OR_NOT, ConvertUtils.boolean2int(isCustomPic));

		mDataProvider.updateShortCutSetting(values, useThemeName);
		return true;
	}

	public boolean updateShortCutSettingEnable(boolean bool) {
		ContentValues values = new ContentValues();

		values.put(ShortcutSettingTable.ENABLE, ConvertUtils.boolean2int(bool));
		mDataProvider.updateShortCutSetting(values);
		return true;
	}

	public AppItemInfo getAppItemInfo(Intent intent) {
		if (null != mAppEngine) {
			return mAppEngine.getCompletedAppItem(intent);
		}
		return null;
	}

	/**
	 * 修改数据库：dockItem　
	 * 
	 * @param id
	 * @param index
	 *            所在行的第几个位置
	 */
	public void updateDockItemIndex(long id, int index) {
		ContentValues values = new ContentValues();
		values.put(ShortcutTable.MINDEX, index);
		mDataProvider.updateShortCutItem(id, values, getThemeName());
	}

	/**
	 * 修改数据库：dockItem　
	 * 
	 * @param info
	 */
	public void updateDockItem(long id, DockItemInfo info) {
		ContentValues values = new ContentValues();
		info.writeObject(values, ShortcutTable.TABLENAME);
		mDataProvider.updateShortCutItem(id, values, getThemeName());
	}

	public void addDockItem(DockItemInfo info) {
		ContentValues values = new ContentValues();
		info.writeObject(values, ShortcutTable.TABLENAME);
		mDataProvider.addShortcutItem(values);
	}

	/**
	 * 增加文件夹项
	 * 
	 * @param folderId
	 * @param intent
	 * @param timeInFolder
	 *            放进文件夹的时间（一次记录不会变更的）
	 */
	public void addItemToFolder(ItemInfo screenItem, long folderId, int index, boolean fromDrawer) {
		ContentValues values = new ContentValues();
		screenItem.writeObject(values, FolderTable.TABLENAME);
		values.put(FolderTable.FOLDERID, folderId);
		values.put(FolderTable.INDEX, index);
		values.put(FolderTable.FROMAPPDRAWER, (fromDrawer ? 1 : 0));
		final long time = System.currentTimeMillis();
		values.put(FolderTable.TIMEINFOLDER, time);
		mDataProvider.addFolderItem(values);
		if (screenItem instanceof ShortCutInfo) {
			((ShortCutInfo) screenItem).mTimeInFolder = time;
		}
	}

	/**
	 * 修改数据库：删除文件夹内的一个item　
	 * 
	 * @param info
	 */
	public void removeDockFolderItem(long itemId, long folderId) {
		mDataProvider.removeAppFromFolder(itemId, folderId);
	}

	/**
	 * 删除文件夹
	 * 
	 * @param folderId
	 */
	public void removeDockFolder(long folderId) {
		mDataProvider.delScreenFolderItems(folderId);
	}

	/**
	 * 获取Folder列表
	 * 
	 * @param folderId
	 * @return
	 */
	public ArrayList<ItemInfo> getDockFolderItems(long folderId, int count, boolean prepare) {
		ArrayList<ItemInfo> folderItemList = new ArrayList<ItemInfo>();

		Cursor cursor = mDataProvider.getScreenFolderItems(folderId);
		convertCursorToFolderItem(cursor, folderItemList, count);

		if (prepare) {
			prepareItemInfo(folderItemList);
		}

		return folderItemList;
	}

	public ArrayList<ItemInfo> getDockFolderItemsFromDB(UserFolderInfo userFolderInfo, int count,
			boolean prepare) {
		synchronized (userFolderInfo) {
			/**
			 * ADT-3723 非必现：桌面文件夹消失，有消失动画 修改方法：对userFolderInfo加锁
			 */
			return getDockFolderItems(userFolderInfo.mInScreenId, count, prepare);
		}
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

	public boolean prepareItemInfo(ItemInfo info) {
		boolean bRet = false;
		if (null == info) {
			return bRet;
		}

		// 关联
		// 图标、名称
		switch (info.mItemType) {
			case IItemType.ITEM_TYPE_APPLICATION :
			case IItemType.ITEM_TYPE_SHORTCUT : {
				ShortCutInfo sInfo = (ShortCutInfo) info;
				if (null == sInfo.getRelativeItemInfo()) {
					bRet |= sInfo.setRelativeItemInfo(getAppItemInfo(sInfo.mIntent, null,
							sInfo.mItemType));
				} else if (sInfo.getRelativeItemInfo() instanceof SelfAppItemInfo) {
					AppItemInfo appItemInfo = getAppItemInfo(sInfo.mIntent, null, sInfo.mItemType);
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

			case IItemType.ITEM_TYPE_USER_FOLDER : {
				UserFolderInfo fInfo = (UserFolderInfo) info;
				if (null == fInfo.getRelativeItemInfo()) {
					bRet |= fInfo.setRelativeItemInfo(getAppItemInfo(null, null, fInfo.mItemType));
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
				} else {
					if (null != fInfo.getRelativeItemInfo()) {
						fInfo.mTitle = fInfo.getRelativeItemInfo().getTitle();
					}
				}
			}
				break;

			default :
				break;
		}
		return bRet;
	}

	private AppItemInfo getAppItemInfo(Intent intent, Uri uri, int type) {
		AppDataEngine appEngine = GOLauncherApp.getAppDataEngine();
		SysShortCutControler shortcutEngine = AppCore.getInstance().getSysShortCutControler();
		SelfAppItemInfoControler selfAppEngine = AppCore.getInstance()
				.getSelfAppItemInfoControler();
		DockItemControler dockItemControler = AppCore.getInstance().getDockItemControler();

		AppItemInfo info = null;
		switch (type) {
			case IItemType.ITEM_TYPE_APPLICATION :
				info = appEngine.getAppItem(intent);
				break;

			case IItemType.ITEM_TYPE_SHORTCUT :
				if (null != dockItemControler) {
					info = dockItemControler.getDockAppItemInfo(intent);
				}
				if (null == info && null != shortcutEngine) {
					info = shortcutEngine.getSysShortCutItemInfo(intent);
				}
				break;

			case IItemType.ITEM_TYPE_USER_FOLDER :
				if (null != selfAppEngine) {
					info = selfAppEngine.getUserFolder();
				}
				break;

			default :
				break;
		}

		if (null == info && null != selfAppEngine) {
			info = selfAppEngine.getDefaultApplication();
		}
		return info;
	}

	public void clearBinder() {
		if (null != mDockBinder) {
			mDockBinder.cancel();
			mDockBinder = null;
		}
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
	 * 非自适应模式，添加空白
	 * 
	 * @param rowid
	 * @param indexinrow
	 * @return
	 */
	public boolean addBlank(int rowid, int indexinrow) {
		String table = ShortcutUnfitTable.TABLENAME;
		ContentValues values = new ContentValues();
		values.put(ShortcutUnfitTable.ROWID, rowid);
		values.put(ShortcutUnfitTable.INDEX, indexinrow);
		values.put(ShortcutUnfitTable.INTENT, ICustomAction.ACTION_BLANK);
		return mDataProvider.addRecord(table, values);
	}

	/**
	 * 删除空白
	 * 
	 * @param rowid
	 * @param indexinrow
	 * @return
	 */
	public boolean delBlank(int rowid, int indexinrow) {
		String table = ShortcutUnfitTable.TABLENAME;
		String selection = ShortcutUnfitTable.ROWID + " = " + rowid + " AND "
				+ ShortcutUnfitTable.INDEX + " = " + indexinrow;
		return mDataProvider.delRecord(table, selection);
	}
}
