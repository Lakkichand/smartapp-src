package com.zhidian.wifibox.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.db.dao.AppDownloadDao;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.StatisticsUtil;

/**
 * 插件模块 app点击下载量统计
 * 
 * @author zhaoyl
 * 
 */

public class DownloadController extends TACommand {

	public static final String DOWNLOAD = "download";// 第一次下载时
	public static final String SQLITE_DOWNLOAD = "sqlite_download";// 从数据库中读取数据
	private static final String TAG = DownloadController.class.getSimpleName();

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		final AppDownloadCount bean = (AppDownloadCount) request.getData();

		if (command.equals(DOWNLOAD)) {
			RequestParams params = new RequestParams();

			// 先保存到数据库，再做上传处理。
			final AppDownloadDao downloadDao = new AppDownloadDao(
					TAApplication.getApplication());
			downloadDao.saveAppDownloadInfo(bean);

			try {
				JSONObject json = new JSONObject();
				json.put("uuId",
						InfoUtil.getUUID(TAApplication.getApplication()));
				json.put("boxNum", bean.boxNum); // 盒子编号
				json.put("appId", bean.appId);// appId
				json.put("packageName", bean.packageName);// 包名
				json.put("version", bean.version);// 版本号
				// 下载来源 0、门店下载 1、非门店下载
				json.put("downloadSource", bean.downloadSource);
				// 下载模式0、急速 1、普通 2、共享
				json.put("downloadModel", bean.downloadModel);
				json.put("networkWay", bean.networkWay);// 联网方式
				json.put("downloadTime", bean.downloadTime);// 下载时间
				params.put("json", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugDownload(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");

								// 上传成功后，删除数据库中的数据
								downloadDao.deleteData(bean.packageName);
								Log.e(TAG, message);
								if (statusCode == 0) {// 成功

								} else if (statusCode == 1) {

									JSONArray array = json
											.optJSONArray("errorDesc");
									if (array != null) {
										for (int i = 0; i < array.length(); i++) {
											String str = array.getString(i);
											ErrorDesc error = DataParser
													.parseErrorDesc(str); // 解析错误信息
											Log.i(TAG, "错误参数名："
													+ error.parameterName
													+ "错误类型" + error.errorType);
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
							String er = error.getMessage();
							Log.i(TAG, er);
						}

						@Override
						public void onFinish() {
						}
					});
		} else if (command.equals(SQLITE_DOWNLOAD)) {

			RequestParams params = new RequestParams();

			JSONObject json = new JSONObject();
			try {
				json.put("uuId",
						InfoUtil.getUUID(TAApplication.getApplication()));
				json.put("boxNum", bean.boxNum); // 盒子编号
				json.put("appId", bean.appId);// appId
				json.put("packageName", bean.packageName);// 包名
				json.put("version", bean.version);// 版本号
				// 下载来源 0、门店下载 1、非门店下载
				json.put("downloadSource", bean.downloadSource);
				// 下载模式0、急速 1、普通 2、共享
				json.put("downloadModel", bean.downloadModel);
				json.put("networkWay", bean.networkWay);// 联网方式
				json.put("downloadTime", bean.downloadTime);// 下载时间
				params.put("json", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugDownload(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.e(TAG, message);

								// 删除数据库中的数据
								AppDownloadDao appdownloadDao = new AppDownloadDao(
										TAApplication.getApplication());
								appdownloadDao.deleteData(bean.packageName);

								if (statusCode == 0) {// 成功

								} else if (statusCode == 1) {

									JSONArray array = json
											.optJSONArray("errorDesc");
									if (array != null) {
										for (int i = 0; i < array.length(); i++) {
											String str = array.getString(i);
											ErrorDesc error = DataParser
													.parseErrorDesc(str); // 解析错误信息
											Log.i(TAG, "错误参数名："
													+ error.parameterName
													+ "错误类型" + error.errorType);
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
							// 失败
							String er = error.getMessage();
							Log.i(TAG, er);
						}

						@Override
						public void onFinish() {
						}
					});
		}
	}

}
