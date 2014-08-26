package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.Setting;

/**
 * 应用更新控制器，为UpdateContainer提供数据
 * 
 * @author zhaoyl
 * 
 */
public class UpdateController extends TACommand {

	public static final String GAIN_UPDATE_NETWORK = "gain_network"; // 从网络获取数据

	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(GAIN_UPDATE_NETWORK)) {
			final String url = (String) request.getData();
			RequestParams params = new RequestParams();

			params.put("localapps",
					AppUtils.getAllAppsString(TAApplication.getApplication()));
			CDataDownloader.getPostData(url, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {// 成功获取数据
							super.onSuccess(content);
							// 加载成功
							PageDataBean bean = new PageDataBean();
							bean.mDataType = PageDataBean.UPDATEPAGE_DATATYPE;
							bean.mUrl = url;
							bean.mPageIndex = 1;
							DataParser.parseUpdateAppList(
									TAApplication.getApplication(), bean,
									content);
							int count = bean.uAppList == null ? 0 : bean.uAppList
									.size();
							MainActivity.sendHandler(
									null,
									IDiyFrameIds.NAVIGATIONBAR,
									IDiyMsgIds.SHOW_UPDATE_COUNT,
									count, null, null);
							
							Setting setting = new Setting(TAApplication.getApplication());
							setting.putInt(Setting.UPDATE_COUNT, count);
							UpdateController.this.sendSuccessMessage(bean);
						}

						@Override
						public void onFailure(Throwable error) {// 获取数据失败
							super.onFailure(error);
							PageDataBean bean = new PageDataBean();
							bean.mDataType = PageDataBean.UPDATEPAGE_DATATYPE;
							bean.mUrl = url;
							bean.mMessage = error.getMessage();
							UpdateController.this.sendFailureMessage(bean);
						}
					});
		}

	}

}
