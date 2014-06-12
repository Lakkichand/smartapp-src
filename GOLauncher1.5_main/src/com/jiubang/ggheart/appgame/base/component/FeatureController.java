package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataDownload;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.recommend.AppKitsActivity;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.BaseController;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;

/**
 * 精品推荐页控制器，负责精品推荐页的逻辑处理
 * 
 * @author xiedezhi
 */
public class FeatureController extends BaseController {
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

	public FeatureController(Context context, IModeChangeListener listener) {
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
														typeIds, mPageId, mStartIndex,
														true);
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
	 * 精品应用的数据cellsize要按照一定的顺序排列，不然会导致排版有问题。2.0版本后只支持iphone样式，就是cellsize为5或6的情况。
	 * 
	 * @param origin
	 *            服务器下发的精品数据列表
	 * @return 修复后的精品数据列表(新的列表对象)
	 */
	public List<BoutiqueApp> fixedFeatureData(List<BoutiqueApp> origin) {
		if (origin == null) {
			return null;
		}
		ArrayList<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
		for (int i = 0; i < origin.size(); i++) {
			int cellsize = origin.get(i).cellsize;
			if (cellsize == 5) {
				// 精品推荐页，IPHONE APPSTORE风格
				if (i + 1 >= origin.size()) {
					continue;
				}
				int cellsize1 = origin.get(i + 1).cellsize;
				if (cellsize1 == 5) {
					ret.add(origin.get(i));
					ret.add(origin.get(i + 1));
					i++;
				}
			} else if (cellsize == 6) {
				// 精品推荐页，IPHONE APPSTORE风格
				ret.add(origin.get(i));
			} else {
				Log.e("FeatureDataParser", "Illegal cellsize = " + cellsize);
			}
		}
		for (int i = 0; i < ret.size(); i++) {
			BoutiqueApp app = ret.get(i);
			app.index = i + 1; // 位置信息要从1开始
		}
		return ret;
	}

	/**
	 * 获取精品推荐数据中格子数为5的应用（banner图展示）
	 * 
	 * @param origin
	 *            原始数据
	 * @return 格子数为5的应用数据列表
	 */
	public List<BoutiqueApp> getBannerApp(List<BoutiqueApp> origin) {
		if (origin == null) {
			return null;
		}
		ArrayList<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
		for (BoutiqueApp app : origin) {
			if (app.cellsize == 5) {
				ret.add(app);
			}
		}
		return ret;
	}

	/**
	 * 获取精品推荐数据中格子数为6的应用（应用详细信息展示）
	 * 
	 * @param origin
	 *            原始数据
	 * @return 格子数为6的应用数据列表
	 */
	public List<BoutiqueApp> getApplicationApp(List<BoutiqueApp> origin) {
		if (origin == null) {
			return null;
		}
		ArrayList<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
		for (BoutiqueApp app : origin) {
			if (app.cellsize == 6) {
				ret.add(app);
			}
		}
		return ret;
	}

	/**
	 * 列表元素点击事件处理器
	 * 
	 * @param app
	 *            列表元素对应的应用单元
	 */
	public void onItemClick(Context context, BoutiqueApp app) {
		if (app == null) {
			return;
		}
		if (app.info.effect == 1) {
			DownloadUtil.saveViewedEffectApp(mContext, app.info.packname);
		}
		int acttype = app.acttype;
		switch (acttype) {
			case 1 :// 打开专题应用列表
			{
				// 进入下一级tab栏
				TabController.skipToTheNextTab(app.rid, app.name, -1, true, -1, -1, null);
				break;
			}
			case 2 :// 打开应用详情
			case 3 :
			case 4 : {
				int startType = AppsDetail.START_TYPE_APPRECOMMENDED;
				AppsDetail.jumpToDetail(context, app, startType, app.index, true);
				break;
			}
			case 5 : {
				// 打开一键装机/玩机
				AppManagementStatisticsUtil.getInstance();
				AppManagementStatisticsUtil.saveTabClickData(mContext, app.rid, null);
				// 启动一键装机
				Intent intent = new Intent(mContext, AppKitsActivity.class);
				intent.putExtra(AppKitsActivity.ENTRANCE_KEY, AppKitsActivity.ENTRANCE_ID_CENTER);
				mContext.startActivity(intent);
			}
			default :
				break;
		}
	}

	/**
	 * 发消息安装apk文件
	 * 
	 * @param context
	 * @param fileName
	 *            apk安装包的本地
	 */
	public static void sendMsgToIntall(Context context, BoutiqueApp app, String fileName) {
		if (context == null || TextUtils.isEmpty(fileName)) {
			return;
		}
		AppRecommendedStatisticsUtil.getInstance().saveReadyToInstall(context, app.info.packname,
				app.info.appid, 0, String.valueOf(app.typeid));
		AppsManagementActivity.sendHandler(context, IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.APPS_MANAGEMENT_INSTALL_APP, 0, fileName, null);
	}

}
