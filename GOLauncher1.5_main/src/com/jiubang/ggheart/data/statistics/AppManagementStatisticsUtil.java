package com.jiubang.ggheart.data.statistics;

import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 应用管理统计类
 * 
 * @author zhaojunjie
 * 
 */
public class AppManagementStatisticsUtil implements IStatistics {

	// 常量
	// 应用更新的统计数据sharedpreference文件(所有要提交的数据)
	private final static String APPMANAGEMENT_STATISTICS_DATA = "appmanagement_statistics_data";
	// 应用更新的下发次数数据sharedpreference文件(因为下发次数只与应用包名和入口关联，与其它条件无关)
	private final static String APPMANAGEMENT_STATISTICS_ISSUED_DATA = "appmanagement_statistics_issued_data";
	// 应用更新的详细点击数据sharedpreference文件(因为详细点击只与应用包名和入口关联，与其它条件无关)
	private final static String APPMANAGEMENT_STATISTICS_DETAIL_CLICKED_DATA = "appmanagement_statistics_detail_clicked_data";
	// 应用更新的入口sharedpreference文件(由于入口会在不同进程中改变，所以要保存于文件中)(入口缓存)
	private final static String APPMANAGEMENT_STATISTICS_APPMANAGEMENT_ENTER_DATA = "appmanagement_statistics_appmanagement_enter_data";
	// 应用更新的界面入口sharedpreference文件(由于界面入口数据会在不同进程中应用，所以要保存于文件中)(界面入口缓存)
	private final static String APPMANAGEMENT_STATISTICS_APPMANAGEMENT_UIENTER_DATA = "appmanagement_statistics_appmanagement_uienter_data";
	// 记录下载完成的应用sharedpreference文件(由于判断所安装的应用是否是从应用管理中下载)(下载临时数据)
	private final static String APPMANAGEMENT_STATISTICS_APPDOWNLOAD_DATA = "appmanagement_statistics_appdownload_data";
	// 记录正在下载的应用sharedpreference文件(由于判断所下载的应用是否是从应用管理中下载)(下载临时数据)
	private final static String APPMANAGEMENT_STATISTICS_APPDOWNLOADING_DATA = "appmanagement_statistics_appdownloading_data";
	// 记录Tab点击sharedpreference文件
	private final static String APPMANAGEMENT_STATISTICS_TAB_CLICK = "appmanagement_statistics_tab_click";

	private final static String KEY_ENTER = "statistics_appmanagement_enter"; // 入口KEY
	private final static String KEY_UIENTER = "statistics_appmanagement_uienter"; // 界面入口KEY
	public final static String KEY_SPERATE = "|"; // 统计数据中KEY的分隔符
	public final static String ITEM_KEY_SPERATE = "#"; // 统计数据中KEY的分隔符2
	public final static String IEMT_SPERATE = "||"; // 统计数据中统计项的分隔符
	private final static String IEMT_SPERATE_TEMP = ";"; // 由于使用“||”不能分成字符串数组，要先替换为“;”

	private final static int ITEM_COUNT = 13; // 应用管理统计项总数
	private final static int STATISTICS_MARK = 12; // 业务标识

	private final static int ITEM_INDEX_STATISTICS_MARK = 0; // 业务标识 数在记录中的位置
	private final static int ITEM_INDEX_APPID = 1; // 软件id在记录中的位置
	private final static int ITEM_INDEX_ISSUED = 2; // 下发次数 在记录中的位置
	private final static int ITEM_INDEX_DETAIL = 3; // 详情点击次数 在记录中的位置
	// private final static int ITEM_INDEX_DOWNLOAD_CLICK = 4; //下载点击 在记录中的位置
	// private final static int ITEM_INDEX_DOWNLOAD_COMPLETE = 5; //下载量 在记录中的位置
	// private final static int ITEM_INDEX_DOWNLOAD_SETUP = 6; //下载安装 在记录中的位置
	private final static int ITEM_INDEX_UPDATE_CLICK = 4; // 更新点击 在记录中的位置
	private final static int ITEM_INDEX_UPDATE_COMPLETE = 5; // 更新下载量 在记录中的位置
	private final static int ITEM_INDEX_UPDATE_SETUP = 6; // 更新安装 在记录中的位置
	// private final static int ITEM_INDEX_RECOMMEND_CATEGORY_ID = 10; //推荐分类ID
	// 在记录中的位置
	private final static int ITEM_INDEX_RECOMMEND_INDEX = 7; // 推荐位置 在记录中的位置
	private final static int ITEM_INDEX_ENTER_CODE = 8; // 入口标识记录中的位置
	private final static int ITEM_INDEX_RECOMMEND_INTERFACE = 9; // 界面入口 在记录中的位置
	private final static int ITEM_INDEX_VERSION = 10; // 版本协议的位置
	private final static int ITEM_INDEX_UID = 11; // 渠道号的位置
	private final static int ITEM_INDEX_DOWNLOADCANCEL = 12; // 下载取消的位置

