package com.zhidian.wifibox.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

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
import com.zhidian.wifibox.data.AppctivateCount;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.db.dao.AppActivateCountDao;
import com.zhidian.wifibox.db.dao.AppPackageDao;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 插件模块 app激活量统计
 * 
 * @author zhaoyl
 * 
 */
public class ActivateCountController extends TACommand {

	public static final String ACTIVATECOUNT = "activatecount";// 正常情况
	public static final String SQLITEACTIVATECOUNT = "sqliteactivatecount";
	private static final String TAG = ActivateCountController.class
			.getSimpleName();

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(ACTIVATECOUNT)) {
			RequestParams params = new RequestParams();
			final AppInstallBean bean = (AppInstallBean) request.getData();
			bean.boxNum = InfoUtil.getBoxId(TAApplication.getApplication());
			bean.uuId = InfoUtil.getUUID(TAApplication.getApplication());
			bean.networkWay = CheckNetwork.getAPNType(TAApplication
					.getApplication());
			if (CheckNetwork.isConnect(TAApplication.getApplication())) {
				bean.isNetWork = "1";
			} else {
				bean.isNetWork = "0";
			}

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			bean.activateTime = formatter.format(curDate);
			bean.isInsertSD = AppUtils.isHaveSdCard();

			JSONObject json = new JSONObject();

			try {
				json.put("uuId", bean.uuId);
				json.put("boxNum", bean.boxNum);// TODO 盒子编号
				json.put("downloadSource", bean.downloadSource);// 下载来源 0、门店下载
																// 1、非门店下载
				json.put("appId", bean.appId);// appId
				json.put("packageName", bean.packageName);// 包名
				json.put("version", bean.version);// 版本号
				json.put("activateTime", bean.activateTime);// TODO 激活时间
				json.put("installTime", bean.installTime);// 安装时间
				json.put("isNetwork", bean.isNetWork);// 是否联网 0、否 1、是
				json.put("isInsertSD", bean.isInsertSD);// 是否插入SD卡 0、没 1、有
				json.put("downloadModel", bean.downloadModel);// 下载模式0、急速 1、普通
																// 2、共享
				json.put("networkWay", bean.networkWay);// 联网方式
				params.put("json", json.toString()); //
				Log.i("激活数据：", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			CDataDownloader.getPostData2(CDataDownloader.getpPlugActivateAPP(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								int isPust = json.optInt("isPust", -1);// 0、推
																		// 1、不推
								Log.i(TAG, message);

								if (statusCode == 0) {// 成功

									if (isPust == 1) {
										AppPackageDao dao = new AppPackageDao(
												TAApplication.getApplication());
										dao.updateActivit(bean.packageName);// 更新数据库为已激活状态
									}

									// Looper.prepare();
									// Toast.makeText(TAApplication.getApplication(),
									// "上传激活数据成功", Toast.LENGTH_SHORT)
									// .show();
									// Looper.loop();

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

									// Looper.prepare();
									// Toast.makeText(TAApplication.getApplication(),
									// "上传激活数据失败", Toast.LENGTH_SHORT)
									// .show();
									// Looper.loop();
								}
							} catch (Exception e) {
								e.printStackTrace();
								// 失败，重新发送
								// AppActivateCountDao dao = new
								// AppActivateCountDao(
								// TAApplication.getApplication());
								// dao.saveAppActivateInfo(bean);

								// Looper.prepare();
								// Toast.makeText(TAApplication.getApplication(),
								// "上传激活数据失败，解析异常", Toast.LENGTH_SHORT)
								// .show();
								// Looper.loop();
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							Log.d("error：", "error");
							// 失败，重新发送
							AppActivateCountDao dao = new AppActivateCountDao(
									TAApplication.getApplication());
							dao.saveAppActivateInfo(bean);

							// Looper.prepare();
							// Toast.makeText(TAApplication.getApplication(),
							// "上传激活数据失败，无法连接到服务器", Toast.LENGTH_SHORT)
							// .show();
							// Looper.loop();
						}

						@Override
						public void onFinish() {
						}
					});
		} else if (command.equals(SQLITEACTIVATECOUNT)) {
			RequestParams params = new RequestParams();
			final AppctivateCount bean = (AppctivateCount) request.getData();
			JSONObject json = new JSONObject();

			try {
				json.put("uuId", bean.uuId);
				json.put("boxNum", bean.boxNum);// TODO 盒子编号
				json.put("downloadSource", bean.downloadSource);// 下载来源 0、门店下载
																// 1、非门店下载
				json.put("appId", bean.appId);// appId
				json.put("packageName", bean.packageName);// 包名
				json.put("version", bean.version);// 版本号
				json.put("activateTime", bean.activateTime);// TODO 激活时间
				json.put("installTime", bean.installTime);// 安装时间
				json.put("isNetwork", bean.isNetwork);// 是否联网 0、否 1、是
				json.put("isInsertSD", bean.isInsertSD);// 是否插入SD卡 0、没 1、有
				json.put("downloadModel", bean.downloadModel);// 下载模式0、急速 1、普通
																// 2、共享
				json.put("networkWay", bean.networkWay); // 联网状态
				params.put("json", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			CDataDownloader.getPostData2(CDataDownloader.getpPlugActivateAPP(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								int isPust = json.optInt("isPust", -1);// 0、推
																		// 1、不推
								Log.i(TAG, message);

								AppActivateCountDao actiDao = new AppActivateCountDao(
										TAApplication.getApplication());
								actiDao.deleteData(bean.packageName);// 删除数据库中的数据

								if (statusCode == 0) {// 成功

									// Looper.prepare();
									// Toast.makeText(TAApplication.getApplication(),
									// "上传数据库中的激活数据成功", Toast.LENGTH_SHORT)
									// .show();
									// Looper.loop();

									// if (isPust == 1) {
									// AppPackageDao dao = new AppPackageDao(
									// TAApplication.getApplication());
									// dao.updateActivit(bean.packageName);//
									// 更新数据库为已激活状态
									// }

									// TODO

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

									// Looper.prepare();
									// Toast.makeText(TAApplication.getApplication(),
									// "上传数据库中的激活数据失败，原因：参数错误",
									// Toast.LENGTH_SHORT)
									// .show();
									// Looper.loop();

								}
							} catch (Exception e) {
								e.printStackTrace();
								// Looper.prepare();
								// Toast.makeText(TAApplication.getApplication(),
								// "上传数据库中的激活数据失败，原因：解析出错", Toast.LENGTH_SHORT)
								// .show();
								// Looper.loop();
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							// 失败
							Log.d(TAG, error.getMessage());
							// Looper.prepare();
							// Toast.makeText(TAApplication.getApplication(),
							// "上传数据库中的激活数据失败，原因：无法连接到数据库", Toast.LENGTH_SHORT)
							// .show();
							// Looper.loop();
						}

						@Override
						public void onFinish() {
						}
					});
		}
	}

}
