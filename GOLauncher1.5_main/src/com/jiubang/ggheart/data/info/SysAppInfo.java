package com.jiubang.ggheart.data.info;

import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.provider.LiveFolders;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.go.util.Utilities;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.FolderStyle;
/**
 * 系统app信息，包括系统文件夹
 * @author jiangxuwen
 *
 */
public class SysAppInfo {
	private final static String tag = "SysAppInfo";

	public static ShortCutInfo createFromShortcut(Context context, Intent data) {
		Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		Bitmap bitmap = null;
		try {
			bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
		} catch (ClassCastException e) {
			// 修改：敖日明 2011-12-30
			// Catch Exception后，不需要打印信息了
			// e.printStackTrace();
			// Log.e(tag, "Intent.EXTRA_SHORTCUT_ICON: "
			// + data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
			// .toString());
			// 这里有会出现空指针 因为无Intent.EXTRA_SHORTCUT_ICON_RESOURCE可以Get出来
			// Log.i(tag, "Intent.EXTRA_SHORTCUT_ICON_RESOURCE: "
			// + data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
			// .toString());
		}
		Drawable icon = null;
		boolean filtered = false;
		boolean customIcon = false;
		ShortcutIconResource iconResource = null;

		if (bitmap != null) {
			try {
				final Resources resources = context.getResources();
				icon = new BitmapDrawable(resources, Utilities.createBitmapThumbnail(bitmap,
						context));
				filtered = true;
				customIcon = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
			if (extra != null && extra instanceof ShortcutIconResource) {
				try {
					iconResource = (ShortcutIconResource) extra;
					final PackageManager packageManager = context.getPackageManager();
					Resources resources = packageManager
							.getResourcesForApplication(iconResource.packageName);
					final int id = resources.getIdentifier(iconResource.resourceName, null, null);
					icon = resources.getDrawable(id);
				} catch (Exception e) {
					Log.i(LogConstants.HEART_TAG, "Could not load shortcut icon: " + extra);
				}
			}
		}

		if (icon == null) {
			icon = context.getPackageManager().getDefaultActivityIcon();
		}

		final ShortCutInfo info = new ShortCutInfo();
		info.mFiltered = filtered;
		info.mTitle = name;
		info.mIntent = intent;
		info.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
		info.mIcon = Utilities.createIconThumbnail(icon, context);
		if (iconResource != null) {
			info.mIconResource = iconResource;
		}

		return info;
	}

	public static ScreenLiveFolderInfo createLiveFolder(Context context, Intent data) {

		Intent baseIntent = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT);
		String name = data.getStringExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME);

		Drawable icon = null;
		boolean filtered = false;
		Intent.ShortcutIconResource iconResource = null;

		Parcelable extra = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON);
		if (extra != null && extra instanceof Intent.ShortcutIconResource) {
			try {
				iconResource = (Intent.ShortcutIconResource) extra;
				final PackageManager packageManager = context.getPackageManager();
				Resources resources = packageManager
						.getResourcesForApplication(iconResource.packageName);
				final int id = resources.getIdentifier(iconResource.resourceName, null, null);
				icon = resources.getDrawable(id);
			} catch (Exception e) {
				Log.i(LogConstants.HEART_TAG, "Could not load live folder icon: " + extra);
			}
		}

		if (null != icon) {
			icon = Utilities.createIconThumbnail(icon, context);
		} else {
			FolderStyle folderStyle = null;
			DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
			if (themeControler != null && themeControler.isUesdTheme()) {
				DeskThemeBean themeBean = themeControler.getDeskThemeBean();
				if (themeBean != null && themeBean.mScreen != null) {
					folderStyle = themeBean.mScreen.mFolderStyle;
				}
			}

			if (folderStyle != null && folderStyle.mBackground != null) {
				icon = themeControler.getThemeResDrawable(folderStyle.mBackground.mResName);
			}

			if (icon == null) // get default
			{
				icon = context.getResources().getDrawable(R.drawable.folder_back);
			}
		}

		ScreenLiveFolderInfo info = new ScreenLiveFolderInfo();
		info.mIcon = icon;
		info.mFiltered = filtered;
		info.mTitle = name;
		info.mIconResource = iconResource;
		info.mUri = data.getData();
		info.mBaseIntent = baseIntent;
		info.mDisplayMode = data.getIntExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
				LiveFolders.DISPLAY_MODE_GRID);

		return info;
	}
}
