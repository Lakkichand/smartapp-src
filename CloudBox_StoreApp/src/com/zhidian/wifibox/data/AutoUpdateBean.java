package com.zhidian.wifibox.data;

/**
 * 自动更新信息
 * 
 * @author xiedezhi
 * 
 */
public class AutoUpdateBean {
	/**
	 * 状态码（0=请求成功，1=服务器内部错误）
	 */
	public int statusCode;
	/**
	 * 信息描述
	 */
	public String message;
	/**
	 * 当前app是否是最新的
	 */
	public boolean isLatest;
	/**
	 * 最新版本
	 */
	public String version;
	/**
	 * 大小 单位为KB
	 */
	public int size;
	/**
	 * 更新描述
	 */
	public String description;
	/**
	 * 更新时间
	 */
	public String updateTime;
	/**
	 * 更新URL
	 */
	public String updateUrl;
	/**
	 * 是否强制更新
	 */
	public boolean isMust;

}
