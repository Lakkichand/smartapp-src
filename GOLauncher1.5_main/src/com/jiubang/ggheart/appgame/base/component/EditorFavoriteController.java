/*
 * 文 件 名:  EditorFavoriteController.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-8
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
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-8]
 */
//CHECKSTYLE:OFF
public class EditorFavoriteController extends BaseController {

	public static int ACTION_NEXT_PAGE = 1001;

	public static int ACTION_CANCEL_NEXT_PAGE = 1002;

	public static int ACTION_RETURN_DATA = 1003;

	private NextPageRunnable mNextPageRunnable = null;

	public EditorFavoriteController(Context context, IModeChangeListener listener) {
		super(context, listener);
		// TODO Auto-generated constructor stub
	}

	/** {@inheritDoc} */

	@Override
	protected Object handleRequest(int action, Object parames) {
		// TODO Auto-generated method stub
		if (action == ACTION_NEXT_PAGE) {
			Bundle bundle = (Bundle) parames;
			int typeId = bundle.getInt("typeId");
			int currentPage = bundle.getInt("currentPage");
			int startIndex = bundle.getInt("startIndex");
			mNextPageRunnable = new NextPageRunnable(typeId, currentPage,
					startIndex);
			mNextPageRunnable.run();
		} else if (action == ACTION_CANCEL_NEXT_PAGE) {
			if (mNextPageRunnable != null) {
				mNextPageRunnable.kill();
			}
		}
		return null;
	}

	/**
	 * 加载下一页的线程，可以杀死
	 * 
	 * @author liuxinyang
	 * 
	 */
	private class NextPageRunnable implements Runnable {

		private int mTypeId;
		private int mCurrentPage;
		private int mStartIndex;

		public NextPageRunnable(int typeid, int currentPage, int startIndex) {
			mTypeId = typeid;
			mCurrentPage = currentPage;
			mStartIndex = startIndex;
		}

		/**
		 * 该线程是否已被杀死
		 */
		private boolean isKilled = false;
		private Object isKilledLock = new Object();

		/**
		 * 标志该线程已被杀死，后台拿到数据后不做处理
		 */
		public void kill() {
			synchronized (isKilledLock) {
				isKilled = true;
			}
		}

		/**
		 * 判断当前线程是否已被杀死
		 * 
		 */
		public boolean isKilled() {
			synchronized (isKilledLock) {
				return isKilled;
			}
		}

		@Override
		public void run() {
			String url = ClassificationDataDownload.getUrl(mContext);
			final int[] typeIds = new int[] { mTypeId };
			final JSONObject postdata = ClassificationDataDownload.getPostJson(mContext,
					typeIds, -1, mCurrentPage + 1, 0);
			THttpRequest request = null;
			try {
				request = new THttpRequest(url, postdata.toString().getBytes(),
						new IConnectListener() {

							@Override
							public void onStart(THttpRequest arg0) {
							}

							@Override
							public void onFinish(THttpRequest request, IResponse response) {
								if (response != null && response.getResponse() != null
										&& (response.getResponse() instanceof JSONObject)) {
									try {
										JSONObject json = (JSONObject) response.getResponse();
										List<ClassificationDataBean> beans = ClassificationDataDownload
												.getClassificationData(json, postdata, mContext, typeIds, mCurrentPage + 1,
														mStartIndex, true);
										if (beans != null && beans.size() > 0) {
											ClassificationDataBean bean = beans.get(0);
											if (isKilled()) {
												return;
											}
											notifyChange(ACTION_RETURN_DATA, STATE_RESPONSE_OK,
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
			notifyChange(ACTION_RETURN_DATA, STATE_RESPONSE_OK, null);
			kill();
		}
	}

	/** {@inheritDoc} */

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}

}
