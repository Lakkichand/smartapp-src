package com.zhidian.wifibox.javascript;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.zhidian.wifibox.download.IDownloadInterface;

/**
 * 下载接口，提供给js调用
 * 
 * @author zhaoyl
 * 
 */
public class DownloadJavaScriptInterface {

	private Context mContext;

	public DownloadJavaScriptInterface(Context context) {
		mContext = context;
	}

	/**
	 * 下载app
	 * 
	 * @param downloadUrl
	 * @param iconUrl
	 * @param name
	 * @param size
	 * @param packName
	 * @param id
	 * @param version
	 */
	@JavascriptInterface
	public void downloadOnAndroid(String downloadUrl, String iconUrl,
			String name, int size, String packName, long id, String version) {
		Intent intent = new Intent(IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
		intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_ADD);
		intent.putExtra("url", downloadUrl);
		intent.putExtra("iconUrl", iconUrl);
		intent.putExtra("name", name);
		intent.putExtra("size", size);
		intent.putExtra("packName", packName);
		intent.putExtra("appId", id);
		intent.putExtra("version", version);
		intent.putExtra("page", "活动页");
		mContext.sendBroadcast(intent);
	}
}
