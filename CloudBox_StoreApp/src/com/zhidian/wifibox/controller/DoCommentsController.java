package com.zhidian.wifibox.controller;

import org.json.JSONException;
import org.json.JSONObject;

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

	public static final String SEND_COMMENT = "SEND_COMMENT";//发表app评论
	public static final String SEND_FEEDBACK = "SEND_FEEDBACK";//发表反馈意见

	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(SEND_COMMENT)) {

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
			CDataDownloader.getPostData(CDataDownloader.getDoCommentUrl(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							super.onSuccess(content);
							int status = DataParser.parsePublishData(content);
							DoCommentsController.this
									.sendSuccessMessage(status);
						}

						@Override
						public void onFailure(Throwable error) {
							super.onFailure(error);
							DoCommentsController.this.sendFailureMessage(error
									.getMessage());
						}
					});

		} else if (command.equals(SEND_FEEDBACK)) {
			RequestParams params = new RequestParams();
			String[] obj = (String[]) request.getData();
			params.put("contact", obj[0]);
			params.put("content", obj[1]);
			params.put("uuid", InfoUtil.getUUID(TAApplication.getApplication()));
			params.put("imei", InfoUtil.getIMEI(TAApplication.getApplication()));
			CDataDownloader.getPostData(CDataDownloader.getFeedbackUrl(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {

							JSONObject json;
							int statusCode = -1;
							try {
								json = new JSONObject(content);
								statusCode = json.optInt("statusCode", -1);
							} catch (JSONException e) {
								e.printStackTrace();
							}
														
							DoCommentsController.this.sendSuccessMessage(statusCode);
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							DoCommentsController.this.sendFailureMessage(error);
						}

						@Override
						public void onFinish() {
						}
					});
		}
	}

}
