package com.jiubang.ggheart.plugin.notification;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.model.DataModel;
import com.jiubang.ggheart.data.tables.NotificationAppSettingTable;

/**
 * 
 * <br>类描述:通讯统计的数据库读写
 * <br>功能详细描述:
 * 
 * @author  wuziyi
 * @date  [2012-10-23]
 */
public class NotificationSettingDateModel extends DataModel {

	public NotificationSettingDateModel(Context context) {
		super(context);
	}
	/**
	 * 添加已勾选的通讯统计应用程序
	 * @author wuziyi
	 * @param intent
	 * @throws DatabaseException 
	 */
	public void addNotificationAppItem(final Intent intent) throws DatabaseException {
		if (intent == null) {
			return;
		}
		ContentValues contentValues = new ContentValues();
		boolean isSelected = true;
		String intentString = ConvertUtils.intentToString(intent);
		contentValues.put(NotificationAppSettingTable.INTENT, intentString);
		contentValues.put(NotificationAppSettingTable.ISSELECTED, isSelected);

		mDataProvider.addNotificationAppItem(contentValues);

		contentValues.clear();
		contentValues = null;
	}

	/**
	 * 删除已勾选的通讯统计程序
	 * @param intent
	 * @throws DatabaseException 
	 */
	public void delNotificationAppItem(final Intent intent) throws DatabaseException {
		mDataProvider.delNotificationAppItem(intent);
	}

	/**
	 * 保存已勾选的通讯统计程序
	 * @param appItems
	 * @throws DatabaseException 
	 */
	public void saveNotificationAppItems(final ArrayList<Intent> appItems) throws DatabaseException {
		if (appItems == null || appItems.size() <= 0) {
			return;
		}
		int size = appItems.size();
		mDataProvider.beginTransaction();
		try {
			for (int i = 0; i < size; i++) {
				Intent intent = appItems.get(i);
				if (intent == null) {
					continue;
				}
				addNotificationAppItem(intent);
			}
			mDataProvider.setTransactionSuccessful();
		} catch (DatabaseException e) {
			throw e;
		} finally {
			mDataProvider.endTransaction();
		}

	}

	/**
	 * 获取所有已勾选的通讯统计程序
	 * @return ArrayList<Intent>
	 */
	public ArrayList<Intent> getNotificationAppItems() {
		Cursor cursor = mDataProvider.getAllNotificationAppItems();
		ArrayList<Intent> intentList = new ArrayList<Intent>();
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						int intentIndex = cursor.getColumnIndex(NotificationAppSettingTable.INTENT);
						int isSelectedIndex = cursor
								.getColumnIndex(NotificationAppSettingTable.ISSELECTED);
						String intentString = cursor.getString(intentIndex);
						Intent intent = ConvertUtils.stringToIntent(intentString);

						boolean isSelected = cursor.getInt(isSelectedIndex) == 1;
						if (isSelected) {
							intentList.add(intent);
						}
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				cursor.close();
			}
		}
		return intentList;
	}

	/**
	 * 清空已勾选的通讯统计列表
	 * @throws DatabaseException 
	 */
	public void clearAllNotificationAppItems() throws DatabaseException {
		mDataProvider.clearAllNotificationAppItems();
	}
}
