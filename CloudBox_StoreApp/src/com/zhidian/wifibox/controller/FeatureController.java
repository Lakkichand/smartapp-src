package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;

/**
 * 推荐应用页控制器
 * 
 * @author xiedezhi
 * 
 */
public class FeatureController extends TACommand {

	/**
	 * 加载下一页命令
	 */
	public static final String NEXT_PAGE = "NEXT_PAGE";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(NEXT_PAGE)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			final int pageIndex = Integer.valueOf(obj[2]);
			final boolean loadLocalDataFirst = Boolean.valueOf(obj[3]);
			// 如果是先加载本地数据
			if (pageIndex == 1 && loadLocalDataFirst) {
				// 先加载本地数据再后台更新数据，拿到新数据后要刷新对应的页面
				String content = CDataDownloader.getLocalData(
						TAApplication.getApplication(), idUrl);
				PageDataBean bean = new PageDataBean();
				bean.mDataType = PageDataBean.FEATURE_DATATYPE;
				bean.mUrl = idUrl;
				bean.mPageIndex = pageIndex;
				DataParser.parseAppList(TAApplication.getApplication(), bean,
						content);
				if (bean.mStatuscode == 0 && bean.mAppList != null
						&& bean.mAppList.size() > 0) {
					// 如果数据没问题，先把数据发送回去
					FeatureController.this.sendSuccessMessage(bean);
					// 再后台更新数据，再刷新界面
					CDataDownloader.getData(dataUrl,
							new AsyncHttpResponseHandler() {
								@Override
								public void onSuccess(String content) {
									// 加载成功
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.FEATURE_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = pageIndex;
									DataParser.parseAppList(
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
					// 如果数据有问题，直接联网读取数据
					CDataDownloader.getData(dataUrl,
							new AsyncHttpResponseHandler() {
								@Override
								public void onSuccess(String content) {
									// 加载成功
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.FEATURE_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = pageIndex;
									DataParser.parseAppList(
											TAApplication.getApplication(),
											bean, content);
									FeatureController.this
											.sendSuccessMessage(bean);
								}

								@Override
								public void onStart() {
								}

								@Override
								public void onFailure(Throwable error) {
									// 加载失败
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.FEATURE_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = pageIndex;
									bean.mStatuscode = 2;
									bean.mMessage = error.getMessage();
									FeatureController.this
											.sendSuccessMessage(bean);
								}

								@Override
								public void onFinish() {
								}
							});
				}
			} else {
				// 加载下一页
				CDataDownloader.getData(dataUrl,
						new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String content) {
								// 加载成功
								PageDataBean bean = new PageDataBean();
								bean.mDataType = PageDataBean.FEATURE_DATATYPE;
								bean.mUrl = idUrl;
								bean.mPageIndex = pageIndex;
								DataParser.parseAppList(
										TAApplication.getApplication(), bean,
										content);
								FeatureController.this.sendSuccessMessage(bean);
							}

							@Override
							public void onStart() {
							}

							@Override
							public void onFailure(Throwable error) {
								// 加载失败
								PageDataBean bean = new PageDataBean();
								bean.mDataType = PageDataBean.FEATURE_DATATYPE;
								bean.mUrl = idUrl;
								bean.mPageIndex = pageIndex;
								bean.mStatuscode = 2;
								bean.mMessage = error.getMessage();
								FeatureController.this.sendSuccessMessage(bean);
							}

							@Override
							public void onFinish() {
							}
						});
			}
		}
	}
}
