package com.jiubang.ggheart.data.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 应用推荐统计类
 * 
 * @author zhaojunjie
 * 
 */

// * 临时表结构描述：
// *
// * 下发数据时 记录：
// * APPMANAGEMENT_STATISTICS_ISSUED_DATA
// * Vlaue: 下发次数;推荐位置;推荐ID
// *
// * 点击下载时 记录：
// * APPRECOMMENDED_STATISTICS_APPDOWNLOADING_DATA
// * Value:推荐分类ID;下载类型(0：下载安装；1：更新安装);应用ID
// *
// * 下载完成时 记录：
// * APPRECOMMENDED_STATISTICS_APPDOWNLOAD_DATA
// * Value:下载时间;应用ID;入口;界面入口;下载类型;推荐分类ID
// *

public class AppRecommendedStatisticsUtil implements IStatistics {

	// 常量
	// 应用推荐的统计数据sharedpreference文件(所有要提交的数据)
	private final static String APPRECOMMENDED_STATISTICS_DATA = "apprecommended_statistics_data";
	// 应用推荐的下发次数数据sharedpreference文件(因为下发次数只与应用包名和入口关联，与其它条件无关)
	private final static String APPRECOMMENDED_STATISTICS_ISSUED_DATA = "apprecommended_statistics_issued_data";
	// 应用推荐的详细点击数据sharedpreference文件(因为详细点击只与应用包名和入口关联，与其它条件无关)
	private final static String APPRECOMMENDED_STATISTICS_DETAIL_CLICKED_DATA = "apprecommended_statistics_detail_clicked_data";
	// 应用推荐的入口sharedpreference文件(由于入口会在不同进程中改变，所以要保存于文件中)(入口缓存 与 应用更新使用同一文件)
	private final static String APPMANAGEMENT_STATISTICS_APPMANAGEMENT_ENTER_DATA = "appmanagement_statistics_appmanagement_enter_data";
	// 应用推荐的界面入口sharedpreference文件(由于界面入口数据会在不同进程中应用，所以要保存于文件中)(界面入口缓存 与
	// 应用更新使用同一文件)
	private final static String APPRECOMMENDED_STATISTICS_APPMANAGEMENT_UIENTER_DATA = "appmanagement_statistics_appmanagement_uienter_data"; // "apprecommended_statistics_appmanagement_uienter_data";
	// 记录下载完成的应用sharedpreference文件(由于判断所安装的应用是否是从应用管理中下载)(下载临时数据)
	private final static String APPRECOMMENDED_STATISTICS_APPDOWNLOAD_DATA = "apprecommended_statistics_appdownload_data";
	// 记录正在下载的应用sharedpreference文件(由于判断所下载的应用是否是从应用管理中下载)(下载临时数据)
	private final static String APPRECOMMENDED_STATISTICS_APPDOWNLOADING_DATA = "apprecommended_statistics_appdownloading_data";

	private final static String KEY_ENTER = "statistics_appmanagement_enter"; // 入口KEY
																				// (与
																				// 应用更新使用同一文件)
	private final static String KEY_UIENTER = "statistics_apprecommended_uienter"; // 界面入口KEY
	private final static String KEY_SPERATE = "|"; // 统计数据中KEY的分隔符
	private final static String IEMT_SPERATE = "||"; // 统计数据中统计项的分隔符
	private final static String IEMT_SPERATE_TEMP = ";"; // 由于使用“||”不能分成字符串数组，要先替换为“;”

	private final static int ITEM_COUNT = 19; // 应用管理统计项总数
	private final static int STATISTICS_MARK = 11; // 业务标识

