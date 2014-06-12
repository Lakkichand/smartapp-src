/**
 * 
 */
package com.jiubang.ggheart.appgame.appcenter.help;

/**
 * 应用中心网络相关常量
 * 
 * @author liguoliang
 * 
 */
public class AppsNetConstant {

	// 应用中心根据分类id取数据的协议版本
	public static final String CLASSIFICATION_INFO_PVERSION = "3.4";
	// 正式服务器地址
	// 应用中心国内域名
	public static final String APP_CENTER_URL_CHINA = "http://goappcenter.3g.net.cn";

	// 应用中心国外域名
	public static final String APP_CENTER_URL_OTHERS = "http://goappcenter.goforandroid.com";

	// 测试后台服务器地址
	// // 应用中心国内域名
//	public static final String APP_CENTER_URL_CHINA = "http://183.61.112.38:8011";
//		
//	// 应用中心国外域名
//	public static final String APP_CENTER_URL_OTHERS = "http://183.61.112.38:8011";

	//	// 长连接测试地址	
	//	public static final String APP_CENTER_URL_CHINA = "http://183.61.112.38:8080";
	//
	//	// 游戏中心国外域名
	//	public static final String APP_CENTER_URL_OTHERS = "http://183.61.112.38:8080";

	// public static final String url =
	// "http://61.145.124.64:8011/recommendedapp/remdinfo.do?rd=1234";

	// 测试智能DNS路由
	// public static final String APP_CENTER_URL_CHINA =
	// "http://goappcenter.3gcdn.cn";
	//
	// public static final String APP_CENTER_URL_OTHERS =
	// "http://goappcenter.3gcdn.cn";

	// 应用中心获取嵌套[tab栏/分类/应用/专题]地址路径
	public static final String APP_CENTER_CLASSIFICATION_PATH = "/recommendedapp/remdinfo.do?rd=";

	// 保存分类id对应mark的preference前綴
	public static final String APP_CENTER_CLASSIFICATION_MARK_PREFIX = "APP_CENTER_CLASSIFICATION_MARK_";

	// 预加载，保存请求新顶级tab栏数据时的"phead"的preference key
	public static final String APP_CENTER_PREVLOAD_PHEAD_MARK = "APP_CENTER_PREVLOAD_PHEAD_MARK";

	//保存加载提示信息的mark值的preference key
	public static final String APP_CENTER_LOADING_TIP_MARK = "APP_CENTER_LOADING_TIP_MARK";

	//保存启动loading页的mark值的preference key
	public static final String APP_CENTER_START_LOADING_MARK = "APP_CENTER_START_LOADING_MARK";

	// 保存点击过的火焰特效应用的preference前綴
	public static final String APP_EFFECT_MARK_PREFIX = "APP_EFFECT_";

	// 应用中心详情地址路径
	public static final String APP_CENTER_DETAIL_PATH = "/recommendedapp/common.do?funid=2&rd=";

	// 应用中心搜索地址路径
	public static final String APP_CENTER_SEARCH_PATH = "/recommendedapp/common.do?funid=3&rd=";

	// 应用中心搜索关键字地址路径
	public static final String APP_CENTER_SEARCH_KEYWORD_PATH = "/recommendedapp/common.do?funid=4&rd=";

	// 应用中心热门搜索关键字请求地址路径
	public static final String APP_CENTER_HOT_SEARCH_KEYWORD_PATH = "/recommendedapp/common.do?funid=5&rd=";

	// 应用中心widget地址路径
	public static final String APP_CENTER_WIDGET_PATH = "/recommendedapp/widgetinfo.do?rd=";

	// 一键装机
	public static final String APP_KITS_PATH = "/recommendedapp/zhuangji.do?rd=";

	//应用中心获取启动loading的请求地址路径
	public static final String APP_START_LOADING_PATH = "/recommendedapp/common.do?funid=6&rd=";

	//应用中心获取loading页提示的请求地址路径
	public static final String APP_LOADING_TIP_PATH = "/recommendedapp/common.do?funid=7&rd=";
	
	//应用中心更多相关推荐的请求地址路径
	public static final String APP_MORE_SIMILAR_APP_PATH = "/recommendedapp/common.do?funid=8&rd=";

	/**
	 * 应用中心国内心跳包地址
	 */
	public static final String HEART_APPCENTER_CHINA = "http://goappcenter.3g.net.cn/recommendedapp/heartbeat.do?rd=";

	/**
	 * 应用中心国外心跳包地址
	 */
	public static final String HEART_APPCENTER_OTHERS = "http://goappcenter.goforandroid.com/recommendedapp/heartbeat.do?rd=";

	/**
	 * 应用中心国内心跳包地址
	 */
	//	public static final String HEART_APPCENTER_CHINA = "http://183.61.112.38:8080/recommendedapp/heartbeat.do?rd=";
	//
	//	/**
	//	 * 应用中心国外心跳包地址
	//	 */
	//	public static final String HEART_APPCENTER_OTHERS = "http://183.61.112.38:8080/recommendedapp/heartbeat.do?rd=";
}