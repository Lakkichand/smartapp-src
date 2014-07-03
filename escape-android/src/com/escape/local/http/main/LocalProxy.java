package com.escape.local.http.main;

import java.io.IOException;

import com.escape.local.http.server.IHttpServer;

public class LocalProxy {
	public static void main(String[] args) {
		IHttpServer proxy = null;
		try {
			proxy = new IHttpServer();
			proxy.start(18081);
			System.out.println("escape start");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (proxy != null) {
				// proxy.shutdown();
			}
		}
	}
}
