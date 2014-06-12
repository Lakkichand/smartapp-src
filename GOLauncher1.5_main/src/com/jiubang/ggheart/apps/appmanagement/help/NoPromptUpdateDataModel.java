package com.jiubang.ggheart.apps.appmanagement.help;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.appmanagement.bean.NoPromptUpdateInfo;
import com.jiubang.ggheart.data.model.DataModel;
import com.jiubang.ggheart.data.tables.NoPromptUpdateAppTable;

public class NoPromptUpdateDataModel extends DataModel {

	public NoPromptUpdateDataModel(Context context) {
		super(context);
	}

	/**
	 * 查询所有忽略更新的应用
	 * 
	 * @return
	 */
	public ArrayList<NoPromptUpdateInfo> getAllNoPromptUpdateApp() {
		Cursor cursor = mDataProvider.getAllNoPromptUpdateApp(NoPromptUpdateAppTable.TABLENAME);
		ArrayList<NoPromptUpdateInfo> noUpdateInfos = new ArrayList<NoPromptUpdateInfo>();
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						NoPromptUpdateInfo noUpdateInfo = new NoPromptUpdateInfo();
						int intentIndex = cursor.getColumnIndex(NoPromptUpdateAppTable.INTENT);
						int isNoUpdateIndex = cursor
								.getColumnIndex(NoPromptUpdateAppTable.NOUPDATE);
						String intentString = cursor.getString(intentIndex);
						Intent intent = ConvertUtils.stringToIntent(intentString);
						noUpdateInfo.setIntent(intent);

						int noUpdat = cursor.getInt(isNoUpdateIndex);
						noUpdateInfo.setmNoUpdate(noUpdat);

						noUpdateInfos.add(noUpdateInfo);

					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
		}
		return noUpdateInfos;
	}

	/**
	 * 恢复更新 选中的应用，传递null时，恢复所有的应用
	 * 
	 * @param intent
	 */
	public void deleteNoUpdateApp(Intent intent) {
		String selection = null;
		if (intent != null) {
			String intentString = ConvertUtils.intentToString(intent);
			selection = NoPromptUpdateAppTable.INTENT + " = '" + intentString + "'";
		}
		mDataProvider.delNoPromptUpdateApp(NoPromptUpdateAppTable.TABLENAME, selection);
	}

	/**
	 * 添加忽略更新的应用
	 * 
	 * @param intent
	 */
	public void addNoUpdateApp(Intent intent) {
		if (intent == null) {
			return;
		}
		ContentValues contentValues = new ContentValues();
		int noUpdate = 1;
		String intentString = ConvertUtils.intentToString(intent);
		contentValues.put(NoPromptUpdateAppTable.INTENT, intentString);
		contentValues.put(NoPromptUpdateAppTable.NOUPDATE, noUpdate);

		mDataProvider.addNoPromptUpdateApp(NoPromptUpdateAppTable.TABLENAME, contentValues);

		contentValues.clear();
		contentValues = null;

		return;
	}
}
