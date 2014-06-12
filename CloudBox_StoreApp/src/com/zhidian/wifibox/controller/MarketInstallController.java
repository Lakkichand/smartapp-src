package com.zhidian.wifibox.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.Setting;

/**
 * 插件部分 市场spk安装控制器 当第一次安装进入应用时调用
 * 
 * @author zhaoyl
 * 
 */
public class MarketInstallController extends TACommand {

	private static final String TAG = MarketInstallController.class
			.getSimpleName();
	public static final String INSTALL_MARKET = "install_market";
	private Setting setting;

	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作.
		setting = new Setting(TAApplication.getApplication());
		String installTime = setting.getString(Setting.INSTALL_TIME);
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(INSTALL_MARKET)) {
			
			RequestParams params = new RequestParams();
			try {
				JSONObject json = new JSONObject();				
				json.put("boxNum", InfoUtil.getBoxId(TAApplication.getApplication()));// 盒子编号
				json.put("uuId", InfoUtil.getUUID(TAApplication.getApplication()));
				json.put("installTime", installTime);// 安装时间
				json.put("installPackageName",
						AppUtils.getAllAppsString(TAApplication.getApplication()));// 已安装包	
				
				params.put("json", json.toString());
				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}		

			CDataDownloader.getPostData2(CDataDownloader.getpPlugInstall(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								// 成功
								if (statusCode == 0) {// 0表示成功
									setting.putBoolean(Setting.INSTALL_STATUS,
											true);//改变状态，以后都不再发送数据
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
