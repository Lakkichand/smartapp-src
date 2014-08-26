package com.zhidian.wifibox.data;

/**
 * App激活量信息Bean
 * @author zhaoyl
 *
 */
public class AppctivateCount {
	public String uuId;//手机唯一标识
	public String boxNum;// 盒子编号
	public String downloadSource;//下载来源   0、门店下载 1、非门店下载
	public String appId;//appId
	public String packageName;// 包名
	public String version;//版本号
	public String activateTime;//激活时间
	public String installTime;//安装时间
	public String isNetwork;//是否联网 0、否 1、是
	public String isInsertSD;//是否插入SD卡  0、没 1、有
	public String downloadModel;//下载模式  0、急速 1、普通 2、共享
	public String networkWay;//联网方式
}
