package com.zhidian.wifibox.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;

import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.util.FileUtil;

/**
 * 普通模式下数据下载工具类
 * 
 * 下载第一页数据时要把数据缓存到文件，其他页不需要
 * 
 * @author xiedezhi
 * 
 */
public class CDataDownloader {

//	 public static final String BASE_URL =
//	 "http://192.168.3.27:8080/CloudBox_StoreServer";
//	 public static final String ADT_BASE_URL =
//	 "http://192.168.3.27:8080/CloudBox_ReceiveServer/";// 插件模块地址

	public static final String BASE_URL = "http://storeserver.yimipingtai.cn";
	public static final String ADT_BASE_URL = "http://receiveserver.yimipingtai.cn/";// 插件模块地址
   
	// public static final String BASE_URL =
	// "http://192.168.0.193:8080/cloudbox";//此路径用于测试搜索功能接口

	/**
	 * 生成幻灯片数据URL
	 * 
	 * @param mark
	 *            推广幻灯片的标识
	 */
	public static String getBannerUrl(String mark) {
		return BASE_URL + "/apps/spread/slide/list.shtml?code=" + mark;
	}

	/**
	 * 软件最热排行列表URL
	 * 
	 * @param pageNo
	 *            页码
	 */
	public static String getHotAppUrl(int pageNo) {
		return BASE_URL + "/apps/software/rank/hotest.shtml?pageNo=" + pageNo;
	}

	/**
	 * 软件最新排行列表URL
	 */
	public static String getLastAppUrl(int pageNo) {
		return BASE_URL + "/apps/software/rank/latest.shtml?pageNo=" + pageNo;
	}

	/**
	 * 软件好评排行列表URL
	 */
	public static String getPopularAppUrl(int pageNo) {
		return BASE_URL + "/apps/software/rank/popular.shtml?pageNo=" + pageNo;
	}

	/**
	 * 软件升温排行列表URL
	 */
	public static String getWarmingAppUrl(int pageNo) {
		return BASE_URL + "/apps/software/rank/warming.shtml?pageNo=" + pageNo;
	}

	/**
	 * 软件推荐列表URL
	 */
	public static String getRecommendAppUrl(int pageNo) {
		return BASE_URL + "/apps/software/recommend/list.shtml?pageNo="
				+ pageNo;
	}

	/**
	 * 游戏最热排行列表URL
	 */
	public static String getHotGameUrl(int pageNo) {
		return BASE_URL + "/apps/game/rank/hotest.shtml?pageNo=" + pageNo;
	}

	/**
	 * 游戏最新排行列表URL
	 */
	public static String getLastGameUrl(int pageNo) {
		return BASE_URL + "/apps/game/rank/latest.shtml?pageNo=" + pageNo;
	}

	/**
	 * 游戏好评排行列表URL
	 */
	public static String getPopularGameUrl(int pageNo) {
		return BASE_URL + "/apps/game/rank/popular.shtml?pageNo=" + pageNo;
	}

	/**
	 * 游戏升温排行列表URL
	 */
	public static String getWarmingGameUrl(int pageNo) {
		return BASE_URL + "/apps/game/rank/warming.shtml?pageNo=" + pageNo;
	}

	/**
	 * 游戏推荐列表URL
	 */
	public static String getRecommendGameUrl(int pageNo) {
		return BASE_URL + "/apps/game/recommend/list.shtml?pageNo=" + pageNo;
	}

	/**
	 * 详情URL
	 */
	public static String getDetailUrl(long appid) {
		return BASE_URL + "/apps/detail/" + appid + ".shtml";
	}
	
	/**
	 * 详情相关推荐应用URL
	 */
	public static String getCorrelationlUrl(long appid) {
		return BASE_URL + "/apps/relatedRecommend/" + appid + ".shtml";
	}

	/**
	 * 应用评论列表URL
	 */
	public static String getCommentUrl() {
		return BASE_URL + "/apps/comment/list.shtml";

	}

	/**
	 * 发表应用评论URL
	 */
	public static String getDoCommentUrl() {
		return BASE_URL + "/apps/comment/publish.shtml";

	}

	/**
	 * 专题URL
	 */
	public static String getTopicUrl(int pageNo) {
		return BASE_URL + "/apps/special/list.shtml?pageNo=" + pageNo;
	}

	/**
	 * 专题内容URL
	 */
	public static String getTopicContentUrl(long specialId, int pageNo) {
		return BASE_URL + "/apps/special/detail.shtml?specialId=" + specialId
				+ "&pageNo=" + pageNo;
	}