	public final static byte ENTRY_TYPE_FUNTAB_ICON = 1; // 功能表菜单入口
	public final static byte ENTRY_TYPE_NOTICE = 2; // 通知栏入口(每8小时显示)
	public final static byte ENTRY_TYPE_GOSTORE = 3; // GoStore入口
	public final static byte ENTRY_TYPE_GOSTORE_ICON = 4; // 功能表--Gostore图标右上角数字
	public final static byte ENTRY_TYPE_APP_ICON = 5; // 功能表--应用图标右上角更新标志(每8小时显示)
	public final static byte ENTRY_TYPE_DESK = 6; // 桌面图标进入
	public final static byte ENTRY_TYPE_APPFUNC_ICON = 7; // 功能表图标进入
	public final static byte ENTRY_TYPE_MENU = 8; // 桌面菜单进入
	public final static byte ENTRY_TYPE_WIDGET = 9; // WIDGET进入
	public final static byte ENTRY_TYPE_APPFUNC_UPDATE = 10; // 功能表更新图标
	public final static byte ENTRY_TYPE_OTHER = 0; // 其它入口

	public final static byte UIENTRY_TYPE_NONE = 0; // 界面入口--没有
	public final static byte UIENTRY_TYPE_LIST = 1; // 界面入口--列表
	public final static byte UIENTRY_TYPE_DETAIL = 1; // 界面入口--详细

	public final static byte TAB_ID_APP = 5; // 一键装机TAB ID
	public final static byte TAB_ID_SEARCH = 6; // 搜索TAB ID
	/**
	 * 我的应用分类id，UI2.0下只能拿到管理的分类id，管理下面我的应用的分类id由客户端写死 add by xiedezhi 2012.7.25
	 */
	public final static int TAB_ID_MYAPP = 2;
	/**
	 * 应用更新分类id，UI2.0下只能拿到管理的分类id，管理下面应用更新的分类id由客户端写死 add by xiedezhi 2012.7.25
	 */
	public final static int TAB_ID_APPUPDATE = 1;
	
	/**
	 * 高级管理分类id，须等淑婷给值，暂时随便写的 LIGUOLIANG
	 */
	public final static int TAB_ID_ADVANCED = 3;

	public final static int NEW_VERSION = 4; // 最新版本协议
	// 初始化单例
	private static AppManagementStatisticsUtil sInstance;

	public static synchronized AppManagementStatisticsUtil getInstance() {

		if (sInstance == null) {
			sInstance = new AppManagementStatisticsUtil();
		}
		return sInstance;
	}

