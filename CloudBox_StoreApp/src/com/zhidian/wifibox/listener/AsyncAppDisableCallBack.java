package com.zhidian.wifibox.listener;

import com.zhidian.wifibox.data.AppInfo;

public interface AsyncAppDisableCallBack {
	void callback(AppInfo info);
	void nowback();
}
