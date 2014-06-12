/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.tables.PartToScreenTable;

/**
 * Represents a widget, which just contains an identifier.
 */
public class ScreenAppWidgetInfo extends ItemInfo {

	/**
	 * Identifier for this widget when talking with {@link AppWidgetManager} for
	 * updates.
	 */
	public int mAppWidgetId;

	/**
	 * View that holds this widget after it's been created. This view isn't
	 * created until Launcher knows it's needed.
	 */
	// public transient AppWidgetHostView mHostView = null;

	// 为了兼容GoWidget这里改成View
	public transient View mHostView = null;

	public Intent mProviderIntent;

	/**
	 * 
	 * @param appWidgetId
	 *            appWidgetId
	 */
	public ScreenAppWidgetInfo(int appWidgetId) {
		init(appWidgetId);
	}

	/**
	 * 
	 * @param appWidgetId
	 * @param cn
	 *            provider ComponentName
	 */
	public ScreenAppWidgetInfo(int appWidgetId, ComponentName cn) {
		init(appWidgetId);
		if (cn != null) {
			mProviderIntent = new Intent();
			mProviderIntent.setComponent(cn);
		}
	}

	/**
	 * 
	 * @param appWidgetId
	 *            appWidgetId
	 * @param itemInfo
	 *            itemInfo
	 */
	public ScreenAppWidgetInfo(int appWidgetId, ComponentName cn, ItemInfo itemInfo) {
		this(appWidgetId, null);
		if (itemInfo != null) {
			mCellX = itemInfo.mCellX;
			mCellY = itemInfo.mCellY;
			mSpanX = itemInfo.mSpanX;
			mSpanY = itemInfo.mSpanY;
			mInScreenId = itemInfo.mInScreenId;
			mId = itemInfo.mId;
			mScreenIndex = itemInfo.mScreenIndex;
		}
	}

	private void init(int appWidgetId) {
		mAppWidgetId = appWidgetId;
		mItemType = IItemType.ITEM_TYPE_APP_WIDGET;
	}

	@Override
	public String toString() {
		return Integer.toString(mAppWidgetId);
	}

	@Override
	public void writeObject(ContentValues values, String table) {
		super.writeObject(values, table);
		if (table.equals(PartToScreenTable.TABLENAME)) {
			values.put(PartToScreenTable.WIDGETID, mAppWidgetId);
			values.put(PartToScreenTable.INTENT, ConvertUtils.intentToString(mProviderIntent));
		}
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		super.readObject(cursor, table);

		if (table.equals(PartToScreenTable.TABLENAME)) {
			mAppWidgetId = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.WIDGETID));
			mProviderIntent = ConvertUtils.stringToIntent(cursor.getString(cursor
					.getColumnIndex(PartToScreenTable.INTENT)));
		}
	}
}
