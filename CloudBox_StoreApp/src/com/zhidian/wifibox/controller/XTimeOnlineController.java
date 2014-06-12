package com.zhidian.wifibox.controller;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.XDataDownload;

/**
 * 极速模式下获取上网时间
 * @author zhaoyl
 *
 */

public class XTimeOnlineController extends TACommand {

	public static final String GAIN_TIMEONLINE = "gain_timeonline";
	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(GAIN_TIMEONLINE)) {
			String url = (String) request.getData();
			XDataDownload.getData(url, new AsyncHttpResponseHandler()
			{
				@Override
				public void onSuccess(String content) {
					super.onSuccess(content);
					XTimeOnlineController.this.sendSuccessMessage(content);
					
				}
				
				@Override
				public void onFailure(Throwable error) {
					super.onFailure(error);
					XTimeOnlineController.this.sendFailureMessage(error);
				}
			});
		}

	}

}
