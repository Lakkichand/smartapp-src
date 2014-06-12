package com.jiubang.ggheart.data.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.info.SysFolderItemInfo;
import com.jiubang.ggheart.data.tables.SysFolderTable;

public class SysFolderDataModel extends DataModel {
	public SysFolderDataModel(Context context) {
		super(context);
	}

	/**
	 * 增加文件夹表
	 * 
	 * @param intent
	 * @param name
	 * @param icon
	 */
	public void addSysFolderRecord(Intent intent, Uri uri, int displayMode, String name,
			BitmapDrawable icon) {
		ContentValues values = new ContentValues();
		values.put(SysFolderTable.INTENT, ConvertUtils.intentToString(intent));
		values.put(SysFolderTable.URI, ConvertUtils.uriToString(uri));
		values.put(SysFolderTable.DISPLAYMODE, displayMode);
		values.put(SysFolderTable.NAME, name);
		ConvertUtils.saveBitmapToValues(values, SysFolderTable.ICON, icon);
		values.put(SysFolderTable.REFCOUNT, 1);

		mDataProvider.addRecord(SysFolderTable.TABLENAME, values);
	}

	/**
	 * 更新文件夹被应用次数
	 * 
	 * @param intent
	 * @param refCount
	 */
	public void updateSysFolderRefCount(Intent intent, Uri uri, int refCount) {
		ContentValues values = new ContentValues();
		values.put(SysFolderTable.REFCOUNT, refCount);

		String intentStr = ConvertUtils.intentToString(intent);
		String uriStr = ConvertUtils.uriToString(uri);

		String selection = SysFolderTable.INTENT + " = " + "'" + intentStr + "'" + " and "
				+ SysFolderTable.URI + " = " + "'" + uriStr + "'";
		mDataProvider.updateRecord(SysFolderTable.TABLENAME, values, selection);
	}

	/**
	 * 删除文件夹表
	 * 
	 * @param intent
	 */
	public void delSysFolderRecord(Intent intent, Uri uri) {
		String intentStr = ConvertUtils.intentToString(intent);
		String uriStr = ConvertUtils.uriToString(uri);

		String selection = SysFolderTable.INTENT + " = " + "'" + intentStr + "'" + " and "
				+ SysFolderTable.URI + " = " + "'" + uriStr + "'";
		mDataProvider.delRecord(SysFolderTable.TABLENAME, selection);
	}

	/**
	 * 获取系统文件夹源
	 * 
	 * @return
	 */
	public ArrayList<SysFolderItemInfo> getSysFolderRecords() {
		ArrayList<SysFolderItemInfo> list = new ArrayList<SysFolderItemInfo>();

		Cursor cursor = mDataProvider.getAllRecord(SysFolderTable.TABLENAME);
		if (null != cursor) {
			int intentIndex = cursor.getColumnIndex(SysFolderTable.INTENT);
			int uriIndex = cursor.getColumnIndex(SysFolderTable.URI);
			int displayModeIndex = cursor.getColumnIndex(SysFolderTable.DISPLAYMODE);
			int nameIndex = cursor.getColumnIndex(SysFolderTable.NAME);
			int iconIndex = cursor.getColumnIndex(SysFolderTable.ICON);
			int countIndex = cursor.getColumnIndex(SysFolderTable.REFCOUNT);
			boolean bData = cursor.moveToFirst();
			if (bData) {
				do {
					String intenStr = cursor.getString(intentIndex);
					Intent intent = ConvertUtils.stringToIntent(intenStr);
					String uriStr = cursor.getString(uriIndex);
					Uri uri = ConvertUtils.stringToUri(uriStr);
					int displayMode = cursor.getInt(displayModeIndex);
					String name = cursor.getString(nameIndex);
					byte[] iconData = cursor.getBlob(iconIndex);
					BitmapDrawable icon = ConvertUtils.createBitmap(mContext, iconData);
					int count = cursor.getInt(countIndex);

					// if (null != intent && null != name && null != icon)
					{
						SysFolderItemInfo info = new SysFolderItemInfo();
						info.mIntent = intent;
						info.mUri = uri;
						info.mDisplayMode = displayMode;
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
