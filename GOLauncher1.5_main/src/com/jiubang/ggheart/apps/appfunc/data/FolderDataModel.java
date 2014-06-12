package com.jiubang.ggheart.apps.appfunc.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.BaseDataModel;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.PersistenceManager;
import com.jiubang.ggheart.data.tables.AppTable;
import com.jiubang.ggheart.data.tables.FolderTable;

/**
 * 文件夹数据库访问处理
 * @author wuziyi
 *
 */
public class FolderDataModel extends BaseDataModel {

	public FolderDataModel(Context context) {
		super(context, PersistenceManager.DB_ANDROID_HEART);
	}

	/**
	 * 取得文件夹中的元素
	 * 
	 * @param folderId
	 *            文件夹id
	 * @return 游标
	 */
	public Cursor getAppsInFolder(final long folderId) {
		// 表名
		String tables = FolderTable.TABLENAME;
		// 查询列数
		String columns[] = { FolderTable.INTENT, FolderTable.INDEX, FolderTable.USERTITLE,
				FolderTable.TIMEINFOLDER };

		// 排序条件
		String sortOrder = FolderTable.INDEX + " ASC";
		String where = FolderTable.FOLDERID + " = " + folderId;

		return mManager.query(tables, columns, where, null, sortOrder);

	}
	
	/**
	 * 更新元素
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @param values
	 *            新值
	 * @throws DatabaseException
	 */
	public void updateFunAppItemInFolder(final long folderId, final Intent intent,
			final ContentValues values) throws DatabaseException {
		String whereStr = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.INTENT
				+ " = " + "'" + ConvertUtils.intentToString(intent) + "'";
		mManager.update(FolderTable.TABLENAME, values, whereStr, null);
	}
	
	/**
	 * 在文件夹中删除一个程序
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param intent
	 * @throws DatabaseException
	 */
	public void removeFunAppFromFolder(final long folderId, final Intent intent)
			throws DatabaseException {
		if (null == intent) {
			return;
		}

		// 获取index
		int index = getFunAppIndexByIntentInFolder(folderId, intent);
		if (index < 0) {
			// TODO:打日志
			return;
		}
		// 删除
		String str = ConvertUtils.intentToString(intent);
		String selection = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.INTENT + " = " + "'"
				+ str + "'";
		// 删除元素
		mManager.delete(FolderTable.TABLENAME, selection, null);
		
		// 维护index
		String updateSql = "update " + FolderTable.TABLENAME + " set " + FolderTable.INDEX
				+ " = " + FolderTable.INDEX + " - 1 " + " where " + FolderTable.FOLDERID
				+ " = " + folderId + " and " + FolderTable.INDEX + " > " + index + ";";
		mManager.exec(updateSql);
	}
	
	/**
	 * 获取应用在文件夹中的index
	 * @param folderId
	 * @param intent
	 * @return
	 */
	private int getFunAppIndexByIntentInFolder(final long folderId, final Intent intent) {
		if (null == intent) {
			return -1;
		}

		String str = ConvertUtils.intentToString(intent);
		String selection = FolderTable.FOLDERID + " = " + folderId + " and " + FolderTable.INTENT
				+ " = " + "'" + str + "'";

		// 查询列数
		String columns[] = { FolderTable.INDEX };

		int retIndex = -1;
		Cursor cursor = mManager.query(FolderTable.TABLENAME, columns, selection, null, null);
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					int idx = cursor.getColumnIndex(FolderTable.INDEX);
					retIndex = cursor.getInt(idx);
				}
			} finally {
				cursor.close();
			}
		}

		return retIndex;
	}
	
	/**
	 * 获取文件夹中元素个数
	 * 
	 * @param folderId
	 *            文件夹id
	 * @return 元素个数
	 */
	public int getSizeOfFolder(final long folderId) {
		String table = FolderTable.TABLENAME;
		String where = FolderTable.FOLDERID + " = " + folderId;
		Cursor cursor = mManager.query(table, null, where, null, null);
		int count = 0;
		if (cursor != null) {
			try {
				count = cursor.getCount();
			} finally {
				cursor.close();
			}
		}
		return count;
	}
	
	/**
	 * 更新
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param values
	 *            新值
	 * @throws DatabaseException
	 */
	public void updateFunAppItem(final long folderId, final ContentValues values)
			throws DatabaseException {
		String whereStr = AppTable.FOLDERID + " = " + folderId;
		mManager.update(AppTable.TABLENAME, values, whereStr, null);
	}
	
	/**
	 * 在文件夹中添加一个程序,并维护index
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param index
	 *            插入位置
	 * @param intent
	 *            程序唯一标识
	 * @throws DatabaseException
	 */
	public void addFunAppToFolder(final long folderId, final int index, final Intent intent,
			final String title) throws DatabaseException {
		if (null == intent) {
			return;
		}
		if (index < 0) {
			return;
		}

//		// 获取folderId对于的文件夹的size
//		int size = getSizeOfFolder(folderId);

		// 维护index
		String updateSql = "update " + FolderTable.TABLENAME + " set " + FolderTable.INDEX + " = "
				+ FolderTable.INDEX + " + 1 " + " where " + FolderTable.FOLDERID + " = " + folderId
				+ " and " + FolderTable.INDEX + " >= " + index + ";";
		mManager.exec(updateSql);

//		// 处理特殊情况
//		int idx = index > size ? size : index;
		// 添加
		ContentValues contentValues = new ContentValues();
		contentValues.put(FolderTable.FOLDERID, folderId);
		contentValues.put(FolderTable.INDEX, index);
		contentValues.put(FolderTable.INTENT, ConvertUtils.intentToString(intent));
		contentValues.put(FolderTable.USERTITLE, title);
		contentValues.put(FolderTable.TIMEINFOLDER, System.currentTimeMillis());
		mManager.insert(FolderTable.TABLENAME, contentValues);
	}
	
}
