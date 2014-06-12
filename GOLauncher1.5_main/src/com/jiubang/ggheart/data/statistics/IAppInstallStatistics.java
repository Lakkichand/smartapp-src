package com.jiubang.ggheart.data.statistics;

public interface IAppInstallStatistics {

	/**
	 * 通过应用ID及入口确定数据是否存在
	 * 
	 * @param appid
	 * @param entry
	 * @return
	 */
	public boolean isDataExist(String appid, String entry, String classify);

	/**
	 * 创建一条新数据
	 * 
	 * @param pkgName
	 * @param appId
	 * @param appName
	 * @param postion
	 * @param entry
	 */
	public void createData(String pkgName, String appId, String appName, int postion, String entry,
			int type, String classify, String clickTime);

	/**
	 * 应用展示时的综合统计方法，应用在列表里被展示时调用，功能集合了：先查表，已有数据就更新，没数据就创建并加1(
	 * 作用同旧方法的saveUserDataShow）
	 * 
	 * @param pkgName
	 * @param appId
	 * @param appName
	 * @param postion
	 * @param entry
	 */
	public void saveDataWhenShow(String pkgName, String appId, String appName, int postion,
			String entry, String classify);

	/**
	 * 应用详情展示时的综合统计方法，应用在详情界面被展示时调用，功能集合了：先查表，已有数据就更新，没数据就创建并加1(
	 * 作用同旧方法的saveUserDataDetailShow）
	 * 
	 * @param pkgName
	 * @param appId
	 * @param appName
	 * @param entry
	 */
	public void saveDataWhenDetailShow(String pkgName, String appId, String appName, String entry,
			String classify);

	/**
	 * 应用下载点击的综合统计方法，应用下载按钮被点击时调用，功能集合了：先查表，已有数据就更新，没数据就创建并加1(
	 * 作用同旧方法的saveUserDataTouch）
	 * 
	 * @param pkgName
	 * @param appId
	 * @param appName
	 * @param entry
	 */
	public void saveDateWhenTouch(String pkgName, String appId, String appName, int postion,
			boolean isUpdate, String entry, String classify, String clickTime);

	/**
	 * 通过应用ID及入口更新其安装(或更新）点击数（自加1）
	 * 
	 * @param appid
	 *            应用ID
	 * @param entry
	 *            指定入口(如果传空为默认入口）
	 * @param isUpdate
	 *            是否更新
	 */
	public void updateInstallClick(String packageName, String appid, String entry,
			boolean isUpdate, String classify, String clickTime);

	/**
	 * 通过应用ID及入口更新其安装(或更新）数（自加1）
	 * 
	 * @param appid
	 * @param entry
	 * @param isUpdate
	 */
	public void updateInstallCount(String appid, String entry, boolean isUpdate, String classify);

	/**
	 * 使用appid及指定入口更新其应用在列表的展示量（自加1）
	 * 
	 * @param appid
	 *            应用ID
	 * @param entry
	 *            指定入口(如果传空为默认入口）
	 */
	public void updateAppListShow(String appid, String entry, String classify);

	/**
	 * 使用appid及指定入口更新其应用在详情的展示量（自加1）
	 * 
	 * @param appid
	 *            应用ID
	 * @param entry
	 *            指定入口(如果传空为默认入口）
	 */
	public void updateAppDetailShow(String appid, String entry, String classify);

	/**
	 * 返回所有数据（已并装为可统计数据）的方法
	 * 
	 * @return
	 */
	public String queryAllData();

	/**
	 * 上传成功，清除所有数据
	 */
	public void clearAllData();
}
