package com.jiubang.ggheart.common.data;

import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.jiubang.ggheart.data.BaseDataModel;
import com.jiubang.ggheart.data.PersistenceManager;
import com.jiubang.ggheart.data.tables.AppClassifyTable;
/**
 * 应用分类数据库查询操作
 */
public class AppClassifyDataModel extends BaseDataModel {

	public AppClassifyDataModel(Context context) {
		super(context, PersistenceManager.DB_APP_CLASSIFY);
	}
	
	// 只提供两个方法，单个查询，多个查询, 查不到map可能会为empty喔
	public HashMap<String, Integer> getAllAppClassifyItems(Set<String> packageNames) {
		HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
		Cursor cursor = null;
		try {
			cursor = getAllAppsClassify(packageNames);
		} catch (SQLException e) {
			return resultMap;
		}
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						int pkgIndex = cursor.getColumnIndex(AppClassifyTable.PACKAGE_NAME);
						int classificationIndex = cursor
								.getColumnIndex(AppClassifyTable.CLASSIFICATION);
						String packageName = cursor.getString(pkgIndex);

						int classification = cursor.getInt(classificationIndex);
						resultMap.put(packageName, classification);
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				cursor.close();
			}
		}
		return resultMap;
	}
	
	/**
	 * 查询单个包名的分类
	 * @param packageName
	 * @return
	 */
	public HashMap<String, Integer> getAppClassify(String packageName) {
		Cursor cursor = null;
		HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
		try {
			String[] args = new String[1];
			args[0] = packageName;
			String where = AppClassifyTable.PACKAGE_NAME + "=?";
			cursor = mManager.query(AppClassifyTable.TABLE_NAME, null, where, args, null, null, null);
		} catch (SQLException e) {
			return resultMap;
		}
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						int pkgIndex = cursor.getColumnIndex(AppClassifyTable.PACKAGE_NAME);
						int classificationIndex = cursor
								.getColumnIndex(AppClassifyTable.CLASSIFICATION);
						String pkg = cursor.getString(pkgIndex);

						int classification = cursor.getInt(classificationIndex);
						resultMap.put(pkg, classification);
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				cursor.close();
			}
		}
		return resultMap;
	}
	
	/**
	 * 根据包名列表获得分类列表
	 * @param packageNames
	 * @return
	 */
	public Cursor getAllAppsClassify(Set<String> packageNames) {
		if (packageNames.isEmpty()) {
			return null;
		}
		String where = null;
		String[] args = new String[packageNames.size()];
		StringBuilder whereBuf = new StringBuilder();
		whereBuf.append(AppClassifyTable.PACKAGE_NAME).append(" in (");
		int i = 0;
		for (String pkg : packageNames) {
			whereBuf.append("?,");
			args[i++] = pkg;
		}
		whereBuf.delete(whereBuf.lastIndexOf(","), whereBuf.length()).append(")");
		where = whereBuf.toString();
		
		return mManager.query(AppClassifyTable.TABLE_NAME, null, where, args, null, null, null);
	}
	
}
