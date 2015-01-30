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
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.data.SpkStart;
import com.zhidian.wifibox.db.dao.SpkStartDao;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.StatisticsUtil;

/**
 * 插件部分 市场启动接口
 * 
 * @author zhaoyl
 * 
 */

public class MarketStartController extends TACommand {

	private static final String TAG = MarketStartController.class
			.getSimpleName();
	public static final String START_MARKET = "start_market";// 正常情况
	public static final String START_MARKET_SQLITE = "start_market_sqlite";// 从数据库中读取

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(START_MARKET)) {

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String startTime = formatter.format(curDate);
			final SpkStart bean = new SpkStart();
			RequestParams params = new RequestParams();
			final SpkStartDao dao = new SpkStartDao(
					TAApplication.getApplication());

			try {

				bean.boxNum = InfoUtil.getBoxId(TAApplication.getApplication());
				bean.uuId = InfoUtil.getUUID(TAApplication.getApplication());
				bean.startTime = startTime;
				bean.mac = InfoUtil.getLocalMacAddress(TAApplication
						.getApplication());
				JSONObject json = new JSONObject();
				json.put("boxNum", bean.boxNum);// 盒子编号
				json.put("uuId", bean.uuId);
				json.put("startTime", startTime);// 启动时间
				json.put("mac", bean.mac);

				params.put("json", json.toString());

				/**
				 * 先把数据保存到数据库
				 */
				dao.save(bean);

			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugStart(),
					params, new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(String content) {
							super.onSuccess(content);
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);

								/**
								 * 上传成功后，删除数据库中的数据
								 */
								dao.deleteData(bean.startTime);

								if (statusCode == 0) {// 0表示成功
									Log.e(TAG, "发送市场启动数据成功");

								} else if (statusCode == 1) {// 有误
									JSONArray array = json
											.optJSONArray("errorDesc");
									if (array != null) {
										for (int i = 0; i < array.length(); i++) {
											String str = array.getString(i);
											ErrorDesc error = DataParser
													.parseErrorDesc(str);// 解析错误信息
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
						public void onFailure(Throwable error) {
							super.onFailure(error);
						}

					});

		} else if (command.equals(START_MARKET_SQLITE)) {
			final SpkStart bean = (SpkStart) request.getData();
			RequestParams params = new RequestParams();

			try {
				JSONObject json = new JSONObject();
				json.put("boxNum", bean.boxNum);// 盒子编号
				json.put("uuId",
						InfoUtil.getUUID(TAApplication.getApplication()));
				json.put("startTime", bean.startTime);// 启动时间
				json.put("mac", bean.mac);
				params.put("json", json.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			CDataDownloader.getPostData2(CDataDownloader.getpPlugStart(),
					params, new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(String content) {
							super.onSuccess(content);
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								String message = json.optString("message", "");
								Log.e(TAG, message);
								SpkStartDao dao = new SpkStartDao(TAApplication
										.getApplication());
								dao.deleteData(bean.startTime);

							} catch (JSONException e) {
								e.printStackTrace();
							}

						}

						@Override
						public void onFailure(Throwable error) {
							super.onFailure(error);
						}

					});

		}
	}
}
