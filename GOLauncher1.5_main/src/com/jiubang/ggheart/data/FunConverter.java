package com.jiubang.ggheart.data;

import java.util.ArrayList;

import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.info.FunItem;
import com.jiubang.ggheart.data.tables.AppTable;
import com.jiubang.ggheart.data.tables.FolderTable;

/**
 * 功能表对象转换类
 * @author wuziyi
 *
 */
public class FunConverter {

	/**
	 * 转换成文件id列表
	 * 
	 * @param cursor
	 *            游标
	 * @param folderIds
	 *            文件id列表
	 */
	public static void convertToFolderIdsFromAppTable(Cursor cursor, ArrayList<Long> folderIds) {
		if (null == cursor || null == folderIds) {
			return;
		}

		folderIds.clear();
		long folderId = 0;
		try {
			final int folderIdIdx = cursor.getColumnIndex(AppTable.FOLDERID);
			if (cursor.moveToFirst()) {
				do {
					folderId = cursor.getInt(folderIdIdx);
					folderIds.add(folderId);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
	}

	public static void convertToFunItemsFromFolderTable(Cursor cursor, ArrayList<FunItem> items) {
		if (null == cursor || null == items) {
			return;
		}

		FunItem info = null;
		String intentStr = null;
		try {
			final int index = cursor.getColumnIndex(FolderTable.INDEX);
			final int intentIdx = cursor.getColumnIndex(FolderTable.INTENT);
			final int titleIdx = cursor.getColumnIndex(FolderTable.USERTITLE);
			final int timeInFolderIdx = cursor.getColumnIndex(FolderTable.TIMEINFOLDER);
			if (cursor.moveToFirst()) {
				do {
					info = new FunItem();
					info.mIndex = cursor.getInt(index);
					intentStr = cursor.getString(intentIdx);
					info.mTitle = cursor.getString(titleIdx);
					info.mIntent = ConvertUtils.stringToIntent(intentStr);
					info.mTimeInFolder = cursor.getLong(timeInFolderIdx);
					items.add(info);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
	}

	public static void convertToFunItemsFromAppTable(Cursor cursor, ArrayList<FunItem> items) {
		if (null == cursor || null == items) {
			return;
		}

		FunItem info = null;
		String str = null;
		try {
			final int index = cursor.getColumnIndex(AppTable.INDEX);
			final int intentIdx = cursor.getColumnIndex(AppTable.INTENT);
			final int folderIdIdx = cursor.getColumnIndex(AppTable.FOLDERID);
			final int folderTitleIdx = cursor.getColumnIndex(AppTable.TITLE);
//			final int folderIconPahtIdx = cursor.getColumnIndex(AppTable.FOLDERICONPATH);
			if (cursor.moveToFirst()) {
				do {
					info = new FunItem();
					info.mIndex = cursor.getInt(index);
					str = cursor.getString(intentIdx);
					info.mIntent = ConvertUtils.stringToIntent(str);
					info.mFolderId = cursor.getLong(folderIdIdx);
					info.mTitle = cursor.getString(folderTitleIdx);
//					info.mFolderIconPath = cursor.getString(folderIconPahtIdx);
					items.add(info);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
	}
}
