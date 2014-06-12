/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.ggheart.data.info;

import java.util.List;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.data.tables.PartToScreenTable;
import com.jiubang.ggheart.data.tables.ShortcutTable;
import com.jiubang.ggheart.plugin.notification.NotificationType;

/**
 * Represents a launchable application. An application is made of a name (or
 * title), an intent and an icon.
 */
public class ShortCutInfo extends FeatureItemInfo {
	/**
	 * The "unread counter" notification
	 */
	public int mCounter = 0;
	/**
	 * The "unread counter" notification type
	 */
	public int mCounterType = NotificationType.IS_NOT_NOTIFICSTION;
	/**
	 * The application name.
	 */
	public CharSequence mTitle;

	/**
	 * The intent used to start the application.
	 */
	public Intent mIntent;

	/**
	 * the icon used to show TODO 在整理数据存储的时候需要考虑把快捷方式的图标也保存起来
	 */
	public Drawable mIcon;

	/**
	 * When set to true, indicates that the icon has been resized.
	 */
	public boolean mFiltered;

	/**
	 * If isShortcut=true and customIcon=false, this contains a reference to the
	 * shortcut icon as an application's resource.
	 */
	public Intent.ShortcutIconResource mIconResource;

	/**
	 * 是否使用用户自定义的图标
	 */
	public boolean mIsUserIcon;

	/**
	 * 是否使用用户自定义的titile
	 */
	public boolean mIsUserTitle;

	/**
	 * 进入文件夹的时间
	 */
	public long mTimeInFolder = -1;

	public ShortCutInfo() {
		mItemType = IItemType.ITEM_TYPE_APPLICATION;
	}

	public void setIcon(Drawable icon, boolean isUserIcon) {
		mIcon = icon;
		mIsUserIcon = isUserIcon;
	}

	public void setTitle(CharSequence title, boolean isUserTitle) {
		mTitle = title;
		mIsUserTitle = isUserTitle;
	}

	public ShortCutInfo(ShortCutInfo info) {
		super(info);
		mIsUserTitle = info.mIsUserTitle;
		mTitle = info.mTitle;
		mIcon = info.mIcon;
		mIntent = new Intent(info.mIntent);
		if (info.mIconResource != null) {
			mIconResource = new Intent.ShortcutIconResource();
			mIconResource.packageName = info.mIconResource.packageName;
			mIconResource.resourceName = info.mIconResource.resourceName;
		}
		mFiltered = info.mFiltered;
		mCounter = info.mCounter;
		mIsUserIcon = info.mIsUserIcon;
	}

	/**
	 * Creates the application intent based on a component name and various
	 * launch flags. Sets {@link #itemType} to
	 * {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
	 * 
	 * @param className
	 *            the class name of the component representing the intent
	 * @param launchFlags
	 *            the launch flags
	 */
	public final void setActivity(ComponentName className, int launchFlags) {
		mIntent = new Intent(Intent.ACTION_MAIN);
		mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mIntent.setComponent(className);
		mIntent.setFlags(launchFlags);
		// mDataStr = mIntent.toURI();
	}

	@Override
	public String toString() {
		return mTitle != null ? mTitle.toString() : "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case AppItemInfo.INCONCHANGE : {
				// 更新图标缓存
				if (!mIsUserIcon && object != null && object instanceof Drawable) {
					mIcon = (Drawable) object;
				}
				break;
			}

			case AppItemInfo.TITLECHANGE : {
				// 更新标题缓存
				if (!mIsUserTitle && object != null && object instanceof String) {
					mTitle = (String) object;
				}
				break;
			}

			default :
				break;
		}
		super.onBCChange(msgId, param, object, objects);
	}

	@Override
	public void writeObject(ContentValues values, String table) {
		super.writeObject(values, table);

		if (table.equals(PartToScreenTable.TABLENAME)) {
			values.put(PartToScreenTable.INTENT, ConvertUtils.intentToString(mIntent));
		} else if (table.equals(FolderTable.TABLENAME)) {
			values.put(FolderTable.INTENT, ConvertUtils.intentToString(mIntent));
			values.put(FolderTable.TIMEINFOLDER, String.valueOf(mTimeInFolder));
		} else if (table.equals(ShortcutTable.TABLENAME)) {
			values.put(ShortcutTable.INTENT, ConvertUtils.intentToString(mIntent));
		}
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		super.readObject(cursor, table);

		if (table.equals(PartToScreenTable.TABLENAME)) {
			mIntent = ConvertUtils.stringToIntent(cursor.getString(cursor
					.getColumnIndex(PartToScreenTable.INTENT)));
		} else if (table.equals(FolderTable.TABLENAME)) {
			mIntent = ConvertUtils.stringToIntent(cursor.getString(cursor
					.getColumnIndex(FolderTable.INTENT)));
			mTimeInFolder = cursor.getLong(cursor.getColumnIndex(FolderTable.TIMEINFOLDER));
		} else if (table.equals(ShortcutTable.TABLENAME)) {
			mIntent = ConvertUtils.stringToIntent(cursor.getString(cursor
					.getColumnIndex(ShortcutTable.INTENT)));
		}
	}

	@Override
	public void selfDestruct() {
		super.selfDestruct();
		if (mIcon != null) {
			mIcon.setCallback(null);
		}
	}
}
