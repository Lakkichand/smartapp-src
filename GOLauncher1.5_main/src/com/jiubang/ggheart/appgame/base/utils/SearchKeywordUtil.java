package com.jiubang.ggheart.appgame.base.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.gau.utils.cache.utils.CacheUtil;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.appcenter.help.AppsNetConstant;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.HotSearchKeyword;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.AppsSearchDownload.SearchDataHandler;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;

/**
 * 
 * 应用游戏中心，搜索关键字工具类
 * 
 * @author xiedezhi
 * @date [2012-9-13]
 */
public class SearchKeywordUtil {

	/**
	 * 保存的最大搜索记录数
	 */
	public static final int SEARCH_HISTORY_MAX = 200;

	/**
	 * 应用中心搜索历史记录缓存key值
	 */
	public static final String KEY_APP_SEARCH_HISTORY = "key_app_search_history";

	/**
	 * 应用中心热门搜索关键字缓存key值
	 */
	public static final String KEY_APP_HOT_SEARCH_KEYWORD = "key_app_hot_search_keyword";

	/**
	 * 获取请求搜索关键字的url
	 */
	private static String getKeywordRequestUrl(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAppCenterHost(context)
					+ AppsNetConstant.APP_CENTER_SEARCH_KEYWORD_PATH
					+ DownloadUtil.sRandom.nextLong();
		}
		return url;
	}

	/**
	 * 获取请求热门搜索关键字的url
	 */
	private static String getHotKeywordRequestUrl(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAppCenterHost(context)
					+ AppsNetConstant.APP_CENTER_HOT_SEARCH_KEYWORD_PATH
					+ DownloadUtil.sRandom.nextLong();
		}
		return url;
	}

	/**
	 * 从网络获取搜索关键字
	 * 
	 * @param key
	 *            原始关键字
	 * @param ty
	 *            搜索类别 0：全部 1：应用 2:游戏
	 * @param pageid
	 *            请求的页码 首次请求传1
	 * @param handler
	 *            数据处理者
	 */
	public static void getSearchKeyword(Context context, final String key, int ty,
			int pageid, final SearchDataHandler handler) {
		if (handler == null) {
			return;
		}
		if (context == null) {
			handler.handleData(null);
			return;
		}
		String url = getKeywordRequestUrl(context);
		if (url == null) {
			handler.handleData(null);
			return;
		}
		JSONObject postdata = new JSONObject();
		JSONObject phead = RecommAppsUtils.createHttpHeader(context,
				AppsNetConstant.CLASSIFICATION_INFO_PVERSION);
		try {
			postdata.put("phead", phead);
			postdata.put("key", key);
			postdata.put("ty", ty);
			postdata.put("pageid", pageid);
		} catch (JSONException e) {
			e.printStackTrace();
			handler.handleData(null);
			return;
		}
		THttpRequest request = null;
		try {
			request = new THttpRequest(url, postdata.toString().getBytes(), new IConnectListener() {

				@Override
				public void onStart(THttpRequest arg0) {
				}

				@Override
				public void onFinish(THttpRequest arg0, IResponse response) {
					if (response != null && response.getResponse() != null
							&& (response.getResponse() instanceof JSONObject)) {
						JSONObject json = (JSONObject) response.getResponse();
						try {
							JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);
							int status = result.getInt(MessageListBean.TAG_STATUS);
							if (status == ConstValue.STATTUS_OK) {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("key", key);
								map.put("totalnum", json.optInt("totalnum", -1));
								map.put("pages", json.optInt("pages", -1));
								map.put("pageid", json.optInt("pageid", -1));
								map.put("keysearchid", json.optInt("keysearchid", -1));
								JSONArray keys = json.optJSONArray("keys");
								if (keys != null) {
									List<String> keylist = new ArrayList<String>();
									int len = keys.length();
									for (int i = 0; i < len; i++) {
										String key = keys.get(i).toString();
										keylist.add(key);
									}
									map.put("keys", keylist);
								}
								handler.handleData(map);
								return;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						handler.handleData(null);
					} else {
						handler.handleData(null);
					}
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
					handler.handleData(null);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			handler.handleData(null);
			return;
		}
		if (request != null) {
			// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
			request.setRequestPriority(Thread.MAX_PRIORITY);
			request.setOperator(new AppJsonOperator());
			AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(context);
			httpAdapter.addTask(request, true);
		}
	}

	/**
	 * 从本地读取历史搜索记录
	 * 
	 * @param context
	 * @param prefix
	 *            需要读取的记录的前缀，传null表示读取所有的搜索记录
	 * @return 历史搜索记录
	 */
	public static List<String> getSearchHistory(Context context, String prefix) {
		if (context == null) {
			return null;
		}
		// TODO:LIGUOLIANG 修改缓存管理方式
		String cacheKey = KEY_APP_SEARCH_HISTORY;
		AppCacheManager acm = AppCacheManager.getInstance();
		byte[] cacheData = acm.loadCache(cacheKey);
		if (cacheData == null) {
			return null;
		}
		String str = CacheUtil.byteArrayToString(cacheData);
		if (str == null) {
			return null;
		}
		try {
			JSONArray array = new JSONArray(str);
			int len = array.length();
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < len; i++) {
				String key = array.getString(i);
				// 如果需要读取某个前缀的历史记录
				if (prefix != null && (!prefix.equals(""))) {
					if (key.startsWith(prefix)) {
						list.add(key);
					}
				} else {
					list.add(key);
				}
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 保存搜索记录到SD卡，新记录会保存在旧记录前面;如果之前有相同的记录，会把之前相同的记录去掉
	 */
	public static void saveSearchHistory(Context context, String key) {
		if (context == null || key == null || key.trim().equals("")) {
			return;
		}

		// TODO:LIGUOLIANG 修改缓存管理方式
		String cacheKey = KEY_APP_SEARCH_HISTORY;
		AppCacheManager acm = AppCacheManager.getInstance();

		byte[] cacheData = acm.loadCache(cacheKey);
		if (cacheData == null) {
			JSONArray array = new JSONArray();
			array.put(key);
			acm.saveCache(cacheKey, array.toString().getBytes());
			return;
		}
		String str = CacheUtil.byteArrayToString(cacheData);
		if (str == null) {
			JSONArray array = new JSONArray();
			array.put(key);
			acm.saveCache(cacheKey, array.toString().getBytes());
			return;
		}

		// 如果之前的缓存不为空，需要先将之前的值读进来，然后再添加进去
		JSONArray array = null;
		try {
			array = new JSONArray(str);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (array == null) {
			array = new JSONArray();
		}
		try {
			int len = array.length();
			{
				JSONArray newArray = new JSONArray();
				newArray.put(key);
				for (int i = 0; i < len; i++) {
					String oldKey = array.getString(i);
					if (oldKey.equals(key)) {
						continue;
					}
					newArray.put(oldKey);
				}
				array = newArray;
			}
			len = array.length();
			if (len > SEARCH_HISTORY_MAX) {
				JSONArray newArray = new JSONArray();
				for (int i = 0; i < SEARCH_HISTORY_MAX; i++) {
					newArray.put(array.get(i));
				}
				array = newArray;
			}
			acm.saveCache(cacheKey, array.toString().getBytes());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 清除历史搜索记录
	 */
	public static void cleanSearchHistory()	{
		String cacheKey = KEY_APP_SEARCH_HISTORY;
		AppCacheManager acm = AppCacheManager.getInstance();
		acm.clearCache(cacheKey);
	}

	/**
	 * 从网络获取搜索热门关键字，并保存在本地
	 * 
	 * @param context
	 * @param ty
	 *            搜索类别 0：全部 1：应用 2:游戏
	 * @param handler
	 *            网络数据处理者
	 */
	public static void refreshHotSearchKeywords(Context context, int ty,
			final DataHandler handler) {
		if (context == null) {
			return;
		}
		String url = getHotKeywordRequestUrl(context);
		if (url == null) {
			return;
		}
		JSONObject postdata = new JSONObject();
		JSONObject phead = RecommAppsUtils.createHttpHeader(context,
				AppsNetConstant.CLASSIFICATION_INFO_PVERSION);
		try {
			postdata.put("phead", phead);
			postdata.put("ty", ty);
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		THttpRequest request = null;
		try {
			request = new THttpRequest(url, postdata.toString().getBytes(), new IConnectListener() {

				@Override
				public void onStart(THttpRequest arg0) {
				}

				@Override
				public void onFinish(THttpRequest arg0, IResponse response) {
					if (response != null && response.getResponse() != null
							&& (response.getResponse() instanceof JSONObject)) {
						JSONObject json = (JSONObject) response.getResponse();
						try {
							JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);
							int status = result.getInt(MessageListBean.TAG_STATUS);
							if (status == ConstValue.STATTUS_OK) {

								// TODO:LIGUOLIANG 修改缓存管理方式
								AppCacheManager acm = AppCacheManager.getInstance();
								String cacheKey = KEY_APP_HOT_SEARCH_KEYWORD;
								acm.saveCache(cacheKey, json.toString().getBytes());
								if (handler != null) {
									handler.handle(json);
								}
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (request != null) {
			request.setRequestPriority(Thread.MIN_PRIORITY);
			request.setOperator(new AppJsonOperator());
			AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(context);
			httpAdapter.addTask(request, true);
		}
	}

	/**
	 * 从本地读取热门搜索关键字
	 */
	public static List<HotSearchKeyword> getHotSearchKeywords(Context context) {
		if (context == null) {
			return null;
		}
		// TODO:LIGUOLIANG 修改缓存管理方式
		AppCacheManager acm = AppCacheManager.getInstance();
		String cacheKey = KEY_APP_HOT_SEARCH_KEYWORD;
		byte[] cacheData = acm.loadCache(cacheKey);
		if (cacheData == null) {
			return null;
		}
		String str = CacheUtil.byteArrayToString(cacheData);
		if (str == null) {
			return null;
		}
		try {
			JSONObject json = new JSONObject(str);
			List<HotSearchKeyword> list = parseHotSearchKeywords(json);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析json数据
	 */
	public static List<HotSearchKeyword> parseHotSearchKeywords(JSONObject json) {
		if (json == null) {
			return null;
		}
		try {
			List<HotSearchKeyword> list = new ArrayList<HotSearchKeyword>();
			int totalnum = json.optInt("totalnum", 0);
			JSONArray array = json.getJSONArray("rankkeys");
			for (int i = 0; i < array.length(); i++) {
				JSONObject jBean = array.getJSONObject(i);
				HotSearchKeyword bean = new HotSearchKeyword();
				bean.name = jBean.optString("name", "");
				bean.state = jBean.optInt("status", 0);
				bean.sicon = jBean.optString("sicon", "");
				list.add(bean);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 数据处理者
	 * 
	 * @author xiedezhi
	 * @date [2012-10-24]
	 */
	public static interface DataHandler {
		// TODO:XIEDEZHI 所有的数据处理类统一起来
		public void handle(Object object);
	}

}
