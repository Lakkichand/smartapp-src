package com.zhidian.wifibox.controller;

import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.TALogger;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;

/**
 * 排行榜数据控制器
 * 
 * @author xiedezhi
 * 
 */
public class RankingController extends TACommand {

	/**
	 * 加载下一页命令
	 */
	public static final String NEXT_PAGE = "RANKINGCONTROLLER_NEXT_PAGE";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(NEXT_PAGE)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			final int pageIndex = Integer.valueOf(obj[2]);
			TALogger.e(TAApplication.getApplication(), "RankingController  dataUrl === " + dataUrl);
			// 成功和不成功的情况都返回这一页的databean
			CDataDownloader.getData(dataUrl, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					// 加载成功
					TALogger.e(TAApplication.getApplication(), "RankingController  content === " + content);

					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.RANKING_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = pageIndex;
					DataParser.parseAppList(TAApplication.getApplication(),
							bean, content);
					RankingController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onStart() {
				}

				@Override
				public void onFailure(Throwable error) {
					// 加载失败
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.RANKING_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = pageIndex;
					bean.mStatuscode = 2;
					bean.mMessage = error.getMessage();
					RankingController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onFinish() {
				}
			});
		}
	}

}
