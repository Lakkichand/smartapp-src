package com.zhidian.wifibox.javascript;

import android.webkit.JavascriptInterface;

import com.ta.TAApplication;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 提供本地接口给网页获取uuid
 * 
 * @author xiedezhi
 * 
 */
public class UUIDJavaScriptInterface {

	@JavascriptInterface
	public String getUUID() {
		return InfoUtil.getUUID(TAApplication.getApplication());
	}
}
