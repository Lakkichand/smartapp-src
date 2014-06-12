package com.jiubang.ggheart.data.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.info.FunTaskAdditionalInfo;
import com.jiubang.ggheart.data.tables.AppWhiteListTable;

public class FunTaskDataModel extends DataModel {

	public FunTaskDataModel(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 添加忽略的应用程序
	 * 
	 * @author huyong
	 * @param intent
	 */
	public FunTaskAdditionalInfo addIgnoreAppItem(final Intent intent) {
		if (intent == null) {
			return null;
		}
		ContentValues contentValues = new ContentValues();
		int ignore = 1;
		String intentString = ConvertUtils.intentToString(intent);
		contentValues.put(AppWhiteListTable.INTENT, intentString);
		contentValues.put(AppWhiteListTable.ISIGNORE, ignore);

		mDataProvider.addIgnoreTaskAppItem(contentValues);

		contentValues.clear();
		contentValues = null;

		FunTaskAdditionalInfo funTaskAdditionalInfo = new FunTaskAdditionalInfo();
		funTaskAdditionalInfo.setIntent(intent);
		funTaskAdditionalInfo.setIsIgnore(ignore);
		return funTaskAdditionalInfo;

	}

	public void delTaskAppItem(final Intent intent) {
		mDataProvider.delTaskAppItem(intent);
	}

	public void saveIgnoreAppItems(final ArrayList<Intent> appItems) {
		if (appItems == null || appItems.size() <= 0) {
			return;
		}
		int size = appItems.size();
		for (int i = 0; i < size; i++) {
			Intent intent = appItems.get(i);
			if (intent == null) {
				continue;
			}
			addIgnoreAppItem(intent);
		}
	}

	public ArrayList<FunTaskAdditionalInfo> getAllIgnoreTaskAppItems() {
		Cursor cursor = mDataProvider.getAllIgnoreTaskAppItems();
		ArrayList<FunTaskAdditionalInfo> funTaskAdditionalInfos = new ArrayList<FunTaskAdditionalInfo>();
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						FunTaskAdditionalInfo taskAdditionalInfo = new FunTaskAdditionalInfo();
						int intentIndex = cursor.getColumnIndex(AppWhiteListTable.INTENT);
						int isIgnoreIndex = cursor.getColumnIndex(AppWhiteListTable.ISIGNORE);
						String intentString = cursor.getString(intentIndex);
						Intent intent = ConvertUtils.stringToIntent(intentString);
						taskAdditionalInfo.setIntent(intent);

						int isIgnore = cursor.getInt(isIgnoreIndex);
						taskAdditionalInfo.setIsIgnore(isIgnore);

						funTaskAdditionalInfos.add(taskAdditionalInfo);

					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
		}

		return funTaskAdditionalInfos;
	}

	public void clearAllIgnoreTaskAppItems() {
		mDataProvider.clearAllIgnoreTaskAppItems();
	}

}
