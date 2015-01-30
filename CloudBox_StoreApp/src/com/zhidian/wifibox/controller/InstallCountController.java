package com.zhidian.wifibox.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.AppInstallBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.db.dao.AppInstallCountDao;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.StatisticsUtil;

/**
 * 插件模块 app安装、卸载量统计
 * 
 * @author zhaoyl
 * 
 */
public class InstallCountController extends TACommand {

	public static final String INSTALLCOUNT = "installcount";// 正常情况
	public static final String SQLITE_INSTALLCOUNT = "sqlite_installcount";// 从数据库中读取数据时
	private static final String TAG = InstallCountController.class
			.getSimpleName();

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		final AppInstallBean installbean = (AppInstallBean) request.getData();
		RequestParams params = new RequestParams();

		try {
			JSONObject json = new JSONObject();
			json.put("uuId", InfoUtil.getUUID(TAApplication.getApplication()));
			json.put("boxNum", installbean.boxNum);// 盒子编号
			json.put("appId", installbean.appId);// appId
			json.put("packageName", installbean.packageName);// 包名
			json.put("version", installbean.version);// 版本号
			// 下载来源 0、门店下载 1、非门店下载
			json.put("downloadSource", installbean.downloadSource);
			json.put("installTime", installbean.installTime);// 操作时间
			// 类型 0、安装 1、卸载
			json.put("installType", installbean.installType);
			json.put("status", installbean.status);// 安装状态 0、失败 1、成功
			// 下载模式0、急速 1、普通 2、共享
			json.put("downloadModel", installbean.downloadModel);
			json.put("networkWay", installbean.networkWay);// 联网方式
			params.put("json", json.toString());
			Log.e(TAG, "正在发送安装数据：" + json.toString());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		if (command.equals(INSTALLCOUNT)) {
			// 先把数据保存到数据库
			final AppInstallCountDao dao = new AppInstallCountDao(
					TAApplication.getApplication());
			dao.saveAppInstallInfo(installbean);
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugInstallAPP(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.e(TAG, message);

								// 删除数据库中的数据
								dao.deleteData(installbean.packageName,
										installbean.installType);

								if (statusCode == 0) {// 成功

								} else if (statusCode == 1) {

									JSONArray array = json
											.optJSONArray("errorDesc");
									if (array != null) {
										for (int i = 0; i < array.length(); i++) {
											String str = array.getString(i);
											ErrorDesc error = DataParser
													.parseErrorDesc(str); // 解析错误信息
											Log.i(TAG, "错误参数："
													+ error.parameterName
													+ "。错误类型" + error.errorType);
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
						}

						@Override
						public void onFinish() {

						}
					});
		} else if (command.equals(SQLITE_INSTALLCOUNT)) {
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugInstallAPP(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.e(TAG, message);

								AppInstallCountDao installDao = new AppInstallCountDao(
										TAApplication.getApplication());
								installDao.deleteData(installbean.packageName,
										installbean.installType);// 删除数据库中的数据
								if (statusCode == 0) {// 成功

								} else if (statusCode == 1) {

									JSONArray array = json
											.optJSONArray("errorDesc");
									if (array != null) {
										for (int i = 0; i < array.length(); i++) {
											String str = array.getString(i);
											ErrorDesc error = DataParser
													.parseErrorDesc(str); // 解析错误信息
											Log.i(TAG, "错误参数："
													+ error.parameterName
													+ "。错误类型" + error.errorType);
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
						}

						@Override
						public void onFinish() {

						}
					});
		}

	}

}
