package com.jiubang.ggheart.appgame.base.bean;

import java.util.List;

/**
 * 
 * 一个层级tab栏数据的封装类
 * 
 * @author xiedezhi
 */
public class TabDataGroup {
	// TODO:XIEDEZHI 加一个观察者列表，数据发生改变时通知观察者
	/**
	 * UI2.0新增，是否顶级分类用图标+文字并排排列展现的数据，如果true则第一层数据是categoryData，
	 * 第二层数据是subGroupList,如果false则第一层数据是data
	 */
	public boolean isIconTab = false;
	/**
	 * 这一层tab栏数据是来自哪个分类id
	 */
	public int typeId = -1;
	/**
	 * UI2.0新增，顶级分类用图标+文字并排排列展现的数据，isIconTab为true时有意义
	 */
	public List<CategoriesDataBean> categoryData = null;
	/**
	 * UI2.0新增，第二层的tab栏数据列表，isIconTab为true时有意义
	 */
	public List<TabDataGroup> subGroupList = null;
	/**
	 * 该层级tab栏的标题，isIconTab为false时有意义
	 */
	public String title = null;
	/**
	 * tab栏数据，isIconTab为false有意义
	 */
	public List<ClassificationDataBean> data = null;
	/**
	 * 当前tab展示的位置
	 */
	public int position = 0;
	/**
	 * coverflow数据，因为coverflow不是页面展示，所以数据单独放出来，不放在data列表
	 */
	public ClassificationDataBean coverFlowBean = null;
	/**
	 * 广告推荐位数据，因为广告推荐不是页面展示，所以数据单独放出来，不放在data列表
	 */
	public ClassificationDataBean adBean = null;
}
