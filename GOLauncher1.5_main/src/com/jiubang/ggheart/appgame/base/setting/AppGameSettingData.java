/*
 * 文 件 名:  AppGameSettingData.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-25
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.setting;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

import com.jiubang.ggheart.appgame.base.database.AppGameProviderConstants;

/**
 * 类描述:应用游戏中心的设置值，以及保存方法 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-25]
 */
public class AppGameSettingData {
	/**
	 * 在数据网络下不加载图标/截图
	 */
	public static final int NOT_LOADING_IMAGES = 0;
	/**
	 * 在数据网络下只加载图标
	 */
	public static final int NOT_LOADING_SCREENSHOT_MOBILE_DATA = 1;
	/**
	 * 在数据网络下加载全部图标/截图
	 */
	public static final int LOADING_ALL_IMAGES = 2;
	/**
	 * 省流量模式的值
	 */
	private int mTrafficSavingMode = LOADING_ALL_IMAGES;

	private static AppGameSettingData sInstance = null;

	private Context mContext = null;
	/**
	 * 设置值更改的ContentObserver
	 */
	private ContentObserver mContentObserver = new ContentObserver(new Handler()) {

		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}

		@Override
		public void onChange(boolean selfChange) {
			//设置值更改后重新加载设置
			roadSettingDataFromDb();
			super.onChange(selfChange);
		}
	};

	public synchronized static AppGameSettingData getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new AppGameSettingData(context);
		}
		return sInstance;
	}

	private AppGameSettingData(Context context) {
		mContext = context;
		//加载设置数据
		roadSettingDataFromDb();
		//注册设置监听
		mContext.getContentResolver().registerContentObserver(
				AppGameProviderConstants.CONTENT_DATA_URI, false, mContentObserver);
	}

	public int getTrafficSavingMode() {
		return mTrafficSavingMode;
	}
	/**
	 * <br>功能简述:加载设置信息的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void roadSettingDataFromDb() {
		if (mContext == null) {
			mTrafficSavingMode = LOADING_ALL_IMAGES;
			return;
		}
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = contentResolver.query(AppGameProviderConstants.CONTENT_DATA_URI, null,
				null, null, null);
		try {
			if (cursor == null || cursor.getCount() <= 0) {
				mTrafficSavingMode = LOADING_ALL_IMAGES;
			} else {
				cursor.moveToFirst();
				int index = cursor.getColumnIndex(AppGameSettingTable.TRAFFIC_SAVING_MODE);
				mTrafficSavingMode = cursor.getInt(index);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public boolean updateValue(String column, int value) {
		if (mContext == null || column == null) {
			return false;
		}
		ContentResolver contentResolver = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(column, value);
		int rowCount = contentResolver.update(AppGameProviderConstants.CONTENT_DATA_URI, values,
				null, null);
		boolean result = rowCount > 0 ? true : false;
		if (column.equals(AppGameSettingTable.TRAFFIC_SAVING_MODE) && result) {
			mTrafficSavingMode = value;
		}
		return result;
	}

	private void cleanUp() {
		if (mContext != null) {
			if (mContentObserver != null) {
				mContext.getContentResolver().unregisterContentObserver(mContentObserver);
			}
			mContext = null;
		}
	}

	public synchronized static void destory() {
		if (sInstance != null) {
			sInstance.cleanUp();
			sInstance = null;
		}
	}

}
