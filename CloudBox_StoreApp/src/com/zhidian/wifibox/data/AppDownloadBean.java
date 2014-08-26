package com.zhidian.wifibox.data;

/**
 * 保存已下载的app数据到数据库bean
 * @author zhaoyl
 *
 */
public class AppDownloadBean {

	public String packageName; //包名
	public String appId; 
	public String downloadSource; //下载来源
	public String installTime; //安装时间
	public String version; //版本号
	public String downloadModel; //下载模式  0、急速 1、普通 2、共享
	public String activit; //是否激活，0表示未激活，1表示已激活
	
	
	
}
