package com.jiubang.ggheart.appgame.base.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gau.utils.cache.utils.CacheUtil;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.appcenter.help.AppsNetConstant;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.utils.AppGameInstalledFilter;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemePurchaseManager;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;

/**
 * 应用中心/游戏中心，根据分类id从服务器下载数据，并把数据封装在DataBean中
 * 
 * @author xiedezhi
 * 
 */
public class ClassificationDataDownload {

	public static final String MODULE_CLASSIFICATION_PREF = "Classification";

	public static final String CLASSFICATION_APP_PREF = "classification_app_";

	/**
	 * 根据分类id和页码得到每个分类id对应的must值
	 * 
	 * @param typeIds
	 * @param pageid
	 * @return 每个分类id对应的must值
	 */
	public static int[] getMusts(int[] typeIds, int pageid) {
		if (typeIds == null || typeIds.length <= 0) {
			return null;
		}
		int[] musts = new int[typeIds.length];
		AppCacheManager acm = AppCacheManager.getInstance();
		for (int i = 0; i < typeIds.length; i++) {
			// TODO:LIGUOLIANG 修改缓存管理方式
			String key = buildClassificationKey(typeIds[i], pageid);
			musts[i] = acm.isCacheExist(key) ? 0 : 1;
		}
		return musts;
	}

	/**
	 * 根据分类id取出对应的mark值
	 * 
	 * @param typeIds
	 * @return 分类id对应的mark列表
	 */
	public static String[] getMarks(Context context, int[] typeIds) {
		if (typeIds == null || typeIds.length <= 0) {
			return null;
		}
		String[] marks = new String[typeIds.length];
		for (int i = 0; i < typeIds.length; i++) {
			marks[i] = DownloadUtil.getMark(context, getMarkKey() + typeIds[i]);
		}
		return marks;
	}

	/**
	 * 获取应用中心/游戏中心保存Mark值的key的前缀
	 */
	public static String getMarkKey() {
		String key = AppsNetConstant.APP_CENTER_CLASSIFICATION_MARK_PREFIX;
		return key;
	}

	/**
	 * 根据入口值返回请求数据的url
	 */
	public static String getUrl(Context context) {
		String host = DownloadUtil.getAppCenterHost(context);
		String url = host + AppsNetConstant.APP_CENTER_CLASSIFICATION_PATH
				+ DownloadUtil.sRandom.nextLong();
		return url;
	}

	/**
	 * 根据入口值返回请求数据的备选URL
	 */
	public static String getAlternativeUrl(Context context) {
		String host = DownloadUtil.getAlternativeAppCenterHost(context);
		String url = host + AppsNetConstant.APP_CENTER_CLASSIFICATION_PATH
				+ DownloadUtil.sRandom.nextLong();
		return url;
	}

	/**
	 * 获取应用中心/游戏中心的协议版本号
	 * 
	 * @return
	 */
	public static String getVersion() {
		return AppsNetConstant.CLASSIFICATION_INFO_PVERSION;
	}

	/** <br>功能简述:根据分类id和页码构建该数据缓存的extra值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param typeId
	 * @param pageId
	 * @return
	 */
	public static String buildClassificationCacheExtra(int typeId, int pageId) {
		String extra = CLASSFICATION_APP_PREF + typeId + File.separator + pageId;
		return extra;
	}

	/** <br>功能简述:根据分类id构建该数据缓存的module值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param typeId
	 * @return
	 */
	public static String buildClassficationCacheModule(int typeId) {
		String module = CLASSFICATION_APP_PREF + typeId;
		return module;
	}

	/** <br>功能简述:应有游戏中心根据分类id和页码，返回该数据的key值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param typeId
	 * @param pageId
	 * @return
	 */
	public static String buildClassificationKey(int typeId, int pageId) {
		String module = buildClassficationCacheModule(typeId);
		String extra = buildClassificationCacheExtra(typeId, pageId);
		return AppCacheManager.getInstance().buildKey(module, extra);
	}

	/**
	 * 获取保存"phead"的key
	 */
	public static String getPHeadMarkKey() {
		String key = AppsNetConstant.APP_CENTER_PREVLOAD_PHEAD_MARK;
		return key;
	}

