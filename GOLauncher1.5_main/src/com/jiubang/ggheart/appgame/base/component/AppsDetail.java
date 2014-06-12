/**
 * 应用中心详情跳转工具类
 */
package com.jiubang.ggheart.appgame.base.component;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.gostore.base.component.AppsThemeDetailActivity;
import com.jiubang.ggheart.appgame.gostore.base.component.WallpaperDetailActivity;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;

/**
 * @author liguoliang
 * 
 */
public class AppsDetail {
	// 详情页入口
	public final static int START_TYPE_APPMANAGEMENT = 1; // 是从应用更新中进入
	public final static int START_TYPE_APPRECOMMENDED = 2; // 是从应用推荐中进入
//	public final static int START_TYPE_GAMERECOMENDED = 12; // 从游戏推荐中进入
	public final static int START_TYPE_WIDGET_APP = 13; // 从应用的widget中进入
//	public final static int START_TYPE_WIDGET_GAME = 14; // 从游戏的widget中进入
	public final static int START_TYPE_DOWNLOAD_GO = 15; // go包下载

	// 功能表搜索及Widget搜索统计都归到应用中心
	public final static int START_TYPE_GO_SEARCH_WIDGET = 16; // GO搜索WIDGET中进入
	public final static int START_TYPE_APPFUNC_SEARCH = 17; //从功能表搜索进入应用中心

