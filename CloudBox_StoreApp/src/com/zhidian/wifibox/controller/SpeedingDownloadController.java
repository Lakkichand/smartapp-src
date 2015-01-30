package com.zhidian.wifibox.controller;

import java.util.ArrayList;
import java.util.List;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.adapter.SpeedingMandatoryAdapter.SpeedingMandatoryDataBean;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.XDataDownload;
import com.zhidian.wifibox.data.XMustDataBean;

/**
 * 超速下载页面控制器
 * 
 * @author xiedezhi
 * 
 */
public class SpeedingDownloadController extends TACommand {
	/**
	 * 加载装机必备
	 */
	public static final String LOAD_MANDATORY = "SPEEDINGDOWNLOADCONTROLLER_LOAD_MANDATORY";
	/**
	 * 加载热门推荐
	 */
	public static final String LOAD_HOT = "SPEEDINGDOWNLOADCONTROLLER_LOAD_HOT";
	/**
	 * 转化数据bean
	 */
	public static final String TRANSFORMATION = "SPEEDINGDOWNLOADCONTROLLER_TRANSFORMATION";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(LOAD_MANDATORY)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			// 成功和不成功的情况都返回这一页的databean
			XDataDownload.getData(dataUrl, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					// 加载成功
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.SPEEDINGDOWNLOAD_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = 1;
					DataParser.parseXMustList(bean, content);
					SpeedingDownloadController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onStart() {
				}

				@Override
				public void onFailure(Throwable error) {
					// 加载失败
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.SPEEDINGDOWNLOAD_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = 1;
					if (error != null
							&& error.toString().toLowerCase()
									.contains("not found")) {
						// 提示数据正在更新
						bean.mStatuscode = 9;
					} else {
						bean.mStatuscode = 2;
					}
					bean.mMessage = error.getMessage();
					SpeedingDownloadController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onFinish() {
				}
			});
		} else if (command.equals(LOAD_HOT)) {
			String[] obj = (String[]) request.getData();
			final String idUrl = obj[0];
			final String dataUrl = obj[1];
			// 成功和不成功的情况都返回这一页的databean
			XDataDownload.getData(dataUrl, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					// 加载成功
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.SPEEDINGDOWNLOAD_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = 1;
					DataParser.parseXNewList(bean, content);
					SpeedingDownloadController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onStart() {
				}

				@Override
				public void onFailure(Throwable error) {
					// 加载失败
					PageDataBean bean = new PageDataBean();
					bean.mDataType = PageDataBean.SPEEDINGDOWNLOAD_DATATYPE;
					bean.mUrl = idUrl;
					bean.mPageIndex = 1;
					if (error != null
							&& error.toString().toLowerCase()
									.contains("not found")) {
						// 提示数据正在更新
						bean.mStatuscode = 9;
					} else {
						bean.mStatuscode = 2;
					}
					bean.mMessage = error.getMessage();
					SpeedingDownloadController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onFinish() {
				}
			});
		} else if (command.equals(TRANSFORMATION)) {
			List<XMustDataBean> list = (List<XMustDataBean>) request.getData();
			if (list == null) {
				sendSuccessMessage(new ArrayList<SpeedingMandatoryDataBean>());
			} else {
				List<SpeedingMandatoryDataBean> ret = new ArrayList<SpeedingMandatoryDataBean>();
				for (XMustDataBean bean : list) {
					for (int i = 0; i < bean.mAppList.size();) {
						SpeedingMandatoryDataBean sb = new SpeedingMandatoryDataBean();
						if (i == 0) {
							sb.mTitle = bean.name;
						} else {
							sb.mTitle = null;
						}
						if (i < bean.mAppList.size()) {
							sb.mBean1 = bean.mAppList.get(i);
						}
						i++;
						if (i < bean.mAppList.size()) {
							sb.mBean2 = bean.mAppList.get(i);
						}
						i++;
						if (i < bean.mAppList.size()) {
							sb.mBean3 = bean.mAppList.get(i);
						}
						i++;
						if (i < bean.mAppList.size()) {
							sb.mBean4 = bean.mAppList.get(i);
						}
						i++;
						ret.add(sb);
					}
				}
				sendSuccessMessage(ret);
			}
		}
	}

}
