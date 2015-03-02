package com.zhidian.controller;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.bean.CheckFailBean;
import com.zhidian.bean.InstallBean;
import com.zhidian.util.DownloadUrl;
import com.zhidian.wifibox.dao.InstallApkDao;

/**
 * 验证APK记录
 * 
 * @author zhaoyl
 * 
 */
public class CheckFailApkController extends TACommand {

	public static final String SIGN_CHECK = "sign_check";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(SIGN_CHECK)) {
			final InstallBean bean = (InstallBean) request.getData();
			RequestParams params = new RequestParams();
			params.put("boxNum", bean.boxNum);
			params.put("code", bean.code);
			params.put("versionCode", bean.versionCode);
			params.put("downloadUrl", bean.downloadUrl);
			params.put("msg", bean.msg);
			params.put("status", bean.status);

			final InstallApkDao dao = new InstallApkDao(
					TAApplication.getApplication());

			DownloadUrl.getPostData(DownloadUrl.CheckFail, params,
					new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(String content) {
							super.onSuccess(content);
							try {
								JSONObject obj = new JSONObject(content);
								int statusCode = obj.optInt("statusCode", -1);

								if (statusCode == 0) {
									dao.updateData(bean.downloadUrl,
											bean.installTime, "0");
									Log.e("CheckFailApkController",
											"上传验证成功记录成功");
								} else {
									dao.updateData(bean.downloadUrl,
											bean.installTime, "1");
									Log.e("CheckFailApkController",
											"上传验证失败记录失败");
								}
							} catch (JSONException e) {
								e.printStackTrace();
								dao.updateData(bean.downloadUrl,
										bean.installTime, "1");
								Log.e("CheckFailApkController", "上传验证失败记录出现异常");
							}
						}

						@Override
						public void onFailure(Throwable error) {
							super.onFailure(error);
							Log.e("CheckFailApkController", "上传验证失败记录连网失败");
							dao.updateData(bean.downloadUrl, bean.installTime,
									"1");
						}

					});

		}

	}

}
