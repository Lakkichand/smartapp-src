/*
 * 文 件 名:  AppkitsController.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-12-3
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.recommend;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataDownload;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataParser;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataParser.LocalJSON;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.BaseController;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-12-3]
 */
public class AppkitsController extends BaseController {

	/**
	 * 开始加载下一页
	 */
	public final static int ACTION_NEXT_PAGE = 101;
	/**
	 * 取消加载下一页
	 */
	public final static int ACTION_CANCLE_NEXT_PAGE = 102;
	/**
	 * 返回下一页数据
	 */
	public final static int ACTION_NEXT_PAGE_DATA = 103;

	/**
	 * 分类ID，统计需要
	 */
	private int mTypeId = 0;
	
	private LoadDataRunnable mCurrentRunnable = null;
	
	public AppkitsController(Context context, IModeChangeListener listener) {
		super(context, listener);
	}

	/** {@inheritDoc} */

	@Override
	protected Object handleRequest(int action, Object parames) {
		if (action == ACTION_NEXT_PAGE) {
			if (mCurrentRunnable != null) {
				mCurrentRunnable.kill();
			}
			Bundle bundle = (Bundle) parames;
			int typeId = bundle.getInt("typeId");
			int itp = bundle.getInt("itp", 2);
			int pageId = bundle.getInt("pageId", 1);
			int startIndex = bundle.getInt("startIndex", 1);
			mCurrentRunnable = new LoadDataRunnable(typeId, itp, pageId, startIndex);
			mCurrentRunnable.run();
		} else if (action == ACTION_CANCLE_NEXT_PAGE) {
			if (mCurrentRunnable != null) {
				mCurrentRunnable.kill();
			}
		}
		return null;
	}

	/** {@inheritDoc} */

	@Override
	public void destory() {
		if (mCurrentRunnable != null) {
			mCurrentRunnable.kill();
		}
	}
	
	public int getTypeId() {
		return mTypeId;
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  Administrator
	 * @date  [2012-12-10]
	 */
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 */
	private class LoadDataRunnable implements Runnable {

		/**
		 * 分类id
		 */
		int mVirtualTypeId = 0;
		/**
		 * id类型
		 */
		int mItp = 0;

		int mStartIndex = 1;

		int mPageId = 1;

		public LoadDataRunnable(int typeId, int itp, int pageId, int startIndex) {
			mVirtualTypeId = typeId;
			mItp = itp;
			mStartIndex = startIndex;
			mPageId = pageId;
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
			String url = ClassificationDataDownload.getUrl(mContext);
			List<LocalJSON> typeIdList = new ArrayList<LocalJSON>();
			try {
				typeIdList = ClassificationDataParser.getLocalSubTypeidList(mVirtualTypeId);
			} catch (Throwable e) {
				// 一个保护机制，假如读取有异常，则清除子typeid的缓存
				ClassificationDataBean bean = ClassificationDataDownload.getLocalData(
						mVirtualTypeId, 1, null);
				if (bean != null && bean.categoriesList != null && bean.categoriesList.size() > 0) {
					AppCacheManager acm = AppCacheManager.getInstance();
					for (CategoriesDataBean category : bean.categoriesList) {
						if (category != null) {
							int typeid = category.typeId;
							String key = ClassificationDataDownload.buildClassificationKey(typeid,
									1);
							acm.clearCache(key);
						}
					}
					String key = ClassificationDataDownload.buildClassificationKey(mVirtualTypeId,
							1);
					acm.clearCache(key);
				}
				typeIdList.clear();
				LocalJSON localJson = new LocalJSON();
				localJson.mTypeId = mVirtualTypeId;
				typeIdList.add(localJson);
			}
			final int[] typeIds = new int[typeIdList.size()];
			for (int i = 0; i < typeIdList.size(); i++) {
				typeIds[i] = typeIdList.get(i).mTypeId;
			}
			for (int i = 0; i < typeIds.length; i++) {
				TabDataManager.getInstance().removeTabData(typeIds[i]);
			}
			JSONObject data = ClassificationDataDownload.getPostJson(mContext, typeIds, -1,
					mPageId, mItp);
			final JSONObject postData = fixedJsonObject(mVirtualTypeId, data);
			THttpRequest request = null;
			try {
				request = new THttpRequest(url, postData.toString().getBytes(),
						new IConnectListener() {

							@Override
							public void onStart(THttpRequest arg0) {
							}

							@Override
							public void onFinish(THttpRequest request, IResponse response) {
								if (response != null && response.getResponse() != null
										&& (response.getResponse() instanceof JSONObject)) {
									try {
										JSONObject json = (JSONObject) response.getResponse();
										// 先取得301虚拟typeid的数据，它挂载了N个分类ID
										List<ClassificationDataBean> beans = ClassificationDataDownload
												.getClassificationData(json, postData, mContext, typeIds, mPageId, mStartIndex,
														false);
										// 取出301挂载的所有分类ID
										ArrayList<Integer> typeIdList = new ArrayList<Integer>();
										if (beans != null && beans.size() > 0) {
											ClassificationDataBean bean = beans.get(0);
											if (bean != null) {
												mTypeId = bean.typeId;
												if (bean.categoriesList != null) {
													for (CategoriesDataBean cageBean : bean.categoriesList) {
														typeIdList.add(cageBean.typeId);
													}
												}
											}
										}
										// 取出分类应用的数据
										List<ClassificationDataBean> appBeans = new ArrayList<ClassificationDataBean>();
										for (int typeId : typeIdList) {
											ClassificationDataBean bean = TabDataManager.getInstance().getTabData(typeId);
											if (bean != null) {
												appBeans.add(bean);
											}
										}
										// 将数据加入到结果列表中
										if (appBeans != null && appBeans.size() > 0) {
											ArrayList<AppkitsBean> resultList = new ArrayList<AppkitsBean>();
											for (ClassificationDataBean bean : appBeans) {
												// 增加一个分组头
												AppkitsBean titleBean = new AppkitsBean();
												titleBean.mTitle = bean.typename;
												resultList.add(titleBean);
												// 分组头的4个需要显示的应用
												AppkitsBean appBean = new AppkitsBean();
												if (bean.featureList == null) {
													resultList.remove(resultList.size() - 1);
													continue;
												}
												for (int i = 0; i < bean.featureList.size(); i++) {
													BoutiqueApp app = bean.featureList.get(i);
													if (app.info != null
															&& app.info.packname != null
															&& !app.info.packname.equals("")) {
															appBean.mAppInfoList.add(app);
													}
												}
												resultList.add(appBean);
											}
											if (isKilled()) {
												return;
											}
											notifyChange(ACTION_NEXT_PAGE_DATA, STATE_RESPONSE_OK,
													resultList);
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
					request.addAlternateUrl(ClassificationDataDownload.getAlternativeUrl(mContext));
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
			notifyChange(ACTION_NEXT_PAGE_DATA, STATE_RESPONSE_ERR, null);
			kill();
		}
		
		private JSONObject fixedJsonObject(int virtualTypeId, JSONObject jsonObject) {
			try {
				JSONArray array = jsonObject.getJSONArray("reqs");
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = (JSONObject) array.get(i);
					int typeId = obj.optInt("typeid", virtualTypeId);
					if (typeId == virtualTypeId) {
						obj.put("itp", 2);
					} else {
						obj.put("itp", 0);
					}
				}
				jsonObject.put("reqs", array);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return jsonObject;
		}
	}

}
