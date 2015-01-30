package com.zhidian.wifibox.data;

/**
 * 弹窗推荐
 * 
 * @author zhaoyl
 * 
 */
public class PopupCommend {

	public String imageUrl; // 图片地址
	public int type; // 跳转类型 1:专题 2:活动 3:应用详情页 4:应用分类列表
	public String target; // 当type=1或者3或者4时，值为各自对应的ID，当type=2时，值为URL
	public String title; // 当type=2时，值为网页的title
}
