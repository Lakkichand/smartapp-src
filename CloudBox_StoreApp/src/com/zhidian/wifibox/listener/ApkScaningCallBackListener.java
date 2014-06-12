package com.zhidian.wifibox.listener;

import com.zhidian.wifibox.data.APKInfo;

/**
 * 扫描手机中apk安装包回调接口
 * @author zhaoyl
 *
 */
public interface ApkScaningCallBackListener {
	void callback(APKInfo info);
	void nowback();

}
