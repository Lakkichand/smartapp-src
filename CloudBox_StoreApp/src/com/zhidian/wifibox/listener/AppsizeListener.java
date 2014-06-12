package com.zhidian.wifibox.listener;

/**
 * 获取应用大小回调接口
 * @author zhaoyl
 *
 */
public interface AppsizeListener {
	void BackCall(long totalSize, String packname);

}
