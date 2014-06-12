package com.jiubang.ggheart.apps.appfunc.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.PersistenceManager;
import com.jiubang.ggheart.data.tables.RecentAppTable;
import com.jiubang.ggheart.data.BaseDataModel;

/**
 * 
 * <br>类描述: 最近打开数据管理器
 * <br>功能详细描述: 封装了最近打开相关数据库访问操作
 * 
 * @author  yangguanxiang
 * @date  [2012-12-27]
 */
public class RecentAppDataModel extends BaseDataModel {

	public RecentAppDataModel(Context context) {
		super(context, PersistenceManager.DB_ANDROID_HEART);
	}

	/**
	 * <br>功能简述: 获取最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public Cursor getRecentAppItems() {
		String columns[] = { RecentAppTable.INDEX, RecentAppTable.INTENT };
		String sortOrder = RecentAppTable.INDEX + " ASC";
		return mManager.query(RecentAppTable.TABLENAME, columns, null, null, sortOrder);
	}

	/**
	 * <br>功能简述: 添加最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intent
	 * @param index
	 */
	public void addRecentAppItem(final Intent intent, final int index) {
		if (intent == null) {
			return;
		}
		String intentStr = ConvertUtils.intentToString(intent);
		ContentValues contentValues = new ContentValues();
		contentValues.put(RecentAppTable.INDEX, index);
		contentValues.put(RecentAppTable.INTENT, intentStr);
		mManager.insertAsync(RecentAppTable.TABLENAME, contentValues, null);
	}

	/**
	 * <br>功能简述: 删除最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intent
	 */
	public void removeRecentAppItem(final Intent intent) {
		if (intent == null) {
			return;
		}
		String intentStr = ConvertUtils.intentToString(intent);
		String selection = RecentAppTable.INTENT + "=?";
		String[] selectionArgs = new String[] { intentStr };

		mManager.deleteAsync(RecentAppTable.TABLENAME, selection, selectionArgs, null);
	}

	/**
	 * <br>功能简述: 删除所有最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void removeAllRecentAppItems() {
		mManager.deleteAsync(RecentAppTable.TABLENAME, null, null, null);
	}

}
