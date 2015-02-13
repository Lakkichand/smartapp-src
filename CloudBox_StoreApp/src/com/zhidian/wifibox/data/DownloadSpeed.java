package com.zhidian.wifibox.data;

/**
 * 下载速度统计bean
 * @author zhaoyl
 *
 */
public class DownloadSpeed {

	public String unique;//下载唯一标示
	public String boxNum; //盒子编号
	public String uuId;//手机uuId
	public String appId;//应用Id
	public String appName;//应用名称
	public String time;//开始时间
	public String speed;//下载速度	
	public String packageName; //包名
	public String downloadSource; //下载来源
	public String downloadModel; //下载模式  0、急速 1、普通 2、共享
	public String version; //版本号
	public String networkWay;//联网方式
	public String currentSize; //当前已下载大小
	public String totalSize; //应用总大小
}
