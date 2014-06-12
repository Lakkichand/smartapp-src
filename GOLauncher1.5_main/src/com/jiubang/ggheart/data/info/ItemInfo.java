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

import android.content.ContentValues;
import android.database.Cursor;

import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.data.BroadCaster;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.data.tables.PartToScreenTable;
import com.jiubang.ggheart.data.tables.ShortcutTable;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo extends BroadCaster
		implements
			BroadCasterObserver,
			IDatabaseObject,
			ISelfObject {

	/**
	 * @deprecated
	 */
	@Deprecated
	public long mId;

	// 自身id，与其他ItemInfo区分的唯一标识，在数据库中相当于主键
	public long mInScreenId;

	// 所在屏幕索引
	public int mScreenIndex;

	// 对外物的一个参考ID
	public long mRefId;

	/**
	 * One of {@link IItemType#ITEM_TYPE_APPLICATION},
	 * {@link IItemType#ITEM_TYPE_SHORTCUT},
	 * {@link IItemType#ITEM_TYPE_USER_FOLDER}, or
	 * {@link IItemType#ITEM_TYPE_APPWIDGET}.
	 */
	public int mItemType;

	/**
	 * Indicates the X position of the associated cell.
	 */
	public int mCellX = -1;

	/**
	 * Indicates the Y position of the associated cell.
	 */
	public int mCellY = -1;

	/**
	 * Indicates the X cell span.
	 */
	public int mSpanX = 1;

	/**
	 * Indicates the Y cell span.
	 */
	public int mSpanY = 1;

	/**
	 * 构造方法
	 */
	public ItemInfo() {
		mRefId = 0;
	}

	/**
	 * 复制的构造方法
	 * 
	 * @param info
	 *            被复制的数据对象
	 */
	public ItemInfo(ItemInfo info) {
		if (info != null) {
			mCellX = info.mCellX;
			mCellY = info.mCellY;
			mSpanX = info.mSpanX;
			mSpanY = info.mSpanY;
			mItemType = info.mItemType;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		broadCast(msgId, param, object, objects);
	}

	@Override
	public boolean unRegisterObserver(BroadCasterObserver observer) {
		return super.unRegisterObserver(observer);
	}

	@Override
	public void writeObject(ContentValues values, String table) {
		if (table.equals(PartToScreenTable.TABLENAME)) {
			values.put(PartToScreenTable.ID, mInScreenId);
			// values.put(PartToScreenTable.SCREENID, mScreenIndex);
			values.put(PartToScreenTable.PARTID, mRefId);
			values.put(PartToScreenTable.SCREENX, mCellX);
			values.put(PartToScreenTable.SCREENY, mCellY);
			values.put(PartToScreenTable.SPANX, mSpanX);
			values.put(PartToScreenTable.SPANY, mSpanY);
			values.put(PartToScreenTable.ITEMTYPE, mItemType);
		} else if (table.equals(FolderTable.TABLENAME)) {
			values.put(FolderTable.ID, mInScreenId);
			values.put(FolderTable.TYPE, mItemType);
		} else if (table.equals(ShortcutTable.TABLENAME)) {
			values.put(ShortcutTable.PARTID, mInScreenId);
			values.put(ShortcutTable.ITEMTYPE, mItemType);
			if (IItemType.ITEM_TYPE_USER_FOLDER == mItemType) {
				values.put(ShortcutTable.REFID, mRefId);
			}
		}
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		if (table.equals(PartToScreenTable.TABLENAME)) {
			mInScreenId = cursor.getLong(cursor.getColumnIndex(PartToScreenTable.ID));
			// mScreenIndex =
			// cursor.getInt(cursor.getColumnIndex(PartToScreenTable.SCREENID));
			mRefId = cursor.getLong(cursor.getColumnIndex(PartToScreenTable.PARTID));
			mCellX = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.SCREENX));
			mCellY = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.SCREENY));
			mSpanX = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.SPANX));
			mSpanY = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.SPANY));
			mItemType = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.ITEMTYPE));
		} else if (table.equals(FolderTable.TABLENAME)) {
			mInScreenId = cursor.getLong(cursor.getColumnIndex(FolderTable.ID));
			mItemType = cursor.getInt(cursor.getColumnIndex(FolderTable.TYPE));
		} else if (table.equals(ShortcutTable.TABLENAME)) {
			mItemType = cursor.getInt(cursor.getColumnIndex(ShortcutTable.ITEMTYPE));
			mInScreenId = cursor.getLong(cursor.getColumnIndex(ShortcutTable.PARTID));
			if (mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
				mRefId = cursor.getLong(cursor.getColumnIndex(ShortcutTable.REFID));
			}
		}
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		clearAllObserver();
	}
}
