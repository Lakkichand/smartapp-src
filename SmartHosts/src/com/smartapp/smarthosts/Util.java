package com.smartapp.smarthosts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.text.TextUtils;

public class Util {

	public static final String connectHTTP(String hostStr, String ipStr) {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
		HttpProtocolParams.setUseExpectContinue(params, true);

		HttpProcessor httpproc = new ImmutableHttpProcessor(
				new HttpRequestInterceptor[] { new RequestContent(),
						new RequestTargetHost(), new RequestConnControl(),
						new RequestUserAgent(), new RequestExpectContinue() });

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpContext context = new BasicHttpContext(null);
		HttpHost host = new HttpHost(hostStr, 80);

		DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
		ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();

		context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
		context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

		try {
			String target = "/";
			if (!conn.isOpen()) {
				Socket socket = new Socket(ipStr, 80);
				conn.bind(socket, params);
			}
			BasicHttpRequest request = new BasicHttpRequest("GET", target);
			request.setParams(params);
			httpexecutor.preProcess(request, httpproc, context);
			HttpResponse response = httpexecutor
					.execute(request, conn, context);
			response.setParams(params);
			httpexecutor.postProcess(response, httpproc, context);

			response.getStatusLine();
			EntityUtils.toString(response.getEntity());
			if (!connStrategy.keepAlive(response, context)) {
				conn.close();
			}
			if (response.getStatusLine().getStatusCode() >= 200
					&& response.getStatusLine().getStatusCode() < 400) {
				return null;
			} else {
				return response.getStatusLine().toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		} finally {
			try {
				conn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static final String connectHTTPS(String hostStr, String ipStr) {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
		HttpProtocolParams.setUseExpectContinue(params, true);

		HttpProcessor httpproc = new ImmutableHttpProcessor(
				new HttpRequestInterceptor[] { new RequestContent(),
						new RequestTargetHost(), new RequestConnControl(),
						new RequestUserAgent(), new RequestExpectContinue() });

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpContext context = new BasicHttpContext(null);
		HttpHost host = new HttpHost(hostStr, 443);

		DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
		ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();

		context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
		context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

		try {
			String targets = "/";
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			final long t1 = System.currentTimeMillis();
			if (!conn.isOpen()) {
				SSLSocket socket = (SSLSocket) factory.createSocket();
				socket.connect(new InetSocketAddress(ipStr, 443));
				String[] sup = socket.getSupportedCipherSuites();
				socket.setEnabledCipherSuites(sup);
				conn.bind(socket, params);
			}
			BasicHttpRequest request = new BasicHttpRequest("GET", targets);
			request.setParams(params);
			httpexecutor.preProcess(request, httpproc, context);
			HttpResponse response = httpexecutor
					.execute(request, conn, context);
			response.setParams(params);
			httpexecutor.postProcess(response, httpproc, context);

			response.getStatusLine();
			EntityUtils.toString(response.getEntity());
			if (!connStrategy.keepAlive(response, context)) {
				conn.close();
			}
			if (response.getStatusLine().getStatusCode() >= 200
					&& response.getStatusLine().getStatusCode() < 400) {
				return null;
			} else {
				return response.getStatusLine().toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		} finally {
			try {
				conn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String readAssetsFile(Context ctx, String fileName) {
		try {
			InputStream is = ctx.getAssets().open(fileName);

			int size = is.available();

			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			if (buffer != null) {
				return (new String(buffer, "UTF-8")).trim();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * °´ÐÐ¶ÁÈ¡×Ö·û´®
	 */
	public static List<String> readLine(String src) {
		try {
			if (src == null) {
				return null;
			}
			BufferedReader rd = new BufferedReader(new StringReader(src));
			String str = null;
			List<String> ret = new ArrayList<String>();
			while ((str = rd.readLine()) != null) {
				if (!TextUtils.isEmpty(str)) {
					ret.add(str);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
