package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个层级的数据封装
 * 
 * @author xiedezhi
 * 
 */
public class TabDataGroup {
	/**
	 * 下标
	 */
	public int index;
	/**
	 * 层级标题
	 */
	public String title;
	/**
	 * 列表页数据
	 */
	public List<PageDataBean> mPageList = new ArrayList<PageDataBean>();

}
