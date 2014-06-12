package com.zhidian.wifibox.data;

/**
 * 单个专题数据对象
 * 
 * @author xiedezhi
 * 
 */
public class TopicDataBean {
	/**
	 * 专题Id
	 */
	public long id;
	/**
	 * 专题标题
	 */
	public String title;
	/**
	 * 专题ICON小图
	 */
	public String iconUrl;
	/**
	 * 专题大图
	 */
	public String bannerUrl;
	/**
	 * 描述
	 */
	public String description;
	/**
	 * 更新时间
	 */
	public String updateTime;
	/**
	 * 专题数量和大小
	 */
	public String message;
	/**
	 * 是否显示专题名字
	 */
	public int isNameVisited;

}
