package com.zhidian.wifibox.controller;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;

/**
 * 应用评论列表Controller
 * 
 * @author zhaoyl
 * 
 */
public class CommentController extends TACommand {

	// public static final String GAIN_COMMENT_NETWORK = "gain_comment_network";
	// // 从网络获取数据

	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String url = (String) request.getTag();
		String[] obj = (String[]) request.getData();
		String pageNow = obj[0];
		String appId = obj[1];
		
		RequestParams params = new RequestParams();
		params.put("pageNo", pageNow);
		params.put("appID", appId);
		CDataDownloader.getPostData(url, params, new AsyncHttpResponseHandler(){

			@Override
			public void onSuccess(String content) {
				super.onSuccess(content);
				PageDataBean bean = DataParser.parseComment(content);
				CommentController.this.sendSuccessMessage(bean);
			}

			@Override
			public void onFailure(Throwable error) {
				super.onFailure(error);
				CommentController.this.sendFailureMessage(error.getMessage());
			}
			
		});
	}

}
