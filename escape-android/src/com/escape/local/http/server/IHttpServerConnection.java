package com.escape.local.http.server;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.HttpParams;

public class IHttpServerConnection extends DefaultHttpServerConnection {

	private HttpParams mParams;

	@Override
	public Socket getSocket() {
		return super.getSocket();
	}

	@Override
	public void bind(Socket socket, HttpParams params) throws IOException {
		mParams = params;
		super.bind(socket, params);
	}

	public HttpParams getParams() {
		return mParams;
	}

}
