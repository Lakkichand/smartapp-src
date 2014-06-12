/*
 * 文 件 名:  MoreAppsViewController.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-6
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataDownload;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.BaseController;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-6]
 */
public class MoreAppsViewController extends BaseController {

	/**
	 * 开始加载下一页
	 */
	public static int sACTION_NEXT_PAGE = 101;
	/**
	 * 取消加载下一页
	 */
	public static int sACTION_CANCLE_NEXT_PAGE = 102;
	/**
	 * 返回下一页数据
	 */
	public static int sACTION_NEXT_PAGE_DATA = 103;

	private LoadDataRunnable mCurrentRunnable = null;

	public MoreAppsViewController(Context context, IModeChangeListener listener) {
		super(context, listener);
		// TODO Auto-generated constructor stub
	}

	/** {@inheritDoc} */

	@Override
	protected Object handleRequest(int action, Object parames) {
		if (action == sACTION_NEXT_PAGE) {
			if (mCurrentRunnable != null) {
				mCurrentRunnable.kill();
			}
			Bundle bundle = (Bundle) parames;
			int typeId = bundle.getInt("typeId", 101);
			int itp = bundle.getInt("itp", 2);
			int pageId = bundle.getInt("pageId", 1);
			int startIndex = bundle.getInt("startIndex", 1);
			mCurrentRunnable = new LoadDataRunnable(typeId, itp, pageId, startIndex);
			mCurrentRunnable.run();
		} else if (action == sACTION_CANCLE_NEXT_PAGE) {
			if (mCurrentRunnable != null) {
				mCurrentRunnable.kill();
			}
		}
		return null;
	}

	/** {@inheritDoc} */

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-11-6]
	 */
	private class LoadDataRunnable implements Runnable {

		/**
		 * 分类id
		 */
		int mTypeId = 0;

		/**
		 * id类型
		 */
		int mItp = 0;

		int mStartIndex = 1;

		int mPageId = 1;

		public LoadDataRunnable(int typeId, int itp, int pageId, int startIndex) {
			mTypeId = typeId;
			mItp = itp;
			mStartIndex = startIndex;
			mPageId = pageId;
		}

		/**
		 * 该线程是否已被杀死
		 */
		private boolean mIsKilled = false;
		/**
		 * 同步锁
		 */
		private Object mIsKilledLock = new Object();

		/**
		 * 标志该线程已被杀死，后台拿到数据后不做处理
		 */
		public void kill() {
			synchronized (mIsKilledLock) {
				mIsKilled = true;
			}
		}

		/**
		 * 判断当前线程是否已被杀死
		 * 
		 */
		public boolean isKilled() {
			synchronized (mIsKilledLock) {
				return mIsKilled;
			}
		}

		@Override
		public void run() {
			String url = ClassificationDataDownload.getUrl(mContext);
			final int[] typeIds = new int[] { mTypeId };
			final JSONObject postData = ClassificationDataDownload.getPostJson(mContext,
					typeIds, -1, mPageId, mItp);
			THttpRequest request = null;
			try {
				request = new THttpRequest(url, postData.toString().getBytes(),
						new IConnectListener() {

							@Override
							public void onStart(THttpRequest arg0) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onFinish(THttpRequest request, IResponse response) {
								if (response != null && response.getResponse() != null
										&& (response.getResponse() instanceof JSONObject)) {
									try {
										JSONObject json = (JSONObject) response.getResponse();
										List<ClassificationDataBean> beans = ClassificationDataDownload
												.getClassificationData(json, postData, mContext,
														typeIds, mPageId, mStartIndex, false);
										if (beans != null && beans.size() > 0) {
											ClassificationDataBean bean = beans.get(0);
											if (isKilled()) {
												return;
											}
											notifyChange(sACTION_NEXT_PAGE_DATA, STATE_RESPONSE_OK,
													bean);
											kill();
											return;
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								notifyError();
							}

							@Override
							public void onException(THttpRequest arg0, int arg1) {
								notifyError();
							}
						});
			} catch (Exception e) {
				notifyError();
				return;
			}
			if (request != null) {
				// 设置备选url
				try {
					request.addAlternateUrl(ClassificationDataDownload.getAlternativeUrl(mContext));
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
				request.setRequestPriority(Thread.MAX_PRIORITY);
				request.setOperator(new AppJsonOperator());
				request.setNetRecord(new AppGameNetRecord(mContext, false));
				AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(mContext);
				httpAdapter.addTask(request, true);
			}
		}

		private void notifyError() {
			if (isKilled()) {
				return;
			}
			notifyChange(sACTION_NEXT_PAGE_DATA, STATE_RESPONSE_ERR, null);
			kill();
		}
	}
}