	private final static int ITEM_INDEX_STATISTICS_MARK = 0; // 业务标识 数在记录中的位置
	private final static int ITEM_INDEX_APPID = 1; // 软件id在记录中的位置
	private final static int ITEM_INDEX_ISSUED = 2; // 下发次数 在记录中的位置
	private final static int ITEM_INDEX_DETAIL = 3; // 详情点击次数 在记录中的位置
	private final static int ITEM_INDEX_DOWNLOAD_CLICK = 4; // 下载点击 在记录中的位置
	private final static int ITEM_INDEX_DOWNLOAD_COMPLETE = 5; // 下载量 在记录中的位置
	private final static int ITEM_INDEX_DOWNLOAD_SETUP = 6; // 下载安装 在记录中的位置
	private final static int ITEM_INDEX_UPDATE_CLICK = 7; // 更新点击 在记录中的位置
	private final static int ITEM_INDEX_UPDATE_COMPLETE = 8; // 更新下载量 在记录中的位置
	private final static int ITEM_INDEX_UPDATE_SETUP = 9; // 更新安装 在记录中的位置
	private final static int ITEM_INDEX_RECOMMEND_CATEGORY_ID = 10; // 推荐分类ID
																	// 在记录中的位置
	private final static int ITEM_INDEX_RECOMMEND_INDEX = 11; // 推荐位置 在记录中的位置
	private final static int ITEM_INDEX_ENTER_CODE = 12; // 入口标识记录中的位置
	private final static int ITEM_INDEX_RECOMMEND_INTERFACE = 13; // 界面入口　
																	// 在记录中的位置
	private final static int ITEM_INDEX_RECOMMEND_VERSION = 14; // 版本协议的位置
	private final static int ITEM_INDEX_UID = 15; // 渠道号的位置
	private final static int ITEM_INDEX_DOWNLOADCANCEL = 16; // 下载取消记录位置
	private final static int ITEM_INDEX_CLICKTIME = 17; // 点击时间记录位置
	private final static int ITEM_INDEX_PKG_TYPE = 18; // 包类型记录位置

	public final static int NEW_VERSION_ID = 4; // 新统计版本ID

	public final static byte ENTRY_TYPE_FUNTAB_ICON = 1; // 功能表菜单入口
	public final static byte ENTRY_TYPE_NOTICE = 2; // 通知栏入口(每8小时显示)
	public final static byte ENTRY_TYPE_GOSTORE = 3; // GoStore入口
	public final static byte ENTRY_TYPE_GOSTORE_ICON = 4; // 功能表--Gostore图标右上角数字
	public final static byte ENTRY_TYPE_APP_ICON = 5; // 功能表--应用图标右上角更新标志(每8小时显示)
	public final static byte ENTRY_TYPE_DESK = 6; // 桌面图标进入
	public final static byte ENTRY_TYPE_APPFUNC = 7; // 功能表图标进入
	public final static byte ENTRY_TYPE_MENU = 8; // 桌面菜单进入
	public final static byte ENTRY_TYPE_WIDGET = 9; // WIDGET进入
	public final static byte ENTRY_TYPE_APPFUNC_UPDATE = 10; // 功能表更新图标
	public final static byte ENTRY_TYPE_APPFUNC_SEARCH = 11; // 功能表搜索进入
	public final static byte ENTRY_TYPE_GOWIDGET_SEARCH = 12; // GO搜索WIDGET进入
	public final static byte ENTRY_TYPE_THEMEMANAGE = 13; // 主题预览界面
	public final static byte ENTRY_TYPE_UPDATA_GUIDE = 14; // "更新提示页”主题推荐入口
	public final static byte ENTRY_TYPE_WALLPAPER = 15; // "壁纸”添加界面
	public final static byte ENTRY_TYPE_MEUN_GOSTORE = 16; // 精品桌面菜单进入
	public final static byte ENTRY_TYPE_APPFUNC_ICO_GOSTORE = 17; // 功能表——精品图标进入
	public final static byte ENTRY_TYPE_MESSAGE_CENTER = 18; // 功能表——精品图标进入
	public final static byte ENTRY_TYPE_LAUNCHER_CENTER = 19; // 桌面进入应用中心详情
	public final static byte ENTRY_TYPE_OTHER = 0; // 其它入口

	public final static byte UIENTRY_TYPE_NONE = 0; // 界面入口--没有
	public final static byte UIENTRY_TYPE_LIST = 1; // 界面入口--列表
	public final static byte UIENTRY_TYPE_DETAIL = 1; // 界面入口--详细

	// 初始化单例
	private static AppRecommendedStatisticsUtil sInstance;

