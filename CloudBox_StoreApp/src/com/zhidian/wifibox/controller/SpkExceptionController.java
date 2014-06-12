package com.zhidian.wifibox.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.util.Log;
import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 应用市场异常Controller
 * @author zhaoyl
 *
 */
@SuppressLint("SimpleDateFormat")
public class SpkExceptionController extends TACommand{

	public static final String UPLOADEXCPTION = "uploadexcption";
	private static final String TAG = SpkExceptionController.class.getSimpleName();
	
	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String exception = (String) request.getTag();
		String command = (String) request.getTag();
		if (command.equals(UPLOADEXCPTION)) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String nowTime = formatter.format(curDate);
			
			RequestParams params = new RequestParams();
			params.put("uuid", InfoUtil.getUUID(TAApplication.getApplication()));
			params.put("imei", InfoUtil.getIMEI(TAApplication.getApplication()));
			params.put("mac",
					InfoUtil.getLocalMacAddress(TAApplication.getApplication()));
			params.put("model", InfoUtil.getModel());
			params.put("manufacturer", InfoUtil.getManuFacturer());
			params.put("osVersion", InfoUtil.getVersion());
			params.put("marketVersion", InfoUtil.getVersionName(TAApplication.getApplication()));
			params.put("insertTime", nowTime);
			params.put("content", exception);//异常信息
			
			CDataDownloader.getPostData(CDataDownloader.getExceptionUrl(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								// 成功
								if (statusCode == 0) {
									Log.i(TAG, "上传异常数据成功");
								}else {
									Log.i(TAG, "上传异常数据失败");
								}
								
							} catch (Exception e) {
								e.printStackTrace();
							}
							SpkExceptionController.this.sendSuccessMessage("success");
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							SpkExceptionController.this.sendFailureMessage(error.getMessage());
						}

						@Override
						public void onFinish() {
						}
					});
		}
	}

}
