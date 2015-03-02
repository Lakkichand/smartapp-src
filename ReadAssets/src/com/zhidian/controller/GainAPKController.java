package com.zhidian.controller;

import java.util.List;

import android.util.Log;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.bean.DownloadBean;
import com.zhidian.util.DataParser;
import com.zhidian.util.DownloadUrl;

/**
 * 获取APK下载地址
 * 
 * @author zhaoyl
 * 
 */
public class GainAPKController extends TACommand {

	public static final String GAIN_DATA = "gain_data";
	private static final String TAG = GainAPKController.class.getSimpleName();

	@Override
	protected void executeCommand() {
		// 子线程
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(GAIN_DATA)) {
			String url = (String) request.getData();

			DownloadUrl.getData(url, new AsyncHttpResponseHandler() {

				@Override
				public void onSuccess(String content) {
					super.onSuccess(content);
					List<DownloadBean> list = DataParser
							.parserDownload(content);
					if (list != null) {
						for (int i = 0; i < list.size(); i++) {
							Log.e(TAG, i + "---" + list.get(i).downUrl);
						}
					}
					GainAPKController.this.sendSuccessMessage(list);
				}

				@Override
				public void onFailure(Throwable error) {
					super.onFailure(error);
					GainAPKController.this.sendFailureMessage(error);
				}

			});
		}

	}

}
