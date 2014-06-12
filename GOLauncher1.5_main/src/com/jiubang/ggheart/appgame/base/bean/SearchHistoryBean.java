package com.jiubang.ggheart.appgame.base.bean;

/**
 * 搜索关键字数据封装类
 * 
 * @author xiedezhi
 * @date [2012-9-12]
 */
public class SearchHistoryBean {
	/**
	 * 搜索关键字类型：历史记录
	 */
	public static final int SEARCH_KEYWORD_TYPE_HISTORY = 34521;
	/**
	 * 搜索关键字类型：服务器下发
	 */
	public static final int SEARCH_KEYWORD_TYPE_NET = 34522;
	/**
	 * 关键字类型
	 */
	public int mType;
	/**
	 * 关键字
	 */
	public String mKeyword;

	public SearchHistoryBean(int type, String key) {
		mType = type;
		mKeyword = key;
	}
}
