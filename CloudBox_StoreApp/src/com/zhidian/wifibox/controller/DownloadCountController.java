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
import com.zhidian.wifibox.data.AppDownloadBean;
import com.zhidian.wifibox.data.AppDownloadCount;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.db.dao.AppDownloadCountDao;
import com.zhidian.wifibox.db.dao.AppPackageDao;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.StatisticsUtil;

/**
 * 插件模块 app下载成功量统计
 * 
 * @author zhaoyl
 * 
 */
public class DownloadCountController extends TACommand {
	/**
	 * 实时数据
	 */
	public static final String DOWNLOADCOUNT = "downloadcount";
	/**
	 * 从数据库中读取数据
	 */
	public static final String SQLITE_DOWNLOADCOUNT = "sqlite_downloadcount";
	private static final String TAG = DownloadCountController.class
			.getSimpleName();

	@Override
	protected void executeCommand() {

		TARequest request = getRequest();
		String command = (String) request.getTag();
		final AppDownloadCount bean = (AppDownloadCount) request.getData();

		if (command.equals(DOWNLOADCOUNT)) {

			// 保存下载记录到数据库
			AppPackageDao dao = new AppPackageDao(
					TAApplication.getApplication());
			AppDownloadBean dbean = new AppDownloadBean();
			dbean.packageName = bean.packageName;
			dbean.appId = bean.appId;
			dbean.downloadModel = bean.downloadModel;
			dbean.downloadSource = bean.downloadSource;
			dbean.installTime = "";
			dbean.version = bean.version;
			dbean.activit = "0";// 0表示未激活
			dao.savePackageName(dbean);

			// 先保存下载数据到数据库
			final AppDownloadCountDao downloadDao = new AppDownloadCountDao(
					TAApplication.getApplication());
			downloadDao.saveAppDownloadInfo(bean);

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
				// 联网方式
				json.put("networkWay", bean.networkWay);
				// 下载时间
				json.put("downloadTime", bean.downloadTime);
				params.put("json", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugDownloaded(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.e(TAG, message);

								// 删除数据库中的数据
								downloadDao.deleteData(bean.packageName);

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
								// 保存信息到数据库，等待下次再上传
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							// 失败 保存信息到数据库，等待下次再上传
							String er = error.getMessage();
							Log.i(TAG, er);
						}

						@Override
						public void onFinish() {
						}
					});
		} else if (command.equals(SQLITE_DOWNLOADCOUNT)) {

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
			CDataDownloader.getPostData2(CDataDownloader.getpPlugDownloaded(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.e(TAG, message);

								// 删除数据库中的数据
								AppDownloadCountDao appdownloadDao = new AppDownloadCountDao(
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
