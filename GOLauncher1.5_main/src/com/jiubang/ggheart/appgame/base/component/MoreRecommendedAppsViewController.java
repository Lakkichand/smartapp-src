package com.jiubang.ggheart.appgame.base.component;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.gau.utils.cache.CacheManager;
import com.gau.utils.cache.impl.FileCacheImpl;
import com.gau.utils.cache.utils.CacheUtil;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.appcenter.help.AppsNetConstant;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.MoreSimilarAppsParser;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.BaseController;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  zhengxiangcan
 * @date  [2012-12-18]
 */
public class MoreRecommendedAppsViewController extends BaseController {
	/**
	 * 开始加载下一页
	 */
	public static int sACTION_NEXT_PAGE = 101;
	/**
	 * 取消加载下一页
	 */
	public static int sACTION_CANCLE_NEXT_PAGE = 102;
	/**
	 * 返回下一页数据
	 */
	public static int sACTION_NEXT_PAGE_DATA = 103;

	private LoadDataRunnable mCurrentRunnable = null;
	
	private CacheManager mCacheManager;

	public MoreRecommendedAppsViewController(Context context, IModeChangeListener listener) {
		super(context, listener);
		mCacheManager = new CacheManager(new FileCacheImpl(LauncherEnv.Path.APPS_DETAIL_CACHE_PATH));
	}

	/** {@inheritDoc} */

	@Override
	protected Object handleRequest(int action, Object parames) {
		if (action == sACTION_NEXT_PAGE) {
			if (mCurrentRunnable != null) {
				mCurrentRunnable.kill();
			}
			Bundle bundle = (Bundle) parames;
			int typeId = bundle.getInt("typeId", 302);
			int itp = bundle.getInt("itp", 2);
			int pageId = bundle.getInt("pageId", 1);
			int startIndex = bundle.getInt("startIndex", 1);
			String pkgName = bundle.getString("pkgName");
			mCurrentRunnable = new LoadDataRunnable(typeId, itp, pageId, startIndex, pkgName);
			mCurrentRunnable.run();
		} else if (action == sACTION_CANCLE_NEXT_PAGE) {
			if (mCurrentRunnable != null) {
				mCurrentRunnable.kill();
			}
		}
		return null;
	}

	/** {@inheritDoc} */

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  
	 * @date  [2012-11-17]
	 */
	private class LoadDataRunnable implements Runnable {

		/**
		 * 分类id
		 */
		int mTypeId = 0;

		/**
		 * id类型
		 */
		int mItp = 0;

		int mStartIndex = 1;

		int mPageId = 1;
		
		String mPkgName = null;

		public LoadDataRunnable(int typeId, int itp, int pageId, int startIndex, String pkgName) {
			mTypeId = typeId;
			mItp = itp;
			mStartIndex = startIndex;
			mPageId = pageId;
			mPkgName = pkgName;
		}

		/**
		 * 该线程是否已被杀死
		 */
		private boolean mIsKilled = false;
		/**
		 * 同步锁
		 */
		private Object mIsKilledLock = new Object();

		/**
		 * 标志该线程已被杀死，后台拿到数据后不做处理
		 */
		public void kill() {
			synchronized (mIsKilledLock) {
				mIsKilled = true;
			}
		}

		/**
		 * 判断当前线程是否已被杀死
		 * 
		 */
		public boolean isKilled() {
			synchronized (mIsKilledLock) {
				return mIsKilled;
			}
		}