	public static synchronized AppRecommendedStatisticsUtil getInstance() {

		if (sInstance == null) {
			sInstance = new AppRecommendedStatisticsUtil();
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
				APPRECOMMENDED_STATISTICS_APPMANAGEMENT_UIENTER_DATA, Context.MODE_PRIVATE);
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
				APPRECOMMENDED_STATISTICS_APPMANAGEMENT_UIENTER_DATA, Context.MODE_PRIVATE);
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

	}

	/**
	 * 记录下发次数、推荐ID 和 推荐位置
	 * 
	 * @param context
	 * @param packageName
	 * @param typeid
	 *            推荐分类ID
	 * @param index
	 *            分类中的位置
	 */
	public void saveIssued(Context context, String packageName, String categoryid, int index) {
		saveAppIssuedData(context, packageName, categoryid, index);
	}

	@Override
	public void saveDetailsClick(Context context, String packageName, int appid, int times) {
		saveAppDetailClickedData(context, packageName);
	}

	/**
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 * @param categoryID
	 */
	public void saveDetailsClick2MainTable(Context context, String packageName, int appid,
			String categoryID) {
		increaseRecordOneItemValue(context, packageName, String.valueOf(appid), "", "", categoryID,
				ITEM_INDEX_DOWNLOAD_CLICK, 0, false, "");
	}

	@Override
	public void saveDownloadClick(Context context, String packageName, int appid, int times) {
		saveDownloadClick(context, packageName, appid, "0", 1);
	}

	/**
	 * 记录下载点击
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 * @param categoryID
	 *            推荐分类ID
	 * @param times
	 */
	public void saveDownloadClick(Context context, String packageName, int appid,
			String categoryID, int times) {
		// if(times == 0) //不能要这句
		saveAppDownloadData(context, packageName, categoryID, 0, String.valueOf(appid));
		increaseRecordOneItemValue(context, packageName, String.valueOf(appid), "", "", categoryID,
				ITEM_INDEX_DOWNLOAD_CLICK, times, false, "");
	}

	/**
	 * 记录下载点击（带点击时间）
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 * @param categoryID
	 *            推荐分类ID
	 * @param times
	 * @param clickTime
	 *            点击时间
	 */
	public void saveDownloadClick(Context context, String packageName, int appid,
			String categoryID, int times, String clickTime) {
		// if(times == 0) //不能要这句
		saveAppDownloadData(context, packageName, categoryID, 0, String.valueOf(appid));
		increaseRecordOneItemValue(context, packageName, String.valueOf(appid), "", "", categoryID,
				ITEM_INDEX_DOWNLOAD_CLICK, times, false, clickTime);
	}
	
	public void saveDownloadClick(Context context, String packageName, int appid,
			String categoryID, int times, int pkgType) {
		// if(times == 0) //不能要这句
		saveAppDownloadData(context, packageName, categoryID, 0, String.valueOf(appid));
		increaseRecordOneItemValue(context, packageName, String.valueOf(appid), "", "", categoryID,
				ITEM_INDEX_DOWNLOAD_CLICK, times, false, "", pkgType);
	}

	@Override
	public void saveDownloadComplete(Context context, String packageName, String appid, int times) {
		saveDownloadFinish(context, packageName, appid, times);
	}

	@Override
	public void saveDownloadSetup(Context context, String packageName) {
		saveSetup(context, packageName);
	}

	@Override
	public void saveUpdataClick(Context context, String packageName, int appid, int times) {
	}

	/**
	 * 记录更新点击
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 * @param categoryID
	 *            推荐分类ID
	 * @param times
	 */
	public void saveUpdataClick(Context context, String packageName, int appid, String categoryID,
			int times) {
		// if(times == 0) //不能要这句
		saveAppDownloadData(context, packageName, categoryID, 1, String.valueOf(appid));
		increaseRecordOneItemValue(context, packageName, String.valueOf(appid), "", "", categoryID,
				ITEM_INDEX_UPDATE_CLICK, times, false, "");
	}

	@Override
	public void saveUpdataComplete(Context context, String packageName, String appid, int times) {

	}

	@Override
	public void saveUpdataSetup(Context context, String packageName) {

	}

	/**
	 * 记录应用下载完成时的数据(临时数据) 格式：Key:包名 Value:下载时间;应用ID;入口;界面入口;下载类型;推荐分类ID
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 * @param time
	 * @param downloadtype
	 *            0：下载安装；1：更新安装
	 */
	private void saveAppDownloadData(Context context, String packageName, String appid, long time,
			int downloadtype, String categoryid) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_APPDOWNLOAD_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			int entercode = AppRecommendedStatisticsUtil.getmCurrentEnterCode(context);
			int uientercode = AppRecommendedStatisticsUtil.getmCurrentUIEnterCode(context);

			// 以包名 为KEY
			String key = packageName;
			StringBuffer recordStringBuffer = new StringBuffer();
			recordStringBuffer.append(time).append(IEMT_SPERATE_TEMP).append(appid)
					.append(IEMT_SPERATE_TEMP).append(entercode).append(IEMT_SPERATE_TEMP)
					.append(uientercode).append(IEMT_SPERATE_TEMP).append(downloadtype)
					.append(IEMT_SPERATE_TEMP).append(categoryid);

			sharedPreferences.putString(key, recordStringBuffer.toString());
			sharedPreferences.commit();
		}
	}

	/**
	 * <br>
	 * 功能简述: 清除安装临时表的指定包名数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param context
	 * @param pkgName
	 */
	private void removeAppDownloadOldData(Context context, String pkgName) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_APPDOWNLOAD_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.remove(pkgName);
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
				APPRECOMMENDED_STATISTICS_APPDOWNLOAD_DATA, Context.MODE_PRIVATE);
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
						// String times =
						// getDownloadUpdataClickedTimes(context,packageName,items[1]);
						// String[] timesitems =
						// times.split(IEMT_SPERATE_TEMP);

						try {
							if (items[4].equals("1")) { // &&
														// !timesitems[0].equals("0")){
														// //downloadtype =
														// 1：更新安装 且 更新点击数不为0
								increaseRecordOneItemValue(context, packageName, items[1],
										items[2], items[3],
										getAppSendCategoryID(context, packageName),
										ITEM_INDEX_UPDATE_SETUP, 1, false, "");
							} else if (items[4].equals("0")) { // &&
																// !timesitems[1].equals("0")){
																// //downloadtype
																// = 0：下载安装
																// 且
																// 下载点击数不为0
								increaseRecordOneItemValue(context, packageName, items[1],
										items[2], items[3],
										getAppSendCategoryID(context, packageName),
										ITEM_INDEX_DOWNLOAD_SETUP, 1, false, "");
							}
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
	 * 
	 * @param packageName
	 * @param appid
	 * @return 更新点击次数；下载点击次数
	 */
	// private static String getDownloadUpdataClickedTimes(Context context
	// ,String packageName , String appid){
	// String times = "0"+IEMT_SPERATE_TEMP+"0" ;
	// SharedPreferences sharedPreferences =
	// context.getSharedPreferences(APPRECOMMENDED_STATISTICS_DATA,
	// Activity.MODE_PRIVATE);
	// String entercode
	// =String.valueOf(AppRecommendedStatisticsUtil.getmCurrentEnterCode(context));
	//
	// String key = packageName + KEY_SPERATE + appid + KEY_SPERATE + entercode
	// + KEY_SPERATE + "1";
	// String record = sharedPreferences.getString(key, null);
	// if(record != null && !"".equals(record.trim())){
	// record = record.replace(IEMT_SPERATE, IEMT_SPERATE_TEMP);
	// String[] items = record.split(IEMT_SPERATE_TEMP);
	// if(items != null && items.length == ITEM_COUNT){
	// times = items[ITEM_INDEX_UPDATE_CLICK] + IEMT_SPERATE_TEMP +
	// items[ITEM_INDEX_DOWNLOAD_CLICK];
	// }
	// }
	//
	//
	// return times;
	// }

	/**
	 * 记录正在下载的应用(应用下载完成后，在此匹配，如存在，则是在应用更新中下载)(临时数据) 格式：Key:包名
	 * Value:推荐分类ID;下载类型(0：下载安装；1：更新安装);应用ID
	 * 
	 * @param context
	 * @param packageName
	 * @param appid
	 */
	private void saveAppDownloadData(Context context, String packageName, String categoryID,
			int downloadtype, String appid) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_APPDOWNLOADING_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {

			// 以包名 为KEY
			String key = packageName;
			StringBuffer recordStringBuffer = new StringBuffer();
			recordStringBuffer.append(categoryID).append(IEMT_SPERATE_TEMP).append(downloadtype)
					.append(IEMT_SPERATE_TEMP).append(appid);

			sharedPreferences.putString(key, recordStringBuffer.toString());
			sharedPreferences.commit();
		}
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
	private void saveDownloadFinish(Context context, String packageName, String appid, int times) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_APPDOWNLOADING_DATA, Context.MODE_PRIVATE);
		StatisticsData.countStatData(context, StatisticsData.KEY_DOWNLOAD_COMPLETE_APPCENTER);
		if (sharedPreferences != null) {
			// 以包名 为KEY
			String key = packageName;
			String value = sharedPreferences.getString(key, "");
			if (!value.equals("")) {
				String[] items = value.split(IEMT_SPERATE_TEMP);
				String categoryID = items[0]; // 对应 saveAppDownloadData()
												// 保存value的结构
				int downloadtype = Integer.valueOf(items[1]);

				if (downloadtype != -1) {
					if (times == 0) {
						increaseRecordOneItemValue(context, packageName, appid, "", "", categoryID,
								ITEM_INDEX_DOWNLOADCANCEL, 1, false, "");
					} else {
						if (downloadtype == 0) { // 下载安装
							increaseRecordOneItemValue(context, packageName, appid, "", "",
									categoryID, ITEM_INDEX_DOWNLOAD_COMPLETE, 1, false, "");
						} else if (downloadtype == 1) { // 更新安装
							increaseRecordOneItemValue(context, packageName, appid, "", "",
									categoryID, ITEM_INDEX_UPDATE_COMPLETE, 1, false, "");
						}
						long time = System.currentTimeMillis();
						saveAppDownloadData(context, packageName, appid, time, downloadtype,
								categoryID);
					}
					// 处理后把记录删除
					sharedPreferences.remove(key);
					sharedPreferences.commit();
				}
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
		// 之清了此包之前点击下载时的预安装临时表
		// removeAppDownloadOldData(context, packageName);
		saveAppDownloadData(context, packageName, appid, time, downloadtype, categoryID);
	}

	/**
	 * 在(APPRECOMMENDED_STATISTICS_APPDOWNLOADING_DATA)缓存表中，取应用推荐分类ID
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	// private static String getAppSendCategoryID(Context context, String
	// packageName) {
	// String categoryid = ""; //推荐分类ID
	// SharedPreferences sharedPreferences =
	// context.getSharedPreferences(APPRECOMMENDED_STATISTICS_APPDOWNLOADING_DATA,
	// Activity.MODE_PRIVATE);
	// if(sharedPreferences != null){
	// //以包名 + 入口 为KEY
	// String key = packageName;
	// SharedPreferences.Editor editor = sharedPreferences.edit();
	// if(editor != null){
	// String value = sharedPreferences.getString(key, "");
	// if(!value.equals("")) {
	// String[] items = value.split(IEMT_SPERATE_TEMP);
	// categoryid = items[0]; //对应 saveAppDownloadData() 保存value的结构
	// }
	// }
	// }
	// return categoryid;
	// }

	/**
	 * 取正在下载的应用ID
	 * 
	 * @param context
	 * @param packageName
	 */
	public String getDownloadAppID(Context context, String packageName) {
		String appid = ""; // 应用ID
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_APPDOWNLOADING_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 + 入口 为KEY
			String key = packageName;
			String value = sharedPreferences.getString(key, "");
			if (!value.equals("")) {
				String[] items = value.split(IEMT_SPERATE_TEMP);
				appid = items[2]; // 对应 saveAppDownloadData() 保存value的结构
			}
		}
		return appid;
	}

	/**
	 * 把下发次数 和 推荐位置 保存到(APPMANAGEMENT_STATISTICS_ISSUED_DATA)缓存表中 key: 包名
	 * |入口|推荐ID vlaue: 下发次数;推荐位置;推荐ID
	 * 
	 * 一次把所有的数据commit，避免消耗系统资源
	 * 
	 * @author xiedezhi
	 */
	public void saveAppIssueDataList(Context context, List<String> packageNames,
			List<String> categoryids, List<Integer> indexs) {
		if (context == null || packageNames == null || categoryids == null || indexs == null) {
			return;
		}
		if (packageNames.size() != categoryids.size() || categoryids.size() != indexs.size()) {
			return;
		}
		String empty = "";
		//先把结果放在一个map中，再提交到SharedPreferences，避免多次commit
		Map<String, String> map = new HashMap<String, String>();
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		int entercode = AppRecommendedStatisticsUtil.getmCurrentEnterCode(context);
		for (int i = 0; i < packageNames.size(); i++) {
			String packageName = packageNames.get(i);
			String categoryid = categoryids.get(i);
			int index = indexs.get(i);

			// 以包名 + 入口 为KEY
			String key = (new StringBuffer().append(packageName).append(KEY_SPERATE)
					.append(entercode).append(KEY_SPERATE).append(categoryid)).toString();
			String value = map.get(key);
			if (value == null) {
				value = sharedPreferences.getString(key, empty);
			}
			StringBuffer recordStringBuffer = new StringBuffer();
			// 有记录
			// 取出下发次数+1
			if (value != null && !value.equals(empty)) {
				String[] items = value.split(IEMT_SPERATE_TEMP);
				int sendtimes = Integer.valueOf(items[0]) + 1;
				recordStringBuffer.append(sendtimes).append(IEMT_SPERATE_TEMP).append(index)
						.append(IEMT_SPERATE_TEMP).append(categoryid);
			} else {
				recordStringBuffer.append(1)
						// 下发1次
						.append(IEMT_SPERATE_TEMP).append(index).append(IEMT_SPERATE_TEMP)
						.append(categoryid);
			}
			map.put(key, recordStringBuffer.toString());
		}
		if (map.size() > 0) {
			for (String key : map.keySet()) {
				String value = map.get(key);
				if (!value.equals(empty)) {
					sharedPreferences.putString(key, value);
				}
			}
			sharedPreferences.commit();
		}
	}

	/**
	 * 把下发次数 和 推荐位置 保存到(APPMANAGEMENT_STATISTICS_ISSUED_DATA)缓存表中 key: 包名
	 * |入口|推荐ID vlaue: 下发次数;推荐位置;推荐ID
	 * 
	 * @param context
	 * @param packageName
	 */
	private void saveAppIssuedData(Context context, String packageName, String categoryid, int index) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			int entercode = AppRecommendedStatisticsUtil.getmCurrentEnterCode(context);

			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode + KEY_SPERATE + categoryid;
			StringBuffer recordStringBuffer = new StringBuffer();
			String value = sharedPreferences.getString(key, "");
			// 有记录
			// 取出下发次数+1
			if (!value.equals("")) {
				String[] items = value.split(IEMT_SPERATE_TEMP);
				int sendtimes = Integer.valueOf(items[0]) + 1;

				recordStringBuffer.append(sendtimes).append(IEMT_SPERATE_TEMP).append(index)
						.append(IEMT_SPERATE_TEMP).append(categoryid);
			} else {
				recordStringBuffer.append(1)
						// 下发1次
						.append(IEMT_SPERATE_TEMP).append(index).append(IEMT_SPERATE_TEMP)
						.append(categoryid);
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
	private static int getAppIssuedData(Context context, String packageName, String entercode,
			String categoryid) {
		int times = 1; // 默认下发1次
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode + KEY_SPERATE + categoryid;
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
	private static int getAppSendIndex(Context context, String packageName, String entercode,
			String categoryid) {
		int index = 1; // 默认下发1次
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode + KEY_SPERATE + categoryid;
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
	 * 在(APPMANAGEMENT_STATISTICS_ISSUED_DATA)缓存表中，取应用推荐分类ID
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	// private static String getAppSendCategoryID(Context context, String
	// packageName, String entercode) {
	// String categoryid = "10000"; //推荐分类ID
	// SharedPreferences sharedPreferences =
	// context.getSharedPreferences(APPRECOMMENDED_STATISTICS_ISSUED_DATA,
	// Activity.MODE_PRIVATE);
	// if(sharedPreferences != null){
	// //以包名 + 入口 为KEY
	// String key = packageName + KEY_SPERATE + entercode;
	// SharedPreferences.Editor editor = sharedPreferences.edit();
	// if(editor != null){
	// String value = sharedPreferences.getString(key, "");
	// if(!value.equals("")) {
	// try
	// {
	// String[] items = value.split(IEMT_SPERATE_TEMP);
	// categoryid = items[2]; //对应 saveAppIssuedData() 保存value的结构
	// }
	// catch (Exception e) {
	// editor.remove(key).commit();
	// }
	// }
	// }
	// }
	// return categoryid;
	// }

	/**
	 * 在(APPRECOMMENDED_STATISTICS_APPDOWNLOAD_DATA)缓存表中，取应用推荐分类ID
	 */
	private static String getAppSendCategoryID(Context context, String packageName) {
		String categoryid = "10000"; // 推荐分类ID
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_APPDOWNLOAD_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 + 入口 为KEY
			String key = packageName;
			String value = sharedPreferences.getString(key, "");
			if (!value.equals("")) {
				try {
					String[] items = value.split(IEMT_SPERATE_TEMP);
					categoryid = items[5]; // 对应 saveAppIssuedData()
											// 保存value的结构
				} catch (Exception e) {
					sharedPreferences.remove(key);
					sharedPreferences.commit();
				}
			}
		}
		return categoryid;
	}

	/**
	 * 把详细点击次数保存到(APPMANAGEMENT_STATISTICS_DETAIL_CLICKED_DATA)缓存表中
	 * 
	 * @param context
	 * @param packageName
	 */
	private void saveAppDetailClickedData(Context context, String packageName) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_DETAIL_CLICKED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			int entercode = AppRecommendedStatisticsUtil.getmCurrentEnterCode(context);

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
				APPRECOMMENDED_STATISTICS_DETAIL_CLICKED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			// 以包名 + 入口 为KEY
			String key = packageName + KEY_SPERATE + entercode;
				times = sharedPreferences.getInt(key, 0);
		}
		return times;
	}
	
	private static synchronized void increaseRecordOneItemValue(Context context,
			String packageName, String appid, String entercode, String uientercode,
			String categoryid, int index, int value, boolean isReturnNorecord, String time) {
		increaseRecordOneItemValue(context, packageName, appid, entercode, uientercode, 
				categoryid, index, value, isReturnNorecord, time, 0);
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
	 * @param categoryid
	 *            推荐分类ID
	 * @param index
	 *            值在数据格式里的位置
	 * @param value
	 *            递增的值
	 * @param isReturnNorecord
	 *            找不到对应记录，则不操作
	 */
	private static synchronized void increaseRecordOneItemValue(Context context,
			String packageName, String appid, String entercode, String uientercode,
			String categoryid, int index, int value, boolean isReturnNorecord, String time, int pkgType) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {

			if (entercode.equals("")) {
				entercode = String.valueOf(AppRecommendedStatisticsUtil
						.getmCurrentEnterCode(context));
			}
			if (uientercode.equals("")) {
				uientercode = String.valueOf(AppRecommendedStatisticsUtil
						.getmCurrentUIEnterCode(context));
			}

				// 以包名+应用ID+当前入口+界面入口 为KEY
				String key = packageName + KEY_SPERATE + appid + KEY_SPERATE + entercode
						+ KEY_SPERATE + uientercode;
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
								packageName, entercode, categoryid)); // 同时更新
																		// 下发次数
						items[ITEM_INDEX_DETAIL] = String.valueOf(getAppDetailClickedData(context,
								packageName, entercode)); // 同时更新
															// 详细点击数
						// }
						items[ITEM_INDEX_RECOMMEND_INDEX] = String.valueOf(getAppSendIndex(context,
								packageName, entercode, categoryid)); // 同时更新
																		// 推荐位置
						items[ITEM_INDEX_RECOMMEND_CATEGORY_ID] = categoryid; // getAppSendCategoryID(context,
																				// packageName);
																				// //同时更新
																				// //点击时间统计
																				// //
																				// 推荐分类ID

						if (index == ITEM_INDEX_DOWNLOAD_CLICK) {
							// 如果是下载点击
							
							// 1.点击时间统计
							String setup = items[ITEM_INDEX_DOWNLOAD_SETUP];
							if (time == null || time.equals("")) {
								// 如果传递过来的点击时间为空或者空字符，转成0
								time = "0";
							}
							if (setup.equals("0") && !time.equals("0")) {
								// 安装量为0且点击时间不为0，才记录时间
								items[ITEM_INDEX_CLICKTIME] = time;
							}
							
							//　2.统计包类型
							if (pkgType != 0) {
								//　包类型不为０才要更新
								items[ITEM_INDEX_PKG_TYPE] = String.valueOf(pkgType);
							}
						}
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
							recordStringBuffer.append(
									getAppIssuedData(context, packageName, entercode, categoryid))
									.append(IEMT_SPERATE);
							// }else{
							// recordStringBuffer.append(0).
							// append(IEMT_SPERATE);
							// }
						} else if (i == ITEM_INDEX_DETAIL) { // 详细点击数
							// 在应用详细中下载更新应用，不统计 下发次数、详细点击数
							// if(!uientercode.equals(String.valueOf(UIENTRY_TYPE_DETAIL))){
							recordStringBuffer.append(
									getAppDetailClickedData(context, packageName, entercode))
									.append(IEMT_SPERATE);
							// }else{
							// recordStringBuffer.append(0).
							// append(IEMT_SPERATE);
							// }
						} else if (i == ITEM_INDEX_RECOMMEND_CATEGORY_ID) { // 推荐分类ID
							// recordStringBuffer.append(getAppSendCategoryID(context,
							// packageName)).
							recordStringBuffer.append(categoryid).append(IEMT_SPERATE);
						} else if (i == ITEM_INDEX_RECOMMEND_INDEX) { // 推荐位置
							recordStringBuffer.append(
									getAppSendIndex(context, packageName, entercode, categoryid))
									.append(IEMT_SPERATE);
						} else if (i == index) { // 要修改的数据
							recordStringBuffer.append(value).append(IEMT_SPERATE);
						} else if (i == ITEM_INDEX_ENTER_CODE) { // 入口
							recordStringBuffer.append(entercode).append(IEMT_SPERATE);
						} else if (i == ITEM_INDEX_RECOMMEND_INTERFACE) { // 界面入口
							recordStringBuffer.append(uientercode).append(IEMT_SPERATE);
						} else if (i == ITEM_INDEX_RECOMMEND_VERSION) { // 版本ID
							recordStringBuffer.append(NEW_VERSION_ID).append(IEMT_SPERATE);
						} else if (i == ITEM_INDEX_UID) { // 渠道号
							recordStringBuffer.append(Statistics.getUid(context)).append(
									IEMT_SPERATE);
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

		// delRecords(context);
	}

	/**
	 * 清空记录
	 * 
	 * @param context
	 */
	public static void delRecords(Context context) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences != null) {
			sharedPreferences.clear();
			sharedPreferences.commit();
		}

		// SharedPreferences sharedPreferences1 =
		// context.getSharedPreferences(APPRECOMMENDED_STATISTICS_ISSUED_DATA,
		// Activity.MODE_PRIVATE);
		// if(sharedPreferences1 != null){
		// SharedPreferences.Editor editorCommon1 = sharedPreferences1.edit();
		// if(editorCommon1 != null){
		// editorCommon1.clear().commit();
		// }
		// }
		clearIssuedTimes(context);

		PreferencesManager sharedPreferences2 = new PreferencesManager(context,
				APPRECOMMENDED_STATISTICS_DETAIL_CLICKED_DATA, Context.MODE_PRIVATE);
		if (sharedPreferences2 != null) {
			sharedPreferences2.clear();
			sharedPreferences2.commit();
		}
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static Map<String, ?> getAllDate(Context context) {
		PreferencesManager sp = new PreferencesManager(context, APPRECOMMENDED_STATISTICS_DATA,
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
				APPRECOMMENDED_STATISTICS_ISSUED_DATA, Context.MODE_PRIVATE);
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