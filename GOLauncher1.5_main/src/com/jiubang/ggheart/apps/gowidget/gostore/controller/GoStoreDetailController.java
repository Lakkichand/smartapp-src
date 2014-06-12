package com.jiubang.ggheart.apps.gowidget.gostore.controller;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.apps.gowidget.gostore.cache.GoStoreCacheManager;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.MainDataHttpOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-9-10]
 */
public class GoStoreDetailController extends BaseController {

	private final static int EVENT_GET_DATA_FINISH = 1; // 获取数据结束
	private final static int EVENT_GET_DATA_EXCEPTION = 2; // 获取数据异常
	// 动作
	public static final int ACTION_NEW_APP_INTALLED = 0;
	public static final int ACTION_GET_DETAIL_DATA = 1;
	public static final int ACTION_NEW_APP_UNINSTALL = 2;
	// 状态
	public static final int STATE_LIST_GET_NO_DATA = 1;
	public static final int STATE_LIST_GET_DATA_FINISH = 2;
	public static final int STATE_LIST_GET_DATA_EXCEPTION = 3;

	private boolean mHasCacheData = false;

	private BroadcastReceiver mBroadcastReceiver = null; // 广播接收器
	private Handler mHandler = null;
	private SimpleHttpAdapter mHttpAdapter = null;

	private GoStoreCacheManager mCacheManager;
	
	// 检查更新的状态
		public static final int STATE_NEW = 0;
		public static final int STATE_START = 1;
		public static final int STATE_CHECKING = 2;
		public static final int STATE_FINISH = 3;

	public GoStoreDetailController(Context context, IModeChangeListener listener) {
		super(context, listener);
		// TODO Auto-generated constructor stub
		if (context != null) {
			mHttpAdapter = SimpleHttpAdapter.getInstance(context.getApplicationContext());
		}
		mCacheManager = GoStoreCacheManager.getInstance();
		// 初始化Handler
		initHandler();
		// 初始化并注册广播接收器
		initAndRegisterReceiver();
	}

	@Override
	protected Object handleRequest(int action, Object parames) {
		// TODO Auto-generated method stub
		switch (action) {
			case ACTION_GET_DETAIL_DATA : {
				ArrayList<NameValuePair> nameValuePairs = null;
				if (parames != null && parames instanceof ArrayList) {
					nameValuePairs = (ArrayList<NameValuePair>) parames;
					if (nameValuePairs != null) {
						String name = nameValuePairs.get(0).getName();
						int size = nameValuePairs.size();
						int state = Integer.parseInt(nameValuePairs.get(size - 1).getValue());
						if (name != null && !"".equals(name) && name.equals("url")) {
							// 如果是url，则直接用url跳转
							String value = nameValuePairs.get(0).getValue();
							nameValuePairs.clear();
							nameValuePairs.add(new BasicNameValuePair("state", Integer.toString(state)));
							getDetailData(value, nameValuePairs,
									GoStorePublicDefine.FUNID_PRODUCT_DETAIL, action);
						} else {
							getDetailData(GoStorePublicDefine.URL_HOST3, nameValuePairs,
									GoStorePublicDefine.FUNID_PRODUCT_DETAIL, action);
						}
					}
				}
			}
				break;

			default :
				break;
		}
		return null;
	}

