package com.jiubang.ggheart.data.statistics;

import android.content.Context;

/**
 * 应用类基础信息统计接口
 * 
 * @author zhaojunjie
 * 
 */
public interface IStatistics {
	/**
	 * 记录当前入口
	 * 
	 * @param code
	 */
	public void saveCurrentEnter(Context context, int cntercode);

	/**
	 * 记录当前 界面入口
	 * 
	 * @param context
	 * @param cntercode
	 */
	public void saveCurrentUIEnter(Context context, int cntercode);

	/**
	 * 记录下发次数 和 推荐位置
	 * 
	 * @param index
	 *            推荐位置（列表中位置）
	 */
	public void saveIssued(Context context, String packageName, int index);

	/**
	 * 记录详情点击次数
	 * 
	 * @param times
	 */
	public void saveDetailsClick(Context context, String packageName, int appid, int times);

	/**
	 * 记录下载点击
	 * 
	 * @param times
	 */
	public void saveDownloadClick(Context context, String packageName, int appid, int times);

	/**
	 * 记录下载量(下载成功后记录)
	 * 
	 * @param times
	 */
	public void saveDownloadComplete(Context context, String packageName, String appid, int times);

	/**
	 * 记录下载安装(下载安装成功后记录)
	 * 
	 * @param times
	 */
	public void saveDownloadSetup(Context context, String packageName);

	/**
	 * 记录更新点击
	 * 
	 * @param times
	 */
	public void saveUpdataClick(Context context, String packageName, int appid, int times);

	/**
	 * 记录更新下载量(更新下载成功后记录）
	 * 
	 * @param times
	 */
	public void saveUpdataComplete(Context context, String packageName, String appid, int times);

	/**
	 * 记录更新安装(更新安装成功后记录）
	 * 
	 * @param times
	 */
	public void saveUpdataSetup(Context context, String packageName);

	/**
	 * 保存安装统计
	 * 
	 * @param id
	 * @param name
	 *            应用名称
	 * @param packageName
	 *            包名
	 * @param isUpdate
	 * @param context
	 */
	public void saveAppRecord(Context context, String id, String name, String packageName,
			boolean isUpdate);
}