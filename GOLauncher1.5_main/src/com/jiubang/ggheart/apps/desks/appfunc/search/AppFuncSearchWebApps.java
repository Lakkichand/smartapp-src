package com.jiubang.ggheart.apps.desks.appfunc.search;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.MainDataHttpOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ListElementBean;
import com.jiubang.ggheart.launcher.LauncherEnv;

public class AppFuncSearchWebApps {

	private final int EVENT_GET_DATA_START = 0; // 开始获取数据
	private final int EVENT_GET_DATA_FINISH = 1; // 获取数据结束
	private final int EVENT_GET_DATA_EXCEPTION = 2; // 获取数据异常

	private Context mContext = null;
	private Handler mHandler = null;
	private WebResultHandler mWebResultHandler = null;

	private int mCurPage = 0;
	private int mTotalPage = 0;
	private int mRequestTimes = 0;
	private String mSearchKey = null;
	private boolean mIsSearching = false;

	/**
	 * 最近一次的网络请求对象
	 */
	private THttpRequest mLastRequest = null;

	private SimpleHttpAdapter mSimpleHttpAdapter = null;

	public AppFuncSearchWebApps(Context context) {
		mContext = context;
		initHandler();
		init();
	}

	public void searchWebApps(String key, WebResultHandler webResultHandler, String appuid) {
		// 在发起一次新的网络请求前把旧的取消掉
		cancelLastRequest();
		mWebResultHandler = webResultHandler;
		mSearchKey = key;
		resetData();

		getNextPageData();
	}

	private void resetData() {
		mCurPage = 0;
		mTotalPage = 0;
		mIsSearching = false;
		mRequestTimes = 0;
	}

	/**
	 * 获取下一页数据
	 * 
	 * @author huyong
	 */
	public void getNextPageData() {
		mRequestTimes++;
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("pn", String.valueOf(mCurPage + 1)));
		nameValuePairs.add(new BasicNameValuePair("key", mSearchKey));
		// add by chenguanyu 增加字段，用于区分功能表搜索和go精品搜索
		nameValuePairs.add(new BasicNameValuePair("source", "desktop"));
		nameValuePairs.add(new BasicNameValuePair("appuid", "4"));
		// end add
		if (isDataOver() && mRequestTimes > 1) {
			return;
		}
		// list列表
		getListData(GoStorePublicDefine.URL_HOST3, nameValuePairs, GoStorePublicDefine.FUNID_SEARCH);
	}

	public boolean isSearching() {
		return mIsSearching;
	}

	public boolean isDataOver() {
		return mCurPage >= mTotalPage;
	}

	/**
	 * 获取list部分数据
	 * 
	 * @author huyong
	 */
	private void getListData(String url, List<NameValuePair> nameValuePairs, int funid) {

		mIsSearching = true;
		try {
			// 获取POST请求数据
			byte[] postData = ThemeHttp.getPostData(mContext, nameValuePairs, funid);

			mLastRequest = new THttpRequest(url, postData, new IConnectListener() {
				Message message = null;

				@Override
				public void onStart(THttpRequest request) {
					mHandler.sendEmptyMessage(EVENT_GET_DATA_START);
				}

				@Override
				public void onFinish(THttpRequest request, IResponse response) {
					if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
						if (mHandler != null) {
							message = mHandler.obtainMessage(EVENT_GET_DATA_FINISH);
							message.obj = response.getResponse();
							mHandler.sendMessage(message);
						}
					}
				}

				@Override
				public void onException(THttpRequest request, int reason) {
					if (mHandler != null) {
						message = mHandler.obtainMessage(EVENT_GET_DATA_EXCEPTION);
						message.obj = Integer.valueOf(reason);
						mHandler.sendMessage(message);
					}
				}
			});
			// 设置POST请求头
			mLastRequest.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);

			MainDataHttpOperator operator = new MainDataHttpOperator();
			mLastRequest.setOperator(operator);

			if (mSimpleHttpAdapter != null) {
				mSimpleHttpAdapter.addTask(mLastRequest);
			}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);
				// 如果搜索没有被手工中断，搜索完成后需要通知ui刷新界面.否则不需要通知ui，因为手工中断说明对ui来讲这个消息已经没有意义了，ui不再需要这个消息也知道要怎么处理了
				if (mIsSearching) {

					// 搜索结束
					// mIsSearching = false;

					switch (msg.what) {
						case EVENT_GET_DATA_START : {
							if (mWebResultHandler != null) {
								mWebResultHandler.onHandleStart(mSearchKey, mRequestTimes);
							}
						}
							break;
						case EVENT_GET_DATA_FINISH : {

							// 数据返回完成
							if (msg.obj != null) {
								@SuppressWarnings("unchecked")
								ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) msg.obj;
								// 清空数据，以免在消息队列中一直持有数据不能释放内存
								msg.obj = null;
								if (listBeans != null && listBeans.size() > 0) {
									BaseBean baseBean = listBeans.get(0);
									if (baseBean != null && baseBean instanceof ListElementBean) {
										ListElementBean listElementBean = (ListElementBean) baseBean;
										mCurPage = listElementBean.mCurrentPage;
										mTotalPage = listElementBean.mTotalPage;
										int mDataCount = listElementBean.mElementCount;
										if (mDataCount > 0) {
											if (mWebResultHandler != null) {
												mWebResultHandler.onHandleFinish(mSearchKey,
														listElementBean);
											}
										} else {
											if (mWebResultHandler != null && mCurPage == 0) {
												mWebResultHandler.onHandleFinishWithoutData(
														mSearchKey, null);
											} else if (mWebResultHandler != null) {
												mWebResultHandler.onHandleException(mSearchKey,
														null);
											}
										}
									}
								} else {
									// 搜索结果没有正常返回。
									if (mWebResultHandler != null) {
										mWebResultHandler.onHandleException(mSearchKey, null);
									}
								}
							}
							mIsSearching = false;
						}
							break;
						case EVENT_GET_DATA_EXCEPTION : {
							if (mContext != null) {
								Toast.makeText(mContext, R.string.http_exception,
										Toast.LENGTH_SHORT).show();
							}
							if (mWebResultHandler != null) {
								mWebResultHandler.onHandleException(mSearchKey, msg.obj);
							}
						}
							mIsSearching = false;
							break;
						default :

							break;
					}
				}
			}

		};
	}

	public interface WebResultHandler {
		public void onHandleStart(String key, Object object);

		public void onHandleFinish(String key, Object object);

		public void onHandleException(String key, Object object);

		public void onHandleFinishWithoutData(String key, Object object);
	}

	/**
	 * 取消最近一次的网络请求
	 */
	public void cancelLastRequest() {
		mIsSearching = false;
		if (mLastRequest != null) {
			SimpleHttpAdapter.getInstance(mContext).cancelTask(mLastRequest);
		}
	}

	private void init() {
		mSimpleHttpAdapter = SimpleHttpAdapter.getInstance(mContext);
		// 设置最大并发连接数
		mSimpleHttpAdapter.setMaxConnectThreadNum(8);
	}
}
