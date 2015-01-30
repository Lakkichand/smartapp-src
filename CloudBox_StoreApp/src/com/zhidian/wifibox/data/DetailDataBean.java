package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用详情对像
 * 
 * @author xiedezhi
 * 
 */
public class DetailDataBean {

	/**
	 * 相关推荐
	 */
	public static class RelatedRecommendBean {
		/**
		 * 应用Id
		 */
		public long id;
		/**
		 * 应用名称
		 */
		public String name;
		/**
		 * 应用图标地址
		 */
		public String iconUrl;
	}

	/**
	 * 应用Id
	 */
	public long id;
	/**
	 * 应用名称
	 */
	public String name;
	/**
	 * 应用简介
	 */
	public String explain;
	/**
	 * 应用详情
	 */
	public String description;
	/**
	 * 应用图标地址
	 */
	public String iconUrl;
	/**
	 * 应用下载总量
	 */
	public long downloads;
	/**
	 * 应用下载路径
	 */
	public String downloadUrl;
	/**
	 * 游戏星级
	 */
	public int rating;
	/**
	 * 游戏大小,单位为：KB
	 */
	public int size;
	/**
	 * 应用版本
	 */
	public String version;

	/**
	 * 应用包名
	 */
	public String packageName;
	/**
	 * 语言
	 */
	public String language;
	/**
	 * 更新时间
	 */
	public String updateTime;
	/**
	 * 应用作者
	 */
	public String author;
	/**
	 * 截图图片地址
	 */
	public List<String> screenshotUrls = new ArrayList<String>();
	
	/**
	 * 缩略图地址
	 */
	public List<String> thumbUrls = new ArrayList<String>();
	/**
	 * 相关推荐应用
	 */
	public List<RelatedRecommendBean> relatedApps = new ArrayList<DetailDataBean.RelatedRecommendBean>();
	/**
	 * 下载状态
	 */
	public int downloadStatus;
	/**
	 * 已下载百分比
	 */
	public int alreadyDownloadPercent;
}