		@Override
		public void run() {
//			if (mShowId == -1) {
//				return;
//			}
			String url = getRequestUrlByType(mContext);
//			final int[] typeIds = new int[] { mTypeId };
			final JSONObject postData = getPostJSON(mContext, mTypeId, mPageId, mItp, mPkgName);
			THttpRequest request = null;
			try {
				request = new THttpRequest(url, postData.toString().getBytes(),
						new IConnectListener() {

							@Override
							public void onStart(THttpRequest arg0) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onFinish(THttpRequest request, IResponse response) {
								if (response != null && response.getResponse() != null
										&& (response.getResponse() instanceof JSONObject)) {
									try {
										JSONObject json = (JSONObject) response.getResponse();
										ClassificationDataBean bean = getClassificationData(json, postData, mContext,
														 mTypeId, mPageId, mStartIndex, mPkgName);
										if (bean != null) {
											if (isKilled()) {
												return;
											}
											notifyChange(sACTION_NEXT_PAGE_DATA, STATE_RESPONSE_OK,
													bean);
											kill();
											return;
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								notifyError();
							}

							@Override
							public void onException(THttpRequest arg0, int arg1) {
								notifyError();
							}
						});
			} catch (Exception e) {
				notifyError();
				return;
			}
			if (request != null) {
				// 设置备选url
				try {
					request.addAlternateUrl(getAlternativeUrl(mContext));
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
				request.setRequestPriority(Thread.MAX_PRIORITY);
				request.setOperator(new AppJsonOperator());
				request.setNetRecord(new AppGameNetRecord(mContext, false));
				AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(mContext);
				httpAdapter.addTask(request, true);
			}
		}

		private void notifyError() {
			if (isKilled()) {
				return;
			}
			notifyChange(sACTION_NEXT_PAGE_DATA, STATE_RESPONSE_ERR, null);
			kill();
		}
	}
	
	/**
	 * 生成向服务器传递的参数信息
	 */
	private JSONObject getPostJSON(Context context, int typeId, int pageId,
			int itp, String pkgName) {
		JSONObject request = new JSONObject();
		JSONObject phead = RecommAppsUtils.createHttpHeader(context,
				AppsNetConstant.CLASSIFICATION_INFO_PVERSION);

		int must = 1;
		String mark = "";
		String detailCacheKey = typeId + "_" + pkgName + "_" + pageId; // 包名 作为键值
		if (mCacheManager.isCacheExist(detailCacheKey)) {
			byte[] cacheByteArray = mCacheManager.loadCache(detailCacheKey);
			JSONObject json = CacheUtil.byteArrayToJson(cacheByteArray);
			if (json != null) {
				must = 0;
				mark = json.optString("mark", "");
			}
		}
		try {
			JSONObject json = new JSONObject();
			json.put("packagename", pkgName);
			request.put("phead", phead);
			request.put("typeid", typeId);
			request.put("itp", itp);
			request.put("must", must);
			request.put("params", json);
			request.put("mark", mark);
			request.put("pageid", pageId);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return request;
	}
	
	/**
	 * <br>功能简述:将服务器下发的数据转换成数据bean
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param json
	 * @param postdata
	 * @param context
	 * @param showId
	 * @param typeId
	 * @param pageId
	 * @param startIndex
	 * @param pkgName
	 * @return
	 */
	private ClassificationDataBean getClassificationData(JSONObject json,
			JSONObject postdata, final Context context, int typeId,
			int pageId, final int startIndex, String pkgName) {
		if (json == null) {
			return null;
		}
		ClassificationDataBean dataBean = null;
		try {
//			int hasNew = json.getInt("hasnew");
			JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);
			int status = result.getInt(MessageListBean.TAG_STATUS);
			String detailCacheKey = typeId + "_" + pkgName + "_" + pageId;
			if (status == ConstValue.STATTUS_OK) {

				int hasNew = json.getInt("hasnew");

				if (hasNew == 1) {
					dataBean = MoreSimilarAppsParser.parseDataBean(typeId,
							json);
					// 缓存数据
					if (mCacheManager.isCacheExist(detailCacheKey)) {
						// 如果缓存已经存在则清除旧的缓存
						mCacheManager.clearCache(detailCacheKey);
					}
					mCacheManager.saveCache(detailCacheKey,
							CacheUtil.jsonToByteArray(json));
				} else {
					JSONObject obj = CacheUtil.byteArrayToJson(mCacheManager
							.loadCache(detailCacheKey));
					dataBean = MoreSimilarAppsParser.parseDataBean(typeId,
							obj);
				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return dataBean;
	}
	
	/**
	 * 功能简述:获取更多相关推荐数据的URL
	 * 
	 * @return
	 */
	private String getRequestUrlByType(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAppCenterHost(context)
					+ AppsNetConstant.APP_MORE_SIMILAR_APP_PATH
					+ DownloadUtil.sRandom.nextLong();
		}
		return url;
	}
	
	/**
	 * 获取更多相关推荐数据的备选地址
	 */
	private String getAlternativeUrl(Context context) {
		String url = null;
		if (null != context) {
			url = DownloadUtil.getAlternativeAppCenterHost(context)
					+ AppsNetConstant.APP_MORE_SIMILAR_APP_PATH
					+ DownloadUtil.sRandom.nextLong();
		}
		return url;
	}
}
