package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppIconForMsg;
import com.jiubang.ggheart.apps.desks.appfunc.model.FolderIconForMsg;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderIcon;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * @author jiangxuwen
 *
 */
public class DragData {
	public static final View createDragView(Context context, AppIconForMsg appIconForMsg) {
		BubbleTextView dragView = null;
		if (null == context || null == appIconForMsg) {
			return dragView;
		}

		OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();

		try {
			final boolean isPort = GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT;
			final int id = isPort ? R.layout.application_port : R.layout.application_land;
			dragView = (BubbleTextView) LayoutInflater.from(context).inflate(id, null, false);
		} catch (Exception e) {
			dragView = null;
			e.printStackTrace();
			return dragView;
		}

		Resources resources = context.getResources();
		// dragView.setWidth(resources.getDimensionPixelSize(R.dimen.cell_width_port));
		// dragView.setHeight(resources.getDimensionPixelSize(R.dimen.cell_height_port));
		dragView.setIcon(appIconForMsg.mImage);
		dragView.setText(appIconForMsg.mIconName);

		ShortCutInfo shortCutInfo = new ShortCutInfo();
		shortCutInfo.mId = -1;
		shortCutInfo.mInScreenId = -1;
		shortCutInfo.mIcon = appIconForMsg.mImage;
		shortCutInfo.mIntent = appIconForMsg.mIntent;
		shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		shortCutInfo.mSpanX = 1;
		shortCutInfo.mSpanY = 1;
		shortCutInfo.mTitle = appIconForMsg.mIconName;
		dragView.setTag(shortCutInfo);

		AppItemInfo appItemInfo = GOLauncherApp.getAppDataEngine()
				.getAppItem(appIconForMsg.mIntent);
		shortCutInfo.setRelativeItemInfo(appItemInfo);
		return dragView;
	}

	public static final View createDragView(Context context, FolderIconForMsg folderIconForMsg) {
		FolderIcon folderIcon = null;
		if (null == context || null == folderIconForMsg) {
			return folderIcon;
		}

		UserFolderInfo folderInfo = new UserFolderInfo();
		folderInfo.mId = -1;
		folderInfo.mInScreenId = -1;
		folderInfo.mRefId = folderIconForMsg.mFolderId;
		folderInfo.mTitle = folderIconForMsg.mIconName;
		folderInfo.mSpanX = 1;
		folderInfo.mSpanY = 1;
		List<AppItemInfo> contents = folderIconForMsg.mAppItemInfoList;
		int size = contents.size();
		for (int i = 0; i < size; i++) {
			// 装填其中的元素
			final AppItemInfo itemInfo = contents.get(i);
			ShortCutInfo shortCutInfo = new ShortCutInfo();
			shortCutInfo.mId = -1;
			shortCutInfo.mIcon = itemInfo.mIcon;
			shortCutInfo.mIntent = itemInfo.mIntent;
			shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			shortCutInfo.mSpanX = 1;
			shortCutInfo.mSpanY = 1;
			shortCutInfo.mTitle = itemInfo.mTitle;
			shortCutInfo.mTimeInFolder = System.currentTimeMillis() + i;
			folderInfo.add(shortCutInfo);
		}
		final int id = GoLauncher.isLargeIcon() ? R.layout.folder_icon_large : R.layout.folder_icon;
		folderIcon = FolderIcon.fromXml(id, context, null, folderInfo, folderIconForMsg.mIconName);
		folderIcon.close();

		return folderIcon;
	}

}
