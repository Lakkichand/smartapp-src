package com.zhidian.wifibox.controller;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.adapter.HomeFeatureadapter.TransformationDataBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.HomeFeatureDataBean;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;

/**
 * 首页推荐控制器
 * 
 * @author xiedezhi
 * 
 */
public class HomeFeatureController extends TACommand {
	/**
	 * 加载下一页命令
	 */
	public static final String NEXT_PAGE = "HOMEFEATURECONTROLLER_NEXT_PAGE";
	/**
	 * 转化bean
	 */
	public static final String TRANSFORMATION = "HOMEFEATURECONTROLLER_TRANSFORMATION";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(NEXT_PAGE)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			final boolean loadLocalDataFirst = Boolean.valueOf(obj[2]);
			if (loadLocalDataFirst) {
				// 先加载本地数据再后台更新数据，拿到新数据后要刷新对应的页面
				String content = CDataDownloader.getLocalData(
						TAApplication.getApplication(), idUrl);
				PageDataBean bean = new PageDataBean();
				bean.mDataType = PageDataBean.HOME_FEATURE_DATATYPE;
				bean.mUrl = idUrl;
				bean.mPageIndex = 1;
				DataParser.parseHomeFeatureList(TAApplication.getApplication(),
						bean, content);
				if (bean.mStatuscode == 0 && bean.mHomeFeatureDataBean != null) {
					// 如果数据没问题，先把数据发送回去
					HomeFeatureController.this.sendSuccessMessage(bean);
					// 后台刷新数据
					CDataDownloader.getData(dataUrl,
							new AsyncHttpResponseHandler() {
								@Override
								public void onSuccess(String content) {
									// 加载成功
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.HOME_FEATURE_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = 1;
									DataParser.parseHomeFeatureList(
											TAApplication.getApplication(),
											bean, content);
									// 更新数据
									TabDataManager.getInstance()
											.replacePageData(bean);
									// 刷新界面
									MainActivity.sendHandler(null,
											IDiyFrameIds.TABMANAGEVIEW,
											IDiyMsgIds.REFRESH_CONTAINER, -1,
											idUrl, null);
								}

								@Override
								public void onStart() {
								}

								@Override
								public void onFailure(Throwable error) {
									// do nothing
								}

								@Override
								public void onFinish() {
								}
							});
				} else {
					// 连网获取数据
					CDataDownloader.getData(dataUrl,
							new AsyncHttpResponseHandler() {
								@Override
								public void onSuccess(String content) {
									// 加载成功
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.HOME_FEATURE_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = 1;
									DataParser.parseHomeFeatureList(
											TAApplication.getApplication(),
											bean, content);
									HomeFeatureController.this
											.sendSuccessMessage(bean);
								}

								@Override
								public void onStart() {
								}

								@Override
								public void onFailure(Throwable error) {
									// 加载失败
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.HOME_FEATURE_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = 1;
									bean.mStatuscode = 2;
									bean.mMessage = error.getMessage();
									HomeFeatureController.this
											.sendSuccessMessage(bean);
								}

								@Override
								public void onFinish() {
								}
							});
				}
			} else {
				// 连网获取数据
				CDataDownloader.getData(dataUrl,
						new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String content) {
								// 加载成功
								PageDataBean bean = new PageDataBean();
								bean.mDataType = PageDataBean.HOME_FEATURE_DATATYPE;
								bean.mUrl = idUrl;
								bean.mPageIndex = 1;
								DataParser.parseHomeFeatureList(
										TAApplication.getApplication(), bean,
										content);
								HomeFeatureController.this
										.sendSuccessMessage(bean);
							}

							@Override
							public void onStart() {
							}

							@Override
							public void onFailure(Throwable error) {
								// 加载失败
								PageDataBean bean = new PageDataBean();
								bean.mDataType = PageDataBean.HOME_FEATURE_DATATYPE;
								bean.mUrl = idUrl;
								bean.mPageIndex = 1;
								bean.mStatuscode = 2;
								bean.mMessage = error.getMessage();
								HomeFeatureController.this
										.sendSuccessMessage(bean);
							}

							@Override
							public void onFinish() {
							}
						});
			}
		} else if (command.equals(TRANSFORMATION)) {
			HomeFeatureDataBean homeBean = (HomeFeatureDataBean) request
					.getData();
			if (homeBean == null || homeBean.mAppList == null
					|| homeBean.mAppList.size() <= 0) {
				sendSuccessMessage(new ArrayList<TransformationDataBean>());
				return;
			}
			List<TransformationDataBean> ret = new ArrayList<TransformationDataBean>();
			for (int i = 0; i < homeBean.mAppList.size();) {
				TransformationDataBean bean = new TransformationDataBean();
				if (i == 0) {
					bean.mTitle = "应用";
					bean.mSubTitle = "全部应用>";
					bean.mSubTitleListener = new OnClickListener() {

						@Override
						public void onClick(View v) {
							// 跳转到应用导航
							MainActivity.sendHandler(null,
									IDiyFrameIds.NAVIGATIONBAR,
									IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
									TabController.NAVIGATIONAPP, null);
						}
					};
				}
				if (i < homeBean.mAppList.size()) {
					bean.mBean1 = homeBean.mAppList.get(i);
				}
				i++;
				if (i < homeBean.mAppList.size()) {
					bean.mBean2 = homeBean.mAppList.get(i);
				}
				i++;
				if (i < homeBean.mAppList.size()) {
					bean.mBean3 = homeBean.mAppList.get(i);
				}
				i++;
				if (i < homeBean.mAppList.size()) {
					bean.mBean4 = homeBean.mAppList.get(i);
				}
				i++;
				ret.add(bean);
			}
			for (int i = 0; i < homeBean.mGameList.size();) {
				TransformationDataBean bean = new TransformationDataBean();
				if (i == 0) {
					bean.mTitle = "游戏";
					bean.mSubTitle = "全部游戏>";
					bean.mSubTitleListener = new OnClickListener() {

						@Override
						public void onClick(View v) {
							// 跳转到游戏导航
							MainActivity.sendHandler(null,
									IDiyFrameIds.NAVIGATIONBAR,
									IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
									TabController.NAVIGATIONGAME, null);
						}
					};
				}
				if (i < homeBean.mGameList.size()) {
					bean.mBean1 = homeBean.mGameList.get(i);
				}
				i++;
				if (i < homeBean.mGameList.size()) {
					bean.mBean2 = homeBean.mGameList.get(i);
				}
				i++;
				if (i < homeBean.mGameList.size()) {
					bean.mBean3 = homeBean.mGameList.get(i);
				}
				i++;
				if (i < homeBean.mGameList.size()) {
					bean.mBean4 = homeBean.mGameList.get(i);
				}
				i++;
				ret.add(bean);
			}
			sendSuccessMessage(ret);
		}
	}

}