	/**
	 * 保存请求头中的phead信息，如果下次的请求与这次请求的phead不同，则需要连网取顶级tab栏数据
	 * 
	 * @param phead
	 */
	public static void savePheadMark(Context context, JSONObject postdata) {
		try {
			JSONObject phead = postdata.getJSONObject("phead");

			JSONObject data = new JSONObject();
			data.put("imsi", phead.optString("imsi", ""));
			data.put("hasmarket", phead.optInt("hasmarket", 0));
			data.put("lang", phead.optString("lang", ""));
			data.put("local", phead.optString("local", ""));
			data.put("channel", phead.optString("channel", ""));
			data.put("sdk", phead.optInt("sdk", 0));
			data.put("pversion", phead.optString("pversion", ""));
			data.put("sbuy", phead.optInt("sbuy", 0));
			data.put("vip",  phead.optInt("vip", 0));
			// 把正选和备选url也加进来,url发生改变时也要重新向服务器申请数据
			data.put("urlchina", AppsNetConstant.APP_CENTER_URL_CHINA);
			data.put("urlothers", AppsNetConstant.APP_CENTER_URL_OTHERS);
			
			int hashcode = data.toString().hashCode();
			// Log.e("XIEDEZHI", "savePheadMark = " + hashcode);

			PreferencesManager sharedPreferences = new PreferencesManager(
					context, IPreferencesIds.APP_MANAGER_RECOMMEND_PHEAD,
					Context.MODE_PRIVATE);
			sharedPreferences
					.putString(ClassificationDataDownload.getPHeadMarkKey(), ""
							+ hashcode);
			sharedPreferences.commit();
		} catch (Exception e) {
			e.printStackTrace();
			PreferencesManager sharedPreferences = new PreferencesManager(
					context, IPreferencesIds.APP_MANAGER_RECOMMEND_PHEAD,
					Context.MODE_PRIVATE);
			sharedPreferences.putString(
					ClassificationDataDownload.getPHeadMarkKey(), "");
			sharedPreferences.commit();
		}
	}
	
