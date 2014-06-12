package com.jiubang.ggheart.data.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.info.SysShortCutItemInfo;
import com.jiubang.ggheart.data.tables.SysFolderTable;
import com.jiubang.ggheart.data.tables.SysShortcutTable;

public class SysShortCutDataModel extends DataModel {
	public SysShortCutDataModel(Context context) {
		super(context);
	}

	/**
	 * 增加快捷方式表
	 * 
	 * @param intent
	 * @param name
	 * @param icon
	 */
	public void addSysShortCutRecord(Intent intent, String name, BitmapDrawable icon) {
		ContentValues values = new ContentValues();
		String intentStr = ConvertUtils.intentToString(intent);
		values.put(SysShortcutTable.INTENT, intentStr);
		values.put(SysShortcutTable.NAME, name);
		ConvertUtils.saveBitmapToValues(values, SysShortcutTable.ICON, icon);
		values.put(SysShortcutTable.REFCOUNT, 1);

		mDataProvider.addRecord(SysShortcutTable.TABLENAME, values);
	}

	/**
	 * 更新快捷方式被应用次数
	 * 
	 * @param intent
	 * @param refCount
	 */
	public void updateSysShortCutRefCount(Intent intent, int refCount) {
		ContentValues values = new ContentValues();
		values.put(SysShortcutTable.REFCOUNT, refCount);

		String intentStr = ConvertUtils.intentToString(intent);

		String selection = SysShortcutTable.INTENT + " = " + "'" + intentStr + "'";
		mDataProvider.updateRecord(SysShortcutTable.TABLENAME, values, selection);
	}

	/**
	 * 删除快捷方式表
	 * 
	 * @param intent
	 */
	public void delSysShortCutRecord(Intent intent) {
		String intentStr = ConvertUtils.intentToString(intent);

		String selection = SysShortcutTable.INTENT + " = " + "'" + intentStr + "'";
		mDataProvider.delRecord(SysShortcutTable.TABLENAME, selection);
	}

	/**
	 * 获取快捷方式数据源
	 * 
	 * @return
	 */
	public ArrayList<SysShortCutItemInfo> getSysShortCutRecords() {
		ArrayList<SysShortCutItemInfo> list = new ArrayList<SysShortCutItemInfo>();

		Cursor cursor = mDataProvider.getAllRecord(SysShortcutTable.TABLENAME);
		if (null != cursor) {
			int intentIndex = cursor.getColumnIndex(SysShortcutTable.INTENT);
			int nameIndex = cursor.getColumnIndex(SysShortcutTable.NAME);
			int iconIndex = cursor.getColumnIndex(SysShortcutTable.ICON);
			int countIndex = cursor.getColumnIndex(SysFolderTable.REFCOUNT);
			boolean bData = cursor.moveToFirst();
			if (bData) {
				do {
					String intenStr = cursor.getString(intentIndex);
					Intent intent = ConvertUtils.stringToIntent(intenStr);
					String name = cursor.getString(nameIndex);
					byte[] iconData = cursor.getBlob(iconIndex);
					BitmapDrawable icon = ConvertUtils.createBitmap(mContext, iconData);
					int count = cursor.getInt(countIndex);

					// if (null != intent && null != name && null != icon)
					{
						SysShortCutItemInfo info = new SysShortCutItemInfo();
						info.mIntent = intent;
						info.mTitle = name;
						info.mIcon = icon;
						info.mRefCount = count;

						list.add(info);
					}

					bData = cursor.moveToNext();
				} while (bData);
			}
			cursor.close();
		}

		return list;
	}
}
