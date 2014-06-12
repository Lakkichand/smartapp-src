package com.jiubang.ggheart.data.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;

import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.Setting;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.tables.AppSettingTable;
import com.jiubang.ggheart.data.tables.PartsTable;
import com.jiubang.ggheart.data.tables.RecentAppTable;

/**
 * 用于封装与业务逻辑相关的数据以及对数据的处理方法
 * 
 * @author HuYong
 * @version 1.0
 */
public class DataModelTmp {

	private Context mContext = null;

	protected DataProvider mDataProvider = null;

	public static DataModelTmp getDataModel(Context context) {

		if (modelSelf == null) {
			modelSelf = new DataModelTmp(context);
		}
		return modelSelf;
	}

	private static DataModelTmp modelSelf = null;

	// TODO:修改，将datamodel设计为基类，各子模块自行拥有各子model
	protected DataModelTmp(Context context) {
		mContext = context;
		mDataProvider = DataProvider.getInstance(context);
		// startLoadData();
	}

	public void addRecentAppItem(final long itemId, final int recentAppIndex) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(RecentAppTable.PARTID, itemId);
		contentValues.put(RecentAppTable.INDEX, recentAppIndex);
		mDataProvider.addRecentAppItem(contentValues);
	}

	/**
	 * 获取功能表设置信息
	 * 
	 * @author huyong
	 * @param settingItem
	 * @return 返回对应设置项的值
	 */
	public String getAppSetting(final String packageName, int settingItem) {
		String columnName = null;
		switch (settingItem) {
			case Setting.MENUAPPSTYLE :
				columnName = AppSettingTable.MENUAPPSTYLE;
				break;
			case Setting.TURNSCREENDIRECTION :
				columnName = AppSettingTable.TURNSCREENDIRECTION;
				break;
			case Setting.APPNAMEVISIABLE :
				columnName = AppSettingTable.APPNAMEVISIABLE;
				break;
			case Setting.LINECOLUMNNUM :
				columnName = AppSettingTable.LINECOLUMNNUM;
				break;
			case Setting.BACKGROUNDPICPATH :
				columnName = AppSettingTable.BACKGROUNDPICPATH;
				break;
			case Setting.BGVISIABLE :
				columnName = AppSettingTable.BACKGROUNDVISIABLE;
				break;
			case Setting.SORTTYPE :
				columnName = AppSettingTable.SORTTYPE;
				break;
			case Setting.SHOWNEGLECTAPP :
				columnName = AppSettingTable.SHOWNEGLECTAPPS;
				break;
			case Setting.INOUTEFFECT :
				columnName = AppSettingTable.INOUTEFFECT;
				break;
			case Setting.ICONEFFECT :
				columnName = AppSettingTable.ICONEFFECT;
				break;
			case Setting.APPFUNC_COLNUM :
				columnName = AppSettingTable.COLNUM;
				break;
			case Setting.APPFUNC_ROWNUM :
				columnName = AppSettingTable.ROWNUM;
				break;
			default :
				break;
		}

		Cursor cursor = mDataProvider.getAppSetting(packageName, columnName);
		String value = null;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					int valueIndex = cursor.getColumnIndex(columnName);
					value = cursor.getString(valueIndex);
				}
			} finally {
				cursor.close();
			}
		}
		return value;
	}

	/**
	 * 更新功能表设置信息
	 * 
	 * @author huyong
	 * @param settingItem
	 * @param value
	 */
	public void setAppSetting(final String packageName, final int settingItem, final String value) {
		String columnName = null;
		switch (settingItem) {
			case Setting.MENUAPPSTYLE :
				columnName = AppSettingTable.MENUAPPSTYLE;
				break;
			case Setting.TURNSCREENDIRECTION :
				columnName = AppSettingTable.TURNSCREENDIRECTION;
				break;
			case Setting.APPNAMEVISIABLE :
				columnName = AppSettingTable.APPNAMEVISIABLE;
				break;
			case Setting.LINECOLUMNNUM :
				columnName = AppSettingTable.LINECOLUMNNUM;
				break;
			case Setting.BACKGROUNDPICPATH :
				columnName = AppSettingTable.BACKGROUNDPICPATH;
				break;
			case Setting.BGVISIABLE :
				columnName = AppSettingTable.BACKGROUNDVISIABLE;
				break;
			case Setting.SORTTYPE :
				columnName = AppSettingTable.SORTTYPE;
				break;
			case Setting.SHOWNEGLECTAPP :
				columnName = AppSettingTable.SHOWNEGLECTAPPS;
				// add by huyong 2011-03-17
				break;
			// add by huyong 2011-03-17 end
			case Setting.INOUTEFFECT :
				columnName = AppSettingTable.INOUTEFFECT;
				break;
			case Setting.ICONEFFECT :
				columnName = AppSettingTable.ICONEFFECT;
				break;
			case Setting.APPFUNC_ROWNUM :
				columnName = AppSettingTable.ROWNUM;
				break;
			case Setting.APPFUNC_COLNUM :
				columnName = AppSettingTable.COLNUM;
				break;
			default :
				break;
		}

		ContentValues values = new ContentValues();
		values.put(columnName, value);

		mDataProvider.setAppSetting2(packageName, values);
	}

	/**
	 * 添加设置项
	 * 
	 * @param packageName
	 * @param value
	 */
	public void addAppSetting(final String packageName) {
		ContentValues value = new ContentValues();
		value.put(AppSettingTable.THEMEPACKAGENAME, packageName);
		value.put(AppSettingTable.MENUAPPSTYLE, AppSettingDefault.MENUAPPSTYLE);
		value.put(AppSettingTable.TURNSCREENDIRECTION, AppSettingDefault.TURNSCREENDIRECTION);
		value.put(AppSettingTable.APPNAMEVISIABLE, AppSettingDefault.APPNAMEVISIABLE);
		value.put(AppSettingTable.LINECOLUMNNUM, AppSettingDefault.LINECOLUMNNUM);
		value.put(AppSettingTable.BACKGROUNDPICPATH, AppSettingDefault.BACKGROUNDPICPATH);
		value.put(AppSettingTable.BACKGROUNDVISIABLE, AppSettingDefault.BACKGROUNDVISIABLE);
		value.put(AppSettingTable.SORTTYPE, AppSettingDefault.SORTTYPE);
		value.put(AppSettingTable.SHOWNEGLECTAPPS, AppSettingDefault.SHOWNEGLECTAPPS);
		value.put(AppSettingTable.INOUTEFFECT, AppSettingDefault.INOUTEFFECT);
		value.put(AppSettingTable.ICONEFFECT, AppSettingDefault.ICONEFFECT);
		mDataProvider.addAppSetting(packageName, value);
	}

	/**
	 * 重置功能表设置
	 * 
	 * @author huyong
	 */
	public void resetAppSetting() {
		String value = Integer.toString(0);
		mDataProvider.setAppSetting(Setting.MENUAPPSTYLE, value);
		mDataProvider.setAppSetting(Setting.TURNSCREENDIRECTION, value);
		mDataProvider.setAppSetting(Setting.APPNAMEVISIABLE, value);
		mDataProvider.setAppSetting(Setting.LINECOLUMNNUM, value);
		mDataProvider.setAppSetting(Setting.BACKGROUNDPICPATH, value);

	}

	/**
	 * 通过uri获取itemid
	 * 
	 * @author huyong
	 * @param itemUri
	 * @return
	 */
	public long getItemIdByUri(Uri itemUri) {
		long itemId = -1;
		Cursor cursor = mDataProvider.getItemIdByUri(itemUri);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					itemId = cursor.getLong(cursor.getColumnIndex(PartsTable.ID));
				}
			} finally {
				cursor.close();
			}
		}
		return itemId;
	}

	public String getItemTitleByIntent(final Intent intent) {
		if (intent == null) {
			return null;
		}
		PackageManager pm = mContext.getPackageManager();
		String title = null;
		try {
			title = pm.getActivityInfo(intent.getComponent(), 0).loadLabel(pm).toString();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return title;
	}

	/**
	 * ********************************添加统计模块********************************
	 */
	/**
	 * 更新统计
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public void updateStatistics(final int key, final long value) {
		mDataProvider.updateStatistics(key, value);
	}

	/**
	 * 查询统计
	 * 
	 * @return 数据
	 */
	public Cursor queryStatistics() {
		return mDataProvider.queryStatistics();
	}

}
