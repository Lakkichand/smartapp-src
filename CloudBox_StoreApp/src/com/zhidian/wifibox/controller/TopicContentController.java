package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;

/**
 * 专题内容列表控制器，为TopicContentContainer提供数据
 * 
 * @author xiedezhi
 * 
 */
public class TopicContentController extends TACommand {

	/**
	 * 加载下一页命令
	 */
	public static final String NEXT_PAGE = "TOPICCONTENTCONTROLLER_NEXT_PAGE";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(NEXT_PAGE)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			final int pageIndex = Integer.valueOf(obj[2]);
			final long t1 = System.currentTimeMillis();
			// 成功和不成功的情况都返回这一页的databean
			CDataDownloader.getData(dataUrl, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					// 加载成功
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.TOPICCONTENT_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = pageIndex;
					DataParser.parseAppList(TAApplication.getApplication(),
							bean, content);
					TopicContentController.this.sendSuccessMessage(bean);
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
					// 加载失败
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.TOPICCONTENT_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = pageIndex;
					bean.mStatuscode = 2;
					bean.mMessage = error.getMessage();
					TopicContentController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onFinish() {
				}
			});
		}
	}

}
