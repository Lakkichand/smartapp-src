package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

public class SearchDataBean {

	/**
	 * 0=请求成功，1=服务器内部错误，-1=还没开始请求数据
	 */
	public int mStatuscode = -1;
	/**
	 * 服务器返回的信息
	 */
	public String mMessage;

	/**
	 * 当前页数，从1开始
	 */
	public int mPageIndex;

	/**
	 * 列表总页数
	 */
	public int mTotalPage;

	/**
	 * 关键词
	 */
	public String mKeyword;

	/**
	 * 该页面的请求URL，这个应该是第一页的请求URL，用于区别其他的页面
	 */
	public String mUrl;

	/**
	 * 搜索应用结果列表
	 */
	public List<AppDataBean> mAppList = new ArrayList<AppDataBean>();

	/**
	 * 搜索关键词列表
	 */
	public List<AppDataBean> mAutoSearchKeyList = new ArrayList<AppDataBean>();
	/**
	 * 搜索专题
	 */
	public List<TopicDataBean> mTopicDataList = new ArrayList<TopicDataBean>();
	/**
	 * 搜索关键词推荐列表
	 */
	public List<String> mSearchKeyList = new ArrayList<String>();
	

}
