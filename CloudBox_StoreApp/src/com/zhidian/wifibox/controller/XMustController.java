package com.zhidian.wifibox.controller;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.XDataDownload;

/**
 * 极速模式装机必备页面控制器
 */
public class XMustController extends TACommand {

	/**
	 * 加载下一页命令
	 */
	public static final String NEXT_PAGE = "XNEWCONTROLLER_NEXT_PAGE";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(NEXT_PAGE)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			// 成功和不成功的情况都返回这一页的databean
			XDataDownload.getData(dataUrl, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					// 加载成功
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.XMUST_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = 1;
					DataParser.parseXMustList(bean, content);
					XMustController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onStart() {
				}

				@Override
				public void onFailure(Throwable error) {
					// 加载失败
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.XMUST_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = 1;
					if (error != null
							&& error.toString().toLowerCase()
									.contains("not found")) {
						// 提示数据正在更新
						bean.mStatuscode = 9;
					} else {
						bean.mStatuscode = 2;
					}
					bean.mMessage = error.getMessage();
					XMustController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onFinish() {
				}
			});
		}
	}
}
