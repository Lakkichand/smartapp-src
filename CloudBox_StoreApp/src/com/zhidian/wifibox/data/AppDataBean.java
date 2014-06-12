package com.zhidian.wifibox.data;

import java.io.Serializable;

import com.zhidian.wifibox.download.DownloadTask;

/**
 * 单个应用对象信息
 * 
 * @author xiedezhi
 * 
 */
public class AppDataBean  implements Serializable{

	/**
	 * 应用Id
	 */
	public long id;
	/**
	 * 应用名称
	 */
	public String name;
	/**
	 * 应用简介
	 */
	public String explain;
	/**
	 * 应用图标地址
	 */
	public String iconUrl;
	/**
	 * 应用下载总量
	 */
	public long downloads;
	/**
	 * 应用下载路径
	 */
	public String downloadUrl;
	/**
	 * 游戏星级
	 */
	public int score;
	/**
	 * 游戏大小,单位为：KB
	 */
	public int size;
	/**
	 * 包名
	 */
	public String packName;
	/**
	 * 版本
	 */
	public String version;
	/**
	 * 下载状态
	 */
	public int downloadStatus = DownloadTask.NOT_START;
	/**
	 * 已下载百分比
	 */
	public int alreadyDownloadPercent = 0;
}
