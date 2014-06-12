package com.jiubang.ggheart.data;

/**
 * 设置的观察者TODO:移到公用目录
 * 
 * @author guodanyang
 * 
 */
public interface ISettingObserver {
	/**
	 * TODO:修改参数
	 * 
	 * @param index
	 *            改变的设置 如果是boolean类型 0:真 1:假
	 * @param object
	 *            改变后的新数据
	 */
	public void onSettingChange(int index, int value, Object object);
}