	/**
	 * 取当前入口
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized int getmCurrentEnterCode(Context context) {
		int record = UIENTRY_TYPE_LIST;
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPMANAGEMENT_ENTER_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			record = sharedPreferences.getInt(KEY_ENTER, UIENTRY_TYPE_LIST);
		}
		return record;
	}

	/**
	 * 取界面入口
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized int getmCurrentUIEnterCode(Context context) {
		int record = UIENTRY_TYPE_LIST;
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPMANAGEMENT_UIENTER_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			record = sharedPreferences.getInt(KEY_UIENTER, UIENTRY_TYPE_LIST);
		}
		return record;
	}

	@Override
	public void saveCurrentEnter(Context context, int entercode) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPMANAGEMENT_ENTER_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.putInt(KEY_ENTER, entercode);
			sharedPreferences.commit();
		}
	}

	@Override
	public void saveCurrentUIEnter(Context context, int uientercode) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPMANAGEMENT_UIENTER_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.putInt(KEY_UIENTER, uientercode);
			sharedPreferences.commit();
		}
	}

	@Override
	public void saveAppRecord(Context context, String appid, String name, String packageName,
			boolean isUpdate) {

	}

	@Override
	public void saveIssued(Context context, String packageName, int index) {
		// increaseRecordOneItemValue(context,packageName,String.valueOf(appid),"","",ITEM_INDEX_ISSUED,1,false);
		// delIssuedData(context);
		saveAppIssuedData(context, packageName, index);
	}

	@Override
	public void saveDetailsClick(Context context, String packageName, int appid, int times) {
		// increaseRecordOneItemValue(context,packageName,String.valueOf(appid),"","",ITEM_INDEX_DETAIL,1,false);
		saveAppDetailClickedData(context, packageName);
	}

	@Override
	public void saveDownloadClick(Context context, String packageName, int appid, int times) {
		// increaseRecordOneItemValue(context,packageName,String.valueOf(appid),"",ITEM_INDEX_DOWNLOAD_CLICK,1,false);

	}

	@Override
	public void saveDownloadComplete(Context context, String packageName, String appid, int times) {
		// increaseRecordOneItemValue(context,packageName,appid,"",ITEM_INDEX_DOWNLOAD_COMPLETE,1,true);

	}

	@Override
	public void saveDownloadSetup(Context context, String packageName) {
		// increaseRecordOneItemValue(context,packageName,String.valueOf(appid),"",ITEM_INDEX_DOWNLOAD_SETUP,1,true);

	}

	@Override
	public void saveUpdataClick(Context context, String packageName, int appid, int times) {
		increaseRecordOneItemValue(context, packageName, String.valueOf(appid), "", "",
				ITEM_INDEX_UPDATE_CLICK, times, false);
		if (times != 0) {
			saveAppDownloadData(context, packageName, String.valueOf(appid));
		}
	}

	@Override
	public void saveUpdataComplete(Context context, String packageName, String appid, int times) {
		saveDownloadFinish(context, packageName, appid, times, 1);
	}

	@Override
	public void saveUpdataSetup(Context context, String packageName) {
		saveSetup(context, packageName);

	}

	/**
	 * 记录应用下载完成时的数据(临时数据) 格式：Key:包名 Value:下载时间;应用ID;入口;界面入口;下载类型
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 * @param time
	 * @param downloadtype
	 *            0：下载安装；1：更新安装
	 */
	private void saveAppDownloadData(Context context, String packageName, String appid, long time,
			int downloadtype) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPDOWNLOAD_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			int entercode = AppManagementStatisticsUtil.getmCurrentEnterCode(context);
			int uientercode = AppManagementStatisticsUtil.getmCurrentUIEnterCode(context);

			// 以包名 为KEY
			String key = packageName;
			StringBuffer recordStringBuffer = new StringBuffer();
			recordStringBuffer.append(time).append(IEMT_SPERATE_TEMP).append(appid)
					.append(IEMT_SPERATE_TEMP).append(entercode).append(IEMT_SPERATE_TEMP)
					.append(uientercode).append(IEMT_SPERATE_TEMP).append(downloadtype);