	/**
	 * 跳转到详情页
	 * 
	 * @param context
	 * @param boutiqueApp
	 * @param startType
	 *            入口，用于区分游戏中心，应用中心
	 * 
	 * @param index
	 *            位置索引值，由1开始
	 * 
	 * @param isNeedDetailCount
	 *            是否需要统计进入详情页数
	 */
	public static void jumpToDetail(Context context, BoutiqueApp boutiqueApp, int startType,
			int index, boolean isNeedDetailCount) {
		// 这段代码缺少分类id categoryId,可能引起统计错误
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		if (boutiqueApp == null) {
			return;
		}

		// update by zhoujun 如果是木瓜移动的数据，需要回调url
		if (boutiqueApp.info.cback == BoutiqueApp.BoutiqueAppInfo.NEED_TO_CBACK) {
			if (boutiqueApp.info.cbacktype == BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_ALL
					|| boutiqueApp.info.cbacktype == BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_DETAIL) {
				DownloadUtil.sendCBackUrl(boutiqueApp.info.cbackurl);
			}
		}
		// update by zhoujun 2012-08-30 end

		if (startType == START_TYPE_APPMANAGEMENT || startType == START_TYPE_APPRECOMMENDED
				|| startType == START_TYPE_WIDGET_APP || startType == START_TYPE_APPFUNC_SEARCH
				|| startType == START_TYPE_GO_SEARCH_WIDGET) {
			// 应用中心统计代码
			AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(context,
					AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);
			if (isNeedDetailCount) {
				AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(context,
						boutiqueApp.info.packname, Integer.parseInt(boutiqueApp.info.appid), 1);
			}

			AppRecommendedStatisticsUtil.getInstance().saveDetailsClick2MainTable(context,
					boutiqueApp.info.packname, Integer.parseInt(boutiqueApp.info.appid),
					boutiqueApp.typeid + "");
		}
		int acttype = boutiqueApp.acttype;
		if (acttype == BoutiqueApp.FEATURE_ACTTYPE_FTP) {
			AppsDetail.gotoDetailDirectly(context,
					Integer.parseInt(boutiqueApp.info.appid),
					boutiqueApp.info.packname, startType, boutiqueApp.typeid
							+ "", boutiqueApp.name, boutiqueApp.info.grade,
					boutiqueApp.info.detailstyle, boutiqueApp);
		} else if (acttype == BoutiqueApp.FEATURE_ACTTYPE_MARKET) {
			String actvalue = boutiqueApp.actvalue;
			if (actvalue != null && !"".equals(actvalue)) {
				actvalue = actvalue.trim();

				// 统计代码
				if (startType == START_TYPE_APPMANAGEMENT || startType == START_TYPE_APPRECOMMENDED
						|| startType == START_TYPE_WIDGET_APP
						|| startType == START_TYPE_APPFUNC_SEARCH
						|| startType == START_TYPE_GO_SEARCH_WIDGET) {
					AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(context,
							boutiqueApp.info.packname, Integer.parseInt(boutiqueApp.info.appid),
							"" + boutiqueApp.typeid, 1,
							String.valueOf(DownloadUtil.getSerTime(context)));
				}

				// TODO 木瓜sdk相应代码
				String afCBackUrl = boutiqueApp.info.iAfCbackurl;
				if (afCBackUrl != null && !afCBackUrl.equals("")) {
					GoStoreOperatorUtil.gotoMarket(context, actvalue, afCBackUrl);
				} else {
					GoStoreOperatorUtil.gotoMarket(context, actvalue);
				}
			}
		} else if (acttype == BoutiqueApp.FEATURE_ACTTYPE_BROWSER) {
			String actvalue = boutiqueApp.actvalue;
			if (actvalue != null && !"".equals(actvalue)) {
				actvalue = actvalue.trim();

				// 统计代码
				if (startType == START_TYPE_APPMANAGEMENT || startType == START_TYPE_APPRECOMMENDED
						|| startType == START_TYPE_WIDGET_APP
						|| startType == START_TYPE_APPFUNC_SEARCH
						|| startType == START_TYPE_GO_SEARCH_WIDGET) {
					AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(context,
							boutiqueApp.info.packname, Integer.parseInt(boutiqueApp.info.appid),
							"" + boutiqueApp.typeid, 1,
							String.valueOf(DownloadUtil.getSerTime(context)));
				}

				GoStoreOperatorUtil.gotoBrowser(context, actvalue);
			}
		}

	}
	/**
	 * <br>功能简述: 跳转到应用中心主题详情页
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param boutiqueApp
	 * @param startType 入口，用于区分游戏中心，应用中心
	 * @param index 位置索引值，由1开始
	 * @param isNeedDetailCount 是否需要统计进入详情页数
	 * @param appids 列表内的应用id
	 * @param pkgs 列表内的应用包名
	 * @param icons 列表内的图标url
	 * @param showList 是否在详情显示列表
	 */
	public static void jumpToThemeDetail(Context context, BoutiqueApp boutiqueApp, int startType,
			int index, boolean isNeedDetailCount, ArrayList<String> appids, ArrayList<String> pkgs,
			ArrayList<String> icons, int showList) {
		// 这段代码缺少分类id categoryId,可能引起统计错误
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		if (boutiqueApp == null) {
			return;
		}

		// update by zhoujun 如果是木瓜移动的数据，需要回调url
		if (boutiqueApp.info.cback == BoutiqueApp.BoutiqueAppInfo.NEED_TO_CBACK) {
			if (boutiqueApp.info.cbacktype == BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_ALL
					|| boutiqueApp.info.cbacktype == BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_DETAIL) {
				DownloadUtil.sendCBackUrl(boutiqueApp.info.cbackurl);
			}
		}
		// update by zhoujun 2012-08-30 end

		if (startType == START_TYPE_APPMANAGEMENT || startType == START_TYPE_APPRECOMMENDED
				|| startType == START_TYPE_WIDGET_APP || startType == START_TYPE_APPFUNC_SEARCH
				|| startType == START_TYPE_GO_SEARCH_WIDGET) {
			// 应用中心统计代码
			AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(context,
					AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);
			if (isNeedDetailCount) {
				AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(context,
						boutiqueApp.info.packname, Integer.parseInt(boutiqueApp.info.appid), 1);
			}

			AppRecommendedStatisticsUtil.getInstance().saveDetailsClick2MainTable(context,
					boutiqueApp.info.packname, Integer.parseInt(boutiqueApp.info.appid),
					boutiqueApp.typeid + "");
		}
		int acttype = boutiqueApp.acttype;
		if (acttype == BoutiqueApp.FEATURE_ACTTYPE_FTP) {
			AppsDetail
					.gotoDetailDirectly(context,
							Integer.parseInt(boutiqueApp.info.appid),
							boutiqueApp.info.packname, startType,
							boutiqueApp.typeid + "", boutiqueApp.name,
							boutiqueApp.info.grade, appids, pkgs, icons,
							showList, boutiqueApp.info.detailstyle, boutiqueApp);
		} else if (acttype == BoutiqueApp.FEATURE_ACTTYPE_MARKET) {
			String actvalue = boutiqueApp.actvalue;
			if (actvalue != null && !"".equals(actvalue)) {
				actvalue = actvalue.trim();
				// 统计代码
				if (startType == START_TYPE_APPMANAGEMENT || startType == START_TYPE_APPRECOMMENDED
						|| startType == START_TYPE_WIDGET_APP
						|| startType == START_TYPE_APPFUNC_SEARCH
						|| startType == START_TYPE_GO_SEARCH_WIDGET) {
					AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(context,
							boutiqueApp.info.packname, Integer.parseInt(boutiqueApp.info.appid),
							"" + boutiqueApp.typeid, 1,
							String.valueOf(DownloadUtil.getSerTime(context)));
				}

				// TODO 木瓜sdk相应代码
				String afCBackUrl = boutiqueApp.info.iAfCbackurl;
				if (afCBackUrl != null && !afCBackUrl.equals("")) {
					// 如果木瓜sdk地址不为空，采用木瓜sdk跳转
					GoStoreOperatorUtil.gotoMarket(context, actvalue, afCBackUrl);
				} else {
					GoStoreOperatorUtil.gotoMarket(context, actvalue);
				}
			}
		} else if (acttype == BoutiqueApp.FEATURE_ACTTYPE_BROWSER) {
			String actvalue = boutiqueApp.actvalue;
			if (actvalue != null && !"".equals(actvalue)) {
				actvalue = actvalue.trim();

				// 统计代码
				if (startType == START_TYPE_APPMANAGEMENT || startType == START_TYPE_APPRECOMMENDED
						|| startType == START_TYPE_WIDGET_APP
						|| startType == START_TYPE_APPFUNC_SEARCH
						|| startType == START_TYPE_GO_SEARCH_WIDGET) {
					AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(context,
							boutiqueApp.info.packname, Integer.parseInt(boutiqueApp.info.appid),
							"" + boutiqueApp.typeid, 1,
							String.valueOf(DownloadUtil.getSerTime(context)));
				}

				GoStoreOperatorUtil.gotoBrowser(context, actvalue);
			}
		}

	}

