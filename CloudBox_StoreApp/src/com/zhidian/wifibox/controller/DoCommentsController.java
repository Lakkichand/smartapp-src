package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 发表应用评论Controller
 * 
 * @author zhaoyl
 * 
 */

public class DoCommentsController extends TACommand {

	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String url = (String) request.getTag();
		String[] obj = (String[]) request.getData();
		String nickname = obj[0];
		String content = obj[1];
		String score = obj[2];
		String appId = obj[3];
		
		RequestParams params = new RequestParams();
		params.put("uuid", InfoUtil.getUUID(TAApplication.getApplication()));
		params.put("nickname", nickname);
		params.put("content", content);
		params.put("score", score);
		params.put("appID", appId);
		CDataDownloader.getPostData(url, params, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String content) {
				super.onSuccess(content);
				int status = DataParser.parsePublishData(content);
				DoCommentsController.this.sendSuccessMessage(status);
			}

			@Override
			public void onFailure(Throwable error) {
				super.onFailure(error);
				DoCommentsController.this.sendFailureMessage(error.getMessage());
			}
		});

	}

}
