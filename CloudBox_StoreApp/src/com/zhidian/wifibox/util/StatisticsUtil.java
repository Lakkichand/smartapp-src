package com.zhidian.wifibox.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.MainController;

/**
 * 统计工具类
 * 
 * @author xiedezhi
 * 
 */
public class StatisticsUtil {

	/**
	 * 检查phone表是否已经上传，如果没有上传，则上传phone表
	 */
	public static boolean verifyPhoneTable() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(TAApplication.getApplication());
		boolean b = prefs.getBoolean(Setting.HASREGISTERUSERINFO, false);
		if (!b) {
			TAApplication.getApplication().doCommand(
					TAApplication.getApplication().getString(
							R.string.maincontroller),
					new TARequest(MainController.REGISTER_USERINFO, null),
					new TAIResponseListener() {

						@Override
						public void onSuccess(TAResponse response) {
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onRuning(TAResponse response) {
						}

						@Override
						public void onFinish() {
						}

						@Override
						public void onFailure(TAResponse response) {
						}
					}, true, false);
		}
		return b;
	}
}
