package com.zhidian.wifibox.controller;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.SearchDataBean;

/**
 * 搜索控制器，负责搜索逻辑处理
 * 
 * @author xiedezhi
 * 
 */
public class SearchController extends TACommand {

	/**
	 * 执行搜索命令
	 */
	public static final String START_SEARCH = "SEARCHCONTROLLER_START_SEARCH";

	/**
	 * 搜索关键词自动完成命令
	 */
	public static final String SEARCH_KEY_AUTOCOMPLETE = "SEARCHCONTROLLER_SEARCH_KEY_AUTOCOMPLETE";

	/**
	 * 搜索关键词推荐命令
	 */
	public static final String SEARCH_KEY_RECOMMEND = "SEARCHCONTROLLER_SEARCH_KEY_RECOMMEND";

	@SuppressWarnings("unchecked")
	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(START_SEARCH)) {
			final long t1 = System.currentTimeMillis();
			Map<String, String> data = (HashMap<String, String>) request
					.getData();
			String keyword = data.get("keyword");
			final String pageNo = data.get("pageNo");
			final String url = CDataDownloader.getSearchUrl();

			RequestParams params = new RequestParams();
			params.put("keyword", keyword);
			params.put("pageNo", pageNo);

			// 异步请求数据，返回Jasn格式数据
			CDataDownloader.getPostData(url, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							SearchDataBean bean = new SearchDataBean();
							bean.mUrl = url;
							bean.mPageIndex = Integer.valueOf(pageNo);
							DataParser.parseSearchData(bean, content);
							SearchController.this.sendSuccessMessage(bean);
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							long t2 = System.currentTimeMillis();
							if (t2 - t1 < 500) {
								try {
									Thread.sleep(500 - (t2 - t1));
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							SearchDataBean bean = new SearchDataBean();
							bean.mStatuscode = 2;
							bean.mMessage = error.getMessage();
							SearchController.this.sendSuccessMessage(bean);
						}

						@Override
						public void onFinish() {
						}
					});
		} else if (command.equals(SEARCH_KEY_AUTOCOMPLETE)) {
			Log.e("mytest", "关键词自动完成   ");
			final String keyword = (String) request.getData();
			String url = CDataDownloader.getSearchKeyHelperUrl(keyword);
			// 异步请求数据，返回Jasn格式数据
			CDataDownloader.getData(url, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					// 加载成功
					SearchDataBean bean = new SearchDataBean();
					bean.mKeyword = keyword;
					DataParser.parseSearchKeyData(bean, content);
					SearchController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onStart() {
					Log.e("mytest", "关键词自动完成  onStart ");
				}

				@Override
				public void onFailure(Throwable error) {
					// 加载失败
					Log.e("mytest", "关键词自动完成   加载失败 ");
					SearchDataBean bean = new SearchDataBean();
					bean.mStatuscode = 2;
					bean.mMessage = error.getMessage();
					SearchController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onFinish() {
				}

			});
		} else if (command.equals(SEARCH_KEY_RECOMMEND)) {
			// 异步请求数据，返回Jasn格式数据
			CDataDownloader.getData(CDataDownloader.getSearchKeyRecommendUrl(),
					new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							// 加载成功
							SearchDataBean bean = new SearchDataBean();
							DataParser.parseSearchKeyRecommendData(bean,
									content);
							SearchController.this.sendSuccessMessage(bean);
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							// 加载失败
							SearchDataBean bean = new SearchDataBean();
							bean.mStatuscode = 2;
							bean.mMessage = error.getMessage();
							SearchController.this.sendSuccessMessage(bean);
						}

						@Override
						public void onFinish() {
						}

					});
		}
	}
}
