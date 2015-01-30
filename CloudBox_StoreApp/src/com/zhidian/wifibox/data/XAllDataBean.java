package com.zhidian.wifibox.data;

import com.zhidian.wifibox.download.DownloadTask;

/**
 * 极速模式全部应用列表数据结构
 * 
 * @author xiedezhi
 * 
 */
public class XAllDataBean {
	/**
	 * 应用id
	 */
	public int id;
	/**
	 * 应用名字
	 */
	public String name;
	/**
	 * 图标地址
	 */
	public String iconPath;
	/**
	 * 下载地址
	 */
	public String downPath;
	/**
	 * 该应用的大小，单位是KB
	 */
	public int size = 0;
	/**
	 * 该应用的包名
	 */
	public String packName = "";
	/**
	 * 版本
	 */
	public String version = "";
	/**
	 * 下载状态
	 */
	public int downloadStatus = DownloadTask.NOT_START;
	/**
	 * 已下载百分比
	 */
	public int alreadyDownloadPercent = 0;
	/**
	 * 下载速度
	 */
	public long speed = 0;
}
