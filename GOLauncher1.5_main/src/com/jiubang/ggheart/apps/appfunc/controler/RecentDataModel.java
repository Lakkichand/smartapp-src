package com.jiubang.ggheart.apps.appfunc.controler;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.model.DataModel;
import com.jiubang.ggheart.data.tables.RecentAppTable;

public class RecentDataModel extends DataModel {

	// 统一程序数据管理
	private AppDataEngine mAppDataEngine;

	public RecentDataModel(Context context, AppDataEngine appDataEngine) {
		super(context);

		mAppDataEngine = appDataEngine;
	}

	public void getRecentAppItems(ArrayList<AppItemInfo> recAppItemInfos) {
		Cursor cursor = mDataProvider.getRecentAppItems();
		recAppItemInfos.clear();
		AppItemInfo appItemInfo = null;
		Intent intent = null;

		String str = null;
		// final int index = cursor.getColumnIndex(RecentAppTable.INDEX);
		if (cursor != null) {
			try {
				final int intentIdx = cursor.getColumnIndex(RecentAppTable.INTENT);
				if (cursor.moveToLast()) {
					do {
						str = cursor.getString(intentIdx);
						intent = ConvertUtils.stringToIntent(str);
						appItemInfo = mAppDataEngine.getAppItem(intent);
						if (null == appItemInfo) {
							continue;
						}
						recAppItemInfos.add(appItemInfo);
					} while (cursor.moveToPrevious());
				}
			} finally {
				cursor.close();
			}
		}
	}

	/**
	 * 取得公用数据bean
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @return 数据bean
	 */
	public AppItemInfo getAppItem(final Intent intent) {
		if (null == intent) {
			return null;
		}
		return mAppDataEngine.getAppItem(intent);
	}

	public int getRecentAppItemsCount() {
		return mDataProvider.getRecentAppItemsCount();
	}

	public void addRecentAppItem(final Intent intent, final int recentAppIndex) {
		if (null == intent) {
			return;
		}

		// TODO:统一转换
		String str = intent.toUri(0);

		ContentValues contentValues = new ContentValues();
		contentValues.put(RecentAppTable.INDEX, recentAppIndex);
		contentValues.put(RecentAppTable.INTENT, str);
		mDataProvider.addRecentAppItem(contentValues);
	}

	/**
	 * 删除指定intent对应的记录
	 * 
	 * @param itemId
	 *            id
	 */
	public void removeRecentAppItem(final Intent intent) {
		mDataProvider.removeRecentAppItem(intent);
	}

	/**
	 * 删除指定id对应的记录
	 * 
	 * @param itemId
	 *            id
	 */
	public void removeRecentAppItem(final int index) {
		mDataProvider.removeRecentAppItem(index);
	}

	/**
	 * 删除所有最近打开的项
	 * 
	 * @author huyong
	 */
	public void removeRecentAppItems() {
		mDataProvider.removeRecentAppItems();
	}

	public void beginDBTransaction() {
		mDataProvider.beginTransaction();
	}

	public void setDBTransactionSuccessful() {
		mDataProvider.setTransactionSuccessful();
	}

	public void endDBTransaction() {
		mDataProvider.endTransaction();
	}
}
