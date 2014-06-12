/*
 * 文 件 名:  AppDetailController.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liguoliang
 * 修改时间:  2012-10-10
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.appcenter.contorler;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.gau.utils.cache.CacheManager;
import com.gau.utils.cache.impl.FileCacheImpl;
import com.gau.utils.cache.utils.CacheUtil;
import com.jiubang.ggheart.appgame.appcenter.help.AppsNetConstant;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.data.AppsDetailParser;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liguoliang
 * @date  [2012-10-10]
 */
public class AppDetailController {

	public static final String KEY_APPCENTER_DETAIL_MARK = "key_appcenter_detail_mark";
	public static final String KEY_GAMEZONE_DETAIL_MARK = "key_gamezone_detail_mark";
	public static final String KEY_NETLOG_MARK = "key_netlog_mark";
	/**
	 * 详情页mark值：每个详情单独保存mark值,保存在对应的缓存文件中
	 */
	private static final String KEY_DETAIL_MARK = "key_detail_mark";

	public static final String KEY_SEVERTIME_MARK = "key_severtime_mark";
	public static final String KEY_LOCALTIME_MARK = "key_localtime_mark";	

	private CacheManager mCacheManager;

	private Context mContext;

	public AppDetailController(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		mCacheManager = new CacheManager(new FileCacheImpl(LauncherEnv.Path.APPS_DETAIL_CACHE_PATH));
		mContext = context;
	}

	/**
	 * 生成向服务器传递的参数信息
	 * 
	 * @param startType 标明是从哪个入口请求详情数据，从而调整请求头的clientId
	 */
	public JSONObject getPostJSON(Context context, int startType, int appId, String pkgName, int detailStyle) {
		JSONObject request = new JSONObject();
		
		// 重置clinetId值
		int clinetId = 1;
		if (startType == AppsDetail.START_TYPE_APPFUNC_SEARCH) {
			clinetId = 2;
		} else if (startType == AppsDetail.START_TYPE_GO_SEARCH_WIDGET) {
			clinetId = 3;
		}
		JSONObject phead = RecommAppsUtils.createHttpHeader(context,
				AppsNetConstant.CLASSIFICATION_INFO_PVERSION, clinetId);
		
		int must = 1;
		String mark = "";
		String detailCacheKey = pkgName + "_" + appId; // 包名 + "_" + id 作为键值
		if (mCacheManager.isCacheExist(detailCacheKey)) {
			byte[] cacheByteArray = mCacheManager.loadCache(detailCacheKey);
			JSONObject json = CacheUtil.byteArrayToJson(cacheByteArray);
			if (json != null) {
				must = 0;
				mark = json.optString(KEY_DETAIL_MARK, "");
			}
		}
		try {
			request.put("phead", phead);
			request.put("appid", appId);
			request.put("pkgname", pkgName);
			request.put("must", must);
			request.put("mark", mark);
			if (detailStyle == Integer.MIN_VALUE) {
				request.put("detailstyle", -1);
			} else {
				request.put("detailstyle", detailStyle);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return request;
	}

	/**
	 * 根据服务器下发的JSON数据解析出应用详情数据，并把数据保存在本地
	 */
	public AppDetailInfoBean getDetailData(JSONObject json, Context context, int appId,
			String pkgName) {
		try {
			JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);
			int status = result.getInt(MessageListBean.TAG_STATUS);
			String detailCacheKey = pkgName + "_" + appId; // 包名 + "_" + id 作为键值
			if (status == ConstValue.STATTUS_OK) {
				int hasnew = json.getInt("hasnew");
				if (hasnew == 1) {
					String resultMark = json.getString("mark");
					JSONObject appInfo = json.getJSONObject("appinfo");
					// 缓存mark值
					appInfo.put(KEY_DETAIL_MARK, resultMark);
					
					// 缓存数据信息					
					if (mCacheManager.isCacheExist(detailCacheKey)) {
						// 如果缓存已经存在则清除旧的缓存
						mCacheManager.clearCache(detailCacheKey);
					}
					mCacheManager.saveCache(detailCacheKey, CacheUtil.jsonToByteArray(appInfo));
					return AppsDetailParser.parseDetailInfo(appInfo);
				} else {
					if (mCacheManager.isCacheExist(detailCacheKey)) {
						return getCacheData(context, appId, pkgName);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 功能简述:通过启动类型来获取请求地址的方法 功能详细描述:主要是为了区分应用中心详情和游戏中心详情的请求URL 注意:
	 * 
	 * @param startType
	 * @return
	 */
	public String getRequestUrlByType(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAppCenterHost(context) + AppsNetConstant.APP_CENTER_DETAIL_PATH
					+ DownloadUtil.sRandom.nextLong();
		}
		return url;
	}

	/**
	 * 获取请求详情数据的备选地址
	 */
	public String getAlternativeUrl(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAlternativeAppCenterHost(context)
					+ AppsNetConstant.APP_CENTER_DETAIL_PATH + DownloadUtil.sRandom.nextLong();
		}
		return url;
	}

	public AppDetailInfoBean getCacheData(Context context, int appId, String pkgName) {
		String detailCacheKey = pkgName + "_" + appId; // 包名 + "_" + id 作为键值
		byte[] cacheData = mCacheManager.loadCache(detailCacheKey);
		if (cacheData == null) {
			return null;
		}
		JSONObject appInfo = CacheUtil.byteArrayToJson(cacheData);
		if (appInfo == null) {
			return null;
		}
		return AppsDetailParser.parseDetailInfo(appInfo);
	}
}
