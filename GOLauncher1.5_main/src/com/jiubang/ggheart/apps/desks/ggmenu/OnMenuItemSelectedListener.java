package com.jiubang.ggheart.apps.desks.ggmenu;

/**
 * 监听菜单项按钮的监听器
 * 
 * @author ouyongqiang
 * 
 */
public interface OnMenuItemSelectedListener {

	/**
	 * 当菜单项被选中后的回调
	 * 
	 * @param menuItem
	 *            被选中的菜单项
	 * @param index
	 *            菜单项在菜单中的索引位置
	 * @param id
	 *            菜单项的ID
	 */
	public void onMenuItemSelected(int id);
}