			sharedPreferences.putString(key, recordStringBuffer.toString());
			sharedPreferences.commit();
		}
	}

	/**
	 * 统计软件安装数据
	 * 
	 * @param context
	 * @param packageName
	 */
	private void saveSetup(Context context, String packageName) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPDOWNLOAD_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 为KEY
			String key = packageName;
			String record = sharedPreferences.getString(key, null);
			if (record != null && !"".equals(record.trim())) {
				String[] items = record.split(IEMT_SPERATE_TEMP); // 数组对应saveAppDownloadData保存格式
				if (items != null) {
					long oldTime = Long.parseLong(items[0]);
					long newTime = System.currentTimeMillis();
					long minutes = (newTime - oldTime) / (1000 * 60);
					// 临时记录从点击到安装完毕在30分钟内有效
					if (minutes <= 30) {
						try {
							if (items[4].equals("1")) { // downloadtype =
														// 1：更新安装
								increaseRecordOneItemValue(context, packageName, items[1],
										items[2], items[3], ITEM_INDEX_UPDATE_SETUP, 1, false);
							}
							// else if(items[4].equals("0")){
							// increaseRecordOneItemValue(context,packageName,items[1],items[2],items[3],ITEM_INDEX_DOWNLOAD_SETUP,1,true);
							// }
						} catch (ArrayIndexOutOfBoundsException aex) {
							aex.printStackTrace();
						}
					}
					// 处理后把记录删除
					sharedPreferences.remove(key);
					sharedPreferences.commit();
				}
			}
		}
	}

	/**
	 * 记录正在下载的应用(应用下载完成后，在此匹配，如存在，则是在应用更新中下载)(临时数据) 格式：Key:包名 Value:应用ID
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 */
	private void saveAppDownloadData(Context context, String packageName, String appid) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPDOWNLOADING_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {

			// 以包名 为KEY
			String key = packageName;
			StringBuffer recordStringBuffer = new StringBuffer();
			recordStringBuffer.append(appid);

			sharedPreferences.putString(key, recordStringBuffer.toString());
			sharedPreferences.commit();
		}
	}

	/**
	 * 取正在下载的应用ID
	 * 
	 * @param context
	 * @param packageName
	 */
	public String getDownloadAppID(Context context, String packageName) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPDOWNLOADING_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 为KEY
			String key = packageName;
			String record = sharedPreferences.getString(key, null);
			if (record != null && !"".equals(record.trim())) {
				return record;
			}
		}
		return "";
	}

	/**
	 * 统计下载/更新下载量
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 * @param times
	 * @param downloadtype
	 */
	private void saveDownloadFinish(Context context, String packageName, String appid, int times,
			int downloadtype) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPDOWNLOADING_DATA, Context.MODE_PRIVATE);
		StatisticsData.countStatData(context, StatisticsData.KEY_DOWNLOAD_COMPLETE_APPCENTER);
		if (sharedPreferences != null) {
			// 以包名 为KEY
			String key = packageName;
			String record = sharedPreferences.getString(key, null);
			if (record != null && !"".equals(record.trim())) {
				if (times == 0) {
					increaseRecordOneItemValue(context, packageName, appid, "", "",
							ITEM_INDEX_DOWNLOADCANCEL, 1, false);
				} else {
					increaseRecordOneItemValue(context, packageName, appid, "", "",
							ITEM_INDEX_UPDATE_COMPLETE, 1, false);

					long time = System.currentTimeMillis();
					saveAppDownloadData(context, packageName, appid, time, downloadtype);
				}
				// 处理后把记录删除
				sharedPreferences.remove(key);
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 准备安装
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @param appid
	 *            APP ID
	 * @param downloadtype
	 *            下载类型 0：下载安装；1：更新安装
	 * @param categoryID
	 *            分类ID
	 */
	public void saveReadyToInstall(Context context, String packageName, String appid,
			int downloadtype, String categoryID) {
		long time = System.currentTimeMillis();
		saveAppDownloadData(context, packageName, appid, time, downloadtype);
	}

	/**
	 * 把下发次数 和 推荐位置 保存到(APPMANAGEMENT_STATISTICS_ISSUED_DATA)缓存表中 key: 包名 |入口
	 * vlaue: 下发次数;推荐位置
	 * 
	 * @param context
	 * @param packageName
	 */
	private void saveAppIssuedData(Context context, String packageName, int index) {
		// 应用安装后，会向服务器请求数据，从而下发一次，不符合统计逻辑
		// UI入口为2，则不统计下发次数
		// if(AppManagementStatisticsUtil.getmCurrentUIEnterCode(context) ==
		// UIENTRY_TYPE_DETAIL)
		// return;

		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			int entercode = AppManagementStatisticsUtil.getmCurrentEnterCode(context);

			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode;
			StringBuffer recordStringBuffer = new StringBuffer();
			String value = sharedPreferences.getString(key, "");
			// 有记录
			// 取出下发次数+1
			if (!value.equals("")) {
				String[] items = value.split(IEMT_SPERATE_TEMP);
				int sendtimes = Integer.valueOf(items[0]) + 1;

				recordStringBuffer.append(sendtimes).append(IEMT_SPERATE_TEMP).append(index);
			} else {
				recordStringBuffer.append(1) // 下发1次
						.append(IEMT_SPERATE_TEMP).append(index);
			}

			sharedPreferences.putString(key, recordStringBuffer.toString());
			sharedPreferences.commit();
		}
	}

	/**
	 * 在(APPMANAGEMENT_STATISTICS_ISSUED_DATA)缓存表中，取应用下发次数
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	private static int getAppIssuedData(Context context, String packageName, String entercode) {
		int times = 1; // 默认下发1次
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode;
			String value = sharedPreferences.getString(key, "");
			if (!value.equals("")) {
				try {
					String[] items = value.split(IEMT_SPERATE_TEMP);
					times = Integer.valueOf(items[0]); // 对应
														// saveAppIssuedData()
														// 保存value的结构
				} catch (Exception e) {
					sharedPreferences.remove(key);
					sharedPreferences.commit();
				}
			}
		}
		return times;
	}

	/**
	 * 在(APPMANAGEMENT_STATISTICS_ISSUED_DATA)缓存表中，取应用推荐位置
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	private static int getAppSendIndex(Context context, String packageName, String entercode) {
		int index = 1; // 默认下发1次
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode;
			String value = sharedPreferences.getString(key, "");
			if (!value.equals("")) {
				try {
					String[] items = value.split(IEMT_SPERATE_TEMP);
					index = Integer.valueOf(items[1]); // 对应
														// saveAppIssuedData()
														// 保存value的结构
				} catch (Exception e) {
					sharedPreferences.remove(key);
					sharedPreferences.commit();
				}
			}
		}
		return index;
	}

	/**
	 * 把详细点击次数保存到(APPMANAGEMENT_STATISTICS_DETAIL_CLICKED_DATA)缓存表中
	 * 
	 * @param context
	 * @param packageName
	 */
	private void saveAppDetailClickedData(Context context, String packageName) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_DETAIL_CLICKED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			int entercode = AppManagementStatisticsUtil.getmCurrentEnterCode(context);

			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode;
			try {
				int record = sharedPreferences.getInt(key, 0) + 1;
				sharedPreferences.putInt(key, record);
				sharedPreferences.commit();
			} catch (Exception e) {
				// 出错则记作点击一次详细
				sharedPreferences.putInt(key, 1);
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 在(APPMANAGEMENT_STATISTICS_DETAIL_CLICKED_DATA)缓存表中，取应用详情点击次数
	 * 
	 * @param context
	 * @param packageName
	 * @param entercode
	 * @return
	 */
	private static int getAppDetailClickedData(Context context, String packageName, String entercode) {
		int times = 0;
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_DETAIL_CLICKED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode;
			times = sharedPreferences.getInt(key, 0);
		}
		return times;
	}

	/**
	 * 对记录中某个数据的值进行增加的操作
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 * @param entercode
	 *            指定入口
	 * @param uientercode
	 *            界面入口
	 * @param index
	 * @param value
	 *            递增的值
	 * @param isReturnNorecord
	 *            找不到对应记录，则不操作
	 */
	private static synchronized void increaseRecordOneItemValue(Context context,
			String packageName, String appid, String entercode, String uientercode, int index,
			int value, boolean isReturnNorecord) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {

			if (entercode.equals("")) {
				entercode = String.valueOf(AppManagementStatisticsUtil
						.getmCurrentEnterCode(context));
			}
			if (uientercode.equals("")) {
				uientercode = String.valueOf(AppManagementStatisticsUtil
						.getmCurrentUIEnterCode(context));
			}

			// 以包名+应用ID+当前入口+界面入口 为KEY
			String key = packageName + KEY_SPERATE + appid + KEY_SPERATE + entercode + KEY_SPERATE
					+ uientercode;
			StringBuffer recordStringBuffer = new StringBuffer();
			String record = sharedPreferences.getString(key, null);
			if (index >= ITEM_COUNT) {
				return;
			}

			if (record != null && !"".equals(record.trim())) {
				record = record.replace(IEMT_SPERATE, IEMT_SPERATE_TEMP);
				String[] items = record.split(IEMT_SPERATE_TEMP);
				if (items != null && items.length == ITEM_COUNT) {
					items[index] = String.valueOf(Integer.parseInt(items[index]) + value);
					// 在应用详细中下载更新应用，不统计 下发次数、详细点击数
					// if(!uientercode.equals(String.valueOf(UIENTRY_TYPE_DETAIL))){
					items[ITEM_INDEX_ISSUED] = String.valueOf(getAppIssuedData(context,
							packageName, entercode)); // 同时更新
														// 下发次数
					items[ITEM_INDEX_DETAIL] = String.valueOf(getAppDetailClickedData(context,
							packageName, entercode)); // 同时更新
														// 详细点击数
					// }
					items[ITEM_INDEX_RECOMMEND_INDEX] = String.valueOf(getAppSendIndex(context,
							packageName, entercode)); // 同时更新
														// 推荐位置

					for (int i = 0; i < items.length; i++) {
						recordStringBuffer.append(items[i]).append(IEMT_SPERATE);
					}
					// 删除最尾的“||”
					recordStringBuffer.delete(recordStringBuffer.lastIndexOf(IEMT_SPERATE),
							recordStringBuffer.length());
				}
			} else {

				if (isReturnNorecord) {
					return;
				}

				for (int i = 0; i < ITEM_COUNT; i++) {
					if (i == ITEM_INDEX_STATISTICS_MARK) { // 业务标示
						recordStringBuffer.append(STATISTICS_MARK).append(IEMT_SPERATE);
					} else if (i == ITEM_INDEX_APPID) { // 软件id
						recordStringBuffer.append(appid).append(IEMT_SPERATE);
					} else if (i == ITEM_INDEX_ISSUED) { // 下发次数
						// 在应用详细中下载更新应用，不统计 下发次数、详细点击数
						// if(!uientercode.equals(String.valueOf(UIENTRY_TYPE_DETAIL))){
						recordStringBuffer
								.append(getAppIssuedData(context, packageName, entercode)).append(
										IEMT_SPERATE);
						// }else{
						// recordStringBuffer.append(0).
						// append(IEMT_SPERATE);
						// }
					} else if (i == ITEM_INDEX_DETAIL) { // 详细点击数
						// 在应用详细中下载更新应用，不统计 下发次数、详细点击数
						// if(!uientercode.equals(String.valueOf(UIENTRY_TYPE_DETAIL))){
						recordStringBuffer.append(
								getAppDetailClickedData(context, packageName, entercode)).append(
								IEMT_SPERATE);
						// }else{
						// recordStringBuffer.append(0).
						// append(IEMT_SPERATE);
						// }
					} else if (i == ITEM_INDEX_RECOMMEND_INDEX) { // 推荐位置
						recordStringBuffer.append(getAppSendIndex(context, packageName, entercode))
								.append(IEMT_SPERATE);
					} else if (i == index) { // 要修改的数据
						recordStringBuffer.append(value).append(IEMT_SPERATE);
					} else if (i == ITEM_INDEX_ENTER_CODE) { // 入口
						recordStringBuffer.append(entercode).append(IEMT_SPERATE);
					} else if (i == ITEM_INDEX_RECOMMEND_INTERFACE) { // 界面入口
						recordStringBuffer.append(uientercode).append(IEMT_SPERATE);
					} else if (i == ITEM_INDEX_VERSION) { // 版本协议
						recordStringBuffer.append(NEW_VERSION).append(IEMT_SPERATE);
					} else if (i == ITEM_INDEX_UID) { // 渠道号
						recordStringBuffer.append(Statistics.getUid(context)).append(IEMT_SPERATE);
					} else { // 其它
						recordStringBuffer.append(0).append(IEMT_SPERATE);
					}
				}
				// 删除最尾的“||”
				recordStringBuffer.delete(recordStringBuffer.lastIndexOf(IEMT_SPERATE),
						recordStringBuffer.length());

			}
			
			sharedPreferences.putString(key, recordStringBuffer.toString());
			sharedPreferences.commit();
		}
	}

	/**
	 * 清空记录
	 * 
	 * @param context
	 */
	public static void delRecords(Context context) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.clear();
			sharedPreferences.commit();
		}
		sharedPreferences = new PreferencesManager(context, APPMANAGEMENT_STATISTICS_TAB_CLICK,
				Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.clear();
			sharedPreferences.commit();
		}
		// delIssuedData(context);
		clearIssuedTimes(context);
		delDetailClickedData(context);
	}

	/**
	 * 清下发数据记录表
	 * 
	 * @param context
	 */
	private static void delIssuedData(Context context) {
		PreferencesManager sharedPreferences1 = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences1 != null) {
			sharedPreferences1.clear();
			sharedPreferences1.commit();
		}
	}

	/**
	 * 清详细点击记录表
	 * 
	 * @param context
	 */
	private static void delDetailClickedData(Context context) {
		PreferencesManager sharedPreferences1 = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_DETAIL_CLICKED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences1 != null) {
			sharedPreferences1.clear();
			sharedPreferences1.commit();
		}
	}

	/**
	 * 清入口记录表
	 * 
	 * @param context
	 */
	private void delEnterData(Context context) {
		PreferencesManager sharedPreferences1 = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPMANAGEMENT_ENTER_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences1 != null) {
			sharedPreferences1.clear();
			sharedPreferences1.commit();
		}
	}

	/**
	 * 清界面入口记录表
	 * 
	 * @param context
	 */
	private void delUIEnterData(Context context) {
		PreferencesManager sharedPreferences1 = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPMANAGEMENT_UIENTER_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences1 != null) {
			sharedPreferences1.clear();
			sharedPreferences1.commit();
		}
	}

	/**
	 * 清下载点击记录表
	 * 
	 * @param context
	 */
	private void delDownloadData(Context context) {
		PreferencesManager sharedPreferences1 = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPDOWNLOAD_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences1 != null) {
			sharedPreferences1.clear();
			sharedPreferences1.commit();
		}
	}

	/**
	 * 清下载中记录表
	 * 
	 * @param context
	 */
	private void delDownloadingData(Context context) {
		PreferencesManager sharedPreferences1 = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_APPDOWNLOADING_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences1 != null) {
			sharedPreferences1.clear();
			sharedPreferences1.commit();
		}
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static Map<String, ?> getAllDate(Context context) {
		PreferencesManager sp = new PreferencesManager(context, APPMANAGEMENT_STATISTICS_DATA,
				Context.MODE_PRIVATE);
		return sp.getAll();
	}

	/**
	 * 保存Tab点击数的方法
	 * 
	 * @author zhouxuewen
	 * @param context
	 * @param tabId
	 *            Tab的ID
	 * @param entercode
	 *            指定入口ID，如无指定时传入NULL,会使用公共入口ID
	 */
	public static void saveTabClickData(Context context, int tabId, String entercode) {

		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_TAB_CLICK, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {

			if (entercode == null || entercode.equals("")) {
				entercode = String.valueOf(AppManagementStatisticsUtil
						.getmCurrentEnterCode(context));
			}

			// 以TabID+入口 为KEY
			String key = tabId + ITEM_KEY_SPERATE + entercode;
			int record = sharedPreferences.getInt(key, 0);

			record++;

			sharedPreferences.putInt(key, record);
			sharedPreferences.commit();
		}

	}

	/**
	 * 获取Tab点击的数据
	 * 
	 * @author zhouxuewen
	 */
	public static Map<String, ?> getTabClickDate(Context context) {
		PreferencesManager sp = new PreferencesManager(context, APPMANAGEMENT_STATISTICS_TAB_CLICK,
				Context.MODE_PRIVATE);
		return sp.getAll();
	}

	/**
	 * 取得应用管理统计数据
	 * 
	 * @return
	 */
	// private static String getAppManagementStatisticsDate(Context context) {
	// String statisticsData = null;
	// if (context != null) {
	// StringBuffer statisticsDataBuffer = new StringBuffer();
	// Map<String, ?> date = AppManagementStatisticsUtil.getAllDate(context);
	// Set<String> keys = date.keySet();
	// for (String key : keys) {
	// String reason = (String) date.get(key);
	// statisticsDataBuffer.append(reason).append(
	// "\r\n");
	// }
	// statisticsData = statisticsDataBuffer.toString();
	// }
	// return statisticsData;
	// }

	/**
	 * 把下发次数还原为 0
	 */
	private static void clearIssuedTimes(Context context) {
		PreferencesManager sp = new PreferencesManager(context,
				APPMANAGEMENT_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		if (sp != null) {

			Map<String, ?> date = sp.getAll();
			if (date != null) {
				Set<String> keys = date.keySet();
				if (keys != null) {
					for (String key : keys) {
						String value = (String) date.get(key);
						String[] items = value.split(IEMT_SPERATE_TEMP);
						if (items.length > 1) {
							StringBuffer recordStringBuffer = new StringBuffer();
							recordStringBuffer.append(0);
							for (int i = 1; i < items.length; i++) { // (i = 1
																		// 开始是因为:下发次数是保存在items[0]中)
								recordStringBuffer.append(IEMT_SPERATE_TEMP).append(items[i]);
							}
							sp.putString(key, recordStringBuffer.toString());
							sp.commit();
						}
					}
				}
			}
		}
	}
}