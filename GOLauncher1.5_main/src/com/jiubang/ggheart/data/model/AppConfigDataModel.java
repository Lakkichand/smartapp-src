package com.jiubang.ggheart.data.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppConfigInfo;
import com.jiubang.ggheart.data.tables.AppHideListTable;

public class AppConfigDataModel extends DataModel {

	public AppConfigDataModel(Context context) {
		super(context);
	}

	/**
	 * 添加隐藏的应用程序
	 * 
	 * @author guodanyang
	 * @param intent
	 * @throws DatabaseException
	 */
	public AppConfigInfo addHideAppItem(final Intent intent) throws DatabaseException {
		if (intent == null) {
			return null;
		}
		ContentValues contentValues = new ContentValues();
		boolean hide = true;
		String intentString = ConvertUtils.intentToString(intent);
		contentValues.put(AppHideListTable.INTENT, intentString);
		contentValues.put(AppHideListTable.ISHIDE, hide);

		mDataProvider.addHideAppItem(contentValues);

		contentValues.clear();
		contentValues = null;

		AppConfigInfo appConfigInfo = new AppConfigInfo();
		appConfigInfo.setIntent(intent);
		appConfigInfo.setHide(hide);
		return appConfigInfo;

	}

	/**
	 * 删除隐藏的程序
	 * 
	 * @param intent
	 * @throws DatabaseException
	 */
	public void delHideAppItem(final Intent intent) throws DatabaseException {
		mDataProvider.delHideAppItem(intent);
	}

	/**
	 * 保存隐藏的程序
	 * 
	 * @param appItems
	 * @throws DatabaseException
	 */
	public void saveHideAppItems(final ArrayList<Intent> appItems) throws DatabaseException {
		if (appItems == null || appItems.size() <= 0) {
			return;
		}
		int size = appItems.size();
		for (int i = 0; i < size; i++) {
			Intent intent = appItems.get(i);
			if (intent == null) {
				continue;
			}
			addHideAppItem(intent);
		}
	}

	/**
	 * 获取所有隐藏的程序
	 * 
	 * @return
	 */
	public ArrayList<AppConfigInfo> getAllHideAppItems() {
		Cursor cursor = mDataProvider.getAllHideAppItems();
		ArrayList<AppConfigInfo> AppConfigInfos = new ArrayList<AppConfigInfo>();
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						AppConfigInfo appConfigInfo = new AppConfigInfo();
						int intentIndex = cursor.getColumnIndex(AppHideListTable.INTENT);
						int isHideIndex = cursor.getColumnIndex(AppHideListTable.ISHIDE);
						String intentString = cursor.getString(intentIndex);
						Intent intent = ConvertUtils.stringToIntent(intentString);
						appConfigInfo.setIntent(intent);

						boolean isHide = cursor.getInt(isHideIndex) == 1;
						appConfigInfo.setHide(isHide);

						AppConfigInfos.add(appConfigInfo);

					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				cursor.close();
			}
		}

		return AppConfigInfos;
	}

	/**
	 * 清空隐藏列表
	 * 
	 * @throws DatabaseException
	 */
	public void clearAllHideAppItems() throws DatabaseException {
		mDataProvider.clearAllHideAppItems();
	}

	public void beginTransaction() {
		mDataProvider.beginTransaction();
	}

	public void setTransactionSuccessful() {
		mDataProvider.setTransactionSuccessful();
	}

	public void endTransaction() {
		mDataProvider.endTransaction();
	}
}
