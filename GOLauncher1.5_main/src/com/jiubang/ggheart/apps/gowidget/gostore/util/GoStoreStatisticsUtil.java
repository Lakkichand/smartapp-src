package com.jiubang.ggheart.apps.gowidget.gostore.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import android.content.Context;

import com.go.util.AppUtils;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.bean.BaseItemBean;
import com.jiubang.ggheart.apps.gowidget.gostore.bean.DetailItemBean;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.statistics.GoStoreAppStatistics;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsFuncId;

/**
 * 
 * <br>类描述:GO精品统计Util
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
public class GoStoreStatisticsUtil {

	private final static String GOSTORE_USERDATA_COMMON = "gostore_statisticsData_content"; // GOStore的统计数据sharedpreference文件
	private final static String GOSTORE_USERDATA_INSTALLED = "gostore_statisticsData_installed"; // GOStore的统计数据sharedpreference文件
	private final static String GOSTORE_USERDATA_SORT = "gostore_statisticsData_sort"; // GOStore的统计数据sharedpreference文件
	private final static String GOSTORE_INSTALL_TEMP_RECORD = "gostore_installed_temp_record"; // 保存安装临时记录
	private final static String GOSTORE_WIDGET_RECORD = "gostore_widget_record"; // GOStore插件统计
	private final static String GOSTORE_APP2PKG = "gostorewidget_app2pkg"; // GOStore的统计数据sharedpreference文件
	// edit by chenguanyu
	private final static String GOSTOREWIDGET_SHAREDPREFERENCE = "gostorewidget_statisticsData"; // GOStore的统计数据sharedpreference文件
	private final static String GOSTOREWIDGET_SHAREDPREFERENCE_CACHE = "gostorewidget_statisticsData_cache"; // GOStore的统计数据sharedpreference文件
	// end edit
	public final static String ONCE_ENTER_GOSTORE = "once_enter_gostore"; // 是否曾经进入过GO
																			// Store的标识
	public final static String ONCE_ENTER_GOSTORE_BY_MENU = "once_enter_gostore_by_menu"; // 是否曾经通过菜单进入过GO
																							// Store的标识
	public final static String ONCE_ENTER_GOSTORE_BY_WIDGET = "once_enter_gostore_by_widget"; // 是否曾经通过Widget进入过GO
																								// Store的标识
	private final static int USERDATA_COMMON_ITEM_COUNT = 6; // GOStore的统计数据中统计项数
	private final static int USERDATA_INSTALLED_ITEM_COUNT = 4; // GOStore的统计数据中统计项数
	private final static int USERDATA_SORT_ITEM_COUNT = 2; // GOStore的统计数据中统计项数
	private final static Long INSTALLED_INFOR_TIME_LIMIT = 1000 * 60 * 30L; // GOStore的统计数据中安装信息的过期时间
	public final static String USERDATA_IEMT_SPERATE = ";"; // GOStore的统计数据中统计项的分隔符

	// add by zhengruilin
	private final static String ENTER_SPERATE = "#";
	public final static byte ENTRY_TYPE_MENU = 1; // 菜单入口
	public final static byte ENTRY_TYPE_WIDGET = 2; // 插件入口
	public final static byte ENTRY_TYPE_NO_WIDGET = 3; // 无安装插件入口
	public final static byte ENTRY_TYPE_FUNTAB_ICON = 4; // 功能表图标入口
	public final static byte ENTRY_TYPE_FUNTAB_SEARCH = 5; // 功能表搜索入口
	public final static byte ENTRY_TYPE_APP_UPDATE = 6; // 应用更新入口
	public final static byte ENTRY_TYPE_OTHER = 0; // 其它入口
	public final static byte ENTRY_TYPE_THEME = 7; // GO文件夹GO桌面主题入口
	public final static byte ENTRY_TYPE_APPCENTER = 8; // 应用中心入口
	public final static byte ENTRY_TYPE_GAMECENTER = 9; // 游戏中心入口
	public final static byte ENTRY_TYPE_THEMEMANAGE = 10; // 主题预览入口
	public final static byte ENTRY_TYPE_MOREWALLPAPER = 11; // 更多壁纸入口
	public final static byte ENTRY_TYPE_MESSAGE_CENTER = 12; // 消息中心入口
	public final static byte ENTRY_TYPE_UPDATA_GUIDE = 13; // 更新引导入口
	public final static String ENTRY_SP_NAME = "current_entry";

	// go store插件统计字段
	public final static int WIDGET_KEY_ACTIVE = 0; // 是否活跃
	public final static int WIDGET_KEY_INTO_GOSTORE = 1; // 是否进入go精品
	public final static int WIDGET_KEY_ADD_TYPE = 2; // 添加方式
	public final static int WIDGET_KEY_TYPE = 3; // 样式如4x1 4x2 4x3

	public final static String VIRTUAL_SORT_ID_WIDGET = "8887"; // WIDGET的分类虚拟ID

	private final static int SOURCE_ID_MUGUA = 1; // 来源ID:木瓜移动

	/** 当前入口值 */
	// private static byte entryType = ENTRY_TYPE_OTHER;
	// edit by chenguanyu
	private final static RWLock LOCK = new RWLock();

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  zhouxuewen
	 * @date  [2012-9-12]
	 */
	private static class RWLock {
		private boolean mIsLock = false;
		private byte[] mLock = new byte[0];

		public void lock() {
			synchronized (mLock) {
				mIsLock = true;
			}
		}

		public void unlock() {
			synchronized (mLock) {
				mIsLock = false;
			}
		}

		public boolean isLock() {
			synchronized (mLock) {
				return mIsLock;
			}
		}
	}

	// end edit

	/**
	 * 清除统计数据的方法
	 * 
	 * @param context
	 */
	public synchronized static void clearStatisticsData(Context context) {
		if (context != null) {
			// 清除内容统计数据
			clearContentUserData(context);
			// 清除分类统计数据
			clearSortUserData(context);
			// 清除入口统计数据
			// clearEntryData(context);
			// 清除插件统计数据
			clearWidgetRecord(context);

			clearInstallTempRecord(context);
		}
	}

	/**
	 * 清除入口统计
	 */
	private static void clearEntryData(Context context) {
		if (context != null) {
			PreferencesManager entrySp = new PreferencesManager(context, ENTRY_SP_NAME,
					Context.MODE_PRIVATE);
			if (entrySp != null) {
				entrySp.clear();
				entrySp.commit();
			}
		}
	}

	/**
	 * 清除内容统计数据的方法
	 */
	private synchronized static void clearContentUserData(Context context) {
		if (context != null) {
			// SharedPreferences sharedPreferencesInstalled =
			// context.getSharedPreferences(GOSTORE_USERDATA_INSTALLED,
			// Activity.MODE_PRIVATE);
			// if(sharedPreferencesInstalled != null){
			// SharedPreferences.Editor editorInstalled =
			// sharedPreferencesInstalled.edit();
			// if(editorInstalled != null){
			// Map<String, ?> installedMap =
			// sharedPreferencesInstalled.getAll();
			// if(installedMap != null && installedMap.size() > 0){
			// Set<String> keySet = installedMap.keySet();
			// String record = null;
			// String[] items = null;
			// Long currentTime = System.currentTimeMillis();
			// for(String key : keySet){
			// record = String.valueOf(installedMap.get(key));
			// if(record != null && !"".equals(record)){
			// items = record.split(USERDATA_IEMT_SPERATE);
			// if(items != null && items.length >=
			// USERDATA_INSTALLED_ITEM_COUNT){
			// if(Integer.parseInt(items[2]) > 0){
			// editorInstalled.remove(key);
			// }else{
			// Long recordTime = 1L;
			// try {
			// recordTime = Long.parseLong(items[4]);
			// } catch (Exception e) {
			// // TODO: handle exception
			// }
			// if(currentTime - recordTime > INSTALLED_INFOR_TIME_LIMIT){
			// editorInstalled.remove(key);
			// }
			// }
			// }
			// }
			// }
			// editorInstalled.commit();
			// }
			// }
			// }

			PreferencesManager sharedPreferencesCommon = new PreferencesManager(context,
					GOSTORE_USERDATA_COMMON, Context.MODE_PRIVATE);
			if (sharedPreferencesCommon != null) {
				sharedPreferencesCommon.clear();
				sharedPreferencesCommon.commit();
			}

			sharedPreferencesCommon = new PreferencesManager(context, GOSTORE_USERDATA_INSTALLED,
					Context.MODE_PRIVATE);
			if (sharedPreferencesCommon != null) {
				sharedPreferencesCommon.clear();
				sharedPreferencesCommon.commit();
			}
		}
	}

	/**
	 * 清除安装临时数据的方法
	 * 
	 * @param context
	 */
	private synchronized static void clearInstallTempRecord(Context context) {
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTORE_INSTALL_TEMP_RECORD, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.clear();
				sharedPreferences.commit();
			}

			sharedPreferences = new PreferencesManager(context, GOSTORE_APP2PKG,
					Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.clear();
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 清除分类统计数据的方法
	 * 
	 * @param context
	 */
	private synchronized static void clearSortUserData(Context context) {
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTORE_USERDATA_SORT, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.clear();
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 保存内容统计数据中展示次数的方法 键值=ID，值=内容名称；推荐位；展示数；点击数；详细内容展示量
	 */
	public synchronized static void saveUserDataShow(ArrayList<BaseItemBean> baseItemBeans,
			Context context, String sortId) {
		// 新统计
		if (context != null && baseItemBeans != null) {
			for (BaseItemBean baseItemBean : baseItemBeans) {
				if (baseItemBean != null) {
					GoStoreAppStatistics.getInstance(context).saveDataWhenShow(
							baseItemBean.getPkgName(), String.valueOf(baseItemBean.getAdID()),
							baseItemBean.getItemNameString(), baseItemBean.getPosition(), null,
							sortId);
				}
			}
		}

		// 老统计
		// if(context != null && baseItemBeans != null)
		// {
		// SharedPreferences sharedPreferences =
		// context.getSharedPreferences(GOSTORE_USERDATA_COMMON,
		// Activity.MODE_PRIVATE);
		// if(sharedPreferences != null){
		// SharedPreferences.Editor editor = sharedPreferences.edit();
		// if(editor != null){
		// StringBuffer recordStringBuffer = null;
		// String[] items = null;
		// for(BaseItemBean baseItemBean : baseItemBeans){
		// if(baseItemBean != null){
		// recordStringBuffer = new StringBuffer();
		// String key = String.valueOf(baseItemBean.getAdID()) + ENTER_SPERATE +
		// getCurrentEntry(context);
		// String record = sharedPreferences.getString(key, null);
		// if(record != null && !"".equals(record.trim())){
		// items = record.split(USERDATA_IEMT_SPERATE);
		// if(items != null && items.length >= USERDATA_COMMON_ITEM_COUNT){
		// int showCount = Integer.parseInt(items[2])+1;
		// recordStringBuffer.append(items[0]).
		// append(USERDATA_IEMT_SPERATE).append(items[1]).
		// append(USERDATA_IEMT_SPERATE).append(showCount).
		// append(USERDATA_IEMT_SPERATE).append(items[3]).
		// append(USERDATA_IEMT_SPERATE).append(items[4]).
		// append(USERDATA_IEMT_SPERATE).append(items[5]);
		// }
		// }else {
		// recordStringBuffer.append(baseItemBean.getItemNameString()).
		// append(USERDATA_IEMT_SPERATE).append(baseItemBean.getPosition()).
		// append(USERDATA_IEMT_SPERATE).append(1).
		// append(USERDATA_IEMT_SPERATE).append(0).
		// append(USERDATA_IEMT_SPERATE).append(0).
		// append(USERDATA_IEMT_SPERATE).append(0);
		// }
		// editor.putString(key, recordStringBuffer.toString());
		// }
		// }
		// editor.commit();
		// // resetEntry();
		// }
		// }
		// }
	}

	/**
	 * 保存内容统计数据中详情界面展示次数的方法 键值=ID#入口，值=内容名字；推荐位；展示数；点击数；详细内容展示量
	 */
	public synchronized static void saveUserDataDetailShow(String itemId, String name,
			Context context, String sortId) {
		// 新统计
		GoStoreAppStatistics.getInstance(context).saveDataWhenDetailShow(null, itemId, name, null,
				sortId);

		// 老统计
		// if(context != null && itemId != null && !"".equals(itemId.trim()) &&
		// name != null && !"".equals(name.trim()))
		// {
		// SharedPreferences sharedPreferences =
		// context.getSharedPreferences(GOSTORE_USERDATA_COMMON,
		// Activity.MODE_PRIVATE);
		// if(sharedPreferences != null){
		// SharedPreferences.Editor editor = sharedPreferences.edit();
		// if(editor != null){
		// String key = itemId + ENTER_SPERATE + getCurrentEntry(context);
		// StringBuffer recordStringBuffer = new StringBuffer();
		// String record = sharedPreferences.getString(key, null);
		// if(record != null && !"".equals(record.trim())){
		// String[] items = record.split(USERDATA_IEMT_SPERATE);
		// if(items != null && items.length >= USERDATA_COMMON_ITEM_COUNT){
		// int showCount = Integer.parseInt(items[4])+1;
		// recordStringBuffer.append(items[0]).
		// append(USERDATA_IEMT_SPERATE).append(items[1]).
		// append(USERDATA_IEMT_SPERATE).append(items[2]).
		// append(USERDATA_IEMT_SPERATE).append(items[3]).
		// append(USERDATA_IEMT_SPERATE).append(showCount).
		// append(USERDATA_IEMT_SPERATE).append(items[5]);
		// }
		// }else {
		// recordStringBuffer.append(name).
		// append(USERDATA_IEMT_SPERATE).append(0).
		// append(USERDATA_IEMT_SPERATE).append(0).
		// append(USERDATA_IEMT_SPERATE).append(0).
		// append(USERDATA_IEMT_SPERATE).append(1).
		// append(USERDATA_IEMT_SPERATE).append(0);
		// }
		// editor.putString(key, recordStringBuffer.toString());
		// editor.commit();
		// // resetEntry();
		// }
		// }
		// }
	}

	/**
	 * 保存内容统计数据中点击次数的方法
	 */
	public synchronized static void saveUserDataTouch(BaseItemBean baseItemBean, boolean isUpdate,
			Context context, String sortId) {
		if (context != null && baseItemBean != null) {

			// 来源回调
			if (startRequestData(baseItemBean.getSource(), baseItemBean.getCallbackUrl())) {
				// 需要统计点击时间
				GoStoreAppStatistics.getInstance(context).saveDateWhenTouch(
						baseItemBean.getPkgName(), String.valueOf(baseItemBean.getAdID()),
						baseItemBean.getItemNameString(), baseItemBean.getPosition(), isUpdate,
						null, sortId, String.valueOf(DownloadUtil.getSerTime(context)));
			} else {
				// 一般统计
				GoStoreAppStatistics.getInstance(context).saveDateWhenTouch(
						baseItemBean.getPkgName(), String.valueOf(baseItemBean.getAdID()),
						baseItemBean.getItemNameString(), baseItemBean.getPosition(), isUpdate,
						null, sortId);
			}
		}
	}

	/**
	 * 保存内容统计数据中点击次数的方法(For Zip)
	 */
	public synchronized static void saveUserDataTouchForZip(BaseItemBean baseItemBean,
			Context context, String sortId) {
		if (context != null && baseItemBean != null) {

			// Zip统计
			GoStoreAppStatistics.getInstance(context).saveDateWhenTouch(baseItemBean.getPkgName(),
					String.valueOf(baseItemBean.getAdID()), baseItemBean.getItemNameString(),
					baseItemBean.getPosition(), false, null, sortId, 1);
		}
	}

	/**
	 * 保存内容统计数据中点击次数的方法(For Zip)
	 */
	public synchronized static void saveUserDataTouchForZip(DetailItemBean baseItemBean,
			Context context, String sortId) {
		if (context != null && baseItemBean != null) {

			// Zip统计
			GoStoreAppStatistics.getInstance(context).saveDateWhenTouch(baseItemBean.getPkgName(),
					String.valueOf(baseItemBean.getId()), baseItemBean.getItemNameString(), 0,
					false, null, sortId, 1);
		}
	}

	/**
	 * 异步回调URL，不使下载延迟
	 * 
	 * @author zhouxuewen
	 * @param url
	 *            回调URL
	 * @return isNeedStatClickTime 是否需要统计点击时间
	 */
	public static boolean startRequestData(int source, final String url) {
		boolean isNeedStatClickTime = false;
		if (source == SOURCE_ID_MUGUA) {
			DownloadUtil.sendCBackUrl(url);
			isNeedStatClickTime = true;
		}
		return isNeedStatClickTime;
	}

	/**
	 * 保存内容统计数据中点击次数的方法
	 */
	public synchronized static void saveUserDataTouch(DetailItemBean detailItemBean,
			boolean isUpdate, Context context, String sortId) {
		if (context != null && detailItemBean != null) {

			// 来源回调
			if (startRequestData(detailItemBean.getSource(), detailItemBean.getCallbackUrl())) {
				// 需要统计点击时间
				GoStoreAppStatistics.getInstance(context).saveDateWhenTouch(
						detailItemBean.getPkgName(), String.valueOf(detailItemBean.getId()),
						detailItemBean.getItemNameString(), 0, isUpdate, null, sortId,
						String.valueOf(DownloadUtil.getSerTime(context)));
			} else {
				// 一般统计
				GoStoreAppStatistics.getInstance(context).saveDateWhenTouch(
						detailItemBean.getPkgName(), String.valueOf(detailItemBean.getId()),
						detailItemBean.getItemNameString(), 0, isUpdate, null, sortId);

			}
		}
	}

	/**
	 * 
	 */
	public synchronized static void saveUserDataTouch(String id, String name, String pkgname,
			boolean isUpdate, Context context, String sortId, int source, String callbackUrl) {
		if (context != null) {

			// 来源回调
			if (startRequestData(source, callbackUrl)) {
				// 需要统计点击时间
				GoStoreAppStatistics.getInstance(context).saveDateWhenTouch(pkgname, id, name, 0,
						isUpdate, null, String.valueOf(DownloadUtil.getSerTime(context)));
			} else {
				// 一般统计
				GoStoreAppStatistics.getInstance(context).saveDateWhenTouch(pkgname, id, name, 0,
						isUpdate, null, sortId);
			}
		}
	}

	// /**
	// * 保存内容统计数据中点击次数的方法
	// */
	// public synchronized static boolean saveUserDataTouch(String id, String
	// name, int position, boolean isUpdate, Context context)
	// {
	// boolean isSuccess = false;
	// if(context != null
	// && id != null && !"".equals(id.trim())
	// && name != null && !"".equals(name.trim())
	// ){
	// SharedPreferences sharedPreferencesCommon =
	// context.getSharedPreferences(GOSTORE_USERDATA_COMMON,
	// Activity.MODE_PRIVATE);
	// if(sharedPreferencesCommon != null){
	// SharedPreferences.Editor editor = sharedPreferencesCommon.edit();
	// if(editor != null){
	// StringBuffer recordStringBuffer = new StringBuffer();
	// String key = id + ENTER_SPERATE + getCurrentEntry(context);
	// String record = sharedPreferencesCommon.getString(key, null);
	// if(record != null && !"".equals(record.trim())){
	// String[] items = record.split(USERDATA_IEMT_SPERATE);
	// if(items != null && items.length >= USERDATA_COMMON_ITEM_COUNT){
	// if(!isUpdate){
	// int touchCount = Integer.valueOf(items[3])+1;
	// recordStringBuffer.append(items[0]).
	// append(USERDATA_IEMT_SPERATE).append(items[1]).
	// append(USERDATA_IEMT_SPERATE).append(items[2]).
	// append(USERDATA_IEMT_SPERATE).append(touchCount).
	// append(USERDATA_IEMT_SPERATE).append(items[4]).
	// append(USERDATA_IEMT_SPERATE).append(items[5]);
	// }else{
	// int touchCount = Integer.valueOf(items[5])+1;
	// recordStringBuffer.append(items[0]).
	// append(USERDATA_IEMT_SPERATE).append(items[1]).
	// append(USERDATA_IEMT_SPERATE).append(items[2]).
	// append(USERDATA_IEMT_SPERATE).append(items[3]).
	// append(USERDATA_IEMT_SPERATE).append(items[4]).
	// append(USERDATA_IEMT_SPERATE).append(touchCount);
	// }
	// }
	// }else {
	// if(!isUpdate){
	// recordStringBuffer.append(name).
	// append(USERDATA_IEMT_SPERATE).append(position).
	// append(USERDATA_IEMT_SPERATE).append(0).
	// append(USERDATA_IEMT_SPERATE).append(1).
	// append(USERDATA_IEMT_SPERATE).append(0).
	// append(USERDATA_IEMT_SPERATE).append(0);
	// }else{
	// recordStringBuffer.append(name).
	// append(USERDATA_IEMT_SPERATE).append(position).
	// append(USERDATA_IEMT_SPERATE).append(0).
	// append(USERDATA_IEMT_SPERATE).append(0).
	// append(USERDATA_IEMT_SPERATE).append(0).
	// append(USERDATA_IEMT_SPERATE).append(1);
	// }
	// }
	// editor.putString(key, recordStringBuffer.toString());
	// editor.commit();
	// isSuccess = true;
	// // resetEntry();
	// }
	// }
	// }
	// return isSuccess;
	// }
	/**
	 * 保存内容统计数据中点击次数的方法
	 */
	public synchronized static boolean saveUserDataTouch(String id, String name, int position,
			boolean isUpdate, Context context) {
		boolean isSuccess = false;
		if (context != null && id != null && !"".equals(id.trim()) && name != null
				&& !"".equals(name.trim())) {
			PreferencesManager sharedPreferencesCommon = new PreferencesManager(context,
					GOSTORE_USERDATA_COMMON, Context.MODE_PRIVATE);
			if (sharedPreferencesCommon != null) {
				StringBuffer recordStringBuffer = new StringBuffer();
				String key = id + ENTER_SPERATE + getCurrentEntry(context);
				String record = sharedPreferencesCommon.getString(key, null);
				if (record != null && !"".equals(record.trim())) {
					String[] items = record.split(USERDATA_IEMT_SPERATE);
					if (items != null && items.length >= USERDATA_COMMON_ITEM_COUNT) {
						if (!isUpdate) {
							int touchCount = Integer.valueOf(items[3]) + 1;
							recordStringBuffer.append(items[0]).append(USERDATA_IEMT_SPERATE)
									.append(items[1]).append(USERDATA_IEMT_SPERATE)
									.append(items[2]).append(USERDATA_IEMT_SPERATE)
									.append(touchCount).append(USERDATA_IEMT_SPERATE)
									.append(items[4]).append(USERDATA_IEMT_SPERATE)
									.append(items[5]);
						} else {
							int touchCount = Integer.valueOf(items[5]) + 1;
							recordStringBuffer.append(items[0]).append(USERDATA_IEMT_SPERATE)
									.append(items[1]).append(USERDATA_IEMT_SPERATE)
									.append(items[2]).append(USERDATA_IEMT_SPERATE)
									.append(items[3]).append(USERDATA_IEMT_SPERATE)
									.append(items[4]).append(USERDATA_IEMT_SPERATE)
									.append(touchCount);
						}
					}
				} else {
					if (!isUpdate) {
						recordStringBuffer.append(name).append(USERDATA_IEMT_SPERATE)
								.append(position).append(USERDATA_IEMT_SPERATE).append(0)
								.append(USERDATA_IEMT_SPERATE).append(1)
								.append(USERDATA_IEMT_SPERATE).append(0)
								.append(USERDATA_IEMT_SPERATE).append(0);
					} else {
						recordStringBuffer.append(name).append(USERDATA_IEMT_SPERATE)
								.append(position).append(USERDATA_IEMT_SPERATE).append(0)
								.append(USERDATA_IEMT_SPERATE).append(0)
								.append(USERDATA_IEMT_SPERATE).append(0)
								.append(USERDATA_IEMT_SPERATE).append(1);
					}
				}
				sharedPreferencesCommon.putString(key, recordStringBuffer.toString());
				sharedPreferencesCommon.commit();
				isSuccess = true;
				// resetEntry();
			}
		}
		return isSuccess;
	}

	/**
	 * 保存分类统计数据中分类点击次数的方法 键值=ID，值=分类名称；点击数
	 */
	public synchronized static void saveUserDataSortTouch(String sortId, String sortName,
			Context context) {
		if (context != null && sortId != null && !"".equals(sortId.trim()) && sortName != null
				&& !"".equals(sortName.trim())) {
			PreferencesManager sharedPreferencesSort = new PreferencesManager(context,
					GOSTORE_USERDATA_SORT, Context.MODE_PRIVATE);
			if (sharedPreferencesSort != null) {
				StringBuffer resultBuffer = new StringBuffer();
				String record = sharedPreferencesSort.getString(sortId, null);
				if (null == record) {
					resultBuffer.append(sortName).append(USERDATA_IEMT_SPERATE).append(1);
				} else {
					String[] items = record.split(USERDATA_IEMT_SPERATE);
					if (items.length >= USERDATA_SORT_ITEM_COUNT) {
						int touchCount = Integer.valueOf(items[1]) + 1;
						resultBuffer.append(items[0]).append(USERDATA_IEMT_SPERATE)
								.append(touchCount);
					}
				}
				sharedPreferencesSort.putString(sortId, resultBuffer.toString());
				sharedPreferencesSort.commit();
			}
		}
	}

	/**
	 * 保存统计数据中安装信息的方法
	 * 
	 * @param baseItemBean
	 * @param context
	 */
	public synchronized static void saveUserDataInstalledInfor(BaseItemBean baseItemBean,
			boolean isUpdate, Context context) {
		if (context != null && baseItemBean != null) {
			// saveUserDataInstalledInfor(String.valueOf(baseItemBean.getAdID()),
			// baseItemBean.getItemNameString(), baseItemBean.getPkgName(),
			// context);
			saveInstallTemp(context, String.valueOf(baseItemBean.getAdID()),
					baseItemBean.getItemNameString(), baseItemBean.getPkgName(), isUpdate);
		}
	}

	/**
	 * 保存统计数据中安装信息的方法
	 * 
	 * @param detailItemBean
	 * @param context
	 */
	public synchronized static void saveUserDataInstalledInfor(DetailItemBean detailItemBean,
			boolean isUpdate, Context context) {
		if (context != null && detailItemBean != null) {
			// saveUserDataInstalledInfor(String.valueOf(detailItemBean.getId()),
			// detailItemBean.getItemNameString(), detailItemBean.getPkgName(),
			// context);
			saveInstallTemp(context, String.valueOf(detailItemBean.getId()),
					detailItemBean.getItemNameString(), detailItemBean.getPkgName(), isUpdate);
		}
	}

	// /**
	// * 保存统计数据中安装信息的方法
	// * @param id
	// * @param name
	// * @param packageName
	// * @param context
	// */
	// public synchronized static void saveUserDataInstalledInfor(String id,
	// String name, String packageName, Context context){
	// if(context != null
	// && id != null && !"".equals(id.trim())
	// && name != null && !"".equals(name.trim())
	// && packageName != null && !"".equals(packageName.trim())
	// ){
	// SharedPreferences sharedPreferences =
	// context.getSharedPreferences(GOSTORE_USERDATA_INSTALLED,
	// Activity.MODE_PRIVATE);
	// if(sharedPreferences != null){
	// SharedPreferences.Editor editor = sharedPreferences.edit();
	// if(editor != null){
	// StringBuffer recordStringBuffer = new StringBuffer();
	// String record = sharedPreferences.getString(id, null);
	// if(record != null && !"".equals(record.trim())){
	// String[] items = record.split(USERDATA_IEMT_SPERATE);
	// if(items != null && items.length >= USERDATA_INSTALLED_ITEM_COUNT){
	// recordStringBuffer.append(items[0]).
	// append(USERDATA_IEMT_SPERATE).append(items[1]).
	// append(USERDATA_IEMT_SPERATE).append(items[2]).
	// append(USERDATA_IEMT_SPERATE).append(items[3]).
	// append(USERDATA_IEMT_SPERATE).append(System.currentTimeMillis());
	// }
	// }else {
	// recordStringBuffer.append(name).
	// append(USERDATA_IEMT_SPERATE).append(packageName).
	// append(USERDATA_IEMT_SPERATE).append(0).
	// append(USERDATA_IEMT_SPERATE).append(0).
	// append(USERDATA_IEMT_SPERATE).append(System.currentTimeMillis());
	// }
	// editor.putString(id, recordStringBuffer.toString());
	// editor.commit();
	// }
	// }
	// }
	// }
	/**
	 * 更新安装数的方法
	 * 
	 * @param context
	 * @param packageName
	 */
	// public synchronized static void updateInstalledCount(Context context,
	// String packageName,String action){
	// if(context != null && packageName != null &&
	// !"".equals(packageName.trim())){
	// SharedPreferences sharedPreferencesInstalled =
	// context.getSharedPreferences(GOSTORE_USERDATA_INSTALLED,
	// Activity.MODE_PRIVATE);
	// if(sharedPreferencesInstalled != null){
	// Map<String,?> installedMap = sharedPreferencesInstalled.getAll();
	// SharedPreferences.Editor editor = sharedPreferencesInstalled.edit();
	// if(installedMap != null && installedMap.size() > 0 && editor != null){
	// Set<String> keySet = installedMap.keySet();
	// StringBuffer recordStringBuffer = null;
	// String installedRecord = null;
	// String[] items = null;
	// //循环拼接所有用户统计数据
	// for(String key : keySet){
	// installedRecord = String.valueOf(installedMap.get(key));
	// if(installedRecord != null){
	// items = installedRecord.split(USERDATA_IEMT_SPERATE);
	// if(items != null && items.length >= USERDATA_INSTALLED_ITEM_COUNT){
	// String recordPackageName = items[1];
	// if(recordPackageName.equals(packageName)){
	// recordStringBuffer = new StringBuffer();
	// if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
	// int installedCount = Integer.parseInt(items[2]) + 1;
	// recordStringBuffer.append(items[0]).append(USERDATA_IEMT_SPERATE)
	// .append(recordPackageName).append(USERDATA_IEMT_SPERATE)
	// .append(installedCount).append(USERDATA_IEMT_SPERATE)
	// .append(items[3]);
	// }else if(action.equals(Intent.ACTION_PACKAGE_REPLACED)){
	// int updateCount;
	// try {
	// updateCount = Integer.parseInt(items[3]) + 1;
	// } catch (Exception e) {
	// // TODO: handle exception
	// updateCount = 1;
	// }
	// recordStringBuffer.append(items[0]).append(USERDATA_IEMT_SPERATE)
	// .append(recordPackageName).append(USERDATA_IEMT_SPERATE)
	// .append(items[2]).append(USERDATA_IEMT_SPERATE)
	// .append(updateCount);
	// }
	// editor.putString(key, recordStringBuffer.toString());
	// }
	// }
	// }
	// }
	// editor.commit();
	// }
	// }
	// }
	// }
	/**
	 * 保存从哪里进入GO Store的方法
	 * 
	 * @param context
	 */
	public synchronized static void saveWhichEnterGostore(Context context, int appId) {
		if (context != null) {
			switch (appId) {
				case GoStorePublicDefine.GO_STORE_WIDGET_ID : {
					// 通过Widget进入
					GoStoreStatisticsUtil.saveOnceEnterGostore(context,
							GoStoreStatisticsUtil.ONCE_ENTER_GOSTORE_BY_WIDGET);
				}
					break;
				case GoStorePublicDefine.GOLAUNCHER_MENU_ID : {
					// 通过GO桌面菜单进入
					GoStoreStatisticsUtil.saveOnceEnterGostore(context,
							GoStoreStatisticsUtil.ONCE_ENTER_GOSTORE_BY_MENU);
				}
					break;

				default :
					break;
			}
		}
	}

	/**
	 * 保存是否曾经进入GO Store的方法
	 * 
	 * @param context
	 */
	public synchronized static void saveOnceEnterGostore(Context context, String key) {
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTORE_USERDATA_SORT, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.putBoolean(key, true);
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 获取是否曾经进入GO Store的方法
	 * 
	 * @param context
	 */
	public synchronized static boolean getOnceEnterGostore(Context context, String key) {
		boolean result = false;
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTORE_USERDATA_SORT, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				result = sharedPreferences.getBoolean(key, false);
			}
		}
		return result;
	}

	/**
	 * 取到用户统计内容数据的方法 格式---产品ID;名称;推荐位;展示数;点击数;更新点击数;详细内容展示量;安装数 ;更新数;入口号
	 * 
	 * @return 所有内容统计数据的集合
	 */
	public synchronized static ArrayList<String> getContentUserDataList(Context context) {
		ArrayList<String> resultArrayList = null;
		try {
			if (context != null) {
				PreferencesManager commonSp = new PreferencesManager(context,
						GOSTORE_USERDATA_COMMON, Context.MODE_PRIVATE);
				PreferencesManager installedSp = new PreferencesManager(context,
						GOSTORE_USERDATA_INSTALLED, Context.MODE_PRIVATE);
				Map<String, ?> commonMap = commonSp.getAll();
				Map<String, ?> installedMap = installedSp.getAll();
				boolean isInstallMapEmpty = true;
				if (installedMap != null && installedMap.size() > 0) {
					isInstallMapEmpty = false;
				}
				// 初始化数据集合
				resultArrayList = new ArrayList<String>();

				if (commonMap != null && commonMap.size() > 0) {
					Set<String> keySet = commonMap.keySet();
					StringBuffer recordStringBuffer = null;
					String commonRecord = null;
					String installedRecord = null;

					String[] items = null;
					// 循环拼接所有用户统计数据
					for (String key : keySet) {
						String[] keySplit = key.split(ENTER_SPERATE); // add by
																		// zhengruilin
						if (keySplit == null || keySplit.length == 0) {
							continue;
						}
						if (keySplit.length < 2) {
							keySplit = new String[] { keySplit[0], "" + ENTRY_TYPE_OTHER };
						}
						int installedCount = 0;
						int updateCount = 0;
						String id = keySplit[0];
						String entryType = keySplit[1];
						commonRecord = String.valueOf(commonMap.get(key));
						if (commonRecord != null) {
							recordStringBuffer = new StringBuffer();
							recordStringBuffer.append(id).append(USERDATA_IEMT_SPERATE)
									.append(commonRecord).append(USERDATA_IEMT_SPERATE);
							String[] commonItem = commonRecord.split(USERDATA_IEMT_SPERATE);
							int clickCount = 0;
							String appName = "";
							try {
								if (commonItem != null && commonItem.length >= 5) {
									appName = commonItem[0];
									clickCount = Integer.valueOf(commonItem[3]);
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
							if (!isInstallMapEmpty) {
								// String installKey = id + ENTER_SPERATE +
								// package
								installedRecord = String.valueOf(installedMap.get(key));
								if (installedRecord != null && !"".equals(installedRecord)) {
									items = installedRecord.split(USERDATA_IEMT_SPERATE);
									if (items != null
											&& items.length >= USERDATA_INSTALLED_ITEM_COUNT) {
										installedCount = Integer.parseInt(items[2]);
										updateCount = Integer.parseInt(items[3]);
										if (installedCount > 0 || updateCount > 0) {
											installedMap.remove(key);
											// installedSp.edit().remove(key).commit();
										}
									}
								}
							}

							if (clickCount > 0 && installedCount == 0) {
								String pkgName = getPkgNameByAppName(appName, context);
								if (pkgName != null && !pkgName.equals("")) {
									if (AppUtils.isAppExist(context, pkgName)) {
										installedCount = 1;
									}
								}
							}
							// if(Integer.valueOf(updateCount)>installedCount)updateCount="0";
							recordStringBuffer.append(installedCount).append(USERDATA_IEMT_SPERATE)
									.append(updateCount).append(USERDATA_IEMT_SPERATE)
									.append(entryType);
						}
						resultArrayList.add(recordStringBuffer.toString());
					}
				}
				if (installedMap != null && installedMap.size() > 0) {
					Set<String> keySet = installedMap.keySet();
					String installedRecord = null;
					StringBuffer recordStringBuffer = null;
					String[] items = null;

					for (String key : keySet) {
						String[] keys = key.split(ENTER_SPERATE);
						String id = keys[0];
						String entryType = keys[1];

						int installedCount = 0;
						int updateCount = 0;
						installedRecord = String.valueOf(installedMap.get(key));
						/*
						 * key: ID#入口; value: 包名;软件名;安装数;更新数;
						 */
						if (installedRecord != null && !"".equals(installedRecord)) {
							items = installedRecord.split(USERDATA_IEMT_SPERATE);
							if (items != null && items.length >= USERDATA_INSTALLED_ITEM_COUNT) {
								String appName = items[1];
								installedCount = Integer.parseInt(items[2]);
								updateCount = Integer.parseInt(items[3]);
								if (installedCount > 0 || updateCount > 0) {
									recordStringBuffer = new StringBuffer();
									recordStringBuffer.append(id).append(USERDATA_IEMT_SPERATE)
											.append(appName).append(USERDATA_IEMT_SPERATE)
											.append(0).append(USERDATA_IEMT_SPERATE).append(0)
											.append(USERDATA_IEMT_SPERATE).append(0)
											.append(USERDATA_IEMT_SPERATE).append(0)
											.append(USERDATA_IEMT_SPERATE).append(0)
											.append(USERDATA_IEMT_SPERATE).append(installedCount)
											.append(USERDATA_IEMT_SPERATE).append(updateCount)
											.append(USERDATA_IEMT_SPERATE).append(entryType);
									resultArrayList.add(recordStringBuffer.toString());
									installedMap.remove(key);
									// installedSp.edit().remove(key).commit();
								}
							}// 产品ID;名称;推荐位;展示数;点击数;更新点击数;详细内容展示量;安装数 ;更新数;入口号
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultArrayList;
	}

	/**
	 * 保存包名和应用名的键值对
	 * 
	 * @author zhouxuewen
	 * @param appName
	 * @param pkgName
	 * @param context
	 */
	private static void savePkgNameByAppName(String appName, String pkgName, Context context) {
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context, GOSTORE_APP2PKG,
					Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.putString(appName, pkgName);
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 通过Pkg名字找应用名字
	 * 
	 * @author zhouxuewen
	 * @param appName
	 * @param context
	 * @return
	 */
	private static String getPkgNameByAppName(String appName, Context context) {
		String pkgName = "";
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context, GOSTORE_APP2PKG,
					Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				pkgName = sharedPreferences.getString(appName, "");
			}
		}

		return pkgName;
	}

	/**
	 * 取到用户统计分类数据的方法 格式---分类ID;分类名称;分类点击次数
	 * 
	 * @return 所有分类统计数据的集合
	 */
	public synchronized static ArrayList<String> getSortUserDataList(Context context) {
		ArrayList<String> resultArrayList = null;
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTORE_USERDATA_SORT, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				Map<String, ?> sortMap = sharedPreferences.getAll();
				if (sortMap != null && sortMap.size() > 0) {
					resultArrayList = new ArrayList<String>();
					Set<String> keySet = sortMap.keySet();
					StringBuffer recordBuffer = null;
					for (String key : keySet) {
						if (!key.equals(ONCE_ENTER_GOSTORE)
								&& !key.equals(ONCE_ENTER_GOSTORE_BY_MENU)
								&& !key.equals(ONCE_ENTER_GOSTORE_BY_WIDGET)) {
							recordBuffer = new StringBuffer();
							recordBuffer.append(key).append(USERDATA_IEMT_SPERATE)
									.append(sortMap.get(key));
							resultArrayList.add(recordBuffer.toString());
						}
					}
				}
			}
		}
		return resultArrayList;
	}

	// edit by chenguanyu
	/**
	 * 统计数据上传失败的处理方法
	 */
	public synchronized static void exceptionUserData(Context context) {
		if (context != null) {
			// 把缓存数据同步到正式存储文件中
			synchronousUserData(context);
			// 解锁
			LOCK.unlock();
		}
	}

	/**
	 * 清除上一次统计数据的方法
	 */
	public synchronized static void clearUserData(Context context) {
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTOREWIDGET_SHAREDPREFERENCE, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				// 清除搜索、推荐、分类的点击次数
				// 添加到桌面的次数不清除
				sharedPreferences.remove(GoStorePublicDefine.USERDATA_KEY_SEARCH);
				sharedPreferences.remove(GoStorePublicDefine.USERDATA_KEY_RECOMMEND);
				sharedPreferences.remove(GoStorePublicDefine.USERDATA_KEY_SORT);
				sharedPreferences.commit();
			}
		}
		// 把缓存数据同步到正式存储文件中
		synchronousUserData(context);
		// 解锁
		LOCK.unlock();
	}

	/**
	 * 保存用户统计数据的方法
	 * 
	 * @param context
	 * @param key
	 *            点击区域的KEY
	 * @param changeCount
	 *            改变量
	 */
	public synchronized static void saveUserData(Context context, String key, int changeCount) {

		if (context != null && key != null && !"".equals(key)) {
			PreferencesManager sharedPreferences = null;
			if (LOCK.isLock()) {
				// 如果已经加锁，则写到缓存文件里面
				sharedPreferences = new PreferencesManager(context,
						GOSTOREWIDGET_SHAREDPREFERENCE_CACHE, Context.MODE_PRIVATE);
			} else {
				// 如果没有加锁，则直接写
				sharedPreferences = new PreferencesManager(context, GOSTOREWIDGET_SHAREDPREFERENCE,
						Context.MODE_PRIVATE);
			}
			int count = sharedPreferences.getInt(key, 0);
			count += changeCount;
			sharedPreferences.putInt(key, count);
			sharedPreferences.commit();
		}
	}

	/**
	 * 获取用户统计数据的方法
	 * 
	 * @param context
	 * @return 用户统计数据字符串 搜索次数||点击推荐次数||点击栏目次数
	 */
	public synchronized static String getUserData(Context context) {

		// 加锁
		LOCK.lock();

		StringBuffer result = new StringBuffer();
		if (context != null) {
			String separateString = "||";
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTOREWIDGET_SHAREDPREFERENCE, Context.MODE_PRIVATE);
			int searchTouchCount = sharedPreferences.getInt(
					GoStorePublicDefine.USERDATA_KEY_SEARCH, 0);
			int recommendTouchCount = sharedPreferences.getInt(
					GoStorePublicDefine.USERDATA_KEY_RECOMMEND, 0);
			int sortTouchCount = sharedPreferences.getInt(GoStorePublicDefine.USERDATA_KEY_SORT, 0);
			result.append(searchTouchCount).append(separateString).append(recommendTouchCount)
					.append(separateString).append(sortTouchCount);
		}
		return result.toString();
	}

	/**
	 * 同步统计数据的方法 把统计数据缓存文件中的数据累加到正式文件中，并清除缓存文件中的数据
	 */
	private synchronized static void synchronousUserData(Context context) {
		if (context != null) {
			PreferencesManager cacheSharedPreferences = new PreferencesManager(context,
					GOSTOREWIDGET_SHAREDPREFERENCE_CACHE, Context.MODE_PRIVATE);
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTOREWIDGET_SHAREDPREFERENCE, Context.MODE_PRIVATE);
			Map<String, ?> cacheMap = cacheSharedPreferences.getAll();
			if (cacheMap != null && cacheMap.size() > 0) {
				Set<String> keySet = cacheMap.keySet();
				int count = 0;
				for (String key : keySet) {
					count = (Integer) cacheMap.get(key);
					sharedPreferences.putInt(key, sharedPreferences.getInt(key, 0) + count);
				}
				sharedPreferences.commit();
			}
			// 把缓存统计数据清理掉
			cacheSharedPreferences.clear();
			cacheSharedPreferences.commit();
		}
	}

	/**
	 * 设置当前GO store入口
	 * 
	 * @param entryType
	 */
	public synchronized static void setCurrentEntry(byte entryType, Context context) {
		if (context == null) {
			return;
		}
		PreferencesManager entrySp = new PreferencesManager(context, ENTRY_SP_NAME,
				Context.MODE_PRIVATE);
		if (entrySp != null) {
			entrySp.putInt(ENTRY_SP_NAME, entryType); // 标记当前入口
			entrySp.commit();
		}
	}

	/**
	 * 读取当前入口
	 */
	public static int getCurrentEntry(Context context) {
		if (context == null) {
			return ENTRY_TYPE_OTHER;
		}
		PreferencesManager entrySp = new PreferencesManager(context, ENTRY_SP_NAME,
				Context.MODE_PRIVATE);
		if (entrySp != null) {
			int entryType = entrySp.getInt(ENTRY_SP_NAME, ENTRY_TYPE_OTHER);
			if (entrySp != null) {
				entrySp.putInt("" + entryType, 1);
				entrySp.commit(); // 标记该渠道号为活跃入口
			}
			return entryType;
		}
		return ENTRY_TYPE_OTHER;
	}

	/**
	 * 重置当前入口
	 */
	public static void resetCurrentEntry(Context context) {
		setCurrentEntry(ENTRY_TYPE_OTHER, context);
	}

	/**
	 * 获取五个用户入口 是否活跃的记录
	 */
	public static int[] getEntryLively(Context context) {
		int[] list = new int[5];
		PreferencesManager sharedPreferences = new PreferencesManager(context, ENTRY_SP_NAME,
				Context.MODE_PRIVATE);
		if (sharedPreferences == null) {
			return list;
		}
		list[0] = sharedPreferences.getInt("" + ENTRY_TYPE_NO_WIDGET, 0);
		list[1] = sharedPreferences.getInt("" + ENTRY_TYPE_FUNTAB_ICON, 0);
		list[2] = sharedPreferences.getInt("" + ENTRY_TYPE_FUNTAB_SEARCH, 0);
		list[3] = 0;
		list[4] = sharedPreferences.getInt("" + ENTRY_TYPE_OTHER, 0);

		return list;
	}

	/**
	 * 保存安装点击的临时记录 格式： key: 包名 value：id;入口号;是否更新;时间
	 */
	public synchronized static void saveInstallTemp(Context context, String id, String appName,
			String packageName, boolean isUpdate) {
		PreferencesManager tempSp = new PreferencesManager(context, GOSTORE_INSTALL_TEMP_RECORD,
				Context.MODE_PRIVATE);

		long time = System.currentTimeMillis();
		String value = id + ENTER_SPERATE + appName + ENTER_SPERATE + getCurrentEntry(context)
				+ ENTER_SPERATE + isUpdate + ENTER_SPERATE + time;
		tempSp.putString(packageName, value);
		tempSp.commit();
	}

	/**
	 * 统计信息
	 */
	public synchronized static void doInstallStistics(Context context, String packageName) {
		String[] keys = checkInstallRecord(context, packageName);
		if (null != keys && keys.length >= 5) {
			saveInstalledRecord(keys[0], keys[1], keys[2], Byte.parseByte(keys[3]),
					Boolean.parseBoolean(keys[4]), context);
		}
	}

	/**
	 * 检查安装临时记录，读取ID及入口信息 return {id， 软件名， 包名， 入口号， 是否为更新}
	 */
	public static String[] checkInstallRecord(Context context, String packageName) {
		PreferencesManager tempSp = new PreferencesManager(context, GOSTORE_INSTALL_TEMP_RECORD,
				Context.MODE_PRIVATE);
		String tempRecord = tempSp.getString(packageName, null);
		String[] ret = null;
		if (tempRecord != null) {
			String[] items = tempRecord.split(ENTER_SPERATE);
			if (items.length >= 3) {
				try {
					String id = items[0];
					String appName = items[1];
					int entryType = Integer.parseInt(items[2]);
					String isUpdate = items[3];
					long oldTime = Long.parseLong(items[4]);
					long newTime = System.currentTimeMillis();
					long minutes = (newTime - oldTime) / (1000 * 60);
					// 临时记录从点击到安装完毕在30分钟内有效
					if (minutes <= 30) {
						ret = new String[] { id + "", appName, packageName, entryType + "",
								isUpdate + "" };
					}
					tempSp.remove(packageName);
					tempSp.commit();
				} catch (Exception e) {
				}
			}
			// }else{
			// return new String[]{0 + "", packageName, packageName,
			// getCurrentEntry(context) + "", false + ""};
		}
		return ret;
	}

	/**
	 * 保存安装统计 格式： key: ID#入口; value: 包名;软件名;安装数;更新数;
	 * 
	 * @param id
	 * @param name
	 * @param packageName
	 * @param context
	 */
	public synchronized static void saveInstalledRecord(String id, String name, String packageName,
			byte entryType, boolean isUpdate, Context context) {
		if (context != null && id != null && !"".equals(id.trim()) && name != null
				&& !"".equals(name.trim()) && packageName != null && !"".equals(packageName.trim())) {
			PreferencesManager installSp = new PreferencesManager(context,
					GOSTORE_USERDATA_INSTALLED, Context.MODE_PRIVATE);
			if (installSp != null) {
				String key = id + ENTER_SPERATE + entryType;
				StringBuffer recordSb = new StringBuffer();
				String record = installSp.getString(key, null);

				int installCount = 0;
				int updateCount = 0;
				// 先计数
				if (!isUpdate) {
					installCount++;
				} else {
					updateCount++;
				}
				// 如果存在记录
				if (record != null && !"".equals(record.trim())) {
					String[] items = record.split(USERDATA_IEMT_SPERATE);
					if (items != null && items.length >= USERDATA_INSTALLED_ITEM_COUNT) {
						try {
							installCount += Integer.parseInt(items[1]);
							updateCount += Integer.parseInt(items[2]);
						} catch (Exception e) {
						}
					}
				}
				recordSb.append(packageName).append(USERDATA_IEMT_SPERATE).append(name)
						.append(USERDATA_IEMT_SPERATE).append(installCount)
						.append(USERDATA_IEMT_SPERATE).append(updateCount);
				installSp.putString(key, recordSb.toString());
				installSp.commit();
			}
		}
	}

	/**
	 * 保存go store插件统计记录 字段：是否活跃||是否进入go精品||添加方式||样式
	 * 
	 * @param context
	 * @param key
	 *            插件ID
	 * @param value
	 */
	public synchronized static void saveWidgetRecord(Context context, String key, int field,
			String value) {
		PreferencesManager widgetSp = new PreferencesManager(context, GOSTORE_WIDGET_RECORD,
				Context.MODE_PRIVATE);
		if (widgetSp != null) {
			// String widgetType = "error";
			String record = widgetSp.getString(key, null);
			if (record == null) {
				record = "0" + ENTER_SPERATE + "0" + ENTER_SPERATE + "0" + ENTER_SPERATE + "xx";
				ArrayList<GoWidgetBaseInfo> widgets = null;
				try {
					widgets = AppCore.getInstance().getGoWidgetManager().getWidgets();
				} catch (NullPointerException e) {
					return;
				}
				
				if (widgets == null) {
					return;
				}
				for (int i = 0; i < widgets.size(); i++) {
					GoWidgetBaseInfo widgetInfo = widgets.get(i);
					if (widgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
						String type;
						switch (widgetInfo.mType) {
							case 0 :
								type = "4x3";
								break;
							case 1 :
								type = "4x1";
								break;
							case 2 :
								type = "4x2";
								break;
							default :
								type = "xx";
								break;
						}
						record = "0" + ENTER_SPERATE + "0" + ENTER_SPERATE + "0" + ENTER_SPERATE
								+ type;
						key = String.valueOf(widgetInfo.mWidgetId);
						break;
					}
				}
			}
			String[] records = record.split(ENTER_SPERATE);
			if (records != null && records.length >= 4) {
				if (field == WIDGET_KEY_TYPE) {
					switch (Integer.parseInt(value)) {
						case 0 :
							value = "4x3";
							break;
						case 1 :
							value = "4x1";
							break;
						case 2 :
							value = "4x2";
							break;
						default :
							value = "xx";
							break;
					}
				} else {
					ArrayList<GoWidgetBaseInfo> widgets = AppCore.getInstance()
							.getGoWidgetManager().getWidgets();
					for (int i = 0; i < widgets.size(); i++) {
						GoWidgetBaseInfo widgetInfo = widgets.get(i);
						if (String.valueOf(widgetInfo.mWidgetId).equals(key)) {
							switch (widgetInfo.mType) {
								case 0 :
									records[WIDGET_KEY_TYPE] = "4x3";
									break;
								case 1 :
									records[WIDGET_KEY_TYPE] = "4x1";
									break;
								case 2 :
									records[WIDGET_KEY_TYPE] = "4x2";
									break;
								default :
									records[WIDGET_KEY_TYPE] = "xx";
									break;
							}
						}
					}
				}

				records[field] = value;
				StringBuffer recordSb = new StringBuffer();
				for (int i = 0; i < records.length; i++) {
					recordSb.append(records[i]).append(ENTER_SPERATE);
				}
				if (widgetSp != null && key != null && value != null) {
					widgetSp.putString(key, recordSb.toString());
					widgetSp.commit();
				}
			}
		}
	}

	/**
	 * 清除插件统计
	 * 
	 * @param context
	 */
	public synchronized static void clearWidgetRecord(Context context) {
		PreferencesManager widgetSp = new PreferencesManager(context, GOSTORE_WIDGET_RECORD,
				Context.MODE_PRIVATE);
		if (widgetSp != null) {
			Vector<String> effectiveRecord = new Vector<String>();
			ArrayList<GoWidgetBaseInfo> widgets = AppCore.getInstance().getGoWidgetManager()
					.getWidgets();
			for (int i = 0; i < widgets.size(); i++) {
				GoWidgetBaseInfo widgetInfo = widgets.get(i);
				// 记录添加在桌面的widget id
				if (widgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
					effectiveRecord.add(widgetInfo.mWidgetId + "");
				}
			}

			Map<String, ?> recordMap = widgetSp.getAll();
			if (recordMap != null && recordMap.size() > 0) {
				Set<String> keySet = recordMap.keySet();
				for (String key : keySet) {
					// 检查widget是否在桌面，已不在桌面的为失效的数据
					boolean isEffective = false;
					for (int i = 0; i < effectiveRecord.size(); i++) {
						if (key.equals(effectiveRecord.get(i))) {
							isEffective = true;
						}
					}
					// 有效数据点击的记录清零
					if (isEffective) {
						saveWidgetRecord(context, key, WIDGET_KEY_ACTIVE, "0");
						saveWidgetRecord(context, key, WIDGET_KEY_INTO_GOSTORE, "0");
					}
					// 无效数据删除
					else {
						widgetSp.remove(key + "");
						widgetSp.commit();
					}
				}
			}
			effectiveRecord.clear();
			effectiveRecord = null;
		}
	}

	/**
	 * 插件统计数据 格式：是否活跃||是否进入go精品||添加方式||样式 前三个的值 1 是 0 否 添加方式 1手动添加 0默认添加
	 */
	public synchronized static String getWidgetRecord(Context context) {
		PreferencesManager widgetSp = new PreferencesManager(context, GOSTORE_WIDGET_RECORD,
				Context.MODE_PRIVATE);
		StringBuffer recordSb = new StringBuffer();
		if (widgetSp != null) {
			ArrayList<GoWidgetBaseInfo> widgets = AppCore.getInstance().getGoWidgetManager()
					.getWidgets();
			for (int i = 0; i < widgets.size(); i++) {
				GoWidgetBaseInfo widgetInfo = widgets.get(i);
				if (widgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
					String deskRecord = widgetSp.getString(widgetInfo.mWidgetId + "", null);
					if (deskRecord == null) {
						String type = "xx";
						switch (widgetInfo.mType) {
							case 0 :
								type = "4x3";
								break;
							case 1 :
								type = "4x1";
								break;
							case 2 :
								type = "4x2";
								break;
							default :
								type = "xx";
								break;
						}
						widgetSp.putString(widgetInfo.mWidgetId + "", "0" + ENTER_SPERATE + "0"
								+ ENTER_SPERATE + "0" + ENTER_SPERATE + type);
						widgetSp.commit();
					}
				}
			}

			Map<String, ?> recordMap = widgetSp.getAll();
			if (recordMap != null && recordMap.size() > 0) {
				Set<String> keySet = recordMap.keySet();
				for (String key : keySet) {
					String record = widgetSp.getString(key, null);
					String[] records = record.split(ENTER_SPERATE);
					if (records != null && records.length >= 4) {
						// 一级功能ID
						recordSb.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOSTOREAPP)
								.append(Statistics.STATISTICS_DATA_SEPARATE_STRING)
								// 二级功能ID
								.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_GOSTORE_WIDGET)
								.append(Statistics.STATISTICS_DATA_SEPARATE_STRING)
								// 数据
								.append(records[WIDGET_KEY_ACTIVE])
								.append(Statistics.STATISTICS_DATA_SEPARATE_STRING)
								.append(records[WIDGET_KEY_INTO_GOSTORE])
								.append(Statistics.STATISTICS_DATA_SEPARATE_STRING)
								.append(records[WIDGET_KEY_ADD_TYPE])
								.append(Statistics.STATISTICS_DATA_SEPARATE_STRING)
								.append(records[WIDGET_KEY_TYPE])
								.append(Statistics.STATISTICS_DATA_LINEFEED);
					}
				}
			}
		}
		return recordSb.toString();
	}
}
