package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
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
import com.jiubang.ggheart.appgame.base.utils.AppGameInstalledFilter;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.BaseController;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;

/**
 * 负责EmptyContainer的数据读取
 * 
 * @author xiedezhi
 * 
 */
public class EmptyController extends BaseController {

	/**
	 * 加载数据，先从内存找，找不到从本地数据找，再找不到就向服务器请求数据
	 */
	public static final int ACTION_LOAD_DATA = 1234;

	/**
	 * 入口值，请求网络数据的时候要用到
	 */
	private int mEntrance;

	public EmptyController(Context context, IModeChangeListener listener) {
		super(context, listener);
	}

	/**
	 * 设置入口值
	 */
	public void setEntrance(int entrance) {
		mEntrance = entrance;
	}

	@Override
	protected Object handleRequest(int action, Object parames) {
		if (action == ACTION_LOAD_DATA) {
			final int typeid = (Integer) parames;
			ClassificationDataBean bean = TabDataManager.getInstance().getTabData(typeid);
			if (bean != null && bean.dataType != ClassificationDataBean.EMPTY_TYPE) {
				Log.e("EmptyController", "bean != null");
				notifyChange(ACTION_LOAD_DATA, STATE_RESPONSE_OK, bean);
				// 如果这里有数据证明是后台获取新数据时把这个页面的数据拿到了，这种情况没有统计应用下发，要在这里补上
				saveAppIssueDataList(getSubBeanFromDataManager(typeid));
				return null;
			}
			// 读取本地数据是否成功
			boolean success = true;
			// 读取本地保存的数据
			List<ClassificationDataBean> beanList = new ArrayList<ClassificationDataBean>();
			List<Integer> subIdList = new ArrayList<Integer>();
			List<LocalJSON> sTypeIds = ClassificationDataParser.getLocalSubTypeidList(typeid);
			for (LocalJSON sTypeId : sTypeIds) {
				subIdList.add(sTypeId.mTypeId);
				// 读取本地数据
				ClassificationDataBean subBean = ClassificationDataDownload.getLocalData(
						sTypeId.mTypeId, 1, sTypeId.mJson);
				if (subBean == null) {
					success = false;
					break;
				}
				beanList.add(subBean);
			}
			if (success) {
				Log.e("EmptyController", "bean != null");
				// 把从本地读出来的数据放到缓存中
				TabDataManager.getInstance().cacheTabData(subIdList,
						AppGameInstalledFilter.filterDataBeanList(beanList));
				notifyChange(ACTION_LOAD_DATA, STATE_RESPONSE_OK, TabDataManager.getInstance()
						.getTabData(typeid));
				// 因为在TabController的prevLoad方法里已经启动后台获取最新数据，所以这里不用后台获取新数据了
				// -----------------统计START-----------------------//
				saveAppIssueDataList(beanList);
				// -----------------统计END-----------------------//
			} else {
				String url = ClassificationDataDownload.getUrl(mContext);
				List<LocalJSON> typeIdList = ClassificationDataParser.getLocalSubTypeidList(typeid);
				int[] typeIdArray = new int[typeIdList.size()];
				for (int i = 0; i < typeIdList.size(); i++) {
					typeIdArray[i] = typeIdList.get(i).mTypeId;
				}
				final int[] typeIds = typeIdArray;
				final int pageId = 1;
				final JSONObject postdata = ClassificationDataDownload.getPostJson(mContext,
						typeIds, mEntrance, pageId, 0);
				THttpRequest request = null;
				try {
					request = new THttpRequest(url, postdata.toString().getBytes(),
							new IConnectListener() {

								@Override
								public void onStart(THttpRequest request) {
								}

								@Override
								public void onFinish(THttpRequest request, IResponse response) {
									if (response != null && response.getResponse() != null
											&& (response.getResponse() instanceof JSONObject)) {
										try {
											JSONObject json = (JSONObject) response.getResponse();
											List<ClassificationDataBean> beans = ClassificationDataDownload
													.getClassificationData(json, postdata,
															mContext, typeIds, pageId, 1, true);
											// 通知EmptyContainer
											notifyChange(ACTION_LOAD_DATA, STATE_RESPONSE_OK,
													TabDataManager.getInstance().getTabData(typeid));
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}

								@Override
								public void onException(THttpRequest request, int reason) {
									// do nothing
								}
							});
				} catch (Exception e) {
					// do nothing
				}
				if (request != null) {
					// 设置备选url
					try {
						request.addAlternateUrl(ClassificationDataDownload
								.getAlternativeUrl(mContext));
					} catch (Exception e) {
						e.printStackTrace();
					}
					// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
					request.setRequestPriority(Thread.MAX_PRIORITY);
					request.setOperator(new AppJsonOperator());
					request.setNetRecord(new AppGameNetRecord(mContext, true));
					AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(mContext);
					httpAdapter.addTask(request, true);
				}
			}
		}
		return null;
	}
	@Override
	public void destory() {

	}

	/**
	 * 从内存中找出分类id对应的所有子分类数据（包括自己）
	 */
	private List<ClassificationDataBean> getSubBeanFromDataManager(int typeid) {
		ClassificationDataBean bean = TabDataManager.getInstance().getTabData(typeid);
		if (bean == null) {
			return null;
		}
		List<ClassificationDataBean> ret = new ArrayList<ClassificationDataBean>();
		ret.add(bean);
		if (bean.dataType == ClassificationDataBean.TAB_TYPE
				|| bean.dataType == ClassificationDataBean.BUTTON_TAB) {
			if (bean.categoriesList != null && bean.categoriesList.size() > 0) {
				for (CategoriesDataBean category : bean.categoriesList) {
					if (category != null) {
						List<ClassificationDataBean> sList = getSubBeanFromDataManager(category.typeId);
						if (sList != null && sList.size() > 0) {
							ret.addAll(sList);
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * 统计应用下发
	 */
	private void saveAppIssueDataList(List<ClassificationDataBean> beanList) {
		// -----------------统计START-----------------------//
		if (beanList != null && beanList.size() > 0) {
			List<String> packNameList = new ArrayList<String>();
			List<String> typeIdList = new ArrayList<String>();
			List<Integer> indexList = new ArrayList<Integer>();
			for (ClassificationDataBean bean : beanList) {
				if (bean != null) {
					if (bean.dataType == ClassificationDataBean.SPECIALSUBJECT_TYPE
							|| bean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE
							|| bean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE
							|| bean.dataType == ClassificationDataBean.FEATURE_TYPE
							|| bean.dataType == ClassificationDataBean.COVER_FLOW
							|| bean.dataType == ClassificationDataBean.AD_BANNER
							|| bean.dataType == ClassificationDataBean.PRICE_ALERT
							|| bean.dataType == ClassificationDataBean.GRID_TYPE
							|| bean.dataType == ClassificationDataBean.WALLPAPER_GRID) {
						List<BoutiqueApp> featureList = bean.featureList;
						if (featureList != null && featureList.size() > 0) {
							for (int i = 0; i < featureList.size(); i++) {
								BoutiqueApp app = featureList.get(i);
								packNameList.add(app.info.packname);
								typeIdList.add(String.valueOf(app.typeid));
								indexList.add(i + 1);
							}
						}
					}
				}
			}
			AppRecommendedStatisticsUtil.getInstance().saveAppIssueDataList(mContext, packNameList,
					typeIdList, indexList);
		}
		// -----------------统计END-----------------------//
	}

}
