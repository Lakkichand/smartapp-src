package com.zhidian.wifibox.data;

/**
 * 应用分类数据单元
 * 
 * @author xiedezhi
 * 
 */
public class CategoriesDataBean {
	/**
	 * 分类Id
	 */
	public long id;
	/**
	 * 分类名称
	 */
	public String name;
	/**
	 * 图片url
	 */
	public String iconUrl;
	/**
	 * 分类简介
	 */
	public String explain;
	/**
	 * 1:app 2：html5游戏
	 */
	public int type;
	/**
	 * 其他跳转目标 (type=2时为Url)
	 */
	public String otherTarget;

}
