package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;

/**
 * 幻灯片控制器
 * 
 * @author xiedezhi
 * 
 */
public class BannerController extends TACommand {

	/**
	 * 加载数据
	 */
	public static final String LOAD_DATA = "BANNERCONTROLLER_LOAD_DATA";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(LOAD_DATA)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			final boolean loadLocalDataFirst = Boolean.valueOf(obj[2]);
			if (loadLocalDataFirst) {
				// 情况一：先加载本地数据，再加载网络数据，加载网络数据后不用立即刷新界面
				String content = CDataDownloader.getLocalData(
						TAApplication.getApplication(), idUrl);
				PageDataBean bean = new PageDataBean();
				bean.mDataType = PageDataBean.BANNER_DATATYPE;
				bean.mUrl = idUrl;
				DataParser.parseBannerList(TAApplication.getApplication(),
						bean, content);
				if (bean.mStatuscode == 0 && bean.mBannerList != null
						&& bean.mBannerList.size() > 0) {
					// 如果数据没问题，先把数据发送回去
					BannerController.this.sendSuccessMessage(bean);
					// 再后台更新数据
					CDataDownloader.getData(dataUrl,
							new AsyncHttpResponseHandler() {
								@Override
								public void onSuccess(String content) {
									// 加载成功
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.BANNER_DATATYPE;
									bean.mUrl = idUrl;
									DataParser.parseBannerList(
											TAApplication.getApplication(),
											bean, content);
									// 更新数据
									TabDataManager.getInstance()
											.replacePageData(bean);
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
									bean.mDataType = PageDataBean.BANNER_DATATYPE;
									bean.mUrl = idUrl;
									DataParser.parseBannerList(
											TAApplication.getApplication(),
											bean, content);
									BannerController.this
											.sendSuccessMessage(bean);

								}

								@Override
								public void onStart() {
								}

								@Override
								public void onFailure(Throwable error) {
									// 加载失败
									PageDataBean bean = new PageDataBean();
									bean.mDataType = PageDataBean.BANNER_DATATYPE;
									bean.mUrl = idUrl;
									bean.mStatuscode = 2;
									bean.mMessage = error.getMessage();
									BannerController.this
											.sendSuccessMessage(bean);
								}

								@Override
								public void onFinish() {
								}
							});
				}
			} else {
				// 情况二：加载网络数据并展示
				CDataDownloader.getData(dataUrl,
						new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String content) {
								// 加载成功
								PageDataBean bean = new PageDataBean();
								bean.mDataType = PageDataBean.BANNER_DATATYPE;
								bean.mUrl = idUrl;
								DataParser.parseBannerList(
										TAApplication.getApplication(), bean,
										content);
								BannerController.this.sendSuccessMessage(bean);

							}

							@Override
							public void onStart() {
							}

							@Override
							public void onFailure(Throwable error) {
								// 加载失败
								PageDataBean bean = new PageDataBean();
								bean.mDataType = PageDataBean.BANNER_DATATYPE;
								bean.mUrl = idUrl;
								bean.mStatuscode = 2;
								bean.mMessage = error.getMessage();
								BannerController.this.sendSuccessMessage(bean);
							}

							@Override
							public void onFinish() {
							}
						});
			}
		}
	}

}
