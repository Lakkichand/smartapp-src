/**
 * 
 */
package com.jiubang.ggheart.appgame.base.data;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.appcenter.help.AppsNetConstant;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;

/**
 * @author liguoliang
 * 
 */
public class AppsSearchDownload {

	/**
	 * 同步锁
	 */
	private Object mLock = new Object();
	public int mCurPage = -1; // 当前分页
	public int mTotalNum = 0; // 总结果数量
	public int mPageNum = -1; // 总页数
	public int mSearchId = -1; // 搜索分类id

	public static final int SEARCH_TYPE_ALL = 0; // 搜索类别： 全部
	public static final int SEARCH_TYPE_APP = 1; // 搜索类别： 应用

	public static final int PRICE_TYPE_ALL = 0; // 价格类型： 全部
	public static final int PRICE_TYPE_CHARGE = 1; // 价格类型： 收费
	public static final int PRICE_TYPE_FREE = 2; // 价格类型： 免费
	/**
	 * 是否正在加载下一页
	 */
	public boolean mIsLoading = false;

	/**
	 * @param context
	 * @param key
	 * @param searchType
	 *            搜索类别
	 * @param priceType
	 *            价格类别
	 * @param pageId
	 *            请求页面,从1开始
	 * @param clientId
	 * 			  访问id，1:应用&游戏中心 2:功能表搜索 3：插件搜索 4:内置中心
	 * @return
	 */
	public THttpRequest getSearchData(Context context, String key, int searchType, int priceType,
			int pageId, final SearchDataHandler handler, int clientId) {
		if (handler == null) {
			return null;
		}
		if (context == null) {
			handler.handleData(null);
			return null;
		}
		String url = getRequestUrlByType(context);
		if (url == null) {
			handler.handleData(null);
			return null;
		}
		JSONObject postdata = new JSONObject();
		JSONObject phead = RecommAppsUtils.createHttpHeader(context,
				AppsNetConstant.CLASSIFICATION_INFO_PVERSION, clientId);
		try {
			postdata.put("phead", phead);
			postdata.put("key", key);
			postdata.put("ty", searchType);
			postdata.put("feety", priceType);
			postdata.put("pageid", pageId);
		} catch (JSONException e) {
			e.printStackTrace();
			handler.handleData(null);
			return null;
		}
		THttpRequest request = null;
		try {
			request = new THttpRequest(url, postdata.toString().getBytes(), new IConnectListener() {

				@Override
				public void onStart(THttpRequest arg0) {
					setIsLoadingNextPage(true);
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
								mTotalNum = json.getInt("totalnum");
								mPageNum = json.getInt("pages");
								mCurPage = json.getInt("pageid");
								mSearchId = json.getInt("searchid");
								JSONArray appArray = json.getJSONArray("apps");
								List<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
								if (appArray != null) {
									for (int i = 0; i < appArray.length(); i++) {
										JSONObject appJson = appArray.optJSONObject(i);
										if (appJson != null) {
											BoutiqueApp app = new BoutiqueApp();
											app.typeid = mSearchId;
											FeatureDataParser.parseAppInfo(appJson, app.info);
											if (app.info.detailtype == BoutiqueApp.DETAIL_TYPE_FTP) {
												app.acttype = BoutiqueApp.FEATURE_ACTTYPE_FTP;
											} else if (app.info.detailtype == BoutiqueApp.DETAIL_TYPE_MARKET) {
												app.acttype = BoutiqueApp.FEATURE_ACTTYPE_MARKET;
											} else if (app.info.detailtype == BoutiqueApp.DETAIL_TYPE_WEB) {
												app.acttype = BoutiqueApp.FEATURE_ACTTYPE_BROWSER;
											}
											app.actvalue = app.info.detailurl;
											ret.add(app);
										}
									}
								}
								handler.handleData(ret);
								setIsLoadingNextPage(false);
								return;
							}
						} catch (JSONException e) {
							e.printStackTrace();
							mTotalNum = 0;
							mPageNum = -1;
							mCurPage = -1;
							mSearchId = -1;
						}
						handler.handleData(null);
					} else {
						handler.handleData(null);
					}
					setIsLoadingNextPage(false);
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
					handler.handleData(null);
					setIsLoadingNextPage(false);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			handler.handleData(null);
			setIsLoadingNextPage(false);
			return null;
		}
		if (request != null) {
			// 设置备选url
			try {
				request.addAlternateUrl(getAlternativeUrl(context));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
			request.setRequestPriority(Thread.MAX_PRIORITY);
			request.setOperator(new AppJsonOperator());
			request.setNetRecord(new AppGameNetRecord(context, false));
			AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(context);
			httpAdapter.addTask(request, true);
			return request;
		}
		return null;
	}

	public void cancelTask(Context context, THttpRequest request) {
		AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(context);
		httpAdapter.cancelTask(request);
	}
	
	/**
	 * 功能简述:通过启动类型来获取请求地址的方法 功能详细描述:主要是为了区分应用中心搜索和游戏中心搜索的请求URL 注意:
	 * 
	 * @return
	 */
	private String getRequestUrlByType(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAppCenterHost(context) + AppsNetConstant.APP_CENTER_SEARCH_PATH
					+ DownloadUtil.sRandom.nextLong();
		}
		return url;
	}

	/**
	 * 获取搜索备选url
	 */
	private String getAlternativeUrl(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAlternativeAppCenterHost(context)
					+ AppsNetConstant.APP_CENTER_SEARCH_PATH + DownloadUtil.sRandom.nextLong();
		}
		return url;
	}

	public boolean getIsLoadingNextPage() {
		synchronized (mLock) {
			return mIsLoading;
		}

	}

	public void setIsLoadingNextPage(boolean isLoading) {
		synchronized (mLock) {
			mIsLoading = isLoading;
		}
	}

	/**
	 * 搜索数据处理者
	 * 
	 * @author xiedezhi
	 * @date [2012-8-23]
	 */
	public interface SearchDataHandler {
		public void handleData(Object object);
	}
}
