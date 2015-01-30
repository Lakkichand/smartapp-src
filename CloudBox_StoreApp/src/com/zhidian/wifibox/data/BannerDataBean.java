package com.zhidian.wifibox.data;

/**
 * 单个幻灯片数据
 * 
 * @author xiedezhi
 * 
 */
public class BannerDataBean {
	/**
	 * 标题
	 */
	public String title;
	/**
	 * 类型 1：网页 2：应用 3：专题 4：活动 5：HTML游戏
	 */
	public int type;
	/**
	 * 图片地址
	 */
	public String imgUrl;
	/**
	 * 对于不同的type返回不同的内容，例如 type=1时target为URL，type=2 or 3 时 target为相应的Id
	 */
	public String target;

}
