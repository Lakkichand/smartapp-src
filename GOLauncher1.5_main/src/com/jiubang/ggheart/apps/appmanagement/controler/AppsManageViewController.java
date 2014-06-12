package com.jiubang.ggheart.apps.appmanagement.controler;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import com.gau.utils.net.HttpAdapter;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.INetRecord;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.AppUtils;
import com.go.util.SortUtils;
import com.jiubang.ggheart.apps.desks.appfunc.appsupdate.AppsListUpdateManager;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.BaseController;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;

public class AppsManageViewController extends BaseController {

	public static final int ACTION_START_REQUEST = 0;

	private Context mContext = null;

	private Handler mHandler = null;

	public static final int MSG_ID_START = 0;
	public static final int MSG_ID_FINISH = 1;
	public static final int MSG_ID_EXCEPTION = 2;

	public AppsManageViewController(Context context, IModeChangeListener listener) {
		super(context, listener);
		mContext = context;

		initHandler();
	}

	@Override
	protected Object handleRequest(int action, Object parames) {
		switch (action) {
			case ACTION_START_REQUEST : {
				getListData();
			}
				break;

			default :
				break;
		}
		return null;
	}

	/**
	 * 请求网络数据
	 */
	private void getListData() {
		// new Thread(new Runnable(){
		// public void run(){
		// 请求网络数据
		AppsListUpdateManager appsListUpdateManager = AppsListUpdateManager.getInstance(mContext);
		if (appsListUpdateManager != null) {
			// 设置监听者
			HttpAdapter httpAdapter = SimpleHttpAdapter.getHttpAdapter(mContext);
			IConnectListener receiver = getConnectListener();
			appsListUpdateManager.startCheckUpdate(httpAdapter, receiver, false);
		}
		// }
		// }).start();
		
		THttpRequest request = null;
		request.setNetRecord(new INetRecord() {
			
			@Override
			public void onTransFinish(THttpRequest arg0, Object arg1, Object arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartConnect(THttpRequest arg0, Object arg1, Object arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onException(Exception arg0, Object arg1, Object arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onConnectSuccess(THttpRequest arg0, Object arg1, Object arg2) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private IConnectListener getConnectListener() {
		IConnectListener receiver = new IConnectListener() {

			@Override
			public void onStart(THttpRequest arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish(THttpRequest arg0, IResponse arg1) {
				// 1、清理统计数据
				// StatisticsAppsInfoData.resetStatisticsAllDataInfos(mContext);
				// 2、可以更新的数据bean
				AppsBean appsBean = null;
				if (arg1 != null) {
					ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) arg1.getResponse();
					if (listBeans != null && listBeans.size() > 0) {
						appsBean = (AppsBean) listBeans.get(0);
						if (appsBean != null && appsBean.mListBeans != null) {
							dataProcessing(appsBean);
						}
					}
				}
				// 通知更新列表界面
				sendMessage(MSG_ID_FINISH, appsBean);
			}

			@Override
			public void onException(THttpRequest arg0, int arg1) {
				// 通知显示无数据
				StatisticsData.saveHttpExceptionDate(mContext, arg0, arg1);
				sendMessage(MSG_ID_EXCEPTION, null);
			}
		};

		return receiver;
	}

	/**
	 * 对下载的数据进行处理，包括过滤，排序和统计
	 * 
	 * @param appsBean
	 */
	private void dataProcessing(final AppsBean appsBean) {
		ArrayList<AppBean> appBeanArrayList = appsBean.mListBeans;
		if (appBeanArrayList != null) {
			filterAppBeans(appBeanArrayList);
			sortAppBeans(appBeanArrayList);

			AppBean appBean = null;
			for (int i = 0; i < appBeanArrayList.size(); i++) {
				appBean = appBeanArrayList.get(i);
				// 统计下发次数 和 推荐位置
				AppManagementStatisticsUtil.getInstance().saveIssued(mContext, appBean.mPkgName,
						i + 1);
				// 统计：应用更新：不当作一次更新点击(times = 0)，但要保存在主表
				AppManagementStatisticsUtil.getInstance().saveUpdataClick(mContext,
						appBean.mPkgName, appBean.mAppId, 0);
			}
		}
	}

	/**
	 * 过滤已经卸载的应用程序
	 * 
	 * @param appListBeans
	 */
	private void filterAppBeans(ArrayList<AppBean> appListBeans) {
		if (appListBeans == null || appListBeans.size() <= 0) {
			return;
		}

		int size = appListBeans.size();
		ArrayList<AppBean> tmpList = new ArrayList<AppBean>(size);
		for (AppBean bean : appListBeans) {
			boolean isExist = AppUtils.isAppExist(mContext, bean.mPkgName);
			if (!isExist) {
				// 如果不存在，则加入临时列表
				tmpList.add(bean);
			}
		}
		for (AppBean item : tmpList) {
			// 把已经卸载或不存在的应用程序从appListBeans中移除
			appListBeans.remove(item);
		}
	}

	private void sortAppBeans(List<AppBean> appBeans) {
		PackageManager pkgMgr = mContext.getPackageManager();
		SortUtils.sort(appBeans, "getAppName", new Class[] { PackageManager.class },
				new Object[] { pkgMgr }, "ASC");
	}

	private void sendMessage(int eventId, Object object) {
		Message msg = new Message();
		msg.arg1 = eventId;
		msg.obj = object;
		mHandler.sendMessage(msg);
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.arg1) {
					case MSG_ID_START :
						break;

					case MSG_ID_FINISH : {
						notifyChange(ACTION_START_REQUEST, MSG_ID_FINISH, msg.obj);
					}
						break;

					case MSG_ID_EXCEPTION : {
						notifyChange(ACTION_START_REQUEST, MSG_ID_EXCEPTION, null);
					}
						break;

					default :
						break;
				}
			}
		};
	}

	@Override
	public void destory() {
		mHandler = null;
	}
}
