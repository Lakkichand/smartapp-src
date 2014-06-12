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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.tables.PartToScreenTable;

public class ScreenLiveFolderInfo extends ScreenFolderInfo {

	/**
	 * The base intent, if it exists.
	 */
	public Intent mBaseIntent;

	/**
	 * The live folder's content uri.
	 */
	public Uri mUri;

	/**
	 * The live folder's display type.
	 */
	public int mDisplayMode;

	/**
	 * The live folder icon.
	 */
	public Drawable mIcon;

	/**
	 * When set to true, indicates that the icon has been resized.
	 */
	public boolean mFiltered;

	/**
	 * Reference to the live folder icon as an application's resource.
	 */
	public Intent.ShortcutIconResource mIconResource;

	public ScreenLiveFolderInfo() {
		mItemType = IItemType.ITEM_TYPE_LIVE_FOLDER;
	}

	@Override
	public void writeObject(ContentValues values, String table) {
		super.writeObject(values, table);

		if (table.equals(PartToScreenTable.TABLENAME)) {
			values.put(PartToScreenTable.INTENT, ConvertUtils.intentToString(mBaseIntent));
			values.put(PartToScreenTable.URI, ConvertUtils.uriToString(mUri));
		}
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		super.readObject(cursor, table);

		if (table.equals(PartToScreenTable.TABLENAME)) {
			mBaseIntent = ConvertUtils.stringToIntent(cursor.getString(cursor
					.getColumnIndex(PartToScreenTable.INTENT)));
			mUri = ConvertUtils.stringToUri(cursor.getString(cursor
					.getColumnIndex(PartToScreenTable.URI)));
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
