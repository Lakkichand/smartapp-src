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

package com.escape.local.http.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.ImmutableHttpProcessor;

/**
 * Basic, yet fully functional and spec compliant, HTTP/1.1 file server.
 * <p>
 * Please note the purpose of this application is demonstrate the usage of
 * HttpCore APIs. It is NOT intended to demonstrate the most efficient way of
 * building an HTTP file server.
 * 
 * 
 */
public class IHttpServer {

	private RequestListenerThread mListenerThread;

	public void start(int port) throws IOException {
		mListenerThread = new RequestListenerThread(port);
		mListenerThread.setDaemon(false);
		mListenerThread.start();
	}

	public void shutdown() {
		if (mListenerThread != null) {
			mListenerThread.shutdown();
		}
	}

	static class RequestListenerThread extends Thread {

		ExecutorService fixedThreadPool = Executors.newCachedThreadPool();

		private final ServerSocket serversocket;
		private final HttpParams params;
		private final IHttpService httpService;

		public void shutdown() {
			if (serversocket != null) {
				try {
					serversocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.interrupt();
		}

		public RequestListenerThread(int port) throws IOException {
			this.serversocket = new ServerSocket(port);
			this.params = new BasicHttpParams();
			this.params
					.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
					.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
							16 * 1024)
					/*
					 * .setBooleanParameter(
					 * CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
					 */
					.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
			/*
			 * .setParameter(CoreProtocolPNames.ORIGIN_SERVER,
			 * "HttpComponents/1.1")
			 */;

			// Set up the HTTP protocol processor
			HttpProcessor httpproc = new ImmutableHttpProcessor(
					new HttpResponseInterceptor[] {/*
													 * new ResponseDate(), new
													 * ResponseServer(), new
													 * ResponseContent(), new
													 * ResponseConnControl()
													 */});

			// Set up request handlers
			HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
			reqistry.register("*", new IHttpRequestHandler());

			// Set up the HTTP service
			this.httpService = new IHttpService(httpproc,
					new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory(), reqistry, null,
					this.params);
		}

		public void run() {
			System.out.println("Listening on port "
					+ this.serversocket.getLocalPort());
			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection
					Socket socket = this.serversocket.accept();
					IHttpServerConnection conn = new IHttpServerConnection();
//					System.out.println("Incoming connection from "
//							+ socket.getInetAddress());
					conn.bind(socket, this.params);

					// Start worker thread
					WorkerRunnable run = new WorkerRunnable(httpService, conn);
					fixedThreadPool.execute(run);
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					System.err
							.println("I/O error initialising connection thread: "
									+ e.getMessage());
					e.printStackTrace();
					break;
				}
			}
		}
	}

	static class WorkerRunnable implements Runnable {

		private final IHttpService httpservice;
		private final HttpServerConnection conn;

		public WorkerRunnable(final IHttpService httpservice,
				final HttpServerConnection conn) {
			this.httpservice = httpservice;
			this.conn = conn;
		}

		@Override
		public void run() {
//			System.out.println("New connection thread");
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.conn.isOpen()) {
					this.httpservice.handleRequest(this.conn, context);
				}
			} catch (ConnectionClosedException ex) {
				System.err.println("Client closed connection");
			} catch (IOException ex) {
				System.err.println("I/O error: " + ex.getMessage());
			} catch (HttpException ex) {
				System.err.println("Unrecoverable HTTP protocol violation: "
						+ ex.getMessage());
			} finally {
				try {
					this.conn.shutdown();
				} catch (IOException ignore) {
					ignore.printStackTrace();
				}
			}
		}

	}

}
