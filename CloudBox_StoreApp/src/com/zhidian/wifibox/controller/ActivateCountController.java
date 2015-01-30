package com.zhidian.wifibox.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

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
import com.zhidian.wifibox.data.AppctivateCount;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.db.dao.AppActivateCountDao;
import com.zhidian.wifibox.db.dao.AppPackageDao;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.StatisticsUtil;

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
			String isSim = "0";
			if (AppUtils.isCanUseSim()) {
				isSim = "1";
			}
			bean.isInsertSD = isSim;

			/**
			 * 为防止在应用被kill掉的情况下，出现数据丢失，先把数据存到数据库，再上传数据。
			 */
			final AppActivateCountDao dao = new AppActivateCountDao(
					TAApplication.getApplication());
			dao.saveAppActivateInfo(bean);

			JSONObject json = new JSONObject();

			try {
				json.put("uuId", bean.uuId);
				json.put("boxNum", bean.boxNum);// 盒子编号
				// 下载来源 0、门店下载 1、非门店下载
				json.put("downloadSource", bean.downloadSource);
				json.put("appId", bean.appId);// appId
				json.put("packageName", bean.packageName);// 包名
				json.put("version", bean.version);// 版本号
				json.put("activateTime", bean.activateTime);// 激活时间
				json.put("installTime", bean.installTime);// 安装时间
				json.put("isNetwork", bean.isNetWork);// 是否联网 0、否 1、是
				json.put("isInsertSD", bean.isInsertSD);// 是否插入SIM卡 0、没 1、有
				// 下载模式0、急速 1、普通 2、共享
				json.put("downloadModel", bean.downloadModel);
				json.put("networkWay", bean.networkWay);// 联网方式
				params.put("json", json.toString()); //
				Log.i("激活数据：", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugActivateAPP(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								// 0、推 1、不推
								int isPust = json.optInt("isPust", -1);
								Log.i(TAG, message);

								dao.deleteData(bean.packageName);// 删除数据库中的数据

								if (statusCode == 0) {// 成功

									if (isPust == 1) {
										AppPackageDao pgdao = new AppPackageDao(
												TAApplication.getApplication());
										pgdao.updateActivit(bean.packageName);// 更新数据库为已激活状态
									}

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
							Log.d("error：", "error");
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
				json.put("uuId",
						InfoUtil.getUUID(TAApplication.getApplication()));
				json.put("boxNum", bean.boxNum);// 盒子编号
				// 下载来源 0、门店下载 1、非门店下载
				json.put("downloadSource", bean.downloadSource);
				json.put("appId", bean.appId);// appId
				json.put("packageName", bean.packageName);// 包名
				json.put("version", bean.version);// 版本号
				json.put("activateTime", bean.activateTime);// 激活时间
				json.put("installTime", bean.installTime);// 安装时间
				json.put("isNetwork", bean.isNetwork);// 是否联网 0、否 1、是
				json.put("isInsertSD", bean.isInsertSD);// 是否插入SIM卡 0、没 1、有
				// 下载模式0、急速 1、普通 2、共享
				json.put("downloadModel", bean.downloadModel);
				json.put("networkWay", bean.networkWay); // 联网状态
				params.put("json", json.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugActivateAPP(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								// 0、推 1、不推
								int isPust = json.optInt("isPust", -1);
								Log.e(TAG, message);

								AppActivateCountDao actiDao = new AppActivateCountDao(
										TAApplication.getApplication());
								actiDao.deleteData(bean.packageName);// 删除数据库中的数据

								if (statusCode == 0) {// 成功

									if (isPust == 1) {
										AppPackageDao dao = new AppPackageDao(
												TAApplication.getApplication());
										dao.updateActivit(bean.packageName);//
									}

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
							// 失败
							Log.d(TAG, error.getMessage());
						}

						@Override
						public void onFinish() {
						}
					});
		}
	}
}
