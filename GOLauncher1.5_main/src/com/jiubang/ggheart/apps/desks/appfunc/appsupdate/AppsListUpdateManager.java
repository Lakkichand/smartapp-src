package com.jiubang.ggheart.apps.desks.appfunc.appsupdate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.zip.GZIPOutputStream;

import android.content.Context;

import com.gau.utils.net.HttpAdapter;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.jiubang.ggheart.apps.desks.net.CryptTool;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.MainDataHttpOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsAppsInfoData;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 程序列表更新管理
 * 
 * @author huyong
 * 
 */
public class AppsListUpdateManager {
	private Context mContext = null;

	private static AppsListUpdateManager mSelf = null;

	public static AppsListUpdateManager getInstance(Context context) {
		if (mSelf == null) {
			mSelf = new AppsListUpdateManager(context);
		}
		return mSelf;
	}

	private AppsListUpdateManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void startCheckUpdate(final HttpAdapter httpAdapter, final IConnectListener receiver,
			final boolean isPrintLog) {
		if (httpAdapter == null || receiver == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = GoStorePublicDefine.URL_HOST3;
				int funid = GoStorePublicDefine.FUNID_APPS_UPDATE;
				//需要打印log，则说明当前需要统计应用程序列表的点击次数。
				String staticsData = getStatisticsAppsInfoData(isPrintLog);
				byte[] postData = ThemeHttp.getAppsListPostData(mContext, null, funid, staticsData,
						isPrintLog);
				try {
					THttpRequest request = new THttpRequest(url, postData, receiver);
					// 设置POST请求头
					request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);
					MainDataHttpOperator operator = new MainDataHttpOperator();
					request.setOperator(operator);

					httpAdapter.addTask(request);

				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private String getStatisticsAppsInfoData(boolean isNeedClickCnt) {
		String statistics = StatisticsAppsInfoData.getAllAppsInfo(mContext, isNeedClickCnt);
		byte[] statisticsByte = null;
		// 对所有的统计数据进行加密
		if (statistics != null) {
			statistics = CryptTool.encrypt(statistics, Statistics.STATISTICS_DATA_ENCRYPT_KEY);
			try {
				statisticsByte = statistics.getBytes(Statistics.STATISTICS_DATA_CODE);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ByteArrayOutputStream byteOutputStream = null;
		GZIPOutputStream gzipOutputStream = null;
		try {
			byteOutputStream = new ByteArrayOutputStream();
			gzipOutputStream = new GZIPOutputStream(byteOutputStream);
			gzipOutputStream.write(statisticsByte);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (gzipOutputStream != null) {
					gzipOutputStream.flush();
					gzipOutputStream.close();
				}
				if (byteOutputStream != null) {
					byteOutputStream.flush();
					byteOutputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		String result = null;
		if (byteOutputStream != null) {
			// result = byteOutputStream.toByteArray();
			try {
				result = byteOutputStream.toString("ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byteOutputStream = null;
		}

		return result;
	}

}
