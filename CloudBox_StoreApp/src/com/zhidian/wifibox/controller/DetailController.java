package com.zhidian.wifibox.controller;

import java.util.List;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.DetailDataBean;
import com.zhidian.wifibox.data.DetailDataBean.RelatedRecommendBean;
import com.zhidian.wifibox.data.TabDataManager;

/**
 * 应用详情控制器，负责DetailView逻辑处理
 * 
 * @author xiedezhi
 * 
 */
public class DetailController extends TACommand {

	public static final String GAIN_NETWORK = "gain_network"; // 从网络获取应用详情数据
	public static final String GAIN_CORRELATION = "gain_correlation"; // 从网络获取相关推荐数据

	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String command = (String) request.getTag();
		final String url = (String) request.getData();
		if (command.equals(GAIN_NETWORK)) {
			final long t1 = System.currentTimeMillis();
			CDataDownloader.getData(url, new AsyncHttpResponseHandler() {

				@Override
				public void onStart() {
					super.onStart();
				}

				@Override
				public void onFinish() {
					super.onFinish();
				}

				@Override
				public void onSuccess(String content) {
					super.onSuccess(content);
					DetailDataBean db = DataParser.parseDetail(content);
					if (db != null) {
						// 保存数据到缓存。
						TabDataManager.getInstance()
								.cacheDataAppDetail(url, db);
					}

					DetailController.this.sendSuccessMessage(db);
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
					DetailController.this.sendFailureMessage(error.getMessage());
				}

			});

		} else if (command.equals(GAIN_CORRELATION)) {// 获取相关推荐

			CDataDownloader.getData(url, new AsyncHttpResponseHandler() {

				@Override
				public void onStart() {
					super.onStart();
				}

				@Override
				public void onFinish() {
					super.onFinish();
				}

				@Override
				public void onSuccess(String content) {
					super.onSuccess(content);
					List<RelatedRecommendBean> list = DataParser
							.parseRelatedData(content);
					if (list != null) {
						// 保存数据到缓存。
						TabDataManager.getInstance()
								.cacheDataRelated(url, list);
					}

					DetailController.this.sendSuccessMessage(list);
				}

				@Override
				public void onFailure(Throwable error) {
					super.onFailure(error);
					DetailController.this.sendFailureMessage(error.getMessage());
				}

			});
		}
	}
}
