package com.smartapp.autostartmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;

public class AppActionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String packageName = intent.getData().getSchemeSpecificPart();
		if (context.getPackageName().equals(packageName)) {
			return;
		}
		// 检查应用自启动
		TAApplication.getApplication().doCommand("maincontroller",
				new TARequest(MainController.CHECK_APP, packageName),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						DataBean bean = (DataBean) response.getData();
						if (bean != null && !bean.mIsForbid) {
							Log.e("", bean.mName + "自启动");
							// TODO 保存到新应用列表
						}
						// TODO 从通知栏点进去才展示新应用
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFailure(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}
				}, true, false);
	}

}