	/**
	 * 应用游戏中心，判断当前phead与上次phead是否相同，如果不同，则向服务器请求新的数据
	 * 
	 * @return 如果两次phead相同返回true，否则返回false
	 */
	public static boolean checkPHead(Context context) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				IPreferencesIds.APP_MANAGER_RECOMMEND_PHEAD,
				Context.MODE_PRIVATE);
		String prevPhead = sharedPreferences.getString(
				ClassificationDataDownload.getPHeadMarkKey(), "");

		JSONObject data = new JSONObject();
		try {
			data.put("imsi", RecommAppsUtils.getCnUser(context));
			data.put("hasmarket",
					GoStoreAppInforUtil.isExistGoogleMarket(context) ? 1 : 0);
			data.put("lang", RecommAppsUtils.language(context));
			data.put("local", RecommAppsUtils.local(context));
			data.put("channel", GoStorePhoneStateUtil.getUid(context));
			data.put("sdk", Build.VERSION.SDK_INT);
			data.put("pversion", ClassificationDataDownload.getVersion());
			data.put("sbuy", Integer.parseInt(GoStorePhoneStateUtil
					.isAppInSupported(context)));
			data.put("vip", ThemePurchaseManager.getCustomerLevel(context));
			// 把正选和备选url也加进来,url发生改变时也要重新向服务器申请数据
			data.put("urlchina", AppsNetConstant.APP_CENTER_URL_CHINA);
			data.put("urlothers", AppsNetConstant.APP_CENTER_URL_OTHERS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String newPhead = data.toString().hashCode() + "";
		if (prevPhead.equals(newPhead)) {
			return true;
		}
		return false;
	}

	/**
	 * 根据分类id和页码读取本地缓存的数据
	 *
	 * @param typeId
	 *            要读取的分类id
	 * @param pageId
	 *            要读取的页码
	 * @param localJson
	 * 			  本地的JSON数据，在getLocalSubTypeidList方法有可能已经把JSON数据读出来，这里就不需要再读，以节省时间。如果该参数为空，则需要重新读取本地JSON。
	 * @return 返回本地缓存的数据，如果没有或解析出错则返回null
	 */
	public static ClassificationDataBean getLocalData(int typeId, int pageId, JSONObject localJson) {
		try {
			JSONObject obj = localJson;
			if (obj == null) {
				// 数据保存键值
				String key = buildClassificationKey(typeId, pageId);
				// 读取本地缓存的tab栏数据
				byte[] cacheData = AppCacheManager.getInstance().loadCache(key);
				if (cacheData == null) {
					return null;
				}
				obj = CacheUtil.byteArrayToJson(cacheData);
			}
			// 解析数据
			ClassificationDataBean ret = ClassificationDataParser.parseDataBean(typeId, obj, null);
			if (ret == null) {
				Log.e("ClassificationDataDownload", "getLocalData id(" + typeId + ") == null");
			} else {
				if (ret.typeId != typeId) {
					Log.e("ClassificationDataDownload", "getLocalData  ret.typeId(" + ret.typeId
							+ ") != typeId(" + typeId + ")");
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 读取SD卡中应用列表信息的原始数据（字符串）
	 */
	public static String getLocalString(int typeId, int pageId) {
		// 数据保存键值
		String key = buildClassificationKey(typeId, pageId);
		// 读取本地缓存的tab栏数据
		byte[] cacheData = AppCacheManager.getInstance().loadCache(key);
		if (cacheData == null) {
			return null;
		}
		String ret = CacheUtil.byteArrayToString(cacheData);
		return ret;
	}

	/**
	 * 生成向服务器传递的参数信息
	 * @param itp id类型  0:与渠道区域相关的分类节点(默认)  1:与渠道区域无关的分类id  2:人工配置挂接用的虚拟id
	 */
	public static JSONObject getPostJson(Context context, int[] typeIds, int access,
			int pageid, int itp) {
		int[] musts = getMusts(typeIds, pageid);
		String[] marks = getMarks(context, typeIds);
		String version = getVersion();
		JSONObject postdata = DownloadUtil.getPostJson(context, version, musts, marks, typeIds,
				access, pageid, itp);
		return postdata;
	}

	/**
	 * 
	 * 把服务器下发的json数据解析成本地的分类数据列表，并把获取到的数据放到TabDataManager的数据缓存池中
	 * 
	 * @param json
	 *            服务器下发的json数据
	 * @param postdata
	 *            客户端向服务器传递的参数信息
	 * @param context
	 * @param typeIds
	 *            分类id列表,顶级tab栏传0
	 * @param pageid
	 *            获取的页码，页码从1开始，仅用在支持分页的数据类型，如专题推荐数据，没有分页的统一写1
	 * @param startIndex
	 *            请求的数据在列表中的开始下标
	 * @param savePhead
	 * 			  是否要保存phead值，用于应用游戏中心进入时是否启动预加载功能。非应用游戏中心请求数据传false，应用游戏中心请求数据传true
	 * @return 返回typeIds列表对应的分类数据
	 */
	public static List<ClassificationDataBean> getClassificationData(JSONObject json,
			JSONObject postdata, final Context context, int[] typeIds,
			int pageid, final int startIndex, boolean savePhead) {
		if (json == null) {
			return null;
		}
		Map<Integer, ClassificationDataBean> map = ClassificationDataParser.parseData(context,
				json, pageid);
		if (map == null) {
			return null;
		}
		// 缓存数据
		final List<ClassificationDataBean> list = new ArrayList<ClassificationDataBean>();
		list.addAll(map.values());
		List<Integer> ids = new ArrayList<Integer>();
		ids.addAll(map.keySet());
		TabDataManager.getInstance().cacheTabData(ids, AppGameInstalledFilter.filterDataBeanList(list));
		// -----------------统计START-----------------------//
		Thread saveIssued = new Thread("saveIssued") {
			public void run() {
				try {
					if (list != null && list.size() > 0) {
						List<String> packNameList = new ArrayList<String>();
						List<String> typeIdList = new ArrayList<String>();
						List<Integer> indexList = new ArrayList<Integer>();
						for (ClassificationDataBean bean : list) {
							if (bean != null) {
								if (bean.dataType == ClassificationDataBean.SPECIALSUBJECT_TYPE
										|| bean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE
										|| bean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE
										|| bean.dataType == ClassificationDataBean.FEATURE_TYPE
										|| bean.dataType == ClassificationDataBean.GRID_TYPE
										|| bean.dataType == ClassificationDataBean.WALLPAPER_GRID
										|| bean.dataType == ClassificationDataBean.COVER_FLOW
										|| bean.dataType == ClassificationDataBean.AD_BANNER
										|| bean.dataType == ClassificationDataBean.PRICE_ALERT) {
									if (bean.featureList != null && bean.featureList.size() > 0) {
										for (int i = 0; i < bean.featureList.size(); i++) {
											BoutiqueApp app = bean.featureList.get(i);
											packNameList.add(app.info.packname);
											typeIdList.add(String.valueOf(app.typeid));
											indexList.add(i + startIndex);
										}
									}
								}
							}
						}
						AppRecommendedStatisticsUtil.getInstance().saveAppIssueDataList(context,
								packNameList, typeIdList, indexList);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		saveIssued.setPriority(Thread.MIN_PRIORITY);
		saveIssued.start();
		// -----------------统计END-----------------------//
		if (list != null && list.size() > 0 && savePhead) {
			// 保存这次请求头phead信息，下次进来时判断当前phead与上次phead是否相同，如果不同，则向服务器请求新的数据
			savePheadMark(context, postdata);
		}
		List<ClassificationDataBean> ret = new ArrayList<ClassificationDataBean>();
		for (int id : typeIds) {
			if (!map.containsKey(id)) {
				Log.e("ClassificationDataDownload", "getClassificationData !map.containsKey(id = "
						+ id + ")");
			}
			ClassificationDataBean dataBean = map.get(id);
			// Log.e("XIEDEZHI", "getClassificationData id = " + id
			// + "  dataBean == " + dataBean);
			ret.add(dataBean);
		}
		return ret;
	}

}
