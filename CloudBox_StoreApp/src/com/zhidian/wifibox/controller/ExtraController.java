package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;

/**
 * 应用游戏列表控制器
 * 
 * @author xiedezhi
 * 
 */
public class ExtraController extends TACommand {

	/**
	 * 加载下一页命令
	 */
	public static final String NEXT_PAGE = "EXTRACONTROLLER_NEXT_PAGE";

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
			if (pageIndex == 1 && loadLocalDataFirst) {
				// 先加载本地数据再后台更新数据，拿到新数据后要刷新对应的页面
				String content = CDataDownloader.getLocalData(
						TAApplication.getApplication(), idUrl);
				PageDataBean bean = new PageDataBean();
				bean.mDataType = PageDataBean.EXTRA_DATATYPE;
				bean.mUrl = idUrl;
				bean.mPageIndex = pageIndex;
				DataParser.parseAppList(TAApplication.getApplication(), bean,
						content);
				if (bean.mStatuscode == 0 && bean.mAppList != null
						&& bean.mAppList.size() > 0) {
					ExtraController.this.sendSuccessMessage(bean);
					// 连网获取数据
					CDataDownloader.getData(dataUrl,
							new AsyncHttpResponseHandler() {
								@Override
								public void onSuccess(String content) {
									// 加载成功
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.EXTRA_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = pageIndex;
									DataParser.parseAppList(
											TAApplication.getApplication(),
											bean, content);
									// 更新数据
									TabDataManager.getInstance()
											.replacePageData(bean);
									// 刷新界面
									TAApplication.sendHandler(null,
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
									bean.mDataType = PageDataBean.EXTRA_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = pageIndex;
									DataParser.parseAppList(
											TAApplication.getApplication(),
											bean, content);
									ExtraController.this
											.sendSuccessMessage(bean);
								}

								@Override
								public void onStart() {
								}

								@Override
								public void onFailure(Throwable error) {
									// 加载失败
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.EXTRA_DATATYPE;
									bean.mUrl = idUrl;
									bean.mPageIndex = pageIndex;
									bean.mStatuscode = 2;
									bean.mMessage = error.getMessage();
									ExtraController.this
											.sendSuccessMessage(bean);
								}

								@Override
								public void onFinish() {
								}
							});
				}
			} else {
				final long t1 = System.currentTimeMillis();
				// 连网获取数据
				CDataDownloader.getData(dataUrl,
						new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String content) {
								// 加载成功
								PageDataBean bean = new PageDataBean();
								bean.mDataType = PageDataBean.EXTRA_DATATYPE;
								bean.mUrl = idUrl;
								bean.mPageIndex = pageIndex;
								DataParser.parseAppList(
										TAApplication.getApplication(), bean,
										content);
								ExtraController.this.sendSuccessMessage(bean);
							}

							@Override
							public void onStart() {
							}

							@Override
							public void onFailure(Throwable error) {
								long t2 = System.currentTimeMillis();
								if (t2 - t1 < 500) {
									try {
										Thread.sleep(500 - (t2 - t1));
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								// 加载失败
								PageDataBean bean = new PageDataBean();
								bean.mDataType = PageDataBean.EXTRA_DATATYPE;
								bean.mUrl = idUrl;
								bean.mPageIndex = pageIndex;
								bean.mStatuscode = 2;
								bean.mMessage = error.getMessage();
								ExtraController.this.sendSuccessMessage(bean);
							}

							@Override
							public void onFinish() {
							}
						});
			}
		}
	}

}