	/**
	 * 根据详情数据的跳转类型跳转到详情页
	 * 
	 * @author xiedezhi
	 * @param context
	 * @param detailBean
	 *            详情数据
	 * @param categoryId
	 *            分类id
	 * @param startType
	 *            入口类型
	 * @param index
	 *            位置索引
	 * @param isNeedCount
	 *            是否需要统计,Widget进入详情点击下载时不需要再做统计
	 */
	public static void jumpToDetail(Context context, AppDetailInfoBean detailBean,
			String categoryId, int startType, int index, boolean isNeedCount) {
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		if (detailBean == null) {
			return;
		}

		// update by zhoujun 如果是木瓜移动的数据，需要回调url
		if (detailBean.cback == BoutiqueApp.BoutiqueAppInfo.NEED_TO_CBACK) {
			// 详情200跳电子市场的，不需要判断cbacktype
			DownloadUtil.sendCBackUrl(detailBean.cbackurl);
		}
		// update by zhoujun 2012-08-30 end

		if (isNeedCount) {
			if (startType == START_TYPE_APPMANAGEMENT
					|| startType == START_TYPE_APPRECOMMENDED || startType == START_TYPE_WIDGET_APP
					|| startType == START_TYPE_APPFUNC_SEARCH
					|| startType == START_TYPE_GO_SEARCH_WIDGET) {
				// 应用中心统计代码
				AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(context,
						AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);
				AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(context,
						detailBean.mPkgName, detailBean.mAppId, 1);
				AppRecommendedStatisticsUtil.getInstance().saveDetailsClick2MainTable(context,
						detailBean.mPkgName, detailBean.mAppId, categoryId);
			}
		}

		int downloadType = detailBean.mDownloadType;
		if (downloadType == AppsThemeDetailActivity.DETAIL_DOWNLOAD_TYPE_FTP) {
			// 详情界面下载跳转如果为FTP的话是直接下载APK的，所以这种情况不会在这里发生
		} else if (downloadType == AppsThemeDetailActivity.DETAIL_DOWNLOAD_TYPE_MARKET) {
			// 跳转到电子市场页面
			String downloadUrl = detailBean.mDownloadUrl;
			if (downloadUrl != null && !"".equals(downloadUrl)) {
				downloadUrl = downloadUrl.trim();
			}

			if (startType == START_TYPE_APPMANAGEMENT
					|| startType == START_TYPE_APPRECOMMENDED
					|| startType == START_TYPE_WIDGET_APP
					|| startType == START_TYPE_APPFUNC_SEARCH
					|| startType == START_TYPE_GO_SEARCH_WIDGET) {
				// 统计代码 - 添加统计时间
				AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(context,
						detailBean.mPkgName, detailBean.mAppId, categoryId, 1,
						String.valueOf(DownloadUtil.getSerTime(context)));
			}
			// TODO 木瓜sdk相应代码
			String afCBackUrl = detailBean.mAfCbackUrl;
			if (afCBackUrl != null && !afCBackUrl.equals("")) {
				// 如果木瓜sdk地址不为空，采用木瓜sdk跳转
				GoStoreOperatorUtil.gotoMarket(context, downloadUrl, afCBackUrl);
			} else {
				// 跳转到电子市场页面
				GoStoreOperatorUtil.gotoMarket(context, downloadUrl);
			}
		} else if (downloadType == AppsThemeDetailActivity.DETAIL_DOWNLOAD_TYPE_WEB) {

			// 统计代码
			// 3：电子市场web版页面
			if (startType == START_TYPE_APPMANAGEMENT
					|| startType == START_TYPE_APPRECOMMENDED
					|| startType == START_TYPE_WIDGET_APP
					|| startType == START_TYPE_APPFUNC_SEARCH
					|| startType == START_TYPE_GO_SEARCH_WIDGET) {
				// 统计代码 - 添加统计时间
				AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(context,
						detailBean.mPkgName, detailBean.mAppId, categoryId, 1,
						String.valueOf(DownloadUtil.getSerTime(context)));
			}

			GoStoreOperatorUtil.gotoBrowser(context, detailBean.mDownloadUrl);
		}
	}

