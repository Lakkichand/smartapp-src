/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.escape.local.http.test;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
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

/**
 * Elemental example for executing a GET request.
 * <p>
 * Please note the purpose of this application is demonstrate the usage of
 * HttpCore APIs. It is NOT intended to demonstrate the most efficient way of
 * building an HTTP client.
 * 
 * 
 * 
 */
public class TestHttpsGet {

	public static void main(String[] args) {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
		HttpProtocolParams.setUseExpectContinue(params, true);

		HttpProcessor httpproc = new ImmutableHttpProcessor(
				new HttpRequestInterceptor[] {
						// Required protocol interceptors
						new RequestContent(), new RequestTargetHost(),
						// Recommended protocol interceptors
						new RequestConnControl(), new RequestUserAgent(),
						new RequestExpectContinue() });

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpContext context = new BasicHttpContext(null);
		HttpHost host = new HttpHost("localhost", 443);

		DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
		ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();

		context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
		context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

		try {

			String targets = "/fetch";
			int testCount = 2;
			int errorCount = 0;
			long totalTime = 0;
			int connectCount = 0;

			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();

			for (int i = 0; i < testCount; i++) {

				try {
					final long t1 = System.currentTimeMillis();
					if (!conn.isOpen()) {
						SSLSocket socket = (SSLSocket) factory.createSocket();
						socket.connect(new InetSocketAddress("localhost", 443));
						socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {

							@Override
							public void handshakeCompleted(
									HandshakeCompletedEvent event) {
								System.out.println("加密套件："
										+ event.getCipherSuite() + " 会话："
										+ event.getSession() + " 对方："
										+ event.getSession().getPeerHost());
								System.out.println("market.android.com"
										+ " handshakeCompleted:"
										+ (System.currentTimeMillis() - t1)
										+ "ms");
							}
						});
						String[] sup = socket.getSupportedCipherSuites();
						socket.setEnabledCipherSuites(sup);
						conn.bind(socket, params);
						connectCount++;
					}
					BasicHttpRequest request = new BasicHttpRequest("GET",
							targets);
					// System.out.println(">> Request URI: "
					// + request.getRequestLine().getUri());
					request.setParams(params);
					httpexecutor.preProcess(request, httpproc, context);
					HttpResponse response = httpexecutor.execute(request, conn,
							context);
					response.setParams(params);
					httpexecutor.postProcess(response, httpproc, context);

					String s1 = "<< Response: " + response.getStatusLine();
					String s2 = EntityUtils.toString(response.getEntity());
					// System.out.println("<< Response: "
					// + response.getStatusLine());
					// System.out.println(EntityUtils.toString(response
					// .getEntity()));
					// System.out.println("==============");
					if (!connStrategy.keepAlive(response, context)) {
						conn.close();
					} else {
						// System.out.println("Connection kept alive...");
					}
					System.out.println(i + "     用时："
							+ (System.currentTimeMillis() - t1) + "ms");
					totalTime += (System.currentTimeMillis() - t1);
				} catch (Exception e) {
					e.printStackTrace();
					errorCount++;
				}
			}
			System.out.println("connectCount = " + connectCount);
			System.out.println("errorCount = " + errorCount);
			System.out.println("平均用时：" + totalTime
					/ (testCount - errorCount + 0.0));

		} finally {
			try {
				conn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
