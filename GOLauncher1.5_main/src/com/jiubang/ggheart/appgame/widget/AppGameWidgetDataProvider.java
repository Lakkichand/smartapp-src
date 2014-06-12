package com.jiubang.ggheart.appgame.widget;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.gau.utils.cache.utils.CacheUtil;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.appcenter.help.AppsNetConstant;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.FeatureDataParser;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 负责提供widget数据，从本地加载或从网络加载
 * @author zhoujun
 *
 */
public class AppGameWidgetDataProvider {
	public static final String KEY_APPCENER_WIDGET_MARK = "key_appcenter_widget_mark";
	public static final String KEY_APPCENER_WIDGET_TYPEID = "key_appcenter_widget_typeid";
	public static final String APPGAME_WIDGET_DATA_LOCAL_PATH = LauncherEnv.Path.APPCENTER_WIDGET_INFO_PATH
			+ "widget.txt";
	
	/**
	 * Widget缓存Key值
	 */
	public static final String KEY_CACHE_WIDGET = "key_cache_widget";

	/**
	 * 首先加载本地的数据
	 * 
	 * @param context
	 * @return
	 */
	public ArrayList<ClassificationDataBean> getLocalWidgetData() {
//		if (FileUtil.isFileExist(APPGAME_WIDGET_DATA_LOCAL_PATH)) {
//			try {
//				String appInfo = RecommAppFileUtil.readFileToString(APPGAME_WIDGET_DATA_LOCAL_PATH);
//				if (appInfo != null && !"".equals(appInfo)) {
//					JSONArray obj = new JSONArray(appInfo);
//					return parseWidgetInfo(obj);
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
		// TODO:LIGUOLIANG 修改缓存管理方式
		AppCacheManager acm = AppCacheManager.getInstance();
		if (!acm.isCacheExist(KEY_CACHE_WIDGET)) {
			return null;
		}
		byte[] cacheData = acm.loadCache(KEY_CACHE_WIDGET);
		if (cacheData == null) {
			return null;
		}
		String appInfo = CacheUtil.byteArrayToString(cacheData);
		if (appInfo == null) {
			return null;
		}
		if (appInfo != null && !"".equals(appInfo)) {
			JSONArray obj = null;;
			try {
				obj = new JSONArray(appInfo);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			return parseWidgetInfo(obj);
		}
		return null;
	}

	/**
	 * 从网络加载数据
	 * @return
	 */
	public void getNetworkWidgetData(final Context context, final WidgetDataHandler handler) {
		if (context == null || handler == null) {
			return;
		}
		// 首选url
		String url = DownloadUtil.getAppCenterHost(context)
				+ AppsNetConstant.APP_CENTER_WIDGET_PATH + DownloadUtil.sRandom.nextLong();
		// 备选url
		String aurl = DownloadUtil.getAlternativeAppCenterHost(context)
				+ AppsNetConstant.APP_CENTER_WIDGET_PATH + DownloadUtil.sRandom.nextLong();
//		String mark = DownloadUtil.getMark(context, KEY_APPCENER_WIDGET_MARK);
		JSONObject phead = RecommAppsUtils.createHttpHeader(context,
				AppsNetConstant.CLASSIFICATION_INFO_PVERSION);
		JSONObject postdata = new JSONObject();
//		int must = FileUtil.isFileExist(APPGAME_WIDGET_DATA_LOCAL_PATH) ? 0 : 1;
		
		// TODO:LIGUOLIANG 修改缓存管理方式
		String mark = "";
		int must = 1;
		AppCacheManager acm = AppCacheManager.getInstance();
		if (acm.isCacheExist(KEY_CACHE_WIDGET)) {
			must = 0;
			mark = DownloadUtil.getMark(context, KEY_APPCENER_WIDGET_MARK);
		}
		try {
			postdata.put("phead", phead);
			postdata.put("must", must);
			postdata.put("mark", mark);
		} catch (JSONException e) {
			e.printStackTrace();
			handler.handle(null);
			return;
		}

		THttpRequest request = null;
		try {
			request = new THttpRequest(url, postdata.toString().getBytes(), new IConnectListener() {

				@Override
				public void onStart(THttpRequest arg0) {
				}

				@Override
				public void onFinish(THttpRequest request, IResponse response) {
					if (response != null && response.getResponse() != null
							&& (response.getResponse() instanceof JSONObject)) {
						JSONObject json = (JSONObject) response.getResponse();
						try {
							JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);

							//保存获取数据时，服务器的时间和本地时间
							long severtime = result.optLong(MessageListBean.TAG_SEVERTIME, -1l);
							DownloadUtil.saveSerTime(context, severtime);

							int status = result.getInt(MessageListBean.TAG_STATUS);
							if (status == ConstValue.STATTUS_OK) {
								int hasNew = json.getInt("hasnew");
								// 服务器有更新数据
								if (hasNew == 1) {
									// 缓存mark值
									String resultMark = json.getString("mark");
									//该widget分类id(用于统计)
									int typeId = json.getInt("typeid");

									DownloadUtil.saveMark(context, KEY_APPCENER_WIDGET_MARK,
											resultMark);
									DownloadUtil.saveMark(context, KEY_APPCENER_WIDGET_TYPEID,
											String.valueOf(typeId));

									JSONArray widgets = json.getJSONArray("widgets");
									// 缓存数据信息
//									FileUtil.saveByteToSDFile(widgets.toString().getBytes(),
//											APPGAME_WIDGET_DATA_LOCAL_PATH);
									// TODO:LIGUOLIANG 修改缓存管理方式
									AppCacheManager acm = AppCacheManager.getInstance();
									acm.saveCache(KEY_CACHE_WIDGET, widgets.toString().getBytes());									
									handler.handle(parseWidgetInfo(widgets));
									return;
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					handler.handle(null);
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
					handler.handle(null);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			handler.handle(null);
			return;
		}
		if (request != null) {
			//  设置备选url
			try {
				request.addAlternateUrl(aurl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
			request.setRequestPriority(Thread.MAX_PRIORITY);
			request.setOperator(new AppJsonOperator());
			AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(context);
			httpAdapter.addTask(request);
		}
	}

	/**
	 * 解析widget数据
	 * @param array
	 * @return
	 */
	private ArrayList<ClassificationDataBean> parseWidgetInfo(JSONArray array) {
		if (array == null || array.length() <= 0) {
			return null;
		}
		int count = array.length();
		ArrayList<ClassificationDataBean> widgetList = new ArrayList<ClassificationDataBean>(count);
		JSONObject json = null;
		// 应用或专题数据
		try {
			for (int i = 0; i < count; i++) {
				json = (JSONObject) array.opt(i);
				ClassificationDataBean ret = new ClassificationDataBean();
				// 该版面widgetid
				int id = json.optInt("id", Integer.MIN_VALUE);
				// 展现类型 1：两个banner图排列 2：5个应用图标排列
				int viewtype = json.optInt("viewtype", Integer.MIN_VALUE);
				int datatype = json.optInt("datatype", Integer.MIN_VALUE);
				ret.mViewType = viewtype;
				ret.mWidgetDataType = datatype;
				JSONArray appArray = json.getJSONArray("appdatas");
				if (viewtype == 1) {
					// 专题推荐数据
					ret.dataType = ClassificationDataBean.SPECIALSUBJECT_TYPE;
					ret.featureList = FeatureDataParser.parseFeatureApp(id, appArray);
				} else if (viewtype == 2) {
					// 精品推荐数据
					ret.dataType = ClassificationDataBean.FEATURE_TYPE;
					ret.featureList = FeatureDataParser.parseFeatureApp(id, appArray);
				} else {
					Log.e("ClassificationDataParser", "parseDataBean bad viewtype = " + viewtype);
				}
				widgetList.add(ret);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return widgetList;
	}

	/**
	 * 
	 widget数据处理者
	 * 
	 * @author xiedezhi
	 * @date [2012-8-24]
	 */
	public interface WidgetDataHandler {
		public void handle(ArrayList<ClassificationDataBean> list);
	}
}