	/**
	 * <br>功能简述: 通过Item Id直接跳转详情界面的方法
	 * <br>功能详细描述:
	 * <br>注意: 工具类内部使用
	 * @param context
	 * @param appId  标识进入GO精品应用的ID
	 * @param pkgName 包名
	 * @param startType 入口类型(1 从应用更新，2从应用推荐)
	 * @param categoryID 推荐分类ID
	 * @param appName 应用名
	 * @param grade 应用评分
	 * @param detailStyle 详情风格
	 */
	private static void gotoDetailDirectly(Context context, int appId, String pkgName,
			int startType, String categoryID, String appName, int grade, int detailStyle, Serializable bean) {
		if (context != null) {
			Intent intent = new Intent();
			intent.setClass(context, AppsThemeDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(AppsThemeDetailActivity.START_GOSTORE_TYPE, startType);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME, pkgName);
			if (startType == START_TYPE_APPRECOMMENDED || startType == START_TYPE_WIDGET_APP
					|| startType == START_TYPE_APPFUNC_SEARCH
					|| startType == START_TYPE_GO_SEARCH_WIDGET) {
				intent.putExtra(AppsThemeDetailActivity.START_RECOMMENDED_CATEGORYID, categoryID);
			}
			if (appId != Integer.MIN_VALUE) {
				intent.putExtra(GoStorePublicDefine.APP_ID_KEY, appId);
				intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, appId);
			}
			intent.putExtra(AppsThemeDetailActivity.KEY_APP_NAME, appName);
			intent.putExtra(AppsThemeDetailActivity.KEY_DETAIL_GRADE, grade);
			intent.putExtra(AppsThemeDetailActivity.DETAIL_STYLE, detailStyle);
			intent.putExtra(AppsThemeDetailActivity.KEY_DATA_BEAN, bean);
			context.startActivity(intent);
		}
	}
	
	/**
	 * <br>功能简述:直接跳转到应用中心主题详情
	 * <br>功能详细描述:
	 * <br>注意:工具类内部使用
	 * @param context
	 * @param appId
	 * @param pkgName
	 * @param startType
	 * @param categoryID
	 * @param appName
	 * @param grade
	 * @param appids 不需要显示底部icon行时填null
	 * @param pkgs 不需要显示底部icon行时填null
	 * @param icons 不需要显示底部icon行时填null
	 * @param showList 是否需要显示底部icon行
	 */
	private static void gotoDetailDirectly(Context context, int appId, String pkgName,
			int startType, String categoryID, String appName, int grade, ArrayList<String> appids,
			ArrayList<String> pkgs, ArrayList<String> icons, int showList, int detailStyle, Serializable bean) {

		if (context != null) {
			Intent intent = new Intent();
			intent.setClass(context, AppsThemeDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(AppsThemeDetailActivity.START_GOSTORE_TYPE, startType);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME, pkgName);
			if (startType == START_TYPE_APPRECOMMENDED || startType == START_TYPE_WIDGET_APP
					|| startType == START_TYPE_APPFUNC_SEARCH
					|| startType == START_TYPE_GO_SEARCH_WIDGET) {
				intent.putExtra(AppsThemeDetailActivity.START_RECOMMENDED_CATEGORYID, categoryID);
			}
			if (appId != Integer.MIN_VALUE) {
				intent.putExtra(GoStorePublicDefine.APP_ID_KEY, appId);
				intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, appId);
			}
			intent.putExtra(AppsThemeDetailActivity.KEY_APP_NAME, appName);
			intent.putExtra(AppsThemeDetailActivity.KEY_DETAIL_GRADE, grade);
			intent.putExtra(AppsThemeDetailActivity.ID_LIST, appids);
			intent.putExtra(AppsThemeDetailActivity.PKG_LIST, pkgs);
			intent.putExtra(AppsThemeDetailActivity.ICON_LIST, icons);
			intent.putExtra(AppsThemeDetailActivity.SHOW_LIST, showList);
			intent.putExtra(AppsThemeDetailActivity.DETAIL_STYLE, detailStyle);
			intent.putExtra(AppsThemeDetailActivity.KEY_DATA_BEAN, bean);
			context.startActivity(intent);
		}
	}

	/**
	 * <br>功能简述:用包名直接跳转详情
	 * <br>功能详细描述:
	 * <br>注意: 由桌面直接跳入应用中心详情，有入口统计，其他跳转请勿使用
	 * @param context
	 * @param startType
	 * @param pkgName
	 */
	public static void gotoDetailDirectly(Context context, int startType, String pkgName) {
		if (context != null) {
			Intent intent = new Intent();
			intent.setClass(context, AppsThemeDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, Integer.MIN_VALUE);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME, pkgName);
			intent.putExtra(AppsThemeDetailActivity.START_GOSTORE_TYPE, startType);
			intent.putExtra(AppsThemeDetailActivity.DETAIL_STYLE, Integer.MIN_VALUE);
			context.startActivity(intent);
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(context, 
					AppRecommendedStatisticsUtil.ENTRY_TYPE_LAUNCHER_CENTER);
		}
	}

	/**
	 * <br>功能简述:用id直接跳转详情
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param startType
	 * @param pkgName
	 */
	public static void gotoDetailDirectly(Context context, int startType, int id) {
		if (context != null) {
			Intent intent = new Intent();
			intent.setClass(context, AppsThemeDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, id);
			intent.putExtra(AppsThemeDetailActivity.START_GOSTORE_TYPE, startType);
			intent.putExtra(AppsThemeDetailActivity.DETAIL_STYLE, Integer.MIN_VALUE);
			context.startActivity(intent);
		}
	}


	/**
	 * <br>功能简述:用id和包名直接跳详情
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param startType
	 * @param appId
	 * @param pkgName
	 */
	public static void gotoDetailDirectly(Context context, int startType, int appId,
			String pkgName) {
		if (context != null) {
			Intent intent = new Intent();
			intent.setClass(context, AppsThemeDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, appId);
			intent.putExtra(AppsThemeDetailActivity.START_GOSTORE_TYPE, startType);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME, pkgName);
			intent.putExtra(AppsThemeDetailActivity.DETAIL_STYLE, Integer.MIN_VALUE);
			context.startActivity(intent);
		}
	}
	
	/**
	 * <br>功能简述:用id和包名直接跳详情
	 * <br>功能详细描述:
	 * <br>注意:主要是由应用更新列表和忽略更新列表跳转使用
	 * @param context
	 * @param startType
	 * @param appId
	 * @param pkgName
	 * @param bean
	 */
	public static void gotoDetailDirectly(Context context, int startType, int appId, String pkgName, Serializable bean) {
		if (context != null) {
			Intent intent = new Intent();
			intent.setClass(context, AppsThemeDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, appId);
			intent.putExtra(AppsThemeDetailActivity.START_GOSTORE_TYPE, startType);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME, pkgName);
			intent.putExtra(AppsThemeDetailActivity.DETAIL_STYLE, 0); // 将详情类型置为普通类型
			intent.putExtra(AppsThemeDetailActivity.KEY_DATA_BEAN, bean);
			context.startActivity(intent);
		}
	}

	/**
	 * <br>功能简述:应用中心详情内跳转到新详情
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param recommApp
	 * @param categoryId
	 * @param startType 入口，用于区分游戏中心，应用中心
	 * @param index 位置索引值，由1开始
	 * @param isNeedDetailCount
	 * @param showOrNot
	 */
	public static void jumpToDetailNew(Context context, BoutiqueApp recommApp,
			String categoryId, int startType, int index, boolean isNeedDetailCount, int showOrNot) {
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		if (recommApp == null) {
			return;
		}
		// update by zhoujun 如果是木瓜移动的数据，需要回调url
		if (recommApp.info.cback == BoutiqueApp.BoutiqueAppInfo.NEED_TO_CBACK) {
			if (recommApp.info.cbacktype == BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_ALL
					|| recommApp.info.cbacktype == BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_DETAIL) {
				DownloadUtil.sendCBackUrl(recommApp.info.cbackurl);
			}
		}
		// update by zhoujun 2012-08-30 end

		if (startType == START_TYPE_APPMANAGEMENT || startType == START_TYPE_APPRECOMMENDED
				|| startType == START_TYPE_WIDGET_APP || startType == START_TYPE_APPFUNC_SEARCH
				|| startType == START_TYPE_GO_SEARCH_WIDGET) {
			// 应用中心
			// 统计代码
			AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(context,
					AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);
			if (isNeedDetailCount) {
				AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(context,
						recommApp.info.packname, Integer.parseInt(recommApp.info.appid), 1);
			}

			AppRecommendedStatisticsUtil.getInstance().saveDetailsClick2MainTable(context,
					recommApp.info.packname, Integer.parseInt(recommApp.info.appid), categoryId);
		}

		int type = recommApp.info.detailtype;
		if (type == BoutiqueApp.DETAIL_TYPE_FTP) {
			gotoDetailDirectlyNew(context, Integer.parseInt(recommApp.info.appid),
					recommApp.info.packname, startType, categoryId, recommApp.info.name,
					recommApp.info.grade, showOrNot, recommApp.info.detailstyle, recommApp);
		} else {
			String detailUrl = recommApp.info.detailurl;
			if (detailUrl != null && !"".equals(detailUrl)) {
				detailUrl = detailUrl.trim();
				if (type == BoutiqueApp.DETAIL_TYPE_MARKET) {

					// 统计代码
					if (startType == START_TYPE_APPMANAGEMENT
							|| startType == START_TYPE_APPRECOMMENDED
							|| startType == START_TYPE_WIDGET_APP
							|| startType == START_TYPE_APPFUNC_SEARCH
							|| startType == START_TYPE_GO_SEARCH_WIDGET) {
						AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(context,
								recommApp.info.packname, Integer.parseInt(recommApp.info.appid),
								categoryId, 1, String.valueOf(DownloadUtil.getSerTime(context)));
					}

					// TODO 木瓜sdk相应代码
					String afCBackUrl = recommApp.info.iAfCbackurl;
					if (afCBackUrl != null && !afCBackUrl.equals("")) {
						// 如果木瓜sdk地址不为空，采用木瓜sdk跳转
						GoStoreOperatorUtil.gotoMarket(context, detailUrl, afCBackUrl);
					} else {
						// 跳转到电子市场页面
						GoStoreOperatorUtil.gotoMarket(context, detailUrl);
					}
				} else if (type == BoutiqueApp.DETAIL_TYPE_WEB) {

					// 统计代码
					if (startType == START_TYPE_APPMANAGEMENT
							|| startType == START_TYPE_APPRECOMMENDED
							|| startType == START_TYPE_WIDGET_APP) {
						AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(context,
								recommApp.info.packname, Integer.parseInt(recommApp.info.appid),
								categoryId, 1, String.valueOf(DownloadUtil.getSerTime(context)));
					}

					// 3：电子市场web版页面
					GoStoreOperatorUtil.gotoBrowser(context, detailUrl);
				}
			}
		}
	}

	/**
	 * 应用中心普通详情内直接跳新详情
	 * 
	 * @param context
	 * @param itemId
	 * @param appId
	 *            标识进入GO精品应用的ID
	 * @param downloadStatus
	 *            标识进入GO精品应用的下载状态
	 * @param startType
	 *            入口类型(1 从应用更新，2从应用推荐)
	 * @param categoryID
	 *            推荐分类ID
	 * @param grage
	 *            应用评分
	 * @param bean
	 * 			  需要查看详情的应用单元封装bean，暂时是BoutiqueApp或者RecommmendedApp或者AppsBean
	 */
	public static void gotoDetailDirectlyNew(Context context, int appId,
			String pkgName, int startType, String categoryID, String appName,
			int grade, int showOrNot, int detailStyle, Serializable bean) {
		if (context != null) {
			Intent intent = new Intent();
			intent.setClass(context, AppsThemeDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(AppsThemeDetailActivity.START_GOSTORE_TYPE, startType);
			intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_PACKAGE_NAME, pkgName);
			intent.putExtra(AppsThemeDetailActivity.RECOMM_APP_SHOW_OR_NOT, showOrNot);

			if (startType == START_TYPE_APPRECOMMENDED || startType == START_TYPE_WIDGET_APP
					|| startType == START_TYPE_APPFUNC_SEARCH
					|| startType == START_TYPE_GO_SEARCH_WIDGET) {
				intent.putExtra(AppsThemeDetailActivity.START_RECOMMENDED_CATEGORYID, categoryID);
			}
			if (appId != Integer.MIN_VALUE) {
				intent.putExtra(GoStorePublicDefine.APP_ID_KEY, appId);
				intent.putExtra(AppsThemeDetailActivity.DOWNLOADING_APP_ID, appId);
			}
			intent.putExtra(AppsThemeDetailActivity.KEY_APP_NAME, appName);
			intent.putExtra(AppsThemeDetailActivity.KEY_DETAIL_GRADE, grade);
			intent.putExtra(AppsThemeDetailActivity.DETAIL_STYLE, detailStyle);
			intent.putExtra(AppsThemeDetailActivity.KEY_DATA_BEAN, bean);
			context.startActivity(intent);
		}
	}

	/**
	 * <br>功能简述:进入壁纸详情
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param startType
	 * @param title
	 * @param appId
	 * @param appIds
	 * @param pics
	 * @param icons
	 * @param downloadUrls
	 */
	public static void gotoWallPaperDirectly(Context context, int startType, String title, String appId,
			ArrayList<String> appIds, ArrayList<String> pics, ArrayList<String> icons, ArrayList<String> downloadUrls) {
		if (context != null) {
			Intent intent = new Intent();
			intent.setClass(context, WallpaperDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(WallpaperDetailActivity.APP_ID, appId);
			intent.putExtra(WallpaperDetailActivity.APP_ID_LIST, appIds);
			intent.putExtra(WallpaperDetailActivity.APP_IMG_URL_LIST, pics);
			intent.putExtra(WallpaperDetailActivity.APP_ICON_URL_LIST, icons);
			intent.putExtra(WallpaperDetailActivity.TITLE, title);
			intent.putExtra(WallpaperDetailActivity.APP_URL_LIST, downloadUrls);
			context.startActivity(intent);
		}
	}
}
