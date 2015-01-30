package com.zhidian.wifibox.data;

/**
 * apk下载量Bean
 * @author zhaoyl
 *
 */
public class AppDownloadCount {
	public String uuId;//手机唯一标识
	public String boxNum;// 盒子编号
	public String downloadSource;//下载来源 0、门店下载 1、非门店下载
	public String appId;//appId
	public String packageName;// 包名
	public String version;//版本号
	public String downloadModel;//下载模式  0、急速 1、普通 2、共享
	public String networkWay;//联网方式
	public String downloadTime;//下载时间
}
