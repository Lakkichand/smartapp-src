/**
 * 
 */
package com.jiubang.ggheart.appgame.base.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;

import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.appcenter.help.AppsNetConstant;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppFileUtil;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;

/**
 * @author liguoliang
 * 
 */
public class AppsDetailDownload {

	public static final String KEY_APPCENTER_DETAIL_MARK = "key_appcenter_detail_mark";
	public static final String KEY_NETLOG_MARK = "key_netlog_mark";
	/**
	 * 详情页mark值：每个详情单独保存mark值,保存在对应的缓存文件中
	 */
	public static final String KEY_DETAIL_MARK = "key_detail_mark";
	public static final String KEY_SEVERTIME_MARK = "key_severtime_mark";
	public static final String KEY_LOCALTIME_MARK = "key_localtime_mark";

	public static final String APPS_DETAIL_CACHE_PATH = Environment.getExternalStorageDirectory()
			.getPath() + "/GoStore/appdetail/";

	/**
	 * 生成向服务器传递的参数信息
	 */
	public static JSONObject getPostJSON(Context context, int appId, String pkgName) {
		JSONObject request = new JSONObject();
		JSONObject phead = RecommAppsUtils.createHttpHeader(context, AppsNetConstant.CLASSIFICATION_INFO_PVERSION);
		String localFilePath = APPS_DETAIL_CACHE_PATH + pkgName + "_" + appId;
		int must = 1;
		String mark = "";
		if (FileUtil.isFileExist(localFilePath)) {
			must = 0;
			// 从缓存文件中读取mark值
			String cacheData = RecommAppFileUtil.readFileToString(localFilePath);
			try {
				JSONObject json = new JSONObject(cacheData);
				mark = json.optString(KEY_DETAIL_MARK, "");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		try {
			request.put("phead", phead);
			request.put("appid", appId);
			request.put("pkgname", pkgName);
			request.put("must", must);
			request.put("mark", mark);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return request;
	}

	/**
	 * 根据服务器下发的JSON数据解析出应用详情数据，并把数据保存在本地
	 */
	public static AppDetailInfoBean getDetailData(JSONObject json, Context context, int appId,
			String pkgName) {
		try {
			String localFilePath = APPS_DETAIL_CACHE_PATH + pkgName + "_" + appId;
			JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);
			int status = result.getInt(MessageListBean.TAG_STATUS);
			if (status == ConstValue.STATTUS_OK) {
				int hasnew = json.getInt("hasnew");
				if (hasnew == 1) {
					String resultMark = json.getString("mark");
					JSONObject appInfo = json.getJSONObject("appinfo");
					// 缓存mark值
					appInfo.put(KEY_DETAIL_MARK, resultMark);
					// 缓存数据信息
					FileUtil.saveByteToSDFile(appInfo.toString().getBytes(), localFilePath);
					return AppsDetailParser.parseDetailInfo(appInfo);
				} else {
					if (FileUtil.isFileExist(localFilePath)) {
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
	public static String getRequestUrlByType(Context context) {
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
	public static String getAlternativeUrl(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAlternativeAppCenterHost(context)
					+ AppsNetConstant.APP_CENTER_DETAIL_PATH + DownloadUtil.sRandom.nextLong();
		}
		return url;
	}

	public static AppDetailInfoBean getCacheData(Context context, int appId, String pkgName) {
		String localFilePath = APPS_DETAIL_CACHE_PATH + pkgName + "_" + appId;
		String localStr = RecommAppFileUtil.readFileToString(localFilePath);
		try {
			JSONObject appInfo = new JSONObject(localStr);
			return AppsDetailParser.parseDetailInfo(appInfo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
