package com.jiubang.ggheart.appgame.gostore.base.component;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataDownload;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.BaseController;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;

/**
 * 精品推荐页控制器，负责精品推荐页的逻辑处理
 * 
 * @author xiedezhi
 */
//CHECKSTYLE:OFF
public class WallpaperGridController extends BaseController {
	/**
	 * 开始加载下一页
	 */
	public static final int ACTION_NEXT_PAGE = 1001;
	/**
	 * 取消加载下一页
	 */
	public static final int ACTION_CANCLE_NEXT_PAGE = 1002;
	/**
	 * 返回下一页数据
	 */
	public static final int ACTION_NEXT_PAGE_DATA = 1003;
	/**
	 * 当前正在load下一页数据的线程
	 */
	private NextPageRunnable mCurrentRunnable = null;

	public WallpaperGridController(Context context, IModeChangeListener listener) {
		super(context, listener);
	}

	@Override
	protected Object handleRequest(int action, Object parames) {
		if (action == ACTION_NEXT_PAGE) {
			if (mCurrentRunnable != null) {
				mCurrentRunnable.kill();
			}
			Bundle bundle = (Bundle) parames;
			int typeId = bundle.getInt("typeId");
			int pageId = bundle.getInt("pageId");
			int startIndex = bundle.getInt("startIndex");
			mCurrentRunnable = new NextPageRunnable(typeId, pageId, startIndex);
			mCurrentRunnable.run();
			return null;
		}
		if (action == ACTION_CANCLE_NEXT_PAGE) {
			if (mCurrentRunnable != null) {
				mCurrentRunnable.kill();
			}
			return null;
		}
		return null;
	}

	@Override
	public void destory() {

	}

	/**
	 * 加载下一页的线程，可以杀死
	 * 
	 * @author xiedezhi
	 * 
	 */
	private class NextPageRunnable implements Runnable {
		/**
		 * 需要加载数据的分类id
		 */
		private int mTypeId;
		/**
		 * 需要加载的页码
		 */
		private int mPageId;
		/**
		 * 该页第一个应用在列表的位置
		 */
		private int mStartIndex;

		public NextPageRunnable(int typeid, int pageid, int startIndex) {
			mTypeId = typeid;
			mPageId = pageid;
			mStartIndex = startIndex;
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
			final JSONObject postdata = ClassificationDataDownload.getPostJson(mContext,
					typeIds, -1, mPageId, 0);
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
												.getClassificationData(json, postdata, mContext,
														typeIds, mPageId, mStartIndex, true);
										if (beans != null && beans.size() > 0) {
											ClassificationDataBean bean = beans.get(0);
											if (isKilled()) {
												return;
											}
											notifyChange(ACTION_NEXT_PAGE_DATA, STATE_RESPONSE_OK,
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
			notifyChange(ACTION_NEXT_PAGE_DATA, STATE_RESPONSE_OK, null);
			kill();
		}
	}

	/**
	 * 列表元素点击事件处理器
	 * 
	 * @param app
	 *            列表元素对应的应用单元
	 */
	public void onItemClick(Context context, BoutiqueApp app, ArrayList<String> appIds, 
			ArrayList<String> pics, ArrayList<String> icons, ArrayList<String> downloadUrls) {
		if (app == null) {
			return;
		}
		int startType = AppsDetail.START_TYPE_APPRECOMMENDED;
		AppsDetail.gotoWallPaperDirectly(context, startType, app.name, app.info.appid, appIds, pics, icons, downloadUrls);
	}

}
