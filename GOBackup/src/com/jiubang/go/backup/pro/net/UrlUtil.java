package com.jiubang.go.backup.pro.net;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * URL联网类
 * 
 * @author songzhaochun
 */
public class UrlUtil {

	// 日志输出模块
	private static final String TAG = "Go Friends Network";

	// private static boolean isLogEnabled() { return
	// GoFriendProtocol.isConfigEnabledLog(); }
	private static void doLog(final String tag, final String msg) {
		Log.d(tag, msg);
	}

	public static String getUrlResult(String url) throws IOException {
		// if( isLogEnabled() ) doLog( TAG, "UrlResult: " + url);

		final int m1000 = 1000;
		final int m20 = 20;
		final int m30 = 30;
		HttpParams httpParameters = new BasicHttpParams();
		int timeoutConnection = m1000 * m20;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		HttpConnectionParams.setSoTimeout(httpParameters, m30 * m1000);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);

		HttpPost post = new HttpPost(url);
		HttpResponse resp = client.execute(post);
		String result = EntityUtils.toString(resp.getEntity(), "utf-8");
		// if( isLogEnabled() ) doLog( TAG, result );
		if (result == null || result.length() <= 0) {
			throw new IOException();
		}
		return result;
	}

}
