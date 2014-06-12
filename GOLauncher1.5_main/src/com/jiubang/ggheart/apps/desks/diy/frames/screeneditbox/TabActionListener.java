package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;
/**
 * 
 * <br>类描述:tab的响应监听
 * <br>功能详细描述:
 */
public interface TabActionListener {

	/**
	 * 用于刷新 Container 里面的数据
	 * 
	 * @param tabName
	 *            指定tab的flag
	 * @param index
	 *            当前屏的索引 >= 0
	 */
	void onRefreshTab(String tabName, int index);

	/**
	 * 用于刷新二级页面上半部分
	 * 
	 * @param tabName
	 *            指定tab的flag
	 */
	void onRefreshTopBack(String tabName);

	/**
	 * 设置当前tab
	 * 
	 * @param tabName
	 *            指定tab的flag
	 */
	void setCurrentTab(String tabName);

	/**
	 * 点击TAB时切换动作
	 * 
	 * @param tag
	 *            指定tab的flag
	 */
	public void onTabClick(String tag);
}
