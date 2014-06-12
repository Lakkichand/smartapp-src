package com.jiubang.ggheart.data.statistics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jiubang.ggheart.data.DatabaseException;

/**
 * 
 * 类描述:统计数据的处理类提供业务层调用
 * 功能详细描述:
 * 
 * @author  huyong
 * @date  [2012-9-18]
 */
public class StatisticsDataProvider {

	private StatisticsDataBaseHelper mDBHelper;

	private static StatisticsDataProvider sDataProvider = null;

	public static synchronized final StatisticsDataProvider getInstance(Context context) {
		if (null == sDataProvider) {
			sDataProvider = new StatisticsDataProvider(context);
		}
		return sDataProvider;
	}

	private StatisticsDataProvider(Context context) {
		mDBHelper = new StatisticsDataBaseHelper(context);
	}

	public void insertData(String tableName, ContentValues values) {
		try {
			mDBHelper.insert(tableName, values);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Cursor queryData(String tableName, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having, String sortOrder) {
		return mDBHelper.query(tableName, projection, selection, selectionArgs, groupBy, having,
				sortOrder);
	}

	public int delete(String tableName, String selection, String[] selectionArgs) {
		int count = 0;
		try {
			count = mDBHelper.delete(tableName, selection, selectionArgs);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}

	public void updateData(String tableName, ContentValues values, String selection) {
		try {
			mDBHelper.update(tableName, values, selection, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void exeSql(String sql) {
		try {
			mDBHelper.exec(sql);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 是否存在某条记录
	 * 
	 * @param tableName
	 *            待查询的表名
	 * @param projection
	 *            返回的字段列，可为null，将返回全部字段列，影响一定效率。
	 * @param selection
	 *            条件语句，不需要where，多条件之间可用and连接
	 * @return
	 */
	public boolean isExistData(String tableName, String selection, String[] projection) {
		Cursor cursor = queryData(tableName, projection, selection, null, null, null, null);
		boolean result = false;
		if (cursor != null) {
			result = cursor.getCount() > 0;
			cursor.close();
		}
		return result;
		
	}

	/**
	 * 检查指定的表是否存在
	 * 
	 * @author zhouxuewen
	 * @param tableName
	 *            要检查是否在的表的表名
	 * @return
	 */
	public boolean isExistTable(String tableName) {
		boolean result = false;
		try {
			result = mDBHelper.isExistTable(tableName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	/**
	 * 创建表
	 * 
	 * @author zhouxuewen
	 * @param tabName
	 *            要创建的表的表名
	 * @return
	 */
	public void createTab(String tabName) {
		try {
			mDBHelper.createTab(tabName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