	/**
	 * 初始化Handler
	 */
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (mIsRecycled || msg == null) {
					return;
				}
				switch (msg.what) {
					case EVENT_GET_DATA_FINISH : {
						boolean hasData = false;
						if (msg.obj != null) {
							ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) msg.obj;
							if (listBeans != null && listBeans.size() > 0) {
								hasData = true;
								notifyChange(msg.arg1, STATE_LIST_GET_DATA_FINISH, listBeans);
							}

						}
						if (!hasData) {
							notifyChange(msg.arg1, STATE_LIST_GET_NO_DATA, null);
						}
					}
						break;
					case EVENT_GET_DATA_EXCEPTION : {
						notifyChange(msg.arg1, STATE_LIST_GET_DATA_EXCEPTION, null);
					}
						break;
					default :
						break;
				}
			}

		};
	}

	/**
	 * 获取详情数据的方法
	 * 
	 * @param url
	 * @param nameValuePairs
	 * @param funid
	 * @param action
	 */
	private void getDetailData(String url, List<NameValuePair> nameValuePairs, int funid, int action) {
		int state = 0;
		if (nameValuePairs != null) {
			int size = 0;
			size = nameValuePairs.size();
			if (size == 1)
			{
				state = Integer.parseInt(nameValuePairs.get(0).getValue());
			}
			else if (size > 1)
			{
				state = Integer.parseInt(nameValuePairs.get(size - 1).getValue());
			}
		}
		THttpRequest request = createHttpRequest(url, nameValuePairs, funid, action);
		if (request != null) {
			if (state == GoStoreCacheManager.STATE_NEW) {
				getCacheDataFromLocal(request, action);
			} else if (state == GoStoreCacheManager.STATE_FINISH && GoStoreCacheManager.sIsServerDataUpdate
					&& !GoStoreCacheManager.sIsFirst) {
				getDetailDataFromNet(request);
			}
		}
	}

	/**
	 * 从本地获取缓存数据的方法
	 * 
	 * @param request
	 * @return
	 */
	private void getCacheDataFromLocal(final THttpRequest request, final int action) {
		if (mCacheManager != null)
		{
			Object result = mCacheManager.getViewCacheData(
					GoStorePublicDefine.VIEW_TYPE_ITEMDETAIL, request);
			if (result != null) {
				mHasCacheData = true;
				if (result instanceof List<?>) {
					List<?> list = (List<?>) result;
					if (list.size() <= 0) {
						mHasCacheData = false;
					}
				}
				if (mHandler != null && mHasCacheData) {
					getCacheDataFromLocalFinish(result, action);
				}
			}
			else
			{
				getDetailDataFromNet(request);
			}
		}
	}
	private void getCacheDataFromLocalFinish(Object object, int action)
	{
		if (mHandler != null && mHasCacheData) {
			Message message = mHandler.obtainMessage();
			message.what = EVENT_GET_DATA_FINISH;
			message.arg1 = action;
			message.obj = object;
			mHandler.sendMessage(message);
		}
	}
	/**
	 * 初始化网络请求的方法
	 * 
	 * @param url
	 * @param nameValuePairs
	 * @param funid
	 * @return
	 */
	private THttpRequest createHttpRequest(String url, List<NameValuePair> nameValuePairs,
			int funid, final int action) {
		int state = GoStoreCacheManager.STATE_NEW;
		if (nameValuePairs != null) {
			int size = 0;
			size = nameValuePairs.size();
			if (size == 1)
			{
				state = Integer.parseInt(nameValuePairs.get(0).getValue());
				nameValuePairs = null;
			}
			else if (size > 1)
			{
				state = Integer.parseInt(nameValuePairs.get(size - 1).getValue());
				nameValuePairs.remove(size - 1);
			}
		}
		final int fState = state;
		THttpRequest request = null;
		// 获取POST请求数据
		byte[] postData = ThemeHttp.getPostData(mContext, nameValuePairs, funid);

		try {
			request = new THttpRequest(url, postData, new IConnectListener() {

				@Override
				public void onStart(THttpRequest arg0) {
				}

				@Override
				public void onFinish(THttpRequest arg0, IResponse arg1) {

					if (arg1.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
						if (mHandler != null && fState == GoStoreCacheManager.STATE_NEW) {
							Message message = mHandler.obtainMessage();
							message.what = EVENT_GET_DATA_FINISH;
							message.arg1 = action;
							message.obj = arg1.getResponse();
							mHandler.sendMessage(message);
						}
					}
					// 保存缓存数据
					GoStoreCacheManager cacheManager = GoStoreCacheManager.getInstance();
					if (cacheManager != null) {
						cacheManager.saveViewCacheData(GoStorePublicDefine.VIEW_TYPE_ITEMDETAIL,
								arg0, arg1.getResponse());
					}
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
					StatisticsData.saveHttpExceptionDate(mContext, arg0, arg1);
					if (mHandler != null) {
						Message message = mHandler.obtainMessage();
						message.what = EVENT_GET_DATA_EXCEPTION;
						message.arg1 = action;
						message.obj = Integer.valueOf(arg1);
						mHandler.sendMessage(message);
					}
				}
			});
			// 设置POST请求头
			request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);

			MainDataHttpOperator operator = new MainDataHttpOperator();
			request.setOperator(operator);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return request;
	}

	/**
	 * 从网络获取list部分数据
	 * 
	 * @author huyong
	 */
	private void getDetailDataFromNet(THttpRequest request) {
		if (request != null && mHttpAdapter != null) {
			mHttpAdapter.addTask(request);
		}
	}

	/**
	 * 初始化并注册广播接收器的方法
	 */
	private void initAndRegisterReceiver() {
		if (mContext != null) {
			mBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					if (intent != null) {
						String action = intent.getAction();
						// 如果是程序安装的广播
						if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
							notifyChange(ACTION_NEW_APP_INTALLED, STATE_RESPONSE_OK, null);
						} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
							notifyChange(ACTION_NEW_APP_UNINSTALL, STATE_RESPONSE_OK, null);
						}
					}
				}
			};
			// 程序安装广播的过滤器
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			intentFilter.addDataScheme("package");
			mContext.registerReceiver(mBroadcastReceiver, intentFilter);
		}
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub
		mIsRecycled = true;
		if (mBroadcastReceiver != null && mContext != null) {
			mContext.unregisterReceiver(mBroadcastReceiver);
			mBroadcastReceiver = null;
		}
		mHandler = null;
		mHttpAdapter = null;
		mChangeListener = null;
		mContext = null;
	}

}
