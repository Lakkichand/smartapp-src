package com.zhidian.wifibox.controller;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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

/**
 * 插件模块 app安装、卸载量统计
 * 
 * @author zhaoyl
 * 
 */
public class InstallCountController extends TACommand {

	public static final String INSTALLCOUNT = "installcount";//正常情况
	public static final String SQLITE_INSTALLCOUNT = "sqlite_installcount";//从数据库中读取数据时
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
			json.put("uuId", installbean.uuId);
			json.put("boxNum", installbean.boxNum);// TODO 盒子编号
			json.put("appId",installbean.appId);// TODO appId
			json.put("packageName", installbean.packageName);// TODO 包名
			json.put("version", installbean.version);// TODO 版本号
			json.put("downloadSource", installbean.downloadSource);// TODO 下载来源   0、门店下载 1、非门店下载
			json.put("installTime", installbean.installTime);// TODO 操作时间
			json.put("installType", installbean.installType);// TODO 类型 0、安装 1、卸载
			json.put("status", installbean.status);// TODO 安装状态 0、失败 1、成功
			json.put("downloadModel", installbean.downloadModel);// TODO 下载模式0、急速 1、普通 2、共享
			json.put("networkWay",installbean.networkWay);// TODO联网方式
			params.put("json", json.toString());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		if (command.equals(INSTALLCOUNT)) {						
			CDataDownloader.getPostData2(CDataDownloader.getpPlugInstallAPP(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.i(TAG, message);

								if (statusCode == 0) {// 成功
//									Looper.prepare();
//									Toast.makeText(
//											TAApplication.getApplication(),
//											"上传安装、卸载数据成功", Toast.LENGTH_SHORT)
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
											Log.i(TAG, "错误参数："
													+ error.parameterName
													+ "。错误类型" + error.errorType);
										}
									}
									
//									Looper.prepare();
//									Toast.makeText(
//											TAApplication.getApplication(),
//											"上传安装、卸载数据失败，原因为参数错误", Toast.LENGTH_SHORT)
//											.show();
//									Looper.loop();
								}
							} catch (Exception e) {
								e.printStackTrace();
								// 失败，保存到数据库
								AppInstallCountDao dao = new AppInstallCountDao(TAApplication.getApplication());
								dao.saveAppInstallInfo(installbean);
								
//								Looper.prepare();
//								Toast.makeText(
//										TAApplication.getApplication(),
//										"上传安装、卸载数据失败，原因为解析错误", Toast.LENGTH_SHORT)
//										.show();
//								Looper.loop();
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							// 失败，保存到数据库
							AppInstallCountDao dao = new AppInstallCountDao(TAApplication.getApplication());
							dao.saveAppInstallInfo(installbean);
							
//							Looper.prepare();
//							Toast.makeText(
//									TAApplication.getApplication(),
//									"上传安装、卸载数据失败，无法连接到服务器", Toast.LENGTH_SHORT)
//									.show();
//							Looper.loop();
						}

						@Override
						public void onFinish() {
							
						}
					});
		}else if(command.equals(SQLITE_INSTALLCOUNT)){
			CDataDownloader.getPostData2(CDataDownloader.getpPlugInstallAPP(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.i(TAG, message);

								AppInstallCountDao installDao = new AppInstallCountDao(TAApplication.getApplication());
								installDao.deleteData(installbean.packageName);//删除数据库中的数据
								if (statusCode == 0) {// 成功
									
//									Looper.prepare();
//									Toast.makeText(
//											TAApplication.getApplication(),
//											"上传数据库中的安装、卸载数据成功", Toast.LENGTH_SHORT)
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
											Log.i(TAG, "错误参数："
													+ error.parameterName
													+ "。错误类型" + error.errorType);
										}
									}
									
//									Looper.prepare();
//									Toast.makeText(TAApplication.getApplication(),
//									"上传数据库中的安装、卸载数据失败，原因参数错误", Toast.LENGTH_SHORT)
//									.show();
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
							// 失败
//							Looper.prepare();
//							Toast.makeText(TAApplication.getApplication(),
//									"上传数据库中的安装、卸载数据失败，原因无法连接到服务器", Toast.LENGTH_SHORT)
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
