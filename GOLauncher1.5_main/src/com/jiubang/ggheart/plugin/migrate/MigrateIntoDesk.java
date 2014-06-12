package com.jiubang.ggheart.plugin.migrate;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.go.util.ConvertUtils;
import com.go.util.Utilities;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.SysFolderControler;
import com.jiubang.ggheart.data.SysShortCutControler;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.data.tables.PartToScreenTable;
import com.jiubang.ggheart.data.tables.ScreenTable;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 获取数据 插入数据库 通知桌面重新加载
 */
public class MigrateIntoDesk {
	private Context mContext;
	private Uri mMigrateUri;
	private boolean mCleanSelfEmptyScreen;

	public MigrateIntoDesk(Context context, Uri migrateUri, boolean cleanFlag) {
		mContext = context;
		mMigrateUri = migrateUri;
		mCleanSelfEmptyScreen = /* cleanFlag */false;
	}

	public void startMigrate() {
		new Thread(ThreadName.MIGRATEINTODESK_STARTMIGRARE) {
			@Override
			public void run() {
				ArrayList<DeskItemData> deskItems = null;
				try {
					deskItems = migrate();
				} catch (Exception e) {
					deskItems = null;
				}

				if (null == deskItems) {
					return;
				}

				int sz = deskItems.size();
				if (sz > 0) {
					DataProvider dataProvider = DataProvider.getInstance(mContext);
					SysShortCutControler sysShortCutControler = AppCore.getInstance()
							.getSysShortCutControler();
					SysFolderControler sysFolderControler = AppCore.getInstance()
							.getSysFolderControler();

					// 屏幕
					int count = dataProvider.getScreenCount();
					int screenCount = 0;
					for (int i = 0; i < sz; i++) {
						DeskItemData data = deskItems.get(i);
						if (null == data) {
							continue;
						}
						if (data.mScreen >= screenCount) {
							screenCount = data.mScreen + 1;
						}

						// 屏幕保护
						// 搬迁桌面屏幕索引错乱，避免主程序崩溃
						// 搬迁桌面屏幕索引最大为20
						int maxScreenCount = 20;
						if (screenCount > maxScreenCount) {
							return;
						}
					}
					long baseId = System.currentTimeMillis();
					for (int i = 0; i < screenCount; i++) {
						ContentValues contentValues = new ContentValues();
						contentValues.put(ScreenTable.SCREENID, baseId + i);
						contentValues.put(ScreenTable.MINDEX, count + i);
						dataProvider.addScreen(contentValues);
					}

					// 删除屏幕标记
					// 已有屏幕
					// 第一次运行，删除空屏
					// 非 不执行删除操作
					// 追加屏幕
					// 删除空屏

					boolean[] screenFlag = new boolean[count + screenCount];
					if (mCleanSelfEmptyScreen) {
						screenFlag[1] = true;
						screenFlag[2] = true;
					} else {
						for (int i = 0; i < count; i++) {
							screenFlag[i] = true;
						}
					}

					// 数据转化
					// 快捷方式
					// 系统文件夹
					// 文件夹
					for (int i = 0; i < sz; i++) {
						DeskItemData data = deskItems.get(i);
						if (null == data) {
							continue;
						}
						if (0 == data.mItemType) {
							// 转化类型
							data.mItemType = IItemType.ITEM_TYPE_APPLICATION;
						} else if (1 == data.mItemType) // 快捷方式
						{
							// 转化类型
							data.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
							if (null != sysShortCutControler) {
								Intent intent = ConvertUtils.stringToIntent(data.mIntent);
								BitmapDrawable icon = getIcon(data);
								sysShortCutControler.addSysShortCut(intent, data.mTitle, icon);
							}
						} else if (2 == data.mItemType) {
							// 转化类型
							data.mItemType = IItemType.ITEM_TYPE_USER_FOLDER;
						} else if (3 == data.mItemType) // 系统文件夹
						{
							// 转化类型
							data.mItemType = IItemType.ITEM_TYPE_LIVE_FOLDER;
							if (null != sysFolderControler) {
								Intent intent = ConvertUtils.stringToIntent(data.mIntent);
								Uri uri = ConvertUtils.stringToUri(data.mUri);
								BitmapDrawable icon = getIcon(data);
								sysFolderControler.addSysFolder(intent, uri, data.mDisplayMode,
										data.mTitle, icon);
							}
						}

						if (data.mContainer < 0) // 桌面
						{
							ContentValues values = new ContentValues();
							values.put(PartToScreenTable.ID, data.mId + baseId);
							values.put(PartToScreenTable.SCREENID, data.mScreen + baseId);
							values.put(PartToScreenTable.PARTID, 0);
							values.put(PartToScreenTable.SCREENX, data.mCellX);
							values.put(PartToScreenTable.SCREENY, data.mCellY);
							values.put(PartToScreenTable.SPANX, data.mSpanX);
							values.put(PartToScreenTable.SPANY, data.mSpanY);
							values.put(PartToScreenTable.USERTITLE, data.mTitle);
							values.put(PartToScreenTable.ITEMTYPE, data.mItemType);
							// values.put(PartToScreenTable.FOLDERID,
							// mFolderId);
							// values.put(PartToScreenTable.FOLDERINDEX,
							// mFolderIndex);
							values.put(PartToScreenTable.WIDGETID, data.mWidgetId);
							values.put(PartToScreenTable.INTENT, data.mIntent);
							values.put(PartToScreenTable.URI, data.mUri);
							// ConvertUtils.saveBitmapToValues(values,
							// PartToScreenTable.USERICON, mUserIcon);
							values.put(PartToScreenTable.USERICONTYPE,
									ImagePreviewResultType.TYPE_DEFAULT);
							values.put(PartToScreenTable.USERICONID, 0);
							dataProvider.addItemToScreen(values);

							screenFlag[data.mScreen + count] = true;
						} else // 文件夹
						{
							ContentValues values = new ContentValues();
							values.put(FolderTable.ID, System.currentTimeMillis());
							values.put(FolderTable.FOLDERID, data.mContainer + baseId);
							values.put(FolderTable.INTENT, data.mIntent);
							int index = dataProvider.getSizeOfFolder(data.mContainer + baseId);
							values.put(FolderTable.INDEX, index);
							values.put(FolderTable.TYPE, data.mItemType);
							values.put(FolderTable.USERTITLE, data.mTitle);
							values.put(FolderTable.USERICONTYPE,
									ImagePreviewResultType.TYPE_DEFAULT);
							values.put(FolderTable.USERICONID, 0);
							values.put(FolderTable.FROMAPPDRAWER, 0);
							values.put(FolderTable.TIMEINFOLDER, System.currentTimeMillis());
							dataProvider.addFolderItem(values);
						}
					}

					// 切换主屏
					if (mCleanSelfEmptyScreen) {
						GoSettingControler gosetting = GoSettingControler.getInstance(mContext);
						if (null != gosetting) {
							ScreenSettingInfo info = gosetting.getScreenSettingInfo();
							info.mMainScreen = 1;
							gosetting.updateScreenSettingInfo(info);
						}
					}

					// 删除屏幕
					// 删除屏幕导致屏幕个数发生变化
					int removeCount = 0;
					for (int i = 0; i < count + screenCount; i++) {
						if (!screenFlag[i]) {
							dataProvider.removeScreen(i - removeCount);
							removeCount++;
						}
					}

					GoLauncher.sendHandler(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
				}
			};
		}.start();
	}

	private BitmapDrawable getIcon(DeskItemData data) {
		BitmapDrawable icon = null;
		if (0 == data.mIconType) {
			PackageManager packageManager = mContext.getPackageManager();
			try {
				Resources resources = packageManager.getResourcesForApplication(data.mIconPackage);
				final int id = resources.getIdentifier(data.mIconResource, null, null);
				icon = (BitmapDrawable) Utilities.createIconThumbnail(resources.getDrawable(id),
						mContext);
			} catch (Exception e) {
				icon = (BitmapDrawable) packageManager.getDefaultActivityIcon();
			}
		} else if (1 == data.mIconType && null != data.mIcon) {
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data.mIcon, 0, data.mIcon.length);
				icon = new BitmapDrawable(mContext.getResources(), Utilities.createBitmapThumbnail(
						bitmap, mContext));
			} catch (Exception e) {
				PackageManager packageManager = mContext.getPackageManager();
				icon = (BitmapDrawable) packageManager.getDefaultActivityIcon();
			}
		}
		return icon;
	}

	private ArrayList<DeskItemData> migrate() throws Exception {
		Cursor cursor = mContext.getContentResolver().query(mMigrateUri, null, null, null, null);
		if (null == cursor) {
			throw new Exception("Query uri table failed");
		}

		ArrayList<DeskItemData> deskItems = null;
		try {
			boolean bParsedIndex = false;
			int idIndex = 0;
			int intentIndex = 0; // string
			int titleIndex = 0; // string
			int icontypeIndex = 0;
			int iconIndex = 0; // blob
			int iconPackageIndex = 0; // string
			int iconResourceIndex = 0; // string
			int containerIndex = 0;
			int itemTypeIndex = 0;
			int appWidgetIdIndex = 0;
			int screenIndex = 0;
			int cellXIndex = 0;
			int cellYIndex = 0;
			int spanXIndex = 0;
			int spanYIndex = 0;
			int uriIndex = 0; // string
			int displayModeIndex = 0;

			boolean bData = cursor.moveToFirst();
			while (bData) {
				if (!bParsedIndex) {
					bParsedIndex = true;
					idIndex = cursor.getColumnIndex("_id");
					intentIndex = cursor.getColumnIndex("intent"); // string
					titleIndex = cursor.getColumnIndex("title"); // string
					icontypeIndex = cursor.getColumnIndex("iconType");
					iconIndex = cursor.getColumnIndex("icon"); // blob
					iconPackageIndex = cursor.getColumnIndex("iconPackage"); // string
					iconResourceIndex = cursor.getColumnIndex("iconResource"); // string
					containerIndex = cursor.getColumnIndex("container");
					itemTypeIndex = cursor.getColumnIndex("itemType");
					appWidgetIdIndex = cursor.getColumnIndex("appWidgetId");
					screenIndex = cursor.getColumnIndex("screen");
					cellXIndex = cursor.getColumnIndex("cellX");
					cellYIndex = cursor.getColumnIndex("cellY");
					spanXIndex = cursor.getColumnIndex("spanX");
					spanYIndex = cursor.getColumnIndex("spanY");
					uriIndex = cursor.getColumnIndex("uri"); // string
					displayModeIndex = cursor.getColumnIndex("displayMode");
				}
				DeskItemData data = new DeskItemData();

				data.mId = cursor.getInt(idIndex);
				data.mIntent = cursor.getString(intentIndex);
				data.mTitle = cursor.getString(titleIndex);
				data.mIconType = cursor.getInt(icontypeIndex);
				// data.mIcon = cursor.getBlob(iconIndex);
				data.mIconPackage = cursor.getString(iconPackageIndex);
				data.mIconResource = cursor.getString(iconResourceIndex);
				data.mContainer = cursor.getInt(containerIndex);
				data.mItemType = cursor.getInt(itemTypeIndex);
				data.mWidgetId = cursor.getInt(appWidgetIdIndex);
				data.mScreen = cursor.getInt(screenIndex);
				data.mCellX = cursor.getInt(cellXIndex);
				data.mCellY = cursor.getInt(cellYIndex);
				data.mSpanX = cursor.getInt(spanXIndex);
				data.mSpanY = cursor.getInt(spanYIndex);
				data.mUri = cursor.getString(uriIndex);
				data.mDisplayMode = cursor.getInt(displayModeIndex);

				// 快捷方式
				// 系统文件夹
				// 解析图标
				if (1 == data.mItemType || 3 == data.mItemType) {
					String iconStr = cursor.getString(iconIndex);
					if (null != iconStr) {
						data.mIcon = iconStr.getBytes("ISO-8859-1");
					}
				}

				if (null == deskItems) {
					deskItems = new ArrayList<MigrateIntoDesk.DeskItemData>();
				}
				deskItems.add(data);
				bData = cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}

		return deskItems;
	}

	class DeskItemData {
		public int mId;

		public String mIntent;

		public String mTitle;

		public int mIconType;
		public byte[] mIcon;
		public String mIconPackage;
		public String mIconResource;

		public int mContainer;

		// 0: application
		// 1: shortcut
		// 2: folder
		// 3: live folder
		// 4: widget
		public int mItemType;

		public int mWidgetId;

		public int mScreen;
		public int mCellX;
		public int mCellY;
		public int mSpanX;
		public int mSpanY;

		public String mUri;

		public int mDisplayMode;
	}
}
