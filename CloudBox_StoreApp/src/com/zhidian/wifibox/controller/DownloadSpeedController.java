package com.zhidian.wifibox.controller;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.DownloadSpeed;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.db.dao.AppDownloadSpeedDao;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.StatisticsUtil;

/**
 * 插件部分---下载速度统计
 * 
 * @author zhaoyl
 * 
 */
public class DownloadSpeedController extends TACommand {

	public static final String DOWNLOAD_SPEED = "download_speed";
	private static final String TAG = DownloadSpeedController.class
			.getSimpleName();

	@SuppressWarnings("unchecked")
	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();

		if (command.equals(DOWNLOAD_SPEED)) {

			List<DownloadSpeed> list = (List<DownloadSpeed>) request.getData();
			JSONArray array = new JSONArray();
			RequestParams params = new RequestParams();
			for (int i = 0; i < list.size(); i++) {
				try {
					DownloadSpeed bean = list.get(i);
					JSONObject json = new JSONObject();
					json.put("unique", bean.unique);
					json.put("uuId", InfoUtil.getUUID(TAApplication.getApplication()));
					json.put("boxNum", bean.boxNum);
					json.put("appId", bean.appId);
					json.put("appName", bean.appName);
					json.put("packageName", bean.packageName);
					json.put("version", bean.version);
					json.put("downloadSource", bean.downloadSource);
					json.put("downloadModel", bean.downloadModel);
					json.put("networkWay", bean.networkWay);
					json.put("downloadTime", bean.time);
					json.put("downloadSpeed", bean.speed);
					json.put("downloadSize", bean.currentSize);
					json.put("appSize", bean.totalSize);
					array.put(json);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			params.put("json", array.toString());
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(
					CDataDownloader.getpPlugDownloadSpeed(), params,
					new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(String content) {
							super.onSuccess(content);

							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.e(TAG, message);
								// 删除数据库中的数据
								AppDownloadSpeedDao dao = new AppDownloadSpeedDao(
										TAApplication.getApplication());
								dao.deleteAllData();
								Log.e(TAG, "删除下载统计数据成功");
								if (statusCode == 0) {// 成功									

								} else if (statusCode == 1) {
									JSONArray array = json
											.optJSONArray("errorDesc");
									if (array != null) {
										for (int i = 0; i < array.length(); i++) {
											String str = array.getString(i);
											ErrorDesc error = DataParser
													.parseErrorDesc(str); // 解析错误信息
											Log.e(TAG, "错误参数名："
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
						public void onFailure(Throwable error) {
							super.onFailure(error);
							Log.e(TAG, error.getMessage().toString());
						}

					});

		}
	}

}
