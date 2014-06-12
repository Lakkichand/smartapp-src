package com.zhidian.wifibox.data;

/**
 * App安装量、卸载量信息Bean
 * @author zhaoyl
 *
 */
public class AppInstallBean {
	public String uuId;//手机唯一标识
	public String boxNum;// 盒子编号
	public String appId;//appId
	public String packageName;// 包名
	public String version;//版本号
	
	public String downloadSource; //来源 0、门店下载 1、非门店下载
	public String installTime;//操作时间
	public String installType;//类型 0、安装 1、卸载
	public String status;//安装状态  0、失败 1、成功
	public String downloadModel;//下载模式  0、急速 1、普通 2、共享
	public String networkWay;//联网方式
	
	public String isNetWork; //是否联网
	public String activateTime; //激活时间
	public String isInsertSD; //是否插入SD卡
	
	

}
