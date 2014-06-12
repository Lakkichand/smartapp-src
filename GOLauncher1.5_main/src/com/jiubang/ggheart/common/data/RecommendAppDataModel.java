package com.jiubang.ggheart.common.data;

import java.util.HashSet;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.BaseDataModel;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.PersistenceManager;
import com.jiubang.ggheart.data.tables.RecommendAppTable;

/**
 * 推荐应用数据库操作类
 * @author yejijiong
 *
 */
public class RecommendAppDataModel extends BaseDataModel {

	public RecommendAppDataModel(Context context) {
		super(context, PersistenceManager.DB_ANDROID_HEART);
	}
	
	/**
	 * 获取所有需要显示New标识的推荐应用Intent列表
	 * @return
	 */
	public HashSet<Intent> getAllShowNewRecommendApps() {
		String where = RecommendAppTable.SHOWNEW + "=1";
		Cursor cursor = mManager.query(RecommendAppTable.TABLENAME, null, where, null, null, null, null);
		HashSet<Intent> recommendAppList = new HashSet<Intent>();
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						int intentIndex = cursor.getColumnIndex(RecommendAppTable.INTENT);
						String intent = cursor.getString(intentIndex);
						recommendAppList.add(ConvertUtils.stringToIntent(intent));
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return recommendAppList;
	}
	
	/**
	 * 添加推荐应用到推荐应用表中
	 * @param intent
	 * @param isNeedShowNew
	 * @return
	 */
	public boolean addRecommendApps(Map<Intent, Boolean> map) {
		boolean flag = false;
		int showNew = 0;
		try {
			mManager.beginTransaction();
			for (Intent intent : map.keySet()) {
				showNew = map.get(intent) ? 1 : 0;
				ContentValues cv = new ContentValues();
				cv.put(RecommendAppTable.INTENT, ConvertUtils.intentToString(intent));
				cv.put(RecommendAppTable.SHOWNEW, showNew);
				mManager.insert(RecommendAppTable.TABLENAME, cv);
			}
			mManager.setTransactionSuccessful();
			flag = true;
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			try {
				mManager.endTransaction(false, null);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	
	/**
	 * 更新一个推荐应用是否需要显示New标识
	 * @param intent
	 * @param isNeedShowNew
	 * @return
	 */
	public boolean updateRecommendApp(Map<Intent, Boolean> map) {
		boolean flag = false;
		String whereStr;
		int showNew;
		try {
			mManager.beginTransaction();
			for (Intent intent : map.keySet()) {
				whereStr = RecommendAppTable.INTENT + " = '" + ConvertUtils.intentToString(intent) + "'";
				showNew = map.get(intent) ? 1 : 0;
				ContentValues cv = new ContentValues();
				cv.put(RecommendAppTable.SHOWNEW, showNew);
				mManager.update(RecommendAppTable.TABLENAME, cv, whereStr, null);
			}
			mManager.setTransactionSuccessful();
			flag = true;
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			try {
				mManager.endTransaction(false, null);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	
	/**
	 * 在推荐应用表中删除推荐应用
	 * @param intent
	 * @return
	 */
	public boolean deleteRecommendApps(HashSet<Intent> intentList) {
		Boolean flag = false;
		try {
			mManager.beginTransaction();
			for (Intent intent : intentList) {
				String whereStr = RecommendAppTable.INTENT + " = '" + ConvertUtils.intentToString(intent) + "'";
				mManager.delete(RecommendAppTable.TABLENAME, whereStr, null);
			}
			mManager.setTransactionSuccessful();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				mManager.endTransaction(false, null);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
}
