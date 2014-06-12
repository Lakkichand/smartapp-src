package com.zhidian.wifibox.view;

import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.download.DownloadTask;

/**
 * 单个页面基类，所有页面都要继承这个接口
 * 
 * @author xiedezhi
 * 
 */
public interface IContainer {

	/**
	 * 当系统有安装，卸载，更新应用等操作时回调该接口
	 * 
	 * @param packName
	 *            安装/卸载/更新的包名
	 */
	public void onAppAction(String packName);

	/**
	 * 返回获取数据的url
	 */
	public String getDataUrl();

	/**
	 * 当收到下载进度更新的消息，把消息发到每个container里
	 */
	public void notifyDownloadState(DownloadTask downloadTask);

	/**
	 * 更新列表数据，如果bean的statuscode为-1，则通知controller去取数据
	 */
	public void updateContent(PageDataBean bean);

	/**
	 * Activity调用onResume
	 */
	public void onResume();

}
