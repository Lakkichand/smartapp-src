package com.jiubang.ggheart.data.statistics;

import android.content.Context;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 功能表搜索统计数据
 * 
 * @author dingzijian
 */
public class StatisticsAppFuncSearch {

	public static final String APPFUNC_SEARCH_USED_TIMES = "appfunc_search_used_times";
	public static final String APPFUNC_SEARCH_LOCAL_TIMES = "local_search_times";
	public static final String APPFUNC_SEARCH_WEB_TIMES = "web_search_times";
	public static final String APPFUNC_SEARCH_NAVIGATION_USED_TIMES = "appfunc_search_navigation_times";
	public static final String APPFUNC_SEARCH_FIND_OUT_TIMES = "appfunc_search_find_out_times";
	private static Object sLock = new Object();
	/**
	 * 记录关键字搜索数据，只接受int型数值，非记录点击记数类的统计数据都用这个
	 * 
	 * @param key
	 *            关键字
	 * @param value
	 *            次数
	 */
	public static void countSearchStatistics(final Context context, final String key) {
		if (context != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (sLock) {
						PreferencesManager sp = new PreferencesManager(context,
								IPreferencesIds.APPFUNC_SEARCH_STATISTIC_DATA, Context.MODE_PRIVATE);
						int count = 0;
						String value = sp.getString(key, "0");
						try {
							count = Integer.valueOf(value);
						} catch (Exception e) {
						}
						count++;
						sp.putString(key, String.valueOf(count));
						sp.commit();
					}
				}
			}).start();
		}
	}
	public static String getSearchStatistics(Context context, String key) {
		String value = null;
		if (context != null) {
			PreferencesManager sp = new PreferencesManager(context,
					IPreferencesIds.APPFUNC_SEARCH_STATISTIC_DATA, Context.MODE_PRIVATE);
			value = sp.getString(key, "0");
		}
		return value;

	}
	/**
	 * 获取功能表搜索统计数据
	 * 
	 * @param context
	 * @param type
	 *            分为本地和网络两种
	 */
	public static String getSearchStatisticsData(Context context) {
		StringBuffer dataSb = new StringBuffer();
		dataSb.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_FUNTAB_SEARCH);
		dataSb.append(Statistics.STATISTICS_DATA_SEPARATE_STRING);
		dataSb.append(getSearchStatistics(context, APPFUNC_SEARCH_LOCAL_TIMES));
		dataSb.append(Statistics.STATISTICS_DATA_SEPARATE_STRING);
		dataSb.append(getSearchStatistics(context, APPFUNC_SEARCH_WEB_TIMES));
		dataSb.append(Statistics.STATISTICS_DATA_SEPARATE_STRING);
		dataSb.append(getSearchStatistics(context, APPFUNC_SEARCH_NAVIGATION_USED_TIMES));
		dataSb.append(Statistics.STATISTICS_DATA_SEPARATE_STRING);
		dataSb.append(getSearchStatistics(context, APPFUNC_SEARCH_USED_TIMES));
		dataSb.append(Statistics.STATISTICS_DATA_SEPARATE_STRING);
		dataSb.append(getSearchStatistics(context, APPFUNC_SEARCH_FIND_OUT_TIMES));
		return dataSb.toString();

	}

	/**
	 * 上传后清理数据
	 * 
	 * @param context
	 */
	public static void clean(Context context) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.APPFUNC_SEARCH_STATISTIC_DATA, Context.MODE_PRIVATE);
		sp.clear();
	}

	/**
	 * 在日志中打印所有搜索统计数据
	 */
	public static void paintLogData(Context context) {

	}
}
