package com.jiubang.ggheart.data.statistics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jiubang.ggheart.data.statistics.tables.MonitorAppsTable;

/**
 * 监控程序安装后的统计管理类
 * 
 * @author huyong
 * 
 */
public class MonitorAppstatisManager {

	private final long MAX_SPAN_TIME = 30 * 60 * 1000; // 30min间隔内有效

	public static final int TYPE_FROM_GOSOTORE = 1; // GOStore统计
	public static final int TYPE_FROM_THEMESMANAGER = 2; // 主题工具

	private static MonitorAppstatisManager mManagerSelf = null;
	private Context mContext = null;

	public static synchronized MonitorAppstatisManager getInstance(Context context) {
		if (mManagerSelf == null) {
			mManagerSelf = new MonitorAppstatisManager(context);
		}
		return mManagerSelf;
	}

	private MonitorAppstatisManager(Context context) {
		mContext = context;
	}

	private IMonitorAppInstallListener produceListener(int type) {
		IMonitorAppInstallListener listener = null;
		switch (type) {
			case TYPE_FROM_GOSOTORE :
				listener = GoStoreAppStatistics.getInstance(mContext);
				break;

			case TYPE_FROM_THEMESMANAGER :
				listener = GuiThemeStatistics.getInstance(mContext);
				break;

			default :
				break;
		}
		return listener;
	}

	/**
	 * 处理监听者希望处理app安装
	 * 
	 * @param context
	 * @param pkgName
	 * @param listener
	 * @param listenerType
	 * @param listenerKey
	 */
	public void handleMonitorAppInstall(String pkgName, int listenerType, String listenerKey) {

		// 首先检查数据库中是否已存在该项纪录，若有，则比较时间，并更新，若无或时间已过期，则直接插入一条心纪录
		StatisticsDataProvider dataProvider = StatisticsDataProvider.getInstance(mContext);
		String selection = MonitorAppsTable.PKG_NAME + " = '" + pkgName + "' and "
				+ MonitorAppsTable.SRC_TYPE + " = " + listenerType;
		String[] projection = { MonitorAppsTable.ACTION_TIME };
		String sortOrder = MonitorAppsTable.ACTION_TIME + " DESC";
		Cursor cursor = dataProvider.queryData(MonitorAppsTable.TABLENAME, projection, selection,
				null, null, null, sortOrder);
		// 有合法的数据
		boolean hasValidData = false;
		long validTime = 0L;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					long curTime = System.currentTimeMillis(); // 当前时间
					int actionTimeIndex = cursor.getColumnIndex(MonitorAppsTable.ACTION_TIME);
					do {
						long actionTime = cursor.getLong(actionTimeIndex);
						if (checkTimeValid(actionTime, curTime)) {
							// 有效安装，最近的有效期内，则需更新当前纪录
							hasValidData = true;
							validTime = actionTime;
							break;
						}

					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}
		}

		ContentValues values = createContentValues(pkgName, listenerType, listenerKey);
		if (hasValidData) {
			// 已有合法值，则直接更新合法值
			selection += " and " + MonitorAppsTable.ACTION_TIME + " = " + validTime;
			dataProvider.updateData(MonitorAppsTable.TABLENAME, values, selection);
		} else {
			// 插入一条新纪录
			dataProvider.insertData(MonitorAppsTable.TABLENAME, values);
		}

	}

	/**
	 * 插入一条新纪录
	 * 
	 * @param pkgName
	 * @param listenerType
	 * @param listenerKey
	 */
	private ContentValues createContentValues(String pkgName, int listenerType, String listenerKey) {
		long curTime = System.currentTimeMillis();
		// 插入一条新数据到表中
		ContentValues values = new ContentValues();
		values.put(MonitorAppsTable.PKG_NAME, pkgName);
		values.put(MonitorAppsTable.ACTION_TIME, curTime);
		values.put(MonitorAppsTable.SRC_TYPE, listenerType);
		values.put(MonitorAppsTable.SRC_KEYS, listenerKey);

		return values;

	}

	/**
	 * 处理app安装事件
	 * 
	 * @param pkgName
	 */
	public void handleAppInstalled(String pkgName) {
		// TODO:根据包名，查询返回值，根据时间值来确定是否需要通知，
		// 然后利用type，定位到是某一个或是几个listener通知更新安装量

		StatisticsDataProvider dataProvider = StatisticsDataProvider.getInstance(mContext);

		String selection = MonitorAppsTable.PKG_NAME + " = '" + pkgName + "'";
		Cursor cursor = dataProvider.queryData(MonitorAppsTable.TABLENAME, null, selection, null,
				null, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					long curTime = System.currentTimeMillis(); // 当前时间
					int actionTimeIndex = cursor.getColumnIndex(MonitorAppsTable.ACTION_TIME);
					int listenerTypeIndex = cursor.getColumnIndex(MonitorAppsTable.SRC_TYPE);
					int listenerKeyIndex = cursor.getColumnIndex(MonitorAppsTable.SRC_KEYS);
					do {
						long actionTime = cursor.getLong(actionTimeIndex);
						if (checkTimeValid(actionTime, curTime)) {
							int listenerType = cursor.getInt(listenerTypeIndex);
							String listenerKey = cursor.getString(listenerKeyIndex);
							// 有效安装，需要通知各listener
							produceListener(listenerType)
									.onHandleAppInstalled(pkgName, listenerKey);
						}

					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

		}

	}

	private boolean checkTimeValid(long oldTime, long curTime) {
		return curTime - oldTime <= MAX_SPAN_TIME ? true : false;
	}

	/**
	 * 清除监控数据
	 */
	public void clearMonitorData() {
		StatisticsDataProvider dataProvider = StatisticsDataProvider.getInstance(mContext);

		dataProvider.delete(MonitorAppsTable.TABLENAME, null, null);
	}
}
