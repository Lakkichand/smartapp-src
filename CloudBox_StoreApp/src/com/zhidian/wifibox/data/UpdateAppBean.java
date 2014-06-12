package com.zhidian.wifibox.data;

/**
 * 可更新应用列表bean
 * 
 * @author zhaoyl
 * 
 */
public class UpdateAppBean {

	public String name; // 应用名称
	public Long id;// 应用Id
	public String iconUrl; // 应用图标地址
	public String downloadUrl; // 应用下载路径
	public Integer size;// 游戏大小,单位为：KB
	public String version; // 版本
	public String packageName; // 包名
	/**
	 * 下载状态
	 */
	public int downloadStatus;
	/**
	 * 已下载百分比
	 */
	public int alreadyDownloadPercent;
}
