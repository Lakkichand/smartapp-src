package com.jiubang.ggheart.apps.gowidget.gostore;

import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.os.Looper;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.MainDataHttpOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 精品通用网络数据工具
 * 
 * @author zhouxuewen
 * 
 */
public class GoStoreHttpTool {

	private static GoStoreHttpTool sInstance = null;

	private Context mContext = null;

	private SimpleHttpAdapter mHttpAdapter;

	private GoStoreHttpTool(Context context) {
		mContext = context;
		Looper.prepare();
		GoStoreCore.prepare(context);
		SimpleHttpAdapter.build(context);
		mHttpAdapter = SimpleHttpAdapter.getInstance();
	}

	public synchronized static GoStoreHttpTool getInstance(Context context) {
		if (sInstance == null) {
			try {
				sInstance = new GoStoreHttpTool(GOLauncherApp.getContext());
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return sInstance;
	}

	/**
	 * 传入参数获取网络数据
	 * 
	 * @param funid
	 *            协议funid
	 * @param nameValuePairs
	 *            参数NameValuePair
	 * @param listener
	 *            数据返回的Listener
	 */
	public void getHttpData(int funid, ArrayList<NameValuePair> nameValuePairs,
			IConnectListener listener) {
		THttpRequest request = null;
		try {
			// 获取POST请求数据
			byte[] postData = ThemeHttp.getPostData(mContext, nameValuePairs, funid);
			request = new THttpRequest(GoStorePublicDefine.URL_HOST3, postData, listener);

			// 设置POST请求头
			request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);
			MainDataHttpOperator operator = new MainDataHttpOperator();
			request.setOperator(operator);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getDataForeNetByRequest(request);
	}

	public THttpRequest createTHttpRequest(int funid, ArrayList<NameValuePair> nameValuePairs,
			IConnectListener listener) {
		THttpRequest request = null;
		try {
			// 获取POST请求数据
			byte[] postData = ThemeHttp.getPostData(mContext, nameValuePairs, funid);
			request = new THttpRequest(GoStorePublicDefine.URL_HOST3, postData, listener);

			// 设置POST请求头
			request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);
			MainDataHttpOperator operator = new MainDataHttpOperator();
			request.setOperator(operator);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return request;
	}
	public void getDataForeNetByRequest(THttpRequest request) {
		if (request != null && mHttpAdapter != null) {
			mHttpAdapter.addTask(request);
		}
	}

	public synchronized static void destory() {
		if (sInstance != null) {
			sInstance.recycle();
			sInstance = null;
		}
	}

	private void recycle() {
		mHttpAdapter = null;

		mContext = null;
	}
}
