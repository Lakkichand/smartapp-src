package com.zhidian.wifibox.data;

import com.zhidian.wifibox.download.DownloadTask;

/**
 * 极速模式应用数据单元
 * 
 * @author xiedezhi
 * 
 */
public class XAppDataBean {
	/**
	 * 应用id
	 */
	public int id;
	/**
	 * 名称
	 */
	public String name;
	/**
	 * 包名
	 */
	public String packageName;
	/**
	 * 当前版本
	 */
	public String version;
	/**
	 * 大小
	 */
	public int size;
	/**
	 * 应用图标路径
	 */
	public String iconPath;
	/**
	 * 下载路径
	 */
	public String downPath;
	/**
	 * 应用评分
	 */
	public int score;
	/**
	 * 下载数
	 */
	public int downloads;
	/**
	 * 应用类型
	 */
	public int type;
	/**
	 * 应用来源
	 */
	public int source;
	/**
	 * 应用简介
	 */
	public String explain;
	/**
	 * 下载速度
	 */
	public long speed = 0;

	/**
	 * 下载状态
	 */
	public int downloadStatus = DownloadTask.NOT_START;
	/**
	 * 已下载百分比
	 */
	public int alreadyDownloadPercent = 0;
	/**
	 * 是否被选中
	 */
	public boolean isSelect = false;
}
