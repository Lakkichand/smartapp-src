package com.zhidian.wifibox.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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

/**
 * 插件模块 app下载量统计
 * 
 * @author zhaoyl
 * 
 */
public class DownloadCountController extends TACommand {

	public static final String DOWNLOADCOUNT = "downloadcount";// 第一次下载时
	public static final String SQLITE_DOWNLOADCOUNT = "sqlite_downloadcount";// 从数据库中读取数据
	private static final String TAG = DownloadCountController.class
			.getSimpleName();

	@Override
	protected void executeCommand() {

		TARequest request = getRequest();
		String command = (String) request.getTag();
		final AppDownloadCount bean = (AppDownloadCount) request.getData();

		if (command.equals(DOWNLOADCOUNT)) {

			// 保存到数据库
			AppPackageDao dao = new AppPackageDao(
					TAApplication.getApplication());
			AppDownloadBean dbean = new AppDownloadBean();
			dbean.packageName = bean.packageName;
			dbean.appId = bean.appId;
			dbean.downloadModel = bean.downloadModel;
			dbean.downloadSource = bean.downloadSource;
			dbean.installTime = "";
			dbean.version = bean.version;
			dbean.activit = "0";//0表示未激活
			dao.savePackageName(dbean);
			RequestParams params = new RequestParams();

			JSONObject json = new JSONObject();
			try {
				json.put("uuId", bean.uuId);
				json.put("boxNum", bean.boxNum);// TODO // 盒子编号
				json.put("appId", bean.appId);// appId
				json.put("packageName", bean.packageName);// 包名
				json.put("version", bean.version);// 版本号
				json.put("downloadSource", bean.downloadSource);// TODO 下载来源
																// 0、门店下载
																// 1、非门店下载
				json.put("downloadModel", bean.downloadModel);// TODO 下载模式0、急速
																// 1、普通 2、共享
				json.put("networkWay", bean.networkWay);// TODO 联网方式
				json.put("downloadTime", bean.downloadTime);// TODO 下载时间
				params.put("json", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
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
								if (statusCode == 0) {// 成功
									
//									Looper.prepare();
//									Toast.makeText(
//											TAApplication.getApplication(),
//											"上传下载数据成功", Toast.LENGTH_SHORT)
//											.show();
//									Looper.loop();
									
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
									
//									Looper.prepare();
//									Toast.makeText(
//											TAApplication.getApplication(),
//											"上传下载数据失败，原因参数错误", Toast.LENGTH_SHORT)
//											.show();
//									Looper.loop();
								}
							} catch (Exception e) {
								e.printStackTrace();
								// 失败 TODO 保存信息到数据库，等待下次再上传
								AppDownloadCountDao downloadDao = new AppDownloadCountDao(
										TAApplication.getApplication());
								downloadDao.saveAppDownloadInfo(bean);
								
//								Looper.prepare();
//								Toast.makeText(
//										TAApplication.getApplication(),
//										"上传下载数据失败，解析异常", Toast.LENGTH_SHORT)
//										.show();
//								Looper.loop();
								
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							// 失败 TODO 保存信息到数据库，等待下次再上传
							AppDownloadCountDao downloadDao = new AppDownloadCountDao(
									TAApplication.getApplication());
							downloadDao.saveAppDownloadInfo(bean);
							String er = error.getMessage();
							Log.i(TAG, er);
							
//							Looper.prepare();
//							Toast.makeText(
//									TAApplication.getApplication(),
//									"上传下载数据失败，无法连接到服务器", Toast.LENGTH_SHORT)
//									.show();
//							Looper.loop();
							
						}

						@Override
						public void onFinish() {
						}
					});
		} else if (command.equals(SQLITE_DOWNLOADCOUNT)) {

			RequestParams params = new RequestParams();

			JSONObject json = new JSONObject();
			try {
				json.put("uuId", bean.uuId);
				json.put("boxNum", bean.boxNum);// TODO // 盒子编号
				json.put("appId", bean.appId);// appId
				json.put("packageName", bean.packageName);// 包名
				json.put("version", bean.version);// 版本号
				json.put("downloadSource", bean.downloadSource);// TODO 下载来源
																// 0、门店下载
																// 1、非门店下载
				json.put("downloadModel", bean.downloadModel);// TODO 下载模式0、急速
																// 1、普通 2、共享
				json.put("networkWay", bean.networkWay);// TODO 联网方式
				json.put("downloadTime", bean.downloadTime);// TODO 下载时间
				params.put("json", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
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
								
								// 删除数据库中的数据 TODO
								AppDownloadCountDao appdownloadDao = new AppDownloadCountDao(
										TAApplication.getApplication());
								appdownloadDao.deleteData(bean.packageName);
								
								if (statusCode == 0) {// 成功
									
									
//									Looper.prepare();
//									Toast.makeText(
//											TAApplication.getApplication(),
//											"上传数据库中的下载数据成功", Toast.LENGTH_SHORT)
//											.show();
//									Looper.loop();
									

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
									
//									Looper.prepare();
//									Toast.makeText(
//											TAApplication.getApplication(),
//											"上传数据库中下载数据失败，原因为参数错误", Toast.LENGTH_SHORT)
//											.show();
//									Looper.loop();
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
							// 失败 TODO
							String er = error.getMessage();
							Log.i(TAG, er);
							
//							Looper.prepare();
//							Toast.makeText(
//									TAApplication.getApplication(),
//									"上传数据库中下载数据失败，原因为无法连接到服务器", Toast.LENGTH_SHORT)
//									.show();
//							Looper.loop();
						}

						@Override
						public void onFinish() {
						}
					});
		}

	}
}
