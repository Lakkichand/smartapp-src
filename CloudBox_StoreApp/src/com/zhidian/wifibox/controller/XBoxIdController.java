package com.zhidian.wifibox.controller;

import android.util.Log;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.XDataDownload;

/**
 * 极速模式下获取盒子编号
 * @author zhaoyl
 *
 */
public class XBoxIdController extends TACommand{

	public static final String GAIN_BOXID = "GAIN_BOXID";
	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(GAIN_BOXID)) {
			String url = (String) request.getData();
			XDataDownload.getData(url, new AsyncHttpResponseHandler(){

				@Override
				public void onSuccess(String content) {
					super.onSuccess(content);
					//String boxId = DataParser.parseBoxIdData(content);
					String boxId = content;
					XBoxIdController.this.sendSuccessMessage(boxId);
				}

				@Override
				public void onFailure(Throwable error) {
					super.onFailure(error);
					String message = error.getMessage();
					if (message != null) {
						Log.e("XBoxIdController", message);
					}
					XBoxIdController.this.sendFailureMessage(error);
					
				}
				
			});
		}
		
	}

}