	/**
	 * 是否专题内容url
	 */
	public static boolean isTopicContentUrl(String url) {
		return url.startsWith(BASE_URL
				+ "/apps/special/detail.shtml?specialId=");
	}

	/**
	 * 软件分类列表
	 */
	public static String getCategoryAppUrl() {
		return BASE_URL + "/apps/software/category/list.shtml";
	}

	/**
	 * 游戏分类列表
	 */
	public static String getCategoryGameUrl() {
		return BASE_URL + "/apps/game/category/list.shtml";
	}

	/**
	 * 分类内容URL
	 */
	public static String getCategoryContentUrl(long categoryId, int pageNo) {
		// 分类内容URL
		return BASE_URL + "/apps/category/detail.shtml?categoryId="
				+ categoryId + "&pageNo=" + pageNo;
	}

	/**
	 * 是否分类内容URL
	 */
	public static boolean isCategoryContentUrl(String url) {
		return url.startsWith(BASE_URL
				+ "/apps/category/detail.shtml?categoryId=");
	}

	/**
	 * 首页推荐URL
	 */
	public static String getFeatureUrl(int pageNo) {
		return BASE_URL + "/apps/home/recommend/list.shtml?pageNo=" + pageNo;
	}

	/**
	 * 搜索关键词URL
	 */
	public static String getSearchUrl() {
		return BASE_URL + "/apps/search/query.shtml";
	}
	
	/**
	 * 应用市场异常接口URL
	 */
	public static String getExceptionUrl() {
		return BASE_URL + "/market/crash/record.shtml";
	}

	/**
	 * 搜索关键词自动完成URL
	 */
	public static String getSearchKeyHelperUrl(String keyword) {
		String url;
		try {
			url = BASE_URL + "/apps/search/keyhelper.shtml?keyword="
					+ URLEncoder.encode(keyword, "utf-8");
			return url;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 搜索关键词推荐URL
	 */
	public static String getSearchKeyRecommendUrl() {
		return BASE_URL + "/apps/search/tabs.shtml";
	}

	/**
	 * 更新可升级应用URL
	 */
	public static String getUpdateAppUrl() {
		return BASE_URL + "/apps/checking/update/list.shtml";
	}

	/**
	 * 首次进入进行用户信息注册的URL
	 */
	public static String getRegisterUrl() {
		return BASE_URL + "/market/first/open.shtml";
	}

	/**
	 * 自动更新URL
	 */
	public static String getAutoUpdateUrl(String version, String mac) {
		return BASE_URL + "/market/version/update.shtml?code=0000&version="
				+ version + "&boxNum=" + mac;
	}

	/**
	 * 反馈接口URL
	 */
	public static String getFeedbackUrl() {
		return BASE_URL + "/market/feedback.shtml";
	}

	/**
	 * 检查是否能连外网的接口
	 */
	public static String getExtranetUrl() {
		return BASE_URL + "/internet/connect/success.shtml";
	}

	/********************** 插件部分begin *********************/

	/**
	 * 市场spk安装接口
	 */
	public static String getpPlugInstall() {
		return ADT_BASE_URL + "apk/install/statistics.api";
	}

	/**
	 * 市场spk卸载接口
	 */
	public static String getpPlugUninstall() {
		return ADT_BASE_URL + "apk/unInstall/statistics.api";
	}

	/**
	 * app下载量统计接口
	 */
	public static String getpPlugDownloaded() {
		return ADT_BASE_URL + "app/download/statistics.api";
	}

	/**
	 * app安装量、卸载量统计接口
	 */
	public static String getpPlugInstallAPP() {
		return ADT_BASE_URL + "app/install/statistics.api";
	}

	/**
	 * App激活量统计接口
	 */
	public static String getpPlugActivateAPP() {
		return ADT_BASE_URL + "app/activate/statistics.api";
	}

	/********************** 插件部分end *********************/
	/**
	 * 同步获取网络数据
	 */
	public static void getData(String url, AsyncHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, handler);
	}

	/**
	 * POST方式请求(同步）
	 */
	public static void getPostData(String url, RequestParams params,
			AsyncHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(url, params, handler);
	}

	/**
	 * POST方式请求(同步）
	 */
	public static void getPostData2(String url, RequestParams params,
			AsyncHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Content-Type", "application/x-www-form-urlencoded");
		client.post(url, params, handler);
	}

	/**
	 * 获取本地的数据
	 */
	public static String getLocalData(Context context, String url) {
		try {
			String fileName = context.getCacheDir().getAbsolutePath() + "/"
					+ url.hashCode();
			byte[] data = FileUtil.getByteFromFile(fileName);
			return new String(data);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

}
